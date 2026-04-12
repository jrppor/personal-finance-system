package com.jirapat.personalfinance.api.repository.specification;

import com.jirapat.personalfinance.api.entity.User;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.domain.Specification;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class UserSpecification {

    public static Specification<User> hasEmail(String email) {
        return (root, query, cb) -> email == null || email.isBlank()
                ? null
                : cb.equal(root.get("email"), email);
    }
}
