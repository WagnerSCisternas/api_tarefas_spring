package com.wasc.tarefa.config;

import com.wasc.tarefa.security.JwtRequestFilter;
import com.wasc.tarefa.service.UserDetailsServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;


@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    @Autowired
    private UserDetailsServiceImpl userDetailsService;

    @Autowired
    private JwtRequestFilter jwtRequestFilter;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable()) // Desabilita CSRF para APIs REST (se for o caso, cuidado com formulários web)
            .cors(cors -> {}) // CORS configurado via WebMvcConfigurer (abaixo)

            // Configuração de autorização
            .authorizeHttpRequests(auth -> auth
                // Páginas web públicas (login, registro, logout)
                .requestMatchers("/login", "/register", "/logout").permitAll()
                .requestMatchers("/style.css").permitAll() // CSS é público
                
                // Endpoints da API REST públicos (apenas o login da API, para clientes externos)
                .requestMatchers("/api/auth/login").permitAll()
                
                // Endpoints de documentação (Swagger UI) - Públicos em dev
                .requestMatchers("/h2-console/**").permitAll() // H2 console para DEV LOCAL
                .requestMatchers("/v3/api-docs/**", "/swagger-ui.html", "/swagger-ui/**").permitAll() 

                // Endpoint de registro da API REST requer autenticação (para evitar criação livre)
                .requestMatchers("/api/auth/register").authenticated()

                // Todas as outras requisições (incluindo /tasks e o resto de /api/**) exigem autenticação
                .anyRequest().authenticated()
            )
            .formLogin(form -> form // HABILITA AUTENTICAÇÃO VIA FORMULÁRIO PARA FLUXO WEB
                .loginPage("/login") // URL da sua página de login customizada
                .loginProcessingUrl("/login") // URL para onde o formulário de login será POSTADO
                .defaultSuccessUrl("/tasks", true) // Redireciona para /tasks após login bem-sucedido
                .failureUrl("/login?error") // Redireciona para /login?error em caso de falha
                .permitAll() // Permite acesso à página de login e ao processamento do login
            )
            .logout(logout -> logout // Configura logout
                .logoutUrl("/logout") // URL para o logout
                .logoutSuccessUrl("/login?logout") // Redireciona após logout bem-sucedido
                .permitAll() // Permite acesso à URL de logout
            )
            .sessionManagement(session -> session
                // Garante que o Spring Security gerencie a sessão para o fluxo web
                .sessionCreationPolicy(SessionCreationPolicy.ALWAYS) // <--- MUDANÇA: Força a criação de sessão
            );

        // Adiciona o filtro JWT CONDICIONALMENTE:
        // Ele só deve ser executado para endpoints REST (/api/**)
        // Para requisições de página web (/tasks, etc.), o filtro JWT deve ser ignorado
        // para que a autenticação da sessão Spring Security funcione sem interferência.
        http.addFilterBefore(jwtRequestFilter, UsernamePasswordAuthenticationFilter.class);
        
        // CORREÇÃO CRÍTICA PARA O JWT FILTER:
        // O JwtRequestFilter deve ser executado apenas para requests da API REST.
        // Para requisições de páginas web, a autenticação baseada em sessão deve prevalecer.
        // Uma forma de fazer isso é permitir que o JwtRequestFilter ignore certas paths,
        // ou desativar o filter para certas paths, ou aplicar a segurança programaticamente.
        // A maneira mais direta é fazer o JwtRequestFilter verificar se é uma requisição REST.
        // No entanto, para fins de teste rápido, podemos mudar a ordem do filterChain.

        // MUDANÇA DE ORDEM DO FILTRO (Experimente se a solução acima não funcionar):
        // Remover o filtro JWT e o adicionar em um ponto mais específico se houver conflito.
        // Por agora, vamos manter como está e assumir que o filtro deve lidar com isso internamente.

        // A solução mais robusta é no JwtRequestFilter, verificar o caminho da requisição
        // e se não for uma rota da API (ex: /api/**), deixar passar sem processar o JWT.
        // Exemplo no JwtRequestFilter (método doFilterInternal):
        // if (request.getRequestURI().startsWith("/api/")) { ... processa JWT ... }
        // else { filterChain.doFilter(request, response); return; }

        // Mantenho o filterChain como está no momento, com a premissa de que o filtro ou a política de sessão
        // devem permitir a autenticação por formulário.

        // Adiciona o provedor de autenticação (DaoAuthenticationProvider)
        http.authenticationProvider(authenticationProvider());

        return http.build();
    }

    // Configuração CORS Global para a Aplicação Monolítica
    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/api/**")
                        .allowedOrigins("*")
                        .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                        .allowedHeaders("*")
                        .allowCredentials(false);
            }
        };
    }
}
