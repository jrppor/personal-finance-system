package com.jirapat.personalfinance.api.repository.specification;

import java.time.LocalDate;
import java.time.LocalTime;

import org.springframework.data.jpa.domain.Specification;

import com.jirapat.personalfinance.api.entity.Category;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CategorySpecification {

    public static Specification<Category> rootOnly() {
        return (root, query, cb) -> cb.isNull(root.get("parent"));
    }

    public static Specification<Category> belongsToUserOrDefault(Long userId) {
        return (root, query, cb) -> cb.or(
                cb.isNull(root.get("user")),
                cb.equal(root.get("user").get("id"), userId)
        );
    }

    public static Specification<Category> createdAfter(LocalDate dateFrom) {
        return  (root, query, cb) -> dateFrom == null
                ? null
                : cb.greaterThanOrEqualTo(root.get("createdAt"), dateFrom.atStartOfDay());
    }

    public static Specification<Category> createdBefore(LocalDate dateTo) {
        return  (root, query, cb) -> dateTo == null
                ? null
                : cb.lessThanOrEqualTo(root.get("createdAt"), dateTo.atTime(LocalTime.MAX));
    }
}
