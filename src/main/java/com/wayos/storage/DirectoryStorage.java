package com.wayos.storage;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.json.JSONObject;

import com.wayos.PathStorage;

public class DirectoryStorage implements PathStorage {
	
	private static final int BUFFER_SIZE = 5 * 1024 * 1024;
	
	private final File home;
	
	public DirectoryStorage(String homeDirectory) {
		
		home = new File(homeDirectory);
		
	}

	@Override
	public List<String> listObjectsWithPrefix(String directoryPrefix) {
		
		if (directoryPrefix.contains("..")) throw new IllegalArgumentException("Illegal Path:" + directoryPrefix);
		
		File dir = new File(home, directoryPrefix);
				
		if (!dir.isDirectory()) return new ArrayList<>();
		
		return Arrays.asList(dir.list());
	}

	@Override
	public void serve(String path, HttpServletResponse resp) throws IOException {
		
		if (path.contains("..")) throw new IllegalArgumentException("Illegal Path:" + path);
		
	    String suffix = path;
	    
    	if (suffix.endsWith(".png")) {
    		
    		resp.setHeader("Content-Type", "image/png");
    		
    	} else if (suffix.endsWith("jpg")) {
    		
    		resp.setHeader("Content-Type", "image/jpeg");
    		
    	} else if (suffix.endsWith("gif")) {
    		
    		resp.setHeader("Content-Type", "image/gif");
    		
    	} else if (suffix.endsWith(".mp3") || suffix.endsWith(".m4a")) {
    		
    		resp.setHeader("Content-Type", "audio/mpeg");
    		
    	} else if (suffix.endsWith(".mp4")) {
    		
    		resp.setHeader("Content-Type", "video/mp4");
    		
    	} else if (suffix.endsWith(".mpeg")) {
    		
    		resp.setHeader("Content-Type", "video/mpeg");
    		
    	} else if (suffix.endsWith(".pdf")) {
    		
    		resp.setHeader("Content-Type", "application/pdf");
    		
    	} else if (suffix.endsWith(".ppt")) {
    		
        	resp.setHeader("Content-Type", "application/vnd.ms-powerpoint");
        	
    	} else if (suffix.endsWith(".pptx")) {
    		
        	resp.setHeader("Content-Type", "application/vnd.openxmlformats-officedocument.presentationml.presentation");
        	
    	} else if (suffix.endsWith(".doc")) {
    		
        	resp.setHeader("Content-Type", "application/msword");
        	
    	} else if (suffix.endsWith(".docx")) {
    		
        	resp.setHeader("Content-Type", "application/vnd.openxmlformats-officedocument.wordprocessingml.document");
        	
    	} else if (suffix.endsWith(".mid") || suffix.endsWith(".midi")) {
    		
        	resp.setHeader("Content-Type", "audio/midi");
        	
    	} else if (suffix.endsWith(".zip")) {
    		
        	resp.setHeader("Content-Type", "application/zip");
        	
    	} else if (suffix.endsWith(".htm") || suffix.endsWith(".html")) {
    		
    		resp.setCharacterEncoding("UTF-8");
        	resp.setHeader("Content-Type", "text/html");
        	
    	} else if (suffix.endsWith(".txt") || suffix.endsWith(".context")) {
    		
    		resp.setCharacterEncoding("UTF-8");
    		resp.setHeader("Content-Type", "text/plain");
    		
    	}
    	
		File fromFile = new File(home, path);
		
		//if (!fromFile.exists()) throw new IllegalArgumentException("File not Found! at" + path);
		
		copy(new FileInputStream(fromFile), resp.getOutputStream());
    
	}

	@Override
	public JSONObject readAsJSONObject(String path) {
		
		if (path.contains("..")) throw new IllegalArgumentException("Illegal Path:" + path);
		
		File fromFile = new File(home, path);
		
    	InputStream inputStream = null;
    	
        try {
        	
        	inputStream = new FileInputStream(fromFile);
        	 
	        return new JSONObject(IOUtils.toString(inputStream, StandardCharsets.UTF_8.name()));
	        					
		} catch (Exception e) {
			
			/* For Debugging only
			e.printStackTrace();
			
			throw(new RuntimeException(e));
			*/
						
		} finally {
			
			if (inputStream!=null) {
				
				try { inputStream.close(); } catch (IOException e) { }
			}
					
		}
        
        return null;
		
	}
	
	/**
	 * Relative path from home directory
	 * Create directories if not exists!
	 * @param path
	 * @return
	 */
	private File placeFileAt(String path) {
		
		if (path.contains("..")) throw new IllegalArgumentException("Illegal Path:" + path);
		
		String dir;
		String fileName;
		
		if (path.contains("/")) {
			
			fileName = path.substring(path.lastIndexOf("/") + 1);
			
			dir = path.substring(0, path.lastIndexOf("/"));
			
		} else {
			
			fileName = path;
			
			dir = null;
			
		}
		
		File parent = home;
		
		/**
		 * Relative path to home directory
		 */
		if (dir!=null) {
			
			parent = new File(home, dir);
			
			if (!parent.exists()) {
				
				parent.mkdirs();
			}
			
		}
		
		return new File(parent, fileName);	
	}

	@Override
	public void write(String content, String path) {
		
		if (path.contains("..")) throw new IllegalArgumentException("Illegal Path:" + path);
		
		try {
			
			copy(IOUtils.toInputStream(content, StandardCharsets.UTF_8.name()), new FileOutputStream(placeFileAt(path)));

		} catch (Exception e) {
			
			throw new RuntimeException(e);
		}
				
	}

	@Override
	public void write(InputStream input, String path) throws IOException {
		
		if (path.contains("..")) throw new IllegalArgumentException("Illegal Path:" + path);
		
		copy(input, new FileOutputStream(placeFileAt(path)));	
	}

	@Override
	public void write(String path, OutputStream outputStream) throws IOException {
		
		if (path.contains("..")) throw new IllegalArgumentException("Illegal Path:" + path);
		
		File fromFile = new File(home, path);
		
		copy(new FileInputStream(fromFile), outputStream);
		
	}
	
	/**
	 * Transfer the data from the inputStream to the outputStream. Then close both streams.
	 */
	private void copy(InputStream input, OutputStream output) throws IOException {
				
		try {
			
			byte[] buffer = new byte[BUFFER_SIZE];
			int bytesRead = input.read(buffer);
			
			while (bytesRead != -1) {
				
				output.write(buffer, 0, bytesRead);
				bytesRead = input.read(buffer);
			}
			
		} finally {
			
			input.close();

			output.close();
		}
	}
	
	@Override
	public boolean exists(String path) {
		
		return new File(home, path).exists();
	}
	
	public static void main(String [] args) {
		
		DirectoryStorage storage = new DirectoryStorage(".");
		
		JSONObject test = new JSONObject();
		test.put("name", "Hello");
		
		storage.write(test.toString(), "../test/wizarud@gmail.com/test.json");
		
		JSONObject test2 = storage.readAsJSONObject("../test/wizarud@gmail.com/test.json");
		
		System.out.println(test2);
		
	}

}
