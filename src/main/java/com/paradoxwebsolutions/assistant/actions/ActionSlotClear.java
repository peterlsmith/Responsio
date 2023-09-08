package com.paradoxwebsolutions.assistant.actions;

import com.paradoxwebsolutions.assistant.Action;
import com.paradoxwebsolutions.assistant.Assistant;
import com.paradoxwebsolutions.assistant.ClientSession;
import com.paradoxwebsolutions.assistant.ClientResponse;
import com.paradoxwebsolutions.core.ApplicationError;

/**
 * Action to clear a parameter slot.
 *
 * @author Peter Smith
 */
public class ActionSlotClear implements Action {

    /** The name of the slot to clear */

    private String name;


    /**
     * Adds an utterance (text output) to the client response.
     */
    @Override
    public void perform(Assistant assistant, ClientSession session, ClientResponse response) {
        session.getSlots().remove(name);
    }
}