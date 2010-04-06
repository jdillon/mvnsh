/*
 * Sonatype Maven Shell (TM) Commercial Version.
 *
 * Copyright (c) 2009 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at http://www.sonatype.com/products/mvnsh/attributions/.
 * "Sonatype" and "Sonatype Nexus" are trademarks of Sonatype, Inc.
 */

package com.sonatype.maven.shell.nexus;

import org.sonatype.nexus.rest.model.RepositoryListResource;

import java.util.List;

/**
 * Repository client extension.
 *
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @since 0.9
 *
 * @see <a href="https://docs.sonatype.com/display/Nx/Nexus+Rest+API">Nexus API</a>
 */
public interface RepositoryClient
    extends NexusClient.Extension
{
    List<RepositoryListResource> list();

    // TODO: Lots more :-(
}