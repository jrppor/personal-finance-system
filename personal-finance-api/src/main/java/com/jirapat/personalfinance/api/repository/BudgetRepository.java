package com.jirapat.personalfinance.api.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.jirapat.personalfinance.api.entity.Budget;
import com.jirapat.personalfinance.api.entity.BudgetPeriod;

@Repository
public interface BudgetRepository extends JpaRepository<Budget, Long>, JpaSpecificationExecutor<Budget> {

    @EntityGraph(attributePaths = "category")
    Page<Budget> findAll(Specification<Budget> spec, Pageable pageable);

    @Query("SELECT CASE WHEN COUNT(b) > 0 THEN true ELSE false END FROM Budget b " +
           "WHERE b.period = :period AND b.user.id = :userId " +
           "AND (:categoryId IS NULL AND b.category IS NULL OR b.category.id = :categoryId)")
    boolean existsByPeriodAndCategoryIdAndUserId(
            @Param("period") BudgetPeriod period,
            @Param("categoryId") Long categoryId,
            @Param("userId") Long userId);

    @EntityGraph(attributePaths = "category")
    List<Budget> findByUserIdAndIsActiveTrue(Long userId);
}
