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
package com.sonatype.maven.shell.nexus.internal;

import com.google.inject.Inject;
import com.sonatype.maven.shell.nexus.BasicClient;
import com.sonatype.maven.shell.nexus.NexusClient;
import org.sonatype.nexus.rest.model.SearchResponse;
import org.sonatype.nexus.rest.model.StatusResource;
import org.sonatype.nexus.rest.model.StatusResourceResponse;

import javax.ws.rs.core.MultivaluedMap;
import java.net.URI;

/**
 * {@link BasicClient} implementation.
 *
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @since 0.9
 */
public class BasicClientImpl
    extends NexusClientExtensionSupport
    implements BasicClient
{
    @Inject
    public BasicClientImpl(final NexusClient client) {
        super(client);
    }

    public StatusResource status() {
        ensureOpened();

        URI uri = baseUri().path("status").build();
        String content = client.get(uri);
        return unmarshal(StatusResourceResponse.class, content).getData();
    }

    public SearchResponse search(final MultivaluedMap<String,String> params) {
        assert params != null;

        ensureOpened();

        URI uri = baseUri().path("data_index").build();
        String content = client.get(uri, params);
        return unmarshal(SearchResponse.class, content);
    }
}