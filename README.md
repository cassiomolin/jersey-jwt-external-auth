# REST API with Google and Facebook authentication using Jersey and CDI

This example application demonstrates how to authenticate using external providers (Google and Facebook) in a REST application using:

 - **Jersey:** JAX-RS reference implementation for creating RESTful web services in Java.
 - **Jackson:** JSON parser for Java.
 - **Undertow:** Servlet container.
 - **Weld:** CDI reference implementation.
 - **JJWT:** Library for creating and parsing JSON Web Tokens (JWTs) in Java.
 - **ScribeJava:** OAuth client library.

This example also use to token-based authentication to authenticate the requests bewteen the client and the server.

## How token-based authentication works?

In token-based authentication, the client exchanges _hard credentials_ (such as username and password) for a piece of data called _token_. Instead of sending the hard credentials in every request, the client sends a token to the server to perform authentication and authorisation.

In this example, instead of exchanging hard credentials with the application, the user user authenticates using an external provider (currently Facebook and Google are supported). If the authentication with the external provider succeeds and permissions are granted to the application, data coming from the external provider (such as first name, last name and email) will be used to register a user in the application. A JWT token will be returned to the client to authenticate the next requests.

The application allows the user to unlink an external provider. However it won't invalidate the JWT token.

## Building and running this application

Once this application depends on external authentication providers, you must obtain an OAuth 2.0 client ID and secret with those providers before building an running the application.

For Facebook, refer to this [page](https://developers.facebook.com/docs/apps/register). For Google, refer to this [page](https://developers.google.com/identity/sign-in/web/devconsole-project).

Then update the `application.properties` file, setting the following values:

- `authentication.provider.facebook.clientId`
- `authentication.provider.facebook.clientSecret`
- `authentication.provider.google.clientId`
- `authentication.provider.google.clientSecret`

As part of the OAuth 2.0 authorization flow, a callback URL is required. If you run the application in your local machine (`localhost`), using the default port (`8080`), the callback URLs will be the following:

- `http://localhost:8080/api/auth/facebook/callback`
- `http://localhost:8080/api/auth/google/callback`

This URLs must be registered with Facebook and Google, respectively.

For Facebook, add the _Facebook Login_ product to your application and, under _Settings_, set the _Valid OAuth redirect URIs_.
For Google, in the API Console, under _Credentials_, you can set _Authorised redirect URIs_.

In the `application.properties`, make sure the value of the following properties match the callback URLs registered with your providers:

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


[RFC 7519]: https://tools.ietf.org/html/rfc7519
[jwt.io]: http://jwt.io/
[jti claim]: https://tools.ietf.org/html/rfc7519#section-4.1.7
[Postman]: https://www.getpostman.com/
[answer]: https://stackoverflow.com/a/26778123/1426227
[example-with-spring-security]: https://github.com/cassiomolin/jersey-jwt-springsecurity
[curl]: https://curl.haxx.se/