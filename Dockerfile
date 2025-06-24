# Estágio de construção (Build Stage)
# Usa uma imagem base com o Java SDK e Maven para compilar o projeto.
# 'maven:3.9.6-eclipse-temurin-21-alpine' já contém Maven e OpenJDK 21 em uma base Alpine (leve).
FROM maven:3.9.6-eclipse-temurin-21-alpine AS build

# Define o diretório de trabalho dentro do contêiner Docker para o estágio de construção.
WORKDIR /app

# Copia o arquivo pom.xml primeiro para que o Docker possa usar o cache de camada.
# Se o pom.xml não mudar, o Maven não precisa baixar as dependências novamente.
COPY pom.xml .

# Baixa as dependências do projeto. Isso garante que as dependências estejam no cache do Maven.
# 'dependency:go-offline' é útil para garantir que todas as dependências transitivas são resolvidas.
RUN mvn dependency:go-offline

# Copia o restante do código-fonte da aplicação para o diretório de trabalho.
COPY src ./src

# Compila o projeto Spring Boot e empacota-o em um JAR executável.
# O flag '-DskipTests' é adicionado para PULAR a execução dos testes durante a construção da imagem Docker.
# Isso evita falhas de build causadas por problemas nos testes em ambientes de construção.
RUN mvn clean install -DskipTests

# Estágio de execução (Runtime Stage)
# Usa uma imagem base leve com apenas o Java Runtime Environment (JRE).
# 'eclipse-temurin:21-jre-alpine' fornece o JRE 21 em uma base Alpine, ideal para execução de aplicativos.
FROM eclipse-temurin:21-jre-alpine

# Define o diretório de trabalho para o estágio de execução.
WORKDIR /app

# Copia o JAR executável gerado no estágio de construção para o estágio de execução.
# O nome do JAR é geralmente <artifactId>-<version>.jar.
# **IMPORTANTE**: Verifique o nome exato do seu arquivo .jar na pasta 'target/'
# do seu projeto local (após você rodar 'mvn clean install -DskipTests').
# Renomeamos para 'app.jar' dentro do contêiner para simplicidade.
# COPY --from=build /app/target/api-tarefas-0.0.1-SNAPSHOT.jar app.jar
COPY --from=build /app/target/Tarefas-0.0.1-SNAPSHOT.jar app.jar


# Expõe a porta que a aplicação Spring Boot usará (padrão 8080).
EXPOSE 8080

# Define o comando que será executado quando o contêiner Docker for iniciado.
# Este comando inicia sua aplicação Spring Boot.
ENTRYPOINT ["java", "-jar", "app.jar"]

# --- Dicas Adicionais ---
# 1. Se você tiver arquivos de configuração específicos para o ambiente Docker,
#    eles devem ser injetados via variáveis de ambiente ou volumes, NUNCA hardcoded no Dockerfile.
# 2. A variável JWT_SECRET e credenciais de banco de dados (username, password, URL)
#    serão passadas como variáveis de ambiente no 'docker run' ou na plataforma de deploy (Railway, Render, etc.).