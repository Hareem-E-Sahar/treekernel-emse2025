package co.edu.unal.ungrid.admin.master;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import net.jini.id.UuidFactory;
import co.edu.unal.space.util.SpaceProxy;

public class JobWrapper extends SpaceListenerImpl {

    public JobWrapper() {
        this(false);
    }

    public JobWrapper(boolean bLocalSpace) {
        super(bLocalSpace);
    }

    @SuppressWarnings("unchecked")
    private static Class<AbstractJob> getJobClass(String sClassName) {
        Class<AbstractJob> cJob = null;
        try {
            cJob = (Class<AbstractJob>) Class.forName(sClassName);
        } catch (Exception exc) {
            System.out.println("JobWrapper::getJobClass(): exc=" + exc);
        }
        return cJob;
    }

    @SuppressWarnings("unchecked")
    private static Constructor<AbstractJob> getConstructor(Class<AbstractJob> cJob, int nargs) {
        Constructor<AbstractJob> ctor = null;
        try {
            Constructor[] ca = cJob.getConstructors();
            for (int i = 0; i < ca.length; i++) {
                int m = ca[i].getModifiers();
                if (Modifier.isPublic(m)) {
                    if (m_bDebug) System.out.println("JobWrapper::getConstructor(): ctor=" + ca[i]);
                    Class[] pt = ca[i].getParameterTypes();
                    if (m_bDebug) System.out.println("JobWrapper::getConstructor(): params=" + pt.length);
                    if (pt.length == nargs) {
                        ctor = ca[i];
                        break;
                    }
                }
            }
        } catch (Exception exc) {
            System.out.println("JobWrapper::getConstructor(): exc=" + exc);
        }
        return ctor;
    }

    private static AbstractJob getJobObject(Class<AbstractJob> cJob, String[] args) {
        AbstractJob job = null;
        try {
            Class<?> superClass = cJob.getSuperclass();
            while (!superClass.equals(AbstractJob.class) && !superClass.equals(Object.class)) {
                superClass = superClass.getSuperclass();
            }
            if (superClass.equals(AbstractJob.class)) {
                if (m_bDebug) System.out.println("JobWrapper::getJobObject(): args=" + (args != null ? args.length : 0));
                Constructor<AbstractJob> ctor = getConstructor(cJob, args != null ? args.length : 0);
                if (ctor != null) {
                    if (m_bDebug) System.out.println("JobWrapper::getJobObject(): ctor=" + ctor);
                    Class<?>[] pt = ctor.getParameterTypes();
                    Object[] oa = new Object[pt.length];
                    for (int i = 0; i < pt.length; i++) {
                        if (pt[i].getName().equals(String.class.getName())) {
                            oa[i] = args[i];
                            if (m_bDebug) System.out.println("JobWrapper::getJobObject(): " + i + "=" + oa[i]);
                        } else {
                            Constructor<?> strCtor = ConstructorFactory.getConstructor(pt[i]);
                            if (strCtor != null) {
                                oa[i] = strCtor.newInstance(new Object[] { args[i] });
                                if (m_bDebug) System.out.println("JobWrapper::getJobObject(): " + i + "=" + oa[i]);
                            } else {
                                return null;
                            }
                        }
                    }
                    job = (AbstractJob) ctor.newInstance(oa);
                }
            }
        } catch (Exception exc) {
            System.out.println("JobWrapper::getJobObject(): exc=" + exc);
        }
        return job;
    }

    private static String[] getJobArgs(String[] args) {
        String[] sa = null;
        if (args.length > 1) {
            sa = new String[args.length - 1];
            for (int i = 0; i < sa.length; i++) {
                sa[i] = args[i + 1];
            }
        }
        return sa;
    }

    public static void runJob(String[] args) {
        if (m_bDebug) System.out.println("JobWrapper::runJob(): jobclass=" + args[0]);
        Class<AbstractJob> cJob = getJobClass(args[0]);
        if (cJob != null) {
            AbstractJob job = getJobObject(cJob, getJobArgs(args));
            if (job != null) {
                if (job.isValid()) {
                    System.setProperty("java.security.policy", ".policy");
                    System.out.println("-Djava.security.policy=" + System.getProperty("java.security.policy"));
                    String sLocal = System.getProperty("local.space");
                    boolean bLocal = (sLocal != null ? Boolean.parseBoolean(sLocal) : false);
                    JobWrapper jw = new JobWrapper(bLocal);
                    SpaceProxy proxy = jw.getProxy();
                    if (proxy != null) {
                        job.setId(UuidFactory.generate());
                        SpaceJobRunner jr = new SpaceJobRunner(proxy, job);
                        jr.start();
                    }
                } else {
                    System.err.println("JobWrapper::runJob(): invalid job params");
                }
            } else {
                System.err.println("JobWrapper::runJob(): null job object");
            }
        } else {
            System.err.println("JobWrapper::runJob(): job class not found");
        }
    }

    public static void main(String[] args) {
        if (args.length > 0) {
            runJob(args);
        }
    }

    private static boolean m_bDebug = !true;
}
