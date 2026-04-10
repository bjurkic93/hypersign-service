package com.reddiax.rdxvideo.model.mapper;

import com.reddiax.rdxvideo.model.dto.AdvertisementDTO;
import com.reddiax.rdxvideo.model.dto.AdvertisementRequestDTO;
import com.reddiax.rdxvideo.model.entity.AdvertisementEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Mappings;
import org.mapstruct.factory.Mappers;

@Mapper
public interface AdvertisementMapper {

    AdvertisementMapper INSTANCE = Mappers.getMapper(AdvertisementMapper.class);

    @Mappings({
            @Mapping(target = "id", ignore = true),
            @Mapping(target = "createdAt", ignore = true),
            @Mapping(target = "modifiedAt", ignore = true),
            @Mapping(target = "createdBy", ignore = true),
            @Mapping(target = "modifiedBy", ignore = true)
    })
    AdvertisementEntity toCreateEntity(AdvertisementRequestDTO advertisementDTO);

    @Mappings({
            @Mapping(target = "id", source = "id"),
            @Mapping(target = "createdAt", ignore = true),
            @Mapping(target = "modifiedAt", ignore = true),
            @Mapping(target = "createdBy", ignore = true),
            @Mapping(target = "modifiedBy", ignore = true)
    })
    AdvertisementEntity toUpdateEntity(Long id, AdvertisementRequestDTO advertisementDTO);

    @Mappings({
            @Mapping(target = "organizationName", ignore = true),
            @Mapping(target = "videoUrl", ignore = true),
            @Mapping(target = "images", ignore = true),
            @Mapping(target = "pricingTier", ignore = true)
    })
    AdvertisementDTO toDto(AdvertisementEntity advertisementEntity);

    @Mappings({
            @Mapping(target = "id", ignore = true),
            @Mapping(target = "createdAt", ignore = true),
            @Mapping(target = "modifiedAt", ignore = true),
            @Mapping(target = "createdBy", ignore = true),
            @Mapping(target = "modifiedBy", ignore = true)
    })
    void updateEntity(AdvertisementRequestDTO requestDTO, @MappingTarget AdvertisementEntity entity);
}
