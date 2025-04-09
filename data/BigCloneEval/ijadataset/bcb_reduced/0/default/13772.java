import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.OutputStreamWriter;
import java.io.File;
import java.io.FileOutputStream;
import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.text.SimpleDateFormat;

public class BookTable extends AbstractTableModel {

    private String[] columnNames = { "Book Name", "Author", "Volumes", "Read", "End", "Burn", "Date", "Notes" };

    ArrayList<Book> books = new ArrayList<Book>();

    ArrayList<Book> display = new ArrayList<Book>();

    public ArrayList<String> authors = new ArrayList<String>();

    boolean modified = false;

    public int getColumnCount() {
        return columnNames.length;
    }

    public int getRowCount() {
        return display.size();
    }

    public String getColumnName(int col) {
        return columnNames[col];
    }

    public Object getValueAt(int row, int col) {
        if (col == 0) return display.get(row).getName();
        if (col == 1) return display.get(row).getAuthor();
        if (col == 2) return Book.toText(display.get(row).getList());
        if (col == 3) return display.get(row).getRead();
        if (col == 4) return display.get(row).getEnd();
        if (col == 5) return display.get(row).getBurn();
        if (col == 6) return new SimpleDateFormat("yyyy.MM.dd.mm.ss").format(display.get(row).getDate());
        if (col == 7) return display.get(row).getNotes();
        return null;
    }

    public Class getColumnClass(int c) {
        return getValueAt(0, c).getClass();
    }

    public boolean isCellEditable(int row, int col) {
        return true;
    }

    public void setValueAt(Object value, int row, int col) {
        int i;
        for (i = 0; i < books.size(); i++) if (books.get(i).getName().equals(display.get(row).getName())) break;
        if (col == 0) {
            modified = modified || !(books.get(row).getName().equals((String) value));
            books.get(i).setName((String) value);
            display.get(row).setName((String) value);
        } else if (col == 1) {
            modified = modified || !(books.get(row).getAuthor().equals((String) value));
            books.get(i).setAuthor((String) value);
            display.get(row).setAuthor((String) value);
            if (((String) value) != "" && !authors.contains((String) value)) {
                authors.add((String) value);
                Collections.sort(authors);
            }
        } else if (col == 2) {
            if (Book.valid((String) value)) {
                modified = modified || !(Book.toText(books.get(row).getList()).equals((String) value));
                books.get(i).setList(Book.toList((String) value));
                display.get(row).setList(Book.toList((String) value));
            }
        } else if (col == 3) {
            modified = modified || !(books.get(row).getRead().equals((Boolean) value));
            books.get(i).setRead((Boolean) value);
            display.get(row).setRead((Boolean) value);
        } else if (col == 4) {
            modified = modified || !(books.get(row).getEnd().equals((Boolean) value));
            books.get(i).setEnd((Boolean) value);
            display.get(row).setEnd((Boolean) value);
        } else if (col == 5) {
            modified = modified || !(books.get(row).getBurn().equals((Boolean) value));
            books.get(i).setBurn((Boolean) value);
            display.get(row).setBurn((Boolean) value);
        } else if (col == 6) {
            modified = modified || !(new SimpleDateFormat("yyyy.MM.dd.mm.ss").format(books.get(row).getDate()).equals((String) value));
            try {
                books.get(i).setDate(new SimpleDateFormat("yyyy.MM.dd.mm.ss").parse((String) value));
                display.get(row).setDate(new SimpleDateFormat("yyyy.MM.dd.mm.ss").parse((String) value));
            } catch (Exception e) {
            }
        } else if (col == 7) {
            modified = modified || !(books.get(row).getNotes().equals((String) value));
            books.get(i).setNotes((String) value);
            display.get(row).setNotes((String) value);
        }
        if (modified) {
            if (col != 6) {
                books.get(i).setDate(new Date(System.currentTimeMillis()));
                display.get(row).setDate(new Date(System.currentTimeMillis()));
                fireTableCellUpdated(row, 6);
            }
            fireTableCellUpdated(row, col);
        }
    }

    public void open(String file) {
        books = new ArrayList<Book>();
        display = new ArrayList<Book>();
        authors = new ArrayList<String>();
        authors.add("");
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF8"));
            String line;
            while ((line = br.readLine()) != null) {
                books.add(new Book(line.split(",")));
                display.add(new Book(line.split(",")));
                if (!authors.contains(line.split(",")[1])) authors.add(line.split(",")[1]);
            }
            br.close();
        } catch (Exception e) {
        }
        Collections.sort(authors);
        modified = false;
        fireTableDataChanged();
    }

    public void save(String file) throws Exception {
        Collections.sort(books, new Name());
        new File(file).createNewFile();
        PrintWriter pw = new PrintWriter(new OutputStreamWriter(new FileOutputStream(file), "UTF8"));
        for (Book b : books) {
            pw.println(b.getName() + "," + b.getAuthor() + "," + Book.toText(b.getList()) + "," + (b.getRead() ? "1" : "0") + "," + (b.getEnd() ? "1" : "0") + "," + (b.getBurn() ? "1" : "0") + "," + Book.formatDate(b.getDate()) + "," + b.getNotes());
        }
        pw.close();
        modified = false;
    }

    public void exportcsv(String file) throws Exception {
        Collections.sort(books, new Name());
        new File(file).createNewFile();
        PrintWriter pw = new PrintWriter(new OutputStreamWriter(new FileOutputStream(file), "UTF8"));
        pw.print(columnNames[0]);
        for (int i = 1; i < columnNames.length; i++) pw.print("," + columnNames[i]);
        pw.println();
        for (Book b : books) {
            pw.println(b.getName() + "," + b.getAuthor() + "," + Book.toText(b.getList()) + "," + (b.getRead() ? "1" : "0") + "," + (b.getEnd() ? "1" : "0") + "," + (b.getBurn() ? "1" : "0") + "," + Book.formatDate(b.getDate()) + "," + b.getNotes());
        }
        pw.close();
    }

    public void print() {
        for (Book b : books) b.print();
    }

    public void find(String s) {
        display = new ArrayList<Book>();
        for (Book b : books) if (b.matches(s)) display.add(b);
        fireTableDataChanged();
    }

    public boolean isModified() {
        return modified;
    }

    public void add(String n, String a, String l, Boolean r, Boolean e, Boolean b, String o) {
        books.add(new Book(n, a, l, r, e, b, new Date(System.currentTimeMillis()), o));
        display.add(new Book(n, a, l, r, e, b, new Date(System.currentTimeMillis()), o));
        if (a != "" && !authors.contains(a)) {
            authors.add(a);
            Collections.sort(authors);
        }
        modified = true;
        fireTableDataChanged();
    }

    public boolean contains(String s) {
        for (Book b : books) if (b.getName().equals(s)) return true;
        return false;
    }

    public void remove(String n) {
        for (int i = 0; i < books.size(); i++) if (books.get(i).getName().equals(n)) books.remove(i);
        for (int i = 0; i < display.size(); i++) if (display.get(i).getName().equals(n)) display.remove(i);
        modified = true;
        fireTableDataChanged();
    }

    public void newfile() {
        books = new ArrayList<Book>();
        display = new ArrayList<Book>();
        modified = true;
        fireTableDataChanged();
    }
}
