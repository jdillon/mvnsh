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