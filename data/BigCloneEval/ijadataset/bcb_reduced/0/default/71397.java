import java.awt.event.*;
import java.io.IOException;
import java.rmi.*;
import javax.swing.*;
import visad.*;
import visad.data.*;
import visad.java3d.DisplayImplJ3D;
import visad.util.Util;

/** A simple test for collaboration using a socket data server */
public class SocketDataTest extends JFrame implements ActionListener {

    /** true if server, false if client */
    private boolean server;

    /** data reference pointing to the data */
    private DataReferenceImpl ref;

    /** display that shows the data */
    private DisplayImpl disp;

    /** dialog for loading data files */
    private JFileChooser dialog;

    /** builds the GUI */
    private void constructGUI(String arg, boolean enableButtons) {
        dialog = Util.getVisADFileChooser();
        JPanel pane = new JPanel();
        setContentPane(pane);
        pane.setLayout(new BoxLayout(pane, BoxLayout.Y_AXIS));
        pane.add(disp.getComponent());
        JPanel buttons = new JPanel();
        buttons.setLayout(new BoxLayout(buttons, BoxLayout.X_AXIS));
        JButton load = new JButton("Load data");
        JButton reset = new JButton("Reset to default");
        load.addActionListener(this);
        load.setActionCommand("load");
        load.setEnabled(enableButtons);
        reset.addActionListener(this);
        reset.setActionCommand("reset");
        reset.setEnabled(enableButtons);
        buttons.add(load);
        buttons.add(reset);
        pane.add(buttons);
        addWindowListener(new WindowAdapter() {

            public void windowClosing(WindowEvent e) {
                System.exit(0);
            }
        });
        setTitle("SocketDataTest " + (server ? "server" : "client") + ": " + arg);
        pack();
        setVisible(true);
    }

    /** usage message */
    private static final String usage = "Usage: java SocketDataTest [-s port] [-c ip.address:port]";

    /** runs the test */
    public static void main(String[] argv) throws VisADException, RemoteException, IOException {
        if (argv.length < 2) {
            System.err.println("Not enough arguments.");
            System.err.println(usage);
            System.exit(1);
        }
        String sc = argv[0];
        String arg = argv[1];
        boolean serv = false;
        if (sc.equalsIgnoreCase("-s")) serv = true; else if (!sc.equalsIgnoreCase("-c")) {
            System.err.println("Please specify either -s or -c");
            System.err.println(usage);
            System.exit(2);
        }
        SocketDataTest test = new SocketDataTest(serv, arg);
    }

    /** constructs a new SocketDataTest display */
    public SocketDataTest(boolean serv, String arg) throws VisADException, RemoteException, IOException {
        server = serv;
        disp = new DisplayImplJ3D("disp");
        if (server) {
            int port = -1;
            try {
                port = Integer.parseInt(arg);
            } catch (NumberFormatException exc) {
            }
            if (port < 0 || port > 9999) {
                System.err.println("Invalid port: " + arg);
                System.exit(3);
            }
            ref = new DataReferenceImpl("ref");
            disp.addReference(ref);
            SocketDataServer server = new SocketDataServer(port, ref);
            loadData(null);
            constructGUI(arg, true);
        } else {
            SocketDataSource source = new SocketDataSource("SocketDataTest");
            source.open(arg);
            ref = source.getReference();
            disp.addReference(ref);
            CellImpl mapsCell = new CellImpl() {

                public synchronized void doAction() throws VisADException, RemoteException {
                    Data data = ref.getData();
                    if (data != null) setMaps(data);
                }
            };
            mapsCell.addReference(ref);
            constructGUI(arg, false);
        }
    }

    /** loads a data set from the given file, or reverts back to the default */
    private void loadData(String file) throws VisADException, RemoteException {
        Data data = null;
        if (file == null) {
            int size = 64;
            RealType ir_radiance = RealType.getRealType("ir_radiance");
            RealType vis_radiance = RealType.getRealType("vis_radiance");
            RealType[] types = { RealType.Latitude, RealType.Longitude };
            RealType[] types2 = { vis_radiance, ir_radiance };
            RealTupleType earth_location = new RealTupleType(types);
            RealTupleType radiance = new RealTupleType(types2);
            FunctionType image_tuple = new FunctionType(earth_location, radiance);
            data = FlatField.makeField(image_tuple, size, false);
        } else {
            DefaultFamily loader = new DefaultFamily("loader");
            try {
                data = loader.open(file);
            } catch (BadFormException exc) {
                throw new VisADException(exc.getMessage());
            }
        }
        if (data != null) {
            setMaps(data);
            ref.setData(data);
        }
    }

    /** sets the current data sets mappings */
    private void setMaps(Data data) throws VisADException, RemoteException {
        ScalarMap[] maps = data.getType().guessMaps(true);
        disp.removeReference(ref);
        disp.clearMaps();
        for (int i = 0; i < maps.length; i++) disp.addMap(maps[i]);
        disp.addReference(ref);
    }

    /** handles button clicks */
    public synchronized void actionPerformed(ActionEvent e) {
        String cmd = e.getActionCommand();
        try {
            if (cmd.equals("load")) {
                int returnVal = dialog.showOpenDialog(this);
                if (returnVal == JFileChooser.APPROVE_OPTION) {
                    loadData(dialog.getSelectedFile().getAbsolutePath());
                }
            } else if (cmd.equals("reset")) {
                loadData(null);
            }
        } catch (VisADException exc) {
            exc.printStackTrace();
        } catch (RemoteException exc) {
            exc.printStackTrace();
        }
    }
}
