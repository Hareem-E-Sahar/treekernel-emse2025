package javax.help.search;

import java.util.Hashtable;
import java.util.Vector;
import java.util.Enumeration;
import java.util.Locale;
import java.net.URL;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import javax.help.HelpSet;
import javax.help.HelpUtilities;
import javax.help.NavigatorView;
import javax.help.search.SearchListener;
import javax.help.search.SearchEvent;
import javax.help.search.SearchEngine;
import javax.help.search.SearchQuery;

public class MergingSearchEngine extends SearchEngine {

    private Vector engines;

    private Hashtable enginePerView = new Hashtable();

    private boolean stopQuery = false;

    public MergingSearchEngine(NavigatorView view) {
        if (view == null) {
            throw new IllegalArgumentException("view must not be null");
        }
        engines = new Vector();
        SearchEngine engine = makeEngine(view);
        engines.addElement(engine);
    }

    public MergingSearchEngine(SearchEngine engine) {
        if (engine == null) {
            throw new IllegalArgumentException("engine must not be null");
        }
        engines = new Vector();
        engines.addElement(engine);
    }

    /**
     * Creates the query for this helpset.
     */
    public SearchQuery createQuery() {
        return new MergingSearchQuery(this);
    }

    /**
     * Adds/Removes a Search Engine to/from list.
     *
     * Possibly the makeEngine should be delayed until the actual query.
     */
    public void merge(NavigatorView view) {
        if (view == null) {
            throw new IllegalArgumentException("view must not be null");
        }
        SearchEngine engine = makeEngine(view);
        if (engine == null) {
            throw new IllegalArgumentException("view is invalid");
        }
        engines.addElement(engine);
        enginePerView.put(view, engine);
    }

    public void remove(NavigatorView view) {
        if (view == null) {
            throw new IllegalArgumentException("view is either null or invalid");
        }
        SearchEngine engine = (SearchEngine) enginePerView.get(view);
        if (engine != null) {
            engines.removeElement(engine);
            enginePerView.remove(engine);
        } else {
            throw new IllegalArgumentException("view is either null or invalid");
        }
    }

    public Enumeration getEngines() {
        return engines.elements();
    }

    private SearchEngine makeEngine(NavigatorView view) {
        Hashtable params = view.getParameters();
        if (params == null || (params != null && !params.containsKey("data"))) {
            return null;
        }
        String engineName = (String) params.get("engine");
        HelpSet hs = view.getHelpSet();
        URL base = hs.getHelpSetURL();
        ClassLoader loader = hs.getLoader();
        if (engineName == null) {
            engineName = HelpUtilities.getDefaultQueryEngine();
            params.put("engine", engineName);
        }
        SearchEngine back = null;
        Constructor konstructor;
        Class types[] = { URL.class, Hashtable.class };
        Object args[] = { base, params };
        Class klass;
        debug("makeEngine");
        debug("  base: " + base);
        debug("  params: " + params);
        try {
            if (loader == null) {
                klass = Class.forName(engineName);
            } else {
                klass = loader.loadClass(engineName);
            }
        } catch (Throwable t) {
            throw new Error("Could not load engine named " + engineName + " for view: " + view);
        }
        try {
            konstructor = klass.getConstructor(types);
        } catch (Throwable t) {
            throw new Error("Could not find constructor for " + engineName + ". For view: " + view);
        }
        try {
            back = (SearchEngine) konstructor.newInstance(args);
        } catch (InvocationTargetException e) {
            System.err.println("Exception while creating engine named " + engineName + " for view: " + view);
            e.printStackTrace();
        } catch (Throwable t) {
            throw new Error("Could not create engine named " + engineName + " for view: " + view);
        }
        return back;
    }

    private class MergingSearchQuery extends SearchQuery implements SearchListener {

        private MergingSearchEngine mhs;

        private Vector queries;

        private String searchparams;

        public MergingSearchQuery(SearchEngine hs) {
            super(hs);
            if (hs instanceof MergingSearchEngine) {
                this.mhs = (MergingSearchEngine) hs;
            }
        }

        public synchronized void start(String searchparams, Locale l) throws IllegalArgumentException, IllegalStateException {
            MergingSearchEngine.this.debug("startSearch()");
            if (isActive()) {
                throw new IllegalStateException();
            }
            stopQuery = false;
            super.start(searchparams, l);
            queries = new Vector();
            for (Enumeration e = mhs.getEngines(); e.hasMoreElements(); ) {
                SearchEngine engine = (SearchEngine) e.nextElement();
                if (engine != null) {
                    queries.addElement(engine.createQuery());
                }
            }
            for (Enumeration e = queries.elements(); e.hasMoreElements(); ) {
                SearchQuery query = (SearchQuery) e.nextElement();
                query.addSearchListener(this);
                query.start(searchparams, l);
            }
        }

        public synchronized void stop() throws IllegalStateException {
            if (queries == null) {
                return;
            }
            stopQuery = true;
            boolean queriesActive = true;
            while (queriesActive) {
                queriesActive = false;
                if (queries == null) {
                    continue;
                }
                for (Enumeration e = queries.elements(); e.hasMoreElements(); ) {
                    SearchQuery query = (SearchQuery) e.nextElement();
                    if (query.isActive()) {
                        debug("queries are active waiting to stop");
                        queriesActive = true;
                    }
                }
                if (queriesActive) {
                    try {
                        wait(250);
                    } catch (InterruptedException ex) {
                        ex.printStackTrace();
                    }
                }
            }
            queries = null;
        }

        public boolean isActive() {
            if (queries == null) {
                return false;
            }
            for (Enumeration e = queries.elements(); e.hasMoreElements(); ) {
                SearchQuery query = (SearchQuery) e.nextElement();
                if (query.isActive()) {
                    return true;
                }
            }
            return false;
        }

        public SearchEngine getSearchEngine() {
            return mhs;
        }

        public synchronized void itemsFound(SearchEvent e) {
            SearchQuery queryin = (SearchQuery) e.getSource();
            if (stopQuery) {
                return;
            }
            if (queries != null) {
                Enumeration enum1 = queries.elements();
                while (enum1.hasMoreElements()) {
                    SearchQuery query = (SearchQuery) enum1.nextElement();
                    if (query == queryin) {
                        fireItemsFound(e);
                    }
                }
            }
        }

        public void searchStarted(SearchEvent e) {
        }

        public synchronized void searchFinished(SearchEvent e) {
            SearchQuery queryin = (SearchQuery) e.getSource();
            if (queries != null) {
                Enumeration enum1 = queries.elements();
                while (enum1.hasMoreElements()) {
                    SearchQuery query = (SearchQuery) enum1.nextElement();
                    if (query == queryin) {
                        queryin.removeSearchListener(this);
                        queries.removeElement(query);
                    }
                }
                if (queries.isEmpty()) {
                    queries = null;
                    if (!stopQuery) {
                        fireSearchFinished();
                    }
                }
            }
        }
    }

    private static final boolean debug = false;

    private static void debug(String msg) {
        if (debug) {
            System.err.println("MergineSearchEngine: " + msg);
        }
    }
}
