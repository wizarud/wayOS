package com.wayos.command.admin;

import com.wayos.Configuration;
import com.wayos.MessageObject;
import com.wayos.NodeEvent;
import com.wayos.command.CommandNode;
import com.wayos.util.SignatureValidator;

/**
 * Created by eossth on 7/31/2017 AD.
 */
public class AdminCommandNode extends CommandNode {

    public static class AuthenticationException extends RuntimeException {
        public AuthenticationException(String msg) {
            super(msg);
        }
    }

    protected final CommandNode commandNode;

    public AdminCommandNode(CommandNode commandNode) {

        super(commandNode.session);
        this.commandNode = commandNode;
    }

    public String execute(MessageObject messageObject) {
    	
        return commandNode.execute(messageObject);
    }

    @Override
    public boolean matched(MessageObject messageObject) {

        if (!commandNode.matched(messageObject)) return false;

        try {

            /**
             * Quick pass for registered Admin sessionId
             */
            String selfSign = (String) messageObject.attr("selfSign");
            
            if (selfSign!=null && selfSign.equals(Configuration.brainySecret)) return true;
            
            /**
             * For embedded http header signature
             */
            String signature = (String) messageObject.attr("signature");
            
            SignatureValidator signatureValidator = new SignatureValidator(Configuration.brainySecret.getBytes());

            /**
             * Signature Validation
             */
            if (signature!=null && signatureValidator.validateSignature(messageObject.toString().getBytes(), signature)) return true;

            /**
             * Fallback handler
             */
            session.context().listener().callback(new NodeEvent(this, messageObject, NodeEvent.Event.Authentication));
            
            return true;

        } catch (AuthenticationException e) {

            throw e;

        } catch (Exception e) {

            throw new RuntimeException(e);

        }

    }
}
