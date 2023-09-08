package com.paradoxwebsolutions.core;

import java.util.Date;
import java.util.logging.LogRecord;
import java.util.logging.SimpleFormatter;

/**
 * Log formatter used to format log messages for web services.
 * <p>This log formatter has a hard-coded formatting as follows:
 * <pre>
 * [yyyy-mm-dd hh:mm:ss.mmm [LEVEL  ] Message ...
 * </pre>
 * This makes the messages chronological when sorted alphabetically, and is
 * designed to make it easy to find and/or filter specific messages or time
 * ranges.
 *
 * @author Peter Smith
 */
public class CustomLogFormatter extends SimpleFormatter {

    /** The log message formatting string */

    private static final String format = "[%1$tF %1$tT.%1$tL] [%2$-7s] %3$s %n";


    /**
     * Formats a message for a given LogRecord instance.
     * @param record the LogRecord instance to format
     */
    @Override public synchronized String format(LogRecord record) {
        return String.format(format,
            new Date(record.getMillis()),
            record.getLevel().getLocalizedName(),
            record.getMessage()
        );
    }
}
