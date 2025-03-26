package com.wayos.experiment.command;

import com.wayos.MessageObject;
import com.wayos.Session;
import com.wayos.command.CommandNode;
import com.wayos.command.DebugCommandNode;
import com.wayos.command.DisableTeacherCommandNode;
import com.wayos.command.EnableTeacherCommandNode;
import com.wayos.command.Key;
import com.wayos.command.admin.AdminCommandNode;
import com.wayos.command.admin.RegisterAdminCommandNode;
import com.wayos.command.data.*;
import com.wayos.command.talk.FeedbackCommandNode;

/**
 * Created by Wisarut Srisawet on 7/31/2017 AD.
 */
public class WakeupCommandNode extends CommandNode {

    public WakeupCommandNode(Session session) {
        this (session, null);
    }

    public WakeupCommandNode(Session session, String [] hooks) {
        super(session, hooks);
    }

    @Override
    public String execute(MessageObject messageObject) {

        try {
        	
            session.context().load();
            
        } catch (Exception e) {
        	
        	throw new RuntimeException(e);
        }

        /**
         * Protected from bad words
         */
        session.protectedList().clear();

        /**
         * Command list is Ordered by Priority
         */
        session.adminCommandList().clear();
        session.adminCommandList().add(new AdminCommandNode(new RegisterAdminCommandNode(session, new String[]{"ลงทะเบียนผู้ดูแล"})));
        session.adminCommandList().add(new AdminCommandNode(new DebugCommandNode(session, new String[]{"ดูทั้งหมด"})));
        session.adminCommandList().add(new AdminCommandNode(new LoadDataCommandNode(session, new String[]{"โหลดข้อมูล"})));
        session.adminCommandList().add(new AdminCommandNode(new SaveDataCommandNode(session, new String[]{"บันทึกข้อมูล"})));
        session.adminCommandList().add(new AdminCommandNode(new BackupDataCommandNode(session, new String[]{"สำรองข้อมูล"})));
        session.adminCommandList().add(new AdminCommandNode(new RestoreDataCommandNode(session, new String[]{"กู้ข้อมูล"})));
        session.adminCommandList().add(new AdminCommandNode(new ClearDataCommandNode(session, new String[]{"ล้างข้อมูล"})));
        session.adminCommandList().add(new AdminCommandNode(new ImportRawDataFromWebCommandNode(session, new String[]{"ใส่ข้อมูลดิบจากเวป"})));
        session.adminCommandList().add(new AdminCommandNode(new ImportRawDataCommandNode(session, new String[]{"ใส่ข้อมูลดิบ"})));
        session.adminCommandList().add(new AdminCommandNode(new ExportRawDataCommandNode(session, new String[]{"ดูข้อมูลดิบ"})));
        session.adminCommandList().add(new AdminCommandNode(new ImportQADataCommandNode(session, new String[]{"ใส่ข้อมูลถามตอบ"}, "Q:", "A:")));
        session.adminCommandList().add(new AdminCommandNode(new ExportQADataCommandNode(session, new String[]{"ดูข้อมูลถามตอบ"}, "Q:", "A:")));
        session.adminCommandList().add(new AdminCommandNode(new ExportMermaidDataCommandNode(session, new String[]{"ดูข้อมูลกราฟ"})));
        session.adminCommandList().add(new AdminCommandNode(new CreateWebIndexCommandNode(session, new String[]{"ใส่ข้อมูลสารบัญจากเวป"})));
        session.adminCommandList().add(new AdminCommandNode(new EnableTeacherCommandNode(session, new String[]{"เปิดโหมดเรียนรู้"})));
        session.adminCommandList().add(new AdminCommandNode(new DisableTeacherCommandNode(session, new String[]{"ปิดโหมดเรียนรู้"})));

        session.commandList().clear();
        session.commandList().add(new FeedbackCommandNode(session, new String[]{"\uD83D\uDC4D"}, "\uD83D\uDE0A", 0.1f));
        session.commandList().add(new FeedbackCommandNode(session, new String[]{"\uD83D\uDC4E"}, "\uD83D\uDE1F", -0.1f, Key.LEARN));
        session.commandList().add(new ForwardCommandNode(session, new String[]{"Next"}, Key.LEARN));
        session.commandList().add(new TalkCommandNode(session, Key.LEARN));

        return "...";
    }
}
