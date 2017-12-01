package com.cassiomolin.example.api.providers;

import com.fasterxml.jackson.databind.ObjectMapper;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.spi.CDI;
import javax.inject.Inject;
import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.Provider;

/**
 * Provider for {@link ObjectMapper}.
 *
 * @author cassiomolin
 */
@Provider
public class ObjectMapperContextResolver implements ContextResolver<ObjectMapper> {

    private final ObjectMapper mapper;

    public ObjectMapperContextResolver() {
        mapper = CDI.current().select(ObjectMapper.class).get();
    }

    @Override
    public ObjectMapper getContext(Class<?> type) {
        return mapper;
    }
}