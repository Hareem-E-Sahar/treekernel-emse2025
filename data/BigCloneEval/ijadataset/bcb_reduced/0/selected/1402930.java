package uk.ac.ebi.intact.application.search2.struts.view;

import org.apache.log4j.Logger;
import uk.ac.ebi.intact.application.search2.business.Constants;
import uk.ac.ebi.intact.application.search2.struts.view.details.*;
import uk.ac.ebi.intact.application.search2.struts.view.single.ExperimentSingleViewBean;
import uk.ac.ebi.intact.application.search2.struts.view.single.InteractionSingleViewBean;
import uk.ac.ebi.intact.application.search2.struts.view.single.ProteinSingleViewBean;
import uk.ac.ebi.intact.application.search2.struts.view.single.SingleViewBean;
import uk.ac.ebi.intact.application.search2.struts.view.single.chunked.ExperimentChunkedSingleViewBean;
import uk.ac.ebi.intact.model.*;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class ViewBeanFactory {

    private static final transient Logger logger = Logger.getLogger(Constants.LOGGER_NAME);

    private static ViewBeanFactory ourInstance;

    /**
     * Mapping related to the detailed view
     */
    private static Map ourBeanToDetailsView = new HashMap();

    /**
     * Mapping related to the single object view
     */
    private static Map ourBeanToSingleItemView = new HashMap();

    /**
     * Maps: Model class -> binary view bean
     */
    private static Map ourBeanToBinaryView = new HashMap();

    /**
     * Maps: Model class -> chunked view bean
     */
    private static Map ourBeanToChunkedView = new HashMap();

    static {
        ourBeanToDetailsView.put(Experiment.class, ExperimentDetailsViewBean.class);
        ourBeanToDetailsView.put(InteractionImpl.class, InteractionDetailsViewBean.class);
        ourBeanToDetailsView.put(ProteinImpl.class, ProteinDetailsViewBean.class);
        ourBeanToSingleItemView.put(Experiment.class, ExperimentSingleViewBean.class);
        ourBeanToSingleItemView.put(InteractionImpl.class, InteractionSingleViewBean.class);
        ourBeanToSingleItemView.put(ProteinImpl.class, ProteinSingleViewBean.class);
        ourBeanToSingleItemView.put(CvDatabase.class, SingleViewBean.class);
        ourBeanToSingleItemView.put(CvXrefQualifier.class, SingleViewBean.class);
        ourBeanToSingleItemView.put(CvTopic.class, SingleViewBean.class);
        ourBeanToSingleItemView.put(CvInteraction.class, SingleViewBean.class);
        ourBeanToSingleItemView.put(CvInteractionType.class, SingleViewBean.class);
        ourBeanToSingleItemView.put(CvComponentRole.class, SingleViewBean.class);
        ourBeanToSingleItemView.put(CvIdentification.class, SingleViewBean.class);
        ourBeanToSingleItemView.put(BioSource.class, SingleViewBean.class);
        ourBeanToBinaryView.put(ProteinImpl.class, BinaryDetailsViewBean.class);
        ourBeanToChunkedView.put(Experiment.class, ExperimentChunkedSingleViewBean.class);
    }

    private ViewBeanFactory() {
    }

    /**
     * Returns the only instance of this class.
     * @return the only instance of this class; always non null value is returned.
     */
    public static synchronized ViewBeanFactory getInstance() {
        if (ourInstance == null) {
            ourInstance = new ViewBeanFactory();
        }
        return ourInstance;
    }

    /**
     * Returns the appropriate view bean for given <code>Collection<code> object.
     * @param objects the <code>Collection</code> of objects to return the view for.
     * @param link the link to help page.
     * @return the appropriate view for <code>object</code>; null is
     * returned if there is no mapping or an error in creating an
     * instance of the view.
     */
    public AbstractViewBean getBinaryViewBean(Collection objects, String link, String contextPath) {
        Object firstItem = objects.iterator().next();
        Class objsClass = firstItem.getClass();
        logger.info(objsClass);
        Class clazz = (Class) ourBeanToBinaryView.get(objsClass);
        return getViewBean(clazz, objects, link, contextPath);
    }

    /**
     * Returns the appropriate view bean for given <code>Collection<code> object.
     * @param objects the <code>Collection</code> of objects to return the view for.
     * Note that for Experiment detail views some Experiments may require a tabbed view
     * and so a different bean will be returned (ie ExperimentDetailsViewBean).
     * @param link the link to help page.
     * @param contextPath
     * @return the appropriate view for <code>object</code>; null is
     * returned if there is no mapping, an error in creating an
     * instance of the view or if the object collection is empty or null.
     */
    public AbstractViewBean getDetailsViewBean(Collection objects, String link, String contextPath) {
        if ((objects.isEmpty()) || (objects == null)) {
            logger.info("ViewBeanFactory: detail view requested for null/empty Collection!");
            return null;
        }
        Object firstItem = objects.iterator().next();
        Class objsClass = firstItem.getClass();
        logger.info(objsClass);
        Class clazz = (Class) ourBeanToDetailsView.get(objsClass);
        return getViewBean(clazz, objects, link, contextPath);
    }

    /**
     * Returns the appropriate view bean for given basic object.
     *
     * @param object the <code>AnnotatedObject</code> to return the view for.
     * @param link the link to help page.
     * @return the appropriate view for <code>object</code>; null is
     * returned if there is no mapping or an error in creating an
     * instance of the view.
     */
    public AbstractViewBean getSingleViewBean(AnnotatedObject object, String link, String contextPath) {
        if (object == null) {
            logger.info("ViewBeanFactory: single view requested for null object!");
            return null;
        }
        logger.info(object.getClass());
        Class beanClass = (Class) ourBeanToSingleItemView.get(object.getClass());
        return getViewBean(beanClass, object, link, contextPath);
    }

    /**
     * Builds a tabbed view for a single result. NB this could be refactored
     * by using a User object as a parameter instead, to avoid needing a special
     * method.
     * @param object
     * @param link
     * @param contextPath
     * @return
     */
    public AbstractViewBean getChunkedSingleViewBean(AnnotatedObject object, String link, String contextPath, int maxChunk, int selectedChunk) {
        if (object == null) {
            logger.info("ViewBeanFactory: chunk view requested for null object!");
            return null;
        }
        logger.info(object.getClass());
        Class beanClass = (Class) ourBeanToChunkedView.get(object.getClass());
        return getViewBean(beanClass, object, link, contextPath, maxChunk, selectedChunk);
    }

    /**
     * Returns the appropriate view bean for given object.
     * The object can be either a <code>Collection</code> or an
     * <code>AnnotatedObject</code>. NB This chould be refactored using a
     * User object to avoid the need for a seperate special method for a single
     * tabbed view.
     *
     * @param beanClazz the type of the bean which will wrap the object to display
     * @param objectToWrap the object to display
     * @param link the link to help page.
     * @param contextPath the context path of the appliction
     * @param maxChunk the count of displayable chunk for that bean
     * @param selectedChunk the chunk we are going to display (0 <= selectedChunk < maxChunk)

     * @return the appropriate view for <code>object</code>; null is
     * returned if there is no mapping or an error in creating an
     * instance of the view.
     *
     * @return
     */
    private AbstractViewBean getViewBean(Class beanClazz, Object objectToWrap, String link, String contextPath, int maxChunk, int selectedChunk) {
        if (beanClazz == null) {
            return null;
        }
        if (objectToWrap == null) {
            logger.info("ViewBeanFactory: null object to be tab viewed! ViewBean Class " + beanClazz);
            return null;
        }
        try {
            Class classToWrap = null;
            if (objectToWrap instanceof AnnotatedObject) {
                classToWrap = AnnotatedObject.class;
            } else {
                classToWrap = Collection.class;
            }
            logger.info("ClassToWrap affected to: " + classToWrap);
            logger.info("Ask constructor to: " + beanClazz.getName());
            logger.info("Param1: " + Class.class.getName() + " value: " + objectToWrap.getClass().getName());
            logger.info("Param2: " + String.class.getName() + " value: " + link);
            logger.info("Param3: " + String.class.getName() + " value: " + contextPath);
            logger.info("Param4: " + Integer.class.getName() + " value: " + maxChunk);
            logger.info("Param5: " + Integer.class.getName() + " value: " + selectedChunk);
            Constructor constructor = beanClazz.getConstructor(new Class[] { classToWrap, String.class, String.class, Integer.class, Integer.class });
            return (AbstractViewBean) constructor.newInstance(new Object[] { objectToWrap, link, contextPath, new Integer(maxChunk), new Integer(selectedChunk) });
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Returns the appropriate view bean for given object.
     * The object can be either a <code>Collection</code> or an
     * <code>AnnotatedObject</code>.
     *
     * @param beanClazz the type of the bean which will wrap the object to display
     * @param objectToWrap the object to display
     * @param link the link to help page.
     * @param contextPath the context path for the help page
     * @return the appropriate view for <code>object</code>; null is
     * returned if there is no mapping or an error in creating an
     * instance of the view.
     */
    private AbstractViewBean getViewBean(Class beanClazz, Object objectToWrap, String link, String contextPath) {
        if (beanClazz == null) {
            return null;
        }
        if (objectToWrap == null) {
            logger.info("ViewBeanFactory: view requested for null object! ViewBean Class " + beanClazz);
            return null;
        }
        try {
            Class classToWrap = null;
            if (objectToWrap instanceof AnnotatedObject) {
                classToWrap = AnnotatedObject.class;
            } else {
                classToWrap = Collection.class;
            }
            logger.info("ClassToWrap affected to: " + classToWrap);
            logger.info("Ask constructor to: " + beanClazz.getName());
            logger.info("Param1: " + classToWrap.getName() + " value: " + objectToWrap);
            logger.info("Param2: " + String.class.getName() + " value: " + link);
            logger.info("Param3: " + String.class.getName() + " value: " + contextPath);
            Constructor constructor = beanClazz.getConstructor(new Class[] { classToWrap, String.class, String.class });
            return (AbstractViewBean) constructor.newInstance(new Object[] { objectToWrap, link, contextPath });
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
        return null;
    }
}
