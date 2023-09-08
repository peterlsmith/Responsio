package com.paradoxwebsolutions.assistant.intentMatchers;

import com.paradoxwebsolutions.assistant.Assistant;
import com.paradoxwebsolutions.assistant.IntentMatcher;
import com.paradoxwebsolutions.assistant.IntentData;
import com.paradoxwebsolutions.core.ApplicationError;
import com.paradoxwebsolutions.core.GenericMap;
import com.paradoxwebsolutions.core.Logger;
import com.paradoxwebsolutions.core.annotations.Init;
import com.paradoxwebsolutions.expression.Expression;
import com.paradoxwebsolutions.expression.ExpressionContext;
import com.paradoxwebsolutions.expression.Functions;
import com.paradoxwebsolutions.expression.Operators;
import com.paradoxwebsolutions.expression.Parser;


/**
 * Expression based implementation of an intent matcher.
 * <p>This intent matcher can match on intent name as well as NER values by using 
 * expressions. The intent (as determined by the categorizer) and any entity values
 * are set up in the expression context prior to evaluation, allowing expressions to
 * access the intent name and entity values directly. e.g.
 * <pre>
 *        intent == "greeting" and !empty(slots.name)
 * </pre>
 * Or more simply,
 * <pre>
 *
 *        greeting and slots.name
 * </pre>
 *
 * @author Peter Smith
 */
public class IntentMatcherExpression implements IntentMatcher {

    /** Expression parser (we only need one since it is thread safe) */

    private static Parser parser = new Parser();


    /**
     * Initialization method to configure some expression extensions.
     *
     * @param logger   the assistant logger
     * @throws ApplicationError on error
     */
    @Init
    public static void init(Logger logger) throws ApplicationError {

        logger.info("Registering 'slot' function for IntentMatcherExpression");
        Parser.registerFunction(Functions.create("slot", 
            (context, name) -> ((GenericMap) context.get("slots")).get(name)
        ));
    
    }




    /** The expression used to perform intent matching */

    private String expression;


    /** The compiled expression */

    private transient Expression compiledExpr;



    /**
     * Sets the itent matching expression.
     *
     * @param expression  the expression to be used for intent matching
     */
    public void setIntent(String expression) {
        this.expression = expression;
    }



    /**
     * Initializes this intent matcher.
     * <p>This method is called after serialization and is used to compile the expression.
     *
     * @throws ApplicationError if the expression could not be parsed
     */
    @Init
    public void init() throws ApplicationError {
        try {
            compiledExpr = parser.parse(expression);
        }
        catch(Exception x) {
            throw new ApplicationError(String.format("Failed to parse intent matcher expression '%s': %s", expression, x.getMessage()));
        }
    }


    /**
     * Evaluates the expression in the context of the intent information to see if it matches.
     */
    @Override
    public boolean match(IntentData intent) throws ApplicationError {
        try {
            double score = 0;

            /* Set the intent and the slots in the expression context */

            ExpressionContext context = new ExpressionContext();
            context.set(intent.getName(), true);
            context.set("intent", intent.getName());
            context.set("slots", intent.getEntities());

            /* Entities are mapped directly by name */
            
            for (String key : intent.getEntities().keySet())
                context.set(key, intent.getEntities().get(key));

            return Operators.toBoolean(compiledExpr.eval(context));
        }
        catch(Exception x) {
            throw new ApplicationError(String.format("Failed evaluate expression '%s': %s", expression, x.getMessage()), x);
        }
    }
}