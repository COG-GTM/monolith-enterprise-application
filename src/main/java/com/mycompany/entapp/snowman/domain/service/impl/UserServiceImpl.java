/*
 * |-------------------------------------------------
 * | Copyright © 2017 Colin But. All rights reserved.
 * |-------------------------------------------------
 */
package com.mycompany.entapp.snowman.domain.service.impl;

import com.mycompany.entapp.snowman.infrastructure.db.dao.UserDao;
import com.mycompany.entapp.snowman.domain.model.PagedResult;
import com.mycompany.entapp.snowman.domain.model.User;
import com.mycompany.entapp.snowman.domain.model.UserSearchCriteria;
import com.mycompany.entapp.snowman.domain.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserServiceImpl implements UserService {

    private static final int DEFAULT_PAGE_SIZE = 25;

    @Autowired
    private UserDao userDao;

    @Override
    public User findUser(String userId) {
        return userDao.findUser(Integer.parseInt(userId));
    }

    @Override
    public PagedResult<User> searchUsers(UserSearchCriteria criteria) {
        if (criteria.getPageSize() <= 0) {
            criteria.setPageSize(DEFAULT_PAGE_SIZE);
        }
        if (criteria.getPage() < 1) {
            criteria.setPage(1);
        }

        List<User> users = userDao.searchUsers(criteria);
        long total = userDao.countUsers(criteria);
        return new PagedResult<User>(users, criteria.getPage(), criteria.getPageSize(), total);
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

}
