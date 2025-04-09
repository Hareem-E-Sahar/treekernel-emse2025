package com;

import com.Musiquera.PropriedadesMusica;
import com.conexao.BD;
import com.config.GerenciadorConfig;
import com.config.JConfiguracao;
import com.help.JHelp;
import com.help.JSobre;
import com.main.Carregador;
import com.main.Notificavel;
import com.melloware.jintellitype.HotkeyListener;
import com.melloware.jintellitype.IntellitypeListener;
import com.melloware.jintellitype.JIntellitype;
import com.utils.Warning;
import java.awt.Image;
import java.awt.event.MouseEvent;
import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import java.awt.Container;
import java.awt.image.BufferedImage;
import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javazoom.jlgui.basicplayer.BasicPlayerEvent;

/**
 *
 * @author manchini
 */
public class JPrincipal extends javax.swing.JFrame implements HotkeyListener, IntellitypeListener, Notificavel {

    public static Aguarde aguarde = new Aguarde();

    private Musiquera musiquera;

    private int estado = 0;

    private JFileChooser jFileChooser = new JFileChooser();

    GerenciadorConfig _conf = null;

    private int volAnt;

    JConfiguracao configuracao;

    private final Carregador carregador;

    public JPrincipal(Musiquera mus, Carregador carregador) {
        initComponents();
        configuracao = new JConfiguracao(this, false);
        this.carregador = carregador;
        this.setIconImage(new ImageIcon(getClass().getResource("/com/img/icon.png")).getImage());
        jButton_Play.setName("jButton_Play");
        jButton_Next.setName("jButton_Next");
        jButton_Ant.setName("jButton_Ant");
        jButton_Stop.setName("jButton_Stop");
        jToggle_Repeat.setName("jToggle_Repeat");
        jToggle_Random.setName("jToggle_Random");
        musiquera = mus;
        inicializaIcones();
    }

    public Musiquera getMusiquera() {
        return musiquera;
    }

    @Override
    public void tempoEh(double v) {
        if (ajusteDeTempo) {
            return;
        }
        jSlider_Tempo.setValue((int) (jSlider_Tempo.getMaximum() * v));
    }

    @Override
    public void tempoEhHMS(String hms) {
        jLabel_tempo.setText(hms);
        jSlider_Tempo.setToolTipText(hms);
    }

    @Override
    public void propriedadesMusicaChanged(PropriedadesMusica propriedadesMusica) {
    }

    @Override
    public void eventoNaMusica(int tipo) {
        switch(tipo) {
            case BasicPlayerEvent.PAUSED:
                break;
            case BasicPlayerEvent.PLAYING:
            case BasicPlayerEvent.RESUMED:
                break;
            case BasicPlayerEvent.STOPPED:
                break;
            default:
                Warning.print("Evento desconhecio. Id: " + tipo);
                break;
        }
    }

    /** Atualiza labels da tela principal
     * @param nome Nome da musica, autor e album
     * @param tempo Tempo em Minutos e Seguntos
     * @param bits KiloBits/s
     * @param freq Frequencia em ?Hz
     */
    @Override
    public void atualizaLabels(String nome, int bits, String tempo, int freq) {
        jLabel_Musica.setText(nome.replaceAll("  ", " ").trim());
        jLabel_tempoTotal.setText(tempo);
        jLabel_bit.setText(bits + " kbps");
        jLabel_freq.setText(freq + " KHz");
    }

    /**
     * Muda o icones do label que contenha o nome indicado.
     * @param quem nome do label.
     * @param icones nome icones.nomeDoIcone
     */
    public void atualizaIcone(String quem, Icon _icone) {
        atualizaIcone(this.getContentPane(), quem, _icone);
    }

    public void atualizaIcone(String quem, String texto) {
        atualizaIcone(this.getContentPane(), quem, texto);
    }

    public void atualizaIcone(Container root, String quem, Icon _icone) {
        for (int i = 0; i < root.getComponentCount(); i++) {
            if (root.getComponent(i) instanceof JLabel) {
                if (root.getComponent(i).getName() != null && root.getComponent(i).getName().equals(quem)) {
                    ((JLabel) root.getComponent(i)).setIcon(_icone);
                    return;
                }
            } else {
                if (root.getComponent(i) instanceof JPanel) {
                    atualizaIcone((JPanel) root.getComponent(i), quem, _icone);
                }
            }
        }
    }

    /**
     * Muda o ToolTipText do label que contenha o nome indicado.
     * @param quem nome do label.
     * @param texto texto a ser colocado como tooltip
     */
    public void atualizaIcone(Container root, String quem, String texto) {
        for (int i = 0; i < root.getComponentCount(); i++) {
            if (root.getComponent(i) instanceof JLabel) {
                if (root.getComponent(i).getName() != null && root.getComponent(i).getName().equals(quem)) {
                    ((JLabel) root.getComponent(i)).setToolTipText(texto);
                    return;
                }
            } else {
                if (root.getComponent(i) instanceof JPanel) {
                    atualizaIcone((JPanel) root.getComponent(i), quem, texto);
                }
            }
        }
    }

    public void setVolume(int v) {
        jSlider_vol.setValue(v);
        jSlider_vol.setToolTipText(jSlider_vol.getValue() + "%");
    }

    public void setBalaco(int b) {
        jSlider_Balanco.setValue(b);
    }

    public String miliSegundosEmMinSeq(long mili) {
        SimpleDateFormat sdf = new SimpleDateFormat("ss");
        Date date = null;
        try {
            date = sdf.parse(String.valueOf(mili / 1000000));
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return new java.text.SimpleDateFormat("HH:mm:ss").format(date);
    }

    public File telaAbrirArquivo() throws Exception {
        jFileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        jFileChooser.setDialogTitle("Abrir Arquivo");
        int res = jFileChooser.showOpenDialog(null);
        if (res == JFileChooser.APPROVE_OPTION) {
            return jFileChooser.getSelectedFile();
        }
        return null;
    }

    private void inicializaIcones() {
        jButton_Play.setIcon(carregador.icones.playIcon32);
        if (musiquera.isPlaying()) {
            jButton_Play.setIcon(carregador.icones.pauseIcon32);
        }
        if (musiquera.isPlaying()) {
            jButton_Play.setIcon(carregador.icones.playIcon32);
        }
        jButton_Stop.setIcon(carregador.icones.stopIcon32);
        jButton_Next.setIcon(carregador.icones.frenteIcon32);
        jButton_Ant.setIcon(carregador.icones.voltaIcon32);
        if (carregador.isRandom()) {
            jToggle_Random.setIcon(carregador.icones.randomOnIcon32);
        } else {
            jToggle_Random.setIcon(carregador.icones.randomOffIcon32);
        }
        if (carregador.isRepeat()) {
            jToggle_Repeat.setIcon(carregador.icones.repeatOnIcon32);
        } else {
            jToggle_Repeat.setIcon(carregador.icones.repeatOffIcon32);
        }
    }

    @Override
    public synchronized void setState(int state) {
        super.setState(state);
    }

    public ImageIcon resizeIcons(BufferedImage im) {
        return new ImageIcon(im.getScaledInstance(17, 17, Image.SCALE_SMOOTH));
    }

    public ImageIcon resizeIcons(BufferedImage im, int l, int a) {
        return new ImageIcon(im.getScaledInstance(l, a, Image.SCALE_SMOOTH));
    }

    @Override
    public void onHotKey(int aIdentifier) {
    }

    @Override
    public void onIntellitype(int aCommand) {
        switch(aCommand) {
            case JIntellitype.APPCOMMAND_MEDIA_NEXTTRACK:
                musiquera.abrir(musiquera.getNextMusica(), 0, false);
                break;
            case JIntellitype.APPCOMMAND_MEDIA_PLAY_PAUSE:
                musiquera.tocarPausar();
                break;
            case JIntellitype.APPCOMMAND_MEDIA_PREVIOUSTRACK:
                musiquera.abrir(musiquera.getPreviousMusica(), 0, false);
                break;
            case JIntellitype.APPCOMMAND_MEDIA_STOP:
                musiquera.parar();
                break;
            case JIntellitype.APPCOMMAND_VOLUME_DOWN:
                jSlider_vol.setValue(jSlider_vol.getValue() - 2);
                break;
            case JIntellitype.APPCOMMAND_VOLUME_UP:
                jSlider_vol.setValue(jSlider_vol.getValue() + 2);
                break;
            case JIntellitype.APPCOMMAND_VOLUME_MUTE:
                if (jSlider_vol.getValue() > 0) {
                    volAnt = jSlider_vol.getValue();
                    jSlider_vol.setValue(0);
                } else {
                    jSlider_vol.setValue(volAnt);
                }
                break;
        }
    }

    public void initJIntellitype() {
        try {
            JIntellitype.getInstance().addHotKeyListener(this);
            JIntellitype.getInstance().addIntellitypeListener(this);
        } catch (RuntimeException ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void setVisible(boolean b) {
        super.setVisible(b);
        if (carregador.isRandom()) {
            jToggle_Random.setIcon(carregador.icones.randomOnIcon32);
        } else {
            jToggle_Random.setIcon(carregador.icones.randomOffIcon32);
        }
        if (carregador.isRepeat()) {
            jToggle_Repeat.setIcon(carregador.icones.repeatOnIcon32);
        } else {
            jToggle_Repeat.setIcon(carregador.icones.repeatOffIcon32);
        }
        jSlider_vol.setValue(musiquera.getVolume());
    }

    private void vouParaOnde(MouseEvent e) {
        estado++;
        if (estado == 5) {
            this.setLocation(e.getXOnScreen() - initX, e.getYOnScreen() - initY);
            estado = 0;
        }
    }

    private void ondeEstou(MouseEvent e) {
        initX = e.getXOnScreen() - this.getX();
        initY = e.getYOnScreen() - this.getY();
        thisX = this.getX();
        thisY = this.getY();
    }

    public int getSliderValue() {
        return jSlider_vol.getValue();
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    private void initComponents() {
        jMenuDeContexto = new javax.swing.JPopupMenu();
        jCMenuReproduz = new javax.swing.JMenu();
        jCIMenuPlay = new javax.swing.JMenuItem();
        jCIMenuStop = new javax.swing.JMenuItem();
        jCMenuVisual = new javax.swing.JMenu();
        jCCheckBarraTitulos = new javax.swing.JCheckBoxMenuItem();
        jCCheckBarraDeMenus = new javax.swing.JCheckBoxMenuItem();
        jCIMenuMinimizar = new javax.swing.JMenuItem();
        jCIMenuFechar = new javax.swing.JMenuItem();
        GrupoSpiner = new javax.swing.ButtonGroup();
        jPanel17 = new javax.swing.JPanel();
        jLabel_bib = new javax.swing.JLabel();
        jLabel_Playlist = new javax.swing.JLabel();
        jLabelFilaReproducao = new javax.swing.JLabel();
        jLabel_Edit = new javax.swing.JLabel();
        jLabel_Minimizar = new javax.swing.JLabel();
        jLabelHelp = new javax.swing.JLabel();
        jPanel1 = new javax.swing.JPanel();
        jPanel3 = new javax.swing.JPanel();
        jPanel15 = new javax.swing.JPanel();
        jLabel_Musica = new javax.swing.JLabel();
        jPanel14 = new javax.swing.JPanel();
        jPanel5 = new javax.swing.JPanel();
        jPanel11 = new javax.swing.JPanel();
        jLabel_bit = new javax.swing.JLabel();
        jPanel12 = new javax.swing.JPanel();
        jLabel_freq = new javax.swing.JLabel();
        jPanel13 = new javax.swing.JPanel();
        jPanel6 = new javax.swing.JPanel();
        jPanel7 = new javax.swing.JPanel();
        jPanel10 = new javax.swing.JPanel();
        jLabel_tempoTotal = new javax.swing.JLabel();
        jPanel8 = new javax.swing.JPanel();
        jLabel_tempo = new javax.swing.JLabel();
        jPanel9 = new javax.swing.JPanel();
        jPanel4 = new javax.swing.JPanel();
        jSlider_Tempo = new javax.swing.JSlider();
        jPanel2 = new javax.swing.JPanel();
        jButton_Play = new javax.swing.JLabel();
        jButton_Stop = new javax.swing.JLabel();
        jButton_Ant = new javax.swing.JLabel();
        jButton_Next = new javax.swing.JLabel();
        jToggle_Random = new javax.swing.JLabel();
        jToggle_Repeat = new javax.swing.JLabel();
        jSlider_vol = new javax.swing.JSlider();
        jPanel16 = new javax.swing.JPanel();
        jSlider_Balanco = new javax.swing.JSlider();
        jMenuBar1 = new javax.swing.JMenuBar();
        jMenu1 = new javax.swing.JMenu();
        jMenuItem_Arquivo = new javax.swing.JMenuItem();
        jMenuItem_Arquivo1 = new javax.swing.JMenuItem();
        jMenuItem3 = new javax.swing.JMenuItem();
        jMenu2 = new javax.swing.JMenu();
        jMenuItem1 = new javax.swing.JMenuItem();
        jMenuItem4 = new javax.swing.JMenuItem();
        jMenuItem2 = new javax.swing.JMenuItem();
        jMenuItem8 = new javax.swing.JMenuItem();
        jMenu3 = new javax.swing.JMenu();
        jMenuItem6 = new javax.swing.JMenuItem();
        jMenuItem5 = new javax.swing.JMenuItem();
        jMenuItem7 = new javax.swing.JMenuItem();
        jCMenuReproduz.setText("Reprodu��o");
        jCIMenuPlay.setText("Tocar");
        jCIMenuPlay.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCIMenuPlayActionPerformed(evt);
            }
        });
        jCMenuReproduz.add(jCIMenuPlay);
        jCIMenuStop.setText("Parar");
        jCIMenuStop.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCIMenuStopActionPerformed(evt);
            }
        });
        jCMenuReproduz.add(jCIMenuStop);
        jMenuDeContexto.add(jCMenuReproduz);
        jCMenuVisual.setText("Visualiza��o");
        jCCheckBarraTitulos.setSelected(true);
        jCCheckBarraTitulos.setText("Mostrar barra de titulos");
        jCCheckBarraTitulos.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCCheckBarraTitulosActionPerformed(evt);
            }
        });
        jCMenuVisual.add(jCCheckBarraTitulos);
        jCCheckBarraDeMenus.setSelected(true);
        jCCheckBarraDeMenus.setText("Mostrar barra de menus");
        jCCheckBarraDeMenus.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCCheckBarraDeMenusActionPerformed(evt);
            }
        });
        jCMenuVisual.add(jCCheckBarraDeMenus);
        jMenuDeContexto.add(jCMenuVisual);
        jCIMenuMinimizar.setText("Minimizar");
        jCIMenuMinimizar.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCIMenuMinimizarActionPerformed(evt);
            }
        });
        jMenuDeContexto.add(jCIMenuMinimizar);
        jCIMenuFechar.setText("Sair");
        jCIMenuFechar.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCIMenuFecharActionPerformed(evt);
            }
        });
        jMenuDeContexto.add(jCIMenuFechar);
        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("Crepz Player 1.0");
        setBackground(new java.awt.Color(255, 255, 255));
        setResizable(false);
        addWindowListener(new java.awt.event.WindowAdapter() {

            public void windowClosing(java.awt.event.WindowEvent evt) {
                formWindowClosing(evt);
            }
        });
        addWindowStateListener(new java.awt.event.WindowStateListener() {

            public void windowStateChanged(java.awt.event.WindowEvent evt) {
                formWindowStateChanged(evt);
            }
        });
        jPanel17.setBackground(new java.awt.Color(255, 255, 255));
        jPanel17.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 5, 0));
        jLabel_bib.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/img/biblioteca.png")));
        jLabel_bib.setToolTipText("Biblioteca");
        jLabel_bib.addMouseListener(new java.awt.event.MouseAdapter() {

            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jLabel_bibMouseClicked(evt);
            }
        });
        jPanel17.add(jLabel_bib);
        jLabel_Playlist.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/img/playlist.gif")));
        jLabel_Playlist.setToolTipText("Playlist");
        jLabel_Playlist.addMouseListener(new java.awt.event.MouseAdapter() {

            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jLabel_PlaylistMouseClicked(evt);
            }
        });
        jPanel17.add(jLabel_Playlist);
        jLabelFilaReproducao.setText("Fila");
        jLabelFilaReproducao.addMouseListener(new java.awt.event.MouseAdapter() {

            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jLabelFilaReproducaoMouseClicked(evt);
            }
        });
        jPanel17.add(jLabelFilaReproducao);
        jLabel_Edit.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/img/edit.png")));
        jLabel_Edit.setToolTipText("Edit Propriedades MP3");
        jLabel_Edit.addMouseListener(new java.awt.event.MouseAdapter() {

            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jLabel_EditMouseClicked(evt);
            }
        });
        jPanel17.add(jLabel_Edit);
        jLabel_Minimizar.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/img/icon1616.png")));
        jLabel_Minimizar.setToolTipText("Minimizar");
        jLabel_Minimizar.addMouseListener(new java.awt.event.MouseAdapter() {

            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jLabel_MinimizarMouseClicked(evt);
            }
        });
        jPanel17.add(jLabel_Minimizar);
        jLabelHelp.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/help/img/help.PNG")));
        jLabelHelp.addMouseListener(new java.awt.event.MouseAdapter() {

            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jLabelHelpMouseClicked(evt);
            }
        });
        jPanel17.add(jLabelHelp);
        getContentPane().add(jPanel17, java.awt.BorderLayout.PAGE_START);
        jPanel1.setBackground(new java.awt.Color(255, 255, 255));
        jPanel1.setPreferredSize(new java.awt.Dimension(375, 130));
        jPanel1.addMouseWheelListener(new java.awt.event.MouseWheelListener() {

            public void mouseWheelMoved(java.awt.event.MouseWheelEvent evt) {
                jPanel1MouseWheelMoved(evt);
            }
        });
        jPanel1.addMouseListener(new java.awt.event.MouseAdapter() {

            public void mousePressed(java.awt.event.MouseEvent evt) {
                jPanel1MousePressed(evt);
            }

            public void mouseReleased(java.awt.event.MouseEvent evt) {
                jPanel1MouseReleased(evt);
            }
        });
        jPanel1.addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {

            public void mouseDragged(java.awt.event.MouseEvent evt) {
                jPanel1MouseDragged(evt);
            }
        });
        jPanel1.setLayout(new javax.swing.BoxLayout(jPanel1, javax.swing.BoxLayout.Y_AXIS));
        jPanel3.setBackground(new java.awt.Color(255, 255, 255));
        jPanel3.setLayout(new javax.swing.BoxLayout(jPanel3, javax.swing.BoxLayout.Y_AXIS));
        jPanel15.setBackground(new java.awt.Color(255, 255, 255));
        jPanel15.setPreferredSize(new java.awt.Dimension(397, 30));
        jPanel15.add(jLabel_Musica);
        jPanel3.add(jPanel15);
        jPanel14.setBackground(new java.awt.Color(255, 255, 255));
        jPanel14.setLayout(new javax.swing.BoxLayout(jPanel14, javax.swing.BoxLayout.X_AXIS));
        jPanel5.setBackground(new java.awt.Color(255, 255, 255));
        jPanel5.setPreferredSize(new java.awt.Dimension(132, 100));
        jPanel5.setLayout(new javax.swing.BoxLayout(jPanel5, javax.swing.BoxLayout.Y_AXIS));
        jPanel11.setBackground(new java.awt.Color(255, 255, 255));
        jPanel11.add(jLabel_bit);
        jPanel5.add(jPanel11);
        jPanel12.setBackground(new java.awt.Color(255, 255, 255));
        jPanel12.add(jLabel_freq);
        jPanel5.add(jPanel12);
        jPanel13.setBackground(new java.awt.Color(255, 255, 255));
        jPanel13.setLayout(new java.awt.BorderLayout());
        jPanel5.add(jPanel13);
        jPanel14.add(jPanel5);
        jPanel6.setBackground(new java.awt.Color(255, 255, 255));
        jPanel6.setPreferredSize(new java.awt.Dimension(132, 120));
        jPanel6.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.CENTER, 0, 0));
        jPanel14.add(jPanel6);
        jPanel7.setLayout(new javax.swing.BoxLayout(jPanel7, javax.swing.BoxLayout.Y_AXIS));
        jPanel10.setBackground(new java.awt.Color(255, 255, 255));
        jLabel_tempoTotal.setText("0:00");
        jPanel10.add(jLabel_tempoTotal);
        jPanel7.add(jPanel10);
        jPanel8.setBackground(new java.awt.Color(255, 255, 255));
        jLabel_tempo.setText("0:00");
        jPanel8.add(jLabel_tempo);
        jPanel7.add(jPanel8);
        jPanel9.setBackground(new java.awt.Color(255, 255, 255));
        jPanel9.setLayout(new java.awt.BorderLayout());
        jPanel7.add(jPanel9);
        jPanel14.add(jPanel7);
        jPanel3.add(jPanel14);
        jPanel1.add(jPanel3);
        jPanel4.setBackground(new java.awt.Color(255, 255, 255));
        jPanel4.setLayout(new java.awt.BorderLayout());
        jSlider_Tempo.setBackground(new java.awt.Color(255, 255, 255));
        jSlider_Tempo.setMaximum(1000);
        jSlider_Tempo.setToolTipText("0:00");
        jSlider_Tempo.setValue(0);
        jSlider_Tempo.addMouseListener(new java.awt.event.MouseAdapter() {

            public void mousePressed(java.awt.event.MouseEvent evt) {
                jSlider_TempoMousePressed(evt);
            }

            public void mouseReleased(java.awt.event.MouseEvent evt) {
                jSlider_TempoMouseReleased(evt);
            }
        });
        jPanel4.add(jSlider_Tempo, java.awt.BorderLayout.CENTER);
        jPanel1.add(jPanel4);
        getContentPane().add(jPanel1, java.awt.BorderLayout.CENTER);
        jPanel2.setBackground(new java.awt.Color(255, 255, 255));
        jPanel2.setMinimumSize(new java.awt.Dimension(248, 35));
        jPanel2.addMouseListener(new java.awt.event.MouseAdapter() {

            public void mousePressed(java.awt.event.MouseEvent evt) {
                jPanel2MousePressed(evt);
            }

            public void mouseReleased(java.awt.event.MouseEvent evt) {
                jPanel2MouseReleased(evt);
            }
        });
        jPanel2.addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {

            public void mouseDragged(java.awt.event.MouseEvent evt) {
                jPanel2MouseDragged(evt);
            }
        });
        jPanel2.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.CENTER, 0, 0));
        jButton_Play.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/img/icons/tipo2/play.png")));
        jButton_Play.addMouseListener(new java.awt.event.MouseAdapter() {

            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jButton_PlayMouseClicked(evt);
            }

            public void mouseEntered(java.awt.event.MouseEvent evt) {
                jButton_PlayMouseEntered(evt);
            }

            public void mouseExited(java.awt.event.MouseEvent evt) {
                jButton_PlayMouseExited(evt);
            }
        });
        jPanel2.add(jButton_Play);
        jButton_Stop.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/img/icons/tipo2/stop.png")));
        jButton_Stop.addMouseListener(new java.awt.event.MouseAdapter() {

            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jButton_StopMouseClicked(evt);
            }

            public void mouseEntered(java.awt.event.MouseEvent evt) {
                jButton_StopMouseEntered(evt);
            }

            public void mouseExited(java.awt.event.MouseEvent evt) {
                jButton_StopMouseExited(evt);
            }
        });
        jPanel2.add(jButton_Stop);
        jButton_Ant.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/img/icons/tipo2/tras.png")));
        jButton_Ant.addMouseListener(new java.awt.event.MouseAdapter() {

            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jButton_AntMouseClicked(evt);
            }

            public void mouseEntered(java.awt.event.MouseEvent evt) {
                jButton_AntMouseEntered(evt);
            }

            public void mouseExited(java.awt.event.MouseEvent evt) {
                jButton_AntMouseExited(evt);
            }
        });
        jPanel2.add(jButton_Ant);
        jButton_Next.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/img/icons/tipo2/frente.png")));
        jButton_Next.addMouseListener(new java.awt.event.MouseAdapter() {

            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jButton_NextMouseClicked(evt);
            }

            public void mouseEntered(java.awt.event.MouseEvent evt) {
                jButton_NextMouseEntered(evt);
            }

            public void mouseExited(java.awt.event.MouseEvent evt) {
                jButton_NextMouseExited(evt);
            }
        });
        jPanel2.add(jButton_Next);
        jToggle_Random.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/img/icons/tipo2/falseRandom.png")));
        jToggle_Random.addMouseListener(new java.awt.event.MouseAdapter() {

            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jToggle_RandomMouseClicked(evt);
            }

            public void mouseEntered(java.awt.event.MouseEvent evt) {
                jToggle_RandomMouseEntered(evt);
            }

            public void mouseExited(java.awt.event.MouseEvent evt) {
                jToggle_RandomMouseExited(evt);
            }
        });
        jPanel2.add(jToggle_Random);
        jToggle_Repeat.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/img/icons/tipo2/repeatOff.png")));
        jToggle_Repeat.addMouseListener(new java.awt.event.MouseAdapter() {

            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jToggle_RepeatMouseClicked(evt);
            }

            public void mouseEntered(java.awt.event.MouseEvent evt) {
                jToggle_RepeatMouseEntered(evt);
            }

            public void mouseExited(java.awt.event.MouseEvent evt) {
                jToggle_RepeatMouseExited(evt);
            }
        });
        jPanel2.add(jToggle_Repeat);
        jSlider_vol.setBackground(new java.awt.Color(255, 255, 255));
        jSlider_vol.setToolTipText("Volume");
        jSlider_vol.setPreferredSize(new java.awt.Dimension(100, 23));
        jSlider_vol.addMouseWheelListener(new java.awt.event.MouseWheelListener() {

            public void mouseWheelMoved(java.awt.event.MouseWheelEvent evt) {
                jSlider_volMouseWheelMoved(evt);
            }
        });
        jSlider_vol.addChangeListener(new javax.swing.event.ChangeListener() {

            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                jSlider_volStateChanged(evt);
            }
        });
        jPanel2.add(jSlider_vol);
        jPanel16.setBackground(new java.awt.Color(255, 255, 255));
        jPanel16.setMaximumSize(new java.awt.Dimension(50, 2147483647));
        jPanel16.setMinimumSize(new java.awt.Dimension(20, 24));
        jPanel16.setPreferredSize(new java.awt.Dimension(50, 20));
        jPanel16.setLayout(new java.awt.BorderLayout());
        jSlider_Balanco.setBackground(new java.awt.Color(255, 255, 255));
        jSlider_Balanco.setMinimum(-100);
        jSlider_Balanco.setToolTipText("balan�o");
        jSlider_Balanco.setValue(0);
        jSlider_Balanco.addMouseWheelListener(new java.awt.event.MouseWheelListener() {

            public void mouseWheelMoved(java.awt.event.MouseWheelEvent evt) {
                jSlider_BalancoMouseWheelMoved(evt);
            }
        });
        jSlider_Balanco.addChangeListener(new javax.swing.event.ChangeListener() {

            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                jSlider_BalancoStateChanged(evt);
            }
        });
        jPanel16.add(jSlider_Balanco, java.awt.BorderLayout.CENTER);
        jPanel2.add(jPanel16);
        getContentPane().add(jPanel2, java.awt.BorderLayout.PAGE_END);
        jMenuBar1.addMouseListener(new java.awt.event.MouseAdapter() {

            public void mousePressed(java.awt.event.MouseEvent evt) {
                jMenuBar1MousePressed(evt);
            }
        });
        jMenuBar1.addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {

            public void mouseDragged(java.awt.event.MouseEvent evt) {
                jMenuBar1MouseDragged(evt);
            }
        });
        jMenu1.setText("Arquivo");
        jMenuItem_Arquivo.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_A, 0));
        jMenuItem_Arquivo.setText("Arquivo");
        jMenuItem_Arquivo.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem_ArquivoActionPerformed(evt);
            }
        });
        jMenu1.add(jMenuItem_Arquivo);
        jMenuItem_Arquivo1.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_B, 0));
        jMenuItem_Arquivo1.setText("Biblioteca");
        jMenuItem_Arquivo1.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem_Arquivo1ActionPerformed(evt);
            }
        });
        jMenu1.add(jMenuItem_Arquivo1);
        jMenuItem3.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F4, 0));
        jMenuItem3.setText("Minimizar");
        jMenuItem3.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem3ActionPerformed(evt);
            }
        });
        jMenu1.add(jMenuItem3);
        jMenuBar1.add(jMenu1);
        jMenu2.setText("Editar");
        jMenuItem1.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_P, 0));
        jMenuItem1.setText("Propriedades");
        jMenuItem1.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem1ActionPerformed(evt);
            }
        });
        jMenu2.add(jMenuItem1);
        jMenuItem4.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_L, 0));
        jMenuItem4.setText("PlayList");
        jMenuItem4.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem4ActionPerformed(evt);
            }
        });
        jMenu2.add(jMenuItem4);
        jMenuItem2.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_SPACE, 0));
        jMenuItem2.setText("Play");
        jMenuItem2.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem2ActionPerformed(evt);
            }
        });
        jMenu2.add(jMenuItem2);
        jMenuItem8.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_C, 0));
        jMenuItem8.setText("Configura��es");
        jMenuItem8.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem8ActionPerformed(evt);
            }
        });
        jMenu2.add(jMenuItem8);
        jMenuBar1.add(jMenu2);
        jMenu3.setText("Sobre");
        jMenuItem6.setText("Sobre");
        jMenuItem6.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem6ActionPerformed(evt);
            }
        });
        jMenu3.add(jMenuItem6);
        jMenuItem5.setText("Help");
        jMenuItem5.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem5ActionPerformed(evt);
            }
        });
        jMenu3.add(jMenuItem5);
        jMenuItem7.setText("Restaurar Configura��o Original");
        jMenuItem7.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem7ActionPerformed(evt);
            }
        });
        jMenu3.add(jMenuItem7);
        jMenuBar1.add(jMenu3);
        setJMenuBar(jMenuBar1);
        java.awt.Dimension screenSize = java.awt.Toolkit.getDefaultToolkit().getScreenSize();
        setBounds((screenSize.width - 398) / 2, (screenSize.height - 231) / 2, 398, 231);
    }

    private void jSlider_TempoMouseReleased(java.awt.event.MouseEvent evt) {
        musiquera.skipTo((double) (jSlider_Tempo.getValue()) / jSlider_Tempo.getMaximum());
        ajusteDeTempo = false;
    }

    private void jMenuItem_ArquivoActionPerformed(java.awt.event.ActionEvent evt) {
        try {
            musiquera.abrir(telaAbrirArquivo());
        } catch (Exception ex) {
            Logger.getLogger(JPrincipal.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void jSlider_volMouseWheelMoved(java.awt.event.MouseWheelEvent evt) {
        jSlider_vol.setValue(jSlider_vol.getValue() - evt.getWheelRotation());
        musiquera.setVolume((byte) jSlider_vol.getValue());
        jSlider_vol.setToolTipText(jSlider_vol.getValue() + "%");
    }

    private void jSlider_TempoMousePressed(java.awt.event.MouseEvent evt) {
        ajusteDeTempo = true;
    }

    private void jPanel1MousePressed(java.awt.event.MouseEvent evt) {
        ondeEstou(evt);
    }

    private void jPanel1MouseDragged(java.awt.event.MouseEvent evt) {
        vouParaOnde(evt);
    }

    private void jMenuBar1MousePressed(java.awt.event.MouseEvent evt) {
        ondeEstou(evt);
    }

    private void jMenuBar1MouseDragged(java.awt.event.MouseEvent evt) {
        vouParaOnde(evt);
    }

    private void jCIMenuFecharActionPerformed(java.awt.event.ActionEvent evt) {
        carregador.sair();
    }

    private void jPanel2MousePressed(java.awt.event.MouseEvent evt) {
        ondeEstou(evt);
    }

    private void jPanel2MouseDragged(java.awt.event.MouseEvent evt) {
        vouParaOnde(evt);
    }

    private void jPanel1MouseReleased(java.awt.event.MouseEvent evt) {
        if (evt.getButton() == MouseEvent.BUTTON3 && thisX == this.getX() && thisY == this.getY()) {
            jMenuDeContexto.show(evt.getComponent(), evt.getX(), evt.getY());
        }
    }

    private void jCCheckBarraTitulosActionPerformed(java.awt.event.ActionEvent evt) {
        this.setVisible(false);
        this.dispose();
        this.setUndecorated(!jCCheckBarraTitulos.getState());
        this.setVisible(true);
    }

    private void jCIMenuPlayActionPerformed(java.awt.event.ActionEvent evt) {
        musiquera.tocarPausar();
    }

    private void jCIMenuStopActionPerformed(java.awt.event.ActionEvent evt) {
        musiquera.parar();
    }

    private void jCCheckBarraDeMenusActionPerformed(java.awt.event.ActionEvent evt) {
        this.dispose();
        jMenuBar1.setVisible(jCCheckBarraDeMenus.getState());
        this.setVisible(true);
    }

    private void jPanel2MouseReleased(java.awt.event.MouseEvent evt) {
        if (evt.getButton() == MouseEvent.BUTTON3 && thisX == this.getX() && thisY == this.getY()) {
            jMenuDeContexto.show(evt.getComponent(), evt.getX(), evt.getY());
        }
    }

    private void jSlider_BalancoStateChanged(javax.swing.event.ChangeEvent evt) {
        musiquera.setBalanco((byte) jSlider_Balanco.getValue());
        jSlider_Balanco.setToolTipText(String.valueOf(jSlider_Balanco.getValue() / 100));
    }

    private void jMenuItem2ActionPerformed(java.awt.event.ActionEvent evt) {
        musiquera.tocarPausar();
    }

    private void jMenuItem1ActionPerformed(java.awt.event.ActionEvent evt) {
        try {
            new JMP3Propriedades(this, true, new File(musiquera.getMusica().getCaminho())).setVisible(true);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Erro ao Abrir Propriedades.\n" + ex);
            ex.printStackTrace();
        }
    }

    private void jMenuItem3ActionPerformed(java.awt.event.ActionEvent evt) {
        carregador.setMiniComoBase();
    }

    private void jMenuItem_Arquivo1ActionPerformed(java.awt.event.ActionEvent evt) {
        carregador.mostrarBiblioteca();
    }

    private void jMenuItem4ActionPerformed(java.awt.event.ActionEvent evt) {
        carregador.mostrarPlayList();
    }

    private void jMenuItem5ActionPerformed(java.awt.event.ActionEvent evt) {
        new JHelp(this).setVisible(true);
    }

    private void jButton_PlayMouseClicked(java.awt.event.MouseEvent evt) {
        if (evt.getButton() == MouseEvent.BUTTON1) {
            musiquera.tocarPausar();
        }
    }

    private void jButton_StopMouseClicked(java.awt.event.MouseEvent evt) {
        if (evt.getButton() == MouseEvent.BUTTON1) {
            musiquera.parar();
        }
    }

    private void jButton_AntMouseClicked(java.awt.event.MouseEvent evt) {
        if (evt.getButton() == MouseEvent.BUTTON1) {
            musiquera.abrir(musiquera.getPreviousMusica(), 0, false);
        }
    }

    private void jButton_NextMouseClicked(java.awt.event.MouseEvent evt) {
        if (evt.getButton() == MouseEvent.BUTTON1) {
            musiquera.abrir(musiquera.getNextMusica(), 0, false);
        }
    }

    private void jToggle_RandomMouseClicked(java.awt.event.MouseEvent evt) {
        carregador.setRandom(!carregador.isRandom());
        if (carregador.isRandom()) {
            jToggle_Random.setIcon(carregador.icones.randomOnIcon32);
        } else {
            jToggle_Random.setIcon(carregador.icones.randomOffIcon32);
        }
    }

    private void jSlider_BalancoMouseWheelMoved(java.awt.event.MouseWheelEvent evt) {
        jSlider_Balanco.setValue(jSlider_Balanco.getValue() - evt.getWheelRotation());
    }

    private void jPanel1MouseWheelMoved(java.awt.event.MouseWheelEvent evt) {
    }

    private void jButton_PlayMouseEntered(java.awt.event.MouseEvent evt) {
    }

    private void jButton_PlayMouseExited(java.awt.event.MouseEvent evt) {
        evt.getComponent().repaint();
    }

    private void jButton_StopMouseEntered(java.awt.event.MouseEvent evt) {
    }

    private void jButton_StopMouseExited(java.awt.event.MouseEvent evt) {
        evt.getComponent().repaint();
    }

    private void jButton_AntMouseEntered(java.awt.event.MouseEvent evt) {
    }

    private void jButton_AntMouseExited(java.awt.event.MouseEvent evt) {
        evt.getComponent().repaint();
    }

    private void jButton_NextMouseEntered(java.awt.event.MouseEvent evt) {
    }

    private void jButton_NextMouseExited(java.awt.event.MouseEvent evt) {
        evt.getComponent().repaint();
    }

    private void jToggle_RandomMouseEntered(java.awt.event.MouseEvent evt) {
    }

    private void jToggle_RandomMouseExited(java.awt.event.MouseEvent evt) {
        evt.getComponent().repaint();
    }

    private void jToggle_RepeatMouseClicked(java.awt.event.MouseEvent evt) {
        carregador.setRepeat(!carregador.isRepeat());
        if (carregador.isRepeat()) {
            jToggle_Repeat.setIcon(carregador.icones.repeatOnIcon32);
        } else {
            jToggle_Repeat.setIcon(carregador.icones.repeatOffIcon32);
        }
    }

    private void jToggle_RepeatMouseEntered(java.awt.event.MouseEvent evt) {
    }

    private void jToggle_RepeatMouseExited(java.awt.event.MouseEvent evt) {
        evt.getComponent().repaint();
    }

    private void jCIMenuMinimizarActionPerformed(java.awt.event.ActionEvent evt) {
        carregador.setMiniComoBase();
    }

    private void formWindowStateChanged(java.awt.event.WindowEvent evt) {
        if (evt.getNewState() != NORMAL) {
            carregador.setMiniComoBase();
        }
    }

    private void formWindowClosing(java.awt.event.WindowEvent evt) {
        carregador.sair();
    }

    private void jLabel_bibMouseClicked(java.awt.event.MouseEvent evt) {
        carregador.mostrarBiblioteca();
    }

    private void jLabel_PlaylistMouseClicked(java.awt.event.MouseEvent evt) {
        carregador.mostrarPlayList();
    }

    private void jLabel_EditMouseClicked(java.awt.event.MouseEvent evt) {
        try {
            new JMP3Propriedades(this, true, new File(musiquera.getMusica().getCaminho())).setVisible(true);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Erro ao Abrir Propriedades.\n" + ex);
            ex.printStackTrace();
        }
    }

    private void jLabel_MinimizarMouseClicked(java.awt.event.MouseEvent evt) {
        carregador.setMiniComoBase();
    }

    private void jMenuItem6ActionPerformed(java.awt.event.ActionEvent evt) {
        new JSobre(this).setVisible(true);
    }

    private void jLabelHelpMouseClicked(java.awt.event.MouseEvent evt) {
        if (evt.getButton() == MouseEvent.BUTTON1) {
            new JHelp(this).setVisible(true);
        }
    }

    private void jMenuItem7ActionPerformed(java.awt.event.ActionEvent evt) {
        if (JOptionPane.showConfirmDialog(this, "Isso limpar� a biblioteca e a playlist.\nO Crepz Player ser� fechado.\n Est� certo disso ??") == JOptionPane.YES_OPTION) {
            try {
                BD.hadukem();
                super.setVisible(false);
                System.exit(0);
            } catch (Exception ex) {
                Logger.getLogger(JPrincipal.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    private void jMenuItem8ActionPerformed(java.awt.event.ActionEvent evt) {
        configuracao.setVisible(true);
    }

    private void jSlider_volStateChanged(javax.swing.event.ChangeEvent evt) {
        musiquera.setVolume((byte) jSlider_vol.getValue());
        jSlider_vol.setToolTipText(jSlider_vol.getValue() + "%");
    }

    private void jLabelFilaReproducaoMouseClicked(java.awt.event.MouseEvent evt) {
        if (carregador.isFilaReproducaoVisivel()) {
            carregador.ocultarFilaReproducao();
        } else {
            carregador.mostrarFilaReproducao();
        }
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) throws ParseException {
    }

    private javax.swing.ButtonGroup GrupoSpiner;

    private javax.swing.JLabel jButton_Ant;

    private javax.swing.JLabel jButton_Next;

    private javax.swing.JLabel jButton_Play;

    private javax.swing.JLabel jButton_Stop;

    private javax.swing.JCheckBoxMenuItem jCCheckBarraDeMenus;

    private javax.swing.JCheckBoxMenuItem jCCheckBarraTitulos;

    private javax.swing.JMenuItem jCIMenuFechar;

    private javax.swing.JMenuItem jCIMenuMinimizar;

    private javax.swing.JMenuItem jCIMenuPlay;

    private javax.swing.JMenuItem jCIMenuStop;

    private javax.swing.JMenu jCMenuReproduz;

    private javax.swing.JMenu jCMenuVisual;

    private javax.swing.JLabel jLabelFilaReproducao;

    private javax.swing.JLabel jLabelHelp;

    private javax.swing.JLabel jLabel_Edit;

    private javax.swing.JLabel jLabel_Minimizar;

    private javax.swing.JLabel jLabel_Musica;

    private javax.swing.JLabel jLabel_Playlist;

    private javax.swing.JLabel jLabel_bib;

    private javax.swing.JLabel jLabel_bit;

    private javax.swing.JLabel jLabel_freq;

    private javax.swing.JLabel jLabel_tempo;

    private javax.swing.JLabel jLabel_tempoTotal;

    private javax.swing.JMenu jMenu1;

    private javax.swing.JMenu jMenu2;

    private javax.swing.JMenu jMenu3;

    private javax.swing.JMenuBar jMenuBar1;

    private javax.swing.JPopupMenu jMenuDeContexto;

    private javax.swing.JMenuItem jMenuItem1;

    private javax.swing.JMenuItem jMenuItem2;

    private javax.swing.JMenuItem jMenuItem3;

    private javax.swing.JMenuItem jMenuItem4;

    private javax.swing.JMenuItem jMenuItem5;

    private javax.swing.JMenuItem jMenuItem6;

    private javax.swing.JMenuItem jMenuItem7;

    private javax.swing.JMenuItem jMenuItem8;

    private javax.swing.JMenuItem jMenuItem_Arquivo;

    private javax.swing.JMenuItem jMenuItem_Arquivo1;

    private javax.swing.JPanel jPanel1;

    private javax.swing.JPanel jPanel10;

    private javax.swing.JPanel jPanel11;

    private javax.swing.JPanel jPanel12;

    private javax.swing.JPanel jPanel13;

    private javax.swing.JPanel jPanel14;

    private javax.swing.JPanel jPanel15;

    private javax.swing.JPanel jPanel16;

    private javax.swing.JPanel jPanel17;

    private javax.swing.JPanel jPanel2;

    private javax.swing.JPanel jPanel3;

    private javax.swing.JPanel jPanel4;

    private javax.swing.JPanel jPanel5;

    private javax.swing.JPanel jPanel6;

    private javax.swing.JPanel jPanel7;

    private javax.swing.JPanel jPanel8;

    private javax.swing.JPanel jPanel9;

    private javax.swing.JSlider jSlider_Balanco;

    private javax.swing.JSlider jSlider_Tempo;

    private javax.swing.JSlider jSlider_vol;

    private javax.swing.JLabel jToggle_Random;

    private javax.swing.JLabel jToggle_Repeat;

    private boolean ajusteDeTempo = false;

    int initX;

    int initY;

    int thisX;

    int thisY;
}
