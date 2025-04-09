package gbl.tsp.genetic.reproducers.sexual;

import gbl.tsp.genetic.entity.Chromosome;
import gbl.tsp.genetic.entity.Population;
import gbl.tsp.genetic.entity.SexualReproducer;
import gbl.tsp.genetic.entity.TravellerChromosome;
import gbl.tsp.genetic.tools.MersenneTwister;

public class OrderedCrossover implements SexualReproducer {

    public Chromosome reproduce(Chromosome father, Chromosome mother) {
        try {
            if ((father instanceof TravellerChromosome) && (mother instanceof TravellerChromosome)) {
                TravellerChromosome f = (TravellerChromosome) father;
                TravellerChromosome m = (TravellerChromosome) mother;
                Chromosome child = (Chromosome) algorithm(f, m);
                child.setOriginator("OrderedCrossover");
                child.checkValidity();
                return child;
            } else {
                throw m_errIncompatible;
            }
        } catch (Exception e) {
            System.err.println("OrderedCrossover.reproduce() threw!");
        }
        return father;
    }

    private TravellerChromosome algorithm(TravellerChromosome f, TravellerChromosome m) {
        MersenneTwister mt = MersenneTwister.getTwister();
        int genomeLength = f.getNumCities();
        f.canonicalize();
        m.canonicalize();
        TravellerChromosome fatherOffspring = new TravellerChromosome(f);
        TravellerChromosome motherOffspring = new TravellerChromosome(m);
        fatherOffspring.invalidateCities();
        motherOffspring.invalidateCities();
        int segmentLength = 2 + mt.nextInt(genomeLength - 3);
        int fatherStart = mt.nextInt(genomeLength);
        int motherOffset = mt.nextInt(genomeLength);
        boolean reverseMother = mt.nextBoolean();
        int mcx_to = 0;
        int fcx_to = 0;
        int mcx_from = 0;
        int fcx_from = 0;
        int mcnx = segmentLength;
        int fcnx = segmentLength;
        for (int i = 0; i < genomeLength; i++) {
            int fatherCity = f.getCity(i + fatherStart);
            int motherCity = m.getCity(i, motherOffset + fatherStart, reverseMother);
            int fatherCityIndexInMother = m.findCity(fatherCity, motherOffset, reverseMother);
            int motherCityIndexInFather = f.findCity(motherCity);
            if (m.indexIsInSegment(fatherCityIndexInMother, fatherStart, segmentLength, motherOffset, reverseMother)) {
                fatherOffspring.setCity(fcx_to, m.getCity(mcx_from, motherOffset + fatherStart, reverseMother));
                fcx_to++;
                mcx_from++;
            } else {
                fatherOffspring.setCity(fcnx, fatherCity);
                fcnx++;
            }
            if (f.indexIsInSegment(motherCityIndexInFather, fatherStart, segmentLength, 0, false)) {
                motherOffspring.setCity(mcx_to, f.getCity(fcx_from + fatherStart));
                mcx_to++;
                fcx_from++;
            } else {
                motherOffspring.setCity(mcnx, motherCity);
                mcnx++;
            }
        }
        TravellerChromosome child = null;
        if ((Population.areMinimizing() && (motherOffspring.testFitness() < fatherOffspring.testFitness())) || ((!Population.areMinimizing()) && (motherOffspring.testFitness() > fatherOffspring.testFitness()))) {
            child = motherOffspring;
        } else {
            child = fatherOffspring;
        }
        return child;
    }
}
