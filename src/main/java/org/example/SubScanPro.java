package org.example;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.*;
import org.json.*;

public class SubScanPro {

    private static final String API_KEY = "your_api_key_here";
    private static final String DOMAIN = "example.com";
    private static final String API_ENDPOINT = "https://subdomains.whoisxmlapi.com/api/v1";

    private static final String INPUT_FILE = "directories.dat";
    private static final String OUTPUT_FILE = "working_links.txt";

    private static final List<String> SKIPPED_SUBDOMAINS = List.of("cpanel", "webmail", "autodiscover");

    private static final int THREADS = 10;
    private static final ExecutorService executor = Executors.newFixedThreadPool(THREADS);

    public static void main(String[] args) {
        try {
            Set<String> directories = loadDirectories(INPUT_FILE);
            if (directories.isEmpty()) {
                System.out.println("No directories found in input.");
                return;
            }

            Path outputPath = prepareOutputFile(OUTPUT_FILE);

            System.out.println("Scanning root domain: " + DOMAIN);
            scanDomain(DOMAIN, directories, outputPath);

            Set<String> subdomains = getSubdomains();
            if (!subdomains.isEmpty()) {
                for (String sub : subdomains) {
                    scanDomain(sub + "." + DOMAIN, directories, outputPath);
                }
            }

            executor.shutdown();
            System.out.println("Scan complete. Results saved to " + OUTPUT_FILE);

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private static Set<String> loadDirectories(String file) throws IOException {
        return new HashSet<>(Files.readAllLines(Paths.get(file)));
    }

    private static Path prepareOutputFile(String fileName) throws IOException {
        Path path = Paths.get(fileName);
        Files.deleteIfExists(path);
        return Files.createFile(path);
    }

    private static void scanDomain(String domain, Set<String> directories, Path outputPath) {
        List<Future<Void>> tasks = new ArrayList<>();
        for (String dir : directories) {
            String url = "https://" + domain + "/" + dir;
            tasks.add(executor.submit(() -> {
                if (urlExists(url)) {
                    System.out.println("Found: " + url);
                    saveLink(outputPath, url);
                }
                return null;
            }));
        }
        tasks.forEach(task -> {
            try { task.get(); } catch (Exception ignored) {}
        });
    }

    private static Set<String> getSubdomains() {
        Set<String> subdomains = new HashSet<>();
        try {
            String url = API_ENDPOINT + "?apiKey=" + API_KEY + "&domainName=" + DOMAIN;
            HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
            conn.setRequestMethod("GET");

            if (conn.getResponseCode() != 200) return subdomains;

            String json = new String(conn.getInputStream().readAllBytes());
            JSONObject root = new JSONObject(json);
            JSONArray records = root.optJSONObject("result").optJSONArray("records");

            if (records == null) return subdomains;

            for (int i = 0; i < records.length(); i++) {
                String sub = records.getJSONObject(i).getString("domain").replace("." + DOMAIN, "");
                if (!SKIPPED_SUBDOMAINS.contains(sub) && !"www".equals(sub)) {
                    subdomains.add(sub);
                }
            }

        } catch (Exception e) {
            System.err.println("Failed to fetch subdomains: " + e.getMessage());
        }

        return subdomains;
    }

    private static boolean urlExists(String url) {
        try {
            HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
            conn.setRequestMethod("HEAD");
            return conn.getResponseCode() == 200 || conn.getResponseCode() == 403;
        } catch (IOException e) {
            return false;
        }
    }

    private static synchronized void saveLink(Path output, String url) {
        try {
            Files.writeString(output, url + System.lineSeparator(), StandardOpenOption.APPEND);
        } catch (IOException e) {
            System.err.println("Could not save: " + url);
        }
    }
}
