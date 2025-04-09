import pFaceDetect.*;
import processing.core.PImage;

public class FaceDetectionManager {

    public static float m_off_size_region_size_percent = 5;

    private int m_off_side_pixels;

    private PFaceDetect m_face_detector;

    private int m_watch_min_time;

    private MosaicMe m_app_reference;

    private int m_watch_counter;

    private int m_width;

    private int m_height;

    FaceDetectionManager(MosaicMe applet, int width, int height, String classifier_xml_file, int watch_min_time) {
        m_app_reference = applet;
        m_width = width;
        m_height = height;
        m_watch_counter = 0;
        m_watch_min_time = watch_min_time;
        m_face_detector = new PFaceDetect(applet, m_width, m_height, classifier_xml_file);
        m_off_side_pixels = (int) (m_off_size_region_size_percent * width * 0.01);
        System.out.println("Face detector setted W:" + m_width + " H:" + m_height + " ... OffSide:" + m_off_side_pixels);
    }

    static {
        System.loadLibrary("cxcore100");
        System.loadLibrary("cv100");
    }

    public void processImage(PImage image) {
        if (isSynchronized()) {
            m_face_detector.findFaces(image);
            if (m_face_detector.getFaces().length > 0) {
                m_watch_counter++;
            } else {
                m_watch_counter = 0;
            }
        }
    }

    public int[][] getAreaFaceDetected() {
        int positions[][] = m_face_detector.getFaces();
        if (positions.length > 0) {
            int[][] cover_area = new int[2][2];
            int[][] points = getSortPositions(positions);
            cover_area[0][0] = points[0][0];
            cover_area[0][1] = points[0][1];
            cover_area[1][0] = points[points.length - 1][0];
            cover_area[1][1] = points[points.length - 1][1];
            System.out.println("\nFace detected in area:");
            System.out.println("Xi:" + cover_area[0][0] + " Yi:" + cover_area[0][1] + " Xf:" + cover_area[1][0] + " Yf:" + cover_area[1][1]);
            cover_area[0][0] = cover_area[0][0] - m_off_side_pixels;
            if (cover_area[0][0] < 0) cover_area[0][0] = 0;
            cover_area[0][1] = cover_area[0][1] - m_off_side_pixels;
            if (cover_area[0][1] < 0) cover_area[0][1] = 0;
            cover_area[1][0] = cover_area[1][0] + m_off_side_pixels;
            if (cover_area[1][0] > m_width) cover_area[1][0] = m_width;
            cover_area[1][1] = cover_area[1][1] + m_off_side_pixels;
            if (cover_area[1][1] > m_height) cover_area[1][1] = m_height;
            return cover_area;
        }
        return null;
    }

    private int[][] getSortPositions(int positions[][]) {
        int[][] points = new int[positions.length * 2][2];
        for (int i = 0; i < positions.length; i++) {
            points[i * 2][0] = positions[i][0];
            points[i * 2][1] = positions[i][1];
            points[i * 2 + 1][0] = positions[i][0] + positions[i][2];
            points[i * 2 + 1][1] = positions[i][1] + positions[i][3];
        }
        quickSort(points, 0, 0, points.length - 1);
        quickSort(points, 1, 0, points.length - 1);
        return points;
    }

    private void quickSort(int list[][], int col_index, int lo, int hi) {
        int i = lo, j = hi;
        int h;
        int x = list[(lo + hi) / 2][col_index];
        do {
            while (list[i][col_index] < x) i++;
            while (list[j][col_index] > x) j--;
            if (i <= j) {
                h = list[i][col_index];
                list[i][col_index] = list[j][col_index];
                list[j][col_index] = h;
                i++;
                j--;
            }
        } while (i <= j);
        if (lo < j) quickSort(list, col_index, lo, j);
        if (i < hi) quickSort(list, col_index, i, hi);
    }

    private boolean isSynchronized() {
        if (m_app_reference.frameCount % ((int) m_app_reference.frameRate * m_watch_min_time) == 0) return true;
        return false;
    }

    public int[][] getFacesDetected() {
        return m_face_detector.getFaces();
    }

    public int getWatchCounter() {
        return m_watch_counter;
    }

    public void resetWatchCounter() {
        m_watch_counter = 0;
    }
}
