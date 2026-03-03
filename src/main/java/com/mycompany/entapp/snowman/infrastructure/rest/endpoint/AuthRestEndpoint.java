/*
 * |-------------------------------------------------
 * | Copyright © 2017 Colin But. All rights reserved.
 * |-------------------------------------------------
 */
package com.mycompany.entapp.snowman.infrastructure.rest.endpoint;

import com.mycompany.entapp.snowman.domain.service.UserService;
import com.mycompany.entapp.snowman.infrastructure.rest.resources.AuthRequest;
import com.mycompany.entapp.snowman.infrastructure.rest.resources.AuthResponse;
import com.mycompany.entapp.snowman.infrastructure.security.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthRestEndpoint {

    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtil jwtUtil;

    @RequestMapping(value = "/login", method = RequestMethod.POST)
    public ResponseEntity<?> login(@RequestBody AuthRequest authRequest) {
        boolean authenticated = userService.authenticate(authRequest.getUsername(), authRequest.getPassword());
        if (authenticated) {
            String token = jwtUtil.generateToken(authRequest.getUsername());
            return ResponseEntity.ok(new AuthResponse(token));
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }
}
