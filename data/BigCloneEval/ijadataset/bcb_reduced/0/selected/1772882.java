package gameslave.dnd35.servlet;

import gameslave.GameslaveStorage;
import gameslave.db.Entity;
import gameslave.dnd35.db.Book;
import gameslave.servlet.GameslaveMultiActionController;
import gameslave.system.GameSystem;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Iterator;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.web.bind.ServletRequestBindingException;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.View;

/**
 * @author Dobes
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class BookController extends GameslaveMultiActionController {

    public GameslaveStorage storage;

    public GameslaveStorage getStorage() {
        return storage;
    }

    public void setStorage(GameslaveStorage storage) {
        this.storage = storage;
    }

    public GameSystem gameSystem;

    public GameSystem getGameSystem() {
        return gameSystem;
    }

    public void setGameSystem(GameSystem gameSystem) {
        this.gameSystem = gameSystem;
    }

    Book getBook(Entity entity) throws ServletRequestBindingException {
        if (entity.getId() == null) throw new ServletRequestBindingException("Must have id");
        return getStorage().getBook(entity.getId());
    }

    Book getBook(HttpServletRequest request) throws ServletException {
        String id = request.getParameter("id");
        if (id == null) throw new ServletRequestBindingException("Must have id");
        return getStorage().getBook(new Integer(id));
    }

    public ModelAndView view(HttpServletRequest request, HttpServletResponse response, Entity entity) throws Exception {
        Book book = getBook(entity);
        if (book == null) return new ModelAndView("error", "error", "No such book");
        return new ModelAndView("book/view", "this", book);
    }

    public ModelAndView export(HttpServletRequest request, HttpServletResponse response, Entity entity) throws Exception {
        final Book book = getBook(entity);
        if (book == null) return new ModelAndView("error", "error", "No such book");
        return new ModelAndView(new View() {

            public void render(Map model, HttpServletRequest request, HttpServletResponse response) throws Exception {
                response.setContentType("text/plain");
                getStorage().exportBook(book, response.getWriter());
            }
        });
    }

    public ModelAndView backup(HttpServletRequest request, HttpServletResponse response, Entity entity) throws Exception {
        return new ModelAndView(new View() {

            public void render(Map model, HttpServletRequest request, HttpServletResponse response) throws Exception {
                response.setContentType("application/zip");
                ZipOutputStream zos = new ZipOutputStream(response.getOutputStream());
                for (Iterator i = storage.getBooks().iterator(); i.hasNext(); ) {
                    Book book = (Book) i.next();
                    String bookFileName = book.getName().replaceAll("[^0-9A-Za-z_.&@()' -]", "_") + ".book.xml";
                    zos.putNextEntry(new ZipEntry(bookFileName));
                    StringWriter sw = new StringWriter();
                    PrintWriter printWriter = new PrintWriter(sw);
                    getStorage().exportBook(book, printWriter);
                    zos.write(sw.toString().getBytes());
                    zos.closeEntry();
                }
                zos.close();
            }
        });
    }

    public ModelAndView create(HttpServletRequest request, HttpServletResponse response) throws Exception {
        Book book = storage.createNewBook("New Book");
        bind(request, book);
        storage.save(book);
        return new ModelAndView("book/edit", "this", book);
    }

    public ModelAndView edit(HttpServletRequest request, HttpServletResponse response, Entity entity) throws Exception {
        Book book = getBook(entity);
        if (book == null) return new ModelAndView("error", "error", "No such book");
        return new ModelAndView("book/edit", "this", book);
    }

    public ModelAndView save(HttpServletRequest request, HttpServletResponse response, String successView) throws Exception {
        Book book = getBook(request);
        if (book == null) return new ModelAndView("error", "error", "No such book");
        bind(request, book);
        request.getSession(true).setAttribute("defaultBookId", book.getId());
        response.sendRedirect(successView + "?id=" + book.getId());
        return null;
    }

    public ModelAndView save(HttpServletRequest request, HttpServletResponse response) throws Exception {
        return save(request, response, "view");
    }

    public ModelAndView saveAndView(HttpServletRequest request, HttpServletResponse response) throws Exception {
        return save(request, response, "view");
    }

    public ModelAndView saveAndEdit(HttpServletRequest request, HttpServletResponse response) throws Exception {
        return save(request, response, "edit");
    }

    public ModelAndView delete(HttpServletRequest request, HttpServletResponse response, Entity entity) throws Exception {
        Book book = getBook(entity);
        if (book == null) return new ModelAndView("error", "error", "No such book");
        storage.deleteBook(book);
        return new ModelAndView("book/deleted", "this", book);
    }

    public ModelAndView books(HttpServletRequest request, HttpServletResponse response) throws Exception {
        return new ModelAndView("book/list");
    }
}
