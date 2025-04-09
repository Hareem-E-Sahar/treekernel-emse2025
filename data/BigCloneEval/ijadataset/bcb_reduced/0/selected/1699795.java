package au.gov.nla.aons.rest.handler;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.exolab.castor.xml.Marshaller;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.web.bind.WebDataBinder;
import au.gov.nla.aons.constants.RestDomainObjectNames;
import au.gov.nla.aons.constants.RestMethodTypes;
import au.gov.nla.aons.repository.RepositoryManager;
import au.gov.nla.aons.repository.domain.Repository;
import au.gov.nla.aons.rest.exceptions.ParameterInvalidException;
import au.gov.nla.aons.rest.handler.util.MarshallerFactory;
import au.gov.nla.aons.schedule.domain.Schedule;

public class RepositoryDomainHandler implements DomainHandler {

    Map<String, String> typeClassNameMap = new HashMap<String, String>();

    private RepositoryManager repositoryManager;

    private HandlerUtil handlerUtil;

    private MarshallerFactory marshallerFactory;

    public void handleDelete(HttpServletRequest request) {
        Long repositoryId = handlerUtil.retrieveParameterAsLong(request, "id", true);
        repositoryManager.deleteRepository(repositoryId);
    }

    public void renderPlural(List objects, HttpServletResponse response) throws Exception {
        Marshaller marshaller = marshallerFactory.retrieveMarshallerForWriter("repositories", response.getWriter(), "classpath:au/gov/nla/aons/rest/handler/util/RepositoryShortCastorMapping.xml");
        marshaller.marshal(objects);
    }

    public void renderSingle(Object object, HttpServletResponse response) throws Exception {
        Marshaller marshaller = marshallerFactory.retrieveMarshallerForWriter("repository", response.getWriter(), "classpath:au/gov/nla/aons/rest/handler/util/RepositoryCastorMapping.xml");
        marshaller.marshal(object);
    }

    public Object handleGet(HttpServletRequest request) {
        Long repositoryId = handlerUtil.retrieveParameterAsLong(request, "id", true);
        return repositoryManager.retrieveRepository(repositoryId);
    }

    public List handleGetPlural(HttpServletRequest request) {
        return repositoryManager.retrieveAllRepositories();
    }

    public void handlePost(HttpServletRequest request) {
        Long repositoryId = handlerUtil.retrieveParameterAsLong(request, "id", true);
        Repository repository = repositoryManager.retrieveRepository(repositoryId);
        createScheduleAsRequired(request, repository);
        WebDataBinder binder = new WebDataBinder(repository, "repository");
        binder.bind(new MutablePropertyValues(request.getParameterMap()));
        repositoryManager.updateRepository(repository);
    }

    private void createScheduleAsRequired(HttpServletRequest request, Repository repository) {
        Enumeration parameterNames = request.getParameterNames();
        while (parameterNames.hasMoreElements()) {
            String parameterName = (String) parameterNames.nextElement();
            if (parameterName.startsWith("schedule")) {
                repository.setSchedule(new Schedule());
                break;
            }
        }
    }

    public Long handlePut(HttpServletRequest request) {
        String repositoryType = handlerUtil.retrieveParameter(request, "type", true);
        Repository repository = createTypeSpecificRepository(repositoryType);
        createScheduleAsRequired(request, repository);
        WebDataBinder binder = new WebDataBinder(repository, "repository");
        binder.bind(new MutablePropertyValues(request.getParameterMap()));
        return repositoryManager.createRepository(repository);
    }

    public String retrieveDomainObjectName() {
        return RestDomainObjectNames.REPOSITORY;
    }

    public Repository createTypeSpecificRepository(String type) {
        String className = typeClassNameMap.get(type);
        if (className == null) {
            String typeList = "";
            Iterator<String> typeIter = typeClassNameMap.keySet().iterator();
            while (typeIter.hasNext()) {
                String availableType = (String) typeIter.next();
                typeList += availableType;
                if (typeIter.hasNext()) {
                    typeList += ", ";
                }
            }
            throw new ParameterInvalidException("type", type, "Invalid type [" + type + "], must be one of [" + typeList + "]");
        }
        try {
            Class clazz = Class.forName(className);
            Constructor<Repository> constructor = clazz.getConstructor();
            return constructor.newInstance();
        } catch (Throwable t) {
            throw new RuntimeException("Could not create registry of class [" + className + "]: " + t.getLocalizedMessage());
        }
    }

    public List<String> retrieveHandledMethods() {
        List<String> handledMethods = new ArrayList<String>();
        handledMethods.add(RestMethodTypes.DELETE);
        handledMethods.add(RestMethodTypes.GET);
        handledMethods.add(RestMethodTypes.POST);
        handledMethods.add(RestMethodTypes.PUT);
        return handledMethods;
    }

    public String retrievePluralDomainObjectName() {
        return RestDomainObjectNames.REPOSITORY_PLURAL;
    }

    public HandlerUtil getHandlerUtil() {
        return handlerUtil;
    }

    public void setHandlerUtil(HandlerUtil handlerUtil) {
        this.handlerUtil = handlerUtil;
    }

    public RepositoryManager getRepositoryManager() {
        return repositoryManager;
    }

    public void setRepositoryManager(RepositoryManager repositoryManager) {
        this.repositoryManager = repositoryManager;
    }

    public MarshallerFactory getMarshallerFactory() {
        return marshallerFactory;
    }

    public void setMarshallerFactory(MarshallerFactory marshallerFactory) {
        this.marshallerFactory = marshallerFactory;
    }

    public Map<String, String> getTypeClassNameMap() {
        return typeClassNameMap;
    }

    public void setTypeClassNameMap(Map<String, String> typeClassNameMap) {
        this.typeClassNameMap = typeClassNameMap;
    }
}
