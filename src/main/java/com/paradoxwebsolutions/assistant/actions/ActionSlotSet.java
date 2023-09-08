package com.paradoxwebsolutions.assistant.actions;

import com.paradoxwebsolutions.assistant.Action;
import com.paradoxwebsolutions.assistant.Assistant;
import com.paradoxwebsolutions.assistant.ClientSession;
import com.paradoxwebsolutions.assistant.ClientResponse;
import com.paradoxwebsolutions.core.ApplicationError;

/**
 * Action to set a parameter slot.
 *
 * @author Peter Smith
 */
public class ActionSlotSet implements Action {

    /** The name of the slot to set */

    private String name;


    /** The value to set the slot to */

    private Object value;



    /**
     * Adds an utterance (text output) to the client response.
     */
    @Override
    public void perform(Assistant assistant, ClientSession session, ClientResponse response) {
        session.getSlots().put(name, value);
    }
}