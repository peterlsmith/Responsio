package com.paradoxwebsolutions.expression;


/**
 * The primary expression interface.
 * <p>All nodes in an expression tree implement this interface. Its purpose is to evalutate
 * the expression (recursively, as needed)
 *
 * @author Peter Smith
 */
public interface Expression {
    /**
     * Evaluates the expression represented by this expression node.
     * <p>
     *
     * @param context  the context in which the expression is being evaluated. The context
     *                 is used to provide an 'environment' that may contain pre-defined
     *                 data values.
     * @return         an object representing the value resulting from the evaluation of the expression
     * @throws ExpressionError if an error occurred during evaluation
     */
    public abstract Object eval(ExpressionContext context) throws ExpressionError;
}