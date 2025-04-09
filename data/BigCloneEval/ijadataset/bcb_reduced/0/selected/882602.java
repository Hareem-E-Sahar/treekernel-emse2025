package br.unisul.semantico;

import javax.swing.JOptionPane;

/**
 * Classe utilizada pela classe "Hipotetica" para armazenar as informa��es de
 * uma instru��o. Esta classe, bem como as classes "AreaInstrucoes",
 * "AreaLiterais" e "Hipotetica" foi criada por Maicon, Reinaldo e Fabio e
 * adaptada para este aplicativo.
 */
class Tipos {

    public int codigo;

    public int op1;

    public int op2;

    /**
	 * Construtor sem par�metros. Todos os atributos s�o inicializados com
	 * valores padr�es.
	 */
    Tipos() {
        codigo = 0;
        op1 = 0;
        op2 = 0;
    }
}

/**
 * Classe utilizada pela classe "Hipotetica" para armazenar a �rea de
 * instru��es. Esta classe, bem como as classes "Tipos", "AreaLiterais" e
 * "Hipotetica" foi criada por Maicon, Reinaldo e Fabio e adaptada para este
 * aplicativo.
 */
class AreaInstrucoes {

    public Tipos AI[] = new Tipos[1000];

    public int LC;

    /**
	 * Construtor sem par�metros. Todos os atributos s�o inicializados com
	 * valores padr�es.
	 */
    AreaInstrucoes() {
        for (int i = 0; i < 1000; i++) {
            AI[i] = new Tipos();
        }
    }
}

/**
 * Classe utilizada pela classe "Hipotetica" para armazenar a �rea de literais.
 * Esta classe, bem como as classes "Tipos", "AreaInstrucoes" e "Hipotetica" foi
 * criada por Maicon, Reinaldo e Fabio e adaptada para este aplicativo.
 */
class AreaLiterais {

    public String AL[] = new String[30];

    public int LIT;
}

/**
 * Classe que implementa a m�quina hipot�tica. Esta classe, bem como as classes
 * "Tipos", "AreaInstrucoes" e "AreaLiterais" foi criada por Maicon, Reinaldo e
 * Fabio e adaptada para este aplicativo.
 */
class Hipotetica {

    public static int MaxInst = 1000;

    public static int MaxList = 30;

    public static int b;

    public static int topo;

    public static int p;

    public static int l;

    public static int a;

    public static int nv;

    public static int np;

    public static int operador;

    public static int k;

    public static int i;

    public static int num_impr;

    public static int[] S = new int[1000];

    /**
	 * Construtor sem par�metros. Os atributos "nv", "np" e "num_impr" s�o
	 * inicializados com valores padr�es.
	 */
    Hipotetica() {
        nv = np = num_impr = 0;
    }

    /**
	 * Inicializa a �rea de instru��es.
	 */
    public static void InicializaAI(AreaInstrucoes AI) {
        for (int i = 0; i < MaxInst; i++) {
            AI.AI[i].codigo = -1;
            AI.AI[i].op1 = -1;
            AI.AI[i].op2 = -1;
        }
        AI.LC = 0;
    }

    /**
	 * Inicializa a �rea de literais
	 */
    public static void InicializaAL(AreaLiterais AL) {
        for (int i = 0; i < MaxList; i++) {
            AL.AL[i] = "";
            AL.LIT = 0;
        }
    }

    /**
	 * Inclui uma instru��o na �rea de instru��es utilizada pela m�quina
	 * hipot�tica.
	 */
    public boolean IncluirAI(AreaInstrucoes AI, int c, int o1, int o2) {
        boolean aux;
        if (AI.LC >= MaxInst) {
            aux = false;
        } else {
            aux = true;
            AI.AI[AI.LC].codigo = c;
            if (o1 != -1) {
                AI.AI[AI.LC].op1 = o1;
            }
            if (c == 24) {
                AI.AI[AI.LC].op2 = o2;
            }
            if (o2 != -1) {
                AI.AI[AI.LC].op2 = o2;
            }
            AI.LC = AI.LC + 1;
        }
        return aux;
    }

    /**
	 * Altera uma instru��o da �rea de instru��es utilizada pela m�quina
	 * hipot�tica.
	 */
    public static void AlterarAI(AreaInstrucoes AI, int s, int o1, int o2) {
        if (o1 != -1) {
            AI.AI[s].op1 = o1;
        }
        if (o2 != -1) {
            AI.AI[s].op2 = o2;
        }
    }

    /**
	 * Inclui um literal na �rea de literais utilizada pela m�quina hipot�tica.
	 */
    public static boolean IncluirAL(AreaLiterais AL, String literal) {
        boolean aux;
        if (AL.LIT >= MaxList) {
            aux = false;
        } else {
            aux = true;
            AL.AL[AL.LIT] = literal;
            AL.LIT = AL.LIT + 1;
        }
        return aux;
    }

    /**
	 * Utilizada para determinar a base.
	 */
    public static int Base() {
        int b1;
        b1 = b;
        while (l > 0) {
            b1 = S[b1];
            l = l - 1;
        }
        return b1;
    }

    /**
	 * Respons�vel por interpretar as instru��es.
	 */
    public static void Interpreta(AreaInstrucoes AI, AreaLiterais AL) {
        topo = 0;
        b = 0;
        p = 0;
        S[1] = 0;
        S[2] = 0;
        S[3] = 0;
        operador = 0;
        String leitura;
        while (operador != 26) {
            operador = AI.AI[p].codigo;
            l = AI.AI[p].op1;
            a = AI.AI[p].op2;
            p = p + 1;
            switch(operador) {
                case 1:
                    p = S[b + 2];
                    topo = b - a;
                    b = S[b + 1];
                    break;
                case 2:
                    topo = topo + 1;
                    S[topo] = S[Base() + a];
                    break;
                case 3:
                    topo = topo + 1;
                    S[topo] = a;
                    break;
                case 4:
                    S[Base() + a] = S[topo];
                    topo = topo - 1;
                    break;
                case 5:
                    S[topo - 1] = S[topo - 1] + S[topo];
                    topo = topo - 1;
                    break;
                case 6:
                    S[topo - 1] = S[topo - 1] - S[topo];
                    topo = topo - 1;
                    break;
                case 7:
                    S[topo - 1] = S[topo - 1] * S[topo];
                    topo = topo - 1;
                    break;
                case 8:
                    if (S[topo] == 0) {
                        JOptionPane.showMessageDialog(null, "Divis�o por zero.", "Erro durante a execu��o", JOptionPane.ERROR_MESSAGE);
                    } else {
                        S[topo - 1] = S[topo - 1] / S[topo];
                        topo = topo - 1;
                    }
                    break;
                case 9:
                    S[topo] = -S[topo];
                    break;
                case 10:
                    S[topo] = 1 - S[topo];
                    break;
                case 11:
                    if ((S[topo - 1] == 1) && (S[topo] == 1)) {
                        S[topo - 1] = 1;
                    } else {
                        S[topo - 1] = 0;
                        topo = topo - 1;
                    }
                    break;
                case 12:
                    if ((S[topo - 1] == 1 || S[topo] == 1)) {
                        S[topo - 1] = 1;
                    } else {
                        S[topo - 1] = 0;
                        topo = topo - 1;
                    }
                    break;
                case 13:
                    if (S[topo - 1] < S[topo]) {
                        S[topo - 1] = 1;
                    } else {
                        S[topo - 1] = 0;
                    }
                    topo = topo - 1;
                    break;
                case 14:
                    if (S[topo - 1] > S[topo]) {
                        S[topo - 1] = 1;
                    } else {
                        S[topo - 1] = 0;
                    }
                    topo = topo - 1;
                    break;
                case 15:
                    if (S[topo - 1] == S[topo]) {
                        S[topo - 1] = 1;
                    } else {
                        S[topo - 1] = 0;
                    }
                    topo = topo - 1;
                    break;
                case 16:
                    if (S[topo - 1] != S[topo]) {
                        S[topo - 1] = 1;
                    } else {
                        S[topo - 1] = 0;
                    }
                    topo = topo - 1;
                    break;
                case 17:
                    if (S[topo - 1] <= S[topo]) {
                        S[topo - 1] = 1;
                    } else {
                        S[topo - 1] = 0;
                    }
                    topo = topo - 1;
                    break;
                case 18:
                    if (S[topo - 1] >= S[topo]) {
                        S[topo - 1] = 1;
                    } else {
                        S[topo - 1] = 0;
                    }
                    topo = topo - 1;
                    break;
                case 19:
                    p = a;
                    break;
                case 20:
                    if (S[topo] == 0) {
                        p = a;
                    }
                    topo = topo - 1;
                    break;
                case 21:
                    topo = topo + 1;
                    leitura = JOptionPane.showInputDialog(null, "Informe o valor:", "Leitura", JOptionPane.QUESTION_MESSAGE);
                    (S[topo]) = Integer.parseInt(leitura);
                    break;
                case 22:
                    JOptionPane.showMessageDialog(null, "" + S[topo], "Informa��o", JOptionPane.INFORMATION_MESSAGE);
                    topo = topo - 1;
                    break;
                case 23:
                    if (a >= AL.LIT) {
                        JOptionPane.showMessageDialog(null, "Literal n�o encontrado na �rea dos literais.", "Erro durante a execu��o", JOptionPane.ERROR_MESSAGE);
                    } else {
                        JOptionPane.showMessageDialog(null, "" + AL.AL[a], "Informa��o", JOptionPane.INFORMATION_MESSAGE);
                    }
                    break;
                case 24:
                    topo = topo + a;
                    break;
                case 25:
                    S[topo + 1] = Base();
                    S[topo + 2] = b;
                    S[topo + 3] = p;
                    b = topo + 1;
                    p = a;
                    break;
                case 26:
                    break;
                case 27:
                    break;
                case 28:
                    topo = topo + 1;
                    S[topo] = S[topo - 1];
                    break;
                case 29:
                    if (S[topo] == 1) {
                        p = a;
                    }
                    topo = topo - 1;
            }
        }
    }
}
