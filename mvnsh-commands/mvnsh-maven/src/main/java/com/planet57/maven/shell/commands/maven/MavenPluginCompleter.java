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
 * Maven plugins completer.
 *
 * @since 3.0
 */
@Named("maven-plugin")
@Singleton
public class MavenPluginCompleter
  implements Completer
{
  private static StringsCompleter2 delegate = new StringsCompleter2();

  public MavenPluginCompleter() {
    delegate.addAll(
      "deploy:deploy-file"
    );

    delegate.addAll(
      "dependency:analyze",
      "dependency:analyze-dep-mgt",
      "dependency:analyze-duplicate",
      "dependency:analyze-only",
      "dependency:analyze-report",
      "dependency:build-classpath",
      "dependency:copy",
      "dependency:copy-dependencies",
      "dependency:get",
      "dependency:go-offline",
      "dependency:help",
      "dependency:list",
      "dependency:list-repositories",
      "dependency:properties",
      "dependency:purge-local-repository",
      "dependency:resolve",
      "dependency:resolve-plugins",
      "dependency:sources",
      "dependency:tree",
      "dependency:unpack",
      "dependency:unpack-dependencies"
    );

    delegate.addAll(
      "install:install-file"
    );

    delegate.addAll(
      "release:clean",
      "release:prepare",
      "release:rollback",
      "release:perform",
      "release:stage",
      "release:branch",
      "release:update-versions"
    );

    delegate.addAll(
      "site:site",
      "site:deploy",
      "site:run",
      "site:stage",
      "site:stage-deploy"
    );
  }

  @Override
  public void complete(final LineReader reader, final ParsedLine line, final List<Candidate> candidates) {
    delegate.complete(reader, line, candidates);
  }
}
