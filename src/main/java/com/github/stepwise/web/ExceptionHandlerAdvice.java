// package com.github.stepwise.web;
//
// // import org.garin.core.exception.BlabberException;
// // import org.garin.core.web.dto.ErrorResponse;
// import org.springframework.http.HttpStatus;
// import org.springframework.http.ResponseEntity;
// import org.springframework.web.bind.annotation.ExceptionHandler;
// import org.springframework.web.bind.annotation.RestControllerAdvice;
//
// import lombok.extern.slf4j.Slf4j;
//
// @RestControllerAdvice
// @Slf4j
// public class ExceptionHandlerAdvice {
//
// @ExceptionHandler(BlabberException.class)
// public ResponseEntity<ErrorResponse> blabberExceptionHandler(BlabberException e) {
// log.error("Blabber exception", e);
//
// return response(HttpStatus.BAD_REQUEST, e.getMessage());
// }
//
// private ResponseEntity<ErrorResponse> response(HttpStatus status, String message) {
// var errorResponseBody = new ErrorResponse(message);
//
// return new ResponseEntity<>(errorResponseBody, status);
// }
//
// }
