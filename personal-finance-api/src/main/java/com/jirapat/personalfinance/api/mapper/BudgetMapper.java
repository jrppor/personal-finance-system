package com.jirapat.personalfinance.api.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;
import org.mapstruct.ReportingPolicy;

import com.jirapat.personalfinance.api.dto.request.CreateBudgetRequest;
import com.jirapat.personalfinance.api.dto.request.UpdateBudgetRequest;
import com.jirapat.personalfinance.api.dto.response.BudgetResponse;
import com.jirapat.personalfinance.api.dto.response.CategoryResponse;
import com.jirapat.personalfinance.api.entity.Budget;
import com.jirapat.personalfinance.api.entity.Category;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface BudgetMapper {

    @Mapping(source = "category", target = "category", qualifiedByName = "toCategory")
    BudgetResponse toBudgetResponse(Budget budget);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "category", ignore = true)
    @Mapping(target = "isActive", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    Budget toEntity(CreateBudgetRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "category", ignore = true)
    @Mapping(target = "isActive", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    Budget updateEntity(UpdateBudgetRequest request, @MappingTarget Budget budget);

    @Named("toCategory")
    default CategoryResponse toCategory(Category category) {
        if (category == null) return null;
        return CategoryResponse.builder()
                .id(category.getId())
                .name(category.getName())
                .type(category.getType())
                .icon(category.getIcon())
                .color(category.getColor())
                .isDefault(category.getIsDefault())
                .isActive(category.getIsActive())
                .createdAt(category.getCreatedAt())
                .updatedAt(category.getUpdatedAt())
                .build();
    }
}
