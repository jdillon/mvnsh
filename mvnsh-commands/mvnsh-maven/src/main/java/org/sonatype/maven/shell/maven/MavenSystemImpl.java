/*
 * Copyright (C) 2010 the original author or authors.
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

package org.sonatype.maven.shell.maven;

import com.google.inject.Inject;
import org.apache.maven.Maven;
import org.apache.maven.cli.CLIReportingUtils;
import org.apache.maven.cli.MavenLoggerManager;
import org.apache.maven.cli.PrintStreamLogger;
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
import org.apache.maven.repository.ArtifactTransferListener;
import org.apache.maven.settings.building.DefaultSettingsBuildingRequest;
import org.apache.maven.settings.building.SettingsBuilder;
import org.apache.maven.settings.building.SettingsBuildingRequest;
import org.apache.maven.settings.building.SettingsBuildingResult;
import org.apache.maven.settings.building.SettingsProblem;
import org.codehaus.plexus.ContainerConfiguration;
import org.codehaus.plexus.DefaultContainerConfiguration;
import org.codehaus.plexus.DefaultPlexusContainer;
import org.codehaus.plexus.util.Os;
import org.codehaus.plexus.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonatype.gshell.io.Closer;
import org.sonatype.gshell.io.StreamSet;
import org.sonatype.gshell.plexus.PlexusRuntime;
import org.sonatype.gshell.util.yarn.Yarn;
import org.sonatype.maven.shell.maven.internal.BatchModeMavenTransferListener;
import org.sonatype.maven.shell.maven.internal.ConsoleMavenTransferListener;
import org.sonatype.maven.shell.maven.internal.ExecutionEventLogger;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;

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

    private final PlexusRuntime plexus;

    @Inject
    public MavenSystemImpl(final PlexusRuntime plexus) {
        assert plexus != null;
        this.plexus = plexus;
    }

    public String getVersion() {
        ByteArrayOutputStream buff = new ByteArrayOutputStream();
        PrintStream out = new PrintStream(buff);
        CLIReportingUtils.showVersion(out);
        Closer.close(out);
        return new String(buff.toByteArray());
    }

    public MavenRuntime create(final MavenRuntimeConfiguration config) throws Exception {
        assert config != null;

        log.debug("Creating runtime w/config: {}", Yarn.render(config, Yarn.Style.MULTI));

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

    private class MavenRuntimeImpl
        implements MavenRuntime
    {
        private final Logger log = LoggerFactory.getLogger(MavenRuntimeImpl.class);

        private final MavenRuntimeConfiguration config;

        private PrintStreamLogger logger;

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

            // Setup defaults
            if (config.getClassWorld() == null) {
                config.setClassWorld(plexus.getClassWorld());
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
            this.logger = config.getLogger();
            if (logger == null) {
                logger = new PrintStreamLogger(streams.out);
            }
            else {
                logger.setStream(streams.out);
            }
            config.setLogger(logger);

            int level = MavenExecutionRequest.LOGGING_LEVEL_INFO;
            if (config.isDebug()) {
                level = MavenExecutionRequest.LOGGING_LEVEL_DEBUG;
            }
            else if (config.isQuiet()) {
                level = MavenExecutionRequest.LOGGING_LEVEL_ERROR;
            }
            logger.setThreshold(level);

            File logFile = config.getLogFile();
            if (logFile != null) {
                logFile = resolveFile(logFile, config.getBaseDirectory());

                try {
                    logStream = new PrintStream(logFile);
                    logger.setStream(logStream);
                }
                catch (FileNotFoundException e) {
                    log.warn("Failed to open logging stream for file: " + logFile, e);
                    logger.setStream(streams.out);
                }
            }

            // Setup the container
            this.container = createContainer();
        }

        private DefaultPlexusContainer createContainer() throws Exception {
            ContainerConfiguration cc = new DefaultContainerConfiguration()
                .setClassWorld(config.getClassWorld())
                .setName("maven");

            DefaultPlexusContainer c = new DefaultPlexusContainer(cc);
            c.setLoggerManager(new MavenLoggerManager(config.getLogger()));
            c.getLoggerManager().setThresholds(logger.getThreshold());

            return c;
        }

        public MavenExecutionRequest create() throws Exception {
            MavenExecutionRequest request = new DefaultMavenExecutionRequest();
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
                CLIReportingUtils.showError(logger, "Error executing Maven.", e, request.isShowErrors()); // TODO: i81n
                return 1;
            }
            finally {
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

            logger.debug("Reading user settings from: " + userSettingsFile);
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

            logger.debug("Reading global settings from: " + globalSettingsFile);
            request.setGlobalSettingsFile(globalSettingsFile);

            configureProperties(request);

            SettingsBuildingRequest settingsRequest = new DefaultSettingsBuildingRequest()
                .setGlobalSettingsFile(globalSettingsFile)
                .setUserSettingsFile(userSettingsFile)
                .setSystemProperties(request.getSystemProperties())
                .setUserProperties(request.getUserProperties());

            SettingsBuilder settingsBuilder = container.lookup(SettingsBuilder.class);
            SettingsBuildingResult settingsResult = settingsBuilder.build(settingsRequest);

            // NOTE: This will nuke some details from the request; profiles, online, etc... :-(
            MavenExecutionRequestPopulator populator = container.lookup(MavenExecutionRequestPopulator.class);
            populator.populateFromSettings(request, settingsResult.getEffectiveSettings());

            if (!settingsResult.getProblems().isEmpty() && logger.isWarnEnabled()) {
                logger.warn("");
                logger.warn("Some problems were encountered while building the effective settings"); // TODO: i18n

                for (SettingsProblem problem : settingsResult.getProblems()) {
                    logger.warn(problem.getMessage() + " @ " + problem.getLocation()); // TODO: i18n
                }

                logger.warn("");
            }
        }

        private void configureProperties(final MavenExecutionRequest request) {
            assert request != null;
            assert config != null;

            Properties sys = new Properties();
            sys.putAll(System.getProperties());

            Properties user = new Properties();
            user.putAll(config.getProperties());
            // NOTE: Not setting to System here, this may or may not cause problems, as mvn3 does set user props as system
            // System.getProperties().putAll(user);

            // Add the env vars to the property set, with the "env." prefix
            boolean caseSensitive = !Os.isFamily(Os.FAMILY_WINDOWS);
            for (Map.Entry<String, String> entry : System.getenv().entrySet()) {
                String key = "env." + (caseSensitive ? entry.getKey() : entry.getKey().toUpperCase(Locale.ENGLISH));
                sys.setProperty(key, entry.getValue());
            }

            request.setUserProperties(user);
            request.setSystemProperties(sys);
        }

        private void configureRequest(final MavenExecutionRequest request) throws Exception {
            assert request != null;
            assert config != null;

            request.setBaseDirectory(new File(request.getBaseDirectory(), "").getAbsoluteFile());

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
                File pom = modelProcessor.locatePom(new File(request.getBaseDirectory()));
                request.setPom(pom);
            }
            else if (request.getBaseDirectory() == null) {
                request.setBaseDirectory(config.getBaseDirectory());
            }

            // Configure the local repo path
            String localRepoPath = request.getUserProperties().getProperty(LOCAL_REPO_PROPERTY);
            if (localRepoPath == null) {
                localRepoPath = request.getSystemProperties().getProperty(LOCAL_REPO_PROPERTY);
            }
            if (localRepoPath != null) {
                request.setLocalRepositoryPath(localRepoPath);
            }

            // Setup the xfr listener
            ArtifactTransferListener transferListener;
            if (request.isInteractiveMode()) {
                transferListener = new ConsoleMavenTransferListener(config.getStreams().out);
            }
            else {
                transferListener = new BatchModeMavenTransferListener(config.getStreams().out);
            }
            transferListener.setShowChecksumEvents(false);
            request.setTransferListener(transferListener);

            // Configure request logging
            request.setLoggingLevel(logger.getThreshold());
            request.setExecutionListener(new ExecutionEventLogger(logger));
        }

        private int doExecute(final MavenExecutionRequest request) throws Exception {
            assert request != null;
            assert config != null;

            if (config.isDebug() || config.isShowVersion()) {
                CLIReportingUtils.showVersion(config.getStreams().out);
            }

            //
            // TODO: i18n all of this
            //

            if (request.isShowErrors()) {
                logger.info("Error stack-traces are turned on.");
            }
            if (MavenExecutionRequest.CHECKSUM_POLICY_WARN.equals(request.getGlobalChecksumPolicy())) {
                logger.info("Disabling strict checksum verification on all artifact downloads.");
            }
            else if (MavenExecutionRequest.CHECKSUM_POLICY_FAIL.equals(request.getGlobalChecksumPolicy())) {
                logger.info("Enabling strict checksum verification on all artifact downloads.");
            }

            if (log.isDebugEnabled()) {
                log.debug("Executing request: {}", Yarn.render(request, Yarn.Style.MULTI));
            }
            
            Maven maven = container.lookup(Maven.class);
            MavenExecutionResult result = maven.execute(request);

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

            logger.error("");

            if (!request.isShowErrors()) {
                logger.error("To see the full stack-trace of the errors, re-run Maven with the -e switch.");
            }
            if (!logger.isDebugEnabled()) {
                logger.error("Re-run Maven using the -X switch to enable full debug logging.");
            }

            if (!references.isEmpty()) {
                logger.error("");
                logger.error("For more information about the errors and possible solutions, please read the following articles:");

                for (Map.Entry<String, String> entry : references.entrySet()) {
                    logger.error(entry.getValue() + " " + entry.getKey());
                }
            }

            if (project != null && !project.equals(result.getTopologicallySortedProjects().get(0))) {
                logger.error("");
                logger.error("After correcting the problems, you can resume the build with the command");
                logger.error("  mvn <goals> -rf :" + project.getArtifactId());
            }

            if (MavenExecutionRequest.REACTOR_FAIL_NEVER.equals(request.getReactorFailureBehavior())) {
                logger.info("Build failures were ignored.");
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
                logger.error(msg, summary.getException());
            }
            else {
                logger.error(msg);
            }

            indent += "  ";

            for (ExceptionSummary child : summary.getChildren()) {
                logSummary(child, references, indent, showErrors);
            }
        }
    }
}