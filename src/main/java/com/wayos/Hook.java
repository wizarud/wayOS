package com.wayos;

import org.json.JSONObject;

import com.wayos.expression.VarExpression;

import java.io.Serializable;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

/**
 * Hook is like an Event Listener of node. 
 * a Node can has many hooks.
 * 
 * Created by eossth on 15/2/2018 AD.
 */
public class Hook implements Serializable, Comparable<Hook> {

    public enum Match {
    	
    	/**
    	 * First version of hook
    	 */
        All(1.0f),
        Head(1.0f),
        Body(1.0f),
        Tail(1.0f),
        
        /**
         * Keyword Hook
         */
        Words(1.0f),
        
        /**
         * Number comparison hook
         */
        GreaterThan(1.0f),
        GreaterEqualThan(1.0f),
        LowerThan(1.0f),
        LowerEqualThan(1.0f),
        
        /**
         * Internal Hook
         */
        Mode(1.0f);

        public final float initWeight;
        
        Match(float initWeight) {
            this.initWeight = initWeight;
        }
        
    }

    public final String text;

    public final Match match;

    public float weight;
    
    /**
     * Create instance of hook
     * 
     * @param text Text that hook will match
     * @param match Match Type
     */
    public Hook(String text, Match match) {
        this(text, match, match.initWeight);
    }

    public Hook(String text, Match match, float weight) {
        this.text = text;
        this.match = match;
        this.weight = weight;
    }

    public void feedback(float feedback) {
        weight += feedback*weight;
        if (weight < 0)
            weight = 0;
    }

    public boolean matched(MessageObject messageObject) {
    	
        String input = messageObject.toString().toLowerCase();

        if (match == Match.All)
            return input.equalsIgnoreCase(text);
        if (match == Match.Head)
            return input.startsWith(text.toLowerCase());
        if (match == Match.Tail)
            return input.endsWith(text.toLowerCase());
        if (match == Match.Body) {
            return input.contains(text.toLowerCase());
        }

        Locale locale = Locale.getDefault();

        /**
         * Number Matched, Use Math >= > <= < operators
         */
        if (VarExpression.isNumeric(input) && VarExpression.isNumeric(text)) {

            try {

                NumberFormat formatter = NumberFormat.getInstance(locale);

                double inputNumber = formatter.parse(input).doubleValue();
                double hookNumber = formatter.parse(text).doubleValue();

                if (match == Match.GreaterEqualThan)
                    return inputNumber >= hookNumber;
                if (match == Match.GreaterThan)
                    return inputNumber > hookNumber;
                if (match == Match.LowerEqualThan)
                    return inputNumber <= hookNumber;
                if (match == Match.LowerThan)
                    return inputNumber < hookNumber;

            } catch (ParseException e) {
            	
                e.printStackTrace();
            }

        }

        Object modeObject = messageObject.attr("mode");
        return modeObject!=null && modeObject.toString().equalsIgnoreCase(text);
    }

    @Override
    public String toString() {
        if (match == Match.All)
            return text;
        if (match == Match.Head)
            return text+"*";
        if (match == Match.Tail)
            return "*"+text;
        if (match == Match.Body)
            return "*"+text+"*";
        if (match == Match.GreaterThan)
            return ">"+text;
        if (match == Match.GreaterEqualThan)
            return ">="+text;
        if (match == Match.LowerThan)
            return "<"+text;
        if (match == Match.LowerEqualThan)
            return "<="+text;
        //if (match == Match.Words)
          //  return "\"" + text + "\"";
        if (match == Match.Words)
            return text;

        //Match
        return "["+text+"]";
    }

    @Override
    public int hashCode() {
        return Objects.hash(match, text);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Hook) {
            Hook another = (Hook)obj;
            return match == another.match && Objects.equals(text, another.text) && weight == another.weight;
        }
        return false;
    }

    public static List<Hook> build(String [] hooks, Match match) {

        List<Hook> hookList = new ArrayList<>();

        if (hooks!=null) {
            String hook;
            for (int i=0; i<hooks.length; i++) {
                hook = hooks[i].trim();
                if (!hook.isEmpty()) {
                    hookList.add(build(hook, match, match.initWeight));
                }
            }
        }

        return hookList;
    }

    public static Hook build(JSONObject jsonObject) {
    	
        String text = jsonObject.getString("text");
        Match match = Match.valueOf(jsonObject.getString("match"));
        float weight = jsonObject.getFloat("weight");
        
        return build(text, match, weight);
        
    }
    
    public static Hook build(String text, Hook.Match match) {
    	
        if (match == Hook.Match.GreaterEqualThan ||
        	match == Hook.Match.GreaterThan ||
        	match == Hook.Match.LowerEqualThan ||
        	match == Hook.Match.LowerThan) {
        	
            return new NumberHook(text, match);
        }
        	
        if (match == Hook.Match.Words) {
        	
            return new KeywordsHook(text, match);
        }
    	
        return new Hook(text, match);
    }

    public static Hook build(String text, Match match, float weight) {
    	
        if (match == Hook.Match.GreaterEqualThan ||
            match == Hook.Match.GreaterThan ||
            match == Hook.Match.LowerEqualThan ||
            match == Hook.Match.LowerThan) {
            	
            return new NumberHook(text, match, weight);
        }
            	
        if (match == Hook.Match.Words) {
            	
            return new KeywordsHook(text, match, weight);
        }

        return new Hook(text, match, weight);
    }

    public static JSONObject json(Hook hook) {
    	
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("text", hook.text);
        jsonObject.put("match", hook.match);
        jsonObject.put("weight", hook.weight);
        
        return jsonObject;
    }

    public static String toString(List<Hook> hookList) {
    	
        StringBuilder sb = new StringBuilder();
        for (Hook hook:hookList) {
            if (Match.Mode==hook.match) continue;
            sb.append(hook.text);
            sb.append(" ");
        }
        
        return sb.toString().trim();
    }
    
    public static String toCSVRow(List<Hook> hookList) {
    	
        StringBuilder sb = new StringBuilder();
        for (Hook hook:hookList) {
            sb.append(hook.text);
            sb.append(", ");
        }
        
        return sb.toString().trim();
    }

	@Override
	public int compareTo(Hook otherHook) {
		
		return this.text.compareTo(otherHook.text);
	}

}