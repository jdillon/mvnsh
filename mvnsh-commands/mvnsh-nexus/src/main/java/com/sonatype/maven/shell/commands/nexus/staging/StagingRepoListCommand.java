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

package com.sonatype.maven.shell.commands.nexus.staging;

import com.sonatype.maven.shell.commands.nexus.NexusCommandSupport;
import com.sonatype.maven.shell.nexus.NexusClient;
import com.sonatype.maven.shell.nexus.StagingClient;
import org.sonatype.gshell.command.Command;
import org.sonatype.gshell.command.CommandContext;
import org.sonatype.gshell.command.IO;
import org.sonatype.gshell.util.cli2.Argument;
import org.sonatype.gshell.util.pref.Preferences;
import org.sonatype.nexus.rest.model.staging.StagingProfileRepository;

import java.util.List;

/**
 * List staged repositories.
 *
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @since 0.9
 */
@Command(name = "nexus/staging/repo/list")
@Preferences(path = "commands/nexus/staging/repo/list")
public class StagingRepoListCommand
    extends NexusCommandSupport
{
    @Argument(required=true)
    private String profileId;

    @Override
    protected Object execute(final CommandContext context, final NexusClient client) throws Exception {
        assert context != null;
        assert client != null;
        IO io = context.getIo();

        List<StagingProfileRepository> repos = client.ext(StagingClient.class).listRepositories(profileId);

        for (StagingProfileRepository repo : repos) {
            io.println("ID: {}", repo.getRepositoryId());
            io.println("  Profile ID: {}", repo.getProfileId());
            io.println("  Description: {}", repo.getDescription());
            io.println("  Type: {}", repo.getType());
            io.println("  User ID: {}", repo.getUserId());
        }

        return repos;
    }
}