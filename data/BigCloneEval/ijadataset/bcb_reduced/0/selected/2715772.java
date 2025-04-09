package pl.kane.autokomp;

import java.awt.AWTException;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * @author KanE
 *
 */
public class Template {

    private int[][] data;

    private int width;

    private int height;

    public Template(int[][] data, int width, int height) {
        super();
        this.data = data;
        this.width = width;
        this.height = height;
    }

    public Template(BufferedImage image) {
        super();
        this.width = image.getWidth();
        this.height = image.getHeight();
        this.data = new int[this.width][this.height];
        for (int i = 0; i < width; i++) for (int j = 0; j < height; j++) data[i][j] = image.getRGB(i, j);
    }

    public Template() {
        super();
        this.data = null;
        this.width = 0;
        this.height = 0;
    }

    public Template(Template template) {
        super();
        this.data = template.data;
        this.width = template.width;
        this.height = template.height;
    }

    public boolean equals(BufferedImage img) {
        if (img.getWidth() == width && img.getHeight() == height) {
            for (int i = 0; i < width; i++) for (int j = 0; j < height; j++) if (img.getRGB(i, j) != data[i][j]) return false;
        } else return false;
        return true;
    }

    public static Template loadTemplate(String path) {
        String record;
        FileReader fr;
        int[][] template = null;
        int dimX = 0;
        int dimY = 0;
        try {
            fr = new FileReader(path);
            BufferedReader br = new BufferedReader(fr);
            record = br.readLine();
            if (record.indexOf(' ') != -1) {
                dimX = Integer.parseInt(record.substring(0, record.indexOf(' ')));
                record = record.substring(record.indexOf(' ') + 1);
            }
            if (record != "") {
                dimY = Integer.parseInt(record);
            }
            template = new int[dimX][dimY];
            for (int i = 0; i < dimY; i++) {
                record = br.readLine();
                for (int j = 0; j < dimX; j++) {
                    if (record.indexOf(' ') != -1) {
                        template[j][i] = Integer.parseInt(record.substring(0, record.indexOf(' ')));
                        record = record.substring(record.indexOf(' ') + 1);
                    }
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new Template(template, dimX, dimY);
    }

    public static Template getTemplate(BufferedImage image) {
        Template template = new Template();
        template.height = image.getHeight();
        template.width = image.getWidth();
        template.data = new int[image.getHeight()][image.getWidth()];
        for (int i = 0; i < image.getHeight(); i++) {
            for (int j = 0; j < image.getWidth(); j++) {
                template.data[j][i] = image.getRGB(j, i);
            }
        }
        return template;
    }

    public boolean saveTemplate(String path) {
        File file = new File(path);
        try {
            PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(file)));
            out.println(this.width + " " + this.height);
            for (int i = 0; i < this.height; i++) {
                for (int j = 0; j < this.width; j++) {
                    out.print(this.data[j][i] + " ");
                }
                out.println();
            }
            out.flush();
            out.close();
        } catch (IOException e) {
            return false;
        }
        return true;
    }

    public static Dimension findTemplateFullScreen(Template template) {
        Dimension dim = null;
        dim = Toolkit.getDefaultToolkit().getScreenSize();
        BufferedImage img = null;
        Robot robot;
        try {
            robot = new Robot();
            img = robot.createScreenCapture(new Rectangle(1, 1, (int) dim.getWidth(), (int) dim.getHeight()));
            for (int x = 0; x < (int) dim.getWidth() - template.getWidth(); x++) for (int y = 0; y < (int) dim.getHeight() - template.getHeight(); y++) {
            }
        } catch (AWTException e) {
            e.printStackTrace();
        }
        return dim;
    }

    public int[][] getData() {
        return data;
    }

    public void setData(int[][] data) {
        this.data = data;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }
}
