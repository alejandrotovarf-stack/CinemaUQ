package com.cinemauq.patterns.structural;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public class ApiGeneradorTXTExterno {
    public void writeTextFile(String path, String content) {
        try {
            Files.createDirectories(Path.of(path).getParent());
            Files.write(Path.of(path), content.getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            throw new RuntimeException("No se pudo escribir el archivo TXT: " + e.getMessage(), e);
        }
    }
}
