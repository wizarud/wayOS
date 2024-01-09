package com.wayos.command.data;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

import org.apache.commons.io.IOUtils;

import com.wayos.Configuration;
import com.wayos.Context;
import com.wayos.MessageObject;
import com.wayos.Session;
import com.wayos.Hook.Match;
import com.wayos.command.CommandNode;
import com.wayos.drawer.Canvas2D;
import com.wayos.drawer.Drawer;
import com.wayos.drawer.basic.PlayDrawer;
import com.wayos.drawer.basic.WayDrawer;

public class ImportWayDataCommandNode extends CommandNode {
	
    public ImportWayDataCommandNode(Session session, String [] hooks) {
    	
        super(session, hooks, Match.Head);
        
    }

    @Override
    public String execute(MessageObject messageObject) {
                        
        try {
        	
        	Context context = session.context();
        	
			context.load();
			
        	String contextName = context.name();
        	
	    	String title = context.prop("title");	    	
        	
        	ByteArrayInputStream inputStream = new ByteArrayInputStream(cleanHooksFrom(messageObject.toString()).getBytes("UTF-8"));
        	
			String text = IOUtils.toString(inputStream, StandardCharsets.UTF_8.name());
			
			byte [] buffer = text.getBytes();
			
			String way = new BufferedReader(
				      new InputStreamReader(new ByteArrayInputStream(buffer), StandardCharsets.UTF_8))
				        .lines()
				        .collect(Collectors.joining("\n"));	
				        
        	Drawer drawer = new PlayDrawer(way);// Way use the WayDrawer
        	
    		String wayTxtPath = Configuration.PRIVATE_PATH + contextName + ".way.txt";
    		
			storage().write(new ByteArrayInputStream(buffer), wayTxtPath);
        	
	    	System.out.println("Updateing context (by Command):" + contextName);
	    	
			Canvas2D canvas2D = new Canvas2D(context, title, 100, true);
			
			drawer.draw(canvas2D);
			
            context.save();
            
            context.load();
            
    		String newTSVPath = Configuration.PRIVATE_PATH + contextName + ".chai.tsv";
			
			storage().delete(newTSVPath);            

            return successMsg();
            
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return failMsg();
    }
    
}
