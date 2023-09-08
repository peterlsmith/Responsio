package com.paradoxwebsolutions.assistant.actions;

import com.paradoxwebsolutions.assistant.Action;
import com.paradoxwebsolutions.assistant.Assistant;
import com.paradoxwebsolutions.assistant.ClientSession;
import com.paradoxwebsolutions.assistant.ClientResponse;
import com.paradoxwebsolutions.assistant.Utterance;

import java.util.regex.Pattern;
import java.util.regex.Matcher;

/**
 * Most basic Action implementation to output an utterance.
 *
 * @author Peter Smith
 * @see com.paradoxwebsolutions.assistant.Utterance
 */
public class ActionUtter implements Action {

    /**
     * Pattern used to look for replaceable '${name}' values in utterances.
     */
    private static Pattern regex = Pattern.compile("\\$\\{([^}]+)}");


    /** The name of the utterance to output */

    private String utterance;


    /**
     * Class constructor.
     */
    public ActionUtter() {
    }


    /**
     * Class constructor.
     *
     * @param utterance  The name of the utterance
     */
    public ActionUtter(final String utterance) {
        this.utterance = utterance;
    }



    /**
     * Adds an utterance (text output) to the client response.
     */
    @Override
    public void perform(Assistant assistant, ClientSession session, ClientResponse response) {
        Utterance utterance = assistant.getUtterance(this.utterance);
        String text = utterance.getContent(session.getSessionData().getLanguage());

        if (text.contains("${")) {
            text = regex.matcher(text)
                .replaceAll(match -> session.getSlots().getOrDefault(match.group(1), "").toString());
        }

        response.utter(text);
    }
}