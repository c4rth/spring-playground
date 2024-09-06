package org.c4rth.cloudazure;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.*;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
@ActiveProfiles("test")
@Slf4j
public class JwtClientTest {

    @Autowired
    private AzureAdConfig azureAdConfig;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public String getJwtToken() throws Exception {
        String tokenUrl = "https://login.microsoftonline.com/" + azureAdConfig.getTenantId() + "/oauth2/v2.0/token";

        // Set headers
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        // Set body parameters
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("client_id", azureAdConfig.getClientId());
        body.add("scope", azureAdConfig.getScope());
        body.add("client_secret", azureAdConfig.getClientSecret());
        body.add("grant_type", azureAdConfig.getGrantType());

        // Create the HTTP entity with headers and body
        HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(body, headers);

        // Send POST request to Azure AD's token endpoint
        ResponseEntity<String> response = restTemplate.exchange(
                tokenUrl,
                HttpMethod.POST,
                requestEntity,
                String.class
        );

        // Parse the response body to extract the JWT token
        log.info("response: {}", response);
        log.info("response body: {}", response.getBody());
        JsonNode jsonNode = objectMapper.readTree(response.getBody());
        return jsonNode.get("access_token").asText();
    }

    @Test
    void testJwtClient() throws Exception {
        String jwtToken = getJwtToken();
        assertNotNull(jwtToken);
        log.info(jwtToken);

        String jwksUri = "https://login.microsoftonline.com/" + azureAdConfig.getTenantId() + "/discovery/v2.0/keys";
        NimbusJwtDecoder jwtDecoder = NimbusJwtDecoder.withJwkSetUri(jwksUri).build();
        Jwt jwt = jwtDecoder.decode(jwtToken);

        log.info("subject: {}",jwt.getSubject());
        log.info("id: {}",jwt.getId());
        log.info("audience: {}",String.join(",", jwt.getAudience()));
        log.info("issuer: {}",jwt.getIssuer());
        log.info("expiresAt: {}",jwt.getExpiresAt());
        log.info("nbf: {}",jwt.getNotBefore());
        log.info("iat: {}",jwt.getIssuedAt());
        log.info("claims:");
        jwt.getClaims();
        jwt.getClaims().forEach((k, v) -> {
            log.info("   - {}:{}", k, v);
        });

    }
}
