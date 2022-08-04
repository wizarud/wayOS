package com.wayos.command.wakeup;

import com.wayos.Hook;
import com.wayos.MessageObject;
import com.wayos.Session;
import com.wayos.command.CommandNode;
import com.wayos.command.GreetingCommandNode;
import com.wayos.command.Key;
import com.wayos.command.admin.AdminCommandNode;
import com.wayos.command.admin.AdminContextCommandNode;
import com.wayos.command.admin.RegisterAdminCommandNode;
import com.wayos.command.data.*;
import com.wayos.command.talk.MultilineInputTalkCommandNode;

import java.util.Arrays;

/**
 * Created by eossth on 7/31/2017 AD.
 */
public class MultilineInputWakeupCommandNode extends CommandNode {

    public static final Key KEY = new Key("\uD83D\uDE0A", "?", Arrays.asList("\uD83D\uDC4D", "\uD83D\uDC4E", "ไม่"));

    public MultilineInputWakeupCommandNode(Session session) {
        super(session);
    }

    @Override
    public String execute(MessageObject messageObject) {

        /**
         * Protected from bad words
         */
        session.protectedList().clear();

        /**
         * Command list is Ordered by Priority
         */
        session.adminCommandList().clear();
         
        /**
         * Admin Switching Commands
         */
        session.adminCommandList().add(new AdminCommandNode(new AdminContextCommandNode(session, new String[]{"admin"}, Hook.Match.All)));
                
        //session.adminCommandList().add(new AdminCommandNode(new DiamondCommandNode(session, new String[]{"แก้"}, Hook.Match.Head, "\\|")));
        
        /*
        session.adminCommandList().add(new AdminCommandNode(new RegisterAdminCommandNode(session, new String[]{"ลงทะเบียนผู้ดูแล"})));
        session.adminCommandList().add(new AdminCommandNode(new DebugCommandNode(session, new String[]{"ดูทั้งหมด"})));
        */
        
        /**
         * Replace cmd "load", "โหลดข้อมูล" with hard command to prevent talk to admin cmd
         */
        session.adminCommandList().add(new AdminCommandNode(new LoadDataCommandNode(session, new String[]{"โหลตๆๆ"})));
        session.adminCommandList().add(new AdminCommandNode(new SaveDataCommandNode(session, new String[]{"บันทึกข้อมูล"})));
        
        /*
        session.adminCommandList().add(new AdminCommandNode(new BackupDataCommandNode(session, new String[]{"สำรองข้อมูล"})));
        session.adminCommandList().add(new AdminCommandNode(new RestoreDataCommandNode(session, new String[]{"กู้ข้อมูล"})));
        session.adminCommandList().add(new AdminCommandNode(new ClearDataCommandNode(session, new String[]{"ล้างข้อมูล"})));
        session.adminCommandList().add(new AdminCommandNode(new ImportRawDataFromWebCommandNode(session, new String[]{"ใส่ข้อมูลดิบจากเวป"})));
        session.adminCommandList().add(new AdminCommandNode(new ImportRawDataCommandNode(session, new String[]{"ใส่ข้อมูลดิบ"})));
        session.adminCommandList().add(new AdminCommandNode(new ExportRawDataCommandNode(session, new String[]{"ดูข้อมูลดิบ"})));
        */

        session.commandList().clear();
        session.commandList().add(new RegisterAdminCommandNode(session, new String[]{"ลงทะเบียนผู้ดูแล"}));
        session.commandList().add(new GreetingCommandNode(session, new String[]{"greeting", "ดีจ้า"}));
        /*
        session.commandList().add(new WakeCommandNode(session, new String[]{"silent"}));
        session.commandList().add(new FeedbackCommandNode(session, new String[]{"\uD83D\uDC4D"}, "\uD83D\uDE0A", 0.1f));
        session.commandList().add(new FeedbackCommandNode(session, new String[]{"\uD83D\uDC4E"}, "\uD83D\uDE1F", -0.1f, KEY));
        session.commandList().add(new ForwardCommandNode(session, new String[]{"Next"}, KEY));
        */
        
        final float LOWER_BOUND = 0.7f;
        final int MIN_LINES_EXECUTE = 5;
        final String responsesDelimiter = System.lineSeparator() + System.lineSeparator() + System.lineSeparator(); 
        
        session.commandList().add(new MultilineInputTalkCommandNode(session, LOWER_BOUND, MIN_LINES_EXECUTE, responsesDelimiter));
        
        return "\\(^o^)/";
    }
}
