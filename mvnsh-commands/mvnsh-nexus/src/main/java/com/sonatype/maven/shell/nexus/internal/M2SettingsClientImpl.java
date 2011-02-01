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