package com.alexdevvv.notifications.infrastructure.in.web;

import com.alexdevvv.notifications.domain.exceptions.IdempotencyKeyReusedException;
import com.alexdevvv.notifications.domain.exceptions.MissingContactInfoException;
import com.alexdevvv.notifications.domain.exceptions.NotificationNotFoundException;
import com.alexdevvv.notifications.domain.exceptions.UnsupportedChannelException;
import com.alexdevvv.notifications.domain.exceptions.UserNotFoundException;
import com.alexdevvv.notifications.infrastructure.in.web.interceptor.InvalidApiKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

/**
 * Traduce las excepciones a respuestas RFC 9457 (application/problem+json).
 * Extender ResponseEntityExceptionHandler cubre además los errores propios de Spring MVC,
 * como la validación del body (400).
 * Las fallas del proveedor no llegan acá: la entrega es asíncrona y se reflejan en el estado de la notificación.
 */
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(InvalidApiKeyException.class)
    ProblemDetail handleInvalidApiKey(InvalidApiKeyException ex) {
        return problem(HttpStatus.UNAUTHORIZED, "Unauthorized", ex.getMessage());
    }

    @ExceptionHandler(UserNotFoundException.class)
    ProblemDetail handleUserNotFound(UserNotFoundException ex) {
        return problem(HttpStatus.NOT_FOUND, "User not found", ex.getMessage());
    }

    @ExceptionHandler(NotificationNotFoundException.class)
    ProblemDetail handleNotificationNotFound(NotificationNotFoundException ex) {
        return problem(HttpStatus.NOT_FOUND, "Notification not found", ex.getMessage());
    }

    @ExceptionHandler({MissingContactInfoException.class, UnsupportedChannelException.class})
    ProblemDetail handleUndeliverable(RuntimeException ex) {
        return problem(HttpStatus.UNPROCESSABLE_CONTENT, "Notification cannot be delivered", ex.getMessage());
    }

    @ExceptionHandler(IdempotencyKeyReusedException.class)
    ProblemDetail handleIdempotencyKeyReused(IdempotencyKeyReusedException ex) {
        return problem(HttpStatus.UNPROCESSABLE_CONTENT, "Idempotency-Key reused", ex.getMessage());
    }

    private static ProblemDetail problem(HttpStatus status, String title, String detail) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(status, detail);
        problem.setTitle(title);
        return problem;
    }
}
