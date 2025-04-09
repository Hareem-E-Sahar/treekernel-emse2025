package uk.ac.lkl.migen.system.ai.collaboration;

import java.util.*;
import uk.ac.lkl.migen.system.ai.analysis.Evaluator;

/**
 * A matrix with distances between students. 
 * 
 * Implements several methods to facilitate its manipulation and several types
 * of calculation. 
 * 
 * @author sergut
 *
 */
public class StudentDistanceMatrix {

    /**
     * An ordered list of the names of the students. 
     */
    private List<String> nameList = new ArrayList<String>();

    /**
     * An array with the distancimport uk.ac.lkl.migen.system.ai.collaboration.StudentDistance;
es between the students. The position of the 
     * students in the name list is the index used in the matrix. 
     */
    private StudentDistance[][] distanceMatrix;

    public StudentDistanceMatrix(int studentCount, List<StudentDistance> studentDistanceList) {
        if (studentCount < 2) throw new IllegalArgumentException("There must be at least 2 students (given " + studentCount + ")");
        int maxPairCount = (studentCount * studentCount - studentCount) / 2;
        if (studentDistanceList.size() != maxPairCount) throw new IllegalArgumentException("Incorrect number of pairs (should be " + maxPairCount + ")");
        distanceMatrix = new StudentDistance[studentCount][studentCount];
        for (StudentDistance studentDistance : studentDistanceList) {
            String nameA = studentDistance.getNameA();
            String nameB = studentDistance.getNameB();
            addNamesToNameList(nameA, nameB);
            int idxA = getIndex(nameA);
            int idxB = getIndex(nameB);
            if (distanceMatrix[idxA][idxB] == null && distanceMatrix[idxB][idxA] == null) {
                distanceMatrix[idxA][idxB] = studentDistance;
                distanceMatrix[idxB][idxA] = studentDistance;
            } else {
                throw new IllegalArgumentException("Repeated distance entry (" + nameA + "," + nameB + ")");
            }
        }
    }

    public StudentDistanceMatrix(StudentDistance[][] matrix) {
        int size = matrix.length;
        distanceMatrix = new StudentDistance[size][size];
        for (int i = 0; i < size; i++) {
            for (int j = i + 1; j < size; j++) {
                if (matrix[i][j] == null) throw new IllegalArgumentException("Null element in matrix (" + i + "," + j + ")"); else {
                    distanceMatrix[i][j] = matrix[i][j];
                    distanceMatrix[j][i] = matrix[i][j];
                }
            }
        }
        nameList = new ArrayList<String>();
        nameList.add(matrix[0][1].getNameA());
        for (int j = 0 + 1; j < size; j++) {
            nameList.add(matrix[0][j].getNameB());
        }
    }

    private String getName(int index) {
        return nameList.get(index);
    }

    private int getIndex(String name) {
        int result = nameList.indexOf(name);
        if (result == -1) throw new IllegalArgumentException("Student '" + name + "' does not exist.");
        return result;
    }

    /**
     * Add given names to the list of names of students represented in this
     * matrix. If a name is already added, nothing is done for it.
     *  
     * @param nameA first name
     * @param nameB second name
     * 
     * @throws RuntimeException when the number of different names exceeds 
     * the max number of different names that can be represented in the matrix 
     */
    private void addNamesToNameList(String nameA, String nameB) {
        if (distanceMatrix == null) return;
        if (!nameList.contains(nameA)) nameList.add(nameA);
        if (!nameList.contains(nameB)) nameList.add(nameB);
        if (nameList.size() > distanceMatrix.length) throw new IllegalArgumentException("Too many different names");
    }

    /**
     * Returns the distance between both given students.
     * 
     * @param studentA first student
     * @param studentB second student
     * 
     * @return the distance between both given students.
     */
    public double getDistance(String studentA, String studentB) {
        int idxA = getIndex(studentA);
        int idxB = getIndex(studentB);
        return distanceMatrix[idxA][idxB].getDistance();
    }

    /**
     * Get the n greater distances from the distance matrix, 
     * where n is the minimum between 10 and half of the 
     * total distances (i.e. (size ^ 2 - size)/4).
     * 
     * @return a sorted set of students-to-student distances
     */
    public SortedSet<StudentDistance> getMaxDistances() {
        int size = distanceMatrix.length;
        if (size == 2) {
            TreeSet<StudentDistance> result = new TreeSet<StudentDistance>();
            result.add(distanceMatrix[0][1]);
            return result;
        }
        int n = Math.min(10, (size * size - size) / 4);
        return getMaxDistances(n);
    }

    /**
     * Get the n greater distances from the distance matrix.
     * 
     * @param n the number of distances to choose
     * 
     * @return a sorted set of students-to-student distances
     */
    public SortedSet<StudentDistance> getMaxDistances(int n) {
        int size = distanceMatrix.length;
        if (n > (size * size - size) / 2) n = (size * size - size) / 2;
        SortedSet<StudentDistance> result = new TreeSet<StudentDistance>();
        for (int i = 0; i < size; i++) {
            for (int j = i + 1; j < size; j++) {
                double distance = distanceMatrix[i][j].getDistance();
                if (result.size() < n) {
                    String nameA = getName(i);
                    String nameB = getName(j);
                    result.add(new StudentDistance(nameA, nameB, distance));
                    continue;
                }
                double minSelectedDistance = result.first().getDistance();
                if (distance > minSelectedDistance) {
                    String nameA = getName(i);
                    String nameB = getName(j);
                    result.add(new StudentDistance(nameA, nameB, distance));
                    result.remove(result.first());
                }
            }
        }
        return result;
    }

    private StudentDistanceMatrix getCopyWithoutPair(int firstRow, int secondRow) {
        int size = distanceMatrix.length;
        if (firstRow == secondRow) throw new IllegalArgumentException("Row indexes must be different");
        if (size < 3) return null;
        StudentDistance[][] resultDistanceMatrix = new StudentDistance[size - 2][size - 2];
        int xOffset = 0, yOffset = 0;
        for (int x = 0; x < size; x++) {
            if (x == firstRow || x == secondRow) {
                xOffset++;
                continue;
            }
            yOffset = 0;
            for (int y = 0; y < size; y++) {
                if (y == firstRow || y == secondRow) {
                    yOffset++;
                } else {
                    resultDistanceMatrix[x - xOffset][y - yOffset] = distanceMatrix[x][y];
                }
            }
        }
        return new StudentDistanceMatrix(resultDistanceMatrix);
    }

    /**
     * Returns a copy of this student matrix, but removing the row 
     * and column corresponding to the given student 
     * studentDistance, or null if the matrix dimensions are 1 or less.
     * 
     * @param studentName the name of the student 
     * 
     * @return
     */
    public StudentDistanceMatrix getCopyWithoutStudent(String studentName) {
        if (studentName == null) return this;
        int index = getIndex(studentName);
        if (index == -1) return this;
        int size = distanceMatrix.length;
        if (size < 2) return null;
        StudentDistance[][] resultDistanceMatrix = new StudentDistance[size - 1][size - 1];
        int xOffset = 0, yOffset = 0;
        for (int x = 0; x < size; x++) {
            if (x == index) {
                xOffset++;
                continue;
            }
            yOffset = 0;
            for (int y = 0; y < size; y++) {
                if (y == index) {
                    yOffset++;
                } else {
                    resultDistanceMatrix[x - xOffset][y - yOffset] = distanceMatrix[x][y];
                }
            }
        }
        return new StudentDistanceMatrix(resultDistanceMatrix);
    }

    /**
     * Returns a copy of the given matrix. 
     * 
     * @return a copy of the given matrix.
     */
    public StudentDistanceMatrix getCopy() {
        int size = distanceMatrix.length;
        StudentDistance[][] resultDistanceMatrix = new StudentDistance[size][size];
        for (int x = 0; x < size; x++) for (int y = 0; y < size; y++) resultDistanceMatrix[x][y] = distanceMatrix[x][y];
        return new StudentDistanceMatrix(resultDistanceMatrix);
    }

    /**
     * Returns a copy of the given matrix, but removing the rows 
     * and columns corresponding to the elements in the given 
     * studentDistance, or null if the matrix dimensions are 2 or less.
     * 
     * If the student distance is null, this method is equivalent to getCopy().
     * 
     * @return a copy of the given matrix
     */
    public StudentDistanceMatrix getCopyWithoutPair(StudentDistance distance) {
        if (distance == null) return getCopy();
        MatrixPosition position = getPosition(distance);
        if (position == null) throw new IllegalArgumentException("Distance " + distance + " is not in this matrix.");
        return getCopyWithoutPair(position.x, position.y);
    }

    /**
     * Returns the position of the given StudentDistance, of null if 
     * it is not in the matrix.
     *  
     * @param studentDistance the distance
     * @return the position of the given StudentDistance, of null if 
     * it is not in the matrix.
     */
    private MatrixPosition getPosition(StudentDistance studentDistance) {
        try {
            int idxA = getIndex(studentDistance.getNameA());
            int idxB = getIndex(studentDistance.getNameB());
            double myDistance = distanceMatrix[idxA][idxB].getDistance();
            double givenDistance = studentDistance.getDistance();
            if (Math.abs(myDistance - givenDistance) < Evaluator.PRECISION) {
                return new MatrixPosition(idxA, idxB);
            } else {
                return null;
            }
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    /**
     * Returns true if this StudentDistanceMatrix contains the
     * given distance, false otherwise.
     * 
     * @param distance
     * @return
     */
    public boolean contains(StudentDistance distance) {
        return getPosition(distance) != null;
    }

    public double getAverageDistance() {
        double total = 0.0;
        int size = distanceMatrix.length;
        int count = 0;
        for (int i = 0; i < size; i++) {
            for (int j = i + 1; j < size; j++) {
                total += distanceMatrix[i][j].getDistance();
                ++count;
            }
        }
        return total / count;
    }

    /**
     * Returns the pair to remove in the next iteration.
     * 
     * @return
     */
    public StudentDistance getDistanceToRemove() {
        int size = distanceMatrix.length;
        if (size == 2) {
            return distanceMatrix[0][1];
        }
        int n = Math.min(10, (size * size - size) / 4);
        return getDistanceToRemove(n);
    }

    /**
     * Returns the pair to remove in the next iteration. 
     * 
     * The pair to remove is the one that, after removal, leaves 
     * the resulting matrix with a higher average distance.
     * 
     * @param n number of pairs to look for (max: (size^2-size)/2
     * 
     * @return
     */
    private StudentDistance getDistanceToRemove(int n) {
        StudentDistance distanceToRemove = null;
        double highestAverageAfterRemoval = 0.0;
        SortedSet<StudentDistance> maxDistances = getMaxDistances(n);
        Iterator<StudentDistance> itr;
        for (itr = maxDistances.iterator(); itr.hasNext(); ) {
            StudentDistance distance = itr.next();
            StudentDistanceMatrix matrixIfRemoved = getCopyWithoutPair(distance);
            double averageIfRemoved = matrixIfRemoved.getAverageDistance();
            if (averageIfRemoved > highestAverageAfterRemoval) {
                distanceToRemove = distance;
                highestAverageAfterRemoval = averageIfRemoved;
            }
        }
        return distanceToRemove;
    }

    /**
     * Returns the student to remove if the number of students is odd. 
     * 
     * The student to remove is the one that is more different to all
     * the others, that is, the one whose average distance to the other
     * students is higher.
     * 
     * If the number of students is even, null is returned. 
     * 
     * @return the student to remove, or null if the number of students is even
     */
    public String getStudentToRemove() {
        int size = distanceMatrix.length;
        if (distanceMatrix.length % 2 == 0) return null;
        int resultIndex = 0;
        double maxAvgDistance = 0.0;
        for (int i = 0; i < size; i++) {
            double distanceSum = 0.0;
            for (int j = 0; j < size; j++) {
                if (i != j) distanceSum += distanceMatrix[i][j].getDistance();
            }
            double avgDistance = distanceSum / size;
            if (avgDistance > maxAvgDistance) {
                maxAvgDistance = avgDistance;
                resultIndex = i;
            }
        }
        return getName(resultIndex);
    }

    @SuppressWarnings("unused")
    private void prettyPrint() {
        System.out.println(this.toString());
    }

    @Override
    public String toString() {
        String result = new String("");
        for (String name : nameList) {
            result += name + "\t";
        }
        result += "\n";
        int size = distanceMatrix.length;
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                if (distanceMatrix[i][j] == null) {
                    result += "0.00\t";
                } else {
                    result += doubleFormat(distanceMatrix[i][j].getDistance()) + "\t";
                }
            }
            result += "\n";
        }
        return result;
    }

    private String doubleFormat(double distance) {
        int intPart = (int) Math.floor(distance);
        int fourDigits = (int) Math.floor(100 * distance);
        int decPart = (fourDigits - intPart) / 100;
        if (decPart < 10) return intPart + ".0" + decPart; else return intPart + "." + decPart;
    }

    public int size() {
        return distanceMatrix.length;
    }

    /**
     * Returns the distance for the given metric between both given students.
     * 
     * Might be null if the metric was not used for calculating the overall distance.
     * 
     * @param metricName the metric name
     * @param studentA first student
     * @param studentB second student
     * 
     * @return the distance between both given students.
     */
    public WeightedDistance getWeightedDistance(String metricName, String studentA, String studentB) {
        int idxA = getIndex(studentA);
        int idxB = getIndex(studentB);
        return distanceMatrix[idxA][idxB].getWeightedDistance(metricName);
    }

    /**
     * Returns the names of the metrics used in this matrix. 
     * 
     * Might be empty if no named metrics are used (only overall distance). 
     * 
     * @return the names of the metrics used in this matrix.
     */
    public Set<String> getMetricIds() {
        Set<String> result = new HashSet<String>();
        int size = this.size();
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                StudentDistance d = distanceMatrix[i][j];
                if (d != null) {
                    result.addAll(d.getMetricIds());
                }
            }
        }
        return result;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof StudentDistanceMatrix)) return false;
        StudentDistanceMatrix other = (StudentDistanceMatrix) o;
        if (!this.nameList.equals(other.nameList)) {
            return false;
        }
        for (String nameA : this.nameList) {
            for (String nameB : this.nameList) {
                if (nameB.equals(nameA)) continue;
                if (this.getDistance(nameA, nameB) != other.getDistance(nameA, nameB)) {
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public int hashCode() {
        return this.distanceMatrix.hashCode();
    }
}

class MatrixPosition {

    public int x, y;

    public MatrixPosition(int x, int y) {
        this.x = x;
        this.y = y;
    }
}
