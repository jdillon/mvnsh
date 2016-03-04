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
package org.sonatype.maven.shell.maven;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;

import org.apache.maven.Maven;
import org.apache.maven.cli.CLIReportingUtils;
import org.apache.maven.exception.DefaultExceptionHandler;
import org.apache.maven.exception.ExceptionHandler;
import org.apache.maven.exception.ExceptionSummary;
import org.apache.maven.execution.DefaultMavenExecutionRequest;
import org.apache.maven.execution.MavenExecutionRequest;
import org.apache.maven.execution.MavenExecutionRequestPopulator;
import org.apache.maven.execution.MavenExecutionResult;
import org.apache.maven.lifecycle.LifecycleExecutionException;
import org.apache.maven.model.building.ModelProcessor;
import org.apache.maven.project.MavenProject;
import org.apache.maven.settings.building.DefaultSettingsBuildingRequest;
import org.apache.maven.settings.building.SettingsBuilder;
import org.apache.maven.settings.building.SettingsBuildingRequest;
import org.apache.maven.settings.building.SettingsBuildingResult;
import org.apache.maven.settings.building.SettingsProblem;
import org.codehaus.plexus.ContainerConfiguration;
import org.codehaus.plexus.DefaultContainerConfiguration;
import org.codehaus.plexus.DefaultPlexusContainer;
import org.codehaus.plexus.classworlds.ClassWorld;
import org.codehaus.plexus.classworlds.realm.ClassRealm;
import org.codehaus.plexus.logging.LoggerManager;
import org.codehaus.plexus.logging.console.ConsoleLoggerManager;
import org.codehaus.plexus.util.Os;
import org.codehaus.plexus.util.StringUtils;
import org.eclipse.aether.transfer.TransferListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonatype.gshell.util.io.Closer;
import org.sonatype.gshell.util.io.StreamSet;
import org.sonatype.gshell.util.yarn.Yarn;
import org.sonatype.maven.shell.maven.internal.BatchModeMavenTransferListener;
import org.sonatype.maven.shell.maven.internal.ConsoleMavenTransferListener;
import org.sonatype.maven.shell.maven.internal.ExecutionEventLogger;

import com.google.inject.Inject;
import com.google.inject.Provider;

import jline.Terminal;

/**
 * {@link MavenSystem} implementation.
 *
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @since 0.9
 */
public class MavenSystemImpl
    implements MavenSystem
{
    private static final Logger log = LoggerFactory.getLogger(MavenSystemImpl.class);

    static {
        // This prevents a thread leak in the org.apache.maven.artifact.resolver.  DefaultArtifactResolver (maven-compat)
        // which uses a thread pool that gets only shutdown when the artifact resolver is finalized.
        System.setProperty("maven.artifact.threads", "0");
    }

    private final Provider<Terminal> terminal;

    @Inject
    public MavenSystemImpl(final Provider<Terminal> terminal) {
        assert terminal != null;
        this.terminal = terminal;
    }

    public String getVersion() {
        return CLIReportingUtils.showVersion();
    }

    public MavenRuntime create(final MavenRuntimeConfiguration config) throws Exception {
        assert config != null;

        if (log.isDebugEnabled()) {
            log.debug("Creating runtime w/config: {}", Yarn.render(config, Yarn.Style.MULTI));
        }

        MavenRuntimeImpl runtime = new MavenRuntimeImpl(config);
        runtime.init();

        return runtime;
    }

    private File resolveFile(final File file, final File dir) {
        if (file == null) {
            return null;
        }
        else if (file.isAbsolute()) {
            return file;
        }
        else if (file.getPath().startsWith(File.separator)) {
            // drive-relative Windows path
            return file.getAbsoluteFile();
        }
        else {
            return new File(dir, file.getPath());
        }
    }

    //
    // FIXME: Make this puppy extensible, need to extend for pmaven at the least
    //

    private class MavenRuntimeImpl
        implements MavenRuntime
    {
        private static final String MVNSH = "mvnsh";

		private final Logger log = LoggerFactory.getLogger(MavenRuntimeImpl.class);

        private final MavenRuntimeConfiguration config;

        private LoggerManager loggerManager;
        private org.codehaus.plexus.logging.Logger plexusLogger;

        private PrintStream logStream;

        private DefaultPlexusContainer container;

        private MavenRuntimeImpl(final MavenRuntimeConfiguration config) {
            assert config != null;
            this.config = config;
        }

        public MavenRuntimeConfiguration getConfiguration() {
            return config;
        }

        private void init() throws Exception {
            log.debug("Initializing");

            // Make sure maven.home is absolute to avoid confusion on windows
            String mavenHome = System.getProperty(MAVEN_HOME);
            if (mavenHome != null) {
                System.setProperty(MAVEN_HOME, new File(mavenHome).getAbsolutePath());
            }

            if (config.getBaseDirectory() == null) {
                config.setBaseDirectory(new File(System.getProperty("user.dir")));
            }

            StreamSet streams = config.getStreams();
            if (streams == null) {
                streams = StreamSet.system();
            }
            config.setStreams(streams);

            // Configure logging
            this.loggerManager = config.getLogger();
            if (loggerManager == null) {
                loggerManager = new ConsoleLoggerManager();
                config.setLogger(loggerManager);
            }
            this.plexusLogger = loggerManager.getLoggerForComponent(MVNSH);

            int level = MavenExecutionRequest.LOGGING_LEVEL_INFO;
            if (config.isDebug()) {
                level = MavenExecutionRequest.LOGGING_LEVEL_DEBUG;
            }
            else if (config.isQuiet()) {
                level = MavenExecutionRequest.LOGGING_LEVEL_ERROR;
            }
            loggerManager.setThreshold(level);

            File logFile = config.getLogFile();
            if (logFile != null) {
//                logFile = resolveFile(logFile, config.getBaseDirectory());
//
//                try {
//                    logStream = new PrintStream(logFile);
//                    logger.setStream(logStream);
//                }
//                catch (FileNotFoundException e) {
//                    log.warn("Failed to open logging stream for file: " + logFile, e);
//                    logger.setStream(streams.out);
//                }
            }

            // Setup the container
            this.container = createContainer();
            log.debug("Using container: {}", container);

            Thread.currentThread().setContextClassLoader(container.getContainerRealm());
        }

        private DefaultPlexusContainer createContainer() throws Exception {
            ContainerConfiguration cc = new DefaultContainerConfiguration()
                .setClassWorld(config.getClassWorld())
                .setName("maven");

            // NOTE: This causes wiring failures for jline.Terminal, investigate further
            //.setAutoWiring(true)
            //.setClassPathScanning(PlexusConstants.SCANNING_CACHE);

            DefaultPlexusContainer c = new DefaultPlexusContainer(cc);
            configureContainer(c);

            return c;
        }

        protected void configureContainer(final DefaultPlexusContainer c) throws Exception {
            assert c != null;

            c.setLookupRealm(null);
            c.setLoggerManager(config.getLogger());

            // If there is a configuration delegate then call it
            if (config.getDelegate() != null) {
                config.getDelegate().configure(c);
            }
        }

        public MavenExecutionRequest create() throws Exception {
            MavenExecutionRequest request = new DefaultMavenExecutionRequest();
            request.setCacheNotFound(true);
            request.setCacheTransferError(false);
            configureSettings(request);
            return request;
        }

        public int execute(final MavenExecutionRequest request) throws Exception {
            assert request != null;

            if (log.isDebugEnabled()) {
                log.debug("Processing request: {}", Yarn.render(request, Yarn.Style.MULTI));
            }
            configureRequest(request);

            try {
                return doExecute(request);
            }
            catch (Exception e) {
                CLIReportingUtils.showError(log, "Error executing Maven.", e, request.isShowErrors()); // TODO: i81n
                return 1;
            }
            finally {
                cleanup();
                Closer.close(logStream);
            }
        }

        private void configureSettings(final MavenExecutionRequest request) throws Exception {
            assert request != null;
            assert config != null;

            File userSettingsFile = config.getSettingsFile();
            if (userSettingsFile != null) {
                userSettingsFile = resolveFile(userSettingsFile, config.getBaseDirectory());
                if (!userSettingsFile.isFile()) {
                    throw new FileNotFoundException("The specified user settings file does not exist: " + userSettingsFile); // TODO: i18n
                }
            }
            else {
                userSettingsFile = DEFAULT_USER_SETTINGS_FILE;
            }

            plexusLogger.debug("Reading user settings from: " + userSettingsFile);
            request.setUserSettingsFile(userSettingsFile);

            File globalSettingsFile = config.getGlobalSettingsFile();
            if (globalSettingsFile != null) {
                globalSettingsFile = resolveFile(globalSettingsFile, config.getBaseDirectory());
                if (!globalSettingsFile.isFile()) {
                    throw new FileNotFoundException("The specified global settings file does not exist: " + globalSettingsFile); // TODO: i18n
                }
            }
            else {
                globalSettingsFile = DEFAULT_GLOBAL_SETTINGS_FILE;
            }

            plexusLogger.debug("Reading global settings from: " + globalSettingsFile);
            request.setGlobalSettingsFile(globalSettingsFile);

            configureProperties(request);

            SettingsBuildingRequest settingsRequest = new DefaultSettingsBuildingRequest()
                .setGlobalSettingsFile(globalSettingsFile)
                .setUserSettingsFile(userSettingsFile)
                .setSystemProperties(request.getSystemProperties())
                .setUserProperties(request.getUserProperties());

            SettingsBuildingResult settingsResult;
            SettingsBuilder settingsBuilder = container.lookup(SettingsBuilder.class);
            try {
                settingsResult = settingsBuilder.build(settingsRequest);
            }
            finally {
                container.release(settingsBuilder);
            }

            // NOTE: This will nuke some details from the request; profiles, online, etc... :-(
            MavenExecutionRequestPopulator populator = container.lookup(MavenExecutionRequestPopulator.class);
            try {
                populator.populateFromSettings(request, settingsResult.getEffectiveSettings());
            }
            finally {
                container.release(populator);
            }

            if (!settingsResult.getProblems().isEmpty() && plexusLogger.isWarnEnabled()) {
                plexusLogger.warn("");
                plexusLogger.warn("Some problems were encountered while building the effective settings"); // TODO: i18n

                for (SettingsProblem problem : settingsResult.getProblems()) {
                    plexusLogger.warn(problem.getMessage() + " @ " + problem.getLocation()); // TODO: i18n
                }

                plexusLogger.warn("");
            }
        }

        private void configureProperties(final MavenExecutionRequest request) {
            assert request != null;
            assert config != null;

            Properties sys = new Properties();
            sys.putAll(System.getProperties());

            Properties user = new Properties();
            user.putAll(config.getProperties());

            // Add the env vars to the property set, with the "env." prefix
            boolean caseSensitive = !Os.isFamily(Os.FAMILY_WINDOWS);
            for (Map.Entry<String, String> entry : System.getenv().entrySet()) {
                String key = "env." + (caseSensitive ? entry.getKey() : entry.getKey().toUpperCase(Locale.ENGLISH));
                sys.setProperty(key, entry.getValue());
            }

            request.setUserProperties(user);

            // HACK: Some bits of Maven still require using System.properties :-(
            sys.putAll(user);
            System.getProperties().putAll(user);

            request.setSystemProperties(sys);
        }

        private void configureRequest(final MavenExecutionRequest request) throws Exception {
            assert request != null;
            assert config != null;

            File dir = new File(config.getBaseDirectory(), "").getAbsoluteFile();
            request.setBaseDirectory(dir);

            // HACK: Some bits need user.dir to be set, or use un-rooted File's :-(
            System.setProperty("user.dir", dir.getAbsolutePath());

            // Configure profiles
            for (String profile : config.getProfiles()) {
                profile = profile.trim();

                if (profile.startsWith("-") || profile.startsWith("!")) {
                    request.addInactiveProfile(profile.substring(1));
                }
                else if (profile.startsWith("+")) {
                    request.addActiveProfile(profile.substring(1));
                }
                else {
                    request.addActiveProfile(profile);
                }
            }

            // Configure user toolchains
            File userToolchainsFile = request.getUserToolchainsFile();
            if (userToolchainsFile != null) {
                userToolchainsFile = resolveFile(userToolchainsFile, config.getBaseDirectory());
            }
            else {
                userToolchainsFile = DEFAULT_USER_TOOLCHAINS_FILE;
            }
            request.setUserToolchainsFile(userToolchainsFile);

            // Configure the pom
            File alternatePomFile = config.getPomFile();
            if (alternatePomFile != null) {
                request.setPom(resolveFile(alternatePomFile, config.getBaseDirectory()));
            }
            else if (request.getPom() != null && !request.getPom().isAbsolute()) {
                request.setPom(request.getPom().getAbsoluteFile());
            }

            if ((request.getPom() != null) && (request.getPom().getParentFile() != null)) {
                request.setBaseDirectory(request.getPom().getParentFile());
            }
            else if (request.getPom() == null && request.getBaseDirectory() != null) {
                ModelProcessor modelProcessor = container.lookup(ModelProcessor.class);
                try {
                    File pom = modelProcessor.locatePom(new File(request.getBaseDirectory()));
                    if (pom.isFile()) {
                        request.setPom(pom);
                    }
                }
                finally {
                    container.release(modelProcessor);
                }
            }

            // Configure the local repo path
            String localRepoPath = request.getUserProperties().getProperty(LOCAL_REPO);
            if (localRepoPath == null) {
                localRepoPath = request.getSystemProperties().getProperty(LOCAL_REPO);
            }
            if (localRepoPath != null) {
                request.setLocalRepositoryPath(localRepoPath);
            }

            // Setup the xfr listener
            TransferListener transferListener;
            if (request.isInteractiveMode()) {
                transferListener = new ConsoleMavenTransferListener(config.getStreams().out);
            }
            else {
                transferListener = new BatchModeMavenTransferListener(config.getStreams().out);
            }
            request.setTransferListener(transferListener);

            // Configure request logging
            request.setLoggingLevel(plexusLogger.getThreshold());
            request.setExecutionListener(new ExecutionEventLogger(terminal.get(), plexusLogger));
        }

        private int doExecute(final MavenExecutionRequest request) throws Exception {
            assert request != null;
            assert config != null;

            if (config.isDebug() || config.isShowVersion()) {
                plexusLogger.info(CLIReportingUtils.showVersion());
            }

            //
            // TODO: i18n all of this
            //

            if (request.isShowErrors()) {
            	plexusLogger.info("Error stack-traces are turned on.");
            }
            if (MavenExecutionRequest.CHECKSUM_POLICY_WARN.equals(request.getGlobalChecksumPolicy())) {
            	plexusLogger.info("Disabling strict checksum verification on all artifact downloads.");
            }
            else if (MavenExecutionRequest.CHECKSUM_POLICY_FAIL.equals(request.getGlobalChecksumPolicy())) {
            	plexusLogger.info("Enabling strict checksum verification on all artifact downloads.");
            }

            if (log.isDebugEnabled()) {
                log.debug("Executing request: {}", Yarn.render(request, Yarn.Style.MULTI));
            }

            // FIXME: Hook up EventSpy support

            MavenExecutionResult result;
            Maven maven = container.lookup(Maven.class);
            try {
                result = maven.execute(request);
            }
            finally {
                container.release(maven);
            }

            if (!result.hasExceptions()) {
                return 0;
            }
            // else process exceptions

            ExceptionHandler handler = new DefaultExceptionHandler();
            Map<String, String> references = new LinkedHashMap<String, String>();
            MavenProject project = null;

            for (Throwable exception : result.getExceptions()) {
                ExceptionSummary summary = handler.handleException(exception);

                logSummary(summary, references, "", request.isShowErrors());

                if (project == null && exception instanceof LifecycleExecutionException) {
                    project = ((LifecycleExecutionException) exception).getProject();
                }
            }

            plexusLogger.error("");

            if (!request.isShowErrors()) {
            	plexusLogger.error("To see the full stack-trace of the errors, re-run Maven with the -e switch.");
            }
            if (!plexusLogger.isDebugEnabled()) {
            	plexusLogger.error("Re-run Maven using the -X switch to enable full debug logging.");
            }

            if (!references.isEmpty()) {
            	plexusLogger.error("");
                plexusLogger.error("For more information about the errors and possible solutions, please read the following articles:");

                for (Map.Entry<String, String> entry : references.entrySet()) {
                	plexusLogger.error(entry.getValue() + " " + entry.getKey());
                }
            }

            if (project != null && !project.equals(result.getTopologicallySortedProjects().get(0))) {
            	plexusLogger.error("");
            	plexusLogger.error("After correcting the problems, you can resume the build with the command");
            	plexusLogger.error("  mvn <goals> -rf :" + project.getArtifactId());
            }

            if (MavenExecutionRequest.REACTOR_FAIL_NEVER.equals(request.getReactorFailureBehavior())) {
            	plexusLogger.info("Build failures were ignored.");
                return 0;
            }
            else {
                return 1;
            }
        }

        private void logSummary(final ExceptionSummary summary, final Map<String, String> references, String indent, final boolean showErrors) {
            assert summary != null;

            String referenceKey = "";

            // TODO: i18n

            if (StringUtils.isNotEmpty(summary.getReference())) {
                referenceKey = references.get(summary.getReference());
                if (referenceKey == null) {
                    referenceKey = "[Help " + (references.size() + 1) + "]";
                    references.put(summary.getReference(), referenceKey);
                }
            }

            String msg = indent + summary.getMessage();

            if (StringUtils.isNotEmpty(referenceKey)) {
                if (msg.indexOf('\n') < 0) {
                    msg += " -> " + referenceKey;
                }
                else {
                    msg += '\n' + indent + "-> " + referenceKey;
                }
            }

            if (showErrors) {
                //noinspection ThrowableResultOfMethodCallIgnored
            	plexusLogger.error(msg, summary.getException());
            }
            else {
            	plexusLogger.error(msg);
            }

            indent += "  ";

            for (ExceptionSummary child : summary.getChildren()) {
                logSummary(child, references, indent, showErrors);
            }
        }

        private void cleanup() {
            ClassWorld world = container.getClassWorld();
            log.debug("Removing all realms from: {}", world);

            //noinspection unchecked
            for (ClassRealm realm : (List<ClassRealm>)world.getRealms()) {
                String id = realm.getId();
                try {
                    log.debug("Disposing class realm: {}", id);
                    world.disposeRealm(id);
                }
                catch (Exception e) {
                    log.warn("Failed to dispose class realm: {}", id, e);
                }
            }

            //noinspection unchecked
            purgeStrayShutdownHooks(world.getRealms());

            container.dispose();
        }

        // TODO: May actually want to snapshot current hooks before executing mvn, then remove all that leaked in after execution when we clean up

        private void purgeStrayShutdownHooks(final Collection<? extends ClassLoader> loaders) {
            // CommandLineUtils from plexus-utils registers a (needless) shutdown hook which in turn causes a mem leak. As
            // counter measure, we inspect all created (plugin) class loaders and try to unregister the hook.
            final String[] CLASSES = {
                "org.codehaus.plexus.util.cli.CommandLineUtils",
                "org.apache.maven.surefire.booter.shade.org.codehaus.plexus.util.cli.CommandLineUtils"
            };

            for (ClassLoader loader : loaders) {
                for (String className : CLASSES) {
                    String resName = className.replace('.', '/') + ".class";
                    if (loader.getResource(resName) != null) {
                        try {
                            Class<?> type = loader.loadClass(className);
                            Method method = type.getMethod("removeShutdownHook", Boolean.TYPE);
                            log.debug("Invoking: {}", method);
                            method.invoke(null, Boolean.TRUE);
                        }
                        catch (Exception e) {
                            // to be expected for plexus-utils 1.5.12-
                        }
                    }
                }
            }

            // The above block only works for recent plexus-utils versions, the following block is a fallback attempt to remove hooks directly from the JRE.
            try {
                Class<?> shutdown = ClassLoader.getSystemClassLoader().loadClass("java.lang.ApplicationShutdownHooks");
                Field field = shutdown.getDeclaredField("hooks");
                field.setAccessible(true);

                @SuppressWarnings("unchecked")
                Collection<Thread> hooks = new ArrayList<Thread>(((Map<Thread, ?>) field.get(null)).keySet());

                Runtime rt = Runtime.getRuntime();
                for (Thread hook : hooks) {
                    String name = hook.getClass().getName();
                    log.debug("Inspecting hook: {}", name);
                    if (name.contains("CommandLineUtils$") || name.equals("jline.TerminalSupport$RestoreHook")) {
                        rt.removeShutdownHook(hook);
                        log.debug("Removed shutdown hook: {} - {}", name, hook);
                    }
                }
            }
            catch (Exception e) {
                // to be expected on jre != 1.6
            }
        }
    }
}