package com.wasc.tarefa.controller;

import com.wasc.tarefa.model.Usuario;
import com.wasc.tarefa.security.JwtUtil;
import com.wasc.tarefa.service.UserDetailsServiceImpl;
import com.wasc.tarefa.service.UsuarioService;

import io.swagger.v3.oas.annotations.Hidden;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin // Mantido para o frontend
public class AuthController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserDetailsServiceImpl userDetailsService;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private PasswordEncoder passwordEncoder; // Para criptografar senhas de novos usuários

    @Autowired
    private UsuarioService usuarioService; // Para salvar novos usuários

    // Endpoint para registrar um novo usuário
    @Hidden
    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody Usuario usuario) {
        if (usuarioService.findByNome(usuario.getNome()).isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Nome de usuário já existe.");
        }
        usuario.setSenha(passwordEncoder.encode(usuario.getSenha())); // Criptografa a senha
        usuario.setAtivo(true); // Define como ativo por padrão
        usuarioService.save(usuario);
        return ResponseEntity.status(HttpStatus.CREATED).body("Usuário registrado com sucesso.");
    }

    // Endpoint para login e obtenção do JWT
    @PostMapping("/login")
    public ResponseEntity<?> createAuthenticationToken(@RequestBody AuthenticationRequest authenticationRequest) throws Exception {
        try {
            // Tenta autenticar o usuário com as credenciais fornecidas
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(authenticationRequest.getUsername(), authenticationRequest.getPassword())
            );
        } catch (BadCredentialsException e) {
            throw new Exception("Nome de usuário ou senha incorretos", e);
        }

        // Se a autenticação foi bem-sucedida, carrega os detalhes do usuário
        final UserDetails userDetails = userDetailsService.loadUserByUsername(authenticationRequest.getUsername());

        // Gera o JWT
        final String jwt = jwtUtil.generateToken(userDetails);

        // Retorna o JWT na resposta
        Map<String, String> response = new HashMap<>();
        response.put("jwt", jwt);
        return ResponseEntity.ok(response);
    }

    // Classe interna para representar a requisição de autenticação (username e password)
    static class AuthenticationRequest {
        private String username;
        private String password;

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }
    }
}