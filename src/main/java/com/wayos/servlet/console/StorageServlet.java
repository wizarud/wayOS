package com.wayos.servlet.console;

import java.io.IOException;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.fileupload.FileItemIterator;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.servlet.ServletFileUpload;

/**
 * For uploading to specific path only
 * @author apple
 *
 */
@SuppressWarnings("serial")
@WebServlet("/console/storage/*")
public class StorageServlet extends ConsoleServlet {
	
	@Override
	public void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {

		String requestURI = req.getRequestURI();
		
		String [] paths = requestURI.split("/", 4);		
		
		String fileName = paths[3];
		
		ServletFileUpload upload = new ServletFileUpload();
		
		try {
			
			FileItemIterator iterator = upload.getItemIterator(req);
			
			while (iterator.hasNext()) {
				/**
				 * TODO: why output to same path for all items
				 */
				storage().write(iterator.next().openStream(), fileName);
			}
			
		} catch (FileUploadException e) {
			
			throw new RuntimeException(e);

		}
		
	}
	
}