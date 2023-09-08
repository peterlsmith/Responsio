package com.paradoxwebsolutions.assistant;


import com.paradoxwebsolutions.assistant.ClientSession;
import com.paradoxwebsolutions.core.ApplicationError;
import com.paradoxwebsolutions.core.StringMap;

import java.util.List;

/**
 * Defines the interface for Named Entity Recognition instances.
 *
 * @author Peter Smith
 */
public interface NER {


    /**
     * Performs named entity extraction.
     *
     * @param session  the client session
     * @param input    the pre-processed client input
     * @param entities  used to return any extracted entity name/value pairs 
     * @throws ApplicationError on error
     */
    public void getEntities(final ClientSession session, final Input input, final StringMap entities) throws ApplicationError;



    /**
     * Returns the parameters supported by this NER.
     *
     * @return a list of the parameter names supported by this NER instance.
     */
    public List<String> getParameters();



    /**
     * Returns a trainer (if required) for this NER.
     *
     * @return a instance of a Trainer object for training this NER instance
     */
    default Trainer getTrainer() {
        return null;
    }
}