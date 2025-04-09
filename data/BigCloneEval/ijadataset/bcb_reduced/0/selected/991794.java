package org.hironico.dbtool2.querymanager;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.apache.log4j.Logger;
import org.hironico.dbtool2.config.DbToolConfiguration;

/**
 * Cette classe modélise le gestionnaire de requétes sauvegardées.
 * Disponible partout dans la JVM (singleton) il permet d'ajouter et de retirer
 * des requétes SQL depuis le SQL Editor. 
 * @author $Author: hironico $
 * @version $Rev: 1.4 $
 * @since 0.0.5
 */
public class QueryManager {

    private static final Logger logger = Logger.getLogger("org.hironico.dbtool2.querymanager");

    /**
     * Instance unique du query manager.
     */
    private static final QueryManager instance = new QueryManager();

    /**
     * Liste des listeners à l'écoute des operations de ce query manager.
     * @ince 2.0.0
     */
    private static final List<QueryManagerListener> listeners = Collections.synchronizedList(new ArrayList<QueryManagerListener>());

    /**
     * Liste des événnements en attente d'envoi
     * @since 2.0.0
     */
    private static final List<QueryManagerEvent> pendingEvents = Collections.synchronizedList(new ArrayList<QueryManagerEvent>());

    /**
     * Racine du répertoire de sauvegardes.
     */
    private SQLQueryDirectory sqlQueries = new SQLQueryDirectory();

    /**
     * Chemin vers le répertoire des queries sauvegardées.
     * @since 0.0.5
     */
    private String savedQueriesDirectory = null;

    /**
     * Constructeur protégé pour assuré l'unicité de l'instance.
     * Il va chercher le répertoire des queries sauvées dans la config.
     * @since 0.0.5
     */
    protected QueryManager() {
        savedQueriesDirectory = DbToolConfiguration.getInstance().getQueryManagerConfig().getSavedQueryDirectory();
        if ((savedQueriesDirectory == null) || ("".equals(savedQueriesDirectory))) {
            logger.warn("No saved queries dir is defined !");
        } else {
            if (!savedQueriesDirectory.endsWith(File.separator)) {
                savedQueriesDirectory += File.separator;
            }
        }
        eventThread.start();
    }

    /**
     * Permet de récupérer l'instance unique de ce Query Manager.
     * @return le seul Query manager de toute la JVM.
     * @since 0.0.5
     */
    public static QueryManager getInstance() {
        return instance;
    }

    public boolean addSqlQuery(SQLQuery query) {
        if (query == null) {
            return false;
        }
        if (!query.save()) {
            logger.error("Cannot save query : " + query.getAbsolutePath());
            return false;
        }
        sqlQueries.addLast(query);
        fireQueryAddedEvent(query);
        return true;
    }

    /**
     * Permet d'effacer tous les fichiers contenus dans le répertoire passé en paramètre.
     * Si le fichier donné n'est pas un répertoire alors cette méthode se contente d'effacer 
     * uniquement le fichier en question. Cette méthode efface récursivement tous les fichiers
     * MAIS ne permet pas de rafraichir le cache qu'il faut recharger.
     * @param file le fichier à effacer.
     * @since 2.0.0
     */
    protected boolean removeAllFiles(File file) {
        if (file == null) {
            logger.error("Cannot delete a null query file !");
            return false;
        }
        String fileName = file.getAbsolutePath();
        String savedDirFileName = DbToolConfiguration.getInstance().getQueryManagerConfig().getSavedQueryDirectory();
        if (fileName.equals(savedDirFileName)) {
            logger.error("Cannot delete the saved queries directory ! " + savedDirFileName);
            return false;
        }
        if (fileName.indexOf(savedDirFileName) < 0) {
            logger.error("The file you're attempting to delete is not under the root directory of saved queries. " + fileName);
            return false;
        }
        boolean result = true;
        if (file.isDirectory()) {
            for (File myFile : file.listFiles()) {
                result = removeAllFiles(myFile);
            }
        }
        result = file.delete();
        return result;
    }

    /**
     * Permet de retirer une SQLQuery de la liste des queries gérées par le
     * query manager.
     * @param query la query à supprimer.
     * @return true si la suppression est effective et false sinon.
     * @since 2.0.0
     */
    public boolean removeSqlQueryOrDirectory(File queryOrDirectory) {
        removeAllFiles(queryOrDirectory);
        loadSqlQueriesWithEvent(false);
        fireQueryRemovedEvent(queryOrDirectory);
        return true;
    }

    /**
     * Permet de charger toutes les queries sauvées sur disque par l'utilisateur
     * dans le répertoire donné par la préférence SavedQueriesDirectory.
     * @see loadSqlQueries(File fromDirectory)
     * @since 0.0.5
     */
    public void loadSqlQueries() {
        loadSqlQueriesWithEvent(true);
    }

    /**
     * Permet de charger les queries SQL depuis les fichiers sur le disque
     * mais avec la possibilité de spécifier qu'on ne veut pas envoyer de
     * notification de chargement.
     * @param sendEvent doit etre mis a true pour evoyer un event QUERY_LOADED.
     * @since 2.0.0
     */
    public void loadSqlQueriesWithEvent(boolean sendEvent) {
        savedQueriesDirectory = DbToolConfiguration.getInstance().getQueryManagerConfig().getSavedQueryDirectory();
        if ((savedQueriesDirectory == null) || ("".equals(savedQueriesDirectory))) {
            logger.warn("No defined saved queries directory !");
            return;
        }
        if (!savedQueriesDirectory.endsWith(File.separator)) {
            savedQueriesDirectory += File.separator;
        }
        File savedQueriesDir = new File(savedQueriesDirectory);
        if (!savedQueriesDir.isDirectory()) {
            logger.error("Cannot load sql queries since saved queries directory setup is invalid : " + savedQueriesDirectory);
            return;
        }
        sqlQueries = new SQLQueryDirectory();
        sqlQueries = loadSqlQueries(savedQueriesDir);
        if (sendEvent) {
            fireQueriesLoadedEvent();
        }
    }

    /**
     * Permet de charger les fichiers contenant les queries sauvées par l'utilisateur, ceci
     * à partir d'un répertoire donné en paramétre. La méthode charge TOUS les fichiers SQLQuery
     * contenus dans TOUS les sous répertoires.
     * @param fromDirectory est un objet File représentant un répertoire.
     */
    protected SQLQueryDirectory loadSqlQueries(File fromDirectory) {
        SQLQueryDirectory list = new SQLQueryDirectory();
        list.setName(fromDirectory.getAbsolutePath());
        if (!fromDirectory.isDirectory()) {
            logger.warn("The file given for loading sql queries is not a directory : " + fromDirectory.getAbsolutePath());
            return list;
        }
        logger.debug("Loading SQL queries from : " + fromDirectory.getAbsolutePath());
        SQLFileFilter filter = new SQLFileFilter();
        File[] files = fromDirectory.listFiles(filter);
        for (int cpt = 0; cpt < files.length; cpt++) {
            File currentFile = files[cpt];
            if (currentFile.isDirectory()) {
                list.addLast(loadSqlQueries(currentFile));
            } else {
                SQLQuery query = new SQLQuery(currentFile.getAbsolutePath());
                if (!query.load()) {
                    logger.warn("Cannot load XML query in file : " + query.getAbsolutePath());
                    logger.warn("Trying plain text format...");
                    String plainTextContent = query.loadPlainText();
                    if (plainTextContent == null) {
                        logger.error("Definitively NOT a valid query file for DB tool. Shoudl be either an XML file or plain text file.");
                    } else {
                        query.setText(plainTextContent);
                        query.setTitle(query.getName());
                        list.addLast(query);
                    }
                } else {
                    list.addLast(query);
                }
            }
        }
        logger.debug("Number of queries found " + list.size() + " in directory " + fromDirectory.getAbsolutePath());
        return list;
    }

    public SQLQueryDirectory getSqlQueries() {
        return sqlQueries;
    }

    /**
     * Permet d'ajouter un listener sur les operations du query manager.
     * @param listener le listener à ajouter.
     * @since 2.0.0
     */
    public synchronized void addQueryManagerListener(QueryManagerListener listener) {
        if (!listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    /**
     * Permet de supprimer un listener des operations du query manager.
     * Le listener ne sera plus notifié des operations passées sur ce query manager.
     * @param listener le listener à retirer.
     * @since 2.0.0
     */
    public synchronized void removeQueryManagerListener(QueryManagerListener listener) {
        listeners.remove(listener);
    }

    /**
     * Permet de notifier les listeners qu'une query a été ajoutée.
     * @param query
     * @since 2.0.0
     */
    public void fireQueryAddedEvent(SQLQuery query) {
        QueryManagerEvent evt = new QueryManagerEvent();
        evt.setEventType(QueryManagerEvent.EventType.QUERY_ADDED);
        evt.setQuery(query);
        pendingEvents.add(evt);
        synchronized (pendingEvents) {
            pendingEvents.notifyAll();
        }
    }

    /**
     * Permet de notifier les listeners qu'une query a été retirée.
     * @param query
     * @since 2.0.0
     */
    public void fireQueryRemovedEvent(File query) {
        QueryManagerEvent evt = new QueryManagerEvent();
        evt.setEventType(QueryManagerEvent.EventType.QUERY_REMOVED);
        evt.setQuery(query);
        pendingEvents.add(evt);
        synchronized (pendingEvents) {
            pendingEvents.notifyAll();
        }
    }

    public void fireQueriesLoadedEvent() {
        QueryManagerEvent evt = new QueryManagerEvent();
        evt.setEventType(QueryManagerEvent.EventType.QUERIES_LOADED);
        evt.setQuery(null);
        pendingEvents.add(evt);
        synchronized (pendingEvents) {
            pendingEvents.notifyAll();
        }
    }

    /**
     * Thread permettant l'envoi des events en asynchrone.
     * @since 2.0.0
     */
    protected Thread eventThread = new Thread("QueryManagerEventThread") {

        @Override
        public void run() {
            try {
                boolean neverStop = true;
                while (neverStop) {
                    synchronized (pendingEvents) {
                        pendingEvents.wait();
                    }
                    while (!pendingEvents.isEmpty()) {
                        QueryManagerEvent evt = null;
                        synchronized (pendingEvents) {
                            evt = pendingEvents.remove(0);
                        }
                        if (evt != null) {
                            for (QueryManagerListener listener : listeners) {
                                listener.queryManaged(evt);
                            }
                        }
                    }
                }
            } catch (InterruptedException ie) {
                logger.error("Interrupted query manager event thread.", ie);
            }
        }
    };
}
