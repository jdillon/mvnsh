/*******************************************************************************
 * Copyright (c) 2009-present Sonatype, Inc.
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

import com.sonatype.maven.shell.nexus.BasicClient;
import com.sonatype.maven.shell.nexus.NexusClient;
import com.sonatype.maven.shell.nexus.internal.wink.MultivaluedMapImpl;
import org.sonatype.gshell.command.Command;
import org.sonatype.gshell.command.CommandContext;
import org.sonatype.gshell.command.IO;
import org.sonatype.gshell.util.cli2.Option;
import org.sonatype.gshell.util.pref.Preferences;
import org.sonatype.nexus.rest.model.NexusArtifact;
import org.sonatype.nexus.rest.model.SearchResponse;

import javax.ws.rs.core.MultivaluedMap;
import java.util.List;

/**
 * Search data index for repository content.
 *
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @since 0.9
 */
@Command(name = "nexus/search")
@Preferences(path = "commands/nexus/search")
public class SearchCommand
    extends NexusCommandSupport
{
    @Option(name="q", longName="query")
    private String query;

    @Option(name="g", longName="group-id")
    private String groupId;

    @Option(name="a", longName="artifact-id")
    private String artifactId;

    @Option(name="v", longName="version")
    private String version;

//    private String classifier;
//
//    private String packaging;
    
    @Override
    protected Object execute(final CommandContext context, final NexusClient client) throws Exception {
        assert context != null;
        assert client != null;
        IO io = context.getIo();

        MultivaluedMap<String,String> params = new MultivaluedMapImpl<String,String>();

        if (query != null) {
            params.putSingle("q", query);
        }
        if (groupId != null) {
            params.putSingle("g", groupId);
        }
        if (artifactId != null) {
            params.putSingle("a", artifactId);
        }
        if (version != null) {
            params.putSingle("v", version);
        }

        if (params.isEmpty()) {
            io.error("Missing search criteria");
            return Result.FAILURE;
        }

        SearchResponse response = client.ext(BasicClient.class).search(params);

        // TODO: Need to return better results, and handle errors
        
        io.println("Total count: {}", response.getTotalCount());
        io.println("From: {}", response.getFrom());
        io.println("Count: {}", response.getCount());
        io.println("Too many results: {}", response.isTooManyResults());

        List<NexusArtifact> results = response.getData();
        if (!results.isEmpty()) {
            log.info("Results:");
            for (NexusArtifact artifact : results) {
                log.info("  Resource: {}", artifact.getResourceURI());
            }
        }

        return results;
    }
}