# Hoje A Festa É Nossa

<p align="center">
  <img src="https://img.shields.io/badge/Java-21-blue" alt="Java Version">
  <img src="https://img.shields.io/badge/Spring%20Boot-4.0.3-green" alt="Spring Boot">
  <img src="https://img.shields.io/badge/License-MIT-yellow" alt="License">
</p>

---

## 🚀 Visão Geral do Projeto

**Hoje A Festa É Nossa** é uma plataforma que permite aos convidados de um evento enviar fotos e vídeos através de upload, que são exibidos em tempo real em um telão durante o evento.

Imagine uma festa de 15 anos, casamento ou formatura: os convidados escaneiam um QR Code, enviam suas fotos/vídeos e imediatamente veem suas mídias aparecendo no telão da festa!

---

## 💡 Problema que Resolve

### Desafio
- Criar um método prático para coletar fotos/vídeos de eventos
- Exibir conteúdo em tempo real em um telão
- Permitir que o anfitrião modere o conteúdo antes da exibição

### Solução
Uma API RESTful com:
- Upload de mídia com processamento assíncrono
- Sistema de moderação (aprovar/rejeitar)
- Exibição em tempo real via WebSocket
- QR Code para acesso fácil

---

## 🛠️ Tech Stack

| Tecnologia | Descrição |
|------------|-----------|
| **Java 21** | Linguagem de programação |
| **Spring Boot 4.0.3** | Framework principal |
| **Spring Web** | REST API |
| **Spring Data JPA** | Persistência de dados |
| **Spring WebSocket** | Comunicação em tempo real |
| **PostgreSQL** | Banco de dados |
| **Flyway** | Migrações de banco |
| **OCI Object Storage** | Armazenamento de mídias (Oracle Cloud) |
| **Thumbnailator** | Compressão de imagens e thumbnails |
| **Lombok** | Redução de boilerplate |

---

## 📋 Pré-requisitos

- Java 21+
- Maven 3.8+
- PostgreSQL 14+
- Oracle Cloud Infrastructure (OCI) - Object Storage

---

## ⚙️ Configuração

### Variáveis de Ambiente

```bash
# Banco de Dados
DATABASE_URL=jdbc:postgresql://localhost:5432/hojeafestaenossa
DATABASE_USERNAME=postgres
DATABASE_PASSWORD=postgres

# Oracle Cloud Infrastructure (OCI)
OCI_REGION=sa-saopaulo-1
OCI_TENANCY_ID=seu_tenancy_id
OCI_USER_ID=seu_user_id
OCI_FINGERPRINT=sua_fingerprint
OCI_PRIVATE_KEY_PATH=/caminho/para/chave_privada
OCI_NAMESPACE=seu_namespace
OCI_BUCKET_NAME=hojeafestaenossa

# Aplicação
APP_BASE_URL=https://hojeafestaenossa.com
```

### application.yml

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/hojeafestaenossa
    username: postgres
    password: postgres

oci:
  region: sa-saopaulo-1
  tenancy-id: ${OCI_TENANCY_ID}
  user-id: ${OCI_USER_ID}
  fingerprint: ${OCI_FINGERPRINT}
  private-key-path: ${OCI_PRIVATE_KEY_PATH}
  namespace: ${OCI_NAMESPACE}
  bucket-name: hojeafestaenossa

app:
  base-url: https://hojeafestaenossa.com
```

---

## 📦 Instalação

```bash
# Clonar o repositório
git clone https://github.com/rafaelclima/hojeafestaenossa.git

# Entrar no diretório
cd hojeafestaenossa

# Compilar o projeto
./mvnw clean package -DskipTests

# Executar
./mvnw spring-boot:run
```

---

## 🔌 Endpoints da API

### 📅 Eventos

| Método | Endpoint | Descrição | Autenticação |
|--------|----------|-----------|--------------|
| POST | `/events` | Criar novo evento | Não |
| GET | `/events` | Listar todos os eventos | Não |
| GET | `/events/{token}` | Buscar evento por token | Não |
| PUT | `/events/{token}` | Atualizar evento | ✅ X-Admin-Token |
| DELETE | `/events/{token}` | Excluir evento | ✅ X-Admin-Token |
| GET | `/events/{token}/stats` | Estatísticas do evento | ✅ X-Admin-Token |

**Exemplo - Criar Evento:**
```bash
curl -X POST http://localhost:8080/events \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Festa de 15 anos da Ana",
    "startedAt": "2026-03-15T19:00:00Z",
    "expiredAt": "2026-03-15T23:59:59Z",
    "isPublic": true
  }'
```

**Resposta:**
```json
{
  "id": "uuid-aqui",
  "name": "Festa de 15 anos da Ana",
  "accessToken": "token-publico",
  "adminToken": "admin123",
  "eventUrl": "https://hojeafestaenossa.com/events?eventId=token-publico"
}
```

---

### 📤 Upload de Mídia

| Método | Endpoint | Descrição | Autenticação |
|--------|----------|-----------|--------------|
| POST | `/uploads/events/{eventToken}` | Enviar foto/vídeo | Não |
| GET | `/uploads/events/{eventToken}/slideshow` | Listar para telão | Não |
| GET | `/uploads/events/{eventToken}/moderation` | Listar para moderação | ✅ X-Admin-Token |
| PUT | `/uploads/{uploadId}/visibility` | Aprovar/Rejeitar | ✅ X-Admin-Token |

**Exemplo - Upload de Foto:**
```bash
curl -X POST http://localhost:8080/uploads/events/{eventToken} \
  -H "Content-Type: multipart/form-data" \
  -F "file=@foto.jpg" \
  -F "message=Obrigado pela festa!"
```

**Exemplo - Moderar (Aprovar):**
```bash
curl -X PUT http://localhost:8080/uploads/{uploadId}/visibility \
  -H "Content-Type: application/json" \
  -H "X-Admin-Token: admin123" \
  -d '{"visible": true}'
```

---

## 🔗 WebSocket

O sistema usa WebSocket (STOMP) para atualização em tempo real do telão.

### Conexão

```
URL: ws://localhost:8080/ws
Topic: /topic/events/{eventToken}/slideshow
```

### Exemplo de Inscrição (JavaScript)

```javascript
const socket = new SockJS('/ws');
const stompClient = Stomp.over(socket);

stompClient.connect({}, function(frame) {
  stompClient.subscribe('/topic/events/TOKEN_DO_EVENTO/slideshow', function(message) {
    const media = JSON.parse(message.body);
    // Exibir mídia no telão
    console.log('Nova mídia:', media.url);
  });
});
```

### Formato da Mensagem

```json
{
  "url": "https://objectstorage.../foto.jpg",
  "mediaType": "PHOTO",
  "message": "Mensagem do convidado",
  "createdAt": "2026-03-15T20:30:00Z"
}
```

---

## 📱 Fluxo de Uso

### 1. Criação do Evento
O anfitrião cria o evento e recebe:
- `accessToken` - para convidados acessarem
- `adminToken` - para moderar o evento

### 2. Divulgação
O anfitrião compartilha o QR Code (gerado a partir do `eventUrl`) com os convidados.

### 3. Envio de Mídia
Convidado acessa a página, envia foto/vídeo com mensagem opcional.

### 4. Moderação
O anfitrião aprova/rejeita cada conteúdo enviado.

### 5. Exibição no Telão
Mídias aprovadas aparecem automaticamente no telão via WebSocket.

---

## 🧪 Testando a API

### Criar Evento
```bash
curl -X POST http://localhost:8080/events \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Teste Evento",
    "startedAt": "2026-03-20T18:00:00Z",
    "expiredAt": "2026-03-20T23:00:00Z"
  }'
```

### Buscar Evento
```bash
curl http://localhost:8080/events/{accessToken}
```

### Listar Slideshow
```bash
curl "http://localhost:8080/uploads/events/{eventToken}/slideshow?page=0&size=50"
```

### Moderar Upload
```bash
curl -X PUT http://localhost:8080/uploads/{uploadId}/visibility \
  -H "Content-Type: application/json" \
  -H "X-Admin-Token: SEU_ADMIN_TOKEN" \
  -d '{"visible": true}'
```

---

## 📂 Estrutura do Projeto

```
src/main/java/com/rafaellima/hojeafestaenossa/
├── event/
│   ├── application/       # Lógica de negócio
│   ├── domain/           # Entidades
│   ├── repository/       # Repositórios JPA
│   └── web/              # Controllers e DTOs
├── upload/
│   ├── application/      # Lógica de negócio
│   ├── domain/           # Entidades
│   ├── repository/       # Repositórios JPA
│   └── web/              # Controllers e DTOs
├── infra/
│   └── storage/          # Integração OCI
└── shared/
    ├── config/            # Configurações
    └── exception/        # Tratamento de exceções
```

---

## 🤝 Contribuição

1. Fork o projeto
2. Crie uma branch (`git checkout -b feature/nova-feature`)
3. Commit suas mudanças (`git commit -m 'Add nova feature'`)
4. Push para a branch (`git push origin feature/nova-feature`)
5. Abra um Pull Request

---

## 📄 Licença

Este projeto está sob a licença MIT. Veja o arquivo LICENSE para mais detalhes.

---

## 👤 Autor

**Rafael Lima**
- GitHub: [@rafaelclima](https://github.com/rafaelclima)

---

## 🔗 Links Úteis

- [Documentação Spring Boot](https://spring.io/projects/spring-boot)
- [OCI Object Storage](https://docs.oracle.com/pt-br/iaas/Content/Object/introobjectstorage.htm)
- [STOMP over WebSocket](https://stomp.github.io/)

---

<p align="center">
  Feido com ❤️ para trazer alegria aos eventos! 🎉
</p>
