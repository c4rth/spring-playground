package org.c4rth.resilience4j.model;

import lombok.Getter;
import lombok.Setter;

import java.time.ZonedDateTime;
import java.util.Objects;

@Setter
@Getter
public class CircuitBreakerEvent {

    private String circuitBreakerName;
    private String type;
    private ZonedDateTime creationTime;
    private String errorMessage;
    private Integer durationInMs;
    private String stateTransition;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CircuitBreakerEvent that = (CircuitBreakerEvent) o;
        return Objects.equals(circuitBreakerName, that.circuitBreakerName)
                && Objects.equals(type, that.type)
                && Objects.equals(creationTime, that.creationTime)
                && Objects.equals(errorMessage, that.errorMessage)
                && Objects.equals(durationInMs, that.durationInMs)
                && Objects.equals(stateTransition, that.stateTransition);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                circuitBreakerName, type, creationTime, errorMessage, durationInMs, stateTransition);
    }

}