# Hoje A Festa É Nossa --- Documento de Arquitetura e Planejamento

## 1. Visão Geral do Projeto

**Objetivo:**\
Construir uma plataforma onde usuários possam enviar fotos e vídeos
curtos (até 30 segundos) para um evento específico através de upload, e
esse conteúdo seja exibido em tempo real em um telão.

### Fluxo Principal

1.  Usuário acessa página do evento.
2.  Realiza upload de foto/vídeo.
3.  Arquivo é enviado para o Oracle Cloud Object Storage.
4.  Metadados são persistidos no banco de dados.
5.  Um endpoint alimenta o telão com os conteúdos aprovados.

------------------------------------------------------------------------

## 2. Stack Tecnológica

### Backend

-   Java 21+
-   Spring Boot
-   Spring Web
-   Spring Data JPA
-   Oracle Cloud Infrastructure (OCI) SDK
-   Oracle Object Storage

### Infraestrutura

-   OCI Free Tier
-   Bucket: hojeafestaenossa
-   Região: sa-saopaulo-1

------------------------------------------------------------------------

## 3. Arquitetura Geral

### Camadas

    Controller -> Application(Service) -> Domain -> Infrastructure

#### Controller

Responsável por receber requisições HTTP.

#### Application

Contém regras de aplicação e orquestra serviços.

#### Domain

Entidades e contratos (interfaces).

#### Infrastructure

Integrações externas (OCI Object Storage, banco, etc).

------------------------------------------------------------------------

## 4. Componentes Implementados

### UploadController

Endpoint responsável por receber arquivos.

    POST /api/events/{slug}/uploads

Recebe: - file (MultipartFile) - message (opcional)

Responsabilidade: - Delegar upload ao UploadService.

------------------------------------------------------------------------

### UploadService

Responsável por:

-   Validar arquivo
-   Identificar tipo (imagem/vídeo)
-   Enviar para Object Storage
-   Persistir metadados

------------------------------------------------------------------------

### OciObjectStorageService

Responsável pela integração com OCI:

-   Upload de arquivos
-   Geração da URL pública
-   Organização dos objetos no bucket

------------------------------------------------------------------------

### Configuração OCI

Bean: - ObjectStorageClient - Authentication Provider - OciProperties
(configurações do OCI)

------------------------------------------------------------------------

## 5. Modelo de Domínio

### Post (Entidade)

Representa um conteúdo enviado ao evento.

Campos sugeridos:

-   id
-   eventSlug
-   objectName
-   url
-   fileType
-   message
-   createdAt
-   approved

------------------------------------------------------------------------

### FileType (Enum)

Valores:

-   IMAGE
-   VIDEO

------------------------------------------------------------------------

### PostRepository

Interface JPA para persistência dos posts.

------------------------------------------------------------------------

## 6. Fluxo de Upload

    Client
      -> UploadController
          -> UploadService
              -> OciObjectStorageService
                  -> Oracle Object Storage
              -> PostRepository.save()

------------------------------------------------------------------------

## 7. Objetivo Final do Sistema

Criar um pipeline funcional:

Upload -\> Armazenamento -\> Persistência -\> Feed do Telão

------------------------------------------------------------------------

## 8. Próximos Passos (Planejamento por Tarefas)

### Fase 1 --- Domínio

-   [ ] Criar entidade Post
-   [ ] Criar enum FileType
-   [ ] Criar PostRepository

### Fase 2 --- Upload

-   [ ] Finalizar UploadService
-   [ ] Validar tamanho máximo (30s vídeo)
-   [ ] Validar tipo MIME

### Fase 3 --- Object Storage

-   [ ] Garantir upload funcional
-   [ ] Testar URL pública
-   [ ] Organizar prefixos no bucket

### Fase 4 --- Telão

-   [ ] Endpoint GET para listar posts aprovados
-   [ ] Ordenação por data
-   [ ] Paginação

### Fase 5 --- Moderação

-   [ ] Endpoint aprovar/rejeitar conteúdo
-   [ ] Flag approved no Post

### Fase 6 --- Deploy

-   [ ] Dockerfile backend
-   [ ] Build image
-   [ ] Deploy OCI Compute
-   [ ] Variáveis de ambiente OCI
-   [ ] HTTPS + domínio

------------------------------------------------------------------------

## 9. Estrutura de Pastas Sugerida

    com.rafaellima.hojeafestaenossa
    ├── event
    │   ├── application
    │   ├── domain
    │   └── infrastructure
    ├── web
    │   └── upload
    └── config

------------------------------------------------------------------------

## 10. Estratégia de Uso com IA CLI (OpenCode)

Use esta documentação como guia para:

1.  Gerar entidades.
2.  Implementar services.
3.  Criar endpoints REST.
4.  Configurar deploy.
5.  Automatizar pipeline.

------------------------------------------------------------------------

## 11. Resultado Esperado

Um sistema MVP totalmente funcional capaz de:

-   Receber uploads
-   Armazenar mídia em cloud
-   Persistir metadados
-   Servir conteúdo para um telão em eventos
