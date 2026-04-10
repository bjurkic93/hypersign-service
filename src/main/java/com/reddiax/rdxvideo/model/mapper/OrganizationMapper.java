package com.reddiax.rdxvideo.model.mapper;

import com.reddiax.rdxvideo.model.dto.OrganizationDTO;
import com.reddiax.rdxvideo.model.dto.OrganizationRequestDTO;
import com.reddiax.rdxvideo.model.entity.OrganizationEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Mappings;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.factory.Mappers;

@Mapper(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface OrganizationMapper {

    OrganizationMapper INSTANCE = Mappers.getMapper(OrganizationMapper.class);

    @Mappings({
            @Mapping(target = "id", ignore = true),
            @Mapping(target = "logoUrl", ignore = true),
            @Mapping(target = "createdAt", ignore = true),
            @Mapping(target = "modifiedAt", ignore = true),
            @Mapping(target = "createdBy", ignore = true),
            @Mapping(target = "modifiedBy", ignore = true)
    })
    OrganizationEntity toCreateEntity(OrganizationRequestDTO requestDTO);

    @Mappings({
            @Mapping(target = "id", source = "id"),
            @Mapping(target = "logoUrl", ignore = true),
            @Mapping(target = "createdAt", ignore = true),
            @Mapping(target = "modifiedAt", ignore = true),
            @Mapping(target = "createdBy", ignore = true),
            @Mapping(target = "modifiedBy", ignore = true)
    })
    OrganizationEntity toUpdateEntity(Long id, OrganizationRequestDTO requestDTO);

    OrganizationDTO toDto(OrganizationEntity entity);

    @Mappings({
            @Mapping(target = "id", ignore = true),
            @Mapping(target = "logoUrl", ignore = true),
            @Mapping(target = "logoImageId", ignore = true),
            @Mapping(target = "createdAt", ignore = true),
            @Mapping(target = "modifiedAt", ignore = true),
            @Mapping(target = "createdBy", ignore = true),
            @Mapping(target = "modifiedBy", ignore = true)
    })
    void updateEntity(OrganizationRequestDTO requestDTO, @MappingTarget OrganizationEntity entity);
}
