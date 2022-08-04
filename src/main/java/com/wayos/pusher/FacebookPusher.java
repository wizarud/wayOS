package com.wayos.pusher;

import java.util.List;

import org.json.JSONObject;

import com.restfb.DefaultFacebookClient;
import com.restfb.FacebookClient;
import com.restfb.Parameter;
import com.restfb.Version;
import com.restfb.types.send.IdMessageRecipient;
import com.restfb.types.send.MediaAttachment;
import com.restfb.types.send.Message;
import com.restfb.types.send.MessageRecipient;
import com.restfb.types.send.SendResponse;
import com.wayos.Configuration;
import com.wayos.PathStorage;
import com.wayos.connector.ResponseObject;
import com.wayos.connector.facebook.FacebookAPI;

public class FacebookPusher extends Pusher {
	
	public FacebookPusher(PathStorage storage) {
		super(storage);
		
	}
		
	@Override
	public void push(String contextName, String sessionId, String text) {
		
		Configuration configuration = new Configuration(contextName);
		
		String facebookPageIdPath = configuration.facebookPageIdPath();
		JSONObject pageIdObject = storage.readAsJSONObject(facebookPageIdPath);
		String facebookPageId = pageIdObject.getString("pageId");
	
		JSONObject configObject = storage.readAsJSONObject(Configuration.facebookACTPath(facebookPageId));
		
		if (configObject==null) throw new RuntimeException("Facebook Config Not Found!");
			
		String pageAccessToken;
		try {
			pageAccessToken = configObject.getString("pageAccessToken");
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		
		MessageRecipient recipient = new IdMessageRecipient(sessionId);
						
        FacebookClient pageClient = new DefaultFacebookClient(pageAccessToken, Version.LATEST);

        String appsecret_proof = pageClient.obtainAppSecretProof(pageAccessToken, Configuration.facebook_appSecret);

        List<Message> messages = FacebookAPI.instance().createMessages(new ResponseObject(text));

        SendResponse resp = null;
        
        for (Message message:messages) {

            try {

                resp = pageClient.publish("me/messages", SendResponse.class,
                		Parameter.with("messaging_type", "MESSAGE_TAG"),
                		Parameter.with("tag", "ACCOUNT_UPDATE"),
                        Parameter.with("recipient", recipient),
                        Parameter.with("message", message),
                        Parameter.with("appsecret_proof", appsecret_proof));

            } catch (Exception e) {

                try {

                    if (message.getAttachment()!=null && 
                    		(message.getAttachment().getType().equals("image") || 
                    		message.getAttachment().getType().equals("audio"))) {

                        /**
                         * UrlPayload[isReusable=null url=%]
                         */
                        Object payload = ((MediaAttachment)message.getAttachment()).getPayload();
                        String url = payload.toString().replace("UrlPayload[isReusable=null url=", "");
                        url = url.substring(0, url.length()-1);

                        resp = pageClient.publish("me/messages", SendResponse.class,
                                Parameter.with("recipient", recipient),
                                Parameter.with("message", new Message(url)),
                                Parameter.with("appsecret_proof", appsecret_proof));
                        
                        return;

                    }

                } catch (Exception attachment) {
                	
                	throw new RuntimeException(attachment);
                }
                
            	throw new RuntimeException(e);

            } finally {
            }

        }		
		
	}	

}
