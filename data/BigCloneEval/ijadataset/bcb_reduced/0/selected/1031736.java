package com.c4j.packer;

import static com.c4j.filetools.IFileTools.EXTERNAL;
import static com.c4j.filetools.IFileTools.RUNTIME;
import static com.c4j.filetools.IFileTools.RUNTIME_BINARY;
import static com.c4j.filetools.IFileTools.RUNTIME_JAR;
import static java.lang.String.format;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipOutputStream;
import com.c4j.IFragmentReference;
import com.c4j.assembly.IAssembly;
import com.c4j.assembly.ILibrary;
import com.c4j.assembly.ILibraryReference;
import com.c4j.assembly.IMain;
import com.c4j.component.IComponent;
import com.c4j.composition.IComposition;
import com.c4j.sre.C4JException;
import com.c4j.sre.C4JRuntimeException;
import com.c4j.workspace.IFragment;
import com.c4j.workspace.IWorkspace;

final class PackerImpl extends PackerBase implements IPacker {

    /**
     * Constructs a new instance of the appropriate component with the given name.
     *
     * @param instanceName
     *         the name of the instance.
     */
    public PackerImpl(final String instanceName) {
        super(instanceName);
    }

    @Override
    protected IPacker provide_packer() {
        return this;
    }

    private static Manifest createManifest(final String mainClass, final Collection<File> absoluteFolders, final Collection<File> absoluteJARs, final Collection<String> relativeFolders, final Collection<String> relativeJARs) throws C4JException {
        final Manifest manifest = new Manifest();
        manifest.getMainAttributes().putValue("Manifest-Version", "1.0");
        if (mainClass != null) manifest.getMainAttributes().putValue("Main-Class", mainClass);
        final StringBuilder classpathstr = new StringBuilder("");
        if (absoluteFolders != null) for (final File file : absoluteFolders) {
            if (classpathstr.length() != 0) classpathstr.append(" ");
            try {
                classpathstr.append(file.getAbsoluteFile().toURI().toURL().toString());
            } catch (final MalformedURLException e) {
                throw new C4JException("Could not create manifest.", e);
            }
        }
        if (absoluteJARs != null) for (final File file : absoluteJARs) {
            if (classpathstr.length() != 0) classpathstr.append(" ");
            try {
                classpathstr.append(file.getAbsoluteFile().toURI().toURL().toString());
            } catch (final MalformedURLException e) {
                throw new C4JException("Could not create manifest.", e);
            }
        }
        if (relativeFolders != null) for (final String relativeFolder : relativeFolders) {
            if (classpathstr.length() != 0) classpathstr.append(" ");
            classpathstr.append(relativeFolder + "/");
        }
        if (relativeJARs != null) for (final String relativeJar : relativeJARs) {
            if (classpathstr.length() != 0) classpathstr.append(" ");
            classpathstr.append(relativeJar);
        }
        if (classpathstr.length() != 0) manifest.getMainAttributes().putValue("Class-Path", classpathstr.toString());
        return manifest;
    }

    private void packFile(final File file, final ZipOutputStream out, final String name, final FileFilter filter) throws IOException {
        if (filter != null && !filter.accept(file)) return;
        if (file.isDirectory()) {
            final File[] list = file.listFiles();
            if (list == null) return;
            for (final File element : list) if (name == null) packFile(element, out, file.getName(), filter); else packFile(element, out, name + "/" + file.getName(), filter);
        } else {
            ZipEntry entry = null;
            if (name == null) entry = new ZipEntry(file.getName()); else entry = new ZipEntry(name + "/" + file.getName());
            try {
                out.putNextEntry(entry);
            } catch (final ZipException e) {
                throw new C4JRuntimeException(format("Could not pack file ‘%s’.", file.getPath()), e);
            }
            InputStream fileIn = null;
            try {
                fileIn = new FileInputStream(file);
                use_filetools().copyStream2Stream(fileIn, out);
            } finally {
                if (fileIn != null) fileIn.close();
            }
            out.closeEntry();
        }
    }

    private void pack(final File sourceFolder, final FileFilter filter, final ZipOutputStream out) throws IOException {
        final File[] list = sourceFolder.listFiles(filter);
        if (list == null) return;
        for (final File element : list) packFile(element, out, null, filter);
    }

    private void pack(final Manifest manifest, final Collection<File> sourceFolders, final FileFilter filter, final File destination) throws C4JException {
        OutputStream fileOut = null;
        JarOutputStream jarOut = null;
        try {
            fileOut = new FileOutputStream(destination);
            if (manifest != null) jarOut = new JarOutputStream(fileOut, manifest); else jarOut = new JarOutputStream(fileOut);
            for (final File sourceFolder : sourceFolders) pack(sourceFolder, filter, jarOut);
        } catch (final IOException e) {
            throw new C4JException(format("Could not pack ‘%s’.", destination.getPath()), e);
        } finally {
            if (jarOut != null) try {
                jarOut.close();
            } catch (final IOException e) {
                if (fileOut != null) try {
                    fileOut.close();
                } catch (final IOException e1) {
                    throw new C4JException(format("Could not close stream for ‘%s’.", destination.getPath()), e);
                }
            }
        }
    }

    private void pack(final ILibrary library, final Map<File, String> fileNaming, final boolean develop) throws C4JException {
        final File outputFolder = use_folders().getAssemblyOutputFolder(library.getRoot());
        final Collection<String> relativeJARs = new HashSet<String>();
        final Collection<File> absoluteFolders = new HashSet<File>();
        final Collection<File> absoluteJars = new HashSet<File>();
        relativeJARs.add(RUNTIME_JAR);
        for (final ILibraryReference libRef : library.getLibraryReferences()) relativeJARs.add(libRef.getLibrary().getName() + ".jar");
        final File destination = new File(outputFolder, library.getName() + ".jar");
        final Collection<File> sourceFolders = new HashSet<File>();
        for (final IFragmentReference reference : library.getFragmentReferences()) {
            if (!reference.getFragmentReference().isResolved()) continue;
            final IFragment fragment = reference.getFragment();
            if (fragment instanceof IComponent) {
                final IComponent component = (IComponent) fragment;
                if (develop) {
                    absoluteFolders.add(use_folders().getInterfaceProjectBinaryFolder(component));
                    absoluteFolders.add(use_folders().getImplementationProjectBinaryFolder(component));
                } else {
                    sourceFolders.add(use_folders().getInterfaceProjectBinaryFolder(component));
                    sourceFolders.add(use_folders().getImplementationProjectBinaryFolder(component));
                }
            } else if (reference.getFragment() instanceof IComposition) {
                final IComposition composition = (IComposition) fragment;
                if (develop) absoluteFolders.add(use_folders().getCompositionBinaryFolder(composition)); else sourceFolders.add(use_folders().getCompositionBinaryFolder(composition));
            }
        }
        for (final String name : library.usedExternalJars(fileNaming)) relativeJARs.add(EXTERNAL + "/" + name);
        final IMain application = library.getMain();
        if (application != null) if (develop) absoluteFolders.add(use_folders().getApplicationBinaryFolder(application)); else sourceFolders.add(use_folders().getApplicationBinaryFolder(application));
        final String mainClass = application != null ? library.getMain().getType().getFullTypeString() : null;
        final Manifest manifest = createManifest(mainClass, absoluteFolders, absoluteJars, null, relativeJARs);
        pack(manifest, sourceFolders, null, destination);
        if (library.getMain() != null) destination.setExecutable(true, false);
    }

    private File getSREJAR(final IWorkspace workspace) {
        return workspace.hasOwnRuntime() ? new File(new File(workspace.getFolder(), RUNTIME), RUNTIME_JAR) : use_filetools().getSRELibrary();
    }

    @Override
    public void packAll(final IWorkspace workspace, final boolean develop) throws C4JException {
        packSRE(workspace, develop);
        for (final IAssembly assembly : workspace.getAllAssemblies()) packAssembly(assembly, develop);
    }

    @Override
    public void packSRE(final IWorkspace workspace, final boolean develop) throws C4JException {
        if (workspace.hasOwnRuntime()) {
            final File sreJAR = getSREJAR(workspace);
            final Collection<File> sourceFolders = new HashSet<File>();
            final Collection<File> absoluteFolders = new HashSet<File>();
            if (develop) absoluteFolders.add(new File(new File(workspace.getFolder(), RUNTIME), RUNTIME_BINARY)); else sourceFolders.add(new File(new File(workspace.getFolder(), RUNTIME), RUNTIME_BINARY));
            final Manifest manifest = createManifest(null, absoluteFolders, null, null, null);
            pack(manifest, sourceFolders, null, sreJAR);
        }
    }

    @Override
    public void packAssembly(final IAssembly assembly, final boolean develop) throws C4JException {
        final IWorkspace workspace = assembly.getWorkspace();
        final File sreJAR = getSREJAR(workspace);
        final File outputFolder = use_folders().getAssemblyOutputFolder(assembly);
        if (!outputFolder.exists() && !outputFolder.mkdirs()) throw new C4JException(format("Could not generate folder ‘%s’.", outputFolder.getPath()));
        use_filetools().copyFile2File(sreJAR, new File(outputFolder, RUNTIME_JAR), false);
        final Map<File, String> fileNaming = assembly.getExternalJarFiles();
        if (!fileNaming.isEmpty()) {
            final File externalLibsFolder = new File(outputFolder, EXTERNAL);
            if (!externalLibsFolder.exists() && !externalLibsFolder.mkdirs()) throw new C4JRuntimeException(format("Could not generate folder ‘%s’.", externalLibsFolder.getPath()));
            for (final File file : fileNaming.keySet()) use_filetools().copyFile2File(file, new File(externalLibsFolder, fileNaming.get(file)), true);
        }
        for (final ILibrary library : assembly.getLibraries()) pack(library, fileNaming, develop);
    }
}
