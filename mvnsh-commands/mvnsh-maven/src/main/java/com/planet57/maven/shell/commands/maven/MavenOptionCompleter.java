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

import java.util.List;

import javax.inject.Named;
import javax.inject.Singleton;

import org.jline.reader.Candidate;
import org.jline.reader.Completer;
import org.jline.reader.LineReader;
import org.jline.reader.ParsedLine;

import com.planet57.gshell.util.jline.StringsCompleter2;

/**
 * Maven option completer.
 *
 * @since 3.0
 */
@Named("maven-option")
@Singleton
public class MavenOptionCompleter
  implements Completer
{
  private final StringsCompleter2 delegate = new StringsCompleter2();

  public MavenOptionCompleter() {
    delegate.addAll(
      "-am", "--also-make",
      "-B", "--batch-mode",
      "-e", "--errors",
      "-N", "--non-recursive",
      "-X", "--debug"
    );
  }

  @Override
  public void complete(final LineReader reader, final ParsedLine line, final List<Candidate> candidates) {
    delegate.complete(reader, line, candidates);
  }
}
