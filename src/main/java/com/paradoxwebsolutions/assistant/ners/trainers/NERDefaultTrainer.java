package com.paradoxwebsolutions.assistant.ners.trainers;

import com.paradoxwebsolutions.assistant.Input;
import com.paradoxwebsolutions.assistant.Trainer;
import com.paradoxwebsolutions.assistant.ners.NERDefault;
import com.paradoxwebsolutions.core.ApplicationError;
import com.paradoxwebsolutions.core.Logger;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import opennlp.tools.namefind.NameFinderME;
import opennlp.tools.namefind.NameSample;
import opennlp.tools.namefind.TokenNameFinderFactory;
import opennlp.tools.namefind.TokenNameFinderModel;
import opennlp.tools.util.ObjectStream;
import opennlp.tools.util.ObjectStreamUtils;
import opennlp.tools.util.Span;

import opennlp.tools.util.TrainingParameters;



/**
 * Trainer class for the default OpenNLP named entity recognizer.
 *
 * @author Peter Smith
 */
public class NERDefaultTrainer implements Trainer {


    /** Pattern used to identify NER tags in the training data */

    final static Pattern tag = Pattern.compile("\\{([a-z0-9_]+):([^\\}]+)\\}");


    /** The NER instance being trained/configured */

    private NERDefault ner;


    /** Training data */

    private Map<String, String[]> docs = new HashMap<>();


    /** A list of parameters obtained from the training data */

    private Set<String> parameters = new HashSet<>();



    /**
     * Creates an instance of a trainer.
     * 
     * @param ner  the NER categorizer being trained
     */
    public NERDefaultTrainer(NERDefault ner) {
        this.ner = ner;
    }



    @Override
    public void train(final Context context, final String language, final String intent, final List<Input> docs) throws ApplicationError {

        final Logger logger = context.logger;
        final String modelDir = context.modelDir;


        /* Get all the training data and replace {} tagging with xml tagging expected by OpenNLP */

        String[] data = docs.stream().map((doc) -> (String) doc.get(context.config.getString("input", "ner")))
            .filter((doc) -> (doc != null))
            .map((doc) -> tag.matcher(doc).replaceAll("<START:$1> $2 <END> "))
            .toArray(String[]::new);

        if (data.length == 0) throw new ApplicationError("No training input for for default NER");

        try {
                List<NameSample> names = new ArrayList<NameSample>();

                for (String doc : data) {
                    logger.debug(String.format("%s: %s", intent, doc));

                    NameSample nameSample = NameSample.parse(doc, true);
                    names.add(nameSample);

                    /* Get any parameters */

                    for (Span span: nameSample.getNames()) {
                        parameters.add(span.getType());
                    }
                }

                if (parameters.size() == 0) throw new ApplicationError(String.format("No parameters found for NER model for intent %s, language %s", intent, language));


                /* Train the NER model */

                logger.info(String.format("Generating NER model for language %s", language));


                TrainingParameters params = new TrainingParameters();
                params.put(TrainingParameters.ITERATIONS_PARAM, context.config.getInt("iterations", 70));
                params.put(TrainingParameters.CUTOFF_PARAM, context.config.getInt("cutoff", 1));

                ObjectStream<NameSample> stream = ObjectStreamUtils.createObjectStream(names);
                TokenNameFinderModel nerModel = NameFinderME.train("en", null, stream, params, new TokenNameFinderFactory());
                
                String modelFilename = language + "-ner-" + Integer.toHexString(intent.hashCode()) + ".bin";
                File modelFile = new File(modelDir + File.separator + modelFilename);
                nerModel.serialize(modelFile);
                logger.info(String.format("Created mode file '%s'", modelFilename));

                /* Now set the model name on the NER itself */

                ner.setModel(language, modelFilename);

        }
        catch (Exception x) {
            throw new ApplicationError(String.format("Failed training language %s for intent %s", language, intent), x);
        }

        this.docs.put(language, data);
    }



    @Override
    public void train(final Context context) throws ApplicationError {

        /* Save the NER parameters */

        ner.setParameters(new ArrayList<String>(parameters));
    }
}