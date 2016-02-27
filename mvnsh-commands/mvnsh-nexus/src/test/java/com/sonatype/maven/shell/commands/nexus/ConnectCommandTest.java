/*******************************************************************************
 * Copyright (c) 2009-present Sonatype, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Apache License v2.0 which accompanies this distribution.
 * The Eclipse Public License is available at 
 *   http://www.eclipse.org/legal/epl-v10.html
 * The Apache License v2.0 is available at
 *   http://www.apache.org/licenses/LICENSE-2.0.html
 * You may elect to redistribute this code under either of these licenses. 
 *******************************************************************************/

package com.sonatype.maven.shell.commands.nexus;

import org.junit.Ignore;
import org.junit.Test;

/**
 * Tests for the {@link ConnectCommand}.
 *
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 */
public class ConnectCommandTest
    extends NexusCommandTestSupport
{
    public ConnectCommandTest() {
        super(ConnectCommand.class);
    }

    @Override
    @Test
    @Ignore
    public void testDefault() throws Exception {
        Object result = execute();
        assertEqualsFailure(result);
    }
}