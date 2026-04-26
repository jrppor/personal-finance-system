package com.jirapat.personalfinance.api.repository;

import java.time.LocalDate;
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

import com.jirapat.personalfinance.api.entity.RecurringTransaction;

@Repository
public interface RecurringTransactionRepository extends JpaRepository<RecurringTransaction, Long>, JpaSpecificationExecutor<RecurringTransaction> {

    @EntityGraph(attributePaths = {"account", "category"})
    @Override
    Page<RecurringTransaction> findAll(Specification<RecurringTransaction> spec, Pageable pageable);

        @EntityGraph(attributePaths = {"user", "account", "category"})
        @Query("""
                        SELECT rt FROM RecurringTransaction rt
                        WHERE rt.isActive = true
                            AND rt.nextOccurrence <= :processingDate
                        ORDER BY rt.nextOccurrence ASC, rt.id ASC
                        """)
        List<RecurringTransaction> findDueForProcessing(@Param("processingDate") LocalDate processingDate);
}
