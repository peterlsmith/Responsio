package com.paradoxwebsolutions.assistant;


import com.paradoxwebsolutions.assistant.ClientSession;
import com.paradoxwebsolutions.core.ApplicationError;
import com.paradoxwebsolutions.core.Config;



/**
 * Interface for intent classifiers.
 * <p>Intent classification is the process of working out the meaning (intent) of the user input.
 */
public abstract class Categorizer {


    /**
     * Initializes the categorizer.
     * <p>This method is invoked after an assistant has been deserialized, and 
     * provides an opportunity to load models or other data files necessary to
     * operate. A default implementation that does nothing is provided.
     *
     * @param assistant  the assistant to which this categorizer belongs
     * @param config     identity (assistant) specific configuration
     * @throws ApplicationError if initialization fails
     */
    public void init(Assistant assistant, Config config) throws ApplicationError {
    }



    /**
     * Score the intents on a scale of 0 to 1, with 1 being the most likely.
     * <p>This method is used to identify the intent of a user input. Results are
     * returned in an {@link IntentScores} instance - basically a hashmap of intents 
     * (as the key) and a 0-1 based probability of that intent being the true meaning
     * of the input as a score. 
     *
     * @param clientSession  the user client session object
     * @param input          the processed user input
     * @return an {@link IntentScores} instance with zero or more values
     * @throws ApplicationError if an error occurred during categorization
     * @see IntentScores
     */
    public abstract IntentScores getIntent(final ClientSession clientSession, final Input input) throws ApplicationError;



    /**
     * Returns a trainer (if required) for this categorizer.
     * <p>This method is only used during assistant setup (model training).
     *
     * @return a trainer for this categorizer, or null if no trainer is required
     */
    public Trainer getTrainer() {
        return null;
    }
}