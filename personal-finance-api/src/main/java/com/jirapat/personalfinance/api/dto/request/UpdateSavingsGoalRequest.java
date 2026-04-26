package com.jirapat.personalfinance.api.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
public class UpdateSavingsGoalRequest {

    @NotNull(message = "Name is required")
    @Size(max = 100, message = "Name must not exceed 100 characters")
    private String name;

    @NotNull(message = "Target amount is required")
    @Positive(message = "Target amount must be greater than 0")
    private BigDecimal targetAmount;

    private LocalDate deadline;

    @Size(max = 50, message = "Name must not exceed 100 characters")
    private String icon;

    @Size(max =  7, message = "Color must not exceed 7 characters")
    private String color;
}
