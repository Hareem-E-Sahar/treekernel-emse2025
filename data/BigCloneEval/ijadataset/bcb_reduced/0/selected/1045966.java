package com.fujitsu.arcon.njs.priest;

import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.Vector;
import java.util.zip.ZipEntry;
import org.unicore.ajo.AbstractTask;
import org.unicore.ajo.ExecuteTask;
import org.unicore.outcome.AbstractActionStatus;
import org.unicore.outcome.AbstractTask_Outcome;
import org.unicore.resources.AlternativeUspace;
import org.unicore.resources.CapabilityResource;
import org.unicore.resources.FloatingPoint;
import org.unicore.resources.Home;
import org.unicore.resources.InformationResource;
import org.unicore.resources.Limit;
import org.unicore.resources.LimitTypeValue;
import org.unicore.resources.Node;
import org.unicore.resources.NumericInfoResource;
import org.unicore.resources.Priority;
import org.unicore.resources.PriorityValue;
import org.unicore.resources.Processor;
import org.unicore.resources.Resource;
import org.unicore.resources.ResourceBooking;
import org.unicore.resources.Root;
import org.unicore.resources.RunTime;
import org.unicore.resources.SoftwareResource;
import org.unicore.resources.StorageServer;
import org.unicore.resources.Tasks;
import org.unicore.resources.Temp;
import org.unicore.resources.TextInfoResource;
import org.unicore.resources.Threads;
import org.unicore.resources.USpace;
import org.unicore.sets.ResourceEnumeration;
import org.unicore.sets.ResourceSet;
import org.unicore.utility.UserAttributesConverter;
import com.fujitsu.arcon.njs.NJSGlobals;
import com.fujitsu.arcon.njs.actions.EKnownAction;
import com.fujitsu.arcon.njs.actions.MappedStorage;
import com.fujitsu.arcon.njs.interfaces.IncarnatedUser;
import com.fujitsu.arcon.njs.interfaces.NJSException;
import com.fujitsu.arcon.njs.interfaces.TSIConnection;
import com.fujitsu.arcon.njs.interfaces.TSIUnavailableException;

public class BatchTargetSystem extends TargetSystem {

    private static Reader batch_reader = new Reader();

    public static MissalReader getBatchReader() {
        return batch_reader;
    }

    public BatchTargetSystem() {
        system_overhead = new SingleField.Deacon("System Overhead");
        qstat_xlogin_r = new SingleField.Deacon("qstat xlogin");
        priorities = new MultiField.Deacon(PriorityValue.elements(), "Resource Priority");
        queuep = new QueueDeacon(this);
        queues = new Hashtable();
        pqs = new Vector[5];
        next_update_time = 0;
    }

    Map sub_spaces = new HashMap();

    Map limits_os_specifics = new HashMap();

    private void incarnateLimits(StringBuffer commands, EKnownAction eka) throws NJSException {
        commands.append("# Limits from AbstractTask resources\n");
        ResourceEnumeration re = ((AbstractTask) eka.getAction()).getResources().elements();
        while (re.hasMoreElements()) {
            Resource r = re.nextElement();
            if (r instanceof Limit) {
                Limit request = (Limit) r;
                Object[] specifics = (Object[]) limits_os_specifics.get(request);
                if (specifics == null) {
                    throw new NJSException("Limit <" + request.getType() + "> is not supported.");
                } else {
                    String os_name = (String) specifics[0];
                    double scale = ((Double) specifics[1]).doubleValue();
                    Limit os_limit = (Limit) specifics[2];
                    double scaled_request = request.getLimit() * scale;
                    if (os_limit.isUnlimited()) {
                    } else {
                        if (request.isUnlimited()) {
                            throw new NJSException("Limit request <" + request.getType() + "> is unlimited, but there is a limit <" + os_limit.getLimit() / scale + ">");
                        } else {
                            if (scaled_request > os_limit.getLimit()) {
                                throw new NJSException("Limit request <" + request.getType() + "> is <" + request.getLimit() + "> but there is a smaller limit <" + os_limit.getLimit() / scale + ">");
                            }
                        }
                    }
                    if (request.isUnlimited()) {
                        commands.append(Task.Priest.LIMIT + os_name + " unlimited\n");
                    } else {
                        int iscaled_request = (int) (scaled_request + 0.5);
                        commands.append(Task.Priest.LIMIT + os_name + " " + iscaled_request + "\n");
                    }
                }
            }
        }
        commands.append("# End of Limits from AbstractTask resources\n");
    }

    public void reorganise() {
        HashMap temp = new HashMap();
        Iterator i = sub_spaces.entrySet().iterator();
        while (i.hasNext()) {
            Map.Entry me = (Map.Entry) i.next();
            temp.put(me.getKey(), me.getValue());
        }
        sub_spaces = temp;
        super.reorganise();
    }

    /** 
	 * Return the a string with complete complete path to the file.
	 *
	 **/
    public String incarnateTargetFile(String file_name, com.fujitsu.arcon.njs.actions.MappedStorage storage) {
        return storage.getLocation() + file_name;
    }

    public void incarnateMappedStorage(com.fujitsu.arcon.njs.actions.MappedStorage storage) throws NJSException {
        if (storage != null) storage.incarnateRoot((String) sub_spaces.get(storage.getStorage()));
    }

    public String mapStorage(org.unicore.resources.Storage storage) {
        return (String) sub_spaces.get(storage);
    }

    org.unicore.resources.Memory default_memory;

    /**
	 * Return the default _per node_ memory for
	 * a task run on this TSI
	 *
	 **/
    private final double getDefaultMemory() {
        return default_memory.getDefaultRequest();
    }

    Processor default_processor;

    /**
	 * Return the default number of processors per node to
	 * be used for a task run on this TSI
	 *
	 **/
    private final int getDefaultProcessorCount() {
        return (int) default_processor.getDefaultRequest();
    }

    Node default_node;

    /**
	 * Return the default number of nodes to
	 * be used for a task run on this TSI
	 *
	 **/
    private final int getDefaultNodeCount() {
        return (int) default_node.getDefaultRequest();
    }

    FloatingPoint default_fp_time;

    /**
	 * Return the default time based on FP ops
	 * be used for a task run on this TSI
	 *
	 **/
    private final int getDefaultFPTime() {
        return (int) default_fp_time.getDefaultTime();
    }

    /**
	 * Return the default value for the priority
	 * if not set by task or Missal
	 *
	 **/
    final PriorityValue getDefaultPriority() {
        return PriorityValue.WHENEVER;
    }

    /**
	 * Return the number of (floating point) operations
	 * per second performed by each (homogeneous) node
	 *
	 **/
    private final double getNodeRating() {
        return default_fp_time.getPerformance();
    }

    /**
	 * Return any overhead imposed by the site for
	 * automatically added pre and post processing scripts
	 *
	 **/
    private int getSystemOverhead() {
        if (overhead < 0) {
            try {
                overhead = Integer.valueOf(system_overhead.incarnate("", "foobar", null, null)).intValue();
            } catch (NumberFormatException nfex) {
                logger.config("Execution TSI <" + getName() + "> System overhead value is not an integer (or not set in IDB). Setting to 0.");
                overhead = 0;
            } catch (Exception ex) {
                overhead = 0;
            }
        }
        return overhead;
    }

    private int overhead = -1;

    SingleField.Deacon system_overhead;

    private IncarnatedUser qstat_user = null;

    SingleField.Deacon qstat_xlogin_r;

    public IncarnatedUser getQstatUser() {
        if (qstat_user == null) {
            try {
                qstat_user = new com.fujitsu.arcon.njs.uudb.IncarnatedUserImpl(null, com.fujitsu.arcon.njs.interfaces.Role.USER, qstat_xlogin_r.incarnate("", "", null, null), null);
            } catch (ArrayIndexOutOfBoundsException aioobex) {
            }
        }
        return qstat_user;
    }

    /**
	 * Batch subsystems differ about how the job limits
	 * are expressed
	 *
	 **/
    private boolean per_processor_limits = true;

    public void setLimitsPerProcessor(boolean b) {
        per_processor_limits = b;
    }

    public boolean areLimitsPerProcessor() {
        return per_processor_limits;
    }

    private boolean per_node_limits = false;

    public void setLimitsPerNode(boolean b) {
        per_node_limits = b;
    }

    public boolean areLimitsPerNode() {
        return per_node_limits;
    }

    private boolean per_job_limits = false;

    public void setLimitsPerJob(boolean b) {
        per_job_limits = b;
    }

    public boolean areLimitsPerJob() {
        return per_job_limits;
    }

    private boolean send_bss_email = true;

    public void setSendEmail(boolean b) {
        send_bss_email = b;
    }

    public boolean sendEmail() {
        return send_bss_email;
    }

    /**
	 * Consign the incarnation of a AbstractAction to
	 * the TSI process. 
	 *
	 **/
    public void consign(EKnownAction eka) {
        if (eka.getIncarnation() == null || eka.getIncarnation().equals("")) {
            eka.setExecutionResults("TSI_OK\nUNICORE EXIT STATUS 0 +\n");
            eka.setStatus(AbstractActionStatus.EXECUTING);
            return;
        }
        try {
            if (gas_instance != null) gas_instance.taskToBeExecuted((AbstractTask) eka.getAction(), eka.getIncarnatedUser(), eka.getAction().getId());
        } catch (NJSException e) {
            eka.logTrace("Failing in consign as job is unacceptable to GAS <" + e.getMessage() + ">");
            handleTSIReply("TSI_FAILED INCARNATION: Job unacceptable to GAS <" + e.getMessage() + ">", eka);
            return;
        }
        if (!(eka.getAction() instanceof ExecuteTask)) {
            eka.logTrace("Executed directly by TSI (no BSS)");
            consignImmediately(eka);
            return;
        }
        ExecuteTask task = (ExecuteTask) eka.getAction();
        double memory = getDefaultMemory();
        int processors_p_node = getDefaultProcessorCount();
        int nodes = getDefaultNodeCount();
        double fp_time = getDefaultFPTime();
        double run_time = -1.0;
        PriorityValue priority = getDefaultPriority();
        double tasks = 1;
        double threads_p_task = 1;
        String software_resources = "";
        String info_resources = "";
        String limits = "";
        try {
            ResourceEnumeration e = task.getResources().elements();
            while (e.hasMoreElements()) {
                org.unicore.resources.Resource r = e.nextElement();
                if (r instanceof Processor) {
                    processors_p_node = (int) (((Processor) r).getRequest() + 0.5);
                } else if (r instanceof Node) {
                    nodes = (int) (((Node) r).getRequest() + 0.5);
                } else if (r instanceof org.unicore.resources.Memory) {
                    memory = ((org.unicore.resources.Memory) r).getRequest();
                } else if (r instanceof FloatingPoint) {
                    fp_time = ((FloatingPoint) r).getRequest();
                    fp_time = fp_time / getNodeRating();
                } else if (r instanceof RunTime) {
                    run_time = ((RunTime) r).getRequest();
                } else if (r instanceof Priority) {
                    priority = ((Priority) r).getValue();
                } else if (r instanceof SoftwareResource) {
                    software_resources += "#TSI_SWR " + ((SoftwareResource) r).getName() + "\n";
                } else if (r instanceof ResourceBooking) {
                    info_resources += "#TSI_RESERVATION_REFERENCE " + ((ResourceBooking) r).getReference() + "\n";
                } else if (r instanceof Limit) {
                    limits += "#TSI_LIMIT " + ((Limit) r).getType().getName().toUpperCase().replaceAll(" ", "_") + " " + ((Limit) r).getLimit() + "\n";
                } else if (r instanceof Tasks) {
                    tasks = ((Tasks) r).getNumber();
                } else if (r instanceof Threads) {
                    threads_p_task = ((Threads) r).getNumber();
                } else if (r instanceof CapabilityResource) {
                } else if (r instanceof InformationResource) {
                    if (r instanceof NumericInfoResource) {
                        NumericInfoResource nir = (NumericInfoResource) r;
                        info_resources += "#TSI_INFO " + nir.getTag() + " " + nir.getValue() + "\n";
                    } else if (r instanceof TextInfoResource) {
                        TextInfoResource tir = (TextInfoResource) r;
                        info_resources += "#TSI_INFO " + tir.getTag() + " " + tir.getValue() + "\n";
                    } else {
                        eka.logTrace("Unknown type of InformationResource: " + r);
                    }
                } else {
                    eka.logTrace("Ignoring a resource request of type <" + r + "> while building task limits.");
                }
            }
        } catch (NullPointerException ex) {
            eka.logTrace("No resources set, using defaults");
        }
        if (tasks < 0.5) tasks = 1.0;
        if (nodes < 1) nodes = 1;
        if (threads_p_task < 0.5) threads_p_task = 1.0;
        if (processors_p_node < 1) processors_p_node = 1;
        int nodes_p_task = (int) Math.floor(nodes / tasks);
        int tasks_p_node = (int) Math.ceil(tasks / nodes);
        int threads_p_processor = (int) Math.ceil((tasks * threads_p_task) / (nodes * processors_p_node));
        int processors_p_thread = (int) Math.floor((nodes * processors_p_node) / (tasks * threads_p_task));
        int tasks_p_processor = (int) (threads_p_processor / threads_p_task + 0.5);
        int processors_p_task = (int) (processors_p_thread * threads_p_task + 0.5);
        if (processors_p_task == 0) processors_p_task = (int) threads_p_task / threads_p_processor;
        double time;
        if (run_time >= 0.0) {
            time = run_time;
        } else {
            time = fp_time;
        }
        if (per_processor_limits) {
            if (processors_p_node != 0) memory = memory / processors_p_node;
        } else if (per_node_limits) {
        } else if (per_job_limits) {
            memory = memory * nodes;
        }
        time += getSystemOverhead();
        eka.logConfig("Incarnated Resources: nodes <" + nodes + "> processors <" + processors_p_node + "> memory <" + memory + ">");
        eka.logConfig("Incarnated Resources: Time <" + time + ">");
        eka.logConfig("Incarnated Resources: priority <" + priority + ">");
        if (info_resources != null) eka.logConfig(info_resources);
        if (software_resources != null) eka.logConfig(software_resources);
        if (processors_p_node <= 0 || nodes <= 0 || memory < 0 || time < 0) {
            eka.logTrace("Failing in consign as resource requests not sensible");
            handleTSIReply("TSI_FAILED INCARNATION: The requested resources are not all sensible.", eka);
            return;
        }
        String queue = null;
        int priority_value = priority.getValue();
        while (priority_value <= PriorityValue.WHENEVER.getValue() && queue == null) {
            Vector vvv = pqs[priority_value];
            if (vvv == null) {
            } else {
                Enumeration e = vvv.elements();
                while (e.hasMoreElements()) {
                    Queue q = (Queue) e.nextElement();
                    if (processors_p_node >= q.min_proc && processors_p_node <= q.max_proc && nodes >= q.min_node && nodes <= q.max_node && time >= q.min_time && time <= q.max_time && memory >= q.min_mory && memory <= q.max_mory) {
                        queue = q.name;
                        break;
                    }
                }
            }
            priority_value++;
        }
        if (queue == null) queue = default_queue.name;
        eka.logConfig("Selected queue: " + queue);
        StringBuffer commands = new StringBuffer();
        if (eka.requiresUspace()) {
            if (!eka.getUspace().isIncarnated()) {
                eka.logTrace("Failing before consign: Uspace has not been created");
                handleTSIReply("TSI_FAILED INCARNATION: Uspace has not been created", eka);
                return;
            }
            GeneralData.Priest.incarnateUserHeader(commands, eka);
        } else {
            GeneralData.Priest.incarnateHeader(commands, eka);
        }
        try {
            incarnateLimits(commands, eka);
            commands.append(makeTSIIdentityLine(eka.getUspace()));
        } catch (NJSException nex) {
            eka.logTrace("Failing before consign,  " + nex.getMessage());
            handleTSIReply("TSI_FAILED INCARNATION: " + nex.getMessage(), eka);
            return;
        }
        commands.append("#TSI_SUBMIT\n");
        commands.append("#TSI_JOBNAME " + task.getName().trim().replace(' ', '_') + "\n");
        commands.append("#TSI_OUTCOME_DIR " + eka.getOutcomeStore().getDirectory() + "\n");
        commands.append("#TSI_TIME " + (new Double(time)).intValue() + "\n");
        commands.append("#TSI_MEMORY " + (new Double(memory)).intValue() + "\n");
        if (nodes * processors_p_node == 1) {
            commands.append("#TSI_NODES NONE\n");
            commands.append("#TSI_PROCESSORS 1\n");
        } else {
            commands.append("#TSI_NODES " + nodes + "\n");
            commands.append("#TSI_PROCESSORS " + processors_p_node + "\n");
        }
        commands.append("#TSI_HOST_NAME " + getName() + "\n");
        commands.append("#TSI_STORAGE_REQUEST " + eka.getEffectiveStorage().getLocation() + " " + (new Double(eka.getEffectiveStorage().getStorage().getRequest())).intValue() + "\n");
        if (eka.getEffectiveStorage() != eka.getRequestedUspace()) commands.append("#TSI_STORAGE_REQUEST " + eka.getRequestedUspace().getLocation() + " " + (new Double(eka.getRequestedUspace().getStorage().getRequest())).intValue() + "\n");
        commands.append(software_resources);
        commands.append(info_resources);
        if (queue.startsWith("noname")) {
            commands.append("#TSI_QUEUE NONE \n");
        } else {
            commands.append("#TSI_QUEUE " + queue + "\n");
        }
        if (sendEmail()) {
            String email = UserAttributesConverter.getEmailAddress(eka.getUser());
            if (email != null && !email.equals("")) {
                commands.append("#TSI_EMAIL " + email + "\n");
            } else {
                commands.append("#TSI_EMAIL NONE\n");
            }
        }
        commands.append("#TSI_SCRIPT\n");
        String userDN = "'" + eka.getUser().getUser().getName() + "'";
        commands.append("UC_USERDN=" + userDN + "; export UC_USERDN;\n");
        commands.append("UC_NODES=" + nodes + "; export UC_NODES;\n");
        commands.append("UC_PROCESSORS=" + processors_p_node + "; export UC_PROCESSORS;\n");
        commands.append("UC_TASKS=" + tasks + "; export UC_TASKS\n");
        commands.append("UC_THREADS=" + threads_p_task + "; export UC_THREADS\n");
        commands.append("UC_TASKS_P_NODE=" + tasks_p_node + "; export UC_TASKS_P_NODE\n");
        commands.append("UC_NODES_P_TASK=" + nodes_p_task + "; export UC_NODES_P_TASK\n");
        commands.append("UC_THREADS_P_PROCESSOR=" + threads_p_processor + "; export UC_THREADS_P_PROCESSOR\n");
        commands.append("UC_PROCESSORS_P_THREAD=" + processors_p_thread + "; export UC_PROCESSORS_P_THREAD\n");
        commands.append("UC_TASKS_P_PROCESSOR=" + tasks_p_processor + "; export UC_TASKS_P_PROCESSOR\n");
        commands.append("UC_PROCESSORS_P_TASK=" + processors_p_task + "; export UC_PROCESSORS_P_TASK\n");
        commands.append(eka.getIncarnation());
        commands.append(GeneralData.Priest.afterCommand("Task incarnation set non-zero error code"));
        GeneralData.Priest.incarnateFooter(commands);
        TSIConnection c = null;
        String reply = null;
        try {
            while (true) {
                try {
                    c = getTSIConnectionFactory().getTSIConnection(eka.getIncarnatedUser());
                    if (eka.wasAborted()) {
                        reply = "TSI_FAILED: Action killed while waiting to contact/got TSI.\n";
                    } else {
                        reply = c.send(commands.toString());
                    }
                    break;
                } catch (TSIUnavailableException tuex) {
                    if (eka.wasAborted()) {
                        reply = "TSI_FAILED: Action killed while waiting to contact TSI.\n";
                        break;
                    } else {
                        eka.logTrace("Could not contact TSI because <" + tuex.getMessage() + ">. Will try again");
                        synchronized (this) {
                            try {
                                wait(NJSGlobals.TSI_UNAVAILABLE_DELAY);
                            } catch (Exception ex) {
                            }
                        }
                    }
                }
            }
        } catch (NJSException nex) {
            reply = "TSI_FAILED: Cannot run on TSI: " + nex.getMessage() + "\n";
        } catch (java.io.IOException ex) {
            reply = "TSI_FAILED: Problems talking to the TSI.\n" + ex;
        } finally {
            try {
                c.done();
            } catch (Exception ex) {
            }
        }
        handleTSIReply(reply, eka);
    }

    /**
	 * Handle the reply from consigning an incarnated AA to
	 * the TSI (probably a Batch Subsystem)
	 *
	 **/
    private void handleTSIReply(String reply, EKnownAction eka) {
        if (reply.startsWith("TSI_FAILED")) {
            eka.logTrace("Failed in consign to BSS: " + reply);
            eka.setExecutionResults("TSI_FAILED CONSIGN: Failed to consign the incarnation to the BSS because:\n" + reply);
            if (gas_instance != null) gas_instance.jobEnded((ResourceSet) null, eka.getAction().getId());
        } else if (reply.startsWith("TSI_OK")) {
            eka.logTrace("Incarnation run using submit interactive option.");
        } else {
            String bss_id = reply.trim();
            eka.logEvent("Incarnation started on BSS with identifier <" + bss_id + ">");
            eka.setBSSId(bss_id);
            latest_bss_state.put(bss_id, AbstractActionStatus.EXECUTING);
            if (gas_instance != null) gas_instance.jobSubmitted(bss_id, eka.getAction().getId());
        }
        eka.setStatus(AbstractActionStatus.EXECUTING);
    }

    private void consignImmediately(EKnownAction eka) {
        StringBuffer commands = new StringBuffer();
        if (eka.requiresUspace()) {
            if (!eka.getUspace().isIncarnated()) {
                eka.setExecutionResults("TSI_FAILED CONSIGN: Uspace has not been created");
                eka.setStatus(AbstractActionStatus.EXECUTING);
                return;
            }
            GeneralData.Priest.incarnateUserHeader(commands, eka);
        } else {
            GeneralData.Priest.incarnateHeader(commands, eka);
        }
        try {
            incarnateLimits(commands, eka);
            commands.append(makeTSIIdentityLine(eka.getUspace()));
        } catch (NJSException nex) {
            eka.setExecutionResults("TSI_FAILED CONSIGN: Xlogin invalid\n" + nex);
            eka.setStatus(AbstractActionStatus.EXECUTING);
            return;
        }
        commands.append("#TSI_EXECUTESCRIPT\n");
        commands.append(eka.getIncarnation());
        commands.append(GeneralData.Priest.afterCommand("Task incarnation set non-zero error code"));
        GeneralData.Priest.incarnateFooter(commands);
        TSIConnection c = null;
        try {
            String reply;
            while (true) {
                try {
                    c = getTSIConnectionFactory().getTSIConnection(eka.getIncarnatedUser());
                    if (eka.wasAborted()) {
                        reply = "TSI_FAILED: Action killed while waiting to contact TSI.\n";
                    } else {
                        reply = c.send(commands.toString());
                    }
                    break;
                } catch (TSIUnavailableException tuex) {
                    if (eka.wasAborted()) {
                        reply = "TSI_FAILED: Action killed while waiting to contact TSI.\n";
                        break;
                    } else {
                        eka.logTrace("Could not contact TSI because <" + tuex.getMessage() + ">. Will try again");
                        synchronized (this) {
                            try {
                                wait(com.fujitsu.arcon.njs.NJSGlobals.TSI_UNAVAILABLE_DELAY);
                            } catch (Exception ex) {
                            }
                        }
                    }
                }
            }
            eka.setExecutionResults(reply);
        } catch (NJSException nex) {
            eka.setExecutionResults("TSI_FAILED CONSIGN: Cannot run on FTM TSI.\n" + nex);
        } catch (java.io.IOException ex) {
            eka.setExecutionResults("TSI_FAILED CONSIGN: Problems talking to FTM TSI.\n" + ex);
        } finally {
            try {
                c.done();
            } catch (Exception ex) {
            }
        }
        eka.setStatus(AbstractActionStatus.EXECUTING);
    }

    /**
	 * Execute the script, returning the results and throwing execption on error. These resources consumed here can't be tracked?
	 *
	 **/
    public String doCommand(String command, com.fujitsu.arcon.njs.interfaces.IncarnatedUser user, UspaceManager.Uspace us) throws NJSException, TSIUnavailableException {
        StringBuffer commands = new StringBuffer();
        GeneralData.Priest.incarnateHeader(commands, "command", true);
        commands.append(makeTSIIdentityLine(us));
        commands.append("#TSI_EXECUTESCRIPT\n");
        commands.append(command + "\n");
        commands.append(GeneralData.Priest.afterCommand("Command execution set non-zero error code"));
        GeneralData.Priest.incarnateFooter(commands);
        TSIConnection c = null;
        String reply = null;
        try {
            c = getTSIConnectionFactory().getTSIConnection(user);
            reply = c.send(commands.toString());
        } catch (java.io.IOException ex) {
            throw new NJSException("Problems communicationg with TSI: " + ex);
        } finally {
            if (c != null) c.done();
        }
        if (reply.startsWith("TSI_FAILED")) {
            int index = reply.indexOf("UNICORE EXIT STATUS");
            if (index > 0) {
                reply = reply.substring(0, index);
            }
            throw new NJSException(reply.substring(11));
        } else {
            return reply;
        }
    }

    private long next_update_time;

    private Hashtable latest_bss_state = new Hashtable();

    boolean updating = false;

    /**
	 * Return the status of the KA according to the BSS.
	 * Do not change the KA's copy as the BSS status may 
	 * not be accurate (it could miss a job moving from
	 * one queue to another etc).
	 *
	 **/
    public AbstractActionStatus query(EKnownAction eka) {
        if (eka.isOnBSS()) {
            if (!updating && System.currentTimeMillis() > next_update_time) updateFromBSS();
            AbstractActionStatus current = (AbstractActionStatus) latest_bss_state.get(eka.getBSSId());
            if (current == null) {
                if (latest_bss_state.contains(eka.getBSSId())) {
                    current = eka.getStatus();
                } else {
                    current = AbstractActionStatus.DONE;
                }
            }
            return current;
        } else {
            return AbstractActionStatus.DONE;
        }
    }

    /**
	 * Update the internal copy of the current BSS state.
	 *
	 **/
    private synchronized void updateFromBSS() {
        TSIConnection c = null;
        try {
            updating = true;
            c = getTSIConnectionFactory().getTSIConnection(getQstatUser());
            String reply = c.send("#TSI_GETSTATUSLISTING\n");
            if (reply.startsWith("TSI_FAILED")) {
                logger.warning("Failed to update status of jobs, using old values. Reason: " + reply);
                return;
            }
            try {
                Hashtable updating_bss_state = new Hashtable(11);
                StringTokenizer st = new StringTokenizer(reply);
                st.nextToken();
                while (st.hasMoreElements()) {
                    String bssstring = null;
                    String state = st.nextToken();
                    AbstractActionStatus status = null;
                    while (status == null) {
                        bssstring = state;
                        if (st.hasMoreTokens()) {
                            state = st.nextToken();
                            status = matchStatus(state);
                            if (status == null) {
                                if (state.equalsIgnoreCase("UNKNOWN")) {
                                    status = AbstractActionStatus.RUNNING;
                                    logger.warning("Updating BSS job states (QSTAT) got UNKNOWN for status of <" + bssstring + ">. Assuming RUNNING...");
                                } else {
                                    logger.warning("Updating BSS job states (QSTAT) got unexpected element for status <" + state + ">/<" + bssstring + ">. Continuing though out of sync...");
                                }
                            }
                        } else {
                            logger.warning("Updating BSS job states (QSTAT) got unexpected element in reply <" + bssstring + ">. Continuing though ...");
                            break;
                        }
                    }
                    if (status == null) {
                        updating_bss_state.put(bssstring, AbstractActionStatus.RUNNING);
                    } else if (!status.isEquivalent(AbstractActionStatus.DONE)) {
                        updating_bss_state.put(bssstring, status);
                    } else {
                    }
                }
                next_update_time = System.currentTimeMillis() + NJSGlobals.getUpdateFrequency();
                latest_bss_state = updating_bss_state;
            } catch (Exception ex) {
                logger.warning("Error parsing host reply to QSTAT. Using old data.\n" + ex + "\n" + reply);
                ex.printStackTrace();
            }
        } catch (NJSException nex) {
            logger.warning("Can't run on TSI<" + nex.getMessage() + ">. Using old data");
        } catch (TSIUnavailableException tuex) {
            logger.warning("Failed to contact TSI (timeout?). Using old data");
        } catch (java.io.IOException ex) {
            try {
                logger.warning(getTSIConnectionFactory().toString(), "Problem communicating while querying job status (Is TSI down?). Using old data.", ex);
            } catch (Exception exex) {
            }
        } finally {
            try {
                if (c != null) c.done();
            } catch (Exception ex) {
            }
            updating = false;
        }
    }

    /**
	 * Try to match the input string to a status for the executing
	 * AbstractAction.
	 * 
	 * @param state
	 * @return The matching AbstractActionStatus or null if no match found
	 */
    private AbstractActionStatus matchStatus(String state) {
        AbstractActionStatus status;
        if (state.equalsIgnoreCase("QUEUED")) {
            status = AbstractActionStatus.QUEUED;
        } else if (state.equalsIgnoreCase("RUNNING")) {
            status = AbstractActionStatus.RUNNING;
        } else if (state.equalsIgnoreCase("SUSPENDED")) {
            status = AbstractActionStatus.SUSPENDED;
        } else if (state.equalsIgnoreCase("FROZEN")) {
            status = AbstractActionStatus.FROZEN;
        } else if (state.equalsIgnoreCase("DONE")) {
            status = AbstractActionStatus.DONE;
        } else {
            status = null;
        }
        return status;
    }

    /**
	 * Process the stdout and stderr files from the execution.
	 *    - both files are moved to the outcome directory (if action is an ExecuteTask)
	 *    - stderr is returned for processing
	 *
	 * Returns true if the job is done and processing went OK
	 *         false ifthe job is still executing
	 * Throws an exception if the processing went wrong.
	 **/
    public boolean fetchExecutionResults(EKnownAction eka) throws NJSException {
        if (eka.areResultsFetched()) {
            if (gas_instance != null) {
                if (eka.getOutcome() instanceof AbstractTask_Outcome) {
                    ResourceSet used = gas_instance.parseResources(eka.getExecutionResults());
                    ((AbstractTask_Outcome) eka.getOutcome()).setResources(used);
                    gas_instance.jobEnded(used, eka.getId());
                }
            }
            return true;
        } else {
            String bssid = eka.getBSSId();
            if (bssid == null) {
                if (logger.CHAT) logger.chat("No BSS Id, assuming interactive execution.");
                bssid = "NONE";
            }
            if (bssid.equals("")) {
                throw new NJSException("Suspicious BSS Identifier, cannot fetch stdout/stderr.");
            }
            String outcome_directory = eka.getOutcomeStore().getDirectory();
            String disposition;
            if (eka.getAction() instanceof ExecuteTask) {
                if (eka.wasCancelled()) {
                    disposition = "DELETE";
                } else {
                    disposition = "KEEP";
                }
            } else {
                disposition = "DELETE";
            }
            TSIConnection c = null;
            try {
                String commands = makeTSIIdentityLine(eka.getUspace()) + "#TSI_ENDPROCESSING\n" + "#TSI_BSSID " + bssid + "\n" + "#TSI_OUTCOME_DIR " + outcome_directory + "\n" + "#TSI_DISPOSITION " + disposition + "\n";
                c = getTSIConnectionFactory().getTSIConnection(eka.getIncarnatedUser());
                String reply = c.send(commands);
                if (reply.indexOf("UNICORE EXIT STATUS ") >= 0) {
                    eka.setExecutionResults(reply);
                    if (gas_instance != null) {
                        if (eka.getOutcome() instanceof AbstractTask_Outcome) {
                            ResourceSet used = gas_instance.parseResources(eka.getExecutionResults());
                            ((AbstractTask_Outcome) eka.getOutcome()).setResources(used);
                            gas_instance.jobEnded(used, eka.getId());
                        }
                    }
                    return true;
                } else if (reply.indexOf("TSI_STILLEXECUTING") >= 0) {
                    if (eka.continueTestingForCompletion()) {
                        eka.logEvent("Job may be complete but output files not found. Assuming that job still executing");
                        return false;
                    } else {
                        eka.logError("Can't find output files, tried lots of times, ending job with error.");
                        eka.setExecutionResults("TSI_OK\nUNICORE EXIT STATUS 999 +");
                        if (gas_instance != null) gas_instance.jobEnded((ResourceSet) null, eka.getId());
                        return true;
                    }
                } else if (reply.startsWith("TSI_FAILED")) {
                    throw new NJSException("Problems in end processing: " + reply);
                } else {
                    if (eka.wasAborted() || eka.wasCancelled()) {
                        eka.setExecutionResults("TSI_OK\nUNICORE EXIT STATUS 0 +");
                        if (gas_instance != null) gas_instance.jobEnded((ResourceSet) null, eka.getId());
                        return true;
                    } else {
                        if (eka.continueTestingForCompletion()) {
                            eka.logEvent("Output files found but incomplete (no status). Assuming that job still executing");
                            return false;
                        } else {
                            eka.logError("Output files are incomplete, tried lots of times, ending job with error.");
                            eka.setExecutionResults("TSI_OK\nUNICORE EXIT STATUS 999 +");
                            if (gas_instance != null) gas_instance.jobEnded((ResourceSet) null, eka.getId());
                            return true;
                        }
                    }
                }
            } catch (TSIUnavailableException tuex) {
                eka.logTrace("Execution result not fectched yet because: " + tuex.getMessage());
                return false;
            } catch (Exception ex) {
                throw new NJSException("Fetch of results (stdout/stderr) failed\n" + ex.getMessage());
            } finally {
                try {
                    if (c != null) c.done();
                } catch (Exception ex) {
                }
            }
        }
    }

    public void abort(EKnownAction eka) throws NJSException {
        docommand(eka, "#TSI_ABORTJOB");
    }

    public void cancel(EKnownAction eka) throws NJSException {
        docommand(eka, "#TSI_CANCELJOB");
    }

    public void resume(EKnownAction eka) throws NJSException {
        docommand(eka, "#TSI_RESUMEJOB");
    }

    public void hold(EKnownAction eka, boolean hold_request) throws NJSException {
        docommand(eka, "#TSI_HOLDJOB" + (hold_request ? "" : "\n#TSI_FREEZE"));
    }

    private void docommand(EKnownAction eka, String command) throws NJSException {
        if (eka.isOnBSS()) {
            TSIConnection c = null;
            try {
                c = getTSIConnectionFactory().getTSIConnection(eka.getIncarnatedUser());
                String reply = c.send(makeTSIIdentityLine(eka.getUspace()) + command + "\n#TSI_BSSID " + eka.getBSSId() + "\n");
                if (reply.startsWith("TSI_FAILED")) {
                    if (logger.CHAT) logger.chat("Task control action failed on BSS\n" + reply);
                    throw new NJSException(reply);
                }
            } catch (TSIUnavailableException tuex) {
                throw new NJSException("Command was not executed by TSI because: " + tuex.getMessage());
            } catch (Exception ex) {
                throw new NJSException(ex.getMessage());
            } finally {
                try {
                    c.done();
                } catch (Exception ex) {
                }
            }
        }
    }

    /**
	 * Write the files to the Uspace (incarnation of IncarnateFiles)
	 * use the TSI PutFiles command.
	 *
	 * V4 will write to any Storage (Uspace, AlternativeUspace, Home, Root, Temp ...)
	 *
	 **/
    public void writeFiles(MappedStorage storage, String[] names, byte[][] contents, EKnownAction eka, boolean overwrite) throws NJSException, TSIUnavailableException {
        TSIConnection c = null;
        String commands;
        if (overwrite) {
            commands = "#TSI_PUTFILES\n#TSI_FILESACTION 0\n";
        } else {
            commands = "#TSI_PUTFILES\n#TSI_FILESACTION 1\n";
        }
        String dest_directory = storage.getLocation();
        try {
            c = getTSIConnectionFactory().getTSIConnection(eka.getUspace().getIncarnatedUser());
            String reply;
            if (eka.wasAborted()) {
                reply = "TSI_FAILED: Action killed while waiting to contact TSI.\n";
            } else {
                reply = c.send(commands);
            }
            if (reply.startsWith("TSI_FAILED")) {
                throw new NJSException("Failed while establishing contact with TSI: " + reply);
            }
            if (c instanceof ClassicTSIConnection) {
                ClassicTSIConnection cc = (ClassicTSIConnection) c;
                for (int i = 0; i < names.length; i++) {
                    eka.logTrace("Writing file: " + dest_directory + names[i] + " size " + contents[i].length);
                    reply = cc.sendNU(dest_directory + names[i] + " 6\n");
                    if (reply.startsWith("TSI_FAILED")) {
                        throw new NJSException("Failed sending file to TSI. Name: <" + names[i] + "> Reason: " + reply);
                    }
                    reply = cc.sendNU(contents[i].length + "\n");
                    if (reply.startsWith("TSI_FAILED")) {
                        throw new NJSException("Failed sending file to TSI. Name: <" + names[i] + "> Size: <" + contents[i].length + "> Reason: " + reply);
                    }
                    c.sendData(contents[i], 0, contents[i].length);
                    reply = cc.sendNU("-1\n");
                    if (reply.startsWith("TSI_FAILED")) {
                        throw new NJSException("Failed terminating send of file to TSI. Name <" + names[i] + "> Reason: " + reply);
                    }
                }
                reply = cc.sendNU("-1\n");
                if (reply.startsWith("TSI_FAILED")) {
                    throw new NJSException("Failed terminating files stream: " + reply);
                }
            } else {
                for (int i = 0; i < names.length; i++) {
                    eka.logTrace("Writing file: " + dest_directory + names[i] + " size " + contents[i].length);
                    reply = c.send(dest_directory + names[i] + " 6\n");
                    if (reply.startsWith("TSI_FAILED")) {
                        throw new NJSException("Failed sending file to TSI. Name: <" + names[i] + "> Reason: " + reply);
                    }
                    reply = c.send(contents[i].length + "\n");
                    if (reply.startsWith("TSI_FAILED")) {
                        throw new NJSException("Failed sending file to TSI. Name: <" + names[i] + "> Size: <" + contents[i].length + "> Reason: " + reply);
                    }
                    c.sendData(contents[i], 0, contents[i].length);
                    reply = c.send("-1\n");
                    if (reply.startsWith("TSI_FAILED")) {
                        throw new NJSException("Failed terminating send of file to TSI. Name <" + names[i] + "> Reason: " + reply);
                    }
                }
                reply = c.send("-1\n");
                if (reply.startsWith("TSI_FAILED")) {
                    throw new NJSException("Failed terminating files stream: " + reply);
                }
            }
        } catch (IOException ioex) {
            throw new NJSException("Problems communications with TSI: " + ioex);
        } finally {
            c.done();
        }
    }

    /**
	 * Copy (part of a) file from Uspace into a buffer.
	 *
	 **/
    public int readFile(String file_name, byte[] buffer, long start, long length, int offset_in_buffer, IncarnatedUser user) throws NJSException, IOException, TSIUnavailableException {
        int read;
        TSIConnection c = null;
        try {
            c = getTSIConnectionFactory().getTSIConnection(user);
            String command = makeTSIIdentityLine(null) + "#TSI_GETFILECHUNK\n" + "#TSI_FILE " + file_name + "\n" + "#TSI_START " + start + "\n" + "#TSI_LENGTH " + length + "\n";
            String reply = c.send(command);
            int length_index = reply.indexOf("TSI_LENGTH");
            read = 0;
            if (length_index >= 0) {
                read = Integer.parseInt(reply.substring(length_index + 10).trim());
                c.getData(buffer, offset_in_buffer, read);
            }
            if (reply.startsWith("TSI_FAILED")) {
                throw new IOException("TSI <" + getName() + "> errors getting <" + file_name + ">: " + reply.substring(12));
            }
            if (reply.indexOf("FIFO") >= 0) {
                return read;
            } else {
                if (read != length) {
                    throw new IOException("TSI <" + getName() + "> errors getting directory/file: " + file_name + ", read <" + read + "> expected <" + length + ">");
                }
            }
        } finally {
            c.getLine();
            if (c != null) c.done();
        }
        return read;
    }

    /**
	}
	/**
	 * Fetch the directory and all subdirectories from the Uspace
	 * and write to the supplied ZipOutputStream
	 *
	 **/
    public void getDirectory(String dir_name, java.util.zip.ZipOutputStream out, com.fujitsu.arcon.njs.interfaces.IncarnatedUser user, UspaceManager.Uspace us) throws NJSException, IOException, TSIUnavailableException {
        TSIConnection c = null;
        boolean still_ok = true;
        try {
            c = getTSIConnectionFactory().getTSIConnection(user);
            String command = makeTSIIdentityLine(us) + "#TSI_GETDIRECTORY\n" + "#TSI_DIRECTORY " + dir_name + "\n";
            String reply;
            try {
                reply = c.send(command);
            } catch (IOException ioex) {
                throw new IOException("Getting directory from TSI <" + getName() + ">: " + ioex);
            }
            if (!reply.equals("TSI_OK\n")) {
                c.dead();
                throw new IOException("TSI <" + getName() + "> responded incorrectly while getting directory: " + reply);
            }
            byte[] buffer = new byte[BUFFER_SIZE];
            String root_dir = c.getLine();
            int root_length = root_dir.length() + 1;
            String file_name = c.getLine();
            while (!file_name.equals("TSI_OK")) {
                if (file_name.startsWith("TSI_FAILED")) {
                    c.getLine();
                    throw new IOException("TSI <" + getName() + "> errors getting directory: " + file_name);
                }
                String size = c.getLine();
                String mode = "6";
                if (size.indexOf(" ") > 0) {
                    mode = size.substring(size.indexOf(" ")).trim();
                    size = size.substring(0, size.indexOf(" ")).trim();
                }
                long num_bytes = Long.valueOf(size).longValue();
                byte[] m_mode = new byte[1];
                m_mode[0] = Byte.parseByte(mode);
                file_name = file_name.substring(root_length);
                if (logger.CHAT) logger.chat(getName(), "getDirectory expecting file <" + file_name + "> size <" + num_bytes + "> <" + m_mode[0] + ">");
                if (num_bytes == -1) {
                    try {
                        ZipEntry next_file = new ZipEntry(file_name);
                        next_file.setExtra(m_mode);
                        if (still_ok) out.putNextEntry(next_file);
                        int this_block = Integer.valueOf(c.getLine()).intValue();
                        do {
                            c.getData(buffer, 0, this_block);
                            try {
                                if (still_ok) out.write(buffer, 0, this_block);
                            } catch (IOException ioex) {
                                if (logger.CHAT) logger.chat(getName(), "getDirectory, downstream IO error - devnulling fifo");
                                still_ok = false;
                            }
                            this_block = Integer.valueOf(c.getLine()).intValue();
                        } while (this_block > 0);
                        if (still_ok) out.closeEntry();
                        file_name = c.getLine();
                    } catch (IOException ioex) {
                        c.dead();
                        throw new IOException("TSI <" + getName() + ">  getDirectory IO Error during fifo <" + file_name + ">");
                    } catch (NumberFormatException ex) {
                        c.dead();
                        throw new IOException("TSI <" + getName() + "> synchronisation problem expecting a fifo <" + file_name + "> size: " + size);
                    }
                } else {
                    try {
                        ZipEntry next_file = new ZipEntry(file_name);
                        next_file.setSize(num_bytes);
                        next_file.setExtra(m_mode);
                        if (still_ok) out.putNextEntry(next_file);
                        do {
                            int to_get = (int) (num_bytes > BUFFER_SIZE ? BUFFER_SIZE : num_bytes);
                            c.getData(buffer, 0, to_get);
                            num_bytes -= to_get;
                            try {
                                if (still_ok) out.write(buffer, 0, to_get);
                            } catch (IOException ioex) {
                                if (logger.CHAT) logger.chat(getName(), "getDirectory, downstream IO error - devnulling");
                                still_ok = false;
                            }
                        } while (num_bytes > 0);
                        if (still_ok) out.closeEntry();
                        file_name = c.getLine();
                    } catch (IOException ioex) {
                        c.dead();
                        throw new IOException("TSI <" + getName() + ">  getDirectory IO Error during file <" + file_name + ">");
                    } catch (NumberFormatException ex) {
                        c.dead();
                        throw new IOException("TSI <" + getName() + "> synchronisation problem expecting a file <" + file_name + "> size: " + size);
                    }
                }
            }
            c.getLine();
        } finally {
            c.done();
        }
        if (!still_ok) {
            throw new IOException("TSI <" + getName() + "> lost downstream connection during getDirectory");
        }
    }

    /**
	 * Copy the file from the input stream to the file on the TSI
	 *
	 **/
    public void putFile(String file_name, String mode, boolean overwrite, boolean append, InputStream data, com.fujitsu.arcon.njs.interfaces.IncarnatedUser user) throws NJSException, TSIUnavailableException {
        TSIConnection c = null;
        int action = 1;
        if (overwrite) action = 0;
        if (append) action = 3;
        if (overwrite && append) throw new NJSException("Contradiction in PutFile (both append and overwrite) <" + file_name + ">");
        String commands = "#TSI_PUTFILES\n" + "#TSI_FILESACTION " + action + "\n";
        try {
            c = getTSIConnectionFactory().getTSIConnection(user);
            String reply = c.send(commands);
            if (reply.startsWith("TSI_FAILED")) {
                throw new NJSException("Failed while establishing contact with TSI: " + reply);
            }
            if (c instanceof ClassicTSIConnection) {
                ClassicTSIConnection cc = (ClassicTSIConnection) c;
                reply = cc.sendNU(file_name + " " + mode + "\n");
                if (reply.startsWith("TSI_FAILED")) {
                    throw new NJSException("Failed sending file name and mode <" + file_name + "> to TSI: " + reply);
                }
                byte[] buffer = new byte[BUFFER_SIZE];
                int read;
                try {
                    int free_bytes = BUFFER_SIZE;
                    int offset = 0;
                    while (free_bytes > 0) {
                        int red = data.read(buffer, offset, free_bytes);
                        if (red == -1) break;
                        offset += red;
                        free_bytes -= red;
                    }
                    read = offset;
                } catch (IOException ioex) {
                    cc.sendNU("-1\n");
                    cc.sendNU("-1\n");
                    throw new NJSException("Problems reading streamed data: " + ioex);
                }
                while (read > 0) {
                    if (read > 0) {
                        reply = cc.sendNU(read + "\n");
                        if (reply.startsWith("TSI_FAILED")) {
                            throw new NJSException("Failed sending buffer size <" + file_name + "> <" + read + "> to TSI: " + reply);
                        }
                        cc.sendData(buffer, 0, read);
                    }
                    try {
                        int free_bytes = BUFFER_SIZE;
                        int offset = 0;
                        while (free_bytes > 0) {
                            int red = data.read(buffer, offset, free_bytes);
                            if (red == -1) break;
                            offset += red;
                            free_bytes -= red;
                        }
                        read = offset;
                    } catch (IOException ioex) {
                        cc.sendNU("-1\n");
                        cc.sendNU("-1\n");
                        throw new NJSException("Problems reading from streamed data: " + ioex);
                    }
                }
                reply = cc.sendNU("-1\n");
                if (reply.startsWith("TSI_FAILED")) {
                    cc.dead();
                    throw new NJSException("Failed terminating data send <" + file_name + "> to TSI: " + reply);
                }
                reply = cc.sendNU("-1\n");
                if (reply.startsWith("TSI_FAILED")) {
                    cc.dead();
                    throw new NJSException("Failed terminating file stream<" + file_name + "> to TSI: " + reply);
                }
            } else {
                reply = c.send(file_name + " " + mode + "\n");
                if (reply.startsWith("TSI_FAILED")) {
                    throw new NJSException("Failed sending file name <" + file_name + "> to TSI: " + reply);
                }
                byte[] buffer = new byte[BUFFER_SIZE];
                int read;
                try {
                    int free_bytes = BUFFER_SIZE;
                    int offset = 0;
                    while (free_bytes > 0) {
                        int red = data.read(buffer, offset, free_bytes);
                        if (red == -1) break;
                        offset += red;
                        free_bytes -= red;
                    }
                    read = offset;
                } catch (IOException ioex) {
                    c.send("-1\n");
                    c.send("-1\n");
                    throw new NJSException("Problems reading streamed data: " + ioex);
                }
                while (read > 0) {
                    if (read > 0) {
                        reply = c.send(read + "\n");
                        if (reply.startsWith("TSI_FAILED")) {
                            throw new NJSException("Failed sending buffer size <" + file_name + "> <" + read + "> to TSI: " + reply);
                        }
                        c.sendData(buffer, 0, read);
                    }
                    try {
                        int free_bytes = BUFFER_SIZE;
                        int offset = 0;
                        while (free_bytes > 0) {
                            int red = data.read(buffer, offset, free_bytes);
                            if (red == -1) break;
                            offset += red;
                            free_bytes -= red;
                        }
                        read = offset;
                    } catch (IOException ioex) {
                        c.send("-1\n");
                        c.send("-1\n");
                        throw new NJSException("Problems reading from streamed data: " + ioex);
                    }
                }
                reply = c.send("-1\n");
                if (reply.startsWith("TSI_FAILED")) {
                    throw new NJSException("Failed terminating data send <" + file_name + "> to TSI: " + reply);
                }
                reply = c.send("-1\n");
                if (reply.startsWith("TSI_FAILED")) {
                    throw new NJSException("Failed terminating file stream<" + file_name + "> to TSI: " + reply);
                }
            }
        } catch (IOException ioex) {
            throw new NJSException("Problems communications with TSI: " + ioex);
        } finally {
            if (c != null) c.done();
        }
    }

    MultiField.Deacon priorities;

    MultiField.Deacon queuep;

    private Hashtable queues;

    public Hashtable getQueues() {
        return queues;
    }

    Vector[] pqs;

    public void setUpPriorities() {
        if (queues.isEmpty()) {
            default_queue = new Queue();
            queues.put(default_queue.name, default_queue);
            logger.info("No queues defined, using a default no-name queue <" + default_queue + ">");
        }
        Enumeration e = PriorityValue.elements();
        while (e.hasMoreElements()) {
            PriorityValue pv = (PriorityValue) e.nextElement();
            int i = pv.getValue();
            priorities.setIndex(i);
            String priority = priorities.incarnate("", "", null, null);
            if (priority.length() > 0) {
                Vector v = new Vector(11);
                pqs[i] = v;
                StringTokenizer st = new StringTokenizer(priority, ", \t");
                while (st.hasMoreElements()) {
                    String q_name = st.nextToken();
                    if (queues.containsKey(q_name)) {
                        v.addElement(queues.get(q_name));
                    } else {
                        logger.severe("A Priority uses an undefined Queue <" + q_name + ">");
                        NJSGlobals.goToLimbo();
                    }
                }
            }
        }
        for (int i = pqs.length - 1; i > 0; i--) {
            if (pqs[i] != null) {
                default_queue = (Queue) pqs[i].elements().nextElement();
            }
        }
        if (default_queue == null) {
            if (queues.size() > 1) {
                logger.severe("There is more than one queue defined but no priorities!");
                NJSGlobals.goToLimbo();
            }
            default_queue = (Queue) queues.elements().nextElement();
        }
        Enumeration qs = getQueues().elements();
        int maxn = Integer.MIN_VALUE;
        int minn = Integer.MAX_VALUE;
        int maxp = Integer.MIN_VALUE;
        int minp = Integer.MAX_VALUE;
        int maxt = Integer.MIN_VALUE;
        int mint = Integer.MAX_VALUE;
        int maxm = Integer.MIN_VALUE;
        int minm = Integer.MAX_VALUE;
        while (qs.hasMoreElements()) {
            Queue q = (Queue) qs.nextElement();
            if (q.max_node > maxn) maxn = q.max_node;
            if (q.min_node < minn) minn = q.min_node;
            if (q.max_proc > maxp) maxp = q.max_proc;
            if (q.min_proc < minp) minp = q.min_proc;
            if (q.max_time > maxt) maxt = q.max_time;
            if (q.min_time < mint) mint = q.min_time;
            if (q.max_mory > maxm) maxm = q.max_mory;
            if (q.min_mory < minm) minm = q.min_mory;
        }
        default_memory = new org.unicore.resources.Memory("From TS Description", maxm, minm, minm);
        default_processor = new Processor("From TS Description", maxp, minp, minp);
        default_node = new Node("From TS Description", maxn, minn, minn);
        default_fp_time = new FloatingPoint("From TS Description", maxt, mint, mint, 1.0);
    }

    private Queue default_queue;

    public static class Reader extends TargetSystem.Reader {

        public Reader() {
            super();
            my_tokens.put("FAST_TEMP", new MissalReader() {

                public void readDefinition(Missal m, String c) {
                    fastAction(m);
                }
            });
            my_tokens.put("LARGE_TEMP", new MissalReader() {

                public void readDefinition(Missal m, String c) {
                    largeAction(m);
                }
            });
            my_tokens.put("HOME_FAST_TEMP", new MissalReader() {

                public void readDefinition(Missal m, String c) {
                    homefastAction(m);
                }
            });
            my_tokens.put("HOME_LARGE_TEMP", new MissalReader() {

                public void readDefinition(Missal m, String c) {
                    homelargeAction(m);
                }
            });
        }

        BatchTargetSystem current_system;

        public BatchTargetSystem makeNewBTS() {
            return new BatchTargetSystem();
        }

        public TargetSystem newTargetSystem() {
            current_system = makeNewBTS();
            addDefineSection("PRIORITY", current_system.priorities.getReader());
            addDefineSection("QUEUE", current_system.queuep.getReader());
            addDefineSection("OVERHEAD", current_system.system_overhead.getReader());
            addDefineSection("PER_PROCESSOR_LIMITS", new MissalReader() {

                public void readDefinition(Missal m, String c) {
                    current_system.setLimitsPerProcessor(true);
                    current_system.setLimitsPerNode(false);
                    current_system.setLimitsPerJob(false);
                }
            });
            addDefineSection("PER_NODE_LIMITS", new MissalReader() {

                public void readDefinition(Missal m, String c) {
                    current_system.setLimitsPerProcessor(false);
                    current_system.setLimitsPerNode(true);
                    current_system.setLimitsPerJob(false);
                }
            });
            addDefineSection("PER_JOB_LIMITS", new MissalReader() {

                public void readDefinition(Missal m, String c) {
                    current_system.setLimitsPerProcessor(false);
                    current_system.setLimitsPerNode(false);
                    current_system.setLimitsPerJob(true);
                }
            });
            addDefineSection("DO_NOT_SEND_EMAIL", new MissalReader() {

                public void readDefinition(Missal m, String c) {
                    current_system.setSendEmail(false);
                }
            });
            addDefineSection("EXECUTE_ALL_TASKS_DIRECTLY", new MissalReader() {

                public void readDefinition(Missal m, String c) {
                    logger.info("EXECUTE_ALL_TASKS_DIRECTLY no longer used");
                }
            });
            addDefineSection("QSTAT_XLOGIN", current_system.qstat_xlogin_r.getReader());
            fast_temp_dir = null;
            large_temp_dir = null;
            home_fast_temp_dir = null;
            home_large_temp_dir = null;
            return current_system;
        }

        Host host_being_read;

        /**
		 * MissalReader promise
		 *
		 **/
        public void readDefinition(Missal missal, String context) {
            host_being_read = new Host("Inserted before reading");
            ResourceSet r = host_being_read.getResources();
            ResourceReader.getResources().add(host_being_read);
            ResourceReader.Text text = new ResourceReader.Text(r);
            my_tokens.put(text.getToken(), text);
            ResourceReader.Numeric numeric = new ResourceReader.Numeric(r);
            my_tokens.put(numeric.getToken(), numeric);
            ResourceReader.Capacity rc = new ResourceReader.Capacity(r, ResourceReader.Capacity.CPUTIME);
            my_tokens.put(rc.getToken(), rc);
            rc = new ResourceReader.Capacity(r, ResourceReader.Capacity.DATAPROCESSING);
            my_tokens.put(rc.getToken(), rc);
            rc = new ResourceReader.Capacity(r, ResourceReader.Capacity.NETWORK);
            my_tokens.put(rc.getToken(), rc);
            rc = new ResourceReader.Capacity(r, ResourceReader.Capacity.NODE);
            my_tokens.put(rc.getToken(), rc);
            rc = new ResourceReader.Capacity(r, ResourceReader.Capacity.PROCESSOR);
            my_tokens.put(rc.getToken(), rc);
            rc = new ResourceReader.Capacity(r, ResourceReader.Capacity.MEMORY);
            my_tokens.put(rc.getToken(), rc);
            rc = new ResourceReader.Capacity(r, ResourceReader.Capacity.RUNTIME);
            my_tokens.put(rc.getToken(), rc);
            ResourceReader.Storage ss = new ResourceReader.Storage(r, ResourceReader.Storage.HOME);
            my_tokens.put(ss.getToken(), ss);
            ss = new ResourceReader.Storage(r, ResourceReader.Storage.USPACE);
            my_tokens.put(ss.getToken(), ss);
            Enumeration limit_elements = LimitTypeValue.elements();
            while (limit_elements.hasMoreElements()) {
                ResourceReader.Limit rl = new ResourceReader.Limit(r, (LimitTypeValue) limit_elements.nextElement(), ((BatchTargetSystem) new_target).limits_os_specifics);
                my_tokens.put(rl.getToken(), rl);
            }
            ResourceReader.NewStorage ns = new ResourceReader.NewStorage(r, current_system.my_resources, ((BatchTargetSystem) new_target).sub_spaces);
            my_tokens.put(ns.getToken(), ns);
            missal.changeTokens(my_tokens);
        }

        private String fast_temp_dir;

        /**
		 * Read the FAST_TEMP keyword, expect a directory (absolute) to the fast temp storage
		 * for the Uspace fast temp
		 *
		 **/
        public void fastAction(Missal missal) {
            fast_temp_dir = missal.getWordAsDirectory();
        }

        private String large_temp_dir;

        /**
		 * Read the LARGE_TEMP keyword, expect a directory (absolute) to the large temp storage
		 * for the Uspace large temp
		 *
		 **/
        public void largeAction(Missal missal) {
            large_temp_dir = missal.getWordAsDirectory();
        }

        private String home_fast_temp_dir;

        /**
		 * Read the FAST_TEMP keyword, expect a directory (absolute) to the fast temp storage
		 * for the Home fast temp
		 *
		 **/
        public void homefastAction(Missal missal) {
            home_fast_temp_dir = missal.getWordAsDirectory();
        }

        private String home_large_temp_dir;

        /**
		 * Read the LARGE_TEMP keyword, expect a directory (absolute) to the large temp storage
		 * for the Home large temp
		 *
		 **/
        public void homelargeAction(Missal missal) {
            home_large_temp_dir = missal.getWordAsDirectory();
        }

        /**
		 * Got to the end of this Task's definiton
		 *
		 **/
        public void endAction(Missal missal) {
            host_being_read.setName(current_system.getName());
            current_system.setUpPriorities();
            if (!TargetSystem.addTargetSystem(current_system)) {
                logger.severe("Problems adding an Execution TSI, check IDB");
                NJSGlobals.goToLimbo();
            }
            current_system.my_resources.add(current_system.default_memory);
            current_system.my_resources.add(current_system.default_processor);
            current_system.my_resources.add(current_system.default_node);
            current_system.my_resources.add(current_system.default_fp_time);
            Enumeration e = PriorityValue.elements();
            boolean done_one = false;
            while (e.hasMoreElements()) {
                PriorityValue pv = (PriorityValue) e.nextElement();
                if (current_system.pqs[pv.getValue()] != null) {
                    Iterator i = ((Vector) current_system.pqs[pv.getValue()]).iterator();
                    String s = "Queues: ";
                    while (i.hasNext()) {
                        s += ((Queue) i.next()).name + " ";
                    }
                    if (s.indexOf("noname") >= 0) {
                        s = "Anonymous queues";
                    }
                    Priority new_priority = new Priority(s, pv);
                    current_system.my_resources.add(new_priority);
                    done_one = true;
                }
            }
            if (!done_one) {
                Priority new_priority = new Priority("Default priority for <" + current_system.getName() + ">", current_system.getDefaultPriority());
                current_system.my_resources.add(new_priority);
            }
            {
                Home home = new Home("NJS Generated", Double.MAX_VALUE, 0.0001, 0.0, (String) null);
                if (!((BatchTargetSystem) new_target).sub_spaces.containsKey(home)) ((BatchTargetSystem) new_target).sub_spaces.put(home, "$HOME/");
                current_system.my_resources.add(home);
                if (NJSGlobals.getDefaultHomeStorage() == null) NJSGlobals.setDefaultHomeStorage(home);
                Root root = new Root("NJS Generated", Double.MAX_VALUE, 0.0001, 0.0, (String) null);
                if (!((BatchTargetSystem) new_target).sub_spaces.containsKey(root)) ((BatchTargetSystem) new_target).sub_spaces.put(root, "/");
                current_system.my_resources.add(root);
                USpace uspace = new USpace("NJS Generated", Double.MAX_VALUE, 0.0001, 0.0, (String) null);
                if (!((BatchTargetSystem) new_target).sub_spaces.containsKey(uspace)) ((BatchTargetSystem) new_target).sub_spaces.put(uspace, "");
                current_system.my_resources.add(uspace);
                if (NJSGlobals.getDefaultUspaceStorage() == null) NJSGlobals.setDefaultUspaceStorage(uspace);
                AlternativeUspace auspace = new AlternativeUspace("NJS Generated", Double.MAX_VALUE, 0.0001, 0.0, (String) null, null);
                if (!((BatchTargetSystem) new_target).sub_spaces.containsKey(auspace)) ((BatchTargetSystem) new_target).sub_spaces.put(auspace, "");
                current_system.my_resources.add(auspace);
                org.unicore.resources.Spool spool = new org.unicore.resources.Spool("NJS Generated", Double.MAX_VALUE, 0.0001, 0.0);
                if (!((BatchTargetSystem) new_target).sub_spaces.containsKey(spool)) ((BatchTargetSystem) new_target).sub_spaces.put(spool, "");
                current_system.my_resources.add(spool);
                if (NJSGlobals.getDefaultSpoolStorage() == null) NJSGlobals.setDefaultSpoolStorage(spool);
                if (home_fast_temp_dir != null) {
                    Temp fast = new Temp("BTS Default FT", Double.MAX_VALUE, 0.0001, 0.0, (String) null);
                    ((BatchTargetSystem) new_target).sub_spaces.put(fast, home_fast_temp_dir);
                    current_system.my_resources.add(fast);
                }
                if (home_large_temp_dir != null) {
                    StorageServer large = new StorageServer("BTS Default LT", Double.MAX_VALUE, 0.0001, 0.0, (String) null, "Large temp on Home");
                    ((BatchTargetSystem) new_target).sub_spaces.put(large, home_large_temp_dir);
                    current_system.my_resources.add(large);
                }
            }
            if (fast_temp_dir != null) {
                StorageServer fast = new StorageServer("BTS Default FT", Double.MAX_VALUE, 0.0001, 0.0, (String) null, "Fast temp on Uspace");
                ((BatchTargetSystem) new_target).sub_spaces.put(fast, fast_temp_dir);
                current_system.my_resources.add(fast);
            }
            if (large_temp_dir != null) {
                StorageServer large = new StorageServer("BTS Default LT", Double.MAX_VALUE, 0.0001, 0.0, (String) null, "Large temp on Uspace");
                ((BatchTargetSystem) new_target).sub_spaces.put(large, large_temp_dir);
                current_system.my_resources.add(large);
            }
            if (((BatchTargetSystem) new_target).getQstatUser() == null) {
                logger.severe("Must supply a value for QSTAT_XLOGIN in the IDB.");
                NJSGlobals.goToLimbo();
            }
            if (((BatchTargetSystem) new_target).getQstatUser().getXlogin().endsWith("root")) {
                logger.severe("QSTAT_XLOGIN in the IDB cannot be root, use a normal user");
                NJSGlobals.goToLimbo();
            }
            super.endAction(missal);
        }

        /**
		 * Add a define section to this processing i.e. where a 
		 * field gets localisation
		 *
		 **/
        public void addDefineSection(String token, MissalReader reader) {
            my_tokens.put(token, reader);
        }
    }
}

class QueueDeacon extends MultiField.Deacon {

    private BatchTargetSystem owner;

    public QueueDeacon(BatchTargetSystem owner) {
        super(new String[] { "NAME", "NODES", "TIME", "MEMORY", "PROCESSORS" }, "ResourceQueues");
        this.owner = owner;
    }

    public MissalReader getReader() {
        return new QueueReader(this, owner);
    }
}

class QueueReader extends MultiField.Reader {

    private BatchTargetSystem owner;

    private MultiField.Deacon mdeacon;

    public QueueReader(QueueDeacon p, BatchTargetSystem owner) {
        super(p);
        this.owner = owner;
        mdeacon = p;
    }

    public void endAction(Missal missal) {
        super.endAction(missal);
        Queue q = new Queue();
        mdeacon.setIndex(0);
        q.name = mdeacon.incarnate("", "", null, null);
        owner.getQueues().put(q.name, q);
        try {
            mdeacon.setIndex(1);
            String ns = mdeacon.incarnate("", "", null, null).trim();
            int i = ns.indexOf(' ');
            String s = ns.substring(0, i);
            q.min_node = Integer.valueOf(s).intValue();
            s = ns.substring(i).trim();
            q.max_node = Integer.valueOf(s).intValue();
            mdeacon.setIndex(2);
            String ts = mdeacon.incarnate("", "", null, null).trim();
            i = ts.indexOf(' ');
            s = ts.substring(0, i);
            q.min_time = Integer.valueOf(s).intValue();
            s = ts.substring(i).trim();
            q.max_time = Integer.valueOf(s).intValue();
            mdeacon.setIndex(3);
            String ms = mdeacon.incarnate("", "", null, null).trim();
            i = ms.indexOf(' ');
            s = ms.substring(0, i);
            q.min_mory = Integer.valueOf(s).intValue();
            s = ms.substring(i).trim();
            q.max_mory = Integer.valueOf(s).intValue();
            mdeacon.setIndex(4);
            String ps = mdeacon.incarnate("", "", null, null).trim();
            if (ps.length() > 0) {
                i = ps.indexOf(' ');
                s = ps.substring(0, i);
                q.min_proc = Integer.valueOf(s).intValue();
                s = ps.substring(i).trim();
                q.max_proc = Integer.valueOf(s).intValue();
            }
        } catch (Exception ex) {
            logger.severe("Error decoding Queue resource descriptions " + q.name, ex);
            NJSGlobals.goToLimbo();
        }
    }
}

class Queue {

    public String name = "noname";

    public int min_node = 0;

    public int max_node = Integer.MAX_VALUE;

    public int min_proc = 1;

    public int max_proc = Integer.MAX_VALUE;

    public int min_time = 0;

    public int max_time = Integer.MAX_VALUE;

    public int min_mory = 0;

    public int max_mory = Integer.MAX_VALUE;

    public String toString() {
        return name + " (" + min_proc + "," + max_proc + ")" + " (" + min_node + "," + max_node + ")" + " (" + min_mory + "," + max_mory + ")" + " (" + min_time + "," + max_time + ")";
    }
}
