package com.wayos.connector.speech;

import java.util.Locale;
import java.util.ResourceBundle;

import javax.servlet.http.HttpServletResponse;

import com.wayos.connector.ResponseConnector;
import com.wayos.connector.ResponseObject;

/**
 * 
 * Only support for text and single menu as talking text
 * 
 */
public class SpeechResponseConnector extends ResponseConnector {

	public SpeechResponseConnector(SpeechHttpRequestObject requestObject) {
		super(requestObject);
	}
	
	private SpeechHttpRequestObject restHttpRequestObject() {
		return (SpeechHttpRequestObject) super.requestObject;
	}	

	@Override
	public String execute(ResponseObject responseObject) {
		
		HttpServletResponse response = restHttpRequestObject().httpServletResponse();
		
		response.addHeader("Access-Control-Allow-Origin", "*");
		response.addHeader("Access-Control-Allow-Methods", "POST, GET");
		response.addHeader("Access-Control-Max-Age", "60");
		
     	response.setContentType("text/plain");
		response.setCharacterEncoding("UTF-8");
		
		try {
			
			Locale locale = new Locale(((SpeechHttpRequestObject)requestObject).language);
			
			ResourceBundle bundle = ResourceBundle.getBundle("com.wayos.i18n.text", locale);
			
			String adviceText = bundle.getString("speech.advice");
			
			String pleaseChooseText = bundle.getString("speech.pleasechoose");
			
			String andText = bundle.getString("speech.and");
			
			String speech = SpeechAPI.instance(adviceText, pleaseChooseText, andText).createMessages(responseObject);
			
			response.getWriter().print(speech);
			
		} catch (Exception e) {
			
			e.printStackTrace();
			
		}
		
		return responseObject.toString();

	}

}
