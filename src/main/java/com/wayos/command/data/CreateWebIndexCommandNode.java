package com.wayos.command.data;

import com.wayos.Context;
import com.wayos.Hook;
import com.wayos.MessageObject;
import com.wayos.Node;
import com.wayos.Session;
import com.wayos.command.CommandNode;
import com.wayos.connector.RequestObject;
import com.wayos.connector.SessionPool;
import com.wayos.connector.SessionPool.ContextFactory;
import com.wayos.context.MemoryContext;
import com.wayos.context.RemoteContext;
import com.wayos.util.RemoteStream;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.net.URI;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Created by eossth on 7/31/2017 AD.
 */
public class CreateWebIndexCommandNode extends CommandNode {

    public CreateWebIndexCommandNode(Session session, String [] hooks) {
        super(session, hooks, Hook.Match.Head);
    }

    @Override
    public String execute(MessageObject messageObject) {

        String params = cleanHooksFrom(messageObject.toString());
        String fileName = params.replace("https://", "").replace("http://", "");

        try {
            Document doc = Jsoup.connect(params).get();
            Elements elements = doc.body().select("a");

            Node newNode;
            Elements imgs;
            Element tagA, tagImg;
            String imgSource, linkResponse;
            Map<String, String> linkImageMap = new HashMap<>();
            for (int i=0;i<elements.size();i++) {
                tagA = elements.get(i);
                linkResponse = tagA.attr("href");
                if (linkResponse==null) continue;

                imgs = tagA.select("img");
                if (imgs!=null&&imgs.size()>0) {
                    tagImg = imgs.first();
                    imgSource = tagImg.attr("src");
                    if (imgSource!=null)
                        linkImageMap.put(linkResponse.trim(), imgSource.trim());
                }
            }

            //Context context = new RemoteContext(fileName);
            Context context = session.context();
            
            elements = doc.body().select("a");
            Element parentOfA;
            String innerText;
            for (int i=0;i<elements.size();i++) {

                tagA = elements.get(i);

                linkResponse = tagA.attr("href");
                if (linkResponse==null) continue;
                linkResponse = linkResponse.trim();

                parentOfA = tagA.parent();
                innerText = parentOfA!=null?parentOfA.text():tagA.text();

                if (innerText==null) continue;
                innerText = innerText.trim();

                imgSource = linkImageMap.get(linkResponse);

                innerText = innerText.trim();

                if (imgSource!=null) {
                    linkResponse = fixLink(params, imgSource) + " " + innerText + System.lineSeparator() + fixLink(params, linkResponse) + ":" + "View";
                } else {
                    linkResponse = innerText + System.lineSeparator() + fixLink(params, linkResponse);
                }

                if (!innerText.isEmpty()) {
                    newNode = Node.build(session.context().split(innerText));
                    newNode.setResponse(linkResponse);
                    context.add(newNode);
                }
            }

            context.save();
            
            /*

            RemoteStream gaeWebStream = new RemoteStream(session.context().name() + ".index");
            String indexData = gaeWebStream.read();
            if (!indexData.contains(fileName)) {
                gaeWebStream.write(indexData + fileName + System.lineSeparator());
            }
            */

            session.context().load();

            return successMsg();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return failMsg();
    }

    private String fixLink (String domain, String link) {
        link = link.trim();
        if (!link.startsWith("http")) {

            if ( link.startsWith("/")) {
                try {
                    URI uri = new URI(domain);
                    link = uri.getScheme() + "://" + uri.getHost() + link;
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            } else {
                try {
                    URI uri = new URI(domain);
                    if (uri.getPath().endsWith("/"))
                        link = uri.getScheme() + "://" + uri.getHost() + uri.getPath().substring(0, uri.getPath().lastIndexOf("/")) + "/" +  link;
                    else
                        link = uri.getScheme() + "://" + uri.getHost() + uri.getPath() + "/" +  link;
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }
        return link;
    }
    
    public static void main(String[]args) {
    	
    	SessionPool sessionPool = new SessionPool();
    	
    	sessionPool.register(new ContextFactory() {

			@Override
			public Context createContext(String contextName) {
				
				Context context = new MemoryContext(contextName);
				
				return context;
			}
    		
    	});
    	
    	RequestObject requestObject = RequestObject.create("testSessionId", "testContext");
    	Session session = sessionPool.get(requestObject);
    	
		session.context().locale(new Locale("th"));
		
    	MessageObject messageObject = requestObject.messageObject();
    	messageObject.setText("https://www.paiduaykan.com");
    	
    	CreateWebIndexCommandNode commandNode = new CreateWebIndexCommandNode(session, null);
    	commandNode.execute(messageObject);
    	
    	System.out.println(session.context().toJSONString());
    	
    }
    
}
