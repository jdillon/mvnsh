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
