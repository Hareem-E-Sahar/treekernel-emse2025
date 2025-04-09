package net.sf.logsaw.dialect.pattern;

import java.text.SimpleDateFormat;
import java.util.BitSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.sf.logsaw.dialect.pattern.internal.Messages;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Philipp Nanz
 */
public class RegexUtils {

    private static transient Logger logger = LoggerFactory.getLogger(RegexUtils.class);

    /**
	 * Returns the Regex lazy suffix for the given rule.
	 * @param rule the conversion rule
	 * @return the Regex lazy suffix
	 */
    public static String getLazySuffix(ConversionRule rule) {
        if (rule.isFollowedByQuotedString()) {
            return "?";
        } else {
            return "";
        }
    }

    /**
	 * Returns the Regex length hint for the given rule.
	 * @param rule the conversion rule
	 * @return the Regex length hint
	 */
    public static String getLengthHint(ConversionRule rule) {
        if ((rule.getMaxWidth() > 0) && (rule.getMaxWidth() == rule.getMinWidth())) {
            return "{" + rule.getMaxWidth() + "}";
        } else if (rule.getMaxWidth() > 0) {
            return "{" + Math.max(0, rule.getMinWidth()) + "," + rule.getMaxWidth() + "}";
        } else if (rule.getMinWidth() > 0) {
            return "{" + rule.getMinWidth() + ",}";
        }
        return "";
    }

    /**
	 * Converts a given <code>java.lang.SimpleDateFormat</code> pattern into 
	 * a regular expression
	 * @param format the pattern
	 * @return the translated pattern
	 * @throws CoreException if an error occurred
	 */
    public static String getRegexForSimpleDateFormat(String format) throws CoreException {
        RegexUtils utils = new RegexUtils();
        return utils.doGetRegexForSimpleDateFormat(format);
    }

    private String doGetRegexForSimpleDateFormat(String format) throws CoreException {
        try {
            new SimpleDateFormat(format);
        } catch (Exception e) {
            throw new CoreException(new Status(IStatus.ERROR, PatternDialectPlugin.PLUGIN_ID, Messages.RegexUtils_error_invalidDateFormat));
        }
        ReplacementContext ctx = new ReplacementContext();
        ctx.setBits(new BitSet(format.length()));
        ctx.setBuffer(new StringBuffer(format));
        unquote(ctx);
        replace(ctx, "G+", "[ADBC]{2}");
        replace(ctx, "[y]{3,}", "\\d{4}");
        replace(ctx, "[y]{2}", "\\d{2}");
        replace(ctx, "y", "\\d{4}");
        replace(ctx, "[M]{3,}", "[a-zA-Z]*");
        replace(ctx, "[M]{2}", "\\d{2}");
        replace(ctx, "M", "\\d{1,2}");
        replace(ctx, "w+", "\\d{1,2}");
        replace(ctx, "W+", "\\d");
        replace(ctx, "D+", "\\d{1,3}");
        replace(ctx, "d+", "\\d{1,2}");
        replace(ctx, "F+", "\\d");
        replace(ctx, "E+", "[a-zA-Z]*");
        replace(ctx, "a+", "[AMPM]{2}");
        replace(ctx, "H+", "\\d{1,2}");
        replace(ctx, "k+", "\\d{1,2}");
        replace(ctx, "K+", "\\d{1,2}");
        replace(ctx, "h+", "\\d{1,2}");
        replace(ctx, "m+", "\\d{1,2}");
        replace(ctx, "s+", "\\d{1,2}");
        replace(ctx, "S+", "\\d{1,3}");
        replace(ctx, "z+", "[a-zA-Z-+:0-9]*");
        replace(ctx, "Z+", "[-+]\\d{4}");
        return ctx.getBuffer().toString();
    }

    private void unquote(ReplacementContext ctx) {
        Pattern p = Pattern.compile("'[^']+'");
        Matcher m = p.matcher(ctx.getBuffer().toString());
        while (m.find()) {
            logger.trace(ctx.toString());
            int offset = -2;
            for (int i = m.end(); i < ctx.getBuffer().length(); i++) {
                ctx.getBits().set(i + offset, ctx.getBits().get(i));
            }
            for (int i = m.start(); i < m.end() + offset; i++) {
                ctx.getBits().set(i);
            }
            ctx.getBuffer().replace(m.start(), m.start() + 1, "");
            ctx.getBuffer().replace(m.end() - 2, m.end() - 1, "");
            logger.trace(ctx.toString());
        }
        p = Pattern.compile("''");
        m = p.matcher(ctx.getBuffer().toString());
        while (m.find()) {
            logger.trace(ctx.toString());
            int offset = -1;
            for (int i = m.end(); i < ctx.getBuffer().length(); i++) {
                ctx.getBits().set(i + offset, ctx.getBits().get(i));
            }
            for (int i = m.start(); i < m.end() + offset; i++) {
                ctx.getBits().set(i);
            }
            ctx.getBuffer().replace(m.start(), m.start() + 1, "");
            logger.trace(ctx.toString());
        }
    }

    private void replace(ReplacementContext ctx, String regex, String replacement) {
        Pattern p = Pattern.compile(regex);
        Matcher m = p.matcher(ctx.getBuffer().toString());
        while (m.find()) {
            logger.trace(regex);
            logger.trace(ctx.toString());
            int idx = ctx.getBits().nextSetBit(m.start());
            if ((idx == -1) || (idx > m.end() - 1)) {
                int len = m.end() - m.start();
                int offset = replacement.length() - len;
                if (offset > 0) {
                    for (int i = ctx.getBuffer().length() - 1; i > m.end(); i--) {
                        ctx.getBits().set(i + offset, ctx.getBits().get(i));
                    }
                } else if (offset < 0) {
                    for (int i = m.end(); i < ctx.getBuffer().length(); i++) {
                        ctx.getBits().set(i + offset, ctx.getBits().get(i));
                    }
                }
                for (int i = m.start(); i < m.end() + offset; i++) {
                    ctx.getBits().set(i);
                }
                ctx.getBuffer().replace(m.start(), m.end(), replacement);
                logger.trace(ctx.toString());
            }
        }
    }

    private class ReplacementContext {

        private BitSet bits;

        private StringBuffer buffer;

        /**
		 * @return the bits
		 */
        public BitSet getBits() {
            return bits;
        }

        /**
		 * @param bits the bits to set
		 */
        public void setBits(BitSet bits) {
            this.bits = bits;
        }

        /**
		 * @return the buffer
		 */
        public StringBuffer getBuffer() {
            return buffer;
        }

        /**
		 * @param buffer the buffer to set
		 */
        public void setBuffer(StringBuffer buffer) {
            this.buffer = buffer;
        }

        @Override
        public String toString() {
            StringBuffer sb = new StringBuffer();
            sb.append("ReplacementContext [bits=");
            for (int i = 0; i < buffer.length(); i++) {
                sb.append(bits.get(i) ? '1' : '0');
            }
            sb.append(", buffer=");
            sb.append(buffer);
            sb.append(']');
            return sb.toString();
        }
    }
}
