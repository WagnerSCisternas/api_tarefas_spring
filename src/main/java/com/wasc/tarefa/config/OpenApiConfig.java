package com.wasc.tarefa.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("API e Frontend Integrado de Tarefas")
                        .version("1.0.0")
                        .description("Esta aplicação integra a API de gerenciamento de usuários e tarefas com um frontend web.")
                        .termsOfService("http://swagger.io/terms/")
                        .contact(new Contact()
                                .name("Seu Nome ou Equipe")
                                .url("http://seusite.com")
                                .email("seu.email@example.com"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("http://springdoc.org")));
    }
}