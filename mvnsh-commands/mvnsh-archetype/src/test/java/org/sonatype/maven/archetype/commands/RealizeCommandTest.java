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
package org.sonatype.maven.archetype.commands;

import org.junit.Ignore;
import org.junit.Test;
import org.sonatype.gshell.command.support.CommandTestSupport;

/**
 * Tests for the {@link RealizeCommand}.
 *
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 */
public class RealizeCommandTest
    extends CommandTestSupport
{
    public RealizeCommandTest() {
        super(RealizeCommand.class);
    }

    @Override
    @Test
    @Ignore
    public void testDefault() throws Exception {
        // nothing
    }
}