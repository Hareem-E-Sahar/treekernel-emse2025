import java.sql.*;
import java.io.*;
import java.util.*;
import java.text.*;
import java.io.*;
import java.util.*;

public class process_indexing {

    /** Creates a new instance of loop_concept_name */
    public process_indexing() {
    }

    private static Connection openDB(String dbdriver, String dburl, String dbuser, String dbpass) {
        try {
            Class.forName(dbdriver);
            Connection db = DriverManager.getConnection(dburl, dbuser, dbpass);
            db.setAutoCommit(false);
            return db;
        } catch (Exception e) {
            System.err.println("Error while opening DB: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    private static void closeDB(Connection db) {
        try {
            db.commit();
            db.close();
        } catch (SQLException e) {
            System.err.println("Close DB: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void commitDB(Connection db) {
        try {
            db.commit();
        } catch (SQLException e) {
            System.err.println("Close DB: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static ResultSet executeQuery(Connection db, String query) {
        try {
            Statement st = db.createStatement();
            ResultSet rs = st.executeQuery(query);
            return rs;
        } catch (SQLException e) {
            System.err.println("ExecuteQuery-ERROR: " + query + " -> " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    private static void executeStatement(Connection db, String statement) {
        try {
            Statement st = db.createStatement();
            st.execute(statement);
        } catch (SQLException e) {
            System.err.println("ExecuteStatement-ERROR: " + statement + " -> " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void updateDB(Connection db, String tablename, String update) {
        String update_sql = "";
        try {
            update_sql = "UPDATE " + tablename + " SET " + update;
            Statement st = db.createStatement();
            st.executeUpdate(update_sql);
            st.close();
        } catch (Exception e) {
            System.err.println("UPDATE-ERROR: " + update_sql + " -> " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void insertIntoDB(Connection db, String tablename, String insert) {
        String insert_sql = "";
        try {
            insert_sql = "INSERT INTO " + tablename + " VALUES(" + insert + ")";
            Statement st = db.createStatement();
            st.executeUpdate(insert_sql);
            st.close();
        } catch (SQLException e) {
            System.err.println("INSERT-ERROR: " + insert_sql + " -> " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void execCommand(String command) throws Exception {
        Runtime r = Runtime.getRuntime();
        Process p = r.exec(command);
        InputStream p_i_s = p.getInputStream();
        OutputStream p_o_s = p.getOutputStream();
        InputStream p_e_s = p.getErrorStream();
        int status = -1;
        try {
            status = p.waitFor();
        } catch (InterruptedException err) {
        }
        if (0 != status) {
            return;
        }
        BufferedReader in = new BufferedReader(new InputStreamReader(p_i_s));
        String temp;
        temp = in.readLine();
        while ((temp = in.readLine()) != null) {
        }
        in.close();
        in = null;
        p_i_s.close();
        p_e_s.close();
        p_o_s.close();
        p_i_s = null;
        p_e_s = null;
        p_o_s = null;
        p = null;
    }

    private void execCommand2(String command) throws Exception {
        Runtime r = Runtime.getRuntime();
        Process p = r.exec(command);
        int status = p.waitFor();
        System.out.println(status);
    }

    private boolean testFilter(boolean applyfilter, Vector filterterms, String concept_name) throws Exception {
        boolean result = true;
        if (applyfilter) {
            for (int i = 0; i < filterterms.size(); i++) {
                String term = (String) filterterms.get(i);
                term = term.toLowerCase();
                String name = concept_name.toLowerCase();
                if (name.indexOf(term) != -1) result = false;
            }
        }
        return result;
    }

    private Vector getConcepts(Connection db, String applyfilter, String filterfilename) throws Exception {
        Vector concepts = new Vector();
        Set IDAndName = new TreeSet();
        boolean filter = false;
        Vector filterterms = new Vector();
        if (applyfilter.compareTo("TRUE") == 0) filter = true;
        if (filter) {
            BufferedReader in = new BufferedReader(new FileReader(filterfilename));
            System.out.println("Filter applied with the following terms:");
            String inputline = in.readLine();
            while (inputline != null) {
                if (inputline.length() > 0) {
                    System.out.println(inputline);
                    filterterms.add(inputline);
                }
                inputline = in.readLine();
            }
            in.close();
            System.out.println();
        }
        String query = "SELECT cs.concept_id,cn.name,cn.name_stemmed " + "FROM concept_subset cs,concept_name cn " + "WHERE " + "cs.concept_id=cn.id ";
        ResultSet rs = executeQuery(db, query);
        System.out.println("Concepts and Synonyms:");
        while (rs.next()) {
            String id = rs.getString(1);
            String name = rs.getString(2);
            String name_stemmed = rs.getString(3);
            String idAndNameStemmed = id + name_stemmed;
            if (testFilter(filter, filterterms, name) && !IDAndName.contains(idAndNameStemmed)) {
                Vector concept = new Vector();
                concept.add(id);
                concept.add(name);
                concept.add(name_stemmed);
                System.out.println(id + " ; " + name + " ; " + name_stemmed);
                concepts.add(concept);
                IDAndName.add(idAndNameStemmed);
            }
        }
        query = "SELECT r.to_concept,cn.name,cn.name_stemmed,cs.concept_id " + "FROM concept_subset cs, concept_name cn, relation r " + "WHERE cs.concept_id=r.from_concept AND cn.id=r.to_concept AND r.of_type='equ'" + "";
        rs = executeQuery(db, query);
        System.out.println("Mappings:");
        while (rs.next()) {
            String id = rs.getString(1);
            String name = rs.getString(2);
            String name_stemmed = rs.getString(3);
            String from_id = rs.getString(4);
            String idAndNameStemmed = id + name_stemmed;
            if (testFilter(filter, filterterms, name) && !IDAndName.contains(idAndNameStemmed)) {
                Vector concept = new Vector();
                concept.add(id);
                concept.add(name);
                concept.add(name_stemmed);
                System.out.println(id + " ; " + name + " ; " + name_stemmed);
                concepts.add(concept);
                IDAndName.add(idAndNameStemmed);
            }
        }
        return concepts;
    }

    private Vector getContextSQL(Connection db, String concept_id) throws Exception {
        Vector context = new Vector();
        String query = "SELECT getContextNameStemmed('" + concept_id + "')";
        ResultSet rs = executeQuery(db, query);
        rs.next();
        Array namesArray = rs.getArray(1);
        if (namesArray != null) {
            ResultSet names = rs.getArray(1).getResultSet();
            while (names.next()) {
                String name = names.getString(2);
                context.add(name);
            }
        }
        return context;
    }

    private Vector getMappedContext(Connection db, Vector context, String concept_id, String direction) throws Exception {
        String query = "SELECT to_concept FROM relation WHERE " + direction + "_concept='" + concept_id + "' AND of_type='equ'";
        ResultSet rs = executeQuery(db, query);
        Vector ids = new Vector();
        while (rs.next()) {
            String id = rs.getString(1);
            if (!ids.contains(id)) {
                Vector add_context = getContextSQL(db, id);
                for (int i = 0; i < add_context.size(); i++) {
                    String con = (String) add_context.get(i);
                    if (!context.contains(con)) context.add(con);
                }
                ids.add(id);
            }
        }
        return context;
    }

    private Vector getContext(Connection db, String concept_id) throws Exception {
        Vector context = getContextSQL(db, concept_id);
        context = getMappedContext(db, context, concept_id, "from");
        context = getMappedContext(db, context, concept_id, "to");
        return context;
    }

    private Vector getSubstringConceptNamesSQL(Connection db, String concept_name) throws Exception {
        Vector concept_names = new Vector();
        String query = "SELECT getSubstringConceptNamesStemmed('" + concept_name + "')";
        ResultSet rs = executeQuery(db, query);
        rs.next();
        Array namesArray = rs.getArray(1);
        if (namesArray != null) {
            ResultSet names = rs.getArray(1).getResultSet();
            while (names.next()) {
                String name = names.getString(2);
                concept_names.add(name);
            }
        }
        return concept_names;
    }

    private String getConceptForSearch(String concept, String seperator) {
        String old_concept = new String(concept);
        old_concept = old_concept.replace(',', ' ');
        old_concept = old_concept.replaceAll(":", " ");
        String modified = new String(old_concept);
        StringTokenizer st = new StringTokenizer(old_concept);
        if (st.countTokens() > 1) {
            String token = st.nextToken();
            modified = "(" + token.toLowerCase();
            while (st.hasMoreTokens()) {
                token = st.nextToken();
                modified = modified + seperator + token.toLowerCase();
            }
            modified = modified + ")";
        }
        return modified;
    }

    private Vector pruneContext(Vector concepts, String main_concept_name) {
        Vector prunedConcepts = new Vector();
        Set unique = new TreeSet();
        Vector toBeRemoved = new Vector();
        toBeRemoved.add(",");
        StringTokenizer main_st = new StringTokenizer(main_concept_name);
        while (main_st.hasMoreTokens()) unique.add(main_st.nextToken().toLowerCase());
        for (int i = 0; i < concepts.size(); i++) {
            String concept = (String) concepts.get(i);
            StringTokenizer st = new StringTokenizer(concept);
            while (st.hasMoreTokens()) {
                String token = st.nextToken();
                if ((token.indexOf(".") != -1) || (token.indexOf(" ") != -1)) System.out.println("Removed token \"" + token + "\" in context of concept name: " + main_concept_name); else unique.add(token.toLowerCase());
            }
        }
        Iterator it = unique.iterator();
        while (it.hasNext()) prunedConcepts.add(it.next());
        for (int i = 0; i < toBeRemoved.size(); i++) prunedConcepts.remove(toBeRemoved.get(i));
        return prunedConcepts;
    }

    private Vector getRankedTextIDs(Connection db, Vector context, String main_concept_name, String main_concept_name_stemmed, String text_table_name, Vector multi_concepts, int indexnr, int partiallines) throws Exception {
        Vector rankedIDs = new Vector();
        String main_concept = main_concept_name_stemmed.toLowerCase();
        main_concept_name_stemmed = getConceptForSearch(main_concept_name_stemmed, "&");
        String context_string = getConceptForSearch((String) context.get(0), "|");
        for (int i = 1; i < context.size(); i++) {
            String cont = getConceptForSearch((String) context.get(i), "|");
            if (cont.indexOf(" ") == -1) context_string = context_string + "|" + cont;
        }
        String query = "SELECT id , rank_ca(true, free_text_idx, q), free_text_tagged " + "FROM " + text_table_name + ", " + "to_tsquery('simple','" + main_concept_name_stemmed + "&(" + context_string + ")') AS q " + "WHERE " + "free_text_idx @@ q " + "AND rank_ca(true, free_text_idx, q) > 0 ";
        if (indexnr > 0) {
            int startrow = (indexnr - 1) * partiallines;
            int lastrow = indexnr * partiallines;
            String start = new String().valueOf(startrow);
            String last = new String().valueOf(lastrow);
            query = query + " AND row_num   > " + start + " AND row_num <= " + last;
        }
        ResultSet rs = executeQuery(db, query);
        while (rs.next()) {
            String id = rs.getString(1);
            double rank = rs.getDouble(2);
            String text = rs.getString(3);
            StringTokenizer st = new StringTokenizer(text);
            String new_text = "";
            while (st.hasMoreTokens()) {
                String token = st.nextToken();
                int pos = token.indexOf("//");
                if (pos > 0) token = token.substring(0, pos);
                new_text = new_text + " " + token;
            }
            text = new_text.trim();
            text = text.toLowerCase();
            for (int i = 0; i < multi_concepts.size(); i++) {
                String multi = (String) multi_concepts.get(i);
                multi = multi.toLowerCase();
                text = text.replaceAll(multi, "");
            }
            if (text.indexOf(main_concept) != -1) {
                Vector entry = new Vector();
                entry.add(id);
                entry.add(new Double(rank));
                entry.add(text_table_name);
                rankedIDs.add(entry);
            }
        }
        return rankedIDs;
    }

    private void getTexts(Connection db, boolean split, String splitext, boolean partial, int partiallines, boolean ramdisk, String ramdiskuser, String ramdiskpath, String nrentriesfile, String contextfile, String outputfile, String applyfilter, String filterfile) throws Exception {
        Vector concepts = getConcepts(db, applyfilter, filterfile);
        DateFormat df = DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.LONG);
        if (ramdisk) {
            String move_to_ramdisk = ramdiskpath + "/run_make.sh " + ramdiskpath + " " + ramdiskuser + " move_indexing_context_files";
            System.out.println("Start of moving indices " + df.format(new java.util.Date()));
            execCommand(move_to_ramdisk);
            System.out.println("End of moving indices " + df.format(new java.util.Date()));
        }
        Vector contexts = new Vector();
        Vector superconcepts = new Vector();
        System.out.println("Start of getting Contexts and SubstringConceptNames: " + df.format(new java.util.Date()));
        System.out.println();
        for (int i = 0; i < concepts.size(); i++) {
            Vector concept = (Vector) concepts.get(i);
            String id = (String) concept.get(0);
            String main_concept_name = (String) concept.get(1);
            String main_concept_name_stemmed = (String) concept.get(2);
            System.out.println(id + ", Name: " + main_concept_name + ", Name Stemmed: " + main_concept_name_stemmed);
            System.out.println("Start of getContextNameStemmed: " + df.format(new java.util.Date()));
            Vector context = getContext(db, id);
            System.out.println("End of getContextNameStemmed: " + df.format(new java.util.Date()));
            context = pruneContext(context, main_concept_name_stemmed);
            contexts.add(context);
            System.out.println("Start of getSubstringConceptNamesSQL: " + df.format(new java.util.Date()));
            Vector multi_concepts = getSubstringConceptNamesSQL(db, main_concept_name_stemmed);
            System.out.println("End of getSubstringConceptNamesSQL: " + df.format(new java.util.Date()));
            System.out.println();
            superconcepts.add(multi_concepts);
        }
        System.out.println("End of getting Contexts and SubstringConceptNames: " + df.format(new java.util.Date()));
        System.out.println();
        if (ramdisk) {
            String remove_to_ramdisk = ramdiskpath + "/run_make.sh " + ramdiskpath + " " + ramdiskuser + " remove_indexing_context_files";
            System.out.println("Start of re-moving indices " + df.format(new java.util.Date()));
            execCommand(remove_to_ramdisk);
            System.out.println("End of re-moving indices " + df.format(new java.util.Date()));
            System.out.println();
        }
        BufferedWriter out = new BufferedWriter(new FileWriter(outputfile));
        Vector rankedTextIDs = new Vector();
        String text_base = "text";
        int nrInserts = 0;
        Integer time = new Integer(0);
        System.out.println("Start of getting rankedTextIDs: " + df.format(new java.util.Date()));
        System.out.println();
        if (!partial) {
            int indexnr = -1;
            for (int i = 0; i < concepts.size(); i++) {
                Vector concept = (Vector) concepts.get(i);
                String id = (String) concept.get(0);
                String main_concept_name = (String) concept.get(1);
                String main_concept_name_stemmed = (String) concept.get(2);
                Vector context = (Vector) contexts.get(i);
                Vector multi_concepts = (Vector) superconcepts.get(i);
                System.out.println("Concept name: " + main_concept_name);
                System.out.println("Start of getRankedTextIDs " + df.format(new java.util.Date()));
                java.util.Date before = new java.util.Date();
                if (!split) {
                    rankedTextIDs = getRankedTextIDs(db, context, main_concept_name, main_concept_name_stemmed, text_base, multi_concepts, indexnr, partiallines);
                } else {
                    rankedTextIDs = new Vector();
                    String text_ext = text_base + splitext;
                    String query = "select count(relname) from pg_class where relname ~* '^" + text_ext + "' and relkind = 'r' ";
                    ResultSet rs = executeQuery(db, query);
                    rs.next();
                    int count_text_tables = (new Integer((String) rs.getString(1))).intValue();
                    for (int j = 0; j < count_text_tables; j++) {
                        String index = "text_part_" + (j + 1) + "_free_text_idx";
                        String text = "text_part_" + (j + 1);
                        if (ramdisk) {
                            String move_to_ramdisk = ramdiskpath + "/run_make.sh " + ramdiskpath + " " + ramdiskuser + " move_indexing_text_files " + index;
                            execCommand(move_to_ramdisk);
                            move_to_ramdisk = ramdiskpath + "/run_make.sh " + ramdiskpath + " " + ramdiskuser + " move_indexing_text_files " + text;
                            execCommand(move_to_ramdisk);
                        }
                        String table_name = text_ext + (j + 1);
                        Vector textIDs = getRankedTextIDs(db, context, main_concept_name, main_concept_name_stemmed, table_name, multi_concepts, indexnr, partiallines);
                        rankedTextIDs.addAll(textIDs);
                        if (ramdisk) {
                            String remove_to_ramdisk = ramdiskpath + "/run_make.sh " + ramdiskpath + " " + ramdiskuser + " remove_indexing_text_files " + index;
                            execCommand(remove_to_ramdisk);
                            remove_to_ramdisk = ramdiskpath + "/run_make.sh " + ramdiskpath + " " + ramdiskuser + " remove_indexing_text_files " + text;
                            execCommand(remove_to_ramdisk);
                        }
                    }
                }
                System.out.println("End of getRankedTextIDs " + df.format(new java.util.Date()));
                java.util.Date after = new java.util.Date();
                long diff = after.getTime() - before.getTime();
                int new_time = time.intValue() + (int) diff;
                time = new Integer(new_time);
                int concept_nr = i + 1;
                double t = (double) time.intValue() / concept_nr;
                t = t / 1000.0;
                System.out.println("Average time: " + t + " sec. for " + concept_nr + " concepts.");
                System.out.println();
                for (int j = 0; j < rankedTextIDs.size(); j++) {
                    Vector entry = (Vector) rankedTextIDs.get(j);
                    String text_id = (String) entry.get(0);
                    Double rank = (Double) entry.get(1);
                    String text_table_name = (String) entry.get(2);
                    if (rank.doubleValue() > 0) {
                        out.write(id + "\t" + text_id + "\tr_ca\t" + rank + "\t\\N\t" + text_table_name + "\n");
                    }
                }
            }
            System.out.println("End of getting rankedTextIDs: " + df.format(new java.util.Date()));
            System.out.println();
        } else {
            if (!split) {
                int nrTextLines = -1;
                try {
                    BufferedReader in = new BufferedReader(new FileReader(nrentriesfile));
                    String inputline = in.readLine();
                    nrTextLines = (new Integer(inputline)).intValue();
                    in.close();
                } catch (FileNotFoundException e) {
                    String query = "select count(*) from " + text_base;
                    ResultSet rs = executeQuery(db, query);
                    rs.next();
                    nrTextLines = (new Integer((String) rs.getString(1))).intValue();
                }
                int nrIndices = (nrTextLines / partiallines) + 1;
                for (int indnr = 0; indnr < nrIndices; indnr++) {
                    String index = "text_free_text_" + (indnr + 1) + "_idx";
                    if (ramdisk) {
                        String move_to_ramdisk = ramdiskpath + "/run_make.sh " + ramdiskpath + " " + ramdiskuser + " move_indexing_text_files " + index;
                        execCommand(move_to_ramdisk);
                    }
                    for (int i = 0; i < concepts.size(); i++) {
                        Vector concept = (Vector) concepts.get(i);
                        String id = (String) concept.get(0);
                        String main_concept_name = (String) concept.get(1);
                        String main_concept_name_stemmed = (String) concept.get(2);
                        Vector context = (Vector) contexts.get(i);
                        Vector multi_concepts = (Vector) superconcepts.get(i);
                        rankedTextIDs = getRankedTextIDs(db, context, main_concept_name, main_concept_name_stemmed, text_base, multi_concepts, (indnr + 1), partiallines);
                        for (int j = 0; j < rankedTextIDs.size(); j++) {
                            Vector entry = (Vector) rankedTextIDs.get(j);
                            String text_id = (String) entry.get(0);
                            Double rank = (Double) entry.get(1);
                            String text_table_name = (String) entry.get(2);
                            if (rank.doubleValue() > 0) {
                                out.write(id + "\t" + text_id + "\tr_ca\t" + rank + "\t\\N\t" + text_table_name + "\n");
                            }
                        }
                    }
                    if (ramdisk) {
                        String remove_to_ramdisk = ramdiskpath + "/run_make.sh " + ramdiskpath + " " + ramdiskuser + " remove_indexing_text_files " + index;
                        execCommand(remove_to_ramdisk);
                    }
                }
            } else {
                System.out.println("INDEXING OF SPLITTED TEXTS WITH PARTIAL INDICES NOT YET IMPLEMENTED");
            }
        }
        out.close();
    }

    public void start(String installpath, String dbdriver, String dburl, String dbuser, String dbpass, String splitstring, String splitext, String partialstring, int partiallines, String ramdiskstring, String ramdiskuser, String ramdiskpath, String nrentriesfile, String contextfile, String outputfile, int contextdepth, String applyfilter, String filterfile) {
        try {
            System.out.println("Start indexing ...");
            System.out.println();
            boolean split = false;
            if (splitstring.compareTo("TRUE") == 0) split = true;
            boolean partial = false;
            if (partialstring.compareTo("TRUE") == 0) partial = true;
            boolean ramdisk = false;
            if (ramdiskstring.compareTo("TRUE") == 0) ramdisk = true;
            Connection db = openDB(dbdriver, dburl, dbuser, dbpass);
            String sqlscript = installpath + "/indexing_get_context_name_stemmed_function_level_" + contextdepth + ".sql";
            String execscript = installpath + "/execute_sql.sh";
            int index = dburl.lastIndexOf("/");
            String dbname = dburl.substring(index + 1, dburl.length());
            String command = execscript + " " + dbname + " " + sqlscript;
            execCommand(command);
            getTexts(db, split, splitext, partial, partiallines, ramdisk, ramdiskuser, ramdiskpath, nrentriesfile, contextfile, outputfile, applyfilter, filterfile);
            closeDB(db);
            System.out.println("Finished indexing ...");
        } catch (Exception e) {
            System.out.println("Exception message (process_indexing): " + e.getMessage());
            e.printStackTrace();
        }
    }
}
