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

import org.apache.maven.cli.PrintStreamLogger;
import org.codehaus.plexus.DefaultPlexusContainer;
import org.codehaus.plexus.classworlds.ClassWorld;
import org.sonatype.gshell.util.io.StreamSet;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * Maven runtime configuration.
 *
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @since 0.9
 */
public class MavenRuntimeConfiguration
{
    private StreamSet streams = StreamSet.system();

    private ClassWorld classWorld;

    private Delegate delegate;

    private File baseDirectory = new File(System.getProperty("user.dir"));

    // TODO: Should use a Verbosity enum here for quiet/debug

    private boolean quiet;

    private boolean debug;

    private Properties properties = new Properties();

    private List<String> profiles = new ArrayList<String>();

    private File pomFile;

    private File settingsFile;

    private File globalSettingsFile;

    private File logFile;

    private PrintStreamLogger logger;

    private boolean showVersion;

    public StreamSet getStreams() {
        return streams;
    }

    public void setStreams(StreamSet streams) {
        this.streams = streams;
    }

    public ClassWorld getClassWorld() {
        return classWorld;
    }

    public void setClassWorld(ClassWorld classWorld) {
        this.classWorld = classWorld;
    }

    public Delegate getDelegate() {
        return delegate;
    }

    public void setDelegate(Delegate delegate) {
        this.delegate = delegate;
    }

    public File getBaseDirectory() {
        return baseDirectory;
    }

    public void setBaseDirectory(File dir) {
        this.baseDirectory = dir;
    }

    public boolean isQuiet() {
        return quiet;
    }

    public void setQuiet(boolean quiet) {
        this.quiet = quiet;
    }

    public boolean isDebug() {
        return debug;
    }

    public void setDebug(boolean debug) {
        this.debug = debug;
    }

    public Properties getProperties() {
        return properties;
    }

    public List<String> getProfiles() {
        return profiles;
    }

    public File getPomFile() {
        return pomFile;
    }

    public void setPomFile(File pomFile) {
        this.pomFile = pomFile;
    }

    public File getSettingsFile() {
        return settingsFile;
    }

    public void setSettingsFile(File file) {
        this.settingsFile = file;
    }

    public File getGlobalSettingsFile() {
        return globalSettingsFile;
    }

    public void setGlobalSettingsFile(File file) {
        this.globalSettingsFile = file;
    }

    public File getLogFile() {
        return logFile;
    }

    public void setLogFile(File logFile) {
        this.logFile = logFile;
    }

    public PrintStreamLogger getLogger() {
        return logger;
    }

    public void setLogger(PrintStreamLogger logger) {
        this.logger = logger;
    }

    public boolean isShowVersion() {
        return showVersion;
    }

    public void setShowVersion(boolean showVersion) {
        this.showVersion = showVersion;
    }

    //
    // Delegate
    //

    public static interface Delegate
    {
        void configure(DefaultPlexusContainer container) throws Exception;
    }
}