/*
 * Sonatype Maven Shell (TM) Professional Version.
 *
 * Copyright (c) 2009 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at http://www.sonatype.com/products/mvnsh/attributions/.
 * "Sonatype" and "Sonatype Nexus" are trademarks of Sonatype, Inc.
 */

package org.sonatype.maven.shell;

import org.sonatype.gshell.MainSupport;
import org.sonatype.gshell.branding.Branding;
import org.sonatype.gshell.builder.guice.GuiceShellBuilder;
import org.sonatype.gshell.registry.CommandRegistrar;
import org.sonatype.gshell.shell.Shell;
import org.sonatype.gshell.shell.ShellErrorHandler;
import org.sonatype.gshell.shell.ShellPrompt;

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
        GuiceShellBuilder builder = new GuiceShellBuilder();

        // Use a custom location for the commands descriptor, since we shade all commands into one single descriptor
        CommandRegistrar registrar = builder.getInjector().getInstance(CommandRegistrar.class);
        registrar.setDescriptorSearchPath(
            "META-INF/mvnsh/commands.xml",
            CommandRegistrar.DEFAULT_DESCRIPTOR_LOCATION
        );

        return builder
                .setBranding(getBranding())
                .setIo(io)
                .setVariables(vars)
                .setPrompt(new ShellPrompt(vars, getBranding()))
                .setErrorHandler(new ShellErrorHandler(io))
                .create();
    }

    public static void main(final String[] args) throws Exception {
        new Main().boot(args);
    }
}
