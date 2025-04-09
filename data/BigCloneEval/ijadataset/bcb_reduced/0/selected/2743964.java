package adv.web;

import adv.db.Db;
import adv.db.bean.*;
import adv.language.*;
import adv.language.beans.Modulo;
import adv.live.*;
import adv.tools.*;
import ognl.OgnlException;
import ognlscript.FileParser;
import ognlscript.TransactionBufferStack;
import ognlscript.block.OgnlscriptTraceableException;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.fileupload.servlet.ServletRequestContext;
import org.apache.commons.io.FilenameUtils;
import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.net.URLEncoder;
import com.sun.org.apache.bcel.internal.classfile.Unknown;

/**
 * Alberto Vilches Ratón
 * <p>
 * Kenshira
 * <p/>
 * Fecha y hora de creación: 05-nov-2007 7:51:10
 */
public class UserFilter implements Filter, ConstantsLive {

    public static SimpleDateFormat dfsave = new SimpleDateFormat("dd_MM_yyyy HH_mm_ss");

    public static SimpleDateFormat dflong = new SimpleDateFormat("EEEE, d MMMM yyyy HH:mm:ss");

    public void destroy() {
    }

    private static final String STATIC_PATH = "/system/static/";

    public void doFilter(ServletRequest servletReq, ServletResponse servletResp, FilterChain chain) throws ServletException, IOException {
        HttpServletRequest request = (HttpServletRequest) servletReq;
        HttpServletResponse response = (HttpServletResponse) servletResp;
        try {
            if (checkRedirection(request, response)) {
                return;
            }
            tryLogin(request);
            Req breq = new Req(request);
            if (breq.user == null) {
                chain.doFilter(servletReq, servletResp);
            } else {
                if (breq.project == null) {
                    dispathFromUser(breq, request, response);
                } else {
                    if (breq.resource == null || breq.resource.length() == 0 || breq.resource.equals("index.jsp")) {
                        if (!breq.isfolder) {
                            redirect(request, response, request.getContextPath() + "/" + breq.user.getLogin() + "/" + breq.project.getAlias() + "/");
                        } else {
                            dipatchFromProject(breq, request, response);
                        }
                    } else {
                        dipatchFromResource(breq, request, response);
                    }
                }
            }
        } catch (Throwable e) {
            request.setAttribute("exception", e);
            serveJsp("/error.jsp", request, response);
        }
    }

    private boolean checkRedirection(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String server = Config.getMng().getServer();
        if (!Config.getMng().isDesktop() && server != null && Config.getMng().isRedirect()) {
            String currentServer = request.getServerName();
            String scheme = request.getScheme();
            if (scheme != null) {
                currentServer = scheme + "://" + currentServer;
            }
            int port = request.getServerPort();
            if (port != 80) {
                currentServer = currentServer + ":" + port;
            }
            if (!server.equals(currentServer)) {
                String url = server;
                String query = request.getQueryString();
                if (query != null && query.length() > 0) {
                    url = url + "?" + query;
                }
                response.sendRedirect(url);
                return true;
            }
        }
        return false;
    }

    private void dispathFromUser(Req breq, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException, SQLException {
        if (breq.resource == null || breq.resource.length() == 0 || breq.resource.equals("index.jsp")) {
            if (!breq.isfolder) {
                redirect(request, response, request.getContextPath() + "/" + breq.user.getLogin() + "/");
                return;
            }
            if (request.getParameter("explore") != null) {
                if (breq.isOwner()) {
                    File path = new File(Config.getMng().getDirCode().getCanonicalPath(), breq.user.getLogin());
                    serveFromPath(request, response, path, ".");
                } else {
                    response.sendError(HttpServletResponse.SC_NOT_FOUND);
                }
            } else {
                breq.user.setHits(breq.user.getHits() + 1);
                UsuarioManager.getInstance().save(breq.user);
                request.setAttribute("user", breq.user);
                serveJsp("/static/profileuser.jsp", request, response);
            }
        } else {
            String resource = breq.resource;
            if (request.getParameter("explore") != null) {
                if (breq.isOwner()) {
                    File path = new File(Config.getMng().getDirCode().getCanonicalPath(), breq.user.getLogin());
                    serveFromPath(request, response, path, resource);
                } else {
                    response.sendError(HttpServletResponse.SC_NOT_FOUND);
                }
            } else if (resource.startsWith("src") && resource.endsWith(".zip")) {
                Map<String, InputStream> map = new HashMap<String, InputStream>();
                for (ProyectoBean pb : Db.getInstance().listProjectsByUser(breq.usuarioSession.getLogin())) {
                    File files[] = ModuloMng.getInstance().getFiles(pb, true);
                    for (File file : files) {
                        map.put(pb.getAlias() + "/" + file.getName(), new FileInputStream(file));
                    }
                }
                response.setContentType("application/zip");
                Zip.zip(map, response.getOutputStream(), 9, null);
                response.getOutputStream().flush();
            } else {
                serveWebAppStatic(breq.resource, request, response);
            }
        }
    }

    private void dipatchFromProject(Req breq, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        if (request.getParameter("explore") != null) {
            if (breq.isOwner()) {
                File path = ModuloMng.getInstance().getCodeRoot(breq.project);
                serveFromPath(request, response, path, ".");
            } else {
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
            }
        } else {
            if (breq.mustRegister()) {
                response.sendRedirect(request.getContextPath() + "/register.jsp?must=true");
            } else if (!breq.permJugar()) {
                responseErrorJugar(breq, request, response);
            } else {
                String template = request.getParameter("template");
                if (template == null) {
                    template = breq.project.getTemplate();
                }
                boolean init = request.getParameter("init") != null;
                if (init) {
                    breq.removeGameSession();
                    breq.setNoLoadAutosave();
                    String uri = request.getRequestURL().toString();
                    response.sendRedirect(uri);
                } else {
                    File path = ModuloMng.getInstance().getCodeRoot(breq.project);
                    File gameTemplate = new File(path, "template.html");
                    if (gameTemplate.exists()) {
                        serveFromPath(request, response, path, "template.html");
                    } else {
                        if (template == null || template.trim().length() == 0) {
                            serveWebAppStatic("play-default.html", request, response);
                        } else {
                            serveWebAppStatic("play-" + template + ".html", request, response);
                        }
                    }
                }
            }
        }
    }

    private String getMensajeErrorJugar(Req breq) {
        String mes = null;
        if (!breq.project.getEjecutable()) {
            mes = "La aventura no es ejecutable (es una libreria)";
        } else if (breq.usuarioSession == null) {
            mes = "No se puede saber si tienes permisos porque no estas identificado todavia.";
        } else {
            mes = "La aventura no esta publicada y no tienes permiso para jugarla.";
        }
        return "Permiso jugar denegado: " + mes;
    }

    private String getMensajeErrorDepurar(Req breq) {
        String mes = null;
        if (!breq.project.getEjecutable()) {
            mes = "La aventura no es ejecutable (es una libreria)";
        } else if (breq.usuarioSession == null) {
            mes = "No se puede saber si tienes permisos porque no estas identificado todavia.";
        } else {
            mes = "No tienes permiso.";
        }
        return "Permiso depurar denegado: " + mes;
    }

    private String getMensajeErrorLeer(Req breq) {
        String mes = null;
        if (breq.usuarioSession == null) {
            mes = "No se puede saber si tienes permisos porque no estas identificado todavia.";
        } else {
            mes = "No tienes permiso.";
        }
        return "Permiso leer denegado: " + mes;
    }

    private String getMensajeErrorEscribir(Req breq) {
        String mes = null;
        if (breq.usuarioSession == null) {
            mes = "No se puede saber si tienes permisos porque no estas identificado todavia.";
        } else if (breq.project.getPublicado()) {
            mes = "La aventura ya esta publicada y no se puede modificar.";
        } else {
            mes = "No tienes permiso.";
        }
        return "Permiso escribir denegado: " + mes;
    }

    private void responseErrorJugar(Req breq, HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.sendError(HttpServletResponse.SC_FORBIDDEN, getMensajeErrorJugar(breq));
    }

    private void responseErrorDepurar(Req breq, HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.sendError(HttpServletResponse.SC_FORBIDDEN, getMensajeErrorDepurar(breq));
    }

    private void responseErrorEscribir(Req breq, HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.sendError(HttpServletResponse.SC_FORBIDDEN, getMensajeErrorEscribir(breq));
    }

    private void responseErrorLeer(Req breq, HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.sendError(HttpServletResponse.SC_FORBIDDEN, getMensajeErrorLeer(breq));
    }

    private void dipatchFromResource(Req breq, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        String resource = breq.resource;
        if (request.getParameter("explore") != null) {
            if (breq.isOwner()) {
                File path = ModuloMng.getInstance().getCodeRoot(breq.project);
                serveFromPath(request, response, path, resource);
            } else {
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
            }
        } else if ("edit".equals(resource)) {
            if (breq.permLeer()) {
                String part = request.getParameter("part");
                if (part != null) {
                    breq.putLastpartOpenedToSession(part);
                } else if (breq.getLastpartOpenedFromSession() == null) {
                    File[] files = ModuloMng.getInstance().getFiles(breq.getProject(), true);
                    if (files.length > 0) {
                        breq.putLastpartOpenedToSession(files[0].getName());
                    }
                }
                request.setAttribute("req", breq);
                serveJsp("/static/frames.jsp", request, response);
            } else {
                responseErrorLeer(breq, request, response);
            }
        } else if ("edit.list".equals(resource)) {
            if (breq.permLeer()) {
                request.setAttribute("req", breq);
                serveJsp("/static/editlist.jsp", request, response);
            } else {
                responseErrorLeer(breq, request, response);
            }
        } else if ("edit.show".equals(resource)) {
            if (breq.permLeer()) {
                String part = request.getParameter("part");
                if (part != null && part.trim().length() > 0) {
                    if (!new File(ModuloMng.getInstance().getCodeRoot(breq.project), part).exists()) {
                        response.sendError(HttpServletResponse.SC_FORBIDDEN, "Fichero " + part + " no existe");
                    } else if (part.endsWith(ModuloMng.ADV_EXTENSION) || isText(part)) {
                        breq.putLastpartOpenedToSession(part);
                        String editor = request.getParameter("editor");
                        if (editor != null && breq.usuarioSession != null) {
                            try {
                                UsuarioBean dbUser = UsuarioManager.getInstance().loadByPrimaryKey(breq.usuarioSession.getLogin());
                                dbUser.setEditor(editor);
                                UsuarioManager.getInstance().save(dbUser);
                                breq.usuarioSession.setEditor(editor);
                            } catch (SQLException e) {
                                ;
                            }
                        }
                        request.setAttribute("req", breq);
                        serveJsp("/static/edit.jsp", request, response);
                    } else {
                        request.setAttribute("req", breq);
                        serveJsp("/static/resource.jsp", request, response);
                    }
                }
            } else {
                responseErrorLeer(breq, request, response);
            }
        } else if ("edit.new".equals(resource)) {
            if (breq.permEscribir()) {
                String part = request.getParameter("part");
                if (part != null && part.trim().length() > 0) {
                    part = part.trim();
                    ModuloMng.getInstance().create(breq.project, part + ModuloMng.ADV_EXTENSION);
                }
                request.setAttribute("req", breq);
                serveJsp("/static/editlist.jsp", request, response);
            } else {
                responseErrorEscribir(breq, request, response);
            }
        } else if ("edit.upload".equals(resource)) {
            if (breq.permEscribir()) {
                uploadResources(request, breq);
                request.setAttribute("req", breq);
                serveJsp("/static/editlist.jsp", request, response);
            } else {
                responseErrorEscribir(breq, request, response);
            }
        } else if ("edit.delete".equals(resource)) {
            if (breq.permEscribir()) {
                String part = request.getParameter("part");
                if (part != null && part.trim().length() > 0) {
                    ModuloMng.getInstance().delete(breq.project, part);
                }
                request.setAttribute("req", breq);
                serveJsp("/static/editlist.jsp", request, response);
            } else {
                responseErrorEscribir(breq, request, response);
            }
        } else if ("info".equals(resource)) {
            if (breq.permLeer()) {
                request.setAttribute(KEY_MODULO, ModuloMng.getInstance().getSecureAdv(breq.project));
                serveJsp("/static/info.jsp", request, response);
            } else {
                responseErrorLeer(breq, request, response);
            }
        } else if ("session".equals(resource)) {
            if (breq.mustRegister()) {
                response.sendRedirect(request.getContextPath() + "/register.jsp?must=true ");
            } else if (breq.permDepurar()) {
                GameSession gs = breq.getGameFromSession();
                if (gs == null) {
                    response.sendError(HttpServletResponse.SC_FORBIDDEN, "No se ha iniciado ninguna partida.");
                } else {
                    request.setAttribute(KEY_MODULO, ModuloMng.getInstance().getSecureAdv(breq.project));
                    request.setAttribute(KEY_GAMESESSION, gs);
                    serveJsp("/static/tr.jsp", request, response);
                }
            } else {
                responseErrorDepurar(breq, request, response);
            }
        } else if ("info-actions".equals(resource)) {
            if (breq.permLeer()) {
                request.setAttribute(KEY_MODULO, ModuloMng.getInstance().getSecureAdv(breq.project));
                serveJsp("/static/info-actions.jsp", request, response);
            } else {
                responseErrorLeer(breq, request, response);
            }
        } else if ("info-ambiguous".equals(resource)) {
            if (breq.permLeer()) {
                request.setAttribute(KEY_MODULO, ModuloMng.getInstance().getSecureAdv(breq.project));
                serveJsp("/static/info-ambiguous.jsp", request, response);
            } else {
                responseErrorLeer(breq, request, response);
            }
        } else if (resource.startsWith("src") && resource.endsWith(".zip")) {
            if (breq.permLeer()) {
                Map<String, InputStream> map = Zip.fileArrayToMap(ModuloMng.getInstance().getFiles(breq.project, true));
                map.put("project.properties.xml", new ByteArrayInputStream(exportInfo(breq.project)));
                response.setContentType("application/zip");
                Zip.zip(map, response.getOutputStream(), 9, null);
                response.getOutputStream().flush();
            } else {
                responseErrorLeer(breq, request, response);
            }
        } else if (resource.equals("src")) {
            if (breq.permLeer()) {
                String filename = request.getParameter("part");
                if (!isText(filename) && !filename.endsWith(ModuloMng.ADV_EXTENSION)) {
                    serveFromPath(request, response, ModuloMng.getInstance().getCodeRoot(breq.project), filename);
                } else {
                    File path = ModuloMng.getInstance().getCodeRoot(breq.project);
                    File file = new File(path, filename);
                    if (!file.exists()) {
                        response.sendError(HttpServletResponse.SC_NOT_FOUND);
                    } else {
                        if (!file.getCanonicalPath().startsWith(path.getCanonicalPath())) {
                            response.sendError(HttpServletResponse.SC_NOT_FOUND);
                        } else if (file.isFile()) {
                            request.setCharacterEncoding(Config.getMng().getEncoding());
                            response.setCharacterEncoding(Config.getMng().getEncoding());
                            response.setContentType("text/plain");
                            TextTools.print(new InputStreamReader(new FileInputStream(file), Config.getMng().getEncoding()), response.getWriter());
                        } else {
                            response.sendError(HttpServletResponse.SC_NOT_FOUND);
                        }
                    }
                }
            } else {
                responseErrorLeer(breq, request, response);
            }
        } else if (resource.equals("log")) {
            if (breq.isOwner()) {
                try {
                    Integer id = new Integer(request.getParameter("id"));
                    GamesessionBean gsb = GamesessionManager.getInstance().loadByPrimaryKey(id);
                    if (gsb.getUsuario().equals(breq.project.getUsuario()) && gsb.getAlias().equals(breq.project.getAlias())) {
                        request.setAttribute("gsb", gsb);
                        serveJsp("/log.jsp", request, response);
                    } else {
                        response.sendError(HttpServletResponse.SC_NOT_FOUND);
                    }
                } catch (SQLException e) {
                    response.sendError(HttpServletResponse.SC_NOT_FOUND);
                } catch (NumberFormatException e) {
                    response.sendError(HttpServletResponse.SC_NOT_FOUND);
                }
            } else {
                responseErrorDepurar(breq, request, response);
            }
        } else if ("do".equals(resource)) {
            request.setCharacterEncoding(Config.getMng().getEncoding());
            response.setCharacterEncoding(Config.getMng().getEncoding());
            json(breq, request, response);
        } else if ("putdata".equals(resource)) {
            if (breq.permEscribir()) {
                try {
                    String src = request.getParameter("data");
                    String filename = request.getParameter("part");
                    ModuloMng.getInstance().saveData(breq.project, filename, src);
                    response.getWriter().println("Fichero guardado");
                } catch (IOException e) {
                    response.getWriter().println("Error: " + e.getMessage());
                }
            } else {
                response.getWriter().println(getMensajeErrorEscribir(breq));
            }
        } else if ("put".equals(resource)) {
            request.setCharacterEncoding(Config.getMng().getEncoding());
            response.setCharacterEncoding(Config.getMng().getEncoding());
            saveSrc(breq, request, response);
        } else if ("load".equals(resource)) {
            if (breq.permJugar()) {
                uploadSavegame(breq, request, response, breq.project);
            } else {
                responseErrorJugar(breq, request, response);
            }
        } else if ("error".equals(resource)) {
            Throwable errorListFromSession = breq.getLastExceptionFromSession();
            if (errorListFromSession != null) {
                request.setAttribute("error.list", errorListFromSession);
                serveJsp("/static/errorlist.jsp", request, response);
            } else {
                response.sendError(HttpServletResponse.SC_FORBIDDEN, "No hay informacion de error disponible.");
            }
        } else if ("session.xml".equals(resource)) {
            if (breq.mustRegister()) {
                response.sendRedirect(request.getContextPath() + "/register.jsp?must=true ");
            } else if (breq.permDepurar()) {
                GameSession gs = breq.getGameFromSession();
                if (gs == null) {
                    mensaje(response, "No se ha iniciado ninguna partida. Debes iniciar una antes para activar el salvado de partidas.");
                } else {
                    response.setContentType("text/plain");
                    response.setCharacterEncoding(Config.getMng().getEncoding());
                    SaveManager.getInstance().writePlainText(gs, new OutputStreamWriter(response.getOutputStream(), Config.getMng().getEncoding()));
                }
            } else {
                responseErrorDepurar(breq, request, response);
            }
        } else if (resource.endsWith(".save")) {
            if (breq.permJugar()) {
                GameSession gs = breq.getGameFromSession();
                if (gs == null) {
                    mensaje(response, "No se ha iniciado ninguna partida. Debes iniciar una antes para activar el salvado de partidas.");
                } else {
                    response.setContentType("application/zip");
                    SaveManager.getInstance().writeZipEncode(gs, response.getOutputStream());
                }
            } else {
                responseErrorJugar(breq, request, response);
            }
        } else {
            if (resource.contains("/")) {
                if (breq.isOwner() || breq.permJugar() || breq.permLeer()) {
                    serveWebAppStatic(breq.resource, request, response);
                } else {
                    response.sendError(HttpServletResponse.SC_NOT_FOUND);
                }
            } else {
                File path = ModuloMng.getInstance().getCodeRoot(breq.project);
                File file = new File(path, resource);
                if (file.exists()) {
                    if (breq.isOwner() || breq.permJugar()) {
                        if (resource.endsWith(ModuloMng.ADV_EXTENSION) && !breq.permLeer()) {
                            responseErrorLeer(breq, request, response);
                        }
                        serveFromPath(request, response, path, resource);
                    } else {
                        responseErrorJugar(breq, request, response);
                    }
                } else {
                    serveWebAppStatic(breq.resource, request, response);
                }
            }
        }
    }

    private byte[] exportInfo(ProyectoBean project) throws IOException {
        Properties prop = new Properties();
        prop.setProperty("nombre", project.getNombre());
        prop.setProperty("alias", project.getAlias());
        prop.setProperty("usuario", project.getUsuario());
        prop.setProperty("ejecutable", String.valueOf(project.getEjecutable()));
        prop.setProperty("hash", project.getHash());
        prop.setProperty("template", project.getTemplate());
        prop.setProperty("resumen", project.getResumen());
        ByteArrayOutputStream baos = new ByteArrayOutputStream(1024);
        prop.storeToXML(baos, "Comment", "UTF8");
        return baos.toByteArray();
    }

    public static String getIcon(String res) {
        String icon = "page_white.png";
        String name = res != null ? res.toLowerCase() : "";
        if (name.endsWith(ModuloMng.ADV_EXTENSION)) {
            icon = "page_white_code.png";
        } else if (name.endsWith(".html") || name.endsWith(".html")) {
            icon = "html.png";
        } else if (name.endsWith(".css")) {
            icon = "css.png";
        } else if (name.endsWith(".pdf")) {
            icon = "page_white_acrobat.png";
        } else if (name.endsWith(".zip") || name.endsWith(".rar") || name.endsWith(".tar") || name.endsWith(".tgz") || name.endsWith(".ace") || name.endsWith(".7z") || name.endsWith(".dmg")) {
            icon = "compress.png";
        } else if (name.endsWith(".swf")) {
            icon = "page_white_flash.png";
        } else if (name.endsWith(".doc") || name.endsWith(".xls") || name.endsWith(".ppt") || name.endsWith(".pps")) {
            icon = "page_white_office.png";
        } else if (UserFilter.isText(name)) {
            icon = "page_white_text.png";
        } else if (UserFilter.isImage(name)) {
            icon = "picture.png";
        } else if (UserFilter.isMusic(name)) {
            icon = "music.png";
        }
        return icon;
    }

    public static boolean isText(String res) {
        res = res.toLowerCase();
        return getContentType(res).startsWith("text/") || res.endsWith(".xml");
    }

    public static boolean isImage(String res) {
        return getContentType(res).startsWith("image/");
    }

    public static boolean isMusic(String res) {
        res = res.toLowerCase();
        return res.endsWith(".mp3") || res.endsWith(".mod") || res.endsWith(".wma") || res.endsWith(".ogg") || res.endsWith(".wav") || res.endsWith(".aiff") || res.endsWith(".mpc") || res.endsWith(".au") || res.endsWith(".mid") || res.endsWith(".flac") || res.endsWith(".ra");
    }

    private void uploadResources(HttpServletRequest request, Req req) {
        ServletRequestContext srctxt = new ServletRequestContext(request);
        if (!ServletFileUpload.isMultipartContent(srctxt)) {
            return;
        }
        try {
            Properties fields = new Properties();
            InputStream res = null;
            ServletFileUpload servletFileUpload = Config.getMng().getServletFileUpload();
            List items = servletFileUpload.parseRequest(srctxt);
            for (Iterator iterator = items.iterator(); iterator.hasNext(); ) {
                FileItem item = (FileItem) iterator.next();
                if (item.isFormField()) {
                    String name = item.getFieldName();
                    String value = item.getString().trim();
                    fields.put(name, value);
                } else {
                    res = item.getInputStream();
                    String name = item.getName();
                    String filename = FilenameUtils.getName(name);
                    File dest = new File(ModuloMng.getInstance().getCodeRoot(req.project), filename);
                    OutputStream os = new BufferedOutputStream(new FileOutputStream(dest));
                    try {
                        IOTools.copy(res, os);
                    } catch (Exception e) {
                        e.printStackTrace();
                    } finally {
                        if (os != null) try {
                            os.flush();
                            os.close();
                        } catch (Exception e) {
                        }
                        ;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void serveWebAppStatic(String resource, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        if (resource.endsWith(".jsp")) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
        } else {
            request.getRequestDispatcher("/static/" + resource).forward(request, response);
        }
    }

    private void serveJsp(String resource, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        request.setCharacterEncoding(Config.getMng().getEncoding());
        response.setCharacterEncoding(Config.getMng().getEncoding());
        request.setAttribute("fromfilter", this);
        request.getRequestDispatcher(resource).forward(request, response);
    }

    public void serveFromPath(HttpServletRequest request, HttpServletResponse response, File path, String resource) throws IOException, ServletException {
        File file = new File(path, resource);
        if (!file.exists()) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
        } else {
            if (!file.getCanonicalPath().startsWith(path.getCanonicalPath())) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND, file.toString());
            } else if (file.isFile()) {
                response.setContentType(getContentType(resource));
                if (request.getParameter("download") != null) {
                    response.addHeader("Content-Disposition", "attachment; filename=" + URLEncoder.encode(resource, Config.getMng().getEncoding()));
                    response.addHeader("Content-Type", "application/force-download");
                    response.addHeader("Content-Type", "application/octet-stream");
                    response.addHeader("Content-Type", "application/download");
                    response.addHeader("Content-Description", "File Transfer");
                }
                response.addHeader("Content-Length", "" + file.length());
                int total = IOTools.copy(new FileInputStream(file), response.getOutputStream());
            } else if (file.isDirectory()) {
                request.setAttribute("dir", file);
                serveJsp("/static/filelist.jsp", request, response);
            } else {
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
            }
        }
    }

    public static String getContentType(String name) {
        name = name.toLowerCase();
        if (name.endsWith(".js")) {
            return "text/javascript";
        } else if (name.endsWith(".css")) {
            return "text/css";
        } else if (name.endsWith(".html") || name.endsWith(".htm")) {
            return "text/html";
        } else if (name.endsWith(".txt") || name.endsWith(".log") || name.endsWith(".properties")) {
            return "text/plain";
        } else if (name.endsWith(".gif")) {
            return "image/gif";
        } else if (name.endsWith(".jpg") || name.endsWith(".jpeg")) {
            return "image/jpeg";
        } else if (name.endsWith(".png")) {
            return "image/png";
        } else if (name.endsWith(".zip")) {
            return "application/zip";
        } else if (name.endsWith(".xml")) {
            return "application/xml";
        } else {
            return "application/octet-stream";
        }
    }

    private void tryLogin(HttpServletRequest request) throws SQLException {
        String loginErrorMessage = null;
        UsuarioSession user = UsuarioSession.getUsuario(request);
        String autologin = Config.getMng().getAutologin();
        boolean canlogin = true;
        if (autologin != null) {
            canlogin = false;
            if (user == null) {
                UsuarioBean dbUser = UsuarioManager.getInstance().loadByPrimaryKey(autologin);
                if (dbUser == null) {
                    loginErrorMessage = "Usuario de autologin [" + autologin + "] no existe!";
                    canlogin = true;
                } else {
                    login(request, dbUser);
                }
            }
        } else {
            if (request.getParameter("logout") != null) {
                request.getSession().invalidate();
            }
        }
        if (canlogin && "true".equals(request.getParameter("login"))) {
            UsuarioBean dbUser = UsuarioManager.getInstance().loadByPrimaryKey(request.getParameter("l"));
            if (dbUser == null || !dbUser.getPassword().equals(TextTools.getMD5(request.getParameter("p")))) {
                loginErrorMessage = "Id o clave incorrecto";
            } else {
                login(request, dbUser);
            }
        }
        request.setAttribute("loginErrorMessage", loginErrorMessage);
    }

    private void login(HttpServletRequest request, UsuarioBean dbUser) throws SQLException {
        dbUser.setLogins(dbUser.getLogins() + 1);
        dbUser.setUltlogin(System.currentTimeMillis());
        UsuarioManager.getInstance().save(dbUser);
        request.getSession().invalidate();
        adv.web.UsuarioSession.putUsuario(request, dbUser);
    }

    private void redirect(HttpServletRequest request, HttpServletResponse response, String newUrl) throws IOException {
        String query = request.getQueryString();
        if (query != null && query.length() > 0) {
            newUrl = newUrl + "?" + query;
        }
        response.sendRedirect(newUrl);
    }

    public static boolean permJugar(ProyectoBean pb, UsuarioSession user) {
        return pb.getEjecutable() && (isOwner(pb, user) || pb.getPublicado() || permCheck(pb.getPJugar(), user));
    }

    public static boolean permDepurar(ProyectoBean pb, UsuarioSession user) {
        return isOwner(pb, user) || permCheck(pb.getPDepurar(), user);
    }

    public static boolean permLeer(ProyectoBean pb, UsuarioSession user) {
        return isOwner(pb, user) || permCheck(pb.getPLeer(), user);
    }

    public static boolean permEscribir(ProyectoBean pb, UsuarioSession user) {
        return (isOwner(pb, user) && !pb.getPublicado()) || isRoot(user);
    }

    private static boolean isRoot(UsuarioSession user) {
        return user != null && user.isRoot();
    }

    public static boolean isOwner(ProyectoBean pb, UsuarioSession user) {
        return isOwner(pb.getUsuario(), user);
    }

    public static boolean isOwner(String userid, UsuarioSession user) {
        return (user != null && (user.isRoot() || userid.equals(user.getLogin())));
    }

    public static boolean permCheck(String c, UsuarioSession user) {
        String clob = " " + c + " ";
        return c != null && (clob.indexOf(" * ") > -1 || (user != null && clob.indexOf(" " + user.getLogin() + " ") > -1 && clob.indexOf(" !" + user.getLogin() + " ") == -1));
    }

    public static class Req {

        UsuarioSession usuarioSession;

        UsuarioBean user;

        ProyectoBean project;

        String resource;

        int type;

        boolean isfolder;

        HttpServletRequest request;

        public boolean permJugar() {
            return UserFilter.permJugar(project, usuarioSession);
        }

        public boolean permDepurar() {
            return permJugar() && UserFilter.permDepurar(project, usuarioSession);
        }

        public boolean permLeer() {
            return UserFilter.permLeer(project, usuarioSession);
        }

        public boolean permEscribir() {
            return UserFilter.permEscribir(project, usuarioSession);
        }

        public boolean isOwner() {
            return UserFilter.isOwner(user.getLogin(), usuarioSession);
        }

        public Req(HttpServletRequest request) {
            this.request = request;
            usuarioSession = UsuarioSession.getUsuario(request);
            resource = getServletPath(request);
            if (resource.startsWith("/")) {
                resource = resource.substring(1);
            }
            isfolder = resource.endsWith("/");
            String userid = nextToken();
            try {
                user = UsuarioManager.getInstance().loadByPrimaryKey(userid);
            } catch (SQLException e) {
                user = null;
            }
            if (!moreTokens()) {
                return;
            }
            if (user != null) {
                String alias = nextToken();
                try {
                    project = ProyectoManager.getInstance().loadByPrimaryKey(alias, userid);
                } catch (SQLException e) {
                    project = null;
                }
                if (project == null) {
                    if (moreTokens()) {
                        resource = alias + "/" + resource;
                    } else {
                        resource = alias;
                    }
                }
            }
        }

        private boolean moreTokens() {
            return resource.length() > 0;
        }

        private String nextToken() {
            String result;
            int pos = resource.indexOf("/");
            if (pos < 1) {
                result = resource.substring(0);
                resource = "";
            } else {
                result = resource.substring(0, pos);
                resource = resource.substring(pos + 1);
            }
            return result;
        }

        public static String getServletPath(HttpServletRequest request) {
            String servletPath = request.getServletPath();
            if (null != servletPath && !"".equals(servletPath)) {
                return servletPath;
            }
            String requestUri = request.getRequestURI();
            int startIndex = request.getContextPath().equals("") ? 0 : request.getContextPath().length();
            int endIndex = request.getPathInfo() == null ? requestUri.length() : requestUri.lastIndexOf(request.getPathInfo());
            if (startIndex > endIndex) {
                endIndex = startIndex;
            }
            return requestUri.substring(startIndex, endIndex);
        }

        public void putGameSessionToSession(AdvOgnlscriptContext context, GameSession gameSession, boolean manualLoad) {
            request.getSession().setAttribute(KEY_GAMESESSION + user.getLogin() + "/" + project.getAlias(), gameSession);
            context.setGameSession(gameSession);
            bindBeansToGameSession(gameSession, manualLoad);
            try {
                SessionMng.getInstance().update(context, request, gameSession.getGamesessionId());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        private void bindBeansToGameSession(GameSession gameSession, boolean manualLoad) {
            GamedataBean gdb = null;
            try {
                if (gameSession.getGamedataBean() == null && usuarioSession != null) {
                    gdb = Db.getInstance().findGamedata(project, usuarioSession.getLogin());
                    if (gdb == null) {
                        gdb = GamedataManager.getInstance().createGamedataBean();
                        gdb.setAlias(project.getAlias());
                        gdb.setUsuario(project.getUsuario());
                        gdb.setUsuarioPlayer(usuarioSession.getLogin());
                        GamedataManager.getInstance().save(gdb);
                    }
                    gameSession.setGamedataBean(gdb);
                }
                Integer gamessessionId = gameSession.getGamesessionId();
                boolean createInBd = false;
                if (gamessessionId == null) {
                    createInBd = true;
                } else {
                    GamesessionBean gsb = GamesessionManager.getInstance().loadByPrimaryKey(gamessessionId);
                    if (gsb == null) {
                        createInBd = true;
                    } else {
                        gsb.setFechaultima(new Date());
                        if (manualLoad) {
                            gsb.setParts(gsb.getParts() + 1);
                        }
                        GamesessionManager.getInstance().save(gsb);
                    }
                }
                if (createInBd) {
                    GamesessionBean gsb = GamesessionManager.getInstance().createGamesessionBean();
                    gsb.setAlias(project.getAlias());
                    gsb.setUsuario(project.getUsuario());
                    gsb.setUsuarioPlayer(usuarioSession == null ? null : usuarioSession.getLogin());
                    gsb.setFechainicio(new Date());
                    gsb.setParts(1);
                    GamesessionManager.getInstance().save(gsb);
                    gameSession.setGamesessionId(gsb.getGamesessionId());
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        public GameSession getGameFromSession() {
            return (GameSession) request.getSession().getAttribute(KEY_GAMESESSION + user.getLogin() + "/" + project.getAlias());
        }

        public void removeGameSession() {
            request.getSession().removeAttribute(ConstantsLive.KEY_GAMESESSION + user.getLogin() + "/" + project.getAlias());
        }

        public void putLastExceptionInSession(Throwable causeList) {
            request.getSession().setAttribute("error.list." + user.getLogin() + "." + project.getAlias(), causeList);
        }

        public Throwable getLastExceptionFromSession() {
            return (Throwable) request.getSession().getAttribute("error.list." + user.getLogin() + "." + project.getAlias());
        }

        public void removeLastExceptionFromSession() {
            request.getSession().removeAttribute("error.list." + user.getLogin() + "." + project.getAlias());
        }

        public ProyectoBean getProject() {
            return project;
        }

        public void putLastpartOpenedToSession(String part) {
            request.getSession().setAttribute("lastopened.part." + user.getLogin() + "." + project.getAlias(), part);
        }

        public String getLastpartOpenedFromSession() {
            return (String) request.getSession().getAttribute("lastopened.part." + user.getLogin() + "." + project.getAlias());
        }

        public UsuarioSession getUsuarioSession() {
            return usuarioSession;
        }

        public boolean mustRegister() {
            return usuarioSession == null && !Config.getMng().isAllowanonymousplay();
        }

        public void setNoLoadAutosave() {
            request.getSession().setAttribute("no.load.autosave", true);
        }

        public void setLoadAutosave() {
            request.getSession().removeAttribute("no.load.autosave");
        }

        public boolean isNoLoadAutosave() {
            return request.getSession().getAttribute("no.load.autosave") != null;
        }
    }

    public void init(FilterConfig config) throws ServletException {
    }

    public void json(Req req, HttpServletRequest request, HttpServletResponse httpResponse) throws IOException, RuntimeException {
        double time = System.nanoTime();
        AdvOgnlscriptContext context = AdvOgnlscriptContext.newContext(req.getUsuarioSession());
        AdvResponse response = context.getAdvResponse();
        try {
            response.setSystemDebug("true".equals(request.getParameter("logDebug")));
            response.setLogParsing("true".equals(request.getParameter("logParsing")));
            response.setRefresh("true".equals(request.getParameter("refresh")));
            context.setDebug(req.permDepurar());
            String phrase = AsciiTools.convertNonAscii(FileParser.trimSpaces(request.getParameter("phrase")).toLowerCase());
            if (phrase.startsWith(":reload") && req.permEscribir()) {
                String line = request.getParameter("phrase").substring(7);
                StringTokenizer st = new StringTokenizer(line, ",");
                Set<String> files = new HashSet<String>();
                while (st.hasMoreTokens()) {
                    files.add(st.nextToken().trim());
                }
                if (files.isEmpty()) {
                    files = null;
                }
                Modulo d = ModuloMng.getInstance().load(req.project.getAlias(), req.user.getLogin(), files);
                context.setModulo(d);
                for (Object s : d.getWarnings()) {
                    response.println(String.valueOf(s));
                }
                if (!d.getErrors().isEmpty()) {
                    response.systemDebug("== Compilacion proyecto " + req.project.getAlias() + " con errores:");
                    response.println("== Compilacion proyecto " + req.project.getAlias() + " con errores:");
                    manageJsonException(req, request, context, response, d.getErrors(), request.getParameter("phrase"));
                } else {
                    refreshSessionContext(req, request, context);
                }
                response.systemDebug("== Compilacion proyecto " + req.project.getAlias() + " finalizada.");
                response.println("== Compilacion proyecto " + req.project.getAlias() + " finalizada.");
                double end = (System.nanoTime() - time);
                response.println("Terminado en " + (end / 1000000000D) + " segundos");
            } else {
                context.setModulo(ModuloMng.getInstance().getSecureAdv(req.project));
                String phraseEval = request.getParameter("phrase");
                if (phraseEval == null) {
                    phraseEval = "";
                }
                boolean evaling = false;
                if ("true".equals(request.getParameter("eval")) && req.permDepurar()) {
                    evaling = true;
                } else if (phraseEval.startsWith(":eval") && req.permDepurar()) {
                    evaling = true;
                    phraseEval = phraseEval.substring(5).trim();
                }
                if (req.permJugar() || evaling) {
                    if (META_START.equals(phrase)) {
                        bindSessionInContext(req, request, context, true);
                        GameManager.start(context);
                    } else if (META_DESTROY.equals(phrase)) {
                        req.removeGameSession();
                        req.setNoLoadAutosave();
                        context.getAdvResponse().println("Eliminando partida de la sesion actual.");
                    } else {
                        boolean first = bindSessionInContext(req, request, context, false);
                        if (first) {
                            GameManager.start(context);
                        }
                        if (evaling) {
                            GameManager.eval(context, phraseEval, true);
                        } else if (phrase != null) {
                            if ("-".equals(phrase) || phrase.startsWith("!")) {
                            } else {
                                process(req, request, context, phrase);
                            }
                        }
                        SessionMng.getInstance().touch(context, request, phrase);
                    }
                } else {
                    response.systemDebug(getMensajeErrorJugar(req));
                    response.println(getMensajeErrorJugar(req));
                }
            }
            req.removeLastExceptionFromSession();
        } catch (Throwable e) {
            manageJsonException(req, request, context, response, e, request.getParameter("phrase"));
        } finally {
            double end = (System.nanoTime() - time);
            response.printStdErr(context.getExpressionCount() + " expr. -------------------------------------------------------------- " + (end / 1000000000D) + " segundos");
            ModuloMng.getInstance().registerRequest(context.getExpressionCount(), end);
            flush(context, httpResponse);
        }
    }

    public static final String META_START = "reiniciar";

    public static final String META_DESTROY = ":destroy";

    public void saveSrc(Req req, HttpServletRequest request, HttpServletResponse httpResponse) throws IOException, RuntimeException {
        double time = System.nanoTime();
        AdvOgnlscriptContext context = AdvOgnlscriptContext.newContext(req.getUsuarioSession());
        AdvResponse response = context.getAdvResponse();
        try {
            response.setSystemDebug("true".equals(request.getParameter("logDebug")));
            response.setLogParsing("true".equals(request.getParameter("logParsing")));
            response.setRefresh("true".equals(request.getParameter("refresh")));
            context.setDebug(req.permDepurar());
            if (req.permEscribir()) {
                String src = request.getParameter("data");
                String filename = request.getParameter("part");
                Modulo d = ModuloMng.getInstance().save(req.project, filename, src);
                context.setModulo(d);
                response.systemDebug("Modulo " + filename + " guardado y compilado correctamente. (" + src.length() + " bytes)");
                response.println("Modulo " + filename + " guardado y compilado correctamente. (" + src.length() + " bytes)");
                for (String s : d.getWarnings()) {
                    response.println(s);
                }
                if (!d.getErrors().isEmpty()) {
                    manageJsonException(req, request, context, response, d.getErrors(), request.getParameter("phrase"));
                } else {
                    refreshSessionContext(req, request, context);
                }
            } else {
                response.systemDebug(getMensajeErrorEscribir(req));
                response.println(getMensajeErrorEscribir(req));
            }
            req.removeLastExceptionFromSession();
        } catch (Throwable e) {
            manageJsonException(req, request, context, response, e, null);
        } finally {
            double end = (System.nanoTime() - time);
            response.println("Terminado en " + (end / 1000000000D) + " segundos");
            response.printStdErr(context.getExpressionCount() + " expr. -------------------------------------------------------------- " + (end / 1000D) + " segundos");
            flush(context, httpResponse);
        }
    }

    private void manageJsonException(Req req, HttpServletRequest request, AdvOgnlscriptContext context, AdvResponse response, List<CompileParserException> list, String phrase) {
        response.printStdErr("<div class='error'>" + list.size() + " error/es de compilacion</div>\n");
        response.printStdOut("<pre class='error'>" + list.size() + " error/es de compilacion</pre>\n");
        for (CompileParserException e : list) {
            manageJsonException(req, request, context, response, e, phrase);
        }
    }

    private void manageJsonException(Req req, HttpServletRequest request, AdvOgnlscriptContext context, AdvResponse response, Throwable e, String phrase) {
        if (e instanceof RuntimeException) {
            e = new RuntimeGameException(e.getCause() != null ? e.getCause() : e);
        }
        try {
            req.putLastExceptionInSession(e);
            response.printStdErr("<div class='error'><pre>" + TextUtil.escapeHTML(TextTools.printStackTrace(e)) + "</pre></div>\n");
            List<Throwable> causeList = TextTools.getStackList(e);
            Throwable cause = causeList.get(causeList.size() - 1);
            response.printStdOut("<pre class='error'>\n");
            response.printStdOut(e.getClass().getName() + ": " + cause.getClass().getName() + "\n");
            Set<String> trace = new LinkedHashSet<String>();
            for (Throwable innerCause : causeList) {
                if (innerCause.getMessage() != null) {
                    trace.add(innerCause.getMessage());
                }
            }
            for (String message : trace) {
                response.printStdOut(TextUtil.escapeHTML(message) + "\n");
            }
            if (context.getStackTraceError() != null) {
                ListIterator<OgnlscriptTraceableException> rev = context.getStackTraceError().listIterator(context.getStackTraceError().size());
                while (rev.hasPrevious()) {
                    OgnlscriptTraceableException pre = rev.previous();
                    response.printStdOut("    en " + TextUtil.escapeHTML(pre.toString()) + "\n");
                }
            }
            response.printStdOut("</pre>\n");
            if (e instanceof RuntimeGameException) {
                SessionMng.getInstance().logException(context, request, e, phrase);
            }
        } catch (Exception e2) {
            e2.printStackTrace();
        }
    }

    private void flush(AdvOgnlscriptContext context, HttpServletResponse httpResponse) {
        AdvResponse response = context.getAdvResponse();
        if (response.isRefresh() && context.getGameSession() != null) {
            for (Iterator i = context.getGameSession().getFields().entrySet().iterator(); i.hasNext(); ) {
                Map.Entry entry = (Map.Entry) i.next();
                response.put(String.valueOf(entry.getKey()), String.valueOf(entry.getValue()));
            }
        }
        for (TransactionBufferStack tb : response.getBuffers().values()) {
            AdvTransactionBufferStack tbs = (AdvTransactionBufferStack) tb;
            tbs.commitAll();
            String text = "";
            if (tbs.getText().length() > 0) {
                text = response.render(tbs.getText());
            }
            String method = tbs.isPrompt() ? "scrollinput" : "scroll";
            if (context.isDebug()) {
                response.put(tbs.getName(), text, false, method);
            } else {
                if (!tbs.isErr()) {
                    response.put(tbs.getName(), text, false, method);
                }
            }
        }
        httpResponse.setContentType("text/plain");
        httpResponse.setHeader("Cache-Control", "no-cache");
        String out = context.getResponse().toString();
        try {
            httpResponse.getOutputStream().write(out.getBytes(Config.getMng().getEncoding()));
            httpResponse.getOutputStream().flush();
        } catch (Exception e) {
            try {
                httpResponse.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error al escribir salida json.");
            } catch (Exception ee) {
                e.printStackTrace();
            }
        }
    }

    public void process(Req req, HttpServletRequest request, AdvOgnlscriptContext context, String phrase) throws Throwable {
        if (!checkPreGame(req, request, context, phrase)) {
            if (context.getModulo() == null) {
                context.getAdvResponse().println("No existe definicion de juego. Cargue uno desde la ventana 'Fuente'");
            } else if (context.getGameSession() == null) {
                context.getAdvResponse().println("Partida no inicializada. Utilice " + META_START);
            } else {
                GameManager.run(request, context, phrase);
                autosave(request, context);
            }
        }
    }

    public void autosave(HttpServletRequest request, AdvOgnlscriptContext context) throws IOException {
        UsuarioSession usuarioSesion = UsuarioSession.getUsuario(request);
        if (usuarioSesion != null) {
            SaveManager.getInstance().save(context, usuarioSesion.getLogin(), "autosave", true);
        }
    }

    public void refreshSessionContext(Req req, HttpServletRequest request, AdvOgnlscriptContext context) throws IOException {
        if (context.getModulo() != null) {
            GameSession gameSession = req.getGameFromSession();
            if (gameSession != null) {
                context.setGameSession(gameSession);
                autosave(request, context);
                UsuarioSession usuarioSession = UsuarioSession.getUsuario(request);
                try {
                    gameSession = SaveManager.getInstance().load(context, usuarioSession.getLogin(), "autosave");
                } catch (ObjDefNotFoundException e) {
                    context.getAdvResponse().printStdOut(e.getMessage());
                } catch (IOException e) {
                    context.getAdvResponse().printStdOut(e.getMessage());
                }
                if (gameSession != null && (gameSession.getHash().equals(context.getModulo().getHash()))) {
                    req.putGameSessionToSession(context, gameSession, false);
                    gameSession.touch();
                }
            }
        }
    }

    public boolean bindSessionInContext(Req req, HttpServletRequest request, AdvOgnlscriptContext context, boolean destroy) throws CompileParserException, IOException {
        boolean first = false;
        if (context.getModulo() == null) {
            throw new RuntimeGameException("No existe definicion de juego. Cargue uno desde la ventana 'Fuente'");
        } else {
            GameSession gameSession = req.getGameFromSession();
            if (gameSession == null || destroy) {
                UsuarioSession usuarioSession = UsuarioSession.getUsuario(request);
                if (destroy || usuarioSession == null || req.isNoLoadAutosave()) {
                    gameSession = createSession(req, request, context);
                    first = true;
                } else {
                    try {
                        gameSession = SaveManager.getInstance().load(context, usuarioSession.getLogin(), "autosave");
                    } catch (ObjDefNotFoundException e) {
                        context.getAdvResponse().printStdOut(e.getMessage());
                    } catch (IOException e) {
                        context.getAdvResponse().printStdOut(e.getMessage());
                    }
                    if (gameSession == null || (!gameSession.getHash().equals(context.getModulo().getHash()))) {
                        gameSession = createSession(req, request, context);
                        first = true;
                    } else {
                        context.getAdvResponse().println("NOTA: Reanundando partida de guardado automatico (" + dflong.format(new Date(gameSession.getLastAccess())) + ")");
                    }
                }
            } else {
                first = false;
                if (!gameSession.getHash().equals(context.getModulo().getHash())) {
                    context.getAdvResponse().println("La partida en curso no pertenece a la version actual del juego, comenzando nueva partida...");
                    gameSession = createSession(req, request, context);
                    first = true;
                }
            }
            req.setLoadAutosave();
            req.putGameSessionToSession(context, gameSession, false);
            gameSession.touch();
            return first;
        }
    }

    public GameSession createSession(Req req, HttpServletRequest request, AdvOgnlscriptContext context) throws CompileParserException {
        req.removeGameSession();
        req.project.setPartidas(req.project.getPartidas() + 1);
        try {
            ProyectoManager.getInstance().save(req.project);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return new GameSession(context.getModulo());
    }

    static Set SAVE_COMMANDS = new HashSet(Arrays.asList(new String[] { ":salva", ":salvar", ":guarda", ":guardar", ":save" }));

    static Set LOAD_COMMANDS = new HashSet(Arrays.asList(new String[] { ":carga", ":cargar", ":load" }));

    static Set DELETE_COMMANDS = new HashSet(Arrays.asList(new String[] { ":borra", ":borrar", ":elimina", ":eliminar", ":remove", ":erase", ":delete", ":rm", ":del" }));

    public boolean checkPreGame(Req req, HttpServletRequest request, AdvOgnlscriptContext context, String phrase) throws IOException, OgnlException {
        boolean result = true;
        StringTokenizer st = new StringTokenizer(phrase);
        String comando = st.hasMoreTokens() ? st.nextToken() : null;
        if (DELETE_COMMANDS.contains(comando)) {
            if (req.usuarioSession == null) {
                context.getAdvResponse().println("Los usuarios no registrados no pueden borrar <a href=\"" + request.getContextPath() + "/\" target=\"_blank\">registrate</a> para poder guardar las partidas en servidor).");
            } else {
                String partida = st.hasMoreTokens() ? st.nextToken() : null;
                if (partida != null) {
                    try {
                        TextTools.validateNumName(partida, "_");
                    } catch (Exception e) {
                        context.getAdvResponse().println("Nombre de partida incorrecto: solo debe contener letras y numeros.");
                        return result;
                    }
                    try {
                        SaveManager.getInstance().delete(context, req.usuarioSession.getLogin(), partida);
                    } catch (FileNotFoundException e) {
                        context.getAdvResponse().println("La partida \"" + partida + "\" no existe.");
                    }
                } else {
                    context.getAdvResponse().println("Para borrar una partida debes especificar el nombre con " + comando + " nombrepartida");
                }
            }
        } else if (SAVE_COMMANDS.contains(comando)) {
            if (req.usuarioSession == null) {
                context.getAdvResponse().println("<div class=\"loadsavegame\">" + "<a href=\"" + context.getModulo().getId().replaceAll("/", ".") + "." + dfsave.format(new Date()) + ".save\">Descargar partida</a> " + "(<a href=\"" + request.getContextPath() + "/\" target=\"_blank\">registrate</a> para poder guardar las partidas en servidor).</div>");
            } else {
                String partida = st.hasMoreTokens() ? st.nextToken() : null;
                if (partida != null) {
                    try {
                        TextTools.validateNumName(partida, "_");
                    } catch (Exception e) {
                        context.getAdvResponse().println("Nombre de partida incorrecto: solo debe contener letras y numeros.");
                        return result;
                    }
                    String param = st.hasMoreTokens() ? st.nextToken() : null;
                    boolean force = param != null && param.trim().toLowerCase().equals("force");
                    try {
                        SaveManager.getInstance().save(context, req.usuarioSession.getLogin(), partida, force);
                        context.getAdvResponse().println("La partida \"" + partida + "\" ha sido salvada en servidor. Tambien puedes <a href=\"" + request.getContextPath() + "/" + context.getModulo().getId() + "/" + context.getModulo().getId().replaceAll("/", ".") + "." + partida + "." + dfsave.format(new Date()) + ".save\">descargartela</a> (aunque no es necesario) para tener tu propia copia.");
                    } catch (FileNotFoundException e) {
                        String com = comando + " " + partida + " force";
                        context.getAdvResponse().println("La partida \"" + partida + "\" ya existe. Para sobreescribirla debes introducir <a href='javascript:void(autoSendPhrase(null, \"" + com + "\"))'>" + com + "</a>.");
                    }
                } else {
                    context.getAdvResponse().println("Para salvar una partida debes especificar el nombre con " + comando + " nombrepartida");
                }
            }
        } else if (LOAD_COMMANDS.contains(comando)) {
            String partida = st.hasMoreTokens() ? st.nextToken() : null;
            if (req.usuarioSession == null || partida == null) {
                if (req.usuarioSession != null) {
                    listaPartidas(req, context);
                }
                long id = System.currentTimeMillis();
                context.getAdvResponse().print("<div class=\"loadsavegame\" id=\"upload_" + id + "\">" + "<form action=\"load\" method=\"post\" enctype=\"multipart/form-data\" id=\"form_" + id + "\" target=\"uploadiframe_" + id + "\">" + "<input type=\"hidden\" name=\"uploadid\" value=\"" + id + "\">" + "<input type=\"file\" name=\"p\">&nbsp;" + "<input type=\"submit\" name=\"submit\" value=\"Subir partida\"/> ");
                if (req.usuarioSession == null) {
                    context.getAdvResponse().print("(<a href=\"" + request.getContextPath() + "/register.jsp\" target=\"_blank\">registrate</a> para poder guardar las partidas en servidor).");
                }
                context.getAdvResponse().print("<iframe id=\"uploadiframe_" + id + "\" name=\"uploadiframe_" + id + "\" width=\"0\" height=\"0\" frameborder=\"0\" scrolling=\"no\" marginheight=\"0\" marginwidth=\"0\">" + "</form>" + "</div>");
            } else {
                try {
                    TextTools.validateNumName(partida, "_");
                } catch (Exception e) {
                    context.getAdvResponse().println("Nombre de partida incorrecto: solo debe contener letras y numeros.");
                    return result;
                }
                try {
                    GameSession gameSession = SaveManager.getInstance().load(context, req.usuarioSession.getLogin(), partida);
                    if (gameSession.getHash().equals(context.getGameSession().getHash())) {
                        req.putGameSessionToSession(context, gameSession, true);
                        context.getAdvResponse().println("NOTA: Cargando partida \"" + partida + "\"");
                        context.getAdvResponse().setRefresh(true);
                    } else {
                        context.getAdvResponse().println("Error, la partida es de otra version.");
                    }
                } catch (FileNotFoundException e) {
                    context.getAdvResponse().println("La partida \"" + partida + "\" no existe.");
                    listaPartidas(req, context);
                } catch (Exception e) {
                    context.getAdvResponse().println("Error al cargar la partida \"" + partida + "\" (" + e.getMessage() + ").");
                }
            }
        } else {
            result = false;
        }
        return result;
    }

    private void listaPartidas(Req req, AdvOgnlscriptContext context) throws IOException {
        final String patronInicioFicheros = context.getModulo().getId().replaceAll("/", ".") + ".";
        File[] files = SaveManager.getRoot(req.usuarioSession.getLogin()).listFiles(new FileFilter() {

            public boolean accept(File pathname) {
                return pathname.isFile() && pathname.getName().startsWith(patronInicioFicheros) && pathname.getName().endsWith(".save");
            }
        });
        if (files.length > 0) {
            StringBuilder buf = new StringBuilder();
            buf.append("Listado de partidas guardadas:<ul>");
            for (File file : files) {
                String filepartida = file.getName().substring(patronInicioFicheros.length(), file.getName().length() - 5);
                buf.append("<li>" + "[<a href='javascript:void(autoSendPhrase(null, \":load " + filepartida + "\"))'>cargar</a>] " + filepartida + " (" + dflong.format(file.lastModified()) + ") " + "[<a href=\"" + req.request.getContextPath() + "/" + req.getUsuarioSession().getLogin() + "/" + file.getName() + "?explore\">descargar</a>] " + "[<a href='javascript:void(autoSendPhrase(null, \":delete " + filepartida + "\"))'>borrar</a>]" + "</li>");
            }
            buf.append("</ul>");
            context.getAdvResponse().println(buf.toString());
        }
    }

    public void uploadSavegame(Req req, HttpServletRequest request, HttpServletResponse response, ProyectoBean pro) throws IOException, RuntimeException {
        ServletRequestContext srctxt = new ServletRequestContext(request);
        if (!ServletFileUpload.isMultipartContent(srctxt)) {
            return;
        }
        try {
            if (!req.permJugar()) {
                frameMensaje(request, response, getMensajeErrorJugar(req));
            }
            AdvOgnlscriptContext context = AdvOgnlscriptContext.newContext(req.getUsuarioSession());
            context.setModulo(ModuloMng.getInstance().getSecureAdv(pro));
            Properties fields = new Properties();
            InputStream savegame = null;
            ServletFileUpload servletFileUpload = Config.getMng().getServletFileUpload();
            List items = servletFileUpload.parseRequest(srctxt);
            for (Iterator iterator = items.iterator(); iterator.hasNext(); ) {
                FileItem item = (FileItem) iterator.next();
                if (item.isFormField()) {
                    String name = item.getFieldName();
                    String value = item.getString().trim();
                    fields.put(name, value);
                } else {
                    savegame = item.getInputStream();
                }
            }
            if (savegame != null) {
                try {
                    GameSession gameSession = SaveManager.getInstance().load(context, savegame);
                    if (gameSession.getHash().equals(context.getModulo().getHash())) {
                        frameMensaje(request, response, "NOTA: Cargando nueva partida.", fields.getProperty("uploadid"));
                        context.getAdvResponse().setRefresh(true);
                        req.putGameSessionToSession(context, gameSession, true);
                    } else {
                        frameMensaje(request, response, "Error, la partida es de otra version.", fields.getProperty("uploadid"));
                    }
                } catch (Exception e) {
                    frameMensaje(request, response, "Error al cargar la partida  (" + e + ")");
                }
            } else {
                frameMensaje(request, response, "Partida no subida");
            }
        } catch (Exception e) {
            frameMensaje(request, response, "Error general al subir la partida  (" + e.getMessage() + ")");
        }
    }

    public void frameMensaje(HttpServletRequest request, HttpServletResponse response, String s) throws IOException {
        frameMensaje(request, response, s, null);
    }

    public void frameMensaje(HttpServletRequest request, HttpServletResponse response, String s, String uploadid) throws IOException {
        response.setContentType("text/html");
        response.getWriter().print("<html><body><script>");
        response.getWriter().print("window.parent.appendText('" + s + "');");
        if (uploadid != null) {
            response.getWriter().print("window.parent.refresh();" + "window.parent.Element.remove(\"upload_" + uploadid + "\");");
        }
        response.getWriter().print("</script></body></html>");
    }

    public void mensaje(HttpServletResponse response, String s) throws IOException {
        response.setContentType("text/html");
        response.getWriter().print("<html><body><h1>Error</h1><p>" + s + "</p></body>");
    }
}
