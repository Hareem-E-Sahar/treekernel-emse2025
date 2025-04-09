package src.tasks;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import src.Resources;
import src.Statistics;
import src.project.file.WikiFile;
import src.resources.RegExpressions;
import src.tasks.Tasks.Task;
import src.utilities.HandlerLists;

/**
 * <p>This task creates HTML lists: Unordered lists, ordered lists, and definition lists.</p>
 * <ul>
 * <li>{@code * Entry} creates a unordered list entry</li>
 * <li>{@code # Entry} creates an ordered list entry</li>
 * <li>{@code ; Entry} creates a defnition term</li>
 * <li>{@code : Entry} creates a definition definition</li>
 * </ul>
 * <p>Examples:</p>
 * <p><code>* Level 1<br/>** Level 2</code></p>
 * <p><code>; Definition<br/>: A definition is <a href="http://en.wikipedia.org/wiki/Definition">this</a></code></p>
 */
public class WikiLists extends WikiTask {

    public Task desc() {
        return Task.Lists;
    }

    public WikiTask nextTask() {
        return new WikiGalleries();
    }

    /**
	 * Build the Lists with a *, # at the beginning.
	 */
    public void parse(WikiFile file) {
        Statistics.getInstance().sw.timeCreatingLists.continueTime();
        StringBuffer out = new StringBuffer();
        BufferedReader b = new BufferedReader(new StringReader(file.getContent().toString()));
        short counter = 0;
        String allowedChars = "*#;:";
        HandlerLists newList = new HandlerLists(), oldList = new HandlerLists();
        int posArgument;
        boolean fileStart = true;
        StringBuffer argument = new StringBuffer();
        String firstArgument = new String();
        StringBuffer content = new StringBuffer();
        try {
            int i;
            for (String line = b.readLine(); line != null; line = b.readLine()) {
                if (line.trim().length() == 0) {
                    out.append(line + "\n");
                    continue;
                }
                i = 0;
                oldList = newList.clone();
                newList.clear();
                newList.addSameBase(":;");
                newList.allowChars(allowedChars);
                newList.parse(line);
                if ((i = newList.equalEntries(oldList)) >= 1) {
                    String replace = oldList.levels().substring(0, i);
                    if ((i = newList.size() - 1) < replace.length()) replace = replace.substring(0, i);
                    newList.replace(replace);
                }
                if (newList.size() > 0) {
                    Matcher m = RegExpressions.listGroupArguments.matcher(line);
                    if (m.find()) {
                        firstArgument = m.group(1).trim();
                        if (firstArgument.length() > 0) firstArgument = " " + firstArgument;
                        line = line.substring(m.end(), line.length());
                    } else firstArgument = "";
                }
                posArgument = line.indexOf("|");
                if (posArgument > 0 && newList.size() > 0) {
                    argument = new StringBuffer(line.substring(newList.size(), posArgument).trim());
                    content = new StringBuffer(line.subSequence(posArgument + 1, line.length()));
                    if (argument.length() != 0) if (argument.charAt(0) != ' ') argument.insert(0, ' ');
                } else {
                    argument.setLength(0);
                    content = new StringBuffer(line.subSequence(newList.size(), line.length()));
                }
                if (newList.size() == 0 && oldList.size() == 0) {
                    if (!fileStart) out.append('\n');
                } else {
                    newList.getDiffBetween(oldList);
                    for (i = 0; i < oldList.difference(); i++) {
                        if (oldList.size() > 0) {
                            out.append(Resources.closeItem(oldList.type(oldList.size() - i), oldList.size() - i));
                        }
                        if (oldList.size() > newList.size() - newList.difference()) {
                            out.append(Resources.closeList(oldList.type(oldList.size() - i), oldList.size() - i + 1));
                        }
                    }
                    if (oldList.size() > newList.size() && newList.size() > 0) out.append(Resources.closeItem(oldList.type(newList.size()), newList.size() + 1));
                    if (oldList.size() > 0 && newList.size() > 0 && oldList.sameStructureAs(newList)) {
                        out.append(Resources.closeItem(oldList.last(), oldList.size()));
                        out.append(Resources.openItem(newList.last(), newList.size(), argument.toString()));
                        Statistics.getInstance().counter.listItems.increase();
                    }
                    for (i = newList.difference() - 1; i >= 0; i--) {
                        if (newList.size() > (oldList.size() - oldList.difference())) {
                            out.append(Resources.openList(newList.type(newList.size() - i), newList.size() - i, (i == 0 ? firstArgument.toString() : "")));
                            Statistics.getInstance().counter.lists.increase();
                        }
                        if (newList.size() > 0) {
                            out.append(Resources.openItem(newList.type(newList.size() - i), newList.size() - i, argument.toString()));
                            Statistics.getInstance().counter.listItems.increase();
                        }
                    }
                    if (oldList.size() > newList.size() && newList.size() > 0 && oldList.nearlyEqual(newList.size(), newList)) {
                        out.append(Resources.openItem(newList.last(), newList.size(), argument.toString()));
                        Statistics.getInstance().counter.listItems.increase();
                    }
                }
                if (!fileStart) out.append(content); else if (content.length() > 0) {
                    fileStart = false;
                    out.append(content);
                }
            }
            if (newList.size() > 0) {
                for (i = 0; i < newList.size(); i++) {
                    out.append(Resources.closeItem(newList.type(newList.size() - i), newList.size() - i));
                    out.append(Resources.closeList(newList.type(newList.size() - i), newList.size() - i + 1));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        file.setContent(out);
        Matcher m = Pattern.compile("\\s*<\\/dl>\\s<dl>\\s*").matcher(file.getContent().toString());
        if (m.find()) {
            out = new StringBuffer();
            int first, last = 0;
            do {
                counter++;
                first = m.start();
                out.append(file.getContent().subSequence(last, first));
                out.append('\n');
                last = m.end();
            } while (m.find());
            out.append(file.getContent().subSequence(last, file.getContent().length()));
        }
        Statistics.getInstance().sw.timeCreatingLists.stop();
    }
}
