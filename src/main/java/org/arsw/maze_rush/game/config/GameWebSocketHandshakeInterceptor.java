package org.arsw.maze_rush.game.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.Map;

/**
 * Interceptor para el handshake WebSocket
 * Captura parámetros de la conexión inicial para uso posterior en el listener de desconexión
 */
@Component
@Slf4j
public class GameWebSocketHandshakeInterceptor implements HandshakeInterceptor {

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                  WebSocketHandler wsHandler, Map<String, Object> attributes) {
        // Los atributos username y lobbyCode se establecerán cuando el jugador haga join
        // No podemos extraerlos del query string aquí porque STOMP no los incluye en el handshake
        log.debug("WebSocket handshake iniciado desde {}", request.getRemoteAddress());
        return true;
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
                              WebSocketHandler wsHandler, Exception exception) {
        if (exception != null) {
            log.error("Error en handshake WebSocket: {}", exception.getMessage());
        } else {
            log.debug("WebSocket handshake completado exitosamente");
        }
    }
}
