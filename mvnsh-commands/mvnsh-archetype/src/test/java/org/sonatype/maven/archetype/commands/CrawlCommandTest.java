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

import com.planet57.gshell.command.support.CommandTestSupport;
import org.junit.Ignore;
import org.junit.Test;

/**
 * Tests for the {@link CrawlCommand}.
 *
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 */
public class CrawlCommandTest
    extends CommandTestSupport
{
    public CrawlCommandTest() {
        super(CrawlCommand.class);
    }

    @Override
    @Test
    @Ignore
    public void testDefault() throws Exception {
        // nothing
    }
}