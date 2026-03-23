# Build stage
FROM maven:3.9.9-eclipse-temurin-21 AS builder

WORKDIR /app

# Copiar arquivos de configuração
COPY pom.xml .
COPY .mvn .mvn
COPY mvnw .

# Baixar dependências (cache layer)
RUN ./mvnw dependency:go-offline -B

# Copiar código fonte e compilar
COPY src ./src
RUN ./mvnw clean package -DskipTests -B

# Extrair layers para cache eficiente do Docker
RUN java -Djarmode=layertools -jar target/*.jar extract

# Runtime stage
FROM eclipse-temurin:21-jre-alpine

# Instalar curl para health check (Dokploy padrão)
RUN apk add --no-cache curl

WORKDIR /app

# Criar usuário não-root para segurança
RUN addgroup -S appgroup && adduser -S appuser -G appgroup

# Copiar layers extraídos (cache eficiente)
COPY --from=builder /app/dependencies/ ./
COPY --from=builder /app/spring-boot-loader/ ./
COPY --from=builder /app/snapshot-dependencies/ ./
COPY --from=builder /app/application/ ./

# Criar diretório para uploads temporários (se necessário)
RUN mkdir -p /app/uploads && chown -R appuser:appgroup /app

# Mudar para usuário não-root
USER appuser

# Expor porta
EXPOSE 8080

# Health check com curl (padrão Dokploy)
# start-period=120s para VPS de 1GB (startup pode levar 100s+)
# timeout=10s para evitar falso negativo em VPS lenta
HEALTHCHECK --interval=30s --timeout=10s --start-period=120s --retries=3 \
    CMD curl -f http://localhost:8080/actuator/health || exit 1

# Executar aplicação com otimizações JVM para container
# MaxRAMPercentage=60% para VPS de 1GB (evita OOM Kill)
# G1GC é recomendado pelo Spring Boot para produção
# UseStringDeduplication economiza ~10-15% de heap
ENTRYPOINT ["java", "-XX:+UseG1GC", "-XX:MaxRAMPercentage=60.0", "-XX:G1HeapRegionSize=4m", "-XX:+UseStringDeduplication", "org.springframework.boot.loader.launch.JarLauncher"]
