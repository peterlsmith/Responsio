package com.paradoxwebsolutions.assistant.categorizers.trainers;

import com.paradoxwebsolutions.assistant.Input;
import com.paradoxwebsolutions.assistant.NER;
import com.paradoxwebsolutions.assistant.Trainer;
import com.paradoxwebsolutions.assistant.categorizers.CategorizerDefault;
import com.paradoxwebsolutions.core.ApplicationError;
import com.paradoxwebsolutions.core.Logger;

import java.io.File;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;


import opennlp.tools.doccat.BagOfWordsFeatureGenerator;
import opennlp.tools.doccat.NGramFeatureGenerator;
import opennlp.tools.doccat.DoccatFactory;
import opennlp.tools.doccat.DoccatModel;
import opennlp.tools.doccat.DocumentCategorizerME;
import opennlp.tools.doccat.DocumentSample;
import opennlp.tools.doccat.FeatureGenerator;
import opennlp.tools.ml.AbstractTrainer;
import opennlp.tools.util.ObjectStream;
import opennlp.tools.util.ObjectStreamUtils;
import opennlp.tools.util.TrainingParameters;
import opennlp.tools.util.model.ModelUtil;

import opennlp.tools.langdetect.Language;
import opennlp.tools.langdetect.LanguageSample;
import opennlp.tools.langdetect.LanguageDetector;
import opennlp.tools.langdetect.LanguageDetectorFactory;
import opennlp.tools.langdetect.LanguageDetectorME;
import opennlp.tools.langdetect.LanguageDetectorModel;


/**
 * Trainer for the default categorizer.
 * <p>This class is used to process the training data and generate models for the default OpenNLP categorizer.
 *
 * @author Peter Smith
 */
public class CategorizerDefaultTrainer implements Trainer {

    /** The categorizer that needs to be configured/trained */

    private CategorizerDefault categorizer;


    /** The pipeline input to use for training */

    private String input;


    /** A list of all the documents for the categorizer to train on */

    private Map<String, List<DocumentSample>> documents = new HashMap<String, List<DocumentSample>>();


    /** A list of language samples to be used for language detection  */

    private List<LanguageSample> langDocuments = new ArrayList<LanguageSample>();



    /**
     * Creates a trainer instance.
     *
     * @param categorizer  the categorizer being trained.
     * @param input        the name of the training data in the preprocessing pipeline
     */
    public CategorizerDefaultTrainer(CategorizerDefault categorizer, String input) {
        this.categorizer = categorizer;
        this.input = input;
    }



    @Override
    public void train(final Context context, final String language, final String intent, final List<Input> docs) throws ApplicationError {
        if (!documents.containsKey(language)) documents.put(language, new ArrayList<DocumentSample>());

        for (Input doc :  docs) {
            String[] tokens = (String[]) doc.get(input); 
            DocumentSample docSample = new DocumentSample(intent, tokens);
            documents.get(language).add(docSample);

            /* Language detection always based on original client input */

            LanguageSample langSample = new LanguageSample(new Language(language), (String) doc.get("document"));
            langDocuments.add(langSample);
        }
    }



    @Override
    public void train(final Context context) throws ApplicationError {

        final String modelDir = context.modelDir;
        final Logger logger = context.logger;

        /* TBD Update to use traingin parameters if available */
        
        try {
            /* Now train the categorizer models - one for each language */

            for (String language : documents.keySet()) {
                ObjectStream<DocumentSample> stream = ObjectStreamUtils.createObjectStream(documents.get(language));

                TrainingParameters params = ModelUtil.createDefaultTrainingParameters();
                params.put(TrainingParameters.ITERATIONS_PARAM, 100);
                params.put(TrainingParameters.CUTOFF_PARAM, 1);
                params.put(AbstractTrainer.VERBOSE_PARAM, true);

                DoccatFactory factory = new DoccatFactory(new FeatureGenerator[] { new BagOfWordsFeatureGenerator() /* NGramFeatureGenerator(2, 3) */ });
                DoccatModel model = DocumentCategorizerME.train(language, stream, params, factory);


                /* Save the model. We do not configure the categorizer itself with the model name - it is assumed */

                File modelFile = new File(modelDir + File.separator + language + "-categorizer-opennlp.bin");
                model.serialize(modelFile);
                logger.info(String.format("Created categorizer model '%s'", modelFile.getName()));

                this.categorizer.setModel(language, modelFile.getName());
                context.files.add(modelFile.getName());
            }


            /*
             * Train a language model if we have more than one langauge. This model is used by the
             * langauge detection preprocessor.
             */
            Set<String> languages = langDocuments.stream().map((d) -> d.getLanguage().getLang()).collect(Collectors.toSet());
            if (languages.size() > 1) {
                logger.info("Multiple languages detected - creating language detection model");
                ObjectStream<LanguageSample> stream = ObjectStreamUtils.createObjectStream(langDocuments);
                TrainingParameters params = ModelUtil.createDefaultTrainingParameters();
                params.put(TrainingParameters.ITERATIONS_PARAM, 100);
                params.put(TrainingParameters.CUTOFF_PARAM, 5);
                params.put(AbstractTrainer.VERBOSE_PARAM, true);
                params.put(TrainingParameters.ALGORITHM_PARAM, "NAIVEBAYES");
                LanguageDetectorModel model = LanguageDetectorME.train(stream, params, new LanguageDetectorFactory());
                LanguageDetector ld = new LanguageDetectorME(model);

                /* Save the model. We do not configure the categorizer itself with the model name - it is assumed */

                File modelFile = new File(modelDir + File.separator + "ld-opennlp.bin");
                model.serialize(modelFile);
                logger.debug(String.format("Created language detection model '%s'", modelFile.getName()));
                context.files.add(modelFile.getName());
            }

        }
        catch (Exception x) {
            throw new ApplicationError("Failed to create categorizer model file: " + x.getMessage());
        }
    }
}


