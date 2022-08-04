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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by eoss-th on 8/15/17.
 */
public class RemoteContext extends Context {
	
    private SignatureValidator signatureValidator;
    
    private final ExecutorService executorService;

    /**
     * Storage URL Pattern: https://<your instance id>.appspot.com/s/
     */
    private static final String dataURL = "https://" + Configuration.storageBucket + "/s/";
    
    private RemoteContext(String name, String brainySecret) {
        this(name, brainySecret, Executors.newFixedThreadPool(1));
    }
    
    public RemoteContext(String name) {
    	this (name, Configuration.brainySecret);
    }
    
    public RemoteContext(String name, String brainySecret, ExecutorService executorService) {
        super(name);
        this.signatureValidator = new SignatureValidator(brainySecret.getBytes());
        this.executorService = executorService;
    }

    @Override
    public void doLoad(String name) throws Exception {
        BufferedReader br = null;
        try {
        	String fullContextName = name + SUFFIX;
            String signed = signatureValidator.generateSignature(fullContextName.getBytes());        	
            
            URL url = new URL(dataURL + fullContextName);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setUseCaches(false);
            
            conn.addRequestProperty("Brainy-Signature", "555" + signed);

            br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine())!=null) {
                sb.append(line);
            }
            loadJSON(sb.toString());
            
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        } finally {
            if (br!=null) try { br.close(); } catch (Exception e) {}
        }
    }

    @Override
    public void doSave(final String name, final List<Node> nodeList) {

        if (executorService==null)
            doFutureSave(name, nodeList);
        else
            executorService.execute(
                    new Runnable() {
                        @Override
                        public void run() {
                            doFutureSave(name, nodeList);
                        }
                    }
            );
    }

    private void doFutureSave(String name, List<Node> nodeList) {
        try {
        	String fullContextName = name + SUFFIX;
            String signed = signatureValidator.generateSignature(fullContextName.getBytes());
            
            HttpsURLConnection connection = (HttpsURLConnection) new URL(dataURL + fullContextName).openConnection();
            connection.setDoOutput(true);
            
            connection.addRequestProperty("Brainy-Signature", "555" + signed);
            
            OutputStreamWriter out = new OutputStreamWriter(connection.getOutputStream(), StandardCharsets.UTF_8);
            out.write(toJSONString());
            out.flush();
            out.close();
            InputStream in = connection.getInputStream();
            in.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
