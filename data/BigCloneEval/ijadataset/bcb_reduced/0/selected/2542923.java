package org.codehaus.groovy.grails.web.pages;

import java.io.PrintWriter;
import java.io.Writer;

/**
 * A PrintWriter used in the generation of GSP pages that allows printing to the target output stream and
 * maintains a record of the current line number during usage.
 *
 * @author Graeme Rocher
 * @since 13-Jan-2006
 */
public class GSPWriter extends PrintWriter {

    private int lineNumber = 1;

    private int[] lineNumbers = new int[1000];

    private Parse parse;

    public GSPWriter(Writer out, Parse parse) {
        super(out);
        this.parse = parse;
    }

    public void write(char buf[], int off, int len) {
        super.write(buf, off, len);
    }

    public void printlnToResponse(String s) {
        if (s == null) s = "''";
        super.print("out.print(");
        super.print(s);
        super.print(")");
        println();
    }

    public void printlnToBuffer(String s, int index) {
        if (s == null) s = "''";
        super.print("buf" + index + " << ");
        super.print(s);
        println();
    }

    public void println() {
        if (lineNumber >= lineNumbers.length) {
            lineNumbers = (int[]) resizeArray(lineNumbers, lineNumbers.length * 2);
        } else {
            lineNumbers[lineNumber - 1] = parse.getCurrentOutputLineNumber();
            lineNumber++;
        }
        super.println();
    }

    private Object resizeArray(Object oldArray, int newSize) {
        int oldSize = java.lang.reflect.Array.getLength(oldArray);
        Class elementType = oldArray.getClass().getComponentType();
        Object newArray = java.lang.reflect.Array.newInstance(elementType, newSize);
        int preserveLength = Math.min(oldSize, newSize);
        if (preserveLength > 0) System.arraycopy(oldArray, 0, newArray, 0, preserveLength);
        return newArray;
    }

    public int getCurrentLineNumber() {
        return this.lineNumber;
    }

    public int[] getLineNumbers() {
        return lineNumbers;
    }
}
