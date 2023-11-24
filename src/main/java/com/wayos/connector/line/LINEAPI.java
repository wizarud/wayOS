package com.wayos.connector.line;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import com.wayos.command.talk.Choice;
import com.wayos.command.talk.Question;
import com.wayos.connector.ResponseObject;
import com.wayos.util.SimpleHTMLDecoder;

public class LINEAPI {
	
    private static final String PREVIEW_MEDIA_URL = "https://wayos.yiem.ai/images/WAYOBOT512.png";
    
    private static final String thumbnailImageUrl = "https://wayos.yiem.ai/images/gigi.png";

	private static LINEAPI _lineAPI;
	
	private LINEAPI() {}
	
	public static LINEAPI instance() {
		if (_lineAPI==null) {
			_lineAPI = new LINEAPI();
		}
		return _lineAPI;
	}
	
    public JSONArray createMessages(ResponseObject responseObject) {

    	JSONArray messages = new JSONArray();

        List<Object> messageList = responseObject.messageList;
        ResponseObject.Text text;
        ResponseObject.Image image;
        ResponseObject.Audio audio;
        ResponseObject.Video video;
        List<Question> questionList;
                
        /**
         * Repack if more than five!
         */
        
        for (Object msg:messageList) {
        	
            if (msg instanceof ResponseObject.Text) {

                text = (ResponseObject.Text) msg;
                messages.put(createText(text.toString()));

            } else if (msg instanceof ResponseObject.Image) {

                image = (ResponseObject.Image) msg;
                messages.put(createImage(image.toString()));

            } else if (msg instanceof ResponseObject.Audio) {

                audio = (ResponseObject.Audio) msg;
                messages.put(createAudio(audio.toString()));

            } else if (msg instanceof ResponseObject.Video) {

            	video = (ResponseObject.Video) msg;
                messages.put(createVideo(video.toString()));

            } else {
                questionList = (List<Question>) msg;

                JSONObject message;
                try {

                    if (questionList.size()==1) {

                        Question question = questionList.get(0);

                        if ( /* question.choices.size()==1 || */ question.choices.size()>4 ) {

                            message = createQuickReply(question);

                        } else {

                            message = createButtonsTemplate(question);

                        }

                    } else {

                        message = createCarouselTemplate(questionList);

                    }

                    messages.put(message);

                } catch (Exception e) {
                    for (Question question:questionList) {
                        messages.put(createText(question.label));
                    }
                }
            }

        }
        
        return messages;
    }

	private List<Object> pack(List<Object> messageList) {
		if (messageList.size() > 5) {
        	
        	List<Object> packedMessageList = new ArrayList<>();
        	
        	StringBuilder sb = new StringBuilder();
        	
            for (Object msg:messageList) {
            	
                if (msg instanceof ResponseObject.Text) {
                	sb.append(msg.toString() + System.lineSeparator());
                	continue;
                }
                
                packedMessageList.add(msg);
            }
            
            String content = sb.toString().trim();
            if (!content.isEmpty()) {
            	
            	/**
            	 * Insert Content Before Question
            	 */
            	if (!packedMessageList.isEmpty() && packedMessageList.get(packedMessageList.size()-1) instanceof List<?>) {
            		packedMessageList.add(packedMessageList.size()-1, new ResponseObject.Text(content));
            	} else {
            		packedMessageList.add(new ResponseObject.Text(content));
            	}
            	
            }            
        	
            messageList = packedMessageList;
        }
		return messageList;
	}
    
    public JSONArray toJSONArray(List<JSONObject> jsonArray) {
    	JSONArray array = new JSONArray();
    	
    	for (JSONObject obj:jsonArray) {
    		array.put(obj);
    	}
    	
    	return array;
    }

    private JSONObject createText(String text) {
        JSONObject message = new JSONObject();
        message.put("type", "text");
        message.put("text", SimpleHTMLDecoder.instance().decode(text));
        return message;
    }

    private JSONObject createImage(String imageURL) {
        JSONObject image = new JSONObject();
        image.put("type", "image");
        image.put("originalContentUrl", imageURL);
        image.put("previewImageUrl", imageURL);
        return image;
    }

    private JSONObject createAudio(String audioURL) {
        JSONObject audio = new JSONObject();
        audio.put("type", "audio");
        audio.put("originalContentUrl", audioURL);
        audio.put("duration", 60000);
        return audio;
    }

    private JSONObject createVideo(String videoURL) {
        JSONObject video = new JSONObject();
        video.put("type", "video");
        video.put("originalContentUrl", videoURL);
        video.put("previewImageUrl", PREVIEW_MEDIA_URL);
        return video;
    }
    
    private JSONObject createQuickReply(Question question) {

        JSONObject quickReply = new JSONObject();

        JSONArray items = new JSONArray();
        JSONObject itemObj, actionObj;
        for (Choice choice:question.choices) {
            itemObj = new JSONObject();
            actionObj = new JSONObject();

            String label = choice.label.trim();
            if(label.length()>20) {
                label = label.substring(0, 18);
                label += "..";
            }

            actionObj.put("type", "postback");
            actionObj.put("label", label);
            actionObj.put("data", choice.parent + " " + choice.label);
            actionObj.put("displayText", choice.label);

            itemObj.put("type", "action");
            if (choice.imageURL!=null) {
                itemObj.put("imageUrl", choice.imageURL);
            }
            itemObj.put("action", actionObj);

            items.put(itemObj);
        }

        if (items.length()>13) throw new RuntimeException("Too many quick reply");

        quickReply.put("items", items);

        JSONObject message = new JSONObject();
        message.put("type", "text");
        message.put("text", question.label);
        message.put("quickReply", quickReply);

        return message;
    }

    private JSONObject createButtonsTemplate(Question question) {

        JSONObject template = new JSONObject();
        template.put("type", "buttons");

        boolean hasImage = false;
        if (question.hasImage()) {
            template.put("thumbnailImageUrl", question.imageURL);
            template.put("imageSize", "contain");
            hasImage = true;
        }

        int textLimit = hasImage?60:160;
        String text = question.label;
        if (text.length()>textLimit) {
            text = text.substring(0, textLimit - 2);
            text += "..";
        }

        text = SimpleHTMLDecoder.instance().decode(text);
        template.put("text", text);

        JSONArray actions = new JSONArray();
        JSONObject actionObj;
        for (Choice choice:question.choices) {
            actionObj = new JSONObject();

            String label = choice.label.trim();
            if(label.length()>20) {
                label = label.substring(0, 18);
                label += "..";
            }

            label = SimpleHTMLDecoder.instance().decode(label);
            if (choice.isLinkLabel()) {

                actionObj.put("type", "uri");
                actionObj.put("label", label);
                actionObj.put("uri", choice.linkURL);

            } else {

                actionObj.put("type", "postback");
                actionObj.put("label", label);
                actionObj.put("data", choice.parent + " " + choice.label);
                actionObj.put("displayText", choice.label);

            }

            actions.put(actionObj);
        }

        if (actions.length()==0) throw new RuntimeException("Empty Action");

        template.put("actions", actions);

        JSONObject message = new JSONObject();
        message.put("type", "template");
        message.put("altText", "Menu");
        message.put("template", template);

        return message;
    }

    private JSONObject createCarouselTemplate(List<Question> questionList) {

        int minChoiceSize = Integer.MAX_VALUE;
        boolean hasImage = false;
        for (Question question:questionList) {
            if (question.choices.size()<minChoiceSize) {
                minChoiceSize = question.choices.size();
            }
            if (question.hasImage()) {
                hasImage = true;
            }
        }
        if (minChoiceSize>4) {
            minChoiceSize = 4;
        }

        JSONObject template = new JSONObject();
        JSONArray columns = new JSONArray();
        JSONObject column;
        JSONArray actions;
        JSONObject actionObj;
        JSONObject defaultAction;
        for (Question question:questionList) {

            if (question.choices.size()>minChoiceSize) continue;

            column = new JSONObject();

            if (question.hasImage()) {
                column.put("thumbnailImageUrl", question.imageURL);
            } else {
            	/**
            	 * Default Image
            	 */
                column.put("thumbnailImageUrl", thumbnailImageUrl);
            }

            int textLimit = question.hasImage()?60:120;
            String text = question.label;
            if (text.length()>textLimit) {
                text = text.substring(0, textLimit - 2);
                text += "..";
            }

            text = SimpleHTMLDecoder.instance().decode(text);
            column.put("text", text);

            actions = new JSONArray();
            for (Choice choice:question.choices) {
                actionObj = new JSONObject();

                String label = choice.label.trim();
                
                label = SimpleHTMLDecoder.instance().decode(label);
                
                if(label.length()>20) {
                    label = label.substring(0, 18);
                    label += "..";
                }
                
                if (choice.isLinkLabel()) {

                    actionObj.put("type", "uri");
                    actionObj.put("label", label);
                    actionObj.put("uri", choice.linkURL);

                } else {

                    actionObj.put("type", "postback");
                    actionObj.put("label", label);
                    actionObj.put("data", choice.parent + " " + label);
                    actionObj.put("displayText", label + " " + text);

                }

                actions.put(actionObj);
            }

            if (actions.length()==0) continue;

            column.put("actions", actions);
            
            defaultAction = new JSONObject();
            defaultAction.put("type", "postback");
            defaultAction.put("label", "View detail");
            defaultAction.put("data", question.id);
            
            column.put("defaultAction", defaultAction);
            
            columns.put(column);
        }

        if (columns.length()==0) throw new RuntimeException("Empty columns");
        if (columns.length()>10) throw new RuntimeException("Too many columns");

        template.put("type", "carousel");
        template.put("columns", columns);

        if (hasImage) {
            template.put("imageSize", "cover");
        }

        JSONObject message = new JSONObject();
        message.put("type", "template");
        message.put("altText", "Menu");
        message.put("template", template);

        return message;
    }
    
	public List<JSONArray> pagination(JSONArray messages) {
		
		List<JSONArray> arrayList = new ArrayList<>();
		
		List<List<Object>> pageList = pagination(messages.toList());
		
		JSONArray array;
		for (List<Object> objList:pageList) {
			array = new JSONArray();			
			for (Object obj:objList) {
				array.put(obj);
			}
			arrayList.add(array);
		}
		
		return arrayList;
	}
    
	public List<List<Object>> pagination(List<Object> messages) {
		
		List<List<Object>> array = new ArrayList<>();
		
		int fromIndex = 0;
		int toIndex;
		
		while (fromIndex<messages.size()) {
			
			if (fromIndex + 5 <= messages.size()-1) {
				toIndex = fromIndex + 5;				
			} else {
				toIndex = messages.size();
			}
			
			array.add(messages.subList(fromIndex, toIndex));
			fromIndex += 5;
		}
		
		return array;
	}
    
    
    public static void main(String[]args) {
    	
    	String responseText = "2SUL_BDC-สวนหลวง Van Courier 1\n" + 
    			"\n" + 
    			"\n" + 
    			"2TRN_BDC-ตรัง Van Courier 1\n" + 
    			"\n" + 
    			"\n" + 
    			"BGY_SP-บางกรวย Van Courier 1\n" + 
    			"\n" + 
    			"\n" + 
    			"BKB_SP-บางกะปิ Van Courier 1\n" + 
    			"\n" + 
    			"\n" + 
    			"DUD_SP-เดชอุดม Van Courier 1\n" + 
    			"\n" + 
    			"\n" + 
    			"KHN_SP-ขุนหาญ Van Courier 1\n" + 
    			"\n" + 
    			"\n" + 
    			"NOG_SP-หนองแขม Van Courier 1\n" + 
    			"\n" + 
    			"\n" + 
    			"NSE_SP-หนองเสือ Van Courier 1\n" + 
    			"\n" + 
    			"\n" + 
    			"PNB_SP-ปราณบุรี Van Courier 1\n" + 
    			"\n" + 
    			"\n" + 
    			"PYA_SP-ปลายพระยา Van Courier 1\n" + 
    			"\n" + 
    			"\n" + 
    			"TMG_SP-ท่ามะกา Van Courier 1\n" + 
    			"\n" + 
    			"\n" + 
    			"TSK_SP-ทับสะแก Van Courier 2\n" + 
    			"\n" + 
    			"\n" + 
    			"WNO_SP-วังน้อย Van Courier 1\n\n\ntest";
    	ResponseObject messages = new ResponseObject(responseText);
    	
    	LINEAPI lineAPI = LINEAPI.instance();
    	
    	List<JSONArray> pages = lineAPI.pagination(lineAPI.createMessages(messages));
    	
    	for (JSONArray page:pages) {
    		System.out.println(page);
    	}
    	
    }
}
