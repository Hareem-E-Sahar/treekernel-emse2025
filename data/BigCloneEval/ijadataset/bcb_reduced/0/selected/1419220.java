package org.rjam.alert;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import javax.script.SimpleScriptContext;
import org.rjam.gui.beans.Field;
import org.rjam.gui.beans.Row;
import org.rjam.gui.beans.Value;

public class ScriptTest implements Runnable {

    private static ThreadLocal<ScriptEngine> threadLocal = new ThreadLocal<ScriptEngine>() {

        @Override
        protected ScriptEngine initialValue() {
            return (new ScriptEngineManager()).getEngineByName("JavaScript");
        }
    };

    private int id;

    public ScriptTest(int id) {
        this.id = id;
    }

    public void run() {
        int myid = id;
        System.out.println("In id=" + myid);
        ScriptEngine engine = threadLocal.get();
        engine.put("id", id);
        System.out.println("id set to " + id);
        Object val = engine.get("id");
        System.out.println("val=" + val);
    }

    public static void main(String[] args) throws ScriptException, IOException {
        Pattern pattern = Pattern.compile("('.*')|(\".*\")|[^\\s]*");
        String template = "/bin/sh \"first argument\" -c 'echo application~{App} Method=~{Method} data=~{Start Data} ave~{Ave} txCount=~{TxCount}'";
        Matcher matcher = pattern.matcher(template);
        List<String> list = new ArrayList<String>();
        while (matcher.find()) {
            list.add(template.substring(matcher.start(), matcher.end()));
        }
        String[] cmd = (String[]) list.toArray(new String[list.size()]);
        for (String arg : cmd) {
            if (arg.length() > 0) {
                byte[] data = arg.getBytes();
                if (data[0] == '\'' && data[data.length - 1] == '\'' || data[0] == '"' && data[data.length - 1] == '"') {
                    arg = new String(data, 1, data.length - 2);
                }
            }
            System.out.println(arg);
        }
    }

    /**
	 * @param args
	 * @throws ScriptException 
	 * @throws IOException 
	 */
    public static void mainx(String[] args) throws ScriptException, IOException {
        ScriptEngineManager factory = new ScriptEngineManager();
        ScriptEngine engine = factory.getEngineByName("JavaScript");
        engine.eval("println('Hello, World')");
        File f = new File("test.txt");
        engine.put("file", f);
        engine.eval("println(file.getAbsolutePath())");
        Row row = new Row();
        row.setAve(10);
        row.add(new Value(Field.FLD_STD_DEV, 12));
        row.add(new Value(Field.FLD_MEAN, 12));
        row.add(new Value(Field.FLD_SAMPLE_SIZE, 30));
        List<Value> vals = row.getValues();
        for (Value v : vals) {
            engine.put(v.getField().getLabel(), v.getValueAsNumber().doubleValue());
        }
        for (Value v : vals) {
            engine.eval("println('" + v.getField().getLabel() + "='+" + v.getField().getLabel() + ");");
        }
        engine.eval("println('ave+stdDev='+(Ave+StdDev));");
        String expr = "Ave+StdDev";
        engine.eval("var ret = " + expr);
        Object obj = engine.get("ret");
        System.out.println("ret=" + obj);
        expr = "Ave>StdDev";
        engine.eval("var ret = " + expr);
        obj = engine.get("ret");
        System.out.println("ret=" + obj);
        List<ScriptEngineFactory> engins = factory.getEngineFactories();
        for (ScriptEngineFactory se : engins) {
            System.out.println("name=" + se.getEngineName() + " lang=" + se.getLanguageName() + " thread support=" + se.getParameter("THREADING"));
        }
        String exp = null;
        exp = "a+b/3";
        engine.put("a", 10);
        engine.put("b", 10);
        Object val = engine.eval(exp);
        System.out.println("val=" + val);
        SimpleScriptContext context = new SimpleScriptContext();
        try {
            val = engine.eval(exp, context);
            System.out.println("Contexy has no effect. val=" + val);
        } catch (ScriptException e) {
            System.out.println("Context causes an exception because vars are not found");
        }
        exp = "A+b/3";
        try {
            val = engine.eval(exp);
            System.out.println("not case sensitive val=" + val);
        } catch (ScriptException e) {
            System.out.println("case sensitive?  yes and throws exception when variables are not found");
        }
        Date date = new Date();
        engine.put("date", date);
        val = engine.get("date");
        if (val instanceof Date) {
            Date dt = (Date) val;
            System.out.println("Got a date as expected " + dt);
        } else {
            System.out.println("not a date???");
        }
        val = engine.eval("var dt = new java.util.Date()");
        val = engine.get("dt");
        if (val instanceof Date) {
            Date dt = (Date) val;
            System.out.println("Got a date as expected " + dt);
        } else {
            System.out.println("not a date??? class=" + val.getClass().getName());
        }
    }
}
