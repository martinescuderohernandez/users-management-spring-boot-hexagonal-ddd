package com.jcaa.usersmanagement.infrastructure.entrypoint.rest.advice;

import com.jcaa.usersmanagement.domain.exception.AccountConflictException;
import com.jcaa.usersmanagement.domain.exception.AccountServiceException;
import com.jcaa.usersmanagement.domain.exception.AccountTimeoutException;
import com.jcaa.usersmanagement.domain.exception.AccountValidationException;
import com.jcaa.usersmanagement.domain.exception.DomainException;
import com.jcaa.usersmanagement.domain.exception.InvalidCredentialsException;
import com.jcaa.usersmanagement.domain.exception.UserAlreadyExistsException;
import com.jcaa.usersmanagement.domain.exception.UserNotFoundException;
import com.jcaa.usersmanagement.infrastructure.adapter.persistence.exception.PersistenceException;
import com.jcaa.usersmanagement.infrastructure.entrypoint.rest.dto.response.ApiErrorResponse;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(UserNotFoundException.class)
  @ResponseStatus(HttpStatus.NOT_FOUND)
  public ApiErrorResponse handleUserNotFound(final UserNotFoundException exception) {
    return new ApiErrorResponse(HttpStatus.NOT_FOUND.value(), exception.getMessage());
  }

  @ExceptionHandler(UserAlreadyExistsException.class)
  @ResponseStatus(HttpStatus.CONFLICT)
  public ApiErrorResponse handleUserAlreadyExists(final UserAlreadyExistsException exception) {
    return new ApiErrorResponse(HttpStatus.CONFLICT.value(), exception.getMessage());
  }

  @ExceptionHandler(InvalidCredentialsException.class)
  @ResponseStatus(HttpStatus.UNAUTHORIZED)
  public ApiErrorResponse handleInvalidCredentials(final InvalidCredentialsException exception) {
    return new ApiErrorResponse(HttpStatus.UNAUTHORIZED.value(), exception.getMessage());
  }

  @ExceptionHandler(ConstraintViolationException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public ApiErrorResponse handleConstraintViolation(final ConstraintViolationException exception) {
    final String message =
        exception.getConstraintViolations().stream()
            .map(v -> v.getPropertyPath() + ": " + v.getMessage())
            .collect(Collectors.joining(", "));
    return new ApiErrorResponse(HttpStatus.BAD_REQUEST.value(), message);
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public ApiErrorResponse handleMethodArgumentNotValid(
      final MethodArgumentNotValidException exception) {
    final String message =
        exception.getBindingResult().getFieldErrors().stream()
            .map(e -> e.getField() + ": " + e.getDefaultMessage())
            .collect(Collectors.joining(", "));
    return new ApiErrorResponse(HttpStatus.BAD_REQUEST.value(), message);
  }

  @ExceptionHandler(AccountConflictException.class)
  @ResponseStatus(HttpStatus.CONFLICT)
  public ApiErrorResponse handleAccountConflict(final AccountConflictException exception) {
    return new ApiErrorResponse(HttpStatus.CONFLICT.value(), exception.getMessage());
  }

  @ExceptionHandler(AccountValidationException.class)
  @ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
  public ApiErrorResponse handleAccountValidation(final AccountValidationException exception) {
    return new ApiErrorResponse(HttpStatus.UNPROCESSABLE_ENTITY.value(), exception.getMessage());
  }

  @ExceptionHandler(AccountTimeoutException.class)
  @ResponseStatus(HttpStatus.GATEWAY_TIMEOUT)
  public ApiErrorResponse handleAccountTimeout(final AccountTimeoutException exception) {
    return new ApiErrorResponse(HttpStatus.GATEWAY_TIMEOUT.value(), exception.getMessage());
  }

  @ExceptionHandler(AccountServiceException.class)
  @ResponseStatus(HttpStatus.BAD_GATEWAY)
  public ApiErrorResponse handleAccountService(final AccountServiceException exception) {
    return new ApiErrorResponse(HttpStatus.BAD_GATEWAY.value(), exception.getMessage());
  }

  @ExceptionHandler(DomainException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public ApiErrorResponse handleDomain(final DomainException exception) {
    return new ApiErrorResponse(HttpStatus.BAD_REQUEST.value(), exception.getMessage());
  }

  @ExceptionHandler(PersistenceException.class)
  @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
  public ApiErrorResponse handlePersistence(final PersistenceException ignored) {
    return new ApiErrorResponse(
        HttpStatus.INTERNAL_SERVER_ERROR.value(), "Error de persistencia.");
  }

  @ExceptionHandler(Exception.class)
  @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
  public ApiErrorResponse handleGeneral(final Exception ignored) {
    return new ApiErrorResponse(
        HttpStatus.INTERNAL_SERVER_ERROR.value(), "Error interno del servidor.");
  }
}

