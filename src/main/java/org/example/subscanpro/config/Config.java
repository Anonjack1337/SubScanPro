package org.example.subscanpro.config;

import java.util.List;

public class Config {
    public static final String DOMAIN = "example.com";
    public static final String INPUT_FILE = "directories.dat"; // shell.dat
    public static final String OUTPUT_FILE = "working_links.txt";
    public static final int THREADS = 10;
    public static final int DEFAULT_RECURSION_DEPTH = 2;

    public static final List<String> SKIPPED_SUBDOMAINS = List.of("cpanel", "webmail", "autodiscover", "cpcontacts", "mail", "cpcalendars", "webdisk", "whm");
    public static final List<String> SENSITIVE_KEYWORDS = List.of(
            ".env", ".git", ".htpasswd", "config", "config", "db", "sql", "key", "passwd"
    );
    public static final List<String> FOLDER_SCAN_BLACKLIST = List.of(
            ".git", ".htpasswd", "backup", "backup", ".htaccess", "logs", "private"
    );
    public static final List<String> FALLBACK_FOLDERS = List.of(
            "store", "vote", "play", "hiscores", "downloads", "forum", "guides",
            "updates", "panel", "admin", "account", "download", "store2"
    );
}
