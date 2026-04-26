package com.jirapat.personalfinance.api.repository.specification;

import org.springframework.data.jpa.domain.Specification;

import com.jirapat.personalfinance.api.entity.Frequency;
import com.jirapat.personalfinance.api.entity.RecurringTransaction;
import com.jirapat.personalfinance.api.entity.TransactionType;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class RecurringTransactionSpecification {

    public static Specification<RecurringTransaction> hasUserId(Long userId) {
        return (root, query, criteriaBuilder) -> userId == null
                ? null
                : criteriaBuilder.equal(root.get("user").get("id"), userId);
    }

    public static Specification<RecurringTransaction> hasAccountId(Long accountId) {
        return (root, query, criteriaBuilder) -> accountId == null
                ? null
                : criteriaBuilder.equal(root.get("account").get("id"), accountId);
    }

    public static Specification<RecurringTransaction> hasType(TransactionType type) {
        return (root, query, criteriaBuilder) -> type == null
                ? null
                : criteriaBuilder.equal(root.get("type"), type);
    }

    public static Specification<RecurringTransaction> hasFrequency(Frequency frequency) {
        return (root, query, criteriaBuilder) -> frequency == null
                ? null
                : criteriaBuilder.equal(root.get("frequency"), frequency);
    }

    public static Specification<RecurringTransaction> hasIsActive(Boolean isActive) {
        return (root, query, criteriaBuilder) -> isActive == null
                ? null
                : criteriaBuilder.equal(root.get("isActive"), isActive);
    }

    public static Specification<RecurringTransaction> hasCategoryId(Long categoryId) {
        return (root, query, criteriaBuilder) -> categoryId == null
                ? null
                : criteriaBuilder.equal(root.get("category").get("id"), categoryId);
    }
}
