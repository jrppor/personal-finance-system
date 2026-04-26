package com.jirapat.personalfinance.api.repository.specification;

import com.jirapat.personalfinance.api.entity.SavingsGoal;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.domain.Specification;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class SavingsGoalSpecification {

    public static Specification<SavingsGoal> hasUserId(Long userId) {
        return (root, query, criteriaBuilder) -> userId == null
                ? null
                : criteriaBuilder.equal(root.get("user").get("id"), userId);
    }


}
