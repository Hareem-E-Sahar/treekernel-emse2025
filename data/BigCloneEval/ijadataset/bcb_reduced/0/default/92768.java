import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.Vector;
import javax.swing.JOptionPane;
import com.lowagie.text.Chunk;
import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;

public class outputToFile {

    LConstructCellular program;

    Vector<LBrick> currentLayerBricks = new Vector<LBrick>();

    int totalNumLegoBricksUsed[];

    /**
	 * Constructer. This creates a instance of an output writer.
	 * It is used to same layer layouts and to generate pdf files.
	 * 
	 * @param prog Reference to the main program.
	 */
    public outputToFile(LConstructCellular prog) {
        program = prog;
    }

    /**
	 * The function writes the layer layout to file.
	 * It is writen to a normal text file with a .ll extension.
	 * The first line has the layer number
	 * The second has the cost of layout
	 * The file has the grid size on the third line ( rows, columns)
	 * Each line contains the block type as int value,colour, row and column value where the left corner must be placed.
	 * Note that this function does not correspond directly with that of previous modelReconstructer method.
	 * Nodes can be compound. Each brick must be drawn or else wholes will appear.
	 * 
	 * @param layer  			The layer number to be written to file.
	 * @param bricks 			List of bricks in the layer.
	 * @param layersDirectory 	The output directory in which file must be saved. 
	 * @param layerCost 		The cost of the layer.
	 * @param gridWidth 		The grid width of the legolised model.
	 * @param gridHeight 		The grid height of the legolised model.
	 * @return void
	 */
    public void saveLayerBricksToFile(int layer, Vector<LBrick> bricks, int layerCost) {
        int i;
        int k;
        LBrick temp;
        FileWriter fileW;
        try {
            if (program.fileExtension.equals(".sl2")) {
                fileW = new FileWriter(program.directory + "layerLayout" + layer + ".ll2");
            } else {
                fileW = new FileWriter(program.directory + "layerLayout" + layer + ".ll");
            }
            BufferedWriter file = new BufferedWriter(fileW);
            file.write("" + layer);
            file.newLine();
            file.write("" + Math.round(layerCost));
            file.newLine();
            file.write("" + program.gridWidth);
            file.write(" ");
            file.write("" + program.gridHeight);
            file.newLine();
            if (bricks == null) {
                return;
            }
            LBrick brick;
            i = 0;
            int size = bricks.size();
            while (i < size) {
                brick = bricks.get(i);
                if (program.fileExtension.equals(".sl2")) {
                    file.write("" + brick.type + " " + brick.colour);
                } else {
                    file.write("" + brick.type);
                }
                file.write(" " + brick.xPos + " " + brick.yPos);
                file.newLine();
                i++;
            }
            file.close();
        } catch (Exception e) {
            System.out.println("An error occured while trying to save layout " + layer);
            System.out.println(e.getMessage());
            System.exit(0);
        }
    }

    /**
	 * This function draws a grid similar to graph paper.
	 * @param g - graphics2D object to draw on
	 * @param width - the width of a unit brick (sqaure)
	 * @return void
	 */
    public void drawGrid(Graphics2D g, int width) {
        try {
            int colLength = width * program.gridWidth;
            int rowLength = width * program.gridHeight;
            int i = program.gridHeight;
            int x = 100;
            int y = 150;
            while (i >= 0) {
                g.drawLine(x, y, x + colLength, y);
                y += width;
                i--;
            }
            x = 100;
            y = 150;
            i = program.gridWidth;
            while (i >= 0) {
                g.drawLine(x, y, x, y + rowLength);
                x += width;
                i--;
            }
        } catch (Exception de) {
            System.err.println(de.getMessage());
        }
    }

    Color getBrickColor(int colType) {
        switch(colType) {
            case -1:
                return Color.cyan;
            case 1:
                return Color.WHITE;
            case 2:
                return Color.DARK_GRAY;
            case 3:
                return Color.red;
            case 4:
                return Color.BLUE;
            case 5:
                return Color.YELLOW;
            case 6:
                return Color.GREEN;
            case 7:
                return Color.ORANGE;
            case 8:
                return Color.GRAY;
        }
        return Color.cyan;
    }

    /**
     *  This function draws each legoblock filled.
     *  @param g - graphics2D object to draw on
     *  @param gridX - x position to start drawing (can change to shift)
     *  @param gridY - y position to start drawing (can change to shift)
     *  @param bricks - list of all bricks to draw
     *  @param width  - the width of a single unit brick
	 *	@return void
     */
    public void drawBlocks(Graphics g, int gridX, int gridY, Vector<LBrick> bricks, int width) {
        int colLength = width * program.gridWidth;
        int rowLength = width * program.gridHeight;
        int i = 0;
        int size = bricks.size();
        LBrick brick;
        while (i < size) {
            brick = bricks.get(i);
            if (program.fileExtension.equals(".sl2")) {
                g.setColor(getBrickColor(brick.colour));
            }
            switch(brick.type) {
                case 0:
                    g.fillRect(gridX + brick.xPos * width, gridY + brick.yPos * width, width, width);
                    break;
                case 1:
                    g.fillRect(gridX + brick.xPos * width, gridY + brick.yPos * width, width * 2, width);
                    break;
                case 2:
                    g.fillRect(gridX + brick.xPos * width, gridY + brick.yPos * width, width, width * 2);
                    break;
                case 3:
                    g.fillRect(gridX + brick.xPos * width, gridY + brick.yPos * width, width * 3, width);
                    break;
                case 4:
                    g.fillRect(gridX + brick.xPos * width, gridY + brick.yPos * width, width, width * 3);
                    break;
                case 5:
                    g.fillRect(gridX + brick.xPos * width, gridY + brick.yPos * width, width * 4, width);
                    break;
                case 6:
                    g.fillRect(gridX + brick.xPos * width, gridY + brick.yPos * width, width, width * 4);
                    break;
                case 7:
                    g.fillRect(gridX + brick.xPos * width, gridY + brick.yPos * width, width * 6, width);
                    break;
                case 8:
                    g.fillRect(gridX + brick.xPos * width, gridY + brick.yPos * width, width, width * 6);
                    break;
                case 9:
                    g.fillRect(gridX + brick.xPos * width, gridY + brick.yPos * width, width * 8, width);
                    break;
                case 10:
                    g.fillRect(gridX + brick.xPos * width, gridY + brick.yPos * width, width, width * 8);
                    break;
                case 11:
                    g.fillRect(gridX + brick.xPos * width, gridY + brick.yPos * width, width, width * 2);
                    g.fillRect(gridX + (brick.xPos + 1) * width, gridY + (brick.yPos + 1) * width, width, width);
                    break;
                case 12:
                    g.fillRect(gridX + brick.xPos * width, gridY + brick.yPos * width, width * 2, width);
                    g.fillRect(gridX + (brick.xPos + 1) * width, gridY + (brick.yPos + 1) * width, width, width);
                    break;
                case 13:
                    g.fillRect(gridX + (brick.xPos + 1) * width, gridY + brick.yPos * width, width, width * 2);
                    g.fillRect(gridX + (brick.xPos) * width, gridY + (brick.yPos + 1) * width, width, width);
                    break;
                case 14:
                    g.fillRect(gridX + brick.xPos * width, gridY + brick.yPos * width, width * 2, width);
                    g.fillRect(gridX + (brick.xPos) * width, gridY + (brick.yPos + 1) * width, width, width);
                    break;
                case 15:
                    g.fillRect(gridX + brick.xPos * width, gridY + brick.yPos * width, width * 2, width * 2);
                    break;
                case 16:
                    g.fillRect(gridX + brick.xPos * width, gridY + brick.yPos * width, width * 3, width * 2);
                    break;
                case 17:
                    g.fillRect(gridX + brick.xPos * width, gridY + brick.yPos * width, width * 2, width * 3);
                    break;
                case 18:
                    g.fillRect(gridX + brick.xPos * width, gridY + brick.yPos * width, width * 4, width * 2);
                    break;
                case 19:
                    g.fillRect(gridX + brick.xPos * width, gridY + brick.yPos * width, width * 2, width * 4);
                    break;
                case 20:
                    g.fillRect(gridX + brick.xPos * width, gridY + brick.yPos * width, width * 6, width * 2);
                    break;
                case 21:
                    g.fillRect(gridX + brick.xPos * width, gridY + brick.yPos * width, width * 2, width * 6);
                    break;
                case 22:
                    g.fillRect(gridX + brick.xPos * width, gridY + brick.yPos * width, width * 8, width * 2);
                    break;
                case 23:
                    g.fillRect(gridX + brick.xPos * width, gridY + brick.yPos * width, width * 2, width * 8);
                    break;
                default:
                    System.out.println("UNKNOWN BLOCK");
            }
            i++;
        }
    }

    /**
     * This function draws only an outline of a rectangle. I used it to draw a border around the leog bricks.
     *  @param g - graphics2D object to draw on
     *  @param gridX - x position to start drawing (can change to shift)
     *  @param gridY - y position to start drawing (can change to shift)
     *  @param bricks - list of all bricks to draw
     *  @param width  - the width of a single unit brick
	 *	@return void
     */
    public void drawBlocksOutline(Graphics g, int gridX, int gridY, Vector<LBrick> bricks, int width) {
        int i = 0;
        int size = bricks.size();
        LBrick brick;
        while (i < size) {
            brick = bricks.get(i);
            switch(brick.type) {
                case 0:
                    g.drawRect(gridX + brick.xPos * width, gridY + brick.yPos * width, width, width);
                    break;
                case 1:
                    g.drawRect(gridX + brick.xPos * width, gridY + brick.yPos * width, width * 2, width);
                    break;
                case 2:
                    g.drawRect(gridX + brick.xPos * width, gridY + brick.yPos * width, width, width * 2);
                    break;
                case 3:
                    g.drawRect(gridX + brick.xPos * width, gridY + brick.yPos * width, width * 3, width);
                    break;
                case 4:
                    g.drawRect(gridX + brick.xPos * width, gridY + brick.yPos * width, width, width * 3);
                    break;
                case 5:
                    g.drawRect(gridX + brick.xPos * width, gridY + brick.yPos * width, width * 4, width);
                    break;
                case 6:
                    g.drawRect(gridX + brick.xPos * width, gridY + brick.yPos * width, width, width * 4);
                    break;
                case 7:
                    g.drawRect(gridX + brick.xPos * width, gridY + brick.yPos * width, width * 6, width);
                    break;
                case 8:
                    g.drawRect(gridX + brick.xPos * width, gridY + brick.yPos * width, width, width * 6);
                    break;
                case 9:
                    g.drawRect(gridX + brick.xPos * width, gridY + brick.yPos * width, width * 8, width);
                    break;
                case 10:
                    g.drawRect(gridX + brick.xPos * width, gridY + brick.yPos * width, width, width * 8);
                    break;
                case 11:
                    g.drawLine(gridX + brick.xPos * width, gridY + brick.yPos * width, gridX + brick.xPos * width, gridY + (brick.yPos + 2) * width);
                    g.drawLine(gridX + brick.xPos * width, gridY + (brick.yPos + 2) * width, gridX + (brick.xPos + 2) * width, gridY + (brick.yPos + 2) * width);
                    g.drawLine(gridX + (brick.xPos + 2) * width, gridY + (brick.yPos + 2) * width, gridX + (brick.xPos + 2) * width, gridY + (brick.yPos + 1) * width);
                    g.drawLine(gridX + (brick.xPos + 2) * width, gridY + (brick.yPos + 1) * width, gridX + (brick.xPos + 1) * width, gridY + (brick.yPos + 1) * width);
                    g.drawLine(gridX + (brick.xPos + 1) * width, gridY + (brick.yPos + 1) * width, gridX + (brick.xPos + 1) * width, gridY + (brick.yPos) * width);
                    g.drawLine(gridX + (brick.xPos + 1) * width, gridY + brick.yPos * width, gridX + brick.xPos * width, gridY + brick.yPos * width);
                    break;
                case 12:
                    g.drawLine(gridX + brick.xPos * width, gridY + brick.yPos * width, gridX + brick.xPos * width, gridY + (brick.yPos + 1) * width);
                    g.drawLine(gridX + brick.xPos * width, gridY + (brick.yPos + 1) * width, gridX + (brick.xPos + 1) * width, gridY + (brick.yPos + 1) * width);
                    g.drawLine(gridX + (brick.xPos + 1) * width, gridY + (brick.yPos + 1) * width, gridX + (brick.xPos + 1) * width, gridY + (brick.yPos + 2) * width);
                    g.drawLine(gridX + (brick.xPos + 1) * width, gridY + (brick.yPos + 2) * width, gridX + (brick.xPos + 2) * width, gridY + (brick.yPos + 2) * width);
                    g.drawLine(gridX + (brick.xPos + 2) * width, gridY + (brick.yPos + 2) * width, gridX + (brick.xPos + 2) * width, gridY + (brick.yPos) * width);
                    g.drawLine(gridX + (brick.xPos + 2) * width, gridY + brick.yPos * width, gridX + brick.xPos * width, gridY + brick.yPos * width);
                    break;
                case 13:
                    g.drawLine(gridX + brick.xPos * width, gridY + (brick.yPos + 2) * width, gridX + (brick.xPos + 2) * width, gridY + (brick.yPos + 2) * width);
                    g.drawLine(gridX + (brick.xPos + 2) * width, gridY + (brick.yPos + 2) * width, gridX + (brick.xPos + 2) * width, gridY + (brick.yPos) * width);
                    g.drawLine(gridX + (brick.xPos + 2) * width, gridY + brick.yPos * width, gridX + (brick.xPos + 1) * width, gridY + brick.yPos * width);
                    g.drawLine(gridX + (brick.xPos + 1) * width, gridY + brick.yPos * width, gridX + (brick.xPos + 1) * width, gridY + (brick.yPos + 1) * width);
                    g.drawLine(gridX + (brick.xPos + 1) * width, gridY + (brick.yPos + 1) * width, gridX + brick.xPos * width, gridY + (brick.yPos + 1) * width);
                    g.drawLine(gridX + brick.xPos * width, gridY + (brick.yPos + 1) * width, gridX + brick.xPos * width, gridY + (brick.yPos + 2) * width);
                    break;
                case 14:
                    g.drawLine(gridX + brick.xPos * width, gridY + brick.yPos * width, gridX + brick.xPos * width, gridY + (brick.yPos + 2) * width);
                    g.drawLine(gridX + brick.xPos * width, gridY + (brick.yPos + 2) * width, gridX + (brick.xPos + 1) * width, gridY + (brick.yPos + 2) * width);
                    g.drawLine(gridX + (brick.xPos + 1) * width, gridY + (brick.yPos + 2) * width, gridX + (brick.xPos + 1) * width, gridY + (brick.yPos + 1) * width);
                    g.drawLine(gridX + (brick.xPos + 1) * width, gridY + (brick.yPos + 1) * width, gridX + (brick.xPos + 2) * width, gridY + (brick.yPos + 1) * width);
                    g.drawLine(gridX + (brick.xPos + 2) * width, gridY + (brick.yPos + 1) * width, gridX + (brick.xPos + 2) * width, gridY + (brick.yPos) * width);
                    g.drawLine(gridX + (brick.xPos + 2) * width, gridY + brick.yPos * width, gridX + brick.xPos * width, gridY + brick.yPos * width);
                    break;
                case 15:
                    g.drawRect(gridX + brick.xPos * width, gridY + brick.yPos * width, width * 2, width * 2);
                    break;
                case 16:
                    g.drawRect(gridX + brick.xPos * width, gridY + brick.yPos * width, width * 3, width * 2);
                    break;
                case 17:
                    g.drawRect(gridX + brick.xPos * width, gridY + brick.yPos * width, width * 2, width * 3);
                    break;
                case 18:
                    g.drawRect(gridX + brick.xPos * width, gridY + brick.yPos * width, width * 4, width * 2);
                    break;
                case 19:
                    g.drawRect(gridX + brick.xPos * width, gridY + brick.yPos * width, width * 2, width * 4);
                    break;
                case 20:
                    g.drawRect(gridX + brick.xPos * width, gridY + brick.yPos * width, width * 6, width * 2);
                    break;
                case 21:
                    g.drawRect(gridX + brick.xPos * width, gridY + brick.yPos * width, width * 2, width * 6);
                    break;
                case 22:
                    g.drawRect(gridX + brick.xPos * width, gridY + brick.yPos * width, width * 8, width * 2);
                    break;
                case 23:
                    g.drawRect(gridX + brick.xPos * width, gridY + brick.yPos * width, width * 2, width * 8);
                    break;
                default:
                    System.out.println("UNKNOWN BLOCK");
            }
            i++;
        }
    }

    /**
	 * This function creates the LEGO building instructions from the 
	 * layout files saved during model creation.
	 * It also adds a list of all the bricks that was used to build it.
	 * @param whether the instructions must be generated. Used in debugging.
	 * @return void
	 */
    public int saveBuildingInstructionstoPDF(boolean mustWrite, String testNum) {
        int totalbricks = 0;
        Paragraph p;
        Chunk chunk;
        int width;
        int i;
        File fileToDelete;
        if (mustWrite) {
            totalNumLegoBricksUsed = new int[LBricks.numLegoBricks];
            try {
                Document document = new Document(PageSize.A0, 50, 50, 50, 50);
                PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(program.directory + "BuildingInstructions" + program.startIndex + "_" + program.stopIndex + testNum + ".pdf"));
                writer.setPageEvent(new pdfListener());
                document.open();
                PdfContentByte cb = writer.getDirectContent();
                int canvasWidth = (int) PageSize.A0.width() - 200;
                int canvasHeight = (int) PageSize.A0.height() - 200;
                if (program.gridWidth != program.gridHeight) {
                    if (program.gridWidth > program.gridHeight) {
                        width = (int) Math.floor((double) (canvasWidth - 100) / (double) program.gridWidth);
                    } else {
                        width = (int) Math.floor((double) (canvasWidth - 100) / (double) program.gridHeight);
                    }
                } else {
                    width = (int) Math.floor((double) (canvasWidth - 100) / (double) program.gridWidth);
                }
                for (i = program.startIndex; i <= program.stopIndex; i++) {
                    p = new Paragraph();
                    chunk = new Chunk("Layout Layer " + i + " :", FontFactory.getFont(FontFactory.TIMES_ROMAN, 14, Font.BOLD, Color.black));
                    chunk.setUnderline(1.5f, -2.5f);
                    p.add(chunk);
                    document.add(p);
                    Graphics2D g = cb.createGraphics(canvasWidth, canvasHeight);
                    g.setColor(Color.LIGHT_GRAY);
                    drawGrid(g, width);
                    if (!program.fileExtension.equals(".sl2")) {
                        if (i > program.startIndex) {
                            g.setColor(Color.gray);
                            loadLayerBricks(i - 1, false);
                            drawBlocks(g, 100, 150, currentLayerBricks, width);
                            g.setColor(Color.DARK_GRAY);
                            drawBlocksOutline(g, 100, 150, currentLayerBricks, width);
                        }
                    }
                    if (!program.fileExtension.equals(".sl2")) {
                        g.setColor(Color.LIGHT_GRAY);
                    }
                    loadLayerBricks(i, true);
                    drawBlocks(g, 100, 150, currentLayerBricks, width);
                    g.setColor(Color.BLACK);
                    drawBlocksOutline(g, 100, 150, currentLayerBricks, width);
                    g.setColor(Color.BLACK);
                    g.dispose();
                    cb.saveState();
                    document.newPage();
                }
                p = new Paragraph();
                chunk = new Chunk("LEGO bricks required to build sculpture:", FontFactory.getFont(FontFactory.TIMES_ROMAN, 14, Font.BOLD, Color.black));
                chunk.setUnderline(1.5f, -2.5f);
                p.add(chunk);
                document.add(p);
                float[] widths = { 0.60f, 0.40f };
                PdfPTable table = new PdfPTable(widths);
                table.setTotalWidth(250);
                table.setLockedWidth(true);
                table.setSpacingBefore(25f);
                table.getDefaultCell().setBackgroundColor(new Color(0.59f, 0.6f, 0.97f));
                table.getDefaultCell().setHorizontalAlignment(Element.ALIGN_LEFT);
                table.setHorizontalAlignment(Element.ALIGN_LEFT);
                PdfPCell cell = new PdfPCell(new Paragraph("Type", FontFactory.getFont(FontFactory.COURIER, 12, Font.BOLD)));
                cell.setBackgroundColor(new Color(0.34f, 0.41f, 0.98f));
                table.addCell(cell);
                cell = new PdfPCell(new Paragraph("Amount required", FontFactory.getFont(FontFactory.COURIER, 12, Font.BOLD)));
                cell.setBackgroundColor(new Color(0.34f, 0.41f, 0.98f));
                table.addCell(cell);
                totalbricks += totalNumLegoBricksUsed[0];
                table.addCell("" + LBricks.blockNames[0]);
                table.addCell("" + totalNumLegoBricksUsed[0]);
                for (i = 1; i < 10; i = i + 2) {
                    totalbricks += totalNumLegoBricksUsed[i] + totalNumLegoBricksUsed[i + 1];
                    table.addCell("" + LBricks.blockNames[i]);
                    table.addCell("" + (totalNumLegoBricksUsed[i] + totalNumLegoBricksUsed[i + 1]));
                }
                for (i = 11; i < 16; i++) {
                    totalbricks += totalNumLegoBricksUsed[i];
                    table.addCell("" + LBricks.blockNames[i]);
                    table.addCell("" + totalNumLegoBricksUsed[i]);
                }
                for (i = 16; i < LBricks.numLegoBricks - 1; i = i + 2) {
                    totalbricks += totalNumLegoBricksUsed[i] + totalNumLegoBricksUsed[i + 1];
                    table.addCell("" + LBricks.blockNames[i]);
                    table.addCell("" + (totalNumLegoBricksUsed[i] + totalNumLegoBricksUsed[i + 1]));
                }
                cell = new PdfPCell(new Paragraph("Total", FontFactory.getFont(FontFactory.COURIER, 12, Font.BOLD)));
                cell.setBackgroundColor(Color.YELLOW);
                table.addCell(cell);
                cell = new PdfPCell(new Paragraph("" + totalbricks, FontFactory.getFont(FontFactory.COURIER, 12, Font.BOLD)));
                cell.setBackgroundColor(Color.YELLOW);
                table.addCell(cell);
                document.add(table);
                document.close();
            } catch (Exception e) {
                System.out.println("An error occured while trying to save the list of LEGO bricks used.");
                System.out.println(e.getMessage());
                System.exit(0);
            }
        } else {
            for (i = 0; i < LBricks.numLegoBricks; i++) {
                totalbricks += totalNumLegoBricksUsed[i];
            }
        }
        return totalbricks;
    }

    public void onStartPage(PdfWriter writer, Document document) {
        if (writer.getPageNumber() < 3) {
            PdfContentByte cb = writer.getDirectContentUnder();
            cb.saveState();
            cb.setColorFill(Color.pink);
            cb.beginText();
            cb.showTextAligned(Element.ALIGN_CENTER, "My Watermark Under " + writer.getPageNumber(), document.getPageSize().width() / 2, document.getPageSize().height() / 2, 45);
            cb.endText();
            cb.restoreState();
        }
    }

    /**
	 * This function loads the bricks from file and stores them in currentLayerBricks.
	 * The funtion is used for pdf output to load the wanted layer's bricks.
	 * 
	 * NOTE : THIS DOES NOT YET HAVE COLOR BRICKS.
	 * 
	 * @param layer - The layer to load from file. According to startIndex.
	 * @param countBrick - Whether to add the bricks to the total amount of bricks in sculpture.
	 * @return boolean - Whether succesful in loading bricks
	 */
    public boolean loadLayerBricks(int layer, boolean countBrick) {
        FileReader reader;
        BufferedReader file;
        Vector<LBrick> layerBricks;
        String line;
        String[] values;
        int lineNum = 0;
        LBrick newBrick;
        try {
            if (program.fileExtension.equals(".sl2")) {
                reader = new FileReader(program.directory + "\\layerLayout" + layer + ".ll2");
            } else {
                reader = new FileReader(program.directory + "\\layerLayout" + layer + ".ll");
            }
            file = new BufferedReader(reader);
            currentLayerBricks = new Vector<LBrick>();
            layerBricks = currentLayerBricks;
            line = file.readLine();
            lineNum++;
            line = file.readLine();
            lineNum++;
            line = file.readLine();
            lineNum++;
            if (program.gridWidth < 0) {
                values = line.split(" ");
                program.gridWidth = Integer.parseInt(values[0]);
                program.gridWidth = Integer.parseInt(values[1]);
            }
            while ((line = file.readLine()) != null) {
                values = line.split(" ");
                if (program.fileExtension.equals(".sl2")) {
                    newBrick = new LBrick((byte) Integer.parseInt(values[0]), Integer.parseInt(values[2]), Integer.parseInt(values[3]), layer, Integer.parseInt(values[1]), program.maxTimestepsBeforeSplitTry);
                } else {
                    newBrick = new LBrick((byte) Integer.parseInt(values[0]), Integer.parseInt(values[1]), Integer.parseInt(values[2]), layer, -1, program.maxTimestepsBeforeSplitTry);
                }
                if (countBrick) {
                    totalNumLegoBricksUsed[newBrick.type]++;
                }
                layerBricks.add(newBrick);
                lineNum++;
            }
            file.close();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "An error occurred while reading layer : " + layer + " on line " + lineNum, "Layout file error occured", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        return true;
    }
}
