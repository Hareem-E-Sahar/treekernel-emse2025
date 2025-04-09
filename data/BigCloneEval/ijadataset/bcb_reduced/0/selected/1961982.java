package student.web;

import org.zkoss.zhtml.Pre;
import org.zkoss.zhtml.Text;
import org.zkoss.zk.scripting.util.GenericInterpreter;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.ext.AfterCompose;
import org.zkoss.zul.Caption;
import org.zkoss.zul.Timer;

/**
 * A component that presents the viewable contents of a robot task on a web
 * page. It can be given a robot, or a task, or a class name, and will run any
 * that it is given.
 * <p>
 * A &lt;robot-viewer&gt; tag supports the following properties:
 * </p>
 * <dl>
 * <dt><b>Parameters</b></dt>
 * <dd><code>task</code> - The task to monitor. It can be any Java expression,
 * or any EL expression (i.e., ${...}), but may not be a bound data value (no
 * 
 * @{... ). The viewer will monitor the output of the robot returned by the 
 *       task's getRobot() method.</dd> <dd><code>taskClass</code> - Instead of
 *       a task object, you can provide a Java class name. The viewer will
 *       create its own instance of this class and then monitor it.</dd> <dd>
 *       <code>robot</code> - Instead of a task, You can provide a robot object
 *       directly. To do anything useful, the robot should implement its own
 *       {@link student.web.WebBot#run()} method. this property can be any Java
 *       expression, or any EL expression (i.e., ${...}), but may not be a bound
 *       data value (no @{...}).</dd> <dd><code>robotClass</code> - Finally,
 *       instead of providing a robot object, you can even provide a Java class
 *       name. The viewer will create its own instance of this class and then
 *       monitor its output.</dd> <dd><code>robotUrl</code> - If you provide a
 *       <code>robotClass</code>, you can use this optional property to specify
 *       a URL parameter to pass to the robot's constructor. The viewer will use
 *       then use this URL when creating its own instance of your robot class.
 *       If you do not specify a <code>robotClass</code> , this property is
 *       ignored.</dd> <dd><code>interactive</code> - If you provide a true
 *       value for this optional property, the viewer will continue to listen to
 *       the robot's output channel and update its display, even after the robot
 *       task (or robot) completes. The default is false, which means the viewer
 *       will stop refreshing itself with any output updates as soon as the task
 *       (or robot) finishes.</dd>
 *       </dl>
 *       <p>
 *       Examples:
 *       </p>
 * 
 *       <pre>
 * &lt;!-- With your class name --&gt;
 * &lt;robot-viewer taskClass="MySuperRobotTask"/&gt;
 * 
 * &lt;!-- With a custom robot --&gt;
 * &lt;robot-viewer robot="new MySuperRobot(200, &quot;http://localhost/&quot;)"/&gt;
 * 
 * &lt;!-- A longer version, so you don't have to escape the quotes. --&gt;
 * &lt;robot-viewer&gt;
 *   &lt;attribute name="robot"&gt;
 *     new MySuperRobot(200, "http://very.cool.com/a/very/long/url")
 *   &lt;/attribute&gt;
 * &lt;/variable&gt;
 * </pre>
 * 
 * @author Stephen Edwards
 * @author Last changed by $Author: vtwoods@gmail.com $
 * @version $Revision: 363 $, $Date: 2010-11-17 22:10:44 -0500 (Wed, 17 Nov
 *          2010) $
 */
public class RobotViewer extends Window implements AfterCompose {

    /**
	 * 
	 */
    private static final long serialVersionUID = 7381568500878470389L;

    /**
     * Create a RobotViewer component.
     */
    public RobotViewer() {
        super();
    }

    /**
     * Create a RobotViewer component.
     * 
     * @param title
     *            the window title to use
     * @param border
     *            either "normal" or "none"
     * @param closable
     *            is this window closable?
     */
    public RobotViewer(String title, String border, boolean closable) {
        super(title, border, closable);
    }

    /**
     * Accessor for the <code>robot</code> property.
     * 
     * @return This viewer's robot
     */
    public WebBot getRobot() {
        if (bot == null && task != null) {
            bot = task.getRobot();
        }
        return bot;
    }

    /**
     * Mutator for the <code>robot</code> property.
     * 
     * @param bot
     *            The new value
     */
    public void setRobot(WebBot bot) {
        this.bot = bot;
    }

    /**
     * Mutator for the <code>robot</code> property.
     * 
     * @param bot
     *            The new value as a string of Java code to be interpreted
     */
    public void setRobot(String bot) {
        String cmd = "student.web.WebBot " + TEMP_VAR + " = " + bot + ";";
        GenericInterpreter interpreter = (GenericInterpreter) getPage().getInterpreter(getPage().getZScriptLanguage());
        try {
            interpreter.interpret(cmd, this);
            this.bot = (WebBot) interpreter.getVariable(this, TEMP_VAR);
            interpreter.unsetVariable(this, TEMP_VAR);
        } catch (Exception e) {
            student.web.internal.Exceptions.addSimpleExceptionGrid(this, e, "In &lt;robot-viewer&gt; tag's \"robot\" attribute");
            robotErrMsg = BAD_ROBOT_MSG;
        }
    }

    /**
     * Accessor for the <code>task</code> property.
     * 
     * @return This viewer's robot task
     */
    public WebBotTask getTask() {
        return task;
    }

    /**
     * Mutator for the <code>task</code> property.
     * 
     * @param task
     *            The new value
     */
    public void setTask(WebBotTask task) {
        this.task = task;
    }

    /**
     * Mutator for the <code>task</code> property.
     * 
     * @param task
     *            The new value as a string of Java code to be interpreted
     */
    public void setTask(String task) {
        String cmd = "student.web.RobotTask " + TEMP_VAR + " = " + task + ";";
        GenericInterpreter interpreter = (GenericInterpreter) getPage().getInterpreter(getPage().getZScriptLanguage());
        try {
            interpreter.interpret(cmd, this);
            this.task = (WebBotTask) interpreter.getVariable(this, TEMP_VAR);
            interpreter.unsetVariable(this, TEMP_VAR);
        } catch (Exception e) {
            student.web.internal.Exceptions.addSimpleExceptionGrid(this, e, "In &lt;robot-viewer&gt; tag's \"robot\" attribute");
            robotErrMsg = BAD_TASK_MSG;
        }
    }

    /**
     * Accessor for the <code>robotClass</code> property.
     * 
     * @return This viewer's robot class name
     */
    public String getRobotClass() {
        return robotClass;
    }

    /**
     * Mutator for the <code>robotClass</code> property.
     * 
     * @param className
     *            The new value
     */
    public void setRobotClass(String className) {
        robotClass = className;
    }

    /**
     * Accessor for the <code>taskClass</code> property.
     * 
     * @return This viewer's task class name
     */
    public String getTaskClass() {
        return taskClass;
    }

    /**
     * Mutator for the <code>taskClass</code> property.
     * 
     * @param className
     *            The new value
     */
    public void setTaskClass(String className) {
        taskClass = className;
    }

    /**
     * Accessor for the <code>robotUrl</code> property.
     * 
     * @return This viewer's robot URL parameter
     */
    public String getRobotUrl() {
        return robotUrl;
    }

    /**
     * Mutator for the <code>robotUrl</code> property.
     * 
     * @param url
     *            The new value
     */
    public void setRobotUrl(String url) {
        robotUrl = url;
    }

    /**
     * Accessor for the <code>interactive</code> property.
     * 
     * @return This viewer's interactive setting
     */
    public boolean getInteractive() {
        return isInteractive;
    }

    /**
     * Mutator for the <code>interactive</code> property.
     * 
     * @param value
     *            The new value
     */
    public void setInteractive(boolean value) {
        isInteractive = value;
    }

    /**
     * Creates the robot and/or task if necessary, starts it running, and
     * creates the output widgets.
     */
    public void afterCompose() {
        timer = new Timer(500);
        appendChild(timer);
        timer.setRepeats(true);
        timer.addEventListener(Events.ON_TIMER, new EventListener() {

            public void onEvent(Event event) {
                updateRobotMsg();
            }
        });
        timer.start();
        if (getCaption() == null) {
            Component before = null;
            if (!getChildren().isEmpty()) {
                before = (Component) getChildren().get(0);
            }
            insertBefore(new Caption(), before);
        }
        if (!getCaption().isImageAssigned()) {
            hasImage = true;
            getCaption().setImage("~./zk/img/progress.gif");
        }
        if (getTask() == null && taskClass != null) {
            try {
                Class<?> tclass = getPage().getZScriptClass(taskClass);
                if (tclass == null) {
                    tclass = Thread.currentThread().getContextClassLoader().loadClass(taskClass);
                }
                setTask((WebBotTask) tclass.newInstance());
            } catch (Exception e) {
                student.web.internal.Exceptions.addSimpleExceptionGrid(this, e, "In &lt;robot-viewer&gt; tag, creating robot task using " + "\"taskClass\" attribute", false);
                robotErrMsg = BAD_TASK_CLASS_MSG;
            }
        } else if (getRobot() == null && robotClass != null) {
            try {
                Class<?> rclass = getPage().getZScriptClass(robotClass);
                if (rclass == null) {
                    rclass = Thread.currentThread().getContextClassLoader().loadClass(robotClass);
                } else if (robotUrl == null) {
                    setRobot((WebBot) rclass.newInstance());
                } else {
                    setRobot((WebBot) rclass.getConstructor(String.class).newInstance(robotUrl));
                }
            } catch (Exception e) {
                student.web.internal.Exceptions.addSimpleExceptionGrid(this, e, "In &lt;robot-viewer&gt; tag, creating robot using " + "\"robotClass\" attribute", false);
                robotErrMsg = BAD_ROBOT_CLASS_MSG;
            }
        }
        runner = new RobotRunner();
        runner.start();
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
        }
        updateRobotMsg();
    }

    /**
     * Update the text output with the current output history of the robot.
     */
    public void updateRobotMsg() {
        if (robotMsg != null) {
            removeChild(robotMsg);
            robotMsg = null;
        }
        if (somethingBad != null) {
            student.web.internal.Exceptions.addSimpleExceptionGrid(this, somethingBad, exceptionMessage);
        }
        WebBot thisBot = getRobot();
        if (thisBot != null) {
            if (thisBot.outputIsHtml()) {
                robotMsg = new Text(thisBot.out().getHistory());
            } else {
                robotMsg = new Pre();
                robotMsg.appendChild(new Text(student.web.internal.Exceptions.escapeHtml(thisBot.out().getHistory())));
            }
        } else if (robotErrMsg != null || (!isInteractive && (runner == null || !runner.isAlive()))) {
            if (robotErrMsg == null) {
                robotErrMsg = NO_ROBOT_MSG;
            }
            robotMsg = new Text(robotErrMsg);
        }
        if (robotMsg != null) {
            appendChild(robotMsg);
        }
        if (!isInteractive && (runner == null || !runner.isAlive())) {
            timer.setRepeats(false);
            timer.stop();
            if (hasImage) {
                getCaption().setImage("~./img/robot.gif");
            }
        }
    }

    private class RobotRunner extends Thread {

        public void run() {
            if (getTask() != null) {
                try {
                    getTask().task();
                    setRobot(getTask().getRobot());
                } catch (Exception e) {
                    somethingBad = e;
                    exceptionMessage = "In &lt;robot-viewer&gt; tag, invoking " + "robot task's task() method";
                }
            } else if (getRobot() != null) {
                try {
                    getRobot().run();
                } catch (Exception e) {
                    somethingBad = e;
                    exceptionMessage = "In &lt;robot-viewer&gt; tag, invoking " + "robot's run() method";
                }
            }
            if (getRobot() != null) {
                getRobot().releaseCachedResources();
            }
            runner = null;
        }
    }

    private WebBot bot;

    private WebBotTask task;

    private String robotClass;

    private String taskClass;

    private String robotUrl;

    private Component robotMsg;

    private String robotErrMsg;

    private Thread runner;

    private Timer timer;

    private boolean hasImage;

    private boolean isInteractive;

    private Exception somethingBad;

    private String exceptionMessage;

    private static final String NO_ROBOT_MSG = "<p><b>No robot created</b>.  Either set the " + "<code>robot</code> property on this component, or create " + "your own RobotViewer subclass and override its run() " + "method.</p>";

    private static final String BAD_ROBOT_CLASS_MSG = "<p><b>No robot created</b>.  The <code>robotClass</code> property on " + "this component was set incorrectly, so the robot could not be " + "created.</p>";

    private static final String BAD_TASK_CLASS_MSG = "<p><b>No task created</b>.  The <code>taskClass</code> property on " + "this component was set incorrectly, so the task could not be " + "created.</p>";

    private static final String BAD_ROBOT_MSG = "<p><b>No robot available</b>.  An error was produced when " + "interpreting the <code>robot</code> property on " + "this component.  Please fix the value of this property.</p>";

    private static final String BAD_TASK_MSG = "<p><b>No task available</b>.  An error was produced when " + "interpreting the <code>task</code> property on " + "this component.  Please fix the value of this property.</p>";

    private static final String TEMP_VAR = "__RobotViewerTemp";
}
