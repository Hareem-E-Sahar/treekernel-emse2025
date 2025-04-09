package org.blackdog.report;

import java.awt.Desktop;
import java.io.File;
import java.util.ResourceBundle;
import javax.swing.JOptionPane;
import org.apache.log4j.Logger;
import org.blackdog.report.generator.HtmlTagsUpdateReportGenerator;
import org.blackdog.report.generator.TagsUpdateReportGenerator;
import org.blackdog.report.TagsUpdateLog;
import org.blackdog.report.TagsUpdateReport;
import org.siberia.properties.PropertiesManager;

/**
 *
 * Allow to manage a report, scan it to know if we must visualize it
 *
 * @author alexis
 */
public class ReportManager {

    /** logger */
    private Logger logger = Logger.getLogger(ReportManager.class);

    /** Creates a new instance of ReportManager */
    public ReportManager() {
    }

    /** mnage a report
     *	@param report
     */
    public void processReport(TagsUpdateReport report) {
        if (report.getLogsCount() > 0) {
            ResourceBundle rb = ResourceBundle.getBundle(ReportManager.class.getName());
            Object param1 = PropertiesManager.getGeneralProperty("report.generator");
            Object param2 = PropertiesManager.getGeneralProperty("report.view.method");
            Object param3 = PropertiesManager.getGeneralProperty("report.view.status.threshold");
            String generatorName = "html";
            String viewMethod = "warning";
            TagsUpdateLog.Status threshold = TagsUpdateLog.Status.WARNING;
            if (param1 instanceof String) {
                generatorName = (String) param1;
            }
            if (param2 instanceof String) {
                viewMethod = (String) param2;
            }
            if (logger.isDebugEnabled()) {
                logger.debug("report manager should use generator : " + generatorName);
                logger.debug("report manager view method : " + viewMethod);
            }
            TagsUpdateReportGenerator generator = null;
            if ("html".equals(generatorName)) {
                generator = new HtmlTagsUpdateReportGenerator();
            } else {
                generator = new HtmlTagsUpdateReportGenerator();
            }
            if (logger.isDebugEnabled()) {
                logger.debug("report manager should use generator : " + generator);
            }
            boolean askUserToViewReport = false;
            Boolean containsWarnings = null;
            Boolean containsErrors = null;
            if ("never".equals(viewMethod)) {
                askUserToViewReport = false;
            } else if ("always".equals(viewMethod)) {
                askUserToViewReport = true;
            } else if ("debug".equals(viewMethod)) {
                askUserToViewReport = report.containsLogWithStatusHigherOrEqualsTo(TagsUpdateLog.Status.DEBUG);
            } else if ("info".equals(viewMethod)) {
                askUserToViewReport = report.containsLogWithStatusHigherOrEqualsTo(TagsUpdateLog.Status.INFO);
            } else if ("warning".equals(viewMethod)) {
                containsWarnings = report.containsLogWithStatusHigherOrEqualsTo(TagsUpdateLog.Status.WARNING);
                askUserToViewReport = containsWarnings.booleanValue();
            } else if ("error".equals(viewMethod)) {
                containsErrors = report.containsLogWithStatusHigherOrEqualsTo(TagsUpdateLog.Status.ERROR);
                askUserToViewReport = containsErrors.booleanValue();
            } else {
                logger.error("bad value '" + viewMethod + "' in properties for 'report.view.method'");
                askUserToViewReport = true;
            }
            if ("debug".equals(param3)) {
                threshold = TagsUpdateLog.Status.DEBUG;
            } else if ("info".equals(param3)) {
                threshold = TagsUpdateLog.Status.INFO;
            } else if ("warning".equals(param3)) {
                threshold = TagsUpdateLog.Status.WARNING;
            } else if ("error".equals(param3)) {
                threshold = TagsUpdateLog.Status.ERROR;
            }
            if (containsWarnings == null) {
                containsWarnings = report.containsLogWithStatusHigherOrEqualsTo(TagsUpdateLog.Status.WARNING);
            }
            if (containsErrors == null) {
                containsErrors = report.containsLogWithStatusHigherOrEqualsTo(TagsUpdateLog.Status.ERROR);
            }
            String askMessageKind = "askToView.message.classic";
            if (containsErrors.booleanValue()) {
                askMessageKind = "asktoView.message.containsErrors";
            } else if (containsWarnings.booleanValue()) {
                askMessageKind = "asktoView.message.containsWarnings";
            }
            if (!report.containsLogWithStatusHigherOrEqualsTo(threshold)) {
                askUserToViewReport = false;
            }
            if (askUserToViewReport) {
                int answer = JOptionPane.showConfirmDialog(null, rb.getString(askMessageKind), rb.getString("askToView.title"), JOptionPane.YES_OPTION);
                if (answer == JOptionPane.YES_OPTION) {
                    try {
                        File f = generator.generateReport(report, threshold);
                        if (f == null) {
                            throw new IllegalArgumentException("generator should not returns a null file");
                        }
                        if (Desktop.isDesktopSupported()) {
                            Desktop.getDesktop().open(f);
                        } else {
                            StringBuffer message = new StringBuffer(rb.getString("showReportLocation.message"));
                            String location = "{location}";
                            int locationPosition = message.indexOf(location);
                            if (locationPosition > -1) {
                                message.delete(locationPosition, location.length());
                                message.insert(locationPosition, f.getAbsolutePath());
                            }
                            JOptionPane.showMessageDialog(null, rb.getString(""), rb.getString("showReportLocation.title"), JOptionPane.INFORMATION_MESSAGE);
                        }
                    } catch (Exception e) {
                        JOptionPane.showMessageDialog(null, rb.getString("reportGeneration.error.message"), rb.getString("reportGeneration.error.title"), JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        }
    }
}
