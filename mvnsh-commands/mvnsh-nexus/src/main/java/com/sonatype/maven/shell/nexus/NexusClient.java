/*
 * Copyright (c) 2009-present Sonatype, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Apache License v2.0 which accompanies this distribution.
 * The Eclipse Public License is available at
 *   http://www.eclipse.org/legal/epl-v10.html
 * The Apache License v2.0 is available at
 *   http://www.apache.org/licenses/LICENSE-2.0.html
 * You may elect to redistribute this code under either of these licenses.
 */
package com.sonatype.maven.shell.nexus;

import org.sonatype.gshell.util.PrintBuffer;
import org.sonatype.nexus.rest.model.ErrorMessage;
import org.sonatype.security.rest.model.AuthenticationLoginResource;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import java.net.URI;
import java.util.List;

/**
 * Nexus client.
 *
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @since 0.9
 *
 * @see <a href="https://docs.sonatype.com/display/Nx/Nexus+Rest+API">Nexus API</a>
 */
public interface NexusClient
{
    void open(URI baseUri, String instance);

    boolean isOpen();

    void ensureOpened();
    
    void close();

    URI getBaseUri();

    String getInstance();

    //
    // Extension Support
    //

    UriBuilder baseUri();

    String get(URI uri);

    String get(URI uri, MultivaluedMap<String,String> params);

    String head(URI uri);

    String post(URI uri, String payload);

    String put(URI uri, String payload);

    String delete(URI uri);

    String request(URI uri, String method, MediaType type, MediaType accept, String payload);

    //
    // Marshaling
    //

    String marshal(Class type, Object content);

    <T> T unmarshal(Class<T> type, String content);

    //
    // Authentication
    //
    
    AuthenticationLoginResource login(String username, String password);

    void logout();

    boolean isAuthenticated();

    void ensureAuthenticated();

    //
    // Extensions
    //

    interface Extension
    {
        // empty
    }

    <T extends Extension> T ext(Class<T> type);

    //
    // Failures
    //

    static class RequestFailed
        extends RuntimeException
    {
        public final Response.StatusType status;

        public final List<ErrorMessage> errors;

        public RequestFailed(final Response.StatusType status, final List<ErrorMessage> errors) {
            assert status != null;
            // errors could be null
            this.status = status;
            this.errors = errors;
        }

        @Override
        public String getMessage() {
            PrintBuffer buff = new PrintBuffer();
            buff.format("%s (%d)", status.getReasonPhrase(), status.getStatusCode());

            if (errors != null) {
                int i = 1;
                for (ErrorMessage error : errors) {
                    buff.println();
                    buff.format("    [%d] %s (%s)", i++, error.getMsg(), error.getId());
                }
            }

            return buff.toString();
        }
    }

    static class NotConnectedFailure
        extends RuntimeException
    {
        public NotConnectedFailure() {}
    }
}