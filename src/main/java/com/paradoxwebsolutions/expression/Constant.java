package com.paradoxwebsolutions.expression;


/**
 * Expression element representing a constant value.
 * <p>This is used to represent boolean, integer, floating point, and
 * quoted string values within an expression.
 *
 * @author Peter Smith
 */
class Constant implements Expression {

    /** The constant value */

    private Object value;



    /**
     * Creates a new instance with the given value.
     *
     * @param value the constant value
     */
    public Constant(final Object value) {
        this.value = value;
    }



    /**
     * Evaluates the expression.
     * <p>This simply returns the constant value.
     *
     * @param context  the context in which the expression is being evaluated
     * @return         the constant value
     */
    public Object eval(final ExpressionContext context) {
        return value;
    }



    /**
     * Serializes this expression to a string.
     * <p>This produces similar results to Json - string values are double quoted, null values map
     * to the string constant 'null' (no quotes). All other values are output using the 'toString'
     * method.
     *
     * @return a string representation of this constant value. 
     */
    public String toString() {
        if (value == null)
            return "null";
        else if (value instanceof String)
            return "\"" + value + "\"";
        else
            return value.toString();
    }
}