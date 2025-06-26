package com.wasc.tarefa.controller.web;

import com.wasc.tarefa.model.Usuario;
import com.wasc.tarefa.service.UsuarioService; // Chamada direta ao serviço
import com.wasc.tarefa.security.JwtUtil; // Para gerar o JWT
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager; // Para autenticar
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder; // Para criptografar senha de registro
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class AuthController {

    @Autowired
    private AuthenticationManager authenticationManager; // Para autenticar o usuário

    @Autowired
    private UsuarioService usuarioService; // Para salvar novos usuários e buscar detalhes

    @Autowired
    private PasswordEncoder passwordEncoder; // Para criptografar senhas

    @Autowired
    private JwtUtil jwtUtil; // Para gerar o JWT após autenticação

    // Exibe a página de login
    @GetMapping("/login")
    public String showLoginForm(Model model) {
        // Limpa a mensagem de erro/sucesso após redirecionamento
        model.addAttribute("error", null);
        model.addAttribute("message", null);
        return "login";
    }

    // Processa o formulário de login
    @PostMapping("/login")
    public String login(@RequestParam String username, @RequestParam String password, HttpSession session, Model model) {
        try {
            // Autentica o usuário usando o AuthenticationManager do Spring Security
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(username, password)
            );

            // Se a autenticação foi bem-sucedida, carrega os detalhes do usuário para gerar o JWT
            UserDetails userDetails = usuarioService.findByNome(username)
                                                 // CORREÇÃO AQUI: u.getAtivo() mudado para u.isAtivo()
                                                 .map(u -> new org.springframework.security.core.userdetails.User(u.getNome(), u.getSenha(), u.isAtivo() ? java.util.Collections.emptyList() : java.util.Collections.emptyList())) // Simples
                                                 .orElseThrow(() -> new BadCredentialsException("Usuário não encontrado após autenticação."));


            final String jwt = jwtUtil.generateToken(userDetails); // Gera o JWT

            session.setAttribute("jwtToken", jwt); // Armazena o token na sessão HTTP do servidor
            session.setAttribute("loggedInUser", username); // Armazena o username para exibição

            model.addAttribute("message", "Login realizado com sucesso!");
            return "redirect:/tasks"; // Redireciona para a página de tarefas
        } catch (BadCredentialsException e) {
            model.addAttribute("error", "Nome de usuário ou senha inválidos.");
            return "login";
        } catch (Exception e) {
            model.addAttribute("error", "Erro ao tentar login: " + e.getMessage());
            return "login";
        }
    }

    // Exibe a página de registro
    @GetMapping("/register")
    public String showRegisterForm(Model model) {
        model.addAttribute("usuario", new Usuario());
        return "register";
    }

    // Processa o formulário de registro
    @PostMapping("/register")
    public String register(@ModelAttribute Usuario usuario, Model model, HttpSession session) {
        try {
            if (usuarioService.findByNome(usuario.getNome()).isPresent()) {
                model.addAttribute("error", "Nome de usuário já existe.");
                return "register";
            }
            usuario.setSenha(passwordEncoder.encode(usuario.getSenha())); // Criptografa a senha
            usuario.setAtivo(true); // Define como ativo por padrão
            usuarioService.save(usuario);
            model.addAttribute("message", "Usuário registrado com sucesso! Faça login.");
            return "login";
        } catch (Exception e) {
            model.addAttribute("error", "Erro ao registrar usuário: " + e.getMessage());
            return "register";
        }
    }

    // Logout
    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate(); // Invalida a sessão HTTP
        return "redirect:/login";
    }
}
