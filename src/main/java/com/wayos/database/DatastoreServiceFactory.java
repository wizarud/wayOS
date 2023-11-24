package com.wayos.database;

import com.wayos.Application;
import com.wayos.PathStorage;

public class DatastoreServiceFactory {
	
	public static DatastoreService getDatastoreService() {
		
		PathStorage pathStorage = (PathStorage) Application.instance().get(PathStorage.class.getName());
		
		return new DatastoreService(pathStorage);
		
	}

}
