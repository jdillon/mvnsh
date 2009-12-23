/*
 * Copyright (C) 2009 the original author or authors.
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