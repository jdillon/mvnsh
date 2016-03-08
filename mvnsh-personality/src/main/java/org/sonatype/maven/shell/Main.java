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
package org.sonatype.maven.shell;

import java.util.List;

import org.sonatype.gshell.branding.Branding;
import org.sonatype.gshell.console.ConsoleErrorHandler;
import org.sonatype.gshell.console.ConsolePrompt;
import org.sonatype.gshell.guice.GuiceMainSupport;
import org.sonatype.gshell.logging.LoggingSystem;
import org.sonatype.gshell.logging.logback.LogbackLoggingSystem;
import org.sonatype.gshell.shell.ShellErrorHandler;
import org.sonatype.gshell.shell.ShellPrompt;

import com.google.inject.AbstractModule;
import com.google.inject.Module;

/**
 * Command-line bootstrap for Apache Maven Shell (<tt>mvnsh</tt>).
 * 
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @since 0.7
 */
public class Main
    extends GuiceMainSupport
{
    @Override
    protected Branding createBranding() {
        return new BrandingImpl();
    }

    @Override
    protected void configure(final List<Module> modules) {
        super.configure(modules);

        Module custom = new AbstractModule()
        {
            @Override
            protected void configure() {
                bind(LoggingSystem.class).to(LogbackLoggingSystem.class);
                bind(ConsolePrompt.class).to(ShellPrompt.class);
                bind(ConsoleErrorHandler.class).to(ShellErrorHandler.class);
            }
        };

        modules.add(custom);
    }

    public static void main(final String[] args) throws Exception {
        new Main().boot(args);
    }
}
