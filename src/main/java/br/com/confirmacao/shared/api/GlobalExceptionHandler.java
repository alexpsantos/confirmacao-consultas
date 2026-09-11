package br.com.confirmacao.shared.api;

import br.com.confirmacao.auth.application.InvalidCredentialsException;
import br.com.confirmacao.auth.application.InvalidPasswordResetTokenException;
import br.com.confirmacao.patient.application.PatientAlreadyExistsException;
import br.com.confirmacao.patient.application.PatientNotFoundException;
import br.com.confirmacao.professional.application.ProfessionalAlreadyExistsException;
import br.com.confirmacao.professional.application.ProfessionalNotFoundException;
import br.com.confirmacao.tenant.application.TenantNotFoundException;
import br.com.confirmacao.tenant.domain.InvalidTimezoneException;
import br.com.confirmacao.user.application.UserAlreadyExistsException;
import br.com.confirmacao.user.application.UserNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import br.com.confirmacao.tenant.application.TenantInactiveException;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<ApiErrorResponse> handleUnauthorized(
            InvalidCredentialsException exception,
            HttpServletRequest request
    ) {
        HttpStatus status = HttpStatus.UNAUTHORIZED;

        ApiErrorResponse response = new ApiErrorResponse(
                Instant.now(),
                status.value(),
                status.getReasonPhrase(),
                exception.getMessage(),
                request.getRequestURI(),
                Map.of()
        );

        return ResponseEntity.status(status).body(response);
    }

    @ExceptionHandler({
            TenantNotFoundException.class,
            ProfessionalNotFoundException.class,
            PatientNotFoundException.class
            , UserNotFoundException.class
    })
    public ResponseEntity<ApiErrorResponse> handleNotFound(
            RuntimeException exception,
            HttpServletRequest request
    ) {
        HttpStatus status = HttpStatus.NOT_FOUND;

        ApiErrorResponse response = new ApiErrorResponse(
                Instant.now(),
                status.value(),
                status.getReasonPhrase(),
                exception.getMessage(),
                request.getRequestURI(),
                Map.of()
        );

        return ResponseEntity
                .status(status)
                .body(response);
    }

    @ExceptionHandler({
            ProfessionalAlreadyExistsException.class,
            PatientAlreadyExistsException.class,
            UserAlreadyExistsException.class,
            TenantInactiveException.class
    })
    public ResponseEntity<ApiErrorResponse> handleConflict(
            RuntimeException exception,
            HttpServletRequest request
    ) {
        HttpStatus status = HttpStatus.CONFLICT;

        ApiErrorResponse response = new ApiErrorResponse(
                Instant.now(),
                status.value(),
                status.getReasonPhrase(),
                exception.getMessage(),
                request.getRequestURI(),
                Map.of()
        );

        return ResponseEntity
                .status(status)
                .body(response);
    }
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleValidation(
            MethodArgumentNotValidException exception,
            HttpServletRequest request
    ) {
        HttpStatus status = HttpStatus.BAD_REQUEST;

        Map<String, String> fieldErrors = new LinkedHashMap<>();

        exception.getBindingResult()
                .getFieldErrors()
                .forEach(fieldError ->
                        fieldErrors.putIfAbsent(
                                fieldError.getField(),
                                fieldError.getDefaultMessage()
                        )
                );

        ApiErrorResponse response = new ApiErrorResponse(
                Instant.now(),
                status.value(),
                status.getReasonPhrase(),
                "Existem campos inválidos",
                request.getRequestURI(),
                fieldErrors
        );

        return ResponseEntity
                .status(status)
                .body(response);
    }

    @ExceptionHandler(InvalidTimezoneException.class)
    public ResponseEntity<ApiErrorResponse> handleInvalidTimezone(
            InvalidTimezoneException exception,
            HttpServletRequest request
    ) {
        HttpStatus status = HttpStatus.BAD_REQUEST;

        ApiErrorResponse response = new ApiErrorResponse(
                Instant.now(),
                status.value(),
                status.getReasonPhrase(),
                "Existem campos inválidos",
                request.getRequestURI(),
                Map.of("timezone", exception.getMessage())
        );

        return ResponseEntity.status(status).body(response);
    }

    @ExceptionHandler(InvalidPasswordResetTokenException.class)
    public ResponseEntity<ApiErrorResponse> handleInvalidPasswordResetToken(
            InvalidPasswordResetTokenException exception,
            HttpServletRequest request) {
        HttpStatus status = HttpStatus.BAD_REQUEST;
        ApiErrorResponse response = new ApiErrorResponse(
                Instant.now(), status.value(), status.getReasonPhrase(),
                exception.getMessage(), request.getRequestURI(), Map.of());
        return ResponseEntity.status(status).body(response);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiErrorResponse> handleIllegalArgument(
            IllegalArgumentException exception, HttpServletRequest request) {
        HttpStatus status = HttpStatus.BAD_REQUEST;
        ApiErrorResponse response = new ApiErrorResponse(
                Instant.now(), status.value(), status.getReasonPhrase(), exception.getMessage(),
                request.getRequestURI(), Map.of());
        return ResponseEntity.status(status).body(response);
    }
}
