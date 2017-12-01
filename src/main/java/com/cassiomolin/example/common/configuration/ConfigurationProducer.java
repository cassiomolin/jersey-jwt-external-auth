package com.cassiomolin.example.common.configuration;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.InjectionPoint;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Read the {@code application.properties} file from the classpath and produce values that can be injected with {@code @}{@link Configurable}.
 * <p>
 * It's a simple and lightweight alternative to the Apache DeltaSpike Configuration Mechanism.
 * <p>
 * Currently only injection of the following types is supported: {@link String}, {@link Boolean}, {@link Integer} and {@link Long}.
 *
 * @author cassiomolin
 */
@ApplicationScoped
public class ConfigurationProducer {

    private Properties properties;

    @PostConstruct
    public void init() {

        properties = new Properties();
        InputStream stream = ConfigurationProducer.class.getResourceAsStream("/application.properties");

        if (stream == null) {
            throw new RuntimeException("Cannot find application.properties configuration file.");
        }

        try {
            this.properties.load(stream);
        } catch (final IOException e) {
            throw new RuntimeException("Configuration file cannot be loaded.");
        }
    }

    @Produces
    @Configurable
    public String produceString(InjectionPoint ip) {
        return properties.getProperty(getKey(ip));
    }

    @Produces
    @Configurable
    public Integer produceInteger(InjectionPoint ip) {
        return Integer.valueOf(properties.getProperty(getKey(ip)));
    }

    @Produces
    @Configurable
    public Long produceLong(InjectionPoint ip) {
        return Long.valueOf(properties.getProperty(getKey(ip)));
    }

    @Produces
    @Configurable
    public Boolean produceBoolean(InjectionPoint ip) {
        return Boolean.valueOf(this.properties.getProperty(getKey(ip)));
    }

    private String getKey(InjectionPoint ip) {
        return ip.getAnnotated().getAnnotation(Configurable.class).value();
    }
}