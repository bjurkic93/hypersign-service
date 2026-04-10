package com.reddiax.rdxvideo.model.dto;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for updating the current user's profile.
 * Only allows updating non-sensitive fields.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserProfileUpdateDTO {

    @Size(max = 100, message = "Display name must not exceed 100 characters")
    private String displayName;

    @Size(max = 500, message = "Bio must not exceed 500 characters")
    private String bio;

    private Long profileImageId;
    
    private Long organizationId;
}
