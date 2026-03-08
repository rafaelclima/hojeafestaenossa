package com.rafaellima.hojeafestaenossa.shared.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // Habilita um "carteiro" em memória para entregar mensagens nos destinos que
        // começam com "/topic".
        // O telão vai se inscrever em um tópico como "/topic/events/{token}/slideshow".
        config.enableSimpleBroker("/topic");
        // Define que as mensagens enviadas pelo cliente para o servidor devem ter o
        // prefixo "/app".
        // Não usaremos isso agora, mas é uma boa prática configurar.
        config.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // Registra o endpoint "/ws" que os clientes usarão para se conectar ao nosso
        // servidor WebSocket.
        // withSockJS() é um fallback para navegadores que não suportam WebSockets
        // nativamente.
        registry.addEndpoint("/ws").setAllowedOriginPatterns("*").withSockJS();
    }
}
