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