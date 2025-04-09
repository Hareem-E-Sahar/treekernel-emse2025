import java.io.*;
import java.util.*;
import java.text.*;
import java.net.*;

class FileManagementDataRes extends HTTPResponse {

    public FileManagementDataRes() throws Exception {
        super();
    }

    public void getResponse(HTTPurl urlData, OutputStream outStream, HashMap<String, String> headers) throws Exception {
        if ("01".equals(urlData.getParameter("action"))) {
            outStream.write(deleteFile(urlData));
            return;
        } else if ("02".equals(urlData.getParameter("action"))) {
            outStream.write(showCommandList(urlData));
            return;
        } else if ("03".equals(urlData.getParameter("action"))) {
            outStream.write(runCommand(urlData));
            return;
        }
        outStream.write(showFileList(urlData));
    }

    private byte[] showFileList(HTTPurl urlData) throws Exception {
        String pathString = urlData.getParameter("path");
        File[] files = null;
        File baseDir = null;
        boolean inBounds = false;
        String[] paths = store.getCapturePaths();
        DllWrapper capEng = new DllWrapper();
        boolean showPlay = store.getProperty("filebrowser.showwsPlay").equals("1");
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
        String tableHeader = "";
        if (inBounds == false) {
            tableHeader = "<tr><td class='itemheading' nowrap><Strong>Capture Paths</Strong></td></tr>\n";
            files = new File[paths.length];
            for (int x = 0; x < paths.length; x++) {
                files[x] = new File(paths[x]);
            }
        } else {
            baseDir = new File(pathString);
            String fileMasks = DataStore.getInstance().getProperty("filebrowser.masks");
            files = baseDir.listFiles(new FileTypeFilter(fileMasks));
        }
        StringBuffer buff = new StringBuffer(2048);
        NumberFormat nf = NumberFormat.getInstance();
        PageTemplate template = new PageTemplate(store.getProperty("path.template") + File.separator + "FileList.html");
        boolean dirsAtTop = "1".equals(store.getProperty("filebrowser.dirsattop"));
        Arrays.sort(files, new CompareFiles(dirsAtTop));
        if (baseDir != null) {
            File parent = baseDir.getParentFile();
            if (parent == null) buff.append("<tr><td colspan='3'><strong><a style='text-decoration: none; color: #FFFFFF;' href='/servlet/" + urlData.getServletClass() + "'>"); else buff.append("<tr><td colspan='3'><strong><a style='text-decoration: none; color: #FFFFFF;' href='/servlet/" + urlData.getServletClass() + "?path=" + URLEncoder.encode(baseDir.getParentFile().getCanonicalPath(), "UTF-8") + "'>");
            buff.append("<img border=0 src='/images/back.png' align='absmiddle' width='24' height='24'> Back</a></strong></td></tr>\n");
        }
        for (int x = 0; x < files.length; x++) {
            if (inBounds == false) {
                buff.append("<tr><td nowrap colspan='3' class='seperator'><strong><a style='text-decoration: none; color: #FFFFFF;' href='/servlet/" + urlData.getServletClass() + "?path=" + URLEncoder.encode(files[x].getCanonicalPath(), "UTF-8") + "'>");
                buff.append(files[x].getCanonicalPath() + "</a> ");
                long freeSpace = capEng.getFreeSpace(files[x].getCanonicalPath());
                buff.append("(" + nf.format((freeSpace / (1024 * 1024))) + " MB Free)");
                buff.append("</strong></td></tr>\n");
            } else if (files[x].isDirectory() && files[x].isHidden() == false) {
                buff.append("<tr><td colspan='2' nowrap width='95%' class='seperator'><strong><a style='text-decoration: none; color: #FFFFFF;' href='/servlet/" + urlData.getServletClass() + "?path=" + URLEncoder.encode(files[x].getCanonicalPath(), "UTF-8") + "'>");
                buff.append("&lt;" + files[x].getName() + "&gt;</a></strong></td>");
                buff.append("<td nowrap class='seperator'><a class='noUnder' onClick='return confirmAction(\"" + URLEncoder.encode(files[x].getPath(), "UTF-8") + "\");' href='#'><img align='absmiddle' src='/images/delete.png' border='0' alt='Delete' width='24' height='24'></a></td></tr>\n");
            } else if (files[x].isHidden() == false) {
                buff.append("<tr><td nowrap width='95%' class='seperator'>" + files[x].getName() + "</td><td nowrap class='seperator'>" + nf.format(files[x].length() / 1024) + " KB</td>");
                buff.append("<td nowrap class='seperator'><a class='noUnder' onClick='return confirmAction(\"" + URLEncoder.encode(files[x].getPath(), "UTF-8") + "\");' href='#'><img align='absmiddle' src='/images/delete.png' border='0' alt='Delete' width='24' height='24'></a> ");
                buff.append("<a class='noUnder' href='/servlet/" + urlData.getServletClass() + "?action=02&file=" + URLEncoder.encode(files[x].getPath(), "UTF-8") + "'><img align='absmiddle' src='/images/RunTaskSmall.png' border='0' alt='Run Task' width='24' height='24'></a> ");
                if (showPlay) buff.append("<a class='noUnder' href='wsplay://ws/" + URLEncoder.encode(files[x].getPath(), "UTF-8") + "'><img align='absmiddle' src='/images/play.png' border='0' alt='Play file using wsplay protocol' width='24' height='24'></a> ");
                buff.append("</td></tr>\n");
            }
        }
        template.replaceAll("$tableHeader$", tableHeader);
        template.replaceAll("$fileList$", buff.toString());
        return template.getPageBytes();
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
        StringBuffer out = new StringBuffer(256);
        out.append("HTTP/1.0 302 Moved Temporarily\n");
        out.append("Location: /servlet/FileManagementDataRes?path=" + URLEncoder.encode(thisFile.getParentFile().getAbsolutePath(), "UTF-8") + "\n\n");
        return out.toString().getBytes();
    }

    private byte[] showCommandList(HTTPurl urlData) throws Exception {
        StringBuffer out = new StringBuffer(256);
        File file = new File(urlData.getParameter("file"));
        HashMap<String, TaskCommand> tasks = store.getTaskList();
        String[] keys = (String[]) tasks.keySet().toArray(new String[0]);
        Arrays.sort(keys);
        for (int x = 0; x < keys.length; x++) {
            TaskCommand command = (TaskCommand) tasks.get(keys[x]);
            if (command != null && command.getEnabled()) {
                out.append("<tr><td class='itemheading'><a class='noUnder' href='/servlet/" + urlData.getServletClass());
                out.append("?action=03&file=" + URLEncoder.encode(file.getAbsolutePath(), "UTF-8") + "&command=" + URLEncoder.encode(keys[x], "UTF-8"));
                out.append("'><strong><center>" + keys[x] + "</center></strong></a></td><tr>\n");
            }
        }
        PageTemplate template = new PageTemplate(store.getProperty("path.template") + File.separator + "CommandList.html");
        template.replaceAll("$commandList", out.toString());
        template.replaceAll("$filename", file.getName());
        template.replaceAll("$backURL$", "/servlet/FileManagementDataRes?path=" + URLEncoder.encode(file.getParentFile().getAbsolutePath(), "UTF-8"));
        return template.getPageBytes();
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
        HashMap<String, TaskCommand> tasks = store.getTaskList();
        TaskCommand taskCommand = (TaskCommand) tasks.get(commandID);
        if (taskCommand != null) {
            String command = taskCommand.getCommand();
            StringBuffer buff = new StringBuffer(command);
            int indexOf = buff.indexOf("$filename");
            if (indexOf > -1) buff = buff.replace(indexOf, indexOf + 9, thisFile.getCanonicalPath());
            System.out.println("Running : " + buff.toString());
            TaskItemThread taskItem = new TaskItemThread(taskCommand, new CommandWaitThread(buff.toString()), thisFile);
            Thread taskThread = new Thread(Thread.currentThread().getThreadGroup(), taskItem, taskItem.getClass().getName());
            taskThread.start();
        }
        StringBuffer out = new StringBuffer(256);
        out.append("HTTP/1.0 302 Moved Temporarily\n");
        out.append("Location: /servlet/TaskManagementDataRes\n\n");
        return out.toString().getBytes();
    }
}
