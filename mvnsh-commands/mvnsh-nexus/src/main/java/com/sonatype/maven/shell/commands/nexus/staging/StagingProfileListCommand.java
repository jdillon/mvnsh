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
import org.sonatype.gshell.util.pref.Preferences;
import org.sonatype.nexus.rest.model.staging.StagingProfile;

import java.util.List;

/**
 * List staged repository profiles.
 *
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @since 0.9
 */
@Command(name = "nexus/staging/profile/list")
@Preferences(path = "commands/nexus/staging/profile/list")
public class StagingProfileListCommand
    extends NexusCommandSupport
{
    @Override
    protected Object execute(final CommandContext context, final NexusClient client) throws Exception {
        assert context != null;
        assert client != null;
        IO io = context.getIo();

        List<StagingProfile> profiles = client.ext(StagingClient.class).listProfiles();

        for (StagingProfile profile : profiles) {
            io.println("Id: {}", profile.getId());
            io.println("  Name: {}", profile.getName());
            io.println("  URI: {}", profile.getResourceURI());
        }

        return profiles;
    }
}