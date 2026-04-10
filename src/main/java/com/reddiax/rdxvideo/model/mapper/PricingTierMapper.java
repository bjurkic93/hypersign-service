package com.reddiax.rdxvideo.model.mapper;

import com.reddiax.rdxvideo.model.dto.PricingTierDTO;
import com.reddiax.rdxvideo.model.dto.PricingTierRequestDTO;
import com.reddiax.rdxvideo.model.entity.PricingTierEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Mappings;
import org.mapstruct.factory.Mappers;

@Mapper
public interface PricingTierMapper {

    PricingTierMapper INSTANCE = Mappers.getMapper(PricingTierMapper.class);

    @Mappings({
            @Mapping(target = "id", ignore = true),
            @Mapping(target = "createdAt", ignore = true),
            @Mapping(target = "modifiedAt", ignore = true),
            @Mapping(target = "createdBy", ignore = true),
            @Mapping(target = "modifiedBy", ignore = true)
    })
    PricingTierEntity toCreateEntity(PricingTierRequestDTO requestDTO);

    @Mappings({
            @Mapping(target = "id", source = "id"),
            @Mapping(target = "createdAt", ignore = true),
            @Mapping(target = "modifiedAt", ignore = true),
            @Mapping(target = "createdBy", ignore = true),
            @Mapping(target = "modifiedBy", ignore = true)
    })
    PricingTierEntity toUpdateEntity(Long id, PricingTierRequestDTO requestDTO);

    PricingTierDTO toDto(PricingTierEntity entity);

    @Mappings({
            @Mapping(target = "id", ignore = true),
            @Mapping(target = "createdAt", ignore = true),
            @Mapping(target = "modifiedAt", ignore = true),
            @Mapping(target = "createdBy", ignore = true),
            @Mapping(target = "modifiedBy", ignore = true)
    })
    void updateEntity(PricingTierRequestDTO requestDTO, @MappingTarget PricingTierEntity entity);
}
