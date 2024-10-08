package org.c4rth.resilience4j;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import org.apache.commons.io.IOUtils;
import org.c4rth.resilience4j.model.CircuitBreakerEvent;
import org.c4rth.resilience4j.model.CircuitBreakerEvents;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.annotation.DirtiesContext;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.IntStream;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

/**
 * This was failing as a unit test in integrated environment
 * probably due to parallel execution of tests.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class CircuitBreakerEventTest {

    private static final ObjectMapper objectMapper =
            new ObjectMapper().registerModule(new JavaTimeModule());
    @RegisterExtension
    static WireMockExtension EXTERNAL_SERVICE =
            WireMockExtension.newInstance()
                    .options(WireMockConfiguration.wireMockConfig().port(9090))
                    .build();
    private final Logger LOGGER = LoggerFactory.getLogger(getClass());
    @Autowired
    private TestRestTemplate restTemplate;
    @LocalServerPort
    private Integer port;

    @Test
    void testCircuitBreakerEvents() throws Exception {
        EXTERNAL_SERVICE.stubFor(WireMock.get("/api/external").willReturn(serverError()));

        IntStream.rangeClosed(1, 5)
                .forEach(
                        i -> {
                            ResponseEntity<String> response =
                                    restTemplate.getForEntity("/api/circuit-breaker", String.class);
                            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
                        });

        // Fetch the events generated by the above calls
        List<CircuitBreakerEvent> circuitBreakerEvents = getCircuitBreakerEvents();
        assertThat(circuitBreakerEvents.size()).isEqualTo(7);

        // The first 5 events are the error events corresponding to the above server error responses
        IntStream.rangeClosed(0, 4)
                .forEach(
                        i -> {
                            assertThat(circuitBreakerEvents.get(i).getCircuitBreakerName())
                                    .isEqualTo("externalService");
                            assertThat(circuitBreakerEvents.get(i).getType()).isEqualTo("ERROR");
                            assertThat(circuitBreakerEvents.get(i).getCreationTime()).isNotNull();
                            assertThat(circuitBreakerEvents.get(i).getErrorMessage()).isNotNull();
                            assertThat(circuitBreakerEvents.get(i).getDurationInMs()).isNotNull();
                            assertThat(circuitBreakerEvents.get(i).getStateTransition()).isNull();
                        });

        // Following event signals the configured failure rate exceeded
        CircuitBreakerEvent failureRateExceededEvent = circuitBreakerEvents.get(5);
        assertThat(failureRateExceededEvent.getCircuitBreakerName()).isEqualTo("externalService");
        assertThat(failureRateExceededEvent.getType()).isEqualTo("FAILURE_RATE_EXCEEDED");
        assertThat(failureRateExceededEvent.getCreationTime()).isNotNull();
        assertThat(failureRateExceededEvent.getErrorMessage()).isNull();
        assertThat(failureRateExceededEvent.getDurationInMs()).isNull();
        assertThat(failureRateExceededEvent.getStateTransition()).isNull();

        // Following event signals the state transition from CLOSED TO OPEN
        CircuitBreakerEvent stateTransitionEvent = circuitBreakerEvents.get(6);
        assertThat(stateTransitionEvent.getCircuitBreakerName()).isEqualTo("externalService");
        assertThat(stateTransitionEvent.getType()).isEqualTo("STATE_TRANSITION");
        assertThat(stateTransitionEvent.getCreationTime()).isNotNull();
        assertThat(stateTransitionEvent.getErrorMessage()).isNull();
        assertThat(stateTransitionEvent.getDurationInMs()).isNull();
        assertThat(stateTransitionEvent.getStateTransition()).isEqualTo("CLOSED_TO_OPEN");

        IntStream.rangeClosed(1, 5)
                .forEach(
                        i -> {
                            ResponseEntity<String> response =
                                    restTemplate.getForEntity("/api/circuit-breaker", String.class);
                            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.SERVICE_UNAVAILABLE);
                        });

        /// Fetch the events generated by the above calls
        List<CircuitBreakerEvent> updatedCircuitBreakerEvents = getCircuitBreakerEvents();
        assertThat(updatedCircuitBreakerEvents.size()).isEqualTo(12);

        // Newly added events will be of type NOT_PERMITTED since the Circuit Breaker is in OPEN state
        IntStream.rangeClosed(7, 11)
                .forEach(
                        i -> {
                            assertThat(updatedCircuitBreakerEvents.get(i).getCircuitBreakerName())
                                    .isEqualTo("externalService");
                            assertThat(updatedCircuitBreakerEvents.get(i).getType()).isEqualTo("NOT_PERMITTED");
                            assertThat(updatedCircuitBreakerEvents.get(i).getCreationTime()).isNotNull();
                            assertThat(updatedCircuitBreakerEvents.get(i).getErrorMessage()).isNull();
                            assertThat(updatedCircuitBreakerEvents.get(i).getDurationInMs()).isNull();
                            assertThat(updatedCircuitBreakerEvents.get(i).getStateTransition()).isNull();
                        });

        EXTERNAL_SERVICE.verify(5, getRequestedFor(urlEqualTo("/api/external")));
    }

    private List<CircuitBreakerEvent> getCircuitBreakerEvents() throws Exception {
        String jsonEventsList =
                IOUtils.toString(
                        new URI("http://localhost:" + port + "/actuator/circuitbreakerevents"), StandardCharsets.UTF_8);
        CircuitBreakerEvents circuitBreakerEvents =
                objectMapper.readValue(jsonEventsList, CircuitBreakerEvents.class);
        return circuitBreakerEvents.getCircuitBreakerEvents();
    }

}