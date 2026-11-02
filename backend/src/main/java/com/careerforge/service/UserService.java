package com.careerforge.service;

import com.careerforge.model.User;
import com.careerforge.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder) {

        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public User registerUser(User user) {

        if (user.getName() == null || user.getName().isBlank()) {
            throw new RuntimeException("Name cannot be empty");
        }

        if (user.getEmail() == null || user.getEmail().isBlank()) {
            throw new RuntimeException("Email cannot be empty");
        }

        if (user.getPassword() == null || user.getPassword().isBlank()) {
            throw new RuntimeException("Password cannot be empty");
        }

        user.setId(null);
        user.setName(user.getName().trim());
        user.setEmail(user.getEmail().trim().toLowerCase(java.util.Locale.ROOT));
        if (user.getName().length() > 100 || user.getEmail().length() > 254 || !user.getEmail().matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) throw new IllegalArgumentException("Enter a valid name and email address");
        if (user.getPassword().length() < 8 || user.getPassword().getBytes(java.nio.charset.StandardCharsets.UTF_8).length > 72) throw new IllegalArgumentException("Use a password of at least 8 characters and no more than 72 UTF-8 bytes");
        if (userRepository.findByEmail(user.getEmail()).isPresent()) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.CONFLICT,
                    "Email is already registered"
            );
        }

        user.setPassword(
                passwordEncoder.encode(user.getPassword())
        );

        return userRepository.save(user);
    }

    @org.springframework.transaction.annotation.Transactional
    public User loginUser(String email, String password) {

        if (email == null || email.isBlank()
                || password == null || password.isBlank()) {

            throw new RuntimeException("Email and password are required");
        }

        User user = userRepository.lockByEmail(email.trim().toLowerCase(java.util.Locale.ROOT))
                .orElseThrow(() ->
                        new org.springframework.web.server.ResponseStatusException(
                                org.springframework.http.HttpStatus.UNAUTHORIZED,
                                "Invalid email or password"
                        ));

        if (!passwordEncoder.matches(
                password,
                user.getPassword())) {

            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.UNAUTHORIZED,
                    "Invalid email or password"
            );
        }

        return user;
    }

    public User getUser(Long userId) {

        return userRepository.findById(userId)
                .orElseThrow(() ->
                        new RuntimeException("User not found"));
    }
}
