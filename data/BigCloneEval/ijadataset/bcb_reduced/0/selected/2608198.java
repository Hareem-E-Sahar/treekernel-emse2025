package uk.org.toot.swingui.midiui.sequenceui;

import java.awt.Dimension;
import uk.org.toot.midi.sequence.MidiSequence;
import uk.org.toot.midi.sequence.MidiTrack;
import javax.swing.JLabel;
import javax.swing.JPopupMenu;
import uk.org.toot.swingui.miscui.ClickAdapter;
import uk.org.toot.swingui.midiui.MidiColor;
import javax.swing.JRadioButtonMenuItem;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import uk.org.toot.swingui.miscui.DynamicPopup;

class TrackLabel extends JLabel {

    protected MidiSequence sequence;

    private MidiTrack track;

    private ClickAdapter clickAdapter;

    public TrackLabel(MidiSequence sequence, MidiTrack track) {
        super("");
        this.sequence = sequence;
        this.track = track;
        this.setOpaque(true);
        setPreferredSize(new Dimension(128, 24));
        setMinimumSize(new Dimension(96, 16));
        refresh();
    }

    public void addNotify() {
        super.addNotify();
        if (clickAdapter == null) {
            clickAdapter = new ClickAdapter(createPopupMenu(), true);
        }
        addMouseListener(clickAdapter);
    }

    public void removeNotify() {
        removeMouseListener(clickAdapter);
        super.removeNotify();
    }

    protected JPopupMenu createPopupMenu() {
        return new TrackPopup();
    }

    public void refresh() {
        Float fhue = (Float) track.getClientProperty("Hue");
        if (fhue != null) {
            float hue = fhue.floatValue();
            setBackground(MidiColor.asHSB(hue, 0.42f, 1.0f));
        }
        setText(track.getTrackName());
    }

    public MidiTrack getSelectedTrack() {
        return track;
    }

    public void setSelectedTrack(MidiTrack track) {
        this.track = track;
        refresh();
    }

    protected MidiSequence getSequence() {
        return sequence;
    }

    public class TrackPopup extends DynamicPopup {

        public TrackPopup() {
            super("Track");
        }

        protected void refreshMenu() {
            removeAll();
            MidiTrack[] tracks = getSequence().getMidiTracks();
            for (int t = 0; t < tracks.length; t++) {
                if (!isValid(tracks[t])) continue;
                add(createTrackItem(tracks[t]));
            }
        }

        protected boolean isValid(MidiTrack track) {
            return true;
        }

        protected JRadioButtonMenuItem createTrackItem(MidiTrack track) {
            return new TrackItem(track);
        }

        protected class TrackItem extends JRadioButtonMenuItem implements ActionListener {

            private MidiTrack track;

            public TrackItem(MidiTrack track) {
                super(track.getTrackName());
                this.track = track;
                setSelected(track == getSelectedTrack());
                if (track.getChannel() >= 0) {
                    setBackground(MidiColor.asHSB((Float) track.getClientProperty("Hue"), 0.342f, 1.0f));
                }
                addActionListener(this);
            }

            public void actionPerformed(ActionEvent ae) {
                setSelectedTrack(track);
            }
        }
    }
}
