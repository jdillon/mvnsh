/*
 * Sonatype Maven Shell (TM) Commercial Version.
 *
 * Copyright (c) 2009 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at http://www.sonatype.com/products/mvnsh/attributions/.
 * "Sonatype" and "Sonatype Nexus" are trademarks of Sonatype, Inc.
 */

package com.sonatype.maven.shell.commands.nexus;

import com.sonatype.maven.shell.nexus.NexusClient;
import org.sonatype.gshell.command.Command;
import org.sonatype.gshell.command.CommandContext;
import org.sonatype.gshell.util.pref.Preferences;
import org.sonatype.gshell.variables.Variables;

/**
 * Disconnect from a Nexus server.
 *
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @since 0.9
 */
@Command(name = "nexus/disconnect")
@Preferences(path = "commands/nexus/disconnect")
public class DisconnectCommand
    extends NexusCommandSupport
{
    @Override
    protected Object execute(final CommandContext context, final NexusClient client) throws Exception {
        assert context != null;
        assert client != null;
        Variables vars = context.getVariables();

        if (client.isAuthenticated()) {
            log.info("Logging out");

            client.logout();
        }

        log.info("Closing");
        client.close();

        vars.unset(NexusClient.class);

        return Result.SUCCESS;
    }
}