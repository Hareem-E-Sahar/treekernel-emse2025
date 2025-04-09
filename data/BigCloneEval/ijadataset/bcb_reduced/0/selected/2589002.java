package rbsla.mediator;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import java.util.Properties;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;
import org.apache.log4j.Logger;
import rbsla.mediator.exceptions.CorruptedProjectFileException;

/**
 * A project consists of 0 to n ressources. Ressources should be added or
 * removed using the methods defined in this class. A ressource is an
 * Observable; the project is registered as an Observer. Whenever a ressource is
 * changed the project gets notified and notifies any Observer that is
 * registered to the project in turn (i.e. the Mediator in the RBSLA
 * environment).
 * 
 * The project itself is stateless by design, though. It does not know if its
 * ressources have been changed or not. In the RBSLA environment, the Mediator
 * takes care of this behavior.
 * 
 * It is possible to save and to load a project. Saving and loading operates on
 * streams. A project will be saved into a ZIP file which contains one property
 * entry and one entry per ressource. The property entry holds information about
 * the project type, the ressource count, and the ressource types as it is
 * crucial to have this information in order to instantiate according classes
 * using Java reflection.
 * 
 * The project does not care about how its ressources are saved. Each ressource
 * has to implement a save method which will be invoked.
 * 
 * A project does not have to have a specific type, it can be generic (thus just
 * instantiating this class). Nevertheless, several types are implemented as
 * subclasses for the RBSLA environment to make handling easier.
 * 
 * @version 1.01
 * - New project type (RBSLA) added
 */
public class Project extends Observable implements Observer {

    private static Logger logger = Logger.getLogger(Project.class);

    /**
	 * Project type that contains a description, a list of views, and a prova
	 * file.
	 */
    public static final int PROJECT_TYPE_PROVA = 1;

    /**
	 * Generic, not specified project type.
	 */
    public static final int PROJECT_TYPE_UNDEFINED = 0;

    /**
	 * Project type that contains a description, a list of views, and a XML
	 * file.
	 */
    public static final int PROJECT_TYPE_XML = 2;

    /**
	 * Project type that contains all ressources needed for the joined
	 * environment of dashboard and editor.
	 */
    public static final int PROJECT_TYPE_RBSLA = 3;

    /**
	 * Key used in property file for referring to the project type.
	 */
    private static final String PROPERTY_KEY_PROJECT_TYPE = "project_type";

    /**
	 * Key used in property file for referring to the ressource count.
	 */
    private static final String PROPERTY_KEY_RESSOURCE_COUNT = "ressource_count";

    /**
	 * Last part of key used in property file for referring to the ressource
	 * type - ressource number stands inbetween.
	 */
    private static final String PROPERTY_KEY_RESSOURCE_TYPE_POSTFIX = "_type";

    /**
	 * First part of key used in property file for referring to the ressource
	 * type - ressource number stands inbetween.
	 */
    private static final String PROPERTY_KEY_RESSOURCE_TYPE_PREFIX = "ressource";

    /**
	 * Loads a project using the specified input stream. The project file has to
	 * be a ZIP file containing a property entry and n ressource entries.
	 * 
	 * @param in
	 *            Stream to read project file from
	 * @return Loaded project
	 * @throws IOException
	 *             In case project can not be loaded
	 */
    protected static Project load(InputStream in) throws IOException {
        logger.debug("Loading project...");
        ZipInputStream zipIn = new ZipInputStream(new BufferedInputStream(in));
        ZipEntry entry = zipIn.getNextEntry();
        Project project;
        if (entry != null) {
            Properties props = new Properties();
            props.load(zipIn);
            logger.debug("Properties loaded");
            String projectType = (String) props.get(PROPERTY_KEY_PROJECT_TYPE);
            logger.debug("Project class is: " + projectType);
            try {
                project = (Project) Class.forName(projectType).getConstructors()[0].newInstance(new Object[0]);
                project.ressources.clear();
                logger.debug("Loading ressources...");
                for (int i = 0; i < Integer.parseInt((String) props.get(PROPERTY_KEY_RESSOURCE_COUNT)); i++) {
                    entry = zipIn.getNextEntry();
                    if (entry != null) {
                        String ressourceType = (String) props.get(PROPERTY_KEY_RESSOURCE_TYPE_PREFIX + (i) + PROPERTY_KEY_RESSOURCE_TYPE_POSTFIX);
                        logger.debug("Ressource " + i + " class is: " + ressourceType);
                        Ressource r = (Ressource) Class.forName(ressourceType).getConstructors()[0].newInstance(new Object[0]);
                        r.load(zipIn);
                        project.addRessource(r);
                    } else {
                        throw new CorruptedProjectFileException("Unable to extract ressource " + i);
                    }
                }
                logger.debug("Ressources loaded");
            } catch (ClassNotFoundException e) {
                throw new CorruptedProjectFileException(e.getMessage());
            } catch (IllegalArgumentException e) {
                throw new CorruptedProjectFileException(e.getMessage());
            } catch (SecurityException e) {
                throw new CorruptedProjectFileException(e.getMessage());
            } catch (InstantiationException e) {
                throw new CorruptedProjectFileException(e.getMessage());
            } catch (IllegalAccessException e) {
                throw new CorruptedProjectFileException(e.getMessage());
            } catch (InvocationTargetException e) {
                throw new CorruptedProjectFileException(e.getMessage());
            }
        } else {
            throw new CorruptedProjectFileException("Unable to extract number of ressources");
        }
        zipIn.close();
        logger.debug("Project loaded");
        return project;
    }

    private List<Ressource> ressources;

    protected Project() {
        ressources = new ArrayList<Ressource>();
    }

    /**
	 * Adds a resource at a specific position in the list. Used by subclasses of
	 * project.
	 * 
	 * @param i
	 *            Position to store ressource in ressource list
	 * @param r
	 *            Ressource to add
	 */
    protected void addRessource(int i, Ressource r) {
        r.addObserver(this);
        ressources.add(i, r);
        logger.debug("Ressource " + r + " added at position " + i);
    }

    public void addRessource(Ressource r) {
        r.addObserver(this);
        ressources.add(r);
        logger.debug("Ressource " + r + " added");
    }

    public List<Ressource> getRessources() {
        return ressources;
    }

    public int getType() {
        return PROJECT_TYPE_UNDEFINED;
    }

    /**
	 * Instead of notifying a ressource about changes directly, the project can
	 * be notified as well (for example if several changes have been made).
	 * 
	 */
    public void notifyAboutContentChange() {
        logger.debug("Content has been changed");
        logger.debug("Sending notification...");
        setChanged();
        notifyObservers();
    }

    public boolean removeRessource(Ressource r) {
        r.deleteObserver(this);
        logger.debug("Removing ressource: " + r);
        return ressources.remove(r);
    }

    /**
	 * Saves project as a ZIP file using the specified output stream.
	 * 
	 * @param out
	 *            Output stream to save project to
	 * @throws IOException
	 *             If project cannot be saved.
	 */
    protected void save(OutputStream out) throws IOException {
        logger.debug("Saving project...");
        Properties props = new Properties();
        props.put(PROPERTY_KEY_PROJECT_TYPE, this.getClass().getCanonicalName());
        props.put(PROPERTY_KEY_RESSOURCE_COUNT, "" + ressources.size());
        int i = 0;
        for (Ressource r : ressources) {
            props.put(PROPERTY_KEY_RESSOURCE_TYPE_PREFIX + (i++) + PROPERTY_KEY_RESSOURCE_TYPE_POSTFIX, r.getClass().getCanonicalName());
        }
        ZipOutputStream zipOut = new ZipOutputStream(new BufferedOutputStream(out));
        ZipEntry entry = new ZipEntry("project.properties");
        zipOut.putNextEntry(entry);
        props.store(zipOut, null);
        logger.debug("Properties saved");
        logger.debug("Saving " + ressources.size() + " ressources...");
        i = 0;
        for (Ressource r : ressources) {
            entry = new ZipEntry("project.ressource" + (i++) + ".data");
            zipOut.putNextEntry(entry);
            r.save(zipOut);
        }
        logger.debug("Ressources saved");
        zipOut.close();
        logger.debug("Project saved");
    }

    public void update(Observable obs, Object obj) {
        logger.debug("Notification received from " + obs);
        logger.debug("Forwarding notification...");
        setChanged();
        notifyObservers();
    }
}
