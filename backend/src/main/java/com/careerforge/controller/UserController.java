package com.careerforge.controller;

import com.careerforge.dto.LoginRequest;
import com.careerforge.dto.LoginResponse;
import com.careerforge.dto.UserResponse;
import com.careerforge.model.User;
import com.careerforge.service.JwtService;
import com.careerforge.service.UserService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;
    private final JwtService jwtService;
    private final com.careerforge.service.AttemptLimiter limiter;

    public UserController(
            UserService userService,
            JwtService jwtService, com.careerforge.service.AttemptLimiter limiter) {

        this.userService = userService;
        this.jwtService = jwtService;
        this.limiter = limiter;
    }

    @PostMapping("/register")
    public UserResponse register(@RequestBody User user, jakarta.servlet.http.HttpServletRequest request) {

        limiter.check("register-ip:" + request.getRemoteAddr(), 30);
        limiter.account("register", user.getEmail());
        User savedUser = userService.registerUser(user);

        return new UserResponse(
                savedUser.getId(),
                savedUser.getName(),
                savedUser.getEmail()
        );
    }

    @org.springframework.transaction.annotation.Transactional
    @PostMapping("/login")
    public LoginResponse login(@RequestBody LoginRequest request, jakarta.servlet.http.HttpServletRequest servletRequest) {

        limiter.check("login-ip:" + servletRequest.getRemoteAddr(), 120);
        limiter.account("login", request.getEmail());
        User user = userService.loginUser(
                request.getEmail(),
                request.getPassword()
        );

        String token = jwtService.generateToken(
                user.getId(),
                user.getEmail()
        );

        return new LoginResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                token
        );
    }
}