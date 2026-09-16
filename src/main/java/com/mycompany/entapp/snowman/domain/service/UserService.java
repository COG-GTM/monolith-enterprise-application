/*
 * |-------------------------------------------------
 * | Copyright © 2017 Colin But. All rights reserved.
 * |-------------------------------------------------
 */
package com.mycompany.entapp.snowman.domain.service;

import com.mycompany.entapp.snowman.domain.model.PagedResult;
import com.mycompany.entapp.snowman.domain.model.User;
import com.mycompany.entapp.snowman.domain.model.UserSearchCriteria;

public interface UserService {
    User findUser(String userId);

    PagedResult<User> searchUsers(UserSearchCriteria criteria);

    void createUser(User user);

    void updateUser(User user);

    void deleteUser(int userId);
}
