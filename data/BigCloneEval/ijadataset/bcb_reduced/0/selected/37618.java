package com.americancoders;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.logging.Logger;

/**
 * point and figure class
 * 
 * 
 */
public class PointAndFigure {

    /** use a print writer for all printing, default to system out. */
    PrintWriter ps = new PrintWriter(System.out);

    /** generic logger */
    static Logger logger = Logger.getLogger(PointAndFigure.class.getName());

    /**
	 * callable from command line
	 * 
	 * @param args
	 * <br>
	 *            if first argument is -sym prompt for stock symbol, get the
	 *            data using GetStockData and the whole process <br>
	 *            otherwise first argument is a stock symbol get the data using
	 *            GetStockData and the whole process <br>
	 *            if second argument is present use for day week or month
	 *            request <br>
	 *            if no second then default to day request.
	 */
    public static void main(String[] args) {
        String dayorweek = "d";
        PrintWriter ps = new PrintWriter(System.out);
        if (args.length < 1) {
            System.out.println("pass 1 argument either -sym or a stock symbol");
            return;
        }
        if (args[0].equals("-sym")) {
            if (args.length > 1) dayorweek = args[1];
            args = new String[1];
            BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
            while (true) {
                try {
                    ps.println("(quit to end) enter symbol: ");
                    ps.flush();
                    args[0] = br.readLine().trim().toLowerCase();
                    if (args[0].length() == 0) continue;
                    if (args[0].equals("quit")) return;
                    GetStockData gsd = new GetStockData(args[0], dayorweek);
                    if (gsd.isGoodData() == false) {
                        System.out.println("no data to get");
                        continue;
                    }
                    PointAndFigure paf = new PointAndFigure(gsd);
                    paf.setDefaultBoxSize(gsd.getInLow()[0]);
                    paf.computeBoxSize();
                    paf.computeUsingClosings();
                    paf.makeCSVSimpleSpreadSheet();
                    paf.dump();
                    paf.genericBuy();
                    paf.genericSell();
                } catch (Exception e) {
                    e.printStackTrace();
                    return;
                }
            }
        }
        try {
            GetStockData gsd = new GetStockData(args[0], dayorweek);
            PointAndFigure paf = new PointAndFigure(gsd);
            paf.computeUsingHighsAndLows();
            paf.dump();
            ps.flush();
            paf.makeCSVSimpleSpreadSheet();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /** default reversal amount is 3 */
    static double reversalAmount = 3;

    /** default box size is 1 */
    static double boxSize = 1.0;

    static boolean dontResize = false;

    public static void setToDontResize() {
        dontResize = true;
    }

    double lows[];

    double closings[];

    double highs[];

    double opens[];

    double volumes[];

    String dts[];

    /** pafColumn is one column of x's or o's */
    ArrayList<PAFColumn> pafColumns = new ArrayList<PAFColumn>();

    public ArrayList<PAFColumn> getPAFColumns() {
        return pafColumns;
    }

    /**
	 * create a point and figure object with stock data
	 * 
	 * @param inGSD
	 *            see StockData
	 */
    public PointAndFigure(StockData inGSD) {
        pafColumns = new ArrayList<PAFColumn>();
        PAFColumn.resetLatestMonth();
        lows = inGSD.getInLow();
        closings = inGSD.getInClose();
        highs = inGSD.getInHigh();
        opens = inGSD.getInOpen();
        dts = inGSD.getInDate();
        volumes = inGSD.getInVolume();
        PAFColumn.resetLastCurrentBoxPosition();
        buildBoxes();
    }

    /**
	 * create a point and figure object from data arrays
	 * 
	 * @param inLows
	 * @param inCloses
	 * @param inHighs
	 * @param inOpens
	 * @param inDates
	 * @param inVolumes
	 */
    public PointAndFigure(double inLows[], double inCloses[], double inHighs[], double inOpens[], String inDates[], double inVolumes[]) {
        pafColumns = new ArrayList<PAFColumn>();
        PAFColumn.resetLatestMonth();
        lows = inLows;
        closings = inCloses;
        highs = inHighs;
        opens = inOpens;
        dts = inDates;
        volumes = inVolumes;
        PAFColumn.resetLastCurrentBoxPosition();
        buildBoxes();
    }

    /** compute the box size based on the prices */
    public void computeBoxSize() {
        double bestLow = Double.MAX_VALUE;
        double bestHigh = Double.MIN_VALUE;
        double sumHighLessLow = 0;
        int bi;
        for (bi = 0; bi < lows.length; bi++) {
            bestLow = Math.min(bestLow, lows[bi]);
            bestHigh = Math.max(bestHigh, highs[bi]);
            sumHighLessLow += (highs[bi] - lows[bi]);
        }
        boxSize = (bestLow + bestHigh) / 2;
        int bs = Double.valueOf(boxSize + .5).intValue();
        boxSize = Double.valueOf("" + bs) / 100;
        boxSize = sumHighLessLow / bi;
    }

    /**
	 * get the box size based on the low value passed, i forget where i found
	 * this criteria
	 * 
	 * @param inLow
	 * @return
	 */
    public double setDefaultBoxSize(double inLow) {
        if (dontResize) return boxSize;
        if (inLow < 0.25) boxSize = .0625; else if (inLow < 1.00) boxSize = .125; else if (inLow < 5.00) boxSize = .25; else if (inLow < 20.00) boxSize = .50; else if (inLow < 100.00) boxSize = 1.00; else if (inLow < 200.00) boxSize = 2.00; else if (inLow < 500.00) boxSize = 4.00; else if (inLow < 1000.00) boxSize = 5.00; else if (inLow < 25000.00) boxSize = 50.00; else boxSize = 500.00;
        return boxSize;
    }

    /**
	 * do paf calculations based on the high and low of the date
	 */
    public void computeUsingHighsAndLows() {
        PAFColumn currentColumn;
        if (opens[0] > closings[0]) {
            currentColumn = new PAFColumn(paftype.O, highs[0], dts[0], boxes);
            currentColumn.addToColumn(lows[0], dts[0]);
        } else {
            currentColumn = new PAFColumn(paftype.X, lows[0], dts[0], boxes);
            currentColumn.addToColumn(highs[0], dts[0]);
        }
        currentColumn.volume = volumes[0];
        pafColumns.add(currentColumn);
        for (int i = 1; i < highs.length; i++) {
            setDefaultBoxSize(lows[i]);
            if (currentColumn.myType == paftype.O) {
                if (highs[i] >= (currentColumn.getCurrentBox() + (boxSize * PointAndFigure.reversalAmount))) {
                    currentColumn = new PAFColumn(paftype.X, highs[i], dts[i], boxes);
                    pafColumns.add(currentColumn);
                } else currentColumn.addToColumn(lows[i], dts[i]);
                currentColumn.volume += volumes[i];
                continue;
            }
            if (lows[i] <= (currentColumn.getCurrentBox() - (boxSize * PointAndFigure.reversalAmount))) {
                currentColumn = new PAFColumn(paftype.O, lows[i], dts[i], boxes);
                pafColumns.add(currentColumn);
            } else currentColumn.addToColumn(highs[i], dts[i]);
            currentColumn.volume += volumes[i];
        }
    }

    /**
	 * do paf calculations based on the closing price
	 */
    public void computeUsingClosings() {
        PAFColumn currentColumn;
        if (opens[0] > closings[0]) {
            currentColumn = new PAFColumn(paftype.O, highs[0], dts[0], boxes);
            currentColumn.addToColumn(closings[0], dts[0]);
        } else {
            currentColumn = new PAFColumn(paftype.X, lows[0], dts[0], boxes);
            currentColumn.addToColumn(closings[0], dts[0]);
        }
        currentColumn.volume = volumes[0];
        pafColumns.add(currentColumn);
        for (int i = 1; i < highs.length; i++) {
            setDefaultBoxSize(closings[i]);
            if (currentColumn.myType == paftype.O) {
                if (closings[i] >= (currentColumn.getCurrentBox() + (boxSize * PointAndFigure.reversalAmount))) {
                    currentColumn = new PAFColumn(paftype.X, closings[i], dts[i], boxes);
                    pafColumns.add(currentColumn);
                } else currentColumn.addToColumn(closings[i], dts[i]);
                currentColumn.volume += volumes[i];
                continue;
            }
            if (closings[i] <= (currentColumn.getCurrentBox() - (boxSize * PointAndFigure.reversalAmount))) {
                currentColumn = new PAFColumn(paftype.O, closings[i], dts[i], boxes);
                pafColumns.add(currentColumn);
            } else currentColumn.addToColumn(closings[i], dts[i]);
            currentColumn.volume += volumes[i];
        }
    }

    /**
	 * generates a csv file for import to a spreadsheet to generate a
	 * rudimentary chart, writes to System Out
	 */
    public void makeCSVSimpleSpreadSheet() {
        for (int boxpos = boxes.length - 1; boxpos > 0; boxpos--) {
            System.out.print(boxes[boxpos]);
            for (int column = 0; column < pafColumns.size(); column++) {
                PAFColumn currentColumn = pafColumns.get(column);
                if (currentColumn.myType == paftype.O) {
                    if (boxpos < currentColumn.stopAt) {
                        System.out.print(",");
                    } else if (boxpos <= currentColumn.startAt) {
                        if (currentColumn.monthIndicators.containsKey(boxpos)) {
                            System.out.print("," + currentColumn.monthIndicators.get(boxpos));
                        } else System.out.print("," + currentColumn.myType);
                    } else {
                        System.out.print(",");
                    }
                } else {
                    if (boxpos < currentColumn.startAt) {
                        System.out.print(",");
                    } else if (boxpos <= currentColumn.stopAt) {
                        if (currentColumn.monthIndicators.containsKey(boxpos)) {
                            System.out.print("," + currentColumn.monthIndicators.get(boxpos));
                        } else System.out.print("," + currentColumn.myType);
                    } else {
                        System.out.print(",");
                    }
                }
            }
            System.out.println("," + boxes[boxpos]);
        }
        String lastYear = "";
        boolean dontShowYears[] = new boolean[pafColumns.size()];
        for (int column = 0; column < pafColumns.size(); column++) {
            PAFColumn currentColumn = pafColumns.get(column);
            String thisYear = "" + currentColumn.startDate.charAt(2) + currentColumn.startDate.charAt(3);
            dontShowYears[column] = lastYear.matches(thisYear);
            lastYear = thisYear;
        }
        int dateFields[] = { 2, 3 };
        for (int showField = 0; showField < dateFields.length; showField++) {
            System.out.print(",");
            for (int column = 0; column < pafColumns.size(); column++) {
                PAFColumn currentColumn = pafColumns.get(column);
                if (dontShowYears[column]) System.out.print(","); else if (showField == 0 && currentColumn.startDate.charAt(dateFields[showField]) == '0') System.out.print(","); else System.out.print(currentColumn.startDate.charAt(dateFields[showField]) + ",");
            }
            System.out.println("");
        }
    }

    public static String buySignal = "Buy Signal";

    public static String sellSignal = "Sell Signal";

    public String getPandFBuy() {
        int colSize = pafColumns.size();
        int colCheck = colSize - 1;
        if (pafColumns.get(colCheck).myType == paftype.X) {
            if (colSize > 2) {
                if (pafColumns.get(colCheck).getCurrentBox() > pafColumns.get(colCheck - 2).getCurrentBox()) return buySignal + " at " + pafColumns.get(colCheck).toString();
            }
        }
        return "";
    }

    public String getPandFSell() {
        int colSize = pafColumns.size();
        int colCheck = colSize - 1;
        if (pafColumns.get(colCheck).myType == paftype.O) {
            if (colSize > 2) {
                if (pafColumns.get(colCheck).getCurrentBox() < pafColumns.get(colCheck - 2).getCurrentBox()) return sellSignal + " at " + pafColumns.get(colCheck).toString();
            }
        }
        return "";
    }

    public static String doubleTop = "Double Top";

    public static String doubleTopBreakout = "Double Top Breakout";

    public String getDoubleTopAndBreakouts() {
        int colSize = pafColumns.size();
        int colCheck = colSize - 1;
        if (pafColumns.get(colCheck).myType == paftype.X) {
            if (colSize > 2) {
                if (pafColumns.get(colCheck).getCurrentBox() == pafColumns.get(colCheck - 2).getCurrentBox()) {
                    return doubleTop + " at " + pafColumns.get(colCheck).toString() + " and " + pafColumns.get(colCheck - 2).toString();
                }
                if (pafColumns.get(colCheck).getCurrentBox() > pafColumns.get(colCheck - 2).getCurrentBox()) {
                    return doubleTopBreakout + " at " + pafColumns.get(colCheck).toString() + " and " + pafColumns.get(colCheck - 2).toString();
                }
            }
        }
        return "";
    }

    public static String doubleBottom = "Double Bottom";

    public static String doubleBottomBreakdown = "Double Bottom Breakdown";

    public String getDoubleBottomAndBreakdowns() {
        int colSize = pafColumns.size();
        int colCheck = colSize - 1;
        if (pafColumns.get(colCheck).myType == paftype.O) {
            if (colSize > 3) {
                if (pafColumns.get(colCheck).getCurrentBox() == pafColumns.get(colCheck - 2).getCurrentBox()) {
                    return doubleBottom + " at " + pafColumns.get(colCheck).toString() + " and " + pafColumns.get(colCheck - 2).toString();
                }
                if (pafColumns.get(colCheck).getCurrentBox() < pafColumns.get(colCheck - 2).getCurrentBox()) {
                    return doubleBottomBreakdown + " at " + pafColumns.get(colCheck).toString() + " and " + pafColumns.get(colCheck - 2).toString();
                }
            }
        }
        return "";
    }

    public static String tripleTop = "Triple Top";

    public static String tripleTopBreakout = "Triple Top Breakout";

    public String getTripleTopAndBreakouts() {
        int colSize = pafColumns.size();
        int colCheck = colSize - 1;
        if (pafColumns.get(colCheck).myType == paftype.X) {
            if (colSize > 4) {
                if (pafColumns.get(colCheck).getCurrentBox() > pafColumns.get(colCheck - 2).getCurrentBox() && pafColumns.get(colCheck - 2).getCurrentBox() == pafColumns.get(colCheck - 4).getCurrentBox()) {
                    return tripleTop + " at " + pafColumns.get(colCheck).toString() + " and " + pafColumns.get(colCheck - 2).toString() + " and " + pafColumns.get(colCheck - 4).toString();
                }
                if (pafColumns.get(colCheck).getCurrentBox() == pafColumns.get(colCheck - 2).getCurrentBox() && pafColumns.get(colCheck - 2).getCurrentBox() == pafColumns.get(colCheck - 4).getCurrentBox()) {
                    return tripleTopBreakout + " at " + pafColumns.get(colCheck).toString() + " and " + pafColumns.get(colCheck - 2).toString() + " and " + pafColumns.get(colCheck - 4).toString();
                }
            }
        }
        return "";
    }

    public static String tripleBottom = "Triple Bottom";

    public static String tripleBottomBreakdown = "Triple Bottom Breakdown";

    public String getTripleBottomAndBreakdowns() {
        int colSize = pafColumns.size();
        int colCheck = colSize - 1;
        if (pafColumns.get(colCheck).myType == paftype.O) {
            if (colSize > 4) {
                if (pafColumns.get(colCheck).getCurrentBox() == pafColumns.get(colCheck - 2).getCurrentBox() && pafColumns.get(colCheck - 2).getCurrentBox() == pafColumns.get(colCheck - 4).getCurrentBox()) {
                    return tripleBottom + " at " + pafColumns.get(colCheck).toString() + " and " + pafColumns.get(colCheck - 2).toString() + " and " + pafColumns.get(colCheck - 4).toString();
                }
                if (pafColumns.get(colCheck).getCurrentBox() < pafColumns.get(colCheck - 2).getCurrentBox() && pafColumns.get(colCheck - 2).getCurrentBox() == pafColumns.get(colCheck - 4).getCurrentBox()) {
                    return tripleBottomBreakdown + " at " + pafColumns.get(colCheck).toString() + " and " + pafColumns.get(colCheck - 2).toString() + " and " + pafColumns.get(colCheck - 4).toString();
                }
            }
        }
        return "";
    }

    public static String quadrupleTop = "Quadruple Top";

    public static String quadrupleTopBreakout = "Quadruple Top Breakout";

    public String getQuadrupleTopAndBreakouts() {
        int colSize = pafColumns.size();
        int colCheck = colSize - 1;
        if (pafColumns.get(colCheck).myType == paftype.X) {
            if (colSize > 6) {
                if (pafColumns.get(colCheck).getCurrentBox() == pafColumns.get(colCheck - 2).getCurrentBox() && pafColumns.get(colCheck - 2).getCurrentBox() == pafColumns.get(colCheck - 4).getCurrentBox() && pafColumns.get(colCheck - 2).getCurrentBox() == pafColumns.get(colCheck - 6).getCurrentBox()) {
                    return quadrupleTop + " at " + pafColumns.get(colCheck).toString() + " and " + pafColumns.get(colCheck - 2).toString() + " and " + pafColumns.get(colCheck - 4).toString() + " and " + pafColumns.get(colCheck - 6).toString();
                }
                if (pafColumns.get(colCheck).getCurrentBox() > pafColumns.get(colCheck - 2).getCurrentBox() && pafColumns.get(colCheck - 2).getCurrentBox() == pafColumns.get(colCheck - 4).getCurrentBox() && pafColumns.get(colCheck - 2).getCurrentBox() == pafColumns.get(colCheck - 6).getCurrentBox()) {
                    return quadrupleTopBreakout + " at " + pafColumns.get(colCheck).toString() + " and " + pafColumns.get(colCheck - 2).toString() + " and " + pafColumns.get(colCheck - 4).toString() + " and " + pafColumns.get(colCheck - 6).toString();
                }
            }
        }
        return "";
    }

    public static String quadrupleBottom = "Quadruple Bottom";

    public static String quadrupleBottomBreakdown = "Quadruple Bottom Breakdown";

    public String getQuadrupleBottomAndBreakdowns() {
        int colSize = pafColumns.size();
        int colCheck = colSize - 1;
        if (pafColumns.get(colCheck).myType == paftype.O) {
            if (colSize > 6) {
                if (pafColumns.get(colCheck).getCurrentBox() == pafColumns.get(colCheck - 2).getCurrentBox() && pafColumns.get(colCheck - 2).getCurrentBox() == pafColumns.get(colCheck - 4).getCurrentBox() && pafColumns.get(colCheck - 2).getCurrentBox() == pafColumns.get(colCheck - 6).getCurrentBox()) {
                    return quadrupleBottom + " at " + pafColumns.get(colCheck).toString() + " and " + pafColumns.get(colCheck - 2).toString() + " and " + pafColumns.get(colCheck - 4).toString() + " and " + pafColumns.get(colCheck - 6).toString();
                }
                if (pafColumns.get(colCheck).getCurrentBox() < pafColumns.get(colCheck - 2).getCurrentBox() && pafColumns.get(colCheck - 2).getCurrentBox() == pafColumns.get(colCheck - 4).getCurrentBox() && pafColumns.get(colCheck - 2).getCurrentBox() == pafColumns.get(colCheck - 6).getCurrentBox()) {
                    return quadrupleBottomBreakdown + " at " + pafColumns.get(colCheck).toString() + " and " + pafColumns.get(colCheck - 2).toString() + " and " + pafColumns.get(colCheck - 4).toString() + " and " + pafColumns.get(colCheck - 6).toString();
                }
            }
        }
        return "";
    }

    public static String ascendingTripleBreakout = "Ascending Triple Breakout";

    public String getAscendingTripleBreakout() {
        int colSize = pafColumns.size();
        int colCheck = colSize - 1;
        if (pafColumns.get(colCheck).myType == paftype.X) {
            if (colSize > 2) {
                if (pafColumns.get(colCheck).getCurrentBox() > pafColumns.get(colCheck - 2).getCurrentBox()) {
                    if (colSize > 4) {
                        if (pafColumns.get(colCheck - 2).getCurrentBox() > pafColumns.get(colCheck - 4).getCurrentBox()) {
                            return ascendingTripleBreakout + " at " + pafColumns.get(colCheck).toString() + " and " + pafColumns.get(colCheck - 2).toString() + " and " + pafColumns.get(colCheck - 4).toString();
                        }
                    }
                }
            }
        }
        return "";
    }

    public static String descendingTripleBreakdown = "Descending Triple Breakdown";

    public String getDescendingTripleBreakdown() {
        int colSize = pafColumns.size();
        int colCheck = colSize - 1;
        if (pafColumns.get(colCheck).myType == paftype.O) {
            if (colSize > 2) {
                if (pafColumns.get(colCheck).getCurrentBox() < pafColumns.get(colCheck - 2).getCurrentBox()) {
                    if (colSize > 4) {
                        if (pafColumns.get(colCheck - 2).getCurrentBox() < pafColumns.get(colCheck - 4).getCurrentBox()) {
                            return descendingTripleBreakdown + " at " + pafColumns.get(colCheck).toString() + " and " + pafColumns.get(colCheck - 2).toString() + " and " + pafColumns.get(colCheck - 4).toString();
                        }
                    }
                }
            }
        }
        return "";
    }

    public static String bullishCatapultBreakout = "Bullish Catapult Breakout";

    public String getBullishCatapultBreakout() {
        int colSize = pafColumns.size();
        int colCheck = colSize - 1;
        if (pafColumns.get(colCheck).myType == paftype.X) {
            if (colSize > 2) {
                if (pafColumns.get(colCheck).getCurrentBox() > pafColumns.get(colCheck - 2).getCurrentBox()) {
                    if (colSize > 4) {
                        if (pafColumns.get(colCheck - 2).getCurrentBox() > pafColumns.get(colCheck - 4).getCurrentBox()) {
                            if (colSize > 6) {
                                if (pafColumns.get(colCheck - 4).getCurrentBox() == pafColumns.get(colCheck - 6).getCurrentBox()) {
                                    return bullishCatapultBreakout + " at " + pafColumns.get(colCheck).toString() + " and " + pafColumns.get(colCheck - 2).toString() + " and " + pafColumns.get(colCheck - 4).toString() + " and " + pafColumns.get(colCheck - 6).toString();
                                }
                            }
                        }
                    }
                }
            }
        }
        return "";
    }

    public static String bearishCatapultBreakdown = "Bearish Catapult Breakdown";

    public String getBearishCatapultBreakdown() {
        int colSize = pafColumns.size();
        int colCheck = colSize - 1;
        if (pafColumns.get(colCheck).myType == paftype.O) {
            if (colSize > 2) {
                if (pafColumns.get(colCheck).getCurrentBox() < pafColumns.get(colCheck - 2).getCurrentBox()) {
                    if (colSize > 4) {
                        if (pafColumns.get(colCheck - 2).getCurrentBox() < pafColumns.get(colCheck - 4).getCurrentBox()) {
                            if (colSize > 6) {
                                if (pafColumns.get(colCheck - 4).getCurrentBox() == pafColumns.get(colCheck - 6).getCurrentBox()) {
                                    return bearishCatapultBreakdown + " at " + pafColumns.get(colCheck).toString() + " and " + pafColumns.get(colCheck - 2).toString() + " and " + pafColumns.get(colCheck - 4).toString() + " and " + pafColumns.get(colCheck - 6).toString();
                                }
                            }
                        }
                    }
                }
            }
        }
        return "";
    }

    public static String bullishTriangleBreakout = "Bullish Triangle Breakout";

    public String getBullishTriangleBreakout() {
        int colSize = pafColumns.size();
        int colCheck = colSize - 1;
        if (colSize > 6) {
            if (pafColumns.get(colCheck).myType == paftype.X) {
                if (pafColumns.get(colCheck).getCurrentBox() > pafColumns.get(colCheck - 2).getCurrentBox() && pafColumns.get(colCheck - 1).getCurrentBox() > pafColumns.get(colCheck - 3).getCurrentBox() && pafColumns.get(colCheck - 2).getCurrentBox() < pafColumns.get(colCheck - 4).getCurrentBox() && pafColumns.get(colCheck - 3).getCurrentBox() > pafColumns.get(colCheck - 5).getCurrentBox() && pafColumns.get(colCheck - 4).getCurrentBox() < pafColumns.get(colCheck - 6).getCurrentBox()) return bullishTriangleBreakout + " at " + pafColumns.get(colCheck).toString();
            }
        }
        return "";
    }

    public static String bearishTriangleBreakdown = "Bearish Triangle Breakdown";

    public String getBearishTriangleBreakdown() {
        int colSize = pafColumns.size();
        int colCheck = colSize - 1;
        if (colSize > 6) {
            if (pafColumns.get(colCheck).myType == paftype.O) {
                if (pafColumns.get(colCheck).getCurrentBox() < pafColumns.get(colCheck - 2).getCurrentBox() && pafColumns.get(colCheck - 1).getCurrentBox() < pafColumns.get(colCheck - 3).getCurrentBox() && pafColumns.get(colCheck - 2).getCurrentBox() > pafColumns.get(colCheck - 4).getCurrentBox() && pafColumns.get(colCheck - 3).getCurrentBox() < pafColumns.get(colCheck - 5).getCurrentBox() && pafColumns.get(colCheck - 4).getCurrentBox() > pafColumns.get(colCheck - 6).getCurrentBox()) return bearishTriangleBreakdown + " at " + pafColumns.get(colCheck).toString();
            }
        }
        return "";
    }

    public static String bullishSignalReversed = "Bullish Signal Reversed";

    public String getBullishSignalReversed() {
        int colSize = pafColumns.size();
        int colCheck = colSize - 1;
        if (colSize > 7) {
            if (pafColumns.get(colCheck).myType == paftype.O) {
                if (pafColumns.get(colCheck).getCurrentBox() < pafColumns.get(colCheck - 2).getCurrentBox() && pafColumns.get(colCheck - 1).getCurrentBox() > pafColumns.get(colCheck - 3).getCurrentBox() && pafColumns.get(colCheck - 2).getCurrentBox() > pafColumns.get(colCheck - 4).getCurrentBox() && pafColumns.get(colCheck - 3).getCurrentBox() > pafColumns.get(colCheck - 5).getCurrentBox() && pafColumns.get(colCheck - 4).getCurrentBox() > pafColumns.get(colCheck - 6).getCurrentBox() && pafColumns.get(colCheck - 5).getCurrentBox() > pafColumns.get(colCheck - 7).getCurrentBox()) return bullishSignalReversed + " at " + pafColumns.get(colCheck).toString();
            }
        }
        return "";
    }

    public static String bearishSignalReversed = "Bearish Signal Reversed";

    public String getBearishSignalReversed() {
        int colSize = pafColumns.size();
        int colCheck = colSize - 1;
        if (colSize > 7) {
            if (pafColumns.get(colCheck).myType == paftype.X) {
                if (pafColumns.get(colCheck).getCurrentBox() > pafColumns.get(colCheck - 2).getCurrentBox() && pafColumns.get(colCheck - 1).getCurrentBox() < pafColumns.get(colCheck - 3).getCurrentBox() && pafColumns.get(colCheck - 2).getCurrentBox() < pafColumns.get(colCheck - 4).getCurrentBox() && pafColumns.get(colCheck - 3).getCurrentBox() < pafColumns.get(colCheck - 5).getCurrentBox() && pafColumns.get(colCheck - 4).getCurrentBox() < pafColumns.get(colCheck - 6).getCurrentBox() && pafColumns.get(colCheck - 5).getCurrentBox() < pafColumns.get(colCheck - 7).getCurrentBox()) return bearishSignalReversed + " at " + pafColumns.get(colCheck).toString();
            }
        }
        return "";
    }

    public static String longTailDownReversal = "Long tail down reversal";

    public String getLongTailDownReversal() {
        int colSize = pafColumns.size();
        int colCheck = colSize - 1;
        if (pafColumns.get(colCheck).myType == paftype.X) {
            if (colSize > 2) {
                if (pafColumns.get(colCheck - 1).startAt - pafColumns.get(colCheck - 1).stopAt > 19) {
                    return longTailDownReversal + " at " + pafColumns.get(colCheck).toString();
                }
            }
        }
        return "";
    }

    public static String bullTrap = "Bull Trap";

    public String getBullTrap() {
        int colSize = pafColumns.size();
        int colCheck = colSize - 1;
        if (colCheck > 5) if (pafColumns.get(colCheck).myType == paftype.O) {
            if (pafColumns.get(colCheck - 1).stopAt == pafColumns.get(colCheck - 3).stopAt + 1 && pafColumns.get(colCheck - 3).getCurrentBox() == pafColumns.get(colCheck - 5).getCurrentBox()) {
                return bullTrap + " at " + pafColumns.get(colCheck).toString();
            }
        }
        return "";
    }

    public static String bearTrap = "Bear Trap";

    public String getBearTrap() {
        int colSize = pafColumns.size();
        int colCheck = colSize - 1;
        if (colCheck > 5) if (pafColumns.get(colCheck).myType == paftype.X) {
            if (pafColumns.get(colCheck - 1).stopAt == pafColumns.get(colCheck - 3).stopAt - 1 && pafColumns.get(colCheck - 3).getCurrentBox() == pafColumns.get(colCheck - 5).getCurrentBox()) {
                return bearTrap + " at " + pafColumns.get(colCheck).toString();
            }
        }
        return "";
    }

    public static String spreadTripleTop = "Spread Triple Top";

    public String getSpreadTripleTop() {
        int colSize = pafColumns.size();
        int colCheck = colSize - 1;
        if (colCheck > 5) {
            if (pafColumns.get(colCheck).myType == paftype.X) {
                if (pafColumns.get(colCheck).getCurrentBox() > pafColumns.get(colCheck - 2).getCurrentBox() && pafColumns.get(colCheck).getCurrentBox() == pafColumns.get(colCheck - 4).getCurrentBox() && pafColumns.get(colCheck).getCurrentBox() == pafColumns.get(colCheck - 6).getCurrentBox()) {
                    return spreadTripleTop;
                }
            }
            if (pafColumns.get(colCheck).myType == paftype.X) {
                if (pafColumns.get(colCheck).getCurrentBox() > pafColumns.get(colCheck - 2).getCurrentBox() && pafColumns.get(colCheck).getCurrentBox() > pafColumns.get(colCheck - 4).getCurrentBox() && pafColumns.get(colCheck - 4).getCurrentBox() == pafColumns.get(colCheck - 6).getCurrentBox()) {
                    return spreadTripleTop + " Breakout";
                }
            }
        }
        return "";
    }

    public static String spreadTripleBottom = "Spread Triple Bottom ";

    public String getSpreadTripleBottom() {
        int colSize = pafColumns.size();
        int colCheck = colSize - 1;
        if (colCheck > 5) {
            if (pafColumns.get(colCheck).myType == paftype.O) {
                if (pafColumns.get(colCheck).getCurrentBox() < pafColumns.get(colCheck - 2).getCurrentBox() && pafColumns.get(colCheck).getCurrentBox() == pafColumns.get(colCheck - 4).getCurrentBox() && pafColumns.get(colCheck).getCurrentBox() == pafColumns.get(colCheck - 6).getCurrentBox()) {
                    return spreadTripleBottom;
                }
            }
            if (pafColumns.get(colCheck).myType == paftype.O) {
                if (pafColumns.get(colCheck).getCurrentBox() < pafColumns.get(colCheck - 2).getCurrentBox() && pafColumns.get(colCheck).getCurrentBox() < pafColumns.get(colCheck - 4).getCurrentBox() && pafColumns.get(colCheck - 4).getCurrentBox() == pafColumns.get(colCheck - 6).getCurrentBox()) {
                    return spreadTripleBottom + " Breakdown";
                }
            }
        }
        return "";
    }

    public static String highPole = "High Pole";

    public String getHighPole() {
        int colSize = pafColumns.size();
        int colCheck = colSize - 1;
        if (colCheck > 2) {
            if (pafColumns.get(colCheck).myType == paftype.O) {
                if (pafColumns.get(colCheck - 1).stopAt > (pafColumns.get(colCheck - 3).stopAt + 3) && ((pafColumns.get(colCheck).startAt - pafColumns.get(colCheck).stopAt) >= (pafColumns.get(colCheck - 1).stopAt - pafColumns.get(colCheck - 1).startAt) / 2)) return highPole;
            }
        }
        return "";
    }

    public static String lowPoleReversal = "Low Pole Reversal";

    public String getLowPoleReversal() {
        int colSize = pafColumns.size();
        int colCheck = colSize - 1;
        if (colCheck > 2) {
            if (pafColumns.get(colCheck).myType == paftype.X) {
                if (pafColumns.get(colCheck - 1).stopAt < (pafColumns.get(colCheck - 3).stopAt - 3) && ((pafColumns.get(colCheck).stopAt - pafColumns.get(colCheck).startAt) <= (pafColumns.get(colCheck - 1).startAt - pafColumns.get(colCheck - 1).stopAt) / 2)) return lowPoleReversal;
            }
        }
        return "";
    }

    public void dumpPrint(String doWePrintIt) {
        if (doWePrintIt.length() > 0) System.out.println(doWePrintIt);
    }

    /**
	 * dump the paf objects to a readable format
	 */
    public void dump() {
        for (int i = 0; i < pafColumns.size(); i++) {
            System.out.println(pafColumns.get(i).toString());
        }
        SupportLevels lSupport = new SupportLevels(levelType.Support, pafColumns);
        System.out.println(lSupport.toString());
        System.out.println("Box size is " + boxSize);
        SupportLevels lResistance = new SupportLevels(levelType.Resistance, pafColumns);
        System.out.println(lResistance.toString());
        dumpPrint(getPandFBuy());
        dumpPrint(getPandFSell());
        dumpPrint(getDoubleTopAndBreakouts());
        dumpPrint(getAscendingTripleBreakout());
        dumpPrint(getDoubleBottomAndBreakdowns());
        dumpPrint(getTripleTopAndBreakouts());
        dumpPrint(getTripleBottomAndBreakdowns());
        dumpPrint(getQuadrupleTopAndBreakouts());
        dumpPrint(getQuadrupleBottomAndBreakdowns());
        dumpPrint(getAscendingTripleBreakout());
        dumpPrint(getDescendingTripleBreakdown());
        dumpPrint(getBullishCatapultBreakout());
        dumpPrint(getBearishCatapultBreakdown());
        dumpPrint(getBullishSignalReversed());
        dumpPrint(getBearishSignalReversed());
        dumpPrint(getBullishTriangleBreakout());
        dumpPrint(getBearishTriangleBreakdown());
        dumpPrint(getLongTailDownReversal());
        dumpPrint(getBullTrap());
        dumpPrint(getBearTrap());
        dumpPrint(getSpreadTripleTop());
        dumpPrint(getSpreadTripleBottom());
        dumpPrint(getHighPole());
        dumpPrint(getLowPoleReversal());
    }

    double boxes[] = null;

    /** build a column of boxes that indicate the starting point of each box. */
    public void buildBoxes() {
        double bestLow = Double.MAX_VALUE;
        double bestHigh = Double.MIN_VALUE;
        int bi;
        for (bi = 0; bi < lows.length; bi++) {
            bestLow = Math.min(bestLow, lows[bi]);
            bestHigh = Math.max(bestHigh, highs[bi]);
        }
        String ds = (bestLow - setDefaultBoxSize(bestLow)) + "";
        String b4dp[] = ds.split("\\.");
        double intv = Double.parseDouble(b4dp[0]);
        ArrayList<Double> dboxes = new ArrayList<Double>();
        for (double l = intv; l < bestHigh + boxSize * 2; l += setDefaultBoxSize(l)) {
            dboxes.add(new Double(l));
        }
        boxes = new double[dboxes.size()];
        for (int i = 0; i < boxes.length; i++) {
            boxes[i] = dboxes.get(i).doubleValue();
        }
    }

    public boolean genericBuy() {
        boolean ret = false;
        ret |= getPandFBuy().length() > 0;
        ret |= getBearTrap().length() > 0;
        ret |= getBullishCatapultBreakout().length() > 0;
        ret |= getBullishSignalReversed().length() > 0;
        ret |= getBullishTriangleBreakout().length() > 0;
        ret |= getAscendingTripleBreakout().length() > 0;
        ret |= getDoubleTopAndBreakouts().length() > 0;
        ret |= getTripleTopAndBreakouts().length() > 0;
        ret |= getQuadrupleTopAndBreakouts().length() > 0;
        ret |= getSpreadTripleTop().length() > 0;
        return ret;
    }

    public boolean genericSell() {
        boolean ret = false;
        ret |= getPandFSell().length() > 0;
        ret |= getBullTrap().length() > 0;
        ret |= getBearishCatapultBreakdown().length() > 0;
        ret |= getBearishSignalReversed().length() > 0;
        ret |= getBearishTriangleBreakdown().length() > 0;
        ret |= getDescendingTripleBreakdown().length() > 0;
        ret |= getDoubleBottomAndBreakdowns().length() > 0;
        ret |= getTripleBottomAndBreakdowns().length() > 0;
        ret |= getQuadrupleBottomAndBreakdowns().length() > 0;
        ret |= getSpreadTripleBottom().length() > 0;
        return ret;
    }
}
