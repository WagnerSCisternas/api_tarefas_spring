package com.wasc.tarefa.controller.api;

import com.wasc.tarefa.model.Tarefa;
import com.wasc.tarefa.model.Usuario;
import com.wasc.tarefa.service.TarefaService;
import com.wasc.tarefa.service.UsuarioService; // Ainda necessário para validação
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Hidden; // Importe Hidden se quiser ocultar no Swagger

import java.util.List;

@RestController
@RequestMapping("/api/tarefas") // Prefixo para diferenciar da web
@Hidden // Oculta no Swagger UI se for para uso interno/avançado
public class TarefaController {

    @Autowired
    private TarefaService tarefaService;

    // UsuarioService aqui é usado para validação, se necessário
    @Autowired
    private UsuarioService usuarioService;

    @GetMapping
    public List<Tarefa> getAllTarefas() {
        return tarefaService.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Tarefa> getTarefaById(@PathVariable Long id) {
        return tarefaService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/usuario/{userId}")
    public ResponseEntity<List<Tarefa>> getTarefasByUsuario(@PathVariable Long userId) {
        try {
            List<Tarefa> tarefas = tarefaService.findByUsuario(userId);
            return ResponseEntity.ok(tarefas);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build(); // Usuário não encontrado
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @PostMapping
    public ResponseEntity<Tarefa> createTarefa(@RequestBody Tarefa tarefa) {
        try {
            // Se o usuário vem apenas com ID, precisamos buscar a entidade completa
            if (tarefa.getUsuario() != null && tarefa.getUsuario().getId() != null) {
                usuarioService.findById(tarefa.getUsuario().getId())
                        .orElseThrow(() -> new RuntimeException("Usuário não encontrado com o ID: " + tarefa.getUsuario().getId()));
            } else {
                 throw new RuntimeException("Usuário associado à tarefa é obrigatório.");
            }
            Tarefa savedTarefa = tarefaService.save(tarefa);
            return ResponseEntity.status(HttpStatus.CREATED).body(savedTarefa);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(null); // Tratar erro de usuário não encontrado
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<Tarefa> updateTarefa(@PathVariable Long id, @RequestBody Tarefa tarefaDetails) {
        try {
            // Valida o usuário associado, se alterado
            if (tarefaDetails.getUsuario() != null && tarefaDetails.getUsuario().getId() != null) {
                usuarioService.findById(tarefaDetails.getUsuario().getId())
                        .orElseThrow(() -> new RuntimeException("Usuário não encontrado com o ID: " + tarefaDetails.getUsuario().getId()));
            } else {
                throw new RuntimeException("Usuário associado à tarefa é obrigatório.");
            }
            Tarefa updatedTarefa = tarefaService.update(id, tarefaDetails);
            if (updatedTarefa != null) {
                return ResponseEntity.ok(updatedTarefa);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(null); // Tratar erro de usuário não encontrado
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTarefa(@PathVariable Long id) {
        if (tarefaService.delete(id)) {
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}