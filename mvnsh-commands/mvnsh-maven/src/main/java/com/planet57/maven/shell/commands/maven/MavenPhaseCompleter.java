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

import com.planet57.gshell.util.jline.StringsCompleter2;
import org.jline.reader.Candidate;
import org.jline.reader.Completer;
import org.jline.reader.LineReader;
import org.jline.reader.ParsedLine;

import javax.inject.Named;
import javax.inject.Singleton;
import java.util.List;

/**
 * Maven lifecycle phase completer.
 *
 * @since 3.0
 */
@Named("maven-phase")
@Singleton
public class MavenPhaseCompleter
  implements Completer
{
  private static StringsCompleter2 delegate = new StringsCompleter2();

  public MavenPhaseCompleter() {
    delegate.addAll(
      "validate",
      "initialize",
      "generate-sources",
      "process-sources",
      "generate-resources",
      "process-resources",
      "compile",
      "process-classes",
      "generate-test-sources",
      "process-test-sources",
      "generate-test-resources",
      "process-test-resources",
      "test-compile",
      "process-test-classes",
      "test",
      "prepare-package",
      "package",
      "pre-integration-test",
      "integration-test",
      "post-integration-test",
      "verify",
      "install",
      "deploy"
    );

    // clean lifecycle
    delegate.addAll(
      "pre-clean",
      "clean",
      "post-clean"
    );

    // site lifecycle
    delegate.addAll(
      "pre-site",
      "site",
      "post-site",
      "site-deploy"
    );
  }

  @Override
  public void complete(final LineReader reader, final ParsedLine line, final List<Candidate> candidates) {
    delegate.complete(reader, line, candidates);
  }
}
