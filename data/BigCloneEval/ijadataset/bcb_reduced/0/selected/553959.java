package org.xiaoniu.suafe;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.lang.reflect.Array;
import org.xiaoniu.suafe.exceptions.AppException;

/**
 * Generic utility methods.
 * 
 * @author Shaun Johnson
 */
public final class Utilities {

    /**
	 * Converts an array of Objects into an array of <T>.
	 * 
	 * @param <T> Any type
	 * @param array Array of <T> objects
	 * @param typeSample Sample type, matching <T>
	 * @return Array of <T> objects
	 */
    @SuppressWarnings("unchecked")
    public static <T> T[] convertToArray(Object[] array, T[] typeSample) {
        if (typeSample.length < array.length) typeSample = (T[]) Array.newInstance(typeSample.getClass().getComponentType(), array.length);
        System.arraycopy(array, 0, typeSample, 0, array.length);
        if (typeSample.length > array.length) typeSample[array.length] = null;
        return typeSample;
    }

    /**
	 * Open output file.
	 * 
	 * @param filePath Path of output file
	 * @return Output stream for file
	 * @throws AppException Error occurred
	 */
    public static PrintStream openOutputFile(String filePath) throws AppException {
        PrintStream output = null;
        try {
            output = new PrintStream(new File(filePath));
        } catch (FileNotFoundException fne) {
            throw new AppException("generator.filenotfound");
        } catch (Exception e) {
            throw new AppException("generator.error");
        }
        return output;
    }

    /**
	 * Open output file.
	 * 
	 * @param file File object representing the output file
	 * @return Output stream for file
	 * @throws AppException Error occurred
	 */
    public static PrintStream openOutputFile(File file) throws AppException {
        PrintStream output = null;
        try {
            output = openOutputFile(file.getCanonicalPath());
        } catch (IOException e) {
            throw new AppException("generator.error");
        }
        return output;
    }
}
