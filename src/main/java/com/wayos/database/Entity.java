package com.wayos.database;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;

public class Entity extends JSONObject {
	
	private final String name;
	
	private final String key;
	
	public Entity(String name, String key) {
		
		this.name = name;
		
		this.key = key;
		
	}
	
	public Entity(JSONObject jsonObject, String name, String key) {
		
		super(jsonObject.toMap());
		
		this.name = name;
		
		this.key = key;
		
	}
	
	public String getName() {
		
		return name;
	}

	public String getKey() {
		
		return key;
	}

	public void setProperty(String name, Object value) {
		
		super.put(name, value);
	}
	
	public void setProperty(String name, Collection<?> values) {
		
		super.put(name, values);
	}

	public Map<String, Object> getProperties() {
		
		return super.toMap();
	}

	public Object getProperty(String name) {
		
		Object result = super.opt(name);
		
		if (result instanceof JSONArray) {
			
			JSONArray array = (JSONArray) result;
			
			List<Object> list = new ArrayList<>();
			
			for (int i=0; i<array.length(); i++) {
				list.add(array.get(i));
			}
			
			return list;
		}
		
		return result;
	}

}
