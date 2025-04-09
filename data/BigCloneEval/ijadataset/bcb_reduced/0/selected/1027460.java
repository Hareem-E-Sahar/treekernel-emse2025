package JSci.codesounding;

import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.*;
import codesounding.BasicProcessor;

public class StatsProgAverageGraph extends BasicProcessor {

    protected Vector VAR_DECLARATION_SERIES = new Vector();

    protected Vector BREAK_SERIES = new Vector();

    protected Vector CONTINUE_SERIES = new Vector();

    protected Vector DO_SERIES = new Vector();

    protected Vector FOR_SERIES = new Vector();

    protected Vector WHILE_SERIES = new Vector();

    protected Vector START_BLOCK_SERIES = new Vector();

    protected Vector END_BLOCK_SERIES = new Vector();

    protected Vector IF_SERIES = new Vector();

    protected Vector RETURN_SERIES = new Vector();

    protected Vector THROW_SERIES = new Vector();

    protected long VAR_DECLARATION_TEMP = 0;

    protected long BREAK_TEMP = 0;

    protected long CONTINUE_TEMP = 0;

    protected long DO_TEMP = 0;

    protected long FOR_TEMP = 0;

    protected long WHILE_TEMP = 0;

    protected long START_BLOCK_TEMP = 0;

    protected long END_BLOCK_TEMP = 0;

    protected long IF_TEMP = 0;

    protected long RETURN_TEMP = 0;

    protected long THROW_TEMP = 0;

    protected long VAR_DECLARATION_PREV = 0;

    protected long BREAK_PREV = 0;

    protected long CONTINUE_PREV = 0;

    protected long DO_PREV = 0;

    protected long FOR_PREV = 0;

    protected long WHILE_PREV = 0;

    protected long START_BLOCK_PREV = 0;

    protected long END_BLOCK_PREV = 0;

    protected long IF_PREV = 0;

    protected long RETURN_PREV = 0;

    protected long THROW_PREV = 0;

    private int interval = 50;

    private int curInterval = 0;

    private int maxInterval = 60;

    private int scala = 100;

    public StatsProgAverageGraph() {
        Timer timer = new Timer();
        TimerTask task = new MyTask(this);
        timer.schedule(task, 0, interval);
    }

    float getAverage(float prev, float actual) {
        float avg = 0;
        avg = (prev + actual) / 2;
        return avg;
    }

    float getProgressiveAverage(Vector v, float prev, float actual) {
        float prog = 0;
        float delta = (actual - prev) / scala;
        if (v.size() > 0) {
            prog = getAverage(((Float) v.get(v.size() - 1)).floatValue(), delta);
        }
        return prog;
    }

    public void getVarDeclaration() {
        VAR_DECLARATION_TEMP++;
    }

    public void getStartBlock() {
        START_BLOCK_TEMP++;
    }

    public void getEndBlock() {
        END_BLOCK_TEMP++;
    }

    public void getIfStatement() {
        IF_TEMP++;
    }

    public void getForStatement() {
        FOR_TEMP++;
    }

    public void getDoStatement() {
        DO_TEMP++;
    }

    public void getWhileStatement() {
        WHILE_TEMP++;
    }

    public void getReturnStatement() {
        RETURN_TEMP++;
    }

    public void getBreakStatement() {
        BREAK_TEMP++;
    }

    public void getContinueStatement() {
        CONTINUE_TEMP++;
    }

    public void getThrowStatement() {
        THROW_TEMP++;
    }

    class MyTask extends TimerTask {

        StatsProgAverageGraph stats = null;

        MyTask(StatsProgAverageGraph stats) {
            this.stats = stats;
        }

        void printAllValues(Vector[] vs, PrintStream ps) {
            for (int i = 0; i < vs.length; i++) {
                ps.print("new float[] {");
                printValues(vs[i], ps);
                ps.println("};");
            }
        }

        void printValues(Vector v, PrintStream ps) {
            for (int i = 0; i < v.size(); i++) {
                ps.print(v.get(i) + "f,");
            }
            if (v.size() == 0) ps.print("EMPTY");
        }

        void showGraph(Vector[] vs) {
            float[][] data = new float[GraphViewer.SERIES_NAMES.length][];
            for (int i = 0; i < data.length; i++) {
                Vector vtemp = (Vector) vs[i];
                data[i] = new float[vtemp.size()];
                for (int x = 0; x < vtemp.size(); x++) {
                    data[i][x] = ((Float) vtemp.get(x)).floatValue();
                }
            }
            GraphViewer graph = new GraphViewer("StatsProgAverageGraph", data);
            graph.setVisible(true);
        }

        public void run() {
            curInterval++;
            if (curInterval == maxInterval) {
                Vector[] vdata = new Vector[] { stats.VAR_DECLARATION_SERIES, stats.BREAK_SERIES, stats.CONTINUE_SERIES, stats.DO_SERIES, stats.FOR_SERIES, stats.WHILE_SERIES, stats.START_BLOCK_SERIES, stats.END_BLOCK_SERIES, stats.IF_SERIES, stats.RETURN_SERIES, stats.THROW_SERIES };
                showGraph(vdata);
            }
            stats.VAR_DECLARATION_SERIES.add(new Float(getProgressiveAverage(stats.VAR_DECLARATION_SERIES, stats.VAR_DECLARATION_PREV, stats.VAR_DECLARATION_TEMP)));
            stats.BREAK_SERIES.add(new Float(getProgressiveAverage(stats.BREAK_SERIES, stats.BREAK_PREV, stats.BREAK_TEMP)));
            stats.CONTINUE_SERIES.add(new Float(getProgressiveAverage(stats.CONTINUE_SERIES, stats.CONTINUE_PREV, stats.CONTINUE_TEMP)));
            stats.DO_SERIES.add(new Float(getProgressiveAverage(stats.DO_SERIES, stats.DO_PREV, stats.DO_TEMP)));
            stats.FOR_SERIES.add(new Float(getProgressiveAverage(stats.FOR_SERIES, stats.FOR_PREV, stats.FOR_TEMP)));
            stats.WHILE_SERIES.add(new Float(getProgressiveAverage(stats.WHILE_SERIES, stats.WHILE_PREV, stats.WHILE_TEMP)));
            stats.START_BLOCK_SERIES.add(new Float(getProgressiveAverage(stats.START_BLOCK_SERIES, stats.START_BLOCK_PREV, stats.START_BLOCK_TEMP)));
            stats.END_BLOCK_SERIES.add(new Float(getProgressiveAverage(stats.END_BLOCK_SERIES, stats.END_BLOCK_PREV, stats.END_BLOCK_TEMP)));
            stats.IF_SERIES.add(new Float(getProgressiveAverage(stats.IF_SERIES, stats.IF_PREV, stats.IF_TEMP)));
            stats.RETURN_SERIES.add(new Float(getProgressiveAverage(stats.RETURN_SERIES, stats.RETURN_PREV, stats.RETURN_TEMP)));
            stats.THROW_SERIES.add(new Float(getProgressiveAverage(stats.THROW_SERIES, stats.THROW_PREV, stats.THROW_TEMP)));
            stats.VAR_DECLARATION_PREV = stats.VAR_DECLARATION_TEMP;
            stats.BREAK_PREV = stats.BREAK_TEMP;
            stats.CONTINUE_PREV = stats.CONTINUE_TEMP;
            stats.DO_PREV = stats.DO_TEMP;
            stats.FOR_PREV = stats.FOR_TEMP;
            stats.WHILE_PREV = stats.WHILE_TEMP;
            stats.START_BLOCK_PREV = stats.START_BLOCK_TEMP;
            stats.END_BLOCK_PREV = stats.END_BLOCK_TEMP;
            stats.IF_PREV = stats.IF_TEMP;
            stats.RETURN_PREV = stats.RETURN_TEMP;
            stats.THROW_PREV = stats.THROW_TEMP;
            stats.VAR_DECLARATION_TEMP = 0;
            stats.BREAK_TEMP = 0;
            stats.CONTINUE_TEMP = 0;
            stats.DO_TEMP = 0;
            stats.FOR_TEMP = 0;
            stats.WHILE_TEMP = 0;
            stats.START_BLOCK_TEMP = 0;
            stats.END_BLOCK_TEMP = 0;
            stats.IF_TEMP = 0;
            stats.RETURN_TEMP = 0;
            stats.THROW_TEMP = 0;
        }
    }
}
