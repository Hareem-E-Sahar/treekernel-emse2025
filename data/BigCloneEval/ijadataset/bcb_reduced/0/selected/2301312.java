package com.sun.tools.example.debug.tty;

import com.sun.jdi.*;
import com.sun.jdi.connect.*;
import com.sun.jdi.request.EventRequestManager;
import com.sun.jdi.request.ExceptionRequest;
import com.sun.jdi.request.ThreadStartRequest;
import com.sun.jdi.request.ThreadDeathRequest;
import java.util.*;
import java.util.regex.*;
import java.io.*;

class VMConnection {

    private VirtualMachine vm;

    private Process process = null;

    private int outputCompleteCount = 0;

    private final Connector connector;

    private final Map<String, com.sun.jdi.connect.Connector.Argument> connectorArgs;

    private final int traceFlags;

    synchronized void notifyOutputComplete() {
        outputCompleteCount++;
        notifyAll();
    }

    synchronized void waitOutputComplete() {
        if (process != null) {
            while (outputCompleteCount < 2) {
                try {
                    wait();
                } catch (InterruptedException e) {
                }
            }
        }
    }

    private Connector findConnector(String name) {
        for (Connector connector : Bootstrap.virtualMachineManager().allConnectors()) {
            if (connector.name().equals(name)) {
                return connector;
            }
        }
        return null;
    }

    private Map<String, com.sun.jdi.connect.Connector.Argument> parseConnectorArgs(Connector connector, String argString) {
        Map<String, com.sun.jdi.connect.Connector.Argument> arguments = connector.defaultArguments();
        String regexPattern = "(quote=[^,]+,)|" + "(\\w+=)" + "(((\"[^\"]*\")|" + "('[^']*')|" + "([^,'\"]+))+,)";
        Pattern p = Pattern.compile(regexPattern);
        Matcher m = p.matcher(argString);
        while (m.find()) {
            int startPosition = m.start();
            int endPosition = m.end();
            if (startPosition > 0) {
                throw new IllegalArgumentException(MessageOutput.format("Illegal connector argument", argString));
            }
            String token = argString.substring(startPosition, endPosition);
            int index = token.indexOf('=');
            String name = token.substring(0, index);
            String value = token.substring(index + 1, token.length() - 1);
            Connector.Argument argument = arguments.get(name);
            if (argument == null) {
                throw new IllegalArgumentException(MessageOutput.format("Argument is not defined for connector:", new Object[] { name, connector.name() }));
            }
            argument.setValue(value);
            argString = argString.substring(endPosition);
            m = p.matcher(argString);
        }
        if ((!argString.equals(",")) && (argString.length() > 0)) {
            throw new IllegalArgumentException(MessageOutput.format("Illegal connector argument", argString));
        }
        return arguments;
    }

    VMConnection(String connectSpec, int traceFlags) {
        String nameString;
        String argString;
        int index = connectSpec.indexOf(':');
        if (index == -1) {
            nameString = connectSpec;
            argString = "";
        } else {
            nameString = connectSpec.substring(0, index);
            argString = connectSpec.substring(index + 1);
        }
        connector = findConnector(nameString);
        if (connector == null) {
            throw new IllegalArgumentException(MessageOutput.format("No connector named:", nameString));
        }
        connectorArgs = parseConnectorArgs(connector, argString);
        this.traceFlags = traceFlags;
    }

    synchronized VirtualMachine open() {
        if (connector instanceof LaunchingConnector) {
            vm = launchTarget();
        } else if (connector instanceof AttachingConnector) {
            vm = attachTarget();
        } else if (connector instanceof ListeningConnector) {
            vm = listenTarget();
        } else {
            throw new InternalError(MessageOutput.format("Invalid connect type"));
        }
        vm.setDebugTraceMode(traceFlags);
        if (vm.canBeModified()) {
            setEventRequests(vm);
            resolveEventRequests();
        }
        if (Env.getSourcePath().length() == 0) {
            if (vm instanceof PathSearchingVirtualMachine) {
                PathSearchingVirtualMachine psvm = (PathSearchingVirtualMachine) vm;
                Env.setSourcePath(psvm.classPath());
            } else {
                Env.setSourcePath(".");
            }
        }
        return vm;
    }

    boolean setConnectorArg(String name, String value) {
        if (vm != null) {
            return false;
        }
        Connector.Argument argument = connectorArgs.get(name);
        if (argument == null) {
            return false;
        }
        argument.setValue(value);
        return true;
    }

    String connectorArg(String name) {
        Connector.Argument argument = connectorArgs.get(name);
        if (argument == null) {
            return "";
        }
        return argument.value();
    }

    public synchronized VirtualMachine vm() {
        if (vm == null) {
            throw new VMNotConnectedException();
        } else {
            return vm;
        }
    }

    boolean isOpen() {
        return (vm != null);
    }

    boolean isLaunch() {
        return (connector instanceof LaunchingConnector);
    }

    public void disposeVM() {
        try {
            if (vm != null) {
                vm.dispose();
                vm = null;
            }
        } finally {
            if (process != null) {
                process.destroy();
                process = null;
            }
            waitOutputComplete();
        }
    }

    private void setEventRequests(VirtualMachine vm) {
        EventRequestManager erm = vm.eventRequestManager();
        Commands evaluator = new Commands();
        evaluator.commandCatchException(new StringTokenizer("uncaught java.lang.Throwable"));
        ThreadStartRequest tsr = erm.createThreadStartRequest();
        tsr.enable();
        ThreadDeathRequest tdr = erm.createThreadDeathRequest();
        tdr.enable();
    }

    private void resolveEventRequests() {
        Env.specList.resolveAll();
    }

    private void dumpStream(InputStream stream) throws IOException {
        BufferedReader in = new BufferedReader(new InputStreamReader(stream));
        int i;
        try {
            while ((i = in.read()) != -1) {
                MessageOutput.printDirect((char) i);
            }
        } catch (IOException ex) {
            String s = ex.getMessage();
            if (!s.startsWith("Bad file number")) {
                throw ex;
            }
        }
    }

    /**
     *  Create a Thread that will retrieve and display any output.
     *  Needs to be high priority, else debugger may exit before
     *  it can be displayed.
     */
    private void displayRemoteOutput(final InputStream stream) {
        Thread thr = new Thread("output reader") {

            public void run() {
                try {
                    dumpStream(stream);
                } catch (IOException ex) {
                    MessageOutput.fatalError("Failed reading output");
                } finally {
                    notifyOutputComplete();
                }
            }
        };
        thr.setPriority(Thread.MAX_PRIORITY - 1);
        thr.start();
    }

    private void dumpFailedLaunchInfo(Process process) {
        try {
            dumpStream(process.getErrorStream());
            dumpStream(process.getInputStream());
        } catch (IOException e) {
            MessageOutput.println("Unable to display process output:", e.getMessage());
        }
    }

    private VirtualMachine launchTarget() {
        LaunchingConnector launcher = (LaunchingConnector) connector;
        try {
            VirtualMachine vm = launcher.launch(connectorArgs);
            process = vm.process();
            displayRemoteOutput(process.getErrorStream());
            displayRemoteOutput(process.getInputStream());
            return vm;
        } catch (IOException ioe) {
            ioe.printStackTrace();
            MessageOutput.fatalError("Unable to launch target VM.");
        } catch (IllegalConnectorArgumentsException icae) {
            icae.printStackTrace();
            MessageOutput.fatalError("Internal debugger error.");
        } catch (VMStartException vmse) {
            MessageOutput.println("vmstartexception", vmse.getMessage());
            MessageOutput.println();
            dumpFailedLaunchInfo(vmse.process());
            MessageOutput.fatalError("Target VM failed to initialize.");
        }
        return null;
    }

    private VirtualMachine attachTarget() {
        AttachingConnector attacher = (AttachingConnector) connector;
        try {
            return attacher.attach(connectorArgs);
        } catch (IOException ioe) {
            ioe.printStackTrace();
            MessageOutput.fatalError("Unable to attach to target VM.");
        } catch (IllegalConnectorArgumentsException icae) {
            icae.printStackTrace();
            MessageOutput.fatalError("Internal debugger error.");
        }
        return null;
    }

    private VirtualMachine listenTarget() {
        ListeningConnector listener = (ListeningConnector) connector;
        try {
            String retAddress = listener.startListening(connectorArgs);
            MessageOutput.println("Listening at address:", retAddress);
            vm = listener.accept(connectorArgs);
            listener.stopListening(connectorArgs);
            return vm;
        } catch (IOException ioe) {
            ioe.printStackTrace();
            MessageOutput.fatalError("Unable to attach to target VM.");
        } catch (IllegalConnectorArgumentsException icae) {
            icae.printStackTrace();
            MessageOutput.fatalError("Internal debugger error.");
        }
        return null;
    }
}
