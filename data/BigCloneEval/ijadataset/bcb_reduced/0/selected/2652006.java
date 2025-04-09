package pck_tap.beerxml;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Vector;
import pck_tap.alg.MultiComparator;
import pck_tap.alg.Util;
import pck_tap.Userinterface.rpe.frmRecipe.JPanels.JYeastPanel.JYeastPanel.YeastNameComparator;
import pck_tap.beerxml.recipe.Yeast;
import pck_tap.current.CurrentObjectMemory;

public class YeastLoader {

    private CurrentObjectMemory com;

    private ArrayList vArrayList;

    private String vFilename = "data/data_gist.txt";

    private Vector Yeasts = new Vector();

    public YeastLoader() {
    }

    public YeastLoader(CurrentObjectMemory com) {
        this.com = com;
        try {
            vArrayList = com.getFiles().getFile(vFilename);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        pl(".YeastLoader :" + vArrayList.size());
        for (int vIndex = 1; vIndex < vArrayList.size(); vIndex++) {
            String vRow = vArrayList.get(vIndex).toString() + "                                                                                                                                    ";
            Yeast vYeast = new Yeast();
            vYeast = row2yeast(vRow);
            Yeasts.addElement(vYeast);
        }
        pl(".YeastLoader : Size : " + Yeasts.size());
        MultiComparator mc;
        mc = new MultiComparator();
        mc.addComparator(new YeastNameComparator());
        Collections.sort(Yeasts, mc);
    }

    private Yeast row2yeast(String pTextRow) {
        pl(".row2yeast : Begin");
        Yeast vYeast = new Yeast();
        String vAvailable = pTextRow.substring(01, 10).trim();
        String vName;
        String vProduct_id = "";
        String vTempProduct_Id = pTextRow.substring(10, 17).trim();
        String vTempProduct_wlp = pTextRow.substring(10, 13).trim();
        String vTempProduct_wlpxxx = pTextRow.substring(10, 13 + 3).trim();
        pl(".row2yeast : vAvailable   : " + vAvailable);
        if (vTempProduct_wlp.equalsIgnoreCase("WLP")) {
            vProduct_id = str2str(vTempProduct_wlpxxx);
            vName = str2str(pTextRow.substring(16, 50).trim());
            pl(".row2yeast : vProduct_Id  : " + vProduct_id);
        } else {
            vTempProduct_Id = str2number(vTempProduct_Id).trim();
            if (vTempProduct_Id.length() == 4) {
                vProduct_id = str2str(vTempProduct_Id);
                vName = str2str(pTextRow.substring(14, 50).trim());
                pl(".row2yeast : vProduct_Id  : " + vProduct_id);
            } else {
                vName = pTextRow.substring(10, 50).trim();
            }
        }
        pl(".row2yeast : vName        : " + vName);
        String vHerkomst = str2str(pTextRow.substring(50, 118).trim());
        pl(".row2yeast : vHerkomst    : " + vHerkomst);
        String vLaboratory = str2str(pTextRow.substring(118, 143).trim());
        pl(".row2yeast : vLaboratory  : " + vLaboratory);
        String vTempType = str2str(pTextRow.substring(153, 170));
        vTempType = vTempType.replace("Boven", "Ale");
        vTempType = vTempType.replace("Meng", "Ale");
        vTempType = vTempType.replace("Wilde", "Ale");
        vTempType = vTempType.replace("Onder", "Lager");
        vTempType = vTempType.replace("Tarwe", "Wheat");
        vTempType = vTempType.replace("Wijn", "Wine");
        vTempType = vTempType.replace("Champagne", "Champagne");
        String vType = vTempType.trim();
        pl(".row2yeast : vType        : " + vType);
        String vTempForm = str2str(pTextRow.substring(143, 153));
        vTempForm = vTempForm.replace("Vloeibaar", "Liquid");
        vTempForm = vTempForm.replace("Korrel", "Dry");
        String vForm = vTempForm.trim();
        pl(".row2yeast : vForm        : " + vForm);
        String vTempFlocculation = str2str(pTextRow.substring(226, 237)).trim();
        vTempFlocculation = vTempFlocculation.replace("Hoog", "Very High");
        vTempFlocculation = vTempFlocculation.replace("Laag", "Low");
        vTempFlocculation = vTempFlocculation.replace("Laag-medium", "Medium");
        vTempFlocculation = vTempFlocculation.replace("Medium", "Medium");
        vTempFlocculation = vTempFlocculation.replace("Medium-hoog", "High");
        String vFlocculation = vTempFlocculation.trim();
        pl(".row2yeast : vFlocculation: " + vFlocculation);
        Double vAttenuationMin = str2double(pTextRow.substring(170, 180));
        Double vAttenuationMax = str2double(pTextRow.substring(181, 191));
        Double vAttenuation = (vAttenuationMin + vAttenuationMax) / 2;
        pl(".row2yeast : vAttenuationMin : " + vAttenuationMin);
        pl(".row2yeast : vAttenuationMax : " + vAttenuationMax);
        pl(".row2yeast : vAttenuation    : " + vAttenuation);
        Double vAlcoholToleration = str2double(pTextRow.substring(215, 225));
        pl(".row2yeast : vAlcoholToleration  : " + vAlcoholToleration);
        Double vMin_Temperature = Util.str2dbl(pTextRow.substring(192, 202));
        pl(".row2yeast : vMin_Temperature    : " + vMin_Temperature);
        Double vMax_Temperature = Util.str2dbl((pTextRow.substring(204, 214)));
        pl(".row2yeast : vMax_Temperature    : " + vMax_Temperature);
        vYeast.setName(vName);
        vYeast.setAttenuation(vAttenuation);
        vYeast.setTaAttenuationMin(vAttenuationMin);
        vYeast.setTaAttenuationMax(vAttenuationMax);
        vYeast.setFlocculation(vFlocculation);
        vYeast.setForm(vForm);
        vYeast.setLaboratory(vLaboratory);
        vYeast.setMax_Temperature(vMax_Temperature);
        vYeast.setMin_Temperature(vMin_Temperature);
        vYeast.setProduct_Id(vProduct_id);
        vYeast.setTaHerkomst(vHerkomst);
        vYeast.setType(vType);
        vYeast.setTap_Available(Util.strY2bln(vAvailable));
        pl(vYeast.getInfo());
        pl(".row2yeast : Einde");
        return vYeast;
    }

    private String str2str(String s) {
        return s.replace("]", "").replace("[", "");
    }

    private void pl(String s) {
    }

    public static String str2number(String s) {
        String good = "0123456789";
        String result = "";
        for (int i = 0; i < s.length(); i++) {
            if (good.indexOf(s.charAt(i)) >= 0) result += s.charAt(i);
        }
        return result;
    }

    public static String str2doubles(String s) {
        String good = "0123456789.,";
        String result = "";
        for (int i = 0; i < s.length(); i++) {
            if (good.indexOf(s.charAt(i)) >= 0) result += s.charAt(i);
        }
        return result;
    }

    private Double str2double(String s) {
        s = str2doubles(s).replace(",", ".");
        if (s.equalsIgnoreCase(".") || s.equalsIgnoreCase(",") || s.equalsIgnoreCase("-")) {
            s = "0";
        }
        if (s.length() > 0) {
            return Double.valueOf(s);
        }
        {
            return Double.valueOf("0");
        }
    }

    public void setYeasts(Vector Yeasts) {
        this.Yeasts = Yeasts;
    }

    public Vector getYeasts() {
        return Yeasts;
    }

    public void setCom(CurrentObjectMemory com) {
        this.com = com;
    }

    public CurrentObjectMemory getCom() {
        return com;
    }
}
