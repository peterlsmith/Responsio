package com.paradoxwebsolutions.assistant;

import com.paradoxwebsolutions.core.GenericMap;

import java.util.ArrayList;
import java.util.List;



/**
 * Represents a response to be returned to the client.
 * <p>A client response consists of a number of commands - key value pairs that
 * tell the client to do something, such as display some text for the user to 
 * read, or terminate the client session.
 *
 * @see ClientCommand
 * @author Peter Smith
 */
public class ClientResponse {

    /** List of response commands to be returned to client */

    private List<ClientCommand> commands = new ArrayList<ClientCommand>();


    /**
     * Adds a new command to be returned to the client.
     *
     * @param command  the command for the client to execute
     * @param data     the command data (may be null)
     */
    public void addCommand(String command, Object data) {
        commands.add(new ClientCommand(command, data));
    }


    /**
     * Appends another client response to this one.
     *
     * @param other  the other client response to append
     * @return       a reference to this instance
     */
    public ClientResponse append(ClientResponse other) {
        commands.addAll(other.commands);
        return this;
    }


    /**
     * Utility method for adding a text response to be displayed to the client.
     *
     * @param response  the text response to be displayed to the client
     */
    public void utter(String response) {
        addCommand("text", response);
    }


    /**
     * Utility class for wrapping client commands.
     *
     * @see ClientResponse
     * @author Peter Smith
     */
    private static class ClientCommand {

        /** The command for the client to execute */

        private String command;


        /** The data associated with the command */

        private Object data;


        /**
         * Creates a new client command instance.
         *
         * @param command  the command for the client to execute
         * @param data     the command data (may be null)
         */
        public ClientCommand(String command, Object data) {
            this.command = command;
            this.data = data;
        }
    }
}