# 📡 Integração WebSocket - Telão (Slideshow)

## Visão Geral

A aplicação utiliza **WebSocket com STOMP** para notificar o telão em tempo real quando novas mídias são aprovadas pela moderação.

---

## 🔧 Configuração do Backend

### Endpoint de Conexão

```
URL: ws://{BASE_URL}/ws
Exemplo: ws://localhost:8080/ws
Produção: wss://hojeafestaenossa.site/ws
```

### Protocolo

- **Protocolo:** WebSocket + STOMP
- **Broker:** Simple Broker (em memória)
- **Prefixo de subscrição:** `/topic`
- **Prefixo de aplicação:** `/app`

---

## 📥 Subscrição do Telão

### Tópico para Escutar

O telão deve se inscrever no seguinte tópico para receber novas mídias aprovadas:

```
/topic/events/{eventToken}/slideshow
```

**Exemplo:**
```
/topic/events/uhHL6wfMxCED/slideshow
```

### Payload da Mensagem

Quando uma mídia é aprovada, o backend envia o seguinte JSON:

```json
{
  "url": "https://objectstorage.sa-saopaulo-1.oraclecloud.com/n/rafaellimadev/b/hojeafestaenossa/o/uhHL6wfMxCED/385ff5e0-f921-4701-948a-768eb99032cd.jpg",
  "mediaType": "PHOTO",
  "message": "Parabéns aos noivos!",
  "createdAt": "2026-03-11T03:02:03Z"
}
```

### Estrutura da Resposta

| Campo | Tipo | Descrição |
|-------|------|-----------|
| `url` | string | URL completa da mídia no OCI Object Storage |
| `mediaType` | enum | `PHOTO` ou `VIDEO` |
| `message` | string | Mensagem enviada pelo usuário (opcional) |
| `createdAt` | datetime | Data/hora do upload em ISO 8601 |

---

## 💻 Exemplo de Implementação no Frontend

### Opção 1: Usando `stompjs` (Recomendado)

```bash
npm install @stomp/stompjs
```

```typescript
import { Client, Frame } from '@stomp/stompjs';

class SlideshowService {
  private client: Client;
  private eventToken: string;

  constructor(eventToken: string) {
    this.eventToken = eventToken;
    
    this.client = new Client({
      brokerURL: 'ws://localhost:8080/ws',
      // Para produção use:
      // brokerURL: 'wss://hojeafestaenossa.site/ws',
      
      reconnectDelay: 5000, // Reconectar a cada 5s se desconectar
      
      onConnect: (frame: Frame) => {
        console.log('✅ Conectado ao WebSocket');
        this.subscribeToSlideshow();
      },
      
      onStompError: (frame: Frame) => {
        console.error('❌ Erro STOMP:', frame.headers['message']);
      },
      
      onWebSocketError: (event: Event) => {
        console.error('❌ Erro WebSocket:', event);
      },
      
      onDisconnect: () => {
        console.log('🔌 Desconectado do WebSocket');
      },
    });
  }

  // Iniciar conexão
  public connect(): void {
    this.client.activate();
  }

  // Parar conexão
  public disconnect(): void {
    this.client.deactivate();
  }

  // Se inscrever no tópico do slideshow
  private subscribeToSlideshow(): void {
    const topic = `/topic/events/${this.eventToken}/slideshow`;
    
    this.client.subscribe(topic, (message) => {
      const payload = JSON.parse(message.body);
      this.handleNewMedia(payload);
    });
    
    console.log(`📡 Inscrito no tópico: ${topic}`);
  }

  // Handler para nova mídia aprovada
  private handleNewMedia(payload: {
    url: string;
    mediaType: 'PHOTO' | 'VIDEO';
    message: string;
    createdAt: string;
  }): void {
    console.log('🎉 Nova mídia aprovada!', payload);
    
    // Adicionar ao slideshow
    // Exemplo: atualizar estado, adicionar à fila, etc.
    this.addToSlideshow(payload);
  }

  private addToSlideshow(media: any): void {
    // Sua lógica para adicionar a mídia ao slideshow
    // Ex: dispatch de action, update de estado, etc.
  }
}

// Uso:
const slideshow = new SlideshowService('uhHL6wfMxCED');
slideshow.connect();
```

---

### Opção 2: Usando `sockjs-client` + `stompjs`

```bash
npm install sockjs-client @stomp/stompjs
```

```typescript
import SockJS from 'sockjs-client';
import { Stomp, Frame } from '@stomp/stompjs';

class SlideshowService {
  private stompClient: any;
  private eventToken: string;

  constructor(eventToken: string) {
    this.eventToken = eventToken;
  }

  public connect(): void {
    // Criar conexão SockJS (fallback para navegadores sem WebSocket)
    const socket = new SockJS('http://localhost:8080/ws');
    
    // Criar cliente STOMP
    this.stompClient = Stomp.over(socket);
    
    // Conectar
    this.stompClient.connect({}, (frame: Frame) => {
      console.log('✅ Conectado: ' + frame);
      this.subscribe();
    }, (error: any) => {
      console.error('❌ Erro de conexão:', error);
    });
  }

  private subscribe(): void {
    const topic = `/topic/events/${this.eventToken}/slideshow`;
    
    this.stompClient.subscribe(topic, (message: Frame) => {
      const payload = JSON.parse(message.body);
      this.handleNewMedia(payload);
    });
  }

  private handleNewMedia(payload: any): void {
    console.log('🎉 Nova mídia:', payload);
    // Adicionar ao slideshow
  }

  public disconnect(): void {
    if (this.stompClient) {
      this.stompClient.disconnect();
    }
  }
}
```

---

### Opção 3: React + Hooks

```typescript
// hooks/useSlideshow.ts
import { useEffect, useState, useCallback } from 'react';
import { Client, Frame } from '@stomp/stompjs';

interface Media {
  url: string;
  mediaType: 'PHOTO' | 'VIDEO';
  message: string;
  createdAt: string;
}

export function useSlideshow(eventToken: string) {
  const [medias, setMedias] = useState<Media[]>([]);
  const [connected, setConnected] = useState(false);

  useEffect(() => {
    const client = new Client({
      brokerURL: `ws://localhost:8080/ws`,
      reconnectDelay: 5000,
      
      onConnect: () => {
        setConnected(true);
        const subscription = client.subscribe(
          `/topic/events/${eventToken}/slideshow`,
          (message) => {
            const newMedia: Media = JSON.parse(message.body);
            setMedias((prev) => [...prev, newMedia]);
          }
        );
        
        return () => {
          subscription.unsubscribe();
        };
      },
      
      onDisconnect: () => {
        setConnected(false);
      },
    });

    client.activate();

    return () => {
      client.deactivate();
    };
  }, [eventToken]);

  const addMedia = useCallback((media: Media) => {
    setMedias((prev) => [...prev, media]);
  }, []);

  return { medias, connected, addMedia };
}

// Uso no componente:
function Slideshow({ eventToken }: { eventToken: string }) {
  const { medias, connected } = useSlideshow(eventToken);

  return (
    <div>
      <div>Status: {connected ? '🟢 Conectado' : '🔴 Desconectado'}</div>
      {medias.map((media) => (
        <MediaItem key={media.url} media={media} />
      ))}
    </div>
  );
}
```

---

### Opção 4: Vue 3 + Composition API

```typescript
// composables/useSlideshow.ts
import { ref, onMounted, onUnmounted } from 'vue';
import { Client, Frame } from '@stomp/stompjs';

interface Media {
  url: string;
  mediaType: 'PHOTO' | 'VIDEO';
  message: string;
  createdAt: string;
}

export function useSlideshow(eventToken: string) {
  const medias = ref<Media[]>([]);
  const connected = ref(false);
  let client: Client | null = null;

  onMounted(() => {
    client = new Client({
      brokerURL: `ws://localhost:8080/ws`,
      reconnectDelay: 5000,
      
      onConnect: () => {
        connected.value = true;
        client?.subscribe(
          `/topic/events/${eventToken}/slideshow`,
          (message) => {
            const newMedia: Media = JSON.parse(message.body);
            medias.value.push(newMedia);
          }
        );
      },
      
      onDisconnect: () => {
        connected.value = false;
      },
    });

    client.activate();
  });

  onUnmounted(() => {
    client?.deactivate();
  });

  return { medias, connected };
}
```

---

## 🔄 Fluxo Completo

```
┌─────────────┐     ┌──────────────┐     ┌─────────────┐     ┌─────────────┐
│   Usuário   │────▶│   Backend    │────▶│   Admin     │────▶│   Telão     │
│   (Upload)  │     │  (Processa)  │     │  (Aprova)   │     │  (WebSocket)│
└─────────────┘     └──────────────┘     └─────────────┘     └─────────────┘
                           │                    │                    │
                           │                    │                    │
                     1. Upload            2. Moderação         3. Notificação
                        PHOTO/VIDEO          visible=true         convertAndSend
                        compressão           (PUT /uploads/       /topic/events/
                        thumbnail            {token}/{id}/        {token}/slideshow
                        save no DB           visibility)
```

---

## 🔐 Considerações de Segurança

### 1. **CORS no Backend**

O backend está configurado para aceitar conexões de qualquer origem:

```java
registry.addEndpoint("/ws")
    .setAllowedOriginPatterns("*")  // ⚠️ Em produção, restrinja as origens
    .withSockJS();
```

**Recomendação para produção:**

```java
registry.addEndpoint("/ws")
    .setAllowedOriginPatterns(
        "https://hojeafestaenossa.site",
        "https://www.hojeafestaenossa.site"
    )
    .withSockJS();
```

### 2. **Autenticação**

Atualmente o WebSocket **não requer autenticação**. Qualquer cliente pode:
- Conectar-se ao endpoint `/ws`
- Se inscrever em qualquer tópico `/topic/events/{token}/slideshow`

**Se precisar de autenticação no futuro:**

```typescript
// No frontend, enviar token no header da conexão
const client = new Client({
  brokerURL: 'ws://localhost:8080/ws',
  connectHeaders: {
    'Authorization': `Bearer ${token}`
  },
  // ...
});
```

```java
// No backend, validar no ChannelInterceptor
public class AuthChannelInterceptor implements ChannelInterceptor {
    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);
        String token = accessor.getFirstNativeHeader("Authorization");
        // Validar token...
        return message;
    }
}
```

---

## 🧪 Testando Manualmente

### 1. **Usando o wscat (Node.js)**

```bash
npm install -g wscat
```

```bash
# Conectar
wscat -c ws://localhost:8080/ws

# Se inscrever (após conectar)
SUBSCRIBE
destination:/topic/events/TEST123/slideshow
id:sub-0

# Aguardar mensagem quando admin aprovar mídia
```

### 2. **Usando o Postman**

1. Criar nova requisição **WebSocket**
2. URL: `ws://localhost:8080/ws`
3. Conectar
4. Enviar frame STOMP manualmente:
   ```
   SUBSCRIBE
   destination:/topic/events/TEST123/slideshow
   id:sub-0
   ```

### 3. **Teste no Navegador (DevTools)**

```javascript
// No Console do Chrome/Firefox
const socket = new WebSocket('ws://localhost:8080/ws');

socket.onopen = () => {
  console.log('Conectado!');
  // Enviar CONNECT do STOMP
  socket.send('CONNECT\naccept-version:1.1,1.0\nheart-beat:10000,10000\n\n\u0000');
};

socket.onmessage = (event) => {
  console.log('Mensagem recebida:', event.data);
};

// Se inscrever após CONNECT
setTimeout(() => {
  socket.send('SUBSCRIBE\ndestination:/topic/events/TEST123/slideshow\nid:sub-0\n\n\u0000');
}, 1000);
```

---

## 📋 Checklist de Implementação

- [ ] Instalar `@stomp/stompjs`
- [ ] Criar serviço de conexão WebSocket
- [ ] Implementar subscrição no tópico `/topic/events/{token}/slideshow`
- [ ] Criar handler para receber novas mídias
- [ ] Adicionar lógica para exibir mídia recebida no slideshow
- [ ] Implementar reconexão automática em caso de queda
- [ ] Tratar estados de conexão (conectado/desconectado)
- [ ] Testar com aprovação real de mídia no admin

---

## 🐛 Troubleshooting

| Problema | Solução |
|----------|---------|
| Conexão não estabelece | Verificar se backend está rodando na porta 8080 |
| Mensagens não chegam | Confirmar que o tópico está correto: `/topic/events/{token}/slideshow` |
| Reconexão infinita | Ajustar `reconnectDelay` para 5000ms ou mais |
| Erro de CORS | Backend deve ter `setAllowedOriginPatterns` configurado |
| Payload undefined | Verificar `JSON.parse(message.body)` |

---

## 📚 Referências

- [Spring WebSocket Documentation](https://docs.spring.io/spring-framework/docs/current/reference/html/web.html#websocket)
- [STOMP Protocol Specification](http://stomp.github.io/stomp-specification-1.2.html)
- [stompjs GitHub](https://github.com/stomp-js/stompjs)

---

**Última atualização:** 11 de março de 2026  
**Versão do backend:** 0.0.1-SNAPSHOT (Spring Boot 3.2.4)
