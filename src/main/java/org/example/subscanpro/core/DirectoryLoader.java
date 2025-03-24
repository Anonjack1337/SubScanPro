package org.example.subscanpro.core;

import org.example.subscanpro.network.HttpUtil;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Pattern;
import org.example.subscanpro.config.Config;


public class DirectoryLoader {

    public static Set<String> loadFromFile(String file) throws IOException {
        return new HashSet<>(Files.readAllLines(Paths.get(file)));
    }

    public static Set<String> scrapeHomepage(String domain) {
        Set<String> foundDirs = new HashSet<>();
        try {
            String html = HttpUtil.get("https://" + domain + "/");
            var matcher = Pattern.compile("href=[\"'](?:https?://[^\"'/]*\\Q" + domain + "\\E)?(/[^\"'#?\\s]+)[\"']")
                    .matcher(html);

            while (matcher.find()) {
                String path = matcher.group(1).replaceFirst("^/", "");
                if (isLikelyDirectory(path)) {
                    String[] parts = path.split("/");
                    StringBuilder cumulative = new StringBuilder();
                    for (String part : parts) {
                        if (!part.isBlank()) {
                            if (cumulative.length() > 0) cumulative.append("/");
                            cumulative.append(part);
                            foundDirs.add(cumulative.toString());
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Failed to scrape homepage: " + e.getMessage());
        }

        if (foundDirs.isEmpty()) {
            foundDirs.addAll(Config.FALLBACK_FOLDERS);
            System.out.println("[~] No homepage folders found, using fallback list.");
        }

        return foundDirs;
    }

    public static Set<String> parseSitemap(String domain) {
        Set<String> found = new HashSet<>();
        List<String> urls = List.of(
                "https://" + domain + "/sitemap.xml",
                "https://" + domain + "/sitemap_index.xml",
                "https://" + domain + "/sitemap1.xml",
                "https://" + domain + "/page-sitemap.xml"
        );

        for (String url : urls) {
            try {
                String xml = HttpUtil.get(url);
                var matcher = Pattern.compile("<loc>(.*?)</loc>").matcher(xml);
                while (matcher.find()) {
                    String loc = matcher.group(1);
                    if (loc.contains(domain)) {
                        String path = new URL(loc).getPath().replaceFirst("^/", "");
                        if (isLikelyDirectory(path)) {
                            String[] parts = path.split("/");
                            StringBuilder cumulative = new StringBuilder();
                            for (String part : parts) {
                                if (!part.isBlank()) {
                                    if (cumulative.length() > 0) cumulative.append("/");
                                    cumulative.append(part);
                                    found.add(cumulative.toString());
                                }
                            }
                        }
                    }
                }
            } catch (Exception ignored) {}
        }

        return found;
    }

    public static Set<String> generateBackupGuesses(String domain) {
        Set<String> guesses = new HashSet<>();

        String base = domain.contains(".") ? domain.split("\\.")[0] : domain;
        String clean = domain.replaceAll("\\W+", "");

        List<String> extensions = List.of("zip", "rar", "tar", "tar.gz", "tgz", "7z", "bak");

        for (String ext : extensions) {
            guesses.add(base + "." + ext);
            guesses.add(clean + "." + ext);
            guesses.add(domain + "." + ext);
        }

        return guesses;
    }



    private static boolean isLikelyDirectory(String path) {
        return !path.matches(".*\\.(css|js|png|jpg|jpeg|gif|svg|ico|woff2?|ttf|eot|html?|xml|pdf|zip|rar|7z|exe)$")
                && !path.contains("..")
                && path.length() > 1;
    }
}