package com.wasc.tarefa.repository;


import com.wasc.tarefa.model.Tarefa;
import com.wasc.tarefa.model.Usuario;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TarefaRepository extends JpaRepository<Tarefa, Long> {
	List<Tarefa> findByUsuario(Usuario usuario); 
}