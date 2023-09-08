package com.paradoxwebsolutions.expression;




/**
 * Base class used for binary operators (operators that take two operands).
 */
public class BinaryOperator extends NamedExpression {
    /**
    * Interface for lambda functions or classes that implement binary operators.
    * <p>Implementations of this interface are used to provide support for binary operations -
    * an operation that requires two expressions (left and right) with the 
    * operator occurring in between them, as follows;
    * <pre>{@code
    *        <left expression> <operator> <right expression>
    * }</pre>
    * A binary operation may evalute the left and right expressions
    * as necessary, but should adhere to existing standards when it comes to order of
    * evaluation. For example, an OR binary operation will usually not evaluate the right
    * operand if the left operand evaluates to true. 
    */
    @FunctionalInterface
    public static interface BinaryOperation {
        /**
        * Executes the binary operation with the two arguments and returns the result.
        *
        * @param context  the expression context.
        * @param left     the left hand expression to operate on.
        * @param right    the right hand expression to operate on.
        * @return  the result of the binary operation.
        * @throws ExpressionError if an error occurred during execution of the binary operation.
        */
        Object exec(ExpressionContext context, Expression left, Expression right) throws ExpressionError;
    }


    /** The left hand side of the binary operator expression */

    private Expression left;


    /** The right hand side of the binary operator expression */

    private Expression right;


    /** The binary operator function */

    private BinaryOperation op;



    /**
     * Creates a binary operator instance.
     *
     * @param name   the name of this operator. This is only used for converting an expression back
     *               into string form.
     * @param left   the left hand expression.
     * @param right  the right hand expression.
     * @param op     a binary operation instance.
     * @see BinaryOperation
     */
    public BinaryOperator(String name, Expression left, Expression right, BinaryOperation op) {
        super(name);
        this.left = left;
        this.right = right;
        this.op = op;
    }



    /**
     * Evaluates the binary operation.
     * 
     * @param context  the expression context.
     * @return the result of the binary operation.
     * @throws ExpressionError if an error occurred during execution of the binary operation.
     */
    public Object eval(ExpressionContext context) throws ExpressionError {
        return op.exec(context, left, right);
    }



    /**
     * Converts this expression back to a string.
     *
     * @return a string representation of this expression
     */
    public String toString() {
        return left.toString() + " " + getName() + " " + right.toString();
    }
}
