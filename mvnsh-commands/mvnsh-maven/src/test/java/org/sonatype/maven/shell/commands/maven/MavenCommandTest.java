/*
 * Copyright (c) 2007-2009 Sonatype, Inc. All rights reserved.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package org.sonatype.maven.shell.commands.maven;

import com.google.inject.Module;
import org.junit.Ignore;
import org.junit.Test;
import org.sonatype.gshell.commands.CommandTestSupport;
import org.sonatype.maven.shell.commands.maven.internal.MavenModule;

import java.net.URL;
import java.util.List;

import static org.junit.Assert.assertNotNull;

/**
 * Tests for the {@link MavenCommand}.
 *
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 */
@Ignore
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