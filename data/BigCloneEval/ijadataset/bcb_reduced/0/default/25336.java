import java.io.*;
import java.lang.ClassNotFoundException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.math.BigInteger;
import java.util.Scanner;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.*;
import java.awt.*;
import java.lang.Runnable;
import java.lang.Thread;

public class CAEMURA {

    public static CAEMURA_GUI mBorrowedResource;

    static int debug = 0;

    static String host_ip;

    static RandomPrime prime = new RandomPrime();

    static String message;

    static byte recvmsg[];

    static String recvmsg2;

    static String sendmsg = "";

    static byte sendmsg2[];

    static int length = 0;

    static AESCrypt A = new AESCrypt(debug);

    static USER userA;

    static BigInteger user_A_private;

    static BigInteger YB;

    static BigInteger UserAKey = new BigInteger("0");

    static BufferedReader input = new BufferedReader(new InputStreamReader(System.in));

    static ObjectInputStream ois;

    static ObjectOutputStream oos;

    public static void getIP() {
        host_ip = JOptionPane.showInputDialog("What IP are you connecting to (leave blank for localhost)?");
        if (host_ip == "") host_ip = "127.0.0.1";
    }

    public static void connect() {
        try {
            Socket socket = new Socket(host_ip, 7777);
            mBorrowedResource.mainConsole.append("Unsecure connection established with server. \n\nPerforming Diffie-Hellman Key Exchange...\n\n");
            oos = new ObjectOutputStream(socket.getOutputStream());
            ois = new ObjectInputStream(socket.getInputStream());
            generateKey();
            mBorrowedResource.setLocked();
            Thread listeningProcedure = new Thread(new Runnable() {

                public void run() {
                    while (true) {
                        CAEMURA.recvMessage();
                    }
                }
            });
            listeningProcedure.start();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void generateKey() {
        try {
            while (length != 16) {
                BigInteger q = prime.getNum(80);
                BigInteger a = prime.getNum(3);
                user_A_private = prime.getNum(50);
                userA = new USER(q, a, user_A_private);
                oos.writeObject(q.toString());
                oos.writeObject(a.toString());
                message = (String) ois.readObject();
                if (debug == 1) mBorrowedResource.mainConsole.append("YB: " + message);
                YB = new BigInteger(message);
                userA.OtherUserPublic(YB);
                oos.writeObject(userA.getPublic().toString());
                UserAKey = userA.CalculateKey(YB);
                if (debug == 1) mBorrowedResource.mainConsole.append("Key:" + UserAKey.toString());
                mBorrowedResource.mainConsole.append("Connection Secured!\n");
                mBorrowedResource.mainConsole.append("Entering chat mode. ");
                mBorrowedResource.mainConsole.append("All messages will be encrypted with Rijndael before transmission.\n\n\n");
                UserAKey = new BigInteger(UserAKey.toString().substring(0, 16));
                byte[] KeyInBytes = UserAKey.toString().getBytes();
                length = KeyInBytes.length;
                if (length != 16) mBorrowedResource.mainConsole.append("Key length incorrect. Regenerating...");
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static void sendMessage() {
        try {
            mBorrowedResource.mainConsole.append("Me: ");
            sendmsg = mBorrowedResource.inputMessage.getText();
            mBorrowedResource.mainConsole.append(sendmsg);
            while (sendmsg.length() % length > 0) sendmsg += " ";
            sendmsg2 = A.encrypt(UserAKey.toString(), sendmsg, length);
            if (debug == 1) mBorrowedResource.mainConsole.append(A.decrypt(UserAKey.toString(), sendmsg2, length));
            while (!A.decrypt(UserAKey.toString(), sendmsg2, length).substring(0, 1).equals(sendmsg.substring(0, 1))) {
                mBorrowedResource.mainConsole.append("\nError in decryption. Please choose a different message.");
                sendmsg = mBorrowedResource.inputMessage.getText();
                while (sendmsg.length() % length > 0) sendmsg += " ";
                sendmsg2 = A.encrypt(UserAKey.toString(), sendmsg, length);
            }
            mBorrowedResource.inputMessage.setText("");
            oos.writeObject(sendmsg2);
            if (sendmsg.substring(0, 5).equals("/quit")) disconnect();
            mBorrowedResource.mainConsole.append("\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void recvMessage() {
        try {
            recvmsg = (byte[]) ois.readObject();
            recvmsg2 = A.decrypt(UserAKey.toString(), recvmsg, length);
            if (recvmsg2.length() >= 5 && recvmsg2.substring(0, 5).equals("/quit")) disconnect();
            mBorrowedResource.mainConsole.append("Them: ");
            mBorrowedResource.mainConsole.append(recvmsg2 + "\n");
        } catch (IOException e) {
            disconnect();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static void disconnect() {
        try {
            ois.close();
            oos.close();
            System.exit(0);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        mBorrowedResource = new CAEMURA_GUI();
        mBorrowedResource.setVisible(true);
    }
}

class CAEMURA_GUI extends JFrame {

    public final JTextArea mainConsole = new JTextArea("");

    public final JButton connect = new JButton("Connect");

    public final JButton disconnect = new JButton("Disconnect");

    public final JButton sendMessage = new JButton("Send Message");

    public final JTextField inputMessage = new JTextField("");

    public final ImageIcon unlock = new ImageIcon("../img/unlock.png", "Not Connected or Not Encrypted!");

    public final ImageIcon lock = new ImageIcon("../img/lock.png", "Connected and Encrypted!");

    public final JLabel labelUnlock = new JLabel();

    public final JLabel labelLock = new JLabel();

    public JPanel panel;

    public final JButton openFileManager = new JButton("Open File Manager");

    public final JFileChooser fileManager = new JFileChooser();

    public void setLocked() {
        panel.add(labelLock);
        panel.remove(labelUnlock);
        panel.repaint();
    }

    CAEMURA_GUI() {
        panel = new JPanel();
        getContentPane().add(panel);
        panel.setLayout(null);
        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("CAEMURA");
        setSize(800, 670);
        JLabel labelOne = new JLabel("Client Application for Encrypted Messaging Using Rijndael Algorithm\n(CAEMURA) v0.9.1 June 26, 2009\n\n");
        labelOne.setBounds(10, 1, 800, 100);
        labelUnlock.setIcon(unlock);
        labelUnlock.setBounds(730, 570, 39, 57);
        labelLock.setIcon(lock);
        labelLock.setBounds(730, 570, 39, 57);
        mainConsole.setBounds(10, 60, 780, 500);
        mainConsole.setEditable(false);
        mainConsole.setLineWrap(true);
        mainConsole.setWrapStyleWord(true);
        JScrollPane scrollPane = new JScrollPane(mainConsole);
        scrollPane.setBounds(10, 60, 780, 500);
        scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        connect.setBounds(10, 10, 150, 30);
        connect.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent event) {
                CAEMURA.getIP();
                CAEMURA.connect();
            }
        });
        disconnect.setBounds(640, 10, 150, 30);
        disconnect.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent event) {
                CAEMURA.disconnect();
            }
        });
        sendMessage.setBounds(10, 600, 150, 30);
        sendMessage.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent event) {
                CAEMURA.sendMessage();
            }
        });
        inputMessage.setBounds(10, 570, 700, 30);
        inputMessage.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent event) {
                CAEMURA.sendMessage();
            }
        });
        openFileManager.setBounds(170, 10, 150, 30);
        openFileManager.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                int returnVal = fileManager.showOpenDialog(CAEMURA_GUI.this);
                if (e.getSource() == openFileManager) {
                    if (returnVal == JFileChooser.APPROVE_OPTION) {
                        File file = fileManager.getSelectedFile();
                        CAEMURA.mBorrowedResource.mainConsole.append("Opening: " + file.getName() + ".\n");
                    } else {
                        CAEMURA.mBorrowedResource.mainConsole.append("Open command cancelled by user.\n");
                    }
                }
            }
        });
        panel.add(labelUnlock);
        panel.add(labelOne);
        panel.add(sendMessage);
        panel.add(inputMessage);
        panel.add(disconnect);
        panel.add(connect);
        panel.add(scrollPane);
        panel.add(openFileManager);
    }
}
