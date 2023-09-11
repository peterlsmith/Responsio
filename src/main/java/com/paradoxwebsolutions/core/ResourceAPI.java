package com.paradoxwebsolutions.core;

import com.paradoxwebsolutions.core.ApplicationError;

import java.io.InputStream;




/**
 * Interface for classes that provide access to application resources.
 *
 * @author Peter Smith
 */
public interface ResourceAPI {

    /**
     * Provides access to an application resource by name.
     *
     * @param name   the name of the resource to get
     * @return       an InputStream instance for the resource
     * @throws       ApplicationError on error
     */
    public InputStream getInputStream(final String name) throws ApplicationError;
}
