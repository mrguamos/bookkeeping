package com.cubeia.bookkeeping.advice;

import com.cubeia.bookkeeping.exception.InsufficientFundsException;
import com.cubeia.bookkeeping.exception.NotFoundException;
import com.cubeia.bookkeeping.exception.SameAccountTransferException;
import com.cubeia.bookkeeping.exception.UniqueException;
import java.util.HashMap;
import java.util.Map;
import org.postgresql.util.PSQLException;
import org.postgresql.util.ServerErrorMessage;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

  /**
   * Map of constraint names to field names. This is used to map the constraint name from the
   * database to the field name in the application.
   */
  private static final Map<String, String> constraintMap = Map.of(
    "wallet_email_key", "email"
  );

  @ExceptionHandler({
    InsufficientFundsException.class,
    UniqueException.class
  })
  ProblemDetail handleConflict(RuntimeException ex, WebRequest request) {
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

  @ExceptionHandler({
    SameAccountTransferException.class
  })
  ProblemDetail handleBadRequest(RuntimeException ex, WebRequest request) {
    return super.createProblemDetail(
      ex,
      HttpStatus.BAD_REQUEST,
      ex.getMessage(),
      null,
      null,
      request
    );
  }

  @Override
  protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
    HttpHeaders headers, HttpStatusCode status, WebRequest request) {
    ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(status, "Validation failed");
    problemDetail.setTitle("Constraint Violation");

    Map<String, String> errors = new HashMap<>();
    ex.getBindingResult().getAllErrors().forEach((error) -> {
      String fieldName = ((FieldError) error).getField();
      String errorMessage = error.getDefaultMessage();
      errors.put(fieldName, errorMessage);
    });

    problemDetail.setProperty("errors", errors);

    return ResponseEntity.status(status).body(problemDetail);
  }

  @ExceptionHandler({
    PSQLException.class
  })
  ProblemDetail handleSQLException(PSQLException ex, WebRequest request) {

    if ("23505".equals(ex.getSQLState())) {
      ServerErrorMessage postgresError = ex.getServerErrorMessage();
      if (postgresError != null) {
        String constraint = postgresError.getConstraint();
        var field = constraintMap.get(constraint);
        if (field == null) {
          field = "Resource";
        }
        return super.createProblemDetail(
          ex,
          HttpStatus.CONFLICT,
          field + " already exists.",
          null,
          null,
          request
        );
      }
    }

    return super.createProblemDetail(
      ex,
      HttpStatus.INTERNAL_SERVER_ERROR,
      "Internal server error",
      null,
      null,
      request
    );
  }

}
