package es.ulpgc.dis.heuriskein.model.solver.antz.operators;

import java.util.ArrayList;
import java.util.HashMap;
import es.ulpgc.dis.heuriskein.model.examples.fo.TravelSalesmanProblem;
import es.ulpgc.dis.heuriskein.model.solver.Execution;
import es.ulpgc.dis.heuriskein.model.solver.Individual;
import es.ulpgc.dis.heuriskein.model.solver.Operator;
import es.ulpgc.dis.heuriskein.model.solver.Population;
import es.ulpgc.dis.heuriskein.model.solver.Selector;
import es.ulpgc.dis.heuriskein.model.solver.antz.CitySelector;
import es.ulpgc.dis.heuriskein.model.solver.representation.Ant;

public class AntBehaviour extends Operator {

    private double p;

    private double q;

    private double initialPheromoneTrail;

    public double pheromone[][] = null;

    public Class[] getType() {
        return new Class[] { Ant.class };
    }

    public AntBehaviour() {
        p = 0.9;
        q = 0.09;
        initialPheromoneTrail = 1.0;
    }

    public AntBehaviour(AntBehaviour beha) {
        p = beha.p;
        q = beha.q;
        initialPheromoneTrail = beha.initialPheromoneTrail;
        pheromone = null;
    }

    public double getDistance(int from, int to) {
        return ((TravelSalesmanProblem) getExecution().getFitnessFunction()).distance(from, to);
    }

    public void initializePheromoneTrail(int size) {
        pheromone = new double[size][size];
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                pheromone[i][j] = initialPheromoneTrail;
            }
        }
    }

    public void setExecution(Execution execution) {
        super.setExecution(execution);
    }

    public void selectNextCity(Individual ind) {
    }

    public void updatePheromones(Population pop) {
        synchronized (this) {
            for (int i = 0; i < pheromone.length; i++) {
                for (int j = i; j < pheromone.length; j++) {
                    pheromone[i][j] *= p;
                    pheromone[j][i] = pheromone[i][j];
                }
            }
            for (int k = 0; k < pop.getPopulationSize(); k++) {
                Ant ant = (Ant) pop.get(k);
                for (int i = 0; i < ant.getRepresentationLength() - 1; i++) {
                    pheromone[ant.getGenAt(i) - 1][ant.getGenAt(i + 1) - 1] += q / ant.getValue();
                    pheromone[ant.getGenAt(i + 1) - 1][ant.getGenAt(i) - 1] = pheromone[ant.getGenAt(i) - 1][ant.getGenAt(i + 1) - 1];
                }
            }
        }
    }

    public void operate(Population population, Selector selector) {
        this.population = population;
        for (Individual ind : population.getIndividuals()) {
            ((Ant) ind).reset();
        }
        if (pheromone == null) {
            initializePheromoneTrail(population.get(0).getRepresentationLength());
        }
        CitySelector citySelector = (CitySelector) selector;
        citySelector.setPheromoneTrail(pheromone);
        for (int i = 0; i < population.getPopulationSize(); i++) {
            Ant ant = (Ant) population.get(i);
            citySelector.setup(ant);
            int nextCity;
            while ((nextCity = citySelector.select(ant)) != -1) {
                ant.append(nextCity);
            }
        }
        getExecution().calculateFitness(population);
        updatePheromones(population);
    }

    public int getTimes() {
        return population.get(0).getRepresentationLength();
    }

    public String getName() {
        return "Standard Ant Behaviour";
    }

    public String infoDebug() {
        String str = getName() + "\n";
        str += getParameters();
        return str;
    }

    public double getInitialPheromoneTrail() {
        return initialPheromoneTrail;
    }

    public void setInitialPheromoneTrail(double initialPheromoneTrail) {
        this.initialPheromoneTrail = initialPheromoneTrail;
    }

    public double getP() {
        return p;
    }

    public void setP(double p) {
        this.p = p;
    }

    public double getQ() {
        return q;
    }

    public void setQ(double q) {
        this.q = q;
    }

    public Object clone() {
        return new AntBehaviour(this);
    }

    public Object[] getData() {
        synchronized (this) {
            ArrayList<Double> data = new ArrayList<Double>();
            data.add((double) pheromone.length);
            for (int i = 0; i < pheromone.length; i++) {
                for (int j = i; j < pheromone.length; j++) {
                    data.add(pheromone[i][j]);
                }
            }
            return data.toArray();
        }
    }

    public void setData(Object data[]) {
        int size = Double.valueOf((String) data[0]).intValue();
        int k = 1;
        pheromone = new double[size][size];
        for (int i = 0; i < size; i++) {
            for (int j = i; j < size; j++) {
                pheromone[i][j] = pheromone[j][i] = Double.valueOf((String) data[k]).doubleValue();
                k++;
            }
        }
    }
}
