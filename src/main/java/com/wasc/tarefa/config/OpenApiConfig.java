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
                        .title("API de Gerenciamento de Tarefas") // Título da sua API
                        .version("1.0.0") // Versão da sua API
                        .description("Esta API permite o gerenciamento de usuários e suas tarefas.") // Descrição
                        .termsOfService("http://swagger.io/terms/") // Opcional: Termos de serviço
                        .contact(new Contact() // Opcional: Informações de contato
                                .name("Wagner")
                                .url("http://wasc.com")
                                .email("devwasc@gmail.com"))
                        .license(new License() // Opcional: Informações de licença
                                .name("Apache 2.0")
                                .url("http://springdoc.org")));
    }
}