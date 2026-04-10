package com.reddiax.rdxvideo.repository;

import com.reddiax.rdxvideo.model.entity.AdvertisementImageEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AdvertisementImageRepository extends JpaRepository<AdvertisementImageEntity, Long> {

    List<AdvertisementImageEntity> findByAdvertisementIdOrderByDisplayOrderAsc(Long advertisementId);

    void deleteByAdvertisementId(Long advertisementId);
}
