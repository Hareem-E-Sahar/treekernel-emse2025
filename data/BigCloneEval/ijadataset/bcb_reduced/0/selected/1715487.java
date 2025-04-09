package org.mitre.rt.client.xml;

import java.awt.Desktop;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.lang.String;
import java.math.BigInteger;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import javax.swing.JOptionPane;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import org.apache.log4j.Logger;
import org.apache.xmlbeans.XmlObject;
import org.mitre.rt.client.core.DataManager;
import org.mitre.rt.client.core.IDManager;
import org.mitre.rt.client.core.MetaManager;
import org.mitre.rt.client.exceptions.DataManagerException;
import org.mitre.rt.client.exceptions.RTClientException;
import org.mitre.rt.client.properties.RTClientProperties;
import org.mitre.rt.client.ui.comments.CommentEditDialog;
import org.mitre.rt.client.ui.groups.AddEditGroupMainDialog;
import org.mitre.rt.client.ui.html.ViewHtmlDialog;
import org.mitre.rt.client.util.GlobalUITools;
import org.mitre.rt.client.util.StringUtils;
import org.mitre.rt.common.xml.XSLProcessor;
import org.mitre.rt.rtclient.ApplicationType;
import org.mitre.rt.rtclient.ChangeTypeEnum;
import org.mitre.rt.rtclient.CommentType;
import org.mitre.rt.rtclient.GroupReferenceType;
import org.mitre.rt.rtclient.GroupReferencesType;
import org.mitre.rt.rtclient.GroupSelectorType;
import org.mitre.rt.rtclient.GroupType;
import org.mitre.rt.rtclient.GroupType.CommentRefs;
import org.mitre.rt.rtclient.GroupsType;
import org.mitre.rt.rtclient.IdedVersionedItemType;
import org.mitre.rt.rtclient.OrderedDeleteableSharedIdValuePairType;
import org.mitre.rt.rtclient.OrderedSharedIdType;
import org.mitre.rt.rtclient.OrderedSharedIdsType;
import org.mitre.rt.rtclient.ProfileType;
import org.mitre.rt.rtclient.ReferenceType;
import org.mitre.rt.rtclient.RecommendationReferenceType;
import org.mitre.rt.rtclient.RecommendationReferencesType;
import org.mitre.rt.rtclient.RecommendationType;
import org.mitre.rt.rtclient.SharedIdType;
import org.mitre.rt.rtclient.UserType;

/**
 *
 * @author BAKERJ
 */
public class GroupHelper extends AbsHelper<GroupType, ApplicationType> {

    private static Logger logger = Logger.getLogger(GroupHelper.class.getPackage().getName());

    private static int SHIFT_UP = 1, SHIFT_DOWN = -1, SHIFT_NONE = 0;

    public GroupHelper() {
        super("GroupReference");
    }

    public List<GroupType> getGroupDependencyCandidates(ApplicationType app, GroupType group) {
        List<GroupType> candidates = null;
        List<GroupType> activeGroups = super.getActiveItems(app.getGroups().getGroupList());
        if (activeGroups.isEmpty()) {
            candidates = Collections.emptyList();
        } else {
            List<String> deps = group.getGroupDependencyList();
            candidates = new ArrayList<GroupType>(activeGroups.size() - deps.size());
            for (GroupType depCandidate : activeGroups) {
                if (!deps.contains(depCandidate.getId())) {
                    if (!isCircular(app, depCandidate, group)) {
                        candidates.add(depCandidate);
                    }
                }
            }
        }
        return candidates;
    }

    /**
     * Add a dependency to the group. depRecommendation is added to the
     * specified group.
     *
     * @param group
     * @param depRecommendation
     */
    public void addDependency(GroupType group, RecommendationType depRecommendation) {
        group.addDependency(depRecommendation.getId());
    }

    /**
     * Add a group dependency to the group.
     *
     * @param group
     * @param depGroup
     */
    public void addDependency(GroupType group, GroupType depGroup) {
        group.addGroupDependency(depGroup.getId());
    }

    /**
     * Returns a list of recommendations that can be added as a dependency for
     * the given group. Recommendations that are already a dependency and
     * recommendations that introduce circular dependencies will be omitted
     * from the returned list.
     * @param app
     * @param group
     * @return
     */
    public List<RecommendationType> getDependencyCandidates(ApplicationType app, GroupType group) {
        final RecommendationHelper ruleHelper = new RecommendationHelper();
        List<RecommendationType> candidates = null;
        List<RecommendationType> activeRecs = ruleHelper.getActiveItems(app.getRecommendations().getRecommendationList());
        if (activeRecs.isEmpty()) {
            candidates = Collections.emptyList();
        } else {
            List<RecommendationType> deps = this.getDependencies(app, group);
            candidates = new ArrayList<RecommendationType>(activeRecs.size() - deps.size());
            List<String> depIds = new ArrayList<String>(deps.size());
            for (RecommendationType dep : deps) {
                depIds.add(dep.getId());
            }
            for (RecommendationType depCandidate : activeRecs) {
                if (!depIds.contains(depCandidate.getId()) && !ruleHelper.isCircular(app, depCandidate, group)) {
                    candidates.add(depCandidate);
                }
            }
        }
        return candidates;
    }

    public List<RecommendationType> getRuleDependencyCandidates(ApplicationType app, GroupType group) {
        List<String> deps;
        deps = group.getDependencyList();
        List<RecommendationType> candidates = null;
        RecommendationHelper ruleHelper = new RecommendationHelper();
        List<RecommendationType> activeRules = ruleHelper.getActiveItems(app.getRecommendations().getRecommendationList());
        if (activeRules.isEmpty()) {
            candidates = Collections.emptyList();
        } else {
            candidates = new ArrayList<RecommendationType>(activeRules.size() - deps.size());
            for (RecommendationType depCandidate : activeRules) {
                if (!deps.contains(depCandidate.getId())) {
                    if (!isCircular(app, group, depCandidate)) {
                        candidates.add(depCandidate);
                    }
                }
            }
        }
        return candidates;
    }

    /**
     * If the item is not found -1 is returned otherwise the index of the item in
     * the array is returned
     * @param items
     * @return
     */
    public int getIndexOfGroupType(List<GroupType> items, String itemId) {
        for (int i = 0; i < items.size(); i++) {
            String currentId = items.get(i).getId();
            if (itemId.equals(currentId)) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Add a reference to the specified recommendation to the specified group.
     * 
     * Calls GroupHelper.markAsModified()
     * 
     * @param group
     * @param recommendation
     */
    public RecommendationReferenceType addRecommendationToGroup(ApplicationType app, GroupType group, RecommendationType recommendation, BigInteger orderNum) throws RTClientException {
        if (recommendation == null) {
            throw new RTClientException("The input recommendation is null. A reference to the recommendation can not be added to the group.");
        }
        return this.addRecommendationToGroup(app, group, recommendation.getId(), orderNum);
    }

    /**
     * Add a reference to the specified recommendation to the specified group.
     * 
     * Calls GroupHelper.markAsModified()
     * 
     * @param group
     * @param recommendationId
     */
    public RecommendationReferenceType addRecommendationToGroup(ApplicationType app, GroupType group, String recommendationId, BigInteger orderNum) throws RTClientException {
        RecommendationHelper recHelper = new RecommendationHelper();
        if (StringUtils.isWhitespaceOnlyOrNull(recommendationId)) {
            throw new RTClientException("The input recommendation id is empty. A reference to the recommendation can not be added to the group.");
        }
        if (group == null) {
            throw new RTClientException("The input group is null. A reference to the recommendation can not be added to the group.");
        }
        if (!group.isSetRecommendations()) {
            group.addNewRecommendations();
        }
        RecommendationReferenceType recRef = group.getRecommendations().addNewRecommendationReference();
        if (orderNum == null) {
            orderNum = this.getMaxOrderNum(group.getRecommendations());
            orderNum = orderNum.add(BigInteger.valueOf(1));
        }
        recRef.setOrderNum(orderNum);
        recRef.setStringValue(recommendationId);
        super.markModified(group);
        recHelper.removeRecommendationOrderItem(app, recommendationId);
        return recRef;
    }

    public GroupType getParentGroup(ApplicationType app, GroupType group) {
        GroupType parentGroup = null;
        String lookingForId = group.getId();
        if (app.isSetGroups()) {
            for (GroupType currentGroup : app.getGroups().getGroupList()) {
                if (parentGroup != null) break;
                if (currentGroup.getChangeType() == ChangeTypeEnum.DELETED) {
                    continue;
                }
                if (currentGroup.equals(group)) continue;
                if (currentGroup.isSetSubGroups()) {
                    for (GroupReferenceType ref : currentGroup.getSubGroups().getGroupReferenceList()) {
                        if (ref.getStringValue().equals(lookingForId)) {
                            parentGroup = currentGroup;
                            break;
                        }
                    }
                }
            }
        }
        return parentGroup;
    }

    /**
     * Get an array of the dependencies curently set on the specified Recommendation.
     * @param recommendation
     * @return
     */
    public List<RecommendationType> getDependencies(ApplicationType application, GroupType group) {
        List<String> depList = group.getDependencyList();
        List<RecommendationType> deps = new ArrayList<RecommendationType>();
        RecommendationHelper ruleHelper = new RecommendationHelper();
        for (String depId : depList) {
            RecommendationType tmp = ruleHelper.getItem(application.getRecommendations().getRecommendationList(), depId);
            if (tmp != null && !ruleHelper.isDeleted(tmp)) {
                deps.add(tmp);
            }
        }
        return deps;
    }

    /**
     * Get an array of the group dependencies curently set on the specified Recommendation.
     * @param recommendation
     * @return
     */
    public List<GroupType> getGroupDependencies(ApplicationType application, GroupType group) {
        List<String> depList = group.getGroupDependencyList();
        List<GroupType> deps = new ArrayList<GroupType>();
        for (String depId : depList) {
            GroupType tmp = getItem(application.getGroups().getGroupList(), depId);
            if (tmp != null) {
                deps.add(tmp);
            }
        }
        return deps;
    }

    /**
     * Recursive function to check the groups and rules that a group depends on to
     * see if the test group is in the chain
     * @param application
     * @param topGroup walk the groups and rules that this group depends on to see if testGroup is in there somewhere
     * @param testGroup the group in question
     * @return
     */
    public boolean isCircular(ApplicationType application, GroupType topGroup, GroupType testGroup) {
        boolean circular = false;
        RecommendationHelper ruleHelper = new RecommendationHelper();
        if (topGroup.getId().equals(testGroup.getId())) return true;
        List<RecommendationType> deps = getDependencies(application, topGroup);
        for (RecommendationType dependent : deps) {
            circular = ruleHelper.isCircular(application, dependent, testGroup);
            if (circular) {
                logger.debug("isCircular [g,g] rule " + dependent.getId() + " has circular dependency with  group " + topGroup.getId());
                return true;
            }
        }
        List<GroupType> groupDeps = this.getGroupDependencies(application, topGroup);
        for (GroupType dependent : groupDeps) {
            if (dependent.getId().equals(testGroup.getId())) {
                logger.debug("isCircular [g,g] group " + dependent.getId() + " has circular dependency with  group " + topGroup.getId());
                return true;
            }
            circular = isCircular(application, dependent, testGroup);
            if (circular) {
                logger.debug("isCircular [g,g] group " + dependent.getId() + " has circular dependency with  group " + topGroup.getId());
                return true;
            }
        }
        return circular;
    }

    /**
     * Recursive function to check the groups and recommendations that a group depends on to
     * see if the test rec is in the chain
     * @param application
     * @param topGroup walk the groups and recs that this group depends on to see if testRec is in there somewhere
     * @param testRec the rec in question
     * @return
     */
    public boolean isCircular(ApplicationType application, GroupType topGroup, RecommendationType testRec) {
        boolean circular = false;
        RecommendationHelper ruleHelper = new RecommendationHelper();
        List<RecommendationType> deps = this.getDependencies(application, topGroup);
        for (RecommendationType dependent : deps) {
            if (dependent.getId().equals(testRec.getId())) {
                logger.debug("isCircular [g,r] rule " + dependent.getId() + " has circular dependency with  group " + topGroup.getId());
                return true;
            }
            circular = ruleHelper.isCircular(application, dependent, testRec);
            if (circular) {
                logger.debug("isCircular [g,r] rule " + dependent.getId() + " has circular dependency with  group " + topGroup.getId());
                return true;
            }
        }
        List<GroupType> groupDeps = this.getGroupDependencies(application, topGroup);
        for (GroupType dependent : groupDeps) {
            circular = isCircular(application, dependent, testRec);
            if (circular) {
                logger.debug("isCircular [g,r] rule " + dependent.getId() + " has circular dependency with  group " + topGroup.getId());
                return true;
            }
        }
        return circular;
    }

    public boolean isDescendantOf(List<GroupType> allGroups, GroupType possibleParent, GroupType possibleChild) {
        if (allGroups == null) return false;
        if (possibleParent.isSetSubGroups()) {
            for (GroupReferenceType g : possibleParent.getSubGroups().getGroupReferenceList()) {
                if (g.getStringValue().equals(possibleChild.getId())) {
                    return true;
                }
            }
            for (GroupReferenceType g : possibleParent.getSubGroups().getGroupReferenceList()) {
                GroupType nextParent = getItem(allGroups, g.getStringValue());
                if (isDescendantOf(allGroups, nextParent, possibleChild)) {
                    return true;
                }
            }
        }
        return false;
    }

    public void guiViewGroup(ApplicationType app, GroupType group) {
        try {
            File outputHtml = this.applyViewGroupXsl(app, group);
            if (Desktop.isDesktopSupported()) {
                Desktop desktop = Desktop.getDesktop();
                URI uri = outputHtml.toURI();
                logger.debug("Displaying via browser: " + uri.toASCIIString());
                desktop.browse(uri);
            } else {
                logger.debug("Displaying via dialog");
                String title = "View Group: " + group.getTitle();
                ViewHtmlDialog recDialog = new ViewHtmlDialog(MetaManager.getMainWindow(), true, title, outputHtml);
                recDialog.setVisible(true);
            }
        } catch (Exception ex) {
            logger.warn(ex);
        }
    }

    public String guiAddGroup(ApplicationType app) {
        String id = null;
        ApplicationHelper appHelper = new ApplicationHelper();
        try {
            UserType me = MetaManager.getAuthenticatedUser();
            ApplicationType tmpApp = ApplicationType.Factory.newInstance();
            tmpApp.set(app);
            GroupType tmpGroup = this.getNewItem(tmpApp);
            AddEditGroupMainDialog editGroupWin = new AddEditGroupMainDialog(MetaManager.getMainWindow(), false, app, tmpApp, false, tmpGroup);
            editGroupWin.setVisible(true);
            if (editGroupWin.isCanceled() == false) {
                id = tmpGroup.getId();
            }
        } catch (Exception ex) {
            logger.warn("Couldn't add Group", ex);
        }
        return id;
    }

    public boolean guiDeleteGroup(ApplicationType application, GroupType group) {
        boolean deleted = false;
        Object[] options = { "Delete", "Cancel" };
        int n = JOptionPane.showOptionDialog(MetaManager.getMainWindow(), "Delete this Group?", "Delete Group?", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
        if (n == JOptionPane.YES_OPTION) {
            if (super.userCanEdit(application, group, MetaManager.getAuthenticatedUser())) {
                String groupId = group.getId();
                List<GroupType> refGroups = getReferencingGroups(application, groupId);
                List<ProfileType> refProfiles = getReferencingProfiles(application, groupId);
                List<GroupType> dependsGroups = getGroupRequiredByGroups(application, groupId);
                List<RecommendationType> dependsRules = getGroupRequiredByRules(application, groupId);
                if (refGroups.size() + refProfiles.size() + dependsGroups.size() + dependsRules.size() > 0) {
                    StringBuffer errMsg = new StringBuffer();
                    for (GroupType g : refGroups) {
                        errMsg.append("Group ");
                        errMsg.append(g.getTitle());
                        errMsg.append(" references ");
                        errMsg.append(group.getTitle());
                        errMsg.append(" as a subgroup\n");
                    }
                    for (ProfileType p : refProfiles) {
                        errMsg.append("Profile ");
                        errMsg.append(p.getTitle());
                        errMsg.append(" references ");
                        errMsg.append(group.getTitle());
                        errMsg.append("\n");
                    }
                    for (GroupType g : dependsGroups) {
                        errMsg.append("Group ");
                        errMsg.append(g.getTitle());
                        errMsg.append(" depends on group ");
                        errMsg.append(group.getTitle());
                        errMsg.append("\n");
                    }
                    for (RecommendationType r : dependsRules) {
                        errMsg.append("Rule [");
                        RecommendationHelper ruleHelper = new RecommendationHelper();
                        errMsg.append(ruleHelper.getUserFriendlyId(application, r));
                        errMsg.append("] ");
                        errMsg.append(r.getTitle());
                        errMsg.append(" depends on group ");
                        errMsg.append(group.getTitle());
                        errMsg.append("\n");
                    }
                    GlobalUITools.displayWarningMessage(MetaManager.getMainWindow(), "Error Deleting Group!", errMsg.toString());
                } else {
                    logger.info("Deleting group " + group.getId() + " in applicaiton " + application.getId());
                    this.markDeleted(application, group);
                    DataManager.setModified(true);
                    deleted = true;
                }
            }
        }
        return deleted;
    }

    public String guiEditGroup(ApplicationType app, GroupType group) {
        String id = null;
        try {
            ApplicationType tmpApp = ApplicationType.Factory.newInstance();
            tmpApp.set(app);
            GroupType tmpGroup = super.getItem(tmpApp.getGroups().getGroupList(), group.getId());
            AddEditGroupMainDialog editRecWin = new AddEditGroupMainDialog(MetaManager.getMainWindow(), false, app, tmpApp, true, tmpGroup);
            editRecWin.setVisible(true);
            id = tmpGroup.getId();
        } catch (Exception ex) {
            logger.warn("guiEditGroup exception", ex);
        }
        return id;
    }

    public String guiEditGroupComments(ApplicationType app, GroupType group) {
        String id = group.getId();
        CommentEditDialog dialog = null;
        try {
            ApplicationType tmpApp = ApplicationType.Factory.newInstance();
            tmpApp.set(app);
            GroupType tmpGroup = super.getItem(tmpApp.getGroups().getGroupList(), group.getId());
            CommentRefs refs = (tmpGroup.isSetCommentRefs() == true) ? tmpGroup.getCommentRefs() : tmpGroup.addNewCommentRefs();
            dialog = new CommentEditDialog(MetaManager.getMainWindow(), tmpApp, tmpGroup, refs);
            dialog.setVisible(true);
            if (dialog.getDataChangedFlag() == true) {
                logger.debug("Saving changes to comments and recommendation");
                super.markModified(tmpGroup);
                app.set(tmpApp);
                MetaManager.getMainWindow().updateApplication(app);
            }
        } catch (Exception ex) {
            logger.warn("guiEditGroupComments exception", ex);
        }
        return id;
    }

    /**
     * Add a reference to the child group and mark the parent gorup as modified.
     * 
     * Calls GroupHelper.markAsModified
     * 
     * @param parentGroup
     * @param childGroup
     */
    public GroupReferenceType addSubGroupReference(ApplicationType app, GroupType parentGroup, String childGroupId, BigInteger orderNum) {
        if (!parentGroup.isSetSubGroups()) {
            parentGroup.addNewSubGroups();
            orderNum = BigInteger.valueOf(1);
        }
        if (orderNum == null) {
            BigInteger maxOrder = this.getMaxOrderNum(parentGroup.getSubGroups());
            orderNum = maxOrder.add(BigInteger.valueOf(1));
        }
        GroupReferencesType groups = parentGroup.getSubGroups();
        GroupReferenceType groupRef = groups.addNewGroupReference();
        groupRef.setOrderNum(orderNum);
        groupRef.setStringValue(childGroupId);
        this.removeGroupOrderItem(app, childGroupId);
        super.markModified(parentGroup);
        return groupRef;
    }

    /**
     * 
     * Calls GroupHelper.markAsModified()
     * 
     * @param group
     * @param ref
     * @throws org.mitre.rt.exceptions.RTClientException
     */
    public void removeRecommendationFromGroup(ApplicationType app, GroupType group, RecommendationType recommendation) throws Exception {
        final RecommendationHelper recHelper = new RecommendationHelper();
        recHelper.removeReference(group.getRecommendations().getRecommendationReferenceList(), recommendation);
        recHelper.addRecommendationOrderItem(app, recommendation);
        super.markModified(group);
    }

    public void addGroupOrderItem(ApplicationType app, GroupType group) {
        this.addGroupOrderItem(app, group.getId());
    }

    public void addGroupOrderItem(ApplicationType app, String id) {
        IHelperImpl<OrderedSharedIdsType, ApplicationType> helper = new IHelperImpl<OrderedSharedIdsType, ApplicationType>();
        OrderedSharedIdsType orderNumbers = null;
        if (app.isSetGroupOrderNumbers()) {
            orderNumbers = app.getGroupOrderNumbers();
        } else {
            orderNumbers = app.addNewGroupOrderNumbers();
            helper.markNew(orderNumbers);
        }
        final BigInteger nextOrderNum = this.getMaxOrderNum(app).add(BigInteger.ONE);
        OrderedSharedIdType newOrderNumber = orderNumbers.addNewOrderedItem();
        newOrderNumber.setStringValue(id);
        newOrderNumber.setOrder(nextOrderNum);
        helper.markModified(orderNumbers);
    }

    public void removeGroupOrderItem(ApplicationType app, String id) {
        IHelperImpl<OrderedSharedIdsType, ApplicationType> helper = new IHelperImpl<OrderedSharedIdsType, ApplicationType>();
        if (app.isSetGroupOrderNumbers()) {
            OrderedSharedIdsType groupOrderNumbers = app.getGroupOrderNumbers();
            List<OrderedSharedIdType> orderedItems = groupOrderNumbers.getOrderedItemList();
            for (OrderedSharedIdType item : orderedItems) {
                if (item.getStringValue().equals(id)) {
                    orderedItems.remove(item);
                    this.reOrderGroups(orderedItems);
                    helper.markModified(groupOrderNumbers);
                    break;
                }
            }
        }
    }

    public void removeGroupOrderItem(ApplicationType app, GroupType group) {
        this.removeGroupOrderItem(app, group.getId());
    }

    public void removeGroupOrderItem(ApplicationType app, BigInteger orderNum) {
        IHelperImpl<OrderedSharedIdsType, ApplicationType> helper = new IHelperImpl<OrderedSharedIdsType, ApplicationType>();
        if (app.isSetGroupOrderNumbers()) {
            OrderedSharedIdsType groupOrderNumbers = app.getGroupOrderNumbers();
            List<OrderedSharedIdType> orderedItems = groupOrderNumbers.getOrderedItemList();
            for (OrderedSharedIdType item : orderedItems) {
                if (item.getOrder().equals(orderNum)) {
                    orderedItems.remove(item);
                    this.reOrderGroups(orderedItems);
                    helper.markModified(groupOrderNumbers);
                    break;
                }
            }
        }
    }

    /**
     * Remove the a reference to a group.
     * 
     * Calls GroupHelper.markAsModified()
     * 
     * @param parentGroup
     * @param childGroup
     * @throws org.mitre.rt.exceptions.RTClientException
     */
    public void removeSubGroupReference(ApplicationType app, GroupType parentGroup, GroupReferenceType childGroup) throws Exception {
        final String id = childGroup.getStringValue();
        if (parentGroup.isSetSubGroups()) {
            super.removeReference(parentGroup.getSubGroups().getGroupReferenceList(), id);
            super.markModified(parentGroup);
            this.addGroupOrderItem(app, id);
        }
    }

    /**
     * Insert a CommentType object into the groups's array of comments.
     * 
     * @param group
     * @return the inserted comment.
     */
    public void insertComment(ApplicationType app, GroupType group, CommentType comment) {
        CommentRefs commentRefs = (group.isSetCommentRefs() == true) ? group.getCommentRefs() : group.addNewCommentRefs();
        ApplicationHelper helper = new ApplicationHelper();
        helper.insertNewComment(app, comment);
        SharedIdType commentRef = commentRefs.addNewCommentRef();
        commentRef.setStringValue(comment.getId());
    }

    /**
     * Return an array of suggroups for the specified group.
     * 
     * @param application
     * @param group
     * @return
     * @throws org.mitre.rt.exceptions.RTClientException
     */
    public List<GroupType> getSubGroups(ApplicationType application, GroupType group) {
        List<GroupType> subGroups = null;
        final List<GroupType> allGroups = application.getGroups().getGroupList();
        if (group.isSetSubGroups()) {
            List<GroupReferenceType> subGroupIds = group.getSubGroups().getGroupReferenceList();
            subGroups = new ArrayList<GroupType>(subGroupIds.size());
            for (GroupReferenceType ref : subGroupIds) {
                String subGroupId = ref.getStringValue();
                GroupType subGroup = super.getItem(allGroups, subGroupId);
                subGroups.add(subGroup);
            }
        } else {
            subGroups = Collections.emptyList();
        }
        return subGroups;
    }

    /**
     * 
     * @param app
     * @param rec
     * @return The group that contains the recommendation
     */
    public GroupType getItem(ApplicationType app, RecommendationType rec) {
        GroupType returnGroup = null;
        if (app.isSetGroups()) {
            for (GroupType group : app.getGroups().getGroupList()) {
                if (group.isSetRecommendations()) {
                    for (RecommendationReferenceType recRef : group.getRecommendations().getRecommendationReferenceList()) {
                        if (recRef.getStringValue().equals(rec.getId())) {
                            returnGroup = group;
                            break;
                        }
                    }
                }
            }
        }
        return returnGroup;
    }

    /**
     * Returns the ordernum of the recommendation within a group
     * @param parentGroup
     * @param rec
     * @return
     * @throws RTClientException
     */
    public BigInteger getOrderNum(GroupType parentGroup, RecommendationType rec) throws RTClientException {
        if (!parentGroup.isSetRecommendations()) {
            throw new RTClientException("The specified parent group does not have any rules!");
        }
        List<RecommendationReferenceType> recRefs = parentGroup.getRecommendations().getRecommendationReferenceList();
        BigInteger orderNum = BigInteger.ONE;
        for (RecommendationReferenceType recRef : recRefs) {
            if (recRef.getStringValue().equals(rec.getId())) {
                orderNum = recRef.getOrderNum();
                break;
            }
        }
        return orderNum;
    }

    public BigInteger getOrderNum(GroupType parentGroup, GroupType childGroup) throws RTClientException {
        if (!parentGroup.isSetSubGroups()) {
            throw new RTClientException("The specified parent group does not have any child groups!");
        }
        List<GroupReferenceType> groupRefs = parentGroup.getSubGroups().getGroupReferenceList();
        BigInteger orderNum = BigInteger.ONE;
        for (GroupReferenceType groupRef : groupRefs) {
            if (groupRef.getStringValue().equals(childGroup.getId())) {
                orderNum = groupRef.getOrderNum();
                break;
            }
        }
        return orderNum;
    }

    public GroupReferenceType getSubGroupReference(GroupType parentGroup, GroupType childGroup) throws RTClientException {
        if (!parentGroup.isSetSubGroups()) {
            throw new RTClientException("The specified parent group does not have any child groups!");
        }
        List<GroupReferenceType> groupRefs = parentGroup.getSubGroups().getGroupReferenceList();
        GroupReferenceType result = null;
        for (GroupReferenceType groupRef : groupRefs) {
            if (groupRef.getStringValue().equals(childGroup.getId())) {
                result = groupRef;
                break;
            }
        }
        return result;
    }

    /**
     * Return the string value for the Group's current statusId
     * @param application
     * @param recommendation
     * @return
     */
    public String getStatus(ApplicationType application, GroupType group) {
        OrderedDeleteableSharedIdValuePairType statusObj = this.getStatusObj(application, group);
        return (statusObj == null) ? "" : statusObj.getStringValue();
    }

    /**
     * Return the OrderedDeleteableSharedIdValuePairType Object for the Group's current statusId
     * @param application
     * @param recommendation
     * @return
     */
    public OrderedDeleteableSharedIdValuePairType getStatusObj(ApplicationType application, GroupType group) {
        if (!application.isSetRuleStatuses()) {
            logger.debug("This application does not use statuses.");
            return null;
        } else {
            if (group.isSetStatusId()) {
                String statusId = group.getStatusId();
                OrderedDeleteableSharedIdValuePairType statusObj = null;
                List<OrderedDeleteableSharedIdValuePairType> statusList = application.getRuleStatuses().getItemList();
                for (OrderedDeleteableSharedIdValuePairType status : statusList) {
                    if (status.getId().equals(statusId)) {
                        statusObj = status;
                        break;
                    }
                }
                return statusObj;
            } else {
                return null;
            }
        }
    }

    /**
     * Returns a group's order number if it is a top level group. Returns null if the group is not a top level group.
     * @param app
     * @param group
     * @return
     */
    public BigInteger getOrderNumber(ApplicationType app, GroupType group) {
        if (app.isSetGroupOrderNumbers()) {
            List<OrderedSharedIdType> orderNumbers = app.getGroupOrderNumbers().getOrderedItemList();
            for (OrderedSharedIdType orderedItem : orderNumbers) {
                if (orderedItem.getStringValue().equals(group.getId())) {
                    return orderedItem.getOrder();
                }
            }
        }
        return BigInteger.ZERO;
    }

    private File applyViewGroupXsl(ApplicationType application, GroupType group) throws DataManagerException, IOException, TransformerConfigurationException, TransformerException {
        logger.debug("Applying view group xsl");
        String viewRecXsl = RTClientProperties.instance().getViewGroupXsl();
        File viewRecXslFile = new File(viewRecXsl);
        HashMap<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("applicationId", application.getId());
        parameters.put("groupId", group.getId());
        Reader xmlFile = DataManager.instance().getRTDocument().newReader();
        String tempDir = RTClientProperties.instance().getTempDir();
        File tempDirFile = new File(tempDir);
        if (!tempDirFile.exists()) tempDirFile.mkdirs();
        File outputHtml = new File(tempDir + File.separator + "view.group." + application.getId() + "." + group.getId() + ".html");
        FileWriter output = new FileWriter(outputHtml);
        XSLProcessor.Instance().processWithCache(xmlFile, viewRecXslFile, output, parameters);
        logger.debug("Finished applying view recommendation xsl");
        return outputHtml;
    }

    private void swapGroups(List<GroupType> list, int x, int y) {
        if (x == y) return;
        Collections.swap(list, x, y);
    }

    /**
     * Orders groups by rootOrderNumber and modified date. If a rootOrderNumber is not present, the groups being compared use their created date as
     * a metric.
     *
     * Shift direction deals with the shifting of Groups while setting an order number. When a group is shifted, it's rootOrderNumber will
     * collide with another group that already has that orderNumber. Because of this, we need to compare the colliding group modified dates.
     * If we shift a group "up" in a list, we want the group being shifted (the one with the most recent modified date) to appear "on top" of the
     * current placeholder, requiring it to appear "less than" the placeholder. If we shift "down" in the list, we want the shifting group to
     * appear "below" the current placeholder, so the one with a more current modified date is deemed "greater than" the current placeholder.
     *
     * @param groups
     * @param shiftDirection
     */
    private void orderGroups(List<OrderedSharedIdType> groupOrderItems) {
        if (!groupOrderItems.isEmpty()) {
            Comparator<OrderedSharedIdType> compare = new Comparator<OrderedSharedIdType>() {

                @Override
                public int compare(OrderedSharedIdType o1, OrderedSharedIdType o2) {
                    return o1.getOrder().compareTo(o2.getOrder());
                }
            };
            Collections.sort(groupOrderItems, compare);
        }
    }

    private void orderSubGroups(List<GroupReferenceType> groups) {
        if (groups != null && groups.size() > 0) {
            Comparator<GroupReferenceType> compare = new Comparator<GroupReferenceType>() {

                @Override
                public int compare(GroupReferenceType o1, GroupReferenceType o2) {
                    return o1.getOrderNum().compareTo(o2.getOrderNum());
                }
            };
            Collections.sort(groups, compare);
        }
    }

    private void orderRuleRefs(List<RecommendationReferenceType> rules) {
        if (rules != null && rules.size() > 0) {
            Comparator<RecommendationReferenceType> compare = new Comparator<RecommendationReferenceType>() {

                @Override
                public int compare(RecommendationReferenceType o1, RecommendationReferenceType o2) {
                    return o1.getOrderNum().compareTo(o2.getOrderNum());
                }
            };
            Collections.sort(rules, compare);
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
        OrderedSharedIdsType currentOrderNumbers = app.getGroupOrderNumbers();
        if (currentOrderNumbers.getChangeType().equals(ChangeTypeEnum.NONE) == false) {
            List<OrderedSharedIdType> newOrderNumbersList = newOrderNumbers.getOrderedItemList(), currentOrderNumbersList = currentOrderNumbers.getOrderedItemList();
            List<GroupType> allGroups = app.getGroups().getGroupList(), allSubGroups = this.getAllSubGroups(app);
            for (OrderedSharedIdType item : currentOrderNumbersList) {
                String id = item.getStringValue();
                GroupType group = this.getItem(allGroups, id);
                if (group != null && group.getChangeType().equals(ChangeTypeEnum.NEW)) {
                    newOrderNumbersList.add(item);
                    changed = true;
                }
            }
            int index = 0;
            while (index < newOrderNumbersList.size()) {
                OrderedSharedIdType item = newOrderNumbersList.get(index);
                String id = item.getStringValue();
                GroupType group = this.getItem(allGroups, id);
                if (group == null || group.getChangeType().equals(ChangeTypeEnum.DELETED) || this.getItem(allSubGroups, id) != null) {
                    newOrderNumbersList.remove(index);
                    changed = true;
                } else index++;
            }
        }
        currentOrderNumbers.set(newOrderNumbers);
        return changed;
    }

    public void shiftGroup(ApplicationType app, GroupType group, BigInteger to) {
        IHelperImpl<OrderedSharedIdsType, ApplicationType> helper = new IHelperImpl<OrderedSharedIdsType, ApplicationType>();
        BigInteger from = this.getOrderNumber(app, group);
        if (from.equals(to) == false) {
            OrderedSharedIdsType copiedGroupOrders = (OrderedSharedIdsType) app.getGroupOrderNumbers().copy();
            OrderedSharedIdsType origGroupOrders = app.getGroupOrderNumbers();
            List<OrderedSharedIdType> copiedOrderedItems = new ArrayList<OrderedSharedIdType>(copiedGroupOrders.getOrderedItemList());
            List<OrderedSharedIdType> origOrderedItems = origGroupOrders.getOrderedItemList();
            this.orderGroups(copiedOrderedItems);
            int from_index = from.intValue() - 1, to_index = to.intValue() - 1;
            OrderedSharedIdType fromObject = copiedOrderedItems.get(from_index);
            OrderedSharedIdType fromCopy = (OrderedSharedIdType) fromObject.copy();
            if (from_index < to_index) {
                copiedOrderedItems.add(to_index + 1, fromCopy);
            } else {
                copiedOrderedItems.add(to_index, fromCopy);
            }
            copiedOrderedItems.remove(fromObject);
            this.alignGroups(copiedOrderedItems);
            while (origOrderedItems.size() > 0) origOrderedItems.remove(0);
            origOrderedItems.addAll(copiedOrderedItems);
            helper.markModified(origGroupOrders);
        }
    }

    /**
    * Aligns the orderNum attribute with the XML objects position in the document.
    * @param orderedItems
    * @return
    */
    boolean alignGroups(List<OrderedSharedIdType> orderedItems) {
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

    public boolean reOrderGroups(List<OrderedSharedIdType> orderedItems) {
        List<OrderedSharedIdType> listItems = new ArrayList<OrderedSharedIdType>(orderedItems);
        this.orderGroups(listItems);
        return this.alignGroups(listItems);
    }

    /**
     * Returns the max order number
     * @param app
     * @return
     */
    public BigInteger getMaxOrderNum(ApplicationType app) {
        if (app.isSetGroupOrderNumbers()) {
            return BigInteger.valueOf(app.getGroupOrderNumbers().sizeOfOrderedItemArray());
        }
        return BigInteger.ZERO;
    }

    public BigInteger getMaxOrderNum(RecommendationReferencesType recommendations) {
        BigInteger maxOrder = BigInteger.valueOf(0);
        for (RecommendationReferenceType rr : recommendations.getRecommendationReferenceList()) {
            BigInteger orderNumber = rr.getOrderNum();
            if (orderNumber != null) {
                if (rr.getOrderNum().compareTo(maxOrder) == 1) {
                    maxOrder = rr.getOrderNum();
                }
            }
        }
        return maxOrder;
    }

    public BigInteger getMaxOrderNum(GroupReferencesType groups) {
        BigInteger maxOrder = BigInteger.valueOf(0);
        for (GroupReferenceType rr : groups.getGroupReferenceList()) {
            BigInteger orderNumber = rr.getOrderNum();
            if (orderNumber != null) {
                if (rr.getOrderNum().compareTo(maxOrder) == 1) {
                    maxOrder = rr.getOrderNum();
                }
            }
        }
        return maxOrder;
    }

    public void shiftSubGroup(GroupType group, BigInteger from, BigInteger to) {
        if (from.equals(to) == false) {
            List<GroupReferenceType> groups = group.getSubGroups().getGroupReferenceList();
            GroupReferenceType shifting = (GroupReferenceType) groups.get(from.intValue() - 1).copy();
            int from_index = from.intValue() - 1, to_index = to.intValue() - 1;
            if (from_index < to_index) {
                while (from_index < to_index) {
                    GroupReferenceType tmpGroup = groups.get(++from_index);
                    tmpGroup.setOrderNum(BigInteger.valueOf(from_index));
                }
            } else {
                while (to_index < from_index) {
                    GroupReferenceType tmpGroup = groups.get(to_index++);
                    tmpGroup.setOrderNum(tmpGroup.getOrderNum().add(BigInteger.ONE));
                }
            }
            groups.remove(from.intValue() - 1);
            shifting.setOrderNum(to);
            groups.add(to.intValue() - 1, shifting);
            this.markModified(group);
        }
    }

    public boolean reOrderSubGroups(GroupType parent, List<GroupReferenceType> groups) {
        boolean orderChanged = false;
        this.orderSubGroups(groups);
        for (int i = 0; i < groups.size(); i++) {
            GroupReferenceType group = groups.get(i);
            BigInteger orderNum = BigInteger.valueOf(i + 1);
            BigInteger groupOrderNum = group.getOrderNum();
            if (groupOrderNum == null || orderNum.compareTo(groupOrderNum) != 0) {
                orderChanged = true;
                group.setOrderNum(orderNum);
                super.markModified(parent);
            }
        }
        return orderChanged;
    }

    public void shiftRuleRef(GroupType group, BigInteger from, BigInteger to) {
        if (from.equals(to) == false) {
            List<RecommendationReferenceType> rules = group.getRecommendations().getRecommendationReferenceList();
            RecommendationReferenceType shifting = (RecommendationReferenceType) rules.get(from.intValue() - 1).copy();
            int from_index = from.intValue() - 1, to_index = to.intValue() - 1;
            if (from_index < to_index) {
                while (from_index < to_index) {
                    RecommendationReferenceType tmpRule = rules.get(++from_index);
                    tmpRule.setOrderNum(BigInteger.valueOf(from_index));
                }
            } else {
                while (to_index < from_index) {
                    RecommendationReferenceType tmpRule = rules.get(to_index++);
                    tmpRule.setOrderNum(tmpRule.getOrderNum().add(BigInteger.ONE));
                }
            }
            rules.remove(from.intValue() - 1);
            shifting.setOrderNum(to);
            rules.add(to.intValue() - 1, shifting);
            this.markModified(group);
        }
    }

    public boolean reOrderRuleRefs(GroupType parent, List<RecommendationReferenceType> rules) {
        boolean orderChanged = false;
        this.orderRuleRefs(rules);
        for (int i = 0; i < rules.size(); i++) {
            RecommendationReferenceType rule = rules.get(i);
            BigInteger orderNum = BigInteger.valueOf(i + 1);
            BigInteger ruleOrderNum = rule.getOrderNum();
            if (ruleOrderNum == null || orderNum.compareTo(ruleOrderNum) != 0) {
                orderChanged = true;
                rule.setOrderNum(orderNum);
                super.markModified(parent);
            }
        }
        return orderChanged;
    }

    @Override
    public List<IdedVersionedItemType> getActiveReferencingItems(String lookingForId, XmlObject scopingObject) {
        ApplicationType application = (ApplicationType) scopingObject;
        List<IdedVersionedItemType> referencingObjects = new ArrayList<IdedVersionedItemType>();
        referencingObjects.addAll(getReferencingGroups(application, lookingForId));
        referencingObjects.addAll(getReferencingProfiles(application, lookingForId));
        referencingObjects.addAll(getGroupRequiredByGroups(application, lookingForId));
        referencingObjects.addAll(getGroupRequiredByRules(application, lookingForId));
        return referencingObjects;
    }

    public List<GroupType> getReferencingGroups(ApplicationType application, String groupId) {
        List<GroupType> referencingGroups = new ArrayList<GroupType>();
        if (application.isSetGroups()) {
            List<GroupType> groups = application.getGroups().getGroupList();
            for (GroupType currentGroup : groups) {
                if (currentGroup.getId().equals(groupId)) {
                    continue;
                }
                if (currentGroup.getChangeType().equals(ChangeTypeEnum.DELETED)) {
                    continue;
                }
                if (currentGroup.isSetSubGroups()) {
                    GroupReferencesType subgroups = currentGroup.getSubGroups();
                    if (subgroups != null) {
                        List<GroupReferenceType> groupRefs = subgroups.getGroupReferenceList();
                        for (GroupReferenceType gr : groupRefs) {
                            if (groupId.equals(gr.getStringValue())) {
                                logger.debug("found ref for group " + groupId + " in group " + currentGroup.getId());
                                referencingGroups.add(currentGroup);
                                break;
                            }
                        }
                    }
                }
            }
        }
        return referencingGroups;
    }

    public List<ProfileType> getReferencingProfiles(ApplicationType application, String groupId) {
        ArrayList<ProfileType> referencingProfiles = new ArrayList<ProfileType>();
        if (application.isSetProfiles()) {
            for (ProfileType profile : application.getProfiles().getProfileList()) {
                if (profile.getChangeType() != ChangeTypeEnum.DELETED && profile.isSetSelectGroups()) {
                    for (GroupSelectorType sg : profile.getSelectGroups().getSelectGroupList()) {
                        if (sg.getGroupId().equals(groupId)) {
                            logger.debug("found group " + groupId + " referenced in profile " + profile.getId());
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
     * 
     * @param application
     * @param groupId
     * @return the groups in the application that depend on the given group
     */
    public List<GroupType> getGroupRequiredByGroups(ApplicationType application, String groupId) {
        ArrayList<GroupType> requiringGroups = new ArrayList<GroupType>();
        if (application.isSetGroups()) {
            List<GroupType> groups = application.getGroups().getGroupList();
            for (GroupType currentGroup : groups) {
                if (currentGroup.getId().equals(groupId)) {
                    continue;
                }
                if (currentGroup.getChangeType().equals(ChangeTypeEnum.DELETED)) {
                    continue;
                }
                for (String requiredGroup : currentGroup.getGroupDependencyList()) {
                    if (requiredGroup.equals(groupId)) {
                        requiringGroups.add(currentGroup);
                        break;
                    }
                }
            }
        }
        return requiringGroups;
    }

    /**
     *
     * @param application
     * @param ruleId
     * @return the rules in the application that depend on the given group
     */
    public List<RecommendationType> getGroupRequiredByRules(ApplicationType application, String groupId) {
        ArrayList<RecommendationType> requiringRules = new ArrayList<RecommendationType>();
        if (application.isSetRecommendations()) {
            List<RecommendationType> rules = application.getRecommendations().getRecommendationList();
            for (RecommendationType currentRule : rules) {
                if (currentRule.getChangeType().equals(ChangeTypeEnum.DELETED)) {
                    continue;
                }
                for (String requiredRule : currentRule.getGroupDependencyList()) {
                    if (requiredRule.equals(groupId)) {
                        requiringRules.add(currentRule);
                        break;
                    }
                }
            }
        }
        return requiringRules;
    }

    public boolean isSubGroup(ApplicationType app, GroupType group) {
        final String groupId = group.getId();
        List<GroupType> allSubGroups = this.getAllSubGroups(app);
        for (GroupType subGroup : allSubGroups) {
            if (subGroup.getId().equals(groupId)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns a list of all the sub groups in the app
     * @param app
     * @return
     */
    public List<GroupType> getAllSubGroups(ApplicationType app) {
        List<GroupType> subGroups = new ArrayList<GroupType>();
        if (app.isSetGroups()) {
            List<GroupType> allGroups = app.getGroups().getGroupList();
            for (GroupType group : allGroups) {
                if (group.isSetSubGroups()) {
                    List<GroupReferenceType> groupRefs = group.getSubGroups().getGroupReferenceList();
                    for (GroupReferenceType groupRef : groupRefs) {
                        String id = groupRef.getStringValue();
                        GroupType subGroup = this.getItem(allGroups, id);
                        if (subGroup != null) {
                            subGroups.add(subGroup);
                        }
                    }
                }
            }
        }
        return subGroups;
    }

    public List<GroupType> getOrderedTopLevelGroups(ApplicationType app) {
        List<GroupType> topGroups = null;
        IHelperImpl<OrderedSharedIdsType, ApplicationType> helper = new IHelperImpl<OrderedSharedIdsType, ApplicationType>();
        if (app.isSetGroups() && app.isSetGroupOrderNumbers()) {
            List<GroupType> allGroups = app.getGroups().getGroupList();
            List<OrderedSharedIdType> orderedItems = new ArrayList<OrderedSharedIdType>(app.getGroupOrderNumbers().getOrderedItemList());
            this.orderGroups(orderedItems);
            boolean orderChanged = this.alignGroups(orderedItems);
            if (orderChanged) {
                helper.markModified(app.getGroupOrderNumbers());
            }
            topGroups = this.getActiveItems(allGroups, orderedItems);
        } else {
            topGroups = Collections.emptyList();
        }
        return topGroups;
    }

    public List<GroupType> getTopLevelGroups(ApplicationType app) {
        List<GroupType> topLevelGroups = null;
        if (app.isSetGroups() && app.isSetGroupOrderNumbers()) {
            List<OrderedSharedIdType> orderNumbers = app.getGroupOrderNumbers().getOrderedItemList();
            List<GroupType> allGroups = app.getGroups().getGroupList();
            topLevelGroups = this.getActiveItems(allGroups, orderNumbers);
        } else {
            topLevelGroups = Collections.emptyList();
        }
        return topLevelGroups;
    }

    @Override
    public boolean isReferenced(GroupType group, XmlObject xmlObject) {
        return (this.getActiveReferencingItems(group.getId(), xmlObject).size() > 0);
    }

    @Override
    public GroupType getNewItem(ApplicationType app) {
        GroupsType groups = (app.isSetGroups()) ? app.getGroups() : app.addNewGroups();
        GroupType newGroup = super.getNewItem(app);
        return newGroup;
    }

    @Override
    public String getNewId(ApplicationType app) {
        return IDManager.getNextGroupId(app);
    }

    @Override
    protected GroupType getInstance() {
        return GroupType.Factory.newInstance();
    }

    /**
     * Deletes a group along with all the Comments and References it contains
     * @param group
     */
    @Override
    public void markDeleted(ApplicationType application, GroupType group) {
        if (group.isSetCommentRefs() == true && application.isSetComments() == true) {
            CommentHelper commentHelper = new CommentHelper();
            List<SharedIdType> commentRefs = group.getCommentRefs().xgetCommentRefList();
            List<CommentType> allComments = application.getComments().getCommentList();
            commentHelper.deleteReferences(application, allComments, commentRefs);
        }
        if (group.isSetReferences() == true && application.isSetReferences()) {
            ReferenceHelper referenceHelper = new ReferenceHelper();
            List<SharedIdType> referenceRefs = group.getReferences().xgetReferenceRefList();
            List<ReferenceType> allReferences = application.getReferences().getReferenceList();
            referenceHelper.deleteReferences(application, allReferences, referenceRefs);
        }
        this.removeGroupOrderItem(application, group);
        super.markDeleted(application, group);
    }

    /**
     * Remove given dependencies from the group
     *
     */
    public void removeDependency(ApplicationType application, GroupType group, RecommendationType recDep) throws RTClientException, DataManagerException {
        List<RecommendationType> deps = this.getDependencies(application, group);
        for (int i = 0; i < deps.size(); i++) {
            if (recDep.getId().equals(deps.get(i).getId())) {
                group.removeDependency(i);
            }
        }
    }

    /**
     * Remove given dependencies from the group
     *
     * @param recommendation
     */
    public void removeDependency(ApplicationType application, GroupType group, GroupType groupDep) throws RTClientException, DataManagerException {
        List<GroupType> deps = this.getGroupDependencies(application, group);
        for (int i = 0; i < deps.size(); i++) {
            if (groupDep.getId().equals(deps.get(i).getId())) {
                group.removeGroupDependency(i);
            }
        }
    }
}
