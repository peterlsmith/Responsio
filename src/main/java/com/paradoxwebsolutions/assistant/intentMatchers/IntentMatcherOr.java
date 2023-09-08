package com.paradoxwebsolutions.assistant.intentMatchers;

import com.paradoxwebsolutions.assistant.Assistant;
import com.paradoxwebsolutions.assistant.IntentMatcher;
import com.paradoxwebsolutions.assistant.IntentData;
import com.paradoxwebsolutions.core.ApplicationError;

import java.util.List;


public class IntentMatcherOr implements IntentMatcher {

    private List<IntentMatcher> intentMatchers;


    /**
     * Check whether or not this intent matches the interpreted intent.
     */
    public boolean match(IntentData intent) throws ApplicationError {
        for (IntentMatcher intentMatcher : this.intentMatchers) {
            if (intentMatcher.match(intent)) return true;
        }

        return false;
    }

}