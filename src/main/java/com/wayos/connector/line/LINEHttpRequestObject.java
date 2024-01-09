package com.wayos.connector.line;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;

import com.wayos.Configuration;
import com.wayos.Session;
import com.wayos.connector.http.HttpRequestObject;

import x.org.json.JSONArray;
import x.org.json.JSONException;
import x.org.json.JSONObject;

public class LINEHttpRequestObject extends HttpRequestObject {
	
	public static final String CONTENT_ENDPOINT = "https://api-data.line.me/v2/bot/message/";

	public static final String PROFILE_URL = "https://api.line.me/v2/bot/profile";
	
    /**
     * For reply to line api
     */
    protected String channelAccessToken;

    /**
     * Use for LINE Response API
     */
    protected String replyToken;

    /**
     * LINE source type (user|room|group)
     */
    private String sourceType;
    
    private JSONObject userProfile;
	
	public LINEHttpRequestObject(HttpServletRequest request) {
		
		super(request, null);
		
		Configuration configuration = new Configuration(contextName);
		
		JSONObject configObject = storage().readAsJSONObject(configuration.linePath());
		
		if (configObject==null) throw new RuntimeException("Line Config Not Found! for " + contextName);
		
		String [] tokens = contextName.split("/");
		
		String accountId = tokens[0];
		
		String botId = tokens[1];
		
		String secret = configObject.getString("secret");
				
    	if (secret==null) throw new IllegalArgumentException("Missing Secret");

		channelAccessToken = configObject.getString("act");
		
    	LINESignatureValidator lineSignatureValidator = new LINESignatureValidator(secret.getBytes());
    	
    	String requestBody = null;
    	
        try {

            String headerSignature = request.getHeader("X-Line-Signature");

            requestBody = request.getReader().lines().collect(Collectors.joining(System.lineSeparator()));

            if (lineSignatureValidator.validateSignature(requestBody.getBytes(), headerSignature)) {

                JSONObject requestObj = new JSONObject(requestBody);
                //String destination = requestObj.getString("destination");
                JSONArray eventArray = requestObj.getJSONArray("events");

                JSONObject eventObj, srcObj, messageObj;
                String type;

                for (int i=0; i<eventArray.length(); i++) {

                    eventObj = eventArray.getJSONObject(i);
                    type = eventObj.getString("type");
                    srcObj = eventObj.getJSONObject("source");
                    sourceType = srcObj.getString("type");

                    if (sourceType.equals("user")) {
                    	
                        sessionId = srcObj.getString("userId");
                        
                    } else if (sourceType.equals("room")) {
                    	
                        sessionId = srcObj.getString("roomId");
                        
                    } else if (sourceType.equals("group")) {
                    	
                        sessionId = srcObj.getString("groupId");
                        
                    } else {
                    	
                    	//sessionId = "";
                    	
                        sessionId = sourceType;
                    }
                    
                    if (type.equals("leave") || type.equals("unfollow") || type.equals("unsend")) {
                        break;
                    }

                    replyToken = eventObj.getString("replyToken");

                    if (type.equals("message")) {

                        messageObj = eventObj.getJSONObject("message");

                        if (messageObj.getString("type").equals("text")) {

                            message = messageObj.getString("text");

                        } else if (messageObj.getString("type").equals("image") || messageObj.getString("type").equals("video") || messageObj.getString("type").equals("audio")) {

                            JSONObject contentProvider = messageObj.getJSONObject("contentProvider");
                            String contentType = contentProvider.getString("type");

                            if (contentType.equals("line")) {

                                String messageId = messageObj.getString("id");
                                String contentSourceURL = CONTENT_ENDPOINT + messageId + "/content";
                                message = contentSourceURL + "?" + messageObj.getString("type");
                                //message += "\n\n\n";
                                
                            }

                        } else if (messageObj.getString("type").equals("sticker")) {

                            String packageId = messageObj.getString("packageId");
                            String stickerId = messageObj.getString("stickerId");
                            //message = "line:sticker " + "lineStickerId:" + packageId + ":" + stickerId;
                            message = "";//Unsupported Sticker Message

                        } else if (messageObj.getString("type").equals("location")) {

                            Double latitude = messageObj.getDouble("latitude");
                            Double longitude = messageObj.getDouble("longitude");
                            message = latitude + ", " + longitude;

                        }

                    } else if (type.equals("postback")) {

                        message = eventObj.getJSONObject("postback").getString("data");

                    } else if (type.equals("follow") || type.equals("join") || type.equals("memberJoined")) {

                        message = "greeting";

                    } else {

                        message = "";
                        
                    }

                }

            } else {

                throw new RuntimeException("Invalid Signature");

            }

        } catch (JSONException e) {
        	
            throw new RuntimeException(requestBody);        	
        
		} catch (IOException e) {
        	
            throw new RuntimeException(e);
            
        }
        

		/**
		 * Download content to WAYOBOT HERE!!!
		 * HEADER => Authorization: Bearer {channel access token}
		 */
		if (message.equals("greeting")) {
			
			try {
				
				URL url = new URL(PROFILE_URL + "/" + sessionId);
				HttpURLConnection con = (HttpURLConnection) url.openConnection();
				con.setRequestMethod("GET");
				con.setRequestProperty("Authorization", "Bearer " + channelAccessToken);
				
				int responseCode = con.getResponseCode();	
				
		        StringBuffer response = new StringBuffer();
		        
				if (responseCode == HttpURLConnection.HTTP_OK) {
					
		            BufferedReader reader = new BufferedReader(new InputStreamReader(con.getInputStream()));
		            String line;

		            while ((line = reader.readLine()) != null) {
		                response.append(line);
		                response.append(System.lineSeparator());
		            }
		            reader.close();							
					
					userProfile = new JSONObject(response.toString());
					
				} 
																
			} catch (Exception e) {
				
				throw new RuntimeException(e + ":" + messageObject.toString());
			}					
			
		} else if (message.startsWith(CONTENT_ENDPOINT)) {	
			
			tokens = message.split("\\?", 2);					
			String contentURL = tokens[0];
			String type = tokens[1];
			
			URL obj;
			try {
				obj = new URL(contentURL);
				HttpURLConnection con = (HttpURLConnection) obj.openConnection();
				con.setRequestMethod("GET");
				con.setRequestProperty("Authorization", "Bearer " + channelAccessToken);
				
				int responseCode = con.getResponseCode();						
				if (responseCode == HttpURLConnection.HTTP_OK) {
					
					InputStream contentStream = new BufferedInputStream(con.getInputStream());
					
					String suffix;
					if (type.equals("image")) {
						suffix = ".png";
					} else if (type.equals("video")) {
						suffix = ".mp4";
					} else if (type.equals("audio")) {
						suffix = ".mp3";
					} else {
						suffix = "";
					}
					
					String contentName = "public/" + accountId + "/" + contentURL.replace(CONTENT_ENDPOINT, "").replace("/content", "") + suffix;

					storage().write(contentStream, contentName);
					
					String contextRoot = request.getContextPath();
					
					String sheme = Configuration.domain.startsWith("localhost") ? "http" : "https";
					
					//message = Configuration.domain + "/" + contentName;
					
					message = sheme + "://" + Configuration.domain + contextRoot + "/" + contentName;					
					
				} else {
					
					message = contentURL + ": " + con.getResponseMessage();
							
				}
				
			} catch (Exception e) {
				
				throw new RuntimeException(e + ":" + messageObject.toString());
			}
		}
		
		messageObject.attr("channel", "line");
	}
	
	public String getReplyToken() {
		
		return replyToken;
	}
	
	public String getChannelAccessToken() {
		
		return channelAccessToken;
	}

	public void setChannelAccessToken(String channelAccessToken) {
		
		this.channelAccessToken = channelAccessToken;
	}
	
	@Override
	public void prepare(Session session) {
		
		/**
		 * Call super if this sessionId is registered admin
		 */
		super.prepare(session);
		
		if (userProfile!=null) {
			
	        session.vars("#s_fullName",  userProfile.getString("displayName"));
		}
		
	}
	
}
