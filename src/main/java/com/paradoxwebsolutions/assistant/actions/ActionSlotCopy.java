package com.paradoxwebsolutions.assistant.actions;

import com.paradoxwebsolutions.assistant.Action;
import com.paradoxwebsolutions.assistant.Assistant;
import com.paradoxwebsolutions.assistant.ClientSession;
import com.paradoxwebsolutions.assistant.ClientResponse;
import com.paradoxwebsolutions.core.ApplicationError;

/**
 * Action to copy a parameter slot.
 *
 * @author Peter Smith
 */
public class ActionSlotCopy implements Action {

    /** The name of the slot to copy */

    private String source;


    /** The name of the slot to set */

    private String destination;



    /**
     * Adds an utterance (text output) to the client response.
     */
    @Override
    public void perform(Assistant assistant, ClientSession session, ClientResponse response) {
        session.getSlots().put(destination, session.getSlots().get(source));
    }
}