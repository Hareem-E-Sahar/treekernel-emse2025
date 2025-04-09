package serene.validation.handlers.stack.impl;

public class SynchronizedStackHandlerPool extends StackHandlerPool {

    private static volatile SynchronizedStackHandlerPool instance;

    int amshPoolFree;

    int amshPoolMaxSize;

    ActiveModelStackHandlerPool[] amshPools;

    int contextStackHAverageUse;

    int contextStackHMaxSize;

    int contextStackHFree;

    ContextStackHandler[] contextStackH;

    int minimalReduceStackHAverageUse;

    int minimalReduceStackHMaxSize;

    int minimalReduceStackHFree;

    MinimalReduceStackHandler[] minimalReduceStackH;

    int maximalReduceStackHAverageUse;

    int maximalReduceStackHMaxSize;

    int maximalReduceStackHFree;

    MaximalReduceStackHandler[] maximalReduceStackH;

    int candidateStackHAverageUse;

    int candidateStackHMaxSize;

    int candidateStackHFree;

    CandidateStackHandlerImpl[] candidateStackH;

    int concurrentStackHAverageUse;

    int concurrentStackHMaxSize;

    int concurrentStackHFree;

    ConcurrentStackHandlerImpl[] concurrentStackH;

    SynchronizedStackHandlerPool() {
        super();
        amshPoolFree = 0;
        amshPoolMaxSize = 10;
        amshPools = new ActiveModelStackHandlerPool[10];
        contextStackHAverageUse = 0;
        contextStackHFree = 0;
        contextStackH = new ContextStackHandler[10];
        minimalReduceStackHAverageUse = 0;
        minimalReduceStackHFree = 0;
        minimalReduceStackH = new MinimalReduceStackHandler[10];
        maximalReduceStackHAverageUse = 0;
        maximalReduceStackHFree = 0;
        maximalReduceStackH = new MaximalReduceStackHandler[10];
        candidateStackHAverageUse = 0;
        candidateStackHFree = 0;
        candidateStackH = new CandidateStackHandlerImpl[10];
        concurrentStackHAverageUse = 0;
        concurrentStackHFree = 0;
        concurrentStackH = new ConcurrentStackHandlerImpl[10];
        contextStackHMaxSize = 20;
        minimalReduceStackHMaxSize = 20;
        maximalReduceStackHMaxSize = 20;
        candidateStackHMaxSize = 40;
        concurrentStackHMaxSize = 20;
    }

    public static SynchronizedStackHandlerPool getInstance() {
        if (instance == null) {
            synchronized (SynchronizedStackHandlerPool.class) {
                if (instance == null) {
                    instance = new SynchronizedStackHandlerPool();
                }
            }
        }
        return instance;
    }

    public synchronized ActiveModelStackHandlerPool getActiveModelStackHandlerPool() {
        if (amshPoolFree == 0) {
            ActiveModelStackHandlerPool amshp = new ActiveModelStackHandlerPool(this);
            return amshp;
        } else {
            ActiveModelStackHandlerPool amshp = amshPools[--amshPoolFree];
            return amshp;
        }
    }

    public synchronized void recycle(ActiveModelStackHandlerPool amshp) {
        if (amshPools.length == amshPoolMaxSize) return;
        if (amshPoolFree == amshPools.length) {
            ActiveModelStackHandlerPool[] increased = new ActiveModelStackHandlerPool[10 + amshPools.length];
            System.arraycopy(amshPools, 0, increased, 0, amshPoolFree);
            amshPools = increased;
        }
        amshPools[amshPoolFree++] = amshp;
    }

    synchronized void fill(ActiveModelStackHandlerPool stackHandlerPool, ContextStackHandler[] contextStackHToFill, MinimalReduceStackHandler[] minimalReduceStackHToFill, MaximalReduceStackHandler[] maximalReduceStackHToFill, CandidateStackHandlerImpl[] candidateStackHToFill, ConcurrentStackHandlerImpl[] concurrentStackHToFill) {
        int contextStackHFillCount;
        if (contextStackHToFill == null || contextStackHToFill.length < contextStackHAverageUse) {
            contextStackHToFill = new ContextStackHandler[contextStackHAverageUse];
            stackHandlerPool.contextStackH = contextStackHToFill;
        }
        if (contextStackHFree > contextStackHAverageUse) {
            contextStackHFillCount = contextStackHAverageUse;
            contextStackHFree = contextStackHFree - contextStackHAverageUse;
        } else {
            contextStackHFillCount = contextStackHFree;
            contextStackHFree = 0;
        }
        System.arraycopy(contextStackH, contextStackHFree, contextStackHToFill, 0, contextStackHFillCount);
        int minimalReduceStackHFillCount;
        if (minimalReduceStackHToFill == null || minimalReduceStackHToFill.length < minimalReduceStackHAverageUse) {
            minimalReduceStackHToFill = new MinimalReduceStackHandler[minimalReduceStackHAverageUse];
            stackHandlerPool.minimalReduceStackH = minimalReduceStackHToFill;
        }
        if (minimalReduceStackHFree > minimalReduceStackHAverageUse) {
            minimalReduceStackHFillCount = minimalReduceStackHAverageUse;
            minimalReduceStackHFree = minimalReduceStackHFree - minimalReduceStackHAverageUse;
        } else {
            minimalReduceStackHFillCount = minimalReduceStackHFree;
            minimalReduceStackHFree = 0;
        }
        System.arraycopy(minimalReduceStackH, minimalReduceStackHFree, minimalReduceStackHToFill, 0, minimalReduceStackHFillCount);
        int maximalReduceStackHFillCount;
        if (maximalReduceStackHToFill == null || maximalReduceStackHToFill.length < maximalReduceStackHAverageUse) {
            maximalReduceStackHToFill = new MaximalReduceStackHandler[maximalReduceStackHAverageUse];
            stackHandlerPool.maximalReduceStackH = maximalReduceStackHToFill;
        }
        if (maximalReduceStackHFree > maximalReduceStackHAverageUse) {
            maximalReduceStackHFillCount = maximalReduceStackHAverageUse;
            maximalReduceStackHFree = maximalReduceStackHFree - maximalReduceStackHAverageUse;
        } else {
            maximalReduceStackHFillCount = maximalReduceStackHFree;
            maximalReduceStackHFree = 0;
        }
        System.arraycopy(maximalReduceStackH, maximalReduceStackHFree, maximalReduceStackHToFill, 0, maximalReduceStackHFillCount);
        int candidateStackHFillCount;
        if (candidateStackHToFill == null || candidateStackHToFill.length < candidateStackHAverageUse) {
            candidateStackHToFill = new CandidateStackHandlerImpl[candidateStackHAverageUse];
            stackHandlerPool.candidateStackH = candidateStackHToFill;
        }
        if (candidateStackHFree > candidateStackHAverageUse) {
            candidateStackHFillCount = candidateStackHAverageUse;
            candidateStackHFree = candidateStackHFree - candidateStackHAverageUse;
        } else {
            candidateStackHFillCount = candidateStackHFree;
            candidateStackHFree = 0;
        }
        System.arraycopy(candidateStackH, candidateStackHFree, candidateStackHToFill, 0, candidateStackHFillCount);
        int concurrentStackHFillCount;
        if (concurrentStackHToFill == null || concurrentStackHToFill.length < concurrentStackHAverageUse) {
            concurrentStackHToFill = new ConcurrentStackHandlerImpl[concurrentStackHAverageUse];
            stackHandlerPool.concurrentStackH = concurrentStackHToFill;
        }
        if (concurrentStackHFree > concurrentStackHAverageUse) {
            concurrentStackHFillCount = concurrentStackHAverageUse;
            concurrentStackHFree = concurrentStackHFree - concurrentStackHAverageUse;
        } else {
            concurrentStackHFillCount = concurrentStackHFree;
            concurrentStackHFree = 0;
        }
        System.arraycopy(concurrentStackH, concurrentStackHFree, concurrentStackHToFill, 0, concurrentStackHFillCount);
        stackHandlerPool.initFilled(contextStackHFillCount, minimalReduceStackHFillCount, maximalReduceStackHFillCount, candidateStackHFillCount, concurrentStackHFillCount);
    }

    synchronized void recycle(int contextStackHRecycledCount, int contextStackHEffectivellyUsed, ContextStackHandler[] contextStackHRecycled, int minimalReduceStackHRecycledCount, int minimalReduceStackHEffectivellyUsed, MinimalReduceStackHandler[] minimalReduceStackHRecycled, int maximalReduceStackHRecycledCount, int maximalReduceStackHEffectivellyUsed, MaximalReduceStackHandler[] maximalReduceStackHRecycled, int candidateStackHRecycledCount, int candidateStackHEffectivellyUsed, CandidateStackHandlerImpl[] candidateStackHRecycled, int concurrentStackHRecycledCount, int concurrentStackHEffectivellyUsed, ConcurrentStackHandlerImpl[] concurrentStackHRecycled) {
        int neededLength = contextStackHFree + contextStackHRecycledCount;
        if (neededLength > contextStackH.length) {
            if (neededLength > contextStackHMaxSize) {
                neededLength = contextStackHMaxSize;
                ContextStackHandler[] increased = new ContextStackHandler[neededLength];
                System.arraycopy(contextStackH, 0, increased, 0, contextStackH.length);
                contextStackH = increased;
                System.arraycopy(contextStackHRecycled, 0, contextStackH, contextStackHFree, contextStackHMaxSize - contextStackHFree);
                contextStackHFree = contextStackHMaxSize;
            } else {
                ContextStackHandler[] increased = new ContextStackHandler[neededLength];
                System.arraycopy(contextStackH, 0, increased, 0, contextStackH.length);
                contextStackH = increased;
                System.arraycopy(contextStackHRecycled, 0, contextStackH, contextStackHFree, contextStackHRecycledCount);
                contextStackHFree += contextStackHRecycledCount;
            }
        } else {
            System.arraycopy(contextStackHRecycled, 0, contextStackH, contextStackHFree, contextStackHRecycledCount);
            contextStackHFree += contextStackHRecycledCount;
        }
        if (contextStackHAverageUse != 0) contextStackHAverageUse = (contextStackHAverageUse + contextStackHEffectivellyUsed) / 2; else contextStackHAverageUse = contextStackHEffectivellyUsed;
        for (int i = 0; i < contextStackHRecycled.length; i++) {
            contextStackHRecycled[i] = null;
        }
        neededLength = minimalReduceStackHFree + minimalReduceStackHRecycledCount;
        if (neededLength > minimalReduceStackH.length) {
            if (neededLength > minimalReduceStackHMaxSize) {
                neededLength = minimalReduceStackHMaxSize;
                MinimalReduceStackHandler[] increased = new MinimalReduceStackHandler[neededLength];
                System.arraycopy(minimalReduceStackH, 0, increased, 0, minimalReduceStackH.length);
                minimalReduceStackH = increased;
                System.arraycopy(minimalReduceStackHRecycled, 0, minimalReduceStackH, minimalReduceStackHFree, minimalReduceStackHMaxSize - minimalReduceStackHFree);
                minimalReduceStackHFree = minimalReduceStackHMaxSize;
            } else {
                MinimalReduceStackHandler[] increased = new MinimalReduceStackHandler[neededLength];
                System.arraycopy(minimalReduceStackH, 0, increased, 0, minimalReduceStackH.length);
                minimalReduceStackH = increased;
                System.arraycopy(minimalReduceStackHRecycled, 0, minimalReduceStackH, minimalReduceStackHFree, minimalReduceStackHRecycledCount);
                minimalReduceStackHFree += minimalReduceStackHRecycledCount;
            }
        } else {
            System.arraycopy(minimalReduceStackHRecycled, 0, minimalReduceStackH, minimalReduceStackHFree, minimalReduceStackHRecycledCount);
            minimalReduceStackHFree += minimalReduceStackHRecycledCount;
        }
        if (minimalReduceStackHAverageUse != 0) minimalReduceStackHAverageUse = (minimalReduceStackHAverageUse + minimalReduceStackHEffectivellyUsed) / 2; else minimalReduceStackHAverageUse = minimalReduceStackHEffectivellyUsed;
        for (int i = 0; i < minimalReduceStackHRecycled.length; i++) {
            minimalReduceStackHRecycled[i] = null;
        }
        neededLength = maximalReduceStackHFree + maximalReduceStackHRecycledCount;
        if (neededLength > maximalReduceStackH.length) {
            if (neededLength > maximalReduceStackHMaxSize) {
                neededLength = maximalReduceStackHMaxSize;
                MaximalReduceStackHandler[] increased = new MaximalReduceStackHandler[neededLength];
                System.arraycopy(maximalReduceStackH, 0, increased, 0, maximalReduceStackH.length);
                maximalReduceStackH = increased;
                System.arraycopy(maximalReduceStackHRecycled, 0, maximalReduceStackH, maximalReduceStackHFree, maximalReduceStackHMaxSize - maximalReduceStackHFree);
                maximalReduceStackHFree = maximalReduceStackHMaxSize;
            } else {
                MaximalReduceStackHandler[] increased = new MaximalReduceStackHandler[neededLength];
                System.arraycopy(maximalReduceStackH, 0, increased, 0, maximalReduceStackH.length);
                maximalReduceStackH = increased;
                System.arraycopy(maximalReduceStackHRecycled, 0, maximalReduceStackH, maximalReduceStackHFree, maximalReduceStackHRecycledCount);
                maximalReduceStackHFree += maximalReduceStackHRecycledCount;
            }
        } else {
            System.arraycopy(maximalReduceStackHRecycled, 0, maximalReduceStackH, maximalReduceStackHFree, maximalReduceStackHRecycledCount);
            maximalReduceStackHFree += maximalReduceStackHRecycledCount;
        }
        if (maximalReduceStackHAverageUse != 0) maximalReduceStackHAverageUse = (maximalReduceStackHAverageUse + maximalReduceStackHEffectivellyUsed) / 2; else maximalReduceStackHAverageUse = maximalReduceStackHEffectivellyUsed;
        for (int i = 0; i < maximalReduceStackHRecycled.length; i++) {
            maximalReduceStackHRecycled[i] = null;
        }
        neededLength = candidateStackHFree + candidateStackHRecycledCount;
        if (neededLength > candidateStackH.length) {
            if (neededLength > candidateStackHMaxSize) {
                neededLength = candidateStackHMaxSize;
                CandidateStackHandlerImpl[] increased = new CandidateStackHandlerImpl[neededLength];
                System.arraycopy(candidateStackH, 0, increased, 0, candidateStackH.length);
                candidateStackH = increased;
                System.arraycopy(candidateStackHRecycled, 0, candidateStackH, candidateStackHFree, candidateStackHMaxSize - candidateStackHFree);
                candidateStackHFree = candidateStackHMaxSize;
            } else {
                CandidateStackHandlerImpl[] increased = new CandidateStackHandlerImpl[neededLength];
                System.arraycopy(candidateStackH, 0, increased, 0, candidateStackH.length);
                candidateStackH = increased;
                System.arraycopy(candidateStackHRecycled, 0, candidateStackH, candidateStackHFree, candidateStackHRecycledCount);
                candidateStackHFree += candidateStackHRecycledCount;
            }
        } else {
            System.arraycopy(candidateStackHRecycled, 0, candidateStackH, candidateStackHFree, candidateStackHRecycledCount);
            candidateStackHFree += candidateStackHRecycledCount;
        }
        if (candidateStackHAverageUse != 0) candidateStackHAverageUse = (candidateStackHAverageUse + candidateStackHEffectivellyUsed) / 2; else candidateStackHAverageUse = candidateStackHEffectivellyUsed;
        for (int i = 0; i < candidateStackHRecycled.length; i++) {
            candidateStackHRecycled[i] = null;
        }
        neededLength = concurrentStackHFree + concurrentStackHRecycledCount;
        if (neededLength > concurrentStackH.length) {
            if (neededLength > concurrentStackHMaxSize) {
                neededLength = concurrentStackHMaxSize;
                ConcurrentStackHandlerImpl[] increased = new ConcurrentStackHandlerImpl[neededLength];
                System.arraycopy(concurrentStackH, 0, increased, 0, concurrentStackH.length);
                concurrentStackH = increased;
                System.arraycopy(concurrentStackHRecycled, 0, concurrentStackH, concurrentStackHFree, concurrentStackHMaxSize - concurrentStackHFree);
                concurrentStackHFree = concurrentStackHMaxSize;
            } else {
                ConcurrentStackHandlerImpl[] increased = new ConcurrentStackHandlerImpl[neededLength];
                System.arraycopy(concurrentStackH, 0, increased, 0, concurrentStackH.length);
                concurrentStackH = increased;
                System.arraycopy(concurrentStackHRecycled, 0, concurrentStackH, concurrentStackHFree, concurrentStackHRecycledCount);
                concurrentStackHFree += concurrentStackHRecycledCount;
            }
        } else {
            System.arraycopy(concurrentStackHRecycled, 0, concurrentStackH, concurrentStackHFree, concurrentStackHRecycledCount);
            concurrentStackHFree += concurrentStackHRecycledCount;
        }
        if (concurrentStackHAverageUse != 0) concurrentStackHAverageUse = (concurrentStackHAverageUse + concurrentStackHEffectivellyUsed) / 2; else concurrentStackHAverageUse = concurrentStackHEffectivellyUsed;
        for (int i = 0; i < concurrentStackHRecycled.length; i++) {
            concurrentStackHRecycled[i] = null;
        }
    }
}
