package com.reddiax.rdxvideo.model.mapper;

import com.reddiax.rdxvideo.model.dto.UserDTO;
import com.reddiax.rdxvideo.model.dto.UserRequestDTO;
import com.reddiax.rdxvideo.model.entity.UserEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Mappings;
import org.mapstruct.factory.Mappers;

@Mapper
public interface UserMapper {

    UserMapper INSTANCE = Mappers.getMapper(UserMapper.class);

    @Mappings({
            @Mapping(target = "id", ignore = true),
            @Mapping(target = "organization", ignore = true),
            @Mapping(target = "lastLoginAt", ignore = true),
            @Mapping(target = "createdAt", ignore = true),
            @Mapping(target = "modifiedAt", ignore = true),
            @Mapping(target = "createdBy", ignore = true),
            @Mapping(target = "modifiedBy", ignore = true)
    })
    UserEntity toCreateEntity(UserRequestDTO requestDTO);

    @Mappings({
            @Mapping(target = "id", source = "id"),
            @Mapping(target = "organization", ignore = true),
            @Mapping(target = "lastLoginAt", ignore = true),
            @Mapping(target = "createdAt", ignore = true),
            @Mapping(target = "modifiedAt", ignore = true),
            @Mapping(target = "createdBy", ignore = true),
            @Mapping(target = "modifiedBy", ignore = true)
    })
    UserEntity toUpdateEntity(Long id, UserRequestDTO requestDTO);

    @Mappings({
            @Mapping(target = "organizationId", source = "organization.id"),
            @Mapping(target = "organizationName", source = "organization.name"),
            @Mapping(target = "organizationLogoUrl", ignore = true),
            @Mapping(target = "givenName", ignore = true),
            @Mapping(target = "familyName", ignore = true)
    })
    UserDTO toDto(UserEntity entity);

    @Mappings({
            @Mapping(target = "id", ignore = true),
            @Mapping(target = "organization", ignore = true),
            @Mapping(target = "lastLoginAt", ignore = true),
            @Mapping(target = "createdAt", ignore = true),
            @Mapping(target = "modifiedAt", ignore = true),
            @Mapping(target = "createdBy", ignore = true),
            @Mapping(target = "modifiedBy", ignore = true)
    })
    void updateEntity(UserRequestDTO requestDTO, @MappingTarget UserEntity entity);
}
