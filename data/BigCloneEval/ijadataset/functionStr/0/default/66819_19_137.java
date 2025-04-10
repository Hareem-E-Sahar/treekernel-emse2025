public class Test {    public static void main(String[] args) throws UnexpectedOption {
        OptionsParser o = new OptionsParser();
        try {
            o.AddOption("h,help", "Display this help message.");
            o.AddOption("o,output=s", "Write output to specified file.");
            o.AddOption("u,undef", "Generate an undefines file.");
            o.AddOption("v,version", "Show version information.");
            args = o.Parse(args);
        } catch (Exception e) {
            System.out.println("error: " + e.getMessage());
            System.exit(1);
        }
        if (o.Get("help").matched) {
            System.out.println(packageName + " version " + version + ".");
            System.out.println("");
            System.out.println("Usage: java DefinesParser.jar [OPTION]... [FILE]...");
            System.out.println("");
            System.out.println("Generate a verilog include file from a CSV variable");
            System.out.println("definitions file.");
            System.out.println("");
            System.out.println(o.ToString());
            System.out.println("If supplied, input is read from the listed files.");
            System.out.println("By default, input is taken from stdin and output");
            System.out.println("is written to stdout.");
            System.out.println("");
            System.out.println("Written by Adam Shapiro <ams348@cornell.edu>.");
            System.exit(0);
        } else if (o.Get("version").matched) {
            System.out.println(packageName + " version " + version + ".");
            System.out.println("Written by Adam Shapiro.");
            System.out.println("Report bugs to ams348@cornell.edu.");
            System.exit(0);
        }
        HashMap<String, MacroEntry> vars = new HashMap<String, MacroEntry>();
        ArrayList<String> verilog = new ArrayList<String>();
        int errorCount = 0;
        if (args.length > 0) {
            for (String file : args) {
                InputParser in = new InputParser(file);
                errorCount += in.Parse(vars, verilog);
            }
        } else {
            InputParser in = new InputParser();
            errorCount += in.Parse(vars, verilog);
        }
        MacroEntry constantEntry = new MacroEntry();
        constantEntry.print = false;
        constantEntry.file = "";
        constantEntry.line = -1;
        constantEntry.comments = packageName + " constant.";
        try {
            constantEntry.expression = new Expression("3.141592654");
            vars.put("PI", constantEntry);
        } catch (Exceptions.ParserError e) {
        }
        if (errorCount == 0) {
            OutputStream out;
            if (!o.Get("output").matched) out = System.out; else {
                try {
                    out = new FileOutputStream(o.Get("output").stringValue);
                } catch (Exception e) {
                    out = null;
                    System.err.println("Error: unable to open output file '" + o.Get("output").stringValue + "'.");
                }
            }
            if (out != null) {
                PrintStream outWriter = new PrintStream(out);
                String output;
                String date = DateFormat.getDateTimeInstance().format(new Date());
                output = "//Generated by " + packageName + " v" + version + " on " + date + ".\n";
                output += "//This file has been automatically generated.\n";
                output += "//Edit contents with extreme caution.\n\n";
                if (!o.Get("undef").matched && verilog.size() > 0) {
                    for (String s : verilog) {
                        output += s + "\n";
                    }
                    output += "\n";
                }
                HashMap<String, Expression> expList = new HashMap<String, Expression>();
                for (String key : vars.keySet()) {
                    expList.put(key, vars.get(key).expression);
                }
                for (String variable : (new TreeSet<String>(vars.keySet()))) {
                    MacroEntry entry = vars.get(variable);
                    if (o.Get("undef").matched) {
                        output += "`ifdef " + variable + "\n";
                        output += " `undef " + variable + "\n";
                        output += "`endif\n";
                        output += "\n";
                    } else {
                        if (!entry.print) continue;
                        try {
                            if (!entry.comments.equals("")) {
                                output += "//";
                                output += entry.comments.replaceAll("\\n", "\n//");
                                output += "\n";
                            }
                            output += "`ifndef " + variable + "\n";
                            output += " `define " + variable + " " + entry.expression.Value(expList) + "\n";
                            output += "`endif\n";
                            output += "\n";
                        } catch (ExpressionError e) {
                            errorCount++;
                            e.SetVariable(variable);
                            System.err.println(e.getMessage());
                        }
                    }
                }
                if (errorCount == 1) System.err.println("Found 1 error."); else if (errorCount > 0) System.err.println("Found " + String.valueOf(errorCount) + " errors."); else outWriter.print(output);
                try {
                    if (o.Get("output").matched) out.close();
                } catch (IOException e) {
                }
            }
        } else {
            if (errorCount == 1) System.err.println("Found 1 error."); else if (errorCount > 0) System.err.println("Found " + String.valueOf(errorCount) + " errors.");
        }
        if (errorCount > 0) System.exit(1);
    }
}