package org.c4rth.problem.exception;

public abstract class AbstractBusinessException extends RuntimeException {

    private final BusinessExceptionType exceptionType;
    private final String returnCode;
    private final String subReturnCode;
    private final String message;

    public AbstractBusinessException(BusinessExceptionType exceptionType, String returnCode, String subReturnCode, String message) {
        this.exceptionType = exceptionType;
        this.returnCode = returnCode;
        this.subReturnCode = subReturnCode;
        this.message = message;
    }

    public BusinessExceptionType getExceptionType() {
        return exceptionType;
    }

    public String getReturnCode() {
        return returnCode;
    }

    public String getSubReturnCode() {
        return subReturnCode;
    }

    public String getMessage() {
        return message;
    }
}
