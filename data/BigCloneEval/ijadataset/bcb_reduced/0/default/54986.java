import java.text.NumberFormat;
import java.util.Random;
import org.apache.log4j.Logger;

/**
 * 
 * @author S. Samara  (c)2008
 * this class generate a randomly segmented hardware/software OS service
 * to control the maximum allowable number of segmentation, set the MAX_NUMBER_OF_SES   
 */
public class DSoCService {

    OptPathNode[][] optTime = null, optPower = null, optArea = null;

    ExecutionPath optTimeExPath = null, optPowerExPath = null, optAreaExPath = null;

    DRSoCFileLogger FLOGGER = new DRSoCFileLogger("c:\\DRSoC.csv");

    /**
	 * used in the get next SES function 
	 */
    private static int getNextSESIndex = 0;

    /**
	 * This is the start point of the Service
	 */
    private SES start;

    /**
	 * the end point of the Service
	 */
    private SES finish;

    /**
	 * used for generating automatic services names
	 * 
	 */
    private static int NameSec = 0;

    private int totalHardArea = 0, totalSoftArea = 0, totalHardPower = 0, totalSoftPower = 0, totalSoftExTime = 0, totalHardExTime = 0;

    private int totalOptExTime = 0, totalOptExPower = 0, totalOptHardArea = 0, totalOptSoftArea = 0, totalOptArea = 0;

    /**
	 * An array contains the hardware service segments (SES: small execution segment)
	 */
    private SES[] hx_SES;

    /**
	 * An array contains the software service segments
	 */
    private SES[] sx_SES;

    /**
	 * The service name, if not initialize an automatic generated name is used
	 */
    String str_Name;

    /**
	 * The number of (SES: small execution segment) the service consist from
	 */
    private int NUMBER_OF_SES = 0;

    /**
	 * The maximum number of SES (small execution segment) a service can partition into
	 * so every service will have  Number_OF_SES <= MAX_NUMBER_OF_SES
	 */
    private int MAX_NUMBER_OF_SES = 100;

    /**
	 * This constant is used to generate a random number for the resources needed for each SES
	 */
    private int MAX_CONSTRAINT_RANDOMIZATION = 30;

    public int getTotalHardwarePower() {
        return this.totalHardPower;
    }

    public int getTotalSoftwarePower() {
        return this.totalSoftPower;
    }

    public int getTotalHardwareArea() {
        return this.totalHardArea;
    }

    public int getTotalSoftwareArea() {
        return this.totalSoftArea;
    }

    /**
	 * logger to track and debbug
	 */
    private static final Logger LOGGER = Logger.getLogger(DSoCService.class.getName());

    /**
	 * 
	 * @return the total number of SESs in both implementations with the start and finish SES included
	 */
    public int getTotalNumOfSESs() {
        return ((NUMBER_OF_SES * 2) + 2);
    }

    /**
	 * 
	 * @return the number of SESs in one implementation including the start and the finish SESs
	 */
    public int getNumberOfSESs() {
        return NUMBER_OF_SES + 2;
    }

    /**
	 * 
	 * @param idx the index of the Hx_SES to retrun, ranges from 0 to NUMBER_OF_SES+1
	 * @return if idx==0  the START SES returned, if idx==NUMBER_OF_SES+1 the FINISH SES is returned else the hx_SES[idx] is returned
	 */
    public SES getHxSES(int idx) {
        SES retSES = null;
        if (idx <= 0) retSES = start; else if (idx >= NUMBER_OF_SES + 1) retSES = finish; else retSES = hx_SES[idx - 1];
        return retSES;
    }

    /**
	 * 
	 * @param idx the index of the Hx_SES to retrun, ranges from 0 to NUMBER_OF_SES+1
	 * @return if idx==0  the START SES returned, if idx==NUMBER_OF_SES+1 the FINISH SES is returned else the sx_SES[idx] is returned
	 */
    public SES getSxSES(int idx) {
        SES retSES = null;
        if (idx <= 0) retSES = start; else if (idx >= NUMBER_OF_SES + 1) retSES = finish; else retSES = sx_SES[idx - 1];
        return retSES;
    }

    public SES getNextSES() {
        SES retSES = null;
        if (getNextSESIndex == 0) {
            retSES = start;
        } else if (getNextSESIndex == ((NUMBER_OF_SES * 2) + 1)) {
            retSES = finish;
            getNextSESIndex = -1;
        } else if (getNextSESIndex < ((NUMBER_OF_SES * 2) + 1)) {
            if (getNextSESIndex <= NUMBER_OF_SES) retSES = sx_SES[getNextSESIndex - 1]; else {
                retSES = hx_SES[getNextSESIndex - NUMBER_OF_SES - 1];
            }
        }
        if (getNextSESIndex < ((NUMBER_OF_SES * 2) + 1)) getNextSESIndex++;
        return retSES;
    }

    /**
	 * Default Constructor. NUMBER_OF_SES is randomly chosen (Default <= MAX_NUMBER_OF_SES), service name is 
	 * automatically chosen
	 */
    public DSoCService() {
        Random rand = new Random();
        int RandNum = rand.nextInt(MAX_NUMBER_OF_SES + 1);
        NUMBER_OF_SES = (RandNum < 3) ? 3 : RandNum;
        hx_SES = new SES[NUMBER_OF_SES];
        sx_SES = new SES[NUMBER_OF_SES];
        NumberFormat nf = NumberFormat.getInstance();
        nf.setMinimumIntegerDigits(4);
        nf.setMaximumIntegerDigits(4);
        str_Name = "Service" + nf.format((long) NameSec);
        NameSec++;
        init_SESs();
    }

    /**
	 * 
	 * @param MaximumNumberOfSegmentation : this will determine that the service will have a 
	 *  number of segmentation <= MaximumNumberOfSegmentation
	 */
    public DSoCService(int MaximumNumberOfSegmentation) {
        MAX_NUMBER_OF_SES = MaximumNumberOfSegmentation;
        Random rand = new Random();
        int RandNum = rand.nextInt(MAX_NUMBER_OF_SES + 1);
        NUMBER_OF_SES = (RandNum < 3) ? 3 : RandNum;
        hx_SES = new SES[NUMBER_OF_SES];
        sx_SES = new SES[NUMBER_OF_SES];
        NumberFormat nf = NumberFormat.getInstance();
        nf.setMinimumIntegerDigits(4);
        nf.setMaximumIntegerDigits(4);
        str_Name = "Service" + nf.format((long) NameSec);
        NameSec++;
        init_SESs();
    }

    public DSoCService(int MaximumNumberOfSegmentation, String ServiceName) {
        MAX_NUMBER_OF_SES = MaximumNumberOfSegmentation;
        Random rand = new Random();
        int RandNum = rand.nextInt(MAX_NUMBER_OF_SES + 1);
        NUMBER_OF_SES = (RandNum < 3) ? 3 : RandNum;
        hx_SES = new SES[NUMBER_OF_SES];
        sx_SES = new SES[NUMBER_OF_SES];
        str_Name = ServiceName;
        init_SESs();
    }

    /**
	 * Initialize the SES with the information required for the algorithm such as
	 * WCET (Worst case Execution time), Power Estimation, Area Estimation
	 */
    private void init_SESs() {
        int tempRand;
        start = new SES(NodeType.Start, -1, 0, 0, 0);
        finish = new SES(NodeType.Finish, NUMBER_OF_SES, 0, 0, 0);
        for (int i = 0; i < NUMBER_OF_SES; i++) {
            hx_SES[i] = new SES(NodeType.Hardware, i, Tools.getIntRand(MAX_CONSTRAINT_RANDOMIZATION, 1), Tools.getIntRand(MAX_CONSTRAINT_RANDOMIZATION, 1), Tools.getIntRand(MAX_CONSTRAINT_RANDOMIZATION, 1));
            totalHardArea += hx_SES[i].getArea();
            totalHardPower += hx_SES[i].getExPower();
            sx_SES[i] = new SES(NodeType.Software, i, Tools.getIntRand(MAX_CONSTRAINT_RANDOMIZATION, 1), Tools.getIntRand(MAX_CONSTRAINT_RANDOMIZATION, 1), Tools.getIntRand(MAX_CONSTRAINT_RANDOMIZATION, 1));
            totalSoftArea += sx_SES[i].getArea();
            totalSoftPower += sx_SES[i].getExPower();
        }
        start.SetNextSoftwareSES(sx_SES[0]);
        start.SetNextHardwareSES(hx_SES[0]);
        start.setHardwareEdgeWeight(Tools.getIntRand(MAX_CONSTRAINT_RANDOMIZATION, 1));
        start.setSoftwareEdgeWeight(Tools.getIntRand(MAX_CONSTRAINT_RANDOMIZATION, 1));
        totalHardExTime += start.getHardwareEdgeWeight();
        totalSoftExTime += start.getSoftwareEdgeWeight();
        for (int i = 0; i < NUMBER_OF_SES - 1; i++) {
            hx_SES[i].SetNextHardwareSES(hx_SES[i + 1]);
            hx_SES[i].SetNextSoftwareSES(sx_SES[i + 1]);
            hx_SES[i].setHardwareEdgeWeight(Tools.getIntRand(MAX_CONSTRAINT_RANDOMIZATION, 1));
            hx_SES[i].setSoftwareEdgeWeight(Tools.getIntRand(MAX_CONSTRAINT_RANDOMIZATION, 1));
            totalHardExTime += hx_SES[i].getExTime() + hx_SES[i].getHardwareEdgeWeight();
            sx_SES[i].SetNextHardwareSES(hx_SES[i + 1]);
            sx_SES[i].SetNextSoftwareSES(sx_SES[i + 1]);
            sx_SES[i].setHardwareEdgeWeight(Tools.getIntRand(MAX_CONSTRAINT_RANDOMIZATION, 1));
            sx_SES[i].setSoftwareEdgeWeight(Tools.getIntRand(MAX_CONSTRAINT_RANDOMIZATION, 1));
            totalSoftExTime += sx_SES[i].getExTime() + sx_SES[i].getSoftwareEdgeWeight();
        }
        hx_SES[NUMBER_OF_SES - 1].SetNextHardwareSES(finish);
        hx_SES[NUMBER_OF_SES - 1].SetNextSoftwareSES(finish);
        tempRand = Tools.getIntRand(MAX_CONSTRAINT_RANDOMIZATION, 1);
        hx_SES[NUMBER_OF_SES - 1].setHardwareEdgeWeight(tempRand);
        hx_SES[NUMBER_OF_SES - 1].setSoftwareEdgeWeight(tempRand);
        totalHardExTime += tempRand + hx_SES[NUMBER_OF_SES - 1].getExTime();
        sx_SES[NUMBER_OF_SES - 1].SetNextHardwareSES(finish);
        sx_SES[NUMBER_OF_SES - 1].SetNextSoftwareSES(finish);
        tempRand = Tools.getIntRand(MAX_CONSTRAINT_RANDOMIZATION, 1);
        sx_SES[NUMBER_OF_SES - 1].setHardwareEdgeWeight(tempRand);
        sx_SES[NUMBER_OF_SES - 1].setSoftwareEdgeWeight(tempRand);
        totalSoftExTime += tempRand + sx_SES[NUMBER_OF_SES - 1].getExTime();
        ;
    }

    public void PrintServiceInformation() {
        int i = 0;
        System.out.printf("SES%-4s,%-5s,%-5s,%-5s,       ,%-5s,%-5s,%-5s", "xxx", "xxx", "xxx", "xxx", "xxx", "xxx", "xxx");
        for (i = 0; i < NUMBER_OF_SES; i++) {
            System.out.printf("SES%-4d,%-5d,%-5d,%-5d,       ,%-5d,%-5d,%-5d", i, hx_SES[i].getExTime(), hx_SES[i].getArea(), hx_SES[i].getExPower(), sx_SES[i].getExTime(), sx_SES[i].getArea(), sx_SES[i].getExPower());
        }
    }

    public void FindOptimalPath(ExecutionPath optExecutionPath, OptPathNode[][] optPathArray, DecisionConstraint x) {
        int DCs = 0;
        int DCh = 0;
        int DC1 = 0, DC2 = 0;
        int EXTs = 0, EXTh = 0;
        int total_S_Power, total_S_Time, total_S_Area, total_H_Power, total_H_Time, total_H_Area;
        optPathArray[0][0].setPreviousExecutedNodeType(NodeType.Start);
        optPathArray[0][0].setTotalExTime(hx_SES[0].getExTime() + start.getHardwareEdgeWeight());
        optPathArray[0][0].setTotalArea(hx_SES[0].getArea());
        optPathArray[0][0].setTotalExPower(hx_SES[0].getExPower());
        optPathArray[0][0].setDecisionConstraintType(x);
        optPathArray[0][1].setPreviousExecutedNodeType(NodeType.Start);
        optPathArray[0][1].setTotalExTime(sx_SES[0].getExTime() + start.getSoftwareEdgeWeight());
        optPathArray[0][1].setTotalArea(sx_SES[0].getArea());
        optPathArray[0][1].setTotalExPower(sx_SES[0].getExPower());
        optPathArray[0][1].setDecisionConstraintType(x);
        int i = 1;
        for (i = 1; i < NUMBER_OF_SES; i++) {
            optPathArray[i][0].setDecisionConstraintType(x);
            optPathArray[i][1].setDecisionConstraintType(x);
            if (x == DecisionConstraint.DC_AREA) {
                DCs = sx_SES[i].getArea();
                DCh = hx_SES[i].getArea();
            } else if (x == DecisionConstraint.DC_EXECUTION_POWER) {
                DCs = sx_SES[i].getExPower();
                DCh = hx_SES[i].getExPower();
            } else {
                DCs = sx_SES[i].getExTime() + hx_SES[i - 1].getSoftwareEdgeWeight();
                DCh = hx_SES[i].getExTime() + sx_SES[i - 1].getHardwareEdgeWeight();
            }
            if (x == DecisionConstraint.DC_EXECUTION_TIME) {
                DC1 = DCs - hx_SES[i - 1].getSoftwareEdgeWeight() + sx_SES[i - 1].getSoftwareEdgeWeight();
                DC2 = DCs;
            } else {
                DC1 = DCs;
                DC2 = DCs;
            }
            if (optPathArray[i - 1][1].getDecisionConstraint(x) + DC1 <= optPathArray[i - 1][0].getDecisionConstraint(x) + DC2) {
                optPathArray[i][1].setDecisionConstraint(optPathArray[i - 1][1].getDecisionConstraint(x) + DC1);
                optPathArray[i][1].setPreviousExecutedNodeType(NodeType.Software);
                EXTs = sx_SES[i - 1].getSoftwareEdgeWeight() + sx_SES[i].getExTime();
                total_S_Power = optPathArray[i - 1][1].getTotalExPower();
                total_S_Time = optPathArray[i - 1][1].getTotalExTime();
                total_S_Area = optPathArray[i - 1][1].getTotalArea();
            } else {
                optPathArray[i][1].setDecisionConstraint(optPathArray[i - 1][0].getDecisionConstraint(x) + DC2);
                optPathArray[i][1].setPreviousExecutedNodeType(NodeType.Hardware);
                EXTs = hx_SES[i - 1].getSoftwareEdgeWeight() + sx_SES[i].getExTime();
                total_S_Power = optPathArray[i - 1][0].getTotalExPower();
                total_S_Time = optPathArray[i - 1][0].getTotalExTime();
                total_S_Area = optPathArray[i - 1][0].getTotalArea();
            }
            if (x == DecisionConstraint.DC_EXECUTION_TIME) {
                DC1 = DCh - sx_SES[i - 1].getHardwareEdgeWeight() + hx_SES[i - 1].getHardwareEdgeWeight();
                DC2 = DCh;
            } else {
                DC1 = DCh;
                DC2 = DCh;
            }
            if (optPathArray[i - 1][0].getDecisionConstraint(x) + DC1 <= optPathArray[i - 1][1].getDecisionConstraint(x) + DC2) {
                optPathArray[i][0].setDecisionConstraint(optPathArray[i - 1][0].getDecisionConstraint(x) + DC1);
                optPathArray[i][0].setPreviousExecutedNodeType(NodeType.Hardware);
                EXTh = hx_SES[i - 1].getHardwareEdgeWeight() + hx_SES[i].getExTime();
                total_H_Power = optPathArray[i - 1][0].getTotalExPower();
                total_H_Time = optPathArray[i - 1][0].getTotalExTime();
                total_H_Area = optPathArray[i - 1][0].getTotalArea();
            } else {
                optPathArray[i][0].setDecisionConstraint(optPathArray[i - 1][1].getDecisionConstraint(x) + DC2);
                optPathArray[i][0].setPreviousExecutedNodeType(NodeType.Software);
                EXTh = sx_SES[i - 1].getHardwareEdgeWeight() + hx_SES[i].getExTime();
                total_H_Power = optPathArray[i - 1][1].getTotalExPower();
                total_H_Time = optPathArray[i - 1][1].getTotalExTime();
                total_H_Area = optPathArray[i - 1][1].getTotalArea();
            }
            if (x == DecisionConstraint.DC_EXECUTION_TIME) {
                optPathArray[i][0].setTotalExPower(total_H_Power + hx_SES[i].getExPower());
                optPathArray[i][0].setTotalArea(total_H_Area + hx_SES[i].getArea());
                optPathArray[i][1].setTotalExPower(total_S_Power + sx_SES[i].getExPower());
                optPathArray[i][1].setTotalArea(total_S_Area + sx_SES[i].getArea());
            } else if (x == DecisionConstraint.DC_AREA) {
                optPathArray[i][0].setTotalExPower(total_H_Power + hx_SES[i].getExPower());
                optPathArray[i][0].setTotalExTime(total_H_Time + EXTh);
                optPathArray[i][1].setTotalExPower(total_S_Power + sx_SES[i].getExPower());
                optPathArray[i][1].setTotalExTime(total_S_Time + EXTs);
            } else {
                optPathArray[i][0].setTotalArea(total_H_Area + hx_SES[i].getArea());
                optPathArray[i][0].setTotalExTime(total_H_Time + EXTh);
                optPathArray[i][1].setTotalArea(total_S_Area + sx_SES[i].getArea());
                optPathArray[i][1].setTotalExTime(total_S_Time + EXTs);
            }
        }
        if (optPathArray[i - 1][0].getTotalExTime() + hx_SES[i - 1].getHardwareEdgeWeight() < optPathArray[i - 1][1].getTotalExTime() + sx_SES[i - 1].getHardwareEdgeWeight()) {
            optPathArray[i][0].setDecisionConstraintType(x);
            optPathArray[i][0].setPreviousExecutedNodeType(NodeType.Hardware);
            optPathArray[i][0].setTotalArea(optPathArray[i - 1][0].getTotalArea());
            optPathArray[i][0].setTotalExTime(optPathArray[i - 1][0].getTotalExTime() + hx_SES[i - 1].getHardwareEdgeWeight());
            optPathArray[i][0].setTotalExPower(optPathArray[i - 1][0].getTotalExPower());
            optPathArray[i][1].setDecisionConstraintType(x);
            optPathArray[i][1].setPreviousExecutedNodeType(NodeType.Hardware);
            optPathArray[i][1].setTotalArea(optPathArray[i - 1][0].getTotalArea());
            optPathArray[i][1].setTotalExTime(optPathArray[i - 1][0].getTotalExTime() + hx_SES[i - 1].getHardwareEdgeWeight());
            optPathArray[i][1].setTotalExPower(optPathArray[i - 1][0].getTotalExPower());
        } else {
            optPathArray[i][0].setDecisionConstraintType(x);
            optPathArray[i][0].setPreviousExecutedNodeType(NodeType.Software);
            optPathArray[i][0].setTotalArea(optPathArray[i - 1][0].getTotalArea());
            optPathArray[i][0].setTotalExTime(optPathArray[i - 1][0].getTotalExTime() + sx_SES[i - 1].getHardwareEdgeWeight());
            optPathArray[i][0].setTotalExPower(optPathArray[i - 1][0].getTotalExPower());
            optPathArray[i][1].setDecisionConstraintType(x);
            optPathArray[i][1].setPreviousExecutedNodeType(NodeType.Software);
            optPathArray[i][1].setTotalArea(optPathArray[i - 1][0].getTotalArea());
            optPathArray[i][1].setTotalExTime(optPathArray[i - 1][0].getTotalExTime() + sx_SES[i - 1].getHardwareEdgeWeight());
            optPathArray[i][1].setTotalExPower(optPathArray[i - 1][0].getTotalExPower());
        }
        OptPathNode testNode = optPathArray[NUMBER_OF_SES][0];
        for (int j = NUMBER_OF_SES; j > 0; j--) {
            if (testNode.getPreviousExecutedNodeType() == NodeType.Hardware) {
                optExecutionPath.addSESToExPath(hx_SES[j - 1]);
                testNode = optPathArray[j - 1][0];
            } else {
                optExecutionPath.addSESToExPath(sx_SES[j - 1]);
                testNode = optPathArray[j - 1][1];
            }
        }
        optExecutionPath.CompleteCalculation();
    }

    private void LogChangeOut(DRSoCFileLogger FLOGGER1, SES ex_path_ses[], int ServiceID) {
        if (ex_path_ses == null) {
            LOGGER.info("LOGOUT function: argument is null");
            return;
        } else if (ex_path_ses.length != NUMBER_OF_SES) {
            LOGGER.info("LOGOUT function: length mismatch");
            return;
        }
        ExecutionPath ex_pth = new ExecutionPath(start);
        for (int xx = 0; xx < NUMBER_OF_SES; xx++) {
            ex_pth.addSESToExPath(ex_path_ses[xx]);
        }
        ex_pth.CompleteCalculation();
        FLOGGER1.LOGExcel((int) ex_pth.getTotalArea() + ";" + (int) ex_pth.getTotalExTime() + ";" + (int) ex_pth.getTotalExPower());
        LOGGER.info("Service ID: " + ServiceID + " has Final (FPa, FPt, FPp): (" + ex_pth.getTotalArea() + ", " + ex_pth.getTotalExTime() + ", " + ex_pth.getTotalExPower() + ")");
    }

    public void getSuitableExecutionPath(ExecutionInfo ei) {
        DRSoCFileLogger FLOGGER1 = new DRSoCFileLogger("c:\\DRSoCLogFiles\\DRSoC" + ei.getServiceID() + ".csv");
        FLOGGER1.LOGExcel("Area;Time;Power");
        int tempTime = 0;
        int tempHardArea = 0;
        int tempSoftArea = 0;
        int tempPower = 0;
        int tempArea = 0;
        int NumberOfExchange = 0;
        int totalTimeAfterExchange = 0;
        int totalHardAreaAfterExchange = 0;
        int totalSoftAreaAfterExchange = 0;
        int totalPowerAfterExchange = 0;
        int oldPreEdgeWieght = 0;
        int newPreEdgeWieght = 0;
        int oldNexEdgeWieght = 0;
        int newNexEdgeWieght = 0;
        int oldHardArea = 0;
        int newHardArea = 0;
        int oldSoftArea = 0;
        int newSoftArea = 0;
        int oldPower = 0;
        int newPower = 0;
        int NumOfPaths = 3;
        while (--NumOfPaths >= 0) {
            SES old_curr_ses = null;
            SES new_curr_ses = null;
            SES previous_ses = null;
            SES next_ses = null;
            ExecutionPath ex_pth = new ExecutionPath(start);
            ExecutionPath ex_pth1 = new ExecutionPath(start);
            ExecutionPath FirstPath = null;
            ExecutionPath SecondPath = null;
            ExecutionPath ThirdPath = null;
            SES ex_path_ses[] = new SES[NUMBER_OF_SES];
            SES ex_path_ses1[] = new SES[NUMBER_OF_SES];
            optTimeExPath = new ExecutionPath(start);
            optAreaExPath = new ExecutionPath(start);
            optPowerExPath = new ExecutionPath(start);
            optTime = new OptPathNode[NUMBER_OF_SES + 1][2];
            optArea = new OptPathNode[NUMBER_OF_SES + 1][2];
            optPower = new OptPathNode[NUMBER_OF_SES + 1][2];
            for (int i = 0; i <= NUMBER_OF_SES; i++) {
                optTime[i][0] = new OptPathNode(i);
                optTime[i][1] = new OptPathNode(i);
                optArea[i][0] = new OptPathNode(i);
                optArea[i][1] = new OptPathNode(i);
                optPower[i][0] = new OptPathNode(i);
                optPower[i][1] = new OptPathNode(i);
            }
            this.FindOptimalPath(optTimeExPath, optTime, DecisionConstraint.DC_EXECUTION_TIME);
            this.FindOptimalPath(optAreaExPath, optArea, DecisionConstraint.DC_AREA);
            this.FindOptimalPath(optPowerExPath, optPower, DecisionConstraint.DC_EXECUTION_POWER);
            if (DSoCConfig.FirstSimulation || DSoCConfig.ParetoOptimumSimulation) {
                ei.setExpectedResponseTime((int) (DSoCConfig.responseTimePercentage * optTimeExPath.getTotalExTime()));
                ei.setPower((int) (DSoCConfig.powerPercentage * optPowerExPath.getTotalExPower()));
                ei.setSoftArea((int) (DSoCConfig.softwareAreaPercentage * optAreaExPath.getTotalArea()));
                ei.setHardArea((int) (DSoCConfig.hardwareAreaPercentage * optAreaExPath.getTotalArea()));
            }
            if (DSoCConfig.ParetoOptimumSimulation) {
                LOGGER.info("Service ID: " + ei.getServiceID() + " has an Optimal Area, Ex_Time, Cons_Power given as\n =========================================================>(optA, optT, optP): (" + optAreaExPath.getTotalArea() + ", " + optTimeExPath.getTotalExTime() + ", " + optPowerExPath.getTotalExPower() + ")");
                LOGGER.info("Number of Partitioning (SESs): " + NUMBER_OF_SES);
                LOGGER.info("Area  opt Ex Path Info (As,Ah, A, T, P): (" + optAreaExPath.getTotalExSoftwareArea() + ", " + optAreaExPath.getTotalExHardwareArea() + ", " + optAreaExPath.getTotalArea() + ", " + optAreaExPath.getTotalExTime() + ", " + optAreaExPath.getTotalExPower() + ")");
                FLOGGER.LOGExcel(optAreaExPath.getTotalExSoftwareArea() + ";" + optAreaExPath.getTotalExHardwareArea() + ";" + (int) optAreaExPath.getTotalArea() + ";" + (int) optAreaExPath.getTotalExTime() + ";" + (int) optAreaExPath.getTotalExPower());
                LOGGER.info("Time  opt Ex Path Info (As,Ah, A, T, P): (" + optTimeExPath.getTotalExSoftwareArea() + ", " + optTimeExPath.getTotalExHardwareArea() + ", " + optTimeExPath.getTotalArea() + ", " + optTimeExPath.getTotalExTime() + ", " + optTimeExPath.getTotalExPower() + ")");
                LOGGER.info("Power opt Ex Path Info (As,Ah, A, T, P): (" + optPowerExPath.getTotalExSoftwareArea() + ", " + optPowerExPath.getTotalExHardwareArea() + ", " + optPowerExPath.getTotalArea() + ", " + optPowerExPath.getTotalExTime() + ", " + optPowerExPath.getTotalExPower() + ")");
            }
            totalTimeAfterExchange = (int) optTimeExPath.getTotalExTime();
            totalHardAreaAfterExchange = optTimeExPath.getTotalExHardwareArea();
            totalSoftAreaAfterExchange = optTimeExPath.getTotalExSoftwareArea();
            totalPowerAfterExchange = (int) optTimeExPath.getTotalExPower();
            if (ei.getExpectedResponseTime() > optTimeExPath.getTotalExTime()) {
                if (DSoCConfig.ParetoOptimumSimulation) {
                    if (NumOfPaths == 2) {
                        FLOGGER1.LOGExcel("Time;Area;Power");
                        FirstPath = optTimeExPath;
                        SecondPath = optAreaExPath;
                        ThirdPath = optPowerExPath;
                    } else if (NumOfPaths == 1) {
                        FLOGGER1.LOGExcel("Area;Time;Power");
                        FirstPath = optAreaExPath;
                        SecondPath = optTimeExPath;
                        ThirdPath = optPowerExPath;
                    } else if (NumOfPaths == 0) {
                        FLOGGER1.LOGExcel("Power;Time;Area");
                        FirstPath = optPowerExPath;
                        SecondPath = optTimeExPath;
                        ThirdPath = optAreaExPath;
                    }
                    tempTime = (int) FirstPath.getTotalExTime();
                    tempPower = (int) FirstPath.getTotalExPower();
                    tempSoftArea = (int) FirstPath.getTotalExSoftwareArea();
                    tempHardArea = (int) FirstPath.getTotalExHardwareArea();
                    tempArea = tempSoftArea + tempHardArea;
                    for (int x = 0; x < NUMBER_OF_SES; x++) {
                        ex_path_ses[x] = FirstPath.getSES(x);
                        ex_path_ses1[x] = FirstPath.getSES(x);
                    }
                }
                for (int j = NUMBER_OF_SES - 1; j >= 0 && ei.getExpectedResponseTime() >= totalTimeAfterExchange; j--) {
                    oldPreEdgeWieght = 0;
                    newPreEdgeWieght = 0;
                    oldNexEdgeWieght = 0;
                    newNexEdgeWieght = 0;
                    oldHardArea = 0;
                    newHardArea = 0;
                    oldSoftArea = 0;
                    newSoftArea = 0;
                    oldPower = 0;
                    newPower = 0;
                    if (DSoCConfig.FirstSimulation) {
                        old_curr_ses = optTimeExPath.getSES(j);
                        if (old_curr_ses.getType() == NodeType.Software) ex_path_ses[j] = old_curr_ses; else {
                            if (j == 0) previous_ses = start; else previous_ses = optTimeExPath.getSES(j - 1);
                            if (old_curr_ses.getType() == NodeType.Software) {
                                oldPreEdgeWieght = previous_ses.getSoftwareEdgeWeight();
                                new_curr_ses = hx_SES[j];
                                newPreEdgeWieght = previous_ses.getHardwareEdgeWeight();
                                oldSoftArea = old_curr_ses.getArea();
                                newHardArea = new_curr_ses.getArea();
                            } else {
                                oldPreEdgeWieght = previous_ses.getHardwareEdgeWeight();
                                new_curr_ses = sx_SES[j];
                                newPreEdgeWieght = previous_ses.getSoftwareEdgeWeight();
                                oldHardArea = old_curr_ses.getArea();
                                newSoftArea = new_curr_ses.getArea();
                            }
                            if (j == NUMBER_OF_SES - 1) {
                                next_ses = null;
                                oldNexEdgeWieght = old_curr_ses.getHardwareEdgeWeight();
                                newNexEdgeWieght = new_curr_ses.getHardwareEdgeWeight();
                            } else {
                                next_ses = optTimeExPath.getSES(j + 1);
                                if (next_ses.getType() == NodeType.Software) {
                                    oldNexEdgeWieght = old_curr_ses.getSoftwareEdgeWeight();
                                    newNexEdgeWieght = new_curr_ses.getSoftwareEdgeWeight();
                                } else {
                                    oldNexEdgeWieght = old_curr_ses.getHardwareEdgeWeight();
                                    newNexEdgeWieght = new_curr_ses.getHardwareEdgeWeight();
                                }
                            }
                            tempTime = totalTimeAfterExchange + new_curr_ses.getExTime() + newPreEdgeWieght + newNexEdgeWieght - oldPreEdgeWieght - oldNexEdgeWieght - old_curr_ses.getExTime();
                            tempHardArea = totalHardAreaAfterExchange - oldHardArea + newHardArea;
                            tempSoftArea = totalSoftAreaAfterExchange - oldSoftArea + newSoftArea;
                            tempPower = totalPowerAfterExchange - old_curr_ses.getExPower() + new_curr_ses.getExPower();
                            if (tempTime < ei.expected_response) {
                                if (tempSoftArea <= ei.getSoftwareArea() && tempHardArea <= ei.getHardwareArea()) {
                                    if (tempPower <= ei.getRemainingPower()) {
                                        totalTimeAfterExchange = tempTime;
                                        totalHardAreaAfterExchange = tempHardArea;
                                        totalSoftAreaAfterExchange = tempSoftArea;
                                        totalPowerAfterExchange = tempPower;
                                        ex_path_ses[j] = new_curr_ses;
                                        NumberOfExchange++;
                                    } else {
                                        ex_path_ses[j] = old_curr_ses;
                                    }
                                } else {
                                    ex_path_ses[j] = old_curr_ses;
                                }
                            } else {
                                ex_path_ses[j] = old_curr_ses;
                            }
                        }
                    } else if (DSoCConfig.ParetoOptimumSimulation) {
                        old_curr_ses = FirstPath.getSES(j);
                        if (FirstPath.getSES(j).getType() == SecondPath.getSES(j).getType()) {
                            ex_path_ses[j] = old_curr_ses;
                            ex_path_ses1[j] = old_curr_ses;
                            continue;
                        }
                        if (j == 0) previous_ses = start; else previous_ses = FirstPath.getSES(j - 1);
                        if (old_curr_ses.getType() == NodeType.Software) {
                            oldPreEdgeWieght = previous_ses.getSoftwareEdgeWeight();
                            new_curr_ses = hx_SES[j];
                            newPreEdgeWieght = previous_ses.getHardwareEdgeWeight();
                            oldSoftArea = old_curr_ses.getArea();
                            newHardArea = new_curr_ses.getArea();
                        } else {
                            oldPreEdgeWieght = previous_ses.getHardwareEdgeWeight();
                            new_curr_ses = sx_SES[j];
                            newPreEdgeWieght = previous_ses.getSoftwareEdgeWeight();
                            oldHardArea = old_curr_ses.getArea();
                            newSoftArea = new_curr_ses.getArea();
                        }
                        if (j == NUMBER_OF_SES - 1) {
                            next_ses = null;
                            oldNexEdgeWieght = old_curr_ses.getHardwareEdgeWeight();
                            newNexEdgeWieght = new_curr_ses.getHardwareEdgeWeight();
                        } else {
                            next_ses = ex_path_ses[j + 1];
                            if (next_ses.getType() == NodeType.Software) {
                                oldNexEdgeWieght = old_curr_ses.getSoftwareEdgeWeight();
                                newNexEdgeWieght = new_curr_ses.getSoftwareEdgeWeight();
                            } else {
                                oldNexEdgeWieght = old_curr_ses.getHardwareEdgeWeight();
                                newNexEdgeWieght = new_curr_ses.getHardwareEdgeWeight();
                            }
                        }
                        if (FirstPath == optTimeExPath) {
                            if (SecondPath == optAreaExPath) {
                                if (tempArea > (tempArea - old_curr_ses.getArea() + new_curr_ses.getArea())) {
                                    if (tempPower > (tempPower - old_curr_ses.getExPower() + new_curr_ses.getExPower())) {
                                        tempArea = tempArea - old_curr_ses.getArea() + new_curr_ses.getArea();
                                        tempPower = tempPower - old_curr_ses.getExPower() + new_curr_ses.getExPower();
                                        tempTime = tempTime - new_curr_ses.getExTime() + newPreEdgeWieght + newNexEdgeWieght - oldPreEdgeWieght - oldNexEdgeWieght - old_curr_ses.getExTime();
                                        ex_path_ses[j] = new_curr_ses;
                                        LogChangeOut(FLOGGER1, ex_path_ses, ei.getServiceID());
                                    } else {
                                        ex_path_ses[j] = old_curr_ses;
                                    }
                                } else {
                                    ex_path_ses[j] = old_curr_ses;
                                }
                            } else {
                                if (tempPower > (tempPower - old_curr_ses.getExPower() + new_curr_ses.getExPower())) {
                                    if (tempArea > (tempArea - old_curr_ses.getArea() + new_curr_ses.getArea())) {
                                        tempArea = tempArea - old_curr_ses.getArea() + new_curr_ses.getArea();
                                        tempPower = tempPower - old_curr_ses.getExPower() + new_curr_ses.getExPower();
                                        tempTime = tempTime - new_curr_ses.getExTime() + newPreEdgeWieght + newNexEdgeWieght - oldPreEdgeWieght - oldNexEdgeWieght - old_curr_ses.getExTime();
                                        ex_path_ses[j] = new_curr_ses;
                                        LogChangeOut(FLOGGER1, ex_path_ses, ei.getServiceID());
                                    } else {
                                        ex_path_ses[j] = old_curr_ses;
                                        LOGGER.info("Exchange Time optimal SES " + j + " improves power but not area");
                                    }
                                }
                            }
                        } else if (FirstPath == optAreaExPath) {
                            if (SecondPath == optTimeExPath) {
                                if (tempTime > (tempTime - new_curr_ses.getExTime() + newPreEdgeWieght + newNexEdgeWieght - oldPreEdgeWieght - oldNexEdgeWieght - old_curr_ses.getExTime())) {
                                    if (tempPower > (tempPower - old_curr_ses.getExPower() + new_curr_ses.getExPower())) {
                                        tempArea = tempArea - old_curr_ses.getArea() + new_curr_ses.getArea();
                                        tempPower = tempPower - old_curr_ses.getExPower() + new_curr_ses.getExPower();
                                        tempTime = tempTime - new_curr_ses.getExTime() + newPreEdgeWieght + newNexEdgeWieght - oldPreEdgeWieght - oldNexEdgeWieght - old_curr_ses.getExTime();
                                        ex_path_ses[j] = new_curr_ses;
                                        LogChangeOut(FLOGGER1, ex_path_ses, ei.getServiceID());
                                    } else {
                                        ex_path_ses[j] = old_curr_ses;
                                    }
                                } else {
                                    ex_path_ses[j] = old_curr_ses;
                                }
                            } else {
                                if (tempPower > (tempPower - old_curr_ses.getExPower() + new_curr_ses.getExPower())) {
                                    if (tempTime > (tempTime - new_curr_ses.getExTime() + newPreEdgeWieght + newNexEdgeWieght - oldPreEdgeWieght - oldNexEdgeWieght - old_curr_ses.getExTime())) {
                                        tempArea = tempArea - old_curr_ses.getArea() + new_curr_ses.getArea();
                                        tempPower = tempPower - old_curr_ses.getExPower() + new_curr_ses.getExPower();
                                        tempTime = tempTime - new_curr_ses.getExTime() + newPreEdgeWieght + newNexEdgeWieght - oldPreEdgeWieght - oldNexEdgeWieght - old_curr_ses.getExTime();
                                        ex_path_ses[j] = new_curr_ses;
                                        LogChangeOut(FLOGGER1, ex_path_ses, ei.getServiceID());
                                    } else {
                                        ex_path_ses[j] = old_curr_ses;
                                    }
                                } else {
                                    ex_path_ses[j] = old_curr_ses;
                                }
                            }
                        } else {
                            if (SecondPath == optTimeExPath) {
                                if (tempTime > (tempTime - new_curr_ses.getExTime() + newPreEdgeWieght + newNexEdgeWieght - oldPreEdgeWieght - oldNexEdgeWieght - old_curr_ses.getExTime())) {
                                    if (tempArea > (tempArea - old_curr_ses.getArea() + new_curr_ses.getArea())) {
                                        tempArea = tempArea - old_curr_ses.getArea() + new_curr_ses.getArea();
                                        tempPower = tempPower - old_curr_ses.getExPower() + new_curr_ses.getExPower();
                                        tempTime = tempTime - new_curr_ses.getExTime() + newPreEdgeWieght + newNexEdgeWieght - oldPreEdgeWieght - oldNexEdgeWieght - old_curr_ses.getExTime();
                                        ex_path_ses[j] = new_curr_ses;
                                        LogChangeOut(FLOGGER1, ex_path_ses, ei.getServiceID());
                                    } else {
                                        ex_path_ses[j] = old_curr_ses;
                                    }
                                } else {
                                    ex_path_ses[j] = old_curr_ses;
                                }
                            } else {
                                if (tempArea > (tempArea - old_curr_ses.getArea() + new_curr_ses.getArea())) {
                                    if (tempTime > (tempTime - new_curr_ses.getExTime() + newPreEdgeWieght + newNexEdgeWieght - oldPreEdgeWieght - oldNexEdgeWieght - old_curr_ses.getExTime())) {
                                        tempArea = tempArea - old_curr_ses.getArea() + new_curr_ses.getArea();
                                        tempPower = tempPower - old_curr_ses.getExPower() + new_curr_ses.getExPower();
                                        tempTime = tempTime - new_curr_ses.getExTime() + newPreEdgeWieght + newNexEdgeWieght - oldPreEdgeWieght - oldNexEdgeWieght - old_curr_ses.getExTime();
                                        ex_path_ses[j] = new_curr_ses;
                                        LogChangeOut(FLOGGER1, ex_path_ses, ei.getServiceID());
                                    } else {
                                        ex_path_ses[j] = old_curr_ses;
                                    }
                                } else {
                                    ex_path_ses[j] = old_curr_ses;
                                }
                            }
                        }
                    }
                    if (false && DSoCConfig.ParetoOptimumSimulation) {
                        tempTime = (int) FirstPath.getTotalExTime();
                        tempPower = (int) FirstPath.getTotalExPower();
                        tempSoftArea = (int) FirstPath.getTotalExSoftwareArea();
                        tempHardArea = (int) FirstPath.getTotalExHardwareArea();
                        tempArea = tempSoftArea + tempHardArea;
                        old_curr_ses = FirstPath.getSES(j);
                        if (FirstPath.getSES(j).getType() == SecondPath.getSES(j).getType() || FirstPath.getSES(j).getType() == ThirdPath.getSES(j).getType()) {
                            ex_path_ses1[j] = old_curr_ses;
                            continue;
                        }
                        if (j == 0) previous_ses = start; else previous_ses = FirstPath.getSES(j - 1);
                        if (old_curr_ses.getType() == NodeType.Software) {
                            oldPreEdgeWieght = previous_ses.getSoftwareEdgeWeight();
                            new_curr_ses = hx_SES[j];
                            newPreEdgeWieght = previous_ses.getHardwareEdgeWeight();
                            oldSoftArea = old_curr_ses.getArea();
                            newHardArea = new_curr_ses.getArea();
                        } else {
                            oldPreEdgeWieght = previous_ses.getHardwareEdgeWeight();
                            new_curr_ses = sx_SES[j];
                            newPreEdgeWieght = previous_ses.getSoftwareEdgeWeight();
                            oldHardArea = old_curr_ses.getArea();
                            newSoftArea = new_curr_ses.getArea();
                        }
                        if (j == NUMBER_OF_SES - 1) {
                            next_ses = null;
                            oldNexEdgeWieght = old_curr_ses.getHardwareEdgeWeight();
                            newNexEdgeWieght = new_curr_ses.getHardwareEdgeWeight();
                        } else {
                            next_ses = ex_path_ses1[j + 1];
                            if (next_ses.getType() == NodeType.Software) {
                                oldNexEdgeWieght = old_curr_ses.getSoftwareEdgeWeight();
                                newNexEdgeWieght = new_curr_ses.getSoftwareEdgeWeight();
                            } else {
                                oldNexEdgeWieght = old_curr_ses.getHardwareEdgeWeight();
                                newNexEdgeWieght = new_curr_ses.getHardwareEdgeWeight();
                            }
                        }
                        if (FirstPath == optTimeExPath) {
                            if (SecondPath == optAreaExPath) {
                                if (tempArea > (tempArea - old_curr_ses.getArea() + new_curr_ses.getArea())) {
                                    if (tempPower > (tempPower - old_curr_ses.getExPower() + new_curr_ses.getExPower())) {
                                        tempArea = tempArea - old_curr_ses.getArea() + new_curr_ses.getArea();
                                        tempPower = tempPower - old_curr_ses.getExPower() + new_curr_ses.getExPower();
                                        tempTime = tempTime - new_curr_ses.getExTime() + newPreEdgeWieght + newNexEdgeWieght - oldPreEdgeWieght - oldNexEdgeWieght - old_curr_ses.getExTime();
                                        ex_path_ses1[j] = new_curr_ses;
                                    } else {
                                        ex_path_ses1[j] = old_curr_ses;
                                    }
                                } else {
                                    ex_path_ses1[j] = old_curr_ses;
                                }
                            } else {
                                if (tempPower > (tempPower - old_curr_ses.getExPower() + new_curr_ses.getExPower())) {
                                    if (tempArea > (tempArea - old_curr_ses.getArea() + new_curr_ses.getArea())) {
                                        LOGGER.info("Exchange Time optimal SES " + j + " improves both power and area");
                                    } else {
                                        LOGGER.info("Exchange Time optimal SES " + j + " improves power but not area");
                                    }
                                }
                            }
                        } else if (FirstPath == optAreaExPath) {
                            if (SecondPath == optTimeExPath) {
                            } else {
                            }
                        } else {
                            if (SecondPath == optTimeExPath) {
                            } else {
                            }
                        }
                    }
                }
                for (int xx = 0; xx < NUMBER_OF_SES; xx++) {
                    ex_pth.addSESToExPath(ex_path_ses[xx]);
                }
                ex_pth.CompleteCalculation();
                LOGGER.info("Service ID: " + ei.getServiceID() + " has Final (FPa, FPt, FPp): (" + ex_pth.getTotalArea() + ", " + ex_pth.getTotalExTime() + ", " + ex_pth.getTotalExPower() + ")");
                for (int xx = 0; xx < NUMBER_OF_SES; xx++) {
                    ex_pth1.addSESToExPath(ex_path_ses1[xx]);
                }
                ex_pth1.CompleteCalculation();
                LOGGER.info("Service ID: " + ei.getServiceID() + " has Final112 (FPa, FPt, FPp): (" + ex_pth1.getTotalArea() + ", " + ex_pth1.getTotalExTime() + ", " + ex_pth1.getTotalExPower() + ")");
            }
            if (NumberOfExchange == 0) {
                if (ex_pth.getTotalExHardwareArea() <= ei.getHardwareArea() && ex_pth.getTotalExSoftwareArea() <= ei.getSoftwareArea()) {
                    if (ex_pth.getTotalExTime() <= ei.getExpectedResponseTime() && ex_pth.getTotalExPower() <= ei.getRemainingPower()) {
                        if (DSoCConfig.FirstSimulation) {
                            LOGGER.debug("************************************************");
                            LOGGER.debug("One feasible execution path found with ");
                            LOGGER.debug("Hardware Area Persentage = " + DSoCConfig.hardwareAreaPercentage);
                            LOGGER.debug("Software Area Persentage = " + DSoCConfig.softwareAreaPercentage);
                            LOGGER.debug("Power Persentage = " + DSoCConfig.powerPercentage);
                            LOGGER.debug("Response Time Percentage Persentage = " + DSoCConfig.responseTimePercentage);
                            LOGGER.debug("************************************************");
                        }
                    }
                }
            }
            if (NumberOfExchange > 0) {
                if (ex_pth.getTotalExHardwareArea() <= ei.getHardwareArea() && ex_pth.getTotalExSoftwareArea() <= ei.getSoftwareArea()) {
                    if (ex_pth.getTotalExTime() <= ei.getExpectedResponseTime() && ex_pth.getTotalExPower() <= ei.getRemainingPower()) {
                        if (DSoCConfig.FirstSimulation) {
                            LOGGER.debug("************************************************");
                            LOGGER.debug(NumberOfExchange + " feasible execution paths found with ");
                            LOGGER.debug("Hardware Area Persentage = " + DSoCConfig.hardwareAreaPercentage);
                            LOGGER.debug("Software Area Persentage = " + DSoCConfig.softwareAreaPercentage);
                            LOGGER.debug("Power Persentage = " + DSoCConfig.powerPercentage);
                            LOGGER.debug("Response Time Percentage Persentage = " + DSoCConfig.responseTimePercentage);
                            LOGGER.debug("************************************************");
                        }
                    }
                }
            }
        }
    }

    public int getNextSESID() {
        return getNextSESIndex;
    }
}
