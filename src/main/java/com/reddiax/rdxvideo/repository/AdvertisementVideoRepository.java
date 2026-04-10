package com.reddiax.rdxvideo.repository;

import com.reddiax.rdxvideo.model.entity.AdvertisementVideoEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AdvertisementVideoRepository extends JpaRepository<AdvertisementVideoEntity, Long> {

    List<AdvertisementVideoEntity> findByAdvertisementIdOrderByDisplayOrderAsc(Long advertisementId);

    void deleteByAdvertisementId(Long advertisementId);

    int countByAdvertisementId(Long advertisementId);
}
