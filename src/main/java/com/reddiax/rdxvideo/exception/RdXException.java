package com.reddiax.rdxvideo.exception;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.http.HttpStatus;

@Data
@EqualsAndHashCode(callSuper = true)
public class RdXException extends RuntimeException {

    private final HttpStatus statusCode;
    private final String errorCode;

    public RdXException(HttpStatus statusCode, String message, String errorCode) {
        super(message);
        this.statusCode = statusCode;
        this.errorCode = errorCode;
    }

}
