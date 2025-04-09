package vi.regex;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class AbstractRegexHighlighter {

    protected abstract void run();

    protected abstract List<String> getTextInLines();

    protected abstract String getRegex();

    protected abstract String getReplaceText();

    protected abstract String getReplacementText();

    protected abstract String getSplitText();

    protected abstract int getRegexOptions();

    protected abstract void highlight(int start, int end, int lineNumber, int groupNumber);

    protected void parse() {
        findHighlights();
    }

    protected String[] split() {
        findHighlights();
        return getSplitText().split(getRegex());
    }

    protected String replace() {
        findHighlights();
        return getReplaceText().replaceAll(getRegex(), getReplacementText());
    }

    private void findHighlights() {
        Pattern p = Pattern.compile(getRegex(), getRegexOptions());
        Matcher m = p.matcher("");
        List<String> lines = getTextInLines();
        for (int i = 0; i < lines.size(); i++) {
            m.reset(lines.get(i));
            while (m.find()) {
                int groupCount = m.groupCount();
                for (int j = 0; j <= groupCount; j++) {
                    highlightMatched(m.start(j), m.end(j), i, j);
                }
            }
        }
    }

    private void highlightMatched(int start, int end, int lineNumber, int groupNumber) {
        if (start != -1 && end != -1) {
            highlight(start, end, lineNumber, groupNumber);
        }
    }
}
