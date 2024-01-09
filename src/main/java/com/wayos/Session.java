package com.wayos;

import com.wayos.command.CommandNode;
import com.wayos.command.admin.AdminCommandNode;
import com.wayos.command.talk.ProblemCommandNode;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.*;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by eossth on 7/14/2017 AD.
 */
@SuppressWarnings("serial")
public class Session implements Serializable {

    public static final int MAX_ROUTE = 200;
    
    public SessionListener sessionListener;

    public Session register(SessionListener sessionListener) {
    	
        this.sessionListener = sessionListener;
        
        return this;
    }

    public static class Entry implements Serializable {
    	
        public final MessageObject messageObject;
        public final Node node;
        public Entry(MessageObject messageObject, Node node) {
            this.messageObject = messageObject;
            this.node = node;
        }
        
    }

    private Context context;

    private boolean problemSolved = false;

    private String mode = null;

    private boolean silent = false;

    private boolean learning = false;

	private boolean parsing = false;
	
    private Entry lastEntry;

    private final Map<String, String> variableMap = new TreeMap<>(Collections.reverseOrder());

	private Set<String> variableChangedNameSet = new HashSet<>();
    
	private final List<Node> protectedList = new ArrayList<>();

    private final List<AdminCommandNode> adminCommandList = new ArrayList<>();

    private final List<CommandNode> commandList = new ArrayList<>();
	
    private int routeCount;
		
    public Session() {}

    public Session context(Context context) {
    	
    	Context oldContext = this.context;
    	
        this.context = context;
        
        if (this.sessionListener!=null) {
        	
        	this.sessionListener.onContextChanged(this, oldContext, this.context);
        	
        }
        
        return this;
    }
    
    public Context context() {
    	
    	return this.context;
    }
    
    public void clearMode() {
    
    	mode = null;
    }
    
    public void setMode(String mode) {
    
    	this.mode = mode;
    }
    
    public boolean silent() {
    	
    	return silent;
    }
    
    public void silent(boolean silent) {
    	
    	this.silent = silent;
    }
    
    public boolean learning() {
    	
    	return learning;
    }
    
    public void learning(boolean learning) {
    	
    	this.learning = learning;
    }
    
    public Map<String, String> vars() {
    	
    	return variableMap;
    }
    
    public String vars(String name) {
    	
        String value = variableMap.get(name);
        
        if (value==null) value = "";
        
        return value;
    }
    
    public void vars(String name, String value) {
    	
    	if (value==null || value.trim().isEmpty()) {
    		
    		removeVariable(name);
    		
    	} else {
    		
    		variableMap.put(name, value);
    		
    	}
    	
		variableChangedNameSet.add(name);
    }
    
    public Set<String> getVariableChangedNameSet() {
    	
    	return variableChangedNameSet;
    }
    
    public void removeVariable(String name) {
    	
        variableMap.remove(name);
        
		variableChangedNameSet.remove(name);
        
    }    
    
    public List<Node> protectedList() {
    	
    	return protectedList;
    }
    
    public List<AdminCommandNode> adminCommandList() {
    	
    	return adminCommandList;
    }
    
    public List<CommandNode> commandList() {
    	
    	return commandList;
    }
        
    public String parse(MessageObject messageObject) {
    	    	
		String result = null;
		
		try {
						
			/**
			 * Clear Problem for Special Commands
			 */
			if (messageObject.toString().equals("greeting") || 
					messageObject.toString().equals("silent")) {
				
				clearProblem();
				
			}
			
			/**
			 * Clear Problem for Shouting!!!
			 */
			if (messageObject.toString().endsWith("!")) {
								
				String text = messageObject.toString();
				
				text = text.substring(0, text.length()-1);//Clear tail
				
				System.out.println("Shouting: " + text);
				
				messageObject.setText(text);
				
				clearProblem();
			}
			
			routeCount = 0;//TODO: Should move to clearLastEntry or not???

			boolean isAdminCommandExecuted = false;
	        
			for (AdminCommandNode node : adminCommandList) {
				
				if (node.matched(messageObject)) {
					
					result = node.execute(messageObject);
					isAdminCommandExecuted = true;
					
					break;
					
				}
				
			}
						
			if (isAdminCommandExecuted) {

				clearProblem();

			} else {
				
				/**
				 * Moved from Top to Here
				 * Project injection from , <comma space>
				 */
				String input = messageObject.toString();
				input = input.replace(",", "");
				messageObject.setText(input);
				
		        /**
		         * Recursive Protection!
		         */
		    	if (parsing) {
		    		
		    		return "";
		    	}
				
				/**
				 * If mode is enable, make it bias for this session.
				 */
		        if (mode!=null && !mode.trim().isEmpty()) {
		        	
		        	messageObject.append(" " + mode);
		        }
		        
				for (CommandNode node : commandList) {
					
					if (node.matched(messageObject)) {
						
						parsing = true;						
						result = node.execute(messageObject);
						
						break;
						
					}
				}
								
				if (problemSolved) {
					
					clearProblem();
				}
			}
			
		} finally {
						
			clearLastEntry();
			
			parsing = false;
			
	        //Reset Index & Separator
	        variableMap.remove("#@");
	        variableMap.remove("#$");    
	        
    		fireVariablesChangedEvent();
	        
		}
		
		return silent || result==null ? "" : result;    	    	
    }

	public void fireVariablesChangedEvent() {
		
		if (!variableChangedNameSet.isEmpty() && sessionListener!=null) {
			
			sessionListener.onVariablesChanged(this);
			variableChangedNameSet.clear();;
			
		}
		
	}

    public void solved(boolean problemSolved) {
    	
        this.problemSolved = problemSolved;
        
    }

    public boolean hasProblem() {
    	
        return !commandList.isEmpty() && commandList.get(0) instanceof ProblemCommandNode;
        
    }

    public void clearProblem() {
    	
        while (hasProblem()) {
        	
            commandList.remove(0);
            
        }
    }

    public void setLastEntry(MessageObject messageObject, Node node) {
    	
        lastEntry = new Entry(messageObject, node);
        routeCount ++;
        
    }

    public int getRoundCount() {
    	
        return routeCount;
        
    }

    public void clearLastEntry() {
    	
        lastEntry = null;
        
    }

    public Entry lastEntry() {
    	
        return lastEntry;
        
    }

    public void insert(ProblemCommandNode problemCommandNode) {
    	
        commandList.add(0, problemCommandNode);
        problemSolved = false;
        
    }

    public boolean reachMaximumRoute() {
    	
        return routeCount > MAX_ROUTE;
        
    }

    public final String parameterized(MessageObject messageObject, String responseText) {
    	
    	String parameterizedText;
    	
        /**
         * From Input ## #1 #2..
         */
        parameterizedText = parameterized(paramMap(messageObject), responseText);
        
        /**
         * From Context Properties
         */
        parameterizedText = parameterized(context.reversedOrderPropertiesMap(), parameterizedText);
        
        /**
         * From Session Variables
         */
        parameterizedText = parameterized(variableMap, parameterizedText);
        
        /**
         * Clean Unresolved Variables
         */
        parameterizedText = cleanUnresolvedVariables(parameterizedText);

        return parameterizedText.trim();
    }
    
    /**
     * Generate built-in variables, constants
     * 
     * @param messageObject
     * @return
     */
    private final Map<String, String> paramMap(MessageObject messageObject) {

        if (messageObject==null) return null;

        String input = messageObject.toString();
        
        //System.out.println("Input (##): " + input);
                
        /**
         * Delimiter of Input
         */
        String delimiter = variableMap.get("#s_$");
        
        if (delimiter==null) {
        	
        	delimiter = "\\s+";
        	
        }
        
        String [] params = input.split(delimiter);

        /**
         * Convert input to arrays
         * ## all input
         * #1..#! members
         * #$ length
         * #@ access member at index
         */
        Map<String, String> paramMap = new HashMap<>();
        
        paramMap.put("##", input);
                
        for (int i=0;i<params.length;i++) {
        	
            paramMap.put("#" + (i+1), params[i]);
            
            /**
             * For Last parameter
             */
            if (i==params.length-1) {
            	
                paramMap.put("#!", params[i]);            	
            }
        }
        
        /**
         * Length of parameters
         */
        paramMap.put("#$", "" + params.length);
        
        int index;
        
        try {
        	
        	index = Integer.parseInt(variableMap.get("#@"));
        	
        } catch (Exception e) {
        	
        	index = -1;
        	
        }
        
        if (index>=0 && index<params.length) {
        	
        	paramMap.put("#%", "" + params[index]);        	
        	
        }
        
        /**
         * Input Encode for URL
         */
        try {
        	
            paramMap.put("%%", URLEncoder.encode(input, "UTF-8"));
            
        } catch (UnsupportedEncodingException e) {
        	
            paramMap.put("%%", input);
            
        }

        /**
         * Result from REST or Unknown
         */
        List<String> resultList = messageObject.resultList();
        
        if (resultList!=null) {
        	
            for (int i=0;i<resultList.size();i++) {
            	
                paramMap.put("%" + (i+1), resultList.get(i));
                
            }
            
        }
        
        /**
         * Build-In Current Date & Time Expressions
         */
        Locale locale = context.locale();
        
        if (locale == null) {
        	
        	locale = Locale.US;
        	
        }
        
        /**
         * Time constants
         */
        Calendar calendar = Calendar.getInstance(locale);
        
        long timestamp = calendar.getTimeInMillis();
                         
        paramMap.put("%year", "" + calendar.get(Calendar.YEAR));
        
        paramMap.put("%dayNumber", "" + calendar.get(Calendar.DAY_OF_WEEK));
        
        paramMap.put("%hour", "" + calendar.get(Calendar.HOUR_OF_DAY));
        
        paramMap.put("%minute", "" + calendar.get(Calendar.MINUTE));
        
        paramMap.put("%second", "" + calendar.get(Calendar.SECOND));
        
        paramMap.put("%timestamp", "" + timestamp);
        
        paramMap.put("%timehex", Long.toHexString(timestamp).toUpperCase());

        paramMap.put("%dayString", new SimpleDateFormat("EEEE").format(calendar.getTime()));
        
        paramMap.put("%monthString", new SimpleDateFormat("MMM").format(calendar.getTime()));
        
        paramMap.put("%monthNumber", new SimpleDateFormat("MM").format(calendar.getTime()));
        
        paramMap.put("%date", new SimpleDateFormat("dd").format(calendar.getTime()));
        
        return paramMap;
    }
    
    private final String parameterized(Map<String, String> paramMap, String text) {
    	
        if (paramMap==null) return text;
        
        String output = text;
        
        /**
         * For variableMap = new TreeMap<>(Collections.reverseOrder());
         * Thats mean replace from #z.. to #a to #Z.. #A order.
         */
        
        for (Map.Entry<String, String> entry : paramMap.entrySet()) {
        	
            if (entry.getValue()==null) continue; //Hot Fix to prevent NullPointerException from null variables
            
			/**
			 * Project value injection from ,
			 */
			String value = entry.getValue();
			value = value.replace(",", "");
            
            output = output.replace(entry.getKey(), value);
            
        }
        
        return output;
    }
    
    private String cleanUnresolvedVariables(String parameterizedText) {
    	    	
        Pattern pattern = Pattern.compile("#\\w+");
        Matcher matcher = pattern.matcher(parameterizedText);

        String unresolvedVar;
        String replacer;
        while (matcher.find()) {
        	
            unresolvedVar = matcher.group();
            
            if (unresolvedVar.startsWith("#i_")) {
            	
                replacer = "0";//Integer
                
            } else if (unresolvedVar.startsWith("#s_")) {
            	
                replacer = "";//Empty String
                
            } else {
            	
                replacer = unresolvedVar; //Do nothing
                
            }
            
            parameterizedText = parameterizedText.replace(unresolvedVar, replacer);
        }
        
        //System.out.println("Parameterized: " + parameterizedText);
                
        return parameterizedText;
    }

}
