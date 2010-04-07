/*
 * Copyright (c) 2009-2010 Sonatype, Inc. All rights reserved.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
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
    protected void configureModules(final List<Module> modules) {
        assert modules != null;
        super.configureModules(modules);
        modules.add(new MavenModule());
    }

    @Override
    @Test
    @Ignore
    public void testDefault() throws Exception {
        // disabled
    }

    @Test
    public void test1() throws Exception {
        URL script = getClass().getResource("test1.pom");
        assertNotNull(script);

        String pom = new File(script.toURI()).toString();
        System.out.println("POM: " + pom);

        Object result = executeWithArgs("-e", "-f", pom, "-o");

        System.out.println("OUT: " + getIo().getOutputString());
        System.out.println("ERR: " + getIo().getErrorString());

        assertEquals(0, result);
    }
}