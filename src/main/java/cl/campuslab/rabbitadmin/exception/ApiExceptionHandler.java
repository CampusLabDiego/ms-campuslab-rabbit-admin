package cl.campuslab.rabbitadmin.exception;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;

import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidation(
            MethodArgumentNotValidException exception
    ) {
        Map<String, String> errors = new LinkedHashMap<>();

        exception.getBindingResult()
                .getFieldErrors()
                .forEach(error -> {
                    String message = error.getDefaultMessage();

                    errors.putIfAbsent(
                            error.getField(),
                            message != null
                                    ? message
                                    : "Valor inválido."
                    );
                });

        return ResponseEntity.badRequest().body(
                new ApiError(
                        400,
                        "Los datos enviados no son válidos.",
                        errors
                )
        );
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiError> handleInvalidJson(
            HttpMessageNotReadableException exception
    ) {
        return ResponseEntity.badRequest().body(
                new ApiError(
                        400,
                        "El JSON no es válido. Revisa los tipos de datos. "
                                + "El tipo de exchange debe ser DIRECT o TOPIC.",
                        Map.of()
                )
        );
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ApiError> handleServiceError(
            ResponseStatusException exception
    ) {
        String detail = exception.getReason();

        return ResponseEntity
                .status(exception.getStatusCode())
                .body(
                        new ApiError(
                                exception.getStatusCode().value(),
                                detail != null
                                        ? detail
                                        : "No se pudo completar la operación.",
                                Map.of()
                        )
                );
    }

    public record ApiError(
            int status,
            String detail,
            Map<String, String> errors
    ) {
    }
}