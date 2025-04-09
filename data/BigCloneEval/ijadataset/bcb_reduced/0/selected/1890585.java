package org.dctmutils.daaf;

import java.lang.reflect.Constructor;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dctmutils.common.exception.MissingParameterException;
import org.dctmutils.daaf.exception.DaafConfigurationException;
import org.dctmutils.daaf.exception.DaafException;
import org.dctmutils.daaf.exception.DaafInterruptedException;
import org.dctmutils.daaf.method.IDaafMethod;
import org.dctmutils.daaf.object.DaafArguments;
import org.dctmutils.daaf.object.DaafMethodArguments;
import org.dctmutils.daaf.xml.Actions;
import org.dctmutils.daaf.xml.Activity;
import org.dctmutils.daaf.xml.MethodCall;
import org.dctmutils.daaf.xml.MethodDefinition;
import org.dctmutils.daaf.xml.OnError;
import com.documentum.fc.common.DfException;

/**
 * Orchestrates the invocation of the <code>IDaafMethods</code> that are
 * configured for each automatic workflow activity. It relies on the
 * <code>DaafConfig</code> to provide it with the names of and order in which
 * to call DAAF methods for a given automatic workflow activity. This approach
 * is easier to maintain than registering each automatic method with Documentum.
 * Furthermore, the automatic workflow and their sequence can be vaired without
 * having to uninstall a given workflow, the consequence of which is
 * interrupting the running workflows.
 * 
 * @author <a href="mailto:luther@dctmutils.org">Luther E. Birdzell</a>
 */
public class DaafFacade {

    /**
     * 
     */
    private static Log log = LogFactory.getLog(DaafFacade.class);

    /**
     * 
     */
    private DaafHelper helper = null;

    /**
     * 
     */
    private DaafArguments daafArgs = null;

    /**
     * Creates a new <code>DaafFacade</code> instance.
     */
    public DaafFacade() {
    }

    /**
     * Creates a new <code>DaafFacade</code> instance.
     * 
     * @param daafArgs
     *            a <code>DaafArguments</code> value
     * @exception DaafConfigurationException
     *                if an error occurs
     */
    public DaafFacade(DaafArguments daafArgs) throws DaafConfigurationException {
        try {
            this.daafArgs = daafArgs;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new DaafConfigurationException(e.getMessage(), e);
        }
    }

    /**
     * @param args
     */
    public void setDaafArguments(DaafArguments args) {
        this.daafArgs = args;
    }

    /**
     * Use reflection to call runMethod()(ActivityArgumentList args) on all
     * workflow that are configured in daaf.xml for the current activity.
     * 
     * @exception DaafException
     * @exception DaafConfigurationException
     */
    public void runMethod() throws DaafException, DaafConfigurationException {
        log.debug("runMethod(): start");
        Activity activity = null;
        String activityName = null;
        try {
            helper = new DaafHelper(daafArgs);
            DaafConfig daafConfig = DaafConfig.getInstance();
            String docbase = daafArgs.getDocbase();
            activityName = helper.getDfActivity().getObjectName();
            String currentProcessName = helper.getProcessName();
            activity = daafConfig.getActivity(docbase, activityName, currentProcessName);
            String activityProcessName = null;
            if (activity != null) {
                activityProcessName = activity.getProcessName();
                if (activityScopedForCurrentProcess(activityProcessName, currentProcessName)) {
                    log.debug("found activity configuration for " + activityName);
                    processActivity(activity, daafConfig);
                }
            } else {
                log.warn("DAAF did not find an entry in the configuration for the '" + activityName + "' activity that matched the process '" + currentProcessName + "'");
            }
            helper.completeWorkitem();
        } catch (Exception e) {
            log.error("Error processing the DAAF method(s) for workflow activity '" + activityName + "' -- " + e.getMessage(), e);
            processActivityException(activity, e);
        }
    }

    /**
     *
     *
     * @param activity
     * @param daafConfig
     */
    protected void processActivity(Activity activity, DaafConfig daafConfig) {
        if (activity == null || daafConfig == null) {
            throw new MissingParameterException("activity and daafConfig");
        }
        Actions actions = null;
        String activityName = activity.getName();
        for (int i = 0; i < activity.sizeOfActionsArray(); i++) {
            actions = activity.getActionsArray(i);
            MethodCall methodCall = null;
            int numMethods = actions.sizeOfMethodCallArray();
            log.debug("numMethods = " + numMethods);
            for (int j = 0; j < numMethods; j++) {
                try {
                    methodCall = actions.getMethodCallArray(j);
                    callRunMethod(daafConfig, methodCall);
                } catch (Exception e) {
                    String methodName = methodCall.getName();
                    log.error("Error processing the DAAF method '" + methodName + "' for workflow activity '" + activityName + "' -- " + e.getMessage(), e);
                    OnError methodOnError = methodCall.getOnError();
                    String errorAction = null;
                    MethodCall[] errorMethods = null;
                    if (methodOnError != null && methodOnError.getAction() != null) {
                        errorAction = methodOnError.getAction().toString();
                        errorMethods = methodOnError.getMethodCallArray();
                        log.info("Found errorAction = '" + errorAction);
                        if (StringUtils.equals(errorAction, OnError.Action.CONTINUE.toString()) && errorMethods != null) {
                            numMethods = errorMethods.length;
                            log.debug("numMethods = " + numMethods);
                            for (int k = 0; k < numMethods; k++) {
                                try {
                                    methodCall = errorMethods[k];
                                    callRunMethod(daafConfig, methodCall);
                                } catch (Exception ex) {
                                    log.error("Error processing the DAAF error methods for '" + methodName + "' and workflow activity '" + activityName + "' -- " + e.getMessage(), e);
                                    throw new DaafException(ex);
                                }
                            }
                            break;
                        } else if (StringUtils.equals(errorAction, OnError.Action.CONTINUE.toString())) {
                            continue;
                        } else {
                            throw new DaafException(e);
                        }
                    } else {
                        throw new DaafException(e);
                    }
                }
            }
        }
    }

    /**
     *
     *
     * @param activity
     * @param activityException
     */
    protected void processActivityException(Activity activity, Exception activityException) {
        if (activity != null) {
            String activityName = activity.getName();
            OnError activityOnError = activity.getOnError();
            String errorAction = null;
            String nextTask = null;
            if (activityOnError != null) {
                processActivityOnError(activityException, activityName, activityOnError, errorAction, nextTask);
            } else {
                helper.completeWorkitem(1, activityException.getMessage(), null);
                throw new DaafException(activityException.getMessage(), activityException);
            }
        } else {
            helper.completeWorkitem(1, activityException.getMessage(), null);
            throw new DaafException(activityException.getMessage(), activityException);
        }
    }

    /**
     *
     *
     * @param activityException
     * @param activityName
     * @param activityOnError
     * @param errorAction
     * @param nextTask
     */
    protected void processActivityOnError(Exception activityException, String activityName, OnError activityOnError, String errorAction, String nextTask) {
        if (activityOnError.getAction() != null) {
            errorAction = activityOnError.getAction().toString();
            nextTask = activityOnError.getNextTask();
            log.info("Found errorAction = '" + errorAction + "' and nextTask = '" + nextTask + "'");
        }
        if (StringUtils.equals(errorAction, OnError.Action.HALT.toString())) {
            helper.haltWorkitem();
            throw new DaafException(activityException.getMessage(), activityException);
        } else if (StringUtils.equals(errorAction, OnError.Action.CONTINUE.toString())) {
            log.info("This activity was configured to continue inspite of this error");
            if (StringUtils.isNotBlank(nextTask)) {
                try {
                    helper.setRouting(nextTask);
                } catch (DfException dfe) {
                    log.error("Unable to set the error routing to '" + nextTask + "' for the error activity '" + activityName + "' DAAF is going to try to complete the workitem anyway.  " + dfe.getMessage(), dfe);
                }
            }
            helper.completeWorkitem();
        } else {
            log.debug("This activity was configured to fail on error");
            if (StringUtils.isNotBlank(nextTask)) {
                try {
                    helper.setRouting(nextTask);
                } catch (DfException dfe) {
                    log.error("Unable to set the error routing to '" + nextTask + "' for the error activity '" + activityName + "' DAAF is going to complete the workitem with a return value of '1'.  " + dfe.getMessage(), dfe);
                }
            }
            helper.completeWorkitem(1, activityException.getMessage(), null);
        }
    }

    /**
     * This method compares the value of the Activity.'process-name' attribute with
     * the dm_process.object name of the current running workflow.
     * <br><br>
     * Activities are considered scoped for the current process if their
     * <ul>
     *   <li>'process-name' attribute is blank</li>
     *   <li>'process-name' attribute equals '*' (no quotes)</li>
     *   <li>'process-name' attribute case-insensitive equals the currentProcessName</li>
     *   <li>'process-name' Pattern.matches() case insensitive with the currentProcessName (see below)</li>
     * </ul>
     * 
     * <p>Regex Examples:<br>
     * Starts with 'DAAF': '^daaf.+' (no quotes)<br>
     * Ends with 'Workflow': '.+WORKFLOW$' (no quotes)<br>
     * Contains 'DAAF Workflow': 'Daaf.+Workflow.+' (no quotes)<br>
     * 
     * @param activityProcessName the value of the 'process-name' attribute of the &lt;activity&gt; 
     *                            associated with the current workitem.
     * @param currentProcessName dm_process.object_name of the current workflow
     * @return the boolean activityScopedForCurrentTask
     */
    public static boolean activityScopedForCurrentProcess(String activityProcessName, String currentProcessName) {
        boolean activityScopedForCurrentTask = false;
        if (StringUtils.isBlank(activityProcessName) || StringUtils.equals(activityProcessName, "*")) {
            activityScopedForCurrentTask = true;
        } else if (StringUtils.equalsIgnoreCase(currentProcessName, activityProcessName)) {
            activityScopedForCurrentTask = true;
        } else {
            Pattern regex = Pattern.compile(activityProcessName, Pattern.CASE_INSENSITIVE);
            Matcher matcher = regex.matcher(StringUtils.upperCase(currentProcessName));
            activityScopedForCurrentTask = matcher.matches();
        }
        return activityScopedForCurrentTask;
    }

    /**
     * Use reflection to call the method that was defined in the daaf
     * configuration.
     * 
     * @param daafConfig
     * @param methodCall
     * @throws DaafException
     * @throws DaafConfigurationException
     * @throws DaafInterruptedException
     */
    protected void callRunMethod(DaafConfig daafConfig, MethodCall methodCall) throws DaafException, DaafConfigurationException, DaafInterruptedException {
        if (methodCall == null) {
            return;
        }
        try {
            String methodName = StringUtils.trim(methodCall.getName());
            log.info("calling methodName: " + methodName + "...");
            DaafMethodArguments arguments = new DaafMethodArguments(methodCall.getArguments());
            String docbase = daafArgs.getDocbase();
            MethodDefinition methodDefinition = daafConfig.getMethodDefinition(docbase, methodName);
            if (methodDefinition == null) {
                throw new RuntimeException("No method defined for " + methodName);
            }
            String className = StringUtils.trim(methodDefinition.getClassName());
            log.info("...on className: " + className);
            Class methodClass = Class.forName(className);
            Class[] paramTypes = { DaafHelper.class, DaafMethodArguments.class };
            Constructor constructor = methodClass.getConstructor(paramTypes);
            Object[] args = { helper, arguments };
            IDaafMethod wfMethod = (IDaafMethod) constructor.newInstance(args);
            wfMethod.runMethod();
        } catch (DaafInterruptedException die) {
            throw die;
        } catch (DaafException de) {
            throw de;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new DaafConfigurationException(e.getMessage(), e);
        }
    }
}
