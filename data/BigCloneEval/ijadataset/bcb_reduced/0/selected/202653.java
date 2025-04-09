package org.wynnit.minows;

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.*;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import org.jruby.*;
import java.util.*;
import javax.script.Bindings;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import javax.script.SimpleScriptContext;
import javax.script.ScriptEngineFactory;
import javax.script.Invocable;
import java.sql.Timestamp;
import org.apache.commons.codec.binary.*;
import java.util.zip.CRC32;
import org.wynnit.minows.minowutils.*;
import org.jruby.util.ByteList;
import org.jruby.exceptions.RaiseException;

/**
 * This is the bean class for the ActivatedTicketBean enterprise bean.
 * Created Oct 28, 2007 3:39:40 PM
 * @author steve
 */
public class ActivatedTicketBean implements SessionBean, ActivatedTicketLocalBusiness {

    private SessionContext context;

    private TicketUtilsLocal tul;

    private RubyObject tick;

    private RubyObject classobj;

    private RubyObject action;

    private RubyHash tickarray;

    private RubyHash nodearray;

    private TicketStoreLocalHome tslh;

    private TicketStoreLocal tcl = null;

    private Integer id;

    private Long ticketid;

    private long initalcrc;

    private ScriptEngineManager sem;

    private ScriptEngine engine;

    private Invocable actioninvoke;

    private CodeStoreLocalHome cslh;

    /**
     * @see javax.ejb.SessionBean#setSessionContext(javax.ejb.SessionContext)
     */
    public void setSessionContext(SessionContext aContext) {
        context = aContext;
    }

    /**
     * @see javax.ejb.SessionBean#ejbActivate()
     */
    public void ejbActivate() {
        System.out.println("ActivatedTicketBean ejbActivate for " + ticketid.toString());
        SetupJRubyInvoker();
        LoadTicket();
    }

    /**
     * @see javax.ejb.SessionBean#ejbPassivate()
     */
    public void ejbPassivate() {
        System.out.println("ActivatedTicketBean ejbPassivate for " + ticketid.toString());
        tul.PopulateActionsFromTicketToStore(actioninvoke, tcl, tick);
        try {
            for (Iterator it = tcl.getTaskStoreBean().iterator(); it.hasNext(); ) {
                TaskStoreLocal tksl = (TaskStoreLocal) it.next();
                tksl.remove();
            }
        } catch (Exception ex) {
            System.out.println("ActivatedTicketBean ejbPassivate error " + ex.getMessage());
        }
        SaveTicket();
        tcl.setType("SJ");
        tcl.setActiveticketbeanref("");
        tick = null;
        classobj = null;
        action = null;
        sem = null;
        actioninvoke = null;
        engine = null;
        tickarray = null;
        nodearray = null;
        System.out.println("ActivatedTicketBean ejbPassivate done " + ticketid.toString());
    }

    /**
     * @see javax.ejb.SessionBean#ejbRemove()
     */
    public void ejbRemove() {
        System.out.println("ActivatedTicketBean ejbRemove for " + ticketid);
        tcl.setType("SJ");
        SaveTicket();
        tcl.setActiveticketbeanref("");
        tick = null;
        classobj = null;
        action = null;
        tickarray = null;
        nodearray = null;
        engine = null;
        sem = null;
        actioninvoke = null;
        System.out.println("ActivatedTicketBean ejbRemove done " + ticketid);
    }

    /**
     * See section 7.10.3 of the EJB 2.0 specification
     * See section 7.11.3 of the EJB 2.1 specification
     */
    public void ejbCreate() {
    }

    public void ejbCreate_FromTicket(TicketStoreLocal tsl) throws CreateException {
        System.out.println("ejbCreate_FromTicket start");
        if (tsl == null) throw new NullPointerException();
        tul = lookupTicketUtilsBean();
        actioninvoke = SetupJRubyInvoker();
        if (actioninvoke != null) {
            tslh = lookupTicketStoreBean();
            this.tcl = tsl;
            LoadTicket();
            BuildTicketArray();
            this.ticketid = tsl.getTicketid();
            tsl.setActiveticketbeanref(tul.SaveEJBHandle(context.getEJBLocalObject()));
            tsl.setType("AJ");
            System.out.println("ejbCreate_FromTicket done ");
        }
    }

    public void ejbCreate_FromString(String ticket) throws CreateException {
        if (ticket == null) throw new NullPointerException();
        System.out.println("ejbCreate_FromString start");
        try {
            tul = lookupTicketUtilsBean();
            tslh = lookupTicketStoreBean();
            actioninvoke = SetupJRubyInvoker();
            if (actioninvoke != null) {
                RubyObject action = (RubyObject) actioninvoke.invokeFunction("getAction");
                if (action != null) {
                    classobj = (RubyObject) actioninvoke.invokeMethod(action, "loadScript", ticket);
                    tick = (RubyObject) actioninvoke.invokeMethod(action, "loadTicket", ticket);
                } else {
                    System.out.println("loadTicket error: action is null");
                }
            }
        } catch (Exception ex) {
            System.out.println("ejbCreate_FromArray error: " + ex.getMessage());
            this.ejbRemove();
        }
        System.out.println("ejbCreate_FromString done");
    }

    public void ejbCreate_FromScratch(TicketStoreLocal tcl) throws CreateException {
        tslh = lookupTicketStoreBean();
        actioninvoke = SetupJRubyInvoker();
        System.out.println("ejbCreate_FromScratch start");
        try {
            id = tcl.getId();
            ticketid = tcl.getTicketid();
            this.tcl = tcl;
            tul = lookupTicketUtilsBean();
            classobj = (RubyObject) actioninvoke.invokeMethod(action, "newScript");
            if (classobj != null) {
                Collection list = cslh.findByNameActive("IdiomNode");
                if (list.size() > 0) {
                    actioninvoke.invokeMethod(classobj, "addClass", "IdiomNode");
                    for (Iterator it = list.iterator(); it.hasNext(); ) {
                        CodeStoreLocal csl = (CodeStoreLocal) it.next();
                        if (csl.getMethodname().equalsIgnoreCase("IdiomNode")) actioninvoke.invokeMethod(classobj, "addCode", "IdiomNode", null, csl.getClasscode(), 1); else actioninvoke.invokeMethod(classobj, "addCode", "IdiomNode", csl.getMethodname(), csl.getClasscode(), 1);
                    }
                }
                list = cslh.findByNameActive("IdiomCouple");
                if (list.size() > 0) {
                    actioninvoke.invokeMethod(classobj, "addClass", "IdiomCouple");
                    for (Iterator it = list.iterator(); it.hasNext(); ) {
                        CodeStoreLocal csl = (CodeStoreLocal) it.next();
                        if (csl.getMethodname().equalsIgnoreCase("IdiomCouple")) actioninvoke.invokeMethod(classobj, "addCode", "IdiomCouple", null, csl.getClasscode(), 1); else actioninvoke.invokeMethod(classobj, "addCode", "IdiomCouple", csl.getMethodname(), csl.getClasscode(), 1);
                    }
                }
                list = cslh.findByNameActive("MinowPackage");
                if (list.size() > 0) {
                    actioninvoke.invokeMethod(classobj, "addClass", "MinowPackage");
                    for (Iterator it = list.iterator(); it.hasNext(); ) {
                        CodeStoreLocal csl = (CodeStoreLocal) it.next();
                        if (csl.getMethodname().equalsIgnoreCase("MinowPackage")) actioninvoke.invokeMethod(classobj, "addCode", "MinowPackage", null, csl.getClasscode(), 1); else actioninvoke.invokeMethod(classobj, "addCode", "MinowPackage", csl.getMethodname(), csl.getClasscode(), 1);
                    }
                }
                System.out.println("loadCode start");
                actioninvoke.invokeMethod(classobj, "loadCode");
                System.out.println("loadCode end");
                tick = (RubyObject) actioninvoke.invokeMethod(action, "newTicket");
                System.out.println("tick end");
                if (tick != null) {
                    actioninvoke.invokeMethod(tick, "setnodeid", tcl.getTicketid());
                    tcl.setActiveticketbeanref(tul.SaveEJBHandle(context.getEJBLocalObject()));
                    tcl.setType("AJ");
                    try {
                        NetworkSupportLocal nsl = lookupNetworkSupportBean();
                        String pcname = nsl.NetHostname();
                        System.out.println("pcname = " + pcname);
                        MessageStoreLocalHome mslh = lookupMessageStoreBean();
                        Timestamp ts = new Timestamp(System.currentTimeMillis());
                        MessageStoreLocal msl = mslh.create(new Long(0), tcl.getTicketid(), pcname, ts, new Integer(1));
                        actioninvoke.invokeMethod(tick, "addParameter", "messagehome", pcname);
                    } catch (CreateException ce) {
                        System.out.println("ejbCreate_FromScratch cannot create message record " + ce.getMessage());
                        return;
                    }
                    System.out.println("ticket inspect:=" + tick.inspect().toString());
                } else {
                    System.out.println("unable to create ticket");
                }
            } else {
                System.out.println("IdiomNode and/or IdiomCouple scripts missing");
            }
        } catch (Exception ex) {
            System.out.println("ejbCreate_FromScratch error: " + ex.getMessage());
            ex.printStackTrace();
        }
        System.out.println("ejbCreate_FromScratch done");
    }

    private TicketUtilsLocal lookupTicketUtilsBean() {
        try {
            Context c = new InitialContext();
            TicketUtilsLocalHome rv = (TicketUtilsLocalHome) c.lookup("java:comp/env/ejb/TicketUtilsBean");
            return rv.create();
        } catch (NamingException ne) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, "exception caught", ne);
            throw new RuntimeException(ne);
        } catch (CreateException ce) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, "exception caught", ce);
            throw new RuntimeException(ce);
        }
    }

    public EJBLocalObject Handle() {
        EJBLocalObject result = null;
        try {
            result = context.getEJBLocalObject();
        } catch (Exception ex) {
            System.out.println("Handle error: " + ex.getMessage());
        }
        return result;
    }

    public RubyObject TicketObj() {
        return tick;
    }

    public RubyObject ClassesObj() {
        return classobj;
    }

    public boolean SaveTicket() {
        boolean result = true;
        System.out.println("SaveTicket for " + TicketID());
        try {
            CRC32 check = new CRC32();
            check.update(tick.inspect().toString().getBytes());
            System.out.println("SaveTicket (" + tcl.getTicketid().toString() + ") " + tcl.getType() + "," + String.valueOf(initalcrc) + " crc=" + String.valueOf(check.getValue()));
            if (initalcrc != check.getValue()) {
                System.out.println("SaveTicket yaml start");
                String b64 = (String) tul.CallTicketCode(actioninvoke, null, "generateTicketFromObjects", tick, classobj);
                System.out.println("SaveTicket yaml end");
                tcl.setTicket(b64);
            }
        } catch (Exception ex) {
            System.out.println("SaveTicket error: " + ex.getMessage());
            ex.printStackTrace();
            result = false;
        }
        System.out.println("SaveTicket done");
        return result;
    }

    private boolean LoadTicket() {
        boolean result = true;
        try {
            System.out.println("LoadTicket ticketid=" + ticketid);
            if (action != null) {
                tick = (RubyObject) actioninvoke.invokeMethod(action, "loadTicket", tcl.getTicket());
                classobj = (RubyObject) actioninvoke.invokeMethod(action, "loadScript");
                CRC32 check = new CRC32();
                check.update(tick.inspect().toString().getBytes());
                initalcrc = check.getValue();
            } else {
                System.out.println("LoadTicket error: ruby objects not loaded");
                result = false;
            }
        } catch (Exception ex) {
            System.out.println("LoadTicket error: " + ex.getMessage());
            result = false;
        }
        System.out.println("LoadTicket done");
        return result;
    }

    private CodeStoreLocalHome lookupCodeStoreBean() {
        try {
            Context c = new InitialContext();
            CodeStoreLocalHome rv = (CodeStoreLocalHome) c.lookup("java:comp/env/ejb/CodeStoreBean");
            return rv;
        } catch (NamingException ne) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, "exception caught", ne);
            throw new RuntimeException(ne);
        }
    }

    public Invocable InvokeHandle() {
        if (actioninvoke == null) System.out.println("actioninvoke is null");
        return actioninvoke;
    }

    private Invocable SetupJRubyInvoker() {
        Invocable invoke = null;
        cslh = lookupCodeStoreBean();
        String rubycode = null;
        System.out.println("Ruby library at " + System.getenv().get("JBOSS_HOME").toString() + "/server/minows/lib/ruby/1.8");
        System.setProperty("com.sun.script.jruby.loadpath", System.getenv().get("JBOSS_HOME").toString() + "/server/minows/lib/ruby/1.8");
        ScriptEngineManager sem = new ScriptEngineManager();
        if (sem == null) {
            System.out.println("Cannot Open EngineManager");
            return null;
        }
        try {
            engine = sem.getEngineByName("jruby");
            if (engine != null) {
                Collection list = cslh.findByNameActive("system");
                for (Iterator it = list.iterator(); it.hasNext(); ) {
                    CodeStoreLocal csl = (CodeStoreLocal) it.next();
                    rubycode = csl.getClasscode();
                    System.out.println("Got ruby code for " + csl.getMethodname());
                    try {
                        engine.eval(rubycode);
                    } catch (ScriptException se) {
                        System.out.println("Ruby script error for " + csl.getMethodname() + ": " + se.getMessage());
                        return null;
                    } catch (RaiseException re) {
                        System.out.println("Ruby general error for " + csl.getMethodname() + ": " + re.getMessage());
                        return null;
                    }
                }
                System.out.println("Code loaded");
                invoke = (Invocable) engine;
                System.out.println("Code loaded2");
                try {
                    action = (RubyObject) invoke.invokeFunction("getAction", new Object[] {});
                } catch (ScriptException se) {
                    System.out.println("getAction not found " + se.getMessage());
                }
                System.out.println("Code loaded3");
            } else {
                System.out.println("Cannot find jruby engine");
            }
        } catch (Exception ex) {
            System.out.println("SetupJRubyInvoker cannot create factory: " + ex.getMessage());
        }
        return invoke;
    }

    private TicketStoreLocalHome lookupTicketStoreBean() {
        try {
            Context c = new InitialContext();
            TicketStoreLocalHome rv = (TicketStoreLocalHome) c.lookup("java:comp/env/ejb/TicketStoreBean");
            return rv;
        } catch (NamingException ne) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, "exception caught", ne);
            throw new RuntimeException(ne);
        }
    }

    public HashMap CreateMirror(HashMap args) {
        HashMap result = new HashMap();
        System.out.println("TicketMirrorCreate running ");
        RubyObject mirror = null;
        Object obj = null;
        BuildTicketArray();
        try {
            String reflect_name = null;
            String mirror_name = null;
            String type = null;
            String name = null;
            String tag = null;
            obj = MinowUtils.RetrieveArg(args, "Reflect");
            if (obj != null) reflect_name = obj.toString();
            obj = MinowUtils.RetrieveArg(args, "Mirror");
            if (obj != null) mirror_name = obj.toString();
            obj = MinowUtils.RetrieveArg(args, "Type");
            if (obj != null) type = obj.toString();
            obj = MinowUtils.RetrieveArg(args, "Name");
            if (obj != null) name = obj.toString();
            obj = MinowUtils.RetrieveArg(args, "Tag");
            if (obj != null) tag = obj.toString();
            System.out.println("args reflect=" + reflect_name + " mirror=" + mirror_name + " type=" + type + " name=" + name + " tag=" + tag);
            if (reflect_name == null || mirror_name == null || type == null) {
                System.out.println("args incorrect");
                return null;
            }
            mirror = (RubyObject) findNodeByNameType(mirror_name, "Mirror", tickarray);
            if (mirror != null) {
                String mirrorid = tul.CallRubyCodeStringWrapper(actioninvoke, mirror, "nodeid");
                System.out.println("class before = " + tul.IdiomObjClass(actioninvoke, mirror));
                RubyObject mirror_dup = (RubyObject) tul.CallTicketCode(actioninvoke, mirror, "clone");
                System.out.println("class after = " + tul.IdiomObjClass(actioninvoke, mirror_dup));
                System.out.println("class=" + mirror_dup.getMetaClass().getName());
                RubyHash array = (RubyHash) tul.CallTicketCode(actioninvoke, mirror_dup, "ticketToArray", mirror_dup);
                System.out.println("TicketMirrorCreate mirrorid = " + mirrorid);
                tul.CallTicketCode(actioninvoke, mirror_dup, "setparentnodeid", new Integer(0));
                System.out.println("TicketMirrorCreate - setpaths begin " + mirror_dup.getMetaClass().getName());
                setPaths(mirror_dup);
                System.out.println("TicketMirrorCreate - setpaths done");
                RubyObject reflect = (RubyObject) findNodeByNameType(reflect_name, "Reflection", tickarray);
                if (reflect != null) {
                    tickarray = (RubyHash) tul.CallTicketCode(actioninvoke, reflect, "ticketToArray", reflect);
                    System.out.println("TicketMirrorCreate Reflection = " + reflect.inspect().toString());
                    RubyObject pack = (RubyObject) tul.CallTicketCode(actioninvoke, mirror_dup, "idiomObj");
                    if (type != null) {
                        tul.CallTicketCode(actioninvoke, pack, "setType", type);
                    }
                    if (name != null) {
                        tul.CallTicketCode(actioninvoke, pack, "setname", name);
                    }
                    if (tag != null) {
                        tul.CallTicketCode(actioninvoke, pack, "setTag", tag);
                    }
                    tul.CallTicketCode(actioninvoke, null, "addnode", reflect, mirror_dup);
                    tickarray = tul.TicketToHash(actioninvoke, tick);
                    nodearray = tul.MakeNodeArray(actioninvoke, tickarray);
                    String mirrornodeid = tul.CallRubyCodeStringWrapper(actioninvoke, mirror_dup, "nodeid");
                    result.put("ReflectID", mirrornodeid);
                    System.out.println("updating ticket mirrorid = " + mirrornodeid);
                } else {
                    System.out.println("reflection not found");
                }
            } else {
                System.out.println("mirror not found");
            }
        } catch (Exception ex) {
            System.out.println("TicketMirrorCreate problem " + ex.getMessage());
            ex.printStackTrace();
        }
        System.out.println("TicketMirrorCreate done");
        return result;
    }

    public boolean CreateShard(HashMap args) {
        System.out.println("TicketMirrorShard running");
        Object obj = null;
        try {
            String reflectid = null;
            String type = null;
            String name = null;
            obj = MinowUtils.RetrieveArg(args, "ReflectID");
            if (obj != null) reflectid = obj.toString();
            obj = MinowUtils.RetrieveArg(args, "Type");
            if (obj != null) type = obj.toString();
            obj = MinowUtils.RetrieveArg(args, "Name");
            if (obj != null) name = obj.toString();
            System.out.println("TicketMirrorShard args=" + type + "," + name + "," + reflectid);
            RubyObject reflecttop = (RubyObject) nodearray.get(new Long(reflectid));
            if (reflecttop != null) {
                RubyObject reflect = (RubyObject) findNodeByNameType(name, type, tickarray);
                if (reflect != null) {
                    Iterator it = args.keySet().iterator();
                    while (it.hasNext()) {
                        String key = ((ByteList) it.next()).toString();
                        if (args.get(key) != null) {
                            System.out.println("CreateShard key=" + key + " value=" + args.get(key).toString());
                            if (!key.equalsIgnoreCase("ReflectID") && !key.equalsIgnoreCase("Name") && !key.equalsIgnoreCase("Type")) {
                                RubyObject pack = (RubyObject) tul.CallTicketCode(actioninvoke, reflect, "idiomObj");
                                System.out.println("TicketMirrorShard looking for " + key);
                                tul.CallTicketCode(actioninvoke, pack, "instance_variable_get", "@" + key.toLowerCase());
                                tul.CallTicketCode(actioninvoke, pack, "instance_eval", "@" + key.toLowerCase() + "= \"" + args.get(key).toString() + "\"");
                            }
                        } else {
                            System.out.println("CreateShard - key " + key + " value is null");
                        }
                    }
                }
                System.out.println("CreateShard - updating ticket");
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
        System.out.println("TicketMirrorShard done");
        return true;
    }

    public boolean setPaths(RubyObject root) {
        System.out.println("setPaths start ");
        RubyHash patharray = (RubyHash) tul.CallTicketCode(actioninvoke, root, "ticketToArray", root);
        RubyHash nodearray = (RubyHash) tul.MakeNodeArray(actioninvoke, patharray);
        int maxlevel = 0;
        int level = 0;
        int clevel = 0;
        Iterator it = patharray.keys().iterator();
        while (it.hasNext()) {
            String key = (String) it.next();
            level = new Integer(key.split("_")[0]).intValue();
            if (level > maxlevel) maxlevel = level;
        }
        level = maxlevel;
        while (level > 0) {
            it = patharray.keys().iterator();
            while (it.hasNext()) {
                String key = (String) it.next();
                clevel = new Integer(key.split("_")[0]).intValue();
                if (clevel == level) {
                    String newid = String.valueOf(System.nanoTime());
                    RubyObject node = (RubyObject) patharray.get(key);
                    if (node != null) {
                        String parentnodeid = tul.CallRubyCodeStringWrapper(actioninvoke, node, "parentnodeid");
                        String nodeid = tul.CallRubyCodeStringWrapper(actioninvoke, node, "nodeid");
                        if (Long.valueOf(parentnodeid).intValue() != 0) {
                            RubyObject pnode = (RubyObject) nodearray.get(new Long(parentnodeid));
                            if (pnode != null) {
                                RubyHash nodes = (RubyHash) tul.CallTicketCode(actioninvoke, pnode, "nodes");
                                if (nodes != null) {
                                    tul.CallTicketCode(actioninvoke, null, "hashReplace", nodes, nodeid, newid);
                                }
                            }
                        }
                        tul.CallTicketCode(actioninvoke, node, "setnodeid", newid);
                        RubyHash nodes = (RubyHash) tul.CallTicketCode(actioninvoke, node, "nodes");
                        if (nodes != null) {
                            Iterator it2 = nodes.keys().iterator();
                            while (it2.hasNext()) {
                                String id = (String) it2.next();
                                RubyObject ticknodes = (RubyObject) nodes.get(id);
                                if (ticknodes != null) tul.CallTicketCode(actioninvoke, ticknodes, "setparentnodeid", newid);
                            }
                        } else {
                            return false;
                        }
                    } else {
                        return false;
                    }
                }
            }
            level = level - 1;
        }
        System.out.println("setPaths done");
        return true;
    }

    public RubyObject findNodeByNameType(String pathkey, String type, RubyHash array) {
        System.out.println("findNodeByNameType start " + pathkey + "," + type);
        Iterator it = array.keys().iterator();
        while (it.hasNext()) {
            String key = (String) it.next();
            if (key.indexOf(pathkey) != -1) {
                System.out.println("found==>" + key);
                RubyObject ticknode = (RubyObject) array.get(key);
                RubyObject pack = (RubyObject) tul.CallTicketCode(actioninvoke, ticknode, "idiomObj");
                String name = pathkey.substring(pathkey.lastIndexOf("|") + 1);
                String packname = tul.CallRubyCodeStringWrapper(actioninvoke, pack, "name");
                String packtype = tul.CallRubyCodeStringWrapper(actioninvoke, pack, "getType");
                System.out.println("findNodeByNameType packname=" + packname + " type=" + packtype + " name=" + name + " class=" + pack.getMetaClass().getName());
                if (name.equalsIgnoreCase(packname) && (type.equalsIgnoreCase(packtype) || type.equalsIgnoreCase("*"))) {
                    System.out.println("findNodeByNameType done key=" + key);
                    return ticknode;
                }
            }
        }
        System.out.println("findNodeByNameType done without finding node ");
        return null;
    }

    public RubyObject findNode(String id, RubyHash array) {
        Iterator it = array.keys().iterator();
        while (it.hasNext()) {
            String key = (String) it.next();
            RubyObject ticknode = (RubyObject) array.get(key);
            String nodeid = tul.CallRubyCodeStringWrapper(actioninvoke, ticknode, "nodeid");
            if (nodeid.equalsIgnoreCase(id)) {
                return ticknode;
            }
        }
        return null;
    }

    public Long TicketID() {
        return ticketid;
    }

    private MessageStoreLocalHome lookupMessageStoreBean() {
        try {
            Context c = new InitialContext();
            MessageStoreLocalHome rv = (MessageStoreLocalHome) c.lookup("java:comp/env/ejb/MessageStoreBean");
            return rv;
        } catch (NamingException ne) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, "exception caught", ne);
            throw new RuntimeException(ne);
        }
    }

    private NetworkSupportLocal lookupNetworkSupportBean() {
        try {
            Context c = new InitialContext();
            NetworkSupportLocalHome rv = (NetworkSupportLocalHome) c.lookup("java:comp/env/ejb/NetworkSupportBean");
            return rv.create();
        } catch (NamingException ne) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, "exception caught", ne);
            throw new RuntimeException(ne);
        } catch (CreateException ce) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, "exception caught", ce);
            throw new RuntimeException(ce);
        }
    }

    private TicketSupportLocal lookupTicketSupportBean() {
        try {
            Context c = new InitialContext();
            TicketSupportLocalHome rv = (TicketSupportLocalHome) c.lookup("java:comp/env/TicketSupportBean");
            return rv.create();
        } catch (NamingException ne) {
            java.util.logging.Logger.getLogger(getClass().getName()).log(java.util.logging.Level.SEVERE, "exception caught", ne);
            throw new RuntimeException(ne);
        } catch (CreateException ce) {
            java.util.logging.Logger.getLogger(getClass().getName()).log(java.util.logging.Level.SEVERE, "exception caught", ce);
            throw new RuntimeException(ce);
        }
    }

    public void BuildTicketArray() {
        if (tickarray == null) {
            System.out.println("BuildTicketArray building tickarray");
            tickarray = (RubyHash) tul.CallTicketCode(actioninvoke, tick, "ticketToArray", tick);
            nodearray = (RubyHash) tul.MakeNodeArray(actioninvoke, tickarray);
        }
    }

    public RubyHash GetTicketArray() {
        return tickarray;
    }

    public void ejbCreate_FromTemplate(TicketStoreLocal tptcl, String[] excludes) throws CreateException {
        System.out.println("ejbCreate_FromTemplate start");
        if (tptcl == null) throw new NullPointerException();
        try {
            tslh = lookupTicketStoreBean();
            tul = lookupTicketUtilsBean();
            actioninvoke = SetupJRubyInvoker();
            if (actioninvoke == null) {
                tick = null;
                return;
            }
            Long tpid = tptcl.getTicketid();
            this.tcl = tptcl;
            System.out.println("ejbCreate_FromTemplate loading " + tpid.toString());
            LoadTicket();
            if (tick == null || classobj == null) return;
            setPaths(tick);
            tickarray = null;
            BuildTicketArray();
            this.ticketid = new Long((String) tul.CallTicketCode(actioninvoke, tick, "nodeid"));
            if (excludes != null) for (int i = 0; i < excludes.length; i++) {
                tul.CallTicketCode(actioninvoke, tick, "delActionTable", excludes[i]);
            }
            try {
                NetworkSupportLocal nsl = lookupNetworkSupportBean();
                String pcname = nsl.NetHostname();
                System.out.println("pcname = " + pcname);
                MessageStoreLocalHome mslh = lookupMessageStoreBean();
                Timestamp ts = new Timestamp(System.currentTimeMillis());
                MessageStoreLocal msl = mslh.create(new Long(0), ticketid, pcname, ts, new Integer(1));
                tul.CallTicketCode(actioninvoke, tick, "addParameter", "messagehome", pcname);
            } catch (CreateException ce) {
                System.out.println("ejbCreate_FromTemplate cannot create message record " + ce.getMessage());
                return;
            }
            String b64 = (String) tul.CallTicketCode(actioninvoke, null, "generateTicketFromObjects", tick, classobj);
            this.tcl = tslh.create(ticketid, "test", "created from " + tpid.toString(), "AJ", b64, new Timestamp(System.currentTimeMillis()), new Long(0), tul.SaveEJBHandle(context.getEJBLocalObject()), new Integer(1), new Integer(0));
            System.out.println("ejbCreate_FromTemplate newid=" + ticketid.toString() + " ticketid=" + ticketid.toString());
        } catch (CreateException ce) {
            System.out.println("ejbCreate_FromTemplate cannot package ticket " + ce.getMessage());
            ce.printStackTrace();
            return;
        }
        System.out.println("ejbCreate_FromTemplate done ");
    }
}
