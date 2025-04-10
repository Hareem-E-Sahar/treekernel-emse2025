package gov.nasa.worldwind.formats.rpf;

import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.util.Logging;
import java.awt.image.*;

/**
 * @author dcollins
 * @version $Id: RPFPolarFrameTransform.java 1 2011-07-16 23:22:47Z dcollins $
 */
@SuppressWarnings({ "UnusedDeclaration" })
class RPFPolarFrameTransform extends RPFFrameTransform {

    private final char zoneCode;

    private final String rpfDataType;

    private final double resolution;

    private final RPFPolarFrameStructure frameStructure;

    private static final PixelTransformer northernPixels = new NorthPixelTransformer();

    private static final PixelTransformer southernPixels = new SouthPixelTransformer();

    private RPFPolarFrameTransform(char zoneCode, String rpfDataType, double resolution, RPFPolarFrameStructure frameStructure) {
        this.zoneCode = zoneCode;
        this.rpfDataType = rpfDataType;
        this.resolution = resolution;
        this.frameStructure = frameStructure;
    }

    static RPFPolarFrameTransform createPolarFrameTransform(char zoneCode, String rpfDataType, double resolution) {
        if (!RPFZone.isZoneCode(zoneCode)) {
            String message = Logging.getMessage("RPFZone.UnknownZoneCode", zoneCode);
            Logging.logger().fine(message);
            throw new IllegalArgumentException(message);
        }
        if (rpfDataType == null || !RPFDataSeries.isRPFDataType(rpfDataType)) {
            String message = Logging.getMessage("RPFDataSeries.UnkownDataType", rpfDataType);
            Logging.logger().fine(message);
            throw new IllegalArgumentException(message);
        }
        if (resolution < 0) {
            String message = Logging.getMessage("generic.ArgumentOutOfRange", rpfDataType);
            Logging.logger().fine(message);
            throw new IllegalArgumentException(message);
        }
        RPFPolarFrameStructure frameStructure = RPFPolarFrameStructure.computeStructure(zoneCode, rpfDataType, resolution);
        return new RPFPolarFrameTransform(zoneCode, rpfDataType, resolution, frameStructure);
    }

    public final char getZoneCode() {
        return this.zoneCode;
    }

    public final String getRpfDataType() {
        return this.rpfDataType;
    }

    public final double getResolution() {
        return this.resolution;
    }

    public final RPFFrameStructure getFrameStructure() {
        return this.frameStructure;
    }

    public int getFrameNumber(int row, int column) {
        return frameNumber(row, column, this.frameStructure.getPolarFrames());
    }

    public int getMaximumFrameNumber() {
        return maxFrameNumber(this.frameStructure.getPolarFrames(), this.frameStructure.getPolarFrames());
    }

    public int getRows() {
        return this.frameStructure.getPolarFrames();
    }

    public int getColumns() {
        return this.frameStructure.getPolarFrames();
    }

    public LatLon computeFrameOrigin(int frameNumber) {
        if (frameNumber < 0 || frameNumber > getMaximumFrameNumber()) {
            String message = Logging.getMessage("generic.ArgumentOutOfRange", frameNumber);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        int originX = pixelColumn(0, frameNumber, this.frameStructure.getPixelRowsPerFrame(), this.frameStructure.getPolarFrames());
        int originY = pixelRow(0, frameNumber, this.frameStructure.getPixelRowsPerFrame(), this.frameStructure.getPolarFrames());
        double lat, lon;
        PixelTransformer pt = (this.zoneCode == '9') ? northernPixels : southernPixels;
        lat = pt.pixel2Latitude(originX, originY, this.frameStructure.getPolarPixelConstant());
        lon = pt.pixel2Longitude(originX, originY);
        return LatLon.fromDegrees(lat, lon);
    }

    public Sector computeFrameCoverage(int frameNumber) {
        int maxFrameNumber = getMaximumFrameNumber();
        if (frameNumber < 0 || frameNumber > maxFrameNumber) {
            String message = Logging.getMessage("generic.ArgumentOutOfRange", frameNumber);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        int minX = pixelColumn(0, frameNumber, this.frameStructure.getPixelRowsPerFrame(), this.frameStructure.getPolarFrames());
        int maxY = pixelRow(0, frameNumber, this.frameStructure.getPixelRowsPerFrame(), this.frameStructure.getPolarFrames());
        int maxX = pixelColumn(this.frameStructure.getPixelRowsPerFrame(), frameNumber, this.frameStructure.getPixelRowsPerFrame(), this.frameStructure.getPolarFrames());
        int minY = pixelRow(this.frameStructure.getPixelRowsPerFrame(), frameNumber, this.frameStructure.getPixelRowsPerFrame(), this.frameStructure.getPolarFrames());
        int midX = (minX + maxX) / 2;
        int midY = (minY + maxY) / 2;
        PixelTransformer pt = (this.zoneCode == '9') ? northernPixels : southernPixels;
        MinMaxLatLon bounds = new MinMaxLatLon();
        double lat = pt.pixel2Latitude(minX, minY, this.frameStructure.getPolarPixelConstant());
        double lon = pt.pixel2Longitude(minX, minY);
        bounds.setMinMax(lat, lon);
        lat = pt.pixel2Latitude(maxX, minY, this.frameStructure.getPolarPixelConstant());
        lon = pt.pixel2Longitude(maxX, minY);
        bounds.setMinMax(lat, lon);
        lat = pt.pixel2Latitude(minX, maxY, this.frameStructure.getPolarPixelConstant());
        lon = pt.pixel2Longitude(minX, maxY);
        bounds.setMinMax(lat, lon);
        lat = pt.pixel2Latitude(maxX, maxY, this.frameStructure.getPolarPixelConstant());
        lon = pt.pixel2Longitude(maxX, maxY);
        bounds.setMinMax(lat, lon);
        lat = pt.pixel2Latitude(midX, maxY, this.frameStructure.getPolarPixelConstant());
        lon = pt.pixel2Longitude(midX, maxY);
        bounds.setMinMax(lat, lon);
        lat = pt.pixel2Latitude(maxX, midY, this.frameStructure.getPolarPixelConstant());
        lon = pt.pixel2Longitude(maxX, midY);
        bounds.setMinMax(lat, lon);
        lat = pt.pixel2Latitude(midX, minY, this.frameStructure.getPolarPixelConstant());
        lon = pt.pixel2Longitude(midX, minY);
        bounds.setMinMax(lat, lon);
        lat = pt.pixel2Latitude(minX, midY, this.frameStructure.getPolarPixelConstant());
        lon = pt.pixel2Longitude(minX, midY);
        bounds.setMinMax(lat, lon);
        lat = pt.pixel2Latitude(midX, midY, this.frameStructure.getPolarPixelConstant());
        lon = pt.pixel2Longitude(midX, midY);
        bounds.setMinMax(lat, lon);
        return Sector.fromDegrees(bounds.minLat, bounds.maxLat, bounds.minLon, bounds.maxLon);
    }

    public RPFImage[] deproject(int frameNumber, BufferedImage frame) {
        if (frame == null) {
            String message = Logging.getMessage("nullValue.ImageSource");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        PixelTransformer pt = (this.zoneCode == '9') ? northernPixels : southernPixels;
        RPFImage[] images;
        if (isDatelineSpanningFrame(frameNumber, pt)) {
            if (pt == northernPixels) images = deprojectNorthernDatelineFrames(frameNumber, frame, pt); else images = deprojectSouthernDatelineFrames(frameNumber, frame, pt);
        } else {
            Sector sector = computeFrameCoverage(frameNumber);
            BufferedImage destImage = new BufferedImage(frame.getWidth(), frame.getHeight(), BufferedImage.TYPE_4BYTE_ABGR);
            resampleFrameFile(sector, frame, destImage, frameNumber, pt);
            images = new RPFImage[1];
            images[0] = new RPFImage(sector, destImage);
        }
        return images;
    }

    private RPFImage[] deprojectNorthernDatelineFrames(int frameNumber, BufferedImage frame, PixelTransformer pt) {
        RPFImage[] images = new RPFImage[2];
        int minX = pixelColumn(0, frameNumber, this.frameStructure.getPixelRowsPerFrame(), this.frameStructure.getPolarFrames());
        int maxX = pixelColumn(this.frameStructure.getPixelRowsPerFrame(), frameNumber, this.frameStructure.getPixelRowsPerFrame(), this.frameStructure.getPolarFrames());
        int minY = pixelRow(0, frameNumber, this.frameStructure.getPixelRowsPerFrame(), this.frameStructure.getPolarFrames());
        int maxY = pixelRow(this.frameStructure.getPixelRowsPerFrame(), frameNumber, this.frameStructure.getPixelRowsPerFrame(), this.frameStructure.getPolarFrames());
        int midX = (minX + maxX) / 2;
        int midY = (minY + maxY) / 2;
        MinMaxLatLon bndsWest = new MinMaxLatLon();
        bndsWest.minLon = -180.;
        if (isCenterFrame(frameNumber)) {
            bndsWest.maxLon = 0.;
            bndsWest.maxLat = pt.pixel2Latitude(midX, midY, this.frameStructure.getPolarPixelConstant());
            bndsWest.minLat = pt.pixel2Latitude(minX, minY, this.frameStructure.getPolarPixelConstant());
        } else {
            bndsWest.minLat = pt.pixel2Latitude(minX, minY, this.frameStructure.getPolarPixelConstant());
            bndsWest.maxLat = pt.pixel2Latitude(midX, maxY, this.frameStructure.getPolarPixelConstant());
            bndsWest.maxLon = pt.pixel2Longitude(minX, maxY);
        }
        Sector sector = Sector.fromDegrees(bndsWest.minLat, bndsWest.maxLat, bndsWest.minLon, bndsWest.maxLon);
        BufferedImage destImage = new BufferedImage(frame.getWidth(), frame.getHeight(), BufferedImage.TYPE_4BYTE_ABGR);
        resampleFrameFile(sector, frame, destImage, frameNumber, pt);
        images[0] = new RPFImage(sector, destImage);
        MinMaxLatLon bndsEast = new MinMaxLatLon();
        bndsEast.minLat = bndsWest.minLat;
        bndsEast.maxLat = bndsWest.maxLat;
        if (isCenterFrame(frameNumber)) {
            bndsEast.minLon = 0.;
            bndsEast.maxLon = 180.;
        } else {
            bndsEast.minLon = pt.pixel2Longitude(maxX, maxY);
            bndsEast.maxLon = 180.;
        }
        sector = Sector.fromDegrees(bndsEast.minLat, bndsEast.maxLat, bndsEast.minLon, bndsEast.maxLon);
        destImage = new BufferedImage(frame.getWidth(), frame.getHeight(), BufferedImage.TYPE_4BYTE_ABGR);
        resampleFrameFile(sector, frame, destImage, frameNumber, pt);
        images[1] = new RPFImage(sector, destImage);
        return images;
    }

    private RPFImage[] deprojectSouthernDatelineFrames(int frameNumber, BufferedImage frame, PixelTransformer pt) {
        RPFImage[] images = new RPFImage[2];
        int minX = pixelColumn(0, frameNumber, this.frameStructure.getPixelRowsPerFrame(), this.frameStructure.getPolarFrames());
        int maxX = pixelColumn(this.frameStructure.getPixelRowsPerFrame(), frameNumber, this.frameStructure.getPixelRowsPerFrame(), this.frameStructure.getPolarFrames());
        int minY = pixelRow(0, frameNumber, this.frameStructure.getPixelRowsPerFrame(), this.frameStructure.getPolarFrames());
        int maxY = pixelRow(this.frameStructure.getPixelRowsPerFrame(), frameNumber, this.frameStructure.getPixelRowsPerFrame(), this.frameStructure.getPolarFrames());
        int midX = (minX + maxX) / 2;
        int midY = (minY + maxY) / 2;
        MinMaxLatLon bndsWest = new MinMaxLatLon();
        bndsWest.minLon = -180.;
        if (isCenterFrame(frameNumber)) {
            bndsWest.maxLon = 0.;
            bndsWest.maxLat = pt.pixel2Latitude(midX, midY, this.frameStructure.getPolarPixelConstant());
            bndsWest.minLat = pt.pixel2Latitude(minX, minY, this.frameStructure.getPolarPixelConstant());
        } else {
            bndsWest.minLat = pt.pixel2Latitude(minX, maxY, this.frameStructure.getPolarPixelConstant());
            bndsWest.maxLat = pt.pixel2Latitude(midX, minY, this.frameStructure.getPolarPixelConstant());
            bndsWest.maxLon = pt.pixel2Longitude(minX, minY);
        }
        Sector sector = Sector.fromDegrees(bndsWest.minLat, bndsWest.maxLat, bndsWest.minLon, bndsWest.maxLon);
        BufferedImage destImage = new BufferedImage(frame.getWidth(), frame.getHeight(), BufferedImage.TYPE_4BYTE_ABGR);
        resampleFrameFile(sector, frame, destImage, frameNumber, pt);
        images[0] = new RPFImage(sector, destImage);
        MinMaxLatLon bndsEast = new MinMaxLatLon();
        bndsEast.minLat = bndsWest.minLat;
        bndsEast.maxLat = bndsWest.maxLat;
        if (isCenterFrame(frameNumber)) {
            bndsEast.minLon = 0.;
            bndsEast.maxLon = 180.;
        } else {
            bndsEast.minLon = pt.pixel2Longitude(maxX, minY);
            bndsEast.maxLon = 180.;
        }
        sector = Sector.fromDegrees(bndsEast.minLat, bndsEast.maxLat, bndsEast.minLon, bndsEast.maxLon);
        destImage = new BufferedImage(frame.getWidth(), frame.getHeight(), BufferedImage.TYPE_4BYTE_ABGR);
        resampleFrameFile(sector, frame, destImage, frameNumber, pt);
        images[1] = new RPFImage(sector, destImage);
        return images;
    }

    private void resampleFrameFile(Sector sector, BufferedImage srcImage, BufferedImage destImage, int frameNumber, PixelTransformer pt) {
        int frameULX = pixelColumn(0, frameNumber, this.frameStructure.getPixelRowsPerFrame(), this.frameStructure.getPolarFrames());
        int frameULY = pixelRow(0, frameNumber, this.frameStructure.getPixelRowsPerFrame(), this.frameStructure.getPolarFrames());
        int width = destImage.getWidth();
        int height = destImage.getHeight();
        double deltaLon = (sector.getMaxLongitude().degrees - sector.getMinLongitude().degrees) / width;
        double deltaLat = (sector.getMaxLatitude().degrees - sector.getMinLatitude().degrees) / height;
        double minLon = sector.getMinLongitude().degrees;
        double minLat = sector.getMinLatitude().degrees;
        double polarConstant = this.frameStructure.getPolarPixelConstant();
        int srcWidth = srcImage.getWidth();
        int srcHeight = srcImage.getHeight();
        for (int y = 0; y < height; y++) {
            double lat = minLat + y * deltaLat;
            for (int x = 0; x < width; x++) {
                double lon = minLon + x * deltaLon;
                int pixelX = pt.latLon2X(lat, lon, polarConstant);
                int pixelY = pt.latLon2Y(lat, lon, polarConstant);
                int i = pixelX - frameULX;
                int j = frameULY - pixelY;
                if (i < 0 || i >= srcWidth || j < 0 || j >= srcHeight) continue;
                int color = srcImage.getRGB(i, j);
                if ((color & 0x00FFFFFF) == 0) color = 0;
                destImage.setRGB(x, height - 1 - y, color);
            }
        }
    }

    private boolean isDatelineSpanningFrame(int frameNumber, PixelTransformer pt) {
        int row = frameNumber / getColumns();
        int col = frameNumber % getColumns();
        if (pt == northernPixels) return (row >= (getRows() / 2) && col == (getColumns() / 2)); else return (row <= (getRows() / 2) && col == (getColumns() / 2));
    }

    private boolean isCenterFrame(int frameNumber) {
        int row = frameNumber / getRows();
        int col = frameNumber % getColumns();
        return (row == (getRows() / 2) && col == (getColumns() / 2));
    }

    private static int pixelRow(int rowInFrame, int frameNumber, int pixelsPerFrameRow, int numFrames) {
        int row = frameRow(frameNumber, numFrames);
        return ((row + 1) * pixelsPerFrameRow - rowInFrame) - (numFrames * pixelsPerFrameRow / 2);
    }

    private static int pixelColumn(int colInFrame, int frameNumber, int pixelsPerFrameRow, int numFrames) {
        int row = frameRow(frameNumber, numFrames);
        int col = frameColumn(frameNumber, row, numFrames);
        return (col * pixelsPerFrameRow + colInFrame) - (numFrames * pixelsPerFrameRow / 2);
    }

    private interface PixelTransformer {

        public double pixel2Latitude(int x, int y, double polarPixelConstant);

        public double pixel2Longitude(int x, int y);

        public int latLon2X(double lat, double lon, double polarPixelConstant);

        public int latLon2Y(double lat, double lon, double polarPixelConstant);
    }

    private static class NorthPixelTransformer implements PixelTransformer {

        public double pixel2Latitude(int x, int y, double polarPixelConstant) {
            return 90. - (Math.sqrt(x * x + y * y) / (polarPixelConstant / 360.));
        }

        public double pixel2Longitude(int x, int y) {
            if (x == 0 && y > 0) return 180.;
            if (x == 0 && y <= 0) return 0.;
            double lambda = Math.acos(-y / Math.sqrt(x * x + y * y)) * 180 / Math.PI;
            return (x > 0) ? lambda : -lambda;
        }

        public int latLon2X(double lat, double lon, double polarPixelConstant) {
            return (int) (polarPixelConstant / 360. * (90. - lat) * Math.sin(lon * Math.PI / 180.));
        }

        public int latLon2Y(double lat, double lon, double polarPixelConstant) {
            return (int) (-polarPixelConstant / 360. * (90. - lat) * Math.cos(lon * Math.PI / 180.));
        }
    }

    private static class SouthPixelTransformer implements PixelTransformer {

        public double pixel2Latitude(int x, int y, double polarPixelConstant) {
            return -90. + (Math.sqrt(x * x + y * y) / (polarPixelConstant / 360.));
        }

        public double pixel2Longitude(int x, int y) {
            if (x == 0 && y > 0) return 0.;
            if (x == 0 && y <= 0) return 180.;
            double lambda = Math.acos(y / Math.sqrt(x * x + y * y)) * 180 / Math.PI;
            return (x > 0) ? lambda : -lambda;
        }

        public int latLon2X(double lat, double lon, double polarPixelConstant) {
            return (int) (polarPixelConstant / 360. * (90. + lat) * Math.sin(lon * Math.PI / 180.));
        }

        public int latLon2Y(double lat, double lon, double polarPixelConstant) {
            return (int) (polarPixelConstant / 360. * (90. + lat) * Math.cos(lon * Math.PI / 180.));
        }
    }

    private class MinMaxLatLon {

        double minLon, minLat, maxLon, maxLat;

        public MinMaxLatLon() {
            minLon = minLat = Double.MAX_VALUE;
            maxLon = maxLat = -Double.MAX_VALUE;
        }

        public void setMinMax(double lat, double lon) {
            if (lon < this.minLon) this.minLon = lon;
            if (lat < this.minLat) this.minLat = lat;
            if (lon > this.maxLon) this.maxLon = lon;
            if (lat > this.maxLat) this.maxLat = lat;
            if (lon == 180) setMinMax(lat, -lon);
        }
    }
}
