// Scanner.java
package org.webprobej.core;

import org.webprobej.config.Config;
import org.webprobej.network.HttpUtil;
import org.webprobej.output.OutputManager;

import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.*;

public class Scanner {
    private static final ExecutorService executor = Executors.newFixedThreadPool(Config.THREADS);
    private static final Set<String> visitedUrls = ConcurrentHashMap.newKeySet();

    public static void scanDomain(String domain, Set<String> directories, Path outputPath, int maxDepth) {
        String baseUrl = "https://" + domain;
        scanFromBase(baseUrl, directories, outputPath, maxDepth);
    }

    public static void scanFromBase(String baseUrl, Set<String> directories, Path outputPath, int maxDepth) {
        List<Future<Void>> tasks = new ArrayList<>();

        System.out.println("Scanning folder: " + baseUrl);

        for (String dir : directories) {
            String url = baseUrl.endsWith("/") ? baseUrl + dir : baseUrl + "/" + dir;
            tasks.add(executor.submit(() -> {
                scanRecursively(url, directories, outputPath, 1, maxDepth);
                return null;
            }));
        }

        for (Future<Void> task : tasks) {
            try {
                task.get();
            } catch (Exception ignored) {}
        }
    }

    private static void scanRecursively(String baseUrl, Set<String> directories, Path outputPath, int depth, int maxDepth) {
        if (depth > maxDepth || visitedUrls.contains(baseUrl)) return;

        visitedUrls.add(baseUrl);

        if (HttpUtil.urlExists(baseUrl)) {
            boolean looksLikeFile = baseUrl.matches(".*\\.[a-zA-Z0-9]{1,5}(/)?$");
            String fileOrFolderName = baseUrl.substring(baseUrl.lastIndexOf('/') + 1).toLowerCase();
            boolean isSensitive = Config.SENSITIVE_KEYWORDS.stream().anyMatch(fileOrFolderName::contains);

            String label = looksLikeFile ? "[File]" : "[Folder]";
            if (isSensitive) label += " [Sensitive]";

            OutputManager.print(baseUrl, label, isSensitive);
            OutputManager.save(outputPath, baseUrl + " " + label);

            if (!looksLikeFile && !isBlacklistedFolder(baseUrl)) {
                for (String dir : directories) {
                    String nestedUrl = baseUrl.endsWith("/") ? baseUrl + dir : baseUrl + "/" + dir;
                    if (!visitedUrls.contains(nestedUrl)) {
                        executor.submit(() -> scanRecursively(nestedUrl, directories, outputPath, depth + 1, maxDepth));
                    }
                }
            }
        }
    }
    public static void scanSingleUrl(String url, Path outputPath, boolean suppressErrors) {
        if (visitedUrls.contains(url)) return;
        visitedUrls.add(url);

        if (HttpUtil.urlExists(url)) {
            boolean looksLikeFile = url.matches(".*\\.[a-zA-Z0-9]{1,5}(/)?$");
            String name = url.substring(url.lastIndexOf('/') + 1).toLowerCase();
            boolean isSensitive = Config.SENSITIVE_KEYWORDS.stream().anyMatch(name::contains);

            String label = looksLikeFile ? "[File]" : "[Folder]";
            if (isSensitive) label += " [Sensitive]";
            if (name.matches(".*\\.(zip|rar|tar|gz|tgz|7z|bak)$")) label += " [Backup]";

            OutputManager.print(url, label, isSensitive);
            OutputManager.save(outputPath, url + " " + label);
        } else if (!suppressErrors) {
            System.out.println("[-] Not found: " + url);
        }
    }



    private static boolean isBlacklistedFolder(String url) {
        String[] parts = url.toLowerCase().split("/");
        String lastSegment = parts[parts.length - 1];
        return Config.FOLDER_SCAN_BLACKLIST.contains(lastSegment);
    }

    public static void shutdown() {
        executor.shutdown();
    }

}