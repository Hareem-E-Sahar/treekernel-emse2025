package com.xenoage.build;

import static com.xenoage.build.entities.filesets.Excludes.excludes;
import static com.xenoage.build.util.Util.copyFile;
import static com.xenoage.build.util.Util.deleteDirectory;
import static com.xenoage.build.util.Util.getTempFolder;
import java.io.File;
import java.io.PrintStream;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import org.apache.tools.ant.taskdefs.BZip2;
import org.apache.tools.ant.taskdefs.Chmod;
import org.apache.tools.ant.taskdefs.ExecTask;
import org.apache.tools.ant.taskdefs.Jar;
import org.apache.tools.ant.taskdefs.Java;
import org.apache.tools.ant.taskdefs.Javac;
import org.apache.tools.ant.taskdefs.Tar;
import org.apache.tools.ant.taskdefs.Zip;
import org.apache.tools.ant.taskdefs.Tar.TarFileSet;
import org.apache.tools.ant.types.FileSet;
import org.apache.tools.ant.types.Path;
import com.xenoage.build.BuildConfig.BuildType;
import com.xenoage.build.entities.CodeItem;
import com.xenoage.build.entities.Dependencies;
import com.xenoage.build.entities.Library;
import com.xenoage.build.entities.Project;
import com.xenoage.build.entities.filesets.Excludes;
import com.xenoage.build.entities.filesets.Includes;
import com.xenoage.build.util.Util;

/**
 * Build engine.
 * 
 * @author Andreas Wenger
 */
public class Build {

    private static String[] exclude = { ".gitignore", ".log" };

    /**
	 * Starts the build.
	 * @param project  build the given project, or null to build all projects
	 */
    public static void build(BuildConfig buildConfig, BuildType buildType, String project) {
        PrintStream out = System.out;
        out.println("Xenoage Build: " + buildConfig.getName());
        boolean build = false;
        boolean jar = false;
        boolean dist = false;
        switch(buildType) {
            case Build:
                build = true;
                break;
            case Jar:
                build = jar = true;
                break;
            case Dist:
                build = jar = dist = true;
                break;
        }
        HashMap<String, Library> libraries = new HashMap<String, Library>();
        HashMap<String, Project> projects = new HashMap<String, Project>();
        ArrayList<Project> projectsSorted = new ArrayList<Project>();
        Class<?> c = buildConfig.getClass();
        for (Field field : c.getDeclaredFields()) {
            try {
                if (field.getType() == Library.class) {
                    Library lib = (Library) field.get(buildConfig);
                    libraries.put(lib.getName(), lib);
                } else if (field.getType() == Project.class) {
                    Project pro = (Project) field.get(buildConfig);
                    if (new File(pro.getName()).exists()) {
                        projects.put(pro.getName(), pro);
                        projectsSorted.add(pro);
                    } else {
                        if (!pro.isOptional()) throw new RuntimeException("Project " + pro.getName() + " is missing!");
                    }
                }
            } catch (IllegalAccessException ex) {
                throw new RuntimeException(ex);
            }
        }
        if (project != null && projects.get(project) == null) {
            throw new IllegalArgumentException("Unknown project: " + project);
        }
        ArrayList<Project> buildProjects = new ArrayList<Project>();
        if (project == null) {
            buildProjects.addAll(projectsSorted);
        } else {
            LinkedList<Project> queue = new LinkedList<Project>();
            queue.add(projects.get(project));
            while (queue.size() > 0) {
                Project pro = queue.removeFirst();
                if (!buildProjects.contains(pro)) {
                    buildProjects.add(0, pro);
                    for (CodeItem dep : pro.getDependencies().getCodeItems()) {
                        if (dep instanceof Project) queue.add((Project) dep);
                    }
                }
            }
        }
        out.println("Projects to build:");
        for (Project p : buildProjects) {
            System.out.println("  " + p.getName());
        }
        LinkedList<Project> queue = new LinkedList<Project>();
        queue.addAll(buildProjects);
        while (queue.size() > 0) {
            Project pro = queue.removeFirst();
            Dependencies proDep = pro.getAllDependencies();
            int sizeBefore = proDep.size();
            for (int i = 0; i < proDep.getCodeItems().size(); i++) proDep.addAll(proDep.getCodeItems().get(i).getDependencies());
            if (proDep.size() > sizeBefore) queue.add(pro);
        }
        out.println("Detected dependencies:");
        for (Project p : buildProjects) {
            out.print("  " + p.getName() + ": ");
            for (CodeItem item : p.getAllDependencies().getCodeItems()) System.out.print(item.getName() + " ");
            System.out.println();
        }
        out.println("Cleaning projects:");
        deleteDirectory(new File("dist"));
        for (Project pro : buildProjects) {
            String proName = pro.getName();
            out.print("  Cleaning " + proName + "...");
            deleteDir(pro, "bin");
            deleteDir(pro, "bin-test");
            deleteDir(pro, "dist");
            out.println(" Done.");
        }
        if (build) {
            out.println("Building projects:");
            for (Project pro : buildProjects) {
                String proName = pro.getName();
                out.print("  Building " + proName + "...");
                buildProject(pro, "src");
                out.println(" Done.");
            }
        }
        if (jar) {
            out.println("Jarring projects:");
            for (Project pro : buildProjects) {
                String proName = pro.getName();
                out.print("  Jarring " + proName + "...");
                jarProject(pro);
                out.println(" Done.");
            }
        }
        if (dist) {
            out.println("Create shared dist packages for projects:");
            for (Project pro : buildProjects) {
                if (pro.isDist()) {
                    String proName = pro.getName();
                    out.print("  Building dist for " + proName + "...");
                    distShareProject(pro, buildConfig);
                    out.println(" Done.");
                }
            }
            out.println("Create OS-specific shared dist packages for projects:");
            for (Project pro : buildProjects) {
                new File("dist/os").mkdirs();
                new File("dist/packages").mkdirs();
                if (pro.isDist()) {
                    String proName = pro.getName();
                    out.print("  Building dist for " + proName + "...");
                    for (OS os : OS.values()) {
                        out.print(" " + os + "...");
                        distOSProject(pro, os, buildConfig);
                    }
                    out.println(" Done.");
                }
            }
        }
        buildConfig.finalStep(out);
        out.println("Done.");
    }

    private static void deleteDir(Project project, String dir) {
        deleteDirectory(new File(project.getName() + "/" + dir));
    }

    private static void buildProject(Project pro, String sourceDir) {
        org.apache.tools.ant.Project antProj = new org.apache.tools.ant.Project();
        String proName = pro.getName();
        new File(proName + "/bin").mkdir();
        Javac javac = new Javac();
        javac.setProject(antProj);
        javac.setDebug(true);
        javac.setDebugLevel("source,lines,vars");
        javac.setDestdir(new File(proName + "/bin"));
        javac.setSource("1.6");
        javac.setTarget("1.6");
        javac.setEncoding("UTF-8");
        Path pathSrc = new Path(antProj, proName + "/" + sourceDir);
        for (File dir : new File(proName).listFiles()) {
            if (dir.isDirectory() && dir.getName().startsWith("srclib-")) pathSrc.add(new Path(antProj, dir.getAbsolutePath()));
        }
        javac.setSrcdir(pathSrc);
        Path classpath = new Path(antProj);
        addDepToPath(antProj, classpath, pro.getAllDependencies());
        javac.setClasspath(classpath);
        javac.execute();
    }

    private static void jarProject(Project pro) {
        org.apache.tools.ant.Project antProj = new org.apache.tools.ant.Project();
        String proName = pro.getName();
        new File(proName + "/dist").mkdir();
        Jar jar = new Jar();
        jar.setProject(antProj);
        FileSet f = new FileSet();
        f.setDir(new File(proName + "/bin"));
        jar.addFileset(f);
        f = new FileSet();
        f.setFile(new File(proName + "/license.txt"));
        jar.addFileset(f);
        jar.setDestFile(new File(proName + "/dist/" + proName + ".jar"));
        jar.execute();
    }

    private static void distShareProject(Project pro, BuildConfig buildConfig) {
        String proName = pro.getName();
        String dirShared = "dist/share/" + proName;
        new File(dirShared).mkdirs();
        String dirLib = dirShared + "/lib";
        new File(dirLib).mkdirs();
        for (CodeItem item : pro.getAllDependencies().getCodeItems()) {
            if (item instanceof Library) {
                Library lib = (Library) item;
                for (String f : lib.getJarFiles()) copyFile("lib/" + f, dirLib + "/" + f);
                for (String f : lib.getOtherFiles()) copyFile("lib/" + f, dirLib + "/" + f);
            } else if (item instanceof Project) {
                Project p = (Project) item;
                String pn = p.getName();
                copyFile(pn + "/dist/" + pn + ".jar", dirLib + "/" + pn + ".jar");
            }
        }
        copyFile(proName + "/dist/" + proName + ".jar", dirLib + "/" + proName + ".jar");
        for (String f : pro.getOtherFiles()) copyFileOrDir(proName + "/" + f, dirShared + "/" + f);
        buildConfig.distCustomStep(pro);
    }

    private static void distOSProject(Project pro, OS os, BuildConfig buildConfig) {
        org.apache.tools.ant.Project antProj = new org.apache.tools.ant.Project();
        String proName = pro.getName();
        String osDir;
        if (os == OS.MacOSX) osDir = "dist/os/" + os.id + "/" + proName + "/" + buildConfig.getPrefix() + proName + ".app/Contents/Resources/Java"; else osDir = "dist/os/" + os.id + "/" + proName;
        new File(osDir).mkdirs();
        copyFileOrDir("dist/share/" + proName, osDir);
        copyFileOrDir(proName + "/launcher/dist/" + os.id, osDir);
        File nativeLibsDir = new File("lib/native/" + os.id);
        if (nativeLibsDir.exists() && nativeLibsDir.isDirectory()) {
            for (CodeItem item : pro.getAllDependencies().getCodeItems()) {
                if (item instanceof Library) {
                    Library lib = (Library) item;
                    File dir = new File(nativeLibsDir, lib.getName());
                    if (dir.exists() && dir.isDirectory()) {
                        String projectNativeLibDir = osDir + "/lib/native/" + os.id + "/" + lib.getName();
                        if (!new File(projectNativeLibDir).exists()) new File(projectNativeLibDir).mkdirs();
                        for (File file : dir.listFiles()) {
                            copyFile(file.getAbsolutePath(), projectNativeLibDir + "/" + file.getName());
                        }
                    }
                }
            }
        }
        String packageFileNameWithoutExtension = "dist/packages/" + buildConfig.getPrefix() + proName + "-" + buildConfig.getVersion() + "-" + os.id;
        if (os.isWindows()) {
            Zip zip = new Zip();
            zip.setProject(antProj);
            zip.setDestFile(new File(packageFileNameWithoutExtension + ".zip"));
            zip.setBasedir(new File(osDir));
            zip.execute();
        } else {
            String executableFiles;
            if (os.isMacOSX()) executableFiles = "**/JavaApplicationStub"; else executableFiles = "**/*.sh";
            createBzip2(osDir, executableFiles, packageFileNameWithoutExtension + ".tar.bz2", excludes());
        }
    }

    public static void createBzip2(String dir, String executableFilter, String destFile, Excludes excludes) {
        org.apache.tools.ant.Project antProj = new org.apache.tools.ant.Project();
        Chmod chmod = new Chmod();
        chmod.setProject(antProj);
        chmod.setPerm("+x");
        chmod.setDir(new File(dir));
        chmod.setIncludes(executableFilter);
        chmod.execute();
        String tarFile = getTempFolder() + "/createBzip2.tar";
        Tar tar = new Tar();
        tar.setProject(antProj);
        tar.setDestFile(new File(tarFile));
        TarFileSet tfs = tar.createTarFileSet();
        tfs.setDir(new File(dir));
        tfs.setMode("755");
        tfs.createInclude().setName(executableFilter);
        for (String s : excludes) tfs.createExclude().setName(s);
        tfs = tar.createTarFileSet();
        tfs.setDir(new File(dir));
        tfs.createExclude().setName(executableFilter);
        for (String s : excludes) tfs.createExclude().setName(s);
        tar.execute();
        BZip2 bzip2 = new BZip2();
        bzip2.setProject(antProj);
        bzip2.setDestfile(new File(destFile));
        bzip2.setSrc(new File(tarFile));
        bzip2.execute();
        new File(tarFile).delete();
    }

    private static void addDepToPath(org.apache.tools.ant.Project proj, Path path, Dependencies dep) {
        for (CodeItem item : dep.getCodeItems()) {
            if (item instanceof Project) path.add(new Path(proj, item.getName() + "/bin")); else if (item instanceof Library) {
                Library lib = (Library) item;
                for (String jarFile : lib.getJarFiles()) path.add(new Path(proj, "lib/" + jarFile));
            }
        }
    }

    public static void copyFileOrDir(String in, String out) {
        for (String ex : exclude) {
            if (in.endsWith(ex)) return;
        }
        File fin = new File(in);
        if (fin.isFile()) {
            Util.copyFile(in, out);
        } else if (fin.isDirectory()) {
            new File(out).mkdirs();
            for (File file : fin.listFiles()) {
                copyFileOrDir(in + "/" + file.getName(), out + "/" + file.getName());
            }
        }
    }

    public static void createJar(String filename, String baseDir, boolean withTOC, Includes includes, Excludes excludes) {
        org.apache.tools.ant.Project antProj = new org.apache.tools.ant.Project();
        Jar jar = new Jar();
        jar.setProject(antProj);
        jar.setDestFile(new File(filename));
        FileSet fs = new FileSet();
        fs.setDir(new File(baseDir));
        for (String s : includes) fs.createInclude().setName(s);
        for (String s : excludes) fs.createExclude().setName(s);
        for (String s : exclude) fs.createExclude().setName("**/*" + s);
        jar.addFileset(fs);
        jar.execute();
        if (withTOC) {
            File tempFile = new File(Util.getTempFolder() + "/.filelist");
            ExecTask exec = new ExecTask();
            exec.setProject(antProj);
            exec.setExecutable("jar");
            exec.setOutput(tempFile);
            exec.createArg().setLine("tf " + filename);
            exec.execute();
            jar = new Jar();
            jar.setProject(antProj);
            jar.setDestFile(new File(filename));
            jar.setUpdate(true);
            fs = new FileSet();
            fs.setFile(tempFile);
            jar.addFileset(fs);
            jar.execute();
            tempFile.delete();
        }
    }

    public static void runJava(Project pro, String classname, String... args) {
        org.apache.tools.ant.Project antProj = new org.apache.tools.ant.Project();
        Java java = new Java();
        java.setProject(antProj);
        java.setDir(new File(pro.getName()));
        java.setClassname(classname);
        java.setFork(true);
        java.setFailonerror(true);
        Path classpath = new Path(antProj);
        addDepToPath(antProj, classpath, pro.getAllDependencies());
        classpath.add(new Path(antProj, pro.getName() + "/bin"));
        java.setClasspath(classpath);
        for (String arg : args) java.createArg().setValue(arg);
        java.execute();
    }
}
