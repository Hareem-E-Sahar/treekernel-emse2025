import hog.HogMessenger;
import hog.ModelHogComponent;
import hog.UiHog;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Iterator;
import java.util.Vector;

class HOGMain {

    /**
     * @param args
     * @throws java.io.IOException
     */
    public static void main(final String[] args) throws java.io.IOException {
        HogMessenger theMessenger = HogMessenger.getMessenger();
        ModelHogComponent[] components = getComponents();
        for (int i = 0; i < components.length; ++i) {
            theMessenger.addObserver(components[i]);
        }
        UiHog mainFrame = new UiHog(components);
        mainFrame.setVisible(true);
        theMessenger.startMessaging();
    }

    /**
     * @return
     */
    public static ModelHogComponent[] getComponents() {
        Vector<String> v = new Vector<String>();
        v.add("ModelHogHelp");
        v.add("ModelHogIACV");
        v.add("ModelHogSpark");
        v.add("ModelHogFuel");
        ModelHogComponent[] components = new ModelHogComponent[v.size()];
        Iterator<String> itr = v.iterator();
        int i = 0;
        while (itr.hasNext()) {
            try {
                Class<?> cls = Class.forName("hog." + (String) itr.next());
                Constructor<?>[] temp = cls.getConstructors();
                ModelHogComponent mhc = (ModelHogComponent) temp[0].newInstance();
                components[i] = mhc;
                i++;
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            } catch (InstantiationException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }
        }
        ModelHogComponent[] mhcArray = new ModelHogComponent[i];
        System.arraycopy(components, 0, mhcArray, 0, i);
        return mhcArray;
    }
}
