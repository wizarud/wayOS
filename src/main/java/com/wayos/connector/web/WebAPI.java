package com.wayos.connector.web;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.text.StringEscapeUtils;

import com.wayos.command.talk.Choice;
import com.wayos.command.talk.Question;
import com.wayos.connector.ResponseObject;

public class WebAPI {
	
	private static WebAPI _webAPI;
	
	private WebAPI() {}
	
	public static WebAPI instance() {
		if (_webAPI==null) {
			_webAPI = new WebAPI();
		}
		return _webAPI;
	}
	
	public String createMessages(ResponseObject responseObject) {
		
		StringBuilder messages = new StringBuilder();
		
		List<Object> messageList = responseObject.messageList;
		
		ResponseObject.Text text;
		ResponseObject.Image image;
		ResponseObject.Audio audio;
		ResponseObject.Video video;
		List<Question> questionList;
		
		for (Object msg:messageList) {
			
			if (msg instanceof ResponseObject.Text) {
				
				text = (ResponseObject.Text) msg;				
				messages.append(createText(text.toString()));
				
			} else if (msg instanceof ResponseObject.Image) {
				
				image = (ResponseObject.Image) msg;
				//messages.append(createImage(image.toString()));
				messages.append(image.toString());
				
			} else if (msg instanceof ResponseObject.Audio) {
				
				audio = (ResponseObject.Audio) msg;
				messages.append(createAudio(audio.toString()));
				
			} else if (msg instanceof ResponseObject.Video) {
				
				video = (ResponseObject.Video) msg;
				messages.append(createVideo(video.toString()));
				
			} else {
				
				questionList = (List<Question>) msg;
				if (questionList.size()>1) {
					
    				messages.append("<div style=\"overflow: auto; white-space: nowrap;\">");
				}
				
				Choice defaultChoice;
				String clickEvent;				
	        	for (Question question:questionList) {
	        			        		
	        		if (questionList.size()>1) {
	        			
	    				messages.append("<div align=\"center\" style=\"_direction_\">");
	        		}
    				
	        		if (!question.choices.isEmpty()) {
	        			
		        		defaultChoice = question.choices.get(0);//Default Choice for image and label touch
		        		
        				if (defaultChoice.isLinkLabel() || defaultChoice.isImageLinkLabel()) {
        					
        					clickEvent = "window.open('" + defaultChoice.linkURL + "', '_blank')";
        							
        				} else {
		        		
        					clickEvent = "wayOS.parse('" + StringEscapeUtils.escapeHtml4(defaultChoice.parent + " " + defaultChoice.label) + "')";
        				}
	        			
	        		} else {
	        			
	        			clickEvent = "";
	        		}	        		
					
            		if (question.hasImage()) {
            			
        				messages.append(createImage(question.imageURL, clickEvent));
        				
                    } else if (questionList.size() > 1) {
            			
                    	/**
                    	 * Default Image for Slide Menu Only
                    	 */
        				messages.append(createImage("/images/gigi.png", clickEvent));
            			
            		}
            		
            		messages.append("<div align=\"center\" onclick=\"" + clickEvent + "\"><h3 class=\"wayos_label\">" + createText(question.label) + "</h3></div>");

	    			if(!question.choices.isEmpty()) {
	    				
	    				if (question.choices.size()>9) {
	    					
			    			messages.append("\n\n\n");	    					
			    			
	    				} 
	    				
	    				messages.append("<div align=\"center\">");
	    				
	        			for (Choice choice:question.choices) {
	        				
	        				if (choice.isLinkLabel() || choice.isImageLinkLabel()) {
	        					
	        					messages.append("<a href=\"" + choice.linkURL + "\" target=\"_blank\"><div class=\"wayos_menu_item\">" + choice.label + "</div></a>");
	        					
	        				} else {
	        					
	        					clickEvent = "wayOS.parse('" + StringEscapeUtils.escapeHtml4(choice.parent + " " + choice.label) + "')";
	        					
	        					messages.append("<div class=\"wayos_menu_item\" onclick=\"" + clickEvent + "\">" + choice.label + "</div>");
	        				}
	        			}
	        			messages.append("</div>");
	    			}
	    			
	        		if (questionList.size()>1) {
	    				messages.append("</div>");				  
	        		} else {
		    			messages.append("\n\n\n");	        			
	        		}
	        	}
	        	
				if (questionList.size()>1) {
    				messages.append("</div>");				  
				}
	        	
			}
			messages.append("\n\n\n");
		}
		
		return messages.toString().trim();
	}    
	
	private String createText(String text) {
		
        //String result = text.replace("<", "&lt;").replace(">", "&gt;");
        
        //result = result.replace("\n", "<br/>");
		
		//return result;
		
		return text;
	}
	
	private String createImage(String imageURL, String linkTo) {
		
		StringBuilder result = new StringBuilder();
		result.append("<div class=\"wayos_image_head\" onclick=\"" + linkTo + "\" style=\"background-image: url('" + imageURL + "');\"></div>");
		return result.toString();
	}

	private String createAudio(String audioURL) {
		
		StringBuilder result = new StringBuilder();		
		result.append("<audio controls style=\"width:95%\"><source src=\"" + audioURL +"\" type=\"audio/mp4\"></audio>");
		return result.toString();
	}
	
	private String createVideo(String videoURL) {
		
		StringBuilder result = new StringBuilder();
		result.append("<video controls style=\"width:95%\"><source src=\"" + videoURL +"\" type=\"video/mp4\"></video>");
		return result.toString();
	}
}
