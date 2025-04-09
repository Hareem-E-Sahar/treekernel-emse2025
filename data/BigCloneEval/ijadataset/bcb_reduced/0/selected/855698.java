package kmztest;

import de.micromata.opengis.kml.v_2_2_0.AbstractObject;
import de.micromata.opengis.kml.v_2_2_0.AltitudeMode;
import de.micromata.opengis.kml.v_2_2_0.Change;
import de.micromata.opengis.kml.v_2_2_0.Document;
import de.micromata.opengis.kml.v_2_2_0.GroundOverlay;
import de.micromata.opengis.kml.v_2_2_0.Kml;
import de.micromata.opengis.kml.v_2_2_0.KmlFactory;
import de.micromata.opengis.kml.v_2_2_0.Placemark;
import de.micromata.opengis.kml.v_2_2_0.RefreshMode;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.logging.Level;
import java.util.logging.Logger;
import de.micromata.opengis.kml.v_2_2_0.TimeSpan;
import de.micromata.opengis.kml.v_2_2_0.gx.AnimatedUpdate;
import de.micromata.opengis.kml.v_2_2_0.gx.FlyToMode;
import de.micromata.opengis.kml.v_2_2_0.gx.Playlist;
import de.micromata.opengis.kml.v_2_2_0.gx.Tour;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.CRC32;
import java.util.zip.CheckedInputStream;
import java.util.zip.GZIPOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class KmzCreator {

    final Kml kml;

    final Document document;

    private List<String> layers;

    private List<City> cities = new ArrayList<City>();

    private double duration = 1.0;

    private int lowCloudAlt = 2000;

    private int midCloudAlt = 5000;

    private int highCloudAlt = 9000;

    private static String[] conditionsNames = { "Rain", "Cloudy", "Few_Showers", "Mostly_Cloudy", "Mostly_Sunny", "Sunny" };

    private int start;

    private int end;

    String day;

    public KmzCreator(List<City> cities, List<String> layers, String day, int start, int end) {
        kml = KmlFactory.createKml();
        document = kml.createAndSetDocument().withName("Chmury").withDescription("Praca inżynierska");
        this.cities = cities;
        this.day = day;
        this.start = start;
        this.end = end;
        this.layers = layers;
    }

    public void generateKMZ() {
        createStyles();
        createPlacemarks();
        createGroundOverlays();
        createTour();
    }

    public void createKMZ(String path, String icons) {
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
     * Tworzy i dodaje do dokumentu placemarki dla wszystkich miast, które chcemy umieścić na mapie.
     */
    private void createPlacemarks() {
        for (int i = 0; i < cities.size(); i++) {
            City c = cities.get(i);
            document.createAndAddPlacemark().withName(c.getName()).withId("c" + i).withStyleUrl(getConditions(c.getConditions().get(0))).createAndSetPoint().addToCoordinates(c.getLon(), c.getLat());
        }
    }

    /**
     * Tworzy warstwy chmur jako obiekty GroundOverlay z wygenerowanych wcześniej plików png.
     */
    private void createGroundOverlays() {
        for (String s : layers) {
            for (int i = start; i < end; i++) {
                final GroundOverlay groundoverlay;
                groundoverlay = document.createAndAddGroundOverlay();
                boolean visibility = (i == start) ? true : false;
                groundoverlay.withName(s + i).withVisibility(visibility).withId(s + i);
                groundoverlay.createAndSetIcon().withHref(s + "_" + i + ".png").withViewBoundScale(0.75d);
                if (s.equals(KmzTest.HIGH_CLOUD)) {
                    groundoverlay.withAltitude(highCloudAlt);
                    groundoverlay.withColor("90ffffff");
                } else if (s.equals(KmzTest.LOW_CLOUD)) {
                    groundoverlay.withAltitude(lowCloudAlt);
                    groundoverlay.withColor("ffa9a9aa");
                } else if (s.equals(KmzTest.MID_CLOUD)) {
                    groundoverlay.withAltitude(midCloudAlt);
                    groundoverlay.withColor("7affffff");
                }
                groundoverlay.withAltitudeMode(AltitudeMode.ABSOLUTE);
                double S = 49.080000;
                double N = S + (0.0194454545454546 * 291);
                double W = 14.926000;
                double E = W + (0.0287159669396865 * 309);
                groundoverlay.createAndSetLatLonBox().withNorth(N).withSouth(S).withEast(E).withWest(W);
            }
        }
    }

    private void createTour() {
        int flyToPoints = cities.size() * 2;
        int numOfFrames = (end - start - 1) / flyToPoints;
        Tour tour = document.createAndAddTour().withName("Animacja pogody");
        Playlist playlist = tour.createAndSetPlaylist();
        int cityNum = 0;
        for (int i = 0; i < flyToPoints; i++) {
            for (int j = 0; j < numOfFrames; j++) {
                Change ch = new Change();
                for (String s : layers) {
                    GroundOverlay g1 = new GroundOverlay().withTargetId(s + (start + numOfFrames * i + j)).withAltitudeMode(AltitudeMode.RELATIVE_TO_GROUND).withVisibility(false);
                    GroundOverlay g2 = new GroundOverlay().withTargetId(s + (start + numOfFrames * i + j + 1)).withAltitudeMode(AltitudeMode.RELATIVE_TO_GROUND).withVisibility(true);
                    if (s.equals(KmzTest.HIGH_CLOUD)) {
                        g1.withAltitude(highCloudAlt);
                        g2.withAltitude(highCloudAlt);
                    } else if (s.equals(KmzTest.MID_CLOUD)) {
                        g1.withAltitude(midCloudAlt);
                        g2.withAltitude(midCloudAlt);
                    } else if (s.equals(KmzTest.LOW_CLOUD)) {
                        g1.withAltitude(lowCloudAlt);
                        g2.withAltitude(lowCloudAlt);
                    }
                    ch.addToAbstractObject(g1).addToAbstractObject(g2);
                }
                for (int k = 0; k < cities.size(); k++) {
                    City c = cities.get(k);
                    Placemark p = new Placemark();
                    p.withTargetId("c" + k).withStyleUrl(getConditions(c.getConditions().get(i * numOfFrames + j + 1)));
                    ch.addToAbstractObject(p);
                }
                playlist.createAndAddAnimatedUpdate().withDuration(duration).withDelayedStart(j * duration).createAndSetUpdate(null, null).addToCreateOrDeleteOrChange(ch);
            }
            int alt = 0;
            int tilt = 0;
            int range = 1000000;
            City c = cities.get(cityNum);
            if (i % 2 == 1) {
                alt = 0;
                tilt = 50;
                range = 100000;
                cityNum++;
            }
            playlist.createAndAddFlyTo().withFlyToMode(FlyToMode.BOUNCE).withDuration(duration * numOfFrames).createAndSetLookAt().withLatitude(c.getLat()).withAltitude(alt).withLongitude(c.getLon()).withHeading(0).withTilt(tilt).withRange(range).withAltitudeMode(AltitudeMode.RELATIVE_TO_GROUND);
        }
        for (String s : layers) {
            for (int i = start + numOfFrames * flyToPoints, j = 0; i < end; i++, j++) {
                GroundOverlay g1 = new GroundOverlay().withTargetId(s + (i - 1)).withAltitudeMode(AltitudeMode.RELATIVE_TO_GROUND).withVisibility(false);
                GroundOverlay g2 = new GroundOverlay().withTargetId(s + i).withAltitudeMode(AltitudeMode.RELATIVE_TO_GROUND).withVisibility(true);
                if (s.equals(KmzTest.HIGH_CLOUD)) {
                    g1.withAltitude(highCloudAlt);
                    g2.withAltitude(highCloudAlt);
                } else if (s.equals(KmzTest.MID_CLOUD)) {
                    g1.withAltitude(midCloudAlt);
                    g2.withAltitude(midCloudAlt);
                } else if (s.equals(KmzTest.LOW_CLOUD)) {
                    g1.withAltitude(lowCloudAlt);
                    g2.withAltitude(lowCloudAlt);
                }
                Change ch = new Change().addToAbstractObject(g1).addToAbstractObject(g2);
                for (int k = 0; k < cities.size(); k++) {
                    City c = cities.get(k);
                    Placemark p = new Placemark();
                    p.withTargetId("c" + k).withStyleUrl(getConditions(c.getConditions().get(numOfFrames * flyToPoints + j)));
                    ch.addToAbstractObject(p);
                }
                playlist.createAndAddAnimatedUpdate().withDuration(duration).withDelayedStart(j * duration).createAndSetUpdate(null, null).addToCreateOrDeleteOrChange(ch);
            }
        }
        double d = duration * ((end - start) % flyToPoints);
        if (d < 3) d = 3;
        playlist.createAndAddFlyTo().withFlyToMode(FlyToMode.BOUNCE).withDuration(d).createAndSetLookAt().withLatitude(52.04).withAltitude(900000).withLongitude(19.28).withHeading(0).withTilt(0).withRange(1000).withAltitudeMode(AltitudeMode.RELATIVE_TO_GROUND);
    }

    private String getConditions(Conditions c) {
        if (c.Clouds > 70) {
            if (c.Rain > 0) return conditionsNames[0]; else return conditionsNames[1];
        } else if (c.Clouds > 40) {
            if (c.Rain > 0) return conditionsNames[2]; else return conditionsNames[3];
        } else if (c.Clouds > 0) return conditionsNames[4]; else return conditionsNames[5];
    }
}
