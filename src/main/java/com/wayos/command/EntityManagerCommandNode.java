package com.wayos.command;

import com.wayos.ContextListener;
import com.wayos.Hook;
import com.wayos.MessageObject;
import com.wayos.Node;
import com.wayos.NodeEvent;
import com.wayos.Session;
import com.wayos.Hook.Match;
import com.wayos.drawer.Canvas2D;

import java.util.HashSet;
import java.util.Set;

/**
 * 
 * Dangerous Command!
 * Created by Wisarut Srisawet on 7/31/2017 AD.
 */
public class EntityManagerCommandNode extends CommandNode {

	/**
	 * Keywords vs Response Delimiter
	 */
	private final String delimiter;

	public EntityManagerCommandNode(Session session, String [] hooks,  Match match, String delimiter) {

		super(session, hooks, match);

		if (delimiter==null) {

			this.delimiter = "=>";

		} else {

			this.delimiter = delimiter;

		}
	}

	@Override
	public String execute(MessageObject messageObject) {

		String params = cleanHooksFrom(messageObject.toString());

		String [] tokens = params.split(delimiter);

		if (tokens.length==0) return "(-.-)ๆ Empty Parameters";

		try {

			this.session.context().load();

			String keyword = tokens[0].trim();
			String response;

			if (tokens.length==1) {

				response = "";

			} else {

				response = tokens[1].trim();

			}

			final Set<Node> matchedNode = new HashSet<>();

			/**
			 * Search 100% Matched!
			 */
			MessageObject keywordsMessageObject = MessageObject.build(keyword);

			session.context().matched(keywordsMessageObject, new ContextListener() {

				@Override
				public void callback(NodeEvent nodeEvent) {

					String label = "";
					for (Hook hook:nodeEvent.node.hookList()) {

						//Ignore Parent
						if (hook.text.startsWith("@")) {
							continue;
						}

						label += hook.text + " ";
					}

					label = label.trim();

					//Whole Matched 
					if (keyword.equals(label)) {
						
						matchedNode.add(nodeEvent.getSourceNode());
					}
				}
			});        

			/**
			 * Add New or Update Entity
			 */

			/**
			 * Create New Entity
			 */
			if (matchedNode.isEmpty()) {

				Canvas2D canvas2D = new Canvas2D(this.session.context(), null, 100, false);

				int maxY = canvas2D.getMaxY();

				System.err.println(maxY);

				canvas2D.setPosition(100, maxY + 100);

				canvas2D.newEntity(null, keyword, response, null);
			} 
			/**
			 * Update Responses
			 */
			else {

				System.err.println("Update.." + matchedNode);

				for (Node node:matchedNode) {
					node.setResponse(response);
				}

			}

			this.session.context().save();

			this.session.context().load();

			return super.successMsg();

		} catch (Exception e) {

			return e.getMessage();
		}

	}
}
