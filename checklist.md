# Hoje A Festa É Nossa - Checklist do Projeto

## Visão Geral do Projeto

**Objetivo:** Plataforma onde usuários enviam fotos/vídeos para eventos específicos que são exibidos em um telão em tempo real.

**Stack:** Java 21 + Spring Boot 4.0.3 + PostgreSQL + Flyway + OCI Object Storage

---

## Funcionalidades Implementadas ✅

### Upload de Mídia
- [x] Upload de fotos (jpg, png, gif, webp, etc)
- [x] Upload de vídeos (mp4, mov, avi, etc)
- [x] Validação de tipo de arquivo (apenas image/* e video/*)
- [x] Validação de tamanho (fotos: máx 8MB, vídeos: máx 50MB)
- [x] Resolução automática do tipo de mídia (PHOTO/VIDEO)
- [x] Geração de chave de storage única
- [x] Upload para OCI Object Storage
- [x] Mensagem opcional do convidado

### Telão / Slideshow
- [x] Listagem de mídias visíveis para o telão
- [x] Ordenação por data (mais recentes primeiro)
- [x] Paginação configurável (page, size)
- [x] Retorno de URL pública para mídia

### Moderação
- [x] Endpoint para alterar visibilidade (aprovar/rejeitar)
- [x] Campo `is_visible` no banco de dados

### Eventos
- [x] Entidade Event com campos: name, accessToken, isPublicAlbum, startedAt, expiredAt
- [x] Busca de evento por token

### Infraestrutura
- [x] Configuração para PostgreSQL
- [x] Configuração para OCI Object Storage
- [x] Flyway migrations
- [x] Tratamento de exceções global padronizado

---

## Endpoints Disponíveis

| Método | Endpoint | Descrição |
|--------|----------|-----------|
| POST | `/uploads/events/{eventToken}` | Enviar foto/vídeo para um evento |
| GET | `/uploads/events/{eventToken}/slideshow?page=0&size=50` | Listar mídias visíveis para o telão |
| PUT | `/uploads/{uploadId}/visibility` | Alterar visibilidade de uma mídia |
| GET | `/events/{token}` | Buscar informações de um evento |

---

## Funcionalidades Pendentes

### Eventos
- [ ] CRUD completo de eventos (POST, GET, PUT, DELETE)
- [ ] Geração automática de QR Code
- [ ] Validação de período do evento (startedAt/expiredAt)
- [ ] Listagem de eventos

### Upload e Mídia
- [ ] Delete de upload
- [ ] Lista completa de uploads (para moderação)
- [ ] Compressão de imagens
- [ ] Thumbnails

### Telão / Slideshow
- [ ] WebSocket para atualização em tempo real

### Segurança
- [ ] Autenticação
- [ ] Autorização (proteger endpoints de moderação)
- [ ] Rate limiting

### Outros
- [ ] Testes unitários
- [ ] Estatísticas de uso

---

## Próximos Passos Recomendados

### Fase 1 - Gestão de Eventos (Alta Prioridade)
1. Implementar POST /events para criar eventos
2. Implementar validação de período (verificar se evento está ativo)
3. Adicionar seed de dados para testes

### Fase 2 - Segurança (Média Prioridade)
1. Adicionar autenticação básica
2. Proteger endpoints de moderação

### Fase 3 - Melhorias (Baixa Prioridade)
1. Adicionar WebSocket para telão em tempo real
2. Implementar testes unitários
3. Adicionar compressão de imagens

---

## Estrutura do Projeto

```
src/main/java/com/rafaellima/hojeafestaenossa/
├── event/
│   ├── application/
│   │   └── FindEventByTokenService.java
│   ├── domain/
│   │   └── Event.java
│   └── repository/
│       └── EventRepository.java
├── upload/
│   ├── application/
│   │   ├── ListSlideshowUploadsService.java
│   │   ├── ModerationService.java
│   │   └── UploadMediaService.java
│   ├── domain/
│   │   ├── MediaType.java
│   │   └── Upload.java
│   ├── repository/
│   │   └── UploadRepository.java
│   └── web/
│       ├── ModerationController.java
│       ├── SlideshowItemResponse.java
│       ├── UploadController.java
│       └── VisibilityRequest.java
├── infra/
│   └── storage/
│       ├── StorageService.java
│       └── oci/
│           └── OciObjectStorageService.java
└── shared/
    ├── config/
    └── exception/
        ├── BusinessException.java
        ├── ErrorResponse.java
        ├── ExceptionCustomized.java
        ├── GlobalExceptionHandling.java
        ├── MaxUploadSizeExceededException.java
        ├── NotFoundException.java
        └── TechnicalException.java
```

---

## Modelo de Dados

### Tabela: events
| Campo | Tipo | Descrição |
|-------|------|-----------|
| id | UUID | PK |
| name | VARCHAR(150) | Nome do evento |
| access_token | VARCHAR(120) | Token único para acesso |
| is_public | BOOLEAN | Visibilidade |
| started_at | TIMESTAMPTZ | Data de início |
| expired_at | TIMESTAMPTZ | Data de expiração |
| created_at | TIMESTAMPTZ | Data de criação |

### Tabela: uploads
| Campo | Tipo | Descrição |
|-------|------|-----------|
| id | UUID | PK |
| event_id | UUID | FK para events |
| media_type | VARCHAR(10) | PHOTO ou VIDEO |
| storage_key | TEXT | Caminho no OCI |
| original_name | TEXT | Nome original |
| file_size | BIGINT | Tamanho em bytes |
| message | TEXT | Mensagem opcional |
| is_visible | BOOLEAN | Visibilidade no telão |
| url | TEXT | URL pública |
| created_at | TIMESTAMPTZ | Data de upload |

---

Última atualização: 2026-03-02
