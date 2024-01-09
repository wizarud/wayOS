package com.wayos.context;

import com.wayos.Configuration;
import com.wayos.Context;
import com.wayos.Node;
import com.wayos.util.SignatureValidator;

import javax.net.ssl.HttpsURLConnection;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Created by eoss-th on 8/15/17.
 */
public class RemoteContext extends Context {
	
    /**
     * Storage URL Pattern: https://<your instance id>.appspot.com/s/
     */
    private static final String defaultDataURL = "https://{ storageBucket }/s/";
    
    private static final String defaultLibPath = Configuration.LIB_PATH;
    
    private final String dataURL;
    
    private final String libPath;
    
    private SignatureValidator signatureValidator;
    
    public RemoteContext(String name) {
    	
    	this (name, Configuration.brainySecret);
    	
    }
    
    public RemoteContext(String name, String brainySecret) {
    
        this(name, brainySecret, defaultDataURL, defaultLibPath);
        
    }
    
    public RemoteContext(String name, String brainySecret, String dataURL, String libPath) {
    	
        super(name);
        
        if (brainySecret!=null) {
            this.signatureValidator = new SignatureValidator(brainySecret.getBytes());        	
        }
        
        this.dataURL = dataURL;
        this.libPath = libPath;
    }

    @Override
    public void doLoad(String name) throws Exception {
    	
        BufferedReader br = null;
        
        try {
        	
        	String fullContextName = libPath + name + SUFFIX;
            
            URL url = new URL(dataURL + fullContextName);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setUseCaches(false);
            
            if (signatureValidator!=null) {
                String signed = signatureValidator.generateSignature(fullContextName.getBytes());        	
                conn.addRequestProperty("Brainy-Signature", "555" + signed);            	
            }

            br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            StringBuilder sb = new StringBuilder();
            String line;
            
            while ((line = br.readLine())!=null) {
            	
                sb.append(line);
            }
            
            loadJSON(sb.toString());
            
        } catch (Exception e) {
        	
            throw e;
            
        } finally {
        	
            if (br!=null) try { br.close(); } catch (Exception e) {}
        }
    }

    @Override
    public void doSave(final String name, final List<Node> nodeList) {

        try {
        	
        	String fullContextName = libPath + name + SUFFIX;
            
            HttpsURLConnection connection = (HttpsURLConnection) new URL(dataURL + fullContextName).openConnection();
            connection.setDoOutput(true);
            
            if (signatureValidator!=null) {
                String signed = signatureValidator.generateSignature(fullContextName.getBytes());            	
                connection.addRequestProperty("Brainy-Signature", "555" + signed);
            }
            
            OutputStreamWriter out = new OutputStreamWriter(connection.getOutputStream(), StandardCharsets.UTF_8);
            out.write(toJSONString());
            out.flush();
            out.close();
            
            InputStream in = connection.getInputStream();
            in.close();
            
        } catch (IOException e) {
        	
        	throw new RuntimeException(e);
        }
    }

}
