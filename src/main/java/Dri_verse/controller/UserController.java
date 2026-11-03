package Dri_verse.controller;

import org.springframework.dao.DuplicateKeyException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import Dri_verse.model.User;
import Dri_verse.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@RestController
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

   @PostMapping("/register")
public String registerUser(@RequestBody User user) {

    try {

        int result = userService.registerUser(user);

        if (result > 0) {
            return "User registered successfully";
        } else {
            return "Registration failed";
        }

    } catch (DuplicateKeyException e) {

        String message = e.getMessage();

        if (message.contains("users.email")) {
            return "Email already exists. Please use a different email.";
        }

        if (message.contains("users.unique_phone")) {
            return "Phone number already exists. Please use a different phone number.";
        }

        return "Email or phone number already exists.";
    }
}
    @PostMapping("/login")
public ResponseEntity<?> login(@RequestBody User user) {

    User loggedInUser = userService.loginUser(
            user.getEmail(),
            user.getPassword()
    );

    if (loggedInUser != null) {

        return ResponseEntity.ok(loggedInUser);

    } else {

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body("Invalid email or password");
    }
}
}