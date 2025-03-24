package org.example.subscanpro;

import org.example.subscanpro.config.Config;
import org.example.subscanpro.core.DirectoryLoader;
import org.example.subscanpro.core.Scanner;
import org.example.subscanpro.network.SubdomainFetcher;
import org.example.subscanpro.output.OutputManager;

import java.io.IOException;
import java.nio.file.Path;
import java.util.LinkedHashSet;
import java.util.Set;

public class Main {
    public static void main(String[] args) throws IOException {
        int maxDepth = args.length > 0 ? Integer.parseInt(args[0]) : Config.DEFAULT_RECURSION_DEPTH;

        Set<String> directories = DirectoryLoader.loadFromFile(Config.INPUT_FILE);
        Path output = OutputManager.prepareOutputFile(Config.OUTPUT_FILE);

        scanDomainWithFolders(Config.DOMAIN, directories, output, maxDepth);

        Set<String> subdomains = SubdomainFetcher.getSubdomains(Config.DOMAIN);
        if (!subdomains.isEmpty()) {
            System.out.println("[+] Found subdomains:");
            for (String sub : subdomains) {
                String fqdn = sub + "." + Config.DOMAIN;
                System.out.println(" → " + fqdn);

                scanDomainWithFolders(fqdn, directories, output, maxDepth);
            }
        } else {
            System.out.println("[!] No subdomains found.");
        }

        Scanner.shutdown();
        System.out.println("Scan complete. Results saved to " + Config.OUTPUT_FILE);
    }

    private static void scanDomainWithFolders(String domain, Set<String> directories, Path output, int maxDepth) {
        Set<String> folderSeeds = DirectoryLoader.scrapeHomepage(domain);
        folderSeeds.addAll(DirectoryLoader.parseSitemap(domain));

        Set<String> validFolders = new LinkedHashSet<>();
        for (String folder : folderSeeds) {
            String fullUrl = "https://" + domain + "/" + folder;
            if (org.example.subscanpro.network.HttpUtil.urlExists(fullUrl)) {
                validFolders.add(folder);
            }
        }

        if (!validFolders.isEmpty()) {
            System.out.println("[✓] Discovered " + validFolders.size() + " valid folders:");
            validFolders.forEach(folder -> System.out.println(" → " + domain + "/" + folder));
        } else {
            System.out.println("[✓] No valid folders discovered.");
        }


        System.out.println("[+] " + domain + " — Found " + validFolders.size() + " folders to scan into.");
        Scanner.scanDomain(domain, directories, output, maxDepth);

        Set<String> backupGuesses = DirectoryLoader.generateBackupGuesses(domain);

        for (String guess : backupGuesses) {
            String url = "https://" + domain + "/" + guess;
            Scanner.scanSingleUrl(url, output, true);
        }

        Scanner.scanDomain(domain, directories, output, maxDepth);

        for (String folder : folderSeeds) {
            String base = domain + "/" + folder;
            String fullUrl = "https://" + base;

            if (!org.example.subscanpro.network.HttpUtil.urlExists(fullUrl)) {
                continue;
            }

            Scanner.scanDomain(base, directories, output, maxDepth);
        }
    }

}
