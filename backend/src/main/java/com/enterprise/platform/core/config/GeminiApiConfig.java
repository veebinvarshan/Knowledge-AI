package com.enterprise.platform.core.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import jakarta.annotation.PostConstruct;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

@Configuration
public class GeminiApiConfig {

    private static final Logger log = LoggerFactory.getLogger(GeminiApiConfig.class);

    private final Environment env;

    @Autowired
    public GeminiApiConfig(Environment env) {
        this.env = env;
    }

    @PostConstruct
    public void validateApiKey() {
        if (!isGeminiActive()) {
            log.info("Gemini AI services are not active; skipping Gemini API key verification.");
            return;
        }

        String apiKey = System.getenv("GEMINI_API_KEY");
        if (apiKey == null || apiKey.trim().isEmpty()) {
            apiKey = System.getProperty("GEMINI_API_KEY");
        }

        // Try reading from relative .env file paths
        if (apiKey == null || apiKey.trim().isEmpty()) {
            apiKey = tryReadFromEnvFile(".env");
        }
        if (apiKey == null || apiKey.trim().isEmpty()) {
            apiKey = tryReadFromEnvFile("backend/.env");
        }
        if (apiKey == null || apiKey.trim().isEmpty()) {
            apiKey = tryReadFromEnvFile("../.env");
        }

        boolean isTest = isTestEnv();

        if (apiKey == null || apiKey.trim().isEmpty()) {
            if (isTest) {
                log.warn("Gemini API key is missing. Using mock/dummy key for tests.");
                System.setProperty("GEMINI_API_KEY", "MOCK_GEMINI_API_KEY");
                return;
            }
            String errorMsg = "CRITICAL STARTUP FAILURE: The environment variable 'GEMINI_API_KEY' is missing. Please configure GEMINI_API_KEY in your environment or .env file.";
            log.error(errorMsg);
            throw new IllegalStateException(errorMsg);
        } else {
            System.setProperty("GEMINI_API_KEY", apiKey);
        }
        log.info("Gemini API Key validation successful. AI Services initialized.");
    }

    private boolean isTestEnv() {
        String command = System.getProperty("sun.java.command", "").toLowerCase();
        if (command.contains("junit") || command.contains("surefire") || command.contains("maven")) {
            return true;
        }
        for (StackTraceElement element : Thread.currentThread().getStackTrace()) {
            String name = element.getClassName().toLowerCase();
            if (name.contains("junit") || name.contains("testng") || name.contains("surefire") || name.contains("spring.test") || name.contains("springbootversion") || name.contains("testcontext")) {
                return true;
            }
        }
        return false;
    }

    private String tryReadFromEnvFile(String path) {
        try {
            File file = new File(path);
            if (file.exists()) {
                try (BufferedReader br = new BufferedReader(new FileReader(file))) {
                    String line;
                    while ((line = br.readLine()) != null) {
                        line = line.trim();
                        if (line.startsWith("GEMINI_API_KEY=")) {
                            return line.substring("GEMINI_API_KEY=".length()).trim();
                        }
                    }
                }
            }
        } catch (Exception ignored) {}
        return null;
    }

    private boolean isGeminiActive() {
        boolean aiEnabled = Boolean.parseBoolean(env.getProperty("platform.ai.enabled", "true"));
        
        String embeddingProvider = env.getProperty("platform.embedding.provider", "gemini");
        boolean embeddingEnabled = Boolean.parseBoolean(env.getProperty("platform.embedding.enabled", "true")) 
                && "gemini".equalsIgnoreCase(embeddingProvider);
                
        boolean ragEnabled = Boolean.parseBoolean(env.getProperty("platform.rag.enabled", "true"));
        
        return aiEnabled || embeddingEnabled || ragEnabled;
    }
}
