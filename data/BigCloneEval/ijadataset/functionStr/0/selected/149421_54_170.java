public class Test {    public void write(Records records, String outputDirectory, String filename, int threadCount, boolean outputDivaGrid, boolean outputASC) throws IOException {
        if (filename == null) {
            filename = "_species_density_av_" + gridSize + "x" + gridSize + "_" + String.valueOf(resolution).replace(".", "");
        }
        BufferedWriter bw = null;
        if (outputASC) {
            bw = new BufferedWriter(new FileWriter(outputDirectory + filename + ".asc"));
        }
        BufferedOutputStream bos = null;
        if (outputDivaGrid) {
            bos = new BufferedOutputStream(new FileOutputStream(outputDirectory + filename + ".gri"));
        }
        byte[] bytes = null;
        ByteBuffer bb = null;
        if (outputDivaGrid) {
            bytes = new byte[4 * width];
            bb = ByteBuffer.wrap(bytes);
            bb.order(ByteOrder.LITTLE_ENDIAN);
            bb.mark();
        }
        bw.append("ncols " + width + "\n" + "nrows " + height + "\n" + "xllcorner " + bbox[0] + "\n" + "yllcorner " + bbox[1] + "\n" + "cellsize " + resolution + "\n" + "NODATA_value -9999\n");
        int uniqueSpeciesCount = records.getSpeciesSize();
        BitSet[][] bsRows = new BitSet[gridSize][];
        double max = 0;
        boolean worldwrap = (bbox[2] - bbox[0]) == 360;
        float[] values = new float[width];
        int partCount = threadCount * 5;
        int partSize = (int) Math.ceil(width / (double) partCount);
        GetValuesSpeciesThread[] getValues = new GetValuesSpeciesThread[threadCount];
        BitSet[] cells = new BitSet[threadCount];
        LinkedBlockingQueue<Integer> lbqGetValues = new LinkedBlockingQueue<Integer>();
        int[] rowStarts = records.sortedRowStarts(bbox[1], height, resolution);
        System.out.println("thread count = " + threadCount);
        System.out.println("Memory usage (total/used/free):" + (Runtime.getRuntime().totalMemory() / 1024 / 1024) + "MB / " + (Runtime.getRuntime().totalMemory() / 1024 / 1024 - Runtime.getRuntime().freeMemory() / 1024 / 1024) + "MB / " + (Runtime.getRuntime().freeMemory() / 1024 / 1024) + "MB");
        for (int row = 0; row < height; row++) {
            long start = System.currentTimeMillis();
            BitSet[] oldBs = bsRows[0];
            for (int i = 0; i < gridSize; i++) {
                if (row == 0) {
                    bsRows[i] = getNextBitSetRow(records, rowStarts, row + i, uniqueSpeciesCount, null);
                } else {
                    if (row + i < height) {
                        if (i + 1 < bsRows.length) {
                            bsRows[i] = bsRows[i + 1];
                        } else {
                            bsRows[i] = getNextBitSetRow(records, rowStarts, row + i, uniqueSpeciesCount, oldBs);
                        }
                    }
                }
            }
            long t1 = System.currentTimeMillis();
            int startRow = (row == 0) ? 0 : row + gridSize / 2;
            int endRow = (row == height - 1) ? height - 1 : row + gridSize / 2;
            for (int currentRow = startRow; currentRow <= endRow; currentRow++) {
                if (bb != null) {
                    bb.reset();
                }
                int offset = gridSize / 2;
                CountDownLatch cdl = new CountDownLatch(partCount);
                for (int i = 0; i < threadCount; i++) {
                    if (getValues[i] == null) {
                        getValues[i] = new GetValuesSpeciesThread(lbqGetValues);
                        getValues[i].start();
                        getValues[i].setPriority(Thread.MIN_PRIORITY);
                        cells[i] = new BitSet(uniqueSpeciesCount);
                    }
                    getValues[i].set(cdl, partSize, bsRows, cells[i], values, worldwrap, height, width, offset, currentRow, row);
                }
                try {
                    for (int i = 0; i < partCount; i++) {
                        lbqGetValues.put(i);
                    }
                    cdl.await();
                } catch (InterruptedException e) {
                }
                long t2 = System.currentTimeMillis();
                for (int i = 0; i < width; i++) {
                    float value = values[i];
                    if (bb != null) {
                        if (max < value) {
                            max = value;
                        }
                        bb.putFloat(value);
                    }
                    if (bw != null) {
                        if (i > 0) {
                            if (bw != null) {
                                bw.append(" ");
                            }
                        }
                        if (value == 0) {
                            bw.append("0");
                        } else {
                            bw.append(String.valueOf(value));
                        }
                    }
                }
                if (bos != null) {
                    bos.write(bytes);
                }
                if (bw != null) {
                    bw.append("\n");
                }
                long end = System.currentTimeMillis();
            }
        }
        for (int i = 0; i < threadCount; i++) {
            getValues[i].interrupt();
        }
        if (bos != null) {
            bos.close();
        }
        if (bw != null) {
            bw.close();
        }
        DensityLayers.writeHeader(outputDirectory + filename + ".grd", resolution, height, width, bbox[0], bbox[1], bbox[2], bbox[3], 0, max, gridSize);
    }
}