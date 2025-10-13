package com.restful.dscatalog.handler;

import com.restful.dscatalog.exception.DuplicateEntryException;
import com.restful.dscatalog.exception.ValidationException;
import com.restful.dscatalog.exception.ResourceNotFoundException;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

import java.util.List;

import static java.time.LocalDateTime.now;
import static java.util.stream.Collectors.toList;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.CONFLICT;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@RestControllerAdvice(basePackages = "com.restful.dscatalog.controller")
public class GlobalExceptionHandler {

    /**
     * Manipula a exceção de entidade não encontrada.
     *
     * @param exception  A exceção de entidade não encontrada.
     * @param webRequest O objeto WebRequest que fornece informações sobre a solicitação.
     * @return Uma ResponseEntity contendo detalhes do erro e status HTTP 404 (Not Found).
     */
    @ExceptionHandler({EntityNotFoundException.class, ResourceNotFoundException.class})
    public ResponseEntity<List<ErrorDetails>> handleResourceNotFoundException(Exception exception,
                                                                              WebRequest webRequest) {

        ErrorDetails errorDetails = new ErrorDetails(
                now(),
                exception.getMessage(),
                webRequest.getDescription(false),
                "RESOURCE_NOT_FOUND"
        );

        return new ResponseEntity<>(List.of(errorDetails), NOT_FOUND);
    }

    /**
     * Manipula a exceção de violação de integridade dos dados.
     *
     * @param exception  A exceção de violação de integridade dos dados.
     * @param webRequest O objeto WebRequest que fornece informações sobre a solicitação.
     * @return Uma ResponseEntity contendo detalhes do erro e status HTTP 400 (Bad Request).
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<List<ErrorDetails>> handleDataIntegrityViolationException(MethodArgumentNotValidException exception,
                                                                                    WebRequest webRequest) {
        var errors = exception.getFieldErrors();

        List<ErrorDetails> errorDetailsList = errors.stream()
                .map(ErrorDetails::new)
                .collect(toList());

        return new ResponseEntity<>(errorDetailsList, BAD_REQUEST);
    }

    /**
     * Manipula a exceção de entrada duplicada.
     *
     * @param exception  A exceção de entrada duplicada.
     * @param webRequest O objeto WebRequest que fornece informações sobre a solicitação.
     * @return Uma ResponseEntity contendo detalhes do erro e status HTTP 409 (Conflict).
     */
    @ExceptionHandler(DuplicateEntryException.class)
    public ResponseEntity<List<ErrorDetails>> handleDuplicateEntryException(DuplicateEntryException exception,
                                                                            WebRequest webRequest) {
        ErrorDetails errorDetails = new ErrorDetails(
                now(),
                exception.getMessage(),
                webRequest.getDescription(false),
                "DUPLICATE_ENTRY"
        );

        return ResponseEntity.status(CONFLICT).body(List.of(errorDetails));
    }

    /**
     * Manipula exceções globais não especificadas.
     *
     * @param exception  A exceção global.
     * @param webRequest O objeto WebRequest que fornece informações sobre a solicitação.
     * @return Uma ResponseEntity contendo detalhes do erro e status HTTP 500 (Internal Server Error).
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorDetails> handleGlobalException(Exception exception,
                                                              WebRequest webRequest) {

        ErrorDetails errorDetails = new ErrorDetails(
                now(),
                exception.getMessage(),
                webRequest.getDescription(false),
                "INTERNAL_SERVER_ERROR"
        );
        return new ResponseEntity<>(errorDetails, INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<List<ErrorDetails>> handleValidacaoException(ValidationException exception,
                                                                       WebRequest webRequest) {

        ErrorDetails errorDetails = new ErrorDetails(
                now(),
                exception.getMessage(),
                webRequest.getDescription(false),
                "VALIDATION_ERROR"
        );

        return new ResponseEntity<>(List.of(errorDetails), BAD_REQUEST);
    }
}
