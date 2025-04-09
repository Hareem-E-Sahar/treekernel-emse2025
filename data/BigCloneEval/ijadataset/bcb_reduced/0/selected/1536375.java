package ru.cos.sim.visualizer.math;

import java.util.ArrayList;
import org.apache.log4j.Logger;
import ru.cos.sim.visualizer.agents.car.CarPosition;

public class BezierCurve {

    protected static Logger logger = Logger.getLogger(BezierCurve.class);

    protected Vector2f[] base_points;

    protected Vector2f[] result;

    protected float[] lengthes;

    protected Vector2f[] topLane;

    protected Vector2f[] bottomLane;

    protected int[] indexes;

    protected int steps = 25;

    protected float length = 0;

    protected Vector2f shift = new Vector2f(0, 0);

    protected float desiredLength;

    public BezierCurve(Vector2f[] points) {
        this.base_points = points;
    }

    public BezierCurve(Vector2f[] points, boolean generateNow) {
        this.base_points = points;
        if (generateNow) generatePoints();
    }

    public void generatePoints() {
        if (base_points.length != 4) throw new Error("Cannot construct Bezier curve");
        result = new Vector2f[steps + 1];
        lengthes = new float[steps + 1];
        generateBezierCurve(0, steps);
    }

    protected void generateBezierCurve(int beginindex, int maxindex) {
        length = 0;
        float time = 0;
        float step_value = 1.0f / steps;
        for (int i = beginindex; i <= maxindex; i++) {
            result[i] = func(time);
            if (i != beginindex) length += result[i - 1].distance(result[i]);
            lengthes[i] = length;
            time += step_value;
            if (time > 1) time = 1;
        }
    }

    protected int generateBezierCurve(int beginindex, int maxindex, float splitLength) {
        float realspllength = 0;
        int splitIndex = -2;
        length = 0;
        float time = 0;
        float step_value = 1.0f / steps;
        int j = beginindex;
        Vector2f point = null;
        float dlength = 0;
        if (splitLength == 0) {
            splitIndex = 0;
        }
        for (int i = beginindex; i <= maxindex; i++) {
            point = func(time);
            dlength = 0;
            if (j != beginindex) {
                dlength = result[j - 1].distance(point);
            }
            if (splitIndex < -1 && length + dlength > splitLength) {
                result[j] = point.subtract(result[j - 1]).normalizeLocal().multLocal(splitLength - length).addLocal(result[j - 1]);
                realspllength = length + result[j].distance(result[j - 1]);
                lengthes[j] = realspllength;
                splitIndex = j;
                j++;
            }
            length += dlength;
            time += step_value;
            if (time > 1) time = 1;
            result[j] = point;
            lengthes[j] = length;
            j++;
        }
        splitIndex = (splitIndex < -1) ? -1 : splitIndex;
        return splitIndex;
    }

    protected Vector2f generateBezierCurve(float begin, float end) {
        Vector2f directionVector = null;
        result = new Vector2f[steps + 3];
        lengthes = new float[steps + 3];
        length = 0;
        float time = 0;
        float step_value = 1.0f / steps;
        int j = 0;
        Vector2f point = null;
        float dlength = 0;
        boolean inSector = false;
        int pointsCount = -1;
        ArrayList<Vector2f> chooseResult = new ArrayList<Vector2f>();
        ArrayList<Float> chooseLengthies = new ArrayList<Float>();
        for (int i = 0; i <= steps; i++) {
            point = func(time);
            dlength = 0;
            if (j != 0) {
                dlength = result[j - 1].distance(point);
                if (!inSector && length <= begin && length + dlength >= begin) {
                    pointsCount++;
                    Vector2f p = point.subtract(result[j - 1]).normalizeLocal().multLocal(begin - length).addLocal(result[j - 1]);
                    chooseResult.add(p);
                    directionVector = point.subtract(result[j - 1]);
                    if (Math.abs(directionVector.length()) <= 0.0001f) directionVector = null;
                    chooseLengthies.add(length + p.distance(result[j - 1]));
                    inSector = true;
                }
                if (inSector && length <= end && length + dlength >= end) {
                    pointsCount++;
                    Vector2f p1 = point.subtract(result[j - 1]).normalizeLocal().multLocal(end - length).addLocal(result[j - 1]);
                    chooseResult.add(p1);
                    chooseLengthies.add(length + p1.distance(result[j - 1]));
                    inSector = false;
                }
            }
            length += dlength;
            time += step_value;
            if (time > 1) time = 1;
            if (inSector) {
                chooseResult.add(point);
                chooseLengthies.add(length);
            }
            result[j] = point;
            lengthes[j] = length;
            j++;
        }
        result = new Vector2f[chooseResult.size()];
        result = chooseResult.toArray(result);
        lengthes = new float[chooseLengthies.size()];
        for (int i = 0; i < chooseLengthies.size(); i++) {
            lengthes[i] = chooseLengthies.get(i);
        }
        return directionVector;
    }

    /**
	 * Calculates point , according to current time and coefficients of polynom
	 * @param time - current time
	 * @param shift - all coordinates will be shifted by this value
	 * @return - point of the curve
	 */
    protected Vector2f func(float time) {
        Vector2f result = new Vector2f(0, 0);
        result.x = (1 - time) * (1 - time) * (1 - time) * base_points[0].x + 3 * time * (1 - time) * (1 - time) * base_points[1].x + 3 * time * time * (1 - time) * base_points[2].x + time * time * time * base_points[3].x + shift.x;
        result.y = (1 - time) * (1 - time) * (1 - time) * base_points[0].y + 3 * time * (1 - time) * (1 - time) * base_points[1].y + 3 * time * time * (1 - time) * base_points[2].y + time * time * time * base_points[3].y + shift.y;
        return result;
    }

    public int generateSplittedCurve(float splitLength) {
        if (base_points.length != 4) throw new Error("Cannot construct Bezier curve");
        result = new Vector2f[steps + 2];
        lengthes = new float[steps + 2];
        int result = generateBezierCurve(0, steps, splitLength);
        return result;
    }

    /**
	 * Calculates tanget in the current point of the curve. Point is described by
	 * length from curve's begin. Gut Precision is  0.01f;
	 * @param position - distance from curve's begin
	 * @param precision - precision of the calculation, must be in range (0,1)
	 * @return tangent vector 
	 */
    public Vector2f getTangent(float position, float precision) {
        if (precision < 0 || position < 0) throw new Error("Position and precision must be positive");
        Vector2f first = this.func(position / this.length);
        if (position + precision > length) {
            if (position - precision < 0) throw new Error("Too low precision");
            Vector2f second = this.func((position - precision) / this.length);
            return first.subtract(second, first);
        } else {
            Vector2f second = this.func((position + precision) / this.length);
            return second.subtract(first, first);
        }
    }

    /**
	 * Calculates tangent in the current point of the curve. Point is described by
	 * length from curve's begin
	 * @param position - distance from curve's begin
	 * @return - tangent vector
	 */
    public Vector2f getTangent(float position) {
        float precision = 0.1f;
        if (steps != 0) precision = 1.0f / steps;
        return getTangent(position, precision);
    }

    public Vector2f[] getPoints() {
        return result;
    }

    public Vector2f getShift() {
        return shift;
    }

    public void setShift(Vector2f shift) {
        this.shift = shift;
    }

    public int[] getIndexes() {
        return indexes;
    }

    public int getSteps() {
        return steps;
    }

    public void setSteps(int steps) {
        this.steps = steps;
    }

    public CarPosition getPoint(float position) {
        if (position < 0) {
            logger.error("Position and precision must be positive");
            position = 0;
        }
        if (position > this.length) {
            return new CarPosition(this.func(1), result[result.length - 1].subtract(result[result.length - 2]).normalizeLocal());
        }
        int index = search(position);
        if (index >= result.length - 1) return new CarPosition(this.func(1), result[result.length - 1].subtract(result[result.length - 2]).normalizeLocal());
        if (lengthes[index] > position || lengthes[index + 1] < position) {
            logger.warn("Error in binary search. Car not correctly placed on tr");
            return new CarPosition(func(1), result[result.length - 1].subtract(result[result.length - 2]).normalizeLocal());
        }
        float rpos = position - lengthes[index];
        return new CarPosition(result[index + 1].subtract(result[index]).normalizeLocal().multLocal(rpos).addLocal(result[index]), result[index + 1].subtract(result[index]).normalizeLocal());
    }

    protected int search(float length) {
        int left = 0;
        int right = result.length;
        int index = (right + left) / 2;
        int maxindex = result.length - 1;
        while (left < right) {
            index = (right + left) / 2;
            if (lengthes[index] == length) return index;
            if (index < maxindex && lengthes[index] < length && lengthes[index + 1] > length) return index;
            if (lengthes[index] > length) {
                right = index;
            } else {
                left = index + 1;
            }
        }
        return maxindex;
    }

    public void setDesiredLength(float dl) {
        this.desiredLength = dl;
    }

    public float getLength() {
        return length;
    }
}
