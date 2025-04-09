package es.caib.regweb.logic.ejb;

import java.util.*;
import java.text.*;
import org.hibernate.Hibernate;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.hibernate.ScrollableResults;
import org.hibernate.ScrollMode;
import javax.ejb.*;
import java.lang.reflect.InvocationTargetException;
import es.caib.regweb.logic.helper.Helper;
import es.caib.regweb.logic.helper.ParametrosRegistroEntrada;
import es.caib.regweb.logic.interfaces.RegistroEntradaFacade;
import es.caib.regweb.logic.util.RegistroEntradaFacadeUtil;
import es.caib.regweb.logic.interfaces.ValoresFacade;
import es.caib.regweb.logic.util.ValoresFacadeUtil;
import org.apache.log4j.Logger;
import es.caib.regweb.model.LogEntradaLopd;
import es.caib.regweb.model.LogEntradaLopdId;
import es.caib.regweb.model.LogModificacionLopd;
import es.caib.regweb.model.LogModificacionLopdId;
import es.caib.regweb.logic.helper.RegistroModificadoSeleccionado;
import es.caib.regweb.logic.helper.ParametrosRegistroModificado;

/**
 * SessionBean per a manteniment de registres modificats d'entrada
 *
 * @ejb.bean
 *  name="logic/RegistroModificadoEntradaFacade"
 *  jndi-name="es.caib.regweb.logic.RegistroModificadoEntradaFacade"
 *  type="Stateless"
 *  view-type="remote"
 *  transaction-type="Container"
 *
 */
public abstract class RegistroModificadoEntradaFacadeEJB extends HibernateEJB {

    private Logger log = Logger.getLogger(this.getClass());

    private SessionContext sessioEjb;

    private static final String TIPO_REGISTRO = "E";

    public void setSessionContext(SessionContext ctx) {
        sessioEjb = ctx;
    }

    /**
    * @ejb.interface-method
    * @ejb.permission unchecked="true"
    */
    public ParametrosRegistroModificado leer(ParametrosRegistroModificado param) {
        Session session = getSession();
        ScrollableResults rs = null;
        SQLQuery q = null;
        ParametrosRegistroModificado res = new ParametrosRegistroModificado();
        try {
            String sentenciaHql = "SELECT FZJANOEN, FZJCENTI, FZJNENTI, FZJCONEN, FZJCUSMO, FZJCUSVI, " + " FZJIREMI, FZJIEXTR, FZJNUMEN, FZJREMIT, FZJTEXTO, FZJCAGCO FROM BZMODIF " + " WHERE FZJCENSA='E' AND FZJCAGCO=? AND FZJNUMEN=? AND FZJANOEN=? AND" + " FZJFMODI=? AND FZJHMODI=?";
            q = session.createSQLQuery(sentenciaHql);
            q.addScalar("FZJANOEN", Hibernate.INTEGER);
            q.addScalar("FZJCENTI", Hibernate.STRING);
            q.addScalar("FZJNENTI", Hibernate.INTEGER);
            q.addScalar("FZJCONEN", Hibernate.STRING);
            q.addScalar("FZJCUSMO", Hibernate.STRING);
            q.addScalar("FZJCUSVI", Hibernate.STRING);
            q.addScalar("FZJIREMI", Hibernate.STRING);
            q.addScalar("FZJIEXTR", Hibernate.STRING);
            q.addScalar("FZJNUMEN", Hibernate.INTEGER);
            q.addScalar("FZJREMIT", Hibernate.STRING);
            q.addScalar("FZJTEXTO", Hibernate.STRING);
            q.addScalar("FZJCAGCO", Hibernate.INTEGER);
            q.setInteger(0, param.getOficina());
            q.setInteger(1, param.getNumeroRegistro());
            q.setInteger(2, param.getAnoEntrada());
            q.setInteger(3, Integer.valueOf(param.getFechaModificacion()));
            q.setInteger(4, Integer.valueOf(param.getHoraModificacion()));
            rs = q.scroll();
            if (rs.next()) {
                res.setLeido(true);
                res.setAnoEntrada(rs.getInteger(0));
                res.setEntidad1(rs.getString(1));
                res.setEntidad2(rs.getInteger(2));
                res.setEntidad1Catalan(Helper.convierteEntidad(res.getEntidad1(), session));
                res.setExtracto(rs.getString(3));
                res.setUsuarioModificacion(rs.getString(4));
                res.setUsuarioVisado(rs.getString(5));
                res.setIndVisRemitente(rs.getString(6));
                res.setIndVisExtracto(rs.getString(7));
                res.setNumeroRegistro(rs.getInteger(8));
                res.setRemitente(rs.getString(9));
                res.setMotivo(rs.getString(10));
                res.setOficina(rs.getInteger(11));
                if (!(rs.getString(6).equals("") || rs.getString(6).equals(" "))) {
                    res.setEntidad1("");
                    res.setEntidad1Catalan("");
                    res.setEntidad2(0);
                    res.setRemitente("");
                }
                if (!(rs.getString(7).equals("") || rs.getString(7).equals(" "))) {
                    res.setExtracto("");
                }
                Date fechaSystem = new Date();
                DateFormat aaaammdd = new SimpleDateFormat("yyyyMMdd");
                DateFormat hhmmss = new SimpleDateFormat("HHmmss");
                DateFormat sss = new SimpleDateFormat("S");
                String ss = sss.format(fechaSystem);
                int fzahsis = Integer.parseInt(hhmmss.format(fechaSystem) + ss);
                String Stringsss = sss.format(fechaSystem);
                switch(Stringsss.length()) {
                    case (1):
                        Stringsss = "00" + Stringsss;
                        break;
                    case (2):
                        Stringsss = "0" + Stringsss;
                        break;
                }
                int horamili = Integer.parseInt(hhmmss.format(fechaSystem) + Stringsss);
                int fzafsis = Integer.parseInt(aaaammdd.format(fechaSystem));
                logLopdBZMODIF("SELECT", sessioEjb.getCallerPrincipal().getName().toUpperCase(), fzafsis, horamili, 'E', param.getNumeroRegistro(), param.getAnoEntrada(), param.getOficina(), Integer.parseInt(param.getFechaModificacion()), Integer.parseInt(param.getHoraModificacion()));
            }
        } catch (Exception e) {
            log.error("Error: " + e.getMessage());
            e.printStackTrace();
            res.setLeido(false);
        } finally {
            if (rs != null) rs.close();
            close(session);
        }
        return res;
    }

    /**
    * @ejb.interface-method
    * @ejb.permission unchecked="true"
    */
    public ParametrosRegistroModificado visar(ParametrosRegistroModificado param) throws HibernateException, ClassNotFoundException, Exception {
        Session session = getSession();
        SQLQuery q = null;
        Date fechaSystem = new Date();
        DateFormat aaaammdd = new SimpleDateFormat("yyyyMMdd");
        DateFormat hhmmss = new SimpleDateFormat("HHmmss");
        DateFormat sss = new SimpleDateFormat("S");
        String ss = sss.format(fechaSystem);
        if (ss.length() > 2) {
            ss = ss.substring(0, 2);
        }
        boolean visado = false;
        try {
            String sentenciaHql = "UPDATE BZMODIF SET FZJCUSVI=?, FZJFVISA=?, FZJHVISA=?" + ((param.isHayVisadoExtracto()) ? ", FZJIEXTR=?" : "") + ((param.isHayVisadoRemitente()) ? ", FZJIREMI=?" : "") + " WHERE FZJCENSA='E' AND FZJCAGCO=? AND FZJNUMEN=? AND FZJANOEN=? AND FZJFMODI=? AND FZJHMODI=?";
            q = session.createSQLQuery(sentenciaHql);
            q.setString(0, param.getUsuarioVisado());
            q.setInteger(1, Integer.parseInt(aaaammdd.format(fechaSystem)));
            q.setInteger(2, Integer.parseInt(hhmmss.format(fechaSystem) + ss));
            int contador = 3;
            if (param.isHayVisadoExtracto()) {
                q.setString(contador++, "X");
            }
            if (param.isHayVisadoRemitente()) {
                q.setString(contador++, "X");
            }
            q.setInteger(contador++, param.getOficina());
            q.setInteger(contador++, param.getNumeroRegistro());
            q.setInteger(contador++, param.getAnoEntrada());
            q.setInteger(contador++, Integer.parseInt(param.getFechaModificacion()));
            q.setInteger(contador++, Integer.parseInt(param.getHoraModificacion()));
            int registrosAfectados = q.executeUpdate();
            if (registrosAfectados > 0 && !param.isHayVisadoExtracto() && !param.isHayVisadoRemitente()) {
                visado = true;
            }
            if (registrosAfectados > 0 && (param.isHayVisadoExtracto() || param.isHayVisadoRemitente())) {
                boolean generado = generarBZVISAD(param, session, Integer.parseInt(aaaammdd.format(fechaSystem)), Integer.parseInt(hhmmss.format(fechaSystem) + ss));
                if (generado) {
                    visado = actualizarBZENTRA(param, session);
                }
                String rem = "";
                String com = "";
                if (param.isHayVisadoRemitente()) {
                    if (!param.getRemitente().trim().equals("")) {
                        rem = param.getRemitente();
                    } else {
                        ValoresFacade valor = ValoresFacadeUtil.getHome().create();
                        rem = valor.recuperaRemitenteCastellano(param.getEntidad1(), param.getEntidad2() + "");
                    }
                } else {
                    if (!param.getAltres().trim().equals("")) {
                        rem = param.getRemitente();
                    } else {
                        ValoresFacade valor = ValoresFacadeUtil.getHome().create();
                        rem = valor.recuperaRemitenteCastellano(param.getEntidad1Old(), param.getEntidad2Old() + "");
                    }
                }
                if (param.isHayVisadoExtracto()) {
                    com = param.getExtracto();
                } else {
                    com = param.getComentario();
                }
                try {
                    Class t = Class.forName("es.caib.regweb.module.PluginHook");
                    Class[] partypes = { String.class, Integer.class, Integer.class, Integer.class, Integer.class, String.class, String.class, String.class, Integer.class, Integer.class, String.class, Integer.class, String.class, String.class, Integer.class, Integer.class, Integer.class, String.class, String.class, String.class };
                    Object[] params = { "V", new Integer(param.getAnoEntrada()), new Integer(param.getNumeroRegistro()), new Integer(param.getOficina()), new Integer(param.getFechaDocumento()), rem, com, param.getTipoDocumento(), new Integer(param.getFechaRegistro()), new Integer(param.getFzacagge()), param.getFora(), new Integer(param.getDestinatario()), param.getIdioma(), null, null, null, null, null, null, null };
                    java.lang.reflect.Method metodo = t.getMethod("entrada", partypes);
                    metodo.invoke(null, params);
                } catch (IllegalAccessException iae) {
                } catch (IllegalArgumentException iae) {
                } catch (InvocationTargetException ite) {
                } catch (NullPointerException npe) {
                } catch (ExceptionInInitializerError eiie) {
                } catch (NoSuchMethodException nsme) {
                } catch (SecurityException se) {
                } catch (LinkageError le) {
                } catch (ClassNotFoundException le) {
                }
            }
            int fzahsis = Integer.parseInt(hhmmss.format(fechaSystem) + ss);
            String Stringsss = sss.format(fechaSystem);
            switch(Stringsss.length()) {
                case (1):
                    Stringsss = "00" + Stringsss;
                    break;
                case (2):
                    Stringsss = "0" + Stringsss;
                    break;
            }
            int horamili = Integer.parseInt(hhmmss.format(fechaSystem) + Stringsss);
            int fzafsis = Integer.parseInt(aaaammdd.format(fechaSystem));
            logLopdBZMODIF("UPDATE", sessioEjb.getCallerPrincipal().getName().toUpperCase(), fzafsis, horamili, 'E', param.getNumeroRegistro(), param.getAnoEntrada(), param.getOficina(), Integer.parseInt(param.getFechaModificacion()), Integer.parseInt(param.getHoraModificacion()));
            session.flush();
        } catch (Exception e) {
            log.error("Error: " + e.getMessage());
            e.printStackTrace();
            visado = false;
        } finally {
            close(session);
        }
        param.setVisado(visado);
        return param;
    }

    private boolean actualizarBZENTRA(ParametrosRegistroModificado param, Session session) throws HibernateException, ClassNotFoundException, Exception {
        boolean generado = false;
        SQLQuery q = null;
        Date fechaSystem = new Date();
        DateFormat hhmmss = new SimpleDateFormat("HHmmss");
        DateFormat aaaammdd = new SimpleDateFormat("yyyyMMdd");
        String entidad1Valor = "";
        int entidad2Valor = 0;
        String remitenteValor = "";
        if (param.isHayVisadoRemitente()) {
            if (param.getEntidad1().trim().equals("")) {
                remitenteValor = param.getRemitente();
                entidad1Valor = " ";
                entidad2Valor = 0;
            } else {
                remitenteValor = "";
                entidad1Valor = param.getEntidad1();
                entidad2Valor = param.getEntidad2();
            }
        }
        String actualizaBZENTRA = "UPDATE BZENTRA SET FZAFACTU=? " + ((param.isHayVisadoExtracto() && param.getIdiomaExtracto().equals("1")) ? ", FZACONEN=?" : "") + ((param.isHayVisadoExtracto() && !param.getIdiomaExtracto().equals("1")) ? ", FZACONE2=?" : "") + ((param.isHayVisadoRemitente()) ? ", FZAREMIT=?, FZACENTI=?, FZANENTI=?" : "") + " WHERE FZANUMEN=? AND FZACAGCO=? AND FZAANOEN=?";
        try {
            q = session.createSQLQuery(actualizaBZENTRA);
            int contador = 0;
            q.setInteger(contador++, Integer.parseInt(aaaammdd.format(fechaSystem)));
            if (param.isHayVisadoExtracto()) {
                q.setString(contador++, param.getExtracto());
            }
            if (param.isHayVisadoRemitente()) {
                q.setString(contador++, remitenteValor);
                q.setString(contador++, entidad1Valor);
                q.setInteger(contador++, entidad2Valor);
            }
            q.setInteger(contador++, param.getNumeroRegistro());
            q.setInteger(contador++, param.getOficina());
            q.setInteger(contador++, param.getAnoEntrada());
            int registrosAfectados = q.executeUpdate();
            generado = (registrosAfectados > 0) ? true : false;
            DateFormat sss = new SimpleDateFormat("S");
            String ss = sss.format(fechaSystem);
            int fzahsis = Integer.parseInt(hhmmss.format(fechaSystem) + ss);
            String Stringsss = sss.format(fechaSystem);
            switch(Stringsss.length()) {
                case (1):
                    Stringsss = "00" + Stringsss;
                    break;
                case (2):
                    Stringsss = "0" + Stringsss;
                    break;
            }
            int horamili = Integer.parseInt(hhmmss.format(fechaSystem) + Stringsss);
            int fzafsis = Integer.parseInt(aaaammdd.format(fechaSystem));
            logLopdBZENTRA("UPDATE", param.getUsuarioModificacion(), fzafsis, horamili, param.getNumeroRegistro(), param.getAnoEntrada(), param.getOficina());
        } catch (Exception e) {
            log.error("Error: " + e.getMessage());
            generado = false;
            e.printStackTrace();
            throw new Exception("S'ha produ\357t un error actualizant BZENTRA");
        }
        return generado;
    }

    private boolean generarBZVISAD(ParametrosRegistroModificado param, Session session, int fecha, int hora) throws HibernateException, ClassNotFoundException, Exception {
        boolean generado = false;
        SQLQuery q = null;
        String insertBZVISAD = "INSERT INTO BZVISAD (FZKANOEN, FZKCAGCO, FZKCENSA, FZKCENTF, FZKCENTI, FZKNENTF, FZKNENTI, " + "FZKREMIF, FZKREMII, FZKCONEF, FZKCONEI, FZKCUSVI, FZKFENTF, FZKFENTI, FZKFVISA, FZKHVISA,  FZKNUMEN, " + "FZKTEXTO) VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
        try {
            RegistroEntradaFacade regent = RegistroEntradaFacadeUtil.getHome().create();
            ParametrosRegistroEntrada registro = new ParametrosRegistroEntrada();
            registro.fijaUsuario(param.getUsuarioVisado());
            registro.setoficina(param.getOficina() + "");
            registro.setNumeroEntrada(param.getNumeroRegistro() + "");
            registro.setAnoEntrada(param.getAnoEntrada() + "");
            registro = regent.leer(registro);
            if (registro == null) {
                throw new Exception("S'ha produ\357t un error i no s'han pogut crear el objecte RegistroEntrada");
            }
            if (registro.getData().equals("0")) {
                param.setFechaDocumento(0);
            } else {
                java.util.Date fechaTest = null;
                DateFormat yyyymmdd = new SimpleDateFormat("yyyyMMdd");
                DateFormat ddmmyyyy = new SimpleDateFormat("dd/MM/yyyy");
                fechaTest = ddmmyyyy.parse(registro.getData());
                param.setFechaDocumento(Integer.parseInt(yyyymmdd.format(fechaTest)));
            }
            if (registro.getDataEntrada().equals("0")) {
                param.setFechaRegistro(0);
            } else {
                java.util.Date fechaTest = null;
                DateFormat yyyymmdd = new SimpleDateFormat("yyyyMMdd");
                DateFormat ddmmyyyy = new SimpleDateFormat("dd/MM/yyyy");
                fechaTest = ddmmyyyy.parse(registro.getDataEntrada());
                param.setFechaRegistro(Integer.parseInt(yyyymmdd.format(fechaTest)));
            }
            param.setTipoDocumento(registro.getTipo());
            param.setFzacagge(Integer.parseInt(registro.getBalears()));
            param.setIdiomaExtracto(registro.getIdioex());
            param.setFora(registro.getFora());
            param.setDestinatario(Integer.parseInt(registro.getDestinatari()));
            param.setComentario(registro.getComentario());
            param.setIdioma(registro.getIdioma());
            param.setAltres(registro.getAltres());
            param.setEntidad1Old(registro.getEntidad1());
            param.setEntidad2Old(Integer.parseInt(registro.getEntidad2()));
            q = session.createSQLQuery(insertBZVISAD);
            q.setInteger(0, param.getAnoEntrada());
            q.setInteger(1, param.getOficina());
            q.setString(2, TIPO_REGISTRO);
            q.setString(3, (param.isHayVisadoRemitente()) ? param.getEntidad1() : " ");
            q.setString(4, (param.isHayVisadoRemitente()) ? registro.getEntidad1Grabada() : " ");
            q.setInteger(5, (param.isHayVisadoRemitente()) ? param.getEntidad2() : 0);
            q.setInteger(6, (param.isHayVisadoRemitente()) ? Integer.parseInt(registro.getEntidad2()) : 0);
            q.setString(7, (param.isHayVisadoRemitente()) ? param.getRemitente() : " ");
            q.setString(8, (param.isHayVisadoRemitente()) ? registro.getAltres() : " ");
            q.setString(9, (param.isHayVisadoExtracto()) ? param.getExtracto() : " ");
            q.setString(10, (param.isHayVisadoExtracto()) ? registro.getComentario() : " ");
            q.setString(11, param.getUsuarioVisado());
            q.setInteger(12, 0);
            q.setInteger(13, 0);
            q.setInteger(14, fecha);
            q.setInteger(15, hora);
            q.setInteger(16, param.getNumeroRegistro());
            q.setString(17, param.getMotivo());
            q.executeUpdate();
            generado = true;
            log.debug("generarBZVISAD finalizado correctamente.");
        } catch (Exception e) {
            generado = false;
            log.error("Error: " + e.getMessage());
            e.printStackTrace();
            throw new Exception("S'ha produ\357t un error insert BZVISAD");
        }
        return generado;
    }

    /**
    * @ejb.interface-method
    * @ejb.permission unchecked="true"
    */
    public List recuperarRegistros(String oficina, String usuario) {
        List registros = new ArrayList();
        Session session = getSession();
        ScrollableResults rs = null;
        SQLQuery q = null;
        String sentenciaHql = "";
        String fecha = "";
        java.util.Date fechaDocumento = null;
        DateFormat yyyymmdd = new SimpleDateFormat("yyyyMMdd");
        DateFormat ddmmyyyy = new SimpleDateFormat("dd/MM/yyyy");
        if (oficina.equals("00")) {
            sentenciaHql = "SELECT FZJCAGCO, FZJNUMEN, FZJANOEN, FZJCENTI, FZJREMIT, FZJIREMI, " + " FZJCONEN, FZJIEXTR, FZJFMODI, FZJHMODI, FZJTEXTO FROM BZMODIF " + " WHERE (FZJIEXTR=' ' OR FZJIREMI=' ' OR FZJIEXTR='' OR FZJIREMI='') AND FZJFVISA=0 AND FZJCAGCO " + " IN (SELECT FZHCAGCO FROM BZAUTOR WHERE FZHCUSU=? AND FZHCAUT=?) AND FZJCENSA='E' ORDER BY " + " FZJCAGCO, FZJANOEN, FZJNUMEN, FZJFMODI, FZJHMODI";
        } else {
            sentenciaHql = "SELECT FZJCAGCO, FZJNUMEN, FZJANOEN, FZJCENTI, FZJREMIT, FZJIREMI, " + " FZJCONEN, FZJIEXTR, FZJFMODI, FZJHMODI, FZJTEXTO FROM BZMODIF " + " WHERE FZJCAGCO=? AND FZJFVISA=0 AND (FZJIEXTR=' ' OR FZJIREMI=' ' OR FZJIEXTR='' OR FZJIREMI='') AND FZJCENSA='E' ORDER BY " + " FZJCAGCO, FZJANOEN, FZJNUMEN, FZJFMODI, FZJHMODI";
        }
        try {
            q = session.createSQLQuery(sentenciaHql);
            q.addScalar("FZJCAGCO", Hibernate.INTEGER);
            q.addScalar("FZJNUMEN", Hibernate.INTEGER);
            q.addScalar("FZJANOEN", Hibernate.INTEGER);
            q.addScalar("FZJCENTI", Hibernate.STRING);
            q.addScalar("FZJREMIT", Hibernate.STRING);
            q.addScalar("FZJIREMI", Hibernate.STRING);
            q.addScalar("FZJCONEN", Hibernate.STRING);
            q.addScalar("FZJIEXTR", Hibernate.STRING);
            ;
            q.addScalar("FZJFMODI", Hibernate.INTEGER);
            q.addScalar("FZJHMODI", Hibernate.INTEGER);
            q.addScalar("FZJTEXTO", Hibernate.STRING);
            if (oficina.equals("00")) {
                q.setString(0, usuario);
                q.setString(1, "VE");
            } else {
                q.setInteger(0, Integer.parseInt(oficina));
            }
            rs = q.scroll();
            while (rs.next()) {
                RegistroModificadoSeleccionado reg = new RegistroModificadoSeleccionado();
                reg.setNumeroOficina(rs.getInteger(0));
                reg.setNumeroRegistro(rs.getInteger(1));
                reg.setAnoRegistro(rs.getInteger(2));
                if ((!rs.getString(3).trim().equals("") || !rs.getString(4).trim().equals("")) && (rs.getString(5).equals(" ") || rs.getString(5).equals(""))) {
                    reg.setVisadoR("*");
                } else {
                    reg.setVisadoR("");
                }
                if (!rs.getString(6).trim().equals("") && (rs.getString(7).equals(" ") || rs.getString(7).equals(""))) {
                    reg.setVisadoC("*");
                } else {
                    reg.setVisadoC("");
                }
                fecha = String.valueOf(rs.getInteger(8));
                reg.setFechaModif(rs.getInteger(8));
                reg.setHoraModif(rs.getInteger(9));
                try {
                    fechaDocumento = yyyymmdd.parse(fecha);
                    reg.setFechaModificacion(ddmmyyyy.format(fechaDocumento));
                } catch (Exception e) {
                    reg.setFechaModificacion(fecha);
                }
                reg.setMotivoCambio(rs.getString(10));
                if (((rs.getString(5).equals(" ") || rs.getString(5).equals("")) && (!rs.getString(3).trim().equals("") || !rs.getString(4).trim().equals(""))) || ((rs.getString(7).equals(" ") || rs.getString(7).equals("")) && !rs.getString(6).trim().equals(""))) {
                    registros.add(reg);
                }
            }
            log.debug("recuperarRegistros ejecutado correctamente.");
        } catch (Exception e) {
            log.error("Error: " + e.getMessage());
            e.printStackTrace();
        } finally {
            if (rs != null) rs.close();
            close(session);
        }
        return registros;
    }

    /**
    * @ejb.interface-method
    * @ejb.permission unchecked="true"
    */
    public boolean generarModificacion(ParametrosRegistroModificado param, Session session) {
        boolean generado = false;
        SQLQuery q = null;
        String insertBZMODIF = "INSERT INTO BZMODIF (FZJANOEN, FZJCAGCO, FZJCENSA, FZJCENTI, FZJCONEN, FZJCUSMO, " + "FZJCUSVI, FZJFMODI, FZJFVISA, FZJHMODI, FZJHVISA, FZJIEXTR, FZJIREMI, FZJNENTI, FZJNUMEN, FZJREMIT, " + "FZJTEXTO) VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
        Date fechaSystem = new Date();
        DateFormat aaaammdd = new SimpleDateFormat("yyyyMMdd");
        DateFormat hhmmss = new SimpleDateFormat("HHmmss");
        DateFormat sss = new SimpleDateFormat("S");
        String ss = sss.format(fechaSystem);
        if (ss.length() > 2) {
            ss = ss.substring(0, 2);
        }
        try {
            q = session.createSQLQuery(insertBZMODIF);
            q.setInteger(0, param.getAnoEntrada());
            q.setInteger(1, param.getOficina());
            q.setString(2, TIPO_REGISTRO);
            q.setString(3, param.getEntidad1());
            q.setString(4, param.getExtracto());
            q.setString(5, param.getUsuarioModificacion());
            q.setString(6, "");
            q.setInteger(7, Integer.parseInt(aaaammdd.format(fechaSystem)));
            q.setInteger(8, 0);
            q.setInteger(9, Integer.parseInt(hhmmss.format(fechaSystem) + ss));
            q.setInteger(10, 0);
            q.setString(11, "");
            q.setString(12, "");
            q.setInteger(13, param.getEntidad2());
            q.setInteger(14, param.getNumeroRegistro());
            q.setString(15, param.getRemitente());
            q.setString(16, param.getMotivo());
            q.executeUpdate();
            String Stringsss = sss.format(fechaSystem);
            switch(Stringsss.length()) {
                case (1):
                    Stringsss = "00" + Stringsss;
                    break;
                case (2):
                    Stringsss = "0" + Stringsss;
                    break;
            }
            int horamili = Integer.parseInt(hhmmss.format(fechaSystem) + Stringsss);
            logLopdBZMODIF("INSERT", param.getUsuarioModificacion(), Integer.parseInt(aaaammdd.format(fechaSystem)), horamili, 'E', param.getNumeroRegistro(), param.getAnoEntrada(), param.getOficina(), Integer.parseInt(aaaammdd.format(fechaSystem)), Integer.parseInt(hhmmss.format(fechaSystem) + ss));
            log.debug("generarModificacion ejecutado correctamente.");
            session.flush();
            generado = true;
        } catch (Exception e) {
            log.error("RegistroModificadoEntradaBean: Excepción al generar modificacion " + e.getMessage());
            generado = false;
        } finally {
        }
        return generado;
    }

    /**
     * @ejb.create-method
     * @ejb.permission unchecked="true"
     */
    public void ejbCreate() throws CreateException {
        super.ejbCreate();
    }

    /**
	 * Emplena la taula de control d'accés complint la llei LOPD per la taula BZMODIF 
	 * @param tipusAcces <code>String</code> tipus d'accés a la taula
	 * @param usuari <code>String</code> codi de l'usuari que fa l'acció.
	 * @param data <code>Intr</code> data d'accés en format numèric (ddmmyyyy)
	 * @param hora <code>Int</code> hora d'accés en format numèric (hhmissmis, hora (2 posicions), minut (2 posicions), segons (2 posicions), milisegons (3 posicions)
	 * @param entrsal <code>char</code> Caràcter que indica si és una entrada o una sortida.
	 * @param nombreRegistre <code>Int</code> nombre de registre
	 * @param any <code>Int</code> any del registre
	 * @param oficina <code>Int</code> oficina on s'ha registrat
	 * @author Sebastià Matas Riera (bitel)
	 */
    private void logLopdBZMODIF(String tipusAcces, String usuari, int data, int hora, char entrsal, int nombreRegistre, int any, int oficina, int dataModif, int horaModif) {
        Session session = getSession();
        try {
            LogModificacionLopd entradaLog = new LogModificacionLopd(new LogModificacionLopdId(tipusAcces, usuari, Integer.valueOf(data), Integer.valueOf(hora), String.valueOf(entrsal), Integer.valueOf(nombreRegistre), Integer.valueOf(any), Integer.valueOf(oficina), Integer.valueOf(dataModif), Integer.valueOf(horaModif)));
            session.save(entradaLog);
            session.flush();
            log.debug("logLopdBZMODIF ejecutado correctamente.");
        } catch (HibernateException he) {
            throw new EJBException(he);
        } finally {
            close(session);
        }
    }

    /**
     * Emplena la taula de control d'accés complint la llei LOPD per la taula BZENTRA
     * @param tipusAcces <code>String</code> tipus d'accés a la taula
     * @param usuari <code>String</code> codi de l'usuari que fa l'acció.
     * @param data <code>Intr</code> data d'accés en format numèric (ddmmyyyy)
     * @param hora <code>Int</code> hora d'accés en format numèric (hhmissmis, hora (2 posicions), minut (2 posicions), segons (2 posicions), milisegons (3 posicions)
     * @param nombreRegistre <code>Int</code> nombre de registre
     * @param any <code>Int</code> any del registre
     * @param oficina <code>Int</code> oficina on s'ha registrat
     * @author Sebastià Matas Riera (bitel)
     */
    private void logLopdBZENTRA(String tipusAcces, String usuari, int data, int hora, int nombreRegistre, int any, int oficina) {
        Session session = getSession();
        try {
            LogEntradaLopd entradaLog = new LogEntradaLopd(new LogEntradaLopdId(tipusAcces, usuari, Integer.valueOf(data), Integer.valueOf(hora), Integer.valueOf(nombreRegistre), Integer.valueOf(any), Integer.valueOf(oficina)));
            session.save(entradaLog);
            session.flush();
            log.debug("logLopdBZENTRA ejecutado correctamente.");
        } catch (HibernateException he) {
            throw new EJBException(he);
        } finally {
            close(session);
        }
    }
}
