/*
 * Sonatype Maven Shell (TM) Commercial Version.
 *
 * Copyright (c) 2009 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at http://www.sonatype.com/products/mvnsh/attributions/.
 * "Sonatype" and "Sonatype Nexus" are trademarks of Sonatype, Inc.
 */

package com.sonatype.maven.shell.nexus.internal;

import org.sonatype.guice.bean.binders.WireModule;
import org.sonatype.guice.bean.locators.DefaultBeanLocator;

import com.google.inject.AbstractModule;
import com.google.inject.name.Names;
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
        bind( DefaultBeanLocator.class );

        install( new WireModule( new AbstractModule()
        {

            @Override
            protected void configure()
            {
                bind( NexusClient.class ).to( NexusClientImpl.class );
                bind( BasicClient.class, BasicClientImpl.class );
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
        } ) );
    }
}
