package com.wayos.command.wakeup;

import com.wayos.Hook;
import com.wayos.MessageObject;
import com.wayos.Session;
import com.wayos.command.CommandNode;
import com.wayos.command.DebugCommandNode;
import com.wayos.command.EntityManagerCommandNode;
import com.wayos.command.GreetingCommandNode;
import com.wayos.command.Key;
import com.wayos.command.WakeCommandNode;
import com.wayos.command.admin.AdminCommandNode;
import com.wayos.command.admin.RegisterAdminCommandNode;
import com.wayos.command.data.*;
import com.wayos.command.talk.FeedbackCommandNode;
import com.wayos.command.talk.FlowTalkCommandNode;

import java.util.Arrays;

/**
 * Created by eossth on 7/31/2017 AD.
 */
public class FlowWakeupCommandNode extends CommandNode {

    public static final Key KEY = new Key("\uD83D\uDE0A", "?", Arrays.asList("\uD83D\uDC4D", "\uD83D\uDC4E", "ไม่"));

    public FlowWakeupCommandNode(Session session) {
        this (session, null);
    }

    public FlowWakeupCommandNode(Session session, String [] hooks) {
        super(session, hooks);
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
        session.adminCommandList().add(new AdminCommandNode(new EntityManagerCommandNode(session, new String[]{"แก้ไขข้อมูล"}, Hook.Match.Head, "\\|")));        
        session.adminCommandList().add(new AdminCommandNode(new ImportQuestionareDataCommandNode(session, new String[]{"importqa"})));
        session.adminCommandList().add(new AdminCommandNode(new ImportCSVDataCommandNode(session, new String[]{"importtsv"}, "\t")));
        session.adminCommandList().add(new AdminCommandNode(new ExportCSVDataCommandNode(session, new String[]{"exporttsv"}, "\t")));        
        session.adminCommandList().add(new AdminCommandNode(new RegisterAdminCommandNode(session, new String[]{"ลงทะเบียนผู้ดูแล"})));
        session.adminCommandList().add(new AdminCommandNode(new DebugCommandNode(session, new String[]{"ดูทั้งหมด"})));
        
        /**
         * Replace cmd "load", "โหลดข้อมูล" with hard command to prevent talk to admin cmd
         */
        session.adminCommandList().add(new AdminCommandNode(new LoadDataCommandNode(session, new String[]{"โหลตๆๆ"})));
        session.adminCommandList().add(new AdminCommandNode(new SaveDataCommandNode(session, new String[]{"บันทึกข้อมูล"})));
        session.adminCommandList().add(new AdminCommandNode(new BackupDataCommandNode(session, new String[]{"สำรองข้อมูล"})));

        session.commandList().clear();
        session.commandList().add(new RegisterAdminCommandNode(session, new String[]{"ลงทะเบียนผู้ดูแล"}));
        session.commandList().add(new GreetingCommandNode(session, new String[]{"greeting", "ดีจ้า"}));
        session.commandList().add(new WakeCommandNode(session, new String[]{"silent"}));
        session.commandList().add(new FeedbackCommandNode(session, new String[]{"\uD83D\uDC4D"}, "\uD83D\uDE0A", 0.1f));
        session.commandList().add(new FeedbackCommandNode(session, new String[]{"\uD83D\uDC4E"}, "\uD83D\uDE1F", -0.1f, KEY));
        //session.commandList().add(new ForwardCommandNode(session, new String[]{"Next"}, KEY));
        
        session.commandList().add(new FlowTalkCommandNode(session, KEY));
                
        return "\\(^o^)ๆ";
    }
}
