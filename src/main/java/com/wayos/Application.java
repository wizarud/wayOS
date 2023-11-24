package com.wayos;

import java.util.HashMap;
import java.util.Map;

/**
 * Facade Helper Class for accessing single instance services
 * 
 * @author Wisarut Srisawet
 *
 */
public class Application {
	
	private static Application application;
	
	private Map<String, Object> serviceMap;
	
	private Application() {
		
		serviceMap = new HashMap<>();
	}
	
	public static Application instance() {
		
		if (application==null) {
			application = new Application();
		}
		
		return application;
	}
	
	public void register(String name, Object service) {
		
		serviceMap.put(name, service);
	}
	
	public <T> T get(Class<T> type) {
		
		return type.cast(serviceMap.get(type.getName()));
	}
	
	public Object get(String name) {
		
		return serviceMap.get(name);
	}

}
