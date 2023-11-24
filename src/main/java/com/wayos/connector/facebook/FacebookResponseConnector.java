package com.wayos.connector.facebook;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.net.URL;
import java.util.List;

import javax.imageio.ImageIO;

import com.restfb.BinaryAttachment;
import com.restfb.DefaultFacebookClient;
import com.restfb.FacebookClient;
import com.restfb.Parameter;
import com.restfb.Version;
import com.restfb.FacebookClient.AccessToken;
import com.restfb.types.send.CommentIdMessageRecipient;
import com.restfb.types.send.IdMessageRecipient;
import com.restfb.types.send.MediaAttachment;
import com.restfb.types.send.MediaAttachment.Type;
import com.restfb.types.send.Message;
import com.restfb.types.send.MessageRecipient;
import com.restfb.types.send.PostIdMessageRecipient;
import com.restfb.types.send.SendResponse;
import com.wayos.Configuration;
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
		AccessToken act = new DefaultFacebookClient(Version.LATEST).obtainExtendedAccessToken(Configuration.facebook_appId, Configuration.facebook_appSecret, facebookHttpRequestObject().getPageAccessToken());					
                
        FacebookClient pageClient = new DefaultFacebookClient(facebookHttpRequestObject().getPageAccessToken(), Version.LATEST);

        String appsecret_proof = pageClient.obtainAppSecretProof(facebookHttpRequestObject().getPageAccessToken(), facebookHttpRequestObject().getAppSecret());

        List<Message> messages = FacebookAPI.instance().createMessages(responseObject);

        SendResponse resp = null;
        StringBuilder logCollector = new StringBuilder();
        
		//Inbox comment notification, retrieve from Reserved Keywords "facebook-feed"
		if (facebookHttpRequestObject().getRecipientType() != null && !messages.isEmpty()) {
			
			String facebookFeedResponse = messages.get(0).getText();
						
			if (facebookFeedResponse!=null && !facebookFeedResponse.trim().isEmpty()) {
				
				System.out.println("Reply comment to.." + facebookHttpRequestObject().sessionId() + ", " + facebookFeedResponse);
				
		        resp = pageClient.publish(facebookHttpRequestObject().sessionId() + "/comments", SendResponse.class,
		                Parameter.with("message", facebookFeedResponse),
		                Parameter.with("appsecret_proof", appsecret_proof));
		        
		        messages.remove(0); //Pop first message from private reply!
			}
		}
        
        for (Message message:messages) {

            try {

                if (message.getAttachment()!=null && message.getAttachment().getType().equals("image")) {

                    /**
                     * UrlPayload[isReusable=null url=%]
                     */
                    Object payload = ((MediaAttachment)message.getAttachment()).getPayload();
                    String url = payload.toString().replace("UrlPayload[isReusable=null url=", "");
                    url = url.substring(0, url.length()-1);
                    
                    BufferedImage bi = ImageIO.read(new URL(url));
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    String suffix;
                    if (url.endsWith("png")) {
                        ImageIO.write(bi, "png", baos);
                        suffix = "png";
                    } else {
                        ImageIO.write(bi, "jpg", baos);
                        suffix = "jpg";
                    }
                    byte[] imageAsByteArray = baos.toByteArray();
                    
                    BinaryAttachment attachment = BinaryAttachment.with("wayos." + suffix, imageAsByteArray);                    
                    
                    resp = pageClient.publish("me/messages", SendResponse.class, attachment,
                            Parameter.with("recipient", recipient),
                            Parameter.with("message", new Message(new MediaAttachment(Type.IMAGE))),
                            Parameter.with("appsecret_proof", appsecret_proof));

                } else {
                	
                    resp = pageClient.publish("me/messages", SendResponse.class,
                            Parameter.with("recipient", recipient),
                            Parameter.with("message", message),
                            Parameter.with("appsecret_proof", appsecret_proof));                    	
                	
                }

            } catch (Exception e) {
            	
            	e.printStackTrace();
            	
                logCollector.append(e.getMessage());
                
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
