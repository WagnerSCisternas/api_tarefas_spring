package com.wasc.tarefa.service;


import com.wasc.tarefa.model.Usuario;
import com.wasc.tarefa.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Optional;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // Busca o usuário pelo nome (que será o username)
        // Você precisará adicionar um método findByNome(String nome) no seu UsuarioRepository
        Optional<Usuario> usuarioOptional = usuarioRepository.findByNome(username);

        if (usuarioOptional.isEmpty()) {
            throw new UsernameNotFoundException("Usuário não encontrado com o nome: " + username);
        }

        Usuario usuario = usuarioOptional.get();

        // Retorna um objeto UserDetails do Spring Security	
        // Por enquanto, sem papéis (roles), apenas um ArrayList vazio
        return new User(usuario.getNome(), usuario.getSenha(), new ArrayList<>());
    }
}