package com.devezq.trackergastos.services;

import com.devezq.trackergastos.domain.User;
import com.devezq.trackergastos.exceptions.TgAuthException;
import com.devezq.trackergastos.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.regex.Pattern;

@Service
@Transactional
public class UserServiceImpl implements UserService{

    @Autowired
    UserRepository userRepository;

    @Override
    public User validateUser(String email, String password) throws TgAuthException {
        if (email == null) throw new TgAuthException("Email can't be null");
        email = email.toLowerCase();
        return userRepository.findByEmailAndPassword(email, password);
    }

    @Override
    public User registerUser(String firstName, String lastName, String email, String password) throws TgAuthException {
        Pattern pattern = Pattern.compile("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,6}$");
        if (email == null) throw new TgAuthException("Email can't be null");
        email = email.toLowerCase();
        if(!pattern.matcher(email).matches()) {throw new TgAuthException(email);}
        Integer count = userRepository.getCountByEmail(email);
        if(count > 0)
            throw new TgAuthException("Email already in use");
        Integer userId = userRepository.create(firstName, lastName, email, password);
        return userRepository.findById(userId);
    }
}
