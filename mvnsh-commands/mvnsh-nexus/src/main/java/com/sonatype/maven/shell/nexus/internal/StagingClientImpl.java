/*
 * Sonatype Maven Shell (TM) Commercial Version.
 *
 * Copyright (c) 2009 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at http://www.sonatype.com/products/mvnsh/attributions/.
 * "Sonatype" and "Sonatype Nexus" are trademarks of Sonatype, Inc.
 */

package com.sonatype.maven.shell.nexus.internal;

import com.google.inject.Inject;
import com.sonatype.maven.shell.nexus.NexusClient;
import com.sonatype.maven.shell.nexus.StagingClient;
import com.sonatype.maven.shell.nexus.internal.marshal.MarshallerFactory;
import com.sonatype.maven.shell.nexus.internal.marshal.MarshallerFactorySupport;
import com.thoughtworks.xstream.XStream;
import org.sonatype.nexus.rest.model.staging.StagingProfile;
import org.sonatype.nexus.rest.model.staging.StagingProfileListResponse;
import org.sonatype.nexus.rest.model.staging.StagingProfileRepositoriesListResponse;
import org.sonatype.nexus.rest.model.staging.StagingProfileRepository;
import org.sonatype.nexus.rest.model.staging.StagingPromote;
import org.sonatype.nexus.rest.model.staging.StagingPromoteRequest;

import java.net.URI;
import java.util.List;

/**
 * {@link com.sonatype.maven.shell.nexus.StagingClient} implementation.
 *
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @since 0.9
 */
public class StagingClientImpl
    extends NexusClientExtensionSupport
    implements StagingClient
{
    @Inject
    public StagingClientImpl(final NexusClient client) {
        super(client);
    }

    @Override
    protected MarshallerFactory createMarshallerFactory() {
        return new MarshallerFactorySupport()
        {
            @Override
            protected XStream configure(final XStream xs) {
                assert xs != null;

                xs.alias("stagingProfiles", StagingProfileListResponse.class);
                xs.alias("stagingProfile", StagingProfile.class);

                xs.alias("stagingRepositories", StagingProfileRepositoriesListResponse.class);
                xs.alias("stagingProfileRepository", StagingProfileRepository.class);

                xs.alias("promoteRequest", StagingPromoteRequest.class);
                
                return super.configure(xs);
            }
        };
    }

    public List<StagingProfile> listProfiles() {
        ensureAuthenticated();

        URI uri = baseUri().path("staging/profiles").build();

        String content = client.get(uri);
        return unmarshal(StagingProfileListResponse.class, content).getData();
    }

    public List<StagingProfileRepository> listRepositories(final String profileId) {
        assert profileId != null;

        ensureAuthenticated();

        URI uri = baseUri().path("staging/profile_repositories").path(profileId).build();

        String content = client.get(uri);
        return unmarshal(StagingProfileRepositoriesListResponse.class, content).getData();
    }

    public void close(final String profileId, final String repositoryId, final String description) {
        assert profileId != null;
        assert repositoryId != null;
        assert description != null;

        ensureAuthenticated();

        URI uri = baseUri().path("staging/profiles").path(profileId).path("finish").build();

        StagingPromoteRequest request = new StagingPromoteRequest();
        StagingPromote data = new StagingPromote();
        data.setStagedRepositoryId(repositoryId);
        data.setDescription(description);
        request.setData(data);

        client.post(uri, marshal(StagingPromoteRequest.class, request));
    }

    public void drop(final String profileId, final String repositoryId) {
        assert profileId != null;
        assert repositoryId != null;

        ensureAuthenticated();

        URI uri = baseUri().path("staging/profiles").path(profileId).path("drop").build();

        StagingPromoteRequest request = new StagingPromoteRequest();
        StagingPromote data = new StagingPromote();
        
        data.setStagedRepositoryId(repositoryId);

        request.setData(data);

        client.post(uri, marshal(StagingPromoteRequest.class, request));
    }

    public void promote(final String profileId, final String stagedRepositoryId, final String targetRepositoryId) {
        assert profileId != null;
        assert stagedRepositoryId != null;
        assert targetRepositoryId != null;

        ensureAuthenticated();

        URI uri = baseUri().path("staging/profiles").path(profileId).path("promote").build();

        StagingPromoteRequest request = new StagingPromoteRequest();
        StagingPromote data = new StagingPromote();
        data.setStagedRepositoryId(stagedRepositoryId);
        data.setTargetRepositoryId(targetRepositoryId);
        request.setData(data);

        client.post(uri, marshal(StagingPromoteRequest.class, request));
    }
}