package com.paradoxwebsolutions.core;

import java.io.File;
import java.io.FileInputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.Set;
import java.util.stream.Stream;


/**
 * Manages application configuration information.
 * Instances of this class can be used to load and access configuration data.
 *
 * @author Peter Smith
 */
public class Config extends GenericMap {

    /**
     * Pattern used to look for replaceable '${name}' values in configuration
     * data.
     */
    private static Pattern regex = Pattern.compile("\\$\\{([^}]+)}");



    /**
     * Loads configuration data from a file.
     * Note that upon loading, parameter replacements will be performed in the
     * configuration values.
     *
     * @param file The file to load the configuration from.
     * @return a reference to this Config instance
     * @throws ConfigError if the configuration data could not be loaded or parsed.
     */
    public Config load(final File file) throws ConfigError {
        assert file != null : "Null file object passed to load";

        try {
            Properties p = new Properties();
            p.load(new FileInputStream(file));

            load(p);
        }
        catch(Exception x) {
            throw new ConfigError(String.format("Invalid configuration file passed to config 'load': %s", x.getMessage()));
        }

        return this;
    }



    /**
     * Loads configuration data from a properties set.
     * Note that upon loading, parameter replacements will be performed on the
     * configuration values.
     *
     * @param properties  the properties to load.
     * @return a reference to this Config instance
     */
    public Config load(final Properties properties) {
        assert properties != null : "Null Properties object passed to load";

        for (String key : properties.stringPropertyNames()) {
            put(key, replace(properties.getProperty(key)));
        }
        return this;
    }



    /**
     * Loads configuration data from another Config instance.
     * Note that upon loading, parameter replacements will be performed in the
     * configuration values.
     *
     * @param config  the configuration to load.
     * @return a reference to this Config instance
     */
    public Config load(final GenericMap config) {
        assert config != null : "Null config object passed to load";

        putAll(config);
        return this;
    }




    /**
     * Return a subset of configuration options.
     *
     * @param name  the name prefix of the options to return. This prefix will be stripped from
     *              parameter names in the returned configuration instance.
     * @return      a configuration instance containing the subset of configuration options
     */
    public Config getConfig(final String name) {
        assert name != null : "Null name value passed to getConfig method";

        Config config = new Config();
        String prefix = name.length() == 0 ? "" : name + '.';

        for (String key : keySet()) {
            if (key.startsWith(prefix)) config.put(key.substring(prefix.length()), get(key));
        }

        return config; 
    }




    /**
     * Returns a named configuration parameter broken down into a list.
     * <p>This converts a string value into a list by splitting it on 
     * whitespace and/or comma.
     *
     * @param name the name of the configuration parameter to get
     * @return the string value of the named configuration parameter, or null if the
     *          named parameter does not exist.
     */
    public String[] getList(final String name) {
        return getList(name, null);
   }


    /**
     * Returns a named configuration parameter broken down into a list.
     * <p>This converts a string value into a list by splitting it on 
     * whitespace and/or comma.
     *
     * @param name      the name of the configuration parameter to get
     * @param default_  the default value to return if the named value does not exist
     * @return the string value of the named configuration parameter, or null if the
     *          named parameter does not exist.
     */
    public String[] getList(final String name, final String[] default_) {
        final String str = getString(name);
        if (str == null) return default_;

        return Arrays.stream(str.split("[\\s,]+")).filter(w -> w.length() > 0).toArray(String[]::new);
    }



    /**
     * Returns an array of all the parameter names in this Config instance.
     * @return an array of Strings containing the names of all parameters in this
     *          Config instance.
     */
//    public String[] getNames() {
//        return config.stringPropertyNames().toArray(new String[config.size()]);
//    }



    /**
     * Converts this Config instance into properties type string.
     * The format of the output is suitable for loading back into a Config instance
     * using the load(File) method.
     * @return a string representation of this Config instance
     */
    public String toString() {
        StringBuilder output = new StringBuilder();

        for (String key : getNames()) {
            String row = String.format("%s = %s\n", key, get(key));
            output.append(row);
        }

        return output.toString();
    }



    /**
     * Performs parameter replacement on configuration values.
     * Searches for a parameter mapping of the form '${name}' in a configuration
     * value, and replaces it with the named configuration value.
     * @param value the configuration value to process
     * @return the processed configuration value, or the original string value if no
     *           replacements were performed.
     */
    private String replace(String value) {

        if (value.contains("${")) {
            Matcher m = Config.regex.matcher(value);

            if (m.groupCount() > 0) {
                while (m.find()) {
                    String replacement = getString(m.group(1));
                    if (replacement != null) {
                        value = value.replace(m.group(), replacement);
                    }
                }
            }
        }
        return value;
    }

}