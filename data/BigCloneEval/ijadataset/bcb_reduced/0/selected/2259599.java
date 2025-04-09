package gov.usda.gdpc.upload.parent;

import java.io.*;
import java.util.*;
import java.util.regex.*;
import java.sql.*;
import gov.usda.gdpc.database.*;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.hssf.usermodel.*;

/**
 *
 * @author  terryc
 */
public class USCultivars {

    public static final int NUM_COLUMNS = 3;

    public static final int CULTIVAR_INDEX = 0;

    public static final int PARENTS_INDEX = 1;

    public static final short CULTIVAR_COLUMN = 0;

    public static final short PARENTS_COLUMN = 1;

    public static final int XLS_ROW_NUM_INDEX = 2;

    private static final boolean DO_UPDATE = true;

    private DefaultJDBCConnection myConnection = null;

    private List myRecords = null;

    private List myTokens = null;

    private List myHeadNodes = null;

    private List myRows = null;

    private final Map myPassportIDs = new HashMap();

    private final Map mySynonymIDs = new HashMap();

    private final Map myStockIDs = new HashMap();

    /** Creates a new instance of USCultivars */
    public USCultivars(String filename, String driver, String url, String uid, String passwd) {
        init(filename);
        parseParents();
        createTrees();
        Node.updateSynonymMap();
        printTrees();
        if (DO_UPDATE) {
            makeConnection(driver, url, uid, passwd);
            updatePassport();
            updateSynonym();
            updateStock();
            updateStockParent();
        }
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        if ((args.length != 5) && (args.length != 4)) {
            System.out.println("\nUsage: USCultivars <.xls filename> <driver>, <url>, <userid>, <passwd>\n");
            System.out.println("Example: UScultivars_since_1971.xls com.mysql.jdbc.Driver " + "jdbc:mysql://mexicana.maize.cornell.edu:3306/aztec " + "userid passwd\n");
            System.exit(0);
        }
        String filename = args[0];
        String driver = args[1];
        String url = args[2];
        String userid = args[3];
        String passwd = null;
        if (args.length == 4) {
            passwd = "";
        } else {
            passwd = args[4];
        }
        USCultivars instance = new USCultivars(filename, driver, url, userid, passwd);
    }

    private void init(String filename) {
        List result = new ArrayList();
        String[] last = null;
        try {
            POIFSFileSystem fs = new POIFSFileSystem(new FileInputStream(filename));
            HSSFWorkbook hssfworkbook = new HSSFWorkbook(fs);
            for (int i = 0, n = hssfworkbook.getNumberOfSheets(); i < n; i++) {
                HSSFSheet sheet = hssfworkbook.getSheetAt(i);
                int rows = sheet.getPhysicalNumberOfRows();
                for (int j = 0; j < rows; j++) {
                    HSSFRow row = sheet.getRow(j);
                    if (row != null) {
                        try {
                            int cells = row.getPhysicalNumberOfCells();
                            if ((cells >= NUM_COLUMNS - 1) || (last != null)) {
                                String[] temp = new String[NUM_COLUMNS];
                                temp[XLS_ROW_NUM_INDEX] = String.valueOf(j + 1);
                                try {
                                    temp[CULTIVAR_INDEX] = row.getCell(CULTIVAR_COLUMN).getStringCellValue();
                                } catch (Exception e) {
                                }
                                HSSFCell parentsCell = row.getCell(PARENTS_COLUMN);
                                if (parentsCell == null) {
                                    temp[PARENTS_INDEX] = "";
                                } else {
                                    temp[PARENTS_INDEX] = parentsCell.getStringCellValue().trim();
                                }
                                if ((temp[CULTIVAR_INDEX] == null) || (temp[CULTIVAR_INDEX].length() == 0)) {
                                    if (last != null) {
                                        last[PARENTS_INDEX] = (last[PARENTS_INDEX] + temp[PARENTS_INDEX]);
                                    }
                                } else {
                                    result.add(temp);
                                    last = temp;
                                }
                            }
                        } catch (Exception e) {
                            System.out.println("problem reading row: " + j);
                            e.printStackTrace();
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        myRecords = result;
    }

    private void makeConnection(String driver, String url, String uid, String passwd) {
        myConnection = new DefaultJDBCConnection("NO_SOURCE", null, driver, url, uid, passwd);
    }

    private static void printList(List list) {
        Iterator itr = list.iterator();
        while (itr.hasNext()) {
            String[] current = (String[]) itr.next();
            for (int i = 0, n = current.length; i < n; i++) {
                System.out.print(current[i] + "  ");
            }
            System.out.println("");
        }
    }

    private static void printTokens(List list) {
        System.out.println("USCultivars: printTokens: start.");
        for (int i = 0; i < list.size(); i++) {
            Object current = list.get(i);
            System.out.print(i + ": " + current);
        }
        System.out.println("");
    }

    private String getSynonymID(String name) {
        String result = getIDfromMap(mySynonymIDs, name);
        if (result != null) {
            return result;
        }
        StringBuffer buffer = new StringBuffer();
        buffer.delete(0, buffer.length());
        buffer.append("select div_synonym_id from div_synonym where synonym='");
        buffer.append(name);
        buffer.append("'");
        String statement = buffer.toString();
        System.out.println("USCultivars: getSynonymID: statement: " + statement);
        ResultSet rs = null;
        try {
            rs = myConnection.executeQueryNoScroll(statement);
            if (rs.next()) {
                String id = rs.getString(1);
                System.out.println("USCultivars: getSynonymID: adding id: " + id);
                putIDinMap(mySynonymIDs, name, id);
                result = id;
            }
            if (rs.next()) {
                System.out.println("USCultivars: getSynonymID: duplicate synonym names already in database for: " + name);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                rs.close();
            } catch (Exception ex) {
            }
        }
        return result;
    }

    private void updateSynonym() {
        String lastInsertedID = "select last_insert_id()";
        Map synonymMap = Node.getSynonymMap();
        System.out.println("number of synonyms in map: " + synonymMap.size());
        Iterator itr = synonymMap.keySet().iterator();
        StringBuffer buffer = new StringBuffer();
        while (itr.hasNext()) {
            String synonym = (String) itr.next();
            Node node = (Node) synonymMap.get(synonym);
            String accename = node.getName();
            String id = getSynonymID(synonym);
            if (id == null) {
                String passportID = getIDfromMap(myPassportIDs, accename);
                if (passportID == null) {
                    System.out.println("USCultivars: updateSynonym: no matching passport record in database for synonym: " + synonym);
                } else {
                    buffer.delete(0, buffer.length());
                    buffer.append("insert into div_synonym (div_passport_id, synonym) values");
                    buffer.append(" (");
                    buffer.append(passportID);
                    buffer.append(", '");
                    buffer.append(synonym);
                    buffer.append("')");
                    String statement = buffer.toString();
                    System.out.println("USCultivars: updateSynonym: statement: " + statement + ";");
                    try {
                        myConnection.executeUpdate(statement);
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                    ResultSet rs = null;
                    try {
                        rs = myConnection.executeQuery(lastInsertedID);
                        if (rs.next()) {
                            String newID = rs.getString(1);
                            System.out.println("USCultivars: updateSynonym: adding newID: " + newID);
                            putIDinMap(mySynonymIDs, synonym, newID);
                        }
                    } catch (SQLException e) {
                        e.printStackTrace();
                    } finally {
                        try {
                            rs.close();
                        } catch (Exception ex) {
                        }
                    }
                }
            }
        }
        System.out.println("number of synonyms in map: " + mySynonymIDs.size());
    }

    private String getStockKey(String passportID, int generation) {
        StringBuffer buffer = new StringBuffer();
        if ((passportID == null) || (passportID.length() == 0)) {
            throw new IllegalStateException("USCultivars: getStockKey: passport ID can't be null or empty");
        }
        buffer.append(passportID);
        if (generation != 0) {
            buffer.append(":");
            buffer.append(generation);
        }
        return buffer.toString();
    }

    private String getStockID(String passportID, int generation) {
        StringBuffer buffer = new StringBuffer();
        String key = getStockKey(passportID, generation);
        String result = getIDfromMap(myStockIDs, key);
        if (result != null) {
            return result;
        }
        buffer.delete(0, buffer.length());
        buffer.append("select div_stock.div_stock_id, div_stock.div_passport_id, ");
        buffer.append("div_generation.selfing_number from div_passport, div_stock left join div_generation ");
        buffer.append("on div_stock.div_generation_id=div_generation.div_generation_id ");
        buffer.append("where div_stock.div_passport_id=div_passport.div_passport_id ");
        buffer.append("and div_stock.div_passport_id=");
        buffer.append(passportID);
        if (generation != 0) {
            buffer.append(" and div_generation.selfing_number='");
            buffer.append(generation);
            buffer.append("'");
        } else {
            buffer.append(" and div_stock.div_generation_id is null");
        }
        String statement = buffer.toString();
        System.out.println("USCultivars: getStockID: statement: " + statement);
        ResultSet rs = null;
        try {
            rs = myConnection.executeQueryNoScroll(statement);
            if (rs.next()) {
                String id = rs.getString(1);
                System.out.println("USCultivars: getStockID: adding id: " + id);
                putIDinMap(myStockIDs, key, id);
                result = id;
            }
            if (rs.next()) {
                System.out.println("USCultivars: getStockID: duplicate synonym names already in database for: " + key);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                rs.close();
            } catch (Exception ex) {
            }
        }
        return result;
    }

    private void updateStock() {
        String lastInsertedID = "select last_insert_id()";
        Map instanceMap = Node.getInstances();
        Iterator itr = instanceMap.values().iterator();
        StringBuffer buffer = new StringBuffer();
        while (itr.hasNext()) {
            Node current = (Node) itr.next();
            String accename = current.getName();
            int generation = current.getGeneration();
            String passportID = getIDfromMap(myPassportIDs, accename);
            String stockID = getStockID(passportID, generation);
            if (stockID != null) {
            } else if (passportID != null) {
                String generationID = null;
                if (generation != 0) {
                    buffer.delete(0, buffer.length());
                    buffer.append("insert into div_generation (selfing_number) values");
                    buffer.append(" ('");
                    buffer.append(generation);
                    buffer.append("')");
                    String statement = buffer.toString();
                    System.out.println("USCultivars: updateStock: statement: " + statement + ";");
                    ResultSet rs = null;
                    try {
                        myConnection.executeUpdate(statement);
                        rs = myConnection.executeQuery(lastInsertedID);
                        if (rs.next()) {
                            generationID = rs.getString(1);
                        }
                    } catch (SQLException e) {
                        e.printStackTrace();
                    } finally {
                        try {
                            rs.close();
                        } catch (Exception ex) {
                        }
                    }
                }
                buffer.delete(0, buffer.length());
                buffer.append("insert into div_stock (div_passport_id, div_generation_id) values");
                buffer.append(" (");
                buffer.append(passportID);
                buffer.append(", ");
                buffer.append(generationID);
                buffer.append(")");
                String statement = buffer.toString();
                System.out.println("USCultivars: updateStock: statement: " + statement + ";");
                ResultSet rs = null;
                try {
                    myConnection.executeUpdate(statement);
                    rs = myConnection.executeQuery(lastInsertedID);
                    if (rs.next()) {
                        String newID = rs.getString(1);
                        System.out.println("USCultivars: updateStock: adding newID: " + newID);
                        putIDinMap(myStockIDs, getStockKey(passportID, generation), newID);
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                } finally {
                    try {
                        rs.close();
                    } catch (Exception ex) {
                    }
                }
            } else {
                System.out.println("USCultivars: updateStock: no record in database div_passport for accession: " + accename);
            }
        }
        System.out.println("number of stocks in map: " + myStockIDs.size());
    }

    private String getIDfromMap(Map map, String name) {
        return (String) map.get(name.toUpperCase());
    }

    private void putIDinMap(Map map, String name, String id) {
        map.put(name.toUpperCase(), id);
    }

    private String getPassportID(String name) {
        String result = getIDfromMap(myPassportIDs, name);
        if (result != null) {
            return result;
        }
        StringBuffer buffer = new StringBuffer();
        buffer.delete(0, buffer.length());
        buffer.append("select div_passport_id from div_passport where accename='");
        buffer.append(name);
        buffer.append("'");
        String statement = buffer.toString();
        System.out.println("USCultivars: getPassportID: statement: " + statement);
        ResultSet rs = null;
        try {
            rs = myConnection.executeQueryNoScroll(statement);
            if (rs.next()) {
                String id = rs.getString(1);
                System.out.println("USCultivars: getPassportID: adding id: " + id);
                putIDinMap(myPassportIDs, name, id);
                result = id;
            }
            if (rs.next()) {
                System.out.println("USCultivars: getPassportID: duplicate accession names already in database for: " + name);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                rs.close();
            } catch (Exception ex) {
            }
        }
        return result;
    }

    private void updatePassport() {
        String lastInsertedID = "select last_insert_id()";
        Iterator itr = Node.getInstances().values().iterator();
        StringBuffer buffer = new StringBuffer();
        while (itr.hasNext()) {
            Node current = (Node) itr.next();
            String accename = current.getName();
            String accenum = current.getAcceNum();
            String id = getPassportID(accename);
            if (id == null) {
                buffer.delete(0, buffer.length());
                buffer.append("insert into div_passport (accename, accenumb) values");
                buffer.append(" ('");
                buffer.append(accename);
                buffer.append("', '");
                buffer.append(accenum);
                buffer.append("')");
                String statement = buffer.toString();
                System.out.println("USCultivars: updatePassport: statement: " + statement + ";");
                try {
                    myConnection.executeUpdate(statement);
                } catch (SQLException e) {
                    e.printStackTrace();
                }
                ResultSet rs = null;
                try {
                    rs = myConnection.executeQuery(lastInsertedID);
                    if (rs.next()) {
                        String newID = rs.getString(1);
                        System.out.println("USCultivars: updatePassport: adding newID: " + newID);
                        putIDinMap(myPassportIDs, accename, newID);
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                } finally {
                    try {
                        rs.close();
                    } catch (Exception ex) {
                    }
                }
            }
        }
        System.out.println("number of accessions in map: " + myPassportIDs.size());
    }

    private void updateStockParent() {
        Map instanceMap = Node.getInstances();
        Iterator itr = instanceMap.values().iterator();
        StringBuffer buffer = new StringBuffer();
        System.out.println("USCultivars: updateStockParent: number of instances: " + instanceMap.size());
        while (itr.hasNext()) {
            Node current = (Node) itr.next();
            String accename = current.getName();
            System.out.println("USCultivars: updateStockParent: name: " + accename);
            List parents = current.getParents();
            if ((parents != null) && (parents.size() > 0)) {
                List roles = current.getRoles();
                int generation = current.getGeneration();
                String recurrentName = current.getRecurrent();
                String passportID = getIDfromMap(myPassportIDs, accename);
                String key = getStockKey(passportID, generation);
                String stockID = getIDfromMap(myStockIDs, key);
                if (stockID == null) {
                    System.out.println("USCultivars: updateStockParent: IllegalState: stock should already exist: " + accename + "   generation: " + generation);
                } else {
                    List stockParentList = getStockParentID(stockID);
                    if (needToAddParents(stockParentList, parents)) {
                        for (int i = 0, n = parents.size(); i < n; i++) {
                            String currentParent = (String) parents.get(i);
                            Node parent = Node.getInstance(currentParent);
                            System.out.println("USCultivars: updateStockParent: current parent: " + currentParent);
                            String role = (String) roles.get(i);
                            int generationParent = parent.getGeneration();
                            String passportIDParent = getIDfromMap(myPassportIDs, currentParent);
                            String keyParent = getStockKey(passportIDParent, generationParent);
                            String parentID = getIDfromMap(myStockIDs, keyParent);
                            boolean recurrent = false;
                            if ((recurrentName != null) && (recurrentName.equals(currentParent))) {
                                recurrent = true;
                            }
                            buffer.delete(0, buffer.length());
                            buffer.append("insert into div_stock_parent (div_stock_id, ");
                            buffer.append("div_parent_id, recurrent, role) values");
                            buffer.append(" (");
                            buffer.append(stockID);
                            buffer.append(", ");
                            buffer.append(parentID);
                            buffer.append(", ");
                            buffer.append(recurrent);
                            buffer.append(", '");
                            buffer.append(role);
                            buffer.append("')");
                            String statement = buffer.toString();
                            System.out.println("USCultivars: updateStockParent: statement: " + statement + ";");
                            try {
                                myConnection.executeUpdate(statement);
                            } catch (SQLException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
            }
        }
    }

    private boolean needToAddParents(List dbParents, List parents) {
        if (dbParents.size() == 0) {
            return true;
        }
        if (dbParents.size() != parents.size()) {
            System.out.println("USCultivars: needToAddParents: number of parents in database doesn't match file.");
            return false;
        }
        Iterator itr = dbParents.iterator();
        while (itr.hasNext()) {
            String name = (String) itr.next();
            if (!parents.contains(name)) {
                System.out.println("USCultivars: needToAddParents: parents do not match parents in database ");
                return false;
            }
        }
        return false;
    }

    private List getStockParentID(String stockID) {
        List result = new ArrayList();
        StringBuffer buffer = new StringBuffer();
        buffer.delete(0, buffer.length());
        buffer.append("select div_passport.accename, ");
        buffer.append("div_stock_parent.div_stock_parent_id ");
        buffer.append("from div_passport, div_stock, div_stock_parent ");
        buffer.append("where div_stock_parent.div_parent_id=div_stock.div_stock_id ");
        buffer.append("and div_stock.div_passport_id=div_passport.div_passport_id ");
        buffer.append("and div_stock_parent.div_stock_id=");
        buffer.append(stockID);
        String statement = buffer.toString();
        System.out.println("USCultivars: getStockParentID: statement: " + statement);
        ResultSet rs = null;
        try {
            rs = myConnection.executeQueryNoScroll(statement);
            while (rs.next()) {
                String name = rs.getString(1);
                String id = rs.getString(2);
                result.add(name);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                rs.close();
            } catch (Exception ex) {
            }
        }
        return result;
    }

    private void parseParents() {
        myTokens = new ArrayList();
        String regExp = "(/((/)|(\\d/))?)|\\*|\\(|\\)|,";
        System.out.println("\nregular expression: " + regExp + "\n");
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile(regExp);
        Matcher matcher = pattern.matcher("null");
        Iterator itr = myRecords.iterator();
        while (itr.hasNext()) {
            String[] current = (String[]) itr.next();
            String parents = current[PARENTS_INDEX];
            List tokens = new ArrayList();
            matcher = matcher.reset(parents);
            int position = 0;
            while (matcher.find()) {
                if (matcher.start() != position) {
                    String temp = parents.substring(position, matcher.start());
                    temp = temp.trim();
                    if (temp.length() != 0) {
                        tokens.add(temp);
                    }
                }
                tokens.add(Token.getInstance(matcher.group()));
                position = matcher.end();
            }
            if (position != parents.length()) {
                String temp = parents.substring(position, parents.length());
                temp = temp.trim();
                if (temp.length() != 0) {
                    tokens.add(temp);
                }
            }
            if ((tokens.size() == 0) && (parents.length() != 0)) {
                tokens.add(parents);
            }
            myTokens.add(tokens);
        }
    }

    private void createTrees() {
        myHeadNodes = new ArrayList();
        myRows = new ArrayList();
        for (int i = 0, n = myRecords.size(); i < n; i++) {
            String[] current = (String[]) myRecords.get(i);
            System.out.println("USCultivars: createTrees: row: " + current[XLS_ROW_NUM_INDEX] + "   parents: " + current[PARENTS_INDEX]);
            printTokens((List) myTokens.get(i));
            try {
                Node head = Node.getInstance(current[CULTIVAR_INDEX], (List) myTokens.get(i), current[XLS_ROW_NUM_INDEX]);
                myHeadNodes.add(head.getName());
                myRows.add(head.getRow());
            } catch (Exception e) {
                System.out.println(e.getMessage());
                e.printStackTrace();
            }
        }
    }

    private void printTrees() {
        for (int i = 0, n = myHeadNodes.size(); i < n; i++) {
            System.out.println("--------------------");
            String str = (String) myHeadNodes.get(i);
            Node current = Node.getInstance(str);
            if (current != null) {
                String[] record = findRecord(current.getRow());
                System.out.println("Row: " + record[XLS_ROW_NUM_INDEX] + "  Cultivar: " + record[CULTIVAR_INDEX] + "  Parents: " + record[PARENTS_INDEX]);
                current.printTree(0, null);
            } else {
                current = Node.getSynonym(str);
                String[] record = findRecord((String) myRows.get(i));
                System.out.println("Row: " + record[XLS_ROW_NUM_INDEX] + "  Cultivar: " + record[CULTIVAR_INDEX] + "  Parents: " + record[PARENTS_INDEX]);
                System.out.println("Cultivar: " + str + " is synonym of: " + current.getName() + " (row: " + current.getRow() + ")");
            }
        }
    }

    public String[] findRecord(String row) {
        Iterator itr = myRecords.iterator();
        while (itr.hasNext()) {
            String[] current = (String[]) itr.next();
            if (current[XLS_ROW_NUM_INDEX].equals(row)) {
                return current;
            }
        }
        return null;
    }

    public static void printXLS(String filename) {
        try {
            POIFSFileSystem fs = new POIFSFileSystem(new FileInputStream(filename));
            HSSFWorkbook hssfworkbook = new HSSFWorkbook(fs);
            for (int i = 0, n = hssfworkbook.getNumberOfSheets(); i < n; i++) {
                HSSFSheet sheet = hssfworkbook.getSheetAt(i);
                int rows = sheet.getPhysicalNumberOfRows();
                for (int j = 0; j < rows; j++) {
                    HSSFRow row = sheet.getRow(j);
                    if (row != null) {
                        int cells = row.getPhysicalNumberOfCells();
                        for (int k = 0; k < cells; k++) {
                            HSSFCell cell = row.getCell((short) k);
                            if (cell != null) {
                                String value = null;
                                switch(cell.getCellType()) {
                                    case HSSFCell.CELL_TYPE_FORMULA:
                                        value = "FORMULA";
                                        break;
                                    case HSSFCell.CELL_TYPE_NUMERIC:
                                        value = Double.toString(cell.getNumericCellValue());
                                        break;
                                    case HSSFCell.CELL_TYPE_STRING:
                                        value = cell.getStringCellValue();
                                        break;
                                    default:
                                }
                                System.out.print(value + "  ");
                            } else {
                                System.out.print("NULL  ");
                            }
                        }
                        System.out.println("");
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
