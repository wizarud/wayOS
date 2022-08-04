package com.wayos.facebook.servlet;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;

import com.restfb.Connection;
import com.restfb.DefaultFacebookClient;
import com.restfb.DefaultJsonMapper;
import com.restfb.FacebookClient;
import com.restfb.Parameter;
import com.restfb.Version;
import com.restfb.FacebookClient.AccessToken;
import com.restfb.batch.BatchRequest;
import com.restfb.batch.BatchResponse;
import com.restfb.types.Conversation;
import com.restfb.types.Page;

public class FacebookPageUtil {
	
	private final String appId;
	
	private final String appSecret;
	
	public FacebookPageUtil(String appId, String appSecret) {
		this.appId = appId;
		this.appSecret = appSecret;
	}
	
	public JSONArray getPageArray(String facebookAccessToken) {
		
		JSONArray pageArray = new JSONArray();
		
		AccessToken accessToken = new DefaultFacebookClient(Version.LATEST).obtainExtendedAccessToken(appId, appSecret, facebookAccessToken);
		
		FacebookClient facebookClient = new DefaultFacebookClient(accessToken.getAccessToken(), appSecret, Version.LATEST);
		
		Connection<Page> pageConnections = facebookClient.fetchConnection("me/accounts", Page.class);
		
		JSONObject pageObj;
		for (List<Page> pageList:pageConnections) {
			for (Page page:pageList) {
				pageObj = new JSONObject();
				pageObj.put("pageId", page.getId());
				pageObj.put("name", page.getName());
				pageArray.put(pageObj);
			}
		}
		
		return pageArray;
	}
	
	private FacebookClient getFacebookClient(String act) {
		
		FacebookClient facebookClient = new DefaultFacebookClient(act, appSecret, Version.LATEST);
				
		return facebookClient;
	}
	
	public String getPageAccessToken(String act, String pageId) {
		
		FacebookClient facebookClient = getFacebookClient(act);
		
		Page page = facebookClient.fetchObject(pageId, Page.class, Parameter.with("fields", "access_token"));
		
		return page.getAccessToken();
	}
	
	private String getPSID(String userId, String pageId, String act) {
		
		FacebookClient facebookClient = getFacebookClient(act);
		
		String appAct = facebookClient.obtainAppAccessToken(appId, appSecret).getAccessToken();
		
		String appsecret_proof = facebookClient.obtainAppSecretProof(appAct, appSecret);
		
		BatchRequest getRequest = new BatchRequest.BatchRequestBuilder(userId + "/ids_for_pages?page=" + pageId + "&access_token=" + appAct + "&appsecret_proof=" + appsecret_proof).method("GET").build();
		
		List<BatchResponse> batchResponses = facebookClient.executeBatch(getRequest);
		
		StringBuilder sb = new StringBuilder();
		for (BatchResponse resp:batchResponses) {
			sb.append(resp.getBody());
			sb.append(System.lineSeparator());
		}

		return sb.toString().trim();
	}
	
	public String unSubscribed(String pageId, String act) {
		
		FacebookClient facebookClient = getFacebookClient(act);
		
		String appAct = facebookClient.obtainAppAccessToken(appId, appSecret).getAccessToken();
		
		String appsecret_proof = facebookClient.obtainAppSecretProof(appAct, appSecret);
	
		BatchRequest postRequest = new BatchRequest.BatchRequestBuilder(pageId + "/subscribed_apps?access_token=" + appAct + "&appsecret_proof=" + appsecret_proof)
                .method("DELETE").build();
		
		List<BatchResponse> batchResponses = facebookClient.executeBatch(postRequest);
		
		StringBuilder sb = new StringBuilder();
		for (BatchResponse resp:batchResponses) {
			sb.append(resp.getBody());
			sb.append(System.lineSeparator());
		}

		return sb.toString().trim();        
	}
	
	public String subscribed(String pageId, String act, String pageAct) {
		
		FacebookClient facebookClient = getFacebookClient(act);
		
		String appsecret_proof = facebookClient.obtainAppSecretProof(pageAct, appSecret);
				
		BatchRequest postRequest = new BatchRequest.BatchRequestBuilder(pageId + "/subscribed_apps?access_token=" + pageAct)
                .method("POST")
                .body(Parameter.with("subscribed_fields", "feed, messages, messaging_postbacks, messaging_referrals, picture, videos"), Parameter.with("appsecret_proof", appsecret_proof)).build();
		
		List<BatchResponse> batchResponses = facebookClient.executeBatch(postRequest);
		
		StringBuilder sb = new StringBuilder();
		for (BatchResponse resp:batchResponses) {
			sb.append(resp.getBody());
			sb.append(System.lineSeparator());
		}

		return sb.toString().trim();
	}
		
	public String getStarted(String act, String pageAct) {
		
		FacebookClient facebookClient = getFacebookClient(act);
		
		String appsecret_proof = facebookClient.obtainAppSecretProof(pageAct, appSecret);		
		
		Map<String, String> value = new HashMap<>();
		value.put("payload", "greeting");		
		
		//Config Greeting
		//String greetingJSON = "{\"get_started\":{\"payload\":\"greeting\"}}";
		
		BatchRequest postRequest = new BatchRequest.BatchRequestBuilder("me/messenger_profile?access_token=" + pageAct)
              .method("POST")
              .body(Parameter.with("get_started", value, new DefaultJsonMapper()), Parameter.with("appsecret_proof", appsecret_proof)).build();
		
		List<BatchResponse> batchResponses = facebookClient.executeBatch(postRequest);
		
		StringBuilder sb = new StringBuilder();
		for (BatchResponse resp:batchResponses) {
			sb.append(resp.getBody());
			sb.append(System.lineSeparator());
		}

		return sb.toString().trim();		
	}
	
	public List<String> getSenderIdListFromInbox(String pageId, String pageAccessToken) {
		
        List<String> senderIdList = new ArrayList<>();
        
        FacebookClient pageClient = new DefaultFacebookClient(pageAccessToken, appSecret, Version.LATEST);
        
        String appsecret_proof = pageClient.obtainAppSecretProof(pageAccessToken, appSecret);
        
        Connection<Conversation> connections = pageClient.fetchConnection(pageId + "/conversations", Conversation.class,
                Parameter.with("folder", "inbox"),
                Parameter.with("fields", "senders"),
                Parameter.with("appsecret_proof", appsecret_proof),
                Parameter.with("limit", Integer.MAX_VALUE));
                
        for (List<Conversation> conversationPage:connections) {
        	
        	for (Conversation conversation:conversationPage) {
        		
        		senderIdList.add(conversation.getSenders().get(0).getId());
    			
        	}
        	
        }
        
        return senderIdList;		
	}
}
