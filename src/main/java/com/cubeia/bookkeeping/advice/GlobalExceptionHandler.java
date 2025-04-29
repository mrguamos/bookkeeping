package com.cubeia.bookkeeping.advice;

import com.cubeia.bookkeeping.exception.InsufficientFundsException;
import com.cubeia.bookkeeping.exception.NotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@RestControllerAdvice
public class GlobalExceptionHandler  extends ResponseEntityExceptionHandler {

  @ExceptionHandler({
    InsufficientFundsException.class
  })
  ProblemDetail handleConflict(InsufficientFundsException ex, WebRequest request) {
    return super.createProblemDetail(
      ex,
      HttpStatus.CONFLICT,
      ex.getMessage(),
      null,
      null,
      request
    );
  }

  @ExceptionHandler({
    IllegalArgumentException.class
  })
  ProblemDetail handleIllegalArgument(IllegalArgumentException ex, WebRequest request) {
    return super.createProblemDetail(
      ex,
      HttpStatus.BAD_REQUEST,
      ex.getMessage(),
      null,
      null,
      request
    );
  }

  @ExceptionHandler({
    NotFoundException.class
  })
  ProblemDetail handleNotFound(NotFoundException ex, WebRequest request) {
    return super.createProblemDetail(
      ex,
      HttpStatus.NOT_FOUND,
      ex.getMessage(),
      null,
      null,
      request
    );
  }

}
