package com.paradoxwebsolutions.expression;

import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

public class TestParser {

    /**
     * Basic parser constructor test
     */
    @Test public void testConstructor() {

        Parser parser = new Parser();

        assertNotEquals("Created parser", parser, null);
    }


    /**
     * Test parser can parse and evaluate a simple integer value
     */
    @Test public void testParseInteger() throws ExpressionError {

        Parser parser = new Parser();
        ExpressionContext context = new ExpressionContext();

        Expression e = parser.parse("6");
        assertNotEquals("Parsed expression", e, null);

        Object result = e.eval(context);
        assertEquals("Expression result is a Integer", result.getClass(), Integer.class);
        assertEquals("Expression result value is correct", result, Integer.valueOf(6));
    }


    /**
     * Test parser can parse and evaluate a simple integer value
     */
    @Test public void testParseNegativeInteger() throws ExpressionError {

        Parser parser = new Parser();
        ExpressionContext context = new ExpressionContext();

        Expression e = parser.parse("-11");
        assertNotEquals("Parsed expression", e, null);

        Object result = e.eval(context);
        assertEquals("Expression result is a Integer", result.getClass(), Integer.class);
        assertEquals("Expression result value is correct", result, Integer.valueOf(-11));
    }


    /**
     * Test parser can parse and evaluate a simple floating point value
     */
    @Test public void testParseFloat() throws ExpressionError {

        Parser parser = new Parser();
        ExpressionContext context = new ExpressionContext();

        Expression e = parser.parse("6.5");
        assertNotEquals("Parsed expression", e, null);

        Object result = e.eval(context);
        assertEquals("Expression result is a Double", result.getClass(), Double.class);
        assertEquals("Expression result value is correct", result, Double.valueOf(6.5));
    }


    /**
     * Test parser can parse and evaluate a simple floating point value
     */
    @Test public void testParseNegativeFloat() throws ExpressionError {

        Parser parser = new Parser();
        ExpressionContext context = new ExpressionContext();

        Expression e = parser.parse("-3.14");
        assertNotEquals("Parsed expression", e, null);

        Object result = e.eval(context);
        assertEquals("Expression result is a Double", result.getClass(), Double.class);
        assertEquals("Expression result value is correct", result, Double.valueOf(-3.14));
    }


    /**
     * Test parser can parse and evaluate quoted string value
     */
    @Test public void testParseString() throws ExpressionError {

        Parser parser = new Parser();
        ExpressionContext context = new ExpressionContext();

        Expression e = parser.parse("\"hello\"");
        assertNotEquals("Parsed expression", e, null);

        Object result = e.eval(context);
        assertEquals("Expression result is a String", result.getClass(), String.class);
        assertEquals("Expression result value is correct", result, "hello");
    }


    /**
     * Test parser can parse a boolean value
     */
    @Test public void testParseBoolean() throws ExpressionError {

        Parser parser = new Parser();
        ExpressionContext context = new ExpressionContext();

        Expression e = parser.parse("true");
        assertNotEquals("Parsed expression", e, null);

        Object result = e.eval(context);
        assertEquals("Expression result is a Boolean", result.getClass(), Boolean.class);
        assertEquals("Expression result value is correct", result, true);

        e = parser.parse("false");
        assertNotEquals("Parsed expression", e, null);

        result = e.eval(context);
        assertEquals("Expression result is a Boolean", result.getClass(), Boolean.class);
        assertEquals("Expression result value is correct", result, false);
    }


    /**
     * Test parser can parse and evaluate a simple integer addition
     */
    @Test public void testIntegerAdd() throws ExpressionError {

        Parser parser = new Parser();
        ExpressionContext context = new ExpressionContext();

        Expression e = parser.parse("6 + 3");
        assertNotEquals("Parsed expression", e, null);

        Object result = e.eval(context);
        assertEquals("Expression result is a Integer", result.getClass(), Integer.class);
        assertEquals("Expression result value is correct", result, Integer.valueOf(9));
    }


    /**
     * Test parser can parse and evaluate a simple integer subtraction
     */
    @Test public void testIntegerSubtract() throws ExpressionError {

        Parser parser = new Parser();
        ExpressionContext context = new ExpressionContext();

        Expression e = parser.parse("7 - 3");
        assertNotEquals("Parsed expression", e, null);

        Object result = e.eval(context);
        assertEquals("Expression result is a Integer", result.getClass(), Integer.class);
        assertEquals("Expression result value is correct", result, Integer.valueOf(4));
    }


    /**
     * Test parser can parse and evaluate a simple integer multiplication
     */
    @Test public void testIntegerMultiply() throws ExpressionError {

        Parser parser = new Parser();
        ExpressionContext context = new ExpressionContext();

        Expression e = parser.parse("7 * 3");
        assertNotEquals("Parsed expression", e, null);

        Object result = e.eval(context);
        assertEquals("Expression result is a Integer", result.getClass(), Integer.class);
        assertEquals("Expression result value is correct", result, Integer.valueOf(21));
    }


    /**
     * Test parser can parse and evaluate a simple integer division
     */
    @Test public void testIntegerDivide() throws ExpressionError {

        Parser parser = new Parser();
        ExpressionContext context = new ExpressionContext();

        Expression e = parser.parse("7 / 3");
        assertNotEquals("Parsed expression", e, null);

        Object result = e.eval(context);
        assertEquals("Expression result is a Integer", result.getClass(), Integer.class);
        assertEquals("Expression result value is correct", result, Integer.valueOf(2));
    }


    /**
     * Test parser can parse and evaluate a simple expression with parenthesis
     */
    @Test public void testParseParenthesis() throws ExpressionError {

        Parser parser = new Parser();
        ExpressionContext context = new ExpressionContext();

        Expression e = parser.parse("(101)");
        assertNotEquals("Parsed expression", e, null);

        Object result = e.eval(context);
        assertEquals("Expression result is a Integer", result.getClass(), Integer.class);
        assertEquals("Expression result value is correct", result, Integer.valueOf(101));
    }


    /**
     * Test parser can parse and evaluate a simple integer value
     */
    @Test public void testOperatorPrecedence() throws ExpressionError {

        String[] expressions = new String[] {
            "3 + 4 / 2",
            "10 - 4 + (6 * 7 - 2)",
            "(10 + 100 / 5) * 3 + 10"
        };
        Object[] results = new Object[] {
            5,
            46,
            100
        };


        Parser parser = new Parser();
        ExpressionContext context = new ExpressionContext();

        for (int i = 0; i < expressions.length; ++i) {

            Expression e = parser.parse(expressions[i]);
            assertNotEquals("Parsed expression", e, null);

            Object result = e.eval(context);
            assertEquals("Expression result is a Integer", result.getClass(), Integer.class);
            assertEquals("Expression result value is correct", result, results[i]);
        }
    }


    /**
     * Test parser can parse and evaluate a a context value
     */
    @Test public void testParseContext() throws ExpressionError {

        Parser parser = new Parser();
        ExpressionContext context = new ExpressionContext();
        context.set("some_name", "some-value");

        Expression e = parser.parse("some_name");
        assertNotEquals("Parsed expression", e, null);

        Object result = e.eval(context);
        assertEquals("Expression result is a String", result.getClass(), String.class);
        assertEquals("Expression result value is correct", result, "some-value");
    }


    /**
     * Test parser can parse and evaluate a simple list
     */
    @Test public void testParseList() throws ExpressionError {

        Parser parser = new Parser();
        ExpressionContext context = new ExpressionContext();

        Expression e = parser.parse("[1, 2.3, true, \"hello\"]");
        assertNotEquals("Parsed expression", e, null);

        Object result = e.eval(context);
        assertEquals("Expression result value is object array", result.getClass(), Object[].class);

        Object[] array = (Object[]) result;
        assertEquals("Expression result array is correct length", array.length, 4);

        assertEquals("Array element is Integer", array[0].getClass(), Integer.class);
        assertEquals("Array element value is correct", array[0], Integer.valueOf(1));

        assertEquals("Array element is Double", array[1].getClass(), Double.class);
        assertEquals("Array element value is correct", array[1], Double.valueOf(2.3));

        assertEquals("Array element is Boolean", array[2].getClass(), Boolean.class);
        assertEquals("Array element value is correct", array[2], Boolean.valueOf(true));

        assertEquals("Array element is String", array[3].getClass(), String.class);
        assertEquals("Array element value is correct", array[3], "hello");
    }


    /**
     * Test parser can parse and evaluate a simple list
     */
    @Test public void testListIn() throws ExpressionError {

        Parser parser = new Parser();
        ExpressionContext context = new ExpressionContext();

        Expression e = parser.parse("2 in [1, 2, 3, 4]");
        assertNotEquals("Parsed expression", e, null);

        Object result = e.eval(context);
        assertEquals("Expression result is Boolean", result.getClass(), Boolean.class);
        assertEquals("Expression result value is correct", result, true);

        e = parser.parse("8 in [1, 2, 3, 4]");
        assertNotEquals("Parsed expression", e, null);

        result = e.eval(context);
        assertEquals("Expression result is Boolean", result.getClass(), Boolean.class);
        assertEquals("Expression result value is correct", result, false);
    }

}