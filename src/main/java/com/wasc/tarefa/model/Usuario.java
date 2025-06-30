package com.wasc.tarefa.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDate;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Usuario {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String nome; // Será usado como username

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String senha; // Campo para a senha

    @Column(nullable = false)
    private LocalDate dataNascimento;

    @Column(nullable = false, unique = true) // NOVO CAMPO: Email (obrigatório e único)
    private String email; 
    
    private boolean ativo;

    @JsonIgnore // Essencial para evitar recursão infinita na serialização JSON
    @OneToMany(mappedBy = "usuario", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Tarefa> tarefas;
    
}