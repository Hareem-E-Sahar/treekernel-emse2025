import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.StringTokenizer;
import lights.extensions.LabelledTuple;
import lights.extensions.LabelledField;
import lights.TupleSpace;
import lights.Field;
import lights.Tuple;
import lights.interfaces.IField;
import lights.interfaces.IValuedField;
import lights.interfaces.ITuple;
import lights.interfaces.TupleSpaceException;
import lights.extensions.fuzzy.FloatFuzzyType;
import lights.extensions.fuzzy.FuzzyTuple;
import lights.extensions.fuzzy.FuzzyField;
import lights.extensions.aggregation.VirtualTuple;

class DistanceVirtualizer extends VirtualTuple {

    private float positionX;

    private float positionY;

    public void setPosition(float x, float y) {
        positionX = x;
        positionY = y;
    }

    public DistanceVirtualizer(ITuple virtualTemplate) {
        super(virtualTemplate);
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
        newTuple.add(fields[0]).add(fields[1]).add(new Field().setValue(calculateDistance((Float) ((IValuedField) fields[2]).getValue(), (Float) ((IValuedField) fields[3]).getValue()))).add(fields[4]);
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
                    tuple.add(new Field().setValue(floatElem));
                } catch (Exception e) {
                    tuple.add(new Field().setValue(token));
                }
            }
            tupleSpace.out(tuple);
            string = buffer.readLine();
        }
        return tupleSpace;
    }

    public static void main(String argv[]) throws TupleSpaceException, IOException {
        TupleSpace ts = loadTuplesFromFile("tuples.txt");
        FloatFuzzyType distance = new FloatFuzzyType("distance", 0, 1500);
        String termsDistance[] = { "close", "regular", "far" };
        distance.generateTrianglePartition(termsDistance);
        FloatFuzzyType money = new FloatFuzzyType("money", 0, 100);
        String termsMoney[] = { "cheap", "fine", "expensive" };
        money.generateTrianglePartition(termsMoney);
        ITuple template = new FuzzyTuple().add(new LabelledField("type")).add(new LabelledField("name")).add(new FuzzyField("distance").setFuzzyType(distance)).add(new FuzzyField("price").setFuzzyType(money));
        ((FuzzyTuple) template).setThreshold(0.8f);
        DistanceVirtualizer distanceVirtualizer = new DistanceVirtualizer(template);
        distanceVirtualizer.add(new Field().setType(String.class)).add(new Field().setType(String.class)).add(new Field().setType(Float.class)).add(new Field().setType(Float.class)).add(new Field().setType(Float.class));
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
                ((FuzzyTuple) template).setMatchingExpression(string);
                ITuple r[];
                try {
                    r = ts.rdg(distanceVirtualizer);
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
