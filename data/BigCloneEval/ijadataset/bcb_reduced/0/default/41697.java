import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;

public class SpideyCC {

    protected static final String EXPRESSIONS_TOKENIZER = " ()+-*%/&|";

    private static final String SPIDEYCC_ROOT = System.getenv("SPIDEYCC_ROOT") + "/";

    private static final String SPIDEY_TEMPLATE_DIR = SPIDEYCC_ROOT + "templates/";

    private static final String SPIDEY_TYPES_TEMPLATE = SPIDEY_TEMPLATE_DIR + "spidey_types.template";

    private static final String SPIDEY_REP_TEMPLATE_CONTIKI = SPIDEY_TEMPLATE_DIR + "spidey_rep.template.contiki";

    private static final String SPIDEY_REP_TEMPLATE_TINYOS = SPIDEY_TEMPLATE_DIR + "spidey_rep.template.tinyos";

    private static final String SPIDEY_TOS_COMPONENT_TEMPLATE = SPIDEY_TEMPLATE_DIR + "LNMessageC.template.tinyos";

    private static final String DEFAULT_DUMP_WORKING_DIR = System.getProperty("user.dir") + "/";

    private static final String ATTR_DUMP = DEFAULT_DUMP_WORKING_DIR + "attributes.spidey.dump";

    private static final String VALUES_DUMP = DEFAULT_DUMP_WORKING_DIR + "values.spidey.dump";

    private static final String IDS_DUMP = DEFAULT_DUMP_WORKING_DIR + "ids.spidey.dump";

    private static final String APP_WORKING_DIR = System.getProperty("user.dir") + "/";

    private static final String SPIDEY_TYPES = APP_WORKING_DIR + "spidey_types.h";

    private static final String COMPILER_TAG = "<compiler_tag>";

    private static final String USE_COST_TAG = "<use_cost>";

    private static final String SPIDEY_USES = "<spidey_uses>";

    private static final String USER_INCLUDE = "<user_include>";

    private static final String SPIDEY_REP_SIZE = "<size>";

    private static final String DEFINITIONS = "<definitions>";

    private static final String SPIDEY_NODE_ATTRIBUTES = "<node_attributes>";

    private static final String SPIDEY_NGH_INSTANCES = "<ngh_instances>";

    private static final String SPIDEY_TOS_USES_WIRINGS = "<spidey_uses_wiring>";

    private static final String SPIDEY_TOS_DATA = "<spidey_data_component>";

    private static final String SPIDEY_COMPONENT_NAME = "<spidey_component_name>";

    private static Set<Attribute> attributes;

    private static Set<Value> values;

    private enum targets {

        CONTIKI, TINYOS, UNDEFINED
    }

    ;

    public static void main(String[] args) {
        Spidey parser = null;
        SpideyCC.targets target = SpideyCC.targets.UNDEFINED;
        boolean cleaning = false;
        try {
            List<String> fileNames = new LinkedList<String>();
            for (int i = 0; i < args.length; i++) {
                if (args[i].equals("-contiki")) {
                    target = targets.CONTIKI;
                } else if (args[i].equals("-tinyos")) {
                    target = targets.TINYOS;
                } else if (args[i].equals("-clean")) {
                    cleaning = true;
                } else {
                    fileNames.add(args[i]);
                }
            }
            if (target == SpideyCC.targets.UNDEFINED || fileNames.size() == 0) {
                printUsageAndExit();
            }
            if (SPIDEYCC_ROOT.equals("null/")) {
                printEnvVariableSettings();
            }
            if (cleaning) {
                System.out.println("Cleaning up attribute and value identifiers...");
                new File(ATTR_DUMP).delete();
                new File(VALUES_DUMP).delete();
                new File(IDS_DUMP).delete();
            }
            boolean first = true;
            for (String fileName : fileNames) {
                System.out.print("SpideyCC: Parsing " + fileName + "...");
                if (first) {
                    first = false;
                    parser = new Spidey(new FileReader(fileName));
                } else {
                    Spidey.ReInit(new FileReader(fileName));
                }
                parser.startParsing();
                System.out.println("done!");
            }
            if (new File(ATTR_DUMP).exists() && new File(VALUES_DUMP).exists() && new File(IDS_DUMP).exists()) {
                ObjectInputStream objReader = new ObjectInputStream(new FileInputStream(ATTR_DUMP));
                attributes = (HashSet<Attribute>) objReader.readObject();
                objReader.close();
                objReader = new ObjectInputStream(new FileInputStream(VALUES_DUMP));
                values = (HashSet<Value>) objReader.readObject();
                objReader.close();
                Value.nextSpideyValueId = values.size() + 1;
                Attribute.nextSpideyAttrId = attributes.size() + 1;
                objReader = new ObjectInputStream(new FileInputStream(IDS_DUMP));
                NghInstance.nextSpideyNghId = objReader.readInt();
                NghTemplate.maxPredicates = objReader.readInt();
                NodeInstance.maxLocalAttr = objReader.readInt();
                objReader.close();
            } else if (new File(ATTR_DUMP).exists() || new File(VALUES_DUMP).exists() || new File(IDS_DUMP).exists()) {
                printDumpFileCleanAndExit();
            } else {
                System.out.println("Starting with fresh attribute and value identifiers!");
                attributes = new HashSet<Attribute>();
                values = new HashSet<Value>();
            }
            for (NodeTemplate templ : Spidey.nodesTemplate.values()) {
                for (NodeAttribute nodeAttr : templ.getAttributes()) {
                    attributes.add(new Attribute(nodeAttr.getName()));
                }
            }
            for (NodeInstance nodeInstance : Spidey.nodesData.values()) {
                for (NodeAttributeBinding attrBinding : nodeInstance.getBindings()) {
                    Attribute attr = lookUpAttribute(attrBinding.getNodeAttribute().getName());
                    values.add(new Value(attr, attrBinding.getValue()));
                }
            }
            for (NghInstance nghInstance : Spidey.nghInstances.values()) {
                for (NghPredicate nghPredicate : nghInstance.getTemplate().getPredicates()) {
                    attributes.add(new Attribute(nghPredicate.getAttributeName()));
                    Attribute attr = lookUpAttribute(nghPredicate.getAttributeName());
                    if (!nghPredicate.isParameter()) {
                        values.add(new Value(attr, nghPredicate.getValue()));
                    } else {
                        values.add(new Value(attr, nghInstance.getActualValue(nghPredicate.getValue())));
                    }
                }
            }
            String s = null;
            BufferedReader reader = new BufferedReader(new FileReader(SPIDEY_TYPES_TEMPLATE));
            BufferedWriter writer = new BufferedWriter(new FileWriter(SPIDEY_TYPES));
            while ((s = reader.readLine()) != null) {
                s = s.concat("\n");
                if (s.indexOf(COMPILER_TAG) != -1) {
                    writer.write(generateCompilerTag());
                } else if (s.indexOf(DEFINITIONS) != -1) {
                    writeAttributeNames(writer);
                    writer.write("\n");
                    writeAttributeValues(writer);
                    writer.write("\n");
                    writeNghNames(writer);
                } else {
                    writer.write(s);
                }
            }
            writer.flush();
            writer.close();
            if (target == targets.CONTIKI) {
                generateContikiTarget();
            } else if (target == targets.TINYOS) {
                generateTinyOSTarget();
            }
            System.out.println("Don't forget to set \n" + "\t MAX_NGH_PREDICATES = " + NghTemplate.maxPredicates + "\n" + "\t LOCAL_ATTRIBUTES = " + NodeInstance.maxLocalAttr);
            ObjectOutputStream objWriter = new ObjectOutputStream(new FileOutputStream(ATTR_DUMP));
            objWriter.writeObject(attributes);
            objWriter.flush();
            objWriter.close();
            objWriter = new ObjectOutputStream(new FileOutputStream(VALUES_DUMP));
            objWriter.writeObject(values);
            objWriter.flush();
            objWriter.close();
            objWriter = new ObjectOutputStream(new FileOutputStream(IDS_DUMP));
            objWriter.writeInt(NghInstance.nextSpideyNghId);
            objWriter.writeInt(NghTemplate.maxPredicates);
            objWriter.writeInt(NodeInstance.maxLocalAttr);
            objWriter.flush();
            objWriter.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    private static void generateContikiTarget() throws IOException {
        String s = null;
        for (NodeInstance node : Spidey.nodesData.values()) {
            BufferedReader reader = new BufferedReader(new FileReader(SPIDEY_REP_TEMPLATE_CONTIKI));
            BufferedWriter writer = new BufferedWriter(new FileWriter(APP_WORKING_DIR + "spidey_" + node.getId() + ".c"));
            while ((s = reader.readLine()) != null) {
                s = s.concat("\n");
                if (s.indexOf(COMPILER_TAG) != -1) {
                    writer.write(generateCompilerTag());
                } else if (s.indexOf(USER_INCLUDE) != -1) {
                    writer.write(Spidey.include);
                } else if (s.indexOf(USE_COST_TAG) != -1) {
                    writer.write(s.replaceAll(USE_COST_TAG + "\n", node.getUseCost()) + ";\n");
                } else if (s.indexOf(SPIDEY_REP_SIZE) != -1) {
                    writer.write("#define SIZE " + node.getBindings().size() + "\n");
                } else if (s.indexOf(SPIDEY_NODE_ATTRIBUTES) != -1) {
                    int index = 0;
                    for (NodeAttributeBinding binding : node.getBindings()) {
                        writer.write("  if (index == " + index + ") {\n    ret.attribute_id = " + lookUpAttribute(binding.getNodeAttribute().getName()).getCdef() + ";\n    ret.attribute_value = " + lookUpValue(binding.getValue()).getCdef() + ";\n  }\n");
                        index++;
                    }
                } else if (s.indexOf(SPIDEY_NGH_INSTANCES) != -1) {
                    for (NghInstance nghInstance : Spidey.nghInstances.values()) {
                        writer.write("  if (neighborhood_id == " + nghInstance.getSpideyId() + ") {\n\n");
                        int actualPredicates = 0;
                        for (NghPredicate nghPredicate : nghInstance.getTemplate().getPredicates()) {
                            String valueCdef = null;
                            if (nghPredicate.isParameter()) {
                                valueCdef = lookUpValue(nghInstance.getActualValue(nghPredicate.getValue())).getCdef();
                            } else {
                                valueCdef = lookUpValue(nghPredicate.getValue()).getCdef();
                            }
                            writer.write("    ln_predicate.attribute_id = " + lookUpAttribute(nghPredicate.getAttributeName()).getCdef() + ";\n    ln_predicate.op_code = " + nghPredicate.getOp() + ";\n    ln_predicate.attribute_value = " + valueCdef + ";\n    ngh.predicates[" + actualPredicates + "] = ln_predicate;\n\n");
                            actualPredicates++;
                        }
                        for (int j = actualPredicates; j < NghTemplate.maxPredicates; j++) {
                            writer.write("    ngh.predicates[" + j + "] = null_predicate;\n");
                        }
                        writer.write("  }\n");
                    }
                } else {
                    writer.write(s);
                }
            }
            writer.flush();
            writer.close();
        }
    }

    private static void generateTinyOSTarget() throws IOException {
        String s = null;
        for (NodeInstance node : Spidey.nodesData.values()) {
            BufferedReader reader = new BufferedReader(new FileReader(SPIDEY_REP_TEMPLATE_TINYOS));
            new File(APP_WORKING_DIR + node.getId() + "/").mkdir();
            BufferedWriter writer = new BufferedWriter(new FileWriter(APP_WORKING_DIR + node.getId() + "/Spidey_" + node.getId() + ".nc"));
            while ((s = reader.readLine()) != null) {
                s = s.concat("\n");
                if (s.indexOf(COMPILER_TAG) != -1) {
                    writer.write(generateCompilerTag());
                } else if (s.indexOf(USER_INCLUDE) != -1) {
                    writer.write(Spidey.include);
                } else if (s.indexOf(SPIDEY_COMPONENT_NAME) != -1) {
                    writer.write("module Spidey_" + node.getId() + " {");
                } else if (s.indexOf(SPIDEY_USES) != -1) {
                    writer.write(Spidey.nesCuses);
                } else if (s.indexOf(USE_COST_TAG) != -1) {
                    writer.write(s.replaceAll(USE_COST_TAG + "\n", node.getUseCost()) + ";\n");
                } else if (s.indexOf(SPIDEY_REP_SIZE) != -1) {
                    writer.write("#define SIZE " + node.getBindings().size() + "\n");
                } else if (s.indexOf(SPIDEY_NODE_ATTRIBUTES) != -1) {
                    int index = 0;
                    for (NodeAttributeBinding binding : node.getBindings()) {
                        writer.write("    if (index_a == " + index + ") {\n      ret.attribute_id = " + lookUpAttribute(binding.getNodeAttribute().getName()).getCdef() + ";\n      ret.attribute_value = " + lookUpValue(binding.getValue()).getCdef() + ";\n    }\n");
                        index++;
                    }
                } else if (s.indexOf(SPIDEY_NGH_INSTANCES) != -1) {
                    for (NghInstance nghInstance : Spidey.nghInstances.values()) {
                        writer.write("    if (neighborhood_id == " + nghInstance.getSpideyId() + ") {\n\n");
                        int actualPredicates = 0;
                        for (NghPredicate nghPredicate : nghInstance.getTemplate().getPredicates()) {
                            String valueCdef = null;
                            if (nghPredicate.isParameter()) {
                                valueCdef = lookUpValue(nghInstance.getActualValue(nghPredicate.getValue())).getCdef();
                            } else {
                                valueCdef = lookUpValue(nghPredicate.getValue()).getCdef();
                            }
                            writer.write("      ln_predicate.attribute_id = " + lookUpAttribute(nghPredicate.getAttributeName()).getCdef() + ";\n      ln_predicate.op_code = " + nghPredicate.getOp() + ";\n      ln_predicate.attribute_value = " + valueCdef + ";\n      ngh.predicates[" + actualPredicates + "] = ln_predicate;\n\n");
                            actualPredicates++;
                        }
                        for (int j = actualPredicates; j < NghTemplate.maxPredicates; j++) {
                            writer.write("      ngh.predicates[" + j + "] = null_predicate;\n");
                        }
                        writer.write("    }\n");
                    }
                } else {
                    writer.write(s);
                }
            }
            writer.flush();
            writer.close();
            reader = new BufferedReader(new FileReader(SPIDEY_TOS_COMPONENT_TEMPLATE));
            writer = new BufferedWriter(new FileWriter(APP_WORKING_DIR + node.getId() + "/LNMessageC.nc"));
            while ((s = reader.readLine()) != null) {
                s = s.concat("\n");
                if (s.indexOf(COMPILER_TAG) != -1) {
                    writer.write(generateCompilerTag());
                } else if (s.indexOf(USER_INCLUDE) != -1) {
                    writer.write(Spidey.include);
                } else if (s.indexOf(SPIDEY_TOS_DATA) != -1) {
                    writer.write("  components Spidey_" + node.getId() + " as SpideyData;\n");
                } else if (s.indexOf(SPIDEY_USES) != -1) {
                    writer.write(Spidey.nesCuses);
                } else if (s.indexOf(SPIDEY_TOS_USES_WIRINGS) != -1) {
                    StringTokenizer tk = new StringTokenizer(Spidey.nesCuses, " ;\n");
                    while (tk.hasMoreTokens()) {
                        String token = tk.nextToken();
                        if (!token.equals("interface")) {
                            writer.write("  " + token + " = SpideyData;\n");
                        }
                    }
                } else {
                    writer.write(s);
                }
            }
            writer.flush();
            writer.close();
        }
    }

    private static Attribute lookUpAttribute(String attrName) {
        for (Attribute attr : attributes) {
            if (attr.getName().equals(attrName)) {
                return attr;
            }
        }
        return null;
    }

    private static Value lookUpValue(String valName) {
        for (Value val : values) {
            if (val.getValueName().equals(valName)) {
                return val;
            }
        }
        return null;
    }

    private static void writeAttributeNames(BufferedWriter writer) throws IOException {
        for (Attribute attr : attributes) {
            writer.write("#define " + attr.getCdef() + " " + attr.getSpideyId() + "\n");
        }
    }

    private static void writeNghNames(BufferedWriter writer) throws IOException {
        for (NghInstance ngh : Spidey.nghInstances.values()) {
            System.out.println("Coding Spidey for neighborhood " + ngh.getCdef());
            writer.write("#define " + ngh.getCdef() + " " + ngh.getSpideyId() + "\n");
        }
    }

    private static void writeAttributeValues(BufferedWriter writer) throws IOException {
        for (Value v : values) {
            if (v.getValueType() == Value.TYPE_STRING_CONST) {
                writer.write("#define " + v.getCdef() + " " + v.getSpideyId() + "\n");
            } else {
                writer.write("#define " + v.getCdef() + " " + v.getValueName() + "\n");
            }
        }
    }

    private static String generateCompilerTag() {
        DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
        DateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");
        Date today = new Date();
        String nowDate = dateFormat.format(today);
        String nowTime = timeFormat.format(today);
        String ret = "/***\n" + " * * Automatically generated by SpideyToContiki on " + nowDate + " " + nowTime + "\n * * DO NOT EDIT! \n ***/\n";
        return ret;
    }

    private static void printUsageAndExit() {
        System.err.println("Usage: java SpideyCC [-clean] {-contiki | -tinyos} file1.sp file2.sp ...");
        System.exit(-1);
    }

    private static void printDumpFileCleanAndExit() {
        System.err.println("Some dump files appear to be missing: please run again with -clean. Note this may require redeploying your application to avoid misbehaviors.");
        System.exit(-1);
    }

    private static void printEnvVariableSettings() {
        System.err.println("SpideyCC detected missing environment variables. \nMake sure to set SPIDEYCC_ROOT.");
        System.exit(-1);
    }
}
