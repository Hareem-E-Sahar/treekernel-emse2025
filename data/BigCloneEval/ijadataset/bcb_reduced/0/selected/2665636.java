package com.intellij.puzzlers;

import com.intellij.ide.plugins.IdeaPluginDescriptor;
import com.intellij.ide.plugins.PluginManager;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.extensions.PluginId;
import com.intellij.openapi.fileChooser.FileChooserDialog;
import com.intellij.openapi.fileChooser.FileChooserFactory;
import com.intellij.openapi.fileChooser.FileSaverDescriptor;
import com.intellij.openapi.fileChooser.FileSaverDialog;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileWrapper;
import com.intellij.puzzlers.ui.JavaPuzzlersGame;
import com.intellij.puzzlers.ui.Login;
import com.intellij.puzzlers.ui.Results;
import org.tmatesoft.sqljet.core.SqlJetException;
import org.tmatesoft.sqljet.core.SqlJetTransactionMode;
import org.tmatesoft.sqljet.core.table.ISqlJetCursor;
import org.tmatesoft.sqljet.core.table.ISqlJetTable;
import org.tmatesoft.sqljet.core.table.SqlJetDb;
import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.Properties;

public class GameController implements Disposable {

    private JPanel mainPanel;

    private final String DB_NAME = "puzzlers.sql";

    private JavaPuzzlersGame javaPuzzlersGame;

    private Login login;

    private Results results;

    private SqlJetDb db;

    private boolean isAnswered(int number) throws SqlJetException {
        db.beginTransaction(SqlJetTransactionMode.READ_ONLY);
        try {
            ISqlJetTable table = db.getTable("answers");
            ISqlJetCursor cursor = table.lookup("login_question", login.getLogin(), number);
            return !cursor.eof();
        } finally {
            db.commit();
        }
    }

    private void checkButtonsDuringQuestion(int number) throws SqlJetException {
        if (isAnswered(number)) {
            javaPuzzlersGame.getAnswerButton().setEnabled(false);
            javaPuzzlersGame.getRunButton().setEnabled(true);
        } else {
            javaPuzzlersGame.getAnswerButton().setEnabled(true);
            javaPuzzlersGame.getRunButton().setEnabled(false);
        }
    }

    public GameController(Project project) {
        if (project == null) {
            throw new RuntimeException("Project is null!");
        }
        javaPuzzlersGame.setProject(project);
        results.getMainPanel().setVisible(false);
        javaPuzzlersGame.getMainPanel().setVisible(false);
        results.getOkButton().addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent actionEvent) {
                javaPuzzlersGame.getMainPanel().setVisible(true);
                results.getMainPanel().setVisible(false);
            }
        });
        login.addOKActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                try {
                    if (!tryLogin(login.getLogin(), login.getPasswordHash())) {
                        System.err.print(":(");
                    } else {
                        mainPanel.remove(login.getMainPanel());
                        javaPuzzlersGame.getMainPanel().setVisible(true);
                        javaPuzzlersGame.setPuzzlerNumber(1);
                        javaPuzzlersGame.addPuzzler(1);
                        javaPuzzlersGame.setCurrentLanguage(login.getLanguage());
                        checkButtonsDuringQuestion(1);
                    }
                } catch (SqlJetException e1) {
                    e1.printStackTrace(System.err);
                }
            }
        });
        login.addRegistrationActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                try {
                    register(login.getLogin(), login.getPasswordHash());
                } catch (SqlJetException e1) {
                    e1.printStackTrace(System.err);
                }
            }
        });
        javaPuzzlersGame.getNextPuzzlerButton().addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                try {
                    if (javaPuzzlersGame.getPuzzlerNumber() < javaPuzzlersGame.maxQuestion) javaPuzzlersGame.setPuzzlerNumber(javaPuzzlersGame.getPuzzlerNumber() + 1); else javaPuzzlersGame.setPuzzlerNumber(1);
                    javaPuzzlersGame.addPuzzler(javaPuzzlersGame.getPuzzlerNumber());
                    checkButtonsDuringQuestion(javaPuzzlersGame.getPuzzlerNumber());
                } catch (SqlJetException e1) {
                    e1.printStackTrace();
                }
            }
        });
        javaPuzzlersGame.getResultButton().addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent actionEvent) {
                javaPuzzlersGame.getMainPanel().setVisible(false);
                results.getMainPanel().setVisible(true);
                try {
                    printResults();
                } catch (SqlJetException e) {
                    e.printStackTrace();
                }
            }
        });
        javaPuzzlersGame.getPreviousPuzzlerButton().addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                try {
                    if (javaPuzzlersGame.getPuzzlerNumber() > 1) javaPuzzlersGame.setPuzzlerNumber(javaPuzzlersGame.getPuzzlerNumber() - 1); else javaPuzzlersGame.setPuzzlerNumber(javaPuzzlersGame.maxQuestion);
                    javaPuzzlersGame.addPuzzler(javaPuzzlersGame.getPuzzlerNumber());
                    checkButtonsDuringQuestion(javaPuzzlersGame.getPuzzlerNumber());
                } catch (SqlJetException e1) {
                    e1.printStackTrace();
                }
            }
        });
        javaPuzzlersGame.getAnswerButton().addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                int correct = 0;
                if (javaPuzzlersGame.checkAnswer()) {
                    correct = 1;
                }
                try {
                    answer(javaPuzzlersGame.getPuzzlerNumber(), correct);
                } catch (SqlJetException e1) {
                    e1.printStackTrace(System.err);
                }
            }
        });
        results.getSendButton().addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent actionEvent) {
                JFrame frame = new JFrame();
                Object email = JOptionPane.showInputDialog(frame, "Enter email: ");
                sendResults(email.toString());
            }
        });
        results.getExportButton().addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                final FileSaverDescriptor descriptor = new FileSaverDescriptor("Save results to", "");
                final FileSaverDialog dialog = FileChooserFactory.getInstance().createSaveFileDialog(descriptor, javaPuzzlersGame.getProject());
                VirtualFile base = javaPuzzlersGame.getProject().getBaseDir();
                final VirtualFileWrapper fileWrapper = dialog.save(base, "results.html");
                if (fileWrapper != null) {
                    ApplicationManager.getApplication().invokeLater(new Runnable() {

                        public void run() {
                            final VirtualFile fileToSave = fileWrapper.getVirtualFile(true);
                            assert fileToSave != null;
                            try {
                                fileToSave.setBinaryContent(results.exportResultsToXML(login.getLogin()).getBytes());
                            } catch (IOException e) {
                                e.printStackTrace(System.err);
                            }
                        }
                    });
                }
            }
        });
        results.getXsltButton().addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent actionEvent) {
                final FileSaverDescriptor descriptor = new FileSaverDescriptor("Choose xsl file", "xsl");
                final FileChooserDialog dialog = FileChooserFactory.getInstance().createFileChooser(descriptor, javaPuzzlersGame.getProject());
                VirtualFile[] xsl = dialog.choose(null, javaPuzzlersGame.getProject());
                results.transformXML(javaPuzzlersGame.getProject(), login.getLogin(), xsl[0]);
            }
        });
        try {
            setupDB();
        } catch (SqlJetException e) {
            e.printStackTrace(System.err);
        } catch (IOException e) {
            e.printStackTrace(System.err);
        }
    }

    private String printShortSummary() throws SqlJetException {
        db.beginTransaction(SqlJetTransactionMode.WRITE);
        ISqlJetTable table = db.getTable("answers");
        final ISqlJetCursor cursor = table.lookup("login", login.getLogin());
        int answered = (int) cursor.getRowCount();
        int right = 0;
        if (!cursor.eof()) {
            do {
                if (Integer.parseInt(cursor.getString("answer")) == 1) ++right;
            } while (cursor.next());
        }
        cursor.close();
        db.commit();
        StringBuilder result = new StringBuilder();
        result.append("Results of " + login.getLogin() + "\n");
        result.append("Number of questions: " + javaPuzzlersGame.maxQuestion + "\n");
        result.append("Number of answers: " + answered + "\n");
        result.append("Number of right answers: " + right + "\n");
        return result.toString();
    }

    private void sendResults(String s) {
        Properties props = new Properties();
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.socketFactory.port", "465");
        props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.port", "465");
        Session session = Session.getDefaultInstance(props, new javax.mail.Authenticator() {

            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication("fofanova.mn", "boor2vaY");
            }
        });
        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress("fofanova.mn@gmail.com"));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(s));
            message.setSubject("JavaPuzzlers results");
            message.setText(printShortSummary());
            Transport.send(message);
            System.out.println("Done");
        } catch (MessagingException e) {
            throw new RuntimeException(e);
        } catch (SqlJetException e) {
            e.printStackTrace();
        }
    }

    public JPanel getMainPanel() {
        return mainPanel;
    }

    public void answer(int question, int ansNum) throws SqlJetException {
        db.beginTransaction(SqlJetTransactionMode.WRITE);
        try {
            ISqlJetTable table = db.getTable("answers");
            table.insert(login.getLogin(), question, ansNum);
        } finally {
            db.commit();
        }
    }

    private void register(String login, String password) throws SqlJetException {
        db.beginTransaction(SqlJetTransactionMode.WRITE);
        try {
            ISqlJetTable table = db.getTable("users");
            table.insert(login, password);
        } finally {
            db.commit();
        }
    }

    private boolean tryLogin(String login, String password) throws SqlJetException {
        db.beginTransaction(SqlJetTransactionMode.WRITE);
        try {
            ISqlJetTable table = db.getTable("users");
            ISqlJetCursor cursor = table.lookup("login_password", login, password);
            return !cursor.eof();
        } finally {
            db.commit();
        }
    }

    private File getDBFile() {
        IdeaPluginDescriptor descriptor = PluginManager.getPlugin(PluginId.getId("com.jetbrains.puzzlers"));
        String path = descriptor.getPath().getAbsolutePath() + File.separator;
        return new File(path + "classes" + File.separator + DB_NAME);
    }

    private void setupDB() throws SqlJetException, IOException {
        File dbFile = getDBFile();
        boolean needCreateTables = !dbFile.exists();
        db = SqlJetDb.open(dbFile, true);
        if (needCreateTables) {
            db.getOptions().setAutovacuum(true);
            db.beginTransaction(SqlJetTransactionMode.WRITE);
            try {
                db.getOptions().setUserVersion(1);
            } finally {
                db.commit();
            }
            createTables();
        }
    }

    private void printResults() throws SqlJetException {
        db.beginTransaction(SqlJetTransactionMode.WRITE);
        try {
            ISqlJetTable table = db.getTable("answers");
            final ISqlJetCursor cursor = table.lookup("login", login.getLogin());
            TableModel dataModel = new AbstractTableModel() {

                String[][] mas = new String[(int) cursor.getRowCount()][2];

                {
                    makeMas();
                }

                public int getRowCount() {
                    try {
                        return (int) cursor.getRowCount();
                    } catch (SqlJetException e) {
                        e.printStackTrace();
                    }
                    return 0;
                }

                public int getColumnCount() {
                    return 2;
                }

                public String getColumnName(int i) {
                    return i == 0 ? "Number of question" : "Result";
                }

                public void makeMas() throws SqlJetException {
                    int i = 0;
                    if (!cursor.eof()) {
                        do {
                            mas[i][0] = cursor.getString("question");
                            mas[i][1] = cursor.getString("answer").equals("1") ? "Correct" : "Incorrect";
                            i++;
                        } while (cursor.next());
                    }
                }

                public Object getValueAt(int row, int col) {
                    return mas[row][col];
                }
            };
            results.getResultTable().setModel(dataModel);
            results.getResultsHeader().getColumnModel().addColumn(new TableColumn());
            results.getResultsHeader().getColumnModel().addColumn(new TableColumn());
            results.getResultsHeader().getColumnModel().getColumn(0).setHeaderValue("Number of question");
            results.getResultsHeader().getColumnModel().getColumn(1).setHeaderValue("Your answer is");
            results.getResultsHeader().getColumnModel().getColumn(0).setWidth(300);
            results.getResultsHeader().getColumnModel().getColumn(1).setWidth(300);
            cursor.close();
        } finally {
            db.commit();
        }
    }

    private void createTables() throws SqlJetException {
        final String createUserTableQuery = "CREATE TABLE users (login TEXT NOT NULL PRIMARY KEY , password TEXT NOT NULL)";
        final String loginPasswordIndexQuery = "CREATE INDEX login_password ON users(login,password)";
        final String createAnswersTableQuery = "CREATE TABLE answers (login TEXT NOT NULL, question INTEGER, answer INTEGER, PRIMARY KEY(login, question), FOREIGN KEY(login) REFERENCES users(login))";
        final String loginQuestionIndexQuery = "CREATE INDEX login_question ON answers(login,question)";
        final String loginAnswersIndexQuery = "CREATE INDEX login ON answers(login)";
        db.beginTransaction(SqlJetTransactionMode.WRITE);
        try {
            db.createTable(createUserTableQuery);
            db.createIndex(loginPasswordIndexQuery);
            db.createTable(createAnswersTableQuery);
            db.createIndex(loginQuestionIndexQuery);
            db.createIndex(loginAnswersIndexQuery);
        } finally {
            db.commit();
        }
    }

    public void dispose() {
        try {
            db.close();
        } catch (SqlJetException e) {
            e.printStackTrace(System.err);
        }
    }
}
