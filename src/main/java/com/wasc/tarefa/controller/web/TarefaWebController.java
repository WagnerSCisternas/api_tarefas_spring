package com.wasc.tarefa.controller.web;

import com.wasc.tarefa.model.Tarefa;
import com.wasc.tarefa.model.Usuario;
import com.wasc.tarefa.service.TarefaService;
import com.wasc.tarefa.service.UsuarioService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Controller
public class TarefaWebController {

    @Autowired
    private TarefaService tarefaService;

    @Autowired
    private UsuarioService usuarioService;

    @GetMapping("/")
    public String redirectToTasks() {
        return "redirect:/tasks";
    }

    @GetMapping("/tasks")
    public String listTasks(Model model, HttpSession session) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
            System.out.println("DEBUG: Usuário NÃO autenticado ou sessão nula (verificado pelo SecurityContextHolder). Redirecionando.");
            session.invalidate();
            return "redirect:/login";
        }
        
        String username = authentication.getName();
        System.out.println("DEBUG: Usuário autenticado: " + username);

        // Remover o try-catch TEMPORARIAMENTE para ver a exceção real
        List<Tarefa> tarefas = tarefaService.findAll(); 
        List<Usuario> usuarios = usuarioService.findAll(); 

        model.addAttribute("tarefas", tarefas);
        model.addAttribute("usuarios", usuarios);
        model.addAttribute("tarefa", new Tarefa()); 
        model.addAttribute("loggedInUser", username); 

        System.out.println("DEBUG: Tarefas no modelo: " + (tarefas != null ? tarefas.size() : "null"));
        System.out.println("DEBUG: Usuários no modelo: " + (usuarios != null ? usuarios.size() : "null"));
        System.out.println("DEBUG: Tarefa no modelo (para formulário): " + (model.getAttribute("tarefa") != null ? "presente" : "null"));
        System.out.println("DEBUG: loggedInUser no modelo: " + model.getAttribute("loggedInUser"));

        return "tasks";
    }

    @PostMapping("/tasks")
    public String saveTask(@ModelAttribute Tarefa tarefa, @RequestParam Long usuarioId, HttpSession session, Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
            session.invalidate();
            return "redirect:/login";
        }

        Usuario usuarioAssociado = new Usuario();
        usuarioAssociado.setId(usuarioId);
        tarefa.setUsuario(usuarioAssociado);

        try {
            Tarefa savedTarefa = tarefaService.save(tarefa);
            model.addAttribute("message", "Tarefa salva com sucesso!");
        } catch (RuntimeException e) { 
            model.addAttribute("error", "Erro ao salvar tarefa: " + e.getMessage());
        } catch (Exception e) {
            model.addAttribute("error", "Erro inesperado ao salvar tarefa: " + e.getMessage());
        }

        return "redirect:/tasks";
    }

    // Pré-preenche o formulário para edição
    @GetMapping("/tasks/edit/{id}")
    public String editTask(@PathVariable Long id, Model model, HttpSession session) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
            session.invalidate();
            return "redirect:/login";
        }

        try {
            Optional<Tarefa> tarefaToEdit = tarefaService.findById(id); 
            // CARREGAR A LISTA COMPLETA DE TAREFAS AQUI TAMBÉM
            List<Tarefa> todasAsTarefas = tarefaService.findAll(); // <--- Adicione esta linha
            List<Usuario> usuarios = usuarioService.findAll(); 

            if (tarefaToEdit.isPresent()) {
                model.addAttribute("tarefa", tarefaToEdit.get());
                model.addAttribute("usuarios", usuarios);
                model.addAttribute("tarefas", todasAsTarefas); // <--- Adicione a lista completa ao modelo
                model.addAttribute("loggedInUser", authentication.getName());
                return "tasks"; 
            } else {
                model.addAttribute("error", "Tarefa não encontrada.");
                return "redirect:/tasks";
            }
        } catch (Exception e) {
            e.printStackTrace(); // Para depurar se houver erro aqui
            session.invalidate();
            model.addAttribute("error", "Erro ao carregar tarefa para edição ou sessão expirada. Faça login novamente.");
            return "login";
        }
    }

    // Exclui uma tarefa
    @GetMapping("/tasks/delete/{id}")
    public String deleteTask(@PathVariable Long id, HttpSession session, Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
            session.invalidate();
            return "redirect:/login";
        }

        try {
            boolean success = tarefaService.delete(id); 
            if (success) {
                model.addAttribute("message", "Tarefa excluída com sucesso!");
            } else {
                model.addAttribute("error", "Erro ao excluir tarefa.");
            }
        } catch (Exception e) {
            session.invalidate();
            model.addAttribute("error", "Erro ao excluir tarefa ou sessão expirada. Faça login novamente.");
            return "login";
        }

        return "redirect:/tasks";
    }
}
