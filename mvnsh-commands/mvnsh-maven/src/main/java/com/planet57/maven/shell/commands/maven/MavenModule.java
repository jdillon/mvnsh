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

import com.google.inject.Binder;
import com.google.inject.Module;
import com.google.inject.Provides;
import org.codehaus.plexus.classworlds.ClassWorld;

import javax.inject.Named;
import javax.inject.Singleton;

/**
 * Maven module.
 *
 * @since 3.0
 */
@Named
public class MavenModule
  implements Module
{
  @Override
  public void configure(final Binder binder) {
    // empty
  }

  /**
   * Prepare the {@link ClassWorld} instance required by Maven.
   */
  @Provides
  @Singleton
  public ClassWorld provideClassWorld() {
    return new ClassWorld("plexus.core", Thread.currentThread().getContextClassLoader());
  }
}
