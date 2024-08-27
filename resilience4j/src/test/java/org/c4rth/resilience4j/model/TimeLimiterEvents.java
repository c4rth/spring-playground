package org.c4rth.resilience4j.model;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
public class TimeLimiterEvents {

    private List<TimeLimiterEvent> timeLimiterEvents;

}