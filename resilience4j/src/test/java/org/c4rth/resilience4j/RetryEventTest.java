package org.c4rth.resilience4j;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import org.apache.commons.io.IOUtils;
import org.c4rth.resilience4j.model.RetryEvent;
import org.c4rth.resilience4j.model.RetryEvents;
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
import java.util.stream.IntStream;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

/**
 * This was failing as a unit test in integrated environment
 * probably due to parallel execution of tests.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class RetryEventTest {

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
    void testRetryEvents() throws Exception {
        EXTERNAL_SERVICE.stubFor(WireMock.get("/api/external").willReturn(ok()));
        ResponseEntity<String> response1 = restTemplate.getForEntity("/api/retry", String.class);
        EXTERNAL_SERVICE.verify(1, getRequestedFor(urlEqualTo("/api/external")));

        EXTERNAL_SERVICE.resetRequests();

        EXTERNAL_SERVICE.stubFor(WireMock.get("/api/external").willReturn(serverError()));
        ResponseEntity<String> response2 = restTemplate.getForEntity("/api/retry", String.class);
        assertThat(response2.getBody()).isEqualTo("all retries have exhausted");
        EXTERNAL_SERVICE.verify(3, getRequestedFor(urlEqualTo("/api/external")));

        List<RetryEvent> retryEvents = getRetryEvents();
        assertThat(retryEvents.size()).isEqualTo(3);

        // First 2 events should be retry events
        IntStream.rangeClosed(0, 1)
                .forEach(
                        i -> {
                            assertThat(retryEvents.get(i).getRetryName()).isEqualTo("externalService");
                            assertThat(retryEvents.get(i).getType()).isEqualTo("RETRY");
                            assertThat(retryEvents.get(i).getCreationTime()).isNotNull();
                            assertThat(retryEvents.get(i).getErrorMessage()).isNotNull();
                            assertThat(retryEvents.get(i).getNumberOfAttempts()).isEqualTo(i + 1);
                        });

        // Last event should be an error event because the configured num of retries is reached
        RetryEvent errorRetryEvent = retryEvents.get(2);
        assertThat(errorRetryEvent.getRetryName()).isEqualTo("externalService");
        assertThat(errorRetryEvent.getType()).isEqualTo("ERROR");
        assertThat(errorRetryEvent.getCreationTime()).isNotNull();
        assertThat(errorRetryEvent.getErrorMessage()).isNotNull();
        assertThat(errorRetryEvent.getNumberOfAttempts()).isEqualTo(3);
    }

    private List<RetryEvent> getRetryEvents() throws Exception {
        String jsonEventsList =
                IOUtils.toString(
                        new URI("http://localhost:" + port + "/actuator/retryevents"), StandardCharsets.UTF_8);
        RetryEvents retryEvents = objectMapper.readValue(jsonEventsList, RetryEvents.class);
        return retryEvents.getRetryEvents();
    }

}