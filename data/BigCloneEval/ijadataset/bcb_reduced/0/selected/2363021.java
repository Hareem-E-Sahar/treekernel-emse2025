package net.sourceforge.xhsi.model.xplane;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.logging.Logger;
import net.sourceforge.xhsi.XHSIPreferences;
import net.sourceforge.xhsi.XHSIStatus;
import net.sourceforge.xhsi.PreferencesObserver;
import net.sourceforge.xhsi.ProgressObserver;
import net.sourceforge.xhsi.model.Airport;
import net.sourceforge.xhsi.model.CoordinateSystem;
import net.sourceforge.xhsi.model.Fix;
import net.sourceforge.xhsi.model.Localizer;
import net.sourceforge.xhsi.model.NavigationObjectRepository;
import net.sourceforge.xhsi.model.RadioNavigationObject;
import net.sourceforge.xhsi.model.RadioNavBeacon;
import net.sourceforge.xhsi.model.Runway;

public class XPlaneNavigationObjectBuilder implements PreferencesObserver {

    private String NAV_file = "/Resources/default data/earth_nav.dat";

    private String FIX_file = "/Resources/default data/earth_fix.dat";

    private String AWY_file = "/Resources/default data/earth_awy.dat";

    private String APT_file = "/Resources/default scenery/default apt dat/Earth nav data/apt.dat";

    private String pathname_to_aptnav;

    private NavigationObjectRepository nor;

    private ProgressObserver progressObserver;

    private Fix fix;

    private static Logger logger = Logger.getLogger("net.sourceforge.xhsi");

    public XPlaneNavigationObjectBuilder(String pathname_to_aptnav) throws Exception {
        this.pathname_to_aptnav = pathname_to_aptnav;
        this.nor = NavigationObjectRepository.get_instance();
        this.progressObserver = null;
    }

    public void set_progress_observer(ProgressObserver observer) {
        this.progressObserver = observer;
    }

    public void read_all_tables() throws Exception {
        if (new File(this.pathname_to_aptnav).exists()) {
            logger.fine("Start reading AptNav resource files in " + XHSIPreferences.PREF_APTNAV_DIR);
            this.nor.init();
            if (this.progressObserver != null) {
                this.progressObserver.set_progress("Loading databases", "loading NAV", 0.0f);
            }
            read_nav_table();
            if (this.progressObserver != null) {
                this.progressObserver.set_progress("Loading databases", "loading FIX", 25.0f);
            }
            read_fix_table();
            if (this.progressObserver != null) {
                this.progressObserver.set_progress("Loading databases", "loading AWY", 50.0f);
            }
            read_awy_table();
            if (this.progressObserver != null) {
                this.progressObserver.set_progress("Loading databases", "loading APT", 75.0f);
            }
            read_apt_table();
            if (this.progressObserver != null) {
                this.progressObserver.set_progress("Loading databases", "Done!", 100.0f);
            }
        } else {
            logger.warning("AptNav resources directory is wrong!");
        }
    }

    public void read_nav_table() throws Exception {
        logger.fine("Reading NAV database ( " + this.pathname_to_aptnav + this.NAV_file + " )");
        File file = new File(this.pathname_to_aptnav + this.NAV_file);
        BufferedReader reader = new BufferedReader(new FileReader(file));
        String line;
        int info_type;
        String[] tokens;
        long line_number = 0;
        RadioNavigationObject coupled_rno;
        Localizer coupled_loc;
        RadioNavigationObject twin_rno;
        Localizer twin_loc;
        boolean has_a_twin;
        String twin_ilt;
        while ((line = reader.readLine()) != null) {
            if (line.length() > 0) {
                line_number++;
                line = line.trim();
                if ((line_number == 2) && (line.length() >= 32)) {
                    XHSIStatus.nav_db_cycle = line.substring(25, 32);
                } else if ((line_number > 2) && (!line.equals("99"))) {
                    try {
                        info_type = Integer.parseInt(line.substring(0, 2).trim());
                        if ((info_type == 2) || (info_type == 3) || (info_type == 13)) {
                            tokens = line.split("\\s+", 9);
                            nor.add_nav_object(new RadioNavBeacon(tokens[8], tokens[7], info_type, Float.parseFloat(tokens[1]), Float.parseFloat(tokens[2]), Float.parseFloat(tokens[3]), Float.parseFloat(tokens[4]), Integer.parseInt(tokens[5]), Float.parseFloat(tokens[6])));
                        } else if ((info_type == 4) || (info_type == 5)) {
                            tokens = line.split("\\s+", 11);
                            twin_rno = nor.find_tuned_nav_object(Float.parseFloat(tokens[1]), Float.parseFloat(tokens[2]), Float.parseFloat(tokens[4]) / 100.0f, "");
                            has_a_twin = ((twin_rno != null) && (twin_rno instanceof Localizer));
                            twin_ilt = "";
                            if (has_a_twin) {
                                twin_loc = (Localizer) twin_rno;
                                twin_loc.has_twin = true;
                                twin_loc.twin_ilt = tokens[7];
                                twin_ilt = twin_loc.ilt;
                            }
                            nor.add_nav_object(new Localizer(tokens[8] + " " + tokens[9], tokens[7], info_type, Float.parseFloat(tokens[1]), Float.parseFloat(tokens[2]), Float.parseFloat(tokens[3]), Float.parseFloat(tokens[4]), Integer.parseInt(tokens[5]), Float.parseFloat(tokens[6]), tokens[8], tokens[9], has_a_twin, twin_ilt));
                        } else if (info_type == 6) {
                            tokens = line.split("\\s+", 11);
                            coupled_rno = nor.find_tuned_nav_object(Float.parseFloat(tokens[1]), Float.parseFloat(tokens[2]), Float.parseFloat(tokens[4]) / 100.0f, tokens[7]);
                            if ((coupled_rno != null) && (coupled_rno instanceof Localizer)) {
                                coupled_loc = (Localizer) coupled_rno;
                                coupled_loc.has_gs = true;
                            } else {
                                logger.warning("Error NAV.dat: no ILS for GS " + tokens[7] + " " + tokens[4]);
                            }
                        } else if (info_type == 12) {
                            tokens = line.split("\\s+", 9);
                            coupled_rno = nor.find_tuned_nav_object(Float.parseFloat(tokens[1]), Float.parseFloat(tokens[2]), Float.parseFloat(tokens[4]) / 100.0f, tokens[7]);
                            if (coupled_rno != null) {
                                coupled_rno.has_dme = true;
                                coupled_rno.dme_lat = Float.parseFloat(tokens[1]);
                                coupled_rno.dme_lon = Float.parseFloat(tokens[2]);
                            } else {
                                logger.config("Error NAV.dat: no VOR or Loc for DME " + tokens[7] + " " + tokens[4]);
                            }
                        }
                    } catch (Exception e) {
                        logger.warning("Parse error in " + file.getName() + ":" + line_number + " '" + line + "' (" + e + ")");
                    }
                }
            }
        }
        if (reader != null) {
            reader.close();
        }
    }

    public void read_fix_table() throws Exception {
        logger.fine("Reading NAV database ( " + this.pathname_to_aptnav + this.FIX_file + " )");
        File file = new File(this.pathname_to_aptnav + this.FIX_file);
        BufferedReader reader = new BufferedReader(new FileReader(file));
        String line;
        String[] tokens;
        long line_number = 0;
        while ((line = reader.readLine()) != null) {
            if (line.length() > 0) {
                line_number++;
                line = line.trim();
                if ((line_number > 2) && (!line.equals("99"))) {
                    try {
                        tokens = line.split("\\s+", 3);
                        nor.add_nav_object(new Fix(tokens[2], Float.parseFloat(tokens[0]), Float.parseFloat(tokens[1]), false));
                    } catch (Exception e) {
                        logger.warning("Parse error in " + file.getName() + ":" + line_number + " '" + line + "' (" + e + ")");
                    }
                }
            }
        }
        if (reader != null) {
            reader.close();
        }
    }

    public void read_awy_table() throws Exception {
        logger.fine("Reading NAV database ( " + this.pathname_to_aptnav + this.AWY_file + " )");
        File file = new File(this.pathname_to_aptnav + this.AWY_file);
        BufferedReader reader = new BufferedReader(new FileReader(file));
        String line;
        String[] tokens;
        long line_number = 0;
        while ((line = reader.readLine()) != null) {
            if (line.length() > 0) {
                line_number++;
                line = line.trim();
                if ((line_number > 2) && (!line.equals("99"))) {
                    try {
                        tokens = line.split("\\s+", 10);
                        fix = nor.get_fix(tokens[0], Float.parseFloat(tokens[1]), Float.parseFloat(tokens[2]));
                        if (fix != null) fix.on_awy = true;
                    } catch (Exception e) {
                        logger.warning("Parse error in " + file.getName() + ":" + line_number + " '" + line + "' (" + e + ")");
                    }
                }
            }
        }
        if (reader != null) {
            reader.close();
        }
    }

    public void read_apt_table() throws Exception {
        logger.fine("Reading NAV database ( " + this.pathname_to_aptnav + this.APT_file + " )");
        File file = new File(this.pathname_to_aptnav + this.APT_file);
        BufferedReader reader = new BufferedReader(new FileReader(file));
        String line;
        long line_number = 0;
        int info_type;
        String airport_icao_code = "";
        String airport_name = "";
        boolean current_airport_saved = true;
        ArrayList runways = new ArrayList();
        float width;
        int surface;
        String rwy_num1;
        float thr1_lat;
        float thr1_lon;
        String rwy_num2;
        float thr2_lat;
        float thr2_lon;
        float length;
        float lat = 0;
        float lon = 0;
        float arp_lat = 0;
        float arp_lon = 0;
        float longest = 0;
        float rwy_count = 0;
        float lat_sum = 0;
        float lon_sum = 0;
        float hard_rwy_count = 0;
        float hard_lat_sum = 0;
        float hard_lon_sum = 0;
        boolean tower = false;
        while ((line = reader.readLine()) != null) {
            if (line.length() > 0) {
                line_number++;
                line = line.trim();
                if ((line_number > 2) && (!line.equals("99"))) {
                    try {
                        info_type = Integer.parseInt(line.substring(0, 3).trim());
                        if (info_type == 1) {
                            if (!current_airport_saved) {
                                if (hard_rwy_count == 1) {
                                    arp_lat = hard_lat_sum;
                                    arp_lon = hard_lon_sum;
                                } else if (rwy_count == 1) {
                                    arp_lat = lat_sum;
                                    arp_lon = lon_sum;
                                } else if (tower) {
                                    arp_lat = lat;
                                    arp_lon = lon;
                                } else if (hard_rwy_count > 1) {
                                    arp_lat = hard_lat_sum / hard_rwy_count;
                                    arp_lon = hard_lon_sum / hard_rwy_count;
                                } else {
                                    arp_lat = lat_sum / rwy_count;
                                    arp_lon = lon_sum / rwy_count;
                                }
                                nor.add_nav_object(new Airport(airport_name, airport_icao_code, arp_lat, arp_lon, runways, longest));
                            }
                            airport_icao_code = line.substring(15, 19);
                            airport_name = line.substring(20);
                            current_airport_saved = false;
                            runways = new ArrayList();
                            arp_lat = 0;
                            arp_lon = 0;
                            longest = 0;
                            rwy_count = 0;
                            lat_sum = 0;
                            lon_sum = 0;
                            hard_rwy_count = 0;
                            hard_lat_sum = 0;
                            hard_lon_sum = 0;
                            tower = false;
                        } else if (info_type == 100) {
                            width = Float.parseFloat(line.substring(5, 11));
                            surface = Integer.parseInt(line.substring(13, 15).trim());
                            rwy_num1 = line.substring(31, 34);
                            thr1_lat = Float.parseFloat(line.substring(35, 47));
                            thr1_lon = Float.parseFloat(line.substring(48, 61));
                            rwy_num2 = line.substring(87, 90);
                            thr2_lat = Float.parseFloat(line.substring(91, 103));
                            thr2_lon = Float.parseFloat(line.substring(104, 117));
                            length = CoordinateSystem.rough_distance(thr1_lat, thr1_lon, thr2_lat, thr2_lon) * 1851.852f;
                            lat = (thr1_lat + thr2_lat) / 2;
                            lon = (thr1_lon + thr2_lon) / 2;
                            if (XHSIPreferences.get_instance().get_draw_runways()) {
                                nor.add_nav_object(new Runway(airport_icao_code, length, width, surface, rwy_num1, thr1_lat, thr1_lon, rwy_num2, thr2_lat, thr2_lon));
                                runways.add(nor.get_runway(airport_icao_code, lat, lon));
                            }
                            if (length > longest) longest = length;
                            rwy_count += 1;
                            lat_sum += lat;
                            lon_sum += lon;
                            if ((surface == Runway.RWY_ASPHALT) || (surface == Runway.RWY_CONCRETE)) {
                                hard_rwy_count += 1;
                                hard_lat_sum += lat;
                                hard_lon_sum += lon;
                            }
                        } else if (info_type == 14) {
                            tower = true;
                            lat = Float.parseFloat(line.substring(4, 16));
                            lon = Float.parseFloat(line.substring(17, 30));
                        } else if ((info_type == 99) && (current_airport_saved == false)) {
                            if (!current_airport_saved) nor.add_nav_object(new Airport(airport_name, airport_icao_code, lat, lon, runways, longest));
                            current_airport_saved = true;
                        }
                    } catch (Exception e) {
                        logger.warning("\nParse error in " + file.getName() + ":" + line_number + "(" + e + ") " + line);
                    }
                }
            }
        }
        if (reader != null) {
            reader.close();
        }
    }

    public void preference_changed(String key) {
        logger.finest("Preference changed");
        if (key.equals(XHSIPreferences.PREF_APTNAV_DIR)) {
            this.pathname_to_aptnav = XHSIPreferences.get_instance().get_preference(XHSIPreferences.PREF_APTNAV_DIR);
            if (XHSIStatus.nav_db_status.equals(XHSIStatus.STATUS_NAV_DB_NOT_FOUND) == false) {
                try {
                    logger.fine("Reload navigation tables");
                    read_all_tables();
                } catch (Exception e) {
                    logger.warning("Could not read navigation tables! (" + e.toString() + ")");
                }
            } else {
                logger.warning("Could not find AptNav Resources! (Status:" + XHSIStatus.nav_db_status + ")");
            }
        }
    }
}
