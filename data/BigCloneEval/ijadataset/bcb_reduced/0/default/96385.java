import java.io.Reader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import javax.swing.text.html.HTMLDocument.Iterator;

public class Avaliador {

    static int codigoInst;

    static int codigoTipoCurso;

    static int codigoCurso;

    static String codigoDisc;

    static String codigoTurma;

    static int idAval;

    static String cdUsuario = "";

    static int cdSolucao;

    static int cdConteudo;

    static String cdUsuarioAluno = "";

    static int cdSolucaoAluno;

    static int cdRespostaAluno;

    /******************************/
    static String serverName = "";

    static String mydatabase = "";

    static String url = "";

    static String username = "";

    static String password = "";

    static Connection connection = null;

    static ArrayList<Aluno> listaAluno = null;

    static ArrayList<Questao> listaQuestao = null;

    static ArrayList<BaseResposta> listaResposta = null;

    static int codigoProb;

    static double average = 0;

    static double dP = 0;

    private static String resultado = "";

    private static String notaNormalAluno = "";

    private static String respostaTeste = "";

    private static Aluno[] vetAluno = null;

    private static String strProblema = "";

    private static String strAval = "";

    private static String stringNotas = "aaaa";

    private static String stringRespostas = "";

    private static String respAluno = "";

    public Avaliador() {
    }

    public String gerarResposta() {
        conectaBanco();
        setInst(2);
        setTipoCurso(1);
        setCurso(4);
        setDisc("EN05064");
        setTurma("100010");
        setProblema(64);
        setCdUsuario("1152675");
        setCdSol(1);
        geraRespostaModelo();
        return resultado;
    }

    public void setInst(int cdInst) {
        codigoInst = cdInst;
    }

    public int getInst() {
        return codigoInst;
    }

    public void setTipoCurso(int cdTipoCurso) {
        codigoTipoCurso = cdTipoCurso;
    }

    public void setRespostaAluno(String respostaAluno) {
        respAluno = respostaAluno;
    }

    public int getTipoCurso() {
        return codigoTipoCurso;
    }

    public String getTurma() {
        return codigoTurma;
    }

    public void setCurso(int cdCurso) {
        codigoCurso = cdCurso;
    }

    public int getCurso() {
        return codigoCurso;
    }

    public void setDisc(String cdDisc) {
        codigoDisc = cdDisc;
    }

    public void setStrProblema(String str) {
        strProblema = str;
    }

    public void setStrAval(String strAvaliacao) {
        strAval = strAvaliacao;
    }

    public String getStrProblema() {
        return strProblema;
    }

    public String getStrAval() {
        return strAval;
    }

    public String getDisc() {
        return codigoDisc;
    }

    public void setTurma(String cdTurma) {
        codigoTurma = cdTurma;
    }

    public void setAval(int idAvaliacao) {
        idAval = idAvaliacao;
    }

    public int getAval() {
        return idAval;
    }

    public void setProblema(int problema) {
        codigoProb = problema;
    }

    public int getProblema() {
        return codigoProb;
    }

    public void setCdUsuario(String codigoUsuario) {
        cdUsuario = codigoUsuario;
    }

    public String getCdUsuario() {
        return cdUsuario;
    }

    public void setCdUsuarioAluno(String codigoUsuarioAluno) {
        cdUsuarioAluno = codigoUsuarioAluno;
    }

    public String getCdUsuarioAluno() {
        return cdUsuarioAluno;
    }

    public void setCdSol(int codigoSolucao) {
        cdSolucao = codigoSolucao;
    }

    public void setCdSolAluno(int codigoSolucaoAluno) {
        cdSolucaoAluno = codigoSolucaoAluno;
    }

    public int getCdSolAluno() {
        return cdSolucaoAluno;
    }

    public void setCdResposta(int codigoResposta) {
        cdRespostaAluno = codigoResposta;
    }

    public int getCdResposta() {
        return cdRespostaAluno;
    }

    public void setCdConteudo(int codigoConteudo) {
        cdConteudo = codigoConteudo;
    }

    public int getCdConteudo() {
        return cdConteudo;
    }

    public String notasAlunos() {
        return stringNotas;
    }

    public String respostasAlunos() {
        return stringRespostas;
    }

    public void avaliaTurma() {
        respAluno = "";
        listaAluno = null;
        listaQuestao = null;
        listaResposta = null;
        vetAluno = null;
        vetAluno = new Aluno[1000];
        conectaBanco();
        stringNotas = "";
        String prob = "";
        for (int i = 0; i < strProblema.length(); i++) {
            if (strProblema.charAt(i) == '.') {
                int problema = Integer.parseInt(prob);
                setProblema(problema);
                contadorVetAlunos = 0;
                String avaliacao = "";
                for (int j = 0; j < strAval.length(); j++) {
                    if (strAval.charAt(j) == '.') {
                        int aval = Integer.parseInt(avaliacao);
                        setAval(aval);
                        avaliacao = "";
                        setStrAval(strAval.substring(j + 1));
                        break;
                    } else {
                        avaliacao += strAval.charAt(j);
                    }
                }
                criaListaAluno();
                if (listaAluno.size() > 0) {
                    criaListaQuestao();
                    avaliaQuestao();
                }
                prob = "";
            } else {
                prob += strProblema.charAt(i);
            }
        }
        listaAluno = null;
        listaQuestao = null;
        listaResposta = null;
        vetAluno = null;
    }

    public void geraRespostaModelo() {
        respAluno = "";
        listaAluno = null;
        listaQuestao = null;
        listaResposta = null;
        vetAluno = null;
        vetAluno = new Aluno[1000];
        conectaBanco();
        criaListaAluno();
        criaListaQuestao();
        criaListaResposta();
        if (listaResposta.size() > 0) {
            respostaModelo();
            calculaMediaDesvio(codigoProb, connection);
        } else notaNormalAluno = "quantidade 0";
        listaAluno = null;
        listaQuestao = null;
        listaResposta = null;
        vetAluno = null;
    }

    public String respostaFinal() {
        return resultado;
    }

    static String queryTeste = "";

    public String notaNormalAluno() {
        return notaNormalAluno;
    }

    public String respTeste() {
        return respostaTeste;
    }

    public void atualizaNotaAluno() {
        listaAluno = null;
        listaQuestao = null;
        listaResposta = null;
        vetAluno = null;
        vetAluno = new Aluno[1000];
        conectaBanco();
        criaListaAvalAluno();
        if (listaAluno.size() > 0) {
            criaListaQuestao();
            avalAluno();
        } else {
        }
        listaAluno = null;
        listaQuestao = null;
        listaResposta = null;
        vetAluno = null;
    }

    public static String atualizaRespostaTop() {
        String respostaJoin = "";
        for (java.util.Iterator<Questao> iter = listaQuestao.iterator(); iter.hasNext(); ) {
            Questao questao = (Questao) iter.next();
            int cont;
            cont = frequencia(questao.getCdProb(), connection);
            System.out.println("questao " + questao.getCdProb() + " " + cont);
            System.out.println("Solucao " + questao.getDescSol());
            if (questao.getDescSol() != "") {
                avalQAlu(questao.getCdProb(), questao.getDescSol(), connection);
                if (cont > 10) {
                    if (cont > 30) {
                        respostaJoin = updateNTop_RespTop(connection, questao.getCdProb(), 5, questao.getDescSol());
                    } else if (cont > 20) {
                        respostaJoin = updateNTop_RespTop(connection, questao.getCdProb(), 4, questao.getDescSol());
                    } else if (cont > 10) {
                        respostaJoin = updateNTop_RespTop(connection, questao.getCdProb(), 2, questao.getDescSol());
                    }
                    AvalContraResp(connection, questao.getCdProb(), respostaJoin);
                    calculaMediaDesvio(questao.getCdProb(), connection);
                    System.out.println();
                    System.out.println("media " + questao.getMedia());
                    System.out.println("desvio " + questao.getDesvio());
                    System.out.println();
                    System.out.println("exc " + questao.getExc());
                    System.out.println("bom " + questao.getBom());
                    System.out.println("reg " + questao.getReg());
                    System.out.println();
                    conceitoAluno(questao.getCdProb(), questao.getExc(), questao.getBom(), questao.getReg());
                    atualizaBanco(connection, questao.getCdProb(), questao.getExc(), questao.getBom(), questao.getReg());
                }
            }
        }
        return respostaJoin;
    }

    static void avaliaQuestao() {
        avalAluno();
    }

    static void avalAluno() {
        for (java.util.Iterator<Questao> iter = listaQuestao.iterator(); iter.hasNext(); ) {
            Questao questao = (Questao) iter.next();
            String query = "select * from IETS_PHP.iets_usuario_solucao where cdInst = ? and cdTipoCurso = ?" + " and cdCurso = ? and cdDisc = ? and cdTurma = ? and cdUsr = ? and cdProb = ? and cdSol = ?";
            PreparedStatement prepStat;
            try {
                prepStat = connection.prepareStatement(query);
                prepStat.setInt(1, codigoInst);
                prepStat.setInt(2, codigoTipoCurso);
                prepStat.setInt(3, codigoCurso);
                prepStat.setString(4, codigoDisc);
                prepStat.setString(5, codigoTurma);
                prepStat.setString(6, cdUsuario);
                prepStat.setInt(7, codigoProb);
                prepStat.setInt(8, cdSolucao);
                ResultSet rs = prepStat.executeQuery();
                String solucao = "";
                while (rs.next()) {
                    solucao = rs.getString(12);
                    break;
                }
                if (solucao.equals("")) {
                } else {
                    System.out.println("solucao ==== > " + solucao);
                    avalQAlu(questao.getCdProb(), solucao, connection);
                    normalizaNotaAluno(rs.getDouble(15), rs.getDouble(14), rs.getDouble(13), rs.getDouble(16), rs.getDouble(17));
                    System.out.println("contadorVetAlunos " + contadorVetAlunos);
                    for (int i = 0; i < contadorVetAlunos; i++) {
                        prepStat = connection.prepareStatement("UPDATE iets_usrturma_conteudo_problema_resposta SET" + " pontosObtidos = ? WHERE cdInst = ? AND cdTipoCurso = ? AND cdCurso = ? " + "AND cdDisc = ? AND cdTurma = ? AND cdUsr = ? and cdConteudo = ? AND cdProb = ?" + " and cdSol = ?  AND idAval = ? and cdResposta = ?");
                        Aluno aluno = vetAluno[i];
                        System.out.print("i ==> " + i);
                        if (vetAluno[i] != null) {
                            stringRespostas += aluno.getCdUsr() + "==> " + aluno.getDescResp() + "||";
                            System.out.println("Nota normalizada " + aluno.getNota());
                            prepStat.setDouble(1, vetAluno[i].getNota());
                            prepStat.setInt(2, codigoInst);
                            prepStat.setInt(3, codigoTipoCurso);
                            prepStat.setInt(4, codigoCurso);
                            prepStat.setString(5, codigoDisc);
                            prepStat.setString(6, codigoTurma);
                            prepStat.setString(7, aluno.getCdUsr());
                            prepStat.setInt(8, aluno.getCdConteudo());
                            prepStat.setInt(9, codigoProb);
                            prepStat.setInt(10, aluno.getCdSol());
                            prepStat.setInt(11, idAval);
                            prepStat.setInt(12, aluno.getCdResp());
                            prepStat.executeUpdate();
                        }
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    static void respostaModelo() {
        for (java.util.Iterator<Questao> iter = listaQuestao.iterator(); iter.hasNext(); ) {
            Questao questao = (Questao) iter.next();
            int cont;
            cont = frequencia(questao.getCdProb(), connection);
            String respostaJoin = "";
            System.out.println("questao " + questao.getCdProb() + " " + cont);
            System.out.println("Solucao " + questao.getDescSol());
            if (questao.getDescSol() != "") {
                avalBaseResposta(questao.getCdProb(), questao.getDescSol(), connection);
                if (cont > 10) {
                    if (cont > 30) {
                        respostaJoin += updateNTop_RespTop(connection, questao.getCdProb(), 5, questao.getDescSol());
                    } else if (cont > 20) {
                        respostaJoin += updateNTop_RespTop(connection, questao.getCdProb(), 4, questao.getDescSol());
                    } else if (cont > 10) {
                        respostaJoin += updateNTop_RespTop(connection, questao.getCdProb(), 2, questao.getDescSol());
                    }
                } else respostaJoin += updateNTop_RespTop(connection, questao.getCdProb(), 0, questao.getDescSol());
                System.out.println(" bom ** " + questao.getBom() + " exc ** " + questao.getExc() + " reg** " + questao.getReg());
                resultado = respostaJoin;
            }
        }
    }

    static int frequencia(int cdProblema, Connection conexao) {
        int count = 0;
        ArrayList<BaseResposta> temp = new ArrayList<BaseResposta>();
        temp = listaResposta;
        for (java.util.Iterator<BaseResposta> iter = temp.iterator(); iter.hasNext(); ) {
            BaseResposta resposta = (BaseResposta) iter.next();
            count += 1;
        }
        return count;
    }

    static void AvalContraResp(Connection conexao, int codigoProblema, String respostaTop) {
        for (java.util.Iterator<Aluno> iter = listaAluno.iterator(); iter.hasNext(); ) {
            Aluno aluno = (Aluno) iter.next();
            if ((aluno.getDescResp() != "")) {
                StemmerPl stPl = new StemmerPl();
                double sum = stPl.soma(respostaTop, aluno.getDescResp());
                aluno.setNota(sum);
            }
        }
    }

    static void avalBaseResposta(int cdProb, String descSolucao, Connection conexao) {
        for (java.util.Iterator<BaseResposta> iter = listaResposta.iterator(); iter.hasNext(); ) {
            BaseResposta resposta = (BaseResposta) iter.next();
            if (resposta.getDescResp() != "") {
                StemmerPl stPl = new StemmerPl();
                double sum = stPl.soma(descSolucao, resposta.getDescResp());
                resposta.setNota(sum);
            }
        }
    }

    static void ordenaListaAluno() {
        Collections.sort(listaAluno, new Comparator() {

            public int compare(Object o1, Object o2) {
                Aluno p1 = (Aluno) o1;
                Aluno p2 = (Aluno) o2;
                return p1.nota < p2.nota ? +1 : (p1.nota > p2.nota ? -1 : 0);
            }
        });
    }

    static void selectionSort(int size, BaseResposta[] vetBaseResp) {
        int i, j, min;
        BaseResposta temp;
        for (j = 0; j < size - 1; j++) {
            min = j;
            for (i = j + 1; i < size; i++) if (vetBaseResp[i].nota > vetBaseResp[min].nota) min = i;
            if (j != min) {
                temp = vetBaseResp[j];
                vetBaseResp[j] = vetBaseResp[min];
                vetBaseResp[min] = temp;
            }
        }
    }

    static int contadorVetAlunos = 0;

    static void avalQAlu(int cdProb, String descSolucao, Connection conexao) {
        int contVetAluno = 0;
        double[] vetNotaAluno = new double[1000];
        for (java.util.Iterator<Aluno> iter = listaAluno.iterator(); iter.hasNext(); ) {
            Aluno aluno = (Aluno) iter.next();
            if (aluno != null) {
                respostaTeste = aluno.getDescResp();
                if ((aluno.getDescResp() != "") && (aluno.getCdProb() == cdProb)) {
                    System.out.println("getDescResp " + aluno.getDescResp());
                    StemmerPl stPl = new StemmerPl();
                    double sum = 0;
                    if (respAluno.equals("")) {
                        sum = stPl.soma(descSolucao, respostaTeste);
                    } else sum = stPl.soma(descSolucao, respAluno);
                    aluno.setNota(sum);
                    stringNotas += aluno.cdUsr + " ==>" + String.valueOf(aluno.getNota()) + "||";
                    vetAluno[contVetAluno] = aluno;
                    vetNotaAluno[contVetAluno] = aluno.getNota();
                    contVetAluno += 1;
                }
            }
        }
        int i, j, min;
        double tempNota;
        Aluno temp2;
        for (j = 0; j < contVetAluno - 1 - 1; j++) {
            min = j;
            for (i = j + 1; i < contVetAluno - 1; i++) if (vetNotaAluno[i] > vetNotaAluno[min]) min = i;
            if (j != min) {
                temp2 = vetAluno[j];
                vetAluno[j] = vetAluno[min];
                vetAluno[min] = temp2;
                tempNota = vetNotaAluno[j];
                vetNotaAluno[j] = vetNotaAluno[min];
                vetNotaAluno[min] = tempNota;
            }
        }
        for (java.util.Iterator<Aluno> iter = listaAluno.iterator(); iter.hasNext(); ) {
            Aluno aluno = (Aluno) iter.next();
            if (aluno != null) {
                System.out.println("nota " + aluno.getNota());
                contadorVetAlunos += 1;
            }
        }
    }

    static String JoinTopN(int N, int codigoProblema, String respostaProfessor) {
        String joinResp = "";
        BaseResposta[] vetBaseResp = new BaseResposta[10000];
        int contAux = 0;
        ArrayList<BaseResposta> temp = new ArrayList<BaseResposta>();
        temp = listaResposta;
        double[] vetNota = new double[1000];
        String[] vetResposta = new String[1000];
        for (java.util.Iterator<BaseResposta> iter = temp.iterator(); iter.hasNext(); ) {
            BaseResposta resposta = (BaseResposta) iter.next();
            vetBaseResp[contAux] = resposta;
            vetNota[contAux] = resposta.getNota();
            vetResposta[contAux] = resposta.getDescResp();
            contAux += 1;
        }
        int i, j, min;
        double tempNota;
        String tempResp;
        BaseResposta temp2;
        for (j = 0; j < contAux - 1 - 1; j++) {
            min = j;
            for (i = j + 1; i < contAux - 1; i++) if (vetNota[i] > vetNota[min]) min = i;
            if (j != min) {
                temp2 = vetBaseResp[j];
                vetBaseResp[j] = vetBaseResp[min];
                vetBaseResp[min] = temp2;
                tempNota = vetNota[j];
                vetNota[j] = vetNota[min];
                vetNota[min] = tempNota;
                tempResp = vetResposta[j];
                vetResposta[j] = vetResposta[min];
                vetResposta[min] = tempResp;
            }
        }
        contAux = 0;
        for (i = 0; i < listaResposta.size(); i++) {
            listaResposta.set(i, vetBaseResp[contAux]);
            contAux += 1;
        }
        int cont = 0;
        int cont2 = 0;
        joinResp += respostaProfessor;
        while (cont < N) {
            joinResp += vetResposta[cont2];
            if (cont != (N - 1)) {
                joinResp += " ";
            }
            cont += 1;
            cont2 += 1;
        }
        return joinResp;
    }

    static String updateNTop_RespTop(Connection conexao, int codigoProblema, int N, String respostaProfessor) {
        PreparedStatement stmt;
        String respJoin = "";
        try {
            stmt = conexao.prepareStatement("UPDATE iets_usuario_solucao SET nTop = ? WHERE cdInst = ? AND cdTipoCurso = ? AND cdCurso = ? AND cdDisc = ? AND cdTurma = ? AND cdProb = ? AND cdUsr = ? AND cdSol = ?");
            stmt.setInt(1, N);
            stmt.setInt(2, codigoInst);
            stmt.setInt(3, codigoTipoCurso);
            stmt.setInt(4, codigoCurso);
            stmt.setString(5, codigoDisc);
            stmt.setString(6, codigoTurma);
            stmt.setInt(7, codigoProblema);
            stmt.setString(8, cdUsuario);
            stmt.setInt(9, cdSolucao);
            stmt.executeUpdate();
            respJoin = JoinTopN(N, codigoProblema, respostaProfessor);
        } catch (SQLException e) {
        }
        System.out.println();
        System.out.println("RESPOSTA TOP");
        resultado = respJoin;
        System.out.println(respJoin);
        System.out.println();
        try {
            stmt = conexao.prepareStatement("UPDATE iets_usuario_solucao SET respTop = ? WHERE cdInst = ? AND cdTipoCurso = ? AND cdCurso = ? AND cdDisc = ? AND cdTurma = ? AND cdProb = ? AND cdUsr = ? AND cdSol = ?");
            stmt.setString(1, respJoin);
            stmt.setInt(2, codigoInst);
            stmt.setInt(3, codigoTipoCurso);
            stmt.setInt(4, codigoCurso);
            stmt.setString(5, codigoDisc);
            stmt.setString(6, codigoTurma);
            stmt.setInt(7, codigoProblema);
            stmt.setString(8, cdUsuario);
            stmt.setInt(9, cdSolucao);
            stmt.executeUpdate();
        } catch (SQLException e) {
        }
        return respJoin;
    }

    static void calculaMediaDesvio(int codigoProblema, Connection conexao) {
        System.out.println("aaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
        double soma = 0;
        double media = 0;
        int contAlunos = 0;
        for (java.util.Iterator<BaseResposta> iter = listaResposta.iterator(); iter.hasNext(); ) {
            BaseResposta resposta = (BaseResposta) iter.next();
            soma += resposta.getNota();
            contAlunos += 1;
        }
        if ((soma != 0) && (contAlunos != 0)) {
            media = soma / contAlunos;
        }
        average = media;
        System.out.println("media " + average);
        PreparedStatement stmt;
        try {
            stmt = conexao.prepareStatement("UPDATE iets_usuario_solucao SET media = ? WHERE cdInst = ? AND cdTipoCurso = ? AND cdCurso = ? AND cdDisc = ? AND cdTurma = ? AND cdProb = ? AND cdUsr = ? AND cdSol = ?");
            stmt.setDouble(1, media);
            stmt.setInt(2, codigoInst);
            stmt.setInt(3, codigoTipoCurso);
            stmt.setInt(4, codigoCurso);
            stmt.setString(5, codigoDisc);
            stmt.setString(6, codigoTurma);
            stmt.setInt(7, codigoProblema);
            stmt.setString(8, cdUsuario);
            stmt.setInt(9, cdSolucao);
            stmt.executeUpdate();
            double somaX = 0;
            for (java.util.Iterator<BaseResposta> iter = listaResposta.iterator(); iter.hasNext(); ) {
                BaseResposta resposta = (BaseResposta) iter.next();
                somaX += (resposta.getNota() - media) * (resposta.getNota() - media);
            }
            double desvio = 0;
            if ((somaX != 0) && (contAlunos != 0)) {
                desvio = Math.sqrt(somaX / contAlunos);
            }
            dP = desvio;
            System.out.println("desvio " + dP);
            stmt = conexao.prepareStatement("UPDATE iets_usuario_solucao SET desvio = ? WHERE cdInst = ? AND cdTipoCurso = ? AND cdCurso = ? AND cdDisc = ? AND cdTurma = ? AND cdProb = ? AND cdUsr = ? AND cdSol = ?");
            stmt.setDouble(1, desvio);
            stmt.setInt(2, codigoInst);
            stmt.setInt(3, codigoTipoCurso);
            stmt.setInt(4, codigoCurso);
            stmt.setString(5, codigoDisc);
            stmt.setString(6, codigoTurma);
            stmt.setInt(7, codigoProblema);
            stmt.setString(8, cdUsuario);
            stmt.setInt(9, cdSolucao);
            stmt.executeUpdate();
            calculaConceito(codigoProblema, media, desvio);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    static void normalizaNotaAluno(double exc, double bom, double reg, double mediaNota, double desvioNota) {
        System.out.println("exc " + exc + " bom " + bom + " reg " + reg);
        average = mediaNota;
        dP = desvioNota;
        System.out.println("Media  " + average + "  desvio " + dP);
        for (int i = 0; i < contadorVetAlunos; i++) {
            Aluno aluno = vetAluno[i];
            if (vetAluno[i] != null) {
                double max = 0;
                double min = 0;
                double notaNorm = 0;
                if (aluno.getNota() > exc) {
                    max = average + 3 * dP;
                    min = exc;
                    notaNorm = (((aluno.getNota() - min) * (10 - 9)) / (max - min) + 9);
                    if (notaNorm > 10) {
                        notaNorm = 10;
                    }
                } else if (aluno.getNota() > bom) {
                    max = exc;
                    min = bom;
                    notaNorm = (((aluno.getNota() - min) * (9 - 7)) / (max - min)) + 7;
                } else if (aluno.getNota() > reg) {
                    max = bom;
                    min = reg;
                    notaNorm = (((aluno.getNota() - min) * (7 - 5)) / (max - min)) + 5;
                } else {
                    max = reg;
                    min = 0;
                    notaNorm = (((aluno.getNota() - min) * 3) / (max - min));
                    notaNorm = (notaNorm == 0 ? notaNorm : notaNorm + 3);
                }
                aluno.setNota(notaNorm);
                vetAluno[i] = aluno;
                System.out.println("nota normalizada " + aluno.getNota() + " conceito " + aluno.getConceito() + " " + aluno.getCdUsr());
                double notaTemp = aluno.getNota();
                notaNormalAluno = String.valueOf(notaTemp);
            }
        }
    }

    static void calculaConceito(int codigoProblema, double media, double desvio) {
        for (java.util.Iterator<Questao> iter = listaQuestao.iterator(); iter.hasNext(); ) {
            Questao questao = (Questao) iter.next();
            questao.setMedia(media);
            questao.setDesvio(desvio);
            double aux = media + desvio;
            questao.setExc(aux);
            System.out.println("exc " + questao.getExc());
            aux = media - desvio / 2;
            questao.setBom(aux);
            System.out.println("bom " + questao.getBom());
            aux = media - desvio;
            questao.setReg(aux);
            System.out.println("reg " + questao.getReg());
            PreparedStatement stmt;
            try {
                stmt = connection.prepareStatement("UPDATE iets_usuario_solucao SET exc = ?, bom = ? , reg = ? WHERE cdInst = ? AND cdTipoCurso = ? AND cdCurso = ? AND cdDisc = ? AND cdTurma = ? AND cdProb = ? AND cdUsr = ? AND cdSol = ?");
                stmt.setDouble(1, questao.getExc());
                stmt.setDouble(2, questao.getBom());
                stmt.setDouble(3, questao.getReg());
                stmt.setInt(4, codigoInst);
                stmt.setInt(5, codigoTipoCurso);
                stmt.setInt(6, codigoCurso);
                stmt.setString(7, codigoDisc);
                stmt.setString(8, codigoTurma);
                stmt.setInt(9, codigoProblema);
                stmt.setString(10, cdUsuario);
                stmt.setInt(11, cdSolucao);
                stmt.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    static void conceitoAluno(int codigoProblema, double exc, double bom, double reg) {
        for (java.util.Iterator<Aluno> iter = listaAluno.iterator(); iter.hasNext(); ) {
            Aluno aluno = (Aluno) iter.next();
            if ((exc == bom) && (bom == reg)) {
                aluno.setConceito("exc");
            } else if (exc == bom) {
                aluno.setConceito("exc");
            } else if (bom == reg) {
                aluno.setConceito("bom");
            } else {
                if (aluno.getNota() > exc) {
                    aluno.setConceito("exc");
                } else if (aluno.getNota() > bom) {
                    aluno.setConceito("bom");
                } else if (aluno.getNota() > reg) {
                    aluno.setConceito("reg");
                } else {
                    aluno.setConceito("ins");
                }
            }
            System.out.println(aluno.getNota() + " " + aluno.getConceito());
        }
    }

    static void criaListaAvalAluno() {
        listaAluno = new ArrayList<Aluno>();
        String query = "SELECT * FROM IETS_PHP.iets_usrturma_conteudo_problema_resposta WHERE cdInst = ? AND cdTipoCurso = ?" + " AND cdCurso = ? AND cdDisc = ? AND cdTurma = ? AND idAval = ? AND cdProb = ?   and cdUsr = ? and cdConteudo = ? and cdSol = ? and cdResposta = ? and cdProb in (Select cdProb from iets_problema  Where tipoProb = 'R') ";
        queryTeste = query;
        PreparedStatement prepStat;
        try {
            prepStat = connection.prepareStatement(query);
            prepStat.setInt(1, codigoInst);
            System.out.println(codigoInst);
            prepStat.setInt(2, codigoTipoCurso);
            System.out.println(codigoTipoCurso);
            prepStat.setInt(3, codigoCurso);
            System.out.println(codigoCurso);
            prepStat.setString(4, codigoDisc);
            System.out.println(codigoDisc);
            prepStat.setString(5, codigoTurma);
            System.out.println(codigoTurma);
            prepStat.setInt(6, idAval);
            System.out.println(idAval);
            prepStat.setInt(7, codigoProb);
            System.out.println(codigoProb);
            prepStat.setString(8, cdUsuarioAluno);
            System.out.println(cdUsuarioAluno);
            prepStat.setInt(9, cdConteudo);
            System.out.println(cdConteudo);
            prepStat.setInt(10, cdSolucaoAluno);
            System.out.println(cdSolucaoAluno);
            prepStat.setInt(11, cdRespostaAluno);
            System.out.println(cdRespostaAluno);
            queryTeste = String.valueOf(codigoInst) + " " + String.valueOf(codigoTipoCurso) + " " + String.valueOf(codigoCurso);
            queryTeste += codigoDisc + " " + codigoTurma + " " + String.valueOf(idAval) + " ";
            queryTeste += String.valueOf(codigoProb) + " " + cdUsuarioAluno + " " + String.valueOf(cdConteudo) + " ";
            queryTeste += String.valueOf(cdSolucaoAluno) + " " + String.valueOf(cdRespostaAluno) + " " + respAluno;
            ResultSet rs = prepStat.executeQuery();
            int count = 0;
            int count2 = 0;
            while (rs.next()) {
                int cdProb = rs.getInt(8);
                if (cdProb != -1) {
                    String cdUsr = rs.getString(6);
                    int cdResp = rs.getInt(11);
                    int idAval = rs.getInt(10);
                    int idConteudo = rs.getInt(7);
                    int idSolucao = rs.getInt(9);
                    String descRespAluno = rs.getString(12);
                    Aluno aluno = new Aluno(cdUsr, cdProb, cdResp, idAval, descRespAluno, idConteudo, idSolucao);
                    listaAluno.add(aluno);
                    ++count2;
                }
                ++count;
            }
            rs.close();
            prepStat.close();
            System.out.println("contAlunos " + count2);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    static void criaListaAluno() {
        listaAluno = new ArrayList<Aluno>();
        String query = "SELECT * FROM IETS_PHP.iets_usrturma_conteudo_problema_resposta WHERE cdInst = ? AND cdTipoCurso = ?" + " AND cdCurso = ? AND cdDisc = ? AND cdTurma = ? AND idAval = ? AND cdProb = ? and cdProb in (Select cdProb from iets_problema  Where tipoProb = 'R') ";
        PreparedStatement prepStat;
        try {
            prepStat = connection.prepareStatement(query);
            prepStat.setInt(1, codigoInst);
            prepStat.setInt(2, codigoTipoCurso);
            prepStat.setInt(3, codigoCurso);
            prepStat.setString(4, codigoDisc);
            prepStat.setString(5, codigoTurma);
            prepStat.setInt(6, idAval);
            prepStat.setInt(7, codigoProb);
            ResultSet rs = prepStat.executeQuery();
            int count = 0;
            int count2 = 0;
            while (rs.next()) {
                System.out.println("PPPP");
                int cdProb = rs.getInt(8);
                if (cdProb != -1) {
                    String cdUsr = rs.getString(6);
                    int cdResp = rs.getInt(11);
                    int idAval = rs.getInt(10);
                    int idConteudo = rs.getInt(7);
                    int idSolucao = rs.getInt(9);
                    String descRespAluno = rs.getString(12);
                    Aluno aluno = new Aluno(cdUsr, cdProb, cdResp, idAval, descRespAluno, idConteudo, idSolucao);
                    listaAluno.add(aluno);
                    ++count2;
                }
                ++count;
            }
            rs.close();
            prepStat.close();
            System.out.println("contAlunos " + count2);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    static void criaListaQuestao() {
        listaQuestao = new ArrayList<Questao>();
        String query = " Select A.cdProb, B.descProb, A.descSol  from IETS_PHP.iets_usuario_solucao  A " + " join IETS_PHP.iets_problema B on A.cdProb=B.cdProb" + " Where B.tipoProb='R' and A.cdProb=" + codigoProb + " and A.cdUsr=" + cdUsuario + " and A.cdSol=" + cdSolucao;
        try {
            PreparedStatement prepStat = connection.prepareStatement(query);
            ResultSet rs = prepStat.executeQuery(query);
            System.out.println(rs.getRow());
            while (rs.next()) {
                System.out.println(query);
                String descSol = "";
                int cdProb = rs.getInt(1);
                String descProb = rs.getString(2);
                descSol = rs.getString(3);
                Questao questao = new Questao(cdProb, descProb, descSol);
                listaQuestao.add(questao);
                break;
            }
            rs.close();
            prepStat.close();
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    public static void criaListaResposta() {
        listaResposta = new ArrayList<BaseResposta>();
        String query = "SELECT * FROM IETS_PHP.iets_usrturma_conteudo_problema_resposta WHERE cdProb = ?" + " and cdProb in (Select cdProb from iets_problema  Where tipoProb = 'R')";
        PreparedStatement prepStat;
        try {
            prepStat = connection.prepareStatement(query);
            prepStat.setInt(1, codigoProb);
            ResultSet rs = prepStat.executeQuery();
            int count = 0;
            int count2 = 0;
            while (rs.next()) {
                if (codigoProb != -1) {
                    String descRespAluno = rs.getString(12);
                    BaseResposta resposta = new BaseResposta(descRespAluno);
                    listaResposta.add(resposta);
                    ++count2;
                }
                ++count;
            }
            rs.close();
            prepStat.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    static void conectaBanco() {
        try {
            String driverName = "org.gjt.mm.mysql.Driver";
            Class.forName(driverName);
            String serverName = "localhost";
            String mydatabase = "IETS_PHP";
            String url = "jdbc:mysql://" + serverName + "/" + mydatabase;
            String username = "iets";
            String password = "bg5ad3";
            connection = DriverManager.getConnection(url, username, password);
        } catch (ClassNotFoundException e) {
            System.out.println("O driver expecificado não foi encontrado.");
            resultado = "O driver expecificado não foi encontrado.";
        } catch (SQLException e) {
            System.out.println("N�o foi poss�vel conectar ao Banco de Dados");
            resultado = "N�o foi poss�vel conectar ao Banco de Dados";
        }
    }

    static void atualizaBanco(Connection conexao, int codigoProblema, double exc, double bom, double reg) {
        PreparedStatement stmt;
        try {
            stmt = conexao.prepareStatement("UPDATE iets_usuario_solucao SET reg = ?, bom = ?, exc = ? WHERE cdInst = ? AND cdTipoCurso = ? AND cdCurso = ? AND cdDisc = ? AND cdTurma = ? AND cdProb = ? AND cdUsr = ? AND cdSol = ?");
            stmt.setDouble(1, reg);
            stmt.setDouble(2, bom);
            stmt.setDouble(3, exc);
            stmt.setInt(4, codigoInst);
            stmt.setInt(5, codigoTipoCurso);
            stmt.setInt(6, codigoCurso);
            stmt.setString(7, codigoDisc);
            stmt.setString(8, codigoTurma);
            stmt.setInt(9, codigoProblema);
            stmt.setString(10, cdUsuario);
            stmt.setInt(11, cdSolucao);
            stmt.executeUpdate();
            for (java.util.Iterator<Aluno> iter = listaAluno.iterator(); iter.hasNext(); ) {
                Aluno aluno = (Aluno) iter.next();
                stmt = conexao.prepareStatement("UPDATE iets_usrturma_conteudo_problema_resposta SET" + " pontosObtidos = ? WHERE cdInst = ? AND cdTipoCurso = ? AND cdCurso = ? " + "AND cdDisc = ? AND cdTurma = ? AND cdUsr = ? and cdConteudo = ? AND cdProb = ?" + " and cdSol = ?  AND idAval = ? and cdResposta = ?");
                stmt.setDouble(1, aluno.getNota());
                stmt.setInt(2, codigoInst);
                stmt.setInt(3, codigoTipoCurso);
                stmt.setInt(4, codigoCurso);
                stmt.setString(5, codigoDisc);
                stmt.setString(6, codigoTurma);
                stmt.setString(7, aluno.getCdUsr());
                stmt.setInt(8, aluno.getCdConteudo());
                stmt.setInt(9, codigoProblema);
                stmt.setInt(10, aluno.getCdSol());
                stmt.setInt(11, idAval);
                stmt.setInt(12, aluno.getCdResp());
                stmt.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
