/*******************************************************************************
 * Copyright (c) 2009-2011 Sonatype, Inc.
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