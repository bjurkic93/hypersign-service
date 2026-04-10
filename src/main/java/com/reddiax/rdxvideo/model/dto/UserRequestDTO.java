package com.reddiax.rdxvideo.model.dto;

import com.reddiax.rdxvideo.constant.UserRoleEnum;
import com.reddiax.rdxvideo.constant.UserStatusEnum;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserRequestDTO {
    @NotBlank(message = "External ID is required")
    private String externalId;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;

    @Size(max = 100, message = "Display name must not exceed 100 characters")
    private String displayName;

    private String profilePictureUrl;

    @NotNull(message = "Role is required")
    private UserRoleEnum role;

    @NotNull(message = "Status is required")
    private UserStatusEnum status;

    private Long organizationId;

    @Size(max = 500, message = "Bio must not exceed 500 characters")
    private String bio;
}
