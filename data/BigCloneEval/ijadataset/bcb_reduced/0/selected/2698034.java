package org.nodevision.portal.struts.pages;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.struts.action.Action;
import org.apache.struts.action.ActionError;
import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.nodevision.portal.struts.pages.forms.PagesForm;
import com.fgm.web.menu.MenuComponent;
import com.fgm.web.menu.MenuRepository;

public class Pages extends Action {

    public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
        PagesForm pagesform = (PagesForm) form;
        createRepository(request);
        String prefix = getServlet().getServletContext().getRealPath("/html");
        if (request.getParameter("showpage") != null) {
            String showPage = request.getParameter("showpage");
            if (!showPage.startsWith(prefix)) {
                return mapping.getInputForward();
            }
            StringBuffer content = new StringBuffer();
            pagesform.setPage(showPage);
            try {
                BufferedReader in = new BufferedReader(new FileReader(showPage));
                String str;
                while ((str = in.readLine()) != null) {
                    content.append(str);
                    content.append("\r\n");
                }
                in.close();
                pagesform.setPageContent(content.toString());
            } catch (IOException e) {
                pagesform.setPageContent(e.toString());
            }
        }
        if (request.getParameter("folder") != null) {
            if (!request.getParameter("folder").startsWith(prefix)) {
                return mapping.getInputForward();
            }
            pagesform.setFolderName(request.getParameter("folder"));
        }
        if ("create".equalsIgnoreCase(request.getParameter("action"))) {
            if (!pagesform.getFolderName().startsWith(prefix)) {
                return mapping.getInputForward();
            }
            ActionErrors errors = new ActionErrors();
            if (!pagesform.getCreateFileName().equalsIgnoreCase("")) {
                try {
                    File newFile = new File(new File(pagesform.getFolderName()), pagesform.getCreateFileName());
                    newFile.createNewFile();
                    errors.add(ActionErrors.GLOBAL_MESSAGE, new ActionError("pages.created", pagesform.getCreateFileName()));
                } catch (Exception e) {
                    errors.add(ActionErrors.GLOBAL_ERROR, new ActionError("pages.error", e.toString()));
                }
            }
            if (!pagesform.getCreateFolderName().equalsIgnoreCase("")) {
                try {
                    File newFile = new File(new File(pagesform.getFolderName()), pagesform.getCreateFolderName());
                    newFile.mkdir();
                    errors.add(ActionErrors.GLOBAL_MESSAGE, new ActionError("pages.created", pagesform.getCreateFolderName()));
                } catch (Exception e) {
                    errors.add(ActionErrors.GLOBAL_ERROR, new ActionError("pages.error", e.toString()));
                }
            }
            createRepository(request);
            saveErrors(request, errors);
        }
        if ("saveFile".equalsIgnoreCase(request.getParameter("action"))) {
            if (!pagesform.getPage().startsWith(prefix)) {
                return mapping.getInputForward();
            }
            ActionErrors errors = new ActionErrors();
            try {
                BufferedWriter out = new BufferedWriter(new FileWriter(pagesform.getPage()));
                out.write(pagesform.getPageContent());
                out.close();
                errors.add(ActionErrors.GLOBAL_MESSAGE, new ActionError("pages.saved"));
            } catch (Exception e) {
                errors.add(ActionErrors.GLOBAL_ERROR, new ActionError("pages.error", e.toString()));
            }
            saveErrors(request, errors);
        }
        if ("deleteFile".equalsIgnoreCase(request.getParameter("action"))) {
            if (!pagesform.getPage().startsWith(prefix)) {
                return mapping.getInputForward();
            }
            ActionErrors errors = new ActionErrors();
            try {
                new File(pagesform.getPage()).delete();
                errors.add(ActionErrors.GLOBAL_MESSAGE, new ActionError("pages.deleted"));
            } catch (Exception e) {
                errors.add(ActionErrors.GLOBAL_ERROR, new ActionError("pages.error", e.toString()));
            }
            createRepository(request);
            saveErrors(request, errors);
        }
        if ("deleteFolder".equalsIgnoreCase(request.getParameter("action"))) {
            if (!pagesform.getFolderName().startsWith(prefix)) {
                return mapping.getInputForward();
            }
            ActionErrors errors = new ActionErrors();
            try {
                deleteSub(new File(pagesform.getFolderName()));
                errors.add(ActionErrors.GLOBAL_MESSAGE, new ActionError("pages.deleted"));
            } catch (Exception e) {
                errors.add(ActionErrors.GLOBAL_ERROR, new ActionError("pages.error", e.toString()));
            }
            createRepository(request);
            saveErrors(request, errors);
        }
        return mapping.getInputForward();
    }

    private void createMenu(MenuComponent comp, File path) {
        if (!path.isDirectory()) {
            MenuComponent tempcmp = new MenuComponent();
            tempcmp.setTitle(path.getName());
            tempcmp.setImage("document.gif");
            tempcmp.setLocation("?showpage=" + path.getAbsolutePath());
            comp.addMenuComponent(tempcmp);
        } else {
            File[] files = path.listFiles();
            MenuComponent tempcmp = new MenuComponent();
            tempcmp.setTitle(path.getName());
            tempcmp.setImage("folder.gif");
            tempcmp.setLocation("?folder=" + path.getAbsolutePath());
            comp.addMenuComponent(tempcmp);
            for (int i = 0; i < files.length; ++i) {
                createMenu(tempcmp, files[i]);
            }
        }
    }

    private void createRepository(HttpServletRequest request) {
        MenuComponent lc_menu1 = new MenuComponent();
        lc_menu1.setName("html");
        MenuRepository rep = new MenuRepository();
        rep.addMenu(lc_menu1);
        createMenu(lc_menu1, new File(getServlet().getServletContext().getRealPath("/html")));
        request.getSession().setAttribute(MenuRepository.MENU_REPOSITORY_KEY, rep);
    }

    public void deleteSub(File path) throws Exception {
        if (!path.isDirectory()) {
            path.delete();
        } else {
            File[] files = path.listFiles();
            for (int i = 0; i < files.length; ++i) {
                if (files[i].isDirectory()) deleteSub(files[i]);
                files[i].delete();
            }
            path.delete();
        }
    }
}
