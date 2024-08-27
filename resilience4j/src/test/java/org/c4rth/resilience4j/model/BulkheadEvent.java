package org.c4rth.resilience4j.model;

import lombok.Getter;
import lombok.Setter;

import java.time.ZonedDateTime;
import java.util.Objects;

@Setter
@Getter
public class BulkheadEvent {

    private String bulkheadName;
    private String type;
    private ZonedDateTime creationTime;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BulkheadEvent that = (BulkheadEvent) o;
        return Objects.equals(bulkheadName, that.bulkheadName)
                && Objects.equals(type, that.type)
                && Objects.equals(creationTime, that.creationTime);
    }

    @Override
    public int hashCode() {
        return Objects.hash(bulkheadName, type, creationTime);
    }

}