import java.awt.Dimension;
import java.awt.TextArea;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import javax.swing.JFrame;

public class Compilation {

    JFrame ab;

    TextArea mess;

    /**Compiles code*/
    public String compile(String code, String name, boolean only) {
        if (only) {
            displayWait();
        }
        String output = "";
        String error = "";
        FileOutputStream out;
        PrintStream p;
        try {
            out = new FileOutputStream(name + ".java");
            p = new PrintStream(out);
            p.println(code);
            p.close();
            out.close();
            Process p1 = Runtime.getRuntime().exec("javac " + name + ".java");
            InputStream in = p1.getInputStream();
            InputStream err = p1.getErrorStream();
            int c = 0;
            int d = 0;
            c = in.read();
            d = err.read();
            while (c != -1) {
                output = output + (char) c;
                c = in.read();
            }
            while (d != -1) {
                error = error + (char) d;
                d = err.read();
            }
            in.close();
            err.close();
        } catch (Exception e) {
            System.out.println("error");
        }
        if (only) {
            if (error.equals("")) {
                displayResult("Compilation result", "Compilation sucessful");
            } else {
                displayResult("Compilation result", error);
            }
        }
        return error;
    }

    /**Calls compile() and then runs code*/
    public void comrun(String code, String name) {
        displayWait();
        String comres = compile(code, name, false);
        System.out.println(comres);
        if (!comres.equals("")) {
            displayResult("Code compilation", comres);
        } else {
            String output = "";
            try {
                Process p2 = Runtime.getRuntime().exec("java " + name);
                InputStream in = p2.getInputStream();
                int c = 0;
                c = in.read();
                while (c != -1) {
                    output = output + (char) c;
                    c = in.read();
                }
                in.close();
                displayResult("Code output", "Compilation sucessful\n\n" + output);
            } catch (Exception e) {
                System.out.println("error");
            }
        }
    }

    /**Displays result of compoilation*/
    public void displayWait() {
        ab = new JFrame("jEneration: Compiling...");
        ab.setPreferredSize(new Dimension(300, 150));
        ab.setMinimumSize(new Dimension(300, 150));
        mess = new TextArea();
        mess.setEditable(false);
        mess.setPreferredSize(new Dimension(300, 150));
        mess.setMinimumSize(new Dimension(300, 150));
        mess.setText("Compiling...");
        ab.add(mess);
        ab.setLocation(jEn.program.getLocation().x + 100, jEn.program.getLocation().y + 100);
        ab.setVisible(true);
    }

    /**Displays result of compoilation*/
    public void displayResult(String title, String res) {
        ab.dispose();
        ab = new JFrame(title);
        ab.setPreferredSize(new Dimension(300, 150));
        ab.setMinimumSize(new Dimension(300, 150));
        mess = new TextArea();
        mess.setEditable(false);
        mess.setPreferredSize(new Dimension(300, 150));
        mess.setMinimumSize(new Dimension(300, 150));
        mess.setText(res);
        ab.add(mess);
        ab.setLocation(jEn.program.getLocation().x + 100, jEn.program.getLocation().y + 100);
        ab.setVisible(true);
    }
}
