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

import com.sonatype.maven.shell.nexus.NexusClient;
import com.sonatype.maven.shell.nexus.internal.marshal.MarshallerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.UriBuilder;

/**
 * Support for {@link NexusClient.Extension} implementations.
 *
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @since 0.9
 */
public class NexusClientExtensionSupport
    implements NexusClient.Extension
{
    protected final Logger log = LoggerFactory.getLogger(getClass());

    protected final NexusClient client;

    private MarshallerFactory marshallerFactory;

    protected NexusClientExtensionSupport(final NexusClient client) {
        assert client != null;
        this.client = client;
        this.marshallerFactory = createMarshallerFactory();
    }

    protected MarshallerFactory createMarshallerFactory() {
        return null;
    }

    protected void ensureOpened() {
        client.ensureOpened();
    }

    protected void ensureAuthenticated() {
        client.ensureAuthenticated();
    }

    protected UriBuilder baseUri() {
        return client.baseUri();
    }

    @SuppressWarnings({"unchecked"})
    protected String marshal(final Class type, final Object content) {
        if (marshallerFactory == null) {
            return client.marshal(type, content);
        }
        return marshallerFactory.create(type).marshal(content);
    }

    protected <T> T unmarshal(final Class<T> type, final String content) {
        if (marshallerFactory == null) {
            return client.unmarshal(type, content);
        }
        return marshallerFactory.create(type).unmarshal(content);
    }
}