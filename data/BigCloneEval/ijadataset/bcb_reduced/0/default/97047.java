import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;

/**
 *A public class
 */
public class AddBooks extends JInternalFrame {

    private JPanel northPanel = new JPanel();

    private JLabel northLabel = new JLabel("BOOK INFORMATION");

    private JPanel centerPanel = new JPanel();

    private JPanel informationLabelPanel = new JPanel();

    private JLabel[] informationLabel = new JLabel[10];

    private JLabel lblShelfNo = new JLabel(" Shelf No");

    private JTextField txtShelfNo = new JTextField();

    private String[] informationString = { " The book subject: ", " The book title: ", " The name of the Author(s): ", " The name of the Publisher: ", " Copyright for the book: ", " The edition number: ", " The number of Pages: ", " ISBN for the book: ", " The number of copies: ", " The name of the Library: " };

    private JPanel informationTextFieldPanel = new JPanel();

    private JTextField[] informationTextField = new JTextField[10];

    private JPanel insertInformationButtonPanel = new JPanel();

    private JButton insertInformationButton = new JButton("Insert the Information");

    private JPanel southPanel = new JPanel();

    private JButton OKButton = new JButton("Exit");

    private Books book;

    private String[] data;

    private boolean availble = true;

    public boolean isCorrect() {
        data = new String[10];
        for (int i = 0; i < informationLabel.length; i++) {
            if (!informationTextField[i].getText().equals("")) {
                data[i] = informationTextField[i].getText();
            } else {
                return false;
            }
        }
        return true;
    }

    public void clearTextField() {
        for (int i = 0; i < informationTextField.length; i++) {
            informationTextField[i].setText(null);
        }
        txtShelfNo.setText(null);
    }

    public AddBooks() {
        super("Add Books", false, true, false, true);
        setFrameIcon(new ImageIcon(ClassLoader.getSystemResource("images/Add16.gif")));
        Container cp = getContentPane();
        northPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
        northLabel.setFont(new Font("Tahoma", Font.BOLD, 14));
        northPanel.add(northLabel);
        cp.add("North", northPanel);
        centerPanel.setLayout(new BorderLayout());
        centerPanel.setBorder(BorderFactory.createTitledBorder("Add a new book:"));
        informationLabelPanel.setLayout(new GridLayout(11, 1, 1, 1));
        for (int i = 0; i < informationLabel.length; i++) {
            informationLabelPanel.add(informationLabel[i] = new JLabel(informationString[i]));
            informationLabel[i].setFont(new Font("Tahoma", Font.BOLD, 11));
        }
        centerPanel.add("West", informationLabelPanel);
        informationTextFieldPanel.setLayout(new GridLayout(11, 1, 1, 1));
        for (int i = 0; i < informationTextField.length; i++) {
            informationTextFieldPanel.add(informationTextField[i] = new JTextField(25));
            informationTextField[i].setFont(new Font("Tahoma", Font.PLAIN, 11));
        }
        lblShelfNo.setFont(new Font("Tahoma", Font.BOLD, 11));
        informationLabelPanel.add(lblShelfNo);
        txtShelfNo.setFont(new Font("Tahoma", Font.PLAIN, 11));
        informationTextFieldPanel.add(txtShelfNo);
        centerPanel.add("East", informationTextFieldPanel);
        insertInformationButtonPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
        insertInformationButton.setFont(new Font("Tahoma", Font.BOLD, 11));
        insertInformationButtonPanel.add(insertInformationButton);
        centerPanel.add("South", insertInformationButtonPanel);
        cp.add("Center", centerPanel);
        southPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
        OKButton.setFont(new Font("Tahoma", Font.BOLD, 11));
        southPanel.add(OKButton);
        southPanel.setBorder(BorderFactory.createEtchedBorder());
        cp.add("South", southPanel);
        insertInformationButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent ae) {
                if (isCorrect()) {
                    Thread runner = new Thread() {

                        public void run() {
                            book = new Books();
                            book.connection("SELECT BookID FROM Books WHERE ISBN = '" + data[7] + "'");
                            String ISBN = book.getISBN();
                            if (!data[7].equalsIgnoreCase(ISBN)) {
                                try {
                                    String sql = "INSERT INTO Books (Subject,Title,Author,Publisher,Copyright," + "Edition,Pages,ISBN,NumberOfBooks,NumberOfAvailbleBooks,Library,Availble,ShelfNo) VALUES " + " (?,?,?,?,?,?,?,?,?,?,?,?,?)";
                                    Class.forName("sun.jdbc.odbc.JdbcOdbcDriver");
                                    Connection con = DriverManager.getConnection("jdbc:odbc:JLibrary");
                                    PreparedStatement ps = con.prepareStatement(sql);
                                    ps.setString(1, data[0]);
                                    ps.setString(2, data[1]);
                                    ps.setString(3, data[2]);
                                    ps.setString(4, data[3]);
                                    ps.setInt(5, Integer.parseInt(data[4]));
                                    ps.setInt(6, Integer.parseInt(data[5]));
                                    ps.setInt(7, Integer.parseInt(data[6]));
                                    ps.setString(8, data[7]);
                                    ps.setInt(9, Integer.parseInt(data[8]));
                                    ps.setInt(10, Integer.parseInt(data[8]));
                                    ps.setString(11, data[9]);
                                    ps.setBoolean(12, availble);
                                    ps.setInt(13, Integer.parseInt(txtShelfNo.getText()));
                                    ps.executeUpdate();
                                } catch (Exception ex) {
                                    JOptionPane.showMessageDialog(null, ex.toString());
                                }
                                clearTextField();
                            } else {
                                JOptionPane.showMessageDialog(null, "The book is in the library", "Error", JOptionPane.ERROR_MESSAGE);
                            }
                        }
                    };
                    runner.start();
                } else {
                    JOptionPane.showMessageDialog(null, "Please, complete the information", "Warning", JOptionPane.WARNING_MESSAGE);
                }
            }
        });
        OKButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent ae) {
                dispose();
            }
        });
        setVisible(true);
        pack();
    }
}
