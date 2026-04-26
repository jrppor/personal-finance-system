package com.jirapat.personalfinance.api.repository;

import com.jirapat.personalfinance.api.entity.SavingsGoal;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface SavingsGoalRepository extends JpaRepository<SavingsGoal, Long>, JpaSpecificationExecutor<SavingsGoal> {

    @EntityGraph(attributePaths = "user")
    Page<SavingsGoal> findAll(Specification<SavingsGoal> spec, Pageable pageable);

}
