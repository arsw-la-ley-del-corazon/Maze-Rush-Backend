package org.arsw.maze_rush;

import io.github.cdimascio.dotenv.Dotenv;
import io.github.cdimascio.dotenv.DotenvBuilder; // <--- IMPORTANTE: Importar esta clase
import io.github.cdimascio.dotenv.DotenvEntry;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;

import java.util.Collections;
import java.util.Set;

import static org.mockito.Mockito.*;

class MazeRushApplicationTests {

    @Test
    void testMain_HappyPath() {
        try (MockedStatic<SpringApplication> springAppMock = mockStatic(SpringApplication.class);
             MockedStatic<Dotenv> dotenvMockStatic = mockStatic(Dotenv.class)) {

            springAppMock.when(() -> SpringApplication.run(MazeRushApplication.class, new String[]{}))
                    .thenReturn(mock(ConfigurableApplicationContext.class));
            DotenvBuilder builderMock = mock(DotenvBuilder.class);
            Dotenv dotenvInstanceMock = mock(Dotenv.class);
            
            DotenvEntry entryMock = mock(DotenvEntry.class);
            when(entryMock.getKey()).thenReturn("TEST_VAR");
            when(entryMock.getValue()).thenReturn("test_value");
            Set<DotenvEntry> entries = Collections.singleton(entryMock);

            dotenvMockStatic.when(Dotenv::configure).thenReturn(builderMock);
            
            when(builderMock.ignoreIfMissing()).thenReturn(builderMock);
            
            when(builderMock.load()).thenReturn(dotenvInstanceMock);
            
            when(dotenvInstanceMock.entries()).thenReturn(entries);

            MazeRushApplication.main(new String[]{});

            verify(builderMock).load();
            springAppMock.verify(() -> SpringApplication.run(MazeRushApplication.class, new String[]{}));
        }
    }

    @Test
    void testMain_WhenDotenvFails_ShouldContinue() {
        try (MockedStatic<SpringApplication> springAppMock = mockStatic(SpringApplication.class);
             MockedStatic<Dotenv> dotenvMockStatic = mockStatic(Dotenv.class)) {

            springAppMock.when(() -> SpringApplication.run(MazeRushApplication.class, new String[]{}))
                    .thenReturn(mock(ConfigurableApplicationContext.class));
            dotenvMockStatic.when(Dotenv::configure).thenThrow(new RuntimeException("Error simulado cargando .env"));

            MazeRushApplication.main(new String[]{});

            springAppMock.verify(() -> SpringApplication.run(MazeRushApplication.class, new String[]{}));
        }
    }
}