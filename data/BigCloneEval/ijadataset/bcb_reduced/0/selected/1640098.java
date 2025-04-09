package trojuhelnik;

public class Vypocty2 {

    public final double PR = 0.00001;

    public final double PI = Math.PI;

    private Spolecne spolecne;

    private CompLibInt compLib;

    /** Creates a new instance of Vypocty2 */
    public Vypocty2(Spolecne spolecne) {
        this.spolecne = spolecne;
        this.compLib = spolecne.compLib;
    }

    public void P060(double a, double b, double ta, boolean znak, String tez) {
        if (ta <= Math.abs(a / 2 - b)) {
            spolecne.chyba(tez, "<=", Math.abs(a / 2 - b));
            return;
        }
        if (ta >= a / 2 + b) {
            spolecne.chyba(tez, ">=", a / 2 + b);
            return;
        }
        spolecne.vyslbac(znak, a, b, spolecne.strtx(a, b, ta));
    }

    public void P061(double a, double b, double tc) {
        if (tc <= Math.abs(a / 2 - b / 2)) {
            spolecne.chyba("tc", "<=", Math.abs(a / 2 - b / 2));
            return;
        }
        if (tc >= (a / 2 + b / 2)) {
            spolecne.chyba("tc", ">=", a / 2 + b / 2);
            return;
        }
        spolecne.vysledek(a, b, Math.sqrt(2 * (a * a + b * b - 2 * tc * tc)));
    }

    public void P062(double a, double al, double ta) {
        double b, c, tam;
        tam = a / 2 / Math.tan(al / 2);
        if (Math.abs(al - PI / 2) < PR / 10) {
            spolecne.hlaseni("alfa_90");
            return;
        }
        if (Math.abs(ta - tam) < PR) {
            spolecne.mala(a, ta);
            return;
        }
        if (al > PI / 2) {
            if (ta >= a / 2) {
                spolecne.chyba("ta", ">=", a / 2);
                return;
            }
            if (ta < tam) {
                spolecne.chyba("ta", "<", tam);
                return;
            }
        } else {
            if (ta <= a / 2) {
                spolecne.chyba("ta", "<=", a / 2);
                return;
            }
            if (ta > tam) {
                spolecne.chyba("ta", ">", tam);
                return;
            }
        }
        c = Math.sqrt(ta * ta + a * a / 4 + Math.sqrt((ta * ta + a * a / 4) * (ta * ta + a * a / 4))) - ((ta * ta - a * a / 4) / Math.cos(al)) * ((ta * ta - a * a / 4) / Math.cos(al));
        b = spolecne.strtx(a, c, ta);
        spolecne.vysacb(a, b, c);
    }

    public void P063(double a, double al, double tb, boolean znak, String tez) {
        double q, r1, cfi, psi, e;
        q = a / 4 * Math.sqrt(9 + (1 / Math.tan(al)) * (1 / Math.tan(al)));
        r1 = a / 4 / Math.sin(al);
        psi = compLib.atan(1 / Math.tan(al) / 3);
        cfi = (q * q + tb * tb - r1 * r1) / 2 / q / tb;
        if ((al > PI / 2 - PR)) {
            if (tb <= a / 2) {
                spolecne.chyba(tez, "<=", a / 2);
                return;
            }
            if (tb >= a) {
                spolecne.chyba(tez, ">=", a);
                return;
            } else e = psi + compLib.acos(cfi);
        } else if ((Math.abs(tb - q + r1) < PR) || (Math.abs(tb - q - r1) < PR)) e = psi; else if (tb < (q - r1)) {
            spolecne.chyba(tez, "<", q - r1);
            return;
        } else if (tb > (q + r1)) {
            spolecne.chyba(tez, ">", q + r1);
            return;
        } else e = psi + compLib.acos(cfi);
        Spolecne.Double2 x = spolecne.bctb(a, tb, e);
        spolecne.vyslacb(znak, a, x.b, x.c);
        if (Math.abs(e - psi) < PR) return;
        e = psi - compLib.acos(cfi);
        if (e <= PR) return;
        x = spolecne.bctb(a, tb, e);
        spolecne.vyslacb(znak, a, x.b, x.c);
    }

    public void P064(double a, double be, double ta, boolean znak) {
        double c, tam;
        tam = a / 2 * Math.sin(be);
        if (be > PI / 2 - PR) {
            if (ta <= a / 2) {
                spolecne.chyba("ta", "<=", a / 2);
                return;
            }
            c = a / 2 * Math.cos(be) + Math.sqrt(ta * ta - (a / 2 * Math.sin(be)) * (a / 2 * Math.sin(be)));
            spolecne.vyslacb(znak, a, spolecne.cs(a, c, be), c);
            return;
        }
        if (Math.abs(ta - tam) < PR) {
            c = a * Math.cos(be) / 2;
            spolecne.vyslacb(znak, a, spolecne.cs(a, c, be), c);
            return;
        }
        if (ta < tam) {
            spolecne.chyba("ta", "<", tam);
            return;
        }
        c = a / 2 * Math.cos(be) + Math.sqrt(ta * ta - (a / 2 * Math.sin(be)) * (a / 2 * Math.sin(be)));
        spolecne.vyslacb(znak, a, spolecne.cs(a, c, be), c);
        if (ta >= a / 2) return;
        c = a / 2 * Math.cos(be) - Math.sqrt(ta * ta - (a / 2 * Math.sin(be)) * (a / 2 * Math.sin(be)));
        spolecne.vyslacb(znak, a, spolecne.cs(a, c, be), c);
    }

    public void P065(double a, double be, double tb, boolean znak, String tez) {
        double c, tbm;
        tbm = a / 2 * Math.sin(be);
        if (be < PI / 2 + PR) {
            if (tb <= a / 2) {
                spolecne.chyba(tez, "<=", a / 2);
                return;
            }
            c = -a * Math.cos(be) + Math.sqrt(4 * tb * tb - (a * Math.sin(be)) * (a * Math.sin(be)));
            spolecne.vyslacb(znak, a, spolecne.cs(a, c, be), c);
            return;
        }
        if (Math.abs(tb - tbm) < PR) {
            c = -a * Math.cos(be);
            spolecne.vyslacb(znak, a, spolecne.cs(a, c, be), c);
            return;
        }
        if (tb < tbm) {
            spolecne.chyba(tez, "<", tbm);
            return;
        }
        c = -a * Math.cos(be) + Math.sqrt(4 * tb * tb - (a * Math.sin(be)) * (a * Math.sin(be)));
        spolecne.vyslacb(znak, a, spolecne.cs(a, c, be), c);
        if (tb >= a / 2) return;
        c = -a * Math.cos(be) - Math.sqrt(4 * tb * tb - (a * Math.sin(be)) * (a * Math.sin(be)));
        spolecne.vyslacb(znak, a, spolecne.cs(a, c, be), c);
        return;
    }

    public void P066(double a, double be, double tc, boolean znak, String tez) {
        double c, tcm;
        tcm = a * Math.sin(be);
        if (be > PI / 2 - PR) {
            if (tc <= a) {
                spolecne.chyba(tez, "<=", a);
                return;
            }
            c = 2 * (a * Math.cos(be) + Math.sqrt(tc * tc - (a * Math.sin(be)) * (a * Math.sin(be))));
            spolecne.vyslacb(znak, a, spolecne.cs(a, c, be), c);
            return;
        }
        if (Math.abs(tc - tcm) < PR) {
            c = 2 * a * Math.cos(be);
            spolecne.vyslacb(znak, a, spolecne.cs(a, c, be), c);
            return;
        }
        if (tc < tcm) {
            spolecne.chyba(tez, "<", tcm);
            return;
        }
        c = 2 * (a * Math.cos(be) + Math.sqrt(tc * tc - (a * Math.sin(be) * (a * Math.sin(be)))));
        spolecne.vyslacb(znak, a, spolecne.cs(a, c, be), c);
        if (tc >= a) return;
        c = 2 * (a * Math.cos(be) - Math.sqrt(tc * tc - (a * Math.sin(be)) * (a * Math.sin(be))));
        spolecne.vyslacb(znak, a, spolecne.cs(a, c, be), c);
    }

    public void P067(double a, double va, double ta) {
        if (ta == va) {
            spolecne.mala(a, ta);
            return;
        }
        if (ta < va) {
            spolecne.chyba("ta", "<", va);
            return;
        }
        double be = compLib.atan(va / (a / 2 + Math.sqrt(ta * ta - va * va)));
        spolecne.vysacb(a, spolecne.cs(a, va / Math.sin(be), be), va / Math.sin(be));
    }

    public void P068(double a, double va, double tb, boolean znak) {
        double ga;
        if (va > 2 * tb) {
            spolecne.chyba("va", ">", 2 * tb);
            return;
        }
        if (Math.abs(tb - Math.sqrt(a * a + va * va / 4)) < PR / 10) ga = PI / 2; else ga = compLib.atan(va / (2 * a - Math.sqrt(4 * tb * tb - va * va)));
        if (ga < 0) ga = PI + ga;
        spolecne.vyslacb(znak, a, va / Math.sin(ga), spolecne.cs(a, va / Math.sin(ga), ga));
        if (tb == va / 2) return;
        ga = compLib.atan(va / (2 * a + Math.sqrt(4 * tb * tb - va * va)));
        if (ga < 0) ga = PI + ga;
        spolecne.vyslacb(znak, a, va / Math.sin(ga), spolecne.cs(a, va / Math.sin(ga), ga));
    }

    public void P069(double a, double vb, double ta, boolean znak) {
        if (vb == a && vb == 2 * ta) {
            spolecne.chyba(" a & 2*ta", "=", vb);
            return;
        }
        if (a < vb) {
            spolecne.chyba("a", "<", vb);
            return;
        }
        double ga = compLib.asin(vb / a);
        if (ta < vb / 2) {
            spolecne.chyba("ta", "<", vb / 2);
            return;
        }
        double b = a / 2 * Math.cos(ga) + Math.sqrt(ta * ta - vb * vb / 4);
        spolecne.vyslacb(znak, a, b, spolecne.cs(a, b, ga));
        if (ta <= a / 2 || Math.abs(ta - vb / 2) < PR || vb == a) return;
        b = a / 2 * Math.cos(PI - ga) + Math.sqrt(ta * ta - vb * vb / 4);
        spolecne.vyslacb(znak, a, b, spolecne.cs(a, b, PI - ga));
    }

    public void P070(double a, double vb, double tb, boolean znak, String vyska) {
        if (vb == a && vb == tb) {
            spolecne.chyba(" a & " + vyska, " = ", tb);
            return;
        }
        if (vb > a) {
            spolecne.chyba(vyska, ">", a);
            return;
        }
        if (vb > tb) {
            spolecne.chyba(vyska, ">", tb);
            return;
        }
        double ga = compLib.asin(vb / a);
        double b = 2 * (a * Math.cos(ga) + Math.sqrt(tb * tb - vb * vb));
        spolecne.vyslacb(znak, a, b, spolecne.cs(a, b, ga));
        if (vb == a || vb == tb || tb == a) return;
        if (tb < a) {
            b = 2 * (a * Math.cos(ga) - Math.sqrt(tb * tb - vb * vb));
            spolecne.vyslacb(znak, a, b, spolecne.cs(a, b, ga));
        } else {
            b = 2 * (a * Math.cos(PI - ga) + Math.sqrt(tb * tb - vb * vb));
            spolecne.vyslacb(znak, a, b, spolecne.cs(a, b, PI - ga));
        }
    }

    private void Vyp071(double a, double vb, double tc, double ga, int t, boolean znak) {
        double b;
        if (t == 1) b = Math.abs(Math.sqrt(4 * tc * tc - vb * vb) - Math.sqrt(a * a - vb * vb)); else b = Math.sqrt(4 * tc * tc - vb * vb) + Math.sqrt(a * a - vb * vb);
        spolecne.vyslacb(znak, a, b, spolecne.cs(a, b, ga));
    }

    public void P071(double a, double vb, double tc, boolean znak, String vyska) {
        if (vb == a && tc == a / 2) {
            spolecne.chyba(" a & " + vyska, " = ", 2 * tc);
            return;
        }
        if (vb > a) {
            spolecne.chyba(vyska, ">", a);
            return;
        }
        double ga = compLib.asin(vb / a);
        if (tc == vb / 2) {
            Vyp071(a, vb, tc, PI - ga, 1, znak);
            return;
        }
        if (tc == a / 2) {
            Vyp071(a, vb, tc, PI - ga, 0, znak);
            return;
        }
        if (vb > 2 * tc) {
            spolecne.chyba(vyska, ">", 2 * tc);
            return;
        }
        if (tc < a / 2) {
            Vyp071(a, vb, tc, PI - ga, 1, znak);
            Vyp071(a, vb, tc, PI - ga, 0, znak);
            return;
        }
        Vyp071(a, vb, tc, ga, 1, znak);
        if (vb == a) return;
        Vyp071(a, vb, tc, PI - ga, 0, znak);
    }

    public void P072(double a, double ta, double p) {
        double c, pm, be;
        pm = a * ta / 2;
        if (Math.abs(p - pm) < PR) {
            spolecne.mala(a, ta);
            return;
        }
        if (p > pm) {
            spolecne.chyba("p", ">", pm);
            return;
        }
        be = compLib.atan(2 * p / (a * a / 2 + Math.sqrt(a * a * ta * ta - 4 * p * p)));
        c = a / 2 * Math.cos(be) + Math.sqrt(ta * ta - (a / 2 * Math.sin(be)) * (a / 2 * Math.sin(be)));
        spolecne.vysacb(a, spolecne.cs(a, c, be), c);
    }

    public void P073(double a, double tb, double p, boolean znak) {
        double b, ga;
        if (Math.abs(p - a * tb) < PR) {
            ga = compLib.atan(tb / a);
            b = 2 * p / a / Math.sin(ga);
            spolecne.vyslacb(znak, a, b, spolecne.cs(a, b, ga));
            return;
        }
        if (p > a * tb) {
            spolecne.chyba("p", ">", a * tb);
            return;
        }
        ga = compLib.atan(p / (a * a - Math.sqrt(a * a * tb * tb - p * p)));
        if (ga < 0) ga = PI + ga;
        b = 2 * p / a / Math.sin(ga);
        spolecne.vyslacb(znak, a, b, spolecne.cs(a, b, ga));
        ga = compLib.atan(p / (a * a + Math.sqrt(a * a * tb * tb - p * p)));
        b = 2 * p / a / Math.sin(ga);
        spolecne.vyslacb(znak, a, b, spolecne.cs(a, b, ga));
    }

    public void P074(double a, double ta, double r) {
        double tam, tami, cal, e;
        if (a >= 2 * r) {
            spolecne.chyba("a", ">=", 2 * r);
            return;
        }
        cal = Math.sqrt(r * r - a * a / 4) / r;
        tam = r * (1 + cal);
        tami = r * (1 - cal);
        if (Math.abs(ta - tam) < PR) {
            spolecne.mala(a, ta);
            return;
        }
        if (Math.abs(ta - tami) < PR) {
            spolecne.mala(a, ta);
            return;
        }
        if (ta > tam) {
            spolecne.chyba("ta", ">", tam);
            return;
        }
        if (ta < tami) {
            spolecne.chyba("ta", "<", tami);
            return;
        }
        if (ta == a / 2) {
            spolecne.chyba("ta", "=", a / 2);
            return;
        }
        e = compLib.acos(Math.abs((ta * ta - a * a / 4) / ta / Math.sqrt(4 * r * r - a * a)));
        Spolecne.Double2 x = spolecne.bcta(a, ta, PI / 2 - e);
        spolecne.vysacb(a, x.b, x.c);
    }

    private void Vyp075(double a, double tb, double e, boolean znak) {
        Spolecne.Double2 x = spolecne.bctb(a, tb, e);
        spolecne.vyslacb(znak, a, x.b, x.c);
    }

    public void P075(double a, double tb, double r, boolean znak, String tez) {
        double q, al, fi, psi, e;
        if (a > 2 * r) {
            spolecne.chyba("a", ">", 2 * r);
            return;
        }
        al = compLib.asin(a / 2 / r);
        q = a / 4 * Math.sqrt(9 + (1 / Math.tan(al)) * (1 / Math.tan(al)));
        psi = compLib.atan(1 / Math.tan(al) / 3);
        fi = compLib.acos((q * q + tb * tb - r / 2 * r / 2) / 2 / q / tb);
        e = psi + fi;
        if (a == 2 * r) {
            if (tb <= a / 2) {
                spolecne.chyba(tez, "<=", a / 2);
                return;
            }
            if (tb >= a) {
                spolecne.chyba(tez, ">=", a);
                return;
            }
            Vyp075(a, tb, e, znak);
            return;
        }
        if ((Math.abs(tb - q + r / 2) < PR) || (Math.abs(tb - q - r / 2) < PR)) {
            Vyp075(a, tb, psi, znak);
            return;
        }
        if (tb < q - r / 2) {
            spolecne.chyba(tez, "<", q - r / 2);
            return;
        }
        if (tb > q + r / 2) {
            spolecne.chyba(tez, ">", q + r / 2);
            return;
        }
        Vyp075(a, tb, e, znak);
        if (Math.abs(e - psi) < PR) return;
        e = psi - fi;
        if (Math.abs(e) < PR) return;
        if (e > 0) Vyp075(a, tb, e, znak); else Vyp075(a, tb, fi - psi, znak);
    }

    public void P076(double a, double ta, double ro) {
        double rom = a / 4 / ta * (Math.sqrt(a * a + 4 * ta * ta) - a);
        if (Math.abs(ro - rom) < PR) {
            spolecne.mala(a, ta);
            return;
        }
        if (ro > rom) {
            spolecne.chyba("ro", ">", rom);
            return;
        }
        double e = spolecne.hodnota(spolecne.funkce1.f076, a, ta, ro, 0.0, PI / 2);
        Spolecne.Double2 x = spolecne.bcta(a, ta, e);
        spolecne.vysacb(a, x.b, x.c);
    }

    private void Vyp077(double e, double a, double tb, boolean znak) {
        Spolecne.Double2 x = spolecne.bctb(a, tb, e);
        spolecne.vyslacb(znak, a, x.b, x.c);
    }

    public void P077(double a, double tb, double ro, boolean znak) {
        Spolecne.Double2M m = spolecne.max(spolecne.funkce1.f077, a, tb, 0.1, 0.2, 0.0);
        if (Math.abs(ro - m.h) < PR) {
            Vyp077(m.u, a, tb, znak);
            return;
        }
        if (ro > m.h) {
            spolecne.chyba("ro", ">", m.h);
            return;
        }
        Vyp077(spolecne.hodnota(spolecne.funkce1.f077, a, tb, ro, 0.0, m.u), a, tb, znak);
        Vyp077(spolecne.hodnota(spolecne.funkce1.f077, a, tb, ro, PI, m.u), a, tb, znak);
    }

    public void P078(double a, double ta, double tb, boolean znak, String tez) {
        if (tb <= Math.abs(0.75 * a - ta / 2)) {
            spolecne.chyba(tez, "<=", Math.abs(0.75 * a - ta / 2));
            return;
        }
        if (tb >= 0.75 * a + ta / 2) {
            spolecne.chyba(tez, ">=", 0.75 * a + ta / 2);
            return;
        }
        double e = PI - compLib.acos((9 * a * a + 4 * ta * ta - 16 * tb * tb) / 12 / a / ta);
        Spolecne.Double2 x = spolecne.bcta(a, ta, e);
        spolecne.vyslacb(znak, a, x.b, x.c);
    }

    public void P079(double a, double tb, double tc) {
        if (tb <= Math.abs(1.5 * a - tc)) {
            spolecne.chyba("tb", "<=", Math.abs(1.5 * a - tc));
            return;
        }
        if (tb >= 1.5 * a + tc) {
            spolecne.chyba("tb", ">=", 1.5 * a + tc);
            return;
        }
        double e = compLib.acos((9 * a * a + 4 * tb * tb - 4 * tc * tc) / 12 / a / tb);
        Spolecne.Double2 x = spolecne.bctb(a, tb, e);
        spolecne.vysledek(a, x.b, x.c);
    }

    public void P080(double al, double be, double ta, String tez) {
        double va, ga;
        ga = PI - al - be;
        if (al + be >= PI) {
            spolecne.chyba2("alfa_beta", ">=", 180.0);
            return;
        }
        if (tez.equals("ta")) va = 2 * ta / Math.sqrt((1 / Math.tan(be) - 1 / Math.tan(ga)) * (1 / Math.tan(be) - 1 / Math.tan(ga)) + 4); else if (tez.equals("tb")) va = ta / Math.sqrt((1 / Math.tan(be) + 1 / Math.tan(ga) / 2) * (1 / Math.tan(be) + 1 / Math.tan(ga) / 2) + 0.25); else va = ta / Math.sqrt((1 / Math.tan(be) / 2 + 1 / Math.tan(ga)) * (1 / Math.tan(be) / 2 + 1 / Math.tan(ga)) + 0.25);
        Spolecne.Double3 x = spolecne.vabega(va, be, ga);
        spolecne.vysledek(x.a, x.b, x.c);
    }

    public void P082(double al, double va, double ta) {
        if (ta < va) {
            spolecne.chyba("ta", "<", va);
            return;
        }
        if (ta == va) {
            spolecne.malux(al, va);
            return;
        }
        Spolecne.Double3 x = spolecne.altae(al, ta, compLib.asin(va / ta));
        spolecne.vysacb(x.a, x.b, x.c);
    }

    private void Vyp083(double va, double e, double ga, boolean znak, boolean tp) {
        if (tp == true) e = -e;
        Spolecne.Double3 x = spolecne.vaega(va, e, ga);
        spolecne.vyslacb(znak, x.a, x.b, x.c);
    }

    public void P083(double al, double va, double tb, boolean znak) {
        if (va > 2 * tb) {
            spolecne.chyba("va", ">", 2 * tb);
            return;
        }
        double e = compLib.asin(va / 2 / tb);
        Spolecne.Double2M m = spolecne.max1(spolecne.funkce1.f083, va, e, PR, PI - e, 0.0, false);
        if (Math.abs(al - m.h) < PR) {
            Vyp083(va, e, m.u, znak, false);
            return;
        }
        if (al > m.h) {
            spolecne.chyba2("alfa", ">", Math.toDegrees(m.h));
            return;
        }
        double ga = spolecne.hodnota1(spolecne.funkce1.f083, va, e, al, 0.0, m.u, false);
        Vyp083(va, e, ga, znak, false);
        ga = spolecne.hodnota1(spolecne.funkce1.f083, va, e, al, PI - e, m.u, false);
        Vyp083(va, e, ga, znak, false);
        if (tb == va / 2) return;
        Spolecne.Double2M m1 = spolecne.max1(spolecne.funkce1.f083, va, e, PR, e, 0.0, true);
        if (Math.abs(al - m1.h) < PR) {
            Vyp083(va, e, m1.u, znak, true);
            return;
        }
        if (al > m1.h) return;
        ga = spolecne.hodnota1(spolecne.funkce1.f083, va, e, al, 0.0, m1.u, true);
        Vyp083(va, e, ga, znak, true);
        ga = spolecne.hodnota1(spolecne.funkce1.f083, va, e, al, e, m1.u, true);
        Vyp083(va, e, ga, znak, true);
    }

    public void P084(double al, double vb, double ta, boolean znak) {
        double a, b, c, e;
        if (al <= PI / 2 && ta <= vb / 2 / Math.sin(al)) {
            spolecne.chyba("ta", "<=", vb / 2 / Math.sin(al));
            return;
        }
        if (ta < vb / 2) {
            spolecne.chyba("ta", "<", vb / 2);
            return;
        }
        e = compLib.asin(vb / 2 / ta);
        c = 2 * ta * Math.sin(e) / Math.sin(al);
        b = 2 * ta * Math.cos(e) - c * Math.cos(al);
        a = spolecne.cs(b, c, al);
        spolecne.vyslacb(znak, a, b, c);
        if (ta == vb / 2 || ta >= c / 2 || al <= PI / 2) return;
        b = -2 * ta * Math.cos(e) - c * Math.cos(al);
        a = spolecne.cs(b, c, al);
        spolecne.vyslacb(znak, a, b, c);
    }

    public void P085(double al, double vb, double tb, boolean znak, String tez) {
        double a, b, c, k;
        c = vb / Math.sin(al);
        if (al >= PI / 2 && tb <= c) {
            spolecne.chyba(tez, "<=", c);
            return;
        }
        if (tb < vb) {
            spolecne.chyba(tez, "<", vb);
            return;
        }
        k = Math.sqrt(tb * tb - vb * vb);
        b = 2 * (c * Math.cos(al) + k);
        a = spolecne.cs(b, c, al);
        spolecne.vyslacb(znak, a, b, c);
        if (tb == vb || tb >= c) return;
        b = 2 * (c * Math.cos(al) - k);
        a = spolecne.cs(b, c, al);
        spolecne.vyslacb(znak, a, b, c);
    }

    public void P086(double al, double vb, double tc, boolean znak, String tez) {
        double a, b, c, k;
        c = vb / Math.sin(al);
        if (al >= PI / 2 && tc <= c / 2) {
            spolecne.chyba(tez, "<=", c / 2);
            return;
        }
        if (tc < vb / 2) {
            spolecne.chyba(tez, "<", vb / 2);
            return;
        }
        k = Math.sqrt(tc * tc - vb * vb / 4);
        b = c / 2 * Math.cos(al) + k;
        a = spolecne.cs(b, c, al);
        spolecne.vyslacb(znak, a, b, c);
        if (tc == vb / 2 || tc >= c / 2) return;
        b = c / 2 * Math.cos(al) - k;
        a = spolecne.cs(b, c, al);
        spolecne.vyslacb(znak, a, b, c);
    }

    public void P087(double al, double ta, double p) {
        double a, b, c, pm;
        pm = ta * ta * Math.tan(al / 2);
        if (Math.abs(p - pm) < PR) {
            spolecne.malux(al, ta);
            return;
        }
        if (p > pm) {
            spolecne.chyba("p", ">", pm);
            return;
        }
        b = Math.sqrt(2 * (ta * ta - p / Math.tan(al) - Math.sqrt(ta * ta * ta * ta - 2 * p * ta * ta / Math.tan(al) - p * p)));
        c = 2 * p / b / Math.sin(al);
        a = spolecne.cs(b, c, al);
        spolecne.vysacb(a, b, c);
    }

    private void Vyp088(double al, double c, double p, boolean znak) {
        double a, b;
        b = 2 * p / c / Math.sin(al);
        a = spolecne.cs(b, c, al);
        spolecne.vyslacb(znak, a, b, c);
    }

    public void P088(double al, double tb, double p, boolean znak) {
        double c, pm;
        pm = tb * tb / 2 / Math.tan(al / 2);
        if (Math.abs(p - pm) < PR) {
            c = Math.sqrt(tb * tb / 2 + p / Math.tan(al));
            Vyp088(al, c, p, znak);
            return;
        }
        if (p > pm) {
            spolecne.chyba("p", ">", pm);
            return;
        }
        c = Math.sqrt(tb * tb / 2 + p / Math.tan(al) + Math.sqrt(tb * tb * tb * tb / 4 + p * tb * tb / Math.tan(al) - p * p));
        Vyp088(al, c, p, znak);
        c = Math.sqrt(tb * tb / 2 + p / Math.tan(al) - Math.sqrt(tb * tb * tb * tb / 4 + p * tb * tb / Math.tan(al) - p * p));
        Vyp088(al, c, p, znak);
    }

    public void P089(double al, double ta, double r) {
        double a, b, c, tam;
        a = 2 * r * Math.sin(al);
        tam = a / 2 / Math.tan(al / 2);
        if (Math.abs(al - PI / 2) < PR) {
            spolecne.hlaseni("alfa_90");
            return;
        }
        if (Math.abs(ta - tam) < PR) {
            spolecne.mala(a, ta);
            return;
        }
        if (al > PI / 2) {
            if (ta >= a / 2 - PR) {
                spolecne.chyba("ta", ">=", a / 2);
                return;
            }
            if (ta < tam) {
                spolecne.chyba("ta", "<", tam);
                return;
            }
        } else {
            if (ta <= a / 2 + PR) {
                spolecne.chyba("ta", "<=", a / 2);
                return;
            }
            if (ta > tam) {
                spolecne.chyba("ta", ">", tam);
                return;
            }
        }
        c = Math.sqrt(ta * ta + a * a / 4 + Math.sqrt((ta * ta + a * a / 4) * (ta * ta + a * a / 4) - ((ta * ta - a * a / 4) / Math.cos(al)) * ((ta * ta - a * a / 4) / Math.cos(al))));
        b = spolecne.strtx(a, c, ta);
        spolecne.vysacb(a, b, c);
    }

    public void P090(double al, double tb, double r, boolean znak, String tez) {
        double a = 2 * r * Math.sin(al);
        P063(a, al, tb, znak, tez);
    }

    public void P091(double al, double ta, double ro) {
        double rom = ta / (1 + 1 / Math.sin(al / 2));
        if (Math.abs(ro - rom) < PR) {
            spolecne.malux(al, ta);
            return;
        }
        if (ro > rom) {
            spolecne.chyba("ro", ">", rom);
            return;
        }
        double be = spolecne.hodnota(spolecne.funkce1.f091, al, ro, ta, PI / 2 - al / 2, 0.0);
        Spolecne.Double3 x = spolecne.abcro(ro, al, be, PI - al - be);
        spolecne.vysacb(x.a, x.b, x.c);
    }

    public void P092(double al, double tb, double ro, boolean znak, String tez) {
        Spolecne.Double3 x;
        Spolecne.Double2M m = spolecne.min(spolecne.funkce1.f092, al, ro, 0.1, 0.02, 10000.0);
        if (Math.abs(tb - m.h) < PR) {
            x = spolecne.abcro(ro, al, m.u, PI - al - m.u);
            spolecne.vyslacb(znak, x.a, x.b, x.c);
            return;
        }
        if (tb < m.h) {
            spolecne.chyba(tez, "<", m.h);
            return;
        }
        double be = spolecne.hodnota(spolecne.funkce1.f092, al, ro, tb, m.u, 0.0);
        x = spolecne.abcro(ro, al, be, PI - al - be);
        spolecne.vyslacb(znak, x.a, x.b, x.c);
        be = spolecne.hodnota(spolecne.funkce1.f092, al, ro, tb, m.u, PI - al);
        x = spolecne.abcro(ro, al, be, PI - al - be);
        spolecne.vyslacb(znak, x.a, x.b, x.c);
    }

    private void Vyp093(double al, double be, double ta, boolean znak) {
        double a, b, c, c1, t1;
        c1 = Math.sin(al + be) / Math.sin(al);
        t1 = spolecne.cs(0.5, c1, be);
        c = ta * c1 / t1;
        b = ta / t1 * Math.sin(be) / Math.sin(al);
        a = spolecne.cs(b, c, al);
        spolecne.vyslacb(znak, a, b, c);
    }

    public void P093(double al, double ta, double tb, boolean znak, String tez) {
        double be;
        if (Math.abs(PI / 2 - al) < PR) {
            if (tb >= 2 * ta) {
                spolecne.chyba(tez, ">=", 2 * ta);
                return;
            }
            if (tb <= ta) {
                spolecne.chyba(tez, "<=", ta);
                return;
            }
            be = spolecne.hodnota(spolecne.funkce1.f093, al, ta, tb, PI - al, 0.0);
            Vyp093(al, be, ta, znak);
            return;
        }
        if (al > PI / 2) {
            Spolecne.Double2M m = spolecne.max(spolecne.funkce1.f093, al, ta, 0.02, 0.02, 0.0);
            if (Math.abs(tb - m.h) < PR) {
                Vyp093(al, m.u, ta, znak);
                return;
            }
            if (tb > m.h) {
                spolecne.chyba(tez, ">", m.h);
                return;
            }
            if (tb <= ta) {
                spolecne.chyba(tez, "<=", ta);
                return;
            }
            be = spolecne.hodnota(spolecne.funkce1.f093, al, ta, tb, PI - al, m.u);
            Vyp093(al, be, ta, znak);
            if (tb <= 2 * ta) return;
            be = spolecne.hodnota(spolecne.funkce1.f093, al, ta, tb, 0, m.u);
            Vyp093(al, be, ta, znak);
        } else {
            Spolecne.Double2M n = spolecne.min(spolecne.funkce1.f093, al, ta, PI - al, -0.02, ta + 1);
            if (Math.abs(tb - n.h) < PR) {
                Vyp093(al, n.u, ta, znak);
                return;
            }
            if (tb < n.h) {
                spolecne.chyba(tez, "<", n.h);
                return;
            }
            if (tb >= 2 * ta) {
                spolecne.chyba(tez, ">=", 2 * ta);
                return;
            }
            be = spolecne.hodnota(spolecne.funkce1.f093, al, ta, tb, n.u, 0.0);
            Vyp093(al, be, ta, znak);
            if (tb >= ta) return;
            be = spolecne.hodnota(spolecne.funkce1.f093, al, ta, tb, n.u, PI - al);
            Vyp093(al, be, ta, znak);
        }
    }

    private void Vyp094(double al, double be, double tb) {
        double a, b, c, b1, c1, tb1;
        c1 = Math.sin(al + be) / Math.sin(al);
        b1 = Math.sin(be) / Math.sin(al);
        tb1 = spolecne.cs(b1 / 2, c1, al);
        c = tb * c1 / tb1;
        b = tb * b1 / tb1;
        a = spolecne.cs(b, c, al);
        spolecne.vysledek(a, b, c);
    }

    public void P094(double al, double tb, double tc) {
        double be;
        if (al > PI / 2 - PR) {
            if (tc <= tb / 2) {
                spolecne.chyba("tc", "<=", tb / 2);
                return;
            }
            if (tc >= 2 * tb) {
                spolecne.chyba("tc", ">=", 2 * tb);
                return;
            }
            be = spolecne.hodnota(spolecne.funkce1.f094, al, tb, tc, 0, PI - al);
            Vyp094(al, be, tb);
            return;
        }
        Spolecne.Double2M n = spolecne.min(spolecne.funkce1.f094, al, tb, 0.02, 0.02, tb);
        if (Math.abs(tc - n.h) < PR) {
            Vyp094(al, n.u, tb);
            return;
        }
        if (tc < n.h) {
            spolecne.chyba("tc", "<", n.h);
            return;
        }
        Spolecne.Double2M m = spolecne.max(spolecne.funkce1.f094, al, tb, PI - al, -0.02, 0.0);
        if (Math.abs(tc - m.h) < PR) {
            Vyp094(al, m.u, tb);
            return;
        }
        if (tc > m.h) {
            spolecne.chyba("tc", ">", m.h);
            return;
        }
        be = spolecne.hodnota(spolecne.funkce1.f094, al, tb, tc, n.u, m.u);
        Vyp094(al, be, tb);
        if (tc > 2 * tb) {
            be = spolecne.hodnota(spolecne.funkce1.f094, al, tb, tc, PI - al, m.u);
            Vyp094(al, be, tb);
            return;
        }
        if (tc >= tb / 2) return;
        be = spolecne.hodnota(spolecne.funkce1.f094, al, tb, tc, n.u, 0.0);
        Vyp094(al, be, tb);
    }

    public void P095(double va, double vb, double ta) {
        double a, b, c, m, n;
        if (ta < va) {
            spolecne.chyba("ta", "<", va);
            return;
        }
        if (ta < vb / 2) {
            spolecne.chyba("ta", "<", vb / 2);
            return;
        }
        if (ta == va && va == vb / 2) {
            spolecne.chyba("ta & va", "=", vb / 2);
            return;
        }
        m = vb / va * Math.sqrt(ta * ta - va * va);
        n = Math.sqrt(4 * ta * ta - vb * vb);
        b = 2 * ta * ta / (m + n);
        a = b * vb / va;
        c = spolecne.strtx(a, b, ta);
        spolecne.vysledek(a, b, c);
        if (vb == 2 * va || ta == vb / 2 || ta == va) return;
        b = 2 * ta * ta / Math.abs(m - n);
        a = b * vb / va;
        c = spolecne.strtx(a, b, ta);
        spolecne.vysledek(a, b, c);
    }

    public void P096(double va, double vb, double tb) {
        double a, b, c, m, n;
        if (tb < vb) {
            spolecne.chyba("tb", "<", vb);
            return;
        }
        if (tb < va / 2) {
            spolecne.chyba("tb", "<", va / 2);
            return;
        }
        if (tb == vb && vb == va / 2) {
            spolecne.chyba("tb & vb", "=", va / 2);
            return;
        }
        m = va / vb * Math.sqrt(tb * tb - vb * vb);
        n = Math.sqrt(4 * tb * tb - va * va);
        a = 2 * tb * tb / (m + n);
        b = a * va / vb;
        c = spolecne.strtx(b, a, tb);
        spolecne.vysledek(a, b, c);
        if (va == 2 * vb || va == 2 * tb || tb == vb) return;
        a = 2 * tb * tb / Math.abs(m - n);
        b = a * va / vb;
        c = spolecne.strtx(b, a, tb);
        spolecne.vysledek(a, b, c);
    }

    public void P097(double va, double vb, double tc) {
        double a, b, c, l, m, n;
        if (tc < va / 2) {
            spolecne.chyba("tc", "<", va / 2);
            return;
        }
        if (tc < vb / 2) {
            spolecne.chyba("tc", "<", vb / 2);
            return;
        }
        if (va == vb) {
            if (tc == vb / 2) {
                spolecne.chyba("va & vb", "=", 2 * tc);
                return;
            }
            b = 2 * tc * tc / Math.sqrt(4 * tc * tc - vb * vb);
            c = 2 * Math.sqrt(b * b - tc * tc);
            spolecne.vysledek(b, b, c);
            return;
        }
        if (va == 2 * tc) {
            a = vb / Math.sqrt(1 - vb * vb / va / va);
            b = a * va / vb;
            c = 2 * Math.sqrt(a * a + va * va / 4);
            spolecne.vysledek(a, b, c);
            return;
        }
        if (vb == 2 * tc) {
            b = va / Math.sqrt(1 - va * va / vb / vb);
            a = b * vb / va;
            c = 2 * Math.sqrt(b * b + vb * vb / 4);
            spolecne.vysledek(a, b, c);
            return;
        }
        l = Math.abs(va / (va * va - vb * vb));
        m = 2 * tc * tc * (va * va + vb * vb) - va * va * vb * vb;
        n = va * vb * Math.sqrt(16 * tc * tc * tc * tc - 4 * tc * tc * (va * va + vb * vb) + va * va * vb * vb);
        b = l * Math.sqrt(2 * (m - n));
        a = b * vb / va;
        c = Math.sqrt(2 * (a * a + b * b - 2 * tc * tc));
        spolecne.vysledek(a, b, c);
        b = l * Math.sqrt(2 * (m + n));
        a = b * vb / va;
        c = Math.sqrt(2 * (a * a + b * b - 2 * tc * tc));
        spolecne.vysledek(a, b, c);
    }

    public void P098(double va, double ta, double p) {
        double a, b, c, be;
        if (ta < va) {
            spolecne.chyba("ta", "<", va);
            return;
        }
        if (ta == va) {
            spolecne.mala(2 * p / va, va);
            return;
        }
        a = 2 * p / va;
        be = compLib.atan(va / (a / 2 + Math.sqrt(ta * ta - va * va)));
        c = va / Math.sin(be);
        b = spolecne.cs(a, c, be);
        spolecne.vysacb(a, b, c);
    }

    public void P099(double va, double tb, double p, boolean znak) {
        double a, b, c, m, ga;
        if (va > 2 * tb) {
            spolecne.chyba("va", ">", 2 * tb);
            return;
        }
        a = 2 * p / va;
        m = Math.sqrt(4 * tb * tb - va * va);
        if (Math.abs(2 * a - m) < PR) ga = PI / 2; else ga = compLib.atan(va / (2 * a - m));
        if (ga < 0) ga = PI + ga;
        b = va / Math.sin(ga);
        c = spolecne.cs(a, b, ga);
        spolecne.vyslacb(znak, a, b, c);
        if (tb == va / 2) return;
        ga = compLib.atan(va / (2 * a + m));
        b = va / Math.sin(ga);
        c = spolecne.cs(a, b, ga);
        spolecne.vyslacb(znak, a, b, c);
    }

    public void P100(double va, double ta, double r) {
        double tap, al, alm, e;
        Spolecne.Double3 x;
        if (va >= 2 * r) {
            spolecne.chyba("va", ">=", 2 * r);
            return;
        }
        if (ta < va) {
            spolecne.chyba("ta", "<", va);
            return;
        }
        e = compLib.asin(va / ta);
        tap = Math.sqrt(2 * r * va);
        alm = compLib.acos(va / r - 1);
        if (ta == va) {
            spolecne.malux(alm, va);
            return;
        }
        Spolecne.Double2M m = spolecne.max2(spolecne.funkce1.f100, va, r, alm - 0.01, -0.02, 0.0, 0.0);
        if (Math.abs(ta - m.h) < PR) {
            if (m.u < PR) {
                spolecne.chyba("ta", ">=", tap);
                return;
            }
            x = spolecne.abctar(ta, r, m.u, e);
            spolecne.vysacb(x.a, x.b, x.c);
            return;
        }
        if (ta > m.h) {
            spolecne.chyba("ta", ">", m.h);
            return;
        }
        al = spolecne.hodnota(spolecne.funkce1.f100, va, r, ta, alm, m.u);
        x = spolecne.abctar(ta, r, al, e);
        spolecne.vysacb(x.a, x.b, x.c);
        if (ta <= tap) return;
        al = spolecne.hodnota(spolecne.funkce1.f100, va, r, ta, 0.0, m.u);
        x = spolecne.abctar(ta, r, al, e);
        spolecne.vysacb(x.a, x.b, x.c);
    }

    private void Vyp101(double al, double r, double va, boolean znak, boolean tp) {
        Spolecne.Double3 x = spolecne.abcrval(r, va, al);
        if (tp == true) spolecne.vyslacb(znak, x.a, x.b, x.c); else spolecne.vyslacb(znak, x.a, x.c, x.b);
    }

    public void P101(double va, double tb, double r, boolean znak, String tez) {
        double q, an, tbn, al, aln;
        Spolecne.Double2M m, n, k, l;
        if (va >= 2 * r) {
            spolecne.chyba("va", ">=", 2 * r);
            return;
        }
        if (tb < va / 2) {
            spolecne.chyba(tez, "<", va / 2);
            return;
        }
        aln = compLib.acos(va / r - 1);
        an = 2 * r * Math.sin(aln);
        tbn = 0.5 * Math.sqrt(2.25 * an * an + va * va);
        q = Math.sqrt(r * va / 2);
        m = spolecne.max1(spolecne.funkce1.f101, va, r, 0.1, 0.01, 0, true);
        if (Math.abs(tb - m.h) < PR) {
            Vyp101(m.u, r, va, znak, true);
            return;
        }
        if (tb > m.h) {
            spolecne.chyba(tez, ">", m.h);
            return;
        }
        n = spolecne.min1(spolecne.funkce1.f101, va, r, 0.02, 0.02, q, false);
        if (Math.abs(tb - n.h) < PR) {
            Vyp101(n.u, r, va, znak, false);
            return;
        }
        if (tb < n.h) {
            spolecne.chyba(tez, "<", n.h);
            return;
        }
        if (tb > q + PR) {
            al = spolecne.hodnota1(spolecne.funkce1.f101, va, r, tb, 0.0, m.u, true);
            Vyp101(al, r, va, znak, true);
        }
        if (tb < q - PR) {
            al = spolecne.hodnota1(spolecne.funkce1.f101, va, r, tb, n.u, 0.0, false);
            Vyp101(al, r, va, znak, false);
        }
        k = spolecne.max3(spolecne.funkce1.f101, va, r, n.u, 0.02, 0.0, aln, false);
        if (Math.abs(k.u - aln) < 0.1) {
            if (Math.abs(tb - tbn) < PR) {
                Vyp101(aln, r, va, znak, true);
                return;
            }
            if (tb > tbn) {
                al = spolecne.hodnota1(spolecne.funkce1.f101, va, r, tb, aln, m.u, true);
                Vyp101(al, r, va, znak, true);
                return;
            } else {
                al = spolecne.hodnota1(spolecne.funkce1.f101, va, r, tb, n.u, aln, false);
                Vyp101(al, r, va, znak, false);
                return;
            }
        }
        l = spolecne.min1(spolecne.funkce1.f101, va, r, k.u, 0.02, k.h + 1, false);
        if (Math.abs(tb - tbn) < PR) Vyp101(aln, r, va, znak, true);
        if (tb > tbn) {
            al = spolecne.hodnota1(spolecne.funkce1.f101, va, r, tb, aln, m.u, true);
            Vyp101(al, r, va, znak, true);
        }
        if (Math.abs(tb - k.h) < PR) {
            Vyp101(k.u, r, va, znak, false);
            return;
        }
        if (tb < k.h) {
            al = spolecne.hodnota1(spolecne.funkce1.f101, va, r, tb, n.u, k.u, false);
            Vyp101(al, r, va, znak, false);
        } else return;
        if (Math.abs(tb - l.h) < PR) {
            Vyp101(l.u, r, va, znak, false);
            return;
        }
        if (tb < l.h) return;
        al = spolecne.hodnota1(spolecne.funkce1.f101, va, r, tb, l.u, k.u, false);
        Vyp101(al, r, va, znak, false);
        if (tb > tbn || Math.abs(tb - tbn) < PR) return;
        al = spolecne.hodnota1(spolecne.funkce1.f101, va, r, tb, l.u, aln, false);
        Vyp101(al, r, va, znak, false);
    }

    public void P102(double va, double ta, double ro) {
        if (va <= 2 * ro) {
            spolecne.chyba("va", "<=", 2 * ro);
            return;
        }
        if (ta < va) {
            spolecne.chyba("ta", "<", va);
            return;
        }
        if (ta == va) {
            spolecne.malux(2 * compLib.asin(ro / (va - ro)), va);
            return;
        }
        double e = compLib.acos((va - ro) * (va - ro) / Math.sqrt((ta * ta - va * va) * (va - 2 * ro) * (va - 2 * ro) + (va - ro) * (va - ro) * (va - ro) * (va - ro)));
        Spolecne.Double3 x = spolecne.abcrova(va, ro, e);
        spolecne.vysacb(x.a, x.b, x.c);
    }

    private void Vyp103(double e, double va, double ro, boolean znak) {
        Spolecne.Double3 x = spolecne.abcrova(va, ro, e);
        spolecne.vyslacb(znak, x.a, x.b, x.c);
    }

    public void P103(double va, double tb, double ro, boolean znak, String tez) {
        double e;
        if (va <= 2 * ro) {
            spolecne.chyba("va", "<=", 2 * ro);
            return;
        }
        if (va == 4 * ro) {
            if (tb <= va / 2) {
                spolecne.chyba(tez, "<=", va / 2);
                return;
            }
            e = spolecne.hodnota(spolecne.funkce1.f103, va, ro, tb, -PI / 2, PI / 2);
            Vyp103(e, va, ro, znak);
            return;
        }
        Spolecne.Double2M n = spolecne.min(spolecne.funkce1.f103, va, ro, 0.02 - PI / 2, 0.02, 100000);
        if (Math.abs(tb - n.h) < PR) {
            Vyp103(n.u, va, ro, znak);
            return;
        }
        if (tb < n.h) {
            spolecne.chyba(tez, "<", n.h);
            return;
        }
        e = spolecne.hodnota(spolecne.funkce1.f103, va, ro, tb, n.u, PI / 2);
        Vyp103(e, va, ro, znak);
        e = spolecne.hodnota(spolecne.funkce1.f103, va, ro, tb, n.u, -PI / 2);
        Vyp103(e, va, ro, znak);
    }

    private void Vyp104(double e, double a, double ta, boolean znak) {
        Spolecne.Double2 x = spolecne.bcta(a, ta, e);
        spolecne.vyslacb(znak, a, x.b, x.c);
    }

    public void P104(double va, double ta, double tb, boolean znak) {
        double a, m, n, e;
        if (va > ta) {
            spolecne.chyba("va", ">", ta);
            return;
        }
        if (va > 2 * tb) {
            spolecne.chyba("va", ">", 2 * tb);
            return;
        }
        m = Math.sqrt(4 * tb * tb - va * va);
        n = Math.sqrt(ta * ta - va * va);
        e = compLib.asin(va / ta);
        if (ta == va) {
            if (ta == 2 * tb) {
                spolecne.chyba("va & ta", " = ", 2 * tb);
                return;
            }
            Vyp104(e, 2 * m / 3, ta, znak);
            return;
        }
        if (tb == va / 2) {
            Vyp104(PI - e, 2 * n / 3, ta, znak);
            return;
        }
        if (tb == ta / 2) {
            Vyp104(PI - e, 4 * n / 3, ta, znak);
            return;
        }
        a = 2 * Math.abs(m - n) / 3;
        if (tb < ta / 2) Vyp104(PI - e, a, ta, znak); else Vyp104(e, a, ta, znak);
        a = 2 * (m + n) / 3;
        Vyp104(PI - e, a, ta, znak);
    }

    private void Vyp105(double a, double k, double l, double m) {
        double b, c;
        b = 2 * Math.sqrt((a - k) * (a - k) + m);
        c = 2 * Math.sqrt((a - l) * (a - l) + m);
        spolecne.vysledek(a, b, c);
    }

    public void P105(double va, double tb, double tc) {
        double a, b, c, k, l, m;
        if (tb < va / 2) {
            spolecne.chyba("tb", "<", va / 2);
            return;
        }
        if (tc < va / 2) {
            spolecne.chyba("tc", "<", va / 2);
            return;
        }
        m = va * va / 4;
        k = Math.sqrt(tb * tb - m);
        l = Math.sqrt(tc * tc - m);
        if (tb == va / 2) {
            if (tc == va / 2) {
                spolecne.chyba("tb & tc", "=", va / 2);
                return;
            }
            Vyp105(2 * l / 3, k, l, m);
            return;
        }
        if (tc == va / 2) {
            Vyp105(2 * k / 3, k, l, m);
            return;
        }
        Vyp105(2 * (k + l) / 3, k, l, m);
        if (tb == tc) return;
        if (tc > tb) {
            a = 2 * (l - k) / 3;
            b = 2 * Math.sqrt((a + k) * (a + k) + m);
            c = 2 * Math.sqrt((a - l) * (a - l) + m);
        } else {
            a = 2 * (k - l) / 3;
            b = 2 * Math.sqrt((a - k) * (a - k) + m);
            c = 2 * Math.sqrt((a + l) * (a + l) + m);
        }
        spolecne.vysledek(a, b, c);
    }

    private void Vyp106(double al, double r, double p) {
        Spolecne.Double3 x = spolecne.abcrval(r, p / r / Math.sin(al), al);
        spolecne.vysacb(x.a, x.b, x.c);
    }

    public void P106(double ta, double p, double r) {
        double a, pm, al;
        Spolecne.Double2M k, l, m, n;
        pm = 1.299038 * r * r;
        if (Math.abs(p - pm) < PR) {
            if (Math.abs(ta - 1.5 * r) > PR) {
                spolecne.chyba("ta", "<>", 1.5 * r);
                return;
            }
            a = 2 * r * Math.sin(PI / 3);
            spolecne.vysledek(a, a, a);
            return;
        }
        if (p > pm) {
            spolecne.chyba("p", ">", pm);
            return;
        }
        k = spolecne.mez1(p, r, 0.0001, 0.02);
        if (Math.abs(ta - k.h) < PR) {
            spolecne.malux(k.u, ta);
            return;
        }
        if (ta > k.h) {
            spolecne.chyba("ta", ">", k.h);
            return;
        }
        l = spolecne.mez1(p, r, PI - 0.01, -0.02);
        if (Math.abs(ta - l.h) < PR) {
            spolecne.malux(l.u, ta);
            return;
        }
        if (ta < l.h) {
            spolecne.chyba("ta", "<", l.h);
            return;
        }
        n = spolecne.min2(spolecne.funkce1.f106, p, r, k.u + 0.01, 0.02, 10000, l.u);
        if (Math.abs(n.u - l.u) < PR) {
            al = spolecne.hodnota(spolecne.funkce1.f106, p, r, ta, l.u, k.u);
            Vyp106(al, r, p);
            return;
        }
        m = spolecne.max(spolecne.funkce1.f106, p, r, n.u, 0.02, 0);
        if (Math.abs(ta - n.h) < PR) {
            Vyp106(n.u, r, p);
            al = spolecne.hodnota(spolecne.funkce1.f106, p, r, ta, l.u, m.u);
            Vyp106(al, r, p);
            return;
        }
        if (ta < n.h) {
            al = spolecne.hodnota(spolecne.funkce1.f106, p, r, ta, l.u, m.u);
            Vyp106(al, r, p);
            return;
        }
        al = spolecne.hodnota(spolecne.funkce1.f106, p, r, ta, n.u, k.u);
        Vyp106(al, r, p);
        if (Math.abs(ta - m.h) < PR) {
            Vyp106(m.u, r, p);
            return;
        }
        if (ta > m.h) return;
        al = spolecne.hodnota(spolecne.funkce1.f106, p, r, ta, n.u, m.u);
        Vyp106(al, r, p);
        al = spolecne.hodnota(spolecne.funkce1.f106, p, r, ta, l.u, m.u);
        Vyp106(al, r, p);
    }

    public void P107(double ta, double p, double ro) {
        double a, va, bem, rom, roo, e, ep, ek, be, ga;
        bem = compLib.atan(ta * ta / p);
        rom = p / ta * Math.tan(bem / 2);
        if (Math.abs(ro - rom) < PR) {
            spolecne.mala(2 * p / ta, ta);
            return;
        }
        if (ro > rom) {
            spolecne.chyba("ro", ">", rom);
            return;
        }
        ep = 0;
        ek = PI / 2;
        do {
            e = (ep + ek) / 2;
            va = ta * Math.sin(e);
            a = 2 * p / va;
            ga = spolecne.gata(a, ta, e);
            be = spolecne.gata(a, ta, PI - e);
            roo = a / (1 / Math.tan(be / 2) + 1 / Math.tan(ga / 2));
            if (roo < ro) ep = e; else ek = e;
        } while (Math.abs(ro - roo) < PR);
        Spolecne.Double3 x = spolecne.vabega(va, be, ga);
        spolecne.vysacb(x.a, x.b, x.c);
    }

    private void Vyp108(double a, double p, double ta, double tb) {
        double b, c, vap;
        vap = p / a;
        if (ta <= 2 * tb) b = 2 * Math.sqrt(vap * vap + (a - Math.sqrt(tb * tb - vap * vap)) * (a - Math.sqrt(tb * tb - vap * vap))); else b = 2 * Math.sqrt(vap * vap + (a + Math.sqrt(tb * tb - vap * vap)) * (a + Math.sqrt(tb * tb - vap * vap)));
        c = spolecne.strtx(b, a, tb);
        spolecne.vysledek(a, b, c);
    }

    public void P108(double ta, double tb, double p) {
        double pm = 2 * ta * tb / 3;
        if (p > pm) {
            spolecne.chyba("p", ">", pm);
            return;
        }
        Vyp108(2 * Math.sqrt(4 * tb * tb + ta * ta - 2 * Math.sqrt(4 * ta * ta * tb * tb - 9 * p * p)) / 3, p, ta, tb);
        if (Math.abs(p - pm) < PR) return;
        Vyp108(2 * Math.sqrt(4 * tb * tb + ta * ta + 2 * Math.sqrt(4 * ta * ta * tb * tb - 9 * p * p)) / 3, p, ta, tb);
    }

    private void Vyp109(double al, double r, double ro) {
        double a, k, be;
        a = 2 * r * Math.sin(al);
        k = a / ro;
        be = 2 * compLib.atan((k - Math.sqrt(k * k - 4 * (1 + k * Math.tan(al / 2)))) / 2 / (1 + k * Math.tan(al / 2)));
        spolecne.vysacb(a, 2 * r * Math.sin(be), 2 * r * Math.sin(al + be));
    }

    public void P109(double ta, double r, double ro) {
        double alma, almi, tama, tami;
        Spolecne.Double2M m, n;
        if (ro == r / 2) {
            if (ta != 1.5 * r) {
                spolecne.chyba("ta", "<>", 1.5 * r);
                return;
            }
            spolecne.malux(PI / 3, ta);
            return;
        }
        if (ro > r / 2) {
            spolecne.chyba("ro", ">", r / 2);
            return;
        }
        alma = compLib.acos(ro / r + Math.sqrt(1 - 2 * ro / r));
        almi = compLib.acos(ro / r - Math.sqrt(1 - 2 * ro / r));
        if (almi < 0) almi = PI + almi;
        tama = r * (1 + Math.cos(alma));
        tami = r * (1 + Math.cos(almi));
        if (Math.abs(ta - tama) < PR) {
            spolecne.malux(alma, ta);
            return;
        }
        if (ta > tama) {
            spolecne.chyba("ta", ">", tama);
            return;
        }
        if (Math.abs(ta - tami) < PR) {
            spolecne.malux(almi, ta);
            return;
        }
        if (ta < tami) {
            spolecne.chyba("ta", "<", tami);
            return;
        }
        n = spolecne.min2(spolecne.funkce1.f109, r, ro, alma + 0.01, 0.02, 10000, almi);
        if (Math.abs(n.u - almi) < PR) {
            Vyp109(spolecne.hodnota(spolecne.funkce1.f109, r, ro, ta, almi, alma), r, ro);
            return;
        }
        m = spolecne.max(spolecne.funkce1.f109, r, ro, n.u, 0.02, 0.0);
        if (Math.abs(ta - n.h) < PR) {
            Vyp109(n.u, r, ro);
            Vyp109(spolecne.hodnota(spolecne.funkce1.f109, r, ro, ta, almi, m.u), r, ro);
            return;
        }
        if (ta < n.h) {
            Vyp109(spolecne.hodnota(spolecne.funkce1.f109, r, ro, ta, almi, m.u), r, ro);
            return;
        }
        Vyp109(spolecne.hodnota(spolecne.funkce1.f109, r, ro, ta, n.u, alma), r, ro);
        if (Math.abs(ta - m.h) < PR) {
            Vyp109(m.u, r, ro);
            return;
        }
        if (ta > m.h) return;
        Vyp109(spolecne.hodnota(spolecne.funkce1.f109, r, ro, ta, n.u, m.u), r, ro);
        Vyp109(spolecne.hodnota(spolecne.funkce1.f109, r, ro, ta, almi, m.u), r, ro);
    }

    private void Vyp110(double e, double ta, double tb) {
        Spolecne.Double3 x = spolecne.abctatb(ta, tb, e);
        spolecne.vysledek(x.a, x.b, x.c);
    }

    public void P110(double ta, double tb, double r) {
        if (ta >= 2 * r) {
            spolecne.chyba("ta", ">=", 2 * r);
            return;
        }
        if (tb >= 2 * r) {
            spolecne.chyba("tb", ">=", 2 * r);
            return;
        }
        if (ta == 0.5 * tb) {
            if (r <= 2 * tb / 3) {
                spolecne.chyba("r", "<=", 2 * tb / 3);
                return;
            }
            Vyp110(spolecne.hodnota(spolecne.funkce1.f110, ta, tb, r, 0, PI), ta, tb);
            return;
        }
        if (ta == 2 * tb) {
            if (r <= 4 * tb / 3) {
                spolecne.chyba("r", "<=", 4 * tb / 3);
                return;
            }
            Vyp110(spolecne.hodnota(spolecne.funkce1.f110, ta, tb, r, 0.0, PI), ta, tb);
            return;
        }
        Spolecne.Double2M n = spolecne.min(spolecne.funkce1.f110, ta, tb, 0.001, 0.02, 100000);
        if (Math.abs(r - n.h) < PR) {
            Vyp110(n.u, ta, tb);
            return;
        }
        if (r < n.h) {
            spolecne.chyba("r", "<", n.h);
            return;
        }
        Vyp110(spolecne.hodnota(spolecne.funkce1.f110, ta, tb, r, n.u, 0.0), ta, tb);
        if (ta == tb && r >= tb) return;
        Vyp110(spolecne.hodnota(spolecne.funkce1.f110, ta, tb, r, n.u, PI), ta, tb);
    }

    private void Vyp111(double e, double ta, double tb) {
        Spolecne.Double3 x = spolecne.abctatb(ta, tb, e);
        spolecne.vysledek(x.a, x.b, x.c);
    }

    public void P111(double ta, double tb, double ro) {
        Spolecne.Double2M m = spolecne.max(spolecne.funkce1.f111, ta, tb, 0.02, 0.02, 0.0);
        if (Math.abs(ro - m.h) < PR) {
            Vyp111(m.u, ta, tb);
            return;
        }
        if (ro > m.h) {
            spolecne.chyba("ro", ">", m.h);
            return;
        }
        Vyp111(spolecne.hodnota(spolecne.funkce1.f111, ta, tb, ro, 0.0, m.u), ta, tb);
        Vyp111(spolecne.hodnota(spolecne.funkce1.f111, ta, tb, ro, PI, m.u), ta, tb);
    }

    public void P112(double ta, double tb, double tc) {
        double a, b, c;
        if (ta <= Math.abs(tb - tc)) {
            spolecne.chyba("ta", "<=", Math.abs(tb - tc));
            return;
        }
        if (ta >= tb + tc) {
            spolecne.chyba("ta", ">=", tb + tc);
            return;
        }
        a = 2 * Math.sqrt(2 * tb * tb + 2 * tc * tc - ta * ta) / 3;
        b = 2 * Math.sqrt(2 * ta * ta + 2 * tc * tc - tb * tb) / 3;
        c = 2 * Math.sqrt(2 * ta * ta + 2 * tb * tb - tc * tc) / 3;
        spolecne.vysledek(a, b, c);
    }
}
