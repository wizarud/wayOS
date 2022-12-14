package com.wayos.servlet.console;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.fileupload.FileItemIterator;
import org.apache.commons.fileupload.FileItemStream;
import org.apache.commons.fileupload.servlet.ServletFileUpload;

import com.wayos.Configuration;
import com.wayos.Context;
import com.wayos.drawer.Canvas2D;
import com.wayos.drawer.Drawer;
import com.wayos.drawer.basic.DataTableDrawer;
import com.wayos.drawer.basic.QuestionareDrawer;
import com.wayos.drawer.basic.VerticallyDrawer;
import com.wayos.drawer.ecommerce.CSVPaginationCatalogDrawer;
import com.wayos.drawer.ecommerce.CSVPaginationCatalogImporter;

@SuppressWarnings("serial")
@WebServlet("/console/factory")
public class BotUploadFactoryServlet extends ConsoleServlet {
	
	final List<Map<String, String>> createMapList(HttpServletRequest request, String...keys) {
		
		List<Map<String, String>> mapList = new ArrayList<>();
		
		//Find max row
		String [] vals;
		int maxRow = Integer.MIN_VALUE;
		for (String key:keys) {
			vals = request.getParameterValues(key);
			if (vals==null) continue;
			if (vals.length > maxRow) {
				maxRow = vals.length;
			}				
		}
		
		//Allocate empty table
		for (int i=0; i<maxRow; i++) {
			mapList.add(new HashMap<>());
		}
		
		Map<String, String> map;		
		int row;		
		for (String key:keys) {
			vals = request.getParameterValues(key);			
			if (vals==null) continue;
			row = 0;
			for (String val:vals) {				
				map = mapList.get(row++);
				map.put(key, val);
			}						
		}
		
		return mapList;
	}
	
	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		HttpSession session = request.getSession();
				
		String accountId = (String) session.getAttribute("accountId");
		
		try {
							    	
			ServletFileUpload upload = new ServletFileUpload();
			
			FileItemIterator iterator = upload.getItemIterator(request);
					
			FileItemStream fileItemStream;
			
			Drawer drawer = null;
			
			String itemName = "";
									
			String errMsg = "unknown error";
			
			if (iterator.hasNext()) {
				
				fileItemStream = iterator.next();
				
				/**
				 * Ex. 103014451870896/Michael-Yong.context/new.tsv
				 */
				itemName = fileItemStream.getName();
								
				String contextName = "";
				
				//TSV
				if (itemName.endsWith("/new.tsv")) {
					
					contextName = itemName.substring(0, itemName.lastIndexOf("/new.tsv"));
							
					try {
						
						//Try with Basic TSV First
			        	drawer = new DataTableDrawer(new DataTableDrawer.TSVTableLoader(fileItemStream.openStream(), "\t"));
			        	
					} catch (Exception e) {
						
						errMsg = e.getMessage();
												
					}
		        	
				} else if (itemName.endsWith("/catalog.tsv")) {
					
					contextName = itemName.substring(0, itemName.lastIndexOf("/catalog.tsv"));
					
					try {
						
						//Read to buffer because multiple reader
						InputStream inputStream = fileItemStream.openStream();						
						byte [] buffer = new byte[inputStream.available()];
						inputStream.read(buffer);
						
						CSVPaginationCatalogImporter csvPaginationCatalogImporter = new CSVPaginationCatalogImporter(null, new ByteArrayInputStream(buffer), "\t");
						
			        	drawer = new CSVPaginationCatalogDrawer(contextName, csvPaginationCatalogImporter);
			        	
			        	/**
			        	 * Incase of catalog, We save the TSV file for reuse the SKUs, Desc information
			        	 */
			    		String catalogTSVPath = Configuration.PRIVATE_PATH + contextName + ".catalog.tsv";
			    		
						storage().write(new ByteArrayInputStream(buffer), catalogTSVPath);
			        	
					} catch (Exception e) {
						
						throw new RuntimeException(e);
						
					}
				}
				//Questionare
				else if (itemName.endsWith("/qa.tsv")) {
					
					contextName = itemName.substring(0, itemName.lastIndexOf("/qa.tsv"));
					
					//Read to buffer because multiple reader
					InputStream inputStream = fileItemStream.openStream();						
					byte [] buffer = new byte[inputStream.available()];
					inputStream.read(buffer);
					
					String qa = new BufferedReader(
						      new InputStreamReader(new ByteArrayInputStream(buffer), StandardCharsets.UTF_8))
						        .lines()
						        .collect(Collectors.joining("\n"));	
						        
		        	drawer = new QuestionareDrawer(qa);					
		        	
		        	/**
		        	 * Incase of catalog, We save the TSV file for reuse the SKUs, Desc information
		        	 */
		    		String qaTSVPath = Configuration.PRIVATE_PATH + contextName + ".qa.tsv";
		    		
					storage().write(new ByteArrayInputStream(buffer), qaTSVPath);
					
				} 
				//RAW Text
				else if (itemName.endsWith("/lines.tsv")) {
					
					contextName = itemName.substring(0, itemName.lastIndexOf("/lines.tsv"));
					
					String lines = new BufferedReader(
						      new InputStreamReader(fileItemStream.openStream(), StandardCharsets.UTF_8))
						        .lines()
						        .collect(Collectors.joining("\n"));	
					
					drawer = new VerticallyDrawer(Arrays.asList(lines.split("\n")), 5);
				}
				
				/**
				 * is this account is the owner of contextName
				 */
				if (!contextName.startsWith(accountId)) throw new IllegalArgumentException("You are not owner of this " + contextName);
				
				if (drawer!=null) {
					
					Context context = sessionPool().getContext(contextName);
					
					context.load();
					
			    	String title = context.prop("title");
			    	
					Canvas2D canvas2D = new Canvas2D(context, title, 100, true);
					
					drawer.draw(canvas2D);
					
		            context.save();
		            
		            context.load();
		            
		            response.getWriter().print("Done!");
		            
					return;
				}
				
	            response.getWriter().print(errMsg + ":" + itemName);
			}
			
		} catch (Exception e) {
			
			e.printStackTrace(response.getWriter());
		}				
		
	}	

}
