package com.paradoxwebsolutions.assistant.intentMatchers;

import com.paradoxwebsolutions.assistant.Assistant;
import com.paradoxwebsolutions.assistant.IntentMatcher;
import com.paradoxwebsolutions.assistant.IntentData;
import com.paradoxwebsolutions.core.ApplicationError;



/**
 * Default implementation of an intent matcher.
 * <p>This intent matcher matches only on the intent name.
 *
 * @author Peter Smith
 */
public class IntentMatcherDefault implements IntentMatcher {

    /** The name of the intent to match*/

    private String intent;



    /**
     * Sets the name of the intent to match against.
     *
     * @param intent  the name of the intent to match
     */
    public void setIntent(String intent) {
        this.intent = intent;
    }



    /**
     * Checks whether or not this intent matches the interpreted intent.
     *
     * @param intent  the intent as determined by the categorizer
     * @return true if this instance matches the intent, false otherwise
     */
    public boolean match(IntentData intent) throws ApplicationError {
        return intent.name.equalsIgnoreCase(this.intent);
    }
}