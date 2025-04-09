package org.gridbus.broker.scheduler;

import java.util.ArrayList;
import java.util.List;
import org.apache.log4j.Logger;
import org.gridbus.broker.common.ComputeServer;
import org.gridbus.broker.common.Job;
import org.gridbus.broker.common.Qos;
import org.gridbus.broker.common.Scheduler;
import org.gridbus.broker.constants.JobStatus;
import org.gridbus.broker.constants.JobType;
import org.gridbus.broker.constants.ScheduleOptimisationType;
import org.gridbus.broker.constants.ServiceType;
import org.gridbus.broker.exceptions.GridBrokerException;
import org.gridbus.broker.exceptions.QosException;
import org.gridbus.broker.util.SleepUtil;
import org.gridbus.scs.common.SCSJob;

/**
 * This is a simple economy based scheduler for the broker, which doesnot take into account remote data files.
 * @author Srikumar Venugopal (srikumar@cs.mu.oz.au)
 */
public class DBScheduler extends Scheduler {

    /**
	 * Logger for this class
	 */
    private final Logger logger = Logger.getLogger(DBScheduler.class);

    private Object oBudgetAlloc = new Object();

    private float budgetAllocated = 0;

    private int optimisation = ScheduleOptimisationType.COST_TIME;

    /**
	 * Constructor for the scheduler
	 * @param applicationID
	 * @param optimisation (one of the constants defined in ScheduleOptimisationType
	 */
    public DBScheduler(String applicationID, int optimisation) throws GridBrokerException {
        super(applicationID);
        this.optimisation = optimisation;
    }

    /**
	 * Implements the scheduling algorithm, based on cost/time/cost-time optimisation.
	 * This scheduler works only for jobs where it is not required to consider
	 * the cost of data and its transfer. It is used with jobs which do not require remote data files.
	 * @see org.gridbus.broker.farming.common.Scheduler#schedule()
	 */
    protected void schedule() throws Exception {
        try {
            float budget = 0, budgetSpent = 0, budgetLeft = 0;
            long deadline = 0, timeSpent = 0, timeLeft = 0;
            while (!finished) {
                Qos qos = store.getQos(applicationID);
                budget = qos.getBudget();
                budgetSpent = qos.getBudgetSpent();
                budgetLeft = budget - budgetSpent;
                deadline = qos.getDeadline();
                timeSpent = qos.getTimeSpent();
                timeLeft = deadline - timeSpent;
                logger.info("The optimisation is " + ScheduleOptimisationType.stringValue(optimisation));
                logger.info("schedule(): Budget = " + budget + ", Deadline = " + deadline);
                Job jNext = getNextReadyJob();
                if (jNext != null) {
                    List servers = this.getCandidateServers(jNext);
                    switch(optimisation) {
                        case ScheduleOptimisationType.COST_TIME:
                            sortByCost(servers);
                            ArrayList grpSrvLst = new ArrayList();
                            groupServers(grpSrvLst, servers);
                            for (int l = 0; l < grpSrvLst.size(); l++) {
                                ArrayList servList = (ArrayList) grpSrvLst.get(l);
                                for (int p = 0; p < servList.size(); p++) {
                                    ComputeServer server = (ComputeServer) servList.get(p);
                                    float currentAvailBudget = budgetLeft - budgetAllocated;
                                    float budgetAvailPerJob = currentAvailBudget / totalStats.getReadyJobs();
                                    if (budgetAvailPerJob < server.getPricePerJob()) continue;
                                    while (jNext != null) {
                                        budgetAllocated += server.getPricePerJob();
                                        saveJobMapping(jNext, server);
                                        jNext = getNextReadyJob();
                                    }
                                    if (jNext == null) break;
                                }
                                if (jNext == null) break;
                            }
                            break;
                        case ScheduleOptimisationType.COST:
                            sortByCost(servers);
                            if (servers != null) {
                                for (int i = 0; i < servers.size(); i++) {
                                    ComputeServer server = (ComputeServer) servers.get(i);
                                    synchronized (oBudgetAlloc) {
                                        float currentAvailBudget = budgetLeft - budgetAllocated;
                                        float budgetAvailPerJob = currentAvailBudget / totalStats.getReadyJobs();
                                        if (budgetAvailPerJob < server.getPricePerJob()) {
                                            continue;
                                        }
                                    }
                                    long jobsToSubmit = 0;
                                    if (server.getAvgJobComputationTime() != 0) {
                                        jobsToSubmit = Math.round(timeLeft / server.getAvgJobComputationTime());
                                        jobsToSubmit = (jobsToSubmit >= server.getJobLimit()) ? server.getJobLimit() : jobsToSubmit;
                                    } else {
                                        jobsToSubmit = 1;
                                    }
                                    while (jNext != null && jobsToSubmit > 0) {
                                        checkQos(qos);
                                        boolean submitSuccess = saveJobMapping(jNext, server);
                                        if (submitSuccess) {
                                            jNext = getNextReadyJob();
                                            synchronized (oBudgetAlloc) {
                                                budgetAllocated += server.getPricePerJob();
                                            }
                                            jobsToSubmit--;
                                        } else {
                                            break;
                                        }
                                    }
                                    if (jNext == null) break;
                                }
                            }
                            break;
                        case ScheduleOptimisationType.TIME:
                            sortByTime(servers);
                            if (servers != null) {
                                for (int i = 0; i < servers.size(); i++) {
                                    ComputeServer server = (ComputeServer) servers.get(i);
                                    long jobsToSubmit = 0;
                                    if (server.getPricePerJob() != 0) {
                                        jobsToSubmit = Math.round(budgetLeft / server.getPricePerJob());
                                        jobsToSubmit = (jobsToSubmit >= server.getJobLimit()) ? server.getJobLimit() : jobsToSubmit;
                                    } else {
                                        jobsToSubmit = totalStats.getReadyJobs();
                                    }
                                    while (jNext != null && jobsToSubmit > 0) {
                                        checkQos(qos);
                                        boolean submitSuccess = saveJobMapping(jNext, server);
                                        if (submitSuccess) {
                                            jNext = getNextReadyJob();
                                            synchronized (oBudgetAlloc) {
                                                budgetAllocated += server.getPricePerJob();
                                            }
                                            jobsToSubmit--;
                                        } else {
                                            break;
                                        }
                                    }
                                    if (jNext == null) break;
                                }
                            }
                            break;
                    }
                }
                updateStats();
                if (budgetLeft <= 0) {
                    logger.warn("No more budget left. Stopping scheduler.");
                    setFeasible(false);
                    break;
                }
                if (timeLeft <= 0) {
                    logger.warn("No more time left. Stopping scheduler.");
                    setFeasible(false);
                    break;
                }
                numPoll++;
                SleepUtil.sleep(pollTime);
            }
            logger.info("schedule() - Total budget spent " + budgetSpent);
            logger.info("schedule() - Total time spent: " + timeSpent);
        } catch (InterruptedException e) {
            logger.error("Scheduler thread interrupted...", e);
            this.setFailed();
        } catch (Exception e) {
            logger.error("Scheduler thread failed...", e);
            this.setFailed();
        }
    }

    private void checkQos(Qos qos) throws QosException {
        try {
            long timeLeft = qos.getDeadline() - System.currentTimeMillis();
            if (timeLeft <= 0) {
                throw new QosException("Insufficient time: deadline passed.");
            }
            float budget = qos.getBudget();
            long deadline = qos.getDeadline();
            float budgetSpent = qos.getBudgetSpent();
            synchronized (oBudgetAlloc) {
                if (budgetAllocated + budgetSpent > budget) {
                    throw new QosException("Insufficient budget to allocate." + "Current required allocation:" + budgetAllocated + ", Budget spent so far:" + budgetSpent + ", total initial Budget= " + budget);
                }
            }
        } catch (QosException qex) {
            throw qex;
        } catch (Exception ex) {
            throw new QosException("Error getting Qos.", ex);
        }
    }

    /**
	 * 
	 */
    public void statusChanged(Job job) {
        super.statusChanged(job);
        if (job != null && (job.getStatus() == JobStatus.DONE || job.getStatus() == JobStatus.FAILED)) {
            if (job.getServer() != null) {
                synchronized (oBudgetAlloc) {
                    budgetAllocated -= job.getServer().getPricePerJob();
                }
            }
            if (job.getStatus() == JobStatus.DONE) {
                try {
                    store.saveApplication(job.getApplication());
                } catch (Exception ex) {
                    logger.warn("Error updating job status : ", ex);
                }
            }
        }
    }

    /**
	 * @see org.gridbus.broker.event.JobListener#statusChanged(org.gridbus.broker.common.Job)
	 */
    public void statusChanged(SCSJob j) {
    }

    public void jobReset(Job job) {
        try {
            if (job.getType() == JobType.USER) {
                freeQueueSlot(job);
                if (job.getServer() != null) {
                    synchronized (oBudgetAlloc) {
                        budgetAllocated -= job.getServer().getPricePerJob();
                    }
                }
            }
        } catch (Exception ex) {
            logger.warn("Error free-ing queue slot , when job is reset: " + job.getName());
        }
    }
}
