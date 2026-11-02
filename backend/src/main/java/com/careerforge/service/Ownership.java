package com.careerforge.service;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

public final class Ownership {
    private Ownership() {}
    public static void require(Long userId) {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !userId.equals(authentication.getPrincipal())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You cannot update another user's data");
        }
    }
}
