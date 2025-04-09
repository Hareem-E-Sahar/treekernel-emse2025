package launcher;

import java.io.*;
import java.net.*;
import java.util.*;
import org.docflower.enhancer.SerializableEnhancer;
import org.docflower.enhancer.utils.*;
import org.objectweb.asm.*;

public class EnhanceLauncher {

    private static EnhancerParams params = new EnhancerParams();

    /**
	 * @param args
	 * @throws IOException
	 */
    public static void main(String[] args) throws IOException {
        Date beginDate = new Date();
        URL[] urls = new URL[] { new URL("file://" + new File("/opt/tmp/LRLBuild/plugins/com.luckyrelease.depo.server_1.0.0.M1/@dot/").getCanonicalPath() + "/"), new URL("file://" + new File("/opt/tmp/LRLBuild/plugins/org.docflower.server_0.5.0.M2/@dot/").getCanonicalPath() + "/"), new URL("file://" + new File("../org.docflower.serializer/bin").getCanonicalPath() + "/"), new URL("file://" + new File("../DocFlowerServer/bin").getCanonicalPath() + "/"), new URL("file://" + new File("../com.luckyrelease.depo.server/bin").getCanonicalPath() + "/") };
        handleInputParams(args, params);
        if (!params.isUseHardcode()) {
            for (int i = 0; i < params.getPathList().length; i++) {
                enhanceDir(params.getRootPath() + params.getPathList()[i].trim(), params.getPrefix(), params.getBundleId(), urls);
            }
        } else {
            enhanceDir("../com.luckyrelease.depo.server/bin/com/luckyrelease/depo/server/domain/", "com.luckyrelease.depo.server", "com.luckyrelease.depo.server", urls);
            enhanceDir("../com.luckyrelease.depo.server/bin/com/luckyrelease/depo/server/rightpanedatas/", "com.luckyrelease.depo.server", "com.luckyrelease.depo.server", urls);
            enhanceDir("../com.luckyrelease.depo.server/bin/com/luckyrelease/depo/server/filters/legacy/", "com.luckyrelease.depo.server", "com.luckyrelease.depo.server", urls);
            enhanceDir("../com.luckyrelease.depo.reports/bin/com/luckyrelease/depo/reports/rightpanedatas/", "com.luckyrelease.depo.reports", "com.luckyrelease.depo.reports", urls);
            enhanceDir("../com.luckyrelease.depo.reports/bin/com/luckyrelease/depo/reports/filters/", "com.luckyrelease.depo.reports", "com.luckyrelease.depo.reports", urls);
            enhanceDir("../com.luckyrelease.depo.reports/bin/com/luckyrelease/depo/server/domain/reports/", "com.luckyrelease.depo.reports", "com.luckyrelease.depo.reports", urls);
            enhanceDir("../com.luckyrelease.depo.journals/bin/com/luckyrelease/depo/journals/rightpanedatas/", "com.luckyrelease.depo.journals", "com.luckyrelease.depo.journals", urls);
            enhanceDir("../com.luckyrelease.depo.journals/bin/com/luckyrelease/depo/journals/filters/", "com.luckyrelease.depo.journals", "com.luckyrelease.depo.journals", urls);
            enhanceDir("../com.luckyrelease.depo.journals/bin/com/luckyrelease/depo/journals/domain/", "com.luckyrelease.depo.journals", "com.luckyrelease.depo.journals", urls);
            enhanceDir("../DocFlowerServer/bin/org/docflower/server/domain/", "DocFlowerServer", "org.docflower.server", urls);
            enhanceDir("../DocFlowerServer/bin/org/docflower/server/home/", "DocFlowerServer", "org.docflower.server", urls);
            enhanceDir("../DocFlowerServer/bin/org/docflower/server/filters/", "DocFlowerServer", "org.docflower.server", urls);
            enhanceDir("../DocFlowerServer/bin/org/docflower/server/jobs/", "DocFlowerServer", "org.docflower.server", urls);
        }
        System.out.println("Enhancement done in " + (new Date().getTime() - beginDate.getTime()) + " milliseconds");
    }

    private static void handleInputParams(String[] args, EnhancerParams params) {
        for (int i = 0; i < args.length; i++) {
            if (args[i].startsWith("-") || args[i].startsWith("--")) {
                if (args[i].contains("=")) {
                    String[] pair = args[i].split("=");
                    if ("-d".equals(pair[0]) || "--dir".equals(pair[0])) {
                        params.setRootPath(pair[1]);
                    }
                    if ("-f".equals(pair[0]) || "--propfile".equals(pair[0])) {
                        params.setPropFileLocation(pair[1]);
                    }
                    if ("-x".equals(pair[0]) || "--hardcode".equals(pair[0])) {
                        params.setUseHardcode(Boolean.parseBoolean(pair[1]));
                    }
                }
            }
        }
        if (params.getPropFileLocation() != null) {
            params.initFromPropFile();
        }
    }

    private static void enhanceDir(String rootDir, String prefix, String bundleId, URL[] classpath) {
        File f = new File(rootDir);
        String[] dirList = f.list();
        for (String string : dirList) {
            if (string.startsWith(".")) {
            } else {
                File newFile = new File(rootDir + string);
                if (newFile.isDirectory()) {
                    enhanceDir(rootDir + string + "/", prefix, bundleId, classpath);
                } else {
                    try {
                        enhanceFile(rootDir + string, prefix, bundleId, classpath);
                    } catch (ClassNotFoundException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    private static void enhanceFile(String fullClassName, String prefix, String bundleId, URL[] classpath) throws ClassNotFoundException, IOException {
        int idx = fullClassName.indexOf("/@dot/");
        if (idx < 0) {
            idx = fullClassName.indexOf("/bin/");
            if (idx < 0) {
                return;
            } else {
                idx += "/bin/".length();
            }
        } else {
            idx += "/@dot/".length();
        }
        String className = fullClassName.substring(idx).replace("/", ".");
        className = className.substring(0, className.lastIndexOf("."));
        URL url = new URL("file://" + new File(SerializableEnhancerUtils.getBinDir(fullClassName)).getCanonicalPath() + "/");
        List<URL> urlList = new ArrayList<URL>();
        URL jdoUrl = new URL("file://" + new File("../SchemaBuilder/lib/jdo2-api-2.3-ec.jar").getCanonicalPath());
        URL eclipseUrl = new URL("file://" + new File("/home/sl/dev/eclipse/plugins/org.eclipse.core.jobs_3.5.100.v20110404.jar").getCanonicalPath());
        URL eclipseCoreruntime = new URL("file://" + new File("/home/sl/dev/eclipse/plugins/org.eclipse.equinox.common_3.6.0.v20110523.jar").getCanonicalPath());
        urlList.add(jdoUrl);
        urlList.add(eclipseUrl);
        urlList.add(eclipseCoreruntime);
        urlList.add(url);
        for (URL urItem : classpath) {
            urlList.add(urItem);
        }
        URL[] urls = new URL[urlList.size()];
        urls = urlList.toArray(urls);
        URLClassLoader l1 = null;
        if (params.isUseHardcode()) {
            ClassLoader clloader = Thread.currentThread().getContextClassLoader();
            l1 = new URLClassLoader(urls, clloader);
        } else {
            l1 = new URLClassLoader(urls, null);
        }
        System.out.println("className: " + className);
        Class<?> cl = l1.loadClass(className);
        if (cl.isInterface()) {
            return;
        }
        String packageName = cl.getPackage().getName();
        if (isClassNeedToBeEnhanced(cl, packageName)) {
            boolean isSerializable = isSerializable(cl, packageName);
            System.out.println("Enhance class: " + className);
            InputStream is = new FileInputStream(new File(fullClassName));
            ClassReader cr = new ClassReader(is);
            ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_MAXS);
            ClassAdapter ca = new SerializableEnhancer(cw, l1, bundleId, isSerializable);
            cr.accept(ca, 0);
            FileOutputStream fos = new FileOutputStream(new File(fullClassName));
            fos.write(cw.toByteArray());
            fos.close();
        } else {
            System.out.println("Class " + className + " already enhanced...");
        }
    }

    private static boolean isClassNeedToBeEnhanced(Class<?> cl, String currentPackage) {
        boolean result = false;
        Class<?> superClass = cl.getSuperclass();
        if (!cl.isEnum() && !(cl.getPackage().getName().startsWith("javax.jdo.spi"))) {
            if (cl.getPackage().getName().equals(currentPackage) || ((superClass != null) && (superClass.getPackage().getName().equals(currentPackage) || superClass.getPackage().getName().equals("java.lang")))) {
                result = true;
            }
        }
        return result;
    }

    private static boolean isSerializable(Class<?> cl, String currentPackage) {
        boolean result = false;
        Class<?> superClass = cl.getSuperclass();
        if (superClass != null && !superClass.getPackage().getName().equals("java.lang")) {
        } else {
            result = true;
        }
        return result;
    }

    public EnhancerParams getParams() {
        return params;
    }
}
