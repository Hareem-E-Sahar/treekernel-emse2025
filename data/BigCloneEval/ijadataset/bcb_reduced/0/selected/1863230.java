package net.sourceforge.dawnlite.control;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.io.OutputStream;
import java.util.Collections;
import java.util.Random;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.Vector;
import net.sourceforge.dawnlite.control.subtitles.SubtitleSegment;
import net.sourceforge.dawnlite.control.subtitles.SubtitleSegmenter;
import net.sourceforge.dawnlite.control.subtitles.Subtitles;
import net.sourceforge.dawnlite.control.subtitles.markup.SubtitleMarkup;
import net.sourceforge.dawnlite.control.sync.SyncDuration;
import net.sourceforge.dawnlite.control.sync.SyncMotionStub;
import net.sourceforge.dawnlite.control.sync.SyncObject;
import net.sourceforge.dawnlite.control.sync.SyncPoint;
import net.sourceforge.dawnlite.control.sync.SyncStay;
import net.sourceforge.dawnlite.control.tts.TTSTool;
import net.sourceforge.dawnlite.control.tts.TextDurationEstimator;
import net.sourceforge.dawnlite.initial.rois.ImageMetadata;
import net.sourceforge.dawnlite.initial.rois.ROI;
import net.sourceforge.dawnlite.script.Script;
import net.sourceforge.dawnlite.script.ViewBox;
import net.sourceforge.dawnlite.tools.Tools;

public class Control {

    /**
	 * Sync mode for using text only to order the regions of interest, no synchronization with audio is performed.
	 */
    public static final int ORDER_ONLY = 0;

    /**
	 * Sync mode for synchronizing the virtual camera with an (possibly pre-existing) audio track that cannot be influenced by the Control object.
	 */
    public static final int RIGID_AUDIO = 1;

    /**
	 * Sync mode for synchronizing the virtual camera with an audio track that can be rearranged in some ways (e.g., trimming at the end; future extension: remove irrelevant text passages) by the Control object
	 */
    public static final int ARRANGABLE_AUDIO = 2;

    /**
	 * Sync mode for synchronizing the virtual camera only to subtitles, not to audio.
	 */
    public static final int SUBTITLES = 3;

    /**
	 * Sync mode for synchronizing the virtual camera to running text (not implemented).
	 */
    public static final int RUNNING_TEXT = 4;

    /**
	 * String representations of the sync modes
	 */
    public static final String[] SYNC_MODE_STRINGS = new String[] { "OrderOnly", "RigidAudio", "ArrangableAudio", "Subtitles", "RunningText" };

    /**
	 * Fill long intervals with nearest possible ROIs in Match-Delay-Fill scripting algorithm.
	 */
    public static final int NEAREST_ROIS = 0;

    /**
	 * Fill long intervals with random ROIs in Match-Delay-Fill scripting algorithm.
	 */
    public static final int RANDOM_ROIS = 1;

    /**
	 * Fill long intervals with furthest possible ROIs in Match-Delay-Fill scripting algorithm.
	 */
    public static final int FURTHEST_ROIS = 2;

    /**
	 * String representations of the fill modes for Match-Delay-Fill scripting algorithm
	 */
    public static final String[] FILL_MODE_STRINGS = new String[] { "Nearest", "Random", "Furthest" };

    /**
	 * The visual output will have exactly the display dimensions
	 */
    public static final int KEEP_ORIENTATION = 0;

    /**
	 * The visual output will have its longer dimension used as width
	 */
    public static final int LANDSCAPE = 1;

    /**
	 * The visual output will have its longer dimension used as height
	 */
    public static final int PORTRAIT = 2;

    /**
	 * Width and height will be swapped if this allows for deeper zooming into most (occuring) ROIs. The idea is that in case of a mobile client device the display can be rotated by the user.
	 */
    public static final int BEST_ORIENTATION = 4;

    /**
	 * Defines under what conditions and how the Control will swap width and height of the visual output. 
	 */
    int orientation = 0;

    /**
	 * Markup that will be applied to the final subtitles (corresponding to highlighting of ROIs)
	 */
    TreeSet<SubtitleMarkup> subtitleMarkup = new TreeSet<SubtitleMarkup>();

    /**
	 * If one of the audio sync modes is used, this defines whether to use the relevance based scripting algorithm or the Match-Delay-Fill scripting algorithm
	 */
    private boolean useRelevance;

    /**
	 * Retrieves the currently set orientation behaviour setting (if and how the Control will swap width and height of the visual output). 
	 */
    public int getOrientation() {
        return orientation;
    }

    /**
	 * Sets under what conditions and how the Control will swap width and height of the visual output.
	 * @param orientation the new value for the property 
	 */
    public void setOrientation(int orientation) {
        this.orientation = orientation;
        int actualOrientation = getOrientationOf(this.displayDim);
        if (actualOrientation != orientation && (orientation == PORTRAIT || orientation == LANDSCAPE)) {
            displayDim.setSize(displayDim.height, displayDim.width);
        }
    }

    /**
	 * This is the setting for additional subtitles.
	 * If one of the non-text-only sync modes is used, this field defines
	 * if subtitles (or running text, to be implemented) are to be generated.
	 * Values supported are to be taken from Sync mode constants. 
	 */
    int additionalSubtitles = 0;

    /**
	 * Gets the setting for additional subtitles.
	 */
    public int getAdditionalSubtitles() {
        return additionalSubtitles;
    }

    /**
	 * Sets the setting for additional subtitles.
	 */
    public void setAdditionalSubtitles(int additionalSubtitles) {
        this.additionalSubtitles = additionalSubtitles;
    }

    /**
	 * In the rois vector managed by the Control object, the (artificial) ROI 
	 * covering the whole image is always at position 0. For sake of clarity,
	 * this constant is used in the code where appropriate.
	 */
    public static final int FULL_IMAGE_ROI = 0;

    /**
	 * The mode for filling long (otherwise empty) intervals in Match-Delay-Fill scripting algorithm. 
	 */
    int fillMode = FURTHEST_ROIS;

    /**
	 * Only keyword matching is to be applied
	 */
    public static final int KEYWORDS = 0;

    /**
	 * Matching that uses keywords and certain phrases is to be used (not yet implemented; code falls back to plain keyword matching)
	 */
    public static final int KEYWORDS_AND_PHRASES = 1;

    /**
	 * The match mode is the type of matching to be applied.
	 */
    int matchMode = KEYWORDS_AND_PHRASES;

    /**
	 * The subtitle segmenter is the component responsible for segmenting the text into subtitles (differently evolved implementations available)
	 */
    SubtitleSegmenter subtitleSegmenter = null;

    /**
	 * The subtitles generated for the output
	 */
    Subtitles subtitles;

    /**
	 * Gets the subtitle segmenter
	 */
    public SubtitleSegmenter getSubtitleSegmenter() {
        return subtitleSegmenter;
    }

    /**
	 * Sets the subtitle segmenter
	 * @param subtitleSegmenter the subtitle segmenter to set
	 */
    public void setSubtitleSegmenter(SubtitleSegmenter subtitleSegmenter) {
        this.subtitleSegmenter = subtitleSegmenter;
    }

    /**
	 * Get the match mode
	 * @return the match mode
	 */
    public int getMatchMode() {
        return matchMode;
    }

    /**
	 * Set the match mode (note that no non-default match modes are currently implemented)
	 * @param matchMode the new match mode
	 */
    public void setMatchMode(int matchMode) {
        this.matchMode = matchMode;
    }

    /**
	 * Detailed synchronization constraints that are generated by the scripting algorithm
	 */
    Vector<SyncObject> scriptUnderConstruction;

    /**
	 * Preliminary synchronization constraints found by matching text and annotations, then refined by temporal information obtained from the text duration estimator 
	 */
    Vector<SyncPoint> matches;

    /**
	 * The script produced by the control (null before a script was generated)
	 */
    Script finalScript = null;

    /**
	 * Dimensions of the image in pixels
	 */
    Dimension imageDim;

    /**
	 * Dimensions of the display in pixels
	 */
    Dimension displayDim;

    /**
	 * Padding to be applied. The padding is computed by scaling the display dimensions up to cover all of the image and the centering the image. The padding gives the top left point of the image (which will be of the form (x, 0) or (0, y)).
	 */
    Point pad;

    /**
	 * Maximum duration for the whole AV output in millis
	 */
    int maxDuration = 60000;

    public int getMaxDuration() {
        return maxDuration;
    }

    /**
	 * Sets the maximum duration
	 * @param maxDuration new maximum duration
	 */
    public void setMaxDuration(int maxDuration) {
        this.maxDuration = maxDuration;
    }

    /**
	 * Default value (will be used when reasonably possible) for stay durations
	 */
    int defaultStayDuration = 1500;

    /**
	 * Default value (will be used when reasonably possible) for motion durations
	 */
    int defaultMotionDuration = 3000;

    /**
	 * Minimum value (will never be violated) for stay durations
	 */
    int minStayDuration = 1300;

    /**
	 * Minimum value (will never be violated) for motion durations
	 */
    int minMotionDuration = 1500;

    int initialAudioDelay = defaultStayDuration + defaultMotionDuration;

    /**
	 * The scheduled outro duration is the duration that the outro is going to take
	 */
    int scheduledOutroDuration = defaultStayDuration + defaultMotionDuration;

    /**
	 * Unmatched ROIs (depending on sync mode and scripting algorithm, a subset of these will be inserted if there are long enough empty intervals)
	 */
    Vector<ROI> unmatchedROIs = null;

    /**
	 * Gets the initial audio delay
	 * @return the initial audio delay
	 */
    public int getInitialAudioDelay() {
        return initialAudioDelay;
    }

    /**
	 * Sets the initial audio delay
	 * @param the initialAudioDelay
	 */
    public void setInitialAudioDelay(int initialAudioDelay) {
        this.initialAudioDelay = initialAudioDelay;
    }

    /**
	 * The image metadata (as parsed from the mini7 file, currently)
	 */
    ImageMetadata metadata;

    /**
	 * All ROIs
	 */
    Vector<ROI> rois;

    String text;

    TextDurationEstimator textDurationEstimator;

    /**
	 * @return the textDurationEstimator
	 */
    public TextDurationEstimator getTextDurationEstimator() {
        return textDurationEstimator;
    }

    /**
	 * @param textDurationEstimator the textDurationEstimator to set
	 */
    public void setTextDurationEstimator(TextDurationEstimator textDurationEstimator) {
        this.textDurationEstimator = textDurationEstimator;
    }

    int estimatedTextDurationMillis = 55000;

    int syncMode = RIGID_AUDIO;

    public void setVisualData(Dimension imageDim, Dimension displayDim, ImageMetadata metadata) {
        this.imageDim = imageDim;
        this.displayDim = displayDim;
        this.metadata = metadata;
        rois = new Vector<ROI>();
        ROI fullImageRoi;
        fullImageRoi = new ROI(new Point(0, 0), imageDim, null);
        rois.add(fullImageRoi);
        rois.addAll(metadata.getRois());
        unmatchedROIs = new Vector<ROI>(rois);
        unmatchedROIs.remove(this.rois.elementAt(FULL_IMAGE_ROI));
        this.pad = Tools.computePadding(imageDim, displayDim);
    }

    /**
	 * Sets the text that is used by the VC control.
	 * This method assumes that visual data (ROIs, dimensions) are already set.
	 * @param text
	 */
    @Deprecated
    public void setTextAndMatch(String text) {
        setText(text);
        match();
    }

    /**
	 * This method assumes that text and visual data (ROIs, dimensions) are already set.
	 * The matches created by this method are sorted.
	 */
    public void match() {
        Matcher matcher;
        switch(matchMode) {
            case KEYWORDS:
                matcher = new KeywordMatcher();
                break;
            case KEYWORDS_AND_PHRASES:
            default:
                matcher = new KeywordAndPhraseMatcher();
        }
        matches = matcher.matchTextAndROIs(this.text, this.rois);
        unmatchedROIs = new Vector<ROI>(matcher.getUnvisitedROIs());
        unmatchedROIs.remove(this.rois.elementAt(FULL_IMAGE_ROI));
        this.sortMatchesByTextIndex();
        long rawEstimate = textDurationEstimator.estimate(this.text);
        if (rawEstimate > Integer.MAX_VALUE) rawEstimate = Integer.MAX_VALUE;
        estimatedTextDurationMillis = (int) rawEstimate;
    }

    /**
	 * Sets the text that will be used to guide the virtual camera
	 * @param text the text to be used
	 */
    public void setText(String text) {
        this.text = text;
    }

    /**
	 * Scripting method for when no audio is to be synchronized to the visual output.
	 * The text-to-image matches found before are incorporated into the temporary script.
	 */
    void performScriptingForOrderOnlyMode() {
        scriptUnderConstruction = new Vector<SyncObject>();
        int constructAt;
        scriptUnderConstruction.add(new SyncPoint(0, -1, FULL_IMAGE_ROI));
        scriptUnderConstruction.add(new SyncStay(defaultStayDuration));
        constructAt = scriptUnderConstruction.size();
        scriptUnderConstruction.add(new SyncMotionStub(0));
        scriptUnderConstruction.add(new SyncPoint(-1, -1, FULL_IMAGE_ROI));
        scriptUnderConstruction.add(new SyncStay(defaultStayDuration));
        for (int i = 0; i < matches.size(); i++) {
            SyncPoint match = matches.elementAt(i);
            scriptUnderConstruction.add(constructAt++, new SyncMotionStub(defaultMotionDuration));
            scriptUnderConstruction.add(constructAt++, new SyncPoint(match.getTextIndex(), -1, match.getRoiID()));
            scriptUnderConstruction.add(constructAt++, new SyncStay(defaultStayDuration));
        }
        ((SyncMotionStub) (scriptUnderConstruction.get(constructAt))).setMillis(defaultMotionDuration);
    }

    /**
	 * This method finalizes the previously created time-to-ROI matchings so that minimum durations hold and less busy periods are filled by additional ROIs
	 * Note that the <code>matches</code> field currently is manipulated by this method: the full image ROI appearances for intro and outro are added if they
	 * were not already present (checked individually for each of the appearances).
	 */
    void performMatchDelayFillScriptingForAudioSyncModes() {
        Vector<SyncObject> tempSyncs;
        tempSyncs = new Vector<SyncObject>();
        this.scriptUnderConstruction = new Vector<SyncObject>();
        int constructAt;
        int durationSoFar = 0;
        int lateMillis = 0;
        if (matches == null) {
            matches = new Vector<SyncPoint>();
        }
        if (matches.firstElement().getRoiID() != FULL_IMAGE_ROI) {
            matches.add(0, new SyncPoint(-1, 0, FULL_IMAGE_ROI));
        }
        int totalTime = initialAudioDelay + estimatedTextDurationMillis + scheduledOutroDuration;
        if (matches.lastElement().getRoiID() != FULL_IMAGE_ROI) {
            matches.add(new SyncPoint(-1, totalTime - (defaultMotionDuration), FULL_IMAGE_ROI));
        }
        constructAt = tempSyncs.size();
        int defaultStayAndMotionDuration = defaultStayDuration + defaultStayDuration;
        int minStayAndMotionDuration = minStayDuration + minMotionDuration;
        int currentStayDuration = 0;
        int currentMotionDuration = 0;
        int kwMatchesSize = matches.size();
        for (int i = 0; i < kwMatchesSize; i++) {
            SyncPoint match = matches.elementAt(i);
            tempSyncs.add(constructAt++, match);
        }
        int defaultStayAndTwoMotionsDuration = 2 * defaultMotionDuration + defaultStayDuration;
        int i = 0;
        int skip = 0;
        for (; i < tempSyncs.size() - 1; i++) {
            if (skip > 0) {
                i += skip;
            }
            SyncPoint startOfCurrentStay = (SyncPoint) tempSyncs.elementAt(i);
            if (startOfCurrentStay.getAudioMillis() == -1) {
                durationSoFar += lateMillis;
                lateMillis = 0;
                startOfCurrentStay.setAudioMillis(durationSoFar);
            } else if (lateMillis > 0) {
                durationSoFar += lateMillis;
                lateMillis = 0;
                startOfCurrentStay.setAudioMillis(durationSoFar);
            }
            SyncPoint startOfNextStay = (SyncPoint) tempSyncs.elementAt(i + 1);
            skip = 0;
            boolean nextStayDecided = false;
            while (!nextStayDecided) {
                int durationAvailableForThisStayAndMotion = 0;
                if (startOfNextStay.getAudioMillis() == -1) {
                    durationAvailableForThisStayAndMotion = defaultStayAndMotionDuration;
                } else {
                    durationAvailableForThisStayAndMotion = startOfNextStay.getAudioMillis() - durationSoFar;
                    System.out.println("DurationAvailable " + durationAvailableForThisStayAndMotion + " = audioMillis " + startOfNextStay.getAudioMillis() + " - durationSoFar " + durationSoFar);
                }
                if (durationAvailableForThisStayAndMotion < minStayAndMotionDuration) {
                    if (startOfNextStay.getNumberOfROIRepetition() > 0) {
                        if (i + 2 + skip < tempSyncs.size() - 2) {
                            System.out.println("After " + startOfCurrentStay.getTag() + ", skipping repetition of " + startOfNextStay.getTag() + " because we are in a hurry...");
                            skip++;
                            startOfNextStay = (SyncPoint) tempSyncs.elementAt(i + 1 + skip);
                            continue;
                        }
                    }
                    currentStayDuration = minStayDuration;
                    currentMotionDuration = minMotionDuration;
                    System.out.println("BEFORE: lateMillis = " + lateMillis);
                    lateMillis = minStayAndMotionDuration - durationAvailableForThisStayAndMotion;
                    if (lateMillis < 0) lateMillis = 0;
                    System.out.println("AFTER: lateMillis = " + lateMillis);
                } else {
                    if (durationAvailableForThisStayAndMotion > defaultStayAndMotionDuration) {
                        currentStayDuration = defaultStayDuration;
                        currentMotionDuration = durationAvailableForThisStayAndMotion - currentStayDuration;
                        if (currentMotionDuration > defaultStayAndTwoMotionsDuration) {
                            ROI newROI = removeAnUnmatchedROI(rois.get(startOfCurrentStay.getRoiID()), rois.get(startOfNextStay.getRoiID()));
                            if (newROI != null) {
                                currentMotionDuration = defaultMotionDuration;
                                startOfNextStay = new SyncPoint(-1, durationSoFar + currentStayDuration + currentMotionDuration, rois.indexOf(newROI));
                                tempSyncs.add(i + 1, startOfNextStay);
                                kwMatchesSize++;
                            }
                        }
                    } else {
                        float scale = ((float) durationAvailableForThisStayAndMotion) / defaultStayAndMotionDuration;
                        currentStayDuration = (int) (defaultStayDuration * scale);
                        currentMotionDuration = (int) (defaultMotionDuration * scale);
                    }
                }
                nextStayDecided = true;
            }
            scriptUnderConstruction.add(startOfCurrentStay);
            scriptUnderConstruction.add(new SyncStay(currentStayDuration));
            durationSoFar += currentStayDuration;
            scriptUnderConstruction.add(new SyncMotionStub(currentMotionDuration));
            durationSoFar += currentMotionDuration;
            if (startOfCurrentStay.getTag() != null) {
                Color hiliteColor = nextHiliteColor();
                subtitleMarkup.add(new SubtitleMarkup(startOfCurrentStay.getTextIndex(), startOfCurrentStay.getTag().length(), SubtitleMarkup.BOLD, null));
                subtitleMarkup.add(new SubtitleMarkup(startOfCurrentStay.getTextIndex(), startOfCurrentStay.getTag().length(), SubtitleMarkup.FONT, hiliteColor));
                startOfCurrentStay.setHiliteColor(hiliteColor);
            }
        }
        for (; i < tempSyncs.size(); i++) {
            scriptUnderConstruction.add(tempSyncs.elementAt(i));
        }
    }

    /**
	 * For cases where the maxDuration allows for far more ROIs than we have annotated, this method creates an artificial ROI
	 */
    void createArtificialROIs(int howMany) {
        int nCandidates = 2 * howMany;
        Vector<Rectangle> candidates = new Vector<Rectangle>();
        int maxX = imageDim.width - displayDim.width;
        int maxY = imageDim.height - displayDim.height;
        {
            int way = 0;
            if (way == 0) {
                for (int i = 0; i < nCandidates; i++) {
                    int x = rand.nextInt(maxX + 1);
                    int y = rand.nextInt(maxY + 1);
                    candidates.add(new Rectangle(new Point(x, y), displayDim));
                }
            } else if (way == 1) {
            }
        }
        {
            int way = 0;
            if (way == 0) {
                for (int i = 0; i < howMany; i++) {
                    Rectangle rect = candidates.remove(rand.nextInt(candidates.size()));
                    ROI roi = new ROI(rect.getLocation(), rect.getSize(), null);
                    roi.setStaticRelevance(ARTIFICIAL_RELEVANCE);
                    rois.add(roi);
                }
            } else if (way == 1) {
            }
        }
    }

    /**
	 * Returns all sync points that sync to the given ROI.
	 * The intended use case of this method is the following: for a ROI with multiple matches, choose the point of time where it fits best)
	 * @param hayStack the set of sync objects to be searched
	 * @param roiID the ROI whose sync'ed sync points are to be retrieved
	 * @return all sync points from hayStack that have roiID as their ROI
	 */
    Vector<SyncPoint> getSyncPointsForROI(Vector<? extends SyncObject> hayStack, int roiID) {
        Vector<SyncPoint> result = new Vector<SyncPoint>();
        for (SyncObject o : hayStack) {
            if (o instanceof SyncPoint) {
                SyncPoint syncPoint = (SyncPoint) o;
                if (syncPoint.getRoiID() == roiID) result.add(syncPoint);
            }
        }
        return result;
    }

    /**
	 * returns all sync points in the interval starting at startMillis and going on for durationMillis millis
	 * (purpose: choose the most relevant ROI to be selected from several matches for a given period)
	 * @param hayStack the set of sync objects to be searched
	 * @param startMillis start of interval to be considered
	 * @param durationMillis duration of interval to be considerded
	 * @return all sync points from haystack that have their audioMillis specified (>-1) and in the given interval (inclusive)
	 */
    Vector<SyncPoint> getSyncPointsForTimeFrame(Vector<?> hayStack, int startMillis, int durationMillis) {
        Vector<SyncPoint> result = new Vector<SyncPoint>();
        for (Object o : hayStack) {
            if (o instanceof SyncPoint) {
                SyncPoint syncPoint = (SyncPoint) o;
                if (syncPoint.getAudioMillis() >= startMillis && syncPoint.getAudioMillis() <= startMillis + durationMillis) {
                    result.add(syncPoint);
                }
            }
        }
        return result;
    }

    /**
	 * Adds the ROI in a new sync point within the timeSlot and returns the objects that the timeSlot is to be replaced by (this might contain a new timeslot containing the remainder)
	 * @param timeSlot the time slot in which the sync point for the ROI is to be inserted
	 * @param targetTime the time at which the sync point is to be inserted (this might be a "don't care" value of -1, and it might be overridden/approximated as well as possible in this method)
	 * @param roi the ROI to be added
	 * @return the elements that the timeSlot is to be replaced by (this may include a residual time slot) in the correct order
	 */
    private boolean addROIToTimeSlot(SyncMotionStub timeSlot, int targetTime, int roi, int textIndex, String tag) {
        int newRoiDuration = minStayDuration + minMotionDuration;
        if (timeSlot.getMillis() < newRoiDuration + minMotionDuration) {
            return false;
        }
        int effectiveTargetTime = targetTime;
        if (effectiveTargetTime > timeSlot.getTempStartMillis() + timeSlot.getMillis() - newRoiDuration) {
            effectiveTargetTime = timeSlot.getTempStartMillis() + timeSlot.getMillis() - newRoiDuration;
        }
        if (effectiveTargetTime < timeSlot.getTempStartMillis() + minMotionDuration) {
            effectiveTargetTime = timeSlot.getTempStartMillis() + minMotionDuration;
        }
        SyncMotionStub pre = new SyncMotionStub();
        SyncMotionStub post = new SyncMotionStub();
        SyncPoint sp = new SyncPoint(textIndex, effectiveTargetTime, roi);
        sp.setTag(tag);
        SyncStay stay = new SyncStay();
        pre.setTempStartMillis(timeSlot.getTempStartMillis());
        pre.setMillis(effectiveTargetTime - timeSlot.getTempStartMillis());
        stay.setMillis(minStayDuration);
        post.setTempStartMillis(effectiveTargetTime + minStayDuration);
        post.setMillis(timeSlot.getTempStartMillis() + timeSlot.getMillis() - effectiveTargetTime - newRoiDuration);
        Vector<SyncObject> replacementForTimeSlot = new Vector<SyncObject>();
        if (pre.getMillis() > newRoiDuration + minMotionDuration) {
            replacementForTimeSlot.add(pre);
        }
        replacementForTimeSlot.add(sp);
        replacementForTimeSlot.add(stay);
        if (post.getMillis() > newRoiDuration + minMotionDuration) {
            replacementForTimeSlot.add(post);
        }
        int timeSlotPosition = scriptUnderConstruction.indexOf(timeSlot);
        scriptUnderConstruction.remove(timeSlotPosition);
        scriptUnderConstruction.addAll(timeSlotPosition, replacementForTimeSlot);
        System.out.println("Time slot of " + timeSlot.getMillis() + " split into " + pre.getMillis() + "  + " + minMotionDuration + "+ " + stay.getMillis() + " + " + post.getMillis());
        return true;
    }

    /**
	 * Relevance assigned to annotated unmatched ROIS
	 */
    static final float UNMATCHED_RELEVANCE = 0.01f;

    /**
	 * Relevance assigned to repeated ROIs
	 */
    static final float REPEATED_RELEVANCE = -0.01f;

    /**
	 * Relevance assigned to artificial (and thus always unmatched) ROIs
	 */
    static final float ARTIFICIAL_RELEVANCE = 0.0f;

    void performRelevanceBasedScriptingForAudioSyncModes() {
        boolean canDoMore = true;
        int minStayAndMotion = this.minMotionDuration + this.minStayDuration;
        int maxROIs = maxDuration / minStayAndMotion;
        int neededROIs = maxROIs - this.rois.size();
        if (neededROIs > 0) {
            createArtificialROIs(neededROIs);
        }
        float[] dynamicRelevance = new float[this.rois.size()];
        for (int i = 1; i < this.rois.size(); i++) {
            float value = this.rois.elementAt(i).getStaticRelevance();
            if (getSyncPointsForROI(matches, i).size() == 0) {
                if (value > 0) value = UNMATCHED_RELEVANCE;
            }
            dynamicRelevance[i] = value;
        }
        dynamicRelevance[FULL_IMAGE_ROI] = 0f;
        int mostRelevant;
        Vector<SyncPoint> candidateSyncs;
        scriptUnderConstruction = new Vector<SyncObject>();
        scriptUnderConstruction.add(new SyncPoint(0, 0, FULL_IMAGE_ROI));
        scriptUnderConstruction.add(new SyncStay(minStayDuration));
        SyncMotionStub timeSlot = new SyncMotionStub(maxDuration - 2 * minStayDuration);
        timeSlot.setTempStartMillis(minStayDuration);
        scriptUnderConstruction.add(timeSlot);
        scriptUnderConstruction.add(new SyncStay(minStayDuration));
        scriptUnderConstruction.add(new SyncPoint(0, 0, FULL_IMAGE_ROI));
        int targetTime;
        int textIndex;
        String tag;
        canDoMore = (timeSlot.getMillis() > minStayDuration + 2 * minMotionDuration);
        while (canDoMore) {
            targetTime = -1;
            textIndex = -1;
            tag = null;
            mostRelevant = findMax(dynamicRelevance);
            candidateSyncs = getSyncPointsForROI(matches, mostRelevant);
            if (candidateSyncs != null) {
                for (SyncPoint candidate : candidateSyncs) {
                    if (candidate.getAudioMillis() < 0) continue;
                    for (int i = 0; i < scriptUnderConstruction.size(); i++) {
                        SyncObject o = scriptUnderConstruction.elementAt(i);
                        if (o instanceof SyncMotionStub) {
                            timeSlot = (SyncMotionStub) o;
                            if (timeSlot.getMillis() > minStayDuration + 2 * minMotionDuration) {
                                if (timeSlot.getTempStartMillis() + minMotionDuration < candidate.getAudioMillis() && candidate.getAudioMillis() < timeSlot.getTempStartMillis() + timeSlot.getMillis()) {
                                    targetTime = candidate.getAudioMillis();
                                    textIndex = candidate.getTextIndex();
                                    tag = candidate.getTag();
                                    System.out.println("Attempting to fix match for " + tag);
                                    break;
                                }
                            }
                        }
                    }
                }
            }
            if (targetTime < 0) {
                if (dynamicRelevance[mostRelevant] == this.rois.elementAt(mostRelevant).getStaticRelevance() && dynamicRelevance[mostRelevant] > UNMATCHED_RELEVANCE) {
                    dynamicRelevance[mostRelevant] *= 0.99f;
                    continue;
                }
                for (int i = 0; i < scriptUnderConstruction.size(); i++) {
                    SyncObject o = scriptUnderConstruction.elementAt(i);
                    if (o instanceof SyncMotionStub) {
                        timeSlot = (SyncMotionStub) o;
                        break;
                    }
                }
            }
            if (!addROIToTimeSlot(timeSlot, targetTime, mostRelevant, textIndex, tag)) {
                System.out.println("Failed to add ROI at given timeslot!");
                dynamicRelevance[mostRelevant] = 0;
            } else {
                dynamicRelevance[mostRelevant] = REPEATED_RELEVANCE;
            }
            canDoMore = false;
            for (int i = scriptUnderConstruction.size() - 1; i >= 0; i--) {
                SyncObject o = scriptUnderConstruction.elementAt(i);
                if (o instanceof SyncMotionStub) {
                    timeSlot = (SyncMotionStub) o;
                    if (timeSlot.getMillis() > minStayDuration + 2 * minMotionDuration) {
                        canDoMore = true;
                    } else {
                        scriptUnderConstruction.remove(i);
                    }
                }
            }
        }
        int durationSoFar = 0;
        for (int i = 0; i == scriptUnderConstruction.size(); i++) {
            SyncObject o = scriptUnderConstruction.elementAt(i);
            if (o instanceof SyncPoint) {
                SyncPoint p = (SyncPoint) o;
                if (p.getAudioMillis() > 0) {
                    if (p.getAudioMillis() > durationSoFar) {
                        scriptUnderConstruction.insertElementAt(new SyncMotionStub(p.getAudioMillis() - durationSoFar), i);
                        durationSoFar = p.getAudioMillis();
                        i++;
                        continue;
                    }
                } else {
                    scriptUnderConstruction.insertElementAt(new SyncMotionStub(minMotionDuration), i);
                    durationSoFar += minMotionDuration;
                    i++;
                    continue;
                }
            }
        }
    }

    /**
	 * Finds the highest value of an array and returns its index.
	 * @param values the array of values to be searched
	 * @return -1 if values is null or has 0 length; the index of the largest element otherwise
	 */
    static int findMax(float[] values) {
        if (values == null || values.length == 0) return -1;
        int candidateIndex = 0;
        float candidateValue = values[0];
        for (int i = 1; i < values.length; i++) {
            if (values[i] > candidateValue) {
                candidateValue = values[i];
                candidateIndex = i;
            }
        }
        return candidateIndex;
    }

    /**
	 * Sorts the previously found matches by text index
	 */
    void sortMatchesByTextIndex() {
        Collections.sort(matches, new TextIndexComparator());
    }

    void mergeROIs() {
    }

    /**
	 * This method uses the text duration estimator's capabilities to assign time values
	 * to the matches of text and image annotations found in previous steps.
	 */
    void addTextTiming() {
        int endIndex;
        int i;
        SyncPoint p;
        for (i = 0; i < this.matches.size(); i++) {
            p = matches.elementAt(i);
            endIndex = p.getTextIndex();
            if (endIndex > -1) {
                int d;
                d = initialAudioDelay + (int) textDurationEstimator.estimate(text, 0, endIndex);
                System.out.println(d);
                p.setAudioMillis(d);
            }
        }
    }

    @Deprecated
    boolean mustReduceScript() {
        return (finalScript.getDurationMillis() > this.maxDuration);
    }

    /**
	 * Reduces the script under construction temporally if required to fit the maximum duration.
	 * Note that this works only for SyncMode=ORDER_ONLY and only if mustReduceScript() returns true.
	 * For other SyncModes, the maxDuration should be considered already in precedings steps.
	 */
    void scaleScriptDownTemporallyIfRequired() {
        if (this.getSyncMode() == ORDER_ONLY) {
            if (this.finalScript == null) generateAndRememberScript();
            int overflowDuration = finalScript.getDurationMillis() - this.maxDuration;
            if (overflowDuration < 0) return;
            int durationsCount = 0;
            for (SyncObject o : scriptUnderConstruction) {
                if (o instanceof SyncDuration) {
                    durationsCount++;
                }
            }
            float targetRatio = (float) (finalScript.getDurationMillis()) / this.maxDuration;
            scaleScriptTemporally(targetRatio);
        }
    }

    /**
	 * Scales the script under construction to be generated by multiplying all durations and absolute time indices by targetRatio.
	 * Note that this makes sense only for SyncMode=ORDER_ONLY.
	 * @param tartetRatio the ratio by which each time value (duration and absolute time point) is to be scaled.
	 * Make it >1 for the script to grow, between 0 and 1 to shrink it.
	 */
    void scaleScriptTemporally(float targetRatio) {
        for (SyncObject o : scriptUnderConstruction) {
            if (o instanceof SyncDuration) {
                SyncDuration d = (SyncDuration) o;
                d.setMillis((int) (d.getMillis() * targetRatio));
            } else if (o instanceof SyncPoint) {
                SyncPoint p = (SyncPoint) o;
                p.setAudioMillis((int) (p.getAudioMillis() * targetRatio));
            }
        }
    }

    /**
	 * Source for random numbers
	 */
    Random rand = new Random();

    /**
	 * From the set of unmatched ROIs managed by this Control, this method removes an unmatched ROI 
	 * and returns it. (The intention is to also USE it (include in script) after retrieving it by
	 * this method, otherwise the fact that it is unmatched will be lost.) 
	 * The choice of the ROI is determined according to the currently set fill mode.
	 * Some fillModes (nearest, furthest) might consider the preceding and succeeding ROIs, which are 
	 * to be given in r1 and r2. Other fill modes (random) might ignore them. 
	 * @param r1
	 * @param r2
	 * @return
	 */
    ROI removeAnUnmatchedROI(Rectangle r1, Rectangle r2) {
        if (unmatchedROIs.size() == 0) return null;
        ROI result;
        switch(fillMode) {
            case RANDOM_ROIS:
                int i = rand.nextInt(unmatchedROIs.size());
                result = unmatchedROIs.elementAt(i);
                break;
            case FURTHEST_ROIS:
                result = (ROI) (Tools.getFurthest(r1, r2, unmatchedROIs));
                break;
            case NEAREST_ROIS:
            default:
                result = (ROI) Tools.getNearest(r1, r2, unmatchedROIs);
        }
        if (result != null) {
            unmatchedROIs.remove(result);
        }
        return result;
    }

    /**
	 * Generates the final script from the script under construction and stores it in the respective field.
	 * @return
	 */
    Script generateAndRememberScript() {
        this.finalScript = generateScript();
        return this.finalScript;
    }

    /**
	 * Generates the final script from the script under construction but does not store it. (This could be used for temporary script creation if it is to be re-generated after some further decisions.)
	 * @return the script
	 */
    Script generateScript() {
        Script script;
        script = new Script();
        script.setDisplaySize(this.displayDim);
        script.setPictureSize(this.imageDim);
        TreeMap<Integer, ViewBox> viewboxes = new TreeMap<Integer, ViewBox>();
        int timeIndex = 0;
        ViewBox currentViewBox = null;
        for (SyncObject o : scriptUnderConstruction) {
            if (o instanceof SyncPoint) {
                SyncPoint p = (SyncPoint) o;
                if (p.getAudioMillis() > timeIndex) {
                    timeIndex = p.getAudioMillis();
                }
                if (p.getRoiID() > -1) {
                    currentViewBox = viewBoxForROI(rois.elementAt(p.getRoiID()));
                    currentViewBox.setStayDurationMillis(0);
                    if (p.getTag() != null) {
                        currentViewBox.setTag("\"" + p.getTag() + "\" at text position " + p.getTextIndex());
                    }
                    currentViewBox.setHiliteColor(p.getHiliteColor());
                    viewboxes.put(timeIndex, currentViewBox);
                }
            } else if (o instanceof SyncDuration) {
                int duration = ((SyncDuration) o).getMillis();
                if (o instanceof SyncStay) {
                    currentViewBox.setStayDurationMillis(duration + currentViewBox.getStayDurationMillis());
                }
            }
        }
        script.setImageURL(metadata.getImageURL());
        script.setViewBoxTimeMap(viewboxes);
        return script;
    }

    /**
	 * Constructs a ViewBox script element for the given ROI. 
	 * The view box will be concentric with the ROI. The view box will not exceed the
	 * image if possible. It will at most exceed the image by a range that is still
	 * within the padding. 
	 * @param roi the roi to be covered with a new ViewBox
	 * @return the ViewBox by which roi will be shown in the final output
	 */
    ViewBox viewBoxForROI(Rectangle roi) {
        float scaleX = ((float) roi.width) / displayDim.width;
        float scaleY = ((float) roi.height) / displayDim.height;
        int vbX, vbY;
        int vbW, vbH;
        if ((scaleX > 1) || (scaleY > 1)) {
            if (scaleX > scaleY) {
                vbX = roi.x;
                vbW = (int) (displayDim.width * scaleX);
                vbH = (int) (displayDim.height * scaleX);
                vbY = roi.y - (vbH - roi.height) / 2;
            } else {
                vbY = roi.y;
                vbW = (int) (displayDim.width * scaleY);
                vbH = (int) (displayDim.height * scaleY);
                vbX = roi.x - (vbW - roi.width) / 2;
            }
        } else {
            System.out.println("Preventing overzooming...");
            vbW = displayDim.width;
            vbH = displayDim.height;
            vbX = roi.x - (vbW - roi.width) / 2;
            vbY = roi.y - (vbH - roi.height) / 2;
        }
        int padX = pad.x;
        int padY = pad.y;
        if (vbW > imageDim.width) {
            if (vbX < (0 - padX)) {
                vbX = 0 - padX;
            } else if (vbX + vbW > (imageDim.width + padX)) {
                vbX = imageDim.width + padX - vbW;
            }
        } else {
            if (vbX < 0) {
                vbX = 0;
            } else if (vbX + vbW > (imageDim.width + padX)) {
                vbX = imageDim.width - vbW;
            }
        }
        if (vbH > imageDim.height) {
            if (vbY < (0 - padY)) {
                vbY = 0 - padY;
            } else if (vbY + vbH > (imageDim.height + padY)) {
                vbY = imageDim.height + padY - vbH;
            }
        } else {
            if (vbY < 0) {
                vbY = 0;
            } else if (vbY + vbH > (imageDim.height)) {
                vbY = imageDim.height - vbH;
            }
        }
        ViewBox result = new ViewBox(vbX, vbY, vbW, vbH);
        result.setRoi(roi);
        return result;
    }

    /**
	 * Scripting is the process of setting detailed synchronization constraints, usually based
	 * upon the matching and other preprocessing steps performed before.
	 * This method delegates to different implementations, depending on the chosen
	 * sync mode and preferences (e.g., relevance-based algorithm).
	 */
    void performScripting() {
        if (this.minMotionDuration > this.defaultMotionDuration) {
            this.defaultMotionDuration = this.minMotionDuration;
        }
        if (this.minStayDuration > this.defaultStayDuration) {
            this.defaultStayDuration = this.minStayDuration;
        }
        if (syncMode == ORDER_ONLY) {
            this.performScriptingForOrderOnlyMode();
        } else if (syncMode == RIGID_AUDIO || syncMode == ARRANGABLE_AUDIO) {
            if (useRelevance) {
                performRelevanceBasedScriptingForAudioSyncModes();
            } else {
                performMatchDelayFillScriptingForAudioSyncModes();
            }
        }
    }

    /**
	 * Executes the control steps until finalization of the script, assuming that all preparatory steps are already done 
	 */
    public Script executeControl() {
        if (this.text == null) this.text = "";
        if (this.text.isEmpty()) {
            this.syncMode = RIGID_AUDIO;
            System.out.println("Warning: Changed sync mode to RigidAudio because others are not compatible with not using text");
        } else {
            if (this.syncMode == ARRANGABLE_AUDIO) {
                this.trimTextToMaxDuration();
            }
            if (!this.text.isEmpty()) {
                this.match();
                this.adjustOrientation();
                if (this.syncMode != ORDER_ONLY) {
                    this.addTextTiming();
                }
            }
        }
        this.performScripting();
        if (!this.text.isEmpty()) {
            if (syncMode == ORDER_ONLY) {
                this.scaleScriptDownTemporallyIfRequired();
            }
            if (this.syncMode == SUBTITLES || this.additionalSubtitles == SUBTITLES) {
                this.subtitles = subtitleSegmenter.getSegments(text, this.subtitleMarkup);
            }
        }
        return this.generateAndRememberScript();
    }

    /**
	 * @return the default duration for motions
	 */
    public int getDefaultMotionDuration() {
        return defaultMotionDuration;
    }

    /**
	 * Sets the default duration for motions
	 * @param defaultMotionDuration the new default duration for motions
	 */
    public void setDefaultMotionDuration(int defaultMotionDuration) {
        this.defaultMotionDuration = defaultMotionDuration;
    }

    /**
	 * @return the default duration for stays
	 */
    public int getDefaultStayDuration() {
        return defaultStayDuration;
    }

    /**
	 * Sets the default duration for stays
	 * @param defaultStayDuration new default duration for stays
	 */
    public void setDefaultStayDuration(int defaultStayDuration) {
        this.defaultStayDuration = defaultStayDuration;
    }

    /**
	 * Returns the last script produced by this Control (without checking if any was produced yet). 
	 * @return this Control's script
	 */
    public Script getScript() {
        return finalScript;
    }

    /**
	 * Gets the sync mode
	 */
    public int getSyncMode() {
        return syncMode;
    }

    /**
	 * Sets the sync mode
	 * @param syncMode new sync mode
	 */
    public void setSyncMode(int syncMode) {
        this.syncMode = syncMode;
    }

    /**
	 * 
	 * @return this Control's text duration estimator if it is also a TTSTool; null otherwise.
	 */
    public TTSTool getTtsTool() {
        if (this.textDurationEstimator instanceof TTSTool) {
            return (TTSTool) textDurationEstimator;
        }
        return null;
    }

    /**
	 * Sets this Control's text duration estimator (!). 
	 * Note that for a Control instance its TTSTool and 
	 * its text duration estimator are the same object; 
	 * however, a TextDurationEstimator does not have to actually 
	 * be a TTSTool, in which case specific TTSTool tasks cannot be performed.
	 * @param ttsTool the TTSTool that will be used as this Control's text duration estimator.
	 */
    public void setTtsTool(TTSTool ttsTool) {
        this.textDurationEstimator = ttsTool;
    }

    /**
	 * Delegates to the TTSTool to generate synthetic speech audio from the text string that is used by this Control to guide the virtual camera.
	 * @param os output stream to write the audio data to
	 * @return true on success, else otherwise
	 */
    public boolean generateWAV(OutputStream os) {
        TTSTool ttsTool = this.getTtsTool();
        if (ttsTool == null) {
            return false;
        }
        if (!(prolog.isEmpty())) {
            return ttsTool.generateWAVOutput(this.prolog + ".\n" + this.text, os);
        } else {
            return ttsTool.generateWAVOutput(this.text, os);
        }
    }

    /**
	 * Text to be uttered before the main text. 
	 * Prolog text is not considered for matching with 
	 * image annotations. It only matters for reasons of
	 * delaying main text's utterance (and thus the
	 * synchronized VC).
	 */
    String prolog = "";

    /**
	 * Estimated duration of the utterance of the prolog in milliseconds.
	 */
    int prologDuration = 0;

    /**
	 * Text to be uttered before the main text. 
	 * Prolog text is not considered for matching with 
	 * image annotations. It only matters for reasons of
	 * delaying main text's utterance (and thus the
	 * synchronized VC).
	 * Example usage would be having the name of a painting
	 * spoken.
	 * Setting a prolog overrides any set or default value
	 * for initial delay.
	 * Prolog utterance is currently not correctly handled.
	 * Possible future extension: design markup language for
	 * text that allows marking text as "to be matched", thus
	 * integrating prolog functionality as well as other 
	 * to-be-ignored text.
	 */
    public String getProlog() {
        return prolog;
    }

    public void setProlog(String prolog) {
        this.prolog = prolog;
        this.prologDuration = (int) this.textDurationEstimator.estimate(this.prolog);
        this.initialAudioDelay = prologDuration;
    }

    /**
	 * Delegates to the TTSTool to generate synthetic speech audio from the prolog string (note that this is currently not used in the overall system)
	 * @param os output stream to write the audio data to
	 * @return true on success, else otherwise
	 */
    public boolean generatePrologWAV(OutputStream os) {
        TTSTool ttsTool = this.getTtsTool();
        if (ttsTool == null) {
            return false;
        }
        return ttsTool.generateWAVOutput(this.prolog, os);
    }

    /**
	 * the fill mode (for the Match-Delay-Fill scripting algorithm)
	 * @return the fill mode (for the Match-Delay-Fill scripting algorithm)
	 */
    public int getFillMode() {
        return fillMode;
    }

    /**
	 * Sets the fill mode (for the Match-Delay-Fill scripting algorithm)
	 * @param fillMode the new value
	 */
    public void setFillMode(int fillMode) {
        this.fillMode = fillMode;
    }

    /**
	 * Sets the fill mode (for the Match-Delay-Fill scripting algorithm) by the new value's string representation
	 * @param fillModeString the new value's string representation
	 */
    public void setFillMode(String fillModeString) {
        this.fillMode = Tools.getIndex(fillModeString, FILL_MODE_STRINGS);
        if (this.fillMode < 0) {
            throw new IllegalArgumentException("Illegal value for fill mode: " + fillModeString);
        }
    }

    /**
	 * Sets the sync mode by the new value's string representation
	 * @param syncModeString the new value's string representation
	 */
    public void setSyncMode(String syncModeString) {
        this.syncMode = Tools.getIndex(syncModeString, SYNC_MODE_STRINGS);
        if (this.syncMode < 0) {
            throw new IllegalArgumentException("Illegal value for sync mode: " + syncModeString);
        }
    }

    /**
	 * Splits the set text into sentences and returns their starting indices. Not smartly implemented yet, fooled by abbreviations, any other use of dots and not caring for non-dot sentence endings at the moment.
	 * @return the text indices of supposed sentence beginnings
	 */
    int[] getSentenceBeginnings() {
        return Tools.getSentenceBeginnings(text);
    }

    /**
	 * This method shortens the given text so that it fits the maxDuration constraint.
	 * It consults the text duration estimator methods and the initialDelay.
	 */
    void trimTextToMaxDuration() {
        int[] sentenceBeginIndices = this.getSentenceBeginnings();
        int[] sentenceBeginTimes = new int[sentenceBeginIndices.length + 1];
        sentenceBeginTimes[0] = this.getInitialAudioDelay();
        int endOfSentenceIndex;
        for (int i = 1; i < sentenceBeginIndices.length + 1; i++) {
            endOfSentenceIndex = (i < sentenceBeginIndices.length ? sentenceBeginIndices[i] : text.length());
            sentenceBeginTimes[i] = this.getInitialAudioDelay() + (int) textDurationEstimator.estimate(text, 0, endOfSentenceIndex);
            if (sentenceBeginTimes[i] > (maxDuration - scheduledOutroDuration)) {
                String newText = this.text.substring(0, sentenceBeginIndices[i - 1]);
                System.out.println("Removing text from position " + sentenceBeginIndices[i - 1] + " on due to time excess: " + text.substring(sentenceBeginIndices[i - 1]));
                if (newText.trim().length() == 0) {
                    System.out.println("Well, that's pretty much all we had, isn't it... Oh well, what the hell...");
                }
                this.text = newText;
                return;
            }
        }
    }

    /**
	 * This method adds timing information to the already prepared subtitles
	 * and returns them
	 * @return finalized subtitles for the script that is being made currently
	 */
    public Iterable<? extends SubtitleSegment> finalizeSubtitles() {
        if (subtitles == null) {
            return null;
        }
        int[] segmentStart = subtitles.getSegmentStartIndices();
        int[] segmentStartMillis = subtitles.getSegmentStartTimes();
        int[] segmentEndMillis = subtitles.getSegmentEndTimes();
        int start;
        int end;
        int startTime;
        int endTime;
        for (int i = 0; i < segmentStartMillis.length; i++) {
            start = segmentStart[i];
            if (i < segmentStart.length - 1) {
                end = segmentStart[i + 1];
            } else {
                end = this.text.length();
            }
            startTime = this.initialAudioDelay + (int) this.textDurationEstimator.estimate(text, 0, start);
            segmentStartMillis[i] = startTime;
            endTime = this.initialAudioDelay + (int) this.textDurationEstimator.estimate(text, 0, end);
            segmentEndMillis[i] = endTime;
        }
        return subtitles.asSegments();
    }

    /**
	 * Determines if a given dimension is to be counted as landscape or portrait, or if it is square.
	 * @param dim the dimension to be checked
	 * @return LANDSCAPE if dim's height is smaller than dim's width, KEEP_ORIENTATION if they are equal and PORTRAIT otherwise.
	 */
    static int getOrientationOf(Dimension dim) {
        if (dim.width == dim.height) {
            return KEEP_ORIENTATION;
        }
        if (dim.height > dim.width) {
            return PORTRAIT;
        }
        return LANDSCAPE;
    }

    /**
	 * If the Control is configured to do so (by the value of the orientation field),
	 * this method checks if output height and width are to be swapped and if yes, it swaps them.
	 * This method is to be called after matches are found so that it is able to consider
	 * only the ROIs that are likely to appear in the output.
	 * 
	 */
    void adjustOrientation() {
        if (this.orientation != BEST_ORIENTATION) return;
        int portraitCount = 0;
        int landscapeCount = 0;
        Rectangle r;
        int currentOrientation;
        for (SyncPoint p : this.matches) {
            r = this.rois.elementAt(p.getRoiID());
            currentOrientation = getOrientationOf(r.getSize());
            if (currentOrientation == PORTRAIT) portraitCount++; else if (currentOrientation == LANDSCAPE) landscapeCount++;
        }
        if (portraitCount > landscapeCount) {
            setOrientation(PORTRAIT);
        } else if (portraitCount == landscapeCount) {
            setOrientation(getOrientationOf(this.displayDim));
        } else setOrientation(LANDSCAPE);
    }

    /**
	 * Set whether to use the relevance based scripting algorithm (for the audio sync modes only)
	 * @param useRelevance whether to use the relevance based scripting algorithm
	 */
    public void useRelevance(boolean useRelevance) {
        this.useRelevance = useRelevance;
    }

    /**
	 * @return whether this Control is set to use the relevance based scripting algorithm (for the audio sync modes only)
	 */
    public boolean useRelevance() {
        return useRelevance;
    }

    /**
	 * counter for iterating through the hilite colors
	 */
    int hiliteCount = 0;

    /**
	 * Colors for highlighting ROIs - to be made configurable in a good way
	 */
    Color[] hiliteColors = new Color[] { Color.cyan, Color.yellow, Color.magenta };

    /**
	 * As a prototype implementation of an approach to obtaining colors for highlighting 
	 * ROIs and their mentioning in subtitles, this method iterates 
	 * through the hiliteColors set for this object, with the next call after
	 * obtaining the last color going back to the first.
	 * A more advanced implementation could consider to choose a color guaranteed to be
	 * well visible when applied over the concerned image regions if region color 
	 * metadata is available.
	 * @return the next color in the defined circle.
	 */
    Color nextHiliteColor() {
        Color result;
        result = hiliteColors[hiliteCount % hiliteColors.length];
        hiliteCount++;
        return result;
    }
}
