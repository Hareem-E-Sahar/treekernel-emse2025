package serene.validation.handlers.content.impl;

import java.util.Arrays;
import javax.xml.validation.ValidatorHandler;
import serene.bind.BindingPool;
import serene.validation.schema.active.ActiveModelPool;

public class SynchronizedContentHandlerPool extends ContentHandlerPool {

    private static volatile SynchronizedContentHandlerPool instance;

    int eventHandlerPoolFree;

    int eventHandlerPoolMaxSize;

    ValidatorEventHandlerPool[] eventHandlerPools;

    int elementVHAverageUse;

    int elementVHMaxSize;

    int elementVHFree;

    ElementValidationHandler[] elementVH;

    int startVHAverageUse;

    int startVHMaxSize;

    int startVHFree;

    StartValidationHandler[] startVH;

    int unexpectedElementHAverageUse;

    int unexpectedElementHMaxSize;

    int unexpectedElementHFree;

    UnexpectedElementHandler[] unexpectedElementH;

    int unexpectedAmbiguousEHAverageUse;

    int unexpectedAmbiguousEHMaxSize;

    int unexpectedAmbiguousEHFree;

    UnexpectedAmbiguousElementHandler[] unexpectedAmbiguousEH;

    int unknownElementHAverageUse;

    int unknownElementHMaxSize;

    int unknownElementHFree;

    UnknownElementHandler[] unknownElementH;

    int elementDefaultHAverageUse;

    int elementDefaultHMaxSize;

    int elementDefaultHFree;

    ElementDefaultHandler[] elementDefaultH;

    int boundUnexpectedElementHAverageUse;

    int boundUnexpectedElementHMaxSize;

    int boundUnexpectedElementHFree;

    BoundUnexpectedElementHandler[] boundUnexpectedElementH;

    int boundUnexpectedAmbiguousEHAverageUse;

    int boundUnexpectedAmbiguousEHMaxSize;

    int boundUnexpectedAmbiguousEHFree;

    BoundUnexpectedAmbiguousElementHandler[] boundUnexpectedAmbiguousEH;

    int boundUnknownElementHAverageUse;

    int boundUnknownElementHMaxSize;

    int boundUnknownElementHFree;

    BoundUnknownElementHandler[] boundUnknownElementH;

    int boundElementDefaultHAverageUse;

    int boundElementDefaultHMaxSize;

    int boundElementDefaultHFree;

    BoundElementDefaultHandler[] boundElementDefaultH;

    int elementConcurrentHAverageUse;

    int elementConcurrentHMaxSize;

    int elementConcurrentHFree;

    ElementConcurrentHandler[] elementConcurrentH;

    int elementParallelHAverageUse;

    int elementParallelHMaxSize;

    int elementParallelHFree;

    ElementParallelHandler[] elementParallelH;

    int elementCommonHAverageUse;

    int elementCommonHMaxSize;

    int elementCommonHFree;

    ElementCommonHandler[] elementCommonH;

    int unexpectedAttributeHAverageUse;

    int unexpectedAttributeHMaxSize;

    int unexpectedAttributeHFree;

    UnexpectedAttributeHandler[] unexpectedAttributeH;

    int unexpectedAmbiguousAHAverageUse;

    int unexpectedAmbiguousAHMaxSize;

    int unexpectedAmbiguousAHFree;

    UnexpectedAmbiguousAttributeHandler[] unexpectedAmbiguousAH;

    int unknownAttributeHAverageUse;

    int unknownAttributeHMaxSize;

    int unknownAttributeHFree;

    UnknownAttributeHandler[] unknownAttributeH;

    int attributeVHAverageUse;

    int attributeVHMaxSize;

    int attributeVHFree;

    AttributeValidationHandler[] attributeVH;

    int candidateAttributeVHAverageUse;

    int candidateAttributeVHMaxSize;

    int candidateAttributeVHFree;

    CandidateAttributeValidationHandler[] candidateAttributeVH;

    int attributeConcurrentHAverageUse;

    int attributeConcurrentHMaxSize;

    int attributeConcurrentHFree;

    AttributeConcurrentHandler[] attributeConcurrentH;

    int attributeParallelHAverageUse;

    int attributeParallelHMaxSize;

    int attributeParallelHFree;

    AttributeParallelHandler[] attributeParallelH;

    int attributeDefaultHAverageUse;

    int attributeDefaultHMaxSize;

    int attributeDefaultHFree;

    AttributeDefaultHandler[] attributeDefaultH;

    int charactersValidationHAverageUse;

    int charactersValidationHMaxSize;

    int charactersValidationHFree;

    CharactersValidationHandler[] charactersValidationH;

    int structuredDataValidationHAverageUse;

    int structuredDataValidationHMaxSize;

    int structuredDataValidationHFree;

    StructuredDataValidationHandler[] structuredDataValidationH;

    int dataValidationHAverageUse;

    int dataValidationHMaxSize;

    int dataValidationHFree;

    DataValidationHandler[] dataValidationH;

    int defaultVAttributeHAverageUse;

    int defaultVAttributeHMaxSize;

    int defaultVAttributeHFree;

    DefaultValueAttributeValidationHandler[] defaultVAttributeH;

    int listPatternVHAverageUse;

    int listPatternVHMaxSize;

    int listPatternVHFree;

    ListPatternValidationHandler[] listPatternVH;

    int exceptPatternVHAverageUse;

    int exceptPatternVHMaxSize;

    int exceptPatternVHFree;

    ExceptPatternValidationHandler[] exceptPatternVH;

    int boundElementVHAverageUse;

    int boundElementVHMaxSize;

    int boundElementVHFree;

    BoundElementValidationHandler[] boundElementVH;

    int boundStartVHAverageUse;

    int boundStartVHMaxSize;

    int boundStartVHFree;

    BoundStartValidationHandler[] boundStartVH;

    int boundElementConcurrentHAverageUse;

    int boundElementConcurrentHMaxSize;

    int boundElementConcurrentHFree;

    BoundElementConcurrentHandler[] boundElementConcurrentH;

    int boundElementParallelHAverageUse;

    int boundElementParallelHMaxSize;

    int boundElementParallelHFree;

    BoundElementParallelHandler[] boundElementParallelH;

    int boundElementCommonHAverageUse;

    int boundElementCommonHMaxSize;

    int boundElementCommonHFree;

    BoundElementCommonHandler[] boundElementCommonH;

    int boundAttributeVHAverageUse;

    int boundAttributeVHMaxSize;

    int boundAttributeVHFree;

    BoundAttributeValidationHandler[] boundAttributeVH;

    int boundCandidateAttributeVHAverageUse;

    int boundCandidateAttributeVHMaxSize;

    int boundCandidateAttributeVHFree;

    BoundCandidateAttributeValidationHandler[] boundCandidateAttributeVH;

    int boundAttributeConcurrentHAverageUse;

    int boundAttributeConcurrentHMaxSize;

    int boundAttributeConcurrentHFree;

    BoundAttributeConcurrentHandler[] boundAttributeConcurrentH;

    int boundAttributeParallelHAverageUse;

    int boundAttributeParallelHMaxSize;

    int boundAttributeParallelHFree;

    BoundAttributeParallelHandler[] boundAttributeParallelH;

    SynchronizedContentHandlerPool() {
        super();
        eventHandlerPoolFree = 0;
        eventHandlerPools = new ValidatorEventHandlerPool[10];
        elementVHAverageUse = 0;
        elementVHFree = 0;
        elementVH = new ElementValidationHandler[10];
        startVHAverageUse = 0;
        startVHFree = 0;
        startVH = new StartValidationHandler[10];
        unexpectedElementHAverageUse = 0;
        unexpectedElementHFree = 0;
        unexpectedElementH = new UnexpectedElementHandler[10];
        unexpectedAmbiguousEHAverageUse = 0;
        unexpectedAmbiguousEHFree = 0;
        unexpectedAmbiguousEH = new UnexpectedAmbiguousElementHandler[10];
        unknownElementHAverageUse = 0;
        unknownElementHFree = 0;
        unknownElementH = new UnknownElementHandler[10];
        elementDefaultHAverageUse = 0;
        elementDefaultHFree = 0;
        elementDefaultH = new ElementDefaultHandler[10];
        boundUnexpectedElementHAverageUse = 0;
        boundUnexpectedElementHFree = 0;
        boundUnexpectedElementH = new BoundUnexpectedElementHandler[10];
        boundUnexpectedAmbiguousEHAverageUse = 0;
        boundUnexpectedAmbiguousEHFree = 0;
        boundUnexpectedAmbiguousEH = new BoundUnexpectedAmbiguousElementHandler[10];
        boundUnknownElementHAverageUse = 0;
        boundUnknownElementHFree = 0;
        boundUnknownElementH = new BoundUnknownElementHandler[10];
        boundElementDefaultHAverageUse = 0;
        boundElementDefaultHFree = 0;
        boundElementDefaultH = new BoundElementDefaultHandler[10];
        elementConcurrentHAverageUse = 0;
        elementConcurrentHFree = 0;
        elementConcurrentH = new ElementConcurrentHandler[10];
        elementParallelHAverageUse = 0;
        elementParallelHFree = 0;
        elementParallelH = new ElementParallelHandler[10];
        elementCommonHAverageUse = 0;
        elementCommonHFree = 0;
        elementCommonH = new ElementCommonHandler[10];
        unexpectedAttributeHAverageUse = 0;
        unexpectedAttributeHFree = 0;
        unexpectedAttributeH = new UnexpectedAttributeHandler[10];
        unexpectedAmbiguousAHAverageUse = 0;
        unexpectedAmbiguousAHFree = 0;
        unexpectedAmbiguousAH = new UnexpectedAmbiguousAttributeHandler[10];
        unknownAttributeHAverageUse = 0;
        unknownAttributeHFree = 0;
        unknownAttributeH = new UnknownAttributeHandler[10];
        attributeVHAverageUse = 0;
        attributeVHFree = 0;
        attributeVH = new AttributeValidationHandler[10];
        candidateAttributeVHAverageUse = 0;
        candidateAttributeVHFree = 0;
        candidateAttributeVH = new CandidateAttributeValidationHandler[10];
        attributeConcurrentHAverageUse = 0;
        attributeConcurrentHFree = 0;
        attributeConcurrentH = new AttributeConcurrentHandler[10];
        attributeParallelHAverageUse = 0;
        attributeParallelHFree = 0;
        attributeParallelH = new AttributeParallelHandler[10];
        attributeDefaultHAverageUse = 0;
        attributeDefaultHFree = 0;
        attributeDefaultH = new AttributeDefaultHandler[10];
        charactersValidationHAverageUse = 0;
        charactersValidationHFree = 0;
        charactersValidationH = new CharactersValidationHandler[10];
        structuredDataValidationHAverageUse = 0;
        structuredDataValidationHFree = 0;
        structuredDataValidationH = new StructuredDataValidationHandler[10];
        dataValidationHAverageUse = 0;
        dataValidationHFree = 0;
        dataValidationH = new DataValidationHandler[10];
        defaultVAttributeHAverageUse = 0;
        defaultVAttributeHFree = 0;
        defaultVAttributeH = new DefaultValueAttributeValidationHandler[10];
        listPatternVHAverageUse = 0;
        listPatternVHFree = 0;
        listPatternVH = new ListPatternValidationHandler[10];
        exceptPatternVHAverageUse = 0;
        exceptPatternVHFree = 0;
        exceptPatternVH = new ExceptPatternValidationHandler[10];
        boundElementVHAverageUse = 0;
        boundElementVHFree = 0;
        boundElementVH = new BoundElementValidationHandler[10];
        boundStartVHAverageUse = 0;
        boundStartVHFree = 0;
        boundStartVH = new BoundStartValidationHandler[10];
        boundElementConcurrentHAverageUse = 0;
        boundElementConcurrentHFree = 0;
        boundElementConcurrentH = new BoundElementConcurrentHandler[10];
        boundElementParallelHAverageUse = 0;
        boundElementParallelHFree = 0;
        boundElementParallelH = new BoundElementParallelHandler[10];
        boundElementCommonHAverageUse = 0;
        boundElementCommonHFree = 0;
        boundElementCommonH = new BoundElementCommonHandler[10];
        boundAttributeVHAverageUse = 0;
        boundAttributeVHFree = 0;
        boundAttributeVH = new BoundAttributeValidationHandler[10];
        boundCandidateAttributeVHAverageUse = 0;
        boundCandidateAttributeVHFree = 0;
        boundCandidateAttributeVH = new BoundCandidateAttributeValidationHandler[10];
        boundAttributeConcurrentHAverageUse = 0;
        boundAttributeConcurrentHFree = 0;
        boundAttributeConcurrentH = new BoundAttributeConcurrentHandler[10];
        boundAttributeParallelHAverageUse = 0;
        boundAttributeParallelHFree = 0;
        boundAttributeParallelH = new BoundAttributeParallelHandler[10];
        elementVHMaxSize = 40;
        startVHMaxSize = 10;
        unexpectedElementHMaxSize = 20;
        unexpectedAmbiguousEHMaxSize = 20;
        unknownElementHMaxSize = 20;
        elementDefaultHMaxSize = 20;
        boundUnexpectedElementHMaxSize = 20;
        boundUnexpectedAmbiguousEHMaxSize = 20;
        boundUnknownElementHMaxSize = 20;
        boundElementDefaultHMaxSize = 20;
        elementConcurrentHMaxSize = 20;
        elementParallelHMaxSize = 20;
        elementCommonHMaxSize = 20;
        unexpectedAttributeHMaxSize = 20;
        unexpectedAmbiguousAHMaxSize = 20;
        unknownAttributeHMaxSize = 20;
        attributeVHMaxSize = 20;
        candidateAttributeVHMaxSize = 20;
        attributeConcurrentHMaxSize = 20;
        attributeParallelHMaxSize = 20;
        attributeDefaultHMaxSize = 20;
        charactersValidationHMaxSize = 20;
        structuredDataValidationHMaxSize = 20;
        dataValidationHMaxSize = 20;
        defaultVAttributeHMaxSize = 20;
        listPatternVHMaxSize = 20;
        exceptPatternVHMaxSize = 20;
        boundElementVHMaxSize = 40;
        boundStartVHMaxSize = 10;
        boundElementConcurrentHMaxSize = 20;
        boundElementParallelHMaxSize = 20;
        boundElementCommonHMaxSize = 20;
        boundAttributeVHMaxSize = 20;
        boundCandidateAttributeVHMaxSize = 20;
        boundAttributeConcurrentHMaxSize = 20;
        boundAttributeParallelHMaxSize = 20;
    }

    public static SynchronizedContentHandlerPool getInstance() {
        if (instance == null) {
            synchronized (ContentHandlerPool.class) {
                if (instance == null) {
                    instance = new SynchronizedContentHandlerPool();
                }
            }
        }
        return instance;
    }

    public synchronized ValidatorEventHandlerPool getValidatorEventHandlerPool() {
        if (eventHandlerPoolFree == 0) {
            ValidatorEventHandlerPool ehp = new ValidatorEventHandlerPool(this);
            return ehp;
        } else {
            ValidatorEventHandlerPool ehp = eventHandlerPools[--eventHandlerPoolFree];
            return ehp;
        }
    }

    public synchronized void recycle(ValidatorEventHandlerPool eventHandlerPool) {
        if (eventHandlerPoolFree == eventHandlerPoolMaxSize) return;
        if (eventHandlerPoolFree == eventHandlerPools.length) {
            ValidatorEventHandlerPool[] increased = new ValidatorEventHandlerPool[10 + eventHandlerPools.length];
            System.arraycopy(eventHandlerPools, 0, increased, 0, eventHandlerPoolFree);
            eventHandlerPools = increased;
        }
        eventHandlerPools[eventHandlerPoolFree++] = eventHandlerPool;
    }

    synchronized void fill(ValidatorEventHandlerPool eventHandlerPool, ElementValidationHandler[] elementVHToFill, StartValidationHandler[] startVHToFill, UnexpectedElementHandler[] unexpectedElementHToFill, UnexpectedAmbiguousElementHandler[] unexpectedAmbiguousEHToFill, UnknownElementHandler[] unknownElementHToFill, ElementDefaultHandler[] elementDefaultHToFill, BoundUnexpectedElementHandler[] boundUnexpectedElementHToFill, BoundUnexpectedAmbiguousElementHandler[] boundUnexpectedAmbiguousEHToFill, BoundUnknownElementHandler[] boundUnknownElementHToFill, BoundElementDefaultHandler[] boundElementDefaultHToFill, ElementConcurrentHandler[] elementConcurrentHToFill, ElementParallelHandler[] elementParallelHToFill, ElementCommonHandler[] elementCommonHToFill, UnexpectedAttributeHandler[] unexpectedAttributeHToFill, UnexpectedAmbiguousAttributeHandler[] unexpectedAmbiguousAHToFill, UnknownAttributeHandler[] unknownAttributeHToFill, AttributeValidationHandler[] attributeVHToFill, CandidateAttributeValidationHandler[] candidateAttributeVHToFill, AttributeConcurrentHandler[] attributeConcurrentHToFill, AttributeParallelHandler[] attributeParallelHToFill, AttributeDefaultHandler[] attributeDefaultHToFill, CharactersValidationHandler[] charactersValidationHToFill, StructuredDataValidationHandler[] structuredDataValidationHToFill, DataValidationHandler[] dataValidationHToFill, DefaultValueAttributeValidationHandler[] defaultVAttributeHToFill, ListPatternValidationHandler[] listPatternVHToFill, ExceptPatternValidationHandler[] exceptPatternVHToFill, BoundElementValidationHandler[] boundElementVHToFill, BoundStartValidationHandler[] boundStartVHToFill, BoundElementConcurrentHandler[] boundElementConcurrentHToFill, BoundElementParallelHandler[] boundElementParallelHToFill, BoundElementCommonHandler[] boundElementCommonHToFill, BoundAttributeValidationHandler[] boundAttributeVHToFill, BoundCandidateAttributeValidationHandler[] boundCandidateAttributeVHToFill, BoundAttributeConcurrentHandler[] boundAttributeConcurrentHToFill, BoundAttributeParallelHandler[] boundAttributeParallelHToFill) {
        int elementVHFillCount;
        if (elementVHToFill == null || elementVHToFill.length < elementVHAverageUse) {
            elementVHToFill = new ElementValidationHandler[elementVHAverageUse];
            eventHandlerPool.elementVH = elementVHToFill;
        }
        if (elementVHFree > elementVHAverageUse) {
            elementVHFillCount = elementVHAverageUse;
            elementVHFree = elementVHFree - elementVHAverageUse;
        } else {
            elementVHFillCount = elementVHFree;
            elementVHFree = 0;
        }
        System.arraycopy(elementVH, elementVHFree, elementVHToFill, 0, elementVHFillCount);
        int startVHFillCount;
        if (startVHToFill == null || startVHToFill.length < startVHAverageUse) {
            startVHToFill = new StartValidationHandler[startVHAverageUse];
            eventHandlerPool.startVH = startVHToFill;
        }
        if (startVHFree > startVHAverageUse) {
            startVHFillCount = startVHAverageUse;
            startVHFree = startVHFree - startVHAverageUse;
        } else {
            startVHFillCount = startVHFree;
            startVHFree = 0;
        }
        System.arraycopy(startVH, startVHFree, startVHToFill, 0, startVHFillCount);
        int unexpectedElementHFillCount;
        if (unexpectedElementHToFill == null || unexpectedElementHToFill.length < unexpectedElementHAverageUse) {
            unexpectedElementHToFill = new UnexpectedElementHandler[unexpectedElementHAverageUse];
            eventHandlerPool.unexpectedElementH = unexpectedElementHToFill;
        }
        if (unexpectedElementHFree > unexpectedElementHAverageUse) {
            unexpectedElementHFillCount = unexpectedElementHAverageUse;
            unexpectedElementHFree = unexpectedElementHFree - unexpectedElementHAverageUse;
        } else {
            unexpectedElementHFillCount = unexpectedElementHFree;
            unexpectedElementHFree = 0;
        }
        System.arraycopy(unexpectedElementH, unexpectedElementHFree, unexpectedElementHToFill, 0, unexpectedElementHFillCount);
        int unexpectedAmbiguousEHFillCount;
        if (unexpectedAmbiguousEHToFill == null || unexpectedAmbiguousEHToFill.length < unexpectedAmbiguousEHAverageUse) {
            unexpectedAmbiguousEHToFill = new UnexpectedAmbiguousElementHandler[unexpectedAmbiguousEHAverageUse];
            eventHandlerPool.unexpectedAmbiguousEH = unexpectedAmbiguousEHToFill;
        }
        if (unexpectedAmbiguousEHFree > unexpectedAmbiguousEHAverageUse) {
            unexpectedAmbiguousEHFillCount = unexpectedAmbiguousEHAverageUse;
            unexpectedAmbiguousEHFree = unexpectedAmbiguousEHFree - unexpectedAmbiguousEHAverageUse;
        } else {
            unexpectedAmbiguousEHFillCount = unexpectedAmbiguousEHFree;
            unexpectedAmbiguousEHFree = 0;
        }
        System.arraycopy(unexpectedAmbiguousEH, unexpectedAmbiguousEHFree, unexpectedAmbiguousEHToFill, 0, unexpectedAmbiguousEHFillCount);
        int unknownElementHFillCount;
        if (unknownElementHToFill == null || unknownElementHToFill.length < unknownElementHAverageUse) {
            unknownElementHToFill = new UnknownElementHandler[unknownElementHAverageUse];
            eventHandlerPool.unknownElementH = unknownElementHToFill;
        }
        if (unknownElementHFree > unknownElementHAverageUse) {
            unknownElementHFillCount = unknownElementHAverageUse;
            unknownElementHFree = unknownElementHFree - unknownElementHAverageUse;
        } else {
            unknownElementHFillCount = unknownElementHFree;
            unknownElementHFree = 0;
        }
        System.arraycopy(unknownElementH, unknownElementHFree, unknownElementHToFill, 0, unknownElementHFillCount);
        int elementDefaultHFillCount;
        if (elementDefaultHToFill == null || elementDefaultHToFill.length < elementDefaultHAverageUse) {
            elementDefaultHToFill = new ElementDefaultHandler[elementDefaultHAverageUse];
            eventHandlerPool.elementDefaultH = elementDefaultHToFill;
        }
        if (elementDefaultHFree > elementDefaultHAverageUse) {
            elementDefaultHFillCount = elementDefaultHAverageUse;
            elementDefaultHFree = elementDefaultHFree - elementDefaultHAverageUse;
        } else {
            elementDefaultHFillCount = elementDefaultHFree;
            elementDefaultHFree = 0;
        }
        System.arraycopy(elementDefaultH, elementDefaultHFree, elementDefaultHToFill, 0, elementDefaultHFillCount);
        int boundUnexpectedElementHFillCount;
        if (boundUnexpectedElementHToFill == null || boundUnexpectedElementHToFill.length < boundUnexpectedElementHAverageUse) {
            boundUnexpectedElementHToFill = new BoundUnexpectedElementHandler[boundUnexpectedElementHAverageUse];
            eventHandlerPool.boundUnexpectedElementH = boundUnexpectedElementHToFill;
        }
        if (boundUnexpectedElementHFree > boundUnexpectedElementHAverageUse) {
            boundUnexpectedElementHFillCount = boundUnexpectedElementHAverageUse;
            boundUnexpectedElementHFree = boundUnexpectedElementHFree - boundUnexpectedElementHAverageUse;
        } else {
            boundUnexpectedElementHFillCount = boundUnexpectedElementHFree;
            boundUnexpectedElementHFree = 0;
        }
        System.arraycopy(boundUnexpectedElementH, boundUnexpectedElementHFree, boundUnexpectedElementHToFill, 0, boundUnexpectedElementHFillCount);
        int boundUnexpectedAmbiguousEHFillCount;
        if (boundUnexpectedAmbiguousEHToFill == null || boundUnexpectedAmbiguousEHToFill.length < boundUnexpectedAmbiguousEHAverageUse) {
            boundUnexpectedAmbiguousEHToFill = new BoundUnexpectedAmbiguousElementHandler[boundUnexpectedAmbiguousEHAverageUse];
            eventHandlerPool.boundUnexpectedAmbiguousEH = boundUnexpectedAmbiguousEHToFill;
        }
        if (boundUnexpectedAmbiguousEHFree > boundUnexpectedAmbiguousEHAverageUse) {
            boundUnexpectedAmbiguousEHFillCount = boundUnexpectedAmbiguousEHAverageUse;
            boundUnexpectedAmbiguousEHFree = boundUnexpectedAmbiguousEHFree - boundUnexpectedAmbiguousEHAverageUse;
        } else {
            boundUnexpectedAmbiguousEHFillCount = boundUnexpectedAmbiguousEHFree;
            boundUnexpectedAmbiguousEHFree = 0;
        }
        System.arraycopy(boundUnexpectedAmbiguousEH, boundUnexpectedAmbiguousEHFree, boundUnexpectedAmbiguousEHToFill, 0, boundUnexpectedAmbiguousEHFillCount);
        int boundUnknownElementHFillCount;
        if (boundUnknownElementHToFill == null || boundUnknownElementHToFill.length < boundUnknownElementHAverageUse) {
            boundUnknownElementHToFill = new BoundUnknownElementHandler[boundUnknownElementHAverageUse];
            eventHandlerPool.boundUnknownElementH = boundUnknownElementHToFill;
        }
        if (boundUnknownElementHFree > boundUnknownElementHAverageUse) {
            boundUnknownElementHFillCount = boundUnknownElementHAverageUse;
            boundUnknownElementHFree = boundUnknownElementHFree - boundUnknownElementHAverageUse;
        } else {
            boundUnknownElementHFillCount = boundUnknownElementHFree;
            boundUnknownElementHFree = 0;
        }
        System.arraycopy(boundUnknownElementH, boundUnknownElementHFree, boundUnknownElementHToFill, 0, boundUnknownElementHFillCount);
        int boundElementDefaultHFillCount;
        if (boundElementDefaultHToFill == null || boundElementDefaultHToFill.length < boundElementDefaultHAverageUse) {
            boundElementDefaultHToFill = new BoundElementDefaultHandler[boundElementDefaultHAverageUse];
            eventHandlerPool.boundElementDefaultH = boundElementDefaultHToFill;
        }
        if (boundElementDefaultHFree > boundElementDefaultHAverageUse) {
            boundElementDefaultHFillCount = boundElementDefaultHAverageUse;
            boundElementDefaultHFree = boundElementDefaultHFree - boundElementDefaultHAverageUse;
        } else {
            boundElementDefaultHFillCount = boundElementDefaultHFree;
            boundElementDefaultHFree = 0;
        }
        System.arraycopy(boundElementDefaultH, boundElementDefaultHFree, boundElementDefaultHToFill, 0, boundElementDefaultHFillCount);
        int elementConcurrentHFillCount;
        if (elementConcurrentHToFill == null || elementConcurrentHToFill.length < elementConcurrentHAverageUse) {
            elementConcurrentHToFill = new ElementConcurrentHandler[elementConcurrentHAverageUse];
            eventHandlerPool.elementConcurrentH = elementConcurrentHToFill;
        }
        if (elementConcurrentHFree > elementConcurrentHAverageUse) {
            elementConcurrentHFillCount = elementConcurrentHAverageUse;
            elementConcurrentHFree = elementConcurrentHFree - elementConcurrentHAverageUse;
        } else {
            elementConcurrentHFillCount = elementConcurrentHFree;
            elementConcurrentHFree = 0;
        }
        System.arraycopy(elementConcurrentH, elementConcurrentHFree, elementConcurrentHToFill, 0, elementConcurrentHFillCount);
        int elementParallelHFillCount;
        if (elementParallelHToFill == null || elementParallelHToFill.length < elementParallelHAverageUse) {
            elementParallelHToFill = new ElementParallelHandler[elementParallelHAverageUse];
            eventHandlerPool.elementParallelH = elementParallelHToFill;
        }
        if (elementParallelHFree > elementParallelHAverageUse) {
            elementParallelHFillCount = elementParallelHAverageUse;
            elementParallelHFree = elementParallelHFree - elementParallelHAverageUse;
        } else {
            elementParallelHFillCount = elementParallelHFree;
            elementParallelHFree = 0;
        }
        System.arraycopy(elementParallelH, elementParallelHFree, elementParallelHToFill, 0, elementParallelHFillCount);
        int elementCommonHFillCount;
        if (elementCommonHToFill == null || elementCommonHToFill.length < elementCommonHAverageUse) {
            elementCommonHToFill = new ElementCommonHandler[elementCommonHAverageUse];
            eventHandlerPool.elementCommonH = elementCommonHToFill;
        }
        if (elementCommonHFree > elementCommonHAverageUse) {
            elementCommonHFillCount = elementCommonHAverageUse;
            elementCommonHFree = elementCommonHFree - elementCommonHAverageUse;
        } else {
            elementCommonHFillCount = elementCommonHFree;
            elementCommonHFree = 0;
        }
        System.arraycopy(elementCommonH, elementCommonHFree, elementCommonHToFill, 0, elementCommonHFillCount);
        int unexpectedAttributeHFillCount;
        if (unexpectedAttributeHToFill == null || unexpectedAttributeHToFill.length < unexpectedAttributeHAverageUse) {
            unexpectedAttributeHToFill = new UnexpectedAttributeHandler[unexpectedAttributeHAverageUse];
            eventHandlerPool.unexpectedAttributeH = unexpectedAttributeHToFill;
        }
        if (unexpectedAttributeHFree > unexpectedAttributeHAverageUse) {
            unexpectedAttributeHFillCount = unexpectedAttributeHAverageUse;
            unexpectedAttributeHFree = unexpectedAttributeHFree - unexpectedAttributeHAverageUse;
        } else {
            unexpectedAttributeHFillCount = unexpectedAttributeHFree;
            unexpectedAttributeHFree = 0;
        }
        System.arraycopy(unexpectedAttributeH, unexpectedAttributeHFree, unexpectedAttributeHToFill, 0, unexpectedAttributeHFillCount);
        int unexpectedAmbiguousAHFillCount;
        if (unexpectedAmbiguousAHToFill == null || unexpectedAmbiguousAHToFill.length < unexpectedAmbiguousAHAverageUse) {
            unexpectedAmbiguousAHToFill = new UnexpectedAmbiguousAttributeHandler[unexpectedAmbiguousAHAverageUse];
            eventHandlerPool.unexpectedAmbiguousAH = unexpectedAmbiguousAHToFill;
        }
        if (unexpectedAmbiguousAHFree > unexpectedAmbiguousAHAverageUse) {
            unexpectedAmbiguousAHFillCount = unexpectedAmbiguousAHAverageUse;
            unexpectedAmbiguousAHFree = unexpectedAmbiguousAHFree - unexpectedAmbiguousAHAverageUse;
        } else {
            unexpectedAmbiguousAHFillCount = unexpectedAmbiguousAHFree;
            unexpectedAmbiguousAHFree = 0;
        }
        System.arraycopy(unexpectedAmbiguousAH, unexpectedAmbiguousAHFree, unexpectedAmbiguousAHToFill, 0, unexpectedAmbiguousAHFillCount);
        int unknownAttributeHFillCount;
        if (unknownAttributeHToFill == null || unknownAttributeHToFill.length < unknownAttributeHAverageUse) {
            unknownAttributeHToFill = new UnknownAttributeHandler[unknownAttributeHAverageUse];
            eventHandlerPool.unknownAttributeH = unknownAttributeHToFill;
        }
        if (unknownAttributeHFree > unknownAttributeHAverageUse) {
            unknownAttributeHFillCount = unknownAttributeHAverageUse;
            unknownAttributeHFree = unknownAttributeHFree - unknownAttributeHAverageUse;
        } else {
            unknownAttributeHFillCount = unknownAttributeHFree;
            unknownAttributeHFree = 0;
        }
        System.arraycopy(unknownAttributeH, unknownAttributeHFree, unknownAttributeHToFill, 0, unknownAttributeHFillCount);
        int attributeVHFillCount;
        if (attributeVHToFill == null || attributeVHToFill.length < attributeVHAverageUse) {
            attributeVHToFill = new AttributeValidationHandler[attributeVHAverageUse];
            eventHandlerPool.attributeVH = attributeVHToFill;
        }
        if (attributeVHFree > attributeVHAverageUse) {
            attributeVHFillCount = attributeVHAverageUse;
            attributeVHFree = attributeVHFree - attributeVHAverageUse;
        } else {
            attributeVHFillCount = attributeVHFree;
            attributeVHFree = 0;
        }
        System.arraycopy(attributeVH, attributeVHFree, attributeVHToFill, 0, attributeVHFillCount);
        int candidateAttributeVHFillCount;
        if (candidateAttributeVHToFill == null || candidateAttributeVHToFill.length < candidateAttributeVHAverageUse) {
            candidateAttributeVHToFill = new CandidateAttributeValidationHandler[candidateAttributeVHAverageUse];
            eventHandlerPool.candidateAttributeVH = candidateAttributeVHToFill;
        }
        if (candidateAttributeVHFree > candidateAttributeVHAverageUse) {
            candidateAttributeVHFillCount = candidateAttributeVHAverageUse;
            candidateAttributeVHFree = candidateAttributeVHFree - candidateAttributeVHAverageUse;
        } else {
            candidateAttributeVHFillCount = candidateAttributeVHFree;
            candidateAttributeVHFree = 0;
        }
        System.arraycopy(candidateAttributeVH, candidateAttributeVHFree, candidateAttributeVHToFill, 0, candidateAttributeVHFillCount);
        int attributeConcurrentHFillCount;
        if (attributeConcurrentHToFill == null || attributeConcurrentHToFill.length < attributeConcurrentHAverageUse) {
            attributeConcurrentHToFill = new AttributeConcurrentHandler[attributeConcurrentHAverageUse];
            eventHandlerPool.attributeConcurrentH = attributeConcurrentHToFill;
        }
        if (attributeConcurrentHFree > attributeConcurrentHAverageUse) {
            attributeConcurrentHFillCount = attributeConcurrentHAverageUse;
            attributeConcurrentHFree = attributeConcurrentHFree - attributeConcurrentHAverageUse;
        } else {
            attributeConcurrentHFillCount = attributeConcurrentHFree;
            attributeConcurrentHFree = 0;
        }
        System.arraycopy(attributeConcurrentH, attributeConcurrentHFree, attributeConcurrentHToFill, 0, attributeConcurrentHFillCount);
        int attributeParallelHFillCount;
        if (attributeParallelHToFill == null || attributeParallelHToFill.length < attributeParallelHAverageUse) {
            attributeParallelHToFill = new AttributeParallelHandler[attributeParallelHAverageUse];
            eventHandlerPool.attributeParallelH = attributeParallelHToFill;
        }
        if (attributeParallelHFree > attributeParallelHAverageUse) {
            attributeParallelHFillCount = attributeParallelHAverageUse;
            attributeParallelHFree = attributeParallelHFree - attributeParallelHAverageUse;
        } else {
            attributeParallelHFillCount = attributeParallelHFree;
            attributeParallelHFree = 0;
        }
        System.arraycopy(attributeParallelH, attributeParallelHFree, attributeParallelHToFill, 0, attributeParallelHFillCount);
        int attributeDefaultHFillCount;
        if (attributeDefaultHToFill == null || attributeDefaultHToFill.length < attributeDefaultHAverageUse) {
            attributeDefaultHToFill = new AttributeDefaultHandler[attributeDefaultHAverageUse];
            eventHandlerPool.attributeDefaultH = attributeDefaultHToFill;
        }
        if (attributeDefaultHFree > attributeDefaultHAverageUse) {
            attributeDefaultHFillCount = attributeDefaultHAverageUse;
            attributeDefaultHFree = attributeDefaultHFree - attributeDefaultHAverageUse;
        } else {
            attributeDefaultHFillCount = attributeDefaultHFree;
            attributeDefaultHFree = 0;
        }
        System.arraycopy(attributeDefaultH, attributeDefaultHFree, attributeDefaultHToFill, 0, attributeDefaultHFillCount);
        int charactersValidationHFillCount;
        if (charactersValidationHToFill == null || charactersValidationHToFill.length < charactersValidationHAverageUse) {
            charactersValidationHToFill = new CharactersValidationHandler[charactersValidationHAverageUse];
            eventHandlerPool.charactersValidationH = charactersValidationHToFill;
        }
        if (charactersValidationHFree > charactersValidationHAverageUse) {
            charactersValidationHFillCount = charactersValidationHAverageUse;
            charactersValidationHFree = charactersValidationHFree - charactersValidationHAverageUse;
        } else {
            charactersValidationHFillCount = charactersValidationHFree;
            charactersValidationHFree = 0;
        }
        System.arraycopy(charactersValidationH, charactersValidationHFree, charactersValidationHToFill, 0, charactersValidationHFillCount);
        int structuredDataValidationHFillCount;
        if (structuredDataValidationHToFill == null || structuredDataValidationHToFill.length < structuredDataValidationHAverageUse) {
            structuredDataValidationHToFill = new StructuredDataValidationHandler[structuredDataValidationHAverageUse];
            eventHandlerPool.structuredDataValidationH = structuredDataValidationHToFill;
        }
        if (structuredDataValidationHFree > structuredDataValidationHAverageUse) {
            structuredDataValidationHFillCount = structuredDataValidationHAverageUse;
            structuredDataValidationHFree = structuredDataValidationHFree - structuredDataValidationHAverageUse;
        } else {
            structuredDataValidationHFillCount = structuredDataValidationHFree;
            structuredDataValidationHFree = 0;
        }
        System.arraycopy(structuredDataValidationH, structuredDataValidationHFree, structuredDataValidationHToFill, 0, structuredDataValidationHFillCount);
        int dataValidationHFillCount;
        if (dataValidationHToFill == null || dataValidationHToFill.length < dataValidationHAverageUse) {
            dataValidationHToFill = new DataValidationHandler[dataValidationHAverageUse];
            eventHandlerPool.dataValidationH = dataValidationHToFill;
        }
        if (dataValidationHFree > dataValidationHAverageUse) {
            dataValidationHFillCount = dataValidationHAverageUse;
            dataValidationHFree = dataValidationHFree - dataValidationHAverageUse;
        } else {
            dataValidationHFillCount = dataValidationHFree;
            dataValidationHFree = 0;
        }
        System.arraycopy(dataValidationH, dataValidationHFree, dataValidationHToFill, 0, dataValidationHFillCount);
        int defaultVAttributeHFillCount;
        if (defaultVAttributeHToFill == null || defaultVAttributeHToFill.length < defaultVAttributeHAverageUse) {
            defaultVAttributeHToFill = new DefaultValueAttributeValidationHandler[defaultVAttributeHAverageUse];
            eventHandlerPool.defaultVAttributeH = defaultVAttributeHToFill;
        }
        if (defaultVAttributeHFree > defaultVAttributeHAverageUse) {
            defaultVAttributeHFillCount = defaultVAttributeHAverageUse;
            defaultVAttributeHFree = defaultVAttributeHFree - defaultVAttributeHAverageUse;
        } else {
            defaultVAttributeHFillCount = defaultVAttributeHFree;
            defaultVAttributeHFree = 0;
        }
        System.arraycopy(defaultVAttributeH, defaultVAttributeHFree, defaultVAttributeHToFill, 0, defaultVAttributeHFillCount);
        int listPatternVHFillCount;
        if (listPatternVHToFill == null || listPatternVHToFill.length < listPatternVHAverageUse) {
            listPatternVHToFill = new ListPatternValidationHandler[listPatternVHAverageUse];
            eventHandlerPool.listPatternVH = listPatternVHToFill;
        }
        if (listPatternVHFree > listPatternVHAverageUse) {
            listPatternVHFillCount = listPatternVHAverageUse;
            listPatternVHFree = listPatternVHFree - listPatternVHAverageUse;
        } else {
            listPatternVHFillCount = listPatternVHFree;
            listPatternVHFree = 0;
        }
        System.arraycopy(listPatternVH, listPatternVHFree, listPatternVHToFill, 0, listPatternVHFillCount);
        int exceptPatternVHFillCount;
        if (exceptPatternVHToFill == null || exceptPatternVHToFill.length < exceptPatternVHAverageUse) {
            exceptPatternVHToFill = new ExceptPatternValidationHandler[exceptPatternVHAverageUse];
            eventHandlerPool.exceptPatternVH = exceptPatternVHToFill;
        }
        if (exceptPatternVHFree > exceptPatternVHAverageUse) {
            exceptPatternVHFillCount = exceptPatternVHAverageUse;
            exceptPatternVHFree = exceptPatternVHFree - exceptPatternVHAverageUse;
        } else {
            exceptPatternVHFillCount = exceptPatternVHFree;
            exceptPatternVHFree = 0;
        }
        System.arraycopy(exceptPatternVH, exceptPatternVHFree, exceptPatternVHToFill, 0, exceptPatternVHFillCount);
        int boundElementVHFillCount;
        if (boundElementVHToFill == null || boundElementVHToFill.length < boundElementVHAverageUse) {
            boundElementVHToFill = new BoundElementValidationHandler[boundElementVHAverageUse];
            eventHandlerPool.boundElementVH = boundElementVHToFill;
        }
        if (boundElementVHFree > boundElementVHAverageUse) {
            boundElementVHFillCount = boundElementVHAverageUse;
            boundElementVHFree = boundElementVHFree - boundElementVHAverageUse;
        } else {
            boundElementVHFillCount = boundElementVHFree;
            boundElementVHFree = 0;
        }
        System.arraycopy(boundElementVH, boundElementVHFree, boundElementVHToFill, 0, boundElementVHFillCount);
        int boundStartVHFillCount;
        if (boundStartVHToFill == null || boundStartVHToFill.length < boundStartVHAverageUse) {
            boundStartVHToFill = new BoundStartValidationHandler[boundStartVHAverageUse];
            eventHandlerPool.boundStartVH = boundStartVHToFill;
        }
        if (boundStartVHFree > boundStartVHAverageUse) {
            boundStartVHFillCount = boundStartVHAverageUse;
            boundStartVHFree = boundStartVHFree - boundStartVHAverageUse;
        } else {
            boundStartVHFillCount = boundStartVHFree;
            boundStartVHFree = 0;
        }
        System.arraycopy(boundStartVH, boundStartVHFree, boundStartVHToFill, 0, boundStartVHFillCount);
        int boundElementConcurrentHFillCount;
        if (boundElementConcurrentHToFill == null || boundElementConcurrentHToFill.length < boundElementConcurrentHAverageUse) {
            boundElementConcurrentHToFill = new BoundElementConcurrentHandler[boundElementConcurrentHAverageUse];
            eventHandlerPool.boundElementConcurrentH = boundElementConcurrentHToFill;
        }
        if (boundElementConcurrentHFree > boundElementConcurrentHAverageUse) {
            boundElementConcurrentHFillCount = boundElementConcurrentHAverageUse;
            boundElementConcurrentHFree = boundElementConcurrentHFree - boundElementConcurrentHAverageUse;
        } else {
            boundElementConcurrentHFillCount = boundElementConcurrentHFree;
            boundElementConcurrentHFree = 0;
        }
        System.arraycopy(boundElementConcurrentH, boundElementConcurrentHFree, boundElementConcurrentHToFill, 0, boundElementConcurrentHFillCount);
        int boundElementParallelHFillCount;
        if (boundElementParallelHToFill == null || boundElementParallelHToFill.length < boundElementParallelHAverageUse) {
            boundElementParallelHToFill = new BoundElementParallelHandler[boundElementParallelHAverageUse];
            eventHandlerPool.boundElementParallelH = boundElementParallelHToFill;
        }
        if (boundElementParallelHFree > boundElementParallelHAverageUse) {
            boundElementParallelHFillCount = boundElementParallelHAverageUse;
            boundElementParallelHFree = boundElementParallelHFree - boundElementParallelHAverageUse;
        } else {
            boundElementParallelHFillCount = boundElementParallelHFree;
            boundElementParallelHFree = 0;
        }
        System.arraycopy(boundElementParallelH, boundElementParallelHFree, boundElementParallelHToFill, 0, boundElementParallelHFillCount);
        int boundElementCommonHFillCount;
        if (boundElementCommonHToFill == null || boundElementCommonHToFill.length < boundElementCommonHAverageUse) {
            boundElementCommonHToFill = new BoundElementCommonHandler[boundElementCommonHAverageUse];
            eventHandlerPool.boundElementCommonH = boundElementCommonHToFill;
        }
        if (boundElementCommonHFree > boundElementCommonHAverageUse) {
            boundElementCommonHFillCount = boundElementCommonHAverageUse;
            boundElementCommonHFree = boundElementCommonHFree - boundElementCommonHAverageUse;
        } else {
            boundElementCommonHFillCount = boundElementCommonHFree;
            boundElementCommonHFree = 0;
        }
        System.arraycopy(boundElementCommonH, boundElementCommonHFree, boundElementCommonHToFill, 0, boundElementCommonHFillCount);
        int boundAttributeVHFillCount;
        if (boundAttributeVHToFill == null || boundAttributeVHToFill.length < boundAttributeVHAverageUse) {
            boundAttributeVHToFill = new BoundAttributeValidationHandler[boundAttributeVHAverageUse];
            eventHandlerPool.boundAttributeVH = boundAttributeVHToFill;
        }
        if (boundAttributeVHFree > boundAttributeVHAverageUse) {
            boundAttributeVHFillCount = boundAttributeVHAverageUse;
            boundAttributeVHFree = boundAttributeVHFree - boundAttributeVHAverageUse;
        } else {
            boundAttributeVHFillCount = boundAttributeVHFree;
            boundAttributeVHFree = 0;
        }
        System.arraycopy(boundAttributeVH, boundAttributeVHFree, boundAttributeVHToFill, 0, boundAttributeVHFillCount);
        int boundCandidateAttributeVHFillCount;
        if (boundCandidateAttributeVHToFill == null || boundCandidateAttributeVHToFill.length < boundCandidateAttributeVHAverageUse) {
            boundCandidateAttributeVHToFill = new BoundCandidateAttributeValidationHandler[boundCandidateAttributeVHAverageUse];
            eventHandlerPool.boundCandidateAttributeVH = boundCandidateAttributeVHToFill;
        }
        if (boundCandidateAttributeVHFree > boundCandidateAttributeVHAverageUse) {
            boundCandidateAttributeVHFillCount = boundCandidateAttributeVHAverageUse;
            boundCandidateAttributeVHFree = boundCandidateAttributeVHFree - boundCandidateAttributeVHAverageUse;
        } else {
            boundCandidateAttributeVHFillCount = boundCandidateAttributeVHFree;
            boundCandidateAttributeVHFree = 0;
        }
        System.arraycopy(boundCandidateAttributeVH, boundCandidateAttributeVHFree, boundCandidateAttributeVHToFill, 0, boundCandidateAttributeVHFillCount);
        int boundAttributeConcurrentHFillCount;
        if (boundAttributeConcurrentHToFill == null || boundAttributeConcurrentHToFill.length < boundAttributeConcurrentHAverageUse) {
            boundAttributeConcurrentHToFill = new BoundAttributeConcurrentHandler[boundAttributeConcurrentHAverageUse];
            eventHandlerPool.boundAttributeConcurrentH = boundAttributeConcurrentHToFill;
        }
        if (boundAttributeConcurrentHFree > boundAttributeConcurrentHAverageUse) {
            boundAttributeConcurrentHFillCount = boundAttributeConcurrentHAverageUse;
            boundAttributeConcurrentHFree = boundAttributeConcurrentHFree - boundAttributeConcurrentHAverageUse;
        } else {
            boundAttributeConcurrentHFillCount = boundAttributeConcurrentHFree;
            boundAttributeConcurrentHFree = 0;
        }
        System.arraycopy(boundAttributeConcurrentH, boundAttributeConcurrentHFree, boundAttributeConcurrentHToFill, 0, boundAttributeConcurrentHFillCount);
        int boundAttributeParallelHFillCount;
        if (boundAttributeParallelHToFill == null || boundAttributeParallelHToFill.length < boundAttributeParallelHAverageUse) {
            boundAttributeParallelHToFill = new BoundAttributeParallelHandler[boundAttributeParallelHAverageUse];
            eventHandlerPool.boundAttributeParallelH = boundAttributeParallelHToFill;
        }
        if (boundAttributeParallelHFree > boundAttributeParallelHAverageUse) {
            boundAttributeParallelHFillCount = boundAttributeParallelHAverageUse;
            boundAttributeParallelHFree = boundAttributeParallelHFree - boundAttributeParallelHAverageUse;
        } else {
            boundAttributeParallelHFillCount = boundAttributeParallelHFree;
            boundAttributeParallelHFree = 0;
        }
        System.arraycopy(boundAttributeParallelH, boundAttributeParallelHFree, boundAttributeParallelHToFill, 0, boundAttributeParallelHFillCount);
        eventHandlerPool.initFilled(elementVHFillCount, startVHFillCount, unexpectedElementHFillCount, unexpectedAmbiguousEHFillCount, unknownElementHFillCount, elementDefaultHFillCount, boundUnexpectedElementHFillCount, boundUnexpectedAmbiguousEHFillCount, boundUnknownElementHFillCount, boundElementDefaultHFillCount, elementConcurrentHFillCount, elementParallelHFillCount, elementCommonHFillCount, unexpectedAttributeHFillCount, unexpectedAmbiguousAHFillCount, unknownAttributeHFillCount, attributeVHFillCount, candidateAttributeVHFillCount, attributeConcurrentHFillCount, attributeParallelHFillCount, attributeDefaultHFillCount, charactersValidationHFillCount, structuredDataValidationHFillCount, dataValidationHFillCount, defaultVAttributeHFillCount, listPatternVHFillCount, exceptPatternVHFillCount, boundElementVHFillCount, boundStartVHFillCount, boundElementConcurrentHFillCount, boundElementParallelHFillCount, boundElementCommonHFillCount, boundAttributeVHFillCount, boundCandidateAttributeVHFillCount, boundAttributeConcurrentHFillCount, boundAttributeParallelHFillCount);
    }

    synchronized void recycle(int elementVHRecycledCount, int elementVHEffectivellyUsed, ElementValidationHandler[] elementVHRecycled, int startVHRecycledCount, int startVHEffectivellyUsed, StartValidationHandler[] startVHRecycled, int unexpectedElementHRecycledCount, int unexpectedElementHEffectivellyUsed, UnexpectedElementHandler[] unexpectedElementHRecycled, int unexpectedAmbiguousEHRecycledCount, int unexpectedAmbiguousEHEffectivellyUsed, UnexpectedAmbiguousElementHandler[] unexpectedAmbiguousEHRecycled, int unknownElementHRecycledCount, int unknownElementHEffectivellyUsed, UnknownElementHandler[] unknownElementHRecycled, int elementDefaultHRecycledCount, int elementDefaultHEffectivellyUsed, ElementDefaultHandler[] elementDefaultHRecycled, int boundUnexpectedElementHRecycledCount, int boundUnexpectedElementHEffectivellyUsed, BoundUnexpectedElementHandler[] boundUnexpectedElementHRecycled, int boundUnexpectedAmbiguousEHRecycledCount, int boundUnexpectedAmbiguousEHEffectivellyUsed, BoundUnexpectedAmbiguousElementHandler[] boundUnexpectedAmbiguousEHRecycled, int boundUnknownElementHRecycledCount, int boundUnknownElementHEffectivellyUsed, BoundUnknownElementHandler[] boundUnknownElementHRecycled, int boundElementDefaultHRecycledCount, int boundElementDefaultHEffectivellyUsed, BoundElementDefaultHandler[] boundElementDefaultHRecycled, int elementConcurrentHRecycledCount, int elementConcurrentHEffectivellyUsed, ElementConcurrentHandler[] elementConcurrentHRecycled, int elementParallelHRecycledCount, int elementParallelHEffectivellyUsed, ElementParallelHandler[] elementParallelHRecycled, int elementCommonHRecycledCount, int elementCommonHEffectivellyUsed, ElementCommonHandler[] elementCommonHRecycled, int unexpectedAttributeHRecycledCount, int unexpectedAttributeHEffectivellyUsed, UnexpectedAttributeHandler[] unexpectedAttributeHRecycled, int unexpectedAmbiguousAHRecycledCount, int unexpectedAmbiguousAHEffectivellyUsed, UnexpectedAmbiguousAttributeHandler[] unexpectedAmbiguousAHRecycled, int unknownAttributeHRecycledCount, int unknownAttributeHEffectivellyUsed, UnknownAttributeHandler[] unknownAttributeHRecycled, int attributeVHRecycledCount, int attributeVHEffectivellyUsed, AttributeValidationHandler[] attributeVHRecycled, int candidateAttributeVHRecycledCount, int candidateAttributeVHEffectivellyUsed, CandidateAttributeValidationHandler[] candidateAttributeVHRecycled, int attributeConcurrentHRecycledCount, int attributeConcurrentHEffectivellyUsed, AttributeConcurrentHandler[] attributeConcurrentHRecycled, int attributeParallelHRecycledCount, int attributeParallelHEffectivellyUsed, AttributeParallelHandler[] attributeParallelHRecycled, int attributeDefaultHRecycledCount, int attributeDefaultHEffectivellyUsed, AttributeDefaultHandler[] attributeDefaultHRecycled, int charactersValidationHRecycledCount, int charactersValidationHEffectivellyUsed, CharactersValidationHandler[] charactersValidationHRecycled, int structuredDataValidationHRecycledCount, int structuredDataValidationHEffectivellyUsed, StructuredDataValidationHandler[] structuredDataValidationHRecycled, int dataValidationHRecycledCount, int dataValidationHEffectivellyUsed, DataValidationHandler[] dataValidationHRecycled, int defaultVAttributeHRecycledCount, int defaultVAttributeHEffectivellyUsed, DefaultValueAttributeValidationHandler[] defaultVAttributeHRecycled, int listPatternVHRecycledCount, int listPatternVHEffectivellyUsed, ListPatternValidationHandler[] listPatternVHRecycled, int exceptPatternVHRecycledCount, int exceptPatternVHEffectivellyUsed, ExceptPatternValidationHandler[] exceptPatternVHRecycled, int boundElementVHRecycledCount, int boundElementVHEffectivellyUsed, BoundElementValidationHandler[] boundElementVHRecycled, int boundStartVHRecycledCount, int boundStartVHEffectivellyUsed, BoundStartValidationHandler[] boundStartVHRecycled, int boundElementConcurrentHRecycledCount, int boundElementConcurrentHEffectivellyUsed, BoundElementConcurrentHandler[] boundElementConcurrentHRecycled, int boundElementParallelHRecycledCount, int boundElementParallelHEffectivellyUsed, BoundElementParallelHandler[] boundElementParallelHRecycled, int boundElementCommonHRecycledCount, int boundElementCommonHEffectivellyUsed, BoundElementCommonHandler[] boundElementCommonHRecycled, int boundAttributeVHRecycledCount, int boundAttributeVHEffectivellyUsed, BoundAttributeValidationHandler[] boundAttributeVHRecycled, int boundCandidateAttributeVHRecycledCount, int boundCandidateAttributeVHEffectivellyUsed, BoundCandidateAttributeValidationHandler[] boundCandidateAttributeVHRecycled, int boundAttributeConcurrentHRecycledCount, int boundAttributeConcurrentHEffectivellyUsed, BoundAttributeConcurrentHandler[] boundAttributeConcurrentHRecycled, int boundAttributeParallelHRecycledCount, int boundAttributeParallelHEffectivellyUsed, BoundAttributeParallelHandler[] boundAttributeParallelHRecycled) {
        int neededLength = elementVHFree + elementVHRecycledCount;
        if (neededLength > elementVH.length) {
            if (neededLength > elementVHMaxSize) {
                neededLength = elementVHMaxSize;
                ElementValidationHandler[] increased = new ElementValidationHandler[neededLength];
                System.arraycopy(elementVH, 0, increased, 0, elementVH.length);
                elementVH = increased;
                System.arraycopy(elementVHRecycled, 0, elementVH, elementVHFree, elementVHMaxSize - elementVHFree);
                elementVHFree = elementVHMaxSize;
            } else {
                ElementValidationHandler[] increased = new ElementValidationHandler[neededLength];
                System.arraycopy(elementVH, 0, increased, 0, elementVH.length);
                elementVH = increased;
                System.arraycopy(elementVHRecycled, 0, elementVH, elementVHFree, elementVHRecycledCount);
                elementVHFree += elementVHRecycledCount;
            }
        } else {
            System.arraycopy(elementVHRecycled, 0, elementVH, elementVHFree, elementVHRecycledCount);
            elementVHFree += elementVHRecycledCount;
        }
        if (elementVHAverageUse != 0) elementVHAverageUse = (elementVHAverageUse + elementVHEffectivellyUsed) / 2; else elementVHAverageUse = elementVHEffectivellyUsed;
        for (int i = 0; i < elementVHRecycled.length; i++) {
            elementVHRecycled[i] = null;
        }
        neededLength = startVHFree + startVHRecycledCount;
        if (neededLength > startVH.length) {
            if (neededLength > startVHMaxSize) {
                neededLength = startVHMaxSize;
                StartValidationHandler[] increased = new StartValidationHandler[neededLength];
                System.arraycopy(startVH, 0, increased, 0, startVH.length);
                startVH = increased;
                System.arraycopy(startVHRecycled, 0, startVH, startVHFree, startVHMaxSize - startVHFree);
                startVHFree = startVHMaxSize;
            } else {
                StartValidationHandler[] increased = new StartValidationHandler[neededLength];
                System.arraycopy(startVH, 0, increased, 0, startVH.length);
                startVH = increased;
                System.arraycopy(startVHRecycled, 0, startVH, startVHFree, startVHRecycledCount);
                startVHFree += startVHRecycledCount;
            }
        } else {
            System.arraycopy(startVHRecycled, 0, startVH, startVHFree, startVHRecycledCount);
            startVHFree += startVHRecycledCount;
        }
        if (startVHAverageUse != 0) startVHAverageUse = (startVHAverageUse + startVHEffectivellyUsed) / 2; else startVHAverageUse = startVHEffectivellyUsed;
        for (int i = 0; i < startVHRecycled.length; i++) {
            startVHRecycled[i] = null;
        }
        neededLength = unexpectedElementHFree + unexpectedElementHRecycledCount;
        if (neededLength > unexpectedElementH.length) {
            if (neededLength > unexpectedElementHMaxSize) {
                neededLength = unexpectedElementHMaxSize;
                UnexpectedElementHandler[] increased = new UnexpectedElementHandler[neededLength];
                System.arraycopy(unexpectedElementH, 0, increased, 0, unexpectedElementH.length);
                unexpectedElementH = increased;
                System.arraycopy(unexpectedElementHRecycled, 0, unexpectedElementH, unexpectedElementHFree, unexpectedElementHMaxSize - unexpectedElementHFree);
                unexpectedElementHFree = unexpectedElementHMaxSize;
            } else {
                UnexpectedElementHandler[] increased = new UnexpectedElementHandler[neededLength];
                System.arraycopy(unexpectedElementH, 0, increased, 0, unexpectedElementH.length);
                unexpectedElementH = increased;
                System.arraycopy(unexpectedElementHRecycled, 0, unexpectedElementH, unexpectedElementHFree, unexpectedElementHRecycledCount);
                unexpectedElementHFree += unexpectedElementHRecycledCount;
            }
        } else {
            System.arraycopy(unexpectedElementHRecycled, 0, unexpectedElementH, unexpectedElementHFree, unexpectedElementHRecycledCount);
            unexpectedElementHFree += unexpectedElementHRecycledCount;
        }
        if (unexpectedElementHAverageUse != 0) unexpectedElementHAverageUse = (unexpectedElementHAverageUse + unexpectedElementHEffectivellyUsed) / 2; else unexpectedElementHAverageUse = unexpectedElementHEffectivellyUsed;
        for (int i = 0; i < unexpectedElementHRecycled.length; i++) {
            unexpectedElementHRecycled[i] = null;
        }
        neededLength = unexpectedAmbiguousEHFree + unexpectedAmbiguousEHRecycledCount;
        if (neededLength > unexpectedAmbiguousEH.length) {
            if (neededLength > unexpectedAmbiguousEHMaxSize) {
                neededLength = unexpectedAmbiguousEHMaxSize;
                UnexpectedAmbiguousElementHandler[] increased = new UnexpectedAmbiguousElementHandler[neededLength];
                System.arraycopy(unexpectedAmbiguousEH, 0, increased, 0, unexpectedAmbiguousEH.length);
                unexpectedAmbiguousEH = increased;
                System.arraycopy(unexpectedAmbiguousEHRecycled, 0, unexpectedAmbiguousEH, unexpectedAmbiguousEHFree, unexpectedAmbiguousEHMaxSize - unexpectedAmbiguousEHFree);
                unexpectedAmbiguousEHFree = unexpectedAmbiguousEHMaxSize;
            } else {
                UnexpectedAmbiguousElementHandler[] increased = new UnexpectedAmbiguousElementHandler[neededLength];
                System.arraycopy(unexpectedAmbiguousEH, 0, increased, 0, unexpectedAmbiguousEH.length);
                unexpectedAmbiguousEH = increased;
                System.arraycopy(unexpectedAmbiguousEHRecycled, 0, unexpectedAmbiguousEH, unexpectedAmbiguousEHFree, unexpectedAmbiguousEHRecycledCount);
                unexpectedAmbiguousEHFree += unexpectedAmbiguousEHRecycledCount;
            }
        } else {
            System.arraycopy(unexpectedAmbiguousEHRecycled, 0, unexpectedAmbiguousEH, unexpectedAmbiguousEHFree, unexpectedAmbiguousEHRecycledCount);
            unexpectedAmbiguousEHFree += unexpectedAmbiguousEHRecycledCount;
        }
        if (unexpectedAmbiguousEHAverageUse != 0) unexpectedAmbiguousEHAverageUse = (unexpectedAmbiguousEHAverageUse + unexpectedAmbiguousEHEffectivellyUsed) / 2; else unexpectedAmbiguousEHAverageUse = unexpectedAmbiguousEHEffectivellyUsed;
        for (int i = 0; i < unexpectedAmbiguousEHRecycled.length; i++) {
            unexpectedAmbiguousEHRecycled[i] = null;
        }
        neededLength = unknownElementHFree + unknownElementHRecycledCount;
        if (neededLength > unknownElementH.length) {
            if (neededLength > unknownElementHMaxSize) {
                neededLength = unknownElementHMaxSize;
                UnknownElementHandler[] increased = new UnknownElementHandler[neededLength];
                System.arraycopy(unknownElementH, 0, increased, 0, unknownElementH.length);
                unknownElementH = increased;
                System.arraycopy(unknownElementHRecycled, 0, unknownElementH, unknownElementHFree, unknownElementHMaxSize - unknownElementHFree);
                unknownElementHFree = unknownElementHMaxSize;
            } else {
                UnknownElementHandler[] increased = new UnknownElementHandler[neededLength];
                System.arraycopy(unknownElementH, 0, increased, 0, unknownElementH.length);
                unknownElementH = increased;
                System.arraycopy(unknownElementHRecycled, 0, unknownElementH, unknownElementHFree, unknownElementHRecycledCount);
                unknownElementHFree += unknownElementHRecycledCount;
            }
        } else {
            System.arraycopy(unknownElementHRecycled, 0, unknownElementH, unknownElementHFree, unknownElementHRecycledCount);
            unknownElementHFree += unknownElementHRecycledCount;
        }
        if (unknownElementHAverageUse != 0) unknownElementHAverageUse = (unknownElementHAverageUse + unknownElementHEffectivellyUsed) / 2; else unknownElementHAverageUse = unknownElementHEffectivellyUsed;
        for (int i = 0; i < unknownElementHRecycled.length; i++) {
            unknownElementHRecycled[i] = null;
        }
        neededLength = elementDefaultHFree + elementDefaultHRecycledCount;
        if (neededLength > elementDefaultH.length) {
            if (neededLength > elementDefaultHMaxSize) {
                neededLength = elementDefaultHMaxSize;
                ElementDefaultHandler[] increased = new ElementDefaultHandler[neededLength];
                System.arraycopy(elementDefaultH, 0, increased, 0, elementDefaultH.length);
                elementDefaultH = increased;
                System.arraycopy(elementDefaultHRecycled, 0, elementDefaultH, elementDefaultHFree, elementDefaultHMaxSize - elementDefaultHFree);
                elementDefaultHFree = elementDefaultHMaxSize;
            } else {
                ElementDefaultHandler[] increased = new ElementDefaultHandler[neededLength];
                System.arraycopy(elementDefaultH, 0, increased, 0, elementDefaultH.length);
                elementDefaultH = increased;
                System.arraycopy(elementDefaultHRecycled, 0, elementDefaultH, elementDefaultHFree, elementDefaultHRecycledCount);
                elementDefaultHFree += elementDefaultHRecycledCount;
            }
        } else {
            System.arraycopy(elementDefaultHRecycled, 0, elementDefaultH, elementDefaultHFree, elementDefaultHRecycledCount);
            elementDefaultHFree += elementDefaultHRecycledCount;
        }
        if (elementDefaultHAverageUse != 0) elementDefaultHAverageUse = (elementDefaultHAverageUse + elementDefaultHEffectivellyUsed) / 2; else elementDefaultHAverageUse = elementDefaultHEffectivellyUsed;
        for (int i = 0; i < elementDefaultHRecycled.length; i++) {
            elementDefaultHRecycled[i] = null;
        }
        neededLength = boundUnexpectedElementHFree + boundUnexpectedElementHRecycledCount;
        if (neededLength > boundUnexpectedElementH.length) {
            if (neededLength > boundUnexpectedElementHMaxSize) {
                neededLength = boundUnexpectedElementHMaxSize;
                BoundUnexpectedElementHandler[] increased = new BoundUnexpectedElementHandler[neededLength];
                System.arraycopy(boundUnexpectedElementH, 0, increased, 0, boundUnexpectedElementH.length);
                boundUnexpectedElementH = increased;
                System.arraycopy(boundUnexpectedElementHRecycled, 0, boundUnexpectedElementH, boundUnexpectedElementHFree, boundUnexpectedElementHMaxSize - boundUnexpectedElementHFree);
                boundUnexpectedElementHFree = boundUnexpectedElementHMaxSize;
            } else {
                BoundUnexpectedElementHandler[] increased = new BoundUnexpectedElementHandler[neededLength];
                System.arraycopy(boundUnexpectedElementH, 0, increased, 0, boundUnexpectedElementH.length);
                boundUnexpectedElementH = increased;
                System.arraycopy(boundUnexpectedElementHRecycled, 0, boundUnexpectedElementH, boundUnexpectedElementHFree, boundUnexpectedElementHRecycledCount);
                boundUnexpectedElementHFree += boundUnexpectedElementHRecycledCount;
            }
        } else {
            System.arraycopy(boundUnexpectedElementHRecycled, 0, boundUnexpectedElementH, boundUnexpectedElementHFree, boundUnexpectedElementHRecycledCount);
            boundUnexpectedElementHFree += boundUnexpectedElementHRecycledCount;
        }
        if (boundUnexpectedElementHAverageUse != 0) boundUnexpectedElementHAverageUse = (boundUnexpectedElementHAverageUse + boundUnexpectedElementHEffectivellyUsed) / 2; else boundUnexpectedElementHAverageUse = boundUnexpectedElementHEffectivellyUsed;
        for (int i = 0; i < boundUnexpectedElementHRecycled.length; i++) {
            boundUnexpectedElementHRecycled[i] = null;
        }
        neededLength = boundUnexpectedAmbiguousEHFree + boundUnexpectedAmbiguousEHRecycledCount;
        if (neededLength > boundUnexpectedAmbiguousEH.length) {
            if (neededLength > boundUnexpectedAmbiguousEHMaxSize) {
                neededLength = boundUnexpectedAmbiguousEHMaxSize;
                BoundUnexpectedAmbiguousElementHandler[] increased = new BoundUnexpectedAmbiguousElementHandler[neededLength];
                System.arraycopy(boundUnexpectedAmbiguousEH, 0, increased, 0, boundUnexpectedAmbiguousEH.length);
                boundUnexpectedAmbiguousEH = increased;
                System.arraycopy(boundUnexpectedAmbiguousEHRecycled, 0, boundUnexpectedAmbiguousEH, boundUnexpectedAmbiguousEHFree, boundUnexpectedAmbiguousEHMaxSize - boundUnexpectedAmbiguousEHFree);
                boundUnexpectedAmbiguousEHFree = boundUnexpectedAmbiguousEHMaxSize;
            } else {
                BoundUnexpectedAmbiguousElementHandler[] increased = new BoundUnexpectedAmbiguousElementHandler[neededLength];
                System.arraycopy(boundUnexpectedAmbiguousEH, 0, increased, 0, boundUnexpectedAmbiguousEH.length);
                boundUnexpectedAmbiguousEH = increased;
                System.arraycopy(boundUnexpectedAmbiguousEHRecycled, 0, boundUnexpectedAmbiguousEH, boundUnexpectedAmbiguousEHFree, boundUnexpectedAmbiguousEHRecycledCount);
                boundUnexpectedAmbiguousEHFree += boundUnexpectedAmbiguousEHRecycledCount;
            }
        } else {
            System.arraycopy(boundUnexpectedAmbiguousEHRecycled, 0, boundUnexpectedAmbiguousEH, boundUnexpectedAmbiguousEHFree, boundUnexpectedAmbiguousEHRecycledCount);
            boundUnexpectedAmbiguousEHFree += boundUnexpectedAmbiguousEHRecycledCount;
        }
        if (boundUnexpectedAmbiguousEHAverageUse != 0) boundUnexpectedAmbiguousEHAverageUse = (boundUnexpectedAmbiguousEHAverageUse + boundUnexpectedAmbiguousEHEffectivellyUsed) / 2; else boundUnexpectedAmbiguousEHAverageUse = boundUnexpectedAmbiguousEHEffectivellyUsed;
        for (int i = 0; i < boundUnexpectedAmbiguousEHRecycled.length; i++) {
            boundUnexpectedAmbiguousEHRecycled[i] = null;
        }
        neededLength = boundUnknownElementHFree + boundUnknownElementHRecycledCount;
        if (neededLength > boundUnknownElementH.length) {
            if (neededLength > boundUnknownElementHMaxSize) {
                neededLength = boundUnknownElementHMaxSize;
                BoundUnknownElementHandler[] increased = new BoundUnknownElementHandler[neededLength];
                System.arraycopy(boundUnknownElementH, 0, increased, 0, boundUnknownElementH.length);
                boundUnknownElementH = increased;
                System.arraycopy(boundUnknownElementHRecycled, 0, boundUnknownElementH, boundUnknownElementHFree, boundUnknownElementHMaxSize - boundUnknownElementHFree);
                boundUnknownElementHFree = boundUnknownElementHMaxSize;
            } else {
                BoundUnknownElementHandler[] increased = new BoundUnknownElementHandler[neededLength];
                System.arraycopy(boundUnknownElementH, 0, increased, 0, boundUnknownElementH.length);
                boundUnknownElementH = increased;
                System.arraycopy(boundUnknownElementHRecycled, 0, boundUnknownElementH, boundUnknownElementHFree, boundUnknownElementHRecycledCount);
                boundUnknownElementHFree += boundUnknownElementHRecycledCount;
            }
        } else {
            System.arraycopy(boundUnknownElementHRecycled, 0, boundUnknownElementH, boundUnknownElementHFree, boundUnknownElementHRecycledCount);
            boundUnknownElementHFree += boundUnknownElementHRecycledCount;
        }
        if (boundUnknownElementHAverageUse != 0) boundUnknownElementHAverageUse = (boundUnknownElementHAverageUse + boundUnknownElementHEffectivellyUsed) / 2; else boundUnknownElementHAverageUse = boundUnknownElementHEffectivellyUsed;
        for (int i = 0; i < boundUnknownElementHRecycled.length; i++) {
            boundUnknownElementHRecycled[i] = null;
        }
        neededLength = boundElementDefaultHFree + boundElementDefaultHRecycledCount;
        if (neededLength > boundElementDefaultH.length) {
            if (neededLength > boundElementDefaultHMaxSize) {
                neededLength = boundElementDefaultHMaxSize;
                BoundElementDefaultHandler[] increased = new BoundElementDefaultHandler[neededLength];
                System.arraycopy(boundElementDefaultH, 0, increased, 0, boundElementDefaultH.length);
                boundElementDefaultH = increased;
                System.arraycopy(boundElementDefaultHRecycled, 0, boundElementDefaultH, boundElementDefaultHFree, boundElementDefaultHMaxSize - boundElementDefaultHFree);
                boundElementDefaultHFree = boundElementDefaultHMaxSize;
            } else {
                BoundElementDefaultHandler[] increased = new BoundElementDefaultHandler[neededLength];
                System.arraycopy(boundElementDefaultH, 0, increased, 0, boundElementDefaultH.length);
                boundElementDefaultH = increased;
                System.arraycopy(boundElementDefaultHRecycled, 0, boundElementDefaultH, boundElementDefaultHFree, boundElementDefaultHRecycledCount);
                boundElementDefaultHFree += boundElementDefaultHRecycledCount;
            }
        } else {
            System.arraycopy(boundElementDefaultHRecycled, 0, boundElementDefaultH, boundElementDefaultHFree, boundElementDefaultHRecycledCount);
            boundElementDefaultHFree += boundElementDefaultHRecycledCount;
        }
        if (boundElementDefaultHAverageUse != 0) boundElementDefaultHAverageUse = (boundElementDefaultHAverageUse + boundElementDefaultHEffectivellyUsed) / 2; else boundElementDefaultHAverageUse = boundElementDefaultHEffectivellyUsed;
        for (int i = 0; i < boundElementDefaultHRecycled.length; i++) {
            boundElementDefaultHRecycled[i] = null;
        }
        neededLength = elementConcurrentHFree + elementConcurrentHRecycledCount;
        if (neededLength > elementConcurrentH.length) {
            if (neededLength > elementConcurrentHMaxSize) {
                neededLength = elementConcurrentHMaxSize;
                ElementConcurrentHandler[] increased = new ElementConcurrentHandler[neededLength];
                System.arraycopy(elementConcurrentH, 0, increased, 0, elementConcurrentH.length);
                elementConcurrentH = increased;
                System.arraycopy(elementConcurrentHRecycled, 0, elementConcurrentH, elementConcurrentHFree, elementConcurrentHMaxSize - elementConcurrentHFree);
                elementConcurrentHFree = elementConcurrentHMaxSize;
            } else {
                ElementConcurrentHandler[] increased = new ElementConcurrentHandler[neededLength];
                System.arraycopy(elementConcurrentH, 0, increased, 0, elementConcurrentH.length);
                elementConcurrentH = increased;
                System.arraycopy(elementConcurrentHRecycled, 0, elementConcurrentH, elementConcurrentHFree, elementConcurrentHRecycledCount);
                elementConcurrentHFree += elementConcurrentHRecycledCount;
            }
        } else {
            System.arraycopy(elementConcurrentHRecycled, 0, elementConcurrentH, elementConcurrentHFree, elementConcurrentHRecycledCount);
            elementConcurrentHFree += elementConcurrentHRecycledCount;
        }
        if (elementConcurrentHAverageUse != 0) elementConcurrentHAverageUse = (elementConcurrentHAverageUse + elementConcurrentHEffectivellyUsed) / 2; else elementConcurrentHAverageUse = elementConcurrentHEffectivellyUsed;
        for (int i = 0; i < elementConcurrentHRecycled.length; i++) {
            elementConcurrentHRecycled[i] = null;
        }
        neededLength = elementParallelHFree + elementParallelHRecycledCount;
        if (neededLength > elementParallelH.length) {
            if (neededLength > elementParallelHMaxSize) {
                neededLength = elementParallelHMaxSize;
                ElementParallelHandler[] increased = new ElementParallelHandler[neededLength];
                System.arraycopy(elementParallelH, 0, increased, 0, elementParallelH.length);
                elementParallelH = increased;
                System.arraycopy(elementParallelHRecycled, 0, elementParallelH, elementParallelHFree, elementParallelHMaxSize - elementParallelHFree);
                elementParallelHFree = elementParallelHMaxSize;
            } else {
                ElementParallelHandler[] increased = new ElementParallelHandler[neededLength];
                System.arraycopy(elementParallelH, 0, increased, 0, elementParallelH.length);
                elementParallelH = increased;
                System.arraycopy(elementParallelHRecycled, 0, elementParallelH, elementParallelHFree, elementParallelHRecycledCount);
                elementParallelHFree += elementParallelHRecycledCount;
            }
        } else {
            System.arraycopy(elementParallelHRecycled, 0, elementParallelH, elementParallelHFree, elementParallelHRecycledCount);
            elementParallelHFree += elementParallelHRecycledCount;
        }
        if (elementParallelHAverageUse != 0) elementParallelHAverageUse = (elementParallelHAverageUse + elementParallelHEffectivellyUsed) / 2; else elementParallelHAverageUse = elementParallelHEffectivellyUsed;
        for (int i = 0; i < elementParallelHRecycled.length; i++) {
            elementParallelHRecycled[i] = null;
        }
        neededLength = elementCommonHFree + elementCommonHRecycledCount;
        if (neededLength > elementCommonH.length) {
            if (neededLength > elementCommonHMaxSize) {
                neededLength = elementCommonHMaxSize;
                ElementCommonHandler[] increased = new ElementCommonHandler[neededLength];
                System.arraycopy(elementCommonH, 0, increased, 0, elementCommonH.length);
                elementCommonH = increased;
                System.arraycopy(elementCommonHRecycled, 0, elementCommonH, elementCommonHFree, elementCommonHMaxSize - elementCommonHFree);
                elementCommonHFree = elementCommonHMaxSize;
            } else {
                ElementCommonHandler[] increased = new ElementCommonHandler[neededLength];
                System.arraycopy(elementCommonH, 0, increased, 0, elementCommonH.length);
                elementCommonH = increased;
                System.arraycopy(elementCommonHRecycled, 0, elementCommonH, elementCommonHFree, elementCommonHRecycledCount);
                elementCommonHFree += elementCommonHRecycledCount;
            }
        } else {
            System.arraycopy(elementCommonHRecycled, 0, elementCommonH, elementCommonHFree, elementCommonHRecycledCount);
            elementCommonHFree += elementCommonHRecycledCount;
        }
        if (elementCommonHAverageUse != 0) elementCommonHAverageUse = (elementCommonHAverageUse + elementCommonHEffectivellyUsed) / 2; else elementCommonHAverageUse = elementCommonHEffectivellyUsed;
        for (int i = 0; i < elementCommonHRecycled.length; i++) {
            elementCommonHRecycled[i] = null;
        }
        neededLength = unexpectedAttributeHFree + unexpectedAttributeHRecycledCount;
        if (neededLength > unexpectedAttributeH.length) {
            if (neededLength > unexpectedAttributeHMaxSize) {
                neededLength = unexpectedAttributeHMaxSize;
                UnexpectedAttributeHandler[] increased = new UnexpectedAttributeHandler[neededLength];
                System.arraycopy(unexpectedAttributeH, 0, increased, 0, unexpectedAttributeH.length);
                unexpectedAttributeH = increased;
                System.arraycopy(unexpectedAttributeHRecycled, 0, unexpectedAttributeH, unexpectedAttributeHFree, unexpectedAttributeHMaxSize - unexpectedAttributeHFree);
                unexpectedAttributeHFree = unexpectedAttributeHMaxSize;
            } else {
                UnexpectedAttributeHandler[] increased = new UnexpectedAttributeHandler[neededLength];
                System.arraycopy(unexpectedAttributeH, 0, increased, 0, unexpectedAttributeH.length);
                unexpectedAttributeH = increased;
                System.arraycopy(unexpectedAttributeHRecycled, 0, unexpectedAttributeH, unexpectedAttributeHFree, unexpectedAttributeHRecycledCount);
                unexpectedAttributeHFree += unexpectedAttributeHRecycledCount;
            }
        } else {
            System.arraycopy(unexpectedAttributeHRecycled, 0, unexpectedAttributeH, unexpectedAttributeHFree, unexpectedAttributeHRecycledCount);
            unexpectedAttributeHFree += unexpectedAttributeHRecycledCount;
        }
        if (unexpectedAttributeHAverageUse != 0) unexpectedAttributeHAverageUse = (unexpectedAttributeHAverageUse + unexpectedAttributeHEffectivellyUsed) / 2; else unexpectedAttributeHAverageUse = unexpectedAttributeHEffectivellyUsed;
        for (int i = 0; i < unexpectedAttributeHRecycled.length; i++) {
            unexpectedAttributeHRecycled[i] = null;
        }
        neededLength = unexpectedAmbiguousAHFree + unexpectedAmbiguousAHRecycledCount;
        if (neededLength > unexpectedAmbiguousAH.length) {
            if (neededLength > unexpectedAmbiguousAHMaxSize) {
                neededLength = unexpectedAmbiguousAHMaxSize;
                UnexpectedAmbiguousAttributeHandler[] increased = new UnexpectedAmbiguousAttributeHandler[neededLength];
                System.arraycopy(unexpectedAmbiguousAH, 0, increased, 0, unexpectedAmbiguousAH.length);
                unexpectedAmbiguousAH = increased;
                System.arraycopy(unexpectedAmbiguousAHRecycled, 0, unexpectedAmbiguousAH, unexpectedAmbiguousAHFree, unexpectedAmbiguousAHMaxSize - unexpectedAmbiguousAHFree);
                unexpectedAmbiguousAHFree = unexpectedAmbiguousAHMaxSize;
            } else {
                UnexpectedAmbiguousAttributeHandler[] increased = new UnexpectedAmbiguousAttributeHandler[neededLength];
                System.arraycopy(unexpectedAmbiguousAH, 0, increased, 0, unexpectedAmbiguousAH.length);
                unexpectedAmbiguousAH = increased;
                System.arraycopy(unexpectedAmbiguousAHRecycled, 0, unexpectedAmbiguousAH, unexpectedAmbiguousAHFree, unexpectedAmbiguousAHRecycledCount);
                unexpectedAmbiguousAHFree += unexpectedAmbiguousAHRecycledCount;
            }
        } else {
            System.arraycopy(unexpectedAmbiguousAHRecycled, 0, unexpectedAmbiguousAH, unexpectedAmbiguousAHFree, unexpectedAmbiguousAHRecycledCount);
            unexpectedAmbiguousAHFree += unexpectedAmbiguousAHRecycledCount;
        }
        if (unexpectedAmbiguousAHAverageUse != 0) unexpectedAmbiguousAHAverageUse = (unexpectedAmbiguousAHAverageUse + unexpectedAmbiguousAHEffectivellyUsed) / 2; else unexpectedAmbiguousAHAverageUse = unexpectedAmbiguousAHEffectivellyUsed;
        for (int i = 0; i < unexpectedAmbiguousAHRecycled.length; i++) {
            unexpectedAmbiguousAHRecycled[i] = null;
        }
        neededLength = unknownAttributeHFree + unknownAttributeHRecycledCount;
        if (neededLength > unknownAttributeH.length) {
            if (neededLength > unknownAttributeHMaxSize) {
                neededLength = unknownAttributeHMaxSize;
                UnknownAttributeHandler[] increased = new UnknownAttributeHandler[neededLength];
                System.arraycopy(unknownAttributeH, 0, increased, 0, unknownAttributeH.length);
                unknownAttributeH = increased;
                System.arraycopy(unknownAttributeHRecycled, 0, unknownAttributeH, unknownAttributeHFree, unknownAttributeHMaxSize - unknownAttributeHFree);
                unknownAttributeHFree = unknownAttributeHMaxSize;
            } else {
                UnknownAttributeHandler[] increased = new UnknownAttributeHandler[neededLength];
                System.arraycopy(unknownAttributeH, 0, increased, 0, unknownAttributeH.length);
                unknownAttributeH = increased;
                System.arraycopy(unknownAttributeHRecycled, 0, unknownAttributeH, unknownAttributeHFree, unknownAttributeHRecycledCount);
                unknownAttributeHFree += unknownAttributeHRecycledCount;
            }
        } else {
            System.arraycopy(unknownAttributeHRecycled, 0, unknownAttributeH, unknownAttributeHFree, unknownAttributeHRecycledCount);
            unknownAttributeHFree += unknownAttributeHRecycledCount;
        }
        if (unknownAttributeHAverageUse != 0) unknownAttributeHAverageUse = (unknownAttributeHAverageUse + unknownAttributeHEffectivellyUsed) / 2; else unknownAttributeHAverageUse = unknownAttributeHEffectivellyUsed;
        for (int i = 0; i < unknownAttributeHRecycled.length; i++) {
            unknownAttributeHRecycled[i] = null;
        }
        neededLength = attributeVHFree + attributeVHRecycledCount;
        if (neededLength > attributeVH.length) {
            if (neededLength > attributeVHMaxSize) {
                neededLength = attributeVHMaxSize;
                AttributeValidationHandler[] increased = new AttributeValidationHandler[neededLength];
                System.arraycopy(attributeVH, 0, increased, 0, attributeVH.length);
                attributeVH = increased;
                System.arraycopy(attributeVHRecycled, 0, attributeVH, attributeVHFree, attributeVHMaxSize - attributeVHFree);
                attributeVHFree = attributeVHMaxSize;
            } else {
                AttributeValidationHandler[] increased = new AttributeValidationHandler[neededLength];
                System.arraycopy(attributeVH, 0, increased, 0, attributeVH.length);
                attributeVH = increased;
                System.arraycopy(attributeVHRecycled, 0, attributeVH, attributeVHFree, attributeVHRecycledCount);
                attributeVHFree += attributeVHRecycledCount;
            }
        } else {
            System.arraycopy(attributeVHRecycled, 0, attributeVH, attributeVHFree, attributeVHRecycledCount);
            attributeVHFree += attributeVHRecycledCount;
        }
        if (attributeVHAverageUse != 0) attributeVHAverageUse = (attributeVHAverageUse + attributeVHEffectivellyUsed) / 2; else attributeVHAverageUse = attributeVHEffectivellyUsed;
        for (int i = 0; i < attributeVHRecycled.length; i++) {
            attributeVHRecycled[i] = null;
        }
        neededLength = candidateAttributeVHFree + candidateAttributeVHRecycledCount;
        if (neededLength > candidateAttributeVH.length) {
            if (neededLength > candidateAttributeVHMaxSize) {
                neededLength = candidateAttributeVHMaxSize;
                CandidateAttributeValidationHandler[] increased = new CandidateAttributeValidationHandler[neededLength];
                System.arraycopy(candidateAttributeVH, 0, increased, 0, candidateAttributeVH.length);
                candidateAttributeVH = increased;
                System.arraycopy(candidateAttributeVHRecycled, 0, candidateAttributeVH, candidateAttributeVHFree, candidateAttributeVHMaxSize - candidateAttributeVHFree);
                candidateAttributeVHFree = candidateAttributeVHMaxSize;
            } else {
                CandidateAttributeValidationHandler[] increased = new CandidateAttributeValidationHandler[neededLength];
                System.arraycopy(candidateAttributeVH, 0, increased, 0, candidateAttributeVH.length);
                candidateAttributeVH = increased;
                System.arraycopy(candidateAttributeVHRecycled, 0, candidateAttributeVH, candidateAttributeVHFree, candidateAttributeVHRecycledCount);
                candidateAttributeVHFree += candidateAttributeVHRecycledCount;
            }
        } else {
            System.arraycopy(candidateAttributeVHRecycled, 0, candidateAttributeVH, candidateAttributeVHFree, candidateAttributeVHRecycledCount);
            candidateAttributeVHFree += candidateAttributeVHRecycledCount;
        }
        if (candidateAttributeVHAverageUse != 0) candidateAttributeVHAverageUse = (candidateAttributeVHAverageUse + candidateAttributeVHEffectivellyUsed) / 2; else candidateAttributeVHAverageUse = candidateAttributeVHEffectivellyUsed;
        for (int i = 0; i < candidateAttributeVHRecycled.length; i++) {
            candidateAttributeVHRecycled[i] = null;
        }
        neededLength = attributeConcurrentHFree + attributeConcurrentHRecycledCount;
        if (neededLength > attributeConcurrentH.length) {
            if (neededLength > attributeConcurrentHMaxSize) {
                neededLength = attributeConcurrentHMaxSize;
                AttributeConcurrentHandler[] increased = new AttributeConcurrentHandler[neededLength];
                System.arraycopy(attributeConcurrentH, 0, increased, 0, attributeConcurrentH.length);
                attributeConcurrentH = increased;
                System.arraycopy(attributeConcurrentHRecycled, 0, attributeConcurrentH, attributeConcurrentHFree, attributeConcurrentHMaxSize - attributeConcurrentHFree);
                attributeConcurrentHFree = attributeConcurrentHMaxSize;
            } else {
                AttributeConcurrentHandler[] increased = new AttributeConcurrentHandler[neededLength];
                System.arraycopy(attributeConcurrentH, 0, increased, 0, attributeConcurrentH.length);
                attributeConcurrentH = increased;
                System.arraycopy(attributeConcurrentHRecycled, 0, attributeConcurrentH, attributeConcurrentHFree, attributeConcurrentHRecycledCount);
                attributeConcurrentHFree += attributeConcurrentHRecycledCount;
            }
        } else {
            System.arraycopy(attributeConcurrentHRecycled, 0, attributeConcurrentH, attributeConcurrentHFree, attributeConcurrentHRecycledCount);
            attributeConcurrentHFree += attributeConcurrentHRecycledCount;
        }
        if (attributeConcurrentHAverageUse != 0) attributeConcurrentHAverageUse = (attributeConcurrentHAverageUse + attributeConcurrentHEffectivellyUsed) / 2; else attributeConcurrentHAverageUse = attributeConcurrentHEffectivellyUsed;
        for (int i = 0; i < attributeConcurrentHRecycled.length; i++) {
            attributeConcurrentHRecycled[i] = null;
        }
        neededLength = attributeParallelHFree + attributeParallelHRecycledCount;
        if (neededLength > attributeParallelH.length) {
            if (neededLength > attributeParallelHMaxSize) {
                neededLength = attributeParallelHMaxSize;
                AttributeParallelHandler[] increased = new AttributeParallelHandler[neededLength];
                System.arraycopy(attributeParallelH, 0, increased, 0, attributeParallelH.length);
                attributeParallelH = increased;
                System.arraycopy(attributeParallelHRecycled, 0, attributeParallelH, attributeParallelHFree, attributeParallelHMaxSize - attributeParallelHFree);
                attributeParallelHFree = attributeParallelHMaxSize;
            } else {
                AttributeParallelHandler[] increased = new AttributeParallelHandler[neededLength];
                System.arraycopy(attributeParallelH, 0, increased, 0, attributeParallelH.length);
                attributeParallelH = increased;
                System.arraycopy(attributeParallelHRecycled, 0, attributeParallelH, attributeParallelHFree, attributeParallelHRecycledCount);
                attributeParallelHFree += attributeParallelHRecycledCount;
            }
        } else {
            System.arraycopy(attributeParallelHRecycled, 0, attributeParallelH, attributeParallelHFree, attributeParallelHRecycledCount);
            attributeParallelHFree += attributeParallelHRecycledCount;
        }
        if (attributeParallelHAverageUse != 0) attributeParallelHAverageUse = (attributeParallelHAverageUse + attributeParallelHEffectivellyUsed) / 2; else attributeParallelHAverageUse = attributeParallelHEffectivellyUsed;
        for (int i = 0; i < attributeParallelHRecycled.length; i++) {
            attributeParallelHRecycled[i] = null;
        }
        neededLength = attributeDefaultHFree + attributeDefaultHRecycledCount;
        if (neededLength > attributeDefaultH.length) {
            if (neededLength > attributeDefaultHMaxSize) {
                neededLength = attributeDefaultHMaxSize;
                AttributeDefaultHandler[] increased = new AttributeDefaultHandler[neededLength];
                System.arraycopy(attributeDefaultH, 0, increased, 0, attributeDefaultH.length);
                attributeDefaultH = increased;
                System.arraycopy(attributeDefaultHRecycled, 0, attributeDefaultH, attributeDefaultHFree, attributeDefaultHMaxSize - attributeDefaultHFree);
                attributeDefaultHFree = attributeDefaultHMaxSize;
            } else {
                AttributeDefaultHandler[] increased = new AttributeDefaultHandler[neededLength];
                System.arraycopy(attributeDefaultH, 0, increased, 0, attributeDefaultH.length);
                attributeDefaultH = increased;
                System.arraycopy(attributeDefaultHRecycled, 0, attributeDefaultH, attributeDefaultHFree, attributeDefaultHRecycledCount);
                attributeDefaultHFree += attributeDefaultHRecycledCount;
            }
        } else {
            System.arraycopy(attributeDefaultHRecycled, 0, attributeDefaultH, attributeDefaultHFree, attributeDefaultHRecycledCount);
            attributeDefaultHFree += attributeDefaultHRecycledCount;
        }
        if (attributeDefaultHAverageUse != 0) attributeDefaultHAverageUse = (attributeDefaultHAverageUse + attributeDefaultHEffectivellyUsed) / 2; else attributeDefaultHAverageUse = attributeDefaultHEffectivellyUsed;
        for (int i = 0; i < attributeDefaultHRecycled.length; i++) {
            attributeDefaultHRecycled[i] = null;
        }
        neededLength = charactersValidationHFree + charactersValidationHRecycledCount;
        if (neededLength > charactersValidationH.length) {
            if (neededLength > charactersValidationHMaxSize) {
                neededLength = charactersValidationHMaxSize;
                CharactersValidationHandler[] increased = new CharactersValidationHandler[neededLength];
                System.arraycopy(charactersValidationH, 0, increased, 0, charactersValidationH.length);
                charactersValidationH = increased;
                System.arraycopy(charactersValidationHRecycled, 0, charactersValidationH, charactersValidationHFree, charactersValidationHMaxSize - charactersValidationHFree);
                charactersValidationHFree = charactersValidationHMaxSize;
            } else {
                CharactersValidationHandler[] increased = new CharactersValidationHandler[neededLength];
                System.arraycopy(charactersValidationH, 0, increased, 0, charactersValidationH.length);
                charactersValidationH = increased;
                System.arraycopy(charactersValidationHRecycled, 0, charactersValidationH, charactersValidationHFree, charactersValidationHRecycledCount);
                charactersValidationHFree += charactersValidationHRecycledCount;
            }
        } else {
            System.arraycopy(charactersValidationHRecycled, 0, charactersValidationH, charactersValidationHFree, charactersValidationHRecycledCount);
            charactersValidationHFree += charactersValidationHRecycledCount;
        }
        if (charactersValidationHAverageUse != 0) charactersValidationHAverageUse = (charactersValidationHAverageUse + charactersValidationHEffectivellyUsed) / 2; else charactersValidationHAverageUse = charactersValidationHEffectivellyUsed;
        for (int i = 0; i < charactersValidationHRecycled.length; i++) {
            charactersValidationHRecycled[i] = null;
        }
        neededLength = structuredDataValidationHFree + structuredDataValidationHRecycledCount;
        if (neededLength > structuredDataValidationH.length) {
            if (neededLength > structuredDataValidationHMaxSize) {
                neededLength = structuredDataValidationHMaxSize;
                StructuredDataValidationHandler[] increased = new StructuredDataValidationHandler[neededLength];
                System.arraycopy(structuredDataValidationH, 0, increased, 0, structuredDataValidationH.length);
                structuredDataValidationH = increased;
                System.arraycopy(structuredDataValidationHRecycled, 0, structuredDataValidationH, structuredDataValidationHFree, structuredDataValidationHMaxSize - structuredDataValidationHFree);
                structuredDataValidationHFree = structuredDataValidationHMaxSize;
            } else {
                StructuredDataValidationHandler[] increased = new StructuredDataValidationHandler[neededLength];
                System.arraycopy(structuredDataValidationH, 0, increased, 0, structuredDataValidationH.length);
                structuredDataValidationH = increased;
                System.arraycopy(structuredDataValidationHRecycled, 0, structuredDataValidationH, structuredDataValidationHFree, structuredDataValidationHRecycledCount);
                structuredDataValidationHFree += structuredDataValidationHRecycledCount;
            }
        } else {
            System.arraycopy(structuredDataValidationHRecycled, 0, structuredDataValidationH, structuredDataValidationHFree, structuredDataValidationHRecycledCount);
            structuredDataValidationHFree += structuredDataValidationHRecycledCount;
        }
        if (structuredDataValidationHAverageUse != 0) structuredDataValidationHAverageUse = (structuredDataValidationHAverageUse + structuredDataValidationHEffectivellyUsed) / 2; else structuredDataValidationHAverageUse = structuredDataValidationHEffectivellyUsed;
        for (int i = 0; i < structuredDataValidationHRecycled.length; i++) {
            structuredDataValidationHRecycled[i] = null;
        }
        neededLength = dataValidationHFree + dataValidationHRecycledCount;
        if (neededLength > dataValidationH.length) {
            if (neededLength > dataValidationHMaxSize) {
                neededLength = dataValidationHMaxSize;
                DataValidationHandler[] increased = new DataValidationHandler[neededLength];
                System.arraycopy(dataValidationH, 0, increased, 0, dataValidationH.length);
                dataValidationH = increased;
                System.arraycopy(dataValidationHRecycled, 0, dataValidationH, dataValidationHFree, dataValidationHMaxSize - dataValidationHFree);
                dataValidationHFree = dataValidationHMaxSize;
            } else {
                DataValidationHandler[] increased = new DataValidationHandler[neededLength];
                System.arraycopy(dataValidationH, 0, increased, 0, dataValidationH.length);
                dataValidationH = increased;
                System.arraycopy(dataValidationHRecycled, 0, dataValidationH, dataValidationHFree, dataValidationHRecycledCount);
                dataValidationHFree += dataValidationHRecycledCount;
            }
        } else {
            System.arraycopy(dataValidationHRecycled, 0, dataValidationH, dataValidationHFree, dataValidationHRecycledCount);
            dataValidationHFree += dataValidationHRecycledCount;
        }
        if (dataValidationHAverageUse != 0) dataValidationHAverageUse = (dataValidationHAverageUse + dataValidationHEffectivellyUsed) / 2; else dataValidationHAverageUse = dataValidationHEffectivellyUsed;
        for (int i = 0; i < dataValidationHRecycled.length; i++) {
            dataValidationHRecycled[i] = null;
        }
        neededLength = defaultVAttributeHFree + defaultVAttributeHRecycledCount;
        if (neededLength > defaultVAttributeH.length) {
            if (neededLength > defaultVAttributeHMaxSize) {
                neededLength = defaultVAttributeHMaxSize;
                DefaultValueAttributeValidationHandler[] increased = new DefaultValueAttributeValidationHandler[neededLength];
                System.arraycopy(defaultVAttributeH, 0, increased, 0, defaultVAttributeH.length);
                defaultVAttributeH = increased;
                System.arraycopy(defaultVAttributeHRecycled, 0, defaultVAttributeH, defaultVAttributeHFree, defaultVAttributeHMaxSize - defaultVAttributeHFree);
                defaultVAttributeHFree = defaultVAttributeHMaxSize;
            } else {
                DefaultValueAttributeValidationHandler[] increased = new DefaultValueAttributeValidationHandler[neededLength];
                System.arraycopy(defaultVAttributeH, 0, increased, 0, defaultVAttributeH.length);
                defaultVAttributeH = increased;
                System.arraycopy(defaultVAttributeHRecycled, 0, defaultVAttributeH, defaultVAttributeHFree, defaultVAttributeHRecycledCount);
                defaultVAttributeHFree += defaultVAttributeHRecycledCount;
            }
        } else {
            System.arraycopy(defaultVAttributeHRecycled, 0, defaultVAttributeH, defaultVAttributeHFree, defaultVAttributeHRecycledCount);
            defaultVAttributeHFree += defaultVAttributeHRecycledCount;
        }
        if (defaultVAttributeHAverageUse != 0) defaultVAttributeHAverageUse = (defaultVAttributeHAverageUse + defaultVAttributeHEffectivellyUsed) / 2; else defaultVAttributeHAverageUse = defaultVAttributeHEffectivellyUsed;
        for (int i = 0; i < defaultVAttributeHRecycled.length; i++) {
            defaultVAttributeHRecycled[i] = null;
        }
        neededLength = listPatternVHFree + listPatternVHRecycledCount;
        if (neededLength > listPatternVH.length) {
            if (neededLength > listPatternVHMaxSize) {
                neededLength = listPatternVHMaxSize;
                ListPatternValidationHandler[] increased = new ListPatternValidationHandler[neededLength];
                System.arraycopy(listPatternVH, 0, increased, 0, listPatternVH.length);
                listPatternVH = increased;
                System.arraycopy(listPatternVHRecycled, 0, listPatternVH, listPatternVHFree, listPatternVHMaxSize - listPatternVHFree);
                listPatternVHFree = listPatternVHMaxSize;
            } else {
                ListPatternValidationHandler[] increased = new ListPatternValidationHandler[neededLength];
                System.arraycopy(listPatternVH, 0, increased, 0, listPatternVH.length);
                listPatternVH = increased;
                System.arraycopy(listPatternVHRecycled, 0, listPatternVH, listPatternVHFree, listPatternVHRecycledCount);
                listPatternVHFree += listPatternVHRecycledCount;
            }
        } else {
            System.arraycopy(listPatternVHRecycled, 0, listPatternVH, listPatternVHFree, listPatternVHRecycledCount);
            listPatternVHFree += listPatternVHRecycledCount;
        }
        if (listPatternVHAverageUse != 0) listPatternVHAverageUse = (listPatternVHAverageUse + listPatternVHEffectivellyUsed) / 2; else listPatternVHAverageUse = listPatternVHEffectivellyUsed;
        for (int i = 0; i < listPatternVHRecycled.length; i++) {
            listPatternVHRecycled[i] = null;
        }
        neededLength = exceptPatternVHFree + exceptPatternVHRecycledCount;
        if (neededLength > exceptPatternVH.length) {
            if (neededLength > exceptPatternVHMaxSize) {
                neededLength = exceptPatternVHMaxSize;
                ExceptPatternValidationHandler[] increased = new ExceptPatternValidationHandler[neededLength];
                System.arraycopy(exceptPatternVH, 0, increased, 0, exceptPatternVH.length);
                exceptPatternVH = increased;
                System.arraycopy(exceptPatternVHRecycled, 0, exceptPatternVH, exceptPatternVHFree, exceptPatternVHMaxSize - exceptPatternVHFree);
                exceptPatternVHFree = exceptPatternVHMaxSize;
            } else {
                ExceptPatternValidationHandler[] increased = new ExceptPatternValidationHandler[neededLength];
                System.arraycopy(exceptPatternVH, 0, increased, 0, exceptPatternVH.length);
                exceptPatternVH = increased;
                System.arraycopy(exceptPatternVHRecycled, 0, exceptPatternVH, exceptPatternVHFree, exceptPatternVHRecycledCount);
                exceptPatternVHFree += exceptPatternVHRecycledCount;
            }
        } else {
            System.arraycopy(exceptPatternVHRecycled, 0, exceptPatternVH, exceptPatternVHFree, exceptPatternVHRecycledCount);
            exceptPatternVHFree += exceptPatternVHRecycledCount;
        }
        if (exceptPatternVHAverageUse != 0) exceptPatternVHAverageUse = (exceptPatternVHAverageUse + exceptPatternVHEffectivellyUsed) / 2; else exceptPatternVHAverageUse = exceptPatternVHEffectivellyUsed;
        for (int i = 0; i < exceptPatternVHRecycled.length; i++) {
            exceptPatternVHRecycled[i] = null;
        }
        neededLength = boundElementVHFree + boundElementVHRecycledCount;
        if (neededLength > boundElementVH.length) {
            if (neededLength > boundElementVHMaxSize) {
                neededLength = boundElementVHMaxSize;
                BoundElementValidationHandler[] increased = new BoundElementValidationHandler[neededLength];
                System.arraycopy(boundElementVH, 0, increased, 0, boundElementVH.length);
                boundElementVH = increased;
                System.arraycopy(boundElementVHRecycled, 0, boundElementVH, boundElementVHFree, boundElementVHMaxSize - boundElementVHFree);
                boundElementVHFree = boundElementVHMaxSize;
            } else {
                BoundElementValidationHandler[] increased = new BoundElementValidationHandler[neededLength];
                System.arraycopy(boundElementVH, 0, increased, 0, boundElementVH.length);
                boundElementVH = increased;
                System.arraycopy(boundElementVHRecycled, 0, boundElementVH, boundElementVHFree, boundElementVHRecycledCount);
                boundElementVHFree += boundElementVHRecycledCount;
            }
        } else {
            System.arraycopy(boundElementVHRecycled, 0, boundElementVH, boundElementVHFree, boundElementVHRecycledCount);
            boundElementVHFree += boundElementVHRecycledCount;
        }
        if (boundElementVHAverageUse != 0) boundElementVHAverageUse = (boundElementVHAverageUse + boundElementVHEffectivellyUsed) / 2; else boundElementVHAverageUse = boundElementVHEffectivellyUsed;
        for (int i = 0; i < boundElementVHRecycled.length; i++) {
            boundElementVHRecycled[i] = null;
        }
        neededLength = boundStartVHFree + boundStartVHRecycledCount;
        if (neededLength > boundStartVH.length) {
            if (neededLength > boundStartVHMaxSize) {
                neededLength = boundStartVHMaxSize;
                BoundStartValidationHandler[] increased = new BoundStartValidationHandler[neededLength];
                System.arraycopy(boundStartVH, 0, increased, 0, boundStartVH.length);
                boundStartVH = increased;
                System.arraycopy(boundStartVHRecycled, 0, boundStartVH, boundStartVHFree, boundStartVHMaxSize - boundStartVHFree);
                boundStartVHFree = boundStartVHMaxSize;
            } else {
                BoundStartValidationHandler[] increased = new BoundStartValidationHandler[neededLength];
                System.arraycopy(boundStartVH, 0, increased, 0, boundStartVH.length);
                boundStartVH = increased;
                System.arraycopy(boundStartVHRecycled, 0, boundStartVH, boundStartVHFree, boundStartVHRecycledCount);
                boundStartVHFree += boundStartVHRecycledCount;
            }
        } else {
            System.arraycopy(boundStartVHRecycled, 0, boundStartVH, boundStartVHFree, boundStartVHRecycledCount);
            boundStartVHFree += boundStartVHRecycledCount;
        }
        if (boundStartVHAverageUse != 0) boundStartVHAverageUse = (boundStartVHAverageUse + boundStartVHEffectivellyUsed) / 2; else boundStartVHAverageUse = boundStartVHEffectivellyUsed;
        for (int i = 0; i < boundStartVHRecycled.length; i++) {
            boundStartVHRecycled[i] = null;
        }
        neededLength = boundElementConcurrentHFree + boundElementConcurrentHRecycledCount;
        if (neededLength > boundElementConcurrentH.length) {
            if (neededLength > boundElementConcurrentHMaxSize) {
                neededLength = boundElementConcurrentHMaxSize;
                BoundElementConcurrentHandler[] increased = new BoundElementConcurrentHandler[neededLength];
                System.arraycopy(boundElementConcurrentH, 0, increased, 0, boundElementConcurrentH.length);
                boundElementConcurrentH = increased;
                System.arraycopy(boundElementConcurrentHRecycled, 0, boundElementConcurrentH, boundElementConcurrentHFree, boundElementConcurrentHMaxSize - boundElementConcurrentHFree);
                boundElementConcurrentHFree = boundElementConcurrentHMaxSize;
            } else {
                BoundElementConcurrentHandler[] increased = new BoundElementConcurrentHandler[neededLength];
                System.arraycopy(boundElementConcurrentH, 0, increased, 0, boundElementConcurrentH.length);
                boundElementConcurrentH = increased;
                System.arraycopy(boundElementConcurrentHRecycled, 0, boundElementConcurrentH, boundElementConcurrentHFree, boundElementConcurrentHRecycledCount);
                boundElementConcurrentHFree += boundElementConcurrentHRecycledCount;
            }
        } else {
            System.arraycopy(boundElementConcurrentHRecycled, 0, boundElementConcurrentH, boundElementConcurrentHFree, boundElementConcurrentHRecycledCount);
            boundElementConcurrentHFree += boundElementConcurrentHRecycledCount;
        }
        if (boundElementConcurrentHAverageUse != 0) boundElementConcurrentHAverageUse = (boundElementConcurrentHAverageUse + boundElementConcurrentHEffectivellyUsed) / 2; else boundElementConcurrentHAverageUse = boundElementConcurrentHEffectivellyUsed;
        for (int i = 0; i < boundElementConcurrentHRecycled.length; i++) {
            boundElementConcurrentHRecycled[i] = null;
        }
        neededLength = boundElementParallelHFree + boundElementParallelHRecycledCount;
        if (neededLength > boundElementParallelH.length) {
            if (neededLength > boundElementParallelHMaxSize) {
                neededLength = boundElementParallelHMaxSize;
                BoundElementParallelHandler[] increased = new BoundElementParallelHandler[neededLength];
                System.arraycopy(boundElementParallelH, 0, increased, 0, boundElementParallelH.length);
                boundElementParallelH = increased;
                System.arraycopy(boundElementParallelHRecycled, 0, boundElementParallelH, boundElementParallelHFree, boundElementParallelHMaxSize - boundElementParallelHFree);
                boundElementParallelHFree = boundElementParallelHMaxSize;
            } else {
                BoundElementParallelHandler[] increased = new BoundElementParallelHandler[neededLength];
                System.arraycopy(boundElementParallelH, 0, increased, 0, boundElementParallelH.length);
                boundElementParallelH = increased;
                System.arraycopy(boundElementParallelHRecycled, 0, boundElementParallelH, boundElementParallelHFree, boundElementParallelHRecycledCount);
                boundElementParallelHFree += boundElementParallelHRecycledCount;
            }
        } else {
            System.arraycopy(boundElementParallelHRecycled, 0, boundElementParallelH, boundElementParallelHFree, boundElementParallelHRecycledCount);
            boundElementParallelHFree += boundElementParallelHRecycledCount;
        }
        if (boundElementParallelHAverageUse != 0) boundElementParallelHAverageUse = (boundElementParallelHAverageUse + boundElementParallelHEffectivellyUsed) / 2; else boundElementParallelHAverageUse = boundElementParallelHEffectivellyUsed;
        for (int i = 0; i < boundElementParallelHRecycled.length; i++) {
            boundElementParallelHRecycled[i] = null;
        }
        neededLength = boundElementCommonHFree + boundElementCommonHRecycledCount;
        if (neededLength > boundElementCommonH.length) {
            if (neededLength > boundElementCommonHMaxSize) {
                neededLength = boundElementCommonHMaxSize;
                BoundElementCommonHandler[] increased = new BoundElementCommonHandler[neededLength];
                System.arraycopy(boundElementCommonH, 0, increased, 0, boundElementCommonH.length);
                boundElementCommonH = increased;
                System.arraycopy(boundElementCommonHRecycled, 0, boundElementCommonH, boundElementCommonHFree, boundElementCommonHMaxSize - boundElementCommonHFree);
                boundElementCommonHFree = boundElementCommonHMaxSize;
            } else {
                BoundElementCommonHandler[] increased = new BoundElementCommonHandler[neededLength];
                System.arraycopy(boundElementCommonH, 0, increased, 0, boundElementCommonH.length);
                boundElementCommonH = increased;
                System.arraycopy(boundElementCommonHRecycled, 0, boundElementCommonH, boundElementCommonHFree, boundElementCommonHRecycledCount);
                boundElementCommonHFree += boundElementCommonHRecycledCount;
            }
        } else {
            System.arraycopy(boundElementCommonHRecycled, 0, boundElementCommonH, boundElementCommonHFree, boundElementCommonHRecycledCount);
            boundElementCommonHFree += boundElementCommonHRecycledCount;
        }
        if (boundElementCommonHAverageUse != 0) boundElementCommonHAverageUse = (boundElementCommonHAverageUse + boundElementCommonHEffectivellyUsed) / 2; else boundElementCommonHAverageUse = boundElementCommonHEffectivellyUsed;
        for (int i = 0; i < boundElementCommonHRecycled.length; i++) {
            boundElementCommonHRecycled[i] = null;
        }
        neededLength = boundAttributeVHFree + boundAttributeVHRecycledCount;
        if (neededLength > boundAttributeVH.length) {
            if (neededLength > boundAttributeVHMaxSize) {
                neededLength = boundAttributeVHMaxSize;
                BoundAttributeValidationHandler[] increased = new BoundAttributeValidationHandler[neededLength];
                System.arraycopy(boundAttributeVH, 0, increased, 0, boundAttributeVH.length);
                boundAttributeVH = increased;
                System.arraycopy(boundAttributeVHRecycled, 0, boundAttributeVH, boundAttributeVHFree, boundAttributeVHMaxSize - boundAttributeVHFree);
                boundAttributeVHFree = boundAttributeVHMaxSize;
            } else {
                BoundAttributeValidationHandler[] increased = new BoundAttributeValidationHandler[neededLength];
                System.arraycopy(boundAttributeVH, 0, increased, 0, boundAttributeVH.length);
                boundAttributeVH = increased;
                System.arraycopy(boundAttributeVHRecycled, 0, boundAttributeVH, boundAttributeVHFree, boundAttributeVHRecycledCount);
                boundAttributeVHFree += boundAttributeVHRecycledCount;
            }
        } else {
            System.arraycopy(boundAttributeVHRecycled, 0, boundAttributeVH, boundAttributeVHFree, boundAttributeVHRecycledCount);
            boundAttributeVHFree += boundAttributeVHRecycledCount;
        }
        if (boundAttributeVHAverageUse != 0) boundAttributeVHAverageUse = (boundAttributeVHAverageUse + boundAttributeVHEffectivellyUsed) / 2; else boundAttributeVHAverageUse = boundAttributeVHEffectivellyUsed;
        for (int i = 0; i < boundAttributeVHRecycled.length; i++) {
            boundAttributeVHRecycled[i] = null;
        }
        neededLength = boundCandidateAttributeVHFree + boundCandidateAttributeVHRecycledCount;
        if (neededLength > boundCandidateAttributeVH.length) {
            if (neededLength > boundCandidateAttributeVHMaxSize) {
                neededLength = boundCandidateAttributeVHMaxSize;
                BoundCandidateAttributeValidationHandler[] increased = new BoundCandidateAttributeValidationHandler[neededLength];
                System.arraycopy(boundCandidateAttributeVH, 0, increased, 0, boundCandidateAttributeVH.length);
                boundCandidateAttributeVH = increased;
                System.arraycopy(boundCandidateAttributeVHRecycled, 0, boundCandidateAttributeVH, boundCandidateAttributeVHFree, boundCandidateAttributeVHMaxSize - boundCandidateAttributeVHFree);
                boundCandidateAttributeVHFree = boundCandidateAttributeVHMaxSize;
            } else {
                BoundCandidateAttributeValidationHandler[] increased = new BoundCandidateAttributeValidationHandler[neededLength];
                System.arraycopy(boundCandidateAttributeVH, 0, increased, 0, boundCandidateAttributeVH.length);
                boundCandidateAttributeVH = increased;
                System.arraycopy(boundCandidateAttributeVHRecycled, 0, boundCandidateAttributeVH, boundCandidateAttributeVHFree, boundCandidateAttributeVHRecycledCount);
                boundCandidateAttributeVHFree += boundCandidateAttributeVHRecycledCount;
            }
        } else {
            System.arraycopy(boundCandidateAttributeVHRecycled, 0, boundCandidateAttributeVH, boundCandidateAttributeVHFree, boundCandidateAttributeVHRecycledCount);
            boundCandidateAttributeVHFree += boundCandidateAttributeVHRecycledCount;
        }
        if (boundCandidateAttributeVHAverageUse != 0) boundCandidateAttributeVHAverageUse = (boundCandidateAttributeVHAverageUse + boundCandidateAttributeVHEffectivellyUsed) / 2; else boundCandidateAttributeVHAverageUse = boundCandidateAttributeVHEffectivellyUsed;
        for (int i = 0; i < boundCandidateAttributeVHRecycled.length; i++) {
            boundCandidateAttributeVHRecycled[i] = null;
        }
        neededLength = boundAttributeConcurrentHFree + boundAttributeConcurrentHRecycledCount;
        if (neededLength > boundAttributeConcurrentH.length) {
            if (neededLength > boundAttributeConcurrentHMaxSize) {
                neededLength = boundAttributeConcurrentHMaxSize;
                BoundAttributeConcurrentHandler[] increased = new BoundAttributeConcurrentHandler[neededLength];
                System.arraycopy(boundAttributeConcurrentH, 0, increased, 0, boundAttributeConcurrentH.length);
                boundAttributeConcurrentH = increased;
                System.arraycopy(boundAttributeConcurrentHRecycled, 0, boundAttributeConcurrentH, boundAttributeConcurrentHFree, boundAttributeConcurrentHMaxSize - boundAttributeConcurrentHFree);
                boundAttributeConcurrentHFree = boundAttributeConcurrentHMaxSize;
            } else {
                BoundAttributeConcurrentHandler[] increased = new BoundAttributeConcurrentHandler[neededLength];
                System.arraycopy(boundAttributeConcurrentH, 0, increased, 0, boundAttributeConcurrentH.length);
                boundAttributeConcurrentH = increased;
                System.arraycopy(boundAttributeConcurrentHRecycled, 0, boundAttributeConcurrentH, boundAttributeConcurrentHFree, boundAttributeConcurrentHRecycledCount);
                boundAttributeConcurrentHFree += boundAttributeConcurrentHRecycledCount;
            }
        } else {
            System.arraycopy(boundAttributeConcurrentHRecycled, 0, boundAttributeConcurrentH, boundAttributeConcurrentHFree, boundAttributeConcurrentHRecycledCount);
            boundAttributeConcurrentHFree += boundAttributeConcurrentHRecycledCount;
        }
        if (boundAttributeConcurrentHAverageUse != 0) boundAttributeConcurrentHAverageUse = (boundAttributeConcurrentHAverageUse + boundAttributeConcurrentHEffectivellyUsed) / 2; else boundAttributeConcurrentHAverageUse = boundAttributeConcurrentHEffectivellyUsed;
        for (int i = 0; i < boundAttributeConcurrentHRecycled.length; i++) {
            boundAttributeConcurrentHRecycled[i] = null;
        }
        neededLength = boundAttributeParallelHFree + boundAttributeParallelHRecycledCount;
        if (neededLength > boundAttributeParallelH.length) {
            if (neededLength > boundAttributeParallelHMaxSize) {
                neededLength = boundAttributeParallelHMaxSize;
                BoundAttributeParallelHandler[] increased = new BoundAttributeParallelHandler[neededLength];
                System.arraycopy(boundAttributeParallelH, 0, increased, 0, boundAttributeParallelH.length);
                boundAttributeParallelH = increased;
                System.arraycopy(boundAttributeParallelHRecycled, 0, boundAttributeParallelH, boundAttributeParallelHFree, boundAttributeParallelHMaxSize - boundAttributeParallelHFree);
                boundAttributeParallelHFree = boundAttributeParallelHMaxSize;
            } else {
                BoundAttributeParallelHandler[] increased = new BoundAttributeParallelHandler[neededLength];
                System.arraycopy(boundAttributeParallelH, 0, increased, 0, boundAttributeParallelH.length);
                boundAttributeParallelH = increased;
                System.arraycopy(boundAttributeParallelHRecycled, 0, boundAttributeParallelH, boundAttributeParallelHFree, boundAttributeParallelHRecycledCount);
                boundAttributeParallelHFree += boundAttributeParallelHRecycledCount;
            }
        } else {
            System.arraycopy(boundAttributeParallelHRecycled, 0, boundAttributeParallelH, boundAttributeParallelHFree, boundAttributeParallelHRecycledCount);
            boundAttributeParallelHFree += boundAttributeParallelHRecycledCount;
        }
        if (boundAttributeParallelHAverageUse != 0) boundAttributeParallelHAverageUse = (boundAttributeParallelHAverageUse + boundAttributeParallelHEffectivellyUsed) / 2; else boundAttributeParallelHAverageUse = boundAttributeParallelHEffectivellyUsed;
        for (int i = 0; i < boundAttributeParallelHRecycled.length; i++) {
            boundAttributeParallelHRecycled[i] = null;
        }
    }
}
