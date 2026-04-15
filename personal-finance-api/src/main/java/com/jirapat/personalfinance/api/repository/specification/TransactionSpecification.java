package com.jirapat.personalfinance.api.repository.specification;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.springframework.data.jpa.domain.Specification;

import com.jirapat.personalfinance.api.entity.Transaction;
import com.jirapat.personalfinance.api.entity.TransactionType;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class TransactionSpecification {

    public static Specification<Transaction> hasUserId(Long userId) {
        return (root, query, criteriaBuilder) -> userId == null
                ? null
                : criteriaBuilder.equal(root.get("user").get("id"), userId);
    }

    public static Specification<Transaction> hasAccountId(Long accountId) {
        return (root, query, criteriaBuilder) -> accountId == null
                ? null
                : criteriaBuilder.equal(root.get("account").get("id"), accountId);
    }

    public static Specification<Transaction> hasType(TransactionType type) {
        return (root, query, criteriaBuilder) -> type == null
                ? null
                : criteriaBuilder.equal(root.get("type"), type);
    }

    public static Specification<Transaction> transactionDateFrom(LocalDate dateFrom) {
        return (root, query, cb) -> dateFrom == null
                ? null
                : cb.greaterThanOrEqualTo(root.get("transactionDate"), dateFrom);
    }

    public static Specification<Transaction> transactionDateTo(LocalDate dateTo) {
        return (root, query, cb) -> dateTo == null
                ? null
                : cb.lessThanOrEqualTo(root.get("transactionDate"), dateTo);
    }

    public static Specification<Transaction> amountMin(BigDecimal min) {
        return (root, query, cb) -> min == null
                ? null
                : cb.greaterThanOrEqualTo(root.get("amount"), min);
    }

    public static Specification<Transaction> amountMax(BigDecimal max) {
        return (root, query, cb) -> max == null
                ? null
                : cb.lessThanOrEqualTo(root.get("amount"), max);
    }

    public static Specification<Transaction> hasCategoryId(Long categoryId) {
        return (root, query, criteriaBuilder) -> categoryId == null
                ? null
                : criteriaBuilder.equal(root.get("category").get("id"), categoryId);
    }
}
