package au.com.kelpie.fgfp.parsers;

import java.io.BufferedReader;
import java.io.EOFException;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.StringTokenizer;
import org.eclipse.core.runtime.IProgressMonitor;
import au.com.kelpie.earth.Coordinate;
import au.com.kelpie.fgfp.Messages;
import au.com.kelpie.fgfp.model.Airport;
import au.com.kelpie.fgfp.model.LayoutNode;
import au.com.kelpie.fgfp.model.Runway;
import au.com.kelpie.fgfp.model.Taxiway;

/**
 * Parser for the flight gear airports file. Data version 850) Reads and parses the airports
 * file into an Collection of java objects
 */
public class AirportParserFG850 implements IAirportParserFG {

    private final HashMap<String, Airport> _airportMap = new HashMap<String, Airport>(25000);

    private final HashMap<String, Runway> _runwayMap = new HashMap<String, Runway>(25000);

    private final HashMap<String, Runway> _runwayOppositeMap = new HashMap<String, Runway>(25000);

    private static final HashMap<String, Long> _runwayOffsets = new HashMap<String, Long>(25000);

    /**
     * Parse the airport file and load into the Airport Collection
     * 
     * @param monitor
     * @param map
     * @param string
     */
    public void loadAirports(final List<Airport> airports, final BufferedReader in, final IProgressMonitor monitor) throws IOException {
        StringTokenizer tokenizer;
        Airport airport = null;
        int airportCount = 0;
        long offset = 0;
        String buf = in.readLine();
        offset += buf.length() + 1;
        buf = in.readLine();
        offset += buf.length() + 1;
        buf = in.readLine();
        try {
            while (true) {
                Thread.yield();
                if (buf == null) {
                    break;
                }
                if (buf.length() > 0) {
                    tokenizer = new StringTokenizer(buf);
                    String rType = tokenizer.nextToken();
                    if (rType.equals("1") || rType.equals("16") || rType.equals("17")) {
                        final double elevation = new Double(tokenizer.nextToken()).doubleValue();
                        final boolean tower = tokenizer.nextToken().equals("1");
                        final boolean defaultBuildings = tokenizer.nextToken().equals("1");
                        final String id = tokenizer.nextToken();
                        final StringBuffer name = new StringBuffer();
                        while (tokenizer.hasMoreTokens()) {
                            name.append(tokenizer.nextToken());
                            name.append(" ");
                        }
                        _runwayOffsets.put(id, new Long(offset));
                        long maxLength = 0;
                        double latitude = 0;
                        double longitude = 0;
                        offset += buf.length() + 1;
                        buf = in.readLine();
                        while (true) {
                            if (buf == null) {
                                break;
                            }
                            if (buf.length() > 0) {
                                tokenizer = new StringTokenizer(buf);
                                rType = tokenizer.nextToken();
                                if (rType.equals("1") || rType.equals("16") || rType.equals("17")) {
                                    break;
                                }
                                if (rType.equals("100")) {
                                    tokenizer.nextToken();
                                    tokenizer.nextToken();
                                    tokenizer.nextToken();
                                    tokenizer.nextToken();
                                    tokenizer.nextToken();
                                    tokenizer.nextToken();
                                    tokenizer.nextToken();
                                    @SuppressWarnings("unused") final String r_number = tokenizer.nextToken();
                                    final double r_lat = new Double(tokenizer.nextToken()).doubleValue();
                                    final double r_long = new Double(tokenizer.nextToken()).doubleValue();
                                    tokenizer.nextToken();
                                    tokenizer.nextToken();
                                    tokenizer.nextToken();
                                    tokenizer.nextToken();
                                    tokenizer.nextToken();
                                    tokenizer.nextToken();
                                    @SuppressWarnings("unused") final String r1_number = tokenizer.nextToken();
                                    final double r1_lat = new Double(tokenizer.nextToken()).doubleValue();
                                    final double r1_long = new Double(tokenizer.nextToken()).doubleValue();
                                    tokenizer.nextToken();
                                    tokenizer.nextToken();
                                    tokenizer.nextToken();
                                    tokenizer.nextToken();
                                    tokenizer.nextToken();
                                    tokenizer.nextToken();
                                    Coordinate c1 = new Coordinate(r_lat, r_long);
                                    Coordinate c2 = new Coordinate(r1_lat, r1_long);
                                    final int r_length = (int) (c1.distanceTo(c2) * 6076);
                                    if (r_length > maxLength) {
                                        maxLength = r_length;
                                        latitude = (r_lat + r1_lat) / 2;
                                        longitude = (r_long + r1_long) / 2;
                                    }
                                }
                                if (rType.equals("101")) {
                                    tokenizer.nextToken();
                                    tokenizer.nextToken();
                                    tokenizer.nextToken();
                                    latitude = new Double(tokenizer.nextToken()).doubleValue();
                                    longitude = new Double(tokenizer.nextToken()).doubleValue();
                                }
                                if (rType.equals("102")) {
                                    tokenizer.nextToken();
                                    latitude = new Double(tokenizer.nextToken()).doubleValue();
                                    longitude = new Double(tokenizer.nextToken()).doubleValue();
                                }
                            }
                            offset += buf.length() + 1;
                            buf = in.readLine();
                        }
                        airport = new Airport(id, latitude, longitude, elevation, "", tower, defaultBuildings, name.toString().trim(), maxLength);
                        airports.add(airport);
                        airportCount++;
                        if (airportCount % 1000 == 0) {
                            monitor.subTask(MessageFormat.format(Messages.getString("AirportParser.3"), airportCount));
                            monitor.worked(1000);
                        }
                        _airportMap.put(id, airport);
                    } else {
                        offset += buf.length() + 1;
                        buf = in.readLine();
                    }
                } else {
                    offset += buf.length() + 1;
                    buf = in.readLine();
                }
            }
        } catch (final EOFException e) {
            e.printStackTrace();
        } catch (final Exception e) {
            e.printStackTrace();
        }
    }

    public synchronized void loadRunways(final Airport airport, final BufferedReader rdrAirport, final BufferedReader rdrRunwayIls) throws IOException {
        StringTokenizer tokenizer;
        try {
            final long offset = (_runwayOffsets.get(airport.getId())).longValue();
            rdrAirport.skip(offset);
            while (true) {
                Thread.yield();
                final String buf = rdrAirport.readLine();
                if (buf == null) {
                    break;
                }
                if (buf.length() > 0) {
                    tokenizer = new StringTokenizer(buf);
                    final String rType = tokenizer.nextToken();
                    if (rType.equals("1") || rType.equals("16") || rType.equals("17")) {
                        @SuppressWarnings("unused") final double elevation = new Double(tokenizer.nextToken()).doubleValue();
                        @SuppressWarnings("unused") final boolean tower = tokenizer.nextToken().equals("1");
                        @SuppressWarnings("unused") final boolean defaultBuildings = tokenizer.nextToken().equals("1");
                        final String id = tokenizer.nextToken();
                        if (airport.getId().equals(id)) {
                            loadRunwaysForAirport(airport, rdrRunwayIls, rdrAirport);
                            return;
                        }
                    }
                }
            }
        } catch (final EOFException e) {
        }
    }

    /**
     * @param airport
     * @param runwayIlsPath
     * @param in
     * @throws IOException
     */
    private void loadRunwaysForAirport(final Airport airport, final BufferedReader rdrRunwayIls, final BufferedReader rdrAirport) throws IOException {
        StringTokenizer tokenizer;
        String buf;
        boolean match = true;
        buf = rdrAirport.readLine();
        while (match) {
            Thread.yield();
            if (buf == null) {
                break;
            }
            if (buf.length() > 0) {
                tokenizer = new StringTokenizer(buf);
                final String rType = tokenizer.nextToken();
                if (rType.equals("100")) {
                    final int r_width = (int) (new Double(tokenizer.nextToken()).doubleValue() * 3.28);
                    final String r_surface = tokenizer.nextToken();
                    tokenizer.nextToken();
                    tokenizer.nextToken();
                    tokenizer.nextToken();
                    final String r_edgeLights = tokenizer.nextToken();
                    tokenizer.nextToken();
                    final String r_number = tokenizer.nextToken();
                    final double r_lat = new Double(tokenizer.nextToken()).doubleValue();
                    final double r_long = new Double(tokenizer.nextToken()).doubleValue();
                    tokenizer.nextToken();
                    tokenizer.nextToken();
                    final String r_markings = tokenizer.nextToken();
                    tokenizer.nextToken();
                    tokenizer.nextToken();
                    tokenizer.nextToken();
                    @SuppressWarnings("unused") final String r1_number = tokenizer.nextToken();
                    final double r1_lat = new Double(tokenizer.nextToken()).doubleValue();
                    final double r1_long = new Double(tokenizer.nextToken()).doubleValue();
                    tokenizer.nextToken();
                    tokenizer.nextToken();
                    tokenizer.nextToken();
                    tokenizer.nextToken();
                    tokenizer.nextToken();
                    tokenizer.nextToken();
                    Coordinate c1 = new Coordinate(r_lat, r_long);
                    Coordinate c2 = new Coordinate(r1_lat, r1_long);
                    final int r_length = (int) (c1.distanceTo(c2) * 6076);
                    final double r_hdg = Math.toDegrees(c1.bearingTo(c2));
                    final double latitude = (r_lat + r1_lat) / 2;
                    final double longitude = (r_long + r1_long) / 2;
                    final Runway runway = new Runway(r_number, latitude, longitude, r_length, r_width, r_hdg, false, r_surface, r_edgeLights, r_markings);
                    airport.addRunway(runway);
                    _runwayMap.put(airport.getId() + runway.getNumber(), runway);
                    _runwayOppositeMap.put(airport.getId() + runway.getOppositeNumber(), runway);
                    buf = rdrAirport.readLine();
                } else if (rType.equals("110")) {
                    List<LayoutNode> nodes = new ArrayList<LayoutNode>();
                    while (true) {
                        buf = rdrAirport.readLine();
                        if (buf == null) {
                            break;
                        }
                        if (buf.length() > 0) {
                            tokenizer = new StringTokenizer(buf);
                            final String nType = tokenizer.nextToken();
                            if (nType.equals("111") || nType.equals("113") || nType.equals("115")) {
                                final double r1_lat = new Double(tokenizer.nextToken()).doubleValue();
                                final double r1_long = new Double(tokenizer.nextToken()).doubleValue();
                                nodes.add(new LayoutNode(nType, r1_lat, r1_long));
                            } else if (nType.equals("112") || nType.equals("114") || nType.equals("116")) {
                                final double r1_lat = new Double(tokenizer.nextToken()).doubleValue();
                                final double r1_long = new Double(tokenizer.nextToken()).doubleValue();
                                final double b1_lat = new Double(tokenizer.nextToken()).doubleValue();
                                final double b1_long = new Double(tokenizer.nextToken()).doubleValue();
                                nodes.add(new LayoutNode(nType, r1_lat, r1_long, b1_lat, b1_long));
                            } else {
                                break;
                            }
                        }
                    }
                    Taxiway taxiway = new Taxiway(null, nodes, false, "", "");
                    airport.addTaxiway(taxiway);
                } else if (rType.equals("1") || rType.equals("16") || rType.equals("17")) {
                    match = false;
                } else {
                    buf = rdrAirport.readLine();
                }
            } else {
                buf = rdrAirport.readLine();
            }
        }
        loadIls(rdrRunwayIls, airport);
    }

    /**
     * Parse the ils file and add to runway data
     * 
     * @param monitor
     * @param airports
     * @param string
     */
    private void loadIls(final BufferedReader rdrRunwayIls, final Airport airport) throws IOException {
        StringTokenizer tokenizer;
        while (true) {
            Thread.yield();
            final String buf = rdrRunwayIls.readLine();
            if (buf == null) {
                break;
            }
            if (buf.length() > 0) {
                switch(buf.charAt(0)) {
                    case '4':
                    case '5':
                        tokenizer = new StringTokenizer(buf);
                        tokenizer.nextToken();
                        tokenizer.nextToken();
                        tokenizer.nextToken();
                        tokenizer.nextToken();
                        final double freq = new Double(tokenizer.nextToken()).doubleValue();
                        tokenizer.nextToken();
                        tokenizer.nextToken();
                        tokenizer.nextToken();
                        final String airportId = tokenizer.nextToken();
                        final String runwayId = tokenizer.nextToken();
                        if (airportId.equals(airport.getId())) {
                            for (final Runway runway : airport.getRunways()) {
                                if (runway.getNumber().equals(runwayId)) {
                                    runway.setIlsFreq(freq / 100);
                                }
                                if (runway.getOppositeNumber().equals(runwayId)) {
                                    runway.setOppositeIlsFreq(freq / 100);
                                }
                            }
                        }
                        break;
                }
            }
        }
    }
}
