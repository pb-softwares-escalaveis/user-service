# Etapa 1: Construção
FROM eclipse-temurin:25-jdk-alpine AS builder
WORKDIR /app
COPY . .
# Compila o projeto ignorando os testes por ora
RUN ./mvnw clean package -DskipTests

# Etapa 2: Imagem final de execução
FROM eclipse-temurin:25-jre-alpine
WORKDIR /app

# Cria um grupo e um usuário de sistema sem privilégios para rodar a aplicação
RUN addgroup -S appgroup && adduser -S appuser -G appgroup

# Copia e muda as permissões para o usuário que acabamos de criar
COPY --from=builder --chown=appuser:appgroup /app/target/*.jar app.jar

# Troca do usuário root (padrão) para o appuser
USER appuser

# Executa a aplicação
ENTRYPOINT ["java", "-jar", "app.jar"]