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

package org.sonatype.maven.shell.commands.maven;

import com.google.inject.Inject;
import org.apache.maven.execution.MavenExecutionRequest;
import org.codehaus.plexus.classworlds.ClassWorld;
import org.codehaus.plexus.classworlds.realm.ClassRealm;
import org.codehaus.plexus.util.Os;
import org.sonatype.grrrowl.Growler;
import org.sonatype.gshell.command.Command;
import org.sonatype.gshell.command.support.CommandActionSupport;
import org.sonatype.gshell.command.CommandContext;
import org.sonatype.gshell.command.IO;
import org.sonatype.gshell.util.io.StreamJack;
import org.sonatype.gshell.util.io.StreamSet;
import org.sonatype.gshell.util.NameValue;
import org.sonatype.gshell.util.Strings;
import org.sonatype.gshell.util.cli2.Argument;
import org.sonatype.gshell.util.cli2.CliProcessor;
import org.sonatype.gshell.util.cli2.CliProcessorAware;
import org.sonatype.gshell.util.cli2.Option;
import org.sonatype.gshell.util.pref.Preference;
import org.sonatype.gshell.util.pref.Preferences;
import org.sonatype.gshell.variables.Variables;
import org.sonatype.maven.shell.maven.MavenRuntime;
import org.sonatype.maven.shell.maven.MavenRuntimeConfiguration;
import org.sonatype.maven.shell.maven.MavenSystem;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import static org.apache.maven.execution.MavenExecutionRequest.CHECKSUM_POLICY_FAIL;
import static org.apache.maven.execution.MavenExecutionRequest.CHECKSUM_POLICY_WARN;
import static org.apache.maven.execution.MavenExecutionRequest.REACTOR_FAIL_AT_END;
import static org.apache.maven.execution.MavenExecutionRequest.REACTOR_FAIL_FAST;
import static org.apache.maven.execution.MavenExecutionRequest.REACTOR_FAIL_NEVER;
import static org.apache.maven.execution.MavenExecutionRequest.REACTOR_MAKE_BOTH;
import static org.apache.maven.execution.MavenExecutionRequest.REACTOR_MAKE_DOWNSTREAM;
import static org.apache.maven.execution.MavenExecutionRequest.REACTOR_MAKE_UPSTREAM;
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
    implements CliProcessorAware
{
    @Option(name="v", longName="version")
    private boolean version;

    @Option(name="f", longName="file")
    private File file;

    private Properties props;

    @Option(name="D", longName="define")
    protected void setProperty(final String input) {
        assert input != null;

        if (props == null) {
            props = new Properties();
        }

        NameValue nv = NameValue.parse(input);
        props.setProperty(nv.name, nv.value);
    }

    /**
     * @since 0.10
     */
    @Option(name="M", longName="maven")
    protected void setMavenProperty(final String input) {
        assert input != null;

        if (props == null) {
            props = new Properties();
        }

        NameValue nv = NameValue.parse(input);
        props.setProperty("maven." + nv.name, nv.value);
    }

    @Preference
    @Option(name="o", longName="offline")
    private Boolean offline;

    @Preference
    @Option(name="q", longName="quiet")
    private Boolean quiet;

    @Preference
    @Option(name="X", longName="debug")
    private Boolean debug;

    @Preference
    @Option(name="e", longName="errors")
    private Boolean showErrors;

    @Option(name="N", longName="non-recursive")
    private Boolean nonRecursive;

    @Option(name="U", longName="update-snapshots")
    private Boolean updateSnapshots;

    private List<String> profiles;

    @Option(name="P", longName="activate-profiles")
    private void addProfile(final String profile) {
        assert profile != null;

        if (profiles == null) {
            profiles = new ArrayList<String>();
        }

        for (String p : profile.split(",")) {
            profiles.add(p.trim());
        }
    }

    @Preference
    @Option(name="B", longName="batch-mode")
    private Boolean batch;

    @Option(name="cpu", longName="check-plugin-updates")
    private boolean checkPluginUpdates;

    @Option(name="up", longName="update-plugins")
    private boolean updatePlugins;

    @Option(name="npu", longName="no-plugin-updates")
    private boolean noPluginUpdates;

    @Option(name="nsu", longName="no-shapshot-updates")
    private Boolean noSnapshotUpdates;

    @Option(name="C", longName="strict-checksums")
    private boolean strictChecksums;

    @Option(name="c", longName="lax-checksums")
    private boolean laxChecksums;

    @Preference
    @Option(name="s", longName="settings")
    private File settingsFile;

    @Preference
    @Option(name="gs", longName="global-settings")
    private File globalSettingsFile;

    @Preference
    @Option(name="t", longName="toolchains")
    private File toolChainsFile;

    @Option(name="ff", longName="fail-fast")
    private boolean failFast;

    @Option(name="fae", longName="fail-at-end")
    private boolean failAtEnd;

    @Option(name="fn", longName="fail-never")
    private boolean failNever;

    @Option(name="rf", longName="resume-from")
    private String resumeFrom;

    private List<String> selectedProjects;

    @Option(name="pl", longName="projects")
    private void addSelectedProject(final String project) {
        assert project != null;

        if (selectedProjects == null) {
            selectedProjects = new ArrayList<String>();
        }

        for (String p : project.split(",")) {
            selectedProjects.add(p.trim());
        }
    }

    @Option(name="am", longName="also-make")
    private boolean alsoMake;

    @Option(name="amd", longName="also-make-dependents")
    private boolean alsoMakeDependents;

    @Option(name="l", longName="log-file")
    private File logFile;

    @Preference
    @Option(name="V", longName="show-version")
    private Boolean showVersion;

    /**
     * @since 0.10
     */
    @Preference
    @Option(longName="color", args=1, optionalArg=true)
    private Boolean color;

    // HACK: Support --encrypt-master-password
    @Option(name="emp", longName="encrypt-master-password")
    private String encryptMasterPassword;

    // HACK: Support --encrypt-password
    @Option(name="ep", longName="encrypt-password")
    private String encryptPassword;

    @Argument()
    private List<String> goals;

    private static enum Notifications
    {
        BUILD_PASSED, BUILD_FAILED
    }

    private final MavenSystem maven;

    private Growler growler;
    
    @Preference
    private boolean growl = true;

    @Inject
    public MavenCommand(final MavenSystem maven) {
        assert maven != null;
        this.maven = maven;
    }

    // HACK: Setup growl once, so clones get the same instance, no real init or registered hook in gshell yet, so we have to use this
    //       could setup up some support to register an event listener in CommandActionSupport or something later to really fix
    @Override
    public void setName(final String name) {
        super.setName(name);

        // Setup growl support
        this.growler = new Growler(name, Notifications.class);
        this.growler.register();
    }

    public void setProcessor(final CliProcessor processor) {
        assert processor != null;
        processor.setFlavor(CliProcessor.Flavor.GNU);
    }

    public Object execute(final CommandContext context) throws Exception {
        assert context != null;
        IO io = context.getIo();
        Variables vars = context.getVariables();
        
        if (version) {
            io.println(maven.getVersion());
            return Result.SUCCESS;
        }

        // HACK: support --encrypt-master-password and --encrypt-password
        if (encryptMasterPassword != null || encryptPassword != null) {
            // TODO: Inspect the registry to find the EncryptPasswordCommand's name, for now just hard-code
            String command = "encrypt-password";

            // HACK: Put all props into System, the security muck needs it
            if (props != null) {
                System.getProperties().putAll(props);
            }

            if (encryptMasterPassword != null) {
                return context.getShell().execute(command, "-m", encryptMasterPassword);
            }
            if (encryptPassword != null) {
                return context.getShell().execute(command, encryptPassword);
            }
        }

        System.setProperty(MavenSystem.MAVEN_HOME, vars.get(SHELL_HOME, File.class).getAbsolutePath());

        MavenRuntimeConfiguration config = new MavenRuntimeConfiguration();

        config.setBaseDirectory(vars.get(SHELL_USER_DIR, File.class));

        ClassWorld world = new ClassWorld("plexus.core", Thread.currentThread().getContextClassLoader());
        config.setClassWorld(world);
        
        if (file != null) {
            config.setPomFile(file);
        }
        if (profiles != null) {
            config.getProfiles().addAll(profiles);
        }
        if (quiet != null) {
            config.setQuiet(quiet);
        }
        if (debug != null) {
            config.setDebug(debug);
        }
        if (showVersion != null) {
            config.setShowVersion(showVersion);
        }
        if (props != null) {
            config.getProperties().putAll(props);
        }
        if (settingsFile != null) {
            config.setSettingsFile(settingsFile);
        }
        if (globalSettingsFile != null) {
            config.setGlobalSettingsFile(globalSettingsFile);
        }
        if (logFile != null) {
            config.setLogFile(logFile);
        }

        customize(config);

        MavenRuntime runtime = maven.create(config);
        MavenExecutionRequest request = runtime.create();

        if (offline != null) {
            request.setOffline(offline);
        }
        if (goals != null) {
            request.setGoals(goals);
        }
        if (batch != null) {
            request.setInteractiveMode(!batch);
        }
        if (resumeFrom != null) {
            request.setResumeFrom(resumeFrom);
        }
        if (toolChainsFile != null) {
            request.setUserToolchainsFile(toolChainsFile);
        }
        if (showErrors != null) {
            request.setShowErrors(showErrors);
        }
        if (nonRecursive != null) {
            request.setRecursive(!nonRecursive);
        }
        if (updateSnapshots != null) {
            request.setUpdateSnapshots(updateSnapshots);
        }
        if (noSnapshotUpdates != null) {
            request.setNoSnapshotUpdates(noSnapshotUpdates);
        }
        if (selectedProjects != null) {
            request.setSelectedProjects(selectedProjects);
        }

        if (strictChecksums) {
            request.setGlobalChecksumPolicy(CHECKSUM_POLICY_FAIL);
        }
        if (laxChecksums) {
            request.setGlobalChecksumPolicy(CHECKSUM_POLICY_WARN);
        }

        if (failFast) {
            request.setReactorFailureBehavior(REACTOR_FAIL_FAST);
        }
        else if (failAtEnd) {
            request.setReactorFailureBehavior(REACTOR_FAIL_AT_END);
        }
        else if (failNever) {
            request.setReactorFailureBehavior(REACTOR_FAIL_NEVER);
        }

        if (alsoMake && !alsoMakeDependents) {
            request.setMakeBehavior(REACTOR_MAKE_UPSTREAM);
        }
        else if (!alsoMake && alsoMakeDependents) {
            request.setMakeBehavior(REACTOR_MAKE_DOWNSTREAM);
        }
        else if (alsoMake && alsoMakeDependents) {
            request.setMakeBehavior(REACTOR_MAKE_BOTH);
        }

        // Customize the plugin groups
        request.addPluginGroup("org.apache.maven.plugins");
        request.addPluginGroup("org.codehaus.mojo");
        request.addPluginGroup("com.sonatype.maven.plugins");
        request.addPluginGroup("org.sonatype.maven.plugins");

        // Setup output colorization
        StreamSet current = StreamJack.current();
        StreamSet streams;
        if (color == null || color) {
            // Complain if the user asked for color and its not supported
            if (color != null && !io.getTerminal().isAnsiSupported()) {
                log.warn("ANSI color is not supported by the current terminal");
            }
            streams = new StreamSet(current.in, new ColorizingStream(current.out), new ColorizingStream(current.err));
        }
        else {
            streams = current;
        }
        config.setStreams(streams);

        StreamJack.register(streams);

        // Execute Maven
        int result = 0;
        try {
            result = runtime.execute(request);
        }
        finally {
            StreamJack.deregister();

            // HACK: Not sure why, but we need to reset the terminal after some mvn builds
            io.getTerminal().reset();

            if (Os.isFamily(Os.FAMILY_WINDOWS)) {
                Runnable gc = new Runnable()
                {
                    public void run() {
                        for (int i=0; i<2; i++) {
                            System.runFinalization();
                            Thread.yield();
                            System.gc();
                            Thread.yield();
                        }
                    }
                };

                gc.run();
                
                // HACK: Dispose all realms, to help avoid problems
                for (ClassRealm realm : (List<ClassRealm>)world.getRealms()) {
                    world.disposeRealm(realm.getId());
                }
                world = null;

                gc.run();
            }
        }

        if (growl) {
            String cl = String.format("%s %s", getName(), Strings.join(context.getArguments(), " "));

            if (result == 0) {
                growler.growl(
                    Notifications.BUILD_PASSED,
                    "BUILD SUCCESS", // TODO: i18n
                    cl);
            }
            else {
                growler.growl(
                    Notifications.BUILD_FAILED,
                    "BUILD FAILURE", // TODO: i18n
                    cl);
            }
        }

        return result;
    }

    /**
     * @since 0.9
     */
    protected void customize(final MavenRuntimeConfiguration config) {
        // empty
    }
}