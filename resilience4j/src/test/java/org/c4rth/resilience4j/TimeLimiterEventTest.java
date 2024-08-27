package org.c4rth.resilience4j;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import org.apache.commons.io.IOUtils;
import org.c4rth.resilience4j.model.TimeLimiterEvent;
import org.c4rth.resilience4j.model.TimeLimiterEvents;
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

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

/**
 * This was failing as a unit test in integrated environment
 * probably due to parallel execution of tests.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class TimeLimiterEventTest {

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
    void testTimeLimiterEvents() throws Exception {
        EXTERNAL_SERVICE.stubFor(WireMock.get("/api/external").willReturn(ok()));
        ResponseEntity<String> response = restTemplate.getForEntity("/api/time-limiter", String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.REQUEST_TIMEOUT);
        EXTERNAL_SERVICE.verify(1, getRequestedFor(urlEqualTo("/api/external")));

        List<TimeLimiterEvent> timeLimiterEvents = getTimeLimiterEvents();
        assertThat(timeLimiterEvents.size()).isEqualTo(1);
        TimeLimiterEvent timeoutEvent = timeLimiterEvents.getFirst();
        assertThat(timeoutEvent.getTimeLimiterName()).isEqualTo("externalService");
        assertThat(timeoutEvent.getType()).isEqualTo("TIMEOUT");
        assertThat(timeoutEvent.getCreationTime()).isNotNull();
    }

    private List<TimeLimiterEvent> getTimeLimiterEvents() throws Exception {
        String jsonEventsList =
                IOUtils.toString(
                        new URI("http://localhost:" + port + "/actuator/timelimiterevents"), StandardCharsets.UTF_8);
        TimeLimiterEvents timeLimiterEvents =
                objectMapper.readValue(jsonEventsList, TimeLimiterEvents.class);
        return timeLimiterEvents.getTimeLimiterEvents();
    }

}