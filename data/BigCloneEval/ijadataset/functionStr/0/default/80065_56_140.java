public class Test {    public static void main(String args[]) throws Exception {
        Class c = Class.forName("tInstance");
        tInstance myInstance = (tInstance) c.newInstance();
        myInstance.ifield = 4;
        myInstance.dfield = 8.8;
        myInstance.bfield = true;
        myInstance.ofield = null;
        System.out.println(c);
        Method methods[] = c.getMethods();
        Method imethod = null;
        Method dmethod = null;
        Method bmethod = null;
        Method omethod = null;
        Method vmethod = null;
        for (int i = 0; i < methods.length; i++) {
            String methodName = methods[i].getName();
            if (methodName.equals("iuserFunction")) imethod = methods[i]; else if (methodName.equals("duserFunction")) dmethod = methods[i]; else if (methodName.equals("buserFunction")) bmethod = methods[i]; else if (methodName.equals("ouserFunction")) omethod = methods[i]; else if (methodName.equals("vuserFunction")) vmethod = methods[i];
        }
        Object[] methodargs = new Object[1];
        if (imethod == null) {
            System.out.println("tInstance.iuserFunction not found!");
            System.exit(-1);
        } else {
            System.out.println("================= READY TO CALL: " + imethod);
            methodargs[0] = new Integer(3);
            int iresult = ((Integer) imethod.invoke(myInstance, methodargs)).intValue();
            if (iresult != 7) {
                System.out.println("Wrong answer from iuserFunction");
                System.out.println(iresult);
                System.exit(-1);
            }
        }
        if (dmethod == null) {
            System.out.println("tInstance.duserFunction not found!");
            System.exit(-1);
        } else {
            System.out.println("================= READY TO CALL: " + dmethod);
            methodargs[0] = new Double(3.4);
            double dresult = ((Double) dmethod.invoke(myInstance, methodargs)).doubleValue();
            if (dresult < 12.2 || dresult >= 12.2000001) {
                System.out.println("Wrong answer from duserFunction");
                System.out.println(dresult);
                System.exit(-1);
            }
        }
        if (bmethod == null) {
            System.out.println("tInstance.buserFunction not found!");
            System.exit(-1);
        } else {
            System.out.println("================= READY TO CALL: " + bmethod);
            methodargs[0] = new Boolean(true);
            boolean bresult = ((Boolean) bmethod.invoke(myInstance, methodargs)).booleanValue();
            if (bresult != true) {
                System.out.println("Wrong answer from buserFunction");
                System.exit(-1);
            }
        }
        if (omethod == null) {
            System.out.println("tInstance.ouserFunction not found!");
            System.exit(-1);
        } else {
            System.out.println("================= READY TO CALL: " + omethod);
            methodargs[0] = new String("123");
            Object oresult = omethod.invoke(myInstance, methodargs);
            if (!(oresult instanceof java.lang.String) || !((java.lang.String) oresult).equals("123abc")) {
                System.out.println("Wrong answer from ouserFunction");
                System.exit(-1);
            }
        }
        if (vmethod == null) {
            System.out.println("tInstance.vuserFunction not found!");
            System.exit(-1);
        } else {
            System.out.println("================= READY TO CALL: " + vmethod);
            Object twoargs[] = new Object[2];
            twoargs[0] = new Integer(4);
            twoargs[1] = new Integer(10);
            Object vresult = vmethod.invoke(myInstance, twoargs);
            if ((vresult != null) || (myInstance.ifield != 18)) {
                System.out.println("Wrong results from vuserFunction");
                System.exit(-1);
            }
        }
        System.out.println("Test success");
    }
}