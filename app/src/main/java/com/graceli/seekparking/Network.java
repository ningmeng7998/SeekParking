package com.graceli.seekparking;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class Network {

    public String getJsonString(String markUrl) throws IOException {
        URL url = new URL(markUrl);
        HttpURLConnection HttpURLConnection = (HttpURLConnection) url.openConnection();
        HttpURLConnection.setRequestMethod("GET");
        InputStream InputStream = HttpURLConnection.getInputStream();
        String jsonString = readFully(InputStream);
        return jsonString;
    }

    public String readFully(InputStream entityResponse) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int length = 0;
        while ((length = entityResponse.read(buffer)) != -1) {
            baos.write(buffer, 0, length);
        }
        return baos.toString();
    }

}
