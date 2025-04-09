package org.formaria.swing.date;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.swing.ComboBoxEditor;
import javax.swing.JComboBox;
import javax.swing.JFormattedTextField;
import javax.swing.ListCellRenderer;
import javax.swing.border.EmptyBorder;
import javax.swing.text.DateFormatter;
import javax.swing.text.DefaultFormatterFactory;
import org.formaria.debug.DebugLogger;
import org.formaria.aria.TextHolder;
import org.formaria.aria.build.BuildProperties;

/**
 * A control for choosing the time-of-day
 *
 * <p> Copyright (c) Formaria Ltd., 2008, This software is licensed under
 * the GNU Public License (GPL), please see license.txt for more details. If
 * you make commercial use of this software you must purchase a commercial
 * license from Formaria.</p>
 * <p> $Revision: 1.2 $</p>
 */
public class TimeChooser extends JComboBox implements TextHolder {

    /**
   * Pick a time of day
   */
    public static final int TIME_MODE = 0;

    /**
   * Pick a time for ending an event, after a spoecified start date
   */
    public static final int END_TIME_MODE = 1;

    /**
   * Pick a length of time
   */
    public static final int DURATION_MODE = 2;

    public static final int STEP_HOURS = 0;

    public static final int STEP_HALF_HOURS = 1;

    public static final int STEP_QUATER_HOURS = 2;

    private boolean use24Clock;

    private int mode;

    private int step = STEP_HALF_HOURS;

    private ComboDateEditor editor;

    private Date startDate;

    protected SimpleDateFormat dateFormat;

    protected SimpleDateFormat parseFormat;

    /** 
   * Creates a new time chooser 
   */
    public TimeChooser() {
        setEditable(true);
        getEditor();
    }

    public ComboBoxEditor getEditor() {
        if (editor == null) {
            if (startDate == null) startDate = new Date();
            dateFormat = new SimpleDateFormat("HH:mm");
            editor = new ComboDateEditor(dateFormat);
            String os = System.getProperty("os.name");
            if (os.toLowerCase().indexOf("windows") >= 0) editor.setBorder(new EmptyBorder(0, 6, 0, 0));
            fillList();
            setEditor(editor);
        }
        return editor;
    }

    private void fillList() {
        removeAllItems();
        int offset = startDate.getHours();
        int selectedHour = startDate.getHours();
        int selectedMin = startDate.getMinutes();
        for (int i = 0; i < 24; i++) {
            int ihr = i;
            if (mode == END_TIME_MODE) ihr += offset;
            String hr = new Integer(ihr % 24).toString();
            addItem(hr + ":00");
            if (step == STEP_HALF_HOURS) {
                if ((i == selectedHour) && (selectedMin != 0) && (selectedMin < 30)) addItem(hr + ":" + selectedMin);
                addItem(hr + ":30");
                if ((i == selectedHour) && (selectedMin != 0) && (selectedMin > 30)) addItem(hr + ":" + selectedMin);
            } else if (step == STEP_QUATER_HOURS) {
                if ((i == selectedHour) && (selectedMin != 0) && (selectedMin < 15)) addItem(hr + ":" + selectedMin);
                addItem(hr + ":15");
                if ((i == selectedHour) && (selectedMin != 0) && (selectedMin > 15) && (selectedMin < 30)) addItem(hr + ":" + selectedMin);
                addItem(hr + ":30");
                if ((i == selectedHour) && (selectedMin != 0) && (selectedMin > 30) && (selectedMin < 45)) addItem(hr + ":" + selectedMin);
                addItem(hr + ":45");
                if ((i == selectedHour) && (selectedMin != 0) && (selectedMin > 45)) addItem(hr + ":" + selectedMin);
            } else {
                if ((i == selectedHour) && (selectedMin != 0)) addItem(hr + ":" + selectedMin);
            }
        }
        String s = dateFormat.format(startDate);
        setSelectedItem(s);
    }

    /**
   * Sets the content of the edit field
   * @param s the new date string
   */
    public void setText(String s) {
        if ((s != null) && (s.length() > 0)) {
            try {
                if (parseFormat != null) startDate = parseFormat.parse(s); else startDate = new Date(s);
                editor.setText(dateFormat.format(startDate));
            } catch (ParseException ex) {
                ex.printStackTrace();
                startDate = new Date(s);
            }
        }
        fillList();
    }

    /**
   * Gets the content of the edit field
   * @return the date string
   */
    public String getText() {
        return editor.getText();
    }

    /**
   * Set the background color for the edit field
   * @param c the color
   */
    public void setBackground(Color c) {
        getEditor().getEditorComponent().setBackground(c);
        super.setBackground(c);
    }

    /**
   * Set the foreground color for the edit field
   * @param c the color
   */
    public void setForeground(Color c) {
        getEditor().getEditorComponent().setForeground(c);
        super.setForeground(c);
    }

    /**
   * Set the font
   * @param f the font
   */
    public void setFont(Font f) {
        getEditor().getEditorComponent().setFont(f);
        super.setFont(f);
    }

    /**
   * Set the list cell renderer, preserving the foreground and background 
   * colors in the process
   * @param renderer the new renderer.
   */
    public void setRenderer(ListCellRenderer renderer) {
        Color frgdColor = getForeground();
        Color bkgdColor = getBackground();
        super.setRenderer(renderer);
        setForeground(frgdColor);
        setBackground(bkgdColor);
    }

    /**
   * Set the format of the edit field. The format is used in the construction of
   * a java.text.SimpleDateFormat instance.
   * @param format the new date format
   */
    public void setFormat(String format) {
        dateFormat = new SimpleDateFormat(format);
        editor.setFormatterFactory(new DefaultFormatterFactory(new DateFormatter(dateFormat)));
    }

    /**
   * Set the format of the edit field data as it is parsed from the data model 
   * or when the setText method is invoked. The format is used in the construction of
   * a java.text.SimpleDateFormat instance.
   * @param format the incoming date
   */
    public void setParseFormat(String format) {
        parseFormat = new SimpleDateFormat(format);
    }

    /**
   * @param value
   */
    public void setMode(int value) {
        mode = value;
    }

    /**
   * @param value
   */
    public void setStep(int value) {
        step = value;
    }

    /**
   * @param value
   */
    public void setStartTime(String date) {
        try {
            startDate = dateFormat.parse(date);
        } catch (ParseException ex) {
            if (BuildProperties.DEBUG) DebugLogger.logError("WIDGET", "Invalid date: " + date);
        }
        fillList();
    }
}

class ComboDateEditor extends JFormattedTextField implements ComboBoxEditor {

    public ComboDateEditor(SimpleDateFormat df) {
        super(df);
    }

    /** Return the component that should be added to the tree hierarchy for
    * this editor
    */
    public Component getEditorComponent() {
        return this;
    }

    /** Set the item that should be edited. Cancel any editing if necessary **/
    public void setItem(Object anObject) {
        if (anObject != null) setText(anObject.toString()); else setText("");
    }

    /** Return the edited item **/
    public Object getItem() {
        return getText();
    }
}
