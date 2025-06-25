package com.wasc.tarefa;

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
            // Define os dados do usuário padrão
            String defaultUsername = "admin";
            String defaultPassword = "Senha@Forte123";

            // Verifica se o usuário já existe no banco de dados
            if (usuarioRepository.findByNome(defaultUsername).isEmpty()) {
                // Cria uma nova instância de Usuario	
                Usuario adminUser = new Usuario();
                adminUser.setNome(defaultUsername);
                adminUser.setSenha(passwordEncoder.encode(defaultPassword)); // Criptografa a senha
                adminUser.setDataNascimento(java.time.LocalDate.of(1990, 1, 1)); // Data de nascimento padrão
                adminUser.setAtivo(true); // Define o usuário como ativo

                // Salva o usuário no banco de dados
                usuarioRepository.save(adminUser);

                System.out.println("Usuário padrão '" + defaultUsername + "' criado com sucesso!");
            } else {
                System.out.println("Usuário padrão '" + defaultUsername + "' já existe.");
            }
        };
    }

}
