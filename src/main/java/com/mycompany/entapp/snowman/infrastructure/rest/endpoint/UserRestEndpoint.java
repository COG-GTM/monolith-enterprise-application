/*
 * |-------------------------------------------------
 * | Copyright © 2017 Colin But. All rights reserved.
 * |-------------------------------------------------
 */
package com.mycompany.entapp.snowman.infrastructure.rest.endpoint;

import com.mycompany.entapp.snowman.domain.model.PagedResult;
import com.mycompany.entapp.snowman.domain.model.User;
import com.mycompany.entapp.snowman.domain.model.UserSearchCriteria;
import com.mycompany.entapp.snowman.domain.service.UserService;
import com.mycompany.entapp.snowman.infrastructure.rest.mappers.UserResourceMapper;
import com.mycompany.entapp.snowman.infrastructure.rest.resources.UserResource;
import com.mycompany.entapp.snowman.infrastructure.rest.resources.UserSearchResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/user")
public class UserRestEndpoint {

    @Autowired
    private UserService userService;

    @RequestMapping(value = "/{userId}", method = RequestMethod.GET)
    public ResponseEntity<UserResource> getUser(@PathVariable("userId") String userId) {
        User user = userService.findUser(userId);
        UserResource userResource = UserResourceMapper.mapUserToUserResource(user);
        return ResponseEntity.ok(userResource);
    }

    @RequestMapping(value = "/search", method = RequestMethod.GET)
    public ResponseEntity<UserSearchResponse> searchUsers(
            @RequestParam(value = "username", required = false) String username,
            @RequestParam(value = "email", required = false) String email,
            @RequestParam(value = "department", required = false) String department,
            @RequestParam(value = "sortColumn", required = false, defaultValue = "username") String sortColumn,
            @RequestParam(value = "sortDirection", required = false, defaultValue = "ASC") String sortDirection,
            @RequestParam(value = "page", required = false, defaultValue = "1") int page,
            @RequestParam(value = "pageSize", required = false, defaultValue = "25") int pageSize) {

        UserSearchCriteria criteria = new UserSearchCriteria();
        criteria.setUsername(username);
        criteria.setEmail(email);
        criteria.setDepartment(department);
        criteria.setSortColumn(sortColumn);
        criteria.setSortDirection(sortDirection);
        criteria.setPage(page);
        criteria.setPageSize(pageSize);

        PagedResult<User> result = userService.searchUsers(criteria);

        List<UserResource> resources = new ArrayList<UserResource>();
        for (User user : result.getItems()) {
            resources.add(UserResourceMapper.mapUserToUserResource(user));
        }

        UserSearchResponse response = new UserSearchResponse();
        response.setResults(resources);
        response.setPage(result.getPage());
        response.setPageSize(result.getPageSize());
        response.setTotalCount(result.getTotalCount());
        response.setTotalPages(result.getTotalPages());
        response.setHasNext(result.isHasNext());

        return ResponseEntity.ok(response);
    }

    @RequestMapping(value = "/create", method = RequestMethod.POST)
    public ResponseEntity createNewUser(@Valid UserResource userResource) {
        User user = UserResourceMapper.mapUserResourceToUser(userResource);
        userService.createUser(user);
        return ResponseEntity.ok().build();
    }

    @RequestMapping(value = "/update", method = RequestMethod.POST)
    public ResponseEntity updateExistingUser(@Valid UserResource userResource){
        User user = UserResourceMapper.mapUserResourceToUser(userResource);
        userService.updateUser(user);
        return ResponseEntity.ok().build();
    }

    @RequestMapping(value = "{userId}/delete", method = RequestMethod.DELETE)
    public ResponseEntity deleteUser(@PathVariable Integer userId) {
        userService.deleteUser(userId);
        return ResponseEntity.ok().build();
    }
}
