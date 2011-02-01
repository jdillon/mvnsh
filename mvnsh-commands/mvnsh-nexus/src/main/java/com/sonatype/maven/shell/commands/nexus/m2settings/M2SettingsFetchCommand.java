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

package com.sonatype.maven.shell.commands.nexus.m2settings;

import com.sonatype.maven.shell.commands.nexus.NexusCommandSupport;
import com.sonatype.maven.shell.nexus.M2SettingsClient;
import com.sonatype.maven.shell.nexus.NexusClient;
import org.sonatype.gshell.command.Command;
import org.sonatype.gshell.command.CommandContext;
import org.sonatype.gshell.command.IO;
import org.sonatype.gshell.util.io.Closer;
import org.sonatype.gshell.util.cli2.Argument;
import org.sonatype.gshell.util.cli2.Option;
import org.sonatype.gshell.util.pref.Preferences;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Fetch Maven settings.
 *
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @since 0.9
 */
@Command(name = "nexus/m2settings/fetch")
@Preferences(path = "commands/m2settings/fetch")
public class M2SettingsFetchCommand
    extends NexusCommandSupport
{
    @Option(name="f", longName="file")
    private File file;

    //
    // TODO: Add support for user or global settings
    //
    
    @Option(name="s", longName="settings")
    private Boolean settings;

    @Option(name="b", longName="backup")
    private boolean backup = true;

    @Option(name="F", longName="backup-format", args=1)
    private String backupFormat = "yyyyMMdd_HHmmss";

    @Argument(required=true)
    private String templateId;
    
    @Override
    protected Object execute(final CommandContext context, final NexusClient client) throws Exception {
        assert context != null;
        assert client != null;
        IO io = context.getIo();

        String content = client.ext(M2SettingsClient.class).fetch(templateId);

        File target = file;
        if (file == null) {
            if (settings != null) {
                target = new File(new File(System.getProperty("user.home")), ".m2/settings.xml");
            }
        }

        if (target == null) {
            io.out.println(content);
            return Result.SUCCESS;
        }

        log.debug("Target file: {}", target);

        if (backup && target.exists()) {
            String backupString = new SimpleDateFormat(backupFormat).format(new Date());

            File b = new File(target.getParentFile(), target.getName() + "." + backupString);

            log.debug("Backing up old settings to: {}", b.getAbsolutePath());

            if (!target.renameTo(b)) {
                io.error("Cannot rename existing settings to backup file.\nExisting file: {}\nBackup file: {}",
                    target.getAbsolutePath(), b.getAbsolutePath());
                return Result.FAILURE;
            }

            log.info("Existing settings backed up to: {}", b.getAbsolutePath());
        }

        PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter(target)));
        try {
            writer.print(content);
        }
        finally {
            Closer.close(writer);
        }

        io.println("Wrote settings to: {}", target.getAbsolutePath());

        return target;
    }
}