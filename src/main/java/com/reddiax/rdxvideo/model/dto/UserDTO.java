package com.reddiax.rdxvideo.model.dto;

import com.reddiax.rdxvideo.constant.UserRoleEnum;
import com.reddiax.rdxvideo.constant.UserStatusEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserDTO {
    private Long id;
    private String externalId;
    private String email;
    private String displayName;
    private String givenName;
    private String familyName;
    private Long profileImageId;
    private String profilePictureUrl;
    private UserRoleEnum role;
    private UserStatusEnum status;
    private Long organizationId;
    private String organizationName;
    private String organizationLogoUrl;
    private LocalDateTime lastLoginAt;
    private String bio;
    private LocalDateTime createdAt;
    private LocalDateTime modifiedAt;
}
