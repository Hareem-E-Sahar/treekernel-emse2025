package edu.clemson.cs.nestbed.client.cli;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import edu.clemson.cs.nestbed.common.management.configuration.ProgramProfilingMessageSymbolManager;
import edu.clemson.cs.nestbed.common.management.configuration.ProgramMessageSymbolManager;
import edu.clemson.cs.nestbed.common.management.profiling.MessageManager;
import edu.clemson.cs.nestbed.common.model.Mote;
import edu.clemson.cs.nestbed.common.model.MoteDeploymentConfiguration;
import edu.clemson.cs.nestbed.common.model.MoteTestbedAssignment;
import edu.clemson.cs.nestbed.common.model.MoteType;
import edu.clemson.cs.nestbed.common.model.Program;
import edu.clemson.cs.nestbed.common.model.ProgramMessageSymbol;
import edu.clemson.cs.nestbed.common.model.ProgramProfilingSymbol;
import edu.clemson.cs.nestbed.common.model.ProgramProfilingMessageSymbol;
import edu.clemson.cs.nestbed.common.model.ProgramSymbol;
import edu.clemson.cs.nestbed.common.model.Project;
import edu.clemson.cs.nestbed.common.model.ProjectDeploymentConfiguration;
import edu.clemson.cs.nestbed.common.model.Testbed;
import edu.clemson.cs.nestbed.common.util.LocalObjectInputStream;
import edu.clemson.cs.nestbed.common.util.RemoteObserver;

class MoteMessageProfilingLevel extends Level {

    private ProjectDeploymentConfiguration config;

    private Program program;

    private Mote mote;

    private MessageManager messageManager;

    private ProgramProfilingMessageSymbolManager progProfMsgSymManager;

    private ProgramMessageSymbolManager symbolManager;

    public MoteMessageProfilingLevel(ProjectDeploymentConfiguration config, Program program, Mote mote, Level parent) throws Exception {
        super("ProfilingMessages", parent);
        this.config = config;
        this.program = program;
        this.mote = mote;
        lookupRemoteManagers();
        if (program == null) {
            return;
        }
        for (ProgramProfilingMessageSymbol i : progProfMsgSymManager.getProgramProfilingMessageSymbols(config.getID(), program.getID())) {
            ProgramMessageSymbol messageSymbol;
            messageSymbol = symbolManager.getProgramMessageSymbol(i.getProgramMessageSymbolID());
            addEntry(new ProgramProfilingMessageSymbolEntry(i, messageSymbol));
        }
        addCommand("startProfiling", new ProfileStartCommand());
        addCommand("stopProfiling", new ProfileStopCommand());
        addCommand("clearProfiling", new ProfileClearCommand());
    }

    private final void lookupRemoteManagers() throws RemoteException, NotBoundException, MalformedURLException {
        progProfMsgSymManager = (ProgramProfilingMessageSymbolManager) Naming.lookup(RMI_BASE_URL + "ProgramProfilingMessageSymbolManager");
        symbolManager = (ProgramMessageSymbolManager) Naming.lookup(RMI_BASE_URL + "ProgramMessageSymbolManager");
        messageManager = (MessageManager) Naming.lookup(RMI_BASE_URL + "MessageManager");
    }

    private class ProgramProfilingMessageSymbolEntry extends FileEntry {

        private ProgramProfilingMessageSymbol ppms;

        private ProgramMessageSymbol pms;

        private StringBuffer contents;

        private MessageObserver messageObserver;

        public ProgramProfilingMessageSymbolEntry(ProgramProfilingMessageSymbol ppms, ProgramMessageSymbol pms) throws Exception {
            super(pms.getName());
            this.ppms = ppms;
            this.pms = pms;
            this.contents = new StringBuffer();
            this.messageObserver = new MessageObserver();
        }

        public void startProfiling() throws Exception {
            messageManager.enable(mote.getID());
            messageManager.addMessageObserver(mote.getID(), pms.getID(), messageObserver);
        }

        public void stopProfiling() throws Exception {
            messageManager.removeMessageObserver(mote.getID(), pms.getID(), messageObserver);
            messageManager.disable(mote.getID());
        }

        public void clearProfiling() {
            contents.setLength(0);
        }

        public String getFileContents() throws Exception {
            return contents.toString();
        }

        private class MessageObserver extends UnicastRemoteObject implements RemoteObserver {

            public MessageObserver() throws RemoteException {
                super();
            }

            public void update(Serializable pmsID, Serializable msg) throws RemoteException {
                ClassLoader classLoader = pms.getClassLoader();
                try {
                    byte[] msgBytes;
                    ByteArrayInputStream bain;
                    Class c;
                    Constructor con;
                    ObjectInputStream in;
                    Object obj;
                    msgBytes = (byte[]) msg;
                    bain = new ByteArrayInputStream(msgBytes);
                    c = classLoader.loadClass(LocalObjectInputStream.class.getName());
                    con = c.getConstructor(InputStream.class, Class.class);
                    in = (ObjectInputStream) con.newInstance(bain, pms.getMessageClass());
                    obj = in.readObject();
                    in.close();
                    contents.append(new Date()).append("\n");
                    for (Method i : getPropertyMethodList(obj.getClass())) {
                        contents.append(i.getName().substring(4)).append("\t");
                        contents.append(i.invoke(obj).toString()).append("\n");
                    }
                    contents.append("------------------------------------------\n");
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }

            private List<Method> getPropertyMethodList(Class msgClass) {
                List<Method> methodList = new ArrayList<Method>();
                Method[] methods = msgClass.getMethods();
                for (Method i : methods) {
                    if (i.getName().startsWith("get_")) {
                        methodList.add(i);
                    }
                }
                return methodList;
            }
        }
    }

    private class ProfileStartCommand implements Command {

        public Level execute(String[] args) throws Exception {
            Level nextLevel = MoteMessageProfilingLevel.this;
            Variables.set("status", "0");
            if (args.length != 2) {
                System.out.printf("usage: %s <messageSymbol>\n", args[0]);
                Variables.set("status", "1");
                return nextLevel;
            }
            String name = args[1];
            Entry entry = getEntryWithName(name);
            if (entry instanceof ProgramProfilingMessageSymbolEntry) {
                ProgramProfilingMessageSymbolEntry ppmse;
                ppmse = (ProgramProfilingMessageSymbolEntry) entry;
                ppmse.startProfiling();
            } else {
                System.out.printf("No entry found for %s\n", name);
                Variables.set("status", "2");
            }
            return nextLevel;
        }

        public String getHelpText() {
            return "Starts profiling of selected message";
        }
    }

    private class ProfileStopCommand implements Command {

        public Level execute(String[] args) throws Exception {
            Level nextLevel = MoteMessageProfilingLevel.this;
            Variables.set("status", "0");
            if (args.length != 2) {
                System.out.printf("usage: %s <messageSymbol>\n", args[0]);
                Variables.set("status", "1");
                return nextLevel;
            }
            String name = args[1];
            Entry entry = getEntryWithName(name);
            if (entry instanceof ProgramProfilingMessageSymbolEntry) {
                ProgramProfilingMessageSymbolEntry ppmse;
                ppmse = (ProgramProfilingMessageSymbolEntry) entry;
                ppmse.stopProfiling();
            } else {
                System.out.printf("No entry found for %s\n", name);
                Variables.set("status", "2");
            }
            return nextLevel;
        }

        public String getHelpText() {
            return "Stops profiling of selected message";
        }
    }

    private class ProfileClearCommand implements Command {

        public Level execute(String[] args) throws Exception {
            Level nextLevel = MoteMessageProfilingLevel.this;
            Variables.set("status", "0");
            if (args.length != 2) {
                System.out.printf("usage: %s <messageSymbol>\n", args[0]);
                Variables.set("status", "1");
                return nextLevel;
            }
            String name = args[1];
            Entry entry = getEntryWithName(name);
            if (entry instanceof ProgramProfilingMessageSymbolEntry) {
                ProgramProfilingMessageSymbolEntry ppmse;
                ppmse = (ProgramProfilingMessageSymbolEntry) entry;
                ppmse.clearProfiling();
            } else {
                System.out.printf("No entry found for %s\n", name);
                Variables.set("status", "2");
            }
            return nextLevel;
        }

        public String getHelpText() {
            return "Clears profiling data associated with selected message";
        }
    }
}
