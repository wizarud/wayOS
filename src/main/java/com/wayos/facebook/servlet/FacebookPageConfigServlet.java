package com.wayos.facebook.servlet;

import java.io.IOException;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONObject;

import com.restfb.DefaultFacebookClient;
import com.restfb.FacebookClient.AccessToken;
import com.wayos.Configuration;
import com.wayos.servlet.console.ConsoleServlet;
import com.restfb.Version;

@SuppressWarnings("serial")
@WebServlet("/console/facebook/*")
public class FacebookPageConfigServlet extends ConsoleServlet {
	
	FacebookPageUtil facebookPageUtil = new FacebookPageUtil(Configuration.facebook_appId, Configuration.facebook_appSecret);
	
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		
		try {
			
			JSONObject obj = new JSONObject();
			
			String jsonPath = facebookPageIdPath(req.getRequestURI());
			
			String facebookAccessToken = (String) req.getSession().getAttribute("facebookAccessToken");
			
			JSONObject jsonObject = storage().readAsJSONObject(jsonPath);
			
			if (jsonObject!=null) {
				
				String pageId = jsonObject.optString("pageId");
				
				if (!pageId.isEmpty()) {
					
					obj.put("pageId", pageId);					
				}
				
			}
						
			obj.put("pages", facebookPageUtil.getPageArray(facebookAccessToken));
			
			resp.setContentType("application/json");
			resp.setCharacterEncoding("UTF-8");
			
			resp.getWriter().print(obj.toString());
			
		} catch (Exception e) {
			
			throw new RuntimeException(e);
		}
		
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		
		try {
												
			String pageIdPath = facebookPageIdPath(req.getRequestURI());
			
			JSONObject pageIdObject = storage().readAsJSONObject(pageIdPath);
			
			if (pageIdObject==null) {
				
				pageIdObject = new JSONObject();
				
			}
			
			String facebookAccessToken = (String) req.getSession().getAttribute("facebookAccessToken");
						
			AccessToken act = new DefaultFacebookClient(Version.LATEST).obtainExtendedAccessToken(Configuration.facebook_appId, Configuration.facebook_appSecret, facebookAccessToken);			
									
			String lastPageId = pageIdObject.optString("pageId");
			
			int senderIdSize = 0;
			
			String unSubscribedResult = null;
									
			/**
			 * Unsubscribe last facebook PageId if occurs
			 */
			if (!lastPageId.isEmpty()) {
				
				unSubscribedResult = facebookPageUtil.unSubscribed(lastPageId, act.getAccessToken());
								
			}
			
			String pageId = req.getParameter("pageId");
			
			if (!pageId.isEmpty()) {
				
				String pageAccessToken = facebookPageUtil.getPageAccessToken(act.getAccessToken(), pageId);
				
				//Subscribe new Page Id
				String subscribedResult = facebookPageUtil.subscribed(pageId, act.getAccessToken(), pageAccessToken);
				
				String getStartedResult = facebookPageUtil.getStarted(act.getAccessToken(), pageAccessToken);

				pageIdObject.put("pageId", pageId);
				
				String actPath = facebookACTPath(pageId);
				
				JSONObject actObject = storage().readAsJSONObject(actPath);
				
				if (actObject == null) {
					
					actObject = new JSONObject();					
				}
				
				String contextName = contextName(req.getRequestURI(), false);
				
				actObject.put("contextName", contextName);
				actObject.put("pageAccessToken", pageAccessToken);
				actObject.put("subscribedResult", subscribedResult);
				actObject.put("getStartedResult", getStartedResult);
				
				if (unSubscribedResult!=null) {
					
					actObject.put("unSubscribedResult", unSubscribedResult);
				}
				
				/**
				 * Find PSID
				 */
				//String psid = getPSID(accountId, pageId, act.getAccessToken());
				//if (psid!=null) {
				//	context.properties.put.setProperty("PSID", psid);					
				//}	
				
				/**
				 * Update REACH from page's inbox
				 * 
				 * Save to vars/<accountId>/<botId>/<channel>/senderId.json
				 * Channel: facebook.page
				 */
				
				List<String> senderIdList = facebookPageUtil.getSenderIdListFromInbox(pageId, pageAccessToken);
				
				senderIdSize = senderIdList.size();
				
				Configuration configuration = new Configuration(contextName);
				
				String channel = "facebook.page";
				
		        String path;
										        
				for (String senderId:senderIdList) {
					
			        path = configuration.vars(channel, senderId);
					
					storage().write("{}", path);
					
				}
								
				storage().write(pageIdObject.toString(), pageIdPath);
				
				storage().write(actObject.toString(), actPath);
				
			} else {
				
				/**
				 * Clear Facebook configuration for empty facebook pageId
				 */
												
				if (!lastPageId.isEmpty()) {
					
					String actPath = facebookACTPath(lastPageId);
					
					JSONObject actObject = storage().readAsJSONObject(actPath);
					
					if (actObject == null) {
						
						actObject = new JSONObject();					
					}
					
					actObject.remove("contextName");
					actObject.remove("pageAccessToken");
					actObject.remove("subscribedResult");
					actObject.remove("getStartedResult");
					
					storage().write(actObject.toString(), actPath);
				}
								
				pageIdObject.remove("pageId");
				storage().write(pageIdObject.toString(), pageIdPath);
			}

			
			resp.getWriter().print("success, " + senderIdSize + " imported");
								
		} catch (Exception e) {
			
			throw new RuntimeException(e);
		}

	}
	
	/**
	 * Remapping to private path
	 * @param requestURI
	 * @return
	 */
	private String facebookPageIdPath(String requestURI) {
		
		String contextName = contextName(requestURI, false);
		
		return new Configuration(contextName).facebookPageIdPath();
	}
		
	/**
	 * ACT Configurations
	 * Remapping to private path
	 * @param requestURI
	 * @return
	 */
	private String facebookACTPath(String pageId) {

		return Configuration.facebookACTPath(pageId);
	}
}