package com.wayos.util;

import com.google.common.io.ByteStreams;

import javax.net.ssl.HttpsURLConnection;
import java.io.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;

/**
 * Created by eossth on 8/22/2017 AD.
 */
public class RemoteStream extends Stream {

    public static final String URL_STORAGE_TXT = "https://eoss-chatbot.appspot.com/s/";
    public static final String URL_STORAGE_BIN = "https://eoss-chatbot.appspot.com/bin/";

    private String fileName;

    public RemoteStream(String fileName) {
        this.fileName = fileName;
    }

    public String getURL() {
        return URL_STORAGE_BIN + fileName;
    }

    @Override
    public String read() {

        StringBuilder sb = new StringBuilder();
        BufferedReader br = null;
        try {
            br = new BufferedReader(new InputStreamReader(new URL(URL_STORAGE_TXT + fileName).openStream(), StandardCharsets.UTF_8));
            String line;
            while (true) {
                line = br.readLine();
                if (line!=null)
                    sb.append(line);
                else
                    break;
                sb.append(System.lineSeparator());
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (br!=null) try { br.close(); } catch (Exception e) {}
        }
        return sb.toString();
    }

    @Override
    public void write(String text) {

        InputStream in = null;
        OutputStreamWriter out = null;
        try {
            HttpsURLConnection connection = (HttpsURLConnection) new URL(URL_STORAGE_TXT + fileName).openConnection();
            connection.setDoOutput(true);

            out = new OutputStreamWriter(connection.getOutputStream(), StandardCharsets.UTF_8);
            out.write(text);
            out.close();

            in = connection.getInputStream();

            in.close();

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (out!=null) try { out.close(); } catch (IOException e) {}
            if (in!=null) try { in.close(); } catch (IOException e) {}
        }

    }

    @Override
    public void write(InputStream inputStream) throws Exception {
        InputStream in = null;
        try {
            HttpsURLConnection connection = (HttpsURLConnection) new URL(URL_STORAGE_TXT + fileName).openConnection();
            connection.setDoOutput(true);
            ByteStreams.copy(inputStream, connection.getOutputStream());
            in = connection.getInputStream();
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        } finally {
            if (in!=null) try { in.close(); } catch (Exception e) {}
        }

    }
}
