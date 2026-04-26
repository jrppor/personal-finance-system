package com.jirapat.personalfinance.api.service;

import java.time.LocalDate;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.jirapat.personalfinance.api.dto.request.CreateCategoryRequest;
import com.jirapat.personalfinance.api.dto.request.UpdateCategoryRequest;
import com.jirapat.personalfinance.api.dto.response.CategoryResponse;
import com.jirapat.personalfinance.api.entity.Category;
import com.jirapat.personalfinance.api.entity.User;
import com.jirapat.personalfinance.api.exception.BadRequestException;
import com.jirapat.personalfinance.api.exception.ResourceNotFoundException;
import com.jirapat.personalfinance.api.exception.UnauthorizedException;
import com.jirapat.personalfinance.api.mapper.CategoryMapper;
import com.jirapat.personalfinance.api.repository.CategoryRepository;
import com.jirapat.personalfinance.api.repository.specification.CategorySpecification;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class CategoryService {

    private static final int MAX_CATEGORY_DEPTH = 2;

    private final SecurityService securityService;
    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;

    public Page<CategoryResponse> getAllCategories(LocalDate dateFrom, LocalDate dateTo, Pageable pageable) {
        Long currentUserId = securityService.getCurrentUserId();

        Specification<Category> spec = CategorySpecification.rootOnly()
                .and(CategorySpecification.belongsToUserOrDefault(currentUserId))
                .and(CategorySpecification.createdAfter(dateFrom))
                .and(CategorySpecification.createdBefore(dateTo));

        return categoryRepository.findAll(spec, pageable)
                .map(categoryMapper::toCategoryResponse);
    }

    @Transactional(readOnly = true)
    public CategoryResponse getCategoryById(Long id) {
        log.info("Fetching Category by id: {}", id);
        Category category = findCategoryById(id);
        validateVisibility(category);
        return categoryMapper.toCategoryResponse(category);
    }

    public CategoryResponse createCategory(CreateCategoryRequest request) {
        User currentUser = securityService.getCurrentUser();
        log.info("Creating category by user: {}", currentUser.getEmail());

        Category category = categoryMapper.toEntity(request);
        category.setUser(currentUser);

        if (request.getParentId() != null) {
            Category parent = findCategoryById(request.getParentId());
            validateCategoryDepth(parent);
            category.setParent(parent);
        }

        Category saved = categoryRepository.save(category);
        return categoryMapper.toCategoryResponse(saved);
    }

    public CategoryResponse updateCategory(Long id, UpdateCategoryRequest request) {
        User currentUser = securityService.getCurrentUser();
        log.info("Updating category {} by user: {}", id, currentUser.getEmail());

        Category category = findCategoryById(id);
        validateOwnership(category);

        categoryMapper.updateEntity(request, category);

        Category saved = categoryRepository.save(category);
        return categoryMapper.toCategoryResponse(saved);
    }

    public void deleteCategory(Long id) {
        log.info("Deleting category: {}", id);
        Category category = findCategoryById(id);
        validateOwnership(category);
        validateDeletable(category);

        categoryRepository.delete(category);
        log.info("Category deleted successfully: {}", id);
    }

    public Category findCategoryById(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category", "id", id.toString()));
    }

    private void validateVisibility(Category category) {
        if (Boolean.TRUE.equals(category.getIsDefault())) {
            return;
        }
        Long currentUserId = securityService.getCurrentUserId();
        if (category.getUser() == null || !category.getUser().getId().equals(currentUserId)) {
            throw new UnauthorizedException("You do not have permission to access this category");
        }
    }

    private void validateOwnership(Category category) {
        if (Boolean.TRUE.equals(category.getIsDefault())) {
            throw new BadRequestException("Cannot modify a default category");
        }
        Long currentUserId = securityService.getCurrentUserId();
        if (category.getUser() == null || !category.getUser().getId().equals(currentUserId)) {
            throw new UnauthorizedException("You do not have permission to modify this category");
        }
    }

    private void validateCategoryDepth(Category parentCategory) {
        if (parentCategory.getParent() != null) {
            throw new BadRequestException("Maximum category depth is " + MAX_CATEGORY_DEPTH + " levels. Cannot create subcategory under a subcategory.");
        }
    }

    private void validateDeletable(Category category) {
        if (categoryRepository.existsByParentId(category.getId())) {
            throw new BadRequestException("Cannot delete category that has subcategories. Please delete or move subcategories first.");
        }
    }
}
