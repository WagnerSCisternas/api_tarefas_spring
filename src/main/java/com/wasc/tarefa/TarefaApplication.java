package com.wasc.tarefa;

import java.time.LocalDate;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.wasc.tarefa.model.Usuario;
import com.wasc.tarefa.repository.UsuarioRepository;

@SpringBootApplication
public class TarefaApplication {

	public static void main(String[] args) {
		SpringApplication.run(TarefaApplication.class, args);
	}
	
	@Bean
    public CommandLineRunner createDefaultUser(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        return args -> {
            String defaultUsername = "adminWasc";
            String defaultPassword = "Senha@Forte123";

            // Verifica se o usuário já existe no banco de dados
            if (usuarioRepository.findByNome(defaultUsername).isEmpty()) {
                Usuario adminUser = new Usuario();
                adminUser.setNome(defaultUsername);
                adminUser.setSenha(passwordEncoder.encode(defaultPassword));
                adminUser.setDataNascimento(LocalDate.of(1990, 1, 1)); // Usar LocalDate
                adminUser.setAtivo(true);

                usuarioRepository.save(adminUser);
                System.out.println("Usuário padrão '" + defaultUsername + "' criado com sucesso!");
            } else {
                System.out.println("Usuário padrão '" + defaultUsername + "' já existe.");
            }
        };
    }

}
