package org.c4rth.problem.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import org.springframework.http.ProblemDetail;

import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    private final CorrelationIDHandler correlationIDHandler;

    public GlobalExceptionHandler(CorrelationIDHandler correlationIDHandler) {
        this.correlationIDHandler = correlationIDHandler;
    }

    @ExceptionHandler(ProductNotFoundException.class)
    ProblemDetail handleProductNotFoundException(ProductNotFoundException e) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, e.getMessage());
        problemDetail.setProperty("returnCode", e.getReturnCode());
        problemDetail.setProperty("subReturnCode", e.getSubReturnCode());
        problemDetail.setProperty("type", e.getExceptionType());
        problemDetail.setProperty("message", e.getMessage());
        problemDetail.setProperty("traceId", correlationIDHandler.getTraceId());
        List<String> errors = List.of("error1", "error2", "error3");
        problemDetail.setProperty("errors", errors);
        List<String> warnings = List.of("warning1", "warning2", "warning3");
        problemDetail.setProperty("warnings", warnings);
        return problemDetail;
    }
}