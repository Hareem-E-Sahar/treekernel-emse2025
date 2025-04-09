package solarex.system;

import java.util.Random;

/**
 * Planetary resources.
 * 
 * @author Hj. Malthaner
 */
public class PlanetResources {

    public enum Gases {

        Hydrogen("Hydrogen", "#eeeeee"), Helium("Helium", "#ffffcc"), Oxygen("Oxygen", "#ccccff"), Nitrogen("Nitrogen", "#ccffbb"), CarbonDioxide("Carbon dioxide", "#ffcccc"), Ammonia("Ammonia", "#ccffff"), WaterVapor("Water vapor", "#99bbff"), Methane("Methane", "#ffdd99");

        private String name;

        public String color;

        Gases(String name, String color) {
            this.name = name;
            this.color = color;
        }

        @Override
        public String toString() {
            return name;
        }
    }

    /**
     * A list of not-too-volatile metals that might exist natively
     * on some planets.
     * @author Hj. Malthaner
     */
    public enum Metals {

        Chromium("#ddeeff"), Lead("#aaaaaa"), Manganese("#eedddd"), Tin("#cccccc"), Copper("#ffbb88"), Silver("#ffffcc"), Gold("#ffff77"), Platinum("#eeeeee"), Zinc("aaaadd"), Mercury("#ffffff"), Iron("#ddccbb"), Nickel("#ddddaa"), Iridium, Osmium, Palladium, Rhodium, Ruthenium, Bismuth, Indium, Tellurium;

        /** Color for display purposes */
        public final String color;

        Metals() {
            color = "#eeeeee";
        }

        ;

        Metals(String color) {
            this.color = color;
        }
    }

    public enum Minerals {

        Sulphur("#dddddd"), Phosphates, Fluorspar, Kaolinite("#dddddd"), Potash, Gypsum, IronOxides("Iron oxides", "#ffcc99"), Scandium, Yttrium, Lanthanum("#ffffff"), Cerium("#ffff00"), Praseodymium("#44dd00"), Neodymium("#dd00ee"), Promethium("#dd5500"), Samarium, Europium("#0066ff"), Gadolinium, Terbium("#55aa55"), Dysprosium("#aaaaaa"), Holmium("#bbccaa"), Erbium("#aaccbb"), Thulium("#5555aa"), Ytterbium("#aa5555"), Lutetium("#cc9955");

        /** Color for display purposes */
        public final String color;

        /** Name for display purposes */
        private String name;

        Minerals() {
            name = name();
            color = "#cccccc";
        }

        ;

        Minerals(String color) {
            name = name();
            this.color = color;
        }

        Minerals(String name, String color) {
            this.name = name;
            this.color = color;
        }

        @Override
        public String toString() {
            return name;
        }
    }

    public enum Fluids {

        Water;

        private String name;

        Fluids() {
            name = name();
        }

        ;

        Fluids(String name) {
            this.name = name;
        }

        @Override
        public String toString() {
            return name;
        }
    }

    public static boolean isRareEarth(int index) {
        return index == Minerals.Scandium.ordinal() || index == Minerals.Yttrium.ordinal() || index == Minerals.Lanthanum.ordinal() || index == Minerals.Cerium.ordinal() || index == Minerals.Praseodymium.ordinal() || index == Minerals.Neodymium.ordinal() || index == Minerals.Promethium.ordinal() || index == Minerals.Samarium.ordinal() || index == Minerals.Europium.ordinal() || index == Minerals.Gadolinium.ordinal() || index == Minerals.Terbium.ordinal() || index == Minerals.Dysprosium.ordinal() || index == Minerals.Holmium.ordinal() || index == Minerals.Erbium.ordinal() || index == Minerals.Thulium.ordinal() || index == Minerals.Ytterbium.ordinal() || index == Minerals.Lutetium.ordinal();
    }

    public static boolean isNobleMetal(int index) {
        return index == Metals.Gold.ordinal() || index == Metals.Platinum.ordinal() || index == Metals.Silver.ordinal() || index == Metals.Osmium.ordinal() || index == Metals.Palladium.ordinal() || index == Metals.Rhodium.ordinal() || index == Metals.Ruthenium.ordinal() || index == Metals.Iridium.ordinal();
    }

    public static boolean isNonIronMetal(int index) {
        return index == Metals.Bismuth.ordinal() || index == Metals.Chromium.ordinal() || index == Metals.Copper.ordinal() || index == Metals.Lead.ordinal() || index == Metals.Manganese.ordinal() || index == Metals.Mercury.ordinal() || index == Metals.Nickel.ordinal() || index == Metals.Tellurium.ordinal() || index == Metals.Indium.ordinal() || index == Metals.Tin.ordinal() || index == Metals.Zinc.ordinal();
    }

    public static boolean isHeavyMetal(int index) {
        return index == Metals.Bismuth.ordinal() || index == Metals.Chromium.ordinal() || index == Metals.Copper.ordinal() || index == Metals.Lead.ordinal() || index == Metals.Mercury.ordinal() || index == Metals.Nickel.ordinal() || index == Metals.Tin.ordinal();
    }

    /**
     * Calculate number/amount of exploitable deposits
     * @param planet
     */
    public static int[] calculateMetals(Solar planet, Random rng) {
        int[] deposits = new int[PlanetResources.Metals.values().length];
        switch(planet.ptype) {
            case BARE_ROCK:
                deposits[PlanetResources.Metals.Chromium.ordinal()] = rng.nextInt(300) / 100;
                deposits[PlanetResources.Metals.Copper.ordinal()] = rng.nextInt(400) / 100;
                deposits[PlanetResources.Metals.Gold.ordinal()] = rng.nextInt(150) / 100;
                deposits[PlanetResources.Metals.Lead.ordinal()] = rng.nextInt(300) / 100;
                deposits[PlanetResources.Metals.Manganese.ordinal()] = rng.nextInt(400) / 100;
                deposits[PlanetResources.Metals.Platinum.ordinal()] = rng.nextInt(150) / 100;
                deposits[PlanetResources.Metals.Silver.ordinal()] = rng.nextInt(200) / 100;
                deposits[PlanetResources.Metals.Tin.ordinal()] = rng.nextInt(300) / 100;
                deposits[PlanetResources.Metals.Zinc.ordinal()] = rng.nextInt(300) / 100;
                deposits[PlanetResources.Metals.Iron.ordinal()] = rng.nextInt(600) / 100;
                deposits[PlanetResources.Metals.Nickel.ordinal()] = rng.nextInt(500) / 100;
                deposits[PlanetResources.Metals.Mercury.ordinal()] = rng.nextInt(120) / 100;
                deposits[PlanetResources.Metals.Iridium.ordinal()] = rng.nextInt(103) / 100;
                deposits[PlanetResources.Metals.Osmium.ordinal()] = rng.nextInt(103) / 100;
                deposits[PlanetResources.Metals.Palladium.ordinal()] = rng.nextInt(103) / 100;
                deposits[PlanetResources.Metals.Rhodium.ordinal()] = rng.nextInt(103) / 100;
                deposits[PlanetResources.Metals.Ruthenium.ordinal()] = rng.nextInt(103) / 100;
                deposits[PlanetResources.Metals.Bismuth.ordinal()] = rng.nextInt(103) / 100;
                deposits[PlanetResources.Metals.Indium.ordinal()] = rng.nextInt(101) / 100;
                deposits[PlanetResources.Metals.Tellurium.ordinal()] = rng.nextInt(101) / 100;
                break;
            case ATM_ROCK:
                deposits[PlanetResources.Metals.Chromium.ordinal()] = rng.nextInt(150) / 100;
                deposits[PlanetResources.Metals.Copper.ordinal()] = rng.nextInt(250) / 100;
                deposits[PlanetResources.Metals.Gold.ordinal()] = rng.nextInt(130) / 100;
                deposits[PlanetResources.Metals.Lead.ordinal()] = rng.nextInt(201) / 100;
                deposits[PlanetResources.Metals.Manganese.ordinal()] = rng.nextInt(330) / 100;
                deposits[PlanetResources.Metals.Platinum.ordinal()] = rng.nextInt(150) / 100;
                deposits[PlanetResources.Metals.Silver.ordinal()] = rng.nextInt(180) / 100;
                deposits[PlanetResources.Metals.Tin.ordinal()] = rng.nextInt(210) / 100;
                deposits[PlanetResources.Metals.Zinc.ordinal()] = rng.nextInt(210) / 100;
                deposits[PlanetResources.Metals.Iron.ordinal()] = rng.nextInt(250) / 100;
                deposits[PlanetResources.Metals.Nickel.ordinal()] = rng.nextInt(250) / 100;
                deposits[PlanetResources.Metals.Mercury.ordinal()] = rng.nextInt(110) / 100;
                deposits[PlanetResources.Metals.Iridium.ordinal()] = rng.nextInt(103) / 100;
                deposits[PlanetResources.Metals.Osmium.ordinal()] = rng.nextInt(103) / 100;
                deposits[PlanetResources.Metals.Palladium.ordinal()] = rng.nextInt(103) / 100;
                deposits[PlanetResources.Metals.Rhodium.ordinal()] = rng.nextInt(103) / 100;
                deposits[PlanetResources.Metals.Ruthenium.ordinal()] = rng.nextInt(103) / 100;
                deposits[PlanetResources.Metals.Bismuth.ordinal()] = rng.nextInt(103) / 100;
                deposits[PlanetResources.Metals.Indium.ordinal()] = rng.nextInt(101) / 100;
                deposits[PlanetResources.Metals.Tellurium.ordinal()] = rng.nextInt(101) / 100;
                break;
            case CLOUD:
                deposits[PlanetResources.Metals.Chromium.ordinal()] = rng.nextInt(110) / 100;
                deposits[PlanetResources.Metals.Copper.ordinal()] = rng.nextInt(200) / 100;
                deposits[PlanetResources.Metals.Gold.ordinal()] = rng.nextInt(150) / 100;
                deposits[PlanetResources.Metals.Lead.ordinal()] = rng.nextInt(150) / 100;
                deposits[PlanetResources.Metals.Manganese.ordinal()] = rng.nextInt(200) / 100;
                deposits[PlanetResources.Metals.Platinum.ordinal()] = rng.nextInt(150) / 100;
                deposits[PlanetResources.Metals.Silver.ordinal()] = rng.nextInt(170) / 100;
                deposits[PlanetResources.Metals.Tin.ordinal()] = rng.nextInt(150) / 100;
                deposits[PlanetResources.Metals.Zinc.ordinal()] = rng.nextInt(101) / 100;
                deposits[PlanetResources.Metals.Iron.ordinal()] = rng.nextInt(110) / 100;
                deposits[PlanetResources.Metals.Nickel.ordinal()] = rng.nextInt(110) / 100;
                deposits[PlanetResources.Metals.Mercury.ordinal()] = rng.nextInt(105) / 100;
                deposits[PlanetResources.Metals.Iridium.ordinal()] = rng.nextInt(101) / 100;
                deposits[PlanetResources.Metals.Osmium.ordinal()] = rng.nextInt(101) / 100;
                deposits[PlanetResources.Metals.Palladium.ordinal()] = rng.nextInt(101) / 100;
                deposits[PlanetResources.Metals.Rhodium.ordinal()] = rng.nextInt(101) / 100;
                deposits[PlanetResources.Metals.Ruthenium.ordinal()] = rng.nextInt(101) / 100;
                deposits[PlanetResources.Metals.Bismuth.ordinal()] = rng.nextInt(101) / 100;
                deposits[PlanetResources.Metals.Indium.ordinal()] = rng.nextInt(101) / 100;
                deposits[PlanetResources.Metals.Tellurium.ordinal()] = rng.nextInt(101) / 100;
                break;
            case EARTH:
                deposits[PlanetResources.Metals.Chromium.ordinal()] = 0;
                deposits[PlanetResources.Metals.Copper.ordinal()] = rng.nextInt(12) / 5;
                deposits[PlanetResources.Metals.Gold.ordinal()] = rng.nextInt(6) / 5;
                deposits[PlanetResources.Metals.Lead.ordinal()] = rng.nextInt(12) / 5;
                deposits[PlanetResources.Metals.Manganese.ordinal()] = rng.nextInt(6) / 5;
                deposits[PlanetResources.Metals.Platinum.ordinal()] = rng.nextInt(6) / 5;
                deposits[PlanetResources.Metals.Silver.ordinal()] = rng.nextInt(8) / 5;
                deposits[PlanetResources.Metals.Tin.ordinal()] = rng.nextInt(12) / 5;
                deposits[PlanetResources.Metals.Iridium.ordinal()] = rng.nextInt(101) / 100;
                deposits[PlanetResources.Metals.Osmium.ordinal()] = rng.nextInt(101) / 100;
                deposits[PlanetResources.Metals.Palladium.ordinal()] = rng.nextInt(101) / 100;
                deposits[PlanetResources.Metals.Rhodium.ordinal()] = rng.nextInt(101) / 100;
                deposits[PlanetResources.Metals.Ruthenium.ordinal()] = rng.nextInt(101) / 100;
                deposits[PlanetResources.Metals.Bismuth.ordinal()] = rng.nextInt(101) / 100;
                break;
            case ICE:
                deposits[PlanetResources.Metals.Chromium.ordinal()] = rng.nextInt(21) / 20;
                deposits[PlanetResources.Metals.Copper.ordinal()] = rng.nextInt(21) / 20;
                deposits[PlanetResources.Metals.Gold.ordinal()] = rng.nextInt(21) / 20;
                deposits[PlanetResources.Metals.Lead.ordinal()] = rng.nextInt(21) / 20;
                deposits[PlanetResources.Metals.Manganese.ordinal()] = rng.nextInt(21) / 20;
                deposits[PlanetResources.Metals.Platinum.ordinal()] = rng.nextInt(21) / 20;
                deposits[PlanetResources.Metals.Silver.ordinal()] = rng.nextInt(21) / 20;
                deposits[PlanetResources.Metals.Tin.ordinal()] = rng.nextInt(21) / 20;
                deposits[PlanetResources.Metals.Iridium.ordinal()] = rng.nextInt(201) / 200;
                deposits[PlanetResources.Metals.Osmium.ordinal()] = rng.nextInt(201) / 200;
                deposits[PlanetResources.Metals.Palladium.ordinal()] = rng.nextInt(201) / 200;
                deposits[PlanetResources.Metals.Rhodium.ordinal()] = rng.nextInt(201) / 200;
                deposits[PlanetResources.Metals.Ruthenium.ordinal()] = rng.nextInt(201) / 200;
                deposits[PlanetResources.Metals.Bismuth.ordinal()] = rng.nextInt(201) / 200;
                deposits[PlanetResources.Metals.Indium.ordinal()] = rng.nextInt(201) / 200;
                deposits[PlanetResources.Metals.Tellurium.ordinal()] = rng.nextInt(201) / 200;
                break;
            default:
        }
        final int promotionFactor = calculateRichness(planet);
        for (int i = 0; i < deposits.length; i++) {
            if (deposits[i] != 0) {
                int rich = rng.nextInt(promotionFactor + 1);
                rich += deposits[i] - 1;
                deposits[i] = rich;
            }
        }
        return deposits;
    }

    /**
     * Calculate atmosphere composition for a planet.
     * @param planet
     */
    public static int[] calculateAtmosphere(Solar planet, Random rng) {
        int[] weights = new int[PlanetResources.Gases.values().length];
        switch(planet.ptype) {
            case BARE_ROCK:
                break;
            case ICE:
                break;
            case ATM_ROCK:
                weights[PlanetResources.Gases.Ammonia.ordinal()] = rng.nextInt(5);
                weights[PlanetResources.Gases.CarbonDioxide.ordinal()] = rng.nextInt(5);
                weights[PlanetResources.Gases.Helium.ordinal()] = rng.nextInt(1);
                weights[PlanetResources.Gases.Hydrogen.ordinal()] = rng.nextInt(7);
                weights[PlanetResources.Gases.Nitrogen.ordinal()] = rng.nextInt(5);
                weights[PlanetResources.Gases.Oxygen.ordinal()] = rng.nextInt(1);
                weights[PlanetResources.Gases.WaterVapor.ordinal()] = rng.nextInt(1);
                weights[PlanetResources.Gases.Methane.ordinal()] = 2 + rng.nextInt(5);
                break;
            case CLOUD:
                weights[PlanetResources.Gases.Ammonia.ordinal()] = 3 + rng.nextInt(15);
                weights[PlanetResources.Gases.CarbonDioxide.ordinal()] = 30 + rng.nextInt(50);
                weights[PlanetResources.Gases.Helium.ordinal()] = rng.nextInt(1);
                weights[PlanetResources.Gases.Hydrogen.ordinal()] = rng.nextInt(7);
                weights[PlanetResources.Gases.Nitrogen.ordinal()] = 30 + rng.nextInt(50);
                weights[PlanetResources.Gases.Oxygen.ordinal()] = 1 + rng.nextInt(1);
                weights[PlanetResources.Gases.WaterVapor.ordinal()] = 30 + rng.nextInt(50);
                weights[PlanetResources.Gases.Methane.ordinal()] = 2 + rng.nextInt(5);
                break;
            case EARTH:
                weights[PlanetResources.Gases.Ammonia.ordinal()] = rng.nextInt(2);
                weights[PlanetResources.Gases.CarbonDioxide.ordinal()] = 1 + rng.nextInt(5);
                weights[PlanetResources.Gases.Helium.ordinal()] = rng.nextInt(3);
                weights[PlanetResources.Gases.Hydrogen.ordinal()] = rng.nextInt(1);
                weights[PlanetResources.Gases.Nitrogen.ordinal()] = 60 + rng.nextInt(50);
                weights[PlanetResources.Gases.Oxygen.ordinal()] = 12 + rng.nextInt(20);
                weights[PlanetResources.Gases.WaterVapor.ordinal()] = 5 + rng.nextInt(10);
                weights[PlanetResources.Gases.Methane.ordinal()] = 1 + rng.nextInt(2);
                break;
            case RINGS:
                weights[PlanetResources.Gases.Ammonia.ordinal()] = 1 + rng.nextInt(3);
                weights[PlanetResources.Gases.CarbonDioxide.ordinal()] = rng.nextInt(2);
                weights[PlanetResources.Gases.Helium.ordinal()] = 2 + rng.nextInt(5);
                weights[PlanetResources.Gases.Hydrogen.ordinal()] = 80 + rng.nextInt(20);
                weights[PlanetResources.Gases.Nitrogen.ordinal()] = rng.nextInt(1);
                weights[PlanetResources.Gases.Oxygen.ordinal()] = rng.nextInt(1);
                weights[PlanetResources.Gases.WaterVapor.ordinal()] = 1 + rng.nextInt(3);
                weights[PlanetResources.Gases.Methane.ordinal()] = 2 + rng.nextInt(8);
                break;
            case SMALL_GAS:
                weights[PlanetResources.Gases.Ammonia.ordinal()] = rng.nextInt(5);
                weights[PlanetResources.Gases.CarbonDioxide.ordinal()] = rng.nextInt(5);
                weights[PlanetResources.Gases.Helium.ordinal()] = 12 + rng.nextInt(25);
                weights[PlanetResources.Gases.Hydrogen.ordinal()] = 50 + rng.nextInt(50);
                weights[PlanetResources.Gases.Nitrogen.ordinal()] = rng.nextInt(1);
                weights[PlanetResources.Gases.Oxygen.ordinal()] = rng.nextInt(1);
                weights[PlanetResources.Gases.WaterVapor.ordinal()] = 1 + rng.nextInt(8);
                weights[PlanetResources.Gases.Methane.ordinal()] = 1 + rng.nextInt(8);
                break;
            case BIG_GAS:
                weights[PlanetResources.Gases.Ammonia.ordinal()] = rng.nextInt(2);
                weights[PlanetResources.Gases.CarbonDioxide.ordinal()] = rng.nextInt(2);
                weights[PlanetResources.Gases.Helium.ordinal()] = 12 + rng.nextInt(25);
                weights[PlanetResources.Gases.Hydrogen.ordinal()] = 50 + rng.nextInt(50);
                weights[PlanetResources.Gases.Nitrogen.ordinal()] = rng.nextInt(1);
                weights[PlanetResources.Gases.Oxygen.ordinal()] = rng.nextInt(1);
                weights[PlanetResources.Gases.WaterVapor.ordinal()] = rng.nextInt(2);
                weights[PlanetResources.Gases.Methane.ordinal()] = rng.nextInt(2);
                break;
            default:
                weights[PlanetResources.Gases.Ammonia.ordinal()] = rng.nextInt(50);
                weights[PlanetResources.Gases.CarbonDioxide.ordinal()] = rng.nextInt(50);
                weights[PlanetResources.Gases.Helium.ordinal()] = rng.nextInt(50);
                weights[PlanetResources.Gases.Hydrogen.ordinal()] = rng.nextInt(50);
                weights[PlanetResources.Gases.Nitrogen.ordinal()] = rng.nextInt(50);
                weights[PlanetResources.Gases.Oxygen.ordinal()] = rng.nextInt(2);
                weights[PlanetResources.Gases.WaterVapor.ordinal()] = rng.nextInt(5);
                weights[PlanetResources.Gases.Methane.ordinal()] = rng.nextInt(5);
        }
        if (planet.eet < 270) {
            weights[PlanetResources.Gases.WaterVapor.ordinal()] /= 10;
        } else if (planet.eet < 300) {
            weights[PlanetResources.Gases.WaterVapor.ordinal()] /= 5;
        }
        return weights;
    }

    /**
     * Calculate number/amount of exploitable mineral deposits
     * @param planet
     */
    public static int[] calculateMinerals(Solar planet, Random rng) {
        int[] deposits = new int[PlanetResources.Minerals.values().length];
        switch(planet.ptype) {
            case BARE_ROCK:
                deposits[PlanetResources.Minerals.Kaolinite.ordinal()] = rng.nextInt(401) / 100;
                deposits[PlanetResources.Minerals.Cerium.ordinal()] = rng.nextInt(205) / 100;
                deposits[PlanetResources.Minerals.Dysprosium.ordinal()] = rng.nextInt(205) / 100;
                deposits[PlanetResources.Minerals.Erbium.ordinal()] = rng.nextInt(205) / 100;
                deposits[PlanetResources.Minerals.Europium.ordinal()] = rng.nextInt(205) / 100;
                deposits[PlanetResources.Minerals.Gadolinium.ordinal()] = rng.nextInt(205) / 100;
                deposits[PlanetResources.Minerals.Holmium.ordinal()] = rng.nextInt(205) / 100;
                deposits[PlanetResources.Minerals.Lanthanum.ordinal()] = rng.nextInt(205) / 100;
                deposits[PlanetResources.Minerals.Lutetium.ordinal()] = rng.nextInt(205) / 100;
                deposits[PlanetResources.Minerals.Neodymium.ordinal()] = rng.nextInt(205) / 100;
                deposits[PlanetResources.Minerals.Praseodymium.ordinal()] = rng.nextInt(205) / 100;
                deposits[PlanetResources.Minerals.Promethium.ordinal()] = rng.nextInt(205) / 100;
                deposits[PlanetResources.Minerals.Samarium.ordinal()] = rng.nextInt(205) / 100;
                deposits[PlanetResources.Minerals.Scandium.ordinal()] = rng.nextInt(205) / 100;
                deposits[PlanetResources.Minerals.Terbium.ordinal()] = rng.nextInt(205) / 100;
                deposits[PlanetResources.Minerals.Thulium.ordinal()] = rng.nextInt(205) / 100;
                deposits[PlanetResources.Minerals.Ytterbium.ordinal()] = rng.nextInt(205) / 100;
                deposits[PlanetResources.Minerals.Yttrium.ordinal()] = rng.nextInt(205) / 100;
                break;
            case ATM_ROCK:
                deposits[PlanetResources.Minerals.Kaolinite.ordinal()] = rng.nextInt(401) / 100;
                deposits[PlanetResources.Minerals.Cerium.ordinal()] = rng.nextInt(105) / 100;
                deposits[PlanetResources.Minerals.Dysprosium.ordinal()] = rng.nextInt(105) / 100;
                deposits[PlanetResources.Minerals.Erbium.ordinal()] = rng.nextInt(105) / 100;
                deposits[PlanetResources.Minerals.Europium.ordinal()] = rng.nextInt(105) / 100;
                deposits[PlanetResources.Minerals.Gadolinium.ordinal()] = rng.nextInt(105) / 100;
                deposits[PlanetResources.Minerals.Holmium.ordinal()] = rng.nextInt(105) / 100;
                deposits[PlanetResources.Minerals.Lanthanum.ordinal()] = rng.nextInt(105) / 100;
                deposits[PlanetResources.Minerals.Lutetium.ordinal()] = rng.nextInt(105) / 100;
                deposits[PlanetResources.Minerals.Neodymium.ordinal()] = rng.nextInt(105) / 100;
                deposits[PlanetResources.Minerals.Praseodymium.ordinal()] = rng.nextInt(105) / 100;
                deposits[PlanetResources.Minerals.Promethium.ordinal()] = rng.nextInt(105) / 100;
                deposits[PlanetResources.Minerals.Samarium.ordinal()] = rng.nextInt(105) / 100;
                deposits[PlanetResources.Minerals.Scandium.ordinal()] = rng.nextInt(105) / 100;
                deposits[PlanetResources.Minerals.Terbium.ordinal()] = rng.nextInt(105) / 100;
                deposits[PlanetResources.Minerals.Thulium.ordinal()] = rng.nextInt(105) / 100;
                deposits[PlanetResources.Minerals.Ytterbium.ordinal()] = rng.nextInt(105) / 100;
                deposits[PlanetResources.Minerals.Yttrium.ordinal()] = rng.nextInt(105) / 100;
                break;
            case CLOUD:
                deposits[PlanetResources.Minerals.Kaolinite.ordinal()] = rng.nextInt(201) / 100;
                deposits[PlanetResources.Minerals.Cerium.ordinal()] = rng.nextInt(103) / 100;
                deposits[PlanetResources.Minerals.Dysprosium.ordinal()] = rng.nextInt(103) / 100;
                deposits[PlanetResources.Minerals.Erbium.ordinal()] = rng.nextInt(103) / 100;
                deposits[PlanetResources.Minerals.Europium.ordinal()] = rng.nextInt(103) / 100;
                deposits[PlanetResources.Minerals.Gadolinium.ordinal()] = rng.nextInt(103) / 100;
                deposits[PlanetResources.Minerals.Holmium.ordinal()] = rng.nextInt(103) / 100;
                deposits[PlanetResources.Minerals.Lanthanum.ordinal()] = rng.nextInt(103) / 100;
                deposits[PlanetResources.Minerals.Lutetium.ordinal()] = rng.nextInt(103) / 100;
                deposits[PlanetResources.Minerals.Neodymium.ordinal()] = rng.nextInt(103) / 100;
                deposits[PlanetResources.Minerals.Praseodymium.ordinal()] = rng.nextInt(103) / 100;
                deposits[PlanetResources.Minerals.Promethium.ordinal()] = rng.nextInt(103) / 100;
                deposits[PlanetResources.Minerals.Samarium.ordinal()] = rng.nextInt(103) / 100;
                deposits[PlanetResources.Minerals.Scandium.ordinal()] = rng.nextInt(103) / 100;
                deposits[PlanetResources.Minerals.Terbium.ordinal()] = rng.nextInt(103) / 100;
                deposits[PlanetResources.Minerals.Thulium.ordinal()] = rng.nextInt(103) / 100;
                deposits[PlanetResources.Minerals.Ytterbium.ordinal()] = rng.nextInt(103) / 100;
                deposits[PlanetResources.Minerals.Yttrium.ordinal()] = rng.nextInt(103) / 100;
                break;
            case EARTH:
                deposits[PlanetResources.Minerals.Kaolinite.ordinal()] = rng.nextInt(401) / 100;
                deposits[PlanetResources.Minerals.Cerium.ordinal()] = rng.nextInt(103) / 100;
                deposits[PlanetResources.Minerals.Dysprosium.ordinal()] = rng.nextInt(103) / 100;
                deposits[PlanetResources.Minerals.Erbium.ordinal()] = rng.nextInt(103) / 100;
                deposits[PlanetResources.Minerals.Europium.ordinal()] = rng.nextInt(103) / 100;
                deposits[PlanetResources.Minerals.Gadolinium.ordinal()] = rng.nextInt(103) / 100;
                deposits[PlanetResources.Minerals.Holmium.ordinal()] = rng.nextInt(103) / 100;
                deposits[PlanetResources.Minerals.Lanthanum.ordinal()] = rng.nextInt(103) / 100;
                deposits[PlanetResources.Minerals.Lutetium.ordinal()] = rng.nextInt(103) / 100;
                deposits[PlanetResources.Minerals.Neodymium.ordinal()] = rng.nextInt(103) / 100;
                deposits[PlanetResources.Minerals.Praseodymium.ordinal()] = rng.nextInt(103) / 100;
                deposits[PlanetResources.Minerals.Promethium.ordinal()] = rng.nextInt(103) / 100;
                deposits[PlanetResources.Minerals.Samarium.ordinal()] = rng.nextInt(103) / 100;
                deposits[PlanetResources.Minerals.Scandium.ordinal()] = rng.nextInt(103) / 100;
                deposits[PlanetResources.Minerals.Terbium.ordinal()] = rng.nextInt(103) / 100;
                deposits[PlanetResources.Minerals.Thulium.ordinal()] = rng.nextInt(103) / 100;
                deposits[PlanetResources.Minerals.Ytterbium.ordinal()] = rng.nextInt(103) / 100;
                deposits[PlanetResources.Minerals.Yttrium.ordinal()] = rng.nextInt(103) / 100;
                break;
            case ICE:
                deposits[PlanetResources.Minerals.Kaolinite.ordinal()] = rng.nextInt(101) / 100;
                deposits[PlanetResources.Minerals.Cerium.ordinal()] = rng.nextInt(301) / 300;
                deposits[PlanetResources.Minerals.Dysprosium.ordinal()] = rng.nextInt(301) / 300;
                deposits[PlanetResources.Minerals.Erbium.ordinal()] = rng.nextInt(301) / 300;
                deposits[PlanetResources.Minerals.Europium.ordinal()] = rng.nextInt(301) / 300;
                deposits[PlanetResources.Minerals.Gadolinium.ordinal()] = rng.nextInt(301) / 300;
                deposits[PlanetResources.Minerals.Holmium.ordinal()] = rng.nextInt(301) / 300;
                deposits[PlanetResources.Minerals.Lanthanum.ordinal()] = rng.nextInt(301) / 300;
                deposits[PlanetResources.Minerals.Lutetium.ordinal()] = rng.nextInt(301) / 300;
                deposits[PlanetResources.Minerals.Neodymium.ordinal()] = rng.nextInt(301) / 300;
                deposits[PlanetResources.Minerals.Praseodymium.ordinal()] = rng.nextInt(301) / 300;
                deposits[PlanetResources.Minerals.Promethium.ordinal()] = rng.nextInt(301) / 300;
                deposits[PlanetResources.Minerals.Samarium.ordinal()] = rng.nextInt(301) / 300;
                deposits[PlanetResources.Minerals.Scandium.ordinal()] = rng.nextInt(301) / 300;
                deposits[PlanetResources.Minerals.Terbium.ordinal()] = rng.nextInt(301) / 300;
                deposits[PlanetResources.Minerals.Thulium.ordinal()] = rng.nextInt(301) / 300;
                deposits[PlanetResources.Minerals.Ytterbium.ordinal()] = rng.nextInt(301) / 300;
                deposits[PlanetResources.Minerals.Yttrium.ordinal()] = rng.nextInt(301) / 300;
                break;
            default:
        }
        final int promotionFactor = calculateRichness(planet);
        for (int i = 0; i < deposits.length; i++) {
            if (deposits[i] != 0) {
                int rich = rng.nextInt(promotionFactor + 1);
                rich += deposits[i] - 1;
                deposits[i] = rich;
            }
        }
        return deposits;
    }

    /**
     * Mineral and metal deposits can be promoted to the next bigger
     * size if the planet is a rich planet
     * @param planet
     * @return promotion factor, 0 for normal planets
     */
    private static int calculateRichness(Solar planet) {
        int promotionFactor = 0;
        switch(planet.ptype) {
            case BARE_ROCK:
                promotionFactor = planet.radius / 1500;
                break;
            case ATM_ROCK:
                promotionFactor = planet.radius / 3000;
                break;
            case CLOUD:
                promotionFactor = planet.radius / 5000;
                break;
            case EARTH:
                promotionFactor = planet.radius / 6000;
                break;
            case ICE:
                promotionFactor = planet.radius / 5000;
                break;
            case RINGS:
            case SMALL_GAS:
                promotionFactor = planet.radius / 15000;
                break;
            case BIG_GAS:
                promotionFactor = planet.radius / 40000;
                break;
            default:
                break;
        }
        return promotionFactor;
    }

    public static Random getPlanetRng(Solar planet) {
        return new Random(planet.seed);
    }

    public static void main(String[] args) {
        System.err.println(":" + Gases.Oxygen);
        System.err.println(":" + Gases.Nitrogen);
        System.err.println(":" + Gases.CarbonDioxide);
    }
}
