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
import com.sonatype.maven.shell.nexus.NexusClient;
import com.sonatype.maven.shell.nexus.RepositoryClient;
import org.sonatype.nexus.rest.model.RepositoryListResource;
import org.sonatype.nexus.rest.model.RepositoryListResourceResponse;

import java.net.URI;
import java.util.List;

/**
 * {@link RepositoryClient} implementation.
 *
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @since 0.9
 */
public class RepositoryClientImpl
    extends NexusClientExtensionSupport
    implements RepositoryClient
{
    @Inject
    public RepositoryClientImpl(final NexusClient client) {
        super(client);
    }

    public List<RepositoryListResource> list() {
        ensureOpened();

        URI uri = baseUri().path("repositories").build();
        String content = client.get(uri);
        return unmarshal(RepositoryListResourceResponse.class, content).getData();
    }
}