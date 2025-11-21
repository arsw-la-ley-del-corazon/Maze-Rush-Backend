package org.arsw.maze_rush;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class MazeRushApplication {
    public static void main(String[] args) {
        // Load environment variables from .env file
        try {
            Dotenv dotenv = Dotenv.configure().ignoreIfMissing().load();
            // Set system properties from .env file
            dotenv.entries().forEach(e -> System.setProperty(e.getKey(), e.getValue()));
        } catch (Exception e) {
            // Continue without .env file - useful for production where env vars are set directly
            System.out.println("No .env file found or error loading it. Continuing with system environment variables.");
        }
        
        SpringApplication.run(MazeRushApplication.class, args);
    }
    
}
