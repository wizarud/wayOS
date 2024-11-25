package com.wayos.connector.facebook;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;

import com.restfb.DefaultFacebookClient;
import com.restfb.FacebookClient;
import com.restfb.Parameter;
import com.restfb.Version;
import com.restfb.types.User;
import com.wayos.Configuration;
import com.wayos.Session;
import com.wayos.connector.http.HttpRequestObject;

import x.org.json.JSONArray;
import x.org.json.JSONObject;

public class FacebookHttpRequestObject extends HttpRequestObject {

	private String appSecret = System.getenv("facebook_appSecret");
	
    /**
     * Facebook PageAccessToken for response
     */
    protected String pageAccessToken;
    
    /**
     * FAcebook recipeintId (pageId)
     */
    protected String recipientId;
    
    protected String recipientType;
    
	private User userProfile;
	
	private String commentName;
    
	public FacebookHttpRequestObject(HttpServletRequest request) {
		
		super(request, null);
				
    	if (appSecret==null) throw new IllegalArgumentException("Missing AppSecret");
    	
    	//setSignature(request.getHeader("Brainy-Signature")); For what???
    	
    	FacebookSignatureValidator facebookSignatureValidator = new FacebookSignatureValidator(appSecret.getBytes());
        
        try {

            String headerSignature = request.getHeader("X-Hub-Signature");

            String requestBody = request.getReader().lines().collect(Collectors.joining(System.lineSeparator()));
            
            if (facebookSignatureValidator.validateSignature(requestBody.getBytes(), headerSignature)) {
            	
                JSONObject requestObj = new JSONObject(requestBody);

                if (requestObj.getString("object").equals("page")) {

                	try {
                		
                        JSONObject messageObj = requestObj.getJSONArray("entry").getJSONObject(0).getJSONArray("messaging").getJSONObject(0);

                        sessionId = messageObj.getJSONObject("sender").getString("id");

                        recipientId = messageObj.getJSONObject("recipient").getString("id");
                        
                    	message = extractText(messageObj);         
                		
                	} catch (Exception e) {
                		
                		/**
                		 * Post/Comment Feed request
                		 * 
                		 * Samples
                		 * {
  "object": "page",
  "entry": [
    {
      "id": "147172370409",
      "time": 1615535075,
      "changes": [
        {
          "value": {
            "from": {
              "id": "147172370409",
              "name": "Eoss-th"
            },
            "message": "promoadds",
            "post_id": "147172370409_10160563886125410",
            "created_time": 1615535070,
            "item": "status",
            "published": 1,
            "verb": "add"
          },
          "field": "feed"
        }
      ]
    }
  ]
}
                		 * 
                		 * 
                		 * {
  "object": "page",
  "entry": [
    {
      "id": "147172370409",
      "time": 1615535000,
      "changes": [
        {
          "value": {
            "from": {
              "id": "2074886809213298",
              "name": "Ken Srisawet"
            },
            "post": {
              "status_type": "mobile_status_update",
              "is_published": true,
              "updated_time": "2021-03-12T07:43:18+0000",
              "permalink_url": "https://www.facebook.com/eossth/posts/10160563860675410",
              "promotion_status": "inactive",
              "id": "147172370409_10160563860675410"
            },
            "message": "testna",
            "post_id": "147172370409_10160563860675410", !POSTID!!!
            "comment_id": "10160563860675410_10160563885055410",
            "created_time": 1615534998,
            "item": "comment",
            "parent_id": "147172370409_10160563860675410", !POSTID!!!
            "verb": "add"
          },
          "field": "feed"
        }
      ]
    }
  ]
}
                		 */
                		JSONObject entryObj = requestObj.getJSONArray("entry").getJSONObject(0);
                		
                		recipientId = entryObj.getString("id"); //pageId

                		JSONObject changeObj = entryObj.getJSONArray("changes").getJSONObject(0).getJSONObject("value");
                		
                		String fromId = changeObj.getJSONObject("from").getString("id");
                		
                		sessionId = fromId; //TODO: check later
                		
                		commentName = changeObj.getJSONObject("from").getString("name");
                		
                		String postId = changeObj.optString("post_id");
                		
                		//From User Comments
                		if (!recipientId.equals(fromId)) {
                			
                			/**
                			 * TODO: Feed to facebook-feed <POSTID> <MESSAGE> to Play with POST vs Answer
                			 */
                			message = "facebook-feed " + postId + " " + changeObj.getString("message");
                			
                			try {
                				
                				sessionId = changeObj.getString("comment_id");
                    			
                				recipientType =  "comment_id";
                    			
                			} catch (Exception e1) {
                				
                				sessionId = changeObj.getString("post_id");
                    			
                				recipientType =  "post_id";
                				
                			}
                			
                		}
                		
                		System.out.println("Check feed message.." + recipientType + ", " + message);
                		System.out.println("Post Id:" + postId);
                		System.out.println("Recipient Id:" + recipientId);
                		System.out.println("From Id:" + fromId);
                		System.out.println("From Name:" + commentName);
                		System.out.println("sessionId:" + sessionId);
                		System.out.println();
                        
                	}

                } else {
                	
            		/**
            		 * None page request
            		 */
            		throw new RuntimeException(requestBody);
                	
                }
                
            } else {
            	
                throw new RuntimeException("Invalid Signature");
            }

        } catch (IOException e) {
        	
            throw new RuntimeException(e);
        }		
        
        /**
         * Lookup ACT from recipientId (pageId)
         */
		String facebookJSONPath = Configuration.facebookACTPath(recipientId);
        
		JSONObject configObject = storage().readAsJSONObject(facebookJSONPath);
		
		if (configObject==null) throw new RuntimeException("Facebook Config Not Found!" + recipientId + "@" + facebookJSONPath);
		
		pageAccessToken = configObject.getString("pageAccessToken").toString();
		
		contextName = configObject.getString("contextName");
		
		String [] tokens = contextName.split("/");
		
		String accountId = tokens[0];
		
		String botId = tokens[1];		
		
		if (message.equals("greeting")) {
			
			try {
				
				String psid = sessionId;
				
				FacebookClient facebookClient = new DefaultFacebookClient(pageAccessToken, appSecret, Version.LATEST);
				userProfile = facebookClient.fetchObject(psid, User.class, Parameter.with("fields", "first_name,last_name,profile_pic"));
				
			} catch (Exception e) {
				throw new RuntimeException(e + ":" + message);
			}
			
			
		} else if (message.startsWith("https://") && message.contains("fbcdn.net")) {
			
			StringBuilder relocatedContents = new StringBuilder();
			
			tokens = message.split("\n\n\n");
			
			for (String text:tokens) {
				
				String contentURL = text;
				
            	//Trim Query Parameters (for Facebook Storage)
				text = text.substring(0, text.lastIndexOf("?"));
				String fileName = text.substring(text.lastIndexOf("/") + 1);
				
				try {
					URL url = new URL(contentURL);
					HttpURLConnection con = (HttpURLConnection) url.openConnection();
					con.setRequestMethod("GET");
					
					int responseCode = con.getResponseCode();						
					if (responseCode == HttpURLConnection.HTTP_OK) {
						
						InputStream contentStream = new BufferedInputStream(con.getInputStream());
						
						String contentName = "public/" + accountId + "/" + fileName;
						
						storage().write(contentStream, contentName);
						
						String contextRoot = request.getContextPath();
						
						relocatedContents.append(Configuration.domain + contextRoot + "/" + contentName);
						relocatedContents.append("\n\n\n");
					}
					
				} catch (Exception e) {
					
					throw new RuntimeException(e + ":" + message);
				}
										
			}
			
			message = relocatedContents.toString().trim();
		}
				
		messageObject.attr("channel", "facebook.page");		
	}
	
	private String extractText(JSONObject messageObj) {
		
		String text;
		
        try {

        	
            /**
             * First Try as simple text
             */
            text = messageObj.getJSONObject("message").getString("text");
                                    	                            

        } catch (Exception e) {

            try {

                /**
                 * Retry as attachment
                 */
            	
            	JSONArray attachmentArray = messageObj.getJSONObject("message").getJSONArray("attachments");
            	JSONObject attachmentObj, payload;
            	String type;
            	
            	StringBuilder contents = new StringBuilder();
            	
            	for (int i=0;i<attachmentArray.length();i++) {
            		
            		attachmentObj = attachmentArray.getJSONObject(i);
                    type = attachmentObj.getString("type");                            		
            		payload = attachmentObj.getJSONObject("payload");                            		
            		
                    if (type.equals("location")) {
                    	
                        Double colat = payload.getJSONObject("coordinates").getDouble("lat");
                        Double colong = payload.getJSONObject("coordinates").getDouble("long");

                    	contents.append(colat + ", " + colong);
                        
                    } else {
                    	
                    	contents.append(payload.getString("url"));
                        
                    }
                    
                	contents.append(System.lineSeparator());
                	contents.append(System.lineSeparator());
                	contents.append(System.lineSeparator());
            	}
            	
            	text = contents.toString().trim();

            } catch (Exception attachment) {

                try {

                    /**
                     * Retry as Get Started!
                     */
                    text = messageObj.getJSONObject("get_started").getString("payload");

                } catch (Exception getStarted) {

                    try {

                        /**
                         *  Retry as referral
                         */
                        text = messageObj.getJSONObject("referral").getString("ref");
                        text = URLDecoder.decode(text, "UTF-8");

                    } catch (Exception refereral) {
                    	
                        try {

                            /**
                             *  Retry as Get Started referral
                             */
                            text = messageObj.getJSONObject("postback").getJSONObject("referral").getString("ref");
                            text = URLDecoder.decode(text, "UTF-8");

                        } catch (Exception getStartedReferral) {

                            try {

                                /**
                                 * Retry as Postback!
                                 */
                                text = messageObj.getJSONObject("postback").getString("payload");

                            } catch (Exception postback) {

                                /**
                                 * throw new RuntimeException("Undefined MSG:" + requestBody);
                                 * text = messageObj.getJSONObject("message").getString("mid");
                                 */
                            	
                            	text = attachment.getMessage();

                            }
                        }
                    
                    }
                }
            }
        }   
        
        return text;
	}
	
	public String getAppSecret() {
		
		return appSecret;
	}
	
    public String getPageAccessToken() {
    	
    	return pageAccessToken;
    }

	public void setPageAccessToken(String pageAccessToken) {
		
		this.pageAccessToken = pageAccessToken;
	}
	
	public String getRecipientType() {
		
		return recipientType;
	}
	
	public String getRecipientId() {
		
		return recipientId;
	}
	
	@Override
	public void prepare(Session session) {
		
		/**
		 * Call super if this sessionId is registered admin
		 */
		super.prepare(session);
		
		if (userProfile!=null) {
			
	        session.vars("#s_fullName",  userProfile.getFirstName() + " " + userProfile.getLastName());
	        
		} else if (commentName!=null) {
			
	        session.vars("#s_fullName", commentName);
	        
		}
		
		session.vars("#pageId", recipientId);
		
	}

}
