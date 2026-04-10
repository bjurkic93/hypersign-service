package com.reddiax.rdxvideo.model.mapper;

import com.reddiax.rdxvideo.model.dto.AdvertisementImageDTO;
import com.reddiax.rdxvideo.model.dto.AdvertisementImageRequestDTO;
import com.reddiax.rdxvideo.model.entity.AdvertisementImageEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.factory.Mappers;

@Mapper
public interface AdvertisementImageMapper {

    AdvertisementImageMapper INSTANCE = Mappers.getMapper(AdvertisementImageMapper.class);

    @Mappings({
            @Mapping(target = "id", ignore = true),
            @Mapping(target = "advertisementId", ignore = true),
            @Mapping(target = "imageHash", ignore = true),
            @Mapping(target = "createdAt", ignore = true),
            @Mapping(target = "modifiedAt", ignore = true),
            @Mapping(target = "createdBy", ignore = true),
            @Mapping(target = "modifiedBy", ignore = true)
    })
    AdvertisementImageEntity toEntity(AdvertisementImageRequestDTO requestDTO);

    @Mappings({
            @Mapping(target = "imageUrl", ignore = true)
    })
    AdvertisementImageDTO toDto(AdvertisementImageEntity entity);
}
