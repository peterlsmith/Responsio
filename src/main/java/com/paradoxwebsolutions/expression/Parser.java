package com.paradoxwebsolutions.expression;


import java.util.Arrays;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;




/**
 * The expression parser.
 * <p>This class provides the primary API of the Expression library. Instances of this class are used
 * to parse string expressions into an expression tree, suitable for evaluation. At a very basic level, an expression 
 * has the form:
 * <pre>{@code
 *     <expression> = <value> | <string> | <context value>
 *
 *     <expression> = <unary operator> <expression>
 *
 *     <expression> = <expression> <binary operator> <expression>
 *
 *     <expression> = <function name>([<expression>[,...]])
 * }</pre>
 * Unary operators work on a single operand, and include - (negation) and
 * !(boolean not). Binary operators require a left and right hand operand to evaluate against, and include the usual
 * arithmetic operators (+, -, *, /) and logic operators (and, or), and comparison operators (==, !=)
 * <p>Examples:
 * <pre>{@code
 *    3
 *    -10
 *    8 + 14 / 2 
 *    2 + -2
 * }</pre>
 * The usual operator precedence rules apply, and parentheses can be used to contol evaluation order, e.g.
 * <pre>{@code
 *
 *    (8 + 14) / 2 
 * }</pre>
 * Functions are also supported.
 * <pre>{@code
 *
 *     <expression> = <function name>([<expression>[,...]])
 * }</pre>
 * <p>Examples:
 * <pre>{@code
 *     arccos(16 / 71)
 *     concat("hello", "world")
 * }</pre>
 *
 * For more details on operator precedence, refer to 
 * https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Operators/Operator_Precedence#table.
 * <p>Note that the Parser class uses no local storage and is therefore thread safe - a single
 * instance may be used simultaneously in multiple threads.
 *
 * <p>Embedding the expression parser into an application can be done with just a few lines of code:
 * <pre>{@code
 *   import pls.expression.Parser;
 *
 *   ...
 *
 *   String expression = "3 + 2 * 4";
 *   new Parser().parse(expression).eval(null);
 *}</pre>
 * If context values are needed, an expression context can be configured and used during evaluation:
 * <pre>{@code
 *   import pls.expression.ExpressionContext;
 *   import pls.expression.Parser;
 *
 *   ...
 *
 *   String expression = "3.1415 + 2 * cos(theta)";
 *   ExpressionContext context = new ExpressionContext();
 *   context.set('theta', 0.1145);
 *   ...
 *
 *   new Parser().parse(expression).eval(context);
 *}</pre>

 */
public class Parser {


    /**
     * Private enumeration used by the parser to represent the type of an expression token.
     * <p>An expression is parsed by splitting it up into tokens. Each token is evaluated
     * to determine its type, such as a number, quoted string, operator, etc.
     */
    private enum TokenType {
        /** A quoted string token */

        STRING,

        /** An integer value token */

        INTEGER,

        /** A floating point token */

        FLOAT,

        /** A boolean token */

        BOOL,

        /** A null value token */

        NULL,

        /** An opening parenthesis */

        BEGINPAREN,

        /** An closing parenthesis */

        ENDPAREN,

        /** An opening list bracket */

        BEGINLIST,

        /** An closing list bracket */

        ENDLIST,

        /** A comma */

        COMMA,

        /** An operator token */

        OPERATOR,


        /** A constant string token */

        TOKEN,

        /** End of stream */

        EOS
    };


    /** Lookup table of binary operators */

    private static Map<String, Factory> biOperatorMap = new HashMap<String, Factory>();


    /** Operator precedence values */

    private static Map<String, Integer> operatorPrecedence = new HashMap<String, Integer>();


    /** Lookup table of unary operators */

    private static Map<String, Factory> uniOperatorMap = new HashMap<String, Factory>();


    /** Lookup table of named functions */

    private static Map<String, Factory> functionMap = new HashMap<String, Factory>();


    /** Regular expression used to identify double quoted string constants */

    private static Pattern doubleQuotes = Pattern.compile("\"(\\\\[^\\\\]|[^\"])*\"");


    /** Regular expression used to identify tokens to pad with spaces */

    private static Pattern expansions = Pattern.compile("(\\(|\\)|\\[|\\]|==|!=|=~|>=|>|<=|<|,|\\+|-|\\*|/|!|@|#|\\$|%|^|&)");


    /** Regular expression used to identify integer tokens */

    private static Pattern integerPattern = Pattern.compile("\\d+");


    /** Regular expression used to identify floating point tokens */

    private static Pattern floatPattern = Pattern.compile("\\d+(\\.\\d+)?");


    /** Regular expression used to identify string constants (unquoted strings) */

    private static Pattern constantPattern = Pattern.compile("^_\\d+$");


    /** List of tokens that trigger an exit from the current expression converter level */

    private static List<TokenType> terminals = Arrays.asList(TokenType.COMMA, TokenType.ENDPAREN, TokenType.ENDLIST, TokenType.EOS);



    /**
     * Registers default operators and functions.
     * <p>This is used to register the core operators and functions that are most likely needed for
     * all expressions.
     */
    static {
        Operators.registerDefaultOperators();
        Functions.registerDefaultFunctions();
    }



    /**
     * Registers a named binary operator.
     * This method is used to register new binary operators (only binary operators have precedence).
     * 
     * @param precedence  the precedence level (refer to Javascript operator precedence levels)
     * @param factory     a factory that can be used to create an instance of the operator
     */
    public static void registerOperator(Factory factory, int precedence) {
        biOperatorMap.put(factory.getName(), factory);
        operatorPrecedence.put(factory.getName(), precedence);
    }



    /**
     * Registers a named unary operator.
     * <p>This method is used to register new unary operators that the parser will then recognize, e.g.
     * <pre>{@code
     *  Factory f = Operators.createUnary("@", (c, e) -> Operators.toInt(e.eval(context)) + 5);
     *  Parser.registerOperator(f);
     * }</pre>
     * Note that unary operators have no explicit precedence - they are all implicitly higher than all
     * binary operators (this simplifies the conversion logic).
     *
     * @param factory     a factory instance that can be used to create an instance of the operator
     */
    public static void registerOperator(Factory factory) {
        uniOperatorMap.put(factory.getName(), factory);
    }
    


    /**
     * Register a named function.
     * <p>This method is used to register new functions that the parser will then recognize, e.g.
     * <pre>{@code
     *  Factory f = Functions.create("sqrt", (context, v) -> Math.sqrt(Operators.toFloat(v)));
     *  Parser.registerFunction(f);
     * }</pre>
     *
     * @param factory     a factory instance that can be used to create an instance of the function
     */
    public static void registerFunction(Factory factory) {
        functionMap.put(factory.getName(), factory);
    }



    /**
     * Parser an expression string.
     * This parses a string expression and returns an expression instance that can be used to evaluate it.
     *
     * @param expression  the string expression to parse.
     * @return an Expression instance that can be used to evaluate the expression.
     * @throws ExpressionError if an error occurred during parsing.
     * @see Expression
     */
    public Expression parse(String expression) throws ExpressionError {
        /*
         * First thing to do is process string constants (quoted strings), since these may contain spaces that
         * would impact the tokenization. We temporarily pull them out and store them, replacing them with a
         * tag that can be used to reinsert later, once tokenization is complete.
         */
        List<String> constants = new ArrayList<String>();
        Matcher m1 = doubleQuotes.matcher(expression);
        String expr = m1.replaceAll((match) -> String.format( " _%d ", constants.size(), constants.add(m1.group())));


        /* Next, expand (insert spaces) around various character sequences to make tokenization easier */

        Matcher m3 = expansions.matcher(expr);
        expr = m3.replaceAll((match) -> String.format(" %s ", m3.group(1)));


        /* Now tokenize and re-insert quoted strings */

        String[] tokens = Arrays.stream(expr.split("\\s+"))
            .map((s) -> (constantPattern.matcher(s).matches() ? constants.get(Integer.parseInt(s.substring(1))) : s))
            .filter((s) -> s.length() > 0)
            .toArray(String[]::new);


        /* Finally, convert the token stream into an expression */

        return this.convert(new TokenStream(tokens));
    }



    /**
     * Converts a tokenized expression into an expression tree.
     * <p>This method does the bulk of the work of converting a stream of tokens (from the
     * expression) into an {@link Expression} tree. It encodes the allowed syntax - constants,
     * operators, functions, lists, argument lists, etc.
     *
     * @param stream  the token stream to convert
     * @return an Expression instance that can be used to evaluate the expression.
     * @throws ExpressionError if an error occurred during parsing.
     */
    private Expression convert(final TokenStream stream) throws ExpressionError {

        /* Stores for the expressions and operators */
        
        List<Expression> exprs = new ArrayList<Expression>();
        List<String> ops = new ArrayList<String>();
        String unaryOp = null;

        TokenType type;


        /*
         * Check to see if the next token is a terminal (e.g. comma, closing parenthesis/list, end of stream). If a terminal,
         * we exit the loop here.
         */
        while (!terminals.contains(type = getTokenType(stream.peek()))) {

            /* Consume the token and process */

            String token = stream.next();
            Expression expr = null;

            switch (type) {
                /* A plain (unquoted) string value is either the start of a function or a context variable */

                case TOKEN: {
                    if (getTokenType(stream.peek()) == TokenType.BEGINPAREN) {

                        /* This appears to be a function so verify */

                        if (!functionMap.containsKey(token)) throw new ExpressionError(String.format("Unsupported function '%s'", token));
                        stream.next(); /* Consume the opening paren */


                        /* Get the (possibly empty) argument list */

                        List<Expression> arguments = new ArrayList<Expression>();
                        while ((expr = convert(stream)) != null) {
                            arguments.add(expr);

                            if (getTokenType(stream.peek()) == TokenType.ENDPAREN) break;
                            if (getTokenType(stream.next()) != TokenType.COMMA) throw new ExpressionError("Unexpected token in argument list - expected comma");
                        }


                        /* An argument list is complete when a closing paren is found */

                        type = getTokenType(stream.next());
                        if (type != TokenType.ENDPAREN) {
                            throw new ExpressionError(String.format("Invalid argument list for function %s", token));
                        }
                        expr = functionMap.get(token).create(arguments.toArray(new Expression[arguments.size()]));
                    }
                    else { /* This must be a context variable */
                        expr = new ContextValue(token);
                    }
                }
                break;

                case OPERATOR: {
                    if (exprs.size() == ops.size()) { /* Check for special case of a unary operator */
                        if (!uniOperatorMap.containsKey(token)) throw new ExpressionError(String.format("Unexpected unary operator '%s'", token));
                        unaryOp = token;
                    }
                    else { /* We have an expression and are expecting a binary operator */

                        if (!biOperatorMap.containsKey(token)) throw new ExpressionError(String.format("Unsupported operator '%s'", token));

                        /* We always expect one more expression than operators at this point */

                        if (exprs.size() != ops.size() + 1) throw new ExpressionError(String.format("Unexpected operator '%s'", token));
                        ops.add(token);
                    }
                }
                break;

                case BEGINPAREN: {
                    /*
                     * This marks the start of a sub-expression. We handle it with recursion. Note that we wrap the sub-expression
                     * in a 'paren' expression which has no impact operationally - it is just a pass thru. However, it does allow
                     * us to preserve the parenthesis when we convert back to a string.
                     */
                    expr = convert(stream);
                    if (expr == null) throw new ExpressionError("Empty expression following parenthesis");
                    
                    /* Pop the terminating token and verify it was a closing parenthesis */

                    type = getTokenType(stream.next());
                    if (type != TokenType.ENDPAREN) throw new ExpressionError("Invalid expression following parenthesis - not terminated with ')'");

                    expr = new Parenthesis(expr);
                }
                break;

                case BEGINLIST: {
                    /*
                     * This marks the start of a list of sub-expressions.
                     */
                    List<Expression> elements = new ArrayList<Expression>();
                    while ((expr = convert(stream)) != null) {
                        elements.add(expr);

                        if (getTokenType(stream.peek()) == TokenType.ENDLIST) break;
                        if (getTokenType(stream.next()) != TokenType.COMMA) throw new ExpressionError("Unexpected token in list - expected comma");
                    }

                    /* An argument list is complete when a closing paren is found */

                    type = getTokenType(stream.next());
                    if (type != TokenType.ENDLIST) {
                        throw new ExpressionError(String.format("Invalid argument list for function %s", token));
                    }

                    expr = new com.paradoxwebsolutions.expression.List(elements.toArray(new Expression[elements.size()]));
                }
                break;

                /* Constant expressions */

                case NULL: expr = new Constant(null); break;
                case STRING: expr = new Constant(token.substring(1, token.length() - 1)); break;
                case INTEGER: expr = new Constant(Integer.parseInt(token)); break;
                case FLOAT: expr = new Constant(Double.parseDouble(token)); break;
                case BOOL: expr = new Constant(Boolean.parseBoolean(token)); break;
            }


            /* If we have a new expression, check it matches the expected number of operators */

            if (expr != null) {
                if (exprs.size() != ops.size())
                    throw new ExpressionError(String.format("Unexpected expression '%s' - operator expected", expr.toString()));

                /* 
                 * Check to see if we have a pending unary op that needs to be used and if so, use it by 
                 * wrapping the current expression.
                 */
                if (unaryOp != null) {
                    expr = uniOperatorMap.get(unaryOp).create(expr);
                    unaryOp = null;
                }
                exprs.add(expr);
            }
        }

        /*
         * At this point, we should have either:
         * 1) An empty set of expressions/operators
         * 2) A single expression
         * 3) A list of n operators and n + 1 expressions
         * In case 3, we need to reduce (collapse) the operators in precedence order.
         */
        Expression e = null; 
        if (!exprs.isEmpty()) e = collapse(exprs, ops);

        return e;
    }



    /**
     * Collapse a stream of expressions and operators down into a single expression.
     * <p>On entry a list of n expressions (n &gt; 0) and n - 1 operators are provided.
     * <pre>{@code 
     *  <expr 1> <op 1> <expr 2> <op 2> <expr 3> .... <expr n-1> <op n-1> <expr n>
     * }</pre>
     * Working from left to right, an operator can be collapsed if it is higher or equal
     * priority to the next operator or there is no additional operator.
     *
     * @param exprs  a list of expressions
     * @param ops    a list of operands
     * @return       a single expression representing the entire set of operations
     * @throws ExpressionError on failure to collapse the expression list (technically, only throw
     *                when attempting to create an binary operator expression).
     */
    private Expression collapse(List<Expression> exprs, List<String> ops) throws ExpressionError {
        assert !exprs.isEmpty();

        /* Work through the list from left to right to see what can be collapsed */

        while (!ops.isEmpty()) {
            int numOps = ops.size();
            int max_i = numOps - 1;

            for (int i = 0; i < numOps; ++i) {
                /*
                 * Get the precedence values of expressions i and i + 1. If there is no i + 1 expression,
                 * set the precedence to very low to force a collapse.
                 */
                int prec1 = operatorPrecedence.get(ops.get(i));
                int prec2 = (i < max_i) ? operatorPrecedence.get(ops.get(i + 1)) : -1;

                if (prec1 >= prec2) {
                    Expression e = biOperatorMap.get(ops.get(i)).create(exprs.get(i), exprs.get(i + 1));
                    exprs.set(i, e);
                    exprs.remove(i + 1);
                    ops.remove(i);
                    break;
                }
            }
        }

        assert exprs.size() == 1;
        return exprs.get(0);
    }



    /**
     * Identifies the type of an expression token.
     *
     * @param token  the token string from the expression.
     * @return the token type as an enumeration value.
     * @see Parser.TokenType
     */
    private TokenType getTokenType(String token) {
        if (token == null)
            return TokenType.EOS;
        else if (token.equals("("))
            return TokenType.BEGINPAREN;
        else if (token.equals(")"))
            return TokenType.ENDPAREN;
        else if (token.equals("["))
            return TokenType.BEGINLIST;
        else if (token.equals("]"))
            return TokenType.ENDLIST;
        else if (token.equals(","))
            return TokenType.COMMA;
        else if (token.equalsIgnoreCase("null"))
            return TokenType.NULL;
        else if (biOperatorMap.containsKey(token) || uniOperatorMap.containsKey(token))
            return TokenType.OPERATOR;
        else if (token.equalsIgnoreCase("true") || token.equalsIgnoreCase("false"))
            return TokenType.BOOL;
        else if (token.startsWith("\"") && token.endsWith("\""))
            return TokenType.STRING;
        else if (integerPattern.matcher(token).matches())
            return TokenType.INTEGER;
        else if (floatPattern.matcher(token).matches())
            return TokenType.FLOAT;

        return TokenType.TOKEN;
    }



    /**
     * Expression parser test tool entry point.
     *
     * @param args  parser command line arguments. 
     */
    public static void main(String[] args) {

        final ExpressionContext context = new ExpressionContext();
        String expression = null;
        int maxArg = args.length - 1;

        for (int i = 0; i <= maxArg; ++i) {
            if (args[i].equals("-h") || args[i].equals("--help")) {
                System.out.println("Options:");
                System.out.println("  -h               Print out this help");
                System.out.println("  -e expression    Specify the expression to be evaluated");
                System.out.println("  -s name=value    Set string variable with name <name> to <value>");
                System.out.println("  -i name=value    Set integer variable with name <name> to <value>");
                System.out.println("  -f name=value    Set floating point variable with name <name> to <value>");
                System.exit(0);
            }
            else if (args[i].equals("-e") && i < maxArg) {
                expression = args[++i];
            }
            else if (args[i].equals("-s") && i < maxArg) {
                String[] kv = args[++i].split("=", 2);
                context.set(kv[0], kv[1]);
            }
            else if (args[i].equals("-i") && i < maxArg) {
                String[] kv = args[++i].split("=", 2);
                context.set(kv[0], Integer.parseInt(kv[1]));
            }
            else if (args[i].equals("-f") && i < maxArg) {
                String[] kv = args[++i].split("=", 2);
                context.set(kv[0], Double.parseDouble(kv[1]));
            }
            else {
                System.err.println("Unsupported argument '" + args[i] + "'");
                System.exit(1);
            }
        }

        /* Prompt in interactive mode */

        if (expression == null) {
            System.out.println("Type an expression then press <enter> to evaluate");
            System.out.println("Use <ctrl> d to exit");
            System.out.println("");
        }


        try {
            /* Create the parser and context */

            Functions.registerStringFunctions();
            Functions.registerMathFunctions();
            Parser parser = new Parser();


            /* Create an input reader to read either the command line arguments or stdin */

            java.io.BufferedReader reader = new java.io.BufferedReader(
                    expression == null ?
                        new java.io.InputStreamReader(System.in)
                        :
                        new java.io.StringReader(expression)
                );


            /* Loop until no more data */

            while (true) {
                if (args.length == 0) System.out.print("expression> ");

                String input = reader.readLine();
                if (input == null) break;
                input = input.trim();

                if (input.length() > 0) {
                    try {
                        System.out.println(Objects.toString(parser.parse(input).eval(context)));
                    }
                    catch (ExpressionError x) {
                        System.out.println(x.getMessage());
                    }
                }
            }
        }
        catch (Exception x) {
            x.printStackTrace();
        }
    }
}