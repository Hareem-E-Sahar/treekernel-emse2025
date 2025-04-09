import rpg.sistema;

/**
 * @author Eric
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class personagem {

    public atributo fe, intuicao, criatividade, vontade, impeto, reflexo;

    public atributo forca, agilidade, resistencia, percepcao, inteligencia;

    private String Nome;

    public inventario inventario;

    public Arma arma;

    public Armadura armadura;

    public atributo habilidade;

    public atributo dano;

    private int penalidade;

    public personagem(String Nome, int fe, int intuicao, int criatividade, int vontade, int impeto, int reflexo) {
        int forca = (impeto + vontade) / 2;
        int inteligencia = (intuicao + criatividade) / 2;
        int resistencia = vontade;
        int percepcao = intuicao;
        int agilidade = (impeto + reflexo) / 2;
        int protecao = 0;
        set_Nome(Nome);
        this.inventario = new inventario(10);
        this.fe = new atributo("Fe", fe);
        this.intuicao = new atributo("Intuicao", intuicao);
        this.criatividade = new atributo("Criatividade", criatividade);
        this.vontade = new atributo("Vontade", vontade);
        this.impeto = new atributo("�mpeto", impeto);
        this.reflexo = new atributo("Reflexo", reflexo);
        this.forca = new atributo("forca", forca);
        this.inteligencia = new atributo("Inteligencia", inteligencia);
        this.resistencia = new atributo("Resistencia", resistencia);
        this.percepcao = new atributo("Percep��o", percepcao);
        this.agilidade = new atributo("Agilidade", agilidade);
        this.dano = new atributo("Dano", 0);
        this.habilidade = new atributo("Habilidade", 2);
        this.penalidade = 0;
    }

    public void set_Nome(String Nome) {
        this.Nome = Nome;
    }

    public void set_arma(Arma arma) {
        if (forca.get_valor() >= arma.get_forca_min()) {
            this.arma = arma;
        }
    }

    public void set_armadura(Armadura armadura) {
        this.armadura = armadura;
    }

    public void set_penalidade(int penalidade) {
        this.penalidade = penalidade;
    }

    public String get_Nome() {
        return this.Nome;
    }

    public Arma get_arma() {
        return this.arma;
    }

    public Armadura get_armadura() {
        return this.armadura;
    }

    public int get_penalidade() {
        return this.penalidade;
    }

    public boolean teste(atributo atributo, int modificador) {
        int teste = sistema.rola_dado(12) + this.penalidade + modificador;
        if (teste <= atributo.get_valor()) {
            return true;
        } else {
            return false;
        }
    }

    public void mostra_ficha() {
        System.out.println("Nome: " + get_Nome());
        System.out.println("F�: " + fe.get_valor());
        System.out.println("Intui��o: " + intuicao.get_valor());
        System.out.println("Criatividade: " + criatividade.get_valor());
        System.out.println("Vontade: " + vontade.get_valor());
        System.out.println("�mpeto: " + impeto.get_valor());
        System.out.println("Reflexo: " + reflexo.get_valor());
        System.out.println("For�a: " + forca.get_valor());
        System.out.println("Intelig�ncia: " + inteligencia.get_valor());
        System.out.println("Resist�ncia: " + resistencia.get_valor());
        System.out.println("Percep��o: " + percepcao.get_valor());
        System.out.println("Agilidade: " + agilidade.get_valor());
    }
}
