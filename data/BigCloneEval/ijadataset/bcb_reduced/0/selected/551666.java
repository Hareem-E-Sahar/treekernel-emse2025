package blue;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarInputStream;
import blue.noteProcessor.NoteProcessor;
import blue.orchestra.Instrument;
import blue.soundObject.SoundObject;
import blue.utility.ClassByteOutputStream;

public class PluginClassLoader extends ClassLoader {

    private Hashtable classes = new Hashtable();

    private ArrayList noteProcessors = new ArrayList();

    private ArrayList soundObjects = new ArrayList();

    private ArrayList instruments = new ArrayList();

    public Class[] getSoundObjectClasses() {
        return getClassArray(soundObjects);
    }

    public Class[] getNoteProcessorClasses() {
        return getClassArray(noteProcessors);
    }

    public Class[] getInstrumentClasses() {
        return getClassArray(instruments);
    }

    private Class[] getClassArray(ArrayList arr) {
        Class[] retVal = new Class[arr.size()];
        for (int i = 0; i < retVal.length; i++) {
            retVal[i] = (Class) arr.get(i);
        }
        return retVal;
    }

    public void loadPlugins() {
        String pluginFolder = BlueSystem.getUserConfigurationDirectory() + File.separator + "plugins";
        File dir = new File(pluginFolder);
        if (dir.exists() && dir.isDirectory()) {
            File[] jars = dir.listFiles(new FileFilter() {

                public boolean accept(File pathname) {
                    String name = pathname.getName();
                    return name.endsWith(".jar");
                }
            });
            for (int i = 0; i < jars.length; i++) {
                File file = jars[i];
                try {
                    JarFile f = new JarFile(file);
                    System.out.println("Reading Plugins from Jar: " + f.getName());
                    Enumeration entries = f.entries();
                    JarInputStream jis = new JarInputStream(new BufferedInputStream(new FileInputStream(file)));
                    while (entries.hasMoreElements()) {
                        JarEntry entry = (JarEntry) entries.nextElement();
                        if (entry.getName().endsWith(".class")) {
                            InputStream stream = f.getInputStream(entry);
                            ClassByteOutputStream baos = new ClassByteOutputStream(stream);
                            byte[] b = baos.toByteArray();
                            String className = entry.getName();
                            className = className.substring(0, className.length() - 6);
                            className = className.replaceAll("/", ".");
                            Class c = defineClass(className, b, 0, b.length);
                            classes.put(className, c);
                            Class[] interfaces = c.getInterfaces();
                            if (NoteProcessor.class.isAssignableFrom(c)) {
                                System.out.println("Found NoteProcessor: " + className);
                                noteProcessors.add(c);
                            }
                            if (SoundObject.class.isAssignableFrom(c)) {
                                System.out.println("Found SoundObject: " + className);
                                soundObjects.add(c);
                            }
                            if (Instrument.class.isAssignableFrom(c)) {
                                System.out.println("Found Instrument: " + className);
                                instruments.add(c);
                            }
                        }
                    }
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public PluginClassLoader() {
    }

    /**
     * This is a simple version for external clients since they will always want
     * the class resolved before it is returned to them.
     */
    public Class loadClass(String className) throws ClassNotFoundException {
        return (loadClass(className, true));
    }

    /**
     * This is the required version of loadClass which is called both from
     * loadClass above and from the internal function FindClassFromClass.
     */
    public synchronized Class loadClass(String className, boolean resolveIt) throws ClassNotFoundException {
        Class result;
        byte classData[];
        result = (Class) classes.get(className);
        if (result != null) {
            return result;
        }
        try {
            result = Class.forName(className);
        } catch (ClassNotFoundException e) {
        }
        if (result == null) {
            try {
                result = super.findSystemClass(className);
                return result;
            } catch (ClassNotFoundException e) {
                System.out.println(" >>>>>> Not a system class.");
                System.out.println("Class With Error: " + className);
            }
        }
        if (result == null) {
            throw new ClassFormatError();
        }
        if (resolveIt) {
            resolveClass(result);
        }
        classes.put(className, result);
        return result;
    }

    /**
     * @param args
     */
    public static void main(String[] args) {
        PluginClassLoader loader = new PluginClassLoader();
        loader.loadPlugins();
    }
}
