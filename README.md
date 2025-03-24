# WebProbeJ

A powerful Java-based reconnaissance tool for discovering:

- Valid directories and files across domains and subdomains
- Sitemap and homepage-extracted folders
- Common backup file names (e.g. `.zip`, `.rar`, `.tar.gz`)
- Sensitive files like `.env`, `config.js`, `apikey.txt`, etc.

---

## ✨ Features

- Scans root domain + all discovered subdomains
- Parses sitemap and homepage for hidden folders
- Flags sensitive files with alerts
- Scans using customizable wordlists (`directories.dat`)
- Auto-generates backup filename guesses per domain
- Avoids false positives via intelligent filtering (e.g. redirect detection)

---

## ⚙️ Configuration

Edit the `Config.java` file to set:

```java
public static final String DOMAIN = "example.com";
public static final String INPUT_FILE = "directories.dat";
public static final String OUTPUT_FILE = "working_links.txt";