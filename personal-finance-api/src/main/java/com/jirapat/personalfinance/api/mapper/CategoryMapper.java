package com.jirapat.personalfinance.api.mapper;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;
import org.mapstruct.ReportingPolicy;

import com.jirapat.personalfinance.api.dto.request.CreateCategoryRequest;
import com.jirapat.personalfinance.api.dto.request.UpdateCategoryRequest;
import com.jirapat.personalfinance.api.dto.response.CategoryResponse;
import com.jirapat.personalfinance.api.entity.Category;
import com.jirapat.personalfinance.api.entity.CategoryType;
import com.jirapat.personalfinance.api.entity.User;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface CategoryMapper {

    @Mapping(source = "user", target = "user", qualifiedByName = "userToFullName")
    @Mapping(source = "parent.id", target = "parentId")
    @Mapping(source = "parent.name", target = "parentName")
    @Mapping(source = "children", target = "children", qualifiedByName = "toChildrenList")
    CategoryResponse toCategoryResponse(Category category);

    @Mapping(source = "user", target = "user", qualifiedByName = "userToFullName")
    @Mapping(source = "parent.id", target = "parentId")
    @Mapping(source = "parent.name", target = "parentName")
    @Mapping(target = "children", ignore = true)
    CategoryResponse toListResponse(Category category);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "parent", ignore = true)
    @Mapping(target = "children", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "isDefault", constant = "false")
    @Mapping(target = "isActive", constant = "true")
    @Mapping(source = "type", target = "type", qualifiedByName = "stringToType")
    Category toEntity(CreateCategoryRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "parent", ignore = true)
    @Mapping(target = "children", ignore = true)
    @Mapping(target = "type", ignore = true)
    @Mapping(target = "isDefault", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    void updateEntity(UpdateCategoryRequest request, @MappingTarget Category category);

    @Named("toChildrenList")
    default List<CategoryResponse> toChildrenList(List<Category> children) {
        if (children == null) return null;
        return children.stream().map(this::toChildResponse).toList();
    }

    default CategoryResponse toChildResponse(Category category) {
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

    @Named("userToFullName")
    default String userToFullName(User user) {
        if (user == null) return null;
        return user.getFirstName() + " " + user.getLastName();
    }

    @Named("stringToType")
    default CategoryType stringToType(String type) {
        if (type == null) return null;
        return CategoryType.valueOf(type.toUpperCase());
    }
}
