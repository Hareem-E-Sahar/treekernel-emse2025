package ognl;

import java.lang.reflect.Array;

/**
 * @author Luke Blanshard (blanshlu@netscape.net)
 * @author Drew Davidson (drew@ognl.org)
 */
public class ASTChain extends SimpleNode {

    public ASTChain(int id) {
        super(id);
    }

    public ASTChain(OgnlParser p, int id) {
        super(p, id);
    }

    public void jjtClose() {
        flattenTree();
    }

    protected Object getValueBody(OgnlContext context, Object source) throws OgnlException {
        Object result = source;
        for (int i = 0, ilast = _children.length - 1; i <= ilast; ++i) {
            boolean handled = false;
            if (i < ilast) {
                if (_children[i] instanceof ASTProperty) {
                    ASTProperty propertyNode = (ASTProperty) _children[i];
                    int indexType = propertyNode.getIndexedPropertyType(context, result);
                    if ((indexType != OgnlRuntime.INDEXED_PROPERTY_NONE) && (_children[i + 1] instanceof ASTProperty)) {
                        ASTProperty indexNode = (ASTProperty) _children[i + 1];
                        if (indexNode.isIndexedAccess()) {
                            Object index = indexNode.getProperty(context, result);
                            if (index instanceof DynamicSubscript) {
                                if (indexType == OgnlRuntime.INDEXED_PROPERTY_INT) {
                                    Object array = propertyNode.getValue(context, result);
                                    int len = Array.getLength(array);
                                    switch(((DynamicSubscript) index).getFlag()) {
                                        case DynamicSubscript.ALL:
                                            result = Array.newInstance(array.getClass().getComponentType(), len);
                                            System.arraycopy(array, 0, result, 0, len);
                                            handled = true;
                                            i++;
                                            break;
                                        case DynamicSubscript.FIRST:
                                            index = new Integer((len > 0) ? 0 : -1);
                                            break;
                                        case DynamicSubscript.MID:
                                            index = new Integer((len > 0) ? (len / 2) : -1);
                                            break;
                                        case DynamicSubscript.LAST:
                                            index = new Integer((len > 0) ? (len - 1) : -1);
                                            break;
                                    }
                                } else {
                                    if (indexType == OgnlRuntime.INDEXED_PROPERTY_OBJECT) {
                                        throw new OgnlException("DynamicSubscript '" + indexNode + "' not allowed for object indexed property '" + propertyNode + "'");
                                    }
                                }
                            }
                            if (!handled) {
                                result = OgnlRuntime.getIndexedProperty(context, result, propertyNode.getProperty(context, result).toString(), index);
                                handled = true;
                                i++;
                            }
                        }
                    }
                }
            }
            if (!handled) {
                result = _children[i].getValue(context, result);
            }
        }
        return result;
    }

    protected void setValueBody(OgnlContext context, Object target, Object value) throws OgnlException {
        boolean handled = false;
        for (int i = 0, ilast = _children.length - 2; i <= ilast; ++i) {
            if (i == ilast) {
                if (_children[i] instanceof ASTProperty) {
                    ASTProperty propertyNode = (ASTProperty) _children[i];
                    int indexType = propertyNode.getIndexedPropertyType(context, target);
                    if ((indexType != OgnlRuntime.INDEXED_PROPERTY_NONE) && (_children[i + 1] instanceof ASTProperty)) {
                        ASTProperty indexNode = (ASTProperty) _children[i + 1];
                        if (indexNode.isIndexedAccess()) {
                            Object index = indexNode.getProperty(context, target);
                            if (index instanceof DynamicSubscript) {
                                if (indexType == OgnlRuntime.INDEXED_PROPERTY_INT) {
                                    Object array = propertyNode.getValue(context, target);
                                    int len = Array.getLength(array);
                                    switch(((DynamicSubscript) index).getFlag()) {
                                        case DynamicSubscript.ALL:
                                            System.arraycopy(target, 0, value, 0, len);
                                            handled = true;
                                            i++;
                                            break;
                                        case DynamicSubscript.FIRST:
                                            index = new Integer((len > 0) ? 0 : -1);
                                            break;
                                        case DynamicSubscript.MID:
                                            index = new Integer((len > 0) ? (len / 2) : -1);
                                            break;
                                        case DynamicSubscript.LAST:
                                            index = new Integer((len > 0) ? (len - 1) : -1);
                                            break;
                                    }
                                } else {
                                    if (indexType == OgnlRuntime.INDEXED_PROPERTY_OBJECT) {
                                        throw new OgnlException("DynamicSubscript '" + indexNode + "' not allowed for object indexed property '" + propertyNode + "'");
                                    }
                                }
                            }
                            if (!handled) {
                                OgnlRuntime.setIndexedProperty(context, target, propertyNode.getProperty(context, target).toString(), index, value);
                                handled = true;
                                i++;
                            }
                        }
                    }
                }
            }
            if (!handled) {
                target = _children[i].getValue(context, target);
            }
        }
        if (!handled) {
            _children[_children.length - 1].setValue(context, target, value);
        }
    }

    public boolean isSimpleNavigationChain(OgnlContext context) throws OgnlException {
        boolean result = false;
        if ((_children != null) && (_children.length > 0)) {
            result = true;
            for (int i = 0; result && (i < _children.length); i++) {
                if (_children[i] instanceof SimpleNode) {
                    result = ((SimpleNode) _children[i]).isSimpleProperty(context);
                } else {
                    result = false;
                }
            }
        }
        return result;
    }

    public String toString() {
        String result = "";
        if ((_children != null) && (_children.length > 0)) {
            for (int i = 0; i < _children.length; i++) {
                if (i > 0) {
                    if (!(_children[i] instanceof ASTProperty) || !((ASTProperty) _children[i]).isIndexedAccess()) {
                        result = result + ".";
                    }
                }
                result += _children[i].toString();
            }
        }
        return result;
    }
}
