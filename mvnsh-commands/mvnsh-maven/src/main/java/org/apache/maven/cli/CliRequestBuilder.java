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
