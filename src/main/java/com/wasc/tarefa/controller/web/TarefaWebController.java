package com.wasc.tarefa.controller.web;

import com.wasc.tarefa.model.Tarefa;
import com.wasc.tarefa.model.Usuario;
import com.wasc.tarefa.service.TarefaService; // Chamada direta ao serviço
import com.wasc.tarefa.service.UsuarioService; // Chamada direta ao serviço
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate; // Importar LocalDate
import java.util.List;
import java.util.Optional;

@Controller
public class TarefaWebController {

    @Autowired
    private TarefaService tarefaService;

    @Autowired
    private UsuarioService usuarioService;

    // Mapeamento da URL raiz para a página de tarefas (após login)
    @GetMapping("/")
    public String redirectToTasks() {
        return "redirect:/tasks";
    }

    // Exibe a lista de tarefas
    @GetMapping("/tasks")
    public String listTasks(Model model, HttpSession session) {
        // Verifica se há um token na sessão, se não, redireciona para login
        if (session.getAttribute("jwtToken") == null) {
            return "redirect:/login";
        }

        try {
            List<Tarefa> tarefas = tarefaService.findAll(); // Chama o serviço diretamente
            List<Usuario> usuarios = usuarioService.findAll(); // Chama o serviço diretamente

            model.addAttribute("tarefas", tarefas);
            model.addAttribute("usuarios", usuarios); // Para o dropdown de usuários
            model.addAttribute("tarefa", new Tarefa()); // Para o formulário de nova tarefa (vazio por padrão)
            model.addAttribute("loggedInUser", session.getAttribute("loggedInUser")); // Para exibir o user logado
            return "tasks";
        } catch (Exception e) {
            // Ocorreu um erro ao carregar dados, pode ser por falta de autenticação no nível da API
            // se o SecurityContext não estiver propagando corretamente para os services,
            // ou erro de banco de dados, etc.
            session.invalidate(); // Invalida a sessão para forçar novo login
            model.addAttribute("error", "Erro ao carregar dados ou sessão expirada. Faça login novamente.");
            return "login";
        }
    }

    // Adiciona ou atualiza uma tarefa
    @PostMapping("/tasks")
    public String saveTask(@ModelAttribute Tarefa tarefa, @RequestParam Long usuarioId, HttpSession session, Model model) {
        if (session.getAttribute("jwtToken") == null) {
            return "redirect:/login";
        }

        // Garante que o objeto Usuario na Tarefa tenha apenas o ID (para salvar)
        Usuario usuarioAssociado = new Usuario();
        usuarioAssociado.setId(usuarioId);
        tarefa.setUsuario(usuarioAssociado);

        try {
            // O serviço de tarefa validará a existência do usuário
            // A data que vem do formulário HTML já é LocalDate, não precisa formatar
            Tarefa savedTarefa = tarefaService.save(tarefa);
            model.addAttribute("message", "Tarefa salva com sucesso!");
        } catch (RuntimeException e) { // Exceções lançadas pelo TarefaService
            model.addAttribute("error", "Erro ao salvar tarefa: " + e.getMessage());
        } catch (Exception e) {
            model.addAttribute("error", "Erro inesperado ao salvar tarefa: " + e.getMessage());
        }

        return "redirect:/tasks";
    }

    // Pré-preenche o formulário para edição
    @GetMapping("/tasks/edit/{id}")
    public String editTask(@PathVariable Long id, Model model, HttpSession session) {
        if (session.getAttribute("jwtToken") == null) {
            return "redirect:/login";
        }

        try {
            Optional<Tarefa> tarefaToEdit = tarefaService.findById(id); // Busca a tarefa pelo ID
            List<Usuario> usuarios = usuarioService.findAll(); // Busca todos os usuários

            if (tarefaToEdit.isPresent()) {
                model.addAttribute("tarefa", tarefaToEdit.get());
                model.addAttribute("usuarios", usuarios);
                // model.addAttribute("tarefas", tarefaService.findAll()); // Não é estritamente necessário recarregar todas as tarefas aqui
                model.addAttribute("loggedInUser", session.getAttribute("loggedInUser"));
                return "tasks"; // Retorna para a mesma página com o formulário preenchido
            } else {
                model.addAttribute("error", "Tarefa não encontrada.");
                return "redirect:/tasks";
            }
        } catch (Exception e) {
            session.invalidate(); // Força logout em caso de erro de autenticação ou serviço
            model.addAttribute("error", "Erro ao carregar tarefa para edição ou sessão expirada. Faça login novamente.");
            return "login";
        }
    }

    // Exclui uma tarefa
    @GetMapping("/tasks/delete/{id}")
    public String deleteTask(@PathVariable Long id, HttpSession session, Model model) {
        if (session.getAttribute("jwtToken") == null) {
            return "redirect:/login";
        }

        try {
            boolean success = tarefaService.delete(id); // Chama o serviço diretamente
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