package com.paradoxwebsolutions.assistant.steps;

import com.paradoxwebsolutions.assistant.Assistant;
import com.paradoxwebsolutions.assistant.ClientSession;
import com.paradoxwebsolutions.assistant.Narrative;
import com.paradoxwebsolutions.assistant.Step;
import com.paradoxwebsolutions.core.ApplicationError;


/**
 * An optional {@link com.paradoxwebsolutions.assistant.Step} implementation.
 * <p>This class represents a single step in a Story that must match once against one of a set of
 * steps.
 *
 * @see com.paradoxwebsolutions.assistant.Story
 * @author Peter Smith
 */
public class StepOr implements Step {

    /** The step of steps to look for a match against */

    private Step[]  steps;

    

    @Override
    public boolean match(Assistant assistant, ClientSession session, Narrative narrative) throws ApplicationError {

        /*
         * Find the best match amongst the steps. 'best' is determined by the number of
         * steps matched.
         */
        Narrative.Checkpoint baseline = narrative.checkpoint();
        Narrative.Checkpoint bestMatch = baseline;
        boolean bestComplete = false;

        for (Step step : steps) {
            boolean complete = step.match(assistant, session, narrative);

            /* Check to see if this step has progressed the story at all */

            if (narrative.getIndex() > bestMatch.getIndex() || (narrative.getIndex() == bestMatch.getIndex() && narrative.getScore() > bestMatch.getScore())) {
                bestMatch = narrative.checkpoint();
                bestComplete = complete;
            }

            /* Restore the narrative for the next match */

            baseline.restore();
        }

        /* If we found a match, restore the narrative to the appropriate state for that match */

        if (bestMatch != baseline) bestMatch.restore();

        return bestComplete;
    }
}