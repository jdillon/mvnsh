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
package org.sonatype.maven.shell.commands.maven;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.sonatype.gshell.command.Command;
import org.sonatype.gshell.command.CommandContext;
import org.sonatype.gshell.command.IO;
import org.sonatype.gshell.command.support.CommandActionSupport;
import org.sonatype.gshell.util.cli2.OpaqueArguments;
import org.sonatype.gshell.util.pref.Preferences;
import org.sonatype.gshell.variables.Variables;
import org.sonatype.maven.shell.maven.MavenSystem;

import org.apache.maven.cli.CliRequestBuilder;
import org.apache.maven.cli.MavenCli;

import static org.sonatype.gshell.variables.VariableNames.SHELL_HOME;
import static org.sonatype.gshell.variables.VariableNames.SHELL_USER_DIR;

/**
 * Execute Maven.
 *
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @since 0.7
 */
@Command(name = "mvn")
@Preferences(path = "commands/mvn")
public class MavenCommand
    extends CommandActionSupport
    implements OpaqueArguments
{
    public Object execute(final CommandContext context) throws Exception {
        assert context != null;

        IO io = context.getIo();
        Variables vars = context.getVariables();

        System.setProperty(MavenSystem.MAVEN_HOME, vars.get(SHELL_HOME, File.class).getAbsolutePath());

        CliRequestBuilder request = new CliRequestBuilder();
        request.setArguments(strings(context.getArguments()));
        request.setWorkingDirectory(vars.get(SHELL_USER_DIR, File.class));

        // FIXME: This should follow same logic in bin/mvn script to search for .mvn directory
        request.setProjectDirectory(vars.get(SHELL_USER_DIR, File.class));

        MavenCli cli = new MavenCli();
        return cli.doMain(request.build());
    }

    private static List<String> strings(final Object[] input) {
        List<String> result = new ArrayList<String>(input.length);
        for (Object value : input) {
            result.add(String.valueOf(value));
        }
        return result;
    }
}