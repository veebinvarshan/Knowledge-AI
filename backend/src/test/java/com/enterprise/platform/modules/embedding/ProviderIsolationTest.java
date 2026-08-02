package com.enterprise.platform.modules.embedding;

import org.junit.jupiter.api.Test;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class ProviderIsolationTest {

    @Test
    void testSpringAiClassesNotImportedInServiceOrDomainPackages() throws Exception {
        // Scanning com.enterprise.platform.modules.embedding (except provider package) for org.springframework.ai imports
        File srcDir = new File("d:\\Knowledge\\backend\\src\\main\\java\\com\\enterprise\\platform\\modules\\embedding");
        if (!srcDir.exists()) return;

        verifyNoSpringAiImports(srcDir);
    }

    private void verifyNoSpringAiImports(File dir) throws Exception {
        File[] files = dir.listFiles();
        if (files == null) return;

        for (File f : files) {
            if (f.isDirectory()) {
                if (f.getName().equals("provider")) {
                    continue; // Providers are allowed to import Spring AI
                }
                verifyNoSpringAiImports(f);
            } else if (f.getName().endsWith(".java")) {
                List<String> lines = Files.readAllLines(f.toPath());
                for (String line : lines) {
                    if (line.trim().startsWith("import org.springframework.ai")) {
                        fail("Spring AI leaked outside provider package in: " + f.getAbsolutePath() + " -> " + line);
                    }
                }
            }
        }
    }
}
