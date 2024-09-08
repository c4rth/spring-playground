package org.c4rth.jpah2.config;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.c4rth.jpah2.db.DummyRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@AllArgsConstructor
public class LoadBasicData {

    private final DummyRepository dummyRepository;

    @Bean
    public CommandLineRunner commandLineRunner(ApplicationContext ctx) {
        return args -> {
            log.info("Starting...");
            log.info("Application name: {}", ctx.getApplicationName());
            log.info("Display name: {}", ctx.getDisplayName());

            if (dummyRepository.count() == 0) {
                log.info("No dummy data found");
            } else {
                log.info("Dummy data found");
                dummyRepository.findAll().forEach(row -> log.info("Row: {}", row));
            }
        };
    }

}
