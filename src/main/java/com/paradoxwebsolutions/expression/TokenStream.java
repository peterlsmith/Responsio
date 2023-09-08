package com.paradoxwebsolutions.expression;



/**
 * Supports the parsing of tokens from an expression.
 * <p>This is a utility class intended to make processing the expression tokens 
 * easier.
 *
 * @author Peter Smith
 */
public class TokenStream {

    /** An array of strings representing the expression tokens */

    private String[] tokens;


    /** The current 'read' position in the token stream */

    private int      next = 0;



    /**
     * Creates a token stream instance with the given tokens.
     *
     * @param tokens  the expression tokens
     */
    public TokenStream(String[] tokens) {
        this.tokens = tokens;
    }



    /**
     * Removes and returns the next token from the stream.
     *
     * @return the next unread token or null if there are no more tokens.
     */
    public String next() {
        if (this.next < this.tokens.length)
            return this.tokens[this.next++];
        else
            return null;
    }


    /**
     * Looks at the next token in the stream.
     * <p>This does not advance the stream position.
     *
     * @return the next unread token or null if there are no more tokens.
     */
    public String peek() {
        if (this.next < this.tokens.length)
            return this.tokens[this.next];
        else
            return null;
    }
}