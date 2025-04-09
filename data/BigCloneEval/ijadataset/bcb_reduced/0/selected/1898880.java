package org.redwood.business.report.reportgeneration.reportfactory;

import org.redwood.business.report.reportgeneration.reportsummarygeneration.*;
import org.redwood.business.report.reportgeneration.reportgenerationmeasures.reportgenerationmeasure.*;
import org.redwood.business.report.reportgeneration.reportgenerationmeasures.exceptions.*;
import org.redwood.business.report.reportgeneration.reportgenerationmeasures.reportgenerationsummeasure.*;
import org.redwood.business.report.reportgeneration.reportgenerationmeasures.reportgenerationratiomeasure.*;
import org.redwood.business.report.reportgeneration.reportgenerationmeasures.reportgenerationlistmeasures.*;
import org.redwood.business.report.reportgeneration.reportgenerationmeasures.reportgenerationclassificationmeasure.*;
import org.redwood.business.report.reportgeneration.reportgenerationmeasures.reportgenerationmeanmeasure.*;
import org.redwood.business.report.reportgeneration.specificationsgroups.specificationsgroup.*;
import org.redwood.business.report.reportgeneration.measuresseries.measureseries.*;
import org.redwood.business.report.reportgeneration.documenthandler.datadocumenthandler.*;
import org.redwood.business.report.reportgeneration.documenthandler.reportdocumenthandler.*;
import org.redwood.business.report.documents.exceptions.*;
import org.redwood.business.report.reportgeneration.charthandler.*;
import org.redwood.business.usermanagement.reportfile.*;
import org.redwood.business.report.archivedeliverysystem.mailservice.MailMessage;
import org.redwood.business.report.archivedeliverysystem.smsservice.SmsMessage;
import org.redwood.business.usermanagement.report.*;
import org.redwood.business.usermanagement.visualisation.*;
import org.redwood.business.usermanagement.reportmeasures.reportmeasure.*;
import org.redwood.business.usermanagement.reportsummary.*;
import org.redwood.business.usermanagement.person.*;
import org.redwood.business.usermanagement.forwardingtype.*;
import org.redwood.business.usermanagement.fileformat.*;
import org.redwood.tools.*;
import org.redwood.business.report.util.TimePeriod;
import java.io.*;
import java.rmi.RemoteException;
import java.util.*;
import javax.ejb.*;
import javax.naming.*;
import javax.rmi.*;
import javax.transaction.UserTransaction;
import java.util.zip.*;
import java.util.Locale;
import java.text.DecimalFormat;
import javax.jms.Message;
import javax.jms.QueueConnectionFactory;
import javax.jms.QueueConnection;
import javax.jms.QueueSession;
import javax.jms.QueueSender;
import javax.jms.Queue;
import javax.jms.ObjectMessage;
import javax.jms.Session;
import javax.jms.JMSException;
import java.text.SimpleDateFormat;

/**
 * The  bean class.
 *
 * @author  Gerrit Franke
 * @version 1.0
 */
public class ReportFactoryBean implements SessionBean, ReportFactory {

    /** Keeps the reference on the SessionContext.
   */
    protected SessionContext sessionContext = null;

    /** The ReportBean with reportID which contains the report configuration.
   */
    protected Report report;

    /** The name of the report.
   */
    protected String reportName;

    /** Contains the id of the report which is to be created.
   */
    protected String reportID;

    /** Contains the ids of the different web site groups of this report.
   */
    protected Vector webSiteGroupIDs;

    /** The document handler handles the creation and output to the data 
   *  documents.
   */
    protected DataDocumentHandler dataDocHandler = null;

    /** The document handler handles the creation and output to the report 
   *  documents.
   */
    protected ReportDocumentHandler reportDocHandler = null;

    /** Contains the names of the image files (for HTML documents) as keys
   * and their corresponding InputStreams as values.
   */
    protected Map imageByteArrays = new HashMap(10, (float) 0.8);

    /** Contains all files of the generated report documents to send them as EMAIL
   * attachment.
   */
    protected ByteArrayOutputStream zipStream;

    /** Contains all documents created for this report represented as 
   *  ReportFileObjects.
   */
    protected Vector reportfiles;

    /** Contains the recipients of this report as instances of the entity bean
   * "Person"
   */
    protected Collection recipients;

    /** The name to describe the type of the specifications. Is displayed as
   *  title of the specification column in report tables and as labels axis
   *  description in charts.
   */
    String specificationsName;

    /** Gets the current date.
   */
    protected Calendar todayCal = Calendar.getInstance();

    /** Counts the number of the chapters, i.e. the number of ReportGenrationBeans
   *  processed.
   */
    protected int chapterNumber = 1;

    /** To establish queue connection.
   */
    protected QueueConnection queueConnection;

    /** To retrieve queue session.
   */
    protected QueueSession queueSession;

    /** To connect to the messageOutboxQueue.
   */
    protected Queue queue;

    /** To store messageOutboxQueue Sender.
   */
    protected QueueSender sender;

    /** To send status messages to the monitor.*/
    protected MonitorMessenger monitorMessenger = new MonitorMessenger();

    /**
   * There must be one ejbCreate() method per create() method on the Home interface,
   * and with the same signature.
   *
   * @exception RemoteException If the instance could not perform the function
   *            requested by the container
   */
    public void ejbCreate() throws RemoteException {
    }

    /**
   * This method is called when the instance is activated from its "passive" state.
   * The instance should acquire any resource that it has released earlier in the
   * ejbPassivate() method.
   * This method is called with no transaction context.
   *
   * @exception RemoteException - Thrown if the instance could not perform the function
   *            requested by the container
   */
    public void ejbActivate() throws RemoteException {
    }

    /**
   * This method is called before the instance enters the "passive" state.
   * The instance should release any resources that it can re-acquire later in the
   * ejbActivate() method.
   * After the passivate method completes, the instance must be in a state that
   * allows the container to use the Java Serialization protocol to externalize
   * and store away the instance's state.
   * This method is called with no transaction context.
   *
   * @exception RemoteException - Thrown if the instance could not perform the function
   *            requested by the container
   */
    public void ejbPassivate() throws RemoteException {
    }

    /**
   * A container invokes this method before it ends the life of the session object.
   * This happens as a result of a client's invoking a remove operation, or when a
   * container decides to terminate the session object after a timeout.
   * This method is called with no transaction context.
   *
   * @exception RemoteException - Thrown if the instance could not perform the function
   *            requested by the container
   */
    public void ejbRemove() throws RemoteException {
    }

    /**
   * Sets the associated session context. The container calls this method after the instance
   * creation.
   * The enterprise Bean instance should store the reference to the context object
   * in an instance variable.
   * This method is called with no transaction context.
   *
   * @param sessionContext - A SessionContext interface for the instance.
   * @exception RemoteException - Thrown if the instance could not perform the function
   *            requested by the container because of a system-level error.
   */
    public void setSessionContext(SessionContext sessionContext) throws RemoteException {
        this.sessionContext = sessionContext;
    }

    /** This methods creates a report following the report configuration for the
   * named reportID. The report starts at (endDate - reportPeriodLength) and
   * ends at endDate. The created documents will be stored and eventually sent
   * to the recipients.
   *
   * @param reportID  The id of the report to generate
   * @param endDate  The end date of the report
   *
   * @exception RemoteException   Thrown if the instance could not perform the
   *                              function requested
   * @exception ReportGenerationFailedException  Thrown if the generation of the
   *                                             report has failed totally.
   */
    public void createReport(String reportID, Date endDate) throws RemoteException, ReportGenerationFailedException {
        imageByteArrays = new HashMap(10, (float) 0.8);
        reportfiles = new Vector();
        chapterNumber = 1;
        this.reportID = reportID;
        Calendar endCal = Calendar.getInstance();
        endCal.setTime(endDate);
        endCal.set(Calendar.HOUR_OF_DAY, 0);
        endCal.set(Calendar.MINUTE, 0);
        endCal.set(Calendar.SECOND, 0);
        Collection reportMeasures = null;
        Collection reportSummaries = null;
        try {
            Context initialContext = new InitialContext();
            ReportHome reportHome = (ReportHome) PortableRemoteObject.narrow(initialContext.lookup(ReportHome.COMP_NAME), ReportHome.class);
            report = reportHome.findByPrimaryKey(new ReportPK(reportID));
            reportMeasures = report.getReportMeasures();
            reportSummaries = report.getReportSummaries();
            webSiteGroupIDs = new Vector(report.getWebSiteGroupIDs());
            reportName = report.getRw_name();
        } catch (Exception e) {
            e.printStackTrace();
            throw new ReportGenerationFailedException("Loading report " + reportID + " failed");
        }
        try {
            recipients = report.getRecipients();
        } catch (Exception e) {
            recipients = new Vector();
        }
        try {
            createDocuments();
        } catch (DocumentCreateException e) {
            throw new ReportGenerationFailedException(e.getMessage());
        }
        try {
            Collection reportFiles;
            ReportFile reportfile;
            Context initialContext = new InitialContext();
            ReportFileHome reportfileHome = (ReportFileHome) PortableRemoteObject.narrow(initialContext.lookup(ReportFileHome.COMP_NAME), ReportFileHome.class);
            reportFiles = reportfileHome.findByReportID("redwood_logo");
            Iterator iter = reportFiles.iterator();
            if (iter.hasNext()) {
                reportfile = (ReportFile) iter.next();
                byte[] file = reportfile.getRw_file();
                if (reportDocHandler.isHTMLGeneration()) imageByteArrays.put("redwood_logo.jpg", file);
                reportDocHandler.addImage(file, "images/redwood_logo.jpg");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            reportDocHandler.writeReportHeader(reportName, webSiteGroupIDs, report.getPerson(), recipients);
        } catch (Exception e) {
        }
        ReportSummary reportSummary = null;
        ReportSummaryGeneration reportSummaryGeneration = null;
        Iterator iterator = reportSummaries.iterator();
        while (iterator.hasNext()) {
            reportSummary = (ReportSummary) iterator.next();
            try {
                reportSummaryGeneration = createReportSummaryGeneration(endCal, reportSummary);
                if (reportSummaryGeneration != null) {
                    processReportSummary(reportSummaryGeneration);
                    reportDocHandler.addReportText("\t");
                } else {
                    reportDocHandler.addReportText("The processing of a summary has failed!");
                }
            } catch (Exception e) {
                e.printStackTrace();
                try {
                    reportDocHandler.addReportText("The processing of a summary has failed!");
                } catch (Exception re) {
                    re.printStackTrace();
                }
            }
        }
        ReportMeasure reportMeasure = null;
        String internalMeasureType;
        ReportGenerationMeasure reportGenerationMeasure = null;
        iterator = reportMeasures.iterator();
        while (iterator.hasNext()) {
            reportMeasure = (ReportMeasure) iterator.next();
            try {
                internalMeasureType = reportMeasure.getInternalMeasureType();
                if (internalMeasureType.equalsIgnoreCase("SumMeasure")) {
                    reportGenerationMeasure = createReportGenerationSumMeasure(endCal, reportMeasure);
                    specificationsName = "Time Periods";
                }
                if (internalMeasureType.equalsIgnoreCase("RatioMeasure")) {
                    reportGenerationMeasure = createReportGenerationRatioMeasure(endCal, reportMeasure);
                    specificationsName = "Time Periods";
                }
                if (internalMeasureType.equalsIgnoreCase("ListMeasure")) {
                    reportGenerationMeasure = createReportGenerationListMeasure(endCal, reportMeasure);
                    specificationsName = reportGenerationMeasure.getMeasureName();
                }
                if (internalMeasureType.equalsIgnoreCase("MeanMeasure")) {
                    reportGenerationMeasure = createReportGenerationMeanMeasure(endCal, reportMeasure);
                    specificationsName = "Mean Time Periods";
                }
                if (internalMeasureType.equalsIgnoreCase("ClassificationMeasure")) {
                    reportGenerationMeasure = createReportGenerationClassificationMeasure(endCal, reportMeasure);
                    specificationsName = "Classes";
                }
                if (reportGenerationMeasure != null) {
                    process(reportGenerationMeasure);
                    reportDocHandler.addReportText("\t");
                } else {
                    reportDocHandler.addReportText("The processing of a measure has failed!");
                }
            } catch (Exception e) {
                try {
                    reportDocHandler.addReportText("The processing of a measure has failed!");
                } catch (Exception re) {
                }
            }
        }
        try {
            reportDocHandler.addReportText("This document was created by using Chart2D (http://chart2d.sourceforge.net), \nJpegEncoder (http://www.obrador.com/essentialjpeg/jpeg.htm)" + ", \nPngEncoder written by J. David Eisenberg (http://catcode.com/pngencoder/) and \niText (http://www.lowagie.com/iText/).");
        } catch (DocumentInsertException de) {
            de.printStackTrace();
        }
        try {
            reportDocHandler.closeDocuments();
        } catch (Exception e) {
        }
        try {
            dataDocHandler.closeDocuments();
        } catch (Exception e) {
        }
        try {
            storeDocuments();
        } catch (DocumentStoreException dse) {
            throw new ReportGenerationFailedException(dse.getMessage());
        }
        try {
            sendReport();
        } catch (ReportSendException rse) {
            rse.printStackTrace();
        }
    }

    /** Creates all the documents to which the report should be written as
   *  indicated by the FileFormats of this report configuration.
   *
   *  @exception  DocumentCreateException  Thrown if the creation of all
   *                                       documents has failed
   */
    protected void createDocuments() throws DocumentCreateException {
        try {
            Context initialContext = new InitialContext();
            DataDocumentHandlerHome dataDocumentHandlerHome = (DataDocumentHandlerHome) PortableRemoteObject.narrow(initialContext.lookup(DataDocumentHandlerHome.COMP_NAME), DataDocumentHandlerHome.class);
            dataDocHandler = dataDocumentHandlerHome.create();
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            Context initialContext = new InitialContext();
            ReportDocumentHandlerHome reportDocumentHandlerHome = (ReportDocumentHandlerHome) PortableRemoteObject.narrow(initialContext.lookup(ReportDocumentHandlerHome.COMP_NAME), ReportDocumentHandlerHome.class);
            reportDocHandler = reportDocumentHandlerHome.create();
        } catch (Exception e) {
            e.printStackTrace();
        }
        Collection fileFormats;
        try {
            fileFormats = report.getFileFormats();
        } catch (Exception e) {
            throw new DocumentCreateException("No file formats found.");
        }
        boolean docsOpened = false;
        try {
            dataDocHandler.createDocuments(reportName, reportID, fileFormats);
            docsOpened = true;
        } catch (RemoteException re) {
            docsOpened = false;
        }
        try {
            reportDocHandler.createDocuments(reportName, reportID, fileFormats);
            docsOpened = true;
        } catch (RemoteException re) {
            if (docsOpened = false) throw new DocumentCreateException(re.getMessage());
        }
    }

    /** Stores the documents created for this report in the database. They are
   *  stored as byte arrays.
   *  HTML documents are stored in a zip format together with the image files
   *  referenced in the document.
   *
   *  @exception DocumentStoreException - If the storage of all documents has
   *                                      failed
   */
    protected void storeDocuments() throws DocumentStoreException {
        ReportFileHome reportFileHome = null;
        try {
            Context initialContext = new InitialContext();
            reportFileHome = (ReportFileHome) PortableRemoteObject.narrow(initialContext.lookup(ReportFileHome.COMP_NAME), ReportFileHome.class);
        } catch (Exception e) {
            throw new DocumentStoreException("ReportFileHome could not be loaded");
        }
        int count = 0;
        String filename;
        String title, ending;
        byte[] b;
        ReportFileObject reportFileObject;
        Enumeration enumTitles;
        try {
            enumTitles = reportDocHandler.getReportDocumentTitles().elements();
        } catch (RemoteException re) {
            enumTitles = new Vector().elements();
        }
        while (enumTitles.hasMoreElements()) {
            try {
                filename = (String) enumTitles.nextElement();
                ending = filename.substring(filename.lastIndexOf('.') + 1);
                title = filename.substring(0, filename.lastIndexOf('.'));
                b = reportDocHandler.getReportDocumentStream(filename);
                if (filename.endsWith("HTML") | filename.endsWith("html")) {
                    zipStream = new ByteArrayOutputStream();
                    ZipOutputStream zip = new ZipOutputStream(zipStream);
                    ZipEntry entry = new ZipEntry(filename);
                    zip.putNextEntry(entry);
                    zip.write(b);
                    zip.closeEntry();
                    String key;
                    Set imageSet = imageByteArrays.keySet();
                    Iterator iterCharts = imageSet.iterator();
                    while (iterCharts.hasNext()) {
                        key = (String) iterCharts.next();
                        b = (byte[]) imageByteArrays.get(key);
                        entry = new ZipEntry("images/" + key);
                        zip.putNextEntry(entry);
                        zip.write(b);
                        zip.closeEntry();
                    }
                    zip.close();
                    UniqueKeyGenerator primkeygen = new UniqueKeyGenerator();
                    reportFileObject = reportFileHome.create(primkeygen.getUniqueId(), title, zipStream.toByteArray(), new Date(), reportID, "ZIP");
                    reportfiles.addElement(reportFileObject);
                    zipStream = null;
                    count++;
                } else {
                    UniqueKeyGenerator primkeygen = new UniqueKeyGenerator();
                    reportFileObject = reportFileHome.create(primkeygen.getUniqueId(), title, b, new Date(), reportID, ending);
                    reportfiles.addElement(reportFileObject);
                    count++;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        try {
            reportDocHandler.deleteDocuments();
        } catch (RemoteException re) {
        }
        try {
            enumTitles = dataDocHandler.getDataDocumentTitles().elements();
        } catch (RemoteException re) {
            enumTitles = new Vector().elements();
        }
        while (enumTitles.hasMoreElements()) {
            try {
                filename = (String) enumTitles.nextElement();
                ending = filename.substring(filename.lastIndexOf('.') + 1);
                title = filename.substring(0, filename.lastIndexOf('.'));
                b = dataDocHandler.getDataDocumentStream(filename);
                UniqueKeyGenerator primkeygen = new UniqueKeyGenerator();
                reportFileObject = reportFileHome.create(primkeygen.getUniqueId(), title, b, new Date(), reportID, ending);
                reportfiles.addElement(reportFileObject);
                count++;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        try {
            dataDocHandler.deleteDocuments();
        } catch (RemoteException re) {
        }
        if (count == 0) throw new DocumentStoreException("No document could be stored.");
    }

    /** Processes (loads the data and adds it to the report)
   *  the current ReportGenerationMeasure.
   *  @param reportGenMeasure  The reportGenerationMeasure to process.
   *  @exception RemoteException - Thrown if the instance could not perform the
   *                              function requested
   */
    protected void process(ReportGenerationMeasure reportGenMeasure) throws RemoteException {
        TimePeriod timePeriod = reportGenMeasure.getReportMeasureTimePeriod();
        String measureName = reportGenMeasure.getMeasureName();
        System.out.println("Processing " + measureName);
        monitorMessenger.sendMonitorInformation(reportName + ": Processing " + measureName);
        reportDocHandler.openChapter(measureName, chapterNumber);
        chapterNumber++;
        Locale englishloc = new Locale("en", "");
        SimpleDateFormat formatterMeasure = new SimpleDateFormat(" EEEE  dd  MMMM  yyyy HH:mm zzz", englishloc);
        Date begindate = timePeriod.getBeginDate().getTime();
        String beginDateString = formatterMeasure.format(begindate);
        Date enddate = timePeriod.getEndDate().getTime();
        enddate.setMinutes(enddate.getMinutes() - 1);
        String endDateString = formatterMeasure.format(enddate);
        String beginEndDateString = beginDateString + " - " + endDateString;
        reportDocHandler.addMeasureHeaderTime(beginEndDateString);
        try {
            reportDocHandler.addReportText("Description: " + reportGenMeasure.getMeasureDescription());
        } catch (Exception e) {
        }
        try {
            reportGenMeasure.loadData();
        } catch (NoDataException nde) {
            try {
                reportDocHandler.addReportText(" ");
                reportDocHandler.addReportText("No data was found at all" + " for this measure at the given time period!");
                reportDocHandler.closeChapter();
            } catch (Exception e) {
            }
            return;
        }
        try {
            reportDocHandler.addReportText("Annotations: " + reportGenMeasure.getAnnotations());
        } catch (Exception e) {
        }
        Collection visualisations = reportGenMeasure.getVisualisations();
        String visualisationName;
        Iterator iterator = visualisations.iterator();
        while (iterator.hasNext()) {
            visualisationName = ((Visualisation) iterator.next()).getRw_name();
            if (visualisationName.equalsIgnoreCase("ReportTable")) {
                reportDocHandler.addReportMeasureTable(specificationsName, reportGenMeasure);
            }
            if (visualisationName.equalsIgnoreCase("Text")) {
                reportDocHandler.addText(specificationsName, reportGenMeasure);
            }
            if (visualisationName.endsWith("Chart") || visualisationName.endsWith("CHART")) {
                try {
                    addMeasureChart(visualisationName, reportGenMeasure);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            if (visualisationName.equalsIgnoreCase("DATA")) {
                try {
                    dataDocHandler.writeData(reportGenMeasure);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        try {
            reportDocHandler.closeChapter();
        } catch (DocumentInsertException de) {
            de.printStackTrace();
        }
    }

    /** Processes (loads the data and adds it to the report)
   *  the current ReportSummary.
   *  @param reportSummaryGen  The ReportSummaryGeneration to process.
   *  @exception RemoteException - Thrown if the instance could not perform the
   *                              function requested
   */
    protected void processReportSummary(ReportSummaryGeneration reportSummaryGen) throws RemoteException {
        TimePeriod timePeriod = reportSummaryGen.getReportSummaryTimePeriod();
        System.out.println("Processing Report Summary");
        monitorMessenger.sendMonitorInformation(reportName + ": Processing Report Summary");
        reportDocHandler.openChapter("Report Summary", chapterNumber);
        chapterNumber++;
        Locale englishloc = new Locale("en", "");
        SimpleDateFormat formatterMeasure = new SimpleDateFormat(" EEEE  dd  MMMM  yyyy HH:mm zzz", englishloc);
        Date begindate = timePeriod.getBeginDate().getTime();
        String beginDateString = formatterMeasure.format(begindate);
        Date enddate = timePeriod.getEndDate().getTime();
        enddate.setMinutes(enddate.getMinutes() - 1);
        String endDateString = formatterMeasure.format(enddate);
        String beginEndDateString = beginDateString + " - " + endDateString;
        reportDocHandler.addMeasureHeaderTime(beginEndDateString);
        try {
            reportSummaryGen.loadData();
        } catch (NoDataException nde) {
            try {
                reportDocHandler.addReportText(" ");
                reportDocHandler.addReportText("No data was found at all" + " for this summary at the given time period!");
                reportDocHandler.closeChapter();
            } catch (Exception e) {
            }
            return;
        }
        Collection visualisations = reportSummaryGen.getVisualisations();
        String visualisationName;
        Iterator iterator = visualisations.iterator();
        while (iterator.hasNext()) {
            visualisationName = ((Visualisation) iterator.next()).getRw_name();
            if (visualisationName.equalsIgnoreCase("ReportTable")) {
                reportDocHandler.addReportSummaryTable(reportSummaryGen);
            }
            if (visualisationName.endsWith("Chart") || visualisationName.endsWith("CHART")) {
                try {
                    addSummaryChart(visualisationName, reportSummaryGen);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        try {
            reportDocHandler.closeChapter();
        } catch (DocumentInsertException de) {
            de.printStackTrace();
        }
    }

    /** Adds charts for this reportGenerationMeasure to the report documents.
   *
   * @param String chartType - The type of the chart.
   * @param ReportGenerationMeasure reportGenMeasure - The 
   *                                ReportGenerationMeasure currently processed
   *  @exception Exception   Exceptions that occur are handled by the calling
   *                         methods (regardless of their type)
   */
    protected void addMeasureChart(String chartType, ReportGenerationMeasure reportGenMeasure) throws Exception {
        ChartHandlerObject chartHandler = null;
        try {
            Context initialContext = new InitialContext();
            ChartHandlerHome chartHandlerHome = (ChartHandlerHome) PortableRemoteObject.narrow(initialContext.lookup(ChartHandlerHome.COMP_NAME), ChartHandlerHome.class);
            chartHandler = chartHandlerHome.create();
        } catch (Exception e) {
            e.printStackTrace();
        }
        Vector charts;
        byte[] b;
        charts = chartHandler.createMeasureCharts(chartType, reportGenMeasure);
        Enumeration enumCharts = charts.elements();
        int chartNb = 1;
        while (enumCharts.hasMoreElements()) {
            b = (byte[]) enumCharts.nextElement();
            String filename = chartType + chartNb + new Date().getTime() + ".png";
            chartNb++;
            if (reportDocHandler.isHTMLGeneration()) imageByteArrays.put(filename, b);
            reportDocHandler.addReportText("");
            reportDocHandler.addImage(b, "images/" + filename);
        }
    }

    /** Adds charts for this reportSummary to the report documents.
   *
   * @param String chartType  The type of the chart.
   * @param ReportSummaryGen reportSummaryGen  The ReportSummaryGen
   *                                           currently processed
   *
   *  @exception Exception   Exceptions that occur are handled by the calling
   *                         methods (regardless of their type)
   */
    protected void addSummaryChart(String chartType, ReportSummaryGeneration reportSummaryGen) throws Exception {
        ChartHandlerObject chartHandler = null;
        try {
            Context initialContext = new InitialContext();
            ChartHandlerHome chartHandlerHome = (ChartHandlerHome) PortableRemoteObject.narrow(initialContext.lookup(ChartHandlerHome.COMP_NAME), ChartHandlerHome.class);
            chartHandler = chartHandlerHome.create();
        } catch (Exception e) {
            e.printStackTrace();
        }
        Vector charts;
        byte[] b;
        charts = chartHandler.createSummaryCharts(chartType, reportSummaryGen);
        Enumeration enumCharts = charts.elements();
        int chartNb = 1;
        while (enumCharts.hasMoreElements()) {
            b = (byte[]) enumCharts.nextElement();
            String filename = chartType + chartNb + new Date().getTime() + ".png";
            chartNb++;
            if (reportDocHandler.isHTMLGeneration()) imageByteArrays.put(filename, b);
            reportDocHandler.addReportText("");
            reportDocHandler.addImage(b, "images/" + filename);
        }
    }

    /** Sends the report to the recipients as stated in the report
   *  configuration by the forwardingType (or no sending).
   *
   *  @exception ReportSendException - Thrown if sending the report has failed.
   */
    protected void sendReport() throws ReportSendException {
        String forwardingType = "";
        try {
            ForwardingType ft = report.getForwardingType();
            if (ft != null) {
                forwardingType = ft.getRw_name();
            } else forwardingType = "NONE";
        } catch (RemoteException e) {
            throw new ReportSendException("Loading of forwarding types failed.");
        }
        if (!(forwardingType.equalsIgnoreCase("NONE"))) {
            try {
                connectToMessageOutboxQueue();
            } catch (Exception e) {
                throw new ReportSendException("Could not connect to messageOutboxQueue.");
            }
            int count = 0;
            Vector filenames = new Vector();
            Vector byteArrays = new Vector();
            Enumeration enumReportfiles = reportfiles.elements();
            ReportFile reportFile;
            while (enumReportfiles.hasMoreElements()) {
                try {
                    reportFile = (ReportFile) enumReportfiles.nextElement();
                    filenames.addElement(reportFile.getRw_title() + "." + reportFile.getRw_filetype());
                    byteArrays.addElement(reportFile.getRw_file());
                } catch (Exception e) {
                }
            }
            Person person;
            Iterator iterator = recipients.iterator();
            while (iterator.hasNext()) {
                try {
                    person = (Person) iterator.next();
                    if (forwardingType.equalsIgnoreCase("EMAIL")) {
                        sendReportAsMail(person.getRw_email(), filenames, byteArrays);
                    } else if (forwardingType.equalsIgnoreCase("SMS")) {
                        String filename = (String) reportDocHandler.getReportDocumentTitles().firstElement();
                        byte[] b = reportDocHandler.getReportDocumentStream(filename);
                        sendReportAsSMS(person.getRw_handyNr(), new String(b));
                    }
                    count++;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            if ((count == 0) && (count < recipients.size())) throw new ReportSendException("No message has been send successfully");
        }
    }

    /** Sends the generated report documents via the messageOutBoxQueue
   *  as email to the recipients.
   *
   *  @param emailAddress - The email address of the person whom to send the
   *                        email
   *
   *  @exception Exception - Exceptions thrown are handled in the calling
   *                         methods (regardless of their type)
   */
    protected void sendReportAsMail(String emailAddress, Vector filenames, Vector byteArrays) throws Exception {
        String subject;
        try {
            subject = report.getRw_name();
        } catch (RemoteException re) {
            subject = "New Report";
        }
        String content = "Your report created on " + todayCal.getTime();
        String from = "noreply@redwood.de";
        MailMessage mailmessage = new MailMessage(emailAddress, subject, content, filenames, byteArrays);
        ObjectMessage message = queueSession.createObjectMessage();
        message.setObject(mailmessage);
        sender.send(queue, message);
    }

    /** Sends the generated report documents via the messageOutBoxQueue
   *  as SMS to the recipients.
   *
   *  @param emailaddressforsms - The email address to which the message is sent
   *                             to, so that the recipient receives a SMS
   *  @param sunject - The subject of the email to send which will be displayed
   *                   as content of the SMS
   *
   *  @exception Exception - Exceptions that occur are handled in the calling 
   *                         methods (regardless of their type)
   */
    protected void sendReportAsSMS(String emailaddressforsms, String subject) throws Exception {
        subject = report.getRw_name() + subject;
        SmsMessage smsMessage = new SmsMessage(emailaddressforsms, subject);
        ObjectMessage message = queueSession.createObjectMessage();
        message.setObject(smsMessage);
        sender.send(queue, message);
    }

    /** Tries to connect to the messageOutBoxQueue to send EMAIL or SMS.
   *
   *  @exception Exception - Exceptions thrown are handled in the calling
   *                         methods (regardless of their type)
   */
    protected void connectToMessageOutboxQueue() throws Exception {
        Context context = new InitialContext();
        QueueConnectionFactory queueFactory = (QueueConnectionFactory) context.lookup("jms/QueueConnectionFactory");
        queueConnection = queueFactory.createQueueConnection();
        queueConnection.start();
        queueSession = queueConnection.createQueueSession(false, Session.AUTO_ACKNOWLEDGE);
        queue = (Queue) context.lookup("jms/MessageOutboxQueue");
        sender = queueSession.createSender(queue);
    }

    /** Creates a ReportGenerationSumMeasure matching the internalMeasureType of
   *  the current ReportMeasure.
   *
   *  @param endCal - The end date of this report
   *  @param reportMeasure - The report measure containig the configuration for
   *                         the measure in this report
   *
   *  @exception Exception - Exceptions that occur are handled in the calling
   *                         methods (regardless of their type)
   */
    protected ReportGenerationMeasure createReportGenerationSumMeasure(Calendar endCal, ReportMeasure reportMeasure) throws Exception {
        Context initialContext = new InitialContext();
        ReportGenerationSumMeasureHome home = (ReportGenerationSumMeasureHome) PortableRemoteObject.narrow(initialContext.lookup(ReportGenerationSumMeasureHome.COMP_NAME), ReportGenerationSumMeasureHome.class);
        return home.create(reportMeasure, webSiteGroupIDs, endCal);
    }

    /** Creates a ReportGenerationRatioMeasure matching the internalMeasureType of
   * the current ReportMeasure
   *
   *  @param endCal - The end date of this report
   *  @param reportMeasure - The report measure containig the configuration for
   *                         the measure in this report
   *
   *  @exception Exception - Exceptions that occur are handled in the calling
   *                         methods (regardless of their type)
   */
    protected ReportGenerationMeasure createReportGenerationRatioMeasure(Calendar endCal, ReportMeasure reportMeasure) throws Exception {
        Context initialContext = new InitialContext();
        ReportGenerationRatioMeasureHome home = (ReportGenerationRatioMeasureHome) PortableRemoteObject.narrow(initialContext.lookup(ReportGenerationRatioMeasureHome.COMP_NAME), ReportGenerationRatioMeasureHome.class);
        return home.create(reportMeasure, webSiteGroupIDs, endCal);
    }

    /** Creates a ReportGenerationListMeasure matching the internalMeasureType of
   * the current ReportMeasure
   *
   *  @param endCal - The end date of this report
   *  @param reportMeasure - The report measure containig the configuration for
   *                         the measure in this report
   *
   *  @exception Exception - Exceptions that occur are handled in the calling
   *                         methods (regardless of their type)
   */
    protected ReportGenerationMeasure createReportGenerationListMeasure(Calendar endCal, ReportMeasure reportMeasure) throws Exception {
        Context initialContext = new InitialContext();
        Object ref = initialContext.lookup(ReportGenerationListMeasureHome.COMP_NAME);
        ReportGenerationListMeasureHome home = (ReportGenerationListMeasureHome) PortableRemoteObject.narrow(ref, ReportGenerationListMeasureHome.class);
        return home.create(reportMeasure, webSiteGroupIDs, endCal);
    }

    /** Creates a ReportGenerationMeanMeasure matching the internalMeasureType of
   * the current ReportMeasure
   *
   *  @param endCal - The end date of this report
   *  @param reportMeasure - The report measure containig the configuration for
   *                         the measure in this report
   *
   *  @exception Exception - Exceptions that occur are handled in the calling
   *                         methods (regardless of their type)
   */
    protected ReportGenerationMeasure createReportGenerationMeanMeasure(Calendar endCal, ReportMeasure reportMeasure) throws Exception {
        Context initialContext = new InitialContext();
        Object ref = initialContext.lookup(ReportGenerationMeanMeasureHome.COMP_NAME);
        ReportGenerationMeanMeasureHome home = (ReportGenerationMeanMeasureHome) PortableRemoteObject.narrow(ref, ReportGenerationMeanMeasureHome.class);
        return home.create(reportMeasure, webSiteGroupIDs, endCal);
    }

    /** Creates a ReportGenerationClassificationMeasure matching the
   *  internalMeasureType of the current ReportMeasure
   *
   *  @param endCal - The end date of this report
   *  @param reportMeasure - The report measure containig the configuration for
   *                         the measure in this report
   *
   *  @exception Exception - Exceptions that occur are handled in the calling
   *                         methods (regardless of their type)
   */
    protected ReportGenerationMeasure createReportGenerationClassificationMeasure(Calendar endCal, ReportMeasure reportMeasure) throws Exception {
        Context initialContext = new InitialContext();
        Object ref = initialContext.lookup(ReportGenerationClassificationMeasureHome.COMP_NAME);
        ReportGenerationClassificationMeasureHome home = (ReportGenerationClassificationMeasureHome) PortableRemoteObject.narrow(ref, ReportGenerationClassificationMeasureHome.class);
        return home.create(reportMeasure, webSiteGroupIDs, endCal);
    }

    /** Creates a ReportSummaryGenerationmatching 
   *
   *  @param endCal - The end date of this report
   *  @param reportSummary - The report summary containig the configuration for
   *                         the summary in this report
   *
   *  @exception Exception - Exceptions that occur are handled in the calling
   *                         methods (regardless of their type)
   */
    protected ReportSummaryGeneration createReportSummaryGeneration(Calendar endCal, ReportSummary reportSummary) throws Exception {
        Context initialContext = new InitialContext();
        ReportSummaryGenerationHome home = (ReportSummaryGenerationHome) PortableRemoteObject.narrow(initialContext.lookup(ReportSummaryGenerationHome.COMP_NAME), ReportSummaryGenerationHome.class);
        return home.create(reportSummary, webSiteGroupIDs, endCal);
    }
}
