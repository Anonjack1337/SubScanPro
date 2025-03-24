# WebProbeJ

**WebProbeJ** is a lightweight multithreaded tool built in Java to scan a target domain and its discovered subdomains for the existence of specific files or paths. Great for reconnaissance, bug bounty, or security research.

## Features

- Fetches subdomains using WhoisXML API
- Scans main and subdomains for potential file paths
- Supports multithreaded concurrent scanning (configurable)
- Logs all successful hits to a file
- Ignores unnecessary or common infrastructure subdomains (like cpanel, webmail)

## Requirements

- Java 11+
- A WhoisXML API key (you can get one from [whoisxmlapi.com](https://whoisxmlapi.com)) 500 API Searches FREE Per Account

## 🛠 Setup

1. Clone or download the project.
2. Create a file named `directories.dat` in the root directory. Each line should be a possible path to test (e.g., `backup.zip`, `.env`, `admin.php`).
3. Open `FileSearcher.java` and replace the API key and domain values.

```java
private static final String API_KEY = "your_api_key_here";
private static final String DOMAIN = "example.com";
