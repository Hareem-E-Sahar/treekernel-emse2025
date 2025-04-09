public class Test {    public double getSpread(SolutionSet solutionSet) {
        return new Spread().spread(solutionSet.writeObjectivesToMatrix(), trueParetoFront_.writeObjectivesToMatrix(), problem_.getNumberOfObjectives());
    }
}