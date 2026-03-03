# Especificação de Telas - Hoje A Festa É Nossa

## Visão Geral do Projeto

Plataforma onde usuários enviam fotos/vídeos para eventos específicos que são exibidos em um telão em tempo real.

---

## Telas Necesárias

### 1. Tela de Acesso ao Evento (Landing Page)

**URL:** `/` ou `/event/{token}`

**Descrição:** Primeira tela que o usuário acessa ao escanear o QR Code do evento.

**Elementos:**
- Banner/Capa do evento (imagem ou cor de fundo)
- Nome do evento
- Data do evento
- Instrução para enviar fotos/vídeos
- Botão principal: "Enviar Foto/Vídeo"
- Botão secundário: "Ver Telão" (se o evento já estiver ativo)

---

### 2. Tela de Upload de Mídia

**URL:** `/event/{token}/upload`

**Descrição:** Formulário para o convidado enviar sua foto ou vídeo.

**Elementos:**
- Evento info (nome do evento - header/topbar)
- Área de drag & drop para arquivo
- Botão para selecionar arquivo do dispositivo
- Pré-visualização do arquivo selecionado
- Campo de mensagem opcional (textarea)
- Botão "Enviar"
- Indicador de progresso durante upload
- Feedback de sucesso/erro

**Regras:**
- Aceitar apenas fotos e vídeos
- Limite: 8MB para fotos, 50MB para vídeos
- Mostrar mensagens de erro claras

---

### 3. Tela do Telão (Slideshow)

**URL:** `/event/{token}/slideshow`

**Descrição:** Exibição em tela cheia das fotos/vídeos aprovados para o evento. Ideal para TV/Monitor.

**Elementos:**
- Exibição em tela cheia
- Mídia atual (foto ou vídeo)
- Transição automática para próxima mídia
- Tempo de exibição: ~5-10 segundos por mídia
- Mensagem da mídia (se houver) - sobreposição discreta
- Indicador de próxima mídia (opcional)
- Controle de navegação (play/pause, próximo/anterior) - opcional para quem está controlando

**Comportamento:**
- Reprodução automática de vídeos
- Loop contínuo
- Ordernação: mais recentes primeiro
- Apenas mídias aprovadas aparecem

---

### 4. Tela de Moderação

**URL:** `/admin/event/{token}/moderation`

**Descrição:** Painel para o organizador aprovar ou rejeitar conteúdos enviados.

**Elementos:**
- Grid ou lista de mídias pendentes
- Miniaturas das mídias
- Botões de ação: Aprovar | Rejeitar
- Opção de pré-visualizar a mídia completa
- Filtros: Pendentes | Aprovadas | Rejeitadas
- Contador de mídias por status
- Botão "Aprovar Todas" / "Rejeitar Todas" (opcional)

---

### 5. Tela de Criação/Gestão de Eventos (Admin)

**URL:** `/admin/events` e `/admin/events/new`

**Descrição:** Painel para criar e gerenciar eventos.

**Elementos:**
- Lista de eventos criados
- Botão para criar novo evento
- Formulário de criação:
  - Nome do evento
  - Data/hora de início
  - Data/hora de término
  - Configurações de privacidade
- QR Code de acesso ao evento
- Link compartilhável

---

## Fluxo de Usuário

```
[QR Code] → [Landing Page] → [Upload] → [Telão]
                ↓
         [Admin/Moderação]
```

---

## Responsividade

| Tela | Desktop | Tablet | Mobile |
|------|---------|--------|--------|
| Landing Page | ✅ | ✅ | ✅ |
| Upload | ✅ | ✅ | ✅ |
| Telão | ✅ (principal) | ✅ | ❌ (não recomendado) |
| Moderação | ✅ | ✅ | ✅ |
| Admin | ✅ | ⚠️ | ❌ |

---

## Integrações com Backend

### Endpoints utilizados:

| Ação | Método | Endpoint |
|------|--------|----------|
| Buscar evento | GET | `/events/{token}` |
| Enviar mídia | POST | `/uploads/events/{eventToken}` |
| Listar telão | GET | `/uploads/events/{eventToken}/slideshow` |
| Listar para moderação | GET | `/uploads/events/{eventToken}/pending` (a implementar) |
| Aprovar mídia | PUT | `/uploads/{uploadId}/visibility` |
| Rejeitar mídia | PUT | `/uploads/{uploadId}/visibility` |
| Criar evento | POST | `/events` (a implementar) |

---

## Sugestões Visuais

### Tema: Festas/Eventos
- Cores vibrantes ou elegantes (depende do tipo de evento)
- Animações suaves nas transições do telão
- Interface limpa e intuitiva
- Foco na mídia (conteúdo principal)

### Telão
- Fundo escuro para destacar imagens
- Fonte legível para mensagens
- Sem elementos de UI desnecessários
- Transições em fade/slide
