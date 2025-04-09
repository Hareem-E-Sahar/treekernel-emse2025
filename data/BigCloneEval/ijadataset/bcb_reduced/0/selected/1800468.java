package sf2.vm.impl.xen;

import java.io.File;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import sf2.core.Config;
import sf2.core.ConfigException;
import sf2.core.ProcessExecutor;
import sf2.core.ProcessExecutorException;
import sf2.log.Logging;
import sf2.vm.VMException;
import sf2.vm.VMM;
import sf2.vm.VMNetwork;
import sf2.vm.VirtualMachine;

public class XenVMM implements VMM, XenCommon {

    protected static final String LOG_NAME = "XenVMM";

    protected static boolean verbose;

    protected static boolean useSudo;

    protected static File snapDir;

    protected static boolean cleanup;

    protected static String xenCmd;

    protected static int waitTime;

    static {
        try {
            Config config = Config.search();
            verbose = config.getBoolean(PROP_VERBOSE, DEFAULT_VERBOSE);
            useSudo = config.getBoolean(PROP_USE_SUDO, DEFAULT_USE_SUDO);
            xenCmd = config.get(PROP_XEN_CMD, DEFAULT_XEN_CMD);
            snapDir = new File(config.get(PROP_SNAP_DIR, DEFAULT_SNAP_DIR));
            cleanup = config.getBoolean(PROP_CLEANUP, DEFAULT_CLEANUP);
            waitTime = config.getInt(PROP_WAIT_TIME, DEFAULT_WAIT_TIME);
        } catch (ConfigException e) {
            e.printStackTrace();
        }
    }

    protected Logging logging = Logging.getInstance();

    protected boolean available = false;

    protected boolean running = false;

    protected List<VirtualMachine> virtualMachines = new LinkedList<VirtualMachine>();

    public String getName() {
        return "Xen";
    }

    public boolean isAvailable() {
        try {
            if (exec("test", "-x", xenCmd) == 0) return true;
        } catch (VMException e) {
        }
        return false;
    }

    public boolean isRunning() {
        return running;
    }

    public void start() throws VMException {
        if (running) throw new VMException("Xen is already running.");
        if (cleanup) {
            logging.debug(LOG_NAME, "cleanup the snapshot dir path=" + snapDir);
            recursiveDelete(snapDir);
            snapDir.mkdirs();
        }
        if (useSudo) exec("sudo", xenCmd, "shutdown", "-a", "-w"); else exec(xenCmd, "shutdown", "-a", "-w");
        running = true;
    }

    protected void recursiveDelete(File file) {
        if (file.isDirectory()) {
            for (File f : file.listFiles()) recursiveDelete(f);
        }
        file.delete();
    }

    public void shutdown() {
        if (!running) return;
        try {
            for (VirtualMachine vm : virtualMachines) vm.halt(true);
            virtualMachines.clear();
            if (useSudo) exec("sudo", xenCmd, "shutdown", "-a"); else exec(xenCmd, "shutdown", "-a");
        } catch (VMException e) {
        }
    }

    public Iterator<VirtualMachine> iterator() {
        return virtualMachines.iterator();
    }

    public void register(VirtualMachine vm) {
        virtualMachines.add(vm);
    }

    public void unregister(VirtualMachine vm) {
        virtualMachines.remove(vm);
    }

    protected int exec(String... cmd) throws VMException {
        int ret = 0;
        logging.debug(LOG_NAME, "Xen CMD: " + Arrays.toString(cmd));
        try {
            ret = ProcessExecutor.exec(verbose, cmd);
        } catch (ProcessExecutorException e) {
            throw new VMException(e);
        }
        return ret;
    }
}
