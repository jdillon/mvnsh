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
package com.planet57.maven.shell.commands.maven

import com.planet57.gshell.testharness.CommandTestSupport
import org.junit.Test

/**
 * Tests for {@link MavenAction}.
 */
class MavenActionTest
    extends CommandTestSupport
{
  MavenActionTest() {
    super(MavenAction.class)
  }

  @Test
  void 'mvn package'() {
    File settings = new File(getClass().getResource('settings.xml').toURI())
    println("Settings: $settings")

    File pom = new File(getClass().getResource('test1.pom').toURI())
    println("POM: $pom")

    //File repoDir = new File(new File(System.getProperty('basedir')), 'target/test-repo')
    //println("Repo Dir: $repoDir")

    Object result = executeCommand(
        '-B', '-e', '-V',
        '-f', pom.absolutePath,
        '-s', settings.absolutePath,
        // "-Dmaven.repo.local=$repoDir",
        'package')

    println("OUT: ${io.outputString}")
    println("ERR: ${io.errorString}")

    assert result == 0
  }

  /**
   * Customized help test as this isn't using the default help support by gshell.
   */
  @Override
  @Test
  void testHelp() {
    assert executeCommand('--help') == 0
    assert executeCommand('-h') == 0
  }
}
