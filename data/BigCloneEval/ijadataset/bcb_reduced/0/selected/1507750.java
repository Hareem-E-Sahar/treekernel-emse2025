package org.gbif.portal.web.controller.dataset;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.apache.commons.lang.time.DateFormatUtils;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.exception.MethodInvocationException;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.gbif.portal.dto.SearchResultsDTO;
import org.gbif.portal.dto.log.LogStatsDTO;
import org.gbif.portal.dto.resources.DataProviderDTO;
import org.gbif.portal.dto.resources.DataResourceDTO;
import org.gbif.portal.dto.util.SearchConstraints;
import org.gbif.portal.io.ResultsOutputter;
import org.gbif.portal.service.DataResourceManager;
import org.gbif.portal.service.LogManager;
import org.gbif.portal.service.ServiceException;
import org.gbif.portal.util.request.TemplateUtils;
import org.gbif.portal.web.content.Query;
import org.gbif.portal.web.download.Field;
import org.gbif.portal.web.download.FieldFormatter;
import org.gbif.portal.web.download.LogEventField;
import org.gbif.portal.web.download.OutputProcess;
import org.gbif.portal.web.download.SecondaryOutput;
import org.springframework.context.MessageSource;

/**
 * Encapsulates a event log query. Also provides support for outputting the query results.
 * 
 * @author dmartin
 */
public class LogQuery implements Query, OutputProcess, SecondaryOutput {

    protected LogManager logManager;

    protected DataResourceManager dataResourceManager;

    protected MessageSource messageSource;

    protected String providerKey;

    protected DataProviderDTO dataProvider;

    protected String resourceKey;

    protected DataResourceDTO dataResource;

    protected String occurrenceRecordKey;

    protected String taxonConceptKey;

    protected String eventKey;

    protected String eventName;

    protected String minEventKey;

    protected String maxEventKey;

    protected Long logLevel;

    protected String logGroupKey;

    protected Date startDate;

    protected Date endDate;

    protected SearchConstraints searchConstraints = new SearchConstraints(0, 250000);

    protected String queryUrl;

    protected Date queryDate;

    /**
	 * @see org.gbif.portal.web.download.SecondaryOutput#process(java.io.OutputStream)
	 */
    public void process(OutputStream outputStream) throws Exception {
        List<LogStatsDTO> logStats = logManager.getDataResourceLogStatsFor(providerKey, resourceKey, null, occurrenceRecordKey, taxonConceptKey, eventKey, minEventKey, maxEventKey, logLevel, logGroupKey, startDate, endDate);
        outputLogStats(outputStream, logStats);
    }

    /**
	 * Output log stats to the supplied output stream.
	 * 
	 * @param outputStream
	 * @param logStats
	 * @throws ServiceException
	 * @throws ResourceNotFoundException
	 * @throws ParseErrorException
	 * @throws Exception
	 * @throws MethodInvocationException
	 * @throws IOException
	 */
    public void outputLogStats(OutputStream outputStream, List<LogStatsDTO> logStats) throws ServiceException, ResourceNotFoundException, ParseErrorException, Exception, MethodInvocationException, IOException {
        if (outputStream instanceof ZipOutputStream) {
            ((ZipOutputStream) outputStream).putNextEntry(new ZipEntry(getFileName()));
        }
        DataProviderDTO dataProvider = null;
        if (providerKey != null) {
            dataProvider = dataResourceManager.getDataProviderFor(providerKey);
        }
        Map<Integer, LogStatsDTO> eventIdProviderStats = new HashMap<Integer, LogStatsDTO>();
        for (LogStatsDTO logStat : logStats) {
            LogStatsDTO providerLogStats = eventIdProviderStats.get(logStat.getEventId());
            if (providerLogStats == null) {
                providerLogStats = new LogStatsDTO();
                if (dataProvider == null) {
                    DataResourceDTO dataResource = dataResourceManager.getDataResourceFor(logStat.getEntityKey());
                    providerLogStats.setEntityKey(dataResource.getDataProviderKey());
                    providerLogStats.setEntityName(dataResource.getDataProviderName());
                } else {
                    providerLogStats.setEntityKey(dataProvider.getKey());
                    providerLogStats.setEntityName(dataProvider.getName());
                }
                providerLogStats.setEventId(logStat.getEventId());
                providerLogStats.setEventName(logStat.getEventName());
                providerLogStats.setEventCount(new Integer(0));
                eventIdProviderStats.put(providerLogStats.getEventId(), providerLogStats);
            }
            if (logStat.getEventCount() != null) {
                providerLogStats.setEventCount(providerLogStats.getEventCount() + logStat.getEventCount());
            }
            if (logStat.getCount() != null) {
                if (providerLogStats.getCount() == null) {
                    providerLogStats.setCount(logStat.getCount());
                } else {
                    providerLogStats.setCount(providerLogStats.getCount() + logStat.getCount());
                }
            }
        }
        VelocityContext velocityContext = new VelocityContext();
        velocityContext.put("logQuery", this);
        velocityContext.put("date", new DateFormatUtils());
        if (eventIdProviderStats != null) {
            List<LogStatsDTO> providerStats = new ArrayList<LogStatsDTO>();
            for (Integer key : eventIdProviderStats.keySet()) {
                providerStats.add(eventIdProviderStats.get(key));
            }
            Collections.sort(providerStats, new Comparator<LogStatsDTO>() {

                public int compare(LogStatsDTO ls1, LogStatsDTO ls2) {
                    if (!ls1.getEntityName().equals(ls2.getEntityName())) {
                        return ls1.getEntityKey().compareTo(ls2.getEntityName());
                    } else {
                        return ls1.getEventId().compareTo(ls2.getEventId());
                    }
                }
            });
            velocityContext.put("dataProviderStats", providerStats);
        }
        if (dataProvider != null) {
            velocityContext.put("dataProvider", dataProvider);
        }
        velocityContext.put("dataResourceStats", logStats);
        Template template = Velocity.getTemplate("org/gbif/portal/io/logMessageStats.vm");
        template.initDocument();
        LogEventField lef = new LogEventField();
        lef.setFieldName("record.eventId");
        List<Field> downloadFields = new ArrayList<Field>();
        downloadFields.add(lef);
        FieldFormatter ff = new FieldFormatter(downloadFields, messageSource, null, null);
        velocityContext.put("propertyFormatter", ff);
        TemplateUtils tu = new TemplateUtils();
        OutputStreamWriter writer = new OutputStreamWriter(outputStream);
        tu.merge(template, velocityContext, writer);
        writer.flush();
        if (outputStream instanceof ZipOutputStream) {
            addTemplate(outputStream, velocityContext, tu, "org/gbif/portal/io/logMessageStatsHTML.vm", "log-statistics.html");
            addTemplate(outputStream, velocityContext, tu, "org/gbif/portal/io/logMessageReadme.vm", "README.txt");
        }
    }

    /**
	 * Add template to output
	 * 
	 * @param outputStream
	 * @param velocityContext
	 * @param tu
	 * @param templatePath
	 * @param fileName
	 * @throws IOException
	 * @throws ResourceNotFoundException
	 * @throws ParseErrorException
	 * @throws Exception
	 * @throws MethodInvocationException
	 */
    private void addTemplate(OutputStream outputStream, VelocityContext velocityContext, TemplateUtils tu, String templatePath, String fileName) throws IOException, ResourceNotFoundException, ParseErrorException, Exception, MethodInvocationException {
        ((ZipOutputStream) outputStream).closeEntry();
        ((ZipOutputStream) outputStream).putNextEntry(new ZipEntry(fileName));
        Template template = Velocity.getTemplate(templatePath);
        template = Velocity.getTemplate(templatePath);
        template.initDocument();
        OutputStreamWriter writer = new OutputStreamWriter(outputStream);
        tu.merge(template, velocityContext, writer);
        writer.flush();
    }

    /**
	 * @see org.gbif.portal.web.download.SecondaryOutput#getFileName()
	 */
    public String getFileName() {
        return "log-statistics.txt";
    }

    /**
	 * @see org.gbif.portal.web.download.OutputProcess#process(org.gbif.portal.io.ResultsOutputter)
	 */
    public void process(ResultsOutputter resultsOutputter) throws Exception {
        logManager.formatLogMessagesFor(providerKey, resourceKey, null, occurrenceRecordKey, taxonConceptKey, eventKey, minEventKey, maxEventKey, logLevel, logGroupKey, startDate, endDate, getSearchConstraints(), resultsOutputter);
    }

    /**
	 * @see org.gbif.portal.web.download.OutputProcess#setProcessLimit(int)
	 */
    public void setProcessLimit(int maxNoToProcess) {
    }

    /**
	 * @see org.gbif.portal.web.content.Query#execute()
	 */
    public SearchResultsDTO execute() throws ServiceException {
        return logManager.findLogMessagesFor(providerKey, resourceKey, null, occurrenceRecordKey, taxonConceptKey, eventKey, minEventKey, maxEventKey, logLevel, logGroupKey, startDate, endDate, getSearchConstraints());
    }

    /**
	 * @see org.gbif.portal.web.content.Query#getSearchConstraints()
	 */
    public SearchConstraints getSearchConstraints() {
        return searchConstraints;
    }

    /**
	 * @param logManager the logManager to set
	 */
    public void setLogManager(LogManager logManager) {
        this.logManager = logManager;
    }

    /**
	 * @param endDate the endDate to set
	 */
    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    /**
	 * @param eventKey the eventKey to set
	 */
    public void setEventKey(String eventKey) {
        this.eventKey = eventKey;
    }

    /**
	 * @param logGroupKey the logGroupKey to set
	 */
    public void setLogGroupKey(String logGroupKey) {
        this.logGroupKey = logGroupKey;
    }

    /**
	 * @param logLevel the logLevel to set
	 */
    public void setLogLevel(Long logLevel) {
        this.logLevel = logLevel;
    }

    /**
	 * @param maxEventKey the maxEventKey to set
	 */
    public void setMaxEventKey(String maxEventKey) {
        this.maxEventKey = maxEventKey;
    }

    /**
	 * @param minEventKey the minEventKey to set
	 */
    public void setMinEventKey(String minEventKey) {
        this.minEventKey = minEventKey;
    }

    /**
	 * @param occurrenceRecordKey the occurrenceRecordKey to set
	 */
    public void setOccurrenceRecordKey(String occurrenceRecordKey) {
        this.occurrenceRecordKey = occurrenceRecordKey;
    }

    /**
	 * @param providerKey the providerKey to set
	 */
    public void setProviderKey(String providerKey) {
        this.providerKey = providerKey;
    }

    /**
	 * @param resourceKey the resourceKey to set
	 */
    public void setResourceKey(String resourceKey) {
        this.resourceKey = resourceKey;
    }

    /**
	 * @param searchConstraints the searchConstraints to set
	 */
    public void setSearchConstraints(SearchConstraints searchConstraints) {
        this.searchConstraints = searchConstraints;
    }

    /**
	 * @param startDate the startDate to set
	 */
    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    /**
	 * @param taxonConceptKey the taxonConceptKey to set
	 */
    public void setTaxonConceptKey(String taxonConceptKey) {
        this.taxonConceptKey = taxonConceptKey;
    }

    /**
	 * @param dataResourceManager the dataResourceManager to set
	 */
    public void setDataResourceManager(DataResourceManager dataResourceManager) {
        this.dataResourceManager = dataResourceManager;
    }

    /**
	 * @return the messageSource
	 */
    public MessageSource getMessageSource() {
        return messageSource;
    }

    /**
	 * @param messageSource the messageSource to set
	 */
    public void setMessageSource(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    /**
	 * @return the dataResourceManager
	 */
    public DataResourceManager getDataResourceManager() {
        return dataResourceManager;
    }

    /**
	 * @return the endDate
	 */
    public Date getEndDate() {
        return endDate;
    }

    /**
	 * @return the eventKey
	 */
    public String getEventKey() {
        return eventKey;
    }

    /**
	 * @return the logGroupKey
	 */
    public String getLogGroupKey() {
        return logGroupKey;
    }

    /**
	 * @return the logLevel
	 */
    public Long getLogLevel() {
        return logLevel;
    }

    /**
	 * @return the logManager
	 */
    public LogManager getLogManager() {
        return logManager;
    }

    /**
	 * @return the maxEventKey
	 */
    public String getMaxEventKey() {
        return maxEventKey;
    }

    /**
	 * @return the minEventKey
	 */
    public String getMinEventKey() {
        return minEventKey;
    }

    /**
	 * @return the occurrenceRecordKey
	 */
    public String getOccurrenceRecordKey() {
        return occurrenceRecordKey;
    }

    /**
	 * @return the providerKey
	 */
    public String getProviderKey() {
        return providerKey;
    }

    /**
	 * @return the resourceKey
	 */
    public String getResourceKey() {
        return resourceKey;
    }

    /**
	 * @return the startDate
	 */
    public Date getStartDate() {
        return startDate;
    }

    /**
	 * @return the taxonConceptKey
	 */
    public String getTaxonConceptKey() {
        return taxonConceptKey;
    }

    /**
	 * @return the dataProvider
	 */
    public DataProviderDTO getDataProvider() {
        return dataProvider;
    }

    /**
	 * @param dataProvider the dataProvider to set
	 */
    public void setDataProvider(DataProviderDTO dataProvider) {
        this.dataProvider = dataProvider;
    }

    /**
	 * @return the dataResource
	 */
    public DataResourceDTO getDataResource() {
        return dataResource;
    }

    /**
	 * @param dataResource the dataResource to set
	 */
    public void setDataResource(DataResourceDTO dataResource) {
        this.dataResource = dataResource;
    }

    /**
	 * @return the queryUrl
	 */
    public String getQueryUrl() {
        return queryUrl;
    }

    /**
	 * @param queryUrl the queryUrl to set
	 */
    public void setQueryUrl(String queryUrl) {
        this.queryUrl = queryUrl;
    }

    /**
	 * @return the queryDate
	 */
    public Date getQueryDate() {
        return queryDate;
    }

    /**
	 * @param queryDate the queryDate to set
	 */
    public void setQueryDate(Date queryDate) {
        this.queryDate = queryDate;
    }

    /**
	 * @return the eventName
	 */
    public String getEventName() {
        return eventName;
    }

    /**
	 * @param eventName the eventName to set
	 */
    public void setEventName(String eventName) {
        this.eventName = eventName;
    }
}
