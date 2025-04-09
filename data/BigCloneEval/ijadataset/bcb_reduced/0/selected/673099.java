package esdomaci.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.Font;
import java.awt.GridLayout;
import java.net.URI;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextPane;

/**
 * @author Milan Aleksic, milanaleksic@gmail.com
 */
public class OProgramu extends JDialog {

    private static final long serialVersionUID = 1L;

    private javax.swing.JPanel jContentPane = null;

    private JButton jButton = null;

    private JPanel panelOpisaZahteva = null;

    private JPanel panelOpisaAutora = null;

    private JPanel panelOpisaPrograma = null;

    private JTabbedPane glavno = null;

    private static final String opisZahteva = "Потребно је реализовати симулацију следеће игре:\n\n" + "Игра се одвија у пећини која има 4x4 просторија. Постоје пролази између суседних просторија. У једној од ћелија се налази злато и циљ игре је пронаћи злато. Такође у једној од просторија се налази људождер који поједе играча ако уђе у његову просторију. У две просторије се налазе и рупе у које играч упада и губи живот. У једној просторији се може налазити и људождер и рупа. У просторији где се налази злато не налазе се ни људождер, ни рупа. Циљ симулатора је да открије злато у ситуацији коју је корисник дефинисао и у тренутку када открије злато симулација се завршава успешно. Ако пре успешног краја симулатор уђе у просторију са људождером или рупом симулација се завршава неуспешно.\n\n" + "Симулатор може да осети мирис људождера у просторијама које су суседне оној у којој се људождер налази, може да осети струјање ваздуха у просторијама суседним оној у којој се налази рупа и да уочи светлост у просторијама  које су суседне оној у којој се налази злато.\n\n" + "Потребно је реализовати кориснички интерфејс који омогућава дефинисање опажаја од стране корисника (мирис, струјање или светлост) после сваког реализованог потеза симулатора. Потребно је приказати следећи потез симулатора и кратко објашњење зашто је изведен тај потез.";

    private static final String opisPrograma = "ЗНАЧЕЊЕ БОЈА:\n\n" + "светла окер - непосећена одаја\n" + "тамна окер - сумња на неку опасност\n" + "светло сива - безбедна одаја која ће бити посећена\n" + "светло жута - безбедна раније већ посећена одаја\n" + "тамно црвена - препозната (\"фиксирана\") опасност\n\n" + "-----------------\n\nЛЕГЕНДА ОЗНАКА НА АИ МАПИ:\n\n" + "ОПШТЕ:\n? - непозната одаја\nO - позиција истраживача\n\n" + "СУМЊЕ:\nk - сумња на канибала\n" + "z - сумња на злато\n" + "r - сумња на рупу\n" + "x - сумња на комбиновану опасност\n" + "n - грешка у АИ (не би требало да се појави)\n\n" + "ОДЛУКЕ:\nB - безбедна одаја\n" + "Q-комплексна одаја (и сумња и фикс)\n" + "K - канибал\n" + "Z - злато\n" + "R - рупа\n" + "X - рупа и канибал у истој одаји\n" + "N - грешка у АИ (не би требало да се појави)\n\n" + "-----------------\n\nОПИС СКРАЋЕНИЦА:\n" + "УЛП - уланчана листа потеза, користи се за диктирање низа потеза\n" + "ХПФ - Хеуристика Препознавања Фиксова\n" + "ХУСПФ - Хеуристика Уклањања Сумњи Препознатих Фиксева\n" + "ХУСДР - Хеуристика Уклањања Сумњи на Далеке Рупе\n" + "ХДДР - Хеуристика Дуге Дијагонале Рупа";

    private JScrollPane jScrollPane = null;

    private JScrollPane jScrollPane2 = null;

    private JTextPane poljeOpisaZahteva = null;

    private JTextPane poljeOpisaPrograma = null;

    /**
	 * This is the default constructor
	 */
    public OProgramu() {
        super();
        initialize();
    }

    /**
	 * This method initializes this
	 * 
	 * @return void
	 */
    private void initialize() {
        this.setTitle("О програму...");
        this.setModal(true);
        this.setSize(450, 300);
        this.setResizable(false);
        this.setLocation(150, 150);
        this.setContentPane(getJContentPane());
        this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
    }

    /**
	 * This method initializes jContentPane
	 * 
	 * @return javax.swing.JPanel
	 */
    private javax.swing.JPanel getJContentPane() {
        if (jContentPane == null) {
            jContentPane = new javax.swing.JPanel();
            jContentPane.setLayout(new java.awt.BorderLayout());
            JPanel dole = new JPanel();
            dole.add(getJButton());
            jContentPane.add(dole, java.awt.BorderLayout.SOUTH);
            jContentPane.add(getRadniPanel(), java.awt.BorderLayout.CENTER);
        }
        return jContentPane;
    }

    private JTabbedPane getRadniPanel() {
        if (glavno == null) {
            glavno = new JTabbedPane();
            glavno.addTab("Опис захтева", getOpisZahteva());
            glavno.addTab("О симулатору", getOpisPrograma());
            glavno.addTab("О аутору", getOpisAutora());
        }
        return glavno;
    }

    private JPanel getOpisAutora() {
        if (panelOpisaAutora == null) {
            panelOpisaAutora = new JPanel();
            panelOpisaAutora.setLayout(new GridLayout(6, 1));
            JLabel jLabel = new JLabel();
            jLabel.setText("Аутор програма је студент Милан Алексић 63/02 - ЕТФ Београд");
            jLabel.setHorizontalAlignment(JLabel.CENTER);
            JLabel jLabel4 = new JLabel();
            jLabel4.setText("ВЕРЗИЈА 3 (март 2007)");
            jLabel4.setHorizontalAlignment(JLabel.CENTER);
            JLabel jLabel2 = new JLabel();
            jLabel2.setText("http://drop.to/goblin");
            jLabel2.setHorizontalAlignment(JLabel.CENTER);
            jLabel2.setForeground(Color.blue);
            jLabel2.setFont(new Font("Dialog", Font.BOLD, 12));
            jLabel2.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            jLabel2.addMouseListener(new java.awt.event.MouseAdapter() {

                public void mouseClicked(java.awt.event.MouseEvent e) {
                    new Thread(new Runnable() {

                        public void run() {
                            if (Desktop.isDesktopSupported()) {
                                Desktop desktop = Desktop.getDesktop();
                                if (desktop.isSupported(Desktop.Action.BROWSE)) {
                                    try {
                                        desktop.browse(URI.create("http://drop.to/goblin"));
                                    } catch (Exception exc) {
                                        System.err.println("Nije omoguceno krstarenje Internetom");
                                    }
                                }
                            }
                        }
                    }).start();
                }
            });
            JLabel jLabel3 = new JLabel();
            jLabel3.setText("milan.aleksic@gmail.com");
            jLabel3.setHorizontalAlignment(JLabel.CENTER);
            jLabel3.setForeground(Color.blue);
            jLabel3.setFont(new Font("Dialog", Font.BOLD, 12));
            jLabel3.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            jLabel3.addMouseListener(new java.awt.event.MouseAdapter() {

                public void mouseClicked(java.awt.event.MouseEvent e) {
                    new Thread(new Runnable() {

                        public void run() {
                            if (Desktop.isDesktopSupported()) {
                                Desktop desktop = Desktop.getDesktop();
                                if (desktop.isSupported(Desktop.Action.MAIL)) {
                                    try {
                                        desktop.mail(new URI("mailto:milan.aleksic@gmail.com"));
                                    } catch (Exception exc) {
                                        System.err.println("Nemoguce slanje elektronske poste");
                                    }
                                }
                            }
                        }
                    }).start();
                }
            });
            panelOpisaAutora.add(new JLabel(""));
            panelOpisaAutora.add(jLabel);
            panelOpisaAutora.add(jLabel4);
            panelOpisaAutora.add(jLabel2);
            panelOpisaAutora.add(jLabel3);
            panelOpisaAutora.add(new JLabel(""));
        }
        return panelOpisaAutora;
    }

    private JButton getJButton() {
        if (jButton == null) {
            jButton = new JButton();
            jButton.setText("Затвори прозор");
            jButton.addActionListener(new java.awt.event.ActionListener() {

                public void actionPerformed(java.awt.event.ActionEvent e) {
                    setVisible(false);
                }
            });
        }
        return jButton;
    }

    private JPanel getOpisZahteva() {
        if (panelOpisaZahteva == null) {
            panelOpisaZahteva = new JPanel();
            panelOpisaZahteva.setLayout(new BorderLayout());
            panelOpisaZahteva.add(getJScrollPane(), BorderLayout.CENTER);
        }
        return panelOpisaZahteva;
    }

    private JPanel getOpisPrograma() {
        if (panelOpisaPrograma == null) {
            panelOpisaPrograma = new JPanel();
            panelOpisaPrograma.setLayout(new BorderLayout());
            panelOpisaPrograma.add(getJScrollPane2(), BorderLayout.CENTER);
        }
        return panelOpisaPrograma;
    }

    private JScrollPane getJScrollPane() {
        if (jScrollPane == null) {
            jScrollPane = new JScrollPane();
            jScrollPane.setViewportView(getPoljeOpisaZahteva());
        }
        return jScrollPane;
    }

    private JScrollPane getJScrollPane2() {
        if (jScrollPane2 == null) {
            jScrollPane2 = new JScrollPane();
            jScrollPane2.setViewportView(getPoljeOpisaPrograma());
        }
        return jScrollPane2;
    }

    private JTextPane getPoljeOpisaZahteva() {
        if (poljeOpisaZahteva == null) {
            poljeOpisaZahteva = new JTextPane();
            poljeOpisaZahteva.setEditable(false);
            poljeOpisaZahteva.setText(OProgramu.opisZahteva);
            poljeOpisaZahteva.setCaretPosition(0);
        }
        return poljeOpisaZahteva;
    }

    private JTextPane getPoljeOpisaPrograma() {
        if (poljeOpisaPrograma == null) {
            poljeOpisaPrograma = new JTextPane();
            poljeOpisaPrograma.setEditable(false);
            poljeOpisaPrograma.setText(OProgramu.opisPrograma);
            poljeOpisaPrograma.setCaretPosition(0);
        }
        return poljeOpisaPrograma;
    }
}
