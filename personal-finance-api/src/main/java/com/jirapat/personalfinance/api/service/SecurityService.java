package com.jirapat.personalfinance.api.service;
import java.util.UUID;
import com.jirapat.personalfinance.api.entity.User;
import com.jirapat.personalfinance.api.exception.UnauthorizedException;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;


@Service
public class SecurityService {

    public User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new UnauthorizedException("User not authenticated");
        }

        Object principal = authentication.getPrincipal();

        if (principal instanceof User) {
            return (User) principal;
        }

        throw new UnauthorizedException("Invalid authentication principal");
    }

    public Long getCurrentUserId() {
        return getCurrentUser().getId();
    }

    public void verifyOwnership(UUID resourceOwnerId) {
        Long currentUserId = getCurrentUserId();
        if (!currentUserId.equals(resourceOwnerId)) {
            throw new UnauthorizedException(
                    "You don't have permission to access this resource"
            );
        }
    }

    public boolean isOwner(Long resourceOwnerId) {
        try {
            Long currentUserId = getCurrentUserId();
            return currentUserId.equals(resourceOwnerId);
        } catch (Exception e) {
            return false;
        }
    }
}

