import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Vector;
import javax.swing.table.DefaultTableModel;

public class EpisodeFileTableModel extends DefaultTableModel {

    private String[] columnNames = { "Filename", "Directory", "Seen", "md5" };

    private Vector rows = new Vector();

    private Connection conn = null;

    private Statement stat = null;

    private String query;

    public void updateInternalData(int episode_id) {
        query = "select FileName, baseDir, Seen, md5, id from EpisodeFile where Episode_id=" + episode_id + ";";
        this.queryDB(query);
    }

    public String getColumnName(int col) {
        return columnNames[col];
    }

    public int getRowCount() {
        if (rows != null) {
            return rows.size();
        }
        return 11;
    }

    public int getColumnCount() {
        return 4;
    }

    public boolean isCellEditable(int row, int col) {
        if (col == 2) {
            return true;
        } else {
            return false;
        }
    }

    public Object getValueAt(int row, int col) {
        if (((Vector) rows.get(row)).get(col) != null) {
            return ((Vector) rows.get(row)).get(col);
        }
        return 0;
    }

    public Class getColumnClass(int column) {
        if (column == 2) {
            try {
                return Class.forName("java.lang.Boolean");
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        } else {
            return getValueAt(0, column).getClass();
        }
        return null;
    }

    public void setValueAt(Object value, int row, int column) {
        System.out.println(value);
        if (column == 2) {
            int id = (Integer) ((Vector) rows.get(row)).get(4);
            try {
                Class.forName("org.sqlite.JDBC");
                conn = DriverManager.getConnection("jdbc:sqlite:test.db");
                stat = conn.createStatement();
                int v = (Boolean) value ? 0 : 1;
                System.out.println(v);
                System.out.println("update EpisodeFile set seen=" + ((Boolean) value ? 1 : 0) + " where id=" + id + ";");
                stat.executeUpdate("update EpisodeFile set seen=" + ((Boolean) value ? 1 : 0) + " where id=" + id + ";");
                ((Vector) (rows.get(row))).set(2, value);
                fireTableCellUpdated(row, column);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void queryDB(String q) {
        rows = null;
        rows = new Vector();
        ResultSet rs;
        try {
            Class.forName("org.sqlite.JDBC");
            conn = DriverManager.getConnection("jdbc:sqlite:test.db");
            stat = conn.createStatement();
            rs = stat.executeQuery(q);
            while (rs.next()) {
                Vector cols = new Vector();
                cols.add(0, rs.getString("FileName"));
                cols.add(1, rs.getString("baseDir"));
                cols.add(2, rs.getBoolean("Seen"));
                cols.add(3, rs.getString("md5"));
                cols.add(4, rs.getInt("id"));
                rows.add(cols);
            }
            stat.close();
            conn.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        this.fireTableDataChanged();
    }

    public EpisodeFileTableModel() {
    }

    public EpisodeFileTableModel(int rowCount, int columnCount) {
        super(rowCount, columnCount);
    }

    public EpisodeFileTableModel(Vector columnNames, int rowCount) {
        super(columnNames, rowCount);
    }

    public EpisodeFileTableModel(Object[] columnNames, int rowCount) {
        super(columnNames, rowCount);
    }

    public EpisodeFileTableModel(Vector data, Vector columnNames) {
        super(data, columnNames);
    }

    public EpisodeFileTableModel(Object[][] data, Object[] columnNames) {
        super(data, columnNames);
    }
}
