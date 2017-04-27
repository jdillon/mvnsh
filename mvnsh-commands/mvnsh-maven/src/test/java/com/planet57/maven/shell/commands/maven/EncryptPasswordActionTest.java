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

import org.junit.Test;

import com.planet57.gshell.testharness.CommandTestSupport;

/**
 * Tests for {@link EncryptPasswordAction}.
 */
public class EncryptPasswordActionTest
    extends CommandTestSupport
{
  public EncryptPasswordActionTest() {
    super(EncryptPasswordAction.class);
  }

  @Test
  public void test1() throws Exception {
    Object result = executeWithArgs("foo");
    assertEqualsSuccess(result);
  }
}
