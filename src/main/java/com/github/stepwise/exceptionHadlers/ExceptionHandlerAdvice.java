package com.github.stepwise.exceptionHadlers;

import java.util.HashMap;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import com.github.stepwise.web.dto.MessageResponse;
import lombok.extern.slf4j.Slf4j;

@RestControllerAdvice
@Slf4j
public class ExceptionHandlerAdvice {

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<Map<String, String>> handleValidationExceptions(
      MethodArgumentNotValidException ex) {
    Map<String, String> errors = new HashMap<>();

    for (FieldError error : ex.getBindingResult().getFieldErrors()) {
      errors.put(error.getField(), error.getDefaultMessage());
    }

    return new ResponseEntity<>(errors, HttpStatus.BAD_REQUEST);
  }

  @ExceptionHandler(AccessDeniedException.class)
  public ResponseEntity<?> handleAccessDeniedException(AccessDeniedException ex) {
    return new ResponseEntity<>(new MessageResponse("Access Denied: Insufficient permissions"),
        HttpStatus.FORBIDDEN);
  }

  @ExceptionHandler(BadCredentialsException.class)
  public ResponseEntity<?> handleBadCredentialsException(BadCredentialsException ex) {
    return new ResponseEntity<>(
        new MessageResponse("Bad credentials: Invalid username or password"),
        HttpStatus.UNAUTHORIZED);
  }

  @ExceptionHandler(MaxUploadSizeExceededException.class)
  public ResponseEntity<String> handleMaxSizeException(MaxUploadSizeExceededException exc) {
    return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE)
        .body("File size exceeds the maximum limit of 2MB!");
  }

  @ExceptionHandler(IllegalArgumentException.class)
  public ResponseEntity<?> handleIllegalArgumentException(IllegalArgumentException ex) {
    return new ResponseEntity<>(new MessageResponse("Invalid input: " + ex.getMessage()),
        HttpStatus.BAD_REQUEST);
  }

  @ExceptionHandler(NullPointerException.class)
  public ResponseEntity<?> handleNullPointerException(NullPointerException ex) {
    return new ResponseEntity<>(new MessageResponse("Internal error: " + ex.getMessage()),
        HttpStatus.INTERNAL_SERVER_ERROR);
  }


  @ExceptionHandler(Exception.class)
  public ResponseEntity<?> handleGlobalException(Exception ex) {
    return new ResponseEntity<>(new MessageResponse("Unexpected error: " + ex.getMessage()),
        HttpStatus.INTERNAL_SERVER_ERROR);
  }



}
