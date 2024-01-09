package com.wayos.database;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.NotImplementedException;

import com.wayos.Configuration;
import com.wayos.PathStorage;

import x.org.json.JSONObject;

public class DatastoreService {
	
	private static interface Operation {
		
		boolean matched(Object propertyValue, Object value);		
		
	}
	
	private static class GreatherThan implements Operation {
		
		@Override
		public boolean matched(Object propertyValue, Object value) {
			
			throw new NotImplementedException(">");
		}
		
	}
	
	private static class GreatherOrEqualsThan implements Operation {
		
		@Override
		public boolean matched(Object propertyValue, Object value) {
			
			throw new NotImplementedException(">=");
		}
		
	}
	
	private static class LowerThan implements Operation {
		
		@Override
		public boolean matched(Object propertyValue, Object value) {
			
			throw new NotImplementedException("<");
		}
		
	}
	
	private static class LowerOrEqualsThan implements Operation {
		
		@Override
		public boolean matched(Object propertyValue, Object value) {
			
			throw new NotImplementedException("<=");
		}
		
	}
	
	private static class Equals implements Operation {
		
		@Override
		public boolean matched(Object propertyValue, Object value) {
			
			return propertyValue.equals(value);
		}
		
	}
	
	public static enum Condition {
		
		GT(new GreatherThan()), 
		GTE(new GreatherOrEqualsThan()), 
		LT(new LowerThan()), 
		LTE(new LowerOrEqualsThan()), 
		EQ(new Equals());
		
		private final Operation operation;
		
		private Condition(Operation operation) {
			this.operation = operation;
		}

		boolean matched(Object propertyValue, Object value) {
			
			return operation.matched(propertyValue, value);
		}
		
		
	}
	
	public static class Query {
				
		public final String propertyName;
		
		public final Condition condition;
		
		public final Object propertyValue;		
		
		public Query(String propertyName, Condition condition, Object propertyValue) {
			
			this.propertyName = propertyName;
			
			this.condition = condition;
			
			this.propertyValue = propertyValue;
		}
		
		public boolean matched(Entity entity) {
			
			return condition.matched(propertyValue, entity.get(propertyName));
		}
		
	}
	
	private final PathStorage pathStorage;
	
	public DatastoreService(PathStorage pathStorage) {
		
		this.pathStorage = pathStorage;
		
	}

	public Entity get(String name, String key) throws EntityNotFoundException {		
		
		JSONObject jsonObject = pathStorage.readAsJSONObject(Configuration.DATASTORE_PATH + name + "/" + key + ".json");
		
		if (jsonObject==null) {
			
			throw new EntityNotFoundException(name, key);
		}
		
		return new Entity(jsonObject, name, key);
	}

	public void put(Entity entity) {
		
		pathStorage.write(entity.toString(), Configuration.DATASTORE_PATH + entity.getName() + "/" + entity.getKey() + ".json");
		
	}

	public List<Entity> asList(String name, Query...queries) {
		
		List<String> keyFileNameList = pathStorage.listObjectsWithPrefix(Configuration.DATASTORE_PATH + name);
		
		Entity entity;
		
		List<Entity> entityList = new ArrayList<>();
		String key;
		for (String keyFileName:keyFileNameList) {
			
			try {
				key = keyFileName.replace(".json", "");
				entity = get(name, key);
				
				/**
				 * Do the logical queries with AND condition
				 */
				boolean matched = true;
				
				if (queries!=null) {
					
					for (Query q:queries) {
						
						if (!q.matched(entity)) {
							matched = false;
							break;
						}
						
					}
						
				}
				
				if (matched) {
					entityList.add(entity);
				}
				
			} catch (EntityNotFoundException e) {
				e.printStackTrace();
				continue;
			}
			
		}
		
		
		return entityList;
	}	

}
