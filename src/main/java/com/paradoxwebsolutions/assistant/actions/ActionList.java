package com.paradoxwebsolutions.assistant.actions;

import com.paradoxwebsolutions.assistant.Action;
import com.paradoxwebsolutions.assistant.Assistant;
import com.paradoxwebsolutions.assistant.ClientSession;
import com.paradoxwebsolutions.assistant.ClientResponse;
import com.paradoxwebsolutions.core.ApplicationError;



/**
 * Action that wraps a list of other actions.
 * <p>This Action class simply passes the Action invocation through to each Action in
 * its list, in order, one at a time.
 *
 * @author Peter Smith
 * @see Action
 */
public class ActionList implements Action {

    /** The list of actions to invoke */

    private Action[] actions;


    @Override
    public void perform(Assistant assistant, ClientSession session, ClientResponse response) throws ApplicationError {
        if (actions != null) {
            for (Action action : actions) action.perform(assistant, session, response);
        }
    }
}