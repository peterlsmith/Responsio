package com.paradoxwebsolutions.assistant.preprocessors;

import com.paradoxwebsolutions.assistant.Preprocessor;
import com.paradoxwebsolutions.assistant.ClientSession;
import com.paradoxwebsolutions.core.ApplicationError;
import com.paradoxwebsolutions.core.GenericMap;
import com.paradoxwebsolutions.core.Logger;
import com.paradoxwebsolutions.core.ObjectFactory;
import com.paradoxwebsolutions.core.annotations.Init;

import java.util.Arrays;
import java.io.InputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;



/**
 * Input preprocessor to remove stopwords.
 * <p>Stop words are redundant or unnecessary words which occur in most sentences and
 * can degrade the quality of the intent classification. This preprocessor can remove
 * those words.
 *
 * @author Peter Smith
 */
@PreprocessorIO(input = "tokens", output = "tokens")
public class PreprocessorStopwords extends PreprocessorCopy {

    /** Utility class for simplifying generics */

    private static class StringSet extends HashSet<String> {};


    /** Utility class for simplifying generics */

    private static class LanguageMap extends HashMap<String, StringSet> {};


    /** Language mapped stopwords */

    private static LanguageMap stopWords;


    /** Empty map for default/unconfigured situations */

    private static final StringSet defaultWords = new StringSet();



    /**
     * Loads the stop word language maps.
     * 
     * @param logger  the service logger
     * @throws ApplicationError on error
     */
    @Init
    public static void init(Logger logger) throws ApplicationError {

        final String name = PreprocessorStopwords.class.getSimpleName();
        final InputStream in = PreprocessorStopwords.class.getResourceAsStream(name + ".json");
        if (in == null) throw new ApplicationError(String.format("Cannot find resource for %s", "PreprocessorStopwords"));

        stopWords = new ObjectFactory().fromJson(in, LanguageMap.class);

        logger.info(String.format("Loaded %s data file. Supported languages: %s", name, String.join(",", stopWords.keySet())));
    }



    @Override
    public Object preprocess(ClientSession session, String[] input) {
        assert input != null : "Null input passed to preprocessor";

        String lang = session.getLanguage();
        final Set<String> words = (Set<String>) stopWords.getOrDefault(lang, defaultWords);

        return Arrays.stream(input).filter(w -> !words.contains(w)).toArray(String[]::new);
    }

}