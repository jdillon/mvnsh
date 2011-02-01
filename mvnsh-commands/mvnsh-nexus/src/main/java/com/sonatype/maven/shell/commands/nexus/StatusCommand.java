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

import com.sonatype.maven.shell.nexus.BasicClient;
import com.sonatype.maven.shell.nexus.NexusClient;
import org.sonatype.gshell.command.Command;
import org.sonatype.gshell.command.CommandContext;
import org.sonatype.gshell.command.IO;
import org.sonatype.gshell.util.pref.Preferences;
import org.sonatype.nexus.rest.model.StatusResource;

/**
 * Get status of a Nexus.
 *
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @since 0.9
 */
@Command(name = "nexus/status")
@Preferences(path = "commands/nexus/status")
public class StatusCommand
    extends NexusCommandSupport
{
    @Override
    protected Object execute(final CommandContext context, final NexusClient client) throws Exception {
        assert context != null;
        assert client != null;
        IO io = context.getIo();

        StatusResource status = client.ext(BasicClient.class).status();

        // TODO: Add colors

        io.println("App Name: {}", status.getAppName()); // TODO: i18n
        io.println("Version: {}", status.getVersion()); // TODO: i18n
        io.println("State: {}", status.getState());

        // TODO: Add more details

        return status;
    }
}