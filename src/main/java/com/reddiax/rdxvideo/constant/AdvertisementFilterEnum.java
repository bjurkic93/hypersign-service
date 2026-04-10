package com.reddiax.rdxvideo.constant;

import lombok.Getter;

@Getter
public enum AdvertisementFilterEnum {
    USERNAME("username"),
    EMAIL("email"),
    CREATED_BY_FROM("created_by_from"),
    CREATED_BY_TO("created_by_to");

    private final String value;

    AdvertisementFilterEnum(String value) {
        this.value = value;
    }
}
