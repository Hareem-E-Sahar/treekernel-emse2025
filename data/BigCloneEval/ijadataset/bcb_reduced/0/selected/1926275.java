package br.edu.ufcg.compiladores.brasigol.analisador;

import br.edu.ufcg.compiladores.brasigol.erros.ErroLexico;
import br.edu.ufcg.compiladores.brasigol.lexico.ConstantesLexicas;
import br.edu.ufcg.compiladores.brasigol.lexico.Token;

public class AnalisadorLexico implements ConstantesLexicas {

    private int posicaoAbsoluta;

    private int posicaoAbsolutaDaLinha;

    private int linha;

    private String codigoFonte;

    private boolean abreAspas;

    private boolean fechaAspas;

    private Token ultimoToken;

    public AnalisadorLexico() {
        this("");
    }

    public AnalisadorLexico(String codigoFonte) {
        setCodigoFonte(codigoFonte);
        linha = 1;
        abreAspas = false;
        fechaAspas = false;
        ultimoToken = null;
    }

    public void setCodigoFonte(String codigoFonte) {
        this.codigoFonte = codigoFonte;
        setPosicaoAbsoluta(0);
    }

    public void setPosicaoAbsoluta(int pos) {
        posicaoAbsoluta = pos;
    }

    /**
	 * @return
	 * @throws ErroLexico
	 */
    public Token proximoToken() throws ErroLexico {
        if (!existeTexto()) return null;
        int start = posicaoAbsoluta;
        abreAspas = false;
        fechaAspas = false;
        int state = 0;
        int lastState = 0;
        int endState = -1;
        int end = -1;
        while (existeTexto()) {
            lastState = state;
            char proximo = proximoCaractere();
            state = proximoEstado(proximo, state);
            if (state < 0) break; else {
                if (tokenPorEstado(state) >= 0) {
                    endState = state;
                    end = posicaoAbsoluta;
                }
            }
        }
        if (endState < 0 || tokenPorEstado(lastState) == -2) throw new ErroLexico(ERRO_LEXICO[lastState], new Token(0, "", linha, start));
        posicaoAbsoluta = end;
        int token = tokenPorEstado(endState);
        if (token == 0) return proximoToken(); else {
            String lexeme = codigoFonte.substring(start, end);
            token = localizarToken(token, lexeme);
            Token novoToken = new Token(token, lexeme, linha, getPosicaoRelativa(start + 1));
            ultimoToken = novoToken;
            return novoToken;
        }
    }

    private int getPosicaoRelativa(int posicaoAbsoluta) {
        int tab = 0;
        if (posicaoAbsoluta > posicaoAbsolutaDaLinha) {
            String linha = codigoFonte.substring(posicaoAbsolutaDaLinha, posicaoAbsoluta);
            while (linha.indexOf("\t") != -1) {
                linha = linha.replaceFirst("\\t", "");
                tab++;
            }
            return (posicaoAbsoluta - posicaoAbsolutaDaLinha) + tab * 3;
        }
        throw new RuntimeException("FUDEU");
    }

    private int proximoEstado(char c, int state) {
        int inicio = INDICE_DE_ESTADOS[state];
        int fim = INDICE_DE_ESTADOS[state + 1] - 1;
        while (inicio <= fim) {
            int meio = (inicio + fim) / 2;
            if (AUTOMATO_DE_ESTADOS[meio][0] == c) return AUTOMATO_DE_ESTADOS[meio][1]; else if (AUTOMATO_DE_ESTADOS[meio][0] < c) inicio = meio + 1; else fim = meio - 1;
        }
        return -1;
    }

    private int tokenPorEstado(int estado) {
        if (estado < 0 || estado >= ESTADOS_TOKEN.length) return -1;
        return ESTADOS_TOKEN[estado];
    }

    public int localizarToken(int base, String chave) {
        int start = INDICES_PALAVRAS_RESERVADAS[base];
        int end = INDICES_PALAVRAS_RESERVADAS[base + 1] - 1;
        chave = chave.toUpperCase();
        while (start <= end) {
            int half = (start + end) / 2;
            int comp = PALAVRAS_RESERVADAS[half].compareTo(chave);
            if (comp == 0) return CODIGO_PALAVRAS_RESERVADAS[half]; else if (comp < 0) start = half + 1; else end = half - 1;
        }
        return base;
    }

    public boolean existeTexto() {
        return posicaoAbsoluta < codigoFonte.length();
    }

    private char proximoCaractere() throws ErroLexico {
        if (existeTexto()) {
            char caractere = codigoFonte.charAt(posicaoAbsoluta++);
            if (caractere == '"') {
                if (!abreAspas) {
                    abreAspas = true;
                    fechaAspas = false;
                } else {
                    fechaAspas = true;
                    abreAspas = false;
                }
            }
            if (caractere == '\n') {
                if (abreAspas && !fechaAspas) {
                    throw new ErroLexico("Abriu aspas e n�o fechou", new Token(ultimoToken.getId(), ultimoToken.getLexeme(), ultimoToken.getLinha(), ultimoToken.getPosition() + 1));
                }
                linha++;
                posicaoAbsolutaDaLinha = posicaoAbsoluta;
            }
            return caractere;
        } else return (char) -1;
    }
}
