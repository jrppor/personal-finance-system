package com.jirapat.personalfinance.api.repository.specification;

import com.jirapat.personalfinance.api.entity.Budget;
import com.jirapat.personalfinance.api.entity.BudgetPeriod;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.domain.Specification;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class BudgetSpecification {

    public static Specification<Budget> hasUserId(Long userId) {
        return (root, query, criteriaBuilder) -> userId == null
                ? null
                : criteriaBuilder.equal(root.get("user").get("id"), userId);
    }

    public static Specification<Budget> hasCategoryId(Long categoryId) {
        return (root, query, criteriaBuilder) -> categoryId == null
                ? null
                : criteriaBuilder.equal(root.get("category").get("id"), categoryId);
    }

    public static Specification<Budget> hasPeriod(BudgetPeriod period) {
        return (root, query, criteriaBuilder) -> period == null
                ? null
                : criteriaBuilder.equal(root.get("period"), period);
    }
}
