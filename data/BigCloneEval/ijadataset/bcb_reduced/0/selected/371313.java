package serene.validation.handlers.error;

public class SynchronizedErrorHandlerPool extends ErrorHandlerPool {

    private static volatile SynchronizedErrorHandlerPool instance;

    int vehPoolFree;

    int vehPoolMaxSize;

    ValidatorErrorHandlerPool[] vehPools;

    int validationErrorHAverageUse;

    int validationErrorHMaxSize;

    int validationErrorHFree;

    ValidationErrorHandler[] validationErrorH;

    int conflictErrorHAverageUse;

    int conflictErrorHMaxSize;

    int conflictErrorHFree;

    ExternalConflictErrorHandler[] conflictErrorH;

    int commonErrorHAverageUse;

    int commonErrorHMaxSize;

    int commonErrorHFree;

    CommonErrorHandler[] commonErrorH;

    int defaultErrorHAverageUse;

    int defaultErrorHMaxSize;

    int defaultErrorHFree;

    DefaultErrorHandler[] defaultErrorH;

    int startErrorHAverageUse;

    int startErrorHMaxSize;

    int startErrorHFree;

    StartErrorHandler[] startErrorH;

    SynchronizedErrorHandlerPool() {
        super();
        vehPoolFree = 0;
        vehPools = new ValidatorErrorHandlerPool[10];
        validationErrorHAverageUse = 0;
        validationErrorHFree = 0;
        validationErrorH = new ValidationErrorHandler[10];
        conflictErrorHAverageUse = 0;
        conflictErrorHFree = 0;
        conflictErrorH = new ExternalConflictErrorHandler[10];
        commonErrorHAverageUse = 0;
        commonErrorHFree = 0;
        commonErrorH = new CommonErrorHandler[10];
        defaultErrorHAverageUse = 0;
        defaultErrorHFree = 0;
        defaultErrorH = new DefaultErrorHandler[10];
        startErrorHAverageUse = 0;
        startErrorHFree = 0;
        startErrorH = new StartErrorHandler[10];
        validationErrorHMaxSize = 40;
        conflictErrorHMaxSize = 20;
        commonErrorHMaxSize = 20;
        defaultErrorHMaxSize = 20;
        startErrorHMaxSize = 10;
    }

    public static SynchronizedErrorHandlerPool getInstance() {
        if (instance == null) {
            synchronized (ErrorHandlerPool.class) {
                if (instance == null) {
                    instance = new SynchronizedErrorHandlerPool();
                }
            }
        }
        return instance;
    }

    public synchronized ValidatorErrorHandlerPool getValidatorErrorHandlerPool() {
        if (vehPoolFree == 0) {
            ValidatorErrorHandlerPool vehp = new ValidatorErrorHandlerPool(this);
            return vehp;
        } else {
            ValidatorErrorHandlerPool vehp = vehPools[--vehPoolFree];
            return vehp;
        }
    }

    public synchronized void recycle(ValidatorErrorHandlerPool vehp) {
        if (vehPoolFree == vehPoolMaxSize) return;
        if (vehPoolFree == vehPools.length) {
            ValidatorErrorHandlerPool[] increased = new ValidatorErrorHandlerPool[10 + vehPools.length];
            System.arraycopy(vehPools, 0, increased, 0, vehPoolFree);
            vehPools = increased;
        }
        vehPools[vehPoolFree++] = vehp;
    }

    synchronized void fill(ValidatorErrorHandlerPool pool, ValidationErrorHandler[] validationErrorHToFill, ExternalConflictErrorHandler[] conflictErrorHToFill, CommonErrorHandler[] commonErrorHToFill, DefaultErrorHandler[] defaultErrorHToFill, StartErrorHandler[] startErrorHToFill) {
        int validationErrorHFillCount;
        if (validationErrorHToFill == null || validationErrorHToFill.length < validationErrorHAverageUse) {
            validationErrorHToFill = new ValidationErrorHandler[validationErrorHAverageUse];
            pool.validationErrorH = validationErrorHToFill;
        }
        if (validationErrorHFree > validationErrorHAverageUse) {
            validationErrorHFillCount = validationErrorHAverageUse;
            validationErrorHFree = validationErrorHFree - validationErrorHAverageUse;
        } else {
            validationErrorHFillCount = validationErrorHFree;
            validationErrorHFree = 0;
        }
        System.arraycopy(validationErrorH, validationErrorHFree, validationErrorHToFill, 0, validationErrorHFillCount);
        int conflictErrorHFillCount;
        if (conflictErrorHToFill == null || conflictErrorHToFill.length < conflictErrorHAverageUse) {
            conflictErrorHToFill = new ExternalConflictErrorHandler[conflictErrorHAverageUse];
            pool.conflictErrorH = conflictErrorHToFill;
        }
        if (conflictErrorHFree > conflictErrorHAverageUse) {
            conflictErrorHFillCount = conflictErrorHAverageUse;
            conflictErrorHFree = conflictErrorHFree - conflictErrorHAverageUse;
        } else {
            conflictErrorHFillCount = conflictErrorHFree;
            conflictErrorHFree = 0;
        }
        System.arraycopy(conflictErrorH, conflictErrorHFree, conflictErrorHToFill, 0, conflictErrorHFillCount);
        int commonErrorHFillCount;
        if (commonErrorHToFill == null || commonErrorHToFill.length < commonErrorHAverageUse) {
            commonErrorHToFill = new CommonErrorHandler[commonErrorHAverageUse];
            pool.commonErrorH = commonErrorHToFill;
        }
        if (commonErrorHFree > commonErrorHAverageUse) {
            commonErrorHFillCount = commonErrorHAverageUse;
            commonErrorHFree = commonErrorHFree - commonErrorHAverageUse;
        } else {
            commonErrorHFillCount = commonErrorHFree;
            commonErrorHFree = 0;
        }
        System.arraycopy(commonErrorH, commonErrorHFree, commonErrorHToFill, 0, commonErrorHFillCount);
        int defaultErrorHFillCount;
        if (defaultErrorHToFill == null || defaultErrorHToFill.length < defaultErrorHAverageUse) {
            defaultErrorHToFill = new DefaultErrorHandler[defaultErrorHAverageUse];
            pool.defaultErrorH = defaultErrorHToFill;
        }
        if (defaultErrorHFree > defaultErrorHAverageUse) {
            defaultErrorHFillCount = defaultErrorHAverageUse;
            defaultErrorHFree = defaultErrorHFree - defaultErrorHAverageUse;
        } else {
            defaultErrorHFillCount = defaultErrorHFree;
            defaultErrorHFree = 0;
        }
        System.arraycopy(defaultErrorH, defaultErrorHFree, defaultErrorHToFill, 0, defaultErrorHFillCount);
        int startErrorHFillCount;
        if (startErrorHToFill == null || startErrorHToFill.length < startErrorHAverageUse) {
            startErrorHToFill = new StartErrorHandler[startErrorHAverageUse];
            pool.startErrorH = startErrorHToFill;
        }
        if (startErrorHFree > startErrorHAverageUse) {
            startErrorHFillCount = startErrorHAverageUse;
            startErrorHFree = startErrorHFree - startErrorHAverageUse;
        } else {
            startErrorHFillCount = startErrorHFree;
            startErrorHFree = 0;
        }
        System.arraycopy(startErrorH, startErrorHFree, startErrorHToFill, 0, startErrorHFillCount);
        pool.initFilled(validationErrorHFillCount, conflictErrorHFillCount, commonErrorHFillCount, defaultErrorHFillCount, startErrorHFillCount);
    }

    synchronized void recycle(int validationErrorHRecycledCount, int validationErrorHEffectivellyUsed, ValidationErrorHandler[] validationErrorHRecycled, int conflictErrorHRecycledCount, int conflictErrorHEffectivellyUsed, ExternalConflictErrorHandler[] conflictErrorHRecycled, int commonErrorHRecycledCount, int commonErrorHEffectivellyUsed, CommonErrorHandler[] commonErrorHRecycled, int defaultErrorHRecycledCount, int defaultErrorHEffectivellyUsed, DefaultErrorHandler[] defaultErrorHRecycled, int startErrorHRecycledCount, int startErrorHEffectivellyUsed, StartErrorHandler[] startErrorHRecycled) {
        int neededLength = validationErrorHFree + validationErrorHRecycledCount;
        if (neededLength > validationErrorH.length) {
            if (neededLength > validationErrorHMaxSize) {
                neededLength = validationErrorHMaxSize;
                ValidationErrorHandler[] increased = new ValidationErrorHandler[neededLength];
                System.arraycopy(validationErrorH, 0, increased, 0, validationErrorH.length);
                validationErrorH = increased;
                System.arraycopy(validationErrorHRecycled, 0, validationErrorH, validationErrorHFree, validationErrorHMaxSize - validationErrorHFree);
                validationErrorHFree = validationErrorHMaxSize;
            } else {
                ValidationErrorHandler[] increased = new ValidationErrorHandler[neededLength];
                System.arraycopy(validationErrorH, 0, increased, 0, validationErrorH.length);
                validationErrorH = increased;
                System.arraycopy(validationErrorHRecycled, 0, validationErrorH, validationErrorHFree, validationErrorHRecycledCount);
                validationErrorHFree += validationErrorHRecycledCount;
            }
        } else {
            System.arraycopy(validationErrorHRecycled, 0, validationErrorH, validationErrorHFree, validationErrorHRecycledCount);
            validationErrorHFree += validationErrorHRecycledCount;
        }
        if (validationErrorHAverageUse != 0) validationErrorHAverageUse = (validationErrorHAverageUse + validationErrorHEffectivellyUsed) / 2; else validationErrorHAverageUse = validationErrorHEffectivellyUsed;
        for (int i = 0; i < validationErrorHRecycled.length; i++) {
            validationErrorHRecycled[i] = null;
        }
        neededLength = commonErrorHFree + commonErrorHRecycledCount;
        if (neededLength > commonErrorH.length) {
            if (neededLength > commonErrorHMaxSize) {
                neededLength = commonErrorHMaxSize;
                CommonErrorHandler[] increased = new CommonErrorHandler[neededLength];
                System.arraycopy(commonErrorH, 0, increased, 0, commonErrorH.length);
                commonErrorH = increased;
                System.arraycopy(commonErrorHRecycled, 0, commonErrorH, commonErrorHFree, commonErrorHMaxSize - commonErrorHFree);
                commonErrorHFree = commonErrorHMaxSize;
            } else {
                CommonErrorHandler[] increased = new CommonErrorHandler[neededLength];
                System.arraycopy(commonErrorH, 0, increased, 0, commonErrorH.length);
                commonErrorH = increased;
                System.arraycopy(commonErrorHRecycled, 0, commonErrorH, commonErrorHFree, commonErrorHRecycledCount);
                commonErrorHFree += commonErrorHRecycledCount;
            }
        } else {
            System.arraycopy(commonErrorHRecycled, 0, commonErrorH, commonErrorHFree, commonErrorHRecycledCount);
            commonErrorHFree += commonErrorHRecycledCount;
        }
        if (commonErrorHAverageUse != 0) commonErrorHAverageUse = (commonErrorHAverageUse + commonErrorHEffectivellyUsed) / 2; else commonErrorHAverageUse = commonErrorHEffectivellyUsed;
        for (int i = 0; i < commonErrorHRecycled.length; i++) {
            commonErrorHRecycled[i] = null;
        }
        neededLength = conflictErrorHFree + conflictErrorHRecycledCount;
        if (neededLength > conflictErrorH.length) {
            if (neededLength > conflictErrorHMaxSize) {
                neededLength = conflictErrorHMaxSize;
                ExternalConflictErrorHandler[] increased = new ExternalConflictErrorHandler[neededLength];
                System.arraycopy(conflictErrorH, 0, increased, 0, conflictErrorH.length);
                conflictErrorH = increased;
                System.arraycopy(conflictErrorHRecycled, 0, conflictErrorH, conflictErrorHFree, conflictErrorHMaxSize - conflictErrorHFree);
                conflictErrorHFree = conflictErrorHMaxSize;
            } else {
                ExternalConflictErrorHandler[] increased = new ExternalConflictErrorHandler[neededLength];
                System.arraycopy(conflictErrorH, 0, increased, 0, conflictErrorH.length);
                conflictErrorH = increased;
                System.arraycopy(conflictErrorHRecycled, 0, conflictErrorH, conflictErrorHFree, conflictErrorHRecycledCount);
                conflictErrorHFree += conflictErrorHRecycledCount;
            }
        } else {
            System.arraycopy(conflictErrorHRecycled, 0, conflictErrorH, conflictErrorHFree, conflictErrorHRecycledCount);
            conflictErrorHFree += conflictErrorHRecycledCount;
        }
        if (conflictErrorHAverageUse != 0) conflictErrorHAverageUse = (conflictErrorHAverageUse + conflictErrorHEffectivellyUsed) / 2; else conflictErrorHAverageUse = conflictErrorHEffectivellyUsed;
        for (int i = 0; i < conflictErrorHRecycled.length; i++) {
            conflictErrorHRecycled[i] = null;
        }
        neededLength = defaultErrorHFree + defaultErrorHRecycledCount;
        if (neededLength > defaultErrorH.length) {
            if (neededLength > defaultErrorHMaxSize) {
                neededLength = defaultErrorHMaxSize;
                DefaultErrorHandler[] increased = new DefaultErrorHandler[neededLength];
                System.arraycopy(defaultErrorH, 0, increased, 0, defaultErrorH.length);
                defaultErrorH = increased;
                System.arraycopy(defaultErrorHRecycled, 0, defaultErrorH, defaultErrorHFree, defaultErrorHMaxSize - defaultErrorHFree);
                defaultErrorHFree = defaultErrorHMaxSize;
            } else {
                DefaultErrorHandler[] increased = new DefaultErrorHandler[neededLength];
                System.arraycopy(defaultErrorH, 0, increased, 0, defaultErrorH.length);
                defaultErrorH = increased;
                System.arraycopy(defaultErrorHRecycled, 0, defaultErrorH, defaultErrorHFree, defaultErrorHRecycledCount);
                defaultErrorHFree += defaultErrorHRecycledCount;
            }
        } else {
            System.arraycopy(defaultErrorHRecycled, 0, defaultErrorH, defaultErrorHFree, defaultErrorHRecycledCount);
            defaultErrorHFree += defaultErrorHRecycledCount;
        }
        if (defaultErrorHAverageUse != 0) defaultErrorHAverageUse = (defaultErrorHAverageUse + defaultErrorHEffectivellyUsed) / 2; else defaultErrorHAverageUse = defaultErrorHEffectivellyUsed;
        for (int i = 0; i < defaultErrorHRecycled.length; i++) {
            defaultErrorHRecycled[i] = null;
        }
        neededLength = startErrorHFree + startErrorHRecycledCount;
        if (neededLength > startErrorH.length) {
            if (neededLength > startErrorHMaxSize) {
                neededLength = startErrorHMaxSize;
                StartErrorHandler[] increased = new StartErrorHandler[neededLength];
                System.arraycopy(startErrorH, 0, increased, 0, startErrorH.length);
                startErrorH = increased;
                System.arraycopy(startErrorHRecycled, 0, startErrorH, startErrorHFree, startErrorHMaxSize - startErrorHFree);
                startErrorHFree = startErrorHMaxSize;
            } else {
                StartErrorHandler[] increased = new StartErrorHandler[neededLength];
                System.arraycopy(startErrorH, 0, increased, 0, startErrorH.length);
                startErrorH = increased;
                System.arraycopy(startErrorHRecycled, 0, startErrorH, startErrorHFree, startErrorHRecycledCount);
                startErrorHFree += startErrorHRecycledCount;
            }
        } else {
            System.arraycopy(startErrorHRecycled, 0, startErrorH, startErrorHFree, startErrorHRecycledCount);
            startErrorHFree += startErrorHRecycledCount;
        }
        if (startErrorHAverageUse != 0) startErrorHAverageUse = (startErrorHAverageUse + startErrorHEffectivellyUsed) / 2; else startErrorHAverageUse = startErrorHEffectivellyUsed;
        for (int i = 0; i < startErrorHRecycled.length; i++) {
            startErrorHRecycled[i] = null;
        }
    }
}
