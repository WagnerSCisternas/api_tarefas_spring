package com.wasc.tarefa.service;

import com.wasc.tarefa.model.Usuario;
import com.wasc.tarefa.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UsuarioService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    public List<Usuario> findAll() {
        return usuarioRepository.findAll();
    }

    public Optional<Usuario> findById(Long id) {
        return usuarioRepository.findById(id);
    }

    public Optional<Usuario> findByNome(String nome) {
        return usuarioRepository.findByNome(nome);
    }

    public Usuario save(Usuario usuario) {
        return usuarioRepository.save(usuario);
    }

    public Usuario update(Long id, Usuario usuarioDetails) {
        Optional<Usuario> optionalUsuario = usuarioRepository.findById(id);
        if (optionalUsuario.isPresent()) {
            Usuario usuario = optionalUsuario.get();
            usuario.setNome(usuarioDetails.getNome());
            // A senha não é atualizada aqui para evitar sobrescrever a criptografada
            usuario.setDataNascimento(usuarioDetails.getDataNascimento());
            usuario.setAtivo(usuarioDetails.isAtivo());
            return usuarioRepository.save(usuario);
        } else {
            return null; // Ou lançar uma exceção de "não encontrado"
        }
    }

    public boolean delete(Long id) {
        if (usuarioRepository.existsById(id)) {
            usuarioRepository.deleteById(id);
            return true;
        }
        return false;
    }
}