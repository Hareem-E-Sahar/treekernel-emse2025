package com.winterwar.web;

import java.io.FileWriter;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.Random;
import java.util.Set;
import javax.mail.Message;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import net.spy.memcached.MemcachedClient;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;
import org.apache.struts.action.ActionMessages;
import org.apache.struts.action.DynaActionForm;
import org.apache.struts.actions.DispatchAction;
import com.winterwar.base.Convention;
import com.winterwar.base.Event;
import com.winterwar.base.Slot;
import com.winterwar.base.Table;
import com.winterwar.base.User;
import com.winterwar.base.WWAuthenticator;
import com.winterwar.service.ConventionManager;
import com.winterwar.service.EventManager;
import com.winterwar.service.SlotManager;
import com.winterwar.service.TableManager;
import com.winterwar.service.UserManager;

public class EventAction extends DispatchAction {

    private static Log log = LogFactory.getLog(EventAction.class);

    private EventManager mgr;

    private UserManager userMgr;

    private SlotManager slotMgr;

    private TableManager tableMgr;

    private ConventionManager conMgr;

    public void setConventionManager(ConventionManager mgr) {
        this.conMgr = mgr;
    }

    public void setTableManager(TableManager mgr) {
        this.tableMgr = mgr;
    }

    public void setManager(EventManager mgr) {
        this.mgr = mgr;
    }

    public void setSlotManager(SlotManager mgr) {
        this.slotMgr = mgr;
    }

    public void setUserManager(UserManager mgr) {
        this.userMgr = mgr;
    }

    public ActionForward delete(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
        if (log.isDebugEnabled()) {
            log.debug("Starting delete...");
        }
        mgr.remove(request.getParameter("event.eventID"));
        ActionMessages messages = new ActionMessages();
        messages.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("event.deleted"));
        saveMessages(request, messages);
        return list(mapping, form, request, response);
    }

    @SuppressWarnings("unchecked")
    public ActionForward save(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
        HttpSession session = request.getSession();
        if (log.isDebugEnabled()) {
            log.debug("Starting save...");
        }
        ActionMessages errors = form.validate(mapping, request);
        if (!errors.isEmpty()) {
            saveErrors(request, errors);
            return edit(mapping, form, request, response);
        }
        DynaActionForm eventForm = (DynaActionForm) form;
        Event event = (Event) eventForm.get("event");
        if (event.getTableID() != null) {
            Event inSameSlot = mgr.getByTableSlot(event.getSlotID(), event.getTableID());
            if (inSameSlot != null && inSameSlot.getEventID().intValue() != event.getEventID().intValue()) {
                ActionMessages tableError = new ActionMessages();
                tableError.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("event.tableConflict"));
                saveErrors(request, tableError);
                return edit(mapping, form, request, response);
            }
        }
        if (session.getAttribute("randFormID").toString().equals(request.getParameter("formID").toString())) {
            Random gen = new Random(new Date().getTime());
            session.setAttribute("randFormID", gen.nextInt() + "");
            User user = null;
            if (eventForm.get("user") != null) {
                user = (User) eventForm.get("user");
            }
            if (user != null && user.getFirstName() != null && (event.getUserID() == null || event.getUserID().intValue() == 0)) {
                user.setUsername(user.getFirstName().toLowerCase().substring(0, 1) + user.getLastName().toLowerCase());
                user.setPassword("ww2009");
                user = userMgr.save(user);
                event.setUserID(user.getUserID());
            }
            log.info("RGT TEst: Selecting Table: " + event.getTableID());
            if ((event.getTableID() == null || event.getTableID().intValue() == 0) && event.getStatus().intValue() == 3) {
                log.info("RGT TEst: Selecting Table.");
                event.setTableID(findTable(event));
            }
            if (event.getInsertDate() == null) {
                event.setInsertDate(new Date());
            }
            mgr.save(event);
            if (event.getStatus().intValue() == 3) {
                event = mgr.get(event.getEventID() + "");
                sendEmail(event);
            }
            request.setAttribute("conID", event.getConID().intValue() + "");
            ActionMessages messages = new ActionMessages();
            messages.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("event.saved"));
            saveMessages(request, messages);
            request.setAttribute("conID", request.getAttribute("conID"));
        }
        return status(mapping, form, request, response);
    }

    @SuppressWarnings("unchecked")
    public ActionForward edit(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
        if (log.isDebugEnabled()) {
            log.debug("Starting edit...");
        }
        DynaActionForm eventForm = (DynaActionForm) form;
        String eventID = request.getParameter("eventID");
        MemcachedClient cacheClient = new MemcachedClient(new InetSocketAddress("localhost", 11211));
        String conID = request.getParameter("conID");
        if (conID == null || conID.equals("")) {
            conID = (cacheClient.get("Curr_Con_ID") != null) ? cacheClient.get("Curr_Con_ID").toString() : null;
            ;
            if (conID == null || conID.equals("")) {
                conID = conMgr.getCurrentCon().getConID().toString();
                cacheClient.set("Curr_Con_ID", 86400, conID);
            }
        }
        List slots = slotMgr.getAllByCon(conID);
        request.setAttribute("slots", slots);
        request.setAttribute("conID", conID);
        request.setAttribute("users", userMgr.getAll());
        request.setAttribute("tables", tableMgr.getAllByCon(conID));
        if (eventID != null) {
            Event event = mgr.get(eventID);
            if (event == null) {
                ActionMessages errors = new ActionMessages();
                errors.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("event.missing"));
                saveErrors(request, errors);
                return mapping.findForward("list");
            }
            if (event.getUserID() != null && event.getUserID().intValue() != 0) {
                User user = userMgr.get(event.getUserID().toString());
                request.setAttribute("user", user);
                request.setAttribute("userID", user.getUserID());
            }
            eventForm.set("event", event);
            request.setAttribute("event", event);
        } else {
            request.setAttribute("userID", "0");
        }
        return mapping.findForward("edit");
    }

    public ActionForward list(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
        if (log.isDebugEnabled()) {
            log.debug("Starting list...");
        }
        request.setAttribute("events", mgr.getAll());
        return mapping.findForward("list");
    }

    public ActionForward listByConID(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
        if (log.isDebugEnabled()) {
            log.debug("Starting list...");
        }
        MemcachedClient cacheClient = new MemcachedClient(new InetSocketAddress("localhost", 11211));
        String conID = request.getParameter("conID");
        String status = request.getParameter("status");
        if (conID == null || conID.equals("")) {
            conID = (cacheClient.get("Curr_Con_ID") != null) ? cacheClient.get("Curr_Con_ID").toString() : null;
            ;
            if (conID == null || conID.equals("")) {
                conID = conMgr.getCurrentCon().getConID().toString();
                cacheClient.set("Curr_Con_ID", 86400, conID);
            }
        }
        request.setAttribute("events", mgr.getByConID(conID, status));
        request.setAttribute("conID", conID);
        request.setAttribute("status", status);
        return mapping.findForward("list");
    }

    @SuppressWarnings("unchecked")
    private Integer findTable(Event event) {
        List tables = tableMgr.getAllByCon(event.getConID().toString());
        List slimTables = new LinkedList();
        Integer tableID = null;
        for (int i = 0; i < tables.size(); i++) {
            Table table = (Table) tables.get(i);
            Event e = mgr.getByTableSlot(event.getSlotID(), table.getTableID());
            if (e == null) {
                log.info("Adding " + table.getTableName() + " as possible table.");
                slimTables.add(table);
            }
        }
        switch(event.getEventType().intValue()) {
            case 1:
                for (int i = 0; i < slimTables.size(); i++) {
                    Table table = (Table) tables.get(i);
                    if (table.getTableName().substring(0, 1).equals("A") && table.getPhysicalTables().intValue() >= event.getTablesRequired().intValue()) {
                        return table.getTableID();
                    }
                    if (table.getTableName().substring(0, 1).equals("M") && table.getPhysicalTables().intValue() >= event.getTablesRequired().intValue()) {
                        return table.getTableID();
                    }
                }
                break;
            case 2:
                for (int i = 0; i < slimTables.size(); i++) {
                    Table table = (Table) tables.get(i);
                    if (table.getTableName().substring(0, 1).equals("A") && table.getPhysicalTables().intValue() >= event.getTablesRequired().intValue()) {
                        return table.getTableID();
                    }
                    if (table.getTableName().substring(0, 1).equals("M") && table.getPhysicalTables().intValue() >= event.getTablesRequired().intValue()) {
                        return table.getTableID();
                    }
                }
                break;
            case 3:
                for (int i = 0; i < slimTables.size(); i++) {
                    Table table = (Table) tables.get(i);
                    if (table.getTableName().substring(0, 1).equals("M") && table.getPhysicalTables().intValue() >= event.getTablesRequired().intValue()) {
                        return table.getTableID();
                    }
                }
                break;
            default:
                tableID = null;
                break;
        }
        return tableID;
    }

    public ActionForward status(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
        MemcachedClient cacheClient = new MemcachedClient(new InetSocketAddress("localhost", 11211));
        String conID = (cacheClient.get("Curr_Con_ID") != null) ? cacheClient.get("Curr_Con_ID").toString() : null;
        ;
        if (conID == null || conID.equals("")) {
            conID = conMgr.getCurrentCon().getConID().toString();
            cacheClient.set("Curr_Con_ID", 86400, conID);
        }
        request.setAttribute("conID", conID);
        request.setAttribute("events", mgr.getByConID(conID, ""));
        return mapping.findForward("status");
    }

    @SuppressWarnings("unchecked")
    public ActionForward exportToCSV(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
        if (log.isDebugEnabled()) {
            log.debug("Starting list...");
        }
        String path = "/home/www-root/ww-static";
        PrintWriter out = new PrintWriter(new FileWriter(path + "/eventList.csv"));
        MemcachedClient cacheClient = new MemcachedClient(new InetSocketAddress("localhost", 11211));
        String conID = request.getParameter("conID");
        if (conID == null || conID.equals("")) {
            conID = (cacheClient.get("Curr_Con_ID") != null) ? cacheClient.get("Curr_Con_ID").toString() : null;
            ;
            if (conID == null || conID.equals("")) {
                conID = conMgr.getCurrentCon().getConID().toString();
                cacheClient.set("Curr_Con_ID", 86400, conID);
            }
        }
        List<Event> events = mgr.getByConID(conID, "3");
        out.println("judge,title,rules,company,genre,description,space,players,rounds,experience,preferred,alternate,area,assigned");
        for (Event e : events) {
            Slot pref = slotMgr.get(e.getSlotID().toString());
            Slot alt = slotMgr.get(e.getAltSlotID().toString());
            out.println(e.getUser().getFirstName() + " " + e.getUser().getLastName() + ((e.getAdditionalJudges() != null && e.getAdditionalJudges().length() > 0) ? " " + e.getAdditionalJudges() + "," : ",") + e.getEventName() + "," + e.getRules() + ",," + getEventType(e.getEventType().intValue()) + ",\"" + e.getEventDesc().replaceAll("\"", "\\\"") + "\"" + "," + e.getTablesRequired() + "," + e.getTotalSeats() + "," + e.getSlots() + "," + e.getPlayerExperience() + "," + pref.getSlotName().charAt(0) + "," + ((alt != null) ? alt.getSlotName().charAt(0) : "") + "," + "," + e.getTableName() + "," + pref.getSlotName().charAt(0));
        }
        out.close();
        return status(mapping, form, request, response);
    }

    private String getEventType(int type) {
        switch(type) {
            case 1:
                return "BG";
            case 2:
                return "CCG";
            case 3:
                return "MIN";
            case 4:
                return "RPG";
            case 5:
                return "ASL";
            case 6:
                return "RPGA";
            default:
                return "";
        }
    }

    public void sendEmail(Event event) throws Exception {
        if (log.isDebugEnabled()) {
            log.info("Starting send mail...");
        }
        StringBuilder eMailBody = new StringBuilder();
        User user = userMgr.get(event.getUserID().toString());
        Slot slot = slotMgr.get(event.getSlotID().toString());
        try {
            eMailBody.append("Hello, " + user.getFirstName() + " from the Winter War Convention!\n\r\n\r");
            eMailBody.append("This e-mail is being sent to inform you that the event \"" + event.getEventName() + "\" has been updated and/or approved for the " + slot.getSlotName() + " time slot!\n\r\n\r");
            eMailBody.append("If you have any questions, comments, or need to make changes to your event, please contact the convention chairman, Don McKinney! \n\r");
            eMailBody.append("Thank you! \n\r");
            Properties props = new Properties();
            props.put("mail.smtp.host", "winterwar.org");
            props.put("mail.smtp.auth", "true");
            props.put("mail.smtp.user", "registrar@mail.winterwar.org");
            props.put("mail.smtp.pass", "Battle0n");
            Session mailSession = Session.getDefaultInstance(props, new WWAuthenticator());
            Message msg = new MimeMessage(mailSession);
            InternetAddress addressFrom = new InternetAddress("registrar@winterwar.org");
            msg.setFrom(addressFrom);
            InternetAddress[] addressTo = new InternetAddress[1];
            addressTo[0] = new InternetAddress(user.getEmail());
            msg.setRecipients(Message.RecipientType.TO, addressTo);
            msg.addHeader("MyHeaderName", "myHeaderValue");
            msg.setSubject("Winter War Convention Account Information");
            msg.setContent(eMailBody.toString(), "text/plain");
            Transport.send(msg);
        } catch (Exception ex) {
            log.error(ex);
            for (StackTraceElement element : ex.getStackTrace()) log.error(element.toString());
        }
    }
}
