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
import org.codehaus.plexus.classworlds.ClassWorld;
import org.jline.reader.Completer;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import javax.inject.Provider;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.planet57.gshell.variables.VariableNames.SHELL_HOME;
import static com.planet57.gshell.variables.VariableNames.SHELL_USER_DIR;

/**
 * Execute Maven.
 *
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @since 0.7
 */
@Command(name = "mvn", description = "Execute Maven")
@Preferences(path = "commands/mvn")
public class MavenAction
    extends CommandActionSupport
    implements OpaqueArguments
{
  private final MavenCompleter completer;

  private final Provider<ClassWorld> classWorld;

  @Inject
  public MavenAction(final Provider<ClassWorld> classWorld, final MavenCompleter completer) {
    this.classWorld = checkNotNull(classWorld);
    this.completer = checkNotNull(completer);
  }

  @Override
  protected Completer discoverCompleter() {
    return completer;
  }

  @Override
  public Object execute(@Nonnull final CommandContext context) throws Exception {
    Variables variables = context.getVariables();

    CliRequestBuilder request = new CliRequestBuilder();
    request.setArguments(strings(context.getArguments()));

    File baseDir = variables.require(SHELL_USER_DIR, File.class);
    request.setWorkingDirectory(baseDir);

    File projectDir = variables.get(MavenCli.MULTIMODULE_PROJECT_DIRECTORY, File.class, null);
    if (projectDir == null) {
      projectDir = findRootProjectDir(baseDir);
    }
    request.setProjectDirectory(projectDir);

    // a few parts of Maven expect "maven.home" system-property to be set
    File shellHome = variables.require(SHELL_HOME, File.class);
    System.setProperty("maven.home", shellHome.getAbsolutePath());

    MavenCli cli = new MavenCli(classWorld.get());
    return cli.doMain(request.build());
  }

  /**
   * Convert object array to list of strings.
   */
  private List<String> strings(final Object[] input) {
    List<String> result = new ArrayList<>(input.length);
    for (Object value : input) {
      result.add(String.valueOf(value));
    }
    return result;
  }

  public List<String> strings(final List<?> input) {
    return strings(input.toArray());
  }

  /**
   * Find the root project directory for given directory.
   */
  private File findRootProjectDir(final File baseDir) {
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
