package org.elf.common;

import java.util.*;
import java.util.regex.*;

/**
 * Clases con diversas utilidades
 * 
 * @author  <a href="mailto:logongas@users.sourceforge.net">Lorenzo Gonz�lez</a>
 */
public class MiscUtil {

    /**
	 * Constructor por defecto privado para evitar que se instance la clase
	 */
    private MiscUtil() {
    }

    ;

    /**
	 * Evalua una expresi�n variables del tipo ${nombre_valor}
	 * @param expression Expresi�n a evaluar
	 * @param variableEvaluator Objeto que nos permite obtener el valor de la variable 
	 * @return String con la expresi�n evaluada
	 */
    public static String evaluateExpression(String expression, VariableEvaluator variableEvaluator) {
        String evaluatedExpression;
        if ((expression != null) && (expression.indexOf("${") >= 0)) {
            Pattern pattern = Pattern.compile("\\$\\{[a-zA-Z_\\._][0-9a-zA-Z_\\._]*\\}", Pattern.CASE_INSENSITIVE);
            Matcher matcher = pattern.matcher(expression);
            StringBuffer sb = new StringBuffer();
            int ultimoTrozo = 0;
            while (matcher.find() == true) {
                sb.append(expression.substring(ultimoTrozo, matcher.start()));
                String variableName = matcher.group().substring(2, matcher.group().length() - 1);
                Object variableValue = variableEvaluator.getVariableValue(variableName);
                sb.append(variableValue);
                ultimoTrozo = matcher.end();
            }
            sb.append(expression.substring(ultimoTrozo, expression.length()));
            evaluatedExpression = sb.toString();
        } else {
            evaluatedExpression = expression;
        }
        return evaluatedExpression;
    }

    /**
         * Transforma un n�mero en un Strign a�adiendo ceros
         */
    public static String numberToStringZeroPadding(long number, int length) {
        StringBuffer sb = new StringBuffer(String.valueOf(number));
        while (sb.length() < length) {
            sb.insert(0, "0");
        }
        return sb.toString();
    }

    /**
     * Compara los valores de dos listas.
     * @param list1 1� Lista a comparar
     * @param list2 2� Lista a comparar
     * @param onlyList1 Elementos que solo aparecen en la 1� Lista
     * @param onlyList2 Elementos que solo aparecen en la 2� Lista
     * @param bothList Elementos que aparecen en ambas listas
     */
    public static void compareLists(List<Object> list1, List<Object> list2, List<Object> onlyList1, List<Object> onlyList2, List<Object> bothList) {
        if (list1 == null) {
            throw new RuntimeException("El argumento 'list1' no puede ser null");
        }
        if (list2 == null) {
            throw new RuntimeException("El argumento 'list2' no puede ser null");
        }
        if (onlyList1 == null) {
            throw new RuntimeException("El argumento 'onlyList1' no puede ser null");
        }
        if (onlyList2 == null) {
            throw new RuntimeException("El argumento 'onlyList2' no puede ser null");
        }
        if (bothList == null) {
            throw new RuntimeException("El argumento 'bothList' no puede ser null");
        }
        for (int i = 0; i < list1.size(); i++) {
            if (list2.contains(list1.get(i)) == true) {
                bothList.add(list1.get(i));
            } else {
                onlyList1.add(list1.get(i));
            }
        }
        for (int i = 0; i < list2.size(); i++) {
            if (list1.contains(list2.get(i)) == false) {
                onlyList2.add(list2.get(i));
            }
        }
    }
}
