package com.wasc.tarefa.controller.api;

import com.wasc.tarefa.model.LoginRequest;
import com.wasc.tarefa.model.LoginResponse;
import com.wasc.tarefa.security.JwtUtil;
import com.wasc.tarefa.service.UserDetailsServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import io.swagger.v3.oas.annotations.Hidden; // Para ocultar no Swagger UI, se desejar

@RestController
@RequestMapping("/api/auth") // Prefixo para diferenciar da web e agrupar endpoints de autenticação da API
@Hidden // Opcional: Oculta este controlador no Swagger UI se for apenas para uso de máquina
public class AuthRestController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserDetailsServiceImpl userDetailsService;

    @Autowired
    private JwtUtil jwtUtil;

    // Endpoint para login e obtenção do JWT (para clientes API)
    @PostMapping("/login") // Endpoint completo será /api/auth/login
    public ResponseEntity<?> createAuthenticationToken(@RequestBody LoginRequest authenticationRequest) throws Exception {
        try {
            // Tenta autenticar o usuário com as credenciais fornecidas
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(authenticationRequest.getUsername(), authenticationRequest.getPassword())
            );
        } catch (BadCredentialsException e) {
            // Lança uma exceção específica para credenciais inválidas
            throw new Exception("Nome de usuário ou senha incorretos", e);
        }

        // Se a autenticação foi bem-sucedida, carrega os detalhes do usuário
        final UserDetails userDetails = userDetailsService.loadUserByUsername(authenticationRequest.getUsername());

        // Gera o JWT
        final String jwt = jwtUtil.generateToken(userDetails);

        // Retorna o JWT na resposta
        return ResponseEntity.ok(new LoginResponse(jwt));
    }

    // Nota: O endpoint /api/auth/register (para registro de usuário via API)
    // já está no TarefaController (API REST), ou você pode movê-lo para cá se preferir.
    // Se ele já existe em TarefaController e você quer ele aqui, copie e ajuste.
    // Por enquanto, assumo que o registro via API continua no TarefaController.
}
