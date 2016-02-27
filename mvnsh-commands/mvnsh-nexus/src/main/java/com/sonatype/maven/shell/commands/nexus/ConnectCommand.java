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
package com.sonatype.maven.shell.commands.nexus;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.sonatype.maven.shell.nexus.BasicClient;
import com.sonatype.maven.shell.nexus.NexusClient;
import org.sonatype.gshell.command.Command;
import org.sonatype.gshell.command.CommandContext;
import org.sonatype.gshell.command.IO;
import org.sonatype.gshell.util.cli2.Argument;
import org.sonatype.gshell.util.cli2.Option;
import org.sonatype.gshell.util.pref.Preference;
import org.sonatype.gshell.util.pref.Preferences;
import org.sonatype.gshell.variables.Variables;
import org.sonatype.nexus.rest.model.StatusResource;
import org.sonatype.security.rest.model.AuthenticationClientPermissions;
import org.sonatype.security.rest.model.AuthenticationLoginResource;

import java.net.URI;

/**
 * Connect to a Nexus server.
 *
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @since 0.9
 */
@Command(name = "nexus/connect")
@Preferences(path = "commands/nexus/connect")
public class ConnectCommand
    extends NexusCommandSupport
{
    private static final String DEFAULT_INSTANCE = "local";

    private final Provider<NexusClient> clientProvider;

    @Preference
    @Option(name = "i", longName="instance")
    private String instance = DEFAULT_INSTANCE;

    @Preference
    @Option(name = "u", longName="username")
    private String username;

    @Preference
    @Option(name = "p", longName="password")
    private String password;

    @Preference
    @Argument(required = true)
    private URI server;

    @Inject
    public ConnectCommand(final Provider<NexusClient> clientProvider) {
        assert clientProvider != null;
        this.clientProvider = clientProvider;
    }

    @Override
    protected NexusClient getClient(final Variables vars) {
        assert vars != null;

        NexusClient client;

        // Close if a client already exists
        try {
            client = super.getClient(vars);
            client.close();
        }
        catch (NexusClient.NotConnectedFailure e) {
            // ignore
        }

        // The create a new one
        client = clientProvider.get();

        return client;
    }

    @Override
    protected Object execute(final CommandContext context, final NexusClient client) throws Exception {
        assert context != null;
        assert client != null;
        IO io = context.getIo();

        //
        // TODO: Prompt for server/instance/username/password if not given, or --interactive is configured,
        //       need to bring back the prompt helper components from gshell 1.x
        //

        log.debug("Opening");

        client.open(server, instance);
    
        if (username != null) {
            log.debug("Logging in");

            AuthenticationLoginResource detail = client.login(username, password);
            AuthenticationClientPermissions perms = detail.getClientPermissions();

            if (perms.isLoggedIn()) {
                // TODO: Add colors

                io.println("User: {}", perms.getLoggedInUsername()); // TODO: i18n
                io.println("Source: {}", perms.getLoggedInUserSource()); // TODO: i18n

                // TODO: Add more details
            }
            else {
                io.error("Authentication failed"); // TODO: i18n
                return Result.FAILURE;
            }
        }

        // Try to fetch something before we bind into context
        StatusResource status = client.ext(BasicClient.class).status();

        context.getVariables().set(NexusClient.class, client);

        io.println("Connected to: {} ({} v{})", status.getBaseUrl(), status.getAppName(), status.getApiVersion()); // TODO: i18n

        return client;
    }
}