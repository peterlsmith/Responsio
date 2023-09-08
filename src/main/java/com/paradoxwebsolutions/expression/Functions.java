package com.paradoxwebsolutions.expression;

import java.util.Arrays;
import java.util.regex.Pattern;

import java.lang.reflect.Method;







/**
 * Provides basic sets of functions that can be registered (if desired).
 */
public class Functions {
    /**
    * Lambda interface used to implement a functional expression which takes a no
    * arguments.
    */
    @FunctionalInterface
    public static interface Function0 {
        /**
        * Evaluate this function.
        *
        * @param context  the context in which the expression is being evaluated
        * @return         an object representing the result of the evaluation.
        * @throws ExpressionError on error
        */
        Object eval(ExpressionContext context) throws ExpressionError;
    }


    /**
    * Lambda interface used to implement a functional expression which takes a single 
    * argument.
    */
    @FunctionalInterface
    public static interface Function1 {
        /**
        * Evaluate this function.
        *
        * @param context  the context in which the expression is being evaluated
        * @param arg1     a value to pass through as an argument to the function
        * @return         an object representing the result of the evaluation.
        * @throws ExpressionError on error
        */
        Object eval(ExpressionContext context, Object arg1) throws ExpressionError;
    }


    /**
    * Lambda interface used to implement a functional expression which takes two 
    * arguments.
    */
    @FunctionalInterface
    public static interface Function2 {
        /**
        * Evaluate this function.
        *
        * @param context  the context in which the expression is being evaluated
        * @param arg1     the first argument to the function
        * @param arg2     the second argument to the function
        * @return         an object representing the result of the evaluation.
        * @throws ExpressionError on error
        */
        Object eval(ExpressionContext context, Object arg1, Object arg2) throws ExpressionError;
    }


    /**
     * Registers default functions.
     * <p>This is used to register core functions that are most likely needed for
     * all expressions.
     */
    public static void registerDefaultFunctions() {
        Parser.registerFunction(Functions.create("float", (context, arg) -> Operators.toFloat(arg)));
        Parser.registerFunction(Functions.create("int", (context, arg) -> Operators.toInt(arg)));
        Parser.registerFunction(Functions.create("not", (context, arg) -> !Operators.toBoolean(arg)));
    }



    /**
     * Registers a basic set of string manipulation functions and operators.
     * <p>This method is not invoked by default - it must be explicity called.
     */
    public static void registerStringFunctions() {

        /* String 'concat' takes a variable number of arguments and requires special handling */

        Parser.registerFunction(
            new Factory("concat") {
                @Override
                public Expression create(Expression ...input) throws ExpressionError {
                    return new NamedExpression("concat") {
                        @Override
                        public Object eval(ExpressionContext context) throws ExpressionError {
                            String[] values = new String[input.length];
                            for (int i = 0; i < input.length; ++i) {
                                values[i] = input[i].eval(context).toString();
                            }
                            return String.join("", values);
                        }
                        @Override
                        public String toString() {
                            return "concat(" + 
                                String.join(",", Arrays.stream(input).map((e) -> e.toString()).toArray(String[]::new))
                                + ")";
                        }
                    };
                }
            }
        );

        Parser.registerFunction(Functions.create("tolower", (context, arg1) -> arg1.toString().toLowerCase()));
        Parser.registerFunction(Functions.create("toupper", (context, arg1) -> arg1.toString().toUpperCase()));
        Parser.registerFunction(Functions.create("length", (context, arg1) -> arg1 == null ? 0 : arg1.toString().length()));

        /* Technically, this is an operator but it behaves like a function in that it needs both sides evaluated */

        Parser.registerOperator(Operators.createBinary("=~", (c, l, r) -> Functions.matches(l.eval(c), r.eval(c))), 3);
        Parser.registerOperator(Operators.createBinary("matches", (c, l, r) -> Functions.matches(l.eval(c), r.eval(c))), 3);
    }
    


    /**
     * Registers a basic set of math functions.
     * <p>This method is not invoked by default - it must be explicity called.
     */
    public static void registerMathFunctions() {

        /* Trigonometric functions */

        Parser.registerFunction(Functions.create("cos", (context, arg1) -> Math.cos(Operators.toFloat(arg1))));
        Parser.registerFunction(Functions.create("sin", (context, arg1) -> Math.sin(Operators.toFloat(arg1))));
        Parser.registerFunction(Functions.create("tan", (context, arg1) -> Math.tan(Operators.toFloat(arg1))));
        Parser.registerFunction(Functions.create("acos", (context, arg1) -> Math.acos(Operators.toFloat(arg1))));
        Parser.registerFunction(Functions.create("asin", (context, arg1) -> Math.asin(Operators.toFloat(arg1))));
        Parser.registerFunction(Functions.create("atan", (context, arg1) -> Math.atan(Operators.toFloat(arg1))));
        Parser.registerFunction(Functions.create("cosh", (context, arg1) -> Math.cosh(Operators.toFloat(arg1))));
        Parser.registerFunction(Functions.create("sinh", (context, arg1) -> Math.sinh(Operators.toFloat(arg1))));
        Parser.registerFunction(Functions.create("tanh", (context, arg1) -> Math.tanh(Operators.toFloat(arg1))));
        Parser.registerFunction(Functions.create("rad", (context, arg1) -> Math.toRadians(Operators.toFloat(arg1))));
        Parser.registerFunction(Functions.create("deg", (context, arg1) -> Math.toDegrees(Operators.toFloat(arg1))));
        Parser.registerFunction(Functions.create("exp", (context, arg1) -> Math.exp(Operators.toFloat(arg1))));
        Parser.registerFunction(Functions.create("log", (context, arg1) -> Math.log(Operators.toFloat(arg1))));
        Parser.registerFunction(Functions.create("sqrt", (context, arg1) -> Math.sqrt(Operators.toFloat(arg1))));

        /* Rounding/conversion functions */

        Parser.registerFunction(Functions.create("floor", (context, arg1) -> Math.floor(Operators.toFloat(arg1))));
        Parser.registerFunction(Functions.create("ceil", (context, arg1) -> Math.ceil(Operators.toFloat(arg1))));
    }



    /**
     * Creates a function factory for functions that take no arguments.
     *
     * @param name  the name of the function
     * @param fn    the interface/lambda function to execute
     * @return a {@link Factory} instance for the supplied function
     */
    public static Factory create(String name, Function0 fn) {
        return new Factory(name) {
            @Override
            public Expression create(Expression ...input) throws ExpressionError {
                return new NamedExpression(name) {
                    @Override
                    public Object eval(ExpressionContext context) throws ExpressionError {
                        return fn.eval(context);
                    }
                    @Override
                    public String toString() {
                        return name + "()";
                    }
                };
            }
        };
    }


    /**
     * Creates a function factory for functions that take one argument.
     *
     * @param name  the name of the function
     * @param fn    the interface/lambda function to execute
     * @return a {@link Factory} instance for the supplied function
     */
    public static Factory create(String name, Function1 fn) {
        return new Factory(name) {
            @Override
            public Expression create(Expression ...input) throws ExpressionError {
                return new NamedExpression(name) {
                    @Override
                    public Object eval(ExpressionContext context) throws ExpressionError {
                        return fn.eval(context, input[0].eval(context));
                    }
                    @Override
                    public String toString() {
                        return name + "(" + input[0].toString() + ")";
                    }
                };
            }
        };
    }


    /**
     * Creates a function factory for functions that take two arguments.
     *
     * @param name  the name of the function
     * @param fn    the interface/lambda function to execute
     * @return a {@link Factory} instance for the supplied function
     */
    public static Factory create(String name, Function2 fn) {
        return new Factory(name) {
            @Override
            public Expression create(Expression ...input) throws ExpressionError {
                return new NamedExpression(name) {
                    @Override
                    public Object eval(ExpressionContext context) throws ExpressionError {
                        return fn.eval(context, input[0].eval(context), input[1].eval(context));
                    }
                    @Override
                    public String toString() {
                        return name + "(" + input[0].toString() + "," + input[1].toString() + ")";
                    }
                };
            }
        };
    }



    /**
     * Regex matching function used in some string operators.
     * 
     * @param left   the operand to match against
     * @param right  the regular expression to match with
     * @return       true if the match succeeds, false otherwise
     */
    private static Boolean matches(Object left, Object right) {
        return Pattern.compile(right.toString()).matcher(left.toString()).find();
    }
}