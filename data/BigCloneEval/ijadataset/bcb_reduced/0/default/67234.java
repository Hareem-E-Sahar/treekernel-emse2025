import java.awt.*;
import javax.swing.*;
import java.awt.event.*;
import java.io.*;

public class EEPROMDebugWindow extends DebugWindow implements ActionListener {

    JButton load;

    Container load_cont;

    Memory eeprom;

    public EEPROMDebugWindow(PIC parent, Memory eeprom) {
        super(parent, eeprom, true);
        this.eeprom = eeprom;
        super.CreateWindow("EEPROM Debug", Toolkit.getDefaultToolkit().getScreenSize().width - 373, 442);
        load = new JButton("Load EEPROM");
        load_cont = new Container();
        FlowLayout flow = new FlowLayout();
        flow.setVgap(0);
        load_cont.setLayout(flow);
        CreateContent();
        f.setSize(370, 96);
    }

    private void CreateContent() {
        adresses.setText("   00 01 02 03 04 05 06 07 08 09 0A 0B 0C 0D 0E 0F");
        adresses2.setText("00 \n10 \n20 \n30");
        content.add(adresses, BorderLayout.NORTH);
        content.add(adresses2, BorderLayout.WEST);
        textarea.setPreferredSize(new Dimension(335, 55));
        load_cont.add(textarea);
        content.add(load_cont, BorderLayout.CENTER);
        load.addActionListener(this);
    }

    public void actionPerformed(ActionEvent evt) {
        if (evt.getSource() == load) {
            JFileChooser lstchooser = new JFileChooser();
            lstchooser.setDialogTitle("Select file containing EEPROM memory data");
            if (lstchooser.showOpenDialog(Workbench.mainframe) == JFileChooser.APPROVE_OPTION) {
                File file = lstchooser.getSelectedFile();
                eeprom.loadbin(file.getPath());
                parent.updateDebugWindows();
            }
        }
        if (evt.getSource() == ok) {
            try {
                int data = Integer.parseInt(text.getText());
                if (data < 256) {
                    changer.setVisible(false);
                    mem.store(adress, data);
                    parent.updateDebugWindows();
                }
            } catch (NumberFormatException e) {
            }
        }
    }
}
