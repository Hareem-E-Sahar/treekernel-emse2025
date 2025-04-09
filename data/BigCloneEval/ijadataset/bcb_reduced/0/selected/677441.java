package org.damour.base.server;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.MemoryCacheImageOutputStream;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.ProgressListener;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.io.IOUtils;
import org.damour.base.client.objects.File;
import org.damour.base.client.objects.FileData;
import org.damour.base.client.objects.FileUploadStatus;
import org.damour.base.client.objects.Folder;
import org.damour.base.client.objects.Photo;
import org.damour.base.client.objects.PhotoThumbnail;
import org.damour.base.client.objects.User;
import org.damour.base.client.objects.Permission.PERM;
import org.damour.base.server.hibernate.HibernateUtil;
import org.damour.base.server.hibernate.helpers.SecurityHelper;
import org.hibernate.Session;
import org.hibernate.Transaction;
import com.mysql.jdbc.ServerPreparedStatement;

public class FileUploadService extends HttpServlet {

    private static BaseService baseService = new BaseService();

    public FileUploadService() {
        super();
    }

    protected void doPost(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException {
        Session session = HibernateUtil.getInstance().getSession();
        User owner = baseService.getAuthenticatedUser(session, request, response);
        if (owner == null) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.flushBuffer();
            Logger.log("Unauthorized upload requested: " + request.getRemoteAddr());
            return;
        }
        List<FileItem> fileItems = Collections.emptyList();
        try {
            fileItems = getFileItems(request, owner);
        } catch (Throwable t) {
            Logger.log(t);
            return;
        }
        final FileUploadStatus status = BaseService.fileUploadStatusMap.get(owner);
        status.setStatus(FileUploadStatus.CREATING_FILE);
        Transaction tx = session.beginTransaction();
        try {
            for (final FileItem item : fileItems) {
                File fileObject = null;
                if (item.getContentType().contains("image")) {
                    fileObject = new Photo();
                } else {
                    fileObject = new File();
                }
                fileObject.setOwner(owner);
                String parentFolderId = request.getParameter("parentFolder");
                Folder parentFolder = null;
                if (parentFolderId != null && !"".equals(parentFolderId)) {
                    parentFolder = (Folder) session.load(Folder.class, new Long(parentFolderId));
                    if (!SecurityHelper.doesUserHavePermission(session, owner, parentFolder, PERM.WRITE)) {
                        Cookie cookie = new Cookie(item.getFieldName(), "");
                        cookie.setMaxAge(0);
                        cookie.setPath("/");
                        response.addCookie(cookie);
                        response.sendError(HttpServletResponse.SC_FORBIDDEN);
                        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                        response.flushBuffer();
                        Logger.log("Unauthorized upload requested: " + request.getRemoteAddr());
                        return;
                    }
                }
                fileObject.setParent(parentFolder);
                fileObject.setContentType(item.getContentType());
                String name = item.getName();
                if (name.lastIndexOf("/") >= 0) {
                    name = name.substring(name.lastIndexOf("/") + 1);
                }
                if (name.lastIndexOf("\\") >= 0) {
                    name = name.substring(name.lastIndexOf("\\") + 1);
                }
                fileObject.setName(name);
                fileObject.setDescription(name);
                session.save(fileObject);
                status.setStatus(FileUploadStatus.WRITING_FILE);
                java.io.File outputPath = new java.io.File(java.io.File.separatorChar + "tmp" + java.io.File.separatorChar + BaseSystem.getDomainName(request));
                java.io.File outputFile = new java.io.File(outputPath, fileObject.getId() + "_" + fileObject.getName());
                outputPath.mkdirs();
                item.write(outputFile);
                tx = session.beginTransaction();
                fileObject.setNameOnDisk(fileObject.getId() + "_" + fileObject.getName());
                fileObject.setSize(outputFile.length());
                session.save(fileObject);
                tx.commit();
                Logger.log("Wrote to file: " + outputFile.getCanonicalPath());
                status.setStatus(FileUploadStatus.WRITING_DATABASE);
                saveFileFromStream(fileObject, new FileInputStream(outputFile));
                try {
                    if (fileObject instanceof Photo) {
                        BufferedImage bi = javax.imageio.ImageIO.read(outputFile);
                        ByteArrayOutputStream pngOut = new ByteArrayOutputStream();
                        ImageIO.write(bi, "png", pngOut);
                        tx = session.beginTransaction();
                        Photo photo = (Photo) fileObject;
                        photo.setWidth(bi.getWidth());
                        photo.setHeight(bi.getHeight());
                        PhotoThumbnail thumbFile = new PhotoThumbnail();
                        thumbFile.setHidden(true);
                        thumbFile.setOwner(owner);
                        thumbFile.setParent(parentFolder);
                        thumbFile.setName(createFileName("", name, "_thumb"));
                        thumbFile.setDescription("Thumbnail for " + name);
                        session.save(thumbFile);
                        PhotoThumbnail previewFile = new PhotoThumbnail();
                        previewFile.setHidden(true);
                        previewFile.setOwner(owner);
                        previewFile.setParent(parentFolder);
                        previewFile.setName(createFileName("", name, "_preview"));
                        previewFile.setDescription("Preview image for " + name);
                        session.save(previewFile);
                        PhotoThumbnail slideFile = new PhotoThumbnail();
                        slideFile.setHidden(true);
                        slideFile.setOwner(owner);
                        slideFile.setParent(parentFolder);
                        slideFile.setName(createFileName("", name, "_slideshow"));
                        slideFile.setDescription("Slideshow image for " + name);
                        session.save(slideFile);
                        photo.setThumbnailImage(thumbFile);
                        photo.setSlideshowImage(slideFile);
                        photo.setPreviewImage(previewFile);
                        ByteArrayInputStream pngIn = new ByteArrayInputStream(pngOut.toByteArray());
                        ByteArrayOutputStream thumbOutStream = new ByteArrayOutputStream();
                        BufferedImage thumbbi = ImageIO.read(pngIn);
                        BufferedImage convertedThumbImage = createThumbnail(thumbbi, 128, 128, true);
                        thumbFile.setWidth(convertedThumbImage.getWidth());
                        thumbFile.setHeight(convertedThumbImage.getHeight());
                        ImageWriter writer = ImageIO.getImageWritersByFormatName("jpg").next();
                        ImageWriteParam iwp = writer.getDefaultWriteParam();
                        iwp.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
                        iwp.setCompressionQuality(0.75f);
                        writer.setOutput(new MemoryCacheImageOutputStream(thumbOutStream));
                        IIOImage thumbimage = new IIOImage(convertedThumbImage, null, null);
                        writer.write(null, thumbimage, iwp);
                        pngIn.reset();
                        ByteArrayOutputStream slideOutStream = new ByteArrayOutputStream();
                        BufferedImage slidebi = ImageIO.read(pngIn);
                        BufferedImage convertedSlideImage = createThumbnail(slidebi, 640, 400, true);
                        slideFile.setWidth(convertedSlideImage.getWidth());
                        slideFile.setHeight(convertedSlideImage.getHeight());
                        writer.setOutput(new MemoryCacheImageOutputStream(slideOutStream));
                        IIOImage slideimage = new IIOImage(convertedSlideImage, null, null);
                        writer.write(null, slideimage, iwp);
                        pngIn.reset();
                        ByteArrayOutputStream previewOutStream = new ByteArrayOutputStream();
                        BufferedImage previewbi = ImageIO.read(pngIn);
                        BufferedImage convertedPreviewImage = createThumbnail(previewbi, 320, 200, true);
                        previewFile.setWidth(convertedPreviewImage.getWidth());
                        previewFile.setHeight(convertedPreviewImage.getHeight());
                        writer.setOutput(new MemoryCacheImageOutputStream(previewOutStream));
                        IIOImage previewimage = new IIOImage(convertedPreviewImage, null, null);
                        writer.write(null, previewimage, iwp);
                        thumbFile.setContentType("image/jpeg");
                        slideFile.setContentType("image/jpeg");
                        previewFile.setContentType("image/jpeg");
                        thumbFile.setSize(thumbOutStream.size());
                        slideFile.setSize(slideOutStream.size());
                        previewFile.setSize(previewOutStream.size());
                        session.save(thumbFile);
                        session.save(slideFile);
                        session.save(previewFile);
                        tx.commit();
                        tx = session.beginTransaction();
                        thumbFile.setNameOnDisk(createFileName(thumbFile.getId().toString() + "_", name, "_thumb"));
                        slideFile.setNameOnDisk(createFileName(slideFile.getId().toString() + "_", name, "_slide"));
                        previewFile.setNameOnDisk(createFileName(previewFile.getId().toString() + "_", name, "_preview"));
                        tx.commit();
                        saveFileFromStream(thumbFile, new ByteArrayInputStream(thumbOutStream.toByteArray()));
                        saveFileFromStream(slideFile, new ByteArrayInputStream(slideOutStream.toByteArray()));
                        saveFileFromStream(previewFile, new ByteArrayInputStream(previewOutStream.toByteArray()));
                    }
                    status.setStatus(FileUploadStatus.FINISHED);
                    Logger.log("Wrote to database: " + fileObject.getName());
                } catch (Throwable t) {
                    Logger.log(t);
                }
                Cookie cookie = new Cookie(item.getFieldName(), fileObject.getId().toString());
                cookie.setPath("/");
                cookie.setMaxAge(BaseService.COOKIE_TIMEOUT);
                response.addCookie(cookie);
                response.setStatus(HttpServletResponse.SC_OK);
                response.getOutputStream().close();
                response.flushBuffer();
            }
        } catch (Throwable t) {
            Logger.log(t);
            try {
                tx.rollback();
            } catch (Exception e2) {
            }
        } finally {
            try {
                session.close();
            } catch (Exception e) {
            }
        }
    }

    protected void doGet(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException {
        response.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
        response.setStatus(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
        response.flushBuffer();
    }

    protected void saveFileFromStream(File fileObj, InputStream inputStream) {
        PreparedStatement ps = null;
        Connection conn = null;
        try {
            Properties dbprops = new Properties();
            dbprops.setProperty("user", HibernateUtil.getInstance().getUsername());
            dbprops.setProperty("password", HibernateUtil.getInstance().getPassword());
            conn = (Connection) DriverManager.getConnection(HibernateUtil.getInstance().getConnectString(), dbprops);
            String insert_data = "insert into " + HibernateUtil.getInstance().getTablePrefix() + FileData.class.getSimpleName().toLowerCase() + " (permissibleObject, data) values (?, ?)";
            conn.setAutoCommit(false);
            if (conn instanceof com.mysql.jdbc.Connection) {
                com.mysql.jdbc.Connection mysqlConn = (com.mysql.jdbc.Connection) conn;
                ps = (ServerPreparedStatement) mysqlConn.serverPrepareStatement(insert_data);
                ((ServerPreparedStatement) ps).setBinaryStream(2, inputStream);
            } else {
                ps = conn.prepareStatement(insert_data);
                try {
                    ps.setBinaryStream(2, inputStream, (int) fileObj.getSize());
                } catch (Throwable t) {
                    ps.setBytes(2, IOUtils.toByteArray(inputStream));
                }
            }
            ps.setLong(1, fileObj.getId());
            ps.executeUpdate();
            conn.commit();
        } catch (Throwable t) {
            Logger.log(t);
        } finally {
            try {
                ps.close();
            } catch (Throwable t) {
            }
            try {
                conn.close();
            } catch (Throwable t) {
            }
            try {
                inputStream.close();
            } catch (Throwable t) {
            }
        }
    }

    protected String createFileName(String prefix, String name, String postfix) {
        int lastDot = name.lastIndexOf(".");
        if (lastDot != -1) {
            return prefix + (name.substring(0, lastDot)) + postfix + name.substring(lastDot);
        }
        return prefix + name + postfix;
    }

    public BufferedImage convert(Image im) {
        BufferedImage bi = new BufferedImage(im.getWidth(null), im.getHeight(null), BufferedImage.TYPE_INT_RGB);
        Graphics bg = bi.getGraphics();
        bg.drawImage(im, 0, 0, null);
        bg.dispose();
        return bi;
    }

    protected BufferedImage createThumbnail(BufferedImage inImage, int nwidth, int nheight, boolean exact) {
        try {
            int imgWidth = inImage.getWidth(null);
            int imgHeight = inImage.getHeight(null);
            int width = nwidth;
            int height = nheight;
            nheight = (int) (nwidth * ((double) imgHeight / (double) imgWidth));
            if (nheight > height) {
                nheight = height;
                nwidth = (int) (nheight * ((double) imgWidth / (double) imgHeight));
            }
            if (nwidth < 1 || nheight < 1) return null;
            BufferedImage outImage = null;
            if (exact) {
                outImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
            } else {
                outImage = new BufferedImage(nwidth, nheight, BufferedImage.TYPE_INT_RGB);
            }
            Graphics2D g = (Graphics2D) outImage.getGraphics();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
            g.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_SPEED);
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
            g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_SPEED);
            if (exact) {
                int y = (height - nheight) / 2;
                int x = (width - nwidth) / 2;
                g.setColor(Color.white);
                g.fillRect(0, 0, width, height);
                Image img = inImage.getScaledInstance(nwidth, nheight, Image.SCALE_SMOOTH);
                g.drawImage(img, x, y, null);
            } else {
                g.drawImage(inImage.getScaledInstance(nwidth, nheight, Image.SCALE_SMOOTH), 0, 0, null);
            }
            g.dispose();
            return outImage;
        } catch (Exception e) {
        }
        return null;
    }

    protected List<FileItem> getFileItems(final HttpServletRequest request, final User user) throws FileUploadException {
        int sizeThreshold = 524288;
        java.io.File repository = new java.io.File(BaseSystem.getTempDir() + java.io.File.separatorChar + "uploads");
        repository.mkdirs();
        DiskFileItemFactory fileFactory = new DiskFileItemFactory(sizeThreshold, repository);
        ServletFileUpload fu = new ServletFileUpload(fileFactory);
        final FileUploadStatus status = new FileUploadStatus();
        status.setStatus(FileUploadStatus.UPLOADING);
        BaseService.fileUploadStatusMap.put(user, status);
        fu.setSizeMax(268435456);
        fu.setProgressListener(new ProgressListener() {

            public void update(long bytesRead, long contentLength, int item) {
                status.setItem(item);
                status.setBytesRead(bytesRead);
                status.setContentLength(contentLength);
                status.setStatus(FileUploadStatus.UPLOADING);
                try {
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        return fu.parseRequest(request);
    }
}
