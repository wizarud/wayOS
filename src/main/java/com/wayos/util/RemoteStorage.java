package com.wayos.util;

import javax.net.ssl.HttpsURLConnection;

import com.google.common.io.ByteStreams;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

/**
 * Created by eoss-th on 8/15/17.
 */
public class RemoteStorage {
	
	/**
	 * GAE Storage Service Servlet Endpoint URL, Must endswith /
	 */	
	private String storageURL;
	
	private SignatureValidator signatureValidator;
	
	public void setStorageURL(String storageURL) {
		this.storageURL = storageURL;
	}
	
	public void setBrainySecret(String brainySecret) {		
        this.signatureValidator = new SignatureValidator(brainySecret.getBytes());    	
	}
	    
    public String read(String path) {
        StringBuilder sb = new StringBuilder();
        BufferedReader br = null;
        try {
            String signed = signatureValidator.generateSignature(path.getBytes());
            
            URL url = new URL(storageURL + path);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setUseCaches(false);            
            conn.addRequestProperty("Brainy-Signature", "555" + signed);

            br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String line;
            while ((line = br.readLine())!=null) {
                sb.append(line);
            }
            
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (br!=null) try { br.close(); } catch (Exception e) {}
        }
        return sb.toString();
    }
    
    public void write(String text, String path) {
        InputStream in = null;
        OutputStreamWriter out = null;
        try {
        	
            String signed = signatureValidator.generateSignature(path.getBytes());
            
            HttpsURLConnection connection = (HttpsURLConnection) new URL(storageURL + path).openConnection();
            connection.setDoOutput(true);
            connection.addRequestProperty("Brainy-Signature", "555" + signed);

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
    
    public void write(InputStream inputStream, String fileName) throws Exception {
        InputStream in = null;
        try {
            String signed = signatureValidator.generateSignature(fileName.getBytes());
        	
            HttpsURLConnection connection = (HttpsURLConnection) new URL(storageURL + fileName).openConnection();
            connection.setDoOutput(true);
            connection.addRequestProperty("Brainy-Signature", "555" + signed);
            
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
