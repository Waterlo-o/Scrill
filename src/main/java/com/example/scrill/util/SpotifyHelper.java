package com.example.scrill.util;

import java.io.*;
import java.net.*;
import java.util.*;

public class SpotifyHelper {

    private static final String SPOTIFY_CLIENT_ID = "YOUR_CLIENT_ID_HERE";
    private static final String SPOTIFY_CLIENT_SECRET = "YOUR_CLIENT_SECRET_HERE";

    // --- ПОЛУЧЕНИЕ НАЗВАНИЯ ТРЕКА ПО ССЫЛКЕ SPOTIFY ---
    public static String getTitleFromSpotify(String spotifyUrl) {
        try {
            String cleanUrl = spotifyUrl;
            if (cleanUrl.contains("?")) cleanUrl = cleanUrl.substring(0, cleanUrl.indexOf("?"));

            //System.out.println("[Spotify] Притворяемся ботом соцсетей...");

            URL url = new URL(cleanUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("User-Agent", "facebookexternalhit/1.1 (+http://www.facebook.com/externalhit_uatext.php)");
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(5000);

            if (conn.getResponseCode() == 200) {
                BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
                StringBuilder html = new StringBuilder();
                String line;
                while ((line = in.readLine()) != null) html.append(line);
                in.close();

                String fullTitle = extractWithRegex(html.toString(), "<title>(.*?)</title>");
                if (fullTitle != null && !fullTitle.equalsIgnoreCase("Spotify")) {
                    fullTitle = fullTitle
                            .replace(" | Spotify", "")
                            .replace(" - song and lyrics by ", " ")
                            .replace(" - song by ", " ")
                            .replace(" - Single by ", " ")
                            .replace(" - EP by ", " ")
                            .replace(" - Album by ", " ");
                    fullTitle = decodeSpotifyText(fullTitle).trim();
                    //System.out.println("[Spotify] Успех! Название: " + fullTitle);
                    return fullTitle;
                }
            }

            // Фоллбек на oEmbed API
            //System.out.println("[Spotify] Бот не прошел, пробуем oEmbed API...");
            String oembedBase = "https://open.s" + "potify.com/oembed?url=";
            String encodedUrl = URLEncoder.encode(cleanUrl, "UTF-8");

            URL apiurl = new URL(oembedBase + encodedUrl);
            HttpURLConnection apiconn = (HttpURLConnection) apiurl.openConnection();
            apiconn.setRequestMethod("GET");
            apiconn.setRequestProperty("User-Agent", "Mozilla/5.0");

            if (apiconn.getResponseCode() == 200) {
                BufferedReader in = new BufferedReader(new InputStreamReader(apiconn.getInputStream(), "UTF-8"));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = in.readLine()) != null) response.append(line);
                in.close();

                String json = response.toString();
                String title = extractWithRegex(json, "\"title\"\\s*:\\s*\"(.*?)\"");
                String artist = extractWithRegex(json, "\"author_name\"\\s*:\\s*\"(.*?)\"");

                if (title != null) {
                    title = decodeSpotifyText(title);
                    String finalQuery = title;
                    if (artist != null) finalQuery += " " + decodeSpotifyText(artist);
                    //System.out.println("[Spotify] Запрос из API: " + finalQuery);
                    return finalQuery.trim();
                }
            }
        } catch (Exception e) {
            //System.out.println("[Spotify] Ошибка: " + e.getMessage());
        }
        return null;
    }

    // --- OAUTH2 ТОКЕН ПОЛЬЗОВАТЕЛЯ ---
    public static String getSpotifyUserToken() throws Exception {
        java.util.concurrent.CountDownLatch latch = new java.util.concurrent.CountDownLatch(1);
        String[] authCode = new String[1];

        com.sun.net.httpserver.HttpServer server = com.sun.net.httpserver.HttpServer.create(
                new InetSocketAddress("127.0.0.1", 8888), 0);
        server.createContext("/callback", exchange -> {
            String query = exchange.getRequestURI().getQuery();
            if (query != null && query.startsWith("code=")) {
                authCode[0] = query.substring(5).split("&")[0];
                String response = "<html><body style='font-family: sans-serif; text-align: center; margin-top: 50px;'>" +
                        "<h2 style='color: #B388FF;'>Успешно!</h2>" +
                        "<p>Scrill Player получил доступ. Можете закрыть эту вкладку.</p>" +
                        "<script>setTimeout(() => window.close(), 2000);</script></body></html>";
                exchange.getResponseHeaders().add("Content-Type", "text/html; charset=UTF-8");
                exchange.sendResponseHeaders(200, response.getBytes("UTF-8").length);
                exchange.getResponseBody().write(response.getBytes("UTF-8"));
            } else {
                String response = "Ошибка авторизации.";
                exchange.sendResponseHeaders(400, response.getBytes().length);
                exchange.getResponseBody().write(response.getBytes());
            }
            exchange.close();
            latch.countDown();
        });
        server.start();

        String redirectUri = "http://127.0.0.1:8888/callback";
        String authUrl = "https://accounts.s" + "potify.com/authorize" +
                "?client_id=" + SPOTIFY_CLIENT_ID +
                "&response_type=code" +
                "&redirect_uri=" + URLEncoder.encode(redirectUri, "UTF-8") +
                "&scope=playlist-read-private%20playlist-read-collaborative";

        java.awt.Desktop.getDesktop().browse(new URI(authUrl));

        boolean success = latch.await(60, java.util.concurrent.TimeUnit.SECONDS);
        server.stop(0);

        if (!success || authCode[0] == null) {
            throw new Exception("Авторизация прервана или истекло время ожидания.");
        }

        String credentials = Base64.getEncoder().encodeToString(
                (SPOTIFY_CLIENT_ID + ":" + SPOTIFY_CLIENT_SECRET).getBytes());
        String requestBody = "grant_type=authorization_code" +
                "&code=" + authCode[0] +
                "&redirect_uri=" + URLEncoder.encode(redirectUri, "UTF-8");

        java.net.http.HttpRequest tokenReq = java.net.http.HttpRequest.newBuilder()
                .uri(URI.create("https://accounts.s" + "potify.com/api/token"))
                .header("Authorization", "Basic " + credentials)
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(java.net.http.HttpRequest.BodyPublishers.ofString(requestBody))
                .build();

        java.net.http.HttpClient client = java.net.http.HttpClient.newHttpClient();
        java.net.http.HttpResponse<String> tokenRes = client.send(
                tokenReq, java.net.http.HttpResponse.BodyHandlers.ofString());

        if (tokenRes.statusCode() != 200) {
            throw new Exception("Не удалось обменять код на токен. Код: " + tokenRes.statusCode());
        }

        org.json.JSONObject json = new org.json.JSONObject(tokenRes.body());
        return json.getString("access_token");
    }

    // --- ВСПОМОГАТЕЛЬНЫЕ МЕТОДЫ ---
    public static String extractWithRegex(String text, String regex) {
        java.util.regex.Matcher matcher = java.util.regex.Pattern.compile(regex).matcher(text);
        return matcher.find() ? matcher.group(1) : null;
    }

    public static String decodeSpotifyText(String text) {
        if (text == null) return null;
        return text.replace("\\u0026", "&")
                .replace("\\\"", "\"")
                .replace("\\/", "/")
                .replace("\\'", "'")
                .replace("&amp;", "&")
                .replace("&#039;", "'")
                .replace("&quot;", "\"");
    }

    public static String decodeJsonUnicode(String input) {
        if (input == null) return "";
        try {
            StringBuilder sb = new StringBuilder();
            int i = 0;
            while (i < input.length()) {
                char c = input.charAt(i);
                if (c == '\\' && i + 1 < input.length() && input.charAt(i + 1) == 'u') {
                    String hex = input.substring(i + 2, i + 6);
                    sb.append((char) Integer.parseInt(hex, 16));
                    i += 6;
                } else {
                    sb.append(c);
                    i++;
                }
            }
            return sb.toString().replace("\\\"", "\"").replace("\\/", "/");
        } catch (Exception e) {
            return input;
        }
    }

    public static String extractJsonProperty(String json, String key) {
        int start = json.indexOf(key);
        if (start != -1) {
            start += key.length();
            int end = json.indexOf("\"", start);
            if (end != -1) return json.substring(start, end);
        }
        return null;
    }


    public static String getClientToken() throws Exception {
        String credentials = java.util.Base64.getEncoder().encodeToString(
                (SPOTIFY_CLIENT_ID + ":" + SPOTIFY_CLIENT_SECRET).getBytes());

        java.net.http.HttpRequest req = java.net.http.HttpRequest.newBuilder()
                .uri(java.net.URI.create("https://accounts.spotify.com/api/token"))
                .header("Authorization", "Basic " + credentials)
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(java.net.http.HttpRequest.BodyPublishers.ofString(
                        "grant_type=client_credentials"))
                .build();

        java.net.http.HttpResponse<String> res = java.net.http.HttpClient.newHttpClient()
                .send(req, java.net.http.HttpResponse.BodyHandlers.ofString());

        org.json.JSONObject json = new org.json.JSONObject(res.body());
        return json.getString("access_token");
    }
}