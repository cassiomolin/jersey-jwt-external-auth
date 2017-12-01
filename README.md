# REST API with Facebook and Google authentication using Jersey and CDI

[![Build Status](https://travis-ci.org/cassiomolin/jersey-jwt-external-auth.svg?branch=master)](https://travis-ci.org/cassiomolin/jersey-jwt-external-auth)
[![MIT Licensed](https://img.shields.io/badge/license-MIT-blue.svg)](https://raw.githubusercontent.com/cassiomolin/jersey-jwt-external-auth/master/LICENSE.txt)

This example application demonstrates how to perform authentication using external providers such as Google and Facebook in a REST application using:

 - **Jersey:** JAX-RS reference implementation for creating RESTful web services in Java.
 - **Jackson:** JSON parser for Java.
 - **Undertow:** Servlet container.
 - **Weld:** CDI reference implementation.
 - **JJWT:** Library for creating and parsing JSON Web Tokens (JWTs) in Java.
 - **ScribeJava:** OAuth client library.

This example also use token-based authentication to authenticate the requests between the client and the server.

## How token-based authentication works?

In token-based authentication, the client exchanges _hard credentials_ (such as username and password) for a piece of data called _token_. Instead of sending the hard credentials in every request, the client sends a token to the server to perform authentication and authorisation.

In this example, instead of exchanging hard credentials with the application, the user authenticates against an external provider (currently Facebook and Google are supported). If the authentication with the external provider succeeds and permissions are granted to the application, data coming from the external provider (such as first name, last name and email) will be used to register a user in the application.

A JWT token will be returned to the client to authenticate the subsequent requests. This is needed so that the API can establish the identity of the user.

The application allows the user to unlink an authentication provider. However it won't invalidate the JWT token. That is, unlinking the authentication provider won't log the user out.

## Building and running this application

This application depends on external authentication providers that use OAuth 2.0. You must obtain a _client ID_ and _client secret_ with those providers before building and running the application. See details for each provider:

- [Facebook](https://developers.facebook.com/docs/apps/register)
- [Google](https://developers.google.com/identity/sign-in/web/devconsole-project)

Then update the [`application.properties`](/src/main/resources/application.properties) file, setting the following values:

- `authentication.provider.facebook.clientId`
- `authentication.provider.facebook.clientSecret`
- `authentication.provider.google.clientId`
- `authentication.provider.google.clientSecret`

As part of the OAuth 2.0 authorization flow, a callback URL is required. If you run the application in your local machine (`localhost`), using the default port (`8080`), the callback URLs will be the following:

- `http://localhost:8080/api/auth/facebook/callback`
- `http://localhost:8080/api/auth/google/callback`

This URLs must be registered with Facebook and Google, respectively:

- For Facebook, add the _Facebook Login_ product to your application and, under _Settings_, set the _Valid OAuth redirect URIs_.
- For Google, in the API Console, under _Credentials_, you can set _Authorised redirect URIs_.

In the [`application.properties`](/src/main/resources/application.properties), make sure that the values of the following properties match the callback URLs registered with the authentication providers:

- `authentication.provider.facebook.callbackUrl`
- `authentication.provider.google.callbackUrl`

Then build and run the application following these steps:

1. Open a command line window or terminal.
1. Navigate to the root directory of the project, where the `pom.xml` resides.
1. Compile the project: `mvn clean compile`.
1. Package the application: `mvn package`.
1. Change into the `target` directory: `cd target`
1. You should see a file with the following or a similar name: `external-auth-1.0.jar`.
1. Execute the JAR: `java -jar external-auth-1.0.jar`.
1. The application should be available at `http://localhost:8080/api`.

### Quick words on Undertow and uber-jars

This application is packed as an [uber-jar](https://stackoverflow.com/q/11947037/1426227), making it easy to run, so you don't need to be bothered by installing a servlet container such as Tomcat and then deploy the application on it. Just execute `java -jar <jar-file>` and the application will be up and running.

This application uses [Undertow](http://undertow.io/), a lighweight Servlet container designed to be fully embeddable. It's used as the default web server in the [WildFly Application Server](http://wildfly.org/).

The uber-jar is created with the [Apache Maven Shade Plugin](https://maven.apache.org/plugins/maven-shade-plugin/), that provides the capability to create an executable jar including its dependencies.

## Application overview

Find below a quick description of the most relevant classes of this application:

- [`FacebookAuthenticationService`](/src/main/java/com/cassiomolin/example/service/external/FacebookAuthenticationService.java): Authentication service for Facebook. Encapsulates the OAuth 2.0 flow.

- [`GoogleAuthenticationService`](/src/main/java/com/cassiomolin/example/service/external/GoogleAuthenticationService.java): Similar to the service defined above.

- [`FacebookAuthenticationResource`](/src/main/java/com/cassiomolin/example/api/resources/FacebookAuthenticationResource.java): REST resource for authenticating a user using Facebook authentication provider. If the authentication with Facebook succeeds and permissions are granted to the application, a JWT token will be returned to the client to authenticate the next requests.

- [`GoogleAuthenticationResource`](/src/main/java/com/cassiomolin/example/api/resources/FacebookAuthenticationResource.java): Similar to the resource above.

- [`AuthenticationFilter`](/src/main/java/com/cassiomolin/example/api/providers/AuthenticationFilter.java): [`ContainerRequestFilter`](https://docs.oracle.com/javaee/7/api/javax/ws/rs/container/ContainerRequestFilter.html) implementation for validating the authentication token sent in the `Authorization` header of the HTTP request.

## REST API overview

See the [curl][] scripts below with the REST API supported operations:

### Authenticate and link Facebook account

Get the OAuth 2.0 authorization URL for Facebook:

```bash
curl -X POST -Ls -o /dev/null -w %{url_effective} \
  'http://localhost:8080/api/auth/facebook'
```

Copy the URL and paste in your browser, input Facebook credentials if required, grant access to the application. Facebook should perform a request to the callback URL and a JWT token issued by the application should be returned in the response. Use this token to authenticate next requests to the API.

If there's no user registered with the email returned from Facebook, a new user will be registered in the application using data coming from the Facebook (first name, last name and email).

### Authenticate and link Google account

Get the OAuth 2.0 authorization URL for Google:

```bash
curl -X POST -Ls -o /dev/null -w %{url_effective} \
  'http://localhost:8080/api/auth/google'
```

Copy the URL and paste it in your browser, input Google credentials if required, grant access to the application. Google should perform a request to the callback URL and a JWT token issued by the application should be returned in the response. Use this token to authenticate next requests to the API.

If there's no user registered with the email returned from Google, a new user will be registered in the application using data coming from the Facebook (first name, last name and email).

### Get current user

Get details from the current user. A valid JWT token is required to perform this operation.

```bash
curl -X GET \
  'http://localhost:8080/api/users/me' \
  -H 'Accept: application/json' \
  -H 'Authorization: Bearer <authentication-token>'
```

For demonstration purpose, this operation returns the access tokens from the authentication providers. You probably don't want to do it in a real world application.

### Unlink Facebook account

Unlink the Facebook account (simply delete the access token) from the current user. A valid JWT token is required to perform this operation.

```bash
curl -X DELETE \
  'http://localhost:8080/api/auth/facebook' \
  -H 'Accept: application/json' \
  -H 'Authorization: Bearer <authentication-token>'
```

This operation won't log the user out from that application (that is, it won't invalidate the JWT token).

### Unlink Google account

Unlink the Google account (simply delete the access token) from the current user. A valid JWT token is required to perform this operation.

```bash
curl -X DELETE \
  'http://localhost:8080/api/auth/google' \
  -H 'Accept: application/json' \
  -H 'Authorization: Bearer <authentication-token>'
```

This operation won't log the user out from that application (that is, it won't invalidate the JWT token).


[Postman]: https://www.getpostman.com/
[curl]: https://curl.haxx.se/