package com.paradoxwebsolutions.expression;




/**
 * Base class used for simple unary operators.
 *
 * @author Peter Smith
 */
public class UnaryOperator extends NamedExpression {

    /**
    * Interface for lambda functions that implement unary operators.
    *
    * @author Peter Smith
    */
    @FunctionalInterface
    public static interface UnaryOperation {
        /**
        * Executes the unary operator.
        *
        * @param context  the context in which the expression is being evaluated
        * @param operand  the operand upon which the unary operator performs
        * @return       the result of the relation operator (usually a Boolean instance)
        * @throws ExpressionError on error
        */
        Object exec(ExpressionContext context, Expression operand) throws ExpressionError;
    }



    /** The operand expression */

    private Expression expr;


    /** The unary operator function */

    private UnaryOperation op;



    /**
     * Creates a unary operator instance.
     *
     * @param name   the name of this unary operator (used in serialization)
     * @param expr   the operand expression
     * @param op     the unary operation implementation
     */
    public UnaryOperator(String name, Expression expr, UnaryOperation op) {
        super(name);
        this.expr = expr;
        this.op = op;
    }


    /**
     * Executes the unary operator.
     *
     * @param context  the context in which the expression is being evaluated
     * @return         an object representing the result of the unary operation
     */
    @Override
    public Object eval(ExpressionContext context) throws ExpressionError {
        return op.exec(context, expr);
    }



    /**
     * Converts this expression back to a string.
     *
     * @return a string representation of this expression
     */
    public String toString() {
        return getName() + " " + expr.toString();
    }
}
