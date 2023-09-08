package com.paradoxwebsolutions.assistant.categorizers;


import com.paradoxwebsolutions.assistant.Assistant;
import com.paradoxwebsolutions.assistant.Categorizer;
import com.paradoxwebsolutions.assistant.ClientSession;
import com.paradoxwebsolutions.assistant.Input;
import com.paradoxwebsolutions.assistant.IntentScores;
import com.paradoxwebsolutions.assistant.Trainer;
import com.paradoxwebsolutions.core.ApplicationError;
import com.paradoxwebsolutions.core.Config;
import com.paradoxwebsolutions.core.GenericMap;
import com.paradoxwebsolutions.core.Logger;
import com.paradoxwebsolutions.core.ObjectFactory;
import com.paradoxwebsolutions.core.annotations.Init;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;


/**
 * A regular expression implementation of an intent categorizer.
 *
 * @author Peter Smith
 */
public class CategorizerRegex extends Categorizer {

    /** Utility class for storing intent to regex mappings */

    public static class RegexMap extends HashMap<String, String[]> {};


    /** Utility class for storing intent to pattern mappings */

    public static class PatternMap extends HashMap<String, Pattern[]> {};


    /** The pipeline input to use for categorization */

    private String input = "document";


    /** The pipeline input to use for training */

    private String trainingInput = "document";


    /** The regular expression mappings (one per supported language) */

    private Map<String, RegexMap> regex = new HashMap<String, RegexMap>();


    /** The compiled regex patterns (one per supported language) */

    private transient Map<String, PatternMap> patterns = new HashMap<String, PatternMap>();



    /**
     * Custom initialization (called after deserialization is complete).
     *
     * @param assistant  the assistant to which this categorizer belongs
     * @param config     any identity specific configuration
     * @throws ApplicationError on error
     */
    @Init
    public void init(Assistant assistant, Config config) throws ApplicationError {

        /* Compile all the regular expressions into patterns */

        for (String lang : regex.keySet()) {
            RegexMap   regexMap = regex.get(lang);
            PatternMap patternMap = new PatternMap();
            patterns.put(lang, patternMap);

            for (String intent : regexMap.keySet()) {
                patternMap.put(intent, Arrays.stream(regexMap.get(intent)).map((regex) -> Pattern.compile(regex)).toArray(Pattern[]::new));
            }
        }

    }



    /**
     * Identifies the intent of the client input.
     *
     * @param session  the client session
     * @param input    the preprocessed client input
     * @return         An IntentScores instance containing the 0-1 based scores of the most relevant intents
     * @throws         ApplicationError on error
     */
    public IntentScores getIntent(final ClientSession session, final Input input) throws ApplicationError {

        String doc = input.get(this.input).toString();
        session.debug("CategorizerRegex categorizing: " + doc);

        IntentScores scores = new IntentScores();

        final String language = session.getLanguage();
        final PatternMap map = patterns.get(language);

        if (map != null) {
            for (String intent :  map.keySet()) {
                for (Pattern pattern : map.get(intent)) {
                    if (pattern.matcher(doc).lookingAt()) {
                        scores.put(intent, 1.0);
                        break;
                    }
                }
            }
        }
        session.debug("CategorizerRegex scores: " + String.join(":", scores.entrySet().stream().map(e -> e.getKey() + "(" + e.getValue() + ")").collect(Collectors.toList())));

        return scores;
    }



    /**
     * Adds a set of regular expressions for a named intent, in a given language.
     *
     * @param lang     the language being configured
     * @param intent   the intent being configured
     * @param regexes  the set of regular expressions that can be used to identify this intent in the given language
     */
    public void addRegex(final String lang, final String intent, final String[] regexes) {

        if( !regex.containsKey(lang)) regex.put(lang, new RegexMap());
        regex.get(lang).put(intent, regexes);
    }



    /**
     * Returns a trainer (if required) for this categorizer.
     *
     * @return a trainer instance for this categorizer
     */
    public Trainer getTrainer() {
        return new CategorizerRegexTrainer(this, trainingInput);
    }

}



/**
 * A training class implementation for this categorizer.
 */
class CategorizerRegexTrainer implements Trainer {

    /** The pipeline input to use for training */

    private String input;


    /** The categorizer being trained */

    private CategorizerRegex categorizer;


    /**
     * Creates a trainer instance.
     * 
     * @param categorizer  the categorizer being trained
     * @param input        the name of the pipeline input to use for training
     */
    public CategorizerRegexTrainer(CategorizerRegex categorizer, String input) {
        this.categorizer = categorizer;
        this.input = input;
    }



    /**
     * Process a set of training data for a given language and intent.
     *
     * @param context   the training context
     * @param language  the language being trained
     * @param intent    the name of the intent being trained
     * @param docs      the preprocessed pipeline input
     * @throws ApplicationError on error
     * @see Context
     */
    public void train(final Context context, final String language, final String intent, final List<Input> docs) throws ApplicationError {

        String[] regexes = docs.stream().map((doc) -> doc.get(input).toString()).toArray(String[]::new);
        this.categorizer.addRegex(language, intent, regexes);
    }

}

