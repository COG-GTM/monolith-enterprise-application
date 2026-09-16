/*
 * |-------------------------------------------------
 * | Copyright © 2017 Colin But. All rights reserved.
 * |-------------------------------------------------
 */
package com.mycompany.entapp.snowman.infrastructure.db.dao;

import com.mycompany.entapp.snowman.domain.model.User;
import com.mycompany.entapp.snowman.domain.model.UserSearchCriteria;

import java.util.List;

public interface UserDao {
    User findUser(int userId);

    List<User> searchUsers(UserSearchCriteria criteria);

    long countUsers(UserSearchCriteria criteria);

    void saveUser(User user);

    void removeUser(int userId);
}
