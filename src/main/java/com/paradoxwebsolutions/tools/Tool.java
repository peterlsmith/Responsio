package com.paradoxwebsolutions.tools;

import com.paradoxwebsolutions.core.ApplicationError;
import com.paradoxwebsolutions.core.Config;
import com.paradoxwebsolutions.core.CustomConsoleHandler;
import com.paradoxwebsolutions.core.Logger;
import com.paradoxwebsolutions.core.ClassInitializer;
import com.paradoxwebsolutions.core.ResourceAPI;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Arrays;



/**
 * Utility base class for chatbot tools
 */
abstract class Tool implements ResourceAPI {

    /* Application configruration  */

    protected Config      config = new Config();


    /* The application root directory */


    protected String      rootDir;


    /* Application logger */

    protected Logger      LOGGER;



    /**
     * Class constructor.
     *
     * @param serviceName  the name of the service being instantiated
     * @throws Exception on any error
     */
    public Tool(final String serviceName) throws Exception {

        /* Set up basic configuration pull from environment */

        for (String property : new String[] {"dir.root", "dir.identity"}) {
            String value = System.getProperty(property);
            if (!value.endsWith(File.separator)) value += File.separator;
            config.setString(property, value);
        }

        rootDir = config.getString("dir.root");
        config.load(new File(rootDir + "scripts" + File.separator + "cfg" + File.separator + serviceName + ".properties"));


        /* Configure the top level logger */

        LOGGER = new Logger("", config.getConfig("logs.service"));
        

        /* Load any modules */

        ClassInitializer initializer = new ClassInitializer();
        String[] modules = Arrays.stream(config.getString("service.modules", "").split("[\\s,]+")).filter(w -> w.length() > 0).toArray(String[]::new);

        for (String module : modules) {
            LOGGER.debug(String.format("Loading module: %s", module));
            Class cls = this.getClass().getClassLoader().loadClass(module);
            initializer.initialize(cls, this, config, LOGGER);
        }
    }



    /**
     * Service API
     */
    @Override
    public InputStream getInputStream(String name) throws ApplicationError {
        try {
            return new FileInputStream(rootDir + name);
        }
        catch (Exception x) {
            throw new ApplicationError(String.format("Failed to open resource '%s'", name), x);
        }
    }
}