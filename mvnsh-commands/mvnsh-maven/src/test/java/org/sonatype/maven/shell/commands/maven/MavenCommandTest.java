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
package org.sonatype.maven.shell.commands.maven;

import com.google.inject.Module;
import org.junit.Ignore;
import org.junit.Test;
import org.sonatype.gshell.command.support.CommandTestSupport;
import org.sonatype.maven.shell.maven.MavenModule;

import java.io.File;
import java.net.URL;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Tests for the {@link MavenCommand}.
 *
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 */
public class MavenCommandTest
    extends CommandTestSupport
{
    public MavenCommandTest() {
        super(MavenCommand.class);
    }

    @Override
    @Test
    @Ignore
    public void testDefault() throws Exception {
        // disabled
    }

    @Test
    public void test1() throws Exception {
        String settings = new File(getClass().getResource("settings.xml").toURI()).toString();
        System.out.println("Settings: " + settings);

        String pom = new File(getClass().getResource("test1.pom").toURI()).toString();
        System.out.println("POM: " + pom);

        //File repoDir = new File(new File(System.getProperty("basedir")), "target/test-repo");
        //System.out.println("Repo Dir: " + repoDir);

        Object result = executeWithArgs(
            "-B", "-e", "-V",
            "-f", pom,
            "-s", settings,
            //"-Dmaven.repo.local=" + repoDir,
            "package");

        System.out.println("OUT: " + getIo().getOutputString());
        System.out.println("ERR: " + getIo().getErrorString());

        assertEquals(0, result);
    }
}