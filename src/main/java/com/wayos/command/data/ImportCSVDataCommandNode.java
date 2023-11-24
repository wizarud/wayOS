package com.wayos.command.data;

import java.io.ByteArrayInputStream;

import com.wayos.MessageObject;
import com.wayos.Session;
import com.wayos.Hook.Match;
import com.wayos.command.CommandNode;
import com.wayos.drawer.Canvas2D;
import com.wayos.drawer.Drawer;
import com.wayos.drawer.basic.DataTableDrawer;

/**
 * Created by eossth on 7/31/2017 AD.
 */
public class ImportCSVDataCommandNode extends CommandNode {
	
	private String delimeter;

    public ImportCSVDataCommandNode(Session session, String [] hooks, String delimeter) {
    	
        super(session, hooks, Match.Head);
        this.delimeter = delimeter;
    }

    @Override
    public String execute(MessageObject messageObject) {
                        
        try {
        	
        	ByteArrayInputStream inputStream = new ByteArrayInputStream(cleanHooksFrom(messageObject.toString()).getBytes("UTF-8"));
        	
        	Drawer drawer = new DataTableDrawer(new DataTableDrawer.TSVTableLoader(inputStream, delimeter));
        	        	
        	String title = session.context().prop("title");

			Canvas2D canvas2D = new Canvas2D(session.context(), title, 100, true);
			
			drawer.draw(canvas2D);
			
            session.context().save();
            
            session.context().load();

            return successMsg();
            
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return failMsg();
    }
    
}
