import java.sql.*;
import javax.swing.table.*;
import javax.swing.*;
import java.awt.Color;

/**
 *
 * @author  rovitotv
 */
public class FinanceJ extends javax.swing.JFrame {

    private static String driver = "org.apache.derby.jdbc.EmbeddedDriver";

    private static String dbName = "FinanceJDB";

    private static String connectionURL = "jdbc:derby:" + dbName + ";create=true";

    private static Connection conn = null;

    private Account AccountDialog;

    private Category CategoryDialog;

    private Ledger LedgerDialog;

    private Reports ReportsDialog;

    private AccountTotalTableModel dataModel;

    public static void LoadDBDriver() {
        try {
            Class.forName(driver);
            System.out.println(driver + " loaded. ");
        } catch (java.lang.ClassNotFoundException e) {
            System.err.print("ClassNotFoundException: ");
            System.err.println(e.getMessage());
            System.out.println("\n    >>> Please check your CLASSPATH variable   <<<\n");
        }
    }

    public static void CreateDBConnection() {
        try {
            conn = DriverManager.getConnection(connectionURL);
            System.out.println("Connected to database " + dbName);
        } catch (Throwable e) {
            System.out.println(" . . . exception thrown:");
            errorPrint(e);
        }
    }

    public static void CreateDBTables() {
        String CreateStringAccount = "create table account (name varchar(50) primary key, description varchar(250))";
        String CreateStringCategory = "create table category (name varchar(50) primary key, description varchar(250), budget float)";
        String CreateStringLedger = "create table ledger (id INTEGER NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1),rec integer, tdate date, payee  varchar(50), description varchar(250), account varchar(50), category varchar(50), amount float)";
        Statement s;
        try {
            s = conn.createStatement();
            if (!DBUtils.ChkTableAccount(conn)) {
                System.out.println(" . . . . creating table account");
                s.execute(CreateStringAccount);
            }
            if (!DBUtils.ChkTableCategory(conn)) {
                System.out.println(" . . . . creating table category");
                s.execute(CreateStringCategory);
            }
            if (!DBUtils.ChkTableLedger(conn)) {
                System.out.println(" . . . . creating table ledger");
                s.execute(CreateStringLedger);
            }
            s.close();
        } catch (Throwable e) {
            System.out.println(" . . . exception thrown:");
            errorPrint(e);
        }
    }

    public static void ShutdownDB() {
        try {
            conn.close();
            System.out.println("Closed connection");
        } catch (Throwable e) {
            System.out.println(" . . . exception thrown:");
            errorPrint(e);
        }
        if (driver.equals("org.apache.derby.jdbc.EmbeddedDriver")) {
            boolean gotSQLExc = false;
            try {
                DriverManager.getConnection("jdbc:derby:;shutdown=true");
            } catch (SQLException se) {
                if (se.getSQLState().equals("XJ015")) {
                    gotSQLExc = true;
                }
            }
            if (!gotSQLExc) {
                System.out.println("Database did not shut down normally");
            } else {
                System.out.println("Database shut down normally");
            }
        }
    }

    public void UpdateTotal() {
        ResultSet LedgerResult;
        Statement s;
        String TotalStr;
        TotalStr = "$0.00";
        if (conn != null) {
            try {
                s = conn.createStatement();
                LedgerResult = s.executeQuery("select sum(amount) from ledger");
                while (LedgerResult.next()) {
                    if (LedgerResult.getFloat(1) <= 0) {
                        Color fg = new Color(255, 51, 50);
                        TotalLabel.setForeground(fg);
                    } else {
                        Color fg = new Color(0, 0, 0);
                        TotalLabel.setForeground(fg);
                    }
                    TotalStr = String.format("$%12.2f", LedgerResult.getFloat(1));
                    TotalLabel.setText(TotalStr);
                }
            } catch (Throwable e) {
                System.out.println(" . . . exception thrown: in AccountListTableModel getValueAt");
                e.printStackTrace();
            }
        }
    }

    /** Creates new form FinanceJ */
    public FinanceJ() {
        LoadDBDriver();
        CreateDBConnection();
        CreateDBTables();
        initComponents();
        LedgerDialog = new Ledger(this, true);
        LedgerDialog.setVisible(false);
        LedgerDialog.SetDBConnection(conn);
        AccountDialog = new Account(this, true);
        AccountDialog.setVisible(false);
        AccountDialog.SetDBConnection(conn);
        CategoryDialog = new Category(this, true);
        CategoryDialog.setVisible(false);
        CategoryDialog.SetDBConnection(conn);
        ReportsDialog = new Reports(this, true);
        ReportsDialog.setVisible(false);
        ReportsDialog.SetDBConnection(conn);
        dataModel = new AccountTotalTableModel(conn);
        AccountTotalTable.setModel(dataModel);
        AccountTotalTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        UpdateTotal();
    }

    private void initComponents() {
        ExitButton = new javax.swing.JButton();
        AccountButton = new javax.swing.JButton();
        CategoriesButton = new javax.swing.JButton();
        LedgerButton = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        AccountTotalTable = new javax.swing.JTable();
        ReportsButton = new javax.swing.JButton();
        TotalLabel = new javax.swing.JLabel();
        jLabel1 = new javax.swing.JLabel();
        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("FinanceJ");
        addWindowListener(new java.awt.event.WindowAdapter() {

            public void windowActivated(java.awt.event.WindowEvent evt) {
                formWindowActivated(evt);
            }

            public void windowClosed(java.awt.event.WindowEvent evt) {
                formWindowClosed(evt);
            }

            public void windowClosing(java.awt.event.WindowEvent evt) {
                formWindowClosing(evt);
            }
        });
        ExitButton.setText("Exit");
        ExitButton.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ExitButtonActionPerformed(evt);
            }
        });
        AccountButton.setText("Accounts");
        AccountButton.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                AccountButtonActionPerformed(evt);
            }
        });
        CategoriesButton.setText("Categories");
        CategoriesButton.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                CategoriesButtonActionPerformed(evt);
            }
        });
        LedgerButton.setText("Ledger");
        LedgerButton.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                LedgerButtonActionPerformed(evt);
            }
        });
        AccountTotalTable.setModel(new javax.swing.table.DefaultTableModel(new Object[][] { { null, null }, { null, null }, { null, null }, { null, null } }, new String[] { "Account", "Balance" }) {

            Class[] types = new Class[] { java.lang.String.class, java.lang.Float.class };

            boolean[] canEdit = new boolean[] { false, false };

            public Class getColumnClass(int columnIndex) {
                return types[columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit[columnIndex];
            }
        });
        jScrollPane1.setViewportView(AccountTotalTable);
        ReportsButton.setText("Reports");
        ReportsButton.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ReportsButtonActionPerformed(evt);
            }
        });
        TotalLabel.setForeground(new java.awt.Color(255, 51, 51));
        TotalLabel.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        TotalLabel.setText("$0.00");
        jLabel1.setText("Net Worth:");
        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup().addContainerGap().addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING).addGroup(layout.createSequentialGroup().addComponent(jLabel1).addGap(18, 18, 18).addComponent(TotalLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 173, javax.swing.GroupLayout.PREFERRED_SIZE)).addGroup(layout.createSequentialGroup().addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING).addComponent(CategoriesButton, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 119, Short.MAX_VALUE).addComponent(LedgerButton, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 119, Short.MAX_VALUE).addComponent(AccountButton, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 119, Short.MAX_VALUE).addComponent(ReportsButton, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 119, Short.MAX_VALUE).addComponent(ExitButton, javax.swing.GroupLayout.DEFAULT_SIZE, 119, Short.MAX_VALUE)).addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED).addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 375, Short.MAX_VALUE))).addContainerGap()));
        layout.setVerticalGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGroup(layout.createSequentialGroup().addGap(28, 28, 28).addComponent(LedgerButton).addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED).addComponent(CategoriesButton).addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED).addComponent(AccountButton).addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED).addComponent(ReportsButton).addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED).addComponent(ExitButton).addContainerGap(141, Short.MAX_VALUE)).addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup().addContainerGap(26, Short.MAX_VALUE).addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 223, javax.swing.GroupLayout.PREFERRED_SIZE).addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED).addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE).addComponent(TotalLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE).addComponent(jLabel1)).addGap(35, 35, 35)));
        pack();
    }

    private void formWindowClosing(java.awt.event.WindowEvent evt) {
        ShutdownDB();
    }

    private void ExitButtonActionPerformed(java.awt.event.ActionEvent evt) {
        dispose();
    }

    private void formWindowClosed(java.awt.event.WindowEvent evt) {
        ShutdownDB();
    }

    private void AccountButtonActionPerformed(java.awt.event.ActionEvent evt) {
        AccountDialog.setVisible(true);
    }

    private void CategoriesButtonActionPerformed(java.awt.event.ActionEvent evt) {
        CategoryDialog.setVisible(true);
    }

    private void LedgerButtonActionPerformed(java.awt.event.ActionEvent evt) {
        LedgerDialog.BuildAccountsComboBox();
        LedgerDialog.BuildCategoryComboBox();
        LedgerDialog.setVisible(true);
    }

    private void formWindowActivated(java.awt.event.WindowEvent evt) {
        dataModel.fireTableDataChanged();
        UpdateTotal();
    }

    private void ReportsButtonActionPerformed(java.awt.event.ActionEvent evt) {
        ReportsDialog.setVisible(true);
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(new Runnable() {

            public void run() {
                new FinanceJ().setVisible(true);
            }
        });
    }

    private javax.swing.JButton AccountButton;

    private javax.swing.JTable AccountTotalTable;

    private javax.swing.JButton CategoriesButton;

    private javax.swing.JButton ExitButton;

    private javax.swing.JButton LedgerButton;

    private javax.swing.JButton ReportsButton;

    private javax.swing.JLabel TotalLabel;

    private javax.swing.JLabel jLabel1;

    private javax.swing.JScrollPane jScrollPane1;

    /***     Exception reporting methods
     **      with special handling of SQLExceptions
     ***/
    static void errorPrint(Throwable e) {
        if (e instanceof SQLException) {
            SQLExceptionPrint((SQLException) e);
        } else {
            System.out.println("A non SQL error occured.");
            e.printStackTrace();
        }
    }

    static void SQLExceptionPrint(SQLException sqle) {
        while (sqle != null) {
            System.out.println("\n---SQLException Caught---\n");
            System.out.println("SQLState:   " + (sqle).getSQLState());
            System.out.println("Severity: " + (sqle).getErrorCode());
            System.out.println("Message:  " + (sqle).getMessage());
            sqle.printStackTrace();
            sqle = sqle.getNextException();
        }
    }
}

class AccountTotalTableModel extends AbstractTableModel {

    private String[] columnNames = { "Account", "Balance" };

    private Connection conn = null;

    public AccountTotalTableModel(Connection DBConn) {
        conn = DBConn;
    }

    public int getColumnCount() {
        return columnNames.length;
    }

    public int getRowCount() {
        ResultSet AccountResult;
        Statement s;
        int NumRecords = 0;
        if (conn != null) {
            try {
                s = conn.createStatement();
                AccountResult = s.executeQuery("select account, sum(amount) from ledger group by account");
                while (AccountResult.next()) {
                    NumRecords++;
                }
            } catch (Throwable e) {
                System.out.println(" . . . exception thrown: in AccountListTableModel getRowCount");
                e.printStackTrace();
            }
        }
        return NumRecords;
    }

    public String getColumnName(int col) {
        return columnNames[col];
    }

    public Object getValueAt(int row, int col) {
        ResultSet AccountResult;
        Statement s;
        int CurrentRow = 0;
        if (conn != null) {
            try {
                s = conn.createStatement();
                AccountResult = s.executeQuery("select account, sum(amount) from ledger group by account");
                while (AccountResult.next()) {
                    if (CurrentRow == row) {
                        if (col == 0) {
                            return AccountResult.getString(1);
                        } else if (col == 1) {
                            return AccountResult.getFloat(2);
                        }
                    }
                    CurrentRow++;
                }
            } catch (Throwable e) {
                System.out.println(" . . . exception thrown: in AccountListTableModel getValueAt");
                e.printStackTrace();
            }
        }
        return "";
    }

    public Class getColumnClass(int c) {
        return getValueAt(0, c).getClass();
    }

    public boolean isCellEditable(int row, int col) {
        return false;
    }
}
