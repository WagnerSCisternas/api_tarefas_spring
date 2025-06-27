# Estágio de construção (Build Stage)
FROM maven:3.9.6-eclipse-temurin-21-alpine AS build
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline
COPY src ./src
RUN mvn clean install -DskipTests

# Estágio de execução (Runtime Stage)
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
# O nome do JAR é <artifactId>-<version>.jar
COPY --from=build /app/target/tarefa-0.0.1-SNAPSHOT.jar app.jar


EXPOSE 8080 # <--- CORRIGIDO AQUI: SEM NENHUM CARACTERE DE COMENTÁRIO '#' APÓS O NÚMERO DA PORTA

ENTRYPOINT ["java", "-jar", "app.jar"]