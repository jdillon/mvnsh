/*
 * Sonatype Maven Shell (TM) Commercial Version.
 *
 * Copyright (c) 2009 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at http://www.sonatype.com/products/mvnsh/attributions/.
 * "Sonatype" and "Sonatype Nexus" are trademarks of Sonatype, Inc.
 */

package com.sonatype.maven.shell.commands.nexus;

import com.sonatype.maven.shell.nexus.NexusClient;
import org.sonatype.gshell.command.support.CommandActionSupport;
import org.sonatype.gshell.command.CommandContext;
import org.sonatype.gshell.variables.Variables;

/**
 * Support for nexus commands.
 *
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @since 0.9
 */
public abstract class NexusCommandSupport
    extends CommandActionSupport
{
    public Object execute(final CommandContext context) throws Exception {
        assert context != null;
        Variables vars = context.getVariables();

        NexusClient client = getClient(vars);

        return execute(context, client);
    }

    protected NexusClient getClient(final Variables vars) {
        assert vars != null;
        
        NexusClient client = vars.get(NexusClient.class);
        if (client == null) {
            throw new NexusClient.NotConnectedFailure();
        }
        return client;
    }

    protected abstract Object execute(CommandContext context, NexusClient client) throws Exception;
}