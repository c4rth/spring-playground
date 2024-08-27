package org.c4rth.resilience4j;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import org.apache.commons.io.IOUtils;
import org.c4rth.resilience4j.model.BulkheadEvent;
import org.c4rth.resilience4j.model.BulkheadEvents;
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
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.IntStream;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.http.HttpStatus.BANDWIDTH_LIMIT_EXCEEDED;
import static org.springframework.http.HttpStatus.OK;

/**
 * This was failing as a unit test in integrated environment
 * probably due to parallel execution of tests.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class BulkheadEventTest {

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
    void testBulkheadEvents() throws Exception {
        EXTERNAL_SERVICE.stubFor(WireMock.get("/api/external").willReturn(ok()));
        Map<Integer, Integer> responseStatusCount = new ConcurrentHashMap<>();
        ExecutorService executorService = Executors.newFixedThreadPool(5);
        CountDownLatch latch = new CountDownLatch(5);

        IntStream.rangeClosed(1, 5)
                .forEach(
                        i ->
                                executorService.execute(
                                        () -> {
                                            ResponseEntity<String> response =
                                                    restTemplate.getForEntity("/api/bulkhead", String.class);
                                            int statusCode = response.getStatusCode().value();
                                            responseStatusCount.merge(statusCode, 1, Integer::sum);
                                            latch.countDown();
                                        }));
        latch.await();
        executorService.shutdown();

        assertEquals(2, responseStatusCount.keySet().size());
        LOGGER.info("Response statuses: " + responseStatusCount.keySet());
        assertTrue(responseStatusCount.containsKey(BANDWIDTH_LIMIT_EXCEEDED.value()));
        assertTrue(responseStatusCount.containsKey(OK.value()));
        EXTERNAL_SERVICE.verify(3, getRequestedFor(urlEqualTo("/api/external")));

        List<BulkheadEvent> bulkheadEvents = getBulkheadEvents();

        // Based on the configuration, the first 3 calls should be permitted, so we should see the
        // CALL_PERMITTED events
        IntStream.rangeClosed(0, 2)
                .forEach(
                        i -> {
                            assertThat(bulkheadEvents.get(i).getBulkheadName()).isEqualTo("externalService");
                            assertThat(bulkheadEvents.get(i).getType()).isEqualTo("CALL_PERMITTED");
                            assertThat(bulkheadEvents.get(i).getCreationTime()).isNotNull();
                        });

        // For the other 2 calls made we should see the CALL_REJECTED events
        IntStream.rangeClosed(3, 4)
                .forEach(
                        i -> {
                            assertThat(bulkheadEvents.get(i).getBulkheadName()).isEqualTo("externalService");
                            assertThat(bulkheadEvents.get(i).getType()).isEqualTo("CALL_REJECTED");
                            assertThat(bulkheadEvents.get(i).getCreationTime()).isNotNull();
                        });
    }

    private List<BulkheadEvent> getBulkheadEvents() throws Exception {
        String jsonEventsList =
                IOUtils.toString(
                        new URI("http://localhost:" + port + "/actuator/bulkheadevents"), StandardCharsets.UTF_8);
        BulkheadEvents bulkheadEvents = objectMapper.readValue(jsonEventsList, BulkheadEvents.class);
        return bulkheadEvents.getBulkheadEvents();
    }
}