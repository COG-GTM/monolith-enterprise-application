/*
 * |-------------------------------------------------
 * | Copyright © 2017 Colin But. All rights reserved.
 * |-------------------------------------------------
 */
package com.mycompany.entapp.snowman.infrastructure.db.dao;

import com.mycompany.entapp.snowman.domain.model.User;

import java.util.List;

public interface UserDao {
    User findUser(int userId);

    List<User> searchUsersByUsername(String username);

    void saveUser(User user);

    void removeUser(int userId);
}
