# 🚀 Guia de Deploy em Produção

## Pré-requisitos

- Servidor VPS com Docker e Docker Compose instalados
- Dockploy instalado (opcional, para gerenciamento)
- Domínio configurado (ex: `hojeafestaenossa.site`)
- Bucket OCI `hojeafestaenossa` criado na região `sa-saopaulo-1`

---

## 📋 Estrutura de Arquivos

```
hojeafestaenossa/
├── Dockerfile
├── docker-compose.yml
├── .env.production.example
├── .env (não versionado - criar no servidor)
├── oci-key/
│   └── api_key.pem (chave privada OCI)
└── src/
```

---

## 🔧 Passo a Passo

### 1. Preparar o Servidor

```bash
# Acessar o servidor
ssh usuario@seu-servidor

# Criar diretório da aplicação
mkdir -p /opt/hojeafestaenossa
cd /opt/hojeafestaenossa
```

### 2. Configurar Arquivos

```bash
# Copiar .env.example e editar
cp .env.production.example .env
nano .env

# Preencher variáveis:
# - DB_PASSWORD
# - OCI_TENANCY_ID
# - OCI_USER_ID
# - OCI_FINGERPRINT
# - OCI_NAMESPACE
# - APP_BASE_URL
```

### 3. Configurar Chave OCI

```bash
# Criar pasta da chave OCI
mkdir -p oci-key

# Copiar chave privada do seu computador para o servidor
# No seu computador local:
scp /caminho/para/api_key.pem usuario@servidor:/opt/hojeafestaenossa/oci-key/

# No servidor, ajustar permissões
chmod 600 oci-key/api_key.pem
chown -R 1000:1000 oci-key/
```

### 4. Deploy com Docker Compose

```bash
# Build da imagem
docker compose build

# Iniciar aplicação
docker compose up -d

# Verificar logs
docker compose logs -f app
```

### 5. Verificar Saúde

```bash
# Health check
curl http://localhost:8080/actuator/health

# Verificar containers
docker compose ps
```

---

## 🐳 Deploy com Dockploy

Se estiver usando Dockploy para gerenciar:

### 1. No Dockploy UI

1. Acesse `http://seu-servidor:3000`
2. Clique em **Add Service**
3. Selecione **Docker Compose**
4. Cole o conteúdo do `docker-compose.yml`
5. Adicione as variáveis de ambiente do `.env`
6. Clique em **Deploy**

### 2. Montar Volumes no Dockploy

No Dockploy, configure os volumes:

```
./oci-key:/app/oci-key:ro
./logs:/app/logs
```

---

## 🗄️ PostgreSQL Separado (Dockploy)

Se for usar PostgreSQL gerenciado pelo Dockploy:

### 1. Criar PostgreSQL no Dockploy

1. No Dockploy UI, clique em **Add Service**
2. Selecione **PostgreSQL**
3. Configure:
   - Database: `hojeafestaenossa`
   - User: `postgres`
   - Password: `senha_forte`
4. Deploy

### 2. Atualizar docker-compose.yml

Comente ou remova o serviço `postgres` do `docker-compose.yml`:

```yaml
# postgres:
#   image: postgres:16-alpine
#   ...
```

### 3. Configurar Connection String

No `.env` ou variáveis do Dockploy:

```bash
SPRING_DATASOURCE_URL=jdbc:postgresql://postgres-container-name:5432/hojeafestaenossa
SPRING_DATASOURCE_USERNAME=postgres
SPRING_DATASOURCE_PASSWORD=senha_forte
```

---

## 🔒 Segurança

### Firewall

```bash
# Liberar apenas portas necessárias
ufw allow 22/tcp    # SSH
ufw allow 80/tcp    # HTTP (para Let's Encrypt)
ufw allow 443/tcp   # HTTPS
ufw deny 5432/tcp   # PostgreSQL (apenas interno)
ufw enable
```

### Permissões da Chave OCI

```bash
chmod 600 /opt/hojeafestaenossa/oci-key/api_key.pem
chown -R 1000:1000 /opt/hojeafestaenossa/oci-key/
```

### Variáveis Sensíveis

- NUNCA faça commit do arquivo `.env`
- Use permissões restritas: `chmod 600 .env`
- Rotacione credenciais periodicamente

---

## 📊 Logs

```bash
# Logs da aplicação
docker compose logs -f app

# Logs do PostgreSQL
docker compose logs -f postgres

# Logs em tempo real com filtro
docker compose logs -f app | grep -i error
```

---

## 🔄 Atualizar Aplicação

```bash
# Acessar servidor
cd /opt/hojeafestaenossa

# Pull das mudanças (se usar git)
git pull origin main

# Rebuild e restart
docker compose build app
docker compose up -d app

# Verificar
docker compose ps
docker compose logs -f app
```

---

## 🐛 Troubleshooting

### Aplicação não inicia

```bash
# Verificar logs
docker compose logs app

# Verificar se porta está em uso
netstat -tulpn | grep 8080

# Verificar memória disponível
free -h
```

### Erro de conexão com banco

```bash
# Verificar se PostgreSQL está up
docker compose ps postgres

# Testar conexão
docker compose exec app wget --spider postgres:5432

# Verificar logs do postgres
docker compose logs postgres
```

### Erro OCI Object Storage

```bash
# Verificar se chave foi montada
docker compose exec app ls -la /app/oci-key/

# Verificar variáveis de ambiente
docker compose exec app env | grep OCI

# Testar conexão com OCI
docker compose exec app curl -I https://objectstorage.sa-saopaulo-1.oraclecloud.com
```

---

## 📈 Monitoramento

### Health Check Endpoint

```bash
curl http://localhost:8080/actuator/health
```

### Métricas (se habilitado)

```bash
curl http://localhost:8080/actuator/metrics
```

### Logs de Erro

```bash
docker compose logs app | grep -i error | tail -50
```

---

## 🎯 Checklist de Deploy

- [ ] `.env` configurado com todas as variáveis
- [ ] Chave OCI copiada para `oci-key/api_key.pem`
- [ ] Permissões da chave OCI ajustadas (600)
- [ ] PostgreSQL configurado (local ou Dockploy)
- [ ] Firewall configurado (portas 22, 80, 443)
- [ ] Docker Compose buildado e rodando
- [ ] Health check respondendo
- [ ] Logs verificados sem erros críticos
- [ ] Upload de arquivo testado
- [ ] Moderação testada
- [ ] Telão (WebSocket) testado

---

## 📚 Referências

- [Docker Compose Documentation](https://docs.docker.com/compose/)
- [Spring Boot Production Guide](https://docs.spring.io/spring-boot/docs/current/reference/html/production-features.html)
- [OCI SDK Java Documentation](https://docs.oracle.com/en-us/iaas/Content/API/SDKDocs/javasdk.htm)

---

**Última atualização:** 12 de março de 2026  
**Versão:** 0.0.1-SNAPSHOT
