package imp.gui;

import imp.Constants;
import imp.ImproVisor;
import imp.com.*;
import imp.data.*;
import imp.util.ErrorLog;
import imp.util.Trace;
import java.awt.Cursor;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import javax.swing.Icon;
import polya.Polylist;

/**
 * Initializes mouse and keyboard actions for a Stave.
 *
 * @author Aaron Wolin, Stephen Jones, Bob Keller (revised from code by Andrew Brown)
 * @version 1.0, 11 July 2005
 */
public class StaveActionHandler implements Constants, MouseListener, MouseMotionListener, KeyListener {

    /**
 * bias estimate for parallax
 * so that default is kept at 0
 */
    private static int parallaxBias = 1;

    /**
 * magic offset for bass-only staves
 */
    private static final int bassOnlyOffset = 48;

    /**
 * Shifts the mouse clicked position slightly for click accuracy
 */
    private static final int verticalAdjustment = 2;

    /**
 * Threshold for determining if additional vertical drag has occurred
 */
    private static int VERTICAL_DRAG_THRESHOLD = 1;

    Note lastAdviceNote;

    Chord lastAdviceChord;

    Chord lastAdviceNext;

    /**
 * This is used in mapping Y offsets to pitches.
 * The accidental is based on the key signature.
 */
    public static final int pitchFromSpacing[] = { c4, d4, e4, f4, g4, a4, b4, c5 };

    public static final int allPitchesFromSpacing[] = { c4, cs4, d4, ds4, e4, f4, fs4, g4, gs4, a4, as4, b4, c5 };

    /**
 * The stave to set all of the actions to
 */
    private Stave stave;

    /**
 * The notation window for the Stave
 */
    private Notate notate;

    /**
 * What single index is currently selected
 */
    private int selectedIndex = OUT_OF_BOUNDS;

    /**
 * What beat is currently selected
 */
    private int selectedBeat = OUT_OF_BOUNDS;

    /**
 * What measure is the mouse currently over
 */
    private int selectedMeasure = OUT_OF_BOUNDS;

    /**
 * The last measure the mouse was over
 */
    private int lastMeasureSelected = OUT_OF_BOUNDS;

    /**
 * Value for if the time signature is selected
 */
    private boolean timeSelected = false;

    /**
 * Value for if the key signature is selected
 */
    private boolean keySelected = false;

    /**
 * Last x position to have been clicked
 */
    private int clickedPosX;

    /**
 * Last y position to have been clicked
 */
    private int clickedPosY;

    /**
 * The current line the mouse is on
 */
    private int currentLine;

    /**
 * Last pitch to have been entered
 */
    private Note storedNote = null;

    /**
 * Indicades whether button1 has been clicked
 */
    private boolean button1Down = false;

    /**
 * Indicates if the last mouse click was on a construction line
 */
    private boolean clickedOnCstrLine = false;

    /**
 * Indicates if the last mouse click was on a beat bracket
 */
    private boolean clickedOnBracket = false;

    /**
 * The starting index of a dragging note
 */
    private int startingIndex = OUT_OF_BOUNDS;

    /**
 * Flag for if you can drag a note's pitch or not
 */
    private boolean draggingPitch = false;

    /**
 * Flag for if you can drag a note or not
 */
    private boolean draggingNote = false;

    /**
 * Flag for if you can drag a group of notes or not
 */
    private boolean draggingGroup = false;

    private int draggingGroupOffset = 0;

    private int draggingGroupOrigSelectionStart = 0;

    private int draggingGroupOrigSelectionEnd = 0;

    /**
 * Flag for if you are currently dragging the selection box handles
 */
    private boolean draggingSelectionHandle = false;

    /**
 * Directional Flag for which handle is being dragged (true: left, false: right)
 */
    private boolean draggingSelectionHandleLeft = false;

    /**
 * Locks the dragging of a note to either pitch or position
 */
    private boolean lockDragging = false;

    /**
 * Flag for if the note is being dragged for the first time
 */
    private boolean firstDrag = false;

    /**
 * The starting x-axis position for dragging
 */
    private int startDragX = OUT_OF_BOUNDS;

    /**
 * The starting y-axis position for dragging
 */
    private int startDragY = OUT_OF_BOUNDS;

    /**
 * The ending x-axis position for dragging
 */
    private int endDragX = OUT_OF_BOUNDS;

    /**
 * The ending y-axis position for dragging
 */
    private int endDragY = OUT_OF_BOUNDS;

    /**
 * The most recent y-axis position for dragging
 */
    private int lastDragY = OUT_OF_BOUNDS;

    /**
 * The lowest slot index encountered during dragging
 */
    private int dragMin;

    /**
 * The highest slot index encountered during dragging
 */
    private int dragMax;

    /**
 * Flag for if the user is selecting notes
 */
    private boolean selectingGroup = false;

    /**
 * Flag for if the user is drawing a contour line
 */
    private boolean drawing = false;

    /**
 * Last index drawn with contour tool
 */
    private int lastIndexDrawn = OUT_OF_BOUNDS;

    /**
 * First index drawn with contour rool
 **/
    private int firstIndexDrawn = OUT_OF_BOUNDS;

    /**
 * Was the last tone added an approach tone?
 */
    private boolean lastToneApproach = false;

    /**
 * What was the last index approached?
 */
    private int lastIndexApproached = OUT_OF_BOUNDS;

    /**
 * Last approach drawn
 */
    private int lastApproachPitch = OUT_OF_BOUNDS;

    /**
 * Last point drawn with contour tool; for use with 'flat-lining' a curve
 * to extend a note's duration;
 */
    private Point lastPointDrawn = null;

    /**
 * Indices added during a draw stroke.  Any untriggered note additions
 * (due to fast mousing) will be determined by the list and redrawn on
 * release
 */
    private java.util.List<Integer> firedIndices = new ArrayList<Integer>();

    /**
 * Line that we're drawing on; we are restricted to drawing on a single line
 * per stroke.
 */
    private int drawingLine = OUT_OF_BOUNDS;

    /**
 * Flags for allowable tones in fitting notes to a drawn contour.
 */
    private boolean drawScaleTones = true;

    private boolean drawChordTones = true;

    private boolean drawColorTones = false;

    /**
 * Left bound of curve
 */
    private int curveLeftBound;

    /**
 * Right bound of curve
 */
    private int curveRightBound;

    /**
 * What was the last change on the x-axis of the curve?  This is used
 * to determine when a curve doubles back on itself and violates itself functionality
 */
    private int oldDiff;

    /**
 * Flag for if the user is shift-clicking
 */
    private boolean shiftClicking = false;

    /**
 * Flag for if the user is holding 'a' down - used for contour drawing
 */
    private boolean aPressed = false;

    /**
 * Cursors
 */
    private Cursor defaultCursor = new Cursor(Cursor.DEFAULT_CURSOR);

    private Cursor crosshair = new Cursor(Cursor.CROSSHAIR_CURSOR);

    private Cursor resizeEastCursor = new Cursor(Cursor.E_RESIZE_CURSOR);

    private Cursor resizeWestCursor = new Cursor(Cursor.W_RESIZE_CURSOR);

    private Cursor moveCursor = new Cursor(Cursor.MOVE_CURSOR);

    private Cursor penCursor = null;

    private Cursor noteCursor = null;

    /**
 * Location of cursor during last mouseMove event, used to detect
 * when the cursor is inside hotspot rectangles such as the selection handles
 */
    private Point cursorLocation = new Point(-1, -1);

    /**
 * Flag for whether the handles should be displayed, when the flag changes
 * a repaint is needed
 */
    private boolean overHandles = false;

    private Cursor makeCursor(String filename, String cursorName, boolean offset) {
        Toolkit toolkit = Toolkit.getDefaultToolkit();
        Icon icon = new javax.swing.ImageIcon(getClass().getResource(filename));
        BufferedImage bi = new BufferedImage(icon.getIconWidth(), icon.getIconHeight(), BufferedImage.TYPE_INT_ARGB);
        icon.paintIcon(null, bi.getGraphics(), 0, 0);
        Point hotspot = offset ? new Point(0, icon.getIconHeight() - 1) : new Point(0, icon.getIconHeight() / 2);
        return toolkit.createCustomCursor(bi, hotspot, cursorName);
    }

    /**
 * Constructs an action handler for a particular stave
 *
 * @param stave             the Stave for which to set the actions to
 * @param notate            the notation window for the Stave
 */
    StaveActionHandler(Stave stave, Notate notate) {
        this.stave = stave;
        this.notate = notate;
        penCursor = makeCursor("graphics/toolbar/pencilCursor.png", "Pencil", true);
        noteCursor = defaultCursor;
    }

    /**
 * Mouse entereds
 */
    public void mouseEntered(MouseEvent e) {
    }

    /**
 * Mouse exited
 */
    public void mouseExited(MouseEvent e) {
    }

    public void maybeSetCursor(MouseEvent e) {
        if (stave.getShowSheetTitle()) {
            if (stave.sheetTitleEditor.checkEvent(e) || stave.sheetComposerEditor.checkEvent(e) || stave.showTitleEditor.checkEvent(e) || stave.yearEditor.checkEvent(e)) {
                setCursor();
            }
        }
        if (stave.getShowPartTitle()) {
            if (stave.partTitleEditor.checkEvent(e) || stave.partComposerEditor.checkEvent(e)) {
                setCursor();
            }
        }
    }

    /**
 * Mouse moved
 */
    public void mouseMoved(MouseEvent e) {
        Trace.log(2, "mouse moved " + e);
        cursorLocation = e.getPoint();
        boolean doRepaint = false;
        boolean withinNoteArea = inNoteArea(e);
        if (withinNoteArea) {
            setCursor(noteCursor);
        } else {
            maybeSetCursor(e);
        }
        if (withinNoteArea && stave.getShowMeasureCL() && !stave.getShowAllCL()) {
            stave.mouseOverMeasure = findMeasure(e);
            Trace.log(4, "mouse over measure " + stave.mouseOverMeasure + " last measure selected was " + lastMeasureSelected);
            if (stave.mouseOverMeasure != lastMeasureSelected) {
                stave.repaintLineFromCstrLine(lastMeasureSelected * stave.getMeasureLength());
                lastMeasureSelected = stave.mouseOverMeasure;
                stave.repaintLineFromCstrLine(lastMeasureSelected * stave.getMeasureLength());
            }
        }
        if (!drawing) {
            if (activeHandles() != overHandles) {
                overHandles = !overHandles;
                doRepaint = true;
            }
            if (mouseOverLHandle() && !stave.nothingSelected()) {
                setCursor(resizeWestCursor);
            } else if (mouseOverRHandle() && !stave.nothingSelected()) {
                setCursor(resizeEastCursor);
            } else {
                setCursor(noteCursor);
            }
        }
        if (doRepaint) {
            stave.repaint();
        }
    }

    boolean isDrawing() {
        return drawing;
    }

    boolean activeHandles() {
        if (!stave.getSelectionBoxDrawn()) {
            return false;
        }
        if (draggingSelectionHandle || stave.selectionLHandle.contains(cursorLocation) || stave.selectionRHandle.contains(cursorLocation)) {
            return true;
        }
        for (Rectangle r : stave.selectionBox) {
            if (r.contains(cursorLocation)) {
                return true;
            }
        }
        return false;
    }

    boolean mouseOverLHandle() {
        return stave.selectionLHandle.contains(cursorLocation);
    }

    boolean mouseOverRHandle() {
        return stave.selectionRHandle.contains(cursorLocation);
    }

    /**
 * Mouse clicked
 */
    public void mouseClicked(MouseEvent e) {
        clickedPosX = e.getX();
        clickedPosY = e.getY();
        if (!e.isShiftDown() && !e.isControlDown()) {
            currentLine = getCurrentLine(clickedPosY);
            selectedIndex = searchForCstrLine(clickedPosX, clickedPosY);
            if (selectedIndex != OUT_OF_BOUNDS) {
                stave.setSelection(selectedIndex);
                addNote(e, stave.getChordProg().getCurrentChord(selectedIndex));
                stave.repaint();
            }
        }
    }

    /**
 * The maximum duration a note should sound on entry.
 */
    private static int MAX_NOTE_ENTRY_LENGTH = BEAT / 2;

    public static int getEntryDuration(Note note) {
        return Math.max(0, Math.min(note.getRhythmValue(), MAX_NOTE_ENTRY_LENGTH) - 1);
    }

    /**
 * Add a note as determined by MouseEvent e.
 */
    private int addNote(MouseEvent e) {
        return addNote(e.getX(), e.getY());
    }

    /**
 * Add a note as determined by MouseEvent e.
 * Note that different methods are called, depending on whether or not there is a chord!
 */
    private int addNote(int x, int y) {
        return addNote(x, y, true);
    }

    /**
 * Add a note as determined by MouseEvent e.
 * Note that different methods are called, depending on whether or not there is a chord!
 */
    private int addNote(int x, int y, boolean play) {
        clearPasteFrom();
        int pitch = yPosToKeyPitch(y, currentLine);
        Note note = new Note(pitch);
        note.setEnharmonic(notate.getScore().getCurrentEnharmonics(selectedIndex));
        notate.cm.execute(new SetNoteCommand(selectedIndex, note, stave.getOrigPart()));
        Trace.log(2, "adding new note: " + note.toLeadsheet() + " at " + selectedIndex);
        draggingPitch = true;
        draggingNote = true;
        selectingGroup = false;
        redoAdvice(selectedIndex);
        int duration = getEntryDuration(note);
        notate.noCountIn();
        if (play) {
            stave.playSelection(selectedIndex, selectedIndex + duration, 0, false);
        }
        return note.getPitch();
    }

    /**
 * Add and play a note as determined by MouseEvent e, within a particular chordal context.
 */
    private int addNote(MouseEvent e, Chord chord) {
        return addNote(e, chord, true);
    }

    /**
 * Add a note as determined by MouseEvent e, within a particular chordal context.
 */
    private int addNote(MouseEvent e, Chord chord, boolean play) {
        return addNote(e.getX(), e.getY(), chord, e.isShiftDown(), play);
    }

    /**
 * Add a note as determined by MouseEvent e, within a particular chordal context.
 */
    private int addNote(int x, int y, Chord chord, boolean shiftDown, boolean play) {
        stave.setSelection(selectedIndex, selectedIndex);
        if (!notate.getSmartEntry()) {
            return addNote(x, y, play);
        }
        if (chord == null || chord.getName().equals("NC")) {
            return addNote(x, y, play);
        }
        ChordPart prog = stave.getChordProg();
        Polylist approachTones = new Polylist();
        drawScaleTones = stave.notate.getScaleTonesSelected();
        drawChordTones = stave.notate.getChordTonesSelected();
        if (!(drawScaleTones || drawChordTones || drawColorTones)) {
            return OUT_OF_BOUNDS;
        }
        boolean approachEnabled = (aPressed && shiftDown);
        boolean apprch = false;
        if (selectedIndex == lastIndexDrawn && selectedIndex == lastIndexApproached) {
            return stave.getOrigPart().getNote(selectedIndex).getPitch();
        }
        apprch = ((selectedIndex + stave.getOrigPart().getUnitRhythmValue(selectedIndex) == prog.getNextUniqueChordIndex(selectedIndex)) && approachEnabled);
        Chord nextChord = prog.getNextUniqueChord(selectedIndex);
        clearPasteFrom();
        int pitch = (lastToneApproach && !apprch) ? lastApproachPitch : yPosToAnyPitch(y - (notate.getParallax() + parallaxBias), currentLine);
        if (pitch < stave.getMinPitch()) {
            pitch = stave.getMinPitch();
        } else if (pitch > stave.getMaxPitch()) {
            pitch = stave.getMaxPitch();
        }
        int keysig = stave.getKeySignature();
        ChordForm form = chord.getChordSymbol().getChordForm();
        String root = chord.getRoot();
        Polylist scaleTones = form.getFirstScaleTones(root);
        Polylist chordTones = form.getSpell(root);
        Polylist colorTones = form.getColor(root);
        Polylist m = new Polylist();
        if (drawScaleTones) {
            m = m.append(scaleTones);
        }
        if (drawChordTones) {
            m = m.append(chordTones);
        }
        if (drawColorTones) {
        }
        if (apprch) {
            ChordForm nextForm = nextChord.getChordSymbol().getChordForm();
            Polylist approachList = nextForm.getApproach(nextChord.getRoot());
            Polylist tones = new Polylist();
            while (approachList.nonEmpty()) {
                tones = tones.append(((Polylist) approachList.first()).rest());
                approachList = approachList.rest();
            }
            if (tones.nonEmpty()) {
                m = tones;
                lastToneApproach = true;
            }
        }
        if (lastToneApproach && !apprch) {
            m = chordTones;
            lastToneApproach = (selectedIndex == lastIndexDrawn);
            lastIndexApproached = selectedIndex;
        }
        Note note = Note.getClosestMatch(pitch, m);
        pitch = note.getPitch();
        if (apprch) {
            lastApproachPitch = pitch;
        }
        notate.cm.execute(new SetNoteCommand(selectedIndex, note, stave.getOrigPart()));
        Trace.log(2, "adding new note over chord: " + note.toLeadsheet() + " at " + selectedIndex);
        draggingPitch = false;
        draggingNote = false;
        selectingGroup = false;
        redoAdvice(selectedIndex);
        notate.noCountIn();
        int duration = getEntryDuration(note);
        if (play) {
            stave.playSelection(selectedIndex, selectedIndex + duration, 0, false);
        }
        return pitch;
    }

    private int yPosToKeyPitch(int y, int currentLine) {
        int pitch = yPosToPitch(y - (notate.getParallax() + parallaxBias), currentLine);
        if (pitch < stave.getMinPitch()) {
            pitch = stave.getMinPitch();
        } else if (pitch > stave.getMaxPitch()) {
            pitch = stave.getMaxPitch();
        }
        int keysig = stave.getKeySignature();
        int adjustment = Key.adjustPitchInKey[keysig - MIN_KEY][pitch % OCTAVE];
        pitch += adjustment;
        return pitch;
    }

    /**
 * Basic contains method for an array of integers.
 * Returns the matched index, or -1 if no match.
 */
    private int arrayContains(int pitch, int[] pitches) {
        for (int i = 0; i < pitches.length; i++) {
            if (pitches[i] == pitch) {
                return i;
            }
        }
        return OUT_OF_BOUNDS;
    }

    /**
 * Clear the "paste from" selection.
 */
    protected void clearPasteFrom() {
        stave.setPasteFromStart(OUT_OF_BOUNDS);
        stave.setPasteFromEnd(OUT_OF_BOUNDS);
    }

    /**
 * Mouse pressed
 */
    public void mousePressed(MouseEvent e) {
        if (!inNoteArea(e)) {
            stave.unselectAll();
            stave.repaint();
            return;
        }
        stave.getCurvePoints()[e.getX()] = e.getY();
        if (notate.justPasted) {
            Trace.log(2, "just pasted");
            clearPasteFrom();
            notate.justPasted = false;
        }
        stave.requestFocusInWindow();
        maybeSetCursor(e);
        if ((e.getModifiersEx() & MouseEvent.BUTTON1_DOWN_MASK) != 0 && !stave.nothingSelected()) {
            if (mouseOverLHandle()) {
                draggingSelectionHandle = true;
                draggingSelectionHandleLeft = true;
                return;
            }
            if (mouseOverRHandle()) {
                draggingSelectionHandle = true;
                draggingSelectionHandleLeft = false;
                return;
            }
        }
        if (!selectingGroup && (e.getModifiersEx() & MouseEvent.BUTTON1_DOWN_MASK) != 0 && searchForBracket(e) == OUT_OF_BOUNDS) {
            Trace.log(2, "set up for dragging");
            startDragX = e.getX();
            startDragY = e.getY();
            lastDragY = startDragY;
            clickedPosX = e.getX();
            clickedPosY = e.getY();
            drawing = (notate.getMode() == Notate.Mode.DRAWING);
            selectingGroup = !drawing;
        }
        if (drawing) {
            startDragX = curveLeftBound = curveRightBound = e.getX();
            startDragY = e.getY();
            drawingLine = currentLine = getCurrentLine(e.getY());
            firedIndices = new ArrayList<Integer>();
        }
        if ((e.getModifiersEx() & MouseEvent.BUTTON1_DOWN_MASK) != 0 && !drawing) {
            Trace.log(2, "clicked in note area");
            currentLine = getCurrentLine(e.getY());
            clickedPosX = e.getX();
            clickedPosY = e.getY();
            selectedIndex = searchForCstrLine(e.getX(), e.getY());
            selectedBeat = searchForBracket(e);
            if (e.isControlDown() && !e.isShiftDown()) {
                Trace.log(2, "control (no shift) = requested popup");
                notate.popupMenu.show(e.getComponent(), e.getX(), e.getY());
                stave.currentLine = getCurrentLine(e.getY());
                selectingGroup = false;
                return;
            } else if (selectedIndex != OUT_OF_BOUNDS) {
                if (e.isShiftDown() && e.isControlDown()) {
                    stave.setSelection(selectedIndex);
                    if (e.isControlDown()) {
                        Trace.log(2, "shift-control: adding rest");
                        notate.addRest();
                    }
                    selectingGroup = false;
                    clickedOnCstrLine = true;
                    stave.repaint();
                    return;
                } else if (e.isShiftDown() && stave.nothingSelected()) {
                    Trace.log(2, "shift: single line selected");
                    stave.setSelection(selectedIndex);
                    selectingGroup = false;
                    clickedOnCstrLine = true;
                    stave.repaint();
                    return;
                }
                if (e.isShiftDown() && stave.somethingSelected()) {
                    boolean selectionLocked = (stave.getLockSelectionWidth() != -1);
                    if (selectedIndex < stave.getSelectionStart() || selectionLocked) {
                        Trace.log(2, "shift: extending selection to the left");
                        stave.setSelectionStart(selectedIndex);
                    } else if (selectedIndex > stave.getSelectionEnd()) {
                        Trace.log(2, "shift: extending selection to the right");
                        stave.setSelectionEnd(selectedIndex);
                    } else {
                        stave.setSelectionStart(selectedIndex);
                        stave.setSelectionEnd(selectedIndex);
                    }
                    shiftClicking = true;
                    selectingGroup = false;
                } else if (selectedIndex >= stave.getSelectionStart() && selectedIndex <= stave.getSelectionEnd()) {
                    Trace.log(2, "pressing within an existing selection, maybe going to drag");
                    draggingGroup = true;
                    draggingGroupOrigSelectionStart = stave.getSelectionStart();
                    draggingGroupOrigSelectionEnd = stave.getSelectionEnd();
                    draggingGroupOffset = (selectedIndex - draggingGroupOrigSelectionStart);
                    selectingGroup = false;
                }
                ChordPart chordProg = stave.getChordProg();
                if (chordProg != null) {
                    Chord chord = chordProg.getCurrentChord(selectedIndex);
                    if (chord != null && !chord.isNOCHORD()) {
                        redoAdvice(selectedIndex);
                    }
                }
                stave.repaint();
                return;
            }
            int theTimeSpace = stave.leftMargin + stave.clefWidth + stave.keySigWidth;
            if ((e.getX() > theTimeSpace) && (e.getX() < theTimeSpace + 10) && (e.getY() < stave.headSpace + stave.lineSpacing)) {
                timeSelected = true;
                clickedPosX = e.getX();
                clickedPosY = e.getY();
            }
            int theClefSpace = stave.leftMargin + stave.clefWidth;
            int minKeySpace = 10;
            if (stave.keySigWidth > minKeySpace) {
                minKeySpace = stave.keySigWidth;
            }
            if ((e.getX() > theClefSpace - 10) && (e.getX() < theClefSpace + minKeySpace) && (e.getY() < stave.headSpace + stave.lineSpacing)) {
                keySelected = true;
                clickedPosX = e.getX();
                clickedPosY = e.getY();
            }
        } else if ((e.getModifiersEx() & MouseEvent.BUTTON3_DOWN_MASK) != 0) {
            currentLine = getCurrentLine(e.getY());
            notate.popupMenu.show(e.getComponent(), e.getX(), e.getY());
            stave.currentLine = getCurrentLine(e.getY());
        }
    }

    public boolean getDraggingSelection() {
        return draggingSelectionHandle;
    }

    /**
 * Mouse dragged
 */
    public void mouseDragged(MouseEvent e) {
        Trace.log(4, "mouse dragged in StaveActionHandler");
        if ((e.getModifiersEx() & MouseEvent.BUTTON1_DOWN_MASK) == 0) {
            return;
        }
        int nearestLine = searchForCstrLine(e.getX(), e.getY());
        if (inNoteArea(e) && stave.getShowMeasureCL()) {
            stave.mouseOverMeasure = findMeasure(e);
            if (stave.mouseOverMeasure != lastMeasureSelected) {
                stave.repaintLineFromCstrLine(lastMeasureSelected * stave.getMeasureLength());
                lastMeasureSelected = stave.mouseOverMeasure;
                stave.repaintLineFromCstrLine(lastMeasureSelected * stave.getMeasureLength());
            }
        }
        boolean selectionLocked = (stave.getLockSelectionWidth() != -1);
        if (!drawing && draggingSelectionHandle) {
            if (nearestLine != OUT_OF_BOUNDS) {
                int start = stave.getSelectionStart();
                int end = stave.getSelectionEnd();
                if (draggingSelectionHandleLeft) {
                    if (nearestLine <= end || selectionLocked) {
                        stave.setSelectionStart(nearestLine);
                    } else {
                        stave.setSelectionEnd(nearestLine);
                        stave.setSelectionStart(end);
                        draggingSelectionHandleLeft = false;
                    }
                } else {
                    if (selectionLocked) {
                        stave.setLockedSelectionEnd(nearestLine);
                    } else if (nearestLine >= start) {
                        stave.setSelectionEnd(nearestLine);
                    } else {
                        stave.setSelectionStart(nearestLine);
                        stave.setSelectionEnd(start);
                        draggingSelectionHandleLeft = true;
                    }
                }
                stave.repaint();
            }
            return;
        }
        if (selectingGroup && !timeSelected && !keySelected && !draggingPitch && !draggingNote && !draggingGroup && !shiftClicking && !drawing) {
            Trace.log(2, "point A");
            stave.setCursor(crosshair);
            if (e.getX() > startDragX) {
                if (e.getY() > startDragY) {
                    stave.getGraphics().drawRect(startDragX, startDragY, e.getX() - startDragX, e.getY() - startDragY);
                } else {
                    stave.getGraphics().drawRect(startDragX, e.getY(), e.getX() - startDragX, startDragY - e.getY());
                }
            } else {
                if (e.getY() > startDragY) {
                    stave.getGraphics().drawRect(e.getX(), startDragY, startDragX - e.getX(), e.getY() - startDragY);
                } else {
                    stave.getGraphics().drawRect(e.getX(), e.getY(), startDragX - e.getX(), startDragY - e.getY());
                }
            }
            stave.repaint();
        } else if (drawing) {
            try {
                int x = e.getX();
                int y = e.getY();
                if ((currentLine = getCurrentLine(y)) < drawingLine) {
                    currentLine = drawingLine;
                    y = drawingLine * stave.lineSpacing + stave.headSpace - stave.lineOffset;
                } else if (currentLine > drawingLine) {
                    currentLine = drawingLine;
                    y = (drawingLine + 1) * stave.lineSpacing + stave.headSpace - stave.lineOffset;
                }
                int newPitch = yPosToPitch(y, currentLine);
                selectedIndex = searchForCstrLine(x, y);
                if (e.isShiftDown() && !aPressed && lastPointDrawn != null) {
                    y += lastPointDrawn.y - y;
                } else {
                    currentLine = getCurrentLine(y);
                }
                Note lastDrawnNote = stave.getOrigPart().getNote(lastIndexDrawn);
                if ((selectedIndex != lastIndexDrawn || (lastDrawnNote != null && lastDrawnNote.getPitch() != newPitch)) && selectedIndex != OUT_OF_BOUNDS) {
                    if (firedIndices.size() == 0 || (selectedIndex != lastIndexDrawn && selectedIndex != OUT_OF_BOUNDS && selectedIndex != firedIndices.get(firedIndices.size() - 1))) {
                        firedIndices.add(selectedIndex);
                    }
                    if (e.isControlDown()) {
                        notate.cm.execute(new SetRestCommand(selectedIndex, stave.getOrigPart()));
                    } else {
                        addNote(e, stave.getChordProg().getCurrentChord(selectedIndex), false);
                    }
                    if (firstIndexDrawn == OUT_OF_BOUNDS) {
                        firstIndexDrawn = selectedIndex;
                        stave.setSelectionStart(firstIndexDrawn);
                        stave.setSelectionEnd(firstIndexDrawn);
                    }
                    lastPointDrawn = new Point(x, y);
                    lastIndexDrawn = selectedIndex;
                    if (selectedIndex != OUT_OF_BOUNDS && e.isShiftDown() && selectedIndex >= 1 && !aPressed) {
                        notate.cm.execute(new DeleteUnitsCommand(stave.getOrigPart(), selectedIndex, selectedIndex));
                    }
                    if (stave.getSelectionStart() > selectedIndex) {
                        stave.setSelectionStart(selectedIndex);
                    }
                    if (stave.getSelectionEnd() < selectedIndex) {
                        stave.setSelectionEnd(selectedIndex);
                    }
                }
                Point p = new Point(x, y);
                if (p.x < curveLeftBound) {
                    curveLeftBound = p.x;
                }
                if (p.x > curveRightBound) {
                    curveRightBound = p.x;
                }
                int newDiff = p.x - startDragX;
                if ((newDiff > 0 && oldDiff < 0) || (newDiff < 0 && oldDiff > 0)) {
                }
                oldDiff = ((newDiff != 0) ? newDiff : oldDiff);
                int[] curve = stave.getCurvePoints();
                if (p.x < curve.length && p.x >= 0) {
                    curve[p.x] = p.y;
                    if (p.x - startDragX > 1) {
                        for (int i = startDragX + 1; i < p.x; i++) {
                            curve[i] = curve[startDragX] + (i - startDragX) * (p.y - startDragY) / (p.x - startDragX);
                        }
                    } else {
                        for (int i = startDragX - 1; i > p.x; i--) {
                            curve[i] = curve[startDragX] + (i - startDragX) * (p.y - startDragY) / (p.x - startDragX);
                        }
                    }
                    startDragX = p.x;
                    startDragY = p.y;
                }
                draggingNote = false;
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            stave.repaint();
        } else {
            setCursor(moveCursor);
        }
        Trace.log(2, "point B");
        if ((e.getX() > clickedPosX + 3 || e.getX() < clickedPosX - 3) && !lockDragging && !drawing) {
            Trace.log(2, "point B1");
            draggingPitch = false;
            lockDragging = true;
            draggingNote = true;
            dragMin = stave.getSelectionStart();
            dragMax = stave.getSelectionEnd();
        } else if ((e.getY() > clickedPosY + 3 || e.getY() < clickedPosY - 3) && !lockDragging && !drawing) {
            Trace.log(2, "point B2");
            draggingNote = false;
            lockDragging = true;
            draggingPitch = true;
        }
        if (draggingNote) {
            int start = stave.getSelectionStart();
            int end = stave.getSelectionEnd();
            if (start < dragMin) {
                dragMin = start;
            }
            if (end > dragMax) {
                dragMax = end;
            }
        }
        if (selectedBeat != OUT_OF_BOUNDS && !drawing) {
            Trace.log(2, "point C");
            if (e.getX() > clickedPosX + 15) {
                int newSubDivs = stave.incSubDivs(selectedBeat);
                if (newSubDivs != stave.getSubDivs(selectedBeat) && newSubDivs <= 12) {
                    stave.setSubDivs(selectedBeat, newSubDivs);
                    stave.repaint();
                }
                clickedPosX = e.getX();
            }
            if (e.getX() < clickedPosX - 15) {
                int newSubDivs = stave.decSubDivs(selectedBeat);
                if (newSubDivs != stave.getSubDivs(selectedBeat)) {
                    stave.setSubDivs(selectedBeat, newSubDivs);
                    stave.repaint();
                }
                clickedPosX = e.getX();
            }
            return;
        } else if (selectedIndex != OUT_OF_BOUNDS && stave.getOrigPart().getNote(stave.getSelectionStart()) != null && draggingPitch && !drawing) {
            if (e.getY() > lastDragY + VERTICAL_DRAG_THRESHOLD) {
                if (e.isAltDown()) {
                    stave.transposeMelodyDownHarmonically();
                } else {
                    stave.transposeMelodyDownSemitone();
                }
            } else if (e.getY() < lastDragY - VERTICAL_DRAG_THRESHOLD) {
                if (e.isAltDown()) {
                    stave.transposeMelodyUpHarmonically();
                } else {
                    stave.transposeMelodyUpSemitone();
                }
            }
            lastDragY = e.getY();
            Trace.log(2, "point D");
            MelodyPart part = stave.getOrigPart();
            int index = stave.getSelectionStart();
            Note note = part.getNote(index);
            storedNote = note;
            stave.repaint();
            return;
        } else if (startingIndex != OUT_OF_BOUNDS && draggingNote) {
            Trace.log(2, "point E");
            if (nearestLine != OUT_OF_BOUNDS) {
                if (firstDrag == false) {
                    firstDrag = true;
                } else {
                    Trace.log(2, "undo command in StaveActionHandler");
                    notate.cm.undo();
                }
                notate.cm.execute(new DragNoteCommand(stave.getOrigPart(), startingIndex, nearestLine, true));
                Trace.log(2, "point K");
                stave.repaint();
                return;
            }
        } else if (nearestLine != OUT_OF_BOUNDS && selectedIndex != OUT_OF_BOUNDS && draggingGroup) {
            Trace.log(2, "point F");
            int pasteIndex = nearestLine - draggingGroupOffset;
            if (pasteIndex < 0) {
                pasteIndex = 0;
            }
            if (pasteIndex < stave.getOrigPart().size()) {
                if (firstDrag == false) {
                    firstDrag = true;
                } else {
                    Trace.log(2, "undo command in StaveActionHandler");
                    notate.cm.undo();
                }
                notate.cm.execute(new DragSetCommand(stave.getOrigPart(), draggingGroupOrigSelectionStart, draggingGroupOrigSelectionEnd, pasteIndex));
                stave.setSelection(pasteIndex, pasteIndex + stave.getSelectionLength());
                stave.repaint();
            } else {
                System.out.println("Internal error with dragging group");
            }
        }
        if (timeSelected) {
            if (e.getY() < clickedPosY - 4) {
                int top = stave.getMetre()[0];
                int bottom = stave.getMetre()[1];
                if (top + 1 > 12 && bottom < 8) {
                    stave.setMetre(1, bottom * 2);
                } else if (top + 1 > 12 && bottom == 8) {
                    stave.setMetre(12, 8);
                } else if (top + 1 < 1 && bottom > 1) {
                    stave.setMetre(1, bottom / 2);
                } else if (top + 1 < 1 && bottom == 1) {
                    stave.setMetre(1, 1);
                } else {
                    stave.setMetre(top + 1, bottom);
                }
                clickedPosY -= 4;
                stave.repaint();
            }
            if (e.getY() > clickedPosY + 4) {
                int top = stave.getMetre()[0];
                int bottom = stave.getMetre()[1];
                if (top - 1 < 1 && bottom > 1) {
                    stave.setMetre(12, bottom / 2);
                } else if (top - 1 < 1 && bottom == 1) {
                    stave.setMetre(1, 1);
                } else if (top - 1 > 12 && bottom < 8) {
                    stave.setMetre(1, bottom * 2);
                } else if (top - 1 > 12 && bottom == 8) {
                    stave.setMetre(12, 8);
                } else {
                    stave.setMetre(top - 1, bottom);
                }
                clickedPosY += 4;
                stave.repaint();
            }
        }
        if (keySelected) {
            int keySig = stave.getKeySignature();
            if (e.getY() < clickedPosY - 4) {
                if (stave.getKeySignature() < MAX_KEY) {
                    stave.setKeySignature(keySig + 1);
                }
                clickedPosY -= 4;
                stave.repaint();
            }
            if (e.getY() > clickedPosY + 4) {
                if (stave.getKeySignature() > MIN_KEY) {
                    stave.setKeySignature(keySig - 1);
                }
                clickedPosY += 4;
                stave.repaint();
            }
        }
    }

    void redoAdvice(int selectedIndex) {
        if (!ImproVisor.getShowAdvice()) {
            return;
        }
        {
            if (selectedIndex < 0) {
                return;
            }
            Chord currentChord = stave.getChordProg().getCurrentChord(selectedIndex);
            if (currentChord == null || currentChord.getName().equals(NOCHORD)) {
                ImproVisor.setShowAdvice(false);
                Notate window = ImproVisor.getCurrentWindow();
                if (window != null) {
                    window.closeAdviceFrame();
                }
                notate.setStatus("To get advice, there must be a chord in effect.");
                return;
            }
            Chord currentNext = stave.getChordProg().getNextUniqueChord(selectedIndex);
            Note currentNote = stave.getOrigPart().getNote(selectedIndex);
            if (((lastAdviceChord == null) || (currentChord != null && !(currentChord.getName().equals(lastAdviceChord.getName())))) || ((lastAdviceNext == null) || (currentNext != null && !(currentNext.getName().equals(lastAdviceNext.getName())))) || ((lastAdviceNote == null) || (currentNote != null))) {
                int row = notate.adviceTree.getMaxSelectionRow();
                notate.displayAdviceTree(selectedIndex, 0, currentNote);
            }
            lastAdviceChord = currentChord;
            lastAdviceNext = currentNext;
            lastAdviceNote = currentNote;
        }
    }

    /**
 * Mouse released
 */
    public void mouseReleased(MouseEvent e) {
        Trace.log(2, "mouse released " + e + " in Stave");
        stave.requestFocusInWindow();
        if (draggingNote) {
        }
        if (timeSelected) {
            notate.initMetreAndLength(stave.getMetre()[0], stave.getMetre()[1], false);
        }
        if (draggingSelectionHandle) {
            draggingSelectionHandle = false;
            return;
        }
        int nearestLine = searchForCstrLine(e.getX(), e.getY());
        if (!shiftClicking && nearestLine == selectedIndex && !drawing) {
            stave.requestFocusInWindow();
            stave.repaint();
        } else if (selectingGroup && !timeSelected && !keySelected && !draggingPitch && !draggingNote && !draggingGroup && !drawing) {
            Trace.log(2, "point G");
            endDragX = e.getX();
            endDragY = e.getY();
            if (endDragX < startDragX) {
                int tempX = endDragX;
                endDragX = startDragX;
                startDragX = tempX;
            }
            boolean selectionStarted = false;
            int tempStart = OUT_OF_BOUNDS;
            int tempEnd = OUT_OF_BOUNDS;
            int endingWithTie = OUT_OF_BOUNDS;
            for (int i = 0; i < stave.cstrLines.length; i++) {
                if (stave.cstrLines[i] != null) {
                    if (!selectionStarted && startDragY < stave.cstrLines[i].getY() + 80 && startDragY > stave.cstrLines[i].getY() - 80 && stave.cstrLines[i].getX() >= startDragX - 5) {
                        tempStart = i;
                        selectionStarted = true;
                    }
                    if (selectionStarted && endDragY < stave.cstrLines[i].getY() + 80 && endDragY > stave.cstrLines[i].getY() - 80 && stave.cstrLines[i].getX() <= endDragX) {
                        tempEnd = i;
                    }
                }
            }
            if (tempStart != OUT_OF_BOUNDS && tempEnd != OUT_OF_BOUNDS && tempStart <= tempEnd) {
                Trace.log(2, "point I");
                stave.setSelection(tempStart, tempEnd);
                selectedIndex = stave.getSelectionStart();
            }
            stave.repaint();
        }
        if (drawing) {
            stave.clearCurvePoints();
            try {
                if (firedIndices.size() > 1) {
                    fitUnfiredNotes();
                }
            } catch (Exception j) {
                ErrorLog.log(ErrorLog.WARNING, j + ": Couldn't retrofit.");
            }
            firstIndexDrawn = lastIndexDrawn = OUT_OF_BOUNDS;
            firedIndices = new ArrayList<Integer>();
            return;
        }
        selectingGroup = false;
        shiftClicking = false;
        selectedBeat = OUT_OF_BOUNDS;
        selectedIndex = OUT_OF_BOUNDS;
        clickedOnCstrLine = false;
        clickedOnBracket = false;
        timeSelected = false;
        keySelected = false;
        draggingPitch = false;
        draggingNote = false;
        draggingGroup = false;
        lockDragging = false;
        firstDrag = false;
        lastIndexApproached = OUT_OF_BOUNDS;
        notate.setItemStates();
        setCursor();
    }

    /**
 * Will march through the drawing, determine which slots didn't fire during
 * the note addition phase (e.g. if you moved the mouse too quickly and the
 * mouseDragged didn't fire at the given index), and try to fit those to the
 * curve.  The current implementation deletes those notes repeatedly until
 * only the 'fired' slots contain notes, who durations may now be longer.
 * This works to 'fit' to the curve.  Another future variation may try to
 * re-insert proper pitches at those outlying slots.
 */
    private void fitUnfiredNotes() {
        if (firedIndices.isEmpty() || firedIndices == null) {
            ErrorLog.log(ErrorLog.WARNING, "*** Warning: Trying to fit notes in an uninitialized or" + " empty curve.");
            return;
        } else {
            MelodyPart part = stave.getOrigPart();
            for (int i = 0; i < firedIndices.size(); i++) {
                int expectedSlot;
                int curSlot = firedIndices.get(i);
                int prevSlot = (i == 0 ? 0 : firedIndices.get(i - 1));
                boolean leftToRight = (curSlot - prevSlot) >= 0;
                if (i != 0) {
                    Note prevNote, curNote;
                    while ((prevNote = part.getNote(prevSlot)) != null && (curNote = part.getNote(curSlot)) != null && curSlot != (expectedSlot = (leftToRight ? prevSlot + prevNote.getRhythmValue() : prevSlot - curNote.getRhythmValue()))) {
                        while (part.getNote(expectedSlot) == null) {
                            expectedSlot--;
                        }
                        if (expectedSlot == 0) {
                            break;
                        }
                        try {
                            notate.cm.execute(new DeleteUnitsCommand(stave.getOrigPart(), expectedSlot, expectedSlot));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
        stave.repaint();
    }

    /**
 * Undo last action
 */
    public void undo() {
        notate.undoMIActionPerformed(null);
    }

    /**
 * Key pressed
 */
    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() != KeyEvent.VK_ENTER) {
            Trace.log(3, "Key event: " + e);
        }
        if (stave.nothingSelected()) {
            stave.setSelection(0);
        }
        notate.resetAdviceUsed();
        int subDivs = 2;
        if (stave.getSelectionStart() != OUT_OF_BOUNDS && stave.getSelectionEnd() != OUT_OF_BOUNDS) {
            if (e.isMetaDown()) {
                notate.controlDownBehavior(e);
            } else if (e.isControlDown()) {
                switch(e.getKeyCode()) {
                    case KeyEvent.VK_SPACE:
                        notate.toggleBothEnharmonics();
                        return;
                }
            } else if (e.isShiftDown()) {
                switch(e.getKeyCode()) {
                    case KeyEvent.VK_ENTER:
                        return;
                    case KeyEvent.VK_Z:
                        undo();
                        return;
                    case KeyEvent.VK_A:
                        aPressed = true;
                        return;
                    case KeyEvent.VK_SPACE:
                        notate.toggleChordEnharmonics();
                        stave.repaint();
                        return;
                }
            } else {
                switch(e.getKeyCode()) {
                    case KeyEvent.VK_ENTER:
                        return;
                    case KeyEvent.VK_A:
                        {
                            notate.moveLeft();
                        }
                        return;
                    case KeyEvent.VK_F:
                        {
                            notate.moveRight();
                        }
                        return;
                    case KeyEvent.VK_R:
                        notate.cm.execute(new SetRestCommand(stave.getSelectionStart(), stave.getOrigPart()));
                        stave.repaint();
                        return;
                    case KeyEvent.VK_DELETE:
                    case KeyEvent.VK_BACK_SPACE:
                        notate.cutBothMIActionPerformed(null);
                        stave.repaint();
                        return;
                    case KeyEvent.VK_SPACE:
                        notate.toggleMelodyEnharmonics();
                        stave.repaint();
                        return;
                    case KeyEvent.VK_ESCAPE:
                        notate.stopPlaying();
                        stave.unselectAll();
                        stave.repaint();
                        return;
                    case KeyEvent.VK_LEFT:
                        notate.fileStepBackward();
                        notate.staveRequestFocus();
                        return;
                    case KeyEvent.VK_RIGHT:
                        notate.fileStepForward();
                        notate.staveRequestFocus();
                        return;
                    default:
                        handleGridLineSpacing(e);
                }
            }
            stave.requestFocusInWindow();
        }
    }

    /**
 * Handle grid-line spacing short chuts.
 */
    public void handleGridLineSpacing(KeyEvent e) {
        switch(e.getKeyCode()) {
            case KeyEvent.VK_1:
            case KeyEvent.VK_NUMPAD1:
                setSubDivs(1);
                break;
            case KeyEvent.VK_2:
            case KeyEvent.VK_NUMPAD2:
                setSubDivs(2);
                break;
            case KeyEvent.VK_3:
            case KeyEvent.VK_NUMPAD3:
                setSubDivs(3);
                break;
            case KeyEvent.VK_4:
            case KeyEvent.VK_NUMPAD4:
                setSubDivs(4);
                break;
            case KeyEvent.VK_5:
            case KeyEvent.VK_NUMPAD5:
                setSubDivs(5);
                break;
            case KeyEvent.VK_6:
            case KeyEvent.VK_NUMPAD6:
                setSubDivs(6);
                break;
            case KeyEvent.VK_8:
            case KeyEvent.VK_NUMPAD8:
                setSubDivs(8);
                break;
        }
    }

    /**
 * set every beat touched to the selected subdivisions
 */
    void setSubDivs(int subDivs) {
        setSubDivs(subDivs, stave.getSelectionStart(), stave.getSelectionEnd());
        return;
    }

    /**
 * set every beat touched to the subdivisions in a range
 */
    void setSubDivs(int subDivs, int start, int end) {
        for (int i = start; i <= end; i += getBeatValue()) {
            stave.setSubDivs(i / getBeatValue(), subDivs);
        }
        stave.repaint();
        return;
    }

    /**
 * Key released
 */
    public void keyReleased(KeyEvent e) {
        notate.setItemStates();
        switch(e.getKeyCode()) {
            case KeyEvent.VK_A:
                if (e.isShiftDown() && aPressed) {
                    aPressed = false;
                }
                return;
            default:
                return;
        }
    }

    /**
 * Key typed
 */
    public void keyTyped(KeyEvent e) {
    }

    /**
 * Checks what line the y value corresponds to.
 *
 * @param y                 y-axis position
 * @return int              the line the mouse is currently on
 */
    private int getCurrentLine(int y) {
        if (y < stave.headSpace - stave.lineOffset) {
            return -1;
        }
        int currLine = (y - (stave.headSpace - stave.lineOffset)) / stave.lineSpacing;
        return currLine;
    }

    /**
 * Searches to see if the mouse has been clicked in the note area. The note
 * area consists of the width of the stave in the horizontal direction, and
 * a <code>lineSpacing</code> after the last stave line in the vertical
 * direction.
 *
 * @param e                 MouseEvent
 * @return boolean          true if the mouse is in the note area
 */
    boolean inNoteArea(MouseEvent e) {
        int lastX = stave.STAVE_WIDTH;
        int lastY = stave.headSpace + ((stave.staveLine + 1) * stave.lineSpacing);
        int extraSpaceAtBottom = 40;
        boolean result = (e.getX() >= stave.leftMargin) && (e.getX() <= lastX) && (e.getY() < lastY + extraSpaceAtBottom);
        return result;
    }

    /**
 * Searches to see if a construction line has been clicked on, returning the
 * selected slot if one has. The search algorithm looks in a small window of
 * pixels before and after the construction line in the x direction and 50
 * pixels above and below it's vertical midpoint.
 * <p>
 * The x-axis window is calculated by taking half of the construction line's
 * x-axis spacing (found using the getSpacing() method on the cstrLines
 * array) and subtracting 2 pixels from that value.
 * <p>
 * This allows the user to have a small amount of space (namely, 4 pixels),
 * in between each construction line that does not select anything. If that
 * space wasn't there, the sensitivity when selecting a construction line is
 * a little too low, and sometimes the user will select the wrong line by
 * accident.
 *
 * @param e             MouseEvent
 * @return int          the slot index corresponding to the construction
 *                      line clicked
 */
    @SuppressWarnings("static-access")
    private int searchForCstrLine(int x, int y) {
        int tempX = 0;
        int tempY = 0;
        for (int i = 0; i < stave.cstrLines.length; i++) {
            if (stave.cstrLines[i] != null) {
                tempX = stave.cstrLines[i].getX();
                tempY = stave.cstrLines[i].getY() - (stave.numPitchLines * stave.staveSpaceHeight) / 2;
                int xMargin = stave.cstrLines[i].getSpacing() / 2;
                int xLower = tempX - xMargin + 2;
                int xUpper = tempX + xMargin - 2;
                int yLower = tempY - stave.lineOffset;
                int yUpper = tempY - stave.lineOffset + stave.lineSpacing;
                if (x >= xLower && x <= xUpper && y > yLower && y < yUpper) {
                    clickedOnCstrLine = true;
                    return i;
                }
            }
        }
        return OUT_OF_BOUNDS;
    }

    /**
 * Searches to see if a beat bracket has been clicked on, returning the
 * selected beat if one has. The user must click on or very close to the
 * bracket image in order to select it. The window in which the user can
 * click is specified in this function as well, and is set to be 5 pixels
 * above the bracket image and 5 pixels below the bracket image
 *
 * @param e             MouseEvent
 * @return int          the beat corresponding to the bracket clicked
 */
    private int searchForBracket(MouseEvent e) {
        int tempX = 0;
        int tempY = 0;
        for (int i = 0; i < stave.cstrLines.length; i++) {
            if (stave.cstrLines[i] != null) {
                tempX = stave.cstrLines[i].getX();
                tempY = stave.cstrLines[i].getY() + (stave.numPitchLines * stave.staveSpaceHeight) / 2 + 20;
                if (e.getX() > tempX - (stave.cstrLines[i].getSpacing() / 2) && e.getX() < tempX + (stave.cstrLines[i].getSpacing() / 2) && e.getY() > tempY - 8 && e.getY() < tempY + 16) {
                    clickedOnBracket = true;
                    clickedPosX = e.getX();
                    clickedPosY = e.getY();
                    selectedBeat = i / getBeatValue();
                    return i / getBeatValue();
                }
            }
        }
        return OUT_OF_BOUNDS;
    }

    /**
 * Finds the measure that the mouse is currently over. Does this by finding
 * what construction line the mouse is closest to, and deducing the measure
 * from the construction line's index.
 *
 * @param e             MouseEvent
 * @return int          the current measure
 */
    private int findMeasure(MouseEvent e) {
        for (int i = 0; i < stave.cstrLines.length; i++) {
            if (stave.cstrLines[i] != null) {
                int tempX = stave.cstrLines[i].getX();
                int tempY = stave.cstrLines[i].getY();
                if ((e.getX() > tempX - stave.cstrLines[i].getSpacing()) && (e.getX() < tempX + stave.cstrLines[i].getSpacing()) && (e.getY() > tempY - 85) && (e.getY() < tempY + 85)) {
                    selectedMeasure = i / getMeasureLength();
                    return selectedMeasure;
                }
            }
        }
        return OUT_OF_BOUNDS;
    }

    /**
 * Finds the y-axis position for a given mouse position yPos and the current
 * line. First finds the y position's difference from middle C. It then
 * modulates the position by the amount of space between each octave (4
 * pitch lines) and uses if-then statements on the modulated difference to
 * find the given pitch. Adding the octaves back in and transposing the
 * note if necessary produces a correct pitch.
 * <p>
 * If the variable <code>staveSpaceHeight</code> ever changes, this method
 * will have to be altered as well.
 *
 * @param yPos          the current mouse position on the y-axis
 * @param currentLine   the current line the mouse is on
 * @return int          the pitch corresponding to the y-position
 */
    private int yPosToPitch(int yPos, int currentLine) {
        if (stave.getStaveType() == StaveType.BASS) {
            yPos += bassOnlyOffset;
        }
        int middleC = stave.headSpace + (currentLine * stave.lineSpacing) + ((10 * stave.staveSpaceHeight) / 2) - 1;
        int yDif = middleC - yPos + verticalAdjustment;
        int modYDif = yDif % ((4 * stave.staveSpaceHeight) - (stave.staveSpaceHeight / 2));
        if (yDif < 0) {
            modYDif = ((4 * stave.staveSpaceHeight) - (stave.staveSpaceHeight / 2)) + modYDif;
        }
        int index = (modYDif + 1) / 4;
        int pitch = pitchFromSpacing[index];
        int octaveDif = (int) Math.floor(yDif / 28) * 12;
        if (yDif < 0) {
            octaveDif -= 12;
        }
        pitch += octaveDif;
        return pitch;
    }

    /**
 * Like yPosToPitch, but maps to all 12 pitches, rather than the 7 natural
 * tones.
 */
    private int yPosToAnyPitch(int yPos, int currentLine) {
        if (stave.getStaveType() == StaveType.BASS) {
            yPos += bassOnlyOffset;
        }
        int middleC = stave.headSpace + (currentLine * stave.lineSpacing) + ((10 * stave.staveSpaceHeight) / 2);
        int yDif = middleC - yPos + verticalAdjustment;
        int modYDif = yDif % ((4 * stave.staveSpaceHeight) - (stave.staveSpaceHeight / 2));
        if (yDif < 0) {
            modYDif = ((4 * stave.staveSpaceHeight) - (stave.staveSpaceHeight / 2)) + modYDif;
        }
        int index = (modYDif + 1) / 2;
        if (index >= allPitchesFromSpacing.length) {
            index = allPitchesFromSpacing.length - 1;
        } else if (index < 0) {
            index = 0;
        }
        int pitch = allPitchesFromSpacing[index];
        int octaveDif = (int) Math.floor(yDif / 28) * 12;
        if (yDif < 0) {
            octaveDif -= 12;
        }
        pitch += octaveDif;
        return pitch;
    }

    /**
 * Moves the selected index to the right by one construction line
 * @param index             the index at which to start
 */
    public void moveSelectionRight(int index) {
        while (index < stave.getOrigPart().size() && stave.cstrLines[index] == null) {
            index++;
        }
        if (index < stave.getOrigPart().size() && stave.cstrLines[index] != null) {
            stave.setSelection(index);
            stave.repaint();
        }
    }

    /**
 * Moves the selected index to the left by one construction line
 * @param index             the index at which to start
 */
    public void moveSelectionLeft(int index) {
        while (index >= 0 && stave.cstrLines[index] == null) {
            index--;
        }
        if (index >= 0 && stave.cstrLines[index] != null) {
            stave.setSelection(index);
            stave.repaint();
        }
    }

    public void setCursor() {
        setCursor(defaultCursor);
    }

    public void setCursor(Cursor cursor) {
        switch(notate.getMode()) {
            case NORMAL:
            case RECORDING:
                stave.setCursor(cursor);
                break;
            case DRAWING:
                stave.setCursor(penCursor);
                break;
        }
    }

    public void setDrawScaleTones(boolean draw) {
        drawScaleTones = draw;
    }

    public void setDrawChordTones(boolean draw) {
        drawChordTones = draw;
    }

    public int getBeatValue() {
        return WHOLE / stave.getMetre()[1];
    }

    public int getMeasureLength() {
        return getBeatValue() * stave.getMetre()[0];
    }
}
