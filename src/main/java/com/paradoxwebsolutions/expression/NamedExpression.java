package com.paradoxwebsolutions.expression;




/**
 * Base class used to represent nodes in an expression tree that are named.
 * Named expression nodes generally include operators and functions that are represented by a constant
 * string value (e.g. '+', 'sin', etc) that determines the action to be taken, rather than a data value to be acted upon.
 *
 * @author Peter Smith
 * @see Expression
 */
public abstract class NamedExpression implements Expression {

    /** The name of this expression instance */

    private String name;



    /**
     * Construct a named expression instance.
     *
     * @param  name    the name of this expression instance
     */
    NamedExpression(String name) {
        this.name = name;
    }


    /**
     * Returns the name of this expression.
     * Note that the name is simply the operator or function text, ,e.g. '!=', '+', 'sin', 'random', etc.
     *
     * @return  the name of this expression
     */
    public String getName() {
        return this.name;
    }



}