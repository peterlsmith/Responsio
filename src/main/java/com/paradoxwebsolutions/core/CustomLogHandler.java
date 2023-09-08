package com.paradoxwebsolutions.core;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.time.format.DateTimeFormatter;
import java.time.Instant;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.LogRecord;

/**
 * Logging handler for managing log files.
 * <p>This class manages the output of logging messages to log files. It can be configured
 * to roll-over logs files when the reach a specific size, as well as to remove old log
 * files when a given number have accumulated.
 * When a log file is rolled-over, it is renamed with a timestamp suffix that allows chronological
 * ordering using an alphabetic sort.
 *
 * @author Peter Smith
 */
public class CustomLogHandler extends Handler {

    /** The name of the active log file */

    private String  logName;


    /** The directory of the active log file */

    private String  logDir;


    /** The maximum size of the log file in bytes */

    private int     logSize;


    /** The maximum number of logs to keep */

    private int     logCount;


    /** The currently active log file */

    Writer    log;


    /**
     * Creates a log handler instance with the given configuration.
     * 
     * @param logFilename  the name to use for the log file
     * @param size         the maximum allowed size of a log file, in bytes
     * @param count        the maximum number of old log files to keep
     * @throws ApplicationError if the log file could not be created
     */
    public CustomLogHandler(String logFilename, int size, int count) throws ApplicationError {
        super();

        assert logFilename != null : "Null log name";
        assert logFilename.length() > 0 : "Empty log name";

        File file = new File(logFilename);
        this.logName = file.getName();
        this.logDir = file.getParent();
        this.logSize = size;
        this.logCount = count;


        /* Verify the logging directory */

        File dir = file.getParentFile();
        if (!dir.isDirectory()) {
            /* If the logging directory does not exist, attempt to create it */

            if (!dir.mkdir()) throw new ApplicationError(String.format("Logging directory '%s' does not exists and could not be created", dir.getPath()));
        }


        /* Check to see if the log file already exists, and if so, roll it over */

        if (file.exists()) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss").withZone(ZoneId.systemDefault());
            String suffix = formatter.format(Instant.now());

            if (!file.renameTo(new File(logFilename + "." + suffix))) {
                throw new ApplicationError("Can not rename log file for roll-over");
            }
        }


        /* Roll off any old log files */

        this.rollOff();


        /* Set up default formatter */

        this.setFormatter(new CustomLogFormatter());


        /* Open the new log */

        try {
            this.log = new OutputStreamWriter(new FileOutputStream(logFilename));
        }
        catch (Exception x) {
            throw new ApplicationError("Failed to open log file for output");
        }
    }



    /**
     * Roll off logs.
     */
    private void rollOff() {
        String filterName = this.logName + ".";

        File dir = new File(this.logDir);
        String[] logFiles = dir.list(new FilenameFilter() {
            public boolean accept(File dir, String name) {return name.startsWith(filterName);}
        });

        if (logFiles.length >= this.logCount) {
            Arrays.sort(logFiles);

            /* we need to remove some old files */
            
            List<String> files = new ArrayList<String>(Arrays.asList(logFiles));
            while (files.size() >= this.logCount) {
                File file = new File(this.logDir + File.separator + files.remove(0));
                file.delete();
            }
        }
    }



    /**
     * Outputs a log message to the log file.
     * @param record the LogRecord instance to output
     */
    @Override
    public void publish(LogRecord record) {
        if (record != null) {
            Formatter formatter = this.getFormatter();
            String line = formatter.format(record);
            try {
                this.log.write(line, 0, line.length());
                this.log.flush();
            }
            catch(Exception x){};
        }
    }


    
    /**
     * Flushes any cache messages to the log file.
     */
    @Override
    public void flush() {
        try {
            this.log.flush();
        }
        catch(Exception x){};
    }



    /**
     * Closes the current log file.
     * <p>This method should only ever be invoked as part of an application/service shutdown.
     */
    @Override
    public void close() {
        try {
            this.log.close();
        }
        catch(Exception x){};
    }

}
