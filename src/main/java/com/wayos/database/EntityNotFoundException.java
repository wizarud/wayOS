package com.wayos.database;

public class EntityNotFoundException extends Exception {

	public EntityNotFoundException(String entity, String key) {
		super("Entity not found " + entity + "/" + key);
	}

}
