package org.hld.avg;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Krkr2NScripter {

    private static final File targetDir = new File("E:\\avg\\data");

    private Map<String, File> files = new LinkedHashMap<String, File>();

    private Map<String, String> resource = new HashMap<String, String>();

    private NScripterMap nscripterMap = new NScripterMap();

    private int isIf = -1;

    private boolean iscript = false;

    private String currentFilename = null;

    private int num = 0;

    private Map<String, Integer> imageMap = new HashMap<String, Integer>();

    private String[] locate = new String[2];

    private int btn = 1;

    private Map<Integer, String> btnMap = new HashMap<Integer, String>();

    private Map<Integer, String> exbtnMap = new HashMap<Integer, String>();

    private StringBuilder exbtn_d = new StringBuilder();

    private String macroName = null;

    private StringBuilder macro = null;

    private String ifName = null;

    private StringBuilder ifValue = null;

    private Map<String, String> macroMap = new HashMap<String, String>();

    private Map<String, Integer> fileMap = new HashMap<String, Integer>();

    private Map<String, String> ifMap = new HashMap<String, String>();

    private StringBuilder log = new StringBuilder();

    public static void main(String[] args) {
        Krkr2NScripter k2n = new Krkr2NScripter();
        File dir = new File("E:\\avg\\data");
        k2n.putResource(dir, "bgimage");
        k2n.putResource(dir, "bgm");
        k2n.putResource(dir, "fgimage");
        k2n.putResource(dir, "image");
        k2n.putResource(dir, "rule");
        k2n.putResource(dir, "sound");
        dir = new File("E:\\avg\\data\\scenario");
        k2n.put(dir, "first.ks");
        k2n.put(dir, "macro.ks");
        k2n.put(dir, "blmacro.ks");
        k2n.parseMacro();
        k2n.put(dir, "bolognie.ks");
        k2n.put(dir, "chapter01.ks");
        k2n.put(dir, "chapter02.ks");
        k2n.put(dir, "chapter03.ks");
        k2n.put(dir, "chapter04.ks");
        k2n.put(dir, "chapter05.ks");
        k2n.put(dir, "epilog.ks");
        k2n.put(dir, "extra.ks");
        k2n.put(dir, "first.ks");
        k2n.put(dir, "history.ks");
        k2n.put(dir, "rmenu.ks");
        k2n.put(dir, "save.ks");
        k2n.btnMap.clear();
        k2n.exbtnMap.clear();
        k2n.exbtn_d = new StringBuilder();
        k2n.btn = 1;
        k2n.execute();
        k2n.log();
    }

    private void parseMacro() {
        Iterator<Entry<String, File>> iterator = files.entrySet().iterator();
        Pattern pattern = Pattern.compile("\\[(.+?)\\]");
        BufferedWriter out = new BufferedWriter(new OutputStreamWriter(System.out));
        while (iterator.hasNext()) {
            Entry<String, File> entry = iterator.next();
            currentFilename = entry.getValue().getName();
            nscripterMap.addLabel(currentFilename, null);
            BufferedReader in = null;
            try {
                in = new BufferedReader(new InputStreamReader(new FileInputStream(entry.getValue()), "UTF-16"));
                String line;
                num = 0;
                for (int i = 1; (line = in.readLine()) != null; i++) {
                    num++;
                    line = line.trim();
                    if (line.length() == 0) continue;
                    if (line.startsWith(";")) continue;
                    if (line.startsWith("*")) continue;
                    if (line.startsWith("@")) {
                        String command = transform(line.substring(1));
                        if (command != null) {
                            writer(out, command);
                        }
                        continue;
                    }
                    Matcher matcher = pattern.matcher(line);
                    StringBuffer sb = new StringBuffer();
                    while (matcher.find()) {
                        String command = transform(matcher.group(1));
                        if (command == null) command = "";
                        matcher.appendReplacement(sb, Matcher.quoteReplacement(command));
                    }
                    matcher.appendTail(sb);
                    if (sb.length() > 0) {
                        writer(out, sb.toString());
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (in != null) try {
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void put(File dir, String filename) {
        files.put(filename, new File(dir, filename));
    }

    private void putResource(File dir, String dirname) {
        File r = new File(dir, dirname);
        for (String f : r.list()) {
            resource.put(f, dirname + "\\" + f);
        }
    }

    private String getFilePath(String filename) {
        String path = resource.get(filename);
        if (path == null) path = resource.get(filename + ".png");
        if (path == null) path = resource.get(filename + ".jpg");
        if (path == null) path = resource.get(filename + ".ogg");
        if (path == null) {
            log(filename + "无法获取路径");
            return filename;
        }
        return path;
    }

    private void log(String message) {
        log.append(currentFilename);
        log.append(":");
        log.append(String.valueOf(num));
        log.append(":");
        log.append(message);
        log.append("\r\n");
        System.out.println(currentFilename + ":" + num + ":" + message);
    }

    private void log() {
        BufferedWriter out = null;
        try {
            out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(new File(targetDir, "log.txt")), "GBK"));
            Map<String, Map> map = new LinkedHashMap<String, Map>();
            map.put("resource", resource);
            map.put("file", fileMap);
            map.put("label", nscripterMap.labelMap);
            map.put("numberVariable", nscripterMap.numberVariableMap);
            map.put("textVariable", nscripterMap.textVariableMap);
            map.put("musicVariable", nscripterMap.musicVariableMap);
            map.put("imageVariable", nscripterMap.imageVariableMap);
            Iterator<Entry<String, Map>> iterator = map.entrySet().iterator();
            while (iterator.hasNext()) {
                Entry<String, Map> entry = iterator.next();
                out.write(entry.getKey());
                out.write(':');
                out.newLine();
                Iterator i = entry.getValue().entrySet().iterator();
                while (i.hasNext()) {
                    Entry e = (Entry) i.next();
                    out.write(e.getKey().toString());
                    out.write('=');
                    out.write(e.getValue().toString());
                    out.newLine();
                }
                out.write("==================================");
                out.newLine();
            }
            out.write("log:");
            out.newLine();
            out.write(log.toString());
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (out != null) try {
                out.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void execute() {
        Iterator<Entry<String, File>> iterator = files.entrySet().iterator();
        int t = 0;
        Pattern pattern = Pattern.compile("\\[(.+?)\\]");
        Pattern replacePattern = Pattern.compile(".*[/\\\\_@].*");
        while (iterator.hasNext()) {
            Entry<String, File> entry = iterator.next();
            currentFilename = entry.getValue().getName();
            fileMap.put(currentFilename, t);
            String label = nscripterMap.addLabel(currentFilename, null);
            BufferedReader in = null;
            BufferedWriter out = null;
            try {
                in = new BufferedReader(new InputStreamReader(new FileInputStream(entry.getValue()), "UTF-16"));
                out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(new File(targetDir, t + ".txt")), "GBK"));
                if (t == 0) {
                    writer(out, ";mode800");
                    writer(out, "*define");
                    writer(out, "caption \"甘井子传说\"");
                    writer(out, "roff");
                    writer(out, "savenumber 10");
                    writer(out, "kidokuskip");
                    writer(out, "windowback");
                    writer(out, "numalias tempnum," + nscripterMap.getNumberVariable("temp_num").substring(1));
                    writer(out, "game");
                    writer(out, "*start");
                }
                writer(out, label);
                String line;
                num = 0;
                for (int i = 1; (line = in.readLine()) != null; i++) {
                    num++;
                    line = line.trim();
                    if (line.length() == 0) continue;
                    if (line.startsWith(";")) continue;
                    if (line.startsWith("*")) {
                        int index = line.indexOf('|');
                        if (index >= 0) line = line.substring(0, index);
                        writer(out, nscripterMap.addLabel(currentFilename, line));
                        continue;
                    }
                    if (line.startsWith("@")) {
                        String command = transform(line.substring(1));
                        if (command != null) {
                            writer(out, command);
                        }
                        continue;
                    }
                    Matcher matcher = pattern.matcher(line);
                    StringBuilder sb = new StringBuilder();
                    int index = 0;
                    while (matcher.find()) {
                        String temp = line.substring(index, matcher.start());
                        if (replacePattern.matcher(temp).matches()) {
                            for (int j = 0; j < temp.length(); j++) {
                                char c = temp.charAt(j);
                                switch(c) {
                                    case '/':
                                        sb.append('／');
                                        break;
                                    case '\\':
                                        sb.append('＼');
                                        break;
                                    case '_':
                                        sb.append('＿');
                                        break;
                                    case '@':
                                        sb.append('＠');
                                        break;
                                    default:
                                        sb.append(c);
                                        break;
                                }
                            }
                        } else {
                            sb.append(temp);
                        }
                        index = matcher.end();
                        String command = transform(matcher.group(1));
                        if (command == null) command = "";
                        sb.append(command);
                    }
                    sb.append(line.substring(index, line.length()));
                    if (sb.length() > 0) {
                        writer(out, sb.toString());
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (in != null) try {
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                if (out != null) try {
                    out.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            t++;
        }
        if (!ifMap.isEmpty()) {
            BufferedWriter out = null;
            try {
                out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(new File(targetDir, t++ + ".txt")), "GBK"));
                Iterator<Entry<String, String>> iter = ifMap.entrySet().iterator();
                while (iter.hasNext()) {
                    Entry<String, String> entry = iter.next();
                    out.write(entry.getKey());
                    out.newLine();
                    out.write(entry.getValue());
                    out.newLine();
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (out != null) try {
                    out.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void writer(BufferedWriter out, String line) throws IOException {
        if (iscript) {
            log("忽略tjs脚本:" + line);
            return;
        }
        if (isIf > 0) {
            ifValue.append(line);
            ifValue.append("\r\n");
        } else if (macroName == null) {
            out.write(line);
            out.newLine();
        } else {
            macro.append(line);
            macro.append("\r\n");
        }
        if (isIf == 0) isIf = 1;
    }

    private static enum Command {

        IF, EVAL, ENDIF, CALL, STARTANCHOR, RCLICK, CLICKSKIP, HISTORY, FADEINSE, BACKLAY, IMAGE, TRANS, CURRENT, MAPPFONT, DEFFONT, P, FREEIMAGE, WT, FADEOUTSE, WAIT, ER, PLAYBGM, LOCATE, BUTTON, JUMP, CLOSE, STOPBGM, PLAYSE, L, R, S, RETURN, ISCRIPT, ENDSCRIPT, MACRO, ENDMACRO, FONT
    }

    private static HashMap<String, Command> commandMap = new HashMap<String, Command>();

    static {
        for (Command c : Command.values()) {
            commandMap.put(c.name().toLowerCase(), c);
        }
    }

    private Map<String, String> getValue(String command) {
        Map<String, String> map = new HashMap<String, String>();
        String[] commands = command.split("\\s+");
        boolean flag = false;
        String name = null;
        StringBuilder sb = null;
        for (int i = 1; i < commands.length; i++) {
            String c = commands[i];
            if (flag) {
                sb.append(" ");
                if (c.endsWith("\"")) {
                    sb.append(c.substring(0, c.length() - 1));
                    map.put(name, sb.toString());
                    flag = false;
                } else {
                    sb.append(c);
                }
            } else {
                int index = c.indexOf('=');
                if (index >= 0) {
                    String value = c.substring(index + 1);
                    if (value.startsWith("\"")) {
                        if (value.endsWith("\"")) {
                            map.put(c.substring(0, index), value.substring(1, value.length() - 1));
                        } else {
                            sb = new StringBuilder(value.substring(1));
                            flag = true;
                            name = c.substring(0, index);
                        }
                    } else {
                        map.put(c.substring(0, index), value);
                    }
                } else {
                    map.put(c, "true");
                }
            }
        }
        return map;
    }

    private void ignoreValue(Map<String, String> values, String... value) {
        for (String v : value) {
            if (values.containsKey(v)) log("被忽略的属性：" + v + "=" + values.get(v));
        }
    }

    private static final Pattern NUMBER_PATTERN = Pattern.compile("(true|false|[\\d\\.-]+)");

    private void exp(StringBuilder sb, String tjs) {
        String[] ss = tjs.split("(==|!=|>=|<=|>|<|<>)", 2);
        if (ss.length == 2) {
            Matcher matcher = Pattern.compile("(==|!=|>=|<=|<>|>|<)").matcher(tjs);
            matcher.find();
            String p = matcher.group(1);
            String value = ss[1];
            if (NUMBER_PATTERN.matcher(value).matches()) {
                sb.append(nscripterMap.getNumberVariable(ss[0]));
                sb.append(p);
                sb.append(NScripterMap.getNumber(value));
            } else {
                sb.append(nscripterMap.getTextVariable(ss[0]));
                sb.append(p);
                sb.append("\"");
                sb.append(value.replace("'", ""));
                sb.append("\"");
            }
            return;
        }
        ss = tjs.split("=", 2);
        if (ss.length == 2) {
            sb.append("mov ");
            String value = ss[1];
            if (NUMBER_PATTERN.matcher(value).matches()) {
                sb.append(nscripterMap.getNumberVariable(ss[0]));
                sb.append(",");
                sb.append(NScripterMap.getNumber(value));
            } else {
                sb.append(nscripterMap.getTextVariable(ss[0]));
                sb.append(",");
                sb.append("\"");
                sb.append(value.replace("'", ""));
                sb.append("\"");
            }
            return;
        }
        log("无法处理的tjs命令:" + tjs);
    }

    private static final Pattern MACRO_VARIABLE_PATTERN = Pattern.compile("\\&\\((.*?)mp\\.(.+?)\\)");

    private void macro(String command, Map<String, String> value, StringBuilder sb) {
        String m = macroMap.get(command);
        if (m != null) {
            StringBuffer s = new StringBuffer();
            Matcher matcher = MACRO_VARIABLE_PATTERN.matcher(m);
            while (matcher.find()) {
                String v1 = matcher.group(1);
                String v2 = matcher.group(2);
                String v = value.get(v2);
                if (v == null) {
                    log("缺少宏参数:" + v2);
                    v = "0";
                }
                if (v1.length() > 3) v = v1.substring(1, v1.length() - 2) + v;
                matcher.appendReplacement(s, Matcher.quoteReplacement(getFilePath(v)));
            }
            matcher.appendTail(s);
            sb.append(s);
        }
    }

    private String transform(String command) {
        if (command == null || command.length() == 0) return null;
        int index = command.indexOf(' ');
        String commandName = index >= 0 ? command.substring(0, index) : command;
        Command c = commandMap.get(commandName);
        StringBuilder sb = new StringBuilder();
        if (c == null) {
            index = command.lastIndexOf('[');
            if (index >= 0) {
                sb.append(command.substring(0, index));
                command = command.substring(index + 1);
                index = command.indexOf(' ');
                commandName = index >= 0 ? command.substring(0, index) : command;
                c = commandMap.get(commandName);
            }
        }
        if (c != null) {
            switch(c) {
                case IF:
                    isIf = 0;
                    ifName = "*" + NScripterMap.md5("if_" + System.currentTimeMillis() + System.nanoTime());
                    ifValue = new StringBuilder();
                    sb.append("if ");
                    Map<String, String> values = getValue(command);
                    String exp = values.get("exp");
                    exp(sb, exp);
                    sb.append(" gosub ");
                    sb.append(ifName);
                    break;
                case EVAL:
                    exp = getValue(command).get("exp");
                    exp(sb, exp);
                    break;
                case ENDIF:
                    isIf = -1;
                    ifValue.append("return");
                    ifMap.put(ifName, ifValue.toString());
                    break;
                case CALL:
                    values = getValue(command);
                    String storage = values.get("storage");
                    if (storage == null) storage = currentFilename;
                    sb.append("gosub ");
                    sb.append(nscripterMap.getLabel(storage, values.get("target")));
                    ignoreValue(values, "countpage");
                    break;
                case FADEINSE:
                    values = getValue(command);
                    storage = getFilePath(values.get("storage"));
                    sb.append("dwave ");
                    sb.append(nscripterMap.getMusicVariable(storage));
                    sb.append(",\"");
                    sb.append(storage);
                    sb.append("\"");
                    break;
                case IMAGE:
                    values = getValue(command);
                    storage = getFilePath(values.get("storage"));
                    Integer id = nscripterMap.getImageVariable(storage);
                    String layer = values.get("layer");
                    if (layer == null) layer = "base";
                    String page = values.get("page");
                    if (page == null) page = "fore";
                    if ("base".equals(layer) && "back".equals(page)) {
                        sb.append("bg \"");
                        sb.append(storage);
                        sb.append("\",1");
                    } else {
                        imageMap.put(page + "-" + layer, id);
                        String left = values.get("left");
                        if (left == null) left = "0";
                        String top = values.get("top");
                        if (top == null) top = "0";
                        String opacity = values.get("opacity");
                        if (opacity == null) opacity = "255";
                        sb.append("lsph ");
                        sb.append(id);
                        sb.append(",\":a;");
                        sb.append(storage);
                        sb.append("\",");
                        sb.append(left);
                        sb.append(",");
                        sb.append(top);
                        sb.append(",");
                        sb.append(opacity);
                        sb.append(":vsp ");
                        sb.append(id);
                        sb.append(",1");
                    }
                    break;
                case WT:
                    sb.append("print 1");
                    break;
                case P:
                    sb.append("\\");
                    break;
                case FREEIMAGE:
                    values = getValue(command);
                    layer = values.get("layer");
                    if (layer == null) layer = "base";
                    page = values.get("page");
                    if (page == null) page = "fore";
                    if ("base".equals(layer) && "back".equals(page)) {
                        sb.append("bg black,1");
                    } else {
                        id = imageMap.get(page + "-" + layer);
                        if (id != null) {
                            sb.append("csp ");
                            sb.append(id);
                        }
                    }
                    break;
                case WAIT:
                    values = getValue(command);
                    String time = values.get("time");
                    String canskip = values.get("canskip");
                    if ("false".equals(canskip)) sb.append("wait "); else sb.append("delay ");
                    sb.append(time);
                    ignoreValue(values, "mode");
                    break;
                case PLAYBGM:
                    values = getValue(command);
                    storage = getFilePath(values.get("storage"));
                    String loop = values.get("loop");
                    if ("false".equals(loop)) sb.append("bgm "); else sb.append("mp3 ");
                    sb.append("\"");
                    sb.append(storage);
                    sb.append("\"");
                    break;
                case LOCATE:
                    values = getValue(command);
                    String x = values.get("x");
                    if (x == null) x = "0";
                    locate[0] = x;
                    String y = values.get("y");
                    if (y == null) y = "0";
                    locate[1] = y;
                    break;
                case BUTTON:
                    values = getValue(command);
                    String normal = getFilePath(values.get("normal"));
                    String over = values.get("over");
                    String enterse = values.get("enterse");
                    String clickse = values.get("clickse");
                    id = nscripterMap.getImageVariable(normal);
                    Integer oid = null;
                    sb.append("lsp ");
                    sb.append(id);
                    sb.append(",\":a/1,0,3;");
                    sb.append(normal);
                    sb.append("\",");
                    sb.append(locate[0]);
                    sb.append(",");
                    sb.append(locate[1]);
                    if (over != null) {
                        over = getFilePath(over);
                        oid = nscripterMap.getImageVariable(over);
                        sb.append(":");
                        sb.append("lsph ");
                        sb.append(oid);
                        sb.append(",\":a/1,0,3;");
                        sb.append(over);
                        sb.append("\",");
                        sb.append(locate[0]);
                        sb.append(",");
                        sb.append(locate[1]);
                        exbtn_d.append("P");
                        exbtn_d.append(id);
                        exbtn_d.append("C");
                        exbtn_d.append(oid);
                    }
                    Integer bid = btn++;
                    StringBuilder s = new StringBuilder();
                    s.append(nscripterMap.getImageVariable(normal));
                    s.append(",");
                    s.append(bid);
                    if (over != null || clickse != null) {
                        s.append(",");
                        s.append("\"");
                        if (over != null) {
                            s.append("P");
                            s.append(oid);
                            s.append("C");
                            s.append(id);
                        }
                        if (clickse != null) {
                            s.append("S1,(");
                            s.append(getFilePath(clickse));
                            s.append(")");
                        }
                        s.append("\"");
                    }
                    exbtnMap.put(bid, s.toString());
                    s = new StringBuilder();
                    if (enterse != null) {
                        enterse = getFilePath(enterse);
                        s.append("dwave ");
                        s.append(nscripterMap.getMusicVariable(enterse));
                        s.append(",\"");
                        s.append(enterse);
                        s.append("\":");
                    }
                    storage = values.get("storage");
                    if (storage == null) storage = currentFilename;
                    s.append("goto ");
                    s.append(nscripterMap.getLabel(storage, values.get("target")));
                    btnMap.put(bid, s.toString());
                    break;
                case S:
                    if (!btnMap.isEmpty()) {
                        String variable = "btnwait_" + System.currentTimeMillis() + System.nanoTime();
                        String label = nscripterMap.getLabel(currentFilename, variable);
                        sb.append(label);
                        sb.append("\r\nbtndef clear\r\n");
                        Iterator<Entry<Integer, String>> iterator = exbtnMap.entrySet().iterator();
                        while (iterator.hasNext()) {
                            sb.append("exbtn ");
                            sb.append(iterator.next().getValue());
                            sb.append("\r\n");
                        }
                        if (exbtn_d.length() > 0) {
                            sb.append("exbtn_d \"");
                            sb.append(exbtn_d);
                            sb.append("\"");
                        }
                        sb.append("\r\n");
                        variable = nscripterMap.getNumberVariable(variable);
                        sb.append("btnwait ");
                        sb.append(variable);
                        iterator = btnMap.entrySet().iterator();
                        while (iterator.hasNext()) {
                            Entry<Integer, String> entry = iterator.next();
                            sb.append("\r\n");
                            sb.append("if ");
                            sb.append(variable);
                            sb.append("==");
                            sb.append(entry.getKey());
                            sb.append(" ");
                            sb.append(entry.getValue());
                        }
                        sb.append("\r\n");
                        sb.append("goto ");
                        sb.append(label);
                        exbtnMap.clear();
                        btnMap.clear();
                        btn = 1;
                        exbtn_d = new StringBuilder();
                    }
                    break;
                case JUMP:
                    values = getValue(command);
                    storage = values.get("storage");
                    if (storage == null) storage = currentFilename;
                    sb.append("goto ");
                    sb.append(nscripterMap.getLabel(storage, values.get("target")));
                    ignoreValue(values, "countpage");
                    break;
                case CLOSE:
                    values = getValue(command);
                    sb.append("end");
                    ignoreValue(values, "ask");
                    break;
                case STOPBGM:
                    sb.append("stop");
                    break;
                case L:
                    sb.append("@");
                    break;
                case RETURN:
                    sb.append("return");
                    break;
                case R:
                    sb.append("\r\n");
                    break;
                case ISCRIPT:
                    iscript = true;
                    break;
                case ENDSCRIPT:
                    iscript = false;
                    break;
                case MACRO:
                    values = getValue(command);
                    macroName = values.get("name");
                    macro = new StringBuilder();
                    break;
                case ENDMACRO:
                    macroMap.put(macroName, macro.toString());
                    macroName = null;
                    break;
                case FONT:
                    values = getValue(command);
                    String color = values.get("color");
                    if (color != null && color.startsWith("0x")) {
                        sb.append("#");
                        sb.append(color.substring(2));
                    }
                    break;
                default:
                    break;
            }
        } else {
            macro(commandName, getValue(command), sb);
        }
        return sb.length() > 0 ? sb.toString() : null;
    }
}
