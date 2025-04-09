package de.enough.polish.ui;

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
import de.enough.polish.ui.Canvas;
import de.enough.polish.ui.StyleSheet;
import de.enough.polish.android.lcdui.Graphics;
import de.enough.polish.ui.Displayable;
import de.enough.polish.util.DrawUtil;
import de.enough.polish.util.Locale;
import de.enough.polish.android.midlet.MIDlet;
import de.enough.polish.calendar.CalendarItem;

/**
 * A <code>DateField</code> is an editable component for presenting
 * date and time (calendar)
 * information that may be placed into a <code>Form</code>. Value for
 * this field can be
 * initially set or left unset. If value is not set then the UI for the field
 * shows this clearly. The field value for &quot;not initialized
 * state&quot; is not valid
 * value and <code>getDate()</code> for this state returns <code>null</code>.
 * <p>
 * Instance of a <code>DateField</code> can be configured to accept
 * date or time information
 * or both of them. This input mode configuration is done by
 * <code>DATE</code>, <code>TIME</code> or
 * <code>DATE_TIME</code> static fields of this
 * class. <code>DATE</code> input mode allows to set only
 * date information and <code>TIME</code> only time information
 * (hours, minutes). <code>DATE_TIME</code>
 * allows to set both clock time and date values.
 * <p>
 * In <code>TIME</code> input mode the date components of
 * <code>Date</code> object
 * must be set to the &quot;zero epoch&quot; value of January 1, 1970.
 * <p>
 * Calendar calculations in this field are based on default locale and defined
 * time zone. Because of the calculations and different input modes date object
 * may not contain same millisecond value when set to this field and get back
 * from this field.
 * <HR>
 * 
 * @author Robert Virkus, robert@enough.de
 * @since MIDP 1.0
 */
public class DateField extends StringItem implements ItemCommandListener {

    /**
	 * Input mode for date information (day, month, year). With this mode this
	 * <code>DateField</code> presents and allows only to modify date
	 * value. The time
	 * information of date object is ignored.
	 * 
	 * <P>Value <code>1</code> is assigned to <code>DATE</code>.</P>
	 */
    public static final int DATE = 1;

    /**
	 * Input mode for time information (hours and minutes). With this mode this
	 * <code>DateField</code> presents and allows only to modify
	 * time. The date components
	 * should be set to the &quot;zero epoch&quot; value of January 1, 1970 and
	 * should not be accessed.
	 * 
	 * <P>Value <code>2</code> is assigned to <code>TIME</code>.</P>
	 */
    public static final int TIME = 2;

    /**
	 * Input mode for date (day, month, year) and time (minutes, hours)
	 * information. With this mode this <code>DateField</code>
	 * presents and allows to modify
	 * both time and date information.
	 * 
	 * <P>Value <code>3</code> is assigned to <code>DATE_TIME</code>.</P>
	 */
    public static final int DATE_TIME = 3;

    private Date date;

    private int inputMode;

    private TimeZone timeZone;

    private boolean showCaret;

    private int originalWidth;

    private int originalHeight;

    private long lastCaretSwitch;

    private Calendar calendar;

    private static final int[] DAYS_IN_MONTHS = { 31, 29, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31 };

    private int editIndex;

    private int textComplementColor;

    private int currentField;

    private int currentFieldStartIndex;

    private ItemCommandListener additionalItemCommandListener;

    private long androidFocusedTime;

    private long androidLastInvalidCharacterTime;

    /**
	 * Creates a <code>DateField</code> object with the specified
	 * label and mode. This call
	 * is identical to <code>DateField(label, mode, null)</code>.
	 * 
	 * @param label item label
	 * @param mode the input mode, one of DATE, TIME or DATE_TIME
	 * @throws IllegalArgumentException if the input mode's value is invalid
	 */
    public DateField(String label, int mode) {
        this(label, mode, null, null);
    }

    /**
	 * Creates a <code>DateField</code> object with the specified
	 * label and mode. This call
	 * is identical to <code>DateField(label, mode, null)</code>.
	 * 
	 * @param label item label
	 * @param mode the input mode, one of DATE, TIME or DATE_TIME
	 * @param style the CSS style for this item
	 * @throws IllegalArgumentException if the input mode's value is invalid
	 */
    public DateField(String label, int mode, Style style) {
        this(label, mode, null, style);
    }

    /**
	 * Creates a date field in which calendar calculations are based
	 * on specific
	 * <code>TimeZone</code> object and the default calendaring system for the
	 * current locale.
	 * The value of the <code>DateField</code> is initially in the
	 * &quot;uninitialized&quot; state.
	 * If <code>timeZone</code> is <code>null</code>, the system's
	 * default time zone is used.
	 * 
	 * @param label item label
	 * @param mode the input mode, one of DATE, TIME or DATE_TIME
	 * @param timeZone a specific time zone, or null for the default time zone
	 * @throws IllegalArgumentException if the input mode's value is invalid
	 */
    public DateField(String label, int mode, TimeZone timeZone) {
        this(label, mode, timeZone, null);
    }

    /**
	 * Creates a date field in which calendar calculations are based
	 * on specific
	 * <code>TimeZone</code> object and the default calendaring system for the
	 * current locale.
	 * The value of the <code>DateField</code> is initially in the
	 * &quot;uninitialized&quot; state.
	 * If <code>timeZone</code> is <code>null</code>, the system's
	 * default time zone is used.
	 * 
	 * @param label item label
	 * @param mode the input mode, one of DATE, TIME or DATE_TIME
	 * @param timeZone a specific time zone, or null for the default time zone
	 * @param style the CSS style for this item
	 * @throws IllegalArgumentException if the input mode's value is invalid
	 */
    public DateField(String label, int mode, TimeZone timeZone, Style style) {
        super(label, null, INTERACTIVE, style);
        this.inputMode = mode;
        if (timeZone != null) {
            this.timeZone = timeZone;
        } else {
            this.timeZone = TimeZone.getDefault();
        }
        setDate(null);
        addCommand(TextField.CLEAR_CMD);
        this.itemCommandListener = this;
    }

    /**
	 * Returns date value of this field. Returned value is
	 * <code>null</code> if field  value is not initialized. 
	 * The date object is constructed according the rules of
	 * locale specific calendaring system and defined time zone.
	 * 
	 * In <code>TIME</code> mode field the date components are set to
	 * the &quot;zero
	 * epoch&quot; value of January 1, 1970. If a date object that presents time
	 * beyond one day from this &quot;zero epoch&quot; then this field
	 * is in &quot;not initialized&quot; state and this method returns <code>null</code>.
	 * 
	 * In <code>DATE</code> mode field the time component of the calendar is set
	 * to zero when
	 * constructing the date object.
	 * 
	 * @return date object representing time or date depending on input mode
	 * @see #setDate(java.util.Date)
	 */
    public Date getDate() {
        if (this.isFocused && (this.date != null)) {
            moveForward(false);
            moveBackward(false);
        }
        return this.date;
    }

    /**
	 * Sets a new value for this field. <code>null</code> can be
	 * passed to set the field
	 * state to &quot;not initialized&quot; state. The input mode of
	 * this field defines
	 * what components of passed <code>Date</code> object is used.<p>
	 * 
	 * In <code>TIME</code> input mode the date components must be set
	 * to the &quot;zero
	 * epoch&quot; value of January 1, 1970. If a date object that presents time
	 * beyond one day then this field is in &quot;not initialized&quot; state.
	 * In <code>TIME</code> input mode the date component of
	 * <code>Date</code> object is ignored and time
	 * component is used to precision of minutes.<p>
	 * 
	 * In <code>DATE</code> input mode the time component of
	 * <code>Date</code> object is ignored.<p>
	 * 
	 * In <code>DATE_TIME</code> input mode the date and time
	 * component of <code>Date</code> are used but
	 * only to precision of minutes.
	 * 
	 * @param date new value for this field
	 * @see #getDate()
	 */
    public void setDate(Date date) {
        if (date != null && this.inputMode == TIME) {
            if (date.getTime() > 86400000) {
                if (this.calendar == null) {
                    this.calendar = Calendar.getInstance();
                    this.calendar.setTimeZone(this.timeZone);
                }
                this.calendar.setTime(date);
                long timeOnly = this.calendar.get(Calendar.MILLISECOND) + this.calendar.get(Calendar.SECOND) * 1000 + this.calendar.get(Calendar.MINUTE) * 60 * 1000 + this.calendar.get(Calendar.HOUR_OF_DAY) * 60 * 60 * 1000;
                date.setTime(timeOnly);
            }
        }
        this.date = date;
        if (date == null) {
            if (this.inputMode == DATE || this.inputMode == DATE_TIME) {
                this.text = "YYYY-MM-DD";
                if (this.inputMode == DATE_TIME) {
                    this.text += " hh:mm";
                }
            } else if (this.inputMode == TIME) {
                this.text = "hh:mm";
            }
        } else {
            if (this.calendar == null) {
                this.calendar = Calendar.getInstance();
                this.calendar.setTimeZone(this.timeZone);
            }
            this.calendar.setTime(date);
            StringBuffer buffer = new StringBuffer(10);
            if ((this.inputMode == DATE) || (this.inputMode == DATE_TIME)) {
                int year = this.calendar.get(Calendar.YEAR);
                int month = this.calendar.get(Calendar.MONTH);
                int day = this.calendar.get(Calendar.DAY_OF_MONTH);
                if (year < 10) {
                    buffer.append("000");
                } else if (year < 100) {
                    buffer.append("00");
                } else if (year < 1000) {
                    buffer.append("0");
                }
                buffer.append(year).append("-");
                if (month < 9) {
                    buffer.append('0');
                }
                buffer.append(++month).append("-");
                if (day < 10) {
                    buffer.append('0');
                }
                buffer.append(day);
                if (this.inputMode == DATE_TIME) {
                    buffer.append(' ');
                }
            }
            if ((this.inputMode == TIME) || (this.inputMode == DATE_TIME)) {
                int hour = this.calendar.get(Calendar.HOUR_OF_DAY);
                if (hour < 10) {
                    buffer.append('0');
                }
                buffer.append(hour).append(':');
                int minute = this.calendar.get(Calendar.MINUTE);
                if (minute < 10) {
                    buffer.append('0');
                }
                buffer.append(minute);
            }
            this.text = buffer.toString();
        }
        if (isInitialized()) {
            setInitialized(false);
            repaint();
        }
    }

    public String getDateFormatPattern() {
        StringBuffer buffer = new StringBuffer(10);
        if ((this.inputMode == DATE) || (this.inputMode == DATE_TIME)) {
            buffer.append("yyyy").append("-").append("MM").append("-").append("dd");
            if (this.inputMode == DATE_TIME) {
                buffer.append(' ');
            }
        }
        if ((this.inputMode == TIME) || (this.inputMode == DATE_TIME)) {
            buffer.append("HH:mm");
        }
        return buffer.toString();
    }

    /**
	 * Gets input mode for this date field. Valid input modes are
	 * <code>DATE</code>, <code>TIME</code> and <code>DATE_TIME</code>.
	 * 
	 * @return input mode of this field
	 * @see #setInputMode(int)
	 */
    public int getInputMode() {
        return this.inputMode;
    }

    /**
	 * Set input mode for this date field. Valid input modes are
	 * <code>DATE</code>, <code>TIME</code> and <code>DATE_TIME</code>.
	 * 
	 * @param mode the input mode, must be one of DATE, TIME or DATE_TIME
	 * @throws IllegalArgumentException if an invalid value is specified
	 * @see #getInputMode()
	 */
    public void setInputMode(int mode) {
        this.inputMode = mode;
        setDate(this.date);
    }

    public void paintContent(int x, int y, int leftBorder, int rightBorder, Graphics g) {
        super.paintContent(x, y, leftBorder, rightBorder, g);
        if (this.isFocused) {
            String head = this.text.substring(0, this.editIndex);
            int headWidth = stringWidth(head);
            char editChar = this.text.charAt(this.editIndex);
            int editWidth = charWidth(editChar);
            if (this.isLayoutCenter) {
                int centerX = leftBorder + (rightBorder - leftBorder) / 2;
                int completeWidth = stringWidth(this.text);
                x = centerX - (completeWidth / 2);
            } else if (this.isLayoutRight) {
                int completeWidth = stringWidth(this.text);
                x = rightBorder - completeWidth;
            }
            g.fillRect(x + headWidth - 1, y - 1, editWidth + 1, getFontHeight());
            if (this.showCaret) {
                g.setColor(this.textComplementColor);
                g.drawChar(editChar, x + headWidth, y + getFontHeight(), Graphics.BOTTOM | Graphics.LEFT);
            }
        }
    }

    protected void initContent(int firstLineWidth, int availWidth, int availHeight) {
        if (this.date == null) {
            setDate(null);
        }
        super.initContent(firstLineWidth, availWidth, availHeight);
        this.originalWidth = this.contentWidth;
        this.originalHeight = this.contentHeight;
        if (this.minimumWidth != null && this.contentWidth < this.minimumWidth.getValue(firstLineWidth)) {
            this.contentWidth = this.minimumWidth.getValue(firstLineWidth);
        }
        if (this.minimumHeight != null && this.contentHeight < this.minimumHeight.getValue(availWidth)) {
            this.contentHeight = this.minimumHeight.getValue(availWidth);
        } else if (this.contentHeight < getFontHeight()) {
            this.contentHeight = getFontHeight();
            this.originalHeight = this.contentHeight;
        }
    }

    public void setStyle(Style style) {
        super.setStyle(style);
        this.textComplementColor = DrawUtil.getComplementaryColor(this.textColor);
    }

    public boolean animate() {
        long currentTime = System.currentTimeMillis();
        if (currentTime - this.lastCaretSwitch > 500) {
            this.lastCaretSwitch = currentTime;
            this.showCaret = !this.showCaret;
            return true;
        } else {
            return false;
        }
    }

    protected void defocus(Style originalStyle) {
        super.defocus(originalStyle);
        this.showCaret = false;
        if (this.date != null) {
            moveForward(false);
            moveBackward(false);
        }
    }

    protected Style focus(Style newStyle, int direction) {
        if (this.isShown) {
            MIDlet.midletInstance.showSoftKeyboard();
            this.androidFocusedTime = System.currentTimeMillis();
        }
        return super.focus(newStyle, direction);
    }

    protected void showNotify() {
        if (this.isFocused) {
            MIDlet.midletInstance.showSoftKeyboard();
        }
        super.showNotify();
    }

    protected synchronized boolean handleKeyPressed(int keyCode, int gameAction) {
        int foundNumber = -1;
        int clearKey = 0;
        if (keyCode == clearKey) {
            long invalidCharInputTime = this.androidLastInvalidCharacterTime;
            if (invalidCharInputTime != 0) {
                this.androidLastInvalidCharacterTime = 0;
                if (invalidCharInputTime - System.currentTimeMillis() <= 10 * 1000) {
                    return true;
                }
            }
        }
        if ((keyCode >= Canvas.KEY_NUM0 && keyCode <= Canvas.KEY_NUM9) || foundNumber != -1) {
            if (this.date == null) {
                setDate(new Date(System.currentTimeMillis()));
            }
            char c = Integer.toString(keyCode - Canvas.KEY_NUM0).charAt(0);
            if (foundNumber != -1) {
                c = Integer.toString(foundNumber).charAt(0);
            }
            String newText = this.text.substring(0, this.editIndex) + c;
            if (this.editIndex < this.text.length() - 1) {
                newText += this.text.substring(this.editIndex + 1);
            }
            setText(newText);
            moveForward(true);
        } else if (this.date != null && (gameAction == Canvas.LEFT || keyCode == clearKey)) {
            moveBackward(true);
        } else if (this.date != null && gameAction == Canvas.RIGHT) {
            moveForward(true);
        } else if (gameAction != Canvas.FIRE) {
            this.androidLastInvalidCharacterTime = System.currentTimeMillis();
            return false;
        }
        return true;
    }

    protected boolean handleKeyReleased(int keyCode, int gameAction) {
        int clearKey = 0;
        if (keyCode == clearKey) {
            return true;
        }
        boolean handled = super.handleKeyReleased(keyCode, gameAction);
        return handled;
    }

    private void moveBackward(boolean notifyStateListener) {
        if (this.date == null) {
            return;
        }
        int forwardIndex = this.editIndex;
        while (Character.isDigit(this.text.charAt(forwardIndex))) {
            forwardIndex++;
            if (forwardIndex == this.text.length()) {
                forwardIndex = 0;
                break;
            }
        }
        int newIndex = this.editIndex - 1;
        while (true) {
            if (newIndex < 0) {
                checkField(forwardIndex, notifyStateListener);
                newIndex = this.text.length() - 1;
                if (this.inputMode == DATE) {
                    this.currentField = 2;
                } else if (this.inputMode == TIME) {
                    this.currentField = 1;
                } else {
                    this.currentField = 4;
                }
            }
            char c = this.text.charAt(newIndex);
            if (Character.isDigit(c)) {
                break;
            }
            checkField(forwardIndex, notifyStateListener);
            this.currentField--;
            newIndex--;
        }
        this.editIndex = newIndex;
        if (newIndex == 0) {
            this.currentFieldStartIndex = 0;
        } else {
            while (Character.isDigit(this.text.charAt(newIndex))) {
                newIndex--;
                if (newIndex == -1) {
                    break;
                }
            }
            this.currentFieldStartIndex = newIndex + 1;
        }
    }

    private void moveForward(boolean notifyStateListener) {
        int newIndex = this.editIndex + 1;
        while (true) {
            if (newIndex >= this.text.length()) {
                newIndex = 0;
                checkField(newIndex, notifyStateListener);
                this.currentField = 0;
                this.currentFieldStartIndex = 0;
            }
            char c = this.text.charAt(newIndex);
            if (Character.isDigit(c)) {
                break;
            }
            checkField(newIndex, notifyStateListener);
            newIndex++;
            this.currentFieldStartIndex = newIndex;
            this.currentField++;
        }
        this.editIndex = newIndex;
    }

    private void checkField(int newIndex, boolean notifyStateListener) {
        String fieldStr;
        if (newIndex == 0) {
            fieldStr = this.text.substring(this.currentFieldStartIndex);
        } else {
            fieldStr = this.text.substring(this.currentFieldStartIndex, newIndex);
        }
        int fieldValue;
        try {
            fieldValue = Integer.parseInt(fieldStr);
        } catch (NumberFormatException e) {
            return;
        }
        if (this.calendar == null) {
            this.calendar = Calendar.getInstance();
            this.calendar.setTimeZone(this.timeZone);
        }
        this.calendar.setTime(this.date);
        int calendarField = -1;
        if ((this.inputMode == DATE) || (this.inputMode == DATE_TIME)) {
            switch(this.currentField) {
                case 0:
                    calendarField = Calendar.YEAR;
                    break;
                case 1:
                    calendarField = Calendar.MONTH;
                    break;
                case 2:
                    calendarField = Calendar.DAY_OF_MONTH;
                    break;
            }
        }
        if (this.inputMode == DATE_TIME) {
            switch(this.currentField) {
                case 3:
                    calendarField = Calendar.HOUR_OF_DAY;
                    break;
                case 4:
                    calendarField = Calendar.MINUTE;
                    break;
            }
        } else if (this.inputMode == TIME) {
            switch(this.currentField) {
                case 0:
                    calendarField = Calendar.HOUR_OF_DAY;
                    break;
                case 1:
                    calendarField = Calendar.MINUTE;
                    break;
            }
        }
        if (calendarField == -1) {
            return;
        } else {
            switch(calendarField) {
                case Calendar.DAY_OF_MONTH:
                    if (fieldValue < 1) {
                        fieldValue = 1;
                    } else {
                        int month = this.calendar.get(Calendar.MONTH);
                        int maxDay = DAYS_IN_MONTHS[month];
                        if (fieldValue > maxDay) {
                            fieldValue = maxDay;
                        }
                    }
                    break;
                case Calendar.MONTH:
                    if (fieldValue < 1) {
                        fieldValue = 1;
                    } else if (fieldValue > 12) {
                        fieldValue = 12;
                    }
                    fieldValue--;
                    break;
                case Calendar.HOUR_OF_DAY:
                    if (fieldValue == 24) {
                        fieldValue = 0;
                    } else if (fieldValue > 24) {
                        fieldValue = 23;
                    }
                    break;
                case Calendar.MINUTE:
                    if (fieldValue > 59) {
                        fieldValue = 59;
                    }
                    break;
            }
            this.calendar.set(calendarField, fieldValue);
            boolean changed = false;
            try {
                Date newDate = this.calendar.getTime();
                setDate(newDate);
                changed = true;
            } catch (IllegalArgumentException e) {
                if (calendarField == Calendar.DAY_OF_MONTH || calendarField == Calendar.MONTH) {
                    int month = calendarField == Calendar.MONTH ? fieldValue : this.calendar.get(Calendar.MONTH);
                    int day = calendarField == Calendar.DAY_OF_MONTH ? fieldValue : this.calendar.get(Calendar.DAY_OF_MONTH);
                    int maxDay = DAYS_IN_MONTHS[month];
                    if (month == 2) {
                        maxDay = 28;
                    }
                    if (day > maxDay) {
                        this.calendar.set(Calendar.DAY_OF_MONTH, maxDay);
                        try {
                            setDate(this.calendar.getTime());
                            changed = true;
                        } catch (IllegalArgumentException e2) {
                        }
                    }
                }
                if (!changed) {
                    de.enough.polish.util.Debug.debug("error", "de.enough.polish.ui.DateField", 1234, "Unable to set date: ", e);
                }
            }
            if (notifyStateListener && changed) {
                notifyStateChanged();
            }
        }
    }

    public void setItemCommandListener(ItemCommandListener l) {
        this.additionalItemCommandListener = l;
    }

    public void commandAction(Command c, Item item) {
        if (c == TextField.CLEAR_CMD) {
            this.editIndex = 0;
            this.currentField = 0;
            this.currentFieldStartIndex = 0;
            setDate(null);
        } else if (this.additionalItemCommandListener != null) {
            this.additionalItemCommandListener.commandAction(c, item);
        }
    }

    protected boolean handlePointerReleased(int relX, int relY) {
        if (isInItemArea(relX, relY)) {
            if (this.isFocused && ((System.currentTimeMillis() - this.androidFocusedTime) > 200)) {
                MIDlet.midletInstance.toggleSoftKeyboard();
                return true;
            }
            return true;
        }
        return super.handlePointerReleased(relX, relY);
    }
}
