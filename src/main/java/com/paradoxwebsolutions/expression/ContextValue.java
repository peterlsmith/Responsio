package com.paradoxwebsolutions.expression;



/**
 * Expression to retrieve a value from the expression context.
 * 
 * @author Peter Smith
 */
class ContextValue implements Expression {

    /* The name of the context value to retrieve */

    private String name;



    /**
     * Creates an instance of a context value expression.
     *
     * @param name  the name of the context variable whos value is to be returned
     */
    public ContextValue(final String name) {
        assert name != null : "Invalid variable name - cannot be null";
        assert name.length() > 0 : "Invalid variable name - cannot be empty";
        this.name = name;
    }



    /**
     * Evaluates the expression.
     * <p>This simply returns the value of the named context variable.
     *
     * @param context  the context in which the expression is being evaluated
     * @return         the value of the named context variable or null if the named variable
     *                 does not exist
     */
    public Object eval(final ExpressionContext context) throws ExpressionError {
        if (context == null)
            return null;
        else
            return context.get(name);
    }



    /**
     * Serializes this expression to a string.
     *
     * @return a string representation of this context value. 
     */
    public String toString() {
        return name;
    }
}