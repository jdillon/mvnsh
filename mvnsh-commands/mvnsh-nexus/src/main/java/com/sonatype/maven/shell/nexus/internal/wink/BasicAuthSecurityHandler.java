/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */
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
