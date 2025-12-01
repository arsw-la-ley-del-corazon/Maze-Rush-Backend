package org.arsw.maze_rush.config;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class RedisConfigTest {

    @Mock
    private RedisConnectionFactory redisConnectionFactory;

    @Test
    void testRedisTemplateConfiguration() {
        //  Instanciar la configuración manualmente
        RedisConfig redisConfig = new RedisConfig();

        // Invocar el método que crea el Bean
        RedisTemplate<String, Object> template = redisConfig.redisTemplate(redisConnectionFactory);

        //  Verificaciones Generales
        assertNotNull(template, "El RedisTemplate no debe ser nulo");
        assertEquals(redisConnectionFactory, template.getConnectionFactory(), "Debe tener la fábrica de conexión asignada");

        //  Verificar la configuración de los Serializadores
        assertTrue(template.getKeySerializer() instanceof StringRedisSerializer, 
                "El serializador de claves debe ser StringRedisSerializer");
        assertTrue(template.getHashKeySerializer() instanceof StringRedisSerializer, 
                "El serializador de claves hash debe ser StringRedisSerializer");

        // Valores (Values) deben ser JSON (GenericJackson2JsonRedisSerializer)
        assertTrue(template.getValueSerializer() instanceof GenericJackson2JsonRedisSerializer, 
                "El serializador de valores debe ser GenericJackson2JsonRedisSerializer");
        assertTrue(template.getHashValueSerializer() instanceof GenericJackson2JsonRedisSerializer, 
                "El serializador de valores hash debe ser GenericJackson2JsonRedisSerializer");
    }
}