/*
 * |-------------------------------------------------
 * | Copyright © 2017 Colin But. All rights reserved.
 * |-------------------------------------------------
 */
package com.mycompany.entapp.snowman.domain.service.impl;

import com.mycompany.entapp.snowman.infrastructure.db.dao.UserDao;
import com.mycompany.entapp.snowman.domain.model.User;
import com.mycompany.entapp.snowman.domain.service.UserService;
import com.mycompany.entapp.snowman.infrastructure.security.PasswordHasher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
        hashPassword(user);
        userDao.saveUser(user);
    }

    @Override
    public void updateUser(User user){
        hashPassword(user);
        userDao.saveUser(user);
    }

    @Override
    public void deleteUser(int userId) {
        userDao.removeUser(userId);
    }

    private static void hashPassword(User user) {
        String password = user.getPassword();
        if (password != null && !PasswordHasher.isHash(password)) {
            user.setPassword(PasswordHasher.hash(password));
        }
    }

}
