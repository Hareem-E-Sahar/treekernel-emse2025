package trojuhelnik;

public class Vypocty3 {

    public final double PR = 0.00001;

    public final double PI = Math.PI;

    private Spolecne spolecne;

    private CompLibInt compLib;

    /** Creates a new instance of Vypocty3 */
    public Vypocty3(Spolecne spolecne) {
        this.spolecne = spolecne;
        this.compLib = spolecne.compLib;
    }

    public void P120(double a, double b, double ua, boolean znak, String osa) {
        double c, ga, gap, gak, uaa, uami, uama;
        if (b <= a) uami = 0.0; else uami = 2 * b * (b - a) / (2 * b - a);
        uama = 2 * b * (a + b) / (a + 2 * b);
        if (ua <= uami) {
            spolecne.chyba(osa, "<=", uami);
            return;
        }
        if (ua >= uama) {
            spolecne.chyba(osa, ">=", uama);
            return;
        }
        gap = 0.0;
        gak = PI;
        do {
            ga = (gap + gak) / 2;
            c = spolecne.cs(a, b, ga);
            uaa = spolecne.osauhlu(b, c, a);
            if (uaa < ua) gap = ga; else gak = ga;
        } while (Math.abs(ua - uaa) > PR);
        spolecne.vyslbac(znak, a, b, c);
    }

    public void P121(double a, double b, double uc) {
        double ucma = 2 * a * b / (a + b);
        if (uc >= ucma) {
            spolecne.chyba("uc", ">=", ucma);
            return;
        }
        double c = (a + b) * Math.sqrt(1 - uc * uc / a / b);
        spolecne.vysledek(a, b, c);
    }

    public void P122(double a, double al, double ua) {
        double uam = a / 2 / Math.tan(al / 2);
        if (Math.abs(ua - uam) < PR) {
            spolecne.mala(a, ua);
            return;
        }
        if (ua > uam) {
            spolecne.chyba("ua", ">", uam);
            return;
        }
        Spolecne.Double3 x = spolecne.abcua(ua, al, spolecne.eta(a, ua, al));
        spolecne.vysacb(x.a, x.b, x.c);
    }

    private void Vyp123(double be, double a, double al, boolean znak) {
        Spolecne.Double2 x = spolecne.bca(a, al, be, al + be);
        spolecne.vyslacb(znak, a, x.b, x.c);
    }

    public void P123(double a, double al, double ub, boolean znak, String osa) {
        if (al > PI / 2 || Math.abs(PI / 2 - al) < PR) {
            if (ub >= a) {
                spolecne.chyba(osa, ">=", a);
                return;
            }
            Vyp123(spolecne.hodnota(spolecne.funkce1.f123, a, al, ub, PI - al, 0.0), a, al, znak);
            return;
        }
        Spolecne.Double2M m = spolecne.max(spolecne.funkce1.f123, a, al, 0.02, 0.02, 0.0);
        if (Math.abs(ub - m.h) < PR) {
            Vyp123(m.u, a, al, znak);
            return;
        }
        if (ub > m.h) {
            spolecne.chyba(osa, ">", m.h);
            return;
        }
        Vyp123(spolecne.hodnota(spolecne.funkce1.f123, a, al, ub, PI - al, m.u), a, al, znak);
        if (ub <= a) return;
        Vyp123(spolecne.hodnota(spolecne.funkce1.f123, a, al, ub, 0.0, m.u), a, al, znak);
    }

    public void P124(double a, double be, double ua, boolean znak) {
        double al = spolecne.hodnota(spolecne.funkce1.f124, a, be, ua, PI - be, 0.0);
        Spolecne.Double2 x = spolecne.bca(a, al, be, al + be);
        spolecne.vyslacb(znak, a, x.b, x.c);
    }

    public void P125(double a, double be, double ub, boolean znak, String osa) {
        double ubm = 2 * a * Math.cos(be / 2);
        if (ub >= ubm) {
            spolecne.chyba(osa, ">=", ubm);
            return;
        }
        double ga = spolecne.gaub(a, ub, be);
        Spolecne.Double2 x = spolecne.bca(a, be + ga, be, ga);
        spolecne.vyslacb(znak, a, x.b, x.c);
    }

    private void Vyp126(double ga, double a, double be, boolean znak) {
        Spolecne.Double2 x = spolecne.bca(a, be + ga, be, ga);
        spolecne.vyslacb(znak, a, x.b, x.c);
    }

    public void P126(double a, double be, double uc, boolean znak, String osa) {
        double ucm = a * Math.sin(be);
        double ucma = 2 * a * Math.sin(be / 2);
        if (Math.abs(uc - ucm) < PR && be < PI / 2 - PR) {
            Vyp126(PI - 2 * be, a, be, znak);
            return;
        }
        if (uc < ucm) {
            spolecne.chyba(osa, "<", ucm);
            return;
        }
        double e = compLib.asin(a * Math.sin(be) / uc);
        if (be > PI / 2 || Math.abs(PI / 2 - be) < PR) {
            if (uc <= a) {
                spolecne.chyba(osa, "<=", a);
                return;
            }
            if (uc >= ucma) {
                spolecne.chyba(osa, ">=", ucma);
                return;
            }
            Vyp126(2 * (PI - be - e), a, be, znak);
            return;
        }
        if (ucma >= a) {
            if (uc >= ucma) {
                spolecne.chyba(osa, ">=", ucma);
                return;
            }
            Vyp126(2 * (PI - be - e), a, be, znak);
            if (uc >= a) return;
            Vyp126(2 * e - 2 * be, a, be, znak);
            return;
        } else {
            if (uc >= a) {
                spolecne.chyba(osa, ">=", a);
                return;
            }
            Vyp126(2 * e - 2 * be, a, be, znak);
            if (uc >= ucma) return;
            Vyp126(2 * (PI - be - e), a, be, znak);
        }
    }

    public void P127(double a, double va, double ua) {
        if (ua == va) {
            spolecne.mala(a, va);
            return;
        }
        if (ua < va) {
            spolecne.chyba("ua", "<", va);
            return;
        }
        double e = compLib.acos(va / ua);
        double k = (Math.tan(e)) * (Math.tan(e));
        double al = 2 * compLib.atan(va / k / a * (-1 - k + Math.sqrt((1 + k) * (1 + k) + k * a * a / va / va)));
        Spolecne.Double2 x = spolecne.bca(a, al, PI / 2 - al / 2 - e, PI / 2 - al / 2 + e);
        spolecne.vysacb(a, x.b, x.c);
    }

    public void P128(double a, double va, double ub, boolean znak) {
        double b, c, be;
        if (a <= ub / 2) {
            spolecne.chyba("a", "<=", ub / 2);
            return;
        }
        be = spolecne.hodnota(spolecne.funkce1.f128, a, va, ub, PI, PR);
        c = va / Math.sin(be);
        b = spolecne.cs(a, c, be);
        spolecne.vyslacb(znak, a, b, c);
    }

    public void P129(double a, double vb, double ua, boolean znak) {
        if (a < vb) {
            spolecne.chyba("a", "<", vb);
            return;
        }
        double ga = compLib.asin(vb / a);
        double al = spolecne.hodnota(spolecne.funkce1.f124, a, ga, ua, PI - ga, 0.0);
        Spolecne.Double2 x = spolecne.bca(a, al, al + ga, ga);
        spolecne.vyslacb(znak, a, x.b, x.c);
        if (vb == a) return;
        ga = PI - ga;
        al = spolecne.hodnota(spolecne.funkce1.f124, a, ga, ua, PI - ga, 0.0);
        x = spolecne.bca(a, al, al + ga, ga);
        spolecne.vyslacb(znak, a, x.b, x.c);
    }

    private void Vyp130(double be, double a, double ga, boolean znak) {
        Spolecne.Double2 x = spolecne.bca(a, be + ga, be, ga);
        spolecne.vyslacb(znak, a, x.b, x.c);
    }

    public void P130(double a, double vb, double ub, boolean znak, String vyska, String osa) {
        double ga, e, ubn, ubn1;
        if (vb > a) {
            spolecne.chyba(vyska, ">", a);
            return;
        }
        if (ub < vb) {
            spolecne.chyba(osa, "<", vb);
            return;
        }
        ga = compLib.asin(vb / a);
        ubn = 2 * a * Math.sin(ga / 2);
        if (vb == a) {
            if (ub == vb) {
                spolecne.chyba(vyska + " & " + osa, "=", a);
                return;
            }
            if (ub >= ubn) {
                spolecne.chyba(osa, ">=", ubn);
                return;
            }
            Vyp130(2 * compLib.acos(vb / ub), a, PI / 2, znak);
            return;
        }
        if (ub == vb) {
            Vyp130(PI - 2 * ga, a, ga, znak);
            return;
        }
        e = compLib.acos(vb / ub);
        if (ub == a) {
            if (ubn <= a) {
                spolecne.chyba(osa, "<=", a);
                return;
            }
            Vyp130(PI - 2 * ga + 2 * e, a, ga, znak);
            return;
        } else if (ub < a) {
            Vyp130(PI - 2 * ga - 2 * e, a, ga, znak);
            if (ub >= ubn) return;
            Vyp130(PI - 2 * ga + 2 * e, a, ga, znak);
            return;
        } else {
            if (ub < ubn) Vyp130(PI - 2 * ga + 2 * e, a, ga, znak);
            ga = PI - ga;
            ubn1 = 2 * a * Math.sin(ga / 2);
            if (ub >= ubn1) {
                spolecne.chyba(osa, ">=", ubn1);
                return;
            }
            Vyp130(PI - 2 * ga + 2 * e, a, ga, znak);
        }
    }

    private void Vyp131(double ga, double a, double uc, boolean znak) {
        double be = spolecne.gaub(a, uc, ga);
        Spolecne.Double2 x = spolecne.bca(a, be + ga, be, ga);
        spolecne.vyslacb(znak, a, x.b, x.c);
    }

    public void P131(double a, double vb, double uc, boolean znak, String osa) {
        if (a < vb) {
            spolecne.chyba("a", "<", vb);
            return;
        }
        double ga = compLib.asin(vb / a);
        double ucm = 2 * a * Math.cos(ga / 2);
        if (uc > ucm) {
            spolecne.chyba(osa, ">=", ucm);
            return;
        }
        Vyp131(ga, a, uc, znak);
        ucm = 2 * a * Math.cos((PI - ga) / 2);
        if (vb == a || uc >= ucm) return;
        Vyp131(PI - ga, a, uc, znak);
    }

    public void P132(double a, double ta, double ua) {
        double uam, e;
        if (ua > ta) {
            spolecne.chyba("ua", ">", ta);
            return;
        }
        if (ta == ua) {
            spolecne.mala(a, ta);
            return;
        }
        if (ta <= a / 2) uam = 0.0; else uam = ta - a * a / 4 / ta;
        if (ua <= uam) {
            spolecne.chyba("ua", "<=", uam);
            return;
        }
        e = spolecne.hodnota(spolecne.funkce1.f132, a, ta, ua, 0.0, PI / 2);
        Spolecne.Double2 x = spolecne.bcta(a, ta, e);
        spolecne.vysacb(a, x.b, x.c);
    }

    public void P133(double a, double ta, double ub, boolean znak, String osa) {
        double ubma, ubmi, e;
        ubma = a * (a + 2 * ta) / (1.5 * a + ta);
        if (ub >= ubma) {
            spolecne.chyba(osa, ">=", ubma);
            return;
        }
        if (ta >= a / 2) ubmi = 0.0; else ubmi = a * (a - 2 * ta) / (1.5 * a - ta);
        if (ub <= ubmi) {
            spolecne.chyba(osa, "<=", ubmi);
            return;
        }
        e = spolecne.hodnota(spolecne.funkce1.f133, a, ta, ub, PI, 0.0);
        Spolecne.Double2 x = spolecne.bcta(a, ta, e);
        spolecne.vyslacb(znak, a, x.b, x.c);
    }

    public void P134(double a, double tb, double ua, boolean znak) {
        double uama, uami, e;
        uama = (a * a + 3 * a * tb + 2 * tb * tb) / (0.75 * a + tb);
        if (ua >= uama) {
            spolecne.chyba("ua", ">=", uama);
            return;
        }
        if (tb <= a && tb >= a / 2) uami = 0.0; else uami = (a * a - 3 * a * tb + 2 * tb * tb) / Math.abs(0.75 * a - tb);
        if (ua < uami) {
            spolecne.chyba("ua", "<=", uami);
            return;
        }
        e = spolecne.hodnota(spolecne.funkce1.f134, a, tb, ua, 0.0, PI);
        Spolecne.Double2 x = spolecne.bctb(a, tb, e);
        spolecne.vyslacb(znak, a, x.b, x.c);
    }

    private void Vyp135(double e, double a, double tb, boolean znak) {
        Spolecne.Double2 x = spolecne.bctb(a, tb, e);
        spolecne.vyslacb(znak, a, x.b, x.c);
    }

    public void P135(double a, double tb, double ub, boolean znak, String osa) {
        double ubm = a + a * (tb - a) / tb;
        if (tb >= a) {
            if (ub >= ubm) {
                spolecne.chyba(osa, ">=", ubm);
                return;
            }
            Vyp135(spolecne.hodnota(spolecne.funkce1.f135, a, tb, ub, PI, 0.0), a, tb, znak);
            return;
        }
        if (tb == ub) {
            spolecne.vyslacb(znak, a, 2 * Math.sqrt(a * a - tb * tb), a);
            return;
        }
        if (ub > tb) {
            spolecne.chyba(osa, ">", tb);
            return;
        }
        double em = compLib.acos(tb / a);
        Vyp135(spolecne.hodnota(spolecne.funkce1.f135, a, tb, ub, PI, em), a, tb, znak);
        if (tb <= 0.5 * a) ubm = 0.0;
        if (ub <= ubm) return;
        Vyp135(spolecne.hodnota(spolecne.funkce1.f135, a, tb, ub, 0.0, em), a, tb, znak);
    }

    public void P136(double a, double tb, double uc, boolean znak, String osa) {
        double ucma, ucmi, e;
        ucma = a + a * (a + 2 * tb) / (3 * a + 2 * tb);
        if (uc >= ucma) {
            spolecne.chyba(osa, ">=", ucma);
            return;
        }
        if (tb >= a) ucmi = 0.0; else ucmi = a + a * (a - 2 * tb) / (3 * a - 2 * tb);
        if (uc <= ucmi) {
            spolecne.chyba(osa, "<=", ucmi);
            return;
        }
        e = spolecne.hodnota(spolecne.funkce1.f136, a, tb, uc, 0.0, PI);
        Spolecne.Double2 x = spolecne.bctb(a, tb, e);
        spolecne.vyslacb(znak, a, x.b, x.c);
    }

    public void P137(double a, double ua, double ub, boolean znak) {
        double bm, cm, uam, be, bem, ga;
        if (a <= ub / 2) {
            spolecne.chyba("a", "<=", ub / 2);
            return;
        }
        bem = 2 * compLib.acos(ub / 2 / a);
        if (ub > a) {
            cm = a * ub / (2 * a - ub);
            bm = cm - a;
            uam = bm + a * bm / (bm + cm);
            if (ua <= uam) {
                spolecne.chyba("ua", "<=", uam);
                return;
            }
        }
        be = spolecne.hodnota(spolecne.funkce1.f137, a, ub, ua, 0.0, bem);
        ga = spolecne.gaub(a, ub, be);
        Spolecne.Double2 x = spolecne.bca(a, be + ga, be, ga);
        spolecne.vyslacb(znak, a, x.b, x.c);
    }

    public void P138(double a, double ub, double uc) {
        double be, bem, ga, bm, cm, ucma, ucmi;
        if (ub >= 2 * a) {
            spolecne.chyba("ub", ">=", 2 * a);
            return;
        }
        bem = 2 * compLib.acos(ub / 2 / a);
        ucma = 2 * a * Math.sin(bem / 2);
        if (uc >= ucma) {
            spolecne.chyba("uc", ">=", ucma);
            return;
        }
        if (ub < a) {
            cm = a * ub / (2 * a - ub);
            bm = Math.abs(cm - a);
            ucmi = spolecne.osauhlu(a, bm, cm);
            if (uc <= ucmi) {
                spolecne.chyba("uc", "<=", ucmi);
                return;
            }
        }
        be = spolecne.hodnota(spolecne.funkce1.f138, a, ub, uc, 0.0, bem);
        ga = spolecne.gaub(a, ub, be);
        Spolecne.Double2 x = spolecne.bca(a, be + ga, be, ga);
        spolecne.vysledek(a, x.b, x.c);
    }

    public void P139(double a, double ua, double p) {
        double va = 2 * p / a;
        P127(a, va, ua);
    }

    public void P140(double a, double ub, double p, boolean znak) {
        double va = 2 * p / a;
        P128(a, va, ub, znak);
    }

    private void Vyp141(double al, double a, double ua) {
        double e = spolecne.eta(a, ua, al);
        double be = PI / 2 - al / 2 - e;
        Spolecne.Double2 x = spolecne.bca(a, al, be, al + be);
        spolecne.vysacb(a, x.b, x.c);
    }

    public void P141(double a, double ua, double r) {
        double uama, uam, al;
        if (a > 2 * r) {
            spolecne.chyba("a", ">", 2 * r);
            return;
        }
        al = compLib.asin(a / 2 / r);
        uama = r * (1 + Math.cos(al));
        if (Math.abs(ua - uama) < PR) {
            spolecne.mala(a, ua);
            return;
        }
        if (ua > uama) {
            spolecne.chyba("ua", ">", uama);
            return;
        }
        if (a == 2 * r) {
            Vyp141(PI / 2, a, ua);
            return;
        }
        Vyp141(al, a, ua);
        uam = r * (1 + Math.cos(PI - al));
        if (Math.abs(ua - uam) < PR) {
            spolecne.mala(a, uam);
            return;
        }
        if (ua > uam) return;
        Vyp141(PI - al, a, ua);
    }

    private void Vyp142(double be, double a, double al, boolean znak) {
        Spolecne.Double2 x = spolecne.bca(a, al, be, al + be);
        spolecne.vyslacb(znak, a, x.b, x.c);
    }

    public void P142(double a, double ub, double r, boolean znak, String osa) {
        if (a > 2 * r) {
            spolecne.chyba("a", ">", 2 * r);
            return;
        }
        double al = compLib.asin(a / 2 / r);
        Spolecne.Double2M m = spolecne.max(spolecne.funkce1.f142, a, al, 0.02, 0.02, 0.0);
        if (Math.abs(ub - m.h) < PR) {
            Vyp142(m.u, a, al, znak);
            return;
        }
        if (ub > m.h) {
            spolecne.chyba(osa, ">", m.h);
            return;
        }
        Vyp142(spolecne.hodnota(spolecne.funkce1.f142, a, al, ub, PI - al, m.u), a, al, znak);
        if (ub == a || a == 2 * r) return;
        if (ub > a) Vyp142(spolecne.hodnota(spolecne.funkce1.f142, a, al, ub, 0.0, m.u), a, al, znak); else {
            al = PI - al;
            Vyp142(spolecne.hodnota(spolecne.funkce1.f142, a, al, ub, PI - al, 0.0), a, al, znak);
        }
    }

    public void P143(double a, double ua, double ro) {
        double rom, al, e, em;
        rom = a / 4 / ua * (Math.sqrt(a * a + 4 * ua * ua) - a);
        if (Math.abs(ro - rom) < PR) {
            spolecne.mala(a, ua);
            return;
        }
        if (ro > rom) {
            spolecne.chyba("ro", ">", rom);
            return;
        }
        em = PI / 2 - compLib.asin(2 * ro / ua);
        e = spolecne.hodnota(spolecne.funkce1.f143, ua, ro, a, 0.0, em);
        al = 2 * compLib.asin(ro / (ua - ro / Math.cos(e)));
        Spolecne.Double3 x = spolecne.abcua(ua, al, e);
        spolecne.vysacb(x.a, x.b, x.c);
    }

    public void P144(double a, double ub, double ro, boolean znak, String osa) {
        double ubmi, ubma, be, bem, ga;
        if (ro >= a / 2) {
            spolecne.chyba("ro", ">=", a / 2);
            return;
        }
        ubmi = 2 * Math.sqrt(a * a / 2 - a * Math.sqrt(a * a / 4 - ro * ro));
        if (ub <= ubmi) {
            spolecne.chyba(osa, "<=", ubmi);
            return;
        }
        ubma = 2 * Math.sqrt(a * a / 2 + a * Math.sqrt(a * a / 4 - ro * ro));
        if (ub >= ubma) {
            spolecne.chyba(osa, ">=", ubma);
            return;
        }
        bem = 2 * compLib.atan(ro / (a / 2 + Math.sqrt(a * a / 4 - ro * ro)));
        be = spolecne.hodnota(spolecne.funkce1.f144, a, ro, ub, PI - bem, bem);
        ga = spolecne.garo(a, ro, be);
        Spolecne.Double2 x = spolecne.bca(a, be + ga, be, ga);
        spolecne.vyslacb(znak, a, x.b, x.c);
    }

    public void P145(double al, double be, double ua, String osa) {
        double a, b, c, ga;
        ga = PI - al - be;
        if (al + be >= PI) {
            spolecne.chyba2("alfa_beta", ">=", 180.0);
            return;
        }
        if (osa.equals("ua")) {
            a = spolecne.strux(ua, al, be);
            Spolecne.Double2 x = spolecne.bca(a, al, be, ga);
            spolecne.vysledek(a, x.b, x.c);
        } else if (osa.equals("ub")) {
            b = spolecne.strux(ua, be, al);
            Spolecne.Double2 x = spolecne.bca(b, be, al, ga);
            spolecne.vysledek(x.b, b, x.c);
        } else {
            c = spolecne.strux(ua, ga, be);
            Spolecne.Double2 x = spolecne.bca(c, ga, al, be);
            spolecne.vysledek(x.b, x.c, c);
        }
    }

    public void P146(double al, double va, double ua) {
        if (va == ua) {
            spolecne.malux(al, va);
            return;
        }
        if (ua < va) {
            spolecne.chyba("ua", "<", va);
            return;
        }
        if (ua >= va / Math.sin(al / 2)) {
            spolecne.chyba("ua", ">=", va / Math.sin(al / 2));
            return;
        }
        double e = compLib.acos(va / ua);
        Spolecne.Double3 x = spolecne.abcua(ua, al, e);
        spolecne.vysacb(x.a, x.b, x.c);
    }

    private void Vyp147(double be, double al, double va, boolean znak) {
        Spolecne.Double3 x = spolecne.vabega(va, be, PI - al - be);
        spolecne.vyslacb(znak, x.a, x.b, x.c);
    }

    public void P147(double al, double va, double ub, boolean znak, String osa) {
        double ubk = va / Math.cos(al / 2);
        Spolecne.Double2M n = spolecne.min(spolecne.funkce1.f147, al, va, 0.2, 0.02, 100000);
        if (Math.abs(ub - n.h) < PR) {
            Vyp147(n.u, al, va, znak);
            return;
        }
        if (ub < n.h) {
            spolecne.chyba(osa, "<", n.h);
            return;
        }
        Vyp147(spolecne.hodnota(spolecne.funkce1.f147, al, va, ub, n.u, 0.0), al, va, znak);
        if (ub >= ubk) return;
        Vyp147(spolecne.hodnota(spolecne.funkce1.f147, al, va, ub, n.u, PI - al), al, va, znak);
    }

    public void P148(double al, double vb, double ua, boolean znak) {
        if (ua >= vb / Math.sin(al / 2)) {
            spolecne.chyba("ua", ">=", vb / Math.sin(al / 2));
            return;
        }
        double c = vb / Math.sin(al);
        double be = spolecne.gaub(c, ua, al);
        Spolecne.Double2 x = spolecne.bca(c, al + be, al, be);
        spolecne.vyslacb(znak, x.b, x.c, c);
    }

    private void Vyp149(double be, double al, double c, boolean znak) {
        Spolecne.Double2 x = spolecne.bca(c, al + be, al, be);
        spolecne.vyslacb(znak, x.b, x.c, c);
    }

    public void P149(double al, double vb, double ub, boolean znak, String osa) {
        double c, ubn, e;
        c = vb / Math.sin(al);
        if (ub < vb) {
            spolecne.chyba("ub", "<", vb);
            return;
        }
        e = compLib.asin(vb / ub);
        ubn = vb / Math.cos(al / 2);
        if (al > PI / 2 || Math.abs(al - PI / 2) < PR) {
            if (ub <= c) {
                spolecne.chyba(osa, "<=", c);
                return;
            }
            if (ub >= ubn) {
                spolecne.chyba(osa, ">=", ubn);
                return;
            }
            Vyp149(2 * (PI - al - e), al, c, znak);
            return;
        } else if (al < PI / 3) {
            if (ub >= c) {
                spolecne.chyba(osa, ">=", c);
                return;
            }
            Vyp149(2 * e - 2 * al, al, c, znak);
            if (ub >= ubn || ub == vb) return;
            Vyp149(2 * (PI - al - e), al, c, znak);
            return;
        } else {
            if (ub >= ubn) {
                spolecne.chyba(osa, ">=", ubn);
                return;
            }
            Vyp149(2 * (PI - al - e), al, c, znak);
            if (ub >= c || ub == vb) return;
            Vyp149(2 * e - 2 * al, al, c, znak);
            return;
        }
    }

    public void P150(double al, double vb, double uc, boolean znak) {
        double c = vb / Math.sin(al);
        double be = spolecne.hodnota(spolecne.funkce1.f150, al, c, uc, 0.0, PI - al);
        Spolecne.Double2 x = spolecne.bca(c, al + be, al, be);
        spolecne.vyslacb(znak, x.b, x.c, c);
    }

    public void P151(double al, double ta, double ua) {
        if (ta < ua) {
            spolecne.chyba("ta", "<", ua);
            return;
        }
        if (ta == ua) {
            spolecne.malux(al, ta);
            return;
        }
        double e = spolecne.hodnota(spolecne.funkce1.f151, al, ua, ta, 0.0, PI / 2 - al / 2);
        Spolecne.Double3 x = spolecne.abcua(ua, al, e);
        spolecne.vysacb(x.a, x.b, x.c);
    }

    private void Vyp152(double be, double al, double ub, boolean znak) {
        Spolecne.Double3 x = spolecne.abcub(ub, al, be);
        spolecne.vyslacb(znak, x.a, x.b, x.c);
    }

    public void P152(double al, double ta, double ub, boolean znak) {
        if (al <= PI / 2) {
            if (ta <= ub / 2) {
                spolecne.chyba("ta", "<=", ub / 2);
                return;
            }
            Vyp152(spolecne.hodnota(spolecne.funkce1.f152, al, ub, ta, 0.0, PI - al), al, ub, znak);
            return;
        }
        Spolecne.Double2M n = spolecne.min(spolecne.funkce1.f152, al, ub, 0.01, 0.02, ub);
        if (Math.abs(ta - n.h) < PR) {
            Vyp152(n.u, al, ub, znak);
            return;
        }
        if (ta < n.h) {
            spolecne.chyba("ta", "<", n.h);
            return;
        }
        Vyp152(spolecne.hodnota(spolecne.funkce1.f152, al, ub, ta, n.u, PI - al), al, ub, znak);
        if (ta >= ub / 2) return;
        Vyp152(spolecne.hodnota(spolecne.funkce1.f152, al, ub, ta, n.u, 0.0), al, ub, znak);
    }

    private void Vyp153(double e, double al, double ua, boolean znak) {
        Spolecne.Double3 x = spolecne.abcua(ua, al, e);
        spolecne.vyslacb(znak, x.a, x.b, x.c);
    }

    public void P153(double al, double tb, double ua, boolean znak, String tez) {
        Spolecne.Double2M n = spolecne.min(spolecne.funkce1.f153, al, ua, 0.0, -0.02, 100000);
        if (Math.abs(tb - n.h) < PR) {
            Vyp153(n.u, al, ua, znak);
            return;
        }
        if (tb < n.h) {
            spolecne.chyba(tez, "<", n.h);
            return;
        }
        Vyp153(spolecne.hodnota(spolecne.funkce1.f153, al, ua, tb, n.u, PI / 2 - al / 2), al, ua, znak);
        Vyp153(spolecne.hodnota(spolecne.funkce1.f153, al, ua, tb, n.u, al / 2 - PI / 2), al, ua, znak);
    }

    private void Vyp154(double be, double al, double ub, boolean znak) {
        Spolecne.Double3 x = spolecne.abcub(ub, al, be);
        spolecne.vyslacb(znak, x.a, x.b, x.c);
    }

    public void P154(double al, double tb, double ub, boolean znak, String tez) {
        if (tb < ub) {
            spolecne.chyba(tez, "<", ub);
            return;
        }
        if (al > PI / 2 || Math.abs(PI / 2 - al) < PR) {
            if (tb == ub) {
                spolecne.chyba(tez, "=", ub);
                return;
            }
            Vyp154(spolecne.hodnota(spolecne.funkce1.f154, al, ub, tb, 0.0, PI - al), al, ub, znak);
            return;
        }
        if (tb == ub) {
            Vyp154(PI - 2 * al, al, ub, znak);
            return;
        }
        Spolecne.Double2M m = spolecne.max(spolecne.funkce1.f154, al, ub, 0.01, 0.02, 0.0);
        Vyp154(spolecne.hodnota(spolecne.funkce1.f154, al, ub, tb, PI - 2 * al, PI - al), al, ub, znak);
        if (Math.abs(tb - m.h) < PR) {
            Vyp154(m.u, al, ub, znak);
            return;
        }
        if (tb > m.h) return;
        Vyp154(spolecne.hodnota(spolecne.funkce1.f154, al, ub, tb, 0.0, m.u), al, ub, znak);
        Vyp154(spolecne.hodnota(spolecne.funkce1.f154, al, ub, tb, PI - 2 * al, m.u), al, ub, znak);
    }

    private void Vyp155(double ga, double al, double uc, boolean znak) {
        double a = spolecne.strtux(uc, al, ga);
        Spolecne.Double2 x = spolecne.bca(a, al, PI - al - ga, ga);
        spolecne.vyslacb(znak, a, x.b, x.c);
    }

    public void P155(double al, double tb, double uc, boolean znak, String tez) {
        if (al > PI / 2 || Math.abs(PI / 2 - al) < PR) {
            if (tb <= uc / 2) {
                spolecne.chyba(tez, "<=", uc / 2);
                return;
            }
            Vyp155(spolecne.hodnota(spolecne.funkce1.f155, al, uc, tb, 0.0, PI - al), al, uc, znak);
            return;
        }
        Spolecne.Double2M n = spolecne.min(spolecne.funkce1.f155, al, uc, 0.01, 0.02, 10000);
        if (Math.abs(tb - n.h) < PR) {
            Vyp155(n.u, al, uc, znak);
            return;
        }
        if (tb < n.h) {
            spolecne.chyba(tez, "<", n.h);
            return;
        }
        Vyp155(spolecne.hodnota(spolecne.funkce1.f155, al, uc, tb, n.u, PI - al), al, uc, znak);
        if (tb >= uc / 2) return;
        Vyp155(spolecne.hodnota(spolecne.funkce1.f155, al, uc, tb, n.u, 0.0), al, uc, znak);
    }

    public void P156(double al, double ua, double ub, boolean znak, String osa) {
        double ubm = ua * Math.tan(al / 2);
        if (ub <= ubm) {
            spolecne.chyba(osa, "<=", ubm);
            return;
        }
        double be = spolecne.hodnota(spolecne.funkce1.f156, al, ua, ub, PI - al, 0.0);
        Spolecne.Double3 x = spolecne.abcual(ua, al, be);
        spolecne.vyslacb(znak, x.a, x.b, x.c);
    }

    public void P157(double al, double ub, double uc) {
        double be = spolecne.hodnota(spolecne.funkce1.f157, al, ub, uc, 0.0, PI - al);
        Spolecne.Double3 x = spolecne.abcub(ub, al, be);
        spolecne.vysledek(x.a, x.b, x.c);
    }

    public void P158(double al, double ua, double p) {
        double pm = ua * ua * Math.tan(al / 2);
        if (Math.abs(p - pm) < PR) {
            spolecne.malux(al, ua);
            return;
        }
        if (p < pm) {
            spolecne.chyba("p", "<", pm);
            return;
        }
        double be = spolecne.hodnota(spolecne.funkce1.f158, al, ua, p, PI / 2 - al / 2, 0.0);
        Spolecne.Double3 x = spolecne.abcual(ua, al, be);
        spolecne.vysacb(x.a, x.b, x.c);
    }

    public void P159(double al, double ub, double p, boolean znak) {
        double be = spolecne.hodnota(spolecne.funkce1.f159, al, ub, p, 0.0, PI - al);
        Spolecne.Double3 x = spolecne.abcub(ub, al, be);
        spolecne.vyslacb(znak, x.a, x.b, x.c);
    }

    public void P160(double al, double ua, double r) {
        double uam = r * (1 + Math.cos(al));
        if (Math.abs(ua - uam) < PR) {
            spolecne.malux(al, ua);
            return;
        }
        if (ua > uam) {
            spolecne.chyba("ua", ">", uam);
            return;
        }
        double be = spolecne.hodnota(spolecne.funkce1.f160, al, r, ua, 0.0, PI / 2 - al / 2);
        Spolecne.Double3 x = spolecne.abcual(ua, al, be);
        spolecne.vysacb(x.a, x.b, x.c);
    }

    private void Vyp161(double be, double al, double a, boolean znak) {
        Spolecne.Double2 x = spolecne.bca(a, al, be, al + be);
        spolecne.vyslacb(znak, a, x.b, x.c);
    }

    public void P161(double al, double ub, double r, boolean znak, String osa) {
        double a = 2 * r * Math.sin(al);
        if (al > PI / 2 || Math.abs(PI / 2 - al) < PR) {
            if (ub >= a) {
                spolecne.chyba(osa, ">=", a);
                return;
            }
            Vyp161(spolecne.hodnota(spolecne.funkce1.f161, al, r, ub, PI - al, 0.0), al, a, znak);
            return;
        }
        Spolecne.Double2M m = spolecne.max(spolecne.funkce1.f161, al, r, 0.01, 0.02, 0.0);
        if (Math.abs(ub - m.h) < PR) {
            Vyp161(m.u, al, a, znak);
            return;
        }
        if (ub > m.h) {
            spolecne.chyba(osa, ">", m.h);
            return;
        }
        Vyp161(spolecne.hodnota(spolecne.funkce1.f161, al, r, ub, PI - al, m.u), al, a, znak);
        if (ub <= a) return;
        Vyp161(spolecne.hodnota(spolecne.funkce1.f161, al, r, ub, 0.0, m.u), al, a, znak);
    }

    public void P162(double al, double ua, double ro) {
        double uama = 2 * ro / Math.sin(al / 2);
        double uami = ro * (1 + 1 / Math.sin(al / 2));
        if (ua > uama - PR) {
            spolecne.chyba("ua", ">=", uama);
            return;
        }
        if (Math.abs(ua - uami) < PR) {
            spolecne.malux(al, ua);
            return;
        }
        if (ua < uami) {
            spolecne.chyba("ua", "<", uami);
            return;
        }
        double be = spolecne.hodnota(spolecne.funkce1.f162, al, ro, ua, PI / 2 - al / 2, 0.0);
        Spolecne.Double3 x = spolecne.abcro(ro, al, be, PI - al - be);
        spolecne.vysacb(x.a, x.b, x.c);
    }

    public void P163(double al, double ub, double ro, boolean znak) {
        double rom = ub / 2 * Math.cos(al / 2);
        if (ro >= rom - PR) {
            spolecne.chyba("ro", ">=", rom);
            return;
        }
        double be = spolecne.hodnota(spolecne.funkce1.f163, al, ro, ub, PI - al, 0.0);
        Spolecne.Double3 x = spolecne.abcro(ro, al, be, PI - al - be);
        spolecne.vyslacb(znak, x.a, x.b, x.c);
    }

    private void Vyp164(double al, double va, double e, boolean znak) {
        Spolecne.Double3 x = spolecne.abcva(va, al, e);
        spolecne.vyslbac(znak, x.a, x.b, x.c);
    }

    public void P164(double va, double vb, double ua, boolean znak) {
        if (va > ua) {
            spolecne.chyba("va", ">", ua);
            return;
        }
        double e = compLib.acos(va / ua);
        if (e == 0 && vb >= 2 * va) {
            spolecne.chyba("vb", ">=", 2 * va);
            return;
        }
        Vyp164(spolecne.hodnota(spolecne.funkce1.f164, va, e, vb, 0.0, PI - Math.abs(2 * e)), va, e, znak);
        if (va == ua) return;
        e = -e;
        if (e <= -PI / 4) {
            if (vb >= va) return;
            Vyp164(spolecne.hodnota(spolecne.funkce1.f164, va, e, vb, 0.0, PI - Math.abs(2 * e)), va, e, znak);
            return;
        }
        Spolecne.Double2M m = spolecne.max(spolecne.funkce1.f164, va, e, 0.01, 0.02, 0.0);
        if (Math.abs(vb - m.h) < PR) {
            Vyp164(m.u, va, e, znak);
            return;
        }
        if (vb > m.h) return;
        Vyp164(spolecne.hodnota(spolecne.funkce1.f164, va, e, vb, 0.0, m.u), va, e, znak);
        if (vb <= va) return;
        Vyp164(spolecne.hodnota(spolecne.funkce1.f164, va, e, vb, PI - Math.abs(2 * e), m.u), va, e, znak);
    }

    public void P166(double va, double vb, double uc) {
        double a, b, c, ucm, ga;
        ucm = va * vb / (va + vb);
        if (uc <= ucm) {
            spolecne.chyba("uc", "<=", ucm);
            return;
        }
        ga = 2 * compLib.asin(ucm / uc);
        a = vb / Math.sin(ga);
        b = va / Math.sin(ga);
        c = spolecne.cs(a, b, ga);
        spolecne.vysledek(a, b, c);
    }

    public void P167(double va, double ta, double ua) {
        if (ua <= va) {
            spolecne.chyba("ua", "<=", va);
            return;
        }
        if (ta <= ua) {
            spolecne.chyba("ta", "<=", ua);
            return;
        }
        double e = compLib.acos(va / ua);
        double al = spolecne.altaua(ta, ua, e);
        Spolecne.Double3 x = spolecne.abcva(va, al, e);
        spolecne.vysacb(x.a, x.b, x.c);
    }

    public void P168(double va, double ta, double ub, boolean znak) {
        if (ta < va) {
            spolecne.chyba("ta", "<", va);
            return;
        }
        double e = compLib.asin(va / ta);
        double be = spolecne.hodnota(spolecne.funkce1.f168, va, e, ub, e, 0.0);
        Spolecne.Double3 x = spolecne.abcubva(va, ub, be);
        spolecne.vyslacb(znak, x.a, x.b, x.c);
        if (ta == va) return;
        e = PI - e;
        be = spolecne.hodnota(spolecne.funkce1.f168, va, e, ub, e, 0.0);
        x = spolecne.abcubva(va, ub, be);
        spolecne.vyslacb(znak, x.a, x.b, x.c);
    }

    private void Vyp169(double e, double va, double k, double tae, boolean znak) {
        double q = -3 * (1 + tae * tae);
        double m = Math.sqrt(q * q - 4 * tae * (1 + k * tae) * (tae - k));
        double n = 2 * tae * (1 + k * tae);
        double al = 2 * compLib.atan((q + m) / n);
        Spolecne.Double3 x = spolecne.abcva(va, al, e);
        spolecne.vyslacb(znak, x.a, x.b, x.c);
    }

    public void P169(double va, double tb, double ua, boolean znak) {
        if (va > 2 * tb) {
            spolecne.chyba("va", ">", 2 * tb);
            return;
        }
        if (va > ua) {
            spolecne.chyba("va", ">", ua);
            return;
        }
        if (ua == va && tb == va / 2) {
            spolecne.chyba("va & ua", "=", 2 * tb);
            return;
        }
        if (ua == va) {
            spolecne.mala(4 * Math.sqrt(tb * tb - va * va / 4) / 3, va);
            return;
        }
        double e = compLib.acos(va / ua);
        double k = 2 / va * Math.sqrt(tb * tb - va * va / 4);
        double tae = Math.tan(e);
        if (tb == va / 2 || tb == ua / 2) {
            Vyp169(-e, va, k, -tae, znak);
            return;
        }
        if (tb < ua / 2) {
            Vyp169(-e, va, k, -tae, znak);
            Vyp169(-e, va, -k, -tae, znak);
            return;
        } else {
            Vyp169(e, va, k, tae, znak);
            Vyp169(-e, va, k, -tae, znak);
        }
    }

    private void Vyp170(double ga, double va, double e, boolean znak) {
        Spolecne.Double3 x = spolecne.vaega(va, e, ga);
        spolecne.vyslacb(znak, x.a, x.b, x.c);
    }

    public void P170(double va, double tb, double ub, boolean znak, String tez, String osa) {
        if (tb < ub) {
            spolecne.chyba(tez, "<", ub);
            return;
        }
        if (tb < va / 2) {
            spolecne.chyba(tez, "<", va / 2);
            return;
        }
        double e = compLib.asin(va / 2 / tb);
        if (tb == ub) {
            if (tb == va / 2) {
                spolecne.chyba(tez + " & " + osa, "=", va / 2);
                return;
            }
            Vyp170(PI / 2 - e, va, e, znak);
            return;
        }
        Vyp170(spolecne.hodnota(spolecne.funkce1.f170, va, e, ub, PI - e, PI / 2 - e), va, e, znak);
        if (tb == va / 2 || ub == va / 2) return;
        if (ub < va / 2) {
            e = PI - e;
            Vyp170(spolecne.hodnota(spolecne.funkce1.f170, va, e, ub, PI - e, 0.0), va, e, znak);
        } else Vyp170(spolecne.hodnota(spolecne.funkce1.f170, va, e, ub, 0.0, PI / 2 - e), va, e, znak);
    }

    public void P171(double va, double tb, double uc, boolean znak) {
        if (va > 2 * tb) {
            spolecne.chyba("va", ">", 2 * tb);
            return;
        }
        double e = compLib.asin(va / 2 / tb);
        double ga = spolecne.hodnota(spolecne.funkce1.f171, va, e, uc, PI - e, 0.0);
        Spolecne.Double3 x = spolecne.vaega(va, e, ga);
        spolecne.vyslacb(znak, x.a, x.b, x.c);
        if (tb == va / 2) return;
        e = PI - e;
        ga = spolecne.hodnota(spolecne.funkce1.f171, va, e, uc, PI - e, 0.0);
        x = spolecne.vaega(va, e, ga);
        spolecne.vyslacb(znak, x.a, x.b, x.c);
    }

    public void P172(double va, double ua, double ub, boolean znak) {
        if (ua < va) {
            spolecne.chyba("ua", "<", va);
            return;
        }
        double e = compLib.acos(va / ua);
        double al = spolecne.hodnota(spolecne.funkce1.f172, va, e, ub, 0.0, PI - Math.abs(2 * e));
        Spolecne.Double3 x = spolecne.abcva(va, al, e);
        spolecne.vyslacb(znak, x.a, x.b, x.c);
        if (ua == va || ub >= ua / Math.tan(Math.abs(e))) return;
        e = -e;
        al = spolecne.hodnota(spolecne.funkce1.f172, va, e, ub, 0.0, PI - Math.abs(2 * e));
        x = spolecne.abcva(va, al, e);
        spolecne.vyslacb(znak, x.a, x.b, x.c);
    }

    public void P173(double va, double ub, double uc) {
        double be;
        Spolecne.Double3 x;
        if (va < ub) {
            be = spolecne.hodnota(spolecne.funkce1.f173, va, ub, uc, 0.0, 2 * compLib.asin(va / ub));
            x = spolecne.vabega(va, be, spolecne.gavaub(va, ub, be));
            spolecne.vysledek(x.a, x.b, x.c);
        } else {
            be = spolecne.hodnota(spolecne.funkce1.f173, va, ub, uc, 0.0, PI);
            x = spolecne.vabega(va, be, spolecne.gavaub(va, ub, be));
            spolecne.vysledek(x.a, x.b, x.c);
        }
    }

    public void P174(double va, double ua, double p) {
        if (ua == va) {
            spolecne.mala(2 * p / va, va);
            return;
        }
        if (ua < va) {
            spolecne.chyba("ua", "<", va);
            return;
        }
        double e = compLib.acos(va / ua);
        double al = spolecne.alpua(ua, p, e);
        Spolecne.Double3 x = spolecne.abcua(ua, al, e);
        spolecne.vysacb(x.a, x.b, x.c);
    }

    public void P175(double va, double ub, double p, boolean znak, String osa) {
        double a, b, c, be;
        a = 2 * p / va;
        if (ub >= 2 * a) {
            spolecne.chyba(osa, ">=", 2 * a);
            return;
        }
        be = spolecne.hodnota(spolecne.funkce1.f175, a, va, ub, PI, 0.0);
        c = va / Math.sin(be);
        b = spolecne.cs(a, c, be);
        spolecne.vyslacb(znak, a, b, c);
    }

    public void P176(double va, double ua, double r) {
        double uam, al, alm, e;
        alm = compLib.acos(va / r - 1);
        if (ua == va) {
            spolecne.malux(alm, va);
            return;
        }
        if (ua < va) {
            spolecne.chyba("ua", "<", va);
            return;
        }
        uam = Math.sqrt(2 * r * va);
        if (ua >= uam) {
            spolecne.chyba("ua", ">=", uam);
            return;
        }
        e = compLib.acos(va / ua);
        al = spolecne.hodnota(spolecne.funkce1.f176, va, e, r, 0.0, PI - 2 * e);
        Spolecne.Double3 x = spolecne.abcrval(r, va, al);
        spolecne.vysacb(x.a, x.b, x.c);
    }

    private void Vyp177(double al, double r, double va, boolean znak, boolean tp) {
        Spolecne.Double3 x = spolecne.abcrval(r, va, al);
        if (tp == true) spolecne.vyslacb(znak, x.a, x.c, x.b); else spolecne.vyslacb(znak, x.a, x.b, x.c);
    }

    public void P177(double va, double ub, double r, boolean znak, String osa) {
        if (va >= 2 * r) {
            spolecne.chyba("va", ">=", 2 * r);
            return;
        }
        double aln = compLib.acos(va / r - 1);
        double ubn = spolecne.funkce1.f177.f(r, va, aln, false);
        Spolecne.Double2M m = spolecne.max1(spolecne.funkce1.f177, r, va, 0.02, 0.02, -1.0, false);
        if (Math.abs(ub - m.h) < PR) {
            Vyp177(m.u, r, va, znak, false);
            return;
        }
        if (ub > m.h) {
            spolecne.chyba(osa, ">", m.h);
            return;
        }
        Vyp177(spolecne.hodnota1(spolecne.funkce1.f177, r, va, ub, 0.0, m.u, false), r, va, znak, false);
        if (Math.abs(ub - ubn) < PR) {
            Vyp177(aln, r, va, znak, false);
            return;
        }
        if (ub > ubn) Vyp177(spolecne.hodnota1(spolecne.funkce1.f177, r, va, ub, aln, m.u, false), r, va, znak, false); else Vyp177(spolecne.hodnota1(spolecne.funkce1.f177, r, va, ub, 0.0, aln, true), r, va, znak, true);
    }

    public void P178(double va, double ua, double ro) {
        if (ua < va) {
            spolecne.chyba("ua", "<", va);
            return;
        }
        if (ro >= va / 2) {
            spolecne.chyba("ro", ">=", va / 2);
            return;
        }
        double e = compLib.acos(va / ua);
        double al = 2 * compLib.asin(ro * va / ua / (va - ro));
        if (ua == va) {
            spolecne.malux(al, va);
            return;
        }
        Spolecne.Double3 x = spolecne.abcva(va, al, e);
        spolecne.vysacb(x.a, x.b, x.c);
    }

    public void P179(double va, double ub, double ro, boolean znak) {
        double c, al, be;
        if (ro >= va / 2) {
            spolecne.chyba("ro", ">=", va / 2);
            return;
        }
        if (ro >= ub / 2) {
            spolecne.chyba("ro", ">=", ub / 2);
            return;
        }
        be = spolecne.hodnota(spolecne.funkce1.f179, va, ro, ub, PI, 0.0);
        c = va / Math.sin(be);
        al = spolecne.garo(c, ro, be);
        Spolecne.Double2 x = spolecne.bca(c, al + be, al, be);
        spolecne.vyslacb(znak, x.b, x.c, c);
    }

    private void Vyp180(double e, double ta, double tb) {
        Spolecne.Double3 x = spolecne.abctatb(ta, tb, e);
        spolecne.vysledek(x.a, x.b, x.c);
    }

    public void P180(double ta, double tb, double ua) {
        double uami, uama, em;
        if (ta < ua) {
            spolecne.chyba("ta", "<", ua);
            return;
        }
        if (tb >= 2 * ta) uami = 0.0; else uami = spolecne.funkce1.f180.f(ta, tb, 0.0);
        if (tb >= ta) uama = 0.0; else uama = spolecne.funkce1.f180.f(ta, tb, PI);
        if (tb > ta / 2) {
            if (ua <= uama) {
                spolecne.chyba("ua", "<=", uama);
                return;
            }
            em = compLib.acos(ta / 2 / tb);
            if (ta == ua) {
                Vyp180(em, ta, tb);
                return;
            }
            Vyp180(spolecne.hodnota(spolecne.funkce1.f180, ta, tb, ua, PI, em), ta, tb);
            if (ua <= uami) return;
            Vyp180(spolecne.hodnota(spolecne.funkce1.f180, ta, tb, ua, 0.0, em), ta, tb);
        } else {
            if (ua >= uami) {
                spolecne.chyba("ua", ">=", uami);
                return;
            }
            if (ua <= uama) {
                spolecne.chyba("ua", "<=", uama);
                return;
            }
            Vyp180(spolecne.hodnota(spolecne.funkce1.f180, ta, tb, ua, PI, 0.0), ta, tb);
        }
    }

    private void Vyp181(double e, double ta, double tb) {
        Spolecne.Double3 x = spolecne.abctatb(ta, tb, e);
        spolecne.vysledek(x.a, x.b, x.c);
    }

    public void P181(double ta, double tb, double ub) {
        double ubmi, ubma, em;
        if (tb < ub) {
            spolecne.chyba("tb", "<", ub);
            return;
        }
        if (tb <= 0.5 * ta) ubmi = 0.0; else ubmi = spolecne.funkce1.f181.f(ta, tb, 0.0);
        if (tb <= ta) ubma = 0.0; else ubma = spolecne.funkce1.f181.f(ta, tb, PI);
        if (tb < 2 * ta) {
            if (ub <= ubma) {
                spolecne.chyba("ub", "<=", ubma);
                return;
            }
            em = compLib.acos(tb / 2 / ta);
            if (tb == ub) {
                Vyp181(em, ta, tb);
                return;
            }
            Vyp181(spolecne.hodnota(spolecne.funkce1.f181, ta, tb, ub, PI, em), ta, tb);
            if (ub <= ubmi) return;
            Vyp181(spolecne.hodnota(spolecne.funkce1.f181, ta, tb, ub, 0.0, em), ta, tb);
        } else {
            if (ub >= ubmi) {
                spolecne.chyba("ub", ">=", ubmi);
                return;
            }
            if (ub <= ubma) {
                spolecne.chyba("ub", "<=", ubma);
                return;
            }
            Vyp181(spolecne.hodnota(spolecne.funkce1.f181, ta, tb, ub, PI, 0.0), ta, tb);
        }
    }

    public void P182(double ta, double tb, double uc) {
        double uco, ucpi, e;
        if (tb >= 0.5 * ta && tb <= 2 * ta) uco = 0.0; else uco = spolecne.funkce1.f182.f(ta, tb, 0.0);
        ucpi = spolecne.funkce1.f182.f(ta, tb, PI);
        if (uc <= uco) {
            spolecne.chyba("uc", "<=", uco);
            return;
        }
        if (uc >= ucpi) {
            spolecne.chyba("uc", ">=", ucpi);
            return;
        }
        e = spolecne.hodnota(spolecne.funkce1.f182, ta, tb, uc, 0.0, PI);
        Spolecne.Double3 x = spolecne.abctatb(ta, tb, e);
        spolecne.vysledek(x.a, x.b, x.c);
    }

    private void Vyp183(double e, double ta, double ua, boolean znak) {
        double al = spolecne.altaua(ta, ua, e);
        Spolecne.Double3 x = spolecne.abcua(ua, al, e);
        spolecne.vyslacb(znak, x.a, x.b, x.c);
    }

    public void P183(double ta, double ua, double ub, boolean znak) {
        double ubb, ubm, al, alp, alk;
        if (ua > ta) {
            spolecne.chyba("ua", ">", ta);
            return;
        }
        if (ua == ta) {
            alp = 0;
            alk = PI / 2;
            do {
                al = (alp + alk) / 2;
                ubb = spolecne.osauhlu(2 * ta * Math.tan(al), ta / Math.cos(al), ta / Math.cos(al));
                if (ubb < ub) alp = al; else alk = al;
            } while (Math.abs(ub - ubb) < 1.0E-10);
            spolecne.malux(2 * al, ta);
            return;
        }
        Vyp183(spolecne.hodnota(spolecne.funkce1.f183, ta, ua, ub, -PI / 2, 0.0), ta, ua, znak);
        ubm = spolecne.funkce1.f183.f(ta, ua, PI / 2 - PR);
        if (ub <= ubm) return;
        Vyp183(spolecne.hodnota(spolecne.funkce1.f183, ta, ua, ub, PI / 2, 0.0), ta, ua, znak);
    }

    public void P184(double ta, double ub, double uc) {
        double ucm, be, bem, e;
        bem = spolecne.hodnota(spolecne.funkce1.f184, ta, PI - PR, ub, PI - PR, 0.0);
        Spolecne.Double3 x = spolecne.abctaub(ta, PI - PR, bem);
        ucm = spolecne.osauhlu(x.a, x.b, x.c);
        if (uc >= ucm) {
            spolecne.chyba("uc", ">=", ucm);
            return;
        }
        e = spolecne.hodnota(spolecne.funkce1.f184a, ta, ub, uc, 0.0, PI);
        be = spolecne.hodnota(spolecne.funkce1.f184, ta, e, ub, e, 0.0);
        x = spolecne.abctaub(ta, e, be);
        spolecne.vysledek(x.a, x.b, x.c);
    }

    public void P185(double ta, double ua, double p) {
        if (ua > ta) {
            spolecne.chyba("ua", ">", ta);
            return;
        }
        if (ua == ta) {
            spolecne.mala(2 * p / ta, ta);
            return;
        }
        double e = spolecne.hodnota(spolecne.funkce1.f185, ta, ua, p, PI / 2, 0.0);
        double al = spolecne.altaua(ta, ua, e);
        Spolecne.Double3 x = spolecne.abcua(ua, al, e);
        spolecne.vysacb(x.a, x.b, x.c);
    }

    private void Vyp186(double e, double ta, double p, boolean znak) {
        Spolecne.Double3 x = spolecne.abctap(ta, p, e);
        spolecne.vyslacb(znak, x.a, x.b, x.c);
    }

    public void P186(double ta, double ub, double p, boolean znak, String osa) {
        Spolecne.Double2M n = spolecne.min(spolecne.funkce1.f186, ta, p, PI / 2, 0.02, 100000);
        if (Math.abs(ub - n.h) < PR) {
            Vyp186(n.u, ta, p, znak);
            return;
        }
        if (ub < n.h) {
            spolecne.chyba(osa, "<", n.h);
            return;
        }
        Vyp186(spolecne.hodnota(spolecne.funkce1.f186, ta, p, ub, n.u, 0.0), ta, p, znak);
        Vyp186(spolecne.hodnota(spolecne.funkce1.f186, ta, p, ub, n.u, PI), ta, p, znak);
    }

    private void Vyp187(double e, double ta, double ua) {
        Spolecne.Double3 x = spolecne.abcua(ua, spolecne.altaua(ta, ua, e), e);
        spolecne.vysacb(x.a, x.b, x.c);
    }

    public void P187(double ta, double ua, double r) {
        if (ua > ta) {
            spolecne.chyba("ua", ">", ta);
            return;
        }
        if (ua == ta) {
            spolecne.malux(compLib.acos(ta / r - 1), ta);
            return;
        }
        Spolecne.Double2M n = spolecne.min(spolecne.funkce1.f187, ta, ua, 0.02, 0.02, 100000);
        if (Math.abs(r - n.h) < PR) {
            Vyp187(n.u, ta, ua);
            return;
        }
        if (r < n.h) {
            spolecne.chyba("r", "<", n.h);
            return;
        }
        Vyp187(spolecne.hodnota(spolecne.funkce1.f187, ta, ua, r, n.u, PI / 2), ta, ua);
        Vyp187(spolecne.hodnota(spolecne.funkce1.f187, ta, ua, r, n.u, 0.0), ta, ua);
    }

    private void Vyp188(double e, double ta, double r, boolean znak, boolean tp) {
        double al;
        if (ta == r) al = PI / 2; else if (tp == true) al = compLib.acos(ta / r * Math.sin(e) - Math.sqrt(1 - (ta / r * Math.cos(e)) * (ta / r * Math.cos(e)))); else al = compLib.acos(ta / r * Math.sin(e) + Math.sqrt(1 - (ta / r * Math.cos(e)) * (ta / r * Math.cos(e))));
        Spolecne.Double3 x = spolecne.abctar(ta, r, al, e);
        spolecne.vyslacb(znak, x.a, x.b, x.c);
    }

    private void Vyp188a(double al, double ta, double r, boolean znak, boolean tp) {
        double e = compLib.asin((ta * ta - (r * Math.sin(al)) * (r * Math.sin(al))) / 2 / ta / r / Math.cos(al));
        if (tp == true) e = PI - e;
        Spolecne.Double3 x = spolecne.abctar(ta, r, al, e);
        spolecne.vyslacb(znak, x.a, x.b, x.c);
    }

    public void P188(double ta, double ub, double r, boolean znak, String osa) {
        double ubn, krok, aln, em;
        if (ta >= 2 * r) {
            spolecne.chyba("ta", ">=", 2 * r);
            return;
        }
        em = compLib.asin(ta / 2 / r);
        aln = compLib.acos(ta / r - 1);
        Spolecne.Double3 x = spolecne.abctar(ta, r, aln, PI / 2);
        ubn = spolecne.osauhlu(x.a, x.c, x.b);
        if (ta == r) {
            if (ub >= 2 * r) {
                spolecne.chyba(osa, ">=", 2 * r);
                return;
            }
            Vyp188(spolecne.hodnota1(spolecne.funkce1.f188, ta, r, ub, PI, 0.0, false), ta, r, znak, false);
            return;
        }
        if (ta > r) {
            if (ta > 1.82 * r) krok = PR; else krok = 0.02;
            Spolecne.Double2M m = spolecne.max1(spolecne.funkce1.f188a, ta, r, 0.02, krok, 0.0, false);
            if (Math.abs(ub - m.h) < PR) {
                Vyp188a(m.u, ta, r, znak, false);
                return;
            }
            if (ub > m.h) {
                spolecne.chyba(osa, ">", m.h);
                return;
            }
            Vyp188a(spolecne.hodnota1(spolecne.funkce1.f188a, ta, r, ub, 0.0, m.u, false), ta, r, znak, false);
            if (Math.abs(ub - ubn) < PR) {
                spolecne.malux(aln, ta);
                return;
            }
            if (ub > ubn) {
                Vyp188a(spolecne.hodnota1(spolecne.funkce1.f188a, ta, r, ub, aln, m.u, false), ta, r, znak, false);
                return;
            }
            Spolecne.Double2M k = spolecne.max3(spolecne.funkce1.f188a, ta, r, 0.02, 0.02, 0.0, aln, true);
            if (Math.abs(k.u - aln) < PR) {
                Vyp188a(spolecne.hodnota1(spolecne.funkce1.f188a, ta, r, ub, 0.0, aln, true), ta, r, znak, true);
                return;
            } else {
                Spolecne.Double2M n = spolecne.min1(spolecne.funkce1.f188a, ta, r, k.u, 0.02, k.h + 1, true);
                if (Math.abs(ub - k.h) < PR) Vyp188a(k.u, ta, r, znak, true);
                if (ub < k.h - PR) {
                    Vyp188(spolecne.hodnota1(spolecne.funkce1.f188a, ta, r, ub, 0.0, k.u, true), ta, r, znak, true);
                    if (Math.abs(ub - n.h) < PR) {
                        Vyp188a(n.u, ta, r, znak, true);
                        return;
                    }
                    if (ub < n.h) return;
                    Vyp188(spolecne.hodnota1(spolecne.funkce1.f188a, ta, r, ub, n.u, k.u, true), ta, r, znak, true);
                }
                Vyp188(spolecne.hodnota1(spolecne.funkce1.f188a, ta, r, ub, n.u, aln, true), ta, r, znak, true);
            }
        } else {
            Spolecne.Double2M l = spolecne.max1(spolecne.funkce1.f188, ta, r, 0.02, 0.02, 0.0, true);
            if (Math.abs(ub - l.h) < PR) {
                Vyp188(l.u, ta, r, znak, true);
                return;
            }
            if (ub > l.h) {
                spolecne.chyba(osa, ">", l.h);
                return;
            }
            Vyp188(spolecne.hodnota1(spolecne.funkce1.f188, ta, r, ub, PI, l.u, true), ta, r, znak, true);
            if (Math.abs(ub - 2 * ta) < PR) return;
            if (ub > 2 * ta) {
                Vyp188(spolecne.hodnota1(spolecne.funkce1.f188, ta, r, ub, 0.0, l.u, true), ta, r, znak, true);
                return;
            }
            Vyp188(spolecne.hodnota1(spolecne.funkce1.f188, ta, r, ub, em, 0.0, false), ta, r, znak, false);
            Spolecne.Double2M m2 = spolecne.max1(spolecne.funkce1.f188, ta, r, PI - 0.01, -0.01, 0, false);
            if (Math.abs(ub - m2.h) < PR) {
                Vyp188(m2.u, ta, r, znak, false);
                return;
            }
            if (ub > m2.h) return;
            Vyp188(spolecne.hodnota1(spolecne.funkce1.f188, ta, r, ub, PI, m2.u, false), ta, r, znak, false);
            Vyp188(spolecne.hodnota1(spolecne.funkce1.f188, ta, r, ub, PI - l.u, m2.u, false), ta, r, znak, false);
        }
    }

    public void P189(double ta, double ua, double ro) {
        if (ta < ua) {
            spolecne.chyba("ta", "<", ua);
            return;
        }
        if (ro >= ua / 2) {
            spolecne.chyba("ro", ">=", ua / 2);
            return;
        }
        if (ta == ua) {
            spolecne.malux(2 * compLib.asin(ro / (ua - ro)), ta);
            return;
        }
        double em = compLib.acos(2 * ro / ua);
        double e = spolecne.hodnota(spolecne.funkce1.f189, ta, ua, ro, em, 0.0);
        Spolecne.Double3 x = spolecne.abcua(ua, spolecne.altaua(ta, ua, e), e);
        spolecne.vysacb(x.a, x.b, x.c);
    }

    private void Vyp190(double e, double ub, double ro, boolean znak) {
        Spolecne.Double3 x = spolecne.abcrova(ub * Math.cos(e), ro, e);
        spolecne.vyslacb(znak, x.b, x.a, x.c);
    }

    public void P190(double ta, double ub, double ro, boolean znak) {
        if (ro >= ub / 2) {
            spolecne.chyba("ro", ">=", ub / 2);
            return;
        }
        double emm = compLib.acos(2 * ro / ub);
        Spolecne.Double2M n = spolecne.min(spolecne.funkce1.f190, ub, ro, 0.0, 0.02, 10000);
        if (Math.abs(ta - n.h) < PR) {
            Vyp190(n.u, ub, ro, znak);
            return;
        }
        if (ta < n.h) {
            spolecne.chyba("ta", "<", n.h);
            return;
        }
        Vyp190(spolecne.hodnota(spolecne.funkce1.f190, ub, ro, ta, n.u, -emm), ub, ro, znak);
        Vyp190(spolecne.hodnota(spolecne.funkce1.f190, ub, ro, ta, n.u, emm), ub, ro, znak);
    }

    public void P191(double ua, double ub, double p) {
        double ubb, e, ep, ek;
        ep = -PI / 2 + PR;
        ek = PI / 2;
        do {
            e = (ep + ek) / 2;
            Spolecne.Double3 x = spolecne.abcua(ua, spolecne.alpua(ua, p, e), e);
            ubb = spolecne.osauhlu(x.a, x.c, x.b);
            if (ubb < ub) ep = e; else ek = e;
        } while (Math.abs(ub - ubb) > 1.0E-10);
        Spolecne.Double3 x = spolecne.abcua(ua, spolecne.alpua(ua, p, e), e);
        spolecne.vysledek(x.a, x.b, x.c);
    }

    private void Vyp192(double al, double ua, double r, boolean tp) {
        double e = compLib.acos(ua / 4 / r + Math.sqrt((ua / 4 / r) * (ua / 4 / r) + (Math.sin(al / 2)) * (Math.sin(al / 2))));
        if (tp == true) e = -e;
        Spolecne.Double3 x = spolecne.abcua(ua, al, e);
        spolecne.vysledek(x.a, x.b, x.c);
    }

    public void P192(double ua, double ub, double r) {
        if (ua >= 2 * r) {
            spolecne.chyba("ua", ">=", 2 * r);
        }
        double aln = compLib.acos(ua / r - 1);
        double ubn = spolecne.osauhlu(2 * ua * Math.tan(aln / 2), ua / Math.cos(aln / 2), ua / Math.cos(aln / 2));
        Spolecne.Double2M m = spolecne.max1(spolecne.funkce1.f192, ua, r, 0.02, 0.02, 0.0, false);
        if (Math.abs(ub - m.h) < PR) {
            Vyp192(m.u, ua, r, false);
            return;
        }
        if (ub > m.h) {
            spolecne.chyba("ub", ">", m.h);
            return;
        }
        Vyp192(spolecne.hodnota1(spolecne.funkce1.f192, ua, r, ub, 0.0, m.u, false), ua, r, false);
        if (Math.abs(ub - ubn) < PR) {
            spolecne.malux(aln, ua);
            return;
        }
        if (ub > ubn) Vyp192(spolecne.hodnota1(spolecne.funkce1.f192, ua, r, ub, aln, m.u, false), ua, r, false); else Vyp192(spolecne.hodnota1(spolecne.funkce1.f192, ua, r, ub, 0.0, aln, true), ua, r, true);
    }

    private void Vyp193(double e, double ua, double ro) {
        Spolecne.Double3 x = spolecne.abcua(ua, 2 * compLib.asin(ro * Math.cos(e) / (ua * Math.cos(e) - ro)), e);
        spolecne.vysledek(x.a, x.b, x.c);
    }

    public void P193(double ua, double ub, double ro) {
        double m, ubk, alk, ekk;
        if (ro >= ua / 2) {
            spolecne.chyba("ro", ">=", ua / 2);
            return;
        }
        ekk = compLib.acos(2 * ro / ua);
        m = ua - ro / Math.cos(ekk);
        alk = compLib.asin(ro / m);
        ubk = 2 * m * Math.tan(alk);
        Spolecne.Double2M n = spolecne.min(spolecne.funkce1.f193, ua, ro, -ekk + 0.02, 0.02, 100000);
        if (Math.abs(ub - n.h) < PR) {
            Vyp193(n.u, ua, ro);
            return;
        }
        if (ub < n.h) {
            spolecne.chyba("ub", "<", n.h);
            return;
        }
        Vyp193(spolecne.hodnota(spolecne.funkce1.f193, ua, ro, ub, n.u, ekk), ua, ro);
        if (ub >= ubk - PR) return;
        Vyp193(spolecne.hodnota(spolecne.funkce1.f193, ua, ro, ub, n.u, -ekk), ua, ro);
    }

    public void P194(double ua, double ub, double uc) {
        double ubb, ucc, al, alp, alk, e, ep, ek;
        Spolecne.Double3 x;
        if (ub == uc) {
            alp = 0.0;
            alk = PI - PR;
            do {
                al = (alp + alk) / 2;
                ubb = spolecne.osauhlu(2 * ua * Math.tan(al / 2), ua / Math.cos(al / 2), ua / Math.cos(al / 2));
                if (ubb < ub) alp = al; else alk = al;
            } while (Math.abs(ub - ubb) > 1.0E-10);
            spolecne.malux(al, ua);
            return;
        } else if (ub < uc) {
            ep = -PI / 2;
            ek = 0.0;
            do {
                e = (ep + ek) / 2;
                al = spolecne.hodnota1(spolecne.funkce1.f194, ua, e, uc, 0.0, PI - 2 * Math.abs(e), true);
                x = spolecne.abcua(ua, al, e);
                ubb = spolecne.osauhlu(x.a, x.c, x.b);
                if (ubb < ub) ep = e; else ek = e;
            } while (Math.abs(ub - ubb) > 1.0E-10);
        } else {
            ep = PI / 2;
            ek = 0.0;
            do {
                e = (ep + ek) / 2;
                al = spolecne.hodnota1(spolecne.funkce1.f194, ua, e, ub, 0.0, PI - 2 * Math.abs(e), false);
                x = spolecne.abcua(ua, al, e);
                ucc = spolecne.osauhlu(x.a, x.b, x.c);
                if (ucc < uc) ep = e; else ek = e;
            } while (Math.abs(uc - ucc) > 1.0E-10);
        }
        x = spolecne.abcua(ua, al, e);
        spolecne.vysledek(x.a, x.b, x.c);
    }

    private void Vyp195(double al, double p, double r) {
        Spolecne.Double3 x = spolecne.abcrval(r, p / r / Math.sin(al), al);
        spolecne.vysacb(x.a, x.b, x.c);
    }

    public void P195(double ua, double p, double r) {
        Spolecne.Double2M me, mi, n;
        double pm = 1.299038 * r * r;
        if (Math.abs(p - pm) < PR) {
            double uaa = r * (1 + Math.cos(PI / 3));
            if (Math.abs(ua - uaa) > PR) {
                spolecne.chyba("ua", "!=", uaa);
                return;
            }
            spolecne.malux(PI / 3, 1.5 * r);
            return;
        }
        if (p > pm) {
            spolecne.chyba("p", ">", pm);
            return;
        }
        me = spolecne.mez1(p, r, 0.0001, 0.02);
        if (Math.abs(ua - me.h) < PR) {
            spolecne.malux(me.u, ua);
            return;
        }
        if (ua > me.h) {
            spolecne.chyba("ua", ">", me.h);
            return;
        }
        mi = spolecne.mez1(p, r, PI - 0.01, -0.02);
        n = spolecne.min2(spolecne.funkce1.f195, p, r, me.u + 0.02, 0.02, 100000, mi.u);
        if (Math.abs(n.u - mi.u) < PR) {
            if (ua < mi.h) {
                spolecne.chyba("ua", "<", mi.h);
                return;
            }
            Vyp195(spolecne.hodnota(spolecne.funkce1.f195, p, r, ua, mi.u, me.u), p, r);
            return;
        }
        if (Math.abs(ua - n.h) < PR) {
            Vyp195(n.u, p, r);
            return;
        }
        if (ua < n.h) {
            spolecne.chyba("ua", "<", n.u);
            return;
        }
        Vyp195(spolecne.hodnota(spolecne.funkce1.f195, p, r, ua, n.u, me.u), p, r);
        if (Math.abs(ua - mi.h) < PR) {
            spolecne.malux(mi.u, ua);
            return;
        }
        if (ua > mi.h) return;
        Vyp195(spolecne.hodnota(spolecne.funkce1.f195, p, r, ua, n.u, mi.u), p, r);
    }

    public void P196(double ua, double p, double ro) {
        if (ro >= ua / 2) {
            spolecne.chyba("ro", ">=", ua / 2);
            return;
        }
        double pm = ro * ua / Math.sqrt(1 - 2 * ro / ua);
        if (Math.abs(p - pm) < PR) {
            spolecne.mala(2 * p / ua, ua);
            return;
        }
        if (p < pm) {
            spolecne.chyba("p", "<", pm);
            return;
        }
        double em = compLib.acos(2 * ro / ua);
        double e = spolecne.hodnota(spolecne.funkce1.f196, ua, ro, p, 0.0, em);
        Spolecne.Double3 x = spolecne.abcua(ua, 2 * compLib.asin(ro * Math.cos(e) / (ua * Math.cos(e) - ro)), e);
        spolecne.vysacb(x.a, x.b, x.c);
    }

    public void P197(double ua, double r, double ro) {
        double rom, al, alm;
        if (ua >= 2 * r) {
            spolecne.chyba("ua", ">=", 2 * r);
            return;
        }
        if (Math.abs(r / 2 - ro) < PR) {
            if (ua != 1.5 * r) {
                spolecne.chyba("ua", "!=", 1.5 * r);
                return;
            }
            spolecne.malux(PI / 3, ua);
            return;
        }
        alm = compLib.acos(ua / r - 1);
        rom = ua / (1 + 1 / Math.sin(alm / 2));
        if (Math.abs(ro - rom) < PR) {
            spolecne.malux(alm, ua);
            return;
        }
        if (ro > rom) {
            spolecne.chyba("ro", ">", rom);
            return;
        }
        al = spolecne.hodnota(spolecne.funkce1.f197, ua, r, ro, 0.0, alm);
        Spolecne.Double3 x = spolecne.abcua(ua, al, compLib.acos(ua / 4 / r + Math.sqrt((ua / 4 / r) * (ua / 4 / r) + (Math.sin(al / 2)) * (Math.sin(al / 2)))));
        spolecne.vysacb(x.a, x.b, x.c);
    }
}
