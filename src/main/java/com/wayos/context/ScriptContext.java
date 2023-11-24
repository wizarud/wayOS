package com.wayos.context;

import java.nio.file.Files;
import java.nio.file.Paths;

import com.wayos.MessageObject;
import com.wayos.Session;
import com.wayos.command.data.ImportQuestionareDataCommandNode;

public class ScriptContext extends MemoryContext {
	
	public static final String SUFFIX = "qa";

	public static final String CMD = "importqa";
	
	public ScriptContext(String name) throws Exception {
		
		super(name);
		
		Session session = new Session();
		
		session.context(this);
		
		ImportQuestionareDataCommandNode cmd = new ImportQuestionareDataCommandNode(session, new String[] {CMD});
		
		String script = new String(Files.readAllBytes(Paths.get(name + "." + SUFFIX)));
				
		MessageObject messageObject = MessageObject.build(script);
		
		cmd.execute(messageObject);
		
		//System.out.println(toJSONString());
		
	}

}
