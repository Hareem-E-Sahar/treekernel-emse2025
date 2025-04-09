import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.awt.BorderLayout;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JScrollPane;

public class GAPInterface {

    private Process p;

    private InputStreamReader isr;

    private BufferedWriter bw;

    private final String[] cmd;

    private ArrayList<String> inputLog = new ArrayList<String>();

    public GAPInterface() {
        String libPath = Configuration.getDefaultConfiguration().gapLibraryPath;
        if (libPath == null || libPath.isEmpty()) {
            cmd = new String[] { "gap", "-b" };
        } else {
            cmd = new String[] { "gap", "-b", "-l", libPath };
        }
        init();
    }

    public static void start() {
    }

    private void init() {
        try {
            ProcessBuilder pb = new ProcessBuilder(cmd);
            pb.redirectErrorStream(true);
            p = pb.start();
            isr = new InputStreamReader(p.getInputStream());
            bw = new BufferedWriter(new OutputStreamWriter(p.getOutputStream()));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void empty() {
        try {
            while (isr.ready()) {
                isr.read();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String read() {
        String output = "";
        try {
            while (true) {
                if (isr.ready()) {
                    int chr = isr.read();
                    int len = output.length() - 3;
                    if (chr == '>' && len >= 0 && output.substring(len).equals("gap")) {
                        output = output.substring(0, len);
                        break;
                    }
                    if (chr == '>' && len >= 0 && output.substring(len).equals("brk")) {
                        output = output.substring(0, len);
                        write("quit;\n");
                        break;
                    }
                    output += (char) chr;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        output = output.replace("\\\n", "").trim();
        return output;
    }

    private void write(String line) {
        try {
            bw.write(line);
            bw.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String execute(String command) {
        command = command.trim();
        if (command.isEmpty()) {
            return "";
        }
        if (!command.endsWith(";")) {
            command += ";";
        }
        empty();
        write(command + "\n");
        inputLog.add(command);
        String output = read();
        return output;
    }

    public void close() {
        p.destroy();
    }

    public Quiver quiverProduct(Quiver q1, Quiver q2) {
        String name1 = q1.getName();
        String name2 = q2.getName();
        String productName = name1 + "x" + name2;
        sendQuiver(q1);
        sendQuiver(q2);
        execute(productName + " := QuiverProduct(" + name1 + ", " + name2 + ")");
        String gapCode = execute("Print(" + productName + ",\"\\n\")");
        Quiver prodQuiver = Quiver.fromGAPCode(gapCode);
        prodQuiver.setName(productName);
        return prodQuiver;
    }

    public void sendQuiver(Quiver quiver) {
        execute(quiver.toGAPCode());
    }

    public void showInterface() {
        final GAPInterface gapInterface = QuiverFrame.getInstance().gapInterface;
        JFrame frame = new JFrame("Gap interface");
        frame.setSize(300, 300);
        frame.setVisible(true);
        JPanel panel = new JPanel(new BorderLayout());
        final JTextField inputField = new JTextField();
        final JTextArea outputArea = new JTextArea();
        final JScrollPane outputScroll = new JScrollPane(outputArea);
        outputArea.setLineWrap(true);
        outputArea.setWrapStyleWord(true);
        outputArea.setEditable(false);
        inputField.setFont(Configuration.getDefaultConfiguration().gapInterfaceInputFont);
        outputArea.setFont(Configuration.getDefaultConfiguration().gapInterfaceOutputFont);
        inputField.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent arg0) {
                String input = inputField.getText();
                if (!input.endsWith(";")) {
                    input += ";";
                }
                outputArea.append("\ngap> " + input + "\n" + gapInterface.execute(input));
                outputScroll.getVerticalScrollBar().setValue(outputScroll.getVerticalScrollBar().getMaximum());
                inputField.setText("");
            }
        });
        panel.add(inputField, BorderLayout.NORTH);
        panel.add(outputScroll, BorderLayout.CENTER);
        frame.add(panel);
        frame.setVisible(true);
    }

    public void writeInputLogToFile(File file) {
        BufferedWriter bufferedWriter = null;
        try {
            bufferedWriter = new BufferedWriter(new FileWriter(file));
            for (String command : inputLog) {
                bufferedWriter.write(command);
                bufferedWriter.newLine();
            }
        } catch (FileNotFoundException ex) {
            ex.printStackTrace();
        } catch (IOException ex) {
            ex.printStackTrace();
        } finally {
            try {
                if (bufferedWriter != null) {
                    bufferedWriter.flush();
                    bufferedWriter.close();
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }
}
