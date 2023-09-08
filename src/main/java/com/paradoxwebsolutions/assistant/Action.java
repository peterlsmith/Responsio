package com.paradoxwebsolutions.assistant;

import com.paradoxwebsolutions.core.ApplicationError;
import com.paradoxwebsolutions.core.Config;



/**
 * Interface for all Actions.
 * <p>An action is associated with a step in a story, and represents a task to be executed
 * when that step is matched to a terminal intent (last intent in the client input).
 * Actions can be used for tasks as simple as returning
 * an utterance to the user, or may perform more complex tasks such as querying a database to
 * retrieve information.
 *
 * @author Peter Smith
 * @see Step
 * @see Story
 * @see Assistant
 */
public interface Action {
    /**
     * Action execution method.
     * <p>This method is invoked when the action is to be executed, usually as a result of matching 
     * a story step to the user intent. It performs the action task.
     *
     * @param assistant  the assistant configuration object
     * @param session    the user (client) session
     * @param response   the user response instance (used to determine what will be returned to the client)
     * @throws ApplicationError on error
     */
    public void perform(Assistant assistant, ClientSession session, ClientResponse response) throws ApplicationError;
}