package com.medilabo.risk.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

/**
 * Active la liaison de {@link RiskProperties} (termes déclencheurs + URIs des
 * services amont) et fournit le {@code RestClient.Builder} utilisé par les clients.
 *
 * <p>En Spring Boot 4, {@code spring-boot-starter-webmvc} n'auto-configure pas de
 * bean {@code RestClient.Builder} — on le déclare donc explicitement.
 */
@Configuration
@EnableConfigurationProperties(RiskProperties.class)
public class RiskConfig {

    @Bean
    RestClient.Builder restClientBuilder() {
        return RestClient.builder();
    }
}
