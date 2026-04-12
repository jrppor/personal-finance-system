package com.jirapat.personalfinance.api.repository.specification;

import com.jirapat.personalfinance.api.entity.Account;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.time.LocalTime;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class AccountSpecification {

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
