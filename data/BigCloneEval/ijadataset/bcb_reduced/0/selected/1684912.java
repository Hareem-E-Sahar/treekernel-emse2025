package hu.colbud.qcg.cads;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class DrawPlanePNG {

    private static final long serialVersionUID = 648004407047497072L;

    int planeSize = 1000;

    int grid[][];

    int frame_nr = 0;

    ArrayList<String> filenames = new ArrayList<String>();

    NumberFormat nf = NumberFormat.getInstance();

    public void drawPlanePNG() {
        final BufferedImage img = new BufferedImage(planeSize, planeSize, BufferedImage.TYPE_INT_RGB);
        final Graphics2D gr = img.createGraphics();
        paintComponent(gr);
        nf.setMinimumIntegerDigits(5);
        nf.setMaximumIntegerDigits(5);
        nf.setGroupingUsed(false);
        try {
            final String filename = "visualization" + nf.format(frame_nr) + ".png";
            final File f = new File(filename);
            final FileOutputStream out = new FileOutputStream(f);
            javax.imageio.ImageIO.write(img, "png", out);
            out.close();
            System.err.println("output saved as: " + f.getAbsolutePath());
            filenames.add(filename);
            frame_nr++;
        } catch (final Exception e) {
            e.printStackTrace();
        }
    }

    public void paintComponent(final Graphics2D g) {
        final int x = planeSize / grid.length;
        final int y = planeSize / grid.length;
        for (int i = 0; i < grid.length; i++) {
            for (int j = 0; j < grid.length; j++) {
                if (grid[i][j] == -1) {
                    g.setColor(Color.white);
                } else if (grid[i][j] == 0) {
                    g.setColor(Color.black);
                } else if (grid[i][j] == 1) {
                    g.setColor(Color.green);
                } else if (grid[i][j] == 2) {
                    g.setColor(Color.red);
                } else if (grid[i][j] == 3) {
                    g.setColor(Color.blue);
                } else if (grid[i][j] == 4) {
                    g.setColor(Color.white);
                } else if (grid[i][j] == 5) {
                    g.setColor(Color.gray);
                } else if (grid[i][j] == 6) {
                    g.setColor(Color.lightGray);
                } else if (grid[i][j] == 7) {
                    g.setColor(Color.darkGray);
                }
                g.fillRect(x * i, y * j, x, y);
            }
        }
    }

    public void setGrid(final int[][] g) {
        grid = new int[planeSize][planeSize];
        grid = g.clone();
    }

    public int getPlaneSize() {
        return planeSize;
    }

    public void setPlaneSize(final int ps) {
        this.planeSize = ps;
    }

    protected void zipVisualizationFiles(final String outputfile) {
        final byte[] buf = new byte[100000];
        try {
            final String outFilename = outputfile;
            final ZipOutputStream out = new ZipOutputStream(new FileOutputStream(outFilename));
            for (int i = 0; i < filenames.size(); i++) {
                final FileInputStream in = new FileInputStream(filenames.get(i));
                out.putNextEntry(new ZipEntry(filenames.get(i)));
                int len;
                while ((len = in.read(buf)) > 0) {
                    out.write(buf, 0, len);
                }
                out.closeEntry();
                in.close();
            }
            out.close();
        } catch (final IOException e) {
        }
    }
}
