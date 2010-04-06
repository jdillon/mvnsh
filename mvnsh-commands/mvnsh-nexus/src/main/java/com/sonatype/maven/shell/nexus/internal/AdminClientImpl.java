/*
 * Sonatype Maven Shell (TM) Commercial Version.
 *
 * Copyright (c) 2009 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at http://www.sonatype.com/products/mvnsh/attributions/.
 * "Sonatype" and "Sonatype Nexus" are trademarks of Sonatype, Inc.
 */

package com.sonatype.maven.shell.nexus.internal;

import com.google.inject.Inject;
import com.sonatype.maven.shell.nexus.AdminClient;
import com.sonatype.maven.shell.nexus.NexusClient;

import javax.ws.rs.core.MediaType;
import java.net.URI;

/**
 * {@link AdminClient} implementation.
 *
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @since 0.9
 */
public class AdminClientImpl
    extends NexusClientExtensionSupport
    implements AdminClient
{
    @Inject
    public AdminClientImpl(final NexusClient client) {
        super(client);
    }

    private void command(final String payload) {
        assert payload != null;

        ensureAuthenticated();

        URI uri = client.baseUri().path("status").path("command").build();
        client.request(uri, "PUT", MediaType.TEXT_PLAIN_TYPE, MediaType.TEXT_PLAIN_TYPE, payload);
    }

    public void stop() {
        command("STOP");
    }

    public void start() {
        command("START");
    }

    public void restart() {
        command("RESTART");
    }

    public void kill() {
        try {
            command("KILL");
            log.warn("Expected exception after kill but did not catch one");
        }
        catch (Exception e) {
            // ignore
        }
    }
}