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
import org.springframework.web.servlet.config.annotation.CorsRegistry; // Importar CorsRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer; // Importar WebMvcConfigurer


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

    // Configuração da cadeia de filtros de segurança HTTP
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable()) // Desabilita CSRF para APIs REST
            // CORS será tratado pelo WebMvcConfigurer para toda a aplicação
            .authorizeHttpRequests(auth -> auth
                // Endpoints públicos
                .requestMatchers("/login", "/register", "/logout").permitAll() // Web frontend pages
                .requestMatchers("/style.css").permitAll() // CSS file for frontend
                .requestMatchers("/api/auth/login").permitAll() // API login endpoint
                .requestMatchers("/h2-console/**").permitAll() // H2 console para DEV LOCAL
                .requestMatchers("/v3/api-docs/**", "/swagger-ui.html", "/swagger-ui/**").permitAll() // Swagger UI é público para visualização

                // Endpoints protegidos
                .requestMatchers("/api/auth/register").authenticated() // API register endpoint (requer token)
                .anyRequest().authenticated() // Todas as outras requisições (incluindo /tasks e /api/**) exigem autenticação
            )
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS) // Define a política de sessão como STATELESS (sem estado)
            );

        // Adiciona o filtro JWT antes do filtro de autenticação de usuário/senha
        http.addFilterBefore(jwtRequestFilter, UsernamePasswordAuthenticationFilter.class);

        // Adiciona o provedor de autenticação
        http.authenticationProvider(authenticationProvider());

        return http.build();
    }

    // Configuração CORS Global para a Aplicação Monolítica
    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                // Se a aplicação é monolítica e o frontend está no mesmo domínio/porta,
                // geralmente não precisa de CORS entre eles.
                // No entanto, se houver chamadas AJAX diretas do navegador para a API,
                // ou se o deploy em nuvem usar subdomínios diferentes para API vs. Web Controller,
                // esta configuração será útil.
                // Para simplificar, permitindo de qualquer origem para qualquer path da API.
                // EM PRODUÇÃO, RESTRINGIR 'allowedOrigins' para o domínio REAL DO SEU FRONTEND.
                registry.addMapping("/api/**") // Aplica CORS apenas para endpoints da API
                        .allowedOrigins("*") // Permite de qualquer origem
                        .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                        .allowedHeaders("*")
                        .allowCredentials(false); // Cookies não serão enviados (pois é JWT)
                // Para os endpoints web (/login, /tasks), CORS geralmente não se aplica.
            }
        };
    }
}