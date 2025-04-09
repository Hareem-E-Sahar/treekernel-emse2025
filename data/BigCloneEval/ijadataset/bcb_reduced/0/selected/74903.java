package au.gov.nla.aons.obsolescence;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.apache.log4j.Logger;
import au.gov.nla.aons.configuration.ConfigurationManager;
import au.gov.nla.aons.constants.AonsFormatStates;
import au.gov.nla.aons.constants.ConfigurationNames;
import au.gov.nla.aons.format.FormatManager;
import au.gov.nla.aons.format.domain.AonsFormat;
import au.gov.nla.aons.format.domain.Format;
import au.gov.nla.aons.obsolescence.dao.ObsolescenceDao;
import au.gov.nla.aons.obsolescence.domain.Question;
import au.gov.nla.aons.obsolescence.domain.QuestionAnswer;
import au.gov.nla.aons.obsolescence.domain.ReviewExpiredEvent;
import au.gov.nla.aons.obsolescence.domain.ReviewReminderEvent;
import au.gov.nla.aons.obsolescence.domain.RiskAssessment;
import au.gov.nla.aons.obsolescence.domain.RiskConfiguration;
import au.gov.nla.aons.obsolescence.domain.Rules;
import au.gov.nla.aons.obsolescence.dto.AnswerDto;
import au.gov.nla.aons.obsolescence.dto.AonsFormatRiskSummaryDto;
import au.gov.nla.aons.obsolescence.dto.BaseFormatRiskSummaryDto;
import au.gov.nla.aons.obsolescence.dto.FormatObsolescenceSummary;
import au.gov.nla.aons.obsolescence.dto.GlobalRiskSummaryDto;
import au.gov.nla.aons.obsolescence.dto.QuestionDto;
import au.gov.nla.aons.obsolescence.dto.RepositoryRiskSummaryDto;
import au.gov.nla.aons.obsolescence.dto.RiskAssessmentDto;
import au.gov.nla.aons.obsolescence.dto.RulesetDto;
import au.gov.nla.aons.obsolescence.dto.SummaryRiskAssessment;
import au.gov.nla.aons.obsolescence.dto.UnIdentifiedFormatRiskSummaryDto;
import au.gov.nla.aons.obsolescence.exceptions.ObsolescenceException;
import au.gov.nla.aons.obsolescence.util.BaseFormatRiskSummaryDtoComparator;
import au.gov.nla.aons.obsolescence.util.ReviewExpiredEventHandler;
import au.gov.nla.aons.obsolescence.util.ReviewReminderEventHandler;
import au.gov.nla.aons.obsolescence.util.RulesLoader;
import au.gov.nla.aons.repository.RepositoryManager;
import au.gov.nla.aons.repository.crawl.AggregateIdentificationMetadataPair;
import au.gov.nla.aons.repository.crawl.RepositoryCollection;
import au.gov.nla.aons.repository.crawl.RepositoryScan;
import au.gov.nla.aons.repository.domain.FormatIdentificationMetadata;
import au.gov.nla.aons.repository.domain.Repository;
import au.gov.nla.aons.repository.dto.ShortRepositoryDto;
import au.gov.nla.aons.schedule.ScheduleManager;
import au.gov.nla.aons.snapshot.exceptions.NoHistoricalForDate;
import au.gov.nla.aons.snapshot.util.HistoricalUtil;

public class ObsolescenceManagerImpl implements ObsolescenceManager {

    private static Logger logger = Logger.getLogger(ObsolescenceManagerImpl.class.getName());

    private ObsolescenceDao obsolescenceDao;

    private RulesLoader loader;

    private FormatManager formatManager;

    private RepositoryManager repositoryManager;

    private HistoricalUtil historicalUtil;

    private ScheduleManager scheduleManager;

    private List<ObsolescenceListener> listeners = new ArrayList<ObsolescenceListener>();

    private ConfigurationManager configurationManager;

    public void initialize() {
        Rules communityRules = obsolescenceDao.retrieveRules("Community");
        if (communityRules == null) {
            communityRules = loader.loadDefaultCommunityRules();
            communityRules.setType("Community");
            communityRules.setValidFrom(Calendar.getInstance());
            obsolescenceDao.createRules(communityRules);
        }
        Rules localRules = obsolescenceDao.retrieveRules("Local");
        if (localRules == null) {
            localRules = loader.loadDefaultLocalRules();
            localRules.setType("Local");
            localRules.setValidFrom(Calendar.getInstance());
            obsolescenceDao.createRules(localRules);
        }
        RiskConfiguration riskConfiguration = (RiskConfiguration) configurationManager.retrieveConfiguration(ConfigurationNames.RISK_CONFIGURATION);
        if (riskConfiguration == null) {
            riskConfiguration = new RiskConfiguration();
            riskConfiguration.setId(ConfigurationNames.RISK_CONFIGURATION);
            riskConfiguration.setRiskAssessmentDuration(3);
            riskConfiguration.setRiskAssessmentReminder(4);
            configurationManager.createConfiguration(riskConfiguration);
        }
        ReviewExpiredEventHandler expiredHandler = new ReviewExpiredEventHandler();
        expiredHandler.setObsolescenceManager(this);
        scheduleManager.registerEventHandler(expiredHandler);
        ReviewReminderEventHandler reminderHandler = new ReviewReminderEventHandler();
        reminderHandler.setObsolescenceManager(this);
        scheduleManager.registerEventHandler(reminderHandler);
    }

    public Rules retrieveRules(String type) {
        return obsolescenceDao.retrieveRules(type);
    }

    public RulesetDto retrieveRulesetQuestionaire(String type) {
        Rules rules = retrieveRules(type);
        if (rules == null) {
            List<Rules> listOfRules = obsolescenceDao.retrieveAllRules();
            String message = "There is no ruleset [" + type + "], available rulesets are: ";
            Iterator<Rules> rulesIter = listOfRules.iterator();
            while (rulesIter.hasNext()) {
                Rules availableRules = (Rules) rulesIter.next();
                message += "[" + availableRules.getType() + "]";
                if (rulesIter.hasNext()) {
                    message += ", ";
                }
            }
            throw new ObsolescenceException(message);
        }
        RulesetDto rulesetDto = new RulesetDto();
        rulesetDto.setName(rules.getName());
        rulesetDto.setVersion(rules.getVersion());
        rulesetDto.setUnderlyingRulesId(rules.getId());
        List<Question> questions = rules.getQuestions();
        if (questions == null) {
            return rulesetDto;
        }
        List<QuestionDto> questionDtos = new ArrayList<QuestionDto>();
        Iterator<Question> questionIter = questions.iterator();
        while (questionIter.hasNext()) {
            Question question = (Question) questionIter.next();
            QuestionDto questionDto = new QuestionDto();
            questionDto.setIndex(question.getIndex());
            questionDto.setText(question.getText());
            questionDto.setUnderlyingQuestionId(question.getId());
            questionDtos.add(questionDto);
        }
        rulesetDto.setQuestions(questionDtos);
        return rulesetDto;
    }

    public Question retrieveQuestion(Long questionId) {
        return obsolescenceDao.retrieveQuestion(questionId);
    }

    /**
     * Creates a new risk assessment for the specified format.
     * 
     * @param riskAssessment
     * 
     * @return identifier of the new risk assessment object
     */
    public Long createRiskAssessment(RiskAssessmentDto communityRiskAssessmentDto, RiskAssessmentDto localRiskAssessmentDto, Long aonsFormatId) {
        AonsFormat aonsFormat = retrieveAonsFormat(aonsFormatId);
        Calendar calendar = Calendar.getInstance();
        if (aonsFormat.getReviewExpiresEventId() != null) {
            scheduleManager.deleteEvent(aonsFormat.getReviewExpiresEventId());
        }
        if (aonsFormat.getReviewReminderEventId() != null) {
            scheduleManager.deleteEvent(aonsFormat.getReviewReminderEventId());
        }
        RiskAssessment riskAssessment = convertRiskAssessmentDtos(communityRiskAssessmentDto, localRiskAssessmentDto, aonsFormat);
        riskAssessment.setAonsFormat(aonsFormat);
        List<RiskAssessment> riskAssessments = aonsFormat.getRiskAssessments();
        historicalUtil.updateHistoricalList(riskAssessments, riskAssessment);
        aonsFormat.setCommunityRisk(riskAssessment.getCommunityAssessment());
        aonsFormat.setLocalRisk(riskAssessment.getLocalAssessment());
        aonsFormat.setFinalRisk(riskAssessment.getFinalAssessment());
        aonsFormat.setLatestRiskAssessment(riskAssessment);
        aonsFormat.setState(AonsFormatStates.RISK_ASSESSMENT_PERFORMED);
        aonsFormat.setReviewPerformedOn(calendar);
        RiskConfiguration riskConfiguration = (RiskConfiguration) configurationManager.retrieveConfiguration(ConfigurationNames.RISK_CONFIGURATION);
        if (riskConfiguration == null) {
            throw new ObsolescenceException("The risk configuration cannot be null, please check that the initialize method was run");
        }
        if (riskConfiguration.getRiskAssessmentDuration() > 0) {
            ReviewExpiredEvent event = new ReviewExpiredEvent();
            Calendar eventDate = Calendar.getInstance();
            eventDate.setTime(calendar.getTime());
            eventDate.add(Calendar.MONTH, riskConfiguration.getRiskAssessmentDuration());
            event.setEventDate(eventDate);
            event.setAonsFormatId(aonsFormat.getId());
            scheduleManager.createEvent(event);
            aonsFormat.setReviewExpiresEventId(event.getId());
            aonsFormat.setReviewExpiresOn(eventDate);
            Integer reminder = riskConfiguration.getRiskAssessmentReminder();
            if (reminder > 0) {
                Calendar reminderEventTime = Calendar.getInstance();
                reminderEventTime.setTime(eventDate.getTime());
                reminderEventTime.add(Calendar.WEEK_OF_YEAR, -reminder);
                aonsFormat.setReviewReminderOn(reminderEventTime);
                ReviewReminderEvent reminderEvent = new ReviewReminderEvent();
                reminderEvent.setAonsFormatId(aonsFormat.getId());
                reminderEvent.setEventDate(reminderEventTime);
                reminderEvent.setExecuted(Boolean.FALSE);
                scheduleManager.createEvent(reminderEvent);
                aonsFormat.setReviewReminderEventId(reminderEvent.getId());
            }
        }
        formatManager.updateFormat(aonsFormat);
        notifyReviewPerformed(aonsFormat);
        return riskAssessment.getId();
    }

    public void handleReviewExpired(ReviewExpiredEvent reviewExpiredEvent) {
        AonsFormat format = (AonsFormat) formatManager.retrieveFormat(reviewExpiredEvent.getAonsFormatId());
        if (format == null) {
            return;
        }
        Calendar calendar = Calendar.getInstance();
        if (format.getState().equals(AonsFormatStates.RISK_ASSESSMENT_PERFORMED) && format.getReviewExpiresOn().compareTo(calendar) < 0) {
            logger.info("Risk assessment expired for format [" + format.getName() + "] with id [" + format.getId() + "]");
            format.setState(AonsFormatStates.RISK_ASSESSMENT_EXPIRED);
            format.setReviewExpiresEventId(null);
            RiskAssessment riskAssessment = new RiskAssessment();
            riskAssessment.setAonsFormat(format);
            riskAssessment.setCommunityAssessment(10f);
            riskAssessment.setCommunityText("Generic Community Risk Assessment for Review Expired");
            riskAssessment.setLocalAssessment(10f);
            riskAssessment.setLocalText("Generic Local Risk Assessment for Review Expired");
            riskAssessment.setFinalAssessment(10f);
            riskAssessment.setValidFrom(calendar);
            format.setLatestRiskAssessment(riskAssessment);
            historicalUtil.updateHistoricalList(format.getRiskAssessments(), riskAssessment, calendar);
            formatManager.updateFormat(format);
            notifyReviewExpired(format);
        }
    }

    public void handleReviewReminder(ReviewReminderEvent reviewReminderEvent) {
        AonsFormat format = (AonsFormat) formatManager.retrieveFormat(reviewReminderEvent.getAonsFormatId());
        if (format == null) {
            return;
        }
        notifyReviewReminder(format);
    }

    protected AonsFormat retrieveAonsFormat(Long aonsFormatId) {
        if (aonsFormatId == null) {
            throw new ObsolescenceException("The Aons format identifier cannot be null.");
        }
        Format format = formatManager.retrieveFormat(aonsFormatId);
        if (format == null) {
            throw new ObsolescenceException("There is no such format given by the id [" + aonsFormatId + "]");
        }
        if (!(format instanceof AonsFormat)) {
            throw new ObsolescenceException("The format with id [" + aonsFormatId + "] is a format, but not of the class AonsFormat. We can only perform RiskAssessments for internal formats which utilise registry formats, not the registry formats themselves.");
        }
        AonsFormat aonsFormat = (AonsFormat) format;
        return aonsFormat;
    }

    private RiskAssessment convertRiskAssessmentDtos(RiskAssessmentDto communityRiskAssessmentDto, RiskAssessmentDto localRiskAssessmentDto, AonsFormat aonsFormat) {
        RiskAssessment riskAssessment = new RiskAssessment();
        Float communityRiskAssessment = communityRiskAssessmentDto.getAssessment();
        Float localRiskAssessment = localRiskAssessmentDto.getAssessment();
        riskAssessment.setCommunityAssessment(communityRiskAssessment);
        List<QuestionAnswer> communityQuestionAnswers = createQuestionAnswerList(communityRiskAssessmentDto);
        riskAssessment.setCommunityQuestionAnswers(communityQuestionAnswers);
        riskAssessment.setCommunityText(communityRiskAssessmentDto.getFreeText());
        riskAssessment.setCommunityRules(obsolescenceDao.retrieveRules(communityRiskAssessmentDto.getRulesId()));
        riskAssessment.setLocalAssessment(localRiskAssessment);
        List<QuestionAnswer> localQuestionAnswers = createQuestionAnswerList(localRiskAssessmentDto);
        riskAssessment.setLocalQuestionAnswers(localQuestionAnswers);
        riskAssessment.setLocalText(localRiskAssessmentDto.getFreeText());
        riskAssessment.setCommunityRules(obsolescenceDao.retrieveRules(communityRiskAssessmentDto.getRulesId()));
        Float finalRiskAssessment = (communityRiskAssessment + localRiskAssessment) / 2;
        riskAssessment.setAonsFormat(aonsFormat);
        riskAssessment.setFinalAssessment(finalRiskAssessment);
        return riskAssessment;
    }

    private List<QuestionAnswer> createQuestionAnswerList(RiskAssessmentDto riskAssessmentDto) {
        Long rulesId = riskAssessmentDto.getRulesId();
        Rules rules = obsolescenceDao.retrieveRules(rulesId);
        Iterator<Question> questionIter = rules.getQuestions().iterator();
        Iterator<AnswerDto> answerDtoIter = riskAssessmentDto.getAnswers().iterator();
        List<QuestionAnswer> questionAnswers = new ArrayList<QuestionAnswer>();
        while (questionIter.hasNext() && answerDtoIter.hasNext()) {
            Question question = questionIter.next();
            AnswerDto answerDto = answerDtoIter.next();
            QuestionAnswer qa = new QuestionAnswer();
            qa.setFreeText(answerDto.getFreeText());
            qa.setQuestionText(question.getText());
            qa.setQuestion(question);
            qa.setRelevant(answerDto.getRelevant());
            questionAnswers.add(qa);
        }
        return questionAnswers;
    }

    public FormatObsolescenceSummary retrieveFormatObsolescenceSummary(Long aonsFormatId) {
        FormatObsolescenceSummary summary = new FormatObsolescenceSummary();
        AonsFormat aonsFormat = retrieveAonsFormat(aonsFormatId);
        summary.setFormatId(aonsFormat.getId());
        summary.setFormatName(aonsFormat.getName());
        summary.setFormatVersion(aonsFormat.getVersion());
        List<RiskAssessment> riskAssessments = aonsFormat.getRiskAssessments();
        List<SummaryRiskAssessment> summaryRiskAssessments = new ArrayList<SummaryRiskAssessment>();
        Iterator<RiskAssessment> riskAssessmentIter = riskAssessments.iterator();
        while (riskAssessmentIter.hasNext()) {
            RiskAssessment riskAssessment = (RiskAssessment) riskAssessmentIter.next();
            SummaryRiskAssessment summaryRiskAssessment = new SummaryRiskAssessment();
            summaryRiskAssessment.setCommunityRisk(riskAssessment.getCommunityAssessment());
            summaryRiskAssessment.setLocalRisk(riskAssessment.getLocalAssessment());
            summaryRiskAssessment.setFinalRisk(riskAssessment.getFinalAssessment());
            summaryRiskAssessment.setReviewDate(riskAssessment.getValidFrom());
            summaryRiskAssessments.add(summaryRiskAssessment);
        }
        summary.setSummaryRiskAssessments(summaryRiskAssessments);
        return summary;
    }

    public GlobalRiskSummaryDto retrieveGlobalRiskSummaryDto() {
        return retrieveHistoricalGlobalRiskSummaryDto(Calendar.getInstance());
    }

    public GlobalRiskSummaryDto retrieveHistoricalGlobalRiskSummaryDto(Calendar date) {
        GlobalRiskSummaryDto globalRiskSummary = new GlobalRiskSummaryDto();
        List<AggregateIdentificationMetadataPair> pairs = new ArrayList<AggregateIdentificationMetadataPair>();
        List<ShortRepositoryDto> shortRepositoryDtos = new ArrayList<ShortRepositoryDto>();
        List<Repository> repositories = repositoryManager.retrieveAllRepositories();
        Iterator<Repository> repositoryIter = repositories.iterator();
        while (repositoryIter.hasNext()) {
            Repository repository = repositoryIter.next();
            if (repository.getRepositoryScans() != null) {
                RepositoryScan scan = historicalUtil.findValidForDate(repository.getRepositoryScans(), null);
                if (scan != null) {
                    shortRepositoryDtos.add(new ShortRepositoryDto(repository));
                    updatePairs(pairs, scan);
                }
            }
        }
        List<BaseFormatRiskSummaryDto> formatRiskSummaryDtos = retrieveFormatRiskSummaries(pairs);
        globalRiskSummary.setFormatRiskSummaries(formatRiskSummaryDtos);
        globalRiskSummary.setShortRepositoryDtos(shortRepositoryDtos);
        globalRiskSummary.setDate(date);
        return globalRiskSummary;
    }

    protected List<BaseFormatRiskSummaryDto> retrieveFormatRiskSummaries(List<AggregateIdentificationMetadataPair> pairs) {
        Map<Long, Long> aonsIdCountMap = new HashMap<Long, Long>();
        Map<Long, AonsFormat> aonsIdAonsFormatMap = new HashMap<Long, AonsFormat>();
        List<BaseFormatRiskSummaryDto> formatRiskSummaryDtos = new ArrayList<BaseFormatRiskSummaryDto>();
        Iterator<AggregateIdentificationMetadataPair> pairIter = pairs.iterator();
        Float totalRiskByQuantity = 0f;
        while (pairIter.hasNext()) {
            AggregateIdentificationMetadataPair pair = pairIter.next();
            FormatIdentificationMetadata formatIdent = pair.getMetadata();
            if (formatIdent.getAonsFormat() == null) {
                UnIdentifiedFormatRiskSummaryDto unIdentifiedFormatRiskSummary = new UnIdentifiedFormatRiskSummaryDto();
                unIdentifiedFormatRiskSummary.setCommunityRisk(10f);
                unIdentifiedFormatRiskSummary.setFinalRisk(10f);
                unIdentifiedFormatRiskSummary.setLocalRisk(10f);
                unIdentifiedFormatRiskSummary.setStatus("UnIdentified");
                unIdentifiedFormatRiskSummary.setFormatIdentId(formatIdent.getId());
                Float multiplacativeRisk = (float) (pair.getCount() * 10f);
                totalRiskByQuantity += multiplacativeRisk;
                unIdentifiedFormatRiskSummary.setMultiplicativeRisk(multiplacativeRisk);
                unIdentifiedFormatRiskSummary.setName(formatIdent.toString());
                unIdentifiedFormatRiskSummary.setQuantity(pair.getCount());
                fillInRiskSectors(unIdentifiedFormatRiskSummary);
                formatRiskSummaryDtos.add(unIdentifiedFormatRiskSummary);
            } else {
                AonsFormat aonsFormat = formatIdent.getAonsFormat();
                if (!aonsIdAonsFormatMap.containsKey(aonsFormat.getId())) {
                    aonsIdAonsFormatMap.put(aonsFormat.getId(), aonsFormat);
                    aonsIdCountMap.put(aonsFormat.getId(), 0L);
                }
                Long currentCount = aonsIdCountMap.get(aonsFormat.getId());
                currentCount = currentCount + pair.getCount();
                aonsIdCountMap.put(aonsFormat.getId(), currentCount);
            }
        }
        Iterator<Long> aonsIdIter = aonsIdAonsFormatMap.keySet().iterator();
        while (aonsIdIter.hasNext()) {
            Long aonsFormatId = (Long) aonsIdIter.next();
            Long count = aonsIdCountMap.get(aonsFormatId);
            AonsFormat aonsFormat = aonsIdAonsFormatMap.get(aonsFormatId);
            AonsFormatRiskSummaryDto aonsFormatRiskSummaryDto = new AonsFormatRiskSummaryDto(aonsFormat, count);
            totalRiskByQuantity += aonsFormatRiskSummaryDto.getMultiplicativeRisk();
            fillInRiskSectors(aonsFormatRiskSummaryDto);
            formatRiskSummaryDtos.add(aonsFormatRiskSummaryDto);
        }
        Iterator<BaseFormatRiskSummaryDto> riskSummaryIter = formatRiskSummaryDtos.iterator();
        while (riskSummaryIter.hasNext()) {
            BaseFormatRiskSummaryDto riskSummary = (BaseFormatRiskSummaryDto) riskSummaryIter.next();
            riskSummary.setMultiplicativeRisk(riskSummary.getMultiplicativeRisk() / totalRiskByQuantity);
        }
        Collections.sort(formatRiskSummaryDtos, new BaseFormatRiskSummaryDtoComparator());
        return formatRiskSummaryDtos;
    }

    public RepositoryRiskSummaryDto retrieveRepositoryRiskSummaryDto(Long repositoryId) {
        return retrieveHistoricalRepositoryRiskSummaryDto(repositoryId, Calendar.getInstance());
    }

    public RepositoryRiskSummaryDto retrieveHistoricalRepositoryRiskSummaryDto(Long repositoryId, Calendar date) {
        Repository repository = repositoryManager.retrieveRepository(repositoryId);
        if (repository.getRepositoryScans() == null || repository.getRepositoryScans().size() == 0) {
            throw new ObsolescenceException("There are no repository scans for the repository with id [" + repositoryId + "]");
        }
        RepositoryScan scan = historicalUtil.findValidForDate(repository.getRepositoryScans(), date);
        if (scan == null) {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss Z");
            throw new NoHistoricalForDate("In the repository with id [" + repository.getId() + "] there isn't a repository scan for the date [" + sdf.format(date.getTime()) + "]", date);
        }
        List<AggregateIdentificationMetadataPair> pairs = scan.getCollection().getPairs();
        List<BaseFormatRiskSummaryDto> formatRiskSummaries = retrieveFormatRiskSummaries(pairs);
        RepositoryRiskSummaryDto repositoryRiskSummaryDto = new RepositoryRiskSummaryDto();
        repositoryRiskSummaryDto.setDate(date);
        repositoryRiskSummaryDto.setCollectionPathInfo(repositoryManager.retrieveCollectionPathInfo(scan.getCollection().getId()));
        repositoryRiskSummaryDto.setFormatRiskSummaries(formatRiskSummaries);
        return repositoryRiskSummaryDto;
    }

    /**
     * Retrieves a risk summary for a collection (a subset of a repository
     * scan).
     * 
     */
    public RepositoryRiskSummaryDto retrieveCollectionRiskSummaryDto(Long collectionId) {
        RepositoryCollection collection = repositoryManager.retrieveCollection(collectionId);
        List<AggregateIdentificationMetadataPair> pairs = collection.getPairs();
        List<BaseFormatRiskSummaryDto> formatRiskSummaries = retrieveFormatRiskSummaries(pairs);
        RepositoryRiskSummaryDto repositoryRiskSummaryDto = new RepositoryRiskSummaryDto();
        repositoryRiskSummaryDto.setDate(null);
        repositoryRiskSummaryDto.setCollectionPathInfo(repositoryManager.retrieveCollectionPathInfo(collection.getId()));
        repositoryRiskSummaryDto.setFormatRiskSummaries(formatRiskSummaries);
        return repositoryRiskSummaryDto;
    }

    /**
     * This just gets the count from the aggregate pair and adds it to the
     * running total. It's a bit bulky for what it does if you ask me: really
     * all we're doing is adding objects which are 'equal' based on their
     * FormatIdentificationMetadata... ah the pains we go through having a
     * domain structure which works well with JPA.
     * 
     * @param pairs
     * @param scan
     */
    private void updatePairs(List<AggregateIdentificationMetadataPair> pairs, RepositoryScan scan) {
        if (scan.getCollection() != null) {
            RepositoryCollection collection = scan.getCollection();
            List<AggregateIdentificationMetadataPair> collectionPairs = collection.getPairs();
            Iterator<AggregateIdentificationMetadataPair> collectionPairIter = collectionPairs.iterator();
            while (collectionPairIter.hasNext()) {
                AggregateIdentificationMetadataPair collectionPair = (AggregateIdentificationMetadataPair) collectionPairIter.next();
                AggregateIdentificationMetadataPair summaryPair = null;
                if (pairs.contains(collectionPair)) {
                    summaryPair = pairs.get(pairs.indexOf(collectionPair));
                } else {
                    summaryPair = new AggregateIdentificationMetadataPair();
                    summaryPair.setMetadata(collectionPair.getMetadata());
                    summaryPair.setCount(0L);
                    pairs.add(summaryPair);
                }
                summaryPair.setCount(summaryPair.getCount() + collectionPair.getCount());
            }
        }
    }

    protected void fillInRiskSectors(BaseFormatRiskSummaryDto formatRiskSummary) {
        formatRiskSummary.setCommunityRiskSector(convertRiskValue(formatRiskSummary.getCommunityRisk()));
        formatRiskSummary.setLocalRiskSector(convertRiskValue(formatRiskSummary.getLocalRisk()));
        formatRiskSummary.setFinalRiskSector(convertRiskValue(formatRiskSummary.getFinalRisk()));
    }

    private String convertRiskValue(Float risk) {
        if (risk <= 0f) {
            return "green";
        } else if (0f < risk && risk <= 4f) {
            return "yellow";
        } else if (4f < risk && risk <= 8f) {
            return "orange";
        } else if (8f < risk) {
            return "red";
        } else {
            throw new RuntimeException("Should not get here.");
        }
    }

    public ObsolescenceDao getObsolescenceDao() {
        return obsolescenceDao;
    }

    public void setObsolescenceDao(ObsolescenceDao obsolescenceDao) {
        this.obsolescenceDao = obsolescenceDao;
    }

    public RulesLoader getLoader() {
        return loader;
    }

    public void setLoader(RulesLoader loader) {
        this.loader = loader;
    }

    public FormatManager getFormatManager() {
        return formatManager;
    }

    public void setFormatManager(FormatManager formatManager) {
        this.formatManager = formatManager;
    }

    public HistoricalUtil getHistoricalUtil() {
        return historicalUtil;
    }

    public void setHistoricalUtil(HistoricalUtil historicalUtil) {
        this.historicalUtil = historicalUtil;
    }

    public RepositoryManager getRepositoryManager() {
        return repositoryManager;
    }

    public void setRepositoryManager(RepositoryManager repositoryManager) {
        this.repositoryManager = repositoryManager;
    }

    public ScheduleManager getScheduleManager() {
        return scheduleManager;
    }

    public void setScheduleManager(ScheduleManager scheduleManager) {
        this.scheduleManager = scheduleManager;
    }

    public void notifyReviewReminder(AonsFormat format) {
        Iterator<ObsolescenceListener> listenerIter = listeners.iterator();
        while (listenerIter.hasNext()) {
            ObsolescenceListener listener = (ObsolescenceListener) listenerIter.next();
            listener.notifyReviewReminder(format);
        }
    }

    public void notifyReviewPerformed(AonsFormat format) {
        Iterator<ObsolescenceListener> listenerIter = listeners.iterator();
        while (listenerIter.hasNext()) {
            ObsolescenceListener listener = (ObsolescenceListener) listenerIter.next();
            listener.notifyReviewPerformed(format);
        }
    }

    public void notifyReviewExpired(AonsFormat format) {
        Iterator<ObsolescenceListener> listenerIter = listeners.iterator();
        while (listenerIter.hasNext()) {
            ObsolescenceListener listener = (ObsolescenceListener) listenerIter.next();
            listener.notifyReviewExpired(format);
        }
    }

    public void addObsolescenceListener(ObsolescenceListener listener) {
        listeners.add(listener);
    }

    public ConfigurationManager getConfigurationManager() {
        return configurationManager;
    }

    public void setConfigurationManager(ConfigurationManager configurationManager) {
        this.configurationManager = configurationManager;
    }
}
