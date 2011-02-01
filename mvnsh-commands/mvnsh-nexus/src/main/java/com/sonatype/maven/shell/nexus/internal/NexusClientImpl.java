/*******************************************************************************
 * Copyright (c) 2009-2011 Sonatype, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Apache License v2.0 which accompanies this distribution.
 * The Eclipse Public License is available at 
 *   http://www.eclipse.org/legal/epl-v10.html
 * The Apache License v2.0 is available at
 *   http://www.apache.org/licenses/LICENSE-2.0.html
 * You may elect to redistribute this code under either of these licenses. 
 *******************************************************************************/

package com.sonatype.maven.shell.nexus.internal;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.sonatype.maven.shell.nexus.NexusClient;
import com.sonatype.maven.shell.nexus.internal.marshal.NexusMarshallerFactory;
import com.sonatype.maven.shell.nexus.internal.wink.BasicAuthSecurityHandler;
import com.sonatype.maven.shell.nexus.internal.wink.LoggingHandler;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.wink.client.ApacheHttpClientConfig;
import org.apache.wink.client.ClientConfig;
import org.apache.wink.client.ClientResponse;
import org.apache.wink.client.Resource;
import org.apache.wink.client.RestClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonatype.nexus.rest.model.ErrorMessage;
import org.sonatype.nexus.rest.model.ErrorResponse;
import org.sonatype.security.rest.model.AuthenticationLoginResource;
import org.sonatype.security.rest.model.AuthenticationLoginResourceResponse;

import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.NewCookie;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Nexus client implementation.
 *
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @since 0.9
 */
@Singleton
public class NexusClientImpl
    implements NexusClient
{
    private static final Logger log = LoggerFactory.getLogger(NexusClientImpl.class);

    private static final String DEFAULT_INSTANCE = "local";

    private static final String AUTH_COOKIE_NAME = "JSESSIONID";

    private final NexusMarshallerFactory marshallerFactory = new NexusMarshallerFactory();

    private final Map<String,Extension> extensions;

    private URI baseUri;

    private String instance;

    private RestClient client;

    private Cookie authCookie;

    @Inject
    public NexusClientImpl(final Map<String,Extension> extensions) {
        assert extensions != null;
        this.extensions = extensions;

        if (log.isDebugEnabled()) {
            log.debug("Extensions:");
            for (Map.Entry<String,Extension> entry : extensions.entrySet()) {
                log.debug("  {} -> {}", entry.getKey(), entry.getValue());
            }
        }
    }

    @Override
    protected void finalize() throws Throwable {
        try {
            close();
        }
        finally {
            super.finalize();
        }
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "{" +
            "baseUri=" + baseUri +
            ", instance='" + instance + '\'' +
            ", open=" + isOpen() +
            ", auth=" + isAuthenticated() +
            '}';
    }

    public URI getBaseUri() {
        return baseUri;
    }

    public String getInstance() {
        return instance;
    }

    private void open(final URI baseUri, final String instance, final String username, final String password) {
        if (isOpen()) {
            close();
        }

        assert baseUri != null;
        this.baseUri = baseUri;
        this.instance = instance != null ? instance : DEFAULT_INSTANCE;

        log.debug("Opening w/username: {}", username);

        // Using Apache HttpClient here, as it appears the default wink client does not handle redirects properly, even when configured to do so
        HttpClient httpClient = new DefaultHttpClient();
        ClientConfig config = new ApacheHttpClientConfig(httpClient);
        config.followRedirects(true);

        config.handlers(
            new LoggingHandler(log, /*debug*/ false),
            new BasicAuthSecurityHandler(username, password)
        );

        this.client = new RestClient(config);
    }

    public void open(final URI baseUri, final String instance) {
        open(baseUri, instance, null, null);
    }

    public boolean isOpen() {
        return client != null;
    }

    public void ensureOpened() {
        if (!isOpen()) {
            throw new IllegalStateException("Not opened");
        }
    }

    public void close() {
        if (isOpen()) {
            log.debug("Closing");
            client = null;
        }
    }

    private RestClient getClient() {
        ensureOpened();
        return client;
    }

    //
    // Marshaling
    //

    @SuppressWarnings({"unchecked"})
    public String marshal(final Class type, final Object content) {
        return marshallerFactory.create(type).marshal(content);
    }

    public <T> T unmarshal(final Class<T> type, final String content) {
        return marshallerFactory.create(type).unmarshal(content);
    }

    //
    // Resource Access
    //

    private Resource resource(final URI uri, final MultivaluedMap<String,String> params, final MediaType type, final MediaType accept) {
        assert uri != null;
        // params could be null
        // type could be null
        // accept could be null

        Resource resource = getClient().resource(uri);

        if (params != null) {
            resource = resource.queryParams(params);
        }

        // Default content and accept types to XML if not configured
        resource = (type != null) ? resource.contentType(type) : resource.contentType(MediaType.APPLICATION_XML_TYPE);
        resource = (accept != null) ? resource.accept(type) : resource.accept(MediaType.APPLICATION_XML_TYPE);

        if (authCookie != null) {
            resource = resource.cookie(authCookie);
        }

        return resource;
    }

    private Resource resource(final URI uri, final MediaType type, final MediaType accept) {
        return resource(uri, null, type, accept);
    }

    private Resource resource(final URI uri) {
        return resource(uri, null, null, null);
    }

    private Resource resource(final URI uri, final MultivaluedMap<String,String> params) {
        return resource(uri, params, null, null);
    }

    private Response.StatusType statusForCode(final int code) {
        Response.StatusType status = Response.Status.fromStatusCode(code);
        if (status == null) {
            status = new Response.StatusType()
            {
                public int getStatusCode() {
                    return code;
                }

                public Response.Status.Family getFamily() {
                    switch(code/100) {
                        case 1: return Response.Status.Family.INFORMATIONAL;
                        case 2: return Response.Status.Family.SUCCESSFUL;
                        case 3: return Response.Status.Family.REDIRECTION;
                        case 4: return Response.Status.Family.CLIENT_ERROR;
                        case 5: return Response.Status.Family.SERVER_ERROR;
                    }
                    return Response.Status.Family.OTHER;
                }

                public String getReasonPhrase() {
                    return "Unknown Status";
                }
            };
        }
        return status;
    }
    
    private String getContent(final ClientResponse response) {
        assert response != null;

        final int code = response.getStatusCode();
        log.debug("Response status code: {}", code);

        Response.StatusType status = statusForCode(code);

        if (status.getFamily() != Response.Status.Family.SUCCESSFUL) {
            List<ErrorMessage> errors;

            String content = response.getEntity(String.class);
            if (content.contains("<nexus-error")) {
                errors = unmarshal(ErrorResponse.class, content).getErrors();
            }
            else {
                ErrorMessage msg = new ErrorMessage();
                msg.setId("unknown-error-response");
                msg.setMsg(content);
                errors = new ArrayList<ErrorMessage>(1);
                errors.add(msg);
            }

            throw new RequestFailed(status, errors);
        }

        if (status != Response.Status.NO_CONTENT) {
            return response.getEntity(String.class);
        }
        else {
            return null;
        }
    }

    //
    // Extension Request Access
    //

    public String get(final URI uri) {
        assert uri != null;

        log.debug("Requesting(GET): {}", uri);
        ClientResponse response = resource(uri).get(ClientResponse.class);
        return getContent(response);
    }

    public String get(final URI uri, final MultivaluedMap<String,String> params) {
        assert uri != null;

        log.debug("Requesting(GET): {} w/params: {}", uri, params);
        ClientResponse response = resource(uri, params).get(ClientResponse.class);
        return getContent(response);
    }

    public String head(final URI uri) {
        assert uri != null;

        log.debug("Requesting(HEAD): {}", uri);
        ClientResponse response = resource(uri).head();
        return getContent(response);
    }

    public String post(final URI uri, final String payload) {
        assert uri != null;

        log.debug("Requesting(POST): {}, payload: {}", uri, payload);
        ClientResponse response = resource(uri).post(ClientResponse.class, payload);
        return getContent(response);
    }

    public String put(final URI uri, final String payload) {
        assert uri != null;

        log.debug("Requesting(PUT): {}, payload: {}", uri, payload);
        ClientResponse response = resource(uri).put(ClientResponse.class, payload);
        return getContent(response);
    }

    public String delete(final URI uri) {
        assert uri != null;

        log.debug("Requesting(DELETE): {}", uri);
        ClientResponse response = resource(uri).delete(ClientResponse.class);
        return getContent(response);
    }

    public String request(final URI uri, final String method, final MediaType type, final MediaType accept, final String payload) {
        assert uri != null;
        assert method != null;
        assert type != null;
        assert accept != null;
        assert payload != null;

        log.debug("Requesting({}): {}", method, uri);
        ClientResponse response = resource(uri, type, accept).invoke(method, ClientResponse.class, payload);
        return getContent(response);
    }

    public UriBuilder baseUri() {
        return UriBuilder.fromUri(getBaseUri()).path("service").path(getInstance());
    }

    //
    // API Muck
    //

    public AuthenticationLoginResource login(final String username, final String password) {
        assert username != null;
        assert password != null;

        open(getBaseUri(), getInstance(), username, password);

        URI uri = baseUri().path("authentication/login").build();

        ClientResponse response = resource(uri).get(ClientResponse.class);

        // Get content first, to handle any errors
        String content = getContent(response);

        // Wink has no nice way to get cookies out of the response, so do it here the ugly way
        MultivaluedMap<String,String> headers = response.getHeaders();
        for (Map.Entry<String,List<String>> entry : headers.entrySet()) {
            if (HttpHeaders.SET_COOKIE.equals(entry.getKey())) {
                for (String raw : entry.getValue()) {
                    if (raw.contains(AUTH_COOKIE_NAME)) {
                        authCookie = NewCookie.valueOf(raw).toCookie();
                    }
                }
            }
        }

        if (authCookie == null) {
            log.warn("Missing session cookie in response");
        }

        return unmarshal(AuthenticationLoginResourceResponse.class, content).getData();
    }

    public boolean isAuthenticated() {
        return authCookie != null;
    }

    public void ensureAuthenticated() {
        if (!isAuthenticated()) {
            throw new IllegalStateException("Not authenticated");
        }

        ensureOpened();
    }
    
    public void logout() {
        ensureAuthenticated();

        URI uri = baseUri().path("authentication/logout").build();
        get(uri);
    }

    //
    // Extensions
    //
    
    @SuppressWarnings({"unchecked"})
    public <T extends Extension> T ext(final Class<T> type) {
        T ext = (T)extensions.get(type.getName());
        if (ext == null) {
            throw new IllegalArgumentException("Unknown client extension type: " + type);
        }
        return ext;
    }
}