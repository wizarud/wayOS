package com.wayos.connector.speech;

import com.wayos.connector.ResponseObject;

import x.org.json.JSONArray;
import x.org.json.JSONObject;

public class SpeechAPI {
	
	private static SpeechAPI speechAPI;
	
	private String adviceText, pleaseChooseText, andText;
	
	public SpeechAPI(String adviceText, String pleaseChooseText, String andText) {
		
		this.adviceText = adviceText;
		this.pleaseChooseText = pleaseChooseText;
		this.andText = andText;
		
	}

	public static SpeechAPI instance(String adviceText, String pleaseChooseText, String andText) {
		if (speechAPI==null) {
			speechAPI = new SpeechAPI(adviceText, pleaseChooseText, andText);
		}
		return speechAPI;
	}
	
	public String createMessages(ResponseObject responseObject) {
				
		JSONArray array = new JSONArray(responseObject.toJSONString());
		
		StringBuilder sb = new StringBuilder();
		
		JSONObject object, menu, choice;
		JSONArray menus, choices;
		String type;
		
		for (int i=0;i<array.length(); i++) {
			
			object = array.getJSONObject(i);
			
			type = object.getString("type");
			
			if (type.equals("text")) {
				
				sb.append(object.getString("text"));
			}
			
			if (type.equals("menus")) {
				
				menus = object.getJSONArray("menus");
				
				if (menus.length()==1) {
					
					menu = menus.getJSONObject(0);
					
					sb.append(menu.getString("label"));
											
					choices = menu.getJSONArray("choices");
					
					for (int j=0;j<choices.length();j++) {
						
						choice = choices.getJSONObject(j);
						
						if (j==0) {
							
							sb.append(System.lineSeparator() + " ");
							
							if (choices.length()==1) {
								
								sb.append(adviceText);														
								
							} else {
								
								sb.append(pleaseChooseText);														
								
							}
							
						} else if (j==choices.length()-1) {
							
							if (choices.length()>1) {
								
								sb.append(" " + andText + " ");
								
							}
							
						}
						
						sb.append(System.lineSeparator() + " ");
						sb.append(choice.getString("label"));
						
					}
					
				} else {
					
					sb.append("Slide menus is not support!");
					
				}
				
			}
							
			sb.append(System.lineSeparator() + " ");
		}
		
		
		String speech = sb.toString().trim();		
		
		return speech;
	}

}
