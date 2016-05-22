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

import java.io.File;
import java.util.Collections;
import java.util.Properties;

import com.google.inject.Inject;
import com.planet57.gshell.command.Command;
import com.planet57.gshell.command.CommandContext;
import com.planet57.gshell.command.IO;
import com.planet57.gshell.command.support.CommandActionSupport;
import com.planet57.gshell.plexus.PlexusRuntime;
import com.planet57.gshell.util.NameValue;
import com.planet57.gshell.util.cli2.Argument;
import com.planet57.gshell.util.cli2.Option;
import com.planet57.gshell.variables.Variables;
import org.apache.maven.archetype.ArchetypeGenerationRequest;
import org.apache.maven.archetype.ArchetypeGenerationResult;
import org.apache.maven.archetype.ArchetypeManager;
import org.apache.maven.archetype.ui.ArchetypeGenerationConfigurator;
import org.apache.maven.archetype.ui.ArchetypeSelector;
import org.apache.maven.repository.RepositorySystem;

import static com.planet57.gshell.variables.VariableNames.SHELL_USER_DIR;

/**
 * Realize a project from an archetype.
 *
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @since 0.10
 */
@Command(name="archetype/realize")
public class RealizeCommand
    extends CommandActionSupport
{
    private final PlexusRuntime plexus;

    private Properties props = new Properties();

    @Option(name = "d", longName="directory", args=1)
    private File outputDirectory;

    @Option(name = "b", longName="batch")
    private boolean batch;

    @Option(name = "c", longName="catalog", args=1)
    private String catalog = "remote,local";

    @Option(name = "D", longName="define", args=1)
    protected void setProperty(final String input) {
        NameValue nv = NameValue.parse(input);
        props.setProperty(nv.name, nv.value);
    }

    @Argument
    private String archetypeId;

    @Inject
    public RealizeCommand(final PlexusRuntime plexus) {
        assert plexus != null;
        this.plexus = plexus;
    }

    public Object execute(final CommandContext context) throws Exception {
        assert context != null;
        IO io = context.getIo();
        Variables vars = context.getVariables();

        RepositorySystem rsys = plexus.lookup(RepositorySystem.class);
        ArchetypeGenerationRequest request = new ArchetypeGenerationRequest()
            .setLocalRepository(rsys.createDefaultLocalRepository())
            .setRemoteArtifactRepositories(Collections.singletonList(rsys.createDefaultRemoteRepository()));

        if (outputDirectory == null) {
            outputDirectory = new File(vars.get(SHELL_USER_DIR, String.class));
        }
        request.setOutputDirectory(outputDirectory.getAbsolutePath());

        if (archetypeId != null) {
            String[] items = archetypeId.split(":", 3);
            if (items.length != 3) {
                io.error("Invalid archetype id: {}", archetypeId); // TODO: i18n
                return Result.FAILURE;
            }

            request.setArchetypeGroupId(items[0])
                .setArchetypeArtifactId(items[1])
                .setArchetypeVersion(items[2]);
        }

        ArchetypeSelector selector = plexus.lookup(ArchetypeSelector.class);

        selector.selectArchetype(request, !batch, catalog);

        ArchetypeGenerationConfigurator configurator = plexus.lookup(ArchetypeGenerationConfigurator.class);
        configurator.configureArchetype(request, !batch, props);

        ArchetypeManager archetypeManager = plexus.lookup(ArchetypeManager.class);
        ArchetypeGenerationResult generationResult = archetypeManager.generateProjectFromArchetype(request);
        if (generationResult.getCause() != null) {
            throw generationResult.getCause();
        }

        return Result.SUCCESS;
    }
}