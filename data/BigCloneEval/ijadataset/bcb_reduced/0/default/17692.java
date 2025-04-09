import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.List;
import java.awt.event.ActionEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.ScrollPaneConstants;

/**
 * Pasinterpreter - prosty interpreter j�zyka Pascal Copyright (C) 2007
 * Krzysztof Mazurek
 * 
 * Niniejszy program jest wolnym oprogramowaniem; mo�esz go rozprowadza� dalej
 * i/lub modyfikowa� na warunkach Powszechnej Licencji Publicznej GNU, wydanej
 * przez Fundacj� Wolnego Oprogramowania - wed�ug wersji 2-giej tej Licencji lub
 * kt�rej� z p�niejszych wersji.
 * 
 * Niniejszy program rozpowszechniany jest z nadziej�, i� b�dzie on u�yteczny -
 * jednak BEZ JAKIEJKOLWIEK GWARANCJI, nawet domy�lnej gwarancji PRZYDATNO�CI
 * HANDLOWEJ albo PRZYDATNO�CI DO OKRE�LONYCH ZASTOSOWA�. W celu uzyskania
 * bli�szych informacji - Powszechna Licencja Publiczna GNU.
 * 
 * Z pewno�ci� wraz z niniejszym programem otrzyma�e� te� egzemplarz Powszechnej
 * Licencji Publicznej GNU (GNU General Public License); je�li nie - napisz do
 * Free Software Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 * 
 */
public class Pasinterpreter extends JFrame {

    /**
	 * 
	 */
    private static final long serialVersionUID = 1L;

    public JTextArea lex;

    private JTextArea tekst;

    public JTextArea wynik;

    private JScrollPane suwak;

    private JScrollPane scroll;

    private JScrollPane suwak2;

    private JButton otworz;

    private ArrayList<String> lexems;

    private JButton analizuj;

    private JFileChooser wyborPliku;

    private Container cp;

    private Map<String, Zmienna> zmienne;

    private Map<String, Tablica> tablica;

    private int pozOsZmiennej;

    boolean ok = false;

    public void setZmienne(Map<String, Zmienna> zmienne) {
        this.zmienne = zmienne;
    }

    public void setPozycjaOstatniejZmiennej(int pozOsZmiennej) {
        this.pozOsZmiennej = pozOsZmiennej;
    }

    public int getPozOsZmiennej() {
        return pozOsZmiennej;
    }

    public Map<String, Zmienna> getZmienne() {
        return zmienne;
    }

    public void setTablica(Map<String, Tablica> tablica) {
        this.tablica = tablica;
    }

    public Map<String, Tablica> getTablica() {
        return tablica;
    }

    public Pasinterpreter() {
        try {
            zmienne = new HashMap<String, Zmienna>();
            JPanel buttonsPanel = new JPanel(new GridBagLayout());
            JPanel buttons2Panel = new JPanel(new GridBagLayout());
            JPanel mainPanel = new JPanel(new BorderLayout());
            JPanel upPanel = new JPanel(new BorderLayout());
            JPanel downPanel = new JPanel(new BorderLayout());
            JPanel textPanel = new JPanel(new BorderLayout());
            JPanel leksemPanel = new JPanel(new BorderLayout());
            JPanel wynikPanel = new JPanel(new BorderLayout());
            setSize(600, 400);
            setTitle(">>Pasinterpreter<<");
            cp = getContentPane();
            tekst = new JTextArea();
            lex = new JTextArea();
            lex.setLineWrap(true);
            lex.append("Leksemy: \n");
            tekst.setEditable(true);
            tekst.setSelectedTextColor(Color.GREEN);
            tekst.setLineWrap(true);
            tekst.setTabSize(2);
            wynik = new JTextArea();
            wynik.setLineWrap(true);
            wynik.setEditable(false);
            wynik.setRows(4);
            wynik.setSize(2, 2);
            suwak = new JScrollPane(tekst, ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
            scroll = new JScrollPane(lex, ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
            suwak2 = new JScrollPane(wynik, ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
            textPanel.add(suwak, BorderLayout.CENTER);
            leksemPanel.add(scroll, BorderLayout.CENTER);
            wynikPanel.add(suwak2, BorderLayout.CENTER);
            wynikPanel.setSize(200, 200);
            otworz = new JButton("Otworz plik");
            analizuj = new JButton("Uruchom");
            GridBagConstraints c = new GridBagConstraints();
            c.fill = GridBagConstraints.BOTH;
            c.gridx = 100;
            c.gridy = 100;
            buttonsPanel.add(otworz, c);
            c.gridx = 200;
            c.gridy = 200;
            buttons2Panel.add(analizuj, c);
            upPanel.add(buttonsPanel, BorderLayout.CENTER);
            downPanel.add(buttons2Panel, BorderLayout.CENTER);
            downPanel.add(wynikPanel, BorderLayout.NORTH);
            mainPanel.add(upPanel, BorderLayout.PAGE_START);
            mainPanel.add(downPanel, BorderLayout.PAGE_END);
            mainPanel.add(textPanel, BorderLayout.CENTER);
            mainPanel.add(leksemPanel, BorderLayout.AFTER_LINE_ENDS);
            cp.add(mainPanel);
            wyborPliku = new JFileChooser();
            wyborPliku.setCurrentDirectory(new File("."));
            otworz.addActionListener(new OtworzPlik("Otworz"));
            analizuj.addActionListener(new Skanuj("Jazda"));
            setVisible(true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        new Pasinterpreter();
    }

    class OtworzPlik extends AbstractAction {

        /**
		 * 
		 */
        private static final long serialVersionUID = 1L;

        public OtworzPlik(String nazwa_pliku) {
            super(nazwa_pliku);
        }

        public void actionPerformed(ActionEvent event) {
            int wynik = wyborPliku.showOpenDialog(Pasinterpreter.this);
            if (wynik == JFileChooser.APPROVE_OPTION) {
                try {
                    tekst.setText("");
                    String nazwa = wyborPliku.getSelectedFile().getPath();
                    FileReader in = new FileReader(new File(nazwa));
                    BufferedReader buf = new BufferedReader(in);
                    String enter = "\n";
                    String linia;
                    while ((linia = buf.readLine()) != null) {
                        tekst.append(linia.concat(enter));
                    }
                    ;
                    buf.close();
                    in.close();
                } catch (IOException e) {
                    JOptionPane.showMessageDialog(null, "Blad otwarcia", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    }

    class Skanuj extends AbstractAction {

        /**
		 * 
		 */
        public Skanuj(String naz) {
            super(naz);
        }

        private static final long serialVersionUID = 1L;

        private ArrayList<String> popraw_leksemy(ArrayList<String> leks) {
            for (int i = 1; i < leks.size() - 1; i++) {
                String leksem = leks.get(i);
                if (leksem.equals(".")) {
                    String a = leks.get(i - 1);
                    String b = leks.get(i + 1);
                    if (czy_liczba_int(a) && czy_liczba_int(b)) {
                        String nowy = a + leksem + b;
                        leks.set(i, nowy);
                        leks.remove(i + 1);
                        leks.remove(i - 1);
                    }
                }
            }
            return leks;
        }

        boolean czy_liczba_int(String co) {
            boolean wynik = true;
            try {
                Integer.parseInt(co);
            } catch (NumberFormatException e) {
                wynik = false;
            }
            return wynik;
        }

        public void actionPerformed(ActionEvent event) {
            try {
                Leksyka analizator = new Leksyka(Pasinterpreter.this);
                ArrayList<String> leksemy = new ArrayList<String>();
                zmienne = new HashMap<String, Zmienna>();
                String enter = "\n";
                wynik.setText("");
                String text = tekst.getText();
                String[] lines = text.split("\n");
                for (String line : lines) {
                    line = line.replace('\t', ' ');
                    analizator.analizuj(line, leksemy);
                }
                lex.setToolTipText("Lista leksemow");
                lex.setText("Leksemy: \n");
                lexems = new ArrayList<String>();
                lex.setBackground(Color.LIGHT_GRAY);
                for (String s : leksemy) {
                    if (s.indexOf(";") != -1 && s.length() != 1) {
                        lexems.add(s.substring(0, s.indexOf(";") - 1));
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
                            if (Leksyka.czyCalkowita(ss) || Leksyka.czyZnak(ss) || Leksyka.czySlowoKluczowe(ss) || Leksyka.czyRzeczywista(ss) || Leksyka.czyString(ss) || Leksyka.czyTyp(ss)) throw new Exception("ERROR: Niepoprawny argument dla isntrukcji read ");
                            if (Leksyka.czyIdentyfikator(ss)) {
                                if (!Skladnia.czyZmiennaTablicowa(ss) && !ss.contains("[")) {
                                    Zmienna z = zmienne.get(ss);
                                    if (z == null) throw new Exception("ERROR: Nieznana zmienna " + ss);
                                }
                            }
                            ss = lexems.get(j++);
                            if (j == lexems.size()) break;
                            if (ss.equals(";")) break;
                        }
                    }
                }
                boolean type = false;
                for (String s : lexems) {
                    if (Leksyka.czyTyp(s)) {
                        type = true;
                        break;
                    }
                }
                if (type) {
                }
                lexems = popraw_leksemy(lexems);
                int i = 0;
                for (String s : lexems) {
                    lex.append(i + ": " + s + "\n");
                    i++;
                }
                Skladnia skladnia = new Skladnia(lexems, Pasinterpreter.this);
                Interpreter interpreter = new Interpreter(lexems, Pasinterpreter.this);
                ok = true;
            } catch (Exception e) {
                JOptionPane.showMessageDialog(null, e.getMessage(), e.toString(), JOptionPane.ERROR_MESSAGE);
                e.printStackTrace();
                wynik.setForeground(Color.RED);
                wynik.append(e.toString());
            }
            if (Skladnia.check && ok && Leksyka.spr && Leksyka.oki) {
                lex.append("\n Program status: OK");
            }
        }
    }
}
