package com.jirapat.personalfinance.api.mapper;

import com.jirapat.personalfinance.api.dto.request.CreateRecurringTransactionRequest;
import com.jirapat.personalfinance.api.dto.request.UpdateRecurringTransactionRequest;
import com.jirapat.personalfinance.api.dto.response.AccountResponse;
import com.jirapat.personalfinance.api.dto.response.CategoryResponse;
import com.jirapat.personalfinance.api.dto.response.RecurringTransactionResponse;
import com.jirapat.personalfinance.api.entity.Account;
import com.jirapat.personalfinance.api.entity.Category;
import com.jirapat.personalfinance.api.entity.RecurringTransaction;
import org.mapstruct.*;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface RecurringTransactionMapper {

    @Mapping(source = "account", target = "account", qualifiedByName = "toAccountResponse")
    @Mapping(source = "category", target = "category", qualifiedByName = "toCategory")
    RecurringTransactionResponse toRecurringTransactionResponse(RecurringTransaction recurringTransaction);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "account", ignore = true)
    @Mapping(target = "category", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    RecurringTransaction toEntity(CreateRecurringTransactionRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "account", ignore = true)
    @Mapping(target = "category", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    RecurringTransaction updateEntity(UpdateRecurringTransactionRequest request, @MappingTarget RecurringTransaction recurringTransaction);

    @Named("toAccountResponse")
    default AccountResponse toAccountResponse(Account account) {
        if (account == null) return null;
        return AccountResponse.builder()
                .id(account.getId())
                .name(account.getName())
                .type(account.getType())
                .balance(account.getBalance())
                .isActive(account.getIsActive())
                .createdAt(account.getCreatedAt())
                .updatedAt(account.getUpdatedAt())
                .build();
    }

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
