package org.c4rth.demoresilience4j;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;

public class RetryTest extends AbstractRetryTest {

	@Test
	public void backendAShouldRetryThreeTimes() {
		// When
		float currentCount = getCurrentCount(FAILED_WITH_RETRY, BACKEND_A);
		produceFailure(BACKEND_A);

		checkMetrics(FAILED_WITH_RETRY, BACKEND_A, currentCount + 1);
	}

	@Test
	public void backendAShouldSucceedWithoutRetry() {
		float currentCount = getCurrentCount(SUCCESS_WITHOUT_RETRY, BACKEND_A);
		produceSuccess(BACKEND_A);

		checkMetrics(SUCCESS_WITHOUT_RETRY, BACKEND_A, currentCount + 1);
	}

	private void produceFailure(String backend) {
		ResponseEntity<String> response = restTemplate.getForEntity("/" + backend + "/failure", String.class);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
	}

	private void produceSuccess(String backend) {
		ResponseEntity<String> response = restTemplate.getForEntity("/" + backend + "/success", String.class);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
	}

}