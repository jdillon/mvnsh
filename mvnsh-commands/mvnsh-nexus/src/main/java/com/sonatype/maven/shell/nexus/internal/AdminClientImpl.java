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