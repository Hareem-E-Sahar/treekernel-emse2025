package org.servingMathematics.qti.domImpl;

import java.lang.reflect.Constructor;
import java.util.EnumSet;
import org.w3c.dom.Element;
import org.imsglobal.qti.dom.*;

/**
 * TODO Description
 * 
 * @author <a href="mailto:j.kahovec@imperial.ac.uk">Jakub Kahovec</a>
 * @author <a href="mailto:d.may@imperial.ac.uk">Daniel J. R. May</a>
 * @version 0.1, 17 October 2005
 */
public class ExpressionImpl extends ElementImpl implements Expression {

    public ExpressionImpl(Element element) {
        super(element);
    }

    public static Expression createExpression(ElementImpl builder, ExpressionEnum enumExpression) {
        return new ExpressionImpl(builder.createQTIElement(enumExpression.getTagName()));
    }

    public Expression getArgument(int index) {
        Element expressionElement = QTIDOMHelper.getElementChildAtIndex(this, index);
        if (expressionElement != null) {
            return new ExpressionImpl(expressionElement);
        }
        return null;
    }

    public Expression[] getArguments() {
        Element[] elements = QTIDOMHelper.getChildElements(this);
        ExpressionImpl[] expressionElements = new ExpressionImpl[elements.length];
        for (int i = 0; i < expressionElements.length; i++) {
            expressionElements[i] = new ExpressionImpl(elements[i]);
        }
        return expressionElements;
    }

    public Expression addArgument(ExpressionEnum argument) {
        Element appendedChild = appendQTIChild(argument.getTagName());
        return new ExpressionImpl(appendedChild);
    }

    public Expression addArgument(Expression argument) {
        Element appendedChild = appendQTIChild((ExpressionImpl) argument);
        return new ExpressionImpl(appendedChild);
    }

    /**
	 * TODO Add a description
	 * 
	 * @return the expression as an Enum
	 */
    public ExpressionEnum getExpressionAsEnum() {
        for (ExpressionEnum expEnum : ExpressionEnum.values()) {
            if (expEnum.getTagName().equals(this.getNodeName())) {
                return expEnum;
            }
        }
        return null;
    }

    public ExpressionImpl getExpressionImpl() {
        try {
            String className = Character.toUpperCase(getNodeName().charAt(0)) + getNodeName().substring(1);
            String implClassName;
            if (QTIDOMHelper.isMathQTIElement(getNodeName())) {
                implClassName = "org.servingMathematics.mathqti.domImpl." + className + "Impl";
            } else {
                implClassName = "org.servingMathematics.qti.domImpl." + className + "Impl";
            }
            Constructor constructor = Class.forName(implClassName).getConstructors()[0];
            if (constructor != null) {
                return (ExpressionImpl) constructor.newInstance(new Object[] { this });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public Correct getCorrect() {
        if (getNodeName().equals("correct")) {
            return new CorrectImpl(this);
        }
        return null;
    }

    public Lt getLt() {
        if (getNodeName().equals("lt")) {
            return new LtImpl(this);
        }
        return null;
    }

    public Lte getLte() {
        if (getNodeName().equals("lte")) {
            return new LteImpl(this);
        }
        return null;
    }

    public Gt getGt() {
        if (getNodeName().equals("gt")) {
            return new GtImpl(this);
        }
        return null;
    }

    public Gte getGte() {
        if (getNodeName().equals("gte")) {
            return new GteImpl(this);
        }
        return null;
    }

    public Equal getEqual() {
        if (getNodeName().equals("equal")) {
            return new EqualImpl(this);
        }
        return null;
    }

    public Multiple getMultiple() {
        if (getNodeName().equals("multiple")) {
            return new MultipleImpl(this);
        }
        return null;
    }

    public And getAnd() {
        if (getNodeName().equals("and")) {
            return new AndImpl(this);
        }
        return null;
    }

    public Or getOr() {
        if (getNodeName().equals("or")) {
            return new OrImpl(this);
        }
        return null;
    }

    public Variable getVariable() {
        if (getNodeName().equals("variable")) {
            return new VariableImpl(this);
        }
        return null;
    }

    public RandomInteger getRandomInteger() {
        if (getNodeName().equals("randomInteger")) {
            return new RandomIntegerImpl(this);
        }
        return null;
    }

    public RandomFloat getRandomFloat() {
        if (getNodeName().equals("randomFloat")) {
            return new RandomFloatImpl(this);
        }
        return null;
    }

    public BaseValue getBaseValue() {
        if (getNodeName().equals("baseValue")) {
            return new BaseValueImpl(this);
        }
        return null;
    }

    public Not getNot() {
        if (getNodeName().equals("not")) {
            return new NotImpl(this);
        }
        return null;
    }

    public IsNull getIsNull() {
        if (getNodeName().equals("isNull")) {
            return new IsNullImpl(this);
        }
        return null;
    }
}
