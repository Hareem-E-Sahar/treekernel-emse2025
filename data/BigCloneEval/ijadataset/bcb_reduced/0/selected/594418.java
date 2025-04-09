package com.manydesigns.portofino.methods.jasperreports;

import com.manydesigns.portofino.base.*;
import com.manydesigns.portofino.base.permissions.PermissionException;
import com.manydesigns.portofino.base.permissions.VisibilityException;
import com.manydesigns.portofino.methods.BadRequestException;
import com.manydesigns.portofino.methods.BasicServlet;
import com.manydesigns.portofino.util.Defs;
import com.manydesigns.portofino.util.Util;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.export.*;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.io.Serializable;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @author Paolo Predonzani - paolo.predonzani@manydesigns.com
 * @author Angelo Lupo      - angelo.lupo@manydesigns.com
 */
public class RunJasperReports3 extends BasicServlet {

    public static final String copyright = "Copyright (c) 2005-2009, ManyDesigns srl";

    @Override
    public void doMethod(HttpServletRequest req, HttpServletResponse res, MDConfig config) throws Exception {
        Locale locale = config.getLocale();
        String returnurl = req.getParameter("returnurl");
        String operation = req.getParameter(Defs.ACTION_FORM);
        if (operation != null && operation.equals(Util.getLocalizedString(Defs.MDLIBI18N, locale, "Cancel"))) {
            res.sendRedirect(returnurl);
            return;
        }
        int id;
        String idString = req.getParameter("id");
        try {
            id = Integer.parseInt(idString);
        } catch (Exception e) {
            String param = "id";
            throw new BadRequestException(MessageFormat.format(Util.getLocalizedString(Defs.MDLIBI18N, locale, "Invalid_value_for_parameter"), idString, param));
        }
        MDJasperReports jasperReportsObj = config.getJasperReportsById(id);
        if (jasperReportsObj.getJasperReports() == null) {
            String msg = MessageFormat.format(Util.getLocalizedString(Defs.MDLIBI18N, config.getLocale(), "JasperReports_file_not_found"), jasperReportsObj.getName(), jasperReportsObj.getLocation());
            throw new Exception(msg);
        }
        if (!jasperReportsObj.isVisible()) {
            throw new VisibilityException(Util.getLocalizedString(Defs.MDLIBI18N, locale, "Visibility_denied"));
        }
        Integer idObject = null;
        String strIdObject = req.getParameter("idObject");
        try {
            if (strIdObject != null) {
                idObject = new Integer(strIdObject);
                MDClass cls = jasperReportsObj.getOwnerClass();
                MDObject obj = cls.getMDObject(idObject);
                if (!obj.isVisible()) {
                    throw new VisibilityException(Util.getLocalizedString(Defs.MDLIBI18N, locale, "Visibility_denied"));
                }
                if (!obj.canRead()) {
                    throw new PermissionException(Util.getLocalizedString(Defs.MDLIBI18N, locale, "Permission_denied"));
                }
            }
        } catch (Exception e) {
            String param = "idObject";
            throw new BadRequestException(MessageFormat.format(Util.getLocalizedString(Defs.MDLIBI18N, locale, "Invalid_value_for_parameter"), strIdObject, param));
        }
        String formatReport;
        if (jasperReportsObj.isSingleFormatOutput()) {
            formatReport = RunJasperReports2.formatReport(jasperReportsObj);
        } else {
            formatReport = req.getParameter("formatReport");
            boolean verifyFormat = formatReport != null && (formatReport.equals(Defs.JRPT_CSV) || formatReport.equals(Defs.JRPT_HTML) || formatReport.equals(Defs.JRPT_PDF) || formatReport.equals(Defs.JRPT_RTF) || formatReport.equals(Defs.JRPT_TXT) || formatReport.equals(Defs.JRPT_XLS));
            if (!verifyFormat) {
                throw new PermissionException(Util.getLocalizedString(Defs.MDLIBI18N, locale, "Undefined_report_format"));
            }
        }
        JasperReport jasperReports = jasperReportsObj.getJasperReports();
        List<JRParameter> jrParamJrxml = new LinkedList<JRParameter>(Arrays.asList(jasperReports.getParameters()));
        Map<String, Serializable> parameters = new HashMap<String, Serializable>();
        parameters = LoadMapJR(req, jrParamJrxml, jasperReportsObj, parameters, config);
        if (idObject != null && !parameters.containsKey("id")) {
            parameters.put("id", idObject);
        }
        parameters.put("schema0", config.getSchema0());
        parameters.put("schema1", config.getSchema1());
        parameters.put("schema2", config.getSchema2());
        parameters.put(JRParameter.REPORT_LOCALE, config.getLocale());
        printReport(jasperReports, jasperReportsObj, res, formatReport, parameters, locale, config);
    }

    private Map<String, Serializable> LoadMapJR(HttpServletRequest req, List<JRParameter> jrParamJrxml, MDJasperReports jasperReportsObj, Map<String, Serializable> parameters, MDConfig config) throws Exception {
        for (MDJasperReportsParam jasperReportsParamObj : jasperReportsObj.fillParameter()) {
            JRParameter param = RunJasperReports2.getParameter(jrParamJrxml, jasperReportsParamObj);
            if (param == null) {
                String msg = MessageFormat.format(Util.getLocalizedString(Defs.MDLIBI18N, config.getLocale(), "JasperReports_error_Parameter_not_found"), jasperReportsParamObj.getName(), jasperReportsObj.getLocation());
                throw new Exception(msg);
            }
            int type;
            String classType = param.getValueClassName();
            if (classType.equals("java.lang.String")) type = java.sql.Types.VARCHAR; else if (classType.equals("java.lang.Integer") || classType.equals("java.lang.Long") || classType.equals("java.lang.Short") || classType.equals("java.lang.Byte")) type = java.sql.Types.INTEGER; else if (classType.equals("java.lang.Boolean")) type = java.sql.Types.BOOLEAN; else if (classType.equals("java.sql.Date") || classType.equals("java.util.Date")) type = java.sql.Types.DATE; else if (classType.equals("java.lang.Double") || classType.equals("java.lang.Float") || classType.equals("java.math.BigDecimal")) type = java.sql.Types.DECIMAL; else {
                String msg = MessageFormat.format(Util.getLocalizedString(Defs.MDLIBI18N, config.getLocale(), "Unknown_Type"), classType, jasperReportsParamObj.getName());
                throw new Exception(msg);
            }
            String value = req.getParameter("attr" + jasperReportsParamObj.getId());
            String defaultValue = jasperReportsParamObj.getForceValue();
            switch(type) {
                case java.sql.Types.VARCHAR:
                    if (defaultValue != null && !defaultValue.equals("")) parameters.put(jasperReportsParamObj.getName(), defaultValue); else if (value != null && !value.equals("")) parameters.put(jasperReportsParamObj.getName(), value);
                    break;
                case java.sql.Types.INTEGER:
                    if (defaultValue != null && !defaultValue.equals("")) parameters.put(jasperReportsParamObj.getName(), new java.lang.Integer(defaultValue)); else if (value != null && !value.equals("")) parameters.put(jasperReportsParamObj.getName(), new java.lang.Integer(value));
                    break;
                case java.sql.Types.BOOLEAN:
                    if (defaultValue != null && defaultValue.equals("1")) parameters.put(jasperReportsParamObj.getName(), true); else if (value != null && value.equals("on")) parameters.put(jasperReportsParamObj.getName(), true); else parameters.put(jasperReportsParamObj.getName(), false);
                    break;
                case java.sql.Types.DECIMAL:
                    DecimalFormat df = new DecimalFormat(Defs.DECIMAL_FORMAT);
                    if (defaultValue != null && !defaultValue.equals("")) {
                        BigDecimal app = new BigDecimal(defaultValue);
                        parameters.put(jasperReportsParamObj.getName(), new java.math.BigDecimal(df.format(app.doubleValue())));
                    } else if (value != null && !value.equals("")) {
                        BigDecimal app = new BigDecimal(value);
                        parameters.put(jasperReportsParamObj.getName(), new java.math.BigDecimal(df.format(app.doubleValue())));
                    }
                    break;
                case java.sql.Types.DATE:
                    SimpleDateFormat sdf = new SimpleDateFormat(config.getLocaleInfo().getDateFormat());
                    sdf.setLenient(false);
                    if (defaultValue != null && !defaultValue.equals("")) parameters.put(jasperReportsParamObj.getName(), sdf.parse(defaultValue)); else if (value != null && !value.equals("")) parameters.put(jasperReportsParamObj.getName(), sdf.parse(value));
                    break;
                default:
                    String msg = MessageFormat.format(Util.getLocalizedString(Defs.MDLIBI18N, config.getLocale(), "Unknown_Type"), type, jasperReportsParamObj.getName());
                    throw new Exception(msg);
            }
        }
        return parameters;
    }

    private void printReport(JasperReport jasperReport, MDJasperReports jasperReportsObj, HttpServletResponse res, String formatReport, Map<String, Serializable> parameters, Locale locale, MDConfig config) throws Exception {
        JRExporter export;
        String suffix;
        if (jasperReportsObj.isCsv() && formatReport.equals(Defs.JRPT_CSV)) {
            export = new JRCsvExporter();
            res.setContentType("text/plain; charset=" + Defs.ENCODING_JR);
            suffix = "csv";
        } else if (jasperReportsObj.isHtml() && formatReport.equals(Defs.JRPT_HTML)) {
            res.setContentType("text/html; charset=" + Defs.ENCODING_JR);
            export = new JRHtmlExporter();
            suffix = "html";
        } else if (jasperReportsObj.isPdf() && formatReport.equals(Defs.JRPT_PDF)) {
            export = new JRPdfExporter();
            res.setContentType("application/pdf; charset=" + Defs.ENCODING_JR);
            suffix = "pdf";
        } else if (jasperReportsObj.isRtf() && formatReport.equals(Defs.JRPT_RTF)) {
            export = new JRRtfExporter();
            res.setContentType("application/rtf; charset=" + Defs.ENCODING_JR);
            suffix = "rtf";
        } else if (jasperReportsObj.isTxt() && formatReport.equals(Defs.JRPT_TXT)) {
            export = new JRTextExporter();
            res.setContentType("text/plain; charset=" + Defs.ENCODING_JR);
            suffix = "txt";
        } else if (jasperReportsObj.isXls() && formatReport.equals(Defs.JRPT_XLS)) {
            export = new JRXlsExporter();
            res.setContentType("application/vnd.ms-excel; charset=" + Defs.ENCODING_JR);
            suffix = "xls";
        } else {
            throw new Exception(Util.getLocalizedString(Defs.MDLIBI18N, locale, "Report_type_undefined"));
        }
        Transaction tx = config.getCurrentTransaction();
        JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, parameters, tx.getConnection());
        ByteArrayOutputStream report = new ByteArrayOutputStream();
        export.setParameter(JRExporterParameter.JASPER_PRINT, jasperPrint);
        export.setParameter(JRExporterParameter.OUTPUT_STREAM, report);
        export.setParameter(JRExporterParameter.CHARACTER_ENCODING, Defs.ENCODING_JR);
        if (suffix.equals("csv")) {
            export.setParameter(JRCsvExporterParameter.RECORD_DELIMITER, ",");
        } else if (suffix.equals("txt")) {
            export.setParameter(JRTextExporterParameter.PAGE_HEIGHT, 842);
            export.setParameter(JRTextExporterParameter.PAGE_WIDTH, 595);
        } else if (suffix.equals("xls")) {
            export.setParameter(JRXlsExporterParameter.IS_DETECT_CELL_TYPE, Boolean.TRUE);
            export.setParameter(JRXlsExporterParameter.IS_REMOVE_EMPTY_SPACE_BETWEEN_ROWS, Boolean.TRUE);
            export.setParameter(JRXlsExporterParameter.IS_WHITE_PAGE_BACKGROUND, Boolean.FALSE);
            export.setParameter(JRXlsExporterParameter.IS_ONE_PAGE_PER_SHEET, Boolean.FALSE);
        }
        export.exportReport();
        byte[] bytes = report.toByteArray();
        report.close();
        res.setContentLength(bytes.length);
        String name = jasperReportsObj.getName();
        if (jasperReportsObj.getOwnerClass() != null) {
            name += "-" + parameters.get("id");
        }
        res.setHeader("Content-disposition", (jasperReportsObj.isAttachment() ? "attachment" : "inline") + "; filename=\"" + name + "." + suffix + "\"");
        ServletOutputStream ouputStream = res.getOutputStream();
        ouputStream.write(bytes, 0, bytes.length);
        ouputStream.flush();
        ouputStream.close();
    }
}
