package com.paradoxwebsolutions.assistant.steps;

import com.paradoxwebsolutions.assistant.Action;
import com.paradoxwebsolutions.assistant.Assistant;
import com.paradoxwebsolutions.assistant.ClientSession;
import com.paradoxwebsolutions.assistant.IntentMatcher;
import com.paradoxwebsolutions.assistant.Narrative;
import com.paradoxwebsolutions.assistant.Step;
import com.paradoxwebsolutions.core.ApplicationError;


/**
 * A default {@link com.paradoxwebsolutions.assistant.Step} implementation.
 * <p>This class represents a single step in a Story that can match against a single intent.
 *
 * @see com.paradoxwebsolutions.assistant.Story
 * @author Peter Smith
 */
public class StepDefault implements Step {

    /** The matcher used to match this step against an intent */

    private IntentMatcher   intentMatcher;


    /** How much this step adds to the story score if it matches */

    private double          score = 1.0;


    /** The action to execute if this is a terminal step in the narrative */

    private Action          action;


    @Override
    public boolean match(Assistant assistant, ClientSession session, Narrative narrative) throws ApplicationError {
        if (intentMatcher.match(narrative.getIntent())) {
            narrative.addScore(score).setAction(action).next();
            return true;
        }
        else {
            return false;
        }
    }
}