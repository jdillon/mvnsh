/*
 * Copyright (C) 2009 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.sonatype.maven.shell.commands.maven;

import com.google.inject.Inject;
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
import org.sonatype.gshell.util.NameValue;
import org.sonatype.gshell.util.Strings;
import org.sonatype.gshell.util.cli.OpaqueArguments;
import org.sonatype.gshell.util.cli.Option;
import org.sonatype.gshell.util.pref.Preference;
import org.sonatype.gshell.util.pref.Preferences;
import org.sonatype.gshell.vars.Variables;

import java.io.File;

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
{
    @Option(name="-f", aliases={"--file"}, argumentRequired=true)
    private File file;

    @Option(name = "-D", aliases = {"--define"}, argumentRequired = true)
    protected void setVariable(final String input) {
        NameValue nv = NameValue.parse(input);
        // FIXME: vars.set(nv.name, nv.value);
    }

    @Preference
    @Option(name="-o", aliases={"--offline"})
    private boolean offline;

    @Option(name = "-v", aliases = {"--version"})
    private boolean version;

    @Preference
    @Option(name = "-a", aliases = {"--quiet"})
    private boolean quiet;

    @Preference
    @Option(name = "-X", aliases = {"--debug"})
    private boolean debug;

    @Preference
    @Option(name = "-e", aliases = {"--errors"})
    private boolean errors;

    @Option(name = "-N", aliases = {"--non-recursive"})
    private boolean nonRecursive;

    @Option(name = "-U", aliases = {"--update-snapshots"})
    private boolean updateSnapshots;

    @Option(name = "-P", aliases = {"--activate-profiles"}, argumentRequired=true)
    private String activateProfiles;

    @Preference
    @Option(name = "-B", aliases = {"--batch-mode"})
    private boolean batch;

    @Option(name = "-cpu", aliases = {"--check-plugin-updates"})
    private boolean checkPluginUpdates;

    @Option(name = "-up", aliases = {"--update-plugins"})
    private boolean updatePlugins;

    @Option(name = "-npu", aliases = {"--no-plugin-updates"})
    private boolean noPluginUpdates;

    @Option(name = "-nsu", aliases = {"--no-shapshot-updates"})
    private boolean noSnapshotUpdates;

    @Option(name = "-C", aliases = {"--strict-checksums"})
    private boolean strictChecksums;

    @Option(name = "-c", aliases = {"--lax-checksums"})
    private boolean laxChecksums;

    @Preference
    @Option(name = "-s", aliases = {"--settings"}, argumentRequired=true)
    private File settings;

    @Preference
    @Option(name = "-gs", aliases = {"--global-settings"}, argumentRequired=true)
    private File globalSettings;

    @Preference
    @Option(name = "-t", aliases = {"--toolchains"}, argumentRequired=true)
    private File toolChains;

    @Option(name = "-ff", aliases = {"--fail-fast"})
    private boolean failFast;

    @Option(name = "-fae", aliases = {"--fail-at-end"})
    private boolean failAtEnd;

    @Option(name = "-fn", aliases = {"--fail-never"})
    private boolean failNever;

    @Option(name = "-rf", aliases = {"--resume-from"}, argumentRequired=true)
    private String resumeFrom;

    @Option(name = "-pl", aliases = {"--projects"}, argumentRequired=true)
    private String projects;

    @Option(name = "-am", aliases = {"--also-make"})
    private boolean alsoMake;

    @Option(name = "-amd", aliases = {"--also-make-dependents"})
    private boolean alsoMakeDependents;

    @Option(name = "-l", aliases = {"--log-file"}, argumentRequired=true)
    private File logFile;

    @Preference
    @Option(name = "-V", aliases = {"--show-version"})
    private boolean showVersion;

    @Option(name = "-emp", aliases = {"--encrypt-master-password"}, argumentRequired=true)
    private String encryptMasterPassword;

    @Option(name = "-ep", aliases = {"--encrypt-password"}, argumentRequired=true)
    private String encryptPassword;

    @Option(name = "-npr", aliases = {"--no-plugin-registry"})
    private boolean noPluginRegistry;

    private static enum Notifications
    {
        BUILD_PASSED, BUILD_FAILED
    }

    @Preference
    private boolean growl = true;

    private final PlexusRuntime plexus;

    @Inject
    public MavenCommand(final PlexusRuntime plexus) {
        assert plexus != null;
        this.plexus = plexus;
    }

    public Object execute(final CommandContext context) throws Exception {
        assert context != null;
        IO io = context.getIo();

        // Setup Growl
        Growler growler = new Growler("mvn", Notifications.class);
        growler.register();

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
//
        int result = 0;

        try {
            //
            // FIXME: Replace with the Embedder
            //
            
//            ClassWorld classWorld = new ClassWorld("plexus.core", Thread.currentThread().getContextClassLoader());
//            result = MavenCli.main(args, classWorld);
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