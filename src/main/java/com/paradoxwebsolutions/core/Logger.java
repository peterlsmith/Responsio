package com.paradoxwebsolutions.core;


import java.io.File;
import java.util.logging.Handler;
import java.util.logging.Level;

/**
 * Service application logger class.
 * <p>This class is used to provide a simplified logging interface to the application code. It provides 
 * four logging levels as follows:
 * <ul>
 *  <li>error - use for critical errors/exceptions or other abnormal conditions
 *  <li>warning - used for non critical conditions that may by important
 *  <li>info - general logging messages normally output operationally
 *  <li>debug - detailed logging messages, not normally output operationally
 * </ul>
 * <p>If configured, it will output the logging to a log file based on the log name. Log files will be
 * rotated in an orderly manner, with old logs expiring (being cleaned up), based on configuration.
 * @author Peter Smith
 */
public class Logger {

    /** Constant defining the DEBUG log level */

    public static final Level DEBUG = Level.FINE;


    /** Constant defining the INFO log level */

    public static final Level INFO = Level.INFO;


    /** Constant defining the TRACE log level */

    public static final Level TRACE = Level.FINEST;



    /** The underlying Logger instance used to handle log records */

    private java.util.logging.Logger logger;



    /**
     * Creates a named logger with the given configuration.
     * <p>Refer to {@link #configure(String, Config)} for available configuration parameters.
     *
     * @param name   the name of the logger. This can be used to identify the logger at a later point
     * @param config the logger configuration. This may be null, in which case no configuration will
     *               be applied to the logger and it will inherit any system default configuration.
     * @throws ApplicationError if the logger could not be initialized
     */
    public Logger(final String name, final Config config) throws ApplicationError {
        logger = configure(name, config);
    }


    /**
     * Creates a named logging.
     * <p>This constructor work on the assumption that the named logger has already been configured.
     *
     * @param name   the name of the logger. This can be used to identify the logger at a later point
     */
    public Logger(final String name) {
        logger = java.util.logging.Logger.getLogger(name);
    }


    /**
     * Logs an error (exception).
     *
     * @param x  the exception to be logged.
     * @return   a reference to this logger
     */
    public Logger error(Throwable x) {
        logger.log(Level.SEVERE, x.getMessage(), x);
        return this;
    }



    /**
     * Logs an error message.
     *
     * @param message  the message to be logged.
     * @return         a reference to this logger
     */
    public Logger error(final String message) {
        logger.severe(message);
        return this;
    }



    /**
     * Logs a warning message.
     *
     * @param message  the message to be logged.
     * @return         a reference to this logger
     */
    public Logger warning(final String message) {
        logger.warning(message);
       return this;
    }



    /**
     * Logs an informational message.
     *
     * @param message  the message to be logged.
     * @return         a reference to this logger
     */
    public Logger info(final String message) {
        logger.info(message);
        return this;
    }



    /**
     * Logs a debug message.
     *
     * @param message  the message to be logged.
     * @return         a reference to this logger
     */
    public Logger debug(final String message) {
        logger.fine(message);
        return this;
    }



    /**
     * Logs at trace message.
     *
     * @param message  the message to be logged.
     * @return         a reference to this logger
     */
    public Logger trace(final String message) {
        logger.finest(message);
        return this;
    }



    /**
     * Determines whether or not a log message would be output at a given log level
     * given the current log configuration.
     *
     * @param level  the log level to check
     * @return       true if the message would be output, false otherwise
     */
    public boolean isLoggable(Level level) {
        return logger.isLoggable(level);
    }



    /**
     * Configures a named logger.
     * <p>Available configuration parameters are:
     * <ul>
     *   <li><code>dir</code> - the file system directory in which log files should be written
     *   <li><code>size</code> - the maximum allowed size of the the log files
     *   <li><code>count</code> - the maximum number of rolled-off log files to keep
     *   <li><code>level</code> - the log level to use
     * </ul>
     *
     * @param name   the name of the logger. This can be used to identify the logger at a later point
     * @param config the logger configuration. This may be null, in which case it will be considered a
     *               console logger, and it will inherit any system default configuration.
     * @return       the underlying Logger object
     * @throws ApplicationError if the logger could not be initialized
     */
    private java.util.logging.Logger configure(final String name, final Config config) throws ApplicationError {
        
        java.util.logging.Logger logger = java.util.logging.Logger.getLogger(name);

        /* Make sure any previous handlers are cleaned out (primarily for the "" logger in console apps) */

        for (Handler h : logger.getHandlers()) logger.removeHandler(h);


        /* If configuration is applied, use it */

        if (config != null) {
            String dir = config.getString("dir");
            if (dir != null && dir.length() > 0) {
                /* This is a file based logger, so set up max log file size and number of rollovers */

                int size = config.getInt("size", 10000000);
                int count = config.getInt("count", 5);
                logger.addHandler(new CustomLogHandler(dir + File.separator + name + ".log", size, count));
            }
            else {
                /* Fall back to console logger */

                logger.addHandler(new CustomConsoleHandler());
            }


            /* Set the log level (defaults to 'INFO') */

            logger.setLevel(Level.parse(config.getString("level", "INFO")));
            logger.setUseParentHandlers(false); /* Prevent logging to catalina log */
        }

        return logger;
    }
}