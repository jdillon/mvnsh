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

package com.sonatype.maven.shell.nexus.internal.wink;

import org.apache.commons.codec.binary.Base64;
import org.apache.wink.client.ClientRequest;
import org.apache.wink.client.ClientResponse;
import org.apache.wink.client.handlers.ClientHandler;
import org.apache.wink.client.handlers.HandlerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.HttpHeaders;

/**
 * Basic HTTP Auth security handler.
 *
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @since 0.9
 */
public class BasicAuthSecurityHandler
    implements ClientHandler
{
    private static final Logger log = LoggerFactory.getLogger(BasicAuthSecurityHandler.class);

    private final String auth;

    public BasicAuthSecurityHandler(final String username, final String password) {
        if (username != null && password != null) {
            auth = "Basic " + new String(Base64.encodeBase64((username + ":" + password).getBytes()));
        }
        else {
            auth = null;
        }
    }

    public ClientResponse handle(final ClientRequest request, final HandlerContext context) throws Exception {
        assert request != null;
        assert context != null;

        if (auth != null) {
            log.debug("Setting auth header");
            request.getHeaders().putSingle(HttpHeaders.AUTHORIZATION, auth);
        }

        return context.doChain(request);
    }
}
