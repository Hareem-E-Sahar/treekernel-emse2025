package net.sf.myra.datamining.io;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.Writer;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.StringTokenizer;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;
import net.sf.myra.datamining.data.Attribute;
import net.sf.myra.datamining.data.ClassHierarchy;
import net.sf.myra.datamining.data.ContinuousAttribute;
import net.sf.myra.datamining.data.Dataset;
import net.sf.myra.datamining.data.Instance;
import net.sf.myra.datamining.data.Metadata;
import net.sf.myra.datamining.data.Node;
import net.sf.myra.datamining.data.NominalAttribute;

/**
 * Helper class to manipulate files in ARFF (Weka) format.
 * 
 * @author Fernando Esteban Barril Otero
 * @version $Revision: 2382 $ $Date:: 2011-07-27 06:47:06#$
 */
public class ArffHelper extends Helper {

    /**
	 * No instances allowed (private constructor).
	 */
    public ArffHelper() {
    }

    /**
	 * Returns a <code>Dataset</code> instance that represents the data of
	 * the specified ARFF file.
	 * 
	 * @param file the file to read the data from.
	 * 
	 * @return a <code>Dataset</code> instance.
	 * 
	 * @throws IOException if any file operation fails.
	 */
    public Dataset read(File file) throws IOException {
        String name = file.getName().toLowerCase();
        Dataset dataset = null;
        if (name.endsWith(".zip")) {
            ZipFile zip = new ZipFile(file, ZipFile.OPEN_READ);
            if (zip.size() > 1) {
                throw new IllegalArgumentException("Multiple entries found " + "in the zip file: " + name);
            }
            try {
                ZipEntry e = zip.entries().nextElement();
                dataset = read(new InputStreamReader(zip.getInputStream(e)));
            } finally {
                zip.close();
            }
        } else {
            dataset = read(new FileReader(file));
        }
        dataset.setFilename(file.getAbsolutePath());
        return dataset;
    }

    /**
	 * Returns a <code>Dataset</code> instance that represents the data of
	 * the specified ARFF file.
	 * 
	 * @param reader the input reader.
	 * 
	 * @return a <code>Dataset</code> instance.
	 * 
	 * @throws IOException if any file operation fails.
	 */
    public Dataset read(Reader reader) throws IOException {
        BufferedReader input = null;
        try {
            input = new BufferedReader(reader);
            Metadata metadata = new Metadata();
            LinkedHashMap<String, ArrayList<String>> hierarchyInfo = new LinkedHashMap<String, ArrayList<String>>();
            String line = null;
            Attribute last = null;
            while ((line = input.readLine()) != null) {
                line = line.trim();
                if ("@data".equals(line.toLowerCase())) {
                    break;
                }
                if (!(isComment(line) || isBlank(line))) {
                    if (line.toLowerCase().startsWith("@attribute")) {
                        String[] fields = line.split("\\s+");
                        if (fields.length < 3) {
                            throw new IllegalArgumentException("Illegal attribute definition: " + line);
                        }
                        String name = trim(fields[1]);
                        String type = trim(fields[2]).toLowerCase();
                        if ("string".equals(type) || "date".equals(type)) {
                            last = new UnsupportedAttribute(name);
                        } else if (type.indexOf("{") != -1) {
                            StringTokenizer values = new StringTokenizer(line.substring(line.indexOf("{") + 1, line.indexOf("}")).trim(), ",");
                            NominalAttribute attribute = new NominalAttribute(trim(name));
                            while (values.hasMoreTokens()) {
                                attribute.add(trim(values.nextToken()));
                            }
                            last = attribute;
                        } else {
                            last = new ContinuousAttribute(trim(name));
                        }
                        metadata.add(last);
                    } else if (line.toLowerCase().startsWith("@relation")) {
                        String[] fields = line.split("\\s+");
                        metadata.setName(trim(fields[1]));
                    } else if (line.toLowerCase().startsWith("@class")) {
                        int firstSpace = line.indexOf(" ");
                        int secondSpace = line.indexOf(" ", firstSpace + 1);
                        String name = trim((secondSpace == -1) ? line.substring(firstSpace + 1) : line.substring(firstSpace, secondSpace));
                        if (line.indexOf("{") != -1) {
                            StringTokenizer values = new StringTokenizer(line.substring(line.indexOf("{") + 1, line.indexOf("}")).trim(), ",");
                            ArrayList<String> parents = new ArrayList<String>();
                            while (values.hasMoreTokens()) {
                                parents.add(trim(values.nextToken()));
                            }
                            hierarchyInfo.put(name, parents);
                        } else {
                            hierarchyInfo.put(name, null);
                        }
                    }
                }
            }
            ClassHierarchy hierarchy = new ClassHierarchy();
            while (!hierarchyInfo.isEmpty()) {
                Iterator<String> iterator = hierarchyInfo.keySet().iterator();
                int before = hierarchyInfo.size();
                while (iterator.hasNext()) {
                    try {
                        String n = iterator.next();
                        if (hierarchyInfo.get(n) == null) {
                            hierarchy.add(n);
                        } else {
                            Iterator<String> parents = hierarchyInfo.get(n).iterator();
                            Node node = null;
                            try {
                                node = hierarchy.find(n);
                            } catch (IllegalArgumentException e) {
                            }
                            while (parents.hasNext()) {
                                if (node == null) {
                                    node = hierarchy.add(n, parents.next());
                                } else {
                                    hierarchy.link(parents.next(), n);
                                }
                                parents.remove();
                            }
                        }
                        iterator.remove();
                    } catch (IllegalArgumentException e) {
                    }
                }
                if (before == hierarchyInfo.size()) {
                    throw new IllegalArgumentException("Illegal class hierarchy.");
                }
            }
            if (!hierarchy.isEmpty()) {
                metadata.setClassHierarchy(hierarchy);
                hierarchy.weigh();
            }
            if (last instanceof NominalAttribute) {
                metadata.setTarget((NominalAttribute) last);
            }
            Dataset dataset = new Dataset(metadata);
            while ((line = input.readLine()) != null) {
                if (!(isComment(line) || isBlank(line))) {
                    StringTokenizer tokens = new StringTokenizer(line, ",");
                    String[] values = new String[tokens.countTokens()];
                    for (int i = 0; tokens.hasMoreTokens(); i++) {
                        values[i] = trim(tokens.nextToken());
                    }
                    try {
                        dataset.add(values);
                    } catch (IllegalArgumentException e) {
                        System.out.println("(ignoring record) " + e.getMessage());
                    }
                }
            }
            return dataset;
        } finally {
            if (input != null) {
                input.close();
            }
        }
    }

    /**
	 * Writes an ARFF file representing the specified dataset.
	 * 
	 * @param directory the output directory.
	 * @param dataset the dataset to be written.
	 * 
	 * @throws IOException
	 */
    public void write(File directory, Dataset dataset) throws IOException {
        Metadata metadata = dataset.getMetadata();
        String name = metadata.getName() + "." + getExtension();
        OutputStream out = new FileOutputStream(new File(directory, name + (isCompressed() ? ".zip" : "")));
        if (isCompressed()) {
            out = new ZipOutputStream(out);
            ((ZipOutputStream) out).putNextEntry(new ZipEntry(name));
        }
        write(new OutputStreamWriter(out), dataset);
    }

    /**
	 * Writes an ARFF file representing the specified dataset.
	 * 
	 * @param writer the output writer.
	 * @param dataset the dataset to be written.
	 * 
	 * @throws IOException
	 */
    public void write(Writer writer, Dataset dataset) throws IOException {
        PrintWriter out = null;
        try {
            Metadata metadata = dataset.getMetadata();
            out = new PrintWriter(writer);
            writeHeader(metadata, out);
            writeData(dataset, out);
        } finally {
            if (out != null) {
                out.close();
            }
        }
    }

    /**
	 * Writes the ARFF header section.
	 * 
	 * @param metadata the metadata information.
	 * @param writer the writer instance.
	 */
    protected void writeHeader(Metadata metadata, PrintWriter writer) {
        writer.println("% ARFF file for " + metadata.getName());
        writer.println("% Generated on " + DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.LONG).format(Calendar.getInstance().getTime()));
        writer.println("%");
        writer.println("@relation " + metadata.getName());
        writer.println();
        for (Attribute attribute : metadata.getPredictor()) {
            writeAttribute(attribute, writer);
        }
        writeTarget(metadata, writer);
        writer.println();
        writer.println("@data");
    }

    protected void writeTarget(Metadata metadata, PrintWriter writer) {
        writeAttribute(metadata.getTarget(), writer);
        writer.println();
        ClassHierarchy hierarchy = metadata.getClassHierarchy();
        if (hierarchy != null) {
            LinkedList<Node> nodes = new LinkedList<Node>();
            LinkedList<Node> printed = new LinkedList<Node>();
            nodes.add(hierarchy.getRoot());
            while (!nodes.isEmpty()) {
                Node node = nodes.removeFirst();
                if (!printed.contains(node)) {
                    writer.print("@class " + node.getLabel());
                    if (!node.getParents().isEmpty()) {
                        StringBuffer parents = new StringBuffer();
                        parents.append(" { ");
                        int index = 0;
                        for (Node parent : node.getParents()) {
                            if (index > 0) {
                                parents.append(",");
                            }
                            parents.append(parent.getLabel());
                            index++;
                        }
                        parents.append(" }");
                        writer.println(parents.toString());
                    } else {
                        writer.println();
                    }
                    printed.add(node);
                    nodes.addAll(node.getChildren());
                }
            }
        }
    }

    /**
	 * Writes the ARFF data section.
	 * 
	 * @param dataset the dataset instance.
	 * @param writer the writer instance.
	 */
    protected void writeData(Dataset dataset, PrintWriter writer) {
        for (Instance instance : dataset) {
            writer.println(instance.toString());
        }
    }

    /**
	 * Writes the ARFF attribute information.
	 * 
	 * @param attribute the attribute to write.
	 * @param writer the writer instance.
	 */
    protected void writeAttribute(Attribute attribute, PrintWriter writer) {
        if (attribute instanceof NominalAttribute) {
            NominalAttribute n = (NominalAttribute) attribute;
            writer.print("@attribute " + n.getName().replaceAll("(\\s)+", "_") + " {");
            for (Iterator<String> i = n.getValues().iterator(); i.hasNext(); ) {
                writer.print(i.next());
                if (i.hasNext()) {
                    writer.print(",");
                }
            }
            writer.println("}");
        } else if (attribute instanceof ContinuousAttribute) {
            writer.println("@attribute " + attribute.getName().replaceAll("(\\s)+", "_") + " numeric");
        }
    }

    /**
	 * Checks if the line begin with the comment character ('%').
	 * 
	 * @param line the line to check.
	 * 
	 * @return <code>true</code> if the line begins with the comment
	 *         character ('%'); <code>false</code> otherwise.
	 */
    protected boolean isComment(String line) {
        return line.startsWith("%");
    }

    /**
	 * Checks if the line is blank.
	 * 
	 * @param line the line to check.
	 * 
	 * @return <code>true</code> if the line is blank; <code>false</code>
	 *         otherwise.
	 */
    protected boolean isBlank(String line) {
        return line.trim().equals("");
    }

    /**
	 * Returns a copy of the specified string, with single and double quotes,
	 * as well as leading and trailing spaces omitted.
	 * 
	 * @param s the string to remove single and double quotes, as well as
	 *          leading and trailing spaces.
	 * 
	 * @return A copy of the specified string, with single and double quotes,
	 *         as well as leading and trailing spaces removed.
	 */
    protected String trim(String s) {
        return s.replace('\'', ' ').replace('\"', ' ').trim();
    }

    /**
	 * Returns the default file extension.
	 * 
	 * @return the default file extension.
	 */
    protected String getExtension() {
        return "arff";
    }

    /**
	 * Utility method to convert the specified dataset file to ARFF format.
	 * 
	 * @param args command-line arguments.
	 */
    public static void main(String[] args) throws Exception {
        if (args.length != 1) {
            System.out.println("Usage: " + ArffHelper.class.getName() + " <dataset file>");
            System.exit(1);
        }
        Dataset dataset = Helper.getHelper(args[0]).read(args[0]);
        new ArffHelper().write(new File(args[0]).getParentFile(), dataset);
    }

    /**
	 * This class represents an unsupported attribute (e.g. string or date
	 * attributes).
	 */
    protected static class UnsupportedAttribute extends Attribute {

        private static final long serialVersionUID = -8421608755709992179L;

        /**
		 * Default constructor.
		 */
        public UnsupportedAttribute(String name) {
            super(name);
        }

        @Override
        public void validate(String value) {
        }

        public boolean isMissing(double value) {
            throw new UnsupportedOperationException();
        }
    }
}
