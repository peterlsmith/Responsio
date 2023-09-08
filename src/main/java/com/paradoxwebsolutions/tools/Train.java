package com.paradoxwebsolutions.tools;


import com.paradoxwebsolutions.assistant.Action;
import com.paradoxwebsolutions.assistant.Assistant;
import com.paradoxwebsolutions.assistant.AssistantFactory;
import com.paradoxwebsolutions.assistant.Categorizer;
import com.paradoxwebsolutions.assistant.Categorizers;
import com.paradoxwebsolutions.assistant.ClientSession;
import com.paradoxwebsolutions.assistant.Entities;
import com.paradoxwebsolutions.assistant.Entity;
import com.paradoxwebsolutions.assistant.IdentityClassLoader;
import com.paradoxwebsolutions.assistant.Input;
import com.paradoxwebsolutions.assistant.Intent;
import com.paradoxwebsolutions.assistant.NER;
import com.paradoxwebsolutions.assistant.NERs;
import com.paradoxwebsolutions.assistant.Preprocessor;
import com.paradoxwebsolutions.assistant.PreprocessPipeline;
import com.paradoxwebsolutions.assistant.SessionData;
import com.paradoxwebsolutions.assistant.Step;
import com.paradoxwebsolutions.assistant.Story;
import com.paradoxwebsolutions.assistant.Trainer;
import com.paradoxwebsolutions.assistant.Utterance;
import com.paradoxwebsolutions.assistant.Utterances;
import com.paradoxwebsolutions.assistant.actions.ActionUtter;
import com.paradoxwebsolutions.assistant.categorizers.CategorizerDefault;
import com.paradoxwebsolutions.assistant.ners.NERDefault;
import com.paradoxwebsolutions.assistant.ners.NERRegex;
import com.paradoxwebsolutions.assistant.preprocessors.PreprocessorDefault;
import com.paradoxwebsolutions.core.Config;
import com.paradoxwebsolutions.core.CustomConsoleHandler;
import com.paradoxwebsolutions.core.ObjectInitializer;
import com.paradoxwebsolutions.core.ServiceAPI;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import java.util.stream.Collectors;


import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.annotations.SerializedName;



/**
 * Sets up the model files and configuration for an assistant.
 *
 * @author Peter Smith
 */
class Train extends Tool {


    /**
     * Command line tool entry point.
     * @param args  command line arguments
     */
    public static void main(String args[]) {
        try {
            if (args.length == 0) throw new Exception("No identity specified");

            Train train = new Train(args[0]);
            train.doTrain();
            train.save();
        }
        catch (Exception x) {
            x.printStackTrace();
        }
    }



    /** The identity being chatted with */

    private String identity;


    /** The directory location of all the input files */

    private String   dataDir;


    /** The directory location of all the output files */

    private String   modelDir;


    /** Factory used to serialize chat assistant components to/from json */

    private AssistantFactory objectFactory;


    /** Local (identity specific) configuration */

    private Config identityConfig;


    /** The assistant being configured/trained */

    private Assistant assistant;


    /** The languages supported by this entity */

    private Set<String> languages;


    /**
     * Creates a trainer instance.
     *
     * @param identity  the assistant identity
     * @throws Exception on error
     */
    public Train(final String identity) throws Exception {
        super();
        this.identity = identity;

        dataDir = System.getProperty("dir.data");
        if (dataDir == null) throw new Exception("Missing 'data.dir' property");
        dataDir = dataDir + File.separator + identity;


        /* Get identify specific configuration required by assistant */

        identityConfig = config.getConfig("identity.default").load(config.getConfig("identity." + identity));
        modelDir = config.getString("dir.model") + File.separator + identity;
        identityConfig.setString("dir.model", modelDir);
        identityConfig.setString("identity", identity);
        identityConfig.setBool("training", true);


        /* Create a class loader and load any custom code files needed by this identity */

        IdentityClassLoader classLoader = new IdentityClassLoader(this);
        classLoader.loadClasses(modelDir + File.separator + "extensions");
        

        /* Create an object factory that will be used to serialize configuration components */

        objectFactory = new AssistantFactory(classLoader);


        /* Set up the assistant configuration object that will be set up during processing/training */

        assistant = new Assistant(identity);
    }



    /**
     * Executes a series of training/configuring steps.
     *
     * @throws Exception on error
     */
    public void doTrain() throws Exception {
        processMisc();
        processPreprocessingPipeline();
        processCategorizers();
        processIntents();
        processUtterances();
        processStories();
    }



    /**
     * Saves the assistant configuration file.
     *
     * @throws Exception on error
     */
    public void save() throws Exception {
        /* Save the assistant configuration file */

        FileWriter cfgWriter = new FileWriter(modelDir + File.separator + "assistant.json");
        String json = objectFactory.toJson(assistant);

        cfgWriter.write(json, 0, json.length());
        cfgWriter.flush();
        cfgWriter.close();

        LOGGER.info(json);
    }




    /**
     * Processes miscellaneous configuration.
     *
     * @throws Exception on error
     */
    private void processMisc() throws Exception {

        /* Locate the miscellaneous configuration file */

        Misc misc;
        File file = new File(dataDir + File.separator + "misc.json");

        if (file.exists())
            misc = objectFactory.fromJson(file, Misc.class);
        else
            misc = new Misc();

        assistant.setDefaultIntent(misc.defaultIntent);
        assistant.setWelcomeAction(misc.welcome);
        assistant.setWelcomeBackAction(misc.welcomeBack);
        languages = misc.languages;

        LOGGER.info("Loaded miscellaneous configuration");
    }



    /**
     * Processes the input processing pipeline configuration.
     *
     * @throws Exception on error
     */
    @SuppressWarnings("unchecked")
    private void processPreprocessingPipeline() throws Exception {

        Preprocessor[] pipeline = new Preprocessor[0];
        
        /* Locate the pipeline file */

        File pipelineFile = new File(dataDir + File.separator + "pipeline.json");
        if (pipelineFile.isFile()) {
            /* Load the Pipeline data */

            pipeline = objectFactory.fromJson(pipelineFile, pipeline.getClass());

            /* Basic story validation */

            LOGGER.info("Loaded processing pipeline");
        }
        else {
            /* Set up a default processing pipeline */

            pipeline = new Preprocessor[]{new PreprocessorDefault()};
            LOGGER.info("Configured default processing pipeline");
        }

        assistant.setPreprocessors(Arrays.asList(pipeline));

        /* Initialize the pipeline - it will be used for processing the training documents */

        ObjectInitializer initializer = new ObjectInitializer();

        for (Preprocessor p : pipeline) {
            initializer.initialize(p, assistant, identityConfig, LOGGER);
        }
    }



    /**
     * Processes the categorizer configuration.
     *
     * @throws Exception on error
     */
    private void processCategorizers() throws Exception {

        /* Locate the story files */

        File catConfigFile = new File(dataDir + File.separator + "categorizers.json");
        Categorizers categorizers;

        if (catConfigFile.isFile()) {
            categorizers = objectFactory.fromJson(catConfigFile, Categorizers.class);
            LOGGER.info("Loaded categorizers");
        }
        else {
            /* Set up a default categorizer */

            categorizers = new Categorizers();
            categorizers.put("default", new CategorizerDefault());
            LOGGER.info("Created default categorizer");
        }

        assistant.setCategorizers(categorizers);
    }



    /**
     * Processes all the story files.
     *
     * @throws Exception on error
     */
    private void processStories() throws Exception {

        /* Locate the story files */

        File storyDir = new File(dataDir + File.separator + "stories");
        if (!storyDir.isDirectory()) throw new Exception(String.format("Invalid story directory '%s'",  storyDir.getName()));


        /* Get a list of the training files and start processing */

        File[] files = storyDir.listFiles();

        for (File file : files) {

            /* Load the story data */

            LOGGER.info(String.format("Loading story file '%s'", file.getName()));
            String name = file.getName().substring(0, file.getName().lastIndexOf('.'));
            Story story = objectFactory.fromJson(file, Story.class);

            /* Finalize */

            LOGGER.info(String.format("Loaded story '%s'", name));
            assistant.addStory(name, story);
        }

        /* Check we have an 'oos' story */

        if (assistant.getStories().get("oos") == null) {
            throw new Exception("No 'oos' story");
        }
    }


    /**
     * Processes all the utterance files.
     *
     * @throws Exception on error
     */
    private void processUtterances() throws Exception {

        Utterances utterances = new Utterances();
        
        /* Locate the utterances file */

        File file = new File(dataDir + File.separator + "utterances.json");
        if (file.exists()) {
            /* Load the data */

            utterances = objectFactory.fromJson(file, utterances.getClass());
            LOGGER.info("Loaded utterances configuration");

            for (String name : utterances.keySet()) {
                Utterance utterance = utterances.get(name);
                if (!languages.containsAll(utterance.keySet())) {
                    LOGGER.warning(String.format("Missing language in utterance '%s'", name));
                }
            }
        }
        LOGGER.info("Loaded utterances");
        assistant.setUtterances(utterances);
    }



    /**
     * Processes all the intent files.
     *
     * @throws Exception on error
     */
    private void processIntents() throws Exception {

        /* Get a list of the intent training files */

        File intentDir = new File(dataDir + File.separator + "intents");
        if (!intentDir.isDirectory()) throw new Exception(String.format("Invalid intent directory '%s'",  intentDir.getName()));


        /* Create the categorizer trainers */

        Categorizers categorizers = assistant.getCategorizers();
        Map<String, Trainer> trainers = new HashMap<String, Trainer>();

        for (String name : categorizers.keySet()) {
            Trainer trainer = categorizers.get(name).getTrainer();
            if (trainer != null) trainers.put(name, trainer);
        }


        /* Perform intent configuration and setup training data */

        File[] files = intentDir.listFiles();
        for (File file : files) {
            processIntent(file, trainers);
        }


        /* Now finalize the categorizer training */

        Trainer.Context context = new Trainer.Context();
        context.identity = assistant.getIdentity();
        context.service = this;
        context.logger = LOGGER;
        context.modelDir = modelDir;

        for (String name : trainers.keySet()) {
            trainers.get(name).train(context);
        }
    }


    /**
     * Processes an intent file.
     *
     * @param intentFile  the intent file to process
     * @param trainers    a map of named trainers that can be trained
     * @throws Exception on error
     */
    private void processIntent(File intentFile, Map<String, Trainer> trainers) throws Exception {

        LOGGER.info("----------------------------------------");
        LOGGER.info(String.format("Processing intent file %s", intentFile.getName()));


        /* Load the intent data */

        IntentData intentData = objectFactory.fromJson(intentFile, IntentData.class);


        /* Set up the basic (unconfigured) intent */

        if (intentData.intent == null) throw new Exception("No intent name");

        String intentName = intentData.intent;
        Intent intent = new Intent();
        assistant.addIntent(intentName, intent);


        /* If NER (Named Entity Recognition) is enabled, set it up */

        if (intentData.ners != null) {
            intent.setNers(intentData.ners);
        }


        /* Set up a dummy client session - this is required during processing */

        ClientSession session = new ClientSession(new SessionData(assistant.getIdentity(), "trainer"), assistant);


        /*
         * Perform any required training. Note that categorizers are trained across multiple intents,
         * while NERs are trained only for a single intent. This is why categorizer trainers are passed
         * in, while NER trainers are obtained directly from the NER.
         */
        if (intentData.lessons != null) {
            Trainer.Context context = new Trainer.Context();
            context.identity = assistant.getIdentity();
            context.service = this;
            context.modelDir = modelDir;
            context.logger = LOGGER;

            for (Lesson lesson : intentData.lessons) {

                /* Retrieve any training parameter configuration */

                context.config = new Config();
                if (lesson.parameters != null) context.config.load(lesson.parameters);


                if ("categorizer".equalsIgnoreCase(lesson.type)) {

                    /* We are training a categorizer. Get the categorizer trainer and data */

                    Trainer trainer = trainers.get(lesson.name);
                    if (trainer == null) throw new Exception(String.format("Invalid categorizer '%s' specified in intent trainer", lesson.name));

                    final LanguageData langs = intentData.data.get(lesson.data);
                    if (langs == null) throw new Exception(String.format("Data set '%s' not found in intent", lesson.data));


                    /*
                    * Check to see if we have a pipeline. This is optional, and if not specified
                    * will default to null (an empty pipeline) which will just return an Input
                    * object with a 'document' value.
                    */
                    PreprocessPipeline pipeline = new PreprocessPipeline(assistant, lesson.pipeline);


                    /*
                    * Process the data and submit to the trainer. This is done one language set at a time.
                    */
                    for (String lang : langs.keySet()) {
                        session.getSessionData().setLanguage(lang);
                        List<Input> docs = new ArrayList<Input>();
                        for (String doc : langs.get(lang)) docs.add(pipeline.preprocess(session, doc));

                        trainer.train(context, lang, intentName, docs);
                    }
                }
                else if ("ner".equalsIgnoreCase(lesson.type)) {

                    /* We are training a NER model - check model and trainer */

                    if (intent.getNers() == null || intent.getNers().get(lesson.name) == null) {
                        throw new Exception(String.format("Invalid NER '%s' specified in intent trainer", lesson.name));
                    }

                    NER ner = intent.getNers().get(lesson.name);
                    if (ner == null) throw new Exception(String.format("No such NER '%s'", lesson.name));

                    Trainer trainer = ner.getTrainer();
                    if (trainer == null) throw new Exception(String.format("No trainer available for NER '%s'", lesson.name));


                    /* Get the data to be used for training */

                    final LanguageData langs = intentData.data.get(lesson.data);
                    if (langs == null) throw new Exception(String.format("Data set '%s' not found in intent", lesson.data));


                    /* Set up the preprocessing pipeline (may be a null one) */

                    PreprocessPipeline pipeline = new PreprocessPipeline(assistant, lesson.pipeline);


                    /* Train the NER */

                    for (String lang : langs.keySet()) {
                        LOGGER.info(String.format("Training NER for intent %s and languague %s", intentName, lang));

                        session.getSessionData().setLanguage(lang);
                        List<Input> docs = new ArrayList<Input>();
                        for (String doc : langs.get(lang)) docs.add(pipeline.preprocess(session, doc));

                        trainer.train(context, lang, intentName, docs);
                    }

                    /* Finalize the NER training - this should configure the parameters for the NER */

                    trainer.train(context);

                    LOGGER.info(String.format("Parameters: [%s]", String.join(",", ner.getParameters())));
                }
                else {
                    throw new Exception(String.format("Unsupported training type '%s'", lesson.type));
                }
            }
        }
        else {
            throw new Exception(String.format("No training data for intent '%s'", intentName));
        }


        /*
         * Configure named entities for the intent. At the moment, the only real configuration available for
         * name entities is whether or not the entity value is local or not. If not local, a named entity will
         * be copied into the slots session data. Entities can be explicitly configured (if non-default behavior
         * is required) or the can be automatically populated from the NER.
         */
        NERs ners = intent.getNers();
        if (ners != null) {
            /* If we have some NERs then, by definition, we expect entities (otherwise what is NER for?) */

            Entities entities = intentData.entities;
            if (entities == null) entities = new Entities();

            /* Iterate over each NER */

            for (NER ner : ners.values()) {
                /* For each NER, iterate over parameters */

                for (String name : ner.getParameters()) {
                    if (name.equalsIgnoreCase(intentName)) {
                        throw new Exception(String.format("intent name '%s' cannot be used as an entity name", intentName));
                    }
                    if (name.equalsIgnoreCase("intent")) {
                        throw new Exception("'intent' not permitted as an entity name");
                    }
                    if (!name.matches("^[a-zA-Z_][a-zA-Z0-9_]+$")) {
                        throw new Exception(String.format("Invalid characters in a entity name '%s'", name));
                    }

                    /* If the named entity has not already been configured, add it as a non-local */

                    if (entities.get(name) == null) entities.put(name, new Entity());
                }
            }
            intent.setEntities(entities);
        }
    }


    /**
     * Returns this service's name.
     *
     * @return the service name
     */
    public String getServiceName() {
        return "train";
    }
}



/**
 * Utility hash map class used to store langauge specific data sets.
 */
class LanguageData extends HashMap<String, String[]> {};



/**
 * Utility class used for storing named language data sets. 
 */
class DataSet extends HashMap<String, LanguageData> {};



/**
 * Local data class used to represent a training operatation for either categorizers
 * or named entity recognizers.
 */
class Lesson {
    /**
     * The type of training to be performed - either 'categorizer' or 'ner'.
     */
    public String       type;


    /**
     * Applies only to categorizer training data. Used to identify the name of the categorized being trained. 
     * A given identity may use multiple categorizers - for example, an NLP categorizer as well as a regex categorizer.
     * It is thus necessary to identify the specific categorizer being trained.
     */
    public String       name;


    /**
     * The name of the training data set to be used for training. 
     */
    public String       data;


    /**
     * The name of the processing pipeline to be used for processing the training data.
     */
    public String       pipeline;


    /**
     * Any additional training parameters that should be passed through to the trainer.
     */
    public Properties   parameters;
}



/**
 * Local data class for loading Intents from file. Not to be confused with the assistant.IntentData class.
 */
class IntentData {
    /** The name of the intent */

    public String       intent;


    /** Any named entity recognition (NER) configuration */

    public NERs          ners;


    /** Any pre-configured entities */

    public Entities      entities;


    /** Lessons (training configuration for categorizers and NERs) */

    @SerializedName("train")
    public Lesson[]     lessons;


    /** Training data (may contain multiple sets for langague specific data) */

    public DataSet      data;
}


/**
 * Local class for loading miscellaneous configuration.
 * Most of these values should be defaulted.
 */
class Misc {

    /** The name of the default (out-of-scope) intent */

    @SerializedName("default-intent")
    public String    defaultIntent = "oos";


    /** A list of supported langauges */

    public Set<String>  languages = new HashSet<>(Arrays.asList("en"));


    /** The welcome action */

    public Action    welcome = new ActionUtter("welcome");


    /** The welcome back action */

    @SerializedName("welcome-back")
    public Action    welcomeBack = new ActionUtter("welcome-back");
}