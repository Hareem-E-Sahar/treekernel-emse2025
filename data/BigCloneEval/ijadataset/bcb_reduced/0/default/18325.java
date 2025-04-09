import uk.co.rubox.*;
import java.io.*;
import java.util.*;
import java.lang.reflect.*;

public class testingrig implements rPlayer {

    rOutput output;

    rInput input;

    piece[][][] cube;

    public game logic;

    int player = 1;

    int players = 4;

    public void nextPlayer() {
        if (++player > players) {
            player = 1;
        }
    }

    public void setDisplayMethod(String classname) {
        try {
            Class col = ClassLoader.getSystemClassLoader().loadClass(classname);
            Constructor colcon = col.getConstructor(new Class[] {});
            output = (rOutput) colcon.newInstance(new Object[] {});
        } catch (Exception e) {
            System.err.println("Error: The specified class does not implement rOutput interface.");
            System.exit(1);
        }
    }

    public testingrig(String game) {
        if (game.equals("gameoxo")) {
            logic = new gameoxo();
        } else {
            logic = new gamelo();
        }
        try {
            cube = logic.initial(4, 4, 4);
        } catch (Exception e) {
            System.exit(1);
        }
    }

    public testingrig() {
        try {
            cube = logic.initial(4, 4, 4);
        } catch (Exception e) {
            System.exit(1);
        }
    }

    public void newGame(int x, int y, int z) {
        System.err.println("" + x + " - " + y + " - " + z + " = " + cube);
        cube = null;
        System.err.println("" + x + " - " + y + " - " + z + " = " + cube);
        try {
            cube = logic.initial(x, y, z);
            player = 0;
        } catch (Exception e) {
            System.err.println(e);
        }
        System.err.println("" + x + " - " + y + " - " + z + " = " + cube);
        this.display();
        this.makeMove(0, 0, 0, 0, 0, 0, 1);
        return;
    }

    public void display() {
        output.refreshCube(cube);
    }

    public void finalise(transitionalpiece[][][] eingabe) {
        System.err.println("I'm trying to finalise a move");
        for (int i = 0; i < eingabe.length; i++) {
            for (int j = 0; j < eingabe[0].length; j++) {
                for (int k = 0; k < eingabe[0][0].length; k++) {
                    eingabe[i][j][k].commitStatus(true);
                    cube[i][j][k] = eingabe[i][j][k].toPiece();
                }
            }
        }
        System.err.println("I just finalised a move");
        return;
    }

    public void makeMove(int x1, int y1, int z1, int x2, int y2, int z2, int type) {
        try {
            System.err.println("I'm trying to make a move");
            transitionalpiece[][][] newcube = logic.executeMove(cube, type, x1, y1, z1, x2, y2, z2, new piecestatus(player));
            finalise(newcube);
            if (logic.lastOK() == true) {
                this.nextPlayer();
            }
            System.err.println("I managed it.");
        } catch (Exception e) {
            System.err.println(e);
        }
        this.display();
        return;
    }

    public static void main(String args[]) {
        String game = "gameoxo";
        try {
            game = args[0];
        } catch (Exception e) {
        }
        testingrig test = new testingrig(game);
        test.input = new RuboxGui();
        test.output = (rOutput) test.input;
        test.input.attachInstance(test);
        test.display();
    }

    public int createData(Object value) {
        return 0;
    }

    public void attachOutput(rOutput outputsystem) {
        return;
    }

    public void attachInput(rInput inputsystem) {
        return;
    }

    public void attachController(rController maininstance) {
        return;
    }

    public rOutput getOutput() {
        return output;
    }

    public rInput getInput() {
        return input;
    }

    public String toString() {
        return "TEST";
    }

    public int createData(String type) {
        return 0;
    }

    public void setData(int index, Object value) {
        return;
    }

    public Object getData(int index) {
        return new Object();
    }

    public Object obtainData(int index) {
        return new Object();
    }
}
