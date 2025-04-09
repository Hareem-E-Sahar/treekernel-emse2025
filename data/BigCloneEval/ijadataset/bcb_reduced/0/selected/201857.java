package src.project;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import src.Constants;
import src.project.file.VirtualWikiFile;
import src.resources.RegExpressions;
import src.tasks.WikiFormattings;
import src.tasks.Tasks.Task;

/**
 *
 * Creates the html code for the menu.
 *
 * @author Simon Eugster
 * ,		hb9eia
 *
 * TODO 3 Untermenu nur schreiben, wenn aktiv
 */
public class WikiMenu {

    public enum SearchLocation {

        NAME, LINK
    }

    ;

    /** Contains the whole menu */
    private ArrayList<MenuEntry> menu = new ArrayList<MenuEntry>();

    /** deactivates links if open */
    private static boolean link_deactivate = false;

    /** uses strong instead of a for open links */
    private static boolean link_strong = false;

    public WikiMenu() {
    }

    /** TODO 4 use in style.config */
    public final int MAX_DEPTH = 3;

    /** Reads the menu from a menu file */
    public void readNewMenu(String content) {
        BufferedReader b = new BufferedReader(new StringReader(content));
        ArrayList<String> al;
        String line;
        try {
            MenuEntry me = new MenuEntry();
            int[] parentItem = new int[MAX_DEPTH];
            int i = 0;
            Arrays.fill(parentItem, -1);
            for (line = b.readLine(); line != null; line = b.readLine()) {
                if (line.length() > 0 && line.charAt(0) != '#') {
                    me = new MenuEntry();
                    al = getCsvMenu(line);
                    me.readLine(al);
                    if (me.level > 0) parentItem[me.level - 1] = i;
                    for (int j = me.level; j < MAX_DEPTH; j++) parentItem[j] = -1;
                    if (me.level >= 2 && parentItem[me.level - 2] >= 0) {
                        me.parent = menu.get(parentItem[me.level - 2]);
                    }
                    menu.add(me);
                    if (me.level >= 2 && parentItem[me.level - 2] >= 0) {
                        menu.get(menu.size() - 1).parent.children.add(menu.get(menu.size() - 1));
                    }
                    i += 1;
                } else if (line.length() > 2 && line.startsWith("#!")) {
                    if (line.contains(Constants.MenuTags.linkDeactivate)) link_deactivate = true;
                    if (line.contains(Constants.MenuTags.linkDeactivateNot)) link_deactivate = false;
                    if (line.contains(Constants.MenuTags.linkStrong)) link_strong = true;
                    if (line.contains(Constants.MenuTags.linkStrongNot)) link_strong = false;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
	 * @return The generated menu in XHTML
	 */
    public synchronized StringBuffer getMenu(String searchText, SearchLocation where) {
        VirtualWikiFile vf = new VirtualWikiFile(VirtualWikiFile.createEmptyProject(), "--", false, getMenuAsList(searchText, where));
        vf.removeAllTasks();
        vf.addTask(Task.Lists);
        vf.parse();
        return vf.getContent();
    }

    /**
	 * @return The generated menu, as a Wiki-List
	 */
    public StringBuffer getMenuAsList(String searchText, SearchLocation where) {
        openItem(searchText, where);
        StringBuffer s = new StringBuffer();
        boolean openChild;
        for (MenuEntry me : menu) {
            openChild = false;
            for (MenuEntry mm : getGroup(me)) for (MenuEntry m : mm.children) if (m.isOpen()) {
                openChild = true;
                break;
            }
            s.append(me.getLine(checkOpen(getGroup(me)), (me.parent != null ? me.parent.isOpen() : false), openChild, link_deactivate, link_strong) + "\n");
        }
        return parseLineArgumentsNolist(s);
    }

    /**
	 * Opens an item. First closes all other items.
	 * @param text The item to open
	 * @return false, if the item couln't be found
	 */
    private boolean openItem(String text, SearchLocation where) {
        Vector<Short> v;
        switch(where) {
            case NAME:
                v = findByName(text);
                break;
            case LINK:
            default:
                v = findByLink(text);
                break;
        }
        closeAll();
        if (v.size() > 0) {
            for (short s = 0; s < v.size(); s++) menu.get(v.get(s)).open();
            return true;
        }
        return false;
    }

    private void closeAll() {
        for (MenuEntry me : menu) {
            me.close();
        }
    }

    /**
	 * @param me
	 * @return The group of the MenuEntry, means this and other entries of the
	 * same level which are not separated by an entry of a lower level
	 */
    private Vector<MenuEntry> getGroup(MenuEntry me) {
        Vector<MenuEntry> v = new Vector<MenuEntry>();
        if (me == null) return v;
        Vector<Short> v2 = findByName(me.name);
        if (v2.size() > 0) {
            for (short s = v2.get(0); s >= 0; s--) {
                if (menu.get(s).level == me.level) v.add(menu.get(s)); else if (menu.get(s).level < me.level) break;
            }
            for (int i = v2.get(0) + 1; i < menu.size(); i++) {
                if (menu.get(i).level == me.level) v.add(menu.get(i)); else if (menu.get(i).level < me.level) break;
            }
        }
        return v;
    }

    /**
	 * @return true, if any of the entries is open
	 */
    private boolean checkOpen(Vector<MenuEntry> v) {
        for (MenuEntry me : v) if (me.isOpen()) return true;
        return false;
    }

    /**
	 * @return An ArrayList containing the input values separated by a comma
	 */
    private ArrayList<String> getCsvMenu(String s) {
        Pattern p = Pattern.compile("(?<!\\\\),");
        Matcher m = p.matcher(s);
        ArrayList<String> al = new ArrayList<String>();
        if (m.find()) {
            int end, start = 0;
            do {
                end = m.start();
                al.add(s.substring(start, end).replaceAll("\\\\,", ","));
                start = m.end();
            } while (m.find());
            if (end < s.length()) al.add(s.substring(start, s.length()).replaceAll("\\\\,", ","));
        } else if (s.length() > 0) al.add(s);
        return al;
    }

    private Vector<Short> findByName(String name) {
        Vector<Short> v = new Vector<Short>();
        for (short i = 0; i < menu.size(); i++) if (menu.get(i).name.equals(name)) v.add(i);
        return v;
    }

    private Vector<Short> findByLink(String link) {
        Vector<Short> v = new Vector<Short>();
        for (short i = 0; i < menu.size(); i++) if (menu.get(i).link.equals(link)) v.add(i);
        return v;
    }

    private StringBuffer parseLineArgumentsNolist(StringBuffer in) {
        BufferedReader b = new BufferedReader(new StringReader(in.toString()));
        StringBuffer out = new StringBuffer();
        String line;
        String arguments;
        int pos;
        Pattern p = RegExpressions.listGroupArguments;
        try {
            Matcher m;
            while ((line = b.readLine()) != null) {
                if (!line.startsWith("*")) {
                    arguments = "";
                    m = p.matcher(line);
                    if (m.find()) {
                        line = line.substring(m.end(), line.length());
                    }
                    if ((pos = line.indexOf('|')) >= 0) {
                        arguments = line.substring(0, pos);
                        line = line.substring(pos + 1, line.length());
                    }
                    out.append((arguments.length() > 0 ? "<h3 " + arguments.trim() + ">" : "<h3>") + line.trim() + "</h3>\n");
                } else out.append(line + '\n');
            }
        } catch (IOException e) {
            e.printStackTrace();
            return in;
        }
        return out;
    }

    private class MenuEntry {

        String name = "";

        String link = "";

        String args = "";

        byte level = 0;

        boolean open = false;

        MenuEntry parent = null;

        ArrayList<MenuEntry> children = new ArrayList<MenuEntry>();

        public MenuEntry() {
        }

        /** marks the entry as open */
        public void open() {
            open = true;
        }

        /** marks the entry as closed */
        public void close() {
            open = false;
        }

        /** @return true, if the entry is marked as open */
        public boolean isOpen() {
            return open;
        }

        /** @return true, if any child of any level is open. */
        public boolean openChild() {
            for (MenuEntry me : children) {
                if (me.open || me.openChild()) return true;
            }
            return false;
        }

        /** @return true, if the (direct) parent is open. */
        public boolean openParent() {
            if (parent != null) return parent.open;
            return false;
        }

        /** @return true, if the entry has a child. */
        public boolean hasChild() {
            return (children.size() > 0);
        }

        /**
		 * Reads the settings from a line
		 * @param al
		 */
        public void readLine(ArrayList<String> al) {
            try {
                name = al.get(0);
                link = (al.size() >= 2) ? al.get(1).trim() : "";
                level = 0;
                while (name.length() > 0 && name.charAt(0) == '*') {
                    level++;
                    name = name.substring(1, name.length());
                }
                if (level > MAX_DEPTH) level = MAX_DEPTH;
                name = name.trim();
                args = (al.size() >= 3) ? al.get(2) : "";
            } catch (IndexOutOfBoundsException e) {
            }
        }

        /**
		 * @return The line for the Menu file in Wikitext
		 */
        public StringBuffer getLine(boolean openGroup, boolean openParent, boolean openChild, boolean link_deactivate, boolean link_strong) {
            StringBuffer line = new StringBuffer();
            if (name.startsWith("----")) line.append("<hr />"); else {
                if (level > 0) {
                    for (int i = 0; i < level; i++) line.append('*');
                    line.append(' ');
                }
                line.append("((class=\"" + getFirstArgs(openGroup, openParent, openChild, level) + "\")) ");
                line.append(getStyleArgs(args) + " | ");
                if (link.length() > 0) line.append((link_strong && open ? "<strong>" : "") + (link_deactivate && open ? "" : "<a href=\"" + link + "\">") + formatEntry(name) + (link_deactivate && open ? "" : "</a>") + (link_strong && open ? "</strong>" : "")); else line.append((link_strong && open ? "<strong>" : "") + name + (link_strong && open ? "</strong>" : ""));
            }
            return line;
        }

        private String getFirstArgs(boolean open, boolean openParent, boolean openChild, int level) {
            String s = new String();
            s += "level" + level + ' ';
            if (open) s += "open ";
            if (openChild) s += "openchild ";
            if (openParent) s += "openparent ";
            return s.trim();
        }

        /**
		 * @since wiki2xhtml 3.4: notOpen 
		 */
        private String getStyleArgs(String args) {
            String classesList = new String();
            if (isOpen()) classesList += "open ";
            if (openChild()) classesList += "openchild ";
            if (openParent()) classesList += "openparent ";
            if (hasChild()) classesList += "hasChild ";
            if (hasChild() && !isOpen() && !openParent() && !openChild()) classesList += "notOpen ";
            classesList = (classesList + "level" + level).trim();
            Matcher classMatcher = Pattern.compile("(?: |^)class=\"([^\"]*)\"").matcher(args);
            if (classMatcher.find()) {
                classesList += " " + classMatcher.group(1);
                args = args.substring(0, classMatcher.start()) + args.substring(classMatcher.end());
            }
            args += " class=\"" + classesList + "\"";
            return args;
        }

        /**
		 * @return The entry with bold/italic type or a horizontal line
		 */
        private StringBuffer formatEntry(String in) {
            if ("----".equals(in)) return new StringBuffer("<hr />"); else {
                StringBuffer out;
                out = WikiFormattings.makeBoldType(new StringBuffer(in));
                out = WikiFormattings.makeItalicType(out);
                return out;
            }
        }
    }
}
