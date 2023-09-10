package com.paradoxwebsolutions.assistant;

import com.paradoxwebsolutions.core.ClassInitializer;
import com.paradoxwebsolutions.core.ClassLoader;
import com.paradoxwebsolutions.core.Config;
import com.paradoxwebsolutions.core.ApplicationError;
import com.paradoxwebsolutions.core.CustomLogHandler;
import com.paradoxwebsolutions.core.GenericMap;
import com.paradoxwebsolutions.core.Logger;
import com.paradoxwebsolutions.core.ObjectInitializer;

import java.io.File;
import java.io.FileInputStream;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Stack;


/**
 * Provides high level management of a client interaction with an assistant (chatbot).
 * <p>The Agent class acts as a coordinator between the client (user) and the assistant
 * (chatbot). It deals with matching the client responses against the storylines, as well
 * as action execution and returning the resulting responses to the client.
 *
 * @author Peter Smith
 * @see Assistant
 */
public class Agent {

    /** Logger */
    
    private Logger LOGGER = null;

    
    /** This assistant identity (name) */

    String identity;

    
    /** The default language for this reporter */

    String lang;


    /** The Interpreter used by this assistant */

    private Interpreter interpreter;


    /** Assistant */

    private Assistant assistant;


    /** Miscellaneous agent config */

    private GenericMap config;



    /**
     * Creates an agent instance.
     *
     * @param config            identity specific configuration for this agent
     * @throws ApplicationError if an error occurs during initialization
     */
    public Agent(final Config config) throws ApplicationError {

        assert config != null : "Null identity configuration passed to Agent";

        /* Get basic configuration - our identity and the location of our archive */

        identity = config.getString("identity");
        if (identity == null) throw new ApplicationError("Invalid assistant configuration - no identity name");

        String identityDir = config.getString("dir.identity");


        /* Set up the identity archive so we can load data from it */

        IdentityArchive archive = new IdentityArchive(identityDir + File.separator + identity + ".zip");


        /*
         * Create a class loader and initializer for this identity and load and extensions from the
         * archive - we might need these to deserialize the assistant itself.
         */
        ClassLoader classLoader = new ClassLoader();
        ClassInitializer classInitializer = new ClassInitializer();

        for (String extension : archive.getFiles("extensions/.*\\.class")) {
            Class<?> cls = classLoader.loadClass(archive.getInputStream(extension));
            classInitializer.initialize(cls, config, archive);
        }


        /* Deserialize the assistant controller */

        assistant = (new AssistantFactory(classLoader)).fromJson(archive.getInputStream("assistant.json"), Assistant.class);


        /* Verify configuration */

        this.config = config;
        lang = config.getString("lang", "en");


        /* Set up any configured logger for this identity */

        LOGGER = new Logger(identity, config.getConfig("log"));
        LOGGER.info("Initializing Agent");

        /* Initialize the assistant */

        new ObjectInitializer().initialize(assistant, assistant, config, LOGGER, archive);


        /* Create the interpreter */

        interpreter = new Interpreter(assistant, config);

        LOGGER.info("Agent initialized");
    }



    /**
     * Process a chat input message.
     * <p>This processes a new input message from the client in the context of any
     * previous conversaion.
     *
     * @param session  the current client session
     * @param input    the new client input message
     * @return         a response object to be returned to the client
     * @throws ApplicationError on error
     */
    public ClientResponse processChatInput(ClientSession session, String input) throws ApplicationError {
        session.info(String.format("Received input: %s", input));

        IntentData userIntent = this.interpreter.getIntent(session, input);
        if (userIntent == null)  {
            /* If no intent could be recognized, default */

            userIntent = new IntentData(assistant.getDefaultIntent());
            session.debug(String.format("Defaulting to '%s' intent", userIntent.getName()));
        }

        /* Set the slots to a copy of the current slot state (it must be immutable) */

        userIntent.setSlots(new GenericMap(session.getSlots()));


        /* Add the new intent and the current slot status to the history */

        session.getSessionData().addHistory(userIntent);


        /* Now try and match the narrative against the stories */

        Stack<Narrative> narratives = new Stack<Narrative>();
        List<IntentData> history = Arrays.asList(session.getSessionData().getHistory());
        boolean historyChanged = false; /* tracks whether or not the history needs to be updated */


        while (history.size() > 0) {
            session.info(String.format("History: [%s]", String.join(",", history.stream().map((h) -> h.name).toArray(String[]::new))));
            session.debug("Testing narrative against stories");

            Narrative bestNarrative = null;
            boolean   bestComplete = false;

            for (Map.Entry<String, Story> entry : assistant.getStories().entrySet()) {

                /* Create the narrative for this story and match */

                Story story = entry.getValue();
                Narrative narrative = new Narrative(entry.getKey(), history);
                boolean isComplete = story.match(assistant, session, narrative);

                if (narrative.getIndex() > 0) { 
                    /*
                     * If we consumed any of the narrative, check the path length and score to see if this
                     * is a new 'best'.
                     */
                    session.debug(String.format("Story '%s' matches with path length %d and score %f", entry.getKey(), narrative.getIndex(), narrative.getScore()));

                    if (bestNarrative == null || narrative.getIndex() > bestNarrative.getIndex() || 
                            (narrative.getIndex() == bestNarrative.getIndex() && narrative.getScore() > bestNarrative.getScore())) {
                        bestNarrative = narrative;
                        bestComplete = isComplete;
                        session.debug(String.format("Found new best story '%s'", entry.getKey()));
                    }
                }
            }            

            /* 
             * Note that the situation of not finding a best narrative should not occur if the stories have
             * been designed properly, as there should be a 'catch all' story. Ideally, the trainer should be
             * able to verify this.
             */
            if (bestNarrative == null) return null;


            /*
             * If the story is complete and we have more narrative, we can discard the chunk of history it represents.
             * Otherwise, we push the narrative on the stack and continue processing any remaining paths (this
             * indicates a break in a story). It is possible that we may come back to it with more client input
             * though.
             */
            if (bestComplete && bestNarrative.hasMore()) {
                /*
                 * If a story is complete, we discard the path steps it represents from the history.
                 * If we have a previously matched story, we throw away the previous story and rematch,
                 * just in case there is a continuation of that story.
                 */
                session.debug(String.format("Discarding matched narrative segment for story '%s'", bestNarrative.getName()));
                historyChanged = true;

                if (narratives.size() > 0) {
                    /* Reset the history, but excluding the bit consumed by the completed story. Also, pop the last story. */

                    history = new ArrayList<IntentData>(narratives.pop().getUsed());
                    history.addAll(bestNarrative.getRemaining());
                    session.debug("Discarding last matched story for rematch");
                }
                else {
                    /* If there is no previous story, just update the history */

                    history = bestNarrative.getRemaining();
                }
            }
            else {
                narratives.push(bestNarrative);
                history = bestNarrative.getRemaining();
            }
        }


        session.info(String.format("Narrative: %s",
            String.join(",",
                narratives.stream().map(
                    (n) -> n.getName() + "[" + String.join(",", n.getUsed().stream().map((h) -> h.name).toArray(String[]::new)) + "]"
                ).toArray(String[]::new)
            )));


        /* Make sure any updates to the history are preserved in the session */

        if (historyChanged) {
            history = new ArrayList<IntentData>();
            for (Narrative narrative : narratives) history.addAll(narrative.getUsed());
            session.getSessionData().setHistory(history.toArray(new IntentData[history.size()]));
        }


        /* Get the most recent best narrative and execute its action */

        return executeAction(session, narratives.pop().getAction());
    }



    /**
     * Execute any configured welcome actions.
     * <p>Welcome actions are executed when a new client connects to the chat services. This
     * can be used, for example, to present introductory messages.
     *
     * @param session  the newly establish client session
     * @return         a response object to be returned to the client
     * @throws ApplicationError on error
     */
    public ClientResponse doWelcome(ClientSession session) throws ApplicationError {
        return executeAction(session, assistant.getWelcomeAction());
    }



    /**
     * Execute any configured welcome back actions.
     * <p>Welcome back actions are executed when a client re-connects to the chat services. This
     * can be used, for example, to reminder messages.
     *
     * @param session  the newly establish client session
     * @return         a response object to be returned to the client
     * @throws ApplicationError on error
     */
    public ClientResponse doWelcomeBack(ClientSession session) throws ApplicationError {
        return executeAction(session, assistant.getWelcomeBackAction());
    }



    /**
     * Executes a given action in the context of a client session.
     * 
     * @param session  the client session
     * @param action   the action to execute
     * @return         a response object to be returned to the client
     * @throws ApplicationError on error
     */
    private ClientResponse executeAction(ClientSession session, Action action) throws ApplicationError {
        ClientResponse response = new ClientResponse();
        if (action != null) action.perform(assistant, session, response);
        return response;
    }



    /**
     * Return the assistant configuration instance.
     *
     * @return the assistant configuration instance
     */
    public Assistant getAssistant() {
        return this.assistant;
    }



    /**
     * Return the agent miscellaneous configuration.
     *
     * @return the agent miscellaneous configuration 
     */
    public GenericMap getConfig() {
        return this.config;
    }



    /**
     * Return the default language for this assistant/agent.
     *
     * @return the default language for this assistant/agent
     */
    public String getDefaultLanguage() {
        return this.config.getString("lang", "en");
    }

}





