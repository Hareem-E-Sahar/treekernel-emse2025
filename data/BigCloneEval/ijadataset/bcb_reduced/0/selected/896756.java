package uk.org.toot.swingui.midiui.sequenceui;

import static uk.org.toot.midi.message.MetaMsg.TRACK_NAME;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Iterator;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JToolBar;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import uk.org.toot.midi.core.MidiSystem;
import uk.org.toot.midi.sequence.Midi;
import uk.org.toot.midi.sequence.MidiSequence;
import uk.org.toot.midi.sequence.MidiTrack;
import uk.org.toot.midi.sequence.SequencePosition;
import uk.org.toot.midi.sequence.edit.SequenceSelection;
import uk.org.toot.swingui.midiui.MidiColor;
import uk.org.toot.swingui.miscui.TootBar;

public abstract class Viewer extends JPanel {

    private OpenSequenceUI sequenceUI;

    private float ticksPerPixel;

    private SequencePosition matchPosition = new SequencePosition(0, 1);

    protected JScrollBar timeBar;

    protected boolean follow = true;

    protected JToolBar toolBar;

    public Viewer(OpenSequenceUI sequenceUI, MidiSystem rack) {
        super(new BorderLayout());
        this.sequenceUI = sequenceUI;
        toolBar = createToolBar();
        add(toolBar, BorderLayout.NORTH);
        sequenceUI.addPropertyChangeListener("position", new PropertyChangeListener() {

            public void propertyChange(PropertyChangeEvent evt) {
                updatePosition(((Long) evt.getOldValue()).longValue());
            }
        });
    }

    protected abstract JToolBar createToolBar();

    protected abstract void updatePosition(long tick);

    public MidiSequence getSequence() {
        return sequenceUI.getSequence();
    }

    public OpenSequenceUI getSequenceUI() {
        return sequenceUI;
    }

    public JToolBar getPositionView() {
        return sequenceUI.getPositionView();
    }

    protected SequenceSelection getSelection() {
        return sequenceUI.getSelection();
    }

    public void setSelection(SequenceSelection sel) {
        sequenceUI.setSelection(sel);
    }

    public boolean isVisibleTrack(MidiTrack track) {
        Object obj = track.getClientProperty(this);
        if (obj == null) {
            setVisibleTrack(track, true);
            return true;
        }
        return ((Boolean) obj).booleanValue();
    }

    public void setVisibleTrack(MidiTrack track, boolean visible) {
        track.putClientProperty(this, Boolean.valueOf(visible));
    }

    public float getTicksPerPixel() {
        return ticksPerPixel;
    }

    public void setTicksPerPixel(float ticksPerPixel) {
        this.ticksPerPixel = ticksPerPixel;
        setupPositionIterator();
    }

    protected void setupPositionIterator() {
        float pixelsPerBeat = getSequence().getResolution() / getTicksPerPixel();
        matchPosition = new SequencePosition(0, 1);
        while (pixelsPerBeat < 4) {
            pixelsPerBeat *= 2;
            if (matchPosition.beat > 0) {
                matchPosition.beat = 0;
                matchPosition.bar = 2;
            } else if (matchPosition.bar > 0) {
                matchPosition.bar *= 2;
            }
        }
    }

    public Iterator positionIterator(long tick) {
        return new PositionIterator(tick, this.matchPosition);
    }

    public boolean getFollowEnabled() {
        return follow;
    }

    public void setFollowEnabled(boolean enable) {
        follow = enable;
    }

    /**
     * knows which positions are resolvable and chooses well :)
     */
    private class PositionIterator implements Iterator {

        private Iterator iterator;

        private SequencePosition match;

        public PositionIterator(long tick, SequencePosition match) {
            this.iterator = getSequence().beatIterator(tick);
            this.match = match;
        }

        public boolean hasNext() {
            return iterator.hasNext();
        }

        public Object next() {
            SequencePosition next;
            do {
                next = (SequencePosition) iterator.next();
            } while (isDiscardable(next) && hasNext());
            return next;
        }

        public void remove() {
            iterator.remove();
        }

        private boolean isDiscardable(SequencePosition pos) {
            if (match.bar > 0) {
                if (pos.beat != 0) return true;
                return (pos.bar & (match.bar - 1)) != 0;
            } else if (match.beat > 0) {
                return (pos.beat & (match.beat - 1)) != 0;
            }
            return false;
        }
    }

    protected class TrackBar extends TootBar implements ActionListener {

        public TrackBar() {
            super(getSequence().getName() + " Tracks");
            setup();
            ChangeListener structureChangeListener = new ChangeListener() {

                public void stateChanged(ChangeEvent event) {
                    removeAll();
                    setup();
                }
            };
            getSequence().getStructureChangeSupport().addChangeListener(structureChangeListener);
        }

        private void setup() {
            for (int t = 0; t < getSequence().getMidiTrackCount(); t++) {
                MidiTrack track = getSequence().getMidiTrack(t);
                if (track.getChannel() < 0 && t > 0) continue;
                add(new TrackButton(track));
            }
        }

        private class TrackButton extends JButton implements ActionListener, PropertyChangeListener {

            private MidiTrack track;

            public TrackButton(MidiTrack track) {
                super(track.getTrackShortName());
                this.track = track;
                setToolTipText(track.getTrackName());
                setSelected(isVisibleTrack(track));
            }

            public void setSelected(boolean sel) {
                super.setSelected(sel);
                updateColors(sel);
                setVisibleTrack(track, sel);
            }

            protected void updateColors(boolean sel) {
                float hue = (Float) track.getClientProperty("Hue");
                Color c = MidiColor.asHSB(hue, sel ? 0.342f : 1.0f, 1.0f);
                setBackground(sel ? c : TrackBar.this.getBackground());
                setForeground(sel ? TrackBar.this.getForeground() : c);
            }

            public void actionPerformed(ActionEvent e) {
                setSelected(!isSelected());
            }

            public void addNotify() {
                super.addNotify();
                addActionListener(this);
                track.getPropertyChangeSupport().addPropertyChangeListener(track.propertyName(TRACK_NAME), this);
            }

            public void removeNotify() {
                track.getPropertyChangeSupport().removePropertyChangeListener(track.propertyName(TRACK_NAME), this);
                removeActionListener(this);
                super.removeNotify();
            }

            public void propertyChange(PropertyChangeEvent evt) {
                setText(track.getTrackShortName());
                setToolTipText(track.getTrackName());
                updateColors(isSelected());
            }
        }
    }

    protected class TimeZoomBar extends TootBar {

        private float TIMEZOOMFACTOR = 1.4f;

        private String TZOOMOUT = "Time Zoom Out";

        private String TZOOMIN = "Time Zoom In";

        public TimeZoomBar() {
            super(getSequence().getName());
            add(makeButton("general/ZoomOut16", TZOOMOUT, TZOOMOUT, TZOOMOUT, true));
            add(makeButton("general/ZoomIn16", TZOOMIN, TZOOMIN, TZOOMIN, true));
        }

        protected void zoomInTime() {
            int value = (int) (timeBar.getValue() * TIMEZOOMFACTOR);
            setTicksPerPixel(getTicksPerPixel() / TIMEZOOMFACTOR);
            timeBar.setValue(value);
        }

        protected void zoomOutTime() {
            int value = (int) (timeBar.getValue() / TIMEZOOMFACTOR);
            setTicksPerPixel(getTicksPerPixel() * TIMEZOOMFACTOR);
            timeBar.setValue(value);
        }

        public void actionPerformed(ActionEvent e) {
            String cmd = e.getActionCommand();
            if (TZOOMIN.equals(cmd)) {
                zoomInTime();
            } else if (TZOOMOUT.equals(cmd)) {
                zoomOutTime();
            }
        }
    }

    /**
     * InfoBar inner class
     */
    protected class InfoBar extends TootBar {

        public InfoBar() {
            super(getSequence().getName() + " Information");
            add(new JLabel(Midi.timePosition(getSequence().getMicrosecondLength()) + "  "));
            addSeparator();
            add(new JLabel(getSequence().getTickLength() + " Ticks  "));
            addSeparator();
            add(new JLabel(getSequence().getResolution() + " TPQ  "));
            addSeparator();
            add(new JLabel(getSequence().getBeatCount() + " Beats  "));
            addSeparator();
        }
    }
}
