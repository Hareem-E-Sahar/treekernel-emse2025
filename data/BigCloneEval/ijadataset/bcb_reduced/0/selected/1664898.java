package gotha;

import java.rmi.*;
import java.awt.*;
import java.awt.print.*;
import javax.swing.*;
import java.text.*;
import java.util.*;

/**
 * TournamentPrinting manages printing jobs.
 * @author LV
 */
public class TournamentPrinting implements Printable {

    static final int TYPE_DEFAULT = 0;

    static final int TYPE_PLAYERSLIST = 1;

    static final int TYPE_GAMESLIST = 2;

    static final int TYPE_STANDINGS = 3;

    static final int TYPE_TOURNAMENT_PARAMETERS = 4;

    static final int SUBTYPE_DEFAULT = 0;

    static final int SUBTYPE_ST_CAT = 1;

    static final int PL_NUMBER_BEG = 0;

    static final int PL_NUMBER_LEN = 4;

    static final int PL_PINLIC_BEG = PL_NUMBER_BEG + PL_NUMBER_LEN + 1;

    static final int PL_PINLIC_LEN = 8;

    static final int PL_NF_BEG = PL_PINLIC_BEG + PL_PINLIC_LEN + 1;

    static final int PL_NF_LEN = 25;

    static final int PL_RANK_BEG = PL_NF_BEG + PL_NF_LEN + 1;

    static final int PL_RANK_LEN = 3;

    static final int PL_MM_BEG = PL_RANK_BEG + PL_RANK_LEN + 1;

    static final int PL_MM_LEN = 3;

    static final int PL_COUNTRY_BEG = PL_MM_BEG + PL_MM_LEN + 1;

    static final int PL_COUNTRY_LEN = 2;

    static final int PL_CLUB_BEG = PL_COUNTRY_BEG + PL_COUNTRY_LEN + 1;

    static final int PL_CLUB_LEN = 4;

    static final int PL_PART_BEG = PL_CLUB_BEG + PL_CLUB_LEN + 1;

    static final int PL_PART_LEN = Gotha.MAX_NUMBER_OF_ROUNDS;

    static final int PL_PADDING = 0;

    static final int PL_NBCAR = PL_PART_BEG + PL_PART_LEN + PL_PADDING;

    static final int GL_TN_BEG = 0;

    static final int GL_TN_LEN = 4;

    static final int GL_WNF_BEG = GL_TN_BEG + GL_TN_LEN + 1;

    static final int GL_WNF_LEN = 33;

    static final int GL_BNF_BEG = GL_WNF_BEG + GL_WNF_LEN + 1;

    static final int GL_BNF_LEN = 32;

    static final int GL_HD_BEG = GL_BNF_BEG + GL_BNF_LEN + 1;

    static final int GL_HD_LEN = 1;

    static final int GL_RES_BEG = GL_HD_BEG + GL_HD_LEN + 1;

    static final int GL_RES_LEN = 3;

    static final int GL_PADDING = 2;

    static final int GL_NBCAR = GL_RES_BEG + GL_RES_LEN + GL_PADDING;

    static final int ST_NUM_BEG = 0;

    static final int ST_NUM_LEN = 4;

    static final int ST_PL_BEG = ST_NUM_BEG + ST_NUM_LEN + 1;

    static final int ST_PL_LEN = 4;

    static final int ST_NF_BEG = ST_PL_BEG + ST_PL_LEN + 1;

    static final int ST_NF_LEN = 22;

    static final int ST_RK_BEG = ST_NF_BEG + ST_NF_LEN + 1;

    static final int ST_RK_LEN = 3;

    static final int ST_NBW_BEG = ST_RK_BEG + ST_RK_LEN + 1;

    static final int ST_NBW_LEN = 3;

    static final int ST_ROUND0_BEG = ST_NBW_BEG + ST_NBW_LEN + 1;

    static final int ST_ROUND_LEN = 7;

    static final int ST_CRIT_LEN = 6;

    static final int ST_PADDING = 1;

    static final int ST_NBFXCAR = ST_ROUND0_BEG + ST_PADDING;

    static final int TP_TAB1 = 6;

    static final int TP_TAB2 = 12;

    static final int TP_TAB3 = 18;

    static final int TP_TAB4 = 24;

    static final int TP_NBCAR = 80;

    static final int WH_RATIO = 50;

    static final int LINEFILLING_RATIO = 90;

    static final int LHFS_RATIO = 140;

    TournamentInterface tournament;

    int printType;

    int printSubType;

    /** from 0 to ... */
    private int roundNumber = -1;

    ArrayList<Player> alPlayersToPrint;

    private ArrayList<ScoredPlayer> alOrderedScoredPlayers;

    private String[][] halfGamesStrings;

    private int[] printCriteria;

    String[] strPlace;

    PrinterJob printerJob;

    PageFormat pageFormat;

    int usableX = -1;

    int usableY = -1;

    int usableWidth = -1;

    int usableHeight = -1;

    int fontSize;

    int lineHeight;

    int numberOfBodyLinesInAPage;

    int numberOfPages;

    int numberOfCharactersInALine;

    public TournamentPrinting(TournamentInterface tournament) {
        this.tournament = tournament;
        printerJob = PrinterJob.getPrinterJob();
        pageFormat = new PageFormat();
        Paper paper = new Paper();
        paper.setImageableArea(0, 0, paper.getWidth(), paper.getHeight());
        pageFormat.setPaper(paper);
        printerJob.setPrintable(this, pageFormat);
    }

    public void makePrinting(int printType, int printSubType) {
        this.printType = printType;
        this.printSubType = printSubType;
        switch(printType) {
            case TournamentPrinting.TYPE_PLAYERSLIST:
                int playersSortType = printSubType;
                try {
                    alPlayersToPrint = new ArrayList<Player>(tournament.playersList());
                } catch (RemoteException ex) {
                    ex.printStackTrace();
                }
                PlayerComparator playerComparator = new PlayerComparator(playersSortType);
                Collections.sort(alPlayersToPrint, playerComparator);
                break;
            case TournamentPrinting.TYPE_STANDINGS:
                int numberOfRoundsPrinted = roundNumber + 1;
                int numberOfCriteriaPrinted = printCriteria.length;
                numberOfCharactersInALine = ST_NBFXCAR + numberOfRoundsPrinted * this.ST_ROUND_LEN + numberOfCriteriaPrinted * ST_CRIT_LEN;
                TournamentParameterSet tps = null;
                try {
                    tps = tournament.getTournamentParameterSet();
                } catch (RemoteException ex) {
                    ex.printStackTrace();
                }
                TournamentParameterSet printTPS = new TournamentParameterSet(tps);
                PlacementParameterSet printPPS = printTPS.getPlacementParameterSet();
                printPPS.setPlaCriteria(this.printCriteria);
                strPlace = ScoredPlayer.catPositionStrings(alOrderedScoredPlayers, roundNumber, printTPS);
                halfGamesStrings = ScoredPlayer.halfGamesStrings(alOrderedScoredPlayers, roundNumber, printTPS);
        }
        if (printerJob.printDialog()) {
            try {
                printerJob.print();
            } catch (PrinterException e) {
                JOptionPane.showMessageDialog(null, e);
            }
        }
    }

    public int print(Graphics g, PageFormat pf, int pi) {
        TournamentParameterSet tps = null;
        try {
            tps = tournament.getTournamentParameterSet();
        } catch (RemoteException ex) {
            ex.printStackTrace();
        }
        GeneralParameterSet gps = tps.getGeneralParameterSet();
        if (usableX < 0) {
            usableX = (int) pf.getImageableX() + 1;
            usableY = (int) pf.getImageableY() + 1;
            usableWidth = (int) pf.getImageableWidth() - 2;
            usableHeight = (int) pf.getImageableHeight() - 2;
            switch(printType) {
                case TYPE_DEFAULT:
                    fontSize = 12;
                    lineHeight = fontSize * LHFS_RATIO;
                    break;
                case TYPE_PLAYERSLIST:
                    int nbCarRef = PL_NBCAR;
                    fontSize = usableWidth / nbCarRef * 100 / WH_RATIO * LINEFILLING_RATIO / 100;
                    lineHeight = fontSize * LHFS_RATIO / 100;
                    try {
                        int numberOfBodyLines = tournament.numberOfPlayers();
                        numberOfBodyLinesInAPage = (usableHeight / lineHeight) - 5;
                        numberOfPages = (numberOfBodyLines + numberOfBodyLinesInAPage - 1) / numberOfBodyLinesInAPage;
                    } catch (RemoteException ex) {
                        ex.printStackTrace();
                    }
                    break;
                case TYPE_GAMESLIST:
                    nbCarRef = GL_NBCAR;
                    fontSize = usableWidth / nbCarRef * 100 / WH_RATIO * LINEFILLING_RATIO / 100;
                    lineHeight = fontSize * LHFS_RATIO / 100;
                    try {
                        int numberOfBodyLines = tournament.gamesList(roundNumber).size();
                        numberOfBodyLinesInAPage = (usableHeight / lineHeight) - 5;
                        numberOfPages = (numberOfBodyLines + numberOfBodyLinesInAPage - 1) / numberOfBodyLinesInAPage;
                    } catch (RemoteException ex) {
                        ex.printStackTrace();
                    }
                    break;
                case TYPE_STANDINGS:
                    fontSize = usableWidth / this.numberOfCharactersInALine * 100 / WH_RATIO * LINEFILLING_RATIO / 100;
                    lineHeight = fontSize * LHFS_RATIO / 100;
                    numberOfBodyLinesInAPage = (usableHeight / lineHeight) - 5;
                    int numberOfBodyLines = this.alOrderedScoredPlayers.size();
                    numberOfPages = (numberOfBodyLines + numberOfBodyLinesInAPage - 1) / numberOfBodyLinesInAPage;
                    if (this.printSubType == TournamentPrinting.SUBTYPE_ST_CAT) {
                        numberOfPages = 0;
                        for (int numCat = 0; numCat < gps.getNumberOfCategories(); numCat++) {
                            int nbPl = 0;
                            try {
                                nbPl = tournament.numberOfPlayersInCategory(numCat, alOrderedScoredPlayers);
                            } catch (RemoteException ex) {
                                ex.printStackTrace();
                            }
                            numberOfPages += (nbPl + numberOfBodyLinesInAPage - 1) / numberOfBodyLinesInAPage;
                        }
                    }
                    break;
                case TYPE_TOURNAMENT_PARAMETERS:
                    nbCarRef = TP_NBCAR;
                    fontSize = usableWidth / nbCarRef * 100 / WH_RATIO * LINEFILLING_RATIO / 100;
                    lineHeight = fontSize * LHFS_RATIO / 100;
                    numberOfBodyLinesInAPage = (usableHeight / lineHeight) - 5;
                    numberOfPages = 2;
                    break;
            }
        }
        switch(printType) {
            case TYPE_DEFAULT:
                return printADefaultPage(g, pf, pi);
            case TYPE_PLAYERSLIST:
                try {
                    return printAPageOfPlayersList(g, pf, pi);
                } catch (RemoteException ex) {
                    ex.printStackTrace();
                }
                break;
            case TYPE_GAMESLIST:
                try {
                    return printAPageOfGamesList(g, pf, pi);
                } catch (RemoteException ex) {
                    ex.printStackTrace();
                }
                break;
            case TYPE_STANDINGS:
                try {
                    return printAPageOfStandings(g, pf, pi);
                } catch (RemoteException ex) {
                    ex.printStackTrace();
                }
                break;
            case TYPE_TOURNAMENT_PARAMETERS:
                try {
                    return printAPageOfTournamentParameters(g, pf, pi);
                } catch (RemoteException ex) {
                    ex.printStackTrace();
                }
        }
        return PAGE_EXISTS;
    }

    private int printAPageOfPlayersList(Graphics g, PageFormat pf, int pi) throws RemoteException {
        Font font = new Font("Default", Font.BOLD, fontSize);
        g.setFont(font);
        FontMetrics fm = g.getFontMetrics(font);
        String strTitle = "Players List";
        char[] cTitle = strTitle.toCharArray();
        int titleWidth = fm.charsWidth(cTitle, 0, cTitle.length);
        int x = (usableWidth - titleWidth) / 2;
        int y = (usableY + lineHeight);
        g.drawString(strTitle, x, y);
        printPlayersListHeaderLine(g, pf, pi);
        int ln = 0;
        for (ln = 0; ln < numberOfBodyLinesInAPage; ln++) {
            int abstractLineNumber = ln + pi * numberOfBodyLinesInAPage;
            int playerNumber = abstractLineNumber;
            if (playerNumber >= alPlayersToPrint.size()) break;
            Player player = alPlayersToPrint.get(playerNumber);
            y = usableY + (4 + ln) * lineHeight;
            if ((ln % 2) == 0) {
                g.setColor(Color.LIGHT_GRAY);
                g.fillRect(usableX, y - lineHeight + 4, usableWidth, lineHeight);
                g.setColor(Color.BLACK);
            }
            if (player.getRegisteringStatus().compareTo("FIN") == 0) g.setColor(Color.BLACK); else g.setColor(Color.RED);
            String strNumber = "" + (playerNumber + 1);
            x = usableX + usableWidth * (PL_NUMBER_BEG + PL_NUMBER_LEN) / PL_NBCAR;
            drawRightAlignedString(g, strNumber, x, y);
            String strPinLic = player.getEgfPin();
            if (strPinLic.length() == 0) strPinLic = player.getFfgLicence();
            if (strPinLic.length() == 0) strPinLic = "--------";
            x = usableX + usableWidth * PL_PINLIC_BEG / PL_NBCAR;
            Font fontCourier = new Font("Courier New", Font.BOLD, fontSize);
            g.setFont(fontCourier);
            g.drawString(strPinLic, x, y);
            g.setFont(font);
            String strName = player.getName();
            String strFirstName = player.getFirstName();
            if (strName.length() > 20) strName = strName.substring(0, 20);
            String strNF = strName + " " + strFirstName;
            if (strNF.length() > 25) strNF = strNF.substring(0, 25);
            if (player.getRegisteringStatus().compareTo("PRE") == 0) strNF += "(P)";
            x = usableX + usableWidth * PL_NF_BEG / PL_NBCAR;
            g.drawString(strNF, x, y);
            String strRk = Player.convertIntToKD(player.getRank());
            x = usableX + usableWidth * (PL_RANK_BEG + PL_RANK_LEN) / PL_NBCAR;
            drawRightAlignedString(g, strRk, x, y);
            String strMM = "  ";
            strMM = "" + player.smms(tournament.getTournamentParameterSet().getGeneralParameterSet());
            x = usableX + usableWidth * (PL_MM_BEG + PL_MM_LEN) / PL_NBCAR;
            drawRightAlignedString(g, strMM, x, y);
            String strCountry = player.getCountry();
            strCountry = Gotha.leftString(strCountry, 2);
            x = usableX + usableWidth * PL_COUNTRY_BEG / PL_NBCAR;
            g.drawString(strCountry, x, y);
            String strClub = player.getClub();
            strClub = Gotha.leftString(strClub, 4);
            x = usableX + usableWidth * PL_CLUB_BEG / PL_NBCAR;
            g.drawString(strClub, x, y);
            String strPart = Player.convertParticipationToString(player, tournament.getTournamentParameterSet().getGeneralParameterSet().getNumberOfRounds());
            x = usableX + usableWidth * PL_PART_BEG / PL_NBCAR;
            fontCourier = new Font("Courier New", Font.BOLD, fontSize);
            g.setFont(fontCourier);
            g.drawString(strPart, x, y);
            g.setFont(font);
            g.setColor(Color.BLACK);
        }
        printPageFooter(g, pf, pi);
        if (ln == 0) return NO_SUCH_PAGE;
        return PAGE_EXISTS;
    }

    private int printAPageOfGamesList(Graphics g, PageFormat pf, int pi) throws RemoteException {
        Font font = new Font("Default", Font.BOLD, fontSize);
        g.setFont(font);
        FontMetrics fm = g.getFontMetrics(font);
        String strTitle = "Games List";
        char[] cTitle = strTitle.toCharArray();
        int titleWidth = fm.charsWidth(cTitle, 0, cTitle.length);
        int x = (usableWidth - titleWidth) / 2;
        int y = (usableY + lineHeight);
        g.drawString(strTitle, x, y);
        String strRound = "Round";
        strRound += " " + (roundNumber + 1);
        char[] cRound = strRound.toCharArray();
        int roundWidth = fm.charsWidth(cRound, 0, cRound.length);
        x = (usableWidth - roundWidth) / 2;
        y += lineHeight;
        g.drawString(strRound, x, y);
        printGamesListHeaderLine(g, pf, pi);
        ArrayList<Game> alGamesToPrint = new ArrayList<Game>(tournament.gamesList(roundNumber));
        int gamesSortType = GameComparator.TABLE_NUMBER_ORDER;
        GameComparator gameComparator = new GameComparator(gamesSortType);
        Collections.sort(alGamesToPrint, gameComparator);
        int ln = 0;
        for (ln = 0; ln < numberOfBodyLinesInAPage; ln++) {
            int abstractLineNumber = ln + pi * numberOfBodyLinesInAPage;
            int gameNumber = abstractLineNumber;
            if (gameNumber >= alGamesToPrint.size()) break;
            Game game = alGamesToPrint.get(gameNumber);
            y = usableY + (4 + ln) * lineHeight;
            if ((ln % 2) == 0) {
                g.setColor(Color.LIGHT_GRAY);
                g.fillRect(usableX, y - lineHeight + 4, usableWidth, lineHeight);
                g.setColor(Color.BLACK);
            }
            String strTN = "" + (game.getTableNumber() + 1);
            x = usableX + usableWidth * (GL_TN_BEG + GL_TN_LEN) / GL_NBCAR;
            drawRightAlignedString(g, strTN, x, y);
            Player wP = game.getWhitePlayer();
            String strName = wP.getName();
            String strFirstName = wP.getFirstName();
            if (strName.length() > 18) strName = strName.substring(0, 18);
            String strNF = strName + " " + strFirstName;
            if (strNF.length() > 22) strNF = strNF.substring(0, 22);
            String strClub = wP.getClub();
            strClub = Gotha.leftString(strClub, 4);
            strNF += " (" + Player.convertIntToKD(wP.getRank()) + "," + strClub + ")";
            x = usableX + usableWidth * GL_WNF_BEG / GL_NBCAR;
            int result = game.getResult();
            if (result >= Game.RESULT_BYDEF) result -= Game.RESULT_BYDEF;
            if (result == Game.RESULT_BOTHLOSE || result == Game.RESULT_EQUAL || result == Game.RESULT_BLACKWINS) g.setFont(new Font("Default", Font.PLAIN, fontSize));
            g.drawString(strNF, x, y);
            g.setFont(font);
            Player bP = game.getBlackPlayer();
            strName = bP.getName();
            strFirstName = bP.getFirstName();
            if (strName.length() > 18) strName = strName.substring(0, 18);
            strNF = strName + " " + strFirstName;
            if (strNF.length() > 22) strNF = strNF.substring(0, 22);
            strClub = bP.getClub();
            strClub = Gotha.leftString(strClub, 4);
            strNF += " (" + Player.convertIntToKD(bP.getRank()) + "," + strClub + ")";
            x = usableX + usableWidth * GL_BNF_BEG / GL_NBCAR;
            if (result == Game.RESULT_BOTHLOSE || result == Game.RESULT_EQUAL || result == Game.RESULT_WHITEWINS) g.setFont(new Font("Default", Font.PLAIN, fontSize));
            g.drawString(strNF, x, y);
            g.setFont(font);
            String strHd = "" + game.getHandicap();
            x = usableX + usableWidth * (GL_HD_BEG + GL_HD_LEN) / GL_NBCAR;
            drawRightAlignedString(g, strHd, x, y);
            String strResult = game.resultAsString();
            x = usableX + usableWidth * (GL_RES_BEG + GL_RES_LEN) / GL_NBCAR;
            drawRightAlignedString(g, strResult, x, y);
        }
        printPageFooter(g, pf, pi);
        if (ln == 0) return NO_SUCH_PAGE;
        return PAGE_EXISTS;
    }

    private int printAPageOfStandings(Graphics g, PageFormat pf, int pi) throws RemoteException {
        TournamentParameterSet tps = null;
        try {
            tps = tournament.getTournamentParameterSet();
        } catch (RemoteException ex) {
            ex.printStackTrace();
        }
        GeneralParameterSet gps = tps.getGeneralParameterSet();
        Font font = new Font("Default", Font.BOLD, fontSize);
        g.setFont(font);
        FontMetrics fm = g.getFontMetrics(font);
        String strTitle = "Standings after round" + " " + (roundNumber + 1);
        char[] cTitle = strTitle.toCharArray();
        int titleWidth = fm.charsWidth(cTitle, 0, cTitle.length);
        int x = (usableWidth - titleWidth) / 2;
        int y = (usableY + lineHeight);
        g.drawString(strTitle, x, y);
        y += lineHeight;
        int curCat = 0;
        int nbPlayersBeforeCurCat = 0;
        int nbPagesBeforeCurCat = 0;
        int nbPlayersOfCurCat = 0;
        try {
            nbPlayersOfCurCat = tournament.numberOfPlayersInCategory(curCat, alOrderedScoredPlayers);
        } catch (RemoteException ex) {
            ex.printStackTrace();
        }
        if (this.printSubType != TournamentPrinting.SUBTYPE_ST_CAT) nbPlayersOfCurCat = tournament.numberOfPlayers();
        int nbPagesForCurCat = (nbPlayersOfCurCat + numberOfBodyLinesInAPage - 1) / numberOfBodyLinesInAPage;
        while (pi >= nbPagesBeforeCurCat + nbPagesForCurCat) {
            curCat++;
            if (curCat >= gps.getNumberOfCategories()) return NO_SUCH_PAGE;
            nbPlayersBeforeCurCat += nbPlayersOfCurCat;
            nbPagesBeforeCurCat += nbPagesForCurCat;
            try {
                nbPlayersOfCurCat = tournament.numberOfPlayersInCategory(curCat, alOrderedScoredPlayers);
            } catch (RemoteException ex) {
                ex.printStackTrace();
            }
            nbPagesForCurCat = (nbPlayersOfCurCat + numberOfBodyLinesInAPage - 1) / numberOfBodyLinesInAPage;
        }
        if (printCriteria[0] == PlacementParameterSet.PLA_CRIT_CAT) {
            String strCat = "Category" + " " + (curCat + 1);
            char[] cCat = strCat.toCharArray();
            int catWidth = fm.charsWidth(cCat, 0, cCat.length);
            x = (usableWidth - catWidth) / 2;
            g.drawString(strCat, x, y);
        }
        printStandingsHeaderLine(g, pf, pi);
        int ln = 0;
        for (ln = 0; ln < numberOfBodyLinesInAPage; ln++) {
            y = usableY + (4 + ln) * lineHeight;
            int playerNumber = nbPlayersBeforeCurCat + ln + (pi - nbPagesBeforeCurCat) * numberOfBodyLinesInAPage;
            if (playerNumber >= this.alOrderedScoredPlayers.size()) break;
            ScoredPlayer sp = alOrderedScoredPlayers.get(playerNumber);
            if (this.printSubType == TournamentPrinting.SUBTYPE_ST_CAT && sp.category(gps) != curCat) break;
            if ((ln % 2) == 0) {
                g.setColor(Color.LIGHT_GRAY);
                g.fillRect(usableX, y - lineHeight + 4, usableWidth, lineHeight);
                g.setColor(Color.BLACK);
            }
            String strNum = "" + (playerNumber + 1);
            x = usableX + usableWidth * (ST_NUM_BEG + ST_NUM_LEN) / numberOfCharactersInALine;
            drawRightAlignedString(g, strNum, x, y);
            String strPL = strPlace[playerNumber];
            x = usableX + usableWidth * (ST_PL_BEG + ST_PL_LEN) / numberOfCharactersInALine;
            drawRightAlignedString(g, strPL, x, y);
            String strName = sp.getName();
            String strFirstName = sp.getFirstName();
            if (strName.length() > 18) strName = strName.substring(0, 18);
            String strNF = strName + " " + strFirstName;
            if (strNF.length() > 22) strNF = strNF.substring(0, 22);
            x = usableX + usableWidth * (ST_NF_BEG) / numberOfCharactersInALine;
            g.drawString(strNF, x, y);
            String strRk = Player.convertIntToKD(sp.getRank());
            x = usableX + usableWidth * (ST_RK_BEG + ST_RK_LEN) / numberOfCharactersInALine;
            drawRightAlignedString(g, strRk, x, y);
            x = usableX + usableWidth * (ST_NBW_BEG + ST_NBW_LEN) / numberOfCharactersInALine;
            String strNbW = sp.formatScore(PlacementParameterSet.PLA_CRIT_NBW, roundNumber);
            drawRightAlignedString(g, strNbW, x, y);
            int numberOfRoundsPrinted = roundNumber + 1;
            int rBeg = ST_ROUND0_BEG;
            for (int r = 0; r < numberOfRoundsPrinted; r++) {
                int xR = usableX + usableWidth * (rBeg + (r + 1) * ST_ROUND_LEN) / numberOfCharactersInALine;
                drawRightAlignedString(g, this.halfGamesStrings[r][playerNumber], xR, y);
            }
            int numberOfCriteriaPrinted = printCriteria.length;
            int cBeg = rBeg + numberOfRoundsPrinted * ST_ROUND_LEN;
            for (int iC = 0; iC < numberOfCriteriaPrinted; iC++) {
                int xC = usableX + usableWidth * (cBeg + (iC + 1) * ST_CRIT_LEN) / numberOfCharactersInALine;
                String strCritValue = sp.formatScore(printCriteria[iC], roundNumber);
                PlacementParameterSet.criterionShortName(printCriteria[iC]);
                drawRightAlignedString(g, strCritValue, xC, y);
            }
        }
        printPageFooter(g, pf, pi);
        if (ln == 0) return NO_SUCH_PAGE;
        return PAGE_EXISTS;
    }

    private int printAPageOfTournamentParameters(Graphics g, PageFormat pf, int pi) throws RemoteException {
        if (pi > 1) return NO_SUCH_PAGE;
        Font font = new Font("Default", Font.BOLD, fontSize);
        g.setFont(font);
        FontMetrics fm = g.getFontMetrics(font);
        String strTitle = "Tournament Parameters";
        char[] cTitle = strTitle.toCharArray();
        int titleWidth = fm.charsWidth(cTitle, 0, cTitle.length);
        int x = (usableWidth - titleWidth) / 2;
        int y = (usableY + lineHeight);
        g.drawString(strTitle, x, y);
        TournamentParameterSet tps = tournament.getTournamentParameterSet();
        GeneralParameterSet gps = tps.getGeneralParameterSet();
        HandicapParameterSet hps = tps.getHandicapParameterSet();
        PlacementParameterSet pps = tps.getPlacementParameterSet();
        PairingParameterSet paiPS = tps.getPairingParameterSet();
        if (pi == 0) {
            int ln = 0;
            x = usableX;
            ln = 0;
            y = usableY + (4 + ln) * lineHeight;
            Font title2Font = new Font("Default", Font.BOLD, fontSize + 4);
            g.setFont(title2Font);
            g.drawString("General Parameters", x, y);
            ln++;
            g.setFont(font);
            ln++;
            y = usableY + (4 + ln) * lineHeight;
            g.drawString(gps.getName(), x, y);
            ln++;
            y = usableY + (4 + ln) * lineHeight;
            int tournamentType = tournament.tournamentType();
            String strType = "Undefined system";
            if (tournamentType == TournamentParameterSet.TYPE_MACMAHON) strType = "Mac-Mahon system";
            if (tournamentType == TournamentParameterSet.TYPE_SWISS) strType = "Swiss system";
            if (tournamentType == TournamentParameterSet.TYPE_SWISSCAT) strType = "SwissCat system";
            g.drawString(strType, x, y);
            ln++;
            y = usableY + (4 + ln) * lineHeight;
            g.drawString(gps.getNumberOfRounds() + " " + "rounds", x, y);
            ln++;
            y = usableY + (4 + ln) * lineHeight;
            g.drawString("Special Results", x, y);
            int xNBW = usableX + usableWidth * TP_TAB3 / TP_NBCAR;
            this.drawRightAlignedString(g, "NBW", xNBW, y);
            int xMMS = usableX + usableWidth * TP_TAB4 / TP_NBCAR;
            this.drawRightAlignedString(g, "MMS", xMMS, y);
            ln++;
            y = usableY + (4 + ln) * lineHeight;
            g.drawString("Absent", usableX + usableWidth * TP_TAB1 / TP_NBCAR, y);
            String strNBW = "";
            switch(gps.getGenNBW2ValueAbsent()) {
                case 0:
                    strNBW = "0";
                    break;
                case 1:
                    strNBW = "�";
                    break;
                case 2:
                    strNBW = "1";
                    break;
            }
            String strMMS = "";
            switch(gps.getGenMMS2ValueAbsent()) {
                case 0:
                    strMMS = "0";
                    break;
                case 1:
                    strMMS = "�";
                    break;
                case 2:
                    strMMS = "1";
                    break;
            }
            this.drawRightAlignedString(g, strNBW, xNBW, y);
            this.drawRightAlignedString(g, strMMS, xMMS, y);
            ln++;
            y = usableY + (4 + ln) * lineHeight;
            g.drawString("Bye", usableX + usableWidth * TP_TAB1 / TP_NBCAR, y);
            strNBW = "";
            switch(gps.getGenNBW2ValueBye()) {
                case 0:
                    strNBW = "0";
                    break;
                case 1:
                    strNBW = "�";
                    break;
                case 2:
                    strNBW = "1";
                    break;
            }
            strMMS = "";
            switch(gps.getGenMMS2ValueBye()) {
                case 0:
                    strMMS = "0";
                    break;
                case 1:
                    strMMS = "�";
                    break;
                case 2:
                    strMMS = "1";
                    break;
            }
            this.drawRightAlignedString(g, strNBW, xNBW, y);
            this.drawRightAlignedString(g, strMMS, xMMS, y);
            if (gps.getNumberOfCategories() > 1) {
                ln++;
                for (int c = 0; c < gps.getNumberOfCategories(); c++) {
                    ln++;
                    y = usableY + (4 + ln) * lineHeight;
                    int lowL = Gotha.MIN_RANK;
                    int highL = Gotha.MAX_RANK;
                    if (c <= gps.getNumberOfCategories() - 2) lowL = gps.getLowerCategoryLimits()[c];
                    if (c >= 1) highL = gps.getLowerCategoryLimits()[c - 1] - 1;
                    String strLow = Player.convertIntToKD(lowL);
                    String strHigh = Player.convertIntToKD(highL);
                    g.drawString("Category" + (c + 1) + " : " + strHigh + " - " + strLow, x, y);
                }
            }
            ln++;
            x = usableX;
            ln++;
            ln++;
            y = usableY + (4 + ln) * lineHeight;
            title2Font = new Font("Default", Font.BOLD, fontSize + 4);
            g.setFont(title2Font);
            g.drawString("Handicap Parameters", x, y);
            ln++;
            g.setFont(font);
            ln++;
            y = usableY + (4 + ln) * lineHeight;
            String strRankThreshold = Player.convertIntToKD(hps.getHdNoHdRankThreshold());
            g.drawString("No handicap for players above" + strRankThreshold, x, y);
            ln++;
            y = usableY + (4 + ln) * lineHeight;
            String strHdCorr = "";
            if (hps.getHdCorrection() == 0) strHdCorr = "Handicap not decreased"; else strHdCorr = "Handicap decreased by" + " " + hps.getHdCorrection();
            g.drawString(strHdCorr, x, y);
            ln++;
            y = usableY + (4 + ln) * lineHeight;
            String strHdCeil = "";
            strHdCeil = "Handicap ceiling" + " : " + hps.getHdCeiling();
            g.drawString(strHdCeil, x, y);
            x = usableX;
            ln++;
            ln++;
            y = usableY + (4 + ln) * lineHeight;
            title2Font = new Font("Default", Font.BOLD, fontSize + 4);
            g.setFont(title2Font);
            g.drawString("Placement Parameters", x, y);
            ln++;
            g.setFont(font);
            int[] plaC = pps.getPlaCriteria();
            int nbCrit = plaC.length;
            for (int c = nbCrit - 1; c > 0; c--) {
                if (plaC[c] == PlacementParameterSet.PLA_CRIT_NUL) nbCrit--; else break;
            }
            int[] plaCrit = new int[nbCrit];
            for (int c = 0; c < nbCrit; c++) plaCrit[c] = plaC[c];
            for (int crit = 0; crit < plaCrit.length; crit++) {
                ln++;
                y = usableY + (4 + ln) * lineHeight;
                String strCrit = PlacementParameterSet.criterionLongName(plaCrit[crit]);
                g.drawString("Criterion" + (crit + 1) + " : " + strCrit, x, y);
            }
        }
        int ln = 0;
        if (pi == 1) {
            x = usableX;
            ln = 0;
            y = usableY + (4 + ln) * lineHeight;
            Font title2Font = new Font("Default", Font.BOLD, fontSize + 4);
            g.setFont(title2Font);
            g.drawString("Pairing Parameters", x, y);
            ln++;
            g.setFont(font);
            ln++;
            y = usableY + (4 + ln) * lineHeight;
            String strSS = "Seeding system";
            int lastRSS1 = paiPS.getPaiMaLastRoundForSeedSystem1();
            if (lastRSS1 < gps.getNumberOfRounds() - 1) {
                strSS += " " + "until Round" + (lastRSS1 + 1);
            }
            strSS += " : ";
            if (paiPS.getPaiMaSeedSystem1() == PairingParameterSet.PAIMA_SEED_SPLITANDRANDOM) strSS += "Split and Random"; else if (paiPS.getPaiMaSeedSystem1() == PairingParameterSet.PAIMA_SEED_SPLITANDFOLD) strSS += "Split and Fold"; else if (paiPS.getPaiMaSeedSystem1() == PairingParameterSet.PAIMA_SEED_SPLITANDSLIP) strSS += "Split and Slip";
            if (paiPS.getPaiMaAdditionalPlacementCritSystem1() != PlacementParameterSet.PLA_CRIT_NUL) strSS += " " + "with additional criterion on" + " " + PlacementParameterSet.criterionLongName(paiPS.getPaiMaAdditionalPlacementCritSystem1());
            g.drawString(strSS, x, y);
            if (lastRSS1 < gps.getNumberOfRounds() - 1) {
                ln++;
                y = usableY + (4 + ln) * lineHeight;
                String strSS2 = "Seeding system starting from Round" + (lastRSS1 + 2) + " : ";
                if (paiPS.getPaiMaSeedSystem2() == PairingParameterSet.PAIMA_SEED_SPLITANDRANDOM) strSS2 += "Split and Random"; else if (paiPS.getPaiMaSeedSystem2() == PairingParameterSet.PAIMA_SEED_SPLITANDFOLD) strSS2 += "Split and Fold"; else if (paiPS.getPaiMaSeedSystem2() == PairingParameterSet.PAIMA_SEED_SPLITANDSLIP) strSS2 += "Split and Slip";
                if (paiPS.getPaiMaAdditionalPlacementCritSystem2() != PlacementParameterSet.PLA_CRIT_NUL) strSS2 += "with_additional_criterion_on" + PlacementParameterSet.criterionLongName(paiPS.getPaiMaAdditionalPlacementCritSystem2());
                g.drawString(strSS2, x, y);
            }
            ln++;
            y = usableY + (4 + ln) * lineHeight;
            String strDG = "When pairing players from different groups is necessary,";
            g.drawString(strDG, x, y);
            int x1 = usableX + usableWidth * TP_TAB1 / TP_NBCAR;
            ln++;
            y = usableY + (4 + ln) * lineHeight;
            String strUG = "The player in the upper group is chosen";
            if (paiPS.getPaiMaDUDDUpperMode() == PairingParameterSet.PAIMA_DUDD_TOP) strUG += " " + "in the top of the group";
            if (paiPS.getPaiMaDUDDUpperMode() == PairingParameterSet.PAIMA_DUDD_MID) strUG += " " + "in the middle of the group";
            if (paiPS.getPaiMaDUDDUpperMode() == PairingParameterSet.PAIMA_DUDD_BOT) strUG += " " + "in the bottom of the group";
            g.drawString(strUG, x1, y);
            ln++;
            y = usableY + (4 + ln) * lineHeight;
            String strLG = "The player in the lower group is chosen";
            if (paiPS.getPaiMaDUDDLowerMode() == PairingParameterSet.PAIMA_DUDD_TOP) strLG += " " + "in the top of the group";
            if (paiPS.getPaiMaDUDDLowerMode() == PairingParameterSet.PAIMA_DUDD_MID) strLG += " " + "in the middle of the group";
            if (paiPS.getPaiMaDUDDLowerMode() == PairingParameterSet.PAIMA_DUDD_BOT) strLG += " " + "in the bottom of the group";
            g.drawString(strLG, x1, y);
            ln++;
            y = usableY + (4 + ln) * lineHeight;
            String strRan = "";
            if (paiPS.getPaiBaRandom() == 0) strRan = "No random"; else if (paiPS.isPaiBaDeterministic()) strRan = "Some deterministic random"; else strRan = "Some non-deterministic random";
            g.drawString(strRan, x, y);
            ln++;
            y = usableY + (4 + ln) * lineHeight;
            String strBalWB = "";
            if (paiPS.getPaiBaBalanceWB() != 0) strBalWB = "Balance White and Black";
            g.drawString(strBalWB, x, y);
            ln++;
            ln++;
            y = usableY + (4 + ln) * lineHeight;
            g.drawString("Secondary criteria", x, y);
            ln++;
            y = usableY + (4 + ln) * lineHeight;
            String strRankThreshold = Player.convertIntToKD(paiPS.getPaiSeRankThreshold());
            g.drawString("Secondary criteria not applied for players with a rank equal or stronger than" + strRankThreshold, usableX + usableWidth * TP_TAB1 / TP_NBCAR, y);
            if (paiPS.isPaiSeNbWinsThresholdActive()) {
                ln++;
                y = usableY + (4 + ln) * lineHeight;
                g.drawString("Secondary criteria not applied for players with at least nbRounds/2 wins", usableX + usableWidth * TP_TAB1 / TP_NBCAR, y);
            }
            ln++;
            y = usableY + (4 + ln) * lineHeight;
            g.drawString("Intra-country pairing is avoided. A group gap of" + " " + paiPS.getPaiSePreferMMSDiffRatherThanSameCountry() + " " + "is preferred", usableX + usableWidth * TP_TAB1 / TP_NBCAR, y);
            ln++;
            y = usableY + (4 + ln) * lineHeight;
            g.drawString("Intra-club pairing is avoided. A group gap of" + " " + paiPS.getPaiSePreferMMSDiffRatherThanSameClub() + " " + "is preferred", usableX + usableWidth * TP_TAB1 / TP_NBCAR, y);
            if (paiPS.getPaiSeMinimizeHandicap() != 0) {
                ln++;
                y = usableY + (4 + ln) * lineHeight;
                g.drawString("Low handicap games are preferred", usableX + usableWidth * TP_TAB1 / TP_NBCAR, y);
            }
        }
        printPageFooter(g, pf, pi);
        return PAGE_EXISTS;
    }

    private void printPlayersListHeaderLine(Graphics g, PageFormat pf, int pi) {
        int y = usableY + 3 * lineHeight;
        int x = usableX + usableWidth * PL_PINLIC_BEG / PL_NBCAR;
        g.drawString("PIN/LIC", x, y);
        x = usableX + usableWidth * PL_NF_BEG / PL_NBCAR;
        g.drawString("Name" + "    " + "First name", x, y);
        x = usableX + usableWidth * (PL_RANK_BEG + PL_RANK_LEN) / PL_NBCAR;
        drawRightAlignedString(g, "Rk", x, y);
        x = usableX + usableWidth * (PL_MM_BEG + PL_MM_LEN) / PL_NBCAR;
        drawRightAlignedString(g, "MM", x, y);
        x = usableX + usableWidth * PL_COUNTRY_BEG / PL_NBCAR;
        g.drawString("Co", x, y);
        x = usableX + usableWidth * PL_CLUB_BEG / PL_NBCAR;
        g.drawString("Club", x, y);
        x = usableX + usableWidth * PL_PART_BEG / PL_NBCAR;
        g.drawString("Participation", x, y);
    }

    private void printGamesListHeaderLine(Graphics g, PageFormat pf, int pi) {
        int y = usableY + 3 * lineHeight;
        int x = usableX + usableWidth * (GL_TN_BEG + GL_TN_LEN) / GL_NBCAR;
        drawRightAlignedString(g, "Tble", x, y);
        x = usableX + usableWidth * GL_WNF_BEG / GL_NBCAR;
        g.drawString("White", x, y);
        x = usableX + usableWidth * GL_BNF_BEG / GL_NBCAR;
        g.drawString("Black", x, y);
        x = usableX + usableWidth * (GL_HD_BEG + GL_HD_LEN) / GL_NBCAR;
        drawRightAlignedString(g, "Hd", x, y);
        x = usableX + usableWidth * (GL_RES_BEG + GL_RES_LEN) / GL_NBCAR;
        drawRightAlignedString(g, "Res", x, y);
    }

    private void printStandingsHeaderLine(Graphics g, PageFormat pf, int pi) {
        int y = usableY + 3 * lineHeight;
        int x = usableX + usableWidth * (ST_NUM_BEG + ST_NUM_LEN) / numberOfCharactersInALine;
        this.drawRightAlignedString(g, "Num", x, y);
        x = usableX + usableWidth * (ST_PL_BEG + ST_PL_LEN) / numberOfCharactersInALine;
        this.drawRightAlignedString(g, "Pl", x, y);
        x = usableX + usableWidth * this.ST_NF_BEG / this.numberOfCharactersInALine;
        g.drawString("Name", x, y);
        x = usableX + usableWidth * (this.ST_RK_BEG + this.ST_RK_LEN) / this.numberOfCharactersInALine;
        this.drawRightAlignedString(g, "Rk", x, y);
        x = usableX + usableWidth * (this.ST_NBW_BEG + this.ST_NBW_LEN) / this.numberOfCharactersInALine;
        this.drawRightAlignedString(g, "NBW", x, y);
        int numberOfRoundsPrinted = roundNumber + 1;
        int rBeg = ST_ROUND0_BEG;
        for (int r = 0; r < numberOfRoundsPrinted; r++) {
            int xR = usableX + usableWidth * (rBeg + (r + 1) * ST_ROUND_LEN) / numberOfCharactersInALine;
            String strRound = "R" + (r + 1);
            this.drawRightAlignedString(g, strRound, xR, y);
        }
        int numberOfCriteriaPrinted = printCriteria.length;
        int cBeg = rBeg + numberOfRoundsPrinted * ST_ROUND_LEN;
        for (int iC = 0; iC < numberOfCriteriaPrinted; iC++) {
            int xC = usableX + usableWidth * (cBeg + (iC + 1) * ST_CRIT_LEN) / numberOfCharactersInALine;
            String strCrit = PlacementParameterSet.criterionShortName(printCriteria[iC]);
            this.drawRightAlignedString(g, strCrit, xC, y);
        }
    }

    private void printPageFooter(Graphics g, PageFormat pf, int pi) {
        Font f = new Font("Default", Font.BOLD, fontSize);
        g.setFont(f);
        FontMetrics fm = g.getFontMetrics();
        String strName = "";
        try {
            strName = tournament.getTournamentParameterSet().getGeneralParameterSet().getName();
        } catch (RemoteException ex) {
            ex.printStackTrace();
        }
        String strLeft = Gotha.getGothaVersionnedName() + " : " + strName + "    ";
        String strCenter = "Page" + " " + (pi + 1) + "/" + numberOfPages;
        char[] tcLeft = strLeft.toCharArray();
        char[] tcCenter = strCenter.toCharArray();
        int wLeft = fm.charsWidth(tcLeft, 0, tcLeft.length);
        int wCenter = fm.charsWidth(tcCenter, 0, tcCenter.length);
        while (wLeft + wCenter / 2 > usableWidth / 2) {
            if (strLeft.length() <= 2) break;
            strLeft = strLeft.substring(0, strLeft.length() - 2);
            tcLeft = strLeft.toCharArray();
            wLeft = fm.charsWidth(tcLeft, 0, tcLeft.length);
        }
        strLeft = strLeft.substring(0, strLeft.length() - 2);
        g.drawString(strLeft, usableX, usableY + usableHeight - fm.getDescent());
        int x = usableX + (usableWidth - wCenter) / 2;
        g.drawString(strCenter, x, usableY + usableHeight - fm.getDescent());
        java.util.Date dh = new java.util.Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm  ");
        String strDH = sdf.format(dh);
        String strRight = strDH;
        x = usableX + usableWidth;
        drawRightAlignedString(g, strRight, x, usableY + usableHeight - fm.getDescent());
    }

    private int printADefaultPage(Graphics g, PageFormat pf, int pi) {
        if (pi > 0) return NO_SUCH_PAGE;
        g.drawString("This page is printed for test only", usableX, usableY + 20);
        g.drawString("uX = " + usableX, 10, 100);
        g.drawString("uY = " + usableY, 10, 120);
        g.drawString("uW = " + usableWidth, 10, 140);
        g.drawString("uH = " + usableHeight, 10, 160);
        g.drawRect(usableX, usableY, usableWidth - 1, usableHeight - 1);
        g.setFont(new Font("Default", Font.PLAIN, 40));
        g.drawString("Font size = 40", usableX, usableY + 260);
        g.setFont(new Font("Default", Font.PLAIN, 18));
        g.drawString("Font size = 18", usableX, usableY + 360);
        g.setFont(new Font("Default", Font.PLAIN, 16));
        g.drawString("Font size = 12", usableX, usableY + 420);
        g.setFont(new Font("Default", Font.PLAIN, 7));
        g.drawString("Font size =  7", usableX, usableY + 500);
        g.setFont(new Font("Default", Font.PLAIN, 6));
        g.drawString("Font size =  5", usableX, usableY + 540);
        g.setFont(new Font("Default", Font.PLAIN, 4));
        g.drawString("Font size =  3", usableX, usableY + 580);
        g.setFont(new Font("Default", Font.PLAIN, 2));
        Font f = new Font("Default", Font.PLAIN, 100);
        FontMetrics fm = g.getFontMetrics(f);
        g.setFont(new Font("Default", Font.PLAIN, 12));
        g.drawString("Font size = 100", usableX + 120, usableY + 380);
        g.drawString("Leading = " + fm.getLeading(), usableX + 120, usableY + 400);
        g.drawString("Ascent = " + fm.getAscent(), usableX + 120, usableY + 420);
        g.drawString("MaxAscent = " + fm.getMaxAscent(), usableX + 120, usableY + 440);
        g.drawString("Descent = " + fm.getDescent(), usableX + 120, usableY + 460);
        g.drawString("MaxDescent = " + fm.getMaxDescent(), usableX + 120, usableY + 480);
        g.drawString("Height = " + fm.getHeight(), usableX + 120, usableY + 500);
        char[] tci = { 'i', 'i', 'i', 'i', 'i', 'i', 'i', 'i', 'i', 'i' };
        g.drawString("charsWidth(\"i\") = " + fm.charsWidth(tci, 0, 1), usableX + 120, usableY + 520);
        g.drawString("charsWidth(\"iiiiiiiiii\") = " + fm.charsWidth(tci, 0, 10), usableX + 120, usableY + 540);
        char[] tcw = { '�' };
        g.drawString("charsWidth(\"�\") = " + fm.charsWidth(tcw, 0, 1), usableX + 120, usableY + 560);
        g.drawLine(100, 100, 500, 100);
        g.drawLine(100, 100 + fm.getLeading(), 500, 100 + fm.getLeading());
        g.drawLine(100, 100 + fm.getLeading() + fm.getAscent(), 500, 100 + fm.getLeading() + fm.getAscent());
        g.drawLine(100, 100 + fm.getLeading() + fm.getAscent() + fm.getDescent(), 500, 100 + fm.getLeading() + fm.getAscent() + fm.getDescent());
        g.setFont(f);
        g.drawString("a�_����", 50, 100 + fm.getLeading() + fm.getAscent());
        return PAGE_EXISTS;
    }

    private static void drawRightAlignedString(Graphics g, String str, int x, int y) {
        FontMetrics fm = g.getFontMetrics();
        char[] tc = str.toCharArray();
        int w = fm.charsWidth(tc, 0, str.length());
        int xLeft = x - w;
        g.drawString(str, xLeft, y);
    }

    public int getRoundNumber() {
        return roundNumber;
    }

    public void setRoundNumber(int roundNumber) {
        this.roundNumber = roundNumber;
    }

    public void setAlOrderedScoredPlayers(ArrayList<ScoredPlayer> alOrderedScoredPlayers) {
        this.alOrderedScoredPlayers = alOrderedScoredPlayers;
    }

    public int[] getPrintCriteria() {
        return printCriteria;
    }

    public void setPrintCriteria(int[] printCriteria) {
        this.printCriteria = printCriteria;
    }
}
