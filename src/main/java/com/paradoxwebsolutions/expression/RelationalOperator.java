package com.paradoxwebsolutions.expression;



/**
 * Base class for relational (comparison) type operators.
 * <p>This extends the binary operator to provide evaluation and type checking of
 * the expression values. This simplifies the lambda operators somewhat.
 *
 * @author Peter Smith
 */
class RelationalOperator extends BinaryOperator {
    /**
    * Interface for lambda functions that implement relational operators.
    * <p>Note that prior to execution, the left and right expressions have both been evaluated,
    * and the resulting data values are passed through to the function as objects. The function
    * is expected to return a result as an object.
    *
    * @author Peter Smith
    */
    @FunctionalInterface
    public static interface RelationalOperation {
        /**
        * Executes the relational operator with two operands.
        *
        * @param left   the left hand (first) operand
        * @param right  the right hand (second) operand
        * @return       the result of the relation operator (usually a Boolean instance)
        * @throws ExpressionError on error
        */
        Object exec(Object left, Object right) throws ExpressionError;
    }



    /**
     * Creates a RelationalOperator instance.
     *
     * @param name   the name of this relational operator (used in serialization)
     * @param left   the left hand expression
     * @param right  the right hand expression
     * @param relOp  the relation operator function 
     */
    public RelationalOperator(String name, Expression left, Expression right, RelationalOperation relOp) {
        super(name, left, right, (context, l, r) -> {
            /* Evaluate both left and right expressions */

            Object r1 = l.eval(context);
            Object r2 = r.eval(context);

            /* If we have non null values for both, verify they are the same types. This is an imposed rule (like Java). */

            if (r1 != null && r2 != null) {
                Class<?> c1 = r1.getClass();
                Class<?> c2 = r2.getClass();
                if (c1 != c2) {
                    throw new ExpressionError(
                        String.format("Type mismatch : (%s) %s %s (%s) %s", 
                            c1.getSimpleName(),
                            r1.toString(), 
                            name,
                            c2.getSimpleName(),
                            r2.toString()));
                }
            }
            
            /* Execute the relational operator */
            
            return relOp.exec(r1, r2);
        });
    }
}
