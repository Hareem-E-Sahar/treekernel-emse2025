package edu.unibi.agbi.biodwh.parser;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.hibernate.StatelessSession;
import edu.unibi.agbi.biodwh.database.ParserConnector;
import edu.unibi.agbi.biodwh.entity.omim.OmimClinicalSynopsis;
import edu.unibi.agbi.biodwh.entity.omim.OmimDiagnosisType;
import edu.unibi.agbi.biodwh.entity.omim.OmimDisease;
import edu.unibi.agbi.biodwh.entity.omim.OmimFeature;
import edu.unibi.agbi.biodwh.entity.omim.OmimGeneDisorders;
import edu.unibi.agbi.biodwh.entity.omim.OmimGeneMap;
import edu.unibi.agbi.biodwh.entity.omim.OmimGeneMethod;
import edu.unibi.agbi.biodwh.entity.omim.OmimGeneSymbol;
import edu.unibi.agbi.biodwh.entity.omim.OmimMorbidMap;
import edu.unibi.agbi.biodwh.entity.omim.OmimReference;
import edu.unibi.agbi.biodwh.entity.omim.OmimSynonym;

public class OMIMParser extends BioDWHParser {

    private StatelessSession session;

    private final String OMIM_FILE = new String("omim.txt");

    private final String GENE_MAP_FILE = new String("genemap");

    private final String MORBID_MAP_FILE = new String("morbidmap");

    private final String FIELD[] = { new String("*RECORD*"), new String("*FIELD* NO"), new String("*FIELD* TI"), new String("*FIELD* TX"), new String("*FIELD* RF"), new String("*FIELD* CS"), new String("*FIELD* ED") };

    private final String PATTERN_FIELD = new String("\\*FIELD\\*");

    private final char[] DIAGNOSOS_TYPE_CHAR = { ' ', '*', '#', '+', '%', '^' };

    private final String D_SEMICOLON = new String(";;");

    private final String SEPARTOR_SEMICOLON = new String(";");

    private final String SEPARATOR_COMMA = new String(",");

    private final String LINE_TERMINATOR = new String("\n");

    private final String[] DIAGNOSIS_TYPES = { new String("phenotype for which the mendelian basis, although suspected, has not been clearly established"), new String("gene of known sequence (OMIM symbol *)"), new String("descriptive entry, usually of a phenotype, and does not represent a unique locus (OMIM symbol #)"), new String("gene of known sequence and a phenotype (OMIM symbol +)"), new String("confirmed mendelian phenotype or phenotypic locus for which the underlying molecular basis is not known (OMIM symbol %)"), new String("before an entry number means the entry no longer exists because it was removed from the database or moved to another entry as indicated. (OMIM symol ^)") };

    private int progress = 0;

    private long read_position = 0;

    private long file_length = 0;

    private boolean abort = false;

    private final String GENE_MAP_SEPARATOR = new String("\\|");

    private int osy_id = 0;

    private int ref_id = 0;

    private int cs_id = 0;

    private int feature_id = 0;

    private int genemap_id = 0;

    private int genemet_id = 0;

    private int genesym_id = 0;

    private int genedis_id = 0;

    /**
	 * StringBuffer entry bekommt stückchenweise von omim.txt vom *RECORD* bis nächsten *RECORD* und ruft jeweils parseEntry(entry) auf.
	 */
    private void parseFile(String filename) throws IOException, SQLException {
        BufferedReader br = new BufferedReader(new FileReader(filename));
        boolean first_entry = true;
        String line;
        StringBuffer entry = new StringBuffer();
        while ((line = br.readLine()) != null && !abort) {
            progress = calculateProgress(line.length() + 1);
            if (line.startsWith(FIELD[0])) {
                if (!first_entry) {
                    parseEntry(entry);
                    entry = new StringBuffer();
                } else first_entry = false;
            } else {
                entry.append(line + LINE_TERMINATOR);
            }
        }
        parseEntry(entry);
        br.close();
    }

    private void parseEntry(StringBuffer entry) throws SQLException {
        String id = new String();
        String title = new String();
        ArrayList<String> synonyms = new ArrayList<String>();
        ArrayList<TextObject> text = new ArrayList<TextObject>();
        ArrayList<String> references = new ArrayList<String>();
        ArrayList<SynopsisObject> clinical_synopsis = new ArrayList<SynopsisObject>();
        String last_update = new String();
        byte type = 0;
        ArrayList<TextBlock> entry_list = findSubEntries(entry);
        for (TextBlock tb : entry_list) {
            String field = entry.substring(tb.getStart(), tb.getEnd());
            if (field.startsWith(FIELD[1])) {
                id = parseID(field);
            }
            if (field.startsWith(FIELD[2])) {
                String ti_field = field.substring(FIELD[2].length() + 1);
                char type_char = ti_field.charAt(0);
                if (type_char == DIAGNOSOS_TYPE_CHAR[1]) type = 2; else if (type_char == DIAGNOSOS_TYPE_CHAR[2]) type = 3; else if (type_char == DIAGNOSOS_TYPE_CHAR[3]) type = 4; else if (type_char == DIAGNOSOS_TYPE_CHAR[4]) type = 5; else if (type_char == DIAGNOSOS_TYPE_CHAR[5]) type = 6; else type = 1;
                String[] ti_info = parseTitle(ti_field, type, id.length());
                title = ti_info[0];
                for (int i = 1; i <= ti_info.length - 1; i++) {
                    String syn[] = ti_info[i].split(SEPARTOR_SEMICOLON);
                    for (String synonym_name : syn) {
                        synonyms.add(synonym_name.trim());
                    }
                }
            }
            if (field.startsWith(FIELD[3])) {
                String tx_field = field.substring(FIELD[3].length() + 1);
                text = parseText(tx_field);
            }
            if (field.startsWith(FIELD[4])) {
                String rf_field = field.substring(FIELD[4].length() + 1);
                references = parseRefences(rf_field);
            }
            if (field.startsWith(FIELD[5])) {
                String cs_text = field.substring(FIELD[5].length() + 1);
                clinical_synopsis = parseClinicalSynopsis(cs_text);
            }
            if (field.startsWith(FIELD[6])) {
                String ed_text = field.substring(FIELD[6].length() + 1);
                last_update = parseLastUpdate(ed_text);
            }
        }
        writeTables(id, title, synonyms, text, references, clinical_synopsis, last_update, type);
    }

    /**
	 * Parses the OMIM identifier
	 * 
	 * @param id_field -
	 *            field containing the identifier
	 * @return strung representation of identifier
	 */
    private String parseID(String id_field) {
        id_field = id_field.substring(FIELD[1].length() + 1);
        return id_field.trim();
    }

    /**
	 * Parses the OMIM title and returns the all synonyms.
	 * 
	 * @param ti_field
	 * @param type
	 * @param id_length
	 * @return array of synonyms
	 */
    private String[] parseTitle(String ti_field, byte type, int id_length) {
        if (type != 0) ti_field = ti_field.substring(id_length + 1); else ti_field = ti_field.substring(id_length);
        ti_field = ti_field.replaceAll("\\n", "");
        return ti_field.trim().split(D_SEMICOLON);
    }

    private ArrayList<TextObject> parseText(String text) {
        String pattern = new String("\\n{1}[A-Z[\\s]]+\\n{1}");
        Pattern p = Pattern.compile(pattern);
        Matcher m = p.matcher(text);
        ArrayList<TextObject> list = new ArrayList<TextObject>();
        ArrayList<String> feature_type = new ArrayList<String>();
        ArrayList<TextBlock> feature_position = new ArrayList<TextBlock>();
        TextBlock feature_length = new TextBlock();
        String feature = new String();
        int start = 0;
        int prev_end = 0;
        if (m.find()) {
            if (m.group().trim().length() > 0) {
                feature = m.group();
                start = m.end();
            }
            while (m.find()) {
                if (m.group().trim().length() > 0) {
                    prev_end = m.start() - 1;
                    feature_length = new TextBlock(start, prev_end);
                    feature_position.add(feature_length);
                    feature_type.add(feature);
                    feature = m.group();
                    start = m.end();
                }
            }
            feature_length = new TextBlock(start, text.length());
            feature_position.add(feature_length);
            feature_type.add(feature);
            for (int i = 0; i <= feature_type.size() - 1; i++) {
                String feature_text = text.substring(feature_position.get(i).getStart(), feature_position.get(i).getEnd());
                String feature_tp = feature_type.get(i).replaceAll(LINE_TERMINATOR, "");
                list.add(new TextObject(feature_tp, feature_text));
            }
        } else list.add(new TextObject("TEXT", text));
        return list;
    }

    /**
	 * Parses the refences of the OMIM and returns a list of references.
	 * 
	 * @param rf_text
	 * @return list of references
	 */
    private ArrayList<String> parseRefences(String rf_text) {
        String[] references = rf_text.split("\n{2}");
        ArrayList<String> ref_list = new ArrayList<String>();
        for (int i = 0; i <= references.length - 1; i++) {
            String rf = references[i].replaceFirst("\\d{1,3}.", "").trim();
            if (rf.length() > 1) ref_list.add(rf);
        }
        return ref_list;
    }

    /**
	 * Parses clinival synopsois in the following way:<br>
	 * Domain => [Subdomain | none] -> feature
	 * 
	 * @param cs_text
	 * @return a list of clinical synopsis for the current entry
	 */
    private ArrayList<SynopsisObject> parseClinicalSynopsis(String cs_text) {
        ArrayList<SynopsisObject> synopsis_list = new ArrayList<SynopsisObject>();
        String domain_pattern = new String(".*?:\n");
        String subdomain_pattern = new String("\\[.*?];");
        Pattern p;
        Matcher m;
        int start = 0;
        int prev_end = 0;
        ArrayList<String> domain_list = new ArrayList<String>();
        ArrayList<TextBlock> domain_position = new ArrayList<TextBlock>();
        TextBlock domain_entry_length = new TextBlock();
        ArrayList<String> subdomain_list = new ArrayList<String>();
        ArrayList<TextBlock> subdomain_position = new ArrayList<TextBlock>();
        TextBlock subdomain_entry_length = new TextBlock();
        p = Pattern.compile(domain_pattern);
        m = p.matcher(cs_text);
        if (m.find()) {
            start = m.end();
            domain_list.add(cleanDomain(m.group()));
            while (m.find()) {
                prev_end = m.start();
                domain_entry_length = new TextBlock(start, prev_end);
                domain_position.add(domain_entry_length);
                start = m.end();
                domain_list.add(cleanDomain(m.group()));
            }
            prev_end = cs_text.length();
            domain_entry_length = new TextBlock(start, prev_end);
            domain_position.add(domain_entry_length);
        }
        p = Pattern.compile(subdomain_pattern);
        for (int z = 0; z <= domain_position.size() - 1; z++) {
            TextBlock tb = domain_position.get(z);
            String domain_text = cs_text.substring(tb.getStart(), tb.getEnd());
            subdomain_list = new ArrayList<String>();
            subdomain_position = new ArrayList<TextBlock>();
            subdomain_entry_length = new TextBlock();
            m = p.matcher(domain_text);
            if (m.find()) {
                start = m.end();
                subdomain_list.add(cleanSubDomain(m.group()));
                while (m.find()) {
                    prev_end = m.start();
                    subdomain_entry_length = new TextBlock(start, prev_end);
                    subdomain_position.add(subdomain_entry_length);
                    start = m.end();
                    subdomain_list.add(cleanSubDomain(m.group()));
                }
                prev_end = domain_text.length();
                subdomain_entry_length = new TextBlock(start, prev_end);
                subdomain_position.add(subdomain_entry_length);
            } else {
                start = 0;
                prev_end = domain_text.length();
                subdomain_entry_length = new TextBlock(start, prev_end);
                subdomain_position.add(subdomain_entry_length);
                subdomain_list.add("none");
            }
            for (int x = 0; x <= subdomain_position.size() - 1; x++) {
                TextBlock stb = subdomain_position.get(x);
                String features[] = domain_text.substring(stb.getStart(), stb.getEnd()).replaceAll(LINE_TERMINATOR, "").trim().split(";");
                for (String feature : features) synopsis_list.add(new SynopsisObject(domain_list.get(z), subdomain_list.get(x), feature.trim()));
            }
        }
        return synopsis_list;
    }

    private String parseLastUpdate(String ed_text) {
        Pattern p = Pattern.compile("\\d{1,2}/\\d{1,2}/\\d{2,4}");
        Matcher m;
        String updates[] = ed_text.split(LINE_TERMINATOR);
        String last_update = updates[0];
        m = p.matcher(last_update);
        if (m.find()) last_update = m.group();
        return last_update;
    }

    /**
	 * Removes all signs from subdomain.
	 * 
	 * @param domain
	 * @return domain name
	 */
    private String cleanDomain(String domain) {
        domain = domain.replaceAll(":", "");
        domain = domain.replaceAll(LINE_TERMINATOR, "");
        return domain;
    }

    /**
	 * Removes all signs from subdomain.
	 * 
	 * @param subdomain
	 * @return subdomain name
	 */
    private String cleanSubDomain(String subdomain) {
        subdomain = subdomain.replaceAll("\\[", "");
        subdomain = subdomain.replaceAll("\\]", "");
        subdomain = subdomain.replaceAll(";", "");
        return subdomain;
    }

    /**
	 * Find the start and end positions of all entries in a record.
	 * 
	 * @param entry -
	 *            string representation of the whole record
	 * @return
	 */
    private ArrayList<TextBlock> findSubEntries(StringBuffer entry) {
        Pattern p = Pattern.compile(PATTERN_FIELD);
        Matcher m = p.matcher(entry);
        TextBlock entry_length = new TextBlock();
        int start = 0;
        int prev_end = 0;
        ArrayList<TextBlock> entry_positions = new ArrayList<TextBlock>();
        m.find();
        start = m.start();
        while (m.find()) {
            prev_end = m.start() - 1;
            entry_length = new TextBlock(start, prev_end);
            entry_positions.add(entry_length);
            start = m.start();
        }
        prev_end = entry.length();
        entry_length = new TextBlock(start, prev_end);
        entry_positions.add(entry_length);
        return entry_positions;
    }

    /**
	 * This method writes the parsed entries into the database.
	 * 
	 * @param id - omim identifier
	 * @param title - omim entry title
	 * @param synonyms - omim synonyms
	 * @param text
	 * @param references - literature references
	 * @param clinical_synopsis
	 * @param last_update - latest update
	 * @param type - entry type
	 * @throws SQLException
	 */
    private void writeTables(String id, String title, ArrayList<String> synonyms, ArrayList<TextObject> text, ArrayList<String> references, ArrayList<SynopsisObject> clinical_synopsis, String last_update, byte type) throws SQLException {
        OmimDisease disease = new OmimDisease(id, title, "MiniMIM", type, last_update);
        saveObject(disease);
        for (String synonym : synonyms) {
            OmimSynonym osy = new OmimSynonym(osy_id++, synonym, disease);
            disease.getOsynonym().add(osy);
            saveObject(osy);
        }
        for (String ref : references) {
            OmimReference r = new OmimReference(ref_id++, ref, disease);
            disease.getReference().add(r);
            saveObject(r);
        }
        for (SynopsisObject so : clinical_synopsis) {
            OmimClinicalSynopsis cs = new OmimClinicalSynopsis(cs_id++, so.getDomain(), so.getSubDomain(), so.getFeature(), disease);
            disease.getClin_syn().add(cs);
            saveObject(cs);
        }
        for (TextObject to : text) {
            OmimFeature f = new OmimFeature(feature_id++, to.getType(), to.getText(), disease);
            disease.getFeature().add(f);
            saveObject(f);
        }
    }

    private void writeDiagnosisTypeTable() throws SQLException {
        for (int i = 0; i <= DIAGNOSIS_TYPES.length - 1; i++) {
            OmimDiagnosisType dt = new OmimDiagnosisType("" + (i + 1), DIAGNOSIS_TYPES[i]);
            saveObject(dt);
        }
    }

    /**
	 * Write table into temporary flat files for bulk loading.
	 * 
	 * @param cm - Chromosome.Map_Entry_Number
	 * @param day
	 * @param month
	 * @param year
	 * @param location
	 * @param symbol - gene symbols
	 * @param status - gene status
	 * @param title
	 * @param mim -MIM number
	 * @param method
	 * @param disorders_id
	 * @param disorders
	 * @throws SQLException
	 * @throws IOException
	 */
    private void writeGeneMapTables(String cm, String day, String month, String year, String location, String[] symbol, String status, String title, String mim, String[] method, String[] disorders, String[] disorders_id) throws SQLException, IOException {
        String date = day.trim() + "." + month.trim() + "." + year.trim();
        OmimGeneMap genemap = new OmimGeneMap();
        genemap.setCm(cm.trim());
        genemap.setMim(mim.trim());
        genemap.setEntry_date(date);
        genemap.setLocation(location.trim());
        genemap.setStatus(status.trim());
        genemap.setTitle(title.trim());
        genemap.setId(genemap_id++);
        saveObject(genemap);
        for (String sym : symbol) {
            OmimGeneSymbol genesymbol = new OmimGeneSymbol(genesym_id++, cm.trim(), sym.trim());
            saveObject(genesymbol);
        }
        for (String met : method) {
            if (met.trim().length() > 3 || met.trim() == null) met = new String("ERR");
            OmimGeneMethod genemet = new OmimGeneMethod(genemet_id++, cm.trim(), met.trim());
            saveObject(genemet);
        }
        if (disorders[0].trim().length() != 0) {
            String dis1, dis_id1;
            if (disorders_id[1].trim().length() != 0) {
                dis1 = disorders[1].trim();
                dis_id1 = disorders_id[1];
            } else {
                dis1 = "none";
                dis_id1 = "none";
            }
            String dis2, dis_id2;
            if (disorders_id[2].trim().length() != 0) {
                dis2 = disorders[2].trim();
                dis_id2 = disorders_id[2];
            } else {
                dis2 = disorders[2].trim();
                dis_id2 = disorders_id[2];
            }
            OmimGeneDisorders genedis = new OmimGeneDisorders();
            genedis.setCm(cm.trim());
            genedis.setMim(mim.trim());
            genedis.setDisorder(disorders[0].trim());
            genedis.setDisorder_c1(dis1);
            genedis.setDisorder_c1_id(dis_id1);
            genedis.setDisorder_c2(dis2);
            genedis.setDisorder_c2_id(dis_id2);
            genedis.setDisorder_id(disorders_id[0]);
            genedis.setId(genedis_id++);
            saveObject(genedis);
        }
    }

    private void parseGeneMap(String filename) throws IOException, SQLException {
        BufferedReader br = new BufferedReader(new FileReader(filename));
        String[] entry;
        String[] gene_symbol;
        String[] method;
        String[] disorders = new String[3];
        String[] disorders_id = new String[3];
        String line;
        while ((line = br.readLine()) != null && !abort) {
            progress = calculateProgress(line.length() + 1);
            entry = line.split(GENE_MAP_SEPARATOR);
            gene_symbol = entry[5].split(SEPARATOR_COMMA);
            method = entry[10].split(SEPARATOR_COMMA);
            disorders[0] = entry[13];
            disorders[1] = entry[14];
            disorders[2] = entry[15];
            disorders_id = parseGeneMapDisorders(disorders);
            writeGeneMapTables(entry[0], entry[2], entry[1], entry[3], entry[4], gene_symbol, entry[6], entry[7], entry[9], method, disorders, disorders_id);
        }
        br.close();
    }

    private String[] parseGeneMapDisorders(String[] disorders) {
        Pattern p = Pattern.compile("\\d{6}");
        Matcher m;
        String[] disorder_id = new String[3];
        for (int i = 0; i < disorders.length; i++) {
            m = p.matcher(disorders[i]);
            if (m.find()) {
                disorder_id[i] = m.group().trim();
            } else disorder_id[i] = new String("none");
        }
        return disorder_id;
    }

    private void parseMorbidMap(String filename) throws IOException, SQLException {
        BufferedReader br = new BufferedReader(new FileReader(filename));
        String line;
        while ((line = br.readLine()) != null && !abort) {
            progress = calculateProgress(line.length() + 1);
            String[] entry = line.split("\\|");
            writeMorbidMapTables(entry[0].trim(), entry[1].trim(), entry[2].trim(), entry[3].trim());
        }
        br.close();
    }

    private void writeMorbidMapTables(String disorder, String symbols, String omim_id, String location) {
        OmimMorbidMap morbid_map = new OmimMorbidMap();
        morbid_map.setDisorder(disorder);
        morbid_map.setLocation(location);
        morbid_map.setSymbols(symbols);
        morbid_map.setMim(omim_id);
        saveObject(morbid_map);
    }

    private void saveObject(Object obj) {
        session.insert(obj);
    }

    private int calculateProgress(int line_length) {
        read_position += line_length;
        double result = (double) read_position / (double) file_length;
        return (int) (result * 100);
    }

    @Override
    public int getProgress() {
        return progress;
    }

    @Override
    public void abort() {
        abort = true;
    }

    @Override
    public String getParserName() {
        return new String("OMIM Parser");
    }

    @Override
    public double getVersion() {
        return 1.1;
    }

    @Override
    public Date getCreationDate() {
        return new GregorianCalendar(2010, 11, 17).getTime();
    }

    @Override
    public String getDefaultDownloadURL() {
        return new String("ftp://ftp.ncbi.nih.gov/repository/OMIM/ARCHIVE/");
    }

    @Override
    public String getEntityPackage() {
        return new String("edu.unibi.agbi.biodwh.entity.omim");
    }

    @Override
    public String[] getFileNames() {
        return new String[] { OMIM_FILE + ".Z", GENE_MAP_FILE, MORBID_MAP_FILE };
    }

    @Override
    public String getParserAuthor() {
        return new String("Benjamin Kormeier and Klaus Hippe");
    }

    @Override
    public String getParserDescription() {
        return new String("OMIM parser works with release from 22.02.2011.");
    }

    @Override
    public String getParserID() {
        return new String("unibi.omim");
    }

    @Override
    public void start(ParserConnector connector, String source_dir) throws Throwable {
        abort = false;
        this.file_length = new File(source_dir + File.separator + OMIM_FILE).length();
        this.file_length += new File(source_dir + File.separator + GENE_MAP_FILE).length();
        this.file_length += new File(source_dir + File.separator + MORBID_MAP_FILE).length();
        session = connector.getStatelessSession();
        session.beginTransaction();
        writeDiagnosisTypeTable();
        parseFile(source_dir + File.separator + OMIM_FILE);
        parseGeneMap(source_dir + File.separator + GENE_MAP_FILE);
        parseMorbidMap(source_dir + File.separator + MORBID_MAP_FILE);
        session.getTransaction().commit();
        session.close();
    }

    private class TextBlock {

        int start = 0;

        int end = 0;

        private TextBlock() {
        }

        private TextBlock(int start, int end) {
            this.start = start;
            this.end = end;
        }

        private int getEnd() {
            return end;
        }

        private int getStart() {
            return start;
        }
    }

    private class TextObject {

        String type = new String();

        String text = new String();

        private TextObject(String type, String text) {
            this.type = type;
            this.text = text;
        }

        private String getType() {
            return type;
        }

        private String getText() {
            return text;
        }
    }

    private class SynopsisObject {

        String domain = new String();

        String subdomain = new String();

        String feature = new String();

        private SynopsisObject(String domain, String subdomain, String feature) {
            this.domain = domain;
            this.subdomain = subdomain;
            this.feature = feature;
        }

        private String getDomain() {
            return domain;
        }

        private String getFeature() {
            return feature;
        }

        private String getSubDomain() {
            return subdomain;
        }
    }
}
