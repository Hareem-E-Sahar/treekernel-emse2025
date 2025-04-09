package br.org.acessobrasil.portal.template;

import java.lang.reflect.Method;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.servlet.http.HttpServletRequest;
import br.org.acessobrasil.portal.util.G_Paginacao;
import br.org.acessobrasil.portal.util.HtmlUtils;
import br.org.acessobrasil.portal.util.Link;
import br.org.acessobrasil.portal.util.StringUtils;
import br.com.goals.tableedit.util.Conversor;

/**
 * A��es comuns ao template
 * @author Fabio Issamu Oshiro
 *
 */
public class Template {

    private static Pattern patRs = Pattern.compile("<!-- ini rs -->(.*?)<!-- fim rs -->", Pattern.DOTALL);

    private static Pattern patRsItens = Pattern.compile("<!-- ini rs\\((.*?)\\) -->(.*?)<!-- fim rs\\(\\1\\) -->", Pattern.DOTALL);

    private static Pattern patRsImagens = Pattern.compile("<!-- ini rs imagem\\((.*?)\\) -->(.*?)<!-- fim rs imagem\\(\\1\\) -->", Pattern.DOTALL);

    private static Pattern patAnterior = Pattern.compile("<!-- ini anterior -->(.*?)<!-- fim anterior -->", Pattern.DOTALL);

    private static Pattern patProxima = Pattern.compile("<!-- ini proxima -->(.*?)<!-- fim proxima -->", Pattern.DOTALL);

    private static Pattern patLimpaLink = Pattern.compile("(<a\\s.*?>|</a>)", Pattern.DOTALL);

    /**
	 * paginacao
	 */
    private static Pattern patPag = Pattern.compile("<!-- ini paginacao -->(.*?)<!-- fim paginacao -->", Pattern.DOTALL);

    private static Pattern patPags = Pattern.compile("<!-- ini paginas -->(.*?)<!-- fim paginas -->", Pattern.DOTALL);

    private String template;

    private String templateOriginal;

    private HashMap<String, String> substituir = new HashMap<String, String>();

    private HashMap<String, Template> subTemplate = new HashMap<String, Template>();

    private RsItemCustomizado rsItemCustomizado;

    /**
	 * Substitui um valor quando este for igual
	 * no ResultSet
	 * @param igual
	 * @param novoValor
	 */
    public void addReplace(String igual, String novoValor) {
        substituir.put(igual, novoValor);
    }

    private String atribuiRsString(String template, ResultSet rs, String valor) throws SQLException {
        return atribuiRsString(template, rs.getString(valor), valor);
    }

    private String atribuiRsString(String template, String valor, String chave) {
        if (valor == null || valor.equals("")) {
            Pattern patRs = Pattern.compile("<!-- ini bloco " + chave + " -->(.*?)<!-- fim bloco " + chave + " -->", Pattern.DOTALL);
            Matcher mat = patRs.matcher(template);
            if (mat.find()) {
                template = mat.replaceAll("");
            }
        }
        Pattern patRs = Pattern.compile("<!-- ini rs\\(" + chave + "\\) -->(.*?)<!-- fim rs\\(" + chave + "\\) -->", Pattern.DOTALL);
        Matcher mat = patRs.matcher(template);
        if (mat.find()) {
            if (valor == null) {
                valor = "";
            }
            if (substituir.get(valor) != null) {
                valor = substituir.get(valor);
            }
            if (mat.group(1).trim().equals("dd/MM/yyyy")) {
                valor = Conversor.deYYYYMMDDparaDDMMYYYY(valor);
            }
            template = mat.replaceAll(StringUtils.tratarCaracteresEspeciaisRegex(valor));
        }
        return template;
    }

    public void encaixaPaginacao(Link voltar) {
        Matcher mat = patPag.matcher(template);
        if (mat.find()) {
            template = mat.replaceAll("<a href=\"" + voltar.getHref() + "\">" + voltar.getLabel() + "</a>");
        }
    }

    public void encaixaPaginacao(String string) {
        Matcher mat = patPag.matcher(template);
        if (mat.find()) {
            template = mat.replaceAll(StringUtils.tratarCaracteresEspeciaisRegex(string));
        }
    }

    /**
	 * Coloca a pagina��o de acordo com o template
	 * @param lstLinks lista de links
	 * @param lnkAnterior link da p�gina anterior
	 * @param lnkProxima link da pr�xima p�gina
	 * @param atual link da p�gina atual
	 */
    public void encaixaPaginacao(List<Link> lstLinks, Link lnkAnterior, Link lnkProxima, Link atual) {
        Matcher mat = patPag.matcher(template);
        if (mat.find()) {
            String paginas = mat.group(1);
            Matcher matPags = patPags.matcher(paginas);
            if (matPags.find()) {
                String strLink = matPags.group(1);
                String strSeparador = " - ";
                String strA = " - ";
                Pattern patLink = Pattern.compile("<a\\s(.*?)>", Pattern.DOTALL);
                Matcher matLink = patLink.matcher(strLink);
                if (matLink.find()) {
                    strA = matLink.group();
                }
                Pattern patSeparador = Pattern.compile("<!-- ini separador -->(.*?)<!-- fim separador -->", Pattern.DOTALL);
                Matcher matSeparador = patSeparador.matcher(strLink);
                if (matSeparador.find()) {
                    strSeparador = matSeparador.group(1);
                }
                StringBuilder sb = new StringBuilder();
                if (lstLinks.size() > 0) {
                    Link link = lstLinks.get(0);
                    if (link.equals(atual)) {
                        sb.append(link.getLabel());
                    } else {
                        sb.append(HtmlUtils.substituiAtributoTag(strA, "a", "href", link.getHref()));
                        sb.append(link.getLabel());
                        sb.append("</a>");
                    }
                    if (lstLinks.size() > 1) sb.append(strSeparador);
                    for (int i = 1; i < lstLinks.size(); i++) {
                        link = lstLinks.get(i);
                        if (link.equals(atual)) {
                            sb.append(link.getLabel());
                        } else {
                            sb.append(HtmlUtils.substituiAtributoTag(strA, "a", "href", link.getHref()));
                            sb.append(link.getLabel());
                            sb.append("</a>");
                        }
                        sb.append(strSeparador);
                    }
                }
                paginas = matPags.replaceAll(sb.toString());
            }
            Matcher matAntProx = patAnterior.matcher(paginas);
            if (matAntProx.find()) {
                String anterior = matAntProx.group(1);
                if (lnkAnterior != null) {
                    paginas = matAntProx.replaceAll(HtmlUtils.substituiAtributoTag(anterior, "a", "href", lnkAnterior.getHref()));
                } else {
                    Matcher matAs = patLimpaLink.matcher(anterior);
                    if (matAs.find()) {
                        anterior = matAs.replaceAll("");
                    }
                    paginas = matAntProx.replaceAll(anterior);
                }
            }
            matAntProx = patProxima.matcher(paginas);
            if (matAntProx.find()) {
                String anterior = matAntProx.group(1);
                if (lnkProxima != null) {
                    paginas = matAntProx.replaceAll(HtmlUtils.substituiAtributoTag(anterior, "a", "href", lnkProxima.getHref()));
                } else {
                    Matcher matAs = patLimpaLink.matcher(anterior);
                    if (matAs.find()) {
                        anterior = matAs.replaceAll("");
                    }
                    paginas = matAntProx.replaceAll(anterior);
                }
            }
            template = mat.replaceAll(paginas);
        }
    }

    /**
	 * 
	 * @param rowCount total de resultados
	 * @param pagina numero da pagina atual
	 * @param max resultados por pagina
	 * @param request
	 */
    @SuppressWarnings("unchecked")
    public void encaixaPaginacao(int rowCount, int pagina, int max, HttpServletRequest request) {
        Matcher mat = patPag.matcher(template);
        if (mat.find()) {
            String qString = "";
            String paginas = mat.group(1);
            G_Paginacao paginacao = new G_Paginacao();
            Enumeration<String> e = request.getParameterNames();
            while (e.hasMoreElements()) {
                String parm = e.nextElement();
                if (!parm.equals("pagina")) qString += parm + "=" + request.getParameter(parm) + "&";
            }
            Matcher matPags = patPags.matcher(paginas);
            if (matPags.find()) {
                String strLink = matPags.group(1);
                String strSeparador = " - ";
                String strA = " - ";
                Pattern patLink = Pattern.compile("<a\\s(.*?)>", Pattern.DOTALL);
                Matcher matLink = patLink.matcher(strLink);
                if (matLink.find()) {
                    strA = matLink.group();
                }
                Pattern patSeparador = Pattern.compile("<!-- ini separador -->(.*?)<!-- fim separador -->", Pattern.DOTALL);
                Matcher matSeparador = patSeparador.matcher(strLink);
                if (matSeparador.find()) {
                    strSeparador = matSeparador.group(1);
                }
                StringBuilder sb = new StringBuilder();
                paginacao.setTotalRows(rowCount);
                paginacao.setPagina(pagina);
                paginacao.setMaxResults(max);
                List<Link> lstLinks = paginacao.getLinks();
                if (lstLinks.size() > 0) {
                    Link link = lstLinks.get(0);
                    if (link.getHref().equals(String.valueOf(pagina))) {
                        sb.append(link.getLabel());
                    } else {
                        sb.append(HtmlUtils.substituiAtributoTag(strA, "a", "href", "index.jsp?" + qString + "pagina=" + link.getHref()));
                        sb.append(link.getLabel());
                        sb.append("</a>");
                    }
                    if (lstLinks.size() > 1) sb.append(strSeparador);
                    for (int i = 1; i < lstLinks.size(); i++) {
                        link = lstLinks.get(i);
                        if (link.getHref().equals(String.valueOf(pagina))) {
                            sb.append(link.getLabel());
                        } else {
                            sb.append(HtmlUtils.substituiAtributoTag(strA, "a", "href", "index.jsp?" + qString + "pagina=" + link.getHref()));
                            sb.append(link.getLabel());
                            sb.append("</a>");
                        }
                        sb.append(strSeparador);
                    }
                }
                paginas = matPags.replaceAll(sb.toString());
            }
            Matcher matAntProx = patAnterior.matcher(paginas);
            if (matAntProx.find()) {
                String anterior = matAntProx.group(1);
                if (pagina > 0) {
                    paginas = matAntProx.replaceAll(HtmlUtils.substituiAtributoTag(anterior, "a", "href", "index.jsp?" + qString + "pagina=" + (pagina - 1)));
                } else {
                    Matcher matAs = patLimpaLink.matcher(anterior);
                    if (matAs.find()) {
                        anterior = matAs.replaceAll("");
                    }
                    paginas = matAntProx.replaceAll(anterior);
                }
            }
            matAntProx = patProxima.matcher(paginas);
            if (matAntProx.find()) {
                String anterior = matAntProx.group(1);
                if (pagina < paginacao.getLastPage()) {
                    paginas = matAntProx.replaceAll(HtmlUtils.substituiAtributoTag(anterior, "a", "href", "index.jsp?" + qString + "pagina=" + (pagina + 1)));
                } else {
                    Matcher matAs = patLimpaLink.matcher(anterior);
                    if (matAs.find()) {
                        anterior = matAs.replaceAll("");
                    }
                    paginas = matAntProx.replaceAll(anterior);
                }
            }
            template = mat.replaceAll(paginas);
        }
    }

    /**
	 * Encaixa o resultSet no template
	 * @param rs resultado de uma busca
	 * @param maximo valor m�ximo para ser mostrado no navegador
	 */
    public void encaixaResultSet(ResultSet rs, int maximo) {
        try {
            String templateRs = "";
            Matcher mat = patRs.matcher(template);
            int i = 0;
            if (mat.find()) {
                templateRs = mat.group(1);
                StringBuilder sb = new StringBuilder();
                String item;
                if (rs.getRow() == 1) {
                    do {
                        item = templateRs;
                        Matcher matCol = patRsItens.matcher(templateRs);
                        while (matCol.find()) {
                            item = atribuiRsString(item, rs, matCol.group(1));
                        }
                        if (rsItemCustomizado != null) {
                            item = rsItemCustomizado.tratar(rs, item);
                        }
                        sb.append(item);
                    } while (rs.next() && maximo > i++);
                } else {
                    while (rs.next() && maximo > i++) {
                        item = templateRs;
                        Matcher matCol = patRsItens.matcher(templateRs);
                        while (matCol.find()) {
                            item = atribuiRsString(item, rs, matCol.group(1));
                        }
                        if (rsItemCustomizado != null) {
                            item = rsItemCustomizado.tratar(rs, item);
                        }
                        sb.append(item);
                    }
                }
                template = mat.replaceAll(StringUtils.tratarCaracteresEspeciaisRegex(sb.toString()));
            }
        } catch (Exception e) {
            System.out.println("Erro no plugin.Template");
            e.printStackTrace();
        }
    }

    public void encaixaResultSet(ResultSet rs) {
        encaixaResultSet(rs, 2000000);
    }

    public void replace(String procurar, String substituir) {
        template = template.replace(procurar, substituir);
    }

    /**
	 * Retorna o resultado e reinicia
	 * @return o template alterado com as informa��es
	 */
    public String getResultado() {
        String retorno = template;
        template = templateOriginal;
        return retorno;
    }

    /**
	 * Retorna o objeto template de uma �rea
	 * @param area nome da �rea
	 * @return Template da �rea
	 */
    public Template getTemplate(String area) {
        Template temp = subTemplate.get(area);
        if (temp == null) {
            temp = new Template();
            temp.setTemplate(getArea(area));
            subTemplate.put(area, temp);
        }
        return temp;
    }

    /**
	 * Coloca o HTML do template no objeto
	 * @param template HTML
	 */
    public void setTemplate(String template) {
        this.templateOriginal = template;
        this.template = template;
    }

    /**
	 * Encaixa um html no espa�o para resultset
	 * @param html c�digo HTML
	 */
    public void encaixaResultSet(String html) {
        Matcher mat = patRs.matcher(template);
        if (mat.find()) {
            template = mat.replaceAll(html);
        }
    }

    /**
	 * Limpa a �rea para resultset
	 */
    public void limpaResultSet() {
        Matcher mat = patRs.matcher(template);
        if (mat.find()) {
            template = mat.replaceAll("");
        }
    }

    /**
	 * Coloca a lista de resultados no template
	 * @param resultado lista de objetos do resultado
	 * @param max numero maximo na pagina
	 */
    @SuppressWarnings("unchecked")
    public void encaixaResultSet(List resultado, int ini, int max) {
        try {
            String templateRs = "";
            Matcher mat = patRs.matcher(template);
            if (mat.find()) {
                templateRs = mat.group(1);
                StringBuilder sb = new StringBuilder();
                String item;
                int tot = resultado.size();
                for (int i = ini; i < tot && i < max; i++) {
                    Object obj = resultado.get(i);
                    Class cls = obj.getClass();
                    item = templateRs;
                    Matcher matCol = patRsItens.matcher(templateRs);
                    while (matCol.find()) {
                        try {
                            String chave = matCol.group(1);
                            Method meth = cls.getMethod(chave);
                            Object retobj = meth.invoke(obj);
                            item = atribuiRsString(item, retobj == null ? "" : retobj.toString(), chave);
                        } catch (NoSuchMethodException e) {
                            System.out.println("Template.encaixaResultSet() Atencao, o metodo nao existe " + e.getMessage());
                        }
                    }
                    matCol = patRsImagens.matcher(item);
                    while (matCol.find()) {
                        try {
                            String chave = matCol.group(1);
                            Object retobj = cls.getMethod(chave).invoke(obj);
                            item = matCol.replaceAll(substituiSrcImagem(matCol.group(2), retobj.toString()));
                        } catch (NoSuchMethodException e) {
                            System.out.println("Template.encaixaResultSet() Atencao, o metodo nao existe " + e.getMessage());
                        }
                    }
                    if (rsItemCustomizado != null) {
                        item = rsItemCustomizado.tratar(obj, item);
                    }
                    sb.append(item);
                }
                template = mat.replaceAll(StringUtils.tratarCaracteresEspeciaisRegex(sb.toString()));
            }
        } catch (Exception e) {
            System.out.println("Erro no plugin.Template");
            e.printStackTrace();
        }
    }

    /**
	 * Coloca a lista de resultados no template
	 * @param resultado lista de objetos do resultado
	 */
    @SuppressWarnings("unchecked")
    public void encaixaResultSet(List resultado) {
        encaixaResultSet(resultado, 0, resultado.size());
        if (true) return;
        try {
            String templateRs = "";
            Matcher mat = patRs.matcher(template);
            if (mat.find()) {
                templateRs = mat.group(1);
                StringBuilder sb = new StringBuilder();
                String item;
                int tot = resultado.size();
                for (int i = 0; i < tot; i++) {
                    Object obj = resultado.get(i);
                    Class cls = obj.getClass();
                    item = templateRs;
                    Matcher matCol = patRsItens.matcher(templateRs);
                    while (matCol.find()) {
                        try {
                            String chave = matCol.group(1);
                            Method meth = cls.getMethod(chave);
                            Object retobj = meth.invoke(obj);
                            item = atribuiRsString(item, retobj.toString(), chave);
                        } catch (NoSuchMethodException e) {
                            System.out.println("Template.encaixaResultSet() Atencao, o metodo nao existe " + e.getMessage());
                        }
                    }
                    matCol = patRsImagens.matcher(item);
                    while (matCol.find()) {
                        try {
                            String chave = matCol.group(1);
                            Object retobj = cls.getMethod(chave).invoke(obj);
                            item = matCol.replaceAll(substituiSrcImagem(matCol.group(2), retobj.toString()));
                        } catch (NoSuchMethodException e) {
                            System.out.println("Template.encaixaResultSet() Atencao, o metodo nao existe " + e.getMessage());
                        }
                    }
                    if (rsItemCustomizado != null) {
                        item = rsItemCustomizado.tratar(obj, item);
                    }
                    sb.append(item);
                }
                template = mat.replaceAll(StringUtils.tratarCaracteresEspeciaisRegex(sb.toString()));
            }
        } catch (Exception e) {
            System.out.println("Erro no plugin.Template");
            e.printStackTrace();
        }
    }

    public void setLink(String nome, String href) {
        String link = getArea("link(" + nome + ")");
        String novo = Template.substituiHrefA(link, href);
        template = template.replace(link, novo);
    }

    public String getArea(String area) {
        area = area.replace("(", "\\(");
        area = area.replace(")", "\\)");
        area = area.replace(".", "\\.");
        Pattern patArea = Pattern.compile("<!-- ini " + area + " -->(.*?)<!-- fim " + area + " -->", Pattern.DOTALL);
        Matcher mat = patArea.matcher(template);
        if (mat.find()) {
            return mat.group(1);
        }
        return "";
    }

    /**
	 * A area � no seguinte formato
	 * &lt;!-- ini NomeDaArea --&gt;
	 * @param area
	 */
    public void retirarArea(String area) {
        try {
            setArea(area, "");
        } catch (Exception e) {
        }
    }

    public void setArea(String area, String valor) throws AreaNaoEncontradaException {
        area = area.replace("(", "\\(");
        area = area.replace(")", "\\)");
        area = area.replace(".", "\\.");
        Pattern patArea = Pattern.compile("<!-- ini " + area + " -->(.*?)<!-- fim " + area + " -->", Pattern.DOTALL);
        Matcher mat = patArea.matcher(template);
        if (mat.find()) {
            template = mat.replaceAll(StringUtils.tratarCaracteresEspeciaisRegex(valor));
        } else {
            throw new AreaNaoEncontradaException(area);
        }
    }

    public static String substituiSrcImagem(String html, String novoSrc) {
        return substituirAtributoTag(html, "img", "src", novoSrc);
    }

    /**
	 * Substitui todos os atributos href de tags a
	 * @param html c�digo HTML
	 * @param novoHref novo href
	 * @return html alterado
	 */
    public static String substituiHrefA(String html, String novoHref) {
        return substituirAtributoTag(html, "a", "href", novoHref);
    }

    /**
	 * Substitui o atributo de uma tag, caso esse atributo exista
	 * @param html codigo com a tag
	 * @param tag nome da tag
	 * @param atributo nome do atributo
	 * @param novoValor valor para ser colocado no atributo
	 * @return novo html
	 */
    public static String substituirAtributoTag(String html, String tag, String atributo, String novoValor) {
        String retorno = "";
        String reg = "<" + tag + "(\\s|\\s[^<]*?\\s)" + atributo + "=\".*?\"(.*?)>";
        Pattern pat = Pattern.compile(reg, Pattern.DOTALL);
        Matcher mat = pat.matcher(html);
        if (mat.find()) {
            String ini = mat.group(1);
            String fim = mat.group(2);
            retorno = mat.replaceAll("<" + tag + ini + atributo + "=\"" + novoValor + "\" " + fim + ">");
            return retorno;
        } else {
            return html;
        }
    }

    public void substituirAtributoTagId(String tagName, String id, String atributo, String novoValor) {
        String reg = "<" + tagName + "(\\s|\\s([^<]*?)\\s)id=\"" + id + "\".*?>";
        Pattern pat = Pattern.compile(reg, Pattern.DOTALL);
        Matcher mat = pat.matcher(template);
        if (mat.find()) {
            String tag = mat.group();
            Pattern patAtr = Pattern.compile("\\s" + atributo + "=\".*?\"", Pattern.DOTALL);
            Matcher matAtr = patAtr.matcher(tag);
            if (matAtr.find()) {
                tag = matAtr.replaceAll(" " + atributo + "=\"" + novoValor + "\"");
            } else {
                if (tag.endsWith("/>")) {
                    tag = tag.substring(0, tag.length() - 2) + " " + atributo + "=\"" + novoValor + "\" />";
                } else {
                    tag = tag.substring(0, tag.length() - 1) + " " + atributo + "=\"" + novoValor + "\" >";
                }
            }
            template = mat.replaceAll(tag);
        }
    }

    /**
	 * Substitui um atributo de uma tag
	 * @param html c�digo html
	 * @param tag nome da tag Ex.: img
	 * @param name nome do atributo nome da tag Ex.: &ltimg name="NOME"
	 * @param atributo nome do atributo a ser alterado
	 * @param novoValor valor do atributo
	 * @return codigo html alterado
	 */
    public static String substituiAtributoTag(String html, String tag, String name, String type, String atributo, String novoValor) {
        String reg = "<" + tag + "(\\s|\\s[^<]*?\\s)" + atributo + "=\".*?\"(.*?)>";
        Pattern pat = Pattern.compile(reg, Pattern.DOTALL);
        Matcher mat = pat.matcher(html);
        while (mat.find()) {
            if (mat.group().indexOf("name=\"" + name + "\"") != -1 && mat.group().indexOf("type=\"" + type + "\"") != -1) {
                String ini = mat.group(1);
                String fim = mat.group(2);
                int st = mat.start();
                int en = mat.end();
                html = html.substring(0, st) + "<" + tag + ini + atributo + "=\"" + novoValor + "\" " + fim + ">" + html.substring(en);
                break;
            }
        }
        return html;
    }

    /**
	 * Substitui um atributo de uma tag
	 * @param html c�digo html
	 * @param tag nome da tag Ex.: img
	 * @param name nome do atributo nome da tag Ex.: &lt;img name="NOME"
	 * @param atributo nome do atributo a ser alterado
	 * @param novoValor valor do atributo
	 * @return codigo html alterado
	 */
    public static String substituiAtributoTag(String html, String tag, String name, String atributo, String novoValor) {
        String reg = "<" + tag + "(\\s|\\s[^<]*?\\s)" + atributo + "=\".*?\"(.*?)>";
        Pattern pat = Pattern.compile(reg, Pattern.DOTALL);
        Matcher mat = pat.matcher(html);
        while (mat.find()) {
            if (mat.group().indexOf("name=\"" + name + "\"") != -1) {
                String ini = mat.group(1);
                String fim = mat.group(2);
                int st = mat.start();
                int en = mat.end();
                html = html.substring(0, st) + "<" + tag + ini + atributo + "=\"" + novoValor + "\" " + fim + ">" + html.substring(en);
                break;
            }
        }
        return html;
    }

    public static String substituiEntre(String procurarEm, String ini, String fim, String conteudo) {
        int i = procurarEm.indexOf(ini);
        if (i != -1) {
            int f = procurarEm.indexOf(fim);
            if (f != -1) {
                procurarEm = procurarEm.substring(0, i + ini.length()) + conteudo + procurarEm.substring(f);
                ;
            }
        }
        return procurarEm;
    }

    public RsItemCustomizado getRsItemCustomizado() {
        return rsItemCustomizado;
    }

    /**
	 * Informa um objeto que customiza o item gerado da forma padr�o pelo Template
	 * @param rsItemCustomizado
	 */
    public void setRsItemCustomizado(RsItemCustomizado rsItemCustomizado) {
        this.rsItemCustomizado = rsItemCustomizado;
    }

    public void substituirAtributoTagId(String tagName, String id, String atributo, int quantidade) {
        substituirAtributoTagId(tagName, id, atributo, quantidade + "");
    }

    public void setArea(String area, int inteiro) throws AreaNaoEncontradaException {
        this.setArea(area, String.valueOf(inteiro));
    }

    /**
	 * N�o entende pai, filho, etc.
	 * @param codHmtl
	 * @param tag
	 * @param name
	 * @param value
	 * @return c�digo alterado
	 */
    public static String substituiConteudoTag(String codHmtl, String tag, String name, String value) {
        final String reg = "<" + tag + "(\\s|\\s[^<]*?\\s)name=\"" + Pattern.quote(name) + "\"(.*?)>";
        Pattern pat = Pattern.compile(reg, Pattern.DOTALL);
        Matcher matcher = pat.matcher(codHmtl);
        if (matcher.find()) {
            return substituiEntre(codHmtl, matcher.group(), "</" + tag + ">", value);
        }
        return codHmtl;
    }

    /**
	 * Marcar o item de um select
	 * @param String codHtml, codigo html a ser alterado
	 * @param String selectName, nome do select o qual deve ser alterado
	 * @param String valor, valor que devera ser selecionado
	 * @return String codHtml
	 */
    public static String marcarSelect(String codHtml, String selectName, String valor) {
        Pattern patSelect = Pattern.compile("(<select[^>]*?(\\sname=\"" + selectName + "\")[^>]*>)(.*?)</select>", Pattern.DOTALL);
        Matcher matSelect = patSelect.matcher(codHtml);
        if (matSelect.find()) {
            String opts = matSelect.group(3);
            opts = opts.replace(" selected=\"selected\"", "");
            Pattern patOpt = Pattern.compile("<option[^>]*?value=\"" + valor + "\"[^>]*?>");
            Matcher matOpt = patOpt.matcher(opts);
            if (matOpt.find()) {
                String opt = matOpt.group();
                opt = opt.substring(0, opt.length() - 1) + " selected=\"selected\">";
                opts = matOpt.replaceAll(opt);
            }
            codHtml = matSelect.replaceAll(matSelect.group(1) + opts + "</select>");
        }
        return codHtml;
    }
}
