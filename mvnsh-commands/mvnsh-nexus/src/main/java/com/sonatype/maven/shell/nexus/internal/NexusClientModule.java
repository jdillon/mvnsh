/*
 * Sonatype Maven Shell (TM) Commercial Version.
 *
 * Copyright (c) 2009 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at http://www.sonatype.com/products/mvnsh/attributions/.
 * "Sonatype" and "Sonatype Nexus" are trademarks of Sonatype, Inc.
 */

package com.sonatype.maven.shell.nexus.internal;

import com.google.inject.AbstractModule;
import com.google.inject.multibindings.MapBinder;
import com.sonatype.maven.shell.nexus.AdminClient;
import com.sonatype.maven.shell.nexus.BasicClient;
import com.sonatype.maven.shell.nexus.M2SettingsClient;
import com.sonatype.maven.shell.nexus.NexusClient;
import com.sonatype.maven.shell.nexus.RepositoryClient;
import com.sonatype.maven.shell.nexus.StagingClient;
import com.sonatype.maven.shell.nexus.UserClient;

/**
 * Nexus client module.
 *
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @since 0.9
 */
public class NexusClientModule
    extends AbstractModule
{
    @Override
    protected void configure() {
        MapBinder<Class,NexusClient.Extension> extbinder
            = MapBinder.newMapBinder(binder(), Class.class, NexusClient.Extension.class);

        extbinder.addBinding(BasicClient.class).to(BasicClientImpl.class);
        extbinder.addBinding(AdminClient.class).to(AdminClientImpl.class);
        extbinder.addBinding(UserClient.class).to(UserClientImpl.class);
        extbinder.addBinding(RepositoryClient.class).to(RepositoryClientImpl.class);
        extbinder.addBinding(M2SettingsClient.class).to(M2SettingsClientImpl.class);
        extbinder.addBinding(StagingClient.class).to(StagingClientImpl.class);

        bind(NexusClient.class).to(NexusClientImpl.class);
    }
}