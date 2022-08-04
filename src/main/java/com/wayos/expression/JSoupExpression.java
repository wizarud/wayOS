package com.wayos.expression;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.wayos.MessageObject;
import com.wayos.Session;

public class JSoupExpression extends Expression {

	public JSoupExpression(Session session, String[] arguments) {
        super(session, arguments);
    }

    @Override
    public String execute(MessageObject messageObject) {

        String [] args = parameterized(messageObject, arguments);

        if (args.length==3) {
        	
        	String dom = args[1];
        	String path = args[2];
        	        	
            String result = jsoup(dom, path);
            
            messageObject.addResult(result);
            
            //return result;
            return "";
        }

        return super.execute(messageObject);
    }

    protected final String jsoup(String dom, String path) {
    	
        try {
        	
            Object result = Jsoup.parse(dom).select(path);
            
            if (result instanceof Elements) {
                return ((Elements)result).text();
            }

            if (result instanceof Element) {
                return ((Element)result).text();
            }

            return result.toString();

        } catch (Exception e) {
            return e.getMessage();
        }

    }

}
