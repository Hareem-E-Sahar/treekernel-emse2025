package com.dsc.test;

import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import javax.persistence.Column;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.DiscriminatorType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import org.hibernate.Session;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import util.HibernateUtil;
import com.dsc.test.data.TestRunData;
import com.dsc.test.util.Message;
import com.dsc.test.util.ResourcePool;
import com.dsc.test.util.XMLHelper;

@Entity
@Table(name = "TEST_CASE")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "type", discriminatorType = DiscriminatorType.STRING)
public abstract class Case {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "test_case_sk")
    int uniqueID;

    public Case() {
        super();
        rnd = new Random();
    }

    private Timestamp start_date;

    private long executionTimeNano;

    private transient Throwable exception, cancelException;

    private final transient Random rnd;

    private transient int seed = 0;

    private static final String POOL = "POOL";

    public String getException_Txt() {
        return this.exception.getMessage();
    }

    public void setException_Txt(String msg) {
    }

    public String getCancel_Exception_Txt() {
        return this.cancelException.getMessage();
    }

    public void setCancel_Exception_Txt(String msg) {
    }

    private int timeout;

    @Column(nullable = true)
    private int cycle;

    protected Integer resultSize;

    private transient Object input;

    private transient String expectedOutput;

    protected transient Integer resultHash;

    @Column(length = 2147483647)
    private String code;

    private transient Node parameters;

    private transient Monitor monitor;

    private transient Map resources;

    private String case_id;

    private int seq;

    private boolean simulate;

    private transient TestRun testRunner;

    private java.sql.Timestamp finish_date;

    private transient Node poolParameters;

    protected transient boolean storeResultHash;

    private String description;

    private boolean success;

    private String error_message;

    public transient int parentExecutionId;

    transient boolean ignoreErrors;

    private transient int maxRestTime;

    private int rest_time_taken;

    @ManyToOne
    @JoinColumn(name = "test_run_sk")
    public TestRunData getParent() {
        return this.testRunner.getData();
    }

    public boolean isSimulation() {
        return this.simulate;
    }

    public void setParent(TestRunData td) {
        this.testRunner = new TestRun(td);
    }

    protected String getCode() {
        return this.code;
    }

    public Timestamp getStartDate() {
        return this.start_date;
    }

    protected void complete() throws Exception {
    }

    public void setStartDate(Timestamp startDate) {
        this.start_date = startDate;
    }

    public Case(Node n, int seed, int cycle, boolean simulate, Map map) throws Exception {
        this.rnd = new Random(seed);
        this.seed = rnd.nextInt();
        this.cycle = cycle;
        this.resources = map;
        this.simulate = simulate;
        NamedNodeMap nmAttrs = n.getAttributes();
        this.ignoreErrors = XMLHelper.getAttributeAsBoolean(nmAttrs, "ignoreerrors", false);
        this.case_id = XMLHelper.getAttributeAsString(nmAttrs, "id", null);
        this.seq = XMLHelper.getAttributeAsInt(nmAttrs, "seq", -1);
        this.maxRestTime = XMLHelper.getAttributeAsInt(nmAttrs, "resttime", -1);
        this.timeout = XMLHelper.getAttributeAsInt(nmAttrs, "timeout", 0);
        this.description = XMLHelper.getAttributeAsString(nmAttrs, "description", this.case_id + "[" + this.seq + "]");
        this.storeResultHash = XMLHelper.getAttributeAsBoolean(nmAttrs, "resulthash", false);
        this.input = XMLHelper.getAttributeAsString(nmAttrs, "input", null);
        this.expectedOutput = XMLHelper.getAttributeAsString(nmAttrs, "expectedoutput", null);
        String parametersId = XMLHelper.getAttributeAsString(nmAttrs, "parameters", XMLHelper.getAttributeAsString(n.getParentNode().getAttributes(), "parameters", null));
        this.parameters = XMLHelper.findElementByName(n, "parameters", "id", parametersId);
        this.code = XMLHelper.getTextContent(n);
        String pool = this.getParameter(POOL, false);
        if (pool != null) {
            String[] lists = XMLHelper.getSubParameterListNames(n, pool, "*");
            if (lists == null || lists.length == 0) throw new Exception("Sub paremeter list '" + pool + "', could not be found.");
            pool = lists[Math.abs(seed % lists.length)];
            this.poolParameters = XMLHelper.findElementByName(n, "parameters", "id", pool);
        }
        if (this.code == null) throw new Exception("Code is missing from tag");
        this.code = resolveParameters(this.code);
    }

    transient Map<String, String> randomParams = new HashMap();

    private String resolveParameters(String code) throws ParseException, Exception {
        String[] strParms;
        while ((strParms = XMLHelper.getParametersFromText(code)).length > 0) {
            for (String element : strParms) {
                String parmValue = null;
                if (element.startsWith("randomDate(")) {
                    SimpleDateFormat sdf = new SimpleDateFormat();
                    sdf.applyPattern("yyyyMMdd");
                    String res = randomParams.get(element);
                    if (res != null) parmValue = res; else {
                        String parts[] = element.split("\\(");
                        String[] opts = parts[1].substring(0, parts[1].length() - 1).split(",");
                        Long minValue = sdf.parse(opts[0].trim()).getTime();
                        Long maxValue = sdf.parse(opts[1].trim()).getTime();
                        double range = maxValue - minValue;
                        long offSet = (long) (this.rnd.nextDouble() * range);
                        parmValue = sdf.format(new Date(minValue + offSet));
                        randomParams.put(element, parmValue);
                    }
                } else if (element.startsWith("random(")) {
                    String res = randomParams.get(element);
                    if (res != null) parmValue = res; else {
                        String parts[] = element.split("\\(");
                        String[] opts = parts[1].substring(0, parts[1].length() - 1).split(",");
                        int minValue = Integer.parseInt(opts[0].trim());
                        int maxValue = Integer.parseInt(opts[1].trim());
                        int maxRnd = maxValue - minValue;
                        parmValue = Integer.toString(this.rnd.nextInt(maxRnd) + minValue);
                        randomParams.put(element, parmValue);
                    }
                } else if (element.equalsIgnoreCase("seed")) parmValue = Integer.toString(this.rnd.nextInt(100)); else if (element.equalsIgnoreCase("execution_id")) {
                    parmValue = Integer.toString(this.parentExecutionId);
                } else parmValue = this.getParameter(element, true);
                if (parmValue != null) {
                    code = XMLHelper.replaceParameter(code, element, parmValue);
                } else {
                    throw new Exception("Parameter " + element + " can not be found in parameters");
                }
            }
        }
        return code;
    }

    protected final Object getResource(String name) {
        return this.resources.get(name);
    }

    protected final void setResource(String name, Object resource) {
        this.resources.put(name, resource);
    }

    public final String getParameter(String name, boolean required) throws ParseException, Exception {
        if (this.poolParameters != null) {
            Node n = XMLHelper.getElementByName(this.poolParameters, "parameter", "id", name);
            if (n != null) return XMLHelper.getTextContent(n);
        }
        Node n = XMLHelper.getElementByName(this.parameters, "parameter", "id", name);
        if (n == null) {
            if (required) throw new RuntimeException(this.getName() + ": Parameter " + name + " not found"); else return null;
        }
        return this.resolveParameters(XMLHelper.getTextContent(n));
    }

    public final void logInfo(String message) {
        ResourcePool.LogMessage(Message.INFO, message);
    }

    protected final void logWarning(String message) {
        ResourcePool.LogMessage(Message.WARNING, message);
    }

    protected final void logError(String message) {
        ResourcePool.LogMessage(Message.ERROR, message);
    }

    protected abstract void run() throws Exception;

    public final long getExecutionTime() {
        return this.executionTimeNano / (long) Math.pow(10, 6);
    }

    public final void execute() {
        this.monitor = this.createMonitor();
        try {
            this.monitor.start();
            this.start_date = new java.sql.Timestamp(System.currentTimeMillis());
            long startTimeNano = System.nanoTime();
            try {
                if (simulate) {
                    String code = this.getCode();
                    code = null;
                    Thread.sleep(0, this.rnd.nextInt(500));
                } else this.run();
            } catch (Throwable e) {
                this.recordException(e);
            }
            this.executionTimeNano = System.nanoTime() - startTimeNano;
            this.finish_date = new java.sql.Timestamp(System.currentTimeMillis());
            this.success = this.isSuccess();
            if (this.maxRestTime > 0) {
                this.rest_time_taken = this.rnd.nextInt(this.maxRestTime) * 1000;
                try {
                    Thread.sleep(this.rest_time_taken);
                } catch (InterruptedException e) {
                    this.recordException(e);
                }
            }
            if (this.isSuccess() == false) {
                this.error_message = this.getException_Txt();
                this.logError("Test case failed: " + this.error_message + "\nCheck Code\n\n" + this.getCode() + "\n\n");
            }
        } finally {
            this.monitor.setAlive(false);
        }
    }

    public abstract void cancel() throws Exception;

    private Monitor createMonitor() {
        return new Monitor(this);
    }

    public final boolean isSuccess() {
        return this.exception == null;
    }

    public final void recordException(Throwable e) {
        this.exception = e;
    }

    public final long getExecutionTimeNano() {
        return this.executionTimeNano;
    }

    public void cancelFailed(Exception e) {
        this.cancelException = e;
    }

    public boolean timedOut() {
        if (this.timeout <= 0) return false;
        return ((System.currentTimeMillis() - this.start_date.getTime()) / 1000) > this.timeout;
    }

    public String getName() {
        return this.case_id;
    }

    public void createAndStore() {
        Session session = HibernateUtil.getSession();
        session.beginTransaction();
        session.save(this);
        session.getTransaction().commit();
    }

    public Throwable getException() {
        return this.exception;
    }
}
