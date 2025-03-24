package org.example.subscanpro.output;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.StandardOpenOption;

public class OutputManager {
    public static Path prepareOutputFile(String fileName) {
        try {
            Path path = Paths.get(fileName);
            Files.deleteIfExists(path);
            return Files.createFile(path);
        } catch (IOException e) {
            throw new RuntimeException("Failed to prepare output file", e);
        }
    }

    public static synchronized void save(Path output, String text) {
        try {
            Files.writeString(output, text + System.lineSeparator(), StandardOpenOption.APPEND);
        } catch (IOException e) {
            System.err.println("Could not save: " + text);
        }
    }

    public static void print(String url, String label, boolean alert) {
        String base = "[✔] Found: " + url + " " + label;
        if (alert) {
            System.out.println("\u001B[33;1m" + base + "\u001B[0m");
            System.out.println("\u001B[31;1m[!] ALERT: Sensitive file detected → " + url + "\u001B[0m");
        } else {
            System.out.println(base);
        }
    }
}
