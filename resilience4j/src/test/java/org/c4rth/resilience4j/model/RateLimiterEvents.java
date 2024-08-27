package org.c4rth.resilience4j.model;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
public class RateLimiterEvents {

    private List<RateLimiterEvent> rateLimiterEvents;

}