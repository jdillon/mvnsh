/*
 * Copyright (c) 2007-2009 Sonatype, Inc. All rights reserved.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package org.sonatype.maven.shell.commands.maven;

import jline.Terminal;
import org.apache.maven.cli.MavenCli;
import org.codehaus.plexus.classworlds.ClassWorld;
import org.codehaus.plexus.util.StringUtils;
import org.sonatype.grrrowl.Growler;
import static org.sonatype.gshell.vars.VariableNames.*;
import org.sonatype.gshell.command.Command;
import org.sonatype.gshell.command.CommandActionSupport;
import org.sonatype.gshell.command.CommandContext;
import org.sonatype.gshell.command.IO;
import org.sonatype.gshell.io.StreamJack;
import org.sonatype.gshell.io.StreamSet;
import org.sonatype.gshell.plexus.PlexusRuntime;
import org.sonatype.gshell.util.Arguments;
import org.sonatype.gshell.util.Strings;
import org.sonatype.gshell.util.cli.OpaqueArguments;
import org.sonatype.gshell.util.pref.Preference;
import org.sonatype.gshell.util.pref.Preferences;
import org.sonatype.gshell.vars.Variables;

/**
 * Execute Maven.
 *
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @since 0.7
 */
@Command(name="mvn")
@Preferences(path="commands/mvn")
public class MavenCommand
    extends CommandActionSupport
    implements OpaqueArguments
{
    private static enum Notifications
    {
        BUILD_PASSED, BUILD_FAILED
    }

    @Preference
    private boolean growl = true;

    public Object execute(final CommandContext context) throws Exception {
        assert context != null;
        IO io = context.getIo();

        // Setup Growl
        Growler growler = new Growler("mvn", Notifications.class);
        growler.register();

        //
        // FIXME: Replace with the Embedder
        //

        String[] args = Arguments.toStringArray(context.getArguments());

        // Propagate shell.user.dir to user.dir and shell.home to maven.home for MavenCLI
        Variables vars = context.getVariables();
        String userDir = vars.get(SHELL_USER_DIR, String.class);
        System.setProperty("user.dir", userDir);
        String homeDir = vars.get(SHELL_HOME, String.class);
        System.setProperty("maven.home", homeDir);

        log.debug("Invoking maven with args: {}, in dir: {}", StringUtils.join(args, " "), userDir);

        StreamSet current = StreamJack.current();
        StreamSet streams = new StreamSet(current.in, new ColorizingStream(current.out), new ColorizingStream(current.err));
        StreamJack.register(streams);

        int result;
        try {
            ClassWorld classWorld = new ClassWorld("plexus.core", Thread.currentThread().getContextClassLoader());
            result = MavenCli.main(args, classWorld);
        }
        finally {
            StreamJack.deregister();

            // HACK: Not sure why, but we need to reset the terminal after some mvn builds
            Terminal term = io.getTerminal();
            term.restore();
            term.init();

            // HACK: Attempt to let the VM clean up
            Thread.yield();
            System.runFinalization();
            Thread.yield();
            System.gc();
            Thread.yield();
        }

        if (growl) {
            if (result == 0) {
                growler.growl(
                    Notifications.BUILD_PASSED,
                    "Build Passed",
                    "Build was successful: " + getName() + " " + Strings.join(args, " "));
            }
            else {
                growler.growl(
                    Notifications.BUILD_FAILED,
                    "Build Failed",
                    "Build has failed: " + getName() + " " + Strings.join(args, " "));
            }
        }

        return result;
    }
}