package com.paradoxwebsolutions.bot;

import com.paradoxwebsolutions.core.Config;
import com.paradoxwebsolutions.core.ApplicationError;
import com.paradoxwebsolutions.core.ConfigError;
import com.paradoxwebsolutions.core.CustomLogHandler;
import com.paradoxwebsolutions.core.HttpError;
import com.paradoxwebsolutions.core.ObjectFactory;
import com.paradoxwebsolutions.core.StringMap;
import com.paradoxwebsolutions.assistant.Agent;
import com.paradoxwebsolutions.assistant.Assistant;
import com.paradoxwebsolutions.assistant.AssistantFactory;
import com.paradoxwebsolutions.assistant.ClientResponse;
import com.paradoxwebsolutions.assistant.ClientSession;
import com.paradoxwebsolutions.assistant.IdentityArchive;
import com.paradoxwebsolutions.assistant.SessionData;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;



/**
 * Chat service implementation.
 *
 * @author Peter Smith
 */
public class ChatService extends BotService {
    
    /** An array of the supported identities for this service instance */

    private List<String> identities;


    /** Loaded agents. This needs to be thread safe */

    private Map<String, Agent>  agents = new Hashtable<String, Agent>();


    /** The Gson response output formatter (thread safe) */

    private Gson gson = new GsonBuilder().disableHtmlEscaping().setPrettyPrinting().create();



    /**
     * Initializes the chat service. 
     * <p>This performs chat service specific initialization.
     *
     * @throws ServletException on initialization error
     */ 
    public void serviceInit() throws ServletException {

        /* Get a list of the supported identities */

        identities = Arrays.stream(getConfig().getString("identities").split("[\\s,]+"))
            .filter(id -> id.length() > 0)
            .collect(Collectors.toList());

        if (identities.size() == 0) throw new ServletException("No configured identities");
        
        LOGGER.info("Supported identities: " + String.join(", ", this.identities));
    }
    


    /**
     * Handles an OPTIONS request (CORS preflight requests).
     *
     * @param request  the client request
     * @param response  the response to be returned to the client
     */    
    public void doOptions(HttpServletRequest request, HttpServletResponse response) throws IOException {

        try {
            /* Get the chat identity and verify */

            String[] paths = request.getPathInfo().split("/");
            if (paths.length < 3) throw new HttpError(400, "Invalid request");
            String command = paths[1];
            String identity = paths[2];

            if (!this.identities.contains(identity)) {
                throw new HttpError(400, String.format("Unsupported identity '%s'", identity));
            }
            LOGGER.info(String.format("Received %s OPTIONS request for %s", command, identity));


            /* Check request has an origin - this is required */

            final String origin = request.getHeader("origin");
            if (origin == null || origin.trim().length() == 0) {
                throw new HttpError(400, "Invalid request");
            }

            response.setStatus(HttpServletResponse.SC_OK);
            response.addHeader("Access-Control-Allow-Origin", origin);
            response.addHeader("Vary", "Origin");
            response.addHeader("Access-Control-Allow-Methods", "GET,POST,OPTIONS");
            response.addHeader("Access-Control-Allow-Headers", "Content-Type");
            response.addHeader("Access-Control-Allow-Credentials", "true");
        }
        catch (HttpError x) {
            LOGGER.info("ERROR: " + x.getMessage());
            
            JsonObject error = new JsonObject();
            error.addProperty("error", x.getMessage());

            response.setContentType("application/json");
            response.setStatus(x.getStatus());
            response.getWriter().write(error.toString());

        }
        response.getWriter().flush();
    }


    /**
     * Execute a client request received as an http get request.
     *
     * @param request  the client request
     * @param response  the response to be returned to the client
     * @throws IOException if there was an error writing the response
     */
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        process(request, response);
    }


    /**
     * Execute a client request received as an http post request.
     *
     * @param request  the client request
     * @param response  the response to be returned to the client
     * @throws IOException if there was an error writing the response
     */    
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        process(request, response);
    }


    /**
     * Execute a client request received as an http post request.
     *
     * @param request  the client request
     * @param response  the response to be returned to the client
     * @throws IOException if there was an error writing the response
     */    
    private void process(HttpServletRequest request, HttpServletResponse response) throws IOException {

        String origin = "*";

        try {
            /* Get the chat identity and verify */

            String[] paths = request.getPathInfo().split("/");
            if (paths.length < 3) throw new Exception("Invalid request");
            String command = paths[1];
            String identity = paths[2];

            if (!this.identities.contains(identity)) throw new Exception(String.format("Unsupported identity '%s'", identity));


            /*
             * Check request has an origin or at least referrer - this is required. Note that this does not really provide any
             * real security since they can be faked, but it is at least a hurdle.
             */
            origin = request.getHeader("origin");
            if (origin == null && request.getHeader("referer") != null) { /* Handle test page from same domain (no origin) */
                URL referer = new URL(request.getHeader("referer"));
                origin = referer.getProtocol() + "://" + referer.getHost();
                if (referer.getPort() != referer.getDefaultPort()) origin += ":" + referer.getPort();
            }
            else if (origin == null || origin.trim().length() == 0) {
                throw new ApplicationError("Invalid request");
            }


            /* Verify origin is valid for identity (or user is authorized) */

            if (request.getUserPrincipal() == null) {
                if (!this.agents.containsKey(identity)) {
                    this.agents.put(identity, createAgent(identity));
                }
                final Agent agent = this.agents.get(identity);

                final List<String> allowedOrigins = Arrays.stream(agent.getConfig().getString("origins").split("[\\s,]+"))
                    .filter(o -> o.length() > 0)
                    .collect(Collectors.toList());
                if (!allowedOrigins.contains(origin)) {
                    throw new ApplicationError("Permission");
                }
            }


            /* Process the request */

            LOGGER.info(String.format("Handling '%s' request for identity '%s'", command, identity));

            ClientResponse clientResponse = null;
            if (command.equals("chat")) {
                clientResponse = doChat(identity, request);
            }
            else if (command.equals("init")) {
                clientResponse = doInit(identity, request);
            }
            else if (command.equals("refresh")) {
                doRefresh(identity, request);
            }
            else
                throw new Exception(String.format("Invalid request '%s'", command));
          

            /* Output the response */

            response.addHeader("Access-Control-Allow-Origin", origin);            
            response.addHeader("Access-Control-Allow-Credentials", "true");
            response.setContentType("application/json");
            response.setStatus(HttpServletResponse.SC_OK);

            response.getWriter().write(gson.toJson(clientResponse));
        }
        catch (Exception x) {
            /* An error was generated, so return the appropriate error response */
            LOGGER.info("ERROR: " + x.getMessage());
            
            JsonObject error = new JsonObject();
            error.addProperty("error", x.getMessage());

            response.addHeader("Access-Control-Allow-Origin", origin);            
            response.addHeader("Access-Control-Allow-Credentials", "true");
            response.setContentType("application/json");
            response.setStatus(400);
            response.getWriter().write(error.toString());
        }
        response.getWriter().flush();
    }
    


    /**
     * Creates a named agent instance.
     *
     * @param identity  the name of the agent to create
     * @return An agent instance for the named agent
     * @throws ApplicationError on error
     * @see Agent
     */
    private Agent createAgent(String identity) throws ApplicationError {
        try {

            /* Get identify specific configuration from global configuration */

            Config localConfig = getConfig().getConfig("identity.default").load(getConfig().getConfig("identity." + identity));
            String modelDir = getConfig().getString("dir.identity") + File.separator + identity;
            localConfig.setString("dir.identity", modelDir);
            localConfig.setString("identity", identity);
            localConfig.setString("dir.root", getConfig().getString("dir.root"));


            /* Look for an identity local configuration file */

            File localConfigFile = new File(modelDir + File.separator + "identity.properties");
            if (localConfigFile.exists()) {
                localConfig.load(localConfigFile);
                LOGGER.info(String.format("Loaded local configuration file for identity '%s'", identity));
            }

            return new Agent(localConfig);
        }
        catch (Exception x) {
            throw new ApplicationError(String.format("Unable to initialize assistant for identity '%s': %s", identity, x.getMessage()));
        }
    }



    /**
     * Services a chat message.
     *
     * @param identity  the identity of the assistant being chatted with
     * @param request   the client HTTP request
     * @return          A {@link ClientResponse} instance represeting the response to be returned to the client
     * @throws Exception on error. 
     */
    private ClientResponse doChat(String identity, HttpServletRequest request) throws Exception {

        /* Check for input */

        StringMap parameters = getParameters(request);
        String input = parameters.get("input");
        if (input == null || input.trim().length() == 0) {
            throw new Exception("No user input provided");
        }


        /* Load agent/assistant for identity */

        final Agent agent = this.agents.get(identity);


        /* Get the client session information */

        SessionData sessionData = (SessionData) request.getSession().getAttribute("agent." + identity);

        if (sessionData == null) {

            /* 
             * Assuming the client followed the protocol and called the 'init' endpoint
             * before beginning to chat, this situation indicates a client has been sitting 
             * idle long enough for the session to expire. We therefore follow the 
             * 'welcome back' protocol.
             */
            sessionData = new SessionData(identity, request.getSession().getId());
            request.getSession().setAttribute("agent." + identity, sessionData);


            /* Set the default langauge for the session */

            sessionData.setLanguage(agent.getDefaultLanguage());
            ClientSession clientSession = new ClientSession(sessionData, agent.getAssistant());

            return agent.doWelcomeBack(clientSession).append(agent.processChatInput(clientSession, input));
        }
        else {
            ClientSession clientSession = new ClientSession(sessionData, agent.getAssistant());

            return agent.processChatInput(clientSession, input);
        }
    }



    /**
     * Execute a client initialization command.
     * <p>This command is invoked when a client is first initialized (e.g. the web page is loaded).
     *
     * @param identity  the identity of the assistant being chatted with
     * @param request   the client HTTP request
     * @return          A {@link ClientResponse} instance represeting the response to be returned to the client
     * @throws          ApplicationError on error
     */
    private ClientResponse doInit(String identity, HttpServletRequest request) throws ApplicationError {

        /* Load agent/assistant for identity */

        final Agent agent = this.agents.get(identity);


        /* Look for a client configuration file */

        File clientFile = new File(agent.getConfig().getString("dir.identity") + File.separator + "client.json");
        JsonObject clientConfig;
    
        try {
            if (clientFile.exists())
                clientConfig = new ObjectFactory().fromJson(new FileReader(clientFile)).getAsJsonObject();
            else
                clientConfig = new JsonObject();
        }
        catch (Exception x) {
            throw new ApplicationError("Unable to load client configuration file");
        }

        /* Set up the basic 'init' client response */

        ClientResponse response = new ClientResponse();
        response.addCommand("init", clientConfig);


        /*
         * Check to see if we have an existing session - in which case this is a returning
         * user. If there is no session, then it is considered a 'first time' user (or one that
         * has gone long enough for the session be forgotten). The response is difference in these
         * two cases.
         */
        SessionData sessionData = (SessionData) request.getSession().getAttribute("agent." + identity);
        if (sessionData == null) {
            /*
             * This is a first time user, so we need to set up a session and perform
             * a 'welcome' action.
             */
            sessionData = new SessionData(identity, request.getSession().getId());
            request.getSession().setAttribute("agent." + identity, sessionData);
            sessionData.setLanguage(agent.getDefaultLanguage());
            ClientSession clientSession = new ClientSession(sessionData, agent.getAssistant());

            response.addCommand("reset", null);
            response.append(agent.doWelcome(clientSession));
        }
        else {
            /*
             * This is a returning user - most likely a page reload. Check time since last
             * activity to see if we should do a 'welcome back'
             */
            long timeSinceLastActivity = System.currentTimeMillis() - sessionData.getTimestamp();
            ClientSession clientSession = new ClientSession(sessionData, agent.getAssistant());

            response.addCommand("restore", null);
            if (timeSinceLastActivity > 600000) {
                response.append(agent.doWelcomeBack(clientSession));
            }
        }

        return response;
    }



    /**
     * Execute a refresh command.
     * <p>This replaces any previous asssistant and will not cause an issue if the
     * previous assistant was in use at the time.
     * @param identity  the identity of the assistant being chatted with
     * @param request   the client HTTP request
     * @throws ApplicationError on error
     */
    private void doRefresh(String identity, HttpServletRequest request) throws ApplicationError {

        if (request.getUserPrincipal() == null) throw new ApplicationError("Login required");

        String user = request.getUserPrincipal().getName();
        LOGGER.info(String.format("Refresh assistant '%s' requested by user '%s'", identity, user));

        this.agents.put(identity, createAgent(identity));
    }



    /**
     * Extracts the request parameters.
     *
     * @param request  the client request
     * @return         a string map of the request parameters
     * @throws IOException if there was an error writing the response
     */    
    private StringMap getParameters(HttpServletRequest request) throws Exception {
        StringMap parameters = null;

        if (request.getMethod().equals("GET")) {
            parameters = new StringMap();

            for (Map.Entry<String, String[]> entry : request.getParameterMap().entrySet()) {
                parameters.put(entry.getKey(), entry.getValue()[0]);
            }
        }
        else if (request.getMethod().equals("POST")) {
            parameters = new ObjectFactory().fromJson(request.getInputStream(), StringMap.class);
        }

        return parameters;
    }

}
