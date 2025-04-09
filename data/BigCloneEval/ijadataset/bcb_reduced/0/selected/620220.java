package serene.validation.handlers.conflict;

public class SynchronizedConflictHandlerPool extends ConflictHandlerPool {

    private static volatile ConflictHandlerPool instance;

    int amchPoolFree;

    int amchPoolMaxSize;

    ActiveModelConflictHandlerPool[] amchPools;

    int ambiguousElementConflictResolverAverageUse;

    int ambiguousElementConflictResolverMaxSize;

    int ambiguousElementConflictResolverFree;

    AmbiguousElementConflictResolver[] ambiguousElementConflictResolver;

    int unresolvedElementConflictResolverAverageUse;

    int unresolvedElementConflictResolverMaxSize;

    int unresolvedElementConflictResolverFree;

    UnresolvedElementConflictResolver[] unresolvedElementConflictResolver;

    int ambiguousAttributeConflictResolverAverageUse;

    int ambiguousAttributeConflictResolverMaxSize;

    int ambiguousAttributeConflictResolverFree;

    AmbiguousAttributeConflictResolver[] ambiguousAttributeConflictResolver;

    int unresolvedAttributeConflictResolverAverageUse;

    int unresolvedAttributeConflictResolverMaxSize;

    int unresolvedAttributeConflictResolverFree;

    UnresolvedAttributeConflictResolver[] unresolvedAttributeConflictResolver;

    int ambiguousCharsConflictResolverAverageUse;

    int ambiguousCharsConflictResolverMaxSize;

    int ambiguousCharsConflictResolverFree;

    AmbiguousCharsConflictResolver[] ambiguousCharsConflictResolver;

    int unresolvedCharsConflictResolverAverageUse;

    int unresolvedCharsConflictResolverMaxSize;

    int unresolvedCharsConflictResolverFree;

    UnresolvedCharsConflictResolver[] unresolvedCharsConflictResolver;

    int ambiguousListTokenConflictResolverAverageUse;

    int ambiguousListTokenConflictResolverMaxSize;

    int ambiguousListTokenConflictResolverFree;

    AmbiguousListTokenConflictResolver[] ambiguousListTokenConflictResolver;

    int unresolvedListTokenConflictResolverAverageUse;

    int unresolvedListTokenConflictResolverMaxSize;

    int unresolvedListTokenConflictResolverFree;

    UnresolvedListTokenConflictResolver[] unresolvedListTokenConflictResolver;

    int boundAmbiguousElementConflictResolverAverageUse;

    int boundAmbiguousElementConflictResolverMaxSize;

    int boundAmbiguousElementConflictResolverFree;

    BoundAmbiguousElementConflictResolver[] boundAmbiguousElementConflictResolver;

    int boundUnresolvedElementConflictResolverAverageUse;

    int boundUnresolvedElementConflictResolverMaxSize;

    int boundUnresolvedElementConflictResolverFree;

    BoundUnresolvedElementConflictResolver[] boundUnresolvedElementConflictResolver;

    int boundAmbiguousAttributeConflictResolverAverageUse;

    int boundAmbiguousAttributeConflictResolverMaxSize;

    int boundAmbiguousAttributeConflictResolverFree;

    BoundAmbiguousAttributeConflictResolver[] boundAmbiguousAttributeConflictResolver;

    int boundUnresolvedAttributeConflictResolverAverageUse;

    int boundUnresolvedAttributeConflictResolverMaxSize;

    int boundUnresolvedAttributeConflictResolverFree;

    BoundUnresolvedAttributeConflictResolver[] boundUnresolvedAttributeConflictResolver;

    SynchronizedConflictHandlerPool() {
        super();
        amchPoolFree = 0;
        amchPoolMaxSize = 10;
        amchPools = new ActiveModelConflictHandlerPool[5];
        ambiguousElementConflictResolverAverageUse = 0;
        ambiguousElementConflictResolverFree = 0;
        ambiguousElementConflictResolver = new AmbiguousElementConflictResolver[10];
        unresolvedElementConflictResolverAverageUse = 0;
        unresolvedElementConflictResolverFree = 0;
        unresolvedElementConflictResolver = new UnresolvedElementConflictResolver[10];
        ambiguousAttributeConflictResolverAverageUse = 0;
        ambiguousAttributeConflictResolverFree = 0;
        ambiguousAttributeConflictResolver = new AmbiguousAttributeConflictResolver[10];
        unresolvedAttributeConflictResolverAverageUse = 0;
        unresolvedAttributeConflictResolverFree = 0;
        unresolvedAttributeConflictResolver = new UnresolvedAttributeConflictResolver[10];
        ambiguousCharsConflictResolverAverageUse = 0;
        ambiguousCharsConflictResolverFree = 0;
        ambiguousCharsConflictResolver = new AmbiguousCharsConflictResolver[10];
        unresolvedCharsConflictResolverAverageUse = 0;
        unresolvedCharsConflictResolverFree = 0;
        unresolvedCharsConflictResolver = new UnresolvedCharsConflictResolver[10];
        ambiguousListTokenConflictResolverAverageUse = 0;
        ambiguousListTokenConflictResolverFree = 0;
        ambiguousListTokenConflictResolver = new AmbiguousListTokenConflictResolver[10];
        unresolvedListTokenConflictResolverAverageUse = 0;
        unresolvedListTokenConflictResolverFree = 0;
        unresolvedListTokenConflictResolver = new UnresolvedListTokenConflictResolver[10];
        boundAmbiguousElementConflictResolverAverageUse = 0;
        boundAmbiguousElementConflictResolverFree = 0;
        boundAmbiguousElementConflictResolver = new BoundAmbiguousElementConflictResolver[10];
        boundUnresolvedElementConflictResolverAverageUse = 0;
        boundUnresolvedElementConflictResolverFree = 0;
        boundUnresolvedElementConflictResolver = new BoundUnresolvedElementConflictResolver[10];
        boundAmbiguousAttributeConflictResolverAverageUse = 0;
        boundAmbiguousAttributeConflictResolverFree = 0;
        boundAmbiguousAttributeConflictResolver = new BoundAmbiguousAttributeConflictResolver[10];
        boundUnresolvedAttributeConflictResolverAverageUse = 0;
        boundUnresolvedAttributeConflictResolverFree = 0;
        boundUnresolvedAttributeConflictResolver = new BoundUnresolvedAttributeConflictResolver[10];
        ambiguousElementConflictResolverMaxSize = 10;
        unresolvedElementConflictResolverMaxSize = 10;
        ambiguousAttributeConflictResolverMaxSize = 10;
        unresolvedAttributeConflictResolverMaxSize = 10;
        ambiguousCharsConflictResolverMaxSize = 10;
        unresolvedCharsConflictResolverMaxSize = 10;
        ambiguousListTokenConflictResolverMaxSize = 10;
        unresolvedListTokenConflictResolverMaxSize = 10;
        boundAmbiguousElementConflictResolverMaxSize = 10;
        boundUnresolvedElementConflictResolverMaxSize = 10;
        boundAmbiguousAttributeConflictResolverMaxSize = 10;
        boundUnresolvedAttributeConflictResolverMaxSize = 10;
    }

    public static ConflictHandlerPool getInstance() {
        if (instance == null) {
            synchronized (SynchronizedConflictHandlerPool.class) {
                if (instance == null) {
                    instance = new SynchronizedConflictHandlerPool();
                }
            }
        }
        return instance;
    }

    synchronized void fill(ActiveModelConflictHandlerPool pool, AmbiguousElementConflictResolver[] ambiguousElementConflictResolverToFill, UnresolvedElementConflictResolver[] unresolvedElementConflictResolverToFill, AmbiguousAttributeConflictResolver[] ambiguousAttributeConflictResolverToFill, UnresolvedAttributeConflictResolver[] unresolvedAttributeConflictResolverToFill, AmbiguousCharsConflictResolver[] ambiguousCharsConflictResolverToFill, UnresolvedCharsConflictResolver[] unresolvedCharsConflictResolverToFill, AmbiguousListTokenConflictResolver[] ambiguousListTokenConflictResolverToFill, UnresolvedListTokenConflictResolver[] unresolvedListTokenConflictResolverToFill, BoundAmbiguousElementConflictResolver[] boundAmbiguousElementConflictResolverToFill, BoundUnresolvedElementConflictResolver[] boundUnresolvedElementConflictResolverToFill, BoundAmbiguousAttributeConflictResolver[] boundAmbiguousAttributeConflictResolverToFill, BoundUnresolvedAttributeConflictResolver[] boundUnresolvedAttributeConflictResolverToFill) {
        int ambiguousElementConflictResolverFillCount;
        if (ambiguousElementConflictResolverToFill == null || ambiguousElementConflictResolverToFill.length < ambiguousElementConflictResolverAverageUse) {
            ambiguousElementConflictResolverToFill = new AmbiguousElementConflictResolver[ambiguousElementConflictResolverAverageUse];
            pool.ambiguousElementConflictResolver = ambiguousElementConflictResolverToFill;
        }
        if (ambiguousElementConflictResolverFree > ambiguousElementConflictResolverAverageUse) {
            ambiguousElementConflictResolverFillCount = ambiguousElementConflictResolverAverageUse;
            ambiguousElementConflictResolverFree = ambiguousElementConflictResolverFree - ambiguousElementConflictResolverAverageUse;
        } else {
            ambiguousElementConflictResolverFillCount = ambiguousElementConflictResolverFree;
            ambiguousElementConflictResolverFree = 0;
        }
        System.arraycopy(ambiguousElementConflictResolver, ambiguousElementConflictResolverFree, ambiguousElementConflictResolverToFill, 0, ambiguousElementConflictResolverFillCount);
        int unresolvedElementConflictResolverFillCount;
        if (unresolvedElementConflictResolverToFill == null || unresolvedElementConflictResolverToFill.length < unresolvedElementConflictResolverAverageUse) {
            unresolvedElementConflictResolverToFill = new UnresolvedElementConflictResolver[unresolvedElementConflictResolverAverageUse];
            pool.unresolvedElementConflictResolver = unresolvedElementConflictResolverToFill;
        }
        if (unresolvedElementConflictResolverFree > unresolvedElementConflictResolverAverageUse) {
            unresolvedElementConflictResolverFillCount = unresolvedElementConflictResolverAverageUse;
            unresolvedElementConflictResolverFree = unresolvedElementConflictResolverFree - unresolvedElementConflictResolverAverageUse;
        } else {
            unresolvedElementConflictResolverFillCount = unresolvedElementConflictResolverFree;
            unresolvedElementConflictResolverFree = 0;
        }
        System.arraycopy(unresolvedElementConflictResolver, unresolvedElementConflictResolverFree, unresolvedElementConflictResolverToFill, 0, unresolvedElementConflictResolverFillCount);
        int ambiguousAttributeConflictResolverFillCount;
        if (ambiguousAttributeConflictResolverToFill == null || ambiguousAttributeConflictResolverToFill.length < ambiguousAttributeConflictResolverAverageUse) {
            ambiguousAttributeConflictResolverToFill = new AmbiguousAttributeConflictResolver[ambiguousAttributeConflictResolverAverageUse];
            pool.ambiguousAttributeConflictResolver = ambiguousAttributeConflictResolverToFill;
        }
        if (ambiguousAttributeConflictResolverFree > ambiguousAttributeConflictResolverAverageUse) {
            ambiguousAttributeConflictResolverFillCount = ambiguousAttributeConflictResolverAverageUse;
            ambiguousAttributeConflictResolverFree = ambiguousAttributeConflictResolverFree - ambiguousAttributeConflictResolverAverageUse;
        } else {
            ambiguousAttributeConflictResolverFillCount = ambiguousAttributeConflictResolverFree;
            ambiguousAttributeConflictResolverFree = 0;
        }
        System.arraycopy(ambiguousAttributeConflictResolver, ambiguousAttributeConflictResolverFree, ambiguousAttributeConflictResolverToFill, 0, ambiguousAttributeConflictResolverFillCount);
        int unresolvedAttributeConflictResolverFillCount;
        if (unresolvedAttributeConflictResolverToFill == null || unresolvedAttributeConflictResolverToFill.length < unresolvedAttributeConflictResolverAverageUse) {
            unresolvedAttributeConflictResolverToFill = new UnresolvedAttributeConflictResolver[unresolvedAttributeConflictResolverAverageUse];
            pool.unresolvedAttributeConflictResolver = unresolvedAttributeConflictResolverToFill;
        }
        if (unresolvedAttributeConflictResolverFree > unresolvedAttributeConflictResolverAverageUse) {
            unresolvedAttributeConflictResolverFillCount = unresolvedAttributeConflictResolverAverageUse;
            unresolvedAttributeConflictResolverFree = unresolvedAttributeConflictResolverFree - unresolvedAttributeConflictResolverAverageUse;
        } else {
            unresolvedAttributeConflictResolverFillCount = unresolvedAttributeConflictResolverFree;
            unresolvedAttributeConflictResolverFree = 0;
        }
        System.arraycopy(unresolvedAttributeConflictResolver, unresolvedAttributeConflictResolverFree, unresolvedAttributeConflictResolverToFill, 0, unresolvedAttributeConflictResolverFillCount);
        int ambiguousCharsConflictResolverFillCount;
        if (ambiguousCharsConflictResolverToFill == null || ambiguousCharsConflictResolverToFill.length < ambiguousCharsConflictResolverAverageUse) {
            ambiguousCharsConflictResolverToFill = new AmbiguousCharsConflictResolver[ambiguousCharsConflictResolverAverageUse];
            pool.ambiguousCharsConflictResolver = ambiguousCharsConflictResolverToFill;
        }
        if (ambiguousCharsConflictResolverFree > ambiguousCharsConflictResolverAverageUse) {
            ambiguousCharsConflictResolverFillCount = ambiguousCharsConflictResolverAverageUse;
            ambiguousCharsConflictResolverFree = ambiguousCharsConflictResolverFree - ambiguousCharsConflictResolverAverageUse;
        } else {
            ambiguousCharsConflictResolverFillCount = ambiguousCharsConflictResolverFree;
            ambiguousCharsConflictResolverFree = 0;
        }
        System.arraycopy(ambiguousCharsConflictResolver, ambiguousCharsConflictResolverFree, ambiguousCharsConflictResolverToFill, 0, ambiguousCharsConflictResolverFillCount);
        int unresolvedCharsConflictResolverFillCount;
        if (unresolvedCharsConflictResolverToFill == null || unresolvedCharsConflictResolverToFill.length < unresolvedCharsConflictResolverAverageUse) {
            unresolvedCharsConflictResolverToFill = new UnresolvedCharsConflictResolver[unresolvedCharsConflictResolverAverageUse];
            pool.unresolvedCharsConflictResolver = unresolvedCharsConflictResolverToFill;
        }
        if (unresolvedCharsConflictResolverFree > unresolvedCharsConflictResolverAverageUse) {
            unresolvedCharsConflictResolverFillCount = unresolvedCharsConflictResolverAverageUse;
            unresolvedCharsConflictResolverFree = unresolvedCharsConflictResolverFree - unresolvedCharsConflictResolverAverageUse;
        } else {
            unresolvedCharsConflictResolverFillCount = unresolvedCharsConflictResolverFree;
            unresolvedCharsConflictResolverFree = 0;
        }
        System.arraycopy(unresolvedCharsConflictResolver, unresolvedCharsConflictResolverFree, unresolvedCharsConflictResolverToFill, 0, unresolvedCharsConflictResolverFillCount);
        int ambiguousListTokenConflictResolverFillCount;
        if (ambiguousListTokenConflictResolverToFill == null || ambiguousListTokenConflictResolverToFill.length < ambiguousListTokenConflictResolverAverageUse) {
            ambiguousListTokenConflictResolverToFill = new AmbiguousListTokenConflictResolver[ambiguousListTokenConflictResolverAverageUse];
            pool.ambiguousListTokenConflictResolver = ambiguousListTokenConflictResolverToFill;
        }
        if (ambiguousListTokenConflictResolverFree > ambiguousListTokenConflictResolverAverageUse) {
            ambiguousListTokenConflictResolverFillCount = ambiguousListTokenConflictResolverAverageUse;
            ambiguousListTokenConflictResolverFree = ambiguousListTokenConflictResolverFree - ambiguousListTokenConflictResolverAverageUse;
        } else {
            ambiguousListTokenConflictResolverFillCount = ambiguousListTokenConflictResolverFree;
            ambiguousListTokenConflictResolverFree = 0;
        }
        System.arraycopy(ambiguousListTokenConflictResolver, ambiguousListTokenConflictResolverFree, ambiguousListTokenConflictResolverToFill, 0, ambiguousListTokenConflictResolverFillCount);
        int unresolvedListTokenConflictResolverFillCount;
        if (unresolvedListTokenConflictResolverToFill == null || unresolvedListTokenConflictResolverToFill.length < unresolvedListTokenConflictResolverAverageUse) {
            unresolvedListTokenConflictResolverToFill = new UnresolvedListTokenConflictResolver[unresolvedListTokenConflictResolverAverageUse];
            pool.unresolvedListTokenConflictResolver = unresolvedListTokenConflictResolverToFill;
        }
        if (unresolvedListTokenConflictResolverFree > unresolvedListTokenConflictResolverAverageUse) {
            unresolvedListTokenConflictResolverFillCount = unresolvedListTokenConflictResolverAverageUse;
            unresolvedListTokenConflictResolverFree = unresolvedListTokenConflictResolverFree - unresolvedListTokenConflictResolverAverageUse;
        } else {
            unresolvedListTokenConflictResolverFillCount = unresolvedListTokenConflictResolverFree;
            unresolvedListTokenConflictResolverFree = 0;
        }
        System.arraycopy(unresolvedListTokenConflictResolver, unresolvedListTokenConflictResolverFree, unresolvedListTokenConflictResolverToFill, 0, unresolvedListTokenConflictResolverFillCount);
        int boundAmbiguousElementConflictResolverFillCount;
        if (boundAmbiguousElementConflictResolverToFill == null || boundAmbiguousElementConflictResolverToFill.length < boundAmbiguousElementConflictResolverAverageUse) {
            boundAmbiguousElementConflictResolverToFill = new BoundAmbiguousElementConflictResolver[boundAmbiguousElementConflictResolverAverageUse];
            pool.boundAmbiguousElementConflictResolver = boundAmbiguousElementConflictResolverToFill;
        }
        if (boundAmbiguousElementConflictResolverFree > boundAmbiguousElementConflictResolverAverageUse) {
            boundAmbiguousElementConflictResolverFillCount = boundAmbiguousElementConflictResolverAverageUse;
            boundAmbiguousElementConflictResolverFree = boundAmbiguousElementConflictResolverFree - boundAmbiguousElementConflictResolverAverageUse;
        } else {
            boundAmbiguousElementConflictResolverFillCount = boundAmbiguousElementConflictResolverFree;
            boundAmbiguousElementConflictResolverFree = 0;
        }
        System.arraycopy(boundAmbiguousElementConflictResolver, boundAmbiguousElementConflictResolverFree, boundAmbiguousElementConflictResolverToFill, 0, boundAmbiguousElementConflictResolverFillCount);
        int boundUnresolvedElementConflictResolverFillCount;
        if (boundUnresolvedElementConflictResolverToFill == null || boundUnresolvedElementConflictResolverToFill.length < boundUnresolvedElementConflictResolverAverageUse) {
            boundUnresolvedElementConflictResolverToFill = new BoundUnresolvedElementConflictResolver[boundUnresolvedElementConflictResolverAverageUse];
            pool.boundUnresolvedElementConflictResolver = boundUnresolvedElementConflictResolverToFill;
        }
        if (boundUnresolvedElementConflictResolverFree > boundUnresolvedElementConflictResolverAverageUse) {
            boundUnresolvedElementConflictResolverFillCount = boundUnresolvedElementConflictResolverAverageUse;
            boundUnresolvedElementConflictResolverFree = boundUnresolvedElementConflictResolverFree - boundUnresolvedElementConflictResolverAverageUse;
        } else {
            boundUnresolvedElementConflictResolverFillCount = boundUnresolvedElementConflictResolverFree;
            boundUnresolvedElementConflictResolverFree = 0;
        }
        System.arraycopy(boundUnresolvedElementConflictResolver, boundUnresolvedElementConflictResolverFree, boundUnresolvedElementConflictResolverToFill, 0, boundUnresolvedElementConflictResolverFillCount);
        int boundAmbiguousAttributeConflictResolverFillCount;
        if (boundAmbiguousAttributeConflictResolverToFill == null || boundAmbiguousAttributeConflictResolverToFill.length < boundAmbiguousAttributeConflictResolverAverageUse) {
            boundAmbiguousAttributeConflictResolverToFill = new BoundAmbiguousAttributeConflictResolver[boundAmbiguousAttributeConflictResolverAverageUse];
            pool.boundAmbiguousAttributeConflictResolver = boundAmbiguousAttributeConflictResolverToFill;
        }
        if (boundAmbiguousAttributeConflictResolverFree > boundAmbiguousAttributeConflictResolverAverageUse) {
            boundAmbiguousAttributeConflictResolverFillCount = boundAmbiguousAttributeConflictResolverAverageUse;
            boundAmbiguousAttributeConflictResolverFree = boundAmbiguousAttributeConflictResolverFree - boundAmbiguousAttributeConflictResolverAverageUse;
        } else {
            boundAmbiguousAttributeConflictResolverFillCount = boundAmbiguousAttributeConflictResolverFree;
            boundAmbiguousAttributeConflictResolverFree = 0;
        }
        System.arraycopy(boundAmbiguousAttributeConflictResolver, boundAmbiguousAttributeConflictResolverFree, boundAmbiguousAttributeConflictResolverToFill, 0, boundAmbiguousAttributeConflictResolverFillCount);
        int boundUnresolvedAttributeConflictResolverFillCount;
        if (boundUnresolvedAttributeConflictResolverToFill == null || boundUnresolvedAttributeConflictResolverToFill.length < boundUnresolvedAttributeConflictResolverAverageUse) {
            boundUnresolvedAttributeConflictResolverToFill = new BoundUnresolvedAttributeConflictResolver[boundUnresolvedAttributeConflictResolverAverageUse];
            pool.boundUnresolvedAttributeConflictResolver = boundUnresolvedAttributeConflictResolverToFill;
        }
        if (boundUnresolvedAttributeConflictResolverFree > boundUnresolvedAttributeConflictResolverAverageUse) {
            boundUnresolvedAttributeConflictResolverFillCount = boundUnresolvedAttributeConflictResolverAverageUse;
            boundUnresolvedAttributeConflictResolverFree = boundUnresolvedAttributeConflictResolverFree - boundUnresolvedAttributeConflictResolverAverageUse;
        } else {
            boundUnresolvedAttributeConflictResolverFillCount = boundUnresolvedAttributeConflictResolverFree;
            boundUnresolvedAttributeConflictResolverFree = 0;
        }
        System.arraycopy(boundUnresolvedAttributeConflictResolver, boundUnresolvedAttributeConflictResolverFree, boundUnresolvedAttributeConflictResolverToFill, 0, boundUnresolvedAttributeConflictResolverFillCount);
        pool.initFilled(ambiguousElementConflictResolverFillCount, unresolvedElementConflictResolverFillCount, ambiguousAttributeConflictResolverFillCount, unresolvedAttributeConflictResolverFillCount, ambiguousCharsConflictResolverFillCount, unresolvedCharsConflictResolverFillCount, ambiguousListTokenConflictResolverFillCount, unresolvedListTokenConflictResolverFillCount, boundAmbiguousElementConflictResolverFillCount, boundUnresolvedElementConflictResolverFillCount, boundAmbiguousAttributeConflictResolverFillCount, boundUnresolvedAttributeConflictResolverFillCount);
    }

    synchronized void recycle(int ambiguousElementConflictResolverRecycledCount, int ambiguousElementConflictResolverEffectivellyUsed, AmbiguousElementConflictResolver[] ambiguousElementConflictResolverRecycled, int unresolvedElementConflictResolverRecycledCount, int unresolvedElementConflictResolverEffectivellyUsed, UnresolvedElementConflictResolver[] unresolvedElementConflictResolverRecycled, int ambiguousAttributeConflictResolverRecycledCount, int ambiguousAttributeConflictResolverEffectivellyUsed, AmbiguousAttributeConflictResolver[] ambiguousAttributeConflictResolverRecycled, int unresolvedAttributeConflictResolverRecycledCount, int unresolvedAttributeConflictResolverEffectivellyUsed, UnresolvedAttributeConflictResolver[] unresolvedAttributeConflictResolverRecycled, int ambiguousCharsConflictResolverRecycledCount, int ambiguousCharsConflictResolverEffectivellyUsed, AmbiguousCharsConflictResolver[] ambiguousCharsConflictResolverRecycled, int unresolvedCharsConflictResolverRecycledCount, int unresolvedCharsConflictResolverEffectivellyUsed, UnresolvedCharsConflictResolver[] unresolvedCharsConflictResolverRecycled, int ambiguousListTokenConflictResolverRecycledCount, int ambiguousListTokenConflictResolverEffectivellyUsed, AmbiguousListTokenConflictResolver[] ambiguousListTokenConflictResolverRecycled, int unresolvedListTokenConflictResolverRecycledCount, int unresolvedListTokenConflictResolverEffectivellyUsed, UnresolvedListTokenConflictResolver[] unresolvedListTokenConflictResolverRecycled, int boundAmbiguousElementConflictResolverRecycledCount, int boundAmbiguousElementConflictResolverEffectivellyUsed, BoundAmbiguousElementConflictResolver[] boundAmbiguousElementConflictResolverRecycled, int boundUnresolvedElementConflictResolverRecycledCount, int boundUnresolvedElementConflictResolverEffectivellyUsed, BoundUnresolvedElementConflictResolver[] boundUnresolvedElementConflictResolverRecycled, int boundAmbiguousAttributeConflictResolverRecycledCount, int boundAmbiguousAttributeConflictResolverEffectivellyUsed, BoundAmbiguousAttributeConflictResolver[] boundAmbiguousAttributeConflictResolverRecycled, int boundUnresolvedAttributeConflictResolverRecycledCount, int boundUnresolvedAttributeConflictResolverEffectivellyUsed, BoundUnresolvedAttributeConflictResolver[] boundUnresolvedAttributeConflictResolverRecycled) {
        int neededLength = ambiguousElementConflictResolverFree + ambiguousElementConflictResolverRecycledCount;
        if (neededLength > ambiguousElementConflictResolver.length) {
            if (neededLength > ambiguousElementConflictResolverMaxSize) {
                neededLength = ambiguousElementConflictResolverMaxSize;
                AmbiguousElementConflictResolver[] increased = new AmbiguousElementConflictResolver[neededLength];
                System.arraycopy(ambiguousElementConflictResolver, 0, increased, 0, ambiguousElementConflictResolver.length);
                ambiguousElementConflictResolver = increased;
                System.arraycopy(ambiguousElementConflictResolverRecycled, 0, ambiguousElementConflictResolver, ambiguousElementConflictResolverFree, ambiguousElementConflictResolverMaxSize - ambiguousElementConflictResolverFree);
                ambiguousElementConflictResolverFree = ambiguousElementConflictResolverMaxSize;
            } else {
                AmbiguousElementConflictResolver[] increased = new AmbiguousElementConflictResolver[neededLength];
                System.arraycopy(ambiguousElementConflictResolver, 0, increased, 0, ambiguousElementConflictResolver.length);
                ambiguousElementConflictResolver = increased;
                System.arraycopy(ambiguousElementConflictResolverRecycled, 0, ambiguousElementConflictResolver, ambiguousElementConflictResolverFree, ambiguousElementConflictResolverRecycledCount);
                ambiguousElementConflictResolverFree += ambiguousElementConflictResolverRecycledCount;
            }
        } else {
            System.arraycopy(ambiguousElementConflictResolverRecycled, 0, ambiguousElementConflictResolver, ambiguousElementConflictResolverFree, ambiguousElementConflictResolverRecycledCount);
            ambiguousElementConflictResolverFree += ambiguousElementConflictResolverRecycledCount;
        }
        if (ambiguousElementConflictResolverAverageUse != 0) ambiguousElementConflictResolverAverageUse = (ambiguousElementConflictResolverAverageUse + ambiguousElementConflictResolverEffectivellyUsed) / 2; else ambiguousElementConflictResolverAverageUse = ambiguousElementConflictResolverEffectivellyUsed;
        for (int i = 0; i < ambiguousElementConflictResolverRecycled.length; i++) {
            ambiguousElementConflictResolverRecycled[i] = null;
        }
        neededLength = unresolvedElementConflictResolverFree + unresolvedElementConflictResolverRecycledCount;
        if (neededLength > unresolvedElementConflictResolver.length) {
            if (neededLength > unresolvedElementConflictResolverMaxSize) {
                neededLength = unresolvedElementConflictResolverMaxSize;
                UnresolvedElementConflictResolver[] increased = new UnresolvedElementConflictResolver[neededLength];
                System.arraycopy(unresolvedElementConflictResolver, 0, increased, 0, unresolvedElementConflictResolver.length);
                unresolvedElementConflictResolver = increased;
                System.arraycopy(unresolvedElementConflictResolverRecycled, 0, unresolvedElementConflictResolver, unresolvedElementConflictResolverFree, unresolvedElementConflictResolverMaxSize - unresolvedElementConflictResolverFree);
                unresolvedElementConflictResolverFree = unresolvedElementConflictResolverMaxSize;
            } else {
                UnresolvedElementConflictResolver[] increased = new UnresolvedElementConflictResolver[neededLength];
                System.arraycopy(unresolvedElementConflictResolver, 0, increased, 0, unresolvedElementConflictResolver.length);
                unresolvedElementConflictResolver = increased;
                System.arraycopy(unresolvedElementConflictResolverRecycled, 0, unresolvedElementConflictResolver, unresolvedElementConflictResolverFree, unresolvedElementConflictResolverRecycledCount);
                unresolvedElementConflictResolverFree += unresolvedElementConflictResolverRecycledCount;
            }
        } else {
            System.arraycopy(unresolvedElementConflictResolverRecycled, 0, unresolvedElementConflictResolver, unresolvedElementConflictResolverFree, unresolvedElementConflictResolverRecycledCount);
            unresolvedElementConflictResolverFree += unresolvedElementConflictResolverRecycledCount;
        }
        if (unresolvedElementConflictResolverAverageUse != 0) unresolvedElementConflictResolverAverageUse = (unresolvedElementConflictResolverAverageUse + unresolvedElementConflictResolverEffectivellyUsed) / 2; else unresolvedElementConflictResolverAverageUse = unresolvedElementConflictResolverEffectivellyUsed;
        for (int i = 0; i < unresolvedElementConflictResolverRecycled.length; i++) {
            unresolvedElementConflictResolverRecycled[i] = null;
        }
        neededLength = ambiguousAttributeConflictResolverFree + ambiguousAttributeConflictResolverRecycledCount;
        if (neededLength > ambiguousAttributeConflictResolver.length) {
            if (neededLength > ambiguousAttributeConflictResolverMaxSize) {
                neededLength = ambiguousAttributeConflictResolverMaxSize;
                AmbiguousAttributeConflictResolver[] increased = new AmbiguousAttributeConflictResolver[neededLength];
                System.arraycopy(ambiguousAttributeConflictResolver, 0, increased, 0, ambiguousAttributeConflictResolver.length);
                ambiguousAttributeConflictResolver = increased;
                System.arraycopy(ambiguousAttributeConflictResolverRecycled, 0, ambiguousAttributeConflictResolver, ambiguousAttributeConflictResolverFree, ambiguousAttributeConflictResolverMaxSize - ambiguousAttributeConflictResolverFree);
                ambiguousAttributeConflictResolverFree = ambiguousAttributeConflictResolverMaxSize;
            } else {
                AmbiguousAttributeConflictResolver[] increased = new AmbiguousAttributeConflictResolver[neededLength];
                System.arraycopy(ambiguousAttributeConflictResolver, 0, increased, 0, ambiguousAttributeConflictResolver.length);
                ambiguousAttributeConflictResolver = increased;
                System.arraycopy(ambiguousAttributeConflictResolverRecycled, 0, ambiguousAttributeConflictResolver, ambiguousAttributeConflictResolverFree, ambiguousAttributeConflictResolverRecycledCount);
                ambiguousAttributeConflictResolverFree += ambiguousAttributeConflictResolverRecycledCount;
            }
        } else {
            System.arraycopy(ambiguousAttributeConflictResolverRecycled, 0, ambiguousAttributeConflictResolver, ambiguousAttributeConflictResolverFree, ambiguousAttributeConflictResolverRecycledCount);
            ambiguousAttributeConflictResolverFree += ambiguousAttributeConflictResolverRecycledCount;
        }
        if (ambiguousAttributeConflictResolverAverageUse != 0) ambiguousAttributeConflictResolverAverageUse = (ambiguousAttributeConflictResolverAverageUse + ambiguousAttributeConflictResolverEffectivellyUsed) / 2; else ambiguousAttributeConflictResolverAverageUse = ambiguousAttributeConflictResolverEffectivellyUsed;
        for (int i = 0; i < ambiguousAttributeConflictResolverRecycled.length; i++) {
            ambiguousAttributeConflictResolverRecycled[i] = null;
        }
        neededLength = unresolvedAttributeConflictResolverFree + unresolvedAttributeConflictResolverRecycledCount;
        if (neededLength > unresolvedAttributeConflictResolver.length) {
            if (neededLength > unresolvedAttributeConflictResolverMaxSize) {
                neededLength = unresolvedAttributeConflictResolverMaxSize;
                UnresolvedAttributeConflictResolver[] increased = new UnresolvedAttributeConflictResolver[neededLength];
                System.arraycopy(unresolvedAttributeConflictResolver, 0, increased, 0, unresolvedAttributeConflictResolver.length);
                unresolvedAttributeConflictResolver = increased;
                System.arraycopy(unresolvedAttributeConflictResolverRecycled, 0, unresolvedAttributeConflictResolver, unresolvedAttributeConflictResolverFree, unresolvedAttributeConflictResolverMaxSize - unresolvedAttributeConflictResolverFree);
                unresolvedAttributeConflictResolverFree = unresolvedAttributeConflictResolverMaxSize;
            } else {
                UnresolvedAttributeConflictResolver[] increased = new UnresolvedAttributeConflictResolver[neededLength];
                System.arraycopy(unresolvedAttributeConflictResolver, 0, increased, 0, unresolvedAttributeConflictResolver.length);
                unresolvedAttributeConflictResolver = increased;
                System.arraycopy(unresolvedAttributeConflictResolverRecycled, 0, unresolvedAttributeConflictResolver, unresolvedAttributeConflictResolverFree, unresolvedAttributeConflictResolverRecycledCount);
                unresolvedAttributeConflictResolverFree += unresolvedAttributeConflictResolverRecycledCount;
            }
        } else {
            System.arraycopy(unresolvedAttributeConflictResolverRecycled, 0, unresolvedAttributeConflictResolver, unresolvedAttributeConflictResolverFree, unresolvedAttributeConflictResolverRecycledCount);
            unresolvedAttributeConflictResolverFree += unresolvedAttributeConflictResolverRecycledCount;
        }
        if (unresolvedAttributeConflictResolverAverageUse != 0) unresolvedAttributeConflictResolverAverageUse = (unresolvedAttributeConflictResolverAverageUse + unresolvedAttributeConflictResolverEffectivellyUsed) / 2; else unresolvedAttributeConflictResolverAverageUse = unresolvedAttributeConflictResolverEffectivellyUsed;
        for (int i = 0; i < unresolvedAttributeConflictResolverRecycled.length; i++) {
            unresolvedAttributeConflictResolverRecycled[i] = null;
        }
        neededLength = ambiguousCharsConflictResolverFree + ambiguousCharsConflictResolverRecycledCount;
        if (neededLength > ambiguousCharsConflictResolver.length) {
            if (neededLength > ambiguousCharsConflictResolverMaxSize) {
                neededLength = ambiguousCharsConflictResolverMaxSize;
                AmbiguousCharsConflictResolver[] increased = new AmbiguousCharsConflictResolver[neededLength];
                System.arraycopy(ambiguousCharsConflictResolver, 0, increased, 0, ambiguousCharsConflictResolver.length);
                ambiguousCharsConflictResolver = increased;
                System.arraycopy(ambiguousCharsConflictResolverRecycled, 0, ambiguousCharsConflictResolver, ambiguousCharsConflictResolverFree, ambiguousCharsConflictResolverMaxSize - ambiguousCharsConflictResolverFree);
                ambiguousCharsConflictResolverFree = ambiguousCharsConflictResolverMaxSize;
            } else {
                AmbiguousCharsConflictResolver[] increased = new AmbiguousCharsConflictResolver[neededLength];
                System.arraycopy(ambiguousCharsConflictResolver, 0, increased, 0, ambiguousCharsConflictResolver.length);
                ambiguousCharsConflictResolver = increased;
                System.arraycopy(ambiguousCharsConflictResolverRecycled, 0, ambiguousCharsConflictResolver, ambiguousCharsConflictResolverFree, ambiguousCharsConflictResolverRecycledCount);
                ambiguousCharsConflictResolverFree += ambiguousCharsConflictResolverRecycledCount;
            }
        } else {
            System.arraycopy(ambiguousCharsConflictResolverRecycled, 0, ambiguousCharsConflictResolver, ambiguousCharsConflictResolverFree, ambiguousCharsConflictResolverRecycledCount);
            ambiguousCharsConflictResolverFree += ambiguousCharsConflictResolverRecycledCount;
        }
        if (ambiguousCharsConflictResolverAverageUse != 0) ambiguousCharsConflictResolverAverageUse = (ambiguousCharsConflictResolverAverageUse + ambiguousCharsConflictResolverEffectivellyUsed) / 2; else ambiguousCharsConflictResolverAverageUse = ambiguousCharsConflictResolverEffectivellyUsed;
        for (int i = 0; i < ambiguousCharsConflictResolverRecycled.length; i++) {
            ambiguousCharsConflictResolverRecycled[i] = null;
        }
        neededLength = unresolvedCharsConflictResolverFree + unresolvedCharsConflictResolverRecycledCount;
        if (neededLength > unresolvedCharsConflictResolver.length) {
            if (neededLength > unresolvedCharsConflictResolverMaxSize) {
                neededLength = unresolvedCharsConflictResolverMaxSize;
                UnresolvedCharsConflictResolver[] increased = new UnresolvedCharsConflictResolver[neededLength];
                System.arraycopy(unresolvedCharsConflictResolver, 0, increased, 0, unresolvedCharsConflictResolver.length);
                unresolvedCharsConflictResolver = increased;
                System.arraycopy(unresolvedCharsConflictResolverRecycled, 0, unresolvedCharsConflictResolver, unresolvedCharsConflictResolverFree, unresolvedCharsConflictResolverMaxSize - unresolvedCharsConflictResolverFree);
                unresolvedCharsConflictResolverFree = unresolvedCharsConflictResolverMaxSize;
            } else {
                UnresolvedCharsConflictResolver[] increased = new UnresolvedCharsConflictResolver[neededLength];
                System.arraycopy(unresolvedCharsConflictResolver, 0, increased, 0, unresolvedCharsConflictResolver.length);
                unresolvedCharsConflictResolver = increased;
                System.arraycopy(unresolvedCharsConflictResolverRecycled, 0, unresolvedCharsConflictResolver, unresolvedCharsConflictResolverFree, unresolvedCharsConflictResolverRecycledCount);
                unresolvedCharsConflictResolverFree += unresolvedCharsConflictResolverRecycledCount;
            }
        } else {
            System.arraycopy(unresolvedCharsConflictResolverRecycled, 0, unresolvedCharsConflictResolver, unresolvedCharsConflictResolverFree, unresolvedCharsConflictResolverRecycledCount);
            unresolvedCharsConflictResolverFree += unresolvedCharsConflictResolverRecycledCount;
        }
        if (unresolvedCharsConflictResolverAverageUse != 0) unresolvedCharsConflictResolverAverageUse = (unresolvedCharsConflictResolverAverageUse + unresolvedCharsConflictResolverEffectivellyUsed) / 2; else unresolvedCharsConflictResolverAverageUse = unresolvedCharsConflictResolverEffectivellyUsed;
        for (int i = 0; i < unresolvedCharsConflictResolverRecycled.length; i++) {
            unresolvedCharsConflictResolverRecycled[i] = null;
        }
        neededLength = ambiguousListTokenConflictResolverFree + ambiguousListTokenConflictResolverRecycledCount;
        if (neededLength > ambiguousListTokenConflictResolver.length) {
            if (neededLength > ambiguousListTokenConflictResolverMaxSize) {
                neededLength = ambiguousListTokenConflictResolverMaxSize;
                AmbiguousListTokenConflictResolver[] increased = new AmbiguousListTokenConflictResolver[neededLength];
                System.arraycopy(ambiguousListTokenConflictResolver, 0, increased, 0, ambiguousListTokenConflictResolver.length);
                ambiguousListTokenConflictResolver = increased;
                System.arraycopy(ambiguousListTokenConflictResolverRecycled, 0, ambiguousListTokenConflictResolver, ambiguousListTokenConflictResolverFree, ambiguousListTokenConflictResolverMaxSize - ambiguousListTokenConflictResolverFree);
                ambiguousListTokenConflictResolverFree = ambiguousListTokenConflictResolverMaxSize;
            } else {
                AmbiguousListTokenConflictResolver[] increased = new AmbiguousListTokenConflictResolver[neededLength];
                System.arraycopy(ambiguousListTokenConflictResolver, 0, increased, 0, ambiguousListTokenConflictResolver.length);
                ambiguousListTokenConflictResolver = increased;
                System.arraycopy(ambiguousListTokenConflictResolverRecycled, 0, ambiguousListTokenConflictResolver, ambiguousListTokenConflictResolverFree, ambiguousListTokenConflictResolverRecycledCount);
                ambiguousListTokenConflictResolverFree += ambiguousListTokenConflictResolverRecycledCount;
            }
        } else {
            System.arraycopy(ambiguousListTokenConflictResolverRecycled, 0, ambiguousListTokenConflictResolver, ambiguousListTokenConflictResolverFree, ambiguousListTokenConflictResolverRecycledCount);
            ambiguousListTokenConflictResolverFree += ambiguousListTokenConflictResolverRecycledCount;
        }
        if (ambiguousListTokenConflictResolverAverageUse != 0) ambiguousListTokenConflictResolverAverageUse = (ambiguousListTokenConflictResolverAverageUse + ambiguousListTokenConflictResolverEffectivellyUsed) / 2; else ambiguousListTokenConflictResolverAverageUse = ambiguousListTokenConflictResolverEffectivellyUsed;
        for (int i = 0; i < ambiguousListTokenConflictResolverRecycled.length; i++) {
            ambiguousListTokenConflictResolverRecycled[i] = null;
        }
        neededLength = unresolvedListTokenConflictResolverFree + unresolvedListTokenConflictResolverRecycledCount;
        if (neededLength > unresolvedListTokenConflictResolver.length) {
            if (neededLength > unresolvedListTokenConflictResolverMaxSize) {
                neededLength = unresolvedListTokenConflictResolverMaxSize;
                UnresolvedListTokenConflictResolver[] increased = new UnresolvedListTokenConflictResolver[neededLength];
                System.arraycopy(unresolvedListTokenConflictResolver, 0, increased, 0, unresolvedListTokenConflictResolver.length);
                unresolvedListTokenConflictResolver = increased;
                System.arraycopy(unresolvedListTokenConflictResolverRecycled, 0, unresolvedListTokenConflictResolver, unresolvedListTokenConflictResolverFree, unresolvedListTokenConflictResolverMaxSize - unresolvedListTokenConflictResolverFree);
                unresolvedListTokenConflictResolverFree = unresolvedListTokenConflictResolverMaxSize;
            } else {
                UnresolvedListTokenConflictResolver[] increased = new UnresolvedListTokenConflictResolver[neededLength];
                System.arraycopy(unresolvedListTokenConflictResolver, 0, increased, 0, unresolvedListTokenConflictResolver.length);
                unresolvedListTokenConflictResolver = increased;
                System.arraycopy(unresolvedListTokenConflictResolverRecycled, 0, unresolvedListTokenConflictResolver, unresolvedListTokenConflictResolverFree, unresolvedListTokenConflictResolverRecycledCount);
                unresolvedListTokenConflictResolverFree += unresolvedListTokenConflictResolverRecycledCount;
            }
        } else {
            System.arraycopy(unresolvedListTokenConflictResolverRecycled, 0, unresolvedListTokenConflictResolver, unresolvedListTokenConflictResolverFree, unresolvedListTokenConflictResolverRecycledCount);
            unresolvedListTokenConflictResolverFree += unresolvedListTokenConflictResolverRecycledCount;
        }
        if (unresolvedListTokenConflictResolverAverageUse != 0) unresolvedListTokenConflictResolverAverageUse = (unresolvedListTokenConflictResolverAverageUse + unresolvedListTokenConflictResolverEffectivellyUsed) / 2; else unresolvedListTokenConflictResolverAverageUse = unresolvedListTokenConflictResolverEffectivellyUsed;
        for (int i = 0; i < unresolvedListTokenConflictResolverRecycled.length; i++) {
            unresolvedListTokenConflictResolverRecycled[i] = null;
        }
        neededLength = boundAmbiguousElementConflictResolverFree + boundAmbiguousElementConflictResolverRecycledCount;
        if (neededLength > boundAmbiguousElementConflictResolver.length) {
            if (neededLength > boundAmbiguousElementConflictResolverMaxSize) {
                neededLength = boundAmbiguousElementConflictResolverMaxSize;
                BoundAmbiguousElementConflictResolver[] increased = new BoundAmbiguousElementConflictResolver[neededLength];
                System.arraycopy(boundAmbiguousElementConflictResolver, 0, increased, 0, boundAmbiguousElementConflictResolver.length);
                boundAmbiguousElementConflictResolver = increased;
                System.arraycopy(boundAmbiguousElementConflictResolverRecycled, 0, boundAmbiguousElementConflictResolver, boundAmbiguousElementConflictResolverFree, boundAmbiguousElementConflictResolverMaxSize - boundAmbiguousElementConflictResolverFree);
                boundAmbiguousElementConflictResolverFree = boundAmbiguousElementConflictResolverMaxSize;
            } else {
                BoundAmbiguousElementConflictResolver[] increased = new BoundAmbiguousElementConflictResolver[neededLength];
                System.arraycopy(boundAmbiguousElementConflictResolver, 0, increased, 0, boundAmbiguousElementConflictResolver.length);
                boundAmbiguousElementConflictResolver = increased;
                System.arraycopy(boundAmbiguousElementConflictResolverRecycled, 0, boundAmbiguousElementConflictResolver, boundAmbiguousElementConflictResolverFree, boundAmbiguousElementConflictResolverRecycledCount);
                boundAmbiguousElementConflictResolverFree += boundAmbiguousElementConflictResolverRecycledCount;
            }
        } else {
            System.arraycopy(boundAmbiguousElementConflictResolverRecycled, 0, boundAmbiguousElementConflictResolver, boundAmbiguousElementConflictResolverFree, boundAmbiguousElementConflictResolverRecycledCount);
            boundAmbiguousElementConflictResolverFree += boundAmbiguousElementConflictResolverRecycledCount;
        }
        if (boundAmbiguousElementConflictResolverAverageUse != 0) boundAmbiguousElementConflictResolverAverageUse = (boundAmbiguousElementConflictResolverAverageUse + boundAmbiguousElementConflictResolverEffectivellyUsed) / 2; else boundAmbiguousElementConflictResolverAverageUse = boundAmbiguousElementConflictResolverEffectivellyUsed;
        for (int i = 0; i < boundAmbiguousElementConflictResolverRecycled.length; i++) {
            boundAmbiguousElementConflictResolverRecycled[i] = null;
        }
        neededLength = boundUnresolvedElementConflictResolverFree + boundUnresolvedElementConflictResolverRecycledCount;
        if (neededLength > boundUnresolvedElementConflictResolver.length) {
            if (neededLength > boundUnresolvedElementConflictResolverMaxSize) {
                neededLength = boundUnresolvedElementConflictResolverMaxSize;
                BoundUnresolvedElementConflictResolver[] increased = new BoundUnresolvedElementConflictResolver[neededLength];
                System.arraycopy(boundUnresolvedElementConflictResolver, 0, increased, 0, boundUnresolvedElementConflictResolver.length);
                boundUnresolvedElementConflictResolver = increased;
                System.arraycopy(boundUnresolvedElementConflictResolverRecycled, 0, boundUnresolvedElementConflictResolver, boundUnresolvedElementConflictResolverFree, boundUnresolvedElementConflictResolverMaxSize - boundUnresolvedElementConflictResolverFree);
                boundUnresolvedElementConflictResolverFree = boundUnresolvedElementConflictResolverMaxSize;
            } else {
                BoundUnresolvedElementConflictResolver[] increased = new BoundUnresolvedElementConflictResolver[neededLength];
                System.arraycopy(boundUnresolvedElementConflictResolver, 0, increased, 0, boundUnresolvedElementConflictResolver.length);
                boundUnresolvedElementConflictResolver = increased;
                System.arraycopy(boundUnresolvedElementConflictResolverRecycled, 0, boundUnresolvedElementConflictResolver, boundUnresolvedElementConflictResolverFree, boundUnresolvedElementConflictResolverRecycledCount);
                boundUnresolvedElementConflictResolverFree += boundUnresolvedElementConflictResolverRecycledCount;
            }
        } else {
            System.arraycopy(boundUnresolvedElementConflictResolverRecycled, 0, boundUnresolvedElementConflictResolver, boundUnresolvedElementConflictResolverFree, boundUnresolvedElementConflictResolverRecycledCount);
            boundUnresolvedElementConflictResolverFree += boundUnresolvedElementConflictResolverRecycledCount;
        }
        if (boundUnresolvedElementConflictResolverAverageUse != 0) boundUnresolvedElementConflictResolverAverageUse = (boundUnresolvedElementConflictResolverAverageUse + boundUnresolvedElementConflictResolverEffectivellyUsed) / 2; else boundUnresolvedElementConflictResolverAverageUse = boundUnresolvedElementConflictResolverEffectivellyUsed;
        for (int i = 0; i < boundUnresolvedElementConflictResolverRecycled.length; i++) {
            boundUnresolvedElementConflictResolverRecycled[i] = null;
        }
        neededLength = boundAmbiguousAttributeConflictResolverFree + boundAmbiguousAttributeConflictResolverRecycledCount;
        if (neededLength > boundAmbiguousAttributeConflictResolver.length) {
            if (neededLength > boundAmbiguousAttributeConflictResolverMaxSize) {
                neededLength = boundAmbiguousAttributeConflictResolverMaxSize;
                BoundAmbiguousAttributeConflictResolver[] increased = new BoundAmbiguousAttributeConflictResolver[neededLength];
                System.arraycopy(boundAmbiguousAttributeConflictResolver, 0, increased, 0, boundAmbiguousAttributeConflictResolver.length);
                boundAmbiguousAttributeConflictResolver = increased;
                System.arraycopy(boundAmbiguousAttributeConflictResolverRecycled, 0, boundAmbiguousAttributeConflictResolver, boundAmbiguousAttributeConflictResolverFree, boundAmbiguousAttributeConflictResolverMaxSize - boundAmbiguousAttributeConflictResolverFree);
                boundAmbiguousAttributeConflictResolverFree = boundAmbiguousAttributeConflictResolverMaxSize;
            } else {
                BoundAmbiguousAttributeConflictResolver[] increased = new BoundAmbiguousAttributeConflictResolver[neededLength];
                System.arraycopy(boundAmbiguousAttributeConflictResolver, 0, increased, 0, boundAmbiguousAttributeConflictResolver.length);
                boundAmbiguousAttributeConflictResolver = increased;
                System.arraycopy(boundAmbiguousAttributeConflictResolverRecycled, 0, boundAmbiguousAttributeConflictResolver, boundAmbiguousAttributeConflictResolverFree, boundAmbiguousAttributeConflictResolverRecycledCount);
                boundAmbiguousAttributeConflictResolverFree += boundAmbiguousAttributeConflictResolverRecycledCount;
            }
        } else {
            System.arraycopy(boundAmbiguousAttributeConflictResolverRecycled, 0, boundAmbiguousAttributeConflictResolver, boundAmbiguousAttributeConflictResolverFree, boundAmbiguousAttributeConflictResolverRecycledCount);
            boundAmbiguousAttributeConflictResolverFree += boundAmbiguousAttributeConflictResolverRecycledCount;
        }
        if (boundAmbiguousAttributeConflictResolverAverageUse != 0) boundAmbiguousAttributeConflictResolverAverageUse = (boundAmbiguousAttributeConflictResolverAverageUse + boundAmbiguousAttributeConflictResolverEffectivellyUsed) / 2; else boundAmbiguousAttributeConflictResolverAverageUse = boundAmbiguousAttributeConflictResolverEffectivellyUsed;
        for (int i = 0; i < boundAmbiguousAttributeConflictResolverRecycled.length; i++) {
            boundAmbiguousAttributeConflictResolverRecycled[i] = null;
        }
        neededLength = boundUnresolvedAttributeConflictResolverFree + boundUnresolvedAttributeConflictResolverRecycledCount;
        if (neededLength > boundUnresolvedAttributeConflictResolver.length) {
            if (neededLength > boundUnresolvedAttributeConflictResolverMaxSize) {
                neededLength = boundUnresolvedAttributeConflictResolverMaxSize;
                BoundUnresolvedAttributeConflictResolver[] increased = new BoundUnresolvedAttributeConflictResolver[neededLength];
                System.arraycopy(boundUnresolvedAttributeConflictResolver, 0, increased, 0, boundUnresolvedAttributeConflictResolver.length);
                boundUnresolvedAttributeConflictResolver = increased;
                System.arraycopy(boundUnresolvedAttributeConflictResolverRecycled, 0, boundUnresolvedAttributeConflictResolver, boundUnresolvedAttributeConflictResolverFree, boundUnresolvedAttributeConflictResolverMaxSize - boundUnresolvedAttributeConflictResolverFree);
                boundUnresolvedAttributeConflictResolverFree = boundUnresolvedAttributeConflictResolverMaxSize;
            } else {
                BoundUnresolvedAttributeConflictResolver[] increased = new BoundUnresolvedAttributeConflictResolver[neededLength];
                System.arraycopy(boundUnresolvedAttributeConflictResolver, 0, increased, 0, boundUnresolvedAttributeConflictResolver.length);
                boundUnresolvedAttributeConflictResolver = increased;
                System.arraycopy(boundUnresolvedAttributeConflictResolverRecycled, 0, boundUnresolvedAttributeConflictResolver, boundUnresolvedAttributeConflictResolverFree, boundUnresolvedAttributeConflictResolverRecycledCount);
                boundUnresolvedAttributeConflictResolverFree += boundUnresolvedAttributeConflictResolverRecycledCount;
            }
        } else {
            System.arraycopy(boundUnresolvedAttributeConflictResolverRecycled, 0, boundUnresolvedAttributeConflictResolver, boundUnresolvedAttributeConflictResolverFree, boundUnresolvedAttributeConflictResolverRecycledCount);
            boundUnresolvedAttributeConflictResolverFree += boundUnresolvedAttributeConflictResolverRecycledCount;
        }
        if (boundUnresolvedAttributeConflictResolverAverageUse != 0) boundUnresolvedAttributeConflictResolverAverageUse = (boundUnresolvedAttributeConflictResolverAverageUse + boundUnresolvedAttributeConflictResolverEffectivellyUsed) / 2; else boundUnresolvedAttributeConflictResolverAverageUse = boundUnresolvedAttributeConflictResolverEffectivellyUsed;
        for (int i = 0; i < boundUnresolvedAttributeConflictResolverRecycled.length; i++) {
            boundUnresolvedAttributeConflictResolverRecycled[i] = null;
        }
    }

    public synchronized ActiveModelConflictHandlerPool getActiveModelConflictHandlerPool() {
        if (amchPoolFree == 0) {
            ActiveModelConflictHandlerPool amchp = new ActiveModelConflictHandlerPool(instance);
            return amchp;
        } else {
            ActiveModelConflictHandlerPool amchp = amchPools[--amchPoolFree];
            return amchp;
        }
    }

    public synchronized void recycle(ActiveModelConflictHandlerPool amchp) {
        if (amchPoolFree == amchPoolMaxSize) return;
        if (amchPoolFree == amchPools.length) {
            ActiveModelConflictHandlerPool[] increased = new ActiveModelConflictHandlerPool[5 + amchPools.length];
            System.arraycopy(amchPools, 0, increased, 0, amchPoolFree);
            amchPools = increased;
        }
        amchPools[amchPoolFree++] = amchp;
    }
}
