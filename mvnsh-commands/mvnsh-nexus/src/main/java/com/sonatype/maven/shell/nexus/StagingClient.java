/*
 * Sonatype Maven Shell (TM) Commercial Version.
 *
 * Copyright (c) 2009 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at http://www.sonatype.com/products/mvnsh/attributions/.
 * "Sonatype" and "Sonatype Nexus" are trademarks of Sonatype, Inc.
 */

package com.sonatype.maven.shell.nexus;

import org.sonatype.nexus.rest.model.staging.StagingProfile;
import org.sonatype.nexus.rest.model.staging.StagingProfileRepository;

import java.util.List;

/**
 * Staging client extension.
 *
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @since 0.9
 *
 * @see <a href="https://docs.sonatype.com/display/Nexus/Staging+API">Staging API</a>
 */
public interface StagingClient
    extends NexusClient.Extension
{
    List<StagingProfile> listProfiles();

    List<StagingProfileRepository> listRepositories(String profileId);

    void close(String profileId, String repositoryId, String description);

    void drop(String profileId, String repositoryId);

    void promote(String profileId, String stagedRepositoryId, String targetRepositoryId);

    // TODO: evaluate

    // TODO: Repo fetch

    // TODO: Profile add/update/delete/order/fetch
}