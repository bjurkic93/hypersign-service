package com.reddiax.rdxvideo.constant;

public enum UserAdvertisementStatusEnum {
    ACTIVE,     // User is currently showing this ad
    PAUSED,     // User temporarily paused the ad
    COMPLETED,  // User successfully completed the ad duration
    VIOLATED,   // User changed wallpaper before completion
    EXPIRED,    // Ad duration expired
    CANCELLED   // User or admin cancelled
}
