package org.exist.requestlog;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.net.Socket;

/** Webapplication Descriptor
 * 
 * Request Replayer Simple Swing GUI Application
 * Opens request replay log's as generated by eXist's web-application Descriptor when enabled
 * and can send the request's back to eXist.
 * Useful for load testing and checking memory leaks.
 * 
 * @author Adam Retter <adam.retter@devon.gov.uk>
 * @serial 2006-02-28
 * @version 1.6
 */
public class RequestReplayer extends JFrame {

    private static final long serialVersionUID = 1L;

    /** Handle to the Request Log File */
    private File requestLogFile = null;

    private JTextField txtReplayFilename = null;

    private JLabel lblRequestCount = null;

    private JTextField txtIterations = null;

    private JTextField txtAlternateHost = null;

    @SuppressWarnings("unused")
    private JLabel lblWaitTimeRequest = null;

    private JTextField txtWaitTimeRequest = null;

    private JButton btnStart = null;

    private JTextArea txtStatus = null;

    /**
	 * Entry point of the program
	 * 
	 * @param args		array of parameters passed in from where the program is executed
	*/
    public static void main(String[] args) {
        String fileName = null;
        if (args.length > 0) fileName = args[0];
        new RequestReplayer(fileName);
    }

    /**
	 * Default Constructor
	 * @param fileName 
	 */
    public RequestReplayer(String fileName) {
        if (fileName != null) requestLogFile = new File(fileName);
        initialize();
    }

    /**
	 * JDialog Window Event Handler
	 * 
	 * @param e		The event
	 */
    protected void processWindowEvent(WindowEvent e) {
        if (e.getID() == WindowEvent.WINDOW_CLOSING) {
            this.setVisible(false);
            this.dispose();
            System.exit(0);
        }
    }

    /**
	 * Initalise Dialog
	 */
    private void initialize() {
        setupGUI();
        this.setSize(600, 430);
        this.setVisible(true);
    }

    /**
	 *Setup the Dialog's GUI 
	 */
    private void setupGUI() {
        this.setTitle("eXist Request Replayer");
        JPanel cnt = new JPanel();
        GridBagLayout grid = new GridBagLayout();
        cnt.setLayout(grid);
        this.setContentPane(cnt);
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(2, 2, 2, 2);
        JPanel panelFile = new JPanel();
        panelFile.setBorder(new TitledBorder("Request Log"));
        GridBagLayout panelFileGrid = new GridBagLayout();
        panelFile.setLayout(panelFileGrid);
        JLabel lblLogFile = new JLabel("Filename:");
        c.gridx = 0;
        c.gridy = 0;
        c.gridwidth = 1;
        c.anchor = GridBagConstraints.WEST;
        c.fill = GridBagConstraints.NONE;
        panelFileGrid.setConstraints(lblLogFile, c);
        panelFile.add(lblLogFile);
        String fileNameInfield = "/usr/local/eXist/request-replay-log.txt";
        if (requestLogFile != null) fileNameInfield = requestLogFile.getAbsolutePath();
        txtReplayFilename = new JTextField(fileNameInfield, 24);
        txtReplayFilename.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                if (!txtReplayFilename.getText().equals(requestLogFile.getPath())) {
                    chooseFile();
                }
            }
        });
        c.gridx = 1;
        c.gridy = 0;
        c.anchor = GridBagConstraints.WEST;
        c.fill = GridBagConstraints.NONE;
        panelFileGrid.setConstraints(txtReplayFilename, c);
        panelFile.add(txtReplayFilename);
        JButton btnChooseFile = new JButton("Choose...");
        btnChooseFile.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                chooseFile();
            }
        });
        c.gridx = 2;
        c.gridy = 0;
        c.anchor = GridBagConstraints.WEST;
        c.fill = GridBagConstraints.NONE;
        panelFileGrid.setConstraints(btnChooseFile, c);
        panelFile.add(btnChooseFile);
        JLabel lblRequestCountText = new JLabel("Request Count: ");
        c.gridx = 0;
        c.gridy = 2;
        c.anchor = GridBagConstraints.WEST;
        c.fill = GridBagConstraints.NONE;
        panelFileGrid.setConstraints(lblRequestCountText, c);
        panelFile.add(lblRequestCountText);
        lblRequestCount = new JLabel(new Integer(countRequestRecords()).toString());
        c.gridx = 1;
        c.gridy = 2;
        c.anchor = GridBagConstraints.WEST;
        c.fill = GridBagConstraints.NONE;
        panelFileGrid.setConstraints(lblRequestCount, c);
        panelFile.add(lblRequestCount);
        c.gridx = 0;
        c.gridy = 0;
        c.anchor = GridBagConstraints.WEST;
        c.fill = GridBagConstraints.NONE;
        grid.setConstraints(panelFile, c);
        cnt.add(panelFile);
        JPanel panelReplay = new JPanel();
        panelReplay.setBorder(new TitledBorder("Replay"));
        GridBagLayout panelReplayGrid = new GridBagLayout();
        panelReplay.setLayout(panelReplayGrid);
        JLabel lblIterations = new JLabel("Iterations:");
        c.gridx = 0;
        c.gridy = 0;
        c.anchor = GridBagConstraints.WEST;
        c.fill = GridBagConstraints.NONE;
        panelReplayGrid.setConstraints(lblIterations, c);
        panelReplay.add(lblIterations);
        txtIterations = new JTextField("1", 5);
        c.gridx = 1;
        c.gridy = 0;
        c.anchor = GridBagConstraints.WEST;
        c.fill = GridBagConstraints.NONE;
        panelReplayGrid.setConstraints(txtIterations, c);
        panelReplay.add(txtIterations);
        JLabel lblAlternateHost = new JLabel("Alternate Host:");
        c.gridx = 0;
        c.gridy = 1;
        c.anchor = GridBagConstraints.WEST;
        c.fill = GridBagConstraints.NONE;
        panelReplayGrid.setConstraints(lblAlternateHost, c);
        panelReplay.add(lblAlternateHost);
        txtAlternateHost = new JTextField(24);
        c.gridx = 1;
        c.gridy = 1;
        c.anchor = GridBagConstraints.WEST;
        c.fill = GridBagConstraints.NONE;
        panelReplayGrid.setConstraints(txtAlternateHost, c);
        panelReplay.add(txtAlternateHost);
        JLabel lblWaitTimeRequest = new JLabel("Delay between requests:");
        c.gridx = 0;
        c.gridy = 2;
        c.anchor = GridBagConstraints.WEST;
        c.fill = GridBagConstraints.NONE;
        panelReplayGrid.setConstraints(lblWaitTimeRequest, c);
        panelReplay.add(lblWaitTimeRequest);
        txtWaitTimeRequest = new JTextField("200", 6);
        c.gridx = 1;
        c.gridy = 2;
        c.anchor = GridBagConstraints.WEST;
        c.fill = GridBagConstraints.NONE;
        panelReplayGrid.setConstraints(txtWaitTimeRequest, c);
        panelReplay.add(txtWaitTimeRequest);
        btnStart = new JButton("Start");
        btnStart.setMnemonic('S');
        btnStart.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                new Thread() {

                    public void run() {
                        doReplay();
                    }
                }.start();
                btnStart.setEnabled(false);
            }
        });
        c.gridx = GridBagConstraints.CENTER;
        c.gridy = 2;
        c.anchor = GridBagConstraints.CENTER;
        c.fill = GridBagConstraints.HORIZONTAL;
        panelReplayGrid.setConstraints(btnStart, c);
        panelReplay.add(btnStart);
        c.gridx = 0;
        c.gridy = 1;
        c.anchor = GridBagConstraints.WEST;
        c.fill = GridBagConstraints.NONE;
        grid.setConstraints(panelReplay, c);
        cnt.add(panelReplay);
        JPanel panelStatus = new JPanel();
        panelStatus.setBorder(new TitledBorder("Status"));
        GridBagLayout panelStatusGrid = new GridBagLayout();
        panelStatus.setLayout(panelStatusGrid);
        txtStatus = new JTextArea(10, 40);
        txtStatus.setEditable(false);
        JScrollPane scrollStatus = new JScrollPane(txtStatus, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        c.gridx = 0;
        c.gridy = 0;
        c.anchor = GridBagConstraints.CENTER;
        c.fill = GridBagConstraints.NONE;
        panelStatusGrid.setConstraints(scrollStatus, c);
        panelStatus.add(scrollStatus);
        c.gridx = 0;
        c.gridy = 2;
        c.anchor = GridBagConstraints.WEST;
        c.fill = GridBagConstraints.NONE;
        grid.setConstraints(panelStatus, c);
        cnt.add(panelStatus);
    }

    /**
	 * Counts the number of request records in the request replay log file
	 * 
	 *  @return		The number of request records in the request replay log file
	 */
    private int countRequestRecords() {
        int count = 0;
        if (requestLogFile == null) {
            try {
                requestLogFile = new File(txtReplayFilename.getText());
            } catch (NullPointerException npe) {
                System.err.println("Invalid path for Request file");
                return (0);
            }
        }
        try {
            BufferedReader bufRead = new BufferedReader(new FileReader(requestLogFile));
            String line = bufRead.readLine();
            while (line != null) {
                if (line.indexOf("Date:") > -1) {
                    count++;
                }
                line = bufRead.readLine();
            }
            bufRead.close();
        } catch (FileNotFoundException fnfe) {
            System.err.println("Request file not found");
            return (0);
        } catch (IOException ioe) {
            System.err.println("An I/O Exception occured whilst reading the Request file");
            return (count);
        }
        return (count);
    }

    /**
	 * Event for when the "Choose..." button is clicked, displays a simple
	 * file chooser dialog 
	 * @param  
	 */
    private void chooseFile() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setApproveButtonText("Open");
        fileChooser.setApproveButtonMnemonic('O');
        if (requestLogFile != null) {
            fileChooser.setCurrentDirectory(requestLogFile.getParentFile());
            fileChooser.ensureFileIsVisible(requestLogFile);
        }
        int retval = fileChooser.showDialog(this, null);
        if (retval == JFileChooser.APPROVE_OPTION) {
            requestLogFile = fileChooser.getSelectedFile();
            txtReplayFilename.setText(requestLogFile.getPath());
            lblRequestCount.setText(new Integer(countRequestRecords()).toString());
        }
    }

    /**
	 * Function that takes each request from the log file
	 * and sends it back to the server 
	 */
    private void doReplay() {
        RandomAccessFile raFile = null;
        String line = null;
        long offset = 0;
        txtStatus.setText("");
        txtStatus.setCaretPosition(txtStatus.getDocument().getLength());
        int iterations = new Integer(txtIterations.getText()).intValue();
        long WaitTime = new Long(txtWaitTimeRequest.getText()).longValue();
        for (int i = 0; i < iterations; i++) {
            try {
                raFile = new RandomAccessFile(requestLogFile, "r");
                while ((line = raFile.readLine()) != null) {
                    if (line.indexOf("Date:") > -1) {
                        StringBuffer bufRequest = new StringBuffer();
                        String server = null;
                        int port = 80;
                        txtStatus.append("Iteration: " + (i + 1) + ", Sending Request from " + line + System.getProperty("line.separator"));
                        txtStatus.setCaretPosition(txtStatus.getDocument().getLength());
                        while ((line = raFile.readLine()) != null) {
                            if (line.length() != 0) {
                                bufRequest.append(line + System.getProperty("line.separator"));
                                if (line.indexOf("Host:") > -1) {
                                    if (txtAlternateHost.getText().length() == 0) {
                                        String host = line.substring(new String("Host: ").length());
                                        server = host.substring(0, host.indexOf(":"));
                                        port = new Integer(host.substring(host.indexOf(":") + 1)).intValue();
                                    } else {
                                        server = txtAlternateHost.getText().substring(0, txtAlternateHost.getText().indexOf(":"));
                                        port = new Integer(txtAlternateHost.getText().substring(txtAlternateHost.getText().indexOf(":") + 1)).intValue();
                                    }
                                }
                            } else {
                                offset = raFile.getFilePointer();
                                if (raFile.readLine().length() == 0 || raFile.length() == offset) {
                                    try {
                                        Socket socReq = new Socket(server, port);
                                        OutputStream socReqOut = socReq.getOutputStream();
                                        DataOutputStream os = new DataOutputStream(socReqOut);
                                        DataInputStream is = new DataInputStream(socReq.getInputStream());
                                        os.writeBytes(bufRequest.toString());
                                        os.flush();
                                        try {
                                            String nextLine;
                                            int len = -1;
                                            while ((nextLine = is.readLine()) != null && nextLine.length() > 0) {
                                                if (nextLine.startsWith("Content-Length:")) len = Integer.parseInt(nextLine.substring(16));
                                                System.out.println(nextLine);
                                            }
                                            System.out.println();
                                            byte[] buf = new byte[512];
                                            int byteCount = len;
                                            while (byteCount > 0) {
                                                int b;
                                                if (byteCount < 512) b = is.read(buf, 0, byteCount); else b = is.read(buf, 0, 512);
                                                if (b == -1) break;
                                                System.out.write(buf, 0, b);
                                                byteCount -= b;
                                            }
                                        } catch (IOException e) {
                                        }
                                        System.out.println();
                                        is.close();
                                        os.close();
                                        socReqOut.close();
                                        socReq.close();
                                    } catch (IOException ioe) {
                                        System.err.println("An I/O Exception occured whilst writting a Request to the server");
                                        System.err.println(ioe.getMessage());
                                    }
                                    synchronized (this) {
                                        try {
                                            wait(WaitTime);
                                        } catch (InterruptedException e) {
                                        }
                                    }
                                    break;
                                } else {
                                    raFile.seek(offset);
                                    bufRequest.append(System.getProperty("line.separator"));
                                }
                            }
                        }
                    }
                }
                raFile.close();
            } catch (IOException ioe) {
                System.err.println("An I/O Exception occured whilst reading the Request file");
                btnStart.setEnabled(true);
                return;
            }
        }
        btnStart.setEnabled(true);
    }
}
