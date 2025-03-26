package com.wayos.drawer.basic;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.UUID;
import java.util.stream.Collectors;

import com.wayos.Context;
import com.wayos.Hook;
import com.wayos.Node;
import com.wayos.drawer.Canvas2D;
import com.wayos.drawer.Drawer;

import x.org.json.JSONObject;

/**
 * 
 * Create Context from format
 * Number, Keywords, Response, Is Question, Next, Expressions, X, Y
 * 
 * @author Wisarut Srisawet
 *
 */
public class DataTableDrawer extends Drawer {
	
	public static final List<String> HEADERS = Arrays.asList("Number", "Keywords", "Answer", "Question", "Next", "Expressions", "W", "X", "Y");
	
	private static void validateHeaders(List<String> headers) {
		
		if (headers.size() != HEADERS.size()) {
			
			throw new IllegalArgumentException("Invalid TSV Column, must be Number	Keywords	Answer	Question	Next	Expressions	W	X	Y");
		}
		
		for (int i=0; i<HEADERS.size(); i++) {
			
			if (!headers.get(i).equals(HEADERS.get(i))) {
				
				throw new IllegalArgumentException("Missing TSV column for position " + i + ", Header must be " + HEADERS.get(i) + "!");
			}
		}
		
	}
	
	public static abstract class TableLoader {
		public abstract List<List<String>> dataTable();
	}
	
	public static class TSVTableLoader extends TableLoader {
		
		private InputStream inputStream;
		
		private String delimeter;
		
		public TSVTableLoader(InputStream inputStream, String delimeter) {
			
			this.inputStream = inputStream;
			this.delimeter = delimeter;
			
		}

		@Override
		public List<List<String>> dataTable() {
			
			List<List<String>> dataTable = new ArrayList<>();
			
			Scanner sc = new Scanner(inputStream);
			
			String line;
			List<String> colList;
			while (sc.hasNextLine()) {

				/*
				dataTable.add(
						Arrays.asList(
							sc.nextLine().split(delimeter)));
							*/

				//Clear Double Quotes, TODO: clear for what???
				
				line = sc.nextLine();
				
				colList = Arrays.asList(
							line.split(delimeter)).stream().map(
								//s -> s.startsWith("\"") && s.endsWith("\"") ? s.substring(1, s.length()-1) : s)
								s -> s.replace("\"", ""))
									.collect(Collectors.toList());
								
				dataTable.add(colList);
			}
									
			sc.close();
			
			List<String> headers = dataTable.get(0);
			
			DataTableDrawer.validateHeaders(headers);			
						
			return dataTable;
		}
		
	}
	
	public static class GoogleSheetsAdapter extends TableLoader {
		
		private List<List<Object>> valueRange;
		
		public GoogleSheetsAdapter(List<List<Object>> valueRange) {
			this.valueRange = valueRange;
		}

		@Override
		public List<List<String>> dataTable() {
			
			List<List<String>> dataTable = new ArrayList<>();
			
			List<String> columnList;
			
			String column;
			
			for (List<Object> objectList: valueRange) {
				
				columnList = new ArrayList<>();
				
				for (int i=0; i < objectList.size(); i++) {
					
					column = objectList.get(i).toString();
					
					if (column.startsWith("\"") && column.endsWith("\"")) {
						column = column.substring(1, column.length()-1);
					}
					
					column = column.trim();
					
					if (i==0 && column.isEmpty()) break;
					
					columnList.add(column);
				}
				
				if (columnList.size() > 1) {
					
					dataTable.add(columnList);					
				}
				
			}
			
			List<String> headers = dataTable.get(0);
			
			DataTableDrawer.validateHeaders(headers);
			
			return dataTable;
		}
		
	}
	
	private List<List<String>> dataTable;
	
	public DataTableDrawer(TableLoader tableLoader) {
		this.dataTable = tableLoader.dataTable();
	}

	@Override
	public void draw(Canvas2D canvas2D) {
		        
		/**
		 * Create Parent Tree
		 */
		Map<String, List<String>> parentListMap = new HashMap<>();
		Map<String, Canvas2D.Entity> entityMap = new HashMap<>();
		
		List<Canvas2D.Entity> leafExpressionEntityList = new ArrayList<>();
		
		String number, keywords, answer, nexts, expressions;
		Boolean isQuestion;
		
		int x = 0;
		int y = 0;
		
		int shiftX = 500;
		int shiftY = 300;
		
		String [] nextTokens, weights;
		List<String> parentList;
		
		Canvas2D.Entity entity;
		
		//Ignore Header
		dataTable.remove(0);
		
		for (List<String> colList: dataTable) {
			
			number = colList.get(0);
			
			if (colList.size()>1)
				keywords = colList.get(1);
			else
				keywords = "";
			
			if (colList.size()>2) {
				answer = colList.get(2).replace("[br]", "\n");
			}
			else
				answer = "";
				
			if (colList.size()>3)
				isQuestion = colList.get(3).equalsIgnoreCase("yes");	
			else
				isQuestion = false;
				
			if (colList.size()>4)
				nexts = colList.get(4);
			else
				nexts = "";
			
			if (nexts.trim().isEmpty()) {
				isQuestion = null; //Reset to undefined if there is no child
			}
			
			if (colList.size()>5) {
								
				expressions = colList.get(5).replace("[br]", "\n");
				
				/**
				 * Remove reserved chars
				 */
				//expressions = expressions.replace("?", ""); Cannot remove because `?<varName>=<varValue>`
				//expressions = expressions.replace("!", "");
				expressions = expressions.replace(",", "");
				
			}
			else
				expressions = "";
			
			/**
			 * Comment below to support empty entity!
			 */
			//if (keywords.isEmpty() && answer.isEmpty() && expressions.isEmpty()) continue;
			
			try {
				weights = colList.get(6).split(" ");
			} catch (Exception e) {
				weights = new String[] {};
			}
			
			try {
				x = (int) Double.parseDouble(colList.get(7));
			} catch (Exception e) {
				x = shiftX;
			}
			
			try {
				y = (int) Double.parseDouble(colList.get(8));
			} catch (Exception e) {
				y = shiftY;
			}  finally {
				shiftY = y + 100;				
			}
						
			if (number.isEmpty()) {
				
				/**
				 * Empty entity, so skip it
				 */
				if (keywords.isEmpty() && answer.isEmpty() && expressions.isEmpty()) {
					
					//Skip
					continue;
				}
				
				/**
				 * Custom Properties
				 */
				if (!keywords.isEmpty() && !answer.isEmpty() && !Context.RESERVED_PROPERTIES.contains(keywords)) {
					
					canvas2D.context.prop(keywords, answer);
					
					//Skip
					continue;
				}
								
			}
			
			nextTokens = nexts.split(",");
			for (String next:nextTokens) {
				next = next.trim();
				if (next.isEmpty() || next.equals(number)) continue;//Skip empty or self forward
				
				parentList = parentListMap.get(next);
				if (parentList==null) {
					parentList = new ArrayList<>();
					parentListMap.put(next, parentList);
				}
				parentList.add(number);
			}
			
			/**
			 * Reserved Properties
			 */
			if (keywords.equals("greeting")) {
				
				entity = canvas2D.GREETING;
				canvas2D.context.prop("greeting", answer);
				
				JSONObject attr = (JSONObject) canvas2D.context.attr("start");
				
				attr.put("x", x);
				attr.put("y", y);
				
				canvas2D.context.attr("start", attr);
				
			} else if (keywords.equals("unknown")) {
					
				entity = canvas2D.UNKNOWN;
				canvas2D.context.prop("unknown", answer);
				
				JSONObject attr = (JSONObject) canvas2D.context.attr("end");
				
				attr.put("x", x);
				attr.put("y", y);
				
				canvas2D.context.attr("end", attr);
				
			} else if (keywords.equals("silent")) {
				
				entity = canvas2D.SILENT;
				canvas2D.context.prop("silent", answer);
				
				JSONObject attr = (JSONObject) canvas2D.context.attr("silent");
				
				attr.put("x", x);
				attr.put("y", y);
				
				canvas2D.context.attr("silent", attr);
				
			} else {
				
				/*
				System.out.println("Create Entity..");
				System.out.println("Keywords:" + keywords);
				System.out.println("Answer:" + answer);
				System.out.println("Expressions:" + expressions);
				System.out.println("isQuestion:" + isQuestion);
				System.out.println("Location:" + x + ", "+ y);
				System.out.println();
				*/
				
				entity = new Canvas2D.Entity(keywords, answer, expressions, isQuestion);
				entity.node.attr("x", x);
				entity.node.attr("y", y);
				
				List<Hook> hookList = entity.node.hookList();
				
				int i = 0;
				for (Hook hook:hookList) {
										
					try {
						//Must skip id
						if (hook.text.startsWith("@")) continue;
						
						hook.weight = Float.parseFloat(weights[i].trim());
						
					} catch (Exception e) {
						
						hook.weight = 1;
						
					} finally {
						
						i++;
					}
					
				}
				
				canvas2D.context.add(entity.node);
				
				/**
				 * To call attachForwarder() later for the leaf entities
				 */
				if (!expressions.trim().isEmpty() && entity.node.type()==Node.Type.LEAF) {
					
					//System.out.println("LEAFEXPR:" + entity.node);
					leafExpressionEntityList.add(entity);
					
				}
				
			}
			
			entityMap.put(number, entity);
						
		}
				
		/**
		 * Update Connections
		 */
		List<Map<String, String>> conntections = (List<Map<String, String>>) canvas2D.context.attr("connections");
        if (conntections == null) {
        	conntections = new ArrayList<>();
            canvas2D.context.attr("connections", conntections);
        }
				
		Canvas2D.Entity parentEntity;
		
		String debug = "";
		
		try {

			String propertyValue;
			
			for (Map.Entry<String, List<String>> entry: parentListMap.entrySet()) {

				debug = entry.getKey();
				entity = entityMap.get(entry.getKey());
				
				//Next number not found!
				if (entity==null) {
					
					continue; //Skip
				}

				for (String parentNumber: entry.getValue()) {

					debug += ":" + parentNumber; 
					parentEntity = entityMap.get(parentNumber);

					entity.node.addHook("@" + parentEntity.id(), Hook.Match.Words);

					Map<String, String> connectionObj = new HashMap<>();
					connectionObj.put("id", UUID.randomUUID().toString());
					connectionObj.put("source", parentEntity.id());
					connectionObj.put("target", entity.id());

					conntections.add(connectionObj);

					if (parentEntity==canvas2D.GREETING) {
						
						canvas2D.GREETING.setQuestion(false);

						propertyValue = canvas2D.context.prop("greeting");
						if (propertyValue==null) {
							propertyValue = "";
						}
						propertyValue = propertyValue.trim();
						
						if (propertyValue.endsWith(", @" + canvas2D.GREETING.id() + canvas2D.GREETING.getMarker())) continue;
							
						canvas2D.context.prop("greeting", propertyValue + ", @" + canvas2D.GREETING.id() + canvas2D.GREETING.getMarker());
						
						canvas2D.context.attr("start", new JSONObject(canvas2D.GREETING.node.attr()));

					} else if (parentEntity==canvas2D.UNKNOWN) {

						canvas2D.UNKNOWN.setQuestion(false);
						
						propertyValue = canvas2D.context.prop("unknown");
						if (propertyValue==null) {
							propertyValue = "";
						}
						propertyValue = propertyValue.trim();
						
						if (propertyValue.endsWith(", @" + canvas2D.UNKNOWN.id() + canvas2D.UNKNOWN.getMarker())) continue;
						
						canvas2D.context.prop("unknown", propertyValue + ", @" + canvas2D.UNKNOWN.id() + canvas2D.UNKNOWN.getMarker());

						canvas2D.context.attr("end", new JSONObject(canvas2D.UNKNOWN.node.attr()));
						
					} else if (parentEntity==canvas2D.SILENT) {

						canvas2D.SILENT.setQuestion(false);
						
						propertyValue = canvas2D.context.prop("silent");
						if (propertyValue==null) {
							propertyValue = "";
						}
						propertyValue = propertyValue.trim();
						
						if (propertyValue.endsWith(", @" + canvas2D.SILENT.id() + canvas2D.SILENT.getMarker())) continue;
						
						canvas2D.context.prop("silent",  propertyValue + ", @" + canvas2D.SILENT.id() + canvas2D.SILENT.getMarker());

						canvas2D.context.attr("silent", new JSONObject(canvas2D.SILENT.node.attr()));
						
					} else {

						parentEntity.attachForwarder();
						
					}

				}

			}
			
			/**
			 * To support expression in leaf entities
			 */
			for (Canvas2D.Entity e:leafExpressionEntityList) {
				
				e.attachExpressionForLeaf();
				
			}

		} catch (Exception e) {
			
			throw new RuntimeException(e + ":" + debug + ":" + entityMap.keySet().size());
		}
 			
	}

}
