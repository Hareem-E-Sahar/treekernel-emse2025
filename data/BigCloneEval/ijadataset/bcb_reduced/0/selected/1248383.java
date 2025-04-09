package com.germinus.portlet.content_admin.action;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Iterator;
import java.util.Stack;
import java.util.zip.ZipOutputStream;
import java.util.zip.ZipEntry;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import javax.jcr.ItemNotFoundException;
import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.portlet.PortletConfig;
import javax.portlet.PortletRequest;
import javax.portlet.PortletResponse;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;
import javax.servlet.ServletContext;
import com.germinus.xpression.groupware.util.LiferayHelperFactory;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;
import org.apache.struts.action.ActionMessages;
import org.apache.struts.action.DynaActionForm;
import com.germinus.portlet.content_admin.model.PaginatedListBean;
import com.germinus.xpression.cms.CMSQuery;
import com.germinus.xpression.cms.CMSResult;
import com.germinus.xpression.cms.CMSRuntimeException;
import com.germinus.xpression.cms.action.CMSPortletAction;
import com.germinus.xpression.cms.action.LookupDispatchPortletAction;
import com.germinus.xpression.cms.action.ScribePortletActionHelper;
import com.germinus.xpression.cms.contents.Content;
import com.germinus.xpression.cms.contents.ContentManager;
import com.germinus.xpression.cms.contents.ContentNotFoundException;
import com.germinus.xpression.cms.contents.ContentStatus;
import com.germinus.xpression.cms.contents.ContentType;
import com.germinus.xpression.cms.contents.DraftContent;
import com.germinus.xpression.cms.contents.FieldDefinition;
import com.germinus.xpression.cms.contents.MalformedContentException;
import com.germinus.xpression.cms.contents.PublishedContent;
import com.germinus.xpression.cms.directory.DirectoryFolder;
import com.germinus.xpression.cms.directory.DirectoryItemNotFoundException;
import com.germinus.xpression.cms.directory.DirectoryPersister;
import com.germinus.xpression.cms.directory.MalformedDirectoryItemException;
import com.germinus.xpression.cms.jcr.DirectoryFolderNode;
import com.germinus.xpression.cms.jcr.JCRUtil;
import com.germinus.xpression.cms.model.ScribeRequest;
import com.germinus.xpression.cms.service.SelectedContentsService;
import com.germinus.xpression.cms.util.ManagerRegistry;
import com.germinus.xpression.cms.worlds.World;
import com.germinus.xpression.cms.worlds.WorldManager;
import com.germinus.xpression.groupware.Authorizator;
import com.germinus.xpression.groupware.CommunityManager;
import com.germinus.xpression.groupware.GroupwareUser;
import com.germinus.xpression.groupware.NotAuthorizedException;
import com.germinus.xpression.groupware.action.GroupwareHelper;
import com.germinus.xpression.groupware.communities.Community;
import com.germinus.xpression.groupware.service.UserCommunitiesService;
import com.germinus.xpression.groupware.util.GroupwareManagerRegistry;
import com.germinus.xpression.groupware.util.GroupwareConfig;
import com.germinus.xpression.i18n.I18NUtils;
import com.liferay.portal.kernel.util.ParamUtil;
import com.liferay.portlet.PortletContextImpl;

public class ManageContentsAction extends LookupDispatchPortletAction {

    /**
     *
     */
    private static final int DEFAULT_PAGE_SIZE = 10;

    private static final Log log = LogFactory.getLog(ManageContentsAction.class);

    public String USER_ATTRIBUTE = "user";

    private static final String EXPORTED_CONTENTS_PATH = GroupwareConfig.getExportedCommunitiesZipsPath();

    private static final String CONTENTS_ZIP_PREFIX = "CONTENTS_";

    private static final String ZIP_FILE_EXTENSION = ".zip";

    public ActionForward restore(ActionMapping mapping, ActionForm form, PortletConfig portletConfig, PortletRequest request, PortletResponse response) throws CMSRuntimeException {
        ContentManager contentManager = ManagerRegistry.getContentManager();
        WorldManager worldManager = ManagerRegistry.getWorldManager();
        CommunityManager communityManager = GroupwareManagerRegistry.getCommunityManager();
        Authorizator authorizator = GroupwareManagerRegistry.getAuthorizator();
        GroupwareUser groupwareUser = GroupwareHelper.getUser(request);
        DynaActionForm dform = (DynaActionForm) form;
        String[] listContentsIDs = (String[]) dform.get("listContentsIDs");
        String workspace = currentWorkspace(request);
        String worldId = LiferayHelperFactory.getLiferayHelper().getWorldId(form, ListContentsAction.CURRENT_WORLD_ID_PARAM, request);
        ActionMessages msgs = new ActionMessages();
        ActionMessages errors = new ActionMessages();
        if (listContentsIDs.length < 1) {
            msgs.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("content_admin.none.selected"));
        }
        int contentsRestored = 0;
        for (int i = 0; i < listContentsIDs.length; i++) {
            String contentId = (String) listContentsIDs[i];
            String contentName = "";
            try {
                log.info("Restauring content with ID:" + contentId);
                final Content content = contentManager.getContentById(contentId, workspace);
                World world = worldManager.getOwnerWorld(content);
                Community community = communityManager.getOwnerCommunity(world);
                contentName = content.getName();
                authorizator.assertAdminAuthorization(groupwareUser, community);
                contentManager.restore(content);
                contentsRestored++;
            } catch (NotAuthorizedException e1) {
                errors.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("content_admin.notAuthorized", contentName));
            } catch (ContentNotFoundException e2) {
                errors.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("content_admin.notFound", contentId));
            } catch (MalformedContentException e) {
                errors.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("content_admin.malformedContent", contentId));
            }
        }
        if (contentsRestored > 0) msgs.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("content_admin.restored.success", String.valueOf(listContentsIDs.length)));
        saveMessages(request, msgs);
        saveErrors(request, errors);
        ActionForward forward = new ActionForward("restore?worldId=" + worldId, null, true);
        return forward;
    }

    private String currentWorkspace(PortletRequest request) {
        String workspace = new ScribeRequest(request).calculateCurrentWorkspace();
        return workspace;
    }

    public ActionForward moveToTrash(ActionMapping mapping, ActionForm form, PortletConfig portletConfig, PortletRequest request, PortletResponse response) throws CMSRuntimeException {
        ContentManager contentManager = ManagerRegistry.getContentManager();
        WorldManager worldManager = ManagerRegistry.getWorldManager();
        DirectoryPersister directoryPersister = ManagerRegistry.getDirectoryPersister();
        CommunityManager communityManager = GroupwareManagerRegistry.getCommunityManager();
        Authorizator authorizator = GroupwareManagerRegistry.getAuthorizator();
        GroupwareUser groupwareUser = GroupwareHelper.getUser(request);
        DynaActionForm dform = (DynaActionForm) form;
        String[] listContentsIDs = (String[]) dform.get("listContentsIDs");
        String workspace = currentWorkspace(request);
        String[] listFolderPaths = (String[]) dform.get("listFolderPaths");
        String worldId = LiferayHelperFactory.getLiferayHelper().getWorldId(form, ListContentsAction.CURRENT_WORLD_ID_PARAM, request);
        ActionMessages msgs = new ActionMessages();
        ActionMessages errors = new ActionMessages();
        if ((listContentsIDs.length < 1) && (listFolderPaths.length < 1)) {
            msgs.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("content_admin.none.selected"));
        }
        int contentsMoved = 0;
        for (int i = 0; i < listContentsIDs.length; i++) {
            String contentId = (String) listContentsIDs[i];
            String contentName = "";
            try {
                log.info("Moving to trash content with ID:" + contentId);
                final Content content = contentManager.getContentById(contentId, workspace);
                World world = worldManager.getOwnerWorld(content);
                Community community = communityManager.getOwnerCommunity(world);
                contentName = content.getName();
                authorizator.assertAdminAuthorization(groupwareUser, community);
                contentManager.moveToTrash(content);
                contentsMoved++;
            } catch (NotAuthorizedException e1) {
                errors.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("content_admin.notAuthorized", contentName));
            } catch (ContentNotFoundException e2) {
                errors.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("content_admin.notFound", contentId));
            } catch (MalformedContentException e) {
                errors.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("content_admin.malformedContent", contentId));
            }
        }
        if (contentsMoved > 0) msgs.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("content_admin.movetotrash.success", String.valueOf(listContentsIDs.length)));
        if (listFolderPaths.length > 0) {
            for (int index = 0; index < listFolderPaths.length; index++) {
                String listFolderPath = listFolderPaths[index];
                try {
                    DirectoryFolder folder = (DirectoryFolder) directoryPersister.getItemFromPath(listFolderPath);
                    if (directoryPersister.isEmpty(folder)) {
                        directoryPersister.deleteItem(folder);
                        msgs.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("content_admin.delete.success", String.valueOf(listFolderPaths.length)));
                    } else {
                        errors.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("content_admin.movetotrash.folder.notEmpty", folder.getName()));
                    }
                } catch (DirectoryItemNotFoundException e) {
                    errors.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("content_admin.notAuthorized", ""));
                } catch (ItemNotFoundException e) {
                    errors.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("content_admin.notFound", listFolderPath));
                }
            }
        }
        saveMessages(request, msgs);
        saveErrors(request, errors);
        ActionForward forward = new ActionForward("moveToTrash?worldId=" + worldId, null, true);
        return forward;
    }

    public ActionForward massiveMoveToTrash(ActionMapping mapping, ActionForm form, PortletConfig portletConfig, PortletRequest request, PortletResponse response) throws CMSRuntimeException {
        ContentManager contentManager = ManagerRegistry.getContentManager();
        DirectoryPersister directoryPersister = ManagerRegistry.getDirectoryPersister();
        String worldId = LiferayHelperFactory.getLiferayHelper().getWorldId(form, ListContentsAction.CURRENT_WORLD_ID_PARAM, request);
        ActionMessages msgs = new ActionMessages();
        ActionMessages errors = new ActionMessages();
        try {
            DirectoryFolder currentFolder = currentFolder(request);
            if (isMainFolder(currentFolder)) {
                errors.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("content_admin.massiveMoveToTrash.error.root.can.not.be.removed"));
                saveErrors(request, errors);
                ActionForward forward = new ActionForward("moveToTrash?worldId=" + worldId, null, true);
                return forward;
            }
            String workspace = currentWorkspace(request);
            Node currentFolderNode = JCRUtil.getNodeById(currentFolder.getId(), workspace);
            Node parentNode = currentFolderNode.getParent();
            String parentFolderId = parentNode.getIdentifier();
            recursiveMoveToTrash(contentManager, directoryPersister, msgs, currentFolder);
            request.getPortletSession().setAttribute(CMSPortletAction.CURRENT_FOLDER_ID, parentFolderId);
            request.getPortletSession().setAttribute(CMSPortletAction.CURRENT_WORKSPACE, workspace);
        } catch (MalformedDirectoryItemException e) {
            String msg = "MalformedDirectoryItemException at massiveMoveToTrash";
            log.error(msg);
            throw new CMSRuntimeException(msg, e);
        } catch (DirectoryItemNotFoundException e) {
            String msg = "DirectoryItemNotFoundException at massiveMoveToTrash";
            log.error(msg);
            throw new CMSRuntimeException(msg, e);
        } catch (RepositoryException e) {
            String msg = "RepositoryException at massiveMoveToTrash";
            log.error(msg);
            throw new CMSRuntimeException(msg, e);
        } catch (ContentNotFoundException e) {
            String msg = "ContentNotFoundException at massiveMoveToTrash";
            log.error(msg);
            throw new CMSRuntimeException(msg, e);
        }
        saveMessages(request, msgs);
        saveErrors(request, errors);
        ActionForward forward = new ActionForward("moveToTrash?worldId=" + worldId, null, true);
        return forward;
    }

    private boolean isMainFolder(DirectoryFolder currentFolder) {
        return currentFolder.getRootFolderId().equals(currentFolder.getId());
    }

    private void recursiveMoveToTrash(ContentManager contentManager, DirectoryPersister directoryPersister, ActionMessages msgs, DirectoryFolder currentFolder) throws ItemNotFoundException, ContentNotFoundException {
        if (directoryPersister.isEmpty(currentFolder)) {
            directoryPersister.deleteItem(currentFolder);
            msgs.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("content_admin.massiveMoveToTrash.success"));
        } else {
            Iterator<Content> iterator = currentFolder.recursiveContentIterator();
            while (iterator.hasNext()) {
                Content currentContent = iterator.next();
                contentManager.moveToTrashNow(currentContent);
            }
            Stack<DirectoryFolder> stack = new Stack<DirectoryFolder>();
            stack.push(currentFolder);
            moveToTrashFolders(directoryPersister, currentFolder, stack, Boolean.TRUE);
            msgs.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("content_admin.massiveMoveToTrash.success"));
        }
    }

    private void moveToTrashFolders(DirectoryPersister directoryPersister, DirectoryFolder currentFolder, Stack<DirectoryFolder> stack, Boolean firstFolder) throws ItemNotFoundException, ContentNotFoundException {
        List<DirectoryFolder> folderList = directoryPersister.listFolders(currentFolder);
        for (DirectoryFolder folder : folderList) {
            stack.push(folder);
            moveToTrashFolders(directoryPersister, folder, stack, Boolean.FALSE);
        }
        if (firstFolder) {
            int originalStackSize = stack.size();
            for (int i = 0; i < originalStackSize; i++) {
                directoryPersister.deleteItem(stack.pop());
            }
        }
    }

    public ActionForward emptyTrash(ActionMapping mapping, ActionForm form, PortletConfig portletConfig, PortletRequest request, PortletResponse response) throws CMSRuntimeException {
        String workspace = currentWorkspace(request);
        String worldId = LiferayHelperFactory.getLiferayHelper().getWorldId(form, ListContentsAction.CURRENT_WORLD_ID_PARAM, request);
        ActionMessages msgs = new ActionMessages();
        ActionMessages errors = new ActionMessages();
        ActionForward forward = new ActionForward("delete?worldId=" + worldId, null, true);
        WorldManager worldManager = ManagerRegistry.getWorldManager();
        CommunityManager communityManager = GroupwareManagerRegistry.getCommunityManager();
        Authorizator authorizator = GroupwareManagerRegistry.getAuthorizator();
        GroupwareUser groupwareUser = GroupwareHelper.getUser(request);
        World world = worldManager.getWorldById(worldId, workspace);
        Community community = communityManager.getOwnerCommunity(world);
        try {
            authorizator.assertAdminAuthorization(groupwareUser, community);
            worldManager.emptyTrash(world);
            msgs.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("content_admin.trash.emptied"));
        } catch (NotAuthorizedException e) {
            errors.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("content_admin.notAuthorized", community.getName()));
        }
        saveMessages(request, msgs);
        saveErrors(request, errors);
        return forward;
    }

    public ActionForward delete(ActionMapping mapping, ActionForm form, PortletConfig portletConfig, PortletRequest request, PortletResponse response) throws CMSRuntimeException {
        DynaActionForm dform = (DynaActionForm) form;
        String[] listContentsIDs = (String[]) dform.get("listContentsIDs");
        String[] listFolderPaths = (String[]) dform.get("listFolderPaths");
        String workspace = currentWorkspace(request);
        String worldId = LiferayHelperFactory.getLiferayHelper().getWorldId(form, ListContentsAction.CURRENT_WORLD_ID_PARAM, request);
        ContentManager contentManager = ManagerRegistry.getContentManager();
        WorldManager worldManager = ManagerRegistry.getWorldManager();
        CommunityManager communityManager = GroupwareManagerRegistry.getCommunityManager();
        Authorizator authorizator = GroupwareManagerRegistry.getAuthorizator();
        GroupwareUser groupwareUser = GroupwareHelper.getUser(request);
        String contentId = "";
        String contentName = "";
        ActionMessages msgs = new ActionMessages();
        ActionMessages errors = new ActionMessages();
        ActionForward forward = new ActionForward("delete?worldId=" + worldId, null, true);
        if ((listContentsIDs.length < 1) && (listFolderPaths.length < 1)) {
            msgs.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("content_admin.none.selected"));
        }
        for (int i = 0; i < listContentsIDs.length; i++) {
            try {
                contentId = (String) listContentsIDs[i];
                log.info("Deleting content with ID:" + contentId);
                final Content content = contentManager.getContentById(contentId, workspace);
                World world = worldManager.getOwnerWorld(content);
                Community community = communityManager.getOwnerCommunity(world);
                contentName = content.getName();
                authorizator.assertAdminAuthorization(groupwareUser, community);
                contentManager.deleteContent(content);
            } catch (NotAuthorizedException e1) {
                errors.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("content_admin.notAuthorized", contentName));
            } catch (ContentNotFoundException e2) {
                errors.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("content_admin.notFound", contentId));
            } catch (MalformedContentException e) {
                errors.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("content_admin.malformedContent", contentId));
            }
        }
        if (listContentsIDs.length > 0) {
            msgs.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("content_admin.delete.success", String.valueOf(listContentsIDs.length)));
        }
        saveMessages(request, msgs);
        saveErrors(request, errors);
        return forward;
    }

    public ActionForward copy(ActionMapping mapping, ActionForm form, PortletConfig portletConfig, PortletRequest request, PortletResponse response) {
        DynaActionForm dform = (DynaActionForm) form;
        String[] listContentsIDs = (String[]) dform.get("listContentsIDs");
        String worldId = LiferayHelperFactory.getLiferayHelper().getWorldId(form, ListContentsAction.CURRENT_WORLD_ID_PARAM, request);
        String workspace = new ScribeRequest(request).calculateCurrentWorkspace();
        ContentManager contentManager = ManagerRegistry.getContentManager();
        WorldManager worldManager = ManagerRegistry.getWorldManager();
        CommunityManager communityManager = GroupwareManagerRegistry.getCommunityManager();
        Authorizator authorizator = GroupwareManagerRegistry.getAuthorizator();
        GroupwareUser groupwareUser = GroupwareHelper.getUser(request);
        String contentId = "";
        String contentName = "";
        ActionMessages msgs = new ActionMessages();
        ActionMessages errors = new ActionMessages();
        ActionForward forward = new ActionForward("list?worldId=" + worldId, null, true);
        if ((listContentsIDs.length < 1)) {
            msgs.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("content_admin.none.selected"));
        }
        for (int i = 0; i < listContentsIDs.length; i++) {
            try {
                contentId = (String) listContentsIDs[i];
                log.info("Copying content with ID:" + contentId);
                final Content content = contentManager.getContentById(contentId, workspace);
                World world = worldManager.getOwnerWorld(content);
                Community community = communityManager.getOwnerCommunity(world);
                contentName = content.getName();
                authorizator.assertAdminAuthorization(groupwareUser, community);
                String localCopyMessage = I18NUtils.getLocalizedMessage("content_admin.copy_of_content");
                StringBuffer newName = new StringBuffer();
                if (!StringUtils.contains(contentName, localCopyMessage)) newName.append(localCopyMessage).append(" ").append(contentName); else newName.append(contentName);
                contentManager.copyContentToFolderWithNewName(content, currentFolder(request), newName.toString());
                msgs.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("content_admin.copy.success", newName.toString()));
                forward = new ActionForward("list?worldId=" + world.getId(), null, true);
            } catch (NotAuthorizedException e1) {
                errors.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("content_admin.notAuthorized", contentName));
            } catch (ContentNotFoundException e2) {
                errors.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("content_admin.notFound", contentId));
            } catch (MalformedContentException e3) {
                errors.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("content_admin.malformedContent", contentId));
            } catch (MalformedDirectoryItemException e) {
                errors.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("content_admin.malformedItem", (String) request.getPortletSession().getAttribute(ContentAdminAction.CURRENT_FOLDER_ID)));
            } catch (DirectoryItemNotFoundException e) {
                errors.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("content_admin.notFound", (String) request.getPortletSession().getAttribute(ContentAdminAction.CURRENT_FOLDER_ID)));
            }
        }
        saveMessages(request, msgs);
        saveErrors(request, errors);
        return forward;
    }

    public ActionForward filter(ActionMapping mapping, ActionForm form, PortletConfig portletConfig, PortletRequest request, PortletResponse response) {
        ActionMessages actionErrors = new ActionMessages();
        try {
            DynaActionForm dform = (DynaActionForm) form;
            ContentManager contentManager = ManagerRegistry.getContentManager();
            CommunityManager communityManager = GroupwareManagerRegistry.getCommunityManager();
            Authorizator authorizator = GroupwareManagerRegistry.getAuthorizator();
            GroupwareUser groupwareUser = GroupwareHelper.getUser(request);
            WorldManager worldManager = ManagerRegistry.getWorldManager();
            DirectoryFolder folder = currentFolder(request);
            String worldId = LiferayHelperFactory.getLiferayHelper().getWorldId(form, ListContentsAction.CURRENT_WORLD_ID_PARAM, request);
            String contentTypeId = (String) dform.get("contentTypeId");
            String searchText = (String) dform.get("searchText");
            String searchCategory = (String) dform.get("searchCategory");
            String selectedTabId = ContentAdminConstants.CONTENTS_TAB_ID;
            String sortingOrder = request.getParameter("dir");
            String sortingCriteria = request.getParameter("sort");
            GroupwareUser user = GroupwareHelper.getUser(request);
            ContentType contentType = new ContentType();
            if (!contentTypeId.equals("allContents")) {
                Long contentTypeIdLong = new Long(contentTypeId);
                contentType = contentManager.getContentType(contentTypeIdLong);
            }
            World world;
            Community currentCommunity;
            if (StringUtils.isNotEmpty(worldId)) {
                world = worldManager.findWorldById(worldId);
                currentCommunity = communityManager.getOwnerCommunity(world);
            } else {
                currentCommunity = communityManager.getPersonalCommunity(groupwareUser);
                world = currentCommunity.getWorld();
                worldId = world.getId();
            }
            authorizator.assertViewAuthorization(currentCommunity, groupwareUser);
            log.info("User " + groupwareUser.getId() + " is getting contents from world with id: " + worldId);
            SelectedContentsService service = new SelectedContentsService();
            Integer pagesize = (Integer) request.getPortletSession().getAttribute("pagesize");
            Integer page = ParamUtil.getInteger(request, "page", 1);
            CMSQuery query = service.buildCMSQuery(contentType, searchText, worldId, pagesize, folder.getURLPath(), searchCategory, page);
            query.setPublicationState(CMSQuery.ALL_PUBLICATION_STATES);
            if (StringUtils.isNotEmpty(sortingCriteria)) query.setSortingCriteria(sortingCriteria);
            if (StringUtils.isNotEmpty(sortingOrder)) query.setSortingOrder(sortingOrder);
            CMSResult result = service.getCMSResultContentsByWorldId(user, contentType, worldId, query);
            PaginatedListBean folderElementsList = new PaginatedListBean(result);
            log.info("WorkContents: " + folderElementsList);
            request.setAttribute("selectedTabId", selectedTabId);
            request.setAttribute("currentWorld", world);
            request.setAttribute("currentCommunity", currentCommunity);
            request.setAttribute("communitySizeInKbytes", Long.toString(communityManager.getCommunitySize(currentCommunity.getId()) / 1024));
            request.setAttribute("communityPercent", Long.toString((communityManager.getCommunitySize(currentCommunity.getId()) / 1024) / (5 * 1024 * 1024)));
            request.setAttribute(ContentAdminAction.CONTENTS_LIST, folderElementsList);
            request.setAttribute(ContentAdminAction.FOLDER_TREE, world.getContentFolder());
            request.setAttribute(USER_ATTRIBUTE, groupwareUser);
            request.setAttribute("pagesize", pagesize);
            request.setAttribute("availableTargetTree", ContentAdminAction.availableTargetFoldersTree(world.getContentFolder(), folder));
            return mapping.findForward("filterList");
        } catch (Exception e) {
            actionErrors.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("content_admin.error.filtering"));
            saveErrors(request, actionErrors);
            return mapping.findForward("error");
        }
    }

    private DirectoryFolder currentFolder(PortletRequest request) throws MalformedDirectoryItemException, DirectoryItemNotFoundException {
        DirectoryPersister directoryPersister = ManagerRegistry.getDirectoryPersister();
        String currentFolderId = (String) request.getPortletSession().getAttribute(ContentAdminAction.CURRENT_FOLDER_ID);
        String workspace = new ScribeRequest(request).calculateCurrentWorkspace();
        DirectoryFolder folder = (DirectoryFolder) directoryPersister.getItemByUUIDWorkspace(currentFolderId, workspace);
        return folder;
    }

    public ActionForward changePaging(ActionMapping mapping, ActionForm form, PortletConfig portletConfig, PortletRequest request, PortletResponse response) {
        getPageSize(request);
        return mapping.findForward("list");
    }

    public ActionForward changeCommunity(ActionMapping mapping, ActionForm form, PortletConfig portletConfig, PortletRequest request, PortletResponse response) {
        DynaActionForm dform = (DynaActionForm) form;
        String communityId = (String) dform.get("community");
        request.setAttribute("worldId", communityId);
        return mapping.findForward("list");
    }

    public ActionForward send(ActionMapping mapping, ActionForm form, PortletConfig portletConfig, PortletRequest request, PortletResponse response) throws NotAuthorizedException {
        DynaActionForm dform = (DynaActionForm) form;
        String[] listContentsIDs = (String[]) dform.get("listContentsIDs");
        String worldId = LiferayHelperFactory.getLiferayHelper().getWorldId(form, ListContentsAction.CURRENT_WORLD_ID_PARAM, request);
        if (listContentsIDs.length < 1) {
            log.info("There is no any selected content to be moved");
            ActionMessages msgs = new ActionMessages();
            request.setAttribute("sourceWorldId", worldId);
            msgs.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("content_admin.none.selected"));
            saveErrors(request, msgs);
            return mapping.findForward("moveError");
        } else {
            log.info("Moving contents: forwarding to select the target world");
            request.setAttribute("sourceWorldId", worldId);
            request.setAttribute("listContentsIDs", listContentsIDs);
            request.setAttribute("targetCommunities", getTargetCommunities(request));
            CommunityManager communityManager = GroupwareManagerRegistry.getCommunityManager();
            WorldManager wm = ManagerRegistry.getWorldManager();
            Community community = communityManager.getOwnerCommunity(wm.findWorldById(worldId));
            request.setAttribute("currentCommunity", community);
            return mapping.findForward("selectTargetCommunity");
        }
    }

    public ActionForward sendToBag(ActionMapping mapping, ActionForm form, PortletConfig portletConfig, PortletRequest request, PortletResponse response) throws NotAuthorizedException {
        DynaActionForm dform = (DynaActionForm) form;
        String[] listContentsIDs = (String[]) dform.get("listContentsIDs");
        String worldId = LiferayHelperFactory.getLiferayHelper().getWorldId(form, ListContentsAction.CURRENT_WORLD_ID_PARAM, request);
        if (listContentsIDs.length < 1) {
            log.info("There is no any selected content to be moved");
            ActionMessages msgs = new ActionMessages();
            request.setAttribute("sourceWorldId", worldId);
            msgs.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("content_admin.none.selected"));
            saveErrors(request, msgs);
            return mapping.findForward("moveError");
        } else {
            log.info("Moving contents: forwarding to select the target world");
            request.setAttribute("sourceWorldId", worldId);
            request.setAttribute("listContentsIDs", listContentsIDs);
            request.setAttribute("targetCommunities", getTargetCommunities(request));
            CommunityManager communityManager = GroupwareManagerRegistry.getCommunityManager();
            WorldManager wm = ManagerRegistry.getWorldManager();
            Community community = communityManager.getOwnerCommunity(wm.findWorldById(worldId));
            request.setAttribute("currentCommunity", community);
            return mapping.findForward("selectTargetUserBags");
        }
    }

    public ActionForward removeAllListings(ActionMapping mapping, ActionForm form, PortletConfig portletConfig, PortletRequest request, PortletResponse response) {
        String folderId = (String) request.getPortletSession().getAttribute(CMSPortletAction.CURRENT_FOLDER_ID);
        String workspace = (String) request.getPortletSession().getAttribute(CMSPortletAction.CURRENT_WORKSPACE);
        Node node = null;
        try {
            node = JCRUtil.getNodeById(folderId, workspace);
        } catch (RepositoryException e) {
            log.error("Error getting node of the folder with id " + folderId);
        }
        DirectoryFolderNode directoryFolderNode = new DirectoryFolderNode(node);
        directoryFolderNode.removeAllListings();
        return mapping.findForward("list");
    }

    public ActionForward publish(ActionMapping mapping, ActionForm form, PortletConfig portletConfig, PortletRequest request, PortletResponse response) {
        ContentManager contentManager = ManagerRegistry.getContentManager();
        WorldManager worldManager = ManagerRegistry.getWorldManager();
        DirectoryPersister directoryPersister = ManagerRegistry.getDirectoryPersister();
        DynaActionForm dform = (DynaActionForm) form;
        String[] contentIds = dform.getStrings("listContentsIDs");
        String workspace = currentWorkspace(request);
        List<String> contentIdsList = Arrays.asList(contentIds);
        ListIterator<String> contentIdsListIterator = contentIdsList.listIterator();
        ActionMessages msgs = new ActionMessages();
        ActionMessages errors = new ActionMessages();
        int numberPublishedContents = 0;
        while (contentIdsListIterator.hasNext()) {
            String contentId = contentIdsListIterator.next();
            Content contentToPublish;
            try {
                contentToPublish = contentManager.getContentById(contentId, workspace);
                if (contentToPublish instanceof PublishedContent) {
                    PublishedContent publishedContent = (PublishedContent) contentToPublish;
                    if ((publishedContent).getDraftContent() != null) {
                        Boolean publishDraft = (Boolean) dform.get("publishDraft");
                        if (publishDraft) contentToPublish = (publishedContent).getDraftContent();
                    }
                }
                List<FieldDefinition> requiredFields = contentToPublish.validateRequiredFields();
                if (requiredFields.isEmpty()) {
                    if (contentToPublish.isDraft()) {
                        contentManager.publishContent((DraftContent) contentToPublish, worldManager.getOwnerWorld(contentToPublish), directoryPersister.getOwnerFolder(contentToPublish));
                        numberPublishedContents++;
                    } else {
                        if (contentToPublish.calculateContentStatus() == ContentStatus.expired) {
                            contentToPublish.setExpirationDate(null);
                            contentManager.modifyPublicationDates((PublishedContent) contentToPublish);
                            numberPublishedContents++;
                        }
                        if (contentToPublish.calculateContentStatus() == ContentStatus.notPublished) {
                            contentToPublish.setPublicationDate(null);
                            contentManager.modifyPublicationDates((PublishedContent) contentToPublish);
                            numberPublishedContents++;
                        }
                    }
                } else {
                    errors.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("content_admin.publish.error.missingRequiredFields", contentToPublish.getName()));
                    saveErrors(request, errors);
                }
            } catch (ContentNotFoundException e) {
                errors.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("content_admin.publish.error.contentNotFound", contentId));
                saveErrors(request, errors);
            } catch (MalformedContentException e) {
                errors.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("content_admin.publish.error.malformedContent", contentId));
                saveErrors(request, errors);
            } catch (MalformedDirectoryItemException e) {
                errors.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("content_admin.publish.error.malformedDirectory", contentId));
                saveErrors(request, errors);
            }
        }
        if (numberPublishedContents > 0) {
            msgs.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("content_admin.publish.success", numberPublishedContents));
            saveMessages(request, msgs);
        } else if (numberPublishedContents == 0) {
            msgs.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("content_admin.publish.error.noContentPublished"));
            saveMessages(request, msgs);
        }
        return mapping.findForward("publish");
    }

    public ActionForward unpublish(ActionMapping mapping, ActionForm form, PortletConfig portletConfig, PortletRequest request, PortletResponse response) {
        ContentManager contentManager = ManagerRegistry.getContentManager();
        DynaActionForm dform = (DynaActionForm) form;
        String[] contentIds = dform.getStrings("listContentsIDs");
        String workpsace = new ScribeRequest(request).calculateCurrentWorkspace();
        List<String> contentIdsList = Arrays.asList(contentIds);
        ListIterator<String> contentIdsListIterator = contentIdsList.listIterator();
        ActionMessages msgs = new ActionMessages();
        ActionMessages errors = new ActionMessages();
        int numberUnpublishedContents = 0;
        Content contentToUnpublish;
        while (contentIdsListIterator.hasNext()) {
            String contentId = contentIdsListIterator.next();
            try {
                contentToUnpublish = contentManager.getContentById(contentId, workpsace);
                if (!contentToUnpublish.isDraft() && contentToUnpublish.calculateContentStatus() == ContentStatus.published) {
                    contentManager.unpublishContent(contentToUnpublish);
                    numberUnpublishedContents++;
                } else {
                    errors.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("content_admin.unpublish.error.idDraft", contentId));
                    saveErrors(request, errors);
                }
            } catch (ContentNotFoundException e) {
                errors.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("content_admin.unpublish.error.malformedContent", contentId));
                saveErrors(request, errors);
            } catch (MalformedContentException e) {
                errors.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("content_admin.unpublish.error.malformedContent", contentId));
                saveErrors(request, errors);
            }
        }
        if (numberUnpublishedContents > 0) {
            msgs.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("content_admin.unpublish.multiple.success", numberUnpublishedContents));
            saveMessages(request, msgs);
        } else if (numberUnpublishedContents == 0) {
            msgs.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("content_admin.unpublish.error.noContentPublished"));
            saveMessages(request, msgs);
        }
        return mapping.findForward("unpublish");
    }

    public ActionForward moveToFolder(ActionMapping mapping, ActionForm form, PortletConfig portletConfig, PortletRequest request, PortletResponse response) {
        ContentManager contentManager = ManagerRegistry.getContentManager();
        DynaActionForm dform = (DynaActionForm) form;
        String[] contentIds = dform.getStrings("listContentsIDs");
        List<String> contentIdsList = Arrays.asList(contentIds);
        ListIterator<String> contentIdsListIterator = contentIdsList.listIterator();
        DirectoryPersister directoryPersister = ManagerRegistry.getDirectoryPersister();
        DirectoryFolder destinationFolder;
        String destinationFolderID = dform.getString("destinationFolderID");
        String workspace = new ScribeRequest(request).calculateCurrentWorkspace();
        Content contentToMove;
        ActionMessages msgs = new ActionMessages();
        ActionMessages errors = new ActionMessages();
        int numberMovedContents = 0;
        try {
            destinationFolder = (DirectoryFolder) directoryPersister.getItemByUUIDWorkspace(destinationFolderID, workspace);
        } catch (Exception e) {
            log.error("Error retrieving destination folder with ID: " + destinationFolderID, e);
            errors.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("content_admin.moveToFolder.error.unableToRetriveFolderToMoveTo"));
            saveErrors(request, errors);
            return mapping.findForward("moveToFolder");
        }
        while (contentIdsListIterator.hasNext()) {
            String contentId = contentIdsListIterator.next();
            try {
                contentToMove = contentManager.getContentById(contentId, workspace);
            } catch (Exception e) {
                log.error("Error retrieving content with ID: " + contentId, e);
                errors.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("content_admin.moveToFolder.error.unableToRetriveContent", contentId));
                saveErrors(request, errors);
                continue;
            }
            try {
                contentManager.moveContentToFolder(contentToMove, destinationFolder);
                numberMovedContents++;
            } catch (MalformedContentException e) {
                log.error("Malformed content [" + contentToMove.getName() + "]", e);
                errors.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("content_admin.moveToFolder.error.malformedContent", contentToMove.getName()));
                saveErrors(request, errors);
                continue;
            } catch (Throwable e) {
                String destinationFolderName = destinationFolder.getName();
                log.error("Error moving content [" + contentToMove.getName() + "] to folder [" + destinationFolderName + "]", e);
                if (destinationFolderName.equals("ContentFolder")) destinationFolderName = I18NUtils.getLocalizedMessage("file_directory.rootFolder");
                errors.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("content_admin.moveToFolder.error.unableToMoveContent", contentToMove.getName(), destinationFolderName));
                saveErrors(request, errors);
                continue;
            }
        }
        if (numberMovedContents > 0) {
            msgs.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("content_admin.moveToFolder.success", numberMovedContents));
            saveMessages(request, msgs);
        } else if (numberMovedContents == 0) {
            msgs.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("content_admin.moveToFolder.error.noContentMoved"));
            saveMessages(request, msgs);
        }
        return mapping.findForward("moveToFolder");
    }

    public ActionForward moveFolder(ActionMapping mapping, ActionForm form, PortletConfig portletConfig, PortletRequest request, PortletResponse response) {
        return null;
    }

    private Collection<Community> getTargetCommunities(PortletRequest request) {
        GroupwareUser groupwareUser = GroupwareHelper.getUser(request);
        Collection<Community> targetCommunities = UserCommunitiesService.getInstance().getUserCommunities(groupwareUser, request);
        return targetCommunities;
    }

    protected void saveMessages(PortletRequest request, ActionMessages messages) {
        ScribePortletActionHelper.saveMessages(request, messages);
    }

    protected void saveErrors(PortletRequest request, ActionMessages errors) {
        ScribePortletActionHelper.saveErrors(request, errors);
    }

    @Override
    protected Map<String, String> getKeyMethodMap() {
        Map<String, String> map = new HashMap<String, String>();
        map.put("button.delete", "delete");
        map.put("button.publish", "publish");
        map.put("button.filter", "filter");
        map.put("button.copy", "copy");
        map.put("button.sendTo", "send");
        map.put("button.sendToBag", "sendToBag");
        map.put("button.change.paging", "changePaging");
        map.put("button.moveToTrash", "moveToTrash");
        map.put("button.restore", "restore");
        map.put("removeAllListings", "removeAllListings");
        map.put("button.publish", "publish");
        map.put("button.unpublish", "unpublish");
        map.put("button.moveToFolder", "moveToFolder");
        map.put("button.severalContentsExportation", "severalContentsExportation");
        map.put("button.massiveMoveToTrash", "massiveMoveToTrash");
        map.put("button.emptyTrash", "emptyTrash");
        return map;
    }

    private void getPageSize(PortletRequest req) {
        String pageSize = ParamUtil.getString(req, "pagesize");
        if (StringUtils.isNotEmpty(pageSize)) {
            req.getPortletSession().setAttribute("pagesize", new Integer(pageSize));
        } else {
            if (req.getPortletSession().getAttribute("pagesize") == null) {
                req.getPortletSession().setAttribute("pagesize", new Integer(DEFAULT_PAGE_SIZE));
            }
        }
    }

    @Override
    public ActionForward render(ActionMapping mapping, ActionForm form, PortletConfig portletConfig, RenderRequest request, RenderResponse response) throws Exception {
        if (StringUtils.isNotEmpty(getForward(request))) return mapping.findForward(getForward(request)); else return super.render(mapping, form, portletConfig, request, response);
    }

    public ActionForward severalContentsExportation(ActionMapping mapping, ActionForm form, PortletConfig portletConfig, PortletRequest request, PortletResponse response) throws CMSRuntimeException {
        DynaActionForm dform = (DynaActionForm) form;
        String[] listContentsIDs = (String[]) dform.get("listContentsIDs");
        String workspace = new ScribeRequest(request).calculateCurrentWorkspace();
        ContentManager contentManager = ManagerRegistry.getContentManager();
        ActionMessages msgs = new ActionMessages();
        ActionMessages errors = new ActionMessages();
        if ((listContentsIDs.length < 1)) {
            errors.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("content_admin.none.selected"));
            saveErrors(request, errors);
            return mapping.findForward("list");
        }
        try {
            File zipOutFilename = new File(EXPORTED_CONTENTS_PATH, CONTENTS_ZIP_PREFIX + Long.toString(System.currentTimeMillis()) + ZIP_FILE_EXTENSION);
            ZipOutputStream out = new ZipOutputStream(new FileOutputStream(zipOutFilename));
            for (String contentId : listContentsIDs) {
                try {
                    log.info("Exporting content:" + contentId);
                    out.putNextEntry(new ZipEntry(contentId));
                    PortletContextImpl portletContextImpl = ((PortletContextImpl) portletConfig.getPortletContext());
                    ServletContext servletContext = portletContextImpl.getServletContext();
                    byte[] contentByteArray = contentManager.exportContentDataByContentId(1, contentId, workspace, servletContext);
                    out.write(contentByteArray);
                } catch (RepositoryException e1) {
                    log.error("Error accesing repository");
                    errors.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("content_admin.export.unexpected"));
                    saveErrors(request, errors);
                    return mapping.findForward("list");
                } catch (IOException e) {
                    log.error("Error managing file operations");
                    errors.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("content_admin.export.unexpected"));
                    saveErrors(request, errors);
                    return mapping.findForward("list");
                }
            }
            out.close();
        } catch (IOException e) {
            log.error("Error managing file operations");
            errors.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("content_admin.export.unexpected"));
            saveErrors(request, errors);
            return mapping.findForward("list");
        }
        if (listContentsIDs.length > 0) {
            msgs.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("content_admin.export.success", String.valueOf(listContentsIDs.length)));
        }
        saveMessages(request, msgs);
        saveErrors(request, errors);
        return mapping.findForward("list");
    }
}
