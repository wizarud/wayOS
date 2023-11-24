package com.wayos.expression;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.wayos.MessageObject;
import com.wayos.Session;

public class RegularExpression extends Expression {

    public RegularExpression(Session session, String[] arguments) {
        super(session, arguments);
    }

    @Override
    public String execute(MessageObject messageObject) {

        String [] args = parameterized(messageObject, arguments);

        if (args.length==3) {

            String text = args[1].replace("`", "");
            
            messageObject.addResult(regx(text, args[2]));
            
            return "";
        }

        return super.execute(messageObject);

    }

    private String regx(String input, String patternString) {

        Pattern pattern = Pattern.compile(patternString);
        Matcher matcher = pattern.matcher(input);

        StringBuilder result = new StringBuilder();
        String matchedString;
        while (matcher.find()) {
            matchedString = matcher.group();
            result.append(matchedString);
        }

        return result.toString().trim();
    }
}
