/*
 * Copyright (c) 2007-2009 Sonatype, Inc. All rights reserved.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package org.sonatype.maven.shell.commands.maven;

import org.junit.Ignore;
import org.junit.Test;
import org.sonatype.gshell.commands.CommandTestSupport;

import java.net.URL;

import static org.junit.Assert.assertNotNull;

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
        Object result = execute();
        assertEqualsFailure(result);
    }

    @Override
    @Test
    @Ignore
    public void testHelp() throws Exception {
        Object result;

        result = executeWithArgs("--help");

        // HACK: Ignore result for now mvn3's cli is in too much flux
        // assertEqualsSuccess(result);

        result = executeWithArgs("-h");

        // HACK: Ignore result for now mvn3's cli is in too much flux
        // assertEqualsSuccess(result);
    }

    @Test
    @Ignore
    public void test1() throws Exception {
        URL script = getClass().getResource("test1.pom");
        assertNotNull(script);
        Object result = executeWithArgs("-f", script.toExternalForm(), "-o");

        // HACK: Ignore result for now mvn3's cli is in too much flux
        // assertEqualsSuccess(result);
    }
}