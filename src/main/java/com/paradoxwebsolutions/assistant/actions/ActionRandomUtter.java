package com.paradoxwebsolutions.assistant.actions;

import com.paradoxwebsolutions.assistant.Action;
import com.paradoxwebsolutions.assistant.Assistant;
import com.paradoxwebsolutions.assistant.ClientSession;
import com.paradoxwebsolutions.assistant.ClientResponse;
import com.paradoxwebsolutions.assistant.Utterance;

import java.util.Random;


/**
 * Action to output one of a selected set of utterances at random.
 *
 * @author Peter Smith
 */
public class ActionRandomUtter implements Action {

    /* The list of utterances that can be output */

    private String[] utterances;



    @Override
    public void perform(Assistant assistant, ClientSession session, ClientResponse response) {
        Utterance utterance = assistant.getUtterance(utterances[(new Random()).nextInt(utterances.length)]);
        response.utter(utterance.getContent(session.getSessionData().getLanguage()));
    }
}