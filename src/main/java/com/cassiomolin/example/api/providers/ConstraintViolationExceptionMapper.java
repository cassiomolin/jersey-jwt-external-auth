package com.cassiomolin.example.api.providers;

import com.cassiomolin.example.api.model.ApiError;
import com.cassiomolin.example.api.model.ApiValidationError;
import com.cassiomolin.example.api.model.ApiValidationError.ValidationError;
import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.introspect.BeanPropertyDefinition;

import javax.enterprise.context.ApplicationScoped;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.ElementKind;
import javax.validation.Path;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import javax.ws.rs.ext.Providers;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * Component that maps a {@link ConstraintViolationException} to a HTTP response.
 *
 * @author cassiomolin
 */
@Provider
@ApplicationScoped
public class ConstraintViolationExceptionMapper implements ExceptionMapper<ConstraintViolationException> {

    @Context
    private UriInfo uriInfo;

    @Context
    private Providers providers;

    private static final String REQUEST_ENTITY = "requestEntity";
    private static final String REQUEST_ENTITY_PROPERTY = "requestEntityProperty";
    private static final String REQUEST_QUERY_PARAMETER = "requestQueryParameter";
    private static final String REQUEST_PATH_PARAMETER = "requestPathParameter";
    private static final String REQUEST_HEADER_PARAMETER = "requestHeaderParameter";
    private static final String REQUEST_COOKIE_PARAMETER = "requestCookieParameter";
    private static final String REQUEST_FORM_PARAMETER = "requestFormParameter";
    private static final String REQUEST_MATRIX_PARAMETER = "requestMatrixParameter";

    @Override
    public Response toResponse(ConstraintViolationException exception) {
        ApiValidationError validationError = mapToApiError(exception);
        int statusCode = determineStatusCode(validationError);
        return Response.status(statusCode).entity(validationError).type(MediaType.APPLICATION_JSON).build();
    }

    /**
     * Determine the status code according to the type of the validation error.
     *
     * @param validationError
     * @return
     */
    private int determineStatusCode(ApiValidationError validationError) {
        boolean containsEntityErrors = validationError.getValidationErrors()
                .stream().map(ValidationError::getType)
                .anyMatch(Arrays.asList(REQUEST_ENTITY, REQUEST_ENTITY_PROPERTY)::contains);
        return containsEntityErrors ? 422 : 400;
    }

    /**
     * Map a {@link ConstraintViolationException} to an {@link ApiError}.
     *
     * @param exception
     * @return
     */
    private ApiValidationError mapToApiError(ConstraintViolationException exception) {

        ApiValidationError error = new ApiValidationError();
        error.setError("Validation error");
        error.setMessage("Request cannot be processed due to validation error(s)");
        error.setPath(uriInfo.getAbsolutePath().getPath());

        List<ValidationError> validationErrors = exception.getConstraintViolations().stream().map(constraintViolation -> {

            Path.Node leafNode = getLeafNode(constraintViolation.getPropertyPath()).get();

            switch (leafNode.getKind()) {

                case PROPERTY:
                    return handleInvalidProperty(constraintViolation);

                case PARAMETER:
                    return handleInvalidParameter(constraintViolation);

                case BEAN:
                    return handleInvalidBean(constraintViolation);

                default:
                    return handleUnknownSource(constraintViolation);
            }

        }).collect(Collectors.toList());
        error.setValidationErrors(validationErrors);

        return error;
    }

    /**
     * Handle an invalid property. Can be:
     * <p>
     * 1. Invalid request parameter (annotated bean param field or annotated resource class field)
     * 2. Invalid request entity property (annotated bean param field)
     *
     * @param constraintViolation
     */
    private ValidationError handleInvalidProperty(ConstraintViolation constraintViolation) {

        Path.Node leafNode = getLeafNode(constraintViolation.getPropertyPath()).get();
        Class<?> beanClass = constraintViolation.getLeafBean().getClass();

        // Can be an invalid request parameter (annotated bean param field or annotated resource class field)
        Optional<Field> optionalField = getField(leafNode.getName(), beanClass);
        if (optionalField.isPresent()) {
            Optional<ParameterDetails> optionalParameterDetails = getParameterDetails(optionalField.get().getAnnotations());
            if (optionalParameterDetails.isPresent()) {
                return createErrorForParameter(optionalParameterDetails.get(), constraintViolation);
            }
        }

        // Get Jackson ObjectMapper
        ContextResolver<ObjectMapper> resolver = providers.getContextResolver(ObjectMapper.class, MediaType.WILDCARD_TYPE);
        ObjectMapper mapper = resolver.getContext(ObjectMapper.class);

        // Can be an invalid request entity property (annotated bean param field)
        Optional<String> optionalJsonProperty = getJsonPropertyName(mapper, beanClass, leafNode.getName());
        if (optionalJsonProperty.isPresent()) {
            ValidationError error = new ValidationError();
            error.setType(REQUEST_ENTITY_PROPERTY);
            error.setName(optionalJsonProperty.get());
            error.setMessage(constraintViolation.getMessage());
            return error;
        }

        return handleUnknownSource(constraintViolation);
    }

    /**
     * Handle an invalid parameter. Can be:
     * <p>
     * 1. Invalid request parameter (annotated method parameter)
     * 2. Invalid request entity (annotated method parameter)
     *
     * @param constraintViolation
     */
    private ValidationError handleInvalidParameter(ConstraintViolation constraintViolation) {

        List<Path.Node> nodes = new ArrayList<>();
        constraintViolation.getPropertyPath().iterator().forEachRemaining(nodes::add);

        Path.Node parent = nodes.get(nodes.size() - 2);
        Path.Node child = nodes.get(nodes.size() - 1);

        if (ElementKind.METHOD == parent.getKind()) {

            Path.MethodNode methodNode = parent.as(Path.MethodNode.class);
            Path.ParameterNode parameterNode = child.as(Path.ParameterNode.class);

            try {

                // Can be an invalid request parameter (annotated method parameter)
                Class<?> beanClass = constraintViolation.getLeafBean().getClass();
                Method method = beanClass.getMethod(methodNode.getName(), methodNode.getParameterTypes().stream().toArray(Class[]::new));
                Annotation[] annotations = method.getParameterAnnotations()[parameterNode.getParameterIndex()];
                Optional<ParameterDetails> optionalParameterDetails = getParameterDetails(annotations);
                if (optionalParameterDetails.isPresent()) {
                    return createErrorForParameter(optionalParameterDetails.get(), constraintViolation);
                }

            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            }

            // Assumes that the request entity is invalid (annotated method parameter)
            ValidationError error = new ValidationError();
            error.setType(REQUEST_ENTITY);
            error.setMessage(constraintViolation.getMessage());
            return error;
        }

        return handleUnknownSource(constraintViolation);
    }

    /**
     * Handle an invalid bean. Can be:
     * <p>
     * 1. Invalid request bean (annotated bean class)
     *
     * @param constraintViolation
     */
    private ValidationError handleInvalidBean(ConstraintViolation constraintViolation) {

        ValidationError error = new ValidationError();
        error.setType("entity");
        error.setMessage(constraintViolation.getMessage());

        return error;
    }

    /**
     * Handle other error situations. Works as as fallback.
     *
     * @param constraintViolation
     * @return
     */
    private ValidationError handleUnknownSource(ConstraintViolation constraintViolation) {
        ValidationError error = new ValidationError();
        error.setName(constraintViolation.getPropertyPath().toString());
        error.setMessage(constraintViolation.getMessage());
        return error;
    }

    /**
     * Create error for parameter.
     *
     * @param parameterDetails
     * @param constraintViolation
     * @return
     */
    private ValidationError createErrorForParameter(ParameterDetails parameterDetails, ConstraintViolation constraintViolation) {
        ValidationError error = new ValidationError();
        error.setType(parameterDetails.getType());
        error.setName(parameterDetails.getName());
        error.setMessage(constraintViolation.getMessage());
        return error;
    }

    /**
     * Get the leaf node.
     *
     * @param path
     * @return
     */
    private Optional<Path.Node> getLeafNode(Path path) {
        return StreamSupport.stream(path.spliterator(), false).reduce((a, b) -> b);
    }

    /**
     * Get field from class.
     *
     * @param fieldName
     * @param beanClass
     * @return
     */
    private Optional<Field> getField(String fieldName, Class<?> beanClass) {
        return Arrays.stream(beanClass.getDeclaredFields())
                .filter(field -> field.getName().equals(fieldName))
                .findFirst();
    }

    /**
     * Get parameter details from JAX-RS annotation.
     *
     * @param annotations
     * @return
     */
    private Optional<ParameterDetails> getParameterDetails(Annotation[] annotations) {

        for (Annotation annotation : annotations) {
            Optional<ParameterDetails> optionalParameterDetails = getParameterDetails(annotation);
            if (optionalParameterDetails.isPresent()) {
                return optionalParameterDetails;
            }
        }

        return Optional.empty();
    }

    /**
     * Get the parameter details from JAX-RS annotation.
     *
     * @param annotation
     * @return
     */
    private Optional<ParameterDetails> getParameterDetails(Annotation annotation) {

        ParameterDetails parameterDetails = new ParameterDetails();

        if (annotation instanceof QueryParam) {
            parameterDetails.setType(REQUEST_QUERY_PARAMETER);
            parameterDetails.setName(((QueryParam) annotation).value());
            return Optional.of(parameterDetails);

        } else if (annotation instanceof PathParam) {
            parameterDetails.setType(REQUEST_PATH_PARAMETER);
            parameterDetails.setName(((PathParam) annotation).value());
            return Optional.of(parameterDetails);

        } else if (annotation instanceof HeaderParam) {
            parameterDetails.setType(REQUEST_HEADER_PARAMETER);
            parameterDetails.setName(((HeaderParam) annotation).value());
            return Optional.of(parameterDetails);

        } else if (annotation instanceof CookieParam) {
            parameterDetails.setType(REQUEST_COOKIE_PARAMETER);
            parameterDetails.setName(((CookieParam) annotation).value());
            return Optional.of(parameterDetails);

        } else if (annotation instanceof FormParam) {
            parameterDetails.setType(REQUEST_FORM_PARAMETER);
            parameterDetails.setName(((FormParam) annotation).value());
            return Optional.of(parameterDetails);

        } else if (annotation instanceof MatrixParam) {
            parameterDetails.setType(REQUEST_MATRIX_PARAMETER);
            parameterDetails.setName(((MatrixParam) annotation).value());
            return Optional.of(parameterDetails);
        }

        return Optional.empty();
    }

    /**
     * Get the JSON property name.
     *
     * @param mapper
     * @param beanClass
     * @param nodeName
     * @return
     */
    private Optional<String> getJsonPropertyName(ObjectMapper mapper, Class<?> beanClass, String nodeName) {

        JavaType javaType = mapper.getTypeFactory().constructType(beanClass);
        BeanDescription introspection = mapper.getSerializationConfig().introspect(javaType);
        List<BeanPropertyDefinition> properties = introspection.findProperties();

        return properties.stream()
                .filter(propertyDefinition -> nodeName.equals(propertyDefinition.getField().getName()))
                .map(BeanPropertyDefinition::getName)
                .findFirst();
    }

    /**
     * Class to hold JAX-RS parameter details.
     */
    private static class ParameterDetails {

        private String type;

        private String name;

        public ParameterDetails() {

        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }
}