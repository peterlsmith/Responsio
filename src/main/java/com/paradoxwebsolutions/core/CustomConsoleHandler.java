package com.paradoxwebsolutions.core;

import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.LogRecord;

/**
 * Provides simplified log handler to send log messages to standard output.
 * <p> This log handler is primarily intended for command line tools. It simply
 * outputs log messages to the console with no additional formatting.
 *
 * @author Peter Smith
 */
public class CustomConsoleHandler extends Handler {



    /**
     * Constructs a new handle instance.
     */
    public CustomConsoleHandler() {
        super();
        setFormatter(new Formatter(){public String format(LogRecord record) {return record.getMessage();}});
    }



    /**
     * Outputs a log message to standard output.
     * @param record the LogRecord instance to output
     */
    @Override
    public void publish(LogRecord record) {
        if (record != null) {
            System.out.println(this.getFormatter().format(record));
        }
    }



    /**
     * Closes the log - a null-op for this class.
     */
    @Override
    public void close() {
    }



    /**
     * Flushes all output.
     */
    @Override
    public void flush() {
        System.out.flush();
    }

}
