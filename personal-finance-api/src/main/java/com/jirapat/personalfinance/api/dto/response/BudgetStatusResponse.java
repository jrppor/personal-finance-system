package com.jirapat.personalfinance.api.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import com.jirapat.personalfinance.api.entity.BudgetStatus;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class BudgetStatusResponse {
    private Long budgetId;
    private String name;
    private BigDecimal budgetAmount;
    private BigDecimal spentAmount;
    private BigDecimal remainingAmount;
    private double usagePercentage;
    private LocalDate periodStart;
    private LocalDate periodEnd;
    private BudgetStatus status;
    private long daysRemaining;
    private BigDecimal dailyBudgetRemaining;
    private List<TopExpense> topExpenses;

    @Data
    @Builder
    public static class TopExpense {
        private String category;
        private BigDecimal amount;
    }
}
