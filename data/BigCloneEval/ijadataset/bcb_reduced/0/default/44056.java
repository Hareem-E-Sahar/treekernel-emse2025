import java.util.ArrayList;
import java.util.Random;
import java.io.*;
import java.util.*;
import javax.xml.parsers.*;
import org.w3c.dom.*;
import org.apache.crimson.tree.XmlDocument;

public class ProducableValuableChessBoard extends ChessBoard implements ProducableValuable, Cloneable {

    private String computer;

    private boolean resetAI;

    private int resetMax;

    private int resetMin;

    class PosInfo {

        String x;

        String y;

        String v;

        public PosInfo(String x, String y, String v) {
            this.x = x;
            this.y = y;
            this.v = v;
        }
    }

    class ChessmanAI {

        public String name;

        public String self;

        public String underAttack;

        public String canGoPosition;

        public String protectedV;

        public ArrayList goodPosition;

        public ChessmanAI() {
            this.goodPosition = new ArrayList();
        }
    }

    class AIIfo {

        String max;

        String min;

        ChessmanAI[] people;

        ChessmanAI[] computer;

        public AIIfo() {
            this.people = new ChessmanAI[7];
            this.computer = new ChessmanAI[7];
            for (int i = 0; i < 7; ++i) {
                this.people[i] = new ChessmanAI();
                this.computer[i] = new ChessmanAI();
            }
        }
    }

    private AIIfo ai;

    public ProducableValuableChessBoard() {
        super();
        this.computer = "blue";
        this.resetMin = 0;
        this.resetMax = 9999;
        this.ai = new AIIfo();
    }

    public ProducableValuableChessBoard(String fileName) throws Exception {
        this();
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = null;
        try {
            db = dbf.newDocumentBuilder();
        } catch (ParserConfigurationException pce) {
            System.err.println(pce);
            System.exit(1);
        }
        Document doc = null;
        try {
            doc = db.parse(fileName);
        } catch (DOMException dom) {
            System.err.println(dom.getMessage());
            System.exit(1);
        } catch (IOException ioe) {
            System.err.println(ioe);
            System.exit(1);
        }
        Element root = doc.getDocumentElement();
        String turn = root.getAttribute("turn");
        String computer = root.getAttribute("computer");
        setTurn(turn);
        Player fRed = new Player();
        fRed.setFlag("red");
        Element redPlayer = (Element) (root.getElementsByTagName("redPlayer").item(0));
        NodeList rchessmen = redPlayer.getElementsByTagName("chessman");
        for (int j = 0; j < rchessmen.getLength(); ++j) {
            Element rchessman = (Element) rchessmen.item(j);
            Chessman rcm = (Chessman) (Class.forName(rchessman.getAttribute("className")).newInstance());
            rcm.setName(rchessman.getAttribute("name"));
            rcm.setPosition(Integer.parseInt(rchessman.getAttribute("x")), Integer.parseInt(rchessman.getAttribute("y")));
            fRed.addChessman(rcm);
        }
        addPlayer(fRed);
        Player fBlue = new Player();
        fBlue.setFlag("blue");
        Element bluePlayer = (Element) (root.getElementsByTagName("bluePlayer").item(0));
        NodeList bchessmen = bluePlayer.getElementsByTagName("chessman");
        for (int j = 0; j < bchessmen.getLength(); ++j) {
            Element bchessman = (Element) bchessmen.item(j);
            Chessman bcm = (Chessman) (Class.forName(bchessman.getAttribute("className")).newInstance());
            bcm.setName(bchessman.getAttribute("name"));
            bcm.setPosition(Integer.parseInt(bchessman.getAttribute("x")), Integer.parseInt(bchessman.getAttribute("y")));
            fBlue.addChessman(bcm);
        }
        addPlayer(fBlue);
    }

    public void addAI(String aiFile) throws Exception {
        Player computerPlayer;
        Player peoplePlayer;
        if (this.computer.equals("blue")) {
            computerPlayer = this.blue;
            peoplePlayer = this.red;
        } else {
            computerPlayer = this.red;
            peoplePlayer = this.blue;
        }
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = null;
        try {
            db = dbf.newDocumentBuilder();
        } catch (ParserConfigurationException pce) {
            System.err.println(pce);
            System.exit(1);
        }
        Document doc = null;
        try {
            doc = db.parse(aiFile);
        } catch (DOMException dom) {
            System.err.println(dom.getMessage());
            System.exit(1);
        } catch (IOException ioe) {
            System.err.println(ioe);
            System.exit(1);
        }
        Element root = doc.getDocumentElement();
        this.resetMax = Integer.parseInt(root.getAttribute("max"));
        this.ai.max = this.resetMax + "";
        this.resetMin = Integer.parseInt(root.getAttribute("min"));
        this.ai.min = this.resetMin + "";
        Chessman temp;
        String chessmanClass;
        Element people = (Element) (root.getElementsByTagName("people").item(0));
        NodeList pchessmen = people.getElementsByTagName("chessman");
        for (int j = 0; j < pchessmen.getLength(); ++j) {
            Element pchessman = (Element) pchessmen.item(j);
            chessmanClass = pchessman.getAttribute("name");
            this.ai.people[j].name = chessmanClass;
            this.ai.people[j].underAttack = pchessman.getAttribute("underAttack");
            this.ai.people[j].self = pchessman.getAttribute("self");
            this.ai.people[j].canGoPosition = pchessman.getAttribute("canGoPosition");
            this.ai.people[j].protectedV = pchessman.getAttribute("protected");
            NodeList cpPosition = pchessman.getElementsByTagName("goodPosition");
            for (int pm = 0; pm < cpPosition.getLength(); ++pm) {
                Element pos = (Element) (cpPosition.item(pm));
                this.ai.people[j].goodPosition.add(new PosInfo(pos.getAttribute("x"), pos.getAttribute("y"), pos.getAttribute("value")));
            }
            for (int k = 0; k < peoplePlayer.getChessmen().size(); ++k) {
                temp = (Chessman) (peoplePlayer.getChessmen().get(k));
                if (temp.getClass().getName().equals(chessmanClass)) {
                    temp.setUnderAttackValue(Integer.parseInt(pchessman.getAttribute("underAttack")));
                    temp.setProtectedValue(Integer.parseInt(pchessman.getAttribute("protected")));
                    temp.setSelfValue(Integer.parseInt(pchessman.getAttribute("self")));
                    temp.setCanGoPositionValue(Integer.parseInt(pchessman.getAttribute("canGoPosition")));
                    NodeList pPosition = pchessman.getElementsByTagName("goodPosition");
                    for (int m = 0; m < pPosition.getLength(); ++m) {
                        Element pos = (Element) (pPosition.item(m));
                        temp.setGoodPositionValue(pos.getAttribute("x"), pos.getAttribute("y"), pos.getAttribute("value"));
                    }
                }
            }
        }
        Element computer = (Element) (root.getElementsByTagName("computer").item(0));
        NodeList cchessmen = people.getElementsByTagName("chessman");
        for (int j = 0; j < cchessmen.getLength(); ++j) {
            Element cchessman = (Element) pchessmen.item(j);
            chessmanClass = cchessman.getAttribute("name");
            this.ai.computer[j].name = chessmanClass;
            this.ai.computer[j].underAttack = cchessman.getAttribute("underAttack");
            this.ai.computer[j].self = cchessman.getAttribute("self");
            this.ai.computer[j].canGoPosition = cchessman.getAttribute("canGoPosition");
            this.ai.computer[j].protectedV = cchessman.getAttribute("protected");
            NodeList cpPosition = cchessman.getElementsByTagName("goodPosition");
            for (int pm = 0; pm < cpPosition.getLength(); ++pm) {
                Element pos = (Element) (cpPosition.item(pm));
                this.ai.computer[j].goodPosition.add(new PosInfo(pos.getAttribute("x"), pos.getAttribute("y"), pos.getAttribute("value")));
            }
            for (int k = 0; k < computerPlayer.getChessmen().size(); ++k) {
                temp = (Chessman) (computerPlayer.getChessmen().get(k));
                if (temp.getClass().getName().equals(chessmanClass)) {
                    temp.setUnderAttackValue(Integer.parseInt(cchessman.getAttribute("underAttack")));
                    temp.setProtectedValue(Integer.parseInt(cchessman.getAttribute("protected")));
                    temp.setSelfValue(Integer.parseInt(cchessman.getAttribute("self")));
                    temp.setCanGoPositionValue(Integer.parseInt(cchessman.getAttribute("canGoPosition")));
                    NodeList cPosition = cchessman.getElementsByTagName("goodPosition");
                    for (int m = 0; m < cPosition.getLength(); ++m) {
                        Element pos = (Element) (cPosition.item(m));
                        temp.setGoodPositionValue(pos.getAttribute("x"), pos.getAttribute("y"), pos.getAttribute("value"));
                    }
                }
            }
        }
    }

    public void improveAI(String who, String which) {
        ChessmanAI[] temp = null;
        if (who.equals("people")) {
            temp = this.ai.people;
        }
        if (who.equals("computer")) {
            temp = this.ai.computer;
        }
        for (int i = 0; i < 7; ++i) {
            if (temp[i].name.equals(which)) {
                try {
                    temp[i].self = "" + (Integer.parseInt(temp[i].self) + 200);
                } catch (Exception e) {
                    System.out.println(e);
                }
            }
        }
    }

    public void changeAI(String outFile) {
        Random r = new Random();
        try {
            int min = Integer.parseInt(this.ai.min);
            int max = Integer.parseInt(this.ai.max);
            int test = r.nextInt(max);
            if (test > min) {
                return;
            } else {
                DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
                DocumentBuilder db = null;
                db = dbf.newDocumentBuilder();
                Document doc = null;
                doc = db.newDocument();
                Element root = doc.createElement("AI");
                doc.appendChild(root);
                root.setAttribute("max", this.ai.max);
                root.setAttribute("min", this.ai.min);
                Element people = doc.createElement("people");
                root.appendChild(people);
                for (int i = 0; i < 7; i++) {
                    Element chessmanNode = doc.createElement("chessman");
                    people.appendChild(chessmanNode);
                    chessmanNode.setAttribute("name", this.ai.people[i].name);
                    chessmanNode.setAttribute("self", this.ai.people[i].self);
                    chessmanNode.setAttribute("underAttack", this.ai.people[i].underAttack);
                    chessmanNode.setAttribute("canGoPosition", this.ai.people[i].canGoPosition);
                    chessmanNode.setAttribute("protected", this.ai.people[i].protectedV);
                    System.out.println("pos size : " + this.ai.people[i].goodPosition.size());
                    for (int j = 0; j < this.ai.people[i].goodPosition.size(); ++j) {
                        PosInfo temp = (PosInfo) (this.ai.people[i].goodPosition.get(j));
                        Element p = doc.createElement("goodPosition");
                        chessmanNode.appendChild(p);
                        p.setAttribute("x", temp.x);
                        p.setAttribute("y", temp.y);
                        p.setAttribute("value", temp.v);
                    }
                }
                Element computer = doc.createElement("computer");
                root.appendChild(computer);
                for (int i = 0; i < 7; i++) {
                    Element chessmanNode = doc.createElement("chessman");
                    computer.appendChild(chessmanNode);
                    chessmanNode.setAttribute("name", this.ai.computer[i].name);
                    chessmanNode.setAttribute("self", this.ai.computer[i].self);
                    chessmanNode.setAttribute("underAttack", this.ai.computer[i].underAttack);
                    chessmanNode.setAttribute("canGoPosition", this.ai.computer[i].canGoPosition);
                    chessmanNode.setAttribute("protected", this.ai.computer[i].protectedV);
                    for (int j = 0; j < this.ai.computer[i].goodPosition.size(); ++j) {
                        PosInfo temp = (PosInfo) (this.ai.computer[i].goodPosition.get(j));
                        Element p = doc.createElement("goodPosition");
                        chessmanNode.appendChild(p);
                        p.setAttribute("x", temp.x);
                        p.setAttribute("y", temp.y);
                        p.setAttribute("value", temp.v);
                    }
                }
                FileOutputStream outStream = new FileOutputStream(outFile);
                OutputStreamWriter outWriter = new OutputStreamWriter(outStream);
                ((XmlDocument) doc).write(outWriter, "gb2312");
                outWriter.close();
                outStream.close();
                System.exit(0);
            }
        } catch (Exception e) {
            System.out.println(e);
            System.exit(-1);
        }
    }

    public void setComputer(String flag) {
        this.computer = flag;
    }

    public String getComputer() {
        return this.computer;
    }

    public int getExchangeValue() {
        this.onTurnPlayer().getValue();
        return (this.notOnTurnPlayer().getValue() - this.onTurnPlayer().getValue());
    }

    public int getValue() {
        this.blue.getValue();
        return (this.red.getValue() - this.blue.getValue());
    }

    public Object clone() throws CloneNotSupportedException {
        ProducableValuableChessBoard result = new ProducableValuableChessBoard();
        result.howTo = this.howTo;
        ArrayList redcm = new ArrayList();
        ArrayList bluecm = new ArrayList();
        Chessman temp;
        Object toAdd;
        Player red = new Player();
        red.setChessBoard(result);
        red.setFlag("red");
        Player blue = new Player();
        blue.setChessBoard(result);
        blue.setFlag("blue");
        try {
            for (int i = 0; i < this.red.getChessmen().size(); ++i) {
                toAdd = this.red.getChessmen().get(i);
                red.addChessman((Chessman) ((Chessman) (this.red.getChessmen().get(i))).clone());
            }
            for (int j = 0; j < this.blue.getChessmen().size(); ++j) {
                blue.addChessman((Chessman) ((Chessman) (this.blue.getChessmen().get(j))).clone());
            }
        } catch (Exception e) {
            System.out.println(e);
        }
        result.addPlayer(red);
        result.addPlayer(blue);
        result.setTurn(this.getTurn());
        result.gameOver = this.gameOver;
        result.loser = this.loser;
        return result;
    }

    public ArrayList nexts() {
        ArrayList result = new ArrayList();
        ArrayList msgs = this.onTurnPlayer().canMove();
        ProducableValuableChessBoard ncb;
        try {
            for (int i = 0; i < msgs.size(); ++i) {
                ncb = (ProducableValuableChessBoard) (this.clone());
                result.add(ncb);
            }
            for (int j = 0; j < msgs.size(); ++j) {
                ((ProducableValuableChessBoard) (result.get(j))).move((Message) (msgs.get(j)));
            }
        } catch (Exception e) {
            System.out.println("some exception happens");
            System.out.println(e);
            System.out.println(e.getCause());
        }
        return result;
    }

    public void textShow() {
        System.out.println(this.getTurn() + " move next");
        for (int j = 9; j >= 0; --j) {
            for (int i = 0; i < 9; ++i) {
                if (this.hasChessman(i, j)) {
                    System.out.print(this.getChessman(i, j).getFlag() + this.getChessman(i, j).getName() + " ");
                } else {
                    System.out.print("**    ");
                }
            }
            System.out.println();
        }
    }

    public void valueShow() {
        System.out.println("----------- ProducableValuableChessBoard value analyze-------------");
        System.out.println(this.getExchangeValue());
        System.out.println("red value: " + this.red.getValue());
        System.out.println("blue value: " + this.blue.getValue());
        System.out.println("-----------values-----------");
        for (int j = 9; j >= 0; --j) {
            for (int i = 0; i < 9; ++i) {
                if (this.hasChessman(i, j)) {
                    System.out.print(this.getChessman(i, j).getValue() + "  ");
                } else {
                    System.out.print("**    ");
                }
            }
            System.out.println();
        }
    }
}
