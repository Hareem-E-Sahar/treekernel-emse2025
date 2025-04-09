package com.ibm.tuningfork.infra.stream.expression.operations;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import com.ibm.tuningfork.infra.data.TupleType;
import com.ibm.tuningfork.infra.event.EventAttribute;
import com.ibm.tuningfork.infra.event.EventType;
import com.ibm.tuningfork.infra.stream.core.EventStream;
import com.ibm.tuningfork.infra.stream.core.Stream;
import com.ibm.tuningfork.infra.stream.expression.base.Expression;
import com.ibm.tuningfork.infra.stream.expression.base.StreamContext;
import com.ibm.tuningfork.infra.stream.expression.base.StreamExpression;
import com.ibm.tuningfork.infra.stream.expression.base.StringExpression;
import com.ibm.tuningfork.infra.stream.expression.base.TupleExpression;
import com.ibm.tuningfork.infra.stream.expression.literals.StringLiteral;
import com.ibm.tuningfork.infra.stream.expression.types.ExpressionType;
import com.ibm.tuningfork.infra.units.Unit;

/**
 * Operation to create a Stream by iteratively evaluating a TupleExpression
 */
public class StreamCreate extends StreamExpression {

    /** The prefixes to be prepended by default to simple class names to form stream class names */
    private static final String[] STREAM_CLASS_PREFIXES = new String[] { "com.ibm.tuningfork.infra.stream.", "com.ibm.tuningfork.infra.stream.core." };

    /** The name of the stream */
    private StringExpression nameExpr;

    /** The Java class of the stream */
    private StringExpression classExpr;

    /** The tuple expression to iteratively evaluate in the stream */
    private TupleExpression tupleExpr;

    /**
     * Make a new StreamCreate operation
     * @param nameExpr expression evaluating to the name of the Stream that will be created
     * @param classExpr expression evaluating to the name of the class of Stream that will be created
     * @param tupleExpr the TupleExpression to evaluate iteratively
     */
    public StreamCreate(StringExpression nameExpr, StringExpression classExpr, TupleExpression tupleExpr) {
        super(ExpressionType.makeStreamType((EventType) tupleExpr.getType().getStructure()));
        this.nameExpr = nameExpr;
        this.classExpr = classExpr;
        this.tupleExpr = tupleExpr;
        getStreamContext().addStream(this);
    }

    public Stream getStreamValue(StreamContext context) {
        Stream candidate = context.getStreamIfPresent(this);
        if (candidate != null) {
            return candidate;
        }
        String name;
        if (nameExpr == null) {
            name = "_" + System.nanoTime();
        } else {
            name = nameExpr.getStringValue(context);
        }
        String className = classExpr.getStringValue(context);
        Class<?> streamClass = null;
        if (className == null) {
            streamClass = EventStream.class;
        } else {
            if (className.indexOf('.') == -1) {
                int prefixIndex = 0;
                while (streamClass == null && prefixIndex < STREAM_CLASS_PREFIXES.length) {
                    String fullClassName = STREAM_CLASS_PREFIXES[prefixIndex++] + className;
                    try {
                        streamClass = Class.forName(fullClassName);
                    } catch (ClassNotFoundException e) {
                    }
                }
            } else {
                try {
                    streamClass = Class.forName(className);
                } catch (ClassNotFoundException e) {
                }
            }
            if (streamClass == null) {
                throw new IllegalArgumentException("Could not resolve " + className + " needed for stream construction");
            }
        }
        if (!Stream.class.isAssignableFrom(streamClass)) {
            throw new IllegalArgumentException("Class " + streamClass.getName() + " was specified as stream type but is not a subclass of Stream");
        }
        Constructor<?> cons;
        try {
            cons = streamClass.getConstructor(String.class, StreamContext.class, TupleExpression.class, Unit.class);
        } catch (SecurityException e) {
            cons = null;
        } catch (NoSuchMethodException e) {
            cons = null;
        }
        if (cons == null) {
            throw new IllegalArgumentException("Class " + streamClass.getName() + " was specified as stream type but does not have a ForkTalk constructor");
        }
        Throwable exception = null;
        try {
            Stream ans = (Stream) cons.newInstance(name, tupleExpr.getStreamContext(), tupleExpr, getUnitFrom(tupleExpr.getType()));
            if (nameExpr == null) {
                ans.setInvisible(true);
            }
            return ans;
        } catch (InvocationTargetException e) {
            exception = e.getCause();
        } catch (IllegalAccessException e) {
            throw new IllegalArgumentException("Class " + streamClass.getName() + " was specified as stream type but it's ForkTalk constructor is not public");
        } catch (InstantiationException e) {
            throw new IllegalArgumentException("Class " + streamClass.getName() + " was specified as stream type but is abstract");
        } catch (Exception e) {
            exception = e;
        }
        if (exception instanceof RuntimeException) {
            throw (RuntimeException) exception;
        } else if (exception instanceof Error) {
            throw (Error) exception;
        } else {
            throw new IllegalArgumentException(exception.getMessage(), exception);
        }
    }

    public StreamExpression resolve(Expression[] arguments, int depth) {
        StringExpression newName = nameExpr == null ? null : nameExpr.resolve(arguments, depth);
        StringExpression newClass = classExpr.resolve(arguments, depth);
        TupleExpression newTuple = tupleExpr.resolve(arguments, depth);
        if (newName != nameExpr || newClass != classExpr || newTuple != tupleExpr) {
            return new StreamCreate(newName, newClass, newTuple);
        }
        return this;
    }

    public String toString() {
        if (nameExpr == null) {
            return "stream () " + tupleExpr;
        }
        if (classExpr == StringLiteral.MISSING) {
            return "stream (" + nameExpr + ") " + tupleExpr;
        }
        return "stream (" + nameExpr + ", " + classExpr + ") " + tupleExpr;
    }

    /**
     * Get the Unit from an ExpressionType by finding the first numeric field in the special order double, long, int.  This is clearly
     *  a heuristic as the actual Unit that is appropriate for a stream could differ from what is found by this algorithm; however, this
     *  works for major stream types.  If no Unit is found this way Dimensionless is returned
     * @param type the type from which a Unit is to be extracted
     * @return the Unit
     */
    private Object getUnitFrom(ExpressionType type) {
        TupleType toSearch = type.getStructure();
        int intCount = toSearch.getNumberOfInts();
        int longCount = toSearch.getNumberOfLongs();
        int doubleCount = toSearch.getNumberOfDoubles();
        EventAttribute attr = (doubleCount > 0) ? toSearch.getAttributeByGlobalIndex(intCount + longCount) : (longCount > 0) ? toSearch.getAttributeByGlobalIndex(intCount) : (intCount > 0) ? toSearch.getAttributeByGlobalIndex(0) : null;
        return (attr == null) ? Unit.DIMENSIONLESS : attr.getUnit();
    }
}
