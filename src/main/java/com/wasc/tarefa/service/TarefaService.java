package com.wasc.tarefa.service;

import com.wasc.tarefa.model.Tarefa;
import com.wasc.tarefa.model.Usuario;
import com.wasc.tarefa.repository.TarefaRepository;
import com.wasc.tarefa.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;	

@Service
public class TarefaService {

    @Autowired
    private TarefaRepository tarefaRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    public List<Tarefa> findAll() {
        return tarefaRepository.findAll();
    }

    public Optional<Tarefa> findById(Long id) {
        return tarefaRepository.findById(id);	
    }

    public Tarefa save(Tarefa tarefa) {
        // Garante que o usuário associado à tarefa existe
        Optional<Usuario> usuario = usuarioRepository.findById(tarefa.getUsuario().getId());
        if (usuario.isPresent()) {
            tarefa.setUsuario(usuario.get());
            return tarefaRepository.save(tarefa);
        } else {
            // Lançar uma exceção ou lidar com o caso de usuário não encontrado
            throw new RuntimeException("Usuário não encontrado com o ID: " + tarefa.getUsuario().getId());
        }
    }

    public Tarefa update(Long id, Tarefa tarefaDetails) {
        Optional<Tarefa> optionalTarefa = tarefaRepository.findById(id);
        if (optionalTarefa.isPresent()) {
            Tarefa tarefa = optionalTarefa.get();
            tarefa.setDescricao(tarefaDetails.getDescricao());
            tarefa.setData(tarefaDetails.getData());
            tarefa.setStatus(tarefaDetails.isStatus());

            // Verifica se o usuário da tarefaDetails é válido
            Optional<Usuario> usuario = usuarioRepository.findById(tarefaDetails.getUsuario().getId());
            if (usuario.isPresent()) {
                tarefa.setUsuario(usuario.get());
                return tarefaRepository.save(tarefa);
            } else {
                throw new RuntimeException("Usuário não encontrado com o ID: " + tarefaDetails.getUsuario().getId());
            }
        } else {
            return null;
        }
    }

    public boolean delete(Long id) {
        if (tarefaRepository.existsById(id)) {
            tarefaRepository.deleteById(id);
            return true;
        }
        return false;
    }
}