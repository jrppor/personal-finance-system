package com.jirapat.personalfinance.api.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SavingsContributionResponse {
    private Long id;
    private BigDecimal amount;
    private String note;
    private LocalDateTime contributedAt;
}