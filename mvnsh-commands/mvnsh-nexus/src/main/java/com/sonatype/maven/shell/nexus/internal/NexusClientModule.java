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

import com.google.inject.AbstractModule;
import com.google.inject.name.Names;
import com.sonatype.maven.shell.nexus.AdminClient;
import com.sonatype.maven.shell.nexus.BasicClient;
import com.sonatype.maven.shell.nexus.M2SettingsClient;
import com.sonatype.maven.shell.nexus.NexusClient;
import com.sonatype.maven.shell.nexus.RepositoryClient;
import com.sonatype.maven.shell.nexus.StagingClient;
import com.sonatype.maven.shell.nexus.UserClient;

import javax.inject.Named;

/**
 * Nexus client module.
 *
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @since 0.9
 */
@Named
public class NexusClientModule
    extends AbstractModule
{
    @Override
    protected void configure() {
        bind( NexusClient.class ).to(NexusClientImpl.class);
        bind(BasicClient.class, BasicClientImpl.class);
        bind( AdminClient.class, AdminClientImpl.class );
        bind( UserClient.class, UserClientImpl.class );
        bind( RepositoryClient.class, RepositoryClientImpl.class );
        bind( M2SettingsClient.class, M2SettingsClientImpl.class );
        bind( StagingClient.class, StagingClientImpl.class );
    }

    private <T extends NexusClient.Extension> void bind( Class<T> key, Class<? extends T> impl )
    {
        bind( NexusClient.Extension.class ).annotatedWith( Names.named( key.getName() ) ).to( impl );
    }
}
