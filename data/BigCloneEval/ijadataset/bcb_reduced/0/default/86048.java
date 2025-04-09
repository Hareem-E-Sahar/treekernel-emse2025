import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.net.*;

class TaskManagementDataRes extends HTTPResponse {

    public TaskManagementDataRes() throws Exception {
        super();
    }

    public void getResponse(HTTPurl urlData, OutputStream outStream, HashMap<String, String> headers) throws Exception {
        if ("01".equals(urlData.getParameter("action"))) {
            outStream.write(showTaskOutput(urlData));
            return;
        } else if ("02".equals(urlData.getParameter("action"))) {
            outStream.write(killTask(urlData));
            return;
        } else if ("03".equals(urlData.getParameter("action"))) {
            outStream.write(removeTask(urlData));
            return;
        } else if ("04".equals(urlData.getParameter("action"))) {
            outStream.write(removeFinished(urlData));
            return;
        } else if ("05".equals(urlData.getParameter("action"))) {
            outStream.write(showArchiveList(urlData));
            return;
        } else if ("06".equals(urlData.getParameter("action"))) {
            outStream.write(showArchivedTaskOutput(urlData));
            return;
        } else if ("07".equals(urlData.getParameter("action"))) {
            outStream.write(deleteArchiveFile(urlData));
            return;
        } else if ("08".equals(urlData.getParameter("action"))) {
            outStream.write(deleteAllArchives(urlData));
            return;
        } else {
            outStream.write(showTaskList(urlData));
        }
    }

    private byte[] showArchiveList(HTTPurl urlData) throws Exception {
        PageTemplate template = new PageTemplate(store.getProperty("path.template") + File.separator + "ArchiveTaskList.html");
        StringBuffer buff = new StringBuffer();
        File outFile = new File(new DllWrapper().getAllUserPath() + "archive");
        if (outFile.exists() == false) outFile.mkdirs();
        File[] files = outFile.listFiles();
        for (int x = 0; files != null && x < files.length; x++) {
            File archiveFile = files[x];
            if (archiveFile.isDirectory() == false && archiveFile.getName().startsWith("Task-")) {
                buff.append("<tr>\n");
                buff.append("<td>");
                buff.append("<a href='/servlet/TaskManagementDataRes?action=06&file=" + URLEncoder.encode(archiveFile.getName(), "UTF-8") + "'>");
                buff.append("<img src='/images/log.png' border='0' alt='Schedule Log' width='24' height='24'></a> ");
                buff.append("<a href='/servlet/TaskManagementDataRes?action=07&file=" + URLEncoder.encode(archiveFile.getName(), "UTF-8") + "'>");
                buff.append("<img src='/images/delete.png' border='0' alt='Schedule Log' width='24' height='24'></a> ");
                buff.append("</td>");
                buff.append("<td style='padding-left:20px;'>" + archiveFile.getName() + "</td>\n");
                buff.append("</tr>\n");
            }
        }
        template.replaceAll("$ArchiveList", buff.toString());
        return template.getPageBytes();
    }

    @SuppressWarnings("unchecked")
    private byte[] showArchivedTaskOutput(HTTPurl urlData) throws Exception {
        PageTemplate template = new PageTemplate(store.getProperty("path.template") + File.separator + "ArchivedTaskOutPut.html");
        File archivePath = new File(new DllWrapper().getAllUserPath() + "archive" + File.separator + urlData.getParameter("file"));
        FileInputStream fis = new FileInputStream(archivePath);
        ObjectInputStream ois = new ObjectInputStream(fis);
        HashMap item = (HashMap) ois.readObject();
        ois.close();
        if (item != null) {
            template.replaceAll("$taskControl", (String) item.get("control"));
            template.replaceAll("$taskOutput", (String) item.get("stdout"));
            template.replaceAll("$taskError", (String) item.get("stderr"));
        } else {
            StringBuffer out = new StringBuffer(256);
            out.append("HTTP/1.0 302 Moved Temporarily\n");
            out.append("Location: /servlet/" + urlData.getServletClass() + "\n\n");
            return out.toString().getBytes();
        }
        return template.getPageBytes();
    }

    private byte[] removeFinished(HTTPurl urlData) throws Exception {
        String[] keys = (String[]) store.runningTaskList.keySet().toArray(new String[0]);
        for (int x = 0; x < keys.length; x++) {
            TaskItemThread task = (TaskItemThread) store.runningTaskList.get(keys[x]);
            if (task.isFinished()) {
                store.runningTaskList.remove(keys[x]);
            }
        }
        StringBuffer out = new StringBuffer(256);
        out.append("HTTP/1.0 302 Moved Temporarily\n");
        out.append("Location: /servlet/" + urlData.getServletClass() + "\n\n");
        return out.toString().getBytes();
    }

    private byte[] killTask(HTTPurl urlData) throws Exception {
        String dateID = urlData.getParameter("id");
        TaskItemThread item = (TaskItemThread) store.runningTaskList.get(dateID);
        if (item != null) {
            item.stop();
        }
        StringBuffer out = new StringBuffer(256);
        out.append("HTTP/1.0 302 Moved Temporarily\n");
        out.append("Location: /servlet/" + urlData.getServletClass() + "\n\n");
        return out.toString().getBytes();
    }

    private byte[] removeTask(HTTPurl urlData) throws Exception {
        String id = urlData.getParameter("id");
        store.runningTaskList.remove(id);
        StringBuffer out = new StringBuffer(256);
        out.append("HTTP/1.0 302 Moved Temporarily\n");
        out.append("Location: /servlet/" + urlData.getServletClass() + "\n\n");
        return out.toString().getBytes();
    }

    private byte[] showTaskOutput(HTTPurl urlData) throws Exception {
        PageTemplate template = new PageTemplate(store.getProperty("path.template") + File.separator + "TaskOutPut.html");
        String id = urlData.getParameter("id");
        TaskItemThread item = (TaskItemThread) store.runningTaskList.get(id);
        if (item != null) {
            template.replaceAll("$taskControl", item.getControl());
            template.replaceAll("$taskOutput", item.getOutput());
            template.replaceAll("$taskError", item.getError());
        } else {
            StringBuffer out = new StringBuffer(256);
            out.append("HTTP/1.0 302 Moved Temporarily\n");
            out.append("Location: /servlet/" + urlData.getServletClass() + "\n\n");
            return out.toString().getBytes();
        }
        return template.getPageBytes();
    }

    private byte[] showTaskList(HTTPurl urlData) throws Exception {
        StringBuffer buff = new StringBuffer(2048);
        PageTemplate template = new PageTemplate(store.getProperty("path.template") + File.separator + "RunningTaskList.html");
        String[] keys = (String[]) store.runningTaskList.keySet().toArray(new String[0]);
        Arrays.sort(keys);
        SimpleDateFormat df = new SimpleDateFormat("yyyy/MM/dd @ HH:mm:ss");
        for (int x = 0; x < keys.length; x++) {
            TaskItemThread item = (TaskItemThread) store.runningTaskList.get(keys[x]);
            buff.append("<tr>");
            buff.append("<td nowrap>" + df.format(item.getCreationDate()) + "</td>");
            buff.append("<td nowrap>" + item.getTaskName() + "</td>");
            buff.append("<td nowrap>" + item.getTargetFile() + "</td>");
            buff.append("<td align='center' nowrap>" + item.getDelayLeft() + "</td>");
            buff.append("<td align='center' nowrap>" + item.getStatus() + "</td>");
            buff.append("<td>");
            buff.append("<a class='noUnder' href='/servlet/" + urlData.getServletClass() + "?action=01&id=" + URLEncoder.encode(keys[x], "UTF-8") + "'><img align='absmiddle' src='/images/log.png' border='0' alt='Show Output' width='24' height='24'></a> ");
            if (item.isFinished() == false) {
                buff.append(" <a class='noUnder' onClick='return confirmAction(\"Kill\");' href='/servlet/" + urlData.getServletClass() + "?action=02&id=" + URLEncoder.encode(keys[x], "UTF-8") + "'><img align='absmiddle' src='/images/stop.png' border='0' alt='Kill' width='24' height='24'></a> ");
            } else {
                buff.append(" <a class='noUnder' onClick='return confirmAction(\"Delete\");' href='/servlet/" + urlData.getServletClass() + "?action=03&id=" + URLEncoder.encode(keys[x], "UTF-8") + "'><img align='absmiddle' src='/images/delete.png' border='0' alt='Delete' width='24' height='24'></a>");
            }
            buff.append("</td></tr>\n");
        }
        template.replaceAll("$taskList", buff.toString());
        return template.getPageBytes();
    }

    public byte[] deleteArchiveFile(HTTPurl urlData) throws Exception {
        File basePath = new File(new DllWrapper().getAllUserPath() + "archive");
        File archivePath = new File(new DllWrapper().getAllUserPath() + "archive" + File.separator + urlData.getParameter("file"));
        if (archivePath.getCanonicalPath().indexOf(basePath.getCanonicalPath()) == -1) {
            throw new Exception("Archive file to delete is not in the archive path!");
        }
        if (archivePath.exists()) {
            archivePath.delete();
        }
        StringBuffer buff = new StringBuffer(256);
        buff.append("HTTP/1.0 302 Moved Temporarily\n");
        buff.append("Location: /servlet/TaskManagementDataRes?action=05\n\n");
        return buff.toString().getBytes();
    }

    public byte[] deleteAllArchives(HTTPurl urlData) throws Exception {
        File outFile = new File(new DllWrapper().getAllUserPath() + "archive");
        if (outFile.exists() == false) outFile.mkdirs();
        File[] files = outFile.listFiles();
        Arrays.sort(files);
        for (int x = files.length - 1; files != null && x >= 0; x--) {
            File archiveFile = files[x];
            if (archiveFile.isDirectory() == false && archiveFile.getName().startsWith("Task-")) {
                archiveFile.delete();
            }
        }
        StringBuffer buff = new StringBuffer(256);
        buff.append("HTTP/1.0 302 Moved Temporarily\n");
        buff.append("Location: /servlet/TaskManagementDataRes?action=05\n\n");
        return buff.toString().getBytes();
    }
}
