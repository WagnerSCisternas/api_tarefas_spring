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

@Configuration
@EnableWebSecurity // Habilita a segurança web
@EnableMethodSecurity(prePostEnabled = true) // Permite segurança baseada em anotações nos métodos
public class SecurityConfig {

    @Autowired
    private UserDetailsServiceImpl userDetailsService;

    @Autowired
    private JwtRequestFilter jwtRequestFilter;

    /// Codificador de senha: BCrypt é o padrão e recomendado
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // Provedor de autenticação que usa o UserDetailsService e o PasswordEncoder
    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());	
        return authProvider;
    }	

    // Gerenciador de autenticação (usado pelo AuthController)
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    // Cadeia de filtros de segurança HTTP
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable()) // Desabilita CSRF para APIs REST
            .cors(cors -> {}) // Mantém CORS configurado (já ativado nos controllers)
            .authorizeHttpRequests(auth -> auth
            //	.requestMatchers("/api/auth/register").authenticated()
            		
            	.requestMatchers("/api/auth/**").permitAll() // Permite acesso público ao endpoint de autenticação
                
                .requestMatchers("/h2-console/**").permitAll() // Permite acesso público ao H2 console (APENAS EM DEV!)
                .requestMatchers("/v3/api-docs/**", "/swagger-ui.html", "/swagger-ui/**").permitAll() // Para o Swagger UI e API Docs
                .anyRequest().authenticated() // Todas as outras requisições exigem autenticação
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
}