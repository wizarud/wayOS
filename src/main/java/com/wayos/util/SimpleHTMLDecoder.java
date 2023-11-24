package com.wayos.util;

public class SimpleHTMLDecoder {
	
	private static SimpleHTMLDecoder simpleHTMLDecoder;
	
	private SimpleHTMLDecoder() {}
	
	public static SimpleHTMLDecoder instance() {
		if (simpleHTMLDecoder==null) {
			simpleHTMLDecoder = new SimpleHTMLDecoder();			
		}
		return simpleHTMLDecoder;
	}
	
	public String decode(String content) {
		
    	content = content.replace("&nbsp;", " ");
    	content = content.replace("&#42;", "*");
    	content = content.replace("&#43;", "+");
    	content = content.replace("&#44;", ",");
    	content = content.replace("&#45;", "-");
    	content = content.replace("&#46;", ".");
    	content = content.replace("&#47;", "/");
    	content = content.replace("&#96;", "`");
    	content = content.replace("&#35;", "#");
    	
    	System.out.println("CONTENT:" + content);
    	
    	return content;		
	}

}
