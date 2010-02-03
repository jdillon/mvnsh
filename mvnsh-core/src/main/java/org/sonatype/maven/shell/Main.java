/*
 * Copyright (c) 2007-2009 Sonatype, Inc. All rights reserved.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package org.sonatype.maven.shell;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.Stage;
import com.google.inject.name.Names;
import org.sonatype.gshell.MainSupport;
import org.sonatype.gshell.branding.Branding;
import org.sonatype.gshell.command.CommandRegistrar;
import org.sonatype.gshell.command.IO;
import org.sonatype.gshell.console.ConsoleErrorHandler;
import org.sonatype.gshell.console.ConsolePrompt;
import org.sonatype.gshell.guice.CoreModule;
import org.sonatype.gshell.shell.Shell;
import org.sonatype.gshell.shell.ShellErrorHandler;
import org.sonatype.gshell.shell.ShellImpl;
import org.sonatype.gshell.shell.ShellPrompt;
import org.sonatype.gshell.vars.Variables;

/**
 * Command-line bootstrap for Apache Maven Shell (<tt>mvnsh</tt>).
 * 
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @since 0.7
 */
public class Main
    extends MainSupport
{
    @Override
    protected Branding createBranding() {
        return new BrandingImpl();
    }

    @Override
    protected Shell createShell() throws Exception {
        Module module = new AbstractModule()
        {
            @Override
            protected void configure() {
                bind(ConsolePrompt.class).to(ShellPrompt.class);
                bind(ConsoleErrorHandler.class).to(ShellErrorHandler.class);
                bind(Branding.class).toInstance(getBranding());
                bind(IO.class).annotatedWith(Names.named("main")).toInstance(io);
                bind(Variables.class).annotatedWith(Names.named("main")).toInstance(vars);
            }
        };

        Injector injector = Guice.createInjector(Stage.PRODUCTION, module, new CoreModule());
        ShellImpl shell = injector.getInstance(ShellImpl.class);
        injector.getInstance(CommandRegistrar.class).registerCommands();

        return shell;
    }
    public static void main(final String[] args) throws Exception {
        new Main().boot(args);
    }
}
