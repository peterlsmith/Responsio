/**
 * A simple, light-weight, high performance, expression parser and evaluator.  
 * <p>The Expression library provides classes for parsing and evaluating expressions involving
 * number, strings, functions and arithmetic operators. It has no dependencies, provides
 * support for named parameter values (via an expression context), includes a number of built
 * in math and string functions, and can be extended with custom functions and operators.
 * The primary API entry point for the library is the {@link Parser} class.
 * <p>Embedding the expression parser into an application can be done with just a few lines of code:
 * <pre>{@code
 *   import com.paradoxwebsolutions.expression.ExpressionContext;
 *   import com.paradoxwebsolutions.expression.Parser;
 *
 *   ...
 *
 *   String expression = "3.1415 + 2 * cos(theta)";
 *   ExpressionContext context = new ExpressionContext();
 *
 *   ...
 *
 *   new Parser().parse(expression).eval(context);
 *}</pre>
 *
 * Note that for simplicity and performance, the Expression library does not use a formal BNF grammer
 * parser - instead it uses a simplified approach to splitting out the components of an expression, 
 * and recursion to handle construction of the appropriate expression tree for evaluation according
 * to operator precedence.
 * <p>The Expression library is thread safe. The {@link Parser}, as well as the resulting {@link Expression}
 * tree instances used for evaluation may be used concurrently across multiple threads.
 *
 */
package pls.expression;