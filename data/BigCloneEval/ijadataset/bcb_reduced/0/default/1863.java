import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Pattern;

public class CSVHandler {

    public static void importFromCSV(File csvFile, String tableName, boolean hasColumnNames) throws FileNotFoundException {
        final Scanner scanner = new Scanner(csvFile);
        final List<String> columns = new ArrayList<String>();
        int lineNumber = 1;
        final String columnLine = scanner.nextLine() + "\n";
        final Scanner columnScanner = new Scanner(columnLine);
        columnScanner.useDelimiter(Pattern.compile(",|\n"));
        if (hasColumnNames) {
            while (columnScanner.hasNext()) {
                columns.add(columnScanner.next().trim());
            }
            lineNumber++;
        } else {
            int columnIndex = 0;
            while (columnScanner.hasNext()) {
                columnScanner.next();
                columns.add("column" + columnIndex++);
            }
        }
        columnScanner.close();
        for (int i = 0; i < columns.size(); i++) {
            System.out.println("column" + i + ": \"" + columns.get(i) + "\"");
        }
        while (scanner.hasNext()) {
            String recordLine = null;
            if (lineNumber == 1 && !hasColumnNames) {
                recordLine = columnLine;
            } else {
                recordLine = scanner.nextLine() + "\n";
            }
            lineNumber++;
            final Scanner recordScanner = new Scanner(recordLine);
            recordScanner.useDelimiter(Pattern.compile(",|\n"));
            int columnCount = 0;
            while (recordScanner.hasNext()) {
                System.out.print("\"");
                System.out.print(recordScanner.next().trim());
                System.out.print("\"");
                System.out.print(" ");
                columnCount++;
            }
            recordScanner.close();
            if (columnCount != columns.size()) {
                System.out.print("<<< unmatched record at line " + lineNumber);
            }
            System.out.println();
        }
    }

    public static void exportToCSV(File csvFile, String tableName, boolean hasColumnNames) throws SQLException, IOException {
        final DBManager mgr = new DBManager("SAMPLE");
        final ResultSet res = mgr.query("select * from " + tableName);
        final FileWriter writer = new FileWriter(csvFile);
        final ResultSetMetaData rsmd = res.getMetaData();
        int columns = rsmd.getColumnCount();
        if (hasColumnNames) {
            for (int i = 1; i <= columns; i++) {
                writer.append(rsmd.getColumnName(i));
                if (i < columns) writer.append(",");
            }
            writer.append("\n");
        }
        while (res.next()) {
            for (int i = 1; i <= columns; i++) {
                writer.append(res.getString(i));
                if (i < columns) writer.append(",");
            }
            writer.append("\n");
        }
        writer.close();
    }

    public static void testImportFromCSV() throws FileNotFoundException {
        final DBManager mgr = new DBManager("SAMPLE");
        importFromCSV(new File("test_no_column_unmatch.csv"), "Student", false);
    }

    public static void testExportToCSV() throws SQLException, IOException {
        final DBManager mgr = new DBManager("SAMPLE");
        final ResultSet res = mgr.query("select * from STUDENT");
        DBManager.printResultSet(res);
        exportToCSV(new File("test_export.csv"), "Student", true);
    }

    public static void main(String[] args) throws SQLException, IOException {
        testImportFromCSV();
        testExportToCSV();
    }
}
