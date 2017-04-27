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

import com.planet57.gshell.plexus.PlexusRuntime;
import com.planet57.gshell.util.jline.StringsCompleter2;
import org.apache.maven.lifecycle.Lifecycle;
import org.codehaus.plexus.component.repository.exception.ComponentLookupException;
import org.jline.reader.Candidate;
import org.jline.reader.Completer;
import org.jline.reader.LineReader;
import org.jline.reader.ParsedLine;
import org.sonatype.goodies.common.ComponentSupport;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Maven lifecycle phase completer.
 *
 * @since 3.0
 */
@Named("maven-phase")
@Singleton
public class MavenPhaseCompleter
  extends ComponentSupport
  implements Completer
{
  private final StringsCompleter2 delegate = new StringsCompleter2();

  @Inject
  public MavenPhaseCompleter(final PlexusRuntime plexus) {
    checkNotNull(plexus);

    try {
      addPhases(plexus, "default");
      addPhases(plexus, "clean");
      addPhases(plexus, "site");
    }
    catch (ComponentLookupException e) {
      throw new RuntimeException(e);
    }
  }

  private void addPhases(final PlexusRuntime plexus, final String roleHint) throws ComponentLookupException {
    Lifecycle lifecycle = plexus.lookup(Lifecycle.class, roleHint);
    log.debug("Adding phases for lifecycle: {}", lifecycle);
    delegate.addAll(lifecycle.getPhases());
  }

  @Override
  public void complete(final LineReader reader, final ParsedLine line, final List<Candidate> candidates) {
    delegate.complete(reader, line, candidates);
  }
}
