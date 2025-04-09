package net.sourceforge.ondex.cytoscape.io;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.text.ParseException;
import java.util.Collection;
import java.util.Enumeration;
import java.util.TreeSet;
import java.util.zip.GZIPInputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileFilter;
import net.sourceforge.ondex.core.ONDEXGraph;
import net.sourceforge.ondex.core.memory.MemoryONDEXGraph;
import net.sourceforge.ondex.cytoscape.OndexPlugin;
import net.sourceforge.ondex.cytoscape.mapping.EdgeMappingDescriptor;
import net.sourceforge.ondex.cytoscape.mapping.MalformedPathException;
import net.sourceforge.ondex.cytoscape.task.FileTask;
import net.sourceforge.ondex.cytoscape.task.OndexTaskConfig;
import net.sourceforge.ondex.exception.type.InconsistencyException;
import net.sourceforge.ondex.exception.type.ParsingFailedException;
import net.sourceforge.ondex.parser.oxl.Parser;
import cytoscape.Cytoscape;
import cytoscape.task.TaskMonitor;
import cytoscape.task.util.TaskManager;
import java.util.ArrayList;
import java.util.List;

/**
 * Handles file input and output for the plugin
 * 
 * @author jweile
 *
 */
public class IOHandler {

    /**
	 * singleton
	 */
    private static IOHandler instance;

    /**
	 * singleton method
	 * @return
	 */
    public static IOHandler getInstance() {
        if (instance == null) instance = new IOHandler();
        return instance;
    }

    private JFileChooser fc;

    public JFileChooser getFileChooser() {
        if (fc == null) {
            fc = new JFileChooser();
            fc.setFileFilter(new FileFilter() {

                @Override
                public boolean accept(File f) {
                    return f.isDirectory() || f.getName().endsWith(".xml.gz") || f.getName().endsWith(".xml") || f.getName().endsWith(".ov1");
                }

                @Override
                public String getDescription() {
                    return "Ondex files";
                }
            });
        }
        return fc;
    }

    /**
	 * saves the current ondexgraph + the given mapping descriptors to a file.
	 * @param file
	 * @param descriptors
	 * @throws Exception
	 */
    public void save(final File file, final Collection<EdgeMappingDescriptor> descriptors) throws Exception {
        FileTask task = new FileTask("Save file") {

            @Override
            public void processFile() {
                try {
                    saveInternal(file, descriptors, monitor);
                } catch (Exception e) {
                    exception = e;
                }
            }
        };
        TaskManager.executeTask(task, OndexTaskConfig.getInstance());
        if (task.getException() != null) {
            throw task.getException();
        }
    }

    /**
	 * saves the current ondexgraph + the given mapping descriptors to a file.
	 * @param file
	 * @param descriptors
	 * @return
	 */
    private void saveInternal(File file, Collection<EdgeMappingDescriptor> descriptors, TaskMonitor monitor) throws IOException {
        if (!file.getName().endsWith(".ov1")) {
            file = new File(file.getAbsolutePath() + ".ov1");
        }
        File graphFile = OndexPlugin.getInstance().getOndexGraphFile();
        File mappingFile = new File(System.getProperty("java.io.tmpdir") + File.separator + "ondex_mapping.tsv");
        monitor.setStatus("saving mapping descriptors...");
        monitor.setPercentCompleted(10);
        saveMapping(mappingFile, descriptors);
        File[] inFiles = new File[] { graphFile, mappingFile };
        ZipOutputStream out = new ZipOutputStream(new FileOutputStream(file));
        monitor.setStatus("compressing...");
        for (File inFile : inFiles) {
            FileInputStream in = new FileInputStream(inFile);
            out.putNextEntry(new ZipEntry(inFile.getName()));
            int len;
            byte[] buf = new byte[1024];
            while ((len = in.read(buf)) > 0) {
                out.write(buf, 0, len);
            }
            in.close();
            out.closeEntry();
        }
        out.close();
        monitor.setPercentCompleted(95);
    }

    /**
	 * saves the mappings
	 * @param file
	 * @param es
	 * @throws IOException
	 */
    private void saveMapping(File file, Collection<EdgeMappingDescriptor> es) throws IOException {
        BufferedWriter w = new BufferedWriter(new FileWriter(file));
        for (EdgeMappingDescriptor e : es) {
            w.write(e.getName() + "\t" + e.getPathString() + "\n");
        }
        w.flush();
        w.close();
    }

    /**
	 * load a given oxl or ov1 file
	 * @param file
	 * @return
	 * @throws Exception
	 */
    @SuppressWarnings("unchecked")
    public Collection<EdgeMappingDescriptor> load(final File file) throws Exception {
        FileTask task = new FileTask("Load file") {

            @Override
            public void processFile() {
                try {
                    output = loadInternal(file, monitor);
                } catch (Exception e) {
                    exception = e;
                }
            }
        };
        TaskManager.executeTask(task, OndexTaskConfig.getInstance());
        if (task.getException() != null) {
            throw task.getException();
        }
        if (task.getOutput() != null && task.getOutput() instanceof Collection) {
            return (Collection<EdgeMappingDescriptor>) task.getOutput();
        }
        return null;
    }

    /**
	 * loads the given file. The file can be either one of the two following types:
	 * oxl file (compressed or uncompressed)
	 * om1 file, containing an oxl file and a mapping file.
	 * 
	 * @param file the file to load.
	 * @return a file object of the extracted mapping descriptor file, if one exists. Otherwise <b>null</b>.
	 * @throws IOException
	 * @throws MalformedPathException 
	 * @throws ParseException 
	 */
    private Collection<EdgeMappingDescriptor> loadInternal(File file, TaskMonitor monitor) throws IOException, ParseException, MalformedPathException {
        InputStream graphStream = null, mappingStream = null;
        long oxlSize = 0L;
        boolean gzip = false;
        if (file.getName().endsWith(".ov1")) {
            ZipFile zipfile = new ZipFile(file);
            Enumeration<? extends ZipEntry> entries = zipfile.entries();
            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();
                if (entry.getName().endsWith(".tsv")) {
                    mappingStream = zipfile.getInputStream(entry);
                } else if (entry.getName().contains(".xml")) {
                    monitor.setStatus("decompressing...");
                    File copy = extractTempCopy(zipfile.getInputStream(entry));
                    OndexPlugin.getInstance().setOndexGraphFile(copy);
                    oxlSize = entry.getSize();
                    graphStream = zipfile.getInputStream(entry);
                    gzip = entry.getName().endsWith(".gz");
                }
            }
        } else {
            gzip = file.getName().endsWith(".gz");
            graphStream = new FileInputStream(file);
            oxlSize = file.length();
        }
        monitor.setStatus("loading graph data set...");
        boolean ok = loadGraph(new ByteCounterInputStream(graphStream), oxlSize, monitor, gzip);
        if (ok && mappingStream != null) {
            monitor.setStatus("loading mapping descriptors...");
            return loadMapping(mappingStream);
        } else {
            return null;
        }
    }

    /**
	 * extracts the contents of a input stream into the temp directory
	 * @param in the stream to save
	 * @return the temporary file
	 * @throws IOException
	 */
    private File extractTempCopy(InputStream in) throws IOException {
        String tmpdir = System.getProperty("java.io.tmpdir");
        File tmpfile = new File(tmpdir, "graph.xml.gz");
        OutputStream out = new FileOutputStream(tmpfile);
        int len = 0;
        byte[] buf = new byte[1024];
        while ((len = in.read(buf)) > 0) {
            out.write(buf, 0, len);
        }
        in.close();
        out.close();
        return tmpfile;
    }

    /**
	 * loads a graph from a stream
	 * @param graphStream
	 * @param monitor
	 */
    private boolean loadGraph(ByteCounterInputStream graphStream, long size, final TaskMonitor monitor, boolean gzip) {
        ONDEXGraph graph = new MemoryONDEXGraph("Ondex");
        final Parser parser = new net.sourceforge.ondex.parser.oxl.Parser();
        parser.setONDEXGraph(graph);
        try {
            final InputStream in = gzip ? new GZIPInputStream(graphStream) : graphStream;
            final List<String> errors = new ArrayList<String>();
            Thread t = new Thread("oxl parser thread") {

                @Override
                public void run() {
                    try {
                        parser.start(in);
                    } catch (ParsingFailedException e) {
                        errors.add("File contains errors!");
                        e.printStackTrace();
                    } catch (InconsistencyException e) {
                        errors.add("File contains inconsistencies!");
                        e.printStackTrace();
                    } finally {
                        try {
                            in.close();
                        } catch (IOException ioe) {
                        }
                    }
                }
            };
            t.start();
            while (t.isAlive()) {
                long read = graphStream.getBytesRead();
                long percent = read * 95L / size;
                monitor.setPercentCompleted((int) percent);
                try {
                    Thread.sleep(100);
                } catch (InterruptedException ex) {
                }
            }
            if (errors.size() > 0) {
                for (String e : errors) {
                    error(e);
                }
                return false;
            }
        } catch (IOException ioe) {
            error("Could not read file!");
            return false;
        }
        OndexPlugin.getInstance().setOndexGraph(graph);
        return true;
    }

    /**
	 * shows an error message on the screen.
	 * @param message your error message.
	 */
    private void error(String message) {
        JOptionPane.showMessageDialog(Cytoscape.getDesktop(), message, "Error", JOptionPane.ERROR_MESSAGE);
    }

    /**
	 * loads mappings from an input stream
	 * @param in the input stream
	 * @return a collection of mappings
	 * @throws IOException
	 * @throws ParseException
	 * @throws MalformedPathException
	 */
    private Collection<EdgeMappingDescriptor> loadMapping(InputStream in) throws IOException, ParseException, MalformedPathException {
        TreeSet<EdgeMappingDescriptor> ds = new TreeSet<EdgeMappingDescriptor>();
        BufferedReader br = new BufferedReader(new InputStreamReader(in));
        String line = null;
        int lineCount = 0;
        while ((line = br.readLine()) != null) {
            lineCount++;
            String[] cols = line.split("\t");
            if (cols.length == 2) {
                String name = cols[0].trim();
                String path = cols[1].trim();
                EdgeMappingDescriptor d = new EdgeMappingDescriptor(name, path);
                d.validate();
                ds.add(d);
            } else if (line.trim().equals("")) {
            } else {
                throw new ParseException("Invalid number of columns", lineCount);
            }
        }
        br.close();
        return ds;
    }
}
