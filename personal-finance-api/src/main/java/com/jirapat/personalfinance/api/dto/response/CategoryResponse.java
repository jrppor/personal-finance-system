package com.jirapat.personalfinance.api.dto.response;

import java.time.LocalDateTime;
import java.util.List;

import com.jirapat.personalfinance.api.entity.CategoryType;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CategoryResponse {
    private Long id;
    private String user;
    private String name;
    private CategoryType type;
    private String icon;
    private String color;
    private Boolean isDefault;
    private Boolean isActive;
    private Long parentId;
    private String parentName;
    private List<CategoryResponse> children;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
