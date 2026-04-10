package com.reddiax.rdxvideo.constant;

public enum UserRoleEnum {
    SYSTEM_ADMIN,    // Global admin - announcements, TV device launch, etc.
    ADMIN,           // Organization-level full access
    MODERATOR,       // Content moderation access
    USER,            // Standard CMS user (belongs to organization)
    VIEWER,          // Read-only access
    MOBILE_PUBLISHER, // Mobile app user - earns rewards, no CMS access
    TV_PUBLISHER      // TV app user - for future use
}
