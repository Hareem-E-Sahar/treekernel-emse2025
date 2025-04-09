package com.main;

import java.lang.Comparable;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.Date;
import java.util.LinkedList;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.hibernate.Session;
import org.hibernate.criterion.Projections;
import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.SimpleFormController;
import com.validation.RegisterData;
import com.validation.StolData;
import com.validation.LoginData;
import com.validation.TorData;
import szablony.LTylkoDostepne;
import szablony.Lista;
import szablony.ListaCzasow;
import util.HiberSession;
import util.MD5generator;
import db.Klient;
import db.Lokal;
import db.Rezerwacje;
import db.Stanowisko;
import db.dao.KlientDAO;
import db.dao.LokalDAO;
import db.dao.RezerwacjeDAO;
import db.dao.StanowiskoDAO;
import db.Miejscowosc;
import db.dao.MiejscowoscDAO;

public class TorController extends SimpleFormController {

    Rezerwacje rez;

    protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) throws Exception {
        ModelAndView mv = super.handleRequestInternal(request, response);
        List<Miejscowosc> m = MiejscowoscDAO.getInstance().findAll();
        mv.addObject("miasta", m);
        Cookie[] ct = request.getCookies();
        Integer idRez = null;
        mv.addObject("editMode", true);
        mv.addObject("viewMode", false);
        mv.addObject("addMode", true);
        if (ct != null) for (int i = 0; i < ct.length; i++) {
            Cookie c = request.getCookies()[i];
            if (c.getName().equals("viewMode")) {
                mv.addObject("viewMode", Boolean.parseBoolean(c.getValue()));
                c.setMaxAge(0);
                response.addCookie(c);
            } else if (c.getName().equals("editMode")) {
                mv.addObject("editMode", Boolean.parseBoolean(c.getValue()));
                c.setMaxAge(0);
                response.addCookie(c);
            } else if ((c.getName().equals("rezId"))) {
                idRez = Integer.valueOf(c.getValue());
                c.setMaxAge(0);
                response.addCookie(c);
            }
        }
        if (idRez != null) {
            mv.addObject("addMode", false);
            mv.addObject("rez", rez);
        }
        return mv;
    }

    public Object formBackingObject(HttpServletRequest request) throws ServletException {
        TorData backingObject = new TorData();
        if (request.getParameter("rezIdd") != null && request.getParameter("rezIdd") != "") {
            rez = RezerwacjeDAO.getInstance().load(Integer.valueOf(request.getParameter("rezIdd")), HiberSession.getInstance().getSession());
            DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
            backingObject.setData(rez.getData().toString());
            backingObject.setGodzinaDo("" + rez.getGodzinaDo().getHours());
            backingObject.setGodzinaOd("" + rez.getGodzinaOd().getHours());
        }
        return backingObject;
    }

    public ModelAndView onSubmit(HttpServletRequest request, HttpServletResponse response, Object command, BindException errors) throws Exception {
        TorData sd = (TorData) command;
        ArrayList<Integer> listadostepnych = new ArrayList<Integer>();
        int ilosc;
        String data;
        Date fordata;
        int godzinaOd;
        int godzinaDo;
        String miejscowosc;
        String lokal;
        String stanowisko;
        data = DateFormat.getDateInstance().format(sd.getFordata());
        fordata = sd.getFordata();
        godzinaOd = sd.getFgodzinaOd();
        godzinaDo = sd.getFgodzinaDo();
        miejscowosc = sd.getMiejscowosc();
        lokal = sd.getLokal();
        stanowisko = sd.getStanowisko();
        DateFormat dfgodz = new SimpleDateFormat("HH:mm:ss");
        DateFormat tf = new SimpleDateFormat("HH");
        Lista lgodzinaOd = new Lista();
        Lista lgodzinaDo = new Lista();
        LTylkoDostepne lltd = new LTylkoDostepne();
        String SgodzinaOd = "";
        String SgodzinaDo = "";
        Date gOd;
        int igOd;
        Date gDo;
        int igDo;
        int iterator;
        String q1;
        int flaga = -1;
        Connection con = HiberSession.getInstance().getSession().connection();
        Statement s = null;
        try {
            s = con.createStatement();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        q1 = "SELECT Godzina_OD, Godzina_DO FROM " + "Rezerwacje WHERE " + "IDStanowiska = " + stanowisko + " AND Data = \'" + DateFormat.getDateInstance().format(sd.getFordata()) + "\' ";
        ResultSet rs = null;
        try {
            rs = s.executeQuery(q1);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        if (rs.first() == false) {
            Rezerwacje r = null;
            if (request.getParameter("rezIdd") != null) r = RezerwacjeDAO.getInstance().load(Integer.valueOf(request.getParameter("rezIdd")), HiberSession.getInstance().getSession()); else r = new Rezerwacje();
            r.setData(fordata);
            r.setGodzinaOd(dfgodz.parse(sd.getGodzinaOd() + ":00:00"));
            r.setGodzinaDo(dfgodz.parse(sd.getGodzinaDo() + ":00:00"));
            r.setLokal(LokalDAO.getInstance().load(Integer.parseInt(lokal)));
            r.setStanowisko(StanowiskoDAO.getInstance().load(Integer.parseInt(stanowisko)));
            HttpSession hs = request.getSession();
            Integer idklienta = (Integer) hs.getAttribute("id");
            if (request.getParameter("rezIdd") == null) r.setKlient(KlientDAO.getInstance().load(idklienta));
            r.setRezerwacjaCzasowa("t");
            RezerwacjeDAO.getInstance().saveOrUpdate(r);
            ModelAndView mav = new ModelAndView("pages/rezToru");
            return mav;
        }
        try {
            while (rs.next()) {
                SgodzinaOd = rs.getString(1);
                gOd = tf.parse(SgodzinaOd);
                igOd = gOd.getHours();
                lgodzinaOd.getLista().addLast(igOd);
                SgodzinaDo = rs.getString(2);
                gDo = tf.parse(SgodzinaDo);
                igDo = gDo.getHours();
                lgodzinaDo.getLista().addLast(igDo);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        try {
            rs.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        try {
            s.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        ListaCzasow[] lczasow = new ListaCzasow[24];
        for (int c = 0; c < 24; c++) {
            lczasow[c] = new ListaCzasow();
            lczasow[c].dostepny = true;
        }
        for (int i = 0; i < lgodzinaOd.getLista().size(); i++) {
            for (int j = lgodzinaOd.getLista().get(i); j < lgodzinaDo.getLista().get(i); j++) {
                System.out.println("To jest " + i + " iteracja godzina Od wynosi " + lgodzinaOd.getLista().get(i) + " Godzina Do wynosi " + lgodzinaDo.getLista().get(i));
                lczasow[j].dostepny = false;
            }
        }
        for (int g = 0; g <= 23; g++) {
            if (lczasow[g].dostepny == true) {
                System.out.println("Godzina " + g + " jest dostepna");
                listadostepnych.add(g);
            } else {
                System.out.println("Godzina " + g + " jest niedostepna");
            }
        }
        for (int z = godzinaOd; z < godzinaDo; z++) {
            if (lczasow[z].dostepny == false) {
                flaga = 1;
            }
        }
        if (flaga == 1) {
            System.out.println("Te godziny sa juz zajeta");
            ModelAndView mvc = new ModelAndView("pages/TorBlad");
            return mvc;
        }
        Rezerwacje r = null;
        if (request.getParameter("rezIdd") != null) r = RezerwacjeDAO.getInstance().load(Integer.valueOf(request.getParameter("rezIdd")), HiberSession.getInstance().getSession()); else r = new Rezerwacje();
        r.setData(fordata);
        r.setGodzinaOd(dfgodz.parse(sd.getGodzinaOd() + ":00:00"));
        r.setGodzinaDo(dfgodz.parse(sd.getGodzinaDo() + ":00:00"));
        r.setLokal(LokalDAO.getInstance().load(Integer.parseInt(lokal)));
        r.setStanowisko(StanowiskoDAO.getInstance().load(Integer.parseInt(stanowisko)));
        HttpSession hs = request.getSession();
        Integer idklienta = (Integer) hs.getAttribute("id");
        if (request.getParameter("rezIdd") == null) r.setKlient(KlientDAO.getInstance().load(idklienta));
        r.setRezerwacjaCzasowa("t");
        RezerwacjeDAO.getInstance().saveOrUpdate(r);
        System.out.println("Te godziny sa wolne");
        ModelAndView mav = new ModelAndView("pages/rezToru");
        return mav;
    }
}
