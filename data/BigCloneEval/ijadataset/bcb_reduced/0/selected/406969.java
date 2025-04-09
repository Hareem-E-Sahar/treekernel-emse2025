package com.neurogrid.simulation.statistics;

class Probability {

    public static double normalCDF(double d) {
        double ad[] = { 1.2533141373155003D, 1.1931829647319152D, 1.1374909212036046D, 1.0858270274680037D, 1.0378245758537268D, 0.99315579048815716D, 0.95152719207120673D, 0.91267556708321218D, 0.87636445645369232D, 0.84238109145213003D, 0.8105337152790304D, 0.78064923787086338D, 0.75257117906340809D, 0.72615786171399188D, 0.70128082185443008D, 0.67782340759117754D, 0.65567954241879844D, 0.63475263197692622D, 0.61495459615092973D, 0.59620501086902133D, 0.57843034604763111D, 0.5615632879362914D, 0.54554213565821696D, 0.53031026307125262D, 0.51581563821796339D, 0.50201039362041699D, 0.48885044152757373D, 0.47629512896051002D, 0.46430692803944218D, 0.45285115763062661D, 0.44189573283260003D, 0.43141093924000323D, 0.42136922928805448D, 0.41174503829897668D, 0.40251461812967171D, 0.39365588656305711D, 0.38514829079843427D, 0.37697268358296182D, 0.36911121069026381D, 0.36154720859634004D, 0.35426511132979333D, 0.34725036558519684D, 0.34048935328708502D, 0.33396932087918235D, 0.32767831469055236D, 0.3216051217986084D, 0.31573921586941034D, 0.31007070750935978D, 0.30459029871010301D, 0.29928924101087695D, 0.29415929704028959D, 0.28919270513321227D, 0.2843821467484926D, 0.27972071644000907D, 0.27520189415760615D, 0.270819519675909D, 0.26656776896822348D, 0.26244113236003597D, 0.25843439431203824D, 0.25454261469658929D, 0.25076111144396523D, 0.24708544444608058D, 0.24351140061545623D, 0.24003498000639087D, 0.23665238291356047D, 0.23335999787069861D, 0.23015439047880062D, 0.2270322929993801D, 0.22399059465382909D, 0.22102633257497697D, 0.21813668336147102D, 0.21531895518973632D, 0.21257058044203206D, 0.20988910881253681D, 0.20727220085650075D, 0.20471762195033022D, 0.20222323663305453D, 0.19978700330198626D, 0.19740696923751944D, 0.1950812659339918D, 0.19280810471531556D, 0.19058577261574028D, 0.18841262850760018D, 0.18628709945929098D, 0.18420767730797033D, 0.18217291543264921D, 0.18018142571439155D, 0.17823187567133156D, 0.17632298575710259D, 0.17445352681211268D, 0.17262231765785077D, 0.17082822282511367D, 0.16907015040769419D, 0.16734705003365535D, 0.1656579109468774D, 0.16400176019206403D, 0.16237766089686734D, 0.16078471064521938D, 0.15922203993636733D, 0.15768881072447175D, 0.15618421503397609D, 0.15470747364627135D, 0.15325783485347902D, 0.15183457327544114D, 0.15043698873626915D, 0.14906440519703301D, 0.14771616974139326D, 0.14639165161118275D, 0.14509024128913084D, 0.14381134962610503D, 0.14255440701040226D, 0.14131886257677895D, 0.1401041834530502D, 0.13890985404222025D, 0.13773537533823035D, 0.13658026427352798D, 0.13544405309676352D, 0.13432628877902719D, 0.13322653244712926D, 0.13214435884251541D, 0.13107935580449179D, 0.13003112377651022D, 0.12899927533433747D, 0.12798343473499657D, 0.12698323748543688D, 0.12599832992994281D, 0.12502836885535032D, 0.12407302111319091D, 0.12313196325793226D, 0.12220488120052962D, 0.12129146987654613D, 0.12039143292813971D, 0.11950448239925308D, 0.1186303384433775D, 0.11776872904329794D, 0.11691938974225351D, 0.11608206338598234D, 0.11525649987514432D, 0.11444245592764314D, 0.11363969485039356D, 0.11284798632010304D, 0.11206710617265928D, 0.11129683620073585D, 0.11053696395924793D, 0.1097872825783083D, 0.10904759058335189D, 0.10831769172211304D, 0.10759739479815637D, 0.10688651351067442D, 0.10618486630028322D, 0.1054922762005561D, 0.10480857069505113D, 0.10413358157959822D, 0.10346714482962362D, 0.10280910047230002D, 0.10215929246332031D, 0.10151756856810279D, 0.10088378024724459D, 0.10025778254604852D, 0.09963943398795666D, 0.099028596471731914D, 0.098425135172235639D, 0.097828918444656868D, 0.097239817732054645D, 0.096657707476081989D, 0.096082465030764619D, 0.09551397057921561D, 0.094952107053169116D, 0.094396760055224432D, 0.093847817783694998D, 0.0933051709599617D, 0.09276871275823452D, 0.092238338737630363D, 0.091713946776479274D, 0.091195437008774735D, 0.090682711762687329D, 0.0901756755010647D, 0.089674234763843744D, 0.089178298112304322D, 0.088687776075096467D, 0.08820258109597616D, 0.087722627483187265D, 0.087247831360429851D, 0.08677811061935764D, 0.086313384873549365D, 0.085853575413901609D, 0.085398605165392258D, 0.084948398645166068D, 0.084502881921895701D, 0.084061982576373637D, 0.083625629663291304D, 0.083193753674165163D, 0.082766286501369135D, 0.082343161403235818D, 0.081924312970189511D, 0.081509677091876009D, 0.081099190925255346D, 0.080692792863624713D, 0.080290422506540463D, 0.079892020630608934D, 0.0794975291611172D, 0.079106891144475791D, 0.078720050721446611D, 0.078336953101130161D, 0.077957544535687193D, 0.077581772295770929D, 0.077209584646646665D, 0.076840930824976603D, 0.076475761016248492D, 0.076114026332827461D, 0.075755678792611109D, 0.075400671298268801D, 0.075048957617046566D, 0.074700492361119905D, 0.074355230968477237D, 0.074013129684317433D, 0.073674145542945629D, 0.073338236350151506D, 0.073005360666055646D, 0.072675477788409229D, 0.072348547736333368D, 0.072024531234484698D, 0.071703389697634318D, 0.071385085215647445D, 0.071069580538852151D, 0.070756839063784729D, 0.070446824819301701D, 0.070139502453046232D, 0.069834837218259435D, 0.069532794960926D, 0.069233342107244383D, 0.068936445651412187D, 0.068642073143717439D, 0.068350192678926988D, 0.06806077288496333D, 0.067773782911861771D, 0.067489192420999697D, 0.067206971574590268D, 0.06692709102543308D, 0.066649521906914422D, 0.06637423582325018D };
        double d6 = d;
        double d5;
        if (Math.abs(d6) > 15D) {
            d5 = 0.0D;
        } else {
            int i = (int) Math.floor(Math.abs(d6) * 16D + 0.5D);
            double d7 = (double) i * 0.0625D;
            double d4 = Math.abs(d6) - d7;
            double d3 = ad[i];
            double d8 = d3 * d7 - 1.0D;
            double d9 = d3 + d7 * d8;
            double d10 = d8 * 2D + d7 * d9;
            double d11 = d9 * 3D + d7 * d10;
            double d12 = d10 * 4D + d7 * d11;
            d5 = d3 + (d4 * (d8 * 120D + d4 * (d9 * 60D + d4 * (d10 * 20D + d4 * (d11 * 5D + d4 * d12))))) / 120D;
            d5 = d5 * 0.3989422804014327D * Math.exp(d6 * -0.5D * d6);
        }
        if (d6 < 0.0D) return d5; else return 1.0D - d5;
    }

    public static double macheps() {
        if (vm_epsilon >= 1.0D) for (; 1.0D + vm_epsilon / 2D != 1.0D; vm_epsilon /= 2D) ;
        return vm_epsilon;
    }

    public static double lngamma(double d) {
        if (d < 1.0D) {
            return lngamma(1.0D + d) - Math.log(d);
        } else {
            double d3 = d - 1.0D;
            double d4 = d3 + 5.5D;
            d4 -= (d3 + 0.5D) * Math.log(d4);
            double d5 = 1.0D + 76.180091730000001D / (d3 + 1.0D) + -86.505320330000004D / (d3 + 2D) + 24.014098220000001D / (d3 + 3D) + -1.231739516D / (d3 + 4D) + 0.00120858003D / (d3 + 5D) + -5.3638199999999999E-006D / (d3 + 6D);
            return -d4 + Math.log(2.5066282746500002D * d5);
        }
    }

    public static double logbeta(double d, double d3) {
        return (lngamma(d) + lngamma(d3)) - lngamma(d + d3);
    }

    public static double betaCDF(double d, double d3, double d4) {
        if (d <= 0.0D) return 0.0D;
        double d20 = macheps();
        double d21 = Math.log(d20);
        double d22 = d20;
        double d23 = d21;
        double d17 = d;
        double d7 = d3;
        double d9 = d4;
        if ((d9 > d7 || d >= 0.80000000000000004D) && d >= 0.20000000000000001D) {
            d17 = 1.0D - d17;
            d7 = d4;
            d9 = d3;
        }
        double d18;
        if (((d7 + d9) * d17) / (d7 + 1.0D) < d20) {
            d18 = 0.0D;
            double d12 = d7 * Math.log(Math.max(d17, d22)) - Math.log(d7) - logbeta(d7, d9);
            if (d12 > d23 && d17 != 0.0D) d18 = Math.exp(d12);
            if (d17 != d || d7 != d3) d18 = 1.0D - d18;
        } else {
            double d8 = d9 - Math.floor(d9);
            if (d8 == 0.0D) d8 = 1.0D;
            double d13 = d7 * Math.log(d17) - logbeta(d8, d7) - Math.log(d7);
            d18 = 0.0D;
            if (d13 >= d23) {
                d18 = Math.exp(d13);
                double d10 = d18 * d7;
                if (d8 != 1.0D) {
                    int k = (int) Math.max(d21 / Math.log(d17), 4D);
                    for (int i = 1; i <= k; i++) {
                        double d15 = i;
                        d10 = (d10 * (d15 - d8) * d17) / d15;
                        d18 += d10 / (d7 + d15);
                    }
                }
            }
            if (d9 > 1.0D) {
                double d14 = (d7 * Math.log(d17) + d9 * Math.log(1.0D - d17)) - logbeta(d7, d9) - Math.log(d9);
                int i1 = (int) Math.max(d14 / d23, 0.0D);
                double d11 = Math.exp(d14 - (double) i1 * d23);
                double d5 = 1.0D / (1.0D - d17);
                double d19 = (d9 * d5) / ((d7 + d9) - 1.0D);
                double d6 = 0.0D;
                int l = (int) d9;
                if (d9 == (double) l) l--;
                for (int j = 1; j <= l; j++) {
                    if (d19 <= 1.0D && d11 / d20 <= d6) break;
                    double d16 = j;
                    d11 = (((d9 - d16) + 1.0D) * d5 * d11) / ((d7 + d9) - d16);
                    if (d11 > 1.0D) i1--;
                    if (d11 > 1.0D) d11 *= d22;
                    if (i1 == 0) d6 += d11;
                }
                d18 += d6;
            }
            if (d17 != d || d7 != d3) d18 = 1.0D - d18;
            d18 = Math.max(Math.min(d18, 1.0D), 0.0D);
        }
        return d18;
    }

    public static double binomialCDF(int i, int j, double d) {
        double d5;
        if (i < 0) d5 = 0.0D; else if (i >= j) d5 = 1.0D; else if (d == 0.0D) d5 = i >= 0 ? 1.0D : 0.0D; else if (d == 1.0D) {
            d5 = i >= j ? 1.0D : 0.0D;
        } else {
            double d3 = (double) i + 1.0D;
            double d4 = j - i;
            d5 = 1.0D - betaCDF(d, d3, d4);
        }
        return d5;
    }

    public static double cauchyCDF(double d) {
        return (Math.atan(d) + 1.5707963267948966D) / 3.1415926535897931D;
    }

    public static double fCDF(double d, double d3, double d4) {
        return 1.0D - betaCDF(d4 / (d4 + d3 * d), 0.5D * d4, 0.5D * d3);
    }

    private static double gnorm(double d, double d3) {
        if (d3 <= 0.0D || d <= 0.0D) {
            return 0.0D;
        } else {
            double d4 = Math.sqrt(d) * 3D * ((Math.pow(d3 / d, 0.33333333333333331D) + 1.0D / (d * 9D)) - 1.0D);
            return normalCDF(d4);
        }
    }

    private static double gser(double d, double d3, double d4) {
        boolean flag = false;
        double d5;
        if (d3 <= 0.0D || d <= 0.0D) {
            d5 = 0.0D;
        } else {
            double d8 = d;
            double d7 = 1.0D / d;
            double d6 = d7;
            for (int i = 1; !flag && i < 1000; i++) {
                d8++;
                d7 *= d3 / d8;
                d6 += d7;
                if (Math.abs(d7) < 1E-014D) flag = true;
            }
            d5 = d6 * Math.exp((-d3 + d * Math.log(d3)) - d4);
        }
        return d5;
    }

    private static double gcf(double d, double d3, double d4) {
        double d5 = 0.0D;
        double d7 = 1.0D;
        double d8 = 1.0D;
        double d9 = 0.0D;
        double d14 = 1.0D;
        boolean flag = false;
        double d13 = d3;
        double d15 = 0.0D;
        for (double d12 = 1.0D; !flag && d12 <= 1000D; d12++) {
            double d11 = d12 - d;
            d14 = (d13 + d14 * d11) * d7;
            d9 = (d8 + d9 * d11) * d7;
            double d10 = d12 * d7;
            d13 = d3 * d14 + d10 * d13;
            d8 = d3 * d9 + d10 * d8;
            if (d13 != 0.0D) {
                d7 = 1.0D / d13;
                double d6 = d8 * d7;
                if (Math.abs((d6 - d5) / d6) < 1E-014D) {
                    d15 = Math.exp((-d3 + d * Math.log(d3)) - d4) * d6;
                    flag = true;
                }
                d5 = d6;
            }
        }
        return d15;
    }

    public static double gammaCDF(double d, double d3) {
        if (d3 <= 0.0D || d <= 0.0D) return 0.0D;
        if (d > 10000D) return gnorm(d, d3);
        double d4 = lngamma(d);
        if (d3 < d + 1.0D) return gser(d, d3, d4); else return 1.0D - gcf(d, d3, d4);
    }

    public static double chisqCDF(double d, double d3) {
        return gammaCDF(0.5D * d3, 0.5D * d);
    }

    public static double poissonCDF(int i, double d) {
        double d3;
        if (i < 0) d3 = 0.0D; else if (d == 0.0D) {
            d3 = i >= 0 ? 1.0D : 0.0D;
        } else {
            double d4 = (double) i + 1.0D;
            d3 = 1.0D - gammaCDF(d4, d);
        }
        return d3;
    }

    public static double tCDF(double d, double d3) {
        double d11 = d3;
        double d9 = 1.0D;
        double d4 = d * d;
        double d5 = d4 / d11;
        double d6 = 1.0D + d5;
        double d12;
        if (d11 > Math.floor(d11) || d11 >= 20D && d4 < d11 || d11 > 20D) {
            if (d11 < 2D && d11 != 1.0D) {
                double d13 = 0.5D;
                double d14 = 0.5D * d11;
                Math.floor(d14);
                double d15 = d14 / (d14 + d13 * d4);
                double d16 = betaCDF(d15, d14, d13);
                d12 = d < 0.0D ? 0.5D * d16 : 1.0D - 0.5D * d16;
            } else {
                if (d5 > 9.9999999999999995E-007D) d5 = Math.log(d6);
                double d7 = d11 - 0.5D;
                d6 = 48D * d7 * d7;
                d5 = d7 * d5;
                d5 = (((((-0.40000000000000002D * d5 - 3.2999999999999998D) * d5 - 24D) * d5 - 85.5D) / (0.80000000000000004D * d5 * d5 + 100D + d6) + d5 + 3D) / d6 + 1.0D) * Math.sqrt(d5);
                d5 = -1D * d5;
                d12 = normalCDF(d5);
                if (d > 0.0D) d12 = 1.0D - d12;
            }
        } else {
            double d8;
            if (d11 < 20D && d4 < 4D) {
                d8 = Math.sqrt(d5);
                d5 = d8;
                if (d11 == 1.0D) d8 = 0.0D;
            } else {
                d8 = Math.sqrt(d6);
                d5 = d8 * d11;
                for (double d10 = 2D; Math.abs(d8 - d9) > 9.9999999999999995E-007D; d10 += 2D) {
                    d9 = d8;
                    d5 = (d5 * (d10 - 1.0D)) / (d6 * d10);
                    d8 += d5 / (d11 + d10);
                }
                d11 += 2D;
                d9 = 0.0D;
                d5 = 0.0D;
                d8 = -d8;
            }
            for (d11 -= 2D; d11 > 1.0D; d11 -= 2D) d8 = ((d11 - 1.0D) / (d6 * d11)) * d8 + d5;
            d8 = Math.abs(d11) >= 9.9999999999999995E-007D ? 0.63661977236758138D * (Math.atan(d5) + d8 / d6) : d8 / Math.sqrt(d6);
            d12 = d9 - d8;
            if (d > 0.0D) d12 = 1.0D - 0.5D * d12; else d12 = 0.5D * d12;
        }
        return d12;
    }

    public static double betaQuantile(double d, double d3, double d4) {
        double d5 = (lngamma(d3) + lngamma(d4)) - lngamma(d3 + d4);
        double d30 = -300D;
        double d6 = d;
        if (d3 <= 0.0D || d4 <= 0.0D) return d6;
        if (d == 0.0D || d == 1.0D) return d6;
        boolean flag;
        double d12;
        double d24;
        double d25;
        if (d <= 0.5D) {
            d12 = d;
            d24 = d3;
            d25 = d4;
            flag = false;
        } else {
            d12 = 1.0D - d;
            d24 = d4;
            d25 = d3;
            flag = true;
        }
        double d15 = Math.sqrt(-Math.log(d12 * d12));
        double d21 = d15 - (d15 * 0.27061000000000002D + 2.3075299999999999D) / (1.0D + (d15 * 0.044810000000000003D + 0.99229000000000001D) * d15);
        if (d24 > 1.0D && d25 > 1.0D) {
            d15 = (d21 * d21 - 3D) / 6D;
            double d16 = 1.0D / ((d24 + d24) - 1.0D);
            double d17 = 1.0D / ((d25 + d25) - 1.0D);
            double d14 = 2D / (d16 + d17);
            double d7 = (d21 * Math.sqrt(d14 + d15)) / d14;
            double d10 = (d17 - d16) * ((d15 + 0.83333333333333337D) - 2D / (3D * d14));
            double d20 = d7 - d10;
            d6 = d24 / (d24 + d25 * Math.exp(d20 + d20));
        } else {
            d15 = d25 + d25;
            double d18 = 1.0D / (d25 * 9D);
            double d8 = (1.0D - d18) + d21 * Math.sqrt(d18);
            d18 = d15 * (d8 * d8 * d8);
            if (d18 <= 0.0D) {
                d6 = 1.0D - Math.exp((Math.log((1.0D - d12) * d25) + d5) / d25);
            } else {
                d18 = ((4D * d24 + d15) - 2D) / d18;
                if (d18 <= 1.0D) d6 = Math.exp((Math.log(d12 * d24) + d5) / d24); else d6 = 1.0D - 2D / (d18 + 1.0D);
            }
        }
        d15 = 1.0D - d24;
        double d19 = 1.0D - d25;
        double d23 = 0.0D;
        double d26 = 1.0D;
        double d11 = 1.0D;
        if (d6 < 0.0001D) d6 = 0.0001D;
        if (d6 > 0.99990000000000001D) d6 = 0.99990000000000001D;
        double d9 = -5D / (d24 * d24) - 1.0D / (d12 * d12) - 13D;
        int i = d9 >= -30D ? (int) d9 : -30;
        double d29 = Math.pow(10D, i);
        do {
            double d22 = betaCDF(d6, d24, d25);
            double d31 = d6;
            d22 = (d22 - d12) * Math.exp(d5 + d15 * Math.log(d31) + d19 * Math.log(1.0D - d31));
            if (d22 * d23 <= 0.0D) d11 = d26 <= d30 ? d30 : d26;
            double d13 = 1.0D;
            double d27;
            do {
                double d28 = d13 * d22;
                d26 = d28 * d28;
                if (d26 < d11) {
                    d27 = d6 - d28;
                    if (d27 >= 0.0D && d27 <= 1.0D) {
                        if (d11 <= d29 || d22 * d22 <= d29) {
                            if (flag) d6 = 1.0D - d6;
                            return d6;
                        }
                        if (d27 != 0.0D && d27 != 1.0D) break;
                    }
                }
                d13 /= 3D;
            } while (true);
            if (d27 == d6) {
                if (flag) d6 = 1.0D - d6;
                return d6;
            }
            d6 = d27;
            d23 = d22;
        } while (true);
    }

    public static int binomialQuantile(double d, int i, double d3) {
        if (d3 == 0.0D) return 0;
        if (d3 == (double) i) return i;
        double d4 = (double) i * d3;
        double d5 = Math.sqrt((double) i * d3 * (1.0D - d3));
        int j1 = Math.max(1, (int) (0.20000000000000001D * d5));
        int j = (int) (d4 + d5 * normalQuantile(d));
        int l = j;
        int i1 = j;
        double d6;
        do {
            l -= j1;
            l = Math.max(0, l);
            d6 = binomialCDF(l, i, d3);
        } while (l > 0 && d6 > d);
        if (l == 0 && d6 >= d) return l;
        double d8;
        do {
            i1 += j1;
            i1 = Math.min(i, i1);
            d8 = binomialCDF(i1, i, d3);
        } while (i1 < i && d8 < d);
        if (i1 == i && d8 <= d) return i1;
        while (i1 - l > 1) {
            int k = (l + i1) / 2;
            double d10 = binomialCDF(k, i, d3);
            if (d10 < d) {
                l = k;
                double d7 = d10;
            } else {
                i1 = k;
                double d9 = d10;
            }
        }
        return i1;
    }

    public static double cauchyQuantile(double d) {
        return Math.tan(3.1415926535897931D * (d - 0.5D));
    }

    public static double chisqQuantile(double d, double d3) {
        double d13 = lngamma(d3 * 0.5D);
        double d4 = -1D;
        double d31 = 0.5D * d3;
        double d12 = d31 - 1.0D;
        double d30;
        if (d3 < -1.24D * Math.log(d)) {
            d30 = Math.pow(d * d31 * Math.exp(d13 + d31 * 0.69314718060000002D), 1.0D / d31);
            if (d30 < 4.9999999999999998E-007D) {
                d4 = d30;
                return d4;
            }
        } else if (d3 > 0.32000000000000001D) {
            double d18 = normalQuantile(d);
            double d19 = 0.222222D / d3;
            double d5 = (d18 * Math.sqrt(d19) + 1.0D) - d19;
            d30 = d3 * (d5 * d5 * d5);
            if (d30 > 2.2000000000000002D * d3 + 6D) d30 = -2D * ((Math.log(1.0D - d) - d12 * Math.log(0.5D * d30)) + d13);
        } else {
            d30 = 0.40000000000000002D;
            double d9 = Math.log(1.0D - d);
            double d14;
            do {
                d14 = d30;
                double d20 = 1.0D + d30 * (4.6699999999999999D + d30);
                double d22 = d30 * (6.7300000000000004D + d30 * (6.6600000000000001D + d30));
                double d6 = -0.5D + (4.6699999999999999D + 2D * d30) / d20;
                double d8 = (6.7300000000000004D + d30 * (13.32D + 3D * d30)) / d22;
                double d16 = d6 - d8;
                d30 -= (1.0D - (Math.exp(d9 + d13 + 0.5D * d30 + d12 * 0.69314718060000002D) * d22) / d20) / d16;
            } while (Math.abs(d14 / d30 - 1.0D) > 0.01D);
        }
        double d15;
        do {
            d15 = d30;
            double d21 = 0.5D * d30;
            double d23 = d - gammaCDF(d31, d21);
            double d17 = d23 * Math.exp((d31 * 0.69314718060000002D + d13 + d21) - d12 * Math.log(d30));
            double d11 = d17 / d30;
            double d10 = 0.5D * d17 - d11 * d12;
            double d24 = (210D + d10 * (140D + d10 * (105D + d10 * (84D + d10 * (70D + 60D * d10))))) / 420D;
            double d25 = (420D + d10 * (735D + d10 * (966D + d10 * (1141D + 1278D * d10)))) / 2520D;
            double d26 = (210D + d10 * (462D + d10 * (707D + 932D * d10))) / 2520D;
            double d27 = (252D + d10 * (672D + 1182D * d10) + d12 * (294D + d10 * (889D + 1740D * d10))) / 5040D;
            double d28 = (84D + 2264D * d10 + d12 * (1175D + 606D * d10)) / 2520D;
            double d29 = (120D + d12 * (346D + 127D * d12)) / 5040D;
            double d7 = d26 - d11 * (d27 - d11 * (d28 - d11 * d29));
            d7 = d24 - d11 * (d25 - d11 * d7);
            d30 += d17 * ((1.0D + 0.5D * d17 * d24) - d11 * d12 * d7);
        } while (Math.abs(d15 / d30 - 1.0D) > 4.9999999999999998E-007D);
        d4 = d30;
        return d4;
    }

    public static double fQuantile(double d, double d3, double d4) {
        if (d == 0.0D) {
            return 0.0D;
        } else {
            double d5 = betaQuantile(1.0D - d, 0.5D * d4, 0.5D * d3);
            return (d4 * (1.0D / d5 - 1.0D)) / d3;
        }
    }

    public static double gammaQuantile(double d, double d3) {
        return 0.5D * chisqQuantile(d3, 2D * d);
    }

    public static double normalQuantile(double d) {
        double d3 = d - 0.5D;
        double d6;
        if (Math.abs(d3) <= 0.41999999999999998D) {
            double d4 = d3 * d3;
            d6 = (d3 * (((-25.44106049637D * d4 + 41.39119773534D) * d4 + -18.615000625290001D) * d4 + 2.5066282388399999D)) / ((((3.13082909833D * d4 + -21.06224101826D) * d4 + 23.083367437429999D) * d4 + -8.4735109308999998D) * d4 + 1.0D);
        } else {
            double d5 = d;
            if (d3 > 0.0D) d5 = 1.0D - d;
            d5 = Math.sqrt(-Math.log(d5));
            d6 = (((2.3212127685000001D * d5 + 4.8501412713500001D) * d5 + -2.2979647913400001D) * d5 + -2.7871893113800001D) / ((1.6370678189700001D * d5 + 3.5438892476200001D) * d5 + 1.0D);
            if (d3 < 0.0D) d6 = -d6;
        }
        return d6;
    }

    public static int poissonQuantile(double d, double d3) {
        if (d == 0.0D) return 0;
        if (d3 == 0.0D) return 0;
        double d4 = d3;
        double d5 = Math.sqrt(d3);
        int i1 = Math.max(1, (int) (0.20000000000000001D * d5));
        int i = (int) (d4 + d5 * normalQuantile(d));
        int k = i;
        int l = i;
        double d6;
        do {
            k -= i1;
            k = Math.max(0, k);
            d6 = poissonCDF(k, d3);
        } while (k > 0 && d6 > d);
        if (k == 0 && d6 >= d) return k;
        double d8;
        do {
            l += i1;
            d8 = poissonCDF(l, d3);
        } while (d8 < d);
        while (l - k > 1) {
            int j = (k + l) / 2;
            double d10 = poissonCDF(j, d3);
            if (d10 < d) {
                k = j;
                double d7 = d10;
            } else {
                l = j;
                double d9 = d10;
            }
        }
        return l;
    }

    public static double tQuantile(double d, double d3) {
        double d5 = d >= 0.5D ? 2D * (1.0D - d) : 2D * d;
        double d4;
        if (d3 <= 3D) {
            if (d3 == 1.0D) d4 = Math.tan(1.5707963268D * (1.0D - d5)); else if (d3 == 2D) {
                d4 = Math.sqrt(2D / (d5 * (2D - d5)) - 2D);
            } else {
                d4 = betaQuantile(d5, 0.5D * d3, 0.5D);
                if (d4 != 0.0D) d4 = Math.sqrt(d3 / d4 - d3);
            }
        } else {
            double d6 = 1.0D / (d3 - 0.5D);
            double d7 = 48D / (d6 * d6);
            double d8 = (((20700D * d6) / d7 - 98D) * d6 - 16D) * d6 + 96.359999999999999D;
            double d9 = ((94.5D / (d7 + d8) - 3D) / d7 + 1.0D) * Math.sqrt(d6 * 1.5707963268D) * d3;
            double d10 = d9 * d5;
            double d12 = Math.pow(d10, 2D / d3);
            if (d12 > 0.050000000000000003D + d6) {
                double d11 = normalQuantile(0.5D * d5);
                d12 = d11 * d11;
                if (d3 < 5D) d8 += 0.29999999999999999D * (d3 - 4.5D) * (d11 + 0.59999999999999998D);
                d8 = (((0.050000000000000003D * d9 * d11 - 5D) * d11 - 7D) * d11 - 2D) * d11 + d7 + d8;
                d12 = (((((0.40000000000000002D * d12 + 6.2999999999999998D) * d12 + 36D) * d12 + 94.5D) / d8 - d12 - 3D) / d7 + 1.0D) * d11;
                d12 = d6 * d12 * d12;
                d12 = d12 <= 0.002D ? 0.5D * d12 * d12 + d12 : Math.exp(d12) - 1.0D;
            } else {
                d12 = (((1.0D / (((d3 + 6D) / (d3 * d12) - 0.088999999999999996D * d9 - 0.82199999999999995D) * (d3 + 2D) * 3D) + 0.5D / (d3 + 4D)) * d12 - 1.0D) * (d3 + 1.0D)) / (d3 + 2D) + 1.0D / d12;
            }
            d4 = Math.sqrt(d3 * d12);
        }
        if (d < 0.5D) d4 = -d4;
        return d4;
    }

    public static double betaPDF(double d, double d3, double d4) {
        if (d <= 0.0D || d >= 1.0D) return 0.0D; else return Math.exp((Math.log(d) * (d3 - 1.0D) + Math.log(1.0D - d) * (d4 - 1.0D)) - logbeta(d3, d4));
    }

    public static double binomialPMF(int i, int j, double d) {
        if (d == 0.0D) return i != 0 ? 0.0D : 1.0D;
        if (d == 1.0D) return i != j ? 0.0D : 1.0D;
        if (i < 0 || i > j) return 0.0D; else return Math.exp((lngamma((double) j + 1.0D) - lngamma((double) i + 1.0D) - lngamma((double) (j - i) + 1.0D)) + (double) i * Math.log(d) + (double) (j - i) * Math.log(1.0D - d));
    }

    public static double cauchyPDF(double d) {
        return tPDF(d, 1.0D);
    }

    public static double chisqPDF(double d, double d3) {
        return 0.5D * gammaPDF(0.5D * d, 0.5D * d3);
    }

    public static double fPDF(double d, double d3, double d4) {
        if (d <= 0.0D) return 0.0D; else return Math.exp((0.5D * d3 * Math.log(d3) + 0.5D * d4 * Math.log(d4) + (0.5D * d3 - 1.0D) * Math.log(d)) - logbeta(0.5D * d3, 0.5D * d4) - 0.5D * (d3 + d4) * Math.log(d4 + d3 * d));
    }

    public static double gammaPDF(double d, double d3) {
        if (d <= 0.0D) return 0.0D; else return Math.exp(Math.log(d) * (d3 - 1.0D) - d - lngamma(d3));
    }

    public static double normalPDF(double d) {
        return Math.exp(-0.5D * d * d) / Math.sqrt(6.2831853071795862D);
    }

    public static double poissonPMF(int i, double d) {
        if (d == 0.0D) return i != 0 ? 0.0D : 1.0D;
        if (i < 0) return 0.0D; else return Math.exp((double) i * Math.log(d) - d - lngamma((double) i + 1.0D));
    }

    public static double tPDF(double d, double d3) {
        return (1.0D / Math.sqrt(d3 * 3.1415926535897931D)) * Math.exp(lngamma(0.5D * (d3 + 1.0D)) - lngamma(0.5D * d3) - 0.5D * (d3 + 1.0D) * Math.log(1.0D + (d * d) / d3));
    }

    public static void uniformSeeds(long l, long l1) {
        seedi = l & 0xffffffffL;
        seedj = l1 & 0xffffffffL;
    }

    public static double uniformRand() {
        seedi = seedi * 0x10dcdL + 0x168360dL & 0xffffffffL;
        seedj ^= seedj << 13 & 0xffffffffL;
        seedj ^= seedj >> 17 & 0xffffffffL;
        seedj ^= seedj << 5 & 0xffffffffL;
        return (double) (seedi + seedj & 0xffffffffL) * Math.pow(2D, -32D);
    }

    public static int bernoulliRand(double d) {
        return uniformRand() > d ? 0 : 1;
    }

    public static int poissonRand(double d) {
        int i;
        if (d < 12D) {
            double d5 = Math.exp(-d);
            i = -1;
            double d7 = 1.0D;
            do {
                i++;
                d7 *= uniformRand();
            } while (d7 > d5);
        } else {
            double d3 = Math.sqrt(2D * d);
            double d4 = Math.log(d);
            double d6 = d * d4 - lngamma(d + 1.0D);
            double d8;
            do {
                double d9;
                do {
                    d9 = Math.tan(3.1415926535897931D * uniformRand());
                    i = (int) Math.floor(d3 * d9 + d);
                } while (i < 0);
                d8 = 0.90000000000000002D * (1.0D + d9 * d9) * Math.exp((double) i * d4 - lngamma((double) i + 1.0D) - d6);
            } while (uniformRand() > d8);
        }
        return i;
    }

    public static int binomialRand(int i, double d) {
        double d7 = d > 0.5D ? 1.0D - d : d;
        double d3 = (double) i * d7;
        int k;
        if (d7 == 0.0D) k = 0; else if (d7 == 1.0D) k = i; else if (i < 50) {
            k = 0;
            for (int j = 0; j < i; j++) if (uniformRand() < d7) k++;
        } else if (d3 < 1.0D) {
            double d5 = Math.exp(-d3);
            double d9 = 1.0D;
            k = -1;
            do {
                k++;
                d9 *= uniformRand();
            } while (d9 > d5);
            if (k > i) k = i;
        } else {
            double d15 = i;
            double d6 = lngamma(d15 + 1.0D);
            double d12 = 1.0D - d7;
            double d13 = Math.log(d7);
            double d14 = Math.log(d12);
            double d8 = Math.sqrt(2D * d3 * d12);
            double d4;
            double d10;
            do {
                double d11;
                do {
                    d11 = Math.tan(3.1415926535897931D * uniformRand());
                    d4 = d8 * d11 + d3;
                } while (d4 < 0.0D || d4 >= d15 + 1.0D);
                d4 = Math.floor(d4);
                d10 = 1.2D * d8 * (1.0D + d11 * d11) * Math.exp((d6 - lngamma(d4 + 1.0D) - lngamma((d15 - d4) + 1.0D)) + d4 * d13 + (d15 - d4) * d14);
            } while (uniformRand() > d10);
            k = (int) d4;
        }
        if (d7 != d) k = i - k;
        return k;
    }

    public static double normalRand() {
        double d = Math.sqrt(2D / Math.exp(1.0D));
        double d3;
        double d4;
        double d5;
        do {
            d5 = uniformRand();
            double d6 = uniformRand();
            double d7 = d * (2D * d6 - 1.0D);
            d3 = d7 / d5;
            d4 = (d3 * d3) / 4D;
        } while (d4 > 1.0D - d5 && d4 > -Math.log(d5));
        return d3;
    }

    public static double cauchyRand() {
        double d4;
        double d5;
        do {
            double d = uniformRand();
            double d3 = uniformRand();
            d4 = 2D * d - 1.0D;
            d5 = d3;
        } while (d4 * d4 + d5 * d5 > 1.0D);
        return d4 / d5;
    }

    public static double gammaRand(double d) {
        double d3 = Math.exp(1.0D);
        double d4;
        if (d < 1.0D) {
            boolean flag = false;
            double d11 = (d + d3) / d3;
            do {
                double d5 = uniformRand();
                double d6 = uniformRand();
                double d9 = d11 * d5;
                if (d9 <= 1.0D) {
                    d4 = Math.exp(Math.log(d9) / d);
                    if (d6 <= Math.exp(-d4)) flag = true;
                } else {
                    d4 = -Math.log((d11 - d9) / d);
                    if (d4 > 0.0D && d6 < Math.exp((d - 1.0D) * Math.log(d4))) flag = true;
                }
            } while (!flag);
        } else if (d == 1.0D) {
            d4 = -Math.log(uniformRand());
        } else {
            double d12 = d - 1.0D;
            double d13 = (d - 1.0D / (6D * d)) / d12;
            double d14 = 2D / d12;
            double d15 = 2D / (d - 1.0D) + 2D;
            double d16 = 1.0D / Math.sqrt(d);
            double d7;
            double d10;
            do {
                double d8;
                do {
                    d7 = uniformRand();
                    d8 = uniformRand();
                    if (d > 2.5D) d7 = d8 + d16 * (1.0D - 1.8600000000000001D * d7);
                } while (d7 <= 0.0D || d7 >= 1.0D);
                d10 = (d13 * d8) / d7;
            } while (d14 * d7 + d10 + 1.0D / d10 > d15 && (d14 * Math.log(d7) - Math.log(d10)) + d10 > 1.0D);
            d4 = d12 * d10;
        }
        return d4;
    }

    public static double chisqRand(double d) {
        return 2D * gammaRand(d / 2D);
    }

    public static double tRand(double d) {
        return normalRand() / Math.sqrt(chisqRand(d) / d);
    }

    public static double betaRand(double d, double d3) {
        double d4 = gammaRand(d);
        double d5 = gammaRand(d3);
        return d4 / (d4 + d5);
    }

    public static double fRand(double d, double d3) {
        return (d3 * chisqRand(d)) / (d * chisqRand(d3));
    }

    Probability() {
    }

    private static double vm_epsilon = 1.0D;

    private static final double COF1 = 76.180091730000001D;

    private static final double COF2 = -86.505320330000004D;

    private static final double COF3 = 24.014098220000001D;

    private static final double COF4 = -1.231739516D;

    private static final double COF5 = 0.00120858003D;

    private static final double COF6 = -5.3638199999999999E-006D;

    private static final double EPSILON = 1E-014D;

    private static final double LARGE_A = 10000D;

    private static final int ITMAX = 1000;

    private static final double TWOVRPI = 0.63661977236758138D;

    private static final double HALF_PI = 1.5707963268D;

    private static final double TOL = 9.9999999999999995E-007D;

    private static final double sae = -30D;

    private static final double zero = 0D;

    private static final double one = 1D;

    private static final double two = 2D;

    private static final double three = 3D;

    private static final double four = 4D;

    private static final double five = 5D;

    private static final double six = 6D;

    private static final double aa = 0.69314718060000002D;

    private static final double c1 = 0.01D;

    private static final double c2 = 0.222222D;

    private static final double c3 = 0.32000000000000001D;

    private static final double c4 = 0.40000000000000002D;

    private static final double c5 = 1.24D;

    private static final double c6 = 2.2000000000000002D;

    private static final double c7 = 4.6699999999999999D;

    private static final double c8 = 6.6600000000000001D;

    private static final double c9 = 6.7300000000000004D;

    private static final double e = 4.9999999999999998E-007D;

    private static final double c10 = 13.32D;

    private static final double c11 = 60D;

    private static final double c12 = 70D;

    private static final double c13 = 84D;

    private static final double c14 = 105D;

    private static final double c15 = 120D;

    private static final double c16 = 127D;

    private static final double c17 = 140D;

    private static final double c18 = 1175D;

    private static final double c19 = 210D;

    private static final double c20 = 252D;

    private static final double c21 = 2264D;

    private static final double c22 = 294D;

    private static final double c23 = 346D;

    private static final double c24 = 420D;

    private static final double c25 = 462D;

    private static final double c26 = 606D;

    private static final double c27 = 672D;

    private static final double c28 = 707D;

    private static final double c29 = 735D;

    private static final double c30 = 889D;

    private static final double c31 = 932D;

    private static final double c32 = 966D;

    private static final double c33 = 1141D;

    private static final double c34 = 1182D;

    private static final double c35 = 1278D;

    private static final double c36 = 1740D;

    private static final double c37 = 2520D;

    private static final double c38 = 5040D;

    private static final double half = 0.5D;

    private static final double pmin = 0D;

    private static final double pmax = 1D;

    private static final double split = 0.41999999999999998D;

    private static final double a0 = 2.5066282388399999D;

    private static final double a1 = -18.615000625290001D;

    private static final double a2 = 41.39119773534D;

    private static final double a3 = -25.44106049637D;

    private static final double b1 = -8.4735109308999998D;

    private static final double b2 = 23.083367437429999D;

    private static final double b3 = -21.06224101826D;

    private static final double b4 = 3.13082909833D;

    private static final double cc0 = -2.7871893113800001D;

    private static final double cc1 = -2.2979647913400001D;

    private static final double cc2 = 4.8501412713500001D;

    private static final double cc3 = 2.3212127685000001D;

    private static final double d1 = 3.5438892476200001D;

    private static final double d2 = 1.6370678189700001D;

    private static final long MASK = 0xffffffffL;

    private static long seedi = 0x75bcd15L;

    private static long seedj = 0x159a55e5L;
}
