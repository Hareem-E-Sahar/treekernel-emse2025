package com.m4f.web.controller;

import java.io.IOException;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.servlet.http.HttpServletRequest;
import javax.xml.parsers.ParserConfigurationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.xml.sax.SAXException;
import com.m4f.business.domain.CronTaskReport;
import com.m4f.business.domain.Inbox;
import com.m4f.business.domain.MediationService;
import com.m4f.business.domain.Provider;
import com.m4f.business.domain.ResultSearchEmail;
import com.m4f.business.domain.School;
import com.m4f.business.domain.extended.ExtendedCourse;
import com.m4f.business.domain.extended.ExtendedSchool;
import com.m4f.business.domain.extended.FeedCourses;
import com.m4f.business.domain.extended.FeedSchools;
import com.m4f.utils.PageManager;
import com.m4f.utils.StackTraceUtil;
import com.m4f.utils.feeds.events.model.Dump;
import com.m4f.utils.feeds.importer.ProviderImporter;
import com.m4f.utils.feeds.importer.SchoolImporter;
import com.m4f.utils.worker.impl.AppEngineBackendWorker;

@Controller
@RequestMapping("/task")
public class TaskController extends BaseController {

    private static final Logger LOGGER = Logger.getLogger(TaskController.class.getName());

    private static final String EMAIL_DOMAIN_SUFFIX = "@hirubila.appspotmail.com";

    @Autowired
    ProviderImporter providerImporter;

    @Autowired
    SchoolImporter schoolImporter;

    @Autowired
    AppEngineBackendWorker worker;

    public static final int RANGE = 900;

    @RequestMapping(value = "/_feed/mediation/create", method = { RequestMethod.POST, RequestMethod.GET })
    @ResponseStatus(HttpStatus.OK)
    public void generateInternalFeedsByMediationId(@RequestParam(required = true) Long mediationId, @RequestHeader("host") String host) throws Exception {
        LOGGER.info("Creating internal feed for manual mediation with id: " + mediationId);
        final String FRONTEND_HOST = "hirubila.appspot.com";
        Provider provider = null;
        CronTaskReport report = cronTaskReportService.create();
        report.setObject_id(mediationId);
        report.setDate(new Date());
        report.setType(CronTaskReport.TYPE.INTERNAL_FEED);
        try {
            provider = providerService.getProviderByMediationService(mediationId, null);
            MediationService mediationService = mediatorService.getMediationService(mediationId, null);
            LOGGER.info("Mediation name: " + mediationService.getName());
            report.setDescription(new StringBuffer("Internal feed para Srv. mediacion: ").append(mediationService.getName()).toString());
            if (!mediationService.getHasFeed() && provider != null) {
                FeedSchools feedSchools = internalFeedService.createFeedSchools(FRONTEND_HOST, provider, mediationService);
                internalFeedService.saveFeedSchools(feedSchools);
                HashMap<Long, ExtendedSchool> schools = new HashMap<Long, ExtendedSchool>();
                Collection<ExtendedCourse> courses = extendedCourseService.getCoursesByOwner(mediationService.getId(), null, null);
                for (ExtendedCourse course : courses) {
                    ExtendedSchool school = extendedSchoolService.getSchool(course.getSchool(), Locale.getDefault());
                    if (school != null) schools.put(school.getId(), school);
                }
                for (ExtendedSchool school : schools.values()) {
                    FeedCourses feedCourse = internalFeedService.createFeedCourses(FRONTEND_HOST, provider, mediationService, school, this.getAvailableLanguages());
                    internalFeedService.saveFeedCourses(feedCourse);
                }
                report.setResult("OK");
                if (provider != null) {
                    Map<String, String> params = new HashMap<String, String>();
                    params.put("providerId", String.valueOf(provider.getId()));
                    params.put("manual", "true");
                    worker.addWork("provider", "/task/provider/feed", params);
                }
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, StackTraceUtil.getStackTrace(e));
            report.setResult(new StringBuffer("ERROR: ").append(e.getMessage()).toString());
            throw e;
        } finally {
            cronTaskReportService.save(report);
        }
    }

    @RequestMapping(value = "/provider/feed", method = { RequestMethod.POST, RequestMethod.GET })
    @ResponseStatus(HttpStatus.OK)
    public void loadProviderFeed(@RequestParam Long providerId, @RequestParam(required = false, defaultValue = "false") String manual, @RequestParam(required = false, defaultValue = "true") String register) throws ParserConfigurationException, SAXException, IOException, Exception {
        LOGGER.info("Updating provider with id: " + providerId);
        Provider provider = null;
        CronTaskReport report = null;
        Dump dump = null;
        try {
            report = cronTaskReportService.create();
            provider = this.providerService.getProviderById(providerId, null);
            report.setObject_id(providerId);
            report.setDate(new Date());
            if (("true").equals(manual)) {
                report.setType(CronTaskReport.TYPE.INTERNAL_FEED);
            } else {
                report.setType(CronTaskReport.TYPE.PROVIDER_FEED);
            }
            report.setDescription(new StringBuffer("Proveedor: ").append(provider.getName()).toString());
            dump = this.dumpService.createDump();
            String message = "Proceso de importacion del proveedor: " + provider.getName() + " (" + provider.getId() + "-" + " " + ")";
            dump.setDescription(message);
            dump.setLaunched(Calendar.getInstance(new Locale("es")).getTime());
            dump.setOwner(provider.getId());
            dump.setOwnerClass(Provider.class.getName());
            this.dumpService.save(dump);
            providerImporter.importSchools(provider, dump);
            Map<String, String> params = new HashMap<String, String>();
            params.put("providerId", String.valueOf(provider.getId()));
            worker.addWork("school", "/task/provider/schools", params);
            report.setResult("OK");
        } catch (Exception e) {
            report.setResult(new StringBuffer("ERROR: ").append(e.getMessage()).toString());
            throw e;
        } finally {
            if ("true".equals(register)) {
                cronTaskReportService.save(report);
            }
            dumpService.save(dump);
        }
    }

    @RequestMapping(value = "/provider/schools", method = { RequestMethod.POST, RequestMethod.GET })
    @ResponseStatus(HttpStatus.OK)
    public void loadProviderSchoolsFeed(@RequestParam Long providerId) throws ParserConfigurationException, SAXException, IOException, Exception {
        LOGGER.info("Updating provider's schools with id: " + providerId + " Creating tasks...");
        try {
            PageManager<School> paginator = new PageManager<School>();
            long total = schoolService.countSchoolsByProvider(providerId);
            paginator.setOffset(RANGE);
            paginator.setStart(0);
            paginator.setSize(total);
            for (Integer page : paginator.getTotalPagesIterator()) {
                int start = (page - 1) * RANGE;
                int end = (page) * RANGE;
                Collection<School> schools = schoolService.getSchoolsByProvider(providerId, "updated", null, start, end);
                for (School school : schools) {
                    try {
                        Map<String, String> params = new HashMap<String, String>();
                        params.put("schoolId", String.valueOf(school.getId()));
                        worker.addWork("school", "/task/school/feed", params);
                    } catch (Exception e) {
                        LOGGER.warning("Error with school: " + school.getName());
                        LOGGER.warning(StackTraceUtil.getStackTrace(e));
                    }
                }
            }
        } catch (Exception e) {
            LOGGER.severe(StackTraceUtil.getStackTrace(e));
        }
    }

    @RequestMapping(value = "/school/feed", method = { RequestMethod.POST, RequestMethod.GET })
    @ResponseStatus(HttpStatus.OK)
    public void loadSchoolFeed(@RequestParam Long schoolId) throws ParserConfigurationException, SAXException, IOException, Exception {
        LOGGER.info("Updating school with id: " + schoolId);
        Provider provider = null;
        School school = null;
        Dump dump = null;
        CronTaskReport report = null;
        try {
            school = schoolService.getSchool(schoolId, null);
            LOGGER.info("School name: " + school.getName());
            provider = providerService.getProviderById(school.getProvider(), null);
            school = schoolService.getSchool(schoolId, null);
            dump = dumpService.getLastDumpByOwner(school.getProvider());
            report = cronTaskReportService.create();
            report.setObject_id(school.getProvider());
            report.setDate(new Date());
            report.setType(CronTaskReport.TYPE.PROVIDER_SCHOOLS);
            report.setDescription(new StringBuffer("School: ").append(provider.getName()).toString());
            int retries = 1;
            boolean errors = false;
            StringBuffer errorsSb = new StringBuffer();
            while (retries > 0) {
                try {
                    providerImporter.createLoadTask(provider, school, dump);
                    retries = -1;
                } catch (Exception e) {
                    LOGGER.severe(StackTraceUtil.getStackTrace(e));
                    LOGGER.info("" + retries + " Fail importing courses: " + school.getName());
                    retries--;
                    if (!(retries > 0)) {
                        errors = true;
                        errorsSb.append(school.getName()).append(" -> ").append(e.getCause()).append("\n");
                        LOGGER.severe(StackTraceUtil.getStackTrace(e));
                    }
                }
            }
            if (errors) {
                report.setResult("NO OK" + errorsSb.toString());
            } else {
                report.setResult("OK");
            }
        } catch (Exception e) {
            LOGGER.severe(StackTraceUtil.getStackTrace(e));
            report.setResult(new StringBuffer("ERROR: ").append(e.getMessage()).toString());
            throw e;
        }
    }

    @RequestMapping(value = "/internalFeeds/mediation", method = RequestMethod.POST)
    public String generateInternalFeedsByMediationId(@RequestParam(required = true) Long mediationId, @RequestHeader("host") String host, HttpServletRequest request) throws Exception {
        CronTaskReport report = cronTaskReportService.create();
        report.setObject_id(mediationId);
        report.setDate(new Date());
        report.setType(CronTaskReport.TYPE.INTERNAL_FEED);
        try {
            Provider provider = providerService.getProviderByMediationService(mediationId, null);
            MediationService mediationService = mediatorService.getMediationService(mediationId, null);
            report.setDescription(new StringBuffer("Servicio de mediación: ").append(mediationService.getName()).toString());
            if (!mediationService.getHasFeed()) {
                FeedSchools feedSchools = internalFeedService.createFeedSchools(host, provider, mediationService);
                internalFeedService.saveFeedSchools(feedSchools);
                HashMap<Long, ExtendedSchool> schools = new HashMap<Long, ExtendedSchool>();
                Collection<ExtendedCourse> courses = extendedCourseService.getCoursesByOwner(mediationService.getId(), null, null);
                for (ExtendedCourse course : courses) {
                    ExtendedSchool school = extendedSchoolService.getSchool(course.getSchool(), Locale.getDefault());
                    if (school != null) schools.put(school.getId(), school);
                }
                for (ExtendedSchool school : schools.values()) {
                    FeedCourses feedCourse = internalFeedService.createFeedCourses(host, provider, mediationService, school, this.getAvailableLanguages());
                    internalFeedService.saveFeedCourses(feedCourse);
                }
                report.setResult("OK");
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, StackTraceUtil.getStackTrace(e));
            report.setResult(new StringBuffer("ERROR: ").append(e.getMessage()).toString());
            throw e;
        } finally {
            cronTaskReportService.save(report);
        }
        return "task.launched";
    }

    /***************************************************************************************
	 * 
	 * 								SEO CATALOG OPERATIONS
	 * 
	 ***************************************************************************************/
    @RequestMapping(value = "/recovery", method = RequestMethod.POST)
    @ResponseBody
    public String recovery(@RequestParam(required = true) String email) {
        LOGGER.info("SENDING EMAIL WITH PASSWORD TO -> " + email);
        return "task.launched";
    }

    /**
	 * RESULT SEARCH EMAIL
	 */
    @RequestMapping(value = "/sendSearchResult", method = RequestMethod.POST)
    @ResponseBody
    public String sendSearchResultEmail(@RequestParam(required = true) Long resultSearchEmailId) {
        try {
            ResultSearchEmail email = inboxService.getResultSearchEmail(resultSearchEmailId, Locale.getDefault());
            Properties props = new Properties();
            Session session = Session.getDefaultInstance(props, null);
            try {
                Message msg = new MimeMessage(session);
                msg.setFrom(new InternetAddress(new StringBuffer("info").append(EMAIL_DOMAIN_SUFFIX).toString(), this.getMessage("search.result.send.from")));
                msg.addRecipient(Message.RecipientType.TO, new InternetAddress(email.getTo(), email.getToName()));
                msg.setSubject(this.getMessage("search.result.send.subject", email.getFromName()));
                msg.setText(this.getMessage("search.result.send.body", email.getResultLink()).toString());
                Transport.send(msg);
                email.setSent(Boolean.TRUE);
                inboxService.save(email, Locale.getDefault());
            } catch (AddressException e) {
                LOGGER.log(Level.SEVERE, StackTraceUtil.getStackTrace(e));
            } catch (MessagingException e) {
                LOGGER.log(Level.SEVERE, StackTraceUtil.getStackTrace(e));
            }
            LOGGER.info("SENDING SEARCH RESULT TO EMAIL -> " + email.getTo());
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, StackTraceUtil.getStackTrace(e));
            return "common.error";
        }
        return "task.launched";
    }

    /**
	 * INBOX RESPONSE EMAIL
	 */
    @RequestMapping(value = "/sendInboxResponse", method = RequestMethod.POST)
    @ResponseBody
    public String sendInboxResponseEmail(@RequestParam(required = true) Long inboxId) {
        try {
            Locale locale = this.getAvailableLanguages().get(0);
            Inbox messageRes = inboxService.getInbox(inboxId, locale);
            Inbox messageReq = null;
            if (messageRes != null) {
                messageReq = inboxService.getInbox(messageRes.getRelatedId(), locale);
            }
            Properties props = new Properties();
            Session session = Session.getDefaultInstance(props, null);
            try {
                Message msg = new MimeMessage(session);
                msg.setFrom(new InternetAddress(new StringBuffer("admin").append(EMAIL_DOMAIN_SUFFIX).toString(), this.getMessage("suggestion.problem.response.from")));
                msg.addRecipient(Message.RecipientType.TO, new InternetAddress(messageReq.getFrom(), messageReq.getName()));
                msg.setSubject(this.getMessage("suggestion.problem.response.subject", messageReq.getId()));
                msg.setText(new StringBuffer(messageRes.getContent()).append("\n\n\n").append(this.getMessage("suggestion.problem.response.advice")).toString());
                Transport.send(msg);
            } catch (AddressException e) {
                LOGGER.log(Level.SEVERE, StackTraceUtil.getStackTrace(e));
            } catch (MessagingException e) {
                LOGGER.log(Level.SEVERE, StackTraceUtil.getStackTrace(e));
            }
            LOGGER.info("SENDING SEARCH RESULT TO EMAIL -> " + messageReq.getFrom());
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, StackTraceUtil.getStackTrace(e));
            return "common.error";
        }
        return "task.launched";
    }
}
