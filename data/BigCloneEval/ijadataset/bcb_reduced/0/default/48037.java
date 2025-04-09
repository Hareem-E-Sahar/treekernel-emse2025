import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import javax.swing.AbstractAction;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.filechooser.FileFilter;

/**
 * * This is free software; you can redistribute it and/or modify it
* under the terms of the GNU Lesser General Public License as
* published by the Free Software Foundation; either version 2.1 of
* the License, or (at your optionjava) any later version.
*
* This software is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
* Lesser General Public License for more details.
*
* You should have received a copy of the GNU Lesser General Public
* License along with this software; if not, write to the Free
* Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
* 02110-1301 USA, or see the FSF site: http://www.fsf.org.
* 
* @author Adam Mazurek
*
*/
public class ProjectGUI extends JFrame {

    private JTextArea area;

    ;

    private JScrollPane scroll;

    private Container cp;

    private JFileChooser wybor;

    private ArrayList<String> lexems;

    private Map<String, Variable> variables;

    private int posOfLastVariable;

    public int getPosOfLastVariable() {
        return posOfLastVariable;
    }

    public void setPosOfLastVariable(int posOfLastVariable) {
        this.posOfLastVariable = posOfLastVariable;
    }

    public Map<String, Variable> getVariables() {
        return variables;
    }

    public void setVariables(Map<String, Variable> variables) {
        this.variables = variables;
    }

    public ProjectGUI() {
        setSize(500, 450);
        setTitle("Pascal Interpreter");
        cp = getContentPane();
        area = new JTextArea();
        scroll = new JScrollPane(area);
        cp.add(scroll, BorderLayout.CENTER);
        variables = new HashMap<String, Variable>();
        JMenu file = new JMenu("Plik");
        JMenuItem open = file.add(new OpenFileAction("Otworz"));
        JMenuItem save = file.add(new SaveFileAction("Zapisz"));
        file.addSeparator();
        JMenuItem exit = file.add(new ExitAction("Zakoncz"));
        JMenuBar menu = new JMenuBar();
        setJMenuBar(menu);
        menu.add(file);
        JMenu action = new JMenu("Uruchom");
        JMenuItem scanner = action.add(new RunScannerAction("Uruchom.."));
        JMenuItem parser = action.add(new RunApp("Info"));
        menu.add(action);
        wybor = new JFileChooser();
        wybor.setCurrentDirectory(new File("."));
        final FiltrRozszerzenia filtr = new FiltrRozszerzenia();
        filtr.ustawRozszerzenie("pas");
        wybor.setFileFilter(filtr);
        setVisible(true);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    /**
	 * @param args
	 */
    public static void main(String[] args) {
        new ProjectGUI();
    }

    /**
	 * Klasy wewznetrzne - aby mialy dostep do skladnikow okna
	 */
    class OpenFileAction extends AbstractAction {

        public OpenFileAction(String nazwa) {
            super(nazwa);
        }

        public void actionPerformed(ActionEvent event) {
            int wynik = wybor.showOpenDialog(ProjectGUI.this);
            if (wynik == JFileChooser.APPROVE_OPTION) {
                try {
                    area.setText("");
                    String nazwa = wybor.getSelectedFile().getPath();
                    FileReader in = new FileReader(new File(nazwa));
                    BufferedReader buf = new BufferedReader(in);
                    String end = "\n";
                    String line;
                    while ((line = buf.readLine()) != null) {
                        area.append(line.concat(end));
                    }
                    buf.close();
                    in.close();
                } catch (IOException e) {
                    JOptionPane.showMessageDialog(null, "Blad podczas odczytu pliku", "Blad", JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    }

    class SaveFileAction extends AbstractAction {

        public SaveFileAction(String nazwa) {
            super(nazwa);
        }

        public void actionPerformed(ActionEvent event) {
        }
    }

    class ExitAction extends AbstractAction {

        public ExitAction(String nazwa) {
            super(nazwa);
        }

        public void actionPerformed(ActionEvent event) {
            ProjectGUI.this.setVisible(false);
            System.exit(0);
        }
    }

    class RunApp extends AbstractAction {

        public RunApp(String nazwa) {
            super(nazwa);
        }

        public void actionPerformed(ActionEvent event) {
            JOptionPane.showMessageDialog(null, "Main Programmer Adas. Second Programmer Tonk.");
        }
    }

    class RunScannerAction extends AbstractAction {

        public RunScannerAction(String nazwa) {
            super(nazwa);
        }

        public void actionPerformed(ActionEvent event) {
            try {
                Scanner scanner = new Scanner(ProjectGUI.this);
                ArrayList<String> lexms = new ArrayList<String>();
                variables = new HashMap<String, Variable>();
                String esc = "\n";
                String text = area.getText();
                String[] lines = text.split("\n");
                for (String line : lines) {
                    scanner.scan(line.trim(), lexms);
                }
                lexems = new ArrayList<String>();
                for (String s : lexms) {
                    if (s.indexOf(";") != -1 && s.length() != 1) {
                        lexems.add(s.substring(0, s.indexOf(";")));
                        lexems.add(";");
                    } else lexems.add(s);
                }
                for (int i = 0; i < lexems.size() - 1; i++) {
                    if (lexems.get(i).equals(";") && lexems.get(i + 1).equals(";")) lexems.remove(i);
                }
                for (int i = 0; i < lexems.size(); i++) {
                    String s = lexems.get(i);
                    if (s.equalsIgnoreCase("read")) {
                        String ss = lexems.get(i + 1);
                        int j = i + 1;
                        while (j < lexems.size()) {
                            if (Scanner.isInteger(ss) || Scanner.isChar(ss) || Scanner.isKeyword(ss) || Scanner.isLogicalOperator(ss) || Scanner.isReal(ss) || Scanner.isString(ss) || Scanner.isType(ss)) throw new Exception("Niepoprawny argument dla isntrukcji read ");
                            if (Scanner.isIdentifier(ss)) {
                                Variable v = variables.get(ss);
                                if (v == null) throw new Exception("Nieznana zmienna " + ss);
                            }
                            ss = lexems.get(j++);
                            if (j == lexems.size()) break;
                            if (ss.equals(";")) break;
                        }
                    }
                    if (s.equalsIgnoreCase("write") || s.equalsIgnoreCase("writeln")) {
                        String ss = lexems.get(i + 1);
                        int j = i + 1;
                        while (j < lexems.size()) {
                            if (Scanner.isIdentifier(ss)) {
                                Variable v = variables.get(ss);
                                if (v == null) throw new Exception("Nieznana zmienna " + ss);
                            }
                            ss = lexems.get(j++);
                            if (j == lexems.size()) break;
                            if (ss.equals(";")) break;
                        }
                    }
                }
                Parser p = new Parser(lexems, ProjectGUI.this);
                Interpreter interpreter = new Interpreter(lexems);
                interpreter.go();
            } catch (Exception e) {
                JOptionPane.showMessageDialog(null, e.getMessage(), e.toString(), JOptionPane.ERROR_MESSAGE);
                e.printStackTrace();
            }
        }
    }

    class FiltrRozszerzenia extends FileFilter {

        private String extension;

        /**
		       Dodaje rozszerzenie, od tej chwili bedzie rozpoznawane przez filtr plikow
		       @param rozszerzenie rozszerzenie pliku (np. ".txt", lub "txt")
		    */
        public void ustawRozszerzenie(String rozszerzenie) {
            if (!rozszerzenie.startsWith(".")) rozszerzenie = "." + rozszerzenie;
            this.extension = rozszerzenie;
        }

        /**
		      Zwraca opis plikow, rozpoznawanych przez dany 
		      filtr plikow.
		      @return opis plikow, rozpoznawanych przez filtr
		    */
        public String getDescription() {
            return opis;
        }

        public boolean accept(File p) {
            if (p.isDirectory()) return true;
            String nazwa = p.getName().toLowerCase();
            if (nazwa.endsWith(extension)) return true;
            return false;
        }

        private String opis = "Pliki programow w jezyku Pascal";
    }
}

class MyClassLoader extends ClassLoader {

    public Class findClass(String name) {
        byte[] b = loadClassData(name);
        return defineClass(name, b, 0, b.length);
    }

    private byte[] loadClassData(String name) {
        try {
            Properties p = System.getProperties();
            String path = p.getProperty("java.library.path").split(";")[0];
            FileInputStream f = new FileInputStream(new File("output.class"));
            byte[] data = new byte[f.available()];
            f.read(data);
            return data;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
