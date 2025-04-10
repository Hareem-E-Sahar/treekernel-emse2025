package org.argouml.persistence;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.Writer;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import org.apache.log4j.Logger;
import org.argouml.application.api.Argo;
import org.argouml.application.helpers.ApplicationVersion;
import org.argouml.configuration.Configuration;
import org.argouml.kernel.Project;
import org.argouml.kernel.ProjectMember;
import org.argouml.model.Facade;
import org.argouml.model.Model;
import org.argouml.model.UmlException;
import org.argouml.model.XmiException;
import org.argouml.model.XmiReader;
import org.argouml.model.XmiWriter;
import org.argouml.uml.ProjectMemberModel;
import org.argouml.uml.diagram.ArgoDiagram;
import org.argouml.uml.diagram.DiagramFactory;
import org.argouml.uml.diagram.DiagramFactory.DiagramType;
import org.xml.sax.InputSource;

/**
 * The file persister for the UML model.
 * @author Bob Tarling
 */
class ModelMemberFilePersister extends MemberFilePersister implements XmiExtensionParser {

    /**
     * Logger.
     */
    private static final Logger LOG = Logger.getLogger(ModelMemberFilePersister.class);

    /**
     * Loads a model (XMI only) from a URL. BE ADVISED this
     * method has a side effect. It sets _UUIDREFS to the model.<p>
     *
     * If there is a problem with the xmi file, an error is set in the
     * getLastLoadStatus() field. This needs to be examined by the
     * calling function.<p>
     *
     * @see org.argouml.persistence.MemberFilePersister#load(org.argouml.kernel.Project,
     * java.io.InputStream)
     */
    public void load(Project project, URL url) throws OpenException {
        load(project, new InputSource(url.toExternalForm()));
    }

    /**
     * Loads a model (XMI only) from an input stream. BE ADVISED this
     * method has a side effect. It sets _UUIDREFS to the model.<p>
     *
     * If there is a problem with the xmi file, an error is set in the
     * getLastLoadStatus() field. This needs to be examined by the
     * calling function.<p>
     *
     * @see org.argouml.persistence.MemberFilePersister#load(org.argouml.kernel.Project,
     * java.io.InputStream)
     */
    public void load(Project project, InputStream inputStream) throws OpenException {
        load(project, new InputSource(inputStream));
    }

    private void load(Project project, InputSource source) throws OpenException {
        Object mmodel = null;
        try {
            source.setEncoding(Argo.getEncoding());
            readModels(source);
            mmodel = getCurModel();
        } catch (OpenException e) {
            LOG.error("UmlException caught", e);
            throw e;
        }
        Model.getUmlHelper().addListenersToModel(mmodel);
        project.addMember(mmodel);
        project.setUUIDRefs(new HashMap<String, Object>(getUUIDRefs()));
    }

    public String getMainTag() {
        try {
            return Model.getXmiReader().getTagName();
        } catch (UmlException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Save the project model to XMI.
     *
     * {@inheritDoc}
     */
    @Deprecated
    @Override
    @SuppressWarnings("deprecation")
    public void save(ProjectMember member, Writer w, boolean xmlFragment) throws SaveException {
        if (w == null) {
            throw new IllegalArgumentException("No Writer specified!");
        }
        try {
            ProjectMemberModel pmm = (ProjectMemberModel) member;
            Object model = pmm.getModel();
            if (xmlFragment) {
                File tempFile = File.createTempFile("xmi", null);
                tempFile.deleteOnExit();
                OutputStream stream = new FileOutputStream(tempFile);
                XmiWriter xmiWriter = Model.getXmiWriter(model, stream, ApplicationVersion.getVersion() + "(" + UmlFilePersister.PERSISTENCE_VERSION + ")");
                xmiWriter.write();
                addXmlFileToWriter((PrintWriter) w, tempFile);
            } else {
                XmiWriter xmiWriter = Model.getXmiWriter(model, w, ApplicationVersion.getVersion() + "(" + UmlFilePersister.PERSISTENCE_VERSION + ")");
                xmiWriter.write();
            }
        } catch (IOException e) {
            throw new SaveException(e);
        } catch (UmlException e) {
            throw new SaveException(e);
        }
    }

    /**
     * Save the project model to XMI.
     * 
     * @see org.argouml.persistence.MemberFilePersister#save(ProjectMember, OutputStream)
     */
    public void save(ProjectMember member, OutputStream outStream) throws SaveException {
        ProjectMemberModel pmm = (ProjectMemberModel) member;
        Object model = pmm.getModel();
        try {
            XmiWriter xmiWriter = Model.getXmiWriter(model, outStream, ApplicationVersion.getVersion() + "(" + UmlFilePersister.PERSISTENCE_VERSION + ")");
            xmiWriter.write();
            outStream.flush();
        } catch (UmlException e) {
            throw new SaveException(e);
        } catch (IOException e) {
            throw new SaveException(e);
        }
    }

    public void parse(String label, String xmiExtensionString) {
        LOG.info("Parsing an extension for " + label);
    }

    private Object curModel;

    private HashMap<String, Object> uUIDRefs;

    private Collection elementsRead;

    /**
     * @return the current model
     */
    public Object getCurModel() {
        return curModel;
    }

    /**
     * Return XMI id to object map for the most recently read XMI file.
     * 
     * @return the UUID
     */
    public HashMap<String, Object> getUUIDRefs() {
        return uUIDRefs;
    }

    /**
     * Read an XMI file from the given URL.
     *
     * @param url the URL
     * @param xmiExtensionParser the XmiExtensionParser
     * @throws OpenException when there is an IO error
     */
    public synchronized void readModels(URL url, XmiExtensionParser xmiExtensionParser) throws OpenException {
        LOG.info("=======================================");
        LOG.info("== READING MODEL " + url);
        try {
            InputSource source = new InputSource(new XmiInputStream(url.openStream(), xmiExtensionParser, 100000, null));
            source.setSystemId(url.toString());
            readModels(source);
        } catch (IOException ex) {
            throw new OpenException(ex);
        }
    }

    /**
     * Read a XMI file from the given inputsource.
     * 
     * @param source The InputSource. The systemId of the input source should be
     *                set so that it can be used to resolve external references.
     * @throws OpenException If an error occur while reading the source
     */
    public synchronized void readModels(InputSource source) throws OpenException {
        XmiReader reader = null;
        try {
            reader = Model.getXmiReader();
            if (Configuration.getBoolean(Argo.KEY_XMI_STRIP_DIAGRAMS, false)) {
                reader.setIgnoredElements(new String[] { "UML:Diagram" });
            } else {
                reader.setIgnoredElements(null);
            }
            List<String> searchPath = reader.getSearchPath();
            String pathList = System.getProperty("org.argouml.model.modules_search_path");
            if (pathList != null) {
                String[] paths = pathList.split(",");
                for (String path : paths) {
                    if (!searchPath.contains(path)) {
                        reader.addSearchPath(path);
                    }
                }
            }
            reader.addSearchPath(source.getSystemId());
            curModel = null;
            elementsRead = reader.parse(source, false);
            if (elementsRead != null && !elementsRead.isEmpty()) {
                Facade facade = Model.getFacade();
                Object current;
                Iterator elements = elementsRead.iterator();
                while (elements.hasNext()) {
                    current = elements.next();
                    if (facade.isAModel(current)) {
                        LOG.info("Loaded model '" + facade.getName(current) + "'");
                        if (curModel == null) {
                            curModel = current;
                        }
                    }
                }
            }
            uUIDRefs = new HashMap<String, Object>(reader.getXMIUUIDToObjectMap());
        } catch (XmiException ex) {
            throw new XmiFormatException(ex);
        } catch (UmlException ex) {
            throw new XmiFormatException(ex);
        }
        LOG.info("=======================================");
    }

    /**
     * Create and register diagrams for activity and statemachines in the
     * model(s) of the project. If no other diagrams are created, a default
     * Class Diagram will be created. ArgoUML currently requires at least one
     * diagram for proper operation. 
     * 
     * TODO: Move to XmiFilePersister (protected)
     * 
     * @param project
     *            The project
     */
    public void registerDiagrams(Project project) {
        registerDiagramsInternal(project, elementsRead, true);
    }

    /**
     * Internal method create diagrams for activity graphs and state machines.
     * It exists soley to contain common functionality from the two public
     * methods.  It can be merged into its caller when the deprecated version
     * of the public method goes away.
     * 
     * @param project
     *            The project
     * @param elements
     *            Collection of top level model elements to process
     * @param atLeastOne
     *            If true, forces at least one diagram to be created.
     */
    private void registerDiagramsInternal(Project project, Collection elements, boolean atLeastOne) {
        Facade facade = Model.getFacade();
        Collection diagramsElement = new ArrayList();
        Iterator it = elements.iterator();
        while (it.hasNext()) {
            Object element = it.next();
            if (facade.isAModel(element)) {
                diagramsElement.addAll(Model.getModelManagementHelper().getAllModelElementsOfKind(element, Model.getMetaTypes().getStateMachine()));
            } else if (facade.isAStateMachine(element)) {
                diagramsElement.add(element);
            }
        }
        DiagramFactory diagramFactory = DiagramFactory.getInstance();
        it = diagramsElement.iterator();
        while (it.hasNext()) {
            Object statemachine = it.next();
            Object namespace = facade.getNamespace(statemachine);
            if (namespace == null) {
                namespace = facade.getContext(statemachine);
                Model.getCoreHelper().setNamespace(statemachine, namespace);
            }
            ArgoDiagram diagram = null;
            if (facade.isAActivityGraph(statemachine)) {
                LOG.info("Creating activity diagram for " + facade.getUMLClassName(statemachine) + "<<" + facade.getName(statemachine) + ">>");
                diagram = diagramFactory.createDiagram(DiagramType.Activity, namespace, statemachine);
            } else {
                LOG.info("Creating state diagram for " + facade.getUMLClassName(statemachine) + "<<" + facade.getName(statemachine) + ">>");
                diagram = diagramFactory.createDiagram(DiagramType.State, namespace, statemachine);
            }
            if (diagram != null) {
                project.addMember(diagram);
            }
        }
        if (atLeastOne && project.getDiagramCount() < 1) {
            ArgoDiagram d = diagramFactory.createDiagram(DiagramType.Class, curModel, null);
            project.addMember(d);
        }
        if (project.getDiagramCount() >= 1 && project.getActiveDiagram() == null) {
            project.setActiveDiagram(project.getDiagramList().get(0));
        }
    }

    /**
     * @return Returns the elementsRead.
     */
    public Collection getElementsRead() {
        return elementsRead;
    }

    /**
     * @param elements The elementsRead to set.
     */
    public void setElementsRead(Collection elements) {
        this.elementsRead = elements;
    }
}
