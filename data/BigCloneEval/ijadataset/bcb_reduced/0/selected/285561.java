package uk.org.ogsadai.dqp.lqp.util;

import java.io.BufferedInputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import java.util.Map;
import java.util.UUID;
import uk.org.ogsadai.dqp.lqp.Annotation;
import uk.org.ogsadai.dqp.lqp.Attribute;
import uk.org.ogsadai.dqp.lqp.Heading;
import uk.org.ogsadai.dqp.lqp.Operator;
import uk.org.ogsadai.dqp.lqp.Predicate;
import uk.org.ogsadai.dqp.lqp.ScanOperator;
import uk.org.ogsadai.dqp.lqp.operators.AbstractJoinOperator;
import uk.org.ogsadai.dqp.lqp.operators.ApplyOperator;
import uk.org.ogsadai.dqp.lqp.operators.GroupByOperator;
import uk.org.ogsadai.dqp.lqp.operators.ProjectOperator;
import uk.org.ogsadai.dqp.lqp.operators.RenameOperator;
import uk.org.ogsadai.dqp.lqp.operators.ScanBindApplyOperator;
import uk.org.ogsadai.dqp.lqp.operators.SelectOperator;
import uk.org.ogsadai.dqp.lqp.operators.SortOperator;
import uk.org.ogsadai.dqp.lqp.operators.TableScanOperator;
import uk.org.ogsadai.dqp.lqp.udf.Function;
import uk.org.ogsadai.dqp.lqp.udf.LogicalFunction;
import uk.org.ogsadai.resource.ResourceID;

/**
 * Generates a DOT file for an operator tree.
 * 
 * @author The OGSA-DAI Project Team.
 */
public class DOTFileGenerator {

    /** Copyright notice. */
    private static final String COPYRIGHT_NOTICE = "Copyright (c) The University of Edinburgh, 2008";

    /** Current index used to number nodes in dot files. */
    private int mCurrentIdx;

    /**
     * Writes a query plan to a file in dot format and runs a command line
     * command afterwards.  This is typically used to write the dot file and
     * then run a command line to convert the dot file to another format,
     * e.g. png.
     * <p>
     * The filename and command are specified as template strings.  In these
     * templates any occurrences of <tt>REQUESTID</tt> will be replaced by the 
     * request resource ID and any occurrences of <tt>UUID</tt> will be replaced
     * by a universally unique identifier.  Within this call occurrences of 
     * <tt>UUID</tt> in either template will be replaced by the same identifier.
     * 
     * @param root             query plan root
     * @param inclHeading      should the header be included?
     * @param requestID        ID of the request resource
     * @param filenameTemplate dot filename template
     * @param commandTemplate  command template.
     * 
     * @throws IOException if an IO error occurs.
     */
    public void writeDotToFileAndRunCommand(Operator root, boolean inclHeading, ResourceID requestID, String filenameTemplate, String commandTemplate) throws IOException {
        UUID uuid = UUID.randomUUID();
        String filename = replaceStringTemplates(filenameTemplate, requestID, uuid);
        String command = replaceStringTemplates(commandTemplate, requestID, uuid);
        writeDotToFile(root, inclHeading, new File(filename));
        runCommand(command);
    }

    /**
     * Runs the given command.
     * 
     * @param command command to run
     * 
     * @throws IOException if an IO error occurs.
     */
    public void runCommand(String command) throws IOException {
        final Process p = Runtime.getRuntime().exec(command);
        Thread streamGobbler = new Thread() {

            @Override
            public void run() {
                try {
                    InputStream is = new BufferedInputStream(p.getInputStream());
                    while (is.read() != -1) {
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
        streamGobbler.start();
        try {
            streamGobbler.join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Writes the query plan in dot format to a file whose name is specified
     * by the given file name template.  Any occurrences of <tt>REQUESTID</tt>
     * in the filename will be replaced by the request resource ID and
     * any occurrences of <tt>UUID</tt> will be replaced by a universally unique
     * identifier.
     * 
     * @param root             query plan root
     * @param inclHeading      should the header be included?
     * @param requestID        ID of the request resource
     * @param filenameTemplate template of filename to write to
     * 
     * @throws IOException if an IO error occurs.
     */
    public void writeDotToFile(Operator root, boolean inclHeading, ResourceID requestID, String filenameTemplate) throws IOException {
        writeDotToFile(root, inclHeading, new File(replaceStringTemplates(filenameTemplate, requestID, UUID.randomUUID())));
    }

    /**
     * Writes the query plan in dot format to a file.
     * 
     * @param root         query plan root
     * @param inclHeading  should the header be included?
     * @param file         file to write to
     * 
     * @throws IOException if an IO error occurs.
     */
    public void writeDotToFile(Operator root, boolean inclHeading, File file) throws IOException {
        Writer writer = new BufferedWriter(new FileWriter(file));
        try {
            writeDot(root, inclHeading, writer);
        } catch (IOException e) {
            throw e;
        } finally {
            writer.close();
        }
    }

    /**
     * Writes the query plan in dot format.
     * 
     * @param root         query plan root
     * @param inclHeading  should the header be included?
     * @param writer       writer to write to
     * 
     * @throws IOException if an IO error occurs.
     */
    public void writeDot(Operator root, boolean inclHeading, Writer writer) throws IOException {
        mCurrentIdx = 0;
        writer.write("digraph simpleDotFile {\nnode [shape=Mrecord,fontsize=10];\n");
        writeDot(root, mCurrentIdx, inclHeading, writer);
        writer.write("}\n");
    }

    /**
     * Generates the dot file.
     * 
     * @param root         local query plan root
     * @param rootIndex    index used to name the file
     * @param inclHeading  should the header be included?
     * @param writer       writer to write to
     * 
     * @throws IOException if an IO error occurs.
     */
    private void writeDot(Operator root, int rootIndex, boolean inclHeading, Writer writer) throws IOException {
        writer.write(rootIndex + "\t[label=\"{{");
        Heading head = root.getHeading();
        if (head != null && inclHeading) for (int i = 0; i < head.getAttributes().size(); i++) {
            writer.write("" + head.getAttributes().get(i).getName());
            writer.write("\\l");
            writer.write("" + head.getAttributes().get(i).getType());
            writer.write("\\l");
            String source = head.getAttributes().get(i).getSource();
            writer.write(source == null ? "null" : source);
            writer.write("\\l");
            writer.write(head.getAttributes().get(i).isKey() ? "key" : "non-key");
            writer.write("\\l");
            if (i != head.getAttributes().size() - 1) {
                writer.write("|");
            }
        }
        if (root instanceof AbstractJoinOperator) {
            String condidtionSQL = ((AbstractJoinOperator) root).getPredicate().toString();
            condidtionSQL = condidtionSQL.replaceAll(">", "\\\\>");
            condidtionSQL = condidtionSQL.replaceAll("<", "\\\\<");
            writer.write("}|{" + condidtionSQL);
            Map<String, Object> annotations = root.getAnnotations();
            writer.write("} | { ");
            boolean first = true;
            for (Map.Entry<String, Object> entry : annotations.entrySet()) {
                if (entry.getKey().equals(Annotation.EVALUATION_NODE) || entry.getKey().equals(Annotation.DATA_NODE) || entry.getKey().equals(Annotation.CARDINALITY) || entry.getKey().equals(Annotation.PARTITION)) {
                    continue;
                }
                if (!first) writer.write("\\n ");
                writer.write(entry.getKey());
                writer.write(" = ");
                Object value = entry.getValue();
                if (value != null) {
                    writer.write(entry.getValue().toString());
                } else {
                    writer.write("null");
                }
                if (entry.getKey().equals(Annotation.READ_FIRST)) {
                    int child = 0;
                    if (!entry.getValue().toString().equalsIgnoreCase("left")) {
                        child = 1;
                    }
                    writer.write(" (" + root.getChild(child).hashCode() + ")");
                }
                first = false;
            }
        }
        if (root instanceof ProjectOperator) {
            ProjectOperator project = (ProjectOperator) root;
            int i = 0;
            writer.write("}|{");
            for (String attr : project.getAttributeDefs()) {
                writer.write("[" + (i++) + "] " + attr + "\\l");
            }
        }
        if (root instanceof GroupByOperator) {
            GroupByOperator groupBy = (GroupByOperator) root;
            writer.write("}|{");
            for (Function aggregate : groupBy.getAggregates()) {
                writer.write("[" + ((LogicalFunction) aggregate).getHeading().getAttributes().get(0).getName() + "] " + aggregate.toSQL() + "\\l");
            }
        }
        if (root instanceof SelectOperator) {
            SelectOperator selectOperator = (SelectOperator) root;
            String condidtionSQL = null;
            condidtionSQL = selectOperator.getPredicate().toString();
            condidtionSQL = condidtionSQL.replaceAll(">", "\\\\>");
            condidtionSQL = condidtionSQL.replaceAll("<", "\\\\<");
            writer.write("}|{" + condidtionSQL);
        }
        if (root instanceof TableScanOperator) {
            String query = ((TableScanOperator) root).getQuery().getSQLQuery();
            query = query.replaceAll(">", "\\\\>");
            query = query.replaceAll("<", "\\\\<");
            StringBuilder sbdr = new StringBuilder();
            int lineBrkCnt = 0;
            for (int i = 0; i < query.length(); i++) {
                if (query.charAt(i) == ' ' && i / 30 > lineBrkCnt) {
                    sbdr.append(" \\l");
                    lineBrkCnt++;
                } else {
                    sbdr.append(query.charAt(i));
                }
            }
            writer.write("}|{" + sbdr.toString() + "\\l");
        }
        if (root instanceof RenameOperator) {
            String origMapStr = ((RenameOperator) root).getRenameMap().getOriginalAttributeList().toString();
            String renmMapStr = ((RenameOperator) root).getRenameMap().getRenamedAttributeList().toString();
            writer.write("}|{" + addLineBreaks(origMapStr, 30) + "\\n" + addLineBreaks(renmMapStr, 30));
        }
        if (root instanceof SortOperator) {
            SortOperator sort = (SortOperator) root;
            int i = 0;
            writer.write("}|{");
            for (Attribute attr : sort.getSortAttributes()) {
                writer.write("[" + (i++) + "] " + attr + "\\l");
            }
        }
        if (root instanceof ApplyOperator) {
            Operator wrappedOperator = ((ApplyOperator) root).getOperator();
            writer.write("}|{" + ((ApplyOperator) root).getOperator().getID() + "\\n");
            if (wrappedOperator instanceof AbstractJoinOperator) {
                String condidtionSQL = ((AbstractJoinOperator) wrappedOperator).getPredicate().toString();
                condidtionSQL = condidtionSQL.replaceAll(">", "\\\\>");
                condidtionSQL = condidtionSQL.replaceAll("<", "\\\\<");
                writer.write(condidtionSQL + "\\n");
            }
            for (Attribute attr : ((ApplyOperator) root).getAttributesToBind()) {
                writer.write("\\{" + attr + "\\}\\n");
            }
            if (root instanceof ScanBindApplyOperator) {
                for (Predicate p : ((ScanBindApplyOperator) root).getBindingPredicates()) writer.write("\\[" + p + "\\]\\n");
            }
        }
        writer.write("}|{" + root.getID() + " (" + root.hashCode() + ") " + root.getResultCardinality() + "\\n" + Annotation.getEvaluationNodeAnnotation(root) + "\\n");
        if (root instanceof ScanOperator) {
            writer.write("" + ((ScanOperator) root).getDataNode());
        } else {
            writer.write("" + Annotation.getDataNodeAnnotation(root));
        }
        writer.write("}}\"];\n");
        if (root.isBinary()) {
            int leftIdx = ++mCurrentIdx;
            int rightIdx = ++mCurrentIdx;
            writer.write(rootIndex + "\t->\t" + leftIdx + ";\n");
            writer.write(rootIndex + "\t->\t" + rightIdx + ";\n");
            writeDot(root.getChild(0), leftIdx, inclHeading, writer);
            writeDot(root.getChild(1), rightIdx, inclHeading, writer);
        } else {
            int leftIdx = ++mCurrentIdx;
            Operator op = root.getChild(0);
            if (op != null) {
                writer.write(rootIndex + "\t->\t" + leftIdx + ";\n");
                writeDot(root.getChild(0), leftIdx, inclHeading, writer);
            }
        }
    }

    /**
     * Replaces certain keywords in a string.  Any occurrences of 
     * <tt>REQUESTID</tt> in the string will be replaced by the request resource
     * ID and any occurrences of <tt>UUID</tt> will be replaced by a universally
     * unique identifier.
     * 
     * @param stringTemplate template string to be replaced
     * @param requestID      ID of the request resource
     * @param uuid           uuid to replace any <tt>UUID</tt> keyword
     * 
     * @return the template string with any occurrences of the keywords 
     *         replaced.
     * 
     * @throws IOException if an IO error occurs.
     */
    private String replaceStringTemplates(String stringTemplate, ResourceID requestID, UUID uuid) {
        String result = stringTemplate;
        if (result.indexOf("REQUESTID") != -1) {
            result = result.replaceAll("REQUESTID", requestID.toString());
        }
        if (result.indexOf("UUID") != -1) {
            result = result.replaceAll("UUID", uuid.toString());
        }
        return result;
    }

    /**
     * Adds line breaks.
     * 
     * @param inString
     * @param breakCol
     * @return
     */
    private String addLineBreaks(String inString, int breakCol) {
        int lineBrkCnt = 0;
        StringBuilder sbdr = new StringBuilder();
        for (int i = 0; i < inString.length(); i++) {
            if (inString.charAt(i) == ' ' && i / breakCol > lineBrkCnt) {
                sbdr.append(" \\l");
                lineBrkCnt++;
            } else {
                sbdr.append(inString.charAt(i));
            }
        }
        return sbdr.toString();
    }
}
