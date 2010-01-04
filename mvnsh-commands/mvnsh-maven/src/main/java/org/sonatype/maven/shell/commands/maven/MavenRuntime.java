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

    class Request
        extends DefaultMavenExecutionRequest
    {
        private StreamSet streams = StreamSet.system();

        private ClassWorld classWorld;

        private File workingDirectory = new File(System.getProperty("user.dir"));

        private PrintStreamLogger logger;

        private File file;

        private Properties properties = new Properties();

        private boolean quiet;

        private boolean debug;

        private File settings;

        private File globalSettings;

        private File toolChains;

        private File logFile;

        private PrintStream fileStream;

        private boolean showVersion;

        public StreamSet getStreams() {
            return streams;
        }

        public void setStreams(final StreamSet streams) {
            assert streams != null;
            this.streams = streams;
        }

        public ClassWorld getClassWorld() {
            return classWorld;
        }

        public void setClassWorld(final ClassWorld classWorld) {
            this.classWorld = classWorld;
        }

        public File getWorkingDirectory() {
            return workingDirectory;
        }

        public void setWorkingDirectory(final File dir) {
            assert dir != null;
            this.workingDirectory = dir;
        }

        public PrintStreamLogger getLogger() {
            return logger;
        }

        public void setLogger(final PrintStreamLogger logger) {
            this.logger = logger;
        }

        public File getFile() {
            return file;
        }

        public void setFile(final File file) {
            this.file = file;
        }

        public Properties getProperties() {
            return properties;
        }

        public boolean isQuiet() {
            return quiet;
        }

        public void setQuiet(final boolean quiet) {
            this.quiet = quiet;
        }

        public boolean isDebug() {
            return debug;
        }

        public void setDebug(final boolean debug) {
            this.debug = debug;
        }

        public void setProfiles(final List<String> profiles) {
            if (profiles == null) {
                return;
            }

            for (String profile : profiles) {
                profile = profile.trim();

                if (profile.startsWith("-") || profile.startsWith("!")) {
                    addInactiveProfile(profile.substring(1));
                }
                else if (profile.startsWith("+")) {
                    addActiveProfile(profile.substring(1));
                }
                else {
                    addActiveProfile(profile);
                }
            }
        }

        public File getSettings() {
            return settings;
        }

        public void setSettings(final File settings) {
            this.settings = settings;
        }

        public File getGlobalSettings() {
            return globalSettings;
        }

        public void setGlobalSettings(final File globalSettings) {
            this.globalSettings = globalSettings;
        }

        public File getToolChains() {
            return toolChains;
        }

        public void setToolChains(final File toolChains) {
            this.toolChains = toolChains;
        }

        public File getLogFile() {
            return logFile;
        }

        public void setLogFile(final File logFile) {
            this.logFile = logFile;
        }

        public PrintStream getFileStream() {
            return fileStream;
        }

        public void setFileStream(final PrintStream fileStream) {
            this.fileStream = fileStream;
        }
        
        public boolean isShowVersion() {
            return showVersion;
        }

        public void setShowVersion(final boolean showVersion) {
            this.showVersion = showVersion;
        }
    }

    class Result
    {
        public final int code;

        public Result(final int code) {
            this.code = code;
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