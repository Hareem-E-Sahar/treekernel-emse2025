package edu.ucdavis.genomics.metabolomics.binbase.algorythm.data;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.rmi.RemoteException;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import javax.ejb.CreateException;
import javax.naming.NamingException;
import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.apache.log4j.Logger;
import org.jdom.Attribute;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.xml.sax.SAXException;
import com.sun.mail.iap.ParsingException;
import edu.ucdavis.genomics.metabolomics.binbase.algorythm.data.output.Writer;
import edu.ucdavis.genomics.metabolomics.binbase.algorythm.data.output.XLS;
import edu.ucdavis.genomics.metabolomics.binbase.algorythm.data.statistic.action.Action;
import edu.ucdavis.genomics.metabolomics.binbase.algorythm.data.statistic.combiner.CombineByMax;
import edu.ucdavis.genomics.metabolomics.binbase.algorythm.data.statistic.tool.Processable;
import edu.ucdavis.genomics.metabolomics.binbase.bci.Configurator;
import edu.ucdavis.genomics.metabolomics.exception.BinBaseException;
import edu.ucdavis.genomics.metabolomics.exception.ConfigurationException;
import edu.ucdavis.genomics.metabolomics.util.config.xml.XmlHandling;
import edu.ucdavis.genomics.metabolomics.util.io.dest.Destination;
import edu.ucdavis.genomics.metabolomics.util.io.dest.FileDestination;
import edu.ucdavis.genomics.metabolomics.util.io.source.ByteArraySource;
import edu.ucdavis.genomics.metabolomics.util.io.source.FileSource;
import edu.ucdavis.genomics.metabolomics.util.io.source.Source;
import edu.ucdavis.genomics.metabolomics.util.statistics.data.ColumnCombiner;
import edu.ucdavis.genomics.metabolomics.util.statistics.data.DataFile;
import edu.ucdavis.genomics.metabolomics.util.statistics.replacement.ZeroReplaceable;
import edu.ucdavis.genomics.metabolomics.util.transform.crosstable.ToDatafileTransformHandler;
import edu.ucdavis.genomics.metabolomics.util.transform.crosstable.object.ContentObject;
import edu.ucdavis.genomics.metabolomics.util.transform.crosstable.object.HeaderFormat;
import edu.ucdavis.genomics.metabolomics.util.type.converter.BooleanConverter;

/**
 * class with standard static statistic actions
 * 
 * @author wohlgemuth
 */
public class StaticStatisticActions {

    private static Logger logger = Logger.getLogger(StaticStatisticActions.class);

    /**
	 * should generated source and destination files be deleted
	 */
    private static final boolean deleteSourceAndDestinationFiles = false;

    /**
	 * replace the zeros in the datafile
	 * 
	 * @author wohlgemuth
	 * @version Jul 13, 2006
	 * @param file
	 * @return
	 */
    public static DataFile replaceZeros(String id, DataFile file, Element element) throws Exception {
        ZeroReplaceable replace = null;
        try {
            String method = element.getAttributeValue("method");
            String range = element.getAttributeValue("range");
            logger.info("create instance of " + method + " for replacement");
            replace = (ZeroReplaceable) Class.forName(method).newInstance();
            if (replace instanceof BinBaseResultZeroReplaceable) {
                logger.info("running initialization method...");
                ((BinBaseResultZeroReplaceable) replace).initializeReplaceable();
            }
            if (range.toLowerCase().equals("class")) {
                if (isDataFileValidForReplacement(id, replace, file)) {
                    logger.info("using class mode");
                    file.replaceZeros(replace, 1);
                } else {
                    logger.info("no zero replacement possible");
                }
            } else if (range.toLowerCase().equals("experiment")) {
                if (isDataFileValidForReplacement(id, replace, file)) {
                    logger.info("using experiment mode");
                    file.replaceZeros(replace, false);
                } else {
                    logger.info("no zero replacement possible");
                }
            } else if (range.toLowerCase().equals("sample")) {
                if (isDataFileValidForReplacement(id, replace, file)) {
                    logger.info("using sample mode");
                    file.replaceZeros(replace, true);
                } else {
                    logger.info("no zero replacement possible");
                }
            } else {
                logger.warn("replace zeros by using experiment mode!");
                if (isDataFileValidForReplacement(id, replace, file)) {
                    logger.info("using experiment mode");
                    file.replaceZeros(replace, false);
                } else {
                    logger.info("no zero replacement possible");
                }
            }
            if (element.getAttribute("folder") == null) {
                if (replace instanceof BinBaseResultZeroReplaceable) {
                    element.setAttribute(new Attribute("folder", ((BinBaseResultZeroReplaceable) replace).getFolder()));
                } else {
                    element.setAttribute(new Attribute("folder", replace.getClass().getName()));
                }
            }
        } catch (Exception e) {
            logger.warn(e.getMessage(), e);
        } finally {
            replace = null;
            System.gc();
        }
        return file;
    }

    /**
	 * runs processing instructions on this file and returns the expsected file.
	 * This can be a completely new file of a differnt type or the original
	 * file.
	 * 
	 * @param file
	 * @param element
	 * @param id
	 * @param folder
	 * @param destiantionIds
	 * @return
	 * @throws Exception
	 */
    public static DataFile process(DataFile file, Element element, String id, String folder, Collection<String> destiantionIds) throws Exception {
        try {
            logger.info("start processing...");
            Processable process = null;
            process = createProcessing(id, destiantionIds, element);
            folder = calculateFolderPathForProcessingInstruction(element, folder, process);
            if (file instanceof ResultDataFile) {
                file = process.process((ResultDataFile) file, element);
            } else {
                logger.info("file was not a result file, we are using instead a simple processing instruction");
                file = process.simpleProcess(file, element);
            }
            writeProcessingResult(file, element, id, folder, destiantionIds, process);
            process = null;
        } catch (Exception e) {
            logger.warn(e.getMessage(), e);
            return null;
        } finally {
            System.gc();
        }
        logger.info("done...");
        return file;
    }

    /**
	 * creates our processing object
	 * 
	 * @param id
	 * @param destiantionIds
	 * @param method
	 * @return
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 * @throws ClassNotFoundException
	 */
    private static Processable createProcessing(String id, Collection<String> destiantionIds, Element element) throws InstantiationException, IllegalAccessException, ClassNotFoundException {
        String method = element.getAttributeValue("method");
        logger.info("create instance of " + method + " for processing");
        Processable process = (Processable) Class.forName(method).newInstance();
        process.setCurrentId(id);
        process.setDestinationIds(destiantionIds);
        return process;
    }

    /**
	 * calculates the local path for the processing result file and sets it to
	 * the processing object
	 * 
	 * @param element
	 * @param folder
	 * @param process
	 */
    private static String calculateFolderPathForProcessingInstruction(Element element, String folder, Processable process) {
        if (element.getAttribute("folder") == null) {
            element.setAttribute(new Attribute("folder", process.getFolder()));
        }
        folder = folder + element.getAttribute("folder").getValue();
        process.setCurrentFolder(folder);
        return folder;
    }

    /**
	 * writes the processing result to the given folder, if the processable
	 * object allows this
	 * 
	 * @param file
	 * @param element
	 * @param id
	 * @param folder
	 * @param destiantionIds
	 * @param process
	 * @throws IOException
	 */
    private static void writeProcessingResult(DataFile file, Element element, String id, String folder, Collection<String> destiantionIds, Processable process) throws IOException {
        if (file != null) {
            if (process.writeResultToFile()) {
                writeEntry(id, folder, file, element, destiantionIds, process.getFileIdentifier());
            } else {
                logger.info("result was not supposed to be written to the result, it was just a modification of the data");
            }
        } else {
            logger.warn("didn't return a value and so is skipped in the output");
        }
    }

    /**
	 * validate if the datafile is valid
	 * 
	 * @author wohlgemuth
	 * @version Feb 13, 2007
	 * @param replace
	 * @param file
	 * @return
	 */
    public static boolean isDataFileValidForReplacement(String id, ZeroReplaceable replace, DataFile file) {
        if (replace instanceof BinBaseResultZeroReplaceable) {
            ((BinBaseResultZeroReplaceable) replace).setFile((ResultDataFile) file);
            if (((BinBaseResultZeroReplaceable) replace).isValid() == false) {
                logger.warn("can't replace zeros with: " + replace.getClass().getName() + " because the class says it is not valid! Please check your configuration");
                return false;
            }
        }
        return true;
    }

    public static ResultDataFile readFile(String id, Source rawdata, Element cross, String currentFolder, Collection<String> destinationIds) throws Exception {
        return readFile(id, rawdata, cross, currentFolder, destinationIds, false);
    }

    public static ResultDataFile readFile(String id, Source rawdata, Element cross) throws Exception {
        return readFile(id, rawdata, cross, "data", new Vector<String>(), false);
    }

    /**
	 * reads a blank datafile from the harddisk
	 * 
	 * @author wohlgemuth
	 * @version Jul 13, 2006
	 * @param rawdata
	 * @param cross
	 * @param currentFolder
	 * @param first
	 * @return
	 * @throws Exception
	 */
    @SuppressWarnings("unchecked")
    public static ResultDataFile readFile(String id, Source rawdata, Element cross, String currentFolder, Collection<String> destinationIds, boolean first) throws Exception {
        ResultDataFile file = parseXMLContent(rawdata, cross);
        logger.info("reading file of instance: " + file.getClass().getName());
        double sizeDown = Double.parseDouble(cross.getAttributeValue("sizedown"));
        if (cross.getChild("reference") == null) {
            file = processNormal(cross, sizeDown, file);
        } else {
            file = refrenceBased(cross, sizeDown, rawdata, currentFolder, id, destinationIds, first);
        }
        file.initialize();
        return file;
    }

    private static ResultDataFile processNormal(Element cross, double sizeDown, ResultDataFile file) {
        filterContent(cross, file);
        cleanUp(cross, file);
        sizeDown(sizeDown, file);
        return file;
    }

    /**
	 * only exports bins which are found in the reference experiment
	 * 
	 * @param cross
	 * @param sizeDown
	 * @param file
	 * @param currentFolder
	 * @param id
	 * @param destiantionIds
	 * @param first
	 * @throws FileNotFoundException
	 * @throws IOException
	 * @throws edu.ucdavis.genomics.metabolomics.binbase.bci.server.exception.FileNotFoundException
	 * @throws BinBaseException
	 * @throws RemoteException
	 * @throws CreateException
	 * @throws NamingException
	 * @throws FactoryConfigurationError
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 * @throws ParsingException
	 */
    private static ResultDataFile refrenceBased(Element cross, double sizeDown, Source rawdata, String currentFolder, String id, Collection<String> destiantionIds, boolean first) throws FileNotFoundException, IOException, edu.ucdavis.genomics.metabolomics.binbase.bci.server.exception.FileNotFoundException, BinBaseException, RemoteException, CreateException, NamingException, FactoryConfigurationError, ParserConfigurationException, SAXException, ParsingException {
        logger.info("reference mode is enabled");
        Element reference = cross.getChild("reference");
        if (reference.getAttributeValue("experiment") != null) {
            String referenceId = reference.getAttributeValue("experiment");
            logger.info("using as reference: " + referenceId);
            Source referenceSource = loadReferenceSource(referenceId);
            logger.info("loading: " + rawdata.getSourceName());
            ResultDataFile file = parseXMLContent(rawdata, cross);
            ResultDataFile referenceFile = parseXMLContent(referenceSource, cross);
            sizeDown(sizeDown, referenceFile);
            sizeDown(0, file);
            logger.info("removing bins and groups which are not in the frefrence file");
            for (HeaderFormat<String> fb : file.getBins(true)) {
                int group = Integer.parseInt(fb.getAttributes().get("group"));
                if (group == 0) {
                    if (referenceFile.containsBin(Integer.parseInt(fb.getAttributes().get("id"))) == false) {
                        file.removeBin(fb);
                        logger.info("bin was not contained in reference file: " + fb.getValue());
                    }
                } else {
                    if (referenceFile.containsGroup(group) == false) {
                        file.removeBin(fb);
                        logger.info("bin group was not contained in reference file: " + fb.getValue());
                    }
                }
            }
            logger.info("adding bins and groups which are not in the file, but found in the reference file");
            for (HeaderFormat<String> rb : referenceFile.getBins(true)) {
                int group = Integer.parseInt(rb.getAttributes().get("group"));
                if (group == 0) {
                    if (file.containsBin(Integer.parseInt(rb.getAttributes().get("id"))) == false) {
                        logger.info("bin was not contained in  file: " + rb.getValue());
                        mergeBin(file, referenceFile, rb);
                    }
                } else {
                    if (file.containsGroup(group) == false) {
                        logger.info("bin group was not contained in file: " + rb.getValue());
                        mergeBin(file, referenceFile, rb);
                    }
                }
            }
            if (first) {
                writeEntry(id, currentFolder + "/reference/" + referenceId + "/", referenceFile, cross, destiantionIds);
                ResultDataFile file2 = parseXMLContent(rawdata, cross);
                processNormal(cross, sizeDown, file2);
                writeEntry(id, currentFolder + "/reference/original/", file2, cross, destiantionIds);
            }
            return file;
        } else {
            throw new ParsingException("sorry we need the attribute experiment with the experiment id as value!");
        }
    }

    private static void mergeBin(ResultDataFile file, ResultDataFile referenceFile, HeaderFormat<String> rb) {
        if (rb.getAttributes().get("standard") != null) {
            if (rb.getAttributes().get("standard").toLowerCase().equals("true")) {
                logger.info("a standard not merging...");
            } else {
                logger.info("not a standard merging...");
                merge(file, referenceFile, rb);
            }
        } else {
            logger.info("merging...");
            merge(file, referenceFile, rb);
        }
    }

    @SuppressWarnings("unchecked")
    private static void merge(ResultDataFile file, ResultDataFile referenceFile, HeaderFormat<String> rb) {
        int pos = referenceFile.getBinPosition(Integer.parseInt(rb.getAttributes().get("id")));
        List list = referenceFile.getColumn(pos);
        pos = file.addEmptyColumn();
        List nc = file.getColumn(pos);
        for (int i = 0; i < nc.size(); i++) {
            if (list.get(i) instanceof ContentObject<?>) {
                nc.set(i, null);
            } else {
                nc.set(i, list.get(i));
            }
        }
        file.setColumn(pos, nc);
    }

    /**
	 * looks in the temporary directory for the result file and if not locally
	 * found it loads it from the server
	 * 
	 * @param referenceId
	 * @return
	 * @throws FileNotFoundException
	 * @throws IOException
	 * @throws edu.ucdavis.genomics.metabolomics.binbase.bci.server.exception.FileNotFoundException
	 * @throws BinBaseException
	 * @throws RemoteException
	 * @throws CreateException
	 * @throws NamingException
	 */
    private static Source loadReferenceSource(String referenceId) throws FileNotFoundException, IOException, edu.ucdavis.genomics.metabolomics.binbase.bci.server.exception.FileNotFoundException, BinBaseException, RemoteException, CreateException, NamingException {
        logger.debug("looking in the temporary directory for the file...");
        Source localSource = new FileSource(new File(System.getProperty("java.io.tmpdir") + File.separator + referenceId + ".xml"));
        if (localSource.exist()) {
            return localSource;
        }
        logger.debug("loading it from the server");
        Source referenceSource = new ByteArraySource(Configurator.getExportService().getResult(referenceId + ".xml"));
        return referenceSource;
    }

    /**
	 * parses the actual xml content and generate the datafile out of it
	 * 
	 * @param rawdata
	 * @param cross
	 * @return
	 * @throws FactoryConfigurationError
	 * @throws IOException
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 */
    @SuppressWarnings("unchecked")
    public static ResultDataFile parseXMLContent(Source rawdata, Element cross) throws FactoryConfigurationError, IOException, ParserConfigurationException, SAXException {
        logger.debug("create transfomer");
        ToDatafileTransformHandler handler = new ToDatafileTransformHandler();
        logger.debug("calculate parameters");
        try {
            XmlHandling.logXML(logger, cross);
        } catch (Exception e) {
        }
        List headers = cross.getChild("header").getChildren("param");
        Iterator it = headers.iterator();
        while (it.hasNext()) {
            String headerContent = ((Element) it.next()).getAttributeValue("value");
            logger.debug("defining header: " + headerContent);
            handler.addHeader(headerContent);
        }
        logger.info("set key: " + cross.getAttributeValue("attribute"));
        handler.setKey(cross.getAttributeValue("attribute"));
        logger.debug("create parser");
        SAXParserFactory factory = SAXParserFactory.newInstance();
        factory.setValidating(false);
        InputStream stream = rawdata.getStream();
        SAXParser builder = factory.newSAXParser();
        logger.debug("parse document into handler");
        builder.parse(stream, handler);
        stream.close();
        logger.debug("parsing is done");
        stream = null;
        builder = null;
        factory = null;
        System.gc();
        ResultDataFile file = maskDatafile(handler, headers);
        combineBins(cross, file);
        file.setResultId(handler.getResultId());
        file.setDatabase(handler.getDatabase());
        return file;
    }

    private static void sizeDown(double sizeDown, ResultDataFile file) {
        logger.info("size of datafile before sizedown " + file.getColumnCount());
        file.sizeDown(true, 1, sizeDown);
        logger.info("size of datafile after sizedown " + file.getColumnCount());
    }

    private static void cleanUp(Element cross, ResultDataFile file) {
        if (cross.getAttribute("keepFailed") != null) {
            try {
                boolean failed = cross.getAttribute("keepFailed").getBooleanValue();
                if (failed == false) {
                    file.removeFailedSamples();
                }
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
        } else {
            logger.info("attribute keepFailed not found");
        }
    }

    private static ResultDataFile maskDatafile(ToDatafileTransformHandler handler, List<Element> headers) {
        ResultDataFile file = (ResultDataFile) handler.getFile();
        int[] rows = new int[headers.size() + 1 + handler.getRefrenceCount()];
        for (int i = 0; i < rows.length; i++) {
            rows[i] = i;
        }
        file.setIgnoreRows(rows);
        file.setIgnoreColumns(new int[] { 0, 1, 2 });
        return file;
    }

    @SuppressWarnings("unchecked")
    private static void filterContent(Element cross, ResultDataFile file) {
        logger.info("filtering data");
        if (cross.getChild("filter") != null) {
            Element filter = cross.getChild("filter");
            List<Element> bins = filter.getChildren("bin");
            List<Element> samples = filter.getChildren("sample");
            filterBins(file, bins, false, false);
            filterSamples(file, samples);
        } else {
            logger.info("no filter defined!");
        }
    }

    private static void combineBins(Element cross, ResultDataFile file) {
        if (cross.getAttribute("combine") != null) {
            String className = cross.getAttributeValue("combine");
            logger.info("combine datasets using: " + className);
            try {
                logger.info("size of datafile before combined: " + file.getColumnCount());
                if (BooleanConverter.StringtoBoolean(cross.getAttribute("combine").getValue()) == true) {
                    logger.info("combine columns --> enabled");
                    file.combineColumns(new CombineByMax());
                } else if ((cross.getAttribute("combine").getValue()).toLowerCase().equals("false")) {
                    logger.info("combine columns --> disabled");
                } else {
                    logger.info("combine columns --> enabled which specific combiner " + className);
                    ColumnCombiner combiner = (ColumnCombiner) Class.forName(className).newInstance();
                    if (combiner.isConfigNeeded()) {
                        logger.info("set configuration of combiner");
                        combiner.setConfig(cross.getChild("combiner"));
                    }
                    file.combineColumns(combiner);
                }
                logger.info("size of datafile after combined: " + file.getColumnCount());
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
        } else {
            logger.info("attribute combine not found!");
        }
    }

    /**
	 * filter samples out of the result file
	 * 
	 * @author wohlgemuth
	 * @version Nov 2, 2006
	 * @param file
	 * @param samples
	 */
    public static void filterSamples(ResultDataFile file, List<Element> samples) {
        if (samples != null) {
            if (samples.isEmpty()) {
                return;
            }
            logger.info("size of datafile before removing samples: " + file.getRowCount());
            List<String> keep = new Vector<String>();
            for (int i = 0; i < samples.size(); i++) {
                Element b = samples.get(i);
                if (b.getAttribute("match") != null) {
                    keep.add(b.getAttributeValue("match"));
                } else {
                    logger.debug("attribute match not defined for sample, ignore");
                }
            }
            file.filterSamples(keep);
            logger.info("size of datafile after removing samples: " + file.getColumnCount());
        } else {
            logger.info("samples will not be filtered");
        }
    }

    /**
	 * filter bins out of the result file
	 * 
	 * @author wohlgemuth
	 * @version Nov 2, 2006
	 * @param file
	 * @param bins
	 */
    public static void filterBins(ResultDataFile file, List<Element> bins, boolean equals, boolean byId) {
        if (bins != null) {
            if (bins.isEmpty()) {
                return;
            }
            logger.info("size of datafile before removing bins: " + file.getColumnCount());
            List<String> keep = new Vector<String>();
            for (int i = 0; i < bins.size(); i++) {
                Element b = bins.get(i);
                if (b.getAttribute("match") != null) {
                    logger.debug("keeping bin: " + b.getAttributeValue("match"));
                    keep.add(b.getAttributeValue("match"));
                } else {
                    logger.warn("attribute match not defined for bin, ignore");
                }
            }
            file.filterBins(keep, equals, byId);
            logger.info("size of datafile after removing bins: " + file.getColumnCount());
        } else {
            logger.info("bins will not be filtered");
        }
    }

    public static void writeEntry(String id, String fileName, final Object current, Element data, Collection<String> destiantionIds) throws IOException {
        writeEntry(id, fileName, current, data, destiantionIds, "result");
    }

    /**
	 * write a result file to the zipfile
	 * 
	 * @author wohlgemuth
	 * @version Nov 19, 2005
	 * @param stream
	 *            the outputstream
	 * @param the
	 *            relative filename from the root of the zip strea,
	 * @param current
	 *            the content to copy to the outputstream
	 * @throws IOException
	 */
    public static void writeEntry(String id, String fileName, final Object current, Element data, Collection<String> destiantionIds, String identifier) throws IOException {
        if (data.getChildren("format").size() == 0) {
            Element e = new Element("format");
            e.setAttribute("type", XLS.class.getName());
            tableOutput(id, fileName, current, e, destiantionIds, identifier);
        } else {
            for (Object e : data.getChildren("format")) {
                tableOutput(id, fileName, current, (Element) e, destiantionIds, identifier);
            }
        }
    }

    public static void writeOutput(String id, String fileName, final Object current, Collection<String> destinationIds, String identifier, Writer writer) {
        if (writer != null) {
            try {
                OutputStream stream = createOutputStream(id, fileName, destinationIds, identifier, writer);
                try {
                    if (writer.isDatafileSupported()) {
                        if (current instanceof DataFile) {
                            writer.write(stream, (DataFile) current);
                        }
                    } else if (writer.isSourceSupported()) {
                        if (current instanceof Source) {
                            writer.write(stream, (Source) current);
                        } else if (current instanceof DataFile) {
                            if (writer.isDatafileSupported() == false) {
                                writer.write(stream, new Source() {

                                    public void configure(Map<?, ?> p) throws ConfigurationException {
                                    }

                                    public boolean exist() {
                                        return true;
                                    }

                                    public String getSourceName() {
                                        return "datfile converted to input stream";
                                    }

                                    public InputStream getStream() throws IOException {
                                        return ((DataFile) current).toInputStream();
                                    }

                                    public long getVersion() {
                                        return 0;
                                    }

                                    public void setIdentifier(Object o) throws ConfigurationException {
                                    }
                                });
                            }
                        } else {
                            writer.write(stream, current);
                        }
                    } else {
                        writer.write(stream, current);
                    }
                } catch (Exception ex) {
                    logger.error(ex.getMessage(), ex);
                }
                stream.flush();
                stream.close();
            } catch (Exception ex) {
                logger.error(ex.getMessage(), ex);
            }
        }
    }

    /**
	 * @author wohlgemuth
	 * @version Feb 13, 2007
	 * @param stream
	 * @param fileName
	 * @param current
	 * @param e
	 * @throws IOException
	 */
    public static void tableOutput(String id, String fileName, final Object current, Element e, Collection<String> destinationIds, String identifier) throws IOException {
        String format = e.getAttributeValue("type");
        Writer writer = null;
        if (format.indexOf(".") > -1) {
            try {
                writer = (Writer) Class.forName(format).newInstance();
            } catch (InstantiationException e1) {
                logger.error(e1.getMessage(), e1);
            } catch (IllegalAccessException e1) {
                logger.error(e1.getMessage(), e1);
            } catch (ClassNotFoundException e1) {
                logger.error(e1.getMessage(), e1);
            }
        } else {
            try {
                writer = (Writer) Class.forName(Writer.class.getPackage().getName() + "." + format).newInstance();
            } catch (InstantiationException e1) {
                logger.error(e1.getMessage(), e1);
            } catch (IllegalAccessException e1) {
                logger.error(e1.getMessage(), e1);
            } catch (ClassNotFoundException e1) {
                logger.error(e1.getMessage(), e1);
            }
        }
        writeOutput(id, fileName, current, destinationIds, identifier, writer);
    }

    private static OutputStream createOutputStream(String id, String fileName, Collection<String> destinationIds, String identifier, Writer writer) throws ConfigurationException, IOException {
        logger.info("using id: " + id);
        logger.info("using filename: " + fileName);
        if (fileName == null) {
            fileName = "/";
        }
        if (fileName.startsWith("/") == false) {
            fileName = "/" + fileName;
        }
        if (fileName.endsWith("/") == false) {
            fileName = fileName + "/";
        }
        String internalId = id + fileName + identifier + "." + writer.toString();
        logger.info("final filename: " + internalId);
        destinationIds.add(internalId);
        Destination dest = createDestination(id + fileName);
        dest.setIdentifier(identifier + "." + writer.toString());
        OutputStream stream = dest.getOutputStream();
        return stream;
    }

    /**
	 * creates a destination with the given id we basically store subdirectories
	 * and stuff like this there anf once its done we all zip it to one big file
	 * 
	 * @param id
	 * @return
	 */
    private static Destination createDestination(String id) {
        File file = new File("result" + File.separator + id);
        if (deleteSourceAndDestinationFiles) {
            file.deleteOnExit();
        }
        logger.info("storing content at: " + file.getAbsolutePath());
        file.mkdirs();
        logger.info("location exist: " + file.exists());
        return new FileDestination(file.getAbsolutePath());
    }

    /**
	 * transform the data into a table
	 * 
	 * @author wohlgemuth
	 * @version Feb 20, 2007
	 * @param cross
	 * @param rawdata
	 * @param id
	 * @param id
	 * @throws Exception
	 */
    @SuppressWarnings("unchecked")
    public static void transform(Element cross, Source rawdata, String id, Collection<String> destiantionIds) throws Exception {
        String currentFolder = null;
        if (cross.getAttributeValue("folder") == null) {
            logger.info("using folder ==> " + cross.getAttributeValue("attribute"));
            currentFolder = cross.getAttributeValue("sizedown") + "/" + cross.getAttributeValue("attribute");
        } else {
            logger.info("using folder ==> " + cross.getAttributeValue("folder"));
            currentFolder = cross.getAttributeValue("sizedown") + "/" + cross.getAttributeValue("folder");
        }
        DataFile file = null;
        try {
            file = readFile(id, rawdata, cross, currentFolder, destiantionIds, true);
        } catch (NullPointerException e) {
            logger.warn("couldn't find key (likley)", e);
            return;
        }
        logger.info("process rawdata");
        Element rawdataOut = cross.getChild("rawdata");
        if (rawdataOut != null) {
            String folder = rawdataOut.getAttributeValue("folder");
            if (folder == null) {
                folder = "rawdata";
            }
            writeEntry(id, currentFolder + "/" + folder, file, rawdataOut, destiantionIds);
        }
        logger.info("generate statistics");
        List<Element> statistics = cross.getChildren("processing");
        for (Element statistic : statistics) {
            List<Element> stat = statistic.getChildren("zero-replacement");
            List<Element> proc = statistic.getChildren("processable");
            logger.info("doing generic zero replacement work");
            for (Element element : stat) {
                List<Element> prePro = element.getChildren("pre-processable");
                List<Element> postPro = element.getChildren("post-processable");
                DataFile current = readFile(id, rawdata, cross, currentFolder, destiantionIds);
                current = chainedPreProcessing(id, destiantionIds, currentFolder, element, prePro, current);
                current = replaceZeros(id, current, element);
                writeEntry(id, currentFolder + "/replace/" + element.getAttributeValue("folder"), current, element, destiantionIds);
                logger.info("found " + postPro.size() + " post processing instructions");
                chainedPostProcessing(id, destiantionIds, currentFolder, element, postPro, current);
                current = null;
                System.gc();
            }
            logger.info("do proccessing after zero replacement, this does not affect the netcdf output");
            processing(cross, rawdata, id, destiantionIds, currentFolder, proc);
        }
    }

    /**
	 * runs a chained pre processing over the datafile and builds the initial
	 * file for the netcdf replacement
	 * 
	 * @param id
	 * @param destiantionIds
	 * @param currentFolder
	 * @param element
	 * @param prePro
	 * @param current
	 * @return
	 * @throws Exception
	 */
    private static DataFile chainedPreProcessing(String id, Collection<String> destiantionIds, String currentFolder, Element element, List<Element> prePro, DataFile current) throws Exception {
        logger.info("found " + prePro.size() + " pre processing instructions");
        for (Element e : prePro) {
            logger.info("doing generic pre processing work");
            String folder = currentFolder + "/replace/" + element.getAttributeValue("folder") + "/pre-process/";
            DataFile result = process(current, e, id, folder, destiantionIds);
            if (current != null) {
                current = result;
            }
        }
        return current;
    }

    /**
	 * runs a chained post processing and each result will be used as the input
	 * file for the next processing isntruction
	 * 
	 * @param id
	 * @param destiantionIds
	 * @param currentFolder
	 * @param element
	 * @param postPro
	 * @param current
	 * @throws Exception
	 */
    private static void chainedPostProcessing(String id, Collection<String> destiantionIds, String currentFolder, Element element, List<Element> postPro, DataFile current) throws Exception {
        for (Element e : postPro) {
            logger.info("doing generic post processing work");
            String folder = currentFolder + "/replace/" + element.getAttributeValue("folder") + "/post-process/";
            DataFile result = process(current, e, id, folder, destiantionIds);
            if (result != null) {
                current = result;
            }
        }
    }

    /**
	 * converts a source to an element
	 * 
	 * @param sop
	 * @return
	 * @throws JDOMException
	 * @throws IOException
	 */
    public static Element readSource(Source sop) throws JDOMException, IOException {
        Document sopDefinition = new SAXBuilder().build(sop.getStream());
        Element root = sopDefinition.getRootElement();
        return root;
    }

    /**
	 * doing generic processing work. The result will not be chained into the
	 * next direction
	 * 
	 * @param cross
	 * @param rawdata
	 * @param id
	 * @param destiantionIds
	 * @param currentFolder
	 * @param proc
	 * @throws Exception
	 * @throws IOException
	 */
    private static void processing(Element cross, Source rawdata, String id, Collection<String> destiantionIds, String currentFolder, List<Element> proc) throws Exception, IOException {
        logger.info("found " + proc.size() + " post processing instructions");
        for (Element element : proc) {
            logger.info("doing generic processing work");
            DataFile current = readFile(id, rawdata, cross, currentFolder, destiantionIds);
            String folder = currentFolder + "/process/";
            process(current, element, id, folder, destiantionIds);
            current = null;
            System.gc();
        }
    }

    /**
	 * creates the final zip file
	 */
    public static void createFinalZip(Destination destination, String id, Collection<String> entryIds) throws Exception {
        logger.info("create zip file for id: " + id);
        ZipOutputStream ou = new ZipOutputStream(destination.getOutputStream());
        byte[] buf = new byte[1024];
        for (String file : entryIds) {
            logger.info("working on: " + file);
            String name = file.substring(file.indexOf(id) + id.length() + 1, file.length());
            logger.info("generated internal name: " + name);
            Source source = createSource(file);
            InputStream in = source.getStream();
            logger.info("write...");
            ou.putNextEntry(new ZipEntry(name));
            int len;
            while ((len = in.read(buf)) > 0) {
                ou.write(buf, 0, len);
            }
            in.close();
            ou.closeEntry();
        }
        ou.flush();
        ou.close();
        logger.info("done with zip creation");
    }

    /**
	 * creates a source to access an entry
	 * 
	 * @param id
	 * @return
	 * @throws Exception
	 */
    public static Source createSource(String id) throws Exception {
        File file = new File("result" + File.separator + id);
        if (deleteSourceAndDestinationFiles) {
            file.deleteOnExit();
        }
        logger.info("retrieving content from: " + file.getAbsolutePath());
        logger.info("location exist: " + file.exists());
        return new FileSource(file);
    }

    /**
	 * executes an action, which can happen before or after the transformation
	 * 
	 * @param cross
	 * @param rawdata
	 * @param id
	 * @param destiantionIds
	 * @param sop
	 */
    public void action(Element cross, Source rawdata, String id, Collection<String> destiantionIds, Source sop, List<Element> transformInstructions) throws Exception {
        try {
            String className = cross.getAttributeValue("method");
            String column = cross.getAttributeValue("column");
            Action action = (Action) Class.forName(className).newInstance();
            action.setTransformInstructions(transformInstructions);
            action.setColumn(column);
            action.setCurrentId(id);
            action.setDestinationIds(destiantionIds);
            logger.info("running action: " + action.getClass().getName());
            action.run(cross, rawdata, sop);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }
}
