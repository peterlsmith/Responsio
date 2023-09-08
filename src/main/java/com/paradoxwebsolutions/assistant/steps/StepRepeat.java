package com.paradoxwebsolutions.assistant.steps;

import com.paradoxwebsolutions.assistant.Assistant;
import com.paradoxwebsolutions.assistant.ClientSession;
import com.paradoxwebsolutions.assistant.Narrative;
import com.paradoxwebsolutions.assistant.Step;
import com.paradoxwebsolutions.core.ApplicationError;



/**
 * A {@link com.paradoxwebsolutions.assistant.Step} implementation that can be used to match
 * against another step multiple times.
 *
 * @see com.paradoxwebsolutions.assistant.Story
 * @author Peter Smith
 */
public class StepRepeat implements Step {

    /** The step that can be repeated (note that this can be a compound step) */

    private Step  step;


    /** The minimum number of times the step must match */

    private int             minMatch = 0;


    /** The maximum number of times the step may match */

    private int             maxMatch = 1;


    /**
     * Matches the given step a number of times against the narrative.
     */
    @Override
    public boolean match(Assistant assistant, ClientSession session, Narrative narrative) throws ApplicationError {

        int count = 0; /* Used to track the number of matches */
        boolean complete = false;

        /* Repeat while we have more narrative and we haven't reached the upper limit */

        while (count < maxMatch && narrative.hasMore()) {

            /* Checkpoint so we can roll back, then match */

            Narrative.Checkpoint checkpoint = narrative.checkpoint();
            complete = step.match(assistant, session, narrative);


            /*
             * If the match wasn't complete and there is more narrative, then the step must have hit a non-matching
             * intent in the narrative. In this case, we must roll-back to the previous complete match and exit
             * immediately.
             * If the match was not complete and there is no more narrative, then we can just exit the repeat loop.
             */
            if (!complete) {
                if (narrative.hasMore()) {
                    checkpoint.restore();
                    complete = true;
                }
                break;
            }
            ++count;
        }


        /*
         * If we reached our minimum count, we can just return the match status with the narrative
         * as-is (the loop above left it in the correct state).
         */
        if (count >= minMatch) return complete;

        /*
         * Otherwise we didn't reach the minimum, so we are either not complete, or no match at all.
         * Either way, the loop above should leave the narrative in the correct state.
         */
        return false;
    }
}