package dakside.reports.arl;

import dakside.reports.arl.blocks.CodeBlock;
import dakside.reports.arl.blocks.EchoCodeBlock;
import dakside.reports.arl.blocks.ForCodeBlock;
import dakside.reports.arl.blocks.IfConditionBlock;
import dakside.reports.arl.blocks.IfCodeBlock;
import dakside.reports.arl.blocks.StaticCodeBlock;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Stack;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Active report language parser
 * @author takaji
 */
public class ARParser {

    public static CodeBlock parse(InputStream stream) {
        return parse(new BufferedReader(new InputStreamReader(stream)));
    }

    /**
     * Parse a string to active report code
     * @param reader
     * @return
     */
    public static CodeBlock parse(BufferedReader reader) {
        Stack<CodeBlock> blockStack = new Stack<CodeBlock>();
        CodeBlock thisBlock = new CodeBlock();
        StringBuffer buffer = new StringBuffer();
        try {
            String nextLine = null;
            while ((nextLine = reader.readLine()) != null) {
                buffer.append(nextLine).append("\r\n");
            }
        } catch (IOException ex) {
            Logger.getLogger(ARParser.class.getName()).log(Level.SEVERE, null, ex);
        }
        Pattern pattern = Pattern.compile(PATTERN);
        Matcher matcher = pattern.matcher(buffer);
        int lastIndex = 0;
        while (matcher.find()) {
            if (matcher.start() - lastIndex > 1) {
                thisBlock.add(new StaticCodeBlock(buffer.substring(lastIndex, matcher.start())));
                lastIndex = matcher.end();
            }
            if (matcher.group(ECHO) != null) {
                thisBlock.add(new EchoCodeBlock(matcher.group(ECHO)));
            } else if (matcher.group(IF_IDX) != null) {
                IfCodeBlock ifBlock = new IfCodeBlock();
                IfConditionBlock ifCondition = new IfConditionBlock(matcher.group(IF_VAR));
                ifBlock.addCondition(ifCondition);
                thisBlock.add(ifBlock);
                blockStack.push(thisBlock);
                blockStack.push(ifBlock);
                thisBlock = ifCondition.getBlock();
            } else if (matcher.group(FOR_IDX) != null) {
                ForCodeBlock forBlock = new ForCodeBlock(matcher.group(FOR_ELEM), matcher.group(FOR_ARR));
                thisBlock.add(forBlock);
                blockStack.push(thisBlock);
                thisBlock = forBlock;
            } else if (matcher.group(ELSEIF) != null) {
                if (blockStack.isEmpty()) {
                    throw new RuntimeException("Invalid template");
                }
                thisBlock = blockStack.pop();
                if (thisBlock instanceof IfCodeBlock) {
                    IfConditionBlock ifCondition = new IfConditionBlock(matcher.group(ELSEIF_VAR));
                    ((IfCodeBlock) thisBlock).addCondition(ifCondition);
                    blockStack.push(thisBlock);
                    thisBlock = ifCondition.getBlock();
                } else {
                    throw new RuntimeException("invalid elseif statement");
                }
            } else if (matcher.group(ELSE) != null) {
                if (blockStack.isEmpty()) {
                    throw new RuntimeException("Invalid template");
                }
                thisBlock = blockStack.pop();
                if (thisBlock instanceof IfCodeBlock) {
                    IfConditionBlock ifCondition = new IfConditionBlock();
                    ((IfCodeBlock) thisBlock).addCondition(ifCondition);
                    blockStack.push(thisBlock);
                    thisBlock = ifCondition.getBlock();
                } else {
                    throw new RuntimeException("invalid elseif statement");
                }
            } else if (matcher.group(ENDFOR) != null) {
                if (blockStack.isEmpty()) {
                    throw new RuntimeException("Invalid template");
                }
                thisBlock = blockStack.pop();
            } else if (matcher.group(ENDIF) != null) {
                if (blockStack.size() < 2) {
                    throw new RuntimeException("Invalid template");
                }
                thisBlock = blockStack.pop();
                thisBlock = blockStack.pop();
            }
        }
        if (lastIndex < buffer.length()) {
            thisBlock.add(new StaticCodeBlock(buffer.substring(lastIndex, buffer.length())));
        }
        return thisBlock;
    }

    private static final String PATTERN = "(?:(?:\\{%){1} *(if){1} +([a-zA-z0-9\\[\\]\\.]*) *(?:%\\}){1})" + "|(?:(?:\\{%){1} *(for){1} +([a-zA-z0-9\\[\\]\\.]*) +in +([a-zA-z0-9\\[\\]\\.]*) *(?:%\\}){1})" + "|(?:(?:\\{%){1} *(endfor){1} *(?:%\\}){1})" + "|(?:(?:\\{%){1} *(endif){1} *(?:%\\}){1})" + "|(?:(?:\\{\\{) *([a-zA-z0-9_\\[\\]\\.]*) *(?:\\}\\}))" + "|(?:(?:\\{%){1} *(elseif){1} +([a-zA-z0-9\\[\\]\\.]*) *(?:%\\}){1})" + "|(?:(?:\\{%){1} *(else){1} *(?:%\\}){1})";

    private static final int IF_IDX = 1;

    private static final int IF_VAR = 2;

    private static final int FOR_IDX = 3;

    private static final int FOR_ELEM = 4;

    private static final int FOR_ARR = 5;

    private static final int ENDFOR = 6;

    private static final int ENDIF = 7;

    private static final int ECHO = 8;

    private static final int ELSEIF = 9;

    private static final int ELSEIF_VAR = 10;

    private static final int ELSE = 11;
}
