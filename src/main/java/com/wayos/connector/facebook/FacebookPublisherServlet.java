package com.wayos.connector.facebook;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.restfb.DefaultFacebookClient;
import com.restfb.FacebookClient;
import com.restfb.Parameter;
import com.restfb.Version;
import com.restfb.types.GraphResponse;
import com.restfb.types.send.SendResponse;
import com.wayos.Configuration;
import com.wayos.command.talk.Choice;
import com.wayos.command.talk.Question;
import com.wayos.connector.ResponseObject;
import com.wayos.servlet.console.ConsoleServlet;

import x.org.json.JSONObject;

@WebServlet("/publish")
public class FacebookPublisherServlet extends ConsoleServlet {
		
	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		
		req.setCharacterEncoding("UTF-8");
		
		String pageId = req.getParameter("pageId");
		
		String message = req.getParameter("message");
				
		String link = req.getParameter("link");
		
		String imageURL = req.getParameter("imageURL");
		
		//Hot Fix for test image posting
		if (imageURL==null && message!=null) {
			int indexOfStartPeakGa = message.indexOf("{");
			int indexOfEndPeakGa = message.indexOf("}");
			if (indexOfStartPeakGa!=-1 && indexOfEndPeakGa!=-1 && indexOfStartPeakGa<indexOfEndPeakGa) {
				imageURL = message.substring(indexOfStartPeakGa, indexOfEndPeakGa + 1);
				message = message.replace(imageURL, "");
				imageURL = imageURL.replace("{", "");
				imageURL = imageURL.replace("}", "");
			}
		}

        String result = publish(pageId, message, link, imageURL);
        
        if (result!=null) {
            resp.getWriter().print(result);        	
        }
    
	}
	
    public String publish(String pageId, String message, String link, String imageURL) {
    	
		String facebookJSONPath = Configuration.facebookACTPath(pageId);
		
		JSONObject configObject = storage().readAsJSONObject(facebookJSONPath);;
		
		if (configObject==null) throw new RuntimeException("Facebook Config Not Found!" + pageId);
		
		String pageAccessToken = configObject.getString("pageAccessToken");
		
        FacebookClient pageClient = new DefaultFacebookClient(pageAccessToken, Version.LATEST);

        String appsecret_proof = pageClient.obtainAppSecretProof(pageAccessToken, Configuration.facebook_appSecret);
		
        GraphResponse sendResponse;
        
        if (imageURL!=null) {
        	
        	if (message==null) {
        		message = "";
        	}
        	
            sendResponse = pageClient.publish(pageId + "/photos", GraphResponse.class, 
            		Parameter.with("url", imageURL),
            		Parameter.with("caption", message),
                    Parameter.with("appsecret_proof", appsecret_proof));
            
        } else if (link!=null) {
        	        	
        	if (message==null) {
        		message = "Click!";
        	}
        	
            sendResponse = pageClient.publish(pageId + "/feed", GraphResponse.class, 
            		Parameter.with("message", message),
            		Parameter.with("link", link),
                    Parameter.with("appsecret_proof", appsecret_proof));
        } else {
        	
            sendResponse = pageClient.publish(pageId + "/feed", GraphResponse.class, 
            		Parameter.with("message", message),
                    Parameter.with("appsecret_proof", appsecret_proof));
            
        }
        
        /**
         * <pageId>_<postId>
         * Ex. 
         * 1944963402244810_849768130147955
         * 1944963402244810_849777216813713
         */
        return sendResponse.getId();
    }

	public void _publish(String pageId, String questionURL, ResponseObject responseObject) {
				
		String facebookJSONPath = Configuration.facebookACTPath(pageId);
		
		JSONObject configObject = storage().readAsJSONObject(facebookJSONPath);;
		
		if (configObject==null) throw new RuntimeException("Facebook Config Not Found!" + pageId);		
		
		String pageAccessToken = configObject.getString("pageAccessToken");
		
        FacebookClient pageClient = new DefaultFacebookClient(pageAccessToken, Version.LATEST);

        String appsecret_proof = pageClient.obtainAppSecretProof(pageAccessToken, Configuration.facebook_appSecret);
		
        class ImageWithCaption {
        	final String imageURL;
        	final String caption;
        	ImageWithCaption(String imageURL, String caption) {
        		this.imageURL = imageURL;
        		this.caption = caption;
        	}
        }
        
        List<ImageWithCaption> imageList = new ArrayList<>();
        
        List<String> linkList = new ArrayList<>();
        
        StringBuilder text = new StringBuilder();
        
        String choices, imageURL;
        
        for (Object message:responseObject.messageList) {
        	
            if (message instanceof List) {

            	List<Question> questionList = (List<Question>) message;
                for (Question q:questionList) {
                	
                	choices = "";
                	for (Choice choice:q.choices) {
                		choices += questionURL + choice + "\n";
                	}
                	
                	//Insert text as caption to all question images
                	choices = text.toString() + "\n\n" + choices.trim();
                	
                    if (q.hasImage()) {
                    	
                        imageURL = q.imageURL;
                        
                    } else {
                    	/**
                    	 * Default Image
                    	 */
                        imageURL = /*Configuration.domain + */ "/images/muay_smiling.png";
                    }
                	
                	imageList.add(new ImageWithCaption(imageURL, choices));
                
                }

            } else if (message instanceof ResponseObject.Image) {
            	
            	//Insert text as caption for all images
            	imageList.add(new ImageWithCaption(message.toString(), text.toString()));
            	
            } else {
            	
            	if (message.toString().startsWith("https://")) {
                	
                	linkList.add(message.toString());                	
                }
            	
            	text.append(message + "\n\n");
                
            }
            
        }
        
        /**
         * Publish as images
         */
        if (!imageList.isEmpty()) {
        	        	        	
        	SendResponse sendResponse;
        	
        	for (ImageWithCaption imageWithCaption:imageList) {
            	sendResponse = pageClient.publish(pageId + "/photos", SendResponse.class, 
            			Parameter.with("url", imageWithCaption.imageURL),
            			Parameter.with("caption", imageWithCaption.caption),
            			Parameter.with("appsecret_proof", appsecret_proof));        		
        	}
        	
        	return;
        } 
        
        if (!linkList.isEmpty()) {
        	
        	String message = text.toString();
        	
        	/**
        	 * Remove all links from message
        	 */
        	for (String link:linkList) {
        		message = message.replace(link, "");
        	}
        	
        	message = message.trim();
        	
        	if (message.isEmpty()) {
        		message = "คลิ้ก!";
        	}
        	
        	SendResponse sendResponse;
        	
        	for (String link:linkList) {
                sendResponse = pageClient.publish(pageId + "/feed", SendResponse.class, 
                		Parameter.with("message", message),
                		Parameter.with("link", link),
                        Parameter.with("appsecret_proof", appsecret_proof));
        	}
        	
        	return;        	
        	
        }
        
        /**
         * Publish as text
         */
        if (text.length()!=0) {
        	
        	SendResponse sendResponse = pageClient.publish(pageId + "/feed", SendResponse.class, 
            		Parameter.with("message", text.toString().trim()),
                    Parameter.with("appsecret_proof", appsecret_proof));
        	
        }

	}	

}
