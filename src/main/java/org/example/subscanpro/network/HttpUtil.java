package org.example.subscanpro.network;

import javax.net.ssl.*;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;

public class HttpUtil {

    // Flag to trust all certs if fallback fails (optional)
    private static boolean trustAllSSL = false;

    static {
        if (trustAllSSL) {
            trustAllCertificates();
        }
    }

    public static boolean urlExists(String url) {
        try {
            HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
            conn.setRequestMethod("GET");
            conn.setInstanceFollowRedirects(false);
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(5000);

            int code = conn.getResponseCode();

            if (code >= 300 && code < 400) {
                String location = conn.getHeaderField("Location");
                if (location != null && (location.contains("/home") || location.equals("/") || location.contains("index"))) {
                    return false;
                }
            }

            if (code == 200) {
                String contentType = conn.getContentType();
                byte[] contentBytes = conn.getInputStream().readAllBytes();
                String content = new String(contentBytes);

                if (content.toLowerCase().contains("can not load controller index")) {
                    return false;
                }

                if (contentType != null && contentType.contains("text/html") && content.length() < 500) {
                    return false;
                }

                return true;
            }

            return false;

        } catch (SSLException e) {
            if (url.startsWith("https://")) {
                String fallbackUrl = url.replaceFirst("https://", "http://");
                return urlExists(fallbackUrl);
            }
            return false;
        } catch (Exception e) {
            return false;
        }
    }

    public static String get(String url) throws IOException {
        try {
            HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(5000);

            if (conn.getResponseCode() != 200) throw new IOException("Non-200 response");

            return new String(conn.getInputStream().readAllBytes());

        } catch (SSLException e) {
            if (url.startsWith("https://")) {
                String fallbackUrl = url.replaceFirst("https://", "http://");
                return get(fallbackUrl);
            }
            throw new IOException("SSL failure with no fallback: " + url);
        }
    }
    private static void trustAllCertificates() {
        try {
            SSLContext ctx = SSLContext.getInstance("TLS");
            TrustManager[] trustAll = new TrustManager[]{
                    new X509TrustManager() {
                        public void checkClientTrusted(X509Certificate[] xcs, String string) {}
                        public void checkServerTrusted(X509Certificate[] xcs, String string) {}
                        public X509Certificate[] getAcceptedIssuers() { return new X509Certificate[0]; }
                    }
            };
            ctx.init(null, trustAll, new SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(ctx.getSocketFactory());
            HttpsURLConnection.setDefaultHostnameVerifier((hostname, session) -> true);
        } catch (Exception ignored) {}
    }
}
