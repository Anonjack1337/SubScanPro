package org.webprobej.network;

import org.webprobej.config.Config;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;

public class SubdomainFetcher {
    public static Set<String> getSubdomains(String domain) {
        Set<String> subdomains = new HashSet<>();
        try {
            String apiUrl = "https://api.hackertarget.com/hostsearch/?q=" + domain;
            HttpURLConnection conn = (HttpURLConnection) new URL(apiUrl).openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("User-Agent", "Mozilla/5.0");

            if (conn.getResponseCode() != 200) {
                System.err.println("[!] HackerTarget subdomain API returned status: " + conn.getResponseCode());
                return subdomains;
            }

            BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.contains(",")) continue;
                String subdomain = line.split(",")[0].trim();

                if (subdomain.endsWith("." + domain)) {
                    String sub = subdomain.replace("." + domain, "");
                    if (!Config.SKIPPED_SUBDOMAINS.contains(sub) && !"www".equals(sub)) {
                        subdomains.add(sub);
                    }
                }
            }
            reader.close();
        } catch (Exception e) {
            System.err.println("Failed to fetch subdomains: " + e.getMessage());
        }
        return subdomains;
    }
}