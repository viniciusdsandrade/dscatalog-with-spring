package com.restful.dscatalog.handler;

import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.validation.FieldError;

import java.time.LocalDateTime;

import static java.time.LocalDateTime.now;

@Schema(description = "Detalhes do erro")
public record ErrorDetails(
        LocalDateTime timestamp,
        String field,
        String details,
        String error
) {
    public ErrorDetails(FieldError erro) {
        this(
                now(),
                erro.getField(),
                erro.getDefaultMessage(),
                erro.getCode()
        );
    }
}