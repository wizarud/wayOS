package com.wayos;

import java.util.ArrayList;
import java.util.List;

public class NumberHook extends Hook {

    public NumberHook(String text, Match match) {
        super(text, match);
    }

    public NumberHook(String text, Match match, double weight) {
        super(text, match, weight);
    }

    @Override
    public boolean matched(MessageObject messageObject) {
    	
        /**
         * Parameterized Hooks
         */
    	String text = super.parameterized(messageObject);

        List<String> wordList = messageObject.wordList();

        if (match == Match.GreaterThan) {
        	
            Float targetNumber;
            try {
                targetNumber = Float.parseFloat(text);
            } catch (Exception e) {
                targetNumber = 0f;
            }

            List<Float> inputNumberList = numberList(wordList);
            
            //System.out.println(targetNumber);
            //System.out.println(inputNumberList);
            
            boolean result = false;
            for (Float number:inputNumberList) {
                if (number>targetNumber)
                    result = true;
            }
            return result;
            
        }
        
        if (match == Match.GreaterEqualThan) {
        	
            Float targetNumber;
            try {
                targetNumber = Float.parseFloat(text);
            } catch (Exception e) {
                targetNumber = 0f;
            }

            List<Float> inputNumberList = numberList(wordList);
            boolean result = false;
            for (Float number:inputNumberList) {
                if (number>=targetNumber)
                    result = true;
            }
            return result;
            
        }
        
        if (match == Match.LowerThan) {
        	
            Float targetNumber;
            try {
                targetNumber = Float.parseFloat(text);
            } catch (Exception e) {
                targetNumber = 0f;
            }

            List<Float> inputNumberList = numberList(wordList);
            boolean result = false;
            for (Float number:inputNumberList) {
                if (number<targetNumber)
                    result = true;
            }
            return result;
            
        }
        if (match == Match.LowerEqualThan) {
        	
            Float targetNumber;
            try {
                targetNumber = Float.parseFloat(text);
            } catch (Exception e) {
                targetNumber = 0f;
            }

            List<Float> inputNumberList = numberList(wordList);
            boolean result = false;
            for (Float number:inputNumberList) {
                if (number<=targetNumber)
                    result = true;
            }
            return result;
            
        }

        return false;
    }

    private List<Float> numberList(List<String> wordList) {
    	
        List<Float> numberList = new ArrayList<>();
        for (String word:wordList) {
            try {
                numberList.add(Float.parseFloat(word));
            } catch (Exception e) {

            }
        }
        
        return numberList;
    }

}
