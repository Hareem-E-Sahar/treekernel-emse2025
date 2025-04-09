import org.eclipse.swt.*;
import org.eclipse.swt.custom.*;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.TraverseEvent;
import org.eclipse.swt.events.TraverseListener;
import org.eclipse.swt.widgets.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.eclipse.swt.graphics.Color;

public class SyntaxHighlighter implements LineStyleListener, LineBackgroundListener, TraverseListener, MouseListener {

    protected Display display;

    protected Label label;

    protected StyledText text;

    public SyntaxHighlighter(StyledText text, Label label, Display display) {
        this.text = text;
        this.label = label;
        this.display = display;
    }

    public void lineGetStyle(LineStyleEvent e) {
        java.util.List styles = new java.util.ArrayList();
        Pattern pattern;
        Matcher matcher;
        if (e.lineText.trim().length() > 0) if (e.lineText.trim().charAt(0) != ';') {
            for (int i = 0; i < reservedWords.length; i++) {
                pattern = Pattern.compile("\\s" + reservedWords[i] + "(?![^;\\s]+)");
                matcher = pattern.matcher("\n" + e.lineText.toUpperCase().split(";")[0] + "\n");
                while (matcher.find()) {
                    styles.add(new StyleRange(e.lineOffset + matcher.start(), reservedWords[i].length(), new Color(display, 0, 0, 204), null, SWT.BOLD));
                }
            }
            for (int i = 0; i < registers.length; i++) {
                pattern = Pattern.compile("(\\s|,)" + registers[i] + "(?![^,;\\s]+)");
                matcher = pattern.matcher("\n" + e.lineText.toUpperCase().split(";")[0] + "\n");
                while (matcher.find()) {
                    styles.add(new StyleRange(e.lineOffset + matcher.start(), registers[i].length(), new Color(display, 102, 0, 51), null, SWT.BOLD));
                }
            }
            for (int i = 0; i < declarations.length; i++) {
                pattern = Pattern.compile("\\s" + declarations[i] + "(?![^;\\s]+)");
                matcher = pattern.matcher("\n" + e.lineText.toUpperCase().split(";")[0] + "\n");
                while (matcher.find()) {
                    styles.add(new StyleRange(e.lineOffset + matcher.start(), declarations[i].length(), new Color(this.display, 100, 100, 100), null, SWT.BOLD));
                }
            }
            pattern = Pattern.compile("^\\s\\w+:");
            matcher = pattern.matcher("\n" + e.lineText.toUpperCase().split(";")[0] + "\n");
            while (matcher.find()) {
                styles.add(new StyleRange(e.lineOffset + matcher.start() - 1, matcher.end() - (matcher.start()), null, null, SWT.BOLD));
            }
            pattern = Pattern.compile("(\\s|,)((0B([0-1]+))|(0X([0-9A-F]+))|([0-9]+))(?![^;\\s]+)");
            matcher = pattern.matcher("\n" + e.lineText.toUpperCase().split(";")[0] + "\n");
            while (matcher.find()) {
                styles.add(new StyleRange(e.lineOffset + matcher.start(), matcher.end() - matcher.start(), new Color(display, 240, 51, 0), null));
            }
            pattern = Pattern.compile("(\"[^\"]*\"|'[^']*')");
            matcher = pattern.matcher("\n" + e.lineText.toUpperCase().split(";")[0] + "\n");
            while (matcher.find()) {
                styles.add(new StyleRange(e.lineOffset + matcher.start() - 1, matcher.end() - (matcher.start()), new Color(display, 204, 0, 0), null));
            }
        }
        pattern = Pattern.compile("\\Q;\\E");
        matcher = pattern.matcher(e.lineText);
        if (matcher.find()) styles.add(new StyleRange(e.lineOffset + matcher.start(), e.lineText.length() - matcher.start(), new Color(display, 63, 127, 95), null, SWT.ITALIC));
        e.styles = (StyleRange[]) styles.toArray(new StyleRange[0]);
    }

    public void lineGetBackground(LineBackgroundEvent e) {
        if (e.lineOffset < text.getCaretOffset() + 1 && text.getCaretOffset() - 1 < e.lineOffset + e.lineText.length()) e.lineBackground = new Color(display, 220, 220, 255); else e.lineBackground = text.getBackground();
    }

    public void keyTraversed(TraverseEvent e) {
        text.setBackground(null);
        refreshLineCount();
    }

    public void mouseDown(org.eclipse.swt.events.MouseEvent arg0) {
        text.setBackground(null);
        refreshLineCount();
    }

    public void refreshLineCount() {
        StringBuffer b = new StringBuffer();
        int topLine = text.getTopIndex();
        String leadingZeros = "";
        if (topLine != lastLine) {
            System.out.println(topLine);
            for (int i = topLine; i < topLine + 38; i++) {
                leadingZeros = "";
                if (i < 100) leadingZeros = "0";
                if (i < 10) leadingZeros = "00";
                b.append(leadingZeros + i + "\n");
            }
            label.setText(b.toString());
            lastLine = topLine;
        }
    }

    public void mouseDoubleClick(org.eclipse.swt.events.MouseEvent arg0) {
    }

    public void mouseUp(org.eclipse.swt.events.MouseEvent arg0) {
    }

    protected int lastLine = 0;

    protected final String reservedWords[] = { "AAA", "AAD", "AAM", "AAS", "ADC", "ADD", "AND", "CALL", "CBW", "CLC", "CLD", "CLI", "CMC", "CMP", "CMPSB", "CMPSW", "CWD", "DAA", "DAS", "DEC", "DIV", "HLT", "IDIV", "IMUL", "IN", "INC", "INT", "INTO", "IRET", "JA", "JAE", "JB", "JBE", "JC", "JCXZ", "JE", "JG", "JGE", "JL", "JLE", "JMP", "JNA", "JNAE", "JNB", "JNBE", "JNC", "JNE", "JNG", "JNGE", "JNL", "JNLE", "JNO", "JNP", "JNS", "JNZ", "JO", "JP", "JPE", "JPO", "JS", "JZ", "LAHF", "LDS", "LEA", "LES", "LODSB", "LODSW", "LOOP", "LOOPE", "LOOPNE", "LOOPNZ", "LOOPZ", "MOV", "MOVSB", "MOVSW", "MUL", "NEG", "NOP", "NOT", "OR", "OUT", "POP", "POPA", "POPF", "PUSH", "PUSHA", "PUSHF", "RCL", "RCR", "REP", "REPE", "REPNE", "REPNZ", "REPZ", "RET", "RETF", "ROL", "ROR", "SAHF", "SAL", "SAR", "SBB", "SCASB", "SCASW", "SHL", "SHR", "STC", "STD", "STI", "STOSB", "STOSW", "SUB", "TEST", "XCHG", "XLATB", "XOR" };

    protected final String declarations[] = { "DB", "DW" };

    protected final String registers[] = { "AX", "AH", "AL", "BX", "BH", "BL", "CX", "CH", "CL", "DX", "DH", "DL", "BP", "SP", "BI", "SI" };
}
