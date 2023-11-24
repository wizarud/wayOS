package com.wayos.experiment.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Container {
	
	private List<Entity> entityList;
	
	private Map<String, List<Entity>> keyEntityListMap;
	
	public Container() {
		
		entityList = new ArrayList<>();
		
		keyEntityListMap = new HashMap<>();
	}
	
	public void add(Entity entity) {
		
		entityList.add(entity);
		
		List<String> keyList = entity.keyList();
		
		/**
		 * Merge Key
		 */
		List<Entity> entityList;
		for (String key:keyList) {
			
			entityList = keyEntityListMap.get(key);
			
			if (entityList==null) {
				entityList = new ArrayList<>();
			}
			
			entityList.add(entity);
			
			keyEntityListMap.put(key, entityList);
		}
				
	}
	
	public Result call(String message) {
		
		
		
		return null;
	}

}
