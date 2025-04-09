package org.gjt.universe;

import java.lang.*;
import java.io.*;
import java.util.*;
import java.util.zip.*;

public final class UPM {

    protected static boolean loadClassesAtStartup = false;

    protected static Vector piVector = null;

    protected static String[] categories = null;

    private UPM() {
    }

    /*********************************************************************
	 *
	 *	This initializes UPM. It scans the directory "directory" for .jar
	 *	files. This must be called before any other method is called, 
	 *	except the utility methods.
	 *
	 *********************************************************************/
    public static void init(File directory) {
        File dirs[] = new File[1];
        dirs[0] = directory;
        init(dirs);
    }

    /*********************************************************************
	 *
	 *	This initializes UPM. It scans each directory in the array.
	 *	This must be called before any other method is called, except
	 *	the utility methods.
	 *
	 *********************************************************************/
    public static void init(File[] directories) {
        piVector = new Vector(20, 10);
        for (int dirCount = 0; dirCount < directories.length; dirCount++) {
            File currentDir = directories[dirCount];
            String jarFiles[] = getJarFilesFromDir(currentDir);
            for (int i = 0; i < jarFiles.length; i++) {
                ManifestItems mi = readManifestFromJar(jarFiles[i]);
                if (mi != null) {
                    getPIForBeans(jarFiles[i], mi);
                }
            }
        }
        getCategories();
    }

    /*********************************************************************
	 *
	 *	This method determines if classes are preloaded (automatically)
	 *	during init(). Thus it must be called before init(). 
	 *
	 *********************************************************************/
    public static void setLoadClassesAtStartup(boolean value) {
        loadClassesAtStartup = value;
    }

    /*********************************************************************
	 *
	 *	returns PluginInfo for each plugin that was loaded. 
	 *	This is useful for diagnostics, so the user can see
	 *	what extensions & what type are loaded.
	 *
	 *********************************************************************/
    public static PluginInfo[] getPluginInfo() {
        PluginInfo pi[] = new PluginInfo[piVector.size()];
        piVector.copyInto(pi);
        return pi;
    }

    /*********************************************************************
	 *
	 *	returns an array which contains all the categories that have
	 *	been loaded.
	 *
	 *********************************************************************/
    public static String[] getCategories() {
        if (categories == null) {
            Vector cVec = new Vector(10);
            for (int i = 0; i < piVector.size(); i++) {
                PluginInfo pi = (PluginInfo) piVector.elementAt(i);
                if (!cVec.contains(pi.category)) {
                    cVec.add(pi.category);
                }
            }
            categories = new String[cVec.size()];
            cVec.copyInto(categories);
        }
        return categories;
    }

    /*********************************************************************
	 *
	 *	returns if the category exists. A category will not exist if
	 *	no plugins of that category were loaded.
	 *
	 *********************************************************************/
    public static boolean doesCategoryExist(String category) {
        for (int i = 0; i < categories.length; i++) {
            if (categories[i].equalsIgnoreCase(category)) {
                return true;
            }
        }
        return false;
    }

    /*********************************************************************
	 *
	 *	returns the # of plugins for a given category.
	 *
	 *********************************************************************/
    public static int getNumInCategory(String category) {
        int count = 0;
        for (int i = 0; i < piVector.size(); i++) {
            PluginInfo pi = (PluginInfo) piVector.elementAt(i);
            if (pi.category.equalsIgnoreCase(category)) {
                count++;
            }
        }
        return count;
    }

    /*********************************************************************
	 *
	 *	returns the class names for each class of a given category.
	 *	This would permit loading of a specific class of a given type.
	 *
	 *********************************************************************/
    public static String[] getClassNamesForCategory(String category) {
        PluginInfo pi[] = findAllMatchingCategory(category);
        String s[] = null;
        if (pi != null) {
            s = new String[pi.length];
            for (int i = 0; i < pi.length; i++) {
                s[i] = pi[i].classname;
            }
            return s;
        }
        s = new String[0];
        return s;
    }

    /*********************************************************************
	 *
	 *	preload Class 'classname'; subsequent calls to newInstance() will
	 *	then execute faster.
	 *
	 *********************************************************************/
    public static boolean preloadClass(String classname) {
        PluginInfo pi = findClass(classname);
        if (pi != null) {
            JarLoader jl = new JarLoader(pi.jarname);
            try {
                if (pi.theclass == null) {
                    pi.theclass = jl.loadClass(pi.classname);
                }
                return true;
            } catch (Exception e) {
                Log.warning("Could not preload class \"" + classname + "\". Reason: " + e);
            }
        }
        return false;
    }

    /*********************************************************************
	 *
	 *	unloads a class.  This should conserve memory, at the expense
	 *	of newInstance() execution speed.
	 *
	 *********************************************************************/
    public static void unloadClass(String classname) {
        PluginInfo pi = findClass(classname);
        if (pi != null) {
            pi.theclass = null;
        }
    }

    /*********************************************************************
	 *
	 *	Preload an entire category of classes.
	 *
	 *********************************************************************/
    public static boolean preloadCategory(String category) {
        boolean success = true;
        PluginInfo pi[] = findAllMatchingCategory(category);
        if (pi != null) {
            for (int i = 0; i < pi.length; i++) {
                JarLoader jl = new JarLoader(pi[i].jarname);
                try {
                    if (pi[i].theclass == null) {
                        pi[i].theclass = jl.loadClass(pi[i].classname);
                    }
                } catch (Exception e) {
                    Log.warning("Could not preload class \"" + pi[i].classname + "\". Reason: " + e);
                    success = false;
                }
            }
        }
        return success;
    }

    /*********************************************************************
	 *
	 *	Unload an entire category of classes.
	 *
	 *********************************************************************/
    public static void unloadCategory(String category) {
        PluginInfo pi[] = findAllMatchingCategory(category);
        if (pi != null) {
            for (int i = 0; i < pi.length; i++) {
                pi[i].theclass = null;
            }
        }
    }

    /*********************************************************************
	 *
	 *	Instantiate an object; in this case, "classname".  This works
	 *	*exactly* the same as the 'new' keyword. Note that there must be
	 *	a public zero-argument constructor in the class. 
	 *
	 *********************************************************************/
    public static Object newInstance(String classname) {
        PluginInfo pi = findClass(classname);
        if (pi.theclass == null) {
            JarLoader jl = new JarLoader(pi.jarname);
            try {
                pi.theclass = jl.loadClass(pi.classname);
            } catch (Exception e) {
                Log.warning("newInstance() failed; could not load class \"" + classname + "\". Reason: " + e);
            }
        }
        return newInstance(pi.theclass);
    }

    /*********************************************************************
	 *
	 *	Instantiates all classes of a given category. 
	 *
	 *********************************************************************/
    public static Object[] newInstances(String category) {
        PluginInfo pi[] = findAllMatchingCategory(category);
        Object objects[] = new Object[pi.length];
        for (int i = 0; i < pi.length; i++) {
            if (pi[i].theclass == null) {
                JarLoader jl = new JarLoader(pi[i].jarname);
                try {
                    pi[i].theclass = jl.loadClass(pi[i].classname);
                } catch (Exception e) {
                    Log.warning("newInstances() failed; could not load class \"" + pi[i].classname + "\". Reason: " + e);
                }
            }
            objects[i] = newInstance(pi[i].theclass);
        }
        return objects;
    }

    /*********************************************************************
	 *
	 *	UTILITY METHOD: DOES NOT REQUIRE UPM INITIALIZATION
	 *
	 *	Given a class object 'c', instantiate it.
	 *	Note that this method will give user-friendly output as to
	 *	why the instantiation fails, if it does.
	 *
	 *	This is equivalent to using "new".
	 *
	 *	e.g.: Planet p = (Planet)newInstance(
	 *		Class.forname("universe.scheme.Planet_001"))
	 *
	 *********************************************************************/
    public static Object newInstance(Class c) {
        if (c != null) {
            try {
                return c.newInstance();
            } catch (IllegalAccessException e) {
                Log.warning("Cannot load class \"" + c.getName() + "\"; it contains no public zero-argument constructor.");
            } catch (InstantiationException e) {
                Log.warning("Cannot load class \"" + c.getName() + "\"; it may represent an abstract class or interface.");
            } catch (ExceptionInInitializerError e) {
                Log.warning("ExceptionInInitializerError: Class \"" + c.getName() + "\" failed to initialize. Reason: " + e.getMessage());
            } catch (SecurityException e) {
                Log.warning("SecurityException while loading class \"" + c.getName() + "\"; " + e.getMessage());
            }
        }
        return null;
    }

    /*********************************************************************
	 *
	 *	UTILITY METHOD: DOES NOT REQUIRE UPM INITIALIZATION
	 *
	 *	Same as newInstance(), but will generate a user-friendly error
	 *	if Class 'c' does not extend/implement 'cast'.  Note that 'cast'
	 *	may be either a class OR an interface.
	 *
	 *	example useage:
	 *		class foo extends bar;		
	 *		class notfoo
	 *
	 *	newInstanceChecked(foo, bar);		// ok (foo derived from bar)
	 *	newInstanceChecked(foo, notfoo); 	// error
	 *			since foo does not implement/extend notfoo
	 *
	 *********************************************************************/
    public static Object newInstanceChecked(Class c, Class cast) {
        Object obj = newInstance(c);
        if (obj != null) {
            if (cast != null) {
                if (!c.isInstance(cast)) {
                    Log.warning("Cannot load class \"" + c + "\"; it does not ");
                    if (cast.isInterface()) {
                        Log.debug("implement interface ");
                    } else {
                        Log.debug("extend class ");
                    }
                    Log.debug("\"" + cast.getName() + "\".\n");
                    return null;
                }
            }
        }
        return obj;
    }

    /*********************************************************************
	 *
	 *	Class PluginInfo:
	 *
	 *	Contains the basic data about each plugin that has been loaded.
	 *	All fields marked optional may be null.
	 *
	 *********************************************************************/
    public static class PluginInfo {

        public String jarname = null;

        public String classname = null;

        public String category = null;

        public String version = null;

        public String author = null;

        public String home = null;

        public String minUVreqd = null;

        public Class theclass = null;
    }

    /*********************************************************************
	 *
	 *	private methods..
	 *
	 *
	 *
	 *
	 *********************************************************************/
    private static PluginInfo[] findAllMatchingCategory(String category) {
        Vector pv = new Vector();
        for (int i = 0; i < piVector.size(); i++) {
            PluginInfo pitmp = (PluginInfo) piVector.elementAt(i);
            if (pitmp.category.equalsIgnoreCase(category)) {
                pv.add(pitmp);
            }
        }
        PluginInfo pi[] = new PluginInfo[pv.size()];
        pv.copyInto(pi);
        return pi;
    }

    private static PluginInfo findClass(String className) {
        for (int i = 0; i < piVector.size(); i++) {
            PluginInfo pitmp = (PluginInfo) piVector.elementAt(i);
            if (pitmp.classname.equals(className)) {
                return pitmp;
            }
        }
        return null;
    }

    private static String[] getJarFilesFromDir(File dir) {
        if (dir.isDirectory()) {
            Vector files = new Vector();
            String s[] = dir.list();
            for (int i = 0; i < s.length; i++) {
                if (s[i].endsWith(".jar")) {
                    files.add(s[i]);
                }
            }
            String s2[] = new String[files.size()];
            files.copyInto(s2);
            s = null;
            files = null;
            return s2;
        }
        Log.warning("Path \"" + dir + "\" is not a valid directory");
        return new String[0];
    }

    private static ManifestItems readManifestFromJar(String jarfile) {
        try {
            ZipFile zfile = new ZipFile(jarfile);
            Enumeration enumer = zfile.entries();
            while (enumer.hasMoreElements()) {
                ZipEntry ze = (ZipEntry) enumer.nextElement();
                String name = ze.getName();
                if (name.charAt(0) == '/') {
                    name = name.substring(1);
                }
                if (name.equalsIgnoreCase("META-INF/MANIFEST.MF")) {
                    InputStream is = zfile.getInputStream(ze);
                    ManifestItems mi = new ManifestItems();
                    if (!mi.read(is)) {
                        Log.warning("unexpected EOF while parsing manifest file for plugin \"" + jarfile + "\"; not loaded.");
                    }
                    is.close();
                    return mi;
                }
            }
            zfile.close();
        } catch (Exception e) {
            Log.warning("could not read manifest for plugin \"" + jarfile + "\"; not loaded.");
        }
        return null;
    }

    private static void getPIForBeans(String jarfile, ManifestItems mi) {
        String global_author = mi.findValue("Universe-Plugin-Home");
        String global_home = mi.findValue("Universe-Plugin-Author");
        String global_minUVreqd = mi.findValue("Universe-Plugin-Minimum-Version");
        ManifestItems.KVPair kvp[] = mi.getKVPairs();
        GroupKVP gkvp = new GroupKVP(kvp);
        ManifestItems.KVPair subkvp[] = gkvp.getNextNameGroup();
        do {
            String isbean = findValueWithin(subkvp, "Java-Bean");
            if (isbean != null) {
                if (isbean.equalsIgnoreCase("true")) {
                    PluginInfo pi = new PluginInfo();
                    pi.jarname = jarfile;
                    String classname = findValueWithin(subkvp, "Name");
                    if (classname.endsWith(".class")) {
                        classname = classname.substring(0, classname.length() - 6);
                    }
                    pi.classname = classname.replace('/', '.');
                    pi.category = findValueWithin(subkvp, "Universe-Plugin-Category");
                    if (pi.category == null) {
                        Log.warning("Required Manifest entry \"Universe-Plugin-Category\" missing in plugin \"" + pi.classname + "\"; not loaded.");
                        return;
                    }
                    pi.version = findValueWithin(subkvp, "Universe-Plugin-Version");
                    if (pi.version == null) {
                        Log.warning("Required Manifest entry \"Universe-Plugin-Version\" missing in plugin \"" + pi.classname + "\"; not loaded.");
                        return;
                    }
                    pi.minUVreqd = findValueWithin(subkvp, "Universe-Plugin-Minimum-Version");
                    if (pi.minUVreqd == null) {
                        pi.minUVreqd = global_minUVreqd;
                    }
                    pi.author = findValueWithin(subkvp, "Universe-Plugin-Author");
                    if (pi.author == null) {
                        pi.author = global_author;
                    }
                    pi.home = findValueWithin(subkvp, "Universe-Plugin-Home");
                    if (pi.home == null) {
                        pi.home = global_home;
                    }
                    if (checkVersion(pi.minUVreqd)) {
                        piVector.add(pi);
                        if (loadClassesAtStartup) {
                            if (!preloadClass(pi.classname)) {
                                piVector.remove(pi);
                            }
                        }
                    }
                }
            }
            subkvp = gkvp.getNextNameGroup();
        } while (subkvp != null);
    }

    private static class GroupKVP {

        ManifestItems.KVPair gkvp[] = null;

        int start = 0;

        int beginIdx = 0;

        int endIdx = 0;

        public GroupKVP(ManifestItems.KVPair kvp[]) {
            gkvp = kvp;
            endIdx = kvp.length - 1;
        }

        public ManifestItems.KVPair[] getNextNameGroup() {
            if (start >= gkvp.length) {
                return null;
            }
            for (int i = start; i < gkvp.length; i++) {
                if (gkvp[i].key.equalsIgnoreCase("Name")) {
                    beginIdx = i;
                    break;
                }
            }
            for (int i = beginIdx + 1; i < gkvp.length; i++) {
                if (gkvp[i].key.equalsIgnoreCase("Name")) {
                    endIdx = i - 1;
                    break;
                }
            }
            ManifestItems.KVPair subkvp[] = new ManifestItems.KVPair[endIdx - beginIdx + 1];
            for (int i = 0; i < subkvp.length; i++) {
                subkvp[i] = gkvp[i + beginIdx];
            }
            start = endIdx + 1;
            return subkvp;
        }
    }

    private static String findValueWithin(ManifestItems.KVPair kvp[], String what) {
        for (int i = 0; i < kvp.length; i++) {
            if (kvp[i].key.equalsIgnoreCase(what)) {
                return kvp[i].value;
            }
        }
        return null;
    }

    private static boolean checkVersion(String pluginVersion) {
        if (pluginVersion == null) {
            return true;
        } else {
        }
        return true;
    }
}
