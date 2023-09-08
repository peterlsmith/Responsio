package com.paradoxwebsolutions.core;

import java.util.HashMap;
import java.util.Map;


/**
 * Utility map class.
 *
 * @author Peter Smith
 */
public class GenericMap extends HashMap<String, Object> {


    /**
     * Class constructor.
     */
    public GenericMap() {
    }



    /**
     * Class constructor.
     *
     * @param copy  a base map to copy as the starting point for this instance
     */
    public GenericMap(final GenericMap copy) {
        putAll(copy);
    }



    /**
     * Checks to see if a named configuration parameter exists.
     * @param name the name of the configuration parameter to check
     * @return true if the named value exists, false otherwise
     */
    public boolean has(final String name) {
        assert name != null : "Null name value passed to 'has' method";

        return containsKey(name);
    }



    /**
     * Sets a name/value pair in this map.
     * @param name the name (key) to store the value under
     * @param value the value to store in the map
     * @return a reference to this map
     */
    public GenericMap set(String name, Object value) {
        put(name, value);
        return this;
    }



    /**
     * Returns a named configuration parameter value as an integer.
     * @param name the name of the configuration parameter to get
     * @param default_ the default value to return if the named parameter does not exist
     * @throws ConfigError if the named parameter exists, but could not be parsed as an integer.
     * @return the integer value of the named configuration parameter, or the supplied default
     *          value if the named parameter does not exists.
     */
    public int getInt(final String name, final int default_) throws ConfigError {
        assert name != null : "Null name value passed to getInt method";

        Object value = get(name);
        if (value == null) return default_;
        try {
            if (value instanceof Number)
                return ((Number) value).intValue();
            else
                return Integer.parseInt(value.toString());
        }
        catch(Exception x) {
            throw new ConfigError(String.format("Configuration value mismatch for '%s' - expected integer value, found '%s'", name, value.toString()));
        }
    }



    /**
     * Returns a named configuration parameter as a string.
     * @param name the name of the configuration parameter to get
     * @param default_ the default value to return if the named parameter does not exist
     * @return the string value of the named configuration parameter, or the supplied default
     *          value if the named parameter does not exists.
     */
    public String getString(final String name, final String default_) {
        assert name != null : "Null name value passed to getString method";

        Object value = get(name);
        return value == null ? default_ : value.toString();
    }



    /**
     * Returns a named configuration parameter as a string.
     * @param name the name of the configuration parameter to get
     * @return the string value of the named configuration parameter, or null if the
     *          named parameter does not exist.
     */
    public String getString(final String name) {
        return getString(name, null);
    }



    /**
     * Returns a named configuration parameter value as a boolean.
     *
     * @param name the name of the configuration parameter to get
     * @param default_ the default value to return if the named parameter does not exist
     * @throws ConfigError if the named parameter exists, but could not be parsed as a a boolean.
     * @return the boolean value of the named configuration parameter, or the supplied default
     *          value if the named parameter does not exists.
     */
    public boolean getBool(final String name, final boolean default_) throws ConfigError {
        assert name != null : "Null name value passed to getBool method";

        Object value = get(name);
        if (value == null) return default_;
        try {
            if (value instanceof Boolean)
                return (Boolean) value;
            else
                return Boolean.parseBoolean(value.toString());
        }
        catch(Exception x) {
            throw new ConfigError(String.format("Configuration value mismatch for '%s' - expected boolean value, found '%s'", name, value.toString()));
        }
    }



    /**
     * Returns a named configuration parameter value as a floating point number.
     * @param name the name of the configuration parameter to get
     * @param default_ the default value to return if the named parameter does not exist
     * @throws ConfigError if the named parameter exists, but could not be parsed as a a floating point number.
     * @return the floating point value of the named configuration parameter, or the supplied default
     *          value if the named parameter does not exists.
     */
   public double getFloat(final String name, final double default_) throws ConfigError {
        assert name != null : "Null name value passed to getFloat method";

        Object value = get(name);
        if (value == null) return default_;
        try {
            if (value instanceof Number)
                return ((Number) value).doubleValue();
            else
                return Double.parseDouble(value.toString());
        }
        catch(Exception x) {
            throw new ConfigError(String.format("Configuration value mismatch for '%s' - expected floating point value, found '%s'", name, value.toString()));
        }

    }



    /**
     * Returns an array of all the parameter names in this GenericMap instance.
     * @return an array of Strings containing the names of all parameters in this
     *          GenericMap instance.
     */
    public String[] getNames() {
        return keySet().toArray(new String[size()]);
    }



    /**
     * Sets the value of a named configuration parameter.
     * If the named parameter already exists, its value will be replaced, otherwise
     * the named parameter will be created.
     * @param name the name of the parameter value to set
     * @param value the new parameter value
     * @return a reference to this GenericMap instance
     */
    public GenericMap setString(final String name, final String value) {
        set(name, value);
        return this;
    }



    /**
     * Sets the value of a named configuration parameter.
     * If the named parameter already exists, its value will be replaced, otherwise
     * the named parameter will be created.
     * @param name the name of the parameter value to set
     * @param value the new parameter value
     * @return a reference to this GenericMap instance
     */
    public GenericMap setBool(final String name, final boolean value) {
        set(name, Boolean.toString(value));
        return this;
    }

}
