package javax.time.calendar.zone;

import static javax.time.calendar.ISODateTimeRule.HOUR_OF_DAY;
import static javax.time.calendar.ISODateTimeRule.MINUTE_OF_HOUR;
import static javax.time.calendar.ISODateTimeRule.SECOND_OF_MINUTE;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.text.ParsePosition;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.StringTokenizer;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.jar.JarOutputStream;
import java.util.zip.ZipEntry;
import javax.time.calendar.DateAdjusters;
import javax.time.calendar.DateTimeField;
import javax.time.calendar.DayOfWeek;
import javax.time.calendar.ISOChronology;
import javax.time.calendar.LocalDate;
import javax.time.calendar.LocalDateTime;
import javax.time.calendar.LocalTime;
import javax.time.calendar.MonthOfYear;
import javax.time.calendar.Period;
import javax.time.calendar.Year;
import javax.time.calendar.ZoneOffset;
import javax.time.calendar.format.DateTimeFormatter;
import javax.time.calendar.format.DateTimeFormatterBuilder;
import javax.time.calendar.format.DateTimeParseContext;
import javax.time.calendar.zone.ZoneRulesBuilder.TimeDefinition;

/**
 * A builder that can read the TZDB TimeZone files and build ZoneRules instances.
 * <p>
 * This class is a mutable builder. A new instance must be created for each compile.
 *
 * @author Stephen Colebourne
 */
public final class TZDBZoneRulesCompiler {

    /**
     * Time parser.
     */
    private static final DateTimeFormatter TIME_PARSER;

    static {
        TIME_PARSER = new DateTimeFormatterBuilder().appendValue(HOUR_OF_DAY).optionalStart().appendLiteral(':').appendValue(MINUTE_OF_HOUR, 2).optionalStart().appendLiteral(':').appendValue(SECOND_OF_MINUTE, 2).toFormatter();
    }

    /**
     * Reads a set of TZDB files and builds a single combined data file.
     *
     * @param args  the arguments
     */
    public static void main(String[] args) {
        if (args.length < 2) {
            outputHelp();
            return;
        }
        String version = null;
        File baseSrcDir = null;
        File dstDir = null;
        boolean verbose = false;
        int i;
        for (i = 0; i < args.length; i++) {
            String arg = args[i];
            if (arg.startsWith("-") == false) {
                break;
            }
            if ("-srcdir".equals(arg)) {
                if (baseSrcDir == null && ++i < args.length) {
                    baseSrcDir = new File(args[i]);
                    continue;
                }
            } else if ("-dstdir".equals(arg)) {
                if (dstDir == null && ++i < args.length) {
                    dstDir = new File(args[i]);
                    continue;
                }
            } else if ("-version".equals(arg)) {
                if (version == null && ++i < args.length) {
                    version = args[i];
                    continue;
                }
            } else if ("-verbose".equals(arg)) {
                if (verbose == false) {
                    verbose = true;
                    continue;
                }
            } else if ("-help".equals(arg) == false) {
                System.out.println("Unrecognised option: " + arg);
            }
            outputHelp();
            return;
        }
        if (baseSrcDir == null) {
            System.out.println("Source directory must be specified using -srcdir: " + baseSrcDir);
            return;
        }
        if (baseSrcDir.isDirectory() == false) {
            System.out.println("Source does not exist or is not a directory: " + baseSrcDir);
            return;
        }
        dstDir = (dstDir != null ? dstDir : baseSrcDir);
        List<String> srcFileNames = Arrays.asList(Arrays.copyOfRange(args, i, args.length));
        if (srcFileNames.isEmpty()) {
            System.out.println("Source filenames not specified, using default set");
            System.out.println("(africa antarctica asia australasia backward etcetera europe northamerica southamerica)");
            srcFileNames = Arrays.asList("africa", "antarctica", "asia", "australasia", "backward", "etcetera", "europe", "northamerica", "southamerica");
        }
        List<File> srcDirs = new ArrayList<File>();
        if (version != null) {
            File srcDir = new File(baseSrcDir, version);
            if (srcDir.isDirectory() == false) {
                System.out.println("Version does not represent a valid source directory : " + srcDir);
                return;
            }
            srcDirs.add(srcDir);
        } else {
            File[] dirs = baseSrcDir.listFiles();
            for (File dir : dirs) {
                if (dir.isDirectory() && dir.getName().matches("[12][0-9][0-9][0-9][A-Za-z0-9._-]+")) {
                    srcDirs.add(dir);
                }
            }
        }
        if (srcDirs.isEmpty()) {
            System.out.println("Source directory contains no valid source folders: " + baseSrcDir);
            return;
        }
        if (dstDir.exists() == false && dstDir.mkdirs() == false) {
            System.out.println("Destination directory could not be created: " + dstDir);
            return;
        }
        if (dstDir.isDirectory() == false) {
            System.out.println("Destination is not a directory: " + dstDir);
            return;
        }
        process(srcDirs, srcFileNames, dstDir, verbose);
        System.exit(0);
    }

    /**
     * Output usage text for the command line.
     */
    private static void outputHelp() {
        System.out.println("Usage: TZDBZoneRulesCompiler <options> <tzdb source filenames>");
        System.out.println("where options include:");
        System.out.println("   -srcdir <directory>   Where to find source directories (required)");
        System.out.println("   -dstdir <directory>   Where to output generated files (default srcdir)");
        System.out.println("   -version <version>    Specify the version, such as 2009a (optional)");
        System.out.println("   -help                 Print this usage message");
        System.out.println("   -verbose              Output verbose information during compilation");
        System.out.println(" There must be one directory for each version in srcdir");
        System.out.println(" Each directory must have the name of the version, such as 2009a");
        System.out.println(" Each directory must contain the unpacked tzdb files, such as asia or europe");
        System.out.println(" Directories must match the regex [12][0-9][0-9][0-9][A-Za-z0-9._-]+");
        System.out.println(" There will be one jar file for each version and one combined jar in dstdir");
        System.out.println(" If the version is specified, only that version is processed");
    }

    /**
     * Process to create the jar files.
     */
    private static void process(List<File> srcDirs, List<String> srcFileNames, File dstDir, boolean verbose) {
        Map<Object, Object> deduplicateMap = new HashMap<Object, Object>();
        Map<String, SortedMap<String, ZoneRules>> allBuiltZones = new TreeMap<String, SortedMap<String, ZoneRules>>();
        Set<String> allRegionIds = new TreeSet<String>();
        Set<ZoneRules> allRules = new HashSet<ZoneRules>();
        for (File srcDir : srcDirs) {
            List<File> srcFiles = new ArrayList<File>();
            for (String srcFileName : srcFileNames) {
                File file = new File(srcDir, srcFileName);
                if (file.exists()) {
                    srcFiles.add(file);
                }
            }
            if (srcFiles.isEmpty()) {
                continue;
            }
            String loopVersion = srcDir.getName();
            TZDBZoneRulesCompiler compiler = new TZDBZoneRulesCompiler(loopVersion, srcFiles, verbose);
            compiler.setDeduplicateMap(deduplicateMap);
            try {
                SortedMap<String, ZoneRules> builtZones = compiler.compile();
                File dstFile = new File(dstDir, "jsr-310-TZDB-" + loopVersion + ".jar");
                if (verbose) {
                    System.out.println("Outputting file: " + dstFile);
                }
                outputFile(dstFile, loopVersion, builtZones);
                allBuiltZones.put(loopVersion, builtZones);
                allRegionIds.addAll(builtZones.keySet());
                allRules.addAll(builtZones.values());
            } catch (Exception ex) {
                System.out.println("Failed: " + ex.toString());
                ex.printStackTrace();
                System.exit(1);
            }
        }
        File dstFile = new File(dstDir, "jsr-310-TZDB-all.jar");
        if (verbose) {
            System.out.println("Outputting combined file: " + dstFile);
        }
        outputFile(dstFile, allBuiltZones, allRegionIds, allRules);
    }

    /**
     * Outputs the file.
     */
    private static void outputFile(File dstFile, String version, SortedMap<String, ZoneRules> builtZones) {
        Map<String, SortedMap<String, ZoneRules>> loopAllBuiltZones = new TreeMap<String, SortedMap<String, ZoneRules>>();
        loopAllBuiltZones.put(version, builtZones);
        Set<String> loopAllRegionIds = new TreeSet<String>(builtZones.keySet());
        Set<ZoneRules> loopAllRules = new HashSet<ZoneRules>(builtZones.values());
        outputFile(dstFile, loopAllBuiltZones, loopAllRegionIds, loopAllRules);
    }

    /**
     * Outputs the file.
     */
    private static void outputFile(File dstFile, Map<String, SortedMap<String, ZoneRules>> allBuiltZones, Set<String> allRegionIds, Set<ZoneRules> allRules) {
        try {
            JarOutputStream jos = new JarOutputStream(new FileOutputStream(dstFile));
            jos.putNextEntry(new ZipEntry("javax/time/calendar/zone/ZoneRules.dat"));
            DataOutputStream out = new DataOutputStream(jos);
            out.writeByte(1);
            out.writeUTF("TZDB");
            String[] versionArray = allBuiltZones.keySet().toArray(new String[allBuiltZones.size()]);
            out.writeShort(versionArray.length);
            for (String version : versionArray) {
                out.writeUTF(version);
            }
            String[] regionArray = allRegionIds.toArray(new String[allRegionIds.size()]);
            out.writeShort(regionArray.length);
            for (String regionId : regionArray) {
                out.writeUTF(regionId);
            }
            List<ZoneRules> rulesList = new ArrayList<ZoneRules>(allRules);
            for (String version : allBuiltZones.keySet()) {
                out.writeShort(allBuiltZones.get(version).size());
                for (Map.Entry<String, ZoneRules> entry : allBuiltZones.get(version).entrySet()) {
                    int regionIndex = Arrays.binarySearch(regionArray, entry.getKey());
                    int rulesIndex = rulesList.indexOf(entry.getValue());
                    out.writeShort(regionIndex);
                    out.writeShort(rulesIndex);
                }
            }
            out.writeShort(rulesList.size());
            ByteArrayOutputStream baos = new ByteArrayOutputStream(1024);
            for (ZoneRules rules : rulesList) {
                baos.reset();
                DataOutputStream dataos = new DataOutputStream(baos);
                Ser.write(rules, dataos);
                dataos.close();
                byte[] bytes = baos.toByteArray();
                out.writeShort(bytes.length);
                out.write(bytes);
            }
            jos.closeEntry();
            out.close();
        } catch (Exception ex) {
            System.out.println("Failed: " + ex.toString());
            ex.printStackTrace();
            System.exit(1);
        }
    }

    /** The TZDB rules. */
    private final Map<String, List<TZDBRule>> rules = new HashMap<String, List<TZDBRule>>();

    /** The TZDB zones. */
    private final Map<String, List<TZDBZone>> zones = new HashMap<String, List<TZDBZone>>();

    /** The TZDB links. */
    private final Map<String, String> links = new HashMap<String, String>();

    /** The built zones. */
    private final SortedMap<String, ZoneRules> builtZones = new TreeMap<String, ZoneRules>();

    /** A map to deduplicate object instances. */
    private Map<Object, Object> deduplicateMap = new HashMap<Object, Object>();

    /** The version to produce. */
    private final String version;

    /** The source files. */
    private final List<File> sourceFiles;

    /** The version to produce. */
    private final boolean verbose;

    /**
     * Creates an instance if you want to invoke the compiler manually.
     *
     * @param version  the version, such as 2009a, not null
     * @param sourceFiles  the list of source files, not empty, not null
     * @param verbose  whether to output verbose messages
     */
    public TZDBZoneRulesCompiler(String version, List<File> sourceFiles, boolean verbose) {
        this.version = version;
        this.sourceFiles = sourceFiles;
        this.verbose = verbose;
    }

    /**
     * Compile the rules file.
     *
     * @return the map of region ID to rules, not null
     * @throws Exception if an error occurs
     */
    public SortedMap<String, ZoneRules> compile() throws Exception {
        printVerbose("Compiling TZDB version " + version);
        parseFiles();
        buildZoneRules();
        printVerbose("Compiled TZDB version " + version);
        return builtZones;
    }

    /**
     * Sets the deduplication map.
     *
     * @param deduplicateMap  the map to deduplicate items
     */
    void setDeduplicateMap(Map<Object, Object> deduplicateMap) {
        this.deduplicateMap = deduplicateMap;
    }

    /**
     * Parses the source files.
     *
     * @throws Exception if an error occurs
     */
    private void parseFiles() throws Exception {
        for (File file : sourceFiles) {
            printVerbose("Parsing file: " + file);
            parseFile(file);
        }
    }

    /**
     * Parses a source file.
     *
     * @param file  the file being read, not null
     * @throws Exception if an error occurs
     */
    private void parseFile(File file) throws Exception {
        int lineNumber = 1;
        String line = null;
        BufferedReader in = null;
        try {
            in = new BufferedReader(new FileReader(file));
            List<TZDBZone> openZone = null;
            for (; (line = in.readLine()) != null; lineNumber++) {
                int index = line.indexOf('#');
                if (index >= 0) {
                    line = line.substring(0, index);
                }
                if (line.trim().length() == 0) {
                    continue;
                }
                StringTokenizer st = new StringTokenizer(line, " \t");
                if (openZone != null && Character.isWhitespace(line.charAt(0)) && st.hasMoreTokens()) {
                    if (parseZoneLine(st, openZone)) {
                        openZone = null;
                    }
                } else {
                    if (st.hasMoreTokens()) {
                        String first = st.nextToken();
                        if (first.equals("Zone")) {
                            if (st.countTokens() < 3) {
                                printVerbose("Invalid Zone line in file: " + file + ", line: " + line);
                                throw new IllegalArgumentException("Invalid Zone line");
                            }
                            openZone = new ArrayList<TZDBZone>();
                            zones.put(st.nextToken(), openZone);
                            if (parseZoneLine(st, openZone)) {
                                openZone = null;
                            }
                        } else {
                            openZone = null;
                            if (first.equals("Rule")) {
                                if (st.countTokens() < 9) {
                                    printVerbose("Invalid Rule line in file: " + file + ", line: " + line);
                                    throw new IllegalArgumentException("Invalid Rule line");
                                }
                                parseRuleLine(st);
                            } else if (first.equals("Link")) {
                                if (st.countTokens() < 2) {
                                    printVerbose("Invalid Link line in file: " + file + ", line: " + line);
                                    throw new IllegalArgumentException("Invalid Link line");
                                }
                                String realId = st.nextToken();
                                String aliasId = st.nextToken();
                                links.put(aliasId, realId);
                            } else {
                                throw new IllegalArgumentException("Unknown line");
                            }
                        }
                    }
                }
            }
        } catch (Exception ex) {
            throw new Exception("Failed while processing file '" + file + "' on line " + lineNumber + " '" + line + "'", ex);
        } finally {
            try {
                if (in != null) {
                    in.close();
                }
            } catch (Exception ex) {
            }
        }
    }

    /**
     * Parses a Rule line.
     *
     * @param st  the tokenizer, not null
     */
    private void parseRuleLine(StringTokenizer st) {
        TZDBRule rule = new TZDBRule();
        String name = st.nextToken();
        if (rules.containsKey(name) == false) {
            rules.put(name, new ArrayList<TZDBRule>());
        }
        rules.get(name).add(rule);
        rule.startYear = parseYear(st.nextToken(), 0);
        rule.endYear = parseYear(st.nextToken(), rule.startYear);
        if (rule.startYear > rule.endYear) {
            throw new IllegalArgumentException("Year order invalid: " + rule.startYear + " > " + rule.endYear);
        }
        parseOptional(st.nextToken());
        parseMonthDayTime(st, rule);
        rule.savingsAmount = parsePeriod(st.nextToken());
        rule.text = parseOptional(st.nextToken());
    }

    /**
     * Parses a Zone line.
     *
     * @param st  the tokenizer, not null
     * @return true if the zone is complete
     */
    private boolean parseZoneLine(StringTokenizer st, List<TZDBZone> zoneList) {
        TZDBZone zone = new TZDBZone();
        zoneList.add(zone);
        zone.standardOffset = parseOffset(st.nextToken());
        String savingsRule = parseOptional(st.nextToken());
        if (savingsRule == null) {
            zone.fixedSavings = Period.ZERO;
            zone.savingsRule = null;
        } else {
            try {
                zone.fixedSavings = parsePeriod(savingsRule);
                zone.savingsRule = null;
            } catch (Exception ex) {
                zone.fixedSavings = null;
                zone.savingsRule = savingsRule;
            }
        }
        zone.text = st.nextToken();
        if (st.hasMoreTokens()) {
            zone.year = Year.of(Integer.parseInt(st.nextToken()));
            if (st.hasMoreTokens()) {
                parseMonthDayTime(st, zone);
            }
            return false;
        } else {
            return true;
        }
    }

    /**
     * Parses a Rule line.
     *
     * @param st  the tokenizer, not null
     * @param mdt  the object to parse into, not null
     */
    private void parseMonthDayTime(StringTokenizer st, TZDBMonthDayTime mdt) {
        mdt.month = parseMonth(st.nextToken());
        if (st.hasMoreTokens()) {
            String dayRule = st.nextToken();
            if (dayRule.startsWith("last")) {
                mdt.dayOfMonth = -1;
                mdt.dayOfWeek = parseDayOfWeek(dayRule.substring(4));
                mdt.adjustForwards = false;
            } else {
                int index = dayRule.indexOf(">=");
                if (index > 0) {
                    mdt.dayOfWeek = parseDayOfWeek(dayRule.substring(0, index));
                    dayRule = dayRule.substring(index + 2);
                } else {
                    index = dayRule.indexOf("<=");
                    if (index > 0) {
                        mdt.dayOfWeek = parseDayOfWeek(dayRule.substring(0, index));
                        mdt.adjustForwards = false;
                        dayRule = dayRule.substring(index + 2);
                    }
                }
                mdt.dayOfMonth = Integer.parseInt(dayRule);
            }
            if (st.hasMoreTokens()) {
                String timeStr = st.nextToken();
                int secsOfDay = parseSecs(timeStr);
                if (secsOfDay == 86400) {
                    mdt.endOfDay = true;
                    secsOfDay = 0;
                }
                LocalTime time = deduplicate(LocalTime.ofSecondOfDay(secsOfDay));
                mdt.time = time;
                mdt.timeDefinition = parseTimeDefinition(timeStr.charAt(timeStr.length() - 1));
            }
        }
    }

    private int parseYear(String str, int defaultYear) {
        str = str.toLowerCase();
        if (matches(str, "minimum")) {
            return Year.MIN_YEAR;
        } else if (matches(str, "maximum")) {
            return Year.MAX_YEAR;
        } else if (str.equals("only")) {
            return defaultYear;
        }
        return Integer.parseInt(str);
    }

    private MonthOfYear parseMonth(String str) {
        str = str.toLowerCase();
        for (MonthOfYear moy : MonthOfYear.values()) {
            if (matches(str, moy.name().toLowerCase())) {
                return moy;
            }
        }
        throw new IllegalArgumentException("Unknown month: " + str);
    }

    private DayOfWeek parseDayOfWeek(String str) {
        str = str.toLowerCase();
        for (DayOfWeek dow : DayOfWeek.values()) {
            if (matches(str, dow.name().toLowerCase())) {
                return dow;
            }
        }
        throw new IllegalArgumentException("Unknown day-of-week: " + str);
    }

    private boolean matches(String str, String search) {
        return str.startsWith(search.substring(0, 3)) && search.startsWith(str) && str.length() <= search.length();
    }

    private String parseOptional(String str) {
        return str.equals("-") ? null : str;
    }

    private int parseSecs(String str) {
        if (str.equals("-")) {
            return 0;
        }
        int pos = 0;
        if (str.startsWith("-")) {
            pos = 1;
        }
        ParsePosition pp = new ParsePosition(pos);
        DateTimeParseContext cal = TIME_PARSER.parse(str, pp);
        if (pp.getErrorIndex() >= 0) {
            throw new IllegalArgumentException(str);
        }
        DateTimeField hour = (DateTimeField) cal.getParsed(HOUR_OF_DAY);
        DateTimeField min = (DateTimeField) cal.getParsed(MINUTE_OF_HOUR);
        DateTimeField sec = (DateTimeField) cal.getParsed(SECOND_OF_MINUTE);
        int secs = (int) (hour.getValue() * 60 * 60 + (min != null ? min.getValue() : 0) * 60 + (sec != null ? sec.getValue() : 0));
        if (pos == 1) {
            secs = -secs;
        }
        return secs;
    }

    private ZoneOffset parseOffset(String str) {
        int secs = parseSecs(str);
        return ZoneOffset.ofTotalSeconds(secs);
    }

    private Period parsePeriod(String str) {
        int secs = parseSecs(str);
        return deduplicate(Period.ofSeconds(secs).normalized());
    }

    private TimeDefinition parseTimeDefinition(char c) {
        switch(c) {
            case 's':
            case 'S':
                return TimeDefinition.STANDARD;
            case 'u':
            case 'U':
            case 'g':
            case 'G':
            case 'z':
            case 'Z':
                return TimeDefinition.UTC;
            case 'w':
            case 'W':
            default:
                return TimeDefinition.WALL;
        }
    }

    /**
     * Build the rules, zones and links into real zones.
     *
     * @throws Exception if an error occurs
     */
    private void buildZoneRules() throws Exception {
        for (String zoneId : zones.keySet()) {
            printVerbose("Building zone " + zoneId);
            zoneId = deduplicate(zoneId);
            List<TZDBZone> tzdbZones = zones.get(zoneId);
            ZoneRulesBuilder bld = new ZoneRulesBuilder();
            for (TZDBZone tzdbZone : tzdbZones) {
                bld = tzdbZone.addToBuilder(bld, rules);
            }
            ZoneRules buildRules = bld.toRules(zoneId, deduplicateMap);
            builtZones.put(zoneId, deduplicate(buildRules));
        }
        for (String aliasId : links.keySet()) {
            aliasId = deduplicate(aliasId);
            String realId = links.get(aliasId);
            printVerbose("Linking alias " + aliasId + " to " + realId);
            ZoneRules realRules = builtZones.get(realId);
            if (realRules == null) {
                realId = links.get(realId);
                printVerbose("Relinking alias " + aliasId + " to " + realId);
                realRules = builtZones.get(realId);
                if (realRules == null) {
                    throw new IllegalArgumentException("Alias '" + aliasId + "' links to invalid zone '" + realId + "' for '" + version + "'");
                }
            }
            builtZones.put(aliasId, realRules);
        }
        builtZones.remove("UTC");
        builtZones.remove("GMT");
    }

    /**
     * Deduplicates an object instance.
     *
     * @param <T> the generic type
     * @param object  the object to deduplicate
     * @return the deduplicated object
     */
    @SuppressWarnings("unchecked")
    <T> T deduplicate(T object) {
        if (deduplicateMap.containsKey(object) == false) {
            deduplicateMap.put(object, object);
        }
        return (T) deduplicateMap.get(object);
    }

    /**
     * Prints a verbose message.
     *
     * @param message  the message, not null
     */
    private void printVerbose(String message) {
        if (verbose) {
            System.out.println(message);
        }
    }

    /**
     * Class representing a month-day-time in the TZDB file.
     */
    abstract class TZDBMonthDayTime {

        /** The month of the cutover. */
        MonthOfYear month = MonthOfYear.JANUARY;

        /** The day-of-month of the cutover. */
        int dayOfMonth = 1;

        /** Whether to adjust forwards. */
        boolean adjustForwards = true;

        /** The day-of-week of the cutover. */
        DayOfWeek dayOfWeek;

        /** The time of the cutover. */
        LocalTime time = LocalTime.MIDNIGHT;

        /** Whether this is midnight end of day. */
        boolean endOfDay;

        /** The time of the cutover. */
        TimeDefinition timeDefinition = TimeDefinition.WALL;

        void adjustToFowards(int year) {
            if (adjustForwards == false && dayOfMonth > 0) {
                LocalDate adjustedDate = LocalDate.of(year, month, dayOfMonth).minusDays(6);
                dayOfMonth = adjustedDate.getDayOfMonth();
                month = adjustedDate.getMonthOfYear();
                adjustForwards = true;
            }
        }
    }

    /**
     * Class representing a rule line in the TZDB file.
     */
    final class TZDBRule extends TZDBMonthDayTime {

        /** The start year. */
        int startYear;

        /** The end year. */
        int endYear;

        /** The amount of savings. */
        Period savingsAmount;

        /** The text name of the zone. */
        String text;

        void addToBuilder(ZoneRulesBuilder bld) {
            adjustToFowards(2004);
            bld.addRuleToWindow(startYear, endYear, month, dayOfMonth, dayOfWeek, time, endOfDay, timeDefinition, savingsAmount);
        }
    }

    /**
     * Class representing a linked set of zone lines in the TZDB file.
     */
    final class TZDBZone extends TZDBMonthDayTime {

        /** The standard offset. */
        ZoneOffset standardOffset;

        /** The fixed savings amount. */
        Period fixedSavings;

        /** The savings rule. */
        String savingsRule;

        /** The text name of the zone. */
        String text;

        /** The year of the cutover. */
        Year year;

        ZoneRulesBuilder addToBuilder(ZoneRulesBuilder bld, Map<String, List<TZDBRule>> rules) {
            if (year != null) {
                bld.addWindow(standardOffset, toDateTime(year.getValue()), timeDefinition);
            } else {
                bld.addWindowForever(standardOffset);
            }
            if (fixedSavings != null) {
                bld.setFixedSavingsToWindow(fixedSavings);
            } else {
                List<TZDBRule> tzdbRules = rules.get(savingsRule);
                if (tzdbRules == null) {
                    throw new IllegalArgumentException("Rule not found: " + savingsRule);
                }
                for (TZDBRule tzdbRule : tzdbRules) {
                    tzdbRule.addToBuilder(bld);
                }
            }
            return bld;
        }

        private LocalDateTime toDateTime(int year) {
            adjustToFowards(year);
            LocalDate date;
            if (dayOfMonth == -1) {
                dayOfMonth = month.getLastDayOfMonth(ISOChronology.isLeapYear(year));
                date = LocalDate.of(year, month, dayOfMonth);
                if (dayOfWeek != null) {
                    date = date.with(DateAdjusters.previousOrCurrent(dayOfWeek));
                }
            } else {
                date = LocalDate.of(year, month, dayOfMonth);
                if (dayOfWeek != null) {
                    date = date.with(DateAdjusters.nextOrCurrent(dayOfWeek));
                }
            }
            date = deduplicate(date);
            LocalDateTime ldt = LocalDateTime.of(date, time);
            if (endOfDay) {
                ldt = ldt.plusDays(1);
            }
            return ldt;
        }
    }
}
