package org.arsw.maze_rush;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SpringBootApplication
@EnableScheduling
public class MazeRushApplication {
    private static final Logger log = LoggerFactory.getLogger(MazeRushApplication.class);

    public static void main(String[] args) {
        // Load environment variables from .env file
        try {
            Dotenv dotenv = Dotenv.configure().ignoreIfMissing().load();
            // Set system properties from .env file
            dotenv.entries().forEach(e -> System.setProperty(e.getKey(), e.getValue()));
        } catch (Exception e) {
            // Continue without .env file - useful for production where env vars are set directly
            log.warn("No .env file found or error loading it. Continuing with system environment variables.");
        }
        
        SpringApplication.run(MazeRushApplication.class, args);
    }
    
}
