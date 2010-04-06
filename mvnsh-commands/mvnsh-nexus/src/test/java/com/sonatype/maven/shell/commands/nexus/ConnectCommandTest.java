/*
 * Sonatype Maven Shell (TM) Commercial Version.
 *
 * Copyright (c) 2009 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at http://www.sonatype.com/products/mvnsh/attributions/.
 * "Sonatype" and "Sonatype Nexus" are trademarks of Sonatype, Inc.
 */

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