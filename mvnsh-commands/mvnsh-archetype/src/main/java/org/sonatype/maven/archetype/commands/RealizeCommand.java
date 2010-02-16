/*
 * Copyright (C) 2009 the original author(s).
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

package org.sonatype.maven.archetype.commands;

import com.google.inject.Inject;
import org.apache.maven.archetype.ArchetypeGenerationRequest;
import org.apache.maven.archetype.ArchetypeGenerationResult;
import org.apache.maven.archetype.ArchetypeManager;
import org.apache.maven.archetype.ui.ArchetypeGenerationConfigurator;
import org.apache.maven.archetype.ui.ArchetypeSelector;
import org.apache.maven.repository.RepositorySystem;
import org.sonatype.gshell.command.Command;
import org.sonatype.gshell.command.CommandContext;
import org.sonatype.gshell.command.IO;
import org.sonatype.gshell.command.support.CommandActionSupport;
import org.sonatype.gshell.plexus.PlexusRuntime;
import org.sonatype.gshell.util.NameValue;
import org.sonatype.gshell.util.cli2.Argument;
import org.sonatype.gshell.util.cli2.Option;
import org.sonatype.gshell.variables.Variables;

import java.io.File;
import java.util.Collections;
import java.util.Properties;

import static org.sonatype.gshell.variables.VariableNames.SHELL_USER_DIR;

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