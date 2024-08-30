package org.c4rth.cloudazure;

import com.azure.security.keyvault.secrets.SecretClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
@Slf4j
public class AzureCloudApplication {

    public static void main(String[] args) {
        SpringApplication.run(AzureCloudApplication.class, args);
    }

    @Bean
    public CommandLineRunner commandLineRunner(SecretClient secretClient) {
        return args -> {
            log.info("sampleProperty: {}", secretClient.getSecret("mysecret").getValue());

            secretClient.listPropertiesOfSecrets().forEach( secretProps ->  {
                log.info("getContentType: {}", secretProps.getContentType());
                log.info("getCreatedOn: {}", secretProps.getCreatedOn());
                log.info("getId: {}", secretProps.getId());
                log.info("getName: {}", secretProps.getName());
                log.info("getVersion: {}", secretProps.getVersion());
            });
        };
    }
}
