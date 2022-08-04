package com.wayos.experiment.axow;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.TreeMap;

public class DNA {
	
    private String path;
    
    private String delimiter;
    
	private Map<String, List<File>> catMap;
	
	public DNA(String path, String delimiter) {
		
	    URL url = DNA.class.getClassLoader().getResource(path);
	    File resourceDir = null;
	    try {
	    	resourceDir = new File(url.toURI());
	    } catch (URISyntaxException e) {
	    	resourceDir = new File(url.getPath());
	    }
		
		if (resourceDir==null || !resourceDir.isDirectory()) throw new IllegalArgumentException("Unknown image resource path " + path);
		
		catMap = new TreeMap<>();
		
		File [] files = resourceDir.listFiles();			
		String cat;
		List<File> fileList;
		for (File file:files) {
			
			try {
				
				cat = file.getName().split(delimiter)[0];
				fileList = catMap.get(cat);
				if (fileList==null) {
					fileList = new ArrayList<>();
					catMap.put(cat, fileList);
				}
				fileList.add(file);
				
			} catch (Exception e) {
				continue; //skip it
			}
		}
		
		if (catMap.isEmpty()) throw new IllegalArgumentException("Empty resources for " + path);
				
		this.path = path;
		this.delimiter = delimiter;
	}
	
	
	/**
	 * Decode from X-XX-X-XX-XX-X-X-X pattern
	 * Ex. A-A1-A-AA-AA-A-A-A
	 * @param code
	 * @return resource paths
	 */
	public String [] decode(String code) {
		
		/**
		 * Random gen
		 */
		if (code==null) {
			code = randomGen();
		} 
		
		String [] codeArray;
				
		codeArray = code.split("-");
		
		if (codeArray.length!=catMap.keySet().size()) throw new IllegalArgumentException("Invalid Code: " + code);
		
		String [] resourceArray = new String[codeArray.length];
		
		int i = 0;
		for (Map.Entry<String, List<File>> entry:catMap.entrySet()) {
			resourceArray[i] = "/" + path + "/" + entry.getKey() + delimiter + codeArray[i] + ".png";	
			i++;
		}
		
		return resourceArray;
	}
	
	public String randomGen() {
		
		String code = "";
		Random random = new Random();
		
		Set<Map.Entry<String, List<File>>> entrySet = catMap.entrySet();
		int size = entrySet.size();
		int i = 0;
		String fileName;
		for (Map.Entry<String, List<File>> entry:entrySet) {
			fileName = entry.getValue().get(random.nextInt(entry.getValue().size())).getName();
			fileName = fileName.split(delimiter)[1];
			fileName = fileName.split("\\.")[0];
			code += fileName;
			if (i<size-1) {
				code += delimiter;
			}
			i++;
		}
		
		return code;
	}
	
	public String encode(String [] resourceArray) {
		
		if (resourceArray.length != catMap.keySet().size()) throw new IllegalArgumentException("Invalid Resources");
		
		String code = "";
		
		int i = 0;
		for (String resource:resourceArray) {
			
			resource = resourceArray[i];
			resource = resource.replace(path, "");
			resource = resource.split(delimiter)[1];
			resource = resource.split("\\.")[0];
			
			code += resource;
			
			if (i<resourceArray.length-1) {
				code += "-";
			}
			
			i++;
		}
					
		return code;
	}
	
    public static void main(String[]args) {
    	
    	String path = "digitart.singers.v2";
    	DNA dnaResolver = new DNA(path, "-");
    	
    	String code = dnaResolver.randomGen();
    	
    	code = "A-O1-W-OA-XW-X-W-A";
    	
    	System.out.println(code);
    	
    	AXOW axow = AXOW.interpret(code);
    	
    	AXOW.debug(axow);
    	    	    	
    }
    	

}