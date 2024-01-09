package com.wayos;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import x.org.json.JSONObject;

public interface PathStorage {

	List<String> listObjectsWithPrefix(String directoryPrefix);

	void serve(String filePath, HttpServletResponse resp) throws IOException;

	/**
	 * Return null if not found
	 * @param path
	 * @return
	 */
	JSONObject readAsJSONObject(String path);

	void write(String content, String toPath);

	void write(InputStream input, String toPath) throws IOException;
	
	void write(String fromPath, OutputStream outputStream) throws IOException;

	boolean exists(String catalogTSVPath);
	
	void delete(String path);

}
