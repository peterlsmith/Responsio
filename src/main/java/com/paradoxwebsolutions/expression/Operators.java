package com.paradoxwebsolutions.expression;

import java.util.Objects;



/**
 * Useful utility methods for a number of default unary, binary, and relational expression operators.
 *
 * @author Peter Smith
 */
public class Operators {


    /**
     * Registers default operators.
     * <p>This is used to register core operators that are most likely needed for
     * all expressions.
     */
    public static void registerDefaultOperators() {
        Parser.registerOperator(Operators.createRelational("==", (v1, v2) -> Objects.equals(v1, v2)), 8);
        Parser.registerOperator(Operators.createRelational("!=", (v1, v2) -> !Objects.equals(v1, v2)), 8);

        /* Technically, these are relational operators, but we treat them as arithmetic (since they only work on numbers) */

        Parser.registerOperator(Operators.createArithmetic(">", (l, r) -> l > r, (l, r) -> l > r), 9);
        Parser.registerOperator(Operators.createArithmetic(">=", (l, r) -> l >= r, (l, r) -> l >= r), 9);
        Parser.registerOperator(Operators.createArithmetic("<", (l, r) -> l < r, (l, r) -> l < r), 9);
        Parser.registerOperator(Operators.createArithmetic("<=", (l, r) -> l <= r, (l, r) -> l <= r), 9);

        Parser.registerOperator(Operators.createBinary("and", (c, l, r) -> (Operators.toBoolean(l.eval(c)) && Operators.toBoolean(r.eval(c)))), 4);
        Parser.registerOperator(Operators.createBinary("or", (c, l, r) -> (Operators.toBoolean(l.eval(c)) || Operators.toBoolean(r.eval(c)))), 3);

        Parser.registerOperator(Operators.createArithmetic("+", (l, r) -> l + r, (l, r) -> l + r), 11);
        Parser.registerOperator(Operators.createArithmetic("-", (l, r) -> l - r, (l, r) -> l - r), 11);
        Parser.registerOperator(Operators.createArithmetic("*", (l, r) -> l * r, (l, r) -> l * r), 12);
        Parser.registerOperator(Operators.createArithmetic("/", (l, r) -> l / r, (l, r) -> l / r), 12);
        Parser.registerOperator(Operators.createArithmetic("^", (l, r) -> (int) Math.pow(l, r), (l, r) -> Math.pow(l, r)), 13);

        Parser.registerOperator(Operators.createUnary("!", (c, e) -> !Operators.toBoolean(e.eval(c))));
        Parser.registerOperator(Operators.createUnary("-", (c, e) -> Operators.neg(e.eval(c))));

        Parser.registerOperator(Operators.createBinary("in", (c, l, r) -> Operators.in(l.eval(c), r.eval(c))), 9);
    }


    /**
     * Creates a lambda function for creating expresssions representing unary operators.
     *
     * @param name  the name of the operator
     * @param fn    the operator function
     * @return      a UnaryFactory (a lambda function) for creating unary expressions
     */
    public static Factory createUnary(String name, UnaryOperator.UnaryOperation fn) {
        return new Factory(name) {
            @Override
            public Expression create(Expression ...input) throws ExpressionError {
                return new UnaryOperator(name, input[0], fn);
            }
        };
    }



    /**
     * Creates a lambda function for creating expresssions representing binary operators.
     *
     * @param name  the name of the operator
     * @param fn    the operator function
     * @return      a Factory instance for creating binary expressions
     */
    public static Factory createBinary(String name, BinaryOperator.BinaryOperation fn) {
        return new Factory(name) {
            @Override
            public Expression create(Expression ...input) throws ExpressionError {
                return new BinaryOperator(name, input[0], input[1], fn);
            }
        };
    }



    /**
     * Creates an operator factory instance suitable for registering with the Parser.
     * <p>An operator factory is used to create expression instances for a given operator
     * and set of operands.
     * e.g.
     * <pre>{@code 
     *   Factory f = Operators.createArithmetic("+", (l, r) -> l + r, (l, r) -> l + r);
     * }</pre>
     *
     * @param name  the name of the operator
     * @param iOp   the operator function for integer values
     * @param fOp   the operator function for floating point values
     * @return      a Factory instance for creating arithmetic expressions for inserting into the expression tree
     */
    public static Factory createArithmetic(String name, ArithmeticOperator.ArithmeticOperationI iOp, ArithmeticOperator.ArithmeticOperationF fOp) {
        return new Factory(name) {
            @Override
            public Expression create(Expression ...input) throws ExpressionError {
                return new ArithmeticOperator(name, input[0], input[1], iOp, fOp);
            }
        };
    }



    /**
     * Creates a lambda function for creating expresssions representing relational operators.
     *
     * @param name  the name of the operator
     * @param fn    the operator function
     * @return      a BinaryFactory (a lambda function) for creating relational expressions
     */
    public static Factory createRelational(String name, RelationalOperator.RelationalOperation fn) {
        return new Factory(name) {
            @Override
            public Expression create(Expression ...input) throws ExpressionError {
                return new RelationalOperator(name, input[0], input[1], fn);
            }
        };
    }



    /**
     * Converts an expression result to a binary true/false value.
     * <p>The general rule is that non-zero numbers and strings with
     * a non-zero legth are true. Zero, null, and empty strings are false. Boolean
     * values are returned as is.
     *
     * @param value  the value to convert to boolean
     * @return       true or false depending upon the value
     */    
    public static boolean toBoolean(Object value) {
        if (value == null)
            return false;
        else if (value instanceof Boolean)
            return ((Boolean) value).booleanValue();
        else if (value instanceof Integer)
            return ((Integer) value).intValue() != 0;
        else if (value instanceof Double)
            return ((Double) value).doubleValue() != 0;
        else
            return value.toString().length() != 0;
    }



    /**
     * Converts a number (or string representation of a number) to a floating point number.
     *
     * @param value  the value to convert
     * @return       the converted value as a floating point number
     * @throws ExpressionError if the input value could not be converted (e.g. not a valid number)
     */
    public static double toFloat(Object value) throws ExpressionError {
        try {
            if (value instanceof Number)
                return ((Number) value).doubleValue();
            else
                return Double.valueOf(value.toString());
        }
        catch (Exception x) {
            throw new ExpressionError("Cannot convert value to float: " + x.getMessage());
        }
    }


    /**
     * Converts a number (or string representation of a number) to an integer number.
     *
     * @param value  the value to convert
     * @return       the converted value as an integer number
     * @throws ExpressionError if the input value could not be converted (e.g. not a valid number)
     */
    public static int toInt(Object value) throws ExpressionError {
        try {
            if (value instanceof Number)
                return ((Number) value).intValue();
            else
                return Double.valueOf(value.toString()).intValue(); /* Allow parsing of float strings */
        }
        catch (Exception x) {
            throw new ExpressionError("Cannot convert value to integer: " + x.getMessage());
        }
    }


    /**
     * Negates a numeric value.
     *
     * @param value  the value to negate
     * @return       the arithmetic negative value
     * @throws ExpressionError if the provided value was not numeric
     */    
    public static Object neg(Object value) throws ExpressionError {
        if (value == null) throw new ExpressionError("Arithmetic error - negation of null value");

        if (value instanceof Integer)
            return -((Integer) value).intValue();
        else if (value instanceof Double)
            return -((Double) value).doubleValue();

        throw new ExpressionError("Attempt to negate a non-numeric value");
    }
   


    /**
     * Value in list operator.
     * <p>This provides an 'in' operator (similar to that in Python) used for determining whether
     * or not a value exists in a list.
     *
     * @param left    the value to search the list for
     * @param right   the list to search
     * @return true if the value exists in the list, false otherwise
     * @throws ExpressionError if the list is not valid
     */
    public static Boolean in(Object left, Object right) throws ExpressionError {
        if (right == null) throw new ExpressionError("Null list passed to 'in' operator");
        if (right.getClass() != Object[].class) throw new ExpressionError("Invalid list type passed to 'in' operator");

        for (Object value : (Object[]) right) {
            if (Objects.equals(value, left)) return true;
        }
        return false;
    }
}