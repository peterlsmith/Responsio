package com.paradoxwebsolutions.assistant.steps;

import com.paradoxwebsolutions.assistant.Assistant;
import com.paradoxwebsolutions.assistant.ClientSession;
import com.paradoxwebsolutions.assistant.Narrative;
import com.paradoxwebsolutions.assistant.Step;
import com.paradoxwebsolutions.core.ApplicationError;


/**
 * An optional {@link com.paradoxwebsolutions.assistant.Step} implementation.
 * <p>This class represents a single step in a Story that can match against a single intent 0 or once.
 *
 * @see com.paradoxwebsolutions.assistant.Story
 * @author Peter Smith
 */
public class StepOptional implements Step {

    /** The step to match against */

    private Step step;



    @Override
    public boolean match(Assistant assistant, ClientSession session, Narrative narrative) throws ApplicationError {
        
        int baseIndex = narrative.getIndex();
        boolean complete = step.match(assistant, session, narrative);

        if (narrative.getIndex() == baseIndex) {
            /* No progress at all on the narrative, so optional step does not exist (and is therefore considered complete) */

            return true;
        }
        else {
            /* We progressed the narrative, so we may or may not be complete */

            return complete;
        }
    }
}