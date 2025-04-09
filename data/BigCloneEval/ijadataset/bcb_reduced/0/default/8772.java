import java.awt.BorderLayout;
import javax.swing.JPanel;
import javax.swing.JFrame;
import javax.swing.JButton;
import java.awt.Rectangle;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Vector;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.JList;
import java.awt.Dimension;
import javax.swing.ComboBoxEditor;
import javax.swing.JTextArea;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JComboBox;

public class GetDataPaciente extends JFrame {

    private static final long serialVersionUID = 1L;

    private JPanel jContentPane = null;

    private JButton jButton = null;

    private JLabel labelSearchName = null;

    private JTextField textSearchFor = null;

    private JLabel labelResult = null;

    private JLabel labelNome = null;

    private JLabel labelIdade = null;

    private JTextField textIdade = null;

    private JTextArea textAreaMessage = null;

    private Connection connection;

    private JScrollPane jScrollPane = null;

    private JComboBox comboNome = null;

    private Vector v_idade = new Vector();

    private int index = 0;

    /**
	 * This is the default constructor
	 */
    public GetDataPaciente() {
        super();
        try {
            Class.forName("com.mysql.jdbc.Driver").newInstance();
        } catch (Exception e) {
            System.err.println("Unable to find and load driver");
            System.exit(1);
        }
        initialize();
    }

    /**
	 * This method initializes this
	 * 
	 * @return void
	 */
    private void initialize() {
        this.setSize(303, 267);
        this.setContentPane(getJContentPane());
        this.setTitle("JFrame");
        connectToDB();
    }

    public void connectToDB() {
        try {
            connection = DriverManager.getConnection("jdbc:mysql://localhost/bcmed?user=root&password=bentes123");
        } catch (SQLException e) {
            System.out.println("Unable to connect to database");
            System.exit(1);
        }
    }

    private void displaySQLErrors(SQLException e) {
        textAreaMessage.append("SQLException: " + e.getMessage() + "\n");
        textAreaMessage.append("SQLState: " + e.getSQLState() + "\n");
        textAreaMessage.append("VendorError: " + e.getErrorCode() + "\n");
    }

    private void loadPaciente() {
        Vector<String> v_nomes = new Vector<String>();
        try {
            Statement statement = connection.createStatement();
            ResultSet rs = statement.executeQuery("SELECT * FROM paciente WHERE nome LIKE '" + textSearchFor.getText() + "%'");
            while (rs.next()) {
                System.out.println(rs.getString("nome"));
                v_idade.addElement(rs.getString("idade"));
                v_nomes.addElement(rs.getString("nome"));
            }
            rs.close();
        } catch (SQLException e) {
            displaySQLErrors(e);
        }
        for (int i = 0; i < v_nomes.size(); i++) {
            comboNome.addItem(v_nomes.get(i));
        }
    }

    /**
	 * This method initializes jContentPane
	 * 
	 * @return javax.swing.JPanel
	 */
    private JPanel getJContentPane() {
        if (jContentPane == null) {
            labelIdade = new JLabel();
            labelIdade.setBounds(new Rectangle(181, 108, 43, 16));
            labelIdade.setText("Idade: ");
            labelNome = new JLabel();
            labelNome.setBounds(new Rectangle(81, 107, 42, 16));
            labelNome.setText("Nome:");
            labelResult = new JLabel();
            labelResult.setBounds(new Rectangle(12, 75, 62, 16));
            labelResult.setText("Results:");
            labelSearchName = new JLabel();
            labelSearchName.setBounds(new Rectangle(21, 8, 69, 23));
            labelSearchName.setText("Search for:");
            jContentPane = new JPanel();
            jContentPane.setLayout(null);
            jContentPane.add(getJButton(), null);
            jContentPane.add(labelSearchName, null);
            jContentPane.add(getTextSearchFor(), null);
            jContentPane.add(labelResult, null);
            jContentPane.add(labelNome, null);
            jContentPane.add(labelIdade, null);
            jContentPane.add(getTextIdade(), null);
            jContentPane.add(getTextAreaMessage(), null);
            jContentPane.add(getJScrollPane(), null);
            jContentPane.add(getComboNome(), null);
        }
        return jContentPane;
    }

    /**
	 * This method initializes jButton	
	 * 	
	 * @return javax.swing.JButton	
	 */
    private JButton getJButton() {
        if (jButton == null) {
            jButton = new JButton();
            jButton.setBounds(new Rectangle(177, 41, 65, 19));
            jButton.setText("find");
            jButton.addActionListener(new java.awt.event.ActionListener() {

                public void actionPerformed(java.awt.event.ActionEvent e) {
                    comboNome.removeAllItems();
                    v_idade.clear();
                    loadPaciente();
                }
            });
        }
        return jButton;
    }

    /**
	 * This method initializes textSearchFor	
	 * 	
	 * @return javax.swing.JTextField	
	 */
    private JTextField getTextSearchFor() {
        if (textSearchFor == null) {
            textSearchFor = new JTextField();
            textSearchFor.setBounds(new Rectangle(95, 8, 155, 23));
            textSearchFor.setText("Type to search");
        }
        return textSearchFor;
    }

    /**
	 * This method initializes textIdade	
	 * 	
	 * @return javax.swing.JTextField	
	 */
    private JTextField getTextIdade() {
        if (textIdade == null) {
            textIdade = new JTextField();
            textIdade.setBounds(new Rectangle(167, 132, 71, 20));
        }
        return textIdade;
    }

    /**
	 * This method initializes textAreaMessage	
	 * 	
	 * @return javax.swing.JTextArea	
	 */
    private JTextArea getTextAreaMessage() {
        if (textAreaMessage == null) {
            textAreaMessage = new JTextArea();
            textAreaMessage.setBounds(new Rectangle(21, 162, 242, 36));
        }
        return textAreaMessage;
    }

    /**
	 * This method initializes jScrollPane	
	 * 	
	 * @return javax.swing.JScrollPane	
	 */
    private JScrollPane getJScrollPane() {
        if (jScrollPane == null) {
            jScrollPane = new JScrollPane(textAreaMessage);
            jScrollPane.setBounds(new Rectangle(21, 162, 242, 58));
        }
        return jScrollPane;
    }

    /**
	 * This method initializes comboNome	
	 * 	
	 * @return javax.swing.JComboBox	
	 */
    private JComboBox getComboNome() {
        if (comboNome == null) {
            comboNome = new JComboBox();
            comboNome.setBounds(new Rectangle(50, 129, 104, 25));
            comboNome.addActionListener(new java.awt.event.ActionListener() {

                public void actionPerformed(java.awt.event.ActionEvent e) {
                    System.out.println("actionPerformed()");
                    if (comboNome.getSelectedItem() != null) {
                        index = comboNome.getSelectedIndex();
                        textIdade.setText((String) v_idade.get(index));
                    } else {
                        textIdade.setText("");
                    }
                }
            });
        }
        return comboNome;
    }
}
