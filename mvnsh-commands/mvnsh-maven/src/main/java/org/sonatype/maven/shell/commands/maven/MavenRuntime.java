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

package org.sonatype.maven.shell.commands.maven;

import org.apache.maven.cli.PrintStreamLogger;
import org.apache.maven.execution.DefaultMavenExecutionRequest;
import org.apache.maven.execution.MavenExecutionRequest;
import org.codehaus.plexus.classworlds.ClassWorld;
import org.sonatype.gshell.io.StreamSet;
import org.sonatype.gshell.notification.Notification;

import java.io.File;
import java.io.PrintStream;
import java.util.List;
import java.util.Properties;

/**
 * Maven runtime.
 *
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @since 0.9
 */
public interface MavenRuntime
{
//    String LOCAL_REPO_PROPERTY = "maven.repo.local";
//
//    String USER_HOME = System.getProperty("user.home");
//
//    File USER_MAVEN_CONFIG_HOME = new File(USER_HOME, ".m2");
//
//    File DEFAULT_USER_SETTINGS_FILE = new File(USER_MAVEN_CONFIG_HOME, "settings.xml");
//
//    File DEFAULT_GLOBAL_SETTINGS_FILE = new File(System.getProperty("maven.home", System.getProperty("user.dir", "")), "conf/settings.xml");
//
//    File DEFAULT_USER_TOOLCHAINS_FILE = new File(USER_MAVEN_CONFIG_HOME, "toolchains.xml");

    Request create();

    String getVersion();

    Result execute(Request request) throws Exception;

    class Request
    {
        private StreamSet streams;

        private ClassWorld classWorld;

        private File workingDirectory;

        private PrintStreamLogger logger;

        private PrintStream fileStream;

        private MavenExecutionRequest request = new DefaultMavenExecutionRequest();

        public StreamSet getStreams() {
            return streams;
        }

        public Request setStreams(final StreamSet streams) {
            this.streams = streams;
            return this;
        }

        public ClassWorld getClassWorld() {
            return classWorld;
        }

        public Request setClassWorld(final ClassWorld classWorld) {
            this.classWorld = classWorld;
            return this;
        }

        public File getWorkingDirectory() {
            return workingDirectory;
        }

        public Request setWorkingDirectory(final File workingDirectory) {
            this.workingDirectory = workingDirectory;
            return this;
        }

        public PrintStreamLogger getLogger() {
            return logger;
        }

        public Request setLogger(final PrintStreamLogger logger) {
            this.logger = logger;
            return this;
        }

        public PrintStream getFileStream() {
            return fileStream;
        }

        public Request setFileStream(final PrintStream fileStream) {
            this.fileStream = fileStream;
            return this;
        }

        public MavenExecutionRequest getRequest() {
            return request;
        }

        public Request setRequest(final MavenExecutionRequest request) {
            this.request = request;
            return this;
        }


        //
        // NOTE: The following map to mvn command-line options
        //

        private File file;

        private Properties properties = new Properties();

        private boolean offline;

//        private boolean version;

        private boolean quiet;

        private boolean debug;

        private boolean showErrors;

        private boolean nonRecursive;

        private boolean updateSnapshots;

        private String activateProfiles;

        private boolean batch;

        private boolean checkPluginUpdates;

        private boolean updatePlugins;

        private boolean noPluginUpdates;

        private boolean noSnapshotUpdates;

        private boolean strictChecksums;

        private boolean laxChecksums;

        private File settings;

        private File globalSettings;

        private File toolChains;

        private boolean failFast;

        private boolean failAtEnd;

        private boolean failNever;

        private String resumeFrom;

        private String projects;

        private boolean alsoMake;

        private boolean alsoMakeDependents;

        private File logFile;

        private boolean showVersion;

        private String encryptMasterPassword;

        private String encryptPassword;

        private boolean noPluginRegistry;

        private List<String> goals;

        public File getFile() {
            return file;
        }

        public Request setFile(final File file) {
            this.file = file;
            return this;
        }

        public Properties getProperties() {
            return properties;
        }

        public Request setProperties(final Properties properties) {
            this.properties = properties;
            return this;
        }

        public boolean isOffline() {
            return offline;
        }

        public Request setOffline(final boolean offline) {
            this.offline = offline;
            return this;
        }

//        public boolean isVersion() {
//            return version;
//        }
//
//        public Request setVersion(final boolean version) {
//            this.version = version;
//            return this;
//        }

        public boolean isQuiet() {
            return quiet;
        }

        public Request setQuiet(final boolean quiet) {
            this.quiet = quiet;
            return this;
        }

        public boolean isDebug() {
            return debug;
        }

        public Request setDebug(final boolean debug) {
            this.debug = debug;
            return this;
        }

        public boolean isShowErrors() {
            return showErrors;
        }

        public Request setShowErrors(final boolean showErrors) {
            this.showErrors = showErrors;
            return this;
        }

        public boolean isNonRecursive() {
            return nonRecursive;
        }

        public Request setNonRecursive(final boolean nonRecursive) {
            this.nonRecursive = nonRecursive;
            return this;
        }

        public boolean isUpdateSnapshots() {
            return updateSnapshots;
        }

        public Request setUpdateSnapshots(final boolean updateSnapshots) {
            this.updateSnapshots = updateSnapshots;
            return this;
        }

        public String getActivateProfiles() {
            return activateProfiles;
        }

        public Request setActivateProfiles(final String activateProfiles) {
            this.activateProfiles = activateProfiles;
            return this;
        }

        public boolean isBatch() {
            return batch;
        }

        public Request setBatch(final boolean batch) {
            this.batch = batch;
            return this;
        }

        public boolean isCheckPluginUpdates() {
            return checkPluginUpdates;
        }

        public Request setCheckPluginUpdates(final boolean checkPluginUpdates) {
            this.checkPluginUpdates = checkPluginUpdates;
            return this;
        }

        public boolean isUpdatePlugins() {
            return updatePlugins;
        }

        public Request setUpdatePlugins(final boolean updatePlugins) {
            this.updatePlugins = updatePlugins;
            return this;
        }

        public boolean isNoPluginUpdates() {
            return noPluginUpdates;
        }

        public Request setNoPluginUpdates(final boolean noPluginUpdates) {
            this.noPluginUpdates = noPluginUpdates;
            return this;
        }

        public boolean isNoSnapshotUpdates() {
            return noSnapshotUpdates;
        }

        public Request setNoSnapshotUpdates(final boolean noSnapshotUpdates) {
            this.noSnapshotUpdates = noSnapshotUpdates;
            return this;
        }

        public boolean isStrictChecksums() {
            return strictChecksums;
        }

        public Request setStrictChecksums(final boolean strictChecksums) {
            this.strictChecksums = strictChecksums;
            return this;
        }

        public boolean isLaxChecksums() {
            return laxChecksums;
        }

        public Request setLaxChecksums(final boolean laxChecksums) {
            this.laxChecksums = laxChecksums;
            return this;
        }

        public File getSettings() {
            return settings;
        }

        public Request setSettings(final File settings) {
            this.settings = settings;
            return this;
        }

        public File getGlobalSettings() {
            return globalSettings;
        }

        public Request setGlobalSettings(final File globalSettings) {
            this.globalSettings = globalSettings;
            return this;
        }

        public File getToolChains() {
            return toolChains;
        }

        public Request setToolChains(final File toolChains) {
            this.toolChains = toolChains;
            return this;
        }

        public boolean isFailFast() {
            return failFast;
        }

        public Request setFailFast(final boolean failFast) {
            this.failFast = failFast;
            return this;
        }

        public boolean isFailAtEnd() {
            return failAtEnd;
        }

        public Request setFailAtEnd(final boolean failAtEnd) {
            this.failAtEnd = failAtEnd;
            return this;
        }

        public boolean isFailNever() {
            return failNever;
        }

        public Request setFailNever(final boolean failNever) {
            this.failNever = failNever;
            return this;
        }

        public String getResumeFrom() {
            return resumeFrom;
        }

        public Request setResumeFrom(final String resumeFrom) {
            this.resumeFrom = resumeFrom;
            return this;
        }

        public String getProjects() {
            return projects;
        }

        public Request setProjects(final String projects) {
            this.projects = projects;
            return this;
        }

        public boolean isAlsoMake() {
            return alsoMake;
        }

        public Request setAlsoMake(final boolean alsoMake) {
            this.alsoMake = alsoMake;
            return this;
        }

        public boolean isAlsoMakeDependents() {
            return alsoMakeDependents;
        }

        public Request setAlsoMakeDependents(final boolean alsoMakeDependents) {
            this.alsoMakeDependents = alsoMakeDependents;
            return this;
        }

        public File getLogFile() {
            return logFile;
        }

        public Request setLogFile(final File logFile) {
            this.logFile = logFile;
        return this;
        }

        public boolean isShowVersion() {
            return showVersion;
        }

        public Request setShowVersion(final boolean showVersion) {
            this.showVersion = showVersion;
            return this;
        }

        public String getEncryptMasterPassword() {
            return encryptMasterPassword;
        }

        public Request setEncryptMasterPassword(final String encryptMasterPassword) {
            this.encryptMasterPassword = encryptMasterPassword;
            return this;
        }

        public String getEncryptPassword() {
            return encryptPassword;
        }

        public Request setEncryptPassword(final String encryptPassword) {
            this.encryptPassword = encryptPassword;
            return this;
        }

        public boolean isNoPluginRegistry() {
            return noPluginRegistry;
        }

        public Request setNoPluginRegistry(final boolean noPluginRegistry) {
            this.noPluginRegistry = noPluginRegistry;
            return this;
        }

        public List<String> getGoals() {
            return goals;
        }

        public Request setGoals(final List<String> goals) {
            this.goals = goals;
            return this;
        }

        @Override
        public String toString() {
            return "Request" +
                "{\n    streams=" + streams +
                ",\n    classWorld=" + classWorld +
                ",\n    workingDirectory=" + workingDirectory +
                ",\n    logger=" + logger +
                ",\n    fileStream=" + fileStream +
                ",\n    request=" + request +
                ",\n    file=" + file +
                ",\n    properties=" + properties +
                ",\n    offline=" + offline +
                ",\n    quiet=" + quiet +
                ",\n    debug=" + debug +
                ",\n    showErrors=" + showErrors +
                ",\n    nonRecursive=" + nonRecursive +
                ",\n    updateSnapshots=" + updateSnapshots +
                ",\n    activateProfiles='" + activateProfiles + '\'' +
                ",\n    batch=" + batch +
                ",\n    checkPluginUpdates=" + checkPluginUpdates +
                ",\n    updatePlugins=" + updatePlugins +
                ",\n    noPluginUpdates=" + noPluginUpdates +
                ",\n    noSnapshotUpdates=" + noSnapshotUpdates +
                ",\n    strictChecksums=" + strictChecksums +
                ",\n    laxChecksums=" + laxChecksums +
                ",\n    settings=" + settings +
                ",\n    globalSettings=" + globalSettings +
                ",\n    toolChains=" + toolChains +
                ",\n    failFast=" + failFast +
                ",\n    failAtEnd=" + failAtEnd +
                ",\n    failNever=" + failNever +
                ",\n    resumeFrom='" + resumeFrom + '\'' +
                ",\n    projects='" + projects + '\'' +
                ",\n    alsoMake=" + alsoMake +
                ",\n    alsoMakeDependents=" + alsoMakeDependents +
                ",\n    logFile=" + logFile +
                ",\n    showVersion=" + showVersion +
                ",\n    encryptMasterPassword='" + encryptMasterPassword + '\'' +
                ",\n    encryptPassword='" + encryptPassword + '\'' +
                ",\n    noPluginRegistry=" + noPluginRegistry +
                ",\n    goals=" + goals +
                "\n}";
        }
    }

    class Result
    {
        public final int code;

        public Result(final int code) {
            this.code = code;
        }

        @Override
        public String toString() {
            return "Result{" +
                "code=" + code +
                '}';
        }
    }

    class ExitNotification
        extends Notification
    {
        public final int code;

        public ExitNotification(final int code) {
            this.code = code;
        }
    }
}