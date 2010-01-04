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
    Request create();

    String getVersion();

    Result execute(Request request) throws Exception;

    //
    // TODO: Sub-class DefaultMavenExecutionRequest, only add methods to augment what additional muck is needed
    //

    class Request
    {
        private StreamSet streams = StreamSet.system();

        private ClassWorld classWorld;

        private File workingDirectory = new File(System.getProperty("user.dir"));

        private PrintStreamLogger logger;

        private PrintStream fileStream;

        private final MavenExecutionRequest request = new DefaultMavenExecutionRequest();

        public StreamSet getStreams() {
            return streams;
        }

        public Request setStreams(final StreamSet streams) {
            assert streams != null;
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

        public Request setWorkingDirectory(final File dir) {
            assert dir != null;
            this.workingDirectory = dir;
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

        private File file;

        private Properties properties = new Properties();

        private boolean quiet;

        private boolean debug;

        private File settings;

        private File globalSettings;

        private File toolChains;

        private File logFile;

        private boolean showVersion;

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

        public boolean isOffline() {
            return getRequest().isOffline();
        }

        public Request setOffline(final boolean offline) {
            getRequest().setOffline(offline);
            return this;
        }

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
            return getRequest().isShowErrors();
        }

        public Request setShowErrors(final boolean showErrors) {
            getRequest().setShowErrors(showErrors);
            return this;
        }

        public Request setActivateProfiles(final List<String> profiles) {
            if (profiles == null) {
                return this;
            }

            for (String profileAction : profiles) {
                profileAction = profileAction.trim();

                if (profileAction.startsWith("-") || profileAction.startsWith("!")) {
                    getRequest().addInactiveProfile(profileAction.substring(1));
                }
                else if (profileAction.startsWith("+")) {
                    getRequest().addActiveProfile(profileAction.substring(1));
                }
                else {
                    getRequest().addActiveProfile(profileAction);
                }
            }

            return this;
        }

        public boolean isBatch() {
            return !getRequest().isInteractiveMode();
        }

        public Request setBatch(final boolean batch) {
            getRequest().setInteractiveMode(!batch);
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
                ",\n    quiet=" + quiet +
                ",\n    debug=" + debug +
                ",\n    settings=" + settings +
                ",\n    globalSettings=" + globalSettings +
                ",\n    toolChains=" + toolChains +
                ",\n    logFile=" + logFile +
                ",\n    showVersion=" + showVersion +
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