package org.ashkelyonok.authservice.mapper;

import org.ashkelyonok.authservice.model.dto.request.RegisterRequestDto;
import org.ashkelyonok.authservice.model.entity.UserCredential;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;

@Mapper(componentModel = "spring", implementationName = "userCredentialMapperImpl")
public abstract class UserCredentialMapper {

    @Autowired
    protected PasswordEncoder passwordEncoder;

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "userId", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "enabled", constant = "true")
    @Mapping(target = "accountNonLocked", constant = "true")
    @Mapping(target = "password", expression = "java(passwordEncoder.encode(dto.getPassword()))")
    public abstract UserCredential toEntity(RegisterRequestDto dto);
}
