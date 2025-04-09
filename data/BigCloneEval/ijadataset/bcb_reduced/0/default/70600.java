import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Line2D;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.border.*;
import java.util.Vector;
import java.util.Enumeration;
import java.io.*;
import javax.sound.sampled.*;
import java.awt.font.*;
import java.text.*;
import java.awt.Image.*;
import javax.imageio.*;
import java.awt.Toolkit;
import java.io.*;
import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JSplitPane;
import java.awt.geom.Line2D;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.net.URL;
import java.net.MalformedURLException;
import javax.sound.sampled.*;
import javax.sound.midi.*;
import java.lang.Object.*;

class Oldfb1 extends JPanel implements ActionListener, Runnable {

    JButton[] buttons;

    int[] drive_index;

    int last_drive_index;

    String[] drives;

    int last_button_index;

    Font font;

    Font font1;

    JButton back_button;

    JButton pdf_button;

    JButton photo_button;

    JButton movie_button;

    JButton doc_button;

    JButton all_button;

    JButton memo_button;

    JButton dir_button;

    JButton record_button;

    JButton last_clicked_file;

    String current_memo;

    String parent_dir;

    String current_dir;

    JFrame frame;

    JScrollPane pane;

    Thread thread;

    Thread memo_thread;

    int type;

    AudioInputStream audioInputStream;

    boolean lineOpen;

    JButton current_selected;

    int filetype;

    boolean showdir;

    Oldfb1 child;

    Oldfb1 sibling;

    static final int TYPE_ALL = 0;

    static final int TYPE_PDF = 1;

    static final int TYPE_DOC = 2;

    static final int TYPE_PHOTO = 3;

    static final int TYPE_MOVIE = 4;

    public Oldfb1(int mytype) {
        font = new Font("Georgia", Font.PLAIN, 14);
        font1 = new Font("Georgia", Font.BOLD, 18);
        showdir = true;
        buttons = new JButton[1024];
        drives = new String[20];
        drive_index = new int[1024];
        for (int i = 0; i < 1024; i++) {
            buttons[i] = new JButton();
            buttons[i].setFont(font);
        }
        type = mytype;
        current_dir = new String("C://");
        parent_dir = new String("C://");
    }

    /**
     * Create the GUI and show it.  For thread safety,
     * this method should be invoked from the
     * event-dispatching thread.
     */
    public void setScrollPane(JScrollPane mypane) {
        pane = mypane;
    }

    public void setChild(Oldfb1 oldfb1) {
        child = oldfb1;
    }

    public void makeicons() {
        setBackground(Color.white);
        setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        try {
            c.anchor = GridBagConstraints.FIRST_LINE_START;
            ImageIcon cup5 = new ImageIcon(new URL("http://veerex.googlecode.com/svn/trunk/src/back_icon.jpg"));
            back_button = new JButton(cup5);
            back_button.addActionListener(this);
            back_button.setBackground(Color.white);
            add(back_button, c);
            ImageIcon cup9 = new ImageIcon(new URL("http://veerex.googlecode.com/svn/trunk/src/dir_big_icon.jpg"));
            dir_button = new JButton(cup9);
            dir_button.addActionListener(this);
            dir_button.setBackground(Color.lightGray);
            dir_button.setText("On");
            add(dir_button, c);
            ImageIcon cup = new ImageIcon(new URL("http://veerex.googlecode.com/files/pdf_icon.jpg"));
            pdf_button = new JButton(cup);
            pdf_button.addActionListener(this);
            pdf_button.setBackground(Color.white);
            add(pdf_button);
            ImageIcon cup2 = new ImageIcon(new URL("http://veerex.googlecode.com/files/docs_icon.jpg"));
            doc_button = new JButton(cup2);
            doc_button.addActionListener(this);
            doc_button.setBackground(Color.white);
            add(doc_button);
            ImageIcon cup1 = new ImageIcon(new URL("http://veerex.googlecode.com/svn/trunk/src/photo_icon.jpg"));
            photo_button = new JButton(cup1);
            photo_button.addActionListener(this);
            photo_button.setBackground(Color.white);
            add(photo_button);
            ImageIcon cup6 = new ImageIcon(new URL("http://veerex.googlecode.com/svn/trunk/src/movie_icon.jpg"));
            movie_button = new JButton(cup6);
            movie_button.addActionListener(this);
            movie_button.setBackground(Color.white);
            add(movie_button);
            ImageIcon cup7 = new ImageIcon(new URL("http://veerex.googlecode.com/svn/trunk/src/all_icon.jpg"));
            all_button = new JButton(cup7);
            all_button.addActionListener(this);
            all_button.setText("All");
            all_button.setBackground(Color.white);
            c.anchor = GridBagConstraints.FIRST_LINE_END;
            add(all_button, c);
            memo_button = new JButton(new ImageIcon(new URL("http://veerex.googlecode.com/svn/trunk/src/memo_icon.jpg")));
            memo_button.addActionListener(this);
            memo_button.setText("List Memos");
            memo_button.setBackground(Color.white);
            c.anchor = GridBagConstraints.FIRST_LINE_END;
            add(memo_button, c);
            File f = new File("C:\\Veerex-AudioMarkers");
            if (!f.exists()) {
                memo_button.setEnabled(false);
            }
            ImageIcon cup8 = new ImageIcon(new URL("http://veerex.googlecode.com/svn/trunk/src/record_icon.jpg"));
            record_button = new JButton(cup8);
            record_button.addActionListener(this);
            record_button.setText("Record Memo");
            record_button.setBackground(Color.white);
            c.anchor = GridBagConstraints.FIRST_LINE_END;
            add(record_button, c);
            current_selected = all_button;
        } catch (MalformedURLException e) {
        }
    }

    public void setFrame(JFrame vframe) {
        frame = vframe;
    }

    public void beginPlayMemos() {
        memo_thread = new Thread(this);
        memo_thread.setName("Memo");
        memo_thread.start();
    }

    public void beginPlay(boolean save) {
        SourceDataLine line;
        final int bufSize = 16384;
        while (audioInputStream == null) {
        }
        try {
            if (save) audioInputStream.reset();
        } catch (Exception e) {
            return;
        }
        if (save) {
            File file = new File("C:\\Veerex-AudioMarkers");
            file.mkdir();
            if (child.last_clicked_file != null) file = new File("C:\\Veerex-AudioMarkers\\" + child.current_dir.replace('\\', '`').replace(':', '^') + "`" + child.last_clicked_file.getText() + ".wav"); else file = new File("C:\\Veerex-AudioMarkers\\" + child.current_dir.replace('\\', '`').replace(':', '^') + ".wav");
            try {
                file.createNewFile();
                if (AudioSystem.write(audioInputStream, AudioFileFormat.Type.WAVE, file) == -1) {
                    throw new IOException("Problems writing to file");
                }
            } catch (Exception ex) {
                System.out.println(ex.toString());
            }
        }
        AudioFormat format = getFormat(true);
        AudioInputStream playbackInputStream = AudioSystem.getAudioInputStream(format, audioInputStream);
        if (playbackInputStream == null) {
            return;
        }
        DataLine.Info info = new DataLine.Info(SourceDataLine.class, format);
        if (!AudioSystem.isLineSupported(info)) {
            return;
        }
        try {
            line = (SourceDataLine) AudioSystem.getLine(info);
            line.open(format, bufSize);
        } catch (LineUnavailableException ex) {
            return;
        }
        int frameSizeInBytes = format.getFrameSize();
        int bufferLengthInFrames = line.getBufferSize() / 8;
        int bufferLengthInBytes = bufferLengthInFrames * frameSizeInBytes;
        byte[] data = new byte[bufferLengthInBytes];
        int numBytesRead = 0;
        line.start();
        while (true) {
            try {
                if ((numBytesRead = playbackInputStream.read(data)) == -1) {
                    break;
                }
                int numBytesRemaining = numBytesRead;
                while (numBytesRemaining > 0) {
                    numBytesRemaining -= line.write(data, 0, numBytesRemaining);
                }
            } catch (Exception e) {
                break;
            }
        }
        line.drain();
        line.stop();
        line.close();
        line = null;
    }

    public AudioFormat getFormat(boolean wav) {
        if (wav) return new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, 44100, 16, 2, 4, 44100, true); else return new AudioFormat(AudioFormat.Encoding.ULAW, 8000, 8, 1, 1, 8000, true);
    }

    public void beginRecording() {
        thread = new Thread(this);
        thread.setName("Record");
        thread.start();
    }

    public void run() {
        if (Thread.currentThread() == memo_thread) {
            File f = new File("C:\\Veerex-AudioMarkers");
            String[] files;
            String str;
            if (child.last_clicked_file == null) files = f.list(); else {
                files = new String[1];
                files[0] = child.last_clicked_file.getText();
            }
            for (int i = 0; (i < files.length) && (memo_thread != null); i++) {
                current_memo = new String(files[i].replace('^', ':').replace('`', '\\'));
                str = new String(files[i]);
                try {
                    audioInputStream = AudioSystem.getAudioInputStream(new File(f.getAbsolutePath() + "\\" + str));
                    beginPlay(false);
                    audioInputStream.close();
                } catch (Exception ex) {
                }
            }
            try {
                record_button.setText("Delete Memo");
                record_button.setIcon(new ImageIcon(new URL("http://veerex.googlecode.com/svn/trunk/src/delete_icon.jpg")));
                memo_button.setText("Play Memos");
                memo_button.setIcon(new ImageIcon(new URL("http://veerex.googlecode.com/svn/trunk/src/play_icon.jpg")));
            } catch (MalformedURLException e) {
            }
            return;
        }
        TargetDataLine line;
        double duration;
        audioInputStream = null;
        AudioFormat format = getFormat(true);
        DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);
        if (!AudioSystem.isLineSupported(info)) {
            return;
        }
        try {
            line = (TargetDataLine) AudioSystem.getLine(info);
            line.open(format, line.getBufferSize());
        } catch (LineUnavailableException ex) {
            return;
        } catch (SecurityException ex) {
            JavaSound.showInfoDialog();
            return;
        } catch (Exception ex) {
            return;
        }
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        int frameSizeInBytes = format.getFrameSize();
        int bufferLengthInFrames = line.getBufferSize() / 8;
        int bufferLengthInBytes = bufferLengthInFrames * frameSizeInBytes;
        byte[] data = new byte[bufferLengthInBytes];
        int numBytesRead;
        line.start();
        while (thread != null) {
            if ((numBytesRead = line.read(data, 0, bufferLengthInBytes)) == -1) {
                break;
            }
            out.write(data, 0, numBytesRead);
        }
        line.stop();
        line.close();
        line = null;
        try {
            out.flush();
            out.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        byte audioBytes[] = out.toByteArray();
        ByteArrayInputStream bais = new ByteArrayInputStream(audioBytes);
        AudioInputStream myaudioInputStream = new AudioInputStream(bais, format, audioBytes.length / frameSizeInBytes);
        long milliseconds = (long) ((myaudioInputStream.getFrameLength() * 1000) / format.getFrameRate());
        duration = milliseconds / 1000.0;
        try {
            myaudioInputStream.reset();
        } catch (Exception ex) {
            ex.printStackTrace();
            return;
        }
        audioInputStream = myaudioInputStream;
    }

    public void listfiles(String a) {
        JButton buttons_i;
        if (type == 2) {
            setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
            buttons_i = new JButton();
            buttons_i.addActionListener(this);
            try {
                ImageIcon cupdrive = new ImageIcon(new URL("http://veerex.googlecode.com/svn/trunk/src/drive_icon.jpg"));
                buttons_i.setIcon(cupdrive);
            } catch (MalformedURLException e) {
            }
            buttons_i.setText(a);
            buttons_i.setFont(font1);
            buttons_i.setForeground(Color.RED);
            buttons_i.setBorder(null);
            buttons_i.setBackground(this.getBackground());
            buttons_i.setContentAreaFilled(false);
            add(buttons_i);
        } else {
            setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        }
        setBackground(Color.white);
        int i, j;
        File f = new File(a);
        String[] files;
        String mline = new String();
        if (!f.exists()) {
            last_drive_index++;
            System.out.println("empty drive " + a);
            return;
        }
        files = f.list();
        JLabel label = new JLabel("Veeru");
        if (f.isDirectory()) {
            current_dir = new String(f.getAbsolutePath());
            frame.setTitle("VeerExplorer - " + current_dir);
            if (f.getParent() != null) {
                parent_dir = f.getParentFile().getAbsolutePath();
            } else {
                parent_dir = null;
            }
        }
        for (j = last_button_index, i = 0; (files != null) && (i < files.length); j++, i++) {
            File f1 = new File(f.getAbsolutePath() + "\\" + files[i]);
            ImageIcon cup;
            try {
                if (f1.isDirectory()) {
                    if (!showdir) continue;
                    if ((type == 2) && (j < 1024)) buttons_i = buttons[j]; else buttons_i = new JButton();
                    drive_index[j] = last_drive_index;
                    cup = new ImageIcon(new URL("http://veerex.googlecode.com/svn/trunk/src/dir_icon.jpg"));
                    buttons_i.addActionListener(this);
                    buttons_i.setIcon(cup);
                    buttons_i.setText(files[i]);
                    buttons_i.setBorder(null);
                    buttons_i.setBackground(this.getBackground());
                    buttons_i.setContentAreaFilled(false);
                    add(buttons_i);
                    buttons_i.setFont(font);
                } else if (type == 3) {
                    int cmderr = 0;
                    String ext = files[i].substring(files[i].lastIndexOf('.') + 1);
                    try {
                        Process p = Runtime.getRuntime().exec("assoc " + ext);
                    } catch (Exception e) {
                        cmderr = 1;
                    }
                    boolean mismatch = false;
                    switch(filetype) {
                        case TYPE_PHOTO:
                            if ((ext.compareTo("jpg") != 0) && (ext.compareTo("JPG") != 0) && (ext.compareTo("gif") != 0) && ((ext.compareTo("jpeg") != 0))) mismatch = true;
                            break;
                        case TYPE_MOVIE:
                            if ((ext.compareTo("avi") != 0) && (ext.compareTo("mpg") != 0)) mismatch = true;
                            break;
                        case TYPE_PDF:
                            if (ext.compareTo("pdf") != 0) mismatch = true;
                            break;
                        case TYPE_DOC:
                            if (ext.compareTo("doc") != 0) mismatch = true;
                            break;
                        default:
                            break;
                    }
                    if (mismatch) continue;
                    if ((ext.compareTo("jpg") == 0) || (ext.compareTo("JPG") == 0) || (ext.compareTo("gif") == 0) || ((ext.compareTo("jpeg") == 0))) {
                        cup = new ImageIcon(new URL("http://veerex.googlecode.com/files//photos_icon_small.jpg"));
                    } else if ((ext.compareTo("avi") == 0) || (ext.compareTo("mpg") == 0) || (ext.compareTo("mpeg") == 0)) cup = new ImageIcon(new URL("http://veerex.googlecode.com/files//photos_icon_small.jpg")); else if ((ext.compareTo("pdf") == 0)) cup = new ImageIcon(new URL("http://veerex.googlecode.com/svn/trunk/src/pdf_icon_small.gif")); else if ((ext.compareTo("doc") == 0)) cup = new ImageIcon(new URL("http://veerex.googlecode.com/files//docs_icon_small.jpg")); else cup = new ImageIcon(new URL("http://veerex.googlecode.com/files//file_icon.jpg"));
                    if (i < 0) buttons_i = buttons[i]; else buttons_i = new JButton();
                    buttons_i.addActionListener(this);
                    buttons_i.setText(files[i]);
                    buttons_i.setBorder(null);
                    buttons_i.setBackground(this.getBackground());
                    buttons_i.setContentAreaFilled(false);
                    buttons_i.setIcon(cup);
                    add(buttons_i);
                    buttons_i.setFont(font);
                }
            } catch (MalformedURLException e) {
            }
            if (i % 20 == 0) {
                validate();
                pane.revalidate();
                pane.repaint();
            }
        }
        if (type == 2) {
            last_button_index = j;
            last_drive_index++;
            System.out.println("last_drive_index=" + last_drive_index + "button_index=" + last_button_index);
        }
        if (files.length == 0) {
            label = new JLabel("Empty Folder");
            label.setForeground(Color.BLUE);
            add(label);
        }
        validate();
        pane.revalidate();
        pane.repaint();
    }

    private static void createAndShowGUI() {
        int i;
        JFrame.setDefaultLookAndFeelDecorated(true);
        JButtons frame = new JButtons("Veeru File Browser");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setExtendedState(frame.getExtendedState() | JFrame.MAXIMIZED_BOTH);
        WindowUtilities.setNativeLookAndFeel();
        frame.setSize(400, 150);
        Container content = frame.getContentPane();
        content.setBackground(Color.white);
        content.setLayout(new FlowLayout());
        frame.makeButtons(frame);
        frame.addWindowListener(new ExitListener());
        frame.setVisible(true);
        JLabel label = new JLabel("Veeru");
        VeryOldfb ofb = new VeryOldfb();
        ofb.ShowFiles(frame);
        String a = "C:\\";
        File f = new File(a);
        String[] files;
        String mline = new String();
        files = f.list();
        mline = mline.concat("<html><body><br><br><br>");
        label.setText("<br>");
        frame.getContentPane().add(label);
        for (i = 0; i < files.length; i++) {
            mline = mline.concat(files[i]);
            mline = mline.concat("<br>");
            label.setText(files[i]);
            frame.getContentPane().add(label);
            label.setText("<br>");
            frame.getContentPane().add(label);
        }
        mline = mline.concat("</body></html>");
        label.setText("<html><body>first line<br>second line<br>third line<br></body></html>");
        frame.getContentPane().add(label);
        label.setText(mline);
        frame.getContentPane().add(label);
        frame.pack();
        frame.setVisible(true);
    }

    public void actionPerformed(ActionEvent evt) {
        File f = null;
        String str = null;
        boolean openfile = false, dirmenu = false, icon_menu = false;
        Oldfb1 comp;
        if (((JButton) evt.getSource()) == back_button) {
            record_button.setText("Record Memo");
            try {
                record_button.setIcon(new ImageIcon(new URL("http://veerex.googlecode.com/svn/trunk/src/record_icon.jpg")));
                memo_button.setIcon(new ImageIcon(new URL("http://veerex.googlecode.com/svn/trunk/src/memo_icon.jpg")));
                memo_button.setText("List Memos");
            } catch (MalformedURLException e) {
            }
            if (child.parent_dir != null) str = new String(child.parent_dir); else str = new String("C:\\");
            f = new File(str);
        } else if (((JButton) evt.getSource()) == dir_button) {
            str = new String(child.current_dir);
            f = new File(str);
            if (dir_button.getBackground().equals(Color.lightGray)) {
                dir_button.setBackground(this.getBackground());
                dir_button.setText("Off");
                child.showdir = false;
            } else {
                dir_button.setBackground(Color.lightGray);
                dir_button.setText("On");
                child.showdir = true;
            }
        } else if (((JButton) evt.getSource()) == pdf_button) {
            str = new String(child.current_dir);
            f = new File(str);
            current_selected.setBackground(this.getBackground());
            current_selected = pdf_button;
            current_selected.setBackground(Color.lightGray);
            child.filetype = Oldfb1.TYPE_PDF;
        } else if (((JButton) evt.getSource()) == photo_button) {
            str = new String(child.current_dir);
            f = new File(str);
            current_selected.setBackground(this.getBackground());
            current_selected = photo_button;
            current_selected.setBackground(Color.lightGray);
            child.filetype = Oldfb1.TYPE_PHOTO;
        } else if (((JButton) evt.getSource()) == movie_button) {
            str = new String(child.current_dir);
            f = new File(str);
            current_selected.setBackground(this.getBackground());
            current_selected = movie_button;
            current_selected.setBackground(Color.lightGray);
            child.filetype = Oldfb1.TYPE_MOVIE;
        } else if (((JButton) evt.getSource()) == doc_button) {
            str = new String(child.current_dir);
            f = new File(str);
            current_selected.setBackground(this.getBackground());
            current_selected = doc_button;
            current_selected.setBackground(Color.lightGray);
            child.filetype = Oldfb1.TYPE_DOC;
        } else if (((JButton) evt.getSource()) == all_button) {
            str = new String(child.current_dir);
            f = new File(str);
            current_selected.setBackground(this.getBackground());
            current_selected = all_button;
            current_selected.setBackground(Color.lightGray);
            child.filetype = Oldfb1.TYPE_ALL;
        } else if (((JButton) evt.getSource()) == memo_button) {
            if (((JButton) evt.getSource()).getText().equals("Play Memos")) {
                record_button.setText("Go!");
                record_button.setEnabled(true);
                memo_button.setText("Stop Memo");
                try {
                    record_button.setIcon(new ImageIcon(new URL("http://veerex.googlecode.com/svn/trunk/src/go_icon.jpg")));
                    beginPlayMemos();
                } catch (MalformedURLException e) {
                }
                return;
            } else if (((JButton) evt.getSource()).getText().equals("Stop Memo")) {
                memo_thread = null;
                record_button.setText("Record Memo");
                try {
                    record_button.setIcon(new ImageIcon(new URL("http://veerex.googlecode.com/svn/trunk/src/record_icon.jpg")));
                    memo_button.setIcon(new ImageIcon(new URL("http://veerex.googlecode.com/svn/trunk/src/memo_icon.jpg")));
                    memo_button.setText("List Memos");
                } catch (MalformedURLException e) {
                }
                return;
            } else if (((JButton) evt.getSource()).getText().equals("List Memos")) {
                str = new String("C://Veerex-AudioMarkers");
                f = new File(str);
                memo_button.setText("Play Memos");
                record_button.setText("Delete Memo");
                record_button.setEnabled(true);
                try {
                    record_button.setIcon(new ImageIcon(new URL("http://veerex.googlecode.com/svn/trunk/src/delete_icon.jpg")));
                    memo_button.setIcon(new ImageIcon(new URL("http://veerex.googlecode.com/svn/trunk/src/play_icon.jpg")));
                } catch (MalformedURLException e) {
                }
                child.filetype = Oldfb1.TYPE_ALL;
            }
        } else if (((JButton) evt.getSource()).getText().equals("Record Memo")) {
            lineOpen = true;
            beginRecording();
            record_button.setText("Stop");
            return;
        } else if (((JButton) evt.getSource()).getText().equals("Delete Memo")) {
            if (child.last_clicked_file != null) {
                str = new String("C://Veerex-AudioMarkers//" + child.last_clicked_file.getText());
                f = new File(str);
                System.out.println("file " + str + "deleted ?" + f.delete());
            }
            str = new String("C://Veerex-AudioMarkers");
            f = new File(str);
            current_selected.setBackground(this.getBackground());
            current_selected = memo_button;
            current_selected.setBackground(Color.lightGray);
            memo_button.setText("Play Memos");
            record_button.setText("Delete Memo");
            record_button.setEnabled(true);
            try {
                record_button.setIcon(new ImageIcon(new URL("http://veerex.googlecode.com/svn/trunk/src/delete_icon.jpg")));
                memo_button.setIcon(new ImageIcon(new URL("http://veerex.googlecode.com/svn/trunk/src/play_icon.jpg")));
            } catch (MalformedURLException e) {
            }
            child.filetype = Oldfb1.TYPE_ALL;
        } else if (((JButton) evt.getSource()).getText().equals("Stop")) {
            str = null;
            if (lineOpen) {
                lineOpen = false;
                thread = null;
                beginPlay(true);
                memo_button.setEnabled(true);
                record_button.setText("Record Memo");
                try {
                    record_button.setIcon(new ImageIcon(new URL("http://veerex.googlecode.com/svn/trunk/src/record_icon.jpg")));
                } catch (MalformedURLException e) {
                }
                return;
            }
        } else if (((JButton) evt.getSource()).getText().equals("Go!")) {
            openfile = true;
            icon_menu = true;
            Thread mthread = memo_thread;
            memo_thread = null;
            try {
                mthread.join();
            } catch (Exception e) {
            }
            str = current_memo.replace('`', '\\').replace('^', ':');
            str = str.substring(0, str.lastIndexOf(".wav"));
            record_button.setText("Record Memo");
            try {
                record_button.setIcon(new ImageIcon(new URL("http://veerex.googlecode.com/svn/trunk/src/record_icon.jpg")));
                memo_button.setText("List Memos");
                memo_button.setIcon(new ImageIcon(new URL("http://veerex.googlecode.com/svn/trunk/src/memo_icon.jpg")));
            } catch (MalformedURLException e) {
            }
        } else {
            openfile = true;
            if (type == 3) {
                str = new String(current_dir + "\\" + ((JButton) evt.getSource()).getText());
            } else if (type == 2) {
                sibling.record_button.setText("Record Memo");
                try {
                    sibling.record_button.setIcon(new ImageIcon(new URL("http://veerex.googlecode.com/svn/trunk/src/record_icon.jpg")));
                    sibling.memo_button.setIcon(new ImageIcon(new URL("http://veerex.googlecode.com/svn/trunk/src/memo_icon.jpg")));
                    sibling.memo_button.setText("List Memos");
                } catch (MalformedURLException e) {
                }
                str = new String(((JButton) evt.getSource()).getText());
                int j;
                for (j = 0; j < 1024; j++) {
                    if (buttons[j] == ((JButton) evt.getSource())) break;
                }
                if (j < 1024) {
                    System.out.println("j=" + j);
                    System.out.println("drive_index[j]" + drive_index[j]);
                    System.out.println("drive letter" + drives[drive_index[j]]);
                    str = new String(drives[drive_index[j]] + "\\" + ((JButton) evt.getSource()).getText());
                    System.out.println(str);
                }
                dirmenu = true;
            }
        }
        if (openfile) {
            System.out.println("action current" + str);
            f = new File(str);
        }
        if (f.isDirectory()) {
            if (child != null) {
                if (dirmenu) {
                    if (last_clicked_file != null) {
                        last_clicked_file.setContentAreaFilled(false);
                        last_clicked_file.setBackground(this.getBackground());
                    }
                    last_clicked_file = ((JButton) evt.getSource());
                    last_clicked_file.setBackground(Color.ORANGE);
                    last_clicked_file.setContentAreaFilled(true);
                }
                comp = child;
            } else {
                if (((JButton) evt.getSource() != last_clicked_file)) {
                    if (last_clicked_file != null) {
                        last_clicked_file.setBackground(this.getBackground());
                        last_clicked_file.setContentAreaFilled(false);
                    }
                    last_clicked_file = ((JButton) evt.getSource());
                    last_clicked_file.setBackground(Color.ORANGE);
                    last_clicked_file.setContentAreaFilled(true);
                    return;
                }
                last_clicked_file.setBackground(Color.ORANGE);
                comp = this;
            }
            comp.last_clicked_file = null;
            comp.removeAll();
            comp.validate();
            comp.pane.revalidate();
            comp.pane.repaint();
            comp.repaint();
            comp.listfiles(str);
            comp.repaint();
        } else {
            if (!icon_menu) {
                if (((JButton) evt.getSource() != last_clicked_file)) {
                    if (last_clicked_file != null) {
                        last_clicked_file.setBackground(this.getBackground());
                        last_clicked_file.setContentAreaFilled(false);
                    }
                    last_clicked_file = ((JButton) evt.getSource());
                    last_clicked_file.setBackground(Color.ORANGE);
                    last_clicked_file.setContentAreaFilled(true);
                    return;
                }
                last_clicked_file.setBackground(Color.ORANGE);
            }
            try {
                Process p = Runtime.getRuntime().exec("rundll32 url.dll,FileProtocolHandler " + str);
            } catch (Exception e) {
            }
        }
    }

    public static void main(String[] args) {
        javax.swing.SwingUtilities.invokeLater(new Runnable() {

            public void run() {
                createAndShowGUI();
            }
        });
    }
}

class SwingScrollBarExample extends JPanel {

    JLabel label;

    Oldfb1 oldfb1;

    JTextArea textarea;

    JScrollPane scrollPane;

    int type;

    public SwingScrollBarExample() {
        super(true);
    }

    public void setSwingScrollBarExample(int i) {
        label = new JLabel();
        setLayout(new BorderLayout());
        type = i;
        oldfb1 = new Oldfb1(type);
        add(oldfb1, BorderLayout.CENTER);
    }

    public void addbuttons() {
        oldfb1.makeicons();
    }

    public void listfiles(String a, Oldfb1 mychild) {
        textarea = new JTextArea();
        scrollPane = new JScrollPane(oldfb1);
        scrollPane.setViewportBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        setPreferredSize(new Dimension(450, 110));
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        add(scrollPane, BorderLayout.CENTER);
        oldfb1.setScrollPane(scrollPane);
        JScrollBar jsp = scrollPane.getVerticalScrollBar();
        jsp.setUnitIncrement(20);
        oldfb1.setChild(mychild);
        if (type > 1) oldfb1.listfiles(a);
    }

    public void addlisting(String a) {
        oldfb1.listfiles(a);
    }

    public void setFrame(JFrame vframe) {
        oldfb1.setFrame(vframe);
    }

    public void updatefiles(String a) {
        oldfb1.listfiles(a);
    }

    public void resizeParentPane() {
        setPreferredSize(new Dimension(450, 110));
    }

    class MyAdjustmentListener implements AdjustmentListener {

        public void adjustmentValueChanged(AdjustmentEvent e) {
        }
    }

    public static void main(String s[]) {
        JFrame frame = new JFrame("Scroll Bar Example");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setContentPane(new SwingScrollBarExample());
        frame.setSize(200, 200);
        frame.setVisible(true);
    }
}

public class Oldfb {

    public static void main(String args[]) {
        String title = (args.length == 0 ? "Veeru Browser" : args[0]);
        JFrame vFrame = new JFrame(title);
        vFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        SwingScrollBarExample dirTree = new SwingScrollBarExample();
        SwingScrollBarExample blowUp = new SwingScrollBarExample();
        final JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        splitPane.setOneTouchExpandable(true);
        final JSplitPane bottomSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        bottomSplit.setOneTouchExpandable(true);
        blowUp.setSwingScrollBarExample(3);
        blowUp.setFrame(vFrame);
        dirTree.setSwingScrollBarExample(2);
        dirTree.setFrame(vFrame);
        File[] drives = File.listRoots();
        for (int i = 0; i < drives.length; i++) {
            if (i == 0) dirTree.listfiles(drives[i].toString(), blowUp.oldfb1); else dirTree.addlisting(drives[i].toString());
            System.out.println(drives[i].toString());
            dirTree.oldfb1.drives[i] = new String(drives[i].toString());
        }
        blowUp.listfiles("", null);
        bottomSplit.setLeftComponent(dirTree);
        bottomSplit.setRightComponent(blowUp);
        SwingScrollBarExample dashBoard = new SwingScrollBarExample();
        dashBoard.setSwingScrollBarExample(1);
        dashBoard.listfiles("", blowUp.oldfb1);
        dashBoard.addbuttons();
        splitPane.setLeftComponent(dashBoard);
        splitPane.setRightComponent(bottomSplit);
        bottomSplit.setDividerLocation(300);
        dirTree.oldfb1.sibling = dashBoard.oldfb1;
        ActionListener oneActionListener = new ActionListener() {

            public void actionPerformed(ActionEvent event) {
                splitPane.resetToPreferredSizes();
            }
        };
        ActionListener anotherActionListener = new ActionListener() {

            public void actionPerformed(ActionEvent event) {
                splitPane.setDividerLocation(10);
                splitPane.setContinuousLayout(true);
            }
        };
        vFrame.getContentPane().add(splitPane, BorderLayout.CENTER);
        vFrame.setExtendedState(Frame.MAXIMIZED_BOTH);
        vFrame.setVisible(true);
    }
}

class JButtons extends JFrame {

    public JButtons(String str) {
        super(str);
    }

    void makeButtons(JFrame frame) {
        Container content = frame.getContentPane();
        WindowUtilities.setNativeLookAndFeel();
        addWindowListener(new ExitListener());
        content.setBackground(Color.white);
        content.setLayout(new FlowLayout());
        ImageIcon cup = new ImageIcon("http://veerex.googlecode.com/files/pdf_icon.jpg");
        JButton button2 = new JButton(cup);
        content.add(button2);
        ImageIcon cup1 = new ImageIcon("http://veerex.googlecode.com/files/photo_icon.jpg");
        JButton button3 = new JButton(cup1);
        content.add(button3);
        ImageIcon cup2 = new ImageIcon("http://veerex.googlecode.com/files/docs_icon.jpg");
        JButton button4 = new JButton(cup2);
        button4.setHorizontalTextPosition(SwingConstants.CENTER);
        content.add(button4);
        setVisible(true);
    }
}

class ExitListener extends WindowAdapter {

    public void windowClosing(WindowEvent event) {
        System.exit(0);
    }
}

/** A few utilities that simplify using windows in Swing.
 *  1998-99 Marty Hall, http://www.apl.jhu.edu/~hall/java/
 */
class WindowUtilities {

    /** Tell system to use native look and feel, as in previous
   *  releases. Metal (Java) LAF is the default otherwise.
   */
    public static void setNativeLookAndFeel() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
        }
    }

    public static void setJavaLookAndFeel() {
        try {
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
        } catch (Exception e) {
        }
    }

    public static void setMotifLookAndFeel() {
        try {
            UIManager.setLookAndFeel("com.sun.java.swing.plaf.motif.MotifLookAndFeel");
        } catch (Exception e) {
        }
    }

    /** A simplified way to see a JPanel or other Container.
   *  Pops up a JFrame with specified Container as the content pane.
   */
    public static JFrame openInJFrame(Container content, int width, int height, String title, Color bgColor) {
        JFrame frame = new JFrame(title);
        frame.setBackground(bgColor);
        content.setBackground(bgColor);
        frame.setSize(width, height);
        frame.setContentPane(content);
        frame.addWindowListener(new ExitListener());
        frame.setVisible(true);
        return (frame);
    }

    /** Uses Color.white as the background color. */
    public static JFrame openInJFrame(Container content, int width, int height, String title) {
        return (openInJFrame(content, width, height, title, Color.white));
    }

    /** Uses Color.white as the background color, and the
   *  name of the Container's class as the JFrame title.
   */
    public static JFrame openInJFrame(Container content, int width, int height) {
        return (openInJFrame(content, width, height, content.getClass().getName(), Color.white));
    }
}

class VeryOldfb {

    public VeryOldfb() {
        ;
    }

    ;

    public static void main(String[] args) {
        int i;
        String a = "C:\\";
        File f = new File(a);
        String[] files;
        files = f.list();
    }

    public static void ShowFiles(JFrame frame) {
        int i;
        String a = "C:\\";
        File f = new File(a);
        String[] files;
        files = f.list();
        for (i = 0; i < files.length; i++) {
        }
    }
}
