package jeplus;

import java.beans.XMLDecoder;
import java.beans.XMLEncoder;
import java.io.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.tree.DefaultMutableTreeNode;
import jeplus.data.ExecutionOptions;
import jeplus.data.ParameterItem;
import jeplus.data.RandomSource;
import jeplus.data.RouletteWheel;
import jeplus.util.CsvUtil;
import jeplus.util.RelativeDirUtil;
import org.apache.commons.math3.distribution.NormalDistribution;
import org.apache.commons.math3.distribution.TriangularDistribution;
import org.apache.commons.math3.distribution.UniformIntegerDistribution;
import org.apache.commons.math3.distribution.UniformRealDistribution;

/**
 * JEPlus Project class encapsulates definition of a project
 * @author Yi Zhang
 * @version 1.0
 * @since 1.0
 */
public class JEPlusProject implements Serializable {

    private static final long serialVersionUID = -3920321004466467177L;

    public static final int EPLUS = 0;

    public static final int TRNSYS = 1;

    /** This is the working directory of the program */
    protected static String UserBaseDir = System.getProperty("user.dir") + File.separator;

    /** Flag marking whether this project has been changed since last save/load */
    protected transient boolean ContentChanged = false;

    /** Base directory of the project, i.e. the location where the project file is saved */
    protected transient String BaseDir = null;

    /** Project Type: E+ or TRNSYS */
    protected int ProjectType = -1;

    /** Project ID string */
    protected String ProjectID = null;

    /** Project notes string */
    protected String ProjectNotes = null;

    /** Local directory for IDF template files */
    protected String IDFDir = null;

    /** Template file to be used in this job; or a (';' delimited) list of files for the batch project */
    protected String IDFTemplate = null;

    /** Local directory for weather files */
    protected String WeatherDir = null;

    /** Weather file to be used in this job; or a (';' delimited) list of files for the batch project */
    protected String WeatherFile = null;

    /** Flag for calling ReadVarsESO or not */
    protected boolean UseReadVars = false;

    /** ReadVarsESO configure file to be used to extract results */
    protected String RVIDir = null;

    /** ReadVarsESO configure file to be used to extract results */
    protected String RVIFile = null;

    /** Local directory for DCK/TRD (for TRNSYS) template files */
    protected String DCKDir = null;

    /** Template file to be used in this job; or a (';' delimited) list of files for the batch project */
    protected String DCKTemplate = null;

    /** Output file names that contain results for each simulation; used for TRNSYS */
    protected String OutputFileNames = null;

    /** Execution settings */
    protected ExecutionOptions ExecSettings = null;

    /** Parameter tree */
    protected DefaultMutableTreeNode ParamTree = null;

    /** Job list in string format */
    protected String[][] StrJobList = null;

    /** Job list in index format */
    protected int[][] IdxJobList = null;

    /**
     * Default constructor
     */
    public JEPlusProject() {
        ProjectType = EPLUS;
        ProjectID = "G";
        ProjectNotes = "New project";
        IDFDir = "./";
        IDFTemplate = "select files ...";
        WeatherDir = "./";
        WeatherFile = "select files ...";
        UseReadVars = true;
        RVIDir = "./";
        RVIFile = "select a file ...";
        DCKDir = "./";
        DCKTemplate = "select a file ...";
        OutputFileNames = "trnsysout.csv";
        ExecSettings = new ExecutionOptions();
        ParamTree = new DefaultMutableTreeNode(new ParameterItem());
    }

    /**
     * Cloning constructor. New project state is set to 'changed' after cloning
     * @param proj Project object to be cloned
     */
    public JEPlusProject(JEPlusProject proj) {
        ContentChanged = true;
        BaseDir = proj.BaseDir;
        ProjectType = proj.ProjectType;
        ProjectID = proj.ProjectID;
        ProjectNotes = proj.ProjectNotes;
        IDFDir = proj.IDFDir;
        IDFTemplate = proj.IDFTemplate;
        WeatherDir = proj.WeatherDir;
        WeatherFile = proj.WeatherFile;
        UseReadVars = proj.UseReadVars;
        RVIDir = proj.RVIDir;
        RVIFile = proj.RVIFile;
        DCKDir = proj.DCKDir;
        DCKTemplate = proj.DCKTemplate;
        OutputFileNames = proj.OutputFileNames;
        ExecSettings = new ExecutionOptions(proj.ExecSettings);
        ParamTree = proj.ParamTree;
    }

    /**
     * Construct a project object from an external .jep file
     * @param projectfile The project file (.jep) to be loaded
     */
    public JEPlusProject(String projectfile) {
        this(loadAsXML(new File(projectfile)));
    }

    /**
     * Save the project to an object file
     * @param fn The File object associated with the file to which the contents will be saved
     * @return Successful or not
     */
    public static boolean serialize(File fn, JEPlusProject proj) {
        boolean success = true;
        try {
            ObjectOutputStream ow = new ObjectOutputStream(new FileOutputStream(fn));
            ow.writeObject(proj);
            ow.close();
        } catch (IOException ioe) {
            ioe.printStackTrace();
            success = false;
        }
        return success;
    }

    /**
     * Read parameter tree from an object file
     * @param fn The File object associated with the file
     * @return de-serialised object
     */
    public static JEPlusProject deserialize(File fn) {
        JEPlusProject proj = null;
        try {
            ObjectInputStream or = new ObjectInputStream(new FileInputStream(fn));
            proj = (JEPlusProject) or.readObject();
            or.close();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return proj;
    }

    /**
     * Save this project to an XML file
     * @param fn The File object associated with the file to which the contents will be saved
     * @return Successful or not
     */
    public static boolean saveAsXML(File fn, JEPlusProject proj) {
        boolean success = true;
        XMLEncoder encoder;
        try {
            encoder = new XMLEncoder(new BufferedOutputStream(new FileOutputStream(fn)));
            encoder.writeObject(proj);
            encoder.close();
            String dir = fn.getAbsoluteFile().getParent();
            dir = dir.concat(dir.endsWith(File.separator) ? "" : File.separator);
            proj.setBaseDir(dir);
        } catch (FileNotFoundException ex) {
            Logger.getLogger(JEPlusProject.class.getName()).log(Level.SEVERE, null, ex);
            success = false;
        }
        return success;
    }

    /**
     * Read a project from an XML file. The members of this project are not updated.
     * @param fn The File object associated with the file
     * @return a new project instance from the file
     */
    public static JEPlusProject loadAsXML(File fn) {
        try {
            XMLDecoder decoder = new XMLDecoder(new BufferedInputStream(new FileInputStream(fn)));
            JEPlusProject proj = ((JEPlusProject) decoder.readObject());
            decoder.close();
            String dir = fn.getAbsoluteFile().getParent();
            dir = dir.concat(dir.endsWith(File.separator) ? "" : File.separator);
            proj.setBaseDir(dir);
            return proj;
        } catch (Exception ex) {
            Logger.getLogger(JEPlusProject.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    public String getBaseDir() {
        return BaseDir;
    }

    public void setBaseDir(String BaseDir) {
        this.BaseDir = BaseDir;
    }

    public int getProjectType() {
        return ProjectType;
    }

    public void setProjectType(int ProjectType) {
        this.ProjectType = ProjectType;
    }

    public String getProjectID() {
        return ProjectID;
    }

    public void setProjectID(String ProjectID) {
        this.ProjectID = ProjectID;
    }

    public String getProjectNotes() {
        return ProjectNotes;
    }

    public void setProjectNotes(String ProjectNotes) {
        this.ProjectNotes = ProjectNotes;
    }

    public ExecutionOptions getExecSettings() {
        return ExecSettings;
    }

    public void setExecSettings(ExecutionOptions ExecSettings) {
        this.ExecSettings = ExecSettings;
    }

    public String getIDFDir() {
        return IDFDir;
    }

    public void setIDFDir(String IDFDir) {
        this.IDFDir = IDFDir;
    }

    public String getIDFTemplate() {
        return IDFTemplate;
    }

    public void setIDFTemplate(String IDFTemplate) {
        this.IDFTemplate = IDFTemplate;
    }

    public DefaultMutableTreeNode getParamTree() {
        return ParamTree;
    }

    public void setParamTree(DefaultMutableTreeNode ParamTree) {
        this.ParamTree = ParamTree;
    }

    public String getRVIDir() {
        return RVIDir;
    }

    public void setRVIDir(String RVIDir) {
        this.RVIDir = RVIDir;
    }

    public String getRVIFile() {
        return RVIFile;
    }

    public void setRVIFile(String RVIFile) {
        this.RVIFile = RVIFile;
    }

    public boolean isUseReadVars() {
        return UseReadVars;
    }

    public void setUseReadVars(boolean UseReadVars) {
        this.UseReadVars = UseReadVars;
    }

    public String getDCKDir() {
        return DCKDir;
    }

    public void setDCKDir(String DCKDir) {
        this.DCKDir = DCKDir;
    }

    public String getDCKTemplate() {
        return DCKTemplate;
    }

    public void setDCKTemplate(String DCKTemplate) {
        this.DCKTemplate = DCKTemplate;
    }

    public String getOutputFileNames() {
        return OutputFileNames;
    }

    public void setOutputFileNames(String OutputFileNames) {
        this.OutputFileNames = OutputFileNames;
    }

    public String getWeatherDir() {
        return WeatherDir;
    }

    public void setWeatherDir(String WeatherDir) {
        this.WeatherDir = WeatherDir;
    }

    public String getWeatherFile() {
        return WeatherFile;
    }

    public void setWeatherFile(String WeatherFile) {
        this.WeatherFile = WeatherFile;
    }

    public void setStrJobList(String[][] list) {
        StrJobList = list;
    }

    public void setIdxJobList(int[][] list) {
        IdxJobList = list;
    }

    /** 
     * Resolve the path to the project's work (a.k.a. parent) directory. If
     * relative path is used, it is relative to the project folder
     */
    public String resolveWorkDir() {
        String dir = RelativeDirUtil.checkAbsolutePath(ExecSettings.getWorkDir(), BaseDir);
        dir = dir.concat(dir.endsWith(File.separator) ? "" : File.separator);
        return dir;
    }

    /** 
     * Resolve the path to the PBS script to use for running this project. If
     * relative path is used, it is relative to the UserBaseDir rather than
     * the project folder
     */
    public String resolvePBSscriptFile() {
        return RelativeDirUtil.checkAbsolutePath(ExecSettings.getPBSscriptFile(), UserBaseDir);
    }

    /** 
     * Resolve the path to the server config file for running this project. If
     * relative path is used, it is relative to the UserBaseDir rather than
     * the project folder
     */
    public String resolveServerConfigFile() {
        return RelativeDirUtil.checkAbsolutePath(ExecSettings.getServerConfigFile(), UserBaseDir);
    }

    /** 
     * Resolve the path to the IDF models of this project. If
     * relative path is used, it is relative to the project folder
     */
    public String resolveIDFDir() {
        String dir = RelativeDirUtil.checkAbsolutePath(this.getIDFDir(), BaseDir);
        dir = dir.concat(dir.endsWith(File.separator) ? "" : File.separator);
        return dir;
    }

    /** 
     * Resolve the path to the RVI file of this project. If
     * relative path is used, it is relative to the project folder
     */
    public String resolveRVIDir() {
        String dir = RelativeDirUtil.checkAbsolutePath(this.getRVIDir(), BaseDir);
        dir = dir.concat(dir.endsWith(File.separator) ? "" : File.separator);
        return dir;
    }

    /** 
     * Resolve the path to the weather files of this project. If
     * relative path is used, it is relative to the project folder
     */
    public String resolveWeatherDir() {
        String dir = RelativeDirUtil.checkAbsolutePath(this.getWeatherDir(), BaseDir);
        dir = dir.concat(dir.endsWith(File.separator) ? "" : File.separator);
        return dir;
    }

    /** 
     * Resolve the path to the RVI file of this project. If
     * relative path is used, it is relative to the project folder
     */
    public String resolveDCKDir() {
        String dir = RelativeDirUtil.checkAbsolutePath(this.getDCKDir(), BaseDir);
        dir = dir.concat(dir.endsWith(File.separator) ? "" : File.separator);
        return dir;
    }

    /**
     * This function copies information from an EPlusWorkEnv object to provide
     * some backwards compatibility
     * @param env the EPlusWorkEnv object
     */
    public void copyFromEnv(EPlusWorkEnv env) {
        IDFDir = env.IDFDir;
        IDFTemplate = env.IDFTemplate;
        WeatherDir = env.WeatherDir;
        WeatherFile = env.WeatherFile;
        UseReadVars = env.UseReadVars;
        RVIDir = env.RVIDir;
        RVIFile = env.RVIFile;
        ProjectType = env.ProjectType;
        DCKDir = env.DCKDir;
        DCKTemplate = env.DCKTemplate;
        OutputFileNames = env.OutputFileNames;
        ExecSettings.setParentDir(env.ParentDir);
        ExecSettings.setKeepEPlusFiles(env.KeepEPlusFiles);
        ExecSettings.setKeepJEPlusFiles(env.KeepJEPlusFiles);
        ExecSettings.setKeepJobDir(env.KeepJobDir);
        ExecSettings.setRerunAll(env.ForceRerun);
    }

    /**
     * This function copies information to an EPlusWorkEnv object to provide
     * some backwards compatibility
     * @param env the EPlusWorkEnv object
     */
    public void resolveToEnv(EPlusWorkEnv env) {
        env.IDFDir = this.resolveIDFDir();
        env.IDFTemplate = IDFTemplate;
        env.WeatherDir = this.resolveWeatherDir();
        env.WeatherFile = WeatherFile;
        env.UseReadVars = UseReadVars;
        env.RVIDir = this.resolveRVIDir();
        env.RVIFile = RVIFile;
        env.ProjectType = ProjectType;
        env.DCKDir = this.resolveDCKDir();
        env.DCKTemplate = DCKTemplate;
        env.OutputFileNames = OutputFileNames;
        env.ParentDir = this.resolveWorkDir();
        env.KeepEPlusFiles = ExecSettings.isKeepEPlusFiles();
        env.KeepJEPlusFiles = ExecSettings.isKeepJEPlusFiles();
        env.KeepJobDir = ExecSettings.isKeepJobDir();
        env.ForceRerun = ExecSettings.isRerunAll();
    }

    /**
     * Decode IDF or Weather files string and store them, with directory, in an array
     * @param dir Default directory for IDF/IMF/EPW/LST files. Entries in the LST files should contain only relative paths to this directory
     * @param files Input files string. ';' delimited list of IDF/IMF/EPW/LST files
     * @return Validation result: true if all files are available
     */
    public ArrayList<String> getFileList(String dir, String files) {
        ArrayList<String> Files = new ArrayList<String>();
        String[] file = files.split("\\s*;\\s*");
        for (int i = 0; i < file.length; i++) {
            if (file[i].length() > 0) {
                if (file[i].toLowerCase().endsWith(".lst")) {
                    Files.addAll(parseListFile(dir, file[i]));
                } else {
                    Files.add(file[i]);
                }
            }
        }
        return Files;
    }

    /**
     * Convert all directories to relative paths to where the project base (the
     * location of the project file, for example) is.
     * @param base_dir The base directory of the project
     * @return conversion successful or not
     */
    protected boolean convertToRelativeDir(File Base) {
        if (Base != null && Base.exists()) {
            File idf = new File(IDFDir);
            File wthr = new File(WeatherDir);
            File rvi = new File(RVIDir);
            File out = new File(ExecSettings.getWorkDir());
            if (idf.exists() && wthr.exists() && rvi.exists() && out.exists()) {
                IDFDir = RelativeDirUtil.getRelativePath(Base, idf);
                WeatherDir = RelativeDirUtil.getRelativePath(Base, wthr);
                RVIDir = RelativeDirUtil.getRelativePath(Base, rvi);
                ExecSettings.setParentDir(RelativeDirUtil.getRelativePath(Base, out));
                return true;
            }
        }
        return false;
    }

    /**
     * Convert all directories to absolute paths.
     * @param base_dir The base directory of the project
     */
    protected void convertToAbsoluteDir(File base) {
        IDFDir = new File(base, IDFDir).getAbsolutePath();
        WeatherDir = new File(base, WeatherDir).getAbsolutePath();
        RVIDir = new File(base, RVIDir).getAbsolutePath();
        ExecSettings.setParentDir(new File(base, ExecSettings.getWorkDir()).getAbsolutePath());
    }

    /**
     * Get a list of search strings from the parameter tree of this project.
     *
     * @return
     */
    public String[] getSearchStrings() {
        DefaultMutableTreeNode ParaTree = this.getParamTree();
        if (ParaTree == null) return null;
        ArrayList<String> SearchStrings = new ArrayList<String>();
        Enumeration nodes = ParaTree.preorderEnumeration();
        while (nodes.hasMoreElements()) {
            Object node = nodes.nextElement();
            String ss = ((ParameterItem) ((DefaultMutableTreeNode) node).getUserObject()).getSearchString();
            if (ss != null && ss.trim().length() > 0 && !SearchStrings.contains(ss)) {
                SearchStrings.add(ss);
            }
        }
        return SearchStrings.toArray(new String[0]);
    }

    /**
     * Parse the list file (for models or weathers) and return result in an
     * ArrayList. The format of a list file must be one input file in each line.
     * "#" and "!" can be used for comment lines.
     * @param dir Directory of the list file
     * @param fn File name
     * @return File list in an List
     */
    protected ArrayList<String> parseListFile(String dir, String fn) {
        BufferedReader fr = null;
        ArrayList<String> list = new ArrayList<String>();
        try {
            fr = new BufferedReader(new FileReader(dir + fn));
            String line = fr.readLine();
            while (line != null) {
                if (line.contains("#")) line = line.substring(0, line.indexOf("#")).trim();
                if (line.contains("!")) line = line.substring(0, line.indexOf("!")).trim();
                if (line.length() > 0) list.add(line);
                line = fr.readLine();
            }
            fr.close();
        } catch (Exception ex) {
            Logger.getLogger(EPlusBatch.class.getName()).log(Level.SEVERE, null, ex);
            try {
                fr.close();
            } catch (IOException ex1) {
                Logger.getLogger(EPlusBatch.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return list;
    }

    /**
     * Import parameters in a CSV table (#-commented) and create a new single-branch tree
     * @param filename File name of the table
     * @return import successful or not
     */
    public boolean importParameterTableFile(File file) {
        String[][] table = CsvUtil.parseCSVwithComments(file);
        if (table != null) {
            ArrayList<ParameterItem> list = new ArrayList<ParameterItem>();
            for (int i = 0; i < table.length; i++) {
                String[] row = table[i];
                if (row.length >= 8) {
                    list.add(new ParameterItem(row));
                }
            }
            addParameterListAsBranch(null, list);
            return true;
        }
        return false;
    }

    /**
     * Add a list of parameter items as a branch at the given root node
     * @param root Root where the new branch is attached
     * @param list The list of parameter items
     */
    public void addParameterListAsBranch(DefaultMutableTreeNode root, ArrayList<ParameterItem> list) {
        if (list != null && list.size() > 0) {
            if (root == null) {
                ParamTree = new DefaultMutableTreeNode(list.get(0));
                DefaultMutableTreeNode current = ParamTree;
                for (int i = 1; i < list.size(); i++) {
                    DefaultMutableTreeNode newnode = new DefaultMutableTreeNode(list.get(i));
                    current.add(newnode);
                    current = newnode;
                }
            } else {
                DefaultMutableTreeNode current = root;
                for (int i = 0; i < list.size(); i++) {
                    DefaultMutableTreeNode newnode = new DefaultMutableTreeNode(list.get(i));
                    current.add(newnode);
                    current = newnode;
                }
            }
        }
    }

    public String[][] getLHSJobList(int LHSsize) {
        String[][] JobList = new String[LHSsize][];
        if (ParamTree != null) {
            DefaultMutableTreeNode thisleaf = ParamTree.getFirstLeaf();
            Object[] path = thisleaf.getUserObjectPath();
            int length = path.length + 3;
            String[][] SampledValues = new String[length][];
            int n_alt = 1;
            n_alt = this.getFileList(this.resolveWeatherDir(), this.getWeatherFile()).size();
            int[] SampledIndex = this.defaultLHSdiscreteSample(LHSsize, n_alt);
            SampledValues[1] = new String[LHSsize];
            for (int j = 0; j < LHSsize; j++) {
                SampledValues[1][j] = Integer.toString(SampledIndex[j]);
            }
            n_alt = this.getFileList(this.resolveIDFDir(), this.getIDFTemplate()).size();
            SampledIndex = this.defaultLHSdiscreteSample(LHSsize, n_alt);
            SampledValues[2] = new String[LHSsize];
            for (int j = 0; j < LHSsize; j++) {
                SampledValues[2][j] = Integer.toString(SampledIndex[j]);
            }
            for (int i = 3; i < length; i++) {
                ParameterItem Param = ((ParameterItem) path[i - 3]);
                if (Param.getValuesString().startsWith("@sample")) {
                    SampledValues[i] = this.defaultLHSdistributionSample(LHSsize, Param.getValuesString(), Param.getType());
                } else {
                    n_alt = Param.getNAltValues();
                    SampledIndex = this.defaultLHSdiscreteSample(LHSsize, n_alt);
                    SampledValues[i] = new String[LHSsize];
                    for (int j = 0; j < LHSsize; j++) {
                        SampledValues[i][j] = Param.getAlternativeValues()[SampledIndex[j]];
                    }
                }
            }
            for (int i = 1; i < length; i++) {
                Collections.shuffle(Arrays.asList(SampledValues[i]), RandomSource.getRandomGenerator());
            }
            for (int i = 0; i < LHSsize; i++) {
                JobList[i] = new String[length];
                JobList[i][0] = new Formatter().format("LHS-%06d", i).toString();
                for (int j = 1; j < length; j++) {
                    JobList[i][j] = SampledValues[j][i];
                }
            }
            return JobList;
        }
        return null;
    }

    private int[] defaultLHSdiscreteSample(int n, int n_alt) {
        int[] index = new int[n];
        if (n_alt > 1) {
            RouletteWheel Wheel = new RouletteWheel(n_alt);
            for (int j = 0; j < n; j++) {
                index[j] = Wheel.spin(j * Wheel.getTotalWidth() / n, (j + 1) * Wheel.getTotalWidth() / n);
            }
        } else {
            for (int j = 0; j < n; j++) index[j] = 0;
        }
        return index;
    }

    private String[] defaultLHSdistributionSample(int n, String funcstr, int type) {
        int start = funcstr.indexOf("(") + 1;
        int end = funcstr.indexOf(")");
        funcstr = funcstr.substring(start, end).trim();
        ArrayList<String> list = new ArrayList<String>();
        String[] params = funcstr.split("\\s*,\\s*");
        String distribution = params[0].toLowerCase();
        if (distribution.equals("uniform") || distribution.equals("u")) {
            double lb = Double.parseDouble(params[1]);
            double ub = Double.parseDouble(params[2]);
            for (int i = 0; i < n; i++) {
                if (type == ParameterItem.DOUBLE) {
                    double bin = (ub - lb) / n;
                    double v = RandomSource.getRandomGenerator().nextDouble() * bin + lb + i * bin;
                    list.add(Double.toString(v));
                } else if (type == ParameterItem.INTEGER) {
                    double bin = (ub + 1. - lb) / n;
                    double v = RandomSource.getRandomGenerator().nextDouble() * bin + lb + i * bin;
                    list.add(Integer.toString((int) Math.floor(v)));
                }
            }
        } else if (distribution.equals("gaussian") || distribution.equals("normal") || distribution.equals("n")) {
            double mean = Double.parseDouble(params[1]);
            double sd = Double.parseDouble(params[2]);
            NormalDistribution Dist = new NormalDistribution(mean, sd);
            double bin = 1.0 / n;
            double a = bin / 10.;
            double b = bin;
            for (int i = 0; i < n; i++) {
                a = Dist.inverseCumulativeProbability(a);
                b = Dist.inverseCumulativeProbability(b);
                double v = RandomSource.getRandomGenerator().nextDouble() * (b - a) + a;
                if (type == ParameterItem.DOUBLE) {
                    list.add(Double.toString(v));
                } else if (type == ParameterItem.INTEGER) {
                    list.add(Long.toString(Math.round(v)));
                }
                a = i * bin;
                b = (i == n - 1) ? (1. - bin / n) : (i + 1) * bin;
            }
        } else if (distribution.equals("triangular") || distribution.equals("tr")) {
            double a = Double.parseDouble(params[1]);
            double c = Double.parseDouble(params[2]);
            double b = Double.parseDouble(params[3]);
            TriangularDistribution Dist = new TriangularDistribution(a, c, b);
            double bin = 1.0 / n;
            a = 0;
            b = bin;
            for (int i = 0; i < n; i++) {
                a = Dist.inverseCumulativeProbability(a);
                b = Dist.inverseCumulativeProbability(b);
                double v = RandomSource.getRandomGenerator().nextDouble() * (b - a) + a;
                if (type == ParameterItem.DOUBLE) {
                    list.add(Double.toString(v));
                } else if (type == ParameterItem.INTEGER) {
                    list.add(Long.toString(Math.round(v)));
                }
                a = i * bin;
                b = (i + 1) * bin;
            }
        } else if (distribution.equals("discrete") || distribution.equals("d")) {
            int nOptions = params.length / 2 - 1;
            String[] options = new String[nOptions];
            double[] probabilities = new double[nOptions];
            double sum = 0;
            for (int i = 0; i < nOptions; i++) {
                options[i] = params[2 * i + 1];
                try {
                    probabilities[i] = Double.parseDouble(params[2 * i + 2]);
                } catch (NumberFormatException nfe) {
                    probabilities[i] = 0.1;
                }
                sum += probabilities[i];
            }
            RouletteWheel Wheel = new RouletteWheel(probabilities);
            double bin = sum / n;
            for (int i = 0; i < n; i++) {
                double a = i * bin;
                double b = (i + 1) * bin;
                int sel = Wheel.spin(a, b);
                list.add(options[sel]);
            }
        } else if (distribution.equals("custom")) {
        }
        return list.toArray(new String[0]);
    }
}
