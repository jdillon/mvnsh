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

import com.google.inject.Inject;
import org.apache.maven.archetype.catalog.ArchetypeCatalog;
import org.apache.maven.archetype.repositorycrawler.RepositoryCrawler;
import org.sonatype.gshell.command.Command;
import org.sonatype.gshell.command.support.CommandActionSupport;
import org.sonatype.gshell.command.CommandContext;
import org.sonatype.gshell.command.IO;
import org.sonatype.gshell.plexus.PlexusRuntime;
import org.sonatype.gshell.util.FileAssert;
import org.sonatype.gshell.util.cli2.Argument;
import org.sonatype.gshell.util.cli2.Option;

import java.io.File;

/**
 * Crawl a repository looking for archetypes.
 *
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @since 0.10
 */
@Command(name="archetype/crawl")
public class CrawlCommand
    extends CommandActionSupport
{
    private final PlexusRuntime plexus;

    @Option(name="c", longName="catalog", args=1)
    private File catalogFile;

    @Argument(required=true)
    private File repository;

    @Inject
    public CrawlCommand(final PlexusRuntime plexus) {
        assert plexus != null;
        this.plexus = plexus;
    }

    public Object execute(final CommandContext context) throws Exception {
        assert context != null;
        IO io = context.getIo();

        new FileAssert(repository).exists().isDirectory();

        RepositoryCrawler crawler = plexus.lookup(RepositoryCrawler.class);

        log.debug("Crawling repository: {}", repository);
        ArchetypeCatalog catalog = crawler.crawl(repository);
        if (catalogFile == null) {
            catalogFile = new File(repository, "archetype-catalog.xml");
        }

        log.debug("Writing catalog file: {}", catalogFile);
        crawler.writeCatalog(catalog, catalogFile);

        return Result.SUCCESS;
    }
}