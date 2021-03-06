package com.devezq.trackergastos.repository;

import com.devezq.trackergastos.domain.Category;
import com.devezq.trackergastos.exceptions.TgBadRequestException;
import com.devezq.trackergastos.exceptions.TgResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;

@Repository

public class CategoryRepositoryImpl implements CategoryRepository {

    private static final String SQL_FIND_BY_ID = "SELECT C.CATEGORY_ID, C.USER_ID, C.TITLE, C.DESCRIPTION, " +
    "COALESCE(SUM(T.AMOUNT),0) TOTAL_EXPENSE FROM TG_TRANSACTIONS T RIGHT OUTER JOIN TG_CATEGORIES C ON C.CATEGORY_ID = T.CATEGORY_ID "+
    "WHERE C.USER_ID = ? AND C.CATEGORY_ID = ? GROUP BY C.CATEGORY_ID";
    private static final String SQL_CREATE ="INSERT INTO TG_CATEGORIES (CATEGORY_ID, USER_ID, TITLE, DESCRIPTION) "+
            "VALUES (NEXTVAL('TG_CATEGORIES_SEQ'), ?, ?, ?)";
    private static final String SQL_FIND_ALL = "SELECT C.CATEGORY_ID, C.USER_ID, C.TITLE, C.DESCRIPTION, " +
            "COALESCE(SUM(T.AMOUNT),0) TOTAL_EXPENSE FROM TG_TRANSACTIONS T RIGHT OUTER JOIN TG_CATEGORIES C ON C.CATEGORY_ID = T.CATEGORY_ID "+
            "WHERE C.USER_ID = ? GROUP BY C.CATEGORY_ID";
    private static final String SQL_UPDATE = "UPDATE TG_CATEGORIES SET TITLE = ?, DESCRIPTION = ? " +
            "WHERE USER_ID = ? AND CATEGORY_ID = ?";
    private static final String SQL_DELETE_CATEGORY = "DELETE FROM TG_CATEGORIES WHERE USER_ID = ? AND CATEGORY_ID = ?";
    private static final String SQL_DELETE_ALL_TRANSACTIONS = "DELETE FROM TG_TRANSACTIONS WHERE USER_ID = ? AND CATEGORY_ID = ?";
    @Autowired
    JdbcTemplate jdbcTemplate;

    @Override
    public List<Category> findAll(Integer userId) throws TgResourceNotFoundException {
        return jdbcTemplate.query(SQL_FIND_ALL, categoryRowMapper, userId);
    }

    @Override
    public Category findById(Integer userId, Integer categoryId) throws TgResourceNotFoundException {
        try {
            return jdbcTemplate.queryForObject(SQL_FIND_BY_ID, categoryRowMapper, userId, categoryId);
        } catch (Exception e) {
            throw new TgResourceNotFoundException("Category not found");
        }
    }

    @Override
    public Integer create(Integer userId, String title, String description) throws TgBadRequestException {
        try {
            KeyHolder keyHolder = new GeneratedKeyHolder();
            jdbcTemplate.update(connection -> {
                PreparedStatement ps = connection.prepareStatement(SQL_CREATE, Statement.RETURN_GENERATED_KEYS);
                ps.setInt(1, userId);
                ps.setString(2, title);
                ps.setString(3, description);
                return ps;
            }, keyHolder);
            return (Integer) keyHolder.getKeys().get("CATEGORY_ID");
        } catch (Exception e) {
            throw new TgBadRequestException("Invalid request");
        }
    }

    @Override
    public void update(Integer userId, Integer categoryId, Category category) throws TgBadRequestException {
        try {
            jdbcTemplate.update(SQL_UPDATE, category.getTitle(), category.getDescription(), userId, categoryId);
        } catch (Exception e) {
            throw new TgBadRequestException("Invalid request");
        }
    }

    @Override
    public void removeById(Integer userId, Integer categoryId) {
        this.removeAllCatTransactions(userId, categoryId);
        jdbcTemplate.update(SQL_DELETE_CATEGORY, userId, categoryId);
    }

    private void removeAllCatTransactions(Integer userId, Integer categoryId){
        jdbcTemplate.update(SQL_DELETE_ALL_TRANSACTIONS, userId, categoryId);
    }

    private RowMapper<Category> categoryRowMapper = ((rs, rowNum)-> {
        return new Category(rs.getInt("CATEGORY_ID"),
                rs.getInt("USER_ID"),
                rs.getString("TITLE"),
                rs.getString("DESCRIPTION"),
                rs.getDouble("TOTAL_EXPENSE"));
    });
}
