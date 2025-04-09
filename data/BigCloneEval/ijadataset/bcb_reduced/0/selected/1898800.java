package common;

import java.io.File;
import java.io.IOException;
import java.util.StringTokenizer;
import common.util.BinReader;
import common.util.BinWriter;

/**
 * A Planet's Environment.
 * 
 * Final simple because you should be aware to overwrite binIn and binOut properly 
 * if you subclass PlanetEnvironment.
 */
public final class PlanetEnvironment {

    private int id = -1;

    private String Name = "";

    private int CraterProb = 0;

    private int CraterMinNum = 0;

    private int CraterMaxNum = 0;

    private int CraterMinRadius = 0;

    private int CraterMaxRadius = 0;

    private int Hillyness = 100;

    private int HillElevationRange = 3;

    private int HillInvertProb = 0;

    private int WaterMinSpots = 3;

    private int WaterMaxSpots = 8;

    private int WaterMinHexes = 2;

    private int WaterMaxHexes = 10;

    private int WaterDeepProb = 20;

    private int ForestMinSpots = 4;

    private int ForestMaxSpots = 8;

    private int ForestMinHexes = 2;

    private int ForestMaxHexes = 6;

    private int ForestHeavyProb = 20;

    private int RoughMinSpots = 0;

    private int RoughMaxSpots = 5;

    private int RoughMinHexes = 1;

    private int RoughMaxHexes = 2;

    private int SwampMinSpots = 0;

    private int SwampMaxSpots = 0;

    private int SwampMinHexes = 0;

    private int SwampMaxHexes = 0;

    private int PavementMinSpots = 0;

    private int PavementMaxSpots = 0;

    private int PavementMinHexes = 0;

    private int PavementMaxHexes = 0;

    private int IceMinSpots = 0;

    private int IceMaxSpots = 0;

    private int IceMinHexes = 0;

    private int IceMaxHexes = 0;

    private int RubbleMinSpots = 0;

    private int RubbleMaxSpots = 0;

    private int RubbleMinHexes = 0;

    private int RubbleMaxHexes = 0;

    private int FortifiedMinSpots = 0;

    private int FortifiedMaxSpots = 0;

    private int FortifiedMinHexes = 0;

    private int FortifiedMaxHexes = 0;

    private int MinBuildings = 0;

    private int MaxBuildings = 0;

    private int MinCF = 0;

    private int MaxCF = 0;

    private int MinFloors = 0;

    private int MaxFloors = 0;

    private int CityDensity = 50;

    private String CityType = "NONE";

    private int Roads = 4;

    private int TownSize = 0;

    private int fxMod = 0;

    private int probForestFire = 0;

    private int probFreeze = 0;

    private int probFlood = 0;

    private int probDrought = 0;

    private String Theme = "";

    private int MountPeaks = 0;

    private int MountWidthMin = 0;

    private int MountWidthMax = 0;

    private int MountHeightMin = 0;

    private int MountHeightMax = 0;

    private int MountStyle = 0;

    private int RoadProb = 25;

    private int RiverProb = 25;

    private int Algorithm = 0;

    private int CliffProb = 0;

    private int InvertNegativeTerrain = 0;

    private int EnvironmentProb = 1;

    private int minSandSpots = 0;

    private int maxSandSpots = 0;

    private int minSandSize = 1;

    private int maxSandSize = 4;

    private int minPlantedFieldSpots = 0;

    private int maxPlantedFieldSpots = 0;

    private int minPlantedFieldSize = 1;

    private int maxPlantedFieldSize = 4;

    /**
     * For Serialisation.
     */
    public PlanetEnvironment() {
    }

    public PlanetEnvironment(String s) {
        StringTokenizer ST = new StringTokenizer(s, "$");
        ST.nextToken();
        Name = ST.nextToken();
        CraterProb = Integer.parseInt(ST.nextToken());
        CraterMinNum = Integer.parseInt(ST.nextToken());
        CraterMaxNum = Integer.parseInt(ST.nextToken());
        CraterMinRadius = Integer.parseInt(ST.nextToken());
        CraterMaxRadius = Integer.parseInt(ST.nextToken());
        Hillyness = Integer.parseInt(ST.nextToken());
        HillElevationRange = Integer.parseInt(ST.nextToken());
        HillInvertProb = Integer.parseInt(ST.nextToken());
        WaterMinSpots = Integer.parseInt(ST.nextToken());
        WaterMaxSpots = Integer.parseInt(ST.nextToken());
        WaterMinHexes = Integer.parseInt(ST.nextToken());
        WaterMaxHexes = Integer.parseInt(ST.nextToken());
        WaterDeepProb = Integer.parseInt(ST.nextToken());
        ForestMinSpots = Integer.parseInt(ST.nextToken());
        ForestMaxSpots = Integer.parseInt(ST.nextToken());
        ForestMinHexes = Integer.parseInt(ST.nextToken());
        ForestMaxHexes = Integer.parseInt(ST.nextToken());
        ForestHeavyProb = Integer.parseInt(ST.nextToken());
        RoughMinSpots = Integer.parseInt(ST.nextToken());
        RoughMaxSpots = Integer.parseInt(ST.nextToken());
        RoughMinHexes = Integer.parseInt(ST.nextToken());
        RoughMaxHexes = Integer.parseInt(ST.nextToken());
        RoadProb = Integer.parseInt(ST.nextToken());
        RiverProb = Integer.parseInt(ST.nextToken());
        Algorithm = Integer.parseInt(ST.nextToken());
        if (ST.hasMoreTokens()) id = Integer.parseInt(ST.nextToken());
        if (ST.hasMoreTokens()) SwampMinSpots = Integer.parseInt(ST.nextToken());
        if (ST.hasMoreTokens()) SwampMaxSpots = Integer.parseInt(ST.nextToken());
        if (ST.hasMoreTokens()) SwampMinHexes = Integer.parseInt(ST.nextToken());
        if (ST.hasMoreTokens()) SwampMaxHexes = Integer.parseInt(ST.nextToken());
        if (ST.hasMoreTokens()) PavementMinSpots = Integer.parseInt(ST.nextToken());
        if (ST.hasMoreTokens()) PavementMaxSpots = Integer.parseInt(ST.nextToken());
        if (ST.hasMoreTokens()) PavementMinHexes = Integer.parseInt(ST.nextToken());
        if (ST.hasMoreTokens()) PavementMaxHexes = Integer.parseInt(ST.nextToken());
        if (ST.hasMoreTokens()) fxMod = Integer.parseInt(ST.nextToken());
        if (ST.hasMoreTokens()) probForestFire = Integer.parseInt(ST.nextToken());
        if (ST.hasMoreTokens()) probFreeze = Integer.parseInt(ST.nextToken());
        if (ST.hasMoreTokens()) probFlood = Integer.parseInt(ST.nextToken());
        if (ST.hasMoreTokens()) probDrought = Integer.parseInt(ST.nextToken());
        if (ST.hasMoreTokens()) Theme = ST.nextToken();
        if (ST.hasMoreTokens()) IceMinSpots = Integer.parseInt(ST.nextToken());
        if (ST.hasMoreTokens()) IceMaxSpots = Integer.parseInt(ST.nextToken());
        if (ST.hasMoreTokens()) IceMinHexes = Integer.parseInt(ST.nextToken());
        if (ST.hasMoreTokens()) IceMaxHexes = Integer.parseInt(ST.nextToken());
        if (ST.hasMoreTokens()) RubbleMinSpots = Integer.parseInt(ST.nextToken());
        if (ST.hasMoreTokens()) RubbleMaxSpots = Integer.parseInt(ST.nextToken());
        if (ST.hasMoreTokens()) RubbleMinHexes = Integer.parseInt(ST.nextToken());
        if (ST.hasMoreTokens()) RubbleMaxHexes = Integer.parseInt(ST.nextToken());
        if (ST.hasMoreTokens()) FortifiedMinSpots = Integer.parseInt(ST.nextToken());
        if (ST.hasMoreTokens()) FortifiedMaxSpots = Integer.parseInt(ST.nextToken());
        if (ST.hasMoreTokens()) FortifiedMinHexes = Integer.parseInt(ST.nextToken());
        if (ST.hasMoreTokens()) FortifiedMaxHexes = Integer.parseInt(ST.nextToken());
        if (ST.hasMoreTokens()) MinBuildings = Integer.parseInt(ST.nextToken());
        if (ST.hasMoreTokens()) MaxBuildings = Integer.parseInt(ST.nextToken());
        if (ST.hasMoreTokens()) MinCF = Integer.parseInt(ST.nextToken());
        if (ST.hasMoreTokens()) MaxCF = Integer.parseInt(ST.nextToken());
        if (ST.hasMoreTokens()) MinFloors = Integer.parseInt(ST.nextToken());
        if (ST.hasMoreTokens()) MaxFloors = Integer.parseInt(ST.nextToken());
        if (ST.hasMoreTokens()) CityDensity = Integer.parseInt(ST.nextToken());
        if (ST.hasMoreTokens()) CityType = ST.nextToken();
        if (ST.hasMoreTokens()) Roads = Integer.parseInt(ST.nextToken());
        if (ST.hasMoreElements()) CliffProb = Integer.parseInt(ST.nextToken());
        if (ST.hasMoreElements()) InvertNegativeTerrain = Integer.parseInt(ST.nextToken());
        if (ST.hasMoreElements()) TownSize = Integer.parseInt(ST.nextToken());
        if (ST.hasMoreElements()) MountPeaks = Integer.parseInt(ST.nextToken());
        if (ST.hasMoreElements()) MountWidthMin = Integer.parseInt(ST.nextToken());
        if (ST.hasMoreElements()) MountWidthMax = Integer.parseInt(ST.nextToken());
        if (ST.hasMoreElements()) MountHeightMin = Integer.parseInt(ST.nextToken());
        if (ST.hasMoreElements()) MountHeightMax = Integer.parseInt(ST.nextToken());
        if (ST.hasMoreElements()) MountStyle = Integer.parseInt(ST.nextToken());
        if (ST.hasMoreElements()) EnvironmentProb = Integer.parseInt(ST.nextToken());
        if (ST.hasMoreElements()) {
            minSandSpots = Integer.parseInt(ST.nextToken());
        }
        if (ST.hasMoreElements()) {
            maxSandSpots = Integer.parseInt(ST.nextToken());
        }
        if (ST.hasMoreElements()) {
            minSandSize = Integer.parseInt(ST.nextToken());
        }
        if (ST.hasMoreElements()) {
            maxSandSize = Integer.parseInt(ST.nextToken());
        }
        if (ST.hasMoreElements()) {
            minPlantedFieldSpots = Integer.parseInt(ST.nextToken());
        }
        if (ST.hasMoreElements()) {
            maxPlantedFieldSpots = Integer.parseInt(ST.nextToken());
        }
        if (ST.hasMoreElements()) {
            minPlantedFieldSize = Integer.parseInt(ST.nextToken());
        }
        if (ST.hasMoreElements()) {
            maxPlantedFieldSize = Integer.parseInt(ST.nextToken());
        }
    }

    public PlanetEnvironment(StringTokenizer ST) {
        ST.nextToken();
        Name = ST.nextToken();
        CraterProb = Integer.parseInt(ST.nextToken());
        CraterMinNum = Integer.parseInt(ST.nextToken());
        CraterMaxNum = Integer.parseInt(ST.nextToken());
        CraterMinRadius = Integer.parseInt(ST.nextToken());
        CraterMaxRadius = Integer.parseInt(ST.nextToken());
        Hillyness = Integer.parseInt(ST.nextToken());
        HillElevationRange = Integer.parseInt(ST.nextToken());
        HillInvertProb = Integer.parseInt(ST.nextToken());
        WaterMinSpots = Integer.parseInt(ST.nextToken());
        WaterMaxSpots = Integer.parseInt(ST.nextToken());
        WaterMinHexes = Integer.parseInt(ST.nextToken());
        WaterMaxHexes = Integer.parseInt(ST.nextToken());
        WaterDeepProb = Integer.parseInt(ST.nextToken());
        ForestMinSpots = Integer.parseInt(ST.nextToken());
        ForestMaxSpots = Integer.parseInt(ST.nextToken());
        ForestMinHexes = Integer.parseInt(ST.nextToken());
        ForestMaxHexes = Integer.parseInt(ST.nextToken());
        ForestHeavyProb = Integer.parseInt(ST.nextToken());
        RoughMinSpots = Integer.parseInt(ST.nextToken());
        RoughMaxSpots = Integer.parseInt(ST.nextToken());
        RoughMinHexes = Integer.parseInt(ST.nextToken());
        RoughMaxHexes = Integer.parseInt(ST.nextToken());
        RoadProb = Integer.parseInt(ST.nextToken());
        RiverProb = Integer.parseInt(ST.nextToken());
        Algorithm = Integer.parseInt(ST.nextToken());
        if (ST.hasMoreTokens()) id = Integer.parseInt(ST.nextToken());
        if (ST.hasMoreTokens()) SwampMinSpots = Integer.parseInt(ST.nextToken());
        if (ST.hasMoreTokens()) SwampMaxSpots = Integer.parseInt(ST.nextToken());
        if (ST.hasMoreTokens()) SwampMinHexes = Integer.parseInt(ST.nextToken());
        if (ST.hasMoreTokens()) SwampMaxHexes = Integer.parseInt(ST.nextToken());
        if (ST.hasMoreTokens()) PavementMinSpots = Integer.parseInt(ST.nextToken());
        if (ST.hasMoreTokens()) PavementMaxSpots = Integer.parseInt(ST.nextToken());
        if (ST.hasMoreTokens()) PavementMinHexes = Integer.parseInt(ST.nextToken());
        if (ST.hasMoreTokens()) PavementMaxHexes = Integer.parseInt(ST.nextToken());
        if (ST.hasMoreTokens()) fxMod = Integer.parseInt(ST.nextToken());
        if (ST.hasMoreTokens()) probForestFire = Integer.parseInt(ST.nextToken());
        if (ST.hasMoreTokens()) probFreeze = Integer.parseInt(ST.nextToken());
        if (ST.hasMoreTokens()) probFlood = Integer.parseInt(ST.nextToken());
        if (ST.hasMoreTokens()) probDrought = Integer.parseInt(ST.nextToken());
        if (ST.hasMoreTokens()) Theme = ST.nextToken();
        if (ST.hasMoreTokens()) IceMinSpots = Integer.parseInt(ST.nextToken());
        if (ST.hasMoreTokens()) IceMaxSpots = Integer.parseInt(ST.nextToken());
        if (ST.hasMoreTokens()) IceMinHexes = Integer.parseInt(ST.nextToken());
        if (ST.hasMoreTokens()) IceMaxHexes = Integer.parseInt(ST.nextToken());
        if (ST.hasMoreTokens()) RubbleMinSpots = Integer.parseInt(ST.nextToken());
        if (ST.hasMoreTokens()) RubbleMaxSpots = Integer.parseInt(ST.nextToken());
        if (ST.hasMoreTokens()) RubbleMinHexes = Integer.parseInt(ST.nextToken());
        if (ST.hasMoreTokens()) RubbleMaxHexes = Integer.parseInt(ST.nextToken());
        if (ST.hasMoreTokens()) FortifiedMinSpots = Integer.parseInt(ST.nextToken());
        if (ST.hasMoreTokens()) FortifiedMaxSpots = Integer.parseInt(ST.nextToken());
        if (ST.hasMoreTokens()) FortifiedMinHexes = Integer.parseInt(ST.nextToken());
        if (ST.hasMoreTokens()) FortifiedMaxHexes = Integer.parseInt(ST.nextToken());
        if (ST.hasMoreTokens()) MinBuildings = Integer.parseInt(ST.nextToken());
        if (ST.hasMoreTokens()) MaxBuildings = Integer.parseInt(ST.nextToken());
        if (ST.hasMoreTokens()) MinCF = Integer.parseInt(ST.nextToken());
        if (ST.hasMoreTokens()) MaxCF = Integer.parseInt(ST.nextToken());
        if (ST.hasMoreTokens()) MinFloors = Integer.parseInt(ST.nextToken());
        if (ST.hasMoreTokens()) MaxFloors = Integer.parseInt(ST.nextToken());
        if (ST.hasMoreTokens()) CityDensity = Integer.parseInt(ST.nextToken());
        if (ST.hasMoreTokens()) CityType = ST.nextToken();
        if (ST.hasMoreTokens()) Roads = Integer.parseInt(ST.nextToken());
        if (ST.hasMoreElements()) CliffProb = Integer.parseInt(ST.nextToken());
        if (ST.hasMoreElements()) InvertNegativeTerrain = Integer.parseInt(ST.nextToken());
        if (ST.hasMoreElements()) TownSize = Integer.parseInt(ST.nextToken());
        if (ST.hasMoreElements()) MountPeaks = Integer.parseInt(ST.nextToken());
        if (ST.hasMoreElements()) MountWidthMin = Integer.parseInt(ST.nextToken());
        if (ST.hasMoreElements()) MountWidthMax = Integer.parseInt(ST.nextToken());
        if (ST.hasMoreElements()) MountHeightMin = Integer.parseInt(ST.nextToken());
        if (ST.hasMoreElements()) MountHeightMax = Integer.parseInt(ST.nextToken());
        if (ST.hasMoreElements()) MountStyle = Integer.parseInt(ST.nextToken());
        if (ST.hasMoreElements()) EnvironmentProb = Integer.parseInt(ST.nextToken());
        if (ST.hasMoreElements()) {
            minSandSpots = Integer.parseInt(ST.nextToken());
        }
        if (ST.hasMoreElements()) {
            maxSandSpots = Integer.parseInt(ST.nextToken());
        }
        if (ST.hasMoreElements()) {
            minSandSize = Integer.parseInt(ST.nextToken());
        }
        if (ST.hasMoreElements()) {
            maxSandSize = Integer.parseInt(ST.nextToken());
        }
        if (ST.hasMoreElements()) {
            minPlantedFieldSpots = Integer.parseInt(ST.nextToken());
        }
        if (ST.hasMoreElements()) {
            maxPlantedFieldSpots = Integer.parseInt(ST.nextToken());
        }
        if (ST.hasMoreElements()) {
            minPlantedFieldSize = Integer.parseInt(ST.nextToken());
        }
        if (ST.hasMoreElements()) {
            maxPlantedFieldSize = Integer.parseInt(ST.nextToken());
        }
    }

    public String toDescription() {
        String result = "";
        int water = (((WaterMaxSpots + WaterMinSpots) / 2) * (WaterMinHexes + WaterMaxSpots) / 2) + RiverProb / 10;
        int rough = (((RoughMaxSpots + RoughMinSpots) / 2) * (RoughMinHexes + RoughMaxSpots) / 2);
        int forest = (((ForestMaxSpots + ForestMinSpots) / 2) * (ForestMinHexes + ForestMaxSpots) / 2);
        result += "The landscape is ";
        if (Hillyness < 200) result += "plain";
        if ((Hillyness < 500) && (Hillyness >= 200)) result += "uneven";
        if ((Hillyness >= 500) && (Hillyness <= 800)) result += "hilly";
        if (Hillyness > 800) result += "mountainous";
        if (CraterProb == 0) {
            result += ". <br> ";
            if (rough > 0) {
                result += "Through tectonic activity of this continent, rough terrain is appearing";
                if (rough > 8) result += " everywhere"; else result += " sometimes";
            }
        } else {
            if (CraterProb < 30) result += ", which is seldom coverd with";
            if ((CraterProb >= 30) && (CraterProb < 60)) result += ", which is covered with";
            if (CraterProb >= 60) result += ", often coverd with";
            int avgCraterSize = (CraterMinRadius + CraterMaxRadius) / 2;
            if (avgCraterSize < 4) result += " small craters";
            if ((avgCraterSize >= 4) && (avgCraterSize < 7)) result += " craters ";
            if (avgCraterSize >= 7) result += " large craters";
            if (rough > 0) {
                result += ". Another remaing of the ancient meteorid impacts is the rough terrain appearing";
                if (rough > 8) result += " everywhere"; else result += " sometimes";
            }
        }
        result += ". <br>";
        result += "Most facitlities on this continent are lying";
        if (forest > 50) {
            result += " deep in the ";
            result += (ForestHeavyProb < 30) ? "woods" : "jungle";
            if (water > 20) result += " mixed up with much water, because of heavy rain due too monsoon period";
            result += ".";
        } else {
            if (water > 20) result += " close to the coast."; else if (water < 3) {
                if (forest < 15) result += " in the desert. So dont expect vegetation for cover or water for cooling."; else result += " in an area moderatly forested.";
            } else result += " in an area famous for its agriculture.";
        }
        return result;
    }

    public String toImageDescription() {
        String result = "";
        int water = (((WaterMaxSpots + WaterMinSpots) / 2) * (WaterMinHexes + WaterMaxSpots) / 2) + RiverProb / 10;
        int rough = (((RoughMaxSpots + RoughMinSpots) / 2) * (RoughMinHexes + RoughMaxSpots) / 2);
        int forest = (((ForestMaxSpots + ForestMinSpots) / 2) * (ForestMinHexes + ForestMaxSpots) / 2);
        if (Hillyness < 200) result += "<img src=\"data/images/hill0.gif\">";
        if ((Hillyness < 500) && (Hillyness >= 200)) result += "<img src=\"data/images/hill1.gif\">";
        if ((Hillyness >= 500) && (Hillyness <= 800)) result += "<img src=\"data/images/hill2.gif\">";
        if (Hillyness > 800) result += "<img src=\"data/images/hill3.gif\">";
        if (rough > 8) result += "<img src=\"data/images/roug1.gif\">";
        if (CraterProb > 30) result += "<img src=\"data/images/crtr1.gif\">";
        if (forest > 15 && forest < 30) result += "<img src=\"data/images/wood1.gif\">"; else if (forest >= 30 && forest < 50) result += "<img src=\"data/images/wood2.gif\">"; else if (forest >= 50) result += "<img src=\"data/images/wood3.gif\">";
        if (water > 5 && water < 20) result += "<img src=\"data/images/watr1.gif\">"; else if (water >= 20) result += "<img src=\"data/images/watr2.gif\">";
        if (getRiverProb() > 50) result += "<img src=\"data/images/rivr1.gif\">";
        if (getRoadProb() > 50) result += "<img src=\"data/images/road1.gif\">";
        return result;
    }

    /**
     * TODO: remove this code bloat - make a better way to get the images.
     */
    public String toImageAbsolutePathDescription() {
        String result = "";
        int water = (((WaterMaxSpots + WaterMinSpots) / 2) * (WaterMinHexes + WaterMaxSpots) / 2) + RiverProb / 10;
        int rough = (((RoughMaxSpots + RoughMinSpots) / 2) * (RoughMinHexes + RoughMaxSpots) / 2);
        int forest = (((ForestMaxSpots + ForestMinSpots) / 2) * (ForestMinHexes + ForestMaxSpots) / 2);
        String path = "file:///" + new File(".").getAbsolutePath();
        if (HillElevationRange < 2) result += "<img src=\"" + path + "/data/images/hill0.gif\">";
        if ((HillElevationRange < 5) && (HillElevationRange >= 2)) result += "<img src=\"" + path + "/data/images/hill1.gif\">";
        if ((HillElevationRange >= 5) && (HillElevationRange <= 8)) result += "<img src=\"" + path + "/data/images/hill2.gif\">";
        if (HillElevationRange > 8) result += "<img src=\"" + path + "/data/images/hill3.gif\">";
        if (rough > 8) result += "<img src=\"" + path + "/data/images/roug1.gif\">";
        if (CraterProb > 30) result += "<img src=\"" + path + "/data/images/crtr1.gif\">";
        if (forest > 15 && forest < 30) result += "<img src=\"" + path + "/data/images/wood1.gif\">"; else if (forest >= 30 && forest < 50) result += "<img src=\"" + path + "/data/images/wood2.gif\">"; else if (forest >= 50) result += "<img src=\"" + path + "/data/images/wood3.gif\">";
        if (water > 5 && water < 20) result += "<img src=\"" + path + "/data/images/watr1.gif\">"; else if (water >= 20) result += "<img src=\"" + path + "/data/images/watr2.gif\">";
        if (getRiverProb() > 50) result += "<img src=\"" + path + "/data/images/rivr1.gif\">";
        if (getRoadProb() > 50) result += "<img src=\"" + path + "/data/images/road1.gif\">";
        return result;
    }

    public String toString() {
        String result = "PE$";
        result += Name + "$";
        result += CraterProb + "$";
        result += CraterMinNum + "$";
        result += CraterMaxNum + "$";
        result += CraterMinRadius + "$";
        result += CraterMaxRadius + "$";
        result += Hillyness + "$";
        result += HillElevationRange + "$";
        result += HillInvertProb + "$";
        result += WaterMinSpots + "$";
        result += WaterMaxSpots + "$";
        result += WaterMinHexes + "$";
        result += WaterMaxHexes + "$";
        result += WaterDeepProb + "$";
        result += ForestMinSpots + "$";
        result += ForestMaxSpots + "$";
        result += ForestMinHexes + "$";
        result += ForestMaxHexes + "$";
        result += ForestHeavyProb + "$";
        result += RoughMinSpots + "$";
        result += RoughMaxSpots + "$";
        result += RoughMinHexes + "$";
        result += RoughMaxHexes + "$";
        result += RoadProb + "$";
        result += RiverProb + "$";
        result += Algorithm + "$";
        result += id + "$";
        result += SwampMinSpots + "$";
        result += SwampMaxSpots + "$";
        result += SwampMinHexes + "$";
        result += SwampMaxHexes + "$";
        result += PavementMinSpots + "$";
        result += PavementMaxSpots + "$";
        result += PavementMinHexes + "$";
        result += PavementMaxHexes + "$";
        result += fxMod + "$";
        result += probForestFire + "$";
        result += probFreeze + "$";
        result += probFlood + "$";
        result += probDrought + "$";
        result += Theme + "$";
        result += IceMinSpots + "$";
        result += IceMaxSpots + "$";
        result += IceMinHexes + "$";
        result += IceMaxHexes + "$";
        result += RubbleMinSpots + "$";
        result += RubbleMaxSpots + "$";
        result += RubbleMinHexes + "$";
        result += RubbleMaxHexes + "$";
        result += FortifiedMinSpots + "$";
        result += FortifiedMaxSpots + "$";
        result += FortifiedMinHexes + "$";
        result += FortifiedMaxHexes + "$";
        result += MinBuildings + "$";
        result += MaxBuildings + "$";
        result += MinCF + "$";
        result += MaxCF + "$";
        result += MinFloors + "$";
        result += MaxFloors + "$";
        result += CityDensity + "$";
        result += CityType + "$";
        result += Roads + "$";
        result += CliffProb + "$";
        result += InvertNegativeTerrain + "$";
        result += TownSize + "$";
        result += MountPeaks + "$";
        result += MountWidthMin + "$";
        result += MountWidthMax + "$";
        result += MountHeightMin + "$";
        result += MountHeightMax + "$";
        result += MountStyle + "$";
        result += EnvironmentProb + "$";
        return result;
    }

    public String toString(String city) {
        if (city.trim().length() <= 1) return this.toString();
        String result = "PE$";
        result += Name + "$";
        result += CraterProb + "$";
        result += CraterMinNum + "$";
        result += CraterMaxNum + "$";
        result += CraterMinRadius + "$";
        result += CraterMaxRadius + "$";
        result += Hillyness + "$";
        result += HillElevationRange + "$";
        result += HillInvertProb + "$";
        result += WaterMinSpots + "$";
        result += WaterMaxSpots + "$";
        result += WaterMinHexes + "$";
        result += WaterMaxHexes + "$";
        result += WaterDeepProb + "$";
        result += ForestMinSpots + "$";
        result += ForestMaxSpots + "$";
        result += ForestMinHexes + "$";
        result += ForestMaxHexes + "$";
        result += ForestHeavyProb + "$";
        result += RoughMinSpots + "$";
        result += RoughMaxSpots + "$";
        result += RoughMinHexes + "$";
        result += RoughMaxHexes + "$";
        result += RoadProb + "$";
        result += RiverProb + "$";
        result += Algorithm + "$";
        result += id + "$";
        result += SwampMinSpots + "$";
        result += SwampMaxSpots + "$";
        result += SwampMinHexes + "$";
        result += SwampMaxHexes + "$";
        result += PavementMinSpots + "$";
        result += PavementMaxSpots + "$";
        result += PavementMinHexes + "$";
        result += PavementMaxHexes + "$";
        result += fxMod + "$";
        result += probForestFire + "$";
        result += probFreeze + "$";
        result += probFlood + "$";
        result += probDrought + "$";
        result += Theme + "$";
        result += IceMinSpots + "$";
        result += IceMaxSpots + "$";
        result += IceMinHexes + "$";
        result += IceMaxHexes + "$";
        result += RubbleMinSpots + "$";
        result += RubbleMaxSpots + "$";
        result += RubbleMinHexes + "$";
        result += RubbleMaxHexes + "$";
        result += FortifiedMinSpots + "$";
        result += FortifiedMaxSpots + "$";
        result += FortifiedMinHexes + "$";
        result += FortifiedMaxHexes + "$";
        result += city + "$";
        result += CliffProb + "$";
        result += InvertNegativeTerrain + "$";
        result += TownSize + "$";
        result += MountPeaks + "$";
        result += MountWidthMin + "$";
        result += MountWidthMax + "$";
        result += MountHeightMin + "$";
        result += MountHeightMax + "$";
        result += MountStyle + "$";
        result += EnvironmentProb + "$";
        return result;
    }

    public int getWaterMinSpots() {
        return WaterMinSpots;
    }

    public int getWaterMinHexes() {
        return WaterMinHexes;
    }

    public int getWaterMaxHexes() {
        return WaterMaxHexes;
    }

    public int getWaterMaxSpots() {
        return WaterMaxSpots;
    }

    public int getWaterDeepProb() {
        return WaterDeepProb;
    }

    public int getRoughMinSpots() {
        return RoughMinSpots;
    }

    public int getRoughMinHexes() {
        return RoughMinHexes;
    }

    public int getRoughMaxSpots() {
        return RoughMaxSpots;
    }

    public int getRoughMaxHexes() {
        return RoughMaxHexes;
    }

    public int getSwampMinSpots() {
        return SwampMinSpots;
    }

    public int getSwampMinHexes() {
        return SwampMinHexes;
    }

    public int getSwampMaxSpots() {
        return SwampMaxSpots;
    }

    public int getSwampMaxHexes() {
        return SwampMaxHexes;
    }

    public int getPavementMinSpots() {
        return PavementMinSpots;
    }

    public int getPavementMinHexes() {
        return PavementMinHexes;
    }

    public int getPavementMaxSpots() {
        return PavementMaxSpots;
    }

    public int getPavementMaxHexes() {
        return PavementMaxHexes;
    }

    public int getIceMinSpots() {
        return IceMinSpots;
    }

    public int getIceMinHexes() {
        return IceMinHexes;
    }

    public int getIceMaxSpots() {
        return IceMaxSpots;
    }

    public int getIceMaxHexes() {
        return IceMaxHexes;
    }

    public int getRubbleMinSpots() {
        return RubbleMinSpots;
    }

    public int getRubbleMinHexes() {
        return RubbleMinHexes;
    }

    public int getRubbleMaxSpots() {
        return RubbleMaxSpots;
    }

    public int getRubbleMaxHexes() {
        return RubbleMaxHexes;
    }

    public int getFortifiedMinSpots() {
        return FortifiedMinSpots;
    }

    public int getFortifiedMinHexes() {
        return FortifiedMinHexes;
    }

    public int getFortifiedMaxSpots() {
        return FortifiedMaxSpots;
    }

    public int getFortifiedMaxHexes() {
        return FortifiedMaxHexes;
    }

    public int getMaxBuildings() {
        return MaxBuildings;
    }

    public int getMinBuildings() {
        return MinBuildings;
    }

    public int getMaxCF() {
        return MaxCF;
    }

    public int getMinCF() {
        return MinCF;
    }

    public int getMaxFloors() {
        return MaxFloors;
    }

    public int getMinFloors() {
        return MinFloors;
    }

    public int getCityDensity() {
        return CityDensity;
    }

    public int getRoads() {
        return Roads;
    }

    public String getCityType() {
        return CityType;
    }

    public int getRoadProb() {
        return RoadProb;
    }

    public int getRiverProb() {
        return RiverProb;
    }

    public int getHillyness() {
        return Hillyness;
    }

    public int getForestMinSpots() {
        return ForestMinSpots;
    }

    public int getHillElevationRange() {
        return HillElevationRange;
    }

    public int getForestMinHexes() {
        return ForestMinHexes;
    }

    public int getForestMaxSpots() {
        return ForestMaxSpots;
    }

    public int getForestMaxHexes() {
        return ForestMaxHexes;
    }

    public int getForestHeavyProb() {
        return ForestHeavyProb;
    }

    public int getCraterProb() {
        return CraterProb;
    }

    public int getCraterMinRadius() {
        return CraterMinRadius;
    }

    public int getCraterMaxRadius() {
        return CraterMaxRadius;
    }

    public int getCraterMinNum() {
        return CraterMinNum;
    }

    public int getCraterMaxNum() {
        return CraterMaxNum;
    }

    public int getAlgorithm() {
        return Algorithm;
    }

    public int getCliffProb() {
        return CliffProb;
    }

    public int getInvertNegativeTerrain() {
        return InvertNegativeTerrain;
    }

    public int getTownSize() {
        return TownSize;
    }

    public int getMountPeaks() {
        return MountPeaks;
    }

    public int getMountWidthMin() {
        return MountWidthMin;
    }

    public int getMountWidthMax() {
        return MountWidthMax;
    }

    public int getMountHeightMin() {
        return MountHeightMin;
    }

    public int getMountHeightMax() {
        return MountHeightMax;
    }

    public int getMountStyle() {
        return MountStyle;
    }

    public int getEnvironmentalProb() {
        return EnvironmentProb;
    }

    public void setEnvironmentalProb(int prob) {
        EnvironmentProb = prob;
    }

    public void setAlgorithm(int Algorithm) {
        this.Algorithm = Algorithm;
    }

    public void setCraterMaxNum(int CraterMaxNum) {
        this.CraterMaxNum = CraterMaxNum;
    }

    public void setCraterMaxRadius(int CraterMaxRadius) {
        this.CraterMaxRadius = CraterMaxRadius;
    }

    public void setCraterMinNum(int CraterMinNum) {
        this.CraterMinNum = CraterMinNum;
    }

    public void setCraterMinRadius(int CraterMinRadius) {
        this.CraterMinRadius = CraterMinRadius;
    }

    public void setCraterProb(int CraterProb) {
        this.CraterProb = CraterProb;
    }

    public void setForestHeavyProb(int ForestHeavyProb) {
        this.ForestHeavyProb = ForestHeavyProb;
    }

    public void setForestMaxHexes(int ForestMaxHexes) {
        this.ForestMaxHexes = ForestMaxHexes;
    }

    public void setForestMaxSpots(int ForestMaxSpots) {
        this.ForestMaxSpots = ForestMaxSpots;
    }

    public void setForestMinHexes(int ForestMinHexes) {
        this.ForestMinHexes = ForestMinHexes;
    }

    public void setForestMinSpots(int ForestMinSpots) {
        this.ForestMinSpots = ForestMinSpots;
    }

    public void setHillElevationRange(int HillElevationRange) {
        this.HillElevationRange = HillElevationRange;
    }

    public void setHillyness(int Hillyness) {
        this.Hillyness = Hillyness;
    }

    public void setRoadProb(int RoadProb) {
        this.RoadProb = RoadProb;
    }

    public void setRiverProb(int RiverProb) {
        this.RiverProb = RiverProb;
    }

    public void setRoughMaxHexes(int RoughMaxHexes) {
        this.RoughMaxHexes = RoughMaxHexes;
    }

    public void setRoughMaxSpots(int RoughMaxSpots) {
        this.RoughMaxSpots = RoughMaxSpots;
    }

    public void setRoughMinSpots(int RoughMinSpots) {
        this.RoughMinSpots = RoughMinSpots;
    }

    public void setRoughMinHexes(int RoughMinHexes) {
        this.RoughMinHexes = RoughMinHexes;
    }

    public void setWaterDeepProb(int WaterDeepProb) {
        this.WaterDeepProb = WaterDeepProb;
    }

    public void setWaterMaxHexes(int WaterMaxHexes) {
        this.WaterMaxHexes = WaterMaxHexes;
    }

    public void setWaterMaxSpots(int WaterMaxSpots) {
        this.WaterMaxSpots = WaterMaxSpots;
    }

    public void setWaterMinHexes(int WaterMinHexes) {
        this.WaterMinHexes = WaterMinHexes;
    }

    public void setWaterMinSpots(int WaterMinSpots) {
        this.WaterMinSpots = WaterMinSpots;
    }

    public int getHillInvertProb() {
        return HillInvertProb;
    }

    public void setHillInvertProb(int HillInvertProb) {
        this.HillInvertProb = HillInvertProb;
    }

    public void setSwampMaxHexes(int SwampMaxHexes) {
        this.SwampMaxHexes = SwampMaxHexes;
    }

    public void setSwampMaxSpots(int SwampMaxSpots) {
        this.SwampMaxSpots = SwampMaxSpots;
    }

    public void setSwampMinSpots(int SwampMinSpots) {
        this.SwampMinSpots = SwampMinSpots;
    }

    public void setSwampMinHexes(int SwampMinHexes) {
        this.SwampMinHexes = SwampMinHexes;
    }

    public void setPavementMaxHexes(int PavementMaxHexes) {
        this.PavementMaxHexes = PavementMaxHexes;
    }

    public void setPavementMaxSpots(int PavementMaxSpots) {
        this.PavementMaxSpots = PavementMaxSpots;
    }

    public void setPavementMinSpots(int PavementMinSpots) {
        this.PavementMinSpots = PavementMinSpots;
    }

    public void setPavementMinHexes(int PavementMinHexes) {
        this.PavementMinHexes = PavementMinHexes;
    }

    public void setIceMaxHexes(int IceMaxHexes) {
        this.IceMaxHexes = IceMaxHexes;
    }

    public void setIceMaxSpots(int IceMaxSpots) {
        this.IceMaxSpots = IceMaxSpots;
    }

    public void setIceMinSpots(int IceMinSpots) {
        this.IceMinSpots = IceMinSpots;
    }

    public void setIceMinHexes(int IceMinHexes) {
        this.IceMinHexes = IceMinHexes;
    }

    public void setRubbleMaxHexes(int RubbleMaxHexes) {
        this.RubbleMaxHexes = RubbleMaxHexes;
    }

    public void setRubbleMaxSpots(int RubbleMaxSpots) {
        this.RubbleMaxSpots = RubbleMaxSpots;
    }

    public void setRubbleMinSpots(int RubbleMinSpots) {
        this.RubbleMinSpots = RubbleMinSpots;
    }

    public void setRubbleMinHexes(int RubbleMinHexes) {
        this.RubbleMinHexes = RubbleMinHexes;
    }

    public void setFortifiedMaxHexes(int FortifiedMaxHexes) {
        this.FortifiedMaxHexes = FortifiedMaxHexes;
    }

    public void setFortifiedMaxSpots(int FortifiedMaxSpots) {
        this.FortifiedMaxSpots = FortifiedMaxSpots;
    }

    public void setFortifiedMinSpots(int FortifiedMinSpots) {
        this.FortifiedMinSpots = FortifiedMinSpots;
    }

    public void setFortifiedMinHexes(int FortifiedMinHexes) {
        this.FortifiedMinHexes = FortifiedMinHexes;
    }

    public void setMaxBuildings(int Buildings) {
        this.MaxBuildings = Buildings;
    }

    public void setMinBuildings(int Buildings) {
        this.MinBuildings = Buildings;
    }

    public void setMaxCF(int CF) {
        this.MaxCF = CF;
    }

    public void setMinCF(int CF) {
        this.MinCF = CF;
    }

    public void setMinFloors(int Floors) {
        this.MinFloors = Floors;
    }

    public void setMaxFloors(int Floors) {
        this.MaxFloors = Floors;
    }

    public void setCityDensity(int types) {
        this.CityDensity = types;
    }

    public void setCityType(String types) {
        this.CityType = types;
    }

    public void setRoads(int Roads) {
        this.Roads = Roads;
    }

    public void setFxMod(int mod) {
        this.fxMod = mod;
    }

    public void setProbForestFire(int prob) {
        this.probForestFire = prob;
    }

    public void setProbFreeze(int prob) {
        this.probFreeze = prob;
    }

    public void setProbFlood(int prob) {
        this.probFlood = prob;
    }

    public void setProbDrought(int prob) {
        this.probDrought = prob;
    }

    public void setCliffProb(int prob) {
        this.CliffProb = prob;
    }

    public void setInvertNegativeTerrain(int invert) {
        this.InvertNegativeTerrain = invert;
    }

    public void setTownSize(int amount) {
        this.TownSize = amount;
    }

    public void setMountPeaks(int amount) {
        this.MountPeaks = amount;
    }

    public void setMountWidthMin(int amount) {
        this.MountWidthMin = amount;
    }

    public void setMountWidthMax(int amount) {
        this.MountWidthMax = amount;
    }

    public void setMountHeightMin(int amount) {
        this.MountHeightMin = amount;
    }

    public void setMountHeightMax(int amount) {
        this.MountHeightMax = amount;
    }

    public void setMountStyle(int amount) {
        this.MountStyle = amount;
    }

    public int getFxMod() {
        return fxMod;
    }

    public int getProbForestFire() {
        return probForestFire;
    }

    public int getProbFreeze() {
        return probFreeze;
    }

    public int getProbFlood() {
        return probFlood;
    }

    public int getProbDrought() {
        return probDrought;
    }

    /**
     * Writes as binary stream
     */
    public void binOut(BinWriter out) throws IOException {
        out.println(id, "id");
        out.println(Name, "name");
        out.println(CraterProb, "CraterProb");
        out.println(CraterMinNum, "CraterMinNum");
        out.println(CraterMaxNum, "CraterMaxNum");
        out.println(CraterMinRadius, "CraterMinRadius");
        out.println(CraterMaxRadius, "CraterMaxRadius");
        out.println(Hillyness, "Hillyness");
        out.println(HillElevationRange, "HillElevationRange");
        out.println(HillInvertProb, "HillInvertProb");
        out.println(WaterMinSpots, "WaterMinSpots");
        out.println(WaterMaxSpots, "WaterMaxSpots");
        out.println(WaterMinHexes, "WaterMinHexes");
        out.println(WaterMaxHexes, "WaterMaxHexes");
        out.println(WaterDeepProb, "WaterDeepProb");
        out.println(ForestMinSpots, "ForestMinSpots");
        out.println(ForestMaxSpots, "ForestMaxSpots");
        out.println(ForestMinHexes, "ForestMinHexes");
        out.println(ForestMaxHexes, "ForestMaxHexes");
        out.println(ForestHeavyProb, "ForestHeavyProb");
        out.println(RoughMinSpots, "RoughMinSpots");
        out.println(RoughMaxSpots, "RoughMaxSpots");
        out.println(RoughMinHexes, "RoughMinHexes");
        out.println(RoughMaxHexes, "RoughMaxHexes");
        out.println(SwampMinSpots, "SwampMinSpots");
        out.println(SwampMaxSpots, "SwampMaxSpots");
        out.println(SwampMinHexes, "SwampMinHexes");
        out.println(SwampMaxHexes, "SwampMaxHexes");
        out.println(PavementMinSpots, "PavementMinSpots");
        out.println(PavementMaxSpots, "PavementMaxSpots");
        out.println(PavementMinHexes, "PavementMinHexes");
        out.println(PavementMaxHexes, "PavementMaxHexes");
        out.println(fxMod, "fxMod");
        out.println(probForestFire, "probForestFire");
        out.println(probFreeze, "probFreeze");
        out.println(probFlood, "probFlood");
        out.println(probDrought, "probDrought");
        out.println(CliffProb, "CliffProb");
        out.println(InvertNegativeTerrain, "InvertNegativeTerrain");
        out.println(RoadProb, "RoadProb");
        out.println(RiverProb, "RiverProb");
        out.println(Algorithm, "Algorithm");
        out.println(Theme, "Theme");
        out.println(IceMinSpots, "IceMinSpots");
        out.println(IceMaxSpots, "IceMaxSpots");
        out.println(IceMinHexes, "IceMinHexes");
        out.println(IceMaxHexes, "IceMaxHexes");
        out.println(RubbleMinSpots, "RubbleMinSpots");
        out.println(RubbleMaxSpots, "RubbleMaxSpots");
        out.println(RubbleMinHexes, "RubbleMinHexes");
        out.println(RubbleMaxHexes, "RubbleMaxHexes");
        out.println(FortifiedMinSpots, "FortifiedMinSpots");
        out.println(FortifiedMaxSpots, "FortifiedMaxSpots");
        out.println(FortifiedMinHexes, "FortifiedMinHexes");
        out.println(FortifiedMaxHexes, "FortifiedMaxHexes");
        out.println(MinBuildings, "MinBuildings");
        out.println(MaxBuildings, "MaxBuildings");
        out.println(MinCF, "MinCF");
        out.println(MaxCF, "MaxCF");
        out.println(MinFloors, "MinFloors");
        out.println(MaxFloors, "MaxFloors");
        out.println(CityDensity, "CityDensity");
        out.println(CityType, "CityType");
        out.println(Roads, "Roads");
        out.println(TownSize, "TownSize");
        out.println(MountPeaks, "MountPeaks");
        out.println(MountWidthMin, "MountWidthMin");
        out.println(MountWidthMax, "MountWidthMax");
        out.println(MountHeightMin, "MountHeightMin");
        out.println(MountHeightMax, "MountHeightMax");
        out.println(MountStyle, "MountStyle");
        out.println(minSandSpots, "minSandSpots");
        out.println(maxSandSpots, "maxSandSpots");
        out.println(minSandSize, "minSandSize");
        out.println(maxSandSize, "maxSandSize");
        out.println(minPlantedFieldSpots, "minPlantedFieldSpots");
        out.println(maxPlantedFieldSpots, "maxPlantedFieldSpots");
        out.println(minPlantedFieldSize, "minPlantedFieldSize");
        out.println(maxPlantedFieldSize, "maxPlantedFieldSize");
    }

    /**
     * Read from a binary stream
     */
    public void binIn(BinReader in, CampaignData data) throws IOException {
        id = in.readInt("id");
        Name = in.readLine("name");
        CraterProb = in.readInt("CraterProb");
        CraterMinNum = in.readInt("CraterMinNum");
        CraterMaxNum = in.readInt("CraterMaxNum");
        CraterMinRadius = in.readInt("CraterMinRadius");
        CraterMaxRadius = in.readInt("CraterMaxRadius");
        Hillyness = in.readInt("Hillyness");
        HillElevationRange = in.readInt("HillElevationRange");
        HillInvertProb = in.readInt("HillInvertProb");
        WaterMinSpots = in.readInt("WaterMinSpots");
        WaterMaxSpots = in.readInt("WaterMaxSpots");
        WaterMinHexes = in.readInt("WaterMinHexes");
        WaterMaxHexes = in.readInt("WaterMaxHexes");
        WaterDeepProb = in.readInt("WaterDeepProb");
        ForestMinSpots = in.readInt("ForestMinSpots");
        ForestMaxSpots = in.readInt("ForestMaxSpots");
        ForestMinHexes = in.readInt("ForestMinHexes");
        ForestMaxHexes = in.readInt("ForestMaxHexes");
        ForestHeavyProb = in.readInt("ForestHeavyProb");
        RoughMinSpots = in.readInt("RoughMinSpots");
        RoughMaxSpots = in.readInt("RoughMaxSpots");
        RoughMinHexes = in.readInt("RoughMinHexes");
        RoughMaxHexes = in.readInt("RoughMaxHexes");
        SwampMinSpots = in.readInt("SwampMinSpots");
        SwampMaxSpots = in.readInt("SwampMaxSpots");
        SwampMinHexes = in.readInt("SwampMinHexes");
        SwampMaxHexes = in.readInt("SwampMaxHexes");
        PavementMinSpots = in.readInt("PavementMinSpots");
        PavementMaxSpots = in.readInt("PavementMaxSpots");
        PavementMinHexes = in.readInt("PavementMinHexes");
        PavementMaxHexes = in.readInt("PavementMaxHexes");
        fxMod = in.readInt("fxMod");
        probForestFire = in.readInt("probForestFire");
        probFreeze = in.readInt("probFreeze");
        probFlood = in.readInt("probFlood");
        probDrought = in.readInt("probDrought");
        CliffProb = in.readInt("CliffProb");
        InvertNegativeTerrain = in.readInt("InvertNegativeTerrain");
        RoadProb = in.readInt("RoadProb");
        RiverProb = in.readInt("RiverProb");
        Algorithm = in.readInt("Algorithm");
        Theme = in.readLine("Theme");
        IceMinSpots = in.readInt("IceMinSpots");
        IceMaxSpots = in.readInt("IceMaxSpots");
        IceMinHexes = in.readInt("IceMinHexes");
        IceMaxHexes = in.readInt("IceMaxHexes");
        RubbleMinSpots = in.readInt("RubbleMinSpots");
        RubbleMaxSpots = in.readInt("RubbleMaxSpots");
        RubbleMinHexes = in.readInt("RubbleMinHexes");
        RubbleMaxHexes = in.readInt("RubbleMaxHexes");
        FortifiedMinSpots = in.readInt("FortifiedMinSpots");
        FortifiedMaxSpots = in.readInt("FortifiedMaxSpots");
        FortifiedMinHexes = in.readInt("FortifiedMinHexes");
        FortifiedMaxHexes = in.readInt("FortifiedMaxHexes");
        MinBuildings = in.readInt("MinBuildings");
        MaxBuildings = in.readInt("MaxBuildings");
        MinCF = in.readInt("MinCF");
        MaxCF = in.readInt("MaxCF");
        MinFloors = in.readInt("MinFloors");
        MaxFloors = in.readInt("MaxFloors");
        CityDensity = in.readInt("CityDensity");
        CityType = in.readLine("CityType");
        Roads = in.readInt("Roads");
        TownSize = in.readInt("TownSize");
        MountPeaks = in.readInt("MountPeaks");
        MountWidthMin = in.readInt("MountWidthMin");
        MountWidthMax = in.readInt("MountWidthMax");
        MountHeightMin = in.readInt("MountHeightMin");
        MountHeightMax = in.readInt("MountHeightMax");
        MountStyle = in.readInt("MountStyle");
        minSandSpots = in.readInt("minSandSpots");
        maxSandSpots = in.readInt("maxSandSpots");
        minSandSize = in.readInt("minSandSize");
        maxSandSize = in.readInt("maxSandSize");
        minPlantedFieldSpots = in.readInt("minPlantedFieldSpots");
        maxPlantedFieldSpots = in.readInt("maxPlantedFieldSpots");
        minPlantedFieldSize = in.readInt("minPlantedFieldSize");
        maxPlantedFieldSize = in.readInt("maxPlantedFieldSize");
    }

    /**
     * @return Returns the id.
     */
    public int getId() {
        return id;
    }

    /**
     * Do not use this to set the id. This is only used in 
     * PlanetEnvironments.add until .dat - saving vanishes.
     * @param id The id to set.
     * @TODO DON'T USE THIS! You were warned! (imi) 
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
	 * @return Returns the name.
	 */
    public String getName() {
        return Name;
    }

    /**
	 * @param name The name to set.
	 */
    public void setName(String name) {
        Name = name;
    }

    public String getTheme() {
        return Theme;
    }

    public void setTheme(String theme) {
        if (theme.length() <= 1) theme = " ";
        Theme = theme;
    }

    /**
	 * @return the environmentProb
	 */
    public int getEnvironmentProb() {
        return EnvironmentProb;
    }

    /**
	 * @param environmentProb the environmentProb to set
	 */
    public void setEnvironmentProb(int environmentProb) {
        EnvironmentProb = environmentProb;
    }

    /**
	 * @return the minSandSpots
	 */
    public int getMinSandSpots() {
        return minSandSpots;
    }

    /**
	 * @param minSandSpots the minSandSpots to set
	 */
    public void setMinSandSpots(int minSandSpots) {
        this.minSandSpots = minSandSpots;
    }

    /**
	 * @return the maxSandSpots
	 */
    public int getMaxSandSpots() {
        return maxSandSpots;
    }

    /**
	 * @param maxSandSpots the maxSandSpots to set
	 */
    public void setMaxSandSpots(int maxSandSpots) {
        this.maxSandSpots = maxSandSpots;
    }

    /**
	 * @return the minSandSize
	 */
    public int getMinSandSize() {
        return minSandSize;
    }

    /**
	 * @param minSandSize the minSandSize to set
	 */
    public void setMinSandSize(int minSandSize) {
        this.minSandSize = minSandSize;
    }

    /**
	 * @return the maxSandSize
	 */
    public int getMaxSandSize() {
        return maxSandSize;
    }

    /**
	 * @param maxSandSize the maxSandSize to set
	 */
    public void setMaxSandSize(int maxSandSize) {
        this.maxSandSize = maxSandSize;
    }

    /**
	 * @return the minPlantedFieldSpots
	 */
    public int getMinPlantedFieldSpots() {
        return minPlantedFieldSpots;
    }

    /**
	 * @param minPlantedFieldSpots the minPlantedFieldSpots to set
	 */
    public void setMinPlantedFieldSpots(int minPlantedFieldSpots) {
        this.minPlantedFieldSpots = minPlantedFieldSpots;
    }

    /**
	 * @return the maxPlantedFieldSpots
	 */
    public int getMaxPlantedFieldSpots() {
        return maxPlantedFieldSpots;
    }

    /**
	 * @param maxPlantedFieldSpots the maxPlantedFieldSpots to set
	 */
    public void setMaxPlantedFieldSpots(int maxPlantedFieldSpots) {
        this.maxPlantedFieldSpots = maxPlantedFieldSpots;
    }

    /**
	 * @return the minPlantedFieldSize
	 */
    public int getMinPlantedFieldSize() {
        return minPlantedFieldSize;
    }

    /**
	 * @param minPlantedFieldSize the minPlantedFieldSize to set
	 */
    public void setMinPlantedFieldSize(int minPlantedFieldSize) {
        this.minPlantedFieldSize = minPlantedFieldSize;
    }

    /**
	 * @return the maxPlantedFieldSize
	 */
    public int getMaxPlantedFieldSize() {
        return maxPlantedFieldSize;
    }

    /**
	 * @param maxPlantedFieldSize the maxPlantedFieldSize to set
	 */
    public void setMaxPlantedFieldSize(int maxPlantedFieldSize) {
        this.maxPlantedFieldSize = maxPlantedFieldSize;
    }
}
