/*
 * Sonatype Maven Shell (TM) Commercial Version.
 *
 * Copyright (c) 2009 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at http://www.sonatype.com/products/mvnsh/attributions/.
 * "Sonatype" and "Sonatype Nexus" are trademarks of Sonatype, Inc.
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