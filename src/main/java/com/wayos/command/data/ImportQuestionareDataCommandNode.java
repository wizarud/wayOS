package com.wayos.command.data;

import com.wayos.Hook;
import com.wayos.MessageObject;
import com.wayos.Session;
import com.wayos.command.CommandNode;
import com.wayos.drawer.Canvas2D;
import com.wayos.drawer.Drawer;
import com.wayos.drawer.basic.QuizDrawer;

public class ImportQuestionareDataCommandNode extends CommandNode {
	
    public ImportQuestionareDataCommandNode(Session session, String [] hooks) {    	
        super(session, hooks, Hook.Match.Head);
    }

    @Override
    public String execute(MessageObject messageObject) {

        try {
       
        	String content = cleanHooksFrom(messageObject.toString());
        	        	        	        	        	
        	Drawer drawer = new QuizDrawer(content);
        	
        	Canvas2D canvas2D = new Canvas2D(session.context(), session.context().prop("title"), 100, true);

        	drawer.draw(canvas2D);
        	
        	canvas2D.context.save();

            return successMsg();
            
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return failMsg();
    }
    
}
