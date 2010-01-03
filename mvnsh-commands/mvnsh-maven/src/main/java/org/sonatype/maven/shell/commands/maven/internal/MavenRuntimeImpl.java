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

package org.sonatype.maven.shell.commands.maven.internal;

import com.google.inject.Inject;
import org.apache.maven.Maven;
import org.apache.maven.cli.CLIReportingUtils;
import org.apache.maven.cli.PrintStreamLogger;
import org.apache.maven.exception.DefaultExceptionHandler;
import org.apache.maven.exception.ExceptionHandler;
import org.apache.maven.exception.ExceptionSummary;
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
import org.codehaus.plexus.util.Os;
import org.codehaus.plexus.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonatype.gshell.io.StreamSet;
import org.sonatype.gshell.plexus.PlexusRuntime;
import org.sonatype.maven.shell.commands.maven.MavenRuntime;
//import org.sonatype.plexus.components.cipher.DefaultPlexusCipher;
//import org.sonatype.plexus.components.sec.dispatcher.DefaultSecDispatcher;
//import org.sonatype.plexus.components.sec.dispatcher.SecDispatcher;
//import org.sonatype.plexus.components.sec.dispatcher.SecUtil;
//import org.sonatype.plexus.components.sec.dispatcher.model.SettingsSecurity;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;

/**
 * {@link MavenRuntime} implementation.
 *
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @since 0.9
 */
public class MavenRuntimeImpl
    implements MavenRuntime
{
    private static final Logger log = LoggerFactory.getLogger(MavenRuntimeImpl.class);

    public static final String LOCAL_REPO_PROPERTY = "maven.repo.local";

    public static final String USER_HOME = System.getProperty("user.home");

    public static final File USER_MAVEN_CONF_HOME = new File(USER_HOME, ".m2");

    public static final File DEFAULT_USER_SETTINGS_FILE = new File(USER_MAVEN_CONF_HOME, "settings.xml");

    public static final File DEFAULT_GLOBAL_SETTINGS_FILE = new File(System.getProperty("maven.home", System.getProperty("user.dir", "")), "conf/settings.xml");

    public static final File DEFAULT_USER_TOOLCHAINS_FILE = new File(USER_MAVEN_CONF_HOME, "toolchains.xml");

    private final PlexusRuntime plexus;

    @Inject
    public MavenRuntimeImpl(final PlexusRuntime plexus) {
        assert plexus != null;
        this.plexus = plexus;
    }

    public Request create() {
        Request request = new Request();
        request.setClassWorld(plexus.getClassWorld());
        return request;
    }

    public String getVersion() {
        ByteArrayOutputStream buff = new ByteArrayOutputStream();
        PrintStream out = new PrintStream(buff);
        CLIReportingUtils.showVersion(out);
        out.close();
        return new String(buff.toByteArray());
    }

    public Result execute(final Request request) throws Exception {
        assert request != null;

        log.debug("Executing request: {}", request);

        setupDefaults(request);
        setupLogging(request);

        if (request.isDebug() || request.isShowVersion()) {
            CLIReportingUtils.showVersion(request.getStreams().out);
        }

        // Show some details/warnings
        if (request.isShowErrors()) {
            log.info("Error stack-traces are turned on.");
        }
        if (MavenExecutionRequest.CHECKSUM_POLICY_WARN.equals(request.getRequest().getGlobalChecksumPolicy())) {
            log.info("Disabling strict checksum verification on all artifact downloads.");
        }
        else if (MavenExecutionRequest.CHECKSUM_POLICY_FAIL.equals(request.getRequest().getGlobalChecksumPolicy())) {
            log.info("Enabling strict checksum verification on all artifact downloads.");
        }

        configureSettings(request);
        configureRequest(request);

        Maven maven = plexus.lookup(Maven.class);
        MavenExecutionResult result = maven.execute(request.getRequest());

        if (result.hasExceptions()) {
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

            log.error("");

            if (!request.isShowErrors()) {
                log.error("To see the full stack trace of the errors, re-run Maven with the -e switch.");
            }
            if (!log.isDebugEnabled()) {
                log.error("Re-run Maven using the -X switch to enable full debug logging.");
            }

            if (!references.isEmpty()) {
                log.error("");
                log.error("For more information about the errors and possible solutions, please read the following articles:");

                for (Map.Entry<String, String> entry : references.entrySet()) {
                    log.error(entry.getValue() + " " + entry.getKey());
                }
            }

            if (project != null && !project.equals(result.getTopologicallySortedProjects().get(0))) {
                log.error("");
                log.error("After correcting the problems, you can resume the build with the command");
                log.error("  mvn <goals> -rf :{}", project.getArtifactId());
            }

            if (MavenExecutionRequest.REACTOR_FAIL_NEVER.equals(request.getRequest().getReactorFailureBehavior())) {
                log.info("Build failures were ignored.");

                return new Result(0);
            }
            else {
                return new Result(1);
            }
        }
        else {
            return new Result(0);
        }
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

    private void setupDefaults(final Request request) {
        assert request != null;

        if (request.getClassWorld() == null) {
            request.setClassWorld(plexus.getClassWorld());
        }

        StreamSet streams = request.getStreams();
        if (streams == null) {
            streams = StreamSet.system();
        }
        request.setStreams(streams);

        PrintStreamLogger logger = request.getLogger();
        if (logger == null) {
            request.setLogger(new PrintStreamLogger(streams.out));
        }
        else {
            logger.setStream(streams.out);
        }

        if (request.getWorkingDirectory() == null) {
            request.setWorkingDirectory(new File(System.getProperty("user.dir")));
        }

        // Make sure the Maven home directory is an absolute path to save us from confusion with say drive-relative Windows paths.
        String mavenHome = System.getProperty("maven.home");

        if (mavenHome != null) {
            System.setProperty("maven.home", new File(mavenHome).getAbsolutePath());
        }
    }

    private void setupLogging(final Request request) {
        assert request != null;

        int level = MavenExecutionRequest.LOGGING_LEVEL_INFO;
        if (request.isDebug()) {
            level = MavenExecutionRequest.LOGGING_LEVEL_DEBUG;
        }
        else if (request.isQuiet()) {
            level = MavenExecutionRequest.LOGGING_LEVEL_ERROR;
        }
        request.getRequest().setLoggingLevel(level);
        request.getLogger().setThreshold(level);

        StreamSet streams = request.getStreams();
        PrintStreamLogger logger = request.getLogger();

        File logFile = request.getLogFile();
        if (logFile != null) {
            logFile = resolveFile(logFile, request.getWorkingDirectory());

            try {
                PrintStream out = new PrintStream(logFile);
                request.setFileStream(out);
                logger.setStream(out);
            }
            catch (FileNotFoundException e) {
                streams.err.println(e);
                logger.setStream(streams.out);
            }
        }
        else {
            logger.setStream(request.getStreams().out);
        }

        request.getRequest().setExecutionListener(new ExecutionEventLogger(logger));
    }

    private void configureSettings(final Request request) throws Exception {
        assert request != null;

        File userSettingsFile = request.getSettings();

        if (userSettingsFile != null) {
            userSettingsFile = resolveFile(userSettingsFile, request.getWorkingDirectory());
            if (!userSettingsFile.isFile()) {
                throw new FileNotFoundException("The specified user settings file does not exist: " + userSettingsFile);
            }
        }
        else {
            userSettingsFile = DEFAULT_USER_SETTINGS_FILE;
        }

        log.debug("Reading user settings from: {}", userSettingsFile);

        File globalSettingsFile = request.getGlobalSettings();

        if (globalSettingsFile != null) {
            globalSettingsFile = resolveFile(globalSettingsFile, request.getWorkingDirectory());
            if (!globalSettingsFile.isFile()) {
                throw new FileNotFoundException("The specified global settings file does not exist: " + globalSettingsFile);
            }
        }
        else {
            globalSettingsFile = DEFAULT_GLOBAL_SETTINGS_FILE;
        }

        log.debug("Reading global settings from: {}", globalSettingsFile);

        request.getRequest().setGlobalSettingsFile(globalSettingsFile);
        request.getRequest().setUserSettingsFile(userSettingsFile);

        configureProperties(request);

        SettingsBuildingRequest settingsRequest = new DefaultSettingsBuildingRequest();
        settingsRequest.setGlobalSettingsFile(globalSettingsFile);
        settingsRequest.setUserSettingsFile(userSettingsFile);
        settingsRequest.setSystemProperties(request.getRequest().getSystemProperties());
        settingsRequest.setUserProperties(request.getRequest().getUserProperties());

        SettingsBuilder settingsBuilder = plexus.lookup(SettingsBuilder.class);
        SettingsBuildingResult settingsResult = settingsBuilder.build(settingsRequest);

        MavenExecutionRequestPopulator populator = plexus.lookup(MavenExecutionRequestPopulator.class);
        populator.populateFromSettings(request.getRequest(), settingsResult.getEffectiveSettings());

        if (!settingsResult.getProblems().isEmpty() && log.isWarnEnabled()) {
            log.warn("");
            log.warn("Some problems were encountered while building the effective settings");

            for (SettingsProblem problem : settingsResult.getProblems()) {
                log.warn("{} @ {}", problem.getMessage(), problem.getLocation());
            }

            log.warn("");
        }
    }

    private void configureProperties(final Request request) {
        assert request != null;

        Properties systemProperties = new Properties();
        systemProperties.putAll(System.getProperties());

        Properties userProperties = new Properties();
        // NOTE: Not setting to System here, this may or may not cause problems, as mvn3 does set user props as system
        userProperties.putAll(request.getProperties());

        // add the env vars to the property set, with the "env." prefix
        boolean caseSensitive = !Os.isFamily(Os.FAMILY_WINDOWS);
        for (Map.Entry<String, String> entry : System.getenv().entrySet()) {
            String key = "env." + (caseSensitive ? entry.getKey() : entry.getKey().toUpperCase(Locale.ENGLISH));
            systemProperties.setProperty(key, entry.getValue());
        }

        request.getRequest().setUserProperties(userProperties);
        request.getRequest().setSystemProperties(systemProperties);
    }

    private void configureRequest(final Request request) throws Exception {
        assert request != null;

        MavenExecutionRequest req = request.getRequest();

        // ----------------------------------------------------------------------
        // Now that we have everything that we need we will fire up plexus and
        // bring the maven component to life for use.
        // ----------------------------------------------------------------------

        if (request.isBatch()) {
            req.setInteractiveMode(false);
        }

        boolean pluginUpdateOverride = false;

        if (request.isCheckPluginUpdates() || request.isUpdatePlugins()) {
            pluginUpdateOverride = true;
        }
        else if (request.isNoPluginUpdates()) {
            pluginUpdateOverride = false;
        }

        boolean noSnapshotUpdates = false;
        if (request.isNoSnapshotUpdates()) {
            noSnapshotUpdates = true;
        }

        // ----------------------------------------------------------------------
        //
        // ----------------------------------------------------------------------

        List<String> goals = request.getGoals();

        boolean recursive = true;
        if (request.isNonRecursive()) {
            recursive = false;
        }

        String reactorFailureBehaviour = MavenExecutionRequest.REACTOR_FAIL_FAST;
        if (request.isFailFast()) {
            reactorFailureBehaviour = MavenExecutionRequest.REACTOR_FAIL_FAST;
        }
        else if (request.isFailAtEnd()) {
            reactorFailureBehaviour = MavenExecutionRequest.REACTOR_FAIL_AT_END;
        }
        else if (request.isFailNever()) {
            reactorFailureBehaviour = MavenExecutionRequest.REACTOR_FAIL_NEVER;
        }

        if (request.isOffline()) {
            req.setOffline(true);
        }

        boolean updateSnapshots = false;
        if (request.isUpdateSnapshots()) {
            updateSnapshots = true;
        }

        String globalChecksumPolicy = null;
        if (request.isStrictChecksums()) {
            globalChecksumPolicy = MavenExecutionRequest.CHECKSUM_POLICY_FAIL;
        }
        else if (request.isLaxChecksums()) {
            globalChecksumPolicy = MavenExecutionRequest.CHECKSUM_POLICY_WARN;
        }

        File baseDirectory = new File(request.getWorkingDirectory(), "").getAbsoluteFile();

        // ----------------------------------------------------------------------
        // Profile Activation
        // ----------------------------------------------------------------------

        List<String> activeProfiles = new ArrayList<String>();

        List<String> inactiveProfiles = new ArrayList<String>();

        if (request.getActivateProfiles() != null) {
            // TODO: Check if gshell cli actually handles this for us or not
            String[] profileOptionValues = request.getActivateProfiles().split(",");
            if (profileOptionValues != null) {
                for (String profileOptionValue : profileOptionValues) {
                    StringTokenizer profileTokens = new StringTokenizer(profileOptionValue, ",");

                    while (profileTokens.hasMoreTokens()) {
                        String profileAction = profileTokens.nextToken().trim();

                        if (profileAction.startsWith("-") || profileAction.startsWith("!")) {
                            inactiveProfiles.add(profileAction.substring(1));
                        }
                        else if (profileAction.startsWith("+")) {
                            activeProfiles.add(profileAction.substring(1));
                        }
                        else {
                            activeProfiles.add(profileAction);
                        }
                    }
                }
            }
        }

        ArtifactTransferListener transferListener;
        if (req.isInteractiveMode()) {
            transferListener = new ConsoleMavenTransferListener(request.getStreams().out);
        }
        else {
            transferListener = new BatchModeMavenTransferListener(request.getStreams().out);
        }
        transferListener.setShowChecksumEvents(false);

        int loggingLevel;
        if (request.isDebug()) {
            loggingLevel = MavenExecutionRequest.LOGGING_LEVEL_DEBUG;
        }
        else if (request.isQuiet()) {
            loggingLevel = MavenExecutionRequest.LOGGING_LEVEL_ERROR;
        }
        else {
            loggingLevel = MavenExecutionRequest.LOGGING_LEVEL_INFO;
        }

        File userToolchainsFile = request.getToolChains();
        if (userToolchainsFile != null) {
            userToolchainsFile = resolveFile(userToolchainsFile, request.getWorkingDirectory());
        }
        else {
            userToolchainsFile = DEFAULT_USER_TOOLCHAINS_FILE;
        }

        req.setBaseDirectory(baseDirectory)
            .setGoals(goals)
            .setReactorFailureBehavior(reactorFailureBehaviour) // default: fail fast
            .setRecursive(recursive) // default: true
            .setShowErrors(request.isShowErrors()) // default: false
            .setUsePluginUpdateOverride(pluginUpdateOverride)
            .addActiveProfiles(activeProfiles) // optional
            .addInactiveProfiles(inactiveProfiles) // optional
            .setLoggingLevel(loggingLevel) // default: info
            .setTransferListener(transferListener) // default: batch mode which goes along with interactive
            .setUpdateSnapshots(updateSnapshots) // default: false
            .setNoSnapshotUpdates(noSnapshotUpdates) // default: false
            .setGlobalChecksumPolicy(globalChecksumPolicy) // default: warn
            .setUserToolchainsFile(userToolchainsFile);

        File alternatePomFile = request.getFile();

        if (alternatePomFile != null) {
            req.setPom(resolveFile(alternatePomFile, request.getWorkingDirectory()));
        }
        else if (req.getPom() != null && !req.getPom().isAbsolute()) {
            req.setPom(req.getPom().getAbsoluteFile());
        }

        if ((req.getPom() != null) && (req.getPom().getParentFile() != null)) {
            req.setBaseDirectory(req.getPom().getParentFile());
        }
        else if ((req.getPom() == null) && (req.getBaseDirectory() != null)) {
            ModelProcessor modelProcessor = plexus.lookup(ModelProcessor.class);
            File pom = modelProcessor.locatePom(new File(req.getBaseDirectory()));
            req.setPom(pom);
        }
        else if (req.getBaseDirectory() == null) {
            req.setBaseDirectory(request.getWorkingDirectory());
        }

        req.setResumeFrom(request.getResumeFrom());

        if (request.getProjects() != null) {
            String projectList = request.getProjects();
            String[] projects = StringUtils.split(projectList, ",");
            req.setSelectedProjects(Arrays.asList(projects));
        }

        if (request.isAlsoMake() && !request.isAlsoMakeDependents()) {
            req.setMakeBehavior(MavenExecutionRequest.REACTOR_MAKE_UPSTREAM);
        }
        else if (!request.isAlsoMake() && request.isAlsoMakeDependents()) {
            req.setMakeBehavior(MavenExecutionRequest.REACTOR_MAKE_DOWNSTREAM);
        }
        else if (request.isAlsoMake() && request.isAlsoMakeDependents()) {
            req.setMakeBehavior(MavenExecutionRequest.REACTOR_MAKE_BOTH);
        }

        String localRepoProperty = req.getUserProperties().getProperty(LOCAL_REPO_PROPERTY);

        if (localRepoProperty == null) {
            localRepoProperty = req.getSystemProperties().getProperty(LOCAL_REPO_PROPERTY);
        }

        if (localRepoProperty != null) {
            req.setLocalRepositoryPath(localRepoProperty);
        }
    }

//    //
//    // TODO: Move this to a separate command, no need to have it included here
//    //
//
//    private void encryption(final Request request) throws Exception {
//        assert request != null;
//
//        DefaultSecDispatcher dispatcher = (DefaultSecDispatcher) plexus.lookup(SecDispatcher.class);
//
//        String passwd = request.getEncryptMasterPassword();
//        if (passwd != null) {
//            DefaultPlexusCipher cipher = new DefaultPlexusCipher();
//
//            request.getStreams().out.println(cipher.encryptAndDecorate(passwd, DefaultSecDispatcher.SYSTEM_PROPERTY_SEC_LOCATION));
//
//            throw new ExitNotification(0);
//        }
//
//        passwd = request.getEncryptPassword();
//        if (passwd != null) {
//            String configurationFile = dispatcher.getConfigurationFile();
//
//            if (configurationFile.startsWith("~")) {
//                configurationFile = System.getProperty("user.home") + configurationFile.substring(1);
//            }
//
//            String file = System.getProperty(DefaultSecDispatcher.SYSTEM_PROPERTY_SEC_LOCATION, configurationFile);
//
//            String master = null;
//
//            SettingsSecurity sec = SecUtil.read(file, true);
//            if (sec != null) {
//                master = sec.getMaster();
//            }
//
//            if (master == null) {
//                throw new IllegalStateException("Master password is not set in the setting security file: " + file);
//            }
//
//            DefaultPlexusCipher cipher = new DefaultPlexusCipher();
//            String masterPasswd = cipher.decryptDecorated(master, DefaultSecDispatcher.SYSTEM_PROPERTY_SEC_LOCATION);
//            request.getStreams().out.println(cipher.encryptAndDecorate(passwd, masterPasswd));
//
//            throw new ExitNotification(0);
//        }
//    }

    private void logSummary(ExceptionSummary summary, Map<String, String> references, String indent, boolean showErrors) {
        assert summary != null;
        
        String referenceKey = "";

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
            log.error(msg, summary.getException());
        }
        else {
            log.error(msg);
        }

        indent += "  ";

        for (ExceptionSummary child : summary.getChildren()) {
            logSummary(child, references, indent, showErrors);
        }
    }
}