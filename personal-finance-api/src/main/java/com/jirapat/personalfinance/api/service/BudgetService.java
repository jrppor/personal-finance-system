package com.jirapat.personalfinance.api.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.jirapat.personalfinance.api.dto.request.CreateBudgetRequest;
import com.jirapat.personalfinance.api.dto.request.UpdateBudgetRequest;
import com.jirapat.personalfinance.api.dto.response.BudgetResponse;
import com.jirapat.personalfinance.api.dto.response.BudgetStatusResponse;
import com.jirapat.personalfinance.api.entity.Budget;
import com.jirapat.personalfinance.api.entity.BudgetPeriod;
import com.jirapat.personalfinance.api.entity.BudgetStatus;
import com.jirapat.personalfinance.api.entity.Category;
import com.jirapat.personalfinance.api.entity.TransactionType;
import com.jirapat.personalfinance.api.entity.User;
import com.jirapat.personalfinance.api.exception.DuplicateResourceException;
import com.jirapat.personalfinance.api.exception.ResourceNotFoundException;
import com.jirapat.personalfinance.api.exception.UnauthorizedException;
import com.jirapat.personalfinance.api.mapper.BudgetMapper;
import com.jirapat.personalfinance.api.repository.BudgetRepository;
import com.jirapat.personalfinance.api.repository.CategoryRepository;
import com.jirapat.personalfinance.api.repository.TransactionRepository;
import com.jirapat.personalfinance.api.repository.specification.BudgetSpecification;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class BudgetService {

    private final SecurityService securityService;
    private final BudgetRepository budgetRepository;
    private final CategoryRepository categoryRepository;
    private final TransactionRepository transactionRepository;
    private final BudgetMapper budgetMapper;

    @Transactional(readOnly = true)
    public Page<BudgetResponse> getAllBudgets(Long categoryId, BudgetPeriod period, Pageable pageable) {
        Long currentUserId = securityService.getCurrentUserId();
        Specification<Budget> spec = BudgetSpecification.hasUserId(currentUserId)
                .and(BudgetSpecification.hasCategoryId(categoryId))
                .and(BudgetSpecification.hasPeriod(period));

        return budgetRepository.findAll(spec, pageable)
                .map(budgetMapper::toBudgetResponse);
    }

    @Transactional(readOnly = true)
    public BudgetResponse getBudgetById(Long id) {
        Long currentUserId = securityService.getCurrentUserId();
        Budget budget = findBudgetById(id);
        validateOwnership(budget, currentUserId);

        return budgetMapper.toBudgetResponse(budget);
    }

    public BudgetResponse createBudget(CreateBudgetRequest request) {
        User currentUser = securityService.getCurrentUser();
        log.info("Creating budget '{}' for user id: {}", request.getName(), currentUser.getId());

        boolean exists = budgetRepository.existsByPeriodAndCategoryIdAndUserId(
                request.getPeriod(), request.getCategoryId(), currentUser.getId());
        if (exists) {
            throw new DuplicateResourceException("Budget", "period+category",
                    request.getPeriod() + "+" + request.getCategoryId());
        }

        Budget budget = budgetMapper.toEntity(request);
        budget.setUser(currentUser);

        if (request.getCategoryId() != null) {
            Category category = findCategoryById(request.getCategoryId());
            budget.setCategory(category);
        }

        Budget saved = budgetRepository.save(budget);
        log.info("Budget created with id: {}", saved.getId());
        return budgetMapper.toBudgetResponse(saved);
    }

    public BudgetResponse updateBudget(Long id, UpdateBudgetRequest request) {
        Long currentUserId = securityService.getCurrentUserId();
        log.info("Updating budget id: {}", id);

        Budget budget = findBudgetById(id);
        validateOwnership(budget, currentUserId);

        budgetMapper.updateEntity(request, budget);

        if (request.getCategoryId() != null) {
            Category category = findCategoryById(request.getCategoryId());
            budget.setCategory(category);
        } else {
            budget.setCategory(null);
        }

        Budget saved = budgetRepository.save(budget);
        return budgetMapper.toBudgetResponse(saved);
    }

    public void deleteBudget(Long id) {
        Long currentUserId = securityService.getCurrentUserId();
        log.info("Deleting budget id: {}", id);

        Budget budget = findBudgetById(id);
        validateOwnership(budget, currentUserId);

        budgetRepository.delete(budget);
        log.info("Budget deleted successfully: {}", id);
    }

    @Transactional(readOnly = true)
    public BudgetStatusResponse getBudgetStatus(Long budgetId) {
        Long currentUserId = securityService.getCurrentUserId();
        Budget budget = findBudgetById(budgetId);
        validateOwnership(budget, currentUserId);

        return calculateBudgetStatus(budget, currentUserId);
    }

    @Transactional(readOnly = true)
    public List<BudgetStatusResponse> getAllBudgetStatuses() {
        Long currentUserId = securityService.getCurrentUserId();
        List<Budget> budgets = budgetRepository.findByUserIdAndIsActiveTrue(currentUserId);

        return budgets.stream()
                .map(budget -> calculateBudgetStatus(budget, currentUserId))
                .toList();
    }

    // -- Private helpers --

    private BudgetStatusResponse calculateBudgetStatus(Budget budget, Long userId) {
        LocalDate[] periodDates = calculateCurrentPeriod(budget);
        LocalDate periodStart = periodDates[0];
        LocalDate periodEnd = periodDates[1];

        BigDecimal spentAmount;
        List<BudgetStatusResponse.TopExpense> topExpenses;

        if (budget.getCategory() != null) {
            List<Long> categoryIds = categoryRepository.findCategoryAndChildrenIds(
                    budget.getCategory().getId());
            spentAmount = transactionRepository.sumAmountByUserAndCategoriesAndTypeAndDateRange(
                    userId, categoryIds, TransactionType.EXPENSE, periodStart, periodEnd);
            topExpenses = buildTopExpenses(
                    transactionRepository.findTopExpensesByCategories(
                            userId, categoryIds, TransactionType.EXPENSE, periodStart, periodEnd));
        } else {
            spentAmount = transactionRepository.sumAmountByUserAndTypeAndDateRange(
                    userId, TransactionType.EXPENSE, periodStart, periodEnd);
            topExpenses = buildTopExpenses(
                    transactionRepository.findTopExpensesGroupedByCategory(
                            userId, TransactionType.EXPENSE, periodStart, periodEnd));
        }

        BigDecimal budgetAmount = budget.getAmount();
        BigDecimal remainingAmount = budgetAmount.subtract(spentAmount);

        double usagePercentage = budgetAmount.compareTo(BigDecimal.ZERO) > 0
                ? spentAmount.multiply(BigDecimal.valueOf(100))
                    .divide(budgetAmount, 2, RoundingMode.HALF_UP).doubleValue()
                : 0.0;

        LocalDate today = LocalDate.now();
        long daysRemaining = Math.max(0, ChronoUnit.DAYS.between(today, periodEnd));

        BigDecimal dailyBudgetRemaining = daysRemaining > 0
                ? remainingAmount.divide(BigDecimal.valueOf(daysRemaining), 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        BudgetStatus status;
        if (usagePercentage >= 100) {
            status = BudgetStatus.EXCEEDED;
        } else if (usagePercentage >= 80) {
            status = BudgetStatus.WARNING;
        } else {
            status = BudgetStatus.ON_TRACK;
        }

        return BudgetStatusResponse.builder()
                .budgetId(budget.getId())
                .name(budget.getName())
                .budgetAmount(budgetAmount)
                .spentAmount(spentAmount)
                .remainingAmount(remainingAmount)
                .usagePercentage(usagePercentage)
                .periodStart(periodStart)
                .periodEnd(periodEnd)
                .status(status)
                .daysRemaining(daysRemaining)
                .dailyBudgetRemaining(dailyBudgetRemaining)
                .topExpenses(topExpenses)
                .build();
    }

    private LocalDate[] calculateCurrentPeriod(Budget budget) {
        LocalDate today = LocalDate.now();
        return switch (budget.getPeriod()) {
            case DAILY -> new LocalDate[]{today, today};
            case WEEKLY -> {
                LocalDate weekStart = today.with(DayOfWeek.MONDAY);
                yield new LocalDate[]{weekStart, weekStart.plusDays(6)};
            }
            case MONTHLY -> {
                LocalDate monthStart = today.withDayOfMonth(1);
                LocalDate monthEnd = today.withDayOfMonth(today.lengthOfMonth());
                yield new LocalDate[]{monthStart, monthEnd};
            }
            case YEARLY -> {
                LocalDate yearStart = LocalDate.of(today.getYear(), 1, 1);
                LocalDate yearEnd = LocalDate.of(today.getYear(), 12, 31);
                yield new LocalDate[]{yearStart, yearEnd};
            }
            case CUSTOM -> {
                LocalDate endDate = budget.getEndDate() != null ? budget.getEndDate() : today;
                yield new LocalDate[]{budget.getStartDate(), endDate};
            }
        };
    }

    private List<BudgetStatusResponse.TopExpense> buildTopExpenses(List<Object[]> results) {
        return results.stream()
                .limit(5)
                .map(row -> BudgetStatusResponse.TopExpense.builder()
                        .category((String) row[0])
                        .amount((BigDecimal) row[1])
                        .build())
                .toList();
    }

    private Budget findBudgetById(Long id) {
        return budgetRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Budget", "id", id.toString()));
    }

    private Category findCategoryById(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category", "id", id.toString()));
    }

    private void validateOwnership(Budget budget, Long userId) {
        if (!budget.getUser().getId().equals(userId)) {
            throw new UnauthorizedException("You don't have permission to access this budget");
        }
    }
}
