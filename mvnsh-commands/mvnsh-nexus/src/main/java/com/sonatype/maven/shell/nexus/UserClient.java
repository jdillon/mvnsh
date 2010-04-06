/*
 * Sonatype Maven Shell (TM) Commercial Version.
 *
 * Copyright (c) 2009 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at http://www.sonatype.com/products/mvnsh/attributions/.
 * "Sonatype" and "Sonatype Nexus" are trademarks of Sonatype, Inc.
 */

package com.sonatype.maven.shell.nexus;

import org.sonatype.security.rest.model.UserResource;

import java.util.List;

/**
 * User client extension.
 *
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @since 0.9
 *
 * @see <a href="https://docs.sonatype.com/display/Nx/Nexus+Rest+API">Nexus API</a>
 */
public interface UserClient
    extends NexusClient.Extension
{
    List<UserResource> list();

    UserResource create(UserResource user);

    void delete(String userId);

    UserResource fetch(String userId);

    UserResource update(UserResource user);

    // TODO: reset-password

    // TODO: forgot-userid

    // TODO: forgot-password

    // TODO: change-password
}