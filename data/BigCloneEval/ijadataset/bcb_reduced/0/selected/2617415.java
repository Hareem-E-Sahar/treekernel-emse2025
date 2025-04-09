package trojuhelnik;

public class Vypocty4 {

    public final double PR = 0.00001;

    public final double PI = Math.PI;

    private Spolecne spolecne;

    private CompLibInt compLib;

    /** Creates a new instance of Vypocty4 */
    public Vypocty4(Spolecne spolecne) {
        this.spolecne = spolecne;
        this.compLib = spolecne.compLib;
    }

    public void P200(double c, double al, double ab, boolean znak) {
        if (ab <= c) {
            spolecne.chyba("ab", "<=", c);
            return;
        }
        double b = (ab * ab - c * c) / 2 / (ab - c * Math.cos(al));
        spolecne.vyslbac(znak, ab - b, b, c);
    }

    public void P201(double c, double ga, double ab) {
        if (ab <= c) {
            spolecne.chyba("ab", "<=", c);
            return;
        }
        double abma = c / Math.sin(ga / 2);
        if (Math.abs(ab - abma) < PR) {
            spolecne.vysledek(ab / 2, ab / 2, c);
            return;
        }
        if (ab > abma) {
            spolecne.chyba("ab", ">", abma);
            return;
        }
        double a = ab / 2 + Math.sqrt(ab * ab / 4 - (ab * ab - c * c) / 2 / (1 + Math.cos(ga)));
        spolecne.vysbac(a, ab - a, c);
    }

    public void P202(double c, double va, double ab, boolean znak) {
        if (ab <= c) {
            spolecne.chyba("ab", "<=", c);
            return;
        }
        if (c < va) {
            spolecne.chyba("c", ">", va);
            return;
        }
        double be = compLib.asin(va / c);
        double a = (ab * ab - c * c) / 2 / (ab - c * Math.cos(be));
        spolecne.vyslbac(znak, a, ab - a, c);
        if (va == c) return;
        a = (ab * ab - c * c) / 2 / (ab + c * Math.cos(be));
        spolecne.vyslbac(znak, a, ab - a, c);
    }

    public void P203(double c, double vc, double ab) {
        double abm = Math.sqrt(4 * vc * vc + c * c);
        if (Math.abs(ab - abm) < PR) {
            spolecne.vysledek(ab / 2, ab / 2, c);
            return;
        }
        if (ab < abm) {
            spolecne.chyba("ab", "<", abm);
            return;
        }
        double a = Math.sqrt(vc * vc + (c / 2 + ab * Math.sqrt(0.25 - vc * vc / (ab * ab - c * c))) * (c / 2 + ab * Math.sqrt(0.25 - vc * vc / (ab * ab - c * c))));
        spolecne.vysbac(a, ab - a, c);
    }

    public void P204(double c, double ta, double ab, boolean znak) {
        double a, abmi, abma;
        if (ta < c / 2 || ta > c) abmi = Math.abs(4 * ta - 3 * c); else abmi = c;
        if (ab <= abmi) {
            spolecne.chyba("ab", "<=", abmi);
            return;
        }
        abma = 3 * c + 4 * ta;
        if (ab >= abma) {
            spolecne.chyba("ab", ">=", abma);
            return;
        }
        a = 2 * ab - Math.sqrt(2 * (ab * ab - c * c + 2 * ta * ta));
        spolecne.vyslbac(znak, a, ab - a, c);
    }

    public void P205(double c, double tc, double ab) {
        double a, abma, abmi;
        abma = Math.sqrt(4 * tc * tc + c * c);
        if (Math.abs(ab - abma) < PR) {
            spolecne.vysledek(abma / 2, abma / 2, c);
            return;
        }
        if (ab > abma) {
            spolecne.chyba("ab", ">", abma);
            return;
        }
        if (tc <= c / 2) abmi = c; else abmi = 2 * tc;
        if (ab <= abmi) {
            spolecne.chyba("ab", "<=", abmi);
            return;
        }
        a = ab / 2 + 0.5 * Math.sqrt(4 * tc * tc + c * c - ab * ab);
        spolecne.vysbac(a, ab - a, c);
    }

    public void P206(double c, double p, double ab) {
        double a, abmi, k;
        k = 4 * p * p / c / c;
        abmi = Math.sqrt(c * c + 4 * k);
        if (Math.abs(ab - abmi) < PR) {
            spolecne.vysledek(ab / 2, ab / 2, c);
            return;
        }
        if (ab < abmi) {
            spolecne.chyba("ab", "<", abmi);
            return;
        }
        a = Math.sqrt(k + (c / 2 + ab * Math.sqrt(0.25 - k / (ab * ab - c * c))));
        spolecne.vysbac(a, ab - a, c);
    }

    public void P207(double c, double r, double ab) {
        double a, abm1, abm2, ga;
        if (r < c / 2) {
            spolecne.chyba("r", "<", c / 2);
            return;
        }
        if (ab <= c) {
            spolecne.chyba("ab", "<=", c);
            return;
        }
        ga = compLib.asin(c / 2 / r);
        abm1 = c / Math.sin(ga / 2);
        if (Math.abs(ab - abm1) < PR) {
            spolecne.vysledek(ab / 2, ab / 2, c);
            return;
        }
        if (ab > abm1) {
            spolecne.chyba("ab", ">", abm1);
            return;
        }
        a = ab / 2 + Math.sqrt(ab * ab / 4 - (ab * ab - c * c) / 2 / (1 + Math.cos(ga)));
        spolecne.vysbac(a, ab - a, c);
        abm2 = c / Math.sqrt((1 - Math.cos(PI - ga)) / 2);
        if (Math.abs(ab - abm2) < PR) {
            spolecne.vysledek(ab / 2, ab / 2, c);
            return;
        }
        if (ab > abm2 || c == 2 * r) return;
        a = ab / 2 + Math.sqrt(ab * ab / 4 - (ab * ab - c * c) / 2 / (1 + Math.cos(PI - ga)));
        spolecne.vysbac(a, ab - a, c);
    }

    public void P208(double c, double ro, double ab) {
        if (ab <= c) {
            spolecne.chyba("ab", "<=", c);
            return;
        }
        double rom = c / 2 * Math.sqrt((ab - c) / (ab + c));
        if (Math.abs(ro - rom) < PR) {
            spolecne.vysledek(ab / 2, ab / 2, c);
            return;
        }
        if (ro > rom) {
            spolecne.chyba("ro", ">", rom);
            return;
        }
        double s = (ab + c) / 2;
        double a = ab / 2 + Math.sqrt(ab * ab / 4 - s * (ab - s + ro * ro / (s - c)));
        spolecne.vysbac(a, ab - a, c);
    }

    public void P209(double al, double be, double ab) {
        if (al + be >= PI) {
            spolecne.chyba2("alfa_beta", ">=", 180.0);
            return;
        }
        double a = ab * Math.sin(al) / (Math.sin(al) + Math.sin(be));
        spolecne.vysledek(a, ab - a, a * Math.sin(al + be) / Math.sin(al));
    }

    public void P210(double al, double ga, double ab, boolean znak, String uhel) {
        if (al + ga >= PI) {
            if (uhel.equals("alfa")) {
                spolecne.chyba2("alfa_gama", ">=", 180.0);
                return;
            } else {
                spolecne.chyba(uhel + " + gama", ">=", 180.0);
                return;
            }
        }
        double a = ab * Math.sin(al) / (Math.sin(al) + Math.sin(al + ga));
        spolecne.vyslbac(znak, a, ab - a, a * Math.sin(ga) / Math.sin(al));
    }

    private void Vyp211(double be, double al, double va, boolean znak) {
        Spolecne.Double3 x = spolecne.vabega(va, be, PI - al - be);
        spolecne.vyslbac(znak, x.a, x.b, x.c);
    }

    public void P211(double al, double va, double ab, boolean znak) {
        Spolecne.Double2M n = spolecne.min(spolecne.funkce2.f211, al, va, 0.02, 0.02, 100000.0);
        if (Math.abs(ab - n.h) < PR) {
            Vyp211(n.u, al, va, znak);
            return;
        }
        if (ab < n.h) {
            spolecne.chyba("ab", "<", n.h);
            return;
        }
        Vyp211(spolecne.hodnota(spolecne.funkce2.f211, al, va, ab, n.u, 0.0), al, va, znak);
        Vyp211(spolecne.hodnota(spolecne.funkce2.f211, al, va, ab, n.u, PI - al), al, va, znak);
    }

    public void P212(double al, double vb, double ab, boolean znak) {
        double c = vb / Math.sin(al);
        if (ab <= c) {
            spolecne.chyba("ab", "<=", c);
            return;
        }
        double b = (ab * ab - c * c) / 2 / (ab - c * Math.cos(al));
        spolecne.vyslbac(znak, ab - b, b, c);
    }

    public void P213(double al, double vc, double ab, boolean znak) {
        double a, b, c, abm;
        b = vc / Math.sin(al);
        a = ab - b;
        if (al > PI / 2 || Math.abs(al - PI / 2) < PR) {
            if (ab <= 2 * b) {
                spolecne.chyba("ab", "<=", 2 * b);
                return;
            }
            c = b * Math.cos(al) + Math.sqrt(a * a - vc * vc);
            spolecne.vyslbac(znak, a, b, c);
            return;
        }
        abm = b + vc;
        if (Math.abs(ab - abm) < PR) {
            spolecne.vyslbac(znak, a, b, Math.sqrt(b * b - vc * vc));
            return;
        }
        if (ab < abm) {
            spolecne.chyba("ab", "<", abm);
            return;
        }
        c = b * Math.cos(al) + Math.sqrt(a * a - vc * vc);
        spolecne.vyslbac(znak, a, b, c);
        if (ab >= 2 * b) return;
        c = b * Math.cos(al) - Math.sqrt(a * a - vc * vc);
        spolecne.vyslbac(znak, a, b, c);
    }

    private void Vyp214(double e, double al, double ta, boolean znak) {
        Spolecne.Double3 x = spolecne.altae(al, ta, e);
        spolecne.vyslbac(znak, x.a, x.b, x.c);
    }

    public void P214(double al, double ta, double ab, boolean znak) {
        if (al > PI / 2) {
            if (ab <= 2 * ta) {
                spolecne.chyba("ab", "<=", 2 * ta);
                return;
            }
            Spolecne.Double2M m = spolecne.max(spolecne.funkce2.f214, al, ta, 0.02, 0.02, 0.0);
            if (Math.abs(ab - m.h) < PR) {
                Vyp214(m.u, al, ta, znak);
                return;
            }
            if (ab > m.h) {
                spolecne.chyba("ab", ">", m.h);
                return;
            }
            Vyp214(spolecne.hodnota(spolecne.funkce2.f214, al, ta, ab, 0.0, m.u), al, ta, znak);
            if (ab <= 4 * ta) return;
            Vyp214(spolecne.hodnota(spolecne.funkce2.f214, al, ta, ab, PI, m.u), al, ta, znak);
            return;
        }
        if (ab >= 4 * ta) {
            spolecne.chyba("ab", ">=", 4 * ta);
            return;
        }
        Spolecne.Double2M n = spolecne.min2(spolecne.funkce2.f214, al, ta, PI, -0.02, 5 * ta, 0.0);
        if (Math.abs(n.u) < PR) {
            if (ab <= 2 * ta) {
                spolecne.chyba("ab", "<=", 2 * ta);
                return;
            }
            Vyp214(spolecne.hodnota(spolecne.funkce2.f214, al, ta, ab, 0.0, PI), al, ta, znak);
            return;
        }
        if (Math.abs(ab - n.h) < PR) {
            Vyp214(n.u, al, ta, znak);
            return;
        }
        if (ab < n.h) {
            spolecne.chyba("ab", "<", n.h);
            return;
        }
        Vyp214(spolecne.hodnota(spolecne.funkce2.f214, al, ta, ab, n.u, PI), al, ta, znak);
        if (ab >= 2 * ta) return;
        Vyp214(spolecne.hodnota(spolecne.funkce2.f214, al, ta, ab, n.u, 0.0), al, ta, znak);
    }

    private void Vyp215(double e, double al, double tb, boolean znak) {
        Spolecne.Double3 x = spolecne.abctaub(tb, PI - e, al);
        spolecne.vyslbac(znak, x.b, x.a, x.c);
    }

    public void P215(double al, double tb, double ab, boolean znak) {
        if (ab <= tb) {
            spolecne.chyba("ab", "<=", tb);
            return;
        }
        if (al > PI / 2 || Math.abs(al - PI / 2) < PR) {
            if (ab >= 4 * tb) {
                spolecne.chyba("ab", ">=", 4 * tb);
                return;
            }
            Vyp215(spolecne.hodnota(spolecne.funkce2.f215, al, tb, ab, PI - al, 0.0), al, tb, znak);
            return;
        }
        Spolecne.Double2M m = spolecne.max(spolecne.funkce2.f215, al, tb, 0.02, 0.02, 0.0);
        if (Math.abs(ab - m.h) < PR) {
            Vyp215(m.u, al, tb, znak);
            return;
        }
        if (ab > m.h) {
            spolecne.chyba("ab", ">", m.h);
            return;
        }
        Vyp215(spolecne.hodnota(spolecne.funkce2.f215, al, tb, ab, PI - al, m.u), al, tb, znak);
        if (ab <= 4 * tb) return;
        Vyp215(spolecne.hodnota(spolecne.funkce2.f215, al, tb, ab, 0.0, m.u), al, tb, znak);
    }

    private void Vyp216(double e, double al, double tc, boolean znak) {
        Spolecne.Double3 x = spolecne.abctaub(tc, PI - e, al);
        spolecne.vyslbac(znak, x.b, x.c, x.a);
    }

    public void P216(double al, double tc, double ab, boolean znak) {
        if (ab <= 2 * tc) {
            spolecne.chyba("ab", "<=", 2 * tc);
            return;
        }
        Spolecne.Double2M m = spolecne.max(spolecne.funkce2.f216, al, tc, 0.02, 0.02, 0.0);
        if (Math.abs(ab - m.h) < PR) {
            Vyp216(m.u, al, tc, znak);
            return;
        }
        if (ab > m.h) {
            spolecne.chyba("ab", ">", m.h);
            return;
        }
        Vyp216(spolecne.hodnota(spolecne.funkce2.f216, al, tc, ab, 0.0, m.u), al, tc, znak);
        Vyp216(spolecne.hodnota(spolecne.funkce2.f216, al, tc, ab, PI - al, m.u), al, tc, znak);
    }

    private void Vyp217(double be, double al, double p, boolean znak) {
        double c = Math.sqrt(2 * p * Math.sin(al + be) / Math.sin(al) / Math.sin(be));
        Spolecne.Double2 x = spolecne.bca(c, al + be, be, al);
        spolecne.vyslbac(znak, x.c, x.b, c);
    }

    public void P217(double al, double p, double ab, boolean znak) {
        Spolecne.Double2M n = spolecne.min(spolecne.funkce2.f217, al, p, 0.02, 0.02, 100000.0);
        if (Math.abs(ab - n.h) < PR) {
            Vyp217(n.u, al, p, znak);
            return;
        }
        if (ab < n.h) {
            spolecne.chyba("ab", "<", n.h);
            return;
        }
        Vyp217(spolecne.hodnota(spolecne.funkce2.f217, al, p, ab, n.u, 0.0), al, p, znak);
        Vyp217(spolecne.hodnota(spolecne.funkce2.f217, al, p, ab, n.u, PI - al), al, p, znak);
    }

    private void Vyp218(int k, double a, double b, double r, double al, boolean znak) {
        double c;
        if (k == 0) c = b * Math.cos(al) + Math.sqrt(a * a - (b * Math.sin(al)) * (b * Math.sin(al))); else if (k == 1) c = b * Math.cos(al) - Math.sqrt(a * a - (b * Math.sin(al)) * (b * Math.sin(al))); else c = 2 * r * Math.cos(al);
        spolecne.vyslbac(znak, a, b, c);
    }

    public void P218(double al, double r, double ab, boolean znak) {
        double a = 2 * r * Math.sin(al);
        double b = ab - a;
        if (ab <= a) {
            spolecne.chyba("ab", "<=", a);
            return;
        }
        if (al > PI / 2 || Math.abs(al - PI / 2) < PR) {
            if (ab >= 2 * a) {
                spolecne.chyba("ab", ">=", 2 * a);
                return;
            }
            Vyp218(0, a, b, r, al, znak);
            return;
        }
        double abm = 2 * r * (Math.sin(al) + 1);
        if (Math.abs(ab - abm) < PR) {
            Vyp218(2, a, b, r, al, znak);
            return;
        }
        if (ab > abm) {
            spolecne.chyba("ab", ">", abm);
            return;
        }
        Vyp218(0, a, b, r, al, znak);
        if (b <= a) return;
        Vyp218(1, a, b, r, al, znak);
    }

    private void Vyp219(double e, double al, double ro, boolean znak) {
        double va = ro * Math.cos(e) / Math.sin(al / 2) + ro;
        Spolecne.Double3 x = spolecne.abcva(va, al, e);
        spolecne.vyslbac(znak, x.a, x.b, x.c);
    }

    public void P219(double al, double ro, double ab, boolean znak) {
        Spolecne.Double2M n = spolecne.min(spolecne.funkce2.f219, al, ro, 0.02, 0.02, 100000.0);
        if (Math.abs(ab - n.h) < PR) {
            Vyp219(n.u, al, ro, znak);
            return;
        }
        if (ab < n.h) {
            spolecne.chyba("ab", "<", n.h);
            return;
        }
        Vyp219(spolecne.hodnota(spolecne.funkce2.f219, al, ro, ab, n.u, -PI / 2 + al / 2), al, ro, znak);
        Vyp219(spolecne.hodnota(spolecne.funkce2.f219, al, ro, ab, n.u, PI / 2 - al / 2), al, ro, znak);
    }

    public void P220(double ga, double va, double ab, boolean znak) {
        double b = va / Math.sin(ga);
        if (ab <= b) {
            spolecne.chyba("ab", "<=", b);
            return;
        }
        double a = ab - b;
        double c = spolecne.cs(a, b, ga);
        spolecne.vyslbac(znak, a, b, c);
    }

    public void P221(double ga, double vc, double ab) {
        double a, b, c, abm, al;
        abm = 2 * vc / Math.cos(ga / 2);
        if (Math.abs(ab - abm) < PR) {
            spolecne.malcux(ga, vc);
            return;
        }
        if (ab < abm) {
            spolecne.chyba("ab", "<", abm);
            return;
        }
        al = spolecne.hodnota(spolecne.funkce2.f221, ga, vc, ab, PI / 2 - ga / 2, 0.0);
        b = vc / Math.sin(al);
        a = ab - b;
        c = spolecne.cs(a, b, ga);
        spolecne.vysbac(a, b, c);
    }

    public void P222(double va, double vb, double ab) {
        double a, b, ga;
        if (ab == va + vb) {
            spolecne.vysledek(vb, va, Math.sqrt(va * va + vb * vb));
            return;
        }
        if (ab < va + vb) {
            spolecne.chyba("ab", "<", va + vb);
            return;
        }
        a = ab * vb / (va + vb);
        b = ab - a;
        ga = compLib.asin(va / b);
        spolecne.vysledek(a, b, spolecne.cs(a, b, ga));
        spolecne.vysledek(a, b, spolecne.cs(a, b, PI - ga));
    }

    private void Vyp223(double be, double va, double vc, boolean znak) {
        Spolecne.Double3 x = spolecne.vavbga(va, vc, be);
        spolecne.vyslbac(znak, x.a, x.c, x.b);
    }

    public void P223(double va, double vc, double ab, boolean znak) {
        Spolecne.Double2M n = spolecne.min(spolecne.funkce2.f223, va, vc, 0.02, 0.02, 100000.0);
        if (Math.abs(ab - n.h) < PR) {
            Vyp223(n.u, va, vc, znak);
            return;
        }
        if (ab < n.h) {
            spolecne.chyba("ab", "<", n.h);
            return;
        }
        Vyp223(spolecne.hodnota(spolecne.funkce2.f223, va, vc, ab, n.u, 0.0), va, vc, znak);
        Vyp223(spolecne.hodnota(spolecne.funkce2.f223, va, vc, ab, n.u, PI), va, vc, znak);
    }

    public void P224(double va, double p, double ab, boolean znak) {
        double a, b, abm, ga;
        a = 2 * p / va;
        b = ab - a;
        abm = a + va;
        if (Math.abs(ab - abm) < PR) {
            spolecne.vyslbac(znak, a, b, Math.sqrt(a * a + b * b));
            return;
        }
        if (ab < abm) {
            spolecne.chyba("ab", "<", abm);
            return;
        }
        ga = compLib.asin(va / b);
        spolecne.vyslbac(znak, a, b, spolecne.cs(a, b, ga));
        spolecne.vyslbac(znak, a, b, spolecne.cs(a, b, PI - ga));
    }

    private void Vyp225(double al, double r, double va, boolean znak, boolean tp) {
        Spolecne.Double3 x = spolecne.abcrval(r, va, al);
        if (tp == false) spolecne.vyslbac(znak, x.a, x.c, x.b); else spolecne.vyslbac(znak, x.a, x.b, x.c);
    }

    public void P225(double va, double r, double ab, boolean znak) {
        double an, abn, abm, aln;
        if (r <= va / 2) {
            spolecne.chyba("r", "<=", va / 2);
            return;
        }
        aln = compLib.acos(va / r - 1);
        an = 2 * r * Math.sin(aln);
        abn = an + Math.sqrt(an * an / 4 + va * va);
        abm = Math.sqrt(2 * r * va);
        if (ab <= abm + PR) {
            spolecne.chyba("ab", "<=", abm);
            return;
        }
        Spolecne.Double2M m = spolecne.max1(spolecne.funkce2.f225, va, r, 0.02, 0.02, 0.0, false);
        if (Math.abs(ab - m.h) < PR) {
            Vyp225(m.u, r, va, znak, false);
            return;
        }
        if (ab > m.h) {
            spolecne.chyba("ab", ">", m.h);
            return;
        }
        Vyp225(spolecne.hodnota1(spolecne.funkce2.f225, va, r, ab, 0.0, m.u, false), r, va, znak, false);
        Spolecne.Double2M m1 = spolecne.max3(spolecne.funkce2.f225, va, r, 0.02, 0.02, 0.0, aln, true);
        if (Math.abs(m1.u - aln) < 0.1) {
            if (Math.abs(ab - abn) < PR) {
                Vyp225(aln, r, va, znak, false);
                return;
            }
            if (ab > abn) {
                Vyp225(spolecne.hodnota1(spolecne.funkce2.f225, va, r, ab, aln, m.u, false), r, va, znak, false);
                return;
            } else {
                Vyp225(spolecne.hodnota1(spolecne.funkce2.f225, va, r, ab, 0.0, aln, true), r, va, znak, true);
                return;
            }
        }
        if (Math.abs(ab - abn) < PR) Vyp225(aln, r, va, znak, false);
        if (ab > abn) Vyp225(spolecne.hodnota1(spolecne.funkce2.f225, va, r, ab, aln, m.u, false), r, va, znak, false);
        if (Math.abs(ab - m1.h) < PR) {
            Vyp225(m1.u, r, va, znak, true);
            return;
        }
        if (ab > m1.h) return;
        Vyp225(spolecne.hodnota1(spolecne.funkce2.f225, va, r, ab, 0.0, m1.u, true), r, va, znak, true);
        Spolecne.Double2M n = spolecne.min1(spolecne.funkce2.f225, va, r, m1.u + 0.02, 0.02, m1.h, true);
        if (Math.abs(ab - n.h) < PR) {
            Vyp225(n.u, r, va, znak, true);
            return;
        }
        if (ab < n.h) return;
        Vyp225(spolecne.hodnota1(spolecne.funkce2.f225, va, r, ab, n.u, m1.u, true), r, va, znak, true);
        if (ab >= abn || Math.abs(ab - abn) < PR) return;
        Vyp225(spolecne.hodnota1(spolecne.funkce2.f225, va, r, ab, n.u, aln, true), r, va, znak, true);
    }

    private void Vyp226(double e, double va, double ro, boolean znak) {
        Spolecne.Double3 x = spolecne.abcva(va, 2 * compLib.asin(ro * Math.cos(e) / (va - ro)), e);
        spolecne.vyslbac(znak, x.a, x.b, x.c);
    }

    public void P226(double va, double ro, double ab, boolean znak) {
        if (ro >= va / 2) {
            spolecne.chyba("ro", ">=", va / 2);
            return;
        }
        Spolecne.Double2M n = spolecne.min(spolecne.funkce2.f226, va, ro, 0.02, 0.02, 100000);
        if (Math.abs(ab - n.h) < PR) {
            Vyp226(n.u, va, ro, znak);
            return;
        }
        if (ab < n.h) {
            spolecne.chyba("ab", "<", n.h);
            return;
        }
        Vyp226(spolecne.hodnota(spolecne.funkce2.f226, va, ro, ab, n.u, PI / 2), va, ro, znak);
        Vyp226(spolecne.hodnota(spolecne.funkce2.f226, va, ro, ab, n.u, -PI / 2), va, ro, znak);
    }

    private void Vyp227(double e, double ga, double ta, boolean znak) {
        Spolecne.Double3 x = spolecne.abctaub(ta, PI - e, ga);
        spolecne.vyslbac(znak, x.a, x.c, x.b);
    }

    public void P227(double ga, double ta, double ab, boolean znak) {
        if (ab <= ta) {
            spolecne.chyba("ab", "<=", ta);
            return;
        }
        Spolecne.Double2M m = spolecne.max2(spolecne.funkce2.f227, ga, ta, PI - ga - 0.02, -0.02, 0.0, 0.0);
        if (m.u < PR) {
            if (ab >= 2 * ta) {
                spolecne.chyba("ab", ">=", 2 * ta);
                return;
            }
            Vyp227(spolecne.hodnota(spolecne.funkce2.f227, ga, ta, ab, PI - ga, m.u), ga, ta, znak);
            return;
        }
        if (Math.abs(ab - m.h) < PR) {
            Vyp227(m.u, ga, ta, znak);
            return;
        }
        if (ab > m.h) {
            spolecne.chyba("ab", ">", m.h);
            return;
        }
        Vyp227(spolecne.hodnota(spolecne.funkce2.f227, ga, ta, ab, PI - ga, m.u), ga, ta, znak);
        if (ab <= 2 * ta) return;
        Vyp227(spolecne.hodnota(spolecne.funkce2.f227, ga, ta, ab, 0.0, m.u), ga, ta, znak);
    }

    public void P228(double ga, double tc, double ab) {
        double a, b, c, abm, b1, tc1, be;
        if (ab <= 2 * tc) {
            spolecne.chyba("ab", "<=", 2 * tc);
        }
        abm = 2 * tc / Math.cos(ga / 2);
        if (Math.abs(ab - abm) < PR) {
            spolecne.malcux(ga, tc);
            return;
        }
        if (ab > abm) {
            spolecne.chyba("ab", ">", abm);
            return;
        }
        be = spolecne.hodnota(spolecne.funkce2.f228, ga, tc, ab, 0.0, PI / 2 - ga / 2);
        b1 = Math.sin(be) / Math.sin(be + ga);
        tc1 = 0.5 * spolecne.cs(1, b1, PI - ga);
        a = tc / tc1;
        b = ab - a;
        c = b * Math.sin(ga) / Math.sin(be);
        spolecne.vysbac(a, b, c);
    }

    public void P229(double ga, double p, double ab) {
        double a, b, c, abm;
        abm = Math.sqrt(8 * p / Math.sin(ga));
        if (Math.abs(ab - abm) < PR) {
            spolecne.malcux(ga, ab / 2 * Math.cos(ga / 2));
            return;
        }
        if (ab < abm) {
            spolecne.chyba("ab", "<", abm);
            return;
        }
        a = 2 * p / (ab * Math.sin(ga) / 2 - Math.sqrt((ab * Math.sin(ga) / 2) * (ab * Math.sin(ga) / 2) - 2 * p * Math.sin(ga)));
        b = ab - a;
        c = spolecne.cs(a, b, ga);
        spolecne.vysbac(a, b, c);
    }

    public void P230(double ga, double r, double ab) {
        double a, c, abm, al;
        c = 2 * r * Math.sin(ga);
        if (ab <= c + PR) {
            spolecne.chyba("ab", "<=", c);
            return;
        }
        abm = c / Math.sin(ga / 2);
        if (Math.abs(ab - abm) < PR) {
            spolecne.malcux(ga, ab / 2 * Math.cos(ga / 2));
            return;
        }
        if (ab > abm) {
            spolecne.chyba("ab", ">", abm);
            return;
        }
        al = spolecne.hodnota(spolecne.funkce2.f230, ga, c, ab, 0.0, PI / 2 - ga / 2);
        a = 2 * r * Math.sin(al);
        spolecne.vysbac(a, ab - a, c);
    }

    public void P231(double ga, double ro, double ab) {
        double abm = 2 * ro / Math.cos(ga / 2) * (1 + 1 / Math.sin(ga / 2));
        if (Math.abs(ab - abm) < PR) {
            spolecne.malcux(ga, ab / 2 * Math.cos(ga / 2));
            return;
        }
        if (ab < abm) {
            spolecne.chyba("ab", "<", abm);
            return;
        }
        double be = spolecne.hodnota(spolecne.funkce2.f231, ga, ro, ab, PI / 2 - ga / 2, 0.0);
        Spolecne.Double3 x = spolecne.abcro(ro, PI - be - ga, be, ga);
        spolecne.vysbac(x.a, x.b, x.c);
    }

    private void Vyp232(double e, double va, double ta, double ab, boolean znak) {
        double k = (ab - 2 * ta * Math.cos(e)) / va;
        double ga = compLib.acos((-2 + k * Math.sqrt(k * k + 3)) / (k * k + 4));
        Spolecne.Double3 x = spolecne.abctaub(ta, PI - e, ga);
        spolecne.vyslbac(znak, x.a, x.c, x.b);
    }

    public void P232(double va, double ta, double ab, boolean znak, String vyska) {
        if (ab <= ta) {
            spolecne.chyba("ab", "<=", ta);
            return;
        }
        if (va > ta) {
            spolecne.chyba(vyska, ">", ta);
            return;
        }
        if (va == ta) {
            Vyp232(PI / 2, va, ta, ab, znak);
            return;
        }
        double e = compLib.asin(va / ta);
        Vyp232(e, va, ta, ab, znak);
        Vyp232(PI - e, va, ta, ab, znak);
    }

    private void Vyp233(double ga, double va, double e, boolean znak) {
        Spolecne.Double3 x = spolecne.vaega(va, e, ga);
        spolecne.vyslbac(znak, x.a, x.b, x.c);
    }

    public void P233(double va, double tb, double ab, boolean znak, String vyska) {
        double k, abm, ga, e;
        if (va > 2 * tb) {
            spolecne.chyba(vyska, ">", 2 * tb);
            return;
        }
        e = compLib.asin(va / 2 / tb);
        if (tb < 0.6 * va) abm = 2 * tb + PR; else abm = tb * Math.cos(e) + va / 2 / Math.tan(2 * PI / 3) + va / Math.sin(2 * PI / 3);
        if (Math.abs(ab - abm) < PR) {
            Vyp233(2 * PI / 3, va, e, znak);
            return;
        }
        if (ab < abm) {
            spolecne.chyba("ab", "<", abm);
            return;
        }
        k = 2 * (ab - tb * Math.cos(e)) / va;
        ga = compLib.acos((-2 + k * Math.sqrt(k * k - 3)) / (k * k + 1));
        Vyp233(ga, va, e, znak);
        if (tb == va / 2 || Math.abs(ab - 2 * tb) < PR) return;
        if (ab > 2 * tb) {
            k = 2 * (ab + tb * Math.cos(e)) / va;
            ga = compLib.acos((-2 + k * Math.sqrt(k * k - 3)) / (k * k + 1));
            e = PI - e;
            Vyp233(ga, va, e, znak);
            return;
        }
        ga = compLib.acos((-2 - k * Math.sqrt(k * k - 3)) / (k * k + 1));
        Vyp233(ga, va, e, znak);
    }

    private void Vyp234(double e, double va, double tc, double ab, boolean znak) {
        double k, be;
        k = ab - 2 * tc * Math.cos(e);
        if (Math.abs(k * ab - va * va) < PR) be = PI / 2; else be = compLib.atan(k * va / (k * ab - va * va));
        if (be < 0.0) be = PI + be;
        Spolecne.Double3 x = spolecne.vaega(va, e, be);
        spolecne.vyslbac(znak, x.a, x.c, x.b);
    }

    public void P234(double va, double tc, double ab, boolean znak) {
        if (tc < va / 2) {
            spolecne.chyba("tc", "<", va / 2);
            return;
        }
        if (ab <= 2 * tc) {
            spolecne.chyba("ab", "<=", 2 * tc);
            return;
        }
        double e = compLib.asin(va / 2 / tc);
        Vyp234(e, va, tc, ab, znak);
        if (tc == va / 2) return;
        Vyp234(PI - e, va, tc, ab, znak);
    }

    private void Vyp235(double be, double vc, double e, boolean znak) {
        Spolecne.Double3 x = spolecne.vaega(vc, e, be);
        spolecne.vyslbac(znak, x.b, x.c, x.a);
    }

    public void P235(double vc, double ta, double ab, boolean znak) {
        if (vc > 2 * ta) {
            spolecne.chyba("vc", ">", 2 * ta);
            return;
        }
        double e = compLib.asin(vc / 2 / ta);
        Spolecne.Double2M n = spolecne.min(spolecne.funkce2.f235, vc, e, 0.02, 0.02, 100000);
        if (Math.abs(ab - n.h) < PR) {
            Vyp235(n.u, vc, e, znak);
            return;
        }
        if (ab < n.h) {
            spolecne.chyba("ab", "<", n.h);
            return;
        }
        Vyp235(spolecne.hodnota(spolecne.funkce2.f235, vc, e, ab, n.u, 0.0), vc, e, znak);
        if (ta == vc / 2) return;
        if (Math.abs(ab - 4 * ta) < PR) return;
        if (ab > 4 * ta) {
            e = PI - e;
            Vyp235(spolecne.hodnota(spolecne.funkce2.f235, vc, e, ab, e, 0.0), vc, e, znak);
            return;
        }
        Vyp235(spolecne.hodnota(spolecne.funkce2.f235, vc, e, ab, n.u, PI - e), vc, e, znak);
    }

    public void P236(double vc, double tc, double ab) {
        double a, b, c, k, m, q;
        if (tc < vc) {
            spolecne.chyba("tc", "<", vc);
            return;
        }
        if (ab <= 2 * tc) {
            spolecne.chyba("ab", "<=", 2 * tc);
            return;
        }
        if (tc == vc) {
            spolecne.malcux(2 * compLib.acos(2 * vc / ab), vc);
            return;
        }
        k = Math.sqrt(tc * tc - vc * vc);
        m = ab * ab - 4 * k * k;
        q = (0.25 * (ab * ab + 4 * k * k) * (ab * ab + 4 * k * k) - 4 * k * k * (ab * ab - vc * vc)) / m;
        b = ab / 2 - Math.sqrt(ab * ab / 4 - q);
        a = ab - b;
        c = 2 * (Math.sqrt(a * a - vc * vc) - k);
        spolecne.vysbac(a, b, c);
    }

    public void P237(double vc, double p, double ab) {
        double b, c, abm, m, q;
        c = 2 * p / vc;
        abm = 2 * Math.sqrt(c * c / 4 + vc * vc);
        if (Math.abs(ab - abm) < PR) {
            spolecne.vysledek(abm / 2, abm / 2, c);
            return;
        }
        if (ab < abm) {
            spolecne.chyba("ab", "<", abm);
            return;
        }
        m = c * c + 2 * vc * vc - ab * ab;
        q = (m * m / 4 + ab * ab * vc * vc - vc * vc * vc * vc) / (ab * ab - c * c);
        b = ab / 2 - Math.sqrt(ab * ab / 4 - q);
        spolecne.vysbac(ab - b, b, c);
    }

    private void Vyp238(double ga, double r, double vc) {
        Spolecne.Double3 x = spolecne.abcrval(r, vc, ga);
        spolecne.vysbac(x.b, x.c, x.a);
    }

    public void P238(double vc, double r, double ab) {
        double abm, abn, gam, gan;
        if (vc >= 2 * r) {
            spolecne.chyba("vc", ">=", 2 * r);
            return;
        }
        abn = 2 * Math.sqrt(2 * r * vc);
        if (ab <= abn) {
            spolecne.chyba("ab", "<=", abn);
            return;
        }
        gan = compLib.acos(vc / r - 1);
        gam = compLib.acos(vc / 2 / r);
        abm = 2 * r + vc;
        if (Math.abs(ab - abm) < PR) {
            Vyp238(gam, r, vc);
            return;
        }
        if (ab > abm) {
            spolecne.chyba("ab", ">", abm);
            return;
        }
        Vyp238(spolecne.hodnota(spolecne.funkce2.f238, vc, r, ab, 0.0, gam), r, vc);
        Vyp238(spolecne.hodnota(spolecne.funkce2.f238, vc, r, ab, gan, gam), r, vc);
    }

    public void P239(double vc, double ro, double ab) {
        double abm, gam, e;
        if (ro >= vc / 2) {
            spolecne.chyba("ro", ">=", vc / 2);
            return;
        }
        gam = 2 * compLib.asin(ro / (vc - ro));
        abm = 2 * vc / Math.cos(gam / 2);
        if (Math.abs(ab - abm) < PR) {
            spolecne.malcux(2 * compLib.acos(2 * vc / ab), vc);
            return;
        }
        if (ab < abm) {
            spolecne.chyba("ab", "<", abm);
            return;
        }
        e = spolecne.hodnota(spolecne.funkce2.f239, vc, ro, ab, 0.0, PI / 2);
        Spolecne.Double3 x = spolecne.abcva(vc, 2 * compLib.asin(ro * Math.cos(e) / (vc - ro)), e);
        spolecne.vysbac(x.b, x.c, x.a);
    }

    public void P240(double ta, double tb, double ab) {
        double a, b, c, abm;
        if (ab >= 2 * (ta + tb)) {
            spolecne.chyba("ab", ">=", 2 * ta + 2 * tb);
            return;
        }
        abm = 2 * (Math.abs(2 * tb - ta) + Math.abs(2 * ta - tb)) / 3;
        if (ab <= abm) {
            spolecne.chyba("ab", "<=", abm);
            return;
        }
        a = (3 * ab * ab - 4 * ta * ta + 4 * tb * tb) / 6 / ab;
        b = (3 * ab * ab + 4 * ta * ta - 4 * tb * tb) / 6 / ab;
        c = spolecne.strtx(a, b, ta);
        spolecne.vysledek(a, b, c);
    }

    private void Vyp241(double e, double ta, double tc, boolean znak) {
        Spolecne.Double3 x = spolecne.abctatb(ta, tc, e);
        spolecne.vyslbac(znak, x.a, x.c, x.b);
    }

    public void P241(double ta, double tc, double ab, boolean znak) {
        double abm, abmi, abn, em;
        abmi = 2 * (ta + tc + Math.abs(2 * tc - ta)) / 3;
        if (ab <= abmi) {
            spolecne.chyba("ab", "<=", abmi);
            return;
        }
        abn = 2 * (2 * tc + ta + Math.abs(ta - tc)) / 3;
        if (ta >= 4 * tc) {
            if (ab >= abn) {
                spolecne.chyba("ab", ">=", abn);
                return;
            }
            Vyp241(spolecne.hodnota(spolecne.funkce2.f241, ta, tc, ab, 0.0, PI), ta, tc, znak);
            return;
        }
        abm = Math.sqrt(2 * ta * ta + 4 * tc * tc);
        em = compLib.acos(-ta / 4 / tc);
        if (Math.abs(ab - abm) < PR) {
            Vyp241(em, ta, tc, znak);
            return;
        }
        if (ab > abm) {
            spolecne.chyba("ab", ">", abm);
            return;
        }
        Vyp241(spolecne.hodnota(spolecne.funkce2.f241, ta, tc, ab, 0.0, em), ta, tc, znak);
        if (ab <= abn) return;
        Vyp241(spolecne.hodnota(spolecne.funkce2.f241, ta, tc, ab, PI, em), ta, tc, znak);
    }

    private void Vyp242(double e, double ta, double p, boolean znak) {
        Spolecne.Double3 x = spolecne.abctap(ta, p, e);
        spolecne.vyslbac(znak, x.a, x.b, x.c);
    }

    public void P242(double ta, double p, double ab, boolean znak) {
        Spolecne.Double2M n = spolecne.min(spolecne.funkce2.f242, ta, p, 0.02, 0.02, 100000);
        if (Math.abs(ab - n.h) < PR) {
            Vyp242(n.u, ta, p, znak);
            return;
        }
        if (ab < n.h) {
            spolecne.chyba("ab", "<", n.h);
            return;
        }
        Vyp242(spolecne.hodnota(spolecne.funkce2.f242, ta, p, ab, n.u, 0.0), ta, p, znak);
        Vyp242(spolecne.hodnota(spolecne.funkce2.f242, ta, p, ab, n.u, PI), ta, p, znak);
    }

    private void Vyp243(double al, double ta, double r, boolean znak, boolean tp) {
        double va = spolecne.taral(ta, r, al);
        Spolecne.Double3 x = spolecne.abcrval(r, va, al);
        if (tp == true) spolecne.vyslbac(znak, x.a, x.b, x.c); else spolecne.vyslbac(znak, x.a, x.c, x.b);
    }

    public void P243(double ta, double r, double ab, boolean znak) {
        double krok, abn, alm, aln;
        if (r <= ta / 2) {
            spolecne.chyba("r", "<=", ta / 2);
            return;
        }
        if (ab <= ta) {
            spolecne.chyba("ab", "<=", ta);
            return;
        }
        aln = compLib.acos(ta / r - 1);
        abn = 2 * ta * Math.tan(aln / 2) + ta / Math.cos(aln / 2);
        if (ta < r) {
            alm = compLib.asin(ta / r);
            Spolecne.Double2M m1 = spolecne.max1(spolecne.funkce2.f243, ta, r, PI - alm, -0.002, 0.0, false);
            if (Math.abs(ab - m1.h) < PR) {
                Vyp243(m1.u, ta, r, znak, false);
                return;
            }
            if (ab > m1.h) {
                spolecne.chyba("ab", ">", m1.h);
                return;
            }
            if (ab > 4 * ta) Vyp243(spolecne.hodnota1(spolecne.funkce2.f243, ta, r, ab, PI - alm, m1.u, false), ta, r, znak, false);
            if (Math.abs(ab - abn) < PR) {
                spolecne.malux(aln, ta);
                return;
            }
            if (ab > abn) {
                Vyp243(spolecne.hodnota1(spolecne.funkce2.f243, ta, r, ab, 0.0, alm, false), ta, r, znak, false);
                return;
            }
            if (ab > 2 * ta) Vyp243(spolecne.hodnota1(spolecne.funkce2.f243, ta, r, ab, PI - alm, aln, true), ta, r, znak, true);
        }
        if (ta <= r) {
            alm = compLib.asin(ta / r);
            if (ab >= 4 * ta) return;
            Vyp243(spolecne.hodnota1(spolecne.funkce2.f243, ta, r, ab, 0.0, alm, false), ta, r, znak, false);
            if (ta == r && ab > 2 * ta) {
                if (znak == true) spolecne.vysledek(2 * r, ab - 2 * r, Math.sqrt(4 * r * r - (ab - 2 * r) * (ab - 2 * r))); else spolecne.vysledek(ab - 2 * r, 2 * r, Math.sqrt(4 * r * r - (ab - 2 * r) * (ab - 2 * r)));
            }
            Spolecne.Double2M m2 = spolecne.max3(spolecne.funkce2.f243, ta, r, 0.02, 0.02, 0.0, alm, true);
            if (Math.abs(m2.u - alm) < PR) {
                if (ab >= 2 * ta) return;
                Vyp243(spolecne.hodnota1(spolecne.funkce2.f243, ta, r, ab, 0.0, alm, true), ta, r, znak, true);
                return;
            }
            if (Math.abs(ab - m2.h) < PR) {
                Vyp243(m2.u, ta, r, znak, true);
                return;
            }
            if (ab > m2.h) return;
            Vyp243(spolecne.hodnota1(spolecne.funkce2.f243, ta, r, ab, 0.0, m2.u, true), ta, r, znak, true);
            if (ab > 2 * ta) Vyp243(spolecne.hodnota1(spolecne.funkce2.f243, ta, r, ab, alm, m2.u, true), ta, r, znak, true);
        } else {
            if (ta > 1.825 * r) krok = 0.0001; else krok = 0.02;
            Spolecne.Double2M m3 = spolecne.max1(spolecne.funkce2.f243, ta, r, 0.02, krok, 0.0, false);
            if (Math.abs(ab - m3.h) < PR) {
                Vyp243(m3.u, ta, r, znak, false);
                return;
            }
            if (ab > m3.h) {
                spolecne.chyba("ab", ">", m3.h);
                return;
            }
            Vyp243(spolecne.hodnota1(spolecne.funkce2.f243, ta, r, ab, 0.0, m3.u, false), ta, r, znak, false);
            if (Math.abs(ab - abn) < PR) {
                spolecne.malux(aln, ta);
                return;
            }
            if (ab > abn) Vyp243(spolecne.hodnota1(spolecne.funkce2.f243, ta, r, ab, aln, m3.u, false), ta, r, znak, false); else Vyp243(spolecne.hodnota1(spolecne.funkce2.f243, ta, r, ab, 0.0, aln, true), ta, r, znak, true);
        }
    }

    private void Vyp244(double o, double ta, double ro, boolean znak) {
        Spolecne.Double3 z = spolecne.taroo(ta, ro, o);
        Spolecne.Double3 x = spolecne.abcva(z.a, z.b, z.c);
        spolecne.vyslbac(znak, x.a, x.b, x.c);
    }

    public void P244(double ta, double ro, double ab, boolean znak) {
        if (ro >= ta / 2) {
            spolecne.chyba("ro", ">=", ta / 2);
            return;
        }
        double om = compLib.acos(2 * ro / ta);
        Spolecne.Double2M n = spolecne.min(spolecne.funkce2.f244, ta, ro, 0.02, 0.02, 100000);
        if (Math.abs(ab - n.h) < PR) {
            Vyp244(n.u, ta, ro, znak);
            return;
        }
        if (ab < n.h) {
            spolecne.chyba("ab", "<", n.h);
            return;
        }
        Vyp244(spolecne.hodnota(spolecne.funkce2.f244, ta, ro, ab, n.u, om), ta, ro, znak);
        Vyp244(spolecne.hodnota(spolecne.funkce2.f244, ta, ro, ab, n.u, -om), ta, ro, znak);
    }

    public void P245(double tc, double p, double ab) {
        double abm = 2 * Math.sqrt(p * p / tc / tc + tc * tc);
        if (Math.abs(ab - abm) < PR) {
            spolecne.vysledek(abm / 2, abm / 2, 2 * p / tc);
            return;
        }
        if (ab < abm) {
            spolecne.chyba("ab", "<", abm);
            return;
        }
        double e = spolecne.hodnota(spolecne.funkce2.f245, tc, p, ab, PI / 2, 0.0);
        Spolecne.Double3 x = spolecne.abctap(tc, p, e);
        spolecne.vysbac(x.b, x.c, x.a);
    }

    private void Vyp246(double ga, double tc, double r) {
        double vc = spolecne.taral(tc, r, ga);
        Spolecne.Double3 x = spolecne.abcrval(r, vc, ga);
        spolecne.vysbac(x.b, x.c, x.a);
    }

    public void P246(double tc, double r, double ab) {
        if (tc >= 2 * r) {
            spolecne.chyba("tc", ">=", 2 * r);
            return;
        }
        if (ab <= 2 * tc) {
            spolecne.chyba("ab", "<=", 2 * tc);
            return;
        }
        double gan = compLib.acos(tc / r - 1);
        double abn = 2 * Math.sqrt(tc * tc + (r * Math.sin(gan)) * (r * Math.sin(gan)));
        if (Math.abs(ab - abn) < PR) {
            spolecne.malcux(gan, tc);
            return;
        }
        if (ab > abn) {
            spolecne.chyba("ab", ">", abn);
            return;
        }
        if (tc > r) {
            Vyp246(spolecne.hodnota(spolecne.funkce2.f246, tc, r, ab, 0.0, gan), tc, r);
            return;
        }
        if (tc == r) {
            double a = ab / 2 - Math.sqrt(2 * r * r - ab * ab / 4);
            spolecne.vysbac(a, ab - a, 2 * r);
        }
        if (tc < r) {
            Vyp246(spolecne.hodnota(spolecne.funkce2.f246, tc, r, ab, PI - compLib.asin(tc / r), gan), tc, r);
        }
        Spolecne.Double2M m = spolecne.max(spolecne.funkce2.f246, tc, r, 0.02, 0.02, 0.0);
        if (Math.abs(ab - m.h) < PR) {
            Vyp246(m.u, tc, r);
            return;
        }
        if (ab > m.h) return;
        Vyp246(spolecne.hodnota(spolecne.funkce2.f246, tc, r, ab, 0.0, m.u), tc, r);
        Vyp246(spolecne.hodnota(spolecne.funkce2.f246, tc, r, ab, compLib.asin(tc / r), m.u), tc, r);
    }

    public void P247(double tc, double ro, double ab) {
        if (ro >= tc / 2) {
            spolecne.chyba("ro", ">=", tc / 2);
            return;
        }
        double abm = 2 * (tc - ro) / Math.sqrt(1 - 2 * ro / tc);
        if (Math.abs(ab - abm) < PR) {
            spolecne.macavc(abm / 2, tc);
            return;
        }
        if (ab < abm) {
            spolecne.chyba("ab", "<", abm);
            return;
        }
        double om = compLib.acos(2 * ro / tc);
        double o = spolecne.hodnota(spolecne.funkce2.f247, tc, ro, ab, 0.0, om);
        Spolecne.Double3 z = spolecne.taroo(tc, ro, o);
        Spolecne.Double3 x = spolecne.abcva(z.a, z.b, z.c);
        spolecne.vysbac(x.b, x.c, x.a);
    }

    private void Vyp248(double ga, double p, double r) {
        Spolecne.Double3 x = spolecne.abcrval(r, p / r / Math.sin(ga), ga);
        spolecne.vysbac(x.b, x.c, x.a);
    }

    public void P248(double p, double r, double ab) {
        double pm = 1.299038 * r * r;
        if (p > pm) {
            spolecne.chyba("p", ">", pm);
            return;
        }
        Spolecne.Double2M z = spolecne.mez1(p, r, 0.02, 0.02);
        double abma = 2 * z.h / Math.cos(z.u / 2);
        if (Math.abs(ab - abma) < PR) {
            spolecne.macavc(abma / 2, z.h);
            return;
        }
        if (ab > abma) {
            spolecne.chyba("ab", ">", abma);
            return;
        }
        Spolecne.Double2M z1 = spolecne.mez1(p, r, PI - 0.02, -0.02);
        double abmi = 2 * z1.h / Math.cos(z1.u / 2);
        if (Math.abs(ab - abmi) < PR) {
            spolecne.macavc(abmi / 2, z1.h);
            return;
        }
        if (ab < abmi) {
            spolecne.chyba("ab", "<", abmi);
            return;
        }
        Spolecne.Double2M n = spolecne.min2(spolecne.funkce2.f248, p, r, z.u + 0.02, 0.02, 5 * r, z1.u);
        if (Math.abs(n.u - z1.u) < PR) {
            Vyp248(spolecne.hodnota(spolecne.funkce2.f248, p, r, ab, z1.u, z.u), p, r);
            return;
        }
        if (Math.abs(ab - n.h) < PR) Vyp248(n.u, p, r);
        if (ab > n.h) Vyp248(spolecne.hodnota(spolecne.funkce2.f248, p, r, ab, n.u, z.u), p, r);
        Spolecne.Double2M m = spolecne.max(spolecne.funkce2.f248, p, r, n.u, 0.02, 0.0);
        if (Math.abs(ab - m.h) < PR) {
            Vyp248(m.u, p, r);
            return;
        }
        if (ab > m.h) return;
        Vyp248(spolecne.hodnota(spolecne.funkce2.f248, p, r, ab, z1.u, m.u), p, r);
        if (ab < n.h) return;
        Vyp248(spolecne.hodnota(spolecne.funkce2.f248, p, r, ab, n.u, m.u), p, r);
    }

    public void P249(double p, double ro, double ab) {
        double a, c, vc, s, rom, abm;
        s = p / ro;
        rom = Math.sqrt(p / 3 / Math.tan(PI / 3));
        abm = 4 * ro * Math.tan(PI / 3);
        if (Math.abs(ro - rom) < PR) {
            if (ab < abm - PR || ab > abm + PR) {
                spolecne.chyba("ab", "!=", abm);
                return;
            }
            spolecne.vysledek(abm / 2, abm / 2, abm / 2);
            return;
        }
        if (ro > rom) {
            spolecne.chyba("ro", ">", rom);
            return;
        }
        Spolecne.Double2M z = spolecne.mez2(p, s, s + 0.1, 0.1);
        if (Math.abs(ab - z.u) < PR) {
            spolecne.macavc(z.u / 2, z.h);
            return;
        }
        if (ab < z.u) {
            spolecne.chyba("ab", "<", z.u);
            return;
        }
        Spolecne.Double2M z1 = spolecne.mez2(p, s, 2 * s - 2 * ro - 0.1, -0.1);
        if (Math.abs(ab - z1.u) < PR) {
            spolecne.macavc(z1.u / 2, z1.h);
            return;
        }
        if (ab > z1.u) {
            spolecne.chyba("ab", ">", z1.u);
            return;
        }
        c = 2 * s - ab;
        vc = 2 * p / c;
        a = Math.sqrt(vc * vc + (c / 2 + ab * Math.sqrt(0.25 - vc * vc / (ab * ab - c * c))) * (c / 2 + ab * Math.sqrt(0.25 - vc * vc / (ab * ab - c * c))));
        spolecne.vysbac(a, ab - a, c);
    }

    private void Vyp250(double ga, double r, double ro, double ab) {
        double a = 2 * r * Math.sin(spolecne.roral(r, ro, ga));
        spolecne.vysbac(a, ab - a, 2 * r * Math.sin(ga));
    }

    public void P250(double r, double ro, double ab) {
        double a, abr, abma, abmi, gama, gami;
        abr = 3.464102 * r;
        if (ro == 0.5 * r) {
            if (ab > abr + PR || ab < abr - PR) {
                spolecne.chyba("ab", "!=", abr);
                return;
            }
            a = 2 * r * Math.sin(PI / 3);
            spolecne.vysledek(a, a, a);
            return;
        }
        if (ro > 0.5 * r) {
            spolecne.chyba("ro", ">", r / 2);
            return;
        }
        gama = compLib.acos(ro / r + Math.sqrt(1 - 2 * ro / r));
        abma = 2 * r * Math.sin(gama) / Math.sin(gama / 2);
        if (Math.abs(ab - abma) < PR) {
            spolecne.malcux(gama, abma / 2 * Math.cos(gama / 2));
            return;
        }
        if (ab > abma) {
            spolecne.chyba("ab", ">", abma);
            return;
        }
        gami = compLib.acos(ro / r - Math.sqrt(1 - 2 * ro / r));
        abmi = 2 * r * Math.sin(gami) / Math.sin(gami / 2);
        if (Math.abs(ab - abmi) < PR) {
            spolecne.malcux(gami, abmi / 2 * Math.cos(gami / 2));
            return;
        }
        if (ab < abmi) {
            spolecne.chyba("ab", "<", abmi);
            return;
        }
        Spolecne.Double2M n = spolecne.min2(spolecne.funkce2.f250, r, ro, gama + 0.02, 0.02, 5 * r, gami);
        if (Math.abs(n.u - gami) < PR) {
            Vyp250(spolecne.hodnota(spolecne.funkce2.f250, r, ro, ab, gami, gama), r, ro, ab);
            return;
        }
        if (Math.abs(ab - n.h) < PR) Vyp250(n.u, r, ro, ab);
        if (ab > n.h) Vyp250(spolecne.hodnota(spolecne.funkce2.f250, r, ro, ab, n.u, gama), r, ro, ab);
        Spolecne.Double2M m = spolecne.max(spolecne.funkce2.f250, r, ro, n.u, 0.02, 0.0);
        if (Math.abs(ab - m.h) < PR) {
            Vyp250(m.u, r, ro, ab);
            return;
        }
        if (ab > m.h) return;
        Vyp250(spolecne.hodnota(spolecne.funkce2.f250, r, ro, ab, gami, m.u), r, ro, ab);
        if (ab < n.h) return;
        Vyp250(spolecne.hodnota(spolecne.funkce2.f250, r, ro, ab, n.u, m.u), r, ro, ab);
    }

    public void P255(double c, double ua, double ab, boolean znak) {
        double abmi, alm, al, be;
        if (c <= ua / 2) {
            spolecne.chyba("c", "<=", ua / 2);
            return;
        }
        alm = 2 * compLib.acos(ua / 2 / c);
        if (ua <= c) abmi = c; else abmi = 2 * c * ua / (2 * c - ua) - c;
        if (ab <= abmi) {
            spolecne.chyba("ab", "<=", abmi);
            return;
        }
        al = spolecne.hodnota(spolecne.funkce2.f255, c, ua, ab, 0.0, alm);
        be = spolecne.gaub(c, ua, al);
        Spolecne.Double2 x = spolecne.bca(c, al + be, al, be);
        spolecne.vyslbac(znak, x.b, x.c, c);
    }

    public void P256(double c, double uc, double ab) {
        double abm, abma, ga, gam;
        gam = 2 * compLib.atan(c / 2 / uc);
        abm = 2 * uc / Math.cos(gam / 2);
        abma = spolecne.funkce2.f256.f(c, uc, PR);
        if (ab >= abma - PR) {
            spolecne.chyba("ab", ">=", abma);
            return;
        }
        if (Math.abs(ab - abm) < PR) {
            spolecne.vysledek(abm / 2, abm / 2, c);
            return;
        }
        if (ab < abm) {
            spolecne.chyba("ab", "<", abm);
            return;
        }
        ga = spolecne.hodnota(spolecne.funkce2.f256, c, uc, ab, gam, 0.0);
        Spolecne.Double3 x = spolecne.abcua(uc, ga, spolecne.eta(c, uc, ga));
        spolecne.vysbac(x.b, x.c, x.a);
    }

    private void Vyp257(double e, double al, double ua, boolean znak) {
        Spolecne.Double3 x = spolecne.abcua(ua, al, e);
        spolecne.vyslbac(znak, x.a, x.b, x.c);
    }

    public void P257(double al, double ua, double ab, boolean znak) {
        Spolecne.Double2M n = spolecne.min(spolecne.funkce2.f257, al, ua, 0.02, 0.02, 100000);
        if (Math.abs(ab - n.h) < PR) {
            Vyp257(n.u, al, ua, znak);
            return;
        }
        if (ab < n.h) {
            spolecne.chyba("ab", "<", n.h);
            return;
        }
        Vyp257(spolecne.hodnota(spolecne.funkce2.f257, al, ua, ab, n.u, PI / 2 - al / 2), al, ua, znak);
        Vyp257(spolecne.hodnota(spolecne.funkce2.f257, al, ua, ab, n.u, al / 2 - PI / 2), al, ua, znak);
    }

    public void P258(double al, double ub, double ab, boolean znak) {
        if (ab <= ub) {
            spolecne.chyba("ab", "<=", ub);
            return;
        }
        double be = spolecne.hodnota(spolecne.funkce2.f258, al, ub, ab, 0.0, PI - al);
        Spolecne.Double3 x = spolecne.abcub(ub, al, be);
        spolecne.vyslbac(znak, x.a, x.b, x.c);
    }

    public void P259(double al, double uc, double ab, boolean znak) {
        if (ab <= 2 * uc) {
            spolecne.chyba("ab", "<=", 2 * uc);
            return;
        }
        double ga = spolecne.hodnota(spolecne.funkce2.f259, al, uc, ab, 0.0, PI - al);
        Spolecne.Double3 x = spolecne.abcub(uc, al, ga);
        spolecne.vyslbac(znak, x.a, x.c, x.b);
    }

    public void P262(double ga, double ua, double ab, boolean znak) {
        if (ab <= ua) {
            spolecne.chyba("ab", "<=", ua);
            return;
        }
        double al = spolecne.hodnota(spolecne.funkce2.f262, ga, ua, ab, 0.0, PI - ga);
        Spolecne.Double3 x = spolecne.abcual(ua, al, ga);
        spolecne.vyslbac(znak, x.a, x.c, x.b);
    }

    public void P264(double ga, double uc, double ab) {
        double abm = 2 * uc / Math.cos(ga / 2);
        if (Math.abs(ab - abm) < PR) {
            spolecne.malcux(ga, uc);
            return;
        }
        if (ab < abm) {
            spolecne.chyba("ab", "<", abm);
            return;
        }
        double e = spolecne.hodnota(spolecne.funkce2.f264, ga, uc, ab, 0.0, PI / 2 - ga / 2);
        Spolecne.Double3 x = spolecne.abcua(uc, ga, e);
        spolecne.vysbac(x.b, x.c, x.a);
    }

    private void Vyp265(double e, double ua, double ab, boolean znak) {
        double al = spolecne.hodnota(spolecne.funkce2.f265, ua, e, ab, 0.0, PI - Math.abs(2 * e));
        Spolecne.Double3 x = spolecne.abcua(ua, al, e);
        spolecne.vyslbac(znak, x.a, x.b, x.c);
    }

    public void P265(double va, double ua, double ab, boolean znak, String vyska) {
        if (va > ua) {
            spolecne.chyba(vyska, ">", ua);
            return;
        }
        if (ab <= ua) {
            spolecne.chyba("ab", "<=", ua);
            return;
        }
        double e = compLib.acos(va / ua);
        Vyp265(e, ua, ab, znak);
        if (ua == va) return;
        Vyp265(-e, ua, ab, znak);
    }

    private void Vyp266(double be, double va, double ub, boolean znak) {
        Spolecne.Double3 x = spolecne.abcubva(va, ub, be);
        spolecne.vyslbac(znak, x.a, x.b, x.c);
    }

    public void P266(double va, double ub, double ab, boolean znak) {
        double bem;
        if (ub <= va) bem = PI; else bem = 2 * compLib.asin(va / ub);
        Spolecne.Double2M n = spolecne.min(spolecne.funkce2.f266, va, ub, 0.05, 0.02, 100000);
        if (Math.abs(ab - n.h) < PR) {
            Vyp266(n.u, va, ub, znak);
            return;
        }
        if (ab < n.h) {
            spolecne.chyba("ab", "<", n.h);
            return;
        }
        Vyp266(spolecne.hodnota(spolecne.funkce2.f266, va, ub, ab, n.u, bem), va, ub, znak);
        Vyp266(spolecne.hodnota(spolecne.funkce2.f266, va, ub, ab, n.u, 0.0), va, ub, znak);
    }

    private void Vyp267(double ga, double va, double uc, boolean znak) {
        Spolecne.Double3 x = spolecne.abcubva(va, uc, ga);
        spolecne.vyslbac(znak, x.a, x.c, x.b);
    }

    public void P267(double va, double uc, double ab, boolean znak) {
        double gam;
        if (uc <= va) gam = PI; else gam = 2 * compLib.asin(va / uc);
        Spolecne.Double2M n = spolecne.min(spolecne.funkce2.f267, va, uc, 0.05, 0.02, 100000);
        if (Math.abs(ab - n.h) < PR) {
            Vyp267(n.u, va, uc, znak);
            return;
        }
        if (ab < n.h) {
            spolecne.chyba("ab", "<", n.h);
            return;
        }
        Vyp267(spolecne.hodnota(spolecne.funkce2.f267, va, uc, ab, n.u, gam), va, uc, znak);
        Vyp267(spolecne.hodnota(spolecne.funkce2.f267, va, uc, ab, n.u, 0.0), va, uc, znak);
    }

    private void Vyp271(double al, double vc, double ua, boolean znak) {
        Spolecne.Double3 x = spolecne.abcubva(vc, ua, al);
        spolecne.vyslbac(znak, x.b, x.c, x.a);
    }

    public void P271(double vc, double ua, double ab, boolean znak) {
        double alm;
        if (ua <= vc) alm = PI; else alm = 2 * compLib.asin(vc / ua);
        Spolecne.Double2M n = spolecne.min(spolecne.funkce2.f271, vc, ua, 0.05, 0.02, 100000);
        if (Math.abs(ab - n.h) < PR) {
            Vyp271(n.u, vc, ua, znak);
            return;
        }
        if (ab < n.h) {
            spolecne.chyba("ab", "<", n.h);
            return;
        }
        Vyp271(spolecne.hodnota(spolecne.funkce2.f271, vc, ua, ab, n.u, alm), vc, ua, znak);
        Vyp271(spolecne.hodnota(spolecne.funkce2.f271, vc, ua, ab, n.u, 0.0), vc, ua, znak);
    }

    public void P273(double vc, double uc, double ab) {
        if (uc < vc) {
            spolecne.chyba("uc", "<", vc);
            return;
        }
        if (ab <= 2 * uc) {
            spolecne.chyba("ab", "<=", 2 * uc);
            return;
        }
        if (uc == vc) {
            spolecne.macavc(ab / 2, vc);
            return;
        }
        double e = compLib.acos(vc / uc);
        double ga = spolecne.hodnota(spolecne.funkce2.f273, e, uc, ab, 0.0, PI - Math.abs(2 * e));
        Spolecne.Double3 x = spolecne.abcua(uc, ga, e);
        spolecne.vysbac(x.b, x.c, x.a);
    }

    private void Vyp274(double e, double ta, double ua, boolean znak) {
        Spolecne.Double3 x = spolecne.abcua(ua, spolecne.altaua(ta, ua, e), e);
        spolecne.vyslbac(znak, x.a, x.b, x.c);
    }

    public void P274(double ta, double ua, double ab, boolean znak, String tez) {
        double abm1, abm2, a, b;
        if (ta < ua) {
            spolecne.chyba(tez, "<", ua);
            return;
        }
        if (ta == ua) {
            if (ab <= ta) {
                spolecne.chyba("ab", "<=", ta);
                return;
            }
            a = (2 * ab - Math.sqrt(ab * ab + 3 * ta * ta)) / 1.5;
            b = Math.sqrt(ta * ta + a * a / 4);
            spolecne.vyslbac(znak, a, b, b);
            return;
        }
        abm1 = spolecne.funkce2.f274.f(ta, ua, PI / 2 - 0.0001);
        if (ab <= abm1) {
            spolecne.chyba("ab", "<=", abm1);
            return;
        }
        Vyp274(spolecne.hodnota(spolecne.funkce2.f274, ta, ua, ab, PI / 2, 0.0), ta, ua, znak);
        abm2 = spolecne.funkce2.f274.f(ta, ua, -PI / 2 + 0.0001);
        if (ab <= abm2) return;
        Vyp274(spolecne.hodnota(spolecne.funkce2.f274, ta, ua, ab, -PI / 2, 0.0), ta, ua, znak);
    }

    public void P275(double ta, double ub, double ab, boolean znak) {
        double abmi, abma, be, e;
        abmi = spolecne.funkce2.f275a.f(ta, ub, PR);
        if (ab <= abmi) {
            spolecne.chyba("ab", "<=", abmi);
            return;
        }
        abma = spolecne.funkce2.f275a.f(ta, ub, PI - PR);
        if (ab >= abma) {
            spolecne.chyba("ab", ">=", abma);
            return;
        }
        e = spolecne.hodnota(spolecne.funkce2.f275a, ta, ub, ab, 0.0, PI);
        be = spolecne.hodnota(spolecne.funkce2.f275, ta, e, ub, e, 0.0);
        Spolecne.Double3 x = spolecne.abctaub(ta, e, be);
        spolecne.vyslbac(znak, x.a, x.b, x.c);
    }

    private void Vyp276(double e, double ta, double uc, boolean znak) {
        double ga = spolecne.hodnota(spolecne.funkce2.f276, ta, e, uc, e, 0.0);
        Spolecne.Double3 x = spolecne.abctaub(ta, e, ga);
        spolecne.vyslbac(znak, x.a, x.c, x.b);
    }

    public void P276(double ta, double uc, double ab, boolean znak) {
        double abma, abp, m;
        abma = spolecne.funkce2.f276a.f(ta, uc, PI - PR);
        abp = spolecne.funkce2.f276a.f(ta, uc, PR);
        if (ab >= abma) {
            spolecne.chyba("ab", ">=", abma);
            return;
        }
        m = uc / ta;
        if (m <= 0.71375) {
            Spolecne.Double2M n = spolecne.min2(spolecne.funkce2.f276a, ta, uc, PI - 0.1, -0.02, 10000, 0.01);
            if (Math.abs(ab - n.h) < 0.1 * PR) {
                Vyp276(n.u, ta, uc, znak);
                return;
            }
            if (ab < n.h) {
                spolecne.chyba("ab", "<", n.h);
                return;
            }
            Vyp276(spolecne.hodnota(spolecne.funkce2.f276a, ta, uc, ab, n.u, PI), ta, uc, znak);
            if (ab > abp) return;
            Vyp276(spolecne.hodnota(spolecne.funkce2.f276a, ta, uc, ab, n.u, 0.0), ta, uc, znak);
            return;
        }
        if (ab <= abp) {
            spolecne.chyba("ab", "<=", abp);
            return;
        }
        Vyp276(spolecne.hodnota(spolecne.funkce2.f276a, ta, uc, ab, 0.0, PI), ta, uc, znak);
        return;
    }

    public void P280(double tc, double ua, double ab, boolean znak) {
        double abmi, abma, al, e;
        abmi = spolecne.funkce2.f280a.f(tc, ua, PR);
        if (ab <= abmi) {
            spolecne.chyba("ab", "<=", abmi);
            return;
        }
        abma = spolecne.funkce2.f280a.f(tc, ua, PI - PR);
        if (ab >= abma) {
            spolecne.chyba("ab", ">=", abma);
            return;
        }
        e = spolecne.hodnota(spolecne.funkce2.f280a, tc, ua, ab, 0.0, PI);
        al = spolecne.hodnota(spolecne.funkce2.f280, tc, e, ua, e, 0.0);
        Spolecne.Double3 x = spolecne.abctaub(tc, e, al);
        spolecne.vyslbac(znak, x.b, x.c, x.a);
    }

    public void P281(double tc, double uc, double ab) {
        if (tc < uc) {
            spolecne.chyba("tc", "<", uc);
            return;
        }
        if (ab <= 2 * tc) {
            spolecne.chyba("ab", "<=", 2 * tc);
            return;
        }
        if (tc == uc) {
            spolecne.macavc(ab / 2, tc);
            return;
        }
        double e = spolecne.hodnota(spolecne.funkce2.f281, tc, uc, ab, PI / 2, 0.0);
        Spolecne.Double3 x = spolecne.abcua(uc, spolecne.altaua(tc, uc, e), e);
        spolecne.vysbac(x.b, x.c, x.a);
    }

    public void P282(double ua, double ub, double ab) {
        double abmi, al, e, em;
        abmi = spolecne.funkce2.f282a.f(ua, ub, PI / 2 - PR);
        if (ab <= abmi) {
            spolecne.chyba("ab", "<=", abmi);
            return;
        }
        em = compLib.atan(ua / ub);
        e = spolecne.hodnota(spolecne.funkce2.f282a, ua, ub, ab, PI / 2, -em);
        al = spolecne.hodnota(spolecne.funkce2.f282, ua, e, ub, 0, PI - 2 * Math.abs(e));
        Spolecne.Double3 x = spolecne.abcua(ua, al, e);
        spolecne.vysledek(x.a, x.b, x.c);
    }

    private void Vyp283(double e, double ua, double uc, boolean znak) {
        double al = spolecne.hodnota(spolecne.funkce2.f283, ua, e, uc, 0.0, PI - Math.abs(2 * e));
        Spolecne.Double3 x = spolecne.abcua(ua, al, e);
        spolecne.vyslbac(znak, x.a, x.b, x.c);
    }

    public void P283(double ua, double uc, double ab, boolean znak) {
        double em = compLib.atan(ua / uc);
        Spolecne.Double2M n = spolecne.min2(spolecne.funkce2.f283a, ua, uc, -PI / 2 + 0.01, -0.02, 10000, em - 0.01);
        if (Math.abs(ab - n.h) < PR) {
            Vyp283(n.u, ua, uc, znak);
            return;
        }
        if (ab <= n.h) {
            spolecne.chyba("ab", "<=", n.h);
            return;
        }
        Vyp283(spolecne.hodnota(spolecne.funkce2.f283a, ua, uc, ab, n.u, em), ua, uc, znak);
        if (ab > spolecne.funkce2.f283a.f(ua, uc, -PI / 2 + PR)) return;
        Vyp283(spolecne.hodnota(spolecne.funkce2.f283a, ua, uc, ab, n.u, -PI / 2), ua, uc, znak);
    }

    private void Vyp284(double e, double ua, double p, boolean znak) {
        Spolecne.Double3 x = spolecne.abcua(ua, spolecne.alpua(ua, p, e), e);
        spolecne.vyslbac(znak, x.a, x.b, x.c);
    }

    public void P284(double ua, double p, double ab, boolean znak) {
        Spolecne.Double2M n = spolecne.min(spolecne.funkce2.f284, ua, p, -0.2, 0.02, 100000);
        if (Math.abs(ab - n.h) < PR) {
            Vyp284(n.u, ua, p, znak);
            return;
        }
        if (ab < n.h) {
            spolecne.chyba("ab", "<", n.h);
            return;
        }
        Vyp284(spolecne.hodnota(spolecne.funkce2.f284, ua, p, ab, n.u, PI / 2), ua, p, znak);
        Vyp284(spolecne.hodnota(spolecne.funkce2.f284, ua, p, ab, n.u, -PI / 2), ua, p, znak);
    }

    private void Vyp285(double al, double ua, double r, boolean znak, boolean tp) {
        double a = 2 * r * Math.sin(al);
        double e = spolecne.eta(a, ua, al);
        if (tp == true) e = -e;
        Spolecne.Double3 x = spolecne.abcua(ua, al, e);
        spolecne.vyslbac(znak, x.a, x.b, x.c);
    }

    public void P285(double ua, double r, double ab, boolean znak) {
        if (r <= ua / 2) {
            spolecne.chyba("r", "<=", ua / 2);
            return;
        }
        if (ab <= ua) {
            spolecne.chyba("ab", "<=", ua);
            return;
        }
        double aln = compLib.acos(ua / r - 1);
        double an = 2 * r * Math.sin(aln);
        double abn = an + ua / Math.cos(aln / 2);
        Spolecne.Double2M m = spolecne.max1(spolecne.funkce2.f285, ua, r, 0.02, 0.02, 0.0, true);
        if (Math.abs(ab - m.h) < PR) {
            Vyp285(m.u, ua, r, znak, true);
            return;
        }
        if (ab > m.h) {
            spolecne.chyba("ab", ">", m.h);
            return;
        }
        Vyp285(spolecne.hodnota1(spolecne.funkce2.f285, ua, r, ab, 0.0, m.u, true), ua, r, znak, true);
        Spolecne.Double2M m1 = spolecne.max3(spolecne.funkce2.f285, ua, r, 0.02, 0.02, 0.0, aln, false);
        if (Math.abs(m1.u - aln) < 0.1) {
            if (Math.abs(ab - abn) < PR) {
                spolecne.mala(an, an / 2 / Math.tan(aln / 2));
                return;
            }
            if (ab > abn) {
                Vyp285(spolecne.hodnota1(spolecne.funkce2.f285, ua, r, ab, aln, m.u, true), ua, r, znak, true);
                return;
            } else {
                Vyp285(spolecne.hodnota1(spolecne.funkce2.f285, ua, r, ab, 0.0, aln, false), ua, r, znak, false);
                return;
            }
        }
        Spolecne.Double2M n = spolecne.min1(spolecne.funkce2.f285, ua, r, m1.u + 0.02, 0.02, m1.h, false);
        if (Math.abs(ab - abn) < PR) spolecne.mala(an, an / 2 / Math.tan(aln / 2));
        if (ab > abn) Vyp285(spolecne.hodnota1(spolecne.funkce2.f285, ua, r, ab, aln, m.u, true), ua, r, znak, true);
        if (Math.abs(ab - m1.h) < PR) {
            Vyp285(m1.u, ua, r, znak, false);
            return;
        }
        if (ab > m1.h) return;
        Vyp285(spolecne.hodnota1(spolecne.funkce2.f285, ua, r, ab, 0.0, m1.u, false), ua, r, znak, false);
        if (Math.abs(ab - n.h) < PR) {
            Vyp285(n.u, ua, r, znak, false);
            return;
        }
        if (ab < n.h) return;
        Vyp285(spolecne.hodnota1(spolecne.funkce2.f285, ua, r, ab, n.u, m1.u, false), ua, r, znak, false);
        if (ab >= abn || Math.abs(ab - abn) < PR) return;
        Vyp285(spolecne.hodnota1(spolecne.funkce2.f285, ua, r, ab, n.u, aln, false), ua, r, znak, false);
    }

    private void Vyp286(double e, double ua, double ro, boolean znak) {
        Spolecne.Double3 x = spolecne.abcua(ua, 2 * compLib.asin(ro / (ua - ro / Math.cos(e))), e);
        spolecne.vyslbac(znak, x.a, x.b, x.c);
    }

    public void P286(double ua, double ro, double ab, boolean znak) {
        if (ro >= ua / 2) {
            spolecne.chyba("ro", ">=", ua / 2);
            return;
        }
        double em = PI / 2 - compLib.asin(2 * ro / ua);
        Spolecne.Double2M n = spolecne.min(spolecne.funkce2.f286, ua, ro, 0.0, 0.02, 100000);
        if (Math.abs(ab - n.h) < PR) {
            Vyp286(n.u, ua, ro, znak);
            return;
        }
        if (ab < n.h) {
            spolecne.chyba("ab", "<", n.h);
            return;
        }
        Vyp286(spolecne.hodnota(spolecne.funkce2.f286, ua, ro, ab, n.u, em), ua, ro, znak);
        Vyp286(spolecne.hodnota(spolecne.funkce2.f286, ua, ro, ab, n.u, -em), ua, ro, znak);
    }

    public void P287(double uc, double p, double ab) {
        double abmi, gam, e;
        abmi = spolecne.funkce2.f287.f(uc, p, 0);
        gam = spolecne.alpua(uc, p, 0);
        if (Math.abs(ab - abmi) < PR) {
            spolecne.malcux(gam, uc);
            return;
        }
        if (ab < abmi) {
            spolecne.chyba("ab", "<", abmi);
            return;
        }
        e = spolecne.hodnota(spolecne.funkce2.f287, uc, p, ab, 0.0, PI / 2);
        Spolecne.Double3 x = spolecne.abcua(uc, spolecne.alpua(uc, p, e), e);
        spolecne.vysbac(x.b, x.c, x.a);
    }

    private void Vyp288(double ga, double uc, double r) {
        double c = 2 * r * Math.sin(ga);
        double e = spolecne.eta(c, uc, ga);
        Spolecne.Double3 x = spolecne.abcua(uc, ga, e);
        spolecne.vysbac(x.b, x.c, x.a);
    }

    public void P288(double uc, double r, double ab) {
        if (uc >= 2 * r) {
            spolecne.chyba("uc", ">=", 2 * r);
            return;
        }
        if (ab <= 2 * uc) {
            spolecne.chyba("ab", "<=", 2 * uc);
            return;
        }
        double gan = compLib.acos(uc / r - 1);
        double abn = 2 * uc / Math.cos(gan / 2);
        if (Math.abs(ab - abn) < PR) spolecne.vysledek(abn / 2, abn / 2, 2 * uc * Math.tan(gan / 2));
        Spolecne.Double2M m = spolecne.max2(spolecne.funkce2.f288, uc, r, 0.02, 0.02, 0.0, gan);
        if (Math.abs(abn - m.h) < PR) {
            if (Math.abs(ab - abn) < PR) return;
            if (ab > abn) {
                spolecne.chyba("ab", ">", abn);
                return;
            }
            Vyp288(spolecne.hodnota(spolecne.funkce2.f288, uc, r, ab, 0.0, gan), uc, r);
            return;
        }
        if (Math.abs(ab - m.h) < PR) {
            Vyp288(m.u, uc, r);
            return;
        }
        if (ab > m.h) {
            spolecne.chyba("ab", ">", m.h);
            return;
        }
        Vyp288(spolecne.hodnota(spolecne.funkce2.f288, uc, r, ab, 0.0, m.u), uc, r);
        if (ab < abn || Math.abs(ab - abn) < PR) return;
        Vyp288(spolecne.hodnota(spolecne.funkce2.f288, uc, r, ab, gan, m.u), uc, r);
    }

    public void P289(double uc, double ro, double ab) {
        double abmi, ga, e, em;
        if (ro >= uc / 2) {
            spolecne.chyba("ro", ">=", uc / 2);
            return;
        }
        abmi = spolecne.funkce2.f289.f(uc, ro, 0.0);
        if (Math.abs(ab - abmi) < PR) {
            spolecne.macavc(ab / 2, uc);
            return;
        }
        if (ab < abmi) {
            spolecne.chyba("ab", "<", abmi);
            return;
        }
        em = PI / 2 - compLib.asin(2 * ro / uc);
        e = spolecne.hodnota(spolecne.funkce2.f289, uc, ro, ab, 0.0, em);
        ga = 2 * compLib.asin(ro / (uc - ro / Math.cos(e)));
        Spolecne.Double3 x = spolecne.abcua(uc, ga, e);
        spolecne.vysbac(x.b, x.c, x.a);
    }
}
