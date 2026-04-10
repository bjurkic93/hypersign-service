package com.reddiax.rdxvideo.exception.handler;

import com.reddiax.rdxvideo.exception.RdXException;
import com.reddiax.rdxvideo.model.dto.ErrorResponseDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(RdXException.class)
    public ResponseEntity<ErrorResponseDTO> handleRdXException(RdXException ex) {
        log.error("Handled RdXException: status={}, errorCode={}, message={}",
                ex.getStatusCode(), ex.getErrorCode(), ex.getMessage(), ex);

        return new ResponseEntity<>(
                ErrorResponseDTO.builder()
                        .errorCode(ex.getErrorCode())
                        .message(ex.getMessage())
                        .build(),
                ex.getStatusCode()
        );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponseDTO> handleException(Exception ex) {
        // log with stacktrace
        log.error("Unhandled exception caught by GlobalExceptionHandler", ex);

        return ResponseEntity.internalServerError().body(
                ErrorResponseDTO.builder()
                        .errorCode("INTERNAL_SERVER_ERROR")
                        .message(ex.getMessage())
                        .build()
        );
    }
}
