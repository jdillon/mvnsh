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

package org.sonatype.maven.archetype.commands;

import com.google.inject.Inject;
import org.apache.maven.archetype.ArchetypeCreationRequest;
import org.apache.maven.archetype.ArchetypeCreationResult;
import org.apache.maven.archetype.ArchetypeManager;
import org.apache.maven.archetype.common.Constants;
import org.apache.maven.archetype.ui.ArchetypeCreationConfigurator;
import org.apache.maven.project.DefaultProjectBuildingRequest;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.ProjectBuilder;
import org.apache.maven.project.ProjectBuildingRequest;
import org.apache.maven.project.ProjectBuildingResult;
import org.apache.maven.repository.RepositorySystem;
import org.sonatype.gshell.command.Command;
import org.sonatype.gshell.command.support.CommandActionSupport;
import org.sonatype.gshell.command.CommandContext;
import org.sonatype.gshell.command.IO;
import org.sonatype.gshell.console.completer.FileNameCompleter;
import org.sonatype.gshell.plexus.PlexusRuntime;
import org.sonatype.gshell.util.NameValue;
import org.sonatype.gshell.util.cli2.Argument;
import org.sonatype.gshell.util.cli2.Option;
import org.sonatype.gshell.variables.Variables;

import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

import static org.sonatype.gshell.variables.VariableNames.SHELL_USER_DIR;
import static org.sonatype.gshell.variables.VariableNames.SHELL_USER_HOME;

/**
 * Create an archetype from an existing project.
 *
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @since 0.10
 */
@Command(name="archetype/create")
public class CreateCommand
    extends CommandActionSupport
{
    private final PlexusRuntime plexus;

    private Properties props = new Properties();

    @Option(name = "b", longName="batch")
    private boolean batch;

    @Option(name = "l", longName="languages", args=1)
    private List<String> languages = Constants.DEFAULT_LANGUAGES;

    @Option(name="x", longName="filtered-extensions", args=1)
    private List<String> filteredExtensions = Constants.DEFAULT_FILTERED_EXTENSIONS;

    @Option(name="r", longName="registry", args=1)
    private File registryFile;

    @Option(name="e", longName="encoding", args=1)
    private String encoding = "UTF-8";

    @Option(name="R", longName="partial")
    private boolean partial;

    @Option(name="P", longName="preserve-cdata")
    private boolean preserveCDATA;

    @Option(name="k", longName="keep-parent")
    private boolean keepParent = true;

    @Option(name="p", longName="package", args=1)
    private String packageName;

    @Option(name = "d", longName="directory", args=1)
    private File outputDirectory;

    @Option(name = "D", longName="define", args=1)
    protected void setProperty(final String input) {
        NameValue nv = NameValue.parse(input);
        props.setProperty(nv.name, nv.value);
    }

    @Argument(required=true)
    private File pomFile;

    @Inject
    public CreateCommand(final PlexusRuntime plexus) {
        assert plexus != null;
        this.plexus = plexus;
    }

    @Inject
    public CreateCommand installCompleters(final FileNameCompleter c1) {
        assert c1 != null;
        setCompleters(c1, null);
        return this;
    }

    public Object execute(final CommandContext context) throws Exception {
        assert context != null;
        IO io = context.getIo();
        Variables vars = context.getVariables();

        io.println("Creating archetype from: {}", pomFile.getAbsoluteFile()); // TODO: i18n

        MavenProject project = buildProject(context);
        log.debug("Built project: {}", project);

        log.debug("Configuring");
        ArchetypeCreationConfigurator configurator = plexus.lookup(ArchetypeCreationConfigurator.class);
        Properties config = configurator.configureArchetypeCreation(project, !batch, props, null, languages);

        RepositorySystem rsys = plexus.lookup(RepositorySystem.class);

        ArchetypeCreationRequest request = new ArchetypeCreationRequest()
            .setProject(project)
            .setProperties(config)
            .setLanguages(languages)
            .setFilteredExtensions(filteredExtensions)
            .setPreserveCData(preserveCDATA)
            .setKeepParent(keepParent)
            .setPartialArchetype(partial)
            .setLocalRepository(rsys.createDefaultLocalRepository())
            .setPackageName(packageName);

        if (registryFile == null) {
            File dir = vars.get(SHELL_USER_HOME, File.class);
            registryFile = new File(dir, ".m2/archetype.xml");
        }
        request.setArchetypeRegistryFile(registryFile);

        if (outputDirectory != null) {
            request.setOutputDirectory(outputDirectory);
        }
        else {
            // HACK: Default dir is not rooted
            System.setProperty("user.dir", vars.get(SHELL_USER_DIR, File.class).getAbsolutePath());
        }

        log.debug("Creating archetype");
        ArchetypeManager archetypeManager = plexus.lookup(ArchetypeManager.class);
        ArchetypeCreationResult result = archetypeManager.createArchetypeFromProject(request);

        if (result.getCause() != null) {
            throw result.getCause();
        }

        // HACK: Prompter has some issues, so add a newline
        io.out.println();

        io.println("Archetype created in: {}", request.getOutputDirectory().getAbsolutePath()); // TODO: i18n

        return Result.SUCCESS;
    }

    private MavenProject buildProject(final CommandContext context) throws Exception {
        assert context != null;

        ProjectBuilder builder = plexus.lookup(ProjectBuilder.class);
        ProjectBuildingRequest request = new DefaultProjectBuildingRequest();

        RepositorySystem rsys = plexus.lookup(RepositorySystem.class);
        request.setLocalRepository(rsys.createDefaultLocalRepository());
        request.setRemoteRepositories(Collections.singletonList(rsys.createDefaultRemoteRepository()));

//        request.setLocalRepository( getLocalRepository() );
//        request.setSystemProperties( getSystemProperties() );
//        request.setUserProperties( getUserProperties() );
//        request.setRemoteRepositories( getRemoteRepositories() );
//        request.setPluginArtifactRepositories( getPluginArtifactRepositories() );
//        request.setRepositoryCache( getRepositoryCache() );
//        request.setOffline( isOffline() );
//        request.setForceUpdate( isUpdateSnapshots() );
//        request.setServers( getServers() );
//        request.setMirrors( getMirrors() );
//        request.setProxies( getProxies() );
//        request.setActiveProfileIds( getActiveProfiles() );
//        request.setInactiveProfileIds( getInactiveProfiles() );
//        request.setProfiles( getProfiles() );
//        request.setProcessPlugins( true );
//        request.setBuildStartTime( getStartTime() );
//        request.setTransferListener( getTransferListener() );

        ProjectBuildingResult result = builder.build(pomFile, request);

        // TODO: Handle problems?

        return result.getProject();
    }
}