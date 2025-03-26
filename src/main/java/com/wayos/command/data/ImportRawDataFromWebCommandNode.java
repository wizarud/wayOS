package com.wayos.command.data;

import com.wayos.Context;
import com.wayos.Hook;
import com.wayos.MessageObject;
import com.wayos.Node;
import com.wayos.Session;
import com.wayos.command.CommandNode;
import com.wayos.context.FileContext;
import com.wayos.context.RemoteContext;
import com.wayos.util.FileStream;
import com.wayos.util.RemoteStream;
import com.wayos.util.Stream;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Wisarut Srisawet on 7/31/2017 AD.
 */
public class ImportRawDataFromWebCommandNode extends CommandNode {

    private File dir;

    public ImportRawDataFromWebCommandNode(Session session, String [] hooks) {
        this(session, hooks, null);
    }

    public ImportRawDataFromWebCommandNode(Session session, String [] hooks, File dir) {
        super(session, hooks, Hook.Match.Head);
        this.dir = dir;
    }

    @Override
    public String execute(MessageObject messageObject) {

        String params = cleanHooksFrom(messageObject.toString());

        try {
            Document doc = Jsoup.connect(params).userAgent("Mozilla/5.0").get();
            String content = doc.body().text();
            String [] sentences = content.split("\\s+");

            List<String> sentenceList = new ArrayList<>();

            String text, lastText;
            for (String sentence:sentences) {
                text = sentence.trim();
//            if (session.context.split(text).size() > 5) {
                if (text.length() > 20) {
                    sentenceList.add(text);
                } else if (!sentenceList.isEmpty()) {
                    lastText = sentenceList.remove(sentenceList.size()-1);
                    sentenceList.add(lastText + " " + text);
                }
            }

            String fileName;
            Stream streamIndex;
            Context context;
            if (dir!=null && dir.isDirectory()) {
                fileName = params.replace("https://", "").replace("http://", "").replace("/", "-");
                streamIndex = new FileStream(dir, session.context().name() + ".index");
                context = new FileContext(dir, fileName);
            } else {
                fileName = params.replace("https://", "").replace("http://", "");
                streamIndex = new RemoteStream(fileName + ".index");
                context = new RemoteContext(fileName);
            }

            Node newNode = null;

            for (String sentence:sentenceList) {

                if (newNode!=null) {
                    newNode.setResponse(sentence);
                    context.add(newNode);
                }

                newNode = Node.build(session.context().split(sentence));
            }

            context.save();

            String [] indexData = streamIndex.read().split(System.lineSeparator());

            List<String> indexList = new ArrayList<>();

            for (String index:indexData) {
                if (index.trim().isEmpty()) continue;
                indexList.add(index.trim());
            }

            indexList.add(fileName);

            if (dir!=null && dir.isDirectory()) {
                List<File> removeFileList = new ArrayList<>();
                while (indexList.size()>5) {
                    removeFileList.add(new File(dir, indexList.remove(0)));
                }

                for (File file:removeFileList) {
                    if (file.exists()) {
                        if (!file.delete()) break;
                    }
                }

            } else {
                while (indexList.size()>5) {
                    indexList.remove(0);
                }
            }

            StringBuilder indexString = new StringBuilder();

            for (String index:indexList) {
                indexString.append(index);
                indexString.append(System.lineSeparator());
            }

            streamIndex.write(indexString.toString().trim());

            session.context().load();

            return successMsg();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return failMsg();
    }

}
