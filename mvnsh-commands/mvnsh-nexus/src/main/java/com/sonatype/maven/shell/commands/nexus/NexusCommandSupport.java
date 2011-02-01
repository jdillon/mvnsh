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