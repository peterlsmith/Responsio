package com.paradoxwebsolutions.assistant.preprocessors;

import com.paradoxwebsolutions.assistant.Assistant;
import com.paradoxwebsolutions.assistant.ClientSession;
import com.paradoxwebsolutions.core.ApplicationError;
import com.paradoxwebsolutions.core.Config;
import com.paradoxwebsolutions.core.annotations.Init;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;




/**
 * Performs regular expression mapping in client input.
 * <p>This preprocessor can be used to map values identified by regular expressions -
 * such as email addresses, telephone numbers, etc.
 *
 * @author Peter Smith
 */
@PreprocessorIO(input = "tokens", output = "tokens")
public class PreprocessorReplace extends PreprocessorCopy {
 
    /** A map of replacements. The key is the textual regular expression, the value the replacement  */

    private Map<String, String> replacements;


    /** Compiled replacement map */

    private transient Map<Pattern, String> patterns = new HashMap<Pattern, String>();



    /**
     * Initialization method used to compile the regular expressions.
     * <p>This method is invoked as part of assistant initialization.
     *
     * @param assistant  the assistant instance to which this preprocessor belongs
     * @param config     assistant specific configuration
     */
    @Init
    public void init(Assistant assistant, Config config) {
        for (Map.Entry<String, String> replacement : replacements.entrySet()) {
            patterns.put(Pattern.compile(replacement.getKey()), replacement.getValue());
        }
    }



    /**
     * Performs string replacements on the client input.
     */
    @Override
    public Object preprocess(ClientSession session, String input) throws ApplicationError {
        assert input != null : "Null input passed to preprocessor";

        String output = input;
        for (Map.Entry<Pattern, String> replace : patterns.entrySet()) {
            output = replace.getKey().matcher(output).replaceAll(replace.getValue());
        }

        return output;
    }



    /**
     * Performs string replacements on the client input.
     */
    @Override
    public Object preprocess(ClientSession session, String[] input) throws ApplicationError {
        assert input != null : "Null input passed to preprocessor";

        String[] output = new String[input.length];
        System.arraycopy(input, 0, output, 0, input.length);

        for (Map.Entry<Pattern, String> replace : patterns.entrySet()) {
            for (int i = 0; i < input.length; ++i) {
                input[i] = replace.getKey().matcher(input[i]).replaceAll(replace.getValue());
            }
        }

        return output;
    }

}