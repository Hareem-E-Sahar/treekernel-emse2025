package org.jumpmind.symmetric.io.data.transform;

import java.util.Map;
import org.jumpmind.db.platform.IDatabasePlatform;
import org.jumpmind.extension.IBuiltInExtensionPoint;
import org.jumpmind.symmetric.io.data.DataContext;
import org.jumpmind.util.Context;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import bsh.Interpreter;
import bsh.TargetError;

public class BshColumnTransform implements ISingleValueColumnTransform, IBuiltInExtensionPoint {

    protected final Logger log = LoggerFactory.getLogger(getClass());

    final String INTERPRETER_KEY = String.format("%d.BshInterpreter", hashCode());

    public static final String NAME = "bsh";

    public String getName() {
        return NAME;
    }

    public boolean isExtractColumnTransform() {
        return true;
    }

    public boolean isLoadColumnTransform() {
        return true;
    }

    public String transform(IDatabasePlatform platform, DataContext context, TransformColumn column, TransformedData data, Map<String, String> sourceValues, String newValue, String oldValue) throws IgnoreColumnException, IgnoreRowException {
        try {
            Interpreter interpreter = getInterpreter(context);
            interpreter.set("sqlTemplate", platform.getSqlTemplate());
            interpreter.set("currentValue", newValue);
            interpreter.set("oldValue", oldValue);
            interpreter.set("sourceNodeId", context.getBatch().getNodeId());
            interpreter.set("channelId", context.getBatch().getChannelId());
            for (String columnName : sourceValues.keySet()) {
                interpreter.set(columnName.toUpperCase(), sourceValues.get(columnName));
            }
            Object result = interpreter.eval(column.getTransformExpression());
            if (result != null) {
                return result.toString();
            } else {
                return null;
            }
        } catch (TargetError evalEx) {
            Throwable ex = evalEx.getTarget();
            if (ex instanceof IgnoreColumnException) {
                throw (IgnoreColumnException) ex;
            } else if (ex instanceof IgnoreRowException) {
                throw (IgnoreRowException) ex;
            } else {
                throw new TransformColumnException(ex);
            }
        } catch (Exception ex) {
            if (ex instanceof IgnoreColumnException) {
                throw (IgnoreColumnException) ex;
            } else if (ex instanceof IgnoreRowException) {
                throw (IgnoreRowException) ex;
            } else {
                log.error("Beanshell script error for target column {} on transform {}", column.getTargetColumnName(), column.getTransformId());
                throw new TransformColumnException(ex);
            }
        }
    }

    protected Interpreter getInterpreter(Context context) {
        Interpreter interpreter = (Interpreter) context.get(INTERPRETER_KEY);
        if (interpreter == null) {
            interpreter = new Interpreter();
            context.put(INTERPRETER_KEY, interpreter);
        }
        return interpreter;
    }
}
