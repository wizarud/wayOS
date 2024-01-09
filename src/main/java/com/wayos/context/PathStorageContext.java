package com.wayos.context;

import java.io.BufferedReader;
import java.io.OutputStreamWriter;
import java.util.List;

import com.wayos.Configuration;
import com.wayos.Context;
import com.wayos.PathStorage;
import com.wayos.Node;

@SuppressWarnings("serial")
public class PathStorageContext extends Context {
	
	private final PathStorage storage;
	
	public PathStorageContext(PathStorage storage, String name) {
		super(name);
		this.storage = storage;
	}
	
	/**
	 * Map to local gcs path
	 * @param name
	 * @return
	 */
	private String fileName(String name) {
		Configuration configuration = new Configuration(name);
		return configuration.contextPath();	
	}

	@Override
	protected void doLoad(String name) throws Exception {
		
        lock.readLock().lock();
        
		BufferedReader br = null;
		
        try {
        	
        	loadJSON(storage.readAsJSONObject(fileName(name)).toString());
        	
        } catch (Exception e) {
        	
            e.printStackTrace();
            throw e;
            
        } finally {
        	
            if (br!=null) try { br.close(); } catch (Exception e) {} 
            lock.readLock().unlock();
            
        }
	}

	@Override
	protected void doSave(String name, List<Node> nodeList) {
		OutputStreamWriter out = null;
		try {            
            storage.write(toJSONString(), fileName(name));
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (out!=null) try { out.close(); } catch (Exception e) {}
        }
	}

}
