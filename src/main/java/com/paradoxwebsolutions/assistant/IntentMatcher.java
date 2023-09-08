package com.paradoxwebsolutions.assistant;

import com.paradoxwebsolutions.core.ApplicationError;


/**
 * Interface for intent matchers.
 * <p>Intent matchers are used to determine whether or not a client intent
 * matches up with a given step in a story.
 *
 * @author Peter Smith
 */
public interface IntentMatcher {


    /**
     * Determines whether or not this intent matcher instance matches the given client intent.
     * 
     * @param intent  the client intent
     * @return        true if the intent matches, false otherwise
     * @throws ApplicationError on error
     */
    public boolean match(IntentData intent) throws ApplicationError;
}