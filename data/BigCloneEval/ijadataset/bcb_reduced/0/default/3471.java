import java.lang.String;
import java.io.*;
import java.util.*;
import java.util.Arrays;

public class GenTD {

    /**
	 * 
	 */
    private String Filename;

    private int NumberOfParentSolutions;

    private int NumPosMut;

    private int NumNoImprovement;

    private int EdgeCnt;

    private boolean ErrorEdgeCnt;

    private boolean ErrorVerticesCnt;

    private int LineCnt;

    private int ErrorLineNumber;

    private int NumEdges;

    private int NumVertices;

    private int NumCreatedChildren;

    private String ProblemType;

    private int NumParentSolutions;

    private int VectorsInChildrenMatrix;

    private int NumMutParent;

    private int NumRecParent;

    private int[][] Graph;

    private int[][] ParentMatrix;

    private int[][] ChildrenMatrix;

    private int cntNoImprovement;

    private int minTreeWidth;

    public void setFilename(String Filename) {
    }

    public void setNumberOfParentSolutions(int NumberOfParentSolutions) {
    }

    public void setNumPosMut(int NumPosMut) {
    }

    public void setNumNoImprovement(int NumNoImprovement) {
    }

    public int getMinTreeWidth() {
        return this.minTreeWidth;
    }

    private int[][] ReadGraph(String Filename) {
        FileReader myFileReader;
        int myChar, CharNum = 0;
        String myLine = "";
        try {
            myFileReader = new FileReader(Filename);
            while ((myChar = myFileReader.read()) != -1) {
                if ((ErrorEdgeCnt == true) || (ErrorVerticesCnt == true)) {
                    break;
                }
                myLine += String.valueOf((char) myChar);
                CharNum++;
                if ((char) myChar == '\n') {
                    CharNum = 0;
                    switch(myLine.charAt(0)) {
                        case 'c':
                            FileComment(myLine);
                            break;
                        case 'p':
                            InitNumberOfEdgesAndVertices(myLine);
                            break;
                        case 'a':
                            WriteEdgeToMatrix(myLine);
                            break;
                        default:
                            System.out.print("Die Eingabedatei weiste eine falsche Syntax auf!");
                    }
                    LineCnt++;
                    myLine = "";
                }
            }
            myFileReader.close();
        } catch (IOException e) {
            System.out.println("Fehler beim Lesen der Datei");
        }
        return Graph;
    }

    private void InitNumberOfEdgesAndVertices(String myLine) {
        int WhiteSpace1, WhiteSpace2, WhiteSpace3, StringLength;
        WhiteSpace1 = WhiteSpace2 = WhiteSpace3 = StringLength = 0;
        myLine.trim();
        WhiteSpace1 = myLine.indexOf(" ");
        WhiteSpace2 = myLine.indexOf(" ", WhiteSpace1 + 1);
        WhiteSpace3 = myLine.indexOf(" ", WhiteSpace2 + 1);
        StringLength = myLine.length() - 1;
        ProblemType = myLine.substring(WhiteSpace1 + 1, WhiteSpace2);
        NumVertices = Integer.valueOf(myLine.substring(WhiteSpace2 + 1, WhiteSpace3)).intValue();
        NumEdges = Integer.valueOf(myLine.substring(WhiteSpace3 + 1, StringLength)).intValue();
        Graph = new int[NumVertices + 1][NumVertices + 1];
    }

    private void WriteEdgeToMatrix(String myLine) {
        int WhiteSpace1, WhiteSpace2, WhiteSpace3, NodeU, NodeV;
        WhiteSpace1 = WhiteSpace2 = WhiteSpace3 = 0;
        EdgeCnt++;
        if (EdgeCnt <= NumEdges) {
            myLine.trim();
            WhiteSpace1 = myLine.indexOf(" ");
            WhiteSpace2 = myLine.indexOf(" ", WhiteSpace1 + 1);
            WhiteSpace3 = myLine.indexOf(" ", WhiteSpace2 + 1);
            NodeU = Integer.valueOf(myLine.substring(WhiteSpace1 + 1, WhiteSpace2)).intValue();
            NodeV = Integer.valueOf(myLine.substring(WhiteSpace2 + 1, WhiteSpace3)).intValue();
            if ((0 < NodeU) && (NodeU <= NumVertices) && (0 < NodeV) && (NodeV <= NumVertices)) {
                Graph[NodeU][NodeV] = 1;
                Graph[NodeV][NodeU] = 1;
            } else {
                ErrorLineNumber = LineCnt + 1;
                ErrorVerticesCnt = true;
            }
        } else {
            ErrorEdgeCnt = true;
        }
    }

    private void FileComment(String myComment) {
    }

    private int CalculateTreeWidth(int myGraph[][], int SolutionVector[], int zaehler) {
        int[][] TreeDecompositionGraph;
        int[][] DecompositionedMatrix;
        int[] ConnectedNodes;
        int[] NextSolutionVector;
        int TreeWidth;
        int NumVerticesOfMyGraph;
        int DeeperTreeWidth;
        int LineIndex;
        NumVerticesOfMyGraph = myGraph.length;
        DecompositionedMatrix = new int[NumVerticesOfMyGraph - 1][NumVerticesOfMyGraph - 1];
        ConnectedNodes = new int[NumVerticesOfMyGraph];
        TreeWidth = 0;
        DeeperTreeWidth = 0;
        LineIndex = 0;
        NextSolutionVector = new int[SolutionVector.length - 1];
        for (int i = 1; i < NumVerticesOfMyGraph; i++) {
            if (SolutionVector[0] == myGraph[i][0]) {
                LineIndex = i;
            }
        }
        for (int j = 1; j < NumVerticesOfMyGraph; j++) {
            if (myGraph[LineIndex][j] == 1) {
                ConnectedNodes[TreeWidth] = myGraph[0][j];
                TreeWidth++;
            }
        }
        for (int i = 0; i < NumVerticesOfMyGraph; i++) {
            if (i < LineIndex) {
                for (int j = 0; j < NumVerticesOfMyGraph; j++) {
                    if (j < LineIndex) {
                        DecompositionedMatrix[i][j] = myGraph[i][j];
                    } else if (j > LineIndex) {
                        DecompositionedMatrix[i][j - 1] = myGraph[i][j];
                    }
                }
            } else if (i > LineIndex) {
                for (int j = 0; j < NumVerticesOfMyGraph; j++) {
                    if (j < LineIndex) {
                        DecompositionedMatrix[i - 1][j] = myGraph[i][j];
                    } else if (j > LineIndex) {
                        DecompositionedMatrix[i - 1][j - 1] = myGraph[i][j];
                    }
                }
            }
        }
        for (int k = 0; k < TreeWidth; k++) {
            for (int i = 0; i < DecompositionedMatrix.length; i++) {
                if (DecompositionedMatrix[i][0] == ConnectedNodes[k]) {
                    for (int l = 0; l < TreeWidth; l++) {
                        for (int j = 0; j < DecompositionedMatrix.length; j++) {
                            if (DecompositionedMatrix[0][j] == ConnectedNodes[l]) {
                                DecompositionedMatrix[i][j] = 1;
                            }
                        }
                    }
                }
            }
        }
        for (int i = 1; i < DecompositionedMatrix.length; i++) {
            DecompositionedMatrix[i][i] = 0;
            for (int j = 1; j < DecompositionedMatrix.length; j++) {
                DecompositionedMatrix[i][j] = DecompositionedMatrix[j][i];
            }
        }
        for (int l = 1; l < SolutionVector.length; l++) {
            NextSolutionVector[l - 1] = SolutionVector[l];
        }
        if (NextSolutionVector.length > 1) {
            DeeperTreeWidth = CalculateTreeWidth(DecompositionedMatrix, NextSolutionVector, zaehler + 1);
            if (TreeWidth < DeeperTreeWidth) {
                TreeWidth = DeeperTreeWidth;
            }
        }
        return TreeWidth;
    }

    public void Mutate() {
        int UsedSelectedParentVectorsCnt, NumMutParentNodesCnt, NumMutParentNodes, NumMutParentNodesMax, VectorNodeValue, FirstIndex, SecondIndex, TmpVectorNodeValue;
        int[] IndexSelectedParentVectors;
        int[] IndexSelectedParentVectorNodes;
        Random r = new Random();
        UsedSelectedParentVectorsCnt = 0;
        NumMutParentNodes = 0;
        NumMutParentNodesCnt = 0;
        VectorNodeValue = 0;
        FirstIndex = 0;
        TmpVectorNodeValue = 0;
        NumMutParentNodesMax = (int) Math.floor(Math.sqrt(NumVertices));
        this.NumMutParent = Math.abs(r.nextInt()) % (NumParentSolutions + 1);
        if (NumMutParent != 0) {
            IndexSelectedParentVectors = new int[NumMutParent];
            while (UsedSelectedParentVectorsCnt < NumMutParent) {
                IndexSelectedParentVectors[UsedSelectedParentVectorsCnt] = Math.abs(r.nextInt()) % (NumParentSolutions);
                NumMutParentNodes = (int) Math.ceil((Math.abs(r.nextInt()) % NumMutParentNodesMax) / 2);
                IndexSelectedParentVectorNodes = new int[NumMutParentNodes];
                for (int i = 0; i < NumVertices; i++) {
                    ChildrenMatrix[i][VectorsInChildrenMatrix] = ParentMatrix[i][IndexSelectedParentVectors[UsedSelectedParentVectorsCnt]];
                }
                VectorsInChildrenMatrix++;
                while (NumMutParentNodesCnt < NumMutParentNodes) {
                    FirstIndex = Math.abs(r.nextInt()) % NumVertices;
                    SecondIndex = Math.abs(r.nextInt()) % NumVertices;
                    while (FirstIndex == SecondIndex) {
                        SecondIndex = Math.abs(r.nextInt()) % NumVertices;
                    }
                    TmpVectorNodeValue = ChildrenMatrix[FirstIndex][IndexSelectedParentVectors[UsedSelectedParentVectorsCnt]];
                    ChildrenMatrix[FirstIndex][IndexSelectedParentVectors[UsedSelectedParentVectorsCnt]] = ChildrenMatrix[SecondIndex][IndexSelectedParentVectors[UsedSelectedParentVectorsCnt]];
                    ChildrenMatrix[SecondIndex][IndexSelectedParentVectors[UsedSelectedParentVectorsCnt]] = TmpVectorNodeValue;
                    ++NumMutParentNodesCnt;
                }
                NumMutParentNodesCnt = 0;
                ++UsedSelectedParentVectorsCnt;
            }
            UsedSelectedParentVectorsCnt = 0;
            VectorsInChildrenMatrix = 0;
        }
    }

    public void Recombine() {
        int NumRecParentCnt, FirstParent, SecondParent, RecIndex, LineInChildrenMatrix, ReadIndex, WriteIndex, VectorNodeValue;
        int l;
        boolean ExistFlag;
        Random r = new Random();
        NumRecParentCnt = 0;
        RecIndex = 0;
        WriteIndex = 0;
        LineInChildrenMatrix = 0;
        ReadIndex = 0;
        l = 0;
        ExistFlag = true;
        this.NumRecParent = (int) Math.floor(Math.sqrt(NumVertices)) - this.NumMutParent;
        while (NumRecParentCnt < this.NumRecParent) {
            FirstParent = Math.abs(r.nextInt()) % (int) Math.floor(Math.sqrt(NumVertices));
            SecondParent = Math.abs(r.nextInt()) % (int) Math.floor(Math.sqrt(NumVertices));
            while (FirstParent == SecondParent) {
                SecondParent = Math.abs(r.nextInt()) % (int) Math.floor(Math.sqrt(NumVertices));
            }
            RecIndex = Math.abs(r.nextInt()) % NumVertices;
            for (int i = 0; i < RecIndex; i++) {
                LineInChildrenMatrix = NumRecParentCnt + this.NumMutParent;
                ChildrenMatrix[i][LineInChildrenMatrix] = ParentMatrix[i][FirstParent];
            }
            WriteIndex = RecIndex;
            for (int i = RecIndex; i < NumVertices; i++) {
                l = 0;
                ExistFlag = true;
                while (ExistFlag == true) {
                    ReadIndex = (l + WriteIndex) % NumVertices;
                    VectorNodeValue = ParentMatrix[ReadIndex][SecondParent];
                    ExistFlag = false;
                    for (int k = 0; k < WriteIndex; k++) {
                        if (ChildrenMatrix[k][LineInChildrenMatrix] == VectorNodeValue) {
                            ExistFlag = true;
                            break;
                        }
                    }
                    l++;
                }
                ChildrenMatrix[WriteIndex][LineInChildrenMatrix] = ParentMatrix[ReadIndex][SecondParent];
                WriteIndex++;
            }
            NumRecParentCnt++;
        }
        NumRecParentCnt = 0;
    }

    public void Select() {
        int[] SolutionVector;
        int[] TournamentVectorWinners;
        int[] TournamentVectorLoosers;
        int[][] TmpParentMatrix;
        int[] ExtractLooserFromWinnerVector;
        float[] SurvivalProbabilityParent;
        float[] SurvivalProbabilityChildren;
        float SurvivalProbability;
        boolean ParentLesserProbability, ChildrenLesserProbability, LesserProbability, VectorDeletedFlagParent, VectorDeletedFlagChildren, TournamentVectorLoosersFlagParent, TournamentVectorLoosersFlagChildren, DuplicateEntryFlag, VectorDeletedFlag;
        int NumChildrenSolutions, IndexFirstCandidate, IndexSecondCandidate, FirstCandidate, SecondCandidate;
        int Tournaments, Tournament;
        int lokalMinTreeWidth;
        int RandomSolutionVectorIndex;
        NumChildrenSolutions = NumParentSolutions;
        Tournaments = NumParentSolutions;
        Tournament = 0;
        ParentLesserProbability = false;
        ChildrenLesserProbability = false;
        LesserProbability = false;
        VectorDeletedFlag = false;
        VectorDeletedFlagParent = false;
        VectorDeletedFlagChildren = false;
        DuplicateEntryFlag = false;
        TournamentVectorLoosersFlagParent = false;
        TournamentVectorLoosersFlagChildren = false;
        Random r = new Random();
        SolutionVector = new int[NumVertices];
        TournamentVectorWinners = new int[NumParentSolutions];
        TournamentVectorLoosers = new int[NumParentSolutions];
        TmpParentMatrix = new int[NumVertices + 1][NumParentSolutions];
        ExtractLooserFromWinnerVector = new int[(NumParentSolutions + NumChildrenSolutions)];
        SurvivalProbabilityParent = new float[NumParentSolutions];
        SurvivalProbabilityChildren = new float[NumChildrenSolutions];
        IndexFirstCandidate = 0;
        IndexSecondCandidate = 0;
        FirstCandidate = 0;
        SecondCandidate = 0;
        lokalMinTreeWidth = 0;
        RandomSolutionVectorIndex = 0;
        for (int ParentVector = 0; ParentVector < NumParentSolutions; ParentVector++) {
            if (ParentMatrix[NumVertices][ParentVector] == 0) {
                for (int VectorElement = 0; VectorElement < NumVertices; VectorElement++) {
                    SolutionVector[VectorElement] = ParentMatrix[VectorElement][ParentVector];
                }
                ParentMatrix[NumVertices][ParentVector] = CalculateTreeWidth(Graph, SolutionVector, 0);
                if ((lokalMinTreeWidth == 0) || ParentMatrix[NumVertices][ParentVector] < lokalMinTreeWidth) {
                    lokalMinTreeWidth = ParentMatrix[NumVertices][ParentVector];
                }
            }
        }
        for (int ChildrenVector = 0; ChildrenVector < NumChildrenSolutions; ChildrenVector++) {
            if (ChildrenMatrix[NumVertices][ChildrenVector] == 0) {
                for (int VectorElement = 0; VectorElement < NumVertices; VectorElement++) {
                    SolutionVector[VectorElement] = ChildrenMatrix[VectorElement][ChildrenVector];
                }
                ChildrenMatrix[NumVertices][ChildrenVector] = CalculateTreeWidth(Graph, SolutionVector, 0);
                if ((lokalMinTreeWidth == 0) || ChildrenMatrix[NumVertices][ChildrenVector] < lokalMinTreeWidth) {
                    lokalMinTreeWidth = ChildrenMatrix[NumVertices][ChildrenVector];
                }
            }
        }
        for (int ParentVector = 0; ParentVector < NumParentSolutions; ParentVector++) {
            SurvivalProbabilityParent[ParentVector] = (float) lokalMinTreeWidth / (float) ParentMatrix[NumVertices][ParentVector];
        }
        for (int ChildrenVector = 0; ChildrenVector < NumChildrenSolutions; ChildrenVector++) {
            SurvivalProbabilityChildren[ChildrenVector] = (float) lokalMinTreeWidth / (float) ChildrenMatrix[NumVertices][ChildrenVector];
        }
        while (Tournament < Tournaments) {
            SurvivalProbability = ((float) (Math.abs(r.nextInt(11))) / 10);
            ParentLesserProbability = false;
            ChildrenLesserProbability = false;
            LesserProbability = false;
            for (int ParentVector = 0; ParentVector < NumParentSolutions; ParentVector++) {
                if (SurvivalProbabilityParent[ParentVector] <= SurvivalProbability) {
                    ParentLesserProbability = true;
                } else {
                    ParentLesserProbability = false;
                }
            }
            for (int ChildrenVector = 0; ChildrenVector < NumChildrenSolutions; ChildrenVector++) {
                if (SurvivalProbabilityChildren[ChildrenVector] <= SurvivalProbability) {
                    ChildrenLesserProbability = true;
                } else {
                    ChildrenLesserProbability = false;
                }
            }
            if ((ChildrenLesserProbability == true) || (ParentLesserProbability == true)) {
                LesserProbability = true;
            } else {
                LesserProbability = false;
            }
            VectorDeletedFlag = false;
            if (LesserProbability == true) {
                while (!(VectorDeletedFlag)) {
                    RandomSolutionVectorIndex = Math.abs(r.nextInt()) % NumParentSolutions;
                    if ((Math.abs(r.nextInt()) % 2) == 0) {
                        if (SurvivalProbabilityParent[RandomSolutionVectorIndex] <= SurvivalProbability) {
                            TournamentVectorLoosers[Tournament] = RandomSolutionVectorIndex;
                        }
                    } else {
                        if (SurvivalProbabilityChildren[RandomSolutionVectorIndex] <= SurvivalProbability) {
                            TournamentVectorLoosers[Tournament] = RandomSolutionVectorIndex + NumParentSolutions;
                        }
                    }
                    DuplicateEntryFlag = false;
                    for (int LastTournament = 0; LastTournament < Tournament; LastTournament++) {
                        if (TournamentVectorLoosers[LastTournament] == TournamentVectorLoosers[Tournament]) {
                            DuplicateEntryFlag = true;
                        }
                    }
                    if (DuplicateEntryFlag == false) {
                        Tournament++;
                        VectorDeletedFlag = true;
                    } else {
                        break;
                    }
                }
            }
        }
        for (int i = 0; i < (NumParentSolutions + NumChildrenSolutions); i++) {
            ExtractLooserFromWinnerVector[i] = 1;
        }
        for (int i = 0; i < Tournaments; i++) {
            ExtractLooserFromWinnerVector[TournamentVectorLoosers[i]] = 0;
        }
        Tournament = 0;
        for (int i = 0; i < (NumParentSolutions + NumChildrenSolutions); i++) {
            if (ExtractLooserFromWinnerVector[i] == 1) {
                TournamentVectorWinners[Tournament] = i;
                Tournament++;
            }
        }
        for (int j = 0; j < NumParentSolutions; j++) {
            if (NumParentSolutions <= TournamentVectorWinners[j]) {
                for (int i = 0; i <= NumVertices; i++) {
                    TmpParentMatrix[i][j] = ChildrenMatrix[i][TournamentVectorWinners[j] % NumChildrenSolutions];
                }
            } else {
                for (int i = 0; i <= NumVertices; i++) {
                    TmpParentMatrix[i][j] = ParentMatrix[i][TournamentVectorWinners[j]];
                }
            }
        }
        for (int i = 0; i < NumParentSolutions; i++) {
            for (int j = 0; j <= NumVertices; j++) {
                ParentMatrix[j][i] = TmpParentMatrix[j][i];
            }
        }
        System.out.println("");
        MyDebug2();
    }

    public void MyDebug() {
        for (int i = 0; i < NumParentSolutions; i++) {
            for (int j = 0; j <= NumVertices; j++) {
                System.out.print(ChildrenMatrix[j][i] + " ");
            }
            System.out.println("");
        }
    }

    public void MyDebug2() {
        for (int i = 0; i < NumParentSolutions; i++) {
            for (int j = 0; j <= NumVertices; j++) {
                System.out.print(ParentMatrix[j][i] + " ");
            }
            System.out.println("");
        }
    }

    public void MyDebug3() {
        for (int i = 0; i < NumParentSolutions; i++) {
            for (int j = 0; j <= NumVertices; j++) {
                System.out.print(ParentMatrix[j][i] + " ");
            }
            System.out.println("");
        }
    }

    public GenTD(String args) {
        int[][] TreeDecompositionGraph;
        int NumVerticesOfMyGraph;
        int UsedNodesCnt;
        EdgeCnt = 0;
        NumEdges = 0;
        NumVertices = 0;
        LineCnt = 0;
        UsedNodesCnt = 0;
        this.NumMutParent = 0;
        this.NumRecParent = 0;
        ErrorEdgeCnt = false;
        ErrorVerticesCnt = false;
        ErrorLineNumber = -1;
        this.NumCreatedChildren = 0;
        VectorsInChildrenMatrix = 0;
        ProblemType = "";
        this.minTreeWidth = 0;
        ReadGraph(args);
        BitSet RemainingNodes = new BitSet();
        Random RandomNode = new Random();
        if (ErrorEdgeCnt == true) {
            System.out.println("Die Angabe der Kanten in der Eingabedatei stimmt nicht mit der Anzahl der tatsächlichen Kanten überein!");
            System.out.print("Anzahl der angegebenen Kanten: ");
            System.out.println(NumEdges);
            System.out.print("Anzahl der gefundenen Kanten: ");
            System.out.println(EdgeCnt);
        }
        if (ErrorVerticesCnt == true) {
            System.out.print("ungültiger Knoten in Zeile: ");
            System.out.println(ErrorLineNumber);
        }
        NumVerticesOfMyGraph = Graph.length - 1;
        TreeDecompositionGraph = new int[NumVerticesOfMyGraph + 1][NumVerticesOfMyGraph + 1];
        for (int i = 0; i <= NumVerticesOfMyGraph; i++) {
            for (int j = 0; j <= NumVerticesOfMyGraph; j++) {
                TreeDecompositionGraph[i][j] = Graph[i][j];
            }
        }
        for (int i = 0; i <= NumVerticesOfMyGraph; i++) {
            TreeDecompositionGraph[i][0] = i;
        }
        for (int j = 0; j <= NumVerticesOfMyGraph; j++) {
            TreeDecompositionGraph[0][j] = j;
        }
        System.out.println("Graph in der Decompositionmatrix:");
        for (int i = 0; i <= NumVerticesOfMyGraph; i++) {
            for (int j = 0; j <= NumVerticesOfMyGraph; j++) {
                if (((i < 10) && (j < 10)) || ((i < 10) && (j >= 10) && (i > 0)) || ((j < 10) && (i >= 10) && (j > 0)) || ((j >= 10) && (i >= 10))) {
                    System.out.print("0");
                }
                System.out.print(TreeDecompositionGraph[i][j]);
                System.out.print(" ");
            }
            System.out.println("");
        }
        NumParentSolutions = (int) Math.floor(Math.sqrt(NumVertices));
        ParentMatrix = new int[NumVertices + 1][NumParentSolutions];
        ChildrenMatrix = new int[NumVertices + 1][NumParentSolutions];
        System.out.println("");
        for (int i = 0; i < NumParentSolutions; i++) {
            while (UsedNodesCnt < NumVertices) {
                int num = 1 + Math.abs(RandomNode.nextInt()) % NumVertices;
                if (!RemainingNodes.get(num)) {
                    RemainingNodes.set(num);
                    ParentMatrix[UsedNodesCnt][i] = num;
                    System.out.print(ParentMatrix[UsedNodesCnt][i]);
                    System.out.print(" ");
                    ++UsedNodesCnt;
                }
            }
            for (int j = 0; j <= RemainingNodes.length(); j++) {
                RemainingNodes.clear(j);
            }
            UsedNodesCnt = 0;
            System.out.println("");
        }
        for (int n = 0; n < 1000; n++) {
            Mutate();
            Recombine();
            Select();
        }
    }

    /**
	 * @param args
	 */
    public static void main(String[] args) {
        GenTD meinGenTD = new GenTD(args[0]);
        System.out.println(" minTreeWidth: " + meinGenTD.getMinTreeWidth());
    }
}
