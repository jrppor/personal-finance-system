package com.jirapat.personalfinance.api.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import com.jirapat.personalfinance.api.entity.SavingsGoalStatus;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SavingsGoalResponse {
    private Long id;
    private String name;
    private BigDecimal targetAmount;
    private BigDecimal currentAmount;
    private BigDecimal progressPercentage;
    private BigDecimal remainingAmount;
    private LocalDate deadline;
    private LocalDate estimatedCompletionDate;
    private Long daysUntilDeadline;
    private Boolean isOnTrack;
    private BigDecimal suggestedMonthlyContribution;
    private List<SavingsContributionResponse> recentContributions;
    private SavingsGoalStatus status;
    private String icon;
    private String color;
    private LocalDateTime createdAt;
}
