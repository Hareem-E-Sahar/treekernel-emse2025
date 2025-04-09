import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class Test {

    public boolean Test1(String methodName) {
        Female f = new Female("Berta", "Baumax", 1990);
        return this.matchResultToString(methodName, "Berta Baumax", f.getIdentifier());
    }

    public boolean Test2(String methodName) {
        Female f = new Female("Berta", "Baumax", 1990);
        f.setSecondname("Bauhaus");
        return this.matchResultToString(methodName, "Bauhaus", f.getSecondName());
    }

    public boolean Test3(String methodName) {
        Female f = new Female("Berta", "Baumax", 1990);
        f.setSecondname("Bauhaus");
        return this.matchResultToString(methodName, "Baumax", f.getMaidenname());
    }

    public boolean Test4(String methodName) {
        Female f = new Female("Berta", "Baumax", 1990);
        f.setSecondname("Bauhaus");
        f.setSecondname("Obi");
        return this.matchResultToString(methodName, "Obi", f.getSecondName());
    }

    public boolean Test5(String methodName) {
        Female f = new Female("Berta", "Baumax", 1990);
        f.setSecondname("Bauhaus");
        f.setSecondname("Obi");
        return this.matchResultToString(methodName, "Baumax", f.getMaidenname());
    }

    public boolean Test6(String methodName) {
        Female f = new Female("Berta", "Baumax", 1990);
        return this.matchResultToString(methodName, "Baumax", f.getMaidenname());
    }

    public boolean Test7(String methodName) {
        try {
            Male m = new Male("Georg", "Gross", 1990);
            m.setFitness(Male.CIVILIANSERVICE);
            return this.matchExecution(methodName, false);
        } catch (AppException e) {
            return this.matchResultToString(methodName, "Nicht moeglich", e.getMessage());
        }
    }

    public boolean Test8(String methodName) {
        try {
            Male m = new Male("Georg", "Gross", 1990);
            m.setFitness(-10);
            return this.matchExecution(methodName, false);
        } catch (AppException e) {
            return this.matchResultToString(methodName, "Nicht moeglich", e.getMessage());
        }
    }

    public boolean Test9(String methodName) {
        Male m = new Male("Georg", "Gross", 1990);
        m.setFitness(Male.FIT);
        return this.matchResultToInteger(methodName, Male.FIT, m.getFitness());
    }

    public boolean Test10(String methodName) {
        Male m = new Male("Georg", "Gross", 1990);
        return this.matchResultToInteger(methodName, Male.UNKNOWNFIT, m.getFitness());
    }

    public boolean Test11(String methodName) {
        Male m = new Male("Georg", "Gross", 1990);
        return this.matchResultToInteger(methodName, Male.NOSERVICE, m.getService());
    }

    public boolean Test12(String methodName) {
        Male m = new Male("Georg", "Gross", 1990);
        m.setFitness(Male.FIT);
        m.setService(Male.CIVILIANSERVICE);
        return this.matchResultToInteger(methodName, Male.CIVILIANSERVICE, m.getService());
    }

    public boolean Test13(String methodName) {
        try {
            Male m = new Male("Georg", "Gross", 1990);
            m.setFitness(Male.FIT);
            m.setService(Male.CIVILIANSERVICE);
            m.setService(Male.NOSERVICE);
            return this.matchExecution(methodName, false);
        } catch (AppException a) {
            return this.matchResultToString(methodName, "Nicht moeglich", a.getMessage());
        }
    }

    public boolean Test15(String methodName) {
        try {
            Male m = new Male("Georg", "Gross", 1990);
            m.setFitness(Male.FIT);
            m.setService(Male.CIVILIANSERVICE);
            m.setService(Male.MILITARYSERVICE);
            return this.matchExecution(methodName, false);
        } catch (AppException a) {
            return this.matchResultToString(methodName, "Nicht moeglich", a.getMessage());
        }
    }

    public boolean Test16(String methodName) {
        Male m = new Male("Georg", "Gross", 1990);
        m.setFitness(Male.FIT);
        m.setService(Male.CIVILIANSERVICE);
        m.setService(Male.CIVILIANSERVICE);
        return this.matchResultToInteger(methodName, Male.CIVILIANSERVICE, m.getService());
    }

    public boolean Test17(String methodName) {
        Male m = new Male("Georg", "Gross", 1990);
        m.setFitness(Male.FIT);
        m.setService(Male.MILITARYSERVICE);
        return this.matchResultToInteger(methodName, Male.FIT, m.getFitness());
    }

    public boolean Test18(String methodName) {
        Male m = new Male("Georg", "Gross", 1990);
        m.setFitness(Male.FIT);
        m.setService(Male.MILITARYSERVICE);
        return this.matchResultToInteger(methodName, Male.FIT, m.getFitness());
    }

    public boolean Test19(String methodName) {
        try {
            FStudent fs = new FStudent("Berta", "Bauhaus", 1990, 1800);
            return this.matchExecution(methodName, false);
        } catch (AppException e) {
            return this.matchResultToString(methodName, "Geburtsjahr kann nicht nach Studienbeginn sein", e.getMessage());
        }
    }

    public boolean Test20(String methodName) {
        try {
            MStudent fs = new MStudent("Georg", "Gross", 1990, 1800);
            return this.matchExecution(methodName, false);
        } catch (AppException e) {
            return this.matchResultToString(methodName, "Geburtsjahr kann nicht nach Studienbeginn sein", e.getMessage());
        }
    }

    public boolean Test21(String methodName) {
        try {
            MStudent fs = new MStudent("Georg", "Gross", 1990, 1991);
            fs.abort(1990);
            return this.matchExecution(methodName, false);
        } catch (AppException e) {
            return this.matchResultToString(methodName, "Das Abschlussjahr muss zeitlich nach dem Inskribtionsjahr liegen.", e.getMessage());
        }
    }

    public boolean Test22(String methodName) {
        try {
            MStudent fs = new MStudent("Georg", "Gross", 1990, 1991);
            fs.finished(1990, 1);
            return this.matchExecution(methodName, false);
        } catch (AppException e) {
            return this.matchResultToString(methodName, "Das Abschlussjahr muss zeitlich nach dem Inskribtionsjahr liegen.", e.getMessage());
        }
    }

    public boolean Test23(String methodName) {
        MStudent fs = new MStudent("Georg", "Gross", 1990, 1991);
        return this.matchResultToInteger(methodName, 1991, fs.getInscriptionYear());
    }

    public boolean Test24(String methodName) {
        MStudent fs = new MStudent("Georg", "Gross", 1990, 1991);
        return this.matchResultToInteger(methodName, 1991, fs.getInscriptionYear());
    }

    public boolean Test25(String methodName) {
        Node n = new Node(new Integer(1));
        return this.matchResultToInteger(methodName, 1, ((Integer) n.getElem()).intValue());
    }

    public boolean Test26(String methodName) {
        Node n = new Node(new Integer(1));
        n.setNext(new Node(new Integer(2)));
        return this.matchResultToInteger(methodName, 2, ((Integer) n.getNext().getElem()).intValue());
    }

    public boolean Test27(String methodName) {
        int counter = 0;
        Node n = new Node(new Integer(1));
        n.setNext(new Node(new Integer(2)));
        n.getNext().setNext(new Node(new Integer(3)));
        Iterator i = n.new NodeIterator(n);
        while (i.hasNext()) {
            i.next();
            counter++;
        }
        return this.matchResultToInteger(methodName, 3, counter);
    }

    public boolean Test28(String methodName) {
        int counter = 0;
        Iterator i = null;
        Node n = new Node(new Integer(1));
        n.setNext(new Node(new Integer(2)));
        n.getNext().setNext(new Node(new Integer(3)));
        i = n.new NodeIterator(n);
        i.next();
        i.next();
        i.remove();
        i = n.new NodeIterator(n);
        while (i.hasNext()) {
            i.next();
            counter++;
        }
        return this.matchResultToInteger(methodName, 2, counter);
    }

    public boolean Test29(String methodName) {
        int counter = 0;
        Iterator i = null;
        Node n = new Node(new Integer(1));
        n.setNext(new Node(new Integer(2)));
        n.getNext().setNext(new Node(new Integer(3)));
        i = n.new NodeIterator(n);
        i.next();
        i.remove();
        i = n.new NodeIterator(n);
        while (i.hasNext()) {
            i.next();
            counter++;
        }
        return this.matchResultToInteger(methodName, 2, counter);
    }

    public boolean Test30(String methodName) {
        Iterator i = null;
        int counter = 0;
        Node n = new Node(new Integer(1));
        n.setNext(new Node(new Integer(2)));
        n.getNext().setNext(new Node(new Integer(3)));
        n.getNext().getNext().setNext(new Node(new Integer(4)));
        i = n.new NodeIterator(n);
        i.next();
        i.remove();
        i.next();
        i.remove();
        i = n.new NodeIterator(n);
        while (i.hasNext()) {
            i.next();
            counter++;
        }
        return this.matchResultToInteger(methodName, 2, counter);
    }

    public boolean Test31(String methodName) {
        int counter = 0;
        TaggingSet ts = new TaggingSet();
        ts.insert(new Male("Alois", "Mauerhofer", 1990));
        ts.insert(new Male("Konrad", "Mauerhofer", 1990));
        ts.insert(new Male("Alois", "Mauerhofer", 1990));
        ts.insert(new Male("Konrad", "Mauerhofer", 1990));
        ts.insert(new Male("Alois", "Mauerhofer", 1990));
        Iterator i = ts.get("Alois Mauerhofer");
        while (i.hasNext()) {
            i.next();
            counter++;
        }
        return this.matchResultToInteger(methodName, 3, counter);
    }

    public boolean Test32(String methodName) {
        int counter = 0;
        TaggingSet ts = new TaggingSet();
        ts.insert(new Male("Alois", "Mauerhofer", 1990));
        ts.insert(new Male("Konrad", "Mauerhofer", 1990));
        ts.insert(new Male("Alois", "Mauerhofer", 1990));
        ts.insert(new Male("Konrad", "Mauerhofer", 1990));
        ts.insert(new Male("Alois", "Mauerhofer", 1990));
        Iterator i = ts.iterator();
        while (i.hasNext()) {
            i.next();
            counter++;
        }
        return this.matchResultToInteger(methodName, 5, counter);
    }

    public boolean Test33(String methodName) {
        int counter = 0;
        TaggingSet ts = new TaggingSet();
        ts.insert(new Male("Alois", "Mauerhofer", 1990));
        ts.insert(new Male("Konrad", "Mauerhofer", 1990));
        ts.insert(new Male("Alois", "Mauerhofer", 1990));
        ts.insert(new Male("Konrad", "Mauerhofer", 1990));
        ts.insert(new Male("Alois", "Mauerhofer", 1990));
        Iterator i = ts.iterator();
        while (i.hasNext()) {
            i.next();
            counter++;
        }
        return this.matchResultToInteger(methodName, 5, counter);
    }

    public boolean Test34(String methodName) {
        try {
            Male m = new Male("Anton", "Mauerhofer", 1990);
            TaggingSet ts = new TaggingSet();
            ts.insert(m);
            ts.insert(m);
            return this.matchExecution(methodName, false);
        } catch (AppException e) {
            return this.matchResultToString(methodName, "Objekt bereits vorhanden", e.getMessage());
        }
    }

    public boolean Test35(String methodName) {
        try {
            Male m = new Male("Anton", "Mauerhofer", 1990);
            TaggingSet ts = new TaggingSet();
            ts.insert(m);
            ts.insert(m);
            return this.matchExecution(methodName, false);
        } catch (AppException e) {
            return this.matchResultToString(methodName, "Objekt bereits vorhanden", e.getMessage());
        }
    }

    public boolean Test36(String methodName) {
        try {
            Male m1 = new Male("Anton", "Mauerhofer", 1990);
            Male m2 = new Male("Anton", "Mauerhofer", 1991);
            UniqueSet ts = new UniqueSet();
            ts.insert(m1);
            ts.insert(m2);
            return this.matchExecution(methodName, false);
        } catch (AppException e) {
            return this.matchResultToString(methodName, "Objekt mit selber Kennung bereits vorhanden", e.getMessage());
        }
    }

    public boolean Test37(String methodName) {
        try {
            UniqueSet ts = new UniqueSet();
            ts.insert(new Male("Hans", "Guck in die Luft", 1800));
            ts.get("Irgendwer der nicht vorhanden ist");
            return this.matchExecution(methodName, false);
        } catch (AppException e) {
            return this.matchResultToString(methodName, "Kein Element gefunden", e.getMessage());
        }
    }

    public boolean Test38(String methodName) {
        try {
            UniqueSet ts = new UniqueSet();
            ts.get("Irgendwer der nicht vorhanden ist");
            return this.matchExecution(methodName, false);
        } catch (AppException e) {
            return this.matchResultToString(methodName, "Kein Element gefunden", e.getMessage());
        }
    }

    public boolean Test39(String methodName) {
        try {
            TaggingSet ts = new TaggingSet();
            ts.insert(new Male("Alois", "Mauerhofer", 1990));
            ts.insert(new Male("Konrad", "Mauerhofer", 1990));
            ts.insert(new Male("Alois", "Mauerhofer", 1990));
            ts.insert(new Male("Konrad", "Mauerhofer", 1990));
            ts.insert(new Male("Alois", "Mauerhofer", 1990));
            ts.get("Irgendwer der nicht vorhanden ist");
            return this.matchExecution(methodName, false);
        } catch (AppException e) {
            return this.matchResultToString(methodName, "Kein Element gefunden", e.getMessage());
        }
    }

    public boolean Test40(String methodName) {
        try {
            TaggingSet ts = new TaggingSet();
            ts.insert(new Male("Alois", "Mauerhofer", 1990));
            ts.insert(new Male("Konrad", "Mauerhofer", 1990));
            ts.insert(new Male("Alois", "Mauerhofer", 1990));
            ts.insert(new Male("Konrad", "Mauerhofer", 1990));
            ts.insert(new Male("Alois", "Mauerhofer", 1990));
            ts.get("Irgendwer der nicht vorhanden ist");
            return this.matchExecution(methodName, false);
        } catch (AppException e) {
            return this.matchResultToString(methodName, "Kein Element gefunden", e.getMessage());
        }
    }

    public boolean Test41(String methodName) {
        try {
            TaggingSet ts = new TaggingSet();
            ts.get("Irgendwer der nicht vorhanden ist");
            return this.matchExecution(methodName, false);
        } catch (AppException e) {
            return this.matchResultToString(methodName, "Kein Element gefunden", e.getMessage());
        }
    }

    private Country createCountry() {
        Country c = new Country("Country");
        MStudent ms;
        FStudent fs;
        Male m;
        Female f;
        ms = new MStudent("Anton", "Mauerhofer", 1980, 1990);
        ms.finished(1991, 1);
        c.insertPerson(ms);
        fs = new FStudent("Susi", "Mauerhofer", 1970, 1980);
        fs.finished(1982, 2);
        c.insertPerson(fs);
        ms = new MStudent("Anton", "Mauerhofer", 1960, 1970);
        ms.finished(1973, 3);
        c.insertPerson(ms);
        fs = new FStudent("Susi", "Mauerhofer", 1950, 1960);
        fs.finished(1964, 4);
        c.insertPerson(fs);
        ms = new MStudent("Anton", "Mauerhofer", 1940, 1950);
        ms.finished(1955, 5);
        c.insertPerson(ms);
        fs = new FStudent("Susi", "Mauerhofer", 1930, 1940);
        fs.finished(1946, 1);
        c.insertPerson(fs);
        ms = new MStudent("Anton", "Mauerhofer", 1980, 1990);
        ms.abort(1991);
        c.insertPerson(ms);
        ms = new MStudent("Anton", "Mauerhofer", 1980, 1990);
        ms.abort(1991);
        c.insertPerson(ms);
        m = new Male("Anton", "Mauerhofer", 1980);
        c.insertPerson(m);
        m = new Male("Anton", "Mauerhofer", 1980);
        c.insertPerson(m);
        return c;
    }

    public boolean Test42(String methodName) {
        Country c = new Country("Country");
        return this.matchResultToString(methodName, "Country", c.getName());
    }

    public boolean Test43(String methodName) {
        Country c = new Country("Country");
        return this.matchResultToString(methodName, "Country", c.getIdentifier());
    }

    public boolean Test44(String methodName) {
        Country c = this.createCountry();
        return this.matchResultToDouble(methodName, 3.5, c.getAveragePeriodOfStudies(Country.PERSON));
    }

    public boolean Test45(String methodName) {
        Country c = this.createCountry();
        return this.matchResultToDouble(methodName, 4, c.getAveragePeriodOfStudies(Country.FEMALE));
    }

    public boolean Test46(String methodName) {
        Country c = this.createCountry();
        return this.matchResultToDouble(methodName, 3, c.getAveragePeriodOfStudies(Country.MALE));
    }

    public boolean Test48(String methodName) {
        Country c = this.createCountry();
        return this.matchResultToDouble(methodName, 25, c.getPercentageOfDropouts(Country.PERSON));
    }

    public boolean Test49(String methodName) {
        Country c = this.createCountry();
        return this.matchResultToDouble(methodName, 0, c.getPercentageOfDropouts(Country.FEMALE));
    }

    public boolean Test50(String methodName) {
        Country c = this.createCountry();
        return this.matchResultToDouble(methodName, 40, c.getPercentageOfDropouts(Country.MALE));
    }

    public boolean Test51(String methodName) {
        Country c = this.createCountry();
        return this.matchResultToDouble(methodName, (5.0 / 8.0 * 100), c.getPercentageOfGraduates(Country.PERSON));
    }

    public boolean Test52(String methodName) {
        Country c = this.createCountry();
        return this.matchResultToDouble(methodName, 100, c.getPercentageOfGraduates(Country.FEMALE));
    }

    public boolean Test53(String methodName) {
        Country c = this.createCountry();
        return this.matchResultToDouble(methodName, (2.0 / 5.0 * 100), c.getPercentageOfGraduates(Country.MALE));
    }

    public boolean Test54(String methodName) {
        try {
            Country c = new Country("Country");
            c.getAveragePeriodOfStudies(Country.FEMALE);
            return this.matchExecution(methodName, false);
        } catch (AppException e) {
            return this.matchResultToString(methodName, "Keine Daten gefunden", e.getMessage());
        }
    }

    public boolean Test55(String methodName) {
        try {
            Country c = new Country("Country");
            c.getAveragePeriodOfStudies(Country.FEMALE);
            return this.matchExecution(methodName, false);
        } catch (AppException e) {
            return this.matchResultToString(methodName, "Keine Daten gefunden", e.getMessage());
        }
    }

    public boolean Test56(String methodName) {
        try {
            Country c = new Country("Country");
            c.getPercentageOfGraduates(Country.FEMALE);
            return this.matchResultToDouble(methodName, 0, c.getPercentageOfGraduates(Country.FEMALE));
        } catch (AppException e) {
            return this.matchResultToString(methodName, "Keine Daten gefunden", e.getMessage());
        }
    }

    private UniqueSet createWorld() {
        UniqueSet world = new UniqueSet();
        world.insert(new Country("Country1"));
        world.insert(new Country("Country2"));
        world.insert(new Country("Country3"));
        world.insert(new Country("Country4"));
        {
            Female f1 = new Female("FrauVorname1", "FrauNachname1", 1970);
            Female f2 = new Female("FrauVorname2", "Maedchenname2", 1970);
            f2.setSecondname("FrauNachname2");
            FStudent f3 = new FStudent("FrauVorname3", "Maedchenname3", 1970, 1990);
            f3.finished(1992, (float) 2.1);
            f3.setSecondname("Nachname3");
            FStudent f4 = new FStudent("FrauVorname4", "FrauNachname4", 1970, 1990);
            f4.finished(1992, (float) 1.5);
            FStudent f5 = new FStudent("FrauVorname5", "FrauNachname5", 1970, 1990);
            f5.finished(1993, (float) 3.0);
            FStudent f6 = new FStudent("FrauVorname6", "FrauNachname6", 1970, 1990);
            f6.finished(1993, (float) 3.2);
            FStudent f7 = new FStudent("FrauVorname7", "FrauNachname7", 1970, 1990);
            f7.finished(1993, (float) 2.5);
            FStudent f8 = new FStudent("FrauVorname8", "Maedchenname8", 1970, 1990);
            f8.abort(1995);
            f8.setSecondname("Frauenname8");
            FStudent f9 = new FStudent("FrauVorname9", "FrauNachname9", 1970, 1990);
            FStudent f10 = new FStudent("FrauVorname10", "FrauNachname10", 1970, 1990);
            ((Country) world.get("Country1")).insertPerson(f1);
            ((Country) world.get("Country1")).insertPerson(f2);
            ((Country) world.get("Country1")).insertPerson(f3);
            ((Country) world.get("Country1")).insertPerson(f4);
            ((Country) world.get("Country1")).insertPerson(f5);
            ((Country) world.get("Country1")).insertPerson(f6);
            ((Country) world.get("Country1")).insertPerson(f7);
            ((Country) world.get("Country1")).insertPerson(f8);
            ((Country) world.get("Country1")).insertPerson(f9);
            ((Country) world.get("Country1")).insertPerson(f10);
            Male m1 = new Male("MannVorname1", "MannVorname1", 1970);
            m1.setFitness(Male.FIT);
            m1.setService(Male.MILITARYSERVICE);
            Male m2 = new Male("MannVorname2", "MannVorname2", 1970);
            m2.setFitness(Male.FIT);
            m2.setService(Male.CIVILIANSERVICE);
            Male m3 = new Male("MannVorname3", "MannVorname3", 1970);
            m3.setFitness(Male.UNFIT);
            Male m4 = new Male("MannVorname4", "MannVorname4", 1970);
            m4.setFitness(Male.UNKNOWNFIT);
            MStudent m5 = new MStudent("MannVorname5", "MannVorname5", 1970, 1990);
            m5.setFitness(Male.FIT);
            m5.setService(Male.CIVILIANSERVICE);
            m5.finished(1995, (float) 1.1);
            MStudent m6 = new MStudent("MannVorname6", "MannVorname6", 1970, 1990);
            m6.setFitness(Male.FIT);
            m6.abort(1993);
            ((Country) world.get("Country1")).insertPerson(m1);
            ((Country) world.get("Country1")).insertPerson(m2);
            ((Country) world.get("Country1")).insertPerson(m3);
            ((Country) world.get("Country1")).insertPerson(m4);
            ((Country) world.get("Country1")).insertPerson(m5);
            ((Country) world.get("Country1")).insertPerson(m6);
        }
        {
            Male m1 = new Male("MannVorname1", "MannNachname1", 1970);
            Male m2 = new Male("MannVorname2", "MannNachname2", 1970);
            Female f1 = new Female("FrauVorname1", "Maedchenname", 1970);
            f1.setSecondname("Irgendwie");
            f1.setSecondname("Frauname1");
            Female f2 = new Female("FrauVorname2", "FrauVorname2", 1970);
            ((Country) world.get("Country2")).insertPerson(f1);
            ((Country) world.get("Country2")).insertPerson(f2);
            ((Country) world.get("Country2")).insertPerson(m1);
            ((Country) world.get("Country2")).insertPerson(m2);
        }
        {
            MStudent m1 = new MStudent("Jule", "Nachname", 1983, 2003);
            m1.setFitness(Male.UNFIT);
            m1.finished(2006, (float) 2);
            Male m2 = new Male("Jule", "Nachname", 1980);
            m2.setFitness(Male.FIT);
            m2.setService(Male.MILITARYSERVICE);
            FStudent f1 = new FStudent("Jule", "Nachname", 1970, 1990);
            f1.abort(1992);
            Female f2 = new Female("Jule", "Nachname", 1999);
            ((Country) world.get("Country3")).insertPerson(f1);
            ((Country) world.get("Country3")).insertPerson(f2);
            ((Country) world.get("Country3")).insertPerson(m1);
            ((Country) world.get("Country3")).insertPerson(m2);
        }
        return world;
    }

    public boolean Test57(String methodName) {
        UniqueSet world = this.createWorld();
        Iterator iworld = world.iterator();
        double periods = 0;
        int counter = 0;
        while (iworld.hasNext()) {
            Country c = (Country) iworld.next();
            try {
                periods += c.getAveragePeriodOfStudies(Country.PERSON);
                counter += c.countGraduates(Country.PERSON);
            } catch (AppException e) {
            }
        }
        return this.matchResultToDouble(methodName, (float) 6.0 / 7.0, periods / counter);
    }

    public boolean Test58(String methodName) {
        UniqueSet world = this.createWorld();
        Iterator iworld = world.iterator();
        double percents = 0;
        int counter = 0;
        while (iworld.hasNext()) {
            Country c = (Country) iworld.next();
            counter += c.countStudent(Country.PERSON);
            percents += c.getPercentageOfDropouts(Country.PERSON) * c.countStudent(Country.PERSON);
        }
        return this.matchResultToDouble(methodName, (float) 300.0 / 12.0, percents / counter);
    }

    public boolean Test59(String methodName) {
        UniqueSet world = this.createWorld();
        Iterator iworld = world.iterator();
        double percents = 0;
        int counter = 0;
        while (iworld.hasNext()) {
            Country c = (Country) iworld.next();
            counter += c.countStudent(Country.PERSON);
            percents += c.getPercentageOfGraduates(Country.PERSON) * c.countStudent(Country.PERSON);
        }
        return this.matchResultToDouble(methodName, (float) 600.0 / 12.0, java.lang.Math.round(((percents / counter) * 100) / 100));
    }

    public boolean Test60(String methodName) {
        Female f;
        Country c;
        UniqueSet world = this.createWorld();
        c = (Country) world.get("Country1");
        f = ((Female) c.get("FrauVorname1 FrauNachname1").next());
        f.setSecondname("NeuerNachname");
        return this.matchResultToString(methodName, "NeuerNachname", ((Female) ((Country) world.get("Country1")).get("FrauVorname1 NeuerNachname").next()).getSecondName());
    }

    public boolean Test61(String methodName) {
        Male m = null;
        UniqueSet world = this.createWorld();
        m = ((Male) ((Country) world.get("Country1")).get("MannVorname4 MannVorname4").next());
        m.setFitness(Male.FIT);
        return this.matchResultToInteger(methodName, Male.FIT, ((Male) ((Country) world.get("Country1")).get("MannVorname4 MannVorname4").next()).getFitness());
    }

    public boolean Test62(String methodName) {
        UniqueSet world = this.createWorld();
        Iterator i = null;
        int counter = 0;
        i = ((Country) world.get("Country3")).get("Jule Nachname");
        i.next();
        i.remove();
        i = ((Country) world.get("Country3")).get("Jule Nachname");
        while (i.hasNext()) {
            i.next();
            counter++;
        }
        return this.matchResultToInteger(methodName, 3, counter);
    }

    /**
	 * Kontrolliert ob diese Funktion aufgerufen wird oder nicht
	 * @param methodName
	 * @param expectedResult true wenn aufruf erwartet, sonst false
	 * @param returnedResult
	 * @return
	 */
    private boolean matchExecution(String methodName, boolean expectedResult) {
        if (expectedResult == true) {
            System.out.println("SUCCESS@" + methodName);
            System.out.println("EXPECTED EXECUTION\n");
            return true;
        } else {
            System.out.println("FAILURE@" + methodName);
            System.out.println("UNEXPECTED EXECUTION\n");
            return false;
        }
    }

    private boolean matchResultToInteger(String methodName, int expectedResult, int returnedResult) {
        if (expectedResult == returnedResult) {
            System.out.println("SUCCESS@" + methodName);
            System.out.println("EXPECTED");
            System.out.println(expectedResult);
            System.out.println("RETURNED");
            System.out.println(returnedResult + "\n");
            return true;
        } else {
            System.out.println("FAILURE@" + methodName);
            System.out.println("EXPECTED");
            System.out.println(expectedResult);
            System.out.println("RETURNED");
            System.out.println(returnedResult + "\n");
            return false;
        }
    }

    private boolean matchResultToDouble(String methodName, double expectedResult, double returnedResult) {
        if (expectedResult == returnedResult) {
            System.out.println("SUCCESS@" + methodName);
            System.out.println("EXPECTED");
            System.out.println(expectedResult);
            System.out.println("RETURNED");
            System.out.println(returnedResult + "\n");
            return true;
        } else {
            System.out.println("FAILURE@" + methodName);
            System.out.println("EXPECTED");
            System.out.println(expectedResult);
            System.out.println("RETURNED");
            System.out.println(returnedResult + "\n");
            return false;
        }
    }

    /**
	 * 
	 * @param methodName Name des Testfalles
	 * @param expectedResult Erwarteter Wert
	 * @param returnedResult Zurueckgeliefertert Wert
	 * @return wenn expectedResult = returnedResult true; sonst false
	 */
    private boolean matchResultToString(String methodName, String expectedResult, String returnedResult) {
        if (expectedResult.equals(returnedResult)) {
            System.out.println("SUCCESS@" + methodName);
            System.out.println("EXPECTED");
            System.out.println(expectedResult);
            System.out.println("RETURNED");
            System.out.println(returnedResult + "\n");
            return true;
        } else {
            System.out.println("FAILURE@" + methodName);
            System.out.println("EXPECTED");
            System.out.println(expectedResult);
            System.out.println("RETURNED");
            System.out.println(returnedResult + "\n");
            return false;
        }
    }

    public void run() {
        Method[] methods = this.getClass().getMethods();
        int countTestCases = 0;
        int countSuccessTestCases = 0;
        for (int i = 0; i < methods.length; i++) {
            if (methods[i].getName().length() > 4) {
                if (methods[i].getName().substring(0, 4).equals("Test")) {
                    countTestCases++;
                    try {
                        if ((Boolean) (methods[i].invoke(this, methods[i].getName().substring(4)))) {
                            countSuccessTestCases++;
                        }
                    } catch (IllegalArgumentException e) {
                        e.printStackTrace();
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    } catch (InvocationTargetException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        System.out.println("------------------------");
        System.out.println("Testcases: " + countTestCases);
        System.out.println("Success  : " + countSuccessTestCases);
        System.out.println("Failure  : " + (countTestCases - countSuccessTestCases));
    }

    public static void main(String[] args) {
        Test test = new Test();
        test.run();
    }
}
