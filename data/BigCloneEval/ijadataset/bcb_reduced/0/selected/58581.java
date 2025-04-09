package testdb4o;

import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Set;
import com.db4o.Db4o;
import com.db4o.ObjectContainer;
import com.db4o.ObjectSet;
import com.db4o.query.Query;
import com.wgo.surveyModel.domain.common.Hydrophone;
import com.wgo.surveyModel.domain.common.Streamer;
import com.wgo.surveyModel.domain.common.StreamerSection;
import com.wgo.surveyModel.domain.common.SurveyDef;
import com.wgo.surveyModel.domain.common.Vessel;
import com.wgo.surveyModel.domain.common.impl.DomainFactory;

public class TestDb4Object {

    static ObjectContainer db = null;

    static final String dbFileName = "survey.db";

    public <T> T getInstance(Class<T> type, Serializable id) {
        Query query = db.query();
        query.constrain(type);
        query.descend("dbId").constrain(new Long(id.toString()));
        ObjectSet set = query.execute();
        T result = (T) set.get(0);
        return result;
    }

    public Object find(Class type, String fieldName, String fieldValue) {
        Class fieldType = null;
        Object fieldObj = null;
        String methodName = null;
        methodName = "get" + fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1, fieldName.length());
        System.out.println("methodName = " + methodName);
        Method m0 = null;
        try {
            m0 = type.getMethod(methodName);
            System.out.println("methodName from type  = " + m0.getReturnType());
            fieldType = m0.getReturnType();
        } catch (Exception e1) {
            e1.printStackTrace();
        }
        try {
            fieldObj = fieldType.getConstructor(String.class).newInstance(fieldValue);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (SecurityException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
        Query query = db.query();
        query.constrain(type);
        query.descend(fieldName).constrain(fieldObj);
        ObjectSet set = query.execute();
        if (set.size() > 0) return set.get(0); else return null;
    }

    public void run() {
        DomainFactory df = new DomainFactory();
        int noObj = 0;
        SurveyDef surdef = df.createSurveyDef();
        Vessel vessel = null;
        for (int v = 0; v < 1; v++) {
            noObj++;
            vessel = df.createVessel();
            vessel.setName("vessel-" + v);
            vessel.setProdYear(1995);
            vessel.setWeight(new Float(55.5));
            surdef.addVessel(vessel);
            Streamer streamer = null;
            for (int s = 0; s < 20; s++) {
                noObj++;
                streamer = df.createStreamer();
                streamer.setSerialNumber("streamer-" + s);
                streamer.setProdYear(1995);
                streamer.setLenght(9000);
                streamer.setType("solid");
                vessel.addStreamer(streamer);
                StreamerSection section = null;
                for (int ss = 0; ss < 20; ss++) {
                    noObj++;
                    section = df.createStreamerSection();
                    section.setSerialNumber("section-" + ss);
                    section.setProdYear(1995);
                    section.setLenght(9000);
                    section.setType("live");
                    streamer.addSection(section);
                    Hydrophone hyd = null;
                    for (int h = 0; h < 20; h++) {
                        noObj++;
                        hyd = df.createHydrophone();
                        hyd.setSerialNumber("hydrophone-" + h);
                        hyd.setProdYear(1995);
                        hyd.setFilter(10);
                        section.addHydrophone(hyd);
                    }
                }
            }
        }
        System.out.println("Start saving now : ");
        db.set(surdef);
        db.commit();
        System.out.println("Saved objects : " + noObj);
    }

    public void readData() {
        try {
            db = Db4o.openClient("134.32.185.196", 20000, "bn", "bn");
        } catch (IOException e1) {
            e1.printStackTrace();
        }
        int nofObj = 0;
        int filt = 0;
        ObjectSet result = db.get(SurveyDef.class);
        SurveyDef surdef = (SurveyDef) result.get(0);
        Set<Vessel> vessels = surdef.getVessels();
        for (Vessel v : vessels) {
            nofObj++;
            System.out.println("Vesselname : " + v.getName());
            Set<Streamer> streamers = v.getStreamers();
            for (Streamer s : streamers) {
                nofObj++;
                System.out.println("  Streamer # = " + s.getSerialNumber());
                Set<StreamerSection> ssections = s.getSections();
                for (StreamerSection ss : ssections) {
                    nofObj++;
                    System.out.println("     Section # = " + ss.getSerialNumber());
                    Set<Hydrophone> hydrophones = ss.getHydrophones();
                    System.out.println("       hyd no = " + hydrophones.size());
                    for (Hydrophone h : hydrophones) {
                        nofObj++;
                        filt += h.getFilter();
                    }
                }
            }
        }
        System.out.println("no objects read = " + nofObj + " filt = " + filt);
    }

    public static void main(String[] args) {
        Db4o.configure().activationDepth(8);
        try {
            db = Db4o.openClient("134.32.185.196", 20000, "bn", "bn");
        } catch (IOException e1) {
            e1.printStackTrace();
        }
        try {
            TestDb4Object me = new TestDb4Object();
            long t1 = System.currentTimeMillis();
            me.run();
            long t2 = System.currentTimeMillis();
            System.out.println("Save time = " + (t2 - t1) / 1000.0);
            Thread.sleep(5000, 0);
            t1 = System.currentTimeMillis();
            me.readData();
            t2 = System.currentTimeMillis();
            System.out.println("read time = " + (t2 - t1) / 1000.0);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            System.out.println("Closing database!");
            db.close();
        }
    }
}
