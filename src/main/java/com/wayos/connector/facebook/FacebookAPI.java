package com.wayos.connector.facebook;

import java.util.ArrayList;
import java.util.List;

import com.restfb.types.send.AbstractButton;
import com.restfb.types.send.Bubble;
import com.restfb.types.send.CallButton;
import com.restfb.types.send.DefaultAction;
import com.restfb.types.send.GenericTemplatePayload;
import com.restfb.types.send.MediaAttachment;
import com.restfb.types.send.Message;
import com.restfb.types.send.PostbackButton;
import com.restfb.types.send.QuickReply;
import com.restfb.types.send.TemplateAttachment;
import com.restfb.types.send.WebButton;
import com.wayos.command.talk.Choice;
import com.wayos.command.talk.Question;
import com.wayos.connector.ResponseObject;
import com.wayos.util.SimpleHTMLDecoder;

public class FacebookAPI {
		
    private static final String domain = System.getenv("domain");
    
    private static final String thumbnailImageUrl = "https://wayos.yiem.cc/images/gigi.png";
    
	private static FacebookAPI _facebookAPI;
	
	private FacebookAPI() {
		
	}
	
	public static FacebookAPI instance() {
		if (_facebookAPI==null) {
			_facebookAPI = new FacebookAPI();
		}
		return _facebookAPI;
	}
	
    public List<Message> createMessages(ResponseObject responseObject) {

        List<Message> messages = new ArrayList<>();

        List<Object> messageList = responseObject.messageList;
        ResponseObject.Text text;
        ResponseObject.Image image;
        ResponseObject.Audio audio;
        ResponseObject.Video video;

        MediaAttachment attachment;
        List<Question> questionList;

        for (Object msg:messageList) {

            if (msg instanceof ResponseObject.Text) {

                text = (ResponseObject.Text) msg;       
                Message message = new Message(SimpleHTMLDecoder.instance().decode(text.toString()));
            	messages.add(message);

            } else if (msg instanceof ResponseObject.Image) {

                image = (ResponseObject.Image) msg;               
                attachment = new MediaAttachment(MediaAttachment.Type.IMAGE, image.toString());
                messages.add(new Message(attachment));

            } else if (msg instanceof ResponseObject.Audio) {

                audio = (ResponseObject.Audio) msg;
                attachment = new MediaAttachment(MediaAttachment.Type.AUDIO, audio.toString());
                messages.add(new Message(attachment));

            } else if (msg instanceof ResponseObject.Video) {

                video = (ResponseObject.Video) msg;
                attachment = new MediaAttachment(MediaAttachment.Type.VIDEO, video.toString());
                messages.add(new Message(attachment));

            } else {

                questionList = (List<Question>) msg;

                Message message;
                try {

                    /**
                     * Quick Reply Conditions
                     */
                    if (questionList.size()==1 &&
                            (/*questionList.get(0).choices.size()==1||*/
                                    questionList.get(0).choices.size()>3)) {

                        message = createQuickReply(questionList.get(0));

                    } else {

                        /**
                         * Slide Menus
                         */
                        message = createGenericTemplate(questionList);

                    }

                    messages.add(message);

                } catch (Exception e) {

                    for (Question question:questionList) {
                        messages.add(new Message(SimpleHTMLDecoder.instance().decode(question.label)));
                    }

                }

            }
        }

        return messages;
    }

    private Message createQuickReply(Question question) {

        Message message = new Message(SimpleHTMLDecoder.instance().decode(question.label));

        QuickReply quickReply;
        for (Choice choice:question.choices) {

            quickReply = new QuickReply(SimpleHTMLDecoder.instance().decode(choice.label), choice.parent + " " + SimpleHTMLDecoder.instance().decode(choice.label));

            if (choice.imageURL!=null) {
                quickReply.setImageUrl(choice.imageURL);
            }

            message.addQuickReply(quickReply);
        }

        return message;
    }    

    private Message createGenericTemplate(List<Question> questionList) {

        GenericTemplatePayload payload = new GenericTemplatePayload();
        Bubble bubble;
        AbstractButton button;
        String defaultURL;
        for (Question question:questionList) {

            if (question.choices.size()>3) continue;

            bubble = new Bubble(SimpleHTMLDecoder.instance().decode(question.label));
            
            defaultURL = null;

            if (question.hasImage()) {
            	
                bubble.setImageUrl(question.imageURL);
                
            } else if (questionList.size() > 1) {
            	
            	/**
            	 * Default Image for Slide Menu Only
            	 */
            	bubble.setImageUrl(thumbnailImageUrl);
            	
            }

            defaultURL = bubble.getImageUrl();
            
            for (Choice choice:question.choices) {
            	
                if (choice.isLinkLabel()) {
                	
                    if (choice.linkURL.startsWith("tel:")) {

                        button = new CallButton(SimpleHTMLDecoder.instance().decode(choice.label), choice.linkURL.replace("tel:", ""));

                    } else {

                        button = new WebButton(SimpleHTMLDecoder.instance().decode(choice.label), choice.linkURL);
                        
                    }
                    
                    defaultURL = choice.linkURL;//Last Link as default URL
                    
                } else {

                    button = new PostbackButton(SimpleHTMLDecoder.instance().decode(choice.label), choice.parent + " " + SimpleHTMLDecoder.instance().decode(choice.label));
                }

                bubble.addButton(button);
            }
                        
            /**
             * Facebook Developer API only support Default Action as URL
             */
            if (defaultURL!=null) {
            	
                bubble.setDefaultAction(new DefaultAction(defaultURL));
            }

            if (bubble.getButtons().isEmpty()) continue;

            payload.addBubble(bubble);
        }

        return new Message(new TemplateAttachment(payload));
    }	
    

}
