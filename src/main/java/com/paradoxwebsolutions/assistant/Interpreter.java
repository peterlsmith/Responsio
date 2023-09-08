package com.paradoxwebsolutions.assistant;

import com.paradoxwebsolutions.core.ApplicationError;
import com.paradoxwebsolutions.core.Config;
import com.paradoxwebsolutions.core.GenericMap;
import com.paradoxwebsolutions.core.Logger;
import com.paradoxwebsolutions.core.StringMap;



/**
 * Provides input parsing, intent categorization, and parameter extraction.
 * <p>Instances of this class are used to perform pipeline preprocessing on client input,
 * intent categorization, and parameter extraction.
 *
 * @author Peter Smith
 */
public class Interpreter {

    /** Identity of the assistant for this interpreter */

    private String identity;


    /** Assistant configuration */

    private Assistant assistant;


    /** Logger */
    
    private Logger LOGGER = null;


    /** The input preprocessor pipeline */

    private PreprocessPipeline preprocessor;



    /**
     * Creates an Interpreter instance for a given assistant.
     *
     * @param assistant       the assistant configuration
     * @param identityConfig  identity specific configuration for this agent
     * @throws ApplicationError on error
     */
    public Interpreter(Assistant assistant, Config identityConfig) throws ApplicationError {
        assert identityConfig != null : "Null identity configuration passed to Interpreter";
        assert assistant != null : "Null assistant configuration passed to Interpreter";


        /* Verify configuration */

        this.assistant = assistant;
        this.identity = assistant.getIdentity();
        if (this.identity == null) throw new ApplicationError("Invalid Interpreter configuration - missing name");


        /* Set up logging (the identity logger should already have been initialized) */

        LOGGER = new Logger(this.identity);
        LOGGER.info("Initializing Interpreter");


        /* Get the input preprocessor pipeline */

        this.preprocessor = new PreprocessPipeline(assistant, "chat");


        /* Check intents */

        if (assistant.getIntents().size() == 0) throw new ApplicationError("Invalid Interpreter configuration - no intents");

        LOGGER.info("Interpreter initialized");
    }



    /**
     * Processes client input to categorize the intent and extract any named
     * entities.
     *
     * @param session   the client session
     * @param document  the raw client input
     * @return          an IntentData instance if the intent was categorized, null otherwise
     * @throws ApplicationError on error
     */
    public IntentData getIntent(ClientSession session, String document) throws ApplicationError {

        /* Preprocess this user input */

        Input input = this.preprocessor.preprocess(session, document);


        /* Categorize it. We loop through the categorizers and capture the intent with the highest score. */

        Categorizers categorizers = this.assistant.getCategorizers();
        double bestScore = 0;
        String bestIntent = null;

        for (String name : categorizers.keySet()) {
        
            IntentScores catScores = categorizers.get(name).getIntent(session, input);
            for (String intent : catScores.keySet()) {
                double threshold = this.assistant.getIntent(intent).getConfidenceThreshold();
                double score = catScores.get(intent);

                if (score > threshold && score > bestScore) {
                    bestScore = score;
                    bestIntent = intent;
                }
            }
        }

        /* If we had an intent match, set up the results and look for named entities */

        IntentData result = null;
        if (bestIntent != null) {
            session.info("Intent: " + bestIntent);

            Intent intent = this.assistant.getIntent(bestIntent);
            result = new IntentData(bestIntent);

            NERs ners = intent.getNers();
            if (ners != null) {

                /* Loop through any NERs to extract entities */

                for (NER ner : ners.values()) {
                    session.info("Performing named entity recognition");
                    ner.getEntities(session, input, result.getEntities());
                }


                /* If we have some entities, copy them to slots if not local */

                StringMap entities = result.getEntities();
                for (String name : entities.keySet()) {
                    if (!intent.getEntity(name).isLocal()) {
                        session.getSlots().put(name, entities.get(name));
                    }
                }
            }
        }
        else {
            session.info("Intent could not be determined");
        }

        return result;
    }

}
