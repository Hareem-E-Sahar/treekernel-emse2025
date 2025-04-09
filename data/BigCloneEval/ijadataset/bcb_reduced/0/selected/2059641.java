package com.nhncorp.usf.core.ognl;

import java.lang.reflect.Array;
import java.util.Map;
import ognl.*;

/**
 * The Class OgnlArrayPropertyAccessor.
 *
 * @author MiddleWare Platform Development Team
 */
class OgnlArrayPropertyAccessor implements PropertyAccessor {

    /**
     * Instantiates a new ognl array property accessor.
     */
    OgnlArrayPropertyAccessor() {
        super();
    }

    /**
     * {@inheritDoc}
     */
    @SuppressWarnings("unchecked")
    public Object getProperty(Map context, Object target, Object name) throws OgnlException {
        if (name instanceof Number) {
            int i = ((Number) name).intValue();
            int len = Array.getLength(target);
            Object value = null;
            if (i < len) {
                value = Array.get(target, i);
            }
            if (value == null) {
                value = OgnlCallExecutor.getDefaultInstance(target.getClass().getComponentType());
                setElement((OgnlContext) context, target, i, value);
            }
            return value;
        } else {
            throw new NoSuchPropertyException(target, name);
        }
    }

    /**
     * {@inheritDoc}
     */
    @SuppressWarnings("unchecked")
    public void setProperty(Map context, Object target, Object name, Object value) throws OgnlException {
        OgnlContext ctx = (OgnlContext) context;
        if (name instanceof Number) {
            TypeConverter converter = ctx.getTypeConverter();
            Object convertedValue;
            convertedValue = converter.convertValue(ctx, target, null, name.toString(), value, target.getClass().getComponentType());
            int i = ((Number) name).intValue();
            setElement(ctx, target, i, convertedValue);
        } else {
            throw new NoSuchPropertyException(target, name);
        }
    }

    /**
     * {@inheritDoc}
     */
    public String getSourceAccessor(OgnlContext ognlContext, Object obj, Object obj1) {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    public String getSourceSetter(OgnlContext ognlContext, Object obj, Object obj1) {
        return null;
    }

    /**
     * Sets the element.
     *
     * @param ctx    the ctx
     * @param target the target
     * @param index  the index
     * @param value  the value
     * @throws OgnlException the ognl exception
     */
    private void setElement(OgnlContext ctx, Object target, int index, Object value) throws OgnlException {
        int len = Array.getLength(target);
        if (index >= 0 && index < len) {
            Array.set(target, index, value);
        } else if (index < 0) {
            throw new ArrayIndexOutOfBoundsException();
        } else {
            Object newArray = Array.newInstance(target.getClass().getComponentType(), index + 1);
            System.arraycopy(target, 0, newArray, 0, len);
            Array.set(newArray, index, value);
            Node parent = ctx.getCurrentNode().jjtGetParent();
            Node pparent = parent.jjtGetParent();
            String parentExpr = pparent.toString();
            int lastTokenPos = parentExpr.lastIndexOf('[');
            parentExpr = parentExpr.substring(0, lastTokenPos);
            Ognl.setValue(parentExpr, ctx.getRoot(), newArray);
        }
    }
}
