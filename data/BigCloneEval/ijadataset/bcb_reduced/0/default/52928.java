import java.awt.Color;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.Timer;
import javax.swing.border.Border;
import javax.swing.border.EtchedBorder;
import javax.swing.JOptionPane;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class ControlPanel extends JPanel {

    private ImagePanel imagePanel;

    private LevelScorePanel levelScorePanel;

    private ButtonPanel buttonPanel;

    private SnakeGame game;

    private Record record;

    private static String url = "jdbc:msql://localhost:3306/snakedb";

    private static String username = "root";

    private static String password = "12345678";

    private static String tablename = "snaketable";

    Connection conn;

    Statement stmt;

    public Connection getconnection() {
        try {
            Class.forName("com.mysql.jdbc.Driver");
            System.out.println("Class Found");
        } catch (java.lang.ClassNotFoundException e) {
            System.err.print("ClassNotFoundException: ");
            System.err.println(e.getMessage());
        }
        try {
            conn = DriverManager.getConnection(url, username, password);
            System.out.println("Connection established");
        } catch (SQLException ex) {
            System.err.println("SQLException: " + ex.getMessage());
        }
        return conn;
    }

    public void createTable() {
        Connection conn = getconnection();
        String createString;
        createString = "create table snaketable (" + "ID " + "Score" + "Level";
        try {
            stmt = conn.createStatement();
            stmt.executeUpdate(createString);
            stmt.close();
            conn.close();
        } catch (SQLException ex) {
            System.err.println("SQLException: " + ex.getMessage());
        }
        JOptionPane.showMessageDialog(null, "Snake Table Created");
    }

    ;

    public ControlPanel(SnakeGame game) {
        this.game = game;
        setLayout(new GridLayout(3, 1));
        imagePanel = new ImagePanel();
        levelScorePanel = new LevelScorePanel();
        buttonPanel = new ButtonPanel();
        add(imagePanel);
        add(levelScorePanel);
        add(buttonPanel);
        this.addKeyListener(new ControlKeyListener());
    }

    private class ImagePanel extends JPanel {

        private Border border = new EtchedBorder(EtchedBorder.RAISED, Color.white, Color.lightGray);

        private ImageIcon snakeImage = new ImageIcon("snake.jpg");

        public ImagePanel() {
            setBorder(border);
        }

        public void paintComponent(Graphics g) {
            super.paintComponent(g);
            snakeImage.paintIcon(this, g, 0, 0);
        }
    }

    private class LevelScorePanel extends JPanel {

        private JLabel scoreLabel = new JLabel("Score");

        private JLabel levelLable = new JLabel("Level");

        private Timer timer;

        private JTextField scoreTextField, levelTextField;

        private Border border = new EtchedBorder(EtchedBorder.RAISED, Color.white, Color.lightGray);

        public LevelScorePanel() {
            scoreTextField = new JTextField();
            levelTextField = new JTextField();
            scoreTextField.setEditable(false);
            levelTextField.setEditable(false);
            setLayout(new GridLayout(4, 1));
            add(scoreLabel);
            add(scoreTextField);
            add(levelLable);
            add(levelTextField);
            setBorder(border);
            timer = new Timer(200, new ActionListener() {

                public void actionPerformed(ActionEvent event) {
                    levelTextField.setText("" + game.getLevel());
                    scoreTextField.setText("" + game.getScore());
                }
            });
            timer.start();
        }
    }

    private class ButtonPanel extends JPanel {

        private JButton gameidrecordButton;

        private JButton playButton, stopButton, pauseOrResumeButton;

        private JButton turnEasierButton, turnHarderButton;

        private Border border = new EtchedBorder(EtchedBorder.RAISED, Color.white, Color.lightGray);

        private Timer timer;

        private String dbstring;

        private String gameid;

        public ButtonPanel() {
            playButton = new JButton("Start");
            stopButton = new JButton("Stop");
            pauseOrResumeButton = new JButton("Pause");
            turnEasierButton = new JButton("Easy");
            turnHarderButton = new JButton("Hard");
            gameidrecordButton = new JButton("Record");
            setLayout(new GridLayout(6, 1));
            add(playButton);
            add(stopButton);
            add(pauseOrResumeButton);
            add(turnEasierButton);
            add(turnHarderButton);
            add(gameidrecordButton);
            setBorder(border);
            playButton.addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent e) {
                    game.startGame();
                    playButton.setEnabled(false);
                    stopButton.setEnabled(true);
                    ControlPanel.this.requestFocus();
                }
            });
            stopButton.addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent e) {
                    game.stopGame();
                    stopButton.setEnabled(false);
                    ControlPanel.this.requestFocus();
                }
            });
            pauseOrResumeButton.addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent e) {
                    if (pauseOrResumeButton.getText().equals("Pause")) {
                        game.pauseGame();
                        pauseOrResumeButton.setText("Resume");
                    } else {
                        game.resumeGame();
                        pauseOrResumeButton.setText("Pause");
                    }
                    ControlPanel.this.requestFocus();
                }
            });
            turnEasierButton.addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent e) {
                    game.decLevel();
                    ControlPanel.this.requestFocus();
                }
            });
            turnHarderButton.addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent e) {
                    game.incLevel();
                    ControlPanel.this.requestFocus();
                }
            });
            gameidrecordButton.addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent e) {
                    game.pauseGame();
                    String gameid = JOptionPane.showInputDialog("Please type your game ID ");
                    String insertString1 = gameid + game.getScore() + game.getLevel();
                    try {
                        stmt = conn.createStatement();
                        stmt.executeUpdate(insertString1);
                        stmt.close();
                        conn.close();
                    } catch (SQLException ex) {
                        System.err.println("SQLException: " + ex.getMessage());
                    }
                }
            });
            timer = new Timer(200, new ActionListener() {

                public void actionPerformed(ActionEvent event) {
                    if (game.isGameOver()) {
                        playButton.setEnabled(true);
                    }
                }
            });
            timer.start();
        }
    }

    private class ControlKeyListener extends KeyAdapter {

        public void keyPressed(KeyEvent ke) {
            int currentDirection = game.getSnakeDirection();
            if (game.isPlaying()) {
                switch(ke.getKeyCode()) {
                    case KeyEvent.VK_DOWN:
                        if (currentDirection != Utils.UP) game.changeSnakeDirection(Utils.DOWN);
                        break;
                    case KeyEvent.VK_UP:
                        if (currentDirection != Utils.DOWN) game.changeSnakeDirection(Utils.UP);
                        break;
                    case KeyEvent.VK_LEFT:
                        if (currentDirection != Utils.RIGHT) game.changeSnakeDirection(Utils.LEFT);
                        break;
                    case KeyEvent.VK_RIGHT:
                        if (currentDirection != Utils.LEFT) game.changeSnakeDirection(Utils.RIGHT);
                        break;
                }
            }
        }
    }
}
