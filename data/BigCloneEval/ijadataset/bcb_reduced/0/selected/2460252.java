package sg.edu.nus.comp.simTL.engine.interpreter.evaluators;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.log4j.Logger;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EAnnotation;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.Resource.Factory;
import sg.edu.nus.comp.simTL.engine.IModel;
import sg.edu.nus.comp.simTL.engine.IModel.ModelType;
import sg.edu.nus.comp.simTL.engine.exceptions.InjectionException;
import sg.edu.nus.comp.simTL.engine.exceptions.InterpreterException;
import sg.edu.nus.comp.simTL.engine.exceptions.SimTLException;
import sg.edu.nus.comp.simTL.engine.exceptions.ValidationException;
import sg.edu.nus.comp.simTL.engine.interpreter.SimTLModel;

/**
 * @author Marcel B�hme
 * Comment created on: 29-Jun-2010
 */
public class TTemplate extends TElement {

    private static Logger log = Logger.getLogger(TTemplate.class);

    private static final Logger interpreterLog = Logger.getLogger("TemplateInterpreter");

    public static final String CLASS_TEMPLATE = "Template";

    public static final String ANNOTATION_TEMPLATE_CLASS = "template_class";

    public static final String ANNOTATION_OBJECT_LANGUAGE = "objectLanguage";

    public static final String ANNOTATION_OBJECT_LANGUAGE_FACTORY = "objectLanguageFactory";

    public static final String ANNOTATION_OBJECT_LANGUAGE_EXTENSION = "objectLanguageExtension";

    public static final String REFERENCE_class = "class";

    public static final String REFERENCE_templateHeader = "templateHeader";

    public static final String REFERENCE_modelImports = "modelImports";

    public static final String ATTRIBUTE_MODELIMPORT_URI = "uri";

    public static final String ATTRIBUTE_MODELIMPORT_NAME = "name";

    private EPackage objectLanguagePackage;

    private Resource.Factory objectLanguageTextRF;

    private String objectLanguageExtension;

    private Map<String, IModel> name2inputModelMap;

    /**
	 * @param tElement
	 * @throws SimTLException
	 */
    public TTemplate(EObject tElement) throws SimTLException {
        super(tElement);
    }

    @Override
    protected void load() throws SimTLException {
        name2inputModelMap = new HashMap<String, IModel>();
        EObject templateHeader = loadReferencedEObject(REFERENCE_templateHeader);
        if (templateHeader == null) {
            throw new InterpreterException("No TemplateHeader object in template");
        }
        loadModelImports(loadReferencedList(templateHeader, REFERENCE_modelImports));
        objectLanguagePackage = resolveObjectLanguagePackage();
        objectLanguageTextRF = resolveTextResourceFactory();
        objectLanguageExtension = resolveObjectLanguageExtension();
    }

    private EClass findTemplateClass(EClass c0) {
        if (c0 != null && c0.getName().compareToIgnoreCase(CLASS_TEMPLATE) == 0) return c0;
        for (EClass c1 : c0.getESuperTypes()) {
            EClass c = findTemplateClass(c1);
            if (c != null) return c;
        }
        return null;
    }

    private EAnnotation findTemplateAnnotation() throws SimTLException {
        EClass templateClass = findTemplateClass(getTElement().eClass());
        if (templateClass == null) throw new InjectionException("There is no " + CLASS_TEMPLATE + " super class?!");
        EAnnotation ann = templateClass.getEAnnotation(ANNOTATION_TEMPLATE_CLASS);
        if (ann == null) {
            throw new InjectionException("No \"" + ANNOTATION_TEMPLATE_CLASS + "\" annotation on XFrame Root");
        }
        return ann;
    }

    private void loadModelImports(List<EObject> modelImports) throws SimTLException {
        if (modelImports == null) {
            log.debug("No modelImports?");
        } else {
            for (EObject modelImport : modelImports) {
                String resourceUriS = loadAttribute(modelImport, ATTRIBUTE_MODELIMPORT_URI);
                String name = loadAttribute(modelImport, ATTRIBUTE_MODELIMPORT_NAME);
                ResourceSet resourceSet = getTElement().eResource().getResourceSet();
                URI resourceUri = null;
                try {
                    resourceUri = URI.createURI(resourceUriS);
                } catch (Exception e) {
                    interpreterLog.warn("Couldn't load resource for model " + name + " from: " + resourceUriS, e);
                    return;
                }
                resourceUri = resolveURI(resourceUri);
                if (resourceUri != null && !resourceUriS.matches(resourceUri.toString())) {
                    log.debug("Resolved " + resourceUriS + " to " + resourceUri.toString());
                }
                Resource inputModelR = resourceSet.createResource(resourceUri);
                if (inputModelR == null) {
                    interpreterLog.warn("Couldn't load resource for model " + name + " from: " + resourceUriS);
                    return;
                }
                if (name2inputModelMap.get(name) != null) interpreterLog.warn("There is already a model: " + name + ". Override it.");
                name2inputModelMap.put(name, new SimTLModel(ModelType.INPUT_MODEL, name, inputModelR));
                log.debug("Importing " + resourceUriS + " as " + name);
            }
        }
    }

    protected URI resolveURI(URI uri) {
        if (uri.isFile()) {
            String relativeS = uri.toFileString();
            if (relativeS.startsWith("\\\\")) {
                relativeS = relativeS.substring(2);
            }
            File absolutFile = new File(relativeS);
            return URI.createFileURI(absolutFile.getAbsolutePath());
        }
        return uri;
    }

    protected EPackage resolveObjectLanguagePackage() throws SimTLException {
        String olS = findTemplateAnnotation().getDetails().get(ANNOTATION_OBJECT_LANGUAGE);
        if (olS == null) {
            throw new InjectionException("No \"" + ANNOTATION_OBJECT_LANGUAGE + "\" annotation details at annotation " + ANNOTATION_TEMPLATE_CLASS);
        }
        URI objectLanguageURI = null;
        try {
            objectLanguageURI = URI.createURI(olS);
        } catch (Exception e) {
            throw new ValidationException(ANNOTATION_OBJECT_LANGUAGE + " must be an URI!");
        }
        EPackage oLPackage = (EPackage) EPackage.Registry.INSTANCE.get(objectLanguageURI.toString());
        if (oLPackage == null) {
            throw new ValidationException("Object Language " + objectLanguageURI + " hasn't been registered at EPackage-registry!");
        }
        return oLPackage;
    }

    protected Resource.Factory resolveTextResourceFactory() throws SimTLException {
        String olS = findTemplateAnnotation().getDetails().get(ANNOTATION_OBJECT_LANGUAGE_FACTORY);
        if (olS == null) {
            throw new InjectionException("No \"" + ANNOTATION_OBJECT_LANGUAGE_FACTORY + "\" annotation details at annotation " + ANNOTATION_TEMPLATE_CLASS);
        }
        Class<?> factoryClass = null;
        try {
            factoryClass = Class.forName(olS);
        } catch (ClassNotFoundException e) {
            throw new ValidationException("No such class found:" + olS + ". Please make this language available", e);
        }
        Object factoryO = null;
        try {
            factoryO = factoryClass.getConstructor().newInstance();
        } catch (Exception e) {
            throw new InjectionException("Couldn't instantiate with empty constructor. Please make this language available", e);
        }
        if (!(factoryO instanceof Resource.Factory)) {
            throw new InjectionException("Referenced class is no ResourceFactory: " + olS);
        }
        return (Resource.Factory) factoryO;
    }

    protected String resolveObjectLanguageExtension() throws SimTLException {
        String olS = findTemplateAnnotation().getDetails().get(ANNOTATION_OBJECT_LANGUAGE_EXTENSION);
        if (olS == null) {
            throw new InjectionException("No \"" + ANNOTATION_OBJECT_LANGUAGE_EXTENSION + "\" annotation details at annotation " + ANNOTATION_TEMPLATE_CLASS);
        }
        return olS;
    }

    public IModel getInputModel(String inputModelName) {
        return name2inputModelMap.get(inputModelName);
    }

    public List<IModel> getInputModels() {
        List<IModel> models = new ArrayList<IModel>();
        models.addAll(name2inputModelMap.values());
        return Collections.unmodifiableList(models);
    }

    public final EObject getChild() throws SimTLException {
        return loadReferencedEObject(REFERENCE_class);
    }

    public String getObjectLanguageExtension() {
        return objectLanguageExtension;
    }

    public EPackage getObjectLanguagePackage() {
        return objectLanguagePackage;
    }

    public Factory getObjectLanguageTextResourceFactory() {
        return objectLanguageTextRF;
    }
}
