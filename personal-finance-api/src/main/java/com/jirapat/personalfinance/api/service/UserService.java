package com.jirapat.personalfinance.api.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.jirapat.personalfinance.api.dto.request.ChangePasswordRequest;
import com.jirapat.personalfinance.api.dto.request.UpdateProfileRequest;
import com.jirapat.personalfinance.api.dto.response.UserResponse;
import com.jirapat.personalfinance.api.entity.User;
import com.jirapat.personalfinance.api.exception.BadRequestException;
import com.jirapat.personalfinance.api.exception.ResourceNotFoundException;
import com.jirapat.personalfinance.api.mapper.UserMapper;
import com.jirapat.personalfinance.api.repository.UserRepository;
import com.jirapat.personalfinance.api.repository.specification.UserSpecification;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;

    @Transactional(readOnly = true)
    public UserResponse getProfile(Long userId) {
        User user = findById(userId);
        return userMapper.toUserResponse(user);
    }

    @Transactional(readOnly = true)
    public Page<UserResponse> getAllUsers(String email, Pageable pageable) {
        log.info("Fetching all users, page: {}, size: {}", pageable.getPageNumber(), pageable.getPageSize());

        Specification<User> spec = Specification.where(UserSpecification.hasEmail(email));

        return userRepository.findAll(spec, pageable)
                .map(userMapper::toUserResponse);
    }


    public UserResponse updateProfile(Long userId, UpdateProfileRequest request) {
        User user = findById(userId);

        if (request.getFirstName() != null) {
            user.setFirstName(request.getFirstName());
        }
        if (request.getLastName() != null) {
            user.setLastName(request.getLastName());
        }

        user = userRepository.save(user);
        log.info("Profile updated for userId: {}", userId);
        return userMapper.toUserResponse(user);
    }

    public void changePassword(Long userId, ChangePasswordRequest request) {
        User user = findById(userId);

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new BadRequestException("Current password is incorrect");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
        log.info("Password changed for userId: {}", userId);
    }

    @Transactional(readOnly = true)
    private User findById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", String.valueOf(userId)));
    }
}
