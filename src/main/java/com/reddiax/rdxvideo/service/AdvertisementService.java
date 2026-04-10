package com.reddiax.rdxvideo.service;

import com.reddiax.rdxvideo.constant.AdvertisementPlatformEnum;
import com.reddiax.rdxvideo.model.dto.AdvertisementDTO;
import com.reddiax.rdxvideo.model.dto.AdvertisementRequestDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface AdvertisementService {

    Page<AdvertisementDTO> getAdvertisements(Pageable pageable);

    Page<AdvertisementDTO> getAdvertisementsByOrganization(Long organizationId, Pageable pageable);

    Page<AdvertisementDTO> getAdvertisementsByPlatform(AdvertisementPlatformEnum platform, Pageable pageable);

    Page<AdvertisementDTO> getValidAdvertisements(AdvertisementPlatformEnum platform, Long organizationId, Pageable pageable);

    AdvertisementDTO getAdvertisement(Long id);

    AdvertisementDTO createAdvertisement(AdvertisementRequestDTO request);

    AdvertisementDTO updateAdvertisement(Long id, AdvertisementRequestDTO request);

    void deleteAdvertisement(Long id);
}
