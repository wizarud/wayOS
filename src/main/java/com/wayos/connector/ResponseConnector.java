package com.wayos.connector;

public abstract class ResponseConnector {
	
	protected final RequestObject requestObject;
	
	public ResponseConnector(RequestObject requestObject) {
	
		this.requestObject = requestObject;
	}
	
	public abstract String execute(ResponseObject responseObject);
	
}
