package com.abstratt.graphviz;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.apache.commons.io.IOUtils;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.ImageLoader;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Display;
import com.abstratt.graphviz.ProcessController.TimeOutException;
import com.abstratt.pluginutils.LogUtils;

public class GraphViz {

    private static final String DOT_EXTENSION = ".dot";

    private static final String TMP_FILE_PREFIX = "graphviz";

    public static void generate(final InputStream input, String format, Point dimension, IPath outputLocation) throws CoreException {
        MultiStatus status = new MultiStatus(GraphVizActivator.ID, 0, "Errors occurred while running Graphviz", null);
        File dotInput = null, dotOutput = outputLocation.toFile();
        ByteArrayOutputStream dotContents = new ByteArrayOutputStream();
        try {
            dotInput = File.createTempFile(TMP_FILE_PREFIX, DOT_EXTENSION);
            FileOutputStream tmpDotOutputStream = null;
            try {
                IOUtils.copy(input, dotContents);
                tmpDotOutputStream = new FileOutputStream(dotInput);
                IOUtils.copy(new ByteArrayInputStream(dotContents.toByteArray()), tmpDotOutputStream);
            } finally {
                IOUtils.closeQuietly(tmpDotOutputStream);
            }
            IStatus result = runDot(format, dimension, dotInput, dotOutput);
            if (dotOutput.isFile()) {
                if (!result.isOK() && Platform.inDebugMode()) LogUtils.log(status);
                return;
            }
        } catch (IOException e) {
            status.add(new Status(IStatus.ERROR, GraphVizActivator.ID, "", e));
        } finally {
            dotInput.delete();
            IOUtils.closeQuietly(input);
        }
        throw new CoreException(status);
    }

    /**
	 * Higher-level API for launching a GraphViz transformation.
	 * 
	 * @return the resulting image, never <code>null</code>
	 * @throws CoreException
	 *             if any error occurs
	 */
    public static Image load(final InputStream input, String format, Point dimension) throws CoreException {
        MultiStatus status = new MultiStatus(GraphVizActivator.ID, 0, "Errors occurred while running Graphviz", null);
        File dotInput = null, dotOutput = null;
        ByteArrayOutputStream dotContents = new ByteArrayOutputStream();
        try {
            dotInput = File.createTempFile(TMP_FILE_PREFIX, DOT_EXTENSION);
            dotOutput = File.createTempFile(TMP_FILE_PREFIX, "." + format);
            dotOutput.delete();
            FileOutputStream tmpDotOutputStream = null;
            try {
                IOUtils.copy(input, dotContents);
                tmpDotOutputStream = new FileOutputStream(dotInput);
                IOUtils.copy(new ByteArrayInputStream(dotContents.toByteArray()), tmpDotOutputStream);
            } finally {
                IOUtils.closeQuietly(tmpDotOutputStream);
            }
            IStatus result = runDot(format, dimension, dotInput, dotOutput);
            status.add(result);
            status.add(logInput(dotContents));
            if (dotOutput.isFile()) {
                if (!result.isOK() && Platform.inDebugMode()) LogUtils.log(status);
                ImageLoader loader = new ImageLoader();
                ImageData[] imageData = loader.load(dotOutput.getAbsolutePath());
                return new Image(Display.getDefault(), imageData[0]);
            }
        } catch (SWTException e) {
            status.add(new Status(IStatus.ERROR, GraphVizActivator.ID, "", e));
        } catch (IOException e) {
            status.add(new Status(IStatus.ERROR, GraphVizActivator.ID, "", e));
        } finally {
            dotInput.delete();
            dotOutput.delete();
            IOUtils.closeQuietly(input);
        }
        throw new CoreException(status);
    }

    public static IStatus runDot(String format, Point dimension, File dotInput, File dotOutput) {
        double dpi = 96;
        double widthInInches = dimension.x / dpi;
        double heightInInches = dimension.y / dpi;
        List<String> cmd = new ArrayList<String>();
        cmd.add("-o" + dotOutput.getAbsolutePath());
        cmd.add("-T" + format);
        if (widthInInches > 0 && heightInInches > 0) cmd.add("-Gsize=" + widthInInches + ',' + heightInInches);
        cmd.add(dotInput.getAbsolutePath());
        return runDot(cmd.toArray(new String[cmd.size()]));
    }

    private static IStatus logInput(ByteArrayOutputStream dotContents) {
        return new Status(IStatus.INFO, GraphVizActivator.ID, "dot input was:\n" + dotContents, null);
    }

    /**
	 * Bare bones API for launching dot. Command line options are passed to
	 * Graphviz as specified in the options parameter. The location for dot is
	 * obtained from the user preferences.
	 * 
	 * @param options
	 *            command line options for dot
	 * @return a non-zero integer if errors happened
	 * @throws IOException
	 */
    public static IStatus runDot(String... options) {
        IPath dotFullPath = GraphVizActivator.getInstance().getDotLocation();
        if (dotFullPath == null || dotFullPath.isEmpty()) return new Status(IStatus.ERROR, GraphVizActivator.ID, "dot.exe/dot not found in PATH. Please install it from graphviz.org, update the PATH or specify the absolute path in the preferences.");
        if (!dotFullPath.toFile().isFile()) return new Status(IStatus.ERROR, GraphVizActivator.ID, "Could not find Graphviz dot at \"" + dotFullPath + "\"");
        List<String> cmd = new ArrayList<String>();
        cmd.add(dotFullPath.toOSString());
        String commandLineExtension = GraphVizActivator.getInstance().getCommandLineExtension();
        if (commandLineExtension != null) {
            String[] tokens = commandLineExtension.split(" ");
            cmd.addAll(Arrays.asList(tokens));
        }
        cmd.addAll(Arrays.asList(options));
        ByteArrayOutputStream errorOutput = new ByteArrayOutputStream();
        try {
            final ProcessController controller = new ProcessController(60000, cmd.toArray(new String[cmd.size()]), null, dotFullPath.removeLastSegments(1).toFile());
            controller.forwardErrorOutput(errorOutput);
            controller.forwardOutput(System.out);
            controller.forwardInput(System.in);
            int exitCode = controller.execute();
            if (exitCode != 0) return new Status(IStatus.WARNING, GraphVizActivator.ID, "Graphviz exit code: " + exitCode + "." + createContentMessage(errorOutput));
            if (errorOutput.size() > 0) return new Status(IStatus.WARNING, GraphVizActivator.ID, createContentMessage(errorOutput));
            return Status.OK_STATUS;
        } catch (TimeOutException e) {
            return new Status(IStatus.ERROR, GraphVizActivator.ID, "Graphviz process did not finish in a timely way." + createContentMessage(errorOutput));
        } catch (InterruptedException e) {
            return new Status(IStatus.ERROR, GraphVizActivator.ID, "Unexpected exception executing Graphviz." + createContentMessage(errorOutput), e);
        } catch (IOException e) {
            return new Status(IStatus.ERROR, GraphVizActivator.ID, "Unexpected exception executing Graphviz." + createContentMessage(errorOutput), e);
        }
    }

    private static String createContentMessage(ByteArrayOutputStream errorOutput) {
        if (errorOutput.size() == 0) return "";
        return " dot produced the following error output: \n" + errorOutput;
    }
}
