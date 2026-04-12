package com.jirapat.personalfinance.api.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateAccountRequest {
    @NotBlank(message = "Name is required")
    @Size(max = 100, message = "Name must not exceed 100 characters")
    private String name;

    @NotBlank(message = "Type is required")
    @Size(max = 30, message = "Type must not exceed 30 characters")
    private String type;

    private BigDecimal balance;

    private String currency;

    private String color;

    private String icon;

    private String description;

    private Boolean isActive;
}
