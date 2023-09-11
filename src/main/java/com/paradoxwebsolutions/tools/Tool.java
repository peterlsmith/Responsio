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

        /* Pull basic configuration from environment */

        rootDir = System.getProperty("dir.root");
        if (rootDir == null) {
            throw new Exception("Root directory location 'dir.root' must be specified in the environment");
        }

        if (!rootDir.endsWith(File.separator)) rootDir += File.separator;
        config.setString("dir.root", rootDir);


        /* Load config files ir order of priority (lest -> most specific) */

        File cfgFile = new File(rootDir + "data" + File.separator + "responsio.properties");
        if (cfgFile.exists()) config.load(cfgFile);

        cfgFile = new File(rootDir + "scripts" + File.separator + "tools.properties");
        if (cfgFile.exists()) config.load(cfgFile);

        cfgFile = new File(rootDir + "scripts" + File.separator + serviceName + ".properties");
        if (cfgFile.exists()) config.load(cfgFile);


        /* Check for a manual override of the identity directory */

        String identityDir = System.getProperty("dir.identity");
        if (identityDir != null) {
            if (!identityDir.endsWith(File.separator)) identityDir += File.separator;
            config.setString("dir.identity", identityDir);
        }


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