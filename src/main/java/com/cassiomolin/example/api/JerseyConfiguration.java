package com.cassiomolin.example.api;

import org.glassfish.jersey.server.ResourceConfig;

/**
 * Jersey configuration class.
 *
 * @author cassiomolin
 */
public class JerseyConfiguration extends ResourceConfig {

    public JerseyConfiguration() {
        packages(true, this.getClass().getPackage().getName());
    }
}