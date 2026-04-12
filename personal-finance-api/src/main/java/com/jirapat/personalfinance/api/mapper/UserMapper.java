package com.jirapat.personalfinance.api.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.jirapat.personalfinance.api.dto.response.AuthResponse;
import com.jirapat.personalfinance.api.dto.response.UserResponse;
import com.jirapat.personalfinance.api.entity.User;

@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mapping(target = "fullName", expression = "java(user.getFullName())")
    @Mapping(target = "role", expression = "java(user.getRole().name())")
    UserResponse toUserResponse(User user);

    @Mapping(target = "id", expression = "java(String.valueOf(user.getId()))")
    @Mapping(target = "fullName", expression = "java(user.getFullName())")
    AuthResponse.UserInfo toUserInfo(User user);
}
