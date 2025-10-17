package com.restful.dscatalog.handler;

import com.restful.dscatalog.exception.DuplicateEntryException;
import com.restful.dscatalog.exception.ValidationException;
import com.restful.dscatalog.exception.ResourceNotFoundException;
import org.hibernate.TypeMismatchException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.List;

import static java.time.LocalDateTime.now;
import static java.util.stream.Collectors.toList;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.CONFLICT;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.web.util.HtmlUtils.htmlEscape;

@RestControllerAdvice(basePackages = "com.restful.dscatalog.controller")
public class GlobalExceptionHandler {

    @ExceptionHandler({EntityNotFoundException.class, ResourceNotFoundException.class})
    public ResponseEntity<List<ErrorDetails>> handleResourceNotFoundException(
            Exception exception,
            WebRequest webRequest
    ) {
        ErrorDetails errorDetails = new ErrorDetails(
                now(),
                htmlEscape(exception.getMessage()),
                htmlEscape(webRequest.getDescription(false)),
                "RESOURCE_NOT_FOUND"
        );

        return new ResponseEntity<>(List.of(errorDetails), NOT_FOUND);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<List<ErrorDetails>> handleDataIntegrityViolationException(MethodArgumentNotValidException exception) {
        List<FieldError> errors = exception.getFieldErrors();

        List<ErrorDetails> errorDetailsList = errors
                .stream()
                .map(ErrorDetails::new)
                .collect(toList());

        return new ResponseEntity<>(errorDetailsList, BAD_REQUEST);
    }

    @ExceptionHandler({MethodArgumentTypeMismatchException.class, TypeMismatchException.class, NumberFormatException.class})
    public ResponseEntity<List<ErrorDetails>> handleTypeMismatch(WebRequest webRequest) {
        ErrorDetails errorDetails = new ErrorDetails(
                now(),
                htmlEscape("Parâmetro inválido: tipo incorreto ou não numérico."),
                htmlEscape(webRequest.getDescription(false)),
                "TYPE_MISMATCH"
        );
        return new ResponseEntity<>(List.of(errorDetails), BAD_REQUEST);
    }

    @ExceptionHandler(DuplicateEntryException.class)
    public ResponseEntity<List<ErrorDetails>> handleDuplicateEntryException(
            DuplicateEntryException ex,
            WebRequest webRequest
    ) {
        ErrorDetails errorDetails = new ErrorDetails(
                now(),
                htmlEscape(ex.getMessage()),
                htmlEscape(webRequest.getDescription(false)),
                "DUPLICATE_ENTRY"
        );
        return ResponseEntity.status(CONFLICT).body(List.of(errorDetails));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorDetails> handleGlobalException(
            Exception exception,
            WebRequest webRequest
    ) {
        ErrorDetails errorDetails = new ErrorDetails(
                now(),
                htmlEscape(exception.getMessage()),
                htmlEscape(webRequest.getDescription(false)),
                "INTERNAL_SERVER_ERROR"
        );
        return new ResponseEntity<>(errorDetails, INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<List<ErrorDetails>> handleValidacaoException(
            ValidationException exception,
            WebRequest webRequest
    ) {
        ErrorDetails errorDetails = new ErrorDetails(
                now(),
                htmlEscape(exception.getMessage()),
                htmlEscape(webRequest.getDescription(false)),
                "VALIDATION_ERROR"
        );

        return new ResponseEntity<>(List.of(errorDetails), BAD_REQUEST);
    }
}
