package org.c4rth.resilience4j.model;

import lombok.Getter;
import lombok.Setter;

import java.time.ZonedDateTime;
import java.util.Objects;

@Setter
@Getter
public class RetryEvent {

    private String retryName;
    private String type;
    private ZonedDateTime creationTime;
    private String errorMessage;
    private Integer numberOfAttempts;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RetryEvent that = (RetryEvent) o;
        return Objects.equals(retryName, that.retryName)
                && Objects.equals(type, that.type)
                && Objects.equals(creationTime, that.creationTime)
                && Objects.equals(errorMessage, that.errorMessage)
                && Objects.equals(numberOfAttempts, that.numberOfAttempts);
    }

    @Override
    public int hashCode() {
        return Objects.hash(retryName, type, creationTime, errorMessage, numberOfAttempts);
    }

}