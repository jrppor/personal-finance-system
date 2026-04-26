package com.jirapat.personalfinance.api.repository.specification;

import java.time.LocalDate;
import java.time.LocalTime;

import org.springframework.data.jpa.domain.Specification;

import com.jirapat.personalfinance.api.entity.Account;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class AccountSpecification {

    public static Specification<Account> hasUserId(Long userId) {
        return (root, query, cb) -> userId == null
                ? null
                : cb.equal(root.get("user").get("id"), userId);
    }

    public static Specification<Account> createdAfter(LocalDate dateFrom) {
        return (root, query, cb) -> dateFrom == null
                ? null
                : cb.greaterThanOrEqualTo(root.get("createdAt"), dateFrom.atStartOfDay());
    }

    public static Specification<Account> createdBefore(LocalDate dateTo) {
        return (root, query, cb) -> dateTo == null
                ? null
                : cb.lessThanOrEqualTo(root.get("createdAt"), dateTo.atTime(LocalTime.MAX));
    }
}
