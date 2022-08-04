package com.wayos.util;

import com.google.common.io.ByteStreams;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;

/**
 * Created by eossth on 8/22/2017 AD.
 */
public class FileStream extends Stream {

    private File dir;

    private String name;

    public FileStream(File dir, String name) {
        this.dir = dir;
        this.name = name;
    }

    public FileStream(String name) {
        this(null, name);
    }

    public File getDir() {
        if (dir.isDirectory())
            return dir;
        return null;
    }

    private File getFile() {
        File file;
        if (dir!=null&&dir.isDirectory()) {
            file = new File(dir, name);
        } else {
            file = new File(name);
        }
        return file;
    }

    @Override
    public String read() {

        StringBuilder sb = new StringBuilder();
        BufferedReader br = null;
        try {
            br = new BufferedReader(new InputStreamReader(new FileInputStream(getFile()), StandardCharsets.UTF_8));
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

        OutputStreamWriter out = null;
        try {

            out = new OutputStreamWriter(new FileOutputStream(getFile()), StandardCharsets.UTF_8);
            out.write(text);

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (out!=null) try { out.close(); } catch (IOException e) {}
        }

    }

    @Override
    public void write(InputStream inputStream) throws Exception {
        try {
            ByteStreams.copy(inputStream, new FileOutputStream(getFile()));
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

}
