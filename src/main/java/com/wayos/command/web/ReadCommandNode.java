package com.wayos.command.web;

import com.wayos.Hook;
import com.wayos.MessageObject;
import com.wayos.Session;
import com.wayos.command.CommandNode;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;

/**
 * Created by Wisarut Srisawet on 7/31/2017 AD.
 */
public class ReadCommandNode extends CommandNode {

    public final String url;

    public ReadCommandNode(Session session, String [] hooks, String url) {
        super(session, hooks, Hook.Match.Head);
        this.url = url;
    }

    /*
    @Override
    public boolean matched(MessageObject messageObject) {
        try {
            new URL(url+clean(messageObject.toString()));
        } catch (Exception e) {
            return false;
        }
        return true;
    }
    */

    @Override
    public String execute(MessageObject messageObject) {

        String params = cleanHooksFrom(messageObject.toString());

        try {

            Document doc = Jsoup.connect(url+params).userAgent("Mozilla/5.0").get();
            return doc.body().text();

        } catch (IOException e) {
            e.printStackTrace();
            return e.getMessage();
        }

    }

}
