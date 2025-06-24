package com.wasc.tarefa.controller;

import com.wasc.tarefa.model.Tarefa;
import com.wasc.tarefa.service.TarefaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*; // Adicione esta importação para @CrossOrigin

import java.util.List;

@RestController
@RequestMapping("/api/tarefas")
@CrossOrigin // Adicionado para permitir requisições do frontend
public class TarefaController {

    @Autowired
    private TarefaService tarefaService;

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

    @PostMapping
    public ResponseEntity<Tarefa> createTarefa(@RequestBody Tarefa tarefa) {
        try {
            Tarefa savedTarefa = tarefaService.save(tarefa);
            return ResponseEntity.status(HttpStatus.CREATED).body(savedTarefa);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(null); // Tratar erro de usuário não encontrado
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<Tarefa> updateTarefa(@PathVariable Long id, @RequestBody Tarefa tarefaDetails) {
        try {
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