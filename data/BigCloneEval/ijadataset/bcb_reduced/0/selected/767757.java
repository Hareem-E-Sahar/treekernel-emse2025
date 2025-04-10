package net.sourceforge.pokerapp;

import java.lang.reflect.Constructor;

/****************************************************
 * The Dealer class simply represents the dealer.  It is a separate thread which 
 * periodically checks to determine if it is time to start dealing a new game.
 *
 * @author Dan Puperi
 * @version 1.00
 *
 **/
public class Dealer implements Runnable {

    private StartPoker theStartApp;

    private Thread dealerThread;

    private boolean dealing;

    private boolean alive;

    /***************************
 * The constructor creates and initializes the dealer.
 *
 * @param a The StartPoker class which instantiated this Dealer.
 *
 **/
    public Dealer(StartPoker a) {
        theStartApp = a;
        theStartApp.log("Constructing a Dealer", 3);
        alive = true;
        dealing = false;
        dealerThread = new Thread(this, "PokerDealer");
        dealerThread.start();
    }

    /***************************
 * run() implements the Thread classes run() function.  Is it called when the thread is 
 * started with Thread.start()
 **/
    public void run() {
        while (alive) {
            if (dealing && (theStartApp.getGame() == null)) {
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException x) {
                }
                restart();
            }
            try {
                Thread.sleep(6000);
            } catch (InterruptedException x) {
            }
        }
    }

    /***************************
 *  startDealing() begins auto-dealing the games
 **/
    public void startDealing() {
        theStartApp.log("Dealer.startDealing()", 3);
        dealing = true;
    }

    /***************************
 *  stopDealing() stops auto-dealing the games
 **/
    public void stopDealing() {
        theStartApp.log("Dealer.stopDealing()", 3);
        dealing = false;
    }

    /***************************
 *  killDealer() kills this thread
 **/
    public void killDealer() {
        alive = false;
    }

    /***************************
 * restart() is called at the end of games.  If one of the auto-start
 * checkboxes are selected, the game will continue to auto deal the selected game.
 **/
    public void restart() {
        theStartApp.log("Dealer.restart()", 3);
        if (theStartApp.dealingGames.size() == 0) return;
        int index = -1;
        int numGames = theStartApp.dealingGames.size();
        int r = (int) (((double) numGames) * Math.random() + 1.0);
        boolean gameDealt = false;
        for (int i = 0; i < numGames; i++) {
            int sum = 0;
            for (int j = 0; j <= i; j++) sum++;
            if ((r <= sum) && (!gameDealt)) {
                String gameName = new String();
                String gameTitle = new String();
                for (int g = 0; g < theStartApp.getGameClasses().size(); g++) {
                    if (((String) theStartApp.dealingGames.get(i)).equals((String) theStartApp.getGameClasses().get(g))) {
                        gameName = (String) theStartApp.dealingGames.get(i);
                        if (theStartApp.noLimit) {
                            gameTitle = "No Limit " + gameName;
                        } else if (theStartApp.betLimit) {
                            gameTitle = "" + theStartApp.maximumBet + " limit " + gameName;
                        } else if (theStartApp.potLimit) {
                            gameTitle = "Pot Limit " + gameName;
                        } else {
                            PokerMoney d = new PokerMoney(2.0f * theStartApp.minimumBet.amount());
                            gameTitle = "" + theStartApp.minimumBet + "/" + d + " " + gameName;
                        }
                        theStartApp.broadcastMessage("NEW GAME  &" + gameTitle);
                        try {
                            Class game_class = Class.forName("net.sourceforge.pokerapp.games." + theStartApp.getGameClasses().get(g));
                            Class arg_types[] = { Class.forName(theStartApp.getClass().getName()) };
                            Constructor construct = game_class.getConstructor(arg_types);
                            Object argList[] = { theStartApp };
                            theStartApp.setGame((PokerGame) construct.newInstance(argList));
                            gameDealt = true;
                        } catch (Exception x) {
                            theStartApp.log("Error : Dealer tried to deal a game of " + gameTitle + ", but caught an exception.");
                            theStartApp.logStackTrace(x);
                        }
                        if (theStartApp.getGame() != null) {
                            if (theStartApp.getGame().getGameError()) {
                                theStartApp.nullifyGame();
                            }
                        }
                    }
                }
            }
        }
    }
}
