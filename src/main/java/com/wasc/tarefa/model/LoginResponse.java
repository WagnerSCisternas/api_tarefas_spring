package com.wasc.tarefa.model;

import lombok.Data;
import lombok.AllArgsConstructor; // NOVO: Importar AllArgsConstructor
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor // NOVO: Gera um construtor com todos os campos (neste caso, 'jwt')
@NoArgsConstructor 
public class LoginResponse {
    private String jwt;
}