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
import com.planet57.gshell.command.CommandActionSupport;
import com.planet57.gshell.util.cli2.OpaqueArguments;
import com.planet57.gshell.util.pref.Preferences;
import com.planet57.gshell.variables.Variables;
import org.apache.maven.cli.CliRequestBuilder;
import org.apache.maven.cli.MavenCli;

import static com.google.common.base.Preconditions.checkNotNull;
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
  public Object execute(final CommandContext context) throws Exception {
    checkNotNull(context);

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

    MavenCli cli = new MavenCli();
    return cli.doMain(request.build());
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
