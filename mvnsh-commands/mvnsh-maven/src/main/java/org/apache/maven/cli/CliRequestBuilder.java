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
package org.apache.maven.cli;

import java.io.File;
import java.util.List;

import static com.google.common.base.Preconditions.checkState;

/**
 * Helper to create {@link CliRequest} instances.
 *
 * This is needed to configure package-private options needed to setup the environment for Maven execution.
 *
 * @since 1.2
 */
public class CliRequestBuilder
{
  private List<String> arguments;

  private File workingDirectory;

  private File projectDirectory;

  public List<String> getArguments() {
    return arguments;
  }

  public void setArguments(final List<String> arguments) {
    this.arguments = arguments;
  }

  public File getWorkingDirectory() {
    return workingDirectory;
  }

  public void setWorkingDirectory(final File workingDirectory) {
    this.workingDirectory = workingDirectory;
  }

  public File getProjectDirectory() {
    return projectDirectory;
  }

  public void setProjectDirectory(final File projectDirectory) {
    this.projectDirectory = projectDirectory;
  }

  public CliRequest build() {
    checkState(arguments != null, "Missing: arguments");

    CliRequest request = new CliRequest(arguments.toArray(new String[arguments.size()]), null);
    request.workingDirectory = workingDirectory.getAbsolutePath();
    request.multiModuleProjectDirectory = projectDirectory;

    return request;
  }
}
