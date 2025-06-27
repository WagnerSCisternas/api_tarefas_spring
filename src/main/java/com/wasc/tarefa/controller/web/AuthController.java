package com.wasc.tarefa.controller.web;

import com.wasc.tarefa.model.Usuario;
import com.wasc.tarefa.service.UsuarioService; // Chamada direta ao serviço
import com.wasc.tarefa.security.JwtUtil; // Para gerar o JWT (se necessário para outros fins)
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder; // Mantenha para o registro

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam; // Mantenha se ainda usar em outros métodos


@Controller
public class AuthController {

    // A injeção de AuthenticationManager NÃO é mais necessária aqui, pois o Spring Security lida com o formLogin()
    // @Autowired
    // private AuthenticationManager authenticationManager; 

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private PasswordEncoder passwordEncoder; // Mantenha para o registro de usuário

    @Autowired
    private JwtUtil jwtUtil; // Mantenha, caso precise gerar JWT para API calls via servidor após o login web


    // Exibe a página de login
    @GetMapping("/login")
    public String showLoginForm(Model model, @RequestParam(value = "error", required = false) String error,
                                @RequestParam(value = "logout", required = false) String logout) {
        // Estas mensagens são passadas por Spring Security em caso de falha ou sucesso de logout
        if (error != null) {
            model.addAttribute("error", "Nome de usuário ou senha inválidos.");
        }
        if (logout != null) {
            model.addAttribute("message", "Você foi desconectado com sucesso.");
        }
        return "login";
    }

    // REMOVIDO: O método @PostMapping("/login") foi REMOVIDO.
    // O Spring Security agora lida com o POST para /login automaticamente via formLogin().
    /*
    @PostMapping("/login")
    public String login(@RequestParam String username, @RequestParam String password, HttpSession session, Model model) {
        // Este código não é mais necessário aqui, pois o Spring Security o gerencia internamente.
        // O defaultSuccessUrl() e failureUrl() do formLogin() já cuidarão do redirecionamento.
    }
    */


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
            return "login"; // Redireciona para a página de login
        } catch (Exception e) {
            model.addAttribute("error", "Erro ao registrar usuário: " + e.getMessage());
            return "register";
        }
    }

    // Logout (Continua o mesmo, mas o Spring Security o intercepta)
    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate(); // Invalida a sessão HTTP
        return "redirect:/login"; // Será substituído pelo logoutSuccessUrl do Spring Security
    }
}