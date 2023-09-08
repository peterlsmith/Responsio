package com.paradoxwebsolutions.expression;


/**
 * Expression element representing a parenthesis containing a sub-expression.
 * <p>This is used simply to 'placehold' parentheses in an expression so we can accurately
 * serialize back to a string.
 */
class Parenthesis implements Expression {

    /** The expression contained within the parentheses */

    private Expression expr;



    /**
     * Creates an parenthesis expression instance containing the given expression.
     *
     * @param expr  the expression contained within the parentheses
     */
    public Parenthesis(final Expression expr) {
        assert expr != null : "Null expression in parentheses";
        this.expr = expr;
    }



    /**
     * Evaluates this expression.
     * <p>This simply returns the result of evaluating the contained expression.
     *
     * @param context  the context in which the expression is being evaluated
     */
    @Override
    public Object eval(final ExpressionContext context) throws ExpressionError {
        return expr.eval(context);
    }



    /**
     * Converts this expression back to a string.
     */
    public String toString() {
        return "(" + expr.toString() + ")";
    }
}