package org.sam.tools;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.io.Reader;
import java.io.StreamTokenizer;
import java.util.ArrayList;
import java.util.List;
import javax.imageio.ImageIO;
import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.text.DefaultStyledDocument;
import javax.vecmath.Matrix4d;
import javax.vecmath.Point3f;
import javax.vecmath.Vector3d;
import org.sam.colisiones.Poligono;
import org.sam.util.Consola;
import org.sam.util.Reflexion;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import com.thoughtworks.xstream.io.xml.DomDriver;

public class GeneradorDeSiluetas {

    @SuppressWarnings("serial")
    public static class ParsingErrorException extends RuntimeException {

        public ParsingErrorException() {
            super();
        }

        public ParsingErrorException(String s) {
            super(s);
        }
    }

    private static class ObjParser extends StreamTokenizer {

        private static final char BACKSLASH = '\\';

        ObjParser(Reader r) {
            super(r);
            setup();
        }

        void setup() {
            resetSyntax();
            eolIsSignificant(true);
            lowerCaseMode(true);
            wordChars('!', '~');
            commentChar('!');
            commentChar('#');
            whitespaceChars(' ', ' ');
            whitespaceChars('\n', '\n');
            whitespaceChars('\r', '\r');
            whitespaceChars('\t', '\t');
            ordinaryChar('/');
            ordinaryChar(BACKSLASH);
        }

        void getToken() throws ParsingErrorException {
            try {
                while (true) {
                    nextToken();
                    if (ttype != ObjParser.BACKSLASH) break;
                    nextToken();
                    if (ttype != StreamTokenizer.TT_EOL) break;
                }
            } catch (IOException e) {
                throw new ParsingErrorException("IO error on line " + lineno() + ": " + e.getMessage());
            }
        }

        void skipToNextLine() throws ParsingErrorException {
            while (ttype != StreamTokenizer.TT_EOF && ttype != StreamTokenizer.TT_EOL) {
                getToken();
            }
        }

        float getFloat() throws ParsingErrorException {
            do {
                getToken();
            } while (ttype == StreamTokenizer.TT_EOL);
            return getLastValueAsFloat();
        }

        float getLastValueAsFloat() throws ParsingErrorException {
            try {
                if (ttype == StreamTokenizer.TT_WORD) return (Double.valueOf(sval)).floatValue();
                throw new ParsingErrorException("Expected number on line " + lineno());
            } catch (NumberFormatException e) {
                throw new ParsingErrorException(e.getMessage());
            }
        }

        int getLastValueAsInteger() throws ParsingErrorException {
            try {
                if (ttype == StreamTokenizer.TT_WORD) return (Integer.valueOf(sval)).intValue();
                throw new ParsingErrorException("Expected number on line " + lineno());
            } catch (NumberFormatException e) {
                throw new ParsingErrorException(e.getMessage());
            }
        }
    }

    private static class Line {

        final int p1;

        final int p2;

        Line(int p1, int p2) {
            this.p1 = p1;
            this.p2 = p2;
        }
    }

    private static class ObjLoader {

        final List<Point3f> vertList;

        final List<Line> lines;

        ObjLoader(final Reader reader) {
            this(reader, false, null);
        }

        ObjLoader(final Reader reader, boolean resize, Matrix4d mt) {
            vertList = new ArrayList<Point3f>(128);
            lines = new ArrayList<Line>(128);
            read(new ObjParser(new BufferedReader(reader)));
            if (mt != null || resize) {
                Matrix4d m;
                if (mt != null) {
                    m = new Matrix4d(mt);
                    if (resize) m.mul(escalarYCentrar());
                } else m = escalarYCentrar();
                for (Point3f v : vertList) m.transform(v);
            }
        }

        private transient float minX, maxX;

        private transient float minY, maxY;

        private transient float minZ, maxZ;

        private void read(ObjParser st) throws ParsingErrorException {
            minX = Float.MAX_VALUE;
            maxX = -Float.MAX_VALUE;
            minY = Float.MAX_VALUE;
            maxY = -Float.MAX_VALUE;
            minZ = Float.MAX_VALUE;
            maxZ = -Float.MAX_VALUE;
            do {
                st.getToken();
                if (st.ttype == StreamTokenizer.TT_WORD) {
                    if (st.sval.equals("v")) readVertex(st); else if (st.sval.equals("vn")) st.skipToNextLine(); else if (st.sval.equals("vt")) st.skipToNextLine(); else if (st.sval.equals("f")) {
                        readFace(st);
                    } else if (st.sval.equals("fo")) {
                        readFace(st);
                    } else if (st.sval.equals("g")) st.skipToNextLine(); else if (st.sval.equals("s")) st.skipToNextLine(); else if (st.sval.equals("p")) st.skipToNextLine(); else if (st.sval.equals("l")) st.skipToNextLine(); else if (st.sval.equals("mtllib")) st.skipToNextLine(); else if (st.sval.equals("usemtl")) st.skipToNextLine(); else if (st.sval.equals("maplib")) st.skipToNextLine(); else if (st.sval.equals("usemap")) st.skipToNextLine(); else throw new ParsingErrorException("Unrecognized token, line " + st.lineno());
                }
                st.skipToNextLine();
            } while (st.ttype != StreamTokenizer.TT_EOF);
        }

        private void readVertex(ObjParser st) throws ParsingErrorException {
            Point3f v = new Point3f();
            v.x = st.getFloat();
            v.y = st.getFloat();
            v.z = st.getFloat();
            if (v.x < minX) minX = v.x;
            if (v.x > maxX) maxX = v.x;
            if (v.y < minY) minY = v.y;
            if (v.y > maxY) maxY = v.y;
            if (v.z < minZ) minZ = v.z;
            if (v.z > maxZ) maxZ = v.z;
            vertList.add(v);
        }

        private void readFace(ObjParser st) throws ParsingErrorException {
            int[] vertIndex = new int[4];
            int count = 0;
            st.getToken();
            do {
                if (st.ttype == StreamTokenizer.TT_WORD) {
                    vertIndex[count] = st.getLastValueAsInteger() - 1;
                    if (vertIndex[count] < 0) vertIndex[count] += vertList.size() + 1;
                }
                st.getToken();
                if (st.ttype == '/') {
                    st.getToken();
                    if (st.ttype == StreamTokenizer.TT_WORD) {
                        st.getToken();
                    }
                    if (st.ttype == '/') {
                        st.getToken();
                        if (st.ttype == StreamTokenizer.TT_WORD) {
                            st.getToken();
                        }
                    }
                }
                count++;
            } while (st.ttype != StreamTokenizer.TT_EOF && st.ttype != StreamTokenizer.TT_EOL);
            for (int i = 0; i < count - 1; i++) {
                lines.add(new Line(vertIndex[i], vertIndex[i + 1]));
            }
            lines.add(new Line(vertIndex[count - 1], vertIndex[0]));
        }

        private double getMaxDistance() {
            double absX = maxX - minX;
            double absY = maxY - minY;
            double absZ = maxZ - minZ;
            return Math.max(absX, Math.max(absY, absZ));
        }

        private Matrix4d escalarYCentrar() {
            double centerX = (minX + maxX) / 2;
            double centerY = (minY + maxY) / 2;
            double centerZ = (minZ + maxZ) / 2;
            double scale = 1.0 / Math.max(1.0, getMaxDistance());
            Matrix4d mt = new Matrix4d();
            mt.set(scale, new Vector3d(-centerX * scale, -centerY * scale, -centerZ * scale));
            return mt;
        }
    }

    static void paintProjection(Graphics2D g2d, List<Point3f> vertList, List<Line> lines, int w, int h) {
        g2d.setBackground(Color.BLACK);
        g2d.clearRect(0, 0, w, h);
        if (vertList == null || lines == null) return;
        int min = Math.min(w, h);
        int off_W = w / 2;
        int off_H = h / 2;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        for (Line line : lines) {
            Point3f v1 = vertList.get(line.p1);
            Point3f v2 = vertList.get(line.p2);
            g2d.setColor(Color.GRAY);
            g2d.drawRect((int) (v1.x * min) + off_W - 1, (int) (-v1.y * min) + off_H - 1, 2, 2);
            g2d.drawRect((int) (v2.x * min) + off_W - 1, (int) (-v2.y * min) + off_H - 1, 2, 2);
            g2d.setColor(Color.DARK_GRAY);
            g2d.drawLine((int) (v1.x * min) + off_W, (int) (-v1.y * min) + off_H, (int) (v2.x * min) + off_W, (int) (-v2.y * min) + off_H);
        }
    }

    private static void export(final Reader reader, final int w, final int h, final String formato, final String rutaFichero) throws ParsingErrorException, IOException {
        ObjLoader loader = new ObjLoader(reader);
        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        paintProjection(img.createGraphics(), loader.vertList, loader.lines, w, h);
        ImageIO.write(img, formato, new File(rutaFichero));
    }

    public static void export(String filename, final int w, final int h, final String formato, final String rutaFichero) throws ParsingErrorException, IOException {
        export(new FileReader(filename), w, h, formato, rutaFichero);
    }

    private static class S {

        private static final String Poligono = "Poligono";

        private static final String nLados = "nLados";

        private static final String coordX = "coordX";

        private static final String coordY = "coordY";

        private static final String Punto2F = "Punto2F";

        private static final String x = "x";

        private static final String y = "y";
    }

    static float[] getArray(Poligono p, String name) {
        java.lang.reflect.Field f = Reflexion.findField(Poligono.class, name);
        float[] array = null;
        try {
            boolean accesible = f.isAccessible();
            f.setAccessible(true);
            array = (float[]) f.get(p);
            f.setAccessible(accesible);
            return array;
        } catch (IllegalAccessException ignorada) {
            return null;
        }
    }

    private static class PoligonoConverter implements Converter {

        PoligonoConverter() {
        }

        @SuppressWarnings("rawtypes")
        public boolean canConvert(Class clazz) {
            return Poligono.class == clazz;
        }

        public void marshal(Object value, HierarchicalStreamWriter writer, MarshallingContext context) {
            Poligono p = (Poligono) value;
            int nLados = p.getNLados();
            writer.addAttribute(S.nLados, ((Integer) nLados).toString());
            float coordX[] = getArray(p, S.coordX);
            float coordY[] = getArray(p, S.coordY);
            for (int i = 0; i < nLados; i++) {
                writer.startNode(S.Punto2F);
                writer.addAttribute(S.x, ((Float) coordX[i]).toString());
                writer.addAttribute(S.y, ((Float) coordY[i]).toString());
                writer.endNode();
            }
        }

        public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext context) {
            int nLados = Integer.parseInt(reader.getAttribute(S.nLados));
            float coordX[] = new float[nLados];
            float coordY[] = new float[nLados];
            int i = 0;
            while (i < nLados && reader.hasMoreChildren()) {
                reader.moveDown();
                coordX[i] = Float.parseFloat(reader.getAttribute(S.x));
                coordY[i] = Float.parseFloat(reader.getAttribute(S.y));
                reader.moveUp();
                i++;
            }
            return new Poligono(coordX, coordY);
        }
    }

    @SuppressWarnings("serial")
    private static class PantallaGrafica extends JComponent implements MouseListener, MouseMotionListener {

        List<Point3f> vertList;

        List<Line> lines;

        float coordX[];

        float coordY[];

        PantallaGrafica() {
            this.addMouseListener(this);
            this.addMouseMotionListener(this);
        }

        public void setModel(List<Point3f> vertList, List<Line> lines) {
            this.vertList = vertList;
            this.lines = lines;
            repaint();
        }

        public void setPoligono(Poligono p) {
            coordX = getArray(p, S.coordX);
            coordY = getArray(p, S.coordY);
            repaint();
        }

        public void paintComponent(Graphics g) {
            int w = this.getWidth();
            int h = this.getHeight();
            paintProjection((Graphics2D) g, vertList, lines, w, h);
            if (coordX != null && coordY != null) {
                int min = Math.min(w, h);
                int off_W = w / 2;
                int off_H = h / 2;
                for (int i = 0; i < coordX.length - 1; i++) {
                    g.setColor(Color.CYAN);
                    g.drawRect((int) (coordX[i] * min) + off_W - 1, (int) (-coordY[i] * min) + off_H - 1, 2, 2);
                    g.setColor(Color.YELLOW);
                    g.drawLine((int) (coordX[i] * min) + off_W, (int) (-coordY[i] * min) + off_H, (int) (coordX[i + 1] * min) + off_W, (int) (-coordY[i + 1] * min) + off_H);
                }
                g.setColor(Color.CYAN);
                g.drawRect((int) (coordX[coordX.length - 1] * min) + off_W - 1, (int) (-coordY[coordX.length - 1] * min) + off_H - 1, 2, 2);
                g.setColor(Color.YELLOW);
                g.drawLine((int) (coordX[coordX.length - 1] * min) + off_W, (int) (-coordY[coordX.length - 1] * min) + off_H, (int) (coordX[0] * min) + off_W, (int) (-coordY[0] * min) + off_H);
            }
        }

        @Override
        public void mouseClicked(MouseEvent e) {
        }

        @Override
        public void mouseEntered(MouseEvent e) {
        }

        @Override
        public void mouseExited(MouseEvent e) {
        }

        int findPoint(int x, int y) {
            if (coordX == null || coordY == null) return -1;
            int w = this.getWidth();
            int h = this.getHeight();
            int min = Math.min(w, h);
            int off_W = w / 2;
            int off_H = h / 2;
            float fx = (float) (x - off_W) / min;
            float fy = (float) (off_H - y) / min;
            int pos = -1;
            float distance = Float.MAX_VALUE;
            for (int i = 0; i < coordX.length; i++) {
                float d = (float) Math.sqrt(Math.pow(coordX[i] - fx, 2) + Math.pow(coordY[i] - fy, 2));
                if (d < distance) {
                    distance = d;
                    pos = i;
                }
            }
            return distance <= 0.01 ? pos : -1;
        }

        int pointPos;

        @Override
        public void mousePressed(MouseEvent e) {
            pointPos = findPoint(e.getX(), e.getY());
        }

        @Override
        public void mouseReleased(MouseEvent e) {
        }

        @Override
        public void mouseDragged(MouseEvent e) {
            if (pointPos < 0) return;
            int w = this.getWidth();
            int h = this.getHeight();
            int min = Math.min(w, h);
            int off_W = w / 2;
            int off_H = h / 2;
            coordX[pointPos] = (float) (e.getX() - off_W) / min;
            coordY[pointPos] = (float) (off_H - e.getY()) / min;
            repaint();
        }

        @Override
        public void mouseMoved(MouseEvent e) {
        }
    }

    static Poligono poligono;

    @SuppressWarnings("serial")
    public static void main(String... args) {
        final XStream xStream = new XStream(new DomDriver());
        xStream.registerConverter(new PoligonoConverter());
        xStream.alias(S.Poligono, Poligono.class);
        poligono = (Poligono) xStream.fromXML("<Poligono nLados=\"14\">" + "  <Punto2F x=\"-0.44\" y=\"-0.13\"/>" + "  <Punto2F x=\"-0.47\" y=\"-0.08\"/>" + "  <Punto2F x=\"-0.50\" y=\"0.13\"/>" + "  <Punto2F x=\"-0.41\" y=\"0.13\"/>" + "  <Punto2F x=\"-0.2\" y=\"0.01\"/>" + "  <Punto2F x=\"0.02\" y=\"0.01\"/>" + "  <Punto2F x=\"0.06\" y=\"0.03\"/>" + "  <Punto2F x=\"0.22\" y=\"0.03\"/>" + "  <Punto2F x=\"0.29\" y=\"-0.02\"/>" + "  <Punto2F x=\"0.52\" y=\"-0.055\"/>" + "  <Punto2F x=\"0.17\" y=\"-0.1\"/>" + "  <Punto2F x=\"0.16\" y=\"-0.12\"/>" + "  <Punto2F x=\"-0.06\" y=\"-0.11\"/>" + "  <Punto2F x=\"-0.15\" y=\"-0.13\"/>" + "</Poligono>");
        final Matrix4d mt = new Matrix4d(0.0, 0.0, 1.0, 0.0, 0.0, 1.0, 0.0, 0.0, -1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0);
        final JFrame frame = new JFrame("Generador de Siluetas");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        JSplitPane contentPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        contentPane.setResizeWeight(1);
        frame.setContentPane(contentPane);
        final PantallaGrafica area = new PantallaGrafica();
        area.setPoligono(poligono);
        JScrollPane scrollPane;
        contentPane.setLeftComponent(new JScrollPane(area, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED));
        JPanel rightComponent = new JPanel();
        rightComponent.setLayout(new BorderLayout());
        final JTextPane tLog = new JTextPane(new DefaultStyledDocument());
        final PrintStream log1 = Consola.getLog(tLog);
        scrollPane = new JScrollPane(tLog, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        rightComponent.add(scrollPane, BorderLayout.CENTER);
        JPanel butonsPanel = new JPanel();
        butonsPanel.add(new JButton(new AbstractAction(" <--- From XML") {

            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    poligono = (Poligono) xStream.fromXML(tLog.getText());
                    area.setPoligono(poligono);
                } catch (Exception ex) {
                }
            }
        }));
        butonsPanel.add(new JButton(new AbstractAction(" ---> To XML") {

            @Override
            public void actionPerformed(ActionEvent e) {
                tLog.setText("");
                log1.println(xStream.toXML(poligono));
            }
        }));
        rightComponent.add(butonsPanel, BorderLayout.SOUTH);
        contentPane.setRightComponent(rightComponent);
        JMenuBar menubar = new JMenuBar();
        final JFileChooser chooser = new JFileChooser();
        final File workDir = new File("resources/obj3d/");
        chooser.addChoosableFileFilter(new FileNameExtensionFilter("Wavefront .obj", "obj"));
        chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        JMenu m = new JMenu("Archivo");
        m.add(new AbstractAction("Abrir") {

            @Override
            public void actionPerformed(ActionEvent e) {
                chooser.setCurrentDirectory(workDir);
                chooser.setSelectedFile(new File(""));
                int returnVal = chooser.showOpenDialog(frame);
                if (returnVal == JFileChooser.APPROVE_OPTION) {
                    File f = chooser.getSelectedFile();
                    if (f.getName().toLowerCase().endsWith("obj")) {
                        try {
                            ObjLoader loader = new ObjLoader(new FileReader(f), true, mt);
                            area.setModel(loader.vertList, loader.lines);
                        } catch (FileNotFoundException ignorada) {
                        }
                    }
                }
            }
        });
        m.addSeparator();
        m.add(new AbstractAction("Salir") {

            @Override
            public void actionPerformed(ActionEvent e) {
                frame.dispatchEvent(new WindowEvent(frame, WindowEvent.WINDOW_CLOSING));
            }
        });
        menubar.add(m);
        frame.setJMenuBar(menubar);
        frame.setSize(800, 450);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
        contentPane.setDividerLocation(500);
    }
}
