package misc;

import java.util.*;
import java.util.jar.*;
import java.io.*;
import java.net.*;
import java.lang.reflect.*;

public class Util {

    public static String uniqString = "uniq";

    public static String multiString = "multi";

    public static String spliceString = "splice";

    public static String getIthField(String line, String delimiter, int idx) {
        String[] tokens = line.split(delimiter);
        return tokens[idx];
    }

    public static String getIthField(String line, int idx) {
        return getIthField(line, "\t", idx);
    }

    public static MappingResultIterator getMRIinstance(String mappingSrc, Map mappingMethodMap, String mappingMethod) {
        MappingResultIterator mri = null;
        try {
            Class mriClass = (Class) mappingMethodMap.get(mappingMethod);
            Class[] mriConstructorParameter = { String.class };
            Constructor mriConstructor = mriClass.getConstructor(mriConstructorParameter);
            Object[] mriparameter = { mappingSrc };
            mri = (MappingResultIterator) mriConstructor.newInstance(mriparameter);
        } catch (Exception ex) {
            ex.printStackTrace();
            System.exit(1);
        }
        return mri;
    }

    public static Map getMethodMap(String templetClassName, String classpath, String packageName) {
        int i;
        Map methodMap = new LinkedHashMap();
        ArrayList jarArray = new ArrayList();
        StringTokenizer st = new StringTokenizer(classpath, System.getProperty("path.separator"));
        while (st.hasMoreTokens()) {
            File file = new File(st.nextToken());
            if (file.isFile()) {
                jarArray.add(file);
            }
        }
        File jarList[] = (File[]) jarArray.toArray(new File[jarArray.size()]);
        URL[] urls = new URL[jarList.length];
        for (i = 0; i < jarList.length; i++) {
            try {
                urls[i] = jarList[i].toURI().toURL();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        ClassLoader classLoader = null;
        try {
            classLoader = URLClassLoader.newInstance(urls);
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
        int enumCnt = 0;
        String tmpStr, classPathStr = "";
        File classPathFile;
        JarEntry je;
        try {
            Class templetClass = Class.forName(templetClassName);
            for (i = 0; i < jarList.length; i++) {
                JarFile jf = new JarFile(jarList[i]);
                Enumeration jfEnum = jf.entries();
                while (jfEnum.hasMoreElements()) {
                    je = (JarEntry) jfEnum.nextElement();
                    if (je.getName().toLowerCase().endsWith(".class")) {
                        enumCnt++;
                        tmpStr = je.getName();
                        if (tmpStr.startsWith(packageName) == false) continue;
                        classPathFile = new File(tmpStr.substring(0, tmpStr.length() - 6));
                        classPathStr = "";
                        while (classPathFile != null) {
                            classPathStr = classPathFile.getName() + "." + classPathStr;
                            classPathFile = classPathFile.getParentFile();
                        }
                        classPathStr = classPathStr.substring(0, classPathStr.length() - 1);
                        Class methodClass = classLoader.loadClass(classPathStr);
                        if (templetClass.isAssignableFrom(methodClass) && Modifier.isAbstract(methodClass.getModifiers()) == false && methodClass.isInterface() == false) {
                            try {
                                String methodStr = (String) methodClass.getField("methodName").get(null);
                                methodMap.put(methodStr, methodClass);
                            } catch (NoSuchFieldException ex) {
                                System.err.println("class " + classPathStr + " doesn't have field methodName");
                            }
                        }
                    }
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            System.exit(1);
        }
        return methodMap;
    }

    public static boolean isReadIDPaired(String readA, String readB) {
        if ((readA.trim().length() <= 0) || (readB.trim().length() <= 0)) return false;
        String strA = readA.substring(0, readA.length() - 1);
        String strB = readB.substring(0, readB.length() - 1);
        if (strA.equals(strB)) {
            return true;
        } else {
            return false;
        }
    }

    public static CanonicalGFF getIntronicCGFF(CanonicalGFF exonCGFF) {
        return getIntronicCGFF(exonCGFF, false);
    }

    public static CanonicalGFF getIntronicCGFF(CanonicalGFF exonCGFF, boolean keepGeneRegion) {
        Map chromosomeIntronSetsMap = new TreeMap();
        for (Iterator geneIterator = exonCGFF.geneRegionMap.keySet().iterator(); geneIterator.hasNext(); ) {
            Object geneID = geneIterator.next();
            GenomeInterval gi = (GenomeInterval) exonCGFF.geneRegionMap.get(geneID);
            Set exonRegions = (Set) exonCGFF.geneExonRegionMap.get(geneID);
            if (exonRegions.size() <= 1) {
                if (keepGeneRegion) {
                    if (chromosomeIntronSetsMap.containsKey(gi.getChr())) {
                        Set intronSets = (Set) chromosomeIntronSetsMap.get(gi.getChr());
                        intronSets.add(geneID);
                    } else {
                        Set intronSets = new HashSet();
                        intronSets.add(geneID);
                        chromosomeIntronSetsMap.put(gi.getChr(), intronSets);
                    }
                    continue;
                } else {
                    continue;
                }
            }
            Set intronRegions = new TreeSet();
            Iterator exonIterator = exonRegions.iterator();
            Interval lastExon = (Interval) exonIterator.next();
            for (; exonIterator.hasNext(); ) {
                Interval thisExon = (Interval) exonIterator.next();
                intronRegions.add(new Interval(lastExon.getStop() + 1, thisExon.getStart() - 1, geneID));
                lastExon = thisExon;
            }
            if (chromosomeIntronSetsMap.containsKey(gi.getChr())) {
                Set intronSets = (Set) chromosomeIntronSetsMap.get(gi.getChr());
                intronSets.add(intronRegions);
            } else {
                Set intronSets = new HashSet();
                intronSets.add(intronRegions);
                chromosomeIntronSetsMap.put(gi.getChr(), intronSets);
            }
        }
        CanonicalGFF intronCGFF;
        if (keepGeneRegion) {
            intronCGFF = new CanonicalGFF(chromosomeIntronSetsMap, exonCGFF.geneRegionMap);
        } else {
            intronCGFF = new CanonicalGFF(chromosomeIntronSetsMap);
        }
        return intronCGFF;
    }
}
