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

import org.junit.Test

import com.planet57.gshell.testharness.CommandTestSupport
import org.sonatype.plexus.components.sec.dispatcher.DefaultSecDispatcher

/**
 * Tests for {@link EncryptPasswordAction}.
 */
class EncryptPasswordActionTest
    extends CommandTestSupport
{
  EncryptPasswordActionTest() {
    super(EncryptPasswordAction.class)
  }

  @Test
  void 'encrypt master password'() {
    Object result = executeCommand('--master', 'changeme')
    assertEqualsSuccess(result)

    // TODO: verify; result is not the value due to compat with maven core-its
  }

  @Test
  void 'encrypt password'() {
    // setup security configuration file required to encrypt
    File configFile = util.createTempFile('settings-security.xml')
    configFile.text = '<settingsSecurity><master>{ZMqZbaOUj68HIixUY8QipRT6ZCsXviNpcP3X7QvXEDc=}</master></settingsSecurity>' // changeme
    System.setProperty(DefaultSecDispatcher.SYSTEM_PROPERTY_SEC_LOCATION, configFile.absolutePath)

    Object result = executeCommand('foo')
    assertEqualsSuccess(result)

    // TODO: verify; result is not the value due to compat with maven core-its
  }
}
