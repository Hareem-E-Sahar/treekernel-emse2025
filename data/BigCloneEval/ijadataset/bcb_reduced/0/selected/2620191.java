package modelo;

import java.awt.Color;
import java.awt.Graphics;
import java.util.ArrayList;
import java.util.List;
import modelo.Ponto;
import modelo.Reta;

public class Poligono extends FiguraGeometrica {

    private List<Ponto> vertices;

    /**
	 * Cria um poligono
	 * @param vertice 1
	 */
    public Poligono(Ponto vertice1) {
        vertices = new ArrayList<Ponto>();
        vertices.add(vertice1);
    }

    /**
	 * Cria um poligono a partir de um array de vertices
	 * @param vertices
	 */
    public Poligono(List<Ponto> vertices) {
        this.vertices = vertices;
    }

    /**
	 * Adiciona um vertice ao poligono
	 * @param vertice
	 */
    public void add(Ponto vertice) {
        vertices.add(vertice);
    }

    /**
	 * @return um array de pontos do poligono 
	 */
    public List<Ponto> getPontos() {
        List<Ponto> pontos = new ArrayList<Ponto>();
        Ponto inicio = vertices.get(0);
        Ponto fim = vertices.get(vertices.size() - 1);
        Ponto anterior = inicio;
        for (int i = 1; i < vertices.size(); i++) {
            pontos.addAll(new Reta(anterior, vertices.get(i)).getPontos());
            anterior = vertices.get(i);
        }
        pontos.addAll(new Reta(inicio, fim).getPontos());
        return pontos;
    }

    /**
	 * @return um array contendo todas as retas do poligono
	 */
    public ArrayList<Reta> getRetas() {
        ArrayList<Reta> retas = new ArrayList<Reta>();
        Ponto inicio = vertices.get(0);
        Ponto fim = vertices.get(vertices.size() - 1);
        Ponto anterior = inicio;
        for (int i = 1; i < vertices.size(); i++) {
            retas.add(new Reta(anterior, vertices.get(i)));
            anterior = vertices.get(i);
        }
        retas.add(new Reta(inicio, fim));
        return retas;
    }

    /**
	 * @return o centro de gravidade do poligono
	 */
    public Ponto getCG() {
        int xMenor = 0;
        int xMaior = 0;
        int yMenor = 0;
        int yMaior = 0;
        for (Ponto ponto : vertices) {
            int x = ponto.getX();
            int y = ponto.getY();
            if (x < xMenor) xMenor = x;
            if (x > xMaior) xMaior = x;
            if (y < yMenor) yMenor = y;
            if (y > yMaior) yMaior = y;
        }
        int x = (xMaior + xMenor) / 2;
        int y = (yMaior + yMenor) / 2;
        return new Ponto(x, y);
    }

    /**
	 * @return um array contendo todos os vertices do poligono
	 */
    public List<Ponto> getVertices() {
        return vertices;
    }

    /**
	 * @return o numero de vertices do poligono
	 */
    public int size() {
        return vertices.size();
    }
}
