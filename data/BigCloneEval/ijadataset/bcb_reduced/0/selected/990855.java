package org.alicebot.server.core;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.Reader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Set;
import java.util.StringTokenizer;
import org.alicebot.server.core.loader.AIMLLoader;
import org.alicebot.server.core.loader.AIMLWatcher;
import org.alicebot.server.core.logging.Log;
import org.alicebot.server.core.util.Trace;
import org.alicebot.server.core.node.Nodemapper;
import org.alicebot.server.core.node.Nodemaster;
import org.alicebot.server.core.parser.AIMLReader;
import org.alicebot.server.core.responder.Responder;
import org.alicebot.server.core.responder.TextResponder;
import org.alicebot.server.core.targeting.TargetMaster;
import org.alicebot.server.core.util.Match;
import org.alicebot.server.core.util.NoMatchException;
import org.alicebot.server.core.util.Shell;
import org.alicebot.server.core.util.Substituter;
import org.alicebot.server.core.util.Toolkit;

/**
 *  <p>
 *  The <code>Graphmaster</code> is the &quot;brain&quot; of an Alicebot.  
 *  It consists of a collection of nodes called <code>Nodemapper</code>s.
 *  These <code>Nodemapper</code>s map the branches from each node.
 *  The branches are either single words or wildcards.
 *  </p>
 *  <p>
 *  The root of the <code>Graphmaster</code> is a <code>Nodemapper</code>
 *  with many branches, one for each of the first words of all the patterns
 *  (40,000 in the case of the A.L.I.C.E. brain). The number of leaf nodes
 *  in the graph is equal to the number of categories, and each leaf node
 *  contains the &lt;template&gt; tag.
 *  </p>
 *
 * @author Richard Wallace, Jon Baer
 * @author Thomas Ringate/Pedro Colla
 * @version 4.1.2
 */
public class Graphmaster {

    /** Copyright notice. */
    public static final String[] COPYRIGHT = { "Alicebot Program D (c) 2001 A.L.I.C.E. AI Foundation", "All Rights Reserved.", "This program is free software; you can redistribute it and/or", "modify it under the terms of the GNU General Public License", "as published by the Free Software Foundation; either version 2", "of the License, or (at your option) any later version." };

    /** Version of this package. */
    public static final String VERSION = "4.1.4";

    /** Build Number of this package (internal regression test control). */
    public static final String BUILD = "00";

    /** The maximum depth of the Graphmaster. */
    public static final int MAX_DEPTH = 24;

    /** A template marker. */
    public static final String TEMPLATE = "<template>";

    /** A that marker. */
    public static final String THAT = "<that>";

    /** A topic marker. */
    public static final String TOPIC = "<topic>";

    /** A filename marker. */
    public static final String FILENAME = "<filename>";

    /** The <code>*</code> wildcard. */
    public static final String ASTERISK = "*";

    /** The <code>_</code> wildcard. */
    public static final String UNDERSCORE = "_";

    /** A path separator. */
    public static final String PATH_SEPARATOR = ":";

    /** An empty string. */
    private static final String EMPTY_STRING = "";

    /** The start of a marker. */
    private static final String MARKER_START = "<";

    /** The end of a marker. */
    private static final String MARKER_END = ">";

    /** A space. */
    private static final String SPACE = " ";

    /** Match state: in <code>input</code> portion of path. */
    private static final int S_INPUT = 0;

    /** Match state: in <code>that</code> portion of path. */
    private static final int S_THAT = 1;

    /** Match state: in <code>topic</code> portion of path. */
    private static final int S_TOPIC = 2;

    /** The root {@link Nodemaster}. */
    private static Nodemapper ROOT = new Nodemaster();

    /** The total number of categories read. */
    private static int TOTAL_CATEGORIES = 0;

    /** Load time marker. */
    private static boolean loadtime = true;

    /** Set of files loaded. */
    private static HashSet loadedFiles = new HashSet();

    /** The current working directory (used by load). */
    private static String workingDirectory = System.getProperty("user.dir");

    /** Set of activated nodes. */
    private static Set ACTIVATED_NODES = new HashSet();

    /** Activations marker. */
    private static final String ACTIVATIONS = "<activations>";

    /**
     *  Creates a new <code>Graphmaster</code>.
     */
    public Graphmaster() {
    }

    /**
     *  Adds a new pattern-that-topic path to the <code>Graphmaster</code> root.
     *
     *  @param pattern  &lt;pattern/&gt; path component
     *  @param that     &lt;that/&gt; path component
     *  @param topic    &lt;topic/&gt; path component
     *
     *  @return <code>Nodemapper</code> which is the result of adding the path.
     */
    public static Nodemapper add(String pattern, String that, String topic) {
        Nodemapper node = add(new StringTokenizer(pattern + SPACE + THAT + SPACE + that + SPACE + TOPIC + SPACE + topic), ROOT);
        return (node);
    }

    /**
     *  Adds a new path to the <code>Graphmaster</code> at a given node.
     *
     *  @since  4.1.3
     *
     *  @param path     the path to add
     *  @param parent   the <code>Nodemapper</code> parent to which the child should be appended
     *
     *  @return <code>Nodemapper</code> which is the result of adding the node
     */
    public static Nodemapper add(StringTokenizer tokenizer, Nodemapper parent) {
        if (tokenizer.countTokens() == 0) {
            return parent;
        } else {
            String word = tokenizer.nextToken();
            Nodemapper node;
            if (parent.containsKey(word)) {
                node = (Nodemapper) parent.get(word);
            } else {
                node = new Nodemaster();
                parent.put(word, node);
            }
            return add(tokenizer, node);
        }
    }

    /**
     *  <p>
     *  Searches for a match in the <code>Graphmaster</code> to a given path.
     *  </p>
     *  <p>
     *  This is a high-level prototype, used for external access.
     *  </p>
     *
     *  @see #match(Nodemapper, Nodemapper, String, String, String, boolean, boolean, boolean)
     *
     *  @param pattern  &lt;pattern/&gt; path component
     *  @param that     &lt;that/&gt; path component
     *  @param topic    &lt;topic/&gt; path component
     *
     *  @return the resulting <code>Match</code> object
     *
     *  @throws NoMatchException if no match was found
     */
    public static Match match(String input, String that, String topic) throws NoMatchException {
        Match match;
        if (input.length() < 1) {
            input = ASTERISK;
        }
        if (that.length() < 1) {
            that = ASTERISK;
        }
        if (topic.length() < 1) {
            topic = ASTERISK;
        }
        String inputPath = input + SPACE + THAT + SPACE + that + SPACE + TOPIC + SPACE + topic;
        match = match(ROOT, ROOT, inputPath, EMPTY_STRING, EMPTY_STRING, S_INPUT);
        if (match != null) {
            if (Globals.useTargeting()) {
                Nodemapper matchNodemapper = match.getNodemapper();
                if (matchNodemapper == null) {
                    Trace.devinfo("Match nodemapper is null!");
                } else {
                    Set activations = (Set) matchNodemapper.get(ACTIVATIONS);
                    if (activations == null) {
                        activations = new HashSet();
                    }
                    String path = match.getPath() + SPACE + PATH_SEPARATOR + SPACE + Substituter.replace(Graphmaster.TOPIC, Graphmaster.PATH_SEPARATOR, Substituter.replace(Graphmaster.THAT, Graphmaster.PATH_SEPARATOR, inputPath));
                    if (!activations.contains(path)) {
                        activations.add(path);
                        match.getNodemapper().put(ACTIVATIONS, activations);
                        ACTIVATED_NODES.add(match.getNodemapper());
                    }
                }
            }
            return match;
        } else {
            throw new NoMatchException(inputPath);
        }
    }

    /**
     *  <p>
     *  Searches for a match in the <code>Graphmaster</code> to a given path.
     *  </p>
     *  <p>
     *  This is a low-level prototype, used for internal recursion.
     *  </p>
     *
     *  @see #match(String, String, String)
     *
     *  @param nodemapper   the nodemapper where we start matching
     *  @param parent       the parent of the nodemapper where we start matching
     *  @param input        the whole input as a String
     *  @param star         contents absorbed by a wildcard
     *  @param path         the whole path as a String
     *  @param matchState   state variable tracking which part of the path we're in
     *
     *  @return the resulting <code>Match</code> object
     */
    public static Match match(Nodemapper nodemapper, Nodemapper parent, String input, String star, String path, int matchState) {
        StringTokenizer tokenizer = new StringTokenizer(input);
        Match match;
        if (tokenizer.countTokens() == 0) {
            if (nodemapper.containsKey(TEMPLATE)) {
                match = new Match();
                match.pushTopicStar(star.trim());
                match.setTopic(path.trim());
                match.setNodemapper(nodemapper);
                return match;
            } else {
                return null;
            }
        }
        String word = tokenizer.nextToken();
        String tail = EMPTY_STRING;
        if (tokenizer.hasMoreTokens()) {
            tail = input.substring(word.length() + 1, input.length());
        }
        if (nodemapper.containsKey(UNDERSCORE)) {
            match = match((Nodemapper) nodemapper.get(UNDERSCORE), nodemapper, tail, word, path + SPACE + UNDERSCORE, matchState);
            if (match != null) {
                switch(matchState) {
                    case S_INPUT:
                        if (star.length() > 0) {
                            match.pushInputStar(star.trim());
                        }
                        break;
                    case S_THAT:
                        if (star.length() > 0) {
                            match.pushThatStar(star.trim());
                        }
                        break;
                    case S_TOPIC:
                        if (star.length() > 0) {
                            match.pushTopicStar(star.trim());
                        }
                        break;
                }
                return match;
            }
        }
        if (nodemapper.containsKey(word)) {
            if (word.startsWith(MARKER_START)) {
                if (word.compareToIgnoreCase(THAT) == 0) {
                    matchState = S_THAT;
                } else if (word.compareToIgnoreCase(TOPIC) == 0) {
                    matchState = S_TOPIC;
                }
                match = match((Nodemapper) nodemapper.get(word), nodemapper, tail, EMPTY_STRING, EMPTY_STRING, matchState);
            } else {
                match = match((Nodemapper) nodemapper.get(word), nodemapper, tail, star, path + SPACE + word, matchState);
            }
            if (match != null) {
                if (word.compareToIgnoreCase(THAT) == 0) {
                    matchState = S_THAT;
                    match.pushInputStar(star.trim());
                    match.setPattern(path.trim());
                } else if (word.compareToIgnoreCase(TOPIC) == 0) {
                    matchState = S_TOPIC;
                    match.pushThatStar(star.trim());
                    match.setThat(path.trim());
                }
                return match;
            }
        }
        if (nodemapper.containsKey(ASTERISK)) {
            match = match((Nodemapper) nodemapper.get(ASTERISK), nodemapper, tail, word, path + SPACE + ASTERISK, matchState);
            if (match != null) {
                switch(matchState) {
                    case S_INPUT:
                        if (star.length() > 0) {
                            match.pushInputStar(star.trim());
                        }
                        break;
                    case S_THAT:
                        if (star.length() > 0) {
                            match.pushThatStar(star.trim());
                        }
                        break;
                    case S_TOPIC:
                        if (star.length() > 0) {
                            match.pushTopicStar(star.trim());
                        }
                        break;
                }
                return match;
            }
        }
        if (nodemapper.equals(parent.get(ASTERISK)) || nodemapper.equals(parent.get(UNDERSCORE))) {
            return match(nodemapper, parent, tail, star + SPACE + word, path, matchState);
        }
        return null;
    }

    /**
     *  Removes a pattern-that-topic path from the <code>Graphmaster</code> root.
     *
     *  @param pattern  &lt;pattern/&gt; path component
     *  @param that     &lt;that/&gt; path component
     *  @param topic    &lt;topic/&gt; path component
     *
     *  @return whether the removal was successful
     */
    public static boolean remove(String pattern, String that, String topic) {
        String path = pattern + SPACE + THAT + SPACE + that + SPACE + TOPIC + SPACE + topic;
        boolean success = remove(new StringTokenizer(path), ROOT);
        return success;
    }

    /**
     *  <p>
     *  Removes a child node from the <code>Graphmaster</code>.
     *  </p>
     *  <p>
     *  This method just takes a path as one String and tokenizes it, then passes to the
     *  {@link #remove(StringTokenizer, Nodemapper)} version
     *  of <code>Graphmaster.remove</code>, so
     *  you may want to use the other version directly.
     *  </p>
     *  
     *  @see #remove(StringTokenizer, Nodemapper, int, int, String)
     *
     *  @param sentence the node in sentence form
     *  @param parent   the <code>Nodemapper</code> parent from which the child should be removed
     *
     *  @return whether the removal was successful
     */
    public static boolean remove(String sentence, Nodemapper parent) {
        StringTokenizer tokenizer = new StringTokenizer(sentence);
        return remove(tokenizer, parent);
    }

    /**
     *  Removes a child node from the <code>Graphmaster</code>.
     *
     *  @param tokenizer    the sentence to remove
     *  @param parent       the <code>Nodemapper</code> parent from which the child should be removed
     *
     *  @return whether the removal was successful
     */
    public static boolean remove(StringTokenizer tokenizer, Nodemapper parent) {
        String word = tokenizer.nextToken();
        Nodemapper node;
        if (parent.containsKey(word)) {
            node = (Nodemapper) parent.get(word);
        } else {
            return false;
        }
        boolean success = remove(tokenizer, node);
        if (success && node.keySet().size() == 1) {
            parent.remove(word);
        }
        return success;
    }

    /**
     *  Marks the end of loadtime.  Depending on settings in {@link Globals},
     *  displays various trace information on the console,
     *  and writes startup information to the log..
     */
    public static void ready() {
        loadtime = false;
        Globals.setBotName();
        if (Globals.showConsole()) {
            Log.userinfo("\"" + Globals.getBotName() + "\" is thinking with " + TOTAL_CATEGORIES + " categories.", Log.STARTUP);
        }
        Trace.insist(COPYRIGHT);
        Log.userinfo("Alicebot Program D version " + VERSION + " Build [" + BUILD + "]", Log.STARTUP);
    }

    /**
     *  Tells the PredicateMaster to
     *  save all predicates.
     */
    public static void shutdown() {
        PredicateMaster.saveAll();
    }

    /**
     *  Sends new targeting data to
     *  {@link org.alicebot.server.core.util.Targets Targets}.
     */
    public static synchronized void checkpoint() {
        if (!Globals.useTargeting()) {
            return;
        }
        Log.log("Targeting checkpoint.", Log.TARGETING);
        Iterator activatedNodesIterator = ACTIVATED_NODES.iterator();
        while (activatedNodesIterator.hasNext()) {
            Nodemapper nodemapper = (Nodemapper) activatedNodesIterator.next();
            Set activations = (Set) nodemapper.get("<activations>");
            Iterator activationsIterator = activations.iterator();
            while (activationsIterator.hasNext()) {
                String path = (String) activationsIterator.next();
                StringTokenizer pathTokenizer = new StringTokenizer(path, PATH_SEPARATOR);
                int tokenCount = pathTokenizer.countTokens();
                if (tokenCount == 6) {
                    String matchPattern = pathTokenizer.nextToken().trim();
                    String matchThat = pathTokenizer.nextToken().trim();
                    String matchTopic = pathTokenizer.nextToken().trim();
                    String matchTemplate = (String) nodemapper.get(TEMPLATE);
                    String inputText = pathTokenizer.nextToken().trim();
                    String inputThat = pathTokenizer.nextToken().trim();
                    String inputTopic = pathTokenizer.nextToken().trim();
                    TargetMaster.add(matchPattern, matchThat, matchTopic, matchTemplate, inputText, inputThat, inputTopic);
                }
                activationsIterator.remove();
            }
        }
    }

    /**
     *  Loads the <code>Graphmaster</code> with the contents of a given path.
     *
     *  @param path path to the file(s) to load
     */
    public static void load(String path) {
        if (path.length() < 1) {
            Log.userinfo("Cannot open a file whose name has zero length.", Log.ERROR);
        }
        if (!loadtime) {
            if (path.equals(Globals.getStartupFilePath())) {
                Log.userinfo("Cannot reload startup file.", Log.ERROR);
            }
        }
        BufferedReader buffReader = null;
        if (path.indexOf("://") != -1) {
            URL url = null;
            try {
                url = new URL(path);
            } catch (MalformedURLException e) {
                Log.userinfo("Malformed URL: \"" + path + "\"", Log.ERROR);
            }
            try {
                String encoding = Toolkit.getDeclaredXMLEncoding(url.openStream());
                buffReader = new BufferedReader(new InputStreamReader(url.openStream(), encoding));
            } catch (IOException e) {
                Log.userinfo("I/O error trying to read \"" + path + "\"", Log.ERROR);
            }
        } else {
            if (path.indexOf(ASTERISK) != -1) {
                String[] files = null;
                try {
                    files = Toolkit.glob(path, workingDirectory);
                } catch (FileNotFoundException e) {
                    Log.userinfo(e.getMessage(), Log.ERROR);
                }
                if (files != null) {
                    for (int index = 0; index < files.length; index++) {
                        load(files[index]);
                    }
                    return;
                }
            }
            File toRead = new File(path);
            if (toRead.isAbsolute()) {
                workingDirectory = toRead.getParent();
            }
            if (loadedFiles.contains(toRead)) {
                if (loadtime) {
                    return;
                }
            } else {
                loadedFiles.add(toRead);
            }
            if (toRead.exists() && !toRead.isDirectory()) {
                try {
                    String encoding = Toolkit.getDeclaredXMLEncoding(new FileInputStream(path));
                    buffReader = new BufferedReader(new InputStreamReader(new FileInputStream(path), encoding));
                } catch (IOException e) {
                    Log.userinfo("I/O error trying to read \"" + path + "\"", Log.ERROR);
                    return;
                }
                if (Globals.isWatcherActive()) {
                    AIMLWatcher.addWatchFile(path);
                }
            } else {
                if (!toRead.exists()) {
                    Log.userinfo("\"" + path + "\" does not exist!", Log.ERROR);
                }
                if (toRead.isDirectory()) {
                    Log.userinfo("\"" + path + "\" is a directory!", Log.ERROR);
                }
            }
        }
        new AIMLReader(path, buffReader, new AIMLLoader(path)).read();
    }

    /**
     *  Returns the number of categories presently loaded.
     *
     *  @return the number of categories presently loaded
     */
    public static int getTotalCategories() {
        return TOTAL_CATEGORIES;
    }

    /**
     *  Increments the total categories.
     *
     *  @return the number of categories presently loaded
     */
    public static int incrementTotalCategories() {
        return TOTAL_CATEGORIES++;
    }
}
