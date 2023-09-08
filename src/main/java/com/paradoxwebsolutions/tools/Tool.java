package com.paradoxwebsolutions.tools;

import com.paradoxwebsolutions.core.Config;
import com.paradoxwebsolutions.core.CustomConsoleHandler;
import com.paradoxwebsolutions.core.Logger;
import com.paradoxwebsolutions.core.ClassInitializer;
import com.paradoxwebsolutions.core.ServiceAPI;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Arrays;



/**
 * Utility base class for chatbot tools
 */
abstract class Tool implements ServiceAPI {

    /* Application configruration  */

    protected Config      config = new Config();


    /* The application root directory */


    protected String      rootDir;


    /* Application logger */

    protected Logger      LOGGER;



    /**
     * Class constructor.
     *
     * @throws Exception on any error
     */
    public Tool() throws Exception {

        /* Set up basic configuration pull from environment */

        for (String property : new String[] {"dir.root", "dir.model"}) {
            String value = System.getProperty(property);
            if (!value.endsWith(File.separator)) value += File.separator;
            config.setString(property, value);
        }

        rootDir = config.getString("dir.root");
        config.load(new File(rootDir + "scripts" + File.separator + "cfg" + File.separator + getServiceName() + ".properties"));


        /* Configure the top level logger */

        LOGGER = new Logger("", config.getConfig("logs.service"));
        

        /* Load any modules */

        ClassInitializer loader = new ClassInitializer();
        String[] modules = Arrays.stream(config.getString("service.modules", "").split("[\\s,]+")).filter(w -> w.length() > 0).toArray(String[]::new);

        for (String module : modules) {
            LOGGER.debug(String.format("Loading module: %s", module));
            loader.loadModule(module, this, config, LOGGER);
        }

    }



    /**
     * Service API
     */
    @Override
    public InputStream getResource(String name) {
        try {
            return new FileInputStream(rootDir + name);
        }
        catch (Exception x) {
            return null;
        }
    }
}