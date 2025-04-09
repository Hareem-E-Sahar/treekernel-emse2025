package org.mitre.rt.client.xml;

import java.awt.Desktop;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.math.BigInteger;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import javax.swing.JOptionPane;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import org.apache.log4j.Logger;
import org.apache.xmlbeans.XmlObject;
import org.mitre.rt.common.xml.XSLProcessor;
import org.mitre.rt.client.core.DataManager;
import org.mitre.rt.client.core.IDManager;
import org.mitre.rt.client.core.MetaManager;
import org.mitre.rt.client.properties.RTClientProperties;
import org.mitre.rt.client.exceptions.DataManagerException;
import org.mitre.rt.client.exceptions.RTClientException;
import org.mitre.rt.rtclient.ApplicationType;
import org.mitre.rt.rtclient.ApplicationType.Recommendations;
import org.mitre.rt.rtclient.ChangeTypeEnum;
import org.mitre.rt.rtclient.CommentType;
import org.mitre.rt.rtclient.ComplexComplianceCheckType;
import org.mitre.rt.rtclient.UserType;
import org.mitre.rt.rtclient.ComplianceCheckType;
import org.mitre.rt.rtclient.DeleteableSharedIdValuePairType;
import org.mitre.rt.rtclient.FixType;
import org.mitre.rt.rtclient.GroupType;
import org.mitre.rt.rtclient.GroupsType;
import org.mitre.rt.rtclient.OrderedDeleteableSharedIdValuePairType;
import org.mitre.rt.rtclient.RecommendationType;
import org.mitre.rt.rtclient.SharedIdType;
import org.mitre.rt.client.ui.recommendations.RecommendationsTableModel;
import org.mitre.rt.client.ui.html.ViewHtmlDialog;
import org.mitre.rt.client.ui.recommendations.AddEditRuleMainDialog;
import org.mitre.rt.client.util.DateUtils;
import org.mitre.rt.client.util.GlobalUITools;
import org.mitre.rt.rtclient.RecommendationType.*;
import org.mitre.rt.client.ui.comments.*;
import org.mitre.rt.client.synchronize.transactions.SynchronizeTransactionUtilities;
import org.mitre.rt.rtclient.OrderedSharedIdType;
import org.mitre.rt.rtclient.OrderedSharedIdsType;
import org.mitre.rt.rtclient.RecommendationReferenceType;
import org.mitre.rt.rtclient.RecommendationReferencesType;
import org.mitre.rt.rtclient.ProfileType;
import org.mitre.rt.rtclient.RuleSelectorType;
import org.mitre.rt.rtclient.SharedIdsType;

/**
 *
 * @author BAKERJ
 */
public class RecommendationHelper extends AbsHelper<RecommendationType, ApplicationType> {

    private static Logger logger = Logger.getLogger(RecommendationHelper.class.getPackage().getName());

    private static final Pattern idPattern = Pattern.compile("^.+-(\\d+-\\d+)$");

    public RecommendationHelper() {
        super("RecommendationReference");
    }

    @Override
    public boolean isDeleted(RecommendationType rec) {
        return (rec.isSetDeleted()) ? rec.getDeleted() : false;
    }

    /**
     * Return the string value for the Recommendation's current statusId
     * @param application
     * @param recommendation
     * @return
     */
    public String getRuleStatus(ApplicationType application, String statusId) {
        if (!application.isSetRuleStatuses()) {
            logger.debug("This application does not use recommendation statuses.");
            return "";
        } else {
            String wfStatusName = "";
            List<OrderedDeleteableSharedIdValuePairType> wfStatusList = application.getRuleStatuses().getItemList();
            for (OrderedDeleteableSharedIdValuePairType wfStatus : wfStatusList) {
                if (wfStatus.getId().equals(statusId)) {
                    wfStatusName = wfStatus.getStringValue();
                    break;
                }
            }
            return wfStatusName;
        }
    }

    /**
     * Return the OrderedDeleteableSharedIdValuePairType Object for the Recommendation's current statusId
     * @param application
     * @param recommendation
     * @return
     */
    public OrderedDeleteableSharedIdValuePairType getStatusObj(ApplicationType application, RecommendationType recommendation) {
        OrderedDeleteableSharedIdValuePairType statusName = null;
        if (application.getRuleStatuses() == null) {
            logger.debug("This application does not use recommendation statuses.");
        } else {
            if (recommendation.isSetStatusId()) {
                String statusId = recommendation.getStatusId();
                List<OrderedDeleteableSharedIdValuePairType> statusList = application.getRuleStatuses().getItemList();
                for (OrderedDeleteableSharedIdValuePairType status : statusList) {
                    if (status.getId().equals(statusId)) {
                        statusName = status;
                        break;
                    }
                }
            }
        }
        return statusName;
    }

    /**
     * Return the string value for the Recommendation's current category Id
     * @param application
     * @param recommendation
     * @return
     */
    public String getCategory(ApplicationType application, RecommendationType recommendation) {
        if (application.getCategories() == null) {
            return "";
        } else {
            if (recommendation.isSetCategoryId()) {
                String catId = recommendation.getCategoryId();
                String catName = "";
                List<DeleteableSharedIdValuePairType> catList = application.getCategories().getItemList();
                for (DeleteableSharedIdValuePairType category : catList) {
                    if (category.getId().equals(catId)) {
                        catName = category.getStringValue();
                        break;
                    }
                }
                return catName;
            } else {
                return "";
            }
        }
    }

    /**
     * Return the Group that this recommendation belongs to. If the Recommendation is not in a 
     * group return null.
     * 
     * @param recommendation
     * @return
     */
    public GroupType getGroupType(ApplicationType application, RecommendationType recommendation) {
        GroupType group = null;
        String recId = recommendation.getId();
        if (application.isSetGroups()) {
            GroupsType groups = application.getGroups();
            List<GroupType> groupList = groups.getGroupList();
            for (GroupType tmpGroup : groupList) {
                if (tmpGroup.isSetRecommendations()) {
                    RecommendationReferencesType recRefs = tmpGroup.getRecommendations();
                    List<RecommendationReferenceType> recRefList = recRefs.getRecommendationReferenceList();
                    for (RecommendationReferenceType recRef : recRefList) {
                        if (recRef.getStringValue().equals(recId)) {
                            return tmpGroup;
                        }
                    }
                }
            }
        }
        return group;
    }

    /**
     * Convenience method to return the ID (the number after the hyphen) of the userFriendlyId.
     * 
     * @param userFriendlyId
     * @return
     */
    public String getIdFromUserFriendlyId(String userFriendlyId) {
        String id = "";
        Matcher matcher = RecommendationHelper.idPattern.matcher(userFriendlyId);
        if (matcher.find() == true) {
            id = matcher.group(1);
        }
        return id;
    }

    /**
     *  Return a list of user friendly ids for all recommendations of the given application.
     * 
     * @param appType
     * @return
     * @throws org.mitre.rt.exceptions.DataManagerException
     */
    public String[] getUserFriendlyRecIdsForApplications(ApplicationType appType) throws DataManagerException {
        String[] recIds = null;
        List<RecommendationType> recList = appType.getRecommendations().getRecommendationList();
        recIds = new String[recList.size()];
        for (int i = 0; i < recList.size(); i++) {
            RecommendationType recs = recList.get(i);
            recIds[i] = appType.getAbbr() + "-" + recs.getId();
        }
        return recIds;
    }

    /**
     * Insert a CommentType object into the recommendation's array of comments.
     * 
     * @param recommendation
     * @return the inserted comment.
     */
    public void insertComment(ApplicationType app, RecommendationType recommendation, CommentType comment) {
        CommentRefs commentRefs = (recommendation.isSetCommentRefs() == true) ? recommendation.getCommentRefs() : recommendation.addNewCommentRefs();
        ApplicationHelper helper = new ApplicationHelper();
        helper.insertNewComment(app, comment);
        SharedIdType commentRef = commentRefs.addNewCommentRef();
        commentRef.setStringValue(comment.getId());
    }

    /**
     * Remove all dependencies from the recommendation
     * 
       * @param recommendation
     */
    public void clearDependencies(RecommendationType recommendation) throws RTClientException, DataManagerException {
        logger.info("Removing all rule dependency from rule " + recommendation.getId());
        while (!recommendation.getDependencyList().isEmpty()) {
            recommendation.removeDependency(0);
        }
    }

    /**
     * Remove given dependencies from the recommendation
     * 
     * @param recommendation
     */
    public void removeDependency(ApplicationType application, RecommendationType recommendation, RecommendationType recDep) throws RTClientException, DataManagerException {
        List<RecommendationType> deps = this.getDependencies(application, recommendation);
        for (int i = 0; i < deps.size(); i++) {
            if (recDep.getId().equals(deps.get(i).getId())) {
                logger.info("Removing rule dependency " + recDep.getId() + " from rule " + recommendation.getId());
                recommendation.removeDependency(i);
            }
        }
    }

    /**
     * Remove given group dependency from the recommendation
     *
     * @param recommendation
     */
    public void removeDependency(ApplicationType application, RecommendationType recommendation, GroupType groupDep) throws RTClientException, DataManagerException {
        List<GroupType> deps = this.getGroupDependencies(application, recommendation);
        for (int i = 0; i < deps.size(); i++) {
            if (groupDep.getId().equals(deps.get(i).getId())) {
                logger.info("Removing group dependency " + groupDep.getId() + " from rule " + recommendation.getId());
                recommendation.removeGroupDependency(i);
            }
        }
    }

    /**
     * Add a dependency to the recommendation. depRecommendation is added to the 
     * specified recommendation.
     * 
     * @param recommendation
     * @param depRecommendation
     */
    public void addDependency(RecommendationType recommendation, RecommendationType depRecommendation) {
        logger.info("Adding rule dependency " + depRecommendation.getId() + " to rule " + recommendation.getId());
        recommendation.addDependency(depRecommendation.getId());
    }

    /**
     * Add a group dependency to the recommendation.
     *
     * @param recommendation
     * @param depGroup
     */
    public void addDependency(RecommendationType recommendation, GroupType depGroup) {
        logger.info("Adding group dependency " + depGroup.getId() + " to rule " + recommendation.getId());
        recommendation.addGroupDependency(depGroup.getId());
    }

    /**
 * Returns a list of recommendations that can be added as a dependency for the given recommendation.
 * Recommendations that are already a dependency and recommendations that introduce circular dependencies
 * will be omitted from the returned list
 * @param app
 * @param rec
 * @return
 */
    public List<RecommendationType> getDependencyCandidates(ApplicationType app, RecommendationType rec) {
        List<RecommendationType> candidates = null;
        List<RecommendationType> activeRecs = super.getActiveItems(app.getRecommendations().getRecommendationList());
        if (activeRecs.isEmpty()) {
            candidates = Collections.emptyList();
        } else {
            List<RecommendationType> deps = this.getDependencies(app, rec);
            candidates = new ArrayList<RecommendationType>(activeRecs.size() - deps.size());
            List<String> depIds = new ArrayList<String>(deps.size());
            for (RecommendationType dep : deps) {
                depIds.add(dep.getId());
            }
            for (RecommendationType depCandidate : activeRecs) {
                if (depIds.contains(depCandidate.getId()) == false && isCircular(app, depCandidate, rec) == false) {
                    candidates.add(depCandidate);
                }
            }
        }
        return candidates;
    }

    /**
 * Returns a list of groups that can be added as a dependency for the given recommendation.
 * Groups that are already a dependency and groups that introduce circular dependencies
 * will be omitted from the returned list
 * @param app
 * @param rec
 * @return
 */
    public List<GroupType> getGroupDependencyCandidates(ApplicationType app, RecommendationType rule) {
        List<GroupType> candidates = null;
        GroupHelper groupHelper = new GroupHelper();
        List<GroupType> activeGroups = groupHelper.getActiveItems(app.getGroups().getGroupList());
        if (activeGroups.isEmpty()) {
            candidates = Collections.emptyList();
        } else {
            List<GroupType> deps = this.getGroupDependencies(app, rule);
            candidates = new ArrayList<GroupType>(activeGroups.size() - deps.size());
            List<String> depIds = new ArrayList<String>(deps.size());
            for (GroupType dep : deps) {
                depIds.add(dep.getId());
            }
            for (GroupType depCandidate : activeGroups) {
                logger.debug("getGroupDependencyCandidates: checking to see if rule " + rule.getId() + " has circular depedency with group " + depCandidate.getId());
                if (!depIds.contains(depCandidate.getId()) && !groupHelper.isCircular(app, depCandidate, rule)) {
                    candidates.add(depCandidate);
                }
            }
        }
        return candidates;
    }

    /**
     * Recursive function to check the recommendations that a rec depends on to 
     * see if the test rec is in the chain
     * @param application
     * @param topRec walk the recommendations that this rec depends on to see if testRec is in there somewhere
     * @param testRec the recommendation in question
     * @return
     */
    public boolean isCircular(ApplicationType application, RecommendationType topRec, RecommendationType testRec) {
        boolean circular = false;
        if (topRec.getId().equals(testRec.getId())) return true;
        List<RecommendationType> deps = this.getDependencies(application, topRec);
        for (RecommendationType dependent : deps) {
            if (dependent.getId().equals(testRec.getId())) {
                logger.debug("isCircular [r,r] rule " + dependent.getId() + " has circular dependency with  rule " + topRec.getId());
                return true;
            } else {
                circular = isCircular(application, dependent, testRec);
                if (circular) {
                    logger.debug("isCircular [r,r] rule " + dependent.getId() + " has circular dependency with  rule " + topRec.getId());
                    return true;
                }
            }
        }
        GroupHelper groupHelper = new GroupHelper();
        List<GroupType> groupDeps = this.getGroupDependencies(application, topRec);
        for (GroupType dependent : groupDeps) {
            circular = groupHelper.isCircular(application, dependent, testRec);
            if (circular) {
                logger.debug("isCircular [r,r] group " + dependent.getId() + " has circular dependency with  rule " + topRec.getId());
                return true;
            }
        }
        return circular;
    }

    /**
     * Recursive function to check the groups and recommendations that a rec depends on to
     * see if the test group is in the chain
     * @param application
     * @param topRec walk the rules and groups that this rule depends on to see if testRec is in there somewhere
     * @param testGroup the group in question
     * @return
     */
    public boolean isCircular(ApplicationType application, RecommendationType topRec, GroupType testGroup) {
        boolean circular = false;
        List<RecommendationType> deps = this.getDependencies(application, topRec);
        for (RecommendationType dependent : deps) {
            circular = isCircular(application, dependent, testGroup);
            if (circular) {
                logger.debug("isCircular [r,g] rule " + dependent.getId() + " has circular dependency with  rule " + topRec.getId());
                return true;
            }
        }
        GroupHelper groupHelper = new GroupHelper();
        List<GroupType> groupDeps = this.getGroupDependencies(application, topRec);
        for (GroupType dependent : groupDeps) {
            if (dependent.getId().equals(testGroup.getId())) {
                logger.debug("isCircular [r,g] group " + dependent.getId() + " has circular dependency with  rule " + topRec.getId());
                return true;
            } else {
                circular = groupHelper.isCircular(application, dependent, testGroup);
                if (circular) {
                    logger.debug("isCircular [r,g] group " + dependent.getId() + " has circular dependency with  rule " + topRec.getId());
                    return true;
                }
            }
        }
        return circular;
    }

    /**
     * Get an array of the dependencies curently set on the specified Recommendation.
     * @param recommendation
     * @return
     */
    public List<RecommendationType> getDependencies(ApplicationType application, RecommendationType recommendation) {
        if (!application.isSetRecommendations()) {
            return Collections.emptyList();
        } else {
            final List<String> depList = recommendation.getDependencyList();
            final List<RecommendationType> deps = new LinkedList<RecommendationType>(), allRecs = application.getRecommendations().getRecommendationList();
            for (int i = 0; i < depList.size(); i++) {
                RecommendationType tmp = super.getItem(allRecs, depList.get(i));
                if (tmp != null && this.isDeleted(tmp) == false) {
                    deps.add(tmp);
                }
            }
            return deps;
        }
    }

    /**
     * Get an array of the group dependencies currently set on the specified Recommendation.
     * @param recommendation
     * @return
     */
    public List<GroupType> getGroupDependencies(ApplicationType application, RecommendationType recommendation) {
        if (!application.isSetGroups()) {
            return Collections.emptyList();
        } else {
            final GroupHelper groupHelper = new GroupHelper();
            final List<String> depList = recommendation.getGroupDependencyList();
            final List<GroupType> deps = new ArrayList<GroupType>(depList.size()), allGroups = application.getGroups().getGroupList();
            for (String depId : depList) {
                GroupType tmp = groupHelper.getItem(allGroups, depId);
                if (tmp != null) {
                    deps.add(tmp);
                }
            }
            return deps;
        }
    }

    /**
     * Add a how to to the recommendation. 
     * 
     * @param recommendation
     * @param howTo
     */
    public void addHowTo(RecommendationType recommendation, FixType howTo) {
        recommendation.setHowToRef(howTo.getId());
        super.markModified(recommendation);
    }

    /**
     * If there is a HowTo set on the Recommendation remove it and mark the how to as deleted.
     * 
     * @param application
     * @param recommendation
     */
    public void removeHowTo(ApplicationType application, RecommendationType recommendation) throws RTClientException {
        FixTypeHelper fixHelper = new FixTypeHelper();
        if (recommendation.isSetHowToRef()) {
            FixType howTo = fixHelper.getItem(application.getHowTos().getHowToList(), recommendation.getHowToRef());
            fixHelper.markDeleted(application, howTo);
            recommendation.unsetHowToRef();
            logger.debug("Unsetting how to in rule " + recommendation.getId());
            super.markModified(recommendation);
        }
    }

    /**
     * Add a compliance check to the recommendation. 
     * 
     * @param recommendation
     * @param complianceCheck
     */
    public void addComplianceCheck(RecommendationType recommendation, ComplianceCheckType complianceCheck) {
        if (!recommendation.isSetComplianceCheckRefs()) {
            recommendation.addNewComplianceCheckRefs();
        }
        ComplianceCheckRefs refs = recommendation.getComplianceCheckRefs();
        refs.addComplianceCheckRef(complianceCheck.getId());
        logger.debug("addComplianceCheck - added " + complianceCheck.getId() + " " + complianceCheck.getTitle());
        super.markModified(recommendation);
    }

    public void addComplexCheckReference(ApplicationType app, RecommendationType rule, String complexCheckId) {
        if (!rule.isSetComplexComplianceCheckRefs()) {
            rule.addNewComplexComplianceCheckRefs();
        }
        SharedIdsType refs = rule.getComplexComplianceCheckRefs();
        refs.addSharedId(complexCheckId);
        super.markModified(rule);
    }

    /**
     * If the specified ComplianceCheck is referenced on the Recommendation 
     * remove it and mark the ComplianceCheck as deleted.
     * 
     * @param application
     * @param recommendation
     */
    public void removeComplianceCheck(ApplicationType application, RecommendationType recommendation, ComplianceCheckType complianceCheck) throws RTClientException {
        if (application == null || recommendation == null || complianceCheck == null) {
            logger.error("removeComplianceCheck input error. app=" + application + " rule=" + recommendation + " complianceCheck=" + complianceCheck);
        }
        if (recommendation.isSetComplianceCheckRefs()) {
            super.removeReference(recommendation.getComplianceCheckRefs().xgetComplianceCheckRefList(), complianceCheck.getId());
            if (recommendation.getComplianceCheckRefs().xgetComplianceCheckRefList().isEmpty()) {
                recommendation.unsetComplianceCheckRefs();
            }
            super.markModified(recommendation);
        }
    }

    public void removeComplexComplianceCheck(ApplicationType application, RecommendationType recommendation, ComplexComplianceCheckType complexCheck) throws RTClientException {
        if (application == null || recommendation == null || complexCheck == null) {
            logger.error("removeComplexComplianceCheck input error. app=" + application + " rule=" + recommendation + " complexCheck=" + complexCheck);
        }
        if (recommendation.isSetComplexComplianceCheckRefs()) {
            super.removeReference(recommendation.getComplexComplianceCheckRefs().xgetSharedIdList(), complexCheck.getId());
            if (recommendation.getComplexComplianceCheckRefs().xgetSharedIdList().isEmpty()) {
                recommendation.unsetComplexComplianceCheckRefs();
            }
            super.markModified(recommendation);
        }
    }

    public List<RecommendationType> getDependents(ApplicationType application, RecommendationType recommendation) {
        List<RecommendationType> dependants = null;
        Recommendations recs = application.getRecommendations();
        String id = recommendation.getId();
        XmlObject[] objs = recs.selectPath("$this/*:Recommendation[*:Dependency = '" + id + "']");
        if (objs.length != 0) {
            dependants = new ArrayList<RecommendationType>(objs.length);
            for (XmlObject obj : objs) {
                dependants.add((RecommendationType) obj);
            }
        } else dependants = Collections.emptyList();
        return dependants;
    }

    public List<GroupType> getGroupDependents(ApplicationType application, RecommendationType recommendation) {
        List<GroupType> dependants = null;
        GroupsType groups = application.getGroups();
        String id = recommendation.getId();
        XmlObject[] objs = groups.selectPath("$this/*:Group[*:Dependency = '" + id + "']");
        if (objs.length != 0) {
            dependants = new ArrayList<GroupType>(objs.length);
            for (XmlObject obj : objs) {
                dependants.add((GroupType) obj);
            }
        } else dependants = Collections.emptyList();
        return dependants;
    }

    /**
     * Return true is the input recopmmendation is in a group.
     * 
     * @param application
     * @param recommendation
     * @return
     */
    public boolean isInAGroup(ApplicationType application, RecommendationType recommendation) {
        return (this.getGroupType(application, recommendation) != null);
    }

    /**
     *
     * @param app
     * @return the list of top level rules. Rules that are not members of any group.
     */
    public List<RecommendationType> getUnGroupedRules(ApplicationType app) {
        ArrayList<RecommendationType> unGroupedRules = new ArrayList<RecommendationType>();
        if (app.isSetRecommendations()) {
            for (RecommendationType rec : app.getRecommendations().getRecommendationList()) {
                if (!isInAGroup(app, rec)) {
                    unGroupedRules.add(rec);
                }
            }
        }
        return unGroupedRules;
    }

    public List<RecommendationType> getGroupedRules(ApplicationType app) {
        RecommendationHelper recHelper = new RecommendationHelper();
        List<RecommendationType> groupedRules = new ArrayList<RecommendationType>();
        if (app.isSetRecommendations()) {
            List<RecommendationType> allRecs = app.getRecommendations().getRecommendationList();
            if (app.isSetGroups()) {
                List<GroupType> allGroups = app.getGroups().getGroupList();
                for (GroupType group : allGroups) {
                    if (group.isSetRecommendations()) {
                        List<RecommendationReferenceType> recReferences = group.getRecommendations().getRecommendationReferenceList();
                        for (RecommendationReferenceType ref : recReferences) {
                            RecommendationType rec = recHelper.getItem(allRecs, ref.getStringValue());
                            if (this.isDeleted(rec) == false) {
                                groupedRules.add(rec);
                            }
                        }
                    }
                }
            }
        }
        return groupedRules;
    }

    public void undoSoftDelete(ApplicationType application, RecommendationType recommendation) {
        recommendation.setDeleted(false);
        super.markModified(recommendation);
        if (this.isInAGroup(application, recommendation) == false) {
            this.addRecommendationOrderItem(application, recommendation);
        }
    }

    /**
   * + iterate over server list, removing references to changetype=deleted groups
   * + iterate over our list, remove references to objects which no longer exist
   * + iterate over our list, add items to server list which are not found in server list
   *
   * @param app
   * @param newOrderNumbers
   * @return
   */
    public boolean mergeOrderNumbers(ApplicationType app, OrderedSharedIdsType newOrderNumbers) {
        boolean changed = false;
        OrderedSharedIdsType currentOrderNumbers = app.getRecommendationOrderNumbers();
        if (currentOrderNumbers.getChangeType().equals(ChangeTypeEnum.NONE) == false) {
            List<OrderedSharedIdType> newOrderNumbersList = newOrderNumbers.getOrderedItemList(), currentOrderNumbersList = currentOrderNumbers.getOrderedItemList();
            List<RecommendationType> allItems = app.getRecommendations().getRecommendationList(), allSubItems = this.getGroupedRules(app);
            for (OrderedSharedIdType item : currentOrderNumbersList) {
                String id = item.getStringValue();
                RecommendationType rec = this.getItem(allItems, id);
                if (rec != null && rec.getChangeType().equals(ChangeTypeEnum.NEW)) {
                    newOrderNumbersList.add(item);
                    changed = true;
                }
            }
            int index = 0;
            while (index < newOrderNumbersList.size()) {
                OrderedSharedIdType item = newOrderNumbersList.get(index);
                String id = item.getStringValue();
                RecommendationType rec = this.getItem(allItems, id);
                if (rec == null || this.isDeleted(rec) || this.getItem(allSubItems, id) != null) {
                    newOrderNumbersList.remove(index);
                    changed = true;
                } else index++;
            }
        }
        currentOrderNumbers.set(newOrderNumbers);
        return changed;
    }

    /**
     * Return the set for recommendations assigned to the user
     * 
     * @param application
     * @param user
     * @return
     */
    public List<RecommendationType> getRecommendationsAssignedToUser(ApplicationType application, UserType user) {
        ArrayList<RecommendationType> userRecs = new ArrayList<RecommendationType>();
        userRecs.ensureCapacity(100);
        if (application.isSetRecommendations()) {
            List<RecommendationType> allRecs = application.getRecommendations().getRecommendationList();
            for (RecommendationType rec : allRecs) {
                if (rec.getAssignedToId().equals(user.getId()) && (rec.getChangeType() != ChangeTypeEnum.DELETED) && !this.isDeleted(rec)) {
                    userRecs.add(rec);
                }
            }
        }
        return userRecs;
    }

    /**
     * Return the set of recommendations that are not deleted
     * 
     * @param application
     * @return
     */
    public List<RecommendationType> getActiveRecommendations(ApplicationType application) {
        List<RecommendationType> activeRecs = null;
        if (application.isSetRecommendations()) {
            activeRecs = this.getActiveItems(application.getRecommendations().getRecommendationList());
        } else activeRecs = Collections.emptyList();
        return activeRecs;
    }

    /**
     * Return the set of recommendations that are deleted
     * 
     * @param application
     * @return
     */
    public List<RecommendationType> getDeletedRecommendations(ApplicationType application) {
        List<RecommendationType> deletedRecs = null;
        if (application.isSetRecommendations()) {
            List<RecommendationType> allRecs = application.getRecommendations().getRecommendationList();
            deletedRecs = new ArrayList<RecommendationType>();
            for (RecommendationType rec : allRecs) {
                if (this.isDeleted(rec)) {
                    deletedRecs.add(rec);
                }
            }
        } else deletedRecs = Collections.emptyList();
        return deletedRecs;
    }

    /**
     * Return the set of recommendations that are added/modified/soft deleted since the last sync.
     * Because recommendations go through a soft delete, we only need to look for a changeType of MODIFIED
     *
     * @param application
     * @return
     */
    public List<RecommendationType> getTouchedRecommendations(ApplicationType application) {
        List<RecommendationType> recs = null;
        if (application.isSetRecommendations()) {
            List<RecommendationType> allRecs = application.getRecommendations().getRecommendationList();
            recs = new ArrayList<RecommendationType>(allRecs.size());
            for (RecommendationType rec : allRecs) {
                if (rec.getChangeType().equals(ChangeTypeEnum.MODIFIED) || rec.getChangeType().equals(ChangeTypeEnum.NEW)) {
                    recs.add(rec);
                }
            }
        } else recs = Collections.emptyList();
        return recs;
    }

    /**
     * Return the set of recommendations that are not deleted and marked noteworthy
     * 
     * @param application
     * @return
     */
    public List<RecommendationType> getNoteworthyRecommendations(ApplicationType application) {
        List<RecommendationType> noteworthyRecs = null;
        if (application.isSetRecommendations()) {
            List<RecommendationType> activeRecs = this.getActiveRecommendations(application);
            noteworthyRecs = new ArrayList<RecommendationType>();
            for (RecommendationType rec : activeRecs) {
                if (rec.getNoteworthy()) {
                    noteworthyRecs.add(rec);
                }
            }
        } else noteworthyRecs = Collections.emptyList();
        return noteworthyRecs;
    }

    /**
     * Return the set of recommendations that are not deleted and marked warning
     * 
     * @param application
     * @return
     */
    public List<RecommendationType> getWarningRecommendations(ApplicationType application) {
        List<RecommendationType> warningRecs = null;
        if (application.isSetRecommendations()) {
            List<RecommendationType> activeRecs = this.getActiveRecommendations(application);
            warningRecs = new ArrayList<RecommendationType>();
            for (RecommendationType rec : activeRecs) {
                if (rec.getWarning()) {
                    warningRecs.add(rec);
                }
            }
        }
        return warningRecs;
    }

    /**
     * Return the set of recommendations that are not deleted
     * 
     * @param application
     * @return
     */
    public List<RecommendationType> getAllRecommendations(ApplicationType application) {
        List<RecommendationType> allRecs = null;
        if (application.isSetRecommendations()) {
            allRecs = application.getRecommendations().getRecommendationList();
        } else allRecs = Collections.emptyList();
        return allRecs;
    }

    /**
     * Apply the view recommendation xsl to the specified recommendation.
     * 
     * @param application
     * @param recommendation
     * @return The resulting html file
     * @throws org.mitre.rt.exceptions.DataManagerException
     * @throws java.io.IOException
     * @throws javax.xml.transform.TransformerConfigurationException
     * @throws javax.xml.transform.TransformerException
     */
    public File applyViewRecommendationXsl(ApplicationType application, RecommendationType recommendation) throws DataManagerException, IOException, TransformerConfigurationException, TransformerException {
        logger.debug("Applying view recommendation xsl");
        String viewRecXsl = RTClientProperties.instance().getViewRecommendationXsl();
        File viewRecXslFile = new File(viewRecXsl);
        HashMap<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("applicationId", application.getId());
        parameters.put("recommendationId", recommendation.getId());
        Reader xmlFile = DataManager.instance().getRTDocument().newReader();
        String tempDir = RTClientProperties.instance().getTempDir();
        File tempDirFile = new File(tempDir);
        if (!tempDirFile.exists()) tempDirFile.mkdirs();
        File outputHtml = new File(tempDir + File.separator + "view.rec." + application.getId() + "." + recommendation.getId() + ".html");
        FileWriter output = new FileWriter(outputHtml);
        XSLProcessor.Instance().processWithCache(xmlFile, viewRecXslFile, output, parameters);
        logger.debug("Finished applying view recommendation xsl");
        return outputHtml;
    }

    public void guiViewRecommendation(ApplicationType app, RecommendationType rec) {
        try {
            File outputHtml = this.applyViewRecommendationXsl(app, rec);
            if (Desktop.isDesktopSupported()) {
                Desktop desktop = Desktop.getDesktop();
                URI uri = outputHtml.toURI();
                logger.debug("Displaying via browser: " + uri.toASCIIString());
                desktop.browse(uri);
            } else {
                logger.debug("Displaying via dialog");
                String title = "View Rule: " + super.getUserFriendlyId(app, rec);
                ViewHtmlDialog recDialog = new ViewHtmlDialog(MetaManager.getMainWindow(), true, title, outputHtml);
                recDialog.setVisible(true);
            }
        } catch (Exception ex) {
            logger.warn("", ex);
        }
    }

    public boolean guiDeleteRecommendation(ApplicationType app, RecommendationType rec) {
        boolean deleted = false;
        Object[] options = { "Delete", "Cancel" };
        int n = JOptionPane.showOptionDialog(MetaManager.getMainWindow(), "Delete this Rule?", "Delete Rule?", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
        if (n == JOptionPane.YES_OPTION) {
            List<RecommendationType> dependents = this.getDependents(app, rec);
            List<GroupType> groupDependents = this.getGroupDependents(app, rec);
            List<ProfileType> refProfiles = getReferencingProfiles(app, rec.getId());
            if (dependents.size() + groupDependents.size() + refProfiles.size() > 0) {
                StringBuilder errMsg = new StringBuilder();
                if (!dependents.isEmpty()) {
                    errMsg.append("Dependent Rules: \n");
                    for (RecommendationType dependent : dependents) {
                        String id = this.getUserFriendlyId(app, dependent);
                        errMsg.append("[" + id + "]\n");
                    }
                }
                if (!groupDependents.isEmpty()) {
                    errMsg.append("\nDependent Groups: \n");
                    for (GroupType dependent : groupDependents) {
                        errMsg.append(dependent.getTitle());
                        errMsg.append("\n");
                    }
                }
                if (!refProfiles.isEmpty()) {
                    errMsg.append("\nReferencing Profiles:\n");
                    for (ProfileType profile : refProfiles) {
                        errMsg.append(profile.getTitle());
                        errMsg.append("\n");
                    }
                }
                GlobalUITools.displayWarningMessage(MetaManager.getMainWindow(), "Error Deleting Rule!", errMsg.substring(0, errMsg.length() - 1));
            } else {
                logger.info("Deleting rule " + rec.getId());
                this.markDeleted(app, rec);
                DataManager.setModified(true);
                deleted = true;
            }
        }
        return deleted;
    }

    public String guiAddRecommendation(ApplicationType app) {
        return this.guiAddRecommendation(app, false, false);
    }

    public String guiAddRecommendation(ApplicationType app, boolean noteworthy, boolean warning) {
        String id = null;
        ApplicationHelper helper = new ApplicationHelper();
        try {
            UserType me = MetaManager.getAuthenticatedUser();
            ApplicationType tmpApp = (ApplicationType) app.copy();
            RecommendationType tmpRec = super.getNewItem(tmpApp);
            if (helper.isEditor(tmpApp, me) == true || helper.isTeamLeader(tmpApp, me) == true) {
                tmpRec.setAssignedToId(me.getId());
            }
            tmpRec.setNoteworthy(noteworthy);
            tmpRec.setWarning(warning);
            AddEditRuleMainDialog editRecWin = new AddEditRuleMainDialog(MetaManager.getMainWindow(), false, app, tmpApp, false, tmpRec);
            editRecWin.setVisible(true);
            if (editRecWin.isCanceled() == false) {
                id = tmpRec.getId();
            }
        } catch (Exception ex) {
            logger.warn("", ex);
        }
        return id;
    }

    public String guiEditRecommendation(ApplicationType app, RecommendationType rec) {
        String id = null;
        try {
            ApplicationType tmpApp = (ApplicationType) app.copy();
            RecommendationType tmpRec = this.getItem(tmpApp.getRecommendations().getRecommendationList(), rec.getId());
            AddEditRuleMainDialog editRecWin = new AddEditRuleMainDialog(MetaManager.getMainWindow(), false, app, tmpApp, true, tmpRec);
            editRecWin.setVisible(true);
            id = tmpRec.getId();
        } catch (Exception ex) {
            logger.warn("Exception in guiEditRecommendation", ex);
        }
        return id;
    }

    public String guiEditRecommendationComments(ApplicationType app, RecommendationType rec) {
        String id = rec.getId();
        CommentEditDialog dialog = null;
        try {
            ApplicationType tmpApp = ApplicationType.Factory.newInstance();
            tmpApp.set(app);
            RecommendationType tmpRec = this.getItem(tmpApp.getRecommendations().getRecommendationList(), rec.getId());
            CommentRefs refs = (tmpRec.isSetCommentRefs() == true) ? tmpRec.getCommentRefs() : tmpRec.addNewCommentRefs();
            dialog = new CommentEditDialog(MetaManager.getMainWindow(), tmpApp, tmpRec, refs);
            dialog.setVisible(true);
            if (dialog.getDataChangedFlag() == true) {
                logger.debug("Saving changes to comments and recommendation");
                super.markModified(tmpRec);
                app.set(tmpApp);
                MetaManager.getMainWindow().updateApplication(app);
            }
        } catch (Exception ex) {
            logger.warn("", ex);
        }
        return id;
    }

    public Object getRecommendationTableColumnData(ApplicationType app, RecommendationType data, int column) {
        String text = "";
        final UserHelper helper = new UserHelper();
        if (data != null) {
            if (column == RecommendationsTableModel.TITLE) {
                text = data.getTitle();
            } else if (column == RecommendationsTableModel.ASSIGNED_TO) {
                String userId = data.getAssignedToId();
                text = helper.getLastNameFirstNameForUser(userId);
            } else if (column == RecommendationsTableModel.LAST_MODIFIED) {
                text = DateUtils.formatDate(data.getModified());
            } else if (column == RecommendationsTableModel.STATUS) {
                if (data.isSetStatusId()) text = this.getRuleStatus(app, data.getStatusId());
            } else if (column == RecommendationsTableModel.ID) {
                return data;
            } else if (column == RecommendationsTableModel.GROUP) {
                GroupType group = this.getGroupType(app, data);
                text = (group != null) ? group.getTitle() : "";
            } else if (column == RecommendationsTableModel.CREATED_BY) {
                String userId = data.getCreatorId();
                text = helper.getLastNameFirstNameForUser(userId);
            } else if (column == RecommendationsTableModel.CATEGORY) {
                text = this.getCategory(app, data);
            } else {
                throw new UnsupportedOperationException("Cell Rendering Error");
            }
        }
        return text;
    }

    public List<ProfileType> getReferencingProfiles(ApplicationType application, String ruleId) {
        ArrayList<ProfileType> referencingProfiles = new ArrayList<ProfileType>();
        if (application.isSetProfiles()) {
            for (ProfileType profile : application.getProfiles().getProfileList()) {
                if (profile.isSetSelectRules()) {
                    for (RuleSelectorType sg : profile.getSelectRules().getSelectRuleList()) {
                        if (sg.getRuleId().equals(ruleId)) {
                            logger.debug("found rule " + ruleId + " referenced in profile " + profile.getId());
                            referencingProfiles.add(profile);
                            break;
                        }
                    }
                }
            }
        }
        return referencingProfiles;
    }

    /**
     * Returns the max order number
     * @param app
     * @return
     */
    public BigInteger getMaxOrderNum(ApplicationType app) {
        if (app.isSetRecommendationOrderNumbers()) {
            return BigInteger.valueOf(app.getRecommendationOrderNumbers().sizeOfOrderedItemArray());
        }
        return BigInteger.ZERO;
    }

    private void orderItems(List<OrderedSharedIdType> listOrderedItems) {
        if (!listOrderedItems.isEmpty()) {
            Comparator<OrderedSharedIdType> compare = new Comparator<OrderedSharedIdType>() {

                @Override
                public int compare(OrderedSharedIdType o1, OrderedSharedIdType o2) {
                    return o1.getOrder().compareTo(o2.getOrder());
                }
            };
            Collections.sort(listOrderedItems, compare);
        }
    }

    /**
    * Aligns the orderNum attribute with the XML objects position in the document.
    * @param orderedItems
    * @return
    */
    boolean alignOrderedItems(List<OrderedSharedIdType> orderedItems) {
        boolean orderChanged = false;
        for (int i = 0; i < orderedItems.size(); i++) {
            OrderedSharedIdType item = orderedItems.get(i);
            BigInteger orderNum = BigInteger.valueOf(i + 1);
            BigInteger groupOrderNum = item.getOrder();
            if (orderNum.compareTo(groupOrderNum) != 0) {
                item.setOrder(orderNum);
                orderChanged = true;
            }
        }
        return orderChanged;
    }

    public boolean reOrderItems(List<OrderedSharedIdType> orderedItems) {
        List<OrderedSharedIdType> listItems = new ArrayList<OrderedSharedIdType>(orderedItems);
        this.orderItems(listItems);
        return this.alignOrderedItems(listItems);
    }

    public void addRecommendationOrderItem(ApplicationType app, RecommendationType rec) {
        this.addRecommendationOrderItem(app, rec.getId());
    }

    public void addRecommendationOrderItem(ApplicationType app, String id) {
        IHelperImpl<OrderedSharedIdsType, ApplicationType> helper = new IHelperImpl<OrderedSharedIdsType, ApplicationType>();
        OrderedSharedIdsType orderNumbers = null;
        if (app.isSetRecommendationOrderNumbers()) {
            orderNumbers = app.getRecommendationOrderNumbers();
        } else {
            orderNumbers = app.addNewRecommendationOrderNumbers();
            helper.markNew(orderNumbers);
        }
        final BigInteger nextOrderNum = this.getMaxOrderNum(app).add(BigInteger.ONE);
        OrderedSharedIdType newOrderNumber = orderNumbers.addNewOrderedItem();
        newOrderNumber.setStringValue(id);
        newOrderNumber.setOrder(nextOrderNum);
        helper.markModified(orderNumbers);
    }

    public void removeRecommendationOrderItem(ApplicationType app, RecommendationType rec) {
        this.removeRecommendationOrderItem(app, rec.getId());
    }

    public void removeRecommendationOrderItem(ApplicationType app, String id) {
        IHelperImpl<OrderedSharedIdsType, ApplicationType> helper = new IHelperImpl<OrderedSharedIdsType, ApplicationType>();
        if (app.isSetRecommendationOrderNumbers()) {
            OrderedSharedIdsType orderNumbers = app.getRecommendationOrderNumbers();
            List<OrderedSharedIdType> orderedItems = orderNumbers.getOrderedItemList();
            for (OrderedSharedIdType item : orderedItems) {
                if (item.getStringValue().equals(id)) {
                    orderedItems.remove(item);
                    this.reOrderItems(orderedItems);
                    helper.markModified(orderNumbers);
                    break;
                }
            }
        }
    }

    public void removeRecommendationOrderItem(ApplicationType app, BigInteger orderNum) {
        if (app.isSetRecommendationOrderNumbers()) {
            List<OrderedSharedIdType> orderedItems = app.getRecommendationOrderNumbers().getOrderedItemList();
            for (OrderedSharedIdType item : orderedItems) {
                if (item.getOrder().equals(orderNum)) {
                    orderedItems.remove(item);
                    break;
                }
            }
        }
    }

    private boolean mergeCommentRefs(ApplicationType app, RecommendationType source, RecommendationType dest) {
        logger.debug("Merging Comment References");
        CommentRefs sourceCommentRefs = (source.isSetCommentRefs() == true) ? source.getCommentRefs() : source.addNewCommentRefs(), destCommentRefs = (dest.isSetCommentRefs() == true) ? dest.getCommentRefs() : dest.addNewCommentRefs();
        List<String> sourceRefs = sourceCommentRefs.getCommentRefList(), destRefs = destCommentRefs.getCommentRefList();
        List<String> availableSourceRefs = SynchronizeTransactionUtilities.getExistingRefs(sourceRefs, app.getComments(), "Comment");
        boolean removed = SynchronizeTransactionUtilities.removeDeletedRefs(destRefs, app.getComments(), "Comment");
        boolean added = SynchronizeTransactionUtilities.mergeReferenceLists(availableSourceRefs, destRefs);
        return (removed || added);
    }

    private boolean mergeReferenceRefs(ApplicationType app, RecommendationType source, RecommendationType dest) {
        logger.debug("Merging Reference Refs");
        References sourcePRefs = (source.isSetReferences() == true) ? source.getReferences() : source.addNewReferences(), destPRefs = (dest.isSetReferences() == true) ? dest.getReferences() : dest.addNewReferences();
        List<String> sourceRefs = sourcePRefs.getReferenceRefList(), destRefs = destPRefs.getReferenceRefList();
        List<String> availableSourceRefs = SynchronizeTransactionUtilities.getExistingRefs(sourceRefs, app.getReferences(), "Reference");
        boolean removed = SynchronizeTransactionUtilities.removeDeletedRefs(destRefs, app.getReferences(), "Reference");
        boolean added = SynchronizeTransactionUtilities.mergeReferenceLists(availableSourceRefs, destRefs);
        return (removed || added);
    }

    private boolean mergeComplianceCheckRefs(ApplicationType app, RecommendationType source, RecommendationType dest) {
        logger.debug("Merging Compliance Check References");
        ComplianceCheckRefs sourceCCheckRefs = (source.isSetComplianceCheckRefs() == true) ? source.getComplianceCheckRefs() : source.addNewComplianceCheckRefs(), destCCheckRefs = (dest.isSetComplianceCheckRefs() == true) ? dest.getComplianceCheckRefs() : dest.addNewComplianceCheckRefs();
        List<String> sourceRefs = sourceCCheckRefs.getComplianceCheckRefList(), destRefs = destCCheckRefs.getComplianceCheckRefList();
        List<String> availableSourceRefs = SynchronizeTransactionUtilities.getExistingRefs(sourceRefs, app.getComplianceChecks(), "ComplianceCheck");
        boolean removed = SynchronizeTransactionUtilities.removeDeletedRefs(destRefs, app.getComplianceChecks(), "ComplianceCheck");
        boolean added = SynchronizeTransactionUtilities.mergeReferenceLists(availableSourceRefs, destRefs);
        return (removed || added);
    }

    /**
     * mergeRecommendations
     * @param app
     * @param local
     * @param serverItem
     * @return true if any changes were made to @dest
     */
    public boolean mergeRecommendations(ApplicationType app, RecommendationType local, RecommendationType serverItem) {
        boolean commentsChanged = this.mergeCommentRefs(app, local, serverItem);
        boolean complianceChecksChanged = this.mergeComplianceCheckRefs(app, local, serverItem);
        boolean referencesChanged = this.mergeReferenceRefs(app, local, serverItem);
        return (commentsChanged || complianceChecksChanged || referencesChanged);
    }

    public boolean userCanEdit(RecommendationType item, ApplicationType app, UserType user) {
        return (item.getAssignedToId().equals(user.getId()) || super.userCanEdit(app, item, user));
    }

    @Override
    public String getNewId(ApplicationType app) {
        return IDManager.getNextRuleId(app);
    }

    @Override
    protected RecommendationType getInstance() {
        return RecommendationType.Factory.newInstance();
    }

    @Override
    public boolean canDeleteItem(ApplicationType app, RecommendationType rec) {
        List<RecommendationType> dependants = this.getDependents(app, rec);
        return (dependants.size() == 0);
    }

    @Override
    public void markDeleted(ApplicationType application, RecommendationType recommendation) {
        GroupType group = this.getGroupType(application, recommendation);
        if (group != null) {
            try {
                this.removeReference(group.getRecommendations().getRecommendationReferenceList(), recommendation);
                new GroupHelper().markModified(group);
            } catch (Exception ex) {
                logger.warn(ex);
            }
        }
        recommendation.setDeleted(true);
        super.markModified(recommendation);
        this.removeRecommendationOrderItem(application, recommendation);
    }
}
