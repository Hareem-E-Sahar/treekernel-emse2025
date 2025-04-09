package org.iosgi.benchmark;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Sven Schulz
 */
public class MIPSolvingAnalysis {

    private static final Logger LOGGER = LoggerFactory.getLogger(MIPSolvingAnalysis.class);

    static class Instance {

        int vertices;

        int edges;

        Map<String, List<Solution>> solutions;

        @Override
        public String toString() {
            StringBuilder b = new StringBuilder();
            b.append('[').append(vertices).append(',').append(edges).append(',').append(solutions).append(']');
            return b.toString();
        }
    }

    static class Solution {

        long elapsed;

        int colors;

        @Override
        public String toString() {
            StringBuilder b = new StringBuilder();
            b.append('[').append(colors).append(',').append(elapsed).append(']');
            return b.toString();
        }
    }

    public static void cactus(List<Instance> instances) throws Exception {
        Map<Instance, Solution> bestSolutions = getBestSolution(instances);
        Map<String, List<Long>> times = new HashMap<String, List<Long>>();
        for (Instance i : instances) {
            Solution best = bestSolutions.get(i);
            outer: for (String name : i.solutions.keySet()) {
                List<Solution> l = i.solutions.get(name);
                for (Solution s : l) {
                    if (s.colors == best.colors) {
                        List<Long> ct = times.get(name);
                        if (ct == null) {
                            times.put(name, ct = new LinkedList<Long>());
                        }
                        ct.add(s.elapsed);
                        continue outer;
                    }
                }
            }
        }
        for (Map.Entry<String, List<Long>> e : times.entrySet()) {
            String name = e.getKey();
            FileWriter w = new FileWriter(name.substring(name.lastIndexOf('.') + 1) + ".dat");
            try {
                List<Long> l = e.getValue();
                Collections.sort(l);
                long c = 0;
                for (Long t : l) {
                    w.write(++c + "\t" + Math.max(1, t) + "\n");
                }
            } finally {
                w.close();
            }
        }
    }

    static class Entry {

        int vertices;

        double density;

        String name;

        public Entry(Instance instance, String name) {
            this.vertices = instance.vertices;
            this.density = Math.round(instance.edges / (double) MIPSolving.arithmeticSeriesSum(vertices - 1, 1, 1) * 100.0) / 100.0;
            this.name = name;
        }

        public Entry(int vertices, double density, String name) {
            this.vertices = vertices;
            this.density = density;
            this.name = name;
        }

        public Entry(Entry e) {
            this.vertices = e.vertices;
            this.density = e.density;
            this.name = e.name;
        }

        @Override
        public int hashCode() {
            return vertices + new Double(density).hashCode() + name.hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof Entry)) {
                return false;
            }
            Entry other = (Entry) obj;
            return vertices == other.vertices && density == other.density && name.equals(other.name);
        }
    }

    public static void heatmap(List<Instance> instances) throws Exception {
        Map<Instance, Solution> bestSolutions = getBestSolution(instances);
        Map<Entry, AtomicInteger> counters = new HashMap<Entry, AtomicInteger>();
        for (Instance i : instances) {
            Solution best = bestSolutions.get(i);
            outer: for (String name : i.solutions.keySet()) {
                List<Solution> l = i.solutions.get(name);
                for (Solution s : l) {
                    if (best.colors == s.colors && best.elapsed == s.elapsed) {
                        Entry e = new Entry(i, name);
                        AtomicInteger c = counters.get(e);
                        if (c == null) {
                            counters.put(e, c = new AtomicInteger());
                        }
                        c.incrementAndGet();
                        continue outer;
                    }
                }
            }
        }
        List<Integer> vertices = new LinkedList<Integer>();
        List<Double> densities = new LinkedList<Double>();
        List<String> names = new LinkedList<String>();
        for (Entry e : counters.keySet()) {
            if (!vertices.contains(e.vertices)) {
                vertices.add(e.vertices);
            }
            if (!densities.contains(e.density)) {
                densities.add(e.density);
            }
            if (!names.contains(e.name)) {
                names.add(e.name);
            }
        }
        Collections.sort(vertices);
        Collections.sort(densities);
        FileWriter[] writers = new FileWriter[names.size()];
        for (int v : vertices) {
            for (double d : densities) {
                int[] c = new int[names.size()];
                int sum = 0;
                for (int i = 0; i < names.size(); i++) {
                    String name = names.get(i);
                    Entry entry = new Entry(v, d, name);
                    c[i] = (counters.containsKey(entry) ? counters.get(entry).get() : 0);
                    sum += c[i];
                }
                for (int i = 0; i < names.size(); i++) {
                    if (writers[i] == null) {
                        String name = names.get(i);
                        writers[i] = new FileWriter("heatmap." + name.substring(name.lastIndexOf('.') + 1) + ".dat");
                    }
                    double prop = c[i] / (double) sum;
                    writers[i].write(v + "\t" + d + "\t" + prop + "\n");
                }
            }
            for (FileWriter w : writers) {
                w.write('\n');
            }
        }
        for (FileWriter w : writers) {
            w.close();
        }
    }

    public static void head2head(List<Instance> instances) throws Exception {
        long timeout = 60000000000L;
        List<String> names = null;
        Map<Instance, Solution> bestSolutions = getBestSolution(instances);
        List<long[]> pairs = new LinkedList<long[]>();
        for (int i = 0; i < instances.size(); i++) {
            Instance instance = instances.get(i);
            long[] p = new long[] { timeout, timeout };
            pairs.add(p);
            if (names == null) {
                names = new ArrayList<String>(instance.solutions.keySet());
            }
            Solution best = bestSolutions.get(instance);
            outer: for (int j = 0; j < names.size(); j++) {
                List<Solution> l = instance.solutions.get(names.get(j));
                for (Solution s : l) {
                    if (s.colors == best.colors) {
                        p[j] = s.elapsed;
                        continue outer;
                    }
                }
            }
        }
        FileWriter w = new FileWriter("head-to-head.dat");
        w.write("# ");
        for (String n : names) {
            w.write(n.substring(n.lastIndexOf('.') + 1) + " ");
        }
        w.write("\n");
        try {
            int[] timeouts = new int[3];
            for (long[] p : pairs) {
                if (p[0] == timeout && p[1] == timeout) {
                    timeouts[2]++;
                } else if (p[0] == timeout) {
                    timeouts[0]++;
                } else if (p[1] == timeout) {
                    timeouts[1]++;
                }
                w.write(p[0] / (double) 1000000 + "\t" + p[1] / (double) 1000000 + "\n");
            }
            System.out.println(Arrays.toString(timeouts));
        } finally {
            w.close();
        }
    }

    public static Map<Instance, Solution> getBestSolution(List<Instance> instances) {
        Map<Instance, Solution> bestSolutions = new HashMap<Instance, Solution>();
        for (Instance i : instances) {
            Solution best = null;
            for (String name : i.solutions.keySet()) {
                for (Solution s : i.solutions.get(name)) {
                    if (best == null || best.colors > s.colors || (best.colors == s.colors && best.elapsed > s.elapsed)) {
                        best = s;
                    }
                }
            }
            bestSolutions.put(i, best);
        }
        return bestSolutions;
    }

    public static List<Instance> analyze(File f) throws Exception {
        FileReader r = new FileReader(f);
        StringBuilder data = new StringBuilder();
        char[] cbuf = new char[4096];
        int read;
        while ((read = r.read(cbuf)) != -1) {
            data.append(cbuf, 0, read);
        }
        r.close();
        LOGGER.debug("{} characters read from input file", data.length());
        Pattern p = Pattern.compile("performing benchmark on graph \\(ID=[^\\|]*,\\|V\\|=(\\d+),\\|E\\|=(\\d+),[^\\)]*\\)", Pattern.MULTILINE | Pattern.DOTALL);
        List<Instance> instances = new LinkedList<Instance>();
        String input = data.toString();
        int from = 0;
        Matcher m = p.matcher(input);
        while (m.find()) {
            Instance i = new Instance();
            i.vertices = Integer.parseInt(m.group(1));
            i.edges = Integer.parseInt(m.group(2));
            i.solutions = new HashMap<String, List<Solution>>();
            LOGGER.debug("found run with {} vertices and {} edges", new Object[] { i.vertices, i.edges });
            instances.add(i);
            if (instances.size() > 1) {
                Instance k = instances.get(instances.size() - 2);
                String text = input.substring(from, m.start());
                extractSolutions(k, text);
            }
            from = m.end();
        }
        Instance k = instances.get(instances.size() - 1);
        String text = input.substring(from, data.length());
        extractSolutions(k, text);
        return instances;
    }

    private static void extractSolutions(Instance k, String text) {
        Pattern initial = Pattern.compile("\\[(.*)\\].*\\D(\\d+).*colors.*in.*\\D(\\d+) ns");
        Matcher m = initial.matcher(text);
        while (m.find()) {
            String name = m.group(1);
            if (name.endsWith("MIPSolving")) {
                continue;
            }
            Solution s = new Solution();
            s.colors = Integer.parseInt(m.group(2));
            s.elapsed = Long.parseLong(m.group(3));
            List<Solution> l = k.solutions.get(name);
            if (l == null) {
                k.solutions.put(name, l = new LinkedList<MIPSolvingAnalysis.Solution>());
            }
            l.add(s);
        }
        for (List<Solution> l : k.solutions.values()) {
            Collections.sort(l, new Comparator<Solution>() {

                @Override
                public int compare(Solution a, Solution b) {
                    return (int) ((a.elapsed - b.elapsed) / Math.max(1, Math.abs(a.elapsed - b.elapsed)));
                }
            });
        }
    }

    private static void repair(File f) throws Exception {
        FileWriter w = new FileWriter("transformed");
        FileReader r = new FileReader(f);
        BufferedReader br = new BufferedReader(r);
        String line = null;
        int i = 0;
        while ((line = br.readLine()) != null) {
            Matcher m = Pattern.compile("^org.*$", Pattern.MULTILINE).matcher(line);
            if (m.matches()) {
                i++;
                w.write("[" + line + "\n");
            } else {
                w.write(line + "\n");
            }
        }
        System.out.println(i);
    }

    public static void main(String[] args) throws Exception {
        List<Instance> instances = analyze(new File("MIPSolving.fixed.out"));
        head2head(instances);
    }
}
