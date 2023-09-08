package com.paradoxwebsolutions.expression;

import java.util.Arrays;

/**
 * Expression element representing a list value.
 * <p>Within an expression, a list is represented using square brackets, e.g.
 * <pre>
 *   [ 'hello', 3 + 2 * 4, 3.14159 ]
 * </pre>
 */
class List implements Expression {

    /** The list values (expressions that must be evaluated) */

    private Expression[] values;



    /**
     * Creates a new list expression instance with the given values.
     *
     * @param values  an array of expressions representing the values in the list
     */
    public List(final Expression[] values) {
        this.values = values;
    }



    /**
     * Evaluates this list.
     * <p>This will evaluate each value in the list in turn, returning all the resulting
     * values as an object array.
     *
     * @param context  the context in which the expression is being evaluated
     * @return         the evaluated list
     */
    public Object eval(final ExpressionContext context) throws ExpressionError {
        Object[] list = new Object[values.length];
        for (int i = 0; i < values.length; ++i) list[i] = values[i].eval(context);
        return list;
    }



    /**
     * Serializes this expression to a string.
     *
     * @return a string representation of this context value. 
     */
    public String toString() {
        if (values == null)
            return "null";
        else
            return "[" 
                + String.join(",", Arrays.stream(values).map((value) -> value.toString()).toArray(String[]::new))
                + "]";
    }
}