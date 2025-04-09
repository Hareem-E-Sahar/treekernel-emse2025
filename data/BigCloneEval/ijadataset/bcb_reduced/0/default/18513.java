import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import org.jgraph.*;
import org.jgraph.event.GraphSelectionEvent;
import org.jgraph.event.GraphSelectionListener;
import java.awt.event.*;
import java.awt.*;
import java.io.File;
import java.util.LinkedList;

/**
 * Clase de la Interfaz Principal
 * 
 * @author Equipo 6 Cynthia Trevi�o, Ricardo Magallanes, Daniel Ramirez
 */
public class Interfaz extends JFrame implements ActionListener, GraphSelectionListener {

    private static final long serialVersionUID = 1039877813653006074L;

    private JMenuBar menuPrincipal;

    private JMenu archivo, simulacion, ayuda;

    private JMenuItem archivoNuevo, archivoAbrir, archivoGuardar, archivoCerrar, simulacionAgregar, simulacionBorrar, simulacionAtributos, ayudaInfo;

    private JList amigos;

    private JPanel pnlGrafo, pnlBusqueda, panelAttribs, pnlResultados;

    private JSplitPane panelSub, panelCentral, panelDer;

    private JButton search, blacklist, edit;

    private JTextField compatibilidad, niveles;

    private JLabel lblStatusBar;

    private Persona seleccionada = null;

    private JLabel seleccionada_nombre;

    private AttributesTable seleccionada_atributos;

    private DefaultListModel dlm = new DefaultListModel();

    public Interfaz() {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        this.setSize((int) screenSize.getWidth(), (int) (screenSize.getHeight() - 30));
        this.setTitle("Simulador de Redes Sociales");
        this.setLayout(new BorderLayout());
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setResizable(false);
        Container content = getContentPane();
        menuPrincipal = new JMenuBar();
        archivo = new JMenu("Archivo");
        archivoNuevo = new JMenuItem("Nueva Red");
        archivoAbrir = new JMenuItem("Abrir Red");
        archivoGuardar = new JMenuItem("Guardar Red");
        archivoCerrar = new JMenuItem("Cerrar Simulador");
        archivoNuevo.addActionListener(this);
        archivoAbrir.addActionListener(this);
        archivoGuardar.addActionListener(this);
        archivoCerrar.addActionListener(this);
        archivo.add(archivoNuevo);
        archivo.add(archivoAbrir);
        archivo.add(archivoGuardar);
        archivo.add(archivoCerrar);
        simulacion = new JMenu("Simulaci�n");
        simulacionAgregar = new JMenuItem("Agregar Persona");
        simulacionBorrar = new JMenuItem("Borrar Persona");
        simulacionAtributos = new JMenuItem("Administrar Atributos");
        simulacionAgregar.addActionListener(this);
        simulacion.add(simulacionAgregar);
        simulacion.add(simulacionBorrar);
        simulacion.add(simulacionAtributos);
        simulacionBorrar.addActionListener(this);
        simulacionAtributos.addActionListener(this);
        ayuda = new JMenu("Equipo");
        ayudaInfo = new JMenuItem("Acerca De");
        ayuda.add(ayudaInfo);
        ayudaInfo.addActionListener(this);
        menuPrincipal.add(archivo);
        menuPrincipal.add(simulacion);
        menuPrincipal.add(ayuda);
        panelCentral = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        panelDer = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        panelSub = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        amigos = new JList(dlm);
        amigos.setPreferredSize(new Dimension(120, 600));
        amigos.setVisibleRowCount(30);
        amigos.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        amigos.setFixedCellHeight(20);
        amigos.setCellRenderer(new CellRenderer());
        amigos.addMouseListener(new MouseListener() {

            public void mouseClicked(MouseEvent e) {
                try {
                    int index = amigos.locationToIndex(e.getPoint());
                    ListModel dlm = amigos.getModel();
                    Object item = dlm.getElementAt(index);
                    amigos.ensureIndexIsVisible(index);
                    if (e.getClickCount() == 1 && e.getButton() == e.BUTTON1) {
                        seleccionada = (Persona) item;
                        updatePropertiesPanel();
                    } else if (e.getClickCount() == 1 && e.getButton() == e.BUTTON3) {
                        DisplayAttributesFrame.getAttributesForm((Persona) item);
                    }
                } catch (Exception ex) {
                    lblStatusBar.setText("La red se encuentra vac�a");
                    lblStatusBar.setForeground(Color.red);
                }
            }

            public void mousePressed(MouseEvent arg0) {
            }

            public void mouseReleased(MouseEvent arg0) {
            }

            public void mouseEntered(MouseEvent arg0) {
            }

            public void mouseExited(MouseEvent arg0) {
            }
        });
        JScrollPane jsam = new JScrollPane(amigos);
        jsam.setPreferredSize(new Dimension(190, 600));
        jsam.setMinimumSize(new Dimension(190, 600));
        jsam.setMaximumSize(new Dimension(190, 600));
        panelAttribs = new JPanel(new BorderLayout());
        panelAttribs.setPreferredSize(new Dimension(150, 200));
        panelAttribs.setMinimumSize(new Dimension(150, 200));
        panelAttribs.setMaximumSize(new Dimension(150, 200));
        pnlGrafo = new JPanel();
        pnlGrafo.setPreferredSize(new Dimension(550, 600));
        pnlGrafo.setMinimumSize(new Dimension(550, 600));
        pnlGrafo.add(new JLabel());
        pnlBusqueda = new JPanel(new GridLayout(4, 2));
        pnlBusqueda.setPreferredSize(new Dimension(550, 200));
        pnlBusqueda.setMinimumSize(new Dimension(550, 200));
        this.buildSearchPanel();
        this.buildPropertiesPanel();
        pnlResultados = new JPanel(new BorderLayout());
        JTabbedPane jtp = new JTabbedPane();
        jtp.addTab("Busqueda", pnlBusqueda);
        jtp.addTab("Resultados", pnlResultados);
        panelSub.setLeftComponent(jtp);
        panelSub.setRightComponent(panelAttribs);
        panelDer.setLeftComponent(pnlGrafo);
        panelDer.setRightComponent(panelSub);
        panelCentral.setLeftComponent(jsam);
        panelCentral.setRightComponent(panelDer);
        this.setJMenuBar(menuPrincipal);
        content.add(panelCentral);
        lblStatusBar = new JLabel("Agregue o Seleccione una persona para comenzar. ");
        content.add(lblStatusBar, BorderLayout.SOUTH);
        setContentPane(content);
    }

    public void addElementToList(Persona p) {
        this.dlm.addElement(p);
    }

    public void removeElement(Persona p) {
        this.dlm.removeElement(p);
    }

    public void removeElements() {
        this.dlm.removeAllElements();
    }

    public void clear() {
        this.removeElements();
        this.seleccionada = null;
        this.updatePropertiesPanel();
    }

    public Dimension getGraphSize() {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int width = ((int) screenSize.getWidth()) - 240;
        return new Dimension(width, 400);
    }

    public void setGraphDisplay(JGraph g) {
        JScrollPane sc = new JScrollPane(g);
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int width = ((int) screenSize.getWidth()) - 440;
        this.panelDer.setLeftComponent(sc);
        g.setPreferredSize(new Dimension(width, 550));
        g.setMinimumSize(new Dimension(width, 550));
        sc.setPreferredSize(new Dimension(width, 550));
        sc.setMinimumSize(new Dimension(width, 550));
        g.addGraphSelectionListener(this);
        g.setAutoResizeGraph(true);
        g.setEditable(false);
        g.setEdgeLabelsMovable(false);
    }

    public void buildPropertiesPanel() {
        Object[][] data = { { "", "", "" } };
        seleccionada_nombre = new JLabel("Seleccione una persona");
        seleccionada_atributos = new AttributesTable();
        seleccionada_atributos.setData(data);
        JScrollPane tsp = new JScrollPane(seleccionada_atributos);
        tsp.setBounds(0, 10, 200, 150);
        panelAttribs.add(seleccionada_nombre, BorderLayout.NORTH);
        panelAttribs.add(tsp, BorderLayout.CENTER);
        blacklist = new JButton("Lista Negra");
        blacklist.addActionListener(this);
        edit = new JButton("Editar");
        edit.addActionListener(this);
        JPanel comp = new JPanel(new FlowLayout());
        comp.add(blacklist);
        comp.add(edit);
        panelAttribs.add(comp, BorderLayout.SOUTH);
    }

    public void updatePropertiesPanel() {
        if (this.seleccionada == null) {
            seleccionada_nombre.setText("Seleccione una persona");
            Object[][] data = { { "", "", "" } };
            seleccionada_atributos.setData(data);
            return;
        }
        seleccionada_nombre.setText("Atributos de la Persona: " + this.seleccionada.getNombre());
        Object[][] data = this.seleccionada.getAtributosTable();
        seleccionada_atributos.setData(data);
        seleccionada_atributos.repaint();
    }

    public void buildSearchPanel() {
        search = new JButton("Buscar");
        search.addActionListener(this);
        compatibilidad = new JTextField("40");
        niveles = new JTextField("3");
        this.pnlBusqueda.add(new JLabel("Buscar Amigos"));
        this.pnlBusqueda.add(new JLabel());
        this.pnlBusqueda.add(new JLabel("Porcentaje de Compatibilidad (x/100):"));
        this.pnlBusqueda.add(compatibilidad);
        this.pnlBusqueda.add(new JLabel("Niveles de B�squeda:"));
        this.pnlBusqueda.add(niveles);
        this.pnlBusqueda.add(new JLabel());
        this.pnlBusqueda.add(search);
    }

    public void actionPerformed(ActionEvent e) {
        this.lblStatusBar.setText("");
        this.lblStatusBar.setForeground(Color.black);
        if (e.getSource().equals(archivoNuevo)) {
            int n = JOptionPane.showConfirmDialog(this, "�Desea guardar los cambios?", "Alerta", JOptionPane.YES_NO_CANCEL_OPTION);
            if (n == JOptionPane.YES_OPTION) {
                JFileChooser fc = new JFileChooser();
                int returnVal = fc.showSaveDialog(this);
                if (returnVal == JFileChooser.APPROVE_OPTION) {
                    File file = fc.getSelectedFile();
                    String path = file.getAbsolutePath();
                    try {
                        Grafo.guardaInformacion(path);
                        this.lblStatusBar.setText("Archivo Guardado: " + path);
                    } catch (Exception ex) {
                        this.lblStatusBar.setText("No fue posible guardar el archivo");
                    }
                }
            } else if (n == JOptionPane.CANCEL_OPTION) {
                return;
            }
            Main.getAgente().clear();
        } else if (e.getSource().equals(archivoAbrir)) {
            if (Persona.getPersonas().length > 0) {
                int n = JOptionPane.showConfirmDialog(this, "�Desea guardar los cambios?", "Alerta", JOptionPane.YES_NO_CANCEL_OPTION);
                if (n == JOptionPane.YES_OPTION) {
                    JFileChooser fc = new JFileChooser();
                    int returnVal = fc.showSaveDialog(this);
                    if (returnVal == JFileChooser.APPROVE_OPTION) {
                        File file = fc.getSelectedFile();
                        String path = file.getAbsolutePath();
                        try {
                            Grafo.guardaInformacion(path);
                            this.lblStatusBar.setText("Archivo Guardado: " + path);
                        } catch (Exception ex) {
                            this.lblStatusBar.setText("No fue posible guardar el archivo");
                        }
                    }
                } else if (n == JOptionPane.CANCEL_OPTION) {
                    return;
                }
            }
            Main.getAgente().clear();
            JFileChooser fc = new JFileChooser();
            int returnVal = fc.showOpenDialog(this);
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                File file = fc.getSelectedFile();
                String path = file.getAbsolutePath();
                try {
                    Grafo.cargaGrafo(path);
                    this.lblStatusBar.setText("Red Cargada: " + path);
                } catch (Exception ex) {
                    this.lblStatusBar.setText("No fue posible abrir el archivo");
                }
            }
        } else if (e.getSource().equals(archivoGuardar)) {
            JFileChooser fc = new JFileChooser();
            int returnVal = fc.showSaveDialog(this);
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                File file = fc.getSelectedFile();
                String path = file.getAbsolutePath();
                try {
                    Grafo.guardaInformacion(path);
                    this.lblStatusBar.setText("Archivo Guardado: " + path);
                } catch (Exception ex) {
                    this.lblStatusBar.setText("No fue posible guardar el archivo");
                }
            }
        } else if (e.getSource().equals(archivoCerrar)) {
            System.exit(0);
        } else if (e.getSource().equals(simulacionAtributos)) {
            manejadorAtributos ma = new manejadorAtributos();
            ma.setVisible(true);
        } else if (e.getSource().equals(search)) {
            if (this.seleccionada == null) {
                this.lblStatusBar.setText("Seleccione una persona sobre la cual realizar la busqueda");
                this.lblStatusBar.setForeground(Color.red);
            } else {
                this.lblStatusBar.setForeground(Color.black);
                this.lblStatusBar.setText("Buscando amigos...");
                Persona sel = this.seleccionada;
                int nivel = Integer.parseInt(this.niveles.getText());
                int error_range = Integer.parseInt(this.niveles.getText());
                if (nivel > 0 && error_range > 0) {
                    LinkedList<Persona> results = Main.getAgente().buscarAmigos(this.seleccionada, nivel, error_range);
                    this.seleccionada = sel;
                    this.updatePropertiesPanel();
                    this.lblStatusBar.setText("Busqueda Finalizada: " + results.size() + " Personas encontradas.");
                    if (results.size() == 0) {
                        JOptionPane.showMessageDialog(this, "No se encontraron personas", "Resultados", JOptionPane.ERROR_MESSAGE);
                    } else {
                        String cols[] = { "Persona" };
                        Object data[][] = new Object[results.size()][1];
                        int i = 0;
                        for (Persona p : results) {
                            data[i][0] = p.getNombre();
                            i++;
                        }
                        pnlResultados.removeAll();
                        pnlResultados.add(new JTable(data, cols));
                        pnlResultados.repaint();
                        DisplayAttributesFrame.refreshWindows();
                    }
                } else if (nivel <= 0) {
                    this.lblStatusBar.setText("El nivel debe de ser mayor a 0");
                    this.lblStatusBar.setForeground(Color.red);
                } else if (error_range <= 0) {
                    this.lblStatusBar.setText("El nivel de compatibilidad debe de ser mayor a 0");
                    this.lblStatusBar.setForeground(Color.red);
                }
            }
        } else if (e.getSource().equals(simulacionAgregar)) {
            AgregaPersonaFrame apf = new AgregaPersonaFrame();
            apf.setVisible(true);
        } else if (e.getSource().equals(blacklist)) {
            if (this.seleccionada == null) {
                this.lblStatusBar.setText("Seleccione una persona sobre la cual realizar la busqueda");
                this.lblStatusBar.setForeground(Color.red);
            } else {
                Persona x = this.seleccionada;
                System.out.println(x.toString());
                BlackListFrame blf = new BlackListFrame(x);
                blf.setVisible(true);
            }
        } else if (e.getSource().equals(ayudaInfo)) {
            about a = new about();
            a.setVisible(true);
        } else if (e.getSource().equals(simulacionBorrar)) {
            if (seleccionada != null) {
                Main.getAgente().quitarPersona(seleccionada);
                for (DisplayAttributesFrame ve : DisplayAttributesFrame.ventanas) {
                    if (ve.p.equals(seleccionada)) {
                        ve.dispose();
                    }
                }
                this.removeElement(seleccionada);
                this.amigos.repaint();
                this.seleccionada = null;
                this.updatePropertiesPanel();
            }
        } else if (e.getSource().equals(edit)) {
            if (this.seleccionada == null) {
                this.lblStatusBar.setText("Seleccione una persona sobre la cual realizar la busqueda");
                this.lblStatusBar.setForeground(Color.red);
            } else {
                Persona y = this.seleccionada;
                AgregaPersonaFrame x = new AgregaPersonaFrame(y);
                x.setVisible(true);
            }
        }
    }

    @Override
    public void valueChanged(GraphSelectionEvent arg0) {
        String nombre = arg0.getCell().toString();
        this.seleccionada = Persona.getPersona(nombre);
        this.updatePropertiesPanel();
    }

    /**
	 * Clase para darle formato a las celdas de la lista de usuarios
	 * @author Revolution Software Developers
	 */
    private class CellRenderer extends DefaultListCellRenderer {

        private static final long serialVersionUID = 1L;

        public CellRenderer() {
            this.setOpaque(true);
        }

        public Component getListCellRendererComponent(JList list, final Object value, int index, boolean isSelected, boolean cellHasFocus) {
            super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            super.setIcon(getIconImage("user.png"));
            super.setText(value.toString());
            super.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(242, 242, 242)));
            return this;
        }
    }

    /**
	 * Regresa el ImageIcon de una imagen especificada
	 * @param filename
	 * @return image
	 */
    public ImageIcon getIconImage(String filename) {
        ImageIcon image = new ImageIcon(filename);
        if (image.getImageLoadStatus() == 4) return null;
        return image;
    }
}
