package com.wasc.tarefa.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor // Gera um construtor sem argumentos
@AllArgsConstructor
public class LoginRequest {
    private String username;
    private String password;
}
