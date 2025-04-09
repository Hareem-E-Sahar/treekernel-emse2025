package seevolution;

import seevolution.animation.*;
import java.io.*;
import java.awt.*;
import java.util.*;
import javax.vecmath.*;
import javax.media.j3d.*;
import com.sun.j3d.utils.geometry.*;

/**
 * This class is used to build and represent circular chromosomes. Circular chromosomes are represented 
 * as tori, and approximated as a limited number of segments, with the same number of sides each.
 * @author Andres Esteban Marcos
 * @version 2.0
 */
public class CircularChromosome extends Chromosome {

    private MutationEvent currentEvent = null;

    private float innerRadius;

    private float outerRadius;

    /**
	 * Builds a CircularChromosome with default name and length
	 * @param ratio The ratio between the inner and outer radius
	 * @param sides The number of sides of each torus segment
	 * @param segments The number of segments that form the chromosome
	 */
    public CircularChromosome(float ratio, int sides, int segments) {
        this("", 1, ratio, 1, sides, segments);
    }

    /**
	 * Builds a CircularChromosome with all the necessary parameters to show animations
	 * @param name The name of the Chromosome
	 * @param length The length of the Chromosome in nucleotides
	 * @param ratio The ratio between the inner and outer radius
	 * @param sides The number of sides of each torus segment
	 * @param segments The number of segments that form the chromosome
	 */
    public CircularChromosome(String name, int length, float ratio, int sides, int segments) {
        this(name, length, ratio, 1, sides, segments);
    }

    /**
	 * Builds a CircularChromosome with all the necessary parameters to show animations
	 * @param name The name of the Chromosome
	 * @param length The length of the Chromosome in nucleotides
	 * @param innerRadius The radius of the torus section
	 * @param outerRadius The radius of the circunference formed by the center of every torus section
	 * @param sides The number of sides of each torus segment
	 * @param segments The number of segments that form the chromosome
	 */
    public CircularChromosome(String name, int length, float innerRadius, float outerRadius, int sides, int segments) {
        super(Chromosome.CIRCULAR, name, length > segments ? length : segments, sides, segments);
        this.innerRadius = innerRadius;
        this.outerRadius = outerRadius;
        initialize();
    }

    /**
	 * Returns the transform necessary to "push out" or "bring in" a segment. It is generally called with
	 * increasing values of count and a constant max to give an impression of movement.
	 * @param in Whether the movement is inwards (true) or outwards(false)
	 * @param count The frame count
	 * @param max The maximum number of frames
	 * @return The transform
	 */
    public Transform3D getDistanceTransform(boolean in, int count, int max) {
        float distance;
        if (in) distance = outerRadius * 0.3f * (1 - (float) count / max); else distance = (outerRadius * 0.3f * count) / max;
        Transform3D distanceTransform = new Transform3D();
        distanceTransform.setTranslation(new Vector3f(0f, distance, 0f));
        return distanceTransform;
    }

    /**
	 * Returns the transform that initially places a segment on the chromosome
	 * @return The transform
	 */
    public Transform3D getInitialTransform() {
        Transform3D initialRotation = new Transform3D();
        return initialRotation;
    }

    /**
	 * Returns the transform that is used to incrementally modify the initial transform and place the other segments
	 * @return The transform
	 */
    public Transform3D getModifierTransform() {
        Transform3D rotateSection = new Transform3D();
        rotateSection.setRotation(new AxisAngle4d(0, 0, -1, 2 * Math.PI / numSegments));
        return rotateSection;
    }

    /**
	 * Returns a torus segment
	 * @return A new TorusSegment
	 */
    public ChromosomeSegment getSegment() {
        return new TorusSegment(numSides, innerRadius, outerRadius, (float) (2 * Math.PI / numSegments));
    }

    /**
	 * Returns a torus segment of arbitrary length
	 * @param length The length of the segment, which will be scaled accordingly
	 * @return A new TorusSegment
	 */
    public ChromosomeSegment getSegment(int width) {
        double angle = 2 * Math.PI * width / (double) length;
        return new TorusSegment(numSides, innerRadius, outerRadius, (float) angle);
    }

    /**
	 * Returns the radius of the circunference
	 * @return The radius of the circunference
	 */
    public float getSize() {
        return outerRadius;
    }

    /**
	 * Returns the transform necessary to place any element at the chosen site along the chromosome
	 * @param site The nucleotide on the chromosome on which the element will be placed
	 * @return The transform
	 */
    public Transform3D getTransform(int site) {
        double angle = -2 * Math.PI * (double) site / length;
        Transform3D rotation = new Transform3D();
        rotation.setRotation(new AxisAngle4d(0, 0, 1, angle));
        return rotation;
    }

    public Transform3D getTransform(int site, int width) {
        double angle = -2 * Math.PI * ((double) site - width / 2.0) / length;
        Transform3D rotation = new Transform3D();
        rotation.setRotation(new AxisAngle4d(0, 0, 1, angle));
        return rotation;
    }

    /**
	 * Changes the chromosome to represent an inversion between orStart and orEnd.<br>
	 * Max is the total number of frames that the animation is run for, and count represents the frame that will be represented after the call to this method.
	 * @param orStart The starting point of the inversion
	 * @param orEnd The ending point of the inversion
	 * @param count The frame out of 'max' frames, the function must be called with count = 0 before any other value.
	 * @param max The total number of frames that form the animation
	 */
    public void animateInversion(int orStart, int orEnd, int count, int max) {
        int start = locationToIndex(orStart);
        int end = locationToIndex(orEnd) + 1;
        if (start == end + 1) animateInversion(orEnd, orStart, count, max);
        if (count == 0) {
            for (int i = start; i != end; i = (i + 1) % numSegments) {
                transformGroups[currentPos[i]].getTransform(tr);
                tempTransformGroups[currentPos[i]] = new TransformGroup(tr);
            }
        }
        if (start != end + 1 && count >= 0 && count <= max) {
            double startAngle = 2 * Math.PI * segments[currentPos[start]].position / (double) length;
            double endAngle = 2 * Math.PI * segments[currentPos[end]].position / (double) length;
            double vectorAngle = (startAngle + endAngle) / 2;
            if (count == 0) System.out.println("Inverting from " + orStart + " ( " + start + ", " + startAngle + " ) to " + orEnd + " ( " + end + ", " + endAngle + " ) -> " + vectorAngle + " ( " + length + " )");
            Transform3D inversionTransform = new Transform3D();
            inversionTransform.setRotation(new AxisAngle4d(Math.sin(vectorAngle), Math.cos(vectorAngle), 0, -Math.PI * count / max));
            for (int i = start; i != end; i = (i + 1) % numSegments) {
                tempTransformGroups[currentPos[i]].getTransform(tr);
                tempTransform.mul(inversionTransform, tr);
                transformGroups[currentPos[i]].setTransform(tempTransform);
            }
        }
        if (start != end + 1 && count == max) invert(start, end, currentPos, false);
    }

    /**
	 * Changes the chromosome to represent a transposition between start and end to insertion.<br>
	 * Max is the total number of frames that the animation is run for, and count represents the frame that will be represented after the call to this method.
	 * @param start The starting point of the transposition
	 * @param end The ending point of the transposition
	 * @param insertion The insertion point of the transposition
	 * @param count The frame out of 'max' frames, the function must be called with count = 0 before any other value.
	 * @param max The total number of frames that form the animation
	 */
    public void animateTransposition(int start, int end, int insertion, float distance, int count, int max) {
        start = (int) (start * numSegments / length) % numSegments;
        end = (int) ((end * numSegments / length) + 1) % numSegments;
        insertion = (int) ((insertion * numSegments / length) + 1) % numSegments;
        if (start == end + 1) return;
        if (count == 0) {
            for (int i = 0; i < transformGroups.length; i++) {
                transformGroups[i].getTransform(tr);
                tempTransformGroups[i] = new TransformGroup(tr);
            }
            return;
        }
        Transform3D firstTranslation = new Transform3D();
        Transform3D secondTranslation = new Transform3D();
        Transform3D outerRotation = new Transform3D();
        double startAngle = 2 * Math.PI / numSegments * start;
        double endAngle = 2 * Math.PI / numSegments * end;
        double vectorAngle = (startAngle + endAngle) / 2;
        if (start > end) vectorAngle += Math.PI;
        int tempCount;
        if (count <= max / 3) tempCount = count; else tempCount = (int) (max / 3);
        int maxTempCount = max / 3;
        if (tempCount != 0) {
            firstTranslation.setTranslation(new Vector3f(0f, 0f, distance * tempCount / maxTempCount));
        }
        if (count <= max / 3) tempCount = 0; else if (count < max * 2 / 3) tempCount = count - max / 3; else tempCount = max / 3;
        if (tempCount != 0) {
            double outerAngle = -Math.PI * 2 / numSegments * (insertion - end);
            outerRotation.setRotation(new AxisAngle4d(0, 0, 1, outerAngle * tempCount / maxTempCount));
            Transform3D innerRotation = new Transform3D();
            double innerAngle;
            if (start > end) innerAngle = Math.PI * 2 / numSegments * (end - start + numSegments); else innerAngle = Math.PI * 2 / numSegments * (end - start);
            innerRotation.setRotation(new AxisAngle4d(0, 0, 1, innerAngle * tempCount / maxTempCount));
            for (int i = end; i != insertion; i = (i + 1) % numSegments) {
                tempTransformGroups[currentPos[i]].getTransform(tr);
                tempTransform.mul(innerRotation, tr);
                transformGroups[currentPos[i]].setTransform(tempTransform);
            }
        }
        if (count <= max * 2 / 3) tempCount = 0; else tempCount = count - max * 2 / 3;
        if (tempCount != 0) {
            startAngle = 2 * Math.PI / numSegments * (insertion + start - end);
            endAngle = 2 * Math.PI / numSegments * insertion;
            vectorAngle = (startAngle + endAngle) / 2;
            if (start > end) vectorAngle += Math.PI;
            secondTranslation.setTranslation(new Vector3f(0f, 0f, -distance * tempCount / maxTempCount));
        }
        secondTranslation.mul(outerRotation);
        secondTranslation.mul(firstTranslation);
        for (int i = start; i != end; i = (i + 1) % numSegments) {
            tempTransformGroups[currentPos[i]].getTransform(tr);
            tempTransform.mul(secondTranslation, tr);
            transformGroups[currentPos[i]].setTransform(tempTransform);
        }
        if (count == max) transpose(start, end, insertion, currentPos);
    }

    /**
	 * Calculates the break points on this chromosome
	 * @param eventList The list of events that may produce breaks
	 */
    public void calculateBreakPoints(LinkedList<MutationEvent> eventList) {
        if (eventList == null) return;
        int breakPoints[] = new int[numSegments];
        int positions[] = new int[numSegments];
        for (int i = 0; i < numSegments; i++) positions[i] = i;
        int breakInd = 0;
        for (int eventInd = 0; eventInd < eventList.size(); eventInd++) {
            MutationEvent event = eventList.get(eventInd);
            if (event.getType() == MutationEvent.INVERSION) {
                InversionEvent invEvent = (InversionEvent) event;
                if (!invEvent.getChromosomeName().equals(name)) continue;
                int start = invEvent.getLeft() % length;
                int end = invEvent.getRight() % length;
                start = (int) (start * numSegments / length) % numSegments;
                end = (int) ((end * numSegments / length) + 1) % numSegments;
                int realStart = positions[start];
                int realEnd = positions[end];
                breakInd = insert(realStart, breakPoints, breakInd);
                breakInd = insert(realEnd, breakPoints, breakInd);
                invert(start, end, positions, true);
            }
            if (event.getType() == MutationEvent.TRANSPOSITION) {
                TranspositionEvent transEvent = (TranspositionEvent) event;
                if (!transEvent.getChromosomeName().equals(name)) continue;
                int start = transEvent.getLeft() % length;
                int end = transEvent.getRight() % length;
                int insertion = transEvent.getInsertionPoint() % length;
                start = (int) (start * numSegments / length) % numSegments;
                end = (int) ((end * numSegments / length) + 1) % numSegments;
                insertion = (int) (insertion * numSegments / length + 1) % numSegments;
                int realStart = positions[start];
                int realEnd = positions[end];
                int realInsertion = positions[insertion];
                breakInd = insert(realStart, breakPoints, breakInd);
                breakInd = insert(realEnd, breakPoints, breakInd);
                breakInd = insert(realInsertion, breakPoints, breakInd);
                transpose(start, end, insertion, positions);
            }
        }
        int tempBreakPoints[] = new int[breakInd];
        for (int i = 0; i < breakInd; i++) tempBreakPoints[i] = breakPoints[i];
        setBreakPoints(tempBreakPoints);
    }

    /**
	 * Displays the name of this chromosome next to it
	 * @param displayName The name is displayed if true
	 */
    public void displayName(boolean displayName) {
    }

    /**
	 * Changes the internal representation of the segments to represent an inversion between start and end
	 * @param start The starting point of the inversion
	 * @param end The ending point of the inversion
	 */
    private void invert(int start, int end, int array[], boolean breakpoints) {
        int ind = 0;
        if (breakpoints) end = (end + 1) % numSegments;
        for (int i = start; i != end; i = (i + 1) % numSegments) temp[ind++] = array[i];
        for (int i = start; i != end; i = (i + 1) % numSegments) array[i] = temp[--ind];
    }

    public void showGap(int gapIndex, int howMany, float initialSectionLength, float gapLength, float finalSectionLength) {
        double segmentLocation = 0;
        double sectionAngle = 2 * Math.PI * initialSectionLength;
        int intInitialSectionLength = 0;
        for (int i = 0; i < gapIndex; i++) intInitialSectionLength += segments[currentPos[i]].length;
        for (int i = 0; i < gapIndex; i++) {
            double segmentAngle = sectionAngle * segments[currentPos[i]].length / (double) intInitialSectionLength;
            sectionAngle -= segmentAngle;
            intInitialSectionLength -= segments[currentPos[i]].length;
            segments[currentPos[i]].setWidth(segmentAngle);
            Transform3D position = getInitialTransform();
            Transform3D rotateSegment = new Transform3D();
            rotateSegment.setRotation(new AxisAngle4d(0, 0, -1, segmentLocation));
            position.mul(rotateSegment);
            transformGroups[currentPos[i]].setTransform(position);
            segmentLocation += segmentAngle;
        }
        double gapAngle = 2 * Math.PI * gapLength;
        segmentLocation += gapAngle;
        gapIndex += howMany;
        sectionAngle = 2 * Math.PI * finalSectionLength;
        int intFinalSectionLength = 0;
        for (int i = gapIndex; i < segments.length; i++) intFinalSectionLength += segments[currentPos[i]].length;
        for (int i = gapIndex; i < segments.length; i++) {
            double segmentAngle = (sectionAngle * segments[currentPos[i]].length) / (double) intFinalSectionLength;
            sectionAngle -= segmentAngle;
            intFinalSectionLength -= segments[currentPos[i]].length;
            segments[currentPos[i]].setWidth(segmentAngle);
            Transform3D position = getInitialTransform();
            Transform3D rotateSegment = new Transform3D();
            rotateSegment.setRotation(new AxisAngle4d(0, 0, -1, segmentLocation));
            position.mul(rotateSegment);
            transformGroups[currentPos[i]].setTransform(position);
            segmentLocation += segmentAngle;
        }
    }

    /**
	 * Changes the internal representation of the segments to represent a transposition between 'start' and 'end' to 'insertion'
	 * @param start The starting point of the transposition
	 * @param end The ending point of the transposition
	 * @param insertion The insertion point of the transposition
	 */
    private void transpose(int start, int end, int insertion, int array[]) {
        int ind = 0;
        for (int i = start; i != (end + 1) % numSegments; i = (i + 1) % numSegments) temp[ind++] = array[i];
        temp[ind] = -1;
        ind = 0;
        for (int i = end; i != (insertion + 1) % numSegments; i = (i + 1) % numSegments) array[(start + ind++) % numSegments] = array[i];
        ind = 0;
        int i = (insertion + start - end) % numSegments;
        if (i < 0) i += numSegments;
        for (; temp[ind + 1] != -1; i = (i + 1) % numSegments) array[i] = temp[ind++];
    }
}
