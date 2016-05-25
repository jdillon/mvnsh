/*
 * Copyright (c) 2009-present the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Apache License v2.0 which accompanies this distribution.
 *
 * The Eclipse Public License is available at
 *   http://www.eclipse.org/legal/epl-v10.html
 *
 * The Apache License v2.0 is available at
 *   http://www.apache.org/licenses/LICENSE-2.0.html
 *
 * You may elect to redistribute this code under either of these licenses.
 */
package com.planet57.maven.shell.commands.maven;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.planet57.gshell.command.Command;
import com.planet57.gshell.command.CommandContext;
import com.planet57.gshell.command.IO;
import com.planet57.gshell.command.support.CommandActionSupport;
import com.planet57.gshell.util.cli2.OpaqueArguments;
import com.planet57.gshell.util.io.StreamJack;
import com.planet57.gshell.util.io.StreamSet;
import com.planet57.gshell.util.pref.Preferences;
import com.planet57.gshell.variables.Variables;
import org.apache.maven.cli.CliRequestBuilder;
import org.apache.maven.cli.MavenCli;

import static com.planet57.gshell.variables.VariableNames.SHELL_USER_DIR;

/**
 * Execute Maven.
 *
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @since 0.7
 */
@Command(name = "mvn")
@Preferences(path = "commands/mvn")
public class MavenCommand
    extends CommandActionSupport
    implements OpaqueArguments
{
  // HACK: no additional options due to use of OpaqueArguments
  private Boolean color = true;

  public Object execute(final CommandContext context) throws Exception {
    assert context != null;

    IO io = context.getIo();
    Variables vars = context.getVariables();

    CliRequestBuilder request = new CliRequestBuilder();
    request.setArguments(strings(context.getArguments()));

    File baseDir = vars.get(SHELL_USER_DIR, File.class);
    request.setWorkingDirectory(baseDir);

    File projectDir = vars.get(MavenCli.MULTIMODULE_PROJECT_DIRECTORY, File.class, null);
    if (projectDir == null) {
      projectDir = findProjectDir(baseDir);
    }
    request.setProjectDirectory(projectDir);

    // Setup output colorization
    StreamSet current = StreamJack.current();
    StreamSet streams;
    if (color == null || color) {
      // Complain if the user asked for color and its not supported
      if (color != null && !io.getTerminal().isAnsiSupported()) {
        log.warn("ANSI color is not supported by the current terminal");
      }
      streams = new StreamSet(current.in, new ColorizingStream(current.out), new ColorizingStream(current.err));
    }
    else {
      streams = current;
    }
    StreamJack.register(streams);

    int result = -1;
    try {
      MavenCli cli = new MavenCli();
      result = cli.doMain(request.build());
    }
    finally {
      StreamJack.deregister();
    }

    return result;
  }

  private List<String> strings(final Object[] input) {
    List<String> result = new ArrayList<String>(input.length);
    for (Object value : input) {
      result.add(String.valueOf(value));
    }
    return result;
  }

  private File findProjectDir(final File baseDir) {
    File dir = baseDir;
    while (dir != null) {
      File file = new File(dir, ".mvn");
      if (file.isDirectory()) {
        return dir;
      }
      dir = dir.getParentFile();
    }

    return baseDir;
  }
}