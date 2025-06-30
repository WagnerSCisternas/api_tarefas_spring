package com.wasc.tarefa.repository;

import com.wasc.tarefa.model.Usuario;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
	Optional<Usuario> findByNome(String nome); 
	Optional<Usuario> findByEmail(String email);
}

	


