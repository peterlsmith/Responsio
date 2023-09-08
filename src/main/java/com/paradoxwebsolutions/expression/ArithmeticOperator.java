package com.paradoxwebsolutions.expression;



/**
 * Utility class for implementing arithmetic type operators.
 * <p>This extends the binary operator to provide evaluation and type checking of
 * the expression values suitable for arithmetic type operators. This is used in
 * conjunction with {@link ArithmeticOperationI} and {@link ArithmeticOperationF} and
 * simplifies the implementation of arithmetic operations. It also performs type 
 * conversion if the operands are of different types. Note that for arithmetic
 * operations, both the left and right hand arguments are always evalutated (unlike logical
 * operators which may only evaluate the left hand argument depending upon circumstances).
 *
 * @see ArithmeticOperationI
 * @see ArithmeticOperationF
 * @author Peter Smith
 */
class ArithmeticOperator extends BinaryOperator {

    /**
    * Lambda interface used to implement arithmetic operations on integer values
    * <p>This interface is usually used when registering arithmetic operations with the
    * {@link Parser} using the {@link Operators#createArithmetic Operators.createArithmetic} utility method.
    */
    @FunctionalInterface
    public static interface ArithmeticOperationI {
        /**
        * Performs an arthimetic operation on integer values.
        * 
        * @param l  the left hand operand
        * @param r  the right hand operand
        * @return   the result of the operation on the two operands
        * @throws ExpressionError if the operation could not be performed (e.g. divide by zero, etc)
        */
        Object op(int l, int r) throws ExpressionError;
    }



    /**
    * Lambda interface used to implement arithmetic operations on floating point values
    * <p>This interface is usually used when registering arithmetic operations with the
    * {@link Parser} using the {@link Operators#createArithmetic Operators.createArithmetic} utility method.
    */
    @FunctionalInterface
    public static interface ArithmeticOperationF {
        /**
        * Performs an arthimetic operation on floating point values.
        * 
        * @param l  the left hand operand
        * @param r  the right hand operand
        * @return   the result of the operation on the two operands
        * @throws ExpressionError if the operation could not be performed (e.g. divide by zero, etc)
        */
        Object op(double l, double r) throws ExpressionError;
    }



    /**
     * Creates an ArithmeticOperator instance with the given name, operands, and operator.
     *
     * @param name   the name of the operation (this will be used for serialization)
     * @param left   the expression that will be used to generate the left hand operand
     * @param right  the expression that will be used to generate the right hand operand
     * @param iOp    the {@link ArithmeticOperationI} instance that will be used to perform the operation on integer values
     * @param fOp    the {@link ArithmeticOperationF} instance that will be used to perform the operation on floating point values
     */
    public ArithmeticOperator(String name, Expression left, Expression right, ArithmeticOperationI iOp, ArithmeticOperationF fOp) {
        super(name, left, right, (context, l, r) -> {
            /* Evaluate both left and right expressions */

            Object r1 = l.eval(context);
            Object r2 = r.eval(context);

            /* Check that the operands are numeric. We allow integer/float conversion to make life easier */

            if (r1 != null && r2 != null) {
                Class<?> c1 = r1.getClass();
                Class<?> c2 = r2.getClass();
                if (!(r1 instanceof Number) || !(r2 instanceof Number)) {
                    throw new ExpressionError(
                        String.format("Invalid type for arithmetic operator: (%s) %s %s (%s) %s", 
                            c1.getSimpleName(),
                            r1.toString(), 
                            name,
                            c2.getSimpleName(),
                            r2.toString()));
                }

                /* Execute the arithmetic operator */

                try {
                    if (c1 == c2 && c1 == Integer.class)
                        return iOp.op(((Number) r1).intValue(), ((Number) r2).intValue());
                    else
                        return fOp.op(((Number) r1).doubleValue(), ((Number) r2).doubleValue());
                }
                catch (ArithmeticException x) {
                    throw new ExpressionError("Arithmetic error: " + x.getMessage());
                }
            }
            else /* Otherwise error - cannot perform arithmetic ops on nulls */
                throw new ExpressionError(String.format("Invalid null argument passed to '%s' operator", name));
        });
    }
}
