import java.awt.*;
import java.text.*;
import java.util.*;
import java.io.*;
import java.sql.*;
import java.math.*;
import java.util.regex.*;
import javax.swing.*;
import javax.print.DocFlavor.*;
import org.apache.axis.AxisFault;
import neo.bcb.*;
import static java.math.BigDecimal.ZERO;
import static java.math.BigDecimal.ONE;

/**
 * Implementação do "Business Data Model" do aplicativo.
*/
public class JCM extends JCMFrame {

    /** Construtor complementar da classe estendida. */
    public JCM() {
        super();
        splashHelper.showTask("Conectando ao banco de dados.");
        dbConnect();
        splashHelper.showTask("Preparando o ambiente.");
        for (DBTable table : dbtables) {
            indicesCombo.addItem(table.name);
        }
        updateIndicesTipText();
        @SuppressWarnings("deprecation") java.util.Date minDate = new java.util.Date(0, 0, 1);
        @SuppressWarnings("deprecation") java.util.Date maxDate = new java.util.Date(199, 11, 31);
        iniChooser.setSelectableDateRange(minDate, maxDate);
        fimChooser.setSelectableDateRange(minDate, maxDate);
        dateUtils = DateUtils.getInstance(iniChooser.getDateFormatString());
        cf = NumberFormatFactory.getDecimalFormat("#,##0.00;'<tt>'(#,##0.00)'</tt>'");
        pf = NumberFormatFactory.getDecimalFormat();
        System.setProperty("javax.net.ssl.trustStore", USER_DIR + FILE_SEPARATOR + "jssecacerts");
        File f = new File(System.getProperty("javax.net.ssl.trustStore"));
        if (checkCAcerts || !f.exists()) {
            splashHelper.showTask("Verificando certificado.");
            try {
                CAInstaller instalador = CAInstaller.getInstance();
                String host = "www3.bcb.gov.br";
                if (!instalador.check(host)) {
                    splashHelper.showTask("Instalando certificado.");
                    instalador.install(null, host, 443);
                }
            } catch (Exception e) {
                System.err.println(e);
            }
        }
        splashHelper.showTask("Ativando a interface.");
    }

    /**
   * Classe de consulta trivial das informações de tabela do DB
   * correspondente a uma série temporal.
  */
    private class DBTable {

        /**
     * Construtor do objeto.
     *
     * @param tableName Nome da tabela.
    */
        public DBTable(String tableName) {
            name = tableName;
            build();
        }

        /** Montagem das informações mais consultadas. */
        public void build() {
            size = 0;
            String q = buildSQL("status", name);
            try {
                Statement stm = connection.createStatement();
                ResultSet result = stm.executeQuery(q);
                if (result.next()) {
                    size = result.getInt(1);
                    if (size > 0) {
                        DateFormat f = null;
                        long t = 0;
                        if (useSQLite) {
                            f = new SimpleDateFormat("yyyy-MM-dd");
                            t = f.parse(result.getString(3)).getTime();
                        } else {
                            t = result.getDate(3).getTime();
                        }
                        Calendar c = Calendar.getInstance();
                        c.setTimeInMillis(t);
                        c.set(Calendar.DATE, DateUtils.daysInMonth(c));
                        c.set(Calendar.HOUR_OF_DAY, 0);
                        c.set(Calendar.MINUTE, 0);
                        c.set(Calendar.SECOND, 0);
                        c.set(Calendar.MILLISECOND, 0);
                        last = c.getTimeInMillis();
                        t = (useSQLite ? f.parse(result.getString(2)) : result.getDate(2)).getTime();
                        c.setTimeInMillis(t);
                        c.set(Calendar.HOUR_OF_DAY, 0);
                        c.set(Calendar.MINUTE, 0);
                        c.set(Calendar.SECOND, 0);
                        c.set(Calendar.MILLISECOND, 0);
                        first = c.getTimeInMillis();
                    }
                }
                result.close();
                stm.close();
            } catch (SQLException e) {
                e.printStackTrace();
            } catch (ParseException e) {
                System.err.println(e);
            }
        }

        /**
     * Constrói comentário sobre a amplitude da série.
     *
     * @param full Indicador da montagem de comentário completo.
     * @return Comentário sobre a amplitude da série.
    */
        public String getComment(boolean full) {
            if (size == 0) return "tabela vazia";
            String format = full ? "<html>série disponível de <b>%1$tB.%1$tY</b> a <b>%2$tB.%2$tY</b></html>" : "<b>%1$tB.%1$tY</b> a <b>%2$tB.%2$tY</b>";
            return String.format(DateUtils.localeBR, format, first, last);
        }

        /** Nome da tabela ou índice de correção monetária */
        private String name;

        /** UNIX timestamp da data inicial da série. */
        private long first;

        /** UNIX timestamp da data final da série. */
        private long last;

        /** Número de registros :: número de observações da série */
        private int size;
    }

    /** Array de informações sobre cada tabela no DB. */
    private DBTable[] dbtables;

    /** Conexão única ao DB. */
    private Connection connection;

    /** Indicador de uso do SQLite devido a ausência do tipo DATE. */
    private boolean useSQLite;

    /** Nome do arquivo resource do esquema do DB. */
    private String dbScheme;

    /**
   * Complementa o carregamento das propriedades comportamentais
   * obtendo o esquema de DB preservado em sessão anterior, cujo
   * valor default implica no uso do SQLite.
  */
    @Override
    protected void loadBehavior() {
        super.loadBehavior();
        dbScheme = properties.getProperty("dbScheme", "db03.properties");
    }

    /**
   * Complementa o mecanismo de persistência das propriedades
   * comportamentais preservando o esquema de DB.
  */
    @Override
    protected void saveBehavior() {
        properties.put("dbScheme", dbScheme);
        super.saveBehavior();
    }

    /**
   * Conecta ao banco de dados e obtêm informações sobre as tabelas
   * preenchendo o componente da interface para escolha de índice
   * de correção monetária.
  */
    protected void dbConnect() {
        if (!dbScheme.matches("(?i:^[a-z]\\w*\\.properties$)")) {
            JOptionPane.showMessageDialog(null, "<html>A execução será abortada.<br><br>Esquema de banco de dados mal declarado.</hmtl>", "Erro Fatal", JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }
        properties = loadProperties(dbScheme);
        try {
            Class.forName(peek(properties, "driverClassName"));
            useSQLite = (peek(properties, "protocol").indexOf("sqlite") != -1);
            String urlDB = String.format("%s%s%s", peek(properties, "protocol"), peek(properties, "serverName"), peek(properties, "dbFileName"));
            String username = null;
            String password = null;
            if (!useSQLite) {
                Properties safe = loadProperties(JCMDB_PROPERTIES);
                username = peek(safe, "username");
                password = peek(safe, "password");
            }
            connection = DriverManager.getConnection(urlDB, username, password);
            DatabaseMetaData dbmd = connection.getMetaData();
            String dbName = useSQLite ? peek(properties, "dbCatalogName") : null;
            String schemaPattern = (peek(properties, "protocol").indexOf("derby") != -1) ? username.toUpperCase() : "PUBLIC";
            final String namePattern = "%";
            final String[] types = { "GLOBAL TEMPORARY", "SYSTEM TABLE", "TABLE", "VIEW" };
            final String FMT = "_%d_";
            int len = 0;
            ResultSet rs = dbmd.getTables(dbName, schemaPattern, namePattern, types);
            while (rs.next()) {
                String tableName = rs.getString(3);
                ResultSet r = dbmd.getColumns(dbName, schemaPattern, tableName, namePattern);
                int n = 0;
                while (r.next() && (n < 2)) {
                    String field = r.getString(4).toUpperCase();
                    String type = r.getString(6).toUpperCase();
                    if ((field.equals("DATA") && type.equals("DATE")) || (field.equals("VALOR") && type.contains("DOUBLE"))) n++;
                }
                r.close();
                if (n == 2) {
                    properties.setProperty(String.format(FMT, len++), tableName);
                }
            }
            rs.close();
            if (len == 0) {
                dbCloseConnection();
                JOptionPane.showMessageDialog(null, "<html>A execução será abortada.<br><br>O banco de dados não contém tabelas em<br>conformidade com as especificações.</html>", "Erro Fatal", JOptionPane.ERROR_MESSAGE);
                System.exit(1);
            }
            dbtables = new DBTable[len];
            while (len-- > 0) {
                String key = String.format(FMT, len);
                dbtables[len] = new DBTable(properties.getProperty(key));
                properties.remove(key);
            }
            properties.setProperty("nameAndVersion", String.format("%s %s", dbmd.getDatabaseProductName(), dbmd.getDatabaseProductVersion()));
        } catch (ClassNotFoundException e) {
            System.err.println(e);
            JOptionPane.showMessageDialog(null, "Driver do banco de dados não encontrado.", "Erro Fatal", JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        } catch (SQLException e) {
            System.out.print("FATAL ERROR: " + e);
            String message = null;
            if (e.toString().contains("Unable to connect")) {
                message = "FALHA DE CONEXÃO AO BANCO DE DADOS.";
            } else {
                message = "ERRO DE ACESSO AO BANCO DE DADOS.";
            }
            JOptionPane.showMessageDialog(null, message, "Erro Fatal", JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }
        Arrays.sort(dbtables, new Comparator<DBTable>() {

            public int compare(DBTable t0, DBTable t1) {
                return t0.last > t1.last ? -1 : (t0.last < t1.last ? 1 : (t1.first < t0.first ? 1 : (t1.first > t0.first ? -1 : 0)));
            }
        });
    }

    /**
   * Invoca diálogo modal de edição da tabela de banco de dados selecionada.
  */
    protected void editTable() {
        int ndx = indicesCombo.getSelectedIndex();
        EditWindow w = new EditWindow(this, connection, dbtables[ndx].name, properties);
        centerDialog(w);
        w.setVisible(true);
        w.dispose();
        dbtables[ndx].build();
        updateIndicesTipText();
    }

    /**
   * Invoca o diálogo modal de criação de tabela de banco de dados.
   * O nome da tabela/índice deve começar com letra, contendo apenas
   * letras/digitos/hífen/underline e não pode coincidir com nomes de
   * tabelas existentes no banco de dados, independente do uso de
   * letras capitais.
  */
    protected void createTable() {
        Properties q = loadProperties(INDICES_MENSAIS_PROPERTIES);
        String[] lista = q.values().toArray(new String[0]);
        NewTableDialog dialog = new NewTableDialog(this, lista);
        centerDialog(dialog);
        dialog.setVisible(true);
        boolean ok = dialog.okPressed();
        String name = dialog.getTableName();
        String serieName = dialog.getIndiceName();
        dialog.dispose();
        if (!ok || (name == null)) return;
        name = name.trim();
        if (!name.matches("(?i:^[a-z][\\w-]*$)")) {
            JOptionPane.showMessageDialog(getContentPane(), String.format("<html>O texto <b>\"%s\"</b> não serve como nome de tabela.</html>", name), "Manutenção do Banco de Dados", JOptionPane.ERROR_MESSAGE);
            return;
        }
        ok = false;
        for (int j = 0; !ok && (j < dbtables.length); j++) {
            ok = name.equalsIgnoreCase(dbtables[j].name);
        }
        if (ok) {
            JOptionPane.showMessageDialog(getContentPane(), String.format("<html>O texto <b>\"%s\"</b> já está em uso como nome de tabela.</html>", name), "Manutenção do Banco de Dados", JOptionPane.ERROR_MESSAGE);
            return;
        }
        try {
            Statement stm = connection.createStatement();
            stm.execute(buildSQL("createTable", name));
            stm.close();
        } catch (SQLException e) {
            System.err.println(e);
            JOptionPane.showMessageDialog(getContentPane(), String.format("<html>Não foi possível criar<br>a tabela <b>\"%s\"</b>.</html>", name), "Manutenção do Banco de Dados", JOptionPane.ERROR_MESSAGE);
            return;
        }
        int ndx = dbtables.length;
        DBTable[] list = new DBTable[ndx + 1];
        for (int j = 0; j < ndx; j++) list[j] = dbtables[j];
        dbtables = list;
        dbtables[ndx] = new DBTable(name);
        indicesCombo.addItem(name);
        indicesCombo.setSelectedIndex(ndx);
        if (!serieName.startsWith("<")) {
            String bcbcode = null;
            for (Enumeration<?> en = q.propertyNames(); en.hasMoreElements(); ) {
                bcbcode = (String) en.nextElement();
                if (q.getProperty(bcbcode).equals(serieName)) break;
            }
            Properties p = loadProperties(DBUPDATE_PROPERTIES);
            p.put(dbtables[ndx].name, bcbcode);
            saveProperties(p, DBUPDATE_PROPERTIES);
        }
    }

    /**
   * Invoca diálogo modal de eliminação da tabela de banco de dados
   * selecionada.
  */
    protected void dropTable() {
        if (dbtables.length == 1) {
            JOptionPane.showMessageDialog(getContentPane(), "<html>Operação não permitida.<br><br>O <b>JCM</b> requer ao menos uma tabela de dados<br>para executar as atualizações.</html>", "Manutenção do Banco de Dados", JOptionPane.WARNING_MESSAGE);
            return;
        }
        final int ndx = indicesCombo.getSelectedIndex();
        final int option = JOptionPane.showConfirmDialog(getContentPane(), String.format("<html>Os dados da tabela <b>\"%s\"</b><br>ficarão  <b>permanentemente indisponíveis</b>.<br>Confirma a eliminação da tabela?", dbtables[ndx].name), "Manutenção do Banco de Dados", JOptionPane.YES_NO_OPTION);
        if (option == JOptionPane.YES_OPTION) {
            try {
                Statement stm = connection.createStatement();
                stm.execute(buildSQL("dropTable", dbtables[ndx].name));
                stm.close();
            } catch (SQLException e) {
                System.err.println(e);
                JOptionPane.showMessageDialog(getContentPane(), String.format("<html>Não foi possível eliminar<br>a tabela <b>\"%s\"</b>.</html>", dbtables[ndx].name), "Manutenção do Banco de Dados", JOptionPane.ERROR_MESSAGE);
                return;
            }
            Properties p = loadProperties(DBUPDATE_PROPERTIES);
            String key;
            if (!p.isEmpty() && (p.containsKey(key = dbtables[ndx].name) || p.containsKey(key = key.toLowerCase()))) {
                p.remove(key);
                saveProperties(p, DBUPDATE_PROPERTIES);
            }
            int len = dbtables.length;
            DBTable[] list = new DBTable[len - 1];
            for (int k = -1, j = 0; j < len; j++) {
                if (j != ndx) list[++k] = dbtables[j];
            }
            dbtables = list;
            indicesCombo.removeItemAt(ndx);
        }
    }

    /**
   * Atualização on line da tabela selecionada.
  */
    protected void updateTable() {
        int ndx = indicesCombo.getSelectedIndex();
        Properties p = loadProperties(DBUPDATE_PROPERTIES);
        String bcbcode = p.isEmpty() ? null : p.getProperty(dbtables[ndx].name);
        if (bcbcode == null) return;
        try {
            Aurea aurea = Aurea.getInstance();
            Aurea.VO vo = aurea.get(bcbcode);
            long t = vo.getDate().getTime();
            if ((dbtables[ndx].size > 0) && (t <= dbtables[ndx].last)) {
                JOptionPane.showMessageDialog(getContentPane(), String.format("<html>A tabela \"%s\" está atualizada.</html>", dbtables[ndx].name), "Atualização On Line", JOptionPane.INFORMATION_MESSAGE);
                return;
            }
            setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            String q = buildSQL("insert", dbtables[ndx].name);
            PreparedStatement ps = connection.prepareStatement(q);
            Calendar c = Calendar.getInstance();
            if (dbtables[ndx].size > 0) {
                c.setTimeInMillis(dbtables[ndx].last);
                c.set(Calendar.DATE, 1);
                c.add(Calendar.MONTH, 1);
            } else {
                WSSerieVO w = aurea.stub.getUltimoValorVO(Long.parseLong(bcbcode));
                c.set(Calendar.YEAR, w.getAnoInicio());
                c.set(Calendar.MONTH, w.getMesInicio());
                c.set(Calendar.DATE, w.getDiaInicio());
                c.set(Calendar.HOUR_OF_DAY, 0);
                c.set(Calendar.MINUTE, 0);
                c.set(Calendar.SECOND, 0);
                c.set(Calendar.MILLISECOND, 0);
            }
            long i = c.getTime().getTime();
            if (i < t) {
                String di = dateUtils.format(c);
                c.setTimeInMillis(t);
                c.add(Calendar.MONTH, -1);
                String df = dateUtils.format(c);
                Object[] serie = aurea.get(bcbcode, di, df);
                for (Object oo : serie) {
                    Aurea.VO item = (Aurea.VO) oo;
                    t = item.getDate().getTime();
                    java.sql.Date date = new java.sql.Date(t);
                    if (useSQLite) ps.setString(1, date.toString()); else ps.setDate(1, date);
                    ps.setDouble(2, new Double(item.getValue().doubleValue() / 100.0));
                    ps.executeUpdate();
                }
            }
            t = vo.getDate().getTime();
            java.sql.Date date = new java.sql.Date(t);
            if (useSQLite) ps.setString(1, date.toString()); else ps.setDate(1, date);
            ps.setDouble(2, new Double(vo.getValue().doubleValue() / 100.0));
            ps.executeUpdate();
            dbtables[ndx].build();
            updateIndicesTipText();
            String message = (i < t) ? String.format("<html><center>A série temporal do \"%1$s\" foi atualizada<br>com observações de %2$tb/%2$tY a %3$tb/%3$tY.</center></html>", dbtables[ndx].name, i, t) : String.format("<html><center>A série temporal do \"%1$s\" foi atualizada<br>com a observação de %2$tb/%2$tY.</center></html>", dbtables[ndx].name, t);
            JOptionPane.showMessageDialog(getContentPane(), message, "Atualização On Line", JOptionPane.INFORMATION_MESSAGE);
        } catch (SQLException e) {
            System.err.println(e);
        } catch (Exception e) {
            String fault = null;
            if (e instanceof AxisFault) {
                fault = ((AxisFault) e).getFaultString();
                if (fault.startsWith("javax.net.ssl")) {
                    try {
                        CAInstaller.getInstance().install(null, "www3.bcb.gov.br", 443);
                    } catch (Exception xe) {
                        System.out.println(xe);
                    }
                    if (fault.contains("SSLHandshakeException")) fault = "Certificado de autoridade foi atualizado."; else fault = "Certificado de autoridade foi instalado.";
                    fault += "<br><br>Reinicie o aplicativo para<br>acessar o webservice por favor.";
                } else {
                    fault = (fault.length() == 0) ? "Erro desconhecido do web service." : fault.substring(fault.lastIndexOf(':') + 2);
                }
            } else if (e instanceof java.rmi.RemoteException) {
                fault = "O servidor está fora do ar.<br>Tente atualizar mais tarde.";
            } else {
                fault = "Erro desconhecido.";
            }
            JOptionPane.showMessageDialog(getContentPane(), String.format("<html><center>%s</center></html>", fault), "Atualização On Line", JOptionPane.ERROR_MESSAGE);
            System.err.println(e);
        } finally {
            setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
        }
    }

    /** Encerra a conexão ao DB. */
    protected void dbCloseConnection() {
        String protocol = properties.getProperty("protocol");
        try {
            if (protocol.indexOf("derby") != -1) {
                connection.close();
                DriverManager.getConnection("jdbc:derby:;shutdown=true");
            } else {
                if (protocol.indexOf("hsqldb") != -1) {
                    connection.createStatement().execute(peek(properties, "shutdown"));
                }
                connection.close();
            }
        } catch (SQLException e) {
            System.err.println(e);
        }
    }

    /** Atualiza o texto de ajuda contextual extraído do DB. */
    protected void updateIndicesTipText() {
        EventQueue.invokeLater(new Runnable() {

            public void run() {
                int ndx = indicesCombo.getSelectedIndex();
                indicesCombo.setToolTipText(dbtables[ndx].getComment(true));
            }
        });
    }

    /** Monta um documento reportando o status operacional do DB. */
    protected void makeDbReport() {
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        Temporizador temporizador = new Temporizador(progressBar);
        StringBuilder sb = new StringBuilder("<html><head>");
        sb.append("<title>Análise Operacional do Banco de Dados</title>");
        sb.append("<style>").append(loadText("dbreport.css"));
        sb.append("</style></head><body>");
        sb.append("<h1>Análise Operacional do Banco de Dados</h1>");
        DateFormat f = useSQLite ? new SimpleDateFormat("yyyy-MM-dd") : null;
        try {
            sb.append("<div>RDBMS: <b>");
            sb.append(properties.getProperty("nameAndVersion"));
            sb.append("</b></div>");
            sb.append("<div><table cellpadding=2 cellspacing=0>");
            sb.append("<tr><td class=first>Nome Catálogo:</td><td>");
            sb.append(peek(properties, "dbCatalogName"));
            sb.append("</td></tr>");
            sb.append("<tr><td class=first>No.Tabelas:</td><td>");
            sb.append(dbtables.length).append("</td></tr></table></div>");
            Statement stm = connection.createStatement();
            Calendar calendar = Calendar.getInstance();
            int n = 1;
            temporizador.start();
            Properties r = loadProperties(INDICES_MENSAIS_PROPERTIES);
            Properties p = loadProperties(DBUPDATE_PROPERTIES);
            for (DBTable table : dbtables) {
                sb.append("<div>[").append(n).append("] <b>");
                sb.append(table.name);
                sb.append("</b><table class=info cellpadding=2 cellspacing=1 border=0>");
                String bcbcode = p.getProperty(table.name);
                if (bcbcode != null) {
                    sb.append("<tr><td nowrap class=first>Código no SGS do BCB:</td><td>");
                    sb.append(bcbcode).append("</td></tr>");
                    sb.append("<tr><td nowrap class=first>Descrição no SGS do BCB:</td><td valign=top>");
                    sb.append(r.getProperty(bcbcode)).append("</td></tr>");
                }
                sb.append("<tr><td nowrap class=first>No.Registros:</td><td>");
                sb.append(table.size).append("</td></tr>");
                if (table.size > 0) {
                    sb.append("<tr><td nowrap class=first>Período:</td><td>");
                    sb.append(table.getComment(false)).append("</td></tr>");
                    String q = buildSQL("allDates", table.name);
                    ResultSet result = stm.executeQuery(q);
                    if (result.next()) {
                        sb.append("<tr><td nowrap valign=top class=first>Conclusão:</td>");
                        StringBuilder errList = null;
                        int errCounter = 0;
                        long previous = (useSQLite ? f.parse(result.getString(1)) : result.getDate(1)).getTime();
                        while (result.next()) {
                            long current = (useSQLite ? f.parse(result.getString(1)) : result.getDate(1)).getTime();
                            calendar.setTimeInMillis(current);
                            calendar.add(Calendar.MONTH, -1);
                            if (calendar.getTimeInMillis() != previous) {
                                if (errCounter++ == 0) {
                                    errList = new StringBuilder();
                                    errList.append("Descontinuidade no(s) período(s):").append("<table cellpadding=5 cellspacing=5 border=0>");
                                }
                                errList.append("<tr><td nowrap class=error>&raquo;&nbsp;").append(String.format(DateUtils.localeBR, "%1$tB de %1$tY", previous)).append("</td></tr>");
                            }
                            previous = current;
                        }
                        if (errCounter > 0) {
                            sb.append("<td nowrap class=warn>").append(errList).append("</table>");
                        } else {
                            sb.append("<td nowrap>série temporal sem descontinuidade.");
                        }
                        sb.append("</td></tr>");
                    }
                    result.close();
                }
                sb.append("</table></div>");
                temporizador.update(((double) (n++)) / dbtables.length);
            }
            stm.close();
        } catch (ParseException e) {
            System.err.println(e);
        } catch (SQLException e) {
            System.err.println(e);
        }
        sb.append("<div class=footer>");
        sb.append(String.format(DateUtils.localeBR, "Gerado em %tc.", System.currentTimeMillis()));
        sb.append("</div>").append("</body></html>");
        clearReport();
        report.getEditorKit().createDefaultDocument();
        report.setContentType("text/html");
        report.setText(sb.toString());
        assureSplitPaneDisplay(true);
        report.setCaretPosition(0);
        report.requestFocusInWindow();
        setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
        temporizador.terminate();
    }

    /**
   * Monta expressão SQL correspondente à chave e nome de tabela fornecidos.
   *
   * @param key Chave da expressão SQL.
   * @param tableName Nome da tabela a consultar.
   * @return A expressão SQL pronta para consulta.
  */
    private String buildSQL(String key, String tableName) {
        return properties.getProperty(key).replace("%s", tableName);
    }

    /**
   * Ajusta o formatador de valores monetários conforme indicação de
   * uso/apresentação dos centavos.
   *
   * @param useCents Indicador de uso dos centavos.
  */
    private void adjustFormater(boolean useCents) {
        this.useCents = useCents;
        if (useCents) {
            cf.setMaximumFractionDigits(2);
            cf.setMinimumFractionDigits(2);
        } else {
            cf.setMaximumFractionDigits(0);
        }
    }

    /**
   * Cálculo de taxas proporcionais.
   *
   * @param x Taxa objeto do cálculo.
   * @param a Numerador da proporção.
   * @param b Denominador da proporção.
  */
    private BigDecimal ratio(BigDecimal x, int a, int b) {
        return (a == b) ? x : x.multiply((new BigDecimal(a)).divide(new BigDecimal(b), mc));
    }

    /**
   * Monta linha descritiva da planilha reportando o capital
   * ou os juros remuneratórios pendentes.
   *
   * @param cssClassName Nome da classe CSS usada na linha da tabela HTML.
   * @param dateString Texto contendo a data da operação.
   * @param descricao Texto contendo a descrição da operação.
   * @param value Valor monetário do capital ou dos juros pendentes.
  */
    private void mkRow(String cssClassName, String dateString, String descricao, BigDecimal value) {
        sw.format("<tr><td class=%1$s_center>%2$s</td><td nowrap colspan=7 class=%1$s_center>%3$s</td><td class=%1$s>%4$s</td></tr>", cssClassName, dateString, descricao, cf.format(value));
    }

    /**
   * Monta linha descritiva da planilha reportando o capital.
   *
   * @param cssClassName Nome da classe CSS usada na linha da tabela HTML.
   * @param dateString Texto contendo a data da operação.
   * @param descricao Texto contendo a descrição da operação.
  */
    private void mkRow(String cssClassName, String dateString, String descricao) {
        mkRow(cssClassName, dateString, descricao, kptal);
    }

    /**
   * Realiza reforma monetária aplicando transformação de escala.
   *
   * @param r Índice da reforma monetária.
  */
    private void scale(int r) {
        kptal = Reforma.reformas[r].transform(kptal);
        kptal = kptal.setScale(2, RoundingMode.HALF_UP);
        okane = Reforma.reformas[r].transform(okane);
        okane = okane.setScale(2, RoundingMode.HALF_UP);
        scorr = Reforma.reformas[r].transform(scorr);
        scorr = scorr.setScale(2, RoundingMode.HALF_UP);
        sjuros = Reforma.reformas[r].transform(sjuros);
        sjuros = sjuros.setScale(2, RoundingMode.HALF_UP);
        jurosPending = Reforma.reformas[r].transform(jurosPending);
        jurosPending = jurosPending.setScale(2, RoundingMode.HALF_UP);
        adjustFormater(Reforma.reformas[r].getUseCents());
        currency.next();
        mkRow("reforma", dateUtils.format(Reforma.reformas[r].getDate()), Reforma.reformas[r].getComment(), kptal);
    }

    /**
   * Calcula/acumula/agrega taxas de juros remuneratórios e correção
   * monetária proporcionais ao número de dias num mês além dos demais
   * valores relacionados, reportando todos numa nova linha da planilha.
   *
   * @param cssClassName Nome da classe CSS usada na linha da tabela HTML.
   * @param dateString Texto contendo a data da operação.
   * @param nDays Número de dias entre a data dessa operação e da data
   *              da operação anterior.
   * @param txMensal Taxa de correção monetária no mês dessa operação.
   * @param daysInMonth Número de dias no mês dessa operação.
   * @param daysInYear Número de dias no ano dessa operação.
  */
    private void jcm(String cssClassName, String dateString, int nDays, BigDecimal txMensal, int daysInMonth, int daysInYear) {
        BigDecimal jrate = null;
        BigDecimal juros = null;
        if (validProRata) {
            jrate = ratio(proRata, nDays, daysInYear);
            jurosAno = jurosAno.add(jrate);
            juros = kptal.multiply(jrate);
            if (!useCents) juros = juros.divideToIntegralValue(ONE); else juros = juros.setScale(2, RoundingMode.HALF_UP);
            sjuros = sjuros.add(juros);
            sjuros = sjuros.setScale(2, RoundingMode.HALF_UP);
            jurosPending = jurosPending.add(juros);
            jurosPending = jurosPending.setScale(2, RoundingMode.HALF_UP);
        }
        BigDecimal rate = null;
        BigDecimal correcao = null;
        if (txMensal != null) {
            rate = ratio(txMensal, nDays, daysInMonth);
            corrAcum = corrAcum.multiply(rate.add(ONE));
            correcao = kptal.multiply(rate);
            if (!useCents) correcao = correcao.divideToIntegralValue(ONE); else correcao = correcao.setScale(2, RoundingMode.HALF_UP);
            scorr = scorr.add(correcao);
            scorr = scorr.setScale(2, RoundingMode.HALF_UP);
            kptal = kptal.add(correcao);
            kptal = kptal.setScale(2, RoundingMode.HALF_UP);
        }
        String s = String.format("<td nowrap class=%s_center>%%s</td>", cssClassName);
        sw.write("<tr>");
        sw.format(s, dateString);
        sw.format(s, nDays);
        String t = String.format("<td nowrap class=%s>%%s</td>", cssClassName);
        if (txMensal == null) sw.format(s, "<tt>N/D</tt>"); else sw.format(t, pf.format(txMensal));
        sw.format(t, pf.format(rate != null ? rate : 0));
        sw.format(t, cf.format(correcao != null ? correcao : 0));
        sw.format(t, pf.format(jrate != null ? jrate : 0));
        sw.format(t, cf.format(juros != null ? juros : 0));
        sw.format(t, cf.format(jurosPending));
        sw.format(t, cf.format(kptal));
        sw.write("</tr>");
    }

    /**
   * O método anterior recebendo a data como Calendar.
   *
   * @param cssClassName Nome da classe CSS usada na linha da tabela HTML.
   * @param data Data da operação do tipo Calendar.
   * @param nDays Número de dias entre a data dessa operação e da data
   *              da operação anterior.
   * @param txMensal Taxa de correção monetária no mês dessa operação.
   * @param daysInMonth Número de dias no mês dessa operação.
   * @param daysInYear Número de dias no ano dessa operação.
  */
    private void jcm(String cssClassName, Calendar data, int nDays, BigDecimal txMensal, int daysInMonth, int daysInYear) {
        jcm(cssClassName, dateUtils.format(data), nDays, txMensal, daysInMonth, daysInYear);
    }

    /**
   * Gera um documento text/html contendo a planilha de atualização
   * monetária mês a mês detalhando taxas/valores proporcionais às datas
   * contemplando as reformas econômicas precisamente, respeitando a
   * legislação e recomendações nos respectivos cálculos com a máxima
   * precisão possível.
   * Durante a montagem do documento atualiza a barra de progresso e ao
   * final exibe o documento no painel de reportagem.
   *
   * @return Status da geração do documento.
  */
    protected boolean makeSpreadsheet() {
        kptal = new BigDecimal(((Number) vini.getValue()).doubleValue());
        if (kptal.compareTo(ZERO) <= 0) {
            assureSplitPaneDisplay(false);
            String msg = (kptal == null) ? "<html>Digite o <b>Valor</b> a atualizar.</html>" : "<html><b>Valor</b> a atualizar<br>precisa ser maior que <b>ZERO</b>.</html>";
            JOptionPane.showMessageDialog(getContentPane(), msg, "Montagem de Planilha", JOptionPane.ERROR_MESSAGE);
            vini.requestFocusInWindow();
            vini.selectAll();
            return false;
        }
        JTextField tf = (JTextField) iniChooser.getDateEditor().getUiComponent();
        final Calendar di = dateUtils.parse(tf.getText());
        if (di == null) {
            assureSplitPaneDisplay(false);
            JOptionPane.showMessageDialog(getContentPane(), "<html><b>Data Inicial</b> mal declarada.</html>", "Montagem de Planilha", JOptionPane.ERROR_MESSAGE);
            tf.requestFocusInWindow();
            return false;
        }
        if (di.get(Calendar.YEAR) < 1900) {
            int response = JOptionPane.showConfirmDialog(getContentPane(), "<html><center>O ano da data inicial<br>é anterior a <b>1900</b>.<br>Continuar cálculos?</center></html>", "Confirmação", JOptionPane.YES_NO_OPTION);
            if (response != JOptionPane.YES_OPTION) {
                setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
                return false;
            }
        }
        tf = (JTextField) fimChooser.getDateEditor().getUiComponent();
        final Calendar df = dateUtils.parse(tf.getText());
        if (df == null) {
            assureSplitPaneDisplay(false);
            JOptionPane.showMessageDialog(getContentPane(), "<html><b>Data Final</b> mal declarada.</html>", "Montagem de Planilha", JOptionPane.ERROR_MESSAGE);
            tf.requestFocusInWindow();
            return false;
        }
        if (df.get(Calendar.YEAR) < 1900) {
            int response = JOptionPane.showConfirmDialog(null, "<html><center>O ano da data final<br>é anterior a <b>1900</b>.<br>Continuar cálculos?</center></html>", "Confirmação", JOptionPane.YES_NO_OPTION);
            if (response != JOptionPane.YES_OPTION) {
                setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
                return false;
            }
        }
        if (!di.before(df)) {
            assureSplitPaneDisplay(false);
            JOptionPane.showMessageDialog(getContentPane(), "<html><b>Data Inicial</b><br>não anterior à <b>Data Final</b>.</html>", "Montagem de Planilha", JOptionPane.ERROR_MESSAGE);
            tf.requestFocusInWindow();
            return false;
        }
        final int NUM_ITER = DateUtils.getNumIteracoes(di, df);
        assert (NUM_ITER > 0) : "NUM_ITER <= 0";
        if (warningsEnabled && (NUM_ITER > 120)) {
            int response = JOptionPane.showConfirmDialog(null, "<html><center>O número de meses relevantes<br>no período é superior a <b>120</b>.<br>Continuar cálculos?</center></html>", "Confirmação", JOptionPane.YES_NO_OPTION);
            if (response != JOptionPane.YES_OPTION) {
                setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
                return false;
            }
        }
        final int ndx = indicesCombo.getSelectedIndex();
        if (warningsEnabled && ((di.getTimeInMillis() < dbtables[ndx].first) || (df.getTimeInMillis() > dbtables[ndx].last))) {
            JOptionPane.showMessageDialog(getContentPane(), "<html>&nbsp;Aos índices de inflação com<br>datas fora do período registrado<br><u>serão atribuidos o valor</u> <b>ZERO</b>,<br>portanto não serão cálculadas as<br>correções monetárias nestas datas.</html>", "Montagem de Planilha", JOptionPane.WARNING_MESSAGE);
        }
        this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        proRata = TAXA.eqValue(InterestRate.AO_ANO);
        validProRata = (proRata.compareTo(ZERO) > 0);
        if (warningsEnabled && validProRata && (proRata.compareTo(ONE) > 0)) {
            int response = JOptionPane.showConfirmDialog(null, "<html><center>A taxa de juros ao ano<br>é superior a <b>100%</b>.<br>Continuar cálculos?</center></html>", "Confirmação", JOptionPane.YES_NO_OPTION);
            if (response != JOptionPane.YES_OPTION) {
                setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
                return false;
            }
        }
        Chrono execChrono = new Chrono();
        execChrono.start();
        playAudioClip(6);
        ((DecimalFormat) pf).applyPattern("#,##0.###%");
        sw = new CustomWriter();
        sw.write("<html><head><title>Atualização de Valor Monetário</title>");
        sw.append("<style>").append(loadText("spreadsheet.css").toString());
        sw.write("</style></head><body>");
        sw.write("<h1>Atualização de Valor Monetário</h1>");
        currency = Moeda.getInstance();
        currency.search(di);
        adjustFormater(Reforma.useCentsFrom(di));
        if (!useCents) kptal = kptal.divideToIntegralValue(ONE);
        sw.append("<p>Valor Inicial: <b>").append(currency.getSymbol());
        sw.append(' ').append(cf.format(kptal));
        sw.append("</b>&nbsp;(").append(currency.getName()).append(")</p>");
        sw.write("<div><table cellpadding=2 cellspacing=1><tr><td nowrap class=ini>Data Inicial:</td><td nowrap class=fim><b>");
        sw.format(DateUtils.localeBR, "%1$td de %1$tB de %1$tY", di);
        sw.write("</b></td></tr><tr><td nowrap class=ini>Data Final:</td><td nowrap class=fim><b>");
        sw.format(DateUtils.localeBR, "%1$td de %1$tB de %1$tY", df);
        sw.write("</b></td></tr>");
        sw.write("<tr><td nowrap class=ini>Tempo Decorrido:</td><td nowrap class=fim><b>");
        int k = (int) dateUtils.daysBetweenDates(di, df);
        sw.format("%,d dia", k);
        if (k > 1) sw.write('s');
        sw.write(" em ");
        if (NUM_ITER == 1) sw.write("único mês"); else sw.format("%d meses", NUM_ITER);
        sw.write(".</b></td></tr></table></div>");
        sw.write("<div><table cellpadding=2 cellspacing=1>");
        sw.write("<tr><td class=ini>Taxa de Juros:</td><td class=fim><b>");
        sw.append(pf.format(proRata)).append("<b> ao ano</td></tr>");
        if (validProRata) {
            sw.write("<tr><td class=ini>Regime de Capitalização:</td><td class=fim><b>");
            sw.write(getSelectedGroupOption(regimeGroup).getText().toUpperCase());
            sw.write("</b></td></tr>");
            sw.write("<tr><td class=ini>Agregação de Juros:</td><td class=fim><b>");
            sw.write(getSelectedGroupOption(agregacaoGroup).getText().toUpperCase());
            sw.write("</b></td></tr>");
        }
        sw.write("</table></div>");
        sw.write("<p>Índice de Correção Monetária: <b>");
        sw.append(dbtables[ndx].name).append("</b>");
        Properties h = loadProperties(DBUPDATE_PROPERTIES);
        String bcbcode = h.getProperty(dbtables[ndx].name);
        if (bcbcode != null) {
            h = loadProperties(INDICES_MENSAIS_PROPERTIES);
            String descricao = h.getProperty(bcbcode);
            if (descricao != null) {
                sw.append(" (<i>");
                Matcher m = Pattern.compile("\\s\\(([\\w-]+)\\)").matcher(descricao);
                if (m.find() && m.group(1).equalsIgnoreCase(dbtables[ndx].name)) {
                    sw.append(descricao.substring(0, m.start()));
                    sw.append(descricao.substring(m.end()));
                } else {
                    sw.append(descricao);
                }
                sw.append("</i>)");
            }
        }
        sw.append("</p>");
        sw.write("<div class=main><table cellpadding=5 cellspacing=1>");
        sw.write("<tr><td colspan=2></td>");
        sw.write("<th colspan=3>Correção Monetária</td>");
        sw.write("<th colspan=3>Juros Remuneratórios</td>");
        sw.write("<td></td></tr><tr>");
        String[] headerList = { "Data", "Dias", "Taxa<br>Mensal", "Taxa<br>Aplicada", "Valor", "Taxa<br>Aplicada", "Valor", "Acumulado", "Capital<br>Acumulado" };
        for (String item : headerList) sw.format("<th nowrap>%s</td>", item);
        sw.write("</tr>");
        String[] className = { "one", "two" };
        mkRow(className[0], dateUtils.format(di), "<strong>Valor Inicial</strong>");
        ((DecimalFormat) pf).applyPattern("#,##0.00##%;'<tt>'(#,##0.00##%)'</tt>'");
        Stat<Double> stat = new Stat<Double>();
        Chrono chrono = new Chrono();
        Temporizador temporizador = new Temporizador(progressBar);
        okane = kptal;
        scorr = sjuros = jurosPending = jurosAno = ZERO;
        corrAcum = jurosAcum = ONE;
        final int DAJ = di.get(Calendar.DATE);
        final boolean skipFirst = (DateUtils.daysInMonth(di) == DAJ);
        final int GAP = opAnual.isSelected() ? 12 : 6;
        class Diem {

            private int year, month, day;

            private int n;

            private String dateStr;

            private boolean disabled;

            public Diem() {
                n = 0;
                disabled = false;
                next();
            }

            public void next() {
                if (disabled) return;
                Calendar c = (Calendar) di.clone();
                c.add(Calendar.MONTH, ++n * GAP);
                if (lenientDatesEnabled) {
                    c.setLenient(true);
                    c.set(Calendar.DATE, DAJ);
                }
                year = c.get(Calendar.YEAR);
                month = c.get(Calendar.MONTH);
                day = c.get(Calendar.DATE);
                dateStr = dateUtils.format(c);
                disabled = !c.before(df);
            }

            public int getDay() {
                return Diem.this.day;
            }

            public String asString() {
                return Diem.this.dateStr;
            }

            public boolean eligible(Calendar c) {
                return (c.get(Calendar.YEAR) == Diem.this.year) && (c.get(Calendar.MONTH) == Diem.this.month) && (c.get(Calendar.DATE) >= Diem.this.day);
            }
        }
        Diem diem = new Diem();
        Calendar data = (Calendar) di.clone();
        if (!skipFirst) data.add(Calendar.MONTH, -1);
        String q = buildSQL("valueFromDate", dbtables[ndx].name);
        try {
            PreparedStatement ps = connection.prepareStatement(q);
            temporizador.start();
            for (int m = (skipFirst ? 1 : 0); m < NUM_ITER; m++) {
                playAudioClip(6);
                chrono.start();
                k = m % 2;
                temporizador.update(((double) (m + 1)) / NUM_ITER);
                data.add(Calendar.MONTH, 1);
                final int daysInMonth = DateUtils.daysInMonth(data);
                final int lastDay = (m < NUM_ITER - 1) ? daysInMonth : df.get(Calendar.DATE);
                data.set(Calendar.DATE, 1);
                java.sql.Date date = new java.sql.Date(data.getTimeInMillis());
                if (useSQLite) ps.setString(1, date.toString()); else ps.setDate(1, date);
                ResultSet rs = ps.executeQuery();
                final BigDecimal txMensal = rs.next() ? new BigDecimal(rs.getString(1)) : null;
                rs.close();
                data.set(Calendar.DATE, lastDay);
                final int daysInYear = DateUtils.daysInYear(data);
                final int r = Reforma.search(data);
                final int dr = (r > 0) ? Reforma.reformas[r].getDay() : -1;
                if (validProRata && ((m > 0 && m % GAP == 0) || (m > 1 && m % GAP == 1)) && diem.eligible(data)) {
                    final int daj = diem.getDay();
                    if ((r > 0) && (dr < daj)) {
                        jcm(className[k], Reforma.reformas[r].getDate(), dr, txMensal, daysInMonth, daysInYear);
                        scale(r);
                        jcm(className[k], diem.asString(), daj - dr, txMensal, daysInMonth, daysInYear);
                    } else {
                        jcm(className[k], diem.asString(), daj, txMensal, daysInMonth, daysInYear);
                    }
                    kptal = kptal.add(jurosPending);
                    jurosPending = ZERO;
                    jurosAcum = jurosAcum.multiply(jurosAno.add(ONE));
                    jurosAno = ZERO;
                    mkRow(className[k], diem.asString(), "Agregação de Juros Remuneratórios");
                    if (daj <= lastDay) {
                        if ((r > 0) && (daj <= dr) && (dr <= lastDay)) {
                            if (dr > daj) {
                                jcm(className[k], Reforma.reformas[r].getDate(), dr - daj, txMensal, daysInMonth, daysInYear);
                            }
                            scale(r);
                            if (lastDay > dr) {
                                jcm(className[k], data, lastDay - dr, txMensal, daysInMonth, daysInYear);
                            }
                        } else {
                            if (lastDay > daj) {
                                jcm(className[k], data, lastDay - daj, txMensal, daysInMonth, daysInYear);
                            }
                        }
                    }
                    diem.next();
                } else {
                    if ((r > 0) && !(m == 0 && dr <= DAJ) && !(m == NUM_ITER - 1 && lastDay < dr)) {
                        jcm(className[k], Reforma.reformas[r].getDate(), ((m > 0) ? dr : dr - DAJ), txMensal, daysInMonth, daysInYear);
                        scale(r);
                        if (lastDay > dr) {
                            jcm(className[k], data, lastDay - dr, txMensal, daysInMonth, daysInYear);
                        }
                    } else {
                        jcm(className[k], data, ((m > 0) ? lastDay : lastDay - DAJ), txMensal, daysInMonth, daysInYear);
                    }
                }
                chrono.stop();
                stat.add(chrono.time());
            }
            ps.close();
        } catch (SQLException e) {
            System.err.println(e);
        }
        sw.format("<tr><td nowrap class=%s_center>TOTAL</td>", className[k]);
        q = String.format("<td class=%s></td>", className[k]);
        for (int i = 0; i < 3; i++) sw.write(q);
        String s = String.format("<td nowrap class=%s>%%s</td>", className[k]);
        sw.format(s, cf.format(scorr));
        sw.write(q);
        sw.format(s, cf.format(sjuros));
        sw.append(q).append(q).append("</tr>");
        String dfString = dateUtils.format(df);
        mkRow(className[k], dfString, "<strong>Valor Atualizado</strong>");
        sw.append("<tr><td colspan=9 class=rem>Padrão Monetário em ");
        sw.append(dfString).append(": <b>");
        sw.append(currency.getName()).append("</b> (<b>");
        sw.append(currency.getSymbol()).append("</b>).</td></tr>");
        boolean delayed = validProRata && (jurosPending.compareTo(ZERO) > 0);
        if (delayed) {
            if (jurosPending.compareTo(EPS) >= 0) {
                s = String.format("Juros Remuneratórios pendentes até %s", diem.asString());
            } else {
                s = String.format("Juros Remuneratórios pendentes até %s<br>menores que %s 0,01", diem.asString(), currency.getSymbol());
            }
            mkRow("xtra0", dfString, s, jurosPending);
            kptal = kptal.add(jurosPending);
            kptal = kptal.setScale(2, RoundingMode.HALF_UP);
            mkRow("xtra1", dfString, "Valor Atualizado + Juros Remuneratórios pendentes");
            jurosAcum = jurosAcum.multiply(jurosAno.add(ONE));
        }
        sw.write("</table></div>");
        pf.setMinimumFractionDigits(4);
        sw.write("<div><table cellpadding=5 cellspacing=1>");
        sw.write("<tr><th colspan=2 nowrap>Taxas Acumuladas no Período</td></tr>");
        sw.write("<tr><td class=one_left>Correção Monetária (<b>");
        sw.write(dbtables[ndx].name);
        sw.write("</b>)</td><td class=one>");
        sw.append(pf.format(corrAcum.subtract(ONE))).append("</td></tr>");
        sw.write("<tr><td class=two_left>Juros Remuneratórios</td><td class=two>");
        sw.append(pf.format(jurosAcum.subtract(ONE))).append("</td></tr>");
        sw.write("</table></div>");
        if (validProRata && (jurosAcum.compareTo(ONE) > 0) && (corrAcum.compareTo(ONE) != 0)) {
            sw.write("<div class=remark><table cellpadding=5><tr>");
            sw.write("<td nowrap valign=top><b>Observação</b>:</td><td>");
            sw.write("Sem Correção Monetária os Juros Remuneratórios seriam ");
            BigDecimal x = jurosAcum.divide(corrAcum, mc).subtract(ONE);
            sw.append(pf.format(x)).append(".</td></tr></table></div>");
        }
        pf.setMaximumFractionDigits(2);
        sw.write("<div><table cellpadding=5 cellspacing=1>");
        sw.write("<tr><th colspan=3 nowrap>Resumo dos Valôres Computados</td></tr>");
        sw.write("<tr><td class=one_left>Correção Monetária</td>");
        s = "<td class=one>%s</td>";
        sw.format(s, cf.format(scorr));
        boolean fault = (kptal.subtract(okane).compareTo(ZERO) <= 0);
        if (fault) sw.write("<td class=one_center>---</td>"); else {
            BigDecimal y = scorr.divide(kptal.subtract(okane), mc);
            sw.format(s, pf.format(y));
        }
        sw.write("</tr>");
        sw.write("<tr><td class=two_left>Juros Remuneratórios</td>");
        q = "<td class=two>%s</td>";
        sw.format(q, cf.format(kptal.subtract(scorr).subtract(okane)));
        if (fault) sw.write("<td class=two_center>---</td>"); else {
            BigDecimal y = kptal.subtract(okane);
            y = y.subtract(scorr).divide(y, mc);
            sw.format(q, pf.format(y));
        }
        sw.write("</tr>");
        sw.write("<tr><td class=one_center>TOTAL</td>");
        sw.format(s, cf.format(kptal.subtract(okane)));
        if (!fault) sw.write("<td class=one>100,00%</td>"); else sw.write("<td class=one_center>---</td>");
        sw.write("</tr>");
        sw.write("<tr><td class=two_left>Valor Inicial</td>");
        sw.format(q, cf.format(okane));
        sw.write("<td class=two_center>");
        sw.append(dateUtils.format(di)).append("</td></tr>");
        sw.write("<tr><td class=one_left>Valor Atualizado</td>");
        sw.format(s, cf.format(kptal));
        sw.write("<td class=one_center>");
        sw.append(dfString).append("</td></tr>");
        sw.write("<tr><td nowrap colspan=3 class=rem>");
        sw.write("Padrão Monetário: <b>");
        sw.append(currency.getName()).append("</b> (<b>");
        sw.append(currency.getSymbol()).append("</b>).</td></tr>");
        sw.write("</table></div>");
        sw.write("<p>Valor Atualizado");
        if (delayed) sw.append(" + Juros&nbsp;Remuneratórios pendentes");
        sw.append(": <b>").append(currency.getSymbol());
        sw.append("&nbsp;").append(cf.format(kptal));
        sw.append("</b>&nbsp;(").append(currency.getName()).append(")</p>");
        execChrono.stop();
        sw.write("<div><table cellpadding=2 cellspacing=1>");
        sw.write("<tr><td class=mer>Número de Iterações:</td><td class=rem>");
        sw.format("<b>%d</b></td></tr>", stat.getSize());
        sw.write("<tr><td class=mer>Tempo Total de Execução (s):</td>");
        sw.format(DateUtils.localeBR, "<td class=rem><b>%.3f", execChrono.time());
        sw.write("</b></td></tr>");
        sw.write("<tr><td class=mer>Tempo Total das Iterações (s):</td>");
        sw.format(DateUtils.localeBR, "<td class=rem><b>%.3f", stat.getTotal());
        sw.write("</b></td></tr>");
        sw.write("<tr><td class=mer>Tempo Médio / Iteração (s):</td>");
        sw.format(DateUtils.localeBR, "<td class=rem><b>%.3f", stat.getMean());
        sw.format(DateUtils.localeBR, " &#177; %.3f", stat.getStdDeviation());
        sw.write("</b></td></tr><tr><td colspan=2 class=rem><br>");
        sw.format(DateUtils.localeBR, "Gerado em %tc.", System.currentTimeMillis());
        sw.write("</td></tr></table></div></body></html>");
        sw.flush();
        clearReport();
        report.getEditorKit().createDefaultDocument();
        report.setContentType("text/html");
        report.setText(sw.toString());
        assureSplitPaneDisplay(true);
        report.requestFocusInWindow();
        temporizador.terminate();
        playAudioClip(2);
        setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
        adjustFormater(true);
        return true;
    }

    /**
   * Inicia o aplicativo.
   *
   * @param args Array de argumentos.
  */
    public static void main(String[] args) {
        splashHelper = new SplashHelper("Construindo a interface.");
        JCM f = new JCM();
        f.pack();
        splashHelper.close();
        f.setVisible(true);
    }

    /** Gerenciador do SplashScreen para qualquer versão de JRE. */
    private static SplashHelper splashHelper;

    /** Writer extendido para facilitar uso de formatadores. */
    private class CustomWriter extends StringWriter {

        /**
     * Escreve conteúdo no final do buffer com formatação para localidade
     * especificada.
     *
     * @param locale Localidade específica na formatação.
     * @param fmt Formato a ser aplicado nos argumentos.
     * @param args Argumentos a escrever no final do buffer.
    */
        public void format(Locale locale, String fmt, Object... args) {
            write(String.format(locale, fmt, args));
        }

        /**
     * Escreve conteúdo no final do buffer com formatação.
     *
     * @param fmt Formato a ser aplicado nos argumentos.
     * @param args Argumentos a escrever no final do buffer.
    */
        public void format(String fmt, Object... args) {
            write(String.format(fmt, args));
        }
    }

    /** Buffer de montagem do documento contendo a planilha. */
    private CustomWriter sw;

    /** Formatador de valores monetários da planilha. */
    private NumberFormat cf;

    /** Formatador de valores percentuais da planilha. */
    private NumberFormat pf;

    /** Montante objeto da atualização monetária. */
    private BigDecimal kptal;

    /** Montante preservado para comparação ao final da atualização. */
    private BigDecimal okane;

    /** Somatório das correções monetárias. */
    private BigDecimal scorr;

    /** Somatório dos juros. */
    private BigDecimal sjuros;

    /** Somatório dos juros pendentes até data de agregação. */
    private BigDecimal jurosPending;

    /** Taxa de correção monetária acumulada no período. */
    private BigDecimal corrAcum;

    /** Taxa de juros remuneratórios acumulado no período. */
    private BigDecimal jurosAcum;

    /** Taxa de juros remuneratórios acumulado no ano. */
    private BigDecimal jurosAno;

    /** PRO-RATA : Taxa de Juros Remuneratórios Anual a cada planilhamento. */
    private BigDecimal proRata;

    /** Efetividate de cálculos com a taxa PRO-RATA a cada planilhamento. */
    private boolean validProRata;

    /** Encapsulamento de vários métodos usuais de calendário. */
    private DateUtils dateUtils;

    /** Engine de pesquisa do padrão monetário nas datas. */
    private Moeda currency;

    /** Habilitação dos centavos conforme moeda da época. */
    private boolean useCents;

    /** Mínimo valor monetário que pode ser arredondado para um centavo. */
    private static final BigDecimal EPS = new BigDecimal("0.005");

    /**
   * Nome do arquivo de propriedades contendo todos os pares código/nome dos
   * índices de atualização monetária disponíveis no SGS do BCB.
  */
    private static final String INDICES_MENSAIS_PROPERTIES = "indicesmensais.properties";

    /**
   * Nome do arquivo de propriedades contendo os pares nome/código dos índices de
   * atualização monetária em uso corrente que são atualizáveis online.
  */
    private static final String DBUPDATE_PROPERTIES = "dbupdate.properties";

    /**
   * Nome do arquivo de propriedades contendo os pares usuário/senha
   * para acesso ao banco de dados marca Derby ou MySQL.
  */
    private static final String JCMDB_PROPERTIES = "jcmdb.properties";

    private static final long serialVersionUID = -1759470129035495977L;
}
