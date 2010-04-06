/*
 * Sonatype Maven Shell (TM) Commercial Version.
 *
 * Copyright (c) 2009 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at http://www.sonatype.com/products/mvnsh/attributions/.
 * "Sonatype" and "Sonatype Nexus" are trademarks of Sonatype, Inc.
 */

package com.sonatype.maven.shell.commands.nexus;

import com.google.inject.Module;
import org.sonatype.gshell.command.support.CommandTestSupport;
import com.sonatype.maven.shell.nexus.internal.NexusClientModule;

import java.util.List;

/**
 * Support for testing Nexus commands.
 *
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 */
public class NexusCommandTestSupport
    extends CommandTestSupport
{
    public NexusCommandTestSupport(final Class<?> type) {
        super(type);
    }

    @Override
    protected void configureModules(final List<Module> modules) {
        super.configureModules(modules);
        modules.add(new NexusClientModule());
    }
}