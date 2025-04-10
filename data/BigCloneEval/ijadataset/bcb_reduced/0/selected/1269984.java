package org.redmine.ta;

import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.message.BasicNameValuePair;
import org.redmine.ta.beans.*;
import org.redmine.ta.internal.*;
import org.redmine.ta.internal.logging.Logger;
import org.redmine.ta.internal.logging.LoggerFactory;
import javax.naming.ldap.InitialLdapContext;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URL;
import java.util.*;
import java.util.Map.Entry;

/**
 * <b>Entry point</b> for the API: use this class to communicate with Redmine servers.
 *
 * @author Alexey Skorokhodov
 */
public class RedmineManager {

    private static final String CONTENT_TYPE = "text/xml; charset=utf-8";

    private static final int DEFAULT_OBJECTS_PER_PAGE = 25;

    public static enum INCLUDE {

        journals, relations, attachments
    }

    private static enum MODE {

        REDMINE_1_0, REDMINE_1_1_OR_CHILIPROJECT_1_2
    }

    private Logger logger = LoggerFactory.getLogger(RedmineManager.class);

    private String host;

    private String apiAccessKey;

    private String login;

    private String password;

    private boolean useBasicAuth = false;

    private int objectsPerPage = DEFAULT_OBJECTS_PER_PAGE;

    private MODE currentMode = MODE.REDMINE_1_1_OR_CHILIPROJECT_1_2;

    public RedmineManager(String uri) {
        if (uri == null || uri.isEmpty()) {
            throw new IllegalArgumentException("The host parameter is NULL or empty");
        }
        this.host = uri;
        this.useBasicAuth = true;
    }

    /**
     * Creates an instance of RedmineManager class. Host and apiAccessKey are not checked at this moment.
     *
     * @param host         complete Redmine server web URI, including protocol and port number. Example: http://demo.redmine.org:8080
     * @param apiAccessKey Redmine API access key. It is shown on "My Account" / "API access key" webpage
     *                     (check  <i>http://redmine_server_url/my/account<i> URL).
     *                     This parameter is <b>optional</b> (can be set to NULL) for Redmine projects, which are "public".
     */
    public RedmineManager(String host, String apiAccessKey) {
        this(host);
        this.apiAccessKey = apiAccessKey;
        this.useBasicAuth = false;
    }

    /**
     * @deprecated Use RedmineManager(String uri) constructor and then setLogin() , setPassword()
     */
    public RedmineManager(String uri, String login, String password) {
        this(uri);
        this.login = login;
        this.password = password;
        this.useBasicAuth = true;
    }

    /**
     * Sample usage:
     * <p/>
     * <p/>
     * <pre>
     * {@code
     *   Issue issueToCreate = new Issue();
     *   issueToCreate.setSubject("This is the summary line 123");
     *   Issue newIssue = mgr.createIssue(PROJECT_KEY, issueToCreate);
     * }
     *
     * @param projectKey The project "identifier". This is a string key like "project-ABC", NOT a database numeric ID.
     * @param issue      the Issue object to create on the server.
     * @return the newly created Issue.
     * @throws IOException
     * @throws AuthenticationException invalid or no API access key is used with the server, which
     *                                 requires authorization. Check the constructor arguments.
     * @throws NotFoundException       the project with the given projectKey is not found
     * @throws RedmineException
     */
    public Issue createIssue(String projectKey, Issue issue) throws IOException, AuthenticationException, NotFoundException, RedmineException {
        URI uri = getURIConfigurator().createURI("issues.xml");
        HttpPost http = new HttpPost(uri);
        String xmlBody = RedmineXMLGenerator.toXML(projectKey, issue);
        setEntity(http, xmlBody);
        String response = getCommunicator().sendRequest(http);
        return RedmineXMLParser.parseObjectFromXML(Issue.class, response);
    }

    /**
     * @deprecated this method will be deleted in the future releases. use update() method instead
     *
     * Note: This method cannot return the updated Issue from Redmine
     * because the server does not provide any XML in response.
     *
     * @param issue the Issue to update on the server. issue.getId() is used for identification.
     * @throws IOException
     * @throws AuthenticationException invalid or no API access key is used with the server, which
     *                                 requires authorization. Check the constructor arguments.
     * @throws NotFoundException       the issue with the required ID is not found
     * @throws RedmineException
     */
    public void updateIssue(Issue issue) throws IOException, AuthenticationException, NotFoundException, RedmineException {
        URI uri = getURIConfigurator().getUpdateURI(issue.getClass(), Integer.toString(issue.getId()));
        HttpPut httpRequest = new HttpPut(uri);
        String NO_PROJECT_KEY = null;
        String xmlBody = RedmineXMLGenerator.toXML(NO_PROJECT_KEY, issue);
        setEntity(httpRequest, xmlBody);
        getCommunicator().sendRequest(httpRequest);
    }

    private void setEntity(HttpEntityEnclosingRequest request, String xmlBody) throws UnsupportedEncodingException {
        StringEntity entity = new StringEntity(xmlBody, Communicator.CHARSET);
        entity.setContentType(CONTENT_TYPE);
        request.setEntity(entity);
    }

    /**
     * Load the list of projects available to the user, which is represented by the API access key.
     *
     * @return list of Project objects
     * @throws AuthenticationException invalid or no API access key is used with the server, which
     *                                 requires authorization. Check the constructor arguments.
     * @throws RedmineException
     * @throws NotFoundException
     */
    public List<Project> getProjects() throws IOException, AuthenticationException, RedmineException {
        Set<NameValuePair> params = new HashSet<NameValuePair>();
        params.add(new BasicNameValuePair("include", "trackers"));
        try {
            return getObjectsList(Project.class, params);
        } catch (NotFoundException e) {
            throw new RuntimeException("NotFoundException received, which should never happen in this request");
        }
    }

    /**
     * There could be several issues with the same summary, so the method returns List.
     *
     * @param summaryField
     * @return empty list if not issues with this summary field exist, never NULL
     * @throws AuthenticationException invalid or no API access key is used with the server, which
     *                                 requires authorization. Check the constructor arguments.
     * @throws NotFoundException
     * @throws RedmineException
     */
    public List<Issue> getIssuesBySummary(String projectKey, String summaryField) throws IOException, AuthenticationException, NotFoundException, RedmineException {
        Set<NameValuePair> params = new HashSet<NameValuePair>();
        params.add(new BasicNameValuePair("subject", summaryField));
        if ((projectKey != null) && (projectKey.length() > 0)) {
            params.add(new BasicNameValuePair("project_id", projectKey));
        }
        return getObjectsList(Issue.class, params);
    }

    /**
     * Generic method to search for issues.
     *
     * @param pParameters the http parameters key/value pairs to append to the rest api request
     * @return empty list if not issues with this summary field exist, never NULL
     * @throws AuthenticationException invalid or no API access key is used with the server, which
     *                                 requires authorization. Check the constructor arguments.
     * @throws NotFoundException
     * @throws RedmineException
     */
    public List<Issue> getIssues(Map<String, String> pParameters) throws IOException, AuthenticationException, NotFoundException, RedmineException {
        Set<NameValuePair> params = new HashSet<NameValuePair>();
        for (final Entry<String, String> param : pParameters.entrySet()) {
            params.add(new BasicNameValuePair(param.getKey(), param.getValue()));
        }
        return getObjectsList(Issue.class, params);
    }

    /**
     * @param id      the Redmine issue ID
     * @param include list of "includes". e.g. "relations", "journals", ...
     * @return Issue object
     * @throws IOException
     * @throws AuthenticationException invalid or no API access key is used with the server, which
     *                                 requires authorization. Check the constructor arguments.
     * @throws NotFoundException       the issue with the given id is not found on the server
     * @throws RedmineException
     */
    public Issue getIssueById(Integer id, INCLUDE... include) throws IOException, AuthenticationException, NotFoundException, RedmineException {
        String value = join(",", include);
        return getObject(Issue.class, id, new BasicNameValuePair("include", value));
    }

    private static String join(String delimToUse, INCLUDE... include) {
        String delim = "";
        StringBuilder sb = new StringBuilder();
        for (INCLUDE i : include) {
            sb.append(delim).append(i);
            delim = delimToUse;
        }
        return sb.toString();
    }

    /**
     * @param projectKey string key like "project-ABC", NOT a database numeric ID
     * @return Redmine's project
     * @throws AuthenticationException invalid or no API access key is used with the server, which
     *                                 requires authorization. Check the constructor arguments.
     * @throws NotFoundException       the project with the given key is not found
     * @throws RedmineException
     */
    public Project getProjectByKey(String projectKey) throws IOException, AuthenticationException, NotFoundException, RedmineException {
        URI uri = getURIConfigurator().getUpdateURI(Project.class, projectKey, new BasicNameValuePair("include", "trackers"));
        HttpGet http = new HttpGet(uri);
        String response = getCommunicator().sendRequest(http);
        return RedmineXMLParser.parseProjectFromXML(response);
    }

    /**
     * @param projectKey string key like "project-ABC", NOT a database numeric ID
     * @throws AuthenticationException invalid or no API access key is used with the server, which
     *                                 requires authorization. Check the constructor arguments.
     * @throws NotFoundException       if the project with the given key is not found
     * @throws RedmineException
     */
    public void deleteProject(String projectKey) throws IOException, AuthenticationException, NotFoundException, RedmineException {
        deleteObject(Project.class, projectKey);
    }

    public void deleteIssue(Integer id) throws IOException, AuthenticationException, NotFoundException, RedmineException {
        deleteObject(Issue.class, Integer.toString(id));
    }

    /**
     * @param projectKey ignored if NULL
     * @param queryId    id of the saved query in Redmine. the query must be accessible to the user
     *                   represented by the API access key (if the Redmine project requires authorization).
     *                   This parameter is <b>optional<b>, NULL can be provided to get all available issues.
     * @return list of Issue objects
     * @throws IOException
     * @throws AuthenticationException invalid or no API access key is used with the server, which
     *                                 requires authorization. Check the constructor arguments.
     * @throws RedmineException
     * @see Issue
     */
    public List<Issue> getIssues(String projectKey, Integer queryId, INCLUDE... include) throws IOException, AuthenticationException, NotFoundException, RedmineException {
        Set<NameValuePair> params = new HashSet<NameValuePair>();
        if (queryId != null) {
            params.add(new BasicNameValuePair("query_id", String.valueOf(queryId)));
        }
        if ((projectKey != null) && (projectKey.length() > 0)) {
            params.add(new BasicNameValuePair("project_id", projectKey));
        }
        String includeStr = join(",", include);
        params.add(new BasicNameValuePair("include", includeStr));
        return getObjectsList(Issue.class, params);
    }

    /**
     * Redmine 1.0 - specific version
     *
     * @return objects list, never NULL
     */
    private <T> List<T> getObjectsListV104(Class<T> objectClass, Set<NameValuePair> params) throws IOException, AuthenticationException, NotFoundException, RedmineException {
        List<T> objects = new ArrayList<T>();
        final int FIRST_REDMINE_PAGE = 1;
        int pageNum = FIRST_REDMINE_PAGE;
        String firstPage = null;
        params.add(new BasicNameValuePair("per_page", String.valueOf(objectsPerPage)));
        do {
            List<NameValuePair> paramsList = new ArrayList<NameValuePair>(params);
            paramsList.add(new BasicNameValuePair("page", String.valueOf(pageNum)));
            URI uri = getURIConfigurator().getRetrieveObjectsListURI(objectClass, paramsList);
            HttpGet http = new HttpGet(uri);
            String response = getCommunicator().sendRequest(http);
            if (pageNum == FIRST_REDMINE_PAGE) {
                firstPage = response;
            } else {
                if (firstPage.equals(response)) {
                    break;
                }
            }
            List<T> foundItems = RedmineXMLParser.parseObjectsFromXML(objectClass, response);
            if (foundItems.size() == 0) {
                break;
            }
            objects.addAll(foundItems);
            pageNum++;
        } while (true);
        return objects;
    }

    /**
     * @return objects list, never NULL
     */
    private <T> List<T> getObjectsList(Class<T> objectClass, Set<NameValuePair> params) throws IOException, AuthenticationException, NotFoundException, RedmineException {
        if (currentMode.equals(MODE.REDMINE_1_1_OR_CHILIPROJECT_1_2)) {
            return getObjectsListV11(objectClass, params);
        } else if (currentMode.equals(MODE.REDMINE_1_0)) {
            return getObjectsListV104(objectClass, params);
        } else {
            throw new RuntimeException("unsupported mode:" + currentMode + ". supported modes are: " + MODE.REDMINE_1_0 + " and " + MODE.REDMINE_1_1_OR_CHILIPROJECT_1_2);
        }
    }

    /**
     * Redmine 1.1+ / Chiliproject 1.2 - specific version
     *
     * @return objects list, never NULL
     */
    private <T> List<T> getObjectsListV11(Class<T> objectClass, Set<NameValuePair> params) throws IOException, AuthenticationException, NotFoundException, RedmineException {
        List<T> objects = new ArrayList<T>();
        params.add(new BasicNameValuePair("limit", String.valueOf(objectsPerPage)));
        int offset = 0;
        int totalObjectsFoundOnServer;
        do {
            List<NameValuePair> paramsList = new ArrayList<NameValuePair>(params);
            paramsList.add(new BasicNameValuePair("offset", String.valueOf(offset)));
            URI uri = getURIConfigurator().getRetrieveObjectsListURI(objectClass, paramsList);
            logger.debug(uri.toString());
            HttpGet http = new HttpGet(uri);
            String response = getCommunicator().sendRequest(http);
            totalObjectsFoundOnServer = RedmineXMLParser.parseObjectsTotalCount(response);
            List<T> foundItems = RedmineXMLParser.parseObjectsFromXML(objectClass, response);
            if (foundItems.size() == 0) {
                break;
            }
            objects.addAll(foundItems);
            offset += foundItems.size();
        } while (offset < totalObjectsFoundOnServer);
        return objects;
    }

    private <T> T getObject(Class<T> objectClass, Integer id, NameValuePair... params) throws IOException, AuthenticationException, NotFoundException, RedmineException {
        URI uri = getURIConfigurator().getRetrieveObjectURI(objectClass, id, new ArrayList<NameValuePair>(Arrays.asList(params)));
        String body = getCommunicator().sendGet(uri);
        return RedmineXMLParser.parseObjectFromXML(objectClass, body);
    }

    private <T> T createObject(Class<T> classs, T obj) throws IOException, AuthenticationException, NotFoundException, RedmineException {
        URI uri = getURIConfigurator().getCreateURI(obj.getClass());
        return createObject(classs, obj, uri);
    }

    private <T> T createObject(Class<T> classs, T obj, URI uri) throws IOException, AuthenticationException, NotFoundException, RedmineException {
        HttpPost http = new HttpPost(uri);
        String xml = RedmineXMLGenerator.toXML(obj);
        setEntity(http, xml);
        String response = getCommunicator().sendRequest(http);
        return RedmineXMLParser.parseObjectFromXML(classs, response);
    }

    public void update(Identifiable obj) throws IOException, AuthenticationException, NotFoundException, RedmineException {
        validate(obj);
        URI uri = getURIConfigurator().getUpdateURI(obj.getClass(), Integer.toString(obj.getId()));
        HttpPut http = new HttpPut(uri);
        String xml = RedmineXMLGenerator.toXML(obj);
        setEntity(http, xml);
        getCommunicator().sendRequest(http);
    }

    private void validate(Identifiable obj) {
        if (obj instanceof TimeEntry && !(isValidTimeEntry((TimeEntry) obj))) {
            throw createIllegalTimeEntryException();
        }
    }

    private <T extends Identifiable> void deleteObject(Class<T> classs, String id) throws IOException, AuthenticationException, NotFoundException, RedmineException {
        URI uri = getURIConfigurator().getUpdateURI(classs, id);
        HttpDelete http = new HttpDelete(uri);
        getCommunicator().sendRequest(http);
    }

    /**
     * Sample usage:
     * <p/>
     * <p/>
     * <pre>
     * {@code
     * 	Project project = new Project();
     * 	Long timeStamp = Calendar.getInstance().getTimeInMillis();
     * 	String key = "projkey" + timeStamp;
     * 	String name = &quot;project number &quot; + timeStamp;
     * 	String description = &quot;some description for the project&quot;;
     * 	project.setIdentifier(key);
     * 	project.setName(name);
     * 	project.setDescription(description);
     *
     * 	Project createdProject = mgr.createProject(project);
     * }
     * </pre>
     *
     * @param project project to create on the server
     * @return the newly created Project object.
     * @throws IOException
     * @throws AuthenticationException invalid or no API access key is used with the server, which
     *                                 requires authorization. Check the constructor arguments.
     * @throws RedmineException
     */
    public Project createProject(Project project) throws IOException, AuthenticationException, RedmineException, NotFoundException {
        URI uri = getURIConfigurator().createURI("projects.xml", new BasicNameValuePair("include", "trackers"));
        HttpPost httpPost = new HttpPost(uri);
        String createProjectXML = RedmineXMLGenerator.toXML(project);
        setEntity(httpPost, createProjectXML);
        String response = getCommunicator().sendRequest(httpPost);
        return RedmineXMLParser.parseProjectFromXML(response);
    }

    /**
     * @deprecated this method will be deleted in the future releases. use update() method instead
     *
     * @param project
     * @throws IOException
     * @throws AuthenticationException invalid or no API access key is used with the server, which
     *                                 requires authorization. Check the constructor arguments.
     * @throws RedmineException
     * @throws NotFoundException
     *
     * @see #update(org.redmine.ta.beans.Identifiable)
     */
    public void updateProject(Project project) throws IOException, AuthenticationException, RedmineException, NotFoundException {
        update(project);
    }

    /**
     * This number of objects (tasks, projects, users) will be requested from Redmine server in 1 request.
     */
    public int getObjectsPerPage() {
        return objectsPerPage;
    }

    /**
     * This number of objects (tasks, projects, users) will be requested from Redmine server in 1 request.
     */
    public void setObjectsPerPage(int pageSize) {
        this.objectsPerPage = pageSize;
    }

    /**
     * Load the list of users on the server.
     * <p><b>This operation requires "Redmine Administrator" permission.</b>
     *
     * @return list of User objects
     * @throws AuthenticationException invalid or no API access key is used with the server, which
     *                                 requires authorization. Check the constructor arguments.
     * @throws NotFoundException
     * @throws RedmineException
     */
    public List<User> getUsers() throws IOException, AuthenticationException, NotFoundException, RedmineException {
        return getObjectsList(User.class, new HashSet<NameValuePair>());
    }

    public User getUserById(Integer userId) throws IOException, AuthenticationException, NotFoundException, RedmineException {
        return getObject(User.class, userId);
    }

    /**
     * @return the current user logged into Redmine
     */
    public User getCurrentUser() throws IOException, AuthenticationException, RedmineException, NotFoundException {
        URI uri = getURIConfigurator().createURI("users/current.xml");
        HttpGet http = new HttpGet(uri);
        String response = getCommunicator().sendRequest(http);
        return RedmineXMLParser.parseUserFromXML(response);
    }

    public User createUser(User user) throws IOException, AuthenticationException, RedmineException, NotFoundException {
        return createObject(User.class, user);
    }

    /**
     * @deprecated this method will be deleted in the future releases. use update() method instead
     *
     * This method cannot return the updated object from Redmine
     * because the server does not provide any XML in response.
     *
     * @param user
     * @throws IOException
     * @throws AuthenticationException invalid or no API access key is used with the server, which
     *                                 requires authorization. Check the constructor arguments.
     * @throws RedmineException
     * @throws NotFoundException       some object is not found. e.g. the user with the given id
     */
    public void updateUser(User user) throws IOException, AuthenticationException, RedmineException, NotFoundException {
        update(user);
    }

    /**
     * @param userId user identifier (numeric ID)
     * @throws AuthenticationException invalid or no API access key is used with the server, which
     *                                 requires authorization. Check the constructor arguments.
     * @throws NotFoundException       if the user with the given id is not found
     * @throws RedmineException
     */
    public void deleteUser(Integer userId) throws IOException, AuthenticationException, RedmineException, NotFoundException {
        deleteObject(User.class, Integer.toString(userId));
    }

    public List<TimeEntry> getTimeEntries() throws IOException, AuthenticationException, NotFoundException, RedmineException {
        return getObjectsList(TimeEntry.class, new HashSet<NameValuePair>());
    }

    /**
     * @param id the database Id of the TimeEntry record
     */
    public TimeEntry getTimeEntry(Integer id) throws IOException, AuthenticationException, NotFoundException, RedmineException {
        return getObject(TimeEntry.class, id);
    }

    public List<TimeEntry> getTimeEntriesForIssue(Integer issueId) throws IOException, AuthenticationException, NotFoundException, RedmineException {
        Set<NameValuePair> params = new HashSet<NameValuePair>();
        params.add(new BasicNameValuePair("issue_id", Integer.toString(issueId)));
        return getObjectsList(TimeEntry.class, params);
    }

    public TimeEntry createTimeEntry(TimeEntry obj) throws IOException, AuthenticationException, NotFoundException, RedmineException {
        validate(obj);
        return createObject(TimeEntry.class, obj);
    }

    /**
     * @deprecated this method will be deleted in the future releases. use update() method instead
     */
    public void updateTimeEntry(TimeEntry obj) throws IOException, AuthenticationException, NotFoundException, RedmineException {
        update(obj);
    }

    public void deleteTimeEntry(Integer id) throws IOException, AuthenticationException, NotFoundException, RedmineException {
        deleteObject(TimeEntry.class, Integer.toString(id));
    }

    private boolean isValidTimeEntry(TimeEntry obj) {
        return obj.getProjectId() != null || obj.getIssueId() != null;
    }

    private IllegalArgumentException createIllegalTimeEntryException() {
        return new IllegalArgumentException("You have to either define a Project or Issue ID for a Time Entry. " + "The given Time Entry object has neither defined.");
    }

    /**
     * Get "saved queries" for the given project available to the current user.
     * <p/>
     * <p>This REST API feature was added in Redmine 1.3.0. See http://www.redmine.org/issues/5737
     */
    public List<SavedQuery> getSavedQueries(String projectKey) throws IOException, AuthenticationException, NotFoundException, RedmineException {
        Set<NameValuePair> params = new HashSet<NameValuePair>();
        if ((projectKey != null) && (projectKey.length() > 0)) {
            params.add(new BasicNameValuePair("project_id", projectKey));
        }
        return getObjectsList(SavedQuery.class, params);
    }

    /**
     * Get all "saved queries" available to the current user.
     * <p/>
     * <p>This REST API feature was added in Redmine 1.3.0. See http://www.redmine.org/issues/5737
     */
    public List<SavedQuery> getSavedQueries() throws IOException, AuthenticationException, NotFoundException, RedmineException {
        return getObjectsList(SavedQuery.class, new HashSet<NameValuePair>());
    }

    public IssueRelation createRelation(Integer issueId, Integer issueToId, String type) throws IOException, AuthenticationException, NotFoundException, RedmineException {
        URI uri = getURIConfigurator().createURI("issues/" + issueId + "/relations.xml");
        HttpPost http = new HttpPost(uri);
        IssueRelation toCreate = new IssueRelation();
        toCreate.setIssueId(issueId);
        toCreate.setIssueToId(issueToId);
        toCreate.setType(type);
        String xml = RedmineXMLGenerator.toXML(toCreate);
        setEntity(http, xml);
        String response = getCommunicator().sendRequest(http);
        return RedmineXMLParser.parseRelationFromXML(response);
    }

    /**
     * Deletes the selected relation.
     *
     * @param id
     * @throws IOException
     * @throws AuthenticationException
     * @throws NotFoundException
     * @throws RedmineException
     */
    public void deleteRelation(Integer id) throws IOException, AuthenticationException, NotFoundException, RedmineException {
        deleteObject(IssueRelation.class, Integer.toString(id));
    }

    /**
     * Delete all issue's relations
     *
     * @param redmineIssue
     * @throws IOException
     * @throws AuthenticationException
     * @throws RedmineException
     * @throws NotFoundException
     */
    public void deleteIssueRelations(Issue redmineIssue) throws IOException, AuthenticationException, RedmineException, NotFoundException {
        for (IssueRelation relation : redmineIssue.getRelations()) {
            deleteRelation(relation.getId());
        }
    }

    /**
     * Delete all issue's relation.
     *
     * @param id
     * @throws IOException
     * @throws AuthenticationException
     * @throws RedmineException
     * @throws NotFoundException
     */
    public void deleteIssueRelationsByIssueId(Integer id) throws IOException, AuthenticationException, RedmineException, NotFoundException {
        Issue issue = getIssueById(id, INCLUDE.relations);
        deleteIssueRelations(issue);
    }

    /**
     * @deprecated use createRelation(Integer issueId, Integer issueToId, String type). "projectKey" parameter is not used anyway.
     *             this method will be deleted soon.
     */
    public IssueRelation createRelation(String projectKey, Integer issueId, Integer issueToId, String type) throws IOException, AuthenticationException, NotFoundException, RedmineException {
        return createRelation(issueId, issueToId, type);
    }

    /**
     * Delivers a list of existing {@link org.redmine.ta.beans.IssueStatus}es.
     *
     * @return a list of existing {@link org.redmine.ta.beans.IssueStatus}es.
     * @throws IOException             thrown in case something went wrong while performing I/O
     *                                 operations
     * @throws AuthenticationException thrown in case something went wrong while trying to login
     * @throws RedmineException        thrown in case something went wrong in Redmine
     * @throws NotFoundException       thrown in case an object can not be found
     */
    public List<IssueStatus> getStatuses() throws IOException, AuthenticationException, RedmineException, NotFoundException {
        return getObjectsList(IssueStatus.class, new HashSet<NameValuePair>());
    }

    /**
     * creates a new {@link Version} for the {@link Project} contained. <br/>
     * Pre-condition: the attribute {@link Project} for the {@link Version} must
     * not be null!
     *
     * @param version the {@link Version}. Must contain a {@link Project}.
     * @return the new {@link Version} created by Redmine
     * @throws IllegalArgumentException thrown in case the version does not contain a project.
     * @throws IOException              thrown in case something went wrong while performing I/O
     *                                  operations
     * @throws AuthenticationException  thrown in case something went wrong while trying to login
     * @throws RedmineException         thrown in case something went wrong in Redmine
     * @throws NotFoundException        thrown in case an object can not be found
     */
    public Version createVersion(Version version) throws IOException, AuthenticationException, RedmineException, IllegalArgumentException, NotFoundException {
        if (version.getProject() == null) {
            throw new IllegalArgumentException("Version must contain a project");
        }
        int projectID = version.getProject().getId();
        URI uri = getURIConfigurator().createURI("projects/" + projectID + "/versions.xml");
        HttpPost httpPost = new HttpPost(uri);
        String createVersionXML = RedmineXMLGenerator.toXML(version);
        setEntity(httpPost, createVersionXML);
        String response = getCommunicator().sendRequest(httpPost);
        logger.debug(response);
        return RedmineXMLParser.parseVersionFromXML(response);
    }

    /**
     * deletes a new {@link Version} from the {@link Project} contained. <br/>
     *
     * @param version the {@link Version}.
     * @return the new {@link Version} created by Redmine
     * @throws IOException             thrown in case something went wrong while performing I/O
     *                                 operations
     * @throws AuthenticationException thrown in case something went wrong while trying to login
     * @throws RedmineException        thrown in case something went wrong in Redmine
     * @throws NotFoundException       thrown in case an object can not be found
     */
    public void deleteVersion(Version version) throws IOException, AuthenticationException, RedmineException, NotFoundException {
        deleteObject(Version.class, Integer.toString(version.getId()));
    }

    /**
     * delivers a list of {@link Version}s of a {@link Project}
     *
     * @param projectID the ID of the {@link Project}
     * @return the list of {@link Version}s of the {@link Project}
     * @throws IOException             thrown in case something went wrong while performing I/O
     *                                 operations
     * @throws AuthenticationException thrown in case something went wrong while trying to login
     * @throws RedmineException        thrown in case something went wrong in Redmine
     * @throws NotFoundException       thrown in case an object can not be found
     */
    public List<Version> getVersions(int projectID) throws IOException, AuthenticationException, RedmineException, NotFoundException {
        URI uri = getURIConfigurator().createURI("projects/" + projectID + "/versions.xml", new BasicNameValuePair("include", "projects"));
        HttpGet http = new HttpGet(uri);
        String response = getCommunicator().sendRequest(http);
        return RedmineXMLParser.parseVersionsFromXML(response);
    }

    public Version getVersionById(int versionId) throws IOException, AuthenticationException, RedmineException, NotFoundException {
        URI uri = getURIConfigurator().createURI("versions/" + versionId + ".xml");
        HttpGet http = new HttpGet(uri);
        String response = getCommunicator().sendRequest(http);
        return RedmineXMLParser.parseVersionFromXML(response);
    }

    /**
     * delivers a list of {@link IssueCategory}s of a {@link Project}
     *
     * @param projectID the ID of the {@link Project}
     * @return the list of {@link IssueCategory}s of the {@link Project}
     * @throws IOException             thrown in case something went wrong while performing I/O
     *                                 operations
     * @throws AuthenticationException thrown in case something went wrong while trying to login
     * @throws RedmineException        thrown in case something went wrong in Redmine
     * @throws NotFoundException       thrown in case an object can not be found
     */
    public List<IssueCategory> getCategories(int projectID) throws IOException, AuthenticationException, RedmineException, NotFoundException {
        URI uri = getURIConfigurator().createURI("projects/" + projectID + "/issue_categories.xml");
        HttpGet http = new HttpGet(uri);
        String response = getCommunicator().sendRequest(http);
        return RedmineXMLParser.parseIssueCategoriesFromXML(response);
    }

    /**
     * creates a new {@link IssueCategory} for the {@link Project} contained. <br/>
     * Pre-condition: the attribute {@link Project} for the {@link IssueCategory} must
     * not be null!
     *
     * @param category the {@link IssueCategory}. Must contain a {@link Project}.
     * @return the new {@link IssueCategory} created by Redmine
     * @throws IllegalArgumentException thrown in case the category does not contain a project.
     * @throws IOException              thrown in case something went wrong while performing I/O
     *                                  operations
     * @throws AuthenticationException  thrown in case something went wrong while trying to login
     * @throws RedmineException         thrown in case something went wrong in Redmine
     * @throws NotFoundException        thrown in case an object can not be found
     */
    public IssueCategory createCategory(IssueCategory category) throws IOException, AuthenticationException, RedmineException, IllegalArgumentException, NotFoundException {
        if (category.getProject() == null) {
            throw new IllegalArgumentException("IssueCategory must contain a project");
        }
        URI uri = getURIConfigurator().getCreateURIIssueCategory(category.getProject().getId());
        return createObject(IssueCategory.class, category, uri);
    }

    /**
     * deletes an {@link IssueCategory}. <br/>
     *
     * @param category the {@link IssueCategory}.
     * @return the new {@link IssueCategory} created by Redmine
     * @throws IOException             thrown in case something went wrong while performing I/O
     *                                 operations
     * @throws AuthenticationException thrown in case something went wrong while trying to login
     * @throws RedmineException        thrown in case something went wrong in Redmine
     * @throws NotFoundException       thrown in case an object can not be found
     */
    public void deleteCategory(IssueCategory category) throws IOException, AuthenticationException, RedmineException, NotFoundException {
        deleteObject(IssueCategory.class, Integer.toString(category.getId()));
    }

    /**
     * @return a list of all {@link Tracker}s available
     * @throws IOException             thrown in case something went wrong while performing I/O
     *                                 operations
     * @throws AuthenticationException thrown in case something went wrong while trying to login
     * @throws RedmineException        thrown in case something went wrong in Redmine
     * @throws NotFoundException       thrown in case an object can not be found
     */
    public List<Tracker> getTrackers() throws IOException, AuthenticationException, RedmineException, NotFoundException {
        return getObjectsList(Tracker.class, new HashSet<NameValuePair>());
    }

    /**
     * Delivers an {@link org.redmine.ta.beans.Attachment} by its ID.
     *
     * @param attachmentID the ID
     * @return the {@link org.redmine.ta.beans.Attachment}
     * @throws IOException             thrown in case something went wrong while performing I/O
     *                                 operations
     * @throws AuthenticationException thrown in case something went wrong while trying to login
     * @throws RedmineException        thrown in case something went wrong in Redmine
     * @throws NotFoundException       thrown in case an object can not be found
     */
    public Attachment getAttachmentById(int attachmentID) throws IOException, AuthenticationException, NotFoundException, RedmineException {
        return getObject(Attachment.class, attachmentID);
    }

    /**
     * Downloads the content of an {@link org.redmine.ta.beans.Attachment} from the Redmine server.
     *
     * @param issueAttachment the {@link org.redmine.ta.beans.Attachment}
     * @return the content of the attachment as a byte[] array
     * @throws IOException thrown in case the download fails
     */
    public byte[] downloadAttachmentContent(Attachment issueAttachment) throws IOException {
        byte[] result = null;
        URL url = new URL(issueAttachment.getContentURL());
        BufferedReader inputReader = null;
        try {
            inputReader = new BufferedReader(new InputStreamReader(url.openStream()));
            StringBuilder contentBuilder = new StringBuilder();
            String line;
            while ((line = inputReader.readLine()) != null) {
                contentBuilder.append(line);
            }
            result = contentBuilder.toString().getBytes();
        } finally {
            if (inputReader != null) {
                inputReader.close();
            }
        }
        return result;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * @param projectKey ignored if NULL
     * @return list of news objects
     * @throws IOException
     * @throws AuthenticationException invalid or no API access key is used with the server, which
     *                                 requires authorization. Check the constructor arguments.
     * @throws RedmineException
     * @see News
     */
    public List<News> getNews(String projectKey) throws IOException, AuthenticationException, NotFoundException, RedmineException {
        Set<NameValuePair> params = new HashSet<NameValuePair>();
        if ((projectKey != null) && (projectKey.length() > 0)) {
            params.add(new BasicNameValuePair("project_id", projectKey));
        }
        return getObjectsList(News.class, params);
    }

    private URIConfigurator getURIConfigurator() {
        URIConfigurator uriConfigurator = new URIConfigurator(host);
        if (!useBasicAuth) {
            uriConfigurator.setApiAccessKey(apiAccessKey);
        }
        return uriConfigurator;
    }

    private Communicator getCommunicator() {
        Communicator communicator = new Communicator();
        if (useBasicAuth) {
            communicator.setCredentials(login, password);
        }
        return communicator;
    }
}
