package com.vdom.utils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.zip.GZIPInputStream;

public class Downloader {
    public static byte[] getBytes(URL url) throws IOException {
        HttpURLConnection connection = getHttpURLConnection(url, 0);
        byte[] bytes = getBytes(connection);
        connection.disconnect();
        return bytes;
    }

    private static byte[] getBytes(HttpURLConnection connection) throws IOException {
        InputStream inputStream = connection.getInputStream();
        if (connection.getContentEncoding() == "gzip" && !(inputStream instanceof GZIPInputStream)) {
            inputStream = new GZIPInputStream(inputStream);
        }

        int n;
        byte[] buffer = new byte[4096];
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        while ((n = inputStream.read(buffer)) > 0) {
            baos.write(buffer, 0, n);
        }
        byte[] bytes  = baos.toByteArray();

        baos.close();
        inputStream.close();
        return bytes;
    }

    private static final HttpURLConnection getHttpURLConnection(URL url, int cycle) throws IOException {
        HttpURLConnection connection = (HttpURLConnection)url.openConnection();
        HttpURLConnection.setFollowRedirects(true);
        connection.setDoInput(true);
        connection.setDoOutput(false);
        connection.setRequestProperty("User-agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/83.0.4103.116 Safari/537.36");
        connection.setConnectTimeout(30000);
        connection.setReadTimeout(30000);
        connection.setUseCaches(false);
        connection.connect();

        String location = connection.getHeaderField("Location");
        if (location != null) {
            connection.disconnect();
            if (cycle < 5) {
                return getHttpURLConnection(new URL(location), cycle + 1);
            } else {
                throw new IOException("Too many redirects for " + url);
            }
        }
        return connection;
    }
}
