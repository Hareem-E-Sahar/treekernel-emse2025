import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import sourceforge.jpacket.*;

public class PacketWizard extends JPanel implements ActionListener {

    PacketEditor payload;

    IPPacketEditor ip;

    IPPacket ippacket;

    TCPPacket tcppacket;

    UDPPacket udppacket;

    JPanel pTools;

    PacketWriter pw = null;

    JButton bSend, bSave, bReply, bClose, bLoad;

    static String SEND = "SEND", SAVE = "SAVE", REPLY = "REPLY", CLOSE = "CLOSE", LOAD = "LOAD";

    public PacketWizard(IPPacket new_ip, PacketWriter w) {
        this(new_ip);
        pw = w;
    }

    public PacketWizard(IPPacket new_ip) {
        super();
        ip = new IPPacketEditor("IP Editor");
        ip.set(new_ip);
        switch(new_ip.getProtocol()) {
            case IPPacket.PROTO_TCP:
                payload = new TCPPacketEditor();
                break;
            case IPPacket.PROTO_UDP:
                payload = new UDPPacketEditor();
                break;
            case IPPacket.PROTO_ICMP:
                payload = new ICMPPacketEditor();
        }
        payload.set(new_ip.getPayload());
        pTools = new JPanel();
        bSend = new JButton("Send");
        bSend.setActionCommand(SEND);
        ;
        bSend.addActionListener(this);
        bSave = new JButton("Save");
        bSave.setActionCommand(SAVE);
        ;
        bSave.addActionListener(this);
        bLoad = new JButton("Load");
        bLoad.setActionCommand(LOAD);
        ;
        bLoad.addActionListener(this);
        bReply = new JButton("Reply");
        bReply.setActionCommand(REPLY);
        ;
        bReply.addActionListener(this);
        bClose = new JButton("Close Conn.");
        bClose.setActionCommand(CLOSE);
        ;
        bClose.addActionListener(this);
        bClose.setEnabled(false);
        pTools.add(bSend);
        pTools.add(bSave);
        pTools.add(bLoad);
        pTools.add(bReply);
        pTools.add(bClose);
        setLayout(new BorderLayout());
        add(pTools, BorderLayout.NORTH);
        add(createPacketHandler(), BorderLayout.SOUTH);
    }

    public void actionPerformed(java.awt.event.ActionEvent e) {
        String command = e.getActionCommand();
        ippacket = ip.get();
        tcppacket = (TCPPacket) payload.get();
        ippacket.setPayload(tcppacket);
        ippacket.doCheckSum();
        tcppacket.doCheckSum();
        if (command.equals(SAVE)) {
            try {
                JFileChooser chooser = new JFileChooser();
                int returnVal = chooser.showSaveDialog(this);
                if (returnVal == JFileChooser.APPROVE_OPTION) {
                    SimpleIPWriter writer = new SimpleIPWriter(chooser.getSelectedFile().getCanonicalPath());
                    writer.writePacket(ippacket);
                }
            } catch (IOException exception) {
            }
        }
        if (command.equals(LOAD)) {
            try {
                JFileChooser chooser = new JFileChooser();
                int returnVal = chooser.showOpenDialog(this);
                if (returnVal == JFileChooser.APPROVE_OPTION) {
                    SimpleIPReader reader = new SimpleIPReader(chooser.getSelectedFile().getCanonicalPath());
                    ip.set((IPPacket) reader.nextPacket());
                }
            } catch (IOException exception) {
            }
        }
        if (command.equals(SEND)) {
            if (pw != null) {
                pw.writePacket(ippacket);
            }
        }
        if (command.equals(REPLY)) {
            PacketCooking.reply(ippacket);
            ip.refresh();
            payload.refresh();
        }
    }

    private Component createPacketHandler() {
        JPanel pane = new JPanel();
        pane.setLayout(new BorderLayout());
        pane.add(ip, BorderLayout.WEST);
        pane.add((JPanel) payload, BorderLayout.EAST);
        return pane;
    }

    private JPanel glue(JComponent c1, JComponent c2) {
        JPanel p = new JPanel();
        p.setLayout(new GridLayout(1, 2));
        p.add(c1);
        p.add(c2);
        return p;
    }

    public static void main(String[] args) {
    }
}
