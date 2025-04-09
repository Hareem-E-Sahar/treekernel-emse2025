import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import javax.swing.SpinnerDateModel;
import javax.swing.SwingConstants;
import todolist.Accion;
import todolist.ListaAcciones;
import todolist.PanelDatos;
import todolist.interfaces.componente;

public class AlarmaPlugin implements componente {

    JLabel reloj = new JLabel("Hora Actual");

    JLabel proximaAlarma = new JLabel();

    JLabel proximaAlarmaHora = new JLabel();

    Date FechaYHora;

    Alarma AlarmaVigente = null;

    ListaDeAlarmas AlarmList;

    JSpinner SpinnerDiaHora = new JSpinner();

    PanelDatos PanelDeDatos;

    JPanel interno1;

    JLabel Tarea;

    @SuppressWarnings("empty-statement")
    public JComponent getComponent(ListaAcciones lista, PanelDatos P) {
        AdministradorDeArchivos adminIO = new AdministradorDeArchivos();
        AlarmList = adminIO.ObtenerLista();
        PanelDeDatos = P;
        JPanel PanelContenedor = new JPanel();
        PanelContenedor.setLayout(new BorderLayout());
        JTabbedPane panelTabulado = new JTabbedPane();
        javax.swing.Timer t = new javax.swing.Timer(1000, new ActionListener() {

            int i = 0;

            public void actionPerformed(ActionEvent e) {
                actualizarReloj();
                interno1.remove(Tarea);
                Accion ac = PanelDeDatos.getAccionSeleccionada();
                if (ac != null) {
                    Tarea = new JLabel("Se registrará una alarma para: ".concat(ac.getTexto()));
                } else {
                    Tarea = new JLabel("Seleccione una tarea en el panel principal");
                }
                interno1.add(Tarea);
                interno1.validate();
                i++;
                if ((i == 10) && (AlarmaVigente == null)) {
                    AlarmaVigente = AlarmList.getProximaAlarma();
                }
                if (i == 20) {
                    AlarmaVigente = AlarmList.getPrimerAlarma();
                    i = 0;
                }
                controlarAlarma();
            }
        });
        t.start();
        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(5, 1));
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 0;
        c.gridy = 0;
        reloj.setHorizontalAlignment(SwingConstants.CENTER);
        panel.add(reloj, c);
        c.gridx = 0;
        c.gridy = 4;
        proximaAlarma.setSize(150, 15);
        proximaAlarma.setHorizontalAlignment(SwingConstants.CENTER);
        panel.add(proximaAlarma, c);
        c.gridx = 0;
        c.gridy = 5;
        proximaAlarmaHora.setHorizontalAlignment(SwingConstants.CENTER);
        panel.add(proximaAlarmaHora, c);
        panelTabulado.addTab("Alarmas", null, panel, "Alarmas");
        JPanel panel2 = new JPanel();
        SpinnerDiaHora.setModel(new SpinnerDateModel());
        SpinnerDiaHora.setSize(150, 30);
        JButton BotonRegistrarAlarma = new JButton();
        BotonRegistrarAlarma.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                registrarAlarma();
            }
        });
        BotonRegistrarAlarma.setSize(100, 45);
        BotonRegistrarAlarma.setText("Registrar");
        panel2.setLayout(new GridLayout(2, 1));
        interno1 = new JPanel();
        Accion ac = PanelDeDatos.getAccionSeleccionada();
        if (ac != null) {
            Tarea = new JLabel("Se registrará una alarma para: ".concat(ac.getTexto()));
        } else {
            Tarea = new JLabel("Seleccione una tarea en el panel principal");
        }
        interno1.add(Tarea);
        panel2.add(interno1);
        Tarea.setHorizontalAlignment(SwingConstants.CENTER);
        JPanel interno2 = new JPanel();
        interno2.add(SpinnerDiaHora);
        interno2.add(BotonRegistrarAlarma);
        panel2.add(interno2);
        panelTabulado.addTab("Registrar Alarma", null, panel2, "Registrar Alarma");
        PanelContenedor.add(panelTabulado, BorderLayout.CENTER);
        return PanelContenedor;
    }

    private void actualizarReloj() {
        Calendar calendarioAhora = Calendar.getInstance();
        SimpleDateFormat formatoFechaHora = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        FechaYHora = calendarioAhora.getTime();
        reloj.setText(formatoFechaHora.format(calendarioAhora.getTime()));
    }

    private void controlarAlarma() {
        if (AlarmaVigente != null) {
            SimpleDateFormat formatoFechaHora = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
            proximaAlarma.setText("Proxima tarea con alarma: ".concat(AlarmaVigente.getTexto()));
            proximaAlarmaHora.setText("Hora de la alarma: ".concat(formatoFechaHora.format(AlarmaVigente.getFecha())));
            if (AlarmaVigente.getFecha().before(FechaYHora)) {
                try {
                    Clip sonido = AudioSystem.getClip();
                    sonido.open(AudioSystem.getAudioInputStream(new File("chau.wav")));
                    sonido.start();
                    JOptionPane.showMessageDialog(null, "Recuerde: ".concat(AlarmaVigente.getTexto()));
                } catch (Exception ex) {
                    System.out.print(ex.getMessage());
                }
                AlarmaVigente = AlarmList.getProximaAlarma();
            }
        } else {
            proximaAlarma.setText("No hay ninguna alarma configurada");
            proximaAlarmaHora.setText("");
        }
    }

    private void registrarAlarma() {
        Accion ac = PanelDeDatos.getAccionSeleccionada();
        Alarma nuevaAlarma;
        AdministradorDeArchivos FileAdmin;
        if (ac != null) {
            nuevaAlarma = new Alarma();
            nuevaAlarma.setFecha((Date) SpinnerDiaHora.getValue());
            nuevaAlarma.setIdentificadorTarea(ac.getId());
            nuevaAlarma.setTexto(ac.getTexto());
            AlarmList.add(nuevaAlarma);
            System.out.print(AlarmList.size());
            FileAdmin = new AdministradorDeArchivos();
            FileAdmin.AlmacenarLista(AlarmList);
        }
    }
}
