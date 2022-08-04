package com.wayos.command.talk;

import com.wayos.MessageObject;
import com.wayos.Session;
import com.wayos.expression.Expression;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ResponseCommandNode extends ProblemCommandNode {

    public final String responseText;

    public ResponseCommandNode(Session session, String responseText) {
        super(session);
        this.responseText = responseText;
    }

    @Override
    public String execute(MessageObject messageObject) {

        Pattern pattern = Pattern.compile("\\`(.|\\n|\\r|\\t)*?\\`", Pattern.MULTILINE);
        Matcher matcher = pattern.matcher(responseText);

        String evaluatedText = responseText;
        String expression;
        while (matcher.find()) {
            expression = matcher.group();
            evaluatedText = evaluatedText.replace(expression, Expression.build(session, expression).execute(messageObject));
        }

        return session.parameterized(messageObject, evaluatedText);
    }

}
