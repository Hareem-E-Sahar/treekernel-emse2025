import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.StringTokenizer;
import lights.IVirtualizer;
import lights.Tuple;
import lights.TupleSpace;
import lights.interfaces.IField;
import lights.interfaces.ITuple;
import lights.interfaces.TupleSpaceException;
import fuzzy.FloatFuzzyVariable;
import fuzzy.FuzzyTupleMatcher;

class DistanceVirtualizer implements IVirtualizer {

    private float positionX;

    private float positionY;

    public void setPosition(float x, float y) {
        positionX = x;
        positionY = y;
    }

    public Float calculateDistance(Float X, Float Y) {
        float x = X.floatValue();
        float y = Y.floatValue();
        float result = (float) Math.pow(Math.pow(x - positionX, 2) + Math.pow(y - positionY, 2), 0.5);
        return new Float(result);
    }

    public ITuple virtualize(ITuple tuple) {
        ITuple newTuple = new Tuple();
        IField fields[] = tuple.getFields();
        newTuple.add(fields[0]).add(fields[1]).addActual(calculateDistance((Float) fields[2].getValue(), (Float) fields[3].getValue())).add(fields[4]);
        return newTuple;
    }
}

public class FuzzyMatching {

    private static TupleSpace loadTuplesFromFile(String fileName) throws IOException, TupleSpaceException {
        StringTokenizer stringTokenizer;
        TupleSpace tupleSpace = new TupleSpace();
        Tuple tuple;
        File input = new File(fileName);
        BufferedReader buffer = new BufferedReader(new FileReader(input));
        String string = buffer.readLine();
        String token;
        Float floatElem;
        while (string != null) {
            stringTokenizer = new StringTokenizer(string, ",");
            tuple = new Tuple();
            while (stringTokenizer.hasMoreTokens()) {
                token = stringTokenizer.nextToken();
                try {
                    floatElem = new Float(token);
                    tuple.addActual(floatElem);
                } catch (Exception e) {
                    tuple.addActual(token);
                }
            }
            tupleSpace.out(tuple);
            string = buffer.readLine();
        }
        return tupleSpace;
    }

    public static void main(String argv[]) throws TupleSpaceException, IOException {
        TupleSpace ts = loadTuplesFromFile("tuples.txt");
        FloatFuzzyVariable distance = new FloatFuzzyVariable("distance", 0, 1500);
        String termsDistance[] = { "near", "regular", "far" };
        distance.generateTrianglePartition(termsDistance);
        FloatFuzzyVariable money = new FloatFuzzyVariable("money", 0, 100);
        String termsMoney[] = { "cheap", "fine", "expensive" };
        money.generateTrianglePartition(termsMoney);
        FuzzyTupleMatcher fuzzyTupleMatcher = new FuzzyTupleMatcher(0.6f);
        fuzzyTupleMatcher.setVariable("kind").setVariable("name").setFuzzyVariable(distance).setFuzzyVariable(money);
        DistanceVirtualizer distanceVirtualizer = new DistanceVirtualizer();
        fuzzyTupleMatcher.setVirtualizer(distanceVirtualizer);
        ITuple template = new Tuple().addFormal(Float.class).addFormal(Float.class).setMatcher(fuzzyTupleMatcher);
        BufferedReader buffer = new BufferedReader(new InputStreamReader(System.in));
        String string;
        float x, y;
        boolean end = false;
        while (!end) {
            System.out.print("> ");
            string = buffer.readLine();
            if (string.equals("move")) {
                System.out.print("Inserisci la tua ascissa:  ");
                string = buffer.readLine();
                x = Float.parseFloat(string);
                System.out.print("Inserisci la tua ordinata: ");
                string = buffer.readLine();
                y = Float.parseFloat(string);
                distanceVirtualizer.setPosition(x, y);
            } else if (string.equals("exit")) end = true; else if (string.equals("list")) System.out.println(ts); else if (string.equals("query")) {
                string = buffer.readLine();
                fuzzyTupleMatcher.setFuzzyRule(string);
                ITuple r[];
                try {
                    r = ts.rdg(template);
                    if (r == null) System.out.println("no match"); else {
                        for (int i = 0; i < r.length; i++) System.out.println(r[i].toString());
                    }
                } catch (IllegalArgumentException e1) {
                    System.out.println(e1.getMessage());
                } catch (Exception e2) {
                    System.out.println("ERROR: " + e2.toString());
                }
            } else System.out.println("Command unknown");
        }
    }
}
