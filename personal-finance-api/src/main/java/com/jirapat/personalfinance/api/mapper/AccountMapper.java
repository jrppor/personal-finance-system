package com.jirapat.personalfinance.api.mapper;

import com.jirapat.personalfinance.api.dto.request.UpdateAccountRequest;
import org.mapstruct.*;

import com.jirapat.personalfinance.api.dto.request.CreateAccountRequest;
import com.jirapat.personalfinance.api.dto.response.AccountResponse;
import com.jirapat.personalfinance.api.entity.Account;
import com.jirapat.personalfinance.api.entity.User;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface AccountMapper {

    @Mapping(source = "user", target = "user", qualifiedByName = "userToFullName")
    AccountResponse toAccountResponse(Account account);

    @Mapping(source = "user", target = "user", qualifiedByName = "userToFullName")
    AccountResponse toListResponse(Account account);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "isActive", constant = "true")
    Account toEntity(CreateAccountRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Account updateEntity(UpdateAccountRequest request, @MappingTarget Account account);

    @Named("userToFullName")
    default String userToFullName(User user) {
        if (user == null) return null;
        return user.getFirstName() + " " + user.getLastName();
    }
}
