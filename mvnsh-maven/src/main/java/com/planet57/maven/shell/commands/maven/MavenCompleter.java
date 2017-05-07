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

import org.jline.reader.Candidate;
import org.jline.reader.Completer;
import org.jline.reader.LineReader;
import org.jline.reader.ParsedLine;
import org.jline.reader.impl.completer.AggregateCompleter;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Completer for {@link MavenAction}.
 */
@Named
@Singleton
public class MavenCompleter
  implements Completer
{
  private final Completer delegate;

  @Inject
  public MavenCompleter(@Named("maven-option") final Completer mavenOption,
                        @Named("maven-phase") final Completer mavenPhase,
                        @Named("maven-plugin-goal") final Completer mavenPluginGoal)
  {
    checkNotNull(mavenOption);
    checkNotNull(mavenPhase);
    checkNotNull(mavenPluginGoal);
    this.delegate = new AggregateCompleter(mavenOption, mavenPhase, mavenPluginGoal);
  }

  @Override
  public void complete(final LineReader reader, final ParsedLine line, final List<Candidate> candidates) {
    delegate.complete(reader, line, candidates);;
  }
}
