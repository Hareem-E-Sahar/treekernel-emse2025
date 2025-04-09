package playground.wrashid.sschieffer.DSC.SlotDistribution;

import java.util.ArrayList;
import org.apache.commons.math.FunctionEvaluationException;
import org.apache.commons.math.MaxIterationsExceededException;
import org.apache.commons.math.analysis.polynomials.PolynomialFunction;
import org.apache.commons.math.optimization.OptimizationException;
import org.matsim.api.core.v01.Id;
import playground.wrashid.sschieffer.DSC.DecentralizedSmartCharger;
import playground.wrashid.sschieffer.SetUp.DomainFinder;
import playground.wrashid.sschieffer.SetUp.IntervalScheduleClasses.ChargingInterval;
import playground.wrashid.sschieffer.SetUp.IntervalScheduleClasses.LoadDistributionInterval;
import playground.wrashid.sschieffer.SetUp.IntervalScheduleClasses.ParkingInterval;
import playground.wrashid.sschieffer.SetUp.IntervalScheduleClasses.Schedule;
import playground.wrashid.sschieffer.SetUp.IntervalScheduleClasses.TimeInterval;

/**
 * assigns charging slots to the required charging times (calculated in the LP) using 
 * a random number generator and the distribution of free charging slots over the day.
 * 
 * Charging Slots are of a certain given maximum length and are stored as a schedule 
 * within the parking interval
 * 
 * 
 * @author Stella
 * 
 */
public class ChargingSlotDistributor {

    public double minChargingLength;

    public ChargingSlotDistributor(double minChargingLength) {
        this.minChargingLength = minChargingLength;
    }

    /**
	 * goes over every time interval in agents schedule
	 * if it is a parking interval it will assign charging slots for the interval 
	 * and returns the relevant charging schedule 
	 * 
	 * 
	 * @param schedule
	 * @return
	 * @throws Exception 
	 */
    public Schedule distribute(Id agentId, Schedule schedule) throws Exception {
        Schedule chargingScheduleAllIntervalsAgent = new Schedule();
        for (int i = 0; i < schedule.getNumberOfEntries(); i++) {
            TimeInterval t = schedule.timesInSchedule.get(i);
            if (t.isParking()) {
                ParkingInterval p = (ParkingInterval) t;
                if (p.getRequiredChargingDuration() > 0.0) {
                    double chargingTime = p.getRequiredChargingDuration();
                    if (chargingTime > 0) {
                    }
                    ArrayList<LoadDistributionInterval> loadList = DecentralizedSmartCharger.myHubLoadReader.getDeterministicLoadDistributionIntervalsAtLinkAndTime(agentId, p.getLocation(), p);
                    if (loadList.size() > 1) {
                        System.out.println("check distribute.. loadList should not be possible to be larger than 1");
                    }
                    PolynomialFunction func = loadList.get(0).getPolynomialFunction();
                    if (p.getIntervalLength() * 0.65 < chargingTime) {
                        if (chargingTime > p.getIntervalLength()) {
                            if (DecentralizedSmartCharger.debug) {
                                System.out.println("rounding error - correction");
                            }
                            chargingTime = p.getIntervalLength();
                            p.setRequiredChargingDuration(p.getIntervalLength());
                        }
                        double diff = p.getIntervalLength() - chargingTime;
                        double startRand = Math.random() * diff;
                        ChargingInterval c = new ChargingInterval(p.getStartTime() + startRand, p.getStartTime() + startRand + chargingTime);
                        chargingScheduleAllIntervalsAgent.addTimeInterval(c);
                        Schedule chargingScheduleForParkingInterval = new Schedule();
                        chargingScheduleForParkingInterval.addTimeInterval(c);
                        p.setChargingSchedule(chargingScheduleForParkingInterval);
                    } else {
                        Schedule chargingScheduleForParkingInterval = assignChargingScheduleForParkingInterval(func, p.getJoulesInInterval(), p.getStartTime(), p.getEndTime(), chargingTime);
                        p.setChargingSchedule(chargingScheduleForParkingInterval);
                        chargingScheduleAllIntervalsAgent.mergeSchedules(chargingScheduleForParkingInterval);
                    }
                } else {
                    p.setChargingSchedule(null);
                }
            }
        }
        return chargingScheduleAllIntervalsAgent;
    }

    public Schedule assignChargingScheduleForParkingInterval(PolynomialFunction func, double joulesInInterval, double startTime, double endTime, double chargingTime) throws Exception {
        Schedule chargingInParkingInterval = new Schedule();
        int intervals = (int) Math.ceil(chargingTime / minChargingLength);
        for (int i = 0; i < intervals; i++) {
            double bit = 0;
            if (i < intervals - 1) {
                bit = minChargingLength;
            } else {
                bit = chargingTime - (intervals - 1) * minChargingLength;
            }
            chargingInParkingInterval = assignRandomChargingSlotInChargingInterval(func, startTime, endTime, joulesInInterval, bit, chargingInParkingInterval);
        }
        return chargingInParkingInterval;
    }

    /**
	 * makes random number RN
	 * finds a time in interval that corresponds to RN according to available free slot distribution
	 * creates a charging slot and saves it in charging schedule, if it does not overlap with already existing charging times
	 * 
	 * @param func
	 * @param startTime
	 * @param endTime
	 * @param joulesInInterval
	 * @param bit
	 * @param chargingInParkingInterval
	 * @return
	 * @throws Exception 
	 */
    public Schedule assignRandomChargingSlotInChargingInterval(PolynomialFunction func, double startTime, double endTime, double joulesInInterval, double bit, Schedule chargingInParkingInterval) throws Exception {
        boolean notFound = true;
        boolean run = true;
        if (DecentralizedSmartCharger.debug) {
            System.out.println("assign random charging slot in interval");
            System.out.println(bit + "seconds between " + startTime + " - " + endTime);
            System.out.println("Schedule already:");
            chargingInParkingInterval.printSchedule();
        }
        double upper = endTime;
        double lower = startTime;
        double trial = (upper + lower) / 2;
        int countNotFoundInARow = 0;
        while (notFound) {
            run = true;
            upper = endTime;
            lower = startTime;
            trial = (upper + lower) / 2;
            double rand = Math.random();
            double integral;
            PolynomialFunction funcSubOpt = null;
            double fullSubOptIntegral = 0;
            while (run) {
                if (joulesInInterval >= 0) {
                    double err = Math.max(joulesInInterval / 100.0, 1.0);
                    if (DecentralizedSmartCharger.debug) {
                        System.out.println("integrate stat:" + startTime + " upto " + trial + " Function" + func.toString());
                        if (startTime == trial) {
                            System.out.println("TROUBLEt:");
                            System.out.println("error:" + err + " joules in interval" + joulesInInterval + " Function" + func.toString());
                        }
                    }
                    integral = DecentralizedSmartCharger.functionSimpsonIntegrator.integrate(func, startTime, trial);
                    if (integral < rand * joulesInInterval) {
                        lower = trial;
                        trial = (upper + lower) / 2;
                    } else {
                        upper = trial;
                        trial = (upper + lower) / 2;
                    }
                    if (Math.abs(integral - rand * joulesInInterval) <= err) {
                        run = false;
                    }
                } else {
                    if (funcSubOpt == null) {
                        funcSubOpt = turnSubOptimalSlotDistributionIntoProbDensityOfFindingAvailableSlot(func, startTime, endTime);
                    }
                    integral = DecentralizedSmartCharger.functionSimpsonIntegrator.integrate(funcSubOpt, startTime, trial);
                    if (fullSubOptIntegral == 0) {
                        fullSubOptIntegral = DecentralizedSmartCharger.functionSimpsonIntegrator.integrate(funcSubOpt, startTime, endTime);
                    }
                    double err = Math.max(Math.abs(fullSubOptIntegral) / 100.0, 1.0);
                    if (Math.abs(integral) < Math.abs(rand * fullSubOptIntegral)) {
                        lower = trial;
                        trial = (upper + lower) / 2;
                    } else {
                        upper = trial;
                        trial = (upper + lower) / 2;
                    }
                    if (Math.abs(Math.abs(integral) - Math.abs(rand * fullSubOptIntegral)) <= err) {
                        run = false;
                    }
                }
            }
            ChargingInterval c1;
            if (trial + bit > endTime) {
                c1 = null;
            } else {
                c1 = new ChargingInterval(trial, trial + bit);
            }
            if (c1 != null && chargingInParkingInterval.overlapWithTimeInterval(c1) == false) {
                notFound = false;
                countNotFoundInARow = 0;
                chargingInParkingInterval.addTimeInterval(c1);
            } else {
                countNotFoundInARow++;
                if (countNotFoundInARow > 100) {
                    chargingInParkingInterval = exitDistributionIfTooConstrained(startTime, endTime, bit, chargingInParkingInterval);
                    notFound = false;
                }
            }
        }
        chargingInParkingInterval.sort();
        return chargingInParkingInterval;
    }

    public Schedule exitDistributionIfTooConstrained(double startTime, double endTime, double bit, Schedule chargingInParkingInterval) {
        double timeAlready = chargingInParkingInterval.getTotalTimeOfIntervalsInSchedule();
        double diff = (endTime - startTime) - (timeAlready + bit);
        double startRand = Math.random() * diff;
        ChargingInterval c = new ChargingInterval(startTime + startRand, startTime + startRand + (timeAlready + bit));
        chargingInParkingInterval = new Schedule();
        chargingInParkingInterval.addTimeInterval(c);
        return chargingInParkingInterval;
    }

    public PolynomialFunction turnSubOptimalSlotDistributionIntoProbDensityOfFindingAvailableSlot(PolynomialFunction func, double start1, double end1) throws Exception {
        DomainFinder d = new DomainFinder(start1, end1, func);
        double minDomain = d.getDomainMin();
        int start = (int) Math.floor(start1);
        int end = (int) Math.ceil(end1);
        int steps = (int) Math.floor(((end - start) / (60.0 * 1.0)));
        if (steps == 0) {
            steps = Math.max(1, steps);
            System.out.println(" \n start " + start + " end " + end + " steps " + steps);
        }
        double[][] newFunc = new double[steps][2];
        for (int i = 0; i < steps; i++) {
            newFunc[i][0] = start + (i * (60.0 * 1.0));
            newFunc[i][1] = func.value(start + (i * (60.0 * 1.0))) - minDomain;
        }
        return DecentralizedSmartCharger.fitCurve(newFunc);
    }
}
