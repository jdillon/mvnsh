/*
 * Sonatype Maven Shell (TM) Commercial Version.
 *
 * Copyright (c) 2009 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at http://www.sonatype.com/products/mvnsh/attributions/.
 * "Sonatype" and "Sonatype Nexus" are trademarks of Sonatype, Inc.
 */

package com.sonatype.maven.shell.nexus.internal;

import com.google.inject.Inject;
import com.sonatype.maven.shell.nexus.M2SettingsClient;
import com.sonatype.maven.shell.nexus.NexusClient;

import java.net.URI;

/**
 * {@link M2SettingsClient} implementation.
 *
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @since 0.9
 */
public class M2SettingsClientImpl
    extends NexusClientExtensionSupport
    implements M2SettingsClient
{
    @Inject
    public M2SettingsClientImpl(final NexusClient client) {
        super(client);
    }

    public String fetch(final String templateId) {
        assert templateId != null;

        ensureOpened();

        URI uri = baseUri().path("templates/settings").path(templateId).path("content").build();
        return client.get(uri);
    }
}