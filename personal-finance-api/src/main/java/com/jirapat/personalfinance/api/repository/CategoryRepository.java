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

import com.jirapat.personalfinance.api.entity.Category;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long>, JpaSpecificationExecutor<Category> {

    @EntityGraph(attributePaths = {"user", "children"})
    Page<Category> findAll(Specification<Category> spec, Pageable pageable);

    boolean existsByParentId(Long parentId);

    @Query("SELECT c.id FROM Category c WHERE c.id = :categoryId OR c.parent.id = :categoryId")
    List<Long> findCategoryAndChildrenIds(@Param("categoryId") Long categoryId);
}
