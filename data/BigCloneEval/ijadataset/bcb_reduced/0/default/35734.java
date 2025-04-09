import java.io.*;
import javax.servlet.*;
import javax.servlet.http.*;
import java.util.*;
import java.awt.image.*;
import javax.imageio.*;
import java.awt.*;

public class ServletEsercitazioni extends HttpServlet {

    static final int TOO_EASY = 0;

    static final int EASY = 1;

    static final int SUITABLE = 2;

    static final int DIFFICULT = 3;

    static final int TOO_DIFFICULT = 4;

    static final int NO_VOTE = -1;

    private static final int GRATIS = 6;

    private static final int LOGIN_MODE = 1;

    private static final int PASSWD_MODE = 2;

    private static final int EX_LIST_MODE = 3;

    private static final int EX_MODE = 4;

    private static final int VOTE_MODE = 5;

    private static final int LIST_MODE = 6;

    private static final int STAT_MODE = 7;

    private static final String home = "/home/coppola/servlets/Esercitazioni/";

    private static final String DOCTYPE = "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.1//EN\" " + "\"http://www.w3.org/TR/xhtml11/DTD/xhtml11.dtd\">\n" + "<html xmlns=\"http://www.w3.org/1999/xhtml\">\n";

    private static final String RACCOMANDAZIONI = "<p class=\"raccomandazioni\"> <strong>Raccomandazioni</strong>: " + "Ove non altrimenti indicato, rispondete alle domande prima " + "ragionando su carta e poi provando a editare, compilare ed " + "eseguire. Gli esercizi etichettati con l'asterisco (*) sono " + "pi&ugrave; difficili: affrontateli dopo aver risolto gli altri." + "</p>\n";

    private static final String STYLE = "<link rel=\"stylesheet\" type=\"text/css\" " + "href=\"http://www.dimi.uniud.it/~coppola/base.css\" />\n" + "<link rel=\"stylesheet\" type=\"text/css\" " + "href=\"http://www.dimi.uniud.it/~coppola/didactics/twm/twm.css\"/>\n";

    private static final String NAVBAR1 = "<div class=\"navbar\"><a class=\"navbar\" href=\"" + "http://www.dimi.uniud.it/~coppola/\">Paolo Coppola homepage</a>" + "&rarr;<a class=\"navbar\" href=\"" + "http://www.dimi.uniud.it/~coppola/didactics/twm/aa2003-2004/\"" + ">Lab. Prog. 2003/04</a>";

    private static final String NAVBAR1b = "&rarr;<span class=\"navbar\">";

    private static final String NAVBAR2 = "</span></div>\n";

    private static int STAT_W = 160;

    private static int STAT_H = 160;

    private static Color[] GRAPH_COLORS = { Color.cyan, Color.blue, Color.green, Color.magenta, Color.red };

    /**
	 * Il file degli utenti e' composto in questo modo:
	 * uid
	 * pass
	 * nomeultimaesercitazione
	 * numeroultimoesercizio
	 * uid
	 * pass
	 * ...
	 *
	 * le esercitazioni devono essere tutte con nome e#.html
	 * gli esercizi devono essere tutti dentro un div con id="#"
	 */
    private static final String usersFile = "userids.txt";

    private static final String marksFile = "marks.txt";

    private static HashMap users;

    private static HashMap usersToCheck;

    private static int uidWrite = 0;

    private static HashMap practices;

    private static boolean[][] buildStatImage;

    private static TreeMap voti;

    private static TreeMap commenti;

    private class UserData {

        String passwd;

        String esercitaz;

        String numEs;

        public UserData(String passwd, String esercitaz, String numEs) {
            this.passwd = passwd;
            this.esercitaz = esercitaz;
            this.numEs = numEs;
        }
    }

    public void init() throws ServletException {
        users = new HashMap();
        usersToCheck = new HashMap();
        practices = new HashMap();
        try {
            BufferedReader r = new BufferedReader(new FileReader(home + usersFile));
            String line;
            while ((line = r.readLine()) != null) {
                UserData dati = new UserData(r.readLine(), r.readLine(), r.readLine());
                String uid = line;
                users.put(uid, dati);
            }
            r.close();
            PrintWriter p = new PrintWriter(new FileWriter(home + usersFile), true);
            Iterator i = users.keySet().iterator();
            while (i.hasNext()) {
                String uid = (String) i.next();
                UserData dati = (UserData) users.get(uid);
                p.println(uid);
                p.println(dati.passwd);
                p.println(dati.esercitaz);
                p.println(dati.numEs);
            }
            p.close();
            r = new BufferedReader(new FileReader(home + marksFile));
            voti = new TreeMap(Stat.getComparator());
            commenti = new TreeMap(Stat.getComparator());
            while ((line = r.readLine()) != null) {
                Stat s = new Stat();
                StringTokenizer st = new StringTokenizer(line, "$");
                st.nextToken();
                s.uid = st.nextToken().trim();
                String temp = st.nextToken().trim();
                s.esercitazione = Integer.parseInt(temp.substring(1, temp.indexOf(".html")));
                s.esercizio = Integer.parseInt(st.nextToken().trim());
                int voto = Integer.parseInt(st.nextToken().trim());
                if (voto >= 0) {
                    voti.put(s, new Integer(voto));
                    String commento;
                    if (st.hasMoreTokens()) {
                        commento = st.nextToken().trim();
                        if (commento.length() > 0) {
                            Stat s2 = new Stat();
                            s2.esercitazione = s.esercitazione;
                            s2.esercizio = s.esercizio;
                            s2.uid = "";
                            ArrayList l;
                            if (commenti.containsKey(s2)) {
                                l = (ArrayList) commenti.get(s2);
                            } else l = new ArrayList();
                            l.add(commento.replaceAll("<", "&lt;").replaceAll(">", "&gt;"));
                            commenti.put(s2, l);
                        }
                    }
                }
            }
            r.close();
        } catch (IOException e) {
            throw new ServletException("IOException " + e);
        }
    }

    private static final int LOGIN_OK = 1;

    private static final int LOGIN_ERROR = 2;

    private static final int LOGIN_NEW = 3;

    private int login(String user, String pass) throws IOException {
        UserData dati;
        if (user.equals("")) return LOGIN_ERROR;
        if (users.containsKey(user)) if (((UserData) users.get(user)).passwd.equals(pass)) return LOGIN_OK; else return LOGIN_ERROR; else return LOGIN_NEW;
    }

    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("text/html");
        PrintWriter out = response.getWriter();
        int mode = 0;
        try {
            mode = Integer.parseInt(request.getParameter("mode"));
        } catch (NumberFormatException e) {
            response.sendRedirect("login.html");
        }
        String uid = request.getParameter("uid");
        String passwd;
        String numEs;
        String ex;
        UserData dati;
        switch(mode) {
            case LOGIN_MODE:
                passwd = request.getParameter("passwd");
                switch(login(uid, passwd)) {
                    case LOGIN_ERROR:
                        generateLoginError(uid, out);
                        break;
                    case LOGIN_NEW:
                        usersToCheck.put(uid, passwd);
                        out.println(DOCTYPE + "<head>\n<title>Login</title>\n" + STYLE + "</head>\n<body>" + NAVBAR1 + NAVBAR1b + "Login" + NAVBAR2 + "\n<form action=\"Esercitazioni\" " + "method=\"post\">\n" + "<h1>Benvenuto " + uid + "</h1>\n" + "<p>&Egrave; la prima volta che ti colleghi.<br/>" + "Digita di nuovo la tua password</p>\n" + "<label>passwd: </label><input type=\"password\" " + "name=\"passwd\" /><br /><br />\n" + "<input type=\"submit\" value=\"ok\" />\n" + "<input type=\"hidden\" name=\"uid\" " + "value=\"" + uid + "\" />" + "<input type=\"hidden\" name=\"mode\"" + "value=\"" + PASSWD_MODE + "\" />" + "\n</form></body></html>");
                        break;
                    case LOGIN_OK:
                        generateExList(uid, out);
                        break;
                }
                break;
            case PASSWD_MODE:
                passwd = request.getParameter("passwd");
                if (passwd.equals(usersToCheck.get(uid))) {
                    PrintWriter p = new PrintWriter(new FileWriter(home + usersFile, true), true);
                    p.println(uid);
                    p.println(passwd);
                    File[] exList = practicesList();
                    p.println(exList[GRATIS].getName());
                    p.println("0");
                    p.close();
                    users.put(uid, new UserData(passwd, exList[GRATIS].getName(), "0"));
                    generateExList(uid, out);
                } else generateLoginError(uid, out);
                break;
            case LIST_MODE:
                generateExList(uid, out);
                break;
            case EX_LIST_MODE:
                String practice = request.getParameter("ex");
                generateEs(uid, practice, out);
                break;
            case EX_MODE:
                ex = request.getParameter("ex");
                numEs = request.getParameter("numEs");
                dati = (UserData) users.get(uid);
                if (request.getParameter("nextB") != null) if (ex.equals(dati.esercitaz) && Integer.parseInt(numEs) > Integer.parseInt(dati.numEs)) generateMark(uid, ex, numEs, false, out); else generateEs(uid, ex, String.valueOf(Integer.parseInt(numEs) + 1), out); else if (request.getParameter("modVotoB") != null) generateMark(uid, ex, numEs, true, out); else generateEs(uid, ex, String.valueOf(Integer.parseInt(numEs) - 1), out);
                break;
            case VOTE_MODE:
                ex = request.getParameter("ex");
                numEs = request.getParameter("numEs");
                if (request.getParameter("voto") == null) generateMark(uid, ex, numEs, request.getParameter("modify").equals("true"), out); else {
                    PrintWriter votiFile = new PrintWriter(new FileWriter(home + marksFile, true), true);
                    String commento = request.getParameter("commento");
                    if (commento.length() > 320) commento = commento.substring(0, 320);
                    int voto = Integer.parseInt(request.getParameter("voto"));
                    votiFile.println((new Date()) + " $ " + uid + " $ " + ex + " $ " + numEs + " $ " + voto + " $ " + commento.replace('\n', ' ').replace('\r', ' '));
                    votiFile.close();
                    if (voto >= 0) {
                        Stat s = new Stat();
                        s.uid = uid;
                        s.esercitazione = Integer.parseInt(ex.substring(1, ex.indexOf(".html")));
                        s.esercizio = Integer.parseInt(numEs);
                        buildStatImage[s.esercitazione][s.esercizio] = true;
                        buildStatImage[s.esercitazione][0] = true;
                        voti.put(s, new Integer(voto));
                        Stat s2 = new Stat();
                        s2.esercitazione = s.esercitazione;
                        s2.esercizio = s.esercizio;
                        s2.uid = "";
                        commento = commento.replace('\n', ' ').replace('\r', ' ').replaceAll("<", "&lt;").replaceAll(">", "&gt;").trim();
                        if (commento.length() > 0) {
                            ArrayList l;
                            if (commenti.containsKey(s2)) {
                                l = (ArrayList) commenti.get(s2);
                            } else l = new ArrayList();
                            l.add(commento);
                            commenti.put(s2, l);
                        }
                    }
                    dati = (UserData) users.get(uid);
                    String nextEs = practices.get(dati.esercitaz).toString();
                    if (ex.equals(dati.esercitaz)) if (Integer.parseInt(numEs) > Integer.parseInt(dati.numEs)) {
                        nextEs = String.valueOf(Integer.parseInt(numEs) + 1);
                        users.put(uid, new UserData(dati.passwd, ex, numEs));
                        if (uidWrite % 30 == 0) {
                            PrintWriter p = new PrintWriter(new FileWriter(home + usersFile), true);
                            Iterator i = users.keySet().iterator();
                            while (i.hasNext()) {
                                String uid1 = (String) i.next();
                                UserData dati1 = (UserData) users.get(uid1);
                                p.println(uid1);
                                p.println(dati1.passwd);
                                p.println(dati1.esercitaz);
                                p.println(dati1.numEs);
                            }
                            p.close();
                        } else {
                            PrintWriter p = new PrintWriter(new FileWriter(home + usersFile, true), true);
                            p.println(uid);
                            p.println(dati.passwd);
                            p.println(ex);
                            p.println(numEs);
                            p.close();
                        }
                        uidWrite++;
                    } else nextEs = dati.numEs;
                    generateEs(uid, ex, nextEs, out);
                }
                break;
            case STAT_MODE:
                ex = request.getParameter("ex");
                generateStatistics(ex, out);
                break;
        }
        out.close();
    }

    private String loadEs(String uid, String esFile, String numEs) throws IOException {
        String es = new String();
        String line;
        BufferedReader r = new BufferedReader(new FileReader(home + esFile));
        while ((line = r.readLine()) != null) if (line.indexOf("<div") >= 0 && line.indexOf("id=\"" + numEs + "\"") >= 0) {
            do {
                es += line + "\n";
                line = r.readLine();
            } while (line.indexOf("</div><!-- " + numEs + " -->") < 0);
            return es + "</div>";
        }
        return null;
    }

    private File[] practicesList() throws IOException {
        File currentDir = new File(home);
        File[] exList = currentDir.listFiles(new FilenameFilter() {

            public boolean accept(File dir, String name) {
                try {
                    return name.matches("e\\d+\\.html") && Integer.parseInt(name.substring(1, name.indexOf(".html"))) > 0;
                } catch (NumberFormatException e) {
                    return false;
                }
            }
        });
        Arrays.sort(exList, new Comparator() {

            public int compare(Object o1, Object o2) {
                String f1 = ((File) o1).getName();
                String f2 = ((File) o2).getName();
                return Integer.parseInt(f1.substring(1, f1.indexOf(".html"))) - Integer.parseInt(f2.substring(1, f2.indexOf(".html")));
            }
        });
        if (practices == null || practices.size() != exList.length) loadPractices(exList);
        return exList;
    }

    private void loadPractices(File[] p) throws IOException {
        for (int i = 0; i < p.length; i++) if (!practices.containsKey(p[i].getName())) {
            BufferedReader br = new BufferedReader(new FileReader(p[i]));
            int count = 0;
            String line;
            while ((line = br.readLine()) != null) if (line.indexOf("</div><!-- ") >= 0) try {
                Integer.parseInt(line.substring(line.indexOf("<!-- ") + 5, line.indexOf("-->") - 1));
                count++;
            } catch (NumberFormatException e) {
            }
            practices.put(p[i].getName(), new Integer(count));
        }
    }

    private void generateLoginError(String uid, PrintWriter out) throws IOException {
        out.println(DOCTYPE + "<head>\n<title>Errore</title>\n" + "<meta http-equiv=Refresh content=\"3; " + "URL=login.html\" />\n</head>\n<body>\n" + "<p style=\"font-size: large; text-align: center;" + " color: red;\"><strong>Errore! Password errata " + "per " + uid + "</strong></p>\n</body>\n</html>");
    }

    private void generateExList(String uid, PrintWriter out) throws IOException {
        File userMessage = new File(home + uid + ".msg");
        File msg4All = new File(home + "msg4all.msg");
        File[] exList = practicesList();
        if (buildStatImage == null) {
            buildStatImage = new boolean[exList.length + 1][];
            for (int i = 1; i < buildStatImage.length; i++) {
                buildStatImage[i] = new boolean[((Integer) practices.get(exList[i - 1].getName())).intValue() + 1];
                Arrays.fill(buildStatImage[i], 0, buildStatImage[i].length, true);
            }
        } else if (buildStatImage.length < exList.length + 1) {
            boolean[][] tmp = new boolean[exList.length + 1][];
            for (int i = 1; i < buildStatImage.length; i++) {
                tmp[i] = new boolean[buildStatImage[i].length];
                for (int j = 0; j < tmp[i].length; j++) tmp[i][j] = buildStatImage[i][j];
            }
            for (int i = buildStatImage.length; i < tmp.length; i++) {
                tmp[i] = new boolean[((Integer) practices.get(exList[i - 1].getName())).intValue() + 1];
                Arrays.fill(tmp[i], 0, tmp[i].length, true);
            }
            buildStatImage = tmp;
        }
        out.println(DOCTYPE + "<head>\n<title>Esercitazioni</title>\n" + STYLE + "</head>\n<body>\n" + NAVBAR1 + NAVBAR1b + "Esercitazioni" + NAVBAR2);
        if (userMessage.exists() || msg4All.exists()) {
            out.println("<div class=\"messaggio\">");
            if (userMessage.exists()) {
                BufferedReader b = new BufferedReader(new FileReader(userMessage));
                String line;
                while ((line = b.readLine()) != null) out.println(line);
                b.close();
                userMessage.renameTo(new File(home + userMessage.getName() + ".read"));
            }
            if (msg4All.exists()) {
                BufferedReader b = new BufferedReader(new FileReader(msg4All));
                String line;
                while ((line = b.readLine()) != null) out.println(line);
                b.close();
            }
            out.println("</div>");
        }
        out.println("<h1>Esercitazioni</h1>\n" + "<ul>");
        int i = 0;
        do {
            out.println("<li><a href=\"Esercitazioni?mode=" + EX_LIST_MODE + "&uid=" + uid + "&ex=" + exList[i].getName() + "\">Esercitazione " + exList[i].getName().substring(1, exList[i].getName().indexOf(".html")) + "</a> (" + practices.get(exList[i].getName()) + " esercizi) [<a href=\"Esercitazioni?mode=" + STAT_MODE + "&ex=" + exList[i].getName() + "\">statistiche</a> (" + "difficolt&agrave; media percepita: " + computeAverageExDifficult(exList[i].getName()) + "%)]</li>");
            i++;
        } while (i < exList.length && !exList[i - 1].getName().equals(((UserData) users.get(uid)).esercitaz));
        while (i < exList.length) {
            out.println("<li>Esercitazione " + exList[i].getName().substring(1, exList[i].getName().indexOf(".html")) + "</li>");
            i++;
        }
        out.println("</ul></body></html>");
    }

    private void generateEs(String uid, String practice, PrintWriter out) throws IOException {
        UserData dati = (UserData) users.get(uid);
        String numEs = (practice.equals(dati.esercitaz)) ? (Integer.parseInt(dati.numEs) > 0 ? String.valueOf(Integer.parseInt(dati.numEs)) : "1") : practices.get(practice).toString();
        generateEs(uid, practice, numEs, out);
    }

    private void generateEs(String uid, String practice, String numEs, PrintWriter out) throws IOException {
        String exText = loadEs(uid, practice, numEs);
        if (exText == null) {
            File[] exList = practicesList();
            int i;
            for (i = 0; i < exList.length && !exList[i].getName().equals(practice); i++) ;
            if (i == exList.length - 1) {
                out.println(DOCTYPE + "<head>\n<title>Vota</title>\n" + STYLE + "<meta http-equiv=Refresh content=\"3; " + "URL=Esercitazioni?mode=" + LIST_MODE + "&uid=" + uid + "\" /></head>\n<body>\n" + NAVBAR1 + "&rarr;<a class=\"navbar\" href=\"" + "Esercitazioni?mode=" + LIST_MODE + "&uid=" + uid + "\">Esercitazioni</a>" + NAVBAR1b + "Fine esercizi" + NAVBAR2 + "<h1>Complimenti!</h1>\n" + "<p>Hai terminato tutti gli esercizi.</p>\n" + "</body>\n</html>");
            } else {
                UserData dati = (UserData) users.get(uid);
                String nextEs = practices.get(exList[i + 1].getName()).toString();
                if (dati.esercitaz.equals(exList[i].getName())) {
                    users.put(uid, new UserData(dati.passwd, exList[i + 1].getName(), "0"));
                    PrintWriter p = new PrintWriter(new FileWriter(home + usersFile, true), true);
                    p.println(uid);
                    p.println(dati.passwd);
                    p.println(exList[i + 1].getName());
                    p.println("0");
                    p.close();
                    nextEs = "1";
                } else if (dati.esercitaz.equals(exList[i + 1].getName())) nextEs = dati.numEs.equals("0") ? "1" : dati.numEs;
                generateEs(uid, exList[i + 1].getName(), nextEs, out);
            }
        } else {
            UserData dati = (UserData) users.get(uid);
            String numEx = practice.substring(1, practice.indexOf(".html"));
            out.println(DOCTYPE + "<head>\n<title>Esercitazione #" + numEx + "</title>\n" + STYLE + "</head>\n<body>\n" + NAVBAR1 + "&rarr;<a class=\"navbar\" href=\"" + "Esercitazioni?mode=" + LIST_MODE + "&uid=" + uid + "\">Esercitazioni</a>" + NAVBAR1b + "Esercitazione #" + numEx + NAVBAR2 + "<h1>Esercitazione #" + numEx + "</h1>\n" + RACCOMANDAZIONI);
            int nEs = Integer.parseInt(numEs);
            double[] diffic;
            for (int i = 1; i < nEs; i++) {
                diffic = computeAverageEsDifficult(numEx, i);
                out.println(loadEs(uid, practice, String.valueOf(i)).replaceFirst("</h2>", "<br />\n<span class=\"sottotitolo\"" + ">(difficolt&agrave; media percepita: " + diffic[0] + "% [" + ((int) diffic[1]) + " vot" + (diffic[1] == 1.0 ? "o" : "i") + "])" + "</span></h2>"));
                out.println("<form action=\"Esercitazioni\" method=\"POST\">" + "<input type=\"hidden\" name=\"mode\" value=\"" + EX_MODE + "\" />\n" + "<input type=\"hidden\" name=\"uid\" value=\"" + uid + "\" />\n" + "<input type=\"hidden\" name=\"ex\" value=\"" + practice + "\" />\n" + "<input type=\"hidden\" name=\"numEs\" value=\"" + i + "\" />\n");
                out.println("<input type=\"submit\" name=\"modVotoB\" " + "value=\"modifica il voto\" />\n</form>");
            }
            diffic = computeAverageEsDifficult(numEx, nEs);
            out.println(exText.replaceFirst("</h2>", "<br />\n<span class=\"sottotitolo\"" + ">(difficolt&agrave; media percepita: " + diffic[0] + "% [" + ((int) diffic[1]) + " vot" + (diffic[1] == 1.0 ? "o" : "i") + "])</span></h2>"));
            out.println("<form action=\"Esercitazioni\" method=\"POST\">" + "<input type=\"hidden\" name=\"mode\" value=\"" + EX_MODE + "\" />\n" + "<input type=\"hidden\" name=\"uid\" value=\"" + uid + "\" />\n" + "<input type=\"hidden\" name=\"ex\" value=\"" + practice + "\" />\n" + "<input type=\"hidden\" name=\"numEs\" value=\"" + numEs + "\" />\n");
            if (!practice.equals(dati.esercitaz) || Integer.parseInt(numEs) <= Integer.parseInt(dati.numEs)) out.println("<input type=\"submit\" name=\"modVotoB\" " + "value=\"modifica il voto\" />");
            out.println("<input type=\"submit\" name=\"nextB\" value=\"" + "successivo\" />\n</form>");
            out.println("</body></html>");
        }
    }

    private void generateMark(String uid, String practice, String numEs, boolean modify, PrintWriter out) throws IOException {
        out.println(DOCTYPE + "<head>\n<title>Vota</title>\n" + STYLE + "</head>\n<body>\n" + NAVBAR1 + NAVBAR1b + "Vota" + NAVBAR2 + "<h1>Vota l'esercizio</h1>\n");
        out.println("<form action=\"Esercitazioni" + (modify ? "" : "#" + (Integer.parseInt(numEs) + 1)) + "\" method=\"POST\">" + "<input type=\"hidden\" name=\"mode\" value=\"" + VOTE_MODE + "\" />\n" + "<input type=\"hidden\" name=\"uid\" value=\"" + uid + "\" />\n" + "<input type=\"hidden\" name=\"ex\" value=\"" + practice + "\" />\n" + "<input type=\"hidden\" name=\"numEs\" value=\"" + numEs + "\" />\n" + "<input type=\"hidden\" name=\"modify\" value=\"" + modify + "\" />\n");
        out.println("<p>L'esercizio appena fatto &egrave;:</p>\n" + "<input type=\"radio\" name=\"voto\" value=\"" + TOO_EASY + "\" />" + "troppo facile<br />\n" + "<input type=\"radio\" name=\"voto\" value=\"" + EASY + "\" />" + "facile<br />\n" + "<input type=\"radio\" name=\"voto\" value=\"" + SUITABLE + "\" />" + "adeguato<br />\n" + "<input type=\"radio\" name=\"voto\" value=\"" + DIFFICULT + "\" />" + "difficile<br />\n" + "<input type=\"radio\" name=\"voto\" value=\"" + TOO_DIFFICULT + "\" />" + "troppo difficile\n<br /><br />" + "<input type=\"radio\" name=\"voto\" value=\"" + NO_VOTE + "\" />" + "non voglio votare\n" + "<p>commento (opzionale, max 320 caratteri, <strong>" + "non usate le lettere accentate!</strong>)<br />" + "<textarea name=\"commento\" rows=\"8\" " + "cols=\"40\"></textarea></p>\n" + "<input type=\"submit\" value=\"ok\" /></form>" + "</body></html>");
    }

    private void generateStatistics(String practice, PrintWriter out) throws IOException {
        int numPractice = Integer.parseInt(practice.substring(1, practice.indexOf(".html")));
        Iterator j = voti.keySet().iterator();
        TreeMap sta = new TreeMap();
        while (j.hasNext()) {
            Stat s = (Stat) j.next();
            if (s.esercitazione == numPractice) {
                int voto = ((Integer) voti.get(s)).intValue();
                Integer temp = new Integer(s.esercitazione);
                if (!sta.containsKey(temp)) sta.put(temp, new TreeMap());
                TreeMap es = (TreeMap) sta.get(temp);
                temp = new Integer(s.esercizio);
                if (!es.containsKey(temp)) es.put(temp, new Voti());
                Voti v = (Voti) es.get(temp);
                v.numero[voto]++;
                es.put(temp, v);
                sta.put(new Integer(s.esercitazione), es);
            }
        }
        j = sta.keySet().iterator();
        out.println(DOCTYPE + "<head>\n<title>Statistiche</title>\n" + STYLE + "</head>\n<body>\n" + NAVBAR1 + NAVBAR1b + "Statistiche" + NAVBAR2 + "<h1>Statistiche</h1>\n<table " + "class=\"statistiche esercitazione\">");
        while (j.hasNext()) {
            Integer temp = (Integer) j.next();
            out.println("<tr><th colspan=\"3\">Esercitazione " + temp + "</th></tr>");
            out.println("<tr><td colspan=\"3\" ><img src=\"Esercitazioni/es" + temp + ".jpg\" /></td></tr>");
            TreeMap es = (TreeMap) sta.get(temp);
            Iterator k = es.keySet().iterator();
            int[] votazioni = new int[((Integer) es.lastKey()).intValue() + 2];
            votazioni[votazioni.length - 1] = 1000000000;
            int contaRiga = 1;
            while (k.hasNext()) {
                Integer tmp = (Integer) k.next();
                Voti v = (Voti) es.get(tmp);
                if (contaRiga % 3 == 1) out.print("<tr>");
                out.println("<td><table class=\"statistiche\" border=\"1\">\n" + "<tr><th colspan=\"5\">Esercizio " + tmp + "</th></tr>");
                int sommaVotanti = 0;
                for (int i = 0; i < v.numero.length; i++) sommaVotanti += v.numero[i];
                BufferedImage torta = null;
                Graphics2D g2d = null;
                if (buildStatImage[temp.intValue()][tmp.intValue()]) {
                    torta = new BufferedImage(STAT_W, STAT_H, BufferedImage.TYPE_INT_RGB);
                    g2d = torta.createGraphics();
                    g2d.setColor(Color.white);
                    g2d.fillRect(0, 0, STAT_W, STAT_H);
                    int arc = 0;
                    for (int i = 0; i < v.numero.length; i++) {
                        g2d.setColor(GRAPH_COLORS[i]);
                        g2d.fillArc(0, 0, STAT_W, STAT_H, arc, i == v.numero.length - 1 ? 360 - arc : v.numero[i] * 360 / sommaVotanti);
                        arc += v.numero[i] * 360 / sommaVotanti;
                    }
                    ImageIO.write(torta, "jpg", new File(home + "e" + temp + "-" + tmp + ".jpg"));
                    buildStatImage[temp.intValue()][tmp.intValue()] = false;
                }
                if (sommaVotanti > votazioni[0]) votazioni[0] = sommaVotanti;
                if (sommaVotanti < votazioni[votazioni.length - 1]) votazioni[votazioni.length - 1] = sommaVotanti;
                votazioni[tmp.intValue()] = sommaVotanti;
                out.println("<tr><td rowspan=\"5\"><img src=\"Esercitazioni/e" + temp + "-" + tmp + ".jpg\" width=\"" + STAT_W / 2 + "\" height=\"" + STAT_H / 2 + "\" /></td>");
                int i = 0;
                do {
                    out.println("<td style=\"background-color: rgb(" + GRAPH_COLORS[i].getRed() + "," + GRAPH_COLORS[i].getGreen() + "," + GRAPH_COLORS[i].getBlue() + ");\">&nbsp;</td>");
                    out.print("<td>");
                    switch(i) {
                        case 0:
                            out.println("troppo facile");
                            break;
                        case 1:
                            out.println("facile");
                            break;
                        case 2:
                            out.println("adeguato");
                            break;
                        case 3:
                            out.println("difficile");
                            break;
                        case 4:
                            out.println("troppo difficile");
                    }
                    out.println("</td><td>" + v.numero[i] + "</td><td>" + Math.round(v.numero[i] / (double) sommaVotanti * 100) + "%</td></tr>");
                    i++;
                } while (i < v.numero.length);
                out.println("<tr><th colspan=\"5\">commenti</th></tr>\n" + "<tr><td colspan=\"5\" class=\"commenti\"><ol>");
                Stat tmpStat = new Stat();
                tmpStat.esercitazione = temp.intValue();
                tmpStat.esercizio = tmp.intValue();
                tmpStat.uid = "";
                if (commenti.containsKey(tmpStat)) {
                    ArrayList comArr = (ArrayList) commenti.get(tmpStat);
                    ListIterator k1 = comArr.listIterator(comArr.size());
                    while (k1.hasPrevious()) out.println("<li>" + k1.previous() + "</li>");
                }
                out.print("</ol></td></tr>\n</table></td>");
                if (contaRiga % 3 == 0) out.println("</tr>");
                contaRiga++;
            }
            if (contaRiga % 3 != 1) out.println("</tr>");
            for (int i = 1; i < votazioni.length - 1; i++) if (votazioni[i] == 0) {
                votazioni[votazioni.length - 1] = 0;
                break;
            }
            BufferedImage grafico = null;
            Graphics2D grafico2d = null;
            if (buildStatImage[temp.intValue()][0]) {
                grafico = new BufferedImage(STAT_W * 3, STAT_H * 2, BufferedImage.TYPE_INT_RGB);
                grafico2d = grafico.createGraphics();
                grafico2d.setColor(Color.white);
                grafico2d.fillRect(0, 0, STAT_W * 3, STAT_H * 2);
                int xStep = grafico.getWidth() / votazioni.length;
                int yStep = (grafico.getHeight() - 40) / (votazioni[0] - votazioni[votazioni.length - 1]);
                grafico2d.setColor(Color.blue);
                for (int i = 1; i < votazioni.length - 1; i++) {
                    grafico2d.fillRect(xStep * i - 2, (votazioni[0] - votazioni[i]) * yStep + 20 - 2, 4, 4);
                    grafico2d.drawString(votazioni[i] + "", xStep * i + 6, (votazioni[0] - votazioni[i]) * yStep + 20 - 6);
                    if (i > 1) grafico2d.drawLine(xStep * i, (votazioni[0] - votazioni[i]) * yStep + 20, xStep * (i - 1), (votazioni[0] - votazioni[i - 1]) * yStep + 20);
                }
                ImageIO.write(grafico, "jpg", new File(home + "es" + temp + ".jpg"));
                buildStatImage[temp.intValue()][0] = false;
            }
        }
        out.println("</body>\n</html>");
    }

    private double computeAverageExDifficult(String practice) {
        int numPractice = Integer.parseInt(practice.substring(1, practice.indexOf(".html")));
        int[][] v = new int[2][((Integer) practices.get(practice)).intValue()];
        Arrays.fill(v[0], 0, v[0].length, 0);
        Arrays.fill(v[1], 0, v[1].length, 0);
        Iterator i = voti.keySet().iterator();
        while (i.hasNext()) {
            Stat s = (Stat) i.next();
            if (s.esercitazione == numPractice) {
                v[0][s.esercizio - 1]++;
                v[1][s.esercizio - 1] += ((Integer) voti.get(s)).intValue();
            }
        }
        double media = 0;
        int noVote = 0;
        for (int j = 0; j < v[0].length; j++) if (v[0][j] == 0) noVote++; else media += (v[1][j] / (4.0 * v[0][j]));
        return Math.round(media / (v[0].length - noVote) * 10000) / 100.0;
    }

    private double[] computeAverageEsDifficult(String practiceN, int esN) {
        int practice = Integer.parseInt(practiceN);
        double media = 0;
        int conta = 0;
        Iterator i = voti.keySet().iterator();
        while (i.hasNext()) {
            Stat s = (Stat) i.next();
            if (s.esercitazione == practice && s.esercizio == esN) {
                conta++;
                media += ((Integer) voti.get(s)).intValue();
            }
        }
        return new double[] { conta > 0 ? Math.round(media / (conta * 4) * 10000) / 100.0 : 0.0, conta };
    }

    public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doGet(request, response);
    }
}

class Stat {

    String uid;

    int esercitazione;

    int esercizio;

    static Comparator getComparator() {
        return new Comparator() {

            public int compare(Object o1, Object o2) {
                Stat s1 = (Stat) o1;
                Stat s2 = (Stat) o2;
                if (s1.esercitazione == s2.esercitazione) if (s1.esercizio == s2.esercizio) return s1.uid.compareTo(s2.uid); else return s1.esercizio - s2.esercizio; else return s1.esercitazione - s2.esercitazione;
            }
        };
    }

    public String toString() {
        return uid + ", " + esercitazione + ", " + esercizio;
    }
}

class Voti {

    int[] numero = new int[5];
}
