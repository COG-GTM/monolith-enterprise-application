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
import org.springframework.stereotype.Service;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserDao userDao;

    @Override
    public User findUser(String userId) {
        return userDao.findUser(Integer.parseInt(userId));
    }

    @Override
    public void createUser(User user){
        user.setPassword(hashPassword(user.getPassword()));
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
        String hashedPassword = hashPassword(password);
        return hashedPassword.equals(user.getPassword());
    }

    private String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(password.getBytes());
            StringBuilder hexString = new StringBuilder();
            for (int i = 0; i < hash.length; i++) {
                String hex = Integer.toHexString(0xff & hash[i]);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not available", e);
        }
    }

}
