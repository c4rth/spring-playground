package org.c4rth.problem.exception;

public class ProductNotFoundException extends AbstractBusinessException {

    public ProductNotFoundException(Long productId) {
        super(BusinessExceptionType.FATAL, "1234", "000000", "Product with id " + productId + " was not found!");
    }
}