package pubweb.supernode;

import static padrmi.Server.getDefaultServer;
import static pubweb.supernode.Supernode.debug;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.Map.Entry;
import padrmi.Receiver;
import padrmi.Server;
import padrmi.exception.PpAuthorizationException;
import padrmi.exception.PpException;
import pubweb.IntegrityException;
import pubweb.InternalException;
import pubweb.Job;
import pubweb.JobProcess;
import pubweb.MigrationInProgressException;
import pubweb.NotEnoughWorkersException;
import pubweb.service.Consumer2Supernode;
import pubweb.service.LbGui2Supernode;
import pubweb.service.Supernode2Consumer;
import pubweb.service.Supernode2Worker;
import pubweb.service.Worker2Supernode;
import pubweb.supernode.sched.DynamicParameterContainer;
import pubweb.supernode.sched.RequirementsContainer;
import pubweb.supernode.sched.Scheduler;
import pubweb.supernode.sched.SchedulerListener;
import pubweb.supernode.sched.StaticParameterContainer;
import pubweb.supernode.sched.simple.SimpleScheduler;

public class Peer2SupernodeImpl implements Consumer2Supernode, LbGui2Supernode, Worker2Supernode, SchedulerListener {

    private static class MigrationSourceDestinationContainer {

        public String source;

        public String destination;

        public MigrationSourceDestinationContainer(String source, String destination) {
            this.source = source;
            this.destination = destination;
        }

        public boolean equals(Object o) {
            if (!(o instanceof MigrationSourceDestinationContainer)) {
                return false;
            } else {
                return source.equals(((MigrationSourceDestinationContainer) o).source) && destination.equals(((MigrationSourceDestinationContainer) o).destination);
            }
        }

        public int hashCode() {
            return source.hashCode() + destination.hashCode();
        }
    }

    private long nextJobID = 0L;

    private Hashtable<String, PeerMetaData> consumers = new Hashtable<String, PeerMetaData>();

    private Hashtable<String, PeerMetaData> workers = new Hashtable<String, PeerMetaData>();

    private Hashtable<Long, JobMetaData> jobs = new Hashtable<Long, JobMetaData>();

    private Hashtable<String, UserMetaData> users = new Hashtable<String, UserMetaData>();

    private Scheduler scheduler;

    private Set<MigrationSourceDestinationContainer> unspecifiedMigrations = new HashSet<MigrationSourceDestinationContainer>();

    public Peer2SupernodeImpl() throws IOException {
        Properties properties = new Properties();
        properties.load(new FileInputStream(System.getProperty("pubweb.userdb")));
        for (Entry<Object, Object> entry : properties.entrySet()) {
            String v = (String) entry.getValue();
            users.put((String) entry.getKey(), new UserMetaData(v.substring(0, v.length() - 2), v.charAt(v.length() - 1) == 'x'));
        }
        try {
            @SuppressWarnings("unchecked") Class<Scheduler> c = (Class<Scheduler>) Class.forName(System.getProperty("pubweb.sched"));
            scheduler = (Scheduler) c.getConstructor(SchedulerListener.class).newInstance(this);
        } catch (Throwable t) {
            System.err.println("warning: could not load scheduler -- using default implementation:");
            t.printStackTrace();
            scheduler = new SimpleScheduler(this);
        }
    }

    public synchronized void expungeDeadPeers(long expirationTime) {
        long time = System.currentTimeMillis() - expirationTime;
        Iterator<String> it = workers.keySet().iterator();
        while (it.hasNext()) {
            String guid = it.next();
            PeerMetaData pm = workers.get(guid);
            if (pm.latestReportBack < time) {
                it.remove();
                try {
                    scheduler.workerLeft(guid);
                } catch (InternalException e) {
                    System.err.println("error in scheduler in workerLeft() call:");
                    e.printStackTrace();
                }
            }
        }
        if (debug) {
            System.out.println("deleting dead peers; peers now in list:");
            for (PeerMetaData pm : workers.values()) {
                System.out.println(pm.host + ":" + pm.port);
            }
        }
    }

    public synchronized void login() throws PpAuthorizationException {
        verifyUserIdentity();
        String guid = Receiver.getGUID();
        if (consumers.containsKey(guid)) {
            consumers.get(guid).update();
        } else {
            consumers.put(guid, new PeerMetaData(Receiver.getHost(), Receiver.getPort(), Receiver.getUserName()));
        }
    }

    public synchronized void login(StaticParameterContainer staticParams, DynamicParameterContainer dynamicParams) throws PpAuthorizationException, InternalException {
        verifyUserIdentity();
        String guid = Receiver.getGUID();
        if (workers.containsKey(guid)) {
            doReportBack(guid, dynamicParams);
        } else {
            PeerMetaData p = new PeerMetaData(Receiver.getHost(), Receiver.getPort(), Receiver.getUserName());
            p.staticParams = staticParams;
            workers.put(guid, p);
            scheduler.workerJoined(guid, staticParams, dynamicParams);
        }
    }

    public synchronized void reportBack(DynamicParameterContainer dynamicParams) throws PpAuthorizationException, IntegrityException, InternalException {
        verifyUserIdentity();
        String guid = Receiver.getGUID();
        if (workers.containsKey(guid)) {
            doReportBack(guid, dynamicParams);
        } else {
            throw new IntegrityException("there is no such worker logged in");
        }
    }

    private void doReportBack(String guid, DynamicParameterContainer dynamicParams) throws InternalException {
        PeerMetaData p = workers.get(guid);
        boolean wasDenounced = p.denounced;
        p.update();
        if (wasDenounced) {
            scheduler.workerJoined(guid, p.staticParams, dynamicParams);
        } else {
            scheduler.workerLoadChanged(guid, dynamicParams);
        }
    }

    public synchronized void logout() throws PpAuthorizationException, InternalException {
        verifyUserIdentity();
        String guid = Receiver.getGUID();
        PeerMetaData pm = workers.remove(guid);
        if (pm != null) {
            if (!pm.denounced) {
                scheduler.workerLeft(guid);
            }
        } else {
            consumers.remove(guid);
        }
    }

    public void ping() {
    }

    public synchronized URL getConsumerUrl(String guid) throws IntegrityException, MalformedURLException {
        if (guid == null || !consumers.containsKey(guid)) {
            throw new IntegrityException("no such guid: " + guid);
        }
        PeerMetaData c = consumers.get(guid);
        return new URL(Server.PROTOCOL, c.host, c.port, "/" + guid);
    }

    public synchronized URL getWorkerUrl(String guid) throws IntegrityException, MalformedURLException {
        if (guid == null || !workers.containsKey(guid)) {
            throw new IntegrityException("no such guid: " + guid);
        }
        PeerMetaData p = workers.get(guid);
        return new URL(Server.PROTOCOL, p.host, p.port, "/" + guid);
    }

    public synchronized String locateProcess(Job job, int pid) throws PpAuthorizationException, IntegrityException, InternalException {
        verifyUserIdentity();
        return scheduler.locateJobProcess(job, pid).getWorker();
    }

    public synchronized void reportDeadWorker(String guid) throws PpException, InternalException {
        verifyUserIdentity();
        PeerMetaData pm = workers.get(guid);
        if (pm != null && !pm.denounced) {
            pm.denounced = true;
            scheduler.workerLeft(guid);
        }
    }

    public synchronized Job newJob(String description, int nProcs, RequirementsContainer reqs) throws PpAuthorizationException, InternalException, NotEnoughWorkersException {
        verifyUserIdentity();
        verifyUserPrivileges();
        Job job = new Job(nextJobID++, Receiver.getUserName(), description, nProcs, Receiver.getGUID());
        jobs.put(job.getId(), new JobMetaData(job));
        try {
            scheduler.newJob(job, reqs);
        } catch (NotEnoughWorkersException ne) {
            jobs.remove(job.getId());
            throw ne;
        }
        return job;
    }

    public synchronized JobProcess[] getWorkersAttachedToJob(Job job) throws PpAuthorizationException, IntegrityException, InternalException {
        verifyUserIdentity();
        return scheduler.locateJob(job);
    }

    public void killJob(Job job) throws PpAuthorizationException, IntegrityException, InternalException {
        JobMetaData j;
        JobProcess[] procs;
        synchronized (this) {
            verifyUserIdentity();
            j = jobs.get(job.getId());
            if (j == null) {
                throw new IntegrityException("no such job");
            }
            procs = scheduler.locateJob(job);
            jobs.remove(job.getId());
            scheduler.jobDied(job);
        }
        if (j.started) {
            killProcessServersWorkingOnJob(j, procs);
            informWorkersAboutJobExit(j);
        }
    }

    public synchronized List<Job> getAllActiveJobs() throws PpAuthorizationException {
        verifyUserIdentity();
        String user = Receiver.getUserName();
        List<Job> list = new LinkedList<Job>();
        for (JobMetaData t : jobs.values()) {
            if (t.job.getUser().equals(user)) {
                list.add(t.job);
            }
        }
        return list;
    }

    public synchronized void requestJobExitNotification(Job job) throws PpAuthorizationException, IntegrityException {
        verifyUserIdentity();
        JobMetaData t = jobs.get(job.getId());
        if (t == null) {
            throw new IntegrityException("no such job");
        }
        t.exitNotifications.add(Receiver.getGUID());
    }

    public synchronized boolean deadProcessAssumed(JobProcess process, int restoringPID) throws PpAuthorizationException, IntegrityException {
        verifyUserIdentity();
        JobMetaData t = jobs.get(process.getJob().getId());
        if (t == null) {
            throw new IntegrityException("no such job");
        }
        JobProcessMetaData tp = t.processes.get(process.getPid());
        if (tp.restorationInProgress) {
            tp.restorationNotifications.add(restoringPID);
            return false;
        } else {
            tp.restorationInProgress = true;
            tp.restorationChecking = true;
            tp.restoringPid = restoringPID;
            return true;
        }
    }

    public void deadProcessVerified(JobProcess process, int restoringPID, boolean reallyDead) throws PpAuthorizationException, IntegrityException, InternalException, NotEnoughWorkersException {
        Map<Integer, String> notifications = null;
        synchronized (this) {
            verifyUserIdentity();
            JobMetaData t = jobs.get(process.getJob().getId());
            if (t == null) {
                throw new IntegrityException("no such job");
            }
            JobProcessMetaData tp = t.processes.get(process.getPid());
            if (tp.restoringPid != restoringPID) {
                throw new IntegrityException("this is not the restoring PID of the specified process");
            }
            if (reallyDead) {
                tp.restorationChecking = false;
                PeerMetaData pm = workers.get(process.getWorker());
                if (pm != null && !pm.denounced) {
                    pm.denounced = true;
                    scheduler.workerLeft(process.getWorker());
                }
            } else {
                tp.restorationInProgress = false;
                notifications = new HashMap<Integer, String>();
                JobProcess[] procs = scheduler.locateJob(process.getJob());
                for (Integer pid : tp.restorationNotifications) {
                    if (procs[pid] != null) {
                        notifications.put(pid, procs[pid].getWorker());
                    }
                }
                tp.restorationNotifications.clear();
            }
        }
        if (notifications != null) {
            informProcessesAboutProcessRestoration(process, notifications);
        }
    }

    public void deadProcessRestored(JobProcess process, int restoringPID, int superstep) throws PpAuthorizationException, IntegrityException, InternalException {
        Map<Integer, String> notifications;
        synchronized (this) {
            verifyUserIdentity();
            JobMetaData t = jobs.get(process.getJob().getId());
            if (t == null) {
                throw new IntegrityException("no such job");
            }
            JobProcessMetaData tp = t.processes.get(process.getPid());
            if (tp.restoringPid != restoringPID) {
                throw new IntegrityException("this is not the restoring PID of the specified process");
            }
            tp.restorationInProgress = false;
            notifications = new HashMap<Integer, String>();
            JobProcess[] procs = scheduler.locateJob(process.getJob());
            for (Integer pid : tp.restorationNotifications) {
                if (procs[pid] != null) {
                    notifications.put(pid, procs[pid].getWorker());
                }
            }
            tp.restorationNotifications.clear();
        }
        informProcessesAboutProcessRestoration(process, notifications);
    }

    public synchronized Set<String> getPreviouslyAttachedWorkers(Job job) throws PpAuthorizationException, IntegrityException {
        verifyUserIdentity();
        JobMetaData t = jobs.get(job.getId());
        if (t == null) {
            throw new IntegrityException("no such job");
        }
        return t.previousHostingPeers;
    }

    public void processExited(JobProcess process) throws PpAuthorizationException, IntegrityException, InternalException {
        JobMetaData t;
        boolean notify = false;
        synchronized (this) {
            verifyUserIdentity();
            t = jobs.get(process.getJob().getId());
            if (t == null) {
                throw new IntegrityException("no such job");
            }
            t.processes.remove(process.getPid());
            if (t.processes.isEmpty()) {
                jobs.remove(process.getJob().getId());
                notify = true;
            }
            scheduler.processFinished(process);
        }
        if (notify) {
            informWorkersAboutJobExit(t);
        }
    }

    public synchronized void migrationAborted(JobProcess process) throws PpAuthorizationException, IntegrityException {
        verifyUserIdentity();
        JobMetaData t = jobs.get(process.getJob().getId());
        if (t == null) {
            throw new IntegrityException("no such job");
        }
        JobProcessMetaData tp = t.processes.get(process.getPid());
        if (!tp.migrationInProgress) {
            throw new IntegrityException("no such migrating process");
        }
        tp.migrationInProgress = false;
    }

    public synchronized void migrationDestinationReached(JobProcess process) throws PpAuthorizationException, IntegrityException, InternalException {
        verifyUserIdentity();
        JobMetaData t = jobs.get(process.getJob().getId());
        if (t == null) {
            throw new IntegrityException("no such job");
        }
        JobProcessMetaData tp = t.processes.get(process.getPid());
        if (tp.migrationInProgress) {
            if (!Receiver.getGUID().equals(tp.migrationDestination)) {
                throw new IntegrityException("this was not the migration destination");
            }
            tp.migrationInProgress = false;
        } else {
            if (!unspecifiedMigrations.remove(new MigrationSourceDestinationContainer(process.getWorker(), Receiver.getGUID()))) {
                throw new IntegrityException("no such migrating process");
            }
        }
        if (debug) {
            System.out.println("process " + process.getPid() + " of program " + process.getJob().getDescription() + " has migrated from " + process.getWorker() + " to " + Receiver.getGUID());
        }
        String srcGuid = process.getWorker();
        process.setWorker(Receiver.getGUID());
        t.previousHostingPeers.add(srcGuid);
        scheduler.processMigrated(process, srcGuid);
    }

    public void startProcesses(JobProcess[] procs) throws PpException, IntegrityException, MalformedURLException {
        synchronized (this) {
            JobMetaData j = jobs.get(procs[0].getJob().getId());
            if (j == null) {
                throw new IntegrityException("no such job");
            }
            j.started = true;
        }
        try {
            Supernode2Consumer consumer = (Supernode2Consumer) getDefaultServer().getProxyFactory().createProxy(new URL(getConsumerUrl(procs[0].getJob().getConsumerPeer()) + "/" + Supernode2Consumer.class.getSimpleName()), Supernode2Consumer.class);
            consumer.jobStarted(procs);
        } catch (Exception e) {
            synchronized (this) {
                jobs.remove(procs[0].getJob().getId());
            }
            if (e instanceof PpException) {
                throw (PpException) e;
            } else if (e instanceof IntegrityException) {
                throw (IntegrityException) e;
            } else if (e instanceof MalformedURLException) {
                throw (MalformedURLException) e;
            } else {
                throw new RuntimeException("unexpected exception informing user that job is started", e);
            }
        }
    }

    public void migrateProcess(String srcGuid, String dstGuid, JobProcess process) throws IntegrityException, MalformedURLException, MigrationInProgressException, PpException {
        synchronized (this) {
            if (process == null) {
                unspecifiedMigrations.add(new MigrationSourceDestinationContainer(srcGuid, dstGuid));
            } else {
                JobMetaData t = jobs.get(process.getJob().getId());
                if (t == null) {
                    throw new IntegrityException("no such job");
                }
                JobProcessMetaData tp = t.processes.get(process.getPid());
                if (tp.migrationInProgress) {
                    throw new MigrationInProgressException("there is already a migration in progress to " + tp.migrationDestination);
                }
                tp.migrationInProgress = true;
                tp.migrationDestination = dstGuid;
            }
        }
        Supernode2Worker worker = (Supernode2Worker) getDefaultServer().getProxyFactory().createProxy(new URL(getWorkerUrl(srcGuid) + "/" + Supernode2Worker.class.getSimpleName()), Supernode2Worker.class);
        worker.migrateProcess(dstGuid, process);
    }

    public void killProcess(JobProcess process) throws IntegrityException, MalformedURLException, PpException {
        Supernode2Worker worker = (Supernode2Worker) getDefaultServer().getProxyFactory().createProxy(new URL(getWorkerUrl(process.getWorker()) + "/" + Supernode2Worker.class.getSimpleName()), Supernode2Worker.class);
        worker.prepareCancelExecution(process);
        worker.cancelExecution(process);
    }

    public synchronized Object getScheduleSnapshot() throws PpException {
        return scheduler.getScheduleSnapshot();
    }

    private void verifyUserIdentity() throws PpAuthorizationException {
        String login = Receiver.getUserName();
        if (login == null || !users.containsKey(login)) {
            throw new PpAuthorizationException("no such user");
        }
        if (!users.get(login).password.equals(Receiver.getPassword())) {
            throw new PpAuthorizationException("wrong password");
        }
    }

    private void verifyUserPrivileges() throws PpAuthorizationException {
        if (!users.get(Receiver.getUserName()).mayExecute) {
            throw new PpAuthorizationException("user " + Receiver.getUserName() + " does not have the required privileges");
        }
    }

    private void killProcessServersWorkingOnJob(JobMetaData t, JobProcess[] procs) throws IntegrityException, InternalException {
        for (JobProcess p : procs) {
            if (p != null) {
                try {
                    Supernode2Worker worker = (Supernode2Worker) getDefaultServer().getProxyFactory().createProxy(new URL(getWorkerUrl(p.getWorker()) + "/" + Supernode2Worker.class.getSimpleName()), Supernode2Worker.class);
                    worker.killProcesses(t.job);
                } catch (Exception e) {
                    System.err.println("could not kill job server on worker " + p.getWorker());
                    if (debug) {
                        e.printStackTrace();
                    }
                }
            }
        }
        for (JobProcessMetaData tp : t.processes.values()) {
            if (tp.migrationInProgress && tp.migrationDestination != null) {
                try {
                    Supernode2Worker worker = (Supernode2Worker) getDefaultServer().getProxyFactory().createProxy(new URL(getWorkerUrl(tp.migrationDestination) + "/" + Supernode2Worker.class.getSimpleName()), Supernode2Worker.class);
                    worker.killProcesses(t.job);
                } catch (Exception e) {
                    System.err.println("could not kill job server on worker " + tp.migrationDestination);
                    if (debug) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    private void informWorkersAboutJobExit(JobMetaData t) {
        for (String s : t.exitNotifications) {
            try {
                Supernode2Worker worker = (Supernode2Worker) getDefaultServer().getProxyFactory().createProxy(new URL(getWorkerUrl(s) + "/" + Supernode2Worker.class.getSimpleName()), Supernode2Worker.class);
                worker.jobExited(t.job);
            } catch (Exception e) {
                System.err.println("could not inform worker " + s + " about job exit");
                if (debug) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void informProcessesAboutProcessRestoration(JobProcess process, Map<Integer, String> pidsToNotify) throws IntegrityException, InternalException {
        for (Integer pid : pidsToNotify.keySet()) {
            String guid = pidsToNotify.get(pid);
            try {
                Supernode2Worker worker = (Supernode2Worker) getDefaultServer().getProxyFactory().createProxy(new URL(getWorkerUrl(guid) + "/" + Supernode2Worker.class.getSimpleName()), Supernode2Worker.class);
                worker.deadProcessRestored(new JobProcess(process.getJob(), pid, guid), process.getPid());
            } catch (Exception e) {
                System.err.println("could not inform worker " + guid + " about process restoration");
                if (debug) {
                    e.printStackTrace();
                }
            }
        }
    }
}
