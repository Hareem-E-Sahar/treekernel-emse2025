package sears.file;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;
import sears.file.exception.io.FileConversionException;
import sears.tools.LinearInterpolation;
import sears.tools.SearsProperties;
import sears.tools.Trace;
import sears.tools.Utils;

/**
 * Class SubtitleFile.
 * <br><b>Summary:</b><br>
 * This class represents a subtitle file.
 * It provides facilities on subtitles, as delay, resynchro.
 * It must be specialized to the subtitle file type you want to open.
 * <br>You would have to use the {@link #getInstance(File, ArrayList, String)} static method for this
 */
public abstract class SubtitleFile {

    /**The system File that contains the file*/
    protected File file;

    /**The ArrayList of subtitles found*/
    protected ArrayList<Subtitle> subtitleList;

    /**A boolean to know if file has changed*/
    protected boolean fileChanged;

    /**The temporary file */
    protected File temporaryFile = null;

    /** (<b>LinearInterpolation</b>) LinearInterpolation: The LinearInterpolation used for magic resynchro. */
    private LinearInterpolation linearInterpolation;

    /** The default charset that Sears use to parse a subtitle file: "ISO-8859-1" */
    public static final String DEFAULT_CHARSET = "ISO-8859-1";

    /** Represents an array of charsets that Sears can use to parse a file if an error occurs with the default one*/
    public static final String[] BASIC_CHARSETS = { "UTF-16", "UTF-8" };

    private String charset;

    /**
	 * Constructor SubtitleFile.
	 * <br><b>Summary:</b><br>
	 * Constructor of the class.
	 * Beware not to use this file directly, because it does contains no ST.
	 * You will have to fill the list of ST, and save the File first.
	 */
    public SubtitleFile() {
        file = null;
        subtitleList = new ArrayList<Subtitle>();
        fileChanged = true;
        charset = DEFAULT_CHARSET;
    }

    /**
	 * Constructor SubtitleFile.
	 * <br><b>Summary:</b><br>
	 * Constructor of the class.
	 * @param fileToOpen        		The <b>(String)</b> path to file to open.
	 * @param _subtitlesList        	The <b>(ArrayList)</b> List of subtitles.
	 * @throws FileConversionException	if a limitation when reading the file appears
	 */
    public SubtitleFile(String fileToOpen, ArrayList<Subtitle> _subtitlesList) throws FileConversionException {
        construct(new File(fileToOpen), _subtitlesList, null);
    }

    /**
	 * Constructor SubtitleFile.
	 * <br><b>Summary:</b><br>
	 * Constructor of the class.    
	 * @param _file             		The <b>(File)</b> file to open.
	 * @param _subtitlesList        	The <b>(ArrayList)</b> List of subtitles.     
	 * @throws FileConversionException	if a limitation when reading the file appears
	 */
    public SubtitleFile(File _file, ArrayList<Subtitle> _subtitlesList) throws FileConversionException {
        construct(_file, _subtitlesList, null);
    }

    /**
	 * Constructor SubtitleFile.
	 * <br><b>Summary:</b><br>
	 * Constructor of the class.    
	 * @param _file              		The <b>(File)</b> file to open.
	 * @param _subtitlesList       		The <b>(ArrayList)</b> List of subtitles.
	 * @param charset					The charset to use during operations on the file ...  
	 * @throws FileConversionException	if a limitation when reading the file appears
	 */
    public SubtitleFile(File _file, ArrayList<Subtitle> _subtitlesList, String charset) throws FileConversionException {
        construct(_file, _subtitlesList, charset);
    }

    /**
	 * Method construct.
	 * <br><b>Summary:</b><br>
	 * Construct the file.
	 * @param _srtFile              	The <b>(File)</b> file to open.
	 * @param _subtitlesList        	The <b>(ArrayList<Subtitle>)</b> List of subtitles.
	 * @param charset					the charset to use during operations on file, if null <tt>DEFAULT_CHARSET</tt> is use
	 * 									<br>This parameter is a String, so if it is not denoted a charset, an exception is throws
	 * @throws NullPointerException 	if <tt>_srtFile<tt> is null   
	 * @throws FileConversionException	if a limitation when reading the file appears
	 */
    private void construct(File _srtFile, ArrayList<Subtitle> _subtitlesList, String charset) throws FileConversionException {
        if (_srtFile == null) {
            throw new NullPointerException("Cannot constructs from a null file");
        }
        if (!_srtFile.exists()) {
            Trace.trace(("File " + _srtFile.getAbsolutePath() + " Does not exist !"), Trace.WARNING_PRIORITY);
        }
        if (_subtitlesList == null) {
            Trace.trace("The given subtitleList is null !", Trace.WARNING_PRIORITY);
        }
        file = _srtFile;
        subtitleList = _subtitlesList;
        this.charset = getANonNullCharset(charset);
        parse();
        fileChanged = false;
    }

    /**
	 * Returns <tt>charset</tt> if it is non null, else <tt>DEFAULT_CHARSET<tt> is returned
	 * @param charset	the charset to test
	 * @return 			<tt>DEFAULT_CHARSET<tt> or <tt>charset</tt> if it is non null
	 */
    protected static String getANonNullCharset(String charset) {
        if (charset == null) {
            charset = DEFAULT_CHARSET;
        }
        return charset;
    }

    /**
	 * Returns the charset used for write and read operations
	 * @return the charset used by <tt>this</tt>
	 */
    public String getCharset() {
        return charset;
    }

    /**
	 * Changes the charset, if a non null value is given, <tt>DEFAULT_CHARSET</tt> is set as 
	 * the used charset
	 * @param charset	the new charset to use
	 */
    public void setCharset(String charset) {
        this.charset = getANonNullCharset(charset);
    }

    /**
	 * Returns the line separator (end of line) to use (depends to the user choice).
	 * <br>By default the system one is return.
	 *  
	 * @return	a string that denoted a line separator
	 */
    public static String getLineSeparator() {
        String lineSeparator = Utils.LINE_SEPARATOR;
        if (!SearsProperties.getProperty(SearsProperties.DOS_LINE_SEPARATOR, "0").equals("0")) {
            lineSeparator = Utils.DOS_LINE_SEPARATOR;
        }
        return lineSeparator;
    }

    /**
	 * Returns the extension file
	 * @return the extension file
	 */
    public abstract String extension();

    /**
	 * Method parse.
	 * <br><b>Summary:</b><br>
	 * This method parse the current file, and construct the subtitleList.
	 * @throws FileConversionException	if a limitation when reading the file appears
	 */
    protected abstract void parse() throws FileConversionException;

    /**
	 * Method getContentDirectory.
	 * <br><b>Summary:</b><br>
	 * This method return the file's parent folder.
	 * @return  <b>(File)</b>   The parent of the current file.
	 */
    public File getContentDirectory() {
        return this.getFile().getParentFile();
    }

    /**
	 * Method getFile.
	 * <br><b>Summary:</b><br>
	 * Return the current file.
	 * @return  <b>(File)</b>   The current file.
	 */
    public File getFile() {
        return file;
    }

    /**
	 * Method getTemporaryFile.
	 * <br><b>Summary:</b><br>
	 * Return the temporary file.
	 * @return  <b>(File)</b>   The temporary file.
	 */
    public File getTemporaryFile() {
        return temporaryFile;
    }

    /**
	 * Method timeToString.
	 * <br><b>Summary:</b><br>
	 * This method transform a number of milliseconds in a string representation.
	 * @param milliseconds               The number of milliseconds to transform
	 * @return  <b>(String)</b>     The corresponding String representation of the number of milliseconds.
	 */
    public static String timeToString(int milliseconds) {
        boolean positive = milliseconds >= 0;
        milliseconds = Math.abs(milliseconds);
        int seconds = milliseconds / 1000;
        milliseconds = milliseconds - seconds * 1000;
        int hours = (seconds - seconds % 3600) / 3600;
        seconds -= hours * 3600;
        int minutes = (seconds - seconds % 60) / 60;
        seconds -= minutes * 60;
        String hoursString = "";
        hours = Math.abs(hours);
        if (hours < 10) {
            hoursString += "0" + hours;
        } else {
            hoursString += hours;
        }
        String minutesString = "";
        if (minutes < 10) {
            minutesString += "0" + minutes;
        } else {
            minutesString += minutes;
        }
        String secondsString = "";
        if (seconds < 10) {
            secondsString += "0" + seconds;
        } else {
            secondsString += seconds;
        }
        String millisecondsString = "";
        if (milliseconds < 10) {
            millisecondsString += "00" + milliseconds;
        } else if (milliseconds < 100) {
            millisecondsString += "0" + milliseconds;
        } else {
            millisecondsString += "" + milliseconds;
        }
        String result = "";
        if (!positive) {
            result = "-";
        }
        result = result + hoursString + ":" + minutesString + ":" + secondsString + "," + millisecondsString;
        return result;
    }

    /**
	 * Method stringToTime.
	 * <br><b>Summary:</b><br>
	 * Return the number of miliseconds that correspond to the given String time representation.
	 * @param time              The string srt time representation.
	 * @return <b>(int)</b>     The corresponding number of miliseconds.
	 * @throws NumberFormatException if there's a time error
	 */
    public static int stringToTime(String time) throws NumberFormatException {
        int result = 0;
        boolean positive = true;
        if (time != null && time.trim().startsWith("-")) {
            positive = false;
            int indexOfMinus = time.indexOf("-");
            if (indexOfMinus != -1) {
                time = time.substring(indexOfMinus + 1);
            }
        }
        StringTokenizer stk = new StringTokenizer(time, ":,. ");
        try {
            result += 3600 * Integer.parseInt(stk.nextToken());
            result += 60 * Integer.parseInt(stk.nextToken());
            result += Integer.parseInt(stk.nextToken());
            result = result * 1000;
            result += Integer.parseInt(stk.nextToken());
            if (!positive) {
                result = -result;
            }
        } catch (NoSuchElementException e) {
            throw new NumberFormatException("Bad Time Format found: " + time);
        }
        return result;
    }

    /**
	 * Method writeToFile.
	 * <br><b>Summary:</b><br>
	 * Use this method to write subtitle file to the given File.
	 * @param fileToWrite       The File to  write the file.
	 * @throws FileConversionException 
	 */
    public abstract void writeToFile(File fileToWrite) throws FileConversionException;

    /**
	 * Method writeToTemporaryFile.
	 * <br><b>Summary:</b><br>
	 * Use this method to write subtitle file to the temporary File.
	 */
    public abstract void writeToTemporaryFile();

    /**
	 * Method addFakeSub.
	 * <br><b>Summary:</b><br>
	 * Use this method to create an empty subtitle file.
	 * 						
	 */
    public void addFakeSub() {
        subtitleList.add(new Subtitle(0, 0, 1, ""));
    }

    /**
	 * Method delay.
	 * <br><b>Summary:</b><br>
	 * Apply a delay on the given range subtitles.
	 * @param beginIndex    The first index to begin delay.
	 * @param endIndex      The last index to put a delay
	 * @param delay         The delay to Apply.
	 */
    public void delay(int beginIndex, int endIndex, int delay) {
        for (int index = beginIndex; index <= endIndex; index++) {
            delaySubtitle(delay, index);
        }
    }

    /**
	 * Method delay.
	 * <br><b>Summary:</b><br>
	 * Delay a list of subtitles, idetified by their index.
	 * @param indexToDelay      The array of subtitle's index to be delayed.
	 * @param delay             The delay to apply.
	 */
    public void delay(int[] indexToDelay, int delay) {
        if (indexToDelay.length == 0) {
            delay(delay);
        } else {
            for (int i = 0; i < indexToDelay.length; i++) {
                delaySubtitle(delay, indexToDelay[i]);
            }
        }
    }

    /**
	 * Method delay.
	 * <br><b>Summary:</b><br>
	 * Use this method to delay whole file.
	 * @param delay     The delay to apply.
	 */
    public void delay(int delay) {
        delay(0, subtitleList.size() - 1, delay);
    }

    /**
	 * Method delaySubtitle.
	 * <br><b>Summary:</b><br>
	 * Delay a given subtitle, identified by its index.
	 * @param delay 	The delay to apply.
	 * @param index		The index of the subtitle to delay.
	 */
    private void delaySubtitle(int delay, int index) {
        Subtitle subtitle = (Subtitle) subtitleList.get(index);
        subtitle.delay(delay);
        fileChanged = true;
    }

    /**
	 * Method normalizeDuration.
	 * <br><b>Summary:</b><br>
	 * This method permits to normalize the subtitle duration for whole file.
	 * It ensures that subtitle display duration is between given minDuration, and maxDuration.
	 * It raise or lower it to fit in the interval.
	 * It takes care that subtitle do not ends before the start of its follower.
	 * If minDuration equals to -1, it does not checks the minDuration.
	 * If maxDuration equals to -1, it does not checks the maxDuration.
	 * @param minDuration	The min duration to ensure.
	 * @param maxDuration	The max duration to ensure.
	 */
    public void normalizeDuration(int minDuration, int maxDuration) {
        normalizeDuration(0, subtitleList.size() - 1, minDuration, maxDuration);
    }

    /**
	 * Method normalizeDuration.
	 * <br><b>Summary:</b><br>
	 * This method permits to normalize the subtitle duration for given index interval.
	 * It ensures that subtitle display duration is between given minDuration, and maxDuration.
	 * It raise or lower it to fit in the interval.
	 * It takes care that subtitle do not ends before the start of its follower.
	 * If minDuration equals to -1, it does not checks the minDuration.
	 * If maxDuration equals to -1, it does not checks the maxDuration.
	 * @param beginIndex	The start index to begin the normalization.
	 * @param endIndex		The end index to finish the normalization.
	 * @param minDuration	The min duration to ensure.
	 * @param maxDuration	The max duration to ensure.
	 */
    private void normalizeDuration(int beginIndex, int endIndex, int minDuration, int maxDuration) {
        for (int index = beginIndex; index <= endIndex; index++) {
            normalizeSubtitleDuration(minDuration, maxDuration, index);
        }
    }

    /**
	 * Method normalizeDuration.
	 * <br><b>Summary:</b><br>
	 * This method permits to normalize the subtitle duration for given subtitle indexes.
	 * It ensures that subtitle display duration is between given minDuration, and maxDuration.
	 * It raise or lower it to fit in the interval.
	 * It takes care that subtitle do not ends before the start of its follower.
	 * If minDuration equals to -1, it does not checks the minDuration.
	 * If maxDuration equals to -1, it does not checks the maxDuration.
	 * If given indexes array is empty, it normalizes the whole file.
	 * @param indexToNormalize	The array of subtitle index to be normalized.
	 * @param minDuration		The min duration to ensure.
	 * @param maxDuration		The max duration to ensure.
	 */
    public void normalizeDuration(int[] indexToNormalize, int minDuration, int maxDuration) {
        if (indexToNormalize.length == 0) {
            normalizeDuration(minDuration, maxDuration);
        } else {
            for (int i = 0; i < indexToNormalize.length; i++) {
                normalizeSubtitleDuration(minDuration, maxDuration, indexToNormalize[i]);
            }
        }
    }

    /**
	 * Method normalizeSubtitleDuration.
	 * <br><b>Summary:</b><br>
	 * This method permits to normalize the subtitle duration.
	 * It ensures that subtitle display duration is between given minDuration, and maxDuration.
	 * It raise or lower it to fit in the interval.
	 * It takes care that subtitle do not ends before the start of its follower.
	 * If minDuration equals to -1, it does not checks the minDuration.
	 * If maxDuration equals to -1, it does not checks the maxDuration.
	 * @param minDuration		The min duration to ensure.
	 * @param maxDuration		The max duration to ensure.
	 * @param index				The index of the subtitle to be normalized.
	 */
    private void normalizeSubtitleDuration(int minDuration, int maxDuration, int index) {
        Subtitle subtitle = (Subtitle) subtitleList.get(index);
        int endDate = subtitle.getEndDate();
        int startDate = subtitle.getStartDate();
        int duration = endDate - startDate;
        int newEndDate = endDate;
        if (minDuration != -1 && duration < minDuration) {
            newEndDate = startDate + minDuration;
        }
        if (maxDuration != -1 && duration > maxDuration) {
            newEndDate = startDate + maxDuration;
        }
        if (index < getSubtitles().size() - 1) {
            int nextSubtitleStartDate = ((Subtitle) getSubtitles().get(index + 1)).getStartDate();
            if (nextSubtitleStartDate <= newEndDate) {
                newEndDate = nextSubtitleStartDate - 1;
            }
        }
        if (newEndDate < startDate) {
            newEndDate = endDate;
        }
        subtitle.setEndDate(newEndDate);
        fileChanged = true;
    }

    /**
	 * Method resynchro.
	 * <br><b>Summary:</b><br>
	 * Use this method to apply a resynchronisation.
	 * @param result    The resynchro parameter, an int array organized like this:
	 *                  [0]:The source 1
	 *                  [1]:The destination 1
	 *                  [2]:The source 2
	 *                  [3]:The destination 2
	 */
    public void resynchro(int[] result) {
        int source1 = result[0];
        int dest1 = result[1];
        int source2 = result[2];
        int dest2 = result[3];
        int delay1 = dest1 - source1;
        int delay2 = dest2 - source2;
        float scale = (float) (delay2 - delay1) / ((float) source2 - (float) source1);
        Trace.trace("Computed scale : " + scale, Trace.ALGO_PRIORITY);
        Iterator<Subtitle> subtitles = subtitleList.iterator();
        while (subtitles.hasNext()) {
            Subtitle subtitle = subtitles.next();
            int localDelay = Math.round(((float) (subtitle.getStartDate() - source1) * scale) + (float) delay1);
            subtitle.delay(localDelay);
        }
        fileChanged = true;
    }

    /**
	 * Method setFile.
	 * <br><b>Summary:</b><br>
	 * Set the file to the given file.
	 * @param file      The file to set.
	 */
    public void setFile(File file) {
        this.file = file;
        fileChanged = true;
    }

    /**
	 * Method addSubtitle.
	 * <br><b>Summary:</b><br>
	 * Add a subtitle to subtitle list.
	 * It may update the given subtitle number if needed.
	 * @param subtitle   The <b>Subtitle</b> to add to the file.
	 */
    public void addSubtitle(Subtitle subtitle) {
        addSubtitle(subtitle, true);
    }

    /**
	 * Method addSubtitle.
	 * <br><b>Summary:</b><br>
	 * Add a subtitle to subtitle list, recomputing its sequence number.
	 * @param subtitle          The <b>Subtitle</b> to add to the file.
	 * @param updateNumber     A <b>boolean</b>, true if want to update the number with its index. False not to update it.
	 */
    public void addSubtitle(Subtitle subtitle, boolean updateNumber) {
        subtitleList.add(subtitle);
        if (updateNumber) {
            subtitle.setNumber(subtitleList.size());
        }
        fileChanged = true;
    }

    /**
	 * Method split.
	 * <br><b>Summary:</b><br>
	 * This method split the subtitle file in two part at the given subtitle index.
	 * The two part will be saved in the given destination files, and a delay will be applied to the second part.
	 * @param destinationFiles      The <b>File[]</b> where to save the two parts of the file.
	 * @param subtitleIndex         The <b>int</b> subtitle index, from wich create the second part of the subtitle.
	 * @param secondPartDelay       The <b>int</b> initial delay to apply to the second part.
	 * @return 						an array of two <tt>SubtitleFile</tt> object
	 */
    public SubtitleFile[] split(File[] destinationFiles, int subtitleIndex, int secondPartDelay) {
        SubtitleFile[] result = new SubtitleFile[2];
        result[0] = getNewInstance();
        result[0].setFile(destinationFiles[0]);
        result[1] = getNewInstance();
        result[1].setFile(destinationFiles[1]);
        int index = 0;
        for (Subtitle currentSubtitle : subtitleList) {
            if (index < subtitleIndex) {
                result[0].addSubtitle(new Subtitle(currentSubtitle));
            } else {
                result[1].addSubtitle(new Subtitle(currentSubtitle), true);
            }
            index++;
        }
        if (secondPartDelay >= 0) {
            result[1].shiftToZero();
            result[1].delay(secondPartDelay);
        }
        return result;
    }

    /**
	 * Method shiftToZero.
	 * <br><b>Summary:</b><br>
	 * This method delays the subtitles, so the first one appears at time 0:00:00.000
	 */
    protected void shiftToZero() {
        int firstTime = ((Subtitle) subtitleList.get(0)).getStartDate();
        delay(-firstTime);
        fileChanged = true;
    }

    /**
	 * Method getNewInstance.
	 * <br><b>Summary:</b><br>
	 * This method should return a new instance of the current SubtitleFile class.
	 * @return <b>SubtitleFile</b>    A new instance of the current SubtitleFile class.  
	 */
    protected abstract SubtitleFile getNewInstance();

    /**
	 * Method append.
	 * <br><b>Summary:</b><br>
	 * Use this method to append a Subtitle file, to the current one.
	 * Using the given delay before last ST of current one and first ST of the one to append.
	 * @param subtitleFileToAppend  The <b>SubtitleFile</b> subtitle file to append.   
	 * @param delay                 The <b>int</b> delay to use.
	 */
    public void append(SubtitleFile subtitleFileToAppend, int delay) {
        if (subtitleFileToAppend.subtitleList.size() != 0) {
            subtitleFileToAppend.shiftToZero();
        }
        int lastSubtitleEndDate = 0;
        if (subtitleList.size() != 0) {
            lastSubtitleEndDate = ((Subtitle) subtitleList.get(subtitleList.size() - 1)).getEndDate();
        }
        subtitleFileToAppend.delay(lastSubtitleEndDate);
        if (delay > 0) {
            subtitleFileToAppend.delay(delay);
        }
        for (Subtitle currentSubtitle : subtitleFileToAppend.getSubtitles()) {
            addSubtitle(currentSubtitle, true);
        }
    }

    /**
	 * Method getSubtitles.
	 * <br><b>Summary:</b><br>
	 * return the subtitle list.
	 * @return  <b>ArrayList</b>        The subtitle list.
	 */
    protected ArrayList<Subtitle> getSubtitles() {
        return subtitleList;
    }

    /**
	 * @return Returns the fileChanged.
	 */
    public boolean isFileChanged() {
        return fileChanged;
    }

    /**
	 * Method <b>fileChanged</b>
	 * <br><b>Summary:</b><br>
	 * Set the fileChanged status flag to true.
	 */
    public void fileChanged() {
        fileChanged = true;
    }

    /**
	 * Method accentRepair.
	 * <br><b>Summary:</b><br>
	 * This method is called when user want to remove 
	 * the accents and special characters from the given index.
	 * If no index is precised, it will remove accents from all the Subtitles.
	 * @param selectedIndex     The index to remove the accents.
	 */
    public void accentRepair(int[] selectedIndex) {
        Trace.trace("Accent repair.", Trace.ALGO_PRIORITY);
        if (selectedIndex == null || selectedIndex.length == 0) {
            for (Subtitle currentSubtitle : subtitleList) {
                currentSubtitle.accentRemove();
            }
        } else {
            for (int i = 0; i < selectedIndex.length; i++) {
                Subtitle currentSubtitle = (Subtitle) subtitleList.get(selectedIndex[i]);
                currentSubtitle.accentRemove();
            }
        }
        fileChanged = true;
    }

    /**
	 * Method htmlRepair.
	 * <br><b>Summary:</b><br>
	 * This method is called when user want to remove 
	 * the htmls.
	 * If no index is precised, it will remove html tags from all the Subtitles.
	 * @param selectedIndex     The index to remove the accents.
	 */
    public void htmlRepair(int[] selectedIndex) {
        Trace.trace("HTML repair.", Trace.ALGO_PRIORITY);
        if (selectedIndex == null || selectedIndex.length == 0) {
            for (Subtitle currentSubtitle : subtitleList) {
                currentSubtitle.htmlRemove();
            }
        } else {
            for (int i = 0; i < selectedIndex.length; i++) {
                Subtitle currentSubtitle = (Subtitle) subtitleList.get(selectedIndex[i]);
                currentSubtitle.htmlRemove();
            }
        }
        fileChanged = true;
    }

    /**
	 * Method timeRepair.
	 * <br><b>Summary:</b><br>
	 * This method is called when user want to time repair.
	 * It will correct the time superposition problem.
	 * When subtitle ends after next ST start time.
	 */
    public void timeRepair() {
        Trace.trace("Time repair.", Trace.ALGO_PRIORITY);
        int time = 0;
        for (Subtitle currentSubtitle : subtitleList) {
            if (currentSubtitle.getStartDate() < time) {
                currentSubtitle.setStartDate(time + 1);
            }
            if (currentSubtitle.getEndDate() < currentSubtitle.getStartDate()) {
                currentSubtitle.setEndDate(currentSubtitle.getStartDate() + 1);
            }
            time = currentSubtitle.getEndDate();
        }
        fileChanged = true;
    }

    /**
	 * Method orderRepair.
	 * <br><b>Summary:</b><br>
	 * This method is called when user want to repair the order of the subtitle file.
	 * It will check chronology, order ST's with their start time, and finally fix ST's numbers.
	 */
    public void orderRepair() {
        Trace.trace("Order repair.", Trace.ALGO_PRIORITY);
        Collections.sort(subtitleList);
        for (int i = 0; i < subtitleList.size(); i++) {
            Subtitle currentSubtitle = (Subtitle) subtitleList.get(i);
            currentSubtitle.setNumber(i + 1);
        }
        fileChanged = true;
    }

    /**
	 * Method getSubtitleIndex.
	 * <br><b>Summary:</b><br>
	 * This method is used to know the subtitle that should be active at the given date.
	 * @param date              The date (in milliseconds).
	 * @return <b>Subtitle</b>  The subtitle.
	 */
    public Subtitle getSubtitleAtDate(int date) {
        Subtitle result = null;
        for (int i = 0; i < subtitleList.size(); i++) {
            Subtitle subtitle = (Subtitle) subtitleList.get(i);
            if ((result == null) || ((subtitle.getStartDate() > (result.getStartDate())) && (subtitle.getStartDate() <= date))) {
                result = subtitle;
            }
        }
        return result;
    }

    /**
	 * Method magicResynchro.
	 * <br><b>Summary:</b><br>
	 * This method permits to perform a magic resynchro, using the defined anchors.
	 */
    public void magicResynchro() {
        ArrayList<Double> xList = new ArrayList<Double>();
        ArrayList<Double> yList = new ArrayList<Double>();
        for (Subtitle subtitle : subtitleList) {
            if (subtitle.isAnchored()) {
                xList.add((double) subtitle.getStartDate());
                yList.add((double) (subtitle.getAnchor() - subtitle.getStartDate()));
            }
        }
        double[] x = new double[xList.size()];
        double[] y = new double[yList.size()];
        for (int i = 0; i < x.length; i++) {
            x[i] = xList.get(i).doubleValue();
            y[i] = yList.get(i).doubleValue();
        }
        getLinearInterpolation(x, y);
        for (Subtitle subtitle : subtitleList) {
            subtitle.delay((int) linearInterpolation.interpolate(subtitle.getStartDate()));
        }
        fileChanged = true;
    }

    private void getLinearInterpolation(double[] x, double[] y) {
        if (linearInterpolation == null) {
            linearInterpolation = new LinearInterpolation(x, y);
        }
    }

    /**
	 * Mix subtitle files keeps times and copy subtiles from another subtitle file.
	 * <br>Mix subtitle files keeps subtitles and copy times from another subtitke file.
	 * <br>This method only works with two subtitle file with the same numer of subtitles
	 * @param theOtherSubtitleFile 	a <code>SubtitleFile</code> object
	 * @param keepSubtitles			true, the subtitles are kept, false the times are kept
	 * @return						true if <code>this</code> are changed, false if not
	 */
    public boolean mixWithAnotherSubtitleFile(SubtitleFile theOtherSubtitleFile, boolean keepSubtitles) {
        if (this.subtitleList.size() == theOtherSubtitleFile.subtitleList.size()) {
            if (keepSubtitles) {
                Trace.trace("Keep subtitles and change times", Trace.MESSAGE_PRIORITY);
                for (int i = 0; i < this.subtitleList.size(); i++) {
                    ((Subtitle) this.subtitleList.get(i)).setStartDate(((Subtitle) theOtherSubtitleFile.subtitleList.get(i)).getStartDate());
                    ((Subtitle) this.subtitleList.get(i)).setEndDate(((Subtitle) theOtherSubtitleFile.subtitleList.get(i)).getEndDate());
                }
            } else {
                Trace.trace("Keep times and change subtitles", Trace.MESSAGE_PRIORITY);
                for (int i = 0; i < this.subtitleList.size(); i++) {
                    ((Subtitle) this.subtitleList.get(i)).setSubtitle(((Subtitle) theOtherSubtitleFile.subtitleList.get(i)).getSubtitle());
                }
            }
            fileChanged = true;
        }
        return fileChanged;
    }

    /**
	 * Returns an instance of a <code>SubtitleFile</code> implementation class
	 * which represents the subtitle type of the given <code>java.io.File</code> object.
	 * <br> The class must be in the same package of the SubtitleFile class
	 * an its name must be format like: "Fileextension" + "File" 
	 * <br>Example: for srt file, <code>SrtFile</code>)
	 * @param file 				the subtitle file
	 * @param subtitleList 		the subtitles list
	 * @param useEmptyConstructor  	True if you want to call the empty constructor of the instance. false If you want the full constructor of subtitleFile to be called (will parse the file)
	 * @return 					an instance of a <code>SubtitleFile</code> implementation class, 
	 * 							null if an error occurs
	 * @throws FileConversionException if a limitation when reading the file appears
	 * @throws Exception // --temporary-- //
	 */
    public static SubtitleFile getInstance(File file, ArrayList<Subtitle> subtitleList, String charset, boolean useEmptyConstructor) throws FileConversionException, Exception {
        SubtitleFile result = null;
        String extension = Utils.getExtension(file);
        extension = extension.substring(0, 1).toUpperCase() + extension.substring(1).toLowerCase();
        try {
            Class<?> subtitleFileClass = Class.forName(SubtitleFile.class.getPackage().getName() + "." + extension + "File");
            if (!useEmptyConstructor) {
                Constructor<?> constructorClass = subtitleFileClass.getConstructor(File.class, ArrayList.class, String.class);
                result = (SubtitleFile) constructorClass.newInstance(file, subtitleList, charset);
            } else {
                Constructor<?> constructorClass = subtitleFileClass.getConstructor();
                result = (SubtitleFile) constructorClass.newInstance();
            }
        } catch (ClassNotFoundException e) {
            throw FileConversionException.getUnsupportedFileFormatException(file, extension);
        } catch (InvocationTargetException e) {
            Throwable wrappedException = e.getCause();
            if (wrappedException instanceof FileConversionException) {
                throw (FileConversionException) wrappedException;
            } else {
                throw (Exception) e;
            }
        } catch (NullPointerException e) {
            throw e;
        } catch (Exception e) {
            throw e;
        }
        return result;
    }

    /**
	 * Returns an instance of a <code>SubtitleFile</code> implementation class
	 * which represents the subtitle type of the given <code>java.io.File</code> object.
	 * <br> The class must be in the same package of the SubtitleFile class
	 * an its name must be format like: "Fileextension" + "File" 
	 * <br>Example: for srt file, <code>SrtFile</code>)
	 * @param file 				the subtitle file
	 * @param subtitleList 		the subtitles list
	 * @return 					an instance of a <code>SubtitleFile</code> implementation class, 
	 * 							null if an error occurs
	 * @throws FileConversionException if a limitation when reading the file appears
	 * @throws Exception // --temporary-- //
	 */
    public static SubtitleFile getInstance(File file, ArrayList<Subtitle> subtitleList, String charset) throws FileConversionException, Exception {
        return getInstance(file, subtitleList, charset, false);
    }

    /**
	 * Same that {@link #getInstance(File, ArrayList, String)}.
	 * <br>Charset is set to the default one.
	 * @param file			the subtitle file
	 * @param subtitleList	the subtitle list to fill
	 * @return				the new created <tt>SubtitleFile</tt> object
	 * @throws FileConversionException	if an error occurs
	 * @throws Exception				if an untraitable error occurs [to solved]
	 */
    public static SubtitleFile getInstance(File file, ArrayList<Subtitle> subtitleList) throws FileConversionException, Exception {
        return getInstance(file, subtitleList, null);
    }

    /**
	 * Method getSubtitleList.
	 * <br><b>Summary:</b><br>
	 * Return the subtitleList.
	 * @return the subtitleList
	 */
    public ArrayList<Subtitle> getSubtitleList() {
        return subtitleList;
    }

    /**
	 * Method setSubtitleList.
	 * <br><b>Summary:</b><br>
	 * Set the subtitleList.
	 * @param subtitleList the subtitleList to set
	 */
    public void setSubtitleList(ArrayList<Subtitle> subtitleList) {
        this.subtitleList.clear();
        this.subtitleList.addAll(subtitleList);
        fileChanged = true;
    }

    /**
	 * Method getSubtitleListClone.
	 * <br><b>Summary:</b><br>
	 * returns a clone of the subtitle list.
	 * All the Subtitles are cloned.
	 * @return  (<b>ArrayList<Subtitle></b>)   A clone of the subtitle list. All Subtitles are cloned.
	 */
    public ArrayList<Subtitle> getSubtitleListClone() {
        ArrayList<Subtitle> result = new ArrayList<Subtitle>();
        for (Subtitle subtitle : subtitleList) {
            result.add(subtitle.cloneSubtitle());
        }
        return result;
    }
}
