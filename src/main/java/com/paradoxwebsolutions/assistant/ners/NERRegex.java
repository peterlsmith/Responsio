package com.paradoxwebsolutions.assistant.ners;

import com.paradoxwebsolutions.assistant.Assistant;
import com.paradoxwebsolutions.assistant.ClientSession;
import com.paradoxwebsolutions.assistant.Input;
import com.paradoxwebsolutions.assistant.NER;
import com.paradoxwebsolutions.core.ApplicationError;
import com.paradoxwebsolutions.core.StringMap;
import com.paradoxwebsolutions.core.annotations.Init;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Implementation of a Name Entity Recognizer (NER) that uses regular expressions.
 *
 * @author Peter Smith
 */
public class NERRegex implements NER {

    /** A map of regular expressions and parameter names */

    private Map<String, String> parameters;


    /** A map of compiled regular expressions */

    private transient Map<Pattern, String> patterns = new HashMap<Pattern, String>();


    /** The input stream to use for named entity extraction */

    private String input = "document";
    


    /**
     * Compiles the regular expressions for this regex NER.
     * 
     * @throws ApplicationError on error loading the language models
     */
    @Init
    public void init() throws ApplicationError {
        if (parameters == null) throw new ApplicationError("No parameters defined for NERRegex");

        for (Map.Entry<String, String> parameter : parameters.entrySet()) {
            patterns.put(Pattern.compile(parameter.getKey()), parameter.getValue());
        }
    }



    @Override
    public void getEntities(final ClientSession session, final Input input, final StringMap entities) throws ApplicationError {
        assert input.get(this.input) != null : String.format("Input '%s' is null", this.input);

        Object data = input.get(this.input);

        if (data.getClass() == String[].class) {
            String[] tokens = (String[]) data;

            for (Map.Entry<Pattern, String> parameter : patterns.entrySet()) {
                for (int i = 0; i < tokens.length; ++i) {
                    Matcher m = parameter.getKey().matcher(tokens[i]);
                    while (m.find()) {
                        entities.put(parameter.getValue(), m.group());
                    }
                }
            }
        }
        else if (data.getClass() == String.class) {
            String sentence = (String) data;

            for (Map.Entry<Pattern, String> parameter : patterns.entrySet()) {
                Matcher m = parameter.getKey().matcher(sentence);
                while (m.find()) {
                    entities.put(parameter.getValue(), m.group());
                    session.debug(String.format("%s[%s]", parameter.getValue(), m.group()));
                }
            }
        }
        else throw new ApplicationError("Unsupported input type for 'replace' preprocessor");
    }


    @Override
    public List<String> getParameters() {
        return parameters.entrySet().stream().map((e) -> e.getValue()).collect(Collectors.toList());
    }
}