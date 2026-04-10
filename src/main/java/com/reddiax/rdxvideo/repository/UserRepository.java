package com.reddiax.rdxvideo.repository;

import com.reddiax.rdxvideo.model.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface UserRepository extends JpaRepository<UserEntity, Long> {

    Optional<UserEntity> findByExternalId(String externalId);

    Optional<UserEntity> findByEmail(String email);

    boolean existsByEmail(String email);

    boolean existsByExternalId(String externalId);

    List<UserEntity> findByOrganizationId(Long organizationId);

    @Query("SELECT u.profileImageId FROM UserEntity u WHERE u.profileImageId IS NOT NULL")
    Set<Long> findAllProfileImageIds();

    @Query("SELECT u FROM UserEntity u WHERE u.email IS NOT NULL AND u.status = 'ACTIVE'")
    List<UserEntity> findAllActiveUsersWithEmail();

    @Query("SELECT COUNT(u) FROM UserEntity u WHERE u.status = 'ACTIVE'")
    long countActiveUsers();
}
