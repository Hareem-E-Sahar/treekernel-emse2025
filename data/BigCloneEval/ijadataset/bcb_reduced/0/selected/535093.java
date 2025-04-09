package Model;

import Controller.Imaginator;
import de.micromata.opengis.kml.v_2_2_0.AbstractObject;
import de.micromata.opengis.kml.v_2_2_0.AltitudeMode;
import de.micromata.opengis.kml.v_2_2_0.Change;
import de.micromata.opengis.kml.v_2_2_0.Delete;
import de.micromata.opengis.kml.v_2_2_0.Document;
import de.micromata.opengis.kml.v_2_2_0.GroundOverlay;
import de.micromata.opengis.kml.v_2_2_0.Icon;
import de.micromata.opengis.kml.v_2_2_0.Kml;
import de.micromata.opengis.kml.v_2_2_0.KmlFactory;
import de.micromata.opengis.kml.v_2_2_0.Placemark;
import de.micromata.opengis.kml.v_2_2_0.RefreshMode;
import de.micromata.opengis.kml.v_2_2_0.ScreenOverlay;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.logging.Level;
import java.util.logging.Logger;
import de.micromata.opengis.kml.v_2_2_0.TimeSpan;
import de.micromata.opengis.kml.v_2_2_0.Units;
import de.micromata.opengis.kml.v_2_2_0.Vec2;
import de.micromata.opengis.kml.v_2_2_0.gx.AnimatedUpdate;
import de.micromata.opengis.kml.v_2_2_0.gx.FlyToMode;
import de.micromata.opengis.kml.v_2_2_0.gx.Playlist;
import de.micromata.opengis.kml.v_2_2_0.gx.Tour;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.zip.CRC32;
import java.util.zip.CheckedInputStream;
import java.util.zip.GZIPOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import javax.annotation.Resources;
import javax.imageio.ImageIO;

public class KMZCreator {

    final Kml kml;

    final Document document;

    private List<String> layers;

    private List<String> paths;

    private List<City> cities = new ArrayList<City>();

    private double duration;

    private int lowCloudAlt = 2000;

    private int midCloudAlt = 5000;

    private int highCloudAlt = 9000;

    private int rainAlt = 10000;

    private static String[] conditionsNames = { "Rain", "Cloudy", "Few_Showers", "Mostly_Cloudy", "Mostly_Sunny", "Sunny" };

    private int start;

    private int end;

    String day;

    public KMZCreator(List<City> cities, List<String> layers, List<String> paths, String day, int start, int end, double duration) {
        kml = KmlFactory.createKml();
        document = kml.createAndSetDocument().withName("Prognoza pogody").withDescription("Prognoza pogody dla wybranych polskich miast");
        this.cities = cities;
        this.day = day;
        this.start = start;
        this.end = end;
        this.layers = layers;
        this.paths = paths;
        this.duration = duration;
    }

    /**
     * Tworzy zawartość pliku kml, dodając kolejno odpowiednie obiekty.
     */
    public void generateKMZ() {
        createStyles();
        createPlacemarks();
        createGroundOverlays();
        createLegend();
        createTour();
    }

    /**
     * Zapisuje zawartość pliku kml oraz wszystkich obiektów graficznych do
     * pojedynczego pliku kmz.
     */
    public void createKMZ() {
        String path = paths.get(1);
        String icons = paths.get(2);
        try {
            kml.marshal(new File(path + "\\" + "Paczka.kml"));
            System.out.println("Utworzono pomyślnie plik kml");
            File file = new File(path + "\\" + "Paczka.kml");
            BufferedReader reader = new BufferedReader(new FileReader(file));
            String line = "", oldtext = "";
            while ((line = reader.readLine()) != null) {
                oldtext += line + "\n";
            }
            reader.close();
            String newtext = oldtext.replaceAll("xmlns:xal=\"urn:oasis:names:tc:ciq:xsdschema:xAL:2.0\"", "");
            FileWriter writer = new FileWriter(path + "\\" + "Paczka.kml");
            writer.write(newtext);
            writer.close();
            File f = new File(path);
            boolean directory = f.isDirectory();
            try {
                zipPackege(new String(path + "\\"), path + "\\" + "Paczka.kmz", icons);
                System.out.println("finished OK");
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (FileNotFoundException ex) {
            System.out.println("Wystąpił błąd podczas tworzenia pliku KMZ");
        } catch (IOException ex) {
        }
    }

    private long doChecksum(String fileName) {
        CheckedInputStream cis = null;
        try {
            cis = new CheckedInputStream(new FileInputStream(fileName), new CRC32());
        } catch (FileNotFoundException e) {
            System.err.println("File not found.");
            System.exit(1);
        }
        byte[] buf = new byte[128];
        try {
            while (cis.read(buf) >= 0) ;
        } catch (IOException e) {
            throw new RuntimeException("error while reading file", e);
        }
        long checksum = cis.getChecksum().getValue();
        return checksum;
    }

    public void zipPackege(final String dirName, final String urldoPliku, String ikony) throws IOException, FileNotFoundException {
        final String nazwaPliku = urldoPliku;
        int x;
        final ZipOutputStream zos = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(nazwaPliku)));
        zos.setMethod(ZipOutputStream.STORED);
        String filePath = dirName + "/" + "Paczka.kml";
        BufferedInputStream in = new BufferedInputStream(new FileInputStream(filePath));
        ZipEntry entry = new ZipEntry("Paczka.kml");
        entry.setMethod(ZipEntry.STORED);
        entry.setSize(new File(filePath).length());
        entry.setCrc(doChecksum(filePath));
        zos.putNextEntry(entry);
        while ((x = in.read()) != -1) {
            zos.write(x);
        }
        in.close();
        for (String s : conditionsNames) {
            filePath = ikony + "/" + s + ".png";
            in = new BufferedInputStream(new FileInputStream(filePath));
            entry = new ZipEntry("icons/" + s + ".png");
            entry.setMethod(ZipEntry.STORED);
            entry.setSize(new File(filePath).length());
            entry.setCrc(doChecksum(filePath));
            zos.putNextEntry(entry);
            while ((x = in.read()) != -1) {
                zos.write(x);
            }
            in.close();
        }
        for (int i = start; i < end; i++) {
            for (String s : layers) {
                filePath = dirName + "/" + s + "_" + i + ".png";
                in = new BufferedInputStream(new FileInputStream(filePath));
                entry = new ZipEntry(s + "_" + i + ".png");
                entry.setMethod(ZipEntry.STORED);
                entry.setSize(new File(filePath).length());
                entry.setCrc(doChecksum(filePath));
                zos.putNextEntry(entry);
                while ((x = in.read()) != -1) {
                    zos.write(x);
                }
                in.close();
            }
        }
        for (int i = start; i < end; i++) {
            filePath = dirName + "/legend_" + i + ".png";
            in = new BufferedInputStream(new FileInputStream(filePath));
            entry = new ZipEntry("legend_" + i + ".png");
            entry.setMethod(ZipEntry.STORED);
            entry.setSize(new File(filePath).length());
            entry.setCrc(doChecksum(filePath));
            zos.putNextEntry(entry);
            while ((x = in.read()) != -1) {
                zos.write(x);
            }
            in.close();
        }
        filePath = ikony + "/1.mp3";
        in = new BufferedInputStream(new FileInputStream(filePath));
        entry = new ZipEntry("1.mp3");
        entry.setMethod(ZipEntry.STORED);
        entry.setSize(new File(filePath).length());
        entry.setCrc(doChecksum(filePath));
        zos.putNextEntry(entry);
        while ((x = in.read()) != -1) {
            zos.write(x);
        }
        in.close();
        zos.close();
    }

    /**
     *  Tworzy i dodaje do dokumentu style, które będą aplikowane do placemarków,
        zmieniając ich ikonę w zależności do warunków pogodowych w danej chwili.
     */
    private void createStyles() {
        for (String s : conditionsNames) {
            document.createAndAddStyle().withId(s).createAndSetIconStyle().withScale(2.0).createAndSetIcon().withHref("icons/" + s + ".png");
        }
    }

    /**
     * Tworzy i dodaje do dokumentu placemarki dla wszystkich miast,
     * które chcemy umieścić na mapie.
     */
    private void createPlacemarks() {
        for (int i = 0; i < cities.size(); i++) {
            City c = cities.get(i);
            document.createAndAddPlacemark().withName(c.getName() + getTemperature(c.getConditions().get(0).Temperature)).withId("c" + i).withStyleUrl(getConditions(c.getConditions().get(0))).createAndSetPoint().addToCoordinates(c.getLon(), c.getLat());
        }
    }

    /**
     * Tworzy warstwy chmur i/lub opadów jako obiekty GroundOverlay
     * z wygenerowanych wcześniej plików png.
     */
    private void createGroundOverlays() {
        for (String s : layers) {
            for (int i = start; i < end; i++) {
                final GroundOverlay groundoverlay;
                groundoverlay = document.createAndAddGroundOverlay();
                boolean visibility = (i == start) ? true : false;
                groundoverlay.withName(s + i).withVisibility(visibility).withId(s + i);
                groundoverlay.createAndSetIcon().withHref(s + "_" + i + ".png").withViewBoundScale(0.75d);
                if (s.equals(Consts.LAYER_HIGH_CLOUD)) {
                    groundoverlay.withAltitude(highCloudAlt);
                    groundoverlay.withColor("90ffffff");
                } else if (s.equals(Consts.LAYER_LOW_CLOUD)) {
                    groundoverlay.withAltitude(lowCloudAlt);
                    groundoverlay.withColor("b4a9a9aa");
                } else if (s.equals(Consts.LAYER_MID_CLOUD)) {
                    groundoverlay.withAltitude(midCloudAlt);
                    groundoverlay.withColor("7affffff");
                } else if (s.equals(Consts.LAYER_RAIN)) {
                    groundoverlay.withAltitude(rainAlt);
                }
                groundoverlay.withAltitudeMode(AltitudeMode.ABSOLUTE);
                double S = Consts.DOMAIN_START_Y;
                double N = S + (Consts.DOMAIN_ROW_INTERVAL * Consts.DOMAIN_ROWS);
                double W = Consts.DOMAIN_START_X;
                double E = W + (Consts.DOMAIN_COL_INTERVAL * Consts.DOMAIN_COLS);
                groundoverlay.createAndSetLatLonBox().withNorth(N).withSouth(S).withEast(E).withWest(W);
            }
        }
    }

    private void createLegend() {
        Imaginator imag = new Imaginator(150, 30);
        DateFormat dfm = new SimpleDateFormat("dd.MM.yyyy HH:mm");
        Calendar cal = Calendar.getInstance();
        try {
            Date date = dfm.parse(day);
            cal.setTime(date);
            for (int i = 0; i < end - start; i++) {
                try {
                    BufferedImage bi = imag.createLegend(dfm.format(cal.getTime()));
                    String name = "legend_" + (start + i);
                    ImageIO.write(bi, "png", new File(paths.get(1) + "\\" + name + ".png"));
                    cal.add(Calendar.HOUR, 1);
                } catch (IOException ex) {
                    System.out.println(ex.toString());
                    Logger.getLogger(KMZCreator.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        } catch (ParseException e) {
            System.out.println(e.toString());
            Logger.getLogger(KMZCreator.class.getName()).log(Level.SEVERE, null, e);
        }
        for (int i = start; i < end; i++) {
            boolean visibility = (i == start) ? true : false;
            ScreenOverlay sc = document.createAndAddScreenOverlay();
            sc.withId("sc" + i).withName("sc" + i).withVisibility(visibility).withIcon(new Icon().withHref("legend_" + i + ".png")).withOverlayXY(new Vec2().withX(0).withY(1).withXunits(Units.FRACTION).withYunits(Units.FRACTION)).withScreenXY(new Vec2().withX(0).withY(1).withXunits(Units.FRACTION).withYunits(Units.FRACTION));
        }
    }

    /**
     * Tworzy obiekt animacji, który wykorzystuje utworzone wcześniej
     * obiekty i warstwy w celu stworzenia płynnej wizualizacji pogody
     * dla określonego okresu czasowego.
     */
    private void createTour() {
        int flyToPoints = cities.size() + 1;
        int hours = end - start - 1;
        double totalTime = duration * flyToPoints;
        double frameTime = totalTime / hours;
        Tour tour = document.createAndAddTour().withName("Animacja pogody");
        Playlist playlist = tour.createAndSetPlaylist();
        playlist.createAndAddSoundCue().withHref("1.mp3");
        int cityNum = 0;
        for (int i = 0; i < hours; i++) {
            Change ch = new Change();
            for (String s : layers) {
                GroundOverlay g1 = new GroundOverlay().withTargetId(s + (start + i)).withAltitudeMode(AltitudeMode.RELATIVE_TO_GROUND).withVisibility(false);
                GroundOverlay g2 = new GroundOverlay().withTargetId(s + (start + i + 1)).withAltitudeMode(AltitudeMode.RELATIVE_TO_GROUND).withVisibility(true);
                if (s.equals(Consts.LAYER_HIGH_CLOUD)) {
                    g1.withAltitude(highCloudAlt);
                    g2.withAltitude(highCloudAlt);
                } else if (s.equals(Consts.LAYER_MID_CLOUD)) {
                    g1.withAltitude(midCloudAlt);
                    g2.withAltitude(midCloudAlt);
                } else if (s.equals(Consts.LAYER_LOW_CLOUD)) {
                    g1.withAltitude(lowCloudAlt);
                    g2.withAltitude(lowCloudAlt);
                } else if (s.equals(Consts.LAYER_RAIN)) {
                    g1.withAltitude(rainAlt);
                    g2.withAltitude(rainAlt);
                }
                ch.addToAbstractObject(g1).addToAbstractObject(g2);
            }
            for (int k = 0; k < cities.size(); k++) {
                City c = cities.get(k);
                Placemark p = new Placemark();
                p.withTargetId("c" + k).withName(c.getName() + getTemperature(c.getConditions().get(i + 1).Temperature)).withStyleUrl(getConditions(c.getConditions().get(i + 1)));
                ch.addToAbstractObject(p);
            }
            ScreenOverlay sc1 = new ScreenOverlay();
            sc1.withTargetId("sc" + (start + i)).withVisibility(false);
            ch.addToAbstractObject(sc1);
            ScreenOverlay sc2 = new ScreenOverlay();
            sc2.withTargetId("sc" + (start + i + 1)).withVisibility(true);
            ch.addToAbstractObject(sc2);
            playlist.createAndAddAnimatedUpdate().withDuration(frameTime).withDelayedStart(frameTime * i).createAndSetUpdate(null, null).addToCreateOrDeleteOrChange(ch);
        }
        for (int i = 0; i < flyToPoints - 1; i++) {
            int alt = 0;
            int tilt = 0;
            int range = 1000000;
            City c = cities.get(cityNum);
            alt = 0;
            tilt = 50;
            range = 100000;
            cityNum++;
            playlist.createAndAddFlyTo().withFlyToMode(FlyToMode.BOUNCE).withDuration(duration).createAndSetLookAt().withLatitude(c.getLat()).withAltitude(alt).withLongitude(c.getLon()).withHeading(0).withTilt(tilt).withRange(range).withAltitudeMode(AltitudeMode.RELATIVE_TO_GROUND);
        }
        playlist.createAndAddFlyTo().withFlyToMode(FlyToMode.BOUNCE).withDuration(duration).createAndSetLookAt().withLatitude(52.04).withAltitude(900000).withLongitude(19.28).withHeading(0).withTilt(0).withRange(1000).withAltitudeMode(AltitudeMode.RELATIVE_TO_GROUND);
    }

    private String getConditions(Conditions c) {
        if (c.Clouds > 70) {
            if (c.Rain > 0) return conditionsNames[0]; else return conditionsNames[1];
        } else if (c.Clouds > 40) {
            if (c.Rain > 0) return conditionsNames[2]; else return conditionsNames[3];
        } else if (c.Clouds > 0) return conditionsNames[4]; else return conditionsNames[5];
    }

    /**
     * Zwraca sformatowany tekst temperatury do wyświetlenia.
     */
    private String getTemperature(double t) {
        return " " + Math.round(t - 273.15d) + " °C";
    }
}
