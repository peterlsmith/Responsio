package com.paradoxwebsolutions.tools;

import com.paradoxwebsolutions.assistant.Agent;
import com.paradoxwebsolutions.assistant.Assistant;
import com.paradoxwebsolutions.assistant.AssistantFactory;
import com.paradoxwebsolutions.assistant.ClientResponse;
import com.paradoxwebsolutions.assistant.ClientSession;
import com.paradoxwebsolutions.assistant.IdentityClassLoader;
import com.paradoxwebsolutions.assistant.SessionData;
import com.paradoxwebsolutions.core.Config;
import com.paradoxwebsolutions.core.CustomConsoleHandler;
import com.paradoxwebsolutions.core.ObjectFactory;
import com.paradoxwebsolutions.core.ServiceAPI;


import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.logging.Level;
import java.util.logging.Logger;


import java.lang.reflect.Method;
import java.lang.reflect.Parameter;


/**
 * Test driver for interactive chatting with an identity bot on the command line
 */
class Chat extends Tool {


    /**
     * Command line tool entry point.
     *
     * @param args  the command line arguments. Currently only a single argument representing the name
     *              of the assistant identity is expected.
     */
    public static void main(String args[]) {
        try {
            if (args.length == 0) throw new Exception("No identity specified");

            Chat chat = new Chat(args[0]);
            chat.doChat();
        }
        catch (Exception x) {
            x.printStackTrace();
        }
    }


    /* The chat agent */

    private Agent agent;


    /* The chat session */

    private ClientSession session;



    /**
     * Initializes the chat agent.
     * 
     * @param identity  the identity of the chat assistant
     * @throws Exception on any error
     */
    public Chat(final String identity) throws Exception {
        super();

        /* Get identity specific configuration required by assistant */

        Config localConfig = config.getConfig("identity.default").load(config.getConfig("identity." + identity));
        String modelDir = config.getString("dir.model") + File.separator + identity;
        localConfig.setString("dir.model", modelDir);
        localConfig.setString("identity", identity);


        /* Create a class loader for this identity so it can isolate any custom code */

        IdentityClassLoader classLoader = new IdentityClassLoader(this);
        classLoader.loadClasses(modelDir + File.separator + "extensions");


        /* Deserialize the assistant controller */

        Assistant assistant = (new AssistantFactory(classLoader)).fromJson(new File(modelDir + File.separator + "assistant.json"), Assistant.class);
        agent = new Agent(assistant, localConfig);


        /* Set up a session */

        SessionData sessionData = new SessionData(identity, "test-user");
        session = new ClientSession(sessionData, assistant);


        /* Disable some annoying networking logging */

        Logger.getLogger("jdk.internal.httpclient.websocket.debug").setLevel(Level.WARNING);
        Logger.getLogger("jdk.internal.httpclient.hpack.debug").setLevel(Level.WARNING);
        Logger.getLogger("jdk.internal.httpclient.debug").setLevel(Level.WARNING);
        Logger.getLogger("jdk.event.security").setLevel(Level.WARNING);
    }



    /**
     * Returns the name of this service.
     *
     * @return  the name of this service
     */
    public String getServiceName() {
        return "chat";
    }



    /**
     * Chat with the user.
     * <p>This repeatedly prompts for input from the user and submits it to the chat service. The
     * response is then mapped to Json and output.
     *
     * @throws Exception  on any error
     */
    public void doChat() throws Exception {

        ObjectFactory factory = new ObjectFactory();

        /* Start off with any welcome */

        ClientResponse response = agent.doWelcome(session);
        System.out.println(factory.toJson(response));


        /* Now start accepting input */

        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

        while (true) {
            System.out.print("Ready> ");
            String input = reader.readLine();
            if (input == null) break;
            input = input.trim();
            if (input.length() > 0) {
                response = agent.processChatInput(session, input);
                System.out.println(factory.toJson(response));
            }
        }

        System.out.println("Goodbye");
    }


}