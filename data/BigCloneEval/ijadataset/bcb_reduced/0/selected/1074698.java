package test.it.schedesoftware.dao.intervento;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import it.schedesoftware.dao.intervento.Intervento;
import it.schedesoftware.dao.intervento.InterventoDAO;
import junit.framework.TestCase;

/**
 * @author andrea
 *
 */
public class TestIntervento extends TestCase {

    private InterventoDAO dao = null;

    @Override
    protected void setUp() throws Exception {
        dao = new InterventoDAO();
        super.setUp();
    }

    public void testGetMaxId() {
        assertEquals(3, dao.getMaxId());
    }

    public void testGetLista() {
        assertNotNull(dao.getList());
        assertEquals(3, dao.getList().size());
    }

    public void testGetVolontario() {
        assertNotNull(dao.getIntervento(1));
        assertEquals(Calendar.SEPTEMBER, dao.getIntervento(1).getData_nascita().getMonth());
    }

    public void testInsertVolontario() {
        Intervento intervento = null;
        Date data = null;
        intervento = new Intervento();
        data = java.sql.Date.valueOf("2008-03-01");
        intervento.setId(dao.getMaxId() + 1);
        intervento.setTurno("17-21");
        intervento.setNome("Valentina");
        intervento.setCognome("Loi");
        intervento.setData_intervento(data);
        data = java.sql.Date.valueOf("1985-09-09");
        intervento.setData_nascita(data);
        intervento.setSesso("F");
        intervento.setResidenza("Cagliari");
        intervento.setVia("via ariosto 2");
        intervento.setServizio(true);
        intervento.setNequipaggio(4);
        intervento.setOrep("09:04");
        intervento.setOrea("10:18");
        intervento.setOre(2);
        intervento.setTurno1(1);
        intervento.setTurno2(1);
        intervento.setTurno3(1);
        intervento.setTurno4(1);
        intervento.setTurno5(1);
        intervento.setTurno6(0);
        intervento.setTipo_intervento(1);
        intervento.setRichiedente(2);
        intervento.setLocalitain("ca/pirri");
        intervento.setDurata(2);
        intervento.setAmbulanza(4);
        intervento.setKmp(26930);
        intervento.setKma(26951);
        intervento.setLuogoricovero("ss trinità");
        intervento.setPatologia(1);
        intervento.setId_year(dao.getIdYearCount(intervento.getData_intervento()) + 1);
        dao.addIntervento(intervento);
        assertEquals(4, dao.getMaxId());
    }

    public void testInsertVolontarioBis() {
        Intervento intervento = null;
        Date data = null;
        intervento = new Intervento();
        data = java.sql.Date.valueOf("2008-03-01");
        intervento.setId(dao.getMaxId() + 1);
        intervento.setTurno("17-21");
        intervento.setNome("Valentina");
        intervento.setCognome("Loi");
        intervento.setData_intervento(data);
        data = java.sql.Date.valueOf("1985-09-09");
        intervento.setData_nascita(data);
        intervento.setSesso("F");
        intervento.setResidenza("Cagliari");
        intervento.setVia("via ariosto 2");
        intervento.setServizio(true);
        intervento.setNequipaggio(4);
        intervento.setOrep("09:04");
        intervento.setOrea("10:18");
        intervento.setOre(2);
        intervento.setTurno1(1);
        intervento.setTurno2(1);
        intervento.setTurno3(1);
        intervento.setTurno4(1);
        intervento.setTurno5(1);
        intervento.setTurno6(0);
        intervento.setTipo_intervento(1);
        intervento.setRichiedente(2);
        intervento.setLocalitain("ca/pirri");
        intervento.setDurata(2);
        intervento.setAmbulanza(4);
        intervento.setKmp(26930);
        intervento.setKma(26951);
        intervento.setLuogoricovero("ss trinità");
        intervento.setPatologia(1);
        intervento.setId_year(dao.getIdYearCount(intervento.getData_intervento()) + 1);
        dao.addIntervento(intervento);
        assertEquals(5, dao.getMaxId());
    }

    public void testUpdate() {
        Intervento intervento = null;
        intervento = dao.getIntervento(4);
        intervento.setNatoa("Cagliari");
        dao.updateIntervento(intervento);
        intervento = dao.getIntervento(4);
        assertEquals("Cagliari", intervento.getNatoa());
        dao.deleteIntervento(4);
        assertEquals(4, dao.getIdYearCount(intervento.getData_intervento()));
    }

    public void testDeleteVolontario() {
        dao.deleteIntervento(5);
    }

    public void testGetLista2() {
        assertNotNull(dao.getList());
        assertEquals(3, dao.getList().size());
    }

    public void testSearchList() throws ParseException {
        String dataS = "06/05/2008";
        SimpleDateFormat dataF = new SimpleDateFormat("dd/MM/yyyy");
        Intervento intervento = null;
        ArrayList<Intervento> interventi = null;
        intervento = new Intervento();
        intervento.setId_year(0);
        intervento.setNome("v");
        intervento.setCognome("");
        interventi = dao.getSearchList(intervento);
        assertNotNull(interventi);
        assertEquals(2, interventi.size());
        intervento.setData_inizio(null);
        try {
            intervento.setData_intervento(dataF.parse(dataS));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        interventi = dao.getSearchList(intervento);
        assertNotNull(interventi);
        assertEquals(1, interventi.size());
        intervento.setId_year(1);
        dataS = "01/01/2008";
        intervento.setData_inizio(new java.sql.Date(dataF.parse(dataS).getTime()));
        interventi = dao.getSearchList(intervento);
        assertNotNull(interventi);
        assertEquals(1, interventi.size());
        intervento.setId_year(0);
        intervento.setData_inizio(new java.sql.Date(dataF.parse(dataS).getTime()));
        interventi = dao.getSearchList(intervento);
        assertNotNull(interventi);
        assertEquals(2, interventi.size());
        dataS = "01/01/2007";
        intervento.setData_inizio(new java.sql.Date(dataF.parse(dataS).getTime()));
        interventi = dao.getSearchList(intervento);
        assertNotNull(interventi);
        assertEquals(1, interventi.size());
        intervento.setId_year(0);
        intervento.setData_inizio(null);
        intervento.setData_intervento(null);
        intervento.setNome("");
        intervento.setTurno1(4);
        interventi = dao.getSearchList(intervento);
        assertNotNull(interventi);
        assertEquals(2, interventi.size());
    }

    public void testGetIdYearCount() {
        Date dataD = null;
        SimpleDateFormat dataF = new SimpleDateFormat("dd/MM/yyyy");
        String dataS = "28/05/2007";
        try {
            dataD = dataF.parse(dataS);
            dataS = dataF.format(dataD);
            dataS = "01/01/" + dataS.substring(6, 10);
            dataD = dataF.parse(dataS);
            assertEquals(java.sql.Date.valueOf("2007-01-01"), dataD);
            dataS = "31/12/" + dataS.substring(6, 10);
            dataD = dataF.parse(dataS);
            assertEquals(java.sql.Date.valueOf("2007-12-31"), dataD);
        } catch (Throwable t) {
            System.out.println("ERRORE");
        }
        dataS = "28/05/2008";
        try {
            dataD = dataF.parse(dataS);
        } catch (Throwable t) {
            System.out.println("ERRORE");
        }
        assertEquals(2, dao.getIdYearCount(dataD));
    }
}
