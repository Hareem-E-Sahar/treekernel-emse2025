package com.zhumulangma.cloudstorage.server;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.zip.CRC32;
import java.util.zip.CheckedOutputStream;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.compress.archivers.ArchiveInputStream;
import org.apache.commons.compress.archivers.ArchiveOutputStream;
import org.apache.commons.compress.archivers.ArchiveStreamFactory;
import org.apache.commons.compress.archivers.jar.JarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.compressors.CompressorOutputStream;
import org.apache.commons.compress.compressors.CompressorStreamFactory;
import org.apache.commons.fileupload.DiskFileUpload;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.FileUploadBase.InvalidContentTypeException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.io.Util;
import org.apache.tools.zip.ZipEntry;
import org.apache.tools.zip.ZipOutputStream;
import com.zhumulangma.cloudstorage.server.adapter.FSAdapter;
import com.zhumulangma.cloudstorage.server.adapter.FSFTPAdapter;
import com.zhumulangma.cloudstorage.server.entity.CloudFile;
import com.zhumulangma.cloudstorage.server.exception.ErrorCodeException;
import com.zhumulangma.cloudstorage.shared.Constants;
import com.zhumulangma.cloudstorage.shared.Utils;
import com.zhumulangma.cloudstorage.support.Support;

public class FSServer extends HttpServlet {

    private static final long serialVersionUID = -6591383074807985812L;

    private FSAdapter fs = FSAdapter.getFtpInstance();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String action = null == req.getParameter(Constants.PARAM_ACTION) ? "" : req.getParameter(Constants.PARAM_ACTION);
        dispatch(action, req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        doGet(req, resp);
    }

    /**
	 * Action dispatcher.
	 * @param action
	 * @param req
	 * @param resp
	 * @return true-handled, false-not handled
	 */
    private boolean dispatch(String action, HttpServletRequest req, HttpServletResponse resp) {
        Utils.log(this.getClass(), "Session: " + req.getSession(true).getId() + " request action=" + action);
        headerInfo(req, resp);
        resp.setContentType("text/plain");
        resp.setCharacterEncoding(Constants.ENCODING.WEB);
        if (action.equals(Constants.ACTION_USER_LOGIN)) {
            userLogin(req, resp);
            return true;
        } else if (action.equals(Constants.ACTION_DEBUG_EXEC)) {
            exec(req, resp);
            return true;
        } else {
            String username = $username(req);
            String password = $password(req);
            String tokenId = $tokenid(req);
            Utils.log(this.getClass(), "Current Info: username=" + username + ", tokenId=" + tokenId);
            if ("".equals(username) || "".equals(tokenId) || !req.getSession().getId().equals(tokenId)) {
                error(req, resp, Constants.ERROR.BADSESSION);
                return true;
            }
            if (action.equals(Constants.ACTION_FILE_LIST)) {
                getFileList(req, resp, username, password);
                return true;
            } else if (action.equals(Constants.ACTION_FILE_DOWNLOAD)) {
                downloadFile(req, resp, username, password);
                return true;
            } else if (action.equals(Constants.ACTION_FILE_UPLOAD)) {
                uploadFile(req, resp, username, password);
                return true;
            } else if (action.equals(Constants.ACTION_FILE_MAKE_DIR)) {
                makeCategory(req, resp, username, password);
                return true;
            } else if (action.equals(Constants.ACTION_FILE_REMOVE_DIR)) {
                removeCategory(req, resp, username, password);
                return true;
            } else if (action.equals(Constants.ACTION_FILE_DELETE)) {
                deleteFile(req, resp, username, password);
                return true;
            } else if (action.equals(Constants.ACTION_FILE_RENAME)) {
                renameFile(req, resp, username, password);
                return true;
            }
        }
        error(req, resp, Constants.ERROR.UNSUPPORT);
        return false;
    }

    private String $username(HttpServletRequest req) {
        Object attr = req.getSession().getAttribute(Constants.ATTR_USERNAME);
        return null == attr ? "" : (String) attr;
    }

    private String $password(HttpServletRequest req) {
        Object attr = req.getSession().getAttribute(Constants.ATTR_PASSWORD);
        return null == attr ? "" : (String) attr;
    }

    private String $tokenid(HttpServletRequest req) {
        Object attr = req.getSession().getAttribute(Constants.ATTR_TOKENID);
        return null == attr ? "" : (String) attr;
    }

    /**
	 * Upload one file. Notice: one and only one!!!
	 * @param req <p>path-where the file puts (necessary)</p>
	 * @param resp
	 * @param username
	 * @param password
	 */
    private void uploadFile(HttpServletRequest req, HttpServletResponse resp, String username, String password) {
        try {
            Utils.log("Upload: Preparing UPLOAD_TEMP_PATH ...");
            File tmpPath = new File(Constants.UPLOAD.TEMP_PATH);
            if (!tmpPath.exists() && !tmpPath.mkdir()) {
                throw new ErrorCodeException(Constants.ERROR.MKDIRFAIL);
            }
            String path = req.getParameter(Constants.PARAM_PATH);
            if (null == path) {
                throw new ErrorCodeException(Constants.ERROR.BADPARAMS);
            }
            path = Utils.normalPath(path);
            DiskFileItemFactory factory = new DiskFileItemFactory(Constants.UPLOAD.CACHE_SIZE, tmpPath);
            ServletFileUpload upload = new ServletFileUpload(factory);
            upload.setFileSizeMax(Constants.UPLOAD.MAX_SIZE);
            List<FileItem> items = upload.parseRequest(req);
            Iterator<FileItem> it = items.iterator();
            Utils.log(this.getClass(), "Upload: items size=" + items.size());
            if (it.hasNext()) {
                Utils.log(this.getClass(), "Upload: Enter iterator.");
                FileItem item = it.next();
                if (!item.isFormField()) {
                    Utils.log(this.getClass(), "Upload: Not a form field and begin to upload.");
                    String fieldName = item.getFieldName();
                    String name = new File(item.getName().trim()).getName();
                    String contentType = item.getContentType();
                    boolean isInMemory = item.isInMemory();
                    long sizeInBytes = item.getSize();
                    Utils.log(this.getClass(), "Upload: Detail - \nField Name=" + fieldName + "\nName=" + name + "\nContent Type=" + contentType + "\nisInMemory=" + isInMemory + "\nsizeInBytes=" + sizeInBytes);
                    fs.storeFile(username, password, name, path, item.getInputStream());
                    PrintWriter out;
                    out = resp.getWriter();
                    String outstr = Utils.makeJsonObjectString(new Utils.PairValue("name", name), new Utils.PairValue("sizeInBytes", sizeInBytes));
                    out.println('[' + outstr + ']');
                    out.flush();
                    return;
                }
            }
            error(req, resp, Constants.ERROR.BADPARAMS);
        } catch (SecurityException e) {
            e.printStackTrace();
            error(req, resp, Constants.ERROR.ACCESSDENIED);
        } catch (InvalidContentTypeException e) {
            e.printStackTrace();
            error(req, resp, Constants.ERROR.BADCONTENT);
        } catch (ErrorCodeException e) {
            e.printStackTrace();
            error(req, resp, e.getErrorCode());
        } catch (Exception e) {
            e.printStackTrace();
            error(req, resp, Constants.ERROR.IOERROR);
        }
    }

    private void downloadFile(HttpServletRequest req, HttpServletResponse resp, String username, String password) {
        try {
            Utils.log(this.getClass(), "Download: Preparing download...");
            File cachePath = new File(Constants.CACHE.PATH);
            if (!cachePath.exists()) {
                if (!cachePath.mkdir()) {
                    throw new ErrorCodeException(Constants.ERROR.MKDIRFAIL);
                }
            }
            String files = req.getParameter(Constants.PARAM_FILES);
            if (null == files) {
                throw new ErrorCodeException(Constants.ERROR.BADPARAMS);
            }
            Utils.log(this.getClass(), "Calling files: " + files);
            String cacheFileName = Utils.makeUniqueRandomName();
            OutputStream out = Support.getFileOutputStream(cachePath, cacheFileName);
            ZipOutputStream zos = new ZipOutputStream(new BufferedOutputStream(new CheckedOutputStream(out, new CRC32())));
            zos.setEncoding(Constants.ENCODING.LOCAL_FS);
            String[] filenames = files.split(Constants.SPLITTER.FILE);
            for (String filename : filenames) {
                try {
                    filename = Utils.makeArchiveAbsName(Utils.normalFileName(filename));
                    Utils.log(this.getClass(), "Add a file to archive file - filename=" + filename);
                    File file = new File(filename);
                    String archiveName = new String(file.getName().getBytes("iso-8859-1"), "utf-8");
                    zos.putNextEntry(new ZipEntry(archiveName));
                    filename = new String((file.getParent() == null ? "" : file.getParent()).getBytes("iso-8859-1"), "utf-8") + "/" + file.getName();
                    fs.retrieveFile(username, password, filename, zos);
                } catch (Exception e) {
                    Utils.log(this.getClass(), "A file compress exception found - filename=" + filename);
                    e.printStackTrace();
                } finally {
                    zos.closeEntry();
                }
            }
            zos.close();
            String packName = "package_" + System.currentTimeMillis() + ".zip";
            resp.addHeader("Content-Disposition", "attachment;filename=" + packName);
            resp.setContentType("application/octet-stream;name=" + packName);
            resp.setCharacterEncoding("utf-8");
            InputStream in = Support.getFileInputStream(cachePath, cacheFileName);
            OutputStream os = resp.getOutputStream();
            try {
                Util.copyStream(in, os);
                os.flush();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                in.close();
                os.close();
            }
            File cf = new File(cachePath, cacheFileName);
            if (cf.exists() && !cf.delete()) {
                cf.deleteOnExit();
            }
        } catch (SecurityException e) {
            e.printStackTrace();
            error(req, resp, Constants.ERROR.ACCESSDENIED);
        } catch (ErrorCodeException e) {
            e.printStackTrace();
            error(req, resp, e.getErrorCode());
        } catch (Exception e) {
            e.printStackTrace();
            error(req, resp, Constants.ERROR.IOERROR);
        }
    }

    /**
	 * User login.
	 * @param req
	 * @param resp
	 */
    private void userLogin(HttpServletRequest req, HttpServletResponse resp) {
        try {
            String username = req.getParameter(Constants.PARAM_USERNAME);
            String password = req.getParameter(Constants.PARAM_PASSWORD);
            if (null == username) {
                error(req, resp, Constants.ERROR.BADLOGIN);
                return;
            } else if (null == password) {
                error(req, resp, Constants.ERROR.BADLOGIN);
                return;
            } else if (!fs.checkLogin(username, password)) {
                error(req, resp, Constants.ERROR.BADLOGIN);
                return;
            }
            String tokenId = "" + req.getSession().getId();
            req.getSession().setAttribute(Constants.ATTR_USERNAME, username);
            req.getSession().setAttribute(Constants.ATTR_PASSWORD, password);
            req.getSession().setAttribute(Constants.ATTR_TOKENID, tokenId);
            PrintWriter out;
            out = resp.getWriter();
            String outstr = Utils.makeJsonObjectString(new Utils.PairValue("username", username), new Utils.PairValue("tokenid", tokenId));
            out.println('[' + outstr + ']');
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
            error(req, resp, Constants.ERROR.CONNERROR);
        } catch (ErrorCodeException e) {
            e.printStackTrace();
            error(req, resp, e.getErrorCode());
        }
    }

    /**
	 * Remove category.
	 * @param req
	 * @param resp
	 * @param username
	 * @param password
	 */
    private void makeCategory(HttpServletRequest req, HttpServletResponse resp, String username, String password) {
        try {
            String path = req.getParameter(Constants.PARAM_PATH);
            String pathname = req.getParameter(Constants.PARAM_PATHNAME);
            if (null == path || "".equals(path)) {
                throw new ErrorCodeException(Constants.ERROR.BADPARAMS);
            }
            if (null == pathname || "".equals(pathname)) {
                throw new ErrorCodeException(Constants.ERROR.BADPARAMS);
            }
            PrintWriter out;
            out = resp.getWriter();
            String outstr = "";
            fs.makeCategory(username, password, path, pathname);
            outstr = Utils.makeJsonObjectString(new Utils.PairValue("path", path), new Utils.PairValue("pathname", pathname));
            out.println("[" + outstr + "]");
            out.flush();
        } catch (ErrorCodeException e) {
            e.printStackTrace();
            error(req, resp, e.getErrorCode());
        } catch (IOException e) {
            e.printStackTrace();
            error(req, resp, Constants.ERROR.IOERROR);
        }
    }

    /**
	 * Remove category.
	 * @param req
	 * @param resp
	 * @param username
	 * @param password
	 */
    private void removeCategory(HttpServletRequest req, HttpServletResponse resp, String username, String password) {
        try {
            String path = req.getParameter(Constants.PARAM_PATH);
            if (null == path || "".equals(path)) {
                throw new ErrorCodeException(Constants.ERROR.BADPARAMS);
            }
            PrintWriter out;
            out = resp.getWriter();
            String outstr = "";
            fs.removeCategory(username, password, path);
            outstr = Utils.makeJsonObjectString(new Utils.PairValue("path", path));
            out.println("[" + outstr + "]");
            out.flush();
        } catch (ErrorCodeException e) {
            e.printStackTrace();
            error(req, resp, e.getErrorCode());
        } catch (IOException e) {
            e.printStackTrace();
            error(req, resp, Constants.ERROR.IOERROR);
        }
    }

    /**
	 * Get file list.
	 * @param req
	 * @param resp
	 * @param username
	 * @param password
	 */
    private void getFileList(HttpServletRequest req, HttpServletResponse resp, String username, String password) {
        try {
            String path = req.getParameter(Constants.PARAM_PATH);
            if (null == path || "".equals(path)) {
                path = ".";
            }
            PrintWriter out;
            out = resp.getWriter();
            String outstr = "";
            List<CloudFile> list = fs.getFileList(username, password, path);
            for (int i = 0; i < list.size(); i++) {
                outstr += list.get(i).$json() + ",";
            }
            if (outstr.length() > 1) {
                outstr = outstr.substring(0, outstr.length() - 1);
            }
            out.println("[" + outstr + "]");
            out.flush();
        } catch (ErrorCodeException e) {
            e.printStackTrace();
            error(req, resp, e.getErrorCode());
        } catch (IOException e) {
            e.printStackTrace();
            error(req, resp, Constants.ERROR.IOERROR);
        }
    }

    private void renameFile(HttpServletRequest req, HttpServletResponse resp, String username, String password) {
        try {
            String src = req.getParameter(Constants.PARAM_SRC);
            String dst = req.getParameter(Constants.PARAM_DST);
            if (null == src || null == dst || "".equals(src) || "".equals(dst)) {
                throw new ErrorCodeException(Constants.ERROR.BADPARAMS);
            }
            PrintWriter out;
            out = resp.getWriter();
            String outstr = "";
            fs.rename(username, password, src, dst);
            outstr = Utils.makeJsonObjectString(new Utils.PairValue("src", src), new Utils.PairValue("dst", dst));
            out.println("[" + outstr + "]");
            out.flush();
        } catch (ErrorCodeException e) {
            e.printStackTrace();
            error(req, resp, e.getErrorCode());
        } catch (IOException e) {
            e.printStackTrace();
            error(req, resp, Constants.ERROR.IOERROR);
        } catch (Exception e) {
            e.printStackTrace();
            error(req, resp, Constants.ERROR.UNKNOWN);
        }
    }

    private void deleteFile(HttpServletRequest req, HttpServletResponse resp, String username, String password) {
        try {
            String files = req.getParameter(Constants.PARAM_FILES);
            if (null == files || "".equals(files)) {
                throw new ErrorCodeException(Constants.ERROR.BADPARAMS);
            }
            String[] filenames = files.split(Constants.SPLITTER.FILE);
            PrintWriter out;
            out = resp.getWriter();
            String outstr = "";
            ArrayList<Utils.PairValue> resultList = new ArrayList<Utils.PairValue>();
            for (String filename : filenames) {
                try {
                    fs.deleteFile(username, password, filename);
                    resultList.add(new Utils.PairValue(filename, Constants.STATUS_OK));
                } catch (Exception e) {
                    e.printStackTrace();
                    Utils.log(this.getClass(), "Delete [" + filename + "] maybe failed.");
                    resultList.add(new Utils.PairValue(filename, Constants.STATUS_ERROR));
                }
            }
            outstr = Utils.makeJsonArrayString(resultList);
            out.println(outstr);
            out.flush();
        } catch (ErrorCodeException e) {
            e.printStackTrace();
            error(req, resp, e.getErrorCode());
        } catch (IOException e) {
            e.printStackTrace();
            error(req, resp, Constants.ERROR.IOERROR);
        } catch (Exception e) {
            e.printStackTrace();
            error(req, resp, Constants.ERROR.UNKNOWN);
        }
    }

    /**
	 * Output error code to client. 
	 * @param req
	 * @param resp
	 * @param errCode
	 */
    private void error(HttpServletRequest req, HttpServletResponse resp, long errCode) {
        try {
            PrintWriter out;
            out = resp.getWriter();
            out.println(Utils.encodeError(errCode));
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void exec(HttpServletRequest req, HttpServletResponse resp) {
        try {
            PrintWriter out;
            out = resp.getWriter();
            out.println(fs.debugExec(req.getParameter("cmd")));
            out.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void headerInfo(final HttpServletRequest req, final HttpServletResponse resp) {
        Utils.log("************************************");
        Utils.log("[" + System.currentTimeMillis() + "] Request in...");
        Enumeration<String> headers = req.getHeaderNames();
        while (headers.hasMoreElements()) {
            String head = headers.nextElement();
            Utils.log("\t" + head + ": " + req.getHeader(head));
        }
        Utils.log("\tQueryString: " + req.getQueryString());
        Utils.log("************************************");
    }
}
