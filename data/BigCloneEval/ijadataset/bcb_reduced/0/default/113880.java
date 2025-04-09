import au.com.bytecode.opencsv.CSVWriter;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.JOptionPane.*;
import javax.swing.event.TableModelEvent;
import javax.swing.table.DefaultTableModel;
import javax.swing.event.TableModelListener;
import java.awt.*;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;

public class Tabla extends JPanel {

    private JTable tabla;

    private JScrollPane scroll;

    private JPanel botones;

    private JButton agregarElemento, borrarElemento, agregarCampo, borrarCampo, obtenerCSV;

    private ModeloTabla modelo;

    private Categoria categoria;

    private ConexionBD conexion;

    private StringEncrypter encriptador;

    /**
     * Crea una nueva instancia de la clase Tabla, inicializando todos sus
     * componentes.
     * @param conexion ConexionBD que permite interactuar con la base de datos.
     * @param categoria Categoria que es representada gráficamente por esta tabla.
     * @param encriptador StringEncrypter que se encuentra encriptando y desencriptando la información del programa.
     */
    public Tabla(ConexionBD conexion, Categoria categoria, StringEncrypter encriptador) {
        this.conexion = conexion;
        this.categoria = categoria;
        this.encriptador = encriptador;
        modelo = new ModeloTabla();
        GeneradorModeloTabla.GenerarModeloTabla(categoria.getResultSet(), modelo, encriptador);
        tabla = new JTable(modelo);
        actualizarTabla();
        scroll = new JScrollPane(tabla);
        add(scroll);
        botones = new JPanel(new GridLayout(0, 1, 5, 5));
        tabla.setDragEnabled(true);
        agregarElemento = new JButton("Agregar elemento");
        borrarElemento = new JButton("Borrar elemento");
        agregarCampo = new JButton("Crear Campo");
        borrarCampo = new JButton("Eliminar campo");
        obtenerCSV = new JButton("Guardar como CSV");
        botones.add(agregarElemento);
        botones.add(borrarElemento);
        botones.add(agregarCampo);
        botones.add(borrarCampo);
        botones.add(obtenerCSV);
        add(botones);
        Listener listener = new Listener();
        agregarElemento.addActionListener(listener);
        agregarCampo.addActionListener(listener);
        borrarCampo.addActionListener(listener);
        borrarElemento.addActionListener(listener);
        TableListener listenerTabla = new TableListener();
        tabla.getModel().addTableModelListener(listenerTabla);
        tabla.getTableHeader().setReorderingAllowed(false);
        obtenerCSV.addActionListener(new java.awt.event.ActionListener() {

            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                obtenerCSVActionPerformed(evt);
            }
        });
    }

    /**
     * Permite actualizar la información contenida por la tabla.
     */
    public void actualizarTabla() {
        GeneradorModeloTabla.GenerarModeloTabla(categoria.getResultSet(), modelo, encriptador);
        tabla.setModel(modelo);
    }

    /**
     * Busca en una tabla cierta palabra, regresando los resultados de dicha búsqueda.
     * @param palabraClave String con la palabra a buscar.
     * @return String que muestra la información de los resultados hallados.
     */
    public String buscarEnTabla(String palabraClave) {
        String resultado = "";
        for (int i = 0; i < modelo.getRowCount(); i++) {
            try {
                String datoAgregar = (String) tabla.getValueAt(i, 1);
                String datoAgregarEnMinusculas = datoAgregar.toLowerCase();
                if (datoAgregarEnMinusculas.contains(palabraClave.toLowerCase())) resultado += "Categoría: " + categoria.getTituloBD() + " Título: " + datoAgregar + " Id: " + (i + 1) + "<br>";
            } catch (java.lang.NullPointerException noHayDato) {
            }
        }
        return resultado;
    }

    /**
     * Devuelve el atributo categoría de esta clase.
     * @return Categoría de la cual esta tabla es la representación gráfica.
     */
    public Categoria getCategoria() {
        return categoria;
    }

    /**
     * Clase privada que actúa como listener para los cuatro botones que permiten modificar la tabla.
     */
    private class Listener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            if (e.getSource() == agregarCampo) {
                String columna = JOptionPane.showInputDialog(null, "Ingrese el nombre del campo a agregar");
                if (columna != null) {
                    try {
                        categoria.crearCampo(columna);
                    } catch (Exception exe) {
                        JOptionPane.showMessageDialog(null, "<html>No se pudo crear el campo. <br>Intente con otro nombre.</html>");
                    }
                    actualizarTabla();
                }
            } else {
                if (e.getSource() == borrarCampo) {
                    int column = tabla.getSelectedColumn();
                    if (column > -1) {
                        categoria.borrarCampo(column);
                        actualizarTabla();
                    } else {
                        JOptionPane.showMessageDialog(null, "Debe de seleccionar una columna.", "Atención", JOptionPane.WARNING_MESSAGE);
                    }
                } else {
                    if (e.getSource() == agregarElemento) {
                        String nombre = JOptionPane.showInputDialog(null, "Ingrese el título del nuevo elemento");
                        if ((nombre != null) && (!nombre.isEmpty()) && (nombre.length() <= 50)) {
                            JLabel label = new JLabel("<html><bf>Ingrese la contraseña</bf></html>");
                            JTextField contra = new JTextField();
                            JButton botonNuevo = new JButton("Generar Contraseña");
                            botonNuevo.addActionListener(new generarListener(contra));
                            javax.swing.JOptionPane.showMessageDialog(null, new Object[] { label, contra, botonNuevo }, "Password:", javax.swing.JOptionPane.OK_CANCEL_OPTION);
                            String contrasena = contra.getText();
                            nombre = encriptador.encrypt(nombre);
                            if (contrasena != null) {
                                contrasena = encriptador.encrypt(contrasena);
                                categoria.agregarElemento(nombre, contrasena);
                                actualizarTabla();
                            }
                        } else {
                            if (nombre != null) {
                                if (nombre.length() > 50) JOptionPane.showMessageDialog(null, "No puede agregar" + " más de 50 caracteres en un campo.", "Atención", JOptionPane.ERROR_MESSAGE);
                                if (nombre.isEmpty()) JOptionPane.showMessageDialog(null, "Debe de ingresar al menos un caracter.", "Atención", JOptionPane.ERROR_MESSAGE);
                            }
                        }
                        actualizarTabla();
                    } else {
                        if (e.getSource() == borrarElemento) {
                            int fila = tabla.getSelectedRow();
                            if (fila >= 0) {
                                String id = (String) tabla.getValueAt(fila, 0);
                                String nombre = (String) tabla.getValueAt(fila, 1);
                                int opcion = javax.swing.JOptionPane.showConfirmDialog(null, "¿Desea borrar la fila \"" + id + ". " + nombre + "\" ?");
                                if (opcion == 0) {
                                    conexion.eliminarFila(categoria.getTituloBD(), id);
                                    actualizarTabla();
                                }
                            } else JOptionPane.showMessageDialog(null, "Debe de seleccionar una fila.", "Atención", JOptionPane.WARNING_MESSAGE);
                        }
                    }
                }
                tabla.repaint();
            }
        }
    }

    /**
     * Clase privada que sirve de listener para la tabla.
     */
    private class TableListener implements TableModelListener {

        @Override
        public void tableChanged(TableModelEvent e) {
            try {
                int row = e.getFirstRow();
                int column = e.getColumn();
                if (row > -1 && column > -1) {
                    Integer id = Integer.parseInt((String) tabla.getValueAt(row, 0));
                    int idint = id;
                    DefaultTableModel modelo = (DefaultTableModel) e.getSource();
                    String data = (String) modelo.getValueAt(row, column);
                    if (data.length() <= 50) {
                        data = encriptador.encrypt(data);
                        categoria.cambioElemento(data, idint, column);
                    } else {
                        JOptionPane.showMessageDialog(null, "<html>No puede agregar" + " más de 50 carácteres en un campo.<br>Por tanto," + "este cambio será eliminado al cerrar el programa.", "Atención", JOptionPane.ERROR_MESSAGE);
                    }
                }
            } catch (java.lang.ArrayIndexOutOfBoundsException cambioNoAplicable) {
            } catch (Exception ex) {
                System.out.println("Error en tableChanged, TableListener");
            }
        }
    }

    /**
     * Indica al programa que realizar al presionar el botón de obtenerCSV.
     * @param evt Evento
     */
    private void obtenerCSVActionPerformed(java.awt.event.ActionEvent evt) {
        try {
            JFileChooser fc = new JFileChooser();
            int returnVal = fc.showSaveDialog(this);
            File file;
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                file = fc.getSelectedFile();
                CSVWriter writer = new CSVWriter(new FileWriter(file.getAbsolutePath()));
                writer.writeAll(getCategoria().getResultSet(), true);
                writer.close();
            }
        } catch (SQLException excepcionSQL) {
            JOptionPane.showMessageDialog(null, "Hubo un problema con la base de dato MySQL.", "ERROR", JOptionPane.ERROR_MESSAGE);
        } catch (IOException excepcionIO) {
            JOptionPane.showMessageDialog(null, "Hubo un problema de lectura escritura.", "ERROR", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Devueleve el modelo de tabla.
     * @return ModeloTabla de esta tabla.
     */
    public ModeloTabla getModelo() {
        return modelo;
    }

    /**
     * Clase interna que sirve como listener para el botón de generar contraseña.
     */
    private class generarListener implements ActionListener {

        private JTextField texto;

        /**
         * Crea una nueva instancia de la clase generarListener.
         * @param texto Texto a mostrar.
         */
        public generarListener(JTextField texto) {
            this.texto = texto;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            java.util.Random random = new java.util.Random();
            int valor;
            char[] lista = new char[10];
            for (int i = 0; i < 10; i++) {
                do {
                    valor = random.nextInt(75) + 48;
                } while (valor < 48 || (57 < valor && valor < 65) || (90 < valor && valor < 97));
                lista[i] = (char) valor;
            }
            String contrasena = new String(lista);
            texto.setText(contrasena);
        }
    }
}
