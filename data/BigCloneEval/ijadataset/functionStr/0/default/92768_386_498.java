public class Test {    public int saveBuildingInstructionstoPDF(boolean mustWrite, String testNum) {
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
}