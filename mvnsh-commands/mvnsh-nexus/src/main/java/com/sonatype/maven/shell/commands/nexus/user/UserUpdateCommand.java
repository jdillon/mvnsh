/*******************************************************************************
 * Copyright (c) 2009-present Sonatype, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Apache License v2.0 which accompanies this distribution.
 * The Eclipse Public License is available at 
 *   http://www.eclipse.org/legal/epl-v10.html
 * The Apache License v2.0 is available at
 *   http://www.apache.org/licenses/LICENSE-2.0.html
 * You may elect to redistribute this code under either of these licenses. 
 *******************************************************************************/

package com.sonatype.maven.shell.commands.nexus.user;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.sonatype.maven.shell.commands.nexus.NexusCommandSupport;
import com.sonatype.maven.shell.nexus.NexusClient;
import com.sonatype.maven.shell.nexus.UserClient;
import org.sonatype.gshell.command.Command;
import org.sonatype.gshell.command.CommandContext;
import org.sonatype.gshell.command.IO;
import org.sonatype.gshell.util.io.PromptReader;
import org.sonatype.gshell.util.cli2.Argument;
import org.sonatype.gshell.util.cli2.Option;
import org.sonatype.gshell.util.pref.Preferences;
import org.sonatype.security.rest.model.UserResource;

import java.util.ArrayList;
import java.util.List;

/**
 * Update a user.
 *
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @since 0.9
 */
@Command(name = "nexus/user/update")
@Preferences(path = "commands/nexus/user/update")
public class UserUpdateCommand
    extends NexusCommandSupport
{
    private final Provider<PromptReader> promptProvider;

    @Option(name="n", longName="name")
    private String name;

    @Option(name="e", longName="email")
    private String email;

    @Option(name="a", longName="active", optionalArg=true)
    private Boolean active;

    @Option(name="m", longName="user-managed", optionalArg=true)
    private Boolean userManaged;

    @Option(name="r", longName="roles")
    private List<String> roles;

    @Argument(required=true)
    private String userId;

    @Inject
    public UserUpdateCommand(final Provider<PromptReader> promptProvider) {
        assert promptProvider != null;
        this.promptProvider = promptProvider;
    }

    @Override
    protected Object execute(final CommandContext context, final NexusClient client) throws Exception {
        assert context != null;
        assert client != null;
        IO io = context.getIo();

        //
        // TODO: Fetch the current details, and use them as defaults
        //
        
        UserResource user = new UserResource();

        // Prompt for any missing details
        PromptReader prompt = promptProvider.get();

        user.setUserId(userId);
        
        // TODO: i18n all of this

        // TODO: Add color

        if (name == null) {
            name = prompt.readLine("User name: ");
        }
        user.setName(name);

        if (email == null) {
            email = prompt.readLine("User email: ");
        }
        user.setEmail(email);

        if (active == null) {
            String tmp = prompt.readLine("User active: ");
            active = Boolean.parseBoolean(tmp);
        }
        if (active) {
            user.setStatus("active");
        }
        else {
            user.setStatus("disabled");
        }

        if (userManaged == null) {
            String tmp = prompt.readLine("User managed: ");
            userManaged = Boolean.parseBoolean(tmp);
        }
        user.setUserManaged(userManaged);

        if (roles == null) {
            roles = new ArrayList<String>();

            String tmp;
            while ((tmp = prompt.readLine("User role: ")) != null && tmp.trim().length() != 0) {
                roles.add(tmp);
            }

        }
        user.getRoles().addAll(roles);

        //
        // TODO: Verify input if interactive
        //

        // Create the user
        user = client.ext(UserClient.class).update(user);

        io.println("Updated user: {}", user.getResourceURI());

        return user;
    }
}