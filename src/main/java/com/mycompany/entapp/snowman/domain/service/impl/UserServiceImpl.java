/*
 * |-------------------------------------------------
 * | Copyright © 2017 Colin But. All rights reserved.
 * |-------------------------------------------------
 */
package com.mycompany.entapp.snowman.domain.service.impl;

import com.mycompany.entapp.snowman.infrastructure.db.dao.UserDao;
import com.mycompany.entapp.snowman.domain.model.User;
import com.mycompany.entapp.snowman.domain.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserDao userDao;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Override
    public User findUser(String userId) {
        return userDao.findUser(Integer.parseInt(userId));
    }

    @Override
    public void createUser(User user){
        userDao.saveUser(user);
    }

    @Override
    public void updateUser(User user){
        userDao.saveUser(user);
    }

    @Override
    public void deleteUser(int userId) {
        userDao.removeUser(userId);
    }

    @Override
    public boolean authenticate(String username, String password) {
        User user = userDao.findByUsername(username);
        if (user == null) {
            return false;
        }
        return passwordEncoder.matches(password, user.getPassword());
    }

    @Override
    public User findByUsername(String username) {
        return userDao.findByUsername(username);
    }

}
