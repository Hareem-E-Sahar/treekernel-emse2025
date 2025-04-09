package net.sf.jagg;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.Future;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * <p>This class represents "group by" functionality that allows aggregate
 * functions, or <code>Aggregators</code>, to return aggregate values
 * over a <code>List</code> of <code>Objects</code>.</p>
 * <p>It can aggregate over <code>Comparable</code> objects or, with a supplied
 * list of properties, any objects.  Parallel execution is also avaialable.</p>
 *
 * @author Randy Gettman
 * @since 0.1.0
 */
public class Aggregations {

    private static ThreadPoolExecutor theThreadPool = null;

    private Aggregations() {
    }

    /**
    * Perform one or more aggregate operations on a <code>List&lt;T&gt;</code>.
    * <code>T</code> should have a "natural ordering", that is, it must be
    * <code>Comparable</code>, and <code>compareTo</code> defines the
    * properties with which to "group by" with its consideration of different
    * properties to determine order.
    *
    * @param <T> The object type to aggregate.
    * @param values The <code>List&lt;T&gt;</code> of objects to aggregate.
    * @param aggregators The <code>List</code> of <code>Aggregators</code> to
    *    apply to <code>values</code>.
    * @return A <code>List&lt;AggregateValue&lt;T&gt;&gt;</code>.
    * @see Aggregator
    */
    public static <T extends Comparable<? super T>> List<AggregateValue<T>> groupBy(List<T> values, List<Aggregator> aggregators) {
        return groupBy(values, aggregators, 1);
    }

    /**
    * Perform one or more aggregate operations on a <code>List&lt;T&gt;</code>.
    * <code>T</code> should have a "natural ordering", that is, it must be
    * <code>Comparable</code>, and <code>compareTo</code> defines the
    * properties with which to "group by" with its consideration of different
    * properties to determine order.  This version accepts an integer argument
    * corresponding to the parallelization desired.
    *
    * @param <T> The object type to aggregate.
    * @param values The <code>List&lt;T&gt;</code> of objects to aggregate.
    * @param aggregators The <code>List</code> of <code>Aggregators</code> to
    *    apply to <code>values</code>.
    * @param parallelism The degree of parallelism desired; if less than 1,
    *    then 1 will be used; if more than 1, then minimum of this number and
    *    the number of processors available to the JVM will be used, as
    *    determined by <code>Runtime.availableProcessors</code>.
    * @return A <code>List&lt;AggregateValue&lt;T&gt;&gt;</code>.
    * @see Aggregator
    * @see Runtime#availableProcessors
    */
    public static <T extends Comparable<? super T>> List<AggregateValue<T>> groupBy(List<T> values, List<Aggregator> aggregators, int parallelism) {
        ArrayList<T> listCopy = new ArrayList<T>(values);
        ComparableComparator<T> comparator = new ComparableComparator<T>();
        Collections.sort(listCopy, comparator);
        if (parallelism < 1) parallelism = 1;
        if (parallelism > 1) {
            int numProcessors = Runtime.getRuntime().availableProcessors();
            parallelism = (parallelism > numProcessors) ? numProcessors : parallelism;
        }
        return doAggregation(listCopy, comparator, aggregators, parallelism);
    }

    /**
    * Perform one or more aggregate operations on a <code>List&lt;T&gt;</code>.
    * <code>T</code> does not need to be <code>Comparable</code>.  The given
    * properties list defines the properties with which to "group by".
    *
    * @param <T> The object type to aggregate.
    * @param values The <code>List&lt;T&gt;</code> of objects to aggregate.
    * @param properties The <code>List&lt;String&gt;</code> of properties to
    *    "group by".
    * @param aggregators The <code>List</code> of <code>Aggregators</code> to
    *    apply to <code>values</code>.
    * @return A <code>List&lt;AggregateValue&lt;T&gt;&gt;</code>
    * @see Aggregator
    */
    public static <T> List<AggregateValue<T>> groupBy(List<T> values, List<String> properties, List<Aggregator> aggregators) {
        return groupBy(values, properties, aggregators, 1);
    }

    /**
    * Perform one or more aggregate operations on a <code>List&lt;T&gt;</code>.
    * <code>T</code> does not need to be <code>Comparable</code>.  The given
    * properties list defines the properties with which to "group by".  This
    * version accepts an integer argument corresponding to the parallelization
    * desired.
    *
    * @param <T> The object type to aggregate.
    * @param values The <code>List&lt;T&gt;</code> of objects to aggregate.
    * @param properties The <code>List&lt;String&gt;</code> of properties to
    *    "group by".
    * @param aggregators The <code>List</code> of <code>Aggregators</code> to
    *    apply to <code>values</code>.
    * @param parallelism The degree of parallelism desired; if less than 1,
    *    then 1 will be used; if more than 1, then minimum of this number and
    *    the number of processors available to the JVM will be used, as
    *    determined by <code>Runtime.availableProcessors</code>.
    * @return A <code>List&lt;AggregateValue&lt;T&gt;&gt;</code>
    * @see Aggregator
    * @see Runtime#availableProcessors
    */
    public static <T> List<AggregateValue<T>> groupBy(List<T> values, List<String> properties, List<Aggregator> aggregators, int parallelism) {
        ArrayList<T> listCopy = new ArrayList<T>(values);
        PropertiesComparator<T> comparator = new PropertiesComparator<T>(properties);
        Collections.sort(listCopy, comparator);
        if (parallelism < 1) parallelism = 1;
        if (parallelism > 1) {
            int numProcessors = Runtime.getRuntime().availableProcessors();
            parallelism = (parallelism > numProcessors) ? numProcessors : parallelism;
        }
        return doAggregation(listCopy, comparator, aggregators, parallelism);
    }

    /**
    * Perform the actual aggregation.  This restricts the parallelism based on
    * the size of the list of values to aggregate, e.g. don't want to have a
    * parallelism of 8 when the list size is 6.  Then it delegates to either
    * the single-threaded or multi-threaded version of
    * <code>getAggregateValues</code>.
    * @param listCopy The sorted copy of the list of values to aggregate.
    * @param comparator A <code>Comparator</code> over T objects.
    * @param aggregators A <code>List</code> of <code>Aggregators</code>.
    * @param parallelism The degree of parallelism, which is assumed to be
    *    limited already by the number of processors on the system.
    * @return A <code>List</code> of <code>AggregateValues</code>.
    */
    private static <T> List<AggregateValue<T>> doAggregation(ArrayList<T> listCopy, Comparator<? super T> comparator, List<Aggregator> aggregators, int parallelism) {
        List<AggregateValue<T>> aggregatedList;
        int size = listCopy.size();
        int minParallelism = (parallelism > size) ? size : parallelism;
        if (minParallelism > 1) aggregatedList = getAggregateValues(listCopy, comparator, aggregators, parallelism); else aggregatedList = getAggregateValues(listCopy, comparator, aggregators);
        return aggregatedList;
    }

    /**
    * Create an <code>ExecutorCompletionService</code>.
    * @return An <code>ExecutorCompletionService</code>.
    */
    private static <T> ExecutorCompletionService<PositionedAggregatorList<T>> initializeService() {
        if (theThreadPool == null) {
            int numProcessors = Runtime.getRuntime().availableProcessors();
            theThreadPool = new ThreadPoolExecutor(0, numProcessors, 0, TimeUnit.MILLISECONDS, new SynchronousQueue<Runnable>());
        }
        return new ExecutorCompletionService<PositionedAggregatorList<T>>(theThreadPool);
    }

    /**
    * Get all aggregate values for all aggregators.  This is the multi-threaded
    * version.
    * @param list The sorted list copy of values to aggregate.
    * @param comparator A <code>Comparator</code> over T objects.
    * @param aggregators A <code>List</code> of <code>Aggregators</code>.
    * @param parallelism The degree of parallelism.
    * @return A <code>List</code> of <code>AggregateValues</code>.
    */
    private static <T> List<AggregateValue<T>> getAggregateValues(ArrayList<T> list, Comparator<? super T> comparator, List<Aggregator> aggregators, int parallelism) {
        List<PositionedAggregatorList<T>> listOfPals = new ArrayList<PositionedAggregatorList<T>>(parallelism);
        for (int p = 0; p < parallelism; p++) listOfPals.add(null);
        ExecutorCompletionService<PositionedAggregatorList<T>> service = initializeService();
        int size = list.size();
        for (int p = 0; p < parallelism; p++) {
            int startIndex = (size * p) / parallelism;
            int endIndex = (size * (p + 1)) / parallelism - 1;
            service.submit(new AggregateRunner<T>(aggregators, list, p, comparator, startIndex, endIndex));
        }
        int numPALs = 0;
        while (numPALs < parallelism) {
            try {
                Future<PositionedAggregatorList<T>> future = service.poll(1, TimeUnit.SECONDS);
                if (future != null) {
                    PositionedAggregatorList<T> pal = future.get();
                    listOfPals.set(pal.getPosition(), pal);
                    numPALs++;
                }
            } catch (InterruptedException ignored) {
            } catch (ExecutionException e) {
                throw new UnsupportedOperationException(e.getClass().getName() + " caught while aggregating.", e);
            }
        }
        return mergeLists(listOfPals, comparator);
    }

    /**
    * Get all aggregate values for all aggregators.  This is the single-
    * threaded version.
    * @param list The sorted list copy of values to aggregate.
    * @param comparator A <code>Comparator</code> over T objects.
    * @param aggregators A <code>List</code> of <code>Aggregators</code>.
    * @return A <code>List</code> of <code>AggregateValues</code>.
    */
    private static <T> List<AggregateValue<T>> getAggregateValues(ArrayList<T> list, Comparator<? super T> comparator, List<Aggregator> aggregators) {
        List<AggregateValue<T>> aggValues = new ArrayList<AggregateValue<T>>();
        List<Aggregator> aggList = new ArrayList<Aggregator>(aggregators.size());
        for (Aggregator archetype : aggregators) aggList.add(Aggregator.getAggregator(archetype));
        int startIndex = 0;
        int endIndex;
        int listsize = list.size();
        while (startIndex < listsize) {
            AggregateValue<T> aggValue = new AggregateValue<T>(list.get(startIndex));
            endIndex = indexOfLastMatching(list, comparator, startIndex);
            for (Aggregator agg : aggList) agg.init();
            for (int i = startIndex; i <= endIndex; i++) {
                T value = list.get(i);
                for (Aggregator agg : aggList) agg.iterate(value);
            }
            for (Aggregator agg : aggList) aggValue.setAggregateValue(agg, agg.terminate());
            aggValues.add(aggValue);
            startIndex = endIndex + 1;
        }
        return aggValues;
    }

    /**
    * <p>Merge <code>Lists</code> of <code>PositionedAggregatorLists</code>, by
    * taking the following structure and merging and terminating any unfinished
    * <code>Aggregators</code>:
    * </p>
    * <p>
    * <code>
    * listOfPals[0] {initObject: T, initAggregators: List&lt;Aggregator&gt;,
    *    midAggValues: List&lt;AggregateValue&lt;T&gt;&gt;,
    *    endingObject: T, endingAggregators; List&lt;Aggregator&gt;}
    * listOfPals[1] {initObject: T, initAggregators: List&lt;Aggregator&gt;,
    *    midAggValues: List&lt;AggregateValue&lt;T&gt;&gt;,
    *    endingObject: T, endingAggregators; List&lt;Aggregator&gt;}
    * ...
    * listOfPals[n - 1] {initObject: T, initAggregators: List&lt;Aggregator&gt;,
    *    midAggValues: List&lt;AggregateValue&lt;T&gt;&gt;,
    *    endingObject: T, endingAggregators; List&lt;Aggregator&gt;}
    * </code>
    * </p>
    * <p>Above, this will terminate all <code>Aggregators</code> in the initial
    * list of the first <code>PositionedAggregatorLists</code> and create the
    * first <code>AggregateValue</code>.  Then it will include all middle
    * <code>AggregateValues</code>, which have already been calculated. On the
    * borders between <code>PositionedAggregatorLists</code>, it will determine
    * if the ending object of one is equal to the initial object of the next.
    * If so, it will merge the <code>Aggregators</code> before creating one
    * <code>AggregateValue</code>, else it will create separate
    * <code>AggregateValues</code>.  Finally, it will terminate all
    * <code>Aggregators</code> in the ending list of the last
    * <code>PositionedAggregatorLists</code> and create the last
    * <code>AggregateValue</code>.</p>
    * @param listOfPals A <code>List</code> of
    *    <code>PositionedAggregatorLists</code>.
    * @param comparator A <code>Comparator</code> of T values.
    * @return A merged <code>List</code> (of one item) of <code>Lists</code> of
    *    <code>Aggregators</code>.
    */
    private static <T> List<AggregateValue<T>> mergeLists(List<PositionedAggregatorList<T>> listOfPals, Comparator<? super T> comparator) {
        List<AggregateValue<T>> aggValues = new ArrayList<AggregateValue<T>>();
        PositionedAggregatorList<T> prev = listOfPals.get(0);
        List<Aggregator> aggs = prev.getInitialAggList();
        T initObject = prev.getInitialObject();
        AggregateValue<T> firstValue = new AggregateValue<T>(initObject);
        for (Aggregator agg : aggs) {
            firstValue.setAggregateValue(agg, agg.terminate());
            agg.setInUse(false);
        }
        aggValues.add(firstValue);
        for (int i = 0; i < listOfPals.size() - 1; i++) {
            PositionedAggregatorList<T> curr = listOfPals.get(i + 1);
            aggValues.addAll(prev.getMiddleAggValues());
            T prevObject = prev.getEndingObject();
            T currObject = curr.getInitialObject();
            List<Aggregator> prevAggsList = prev.getEndingAggList();
            List<Aggregator> currAggsList = curr.getInitialAggList();
            if (comparator.compare(prevObject, currObject) == 0) {
                AggregateValue<T> prevValue = new AggregateValue<T>(prevObject);
                for (int a = 0; a < prevAggsList.size(); a++) {
                    Aggregator prevAgg = prevAggsList.get(a);
                    Aggregator currAgg = currAggsList.get(a);
                    prevAgg.merge(currAgg);
                    currAgg.setInUse(false);
                    prevValue.setAggregateValue(prevAgg, prevAgg.terminate());
                    prevAgg.setInUse(false);
                }
                aggValues.add(prevValue);
            } else {
                AggregateValue<T> prevValue = new AggregateValue<T>(prevObject);
                AggregateValue<T> currValue = new AggregateValue<T>(currObject);
                for (int a = 0; a < prevAggsList.size(); a++) {
                    Aggregator prevAgg = prevAggsList.get(a);
                    prevValue.setAggregateValue(prevAgg, prevAgg.terminate());
                    prevAgg.setInUse(false);
                    Aggregator currAgg = currAggsList.get(a);
                    currValue.setAggregateValue(currAgg, currAgg.terminate());
                    currAgg.setInUse(false);
                }
                aggValues.add(prevValue);
                aggValues.add(currValue);
            }
            prev = curr;
        }
        PositionedAggregatorList<T> last = listOfPals.get(listOfPals.size() - 1);
        aggValues.addAll(last.getMiddleAggValues());
        T endingObject = last.getEndingObject();
        aggs = last.getEndingAggList();
        AggregateValue<T> lastValue = new AggregateValue<T>(endingObject);
        for (Aggregator agg : aggs) {
            lastValue.setAggregateValue(agg, agg.terminate());
            agg.setInUse(false);
        }
        aggValues.add(lastValue);
        return aggValues;
    }

    /**
    * In the already sorted list, return the highest index whose item in the
    * list compares equal to the item at the given start index.
    *
    * @param <T> The type of objects in the <code>List</code> of values.
    * @param list The <code>List</code> of values.
    * @param comparator Decides how to compare values for equality.
    * @param startIdx Start looking for the last match at this index.
    * @return The index that represents the last object in the given
    *    <code>List</code> that compares equal to the object represented by
    *    the start index.
    */
    private static <T> int indexOfLastMatching(List<T> list, Comparator<? super T> comparator, int startIdx) {
        return indexOfLastMatching(list, comparator, startIdx, list.size() - 1);
    }

    /**
    * In the already sorted list, return the highest index whose item in the
    * list compares equal to the item at the given start index, except that no
    * value larger than the maximum index will be returned.
    * @param <T> The type of objects in the <code>List</code> of values.
    * @param list The <code>List</code> of values.
    * @param comparator Decides how to compare values for equality.
    * @param startIdx Start looking for the last match at this index.
    * @param maxIdx Don't look past this index.
    * @return The lesser of the index that represents the last object in the
    *    given <code>List</code> that compares equal to the object represented
    *    by the start index, and <code>maxIdx</code>.
    */
    private static <T> int indexOfLastMatching(List<T> list, Comparator<? super T> comparator, int startIdx, int maxIdx) {
        T value = list.get(startIdx);
        int addMatchIdx = 1;
        int lowerBoundMatchIdx = startIdx;
        int upperBoundMatchIdx = startIdx + addMatchIdx;
        while (true) {
            if (upperBoundMatchIdx >= maxIdx) {
                upperBoundMatchIdx = maxIdx;
                break;
            }
            if (comparator.compare(value, list.get(upperBoundMatchIdx)) == 0) {
                lowerBoundMatchIdx = upperBoundMatchIdx;
                addMatchIdx *= 2;
                upperBoundMatchIdx += addMatchIdx;
            } else {
                break;
            }
        }
        while (true) {
            int midMatchIdx = (lowerBoundMatchIdx + upperBoundMatchIdx) / 2;
            if (lowerBoundMatchIdx == upperBoundMatchIdx) break;
            boolean downToTwo = (lowerBoundMatchIdx == upperBoundMatchIdx - 1);
            if (comparator.compare(value, list.get(midMatchIdx)) == 0) {
                if (downToTwo) {
                    if (comparator.compare(value, list.get(upperBoundMatchIdx)) == 0) {
                        lowerBoundMatchIdx = upperBoundMatchIdx;
                    }
                    break;
                }
                lowerBoundMatchIdx = midMatchIdx;
            } else {
                upperBoundMatchIdx = midMatchIdx - 1;
            }
        }
        return lowerBoundMatchIdx;
    }

    /**
    * Gets aggregate values for a sub-range of a <code>List</code> of Objects.
    * Running Time: <em>O(n * a)</em>, where <em>n</em> is the number of
    * items to process (<code>end - start + 1</code>), and <em>a</em> is the
    * number of aggregators desired.  That assumes that property access in
    * <code>T</code> is constant-time.
    */
    static class AggregateRunner<T> implements Callable<PositionedAggregatorList<T>> {

        private List<Aggregator> myAggregators;

        private List<T> myValuesList;

        private Comparator<? super T> myComparator;

        private int myStart;

        private int myEnd;

        private int myPosition;

        /**
       * Construct an <code>AggregateRunner</code> that in a separate
       * <code>Thread</code> will create a
       * <code>PositionedAggregatorList</code>.
       *
       * @param aggregators The <code>List</code> of <code>Aggregators</code>.
       * @param valuesList The list of values to aggregate.
       * @param pos The position in the order, as a 0-based index.
       * @param comparator A <code>Comparator</code> used to identify runs of
       *    equivalent T objects.
       * @param start The start index.
       * @param end The end index.
       */
        public AggregateRunner(List<Aggregator> aggregators, List<T> valuesList, int pos, Comparator<? super T> comparator, int start, int end) {
            myAggregators = aggregators;
            myValuesList = valuesList;
            myComparator = comparator;
            myStart = start;
            myEnd = end;
            myPosition = pos;
        }

        /**
       * Runs through a section of the values list from start to end, getting
       * additional <code>Aggregators</code> that are necessary, initializing
       * them, and iterating through all values from start to end.
       *
       * @return A <code>PositionedAggregatorList</code>.
       */
        public PositionedAggregatorList<T> call() {
            PositionedAggregatorList<T> pal = new PositionedAggregatorList<T>(myPosition);
            int startIndex = myStart;
            int endIndex = indexOfLastMatching(myValuesList, myComparator, startIndex, myEnd);
            T currObject = myValuesList.get(startIndex);
            List<Aggregator> initAggList = new ArrayList<Aggregator>(myAggregators.size());
            for (Aggregator archetype : myAggregators) initAggList.add(Aggregator.getAggregator(archetype));
            for (Aggregator agg : initAggList) agg.init();
            for (int i = startIndex; i <= endIndex; i++) {
                T value = myValuesList.get(i);
                for (Aggregator agg : initAggList) agg.iterate(value);
            }
            pal.setInitialList(currObject, initAggList);
            if (endIndex == myEnd) return pal;
            startIndex = endIndex + 1;
            List<AggregateValue<T>> aggValues = new ArrayList<AggregateValue<T>>();
            List<Aggregator> currAggList = new ArrayList<Aggregator>(myAggregators.size());
            for (Aggregator archetype : myAggregators) currAggList.add(Aggregator.getAggregator(archetype));
            while (startIndex <= myEnd) {
                currObject = myValuesList.get(startIndex);
                endIndex = indexOfLastMatching(myValuesList, myComparator, startIndex, myEnd);
                for (Aggregator agg : currAggList) agg.init();
                for (int i = startIndex; i <= endIndex; i++) {
                    T value = myValuesList.get(i);
                    for (Aggregator agg : currAggList) agg.iterate(value);
                }
                if (endIndex == myEnd) {
                    pal.setEndingList(currObject, currAggList);
                } else {
                    AggregateValue<T> aggValue = new AggregateValue<T>(currObject);
                    for (Aggregator agg : currAggList) {
                        Object result = agg.terminate();
                        aggValue.setAggregateValue(agg, result);
                    }
                    aggValues.add(aggValue);
                }
                startIndex = endIndex + 1;
            }
            pal.setMiddleAggValues(aggValues);
            return pal;
        }
    }

    /**
    * <p>This class is necessary for parallel processing.  It is possible for
    * some <code>Aggregators</code> to return different results based on the
    * order in which threads finish, e.g. <code>ConcatAggregator</code>.  When
    * a thread finishes and creates its <code>Future</code>, its position is
    * also stored here so that order can be preserved by the consumer.</p>
    * <p>This stores an unfinished list of Aggregators at the front, a finished
    * list of AggregateValues in the middle, and an unfinished list of
    * Aggregators at the end.</p>
    */
    static class PositionedAggregatorList<T> {

        private int myPos;

        private T myInitialObject;

        private List<Aggregator> myInitialAggList;

        private List<AggregateValue<T>> myMiddleAggValues;

        private T myEndingObject;

        private List<Aggregator> myEndingAggList;

        /**
       * Create a <code>PositionedAggregatorList</code> that represents the
       * work done by a Thread in parallel mode.  It consist of a (possibly)
       * unfinished initial <code>List</code> of <code>Aggregators</code>, a
       * finished middle <code>List</code> of <code>AggregateValues</code>, and
       * a (possibly) unfinished ending <code>List</code> of
       * <code>Aggregators</code>.
       * @param pos The 0-based position for ordering purposes.
       */
        public PositionedAggregatorList(int pos) {
            myInitialObject = null;
            myInitialAggList = null;
            myMiddleAggValues = new ArrayList<AggregateValue<T>>();
            myEndingObject = null;
            myEndingAggList = null;
            myPos = pos;
        }

        /**
       * Sets the results from the initial run (of the thread).
       * @param initObject The initial object.
       * @param initAggregators The initial <code>List</code> of
       *    <code>Aggregators</code>.
       */
        public void setInitialList(T initObject, List<Aggregator> initAggregators) {
            myInitialObject = initObject;
            myInitialAggList = initAggregators;
        }

        /**
       * Sets the finished results from the middle runs (of the thread).
       * @param aggValues The finished <code>List</code> of
       *    <code>AggregateValues</code>.
       */
        public void setMiddleAggValues(List<AggregateValue<T>> aggValues) {
            myMiddleAggValues.addAll(aggValues);
        }

        /**
       * Sets the results from the ending run (of the thread).
       * @param endingObject The ending object.
       * @param endingAggregators The ending <code>List</code> of
       *    <code>Aggregators</code>.
       */
        public void setEndingList(T endingObject, List<Aggregator> endingAggregators) {
            myEndingObject = endingObject;
            myEndingAggList = endingAggregators;
        }

        /**
       * Returns the position, as a 0-based index.
       * @return The position, as a 0-based index.
       */
        public int getPosition() {
            return myPos;
        }

        /**
       * Returns the object from the initial run.
       * @return The object from the initial run.
       */
        public T getInitialObject() {
            return myInitialObject;
        }

        /**
       * Returns the initial <code>List</code> of <code>Aggregators</code>.
       * @return The initial <code>List</code> of <code>Aggregators</code>.
       */
        public List<Aggregator> getInitialAggList() {
            return myInitialAggList;
        }

        /**
       * Returns the middle <code>List</code>of <code>AggregateValues</code>.
       * @return The middle <code>List</code>of <code>AggregateValues</code>.
       */
        public List<AggregateValue<T>> getMiddleAggValues() {
            return myMiddleAggValues;
        }

        /**
       * Returns the object from the ending run.
       * @return The object from the ending run.
       */
        public T getEndingObject() {
            return myEndingObject;
        }

        /**
       * Returns the ending <code>List</code> of <code>Aggregators</code>.
       * @return The ending <code>List</code> of <code>Aggregators</code>.
       */
        public List<Aggregator> getEndingAggList() {
            return myEndingAggList;
        }

        /**
       * Returns the string representation.
       * @return The string representation.
       */
        public String toString() {
            StringBuffer buf = new StringBuffer();
            buf.append("PAL: pos=");
            buf.append(myPos);
            buf.append("\n  Initial object=");
            buf.append((myInitialObject == null) ? "(null)" : myInitialObject.toString());
            for (int i = 0; i < myMiddleAggValues.size(); i++) {
                buf.append("\n  MiddleAggValue object(");
                buf.append(i);
                buf.append(")=");
                buf.append(myMiddleAggValues.get(i).getObject().toString());
            }
            buf.append("\n  Ending object=");
            buf.append((myEndingObject == null) ? "(null)" : myEndingObject.toString());
            return buf.toString();
        }
    }
}
