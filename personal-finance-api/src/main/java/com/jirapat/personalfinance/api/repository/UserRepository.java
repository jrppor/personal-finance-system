package com.jirapat.personalfinance.api.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.jirapat.personalfinance.api.entity.User;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Page<User> findAll(Specification<User> spec, Pageable pageable);

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);
}
