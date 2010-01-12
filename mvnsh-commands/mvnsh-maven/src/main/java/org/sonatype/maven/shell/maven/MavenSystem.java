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

import java.io.File;

/**
 * Maven system.
 *
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @since 0.9
 */
public interface MavenSystem
{
    // FIXME: Use _PATH and _DIR to sep from property name constants
    
    String MAVEN_HOME = "maven.home";

    String LOCAL_REPO = "maven.repo.local";

    String USER_HOME = System.getProperty("user.home");

    File USER_MAVEN_CONF_HOME = new File(USER_HOME, ".m2");

    File DEFAULT_USER_SETTINGS_FILE = new File(USER_MAVEN_CONF_HOME, "settings.xml");

    File DEFAULT_GLOBAL_SETTINGS_FILE = new File(System.getProperty(MAVEN_HOME, System.getProperty("user.dir", "")), "conf/settings.xml");

    File DEFAULT_USER_TOOLCHAINS_FILE = new File(USER_MAVEN_CONF_HOME, "toolchains.xml");

    String getVersion();

    MavenRuntime create(MavenRuntimeConfiguration config) throws Exception;
}