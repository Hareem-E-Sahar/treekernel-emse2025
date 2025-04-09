import java.io.*;
import java.net.*;
import java.util.*;
import java.text.*;
import org.w3c.dom.*;
import javax.xml.parsers.*;

class KBFileManagerRes extends HTTPResponse {

    public KBFileManagerRes() throws Exception {
        super();
    }

    public void getResponse(HTTPurl urlData, OutputStream outStream, HashMap headers) throws Exception {
        if ("01".equals(urlData.getParameter("action"))) {
            outStream.write(showFileActions(urlData, headers));
            return;
        }
        if ("02".equals(urlData.getParameter("action"))) {
            outStream.write(deleteFile(urlData));
            return;
        }
        if ("03".equals(urlData.getParameter("action"))) {
            outStream.write(runCommand(urlData));
            return;
        } else {
            outStream.write(showFiles(urlData, headers));
            return;
        }
    }

    private byte[] runCommand(HTTPurl urlData) throws Exception {
        String commandID = urlData.getParameter("command");
        File thisFile = new File(urlData.getParameter("file"));
        String requestedFilePath = thisFile.getCanonicalPath();
        boolean inBounds = false;
        String[] paths = store.getCapturePaths();
        for (int x = 0; x < paths.length; x++) {
            String rootFilePath = new File(paths[x]).getCanonicalPath();
            if (requestedFilePath.indexOf(rootFilePath) == 0) {
                inBounds = true;
                break;
            }
        }
        if (inBounds == false) {
            throw new Exception("File out of bounds!");
        }
        HashMap tasks = store.getTaskList();
        TaskCommand taskCommand = (TaskCommand) tasks.get(commandID);
        String command = taskCommand.getCommand();
        StringBuffer buff = new StringBuffer(command);
        int indexOf = buff.indexOf("$filename");
        if (indexOf > -1) buff = buff.replace(indexOf, indexOf + 9, thisFile.getCanonicalPath());
        System.out.println("Running : " + buff.toString());
        TaskItemThread taskItem = new TaskItemThread(taskCommand, new CommandWaitThread(buff.toString()), thisFile);
        Thread taskThread = new Thread(Thread.currentThread().getThreadGroup(), taskItem, taskItem.getClass().getName());
        taskThread.start();
        String start = urlData.getParameter("start");
        if (start == null) start = "0";
        start = start.trim();
        StringBuffer out = new StringBuffer(256);
        out.append("HTTP/1.0 302 Moved Temporarily\n");
        out.append("Location: /servlet/" + urlData.getServletClass() + "?path=" + URLEncoder.encode(thisFile.getParent(), "UTF-8") + "&start=" + start + "\n\n");
        return out.toString().getBytes();
    }

    private byte[] deleteFile(HTTPurl urlData) throws Exception {
        File thisFile = new File(urlData.getParameter("file"));
        String requestedFilePath = thisFile.getCanonicalPath();
        boolean inBounds = false;
        String[] paths = store.getCapturePaths();
        for (int x = 0; x < paths.length; x++) {
            String rootFilePath = new File(paths[x]).getCanonicalPath();
            if (requestedFilePath.indexOf(rootFilePath) == 0) {
                inBounds = true;
                break;
            }
        }
        if (inBounds == false) {
            throw new Exception("File out of bounds!");
        }
        if (thisFile != null && thisFile.exists()) {
            System.out.println("Deleting File : " + thisFile.getName());
            thisFile.delete();
        }
        String start = urlData.getParameter("start");
        if (start == null) start = "0";
        start = start.trim();
        StringBuffer out = new StringBuffer(256);
        out.append("HTTP/1.0 302 Moved Temporarily\n");
        out.append("Location: /servlet/" + urlData.getServletClass() + "?path=" + URLEncoder.encode(thisFile.getParent(), "UTF-8") + "&start=" + start + "\n\n");
        return out.toString().getBytes();
    }

    private byte[] showFileActions(HTTPurl urlData, HashMap headers) throws Exception {
        File file = new File(urlData.getParameter("file"));
        boolean showPlay = store.getProperty("filebrowser.ShowWsPlay").equals("1");
        if (file == null || !file.exists()) {
            StringBuffer out = new StringBuffer();
            out.append("HTTP/1.0 302 Moved Temporarily\n");
            out.append("Location: /servlet/KBFileManagerRes\n\n");
            return out.toString().getBytes();
        }
        String start = urlData.getParameter("start");
        if (start == null) start = "0";
        start = start.trim();
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = dbf.newDocumentBuilder();
        DOMImplementation di = db.getDOMImplementation();
        Document doc = di.createDocument("", "buttons", null);
        Element root = doc.getDocumentElement();
        root.setAttribute("title", file.getName());
        Element button = null;
        Element elm = null;
        Text text = null;
        String action = "";
        button = doc.createElement("button");
        button.setAttribute("name", "Back to Files");
        elm = doc.createElement("url");
        action = "/servlet/" + urlData.getServletClass() + "?path=" + URLEncoder.encode(file.getParent(), "UTF-8") + "&start=" + start;
        text = doc.createTextNode(action);
        elm.appendChild(text);
        button.appendChild(elm);
        root.appendChild(button);
        button = doc.createElement("button");
        button.setAttribute("name", "Delete File");
        elm = doc.createElement("url");
        action = "/servlet/" + urlData.getServletClass() + "?action=02&file=" + URLEncoder.encode(file.getPath(), "UTF-8") + "&start=" + start;
        text = doc.createTextNode(action);
        elm.appendChild(text);
        button.appendChild(elm);
        elm = doc.createElement("confirm");
        text = doc.createTextNode("true");
        elm.appendChild(text);
        button.appendChild(elm);
        root.appendChild(button);
        if (showPlay) {
            button = doc.createElement("button");
            button.setAttribute("name", "Play File");
            elm = doc.createElement("url");
            action = "wsplay://ws/" + URLEncoder.encode(file.getPath(), "UTF-8");
            text = doc.createTextNode(action);
            elm.appendChild(text);
            button.appendChild(elm);
            root.appendChild(button);
        }
        addCommands(doc, file, urlData, start);
        XSL transformer = new XSL(doc, "kb-buttons.xsl", urlData, headers);
        return transformer.doTransform();
    }

    private void addCommands(Document doc, File file, HTTPurl urlData, String start) throws Exception {
        HashMap<String, TaskCommand> tasks = store.getTaskList();
        String[] keys = (String[]) tasks.keySet().toArray(new String[0]);
        Arrays.sort(keys);
        Element button = null;
        Element elm = null;
        Text text = null;
        Element root = doc.getDocumentElement();
        for (int x = 0; x < keys.length; x++) {
            TaskCommand command = (TaskCommand) tasks.get(keys[x]);
            if (command != null && command.getEnabled()) {
                String action = "/servlet/" + urlData.getServletClass() + "?action=03&file=" + URLEncoder.encode(file.getPath(), "UTF-8") + "&command=" + URLEncoder.encode(keys[x], "UTF-8") + "&start=" + start;
                button = doc.createElement("button");
                button.setAttribute("name", keys[x]);
                elm = doc.createElement("url");
                text = doc.createTextNode(action);
                elm.appendChild(text);
                button.appendChild(elm);
                elm = doc.createElement("confirm");
                text = doc.createTextNode("true");
                elm.appendChild(text);
                button.appendChild(elm);
                root.appendChild(button);
            }
        }
    }

    private byte[] showFiles(HTTPurl urlData, HashMap headers) throws Exception {
        String pathString = urlData.getParameter("path");
        File[] files = null;
        File baseDir = null;
        boolean inBounds = false;
        String[] paths = store.getCapturePaths();
        DllWrapper capEng = new DllWrapper();
        if (pathString != null) {
            File thisPath = new File(pathString);
            String requestedFilePath = thisPath.getCanonicalPath();
            for (int x = 0; x < paths.length; x++) {
                String rootFilePath = new File(paths[x]).getCanonicalPath();
                if (requestedFilePath.indexOf(rootFilePath) == 0) {
                    inBounds = true;
                    break;
                }
            }
        }
        if (inBounds == false) {
            files = new File[paths.length];
            for (int x = 0; x < paths.length; x++) {
                files[x] = new File(paths[x]);
            }
        } else {
            baseDir = new File(pathString);
            String fileMasks = DataStore.getInstance().getProperty("filebrowser.masks");
            files = baseDir.listFiles(new FileTypeFilter(fileMasks));
        }
        NumberFormat nf = NumberFormat.getInstance();
        int count = 0;
        int start = 0;
        int show = 10;
        try {
            show = Integer.parseInt(urlData.getParameter("show"));
        } catch (Exception e) {
        }
        try {
            start = Integer.parseInt(urlData.getParameter("start"));
        } catch (Exception e) {
        }
        if (start < 0) start = 0;
        if (files == null) files = new File[0];
        boolean dirsAtTop = "1".equals(store.getProperty("filebrowser.DirsAtTop"));
        Arrays.sort(files, new CompareFiles(dirsAtTop));
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = dbf.newDocumentBuilder();
        DOMImplementation di = db.getDOMImplementation();
        Document doc = di.createDocument("", "files", null);
        Element root = doc.getDocumentElement();
        root.setAttribute("back", "/servlet/ApplyTransformRes?xml=root&xsl=kb-buttons");
        Element fileitem = null;
        Element elm = null;
        Text text = null;
        int fileIndex = start;
        if (inBounds) {
            count++;
            fileitem = doc.createElement("file");
            elm = doc.createElement("name");
            text = doc.createTextNode("<parent>");
            elm.appendChild(text);
            fileitem.appendChild(elm);
            elm = doc.createElement("size");
            elm.setAttribute("units", "");
            text = doc.createTextNode("");
            elm.appendChild(text);
            fileitem.appendChild(elm);
            String action = "/servlet/" + urlData.getServletClass();
            File parent = baseDir.getParentFile();
            if (parent != null) action += "?path=" + URLEncoder.encode(baseDir.getParentFile().getCanonicalPath(), "UTF-8");
            elm = doc.createElement("action");
            text = doc.createTextNode(action);
            elm.appendChild(text);
            fileitem.appendChild(elm);
            root.appendChild(fileitem);
        }
        while (fileIndex < files.length && (count) < show) {
            if (inBounds == false) {
                count++;
                fileitem = doc.createElement("file");
                elm = doc.createElement("name");
                String nameData = files[fileIndex].getCanonicalPath();
                long freeSpace = new File(files[fileIndex].getCanonicalPath()).getFreeSpace();
                nameData += " (" + nf.format((freeSpace / (1024 * 1024))) + " MB Free)";
                text = doc.createTextNode(nameData);
                elm.appendChild(text);
                fileitem.appendChild(elm);
                elm = doc.createElement("size");
                elm.setAttribute("units", "");
                text = doc.createTextNode("");
                elm.appendChild(text);
                fileitem.appendChild(elm);
                String action = "/servlet/" + urlData.getServletClass() + "?path=" + URLEncoder.encode(files[fileIndex].getCanonicalPath(), "UTF-8");
                elm = doc.createElement("action");
                text = doc.createTextNode(action);
                elm.appendChild(text);
                fileitem.appendChild(elm);
                root.appendChild(fileitem);
            } else if (files[fileIndex].isDirectory() && files[fileIndex].isHidden() == false) {
                count++;
                fileitem = doc.createElement("file");
                elm = doc.createElement("name");
                text = doc.createTextNode("<" + files[fileIndex].getName() + ">");
                elm.appendChild(text);
                fileitem.appendChild(elm);
                elm = doc.createElement("size");
                elm.setAttribute("units", "");
                text = doc.createTextNode("");
                elm.appendChild(text);
                fileitem.appendChild(elm);
                String action = "/servlet/" + urlData.getServletClass() + "?path=" + URLEncoder.encode(files[fileIndex].getCanonicalPath(), "UTF-8");
                elm = doc.createElement("action");
                text = doc.createTextNode(action);
                elm.appendChild(text);
                fileitem.appendChild(elm);
                root.appendChild(fileitem);
            } else if (files[fileIndex].isHidden() == false) {
                count++;
                fileitem = doc.createElement("file");
                elm = doc.createElement("name");
                text = doc.createTextNode(files[fileIndex].getName());
                elm.appendChild(text);
                fileitem.appendChild(elm);
                elm = doc.createElement("size");
                elm.setAttribute("units", "KB");
                text = doc.createTextNode(nf.format(files[fileIndex].length() / 1024));
                elm.appendChild(text);
                fileitem.appendChild(elm);
                String action = "/servlet/" + urlData.getServletClass() + "?action=01" + "&file=" + URLEncoder.encode(files[fileIndex].getPath(), "UTF-8") + "&start=" + start;
                elm = doc.createElement("action");
                text = doc.createTextNode(action);
                elm.appendChild(text);
                fileitem.appendChild(elm);
                root.appendChild(fileitem);
            }
            fileIndex++;
        }
        root.setAttribute("start", new Integer(start).toString());
        root.setAttribute("end", new Integer(fileIndex).toString());
        root.setAttribute("show", new Integer(show).toString());
        root.setAttribute("total", new Integer(files.length).toString());
        if (pathString != null) root.setAttribute("path", URLEncoder.encode(new File(pathString).getCanonicalPath(), "UTF-8")); else root.setAttribute("path", "Root");
        XSL transformer = new XSL(doc, "kb-showfiles.xsl", urlData, headers);
        return transformer.doTransform();
    }
}
