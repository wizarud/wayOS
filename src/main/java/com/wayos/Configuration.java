package com.wayos;

public class Configuration {
		
	/**
	 * Environment configuration
	 */
    public static final String domain = System.getenv("domain");
        
	public static final String brainySecret = System.getenv("brainySecret");    
	
	public static final String facebook_appId = System.getenv("facebook_appId");
	
	public static final String facebook_appSecret = System.getenv("facebook_appSecret");
	
	public static final String adminContextName = System.getenv("ADMIN_CONTEXT_NAME");
	
	public static final String contextHome = System.getenv("contextHome");
		
	/**
	 * Storage relative path from contextName configuration
	 */
	public static final String LIB_PATH = "libs/";
	
	public static final String LOGS_PATH = "logs/";
	
	public static final String VARS_PATH = "vars/";
	
	public static final String MESSAGES_PATH = "messages/";
	
	public static final String PRIVATE_PATH = "private/";
	
	public static final String PUBLIC_PATH = "public/";
	
	public static final String USER_PATH = "users/";
	
	public static final String DATASTORE_PATH = "datastore/";
	
	/**
	 * contextName = <accountId>/<botId>
	 */
	private final String contextName;
	
	public Configuration(String contextName) {
		
		this.contextName = contextName;
	}
	
	public String contextPath() {
		
		return LIB_PATH + contextName + ".context";
	}
	
	public String linePath() {
		
		return PRIVATE_PATH + contextName + ".line.json";
	}
	
	public String adminIdPath() {
		
		return PRIVATE_PATH + contextName + ".adminId.json";
	}
	
	public String lineAdminIdPath() {
		
		return PRIVATE_PATH + contextName + ".line.adminId.json";
	}
	
	public String facebookPageIdPath() {
		
		return PRIVATE_PATH + contextName + ".facebook.pageId.json";
	}
	
	public String facebookAdminIdPath() {
		
		return PRIVATE_PATH + contextName + ".facebook.adminId.json";
	}
	
	public static String facebookACTPath(String facebookPageId) {
		
		return PRIVATE_PATH + facebookPageId + ".facebook.json";		
	}
	
	public String vars(String channel, String sessionId) {
		
		return VARS_PATH + contextName + "/" + channel + "/" + sessionId + ".json";
	}

}
