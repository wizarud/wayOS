package com.wayos.connector.facebook;

import java.util.List;

import com.restfb.DefaultFacebookClient;
import com.restfb.FacebookClient;
import com.restfb.Parameter;
import com.restfb.Version;
import com.restfb.types.send.CommentIdMessageRecipient;
import com.restfb.types.send.IdMessageRecipient;
import com.restfb.types.send.MediaAttachment;
import com.restfb.types.send.Message;
import com.restfb.types.send.MessageRecipient;
import com.restfb.types.send.PostIdMessageRecipient;
import com.restfb.types.send.SendResponse;
import com.wayos.connector.ResponseConnector;
import com.wayos.connector.ResponseObject;

public class FacebookResponseConnector extends ResponseConnector {

	public FacebookResponseConnector(FacebookHttpRequestObject requestObject) {
		super(requestObject);
	}
	
	private FacebookHttpRequestObject facebookHttpRequestObject() {
		return (FacebookHttpRequestObject) super.requestObject;
	}	

	@Override
	public String execute(ResponseObject responseObject) {
		
    	if (facebookHttpRequestObject().getPageAccessToken()==null) throw new IllegalArgumentException("Missing PageAccessToken");

        MessageRecipient recipient;
        
		if (facebookHttpRequestObject().getRecipientType() == null) {
			
			recipient = new IdMessageRecipient(facebookHttpRequestObject().sessionId());
			
		} else if (facebookHttpRequestObject().getRecipientType().equals("post_id")) {
			
			recipient = new PostIdMessageRecipient(facebookHttpRequestObject().sessionId());
			
		} else if (facebookHttpRequestObject().getRecipientType().equals("comment_id")) {
			
			recipient = new CommentIdMessageRecipient(facebookHttpRequestObject().sessionId());
			
		} else {
			
			throw new IllegalArgumentException("Unknown recipientType for " + facebookHttpRequestObject().getRecipientType());
			
		}
                
        FacebookClient pageClient = new DefaultFacebookClient(facebookHttpRequestObject().getPageAccessToken(), Version.LATEST);

        String appsecret_proof = pageClient.obtainAppSecretProof(facebookHttpRequestObject().getPageAccessToken(), facebookHttpRequestObject().getAppSecret());

        List<Message> messages = FacebookAPI.instance().createMessages(responseObject);

        SendResponse resp = null;
        StringBuilder logCollector = new StringBuilder();
        
		//Inbox comment notification, retrieve from Reserved Keywords "facebook-feed"
		if (facebookHttpRequestObject().getRecipientType() != null && !messages.isEmpty()) {
			
			String facebookFeedResponse = messages.get(0).getText();
						
			if (facebookFeedResponse!=null && !facebookFeedResponse.trim().isEmpty()) {
				
		        resp = pageClient.publish(facebookHttpRequestObject().sessionId() + "/comments", SendResponse.class,
		                Parameter.with("message", facebookFeedResponse),
		                Parameter.with("appsecret_proof", appsecret_proof));
		        
		        messages.remove(0); //Pop first message from private reply!
			}
		}
        
        for (Message message:messages) {

            try {

                resp = pageClient.publish("me/messages", SendResponse.class,
                        Parameter.with("recipient", recipient),
                        Parameter.with("message", message),
                        Parameter.with("appsecret_proof", appsecret_proof));

            } catch (Exception e) {

                try {

                    if (message.getAttachment()!=null && (message.getAttachment().getType().equals("image") || message.getAttachment().getType().equals("audio"))) {

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

                    }

                } catch (Exception attachment) {
                    logCollector.append(attachment.getMessage());
                }

            } finally {
                /**
                 * Log Collection
                 */
                logCollector.append(message + System.lineSeparator());
            }

        }
        
        return logCollector.toString().trim();
	}

}
