package com.jirapat.personalfinance.api.repository;


import com.jirapat.personalfinance.api.entity.Transaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long>, JpaSpecificationExecutor<Transaction> {

    @EntityGraph(attributePaths = {"account", "category"})
    Page<Transaction> findAll(Specification<Transaction> spec, Pageable pageable);
}
