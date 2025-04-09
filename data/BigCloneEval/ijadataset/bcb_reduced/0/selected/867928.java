package de.bielefeld.uni.cebitec.repfish.tasks;

import de.bielefeld.uni.cebitec.repfish.QGramCoder;
import de.bielefeld.uni.cebitec.repfish.Task;
import de.bielefeld.uni.cebitec.repfish.acefile.AceFile;
import de.bielefeld.uni.cebitec.repfish.acefile.Contig;
import de.bielefeld.uni.cebitec.repfish.acefile.QGramIndex;
import de.bielefeld.uni.cebitec.repfish.acefile.QGramResult;
import de.bielefeld.uni.cebitec.repfish.acefile.QGramResultSet;
import de.bielefeld.uni.cebitec.repfish.acefile.Read;
import java.util.BitSet;

/**
 *
 * @author Patrick Schwientek (pschwien at cebitec.uni-bielefeld.de)
 */
public class TaskQueryQGramIndexThreadsafeSingleMaximal extends Task {

    public static final double MIN_MATCH_PERCENT = 1 / 3;

    public static final int MIN_MATCH_QGRAM = 3;

    private AceFile aceFile;

    private QGramIndex qGramIndex;

    private QGramResultSet[] resultSet;

    private int processedOverlappingQueryReads;

    private int totalOverlappingQueryReads;

    private int contigNumber2Search;

    private QGramCoder coder;

    public TaskQueryQGramIndexThreadsafeSingleMaximal(QGramIndex qGramIndex, QGramResultSet[] resultSet, AceFile aceFile, int contigNumber2Search) {
        this.qGramIndex = qGramIndex;
        this.aceFile = aceFile;
        this.contigNumber2Search = contigNumber2Search;
        this.resultSet = resultSet;
        coder = new QGramCoder(qGramIndex.getQLength());
    }

    private void queryIndex(byte[] queryChars, boolean reverseComplementDirection, BitSet bitSet) {
        int[] hashTable = qGramIndex.getHashTable();
        int[] occurrenceTable = qGramIndex.getOccurrenceTable();
        int qLength = coder.getQLength();
        bitSet.clear();
        coder.reset();
        int endPos, queryOffset;
        if (reverseComplementDirection) {
            endPos = 0;
            queryOffset = queryChars.length - 1;
        } else {
            endPos = queryChars.length - 1;
            queryOffset = 0;
        }
        boolean nextBase = true;
        int code;
        while (nextBase) {
            code = coder.updateEncoding(queryChars[queryOffset], reverseComplementDirection);
            if (code != -1) {
                for (int occOffset = hashTable[code]; occOffset < hashTable[code + 1]; occOffset++) {
                    int i = occurrenceTable[occOffset];
                    bitSet.set(i, i + qLength);
                }
            }
            if (!reverseComplementDirection) {
                if (queryOffset < endPos) {
                    queryOffset++;
                } else {
                    nextBase = false;
                }
            } else {
                if (queryOffset > endPos) {
                    queryOffset--;
                } else {
                    nextBase = false;
                }
            }
        }
    }

    private QGramResult queryIndexNew(byte[] queryChars, boolean reverseComplementDirection, int excludeFrom, int excludeTo) {
        int[] hashTable = qGramIndex.getHashTable();
        int[] occurrenceTable = qGramIndex.getOccurrenceTable();
        int qLength = coder.getQLength();
        coder.reset();
        int endPos, queryOffset;
        if (reverseComplementDirection) {
            endPos = 0;
            queryOffset = queryChars.length - 1;
        } else {
            endPos = queryChars.length - 1;
            queryOffset = 0;
        }
        boolean nextBase = true;
        int code;
        int occurenceIndex = 0;
        Seed firstBufferSeed = null;
        Seed firstSeed = null;
        Seed lastSeed = null;
        Seed currentSeed = null;
        QGramResult maxSeed = new QGramResult();
        Seed linkHelperForward = null;
        Seed linkHelperBackward = null;
        int matchIncrement = 1;
        int occurenceMaxIndex = 0;
        int currentMatchPosition = 0;
        boolean debug = false;
        while (nextBase) {
            if (debug) System.out.println("#BASE " + queryOffset + "/" + endPos + " occurences: " + (occurenceMaxIndex - occurenceIndex) + ")####");
            code = coder.updateEncoding(queryChars[queryOffset], reverseComplementDirection);
            if (code != -1) {
                occurenceIndex = hashTable[code];
                occurenceMaxIndex = hashTable[code + 1];
                currentSeed = firstSeed;
                while (currentSeed != null || occurenceIndex < occurenceMaxIndex) {
                    if (currentSeed != null && currentSeed.skipOnNextContact) {
                        if (debug) System.out.println("Skipping the current Seed");
                        currentSeed.skipOnNextContact = false;
                        currentSeed = currentSeed.next;
                        continue;
                    }
                    if (currentSeed != null && occurenceIndex < occurenceMaxIndex) {
                        currentMatchPosition = occurrenceTable[occurenceIndex];
                        if (currentMatchPosition == currentSeed.lastMatchPosition + matchIncrement) {
                            if (debug) System.out.println("found a match increment");
                            currentSeed.lastMatchPosition += matchIncrement;
                            currentSeed.currentMatchLength += matchIncrement;
                            if (currentSeed.currentMatchLength >= currentSeed.maxMatchLength) {
                                if (debug) System.out.println("----> match exceeds maximum match length (value=" + currentSeed.currentMatchLength + ", max=" + currentSeed.maxMatchLength + ")");
                                if (currentSeed.maxMatchLength > maxSeed.matchLength) {
                                    maxSeed.matchLength = currentSeed.maxMatchLength;
                                    maxSeed.toSubjectPosition = currentSeed.lastMatchPosition;
                                    maxSeed.complemented = reverseComplementDirection;
                                    if (reverseComplementDirection) {
                                        maxSeed.fromReadPosition = queryOffset;
                                    } else {
                                        maxSeed.fromReadPosition = queryOffset;
                                    }
                                    maxSeed.maxQueryMatchLength = currentSeed.maxQueryMatchLength;
                                    maxSeed.maxTargetMatchLength = currentSeed.maxTargetMatchLength;
                                }
                                if (debug) System.out.println("seed unlinked");
                                if (currentSeed.next == null || currentSeed.prev == null) {
                                    if (debug) System.out.println("current seed is the first, last or only seed in the list");
                                    if (currentSeed.next == null && currentSeed.prev == null) {
                                        firstSeed = lastSeed = null;
                                    } else if (currentSeed.prev == null) {
                                        firstSeed = currentSeed.next;
                                        firstSeed.prev = null;
                                    } else {
                                        lastSeed = currentSeed.prev;
                                        lastSeed.next = null;
                                    }
                                } else {
                                    if (debug) System.out.println("current seed is somewhere in betwee the list");
                                    currentSeed.prev.next = currentSeed.next;
                                    currentSeed.next.prev = currentSeed.prev;
                                }
                                if (firstBufferSeed == null) {
                                    firstBufferSeed = currentSeed;
                                    currentSeed = currentSeed.next;
                                    firstBufferSeed.next = null;
                                    firstBufferSeed.prev = null;
                                } else {
                                    firstBufferSeed.prev = currentSeed;
                                    currentSeed = currentSeed.next;
                                    firstBufferSeed.prev.next = firstBufferSeed.next;
                                    firstBufferSeed.prev = firstBufferSeed;
                                    firstBufferSeed.prev = null;
                                }
                            } else {
                                if (debug) System.out.println("order the increased seed at its new appropriate position");
                                linkHelperForward = currentSeed;
                                currentSeed = currentSeed.next;
                                while (linkHelperForward.next != null && linkHelperForward.lastMatchPosition > linkHelperForward.next.lastMatchPosition) {
                                    linkHelperBackward = linkHelperForward.next;
                                    linkHelperForward.next = linkHelperBackward.next;
                                    linkHelperBackward.next = linkHelperForward;
                                    if (linkHelperForward.prev == null) {
                                        linkHelperBackward.prev = null;
                                        firstSeed = linkHelperBackward;
                                    } else {
                                        linkHelperBackward.prev = linkHelperForward.prev;
                                        linkHelperBackward.prev.next = linkHelperBackward;
                                    }
                                    linkHelperForward.prev = linkHelperBackward;
                                    if (linkHelperForward.next == null) {
                                        lastSeed = linkHelperForward;
                                    } else {
                                        linkHelperForward.next.prev = linkHelperForward;
                                    }
                                    linkHelperForward.skipOnNextContact = true;
                                }
                            }
                            linkHelperBackward = null;
                            linkHelperForward = null;
                            occurenceIndex++;
                        } else if (currentMatchPosition < currentSeed.lastMatchPosition + matchIncrement) {
                            if (debug) System.out.print("the entry was not found in the seeds, add it at the current position: ");
                            if (debug) System.out.println(qGramIndex.distanceToNextSequenceEnd[currentMatchPosition] + " > " + qLength + " && (" + currentMatchPosition + "<" + excludeFrom + " || " + currentMatchPosition + ">=" + excludeTo + ")");
                            if (qGramIndex.distanceToNextSequenceEnd[currentMatchPosition] > qLength && (currentMatchPosition < excludeFrom || currentMatchPosition >= excludeTo)) {
                                if (debug) System.out.println("lohnt sich zu adden");
                                linkHelperBackward = currentSeed;
                                if (firstBufferSeed != null) {
                                    linkHelperBackward = firstBufferSeed;
                                    firstBufferSeed = firstBufferSeed.next;
                                    if (firstBufferSeed != null) {
                                        firstBufferSeed.prev = null;
                                    }
                                } else {
                                    linkHelperBackward = new Seed();
                                }
                                linkHelperBackward.skipOnNextContact = false;
                                linkHelperBackward.lastMatchPosition = currentMatchPosition;
                                linkHelperBackward.currentMatchLength = qLength;
                                if (reverseComplementDirection) {
                                    linkHelperBackward.maxQueryMatchLength = queryOffset + qLength;
                                    linkHelperBackward.maxTargetMatchLength = qGramIndex.distanceToNextSequenceEnd[currentMatchPosition];
                                    linkHelperBackward.maxMatchLength = Math.min(qGramIndex.distanceToNextSequenceEnd[currentMatchPosition], queryOffset + qLength);
                                } else {
                                    linkHelperBackward.maxQueryMatchLength = queryChars.length - (queryOffset + 1) + qLength;
                                    linkHelperBackward.maxTargetMatchLength = qGramIndex.distanceToNextSequenceEnd[currentMatchPosition];
                                    linkHelperBackward.maxMatchLength = Math.min(qGramIndex.distanceToNextSequenceEnd[currentMatchPosition], queryChars.length - (queryOffset + 1) + qLength);
                                }
                                if (currentSeed.prev == null) {
                                    if (debug) System.out.println("added first element");
                                    firstSeed = linkHelperBackward;
                                    currentSeed.prev = firstSeed;
                                    firstSeed.next = currentSeed;
                                    firstSeed.prev = null;
                                } else {
                                    if (debug) System.out.println("added intermediate element");
                                    linkHelperBackward.prev = currentSeed.prev;
                                    linkHelperBackward.next = currentSeed;
                                    linkHelperBackward.prev.next = linkHelperBackward;
                                    currentSeed.prev = linkHelperBackward;
                                }
                                linkHelperBackward = null;
                            } else {
                                if (debug) System.out.println("lohnt sich nicht zu adden");
                            }
                            occurenceIndex++;
                        } else {
                            if (debug) System.out.println("the seed had no increment, dismiss it");
                            if (currentSeed.currentMatchLength > maxSeed.matchLength) {
                                maxSeed.matchLength = currentSeed.currentMatchLength;
                                maxSeed.toSubjectPosition = currentSeed.lastMatchPosition;
                                maxSeed.complemented = reverseComplementDirection;
                                if (reverseComplementDirection) {
                                    maxSeed.fromReadPosition = queryOffset + 1;
                                } else {
                                    maxSeed.fromReadPosition = queryOffset - 1;
                                }
                                maxSeed.maxQueryMatchLength = currentSeed.maxQueryMatchLength;
                                maxSeed.maxTargetMatchLength = currentSeed.maxTargetMatchLength;
                            }
                            if (debug) System.out.println("seed unlinked");
                            if (currentSeed.next == null || currentSeed.prev == null) {
                                if (currentSeed.next == null && currentSeed.prev == null) {
                                    firstSeed = lastSeed = null;
                                } else if (currentSeed.prev == null) {
                                    firstSeed = currentSeed.next;
                                    firstSeed.prev = null;
                                } else {
                                    lastSeed = currentSeed.prev;
                                    lastSeed.next = null;
                                }
                            } else {
                                currentSeed.prev.next = currentSeed.next;
                                currentSeed.next.prev = currentSeed.prev;
                            }
                            if (firstBufferSeed == null) {
                                firstBufferSeed = currentSeed;
                                currentSeed = currentSeed.next;
                                firstBufferSeed.next = null;
                                firstBufferSeed.prev = null;
                            } else {
                                firstBufferSeed.prev = currentSeed;
                                currentSeed = currentSeed.next;
                                firstBufferSeed.prev.next = firstBufferSeed.next;
                                firstBufferSeed.prev = firstBufferSeed;
                                firstBufferSeed.prev = null;
                            }
                        }
                    } else if (currentSeed == null) {
                        currentMatchPosition = occurrenceTable[occurenceIndex];
                        if (qGramIndex.distanceToNextSequenceEnd[currentMatchPosition] > qLength && (currentMatchPosition < excludeFrom || currentMatchPosition >= excludeTo)) {
                            if (debug) System.out.println("es lohnt sich zu adden");
                            if (firstBufferSeed != null) {
                                linkHelperBackward = firstBufferSeed;
                                firstBufferSeed = firstBufferSeed.next;
                                if (firstBufferSeed != null) {
                                    firstBufferSeed.prev = null;
                                }
                            } else {
                                linkHelperBackward = new Seed();
                            }
                            linkHelperBackward.skipOnNextContact = false;
                            linkHelperBackward.lastMatchPosition = currentMatchPosition;
                            linkHelperBackward.currentMatchLength = qLength;
                            if (reverseComplementDirection) {
                                linkHelperBackward.maxQueryMatchLength = queryOffset + qLength;
                                linkHelperBackward.maxTargetMatchLength = qGramIndex.distanceToNextSequenceEnd[currentMatchPosition];
                                linkHelperBackward.maxMatchLength = Math.min(qGramIndex.distanceToNextSequenceEnd[currentMatchPosition], queryOffset + qLength);
                            } else {
                                linkHelperBackward.maxQueryMatchLength = queryChars.length - (queryOffset + 1) + qLength;
                                linkHelperBackward.maxTargetMatchLength = qGramIndex.distanceToNextSequenceEnd[currentMatchPosition];
                                linkHelperBackward.maxMatchLength = Math.min(qGramIndex.distanceToNextSequenceEnd[currentMatchPosition], queryChars.length - (queryOffset + 1) + qLength);
                            }
                            if (lastSeed == null) {
                                if (debug) System.out.println("added first element");
                                firstSeed = lastSeed = linkHelperBackward;
                                firstSeed.prev = null;
                                firstSeed.next = null;
                            } else {
                                if (debug) System.out.println("added intermediate element");
                                lastSeed.next = linkHelperBackward;
                                linkHelperBackward.prev = lastSeed;
                                linkHelperBackward.next = null;
                                lastSeed = lastSeed.next;
                            }
                            linkHelperBackward = null;
                        } else {
                            if (debug) System.out.println("lohnt sich nicht zu adden");
                        }
                        occurenceIndex++;
                    } else {
                        if (debug) System.out.println("if we processed all occurences, or there were no occurences at all");
                        if (currentSeed.currentMatchLength > maxSeed.matchLength) {
                            maxSeed.matchLength = currentSeed.currentMatchLength;
                            maxSeed.toSubjectPosition = currentSeed.lastMatchPosition;
                            maxSeed.complemented = reverseComplementDirection;
                            if (reverseComplementDirection) {
                                maxSeed.fromReadPosition = queryOffset + 1;
                            } else {
                                maxSeed.fromReadPosition = queryOffset - 1;
                            }
                            maxSeed.maxQueryMatchLength = currentSeed.maxQueryMatchLength;
                            maxSeed.maxTargetMatchLength = currentSeed.maxTargetMatchLength;
                        }
                        if (debug) System.out.println("seed unlinked");
                        if (currentSeed.next == null || currentSeed.prev == null) {
                            if (currentSeed.next == null && currentSeed.prev == null) {
                                if (debug) System.out.println("only element");
                                firstSeed = lastSeed = null;
                            } else if (currentSeed.prev == null) {
                                if (debug) System.out.println("first element");
                                firstSeed = currentSeed.next;
                                firstSeed.prev = null;
                            } else {
                                if (debug) System.out.println("last element");
                                lastSeed = currentSeed.prev;
                                lastSeed.next = null;
                            }
                        } else {
                            if (debug) System.out.println("somewhere in between");
                            currentSeed.prev.next = currentSeed.next;
                            currentSeed.next.prev = currentSeed.prev;
                        }
                        if (firstBufferSeed == null) {
                            firstBufferSeed = currentSeed;
                            currentSeed = currentSeed.next;
                            firstBufferSeed.next = null;
                            firstBufferSeed.prev = null;
                        } else {
                            firstBufferSeed.prev = currentSeed;
                            currentSeed = currentSeed.next;
                            firstBufferSeed.prev.next = firstBufferSeed.next;
                            firstBufferSeed.prev = firstBufferSeed;
                            firstBufferSeed.prev = null;
                        }
                    }
                }
                if (debug) System.out.println("#ENTRY done");
            } else {
                while (firstSeed != null) {
                    currentSeed = firstSeed;
                    if (currentSeed.currentMatchLength > maxSeed.matchLength) {
                        maxSeed.matchLength = currentSeed.currentMatchLength;
                        maxSeed.toSubjectPosition = currentSeed.lastMatchPosition;
                        maxSeed.complemented = reverseComplementDirection;
                        if (reverseComplementDirection) {
                            maxSeed.fromReadPosition = queryOffset + 1;
                        } else {
                            maxSeed.fromReadPosition = queryOffset - 1;
                        }
                        maxSeed.maxQueryMatchLength = currentSeed.maxQueryMatchLength;
                        maxSeed.maxTargetMatchLength = currentSeed.maxTargetMatchLength;
                    }
                    if (currentSeed.next == null || currentSeed.prev == null) {
                        if (currentSeed.next == null && currentSeed.prev == null) {
                            firstSeed = lastSeed = null;
                        } else if (currentSeed.prev == null) {
                            firstSeed = currentSeed.next;
                            firstSeed.prev = null;
                        } else {
                            lastSeed = currentSeed.prev;
                            lastSeed.next = null;
                        }
                    } else {
                        currentSeed.prev.next = currentSeed.next;
                        currentSeed.next.prev = currentSeed.prev;
                    }
                    if (firstBufferSeed == null) {
                        firstBufferSeed = currentSeed;
                        currentSeed = currentSeed.next;
                        firstBufferSeed.next = null;
                        firstBufferSeed.prev = null;
                    } else {
                        firstBufferSeed.prev = currentSeed;
                        currentSeed = currentSeed.next;
                        firstBufferSeed.prev.next = firstBufferSeed.next;
                        firstBufferSeed.prev = firstBufferSeed;
                        firstBufferSeed.prev = null;
                    }
                }
            }
            if (reverseComplementDirection) {
                if (queryOffset > endPos) {
                    queryOffset -= matchIncrement;
                } else {
                    nextBase = false;
                }
            } else {
                if (queryOffset < endPos) {
                    queryOffset += matchIncrement;
                } else {
                    nextBase = false;
                }
            }
        }
        firstBufferSeed = firstSeed = lastSeed = currentSeed = linkHelperBackward = linkHelperForward = null;
        if (maxSeed.matchLength > 0) {
            if (reverseComplementDirection) {
                maxSeed.toSubjectPosition += (qLength - 1);
            } else {
                maxSeed.toSubjectPosition -= (maxSeed.matchLength - qLength);
                maxSeed.fromReadPosition -= (maxSeed.matchLength - 1);
            }
        } else {
            maxSeed = null;
        }
        return maxSeed;
    }

    private int getOffsetFromIndex(int[] offsets, int index) {
        int lowerBound = 0;
        int upperBound = offsets.length;
        int newBound = 0;
        while (upperBound - lowerBound > 1) {
            newBound = lowerBound + (upperBound - lowerBound) / 2;
            if (offsets[newBound] > index) {
                upperBound = newBound;
            } else {
                lowerBound = newBound;
            }
        }
        return lowerBound;
    }

    private int getPositionInSequenceFromIndex(int[] offsets, int index) {
        int lowerBound = 0;
        int upperBound = offsets.length;
        int newBound = 0;
        while (upperBound - lowerBound > 1) {
            newBound = lowerBound + (upperBound - lowerBound) / 2;
            if (offsets[newBound] > index) {
                upperBound = newBound;
            } else {
                lowerBound = newBound;
            }
        }
        return index - offsets[lowerBound];
    }

    private void queryIndexOptimized(byte[] queryChars, boolean reverseComplementDirection, BitSet bitSet) {
        int[] hashTable = qGramIndex.getHashTable();
        int[] occurrenceTable = qGramIndex.getOccurrenceTable();
        int qLength = qGramIndex.getQLength();
        bitSet.clear();
        coder.reset();
        int endPos, queryOffset;
        if (reverseComplementDirection) {
            endPos = 0;
            queryOffset = queryChars.length - 1;
        } else {
            endPos = queryChars.length - 1;
            queryOffset = 0;
        }
        boolean nextBase = true;
        int code;
        while (nextBase) {
            code = coder.encode(queryChars, queryOffset, reverseComplementDirection);
            if (code != -1) {
                for (int occOffset = hashTable[code]; occOffset < hashTable[code + 1]; occOffset++) {
                    int i = occurrenceTable[occOffset];
                    bitSet.set(i, i + qLength);
                }
            }
            if (!reverseComplementDirection) {
                if (queryOffset + 2 * qLength < endPos) {
                    queryOffset += qLength;
                } else {
                    nextBase = false;
                }
            } else {
                if (queryOffset - 2 * qLength > endPos) {
                    queryOffset -= qLength;
                } else {
                    nextBase = false;
                }
            }
        }
    }

    public Object executeTask() {
        Contig contig = aceFile.getContigs()[contigNumber2Search];
        int[] contigOffsets = qGramIndex.getContigOffsets();
        int[] contigSeparatorOffsets = qGramIndex.getContigSeparatorOffsets();
        Read[] reads = contig.getReads();
        totalOverlappingQueryReads = contig.getLeftCoverage() + contig.getRightCoverage();
        byte state = QGramResult.LEFT_CONTIG;
        boolean run = true;
        byte[] unpaddedByteArray;
        int lowerIdentMarker, upperIdentMarker, overlappingPosition, startIndex;
        QGramResult result, uncomplementedResult, complementedResult;
        while (run) {
            if (state == QGramResult.LEFT_CONTIG) {
                lowerIdentMarker = contigOffsets[contigNumber2Search];
                if (qGramIndex.isConsenusUsed()) {
                    upperIdentMarker = contigSeparatorOffsets[contigNumber2Search] + contig.getUnpaddedBaseCount();
                } else {
                    upperIdentMarker = contigSeparatorOffsets[contigNumber2Search];
                }
                overlappingPosition = 1;
                startIndex = 0;
            } else {
                lowerIdentMarker = contigSeparatorOffsets[contigNumber2Search];
                upperIdentMarker = contigOffsets[contigNumber2Search + 1];
                overlappingPosition = contig.getPaddedBaseCount();
                startIndex = contig.getIndexOfFirstRightOverlappingRead();
            }
            for (int i = startIndex; i < reads.length; i++) {
                if (reads[i].getPaddedStartPosition() <= overlappingPosition && reads[i].getPaddedStartPosition() + reads[i].getPaddedBaseCount() > overlappingPosition) {
                    unpaddedByteArray = reads[i].getUnpaddedByteArray();
                    uncomplementedResult = queryIndexNew(unpaddedByteArray, false, lowerIdentMarker, upperIdentMarker);
                    complementedResult = queryIndexNew(unpaddedByteArray, true, lowerIdentMarker, upperIdentMarker);
                    if (uncomplementedResult != null && complementedResult != null) {
                        if (uncomplementedResult.matchLength > complementedResult.matchLength) {
                            result = uncomplementedResult;
                        } else {
                            result = complementedResult;
                        }
                    } else if (uncomplementedResult != null) {
                        result = uncomplementedResult;
                    } else if (complementedResult != null) {
                        result = complementedResult;
                    } else {
                        result = null;
                    }
                    if (result != null) {
                        result.fromContigIndex = contigNumber2Search;
                        result.fromContigAlignment = state;
                        result.fromReadIndex = i;
                        result.toContigIndex = getOffsetFromIndex(contigOffsets, result.toSubjectPosition);
                        Contig toContig = aceFile.getContigs()[result.toContigIndex];
                        if (result.toSubjectPosition < contigSeparatorOffsets[result.toContigIndex]) {
                            result.toContigAlignment = QGramResult.LEFT_CONTIG;
                            toContig.getReadIndexByPosition(result, result.toSubjectPosition - contigOffsets[result.toContigIndex]);
                        } else if (qGramIndex.isConsenusUsed() && result.toSubjectPosition < contigSeparatorOffsets[result.toContigIndex] + toContig.getUnpaddedBaseCount()) {
                            result.toContigAlignment = QGramResult.MIDDLE_CONTIG;
                            result.toReadIndex = -1;
                            result.toReadPosition = result.toSubjectPosition - contigSeparatorOffsets[result.toContigIndex];
                            System.out.println("found a middle pair?!");
                        } else {
                            result.toContigAlignment = QGramResult.RIGHT_CONTIG;
                            if (qGramIndex.isConsenusUsed()) {
                                toContig.getReadIndexByPosition(result, result.toSubjectPosition - (contigSeparatorOffsets[result.toContigIndex] + toContig.getUnpaddedBaseCount()));
                            } else {
                                toContig.getReadIndexByPosition(result, result.toSubjectPosition - contigSeparatorOffsets[result.toContigIndex]);
                            }
                        }
                        resultSet[contigNumber2Search].addResult(result);
                    } else {
                        System.out.println("No Hit for " + contig.getName() + ":" + reads[i].getName() + "!");
                    }
                    processedOverlappingQueryReads++;
                } else if (reads[i].getPaddedStartPosition() > overlappingPosition) {
                    break;
                }
            }
            if (state == QGramResult.LEFT_CONTIG) {
                state = QGramResult.RIGHT_CONTIG;
            } else {
                run = false;
            }
        }
        setProgress(100);
        return null;
    }

    public int getWorkingContigIndex() {
        return contigNumber2Search;
    }

    @Override
    public void updateProgress() {
        if (totalOverlappingQueryReads > 0) {
            setProgress((int) ((100.0 * processedOverlappingQueryReads) / totalOverlappingQueryReads));
        } else {
            setProgress(0);
        }
    }

    private class Seed {

        public int lastMatchPosition;

        public int maxMatchLength;

        public int currentMatchLength;

        public Seed next;

        public Seed prev;

        public boolean skipOnNextContact = false;

        public int maxQueryMatchLength;

        public int maxTargetMatchLength;

        public Seed() {
        }

        public Seed(int lastMatchPosition, int currentMatchLength, int maxMatchLength) {
            this.lastMatchPosition = lastMatchPosition;
            this.currentMatchLength = currentMatchLength;
            this.maxMatchLength = maxMatchLength;
        }
    }
}
