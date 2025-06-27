package com.wasc.tarefa.security;

import com.wasc.tarefa.service.UserDetailsServiceImpl;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtRequestFilter extends OncePerRequestFilter {

    @Autowired
    private UserDetailsServiceImpl userDetailsService;

    @Autowired
    private JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        // CORREÇÃO CRÍTICA: IGNORE O FILTRO JWT PARA ROTAS WEB PADRÃO (HTML PAGES).
        // Ele só deve processar JWT para requisições de API REST.
        // Rotas como /login, /tasks, /register são controladas pela sessão do Spring Security.
        if (!request.getRequestURI().startsWith("/api/")) { // <--- ESTA LINHA É CRUCIAL!
            filterChain.doFilter(request, response);
            return; // Sai do filtro JWT para não interferir
        }

        // Se a requisição é para /api/, então tenta processar o JWT
        final String authorizationHeader = request.getHeader("Authorization");

        String username = null;
        String jwt = null;

        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            jwt = authorizationHeader.substring(7);
            try {
                username = jwtUtil.extractUsername(jwt);
            } catch (Exception e) {
                System.out.println("Erro ao extrair username ou token inválido: " + e.getMessage());
                // Se o token for inválido/expirado, não autenticar, mas não bloquear a cadeia para 401/403 ser gerado depois
                // SecurityContextHolder.clearContext(); // Não limpe o contexto aqui se for para cair em outros filtros
            }
        }

        // Se o username foi extraído e não há autenticação no contexto de segurança atual (apenas para este filtro)
        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            UserDetails userDetails = this.userDetailsService.loadUserByUsername(username);

            if (jwtUtil.validateToken(jwt, userDetails)) {
                UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken = new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities());
                usernamePasswordAuthenticationToken
                        .setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(usernamePasswordAuthenticationToken);
            }
        }
        filterChain.doFilter(request, response);
    }
}
