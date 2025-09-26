package org.arsw.maze_rush.users.controller;

import org.arsw.maze_rush.users.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;



@RestController
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping("path/v1/login")
    public String getMethodName(@RequestParam String param) {
        return new String();
    }

    @GetMapping("path/v1/users")
    public String getAllUsers() {
        return new String();
    }

}
