package com.jirapat.personalfinance.api.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class ContributeRequest {

    @NotNull(message = "Amount is required")
    @Positive(message = "Amount must be greater 0")
    private BigDecimal amount;

    private String note;
}
