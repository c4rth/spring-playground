package org.c4rth.resilience4j;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import org.apache.commons.io.IOUtils;
import org.c4rth.resilience4j.model.RateLimiterEvent;
import org.c4rth.resilience4j.model.RateLimiterEvents;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.ResponseEntity;
import org.springframework.test.annotation.DirtiesContext;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.IntStream;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.HttpStatus.TOO_MANY_REQUESTS;

/**
 * This was failing as a unit test in integrated environment
 * probably due to parallel execution of tests.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class RateLimiterEventTest {

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
    void testRateLimiterEvents() throws Exception {
        EXTERNAL_SERVICE.stubFor(WireMock.get("/api/external").willReturn(ok()));
        Map<Integer, Integer> responseStatusCount = new ConcurrentHashMap<>();

        IntStream.rangeClosed(1, 50)
                .forEach(
                        i -> {
                            ResponseEntity<String> response =
                                    restTemplate.getForEntity("/api/rate-limiter", String.class);
                            int statusCode = response.getStatusCode().value();
                            responseStatusCount.put(
                                    statusCode, responseStatusCount.getOrDefault(statusCode, 0) + 1);
                        });

        assertEquals(2, responseStatusCount.keySet().size());
        assertTrue(responseStatusCount.containsKey(TOO_MANY_REQUESTS.value()));
        assertTrue(responseStatusCount.containsKey(OK.value()));
        EXTERNAL_SERVICE.verify(5, getRequestedFor(urlEqualTo("/api/external")));

        List<RateLimiterEvent> rateLimiterEvents = getRateLimiterEvents();
        assertThat(rateLimiterEvents.size()).isEqualTo(50);

        // First allowed calls in the rate limit is 5, so we should see for those SUCCESSFUL_ACQUIRE
        // events
        IntStream.rangeClosed(0, 4)
                .forEach(
                        i -> {
                            assertThat(rateLimiterEvents.get(i).getRateLimiterName())
                                    .isEqualTo("externalService");
                            assertThat(rateLimiterEvents.get(i).getType()).isEqualTo("SUCCESSFUL_ACQUIRE");
                            assertThat(rateLimiterEvents.get(i).getCreationTime()).isNotNull();
                        });

        // the rest should be FAILED_ACQUIRE events since the rate limiter kicks in
        IntStream.rangeClosed(5, rateLimiterEvents.size() - 1)
                .forEach(
                        i -> {
                            assertThat(rateLimiterEvents.get(i).getRateLimiterName())
                                    .isEqualTo("externalService");
                            assertThat(rateLimiterEvents.get(i).getType()).isEqualTo("FAILED_ACQUIRE");
                            assertThat(rateLimiterEvents.get(i).getCreationTime()).isNotNull();
                        });
    }

    private List<RateLimiterEvent> getRateLimiterEvents() throws Exception {
        String jsonEventsList =
                IOUtils.toString(
                        new URI("http://localhost:" + port + "/actuator/ratelimiterevents"), StandardCharsets.UTF_8);
        RateLimiterEvents rateLimiterEvents =
                objectMapper.readValue(jsonEventsList, RateLimiterEvents.class);
        return rateLimiterEvents.getRateLimiterEvents();
    }
}