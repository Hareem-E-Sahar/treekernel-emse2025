package br.ufrj.cad.model.planilha;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import net.sourceforge.jeval.EvaluationException;
import net.sourceforge.jeval.Evaluator;
import br.ufrj.cad.fwk.exception.BaseRuntimeException;
import br.ufrj.cad.fwk.exception.Notification;
import br.ufrj.cad.fwk.util.Util;
import br.ufrj.cad.model.bo.ItemPlanilha;

/**
 * Classe que representa uma expressao logica da formula de uma planiha.
 * uma expressao logica é composta pela expressao em si, e por um 
 * resultado (que será retornado caso a expressao seja "verdadiera").
 * 
 *  Caso uma expressao envolva a variavel "total", essa variavel
 *  deve ser sustituída pela pontuação total obtida pelo professor.
 * 
 * @author dalton
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class ExpressaoLogica {

    /**
   * Marcação que identifica a presenca de um ID de um item de planilha em uma expressao logica.
   * Essa marcação é necessária pois preciso substituir todos os ID's econtrados por valores true/false
   * de acordo com a marcacao do usuario na planilha.
   * ex:
   *  id:10 && id:25
   *  se transforma em true && false, caso o usuario em questao tenha marcado o Item de ID=10 e *nao* tenha
   *  marcado o item de ID=25.
   *  
   *  Em expressoes mais complexar, como:
   *  id:25 && cargaHoraria > 10
   *  Nao teria como diferenciar o 25 do 10. Nesse caso, 25 indica um item da planilha mas 10 é um valor literal.
   *  a substituição correta é: true && cargaHoraria > 10 e nao true && cargaHoraria > false, por exemplo.
   *  
   */
    public static final String PREFIXO_ID_ITEM = "id:";

    @XmlElement
    String expressao;

    @XmlElement
    String resultado;

    public ExpressaoLogica() {
        this(null, null);
    }

    public ExpressaoLogica(String expressao, String resultado) {
        super();
        this.expressao = expressao;
        this.resultado = resultado;
    }

    public String getExpressao() {
        return expressao;
    }

    public void setExpressao(String expressao) {
        this.expressao = expressao;
    }

    public String getResultado() {
        return resultado;
    }

    public void setResultado(String resultado) {
        this.resultado = resultado;
    }

    public boolean verdadeiro() {
        return !this.falso();
    }

    public boolean falso() {
        Evaluator evaluator = new Evaluator();
        if (Util.vazio(this.expressao)) {
            return true;
        }
        try {
            String expressaoModificada = this.expressao.replaceAll("true", "1").replaceAll("false", "0");
            return Util.vazio(expressaoModificada) || !evaluator.getBooleanResult(expressaoModificada);
        } catch (EvaluationException e) {
            throw new BaseRuntimeException(e);
        }
    }

    /**
   * Substitui a variavel total por um valor especifico.
   * @param integer
   */
    public void substituiPontuacaoTotal(Integer integer) {
        if (Formula.precisaDeVariavel(Formula.NOME_VARIAVEL_PONTUACAO_TOTAL, this.expressao)) {
            String expressaoAposSubstituicao = this.expressao.replaceAll(Formula.NOME_VARIAVEL_PONTUACAO_TOTAL, integer.toString());
            this.expressao = expressaoAposSubstituicao;
        }
        if (Formula.precisaDeVariavel(Formula.NOME_VARIAVEL_PONTUACAO_TOTAL, this.resultado)) {
            String resultadoAposSubstituicao = this.resultado.replaceAll(Formula.NOME_VARIAVEL_PONTUACAO_TOTAL, integer.toString());
            this.resultado = resultadoAposSubstituicao;
        }
    }

    /**
   * Sibstitui a variavel cargaHoraria por um valor especifico
   * @param valor TODO
   *
   */
    public void substituiCargaHoraria(String valor) {
        if (Formula.precisaDeVariavel(Formula.NOME_VARIAVEL_CARGA_HORARIA, this.expressao)) {
            this.expressao = this.expressao.replaceAll(Formula.NOME_VARIAVEL_CARGA_HORARIA, valor);
        }
        if (Formula.precisaDeVariavel(Formula.NOME_VARIAVEL_CARGA_HORARIA, this.resultado)) {
            this.resultado = this.resultado.replaceAll(Formula.NOME_VARIAVEL_CARGA_HORARIA, valor);
        }
    }

    /**
   * Retorna uma lista contento todos os ids os itens envolvidos nessa expressao
   * @return
   */
    public List<Integer> retornarIDItensPlanilha() {
        ArrayList<Integer> lista = new ArrayList<Integer>();
        if (Util.vazio(this.expressao)) {
            return lista;
        }
        Pattern padrao = Pattern.compile((PREFIXO_ID_ITEM + "[0-9]+"));
        Matcher m = padrao.matcher(this.expressao);
        while (m.find()) {
            lista.add(new Integer(this.expressao.substring(m.start() + 3, m.end())));
        }
        return lista;
    }

    public synchronized void substituir(String idItem, String valor) {
        this.expressao = this.expressao.replaceAll(idItem, valor);
    }

    public boolean precisaDaPontuacaoTotal() {
        return Formula.precisaDeVariavel(Formula.NOME_VARIAVEL_PONTUACAO_TOTAL, this.expressao) || Formula.precisaDeVariavel(Formula.NOME_VARIAVEL_PONTUACAO_TOTAL, this.resultado);
    }

    /**
   * calcula o resultado final.
   * Esse método assume que todas as variaveis JA FORAM substituídas!!
   * @return
   */
    public BigDecimal getResultadoFinal() {
        Evaluator evaluator = new Evaluator();
        try {
            return new BigDecimal(evaluator.getNumberResult(this.resultado));
        } catch (EvaluationException e) {
            throw new BaseRuntimeException(e);
        }
    }

    /**
   * Verifica se essa expressaoLogica precisa da variavel cargaHoraria
   * @return
   */
    public boolean precisaDaVariavelCargaHoraria() {
        return Formula.precisaDeVariavel(Formula.NOME_VARIAVEL_CARGA_HORARIA, this.expressao) || Formula.precisaDeVariavel(Formula.NOME_VARIAVEL_CARGA_HORARIA, this.resultado);
    }

    @Override
    public String toString() {
        return "Se " + this.substituiItensBoleanos() + " então resultado igual a " + bold(this.resultado);
    }

    /**
 * Substitui todas as ocorrêcias de "id:<NNN>" pela descrição do respectivo item
 * @return
 */
    private String substituiItensBoleanos() {
        String expressaoModificada = this.expressao;
        for (Integer idItem : this.retornarIDItensPlanilha()) {
            ItemPlanilha item = new ItemPlanilha();
            item.setId(new Long(idItem));
            ItemPlanilha itemNoBanco = Util.preenchido(item.find()) ? item.find() : new ItemPlanilha();
            expressaoModificada = expressaoModificada.replaceAll(PREFIXO_ID_ITEM + idItem, bold(itemNoBanco.getDescricao()));
        }
        return expressaoModificada;
    }

    private String bold(String descricao) {
        return "<strong>" + descricao + "</strong>";
    }

    public void validar() {
        this.substituiCargaHoraria("1");
        this.substituiPontuacaoTotal(new Integer(10));
        List<Integer> idItens = this.retornarIDItensPlanilha();
        for (Integer idItemPlanilha : idItens) {
            this.substituir(ExpressaoLogica.PREFIXO_ID_ITEM + idItemPlanilha.toString(), String.valueOf(Boolean.FALSE));
        }
        Notification erros = new Notification();
        try {
            this.falso();
            this.getResultadoFinal();
        } catch (BaseRuntimeException e) {
            erros.addEvent("expressao.invalida", new Object[] { e.getMessage() });
            throw new BaseRuntimeException(erros);
        } catch (NumberFormatException n) {
            erros.addEvent("expressao.invalida.divisao.por.zero", new Object[] { n.getMessage() });
            throw new BaseRuntimeException(erros);
        }
    }
}
