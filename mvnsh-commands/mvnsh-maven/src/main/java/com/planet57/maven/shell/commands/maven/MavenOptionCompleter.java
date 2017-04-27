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
import java.util.concurrent.atomic.AtomicReference;

import javax.inject.Named;
import javax.inject.Singleton;

import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.maven.cli.CLIManager;
import org.jline.reader.Candidate;
import org.jline.reader.Completer;
import org.jline.reader.LineReader;
import org.jline.reader.ParsedLine;

import com.planet57.gshell.util.jline.StringsCompleter2;

import static com.google.common.base.Preconditions.checkState;

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
    mavenOptions().forEach(option -> {
      if (option.hasArg() || option.hasArgs() || option.hasOptionalArg()) {
        // TODO: adjust for options with arguments; for now omit
      }
      else {
        delegate.add("-" + option.getOpt());
        delegate.add("--" + option.getLongOpt());
      }
    });
  }

  /**
   * Extract {@code mvn} options from {@link CLIManager}.
   */
  private static Iterable<Option> mavenOptions() {
    AtomicReference<Options> holder = new AtomicReference<>();
    new CLIManager() {
      {
        // expose protected options which is setup by super ctor
        holder.set(options);
      }
    };

    Options options = holder.get();
    checkState(options != null);
    return options.getOptions();
  }

  @Override
  public void complete(final LineReader reader, final ParsedLine line, final List<Candidate> candidates) {
    delegate.complete(reader, line, candidates);
  }
}
