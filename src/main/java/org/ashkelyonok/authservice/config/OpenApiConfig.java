package org.ashkelyonok.authservice.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI authServiceOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Auth Service API")
                        .version("1.0.0")
                        .description("Microservice for authentication")
                        .contact(new Contact()
                                .name("Anastasia Shkelyonok")
                                .email("anastasia.shkelyonok@gmail.com")))
                .servers(
                        List.of(new Server().url("http://localhost:8082").description("Development server")));
    }
}
