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
import com.sonatype.maven.shell.nexus.UserClient;
import org.sonatype.security.rest.model.UserListResourceResponse;
import org.sonatype.security.rest.model.UserResource;
import org.sonatype.security.rest.model.UserResourceRequest;
import org.sonatype.security.rest.model.UserResourceResponse;

import java.net.URI;
import java.util.List;

/**
 * {@link UserClient} implementation.
 *
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @since 0.9
 */
public class UserClientImpl
    extends NexusClientExtensionSupport
    implements UserClient
{
    @Inject
    public UserClientImpl(final NexusClient client) {
        super(client);
    }

    public List<UserResource> list() {
        ensureAuthenticated();

        URI uri = baseUri().path("users").build();
        String content = client.get(uri);
        return unmarshal(UserListResourceResponse.class, content).getData();
    }

    public UserResource create(final UserResource user) {
        assert user != null;

        ensureAuthenticated();

        URI uri = baseUri().path("users").build();

        UserResourceRequest request = new UserResourceRequest();
        request.setData(user);

        String content = client.post(uri, marshal(UserResourceRequest.class, request));
        return unmarshal(UserResourceResponse.class, content).getData();
    }

    public UserResource fetch(final String userId) {
        assert userId != null;

        ensureAuthenticated();

        URI uri = baseUri().path("users").path(userId).build();
        String content = client.get(uri);
        return unmarshal(UserResourceResponse.class, content).getData();
    }

    public UserResource update(final UserResource user) {
        assert user != null;

        ensureAuthenticated();

        String userId = user.getUserId();
        assert userId != null;

        URI uri = baseUri().path("users").path(userId).build();

        UserResourceRequest request = new UserResourceRequest();
        request.setData(user);

        String content = client.put(uri, marshal(UserResourceRequest.class, request));
        return unmarshal(UserResourceResponse.class, content).getData();
    }

    public void delete(final String userId) {
        assert userId != null;

        ensureAuthenticated();

        URI uri = baseUri().path("users").path(userId).build();
        client.delete(uri);
    }
}