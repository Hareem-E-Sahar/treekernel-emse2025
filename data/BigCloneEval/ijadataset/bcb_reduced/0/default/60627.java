import java.awt.AWTException;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.image.BufferedImage;
import java.awt.image.PixelGrabber;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

public class enJeu extends Thread {

    boolean[] play;

    Robot bot;

    int i, j, id, dealer, nbplay;

    double valeurPot, blind, blind2;

    double[] bankroll;

    Card[] mesCartes;

    Card[] flop;

    Card turn, river;

    Player[] players;

    boolean amPlaying;

    int moment;

    public enJeu(int i, int j, int id, double b1, double b2) {
        this.i = i;
        this.j = j;
        this.id = id;
        this.play = new boolean[9];
        this.nbplay = 0;
        this.dealer = -1;
        this.valeurPot = 0;
        this.bankroll = new double[9];
        this.mesCartes = new Card[2];
        this.flop = new Card[3];
        this.turn = new Card();
        this.river = new Card();
        this.players = new Player[9];
        this.blind = b1;
        this.blind2 = b2;
        this.moment = -2;
        this.players[1] = new Player();
        this.players[2] = new Player();
        this.players[3] = new Player();
        this.players[4] = new Player();
        this.players[5] = new Player();
        this.players[6] = new Player();
        this.players[7] = new Player();
        this.players[8] = new Player();
        try {
            this.bot = new Robot();
        } catch (AWTException e) {
            e.printStackTrace();
        }
    }

    public void run() {
        playerS p1 = new playerS(1);
        playerS p2 = new playerS(2);
        playerS p3 = new playerS(3);
        playerS p4 = new playerS(4);
        playerS p5 = new playerS(5);
        playerS p6 = new playerS(6);
        playerS p7 = new playerS(7);
        playerS p8 = new playerS(8);
        for (int temp = 0; temp < 9; temp++) {
            play[temp] = true;
        }
        p1.start();
        p2.start();
        p3.start();
        p4.start();
        p5.start();
        p6.start();
        p7.start();
        p8.start();
        this.checkDealer();
        int a = this.dealer;
        while (this.dealer == a) {
            this.checkDealer();
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        System.out.println("1�re main");
        while (true) {
            a = this.dealer;
            this.valeurPot = 0;
            System.out.println("Nouvelle main");
            while (a == this.dealer) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                this.checkPlayers();
            }
        }
    }

    public void checkPlayers() {
        this.nbplay = 0;
        int[] pix = new int[152154];
        BufferedImage image1 = this.bot.createScreenCapture(new Rectangle(i, j, 474, 321));
        PixelGrabber pg = new PixelGrabber(image1, 0, 0, 474, 321, pix, 0, 474);
        try {
            pg.grabPixels();
        } catch (InterruptedException d) {
            System.err.println("en attente des pixels");
        }
        if ((pix[474 * 85 + 121] >> 8 & 0xFF) < 120 || (pix[474 * 85 + 121] >> 16 & 0xFF) > 120) {
            this.play[1] = true;
            this.nbplay++;
        } else {
            this.play[1] = false;
        }
        if ((pix[474 * 69 + 158] >> 8 & 0xFF) < 120 || (pix[474 * 69 + 158] >> 16 & 0xFF) > 80) {
            this.play[2] = true;
            this.nbplay++;
        } else {
            this.play[2] = false;
        }
        if ((pix[474 * 69 + 336] >> 8 & 0xFF) < 120 || (pix[474 * 69 + 336] >> 16 & 0xFF) > 80) {
            this.play[3] = true;
            this.nbplay++;
        } else {
            this.play[3] = false;
        }
        if ((pix[474 * 86 + 374] >> 8 & 0xFF) < 120 || (pix[474 * 86 + 374] >> 16 & 0xFF) > 80) {
            this.play[4] = true;
            this.nbplay++;
        } else {
            this.play[4] = false;
        }
        if ((pix[474 * 140 + 418] >> 8 & 0xFF) < 120 || (pix[474 * 140 + 418] >> 16 & 0xFF) > 80) {
            this.play[5] = true;
            this.nbplay++;
        } else {
            this.play[5] = false;
        }
        if ((pix[474 * 184 + 368] >> 8 & 0xFF) < 120 || (pix[474 * 184 + 368] >> 16 & 0xFF) > 80) {
            this.play[6] = true;
            this.nbplay++;
        } else {
            this.play[6] = false;
        }
        if ((pix[474 * 205 + 266] >> 8 & 0xFF) < 120 || (pix[474 * 205 + 266] >> 16 & 0xFF) > 80) {
            this.play[7] = true;
            this.nbplay++;
        } else {
            this.play[7] = false;
        }
        if ((pix[474 * 183 + 118] >> 8 & 0xFF) < 120 || (pix[474 * 183 + 118] >> 16 & 0xFF) > 80) {
            this.play[8] = true;
            this.nbplay++;
        } else {
            this.play[8] = false;
        }
    }

    public double OCR(int abscisseA, int ordonneeA, int abscisseB, int[] pix, int couleurPixel) {
        String valeur = "0";
        for (int debut = abscisseA; debut <= abscisseB - 3; debut++) {
            if (pix[474 * ordonneeA + debut] == couleurPixel && pix[474 * (ordonneeA + 1) + debut] == couleurPixel && pix[474 * (ordonneeA + 2) + debut] == couleurPixel && pix[474 * (ordonneeA + 3) + debut] == couleurPixel && pix[474 * (ordonneeA + 1) + debut - 1] == couleurPixel) {
                if (pix[474 * (ordonneeA + 4) + debut + 1] != couleurPixel) {
                    valeur += "1";
                } else if (pix[474 * (ordonneeA + 4) + debut - 2] == couleurPixel) {
                    valeur += "4";
                }
            } else if (pix[474 * (ordonneeA + 1) + debut] == couleurPixel && pix[474 * (ordonneeA + 4) + debut] == couleurPixel && pix[474 * (ordonneeA + 1) + debut + 3] == couleurPixel) {
                if (pix[474 * (ordonneeA + 2) + debut + 3] == couleurPixel) {
                    if (pix[474 * (ordonneeA + 2) + debut + 1] == couleurPixel) {
                        valeur += "9";
                    } else if (pix[474 * (ordonneeA + 3) + debut + 2] == couleurPixel && pix[474 * (ordonneeA + 3) + debut + 2] == couleurPixel && pix[474 * (ordonneeA + 5) + debut + 1] == couleurPixel) {
                        valeur += "2";
                    } else if (pix[474 * (ordonneeA + 2) + debut] == couleurPixel && pix[474 * (ordonneeA + 3) + debut] == couleurPixel && pix[474 * (ordonneeA + 4) + debut] == couleurPixel && pix[474 * (ordonneeA + 5) + debut + 1] == couleurPixel && pix[474 * (ordonneeA + 5) + debut + 2] == couleurPixel) {
                        valeur += "0";
                    }
                } else if (pix[474 * (ordonneeA + 3) + debut] == couleurPixel) {
                    valeur += "8";
                } else {
                    valeur += "3";
                }
            } else if (pix[474 * (ordonneeA) + debut] == couleurPixel && pix[474 * (ordonneeA) + debut + 1] == couleurPixel && pix[474 * (ordonneeA) + debut + 2] == couleurPixel) {
                if (pix[474 * (ordonneeA + 1) + debut + 2] == couleurPixel && pix[474 * (ordonneeA + 2) + debut + 1] == couleurPixel && pix[474 * (ordonneeA + 3) + debut + 1] == couleurPixel) {
                    valeur += "7";
                } else if (pix[474 * (ordonneeA + 3) + debut + 2] == couleurPixel && pix[474 * (ordonneeA + 4) + debut + 2] == couleurPixel && pix[474 * (ordonneeA + 2) + debut + 1] == couleurPixel) {
                    valeur += "5";
                }
            } else if (pix[474 * (ordonneeA) + debut + 2] == couleurPixel && pix[474 * (ordonneeA) + debut + 1] == couleurPixel && pix[474 * (ordonneeA + 1) + debut] == couleurPixel && pix[474 * (ordonneeA + 3) + debut + 3] == couleurPixel && pix[474 * (ordonneeA + 5) + debut + 2] == couleurPixel) {
                valeur += "6";
            } else if (pix[474 * (ordonneeA + 5) + debut] == couleurPixel && pix[474 * (ordonneeA + 5) + debut - 1] != couleurPixel && pix[474 * (ordonneeA + 5) + debut + 1] != couleurPixel && pix[474 * (ordonneeA + 4) + debut] != couleurPixel && pix[474 * (ordonneeA + 6) + debut] != couleurPixel && pix[474 * (ordonneeA + 3) + debut] != couleurPixel && pix[474 * (ordonneeA + 2) + debut] != couleurPixel) {
                valeur += ".";
            }
        }
        double valeurFinale = Double.parseDouble(valeur);
        return valeurFinale;
    }

    public void checkDealer() {
        int[] pix = new int[152154];
        BufferedImage image1 = this.bot.createScreenCapture(new Rectangle(i, j, 474, 321));
        PixelGrabber pg = new PixelGrabber(image1, 0, 0, 474, 321, pix, 0, 474);
        try {
            pg.grabPixels();
        } catch (InterruptedException d) {
            System.err.println("en attente des pixels");
        }
        if ((pix[474 * 99 + 84] >> 8 & 0xFF) < 120 || (pix[474 * 99 + 84] >> 16 & 0xFF) > 80) {
            this.dealer = 1;
        } else if ((pix[474 * 65 + 171] >> 8 & 0xFF) < 120 || (pix[474 * 65 + 171] >> 16 & 0xFF) > 80) {
            this.dealer = 2;
        } else if ((pix[474 * 65 + 315] >> 8 & 0xFF) < 120 || (pix[474 * 65 + 315] >> 16 & 0xFF) > 80) {
            this.dealer = 3;
        } else if ((pix[474 * 99 + 400] >> 8 & 0xFF) < 120 || (pix[474 * 99 + 400] >> 16 & 0xFF) > 80) {
            this.dealer = 4;
        } else if ((pix[474 * 156 + 412] >> 8 & 0xFF) < 120 || (pix[474 * 156 + 412] >> 16 & 0xFF) > 80) {
            this.dealer = 5;
        } else if ((pix[474 * 200 + 338] >> 8 & 0xFF) < 120 || (pix[474 * 200 + 338] >> 16 & 0xFF) > 80) {
            this.dealer = 6;
        } else if ((pix[474 * 201 + 237] >> 8 & 0xFF) < 120 || (pix[474 * 201 + 237] >> 16 & 0xFF) > 80) {
            this.dealer = 7;
        } else if ((pix[474 * 200 + 148] >> 8 & 0xFF) < 120 || (pix[474 * 200 + 148] >> 16 & 0xFF) > 80) {
            this.dealer = 8;
        } else {
            this.dealer = 0;
        }
    }

    public void checkPot() {
        int[] pix = new int[152154];
        BufferedImage image1 = this.bot.createScreenCapture(new Rectangle(i, j, 474, 321));
        PixelGrabber pg = new PixelGrabber(image1, 0, 0, 474, 321, pix, 0, 474);
        try {
            pg.grabPixels();
        } catch (InterruptedException d) {
            System.err.println("en attente des pixels");
        }
        this.valeurPot = OCR(237, 12, 265, pix, -16777216);
    }

    public void checkBankroll(int joueur) {
        this.bankroll[joueur] = 0;
        while (this.bankroll[joueur] == 0) {
            int[] pix = new int[152154];
            BufferedImage image1 = this.bot.createScreenCapture(new Rectangle(i, j, 474, 321));
            PixelGrabber pg = new PixelGrabber(image1, 0, 0, 474, 321, pix, 0, 474);
            try {
                pg.grabPixels();
            } catch (InterruptedException d) {
                System.err.println("en attente des pixels");
            }
            switch(joueur) {
                case 0:
                    this.bankroll[0] = OCR(10, 177, 60, pix, -4144960);
                    break;
                case 1:
                    this.bankroll[1] = OCR(9, 65, 59, pix, -4144960);
                    break;
                case 2:
                    this.bankroll[2] = OCR(95, 32, 145, pix, -4144960);
                    break;
                case 3:
                    this.bankroll[3] = OCR(340, 32, 390, pix, -4144960);
                    break;
                case 4:
                    this.bankroll[4] = OCR(420, 65, 470, pix, -4144960);
                    break;
                case 5:
                    this.bankroll[5] = OCR(417, 176, 467, pix, -4144960);
                    break;
                case 6:
                    this.bankroll[6] = OCR(290, 214, 340, pix, -4144960);
                    break;
                case 7:
                    this.bankroll[7] = OCR(210, 260, 260, pix, -4144960);
                    break;
                case 8:
                    this.bankroll[8] = OCR(140, 214, 190, pix, -4144960);
                    break;
                default:
                    Main.logger.info("Impossible de r�aliser le chekBankroll pour le joueur " + joueur);
                    break;
            }
        }
    }

    public void checkCards() {
        char couleur = 'o';
        this.mesCartes[0] = new Card();
        this.mesCartes[1] = new Card();
        int[] pix = new int[54];
        BufferedImage image1 = this.bot.createScreenCapture(new Rectangle(i + 20, j + 124, 3, 18));
        PixelGrabber pg = new PixelGrabber(image1, 0, 0, 3, 18, pix, 0, 3);
        try {
            pg.grabPixels();
        } catch (InterruptedException d) {
            System.err.println("en attente des pixels");
        }
        if ((pix[53] >> 8 & 0xFF) == (pix[53] >> 16 & 0xFF) && (pix[53] >> 8 & 0xFF) == (pix[53] >> 0 & 0xFF)) {
            couleur = 's';
        } else if ((pix[53] >> 16 & 0xFF) > (pix[53] >> 8 & 0xFF) && (pix[53] >> 16 & 0xFF) > (pix[53] >> 0 & 0xFF)) {
            couleur = 'h';
        } else if ((pix[53] >> 0 & 0xFF) > (pix[53] >> 16 & 0xFF) && (pix[53] >> 0 & 0xFF) > (pix[53] >> 8 & 0xFF)) {
            couleur = 'd';
        } else {
            couleur = 'c';
        }
        if (pix[7] == -1 && pix[18] == pix[19] && pix[5] == pix[8] && pix[11] == pix[14]) {
            if (pix[20] != -1) {
                this.mesCartes[0] = new Card('K', couleur);
            } else {
                this.mesCartes[0] = new Card('J', couleur);
            }
        } else if (pix[15] == pix[18] && pix[3] == -1 && pix[20] != -1 & pix[35] != -1) {
            if (pix[20] == pix[19] && pix[19] == pix[16] && pix[0] != pix[1]) {
                this.mesCartes[0] = new Card('T', couleur);
            } else if (pix[22] == -1) {
                if (pix[25] == -1) {
                    this.mesCartes[0] = new Card('9', couleur);
                } else {
                    this.mesCartes[0] = new Card('5', couleur);
                }
            } else if (pix[7] == -1) {
                if (pix[5] != -1) {
                    this.mesCartes[0] = new Card('8', couleur);
                } else {
                    this.mesCartes[0] = new Card('A', couleur);
                }
            } else {
                this.mesCartes[0] = new Card('6', couleur);
            }
        } else if (pix[1] == pix[2] && pix[4] == pix[5] && pix[7] == pix[8] && pix[7] != pix[10]) {
            this.mesCartes[0] = new Card('7', couleur);
        } else if (pix[2] == -1 && pix[15] != pix[16] && pix[4] == -1 && pix[4] == pix[5]) {
            this.mesCartes[0] = new Card('4', couleur);
        } else {
            if (pix[15] == -1 && pix[18] == -1 && pix[14] == -1) {
                if (pix[7] != -1) {
                    this.mesCartes[0] = new Card('2', couleur);
                } else {
                    this.mesCartes[0] = new Card('3', couleur);
                }
            } else if (pix[2] == pix[4] && pix[7] != -1) {
                this.mesCartes[0] = new Card('Q', couleur);
            }
        }
        image1 = this.bot.createScreenCapture(new Rectangle(i + 33, j + 126, 3, 18));
        pg = new PixelGrabber(image1, 0, 0, 3, 18, pix, 0, 3);
        try {
            pg.grabPixels();
        } catch (InterruptedException d) {
            System.err.println("en attente des pixels");
        }
        if ((pix[53] >> 8 & 0xFF) == (pix[53] >> 16 & 0xFF) && (pix[53] >> 8 & 0xFF) == (pix[53] >> 0 & 0xFF)) {
            couleur = 's';
        } else if ((pix[53] >> 16 & 0xFF) > (pix[53] >> 8 & 0xFF) && (pix[53] >> 16 & 0xFF) > (pix[53] >> 0 & 0xFF)) {
            couleur = 'h';
        } else if ((pix[53] >> 0 & 0xFF) > (pix[53] >> 16 & 0xFF) && (pix[53] >> 0 & 0xFF) > (pix[53] >> 8 & 0xFF)) {
            couleur = 'd';
        } else {
            couleur = 'c';
        }
        if (pix[7] == -1 && pix[18] == pix[19] && pix[5] == pix[8] && pix[11] == pix[14]) {
            if (pix[20] != -1) {
                this.mesCartes[1] = new Card('K', couleur);
            } else {
                this.mesCartes[1] = new Card('J', couleur);
            }
        } else if (pix[15] == pix[18] && pix[3] == -1 && pix[20] != -1 & pix[35] != -1) {
            if (pix[20] == pix[19] && pix[19] == pix[16] && pix[0] != pix[1]) {
                this.mesCartes[1] = new Card('T', couleur);
            } else if (pix[22] == -1) {
                if (pix[25] == -1) {
                    this.mesCartes[1] = new Card('9', couleur);
                } else {
                    this.mesCartes[1] = new Card('5', couleur);
                }
            } else if (pix[7] == -1) {
                if (pix[5] != -1) {
                    this.mesCartes[1] = new Card('8', couleur);
                } else {
                    this.mesCartes[1] = new Card('A', couleur);
                }
            } else {
                this.mesCartes[1] = new Card('6', couleur);
            }
        } else if (pix[1] == pix[2] && pix[4] == pix[5] && pix[7] == pix[8] && pix[7] != pix[10]) {
            this.mesCartes[1] = new Card('7', couleur);
        } else if (pix[2] == -1 && pix[15] != pix[16] && pix[4] == -1 && pix[4] == pix[5]) {
            this.mesCartes[1] = new Card('4', couleur);
        } else {
            if (pix[15] == -1 && pix[18] == -1 && pix[14] == -1) {
                if (pix[7] != -1) {
                    this.mesCartes[1] = new Card('2', couleur);
                } else {
                    this.mesCartes[1] = new Card('3', couleur);
                }
            } else if (pix[2] == pix[4] && pix[7] != -1) {
                this.mesCartes[1] = new Card('Q', couleur);
            }
        }
    }

    public void checkMoment() {
        int moment = -1;
        int[] pix = new int[131];
        BufferedImage image1 = this.bot.createScreenCapture(new Rectangle(i + 180, j + 97, 131, 1));
        try {
            ImageIO.write(image1, "png", new File("coincoin.png"));
        } catch (IOException e1) {
            e1.printStackTrace();
        }
        PixelGrabber pg = new PixelGrabber(image1, 0, 0, 131, 1, pix, 0, 131);
        try {
            pg.grabPixels();
        } catch (InterruptedException d) {
            System.err.println("en attente des pixels");
        }
        if (pix[130] == -1) {
            moment = 2;
        } else if (pix[100] == -1) {
            moment = 1;
        } else if (pix[0] == -1) {
            moment = 0;
        }
        System.out.println("Moment: " + moment);
        this.moment = moment;
    }

    public void getFlop() {
        char couleur = 'o';
        int[] pix = new int[54];
        BufferedImage image1 = this.bot.createScreenCapture(new Rectangle(i + 165, j + 97, 3, 18));
        PixelGrabber pg = new PixelGrabber(image1, 0, 0, 3, 18, pix, 0, 3);
        try {
            ImageIO.write(image1, "png", new File("coincoin.png"));
        } catch (IOException e1) {
            e1.printStackTrace();
        }
        try {
            pg.grabPixels();
        } catch (InterruptedException d) {
            System.err.println("en attente des pixels");
        }
        if ((pix[53] >> 8 & 0xFF) == (pix[53] >> 16 & 0xFF) && (pix[53] >> 8 & 0xFF) == (pix[53] >> 0 & 0xFF)) {
            couleur = 's';
        } else if ((pix[53] >> 16 & 0xFF) > (pix[53] >> 8 & 0xFF) && (pix[53] >> 16 & 0xFF) > (pix[53] >> 0 & 0xFF)) {
            couleur = 'h';
        } else if ((pix[53] >> 0 & 0xFF) > (pix[53] >> 16 & 0xFF) && (pix[53] >> 0 & 0xFF) > (pix[53] >> 8 & 0xFF)) {
            couleur = 'd';
        } else {
            couleur = 'c';
        }
        if (pix[7] == -1 && pix[18] == pix[19] && pix[5] == pix[8] && pix[11] == pix[14]) {
            if (pix[20] != -1) {
                this.flop[0] = new Card('K', couleur);
            } else {
                this.flop[0] = new Card('J', couleur);
            }
        } else if (pix[15] == pix[18] && pix[3] == -1 && pix[20] != -1 & pix[35] != -1) {
            if (pix[20] == pix[19] && pix[19] == pix[16] && pix[0] != pix[1]) {
                this.flop[0] = new Card('T', couleur);
            } else if (pix[22] == -1) {
                if (pix[25] == -1) {
                    this.flop[0] = new Card('9', couleur);
                } else {
                    this.flop[0] = new Card('5', couleur);
                }
            } else if (pix[7] == -1) {
                if (pix[5] != -1) {
                    this.flop[0] = new Card('8', couleur);
                } else {
                    this.flop[0] = new Card('A', couleur);
                }
            } else {
                this.flop[0] = new Card('6', couleur);
            }
        } else if (pix[1] == pix[2] && pix[4] == pix[5] && pix[7] == pix[8] && pix[7] != pix[10]) {
            this.flop[0] = new Card('7', couleur);
        } else if (pix[2] == -1 && pix[15] != pix[16] && pix[4] == -1 && pix[4] == pix[5]) {
            this.flop[0] = new Card('4', couleur);
        } else {
            if (pix[15] == -1 && pix[18] == -1 && pix[14] == -1) {
                if (pix[7] != -1) {
                    this.flop[0] = new Card('2', couleur);
                } else {
                    this.flop[0] = new Card('3', couleur);
                }
            } else if (pix[2] == pix[4] && pix[7] != -1) {
                this.flop[0] = new Card('Q', couleur);
            }
        }
        image1 = this.bot.createScreenCapture(new Rectangle(i + 197, j + 97, 3, 18));
        pg = new PixelGrabber(image1, 0, 0, 3, 18, pix, 0, 3);
        try {
            pg.grabPixels();
        } catch (InterruptedException d) {
            System.err.println("en attente des pixels");
        }
        if ((pix[53] >> 8 & 0xFF) == (pix[53] >> 16 & 0xFF) && (pix[53] >> 8 & 0xFF) == (pix[53] >> 0 & 0xFF)) {
            couleur = 's';
        } else if ((pix[53] >> 16 & 0xFF) > (pix[53] >> 8 & 0xFF) && (pix[53] >> 16 & 0xFF) > (pix[53] >> 0 & 0xFF)) {
            couleur = 'h';
        } else if ((pix[53] >> 0 & 0xFF) > (pix[53] >> 16 & 0xFF) && (pix[53] >> 0 & 0xFF) > (pix[53] >> 8 & 0xFF)) {
            couleur = 'd';
        } else {
            couleur = 'c';
        }
        if (pix[7] == -1 && pix[18] == pix[19] && pix[5] == pix[8] && pix[11] == pix[14]) {
            if (pix[20] != -1) {
                this.flop[1] = new Card('K', couleur);
            } else {
                this.flop[1] = new Card('J', couleur);
            }
        } else if (pix[15] == pix[18] && pix[3] == -1 && pix[20] != -1 & pix[35] != -1) {
            if (pix[20] == pix[19] && pix[19] == pix[16] && pix[0] != pix[1]) {
                this.flop[1] = new Card('T', couleur);
            } else if (pix[22] == -1) {
                if (pix[25] == -1) {
                    this.flop[1] = new Card('9', couleur);
                } else {
                    this.flop[1] = new Card('5', couleur);
                }
            } else if (pix[7] == -1) {
                if (pix[5] != -1) {
                    this.flop[1] = new Card('8', couleur);
                } else {
                    this.flop[1] = new Card('A', couleur);
                }
            } else {
                this.flop[1] = new Card('6', couleur);
            }
        } else if (pix[1] == pix[2] && pix[4] == pix[5] && pix[7] == pix[8] && pix[7] != pix[10]) {
            this.flop[1] = new Card('7', couleur);
        } else if (pix[2] == -1 && pix[15] != pix[16] && pix[4] == -1 && pix[4] == pix[5]) {
            this.flop[1] = new Card('4', couleur);
        } else {
            if (pix[15] == -1 && pix[18] == -1 && pix[14] == -1) {
                if (pix[7] != -1) {
                    this.flop[1] = new Card('2', couleur);
                } else {
                    this.flop[1] = new Card('3', couleur);
                }
            } else if (pix[2] == pix[4] && pix[7] != -1) {
                this.flop[1] = new Card('Q', couleur);
            }
        }
        image1 = this.bot.createScreenCapture(new Rectangle(i + 229, j + 97, 3, 18));
        pg = new PixelGrabber(image1, 0, 0, 3, 18, pix, 0, 3);
        try {
            pg.grabPixels();
        } catch (InterruptedException d) {
            System.err.println("en attente des pixels");
        }
        if ((pix[53] >> 8 & 0xFF) == (pix[53] >> 16 & 0xFF) && (pix[53] >> 8 & 0xFF) == (pix[53] >> 0 & 0xFF)) {
            couleur = 's';
        } else if ((pix[53] >> 16 & 0xFF) > (pix[53] >> 8 & 0xFF) && (pix[53] >> 16 & 0xFF) > (pix[53] >> 0 & 0xFF)) {
            couleur = 'h';
        } else if ((pix[53] >> 0 & 0xFF) > (pix[53] >> 16 & 0xFF) && (pix[53] >> 0 & 0xFF) > (pix[53] >> 8 & 0xFF)) {
            couleur = 'd';
        } else {
            couleur = 'c';
        }
        if (pix[7] == -1 && pix[18] == pix[19] && pix[5] == pix[8] && pix[11] == pix[14]) {
            if (pix[20] != -1) {
                this.flop[2] = new Card('K', couleur);
            } else {
                this.flop[2] = new Card('J', couleur);
            }
        } else if (pix[15] == pix[18] && pix[3] == -1 && pix[20] != -1 & pix[35] != -1) {
            if (pix[20] == pix[19] && pix[19] == pix[16] && pix[0] != pix[1]) {
                this.flop[2] = new Card('T', couleur);
            } else if (pix[22] == -1) {
                if (pix[25] == -1) {
                    this.flop[2] = new Card('9', couleur);
                } else {
                    this.flop[2] = new Card('5', couleur);
                }
            } else if (pix[7] == -1) {
                if (pix[5] != -1) {
                    this.flop[2] = new Card('8', couleur);
                } else {
                    this.flop[2] = new Card('A', couleur);
                }
            } else {
                this.flop[2] = new Card('6', couleur);
            }
        } else if (pix[1] == pix[2] && pix[4] == pix[5] && pix[7] == pix[8] && pix[7] != pix[10]) {
            this.flop[2] = new Card('7', couleur);
        } else if (pix[2] == -1 && pix[15] != pix[16] && pix[4] == -1 && pix[4] == pix[5]) {
            this.flop[2] = new Card('4', couleur);
        } else {
            if (pix[15] == -1 && pix[18] == -1 && pix[14] == -1) {
                if (pix[7] != -1) {
                    this.flop[2] = new Card('2', couleur);
                } else {
                    this.flop[2] = new Card('3', couleur);
                }
            } else if (pix[2] == pix[4] && pix[7] != -1) {
                this.flop[2] = new Card('Q', couleur);
            }
        }
    }

    public void getTurn() {
        char couleur = 'o';
        int[] pix = new int[54];
        BufferedImage image1 = this.bot.createScreenCapture(new Rectangle(i + 261, j + 97, 3, 18));
        PixelGrabber pg = new PixelGrabber(image1, 0, 0, 3, 18, pix, 0, 3);
        try {
            pg.grabPixels();
        } catch (InterruptedException d) {
            System.err.println("en attente des pixels");
        }
        if ((pix[53] >> 8 & 0xFF) == (pix[53] >> 16 & 0xFF) && (pix[53] >> 8 & 0xFF) == (pix[53] >> 0 & 0xFF)) {
            couleur = 's';
        } else if ((pix[53] >> 16 & 0xFF) > (pix[53] >> 8 & 0xFF) && (pix[53] >> 16 & 0xFF) > (pix[53] >> 0 & 0xFF)) {
            couleur = 'h';
        } else if ((pix[53] >> 0 & 0xFF) > (pix[53] >> 16 & 0xFF) && (pix[53] >> 0 & 0xFF) > (pix[53] >> 8 & 0xFF)) {
            couleur = 'd';
        } else {
            couleur = 'c';
        }
        if (pix[7] == -1 && pix[18] == pix[19] && pix[5] == pix[8] && pix[11] == pix[14]) {
            if (pix[20] != -1) {
                this.turn = new Card('K', couleur);
            } else {
                this.turn = new Card('J', couleur);
            }
        } else if (pix[15] == pix[18] && pix[3] == -1 && pix[20] != -1 & pix[35] != -1) {
            if (pix[20] == pix[19] && pix[19] == pix[16] && pix[0] != pix[1]) {
                this.turn = new Card('T', couleur);
            } else if (pix[22] == -1) {
                if (pix[25] == -1) {
                    this.turn = new Card('9', couleur);
                } else {
                    this.turn = new Card('5', couleur);
                }
            } else if (pix[7] == -1) {
                if (pix[5] != -1) {
                    this.turn = new Card('8', couleur);
                } else {
                    this.turn = new Card('A', couleur);
                }
            } else {
                this.turn = new Card('6', couleur);
            }
        } else if (pix[1] == pix[2] && pix[4] == pix[5] && pix[7] == pix[8] && pix[7] != pix[10]) {
            this.turn = new Card('7', couleur);
        } else if (pix[2] == -1 && pix[15] != pix[16] && pix[4] == -1 && pix[4] == pix[5]) {
            this.turn = new Card('4', couleur);
        } else {
            if (pix[15] == -1 && pix[18] == -1 && pix[14] == -1) {
                if (pix[7] != -1) {
                    this.turn = new Card('2', couleur);
                } else {
                    this.turn = new Card('3', couleur);
                }
            } else if (pix[2] == pix[4] && pix[7] != -1) {
                this.turn = new Card('Q', couleur);
            }
        }
    }

    public void getRiver() {
        char couleur = 'o';
        int[] pix = new int[54];
        BufferedImage image1 = this.bot.createScreenCapture(new Rectangle(i + 293, j + 97, 3, 18));
        PixelGrabber pg = new PixelGrabber(image1, 0, 0, 3, 18, pix, 0, 3);
        try {
            pg.grabPixels();
        } catch (InterruptedException d) {
            System.err.println("en attente des pixels");
        }
        if ((pix[53] >> 8 & 0xFF) == (pix[53] >> 16 & 0xFF) && (pix[53] >> 8 & 0xFF) == (pix[53] >> 0 & 0xFF)) {
            couleur = 's';
        } else if ((pix[53] >> 16 & 0xFF) > (pix[53] >> 8 & 0xFF) && (pix[53] >> 16 & 0xFF) > (pix[53] >> 0 & 0xFF)) {
            couleur = 'h';
        } else if ((pix[53] >> 0 & 0xFF) > (pix[53] >> 16 & 0xFF) && (pix[53] >> 0 & 0xFF) > (pix[53] >> 8 & 0xFF)) {
            couleur = 'd';
        } else {
            couleur = 'c';
        }
        if (pix[7] == -1 && pix[18] == pix[19] && pix[5] == pix[8] && pix[11] == pix[14]) {
            if (pix[20] != -1) {
                this.river = new Card('K', couleur);
            } else {
                this.river = new Card('J', couleur);
            }
        } else if (pix[15] == pix[18] && pix[3] == -1 && pix[20] != -1 & pix[35] != -1) {
            if (pix[20] == pix[19] && pix[19] == pix[16] && pix[0] != pix[1]) {
                this.river = new Card('T', couleur);
            } else if (pix[22] == -1) {
                if (pix[25] == -1) {
                    this.river = new Card('9', couleur);
                } else {
                    this.river = new Card('5', couleur);
                }
            } else if (pix[7] == -1) {
                if (pix[5] != -1) {
                    this.river = new Card('8', couleur);
                } else {
                    this.river = new Card('A', couleur);
                }
            } else {
                this.river = new Card('6', couleur);
            }
        } else if (pix[1] == pix[2] && pix[4] == pix[5] && pix[7] == pix[8] && pix[7] != pix[10]) {
            this.river = new Card('7', couleur);
        } else if (pix[2] == -1 && pix[15] != pix[16] && pix[4] == -1 && pix[4] == pix[5]) {
            this.river = new Card('4', couleur);
        } else {
            if (pix[15] == -1 && pix[18] == -1 && pix[14] == -1) {
                if (pix[7] != -1) {
                    this.river = new Card('2', couleur);
                } else {
                    this.river = new Card('3', couleur);
                }
            } else if (pix[2] == pix[4] && pix[7] != -1) {
                this.river = new Card('Q', couleur);
            }
        }
    }

    public class playerSurveille extends Thread {

        int id;

        public playerSurveille(int i) {
            this.id = i;
        }

        public void run() {
        }
    }

    public boolean isItMyTurn() {
        int[] pix = new int[1];
        boolean myTurn = false;
        BufferedImage image1 = this.bot.createScreenCapture(new Rectangle(i + 254, j + 308, 1, 1));
        PixelGrabber pg = new PixelGrabber(image1, 0, 0, 1, 1, pix, 0, 1);
        try {
            pg.grabPixels();
        } catch (InterruptedException d) {
            System.err.println("en attente des pixels");
        }
        if (pix[0] == -7060977) {
            myTurn = true;
        }
        return myTurn;
    }

    public class playerS extends Thread {

        int id;

        Robot boty;

        int[] x, y;

        public playerS(int i) {
            this.id = i;
            try {
                boty = new Robot();
            } catch (AWTException e) {
                e.printStackTrace();
            }
            x = new int[9];
            y = new int[9];
            x[1] = 84;
            x[2] = 170;
            x[3] = 312;
            x[4] = 395;
            x[5] = 443;
            x[6] = 369;
            x[7] = 237;
            x[8] = 110;
            y[1] = 63;
            y[2] = 27;
            y[3] = 27;
            y[4] = 64;
            y[5] = 140;
            y[6] = 210;
            y[7] = 226;
            y[8] = 209;
        }

        @SuppressWarnings("static-access")
        public void run() {
            while (true) {
                if (play[this.id]) {
                    int[] pix = new int[25];
                    BufferedImage image1 = this.boty.createScreenCapture(new Rectangle(i + x[this.id], j + y[this.id], 5, 5));
                    PixelGrabber pg = new PixelGrabber(image1, 0, 0, 5, 5, pix, 0, 5);
                    try {
                        pg.grabPixels();
                    } catch (InterruptedException d) {
                        System.err.println("en attente des pixels");
                    }
                    if (pix[6] == -13884141 && pix[7] == -8954866) {
                        checkBankroll(this.id);
                        if (players[this.id].bankroll != -1) {
                            players[this.id].addAction(3, players[this.id].bankroll - bankroll[this.id], moment);
                            System.out.println("Seat " + this.id + " bets " + (players[this.id].bankroll - bankroll[this.id]));
                        } else {
                            System.out.println("Seat " + this.id + " bets" + " - Initialisation BR");
                        }
                        players[this.id].bankroll = bankroll[this.id];
                        while (pix[6] == -13884141 && pix[7] == -8954866) {
                            image1 = this.boty.createScreenCapture(new Rectangle(i + x[this.id], j + y[this.id], 5, 5));
                            pg = new PixelGrabber(image1, 0, 0, 5, 5, pix, 0, 5);
                            try {
                                pg.grabPixels();
                            } catch (InterruptedException d) {
                                System.err.println("en attente des pixels");
                            }
                        }
                    } else if (pix[6] == -15198445 && pix[7] == -7115253) {
                        checkBankroll(this.id);
                        if (players[this.id].bankroll != -1) {
                            players[this.id].addAction(0, 0, moment);
                            System.out.println("Seat " + this.id + " folds");
                        } else {
                            System.out.println("Seat " + this.id + " folds" + " - Initialisation BR");
                        }
                        players[this.id].bankroll = bankroll[this.id];
                        while (pix[6] == -15198445 && pix[7] == -7115253) {
                            image1 = this.boty.createScreenCapture(new Rectangle(i + x[this.id], j + y[this.id], 5, 5));
                            pg = new PixelGrabber(image1, 0, 0, 5, 5, pix, 0, 5);
                            try {
                                pg.grabPixels();
                            } catch (InterruptedException d) {
                                System.err.println("en attente des pixels");
                            }
                        }
                    } else if (pix[6] == -5340917 && pix[7] == -13621229) {
                        checkBankroll(this.id);
                        if (players[this.id].bankroll != -1) {
                            players[this.id].addAction(2, players[this.id].bankroll - bankroll[this.id], moment);
                            System.out.println("Seat " + this.id + " calls " + (players[this.id].bankroll - bankroll[this.id]));
                        } else {
                            System.out.println("Seat " + this.id + " calls" + " - Initialisation BR");
                        }
                        players[this.id].bankroll = bankroll[this.id];
                        while (pix[6] == -5340917 && pix[7] == -13621229) {
                            image1 = this.boty.createScreenCapture(new Rectangle(i + x[this.id], j + y[this.id], 5, 5));
                            pg = new PixelGrabber(image1, 0, 0, 5, 5, pix, 0, 5);
                            try {
                                pg.grabPixels();
                            } catch (InterruptedException d) {
                                System.err.println("en attente des pixels");
                            }
                        }
                    } else if (pix[6] == -5275381 && pix[7] == -4814837) {
                        checkBankroll(this.id);
                        if (players[this.id].bankroll != -1) {
                            players[this.id].addAction(4, players[this.id].bankroll - bankroll[this.id], moment);
                            System.out.println("Seat " + this.id + " raises " + (players[this.id].bankroll - bankroll[this.id]));
                        } else {
                            System.out.println("Seat " + this.id + " raises " + " - Initialisation BR");
                        }
                        players[this.id].bankroll = bankroll[this.id];
                        while (pix[6] == -5275381 && pix[7] == -4814837) {
                            image1 = this.boty.createScreenCapture(new Rectangle(i + x[this.id], j + y[this.id], 5, 5));
                            pg = new PixelGrabber(image1, 0, 0, 5, 5, pix, 0, 5);
                            try {
                                pg.grabPixels();
                            } catch (InterruptedException d) {
                                System.err.println("en attente des pixels");
                            }
                        }
                    } else if (pix[6] == -13883885 && pix[7] == -8954866) {
                        checkBankroll(this.id);
                        if (players[this.id].bankroll != -1) {
                            players[this.id].addAction(1, 0, moment);
                            System.out.println("Seat " + this.id + " checks ");
                        } else {
                            System.out.println("Seat " + this.id + " checks" + " - Initialisation BR");
                        }
                        players[this.id].bankroll = bankroll[this.id];
                        while (pix[6] == -13883885 && pix[7] == -8954866) {
                            image1 = this.boty.createScreenCapture(new Rectangle(i + x[this.id], j + y[this.id], 5, 5));
                            pg = new PixelGrabber(image1, 0, 0, 5, 5, pix, 0, 5);
                            try {
                                pg.grabPixels();
                            } catch (InterruptedException d) {
                                System.err.println("en attente des pixels");
                            }
                        }
                    } else if (pix[22] == -11978223 && pix[23] == -14607085) {
                        checkBankroll(this.id);
                        if (players[this.id].bankroll != -1) {
                            players[this.id].addAction(12, players[this.id].bankroll - bankroll[this.id], moment);
                            System.out.println("Seat " + this.id + " posts big blind " + (players[this.id].bankroll - bankroll[this.id]));
                        } else {
                            System.out.println("Seat " + this.id + " posts big blind" + " - Initialisation BR");
                        }
                        players[this.id].bankroll = bankroll[this.id];
                        while (pix[22] == -11978223 && pix[23] == -14607085) {
                            image1 = this.boty.createScreenCapture(new Rectangle(i + x[this.id], j + y[this.id], 5, 5));
                            pg = new PixelGrabber(image1, 0, 0, 5, 5, pix, 0, 5);
                            try {
                                pg.grabPixels();
                            } catch (InterruptedException d) {
                                System.err.println("en attente des pixels");
                            }
                        }
                    } else if (pix[22] == -11846639 && pix[23] == -14607085) {
                        checkBankroll(this.id);
                        if (players[this.id].bankroll != -1) {
                            players[this.id].addAction(11, players[this.id].bankroll - bankroll[this.id], moment);
                            System.out.println("Seat " + this.id + " posts small blind " + (players[this.id].bankroll - bankroll[this.id]));
                        } else {
                            System.out.println("Seat " + this.id + " posts small blind" + " - Initialisation BR");
                        }
                        players[this.id].bankroll = bankroll[this.id];
                        while (pix[22] == -11846639 && pix[23] == -14607085) {
                            image1 = this.boty.createScreenCapture(new Rectangle(i + x[this.id], j + y[this.id], 5, 5));
                            pg = new PixelGrabber(image1, 0, 0, 5, 5, pix, 0, 5);
                            try {
                                pg.grabPixels();
                            } catch (InterruptedException d) {
                                System.err.println("en attente des pixels");
                            }
                        }
                    } else {
                    }
                } else {
                }
                try {
                    this.sleep(50);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
