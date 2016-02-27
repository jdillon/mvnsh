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