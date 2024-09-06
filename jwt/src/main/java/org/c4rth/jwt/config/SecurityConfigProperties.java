package org.c4rth.jwt.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Setter
@Getter
@Component
@ConfigurationProperties(prefix = "application.security")
public class SecurityConfigProperties {
    private String secretKey;
    private Long jwtTtl;
}