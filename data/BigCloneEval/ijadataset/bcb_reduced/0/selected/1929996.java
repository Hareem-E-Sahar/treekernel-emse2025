package ac.hiu.j314.elmve.ui;

import ac.hiu.j314.elmve.*;
import java.lang.reflect.*;
import java.io.*;

public class Elm2DData implements Serializable {

    public static final int NEW = 1;

    public static final int OLD = 2;

    public static final int UPDATE = 3;

    public int type;

    public String className;

    public ElmStub elm;

    public Place place = new Place();

    public Serializable data;

    public Elm2DData(int type) {
        this.type = type;
    }

    public Elm2DUIInterface makeUI() {
        try {
            Class c = ElmVE.classLoader.loadClass(className);
            Constructor con = c.getConstructor(new Class[0]);
            Elm2DUIInterface ui = (Elm2DUIInterface) con.newInstance(new Object[0]);
            ui.setElm(elm);
            ui.setPlace(place);
            return ui;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public Elm2DBGInterface makeBG() {
        try {
            Class c = ElmVE.classLoader.loadClass(className);
            Constructor con = c.getConstructor(new Class[0]);
            Elm2DBGInterface ui = (Elm2DBGInterface) con.newInstance(new Object[0]);
            ui.setElm(elm);
            ui.setPlace(place);
            return ui;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
