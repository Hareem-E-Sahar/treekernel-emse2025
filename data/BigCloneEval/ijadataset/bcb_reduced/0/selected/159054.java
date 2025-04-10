package uk.org.toot.swingui.midiui.sequenceui;

import javax.swing.event.TableModelEvent;
import java.awt.BorderLayout;
import uk.org.toot.midi.message.ChannelMsg;
import uk.org.toot.midi.sequence.MidiTrack;
import java.awt.event.ActionEvent;
import java.util.Arrays;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;
import javax.swing.JTable;
import javax.swing.JPanel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;
import javax.swing.table.AbstractTableModel;
import javax.sound.midi.MidiEvent;
import javax.sound.midi.MidiMessage;
import uk.org.toot.swingui.miscui.TootBar;
import uk.org.toot.swingui.midiui.MidiUI;

public class TrackEditor extends JPanel {

    private TableModel dataModel;

    private JTable table;

    protected JToolBar toolBar;

    protected JToolBar infoBar;

    private JScrollPane scrollPane;

    private MidiTrack track;

    private JLabel sizeL;

    public TrackEditor(MidiTrack track) {
        super(new BorderLayout());
        this.track = track;
        toolBar = new ToolBar();
        infoBar = new InfoBar();
        dataModel = new EventTableModel();
        table = new EventTable(dataModel);
        scrollPane = new JScrollPane(table);
        JPanel innerPane = new JPanel(new BorderLayout());
        add(toolBar, BorderLayout.NORTH);
        add(innerPane, BorderLayout.CENTER);
        innerPane.add(scrollPane, BorderLayout.CENTER);
        innerPane.add(infoBar, BorderLayout.SOUTH);
    }

    public void setTrack(MidiTrack track) {
        this.track = track;
        table.tableChanged(new TableModelEvent(dataModel));
        updateInfo();
    }

    /** Delete the currently selected events, if any */
    protected void deleteEvents() {
        int[] rows = table.getSelectedRows();
        Arrays.sort(rows);
        for (int row = rows.length - 1; row >= 0; row--) {
            track.remove(track.get(row));
        }
        track.getSequence().fireChanged();
        table.tableChanged(new TableModelEvent(dataModel));
        updateInfo();
    }

    protected void updateInfo() {
        sizeL.setText(new Long(track.size()).toString() + " events ");
    }

    protected String getPosition(long tick) {
        return String.valueOf(tick);
    }

    protected class ToolBar extends TootBar {

        private final String NEW_EVENT = "New Event";

        private final String DELETE_EVENTS = "Delete Events";

        public ToolBar() {
            super("Track Editor");
            add(makeButton("general/New16", NEW_EVENT, NEW_EVENT, NEW_EVENT, true));
            add(makeButton("general/Delete16", DELETE_EVENTS, DELETE_EVENTS, DELETE_EVENTS, true));
        }

        public void actionPerformed(ActionEvent e) {
            String cmd = e.getActionCommand();
            if (DELETE_EVENTS.equals(cmd)) {
                TrackEditor.this.deleteEvents();
            } else if (NEW_EVENT.equals(cmd)) {
            }
        }
    }

    protected class InfoBar extends TootBar {

        public InfoBar() {
            sizeL = new JLabel(new Long(track.size()).toString() + " events ");
            add(sizeL);
            addSeparator();
        }
    }

    private class EventTableModel extends AbstractTableModel {

        private final int TICK = 0;

        private final int POSITION = 1;

        private final int TYPE = 2;

        private final int CHANNEL = 3;

        private final int VALUE = 4;

        private final String[] names = { "Tick", "Position", "Type", "Ch", "Value" };

        public int getRowCount() {
            return track.size();
        }

        public int getColumnCount() {
            return names.length;
        }

        public Object getValueAt(int row, int col) {
            MidiEvent ev = track.get(row);
            MidiMessage m = ev.getMessage();
            switch(col) {
                case TICK:
                    return new Long(ev.getTick());
                case POSITION:
                    return getPosition(ev.getTick());
                case TYPE:
                    return MidiUI.getMessageType(m);
                case CHANNEL:
                    if (ChannelMsg.isChannel(m)) return String.valueOf(1 + ChannelMsg.getChannel(m));
                    break;
                case VALUE:
                    return MidiUI.getMessageValue(m);
            }
            return "";
        }

        public String getColumnName(int col) {
            return names[col];
        }

        public Class<?> getColumnClass(int c) {
            return getValueAt(0, c).getClass();
        }

        public boolean isCellEditable(int row, int col) {
            if (col == VALUE) return true;
            return false;
        }

        public void setValueAt(Object val, int row, int col) {
            MidiEvent ev = track.get(row);
            MidiMessage msg = ev.getMessage();
            switch(col) {
                case TICK:
                    break;
                case POSITION:
                    break;
                case TYPE:
                    break;
                case CHANNEL:
                    break;
                case VALUE:
                    MidiUI.setMessageValue(msg, (String) val);
                    break;
            }
        }
    }

    private static class EventTable extends JTable {

        public EventTable(TableModel model) {
            super(model);
            TableColumn col = getColumn("Ch");
            col.setMaxWidth(32);
        }
    }
}
