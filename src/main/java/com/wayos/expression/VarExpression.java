package com.wayos.expression;

import com.wayos.MessageObject;
import com.wayos.Session;
import com.wayos.util.SignatureValidator;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;

public class VarExpression extends Expression {
	
    //NumberFormat formatter = NumberFormat.getInstance(session.context.locale());
	NumberFormat formatter = new DecimalFormat("0.########");

    public VarExpression(Session session, String[] arguments) {
        super(session, arguments);
    }

    @Override
    public String execute(MessageObject messageObject) {
        return var(parameterized(messageObject, arguments)[0]);
    }

    /**
     * Support multiple values with "&" combination
     * @param queryParams
     * @return
     */
    @Deprecated
    protected final String _var(String queryParams) {
        String [] params = queryParams.split("&");
        String [] tokens;
        String name, value;
        for (String paramValue:params) {
            if (!paramValue.contains("=")) continue;
            tokens = paramValue.split("=");
            name = tokens[0].trim();
            if (paramValue.trim().endsWith("=")) {
                session.removeVariable("#" + name);
                continue;
            }
            if (tokens.length!=2) continue;
            value = tokens[1].trim();
            session.vars("#" + name, optValue(name, value));
        }
        return "";
    }
    
    /**
     * HOTFIXED!!!
     * Only support single parameter assignment
     * @param queryParams
     * @return
     */
    protected final String var(String queryParam) {
        String [] tokens;
        String name, value;
        if (!queryParam.contains("=")) return "";
        tokens = queryParam.split("=", 2);
        name = tokens[0].trim();
        if (queryParam.trim().endsWith("=")) {
            session.removeVariable("#" + name);
        }
        if (tokens.length!=2) return "";
        value = tokens[1].trim();
        session.vars("#" + name, optValue(name, value));
        return "";
    }

    public static boolean isNumeric(String strNum) {
        try {
        	Double.parseDouble(strNum);
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    private String optValue(String name, String newValue) {

        String oldValue = session.vars("#" + name);
        
        /**
         * Check there is prefix operand or not?
         * TODO: Try to support Mod with opt %, Lets test again!
         */
        if (newValue.startsWith("+") || newValue.startsWith("-") || newValue.startsWith("*") || newValue.startsWith("/") || newValue.startsWith("^") || newValue.startsWith("%") || newValue.startsWith("|")) {

            try {
            	
                String opt = newValue.substring(0, 1);
                
                newValue = newValue.substring(1).trim();

                /**
                 * If new variable, just set
                 */
                if (oldValue==null || oldValue.isEmpty()) return newValue;

                if (isNumeric(oldValue) && isNumeric(newValue)) {

                    /**
                     * Supports + - * /
                     */

                    double oldNumber = formatter.parse(oldValue).doubleValue();
                    double newNumber = formatter.parse(newValue).doubleValue();

                    if (opt.equals("+"))
                        return formatter.format(oldNumber + newNumber);

                    if (opt.equals("-"))
                        return formatter.format(oldNumber - newNumber);

                    if (opt.equals("*"))
                        return formatter.format(oldNumber * newNumber);

                    if (opt.equals("/"))
                        return formatter.format(oldNumber / newNumber);

                    if (opt.equals("^"))
                        return formatter.format(Math.pow(oldNumber, newNumber));
                    
                    //TODO: Try to support Mod %, Lets test again!
                    if (opt.equals("%"))
                        return formatter.format(oldNumber % newNumber);

                } else if (isNumeric(oldValue)) {

                    /**
                     * Support + - *
                     */

                    if (opt.equals("+"))
                        return oldValue + newValue;

                    if (opt.equals("-"))
                        return oldValue.replace(newValue, "");

                    if (opt.equals("*")) {
                        double oldNumber = formatter.parse(oldValue).doubleValue();
                        int round = (int) Math.round(oldNumber);
                        StringBuilder result = new StringBuilder();
                        for (int i=0;i<round;i++) {
                            result.append(newValue);
                            result.append(System.lineSeparator());
                        }
                        return result.toString().trim();
                    }

                    return newValue;

                } else if (isNumeric(newValue)) {

                    /**
                     * Support + - *
                     */

                    if (opt.equals("+"))
                        return oldValue + newValue;

                    if (opt.equals("-"))
                        return oldValue.replace(newValue, "").trim();

                    if (opt.equals("*")) {
                        double newNumber = formatter.parse(newValue).doubleValue();
                        int round = (int) Math.round(newNumber);
                        StringBuilder result = new StringBuilder();
                        for (int i=0;i<round;i++) {
                            result.append(oldValue);
                            result.append(System.lineSeparator());
                        }
                        return result.toString().trim();
                    }

                    return newValue;

                } else {

                    /**
                     * String Operator, newValue as a parameter
                     * + for Concatenation
                     * | for Union
                     * - for Removing
                     * * for Combination
                     * / for Splitting 
                     * ^ for Signing
                     */
                	/* DEBUG!!
                	if (opt.equals("-")) {
                		System.err.println(oldValue);
                		System.err.println("-");
                		System.err.println(newValue);
                	}
                	
                	*/
                	
                	/**
                	 * Concatenation
                	 */
                    if (opt.equals("+")) {
                    	
                        return oldValue + newValue;                    	
                    }
                    
                	/**
                	 * Union
                	 */
                    if (opt.equals("|")) {
                    	
                    	if (!oldValue.contains(newValue))
                    		return oldValue + newValue;
                    	
                    	return oldValue;
                    }
                    
                    /**
                     * Remove
                     */
                    if (opt.equals("-")) {
                    	
                    	return oldValue.replace(newValue, "").trim();
                    }
                    
                    /**
                     * Combination
                     */
                    if (opt.equals("*")) {
                    	
                    	/**
                    	 * Do the simple combination
                    	 */
                    	String delimiter = session.vars("#s_$");
                    	
                    	if (delimiter.isEmpty() || delimiter.equals("\\s+")) {
                    		
                    		delimiter = " ";
                    	}
                    	
                    	return (oldValue + newValue) + delimiter + (newValue + oldValue);
                    }
                    
                    /**
                     * Replace String variable with specific delimiter
                     * Ex. hello world how are you >> hello|world|how|are|you
                     */
                    if (opt.equals("/")) {
                    	
                    	/**
                    	 * Do the simple combination
                    	 */
                    	String delimiter = session.vars("#s_$");
                    	
                    	if (delimiter.isEmpty() || delimiter.equals("\\s+")) {
                    		
                    		delimiter = " ";
                    	}
                    	                    	
                    	return oldValue.replace(newValue, delimiter);
                    }
                    
                    /**
                     * Sign, newValue as a secret
                     */
                    if (opt.equals("^")) {
                    	
                    	SignatureValidator signatureValidator;
                    	
                    	String algorithm = session.context().prop("algorithm");
                    	if (algorithm==null) {
                    		//Use HmacSHA1 as the default algorithm
                    		signatureValidator = new SignatureValidator(newValue.getBytes());
                    	} else {
                    		signatureValidator = new SignatureValidator(newValue.getBytes(), algorithm);
                    	}
                    	
                    	return signatureValidator.generateSignature(oldValue.getBytes());
                    }
                    
                    return newValue;
                }
            } catch (ParseException e) {
              e.printStackTrace();
            }

        }
        
        return newValue;
    }
}
