package org.c4rth.cronjob;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestClient;

@SpringBootApplication
@Slf4j
public class CronJobApplication {

    public static void main(String[] args) {
        SpringApplication application = new SpringApplication(CronJobApplication.class);
        application.setWebApplicationType(WebApplicationType.NONE);
        application.run(args);
    }

    @Bean
    public CommandLineRunner commandLineRunner(ApplicationContext ctx, RestClient.Builder restClientBuilder) {
        return args -> {
            log.info("Starting CronJob...");
            log.info("Application name: {}", ctx.getApplicationName());
            log.info("Display name: {}", ctx.getDisplayName());
            log.info("Beans");
            for (String beanName : ctx.getBeanDefinitionNames()) {
                log.info("- {}", beanName);
                Thread.sleep(50);
            }

            //throw new RuntimeException("intentional error");
            /*
            log.info("Call quitquitquit");
            RestClient restClient = restClientBuilder.build();
            try {
                RestClient.ResponseSpec response = restClient.post().uri("http://127.0.0.1:15020/quitquitquit").retrieve();
                log.info("response: {}", response);
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
            log.info("Call quitquitquit - done");
             */
        };
    }
}
