package com.wayos.command;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

public class Key {
	
    public static final Key LEARN = new Key("\uD83D\uDE0A", "?", Arrays.asList("\uD83D\uDC4D", "\uD83D\uDC4E", "ไม่"));

    public final String doneMsg;
    public final String questMsg;
    public final List<String> cancelKeys;

    public Key(String doneMsg, String questMsg, List<String> cancelKeys) {
        this.doneMsg = doneMsg;
        this.questMsg = questMsg;
        this.cancelKeys = cancelKeys;
    }
}
