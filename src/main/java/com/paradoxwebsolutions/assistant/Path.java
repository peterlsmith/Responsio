package com.paradoxwebsolutions.assistant;

import com.paradoxwebsolutions.assistant.Assistant;
import com.paradoxwebsolutions.assistant.ClientSession;
import com.paradoxwebsolutions.assistant.Narrative;
import com.paradoxwebsolutions.assistant.Step;
import com.paradoxwebsolutions.core.ApplicationError;


/**
 * A {@link com.paradoxwebsolutions.assistant.Step} implementation that can be used to match
 * against a sequence of steps (path segment).
 *
 * @see com.paradoxwebsolutions.assistant.Story
 * @author Peter Smith
 */
public class Path implements Step {

    /** The sequence of steps to match against */

    private Step[]  path;



    @Override
    public boolean match(Assistant assistant, ClientSession session, Narrative narrative) throws ApplicationError {

        /*
         * Loop through each step in turn until we run out, or find one that does not match. If
         * we find one that does not match, we exit with 'false' (this step is not complete)
         */
        int i = 0;
        while (narrative.hasMore() && i < path.length) {
            if (!path[i++].match(assistant, session, narrative)) return false;
        }

        /*
         * If we get here, we either matched everything or we ran out of narrative.
         * If we matched everything, this step is complete, otherwise not.
         */
        return (i == path.length);
    }
}